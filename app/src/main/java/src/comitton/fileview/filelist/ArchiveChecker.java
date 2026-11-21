package src.comitton.fileview.filelist;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import androidx.documentfile.provider.DocumentFile;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbRandomAccessFile;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;

// パスワード付きの圧縮ファイルの有無をチェックする
public class ArchiveChecker {
	// try～catchの塊のコードになってしまった

	private static final byte[] EMPTY_BYTES = new byte[0];
	// SMB/SAF向け64KBブロックバッファ
	private static final int DEFAULT_BUFFER_SIZE = 64 * 1024;

	public static class CheckResult {
		public final String file;
		public final int parts;
		public final String format;
		public final Boolean encrypted;
		public final String method;

		public CheckResult(String file, int parts, String format, Boolean encrypted, String method) {
			this.file = file;
			this.parts = parts;
			this.format = format;
			this.encrypted = encrypted;
			this.method = method;
		}

		@Override
		public String toString() {
			return "CheckResult{file='" + file + "', parts=" + parts + ", format='" + format + "', encrypted=" + encrypted + ", method='" + method + "'}";
		}
	}

	// ランダムアクセス可能なバイトソース抽象(ローカル / SMB / SAF 共通)
	public interface SeekableByteSource extends Closeable {
		long length() throws IOException;
		int readAt(long position, byte[] dst, int offset, int length) throws IOException;
	}

	//ストレージ上のファイル/ディレクトリ抽象(ローカル / jCIFS / SAF 共通)
	public interface ArchiveEntry {
		String getName();
		String getPath();
		boolean isDirectory() throws IOException;
		boolean isFile() throws IOException;
		long length() throws IOException;
		ArchiveEntry getParent();
		ArchiveEntry resolveChild(String childName) throws IOException;
		List<ArchiveEntry> listChildren() throws IOException;
		SeekableByteSource openByteSource() throws IOException;
	}

	// ローカルファイル用 SeekableByteSource
	public static class LocalSeekableByteSource implements SeekableByteSource {
		private final RandomAccessFile raf;
		private final long length;

		public LocalSeekableByteSource(File file) throws IOException {
			this.raf = new RandomAccessFile(file, "r");
			this.length = raf.length();
		}

		@Override
		public long length() {
			return length;
		}

		@Override
		public int readAt(long position, byte[] dst, int offset, int length) throws IOException {
			if (position < 0 || position >= this.length || length <= 0) {
				return -1;
			}
			raf.seek(position);
			int toRead = (int) Math.min(length, this.length - position);
			return raf.read(dst, offset, toRead);
		}

		@Override
		public void close() throws IOException {
			raf.close();
		}
	}

	// jCIFS (SmbRandomAccessFile) 用 SeekableByteSource
	public static class JcifsSeekableByteSource implements SeekableByteSource {
		private final SmbRandomAccessFile raf;
		private final long length;

		public JcifsSeekableByteSource(SmbFile smbFile) throws IOException {
			this.raf = new SmbRandomAccessFile(smbFile, "r");
			this.length = raf.length();
		}

		@Override
		public long length() {
			return length;
		}

		@Override
		public int readAt(long position, byte[] dst, int offset, int length) throws IOException {
			if (position < 0 || position >= this.length || length <= 0) {
				return -1;
			}
			raf.seek(position);
			int toRead = (int) Math.min(length, this.length - position);
			return raf.read(dst, offset, toRead);
		}

		@Override
		public void close() throws IOException {
			raf.close();
		}
	}

	// Android SAF (content://) 用 SeekableByteSource
	public static class SafSeekableByteSource implements SeekableByteSource {
		private final ParcelFileDescriptor pfd;
		private final java.io.FileInputStream fis;
		private final java.nio.channels.FileChannel channel;
		private final long length;

		public SafSeekableByteSource(Context context, Uri uri) throws IOException {
			this.pfd = context.getContentResolver().openFileDescriptor(uri, "r");
			if (this.pfd == null) {
				throw new IOException("Failed to open SAF ParcelFileDescriptor: " + uri);
			}
			this.fis = new java.io.FileInputStream(pfd.getFileDescriptor());
			this.channel = fis.getChannel();
			long statSize = pfd.getStatSize();
			this.length = statSize >= 0 ? statSize : channel.size();
		}

		@Override
		public long length() {
			return length;
		}

		@Override
		public int readAt(long position, byte[] dst, int offset, int length) throws IOException {
			if (position < 0 || position >= this.length || length <= 0) {
				return -1;
			}
			channel.position(position);
			int toRead = (int) Math.min(length, this.length - position);
			ByteBuffer bb = ByteBuffer.wrap(dst, offset, toRead);
			int totalRead = 0;
			while (bb.hasRemaining()) {
				int n = channel.read(bb);
				if (n <= 0) {
					break;
				}
				totalRead += n;
			}
			return totalRead > 0 ? totalRead : -1;
		}

		@Override
		public void close() throws IOException {
			IOException firstError = null;
			try {
				channel.close();
			}
			catch (IOException e) {
				firstError = e;
			}
			try {
				fis.close();
			}
			catch (IOException e) {
				if (firstError == null) firstError = e;
			}
			try {
				pfd.close();
			}
			catch (IOException e) {
				if (firstError == null) firstError = e;
			}
			if (firstError != null) {
				throw firstError;
			}
		}
	}

