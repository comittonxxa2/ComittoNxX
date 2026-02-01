package src.comitton.imageview;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.nio.ByteBuffer;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.IInStream;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Point;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.graphics.ColorMatrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;

import jcifs.CIFSContext;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbRandomAccessFile;
import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.config.SetFileListActivity;
import src.comitton.config.SetImageActivity;
import src.comitton.dialog.CustomProgressDialog;
import src.comitton.fileaccess.FileAccess;
import src.comitton.fileview.data.FileData;
import src.comitton.fileaccess.FileAccessException;
import src.comitton.fileaccess.WorkStream;
import src.comitton.jni.CallImgLibrary;
import src.comitton.jni.CallJniLibrary;
import src.comitton.fileview.data.FileListItem;
import src.comitton.fileaccess.RarInputStream;
import src.comitton.textview.TextManager;

import android.annotation.SuppressLint;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class ImageManager extends InputStream implements Runnable {
	private static final String TAG = "ImageManager";

	public static final int FILETYPE_ZIP = 100;
	public static final int FILETYPE_RAR = 200;
	public static final int FILETYPE_7Z = 300;
	public static final int FILETYPE_TAR = 400;
	public static final int FILETYPE_CAB = 500;
	public static final int FILETYPE_LZH = 600;

	public static final int FILESEEK_MAX = 50;
	public static final int FILESEEK_MAX_SOLID = 500;
	public static final int FILEERROR_RETRY = 10;
	public static final int FILEERROR_RETRY_SMB = 50;
	public static final int FILEERROR_DELAY = 10;
	public static final int FILEERROR_DELAY_SMB = 100;

	public static final int OPENMODE_VIEW = 0;
	public static final int OPENMODE_LIST = 1;
	public static final int OPENMODE_TEXTVIEW = 2;
	public static final int OPENMODE_THUMBNAIL = 3;
	public static final int OPENMODE_THUMBSORT = 4;

	public static final int OFFSET_LCL_SIGNA_LEN = 0;
	public static final int OFFSET_LCL_BFLAG_LEN = 6;
	public static final int OFFSET_LCL_FTIME_LEN = 10;
	public static final int OFFSET_LCL_FDATE_LEN = 12;
	public static final int OFFSET_LCL_CRC32_LEN = 14;
	public static final int OFFSET_LCL_CDATA_LEN = 18;
	public static final int OFFSET_LCL_OSIZE_LEN = 22;
	public static final int OFFSET_LCL_FNAME_LEN = 26;
	public static final int OFFSET_LCL_EXTRA_LEN = 28;

	public static final int OFFSET_CTL_SIGNA_LEN = 0;
	public static final int OFFSET_CTL_BFLAG_LEN = 8;
	public static final int OFFSET_CTL_FTIME_LEN = 12;
	public static final int OFFSET_CTL_FDATE_LEN = 14;
	public static final int OFFSET_CTL_CDATA_LEN = 20;
	public static final int OFFSET_CTL_OSIZE_LEN = 24;
	public static final int OFFSET_CTL_FNAME_LEN = 28;
	public static final int OFFSET_CTL_EXTRA_LEN = 30;
	public static final int OFFSET_CTL_CMENT_LEN = 32;
	public static final int OFFSET_CTL_LOCAL_LEN = 42;
	public static final int OFFSET_CTL_FNAME = 46;

	public static final int OFFSET_TRM_SIGNA_LEN = 0;
	public static final int OFFSET_TRM_CNTRL_LEN = 16;

	// RAR Format
	public static final int RAR_HTYPE_MARK = 0x72;
	public static final int RAR_HTYPE_MAIN = 0x73;
	public static final int RAR_HTYPE_FILE = 0x74;
	public static final int RAR_HTYPE_SUB = 0x7a;
	public static final int RAR_HTYPE_OLD = 0x7e;

	public static final byte RAR_METHOD_STORING = 0x30;

	public static final int OFFSET_RAR_HCRC = 0;
	public static final int OFFSET_RAR_HTYPE = 2;
	public static final int OFFSET_RAR_HFLAGS = 3;
	public static final int OFFSET_RAR_HSIZE = 5;
	public static final int OFFSET_RAR_ASIZE = 7;

	// Marker Block
	// Archive Header
	public static final int OFFSET_RAR_RESV1 = 7; // 2bytes
	public static final int OFFSET_RAR_RESV2 = 9; // 4bytes

	// FileHeader
	public static final int OFFSET_RAR_PKSIZE = 7; // 4bytes
	public static final int OFFSET_RAR_UNSIZE = 11; // 4bytes
	public static final int OFFSET_RAR_HOSTOS = 15; // 1byte
	public static final int OFFSET_RAR_FCRC = 16; // 4bytes
	public static final int OFFSET_RAR_FTIME = 20; // 2bytes
	public static final int OFFSET_RAR_FDATE = 22; // 2bytes
	public static final int OFFSET_RAR_UNPVER = 24; // 1byte
	public static final int OFFSET_RAR_METHOD = 25; // 1byte
	public static final int OFFSET_RAR_FNSIZE = 26; // 2bytes
	public static final int OFFSET_RAR_ATTRIB = 28; // 4bytes
	public static final int OFFSET_RAR_FNAME = 32; // OFFSET_RAR_FNSIZE
//	public static final int OFFSET_RAR_HPSIZE  = 32;	// 4bytes
//	public static final int OFFSET_RAR_HUSIZE  = 36;	// 4bytes
//	public static final int OFFSET_RAR_SALT    = xx;	// 8bytes
//	public static final int OFFSET_RAR_EXTTIME = xx;	// variable

	public static final int FILESORT_NONE = 0;
	public static final int FILESORT_NAME_UP = 1;
	public static final int FILESORT_NAME_DOWN = 2;

	public static final int SIZE_LOCALHEADER = 30;
	public static final int SIZE_CENTHEADER = 46;
	public static final int SIZE_TERMHEADER = 22;
	public static final int SIZE_EXTRAHEADER1 = 16;
	public static final int SIZE_EXTRAHEADER2 = 12;

	public static final int SIZE_BITFLAG = 12;

	public static final int BIS_BUFFSIZE = 100 * 1024;//Buffered Input Streamのバッファサイズ

	private static final int SIZE_BUFFER = 1024;
//	private static final int SIZE_RARHEADER = 7;
//	private static final int SIZE_RAR_HIGHSIZE = 8;

	private static final int CACHEMODE_NONE = 0;
	private static final int CACHEMODE_FILE = 1;
//	private static final int CACHEMODE_MEM = 2;

	private static final int FROMTYPE_CACHE = 2;
	private static final int FROMTYPE_LOCAL = 3;
	private static final int FROMTYPE_SERVER = 4;

	private static final int BLOCKSIZE = 128 * 1024;

	private static final int DISPMODE_DUAL = 1;
	private static final int DISPMODE_HALF = 2;
	private static final int DISPMODE_EXCHANGE = 3;

	private static final int HOKAN_DOTS = 4;

	private static final int ROTATE_NORMAL = 0;
//	private static final int ROTATE_90DEG = 1;
	private static final int ROTATE_180DEG = 2;
//	private static final int ROTATE_270DEG = 3;

	private final int THUMBNAIL_BUFFSIZE = 5;

	private final AppCompatActivity mActivity;
	private TextManager mTextManager;
	private int mPageWay;
	private int mQuality;
	private boolean mPseLand;

	private int mHostType;
	private short mFileType;
	private final int mFileSort;
	private int mOpenMode;

	private final boolean mHidden;

	private int mCacheMode;
	private int mCurrentPage;
	private int mLoadingPage;
	private boolean mCurrentSingle;
	private final Handler mHandler;
	private int mFromType;

	private long mStartTime;
	private int mMsgCount;
	private int mReadSize;
	private int mDataSize;
	private boolean mCheWriteFlag;
	private boolean mThreadLoading = true;

	private Thread mThread;
	private boolean mRunningFlag = false;
	private boolean mTerminate = false;
	private boolean mCloseFlag = false;
	private boolean mCacheBreak;
	private boolean mCacheSleep;
	private final Object mLock;

	/** URIとパスとファイル名 */
	private final String mPath;
	private final String mFileName;
	private final String mFilePath;
	private final String mUser;
	private final String mPass;
	public FileListItem[] mFileList = null;
	AnimeList[] mAnimeList = null;
	private int mMaxCmpLength;
	private int mMaxOrgLength;
	private boolean mLoadImage = false;
	private int mTimestamp = 0;

	private PdfRenderer mPdfRenderer = null;
	private boolean mFileListCacheOff = false;

	private final int mMaxThreadNum;
	private final String mRarCharset;
	private int mCacheIndex = DEF.ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
	private byte[] readData;
	private byte[] retObject;
	private byte[] buffer;

	private static int mContentsLength;
	private static String[] mContentsFile;
	private static String[] mContentsTltle;
	private static int[] mContentsPage;
	private boolean mEnableContentsFile;

	private IInArchive archive = null;
	private int numberOfItems = 0;
	private String[] mItems;
	private int mStart;
	private boolean mCacheInit = false;
	private int mCacheMax = DEF.FILECACHEMAX;
	private boolean mSolid = false;
	private int mAccessType = DEF.ACCESS_TYPE_LOCAL;
	private File mCacheDir;
	private String mCacheName;
	private String mCacheDirName;
	private SafInStream safstream;
	private boolean mAccessMode = false;

	@SuppressLint("SuspiciousIndentation")
    public ImageManager(AppCompatActivity activity, String path, String cmpfile, String user, String pass, int sort, Handler handler, boolean hidden, int openmode, int maxthread) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. path=" + path + ", cmpfile=" + cmpfile);

        mActivity = activity;
		mFileList = null;
		mAnimeList = null;
		mFilePath = DEF.relativePath(mActivity, path, cmpfile);
		mPath = path;
		mFileName = cmpfile;
		mUser = user;
		mPass = pass;
		mTerminate = false;
		mCacheBreak = false;
		mCacheSleep = false;
		mLock = this;
		mRunningFlag = true;
		mCloseFlag = false;
		mHandler = handler;
		mFileSort = sort;
		mHidden = hidden;
		mOpenMode = openmode;
		mTimestamp = (int)FileAccess.date(mActivity, mFilePath, mUser, mPass);//(int)timestamp;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
		mFileListCacheOff = SetFileListActivity.GetFileListCacheOff(sp);

 		// スレッド数
 		mMaxThreadNum = maxthread;

		//mMemSize = memsize;
		//mMemNextPages = memnext;
		//mMemPrevPages = memprev;
		//LoadImageList(memsize, memnext, memprev);

		mRarCharset = new String( "UTF-8" );
		Logcat.d(logLevel, "終了します.");
	}

	public void LoadImageList(int memsize, int memnext, int memprev, int memcache, int messagemode) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. memsize=" + memsize + ", memnext=" + memnext + ", memprev=" + memprev + ", memcache=" + memcache + ", messagemode=" + messagemode);
		mMemSize = memsize;
		mMemNextPages = memnext;
		mMemPrevPages = memprev;
		mMemCacheThreshold = memcache;
		mMessageMode = messagemode;

		try {

			mHostType = FileAccess.accessType(mFilePath);
			mFileType = FileData.getType(mActivity, mFilePath);
			Logcat.d(logLevel, "mHostType=" + mHostType + ", mFileType=" + mFileType);

			if (mFileType != FileData.FILETYPE_DIR && mFileType != FileData.FILETYPE_IMG && mFileType != FileData.FILETYPE_PDF && mFileType != FileData.FILETYPE_ARC && mFileType != FileData.FILETYPE_EPUB && mFileType != FileData.FILETYPE_WEB) {
				// ファイルタイプが不正な場合
				if (mOpenMode == OPENMODE_TEXTVIEW) {
					// テキストビュワーから呼ばれたなら、FILETYPE_IMGに偽装する
					mFileType = FileData.FILETYPE_IMG;
				}
				else {
					// テキストビュワーから呼ばれていなければエラーを返す
					Logcat.e(logLevel, "ファイルタイプが不正です. mFileType=" + mFileType + ", mFilePath=" + mFilePath);
					throw new IOException(TAG + ": LoadImageList: ファイルタイプが不正です. mFileType=" + mFileType + ", mFilePath=" + mFilePath);
				}
			}

			if (mFileType == FileData.FILETYPE_DIR) {
				DirFileList(mFilePath, mUser, mPass);
			}
			else if (mFileType == FileData.FILETYPE_IMG) {
				fileAccessInit(mFilePath);
				ImageFileList(mFilePath, mUser, mPass);
			}
			else if (mFileType == FileData.FILETYPE_PDF) {
				PdfFileList(mFilePath, mUser, mPass);
			}
			else if (mEpubOrder && mFileType == FileData.FILETYPE_EPUB) {
				fileAccessInit(mFilePath);
				epubFileList();
			}
			else if (mFileType == FileData.FILETYPE_WEB) {
				return;
			}
			else {
				fileAccessInit(mFilePath);
				cmpFileList();
			}

			if (mCloseFlag) {
				// クローズされてたら初期化しない
				return;
			}

			// メモリキャッシュの初期化(JNI)
			Logcat.d(logLevel, MessageFormat.format("メモリキャッシュを初期化します. MemoryCacheInit({0}, {1}, {2}, {3}, {4})", new Object[]{mMemSize, mMemNextPages, mMemPrevPages, mFileList.length, mMaxOrgLength}));
			if (!MemoryCacheInit(mMemSize, mMemNextPages, mMemPrevPages, mFileList.length, mMaxOrgLength)) {
				mFileList = new FileListItem[0];
				mLoadImage = true;
				return;
			}

			if (mCloseFlag) {
				// 初期化中にクローズされてたら解放する
				int returnCode = CallImgLibrary.ImageTerminate(mActivity, mHandler, mCacheIndex);
				return;
			}

			boolean mFileCacheOn = mHostType != DEF.ACCESS_TYPE_LOCAL;
			if (!mFileListCacheOff && mFileType == FileData.FILETYPE_ARC) {
				// 自前のキャッシュファイルシステムを有効にする(falseにする)
				mFileCacheOn = false;
			}
			fileCacheInit(mFileList.length, mFileCacheOn);
			startCacheRead();

		}
		catch (IOException ex) {
			mFileList = new FileListItem[0];
			Logcat.e(logLevel, "", ex);
			Message message = new Message();
			message.what = DEF.HMSG_ERROR;
			message.obj = ex.getMessage();
			mHandler.sendMessage(message);
		}
		Logcat.d(logLevel, "終了します. ");
		mLoadImage = true;
	}

	// テキストの解析
	public static void TextAnalysis(String text) {
		// テキストが見つからない場合は戻る
		if (text == null || text.trim().isEmpty()) return;
		// テキストを改行コードで分割
		String[] lines = text.split("\\R");
		// 各項目の配列を確保する
		String[] extractedFileName = new String[lines.length];
		int[] pageNumber = new int[lines.length];
		String[] infoString = new String[lines.length];
		// カウントを初期化
		mContentsLength = 0;
		int count = 0;
		// テキストの行数だけループ
		for (String line : lines) {
			String trimmedLine = line.trim();
			// テキストが見つからない場合はスキップ
			if (trimmedLine.isEmpty()) continue;
			// 半角スペース/カンマ/タブでテキストを分割
			String[] parts = trimmedLine.split("[ \t,]+");
        	// テキストのみ格納
			List<String> textElements = new ArrayList<>(); 
			infoString[count] = "";
			extractedFileName[count] = "";
			pageNumber[count] = -1;
			// テキストの解析
			for (String part : parts) {
				// ファイル名判定
				if (part.matches("(?i).*\\.(jpg|jpeg|png|gif|webp|avif|heif|heic|jxl)$")) {
					extractedFileName[count] = part;
				}
				// 数値判定(ページ番号)
				else if (part.matches("\\d+")) {
					pageNumber[count] = Integer.parseInt(part);
	            }
				// 該当しない場合はテキスト判定(タイトル名)
				else if (!part.contains(".")) {
					// ドットを含まないテキストを順に格納する
					textElements.add(part);
				}
			}
			// テキスト要素のみをスペースで連結
			infoString[count] = String.join(" ", textElements);
			// 解析結果の出力
			if (!infoString[count].isEmpty() || pageNumber[count] != -1 || !extractedFileName[count].isEmpty()) {
				// 解析結果が有ればカウントを増やす
				count++;
			}
		}
		// 解析結果を配列へ保存
		if (count > 0) {
			mContentsLength = count;
			mContentsFile = new String[count];
			mContentsTltle = new String[count];
			mContentsPage = new int[count];
			for (int i = 0; i < count; i++) {
				mContentsFile[i] = extractedFileName[i];
				mContentsTltle[i] = infoString[i];
				mContentsPage[i] = pageNumber[i];
			}
		}
	}

	public int GetContentsLegnth() {
		return mContentsLength;
	}
	public String GetContentsFile(int num) {
		return mContentsFile[num];
	}
	public String GetContentsTitle(int num) {
		return mContentsTltle[num];
	}
	public int GetContentsPage(int num) {
		return mContentsPage[num];
	}
	public int GetmFileListLength() {
		return mFileList.length;
	}
	public String GetmFileListFilename(int num) {
		return mFileList[num].name;
	}

	// 保存済みのテキストを読み出して解析を行う
	private void CheckTextAnalysis() {
		if (mEnableContentsFile) {
			// 保存済みのテキストを読み出す
			String name = mFilePath;
			// 区切りをアンダーバーへ変換
			name = name.replace("\\", "_");
			name = name.replace("/", "_");
			// ファイル名の末尾に目次を加える
			String rname = name + "_contents";
			// ファイル名をMD5のハッシュ値へ変換
			String pathcode = DEF.makeCode(rname, 0, 0);
			String rfile = DEF.getBaseDirectory() + "filelist/" + pathcode + ".cache";
			try {
				// ファイルを読み出す
				FileInputStream fis = new FileInputStream(rfile);
				int length;
				byte[] buff = new byte[1024 * 64];
				if ((length = fis.read(buff)) > 0) {
					// ファイルが読み込めた場合
					// BASE64デコードして中身を取り出す
					byte[] copiedArray = Arrays.copyOf(buff, length);
					byte[] byteArray = Base64.getDecoder().decode(copiedArray);
					String str = new String(byteArray, StandardCharsets.UTF_8);
					// テキストを解析
					TextAnalysis(str);
				}
				fis.close();
			} catch (Exception e) {
			}
		}
	}

	// ストレージのアクセスのタイプを得る
	private static int accessType(final String uri) {
		if (uri.startsWith("/")) {
			return DEF.ACCESS_TYPE_LOCAL;
		}
		else if (uri.startsWith("smb://")) {
			return DEF.ACCESS_TYPE_SMB;
		}
		else if (uri.startsWith("content://")) {
			return DEF.ACCESS_TYPE_SAF;
		}
		return DEF.ACCESS_TYPE_LOCAL;
	}
	// ストレージアクセスフレームワークのストリームアクセス(7-Zip-JBinding-4Android専用)
	public class SafInStream implements IInStream {
		private final ParcelFileDescriptor pfd;
		private final FileDescriptor fd;

		private SafInStream(ParcelFileDescriptor pfd) {
			this.pfd = pfd;
			this.fd = pfd.getFileDescriptor();
		}
		@Override
		public int read(byte[] data) throws SevenZipException {
			try {
				// Androidのシステムコールを直接呼び出して読み込み
				int bytesRead = Os.read(fd, data, 0, data.length);
				// 7-Zip-JBindingの仕様では、EOFは0ではなく-1を返す必要がある場合があるが、読み取ったバイト数をそのまま返せばよい
				return bytesRead;
			}
			catch (ErrnoException | IOException e) {
				throw new SevenZipException("読み込みに失敗しました", e);
			}
		}
		@Override
		public long seek(long offset, int origin) throws SevenZipException {
			try {
				int whence;
				switch (origin) {
					case SEEK_SET:
						// 先頭から
						whence = OsConstants.SEEK_SET;
						break;
					case SEEK_CUR:
						// 現在位置から
						whence = OsConstants.SEEK_CUR;
						break;
					case SEEK_END:
						// 末尾から
						whence = OsConstants.SEEK_END;
						break;
					default: throw new SevenZipException("無効なseek originです");
				}
				return Os.lseek(fd, offset, whence);
			}
			catch (ErrnoException e) {
				throw new SevenZipException("シークに失敗しました", e);
			}
		}
		public void close() throws IOException {
			if (pfd != null) {
				pfd.close();
			}
		}
	}
	// SMBのストリームアクセス(7-Zip-JBinding-4Android専用)
	public class SmbInStream implements IInStream {
		private final SmbRandomAccessFile sraf;
		private SmbInStream(SmbRandomAccessFile sraf) {
			this.sraf = sraf;
		}
		@Override
		public int read(byte[] data) throws SevenZipException {
			try {
				// SmbRandomAccessFileから読み込み
				return sraf.read(data);
			}
			catch (IOException e) {
				throw new SevenZipException("SMBからの読み込みに失敗しました", e);
			}
		}
		@Override
		public long seek(long offset, int origin) throws SevenZipException {
			try {
				long newPosition;
				switch (origin) {
					case SEEK_SET:
						// 先頭から
						newPosition = offset;
						break;
					case SEEK_CUR:
						// 現在位置から
						newPosition = sraf.getFilePointer() + offset;
						break;
					case SEEK_END:
						// 末尾から
						newPosition = sraf.length() + offset;
						break;
					default:
						throw new SevenZipException("無効なseek originです");
				}
				sraf.seek(newPosition);
				return newPosition;
			}
			catch (IOException e) {
				throw new SevenZipException("SMBでのシークに失敗しました", e);
			}
		}
		public void close() throws IOException {
			if (sraf != null) {
				sraf.close();
			}
		}
	}
	// 7-Zip-JBinding-4Androidのファイル名の文字化け対策
	private String getDecodedPath(IInArchive archive, int index) {
		try {
			Object pathObj = archive.getProperty(index, PropID.PATH);
			if (pathObj == null) {
				return null;
			}
			String rawPath = pathObj.toString();
			String format = archive.getArchiveFormat().toString().toUpperCase();
			// 7z, RARなどのUnicode標準形式はJNI層で正しく処理されるためそのまま返す
			if (format.matches("7Z|RAR.*|TAR")) {
				// 基本はそのまま返すが万が一化けを検知したら再デコードへ回す
				if (!containsControlCharacters(rawPath) && !rawPath.contains("\uFFFD")) {
					return rawPath;
				}
			}
			// UTF-8の日本語文字が含まれているか判定する
			if (containsJapanese(rawPath)) {
				// すでに正しくStringになっているのでそのまま返す
				return rawPath;
			}
			// JNI層での勝手な変換を解除するため一旦ISO-8859-1で生のバイト列に戻す
			byte[] rawBytes = rawPath.getBytes(StandardCharsets.ISO_8859_1);
			// UTF-8 として妥当かチェック
			if (isValidUtf8(rawBytes)) {
				String decodedUtf8 = new String(rawBytes, StandardCharsets.UTF_8);
				// UTF-8として成立しても制御文字が含まれていれば誤デコードの可能性が高い
				if (!containsControlCharacters(decodedUtf8)) {
					return decodedUtf8;
				}
			}
			// 日本語Windows標準(MS932)でデコードを試みる
			String decodedMS932 = new String(rawBytes, Charset.forName("MS932"));
			return decodedMS932;
		}
		catch (Exception e) {
			return "Unknown_Path";
		}
	}
	// 文字化け(代替文字または制御文字)が含まれているか判定
	private boolean isStillCorrupted(String text) {
		if (text == null) {
			return false;
		}
		if (text.contains("\uFFFD")) {
			return true;
		}
		return containsControlCharacters(text);
	}
	// 制御文字が含まれているか判定
	private boolean containsControlCharacters(String s) {
		if (s == null) {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			// 通常のテキストに含まれない制御文字(0x00-0x1F, 0x7F)をチェック
			if ((c < 0x20 && c != '\t' && c != '\n' && c != '\r') || c == 0x7F) {
				return true;
			}
		}
		return false;
	}
	// バイト列がUTF-8として妥当かどうかを厳格に判定する
	private boolean isValidUtf8(byte[] bytes) {
		if (bytes == null || bytes.length == 0) return false;
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
		// 不正なバイト列や変換できない文字が含まれる場合にエラーを報告させる設定
		decoder.onMalformedInput(CodingErrorAction.REPORT);
		decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		try {
			decoder.decode(ByteBuffer.wrap(bytes));
			// 最後までデコードに成功すれば妥当なUTF-8
			return true;
		}
		catch (CharacterCodingException e) {
			// UTF-8の規格に合わないバイトが含まれている
			return false;
		}
	}
	// 日本語文字が含まれているか判定する
	private boolean containsJapanese(String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			// Unicodeの日本語範囲(平仮名、片仮名、漢字)が含まれているか
			if ((c >= 0x3040 && c <= 0x309F) || (c >= 0x30A0 && c <= 0x30FF) || (c >= 0x4E00 && c <= 0x9FFF)) {
				return true;
			}
		}
		return false;
	}
	// 圧縮ファイルの判定
	private boolean IsArchive(short fileType) {
		// 圧縮ファイルの場合は真を返す
		return (fileType == FILETYPE_ZIP || fileType == FILETYPE_RAR || fileType == FILETYPE_7Z || fileType == FILETYPE_TAR || fileType == FILETYPE_CAB || fileType == FILETYPE_LZH);
	}
	// RandomAccessFileが遅いので作成した(7-Zip-JBinding-4Android専用)
	public class FastInStream implements IInStream {
		private RandomAccessFile raf;
		// バッファを利用することで、RandomAccessFileの生のreadより数十倍速くなる
		private static final int BUFFER_SIZE = 16 * 1024;
		public FastInStream(RandomAccessFile raf) {
			this.raf = raf;
		}
		@Override
		public long seek(long offset, int seekOrigin) throws SevenZipException {
			try {
				switch (seekOrigin) {
					case SEEK_SET:
						raf.seek(offset);
						break;
					case SEEK_CUR:
						raf.seek(raf.getFilePointer() + offset);
						break;
					case SEEK_END:
						raf.seek(raf.length() + offset);
						break;
				}
				return raf.getFilePointer();
			}
			catch (IOException e) {
				throw new SevenZipException(e);
			}
		}
		@Override
		public int read(byte[] data) throws SevenZipException {
			try {
				// ここで一気に読み込むようにするとOS側のキャッシュも効きやすくなる
				return raf.read(data);
			}
			catch (IOException e) {
				throw new SevenZipException(e);
			}
		}
		@Override
		public void close() throws IOException {
			raf.close();
		}
	}
	// 再帰的な削除メソッド(キャッシュ削除に使用する)
	private void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			for (File child : fileOrDirectory.listFiles()) {
				deleteRecursive(child);
			}
		}
		fileOrDirectory.delete();
	}
	public int mEpubMode = TextManager.EPUB_MODE_ALL_IMAGE;

	private void epubFileList() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		int tmpOpenMode = mOpenMode;

		// ファイル一覧を取得
		mOpenMode = OPENMODE_TEXTVIEW;
		fileAccessInit(mFilePath);
		cmpFileList();
		mOpenMode = tmpOpenMode;

		// EPUBファイルを解析
		mTextManager = new TextManager(this, "META-INF/container.xml", mUser, mPass, mHandler, mActivity, FileData.FILETYPE_EPUB);
		mTextManager.mEpubMode = mEpubMode;
		mFileList = mTextManager.getEpubImageList();
		mTextManager.release();
		mTextManager = null;
		Logcat.d(logLevel, "終了します.");
	}

	private void cmpFileList() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		int thumbSortType = 0;
		// 圧縮ファイル読み込み
		byte[] buf = new byte[SIZE_BUFFER];
		int readSize = 0;
		long cmppos = 0;
		long orgpos = 0;
		long headpos = 0;
		int nowPercent = 0;
		int oldPercent = 0;
		int maxcmplen = 0;
		int maxorglen = 0;
		int timestamp = 0;
		boolean cachecheck = false;
		List<FileListItem> list = new ArrayList<FileListItem>();
		byte [] cdhBuf = null;
		long fileLength = cmpDirectLength();
		long cdhLength = 0;		//central directory header length
		String wname;
		String wfile;

		boolean rar5 = false;
		boolean stop = false;

		sendProgress(0, 0, 0, 0);

		Logcat.d(logLevel, "mFilePath:" + mFilePath);
		String name = mFilePath;
		// 区切りをアンダーバーへ変換
		name = name.replace("\\", "_");
		name = name.replace("/", "_");
		// ファイル名をMD5のハッシュ値へ変換
		String pathcode = DEF.makeCode(name, 0, 0);
		String file = DEF.getBaseDirectory() + "filelist/" + pathcode + ".cache";
		Logcat.d(logLevel, "file:" + file);
		// ファイルリストを格納するディレクトリを作成する
		new File(DEF.getBaseDirectory() + "filelist/").mkdirs();

		int	readbytes;
		ZipInputStream zin = null;
		ZipEntry zipEntry = null;
		byte[] zbuff = new byte[14];
		byte[] lbuff = new byte[4];
		byte[] bbuff = new byte[2];
		buffer = new byte[1024];
		byte[] buff = new byte[1024 * 64];
		byte[] mContentsbuff = new byte[1024 * 64];
		int mContentsLen = 0;
		boolean mContentsFound = false;
		// あらかじめ目次の解析結果を初期化しておく(これを入れないと以前の解析結果が参照されてしまう)
		mContentsLength = 0;

		if (!mFileListCacheOff) {
			// ファイルリストのキャッシュが有効の場合
			synchronized (mLock) {
		        try {
					// ZIP形式の圧縮ファイルを展開する
					zin = new ZipInputStream(new FileInputStream(file));
					// 最初のエントリを得る
					zipEntry = zin.getNextEntry();
					File entryfile = new File(zipEntry.getName());
					Logcat.d(logLevel, "file:" + entryfile);
					// ZIP形式の圧縮ファイルをメモリー上に展開する
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					while ((readbytes = zin.read(buffer)) != -1) {
						baos.write(buffer, 0, readbytes);
					}
					// 展開したデータを格納
					readData = baos.toByteArray();
		            // 出力ストリームを閉じる
					baos.close();
					Logcat.d(logLevel, "readbytes=" + readbytes);
		            // エントリをクローズする
		            zin.closeEntry();
					// 次のエントリを得る
					zipEntry = zin.getNextEntry();
					File nextfile = new File(zipEntry.getName());
					Logcat.d(logLevel, "file:" + nextfile);
					// ZIP形式の圧縮ファイルを展開する
		            readbytes = zin.read(zbuff);
					Logcat.d(logLevel, "readbytes=" + readbytes);
		            // エントリをクローズする
		            zin.closeEntry();
		            // 入力ストリームを閉じる
					zin.close();
		        } catch (Exception e) {
					// ファイルの読み込みに失敗した場合は素通りしてファイルリストを再取得させる
					Logcat.e(logLevel, "ZipInputStream error.", e);
		        }

				try {
					// シリアライズされたファイルリストを元に戻す
				    ByteArrayInputStream bais = new ByteArrayInputStream(readData);  
				    ObjectInputStream ois = new ObjectInputStream(bais);  
				    mFileList = (FileListItem[])ois.readObject();
				    // ファイルリストへの変換に成功したと判断
				    cachecheck = true;
				} catch (Exception e) {
					// ファイルリストへの変換に失敗した場合は素通りしてファイルリストを再取得させる
					mFileList = null;
					Logcat.e(logLevel, "Deserializable error.", e);
				}

				// RARのメモリ確保時のパラメータを読み込む
				for	(int i = 0; i < 4; i++)	{
					lbuff[i] = zbuff[i];
				}
		        maxcmplen = ByteBuffer.wrap(lbuff).getInt();
				// RARのメモリ確保時のパラメータを読み込む
				for	(int i = 0; i < 4; i++)	{
					lbuff[i] = zbuff[i + 4];
				}
		        maxorglen = ByteBuffer.wrap(lbuff).getInt();
		        // タイムスタンプを読み込む
				for	(int i = 0; i < 4; i++)	{
					lbuff[i] = zbuff[i + 8];
				}
				// ファイルのタイプを読み込む
		        timestamp = ByteBuffer.wrap(lbuff).getInt();
				for	(int i = 0; i < 2; i++)	{
					bbuff[i] = zbuff[i + 12];
				}
		        mFileType = ByteBuffer.wrap(bbuff).getShort();
		        if	(mTimestamp == 0)	{
					// イメージビューアからの呼び出し以外でファイルのタイムスタンプが取得できない場合は保存していたタイムスタンプと同じにする
					mTimestamp = timestamp;
				}
		        if	(timestamp == mTimestamp && cachecheck == true)	{
					// ファイルリストの読み込みとタイムスタンプが同じだった場合はファイルリストの取得をキャンセルさせる
					Logcat.d(logLevel, "stop.");
				    stop = true;
				}
			}
		}

		// 7-Zip-JBinding-4Androidの初期化
		try {
			archive = null;
			// ストレージのアクセスのタイプを得る
			mAccessType = accessType(mFilePath);
			switch (mAccessType) {
				case DEF.ACCESS_TYPE_LOCAL:
					// ローカルパスの場合はランダムアクセス
					RandomAccessFile randomAccessFile = null;
					randomAccessFile = new RandomAccessFile(mFilePath, "r");
					FastInStream rais = new FastInStream(randomAccessFile);
					archive = SevenZip.openInArchive(null, rais);
					break;
				case DEF.ACCESS_TYPE_SMB:
					// SMBの場合
					CIFSContext mSmbContext = SingletonContext.getInstance()
						.withCredentials(new NtlmPasswordAuthenticator(null, mUser, mPass));
					
					SmbFile smbFile = new SmbFile(mFilePath, mSmbContext);
					SmbRandomAccessFile sraf = new SmbRandomAccessFile(smbFile, "r");
					// SMBのストリームアクセス
					SmbInStream smbStream = new SmbInStream(sraf);
					archive = SevenZip.openInArchive(null, smbStream);
					break;
				case DEF.ACCESS_TYPE_SAF:
					// ストレージアクセスフレームワークの場合
					// URIを得る
					Uri uri = Uri.parse(mFilePath);
					ParcelFileDescriptor pfd = mActivity.getContentResolver().openFileDescriptor(uri, "r");
					// ストレージアクセスフレームワークのストリームアクセス
					SafInStream safstream = new SafInStream(pfd);
					archive = SevenZip.openInArchive(null, safstream);
					break;
			}
		}
		catch (Exception e) {
		}
		// キャッシュファイルの処理
		String cachename = mFilePath;
		// 区切りをアンダーバーへ変換
		cachename = cachename.replace("\\", "_");
		cachename = cachename.replace("/", "_");
		mCacheDirName = DEF.makeCode(cachename, 0, 0);
		// ファイル名の末尾にキャッシュファイルを加える
		String rcachename = cachename + "_cachefile";
		// ファイル名をMD5のハッシュ値へ変換
		mCacheName = DEF.makeCode(rcachename, 0, 0);
		String numbername = cachename + "_number";
		String mNumberName = DEF.makeCode(numbername, 0, 0);
		String metaname = cachename + "_metafile";
		String mMetaName = DEF.makeCode(metaname, 0, 0);
		String solidname = cachename + "_solid";
		String mSolidName = DEF.makeCode(solidname, 0, 0);
		mCacheDir = new File(mActivity.getCacheDir(), mCacheDirName);
		File tempFile;

		if (!stop) {
			// ファイルリストの取得の場合
			// キャッシュディレクトリ内のファイルを消す
			if (mCacheDir.exists()) {
				// 内部のファイルを再帰的に削除する
				deleteRecursive(mCacheDir);
			}
			// キャッシュディレクトリを作成
			boolean result = mCacheDir.mkdirs();
		}

		if (stop) {
			// ファイルリストの取得を行わない場合
			// メタデータからそのままファイル名を取ってくる(7-Zip-JBinding-4Androidはインデックスでファイルアクセスするため)
			if (!mCacheDir.exists()) {
				// キャッシュディレクトリを作成
				boolean result = mCacheDir.mkdirs();
			}
			tempFile = new File(mCacheDir, mNumberName);
			// メタデータの総数をキャッシュから読み出す
			if (tempFile.exists() && tempFile.canRead()) {
				try (DataInputStream dis = new DataInputStream(new FileInputStream(tempFile))) {
					numberOfItems = dis.readInt();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				// 読み取れなかった場合は新しくメタデータの総数を取得して書き込む
				numberOfItems = archive.getNumberOfItems();
				try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile))) {
					dos.writeInt(numberOfItems);
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			mItems = new String[numberOfItems];
			// ファイル名をキャッシュから読み出す
			for (int i = 0; i < numberOfItems; i++) {
				tempFile = new File(mCacheDir, mMetaName + i);
				if (tempFile.exists() && tempFile.canRead()) {
					// FileReaderの代わりにInputStreamReaderを使用(API30未満対応)
					try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(tempFile), StandardCharsets.UTF_8))) {
						mItems[i] = reader.readLine();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
				else {
					// 読み取れなかった場合は新しくファイル名を取得して書き込む
					mItems[i] = getDecodedPath(archive, i);
					// 書き込み側も明示的にUTF-8を指定する
					try (BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
						writer.write(mItems[i]);
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
			tempFile = new File(mCacheDir, mSolidName);
			// ソリッド書庫の情報をキャッシュから読み出す
			if (tempFile.exists() && tempFile.canRead()) {
				try (DataInputStream dis = new DataInputStream(new FileInputStream(tempFile))) {
					mSolid = dis.readBoolean();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				// 読み取れなかった場合は新しくソリッド書庫の情報を取得して書き込む
				Object isSolid = archive.getArchiveProperty(PropID.SOLID);
				mSolid = (isSolid instanceof Boolean && (Boolean) isSolid) ? true : false;
				try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile))) {
					dos.writeBoolean(mSolid);
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}

		if	(!stop)	{
			// ファイルリストの取得の場合
			try {
				// アーカイブ形式の取得
				ArchiveFormat format = archive.getArchiveFormat();
				// 形式名の表示 (例: "7z", "zip", "rar")
				String formatName = format.getMethodName();
				if (formatName.equalsIgnoreCase("zip")) {
					Logcat.d(logLevel, "ZIPファイルです.");
					mFileType = FILETYPE_ZIP;
				}
				else if (formatName.equalsIgnoreCase("rar") || formatName.equalsIgnoreCase("rar5")) {
					Logcat.d(logLevel, "RARファイルです.");
					mFileType = FILETYPE_RAR;
				}
				else if (formatName.equalsIgnoreCase("7z")) {
					Logcat.d(logLevel, "7-Zipファイルです.");
					mFileType = FILETYPE_7Z;
				}
				else if (formatName.equalsIgnoreCase("tar")) {
					Logcat.d(logLevel, "TARファイルです.");
					mFileType = FILETYPE_TAR;
				}
				else if (formatName.equalsIgnoreCase("cab")) {
					Logcat.d(logLevel, "CABファイルです.");
					mFileType = FILETYPE_CAB;
				}
				else if (formatName.equalsIgnoreCase("lzh")) {
					Logcat.d(logLevel, "LZHファイルです.");
					mFileType = FILETYPE_LZH;
				}
				else {
					DEF.sendMessage(mActivity.getString(R.string.UnknownArchiveFormat) + "\n" + mFileName, Toast.LENGTH_LONG, mHandler);
					mFileList = new FileListItem[0];
					return;
				}
				Object isSolid = archive.getArchiveProperty(PropID.SOLID);
				mSolid = (isSolid instanceof Boolean && (Boolean) isSolid) ? true : false;
			}
			catch (Exception e) {
				Logcat.d(logLevel, "アーカイブとして認識できないか、未対応の形式です。");
				DEF.sendMessage(mActivity.getString(R.string.UnknownArchiveFormat) + "\n" + mFileName, Toast.LENGTH_LONG, mHandler);
				mFileList = new FileListItem[0];
				return;
			}

			if (IsArchive(mFileType)) {
				Logcat.d(logLevel, "解析を開始します: " + mFilePath);

				numberOfItems = archive.getNumberOfItems();
				mItems = new String[numberOfItems];

				for (int i = 0; i < numberOfItems; i++) {
					if (!mRunningFlag) {
						mFileList = new FileListItem[0];
						return;
					}
					FileListItem fl = new FileListItem();
					fl.name = getDecodedPath(archive, i);
					// メタデータからそのままファイル名を取得して格納(7-Zip-JBinding-4Androidはインデックスでファイルアクセスするため)
					mItems[i] = fl.name;
					Long size = (Long) archive.getProperty(i, PropID.SIZE);
					fl.orglen = (size != null) ? size.intValue() : 0;
					Long packedSize = (Long) archive.getProperty(i, PropID.PACKED_SIZE);
					fl.cmplen = (packedSize != null) ? packedSize.intValue() : 0;
					Date mTime = (Date) archive.getProperty(i, PropID.LAST_MODIFICATION_TIME);
					if (mTime != null) {
						fl.dtime = mTime.getTime();
					}
					String method = archive.getStringProperty(i, PropID.METHOD);
					fl.nocomp = "Store".equalsIgnoreCase(method);
					// 対象ファイル判定
					if (fl.name != null && fl.name.length() > 4 && (fl.orglen > 0 || fl.cmplen > 0)) {
						if (!mHidden || !DEF.checkHiddenFile(fl.name)) {
							fl.type = FileData.getType(mActivity, fl.name);
							fl.exttype = FileData.getExtType(mActivity, fl.name);
							boolean use = true;
							if (fl.type == FileData.FILETYPE_ARC || fl.type == FileData.FILETYPE_PARENT || fl.type == FileData.FILETYPE_DIR || fl.type == FileData.FILETYPE_PDF || fl.type == FileData.FILETYPE_WEB) {
								use = false;
							}
							else if (fl.type == FileData.FILETYPE_TXT && mOpenMode != OPENMODE_TEXTVIEW) {
								// テキストファイルを取り出す
								try {
									File cacheFile = new File(mCacheDir, mCacheName + i);
									if (!cacheFile.exists() || cacheFile.length() == 0) {
										LoadFileToCache(i);
									}
									// キャッシュファイルファイルから直接ストリームを作成
									InputStream fileStream = new FileInputStream(cacheFile);
									InputStream inputStream = new BufferedInputStream(fileStream);
									int length;
									if ((length = inputStream.read(buff)) > 0) {
										// ファイルが読み込めた場合
										// 文字列に変換
										String str = new String(buff, StandardCharsets.UTF_8);
										// テキストを解析
										TextAnalysis(str);
										if (GetContentsLegnth() > 0) {
											// 解析結果が有れば
											mContentsFound = true;
											// サイズとバッファを保存
											mContentsLen = length;
											mContentsbuff = Arrays.copyOf(buff, length);
										}
									}
									fileStream.close();
								}
								catch (Exception e) {
								}
								use = false;
							}
							else if (fl.type == FileData.FILETYPE_EPUB) {
								use = false;
							}
							else if (fl.type == FileData.FILETYPE_EPUB_SUB) {
								if (mOpenMode == OPENMODE_TEXTVIEW) {
									use = true;
								}
								else if (mOpenMode == OPENMODE_LIST) {
									// リストに表示しないものもページ数の計算に使うのでtrue
									use = true;
									//if (!fl.name.equals("META-INF/container.xml")) {
									//	use = false;
									//}
								}
								else {
									use = false;
								}
							}
							else if (fl.type == FileData.FILETYPE_NONE) {
								use = false;
							}

							if (use) {
								// リストへ登録
								list.add(fl);
								if (mFileType == FILETYPE_RAR) {
									if (maxcmplen < fl.cmplen - fl.header) {
										// 最大サイズを求める
										maxcmplen = fl.cmplen - fl.header;
									}
								}
								if (maxorglen < fl.orglen) {
									// 最大サイズを求める
									maxorglen = fl.orglen;
								}
							}
						}
					}
					// 次のファイルへ
					cmppos += fl.cmplen;
					orgpos += fl.orglen;

					oldPercent = nowPercent;
					// 割合を計算する
					nowPercent = (int)(((float)i / (float)numberOfItems + 0.005) * 100);
					Logcat.v(1, "numberOfItems=" + numberOfItems + ", i=" + i);
					// 100パーセントを超えた場合はリミッタを掛ける
					nowPercent = nowPercent > 100 ? 100 : nowPercent;
					if (oldPercent != nowPercent) {
						// 値が変化していればメッセージを送る
						sendProgress(0, nowPercent, i, numberOfItems);
					}
				}
			}
		}

		if	(!stop)	{
			sort(list);
			mFileList = list.toArray(new FileListItem[0]);

			if (!mFileListCacheOff) {
				// ファイルリストのキャッシュが有効の場合
				synchronized (mLock) {
					retObject = null;
					try {
						// ファイルリストをシリアライズする
						ByteArrayOutputStream byteos = new ByteArrayOutputStream();
						ObjectOutputStream objos = new ObjectOutputStream(byteos);
						objos.writeObject(mFileList);
						retObject = byteos.toByteArray();
						objos.close();
						byteos.close();
					} catch (Exception e) {
						// シリアライズに失敗した場合は戻る
						Logcat.e(logLevel, "Serializable error.", e);
						return;
					}

					// RARのメモリ確保時のパラメータを書き出す
					lbuff = ByteBuffer.allocate(4).putInt(maxcmplen).array();
					for (int i = 0; i < 4; i++)	{
						zbuff[i] = lbuff[i];
					}
					// RARのメモリ確保時のパラメータを書き出す
					lbuff = ByteBuffer.allocate(4).putInt(maxorglen).array();
					for (int i = 0; i < 4; i++)	{
						zbuff[i + 4] = lbuff[i];
					}
					// タイムスタンプを書き出す(イメージビューアからの呼び出しがあることを想定しているため中身が0でもそのまま書き込む)
					lbuff = ByteBuffer.allocate(4).putInt(mTimestamp).array();
					for (int i = 0; i < 4; i++)	{
						zbuff[i + 8] = lbuff[i];
					}
					// ファイルのタイプを書きだす
					bbuff = ByteBuffer.allocate(2).putShort(mFileType).array();
					for (int i = 0; i < 2; i++)	{
						zbuff[i + 12] = bbuff[i];
					}

					try {
						// あらかじめZIPファイルを削除
						new File(file).delete();
					} catch (Exception e) {
						// ファイルが存在しなかった場合は素通り
						Logcat.e(logLevel, "Delete error.", e);
					}
				    // ZIP形式の出力ストリーム
				    ZipOutputStream zos = null;
					try {
						// ZipOutputStreamオブジェクトの作成
						zos = new ZipOutputStream(new FileOutputStream(file));
					} catch (Exception e) {
						// オブジェクトの作成に失敗した場合は戻る
						Logcat.e(logLevel, "ZipOutputStream error.", e);
						return;
					}
					// ファイル名の格納用
					String filename;
					try {
						// エントリのファイル名を設定
						filename = String.format("mFileList.bin");
			            // 最初のZIPエントリを作成
			            ZipEntry ze1 = new ZipEntry(filename);
			            // 作成したZIPエントリを登録
			            zos.putNextEntry(ze1);
			            // バッファからZIP形式の出力ストリームへ書き出す
						zos.write(retObject);
			            // エントリをクローズする
						zos.closeEntry();
						// 次のエントリのファイル名を設定
						filename = String.format("setting.bin");
			            // 次のZIPエントリを作成
			            ZipEntry ze2 = new ZipEntry(filename);
			            // 作成したZIPエントリを登録
			            zos.putNextEntry(ze2);
			            // バッファからからZIP形式の出力ストリームへ書き出す
						zos.write(zbuff);
			            // エントリをクローズする
						zos.closeEntry();
						// 出力ストリームを閉じる
						zos.flush();
						zos.close();
					} catch (Exception e) {
						// ZIPファイルへの書き込みが失敗した場合は戻る
						Logcat.e(logLevel, "Write error.", e);
						return;
					}
				}
			}
			// 目次のファイルを保存
			// ファイル名の末尾に目次を加える
			wname = name + "_contents";
			// ファイル名をMD5のハッシュ値へ変換
			pathcode = DEF.makeCode(wname, 0, 0);
			wfile = DEF.getBaseDirectory() + "filelist/" + pathcode + ".cache";
			try {
				// 以前のファイルを削除
				new File(wfile).delete();
			} catch (Exception e) {
			}
			if (mContentsFound) {
				// 解析結果が有れば
				try {
					FileOutputStream fos = new FileOutputStream(wfile);
					byte[] copiedArray = Arrays.copyOf(mContentsbuff, mContentsLen);
					// ファイルの中身が直ぐに分からないようにするためにBASE64エンコードする
					byte[] enc = Base64.getEncoder().encode(copiedArray);
					// ファイルを書き出す
					fos.write(enc, 0, enc.length);
					fos.close();
				} catch (Exception e) {
				}
			}
			if (!mCacheDir.exists()) {
				// キャッシュディレクトリを作成
				boolean result = mCacheDir.mkdirs();
			}
			// メタデータを保存
			// メタデータの総数を取得して書き込む
			numberOfItems = archive.getNumberOfItems();
			tempFile = new File(mCacheDir, mNumberName);
			try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile))) {
				dos.writeInt(numberOfItems);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			// ファイル名を取得して書き込む
			mItems = new String[numberOfItems];
			for (int i = 0; i < numberOfItems; i++) {
				tempFile = new File(mCacheDir, mMetaName + i);
				mItems[i] = getDecodedPath(archive, i);
				// FileWriterの代わりにOutputStreamWriterを使用(API30未満対応)
				try (BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
					writer.write(mItems[i]);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			// ソリッド書庫の情報を取得して書き込む
			Object isSolid = archive.getArchiveProperty(PropID.SOLID);
			mSolid = (isSolid instanceof Boolean && (Boolean) isSolid) ? true : false;
			tempFile = new File(mCacheDir, mSolidName);
			try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile))) {
				dos.writeBoolean(mSolid);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		mMaxCmpLength = maxcmplen;
		mMaxOrgLength = maxorglen;

		// 保存済みのテキストを読み出して解析を行う
		CheckTextAnalysis();

		Logcat.d(logLevel, "終了します. mMaxCmpLength=" + mMaxCmpLength + ", mMaxOrgLength=" + mMaxOrgLength);
	}

	public boolean sendProgress(int type, int count, long numerator, long denominator) {
		// 割合を通知
		if (!mRunningFlag) {
			return false;
		}
		// 呼び出しの種類でメッセージの送出先を変更する
		int messagewhat = 0;
		long delay = 0;
		if (mMessageMode == DEF.MESSAGE_IMAGE) {
			// イメージビューアの場合
			messagewhat = DEF.HMSG_PROGRESS_IMAGE;
		}
		else if (mMessageMode == DEF.MESSAGE_EXPAND) {
			// ファイルの展開の場合
			messagewhat = DEF.HMSG_PROGRESS_EXPAND;
		}
		else if (mMessageMode == DEF.MESSAGE_IMAGE_START) {
			messagewhat = DEF.HMSG_PROGRESS_IMAGE_START;
			// ソリッド書庫の場合は時間がかかるのが判明しているので先にメッセージを送ってしまう
			delay = 1000;
		}
		else if (mMessageMode == DEF.MESSAGE_IMAGE_END) {
			mHandler.removeMessages(DEF.HMSG_PROGRESS_IMAGE_START);
			messagewhat = DEF.HMSG_PROGRESS_IMAGE_END;
		}
		else {
			// 該当なし
			messagewhat = DEF.HMSG_PROGRESS;
		}
		Message message = new Message();
		message.what = messagewhat;
		message.arg1 = count;
		message.arg2 = type;
		Bundle bundle = new Bundle();
		bundle.putLong("arg3", numerator);
		bundle.putLong("arg4", denominator);
		message.setData(bundle);
		long NextTime = SystemClock.uptimeMillis() + delay;
		mHandler.sendMessageAtTime(message, NextTime);
		return true;
	}

	public class AnimeList{
		boolean animeon;
		boolean animefile;
		private AnimeList(boolean enable, boolean fileon) {
			animeon = enable;
			animefile = fileon;
		}
		public boolean getAnimeOn() {
			return animeon;
		}
		public boolean getAnimeFile() {
			return animefile;
		}
	}

	private void DirFileList(String path, String user, String pass) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. path=" + path);
		int maxorglen = 0;

		// ファイルリストを作成
		List<FileListItem> list = new ArrayList<FileListItem>();

		dirListFiles(path, user, pass);

		if (!mRunningFlag) {
			return;
		}

		int nowPercent = 0;
		int oldPercent = 0;

		int count = 0;
		while (true) {
			FileListItem fl = dirGetFileListItem();
			if (fl == null) {
				break;
			}
			list.add(fl);

			if (maxorglen < fl.orglen) {
				maxorglen = fl.orglen;
			}
			// 読込通知
			oldPercent = nowPercent;
			// 割合を計算する
			nowPercent = (int)(((float)count / (float)maxorglen + 0.005) * 100);
			// 100パーセントを超えた場合はリミッタを掛ける
			nowPercent = nowPercent > 100 ? 100 : nowPercent;
			// 読込通知
			count++;
			if (oldPercent != nowPercent) {
				if (!mRunningFlag) {
					mFileList = new FileListItem[0];
					return;
				}
				Message message = new Message();
				message.what = DEF.HMSG_PROGRESS_IMAGE;
				message.arg1 = nowPercent;
				message.arg2 = 0;
				Bundle bundle = new Bundle();
				bundle.putLong("arg3", count);
				bundle.putLong("arg4", maxorglen);
				message.setData(bundle);
				mHandler.sendMessage(message);
			}
		}

		sort(list);
		mFileList = (FileListItem[]) list.toArray(new FileListItem[0]);
		mMaxOrgLength = maxorglen;

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
		boolean mAnimationScan = SetImageActivity.getAnimationScan(sp);

		List<AnimeList> animelist = new ArrayList<AnimeList>();
		for (int i = 0; i < mFileList.length; i++) {
			boolean enable = false;
			boolean fileon = false;
			if (!(mFileList[i].exttype == FileData.EXTTYPE_WEBP) && !(mFileList[i].exttype == FileData.EXTTYPE_GIF)) {
				// WebP/Gifファイル以外は何もしない
			}
			else if (!mAnimationScan) {
				// スキャンをしない
				fileon = true;
			}
			else {
				// ファイルのスキャンを行う
				fileon = true;
				try {
					String filepath = DEF.relativePath(mActivity, mFilePath, mFileList[i].name);
					File file = new File(filepath);
					// デコーダーへファイルを送る
					ImageDecoder.Source source = ImageDecoder.createSource(file);
					// デコード結果を得る
					Drawable drawable = ImageDecoder.decodeDrawable(source);
					if (drawable instanceof AnimatedImageDrawable) {
						enable = true;
					}
				}
				catch (Exception e) {
				}
			}
			animelist.add(new AnimeList(enable, fileon));
		}
		mAnimeList = (AnimeList[]) animelist.toArray(new AnimeList[0]);

		Logcat.d(logLevel, "終了します. ");
	}

	@SuppressLint("SuspiciousIndentation")
    private void ImageFileList(String path, String user, String pass) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. path=" + path);
		mFileList = new FileListItem[1];
		FileListItem filelist = new FileListItem();
		mFileList[0] = filelist;

		filelist.type = FileData.getType(mActivity, mFilePath);
		filelist.exttype = FileData.getExtType(mActivity, mFilePath);
		filelist.name = FileData.getName(mFilePath);

		FileAccess fileAccess = new FileAccess(mActivity, mFilePath, mUser, mPass, mHandler);
        try {
            fileAccess.open("r");
        } catch (FileAccessException e) {
			Logcat.e(logLevel, "ファイルオープンに失敗しました.", e);
        }
        try {
            mFileList[0].orglen = (int)fileAccess.length();
        } catch (FileAccessException e) {
			Logcat.e(logLevel, "ファイルサイズの取得に失敗しました.", e);
        }

		mMaxOrgLength = mFileList[0].orglen;
		Logcat.d(logLevel, "終了します. path=" + path + ", length=" + mMaxOrgLength);
	}

	private void PdfFileList(String path, String user, String pass) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		int maxPage = 0;
		mMaxOrgLength = 0;

		Logcat.d(logLevel, "開始します. path=" + path + ", user=" + user + ", pass=" + pass);

		if (mPdfRenderer == null) {
			Logcat.d(logLevel, "PdfRendererを取得します.");
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {
				// ParcelFileDescriptorインスタンスを作成する。
                parcelFileDescriptor = FileAccess.openParcelFileDescriptor(mActivity, path, user, pass, mActivity, mHandler);
				//ParcelFileDescriptorインスタンスを使用しPdfRendererをインスタンス化する。
				mPdfRenderer = new PdfRenderer(parcelFileDescriptor);
            } catch (Exception e) {
				Logcat.e(logLevel, "PDFの読み込みに失敗しました.", e);
            }
		}
		if (mPdfRenderer == null) {
			Logcat.e(logLevel, "mPdfRendererがnullです.");
			mFileList = new FileListItem[0];
			return;
		}

		// ファイルリストを作成
		maxPage = mPdfRenderer.getPageCount();
		mFileList = new FileListItem[maxPage];
		int nowPercent = 0;
		int oldPercent = 0;

		for (int page = 0; page < maxPage; page++) {

			FileListItem filelist = new FileListItem();
			filelist.type = FileData.FILETYPE_PDF;
			filelist.exttype = FileData.EXTTYPE_PDF;
			filelist.name = "Page" + (page+1);
			filelist.orglen = 0; // ファイルリスト読込中
			mFileList[page] = filelist;

			oldPercent = nowPercent;
			// 割合を計算する
			nowPercent = (int)(((float)page / (float)maxPage + 0.005) * 100);
			// 100パーセントを超えた場合はリミッタを掛ける
			nowPercent = nowPercent > 100 ? 100 : nowPercent;
			// 読込通知
			if (oldPercent != nowPercent) {
				if (!mRunningFlag) {
					mFileList = new FileListItem[0];
					return;
				}
				Message message = new Message();
				message.what = DEF.HMSG_PROGRESS_IMAGE;
				message.arg1 = nowPercent;
				message.arg2 = 0;
				Bundle bundle = new Bundle();
				bundle.putLong("arg3", page);
				bundle.putLong("arg4", maxPage);
				message.setData(bundle);
				mHandler.sendMessage(message);
			}
		}
		Logcat.d(logLevel, "終了します.");
	}

	// ソート実行
	public void sort(List<FileListItem> list) {
		if (mFileSort != FILESORT_NONE) {
			Collections.sort(list, new ZipComparator());
		}
	}

	// ソート用比較関数
	public class ZipComparator implements Comparator<FileListItem> {
		public int compare(FileListItem file1, FileListItem file2) {
			int result;
			result = DEF.compareFileName(file1.name, file2.name, DEF.SORT_BY_FILE_TYPE);
			if (mFileSort == FILESORT_NAME_DOWN) {
				result *= -1;
			}
			return result;
		}
	}

	// ファイルパスを返す
	public String getFilePath() {
		return mFilePath;
	}

	// ファイルタイプを返す
	public int getFileType() {
		return mFileType;
	}

	// 最大ファイルサイズ(圧縮時)を返す
	public int getMaxCmpLength() {
		return mMaxCmpLength;
	}

	// 最大ファイルサイズ(解凍時)を返す
	public int getMaxOrgLength() {
		return mMaxOrgLength;
	}

	// バックグラウンドのキャッシュ読み込みを止める
	public void setCacheSleep(boolean sleep) {
		mCacheSleep = sleep;
	}

	// 読み込み処理中断
	public void setBreakTrigger() {
		mRunningFlag = false;
	}

	// 読み込み処理中断
	public void unsetBreakTrigger() {
		mRunningFlag = true;
	}

	// ファイル数を返す
	public int length() {
		if (mFileList != null) {
			return mFileList.length;
		}
		return 0;
	}

	// ファイルを閉じる
	public void closeFiles() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		try {
			close();
		}
		catch (IOException e) {
			Logcat.e(logLevel, "", e);
		}
	}

	// ロック用オブジェクト取得
	public Object getLockObject() {
		return mLock;
	}

	// ページ選択時に表示する文字列を作成
	public String createPageStr(int page) {
		// パラメタチェック
		if (mFileList == null || (page < 0 || mFileList.length <= page)) {
			return "";
		}

		String strPath = mFilePath;
		if (mHostType == DEF.ACCESS_TYPE_SMB) {
			int idx = strPath.indexOf("@");
			if (idx >= 0) {
				strPath = "smb://" + strPath.substring(idx + 1);
			}
		}
		else if (mHostType == DEF.ACCESS_TYPE_SAF || mHostType == DEF.ACCESS_TYPE_PICKER) {
			strPath = FileAccess.filename(mActivity, strPath);
		}

		String pageStr;
		pageStr = (page + 1) + " / " + mFileList.length + "\n" + strPath + "\n" + mFileList[page].name;
		return pageStr;
	}

	public void startCacheRead() throws FileNotFoundException {
		if (mOpenMode != OPENMODE_VIEW) {
			// リスト取得モードの時は読み込み不要
			return;
		}
		// キャッシュ読込みスレッド開始
		mThread = new Thread(this);
		mThread.setPriority(Thread.MIN_PRIORITY);
		mThread.start();
	}

	// 画像の並びを逆にする
	public void reverseOrder() {
		mCacheBreak = true;
		synchronized (mLock) {
			CallImgLibrary.ImageCancel(mActivity, mHandler, mCacheIndex, 1);
		}
		// キャンセルを送出した後に確実に実行させるため100ミリ秒の時間待ちを入れてみた
		new Thread(() -> {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		synchronized (mLock) {
			if (!mCloseFlag) {
				CallImgLibrary.ImageCancel(mActivity, mHandler, mCacheIndex, 0);
				if (mFileList != null) {
					FileListItem[] newlist = new FileListItem[mFileList.length];

					int num = mFileList.length;
					CallImgLibrary.ImageScaleFree(mActivity, mHandler, mCacheIndex, -1, -1);
					for (int i = 0; i < num; i++) {
						if (mCloseFlag) {
							break;
						}
						CallImgLibrary.ImageFree(mActivity, mHandler, mCacheIndex, i);

						newlist[num - i - 1] = mFileList[i];

						// キャッシュ状態初期化
						mMemCacheFlag[i] = new MemCacheFlag();
						if (mCheCacheFlag != null) {
							mCheCacheFlag[i] = false;
						}
					}
					mFileList = newlist;
				}
			}
		}
	}

	// キャッシュをクリアする
	public void clearMemCache() {
		mCacheBreak = true;
		synchronized (mLock) {
			CallImgLibrary.ImageCancel(mActivity, mHandler, mCacheIndex, 1);
		}
		// キャンセルを送出した後に確実に実行させるため100ミリ秒の時間待ちを入れてみた
		new Thread(() -> {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		synchronized (mLock) {
			if (!mCloseFlag) {
				CallImgLibrary.ImageCancel(mActivity, mHandler, mCacheIndex, 0);
				CallImgLibrary.ImageScaleFree(mActivity, mHandler, mCacheIndex, -1, -1);

				if (mFileList != null) {
					for (int i = 0; i < mFileList.length; i++) {
						if (mCloseFlag) {
							break;
						}
						// キャッシュ状態初期化
						mMemCacheFlag[i] = new MemCacheFlag();
					}
				}
			}
		}
	}

	public void run() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		// 読込用バッファ
		final int CACHE_FPAGE = 4;
		final int CACHE_BPAGE = 2;
		final int CACHE_RANGE = 50;
		byte[] buf = new byte[BIS_BUFFSIZE];
		boolean isError = false;
		boolean fMemCacheExec = false;
		int prevReadPage = -1;
		int sleepTimer;
		boolean mInitCacheFlag = false;
		int mDiffPage = 0;

		sleepTimer = 1000;
		// ページキャッシュ開始の閾値
		double mThreshold = (10 - mMemCacheThreshold) * 0.1;

		// キャッシュ読込
		while (mRunningFlag && !isError) {

			// 処理中断フラグ
			boolean fContinue = false;

			if (sleepTimer > 0) {
				try {
					// 指定秒数スリープ
					Thread.sleep(sleepTimer);
				}
				catch (InterruptedException e) {

				}
			}
			sleepTimer = 50;

			boolean fMemCacheWrite = false;
			int page = -1;
			if (mCacheBreak || mCacheSleep) {
				mCacheBreak = false;
				if (mMemPriority != null && mMemPriority.length > 0) {
					fMemCacheExec = true;
					prevReadPage = -1;
				}

				try {
					Thread.sleep(300);
				}
				catch (InterruptedException e) {

				}
				continue;
			}

			synchronized (mLock) {
				if (mCloseFlag) {
					break;
				}
				if (fMemCacheExec) {
					int iPrio;
					for (iPrio = 1; iPrio < mMemPriority.length && page == -1; iPrio++) {
						if (mCloseFlag) {
							break;
						}
						if (mCacheBreak) {
							// キャッシュ処理中断
							break;
						}

						if (!fMemCacheExec) {
							// メモリキャッシュ不可ならそれ以上しない
							break;
						}

						// チェック対象ページ
						int chkPage = mMemPriority[iPrio] + mCurrentPage;
						int chkPage2 = mMemPriority[iPrio] + mCurrentPage + (mMemPriority[iPrio] >= 0 ? 1 : -1);

						if (0 <= chkPage2 && chkPage2 < mFileList.length && mFileList[chkPage2].width <= 0) {
							if (SizeCheckImage(chkPage2) < 0) {
								Logcat.e(logLevel, "SizeCheckImage(chkPage2) < 0, chkPage2=" + chkPage2);
								break;
							}
						}

						if (0 <= chkPage && chkPage < mFileList.length) {
							if (mFileList[chkPage].width <= 0) {
								if (SizeCheckImage(chkPage) < 0) {
									Logcat.e(logLevel, "SizeCheckImage(chkPage) < 0, chkPage=" + chkPage);
									break;
								}
							}

							// 範囲内の時だけチェック
							if (!mMemCacheFlag[chkPage].fSource) {
								// ページキャッシュ開始のしきい値
								int mDiffPageMax = (int)(mDiffPage * mThreshold);
								// ページの差分
								int mDiffPageAbs = Math.abs(chkPage - mCurrentPage);
								boolean mSkipFreeMemory = false;
								if ((mInitCacheFlag == true) && ((mDiffPageMax <= mDiffPageAbs - 1) || (mDiffPageMax <= mDiffPageAbs + 1)) && (mMemCacheThreshold != 0)) {
									// ページキャッシュ開始のしきい値に達しない場合はキャッシュの開放をスキップさせる
									mSkipFreeMemory = true;
								}
								if (prevReadPage != chkPage && memWriteLock(chkPage, 0, false, mSkipFreeMemory)) {
									// メモリキャッシュ確保OK
									// Logcat.d(logLevel, "current" + mCurrentPage + ", chkPage" + chkPage + ", prevPage=" + prevReadPage);
									page = chkPage;
									prevReadPage = chkPage;
									fMemCacheWrite = true;
									fContinue = false;
									// ページの差分を計算させるため初回時に戻す
									mInitCacheFlag = false;
								}
								else {
									// メモリがなくなったら終了
									// Logcat.d(logLevel, "chkPage" + chkPage + ", prevReadPage=" + prevReadPage);
									fMemCacheExec = false;
									if (mInitCacheFlag == false && mMemCacheThreshold != 0) {
										// 初回時のみ
										mInitCacheFlag = true;
										// ページの差分を計算(これが最大のページ格納サイズになる)
										mDiffPage = Math.abs(chkPage - mCurrentPage);
									}
								}
								// ロックしてみたら結果によらずこれ以上探さない
								break;
							}
							else if (mCurrentPage >= 0 && mCurrentPage < mFileList.length) {
								// スケーリング処理を通知
								try {
									if (isDualView()) {
										// 並べて表示
										int p;
										int page1 = -1;
										int page2 = -1;	// ターゲット

										if (chkPage < mCurrentPage) {
											// 前方向
											for (p = mCurrentPage - 1 ; p >= chkPage ; p --) {	// 1ページ前からチェック
												if (mCloseFlag) {
													break;
												}
												if (!DEF.checkPortrait(mFileList[p].width, mFileList[p].height, mScrRotate)) {
													// 横
													page1 = p;
													page2 = -1;
												}
												else {
													// 左ページは縦
													if (p == 0 || !DEF.checkPortrait(mFileList[p - 1].width, mFileList[p - 1].height, mScrRotate)) {
														// 左ページが先頭ページ 又は 右ページが横長なら左ページ単体とする
														page1 = p;
														page2 = -1;
													}
													else {
														if (mTopSingle != 0 && p == 1) {
															// 先頭単独ON かつ 右ページが先頭ページなら左ページ単体とする
															page1 = p;
															page2 = -1;
														}
														else {
															// 右ページも縦長なら並べて見開き
															page1 = p - 1;
															page2 = p;
															p --;
														}
													}
												}
											}
										}
										else {
											// 後方向
											for (p = mCurrentPage ; p <= chkPage ; p ++) {	// 1ページ前からチェック
												if (mCloseFlag) {
													break;
												}
												if (!DEF.checkPortrait(mFileList[p].width, mFileList[p].height, mScrRotate) || (p == mCurrentPage && mCurrentSingle)) {
													// 横長 又は 先頭が単ページ指定
													page1 = p;
													page2 = -1;
												}
												else {
													// 左ページは縦
													if (p >= mFileList.length - 1 || !DEF.checkPortrait(mFileList[p + 1].width, mFileList[p + 1].height, mScrRotate)) {
														// 右ページが最終ページ 又は 左ページが横なら右ページ単体とする
														page1 = p;
														page2 = -1;
													}
													else {
														// 左ページも縦長なら並べて見開き
														page1 = p;
														page2 = p + 1;
														p ++;
													}
												}
											}
										}

										if (page1 != -1 && page2 == -1) {
											// 単ページ
											if (mMemCacheFlag[page1].fSource) {
												// 通知
												//sendMessage(mHandler, DEF.HMSG_CACHE, 0, 2, null);
												if (!ImageScaling(page1, -1, ImageData.HALF_NONE, 0, null, null)) {
													// スケール失敗
													fMemCacheExec = false;
												}
											}
										}
										else if (page1 != -1 && page2 != -1) {
											if (mMemCacheFlag[page1].fSource && mMemCacheFlag[page2].fSource) {
												// 縦長なら左ページの可能性
												// 左表紙は左右反転
												if (mPageWay != DEF.PAGEWAY_RIGHT) {
													int page3 = page1;
													page1 = page2;
													page2 = page3;
												}
												// 通知
												//sendMessage(mHandler, DEF.HMSG_CACHE, 0, 2, null);
												if (!ImageScaling(page1, page2, ImageData.HALF_NONE, ImageData.HALF_NONE, null, null)) {
													// スケール失敗
													fMemCacheExec = false;
												}
											}
										}
									}
									else if (isHalfView() && !DEF.checkPortrait(mFileList[chkPage].width, mFileList[chkPage].height, mScrRotate)) {
										if (mMemCacheFlag[chkPage].fSource) {
											// 左側のみ単独表示
											if (!mMemCacheFlag[chkPage].fScale[ImageData.HALF_LEFT]) {
												// 通知
												//sendMessage(mHandler, DEF.HMSG_CACHE, 0, 2, null);
												if (!ImageScaling(chkPage, -1, ImageData.HALF_LEFT, ImageData.HALF_NONE, null, null)) {
													// スケール失敗
													fMemCacheExec = false;
												}
												// スケールしたら結果によらずこれ以上探さない
												fContinue = true;
											}
											// 右側のみ単独表示
											if (!mCacheBreak && fMemCacheExec) {
												if (!mMemCacheFlag[chkPage].fScale[ImageData.HALF_RIGHT]) {
													// 通知
													//sendMessage(mHandler, DEF.HMSG_CACHE, 0, 2, null);
													if (!ImageScaling(chkPage, -1, ImageData.HALF_RIGHT, ImageData.HALF_NONE, null, null)) {
														// スケール失敗
														fMemCacheExec = false;
													}
													// スケールしたら結果によらずこれ以上探さない
													fContinue = true;
												}
											}
										}
									}
									else {
										// 単独表示
										if (mMemCacheFlag[chkPage].fSource) {
											if (!mMemCacheFlag[chkPage].fScale[ImageData.HALF_NONE]) {
												// 通知
												//sendMessage(mHandler, DEF.HMSG_CACHE, 0, 2, null);
												if (!ImageScaling(chkPage, -1, ImageData.HALF_NONE, ImageData.HALF_NONE, null, null)) {
													// スケール失敗
													fMemCacheExec = false;
												}
												// スケールしたら結果によらずこれ以上探さない
												fContinue = true;
											}
										}
									}
								}
								finally {
									// 読み込み完了
									//sendMessage(mHandler, DEF.HMSG_CACHE, -1, 0, null);
								}
							}
						}
						else {
							continue;
						}
					}
					if (iPrio >= mMemPriority.length) {
						fMemCacheExec = false;
					}
//					Logcat.d(logLevel, "----  End  ----");
				}
				if (fContinue) {
					// スケール作成した
					continue;
				}

				if (mCacheBreak) {
					// キャッシュ処理中断
					continue;
				}

				// キャッシュ対象ページ
				if (page == -1 && mHostType != DEF.ACCESS_TYPE_LOCAL) {
					// 読込ページを探す
					int startPage = mCurrentPage;
					int range;

					// 対象ページ
					page = -1;

					for (range = 0; range < CACHE_RANGE; range++) {
						if (mCloseFlag) {
							break;
						}
						if (mCacheBreak) {
							// キャッシュ処理中断
							break;
						}

						int st; // 検索範囲
						int ed;

						// 順方向
						st = startPage + CACHE_FPAGE * range + 1;
						ed = startPage + CACHE_FPAGE * (range + 1);
						if (mFileList != null && st < mFileList.length) {
							// 最終ページ以内
							if (ed >= mFileList.length) {
								// 範囲がはみ出していれば納める
								ed = mFileList.length - 1;
							}
							for (page = st; page <= ed; page++) {
								if (!cheGetCacheFlag(page)/* && memGetCacheState(page) == MEMCACHE_NONE */) {
									// 読込むページを見つけた
									break;
								}
							}
							if (page <= ed) {
								// キャッシュのないページを発見
								break;
							}
						}

						// 逆方向の割合は減らす
						st = startPage - CACHE_BPAGE * range - 1;
						ed = startPage - CACHE_BPAGE * (range + 1);
						if (st >= 0) {
							// 最終ページ以内
							if (ed < 0) {
								// 範囲がはみ出していれば納める
								ed = 0;
							}
							for (page = st; page >= ed; page--) {
								if (!cheGetCacheFlag(page)/* && memGetCacheState(page) != MEMCACHE_OK */) {
									// 読込むページを見つけた
									break;
								}
							}
							if (page >= ed) {
								// キャッシュのないページを発見
								break;
							}
						}
					}
					if (mCacheBreak) {
						// キャッシュ処理中断
						continue;
					}

					if (range >= CACHE_RANGE || page < 0 || mFileList.length <= page) {
						sleepTimer = 1000;
						continue;
					}
				}
				if (page != -1) {
					if (!mRunningFlag) {
						// closeされた場合
						break;
					}
					if (mCacheBreak) {
						// メインスレッドでビットマップの読込処理が入った
						continue;
					}

					// キャッシュ読み込みを通知
					// Logcat.d(logLevel, "Load p=" + page);
					sendMessage(mHandler, DEF.HMSG_CACHE, 0, fMemCacheWrite ? 1 : 0, null);

					if (fMemCacheWrite) {
						if (cheGetCacheFlag(page)) {
							// ファイルキャッシュあり
							mCheWriteFlag = false;
						}
						else {
							// ファイルキャッシュなしならキャッシュする
							mCheWriteFlag = true;
							long pos;
							int len;
							mLoadingPage = page;
							if (mFileType != FileData.FILETYPE_DIR) {
								pos = mFileList[page].cmppos;
								len = mFileList[page].cmplen;
							}
							else {
								pos = mFileList[page].orgpos;
								len = mFileList[page].orglen;
							}
							try {
								// ファイル書き込み準備
								cheSeek(pos, len, page);
							}
							catch (IOException e) {
								Logcat.e(logLevel, "ファイル書き込み準備に失敗しました.", e);
								// ファイルキャッシュしない
								mCheWriteFlag = false;
							}
						}

						try {
							ImageData id = LoadImage(page, false);
							if (id == null) {
								// 読み込み失敗ならメモリキャッシュを継続しない
								fMemCacheExec = false;
							}
						}
						catch (IOException e) {
							Logcat.e(logLevel, "run: LoadImageに失敗しました.", e);
						}
//							mThreadLoading = true;
//							Logcat.d(logLevel, "---- Start ----");n

					}
					else {
						// このページの読込サイズ
						int lastsize;
						if (!fMemCacheWrite && mFileType != FileData.FILETYPE_DIR) {
							lastsize = mFileList[page].cmplen;// + SIZE_CENTHEADER + SIZE_TERMHEADER;
						}
						else {
							lastsize = mFileList[page].orglen;
						}

						// ファイルキャッシュする
						mCheWriteFlag = true;
						long pos;
						int len;
						if (mFileType != FileData.FILETYPE_DIR) {
							pos = mFileList[page].cmppos;
							len = mFileList[page].cmplen;
						}
						else {
							pos = mFileList[page].orgpos;
							len = mFileList[page].orglen;
						}

						try {
							// ファイル読み込み準備
							setLoadBitmapStart(page, false);
							cheSeek(pos, len, page);
						}
						catch (IOException e) {
							Logcat.e(logLevel, "ファイル読み込み準備に失敗しました.", e);
						}

						//
						while (mRunningFlag) {
							if (mCloseFlag) {
								break;
							}
							// Logcat.d(logLevel, "---- Start ----");
							if (!mRunningFlag) {
								// closeされた場合
								break;
							}
							if (mCacheBreak) {
								// メインスレッドでビットマップの読込処理が入った
								break;
							}
							try {
								int retsize;
								retsize = this.read(buf);
								if (retsize > 0) {
									lastsize -= retsize;
								}
								if (lastsize == 0 || retsize <= 0) {
									// ファイル終端？
									break;
								}
							}
							catch (Exception e) {
								Logcat.e(logLevel, "", e);
								// isError = true;
								break;
							}
						}
						try {
							setLoadBitmapEnd();
						}
						catch (IOException e) {
							Logcat.e(logLevel, "", e);
						}
					}

					// キャッシュ読み込み完了を通知
					sendMessage(mHandler, DEF.HMSG_CACHE, -1, 0, null);
				}
				else {
					sleepTimer = 1000;
				}
			}
		}
		mTerminate = true;
		Logcat.d(logLevel, "終了します.");
	}

	private void sendMessage(Handler handler, int what, int arg1, int arg2, Object obj) {
//		Logcat.d(logLevel, "arg=" + arg1 + ", " + arg2);
		Message message = new Message();
		message.what = what;
		message.arg1 = arg1;
		message.arg2 = arg2;
		message.obj = obj;
		handler.sendMessage(message);
	}

	// 見開きモードか？
	private boolean isDualView() {
		if (mScrDispMode == DISPMODE_DUAL) {
			return true;
		}
		else if (mScrDispMode == DISPMODE_EXCHANGE) {
			if (!DEF.checkPortrait(mScrWidth, mScrHeight)) {
				return true;
			}
		}
		return false;
	}

	// 単ページモードか？
	private boolean isHalfView() {
		if (mScrDispMode == DISPMODE_HALF) {
			return true;
		}
		else if (mScrDispMode == DISPMODE_EXCHANGE) {
			if (DEF.checkPortrait(mScrWidth, mScrHeight)) {
				return true;
			}
		}
		return false;
	}

	public int getHostType() {
		return mHostType;
	}

	// ZIP内のファイル情報を返す
	public FileListItem[] getList() {
		return mFileList;
	}

	public int search(String name) {
		if (name != null && !name.isEmpty() /* mFileType == FILETYPE_DIR */) {
			for (int i = 0; i < mFileList.length; i++) {
				if (mFileList[i].name.equals(name)) {
					return i;
				}
			}
		}
		return -1;
	}

	// 現在ページを設定
	public void setCurrentPage(int page, boolean single) {
		// キャッシュ範囲などで使用
		mCurrentPage = page;
		mCurrentSingle = single;
	}

	// ビットマップ読み込み開始
	public ImageData getImageData(int page) {
		// パラメタチェック
		if (mFileList != null && page < 0 && mFileList.length <= page) {
			return null;
		}

		ImageData id = null;
		if (mMemCacheFlag[page].fSource) {
			// メモリキャッシュあり
			id = new ImageData();
			id.Page = page;
			id.Width = mFileList[page].width;
			id.Height = mFileList[page].height;
			for (int i = 0; i < 3; i++) {
				if (mMemCacheFlag[page].fScale[i]) {
					id.HalfMode = i;
					id.SclWidth = mFileList[page].swidth[i];
					id.SclHeight = mFileList[page].sheight[i];
					id.FitWidth = mFileList[page].fwidth[i];
					id.FitHeight = mFileList[page].fheight[i];
				}
			}
		}
		return id;
	}

	// アニメーション可能かどうかをチェック
	public boolean checkAnimeEnable(int page) {
		boolean enable = false;
		if (FileData.getExtType(mActivity, mFileList[page].name) != FileData.EXTTYPE_AVIF && FileData.getExtType(mActivity, mFileList[page].name) != FileData.EXTTYPE_JXL) {
			try {
				String filepath = DEF.relativePath(mActivity, mFilePath, mFileList[page].name);
				File file = new File(filepath);
				// デコーダーへファイルを送る
				ImageDecoder.Source source = ImageDecoder.createSource(file);
				// デコード結果を得る
				Drawable drawable = ImageDecoder.decodeDrawable(source);
				if (drawable instanceof AnimatedImageDrawable) {
					// 可能だった場合
					enable = true;
				}
			}
				catch (Exception e) {
			}
		}
		return enable;
	}

	public void loadAnime(int page) {
		if (FileData.getExtType(mActivity, mFileList[page].name) != FileData.EXTTYPE_AVIF && FileData.getExtType(mActivity, mFileList[page].name) != FileData.EXTTYPE_JXL) {
			String filepath = DEF.relativePath(mActivity, mFilePath, mFileList[page].name);
			File file = new File(filepath);
			// アニメーションを表示
			ImageActivity.PutAnimation(file);
		}
	}

	// ビットマップ読み込み開始
	public ImageData loadBitmap(int page, boolean notice) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// パラメタチェック
		if (mFileList != null && page < 0 && mFileList.length <= page) {
			return null;
		}

		ImageData id = null;
		mCacheBreak = true;
		synchronized (mLock) {
//			CallImgLibrary.ImageCancel(mActivity, mHandler, mCacheIndex, 1);
//		}
//		synchronized (mLock) {
			if (!mCloseFlag) {
				mCacheBreak = false;
//				CallImgLibrary.ImageCancel(mActivity, mHandler, mCacheIndex, 0);
				mThreadLoading = false;
				if (mMemCacheFlag[page].fSource) {
					// メモリキャッシュあり
					id = new ImageData();
					id.Page = page;
					id.Width = mFileList[page].width;
					id.Height = mFileList[page].height;
				} else {
					// メモリキャッシュ無しなので読み込み
					mCheWriteFlag = false;
					if (mCacheMode != CACHEMODE_FILE && mHostType != DEF.ACCESS_TYPE_LOCAL) {
						// メモリキャッシュに保存できない場合はファイルキャッシュする
						mCheWriteFlag = true;
						long pos;
						int len;
						if (mFileType != FileData.FILETYPE_DIR) {
							pos = mFileList[page].cmppos;
							len = mFileList[page].cmplen;// + SIZE_CENTHEADER + SIZE_TERMHEADER;
						} else {
							pos = mFileList[page].orgpos;
							len = mFileList[page].orglen;
						}
						try {
							cheSeek(pos, len, page);
						} catch (IOException e) {
							Logcat.e(logLevel, "cheSeek Catch Exeption. ", e);
							mCheWriteFlag = false;
						}
					}
					if (!mCloseFlag) {
						id = LoadImage(page, notice);
					}
				}
				mThreadLoading = true;
			}
		}
		return id;
	}

	// ビットマップ読み込み開始
	public void setLoadBitmapStart(int page, boolean notice) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. page=" + page + ", notice=" + notice);

		int from;

		// 読み込み位置を設定
		long pos;
		int len;
		if (mFileType != FileData.FILETYPE_DIR) {
			Logcat.d(logLevel, "FILETYPE_DIR以外 1");
			pos = mFileList[page].cmppos;
			len = mFileList[page].cmplen;// + SIZE_CENTHEADER + SIZE_TERMHEADER;
			mLoadingPage = page;
		}
		else {
			Logcat.d(logLevel, "FILETYPE_DIR 1");
			pos = mFileList[page].orgpos;
			len = mFileList[page].orglen;
		}
		mDataSize = len;

		if (cheGetCacheFlag(page)) {
			// ファイルキャッシュに存在
			Logcat.d(logLevel, "キャッシュにヒットした");
			mCacheMode = CACHEMODE_FILE;
			from = FROMTYPE_CACHE;
			// キャッシュファイルの読込設定
			cheSeek(pos, len, page);
		}
		else {
			// キャッシュに存在しない
			Logcat.d(logLevel, "キャッシュにヒットしなかった");
			mCacheMode = CACHEMODE_NONE;
			if (mHostType != DEF.ACCESS_TYPE_LOCAL) {
				Logcat.d(logLevel, "ローカル以外");
				from = FROMTYPE_SERVER;
			}
			else {
				Logcat.d(logLevel, "ローカル");
				from = FROMTYPE_LOCAL;
			}
			// 元ファイルの読込設定
			if (mFileType == FileData.FILETYPE_DIR) {
				Logcat.d(logLevel, "FILETYPE_DIR 2");
				dirSetPage(DEF.relativePath(mActivity, mFilePath, mFileList[page].name));
				if (mHostType != DEF.ACCESS_TYPE_LOCAL)
					cmpSeek(0, len);
			}
			else {
				Logcat.d(logLevel, "FILETYPE_DIR以外 2");
				cmpSeek(pos, len);
			}
		}

//		if (mCacheMode != CACHEMODE_MEM && mFileList[page].len + BIS_BUFFSIZE < mMemCache.length) {
//			mMemCacheSave = true;
//		}
//		else {
//			mMemCacheSave = false;
////			mMemCachePage = -1;
//		}
		if (!mThreadLoading && notice) {
			Logcat.d(logLevel, "mThreadLoading=false");
			// 0%
			sendHandler(DEF.HMSG_LOADING, from, 0, null);
		}

		mStartTime = System.currentTimeMillis();
		mReadSize = 0;
		mMsgCount = 0;
	}

	// ビットマップ読み込み終了
	public void setLoadBitmapEnd() throws IOException {
//		if (page != -1 && mMemWriteFlag) {
//			// 同時キャッシュ保存モードで正常終了した場合
//			cheSetCacheFlag(page);
//		}
		if (mFileType != FileData.FILETYPE_DIR) {
			// mFile.endPage();
		}
		else {
			dirEndPage();
		}
//		if (mMemCacheSave) {
//			// キャッシュをためた
//			mMemCacheLen = mMemCachePos;
//			mMemCachePage = page;
//		}
	}

	@Override
	public int read() throws IOException {
		return 0;
	}

	@Override
	public int read(byte[] buf, int off, int len) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, MessageFormat.format("開始します. off={0}, len={1}", new Object[]{off, len}));
		int ret = 0;
		try {
			if (mCacheMode == CACHEMODE_FILE) {
				Logcat.d(logLevel, "CACHEMODE_FILE:");
				// ファイルキャッシュに存在する
				ret = cheRead(buf, off, len);
				Logcat.d(logLevel, MessageFormat.format("CACHEMODE_FILE: ret={0}", new Object[]{ret}));
			}
			else {
				// キャッシュに存在しない
				if (mFileType == FileData.FILETYPE_DIR || mFileType == FileData.FILETYPE_IMG) {
					Logcat.d(logLevel, "FILETYPE_DIR || FILETYPE_IMG:");
					ret = dirRead(buf, off, len);
					Logcat.d(logLevel, MessageFormat.format("FILETYPE_DIR || FILETYPE_IMG: ret={0}", new Object[]{ret}));
				}
				else {
					Logcat.d(logLevel, "OTHER:");
					ret = cmpRead(buf, off, len);
					Logcat.d(logLevel, MessageFormat.format("OTHER: ret={0}", new Object[]{ret}));
				}
			}
		}
		catch (Exception e) {
			if (mCloseFlag) {
				Logcat.w(logLevel, "キャンセルされました.", e);
			}
			else {
				Logcat.e(logLevel, "", e);
			}

			if (!mThreadLoading) {
				// ユーザ操作による読み込みの場合
				Message message = new Message();
				message.what = DEF.HMSG_ERROR;
				message.obj = e.getLocalizedMessage();
				mHandler.sendMessage(message);
			}
			// 終了時は壊れたデータを返してやろう
			try {
				Arrays.fill(buf, off, len - off, (byte) 0);
			}
			catch (Exception ex) {
				Logcat.e(logLevel, "エラーが発生しました.", e);
			}
			return len - off;
			//throw new IOException(e.getLocalizedMessage());
		}

		if (ret > 0) {
			mReadSize += ret;
		}
		long nowTime = System.currentTimeMillis();
		// ちょっとだけ更新頻度を増やしてみる
		if (!mThreadLoading && nowTime - mStartTime > (mMsgCount + 1) * 500) {
			mMsgCount++;
			int prog = (int) ((long) mReadSize * 100 / mDataSize);
			int rate = (int) ((long) mReadSize * 10 / (nowTime - mStartTime));
			sendHandler(DEF.HMSG_LOADING, prog << 24 | 0x0100 | mFromType, rate, null);
		}
		Logcat.d(logLevel, MessageFormat.format("終了します. ret={0}", new Object[]{ret}));
		return ret;
	}

	@Override
	public void close() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. mCloseFlag=" + mCloseFlag);

		// たまに時間がかかるので非同期処理にする
		// 意図的にExceptionを発生させるためスレッドセーフにしない
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(new Runnable() {
			@Override
			public void run() {
				mRunningFlag = false;
				if (!mCloseFlag) {
					Logcat.v(logLevel, "mCloseFlag=false");
					mCloseFlag = true;
					if (mThread != null) {
						Logcat.v(logLevel, "mThread.interrupt()");
						mThread.interrupt();
						// スレッドの終了待ち
						for (int i = 0; i < 10 && !mTerminate; i++) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException ignored) {

							}
						}
					} else {
						Logcat.v(logLevel, "mTerminate=true");
						mTerminate = true;
					}

					try {
						Logcat.v(logLevel, "cheClose()");
						cheClose();
						cmpClose();
						dirClose();
					}
					catch (IOException e) {
						// なにもしない
					}

					if (mPdfRenderer != null) {
						mPdfRenderer.close();
						mPdfRenderer = null;
					}
					Logcat.v(logLevel, "CallImgLibrary.ImageTerminate(mActivity, mHandler, mCacheIndex=" + mCacheIndex + ")");
					int returnCode = CallImgLibrary.ImageTerminate(mActivity, mHandler, mCacheIndex);
					Logcat.v(logLevel, "returnCode=" + returnCode);
				}
				else {
					Logcat.w(logLevel, "mCloseFlag=true");
				}
			}
		});

		Logcat.d(logLevel, "終了します. mCloseFlag=" + mCloseFlag);
	}

	// 4バイト数値取得
	public int getInt(byte[] b, int pos) {
		int val;
		val = ((int) b[pos] & 0x000000FF) | (((int) b[pos + 1] << 8) & 0x0000FF00) | (((int) b[pos + 2] << 16) & 0x00FF0000) | (((int) b[pos + 3] << 24) & 0xFF000000);

		return val;
	}

	// 2バイト数値取得
	public short getShort(byte[] b, int pos) {
		int val;
		val = ((int) b[pos] & 0x000000FF) | (((int) b[pos + 1] << 8) & 0x0000FF00);

		return (short) val;
	}

	// 通知
	private void sendHandler(int id, int arg1, int arg2, Object data) {
		Message message = new Message();
		message.what = id;
		message.arg1 = arg1;
		message.arg2 = arg2;
		message.obj = data;
		mHandler.sendMessage(message);
	}

	/*************************** FileCache ***************************/
	private RandomAccessFile mCheRndFile;
	private int mChePage;
	private int mCheSize;
	private int mChePos;
	private boolean[] mCheCacheFlag;
	private boolean mCheEnable;

	public void fileCacheInit(int total, boolean isEnable) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. total=" + total + ", isEnable=" + isEnable);
		mCheEnable = isEnable;

		// サーバアクセス時はファイルキャッシュも行う
		if (isEnable) {
			// キャッシュ読込モードオン
			String file = DEF.getBaseDirectory() + "comittona.cache";
			String path = DEF.getBaseDirectory() + "thumb/";
			try {
				new File(path).mkdirs();
				new File(file).delete();
				mCheRndFile = new RandomAccessFile(file, "rw");
			}
			catch (Exception e) {
				mCheEnable = false;
				Logcat.d(logLevel, "", e);
				Message message = new Message();
				message.what = DEF.HMSG_ERROR;
				message.obj = "Open Error.(" + path + ")";
				mHandler.sendMessage(message);
			}
		}

		// 参照先
		if (mCheEnable) {
			mCheCacheFlag = new boolean[total];
			// キャッシュ済みフラグ初期化
            Arrays.fill(mCheCacheFlag, false);
		}
		Logcat.d(logLevel, "終了します.");
	}

	public void cheSetCacheFlag(int index) {
		if (mCheCacheFlag != null && index >= 0 && index < mCheCacheFlag.length) {
			mCheCacheFlag[index] = true;
		}
	}

	public boolean cheGetCacheFlag(int index) {
		if (mCheCacheFlag != null && index >= 0 && index < mCheCacheFlag.length) {
			return mCheCacheFlag[index];
		}
		return false;
	}

	public int cheRead(byte[] buf, int off, int len) throws IOException {
		if (!mRunningFlag) {
			throw new IOException("ImageManaget: cheRead: User Canceled.");
		}
		if (!mCheEnable) {
			return -1;
		}
		if (mCheSize == mChePos) {
			return -1;
		}
		int ret = 0;
		int size = len;
		if (size > mCheSize - mChePos) {
			size = mCheSize - mChePos;
		}
		if (size > 0) {
			ret = mCheRndFile.read(buf, off, size);
		}
		if (mChePos == 0 && mFileType == FILETYPE_ZIP) {
			// SHIFT-JISで読込み
			if (ret >= OFFSET_LCL_FNAME_LEN + 2) {
				int lenFName = getShort(buf, OFFSET_LCL_FNAME_LEN);

				if (ret >= SIZE_LOCALHEADER + lenFName) {
					for (int i = 0; i < lenFName - 4; i++) {
						buf[off + SIZE_LOCALHEADER + i] = '0';
					}
				}
			}
		}
		mChePos += ret;
		return ret;
	}

	public void cheWrite(byte[] buf, int off, int len) throws IOException {
		if (!mCheEnable) {
			return;
		}

		if (len > mCheSize - mChePos) {
			len = mCheSize - mChePos;
		}
		if (len > 0) {
			mCheRndFile.write(buf, off, len);
			mChePos += len;
		}
		if (mCheSize == mChePos) {
			cheSetCacheFlag(mChePage);
//			Logcat.d(logLevel, "Page:" + mChePage + " Cache OK (" + mCheSize + "/" + mChePos + ")" + (mThreadLoading ? "Sub" : "Main"));
		}
	}

	public void cheSeek(long pos, int size, int page) throws IOException {
		if (!mCheEnable) {
			return;
		}
		//Logcat.d(logLevel, "開始します.");
		// エントリーサイズ
		mCheSize = size;
		mChePos = 0;
		mChePage = page;
		if (mFileType != FileData.FILETYPE_PDF) {
			mCheRndFile.seek(pos);
		}
		//Logcat.d(logLevel, "終了します.");
	}

	public void cheClose() throws IOException {
		if (mCheRndFile != null) {
			mCheRndFile.close();
			mCheRndFile = null;
		}
	}

	/*************************** CompressAccess ***************************/
	private WorkStream mWorkStream;
	private int mCmpSize;
	private int mCmpPos;

	public void fileAccessInit(String uri) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);
		// 参照先
		mWorkStream = new WorkStream(mActivity, uri, mUser, mPass, mHandler);
		Logcat.d(logLevel, "終了します. uri=" + uri);
	}

	public int cmpDirectRead(byte[] buf, int off, int len) throws IOException {
		return mWorkStream.read(buf, off, len);
	}

	public void cmpDirectSeek(long pos) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		// エントリーサイズ
		mWorkStream.seek(pos);
	}

	public long cmpDirectTell() throws IOException {
		// エントリーサイズ
		return mWorkStream.getFilePointer();
	}

	public long cmpDirectLength() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		long fileLength = 0;

		// エントリーサイズ
		if (mWorkStream != null) {
			fileLength = mWorkStream.length();
		}
		else {
			Logcat.e(logLevel, "mWorkStream が null.");
		}

		if ((fileLength & 0xFFFFFFFF00000000L) == fileLength) {
			fileLength = (fileLength >> 32) & 0x00000000FFFFFFFFL;
		}
		Logcat.d(logLevel, "終了します. filelength=" + fileLength);
		return fileLength;
	}

	public int cmpRead(byte[] buf, int off, int len) throws IOException {
		if (!mRunningFlag) {
			throw new IOException(TAG + ": cmpDirectLength: User Canceled.");
		}
		else if (mCmpSize <= mCmpPos) {
			// throw new IOException("This file format is not supported.");
			return -1;
		}

		int ret = 0;
		int size = len;
		if (size > mCmpSize - mCmpPos) {
			size = mCmpSize - mCmpPos;
		}
		if (size > 0) {
			ret = mWorkStream.read(buf, off, size);
		}
		if (ret <= 0) {
			return -1;
		}
		if (mCheWriteFlag) {
			// ファイルにキャッシュ
			cheWrite(buf, off, ret);
		}
		mCmpPos += ret;
		return ret;
	}

	public void cmpSeek(long pos, int size) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		// エントリーサイズ
		mCmpSize = size;
		mCmpPos = 0;
		if (mFileType != FileData.FILETYPE_PDF) {
			if (mWorkStream != null) {
				Logcat.d(logLevel, "mWorkStream != null");
				mWorkStream.seek(pos);
			}
			else {
				if (mCloseFlag) {
					Logcat.w(logLevel, "キャンセルされました. mWorkStream == null");
				}
				else {
					Logcat.e(logLevel, "mWorkStream == null");
				}
				throw new IOException();
			}
		}
	}

	public void cmpClose() throws IOException {
		if (mWorkStream != null) {
			mWorkStream.close();
			mWorkStream = null;
		}
		if (mFileType == FILETYPE_RAR) {
			// RARの領域解放
			CallJniLibrary.rarClose();
		}
	}

	/*************************** DirAccess ***************************/
	private BufferedInputStream mDirStream;
	private ArrayList<FileData> mFiles;

	private int mDirIndex;
	private int mDirOrgPos;

	public void dirListFiles(String uri, String user, String pass) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);
		mDirIndex = 0;
		mDirOrgPos = 0;
		try {
			mFiles = FileAccess.listFiles(mActivity, uri, user, pass, mHandler);
		}
		catch (FileAccessException e) {
			throw new IOException(TAG + ": dirListFiles: " + e.getLocalizedMessage());
		}
	}

	public FileListItem dirGetFileListItem() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		while (true) {
			FileData fileData = null;
			String name = "";
			String uri = "";
			boolean isDirectory = false;
			long size = 0;

			if (mDirIndex < 0 || mFiles == null || mFiles.size() <= mDirIndex) {
				break;
			}
			fileData = mFiles.get(mDirIndex);
			name = fileData.getName();
			isDirectory = name.endsWith("/");
			size = fileData.getSize();

			mDirIndex++;
			if (!isDirectory) {
				// 通常のファイル
				if (mHidden && DEF.checkHiddenFile(name)) {
					continue;
				}

				short type = FileData.getType(mActivity, name);
				short exttype = FileData.getExtType(mActivity, name);
				boolean use = true;
				if (type == FileData.FILETYPE_TXT && mOpenMode != OPENMODE_TEXTVIEW) {
					use = false;
				}
				else if (type == FileData.FILETYPE_EPUB_SUB && mOpenMode != OPENMODE_TEXTVIEW) {
					use = false;
				}
				else if (type == FileData.FILETYPE_ARC || type == FileData.FILETYPE_EPUB || type == FileData.FILETYPE_PDF || type == FileData.FILETYPE_NONE) {
					use = false;
				}

				if (use) {
					Logcat.d(logLevel, "mDirIndex=" + mDirIndex + ", name=" + name);
					FileListItem file = new FileListItem();
					file.name = name;
					file.type = type;
					file.exttype = exttype;
					file.cmppos = 0;
					file.orgpos = mDirOrgPos;
					file.cmplen = 0;
					file.orglen = (int) size;
					mDirOrgPos += size;
					return file;
				}
			}
		}
		return null;
	}

	public void dirSetPage(String imagefile) throws IOException {
		mWorkStream = new WorkStream(mActivity, imagefile, mUser, mPass, mHandler);
	}

	public void dirEndPage() throws IOException {
		if (mWorkStream != null) {
			mWorkStream.close();
			mWorkStream = null;
		}
	}

	public int dirRead(byte[] buf, int off, int len) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (!mRunningFlag) {
			Logcat.e(logLevel, "User Canceled.");
			throw new IOException(TAG + ": dirEndPage: User Canceled.");
		}

		int ret = mWorkStream.read(buf, off, len);

		if (mCheWriteFlag) {
			// ファイルにキャッシュ
			cheWrite(buf, off, ret);
		}
		return ret;
	}

	public void dirClose() throws IOException {
		if (mWorkStream != null) {
			mWorkStream.close();
			mWorkStream = null;
		}
	}

	/*************************** MemoryCache ***************************/
	public static final byte MEMCACHE_NONE = 0;
