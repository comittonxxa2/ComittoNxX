package src.comitton.fileaccess;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.text.MessageFormat;

import src.comitton.common.DEF;
import src.comitton.common.Logcat;

public class WorkStream extends InputStream {
	private static final String TAG = "WorkStream";

	private final Activity mActivity;
	private String mURI;
	private String mUser;
	private String mPass;
	private FileAccess mFileAccess;
	private long mPos;
	private Handler mHandler;

	// 再接続の試行回数と、やり直す前の待ち時間
	private final static int REOPEN_TRIES = 3;
	private final static int REOPEN_WAIT_MILLIS = 500;

	public WorkStream(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String user, @NonNull final String pass, @NonNull final Handler handler) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", user=" + user + ", pass=" + Logcat.mask(pass));
		mActivity = activity;
		mURI = uri;
		mUser = user;
		mPass = pass;
		mHandler = handler;
		Open();
	}

	private void Open() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + mURI + ", user=" + mUser + ", pass=" + Logcat.mask(mPass));
//		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "OPEN");

		try {
			mFileAccess = new FileAccess(mActivity, mURI, mUser, mPass, null);
			mFileAccess.openRandomAccessFile("r");
		} catch (FileAccessException e) {
			DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "");
			throw new IOException(TAG + ": Open: " + e.getLocalizedMessage());
		}
//		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "");
	}

	/**
	 * 読み込みに失敗したとき、ファイルを開き直して元の位置へ戻す.
	 * 失敗したら少し待ってやり直し、それでもだめなら IOException を投げる
	 * (以前は失敗を無視していたため、中身のない接続のまま次の読み込みで NullPointerException になっていた).
	 * 経過時間には、アプリが裏で止められていた時間も含まれる.
	 */
	private void reOpen() throws IOException {
		long perfReopenStart = System.currentTimeMillis();
		Logcat.w(Logcat.LOG_LEVEL_WARN, "再接続します. uri=" + mURI);

		// 切れた接続は閉じておく(閉じる処理は別スレッドで行われる)
		if (mFileAccess != null) {
			try {
				mFileAccess.close();
			} catch (Exception ignored) {
				;
			}
			mFileAccess = null;
		}

		IOException lastError = null;
		for (int i = 1; i <= REOPEN_TRIES; i++) {
			try {
				if (i > 1) {
					Thread.sleep(REOPEN_WAIT_MILLIS);
				}
				Open();
				mFileAccess.seek(mPos);
				Logcat.w(Logcat.LOG_LEVEL_WARN, "再接続しました. uri=" + mURI + ", tries=" + i + ", elapsed=" + (System.currentTimeMillis() - perfReopenStart) + "ms");
				return;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new InterruptedIOException(TAG + ": reOpen: 中断されました");
			} catch (Exception e) {
				lastError = (e instanceof IOException) ? (IOException) e : new IOException(TAG + ": reOpen: " + e, e);
				Logcat.w(Logcat.LOG_LEVEL_WARN, "再接続に失敗しました. tries=" + i + ", error=" + e);
			}
		}
		Logcat.w(Logcat.LOG_LEVEL_WARN, "再接続できませんでした. uri=" + mURI + ", elapsed=" + (System.currentTimeMillis() - perfReopenStart) + "ms");
		throw lastError;
	}

	@Override
	public void close() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
//		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "CLOSE");
		if (mFileAccess != null) {
			mFileAccess.close();
			mFileAccess = null;
		}
//		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "");
	}

	@Override
	public int read() throws IOException {
		return 0;
	}

	@Override
	public int read(byte[] b, int off, int size) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// 転送のホットパスなので、実際に出力されないログレベルのときはMessageFormat.format等の
		// 文字列生成コストをかけない
		boolean logEnabled = Logcat.global_log_level <= Logcat.LOG_LEVEL_DEBUG;
		if (logEnabled) {
			Logcat.d(logLevel, MessageFormat.format("開始します. pos={0}, off={1}, size={2}, pos+off+size={3}, length={4}", new Object[]{mPos, off, size, mPos+off+size, length()}));
		}
//		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "READ");

		// off は配列の書き込み位置で、ファイル上の位置ではないので mPos には足さない
		// (mPos は再接続したときのシーク先に使う)
		int ret = 0;
		try {
			ret = mFileAccess.read(b, off, size);
			if (ret > 0) {
				mPos += ret;
			}
		}
		catch (Exception e) {
			// 原因を同じ行に書く(2行目以降はログ取得で落ちるため)
			Logcat.w(logLevel, "読み込みに失敗しました. pos=" + mPos + ", error=" + e);
			reOpen();
			ret = mFileAccess.read(b, off, size);
			if (ret > 0) {
				mPos += ret;
			}
		}
		if (logEnabled) {
			Logcat.d(logLevel, MessageFormat.format("終了します. ret={0}, off={1}, mPos={2}, length={3}", new Object[]{ret, off, mPos, length()}));
		}
//		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "");
		return ret;
	}

	public void seek(long pos) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, MessageFormat.format("開始します. pos={0}", new Object[]{pos}));
//		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "SEEK");
		try {
			mFileAccess.seek(pos);
		}
		catch (Exception e) {
			Logcat.e(logLevel, MessageFormat.format("Catch Exception. pos={0} {1}", new Object[]{pos, e.toString()}));
		}
		mPos = pos;
//		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "");
	}


	public long getFilePointer() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
//		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "GET_FILEPOINTER");

		long result = 0L;
		try {
			result = mFileAccess.getFilePointer();
		}
		catch (Exception e) {
			Logcat.e(logLevel, "位置の取得に失敗しました. error=" + e);
			reOpen();
			result = mFileAccess.getFilePointer();
		}
//		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "");
		return result;
	}

	public long length() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, " 開始します.");
//		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "LENGTH");

		long length = 0;
		try {
			length = mFileAccess.length();
			Logcat.d(logLevel, MessageFormat.format("終了します. length={0}", new Object[]{length}));
		}
		catch (Exception e) {
			Logcat.e(logLevel, "サイズの取得に失敗しました. error=" + e);
			reOpen();
			length = mFileAccess.length();
		}
		Logcat.d(logLevel, MessageFormat.format("終了します. length={0}", new Object[]{length}));
//		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "");
		return length;
	}
}