	// SMB/SAF越しの細かいシーク・小口リードを減らすブロックバッファラッパー
	public static class BufferedSeekableSource implements SeekableByteSource {
		private final SeekableByteSource delegate;
		private final byte[] buffer;
		private long bufferStart = -1;
		private int bufferValidLen = 0;

		public BufferedSeekableSource(SeekableByteSource delegate) {
			this(delegate, DEFAULT_BUFFER_SIZE);
		}

		public BufferedSeekableSource(SeekableByteSource delegate, int bufferSize) {
			this.delegate = delegate;
			this.buffer = new byte[Math.max(4096, bufferSize)];
		}

		@Override
		public long length() throws IOException {
			return delegate.length();
		}

		@Override
		public int readAt(long position, byte[] dst, int offset, int length) throws IOException {
			long totalLen = length();
			if (position < 0 || position >= totalLen || length <= 0) {
				return -1;
			}
			int maxToRead = (int) Math.min(length, totalLen - position);
			int copied = 0;

			while (copied < maxToRead) {
				long curPos = position + copied;
				if (bufferStart < 0 || curPos < bufferStart || curPos >= bufferStart + bufferValidLen) {
					if (maxToRead - copied >= buffer.length) {
						int directRead = delegate.readAt(curPos, dst, offset + copied, maxToRead - copied);
						if (directRead <= 0) {
							break;
						}
						copied += directRead;
						continue;
					}
					bufferStart = curPos;
					bufferValidLen = 0;
					int totalFetched = 0;
					while (totalFetched < buffer.length) {
						int n = delegate.readAt(bufferStart + totalFetched, buffer, totalFetched, buffer.length - totalFetched);
						if (n <= 0) {
							break;
						}
						totalFetched += n;
					}
					bufferValidLen = totalFetched;
					if (bufferValidLen <= 0) {
						break;
					}
				}

				int bufOffset = (int) (curPos - bufferStart);
				int avail = bufferValidLen - bufOffset;
				int step = Math.min(avail, maxToRead - copied);
				System.arraycopy(buffer, bufOffset, dst, offset + copied, step);
				copied += step;
			}

			return copied > 0 ? copied : -1;
		}

		@Override
		public void close() throws IOException {
			delegate.close();
		}
	}

	// ローカル File 実装
	public static class LocalArchiveEntry implements ArchiveEntry {
		private final File file;

		public LocalArchiveEntry(File file) {
			this.file = file;
		}

		@Override
		public String getName() {
			return file.getName();
		}

		@Override
		public String getPath() {
			return file.getPath();
		}

		@Override
		public boolean isDirectory() {
			return file.isDirectory();
		}

		@Override
		public boolean isFile() {
			return file.isFile();
		}

		@Override
		public long length() {
			return file.length();
		}

		@Override
		public ArchiveEntry getParent() {
			File p = file.getParentFile();
			return p != null ? new LocalArchiveEntry(p) : null;
		}

		@Override
		public ArchiveEntry resolveChild(String childName) {
			return new LocalArchiveEntry(new File(file, childName));
		}

		@Override
		public List<ArchiveEntry> listChildren() {
			File[] children = file.listFiles();
			List<ArchiveEntry> list = new ArrayList<>();
			if (children != null) {
				for (File c : children) {
					list.add(new LocalArchiveEntry(c));
				}
			}
			return list;
		}

		@Override
		public SeekableByteSource openByteSource() throws IOException {
			return new BufferedSeekableSource(new LocalSeekableByteSource(file));
		}
	}

	// jCIFS SmbFile 実装
	public static class JcifsArchiveEntry implements ArchiveEntry {
		private final SmbFile smbFile;

		public JcifsArchiveEntry(SmbFile smbFile) {
			this.smbFile = smbFile;
		}

		@Override
		public String getName() {
			String raw = smbFile.getName();
			if (raw.endsWith("/")) {
				raw = raw.substring(0, raw.length() - 1);
			}
			return raw;
		}

		@Override
		public String getPath() {
			return smbFile.getPath();
		}

		@Override
		public boolean isDirectory() throws IOException {
			try {
				return smbFile.isDirectory();
			}
			catch (SmbException e) {
				throw new IOException("SMB isDirectory failed: " + smbFile.getPath(), e);
			}
		}

		@Override
		public boolean isFile() throws IOException {
			try {
				return smbFile.isFile();
			}
			catch (SmbException e) {
				throw new IOException("SMB isFile failed: " + smbFile.getPath(), e);
			}
		}

		@Override
		public long length() throws IOException {
			try {
				return smbFile.length();
			}
			catch (SmbException e) {
				throw new IOException("SMB length failed: " + smbFile.getPath(), e);
			}
		}

		@Override
		public ArchiveEntry getParent() {
			String parentUrl = smbFile.getParent();
			if (parentUrl == null || parentUrl.equals(smbFile.getPath())) {
				return null;
			}
			try {
				return new JcifsArchiveEntry(new SmbFile(parentUrl, smbFile.getContext()));
			}
			catch (Exception e) {
				return null;
			}
		}