//	public static final byte MEMCACHE_LOCK  = 1;
	public static final byte MEMCACHE_ORG = 2;
	public static final byte MEMCACHE_SCALE = 3;
//	public static final byte MEMCACHE_CHECK = 4;

	private int mMemSize = 0;
	private int mMemPrevPages = 0;
	private int mMemNextPages = 0;
	private int mMemCacheThreshold = 0;
	private int mMessageMode = 0;
	private MemCacheFlag[] mMemCacheFlag;
	private int[] mMemPriority;

	private static class MemCacheFlag {
		public boolean fSource = false;
		public boolean[] fScale = { false, false, false };
	}

	private boolean MemoryCacheInit(int memsize, int next, int prev, int total, long maxorglen) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. memsize=" + memsize + ", prev=" + prev + ", total=" + total + ", maxorglen=" + maxorglen);

		mMemNextPages = next;
		if (mMemNextPages == 0) {
			mMemNextPages = 1;
		}
		mMemPrevPages = prev;

		Logcat.d(logLevel, MessageFormat.format("コマンドを実行します. CallImgLibrary.ImageInitialize({0}, {1}, {2}, {3})", new Object[]{maxorglen, memsize, mFileList.length, mMaxThreadNum}));
		mCacheIndex = CallImgLibrary.ImageInitialize(mActivity, mHandler, maxorglen, memsize, mFileList.length, mMaxThreadNum);
		if (mCacheIndex < 0) {
			Logcat.e(logLevel, MessageFormat.format("メモリキャッシュの初期化に失敗しました. mCacheIndex={0}", new Object[]{mCacheIndex}));
			Logcat.w(logLevel, "メモリの解放は済んでいるが Out Of Memory が防げない.");
			Logcat.d(logLevel, "メモリ利用状況.\n" + DEF.getMemoryString(mActivity));
			return false;
		}
		Logcat.d(logLevel, MessageFormat.format("メモリキャッシュの初期化に成功しました. mCacheIndex={0}", new Object[]{mCacheIndex}));

		// 配列初期化
		mMemCacheFlag = new MemCacheFlag[total];
		for (int i = 0; i < total; i++) {
			// キャッシュ状態初期化
			mMemCacheFlag[i] = new MemCacheFlag();
		}

		// 優先順位保持
		mMemPriority = new int[prev + next + 1];
		int prevIdx = 0;
		int nextIdx = 0;
		boolean fCacheNext;
		mMemPriority[0] = 0;
		for (int i = 1; i < prev + next + 1; i++) {
			if (mMemPrevPages == 0) {
				// 前頁方向にキャッシュしない
				fCacheNext = true;
			} else if (mMemNextPages == 0) {
				// 次頁方向にキャッシュしない
				fCacheNext = false;
			} else if (nextIdx < 2 && mMemNextPages >= 2) {
				// 次の2ページだけは優先的に読込
				fCacheNext = true;
			} else if (-prevIdx * 1000 / mMemPrevPages >= nextIdx * 1000 / mMemNextPages) {
				// 次頁の読込みが少ないので読込み
				fCacheNext = true;
			} else {
				// 前頁の読込みが少ないので読込み
				fCacheNext = false;
			}

			if (fCacheNext) {
				// 次ページ方向
				nextIdx++;
				mMemPriority[i] = nextIdx;
			} else {
				// 前ページ方向
				prevIdx--;
				mMemPriority[i] = prevIdx;
			}
		}

		Logcat.d(logLevel, "終了します.");
		return true;
	}

	private MemCacheFlag memGetCacheState(int page) {
		return mMemCacheFlag[page];
	}

