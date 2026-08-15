package src.comitton.fileaccess;

import org.codelibs.jcifs.smb.impl.SmbRandomAccessFile;
import java.io.IOException;
import java.io.InputStream;

import src.comitton.fileaccess.SmbRandomAccessFileCompat;

// JCIFS-NGライブラリを使用してランダムアクセス(read/seek)を提供するラッパークラス
public class JcifsNgRandomAccessFile implements SmbRandomAccessFileCompat {
	private static final String TAG = "JcifsNgRandomAccessFile";

	private final SmbRandomAccessFile raf;

	public JcifsNgRandomAccessFile(SmbRandomAccessFile raf) {
		this.raf = raf;
	}

	@Override
	public void write(byte[] buffer, int byteOffset, int byteCount) throws IOException {
		if (raf != null) {
			raf.write(buffer, byteOffset, byteCount);
		}
		else {
			throw new IOException("File is not open");
		}
	}

	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
		return raf.read(buffer, byteOffset, byteCount);
	}

	@Override
	public void seek(long position) throws IOException {
		raf.seek(position);
	}

	@Override
	public long getFilePointer() throws IOException {
		return raf.getFilePointer();
	}

	@Override
	public long length() throws IOException {
		return raf.length();
	}

	@Override
	public void close() throws IOException {
		raf.close();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new InputStream() {
			@Override
			public int read() throws IOException {
				byte[] b = new byte[1];
				int n = raf.read(b, 0, 1);
				return n == -1 ? -1 : (b[0] & 0xFF);
			}
			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return raf.read(b, off, len);
			}
			@Override
			public void close() throws IOException {
				raf.close();
			}
		};
	}
}
