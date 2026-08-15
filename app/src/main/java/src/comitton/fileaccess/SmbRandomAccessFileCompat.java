package src.comitton.fileaccess;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface SmbRandomAccessFileCompat extends Closeable {
    void write(byte[] buffer, int byteOffset, int byteCount) throws IOException;
    int read(byte[] buffer, int byteOffset, int byteCount) throws IOException;
	void seek(long position) throws IOException;
	long getFilePointer() throws IOException;
	long length() throws IOException;
	void close() throws IOException;
    InputStream getInputStream() throws IOException;
}