//	public boolean memSaveCache(int page) {
//		// イメージをメモリキャッシュに保存
//		mFileList[page].width = bm.getWidth();
//		mFileList[page].height = bm.getHeight();
////		mFileList[page].bmpsize = mFileList[page].width * mFileList[page].height * 2;
//
//		int ret = CallImgLibrary.ImageSave(page, bm);
//		if (ret == 0) {
//			mMemCacheFlag[page] = MEMCACHE_OK;
//			return true;
//		}
//		return false;
//	}

	public boolean memFreeCache(int page) {
		// メモリキャッシュを解放
		int ret = CallImgLibrary.ImageFree(mActivity, mHandler, mCacheIndex, page);
		if (ret == 0) {
			mMemCacheFlag[page].fSource = false;
			mMemCacheFlag[page].fScale[0] = false;
			mMemCacheFlag[page].fScale[1] = false;
			mMemCacheFlag[page].fScale[2] = false;
			return true;
		}
		return false;
	}

	// キャッシュ書き込みするページを指定
	public boolean memWriteLock(int page, int half, boolean sclMode, boolean skipMode) {
		if (skipMode) {
			// ページキャッシュ開始しきい値が有効の場合は何もしない
			return false;
		}
		if (!sclMode && mMemCacheFlag[page].fSource) {
			// 元画像読み込みなのに未キャッシュじゃない場合
			return false;
		}
//		else if (sclMode && mMemCacheFlag[page] != MEMCACHE_ORG) {
//			// スケーリング用なのに元画像のみじゃない場合
//			return false;
//		}

//		// 自身が範囲外なら終了
//		if (page < mCurrentPage - mMemPrevPages || mCurrentPage + mMemNextPages < page) {
//			return false;
//		}

		boolean fClear = true;
		int clearIdx = mMemPriority.length - 1;

		int lineCount;
		int useCount;
		if (!sclMode) {
			// 元画像モード
			lineCount = (BLOCKSIZE / (mFileList[page].width + HOKAN_DOTS));
			useCount = (int) (Math.ceil((double) mFileList[page].height / (double) lineCount));
			CallImgLibrary.ImageFree(mActivity, mHandler, mCacheIndex, page);
		}
		else {
			// スケールモード
			lineCount = BLOCKSIZE / (mFileList[page].swidth[half] + HOKAN_DOTS);
			useCount = (int) (Math.ceil((double) mFileList[page].sheight[half] / (double) lineCount));
			CallImgLibrary.ImageScaleFree(mActivity, mHandler, mCacheIndex, page, half);
		}

		for (int loop = 0; fClear; loop++) {
			// 未使用領域で割り当て
			// ブロック数を求める
			int freeCount = CallImgLibrary.ImageGetFreeSize(mActivity, mHandler, mCacheIndex);
			if (freeCount >= useCount) {
				// 領域が足りた
			return true;
		}

			if (loop == 0) {
				// 初回は範囲外を全て消す
				for (int i = 0; i < mMemCacheFlag.length; i++) {
					if (i < mCurrentPage - mMemPrevPages || mCurrentPage + mMemNextPages < i) {
						if (mMemCacheFlag[i].fSource) {
							// メモリ使用中であれば解放
							mMemCacheFlag[i].fSource = false;
							if (memFreeCache(i)) {
								// 解放する物があった

							}
						}
					}
				}
			}
			else {
				// 自身が範囲外なら終了
				if (page < mCurrentPage - mMemPrevPages || mCurrentPage + mMemNextPages < page) {
					return false;
				}

				// 範囲外を消しましょう
				fClear = false;
				int clr = -1;
				while (clearIdx >= 0) {
					clr = mCurrentPage + mMemPriority[clearIdx];
					clearIdx--;
					if (clr == page) {
						// ロックしたい対象ページまできてしまったらループ終了
						break;
					}
					if (0 <= clr && clr < mMemCacheFlag.length) {
						if (mMemCacheFlag[clr].fSource) {
							mMemCacheFlag[clr].fSource = false;
							if (memFreeCache(clr)) {
								// 解放する物があった
								fClear = true;
								break;
							}
						}
					}
				}
			}
		}
		// 領域不足でロックできず
		return false;
	}

	public Bitmap GetBitmapFromPath(Activity activity, String filepath, Handler handler) {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		int ret = 0;
		int width = 0;
		int height = 0;
		WorkStream ws = null;
		File fileObj = null;
		int extType = 0;
		long orglen = 0;
		Bitmap bm = null;
		try {
			Logcat.d(logLevel, "イメージファイルを開きます. filepath=" + filepath);

			//String path = filepath.substring(0, filepath.lastIndexOf("/") + 1);
			//String file = filepath.substring(filepath.lastIndexOf("/") + 1);

			BitmapFactory.Options option = new BitmapFactory.Options();
			option.inJustDecodeBounds = true;

			Logcat.d(logLevel, "サイズ取得(BitmapFactory)を実行します. filepath=" + filepath);
			BitmapFactory.decodeFile(filepath, option);
			width = option.outWidth;
			height = option.outHeight;

			if (width > 0 && height > 0) {
				Logcat.d(logLevel, "サイズ取得(BitmapFactory)に成功しました.");
			} else {
				Logcat.d(logLevel, "サイズ取得(BitmapFactory)に失敗しました.");
				Logcat.d(logLevel, "WorkStreamを作成します. filepath=" + filepath);
				ws = new WorkStream(activity, filepath, "", "", handler);
				Logcat.d(logLevel, "Fileオブジェクトを作成します. uri=" + filepath);
				try {
					fileObj = new File(filepath);
				} catch (Exception e) {
					Logcat.e(logLevel, "Fileオブジェクトを作成中にエラーが発生しました.", e);
					throw new RuntimeException(e);
				}
				Logcat.d(logLevel, "ファイルサイズを取得します.");
				try {
					orglen = fileObj.length();
				} catch (Exception e) {
					Logcat.e(logLevel, "ファイルサイズの取得中にエラーが発生しました.", e);
					throw new RuntimeException(e);
				}
				if (orglen == 0) {
					Logcat.e(logLevel, "ファイルサイズの取得に失敗しました.");
				}
				Logcat.d(logLevel, "ファイルタイプを取得します.");
				extType = FileData.getExtType(activity, filepath);
				int[] imagesize = new int[2];
				Logcat.d(logLevel, "サイズ取得(Native)を実行します. type=" + extType + ", orglen=" + orglen);
				ret = SizeCheckImage(ws, -1, extType, orglen, imagesize);
				if (ret == 0 && imagesize[0] > 0 && imagesize[1] > 0) {
					Logcat.d(logLevel, "サイズ取得(Native)に成功しました.");
					width = imagesize[0];
					height = imagesize[1];
				} else {
					Logcat.e(logLevel, "サイズ取得(Native)に失敗しました.");
					return null;
				}
			}
			Logcat.d(logLevel, "イメージファイルのサイズ. width=" + width + ", height=" + height);

			// 縮小してファイル読込
			Logcat.d(logLevel, "イメージデータを取得します.");
			option.inJustDecodeBounds = false;
			option.inPreferredConfig = Config.RGB_565;
			Logcat.d(logLevel, "イメージデータ取得(BitmapFactory)を実行します. pathname=" + filepath);
			try {
				bm = BitmapFactory.decodeFile(filepath, option);
			} catch (Exception e) {
				Logcat.e(logLevel, "イメージデータ取得(BitmapFactory)でエラーが発生しました.", e);
				return null;
			}
			if (bm != null) {
				Logcat.d(logLevel, "イメージデータ取得(BitmapFactory)に成功しました.");
			} else {
				Logcat.d(logLevel, "イメージデータ取得(BitmapFactory)に失敗しました.");
				ws.seek(0);
				bm = GetBitmapNativeMain(ws, -1, extType, 1, orglen, width, height, bm);
				if (bm != null) {
					Logcat.d(logLevel, "イメージデータ取得(Native)に成功しました.");
				} else {
					Logcat.e(logLevel, "イメージデータ取得(Native)に失敗しました.");
					return null;
				}
			}
			if (bm == null) {
				Logcat.e(logLevel, "イメージファイルを取得できませんでした.");
				// NoImageであればステータス設定
				return null;
				//CallImgLibrary.ThumbnailSetNone(mID, index);
			} else {
				Logcat.d(logLevel, "イメージファイルを取得できました.");
			}
		} catch (Exception e) {
			Logcat.e(logLevel, "エラーが発生しました.", e);
			throw new RuntimeException(e);
		}
		return bm;
	}

	@SuppressLint("Range")
    private int SizeCheckImage(int page) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. page=" + page);
		int returnCode = 0;
		//Logcat.e(logLevel, "page=" + page + ", type=" + type);
		int width = -1;
		int height = -1;

		if (page < 0 || mFileList.length <= page) {
			// 範囲外
			Logcat.e(logLevel, "pageが範囲外です. page=" + page + ", length=" + mFileList.length);
			return -1;
		}

		Logcat.v(logLevel, "page=" + page + ", name=" + mFileList[page].name + ", size=" + mFileList[page].orglen);

		if (mFileList[page].width > 0) {
			return -2;
		}

		if (mFileList[page].o_width == 0) {
			BitmapFactory.Options option = new BitmapFactory.Options();
			boolean fError = false;

			mCheWriteFlag = false;
			option.inJustDecodeBounds = true;
			InputStream inputStream = null;

			try {
				Logcat.v(logLevel, "setLoadBitmapStartを開始します.");
				setLoadBitmapStart(page, false);
				if (mFileType == FileData.FILETYPE_PDF) {
					Logcat.d(logLevel, "PDFファイルです.");
					//ページ番号を指定してPdfRenderer.Pageインスタンスを取得する。
					PdfRenderer.Page pdfPage = mPdfRenderer.openPage(page);
					if (mScrWidth == 0 || mScrHeight == 0) {
						// サムネイル作成なので、元のサイズを返す
						width = pdfPage.getWidth();
						height = pdfPage.getHeight();
					} else {
						// 取得したサイズのままだと画質が悪いため、大きなサイズに変換する
						int maxsize = Math.min(3000, Math.max(mScrWidth, mScrHeight));
						if (pdfPage.getWidth() > pdfPage.getHeight()) {
							width = maxsize;
							height = maxsize * pdfPage.getHeight() / pdfPage.getWidth();
						} else {
							width = maxsize * pdfPage.getWidth() / pdfPage.getHeight();
							height = maxsize;
						}
					}
					Logcat.v(logLevel, "PDF: pdfPage.getWidth()=" + pdfPage.getWidth() + ", pdfPage.getHeight()=" + pdfPage.getHeight() + ", " + mFileList[page].name);
					Logcat.v(logLevel, "PDF: width=" + width + ", height=" + height + ", " + mFileList[page].name);
					//PdfRenderer.Pageを閉じる、この処理を忘れると次回読み込む時に例外が発生する。
					pdfPage.close();
				}
				else if (IsArchive(mFileType)) {
					Logcat.v(logLevel, "圧縮ファイルです. " + mFilePath + " " + mFileList[page].name);
					int targetIndex = -1;
					// ファイル名でインデックスを検索
					for (int i = 0; i < numberOfItems; i++) {
						if (mFileList[page].name.equals(mItems[i])) {
							targetIndex = i;
							break;
						}
					}
					if (targetIndex == -1) {
					}
					else {
						File cacheFile = new File(mCacheDir, mCacheName + targetIndex);
						if (!cacheFile.exists() || cacheFile.length() == 0) {
							LoadFileToCache(targetIndex);
						}
						// キャッシュファイルファイルから直接ストリームを作成
						InputStream fileStream = new FileInputStream(cacheFile);
	                    inputStream = new BufferedInputStream(fileStream);
					}
				}
				else {
					Logcat.d(logLevel, "イメージファイルです.");
					inputStream = new BufferedInputStream(this, BIS_BUFFSIZE);
				}
				Logcat.v(logLevel, "inputStreamの準備が完了しました.");

				// PDFファイルの場合はヌルポインタになるので除外
				if (mFileType != FileData.FILETYPE_PDF) {
					inputStream.mark(mFileList[page].orglen + 1);
				}

				if (width <= 0 || height <= 0) {
					if (FileData.getExtType(mActivity, mFileList[page].name) != FileData.EXTTYPE_AVIF && FileData.getExtType(mActivity, mFileList[page].name) != FileData.EXTTYPE_JXL) {
						Logcat.v(logLevel, "BitmapFactoryでデコードします. " + mFileList[page].name);
						BitmapFactory.decodeStream(new BufferedInputStream(inputStream), null, option);
						width = option.outWidth;
						height = option.outHeight;
					}
					if (width > 0 && height > 0) {
						Logcat.v(logLevel, "BitmapFactoryでデコードに成功しました. " + mFileList[page].name);
					}
					else {
						Logcat.v(logLevel, "BitmapFactoryでデコードに失敗しました. " + mFileList[page].name);
						try {
							inputStream.reset();
						} catch (IOException e) {
							Logcat.e(logLevel, "inputStreamのリセットでエラーになりました. " + mFileList[page].name, e);
							inputStream.close();
							return -3;
						}
						int[] imagesize = new int[2];
						returnCode = SizeCheckImage(page, inputStream, imagesize);
						if (returnCode == 0 && imagesize[0] > 0 && imagesize[1] > 0) {
							Logcat.v(logLevel, "SizeCheckImage(3) に成功しました. " + mFileList[page].name);
							width = imagesize[0];
							height = imagesize[1];
						} else {
							Logcat.e(logLevel, "SizeCheckImage(3) に失敗しました. " + mFileList[page].name);
							return -4;
						}
					}
				}
			}
			catch (IOException e) {
				if (mCloseFlag) {
					Logcat.w(logLevel, "キャンセルされました.", e);
				}
				else {
					Logcat.e(logLevel, "エラーになりました.", e);
				}
				fError = true;
			}

			Logcat.v(logLevel, "サイズを取得しました. width=" + width + ", height=" + height);
			try {
				setLoadBitmapEnd();
			}
			catch (Exception e) {
				Logcat.e(logLevel, "setLoadBitmapEndでエラーになりました.", e);
				fError = true;
			}

			if (fError) {
				return -5;
			}
			mFileList[page].o_width = width;
			mFileList[page].o_height = height;
		}
		mFileList[page].scale = DEF.calcScale(mFileList[page].o_width, mFileList[page].o_height, mFileList[page].exttype, 3200, 3200);
		mFileList[page].width = DEF.divRoundUp(mFileList[page].o_width, mFileList[page].scale);
		mFileList[page].height = DEF.divRoundUp(mFileList[page].o_height, mFileList[page].scale);
		Logcat.d(logLevel, "終了します. " + mFileList[page].name);
		return returnCode;
	}

	private int SizeCheckImage(int page, InputStream inputStream, int[] imagesize) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. page=" + page + ", " + mFileList[page].name);
		int returnCode = 0;

		if (page < -1 || mFileList.length <= page) {
			// 範囲外
			return -6;
		}
		if (mFileList[page].width > 0) {
			return -7;
		}

		if (mFileList[page].o_width == 0) {
			returnCode = SizeCheckImage(inputStream, page, mFileList[page].exttype, mFileList[page].orglen, imagesize);
			if (returnCode < 0) {
				Logcat.e(logLevel, "SizeCheckImage(5) の実行に失敗しました.");
				return -8;
			}
			Logcat.d(logLevel, "SizeCheckImage(5) の実行に成功しました.");
		}
		Logcat.d(logLevel, "終了します. " + mFileList[page].name);
		return returnCode;
	}

	public int SizeCheckImage(InputStream inputStream, int page, int type, long orglen, int[] imagesize) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		int returnCode = 0;
		Logcat.d(logLevel, MessageFormat.format("開始します. page={0}, type={1}, orglen={2}", new Object[]{page, type, orglen}));

		// 読み込み準備
		Logcat.d(logLevel, MessageFormat.format("コマンドを実行します. CallImgLibrary.ImageSetFileSize({0})", new Object[]{orglen}));
		returnCode = CallImgLibrary.ImageSetFileSize(mActivity, mHandler, mCacheIndex, orglen);
		if (returnCode < 0) {
			Logcat.e(logLevel, "ImageSetFileSize に失敗しました. return=" + returnCode);
			return returnCode;
		}
		byte[] data = new byte[100 * 1024];
		int total = 0;
		while (true) {
			int size = 0;
			try {
				if (!mRunningFlag) {
					return DEF.RETURN_CODE_TERMINATED;
				}

				Logcat.d(logLevel, MessageFormat.format("データを読み込みます. page={0}, type={1}, orglen={2}, off={3}, size={4}", new Object[]{page, type, orglen, 0, data.length}));
				size = inputStream.read(data, 0, data.length);
				Logcat.d(logLevel, MessageFormat.format("データ取得サイズ. page={0}, type={1}, orglen={2}, size={3}, total={4}", new Object[]{page, type, orglen, size, total}));
				if (size <= 0) {
					if (total != orglen) {
						Logcat.w(logLevel, MessageFormat.format("データ取得サイズが0以下です. page={0}, type={1}, orglen={2}, size={3}, total={4}", new Object[]{page, type, orglen, size, total}));
						return DEF.RETURN_CODE_ERROR_READ_DATA;
					}
					else {
						Logcat.d(logLevel, MessageFormat.format("必要なデータを読み終えました. page={0}, type={1}, orglen={2}, size={3}, total={4}", new Object[]{page, type, orglen, size, total}));
					}
					break;
				}
			} catch (IOException e) {
				if (total != orglen) {
					Logcat.e(logLevel, "データ取得でエラーになりました.", e);
				} else {
					//必要なデータを読み終えていた場合に抜ける
					Logcat.d(logLevel, "必要なデータを読み終えました. IOException: " + e.getLocalizedMessage());
				}
				return DEF.RETURN_CODE_ERROR_READ_DATA;
			}
			Logcat.d(logLevel, MessageFormat.format("コマンドを実行します. CallImgLibrary.ImageSetData(data, {0})", new Object[]{size}));
			int ret = CallImgLibrary.ImageSetData(mActivity, mHandler, mCacheIndex, data, size);
			if (ret  < 0) {
				Logcat.e(logLevel, MessageFormat.format("コマンドの実行に失敗しました. CallImgLibrary.ImageSetData(data, {0})", new Object[]{size}));
				return ret;
			}
			total += size;
		}
		if (total < orglen) {
			Logcat.e(logLevel, MessageFormat.format("InputStream から取得したサイズが足りませんでした. total={0}, orglen={1}", new Object[]{total, orglen}));
			return -12;
		} else {
			// 画像のサイズを取得する
			Logcat.d(logLevel, MessageFormat.format("コマンドを実行します. CallImgLibrary.ImageGetSize({0}, imagesize)", new Object[]{type}));
			returnCode = CallImgLibrary.ImageGetSize(mActivity, mHandler, mCacheIndex, type, imagesize);
			if (returnCode < 0 || imagesize[0] < 0 || imagesize[1] < 0) {
				Logcat.e(logLevel, MessageFormat.format("画像サイズの取得に失敗しました. returnCode={0}, imagesize[0]={1}, imagesize[1]={2}", new Object[]{returnCode, imagesize[0], imagesize[1]}));
				return returnCode;
			}
		}
		Logcat.d(logLevel, "終了します.");
		return returnCode;
	}

	public Bitmap GetBitmapNative(InputStream inputStream, int page, int scale, Bitmap bm) {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		Logcat.d(logLevel, "開始します. page=" + page + ", scale=" + scale + ", " + mFileList[page].name);

		if (page < 0 || mFileList.length <= page) {
			// 範囲外
			Logcat.e(logLevel, "Page is out of range. " + mFileList[page].name);
			return null;
		}
		if (mFileList[page].width <= 0 || mFileList[page].height <= 0) {
			Logcat.e(logLevel, "Image size is invalid. width=" + mFileList[page].width + ", height=" + mFileList[page].height + ", " + mFileList[page].name);
			return null;
		}

		bm = GetBitmapNativeMain(inputStream, page, mFileList[page].exttype, scale, mFileList[page].orglen, mFileList[page].width, mFileList[page].height, bm);
		if (bm == null) {
			Logcat.e(logLevel, "GetBitmapNativeMain() failed. " + mFileList[page].name);
		}
		Logcat.d(logLevel, "終了します. Bitmap=" + bm + ", " + mFileList[page].name);
		return bm;
	}

	public Bitmap GetBitmapNativeMain(InputStream inputStream, int page, int exttype, int scale, long orglen, int width, int height, Bitmap bm) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. page=" + page + ", exttype=" + exttype + ", scale=" + scale);

		int ret = 0;
		int returnCode = 0;

		// 読み込み準備
		returnCode = CallImgLibrary.ImageSetFileSize(mActivity, mHandler, mCacheIndex, orglen);
		if (returnCode < 0) {
			Logcat.e(logLevel, "ImageSetFileSize failed. return=" + returnCode);
			return null;
		}
		byte[] data = new byte[100 * 1024];
		int total = 0;
		while (true) {
			int size = 0;
			try {
				if (!mRunningFlag) {
					return null;
				}

				size = inputStream.read(data, 0, data.length);
				if (size <= 0) {
					if (total != orglen) {
						Logcat.w(logLevel, MessageFormat.format("データ取得サイズが0以下です. size={0}, total={1}, orglen={2}", new Object[]{size, total, orglen}));
						break;
					}
					else {
						Logcat.d(logLevel, MessageFormat.format("必要なデータを読み終えています. size={0}, total={1}, orglen={2}", new Object[]{size, total, orglen}));
						break;
					}
				}
			} catch (IOException e) {
				if (total != orglen) {
					String msg = null;
					if (e.getLocalizedMessage() != null) {
						msg = e.getLocalizedMessage();
					}
					if (msg == null) {
						msg = "";
					}
					Logcat.e(logLevel, "Catch IOException: " + msg);
					return null;
				} else {
					//必要なデータを読み終えていた場合に抜ける
					break;
				}
			}
			returnCode = CallImgLibrary.ImageSetData(mActivity, mHandler, mCacheIndex, data, size);
			if (returnCode < 0) {
				Logcat.e(logLevel, "ImageSetData failed. return=" + returnCode);
				return null;
			}
			total += size;
		}
		if (total < orglen) {
			Logcat.e(logLevel, "bufferd size is short. total=" + total + ", orglen=" + orglen);
			return null;
		} else {
			bm = Bitmap.createBitmap(width, height, Config.RGB_565);
			try {
				Logcat.d(logLevel, "CallImgLibrary.ImageGetBitmap start. exttype=" + exttype + ", scale=" + scale);
				ret = CallImgLibrary.ImageGetBitmap(mActivity, mHandler, mCacheIndex, exttype, scale, bm);
			} catch (Exception e) {
				Logcat.e(logLevel, "CallImgLibrary.ImageGetBitmap error.", e);
				return null;
			}

		//SaveFile(bm);
			if (ret < 0) {
				Logcat.e(logLevel, "CallImgLibrary.ImageConvertBitmap() failed. return=" + ret);
				return null;
			}
			if (scale != 1) {
				int Outwidth = width / scale;
				int Outheight = height / scale;
				Logcat.d(logLevel, "Bitmap.createScaledBitmap start. width=" + Outwidth + ", height=" + Outheight);
				bm = Bitmap.createScaledBitmap(bm, Outwidth, Outheight, true);
				if (bm == null) {
					Logcat.e(logLevel, "Bitmap.createScaledBitmap failed.");
					return null;
				}
			}
		}
		if (bm == null) {
			Logcat.w(logLevel, "Bitmap is null.");
		}
		Logcat.d(logLevel, "終了します. Bitmap=" + bm);
		return bm;
	}

	private ImageData LoadImage(int page, boolean notice) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. page=" + page + ", notice=" + notice);
		ImageData id = null;

		if (mMemCacheFlag[page].fSource) {
			id = new ImageData();
			id.Page = page;
			id.Width = mFileList[page].width;
			id.Height = mFileList[page].height;
			id.SclWidth = 0;// mFileList[page].swidth[half];
			id.SclHeight = 0;// mFileList[page].sheight[half];
			return id;
		}

