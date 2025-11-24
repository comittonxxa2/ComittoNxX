package src.comitton.fileview.filelist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import jp.dip.muracoro.comittonx.R;

import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.config.SetFileListActivity;
import src.comitton.fileaccess.FileAccess;
import src.comitton.fileview.data.FileData;
import src.comitton.dialog.LoadingDialog;
import src.comitton.imageview.ImageActivity;
import src.comitton.imageview.ImageManager;
import src.comitton.textview.TextManager;
import src.comitton.config.SetTextActivity;
import src.comitton.config.SetSortActivity;
import src.comitton.imageview.MyImageView;
import src.comitton.dialog.CustomProgressDialog;

import android.content.res.Resources;
import android.graphics.Point;
import android.view.WindowMetrics;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

@SuppressLint("DefaultLocale")
public class FileSelectList implements Runnable, Callback, DialogInterface.OnDismissListener {
	private static final String TAG = "FileSelectList";

	//標準のストレージパスを保存
	private static final String mStaticRootDir = Environment.getExternalStorageDirectory().getAbsolutePath() +"/";

	private ArrayList<FileData> mFileList = null;
	private ArrayList<FileData> m2FileList = null;
	private ArrayList<FileData> m3FileList = null;

	private String mURI;
	private String mPath;
	private String mUriPath;
	private String mUser;
	private String mPass;
	private static int mSortMode = 0;
	private int mSortModeOld = -1;
	private static int mSortModeOld2 = -1;
	private boolean mParentMove;
	private boolean mHidden;
	private boolean mFilter;
	private boolean mApplyDir;
	private String mMarker;
	private boolean mEpubViewer;
	private static boolean mKeepSortShuffle;
	private static boolean mKeepShuffle = false;
	private static boolean mKeepShuffle2 = false;

	public LoadingDialog mDialog;
	private static CustomProgressDialog mProgressDialog;
	private static FragmentManager supportFragmentManager;
	private static Handler mHandler;
	private Handler mActivityHandler;
	private static AppCompatActivity mActivity;
	private static SharedPreferences mSp;
	private ImageManager mImageMgr = null;
	private TextManager mTextMgr;
	private Thread mThread;
	private MyImageView mImageView = null;

	private static float mDensity;
	private static int mHeadSizeOrg;
	private static int mBodySizeOrg;
	private static int mRubiSizeOrg;
	private static int mInfoSizeOrg;
	private static int mMarginWOrg;
	private static int mMarginHOrg;

	private static int mPaperSel;
	private static int mTextWidth;
	private static int mTextHeight;
	private static int mHeadSize;
	private static int mBodySize;
	private static int mRubiSize;
	private static int mInfoSize;
	private static int mPicSize;
	private static int mSpaceW;
	private static int mSpaceH;
	private static int mMarginW;
	private static int mMarginH;

	private static int mAscMode;	// 半角の表示方法
	private static String mFontFile;
	private static boolean mChangeTextSize = false;
	private static boolean mCacheFile = false;
	private boolean mFileListFastReadOff = false;
	private static int old_progress;
	private static int progress;
	private static Thread mMultiThread = null;
	private static Handler mainHandler;
	private static boolean threadstartcheck = false;
	private static int mSoftImageFile;
	private static int mSoftDirFile;
	private static int mSoftTextFile;
	private static int mSoftCompFile;
	private static int mSoftPdfFile;
	private static int mSoftEpubFile;
	private static int mSoftOtherFile;
	private static boolean mSoftDirTop;
	private static int mSoftFileTop;
	private static int mSoftImageFileOld = -1;
	private static int mSoftDirFileOld = -1;
	private static int mSoftTextFileOld = -1;
	private static int mSoftCompFileOld = -1;
	private static int mSoftPdfFileOld = -1;
	private static int mSoftEpubFileOld = -1;
	private static int mSoftOtherFileOld = -1;
	private static int mSpecificSoftMode;
	private static Random random;
	private static int random_data1[];
	private static int random_data2[];
	private static int mRandowCount = 0;

	public FileSelectList(Handler handler, AppCompatActivity activity, SharedPreferences sp) {
		mActivityHandler = handler;
		mHandler = new Handler(this);
		mActivity = activity;
		mSp = sp;
		random = new Random();
		random_data1 = new int[10000];
		random_data2 = new int[10000];
		// シャッフル用の乱数を作成
		for (int i = 0; i < 10000; i++) {
			random_data1[i] = random.nextInt(10000);
			random_data2[i] = random.nextInt(10000);
		}
	}

