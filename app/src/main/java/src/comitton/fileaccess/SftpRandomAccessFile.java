package src.comitton.fileaccess;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.BitSet;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;

import src.comitton.common.Logcat;

public class SftpRandomAccessFile implements Closeable {
	private static final String TAG = "SftpRandomAccessFile";

	private final String uri;
	private final String user;
	private final String pass;
	private final String portnumber;

	private Session session;
	private ChannelSftp channelSftp;
	private File cacheFile;
	private RandomAccessFile randomAccessFile;
	private volatile boolean mClosed = false;

	private long fileLength = -1;
	private long position = 0;

	// 小刻みなシーク・逆順読み込みに最適化したブロックサイズ(64KB)
	private static final int BLOCK_SIZE = 64 * 1024;
	private BitSet downloadedBlocks;

	public SftpRandomAccessFile(String uri, String user, String pass, String portnumber) throws IOException {
		this.uri = uri;
		this.user = user;
		this.pass = pass;
		this.portnumber = portnumber;
		connectAndInit();
	}

	private synchronized void connectAndInit() throws IOException {
		try {
			Object[] sc = SftpFileAccess.connectAndOpenChannel(uri, user, pass, portnumber);
			this.session = (Session) sc[0];
			this.channelSftp = (ChannelSftp) sc[1];
			String remotePath = SftpFileAccess.parsePath(uri);
			SftpATTRS attrs = channelSftp.lstat(remotePath);
			this.fileLength = attrs.getSize();
			if (this.fileLength < 0) {
				throw new IOException("File not found or unable to get length: " + this.uri);
			}
			cacheFile = File.createTempFile("sftp_cache_", ".tmp");
			cacheFile.deleteOnExit();
			randomAccessFile = new RandomAccessFile(cacheFile, "rw");
			randomAccessFile.setLength(fileLength);
			int totalBlocks = (int) ((fileLength + BLOCK_SIZE - 1) / BLOCK_SIZE);
			downloadedBlocks = new BitSet(totalBlocks);
		}
		catch (Exception e) {
			close();
			throw new IOException("Failed to initialize SftpRandomAccessFile: " + e.getMessage(), e);
		}
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
		if (session == null || !session.isConnected() || channelSftp == null || !channelSftp.isConnected()) {
			try {
				if (channelSftp != null) {
					try { channelSftp.exit(); } catch (Exception ignored) {}
				}
				if (session != null) {
					try { session.disconnect(); } catch (Exception ignored) {}
				}
				Object[] sc = SftpFileAccess.connectAndOpenChannel(uri, user, pass, portnumber);
				this.session = (Session) sc[0];
				this.channelSftp = (ChannelSftp) sc[1];
			}
			catch (Exception e) {
				throw new IOException("Failed to reconnect SFTP server: " + e.getMessage(), e);
			}
		}
		String remotePath = SftpFileAccess.parsePath(uri);
		InputStream in = null;
		try {
			// JSchの機能で指定オフセット（skip）からInputStreamを取得
			in = channelSftp.get(remotePath, null, blockStart);
			if (in == null) {
				throw new IOException("Failed to open stream for block " + blockIndex);
			}
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
			if (totalRead > 0) {
				downloadedBlocks.set(blockIndex);
			}
		}
		catch (Exception e) {
			throw new IOException("SFTP downloadBlock error (block " + blockIndex + "): " + e.getMessage(), e);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException ignored) {}
			}
		}
	}

	public void write(byte[] buffer, int offset, int len) throws IOException {
		throw new IOException("SFTP random access write not supported.");
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
		if (channelSftp != null) {
			try {
				channelSftp.exit();
			}
			catch (Exception ignored) {}
			channelSftp = null;
		}
		if (session != null) {
			try {
				session.disconnect();
			}
			catch (Exception ignored) {}
			session = null;
		}
	}
}