//		Logcat.d(logLevel, "start");

		if (mFileList[page].width <= 0) {
			SizeCheckImage(page);
		}

		if (!memWriteLock(page, 0, false, false)) {
			// throw new IOException("Memory Lock Error.");
			return null;
		}

		try {
			setLoadBitmapStart(page, notice);
			if (mFileType == FileData.FILETYPE_PDF) {
				id = LoadPdfImageData(page, mPdfRenderer);
			}
			else if (IsArchive(mFileType)) {
				Logcat.v(logLevel, "圧縮ファイルです. " + mFilePath + " " + mFileList[page].name);
				int targetIndex = -1;
				// ファイル名でインデックスを検索
				for (int i = 0; i < numberOfItems; i++) {
					if (mFileList[page].name.equals(mItems[i])) {
						targetIndex = i;
						break;
					}
				}
				if (targetIndex == -1) {
				}
				else {
					File cacheFile = new File(mCacheDir, mCacheName + targetIndex);
					if (!cacheFile.exists() || cacheFile.length() == 0) {
						LoadFileToCache(targetIndex);
					}
					// キャッシュファイルファイルから直接ストリームを作成
					InputStream fileStream = new FileInputStream(cacheFile);
                    InputStream inputStream = new BufferedInputStream(fileStream);
					try {
						id = LoadImageData(page, inputStream, mFileList[page].orglen);
					}
						catch (IOException e) {
					}
				}
			}
			else {
				id = LoadImageData(page, this, mFileList[page].orglen);
			}
		}
		catch (IOException e) {
			Logcat.e(logLevel, "", e);
		}

		try {
			setLoadBitmapEnd();
		}
		catch (Exception e) {
			Logcat.e(logLevel, "", e);
		}