		@Override
		public ArchiveEntry resolveChild(String childName) throws IOException {
			String baseUrl = smbFile.getPath();
			if (!baseUrl.endsWith("/")) {
				baseUrl += "/";
			}
			try {
				return new JcifsArchiveEntry(new SmbFile(baseUrl + childName, smbFile.getContext()));
			}
			catch (Exception e) {
				throw new IOException("SMB resolveChild failed: " + childName, e);
			}
		}

		@Override
		public List<ArchiveEntry> listChildren() throws IOException {
			List<ArchiveEntry> list = new ArrayList<>();
			try {
				SmbFile[] children = smbFile.listFiles();
				if (children != null) {
					for (SmbFile c : children) {
						list.add(new JcifsArchiveEntry(c));
					}
				}
				return list;
			}
			catch (SmbException e) {
				throw new IOException("SMB listFiles failed: " + smbFile.getPath(), e);
			}
		}

		@Override
		public SeekableByteSource openByteSource() throws IOException {
			return new BufferedSeekableSource(new JcifsSeekableByteSource(smbFile));
		}
	}

	// Android SAF (content://) 用 ArchiveEntry
	public static class SafArchiveEntry implements ArchiveEntry {
		private final Context appContext;
		private final Uri uri;
		private final DocumentFile docFile;
		private final ArchiveEntry parentEntry;
		private final String cachedName;
		private final long cachedLength;

		public SafArchiveEntry(Context context, Uri uri) {
			this(context, uri, null, null);
		}

		public SafArchiveEntry(Context context, Uri uri, DocumentFile docFile, ArchiveEntry parentEntry) {
			this.appContext = context.getApplicationContext();
			this.uri = uri;
			this.docFile = docFile != null ? docFile : DocumentFile.fromSingleUri(this.appContext, uri);
			this.parentEntry = parentEntry;
			this.cachedName = resolveDisplayName();
			this.cachedLength = resolveLength();
		}

		private String resolveDisplayName() {
			if (docFile != null && docFile.getName() != null) {
				return docFile.getName();
			}
			try (Cursor cursor = appContext.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
				if (cursor != null && cursor.moveToFirst()) {
					int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
					if (idx >= 0 && !cursor.isNull(idx)) {
						return cursor.getString(idx);
					}
				}
			}
			catch (Exception ignored) {
			}
			String last = uri.getLastPathSegment();
			return last != null ? last : uri.toString();
		}

		private long resolveLength() {
			if (docFile != null && docFile.length() > 0) {
				return docFile.length();
			}
			try (Cursor cursor = appContext.getContentResolver().query(uri, new String[]{OpenableColumns.SIZE}, null, null, null)) {
				if (cursor != null && cursor.moveToFirst()) {
					int idx = cursor.getColumnIndex(OpenableColumns.SIZE);
					if (idx >= 0 && !cursor.isNull(idx)) {
						long sz = cursor.getLong(idx);
						if (sz >= 0) {
							return sz;
						}
					}
				}
			}
			catch (Exception ignored) {
			}
			return 0L;
		}

		@Override
		public String getName() {
			return cachedName;
		}

		@Override
		public String getPath() {
			return uri.toString();
		}

		@Override
		public boolean isDirectory() {
			return docFile != null && docFile.isDirectory();
		}

		@Override
		public boolean isFile() {
			if (docFile != null) {
				return docFile.isFile();
			}
			return cachedName != null && !cachedName.isEmpty();
		}

		@Override
		public long length() throws IOException {
			if (cachedLength > 0) {
				return cachedLength;
			}
			try (SeekableByteSource src = openByteSource()) {
				return src.length();
			}
		}

		@Override
		public ArchiveEntry getParent() {
			if (parentEntry != null) {
				return parentEntry;
			}
			if (docFile != null) {
				DocumentFile parentDoc = docFile.getParentFile();
				if (parentDoc != null) {
					return new SafArchiveEntry(appContext, parentDoc.getUri(), parentDoc, null);
				}
			}
			return null;
		}

		@Override
		public ArchiveEntry resolveChild(String childName) {
			if (docFile != null && docFile.isDirectory()) {
				DocumentFile childDoc = docFile.findFile(childName);
				if (childDoc != null) {
					return new SafArchiveEntry(appContext, childDoc.getUri(), childDoc, this);
				}
			}
			return new MissingSafArchiveEntry(uri, childName);
		}

		@Override
		public List<ArchiveEntry> listChildren() {
			List<ArchiveEntry> list = new ArrayList<>();
			if (docFile != null && docFile.isDirectory()) {
				DocumentFile[] children = docFile.listFiles();
				for (DocumentFile c : children) {
					list.add(new SafArchiveEntry(appContext, c.getUri(), c, this));
				}
			}
			return list;
		}

		@Override
		public SeekableByteSource openByteSource() throws IOException {
			return new BufferedSeekableSource(new SafSeekableByteSource(appContext, uri));
		}
	}

	private static class MissingSafArchiveEntry implements ArchiveEntry {
		private final String name;
		private final String path;

		MissingSafArchiveEntry(Uri baseUri, String childName) {
			this.name = childName;
			this.path = baseUri.toString() + "/" + childName;
		}

