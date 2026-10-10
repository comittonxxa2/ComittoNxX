package src.comitton.fileaccess;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.comitton.common.Logcat;

public class WebDavRandomAccessFile implements Closeable {
	private static final String TAG = "WebDavRandomAccessFile";

	private final String uri;
	private final Sardine sardine;
	private long position = 0;
	private long length = -1;

	// バッファリング用(小刻みな read による大量 HTTP リクエストの防止)
	// 64KB
	private static final int BUFFER_SIZE = 64 * 1024;
	private final byte[] buffer = new byte[BUFFER_SIZE];
	// バッファに保持されている先頭のファイルポインタ位置
	private long bufferStart = -1;
	// バッファ内に有効なデータのバイト数
	private int bufferLength = 0;

	public WebDavRandomAccessFile(String uri, String user, String pass) throws IOException {
		this.uri = WebDavUtil.normalizeUri(uri);
		this.sardine = WebDavFileAccess.getSardine(user, pass);

		List<DavResource> resources = this.sardine.list(this.uri, 0);
		if (!resources.isEmpty()) {
			this.length = resources.get(0).getContentLength();
		}
		else {
			throw new IOException("File not found: " + this.uri);
		}
	}
	// ファイル全体のサイズを取得
	public long length() {
		return this.length;
	}

	// 指定したバイト位置にシーク
	public void seek(long pos) throws IOException {
		if (pos < 0) {
			throw new IOException("Negative seek offset");
		}
		this.position = pos;
	}

	// 現在のファイルポインタ位置を取得
	public long getFilePointer() throws IOException {
		return this.position;
	}

	// 指定されたバッファへデータを読み込み(Rangeリクエスト + 内部キャッシュ)
	public synchronized int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("buffer is null");
		}
		if (off < 0 || len < 0 || off + len > b.length) {
			throw new IndexOutOfBoundsException();
		}
		if (len == 0) {
			return 0;
		}

		// EOF 判定
		if (this.position >= this.length) {
			return -1;
		}

		int totalRead = 0;

		while (len > 0 && this.position < this.length) {
			// 現在の position が内部バッファ内にあるか判定
			if (this.position >= bufferStart && this.position < bufferStart + bufferLength) {
				int bufferOffset = (int) (this.position - bufferStart);
				int bytesAvailable = bufferLength - bufferOffset;
				int bytesToCopy = Math.min(len, bytesAvailable);

				System.arraycopy(buffer, bufferOffset, b, off, bytesToCopy);

				this.position += bytesToCopy;
				off += bytesToCopy;
				len -= bytesToCopy;
				totalRead += bytesToCopy;
			} else {
				// バッファ外の場合は HTTP Range リクエストで新規データを取得・キャッシュする
				fillBuffer();
				if (bufferLength <= 0) {
					break; // これ以上読めない場合
				}
			}
		}

		return totalRead > 0 ? totalRead : -1;
	}

	// 1バイト読み込み用のオーバーロード
	public int read() throws IOException {
		byte[] oneByte = new byte[1];
		int result = read(oneByte, 0, 1);
		if (result == -1) {
			return -1;
		}
		return oneByte[0] & 0xFF;
	}

	// HTTP Range ヘッダーを発行して内部バッファにデータを読み込み
	private void fillBuffer() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		long fetchEnd = Math.min(this.position + BUFFER_SIZE - 1, this.length - 1);
		if (this.position > fetchEnd) {
			bufferLength = 0;
			return;
		}

		Map<String, String> headers = new HashMap<>();
		headers.put("Range", "bytes=" + this.position + "-" + fetchEnd);

		try (InputStream in = sardine.get(this.uri, headers)) {
			int bytesRead = 0;
			while (bytesRead < BUFFER_SIZE) {
				int read = in.read(buffer, bytesRead, BUFFER_SIZE - bytesRead);
				if (read == -1) {
					break;
				}
				bytesRead += read;
			}

			this.bufferStart = this.position;
			this.bufferLength = bytesRead;
		} catch (IOException e) {
			Logcat.e(logLevel, "Failed to fetch range: ", e);
			this.bufferLength = 0;
			throw e;
		}
	}

	// WebDAV ではランダム書き込み(Range書き込み)がサポートされていないため例外を送出
	public void write(byte[] buffer, int offset, int len) throws IOException {
		throw new IOException("WebDAV random access write is not supported.");
	}

	public void write(int b) throws IOException {
		throw new IOException("WebDAV random access write is not supported.");
	}

	@Override
	public void close() throws IOException {
		// Sardine インスタンスの内部リソース開放(必要に応じて処理を記述)
		this.bufferLength = 0;
		this.bufferStart = -1;
	}
}
