package src.comitton.fileaccess;

import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.hierynomus.mssmb2.SMB2ShareAccess;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

// SMBJライブラリを使用してランダムアクセス(read/seek)を提供するラッパークラス
public class SmbjRandomAccessFile implements SmbRandomAccessFileCompat {
	private static final String TAG = "SmbjRandomAccessFile";

	private SMBClient client;
	private Connection connection;
	private Session session;
	private DiskShare share;
	private File file;
	// 現在のシーク位置(ファイルポインタ)
	private long currentOffset = 0;
	// ファイル全体のサイズ
	private final long fileSize;

	public SmbjRandomAccessFile(String smbUri, String user, String pass, String mode) throws IOException {
		SmbUrlInfo info = new SmbUrlInfo(smbUri);
		String host = info.getHost();
		String shareName = info.getShare();
		String filePath = info.getPath();
		String domain = info.getDomain();
		String username = (user != null && !user.isEmpty()) ? user : info.getUsername();
		String password = (pass != null) ? pass : info.getPassword();
		SmbConfig config = SmbConfig.builder()
			// 1回のバッファサイズを64KBに制限してメモリ圧迫を防ぐ(これを入れないとメモリー不足でアプリが落ちる)
			.withReadBufferSize(64 * 1024)
			.withWriteBufferSize(64 * 1024)
			.withReadTimeout(5, TimeUnit.SECONDS)
			.withSoTimeout(3, TimeUnit.SECONDS)
			.withTransactTimeout(5, TimeUnit.SECONDS)
			.withMultiProtocolNegotiate(true)
			.build();
		// クライアント作成とサーバーへの接続
		this.client = new SMBClient();
		this.connection = client.connect(host);
		// ユーザー認証とセッション開始
		AuthenticationContext auth = new AuthenticationContext(
				username,
				password != null ? password.toCharArray() : new char[0],
				domain
		);
		this.session = connection.authenticate(auth);
		// 共有フォルダ(Share)への接続
		this.share = (DiskShare) session.connectShare(shareName);
		// フラグの設定("r"なら読み込み専用、"rw"なら書き込み許可)
		EnumSet<AccessMask> accessMask = "rw".equalsIgnoreCase(mode) ?
				EnumSet.of(AccessMask.GENERIC_READ, AccessMask.GENERIC_WRITE) :
				EnumSet.of(AccessMask.GENERIC_READ);
		// ファイルのオープン
		this.file = share.openFile(
				filePath,
				accessMask,
				null,
				SMB2ShareAccess.ALL,
				SMB2CreateDisposition.FILE_OPEN_IF,
				null
		);
		// ファイルサイズの取得・保持
		this.fileSize = file.getFileInformation().getStandardInformation().getEndOfFile();
	}

	@Override
	public void write(byte[] buffer, int byteOffset, int byteCount) throws IOException {
		if (file != null) {
			file.write(buffer, currentOffset, byteOffset, byteCount);
			currentOffset += byteCount;
		}
		else {
			throw new IOException("File is not open");
		}
	}

	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
		if (file == null) {
			throw new IOException("File is closed.");
		}
		int bytesRead = file.read(buffer, currentOffset, byteOffset, byteCount);
		if (bytesRead > 0) {
			currentOffset += bytesRead;
		}
		return bytesRead;
	}

	@Override
	public void seek(long position) throws IOException {
		if (position < 0) {
			throw new IOException("Negative seek offset");
		}
		this.currentOffset = position;
	}

	@Override
	public long getFilePointer() throws IOException {
		return this.currentOffset;
	}

	@Override
	public long length() throws IOException {
		return this.fileSize;
	}

	@Override
	public void close() throws IOException {
		try { if (file != null) file.close(); } catch (Exception ignored) {}
		try { if (share != null) share.close(); } catch (Exception ignored) {}
		try { if (session != null) session.close(); } catch (Exception ignored) {}
		try { if (connection != null) connection.close(); } catch (Exception ignored) {}
		try { if (client != null) client.close(); } catch (Exception ignored) {}
		file = null;
		share = null;
		session = null;
		connection = null;
		client = null;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (file != null) {
			return file.getInputStream();
		}
		return new InputStream() {
			@Override
			public int read() throws IOException {
				byte[] b = new byte[1];
				int n = SmbjRandomAccessFile.this.read(b, 0, 1);
				return n == -1 ? -1 : (b[0] & 0xFF);
			}
			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return SmbjRandomAccessFile.this.read(b, off, len);
			}
			@Override
			public void close() throws IOException {
				SmbjRandomAccessFile.this.close();
			}
		};
	}
}