		@Override
		public String getName() { return name; }

		@Override
		public String getPath() { return path; }

		@Override
		public boolean isDirectory() { return false; }

		@Override
		public boolean isFile() { return false; }

		@Override
		public long length() { return 0L; }

		@Override
		public ArchiveEntry getParent() { return null; }

		@Override
		public ArchiveEntry resolveChild(String childName) { return this; }

		@Override
		public List<ArchiveEntry> listChildren() { return new ArrayList<>(); }

		@Override
		public SeekableByteSource openByteSource() throws IOException {
			throw new IOException("SAF entry does not exist: " + path);
		}
	}

	// 複数パートに跨る結合シーカブルリーダー
	public static class MultiPartReader implements Closeable {
		private final List<ArchiveEntry> parts;
		private final long[] sizes;
		private final long[] cumulativeOffsets;
		private final long totalSize;

		private long pos = 0;
		private int currentIdx = -1;
		private SeekableByteSource currentSource = null;

		public MultiPartReader(List<ArchiveEntry> partEntries) throws IOException {
			if (partEntries == null || partEntries.isEmpty()) {
				throw new IOException("No archive parts provided");
			}
			this.parts = partEntries;
			this.sizes = new long[partEntries.size()];
			this.cumulativeOffsets = new long[partEntries.size()];

			long sum = 0;
			for (int i = 0; i < partEntries.size(); i++) {
				this.cumulativeOffsets[i] = sum;
				long sz = partEntries.get(i).length();
				this.sizes[i] = Math.max(0L, sz);
				sum += this.sizes[i];
			}
			this.totalSize = sum;
		}

		public long length() {
			return totalSize;
		}

		public long getFilePointer() {
			return pos;
		}

		public void seek(long newPos) {
			this.pos = Math.max(0L, Math.min(newPos, totalSize));
		}

		public byte[] read(int size) throws IOException {
			if (pos >= totalSize || size <= 0) {
				return EMPTY_BYTES;
			}
			int toRead = (int) Math.min(size, totalSize - pos);
			byte[] result = new byte[toRead];
			int offset = 0;

			while (offset < toRead && pos < totalSize) {
				int targetIdx = findPartIndex(pos);
				long localOffset = pos - cumulativeOffsets[targetIdx];

				if (currentIdx != targetIdx) {
					closeCurrentSource();
					currentSource = parts.get(targetIdx).openByteSource();
					currentIdx = targetIdx;
				}

				int canRead = (int) Math.min(toRead - offset, sizes[targetIdx] - localOffset);
				int readBytes = currentSource.readAt(localOffset, result, offset, canRead);
				if (readBytes <= 0) {
					break;
				}
				offset += readBytes;
				pos += readBytes;
			}

			return (offset == toRead) ? result : Arrays.copyOf(result, offset);
		}

		public int readByte() throws IOException {
			byte[] b = read(1);
			return (b.length == 0) ? -1 : (b[0] & 0xFF);
		}

		private int findPartIndex(long position) {
			int idx = 0;
			for (int i = 0; i < sizes.length; i++) {
				if (position < cumulativeOffsets[i] + sizes[i]) {
					return i;
				}
				idx = i;
			}
			return idx;
		}

		private void closeCurrentSource() {
			if (currentSource != null) {
				try {
					currentSource.close();
				}
				catch (IOException ignored) {
				}
				finally {
					currentSource = null;
					currentIdx = -1;
				}
			}
		}

		@Override
		public void close() throws IOException {
			closeCurrentSource();
		}
	}

	// SevenZipJBinding 用 IInStream アダプタ(MultiPartReader を利用)
	private static class MultiPartInStream implements IInStream {
		private final MultiPartReader reader;

		MultiPartInStream(MultiPartReader reader) {
			this.reader = reader;
		}

		@Override
		public long seek(long offset, int seekOrigin) throws SevenZipException {
			long base;
			switch (seekOrigin) {
				case SEEK_SET:
					base = 0L;
					break;
				case SEEK_CUR:
					base = reader.getFilePointer();
					break;
				case SEEK_END:
					base = reader.length();
					break;
				default:
					throw new SevenZipException("Unsupported seekOrigin: " + seekOrigin);
			}
			long target = base + offset;
			if (target < 0) {
				throw new SevenZipException("Negative seek position: " + target);
			}
			reader.seek(Math.min(target, reader.length()));
			return reader.getFilePointer();
		}

		@Override
		public int read(byte[] data) throws SevenZipException {
			if (data == null) {
				return 0;
			}
			return readChunk(data, 0, data.length);
		}

		public int read(byte[] data, int off, int size) throws SevenZipException {
			if (data == null || off < 0 || size < 0 || off + size > data.length) {
				throw new SevenZipException("Invalid buffer arguments");
			}
			return readChunk(data, off, size);
		}

		private int readChunk(byte[] data, int off, int size) throws SevenZipException {
			try {
				if (size <= 0 || reader.getFilePointer() >= reader.length()) {
					return 0;
				}
				byte[] chunk = reader.read(size);
				if (chunk.length == 0) {
					return 0;
				}
				System.arraycopy(chunk, 0, data, off, chunk.length);
				return chunk.length;
			}
			catch (IOException e) {
				throw new SevenZipException("Failed reading from MultiPartReader", e);
			}
		}

