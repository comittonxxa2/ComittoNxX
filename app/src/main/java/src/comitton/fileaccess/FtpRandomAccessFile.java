package src.comitton.fileaccess;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.BitSet;

import org.apache.commons.net.ftp.FTPClient;
import src.comitton.common.Logcat;

public class FtpRandomAccessFile implements Closeable {
	private final String uri;
	private final String user;
	private final String pass;
	private final String port;
	private final boolean passive;
	private final boolean isFtps;
	
	private FTPClient ftpClient;
	private File cacheFile;
	private RandomAccessFile randomAccessFile;
	private volatile boolean mClosed = false;
	
	private long fileLength = -1;
	private long position = 0;

	// 小刻みなシーク・逆順読み込みに最適化したブロックサイズ(64KB)
	private static final int BLOCK_SIZE = 64 * 1024;
	private BitSet downloadedBlocks;

	public FtpRandomAccessFile(String uri, String user, String pass, boolean isFtps, String port, boolean passive) throws IOException {
		this.uri = uri;
		this.user = user;
		this.pass = pass;
		this.port = port;
		this.passive = passive;
		this.isFtps = isFtps;
		connectAndInit();
	}

	private synchronized void connectAndInit() throws IOException {
		this.ftpClient = FtpFileAccess.connectAndLogin(uri, user, pass, isFtps, port, passive);
		String remotePath = FtpFileAccess.parsePath(uri);
		this.fileLength = FtpFileAccess.getFileSize(ftpClient, remotePath);
		if (this.fileLength < 0) {
			throw new IOException("File not found or unable to get length: " + this.uri);
		}
		try {
			cacheFile = File.createTempFile("ftp_fine_cache_", ".tmp");
			cacheFile.deleteOnExit();
			randomAccessFile = new RandomAccessFile(cacheFile, "rw");
			randomAccessFile.setLength(fileLength);
		}
		catch (IOException e) {
			close();
			throw new IOException("Failed to create local cache file: " + e.getMessage());
		}
		int totalBlocks = (int) ((fileLength + BLOCK_SIZE - 1) / BLOCK_SIZE);
		downloadedBlocks = new BitSet(totalBlocks);
	}

	public long length() {
		return this.fileLength;
	}

	public synchronized void seek(long pos) throws IOException {
		if (mClosed || randomAccessFile == null) {
			return;
		}
		if (pos < 0) throw new IOException("Negative seek");
		try {
			this.position = Math.min(pos, this.fileLength);
			if (randomAccessFile != null) {
				randomAccessFile.seek(this.position);
			}
		}
		catch (Exception e) {
			Logcat.e(Logcat.LOG_LEVEL_WARN, "Seek error: " + e.getMessage());
			throw new IOException("Seek failed", e);
		}
	}

	public long getFilePointer() {
		return this.position;
	}

	public synchronized int read(byte[] b, int off, int len) throws IOException {
		if (mClosed || randomAccessFile == null || b == null) {
			return -1;
		}
		if (b == null) throw new NullPointerException();
		if (off < 0 || len < 0 || off + len > b.length) throw new IndexOutOfBoundsException();
		if (len == 0) return 0;
		try {
			if (this.position >= this.fileLength) return -1;
			int bytesToRead = (int) Math.min(len, this.fileLength - this.position);
			if (bytesToRead <= 0) return -1;
			// 読み込み範囲のブロックがキャッシュされているか確認・取得
			ensureDataCached(this.position, bytesToRead);
			randomAccessFile.seek(this.position);
			int readBytes = randomAccessFile.read(b, off, bytesToRead);
			if (readBytes > 0) {
				this.position += readBytes;
			}
			return (readBytes < 0) ? -1 : readBytes;
		}
		catch (Exception e) {
			Logcat.e(Logcat.LOG_LEVEL_WARN, "Custom InStream read error: " + e.getMessage());
			return -1;
		}
	}

	private void ensureDataCached(long startPos, int length) throws IOException {
		long endPos = startPos + length - 1;
		int startBlock = (int) (startPos / BLOCK_SIZE);
		int endBlock = (int) (endPos / BLOCK_SIZE);
		for (int blockIdx = startBlock; blockIdx <= endBlock; blockIdx++) {
			if (!downloadedBlocks.get(blockIdx)) {
				downloadBlock(blockIdx);
			}
		}
	}

	private synchronized void downloadBlock(int blockIndex) throws IOException {
		if (downloadedBlocks.get(blockIndex)) {
			return;
		}
		long blockStart = (long) blockIndex * BLOCK_SIZE;
		long blockEnd = Math.min(blockStart + BLOCK_SIZE, fileLength);
		int expectedBytes = (int) (blockEnd - blockStart);
		// 接続が切れていた場合の自動再接続
		if (ftpClient == null || !ftpClient.isConnected()) {
			try {
				if (ftpClient != null) {
					try { ftpClient.disconnect(); }
					catch (Exception ignored) {}
				}
				ftpClient = FtpFileAccess.connectAndLogin(uri, user, pass, isFtps, port, passive);
			}
			catch (Exception e) {
				throw new IOException("Failed to reconnect FTP server: " + e.getMessage());
			}
		}
		// REST 0 対策
		if (blockStart > 0) {
			ftpClient.setRestartOffset(blockStart);
		}
		// 10秒タイムアウト
		ftpClient.setSoTimeout(10000);
		String remotePath = FtpFileAccess.parsePath(uri);
		InputStream in = null;
		boolean streamOpened = false;
		try {
			in = ftpClient.retrieveFileStream(remotePath);
			if (in == null) {
				throw new IOException("Failed to open stream for block " + blockIndex);
			}
			streamOpened = true;
			randomAccessFile.seek(blockStart);
			byte[] buf = new byte[8192];
			int totalRead = 0;
			while (totalRead < expectedBytes) {
				int toRead = Math.min(buf.length, expectedBytes - totalRead);
				int read = in.read(buf, 0, toRead);
				if (read <= 0) {
					break;
				}
				randomAccessFile.write(buf, 0, read);
				totalRead += read;
			}
			// 読み込めた場合はブロック完了とする
			if (totalRead > 0) {
				downloadedBlocks.set(blockIndex);
			}
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException ignored) {}
			}
			if (streamOpened) {
				try {
					ftpClient.completePendingCommand();
				}
				catch (Exception ignored) {}
			}
		}
	}

	public void write(byte[] buffer, int offset, int len) throws IOException {
		throw new IOException("FTP random access write not supported.");
	}

	@Override
	public synchronized void close() throws IOException {
		if (mClosed) return;
		mClosed = true;
		if (randomAccessFile != null) {
			try {
				randomAccessFile.close();
			}
			catch (IOException ignored) {}
			randomAccessFile = null;
		}
		if (cacheFile != null && cacheFile.exists()) {
			try {
				cacheFile.delete();
			}
			catch (Exception ignored) {}
			cacheFile = null;
		}
		if (ftpClient != null && ftpClient.isConnected()) {
			try {
				ftpClient.logout();
				ftpClient.disconnect();
			}
			catch (Exception ignored) {}
			ftpClient = null;
		}
	}
}