	// パス
	public void setPath(String uri, String path, String user, String pass) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", path=" + path);
		//if (debug) {DEF.StackTrace(TAG, "setPath: ");}

		mURI = uri;
		mPath = path;
		mUriPath = DEF.relativePath(mActivity, mURI, mPath);
		mUser = user;
		mPass = pass;
	}

	private static void SortOptionReset() {
		mSoftDirFileOld = -1;
		mSoftImageFileOld = -1;
		mSoftTextFileOld = -1;
		mSoftCompFileOld = -1;
		mSoftPdfFileOld = -1;
		mSoftEpubFileOld = -1;
		mSoftOtherFileOld = -1;
	}

	// ファイル別ソート
	private static void SoftFileOption(ArrayList<FileData> List) {
		// ファイル別のソートオプションを読み込む
		mSoftDirFile = SetSortActivity.getSoftDirFile(mSp);
		mSoftImageFile = SetSortActivity.getSoftImageFile(mSp);
		mSoftTextFile = SetSortActivity.getSoftTextFile(mSp);
		mSoftCompFile = SetSortActivity.getSoftCompFile(mSp);
		mSoftPdfFile = SetSortActivity.getSoftPdfFile(mSp);
		mSoftEpubFile = SetSortActivity.getSoftEpubFile(mSp);
		mSoftOtherFile = SetSortActivity.getSoftOtherFile(mSp);
		mSoftDirTop = SetSortActivity.getSoftDirTop(mSp);
		mSoftFileTop = SetSortActivity.getSoftFileTop(mSp);

		if (mSoftDirFile == 0 && mSoftImageFile == 0 && mSoftTextFile == 0 && mSoftCompFile == 0 && mSoftPdfFile == 0 && mSoftEpubFile == 0 && mSoftOtherFile == 0 && mSoftDirTop == false && mSoftFileTop == 0) {
			// 設定されていなければ戻る
			return;
		}

		try {
			// 乱数を生成する際に落ちる可能性があるのでtry～catchで囲む
			if (!mKeepSortShuffle || mKeepSortShuffle && !mKeepShuffle2) {
				mKeepShuffle2 = true;
				// シャッフルのパターンを更新する場合
				// シャッフル用の乱数を作成
				for (int i = 0; i < 10000; i++) {
					random_data1[i] = random.nextInt(10000);
					random_data2[i] = random.nextInt(10000);
				}
				// 更新をリセット
				SortOptionReset();
			}
			if (mSortMode != mSortModeOld2) {
				// ソートオプションが更新された場合
				// 更新をリセット
				SortOptionReset();
			}
			mSortModeOld2 = mSortMode;
			if (mSortMode == 0) {
				// ソート無しの場合はシャッフルを毎回実行させないため一回のみ
				if (mSoftDirFile != mSoftDirFileOld || mSoftImageFile != mSoftImageFile || mSoftTextFile != mSoftTextFileOld || mSoftCompFile != mSoftCompFileOld || mSoftPdfFile != mSoftPdfFileOld || mSoftEpubFile != mSoftEpubFileOld || mSoftOtherFile != mSoftOtherFile) {
					// ファイル別のソートオプションが更新された場合
					// ソート実行
					Collections.sort(List, new SpecificComparator());
				}
				// 更新
				mSoftDirFileOld = mSoftDirFile;
				mSoftImageFileOld = mSoftImageFile;
				mSoftTextFileOld = mSoftTextFile;
				mSoftCompFileOld = mSoftCompFile;
				mSoftPdfFileOld = mSoftPdfFile;
				mSoftEpubFileOld = mSoftEpubFile;
				mSoftOtherFileOld = mSoftOtherFile;
			}
			else {
				// ソート有りの場合はあらかじめソートされているので毎回実行
				// 毎回同じ乱数を発生させるためシャッフル位置をリセット
				mRandowCount = 0;
				// ソート実行
				Collections.sort(List, new SpecificComparator());
			}
		}
		catch (Exception e) {
		}
	}

	// ソートモード
	public void setMode(int mode, boolean keep) {
		mSortMode = mode;
		if (mSortMode != mSortModeOld) {
			// モードが変化したらシャッフルの保持を解除
			mKeepShuffle = false;
			mKeepShuffle2 = false;
		}
		mSortModeOld = mSortMode;
		mKeepSortShuffle = keep;
		if (mFileList != null) {
			// ソートあり設定の場合
			if (mSortMode == DEF.ZIPSORT_SHUFFLESEP || mSortMode == DEF.ZIPSORT_SHUFFLEMGR) {
				if (!mKeepSortShuffle || mKeepSortShuffle && !mKeepShuffle) {
					mKeepShuffle = true;
					// シャッフル
					ArrayList<FileData> par_list = new ArrayList<FileData>();
					ArrayList<FileData> all_list = new ArrayList<FileData>();
					ArrayList<FileData> dir_list = new ArrayList<FileData>();
					ArrayList<FileData> file_list = new ArrayList<FileData>();
					// ディレクトリとファイルを分離
					for (int i = 0; i < mFileList.size(); i++) {
						if (mFileList.get(i).getType() == FileData.FILETYPE_PARENT) {
							par_list.add(mFileList.get(i));
						}
						else if (mFileList.get(i).getType() == FileData.FILETYPE_DIR) {
							dir_list.add(mFileList.get(i));
							all_list.add(mFileList.get(i));
						}
						else {
							file_list.add(mFileList.get(i));
							all_list.add(mFileList.get(i));
						}
					}
					// 別々にシャッフル
					Collections.shuffle(dir_list);
					Collections.shuffle(file_list);
					Collections.shuffle(all_list);
					// マージ
					mFileList = new ArrayList<FileData>(par_list);
					if (mSortMode == DEF.ZIPSORT_SHUFFLESEP) {
						mFileList.addAll(dir_list);
						mFileList.addAll(file_list);
					}
					else {
						mFileList.addAll(all_list);
					}
					// シャッフルの値を保存
					m3FileList = mFileList;
				}
				else {
					// シャッフルの値を取り出す
					mFileList = m3FileList;
				}
			}
			else {
				Collections.sort(mFileList, new MyComparator());
			}
			// ファイル別ソート
			SoftFileOption(mFileList);
		}
	}

	// リストモード
	public void setParams(boolean hidden, String marker, boolean filter, boolean applydir, boolean parentmove, boolean epubViewer) {
		mHidden = hidden;
		mMarker = marker;
		mFilter = filter;
		mApplyDir = applydir;
		mParentMove = parentmove;
		mEpubViewer = epubViewer;
	}

	public ArrayList<FileData> getFileList() {
		return mFileList;
	}

	public void setFileList(ArrayList<FileData> filelist) {
		mFileList = filelist; 
	}

	public void loadFileList() {
		mDialog = new LoadingDialog(mActivity);
		mDialog.setOnDismissListener(this);
		mDialog.show();

		// サムネイルスレッド開始
		if (mThread == null) {
			mThread = new Thread(this);
			mThread.start();
		}

		// ファイルリスト読み込みのフラグをクリアしておく
		threadstartcheck = false;

		// ファイルリスト読み込みダイアログの表示スレッド開始
		if (mMultiThread == null) {
			mMultiThread = new MultiThread();
			mMultiThread.start();
		}

		return;
	}

	public static void SetReadConfig(SharedPreferences msp, TextManager manager)	{
		mSpaceW = SetTextActivity.getSpaceW(msp);
		mSpaceH = SetTextActivity.getSpaceH(msp);
		mHeadSizeOrg = SetTextActivity.getFontTop(msp);	// 見出し
		mBodySizeOrg = SetTextActivity.getFontBody(msp);	// 本文
		mRubiSizeOrg = SetTextActivity.getFontRubi(msp);	// ルビ
		mInfoSizeOrg = SetTextActivity.getFontInfo(msp);	// ページ情報など
		mMarginWOrg = SetTextActivity.getMarginW(msp);	// 左右余白(設定値)
		mMarginHOrg = SetTextActivity.getMarginH(msp);	// 上下余白(設定値)
		mDensity = mActivity.getResources().getDisplayMetrics().scaledDensity;
		mHeadSize = DEF.calcFontPix(mHeadSizeOrg, mDensity);	// 見出し
		mBodySize = DEF.calcFontPix(mBodySizeOrg, mDensity);	// 本文
		mRubiSize = DEF.calcFontPix(mRubiSizeOrg, mDensity);	// ルビ
		mInfoSize = DEF.calcFontPix(mInfoSizeOrg, mDensity);	// ページ情報など
		mPicSize = SetTextActivity.getPicSize(msp);	// 挿絵サイズ

		mMarginW = DEF.calcDispMargin(mMarginWOrg);				// 左右余白
		mMarginH = mInfoSize + DEF.calcDispMargin(mMarginHOrg);	// 上下余白
		mAscMode = SetTextActivity.getAscMode(msp);
		String fontname = SetTextActivity.getFontName(msp);
		if (fontname != null && fontname.length() > 0) {
			String path = DEF.getFontDirectory();
			mFontFile = path + fontname;
		}
		else {
			mFontFile = null;
		}
		mPaperSel = SetTextActivity.getPaper(msp); // 用紙サイズ
		if (mPaperSel == DEF.PAPERSEL_SCREEN) {
			int cx;
			int cy;
			if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
				Point point = new Point();
				mActivity.getWindowManager().getDefaultDisplay().getRealSize(point);
				cx = point.x;
				cy = point.y;
			}else{
				WindowMetrics wm = mActivity.getWindowManager().getCurrentWindowMetrics();
				cx = wm.getBounds().width();
				cy = wm.getBounds().height();
			}

			if (cx < cy) {
				mTextWidth = cx;
				mTextHeight = cy;
			}
			else {
				mTextWidth = cy;
				mTextHeight = cx;
			}
		}
		else {
			mTextWidth = DEF.PAPERSIZE[mPaperSel][0];
			mTextHeight = DEF.PAPERSIZE[mPaperSel][1];
		}
		manager.formatTextFile(mTextWidth, mTextHeight, mHeadSize, mBodySize, mRubiSize, mSpaceW, mSpaceH, mMarginW, mMarginH, mPicSize, mFontFile, mAscMode);
	}

	public static void ChangeTextSize()
	{
		mChangeTextSize = true;
	}

	public static void FlushFileList()
	{
		mCacheFile = false;
		// リストの内容を更新したらシャッフルの保持を解除
		mKeepShuffle = false;
		mKeepShuffle2 = false;
	}

	private void SetBreakProgressDialogThread() {
		threadstartcheck = true;
	}

	// ファイルリスト読み込みダイアログの表示スレッド
	static class MultiThread extends Thread {
		public void run() {
			try {
				try {
					// 頻繁に表示が行われるのを防ぐため500ミリ秒間待機
					for (int i = 0 ; i < 50 ; i++) {
						if (threadstartcheck) {
							// 500ミリ秒の間に読み込み完了すれば戻る
							// スレッドを終了させて最初から始める
							msendResult();
							return;
						}
						mMultiThread.sleep(10);
					}
				} catch (Exception e) {
				}
				// ファイルリスト読み込みダイアログの表示を準備
				Resources res = mActivity.getResources();
				mProgressDialog = new CustomProgressDialog(res.getString(R.string.loadfilelist), res.getString(R.string.loadingfilelist),true, mHandler);
				supportFragmentManager = mActivity.getSupportFragmentManager();
				// メイン画面で表示させるためハンドラを得る
				mainHandler = new Handler(Looper.getMainLooper());
				// メイン画面で表示
				mainHandler.post(() -> {
					// ダイアログの表示
					mProgressDialog.show(supportFragmentManager, TAG);
					// プログレスバーをリセット
					mProgressDialog.setProgress(0);
				});
				// 読み込み中の位置の変化量をクリア
				old_progress = progress;
				boolean stop = true;
				while (stop) {
					if (threadstartcheck) {
						// ファイルリスト読み込みが完了していれば終了させる
						mainHandler.post(() -> {
							// メイン画面で表示
							mProgressDialog.dismiss();
						});
						// ループ終了
						stop = false;
					}
					else if (old_progress != progress) {
						// 読み込み中の位置が変化したらプログレスバーを更新
						int finalProgress = progress;
						mainHandler.post(() -> {
							// メイン画面で表示
							mProgressDialog.setProgress(finalProgress);
						});
						// 次の位置と比較するため値を保存
						old_progress = progress;
					}
		        }
			} catch  (Exception e) {
				// 処理中断
				mainHandler.post(() -> {
					// メイン画面で表示
					mProgressDialog.dismiss();
				});
			}
			// スレッドを終了させて最初から始める
			msendResult();
 	 	}
	}

	private static void msendResult() {
		// スレッドを終了させて最初から始める
		mMultiThread = null;
	}

	@SuppressLint("SuspiciousIndentation")
    @Override
	public void run() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		boolean debug = false;
		String name;
		int maxpage;
		int state;
		int size;
		long date;
		long nowdate;
		boolean hit;

		Thread thread = mThread;
		boolean hidden = mHidden;
		String marker = mMarker.toUpperCase();
		if (marker.isEmpty()) {
			// 空文字列ならnullにする
			marker = null;
		}
		
		ArrayList<FileData> fileList;
		mFileList = null;

		String currentPath = DEF.relativePath(mActivity, mURI, mPath);
		// ファイルリストのキャッシュを参照しない場合はtrue
		mFileListFastReadOff = SetFileListActivity.GetFileListFastReadOff(mSp);

		try {
			if (!mCacheFile || mFileListFastReadOff) {
				fileList = FileAccess.listFiles(mActivity, currentPath, mUser, mPass, mHandler);

				if (thread.isInterrupted()) {
					// 処理中断
					// ファイルリスト読み込みダイアログの表示を終了させる
					SetBreakProgressDialogThread();
					return;
				}

				if (fileList.isEmpty()) {
					// ファイルがない場合
					Logcat.d(logLevel, "ファイルがありません.");
					fileList = new ArrayList<FileData>();
					String uri = FileAccess.parent(mActivity, mPath);
					FileData fileData;

					if (!uri.isEmpty() && mParentMove) {
						// 親フォルダを表示
						fileData = new FileData(mActivity, "..", DEF.PAGENUMBER_NONE);
						fileList.add(fileData);
					}

					// SDカードフォルダより上のフォルダの場合
					String[] SDCardPath = FileAccess.getExtSdCardPaths(mActivity);
					for (int i = 0; i < SDCardPath.length; ++i) {
						Logcat.i(logLevel, "SD Card Path=" + SDCardPath[i] + ", mURI=" + mURI + ", mPath=" + mPath + ", currentPath=" + currentPath);
						if (SDCardPath[i].startsWith(currentPath) && !SDCardPath[i].equals(currentPath)) {
							int pos = SDCardPath[i].indexOf("/", mPath.length());
							String dir = SDCardPath[i].substring(mPath.length(), pos + 1);

							//途中のフォルダを表示対象に追加
							fileData = new FileData(mActivity, dir, DEF.PAGENUMBER_UNREAD);
							if (!fileList.contains(fileData)) {
								Logcat.i(logLevel, "追加 dir=" + dir);
								fileList.add(fileData);
							}
						}
					}

					// 処理中断
					sendResult(true, thread);
					mFileList = fileList;
					// ファイルリスト読み込みダイアログの表示を終了させる
					SetBreakProgressDialogThread();
					return;
				}

				String uri = FileAccess.parent(mActivity, mPath);
				if (!uri.isEmpty() && mParentMove) {
					FileData fileData = new FileData(mActivity, "..", DEF.PAGENUMBER_NONE);
					fileList.add(0, fileData);
				}
				m2FileList = fileList;
				mCacheFile = true;
			}
			else	{
				fileList = m2FileList;
			}

			String uri = FileAccess.parent(mActivity, mPath);
			int mMargin = ImageActivity.isDualMode();

			for (int i = fileList.size() - 1; i >= 0; i--) {

				// ファイルリスト読み込みダイアログのプログレスバー表示を更新
				progress = 100 - (int)(((float)(i) / (float)fileList.size()) * 100);
				name = fileList.get(i).getName();
				uri = DEF.relativePath(mActivity, currentPath, name);

				if (fileList.get(i).getType() == FileData.FILETYPE_TXT) {
					if (FileAccess.accessType(currentPath) == DEF.ACCESS_TYPE_SAF) {
						// SAFの場合は特例でパスのURLを解決する(これを入れないと値を操作できない)
						uri = currentPath + name;
					}
					else {
						uri = DEF.relativePath(mActivity, currentPath, name);
					}
					maxpage = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", DEF.PAGENUMBER_NONE);
					state = mSp.getInt(DEF.createUrl(uri, mUser, mPass), DEF.PAGENUMBER_UNREAD);
					fileList.get(i).setMaxpage(maxpage);
					if (state >= 0) { // 先頭ページでも動作するようにした
						nowdate = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#date", DEF.PAGENUMBER_UNREAD);
						date = fileList.get(i).getDate();
						if ((nowdate != ((date / 1000))) || (mChangeTextSize)) {
							int openmode = 0;
							// ファイルリストの読み込み
							openmode = ImageManager.OPENMODE_TEXTVIEW;
							mImageMgr = new ImageManager(this.mActivity, currentPath, "", mUser, mPass, 0, mHandler, mHidden, openmode, 1);
							mImageMgr.LoadImageList(0, 0, 0, 0, 0);
							mTextMgr = new TextManager(mImageMgr, name, mUser, mPass, mHandler, mActivity, FileData.FILETYPE_TXT);
							SetReadConfig(mSp, mTextMgr);
							maxpage = mTextMgr.length();
							SharedPreferences.Editor ed = mSp.edit();
							ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", maxpage);
							ed.putInt(DEF.createUrl(uri, mUser, mPass), state);
							ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#date", (int)((date / 1000)));
							ed.apply();
							releaseManager();
							if (maxpage == DEF.PAGENUMBER_NONE) {
								state = DEF.PAGENUMBER_UNREAD;
								size = DEF.PAGENUMBER_NONE;
							} else if (state >= maxpage - mMargin) {
								// 0から始まるので+1、見開きの分で-1
								state = DEF.PAGENUMBER_READ;
								size = maxpage;
							} else {
								size = maxpage;
							}
						}
						else {
							if (maxpage == DEF.PAGENUMBER_NONE) {
								state = DEF.PAGENUMBER_UNREAD;
								size = DEF.PAGENUMBER_NONE;
							} else if (state >= maxpage - mMargin) {
								// 0から始まるので+1、見開きの分で-1
								state = DEF.PAGENUMBER_READ;
								size = maxpage;
							} else {
								size = maxpage;
							}
						}
						fileList.get(i).setMaxpage(size);
					}
					fileList.get(i).setState(state);
				}

				if (fileList.get(i).getType() == FileData.FILETYPE_ARC
						|| fileList.get(i).getType() == FileData.FILETYPE_PDF) {
					maxpage = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", DEF.PAGENUMBER_NONE);
					state = mSp.getInt(DEF.createUrl(uri, mUser, mPass), DEF.PAGENUMBER_UNREAD);
					fileList.get(i).setMaxpage(maxpage);
					if	(state >= 0)	{ // 先頭ページでも動作するようにした
						nowdate = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#date", DEF.PAGENUMBER_UNREAD);
						date = fileList.get(i).getDate();
						if (nowdate != ((date / 1000)))	{
							int openmode = 0;
							// ファイルリストの読み込み
							openmode = ImageManager.OPENMODE_VIEW;
							// 設定の読み込み
							mImageMgr = new ImageManager(this.mActivity, currentPath, name, mUser, mPass, 0, mHandler, mHidden, openmode, 1);
							mImageMgr.LoadImageList(0, 0, 0, 0, 0);
							maxpage = mImageMgr.length();
							SharedPreferences.Editor ed = mSp.edit();
							ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", maxpage);
							ed.putInt(DEF.createUrl(uri, mUser, mPass), state);
							ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#date", (int)((date / 1000)));
							ed.apply();
							releaseManager();
							if (maxpage == DEF.PAGENUMBER_NONE) {
								state = DEF.PAGENUMBER_UNREAD;
								size = DEF.PAGENUMBER_NONE;
							} else if (state >= maxpage - mMargin) {
								// 0から始まるので+1、見開きの分で-1
								state = DEF.PAGENUMBER_READ;
								size = maxpage;
							} else {
								size = maxpage;
							}
						}
						else {
							if (maxpage == DEF.PAGENUMBER_NONE) {
								state = DEF.PAGENUMBER_UNREAD;
								size = DEF.PAGENUMBER_NONE;
							} else if (state >= maxpage - mMargin) {
								// 0から始まるので+1、見開きの分で-1
								state = DEF.PAGENUMBER_READ;
								size = maxpage;
							} else {
								size = maxpage;
							}
						}
						fileList.get(i).setMaxpage(size);
					}
					fileList.get(i).setState(state);
				}
				if (fileList.get(i).getType() == FileData.FILETYPE_EPUB) {
					if (FileAccess.accessType(currentPath) == DEF.ACCESS_TYPE_SAF) {
						// SAFの場合は特例でパスのURLを解決する(これを入れないと値を操作できない)
						uri = currentPath + name;
					}
					else {
						uri = DEF.relativePath(mActivity, currentPath, name);
					}
					if (DEF.TEXT_VIEWER == mEpubViewer) {
						maxpage = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "META-INF/container.xml" + "#maxpage", DEF.PAGENUMBER_NONE);
						state = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "META-INF/container.xml", DEF.PAGENUMBER_UNREAD);
						fileList.get(i).setMaxpage(maxpage);
						if	(state >= 0)	{ // 先頭ページでも動作するようにした
							nowdate = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#date", DEF.PAGENUMBER_UNREAD);
							date = fileList.get(i).getDate();
							if ((nowdate != ((date / 1000))) || (mChangeTextSize))	{
								int openmode = 0;
								// ファイルリストの読み込み
								openmode = ImageManager.OPENMODE_TEXTVIEW;
								Logcat.d(logLevel,"run: mUri + mPath=" + mURI + mPath + ", name=" + name);
								mImageMgr = new ImageManager(this.mActivity, mURI + mPath, name, mUser, mPass, 0, mHandler, mHidden, openmode, 1);
								mImageMgr.LoadImageList(0, 0, 0, 0, 0);
								mTextMgr = new TextManager(mImageMgr, "META-INF/container.xml", mUser, mPass, mHandler, mActivity, FileData.FILETYPE_EPUB);
								SetReadConfig(mSp, mTextMgr);
								maxpage = mTextMgr.length();
								SharedPreferences.Editor ed = mSp.edit();
								ed.putInt(DEF.createUrl(uri, mUser, mPass) + "META-INF/container.xml" + "#maxpage", maxpage);
								ed.putInt(DEF.createUrl(uri, mUser, mPass) + "META-INF/container.xml", state);
								ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#date", (int)((date / 1000)));
								ed.apply();
								releaseManager();
								if (maxpage == DEF.PAGENUMBER_NONE) {
									state = DEF.PAGENUMBER_UNREAD;
									size = DEF.PAGENUMBER_NONE;
								} else if (state >= maxpage - mMargin) {
									// 0から始まるので+1、見開きの分で-1
									state = DEF.PAGENUMBER_READ;
									size = maxpage;
								} else {
									size = maxpage;
								}
							}
							else	{
								if (maxpage == DEF.PAGENUMBER_NONE) {
									state = DEF.PAGENUMBER_UNREAD;
									size = DEF.PAGENUMBER_NONE;
								} else if (state >= maxpage - mMargin) {
									// 0から始まるので+1、見開きの分で-1
									state = DEF.PAGENUMBER_READ;
									size = maxpage;
								} else {
									size = maxpage;
								}
							}
							fileList.get(i).setMaxpage(size);
						}
					}
					else {
						maxpage = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", DEF.PAGENUMBER_NONE);
						state = mSp.getInt(DEF.createUrl(uri, mUser, mPass), DEF.PAGENUMBER_UNREAD);
						fileList.get(i).setMaxpage(maxpage);
						if	(state >= 0)	{ // 先頭ページでも動作するようにした
							nowdate = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#date", DEF.PAGENUMBER_UNREAD);
							date = fileList.get(i).getDate();
							if ((nowdate != ((date / 1000))) || (mChangeTextSize))	{
								int openmode = 0;
								// ファイルリストの読み込み
								openmode = ImageManager.OPENMODE_VIEW;
								mImageMgr = new ImageManager(this.mActivity, currentPath, "", mUser, mPass, 0, mHandler, mHidden, openmode, 1);
								mImageMgr.LoadImageList(0, 0, 0, 0, 0);
								maxpage = mImageMgr.length();
								SharedPreferences.Editor ed = mSp.edit();
								ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", maxpage);
								ed.putInt(DEF.createUrl(uri, mUser, mPass), state);
								ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#date", (int)((date / 1000)));
								ed.apply();
								releaseManager();
								if (maxpage == DEF.PAGENUMBER_NONE) {
									state = DEF.PAGENUMBER_UNREAD;
									size = DEF.PAGENUMBER_NONE;
								} else if (state >= maxpage - mMargin) {
									// 0から始まるので+1、見開きの分で-1
									state = DEF.PAGENUMBER_READ;
									size = maxpage;
								} else {
									size = maxpage;
								}
							}
							else {
								if (maxpage == DEF.PAGENUMBER_NONE) {
									state = DEF.PAGENUMBER_UNREAD;
									size = DEF.PAGENUMBER_NONE;
								} else if (state >= maxpage - mMargin) {
									// 0から始まるので+1、見開きの分で-1
									state = DEF.PAGENUMBER_READ;
									size = maxpage;
								} else {
									size = maxpage;
								}
							}
							fileList.get(i).setMaxpage(size);
						}
					}
					fileList.get(i).setState(state);
				}

				if (fileList.get(i).getType() == FileData.FILETYPE_DIR) {
					maxpage = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", DEF.PAGENUMBER_NONE);
					state = mSp.getInt(DEF.createUrl(uri, mUser, mPass), DEF.PAGENUMBER_UNREAD);
					if	(state >= 0)	{ // 先頭ページでも動作するようにした
						nowdate = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#date", DEF.PAGENUMBER_UNREAD);
						date = fileList.get(i).getDate();
						if (nowdate != ((date / 1000)))	{
							int openmode = 0;
							// ファイルリストの読み込み
							openmode = ImageManager.OPENMODE_VIEW;
							// 設定の読み込み
							mImageMgr = new ImageManager(this.mActivity, currentPath, name, mUser, mPass, 0, mHandler, mHidden, openmode, 1);
							mImageMgr.LoadImageList(0, 0, 0, 0, 0);
							maxpage = mImageMgr.length();
							SharedPreferences.Editor ed = mSp.edit();
							ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", maxpage);
							ed.putInt(DEF.createUrl(uri, mUser, mPass), state);
							ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#date", (int)((date / 1000)));
							ed.apply();
							releaseManager();
							if (maxpage == DEF.PAGENUMBER_NONE) {
								state = DEF.PAGENUMBER_UNREAD;
								size = DEF.PAGENUMBER_NONE;
							} else if (state >= maxpage - mMargin) {
								// 0から始まるので+1、見開きの分で-1
								state = DEF.PAGENUMBER_READ;
								size = maxpage;
							} else {
								size = maxpage;
							}
						}
						else	{
							if (maxpage == DEF.PAGENUMBER_NONE) {
								state = DEF.PAGENUMBER_UNREAD;
								size = DEF.PAGENUMBER_NONE;
							} else if (state >= maxpage - mMargin) {
								// 0から始まるので+1、見開きの分で-1
								state = DEF.PAGENUMBER_READ;
								size = maxpage;
							} else {
								size = maxpage;
							}
						}
						fileList.get(i).setMaxpage(size);
					}
					fileList.get(i).setState(state);
				}

				if (fileList.get(i).getType() == FileData.FILETYPE_IMG){
					state = DEF.PAGENUMBER_NONE;
					fileList.get(i).setState(state);
				}

				if (fileList.get(i).getType() == FileData.FILETYPE_NONE){
					fileList.remove(i);
					continue;
				}
				if (fileList.get(i).getType() == FileData.FILETYPE_EPUB_SUB){
					fileList.remove(i);
					continue;
				}
				if (fileList.get(i).getType() != FileData.FILETYPE_DIR && fileList.get(i).getType() != FileData.FILETYPE_PARENT) {
					// 通常のファイル
					if (hidden && DEF.checkHiddenFile(name)) {
						fileList.remove(i);
						continue;
					}
				}

				hit = false;
				if (marker != null) {
					if (name.toUpperCase().contains(marker)) {
						// 検索文字列が含まれる
						hit = true;
					}
					//フィルタ設定
					if(mFilter){
						if(!hit){
							fileList.remove(i);
							continue;
						}
						//ディレクトリに適用する場合にリスト削除
						if(!mApplyDir){
							if(fileList.get(i).getType() == FileData.FILETYPE_DIR){
								fileList.remove(i);
								continue;
							}
						}
					}
				}
				fileList.get(i).setMarker(hit);

				if (thread.isInterrupted()) {
					// 処理中断
					// ファイルリスト読み込みダイアログの表示を終了させる
					SetBreakProgressDialogThread();
					return;
				}
			}
			mChangeTextSize = false;
			// ファイルリスト読み込みダイアログの表示を終了させる
			SetBreakProgressDialogThread();
		}
		catch (Exception e) {
			Logcat.e(logLevel, "", e);
			sendResult(false, e.toString(), thread);
			// ファイルリスト読み込みダイアログの表示を終了させる
			SetBreakProgressDialogThread();
			return;
		}

		if (thread.isInterrupted()) {
			// 処理中断
			// ファイルリスト読み込みダイアログの表示を終了させる
			SetBreakProgressDialogThread();
			return;
		}

		// sort
		if (mSortMode != 0) {
			// ソートあり設定の場合
			if (mSortMode == DEF.ZIPSORT_SHUFFLESEP || mSortMode == DEF.ZIPSORT_SHUFFLEMGR) {
				if (!mKeepSortShuffle || mKeepSortShuffle && !mKeepShuffle) {
					mKeepShuffle = true;
					// シャッフル
					ArrayList<FileData> par_list = new ArrayList<FileData>();
					ArrayList<FileData> all_list = new ArrayList<FileData>();
					ArrayList<FileData> dir_list = new ArrayList<FileData>();
					ArrayList<FileData> file_list = new ArrayList<FileData>();
					// ディレクトリとファイルを分離
					for (int i = 0; i < fileList.size(); i++) {
						if (fileList.get(i).getType() == FileData.FILETYPE_PARENT) {
							par_list.add(fileList.get(i));
						}
						else if (fileList.get(i).getType() == FileData.FILETYPE_DIR) {
							dir_list.add(fileList.get(i));
							all_list.add(fileList.get(i));
						}
						else {
							file_list.add(fileList.get(i));
							all_list.add(fileList.get(i));
						}
					}
					// 別々にシャッフル
					Collections.shuffle(dir_list);
					Collections.shuffle(file_list);
					Collections.shuffle(all_list);
					// マージ
					fileList = new ArrayList<FileData>(par_list);
					if (mSortMode == DEF.ZIPSORT_SHUFFLESEP) {
						fileList.addAll(dir_list);
						fileList.addAll(file_list);
					}
					else {
						fileList.addAll(all_list);
					}
					// シャッフルの値を保存
					m3FileList = fileList;
				}
				else {
					// シャッフルの値を取り出す
					fileList = m3FileList;
				}
			}
			else {
				Collections.sort(fileList, new MyComparator());
			}
		}
		// ファイル別ソート
		SoftFileOption(fileList);

		if (thread.isInterrupted()) {
			// 処理中断
			// ファイルリスト読み込みダイアログの表示を終了させる
			SetBreakProgressDialogThread();
			return;
		}
		mFileList = fileList;
		sendResult(true, thread);
		// ファイルリスト読み込みダイアログの表示を終了させる
		SetBreakProgressDialogThread();
	}

	public class MyComparator implements Comparator<FileData> {
		public int compare(FileData file1, FileData file2) {

			int result;
			// ディレクトリ/ファイルタイプ
			int type1 = file1.getType();
			int type2 = file2.getType();
			if (type1 == FileData.FILETYPE_PARENT || type2 == FileData.FILETYPE_PARENT) {
				return type1 - type2;
			}
			else if (mSortMode == DEF.ZIPSORT_FILESEP || mSortMode == DEF.ZIPSORT_NEWSEP || mSortMode == DEF.ZIPSORT_OLDSEP) {
				// IMAGEとZIPのソート優先度は同じにする
				if (type1 == FileData.FILETYPE_IMG || type1 == FileData.FILETYPE_TXT || type1 == FileData.FILETYPE_PDF || type1 == FileData.FILETYPE_EPUB) {
					type1 = FileData.FILETYPE_ARC;
				}
				if (type2 == FileData.FILETYPE_IMG || type2 == FileData.FILETYPE_TXT || type2 == FileData.FILETYPE_PDF || type2 == FileData.FILETYPE_EPUB) {
					type2 = FileData.FILETYPE_ARC;
				}

				result = type1 - type2;
				if (result != 0) {
					return result;
				}
			}
			switch (mSortMode) {
				case DEF.ZIPSORT_FILEMGR:
				case DEF.ZIPSORT_FILESEP:
//					return file1.getName().toUpperCase().compareTo(file2.getName().toUpperCase());
					return DEF.compareFileName(file1.getName(), file2.getName());
				case DEF.ZIPSORT_NEWMGR:
				case DEF.ZIPSORT_NEWSEP:
				{
					long val = file2.getDate() - file1.getDate();
					return val == 0 ? 0 : (val > 0 ? 1 : -1);
				}
				case DEF.ZIPSORT_OLDMGR:
				case DEF.ZIPSORT_OLDSEP:
				{
					long val = file1.getDate() - file2.getDate();
					return val == 0 ? 0 : (val > 0 ? 1 : -1);
				}
			}
			return 0;
		}
	}

	// ファイル別ソートの比較
	public static class SpecificComparator implements Comparator<FileData> {
		public int compare(FileData file1, FileData file2) {

			int result;
			// ディレクトリ/ファイルタイプ
			int type1 = file1.getType();
			int type2 = file2.getType();
			// 親ディレクトリは一番にする
			if (type1 == FileData.FILETYPE_PARENT || type2 == FileData.FILETYPE_PARENT) {
				return type1 - type2;
			}
			else if (mSortMode == DEF.ZIPSORT_FILESEP || mSortMode == DEF.ZIPSORT_NEWSEP || mSortMode == DEF.ZIPSORT_OLDSEP) {
				// IMAGEとZIPのソート優先度は同じにする
				if (type1 == FileData.FILETYPE_IMG || type1 == FileData.FILETYPE_TXT || type1 == FileData.FILETYPE_PDF || type1 == FileData.FILETYPE_EPUB) {
					type1 = FileData.FILETYPE_ARC;
				}
				if (type2 == FileData.FILETYPE_IMG || type2 == FileData.FILETYPE_TXT || type2 == FileData.FILETYPE_PDF || type2 == FileData.FILETYPE_EPUB) {
					type2 = FileData.FILETYPE_ARC;
				}

				result = type1 - type2;
				if (result != 0) {
					return result;
				}
			}
			// ここまで同じ処理にする

			type1 = file1.getType();
			type2 = file2.getType();
			// ディレクトリを先頭に集める場合
			if (mSoftDirTop && (type1 == FileData.FILETYPE_DIR || type2 == FileData.FILETYPE_DIR)) {
				return type1 - type2;
			}
			else {
				// 該当ファイルを先頭に集める場合
				if (mSoftFileTop == 1 && type1 == FileData.FILETYPE_DIR || mSoftFileTop == 2 && type1 == FileData.FILETYPE_ARC || mSoftFileTop == 3 && type1 == FileData.FILETYPE_IMG || mSoftFileTop == 4 && type1 == FileData.FILETYPE_PDF || mSoftFileTop == 5 && type1 == FileData.FILETYPE_EPUB || mSoftFileTop == 6 && type1 == FileData.FILETYPE_TXT || mSoftFileTop == 7 && type1 != FileData.FILETYPE_DIR && type1 != FileData.FILETYPE_ARC && type1 != FileData.FILETYPE_IMG && type1 != FileData.FILETYPE_PDF && type1 != FileData.FILETYPE_TXT && type1 != FileData.FILETYPE_EPUB) {
					// 該当すれば親ディレクトリの次にする
					type1 = FileData.FILETYPE_SORT;
				}
				if (mSoftFileTop == 1 && type2 == FileData.FILETYPE_DIR || mSoftFileTop == 2 && type2 == FileData.FILETYPE_ARC || mSoftFileTop == 3 && type2 == FileData.FILETYPE_IMG || mSoftFileTop == 4 && type2 == FileData.FILETYPE_PDF || mSoftFileTop == 5 && type2 == FileData.FILETYPE_EPUB || mSoftFileTop == 6 && type2 == FileData.FILETYPE_TXT || mSoftFileTop == 7 && type2 != FileData.FILETYPE_DIR && type2 != FileData.FILETYPE_ARC && type2 != FileData.FILETYPE_IMG && type2 != FileData.FILETYPE_PDF && type2 != FileData.FILETYPE_TXT && type2 != FileData.FILETYPE_EPUB) {
					// 該当すれば親ディレクトリの次にする
					type2 = FileData.FILETYPE_SORT;
				}
				result = type1 - type2;
				if (result != 0) {
					// 比較対象があれば戻る
					return result;
				}
			}

			type1 = file1.getType();
			type2 = file2.getType();

			mSpecificSoftMode = -1;

			if (mSoftDirFile != 0 && (type1 == FileData.FILETYPE_DIR || type2 == FileData.FILETYPE_DIR)) {
				// ディレクトリだった場合
				mSpecificSoftMode = mSoftDirFile;
			}
			else if (mSoftCompFile != 0 && (type1 == FileData.FILETYPE_ARC || type2 == FileData.FILETYPE_ARC)) {
				// 圧縮ファイルだった場合
				mSpecificSoftMode = mSoftCompFile;
			}
			else if (mSoftImageFile != 0 && (type1 == FileData.FILETYPE_IMG || type2 == FileData.FILETYPE_IMG)) {
				// 画像ファイルだった場合
				mSpecificSoftMode = mSoftImageFile;
			}
			else if (mSoftPdfFile != 0 && (type1 == FileData.FILETYPE_PDF || type2 == FileData.FILETYPE_PDF)) {
				// PDFファイルだった場合
				mSpecificSoftMode = mSoftPdfFile;
			}
			else if (mSoftEpubFile != 0 && (type1 == FileData.FILETYPE_EPUB || type2 == FileData.FILETYPE_EPUB)) {
				// EPUBだった場合
				mSpecificSoftMode = mSoftEpubFile;
			}
			else if (mSoftTextFile != 0 && (type1 == FileData.FILETYPE_TXT || type2 == FileData.FILETYPE_TXT)) {
				// テキストファイルだった場合
				mSpecificSoftMode = mSoftTextFile;
			}
			else if (mSoftOtherFile != 0 && (type1 != FileData.FILETYPE_DIR && type2 != FileData.FILETYPE_DIR && type1 != FileData.FILETYPE_ARC && type2 != FileData.FILETYPE_ARC && type1 != FileData.FILETYPE_IMG && type2 != FileData.FILETYPE_IMG && type1 != FileData.FILETYPE_PDF && type2 != FileData.FILETYPE_PDF && type1 != FileData.FILETYPE_TXT && type2 != FileData.FILETYPE_TXT && type1 != FileData.FILETYPE_EPUB && type2 != FileData.FILETYPE_EPUB)) {
				// その他のファイルだった場合
				mSpecificSoftMode = mSoftOtherFile;
			}
			float val1 = 0;
			float val2 = 0;

			switch (mSpecificSoftMode) {
				case DEF.SPECIFICSORT_FILEACSEND:
					// ファイル名(昇順)
					return DEF.compareFileName(file1.getName(), file2.getName());
				case DEF.SPECIFICSORT_FILEDESSEND:
					// ファイル名(降順)
					return DEF.compareFileName(file2.getName(), file1.getName());
				case DEF.SPECIFICSORT_NEWDATE:
				{
					// 新しい順
					long val = file2.getDate() - file1.getDate();
					return val == 0 ? 0 : (val > 0 ? 1 : -1);
				}
				case DEF.SPECIFICSORT_OLDDATE:
				{
					// 古い順
					long val = file1.getDate() - file2.getDate();
					return val == 0 ? 0 : (val > 0 ? 1 : -1);
				}
				case DEF.SPECIFICSORT_SHUFFLE:
				{
					// 乱数をシャッフルの要素に割り当てる
					int random1 = random_data1[mRandowCount % 10000];
					int random2 = random_data2[mRandowCount % 10000];
					mRandowCount++;
					int val = random2 - random1;
					return val == 0 ? 0 : (val > 0 ? 1 : -1);
				}
				case DEF.SPECIFICSORT_READINGHIGH:
				{
					// 読書中の割合が多い順
					val1 = 0;
					if (file1.getState() >= 0 && file1.getMaxpage() > 0) {
						if (file1.getState() >= file1.getMaxpage() - ImageActivity.isDualMode()) {
							val1 = 100;
						}
						else {
							val1 = (float)(file1.getState() + 1) / (float)file1.getMaxpage();
						}
					}
					val2 = 0;
					if (file2.getState() >= 0 && file2.getMaxpage() > 0) {
						if (file2.getState() >= file2.getMaxpage() - ImageActivity.isDualMode()) {
							val2 = 100;
						}
						else {
							val2 = (float)(file2.getState() + 1) / (float)file2.getMaxpage();
						}
					}
					float val = val2 - val1;
					return val == 0 ? 0 : (val > 0 ? 1 : -1);
				}
				case DEF.SPECIFICSORT_READINGLOW:
				{
					// 読書中の割合が少ない順
					val1 = 100;
					if (file1.getState() >= 0 && file1.getMaxpage() > 0) {
						if (file1.getState() >= file1.getMaxpage() - ImageActivity.isDualMode()) {
						}
						else {
							val1 = (float)(file1.getState() + 1) / (float)file1.getMaxpage();
						}
					}
					val2 = 100;
					if (file2.getState() >= 0 && file2.getMaxpage() > 0) {
						if (file2.getState() >= file2.getMaxpage() - ImageActivity.isDualMode()) {
						}
						else {
							val2 = (float)(file2.getState() + 1) / (float)file2.getMaxpage();
						}
					}
					float val = val1 - val2;
					return val == 0 ? 0 : (val > 0 ? 1 : -1);
				}
			}
			return 0;
		}
	}

	
	private void sendResult(boolean result, Thread thread) {
		Resources res = mActivity.getResources();
		sendResult(result, result ? null : res.getString(R.string.cancelloadfilelist), thread);
	}

	private void sendResult(boolean result, String str, Thread thread) {
		if (mThread != null) {
			if (mThread == thread) {
				if (!result) {
					mFileList = new ArrayList<FileData>();
					if (mParentMove) {
						String uri = FileAccess.parent(mActivity, mPath);
    					FileData fileData = new FileData(mActivity, "..", DEF.PAGENUMBER_NONE);
    					mFileList.add(fileData);
					}
				}

				Message message;
				message = new Message();
				message.what = DEF.HMSG_LOADFILELIST;
				message.arg1 = result ? 1 : 0;
				mActivityHandler.sendMessage(message);

				message = new Message();
				message.what = DEF.HMSG_LOADFILELIST;
				message.arg1 = result ? 1 : 0;
				message.obj = str;
				mHandler.sendMessage(message);
			}
			mThread = null;
		}
	}

	public void closeDialog() {
		if (mDialog != null) {
			try {
				mDialog.dismiss();
			}
			catch (IllegalArgumentException e) {
				;
			}
			mDialog = null;
		}
	}

	@Override
	public void onDismiss(DialogInterface di) {
		// 閉じる
		if (mDialog != null) {
			mDialog = null;
			// 割り込み
			if (mThread != null) {
				mThread.interrupt();

				// キャンセル時のみ
				sendResult(false, mThread);
			}
			if (mMultiThread != null) {
				mMultiThread.interrupt();
				msendResult();
			}
		}
	}

	@Override
	public boolean handleMessage(Message msg) {

		if (msg.what == DEF.HMSG_WORKSTREAM) {
			// ファイルアクセスの表示
			return true;
		}
		if (msg.what == DEF.HMSG_PROGRESS_CANCEL) {
			// 割り込み
			if (mThread != null) {
				mThread.interrupt();
				// キャンセル時のみ
				sendResult(false, mThread);
			}
			return true;
		}

		// ファイルアクセス以外なら終了
		closeDialog();

		if (msg.obj != null) {
			Toast.makeText(mActivity, (String)msg.obj, Toast.LENGTH_LONG).show();
		}
		return true;
	}

	public static void ExitmMultiThread() {
		if (mMultiThread != null) {
			mMultiThread.interrupt();
			msendResult();
		}
	}

	// ImageManager と TextManager を解放する
	private void releaseManager() {
		// 読み込み終了
		if (mImageMgr != null) {
			try {
				mImageMgr.close();
			} catch (IOException e) {
				;
			}
			mImageMgr = null;
		}
		if (mTextMgr != null) {
			mTextMgr.release();
			mTextMgr = null;
		}
	}
}