		@Override
		public void close() throws IOException {
		}
	}

	private static SmbFile createSmbFile(String smbUrl, String user, String pass) throws IOException {
		CIFSContext baseCtx = SingletonContext.getInstance();
		if (user != null && !user.isEmpty()) {
			CIFSContext authCtx = baseCtx.withCredentials(
					new NtlmPasswordAuthenticator(null, user, pass != null ? pass : ""));
			return new SmbFile(smbUrl, authCtx);
		}
		return new SmbFile(smbUrl, baseCtx);
	}

	private static ByteBuffer leBuffer(byte[] data) {
		return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
	}

	private static boolean startsWith(byte[] data, byte[] prefix) {
		if (data.length < prefix.length) return false;
		for (int i = 0; i < prefix.length; i++) {
			if (data[i] != prefix[i]) return false;
		}
		return true;
	}

	private static int lastIndexOf(byte[] data, byte[] pattern) {
		for (int i = data.length - pattern.length; i >= 0; i--) {
			boolean match = true;
			for (int j = 0; j < pattern.length; j++) {
				if (data[i + j] != pattern[j]) {
					match = false;
					break;
				}
			}
			if (match) return i;
		}
		return -1;
	}

	private static CheckResult checkZip(String relName, int parts, MultiPartReader f) {
		try {
			if (f.length() < 22) {
				return new CheckResult(relName, parts, "ZIP", null, "Invalid ZIP size");
			}
			int readSize = (int) Math.min(f.length(), 65557L);
			f.seek(f.length() - readSize);
			byte[] tailData = f.read(readSize);

			byte[] eocdSig = new byte[]{0x50, 0x4B, 0x05, 0x06};
			int eocdPos = lastIndexOf(tailData, eocdSig);
			if (eocdPos == -1 || eocdPos + 22 > tailData.length) {
				return new CheckResult(relName, parts, "ZIP", null, "ZIP EOCD not found");
			}

			ByteBuffer eocdBuf = leBuffer(Arrays.copyOfRange(tailData, eocdPos, eocdPos + 22));
			long totalEntries = Short.toUnsignedInt(eocdBuf.getShort(10));
			long cdOffset = Integer.toUnsignedLong(eocdBuf.getInt(16));

			if (totalEntries == 0xFFFFL || cdOffset == 0xFFFFFFFFL) {
				int locatorPos = eocdPos - 20;
				byte[] locSig = new byte[]{0x50, 0x4B, 0x06, 0x07};
				if (locatorPos >= 0 && startsWith(Arrays.copyOfRange(tailData, locatorPos, locatorPos + 4), locSig)) {
					ByteBuffer locBuf = leBuffer(Arrays.copyOfRange(tailData, locatorPos, locatorPos + 20));
					f.seek(locBuf.getLong(8));
					byte[] zip64Eocd = f.read(56);
					byte[] z64Sig = new byte[]{0x50, 0x4B, 0x06, 0x06};
					if (zip64Eocd.length >= 56 && startsWith(zip64Eocd, z64Sig)) {
						ByteBuffer z64Buf = leBuffer(zip64Eocd);
						totalEntries = z64Buf.getLong(32);
						cdOffset = z64Buf.getLong(48);
					}
				}
			}

			f.seek(cdOffset);
			byte[] cdSig = new byte[]{0x50, 0x4B, 0x01, 0x02};
			for (long i = 0; i < totalEntries; i++) {
				byte[] header = f.read(46);
				if (header.length < 46 || !startsWith(header, cdSig)) break;
				ByteBuffer hBuf = leBuffer(header);
				int gpFlag = Short.toUnsignedInt(hBuf.getShort(8));
				int compMethod = Short.toUnsignedInt(hBuf.getShort(10));
				int nameLen = Short.toUnsignedInt(hBuf.getShort(28));
				int extraLen = Short.toUnsignedInt(hBuf.getShort(30));
				int commentLen = Short.toUnsignedInt(hBuf.getShort(32));

				if ((gpFlag & 0x0001) != 0) {
					String method;
					if (compMethod == 99) {
						f.seek(f.getFilePointer() + nameLen);
						method = detectZipAesStrength(f.read(extraLen));
					}
					else if ((gpFlag & 0x0040) != 0) {
						method = "ZIP (Strong Encryption)";
					}
					else {
						method = "ZIP (ZipCrypto)";
					}
					return new CheckResult(relName, parts, "ZIP", true, method);
				}
				f.seek(f.getFilePointer() + nameLen + extraLen + commentLen);
			}
			return new CheckResult(relName, parts, "ZIP", false, null);
		}
		catch (Exception e) {
			return new CheckResult(relName, parts, "ZIP", null, "ZIP Error: " + e.getMessage());
		}
	}