//		Logcat.d(logLevel, "end");
		// ファイルクローズは不要
		return id;
	}

	public byte[] loadExpandData(String filename) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. filename=" + filename + ", mFileList.length=" + mFileList.length);

		int page = -1;

		// データを探す
		for (int i = 0; i < mFileList.length; i++) {
			if (filename.equals(mFileList[i].name)) {
				page = i;
				break;
			}
		}

		if (page < 0) {
			// 見つからないならrelativePathに変換してみる (AccessTypeがSAFでテキストファイルオープンの時)
			String target = DEF.relativePath(mActivity, mPath, filename);
			String[] targetArray = target.split("/");
			if (targetArray.length > 1) {
				String name = targetArray[targetArray.length - 1];
				for (int i = 0; i < mFileList.length; i++) {
					if (name.equals(mFileList[i].name)) {
						page = i;
						break;
					}
				}
			}
		}
		if (page < 0) {
			// 見つからないなら名前に変換してみる (AccessTypeがPICKERでテキストファイルオープンの時)
			String name = FileData.getName(filename);
			for (int i = 0; i < mFileList.length; i++) {
				if (name.equals(mFileList[i].name)) {
					page = i;
					break;
				}
			}
		}
		if (page < 0) {
			// 範囲外
			Logcat.e(logLevel, "ファイルがみつかりませんでした. filename=" + filename);
			return null;
		}

		boolean fError = false;
		byte[] result = new byte[mFileList[page].orglen];
		Logcat.d(logLevel, MessageFormat.format("page={0} mFileList[page].orglen={1}", new Object[]{page, mFileList[page].orglen}));

		try {
			setLoadBitmapStart(page, false);
			if (IsArchive(mFileType)) {
				Logcat.v(logLevel, "圧縮ファイルです. " + mFilePath + " " + mFileList[page].name);
				int targetIndex = -1;
				// ファイル名でインデックスを検索
				for (int i = 0; i < numberOfItems; i++) {
					if (mFileList[page].name.equals(mItems[i])) {
						targetIndex = i;
						break;
					}
				}
				if (targetIndex == -1) {
				}
				else {
					File cacheFile = new File(mCacheDir, mCacheName + targetIndex);
					if (!cacheFile.exists() || cacheFile.length() == 0) {
						LoadFileToCache(targetIndex);
					}
					// キャッシュファイルから直接ストリームを作成
					try (InputStream fileStream = new FileInputStream(cacheFile);
						InputStream inputStream = new BufferedInputStream(fileStream);
						ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
						byte[] buffer = new byte[16384];
						int len;
						while ((len = inputStream.read(buffer)) != -1) {
							baos.write(buffer, 0, len);
						}
						result = baos.toByteArray();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			else {
				Logcat.d(logLevel, MessageFormat.format("mFileType == 圧縮ファイル以外 result.length={0}", new Object[]{result.length}));
				this.read(result, 0, result.length);
			}
		}
		catch (IOException e) {
			Logcat.e(logLevel, "", e);
			fError = true;
		}

		try {
			setLoadBitmapEnd();
		}
		catch (Exception e) {
			Logcat.e(logLevel, "", e);
			fError = true;
		}

		if (fError) {
			return null;
		}
		return result;
	}

	public void getImageSize(String filename, Point pt) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel,"filename=" + filename);
		int page = -1;
		pt.x = 0;
		pt.y = 0;

		// データを探す
		for (int i = 0; i < mFileList.length; i++) {
			if (filename.equals(mFileList[i].name)) {
				page = i;
				break;
			}
		}

		if (page < 0) {
			// 範囲外
			Logcat.e(logLevel,"File not found. filename=" + filename);
			return;
		}

		//
		if (mFileList[page].width <= 0) {
			SizeCheckImage(page);
		}

		pt.x = mFileList[page].width;
		pt.y = mFileList[page].height;
		// ファイルクローズは不要
		return;
	}

	// テキスト用
	public Bitmap loadBitmapByName(String filename) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_VERBOSE;
		Logcat.d(logLevel,"filename=" + filename);

		int page = -1;

		// データを探す
		for (int i = 0; i < mFileList.length; i++) {
			Logcat.v(logLevel,"mFileList[" + i + "].name=" + mFileList[i].name);
			if (filename.equals(mFileList[i].name)) {
				page = i;
				break;
			}
		}

		if (page < 0) {
			// 範囲外
			Logcat.e(logLevel,"File not found. filename=" + filename);
			return null;
		}

		BitmapFactory.Options option = new BitmapFactory.Options();
		Bitmap bm = null;

		option.inJustDecodeBounds = false;
		option.inPreferredConfig = Config.ARGB_8888;
		InputStream inputStream = null;

		ZipInputStream zipStream = null;
		try {
			setLoadBitmapStart(page, false);
			if (IsArchive(mFileType)) {
				Logcat.v(logLevel, "圧縮ファイルです. " + mFilePath + " " + mFileList[page].name);
				int targetIndex = -1;
				// ファイル名でインデックスを検索
				for (int i = 0; i < numberOfItems; i++) {
					if (mFileList[page].name.equals(mItems[i])) {
						targetIndex = i;
						break;
					}
				}
				if (targetIndex == -1) {
				}
				else {
					File cacheFile = new File(mCacheDir, mCacheName + targetIndex);
					if (!cacheFile.exists() || cacheFile.length() == 0) {
						LoadFileToCache(targetIndex);
					}
					// キャッシュファイルファイルから直接ストリームを作成
					InputStream fileStream = new FileInputStream(cacheFile);
					inputStream = new BufferedInputStream(fileStream);
				}
			}
			else {
				inputStream = this;
			}
			bm = BitmapFactory.decodeStream(inputStream, null, option);
		}
		catch (IOException e) {
			Logcat.e(logLevel,"", e);
		}

		try {
			setLoadBitmapEnd();
		}
		catch (Exception e) {
			Logcat.e(logLevel, "", e);
		}

		// ファイルクローズは不要
		return bm;
	}

	// サムネイルに近い縮尺を求める
	public Bitmap LoadEpubThumbnail(int width, int height) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. width=" + width + ", height=" + height);
		mEpubOrder = true;
		mEpubMode = TextManager.EPUB_MODE_COVER;

		LoadImageList(0, 0, 0, 0, 0);

		if (mFileList != null || mFileList.length != 0) {
			Logcat.d(logLevel, "mFileList[0].name=" + mFileList[0].name);
		}

		return LoadThumbnail(0, width, height);
	}

	// サムネイルに近い縮尺を求める
	public Bitmap LoadThumbnail(int page, int width, int height) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. page=" + page + ", width=" + width + ", height=" + height);

		if (mFileList == null || mFileList.length == 0) {
			LoadImageList(0, 0, 0, 0, 0);
		}

		if (mFileList == null || mFileList.length == 0) {
			return null;
		}

		if (mFileList[page].width <= 0) {
			SizeCheckImage(page);
		}
		Logcat.d(logLevel, "開始します. page=" + page + ", mFileList[page].width=" + mFileList[page].width + ", mFileList[page].height=" + mFileList[page].height);

		int sampleSize = 1;
		if (width != 0 && height != 0) {
			sampleSize = DEF.calcThumbnailScale(mFileList[page].width, mFileList[page].height, width, height);
		}
		else {
			sampleSize = 1;
		}
		Logcat.d(logLevel, "LoadThumbnailMain を開始します. page=" + page + ", sampleSize=" + sampleSize);
		Bitmap bm = LoadThumbnailMain(page, sampleSize);
		Logcat.d(logLevel, "LoadThumbnailMain を終了します. page=" + page + ", bm.getWidth()=" + bm.getWidth() + ", bm.getHeight()=" + bm.getHeight());

		Logcat.d(logLevel, "終了します.");
		return bm;
	}

	// サムネイル用に画像読込み
	public Bitmap LoadThumbnailMain(int page, int sampleSize) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		BitmapFactory.Options option = new BitmapFactory.Options();
		Bitmap bm = null;

		option.inJustDecodeBounds = false;
		option.inPreferredConfig = Config.RGB_565;
		option.inSampleSize = sampleSize;
		InputStream inputStream = null;

		Logcat.d(logLevel, "開始します. page=" + page + ", sampleSize=" + sampleSize + ", filename=" + mFileList[page].name);

		try {
			setLoadBitmapStart(page, false);
			if (mFileType == FileData.FILETYPE_PDF) {
				Logcat.v(logLevel, "PDFファイルを開きます. filename=" + mFileList[page].name);
				//ページ番号を指定してPdfRenderer.Pageインスタンスを取得する。
				PdfRenderer.Page pdfPage = mPdfRenderer.openPage(page);
				// サムネイル画像が崩れる可能性があるのでそのまま返す
				int Outwidth = pdfPage.getWidth();// / sampleSize;
				int Outheight = pdfPage.getHeight();/// sampleSize;

				//PdfRenderer.Pageの情報を使って空の描画用Bitmapインスタンスを作成する。
				bm = Bitmap.createBitmap(pdfPage.getWidth() , pdfPage.getHeight() , Config.ARGB_8888);
				// PDFをレンダリングする前にBitmapを白く塗る。
				Canvas canvas = new Canvas(bm);
				canvas.drawColor(Color.WHITE);
				canvas.drawBitmap(bm, 0, 0, null);
				//空のBitmapにPDFの内容を描画する。
				pdfPage.render(bm , null,null , PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
				//PdfRenderer.Pageを閉じる、この処理を忘れると次回読み込む時に例外が発生する。
				pdfPage.close();
				if (bm == null) {
					Logcat.e(logLevel, "PDFファイルのレンダリングに失敗しました. filename=" + mFileList[page].name);
					return null;
				}
				else {
					Logcat.v(logLevel, "PDFファイルのレンダリングに成功しました.");
					Logcat.v(logLevel, "ビットマップのサイズを変更します. width=" + Outwidth + ", height=" + Outheight);
					bm = Bitmap.createScaledBitmap(bm, Outwidth, Outheight, true);
					if (bm == null) {
						Logcat.e(logLevel, "ビットマップのサイズの変更に失敗しました. filename=" + mFileList[page].name);
						return null;
					}
					else {
						Logcat.v(logLevel, "ビットマップのサイズの変更に成功しました.");
						return bm;
					}
				}
			}
			else if (IsArchive(mFileType)) {
					Logcat.v(logLevel, "圧縮ファイルです. " + mFilePath + " " + mFileList[page].name);
					int targetIndex = -1;
					// ファイル名でインデックスを検索
					for (int i = 0; i < numberOfItems; i++) {
						if (mFileList[page].name.equals(mItems[i])) {
							targetIndex = i;
							break;
						}
					}
					if (targetIndex == -1) {
					}
					else {
						File cacheFile = new File(mCacheDir, mCacheName + targetIndex);
						if (!cacheFile.exists() || cacheFile.length() == 0) {
							LoadFileToCache(targetIndex);
						}
						// キャッシュファイルファイルから直接ストリームを作成
						InputStream fileStream = new FileInputStream(cacheFile);
						inputStream = new BufferedInputStream(fileStream);
					}
			}
			else {
				inputStream = new BufferedInputStream(this);
			}

			inputStream.mark(mFileList[page].orglen + 1);

			if (FileData.getExtType(mActivity, mFileList[page].name) != FileData.EXTTYPE_AVIF && FileData.getExtType(mActivity, mFileList[page].name) != FileData.EXTTYPE_JXL) {
				bm = BitmapFactory.decodeStream(new BufferedInputStream(inputStream), null, option);
			}
			if (bm != null) {
				// なにもしない
				Logcat.v(logLevel, "BitmapFactory decode succeed. filename=" + mFileList[page].name);
			}
			else {
				Logcat.v(logLevel, "BitmapFactory decode failed. filename=" + mFileList[page].name);
				try {
					inputStream.reset();
				} catch (IOException e) {
					Logcat.e(logLevel, "inputStream.reset() failed. filename=" + mFileList[page].name);
					inputStream.close();
					return null;
				}
				bm = GetBitmapNative(inputStream, page, sampleSize, bm);
				if (bm == null) {
					Logcat.e(logLevel, "GetBitmapNative failed. filename=" + mFileList[page].name);
					return null;
				}
				Logcat.v(logLevel, "GetBitmapNative succeed. filename=" + mFileList[page].name);
			}
		}
		catch (IOException e) {
			if (mCloseFlag) {
				Logcat.w(logLevel, "キャンセルされました.", e);
			}
			else {
				Logcat.e(logLevel, "", e);
			}
		}

		try {
			setLoadBitmapEnd();
		}
		catch (Exception e) {
			Logcat.e(logLevel, "", e);
		}

		if (bm != null && bm.getConfig() != Config.RGB_565) {
			bm = bm.copy(Config.RGB_565, true);
		}

		if (bm == null) {
			Logcat.w(logLevel, "Bitmap が NULL です. filename=" + mFileList[page].name);
		}

		Logcat.d(logLevel, "終了します. filename=" + mFileList[page].name);
		// ファイルクローズは不要
		return bm;
	}

	private ImageData LoadPdfImageData(int page, PdfRenderer mPdfRenderer) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		int ret = 0;
		ImageData id = null;
		//ページ番号を指定してPdfRenderer.Pageインスタンスを取得する。
		PdfRenderer.Page pdfPage = mPdfRenderer.openPage(page);
		//PdfRenderer.Pageの情報を使って空の描画用Bitmapインスタンスを作成する。
		Logcat.d(logLevel, "BitmapSize pdfPage.getWidth()=" + pdfPage.getWidth() + ", pdfPage.getHeight()=" + pdfPage.getHeight() + ", " + mFileList[page].name);
		Logcat.d(logLevel, "BitmapSize  mFileList[page].width=" + mFileList[page].o_width + ", mFileList[page].height =" + mFileList[page].o_height + ", " + mFileList[page].name);
		Bitmap bm = Bitmap.createBitmap(mFileList[page].o_width , mFileList[page].o_height, Config.ARGB_8888);
		// PDFをレンダリングする前にBitmapを白く塗る。
		Canvas canvas = new Canvas(bm);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(bm, 0, 0, null);
		//空のBitmapにPDFの内容を描画する。
		pdfPage.render(bm , null,null , PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
		//PdfRenderer.Pageを閉じる、この処理を忘れると次回読み込む時に例外が発生する。
		pdfPage.close();
		if (bm != null) {
			Logcat.d(logLevel, "BitmapFactory decode succeed. " + mFileList[page].name);
			ret = CallImgLibrary.ImageSetPage(mActivity, mHandler, mCacheIndex, page, 0);
			if (ret < 0) {
				Logcat.e(logLevel, "CallImgLibrary.ImageSetPage failed. return=" + ret + ", " + mFileList[page].name);
				return null;
			}
			if (mFileList[page].scale != 1) {
				int Outwidth = mFileList[page].width / mFileList[page].scale;
				int Outheight = mFileList[page].height / mFileList[page].scale;
				Logcat.d(logLevel, "Bitmap.createScaledBitmap start. width=" + Outwidth + ", height=" + Outheight);
				bm = Bitmap.createScaledBitmap(bm, Outwidth, Outheight, true);
				if (bm == null) {
					Logcat.e(logLevel, "Bitmap.createScaledBitmap failed.");
					return null;
				}
			}

			Logcat.d(logLevel, "CallImgLibrary.ImageSetPage succeed. " + mFileList[page].name);
			ret = CallImgLibrary.ImageSetBitmap(mActivity, mHandler, mCacheIndex, bm);
			if (ret < 0) {
				Logcat.e(logLevel, "ImageSetBitmap failed. return=" +ret + ", " + mFileList[page].name);
				return null;
			}
			Logcat.d(logLevel, "CallImgLibrary.ImageSetBitmap succeed. " + mFileList[page].name);
			bm.recycle();
			// 読み込み成功
			mMemCacheFlag[page].fSource = true;
			id = new ImageData();
			id.Page = page;
			id.Width = mFileList[page].width;
			id.Height = mFileList[page].height;
		}

		return id;
	}

	// ビュワー表示用の画像読込
	private ImageData LoadImageData(int page, InputStream is, int orglen) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		int ret = 0;
		InputStream inputStream = new BufferedInputStream(is);

		BitmapFactory.Options option = new BitmapFactory.Options();
		Bitmap bm = null;
		option.inJustDecodeBounds = false;
		option.inPreferredConfig = Config.ARGB_8888;
		option.inSampleSize = mFileList[page].scale;

		ImageData id = null;

		Logcat.d(logLevel, "Start. " + mFileList[page].name);

		inputStream.mark(orglen + 1);
		if (FileData.getExtType(mActivity, mFileList[page].name) != FileData.EXTTYPE_AVIF && FileData.getExtType(mActivity, mFileList[page].name) != FileData.EXTTYPE_JXL) {
			bm = BitmapFactory.decodeStream(new BufferedInputStream(inputStream), null, option);
		}
		if (bm != null) {
			Logcat.v(logLevel, "BitmapFactory decode succeed. " + mFileList[page].name);
			ret = CallImgLibrary.ImageSetPage(mActivity, mHandler, mCacheIndex, page, 0);
			if (ret < 0) {
				Logcat.e(logLevel, "CallImgLibrary.ImageSetPage failed. return=" + ret + ", " + mFileList[page].name);
				return null;
			}
			Logcat.v(logLevel, "CallImgLibrary.ImageSetPage succeed. " + mFileList[page].name);
			ret = CallImgLibrary.ImageSetBitmap(mActivity, mHandler, mCacheIndex, bm);
			if (ret < 0) {
				Logcat.e(logLevel, "ImageSetBitmap failed. return=" +ret + ", " + mFileList[page].name);
				return null;
			}
			Logcat.v(logLevel, "CallImgLibrary.ImageSetBitmap succeed. " + mFileList[page].name);
			bm.recycle();
			// 読み込み成功
			mMemCacheFlag[page].fSource = true;
			id = new ImageData();
			id.Page = page;
			id.Width = mFileList[page].width;
			id.Height = mFileList[page].height;
		}
		else {
			Logcat.v(logLevel, "BitmapFactory decode failed. " + mFileList[page].name);
			try {
				inputStream.reset();
			} catch (IOException e) {
				Logcat.e(logLevel, "inputStream.reset() failed. " + mFileList[page].name, e);
				inputStream.close();
				return null;
			}

			// 読み込み準備
			Logcat.v(logLevel, MessageFormat.format("コマンドを実行します. CallImgLibrary.ImageSetPage({0}, {1}), ret={2} {3}", new Object[]{page, orglen, ret, mFileList[page].name}));
			ret = CallImgLibrary.ImageSetPage(mActivity, mHandler, mCacheIndex, page, orglen);
			if (ret  < 0) {
				Logcat.e(logLevel, MessageFormat.format("コマンドの実行に失敗しました. CallImgLibrary.ImageSetPage({0}, {1}), ret={2} {3}", new Object[]{page, orglen, ret, mFileList[page].name}));
				return null;
			}

//			Logcat.d(logLevel, "start: page=" + page);
			byte[] data = new byte[100 * 1024];
			int total = 0;
			while (true) {
				int size = 0;
				try {
					size = inputStream.read(data, 0, data.length);
					if (size <= 0) {
						break;
					}
				}
				catch (IOException e) {
					if(total != orglen) {
						Logcat.e(logLevel, "catch IOException. " + mFileList[page].name, e);
						throw new IOException("Can't read file.");
					}
					else {
						//必要なデータを読み終えていた場合に抜ける
						break;
					}
				}
				// メモリセットも中断する
				if (mCacheBreak || !mRunningFlag) {
					Logcat.e(logLevel, "User Canceled in LoadImageData. " + mFileList[page].name);
					// throw new IOException("User Canceled in LoadImageData.");
					return null;
				}
				CallImgLibrary.ImageSetData(mActivity, mHandler, mCacheIndex, data, size);
				total += size;
			}

			//		long sttime = SystemClock.uptimeMillis();
			if (total < orglen) {
				Logcat.e(logLevel, "bufferd size is short. total=" + total + ", orglen=" + orglen + ", " + mFileList[page].name);
				mFileList[page].error = true;
				throw new IOException("File is broken.");
			} else {
				int returnCode = 0;
				// 画像を取得してバッファに格納する
				returnCode = CallImgLibrary.ImageConvert(mActivity, mHandler, mCacheIndex, mFileList[page].exttype, mFileList[page].scale);
				if (returnCode >= 0 && mFileList[page].width > 0 && mFileList[page].height > 0) {
					Logcat.v(logLevel, "CallImgLibrary.ImageConvert succeed. " + mFileList[page].name);
					// 読み込み成功
					mMemCacheFlag[page].fSource = true;
					id = new ImageData();
					id.Page = page;
					id.Width = mFileList[page].width;
					id.Height = mFileList[page].height;
				} else {
					Logcat.e(logLevel, "CallImgLibrary.ImageConvert failed. return=" + returnCode + ", " + mFileList[page].name);
					mFileList[page].error = true;
				}
			}
		}