	private static String detectZipAesStrength(byte[] extraData) {
		int pos = 0;
		ByteBuffer buf = leBuffer(extraData);
		while (pos + 4 <= extraData.length) {
			int headerId = Short.toUnsignedInt(buf.getShort(pos));
			int dataSize = Short.toUnsignedInt(buf.getShort(pos + 2));
			pos += 4;
			if (headerId == 0x9901 && dataSize >= 7 && pos + dataSize <= extraData.length) {
				int strength = buf.get(pos + 4) & 0xFF;
				if (strength == 1) return "ZIP (AES-128)";
				if (strength == 2) return "ZIP (AES-192)";
				if (strength == 3) return "ZIP (AES-256)";
				return "ZIP (AES)";
			}
			pos += dataSize;
		}
		return "ZIP (AES)";
	}

	private static CheckResult checkRar4(String relName, int parts, MultiPartReader f) {
		try {
			f.seek(7);
			while (true) {
				long blockStart = f.getFilePointer();
				byte[] hdr = f.read(7);
				if (hdr.length < 7) break;
				ByteBuffer buf = leBuffer(hdr);
				int headType = buf.get(2) & 0xFF;
				int headFlags = Short.toUnsignedInt(buf.getShort(3));
				int headSize = Short.toUnsignedInt(buf.getShort(5));

				if (headType == 0x73 && (headFlags & 0x0080) != 0)
					return new CheckResult(relName, parts, "RAR4", true, "RAR4 (AES-128 / Header Encrypted)");
				if (headType == 0x74 && (headFlags & 0x0004) != 0)
					return new CheckResult(relName, parts, "RAR4", true, "RAR4 (AES-128 / File Encrypted)");
				if (headType == 0x7B) break;

				long addSize = 0;
				if ((headFlags & 0x8000) != 0 || headType == 0x74) {
					byte[] addBytes = f.read(4);
					if (addBytes.length < 4) break;
					addSize = Integer.toUnsignedLong(leBuffer(addBytes).getInt(0));
				}
				long nextPos = blockStart + headSize + addSize;
				if (nextPos <= blockStart) break;
				f.seek(nextPos);
			}
			return new CheckResult(relName, parts, "RAR4", false, null);
		}
		catch (Exception e) {
			return new CheckResult(relName, parts, "RAR4", null, "RAR4 Error: " + e.getMessage());
		}
	}

	private static Long readVint(MultiPartReader f) throws IOException {
		long value = 0;
		int shift = 0;
		while (true) {
			int b = f.readByte();
			if (b == -1) return null;
			value |= (long) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) return value;
			shift += 7;
			if (shift > 63) return null;
		}
	}

	private static List<Long> parseVint(ByteBuffer buf, int pos) {
		long value = 0;
		int shift = 0;
		while (pos < buf.limit()) {
			int b = buf.get(pos) & 0xFF;
			pos++;
			value |= (long) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) return Arrays.asList(value, (long) pos);
			shift += 7;
		}
		return Arrays.asList(value, (long) pos);
	}

	private static CheckResult checkRar5(String relName, int parts, MultiPartReader f) {
		try {
			f.seek(8);
			while (true) {
				if (f.read(4).length < 4) break;
				Long headSizeObj = readVint(f);
				if (headSizeObj == null) break;
				int headSize = headSizeObj.intValue();
				long bodyStart = f.getFilePointer();
				byte[] body = f.read(headSize);
				if (body.length < headSize) break;

				ByteBuffer bodyBuf = ByteBuffer.wrap(body);
				List<Long> r1 = parseVint(bodyBuf, 0);
				long headType = r1.get(0);
				List<Long> r2 = parseVint(bodyBuf, r1.get(1).intValue());
				long headFlags = r2.get(0);
				int pos = r2.get(1).intValue();

				if (headType == 4) return new CheckResult(relName, parts, "RAR5", true, "RAR5 (AES-256 / Header Encrypted)");

				long extraSize = 0;
				if ((headFlags & 0x0001) != 0) {
					List<Long> r3 = parseVint(bodyBuf, pos);
					extraSize = r3.get(0);
					pos = r3.get(1).intValue();
				}
				long dataSize = 0;
				if ((headFlags & 0x0002) != 0) {
					List<Long> r4 = parseVint(bodyBuf, pos);
					dataSize = r4.get(0);
				}

				if (headType == 2 && extraSize > 0 && extraSize <= body.length) {
					byte[] extraData = Arrays.copyOfRange(body, body.length - (int) extraSize, body.length);
					ByteBuffer extraBuf = ByteBuffer.wrap(extraData);
					int epos = 0;
					while (epos < extraData.length) {
						List<Long> er1 = parseVint(extraBuf, epos);
						int recSize = er1.get(0).intValue();
						epos = er1.get(1).intValue();
						int recEnd = epos + recSize;
						List<Long> er2 = parseVint(extraBuf, epos);
						if (er2.get(0) == 0x01) return new CheckResult(relName, parts, "RAR5", true, "RAR5 (AES-256 / File Encrypted)");
						epos = recEnd;
					}
				}
				if (headType == 5) break;
				f.seek(bodyStart + headSize + dataSize);
			}
			return new CheckResult(relName, parts, "RAR5", false, null);
		}
		catch (Exception e) {
			return new CheckResult(relName, parts, "RAR5", null, "RAR5 Error: " + e.getMessage());
		}
	}

	private static CheckResult check7z(String relName, int parts, MultiPartReader f) {
		IInArchive inArchive = null;
		try {
			f.seek(0L);
			inArchive = SevenZip.openInArchive(null, new MultiPartInStream(f));
			boolean isEncrypted = false;

			Object archiveEncrypted = inArchive.getArchiveProperty(PropID.ENCRYPTED);
			if (archiveEncrypted instanceof Boolean && (Boolean) archiveEncrypted) {
				isEncrypted = true;
			}

			if (!isEncrypted) {
				for (int i = 0; i < inArchive.getNumberOfItems(); i++) {
					Object itemEncrypted = inArchive.getProperty(i, PropID.ENCRYPTED);
					if (itemEncrypted instanceof Boolean && (Boolean) itemEncrypted) {
						isEncrypted = true;
						break;
					}
				}
			}

			String method = isEncrypted ? "7z (Encrypted)" : null;
			return new CheckResult(relName, parts, "7Z", isEncrypted, method);
		}
		catch (Exception e) {
			return new CheckResult(relName, parts, "7Z", true, "7z (Encrypted / " + e.getMessage() + ")");
		}
		finally {
			if (inArchive != null) {
				try {
					inArchive.close();
				}
				catch (Exception ignored) {
				}
			}
		}
	}

	private static CheckResult inspectParts(String relName, List<ArchiveEntry> partEntries) throws IOException {
		try (MultiPartReader reader = new MultiPartReader(partEntries)) {
			byte[] sig = reader.read(8);
			if (startsWith(sig, new byte[]{0x50, 0x4B, 0x03, 0x04}) || startsWith(sig, new byte[]{0x50, 0x4B, 0x05, 0x06}))
				return checkZip(relName, partEntries.size(), reader);
			if (Arrays.equals(sig, new byte[]{0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00}))
				return checkRar5(relName, partEntries.size(), reader);
			if (startsWith(sig, new byte[]{0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00}))
				return checkRar4(relName, partEntries.size(), reader);
			if (startsWith(sig, new byte[]{0x37, 0x7A, (byte) 0xBC, (byte) 0xAF, 0x27, 0x1C}))
				return check7z(relName, partEntries.size(), reader);
		}
		return null;
	}

	public static List<CheckResult> scanArchiveFolder(String folderPath, boolean recursive) throws IOException {
		return scanArchiveFolder(null, folderPath, null, null, recursive);
	}

	public static List<CheckResult> scanArchiveFolder(Context context, String folderOrUri, String user, String pass, boolean recursive) throws IOException {
		if (folderOrUri == null || folderOrUri.isEmpty()) {
			throw new IOException("Folder path or URI is empty");
		}
		if (folderOrUri.regionMatches(true, 0, "content://", 0, 10)) {
			if (context == null) {
				throw new IOException("Context is required for content:// URI: " + folderOrUri);
			}
			Uri uri = Uri.parse(folderOrUri);
			DocumentFile treeDoc = DocumentFile.fromTreeUri(context, uri);
			ArchiveEntry base = (treeDoc != null) ? new SafArchiveEntry(context, treeDoc.getUri(), treeDoc, null) : new SafArchiveEntry(context, uri);
			return scanArchiveFolder(base, recursive);
		}
		if (folderOrUri.regionMatches(true, 0, "smb://", 0, 6)) {
			return scanArchiveFolder(new JcifsArchiveEntry(createSmbFile(folderOrUri, user, pass)), recursive);
		}
		return scanArchiveFolder(new LocalArchiveEntry(new File(folderOrUri)), recursive);
	}

	public static List<CheckResult> scanArchiveFolder(ArchiveEntry base, boolean recursive) throws IOException {
		if (base == null || !base.isDirectory()) {
			throw new IOException("Folder not found: " + (base != null ? base.getPath() : "null"));
		}

		List<ArchiveEntry> files = new ArrayList<>();
		collectFiles(base, recursive, files);
		files.sort(Comparator.comparing(ArchiveEntry::getPath));

		Map<String, ArchiveEntry> siblingByLowerName = new HashMap<>();
		for (ArchiveEntry entry : files) {
			siblingByLowerName.put(entry.getName().toLowerCase(Locale.ROOT), entry);
		}

		Pattern partRar = Pattern.compile("\\.part(0*[2-9]|[1-9]\\d+)\\.rar$");
		Pattern oldRar = Pattern.compile("\\.r\\d{2,}$");
		Pattern splitZip = Pattern.compile("\\.z\\d{2,}$");
		Pattern numAny = Pattern.compile("\\.\\d{3,}$");
		Pattern numFirst = Pattern.compile("^(.+\\.)(0*1)$");
		List<CheckResult> results = new ArrayList<>();

		for (ArchiveEntry entry : files) {
			String name = entry.getName();
			String lower = name.toLowerCase(Locale.ROOT);
			if (partRar.matcher(lower).find() || oldRar.matcher(lower).find() || splitZip.matcher(lower).find()) continue;
			if (numAny.matcher(lower).find() && !numFirst.matcher(lower).find()) continue;

			List<ArchiveEntry> partEntries = new ArrayList<>();
			Matcher m = numFirst.matcher(name);
			if (m.find()) {
				String prefix = m.group(1);
				int digits = m.group(2).length();
				int idx = 1;
				while (true) {
					String candName = String.format(Locale.ROOT, "%s%0" + digits + "d", prefix, idx);
					ArchiveEntry cand = siblingByLowerName.get(candName.toLowerCase(Locale.ROOT));
					if (cand != null && cand.isFile()) {
						partEntries.add(cand);
						idx++;
					}
					else {
						break;
					}
				}
			}
			else {
				partEntries.add(entry);
			}

			String relName = buildRelativeName(base, entry);
			try {
				CheckResult res = inspectParts(relName, partEntries);
				if (res != null) results.add(res);
			}
			catch (Exception e) {
				results.add(new CheckResult(relName, partEntries.size(), "ERROR", null, e.getMessage()));
			}
		}
		return results;
	}

	private static void collectFiles(ArchiveEntry current, boolean recursive, List<ArchiveEntry> out) throws IOException {
		List<ArchiveEntry> children = current.listChildren();
		for (ArchiveEntry child : children) {
			if (child.isFile()) {
				out.add(child);
			}
			else if (recursive && child.isDirectory()) {
				collectFiles(child, true, out);
			}
		}
	}

	private static String buildRelativeName(ArchiveEntry base, ArchiveEntry target) {
		String basePath = base.getPath().replace('\\', '/');
		String targetPath = target.getPath().replace('\\', '/');
		if (!basePath.endsWith("/")) {
			basePath += "/";
		}
		if (targetPath.startsWith(basePath)) {
			return targetPath.substring(basePath.length());
		}
		return target.getName();
	}

	public static CheckResult checkSingleArchive(String pathOrUri) throws IOException {
		return checkSingleArchive(null, pathOrUri, null, null);
	}

	public static CheckResult checkSingleArchive(String pathOrUri, String user, String pass) throws IOException {
		return checkSingleArchive(null, pathOrUri, user, pass);
	}

	public static CheckResult checkSingleArchive(Context context, String pathOrUri) throws IOException {
		return checkSingleArchive(context, pathOrUri, null, null);
	}

	public static CheckResult checkSingleArchive(Context context, String pathOrUri, String user, String pass) throws IOException {
		if (pathOrUri == null || pathOrUri.isEmpty()) {
			throw new IOException("Path or URI is empty");
		}
		if (pathOrUri.regionMatches(true, 0, "content://", 0, 10)) {
			if (context == null) {
				throw new IOException("Context is required for content:// URI: " + pathOrUri);
			}
			return checkSingleArchive(new SafArchiveEntry(context, Uri.parse(pathOrUri)));
		}
		if (pathOrUri.regionMatches(true, 0, "smb://", 0, 6)) {
			return checkSingleArchive(new JcifsArchiveEntry(createSmbFile(pathOrUri, user, pass)));
		}
		return checkSingleArchive(new LocalArchiveEntry(new File(pathOrUri)));
	}

	public static CheckResult checkSingleArchive(File file) throws IOException {
		if (file == null) {
			throw new IOException("File is null");
		}
		return checkSingleArchive(new LocalArchiveEntry(file));
	}

	public static CheckResult checkSingleArchive(SmbFile smbFile) throws IOException {
		if (smbFile == null) {
			throw new IOException("SMB file is null");
		}
		return checkSingleArchive(new JcifsArchiveEntry(smbFile));
	}

	public static CheckResult checkSingleArchive(ArchiveEntry entry) throws IOException {
		if (entry == null || !entry.isFile()) {
			throw new IOException("File not found or not a regular file: " + (entry != null ? entry.getPath() : "null"));
		}

		ArchiveEntry parentDir = entry.getParent();
		String name = entry.getName();

		List<ArchiveEntry> partEntries = new ArrayList<>();
		Pattern numFirst = Pattern.compile("^(.+\\.)(0*1)$");
		Pattern part1Rar = Pattern.compile("^(.*)\\.part(0*1)\\.rar$", Pattern.CASE_INSENSITIVE);
		Matcher mNumFirst = numFirst.matcher(name);
		Matcher mPart1Rar = part1Rar.matcher(name);

		if (parentDir != null && mNumFirst.find()) {
			String prefix = mNumFirst.group(1);
			int digits = mNumFirst.group(2).length();
			int idx = 1;
			while (true) {
				ArchiveEntry cand = parentDir.resolveChild(String.format(Locale.ROOT, "%s%0" + digits + "d", prefix, idx));
				if (cand.isFile()) {
					partEntries.add(cand);
					idx++;
				}
				else {
					break;
				}
			}
		}
		else if (parentDir != null && mPart1Rar.find()) {
			String baseName = mPart1Rar.group(1);
			int idx = 1;
			while (true) {
				ArchiveEntry cand = parentDir.resolveChild(baseName + ".part" + idx + ".rar");
				if (!cand.isFile()) {
					cand = parentDir.resolveChild(String.format(Locale.ROOT, "%s.part%02d.rar", baseName, idx));
				}
				if (cand.isFile()) {
					partEntries.add(cand);
					idx++;
				}
				else {
					break;
				}
			}
		}
		else {
			partEntries.add(entry);
		}
		if (partEntries.isEmpty()) {
			partEntries.add(entry);
		}
		return inspectParts(name, partEntries);
	}
}