//		Logcat.i(logLevel, "time: " + (int)(SystemClock.uptimeMillis() - sttime));
		Logcat.v(logLevel, "End. " + mFileList[page].name);
		return id;
	}

	private int mScrWidth;
	private int mScrHeight;
	private int mScrCenter;
	private int mScrScaleMode;
	private int mScrDispMode;
	private int mScrAlgoMode;
	private int mScrRotate;
	private int mScrScale; // 任意倍率
	private int mScrWAdjust; // 幅調整縮小
	private int mScrWidthScale; // 幅調整
	private int mScrImgScale; // 拡大
	private int mMarginCut; // 余白削除
	private int mMarginCutColor; // 余白削除の色
	private int mMarginBlackMask;
	private int mMarginLimit;
	private int mMarginSpace;
	private int mMarginRange;
	private int mMarginStart;
	private int mMarginLevel;
	private boolean mMarginAspectMask;
	private boolean mMarginForceIgnoreAspect;
	private int mSharpen;	// シャープ化
	private int mInvert;	// カラー反転
	private int mGray;		// グレースケール
	private int mColoring;		// 自動着色
	private int mMoire;		// モアレ軽減
	private int mTopSingle;	// 先頭単ページ
	private int mGamma;		// ガンマ補正
	private int mBright;	// 明るさ
	private int mContrast;
	private int mHue;
	private int mSaturation;
	private float[] mColorMatrix;

	private boolean mScrFitDual;
	private boolean mScrNoExpand;
	private boolean mEpubOrder;
	private int mZoomType;	// ルーペ表示の拡大率

	// 画面変更
	public void setViewSize(int width, int height) {
		mScrWidth = width;
		mScrHeight = height;

		CallImgLibrary.ImageScaleFree(mActivity, mHandler, mCacheIndex, -1, -1);
		if (mFileList != null && mMemCacheFlag != null) {
			for (int i = 0; i < mFileList.length; i++) {
				if (mMemCacheFlag[i] != null && (mMemCacheFlag[i].fScale[0] || mMemCacheFlag[i].fScale[1] || mMemCacheFlag[i].fScale[2])) {
					// 要チェックにする
					mMemCacheFlag[i].fScale[0] = false;
					mMemCacheFlag[i].fScale[1] = false;
					mMemCacheFlag[i].fScale[2] = false;
				}
			}
		}
	}

	public void setEpubOrder(boolean epubOrder) {
		mEpubOrder = epubOrder;
	}

	// フィルター設定
	private void SetColorEffect() {
		// Webview版のフィルター設定の内容で書き直し
		float cont = (float)mContrast * 0.02f;
		float hue = (float)mHue;
		float sat = (float)mSaturation * 0.01f;
		// コントラストを変更
		float translate = (0.5f - 0.5f * cont) * 255.f;
		float[] values = {
			cont, 0, 0, 0, translate,
			0, cont, 0, 0, translate,
			0, 0, cont, 0, translate,
			0, 0, 0, 1, 0
		};
		ColorMatrix cm = new ColorMatrix(values);
		// 彩度を変更
		ColorMatrix saturationCM = new ColorMatrix();
		saturationCM.setSaturation(sat);
		// 連結する
		cm.postConcat(saturationCM);
		ColorMatrix hueCM = new ColorMatrix();
		// 色相を変更
		// 色相を変化させると輝度レベルが変わる可能性があったので手直ししてみた
		float hueRad = (float) (mHue * Math.PI / 180.0);
		float cosVal = (float) Math.cos(hueRad);
		float sinVal = (float) Math.sin(hueRad);
		// ITU-R BT.709 輝度の重み付け係数
		float lumR = 0.213f;
		float lumG = 0.715f;
		float lumB = 0.072f;
		// 輝度を維持したまま色相を回転させる
		float[] hueMatrix = {
			lumR + cosVal * (1 - lumR) + sinVal * (-lumR), lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,
			lumR + cosVal * (-lumR) + sinVal * (0.143f), lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
			lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * (lumG), lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0,
			0, 0, 0, 1, 0
		};
		hueCM.set(hueMatrix);
		// 連結する
		cm.postConcat(hueCM);
		// マトリックスを取り出す
		mColorMatrix = cm.getArray();
	}

	// 設定変更
	public void setConfig(int mode, int center, boolean fFitDual, int dispMode, boolean noExpand, int algoMode, int rotate, int wadjust, int wscale, int scale, int pageway, int mgncut, int mgncutcolor, int quality, int bright, int gamma, int sharpen, boolean invert, boolean gray, boolean pseland, boolean moire, boolean topsingle, boolean scaleinit, boolean epubOrder, int zoomtype, int contrast, int hue, int saturation, boolean coloring, boolean marginblackmask, int marginlevel, int marginlimit, int marginspace, int marginrange, int marginstart, boolean marginaspectmask, boolean marginforceignoreaspect, boolean enablecontentsfile) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "wscale=" + wscale + ", scale=" + scale);
		mScrScaleMode = mode;
		if (scaleinit) {
			mScrScale = scale;	// 初期化
		}
		mScrCenter = center;
		mScrFitDual = fFitDual;
		mScrDispMode = dispMode;
		mScrNoExpand = noExpand;
		mScrAlgoMode = algoMode;
		mScrRotate = rotate;
		mScrWAdjust = wadjust;
		mScrWidthScale = wscale;
		mScrImgScale = scale;
		mPageWay = pageway;
		mMarginCut = mgncut;
		mMarginCutColor = mgncutcolor;
		mMarginBlackMask = (marginblackmask == true) ? 1 : 0;
		mMarginLimit = marginlimit;
		mMarginSpace = marginspace;
		mMarginRange = marginrange;
		mMarginStart = marginstart;
		mMarginLevel = marginlevel;
		mMarginAspectMask = marginaspectmask;
		mMarginForceIgnoreAspect = marginforceignoreaspect;
		mBright = bright;
		mGamma = gamma;
		mSharpen = sharpen;
		mInvert = invert ? 1 : 0;
		mGray = gray ? 1 : 0;
		mColoring = coloring ? 1 : 0;
		mQuality = quality;
		mPseLand = pseland;
		mMoire = moire ? 1 : 0;
		mTopSingle = topsingle ? 1 : 0;
		mEpubOrder = epubOrder;
		mZoomType = zoomtype;
		mContrast  = contrast;
		mHue = hue;
		mSaturation = saturation;
		mEnableContentsFile = enablecontentsfile;

		// フィルター設定
		SetColorEffect();

		if (mCacheIndex >= 0) {
			freeScaleCache();
		}
	}

	public void setImageScale(int scale) {
		mScrScale = scale;
//		mScrScaleMode = DEF.SCALE_PINCH;
		freeScaleCache();
	}

	private void freeScaleCache() {
		mCacheBreak = true;
		synchronized (mLock) {
			CallImgLibrary.ImageCancel(mActivity, mHandler, mCacheIndex, 1);
		}
		// キャンセルを送出した後に確実に実行させるため100ミリ秒の時間待ちを入れてみた
		new Thread(() -> {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		synchronized (mLock) {
			if (!mCloseFlag) {
				CallImgLibrary.ImageCancel(mActivity, mHandler, mCacheIndex, 0);
				CallImgLibrary.ImageScaleFree(mActivity, mHandler, mCacheIndex, -1, -1);
				if (mFileList != null && mMemCacheFlag != null) {
					for (int i = 0; i < mFileList.length; i++) {
						if (mCloseFlag) {
							break;
						}
						if (mMemCacheFlag[i].fScale[0] || mMemCacheFlag[i].fScale[1] || mMemCacheFlag[i].fScale[2]) {
							// 要チェックにする
							mMemCacheFlag[i].fScale[0] = false;
							mMemCacheFlag[i].fScale[1] = false;
							mMemCacheFlag[i].fScale[2] = false;
						}
					}
				}
			}
		}
	}

	public boolean ImageScalingSync(int page1, int page2, int half1, int half2, ImageData img1, ImageData img2) {
		boolean ret = false;
		mCacheBreak = true;
		synchronized (mLock) {
//			CallImgLibrary.ImageCancel(mActivity, mHandler, mCacheIndex, 1);
//		}
//		synchronized (mLock) {
			if (!mCloseFlag) {
//				CallImgLibrary.ImageCancel(mActivity, mHandler, mCacheIndex, 0);
				ret = ImageScaling(page1, page2, half1, half2, img1, img2);
			}
		}
		return ret;
	}

	/**
	 * イメージを並べて作成
	 */
	public boolean ImageScaling(int page1, int page2, int half1, int half2, ImageData img1, ImageData img2) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "Page=" + page1 + ", Half=" + half1 + ", ■■■■■ ■■■■■ 開始 ■■■■■ ■■■■■ ");

		if (mScrWidth == 0 || mScrHeight == 0) {
			return false;
		}

		int[] src_x = {0, 0}; // 映像オリジナルサイズ
		int[] src_y = {0, 0};
		int[] adj_x = {0, 0}; // 映像拡大縮小後サイズ
		int[] adj_y = {0, 0};
		int view_x; // 1～2画像のまとめたサイズ
		int view_y;
		int disp_x = mScrWidth; // 画面の横サイズ
		int disp_x2 = disp_x - mScrCenter; // 2ページ目の横サイズ
		int disp_y = mScrHeight; // 画面の縦サイズ
		boolean fWidth;
		int pseland = mPseLand ? 1 : 0;

		int[] size = {0, 0}; // 画像の完成サイズの戻り値
		int[] margin = {0, 0, 0, 0}; // 余白サイズの戻り値, 左, 右, 上, 下
		int[] left = {0, 0};
		int[] right = {0, 0};
		int[] top = {0, 0};
		int[] bottom = {0, 0};

		// 画面サイズ
		disp_x = mScrWidth;
		disp_y = mScrHeight;
		Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 画面サイズ disp_x=" + disp_x + ", disp_y=" + disp_y);

		// 画像1の情報
		if (mScrRotate == ROTATE_NORMAL || mScrRotate == ROTATE_180DEG) {
			src_x[0] = mFileList[page1].width;
			src_y[0] = mFileList[page1].height;
		} else {
			src_x[0] = mFileList[page1].height;
			src_y[0] = mFileList[page1].width;
		}
		Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 元画像:P1 src_x1=" + src_x[0] + ", src_y1=" + src_y[0]);

		if (mMarginCut != 0) {
			// 余白カットありの場合
			// 余白のサイズを計測
			if (CallImgLibrary.GetMarginSize(mActivity, mHandler, mCacheIndex, page1, half1, 0, mMarginCut, mMarginCutColor, margin, mMarginBlackMask, mMarginLimit, mMarginSpace, mMarginRange, mMarginStart, mMarginLevel) > 0) {
				left[0] = margin[0];
				right[0] = margin[1];
				top[0] = margin[2];
				bottom[0] = margin[3];
			}
		}
		Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", マージン:P1 左=" + left[0] + ", 右=" + right[0] + ", 上=" + top[0] + ", 下=" + bottom[0]);

		if (page2 != -1) {
			// 画像2の情報
			if (mScrRotate == ROTATE_NORMAL || mScrRotate == ROTATE_180DEG) {
				src_x[1] = mFileList[page2].width;
				src_y[1] = mFileList[page2].height;
			} else {
				src_x[1] = mFileList[page2].height;
				src_y[1] = mFileList[page2].width;
			}
			Logcat.v(logLevel, "Page=" + page2 + ", Half=" + half2 + ", 元画像:P2 src_x2=" + src_x[1] + ", src_y2=" + src_y[1]);

			if (mMarginCut != 0) {
				// 余白カットありの場合
				// 余白のサイズを計測
				if (CallImgLibrary.GetMarginSize(mActivity, mHandler, mCacheIndex, page2, half2, 0, mMarginCut, mMarginCutColor, margin, mMarginBlackMask, mMarginLimit, mMarginSpace, mMarginRange, mMarginStart, mMarginLevel) > 0) {
					left[1] = margin[0];
					right[1] = margin[1];
					top[1] = margin[2];
					bottom[1] = margin[3];
				}
			}
			CallImgLibrary.GetMarginSize(mActivity, mHandler, mCacheIndex, page2, half2, 0, mMarginCut, mMarginCutColor, margin, mMarginBlackMask, mMarginLimit, mMarginSpace, mMarginRange, mMarginStart, mMarginLevel);
			Logcat.v(logLevel, "Page=" + page2 + ", Half=" + half1 + ", マージン:P2 左=" + left[1] + ", 右=" + right[1] + ", 上=" + top[1] + ", 下=" + bottom[1]);
		}

		// カットしてサイズがマイナスになったらプラスに戻す
		if (src_y[0] - top[0] - bottom[0] <= 20) {
			top[0] = (src_y[0] / 2) - 10;
			bottom[0] = (src_y[0] / 2) - 10;
		}
		if (src_x[0] - left[0] - right[0] <= 20) {
			left[0] = (src_x[0] / 2) - 10;
			right[0] = (src_x[0] / 2) - 10;
		}
		if (page2 != -1) {
			if (src_y[1] - top[1] - bottom[1] <= 20) {
				top[1] = (src_y[1] / 2) - 10;
				bottom[1] = (src_y[1] / 2) - 10;
			}
			if (src_x[1] - left[1] - right[1] <= 20) {
				left[1] = (src_x[1] / 2) - 10;
				right[1] = (src_x[1] / 2) - 10;
			}
		}

		if (mMarginCut != 0 && mMarginCut != 6) {
			// 余白カットありで縦横比を維持の場合

			if (page2 != -1) {
				if (mScrFitDual) {
					// 高さを揃える場合
					// 上下のカット率を少ないほうに合わせる
					if (top[0] * 1000 / src_y[0] > top[1] * 1000 / src_y[1]) {
						top[0] = top[1] * src_y[0] / src_y[1];
					} else {
						top[1] = top[0] * src_y[1] / src_y[1];
					}
					if (bottom[0] * 1000 / src_y[0] > bottom[1] * 1000 / src_y[1]) {
						bottom[0] = bottom[1] * src_y[0] / src_y[1];
					} else {
						bottom[1] = bottom[0] * src_y[1] / src_y[0];
					}

					Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 上下を揃える:P1 左=" + left[0] + ", 右=" + right[0] + ", 上=" + top[0] + ", 下=" + bottom[0]);
					Logcat.v(logLevel, "Page=" + page2 + ", Half=" + half1 + ", 上下を揃える:P2 左=" + left[1] + ", 右=" + right[1] + ", 上=" + top[1] + ", 下=" + bottom[1]);
				}
			}

			// 横幅が画面の縦横比より細い場合、横のカットを戻す
			int work_x = (disp_x > disp_y) ? disp_x / 2 : disp_x;
			if (left[0] + right[0] > 0) {
				int x = (src_x[0] - left[0] - right[0]);
				int y = (src_y[0] - top[0] - bottom[0]);
				if (x * 1000 / work_x < y * 1000 / disp_y) {
					int margin_x = (int) ((float) src_x[0] - ((float) y * ((float) work_x / (float) disp_y)));
					margin_x = Math.max(0, margin_x);
					if (!mMarginAspectMask) {
						left[0] = margin_x * left[0] / (left[0] + right[0]);
						right[0] = margin_x - left[0];
					}
				}
			}
			if (page2 != -1) {
				if (left[1] + right[1] > 0) {
					int x = (src_x[1] - left[1] - right[1]);
					int y = (src_y[1] - top[1] - bottom[1]);
					if (x * 1000 / work_x < y * 1000 / disp_y) {
						int margin_x = (int) ((float) src_x[1] - ((float) y * ((float) work_x / (float) disp_y)));
						margin_x = Math.max(0, margin_x);
						if (!mMarginAspectMask) {
							left[1] = margin_x * left[1] / (left[1] + right[1]);
							right[1] = margin_x - left[1];
						}
					}
				}
			}

			// 横幅が画面の縦横比より太い場合、縦のカットを戻す
			if (top[0] + bottom[0] > 0) {
				int x = (src_x[0] - left[0] - right[0]);
				int y = (src_y[0] - top[0] - bottom[0]);
				if (x * 1000 / work_x > y * 1000 / disp_y) {
					int margin_y = (int) ((float) src_y[0] - ((float) x * ((float) disp_y / (float) work_x)));
					margin_y = Math.max(0, margin_y);
					if (!mMarginAspectMask) {
						top[0] = margin_y * top[0] / (top[0] + bottom[0]);
						bottom[0] = margin_y - top[0];
					}
				}
			}
			if (page2 != -1) {
				if (top[1] + bottom[1] > 0) {
					int x = (src_x[1] - left[1] - right[1]);
					int y = (src_y[1] - top[1] - bottom[1]);
					if (x * 1000 / work_x > y * 1000 / disp_y) {
						int margin_y = (int) ((float) src_y[1] - ((float) x * ((float) disp_y / (float) work_x)));
						margin_y = Math.max(0, margin_y);
						if (!mMarginAspectMask) {
							top[1] = margin_y * top[1] / (top[1] + bottom[1]);
							bottom[1] = margin_y - top[1];
						}
					}
				}
			}

			Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 画面の比率に近づける:P1 左=" + left[0] + ", 右=" + right[0] + ", 上=" + top[0] + ", 下=" + bottom[0]);
			Logcat.v(logLevel, "Page=" + page2 + ", Half=" + half1 + ", 画面に比率に近づける:P2 左=" + left[1] + ", 右=" + right[1] + ", 上=" + top[1] + ", 下=" + bottom[1]);

			if (page2 != -1) {
				// 左右の画像の縦横比を揃える
				int x0 = src_x[0] - left[0] - right[0];
				int x1 = src_x[1] - left[1] - right[1];
				int y0 = src_y[0] - top[0] - bottom[0];
				int y1 = src_y[1] - top[1] - bottom[1];
				if (x0 * 1000 / y0 > x1 * 1000 / y1) {
					int width = x0 * y1 / y0;
					if (src_x[1] > width) {
						if (left[1] + right[1] != 0) {
							left[1] = (src_x[1] - width) * left[1] / (left[1] + right[1]);
							right[1] = (src_x[1] - width) - left[1];
						}
					} else {
						left[1] = 0;
						right[1] = 0;
					}
					Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 左右を揃える:P1 width=" + width + ", src_x=" + src_x[1] + ", 左=" + left[1] + ", 右=" + right[1]);
				} else {
					int width = x1 * y0 / y1;
					if (src_x[0] > width) {
						if (left[0] + right[0] != 0) {
							left[0] = (src_x[0] - width) * left[0] / (left[0] + right[0]);
							right[0] = (src_x[0] - width) - left[0];
						}
					} else {
						left[0] = 0;
						right[0] = 0;
					}
					Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 左右を揃える:P1 width=" + width + ", src_x=" + src_x[0] + ", 左=" + left[0] + ", 右=" + right[0]);
				}

				Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 左右を揃える:P1 左=" + left[0] + ", 右=" + right[0] + ", 上=" + top[0] + ", 下=" + bottom[0]);
				Logcat.v(logLevel, "Page=" + page2 + ", Half=" + half1 + ", 左右を揃える:P2 左=" + left[1] + ", 右=" + right[1] + ", 上=" + top[1] + ", 下=" + bottom[1]);
			}

			// 画像が横長なら左右のカットを同じにする
			if (src_x[0] > src_y[0]) {
				left[0] = Math.min(left[0], right[0]);
				right[0] = left[0];
			}


			// 元画像の縦横比をカット後の値にする
			src_x[0] = (src_x[0] - left[0] - right[0]);
			src_y[0] = (src_y[0] - top[0] - bottom[0]);
			src_x[1] = (src_x[1] - left[1] - right[1]);
			src_y[1] = (src_y[1] - top[1] - bottom[1]);

		}

		if (img1 != null) {
			if (mZoomType >= 3) {
				img1.CutLeft = left[0];
				img1.CutRight = right[0];
				img1.CutTop = top[0];
				img1.CutBottom = bottom[0];
			} else {
				//	ルーペ表示の拡大率が元画像サイズの場合はカットしない
				img1.CutLeft = 0;
				img1.CutRight = 0;
				img1.CutTop = 0;
				img1.CutBottom = 0;
			}
		}
		if (img2 != null) {
			if (mZoomType >= 3) {
				img2.CutLeft = left[1];
				img2.CutRight = right[1];
				img2.CutTop = top[1];
				img2.CutBottom = bottom[1];
			} else {
				//	ルーペ表示の拡大率が元画像サイズの場合はカットしない
				img2.CutLeft = 0;
				img2.CutRight = 0;
				img2.CutTop = 0;
				img2.CutBottom = 0;
			}
		}

		if (mMarginCut == 6 || mMarginForceIgnoreAspect) {
			// 余白削除モードが縦横比無視の場合
			// 元画像の縦横比を画面サイズにする
			src_x[0] = disp_x;
			src_y[0] = disp_y;
			src_x[1] = disp_x2;
			src_y[1] = disp_y;

		}

		// 画面縦横比調整
		if (mScrWAdjust != 100) {
			if (disp_x > disp_y) {
				// 横持ちの時
				src_x[0] = src_x[0] * 100 / mScrWAdjust;
			} else {
				// 縦持ちの時
				src_x[0] = src_x[0] * mScrWAdjust / 100;
			}
		}
		// 画像幅調整
		if (mScrWidthScale != 100) {
			src_x[0] = src_x[0] * mScrWidthScale / 100;
		}

		if (half1 != 0) {
			// 半分にする
			src_x[0] = (src_x[0] + 1) / 2;
		}
		Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", アスペクト比調整:P1 src_x1=" + src_x[0] + ", src_y1=" + src_y[0]);
		adj_x[0] = src_x[0];
		adj_y[0] = src_y[0];


		if (page2 != -1) {
			// 画面縦横比調整
			if (mScrWAdjust != 100) {
				if (disp_x2 > disp_y) {
					// 横持ちの時
					src_x[1] = src_x[1] * 100 / mScrWAdjust;
				} else {
					// 縦持ちの時
					src_x[1] = src_x[1] * mScrWAdjust / 100;
				}
			}
			// 画像幅調整
			if (mScrWidthScale != 100) {
				src_x[1] = src_x[1] * mScrWidthScale / 100;
			}

			if (half2 != 0) {
				// 半分にする
				src_x[1] = (src_x[1] + 1) / 2;
			}
			Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", アスペクト比調整:P1 src_x2=" + src_x[1] + ", src_y2=" + src_y[1]);
			adj_x[1] = src_x[1];
			adj_y[1] = src_y[1];
		}

		// 高さを揃える必要がある
		if (mScrFitDual) {
			// 拡大あり
			if (src_y[0] > src_y[1] && src_y[1] != 0) {
				adj_x[1] = src_x[1] * src_y[0] / src_y[1];
				adj_y[1] = src_y[0];
			} else if (src_y[0] < src_y[1] && src_y[0] != 0) {
				adj_x[0] = src_x[0] * src_y[1] / src_y[0];
				adj_y[0] = src_y[1];
			}
		}


		// 1～2映像を足したサイズ
		int src_cx = adj_x[0] + adj_x[1];
		int src_cy = adj_y[0] > adj_y[1] ? adj_y[0] : adj_y[1];
		Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 左右サイズ揃えP1 adj_x=" + adj_x[0] + ", adj_y=" + adj_y[0]);
		Logcat.v(logLevel, "Page=" + page2 + ", Half=" + half2 + ", 左右サイズ揃えP2 adj_x=" + adj_x[1] + ", adj_y=" + adj_y[1]);
		Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ",左右サイズ合計 src_cx=" + src_cx + ", src_cy=" + src_cy);

		// サイズ0だと0除算なので終了
		if (src_cx == 0 || src_cy == 0) {
			return false;
		}

		if (mScrScaleMode == DEF.SCALE_ORIGINAL) {
			// 元サイズのまま
			view_x = src_cx;
			view_y = src_cy;
		} else if (mScrScaleMode == DEF.SCALE_FIT_ALLMAX) {
			// 縦横比無視で拡大
			view_x = disp_x;
			view_y = disp_y;
		} else if (mScrScaleMode == DEF.SCALE_FIT_SPRMAX) {
			// 縦横比無視で拡大（見開き対応）
			if (DEF.checkPortrait(disp_x, disp_y)) {
				// 縦画面
				view_x = DEF.checkPortrait(src_cx, src_cy) ? disp_x : disp_x * 2;
			} else {
				// 横画面
				view_x = DEF.checkPortrait(src_cx, src_cy) ? disp_x / 2 : disp_x;
			}
			view_y = disp_y;
		} else if (mScrScaleMode == DEF.SCALE_FIT_WIDTH2) {
			// 幅基準（見開き対応）
			if (DEF.checkPortrait(disp_x, disp_y)) {
				// 縦画面
				view_x = DEF.checkPortrait(src_cx, src_cy) ? disp_x : disp_x * 2;
			} else {
				// 横画面
				view_x = DEF.checkPortrait(src_cx, src_cy) ? disp_x / 2 : disp_x;
			}
			view_y = src_cy * view_x / src_cx;
		} else if (mScrScaleMode == DEF.SCALE_FIT_ALL2) {
			// 全体表示（見開き対応）
			Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 全体表示（見開き対応）");
			int dispwk_x;
			if (DEF.checkPortrait(disp_x, disp_y)) {
				// 縦画面
				dispwk_x = DEF.checkPortrait(src_cx, src_cy) ? disp_x : disp_x * 2;
				Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 縦画面 dispwk_x=" + dispwk_x);
			} else {
				// 横画面
				dispwk_x = DEF.checkPortrait(src_cx, src_cy) ? disp_x / 2 : disp_x;
				Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 横画面 dispwk_x=" + dispwk_x);
			}

			if (dispwk_x * 1000 / src_cx < disp_y * 1000 / src_cy) {
				// Y方向よりもX方向の方が拡大率が小さく画面いっぱいになる
				// 幅基準
				view_x = dispwk_x;
				view_y = src_cy * dispwk_x / src_cx;
				// 誤差(1ピクセル大きい)を修正
				view_y = Math.min(view_y, disp_y);
				Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 幅基準 view_x=" + view_x + ", view_y=" + view_y);
			} else {
				// 高さ基準
				view_x = src_cx * disp_y / src_cy;
				// 誤差(1ピクセル大きい)を修正
				view_x = Math.min(view_x, dispwk_x);
				view_y = disp_y;
				Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 高さ基準 view_x=" + view_x + ", view_y=" + view_y);
			}

		} else if (mScrScaleMode == DEF.SCALE_FIT_ALL) {
			if (disp_x * 1000 / src_cx < disp_y * 1000 / src_cy) {
				// Y方向よりもX方向の方が拡大率が小さく画面いっぱいになる
				// 幅基準
				view_x = disp_x;
				view_y = src_cy * disp_x / src_cx;
				// 誤差(1ピクセル大きい)を修正
				view_y = Math.min(view_y, disp_y);
			} else {
				// 高さ基準
				view_x = src_cx * disp_y / src_cy;
				// 誤差(1ピクセル大きい)を修正
				view_x = Math.min(view_x, disp_x);
				view_y = disp_y;
			}
		} else {

			if (mScrScaleMode == DEF.SCALE_FIT_WIDTH) {
				// 幅にあわせる
				fWidth = true;
			} else {
				// 高さにあわせる
				fWidth = false;
			}

			if (fWidth) {
				// 幅基準
				view_x = disp_x;
				view_y = src_cy * disp_x / src_cx;
			} else {
				// 高さ基準
				view_x = src_cx * disp_y / src_cy;
				view_y = disp_y;
			}
		}

		if (view_x > src_cx && mScrNoExpand) {
			// 拡大かつ拡大しないモードのときは元サイズのまま
			view_x = src_cx;
			view_y = src_cy;
		}

		Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 表示方法を反映 view_x=" + view_x + ", view_y=" + view_y);

		int[] width = new int[2];
		int[] height = new int[2];
		int[] fitwidth = new int[2];
		int[] fitheight = new int[2];

		// サイズ算出 & リサイズ
		width[0] = view_x * adj_x[0] / (adj_x[0] + adj_x[1]);
		height[0] = view_y * adj_y[0] / src_cy;

		if (page2 >= 0) {
			width[1] = view_x - width[0];
			height[1] = view_y * adj_y[1] / src_cy;
		}

		// 拡大しすぎの時は抑える
		int limit = Math.max(mScrWidth, mScrHeight) * 3;
		for (int i = 0 ; i < 2 ; i ++) {
    		if (mScrScaleMode == DEF.SCALE_FIT_HEIGHT) {
    			if (width[i] > limit) {
    				// 高さに合わせる場合の幅は画面の長辺の2倍まで
    				height[i] = height[i] * limit / width[i] ;
    				width[i] = limit;
    			}
    		}
    		if (mScrScaleMode == DEF.SCALE_FIT_WIDTH) {
    			if (height[i] > limit) {
    				// 幅さに合わせる場合の高さ画面の長辺の2倍まで
    				width[i] = width[i] * limit / height[i];
    				height[i] = limit;
    			}
    		}
    		if (page2 < 0) {
    			// 2ページ目はない
    			break;
    		}
		}

		Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 指定サイズP1 width=" + width[0] + ", height=" + height[0]);
		Logcat.v(logLevel, "Page=" + page2 + ", Half=" + half2 + ", 指定サイズP2 width=" + width[1] + ", height=" + height[1]);

		// 任意スケールの設定前に100%状態のサイズを保持
		fitwidth[0] = width[0];
		fitheight[0] = height[0];
		fitwidth[1] = width[1];
		fitheight[1] = height[1];

		mFileList[page1].fwidth[half1] = fitwidth[0];
		mFileList[page1].fheight[half1] = fitheight[0];
		if (page2 != -1) {
			mFileList[page2].fwidth[half1] = fitwidth[1];
			mFileList[page2].fheight[half1] = fitheight[1];
		}
		// 任意スケールは結果に対して設定
		width[0] = width[0] * mScrScale / 100;
		height[0] = height[0] * mScrScale / 100;

		if (page2 >= 0) {
			width[1] = width[1] * mScrScale / 100;
			height[1] = height[1] * mScrScale / 100;
		}

		if (page1 >= 0 && mMemCacheFlag[page1].fSource && width[0] > 0 && height[0] > 0) {
			Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", ソース読み込み済み");
			if (mFileList[page1].swidth[half1] == width[0] && mFileList[page1].sheight[half1] == height[0] && mMemCacheFlag[page1].fScale[half1]) {
				Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 画像作成済み");
				if (img1 != null) {
					img1.SclWidth = width[0];
					img1.SclHeight = height[0];
					img1.FitWidth = fitwidth[0];
					img1.FitHeight = fitheight[0];
				}
			}
			else {
				Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 画像作成開始");
				mFileList[page1].swidth[half1] = width[0];
				mFileList[page1].sheight[half1] = height[0];
				if (memWriteLock(page1, half1, true, false)) {
					// スケール作成
					sendMessage(mHandler, DEF.HMSG_CACHE, 0, 2, null);
//					long sttime = SystemClock.uptimeMillis();
					int param = CallImgLibrary.ImageScaleParam(mInvert, mGray, mColoring, mMoire, pseland);
					if (CallImgLibrary.ImageScale(mActivity, mHandler, mCacheIndex, page1, half1, width[0], height[0], left[0], right[0], top[0], bottom[0], mScrAlgoMode, mScrRotate, mMarginCut, mMarginCutColor, mSharpen, mBright, mGamma, param, size, mColorMatrix) >= 0) {
						Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1 + ", 完成サイズP1 size_w=" + size[0] + ", size_h=" + size[1]);
						mMemCacheFlag[page1].fScale[half1] = true;
						if (img1 != null) {
							img1.SclWidth = width[0];
							img1.SclHeight = height[0];
							img1.FitWidth = fitwidth[0];
							img1.FitHeight = fitheight[0];
						}
					}
					else {
						mFileList[page1].swidth[half1] = 0;
						mFileList[page1].sheight[half1] = 0;
					}
//					Logcat.i(logLevel, "time: " + (int)(SystemClock.uptimeMillis() - sttime));
					sendMessage(mHandler, DEF.HMSG_CACHE, -1, 0, null);
				}
			}
			if (img1 != null) {
				Logcat.v(logLevel, "Page=" + page1 + ", Half=" + half1
						+ ", Width=" + img1.Width + ", Height=" + img1.Height
						+ ", FitWidth=" + img1.FitWidth + ", FitHeight=" + img1.FitHeight
						+ ", SclWidth=" + img1.SclWidth + ", SclHeight=" + img1.SclHeight);
			}
		}

		if (page2 >= 0 && mMemCacheFlag[page2].fSource && width[1] > 0 && height[1] > 0) {
			// 見開き時
			if (mFileList[page2].swidth[half2] == width[1] && mFileList[page2].sheight[half2] == height[1] && mMemCacheFlag[page2].fScale[half2]) {
				if (img2 != null) {
					img2.SclWidth = width[1];
					img2.SclHeight = height[1];
					img2.FitWidth = fitwidth[1];
					img2.FitHeight = fitheight[1];
				}
			}
			else {
				mFileList[page2].swidth[half2] = width[1];
				mFileList[page2].sheight[half2] = height[1];
				if (memWriteLock(page2, half2, true, false)) {
					// スケール作成
					sendMessage(mHandler, DEF.HMSG_CACHE, 0, 2, null);
//					long sttime = SystemClock.uptimeMillis();
					int param = CallImgLibrary.ImageScaleParam(mInvert, mGray, mColoring, mMoire, pseland);
					if (CallImgLibrary.ImageScale(mActivity, mHandler, mCacheIndex, page2, half2, width[1], height[1], left[1], right[1], top[1], bottom[1], mScrAlgoMode, mScrRotate, mMarginCut, mMarginCutColor, mSharpen, mBright, mGamma, param, size, mColorMatrix) >= 0) {
						Logcat.d(logLevel, "Page=" + page2 + ", Half=" + half2 + ", 完成サイズP2 size_w=" + size[0] + ", size_h=" + size[1]);
						mMemCacheFlag[page2].fScale[half2] = true;
						if (img2 != null) {
							img2.SclWidth = width[1];
							img2.SclHeight = height[1];
							img2.FitWidth = fitwidth[1];
							img2.FitHeight = fitheight[1];
						}
					}
					else {
						mFileList[page2].swidth[half2] = 0;
						mFileList[page2].sheight[half2] = 0;
					}
//					Logcat.i(logLevel, "time: " + (int)(SystemClock.uptimeMillis() - sttime));
					sendMessage(mHandler, DEF.HMSG_CACHE, -1, 0, null);
				}
			}
			if (img2 != null) {
				Logcat.v(logLevel, "Page=" + page2 + ", Half=" + half2
						+ ", Width=" + img2.Width + ", Height=" + img2.Height
						+ ", FitWidth=" + img2.FitWidth + ", FitHeight=" + img2.FitHeight
						+ ", SclWidth=" + img2.SclWidth + ", SclHeight=" + img2.SclHeight);
			}
		}

		//		Logcat.d(logLevel, "end: p1=" + page1 + ", p2=" + page2 + ", h1=" + half1 + ", h2=" + half2);
		return true;
	}

	// 指定した保存ファイル名で指定ページを書き出し
	// nameがnullなら元のファイル名で
	public String decompFile(int page, String name) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (page < 0 || mFileList.length <= page) {
			// 範囲外
			return null;
		}

		String resultPath = null;
		mCacheBreak = true;
		synchronized (mLock) {
//			CallImgLibrary.ImageCancel(mActivity, mHandler, mCacheIndex, 1);
//		}
//		synchronized (mLock) {
			if (!mCloseFlag) {
//				CallImgLibrary.ImageCancel(mActivity, mHandler, mCacheIndex, 0);

				// キャッシュ読込モードオン
				new File(DEF.getBaseDirectory()).mkdirs();
				new File(DEF.getBaseDirectory() + "share/").mkdirs();

				if (name == null) {
					name = new String();
					name = mFileList[page].name;
					if (mFileType == FileData.FILETYPE_PDF) {
						name += ".jpg";
					}
				}
				name = name.replace("\\", "_");
				name = name.replace("/", "_");
				String file = DEF.getBaseDirectory() + "share/" + name;
				new File(file).delete();

				BufferedOutputStream os;
				try {
					os = new BufferedOutputStream(new FileOutputStream(file), 500 * 1024);
				} catch (FileNotFoundException e) {
					Logcat.e(logLevel, "", e);
					return null;
				}
				byte[] buff = new byte[BIS_BUFFSIZE];

				try {
					mCheWriteFlag = false;
					setLoadBitmapStart(page, false);

					if (mFileType == FileData.FILETYPE_PDF) {
						Logcat.d(logLevel, "PDFファイルを開きます.");
						//ページ番号を指定してPdfRenderer.Pageインスタンスを取得する。
						PdfRenderer.Page pdfPage = mPdfRenderer.openPage(page);
						//PdfRenderer.Pageの情報を使って空の描画用Bitmapインスタンスを作成する。
						Bitmap bm = Bitmap.createBitmap(pdfPage.getWidth() , pdfPage.getHeight() , Config.ARGB_8888);
						// PDFをレンダリングする前にBitmapを白く塗る。
						Canvas canvas = new Canvas(bm);
						canvas.drawColor(Color.WHITE);
						canvas.drawBitmap(bm, 0, 0, null);
						//空のBitmapにPDFの内容を描画する。
						pdfPage.render(bm , null,null , PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
						//PdfRenderer.Pageを閉じる、この処理を忘れると次回読み込む時に例外が発生する。
						pdfPage.close();
						if (bm == null) {
							Logcat.e(logLevel, "PDFファイルのレンダリングに失敗しました.");
							return null;
						}
						else {
							Logcat.d(logLevel, "PDFファイルのレンダリングに成功しました.");
							Logcat.d(logLevel, "JPG形式で保存します.");
							// jpegで保存
							bm.compress(Bitmap.CompressFormat.JPEG, 100, os);
						}
					}
					else if (IsArchive(mFileType)) {
						Logcat.v(logLevel, "圧縮ファイルです. " + mFilePath + " " + mFileList[page].name);
						int targetIndex = -1;
						// ファイル名でインデックスを検索
						for (int i = 0; i < numberOfItems; i++) {
							if (mFileList[page].name.equals(mItems[i])) {
								targetIndex = i;
								break;
							}
						}
						if (targetIndex == -1) {
						}
						else {
							File cacheFile = new File(mCacheDir, mCacheName + targetIndex);
							if (!cacheFile.exists() || cacheFile.length() == 0) {
								LoadFileToCache(targetIndex);
							}
							// キャッシュファイルファイルから直接ストリームを作成
							InputStream fileStream = new FileInputStream(cacheFile);
							InputStream inputStream = new BufferedInputStream(fileStream);
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							byte[] buffer = new byte[8192];
							int len;
							while ((len = inputStream.read(buffer)) != -1) {
								bos.write(buffer, 0, len);
							}
							byte[] inputbuff = bos.toByteArray();
							os.write(inputbuff, 0, inputbuff.length);
						}
					} else {
						while (mRunningFlag) {
							if (mCloseFlag) {
								break;
							}
							int readsize = this.read(buff, 0, buff.length);
							if (readsize <= 0) {
								break;
							}
							os.write(buff, 0, readsize);
						}
					}
					os.flush();
					os.close();
					resultPath = file;
				} catch (IOException e) {
					Logcat.e(logLevel, "", e);
					resultPath = null;
				}

				try {
					setLoadBitmapEnd();
				} catch (Exception e) {
					Logcat.e(logLevel, "", e);
					resultPath = null;
				}
			}
		}
		return resultPath;
	}

	// 指定ページをファイルに書き出し
	public String decompFile(int page) {
		return decompFile(page, null);
	}

	// 共有ファイルを削除する
	public void deleteShareCache() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		// キャッシュ保存先
		String path = DEF.getBaseDirectory() + "share/";

		// ファイルのリスト取得
		File[] files = new File(path).listFiles();
		if (files == null || files.length == 0) {
			// ファイルなし
			Logcat.d(logLevel, "ファイルがありません.");
			return;
		}

		// ファイルのリストを全て削除
		for (File file : files) {
			file.delete();
		}
	}

	public class CacheInputStream extends InputStream {
		InputStream	mInputStream;

		public CacheInputStream(InputStream is) throws IOException {
			mInputStream = is;
		}

		@Override
		public int read() throws IOException {
			// 自動生成されたメソッド・スタブ
			return 0;
		}

		@Override
		public int read(byte[] buf, int off, int len) throws IOException {
			int size = len;
			int total = 0;
			int ret = 0;
			while (size > 0) {
				ret = mInputStream.read(buf, off + total, size);
				if (!mRunningFlag) {
					return DEF.RETURN_CODE_TERMINATED;
				}
				if (ret <= 0) {
					break;
				}
				total += ret;
				size -= ret;
			}
			if (total == 0 && ret < 0) {
				return -1;
			}
			return total;
		}
	}

	public int getCacheIndex() {
		return mCacheIndex;
	}
	// 展開の手順でソリッド書庫の先読みの有無を設定する(サムネイル画像の作成時は先読みをさせない)
	public void setAccessMode(boolean mode) {
		mAccessMode = mode;
	}
	// 圧縮ファイルからキャッシュファイルへ書き出す
	public void LoadFileToCache(int currentPage) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		int[] indices;
		boolean[] checklist;
		// ここで登録(リトライ中に変更されておかしな動きになるのを防ぐ)
		boolean mode = !mAccessMode;
		int mStart = 0;
		int countmax = 0;
		int count = 0;
		int fileseek_max = 0;
		int error_retry_max;
		int error_delay_max;
		final int[] countstart = {0};
		if (archive == null) {
			Logcat.d(logLevel, "アーカイブが既に閉じられています.");
			return;
		}
		if (mAccessType == DEF.ACCESS_TYPE_SMB) {
			// SMBのアクセスは特別にリトライ回数と時間待ちを工夫する
			error_retry_max = FILEERROR_RETRY_SMB;
			error_delay_max = FILEERROR_DELAY_SMB;
		}
		else {
			// ローカルパス/ストレージアクセスフレームワークの場合はリトライの必要はないがもしものために設定する
			error_retry_max = FILEERROR_RETRY;
			error_delay_max = FILEERROR_DELAY;
		}
		boolean errorcheck = false;
		int errorcount = 0;
		do {
			// 中断フラグをクリア(7-Zip-JBinding-4Androidが実行を中断しないようにする)
			if (Thread.interrupted()) {
				Logcat.d(logLevel, "中断フラグが立っていたためクリアしました.");
			}
			if (mSolid && mode) {
				// ソリッド書庫でサムネイル画像の作成以外の場合
				int total = 0;
				int fileseek;
				// ソリッド書庫の場合は先読みの範囲を前後に広げる
				fileseek = FILESEEK_MAX_SOLID;
				fileseek_max = fileseek * 2;
				// 先読みの先頭位置を設定
				if (currentPage < fileseek) {
					mStart = 0;
				}
				else {
					mStart = currentPage - fileseek;
				}
				// 圧縮ファイルのエントリー数を得る
				total = numberOfItems;
				for (int i = 0; i < fileseek_max; i++) {
					// ソリッド書庫の場合は必ず先頭から連続する必要がある
					if ((mStart + i) >= total) {
						// 圧縮ファイルのエントリー数を超えた場合はループ終了
						break;
					}
					countmax++;

				}
				// ページの前後を先読みさせるためのリストを作成
				indices = new int[countmax];
				for (int i = 0; i < countmax; i++) {
					// 登録
					indices[count] = mStart + i;
					count++;
				}
				// ソートを行う
				indices = IntStream.of(indices).distinct().sorted().toArray();
			}
			else {
				// 通常の書庫の場合は1ファイル毎に解凍する
				indices = new int[1];
				indices[0] = currentPage;
			}
			if (mSolid) {
				// メッセージを送る
				mMessageMode = DEF.MESSAGE_IMAGE_START;
				sendProgress(0, 0, 0, mStart);
			}
			// 解凍処理を実行
			errorcheck = false;
			try {
				int finalMStart = mStart;
				int finalCount = count;
				// 圧縮ファイルからの解凍を行う(7-Zip-JBinding-4Android)
				archive.extract(indices, false, new IArchiveExtractCallback() {
					BufferedOutputStream currentBufferedStream = null;
					@Override
					public ISequentialOutStream getStream(int index, ExtractAskMode mode) throws SevenZipException {
						if (!mRunningFlag) {
							// 外部から中断させる場合
							// 例外を投げて全体の処理を強制終了させる
							throw new SevenZipException("User cancelled extraction.");
						}
						if (mode != ExtractAskMode.EXTRACT) {
							// 処理中なら
							return null;
						}
						// 展開中の進捗のメッセージを送る
						sendProgress(0, (int)((float) countstart[0] / (float)finalCount * 100), index, finalMStart + finalCount);
						countstart[0]++;
						try {
							// キャッシュファイルを開く
							File tempFile = new File(mCacheDir, mCacheName + index);
							currentBufferedStream = new BufferedOutputStream(new FileOutputStream(tempFile), 32768);
					        // 毎回新しくストリームを開く(EMFILEエラー対策)
							final BufferedOutputStream fos = currentBufferedStream;
							// ラムダ式を返す
							return data -> {
								try {
									fos.write(data, 0, data.length);
									return data.length;
								}
								catch (IOException e) {
									throw new SevenZipException("Write error", e);
								}
							};
					    }
						catch (IOException e) {
							// エラーが発生した場合もSevenZipExceptionを投げて中断させる
							throw new SevenZipException("Failed to open stream for index " + index, e);
						}
					}
	    			@Override
					public void setOperationResult(ExtractOperationResult result) {
						// 処理が終了した場合
						if (currentBufferedStream != null) {
							try {
								currentBufferedStream.flush();
								currentBufferedStream.close();
							}
							catch (IOException e) {
							}
							finally {
								// 参照をクリア
								currentBufferedStream = null;
							}
						}
					}
					public void prepareOperation(ExtractAskMode mode) {}
					public void setCompleted(long completeValue) {}
					public void setTotal(long totalValue) {}
				});
			}
			catch (IOException e) {
				// エラーになった
				errorcheck = true;
				errorcount++;
				Logcat.e(logLevel, "", e);
				// 中断フラグをクリア
				Thread.interrupted();
				// リトライ前に時間待ちを入れる
				try {
					Thread.sleep(error_delay_max);
				}
				catch (Exception ex) {
				}
			}
			// もしもエラーになった場合はリトライを行う
		} while (errorcheck && errorcount < error_retry_max && mRunningFlag);
		if (errorcount > 0) {
			Logcat.v(logLevel, "errorcount=" + errorcount);
		}
		if (mSolid) {
			mMessageMode = DEF.MESSAGE_IMAGE_END;
			sendProgress(0, 0, 0, 0);
		}
	}
}
