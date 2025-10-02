package src.comitton.imageview;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.Logcat;
import src.comitton.cropimageview.CropImageActivity;
import src.comitton.helpview.HelpActivity;
import src.comitton.common.DEF;
import src.comitton.config.SetCacheActivity;
import src.comitton.config.SetCommonActivity;
import src.comitton.config.SetConfigActivity;
import src.comitton.config.SetEpubActivity;
import src.comitton.config.SetImageActivity;
import src.comitton.config.SetMarginCutActivity;
import src.comitton.config.SetImageDetailActivity;
import src.comitton.config.SetImageText;
import src.comitton.config.SetImageTextColorActivity;
import src.comitton.config.SetImageTextDetailActivity;
import src.comitton.config.SetNoiseActivity;
import src.comitton.config.SetHardwareImageViewerKeyActivity;
import src.comitton.fileaccess.FileAccess;
import src.comitton.fileview.data.RecordItem;
import src.comitton.dialog.BookmarkDialog;
import src.comitton.dialog.CheckDialog;
import src.comitton.dialog.CloseDialog;
import src.comitton.dialog.DirTreeDialog;
import src.comitton.dialog.ImageConfigDialog;
import src.comitton.dialog.Information;
import src.comitton.dialog.ListDialog;
import src.comitton.dialog.ToolbarEditDialog;
import src.comitton.dialog.PageSelectDialog;
import src.comitton.dialog.PageThumbnail;
import src.comitton.dialog.BookmarkDialog.BookmarkListenerInterface;
import src.comitton.dialog.CheckDialog.CheckListener;
import src.comitton.dialog.CloseDialog.CloseListenerInterface;
import src.comitton.dialog.ListDialog.ListSelectListener;
import src.comitton.dialog.MenuDialog.MenuSelectListener;
import src.comitton.dialog.ImageConfigDialog.ImageConfigListenerInterface;
import src.comitton.dialog.TabDialogFragment;
import src.comitton.dialog.TextInputDialog;
import src.comitton.dialog.CustomProgressDialog;
import src.comitton.fileview.filelist.RecordList;
import src.comitton.fileview.FileSelectActivity;
import src.comitton.noise.NoiseSwitch;
import src.comitton.common.GuideView;

import android.annotation.SuppressLint;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import androidx.preference.PreferenceManager;

import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.FrameLayout;
import android.widget.Toast;

import src.comitton.common.ImageAccess;
import src.comitton.config.SetFileListActivity;
import src.comitton.fileview.data.FileData;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import src.comitton.common.ThumbnailLoader;

/**
 * 画像のスクロールを試すための画面を表します。
 */
@SuppressLint("NewApi")
public class ImageActivity extends AppCompatActivity implements  GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, Handler.Callback, MenuSelectListener, PageSelectListener, BookmarkListenerInterface {
	private static final String TAG = "ImageActivity";

	public static final int FILESORT_NONE = 0;
	public static final int FILESORT_NAME_UP = 1;
	public static final int FILESORT_NAME_DOWN = 2;

	private static final int HALFPOS_1ST = 1;
	private static final int HALFPOS_2ND = 2;

	private static final int TIME_VIB_TERM = 20;
	private static final int TIME_VIB_RANGE = 30;

	private static final int[] CTL_COUNT = { 1, 1, 2, 99999 }; // 対象のページ数
	private static final int[] CTL_RANGE = { 2, 4, 3, 1 }; // 1ページ選択に必要な移動幅(単位)

	private static final int NOISE_NEXTPAGE = 1;
	private static final int NOISE_PREVPAGE = 2;
	private static final int NOISE_NEXTSCRL = 3;
	private static final int NOISE_PREVSCRL = 4;

	private static final int PAGE_SLIDE = 0;
	private static final int PAGE_INPUT = 1;
	private static final int PAGE_THUMB = 2;

	// 上下の操作領域タッチ後何msでボタンを表示するか
	private static final int LONGTAP_TIMER_UI = 400;
	private static final int LONGTAP_TIMER_BTM = 400;

	private static final int LIST_PROFILE1 = 26;
	private static final int LIST_PROFILE2 = 27;
	private static final int LIST_PROFILE3 = 28;
	private static final int LIST_PROFILE4 = 29;
	private static final int LIST_PROFILE5 = 30;

	private static final int CLICKGUARD = 32;

	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;

	// 古い設定ファイルとの互換性維持のための番号
	// ここに追加や削除する場合は番号を変更しないこと
	private final int[] COMMAND_INDEX =
	{
			0,	// 画面方向
			1,	// 余白削除
			21,	// 余白削除の色
			22,	// 画像表示設定
			2,	// 見開き設定
			3,	// 画像サイズ
			4,	// 音操作
			5,	// オートプレイ開始
			6,	// ブックマーク追加
			7,	// ブックマーク選択
			8,	// シャープ化
			23,	// 明るさ補正
			24,	// ガンマ補正
			32,
			33,
			34,
			25,	// バックライト
			9,	// 白黒反転
			10,	// グレースケール
			35,	// 自動着色
			11,	// 画像回転
			12,	// 画像補間方式
			13, // ページ逆順
			14,	// 操作入れ替え
			15,	// 表紙方向
			16,	// スクロール方向入れ替え
			17,	// 上部メニュー設定
			18,	// 設定
			19,	// 中央余白表示
			20,	// 中央影表示,
			31,
			LIST_PROFILE1,	// プロファイル1
			LIST_PROFILE2,	// プロファイル2
			LIST_PROFILE3,	// プロファイル3
			LIST_PROFILE4,	// プロファイル4
			LIST_PROFILE5	// プロファイル5
	};

	private final int[] COMMAND_ID =
	{
		DEF.MENU_ROTATE,	// 画面方向
		DEF.MENU_MGNCUT,	// 余白削除
		DEF.MENU_MGNCUTCOLOR,	// 余白削除の色
		DEF.MENU_IMGCONF,	// 画像表示設定
		DEF.MENU_IMGVIEW,	// 見開き設定
		DEF.MENU_IMGSIZE,	// 画像サイズ
		DEF.MENU_NOISE,		// 音操作
		DEF.MENU_AUTOPLAY,	// オートプレイ開始
		DEF.MENU_ADDBOOKMARK,// ブックマーク追加
		DEF.MENU_SELBOOKMARK,// ブックマーク選択
		DEF.MENU_SHARPEN,	// シャープ化
		DEF.MENU_BRIGHT,	// 明るさ補正
		DEF.MENU_GAMMA,		// ガンマ補正
		DEF.MENU_CONTRAST,	// コントラスト
		DEF.MENU_HUE,		// 色相
		DEF.MENU_SATURATION,	// 彩度
		DEF.MENU_BKLIGHT,	// バックライト
		DEF.MENU_INVERT,	// 白黒反転
		DEF.MENU_GRAY,		// グレースケール
		DEF.MENU_COLORING,	// 自動着色
		DEF.MENU_IMGROTA,	// 画像回転
		DEF.MENU_IMGALGO,	// 画像補間方式
		DEF.MENU_REVERSE, 	// ページ逆順
		DEF.MENU_CHG_OPE,	// 操作入れ替え
		DEF.MENU_PAGEWAY,	// 表紙方向
		DEF.MENU_SCRLWAY,	// スクロール方向入れ替え
		DEF.MENU_TOP_SETTING,// 上部メニュー設定
		DEF.MENU_SETTING,	// 設定
		DEF.MENU_CMARGIN,	// 中央余白表示
		DEF.MENU_CSHADOW,	// 中央影表示
		DEF.MENU_DISPLAY_POSITION,	// 画面の表示位置
		DEF.MENU_PROFILE1,	// プロファイル1
		DEF.MENU_PROFILE2,	// プロファイル2
		DEF.MENU_PROFILE3,	// プロファイル3
		DEF.MENU_PROFILE4,	// プロファイル4
		DEF.MENU_PROFILE5	// プロファイル5
	};
	private final int[] COMMAND_RES =
	{
		R.string.rotateMenu,	// 画面方向
		R.string.mgnCutMenu,	// 余白削除
		R.string.mgnCutColorMenu,	// 余白削除の色
		R.string.imgConfMenu,	// 画像表示設定
		R.string.tguide02,		// 見開き設定
		R.string.tguide03,		// 画像サイズ
		R.string.noiseMenu,		// 音操作
		R.string.playMenu,		// オートプレイ開始
		R.string.addBookmarkMenu,// ブックマーク追加
		R.string.selBookmarkMenu,// ブックマーク選択
		R.string.sharpenMenu,	// シャープ化
		R.string.brightMenu,	// 明るさ補正
		R.string.gammaMenu,		// ガンマ補正
		R.string.contrastMenu,	// コントラスト
		R.string.hueMenu,		// 色相
		R.string.saturationMenu,	// 彩度
		R.string.bklightMenu,	// バックライト
		R.string.invertMenu,	// 白黒反転
		R.string.grayMenu,		// グレースケール
		R.string.coloringMenu,	// 自動着色
		R.string.imgRotaMenu,	// 画像回転
		R.string.algoriMenu,	// 画像補間方式
		R.string.reverseMenu,	// ページ逆順
		R.string.chgOpeMenu,	// 操作入れ替え
		R.string.pageWayMenu,	// 表紙方向
		R.string.scrlWay2Menu,	// スクロール方向入れ替え
		R.string.setTopMenu,	// 上部メニュー設定
		R.string.setMenu,		// 設定
		R.string.cMargin,		// 中央余白表示
		R.string.cShadow,		// 中央影表示
		R.string.DisplayPositionMenu,		// 画面の表示位置
		R.string.Profile1,		// プロファイル1
		R.string.Profile2,		// プロファイル2
		R.string.Profile3,		// プロファイル3
		R.string.Profile4,		// プロファイル4
		R.string.Profile5		// プロファイル5
	};
	private int[] mCommandId;
	private String[] mCommandStr;
	private String[] mProfileWord;

	private int RANGE_FLICK;

	// public static final int FLICK_NONE = 0;
	// public static final int FLICK_RIGHTNEXT = 1;
	// public static final int FLICK_LEFTNEXT = 2;

	private final int SELLIST_ALGORITHM = 0;
	private final int SELLIST_IMG_ROTATE = 1;
	private final int SELLIST_VIEW_MODE = 2;
	private final int SELLIST_SCALE_MODE = 3;
	private final int SELLIST_MARGIN_CUT = 4;
	private final int SELLIST_MARGIN_CUTCOLOR = 5;
	private final int SELLIST_SCR_ROTATE = 6;
	private final int SELLIST_SETPROFILE = 7;
	private final int SELLIST_DELPROFILE = 8;
	private final int SELLIST_DISPLAY_POSITION = 9;

	private final int TOUCH_NONE      = 0;
	private final int TOUCH_COMMAND   = 1;
	private final int TOUCH_OPERATION = 2;

	private final int[] SCALENAME_ORDER = { 0, 1, 6, 2, 3, 7, 4, 5 };

	// 設定値の保持
	private int mClickArea = 16;
	private boolean mClickGuard = false;
	private int mPageRange = 16;
	private int mScroll = 5;
	private int mMoveRange = 12;
	private int mLongTapZoom = 800; // 長押し時間
	private int mDoubleTap = 300; // ダブルタップ時間
	private int mCenter = 0;
	private int mShadow = 0;
	private int mFileSort = FILESORT_NAME_UP;
	// private int mOrgWidth = DEF.DEFAULT_ORGWIDTH;
	// private int mOrgHeight = DEF.DEFAULT_ORGHEIGHT;
	private int mViewPoint;
	private int mMargin;
	private int mMgnColor;
	private int mCenColor;
	private int mTopColor1;
	private int mTopColor2;
	private int mZoomType = 0; // 拡大方法
	private int mPageWay = DEF.PAGEWAY_RIGHT;
	private int mMemSize;
	private int mMemNext;
	private int mMemPrev;
	private int mMemCache;
	private int mVolKeyMode;
	private int mViewRota;
	private int mRotateBtn;
	private int mVolScrl;
	private int mScrlWay;
	private int mScrlRngW;
	private int mScrlRngH;
	private int mMgnCut;
	private int mMgnCutColor;
	private boolean mMgnBlkMsk = false;
	private int mMarginLevel;
	private int mMarginSpace;
	private int mMarginRange;
	private int mMarginStart;
	private int mMarginLimit;
	private boolean mMarginAspectMask = false;
	private boolean mMarginForceIgnoreAspect = false;
	private int mDisplayPosition;
	private int mEffect;
	private int mLastMsg;
	private int mPageSelect;
	private int mMomentMode;
	private int mSharpen;
	private int mBright;
	private int mGamma;
	private int mBkLight;
	private int mContrast;
	private int mHue;
	private int mSaturation;
	private int mMaxThread;
	private boolean mOldMenu;
	private int mLoupeSize;

	private boolean mGrayBackup;
	private boolean mColoringBackup;
	private boolean mInvertBackup;
	private boolean mMoireBackup;
	private int mSharpenBackup;
	private int mBrightBackup;
	private int mGammaBackup;
	private int mContrastBackup;
	private int mHueBackup;
	private int mSaturationBackup;
	private int mRotateBackup;
	private boolean mReverseOrderBackup;
	private boolean mChgPageBackup;
	private int mPageWayBackup;
	private int mScrlWayBackup;
	private boolean mTopSingleBackup;
	private int mBkLightBackup;
	private int mAlgoModeBackup;
	private int mDispModeBackup;
	private int mScaleModeBackup;
	private int mMgnCutBackup;
	private int mMgnCutColorBackup;
	private int mPinchScaleBackup;
	private int mDisplayPositionBackup;

	private boolean mHidden;
	private boolean mDelShare;
	private boolean mFlickPage;
	private boolean mReverseOrder;
	private boolean mReverseOrderProfile;

	private int mNoiseScrl;
	private int mNoiseUnder;
	private int mNoiseOver;
	private int mNoiseDec;
	private boolean mNoiseLevel;

	private int mViewWidth;
	private int mViewHeight;

	private boolean mNotice = false;
	private boolean mForceNotice = false;
	private boolean mNoSleep = false;
	private static boolean mViewPause = false;
	private static boolean mViewUpdate = false;
	private boolean mChgPage = false;
	private boolean mChgPageProfile = false;
	private boolean mChgFlick = false;
	// private boolean mTwice = false;
	// private boolean mResumeOpen;
	private boolean mReturnListView;
	private boolean mConfirmBack;
	private boolean mFitDual = true;
	private boolean mCMargin = false;
	private boolean mCShadow = false;
	private boolean mPrevRev = false;
	private boolean mNoExpand = true;
	private boolean mVibFlag = false;
	private boolean mPseLand = false;
	// private boolean mHalfHeight = false;
	// private boolean mScaleBmp = false;
	// private boolean mBackMode = false;
	private boolean mAccessLamp = true;
	private boolean mTapScrl = false;
	private boolean mInvert;
	private boolean mGray;
	private boolean mColoring;
	private boolean mMoire;
	private boolean mTopSingle;
	private boolean mSavePage;
	private boolean mFlickEdge;
	private boolean mIsConfSave;
	private boolean mScrlNext = false; // スクロールで前後のページへ移動
	private boolean mViewNext = false; // 次のページを表示
	private boolean mNextFilter = true;
	private boolean mChgPageKey = false;
	private boolean mEpubOrder = false;
	private boolean mEpubThumb = false;

	// ファイル情報
	/** 選択したサーバのインデックス */
	private int mServer;
	/** URI(サーバのTOPディレクトリ) */
	private String mURI = "";
	private String mUser = "";
	private String mPass = "";
	/** ベースURIからの相対パス名 */
	private String mPath = "";
	/** ZIP指定時 */
	private String mFileName = "";
	/** 画像直接指定時はファイル/ExpandActivityから開いた時はZIP内部のファイル */
	private String mImageName = "";
	/** URIとパス */
	private String mUriPath = "";
	/** URIとパスと圧縮ファイル */
	private String mFilePath = "";
	/** ファイルのタイムスタンプ */
	private long mTimestamp = 0L;

	private ImageManager mImageMgr = null;

	// ページ表示のステータス情報
	private int mRestoreMaxPage;
	private int mRestorePage;
	private int mCurrentPage;
	private int mNextPage;
	private boolean mPageSelecting = true;
	private int mSelectPage = 0;
	private boolean mCurrentPageHalf = false;
	private boolean mCurrentPageDual = false;
	private int mHalfPos = HALFPOS_1ST;
	private boolean mPageBack = false;
	private int mRotate = 0; // 回転角度(0～359)
	private int mInitFlg = 0; // 初期表示の制御用フラグ

	// 画像の表示制御情報
	private int mScaleMode;
	private int mDispMode;
	private int mAlgoMode;

	// 表示文言
	private String mLoadErrStr;

	// 画面を構成する View の保持
	private static MyImageView mImageView = null;
	private GuideView mGuideView = null;
	private boolean mKeepGuide = false;
	// フリック判定用
	// private long mInTime1;
	// private long mInTime2;
	// private long mTouchTime;
	// private Point mInPoint1 = new Point();
	// private Point mInPoint2 = new Point();

	// 画面タッチの制御
	private float mTouchBeginX; // 開始x座標
	private float mTouchBeginY; // 開始y座標
	// private float mTouchFirstX; // 開始x座標
	private int mTouchDrawLeft;
	private int mOperation; 	// 操作種別
	private boolean mTouchFirst = false; // タッチ開始後リミットを超えて移動していない
	private boolean mTouchMove = false;
	private boolean mPageMode = false; // ページ選択中の操作エリア外フラグ
	private boolean mPageModeIn = false; // ページ選択中の操作エリア外フラグ
	private boolean mTopMode = false; // トップ操作モード
	private boolean mPinchOn = false;
	private boolean mPinchDown = false;
	private int mPinchScale;
	private int mPinchScaleSel;
	private int mPinchCount;
	private long mPinchTime;
	private int mPinchRange;
	private int mTapPattern;
	private int mTapRate;

	private final int MAX_TOUCHPOINT = 4;
	private final int TERM_MOMENT = 200;
	private int mTouchPointNum;
	private PointF[] mTouchPoint;
	private long[] mTouchPointTime;

	private boolean mPnumDisp;
	private int mPnumFormat;
	private int mPnumPos;
	private int mPnumSize;
	private int mPnumColor;

	private boolean mTimeDisp;
	private int mTimeFormat;
	private int mTimePos;
	private int mTimeSize;
	private int mTimeColor;

	// サムネイルページ選択用
	private long mThumID;

	// ビットマップの保持
	private final ImageData[] mSourceImage = { null, null };
	// private boolean mIsLandscape = false;

	private long mPrevVibTime = 0;
	// private long mPrevScrollTime = 0;

	// ビットマップ読み込みスレッドの制御用
	private Handler mHandler;
	private BmpLoad mBmpLoad;
	private Thread mBmpThread;
	private boolean mBitmapLoading = false;
	private boolean mLoadingNext = false;

	private ZipLoad mZipLoad;
	private Thread mZipThread;

	// long touch timer
	private boolean mLongTouchMode = false;
	private int mLongTouchCount = 0;

	private int mWAdjust = 100;
	private int mWidthScale = 100;
	private int mImgScale = 100;

	private Vibrator mVibrator;

	private boolean mTerminate = false;
	private boolean mListLoading = false; //
	private boolean mReadBreak;
	private boolean mFinishActivity;

	private Information mInformation;
	private String[] mReadingMsg;
	private Message mReadTimerMsg;
	private static CustomProgressDialog mProgressDialog;
	private static FragmentManager supportFragmentManager;

	private NoiseSwitch mNoiseSwitch = null;
	private int mNoiseScroll = 0;
	private boolean mScrolling = false;

	private boolean mAutoPlay;
	private int mAutoPlayTerm = 1000;

	private float mEffectRate = 0;
	private long mEffectStart = 0;
	private int mEffectTime;
	private boolean mImmEnable;
	private boolean mImmForce;
	private boolean mBottomFile;
	private boolean mPinchEnable;
	private long mActionMoveSkipStartTime;

	private ImageActivity mActivity;
	private SharedPreferences mSharedPreferences;
	private float mSDensity;
	private int mImmCancelRange;
	private boolean mImmCancel;

	private boolean mTapEditMode = false;

	private ImageConfigDialog mImageConfigDialog;
	private CloseDialog mCloseDialog;
	private ListDialog mListDialog;
	private CheckDialog mCheckDialog;
	private TabDialogFragment mMenuDialog;
	private TextInputDialog mTextInputDialog;

	private PageSelectDialog mPageDlg = null;
	private PageThumbnail mThumbDlg = null;

	private GestureDetectorCompat mDetector;
	private boolean mDoubleTapMode = false;
	private boolean mAutoRepeatCheck = false;
	private boolean mPinchScaleSetting = false;

	private int mResult = 0;
	private Context context;
	private Insets insets;
	private boolean mHideNavigationBar = false;

	/**
	 * 画面が作成された時に発生します。
	 *
	 * @param savedInstanceState
	 *            保存されたインスタンスの状態。
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.i(logLevel, "開始します.");

		// 起動処理失敗回数をリセット
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor ed = mSharedPreferences.edit();
		ed.putInt(DEF.KEY_INITIALIZE, 0);
		ed.apply();

		// 回転
		mInitFlg = 0;
		mDispMode = DEF.DISPMODE_IM_NORMAL;
		mBitmapLoading = false;
		mLoadingNext = false;
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		mScaleMode = DEF.SCALE_ORIGINAL;
		mReverseOrder = false;
		mTerminate = false;
		mListLoading = false;
		mHandler = new Handler(this);
		mActivity = this;
		mNextPage = -1;
		mIsConfSave = true;
		mNoiseSwitch = new NoiseSwitch(mHandler);

		// ダイアログは初期化
		PageThumbnail.mIsOpened = false;
		PageSelectDialog.mIsOpened = false;

		// 慣性スクロール用領域初期化
		mTouchPointNum = 0;
		mTouchPoint = new PointF[MAX_TOUCHPOINT];
		mTouchPointTime = new long[MAX_TOUCHPOINT];
		for (int i = 0; i < MAX_TOUCHPOINT; i++) {
			mTouchPoint[i] = new PointF();
		}
		mImmCancelRange = (int)(getResources().getDisplayMetrics().density * 32);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			// ステータスバーとナビゲーションバーの高さを求めるための準備を行う
			WindowMetrics windowMetrics = this.getWindowManager().getCurrentWindowMetrics();
			insets = windowMetrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
		}
		mSDensity = getResources().getDisplayMetrics().scaledDensity;
		RANGE_FLICK = (int) (50 * mSDensity);

		super.onCreate(savedInstanceState);

		// タイトル非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 設定の読み込み
		SetCommonActivity.loadSettings(mSharedPreferences);
		ReadSetting(mSharedPreferences);
		if (mNotice || mForceNotice) {
			// 通知領域非表示
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		if ((mImmEnable || mImmForce) && mSdkVersion >= 19) {
            int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
                uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                getWindow().getDecorView().setSystemUiVisibility(uiOptions);
		}
		if (mNoSleep) {
			// スリープしない
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		if (FileSelectActivity.GetRecordSw()) {
			// マイク開始
			mNoiseSwitch.recordStart();
		}

		Resources res = getResources();
		mLoadErrStr = res.getString(R.string.loaderr);
		mReadingMsg = new String[3];
		mReadingMsg[0] = res.getString(R.string.reading);
		mReadingMsg[1] = res.getString(R.string.epubParsing);
		mReadingMsg[2] = res.getString(R.string.htmlParsing);

		mImageView = new MyImageView(this);
		mGuideView = new GuideView(this);
		FrameLayout layout = new FrameLayout(this);
		layout.addView(mImageView);
		setContentView(layout);

		mImageView.setFocusable(true);
		mImageView.setGuideView(mGuideView);

		// 色とサイズを指定
		mGuideView.setColor(mTopColor1, mTopColor2, mMgnColor);
		mGuideView.setGuideSize(mClickArea, mTapPattern, mTapRate, mChgPage, mOldMenu);

		mProfileWord = new String[5];

		// 初期値を読み出す
		mProfileWord[0] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_01, "");
		mProfileWord[1] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_02, "");
		mProfileWord[2] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_03, "");
		mProfileWord[3] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_04, "");
		mProfileWord[4] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_05, "");

		// 上部メニューの設定を読み込み
		loadTopMenuState();
		if (mGuideView != null) {
			// 上部メニューの文字列情報をガイドに設定
			mGuideView.setTopCommandStr(mCommandStr);
			// 時刻＆バッテリー表示の情報をガイドに設定
			mGuideView.setTimeFormat(mTimeDisp, mTimeFormat, mTimePos, mTimeSize, mTimeColor);
		}

		setViewConfig();

		// Intentを取得する
		Intent intent = getIntent();
		// Intentに保存されたデータを取り出す
		mServer = intent.getIntExtra("Server", -1);	// サーバ選択番号
		//mHost = intent.getStringExtra("Uri");
		mURI = intent.getStringExtra("Uri");					// ベースディレクトリのuri
		mPath = intent.getStringExtra("Path");				// ベースURIからの相対パス名
		mUser = intent.getStringExtra("User");				// SMB認証用
		mPass = intent.getStringExtra("Pass");				// SMB認証用
		mFileName = intent.getStringExtra("File");			// ZIP指定時
		mImageName = intent.getStringExtra("Image"); 			// 画像直接指定時はファイル/ExpandActivityから開いた時はZIP内部のファイル

		// intentからページ番号を取り出すとバグが発生するため保存しない
		// 画像ファイル名からページ番号を決めることもしない
		// バックグラウンドでの実行を許可すると復帰時にonCreate()が呼ばれる
		// ビュワーを開いた後でintentにページ位置を上書きしても効果がない
		// mImageNameはファイルから開いたかディレクトリを開いたかの判定に使用する

		Logcat.d(logLevel, "mServer=" + mServer + ", mURI=" + mURI + ", mPath=" + mPath
				+ ", mUser=" + mUser + ", mPass=" + mPass
				+ ", mFileName=" + mFileName + ", mImageName=" + mImageName);

		if (mPath == null) {
			return;
		}

		// 最後に保存したファイル用
		mUriPath = DEF.relativePath(mActivity, mURI, mPath);
		if (mFileName != null) {
			mFilePath = DEF.relativePath(mActivity, mUriPath, mFileName);
		}
		else {
			mFilePath = mUriPath;
		}
		mTimestamp = FileAccess.date(mActivity, mFilePath, mUser, mPass);
		Logcat.d(logLevel, "mUriPath=" + mUriPath + ", mFilePath=" + mFilePath);

		saveLastFile();

		Logcat.d(logLevel, "既読位置を取得します.");
		mRestorePage = mSharedPreferences.getInt(DEF.createUrl(mFilePath, mUser, mPass), DEF.PAGENUMBER_UNREAD);

		// ジェスチャー検出を有効にする
		mDetector = new GestureDetectorCompat(this,this);
		// ダブルタップ検出のリスナーを有効にする
        mDetector.setOnDoubleTapListener(this);

		// プログレスダイアログ準備
		mReadBreak = false;

		// ページ読み込みダイアログの表示を準備
		res = mActivity.getResources();
		mProgressDialog = new CustomProgressDialog(res.getString(R.string.loadpage), res.getString(R.string.loadingfilelist),true, mHandler);

		supportFragmentManager = mActivity.getSupportFragmentManager();
		// ダイアログの表示
		mProgressDialog.show(supportFragmentManager, TAG);
		// プログレスバーをリセット
		mProgressDialog.setProgress(0);
		// 自分のIDからViewのIDを取得する
		View contentView = findViewById(android.R.id.content);
		ViewGroup rootView = (ViewGroup)contentView.getRootView();
		// ナビゲーションバーの表示更新を検出するためリスナーをセット
		rootView.setOnApplyWindowInsetsListener((view, insets) -> {
			// ナビゲーションバーの情報を取得
			boolean isVisible = insets.isVisible(WindowInsets.Type.navigationBars());
			if (isVisible) {
				// ナビゲーションバーが表示されている場合の処理
				mHideNavigationBar = false;
			} else {
				// ナビゲーションバーが非表示の場合の処理
				mHideNavigationBar = true;
			}
			return insets;
		});

		registerActivityLifecycleCallbacks(new MyLifecycleHandler());

		mListLoading = true;
		mZipLoad = new ZipLoad(mHandler, this);
		mZipThread = new Thread(mZipLoad);
		mZipThread.start();
		Logcat.d(logLevel, "終了します.");
	}

	private class MyLifecycleHandler implements Application.ActivityLifecycleCallbacks {
		@Override
		public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		}
		@Override
		public void onActivityDestroyed(Activity activity) {
		}
		@Override
		public void onActivityResumed(Activity activity) {
			// アクティビティが表側に戻ったら描画スレッドを再開させる
			SetViewUpdate();
		}
		@Override
		public void onActivityPaused(Activity activity) {
			// アクティビティが裏側に回ったら描画スレッドを停止させる
			SetViewPause();
		}
		@Override
		public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
		}
		@Override
		public void onActivityStarted(Activity activity) {
		}
		@Override
		public void onActivityStopped(Activity activity) {
		}
    }

	@Override
	public boolean onDown(MotionEvent event) {
		return true;
	}

	@Override
	public boolean onFling(MotionEvent event1, MotionEvent event2,
		float velocityX, float velocityY) {
		return true;
	}

	@Override
	public void onLongPress(MotionEvent event) {
	}

	@Override
	public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
		return true;
	}

	@Override
	public void onShowPress(MotionEvent event) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		return true;
	}

	@Override
	public boolean onDoubleTap(MotionEvent event) {
		// タッチイベントで捕捉できないダブルタップを実行する
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "onDoubleTap: " + event.toString());
		if (mDoubleTapMode) {
			// 先にタップ操作があった場合のみダブルタップを実行する
			mDoubleTapMode = false;
			if (TouchPanelView.GetTouchPositionData(2) > 0) {
				SetTouchPanelCommand(2);
			}
		}
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent event) {
		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent event) {
		// タッチイベントで捕捉できないシングルタップを実行する
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "onSingleTapConfirmed: " + event.toString());
		if (mDoubleTapMode) {
			// 先にタップ操作があった場合のみシングルタップを実行する
			mDoubleTapMode = false;
			if (TouchPanelView.GetTouchPositionData(1) > 0) {
				SetTouchPanelCommand(1);
			}
		}
		return true;
	}

	/**
	 * @Override アクティビティ一時停止時に呼び出される
	 */
	@Override
	protected void onPause() {
		super.onPause();
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "開始します.");

		if (!mFinishActivity && mSavePage && !mReadBreak) {
			saveCurrentPage();
		}
		if (mNoiseSwitch != null) {
			mNoiseSwitch.recordPause(true);
		}
		// アクティビティ一時停止時に保存
		SaveCurrentSetting();
		Logcat.v(logLevel, "終了します.");
	}

	/**
	 * @Override アクティビティ停止時に呼び出される
	 */
	@Override
	protected void onStop() {
		super.onStop();
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "開始します.");

		if (!mFinishActivity) {
			saveHistory();
		}

		if (mNoiseSwitch != null) {
			mNoiseSwitch.recordPause(true);
		}
		Logcat.v(logLevel, "終了します.");
	}

	/**
	 * @Override アクティビティ終了時に呼び出される
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.i(logLevel, "開始します.");

		if (mSourceImage[0] != null) {
			mSourceImage[0] = null;
		}
		if (mSourceImage[1] != null) {
			mSourceImage[1] = null;
		}
		if (mImageView != null) {
			mImageView.setImageBitmap(mSourceImage);
		}
		System.gc();
		Logcat.i(logLevel, "終了します.");
	}

	/**
	 * @Override アクティビティ再開時に呼び出される
	 */
	@Override
	public void onRestart(){
		super.onRestart();
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "開始します.");

		if ((mImmEnable || mImmForce) && mSdkVersion >= 19) {
			int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
			uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			getWindow().getDecorView().setSystemUiVisibility(uiOptions);
		}
		Logcat.v(logLevel, "終了します.");
	}

	/**
	 * @Override アクティビティ初回起動時や再開時に呼び出される
	 */
	@Override
	public void onStart(){
		super.onStart();
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "開始します.");
		Logcat.v(logLevel, "終了します.");
	}

	/**
	 * @Override アクティビティ初回起動時や再開時に画面が表示される時に呼び出される
	 */
	@Override
	public void onResume() {
		super.onResume();
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "開始します.");

		if (mNoiseSwitch != null) {
			mNoiseSwitch.recordPause(false);
		}
		if (mImageView != null) {
			// スレッドのループ待ちカウンタのリセットを入れる
			mImageView.lockDraw();
			mImageView.update(true);
		}

		if (mBitmapLoading) {
			// なぜか固まることがあるので暫定で実施
			long now = SystemClock.uptimeMillis();
			int t = (int) (now - mEffectStart);
			if (mEffect != 0 && t >= mEffectTime) {
				// エフェクト中でない場合は mBitmapLoading を終了させる
				startViewTimer(DEF.HMSG_EVENT_EFFECT_NEXT);
			}
		}
		Logcat.v(logLevel, "終了します.");
	}

	@Override
	protected void onUserLeaveHint(){
		super.onUserLeaveHint();
		if (mReturnListView) {
			// 画面が裏に入った場合にリスト一覧へ戻す(Android13の一部の機種でフリーズしてしまうための対策)
			finish();
		}
	}

	public class ZipLoad implements Runnable {
		private Handler handler;
		private AppCompatActivity mActivity;

		public ZipLoad(Handler handler, ImageActivity activity) {
			super();
			this.handler = handler;
			this.mActivity = activity;
		}

		public void run() {
			int logLevel = Logcat.LOG_LEVEL_WARN;
			Logcat.d(logLevel, "開始します.");

			// ファイルリストの読み込み
			mImageMgr = new ImageManager(this.mActivity, mUriPath, mFileName, mUser, mPass, mFileSort, handler, mHidden, ImageManager.OPENMODE_VIEW, mMaxThread);
			Logcat.v(logLevel, "メモリ利用状況.\n" + DEF.getMemoryString(mActivity));
			setMgrConfig(true);
			mImageMgr.LoadImageList(mMemSize, mMemNext, mMemPrev, mMemCache, DEF.MESSAGE_IMAGE);
			Logcat.v(logLevel, "メモリ利用状況.(2回目)\n" + DEF.getMemoryString(mActivity));
			mImageMgr.setViewSize(mViewWidth, mViewHeight);
			mImageView.setImageManager(mImageMgr);

			if (mImageMgr.length() == 0) {
				DEF.sendMessage(mActivity, R.string.ErrorNoPages, DEF.HMSG_TOAST, mHandler);
			}

			mRestoreMaxPage = mImageMgr.length();
			if (mRestorePage == DEF.PAGENUMBER_READ)	mRestorePage = mRestoreMaxPage - 1;
			if (mRestorePage == DEF.PAGENUMBER_UNREAD)	mRestorePage = 0;

			mCurrentPage = mRestorePage;
			if (mCurrentPage == DEF.PAGENUMBER_READ)	mCurrentPage = mRestoreMaxPage - 1;
			if (mCurrentPage == DEF.PAGENUMBER_UNREAD)	mCurrentPage = 0;
			Logcat.v(logLevel, "mCurrentPage=" + mCurrentPage + ", mRestoreMaxPage=" + mRestoreMaxPage);

			// 終了通知
			Message message = new Message();
			message.what = DEF.HMSG_READ_END;
			handler.sendMessage(message);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		// 画面外からスワイプした場合にフォーカスが外れた場合はガードを外す
		mImmCancel = false;
		if (mClickGuard) {
			mClickGuard = false;
		}
		/*
		 * if (hasFocus) { if (mInitFlg == 0) { // 起動直後のみ呼び出し mInitFlg = 1;
		 *
		 * // ビットマップの設定 mPageBack = false; setBitmapImage(); } }
		 */
		// プログレスダイアログの設定
		if (mInitFlg == 0) {
			mInitFlg = 1;
			startDialogTimer(100);
		}

		if (hasFocus) {
			if ((mImmEnable || mImmForce) && mSdkVersion >= 19) {
                int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
                uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                getWindow().getDecorView().setSystemUiVisibility(uiOptions);
			}
		}
	}

	/**
	 * 画面の設定が変更された時に発生します。
	 *
	 * @param newConfig
	 *            新しい設定。
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (mWAdjust != 100) {
			mImageView.setImageBitmap(null);
		}
		super.onConfigurationChanged(newConfig);

		// 画面方向を保持
		// Configuration config = getResources().getConfiguration();
		// mIsLandscape = (config.orientation ==
		// Configuration.ORIENTATION_LANDSCAPE);

		if (mBitmapLoading) {
			return;
		}
		if (mInitFlg == 0) {
			// // 起動直後のみ呼び出し
			// mInitFlg = false;
			//
			// // ビットマップの設定
			// setBitmapImage();
		}
		else {
			// // 縦横で単ページと見開き切替える場合
			// if (mDispMode == DISPMODE_EXCHANGE) {
			// mImageView.updateScreenSize();

			// // イメージ拡大縮小
			// ImageScaling();
			// updateOverSize();
			// setBitmapImage();
			// return;
			// }

			// 幅調整なら回転時に再読み込み
			if (mWAdjust != 100) {
				setBitmapImage();
			}

			// 2011/11/26 Viewのサイズ変更で処理する
			// this.updateOverSize();
		}
	}

	private void SetHardwareKey(int data) {
		SetTouchPanelCommandMain(data);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		int code = event.getKeyCode();

		int found_code = -1;
		int data;
		// 登録されているハードウェアキーを探す
		for (int i = 0; i < DEF.KEYCODE_INDEX.length; i++) {
			if (DEF.KEYCODE_INDEX[i] == code) {
				// 見つかった場合はハードウェアキーの設定を取り出す
				data = SetHardwareImageViewerKeyActivity.GetHardwareKeySetData(mSharedPreferences, i + 1);
				if (data != 0) {
					// 設定があればハードウェアキーが見つかったことにする
					found_code = i;
				}
				break;
			}
		}
		if (found_code == -1) {
			data = 0;
			// ハードウェアキーが見つからない場合はカスタムキーの中から探す
			for (int i = 0; i < DEF.KEY_CODE_CUSTOM_MAX; i++) {
				if (TouchPanelView.LoadCustomkeyCode(mSharedPreferences, i) == code) {
					// 見つかった場合はハードウェアキーの設定を取り出す
					// カスタムキーは末尾から追加されているの長さ分を加算する
					data = SetHardwareImageViewerKeyActivity.GetHardwareKeySetData(mSharedPreferences, DEF.KEYCODE_INDEX.length + i + 1);
					if (data != 0) {
						// 設定があれば終了
						// 無ければ他のカスタムキーを探す
						break;
					}
				}
			}
		}
		else {
			// ハードウェアキーの設定を取り出す
			data = SetHardwareImageViewerKeyActivity.GetHardwareKeySetData(mSharedPreferences, found_code + 1);
		}

		found_code = -1;
		// 戻るキー設定があるかどうかを確認する
		for (int i = 0; i < (DEF.KEYCODE_INDEX.length + DEF.KEY_CODE_CUSTOM_MAX) ; i++) {
			if (SetHardwareImageViewerKeyActivity.GetHardwareKeySetData(mSharedPreferences, i + 1) == DEF.TAP_BACK) {
				// 戻る設定があれば処理を委ねる
				found_code = 0;
				break;
			}
		}
		if (found_code == -1 && code == DEF.KEYCODE_BACK) {
			// 戻る設定が無くてコードが戻るキーだった場合はシステム設定にする
			// 戻るキーが無かった場合に破綻しないようにする
			data = 0;
		}

		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (mAutoPlay) {
				// オートプレイ中は解除
				setAutoPlay(false);
			}
			if (data == 0) {
				// システム設定の場合
				switch (code) {
					case KeyEvent.KEYCODE_DPAD_RIGHT:
					case KeyEvent.KEYCODE_DPAD_LEFT: {
						// カーソル左右でページ遷移
						if ((code == (!mChgPageKey ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_DPAD_LEFT) && mPageWay == DEF.PAGEWAY_RIGHT) ||
							(code == (!mChgPageKey ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT) && mPageWay != DEF.PAGEWAY_RIGHT)) {
							// 次ページへ
							nextPage();
						}
						else {
							// 前ページへ
							prevPage();
						}
						break;
					}
					case KeyEvent.KEYCODE_DPAD_UP: {
						// 前ページへずらす
						shiftPage(-1);
						break;
					}
					case KeyEvent.KEYCODE_DPAD_DOWN: {
						// 次ページへずらす
						shiftPage(1);
						break;
					}
					case KeyEvent.KEYCODE_MENU: {
						// 独自メニュー表示
						openMenu();
						return true;
					}
					case KeyEvent.KEYCODE_DEL:
					case KeyEvent.KEYCODE_BACK: {
						operationBack();
						return true;
					}
					case KeyEvent.KEYCODE_VOLUME_DOWN:
					case KeyEvent.KEYCODE_VOLUME_UP: {
						// ボリュームモード
						if (mVolKeyMode == DEF.VOLKEY_NONE) {
							// Volキーを使用しない
							break;
						}
						int move = mVolKeyMode == DEF.VOLKEY_DOWNTONEXT ? 1 : -1;
						if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
							move *= -1;
						}
						// 読込中の表示
						startScroll(move);
						return true;
					}
					case KeyEvent.KEYCODE_SPACE: {
						int meta = event.getMetaState();
						int move = (meta & KeyEvent.META_SHIFT_ON) == 0 ? 1 : -1;
						// 読込中の表示
						startScroll(move);
						return true;
					}
					case KeyEvent.KEYCODE_CAMERA:
					case KeyEvent.KEYCODE_FOCUS: {
						if (mRotateBtn == 0) {
							break;
						}
						else if (event.getKeyCode() != mRotateBtn) {
							return true;
						}
						if (mViewRota == DEF.ROTATE_PORTRAIT || mViewRota == DEF.ROTATE_LANDSCAPE) {
							int rotate;
							if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
								// 横にする
								rotate = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
							}
							else {
								// 縦にする
								rotate = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
							}
							setRequestedOrientation(rotate);
						}
						break;
					}
					default:
						break;
				}
			}
			else {
				// 通常設定の場合
				if (!mAutoRepeatCheck) {
					// オートリピート対策
					mAutoRepeatCheck = true;
					SetHardwareKey(data);
				}
				return true;
			}
		}
		else if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (data) {
				case DEF.HARDWARE_NONE:
					// システム設定の場合
					switch (event.getKeyCode()) {
						case KeyEvent.KEYCODE_BACK:
							break;
						case KeyEvent.KEYCODE_VOLUME_DOWN:
						case KeyEvent.KEYCODE_VOLUME_UP:
							// ボリュームモード
							if (mVolKeyMode == DEF.VOLKEY_NONE) {
								// Volキーを使用しない
								break;
							}
							return true;
						default:
							break;
					}
					break;
				default:
					// 通常設定の場合
					mAutoRepeatCheck = false;
					return true;
			}
		}
		// 自動生成されたメソッド・スタブ
		return super.dispatchKeyEvent(event);
	}

	// 描画スレッドを再開
	static void SetViewUpdate() {
		if (mViewPause) {
			if (!mViewUpdate) {
				mImageView.update(true);
				mViewUpdate = true;
			}
		}
	}
	// 描画スレッドを停止
	static void SetViewPause() {
		if (mViewPause) {
			if (mViewUpdate) {
				mImageView.lockDraw();
				mViewUpdate = false;
			}
		}
	}

	String mMessage = "";
	String mMessage2 = "";
	String mWorkMessage = "";
	// スレッドからの通知取得
	public boolean handleMessage(Message msg) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. msg.what=" + msg.what);

		// 次のイベント時間
		long now = SystemClock.uptimeMillis();
		int t = (int) (now - mEffectStart);

		if (DEF.ToastMessage(mActivity, msg)) {
			// HMSG_TOASTならトーストを表示して終了
			return true;
		}

		switch (msg.what) {
			case DEF.HMSG_RECENT_RELEASE:
				// 最新バージョンを表示
				mInformation.showRecentRelease();
				return true;

			case DEF.HMSG_PROGRESS_IMAGE:
				// 読込中の表示
				synchronized (this) {
					if (!isFinishing() && mProgressDialog != null) {
						// ページ読み込み中
						mProgressDialog.setProgress(msg.arg1);
					}
				}
				return true;

			case DEF.HMSG_EPUB_PARSE:
			case DEF.HMSG_HTML_PARSE:
				// 読込中の表示
				return true;

			case DEF.HMSG_SUB_MESSAGE:
				// 読込中の表示
				return true;

			case DEF.HMSG_WORKSTREAM:
				// 読込中の表示
				return true;

			case DEF.HMSG_EVENT_TOUCH_ZOOM:
//				Logcat.d(logLevel, "msg=" + msg.what + ", arg1=" + msg.arg1 + ", count" + mLongTouchCount);

				if (mLongTouchCount == msg.arg1) {
					// 最新のタイマーの時だけ処理
					if (mTouchFirst) {
						if (mVibFlag) {
							// 振動
							mVibrator.vibrate(TIME_VIB_RANGE);
						}

						// タッチ位置が範囲内の時だけ処理
						mLongTouchMode = true;
						mImageView.setZoomMode(true);
					}
				}
				return true;
			case DEF.HMSG_EVENT_LONG_TAP:
				if (mLongTouchCount == msg.arg1) {
					if (mTouchFirst) {
						// タッチ位置が範囲内の時だけ処理
						Logcat.v(logLevel, "HMSG_EVENT_LONG_TAP");
						SetTouchPanelCommand(3);
					}
				}
				return true;
			case DEF.HMSG_EVENT_TOUCH_TOP:
			case DEF.HMSG_EVENT_TOUCH_BOTTOM:
//				Logcat.d(logLevel, "msg=" + msg.what + ", arg1=" + msg.arg1 + ", count" + mLongTouchCount);

				if (mLongTouchCount == msg.arg1) {
					// 最新のタイマーの時だけ処理
					if (mTouchFirst) {
						// 上部の操作エリア
						mGuideView.eventTouchTimer();
					}
				}
				return true;

			case DEF.HMSG_EVENT_EFFECT:
			case DEF.HMSG_EVENT_EFFECT_NEXT:
				// 稼働中のみ次のイベント登録
				if (logLevel <= Logcat.LOG_LEVEL_DEBUG) {
					String now_ms = (new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())).format(now);
					String effect_ms = (new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())).format(mEffectStart);
					Logcat.v(logLevel, "EVENT_EFFECT: now=" + now_ms + ", mEffectStart=" + effect_ms + ", mEffectTime=" + mEffectTime + ", t=" + t);
				}
				if (t >= mEffectTime) {
					// mEffectTimeミリ秒を超えている
					mEffectRate = 0.0f;
				}
				else {
					// mEffectTimeミリ秒未満
					mEffectRate = ((float) (mEffectTime - t)) / (float) mEffectTime * (mEffectRate > 0 ? 1.0f : -1.0f);
				}
				if (mEffectRate == 0.0f) {
					// エフェクトの終わりには以前のページ表示をしない
					mImageView.createBackground(false);
				}
				// エフェクト位置
				mImageView.setEffectRate(mEffectRate);

				Logcat.v(logLevel, "EVENT_EFFECT: mEffectRate=" + mEffectRate);
				if (mEffectRate != 0.0f) {
					// エフェクト中は次のイベントを登録
					startViewTimer(DEF.HMSG_EVENT_EFFECT_NEXT);
				}
				else {
					// エフェクト無しのときはそのまま修了
					mLoadingNext = true;
					Logcat.v(logLevel, "EVENT_EFFECT: SET mBitmapLoading = false");
					mBitmapLoading = false;
					if (mAutoPlay) {
						startViewTimer(DEF.HMSG_EVENT_AUTOPLAY);
					}
				}
				break;

			case DEF.HMSG_EVENT_SCROLL:
			case DEF.HMSG_EVENT_SCROLL_NEXT:
				Logcat.v(logLevel, "HMSG_EVENT_SCROLL:");
				// スクロールで移動
				if (mImageView.moveToNextPoint(mVolScrl)) {
					// エフェクト中は次のイベントを登録
					startViewTimer(DEF.HMSG_EVENT_SCROLL_NEXT);
				}
				else {
					// エフェクト無しのときはそのまま修了
					mLoadingNext = true;
					mBitmapLoading = false;
					mScrolling = false;
					if (mAutoPlay) {
						startViewTimer(DEF.HMSG_EVENT_AUTOPLAY);
					}
				}
				break;

			case DEF.HMSG_EVENT_LOADING:
			case DEF.HMSG_EVENT_LOADING_NEXT:
				// イメージをローディング中
				if (mEffect != 0 && mBitmapLoading && t > mEffectTime) {
					mGuideView.countLoading(true);
					startViewTimer(DEF.HMSG_EVENT_LOADING_NEXT);
				}
				break;

			case DEF.HMSG_EVENT_AUTOPLAY:
				// スクロールで移動
				if (mAutoPlay) {
					startScroll(1);
				}
				break;

			case DEF.HMSG_LOAD_END: // 画像読み込み終了
				Logcat.v(logLevel, "HMSG_LOAD_END: mEffectRate=" + mEffectRate);
				if (mBitmapLoading) {
					// Loading中を消去
					if (mSourceImage[0] != null) {
						mGuideView.setLodingState();
					}
					else {
						mGuideView.setLodingState(mLoadErrStr);
					}

					// 表示中の画像が1枚か2枚かを判定
					int shareType;
					if (mSourceImage[0] != null && mSourceImage[1] != null) {
						shareType = DEF.SHARE_LR;
					}
					else {
						shareType = DEF.SHARE_SINGLE;
					}

					// ページ番号入力が開いていたら共有タイプを設定
					if (PageSelectDialog.mIsOpened) {
						mPageDlg.setShareType(shareType);
					}
					// サムネイルページ選択が開いていたら共有タイプを設定
					if (PageThumbnail.mIsOpened) {
						mThumbDlg.setShareType(shareType);
					}

					// ビットマップを設定
					synchronized (mImageView) {
						mImageView.setImageBitmap(mSourceImage);
						// 2011/11/18 ルーペ機能
						this.updateOverSize(false);
					}
					if (mTerminate) {
						finish();
					}

					if (mEffect != 0 && !mPageSelecting && mEffectRate == 0.0f) {
						// エフェクト開始
						// 次のページ遷移が予約されている場合はエフェクトしない
						startViewTimer(DEF.HMSG_EVENT_EFFECT);
					}
					else {
						// 以前のページ表示を終了
						mImageView.createBackground(false);

						// エフェクト無しのときはそのまま終了
						mLoadingNext = true;
						mBitmapLoading = false;
						mPageSelecting = false;
						if (mAutoPlay) {
							startViewTimer(DEF.HMSG_EVENT_AUTOPLAY);
						}
					}
					mGuideView.countLoading(false);

					String pagenum = null;
					if (mPnumDisp) {
						if (mPnumFormat == 0 || mSourceImage[1] == null) {
							pagenum = (mCurrentPage + 1) + " / " + mImageMgr.length();
						}
						else {
							pagenum = (mCurrentPage + 1) + " - " + (mCurrentPage + 2) + " / " + mImageMgr.length();
						}
					}
					mGuideView.setPageNumberFormat(mPnumDisp, mPnumFormat, mPnumPos, mPnumSize, mPnumColor);
					mGuideView.setPageNumberString(pagenum);

					// 現在ページを保存
					if (mSavePage) {
						saveCurrentPage();
					}
					if (mNextPage != -1 && !mBitmapLoading) {
						// ページ選択
						if (mCurrentPage != mNextPage) {
							Logcat.d(logLevel, "current:" + mCurrentPage + ", next:" + mNextPage + " (LoadEnd)");
							mCurrentPage = mNextPage;

							// ページ変更時に振動
							startVibrate();
							mPageBack = false;
							setBitmapImage();
							mPageSelecting = true;
						}
						mNextPage = -1;
					}
				}
				return true;

			case DEF.HMSG_ERROR:
				// 読込中の表示
				Toast.makeText(this, (String) msg.obj, Toast.LENGTH_SHORT).show();
				return true;

			case DEF.HMSG_CACHE:
				// アクセス状態表示フラグ
				if (mAccessLamp) {
					// 読込中の表示
					int mark;
					if (msg.arg1 < 0) {
						mark = 0;
					}
					else {
						mark = msg.arg2;
					}
					mGuideView.setCacheMark(mark);
				}
				return true;

			case DEF.HMSG_NOISESTATE:
				// 状態表示
				if (FileSelectActivity.GetRecordSw()) {
					mGuideView.setNoiseState(msg.arg1, mNoiseLevel ? msg.arg2 : -1);
				}
				return true;

			case DEF.HMSG_NOISE:
				// 読込中の表示
				if (msg.arg1 == NOISE_NEXTPAGE) {
					if (mNoiseScroll != 0) {
						// スクロール停止
						mNoiseScroll = 0;
					}
					// else
					startScroll(1);
				}
				else if (msg.arg1 == NOISE_PREVPAGE) {
					if (mNoiseScroll != 0) {
						// スクロール停止
						mNoiseScroll = 0;
					}
					// else
					startScroll(-1);
				}
				else if (msg.arg1 == NOISE_NEXTSCRL || msg.arg1 == NOISE_PREVSCRL) {
					int way = 1;
					if (msg.arg1 == NOISE_PREVSCRL) {
						way = -1;
					}

					// 読込中の表示
					if (mImageView.checkScrollPoint() && (mNoiseScroll != 0 && way == mNoiseScroll)) {
						mImageView.moveToNextPoint(mNoiseScrl);
					}
					else {
						mNoiseScroll = way;
						// 次のポイントへスクロール開始
						if (mImageView.setViewPosScroll(mNoiseScroll)) {
						}
					}
				}
				return true;

			case DEF.HMSG_LOADING:
				// アクセス状態表示フラグ
				if (mAccessLamp) {
					// 読み込み済みデータサイズの表示
					mGuideView.setLodingState((msg.arg1 >> 8) & 0xFF, msg.arg1 & 0xFF, msg.arg1 >> 24, msg.arg2);
				}
				return true;

			case DEF.HMSG_READ_END:
				// 読込中の表示
				if (!isFinishing() && mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				mListLoading = false;
				if (mTerminate) {
					finish();
				}

				// intentから取り出した画像ファイル名からページ番号を決定するとバグが発生するため反映しない
				// バックグラウンドでの実行を許可すると復帰時にonCreate()が呼ばれる
				// ビュワーを開いた後でintentにページ位置を上書きしても効果がない
				if (mImageName != null && !mImageName.isEmpty()) {
					int page = mImageMgr.search(mImageName);
					if (page != -1) {
						mCurrentPage = page;
					}
				}

				// 既読の場合は最終ページ
				if (mImageMgr.length() == 0) {
					mCurrentPage = 0;
				}
				else if (mCurrentPage < 0) {
					mCurrentPage = mImageMgr.length() - 1;
				}
				else if (mCurrentPage >= mImageMgr.length()) {
					mCurrentPage = mImageMgr.length() - 1;
				}

				mThumID = System.currentTimeMillis();
				mPageBack = false;
				setBitmapImage();
				break;
			case DEF.HMSG_PROGRESS_CANCEL:
				// Thread を停止
				if (mImageMgr != null) {
					mImageMgr.setBreakTrigger();
				}
				mTerminate = true;
				mReadBreak = true;
				// トースト表示させる
				Resources res = mActivity.getResources();
				Toast.makeText(this, res.getString(R.string.cancelloadpage), Toast.LENGTH_SHORT).show();
				break;
		}
		return false;
	}

	// ピンチズームの更新
	private void SetPinchScaleSetting() {
		if (mPinchScaleSetting) {
			// スケーリング中だった場合は解除
			mPinchScaleSetting = false;
			// サイズ変更終了
			mImageView.setPinchChanging(0);
			// 画面更新中の文字を表示
			Resources res = mActivity.getResources();
			mGuideView.setGuideText(res.getString(R.string.MesUpscaling));
			// ピンチズームが変更されていれば画面更新
			if (mPinchScale != mPinchScaleSel) {
    			mImageView.lockDraw();
				synchronized (mImageView) {
					mPinchScale = mPinchScaleSel;
					// スケーリング変更
					mImageMgr.setImageScale(mPinchScale);
					// イメージ拡大縮小
					ImageScaling();
				}
				// ビットマップを調整
				this.updateOverSize(true);
				// 描画スレッド開始
				mImageView.update(true);
			}
			// 画面更新中の文字を消す
			mGuideView.setGuideText(null);
		}

	}

	public void setBitmapImage() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mImageMgr == null || mImageMgr.length() - 1 < mCurrentPage) {
			return;
		}

		// イメージの場合
		if (mBitmapLoading) {
			return;
		}
		mBitmapLoading = true;

		// ピンチズームの更新
		SetPinchScaleSetting();

		mImageView.lockDraw();
		mImageView.createBackground(true);
		mImageView.update(true);

		// mImageView.lockDraw();
		// 旧ビットマップを解放
		mSourceImage[0] = null;
		mSourceImage[1] = null;
		// 解放
		mImageView.setImageBitmap(mSourceImage);

		mCurrentPageHalf = false;
		mCurrentPageDual = false;
		if (!isDualView() && mCurrentPage < 0) {
			// 範囲外は読み込みしない
			mCurrentPage = 0;
		}

		// Loadingのダイアログを表示
		if (mTerminate) {
			finish();
		}

		mBmpLoad = new BmpLoad(mHandler, DEF.HMSG_LOAD_END);
		mBmpThread = new Thread(mBmpLoad);
		mBmpThread.start();

		startViewTimer(DEF.HMSG_EVENT_LOADING);
	}

	public class BmpLoad implements Runnable {
		private Handler handler;
		private int mLoadEndMsg;

		public BmpLoad(Handler handler, int msg) {
			super();
			this.handler = handler;
			mLoadEndMsg = msg;
		}

		public void run() {
			int logLevel = Logcat.LOG_LEVEL_WARN;
			Logcat.d(logLevel, "開始します.");

			// 0: 現在のページ, 1: 2ページ目, 2: 前のページ, 3: 前の2ページ目, 4: 次のページ, 5: 次の2ページ目
			//ImageData bm[] = { null, null, null, null, null, null };

			ImageData[] bm = { null, null };

			// 仮に現在ページを設定
			mImageMgr.setCurrentPage(mCurrentPage, false);

			// 並べて表示以外は1回ループ
			for (int i = 0; i < 2; i++) {
				// 並べて表示以外は1回のみ読込
				if (!isDualView() && i == 1) {
					break;
				}

				int idx;

				// 並べて表示かつページ戻りのときは2ページ目から読込み
				if (isDualView() && mPageBack) {
					idx = (i == 0) ? 1 : 0;
				}
				else {
					idx = i;
				}

				int page = mCurrentPage + idx;

				if (page >= mImageMgr.length() || page < 0) {
					// 範囲外は読み込みしない
					continue;
				}

				// ビットマップのロード
				bm[idx] = loadBitmap(page, true);
				if (bm[idx] != null && isDualView()) {
					if (i == 0) {
						if (!DEF.checkPortrait(bm[idx].Width, bm[idx].Height, mRotate)) {
							// 横長だったので次は読み込まない
							break;
						}
					}
					else if (i == 1) {
						// 2ループ目
						// 2枚目の横長チェック
						// 2つ目のBitmapが横長の場合は読み込まない
						if (!DEF.checkPortrait(bm[idx].Width, bm[idx].Height, mRotate)) {
							// 横長だったので使用しない
							bm[idx] = null;
						}
					}
				}
			}

			if (bm[1] != null && bm[0] == null) {
				// 片側しか読み込んでいない場合はbm[0]に移す
				bm[0] = bm[1];
				bm[1] = null;
			}

			boolean isSingle = false; // 現在ページが単ページか
			if (isHalfView() && bm[0] != null) {
				if (!DEF.checkPortrait(bm[0].Width, bm[0].Height, mRotate)) {
					// 横長画像であれば分割
					mSourceImage[0] = bm[0];
					if ((mHalfPos == HALFPOS_2ND && mPageWay == DEF.PAGEWAY_RIGHT) || (mHalfPos != HALFPOS_2ND && mPageWay == DEF.PAGEWAY_LEFT)) {
						// 左側用にする
						mSourceImage[0].HalfMode = ImageData.HALF_LEFT;
					}
					else {
						// 右側用にする
						mSourceImage[0].HalfMode = ImageData.HALF_RIGHT;
					}

					mCurrentPageHalf = true;
				}
				else {
					mHalfPos = HALFPOS_1ST;
					mSourceImage[0] = bm[0];
				}
			}
			else if (isDualView()) {
				if (bm[0] != null && bm[1] != null) {
					if (mCurrentPage == 0 && mTopSingle) {
						// 1ページのみ出力
						mHalfPos = HALFPOS_1ST;
						if (mPageBack) {
							// 戻りの場合は2ページ目にする
							mSourceImage[0] = (mPageWay != DEF.PAGEWAY_LEFT) ? bm[1] : bm[0];
							mCurrentPage = 1;
						}
						else {
							// 戻りじゃない場合は1ページ目にする
							mSourceImage[0] = (mPageWay != DEF.PAGEWAY_LEFT) ? bm[0] : bm[1];
							mCurrentPage = 0;
						}
						isSingle = true;
					}
					else {
						// 並べて表示
						// ページ方向が逆なら左右の並びを入れ替える
						if (mPageWay != DEF.PAGEWAY_LEFT) {
							mSourceImage[0] = bm[0]; // 右描画用
							mSourceImage[1] = bm[1]; // 左描画用
						}
						else {
							mSourceImage[0] = bm[1]; // 右描画用
							mSourceImage[1] = bm[0]; // 左描画用
						}

						mCurrentPageDual = true;
						mHalfPos = HALFPOS_1ST;
					}
				}
				else {
					// 1ページをそのまま出力
					mHalfPos = HALFPOS_1ST;
					mSourceImage[0] = bm[0];
					if (mPageBack) {
						// 戻りの場合は2ページ目にする
						mCurrentPage++;
					}
					isSingle = true;
				}
			}
			else if (bm[0] != null) {
				// 1ページをそのまま出力
				mHalfPos = HALFPOS_1ST;
				mSourceImage[0] = bm[0];
			}
			bm[0] = null;
			bm[1] = null;

			// 正しい現在頁
			mImageMgr.setCurrentPage(mCurrentPage, isSingle);

			// 拡大/縮小
			ImageScaling();

			// 終了通知
			Message message = new Message();
			message.what = mLoadEndMsg;
			handler.sendMessage(message);
		}
	}

	// Bitmapを読み込む
	private ImageData loadBitmap(int page, boolean notice) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		ImageData bm = null;

		try {
			bm = mImageMgr.loadBitmap(page, notice);
		} catch (Exception ex) {
			Message message = new Message();
			message.what = DEF.HMSG_ERROR;
			message.obj = ex.getMessage();
			mHandler.sendMessage(message);
			return null;
		}
		return bm;
	}

	/**
	 * View がクリックされた時に発生します。
	 */
	public void ChangeScale(int mode) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		mScaleMode = mode;

		setMgrConfig(true);
		// イメージ拡大縮小
		ImageScaling();

		this.updateOverSize(true);

		// 第3引数は、表示期間（LENGTH_SHORT、または、LENGTH_LONG）
		// Toast.makeText(this, strScaleMode, Toast.LENGTH_SHORT).show();

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor ed = sp.edit();
		ed.putInt("scalemode", mScaleMode);
		ed.apply();
	}

	/**
	 * ページ並びを逆順にする
	 */
	public void reverseOrder() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		mReverseOrder = !mReverseOrder;
		mImageMgr.reverseOrder();
		setMgrConfig(false);

		// イメージ拡大縮小
		ImageScaling();
		this.updateOverSize(false);
		setBitmapImage();
	}

	//指定したページをクロップして書庫／フォルダのサムネイルに設定
	public void setThumbCropped(int page) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		int thumbH = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeH(mSharedPreferences));
		int thumbW = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeW(mSharedPreferences));
		Intent intent = new Intent(mActivity, CropImageActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("Path", mUriPath);
		intent.putExtra("File", mFileName);
		intent.putExtra("User", mUser);
		intent.putExtra("Pass", mPass);
		intent.putExtra("Page", page);
		intent.putExtra("aspectRatio", (float)thumbW / (float)thumbH);
		startActivityForResult(intent, DEF.REQUEST_CROP);
	}

	public void setThumb(Uri uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		Bitmap bm = null;
		long thumbID = System.currentTimeMillis();
		int thumH = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeH(mSharedPreferences));
		int thumW = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeW(mSharedPreferences));

		try {
			ContentResolver cr = getContentResolver();
			InputStream in = cr.openInputStream(uri);
			// サイズのみ取得
			BitmapFactory.Options option = new BitmapFactory.Options();
			option.inJustDecodeBounds = true;
			option.inPreferredConfig = Bitmap.Config.RGB_565;
			BitmapFactory.decodeStream(in, null, option);
			in.close();
			if (option.outHeight != -1 && option.outWidth != -1) {
				// 縮小してファイル読込
				option.inJustDecodeBounds = false;
				option.inPreferredConfig = Bitmap.Config.RGB_565;
				option.inSampleSize = DEF.calcThumbnailScale(option.outWidth, option.outHeight, thumW, thumH);
				in = cr.openInputStream(uri);
				bm = BitmapFactory.decodeStream(in, null, option);
				in.close();
			}
			if(bm == null)
				return;
		}catch(IOException e){
			Logcat.e(logLevel, "error");
			Toast.makeText(mActivity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
			return;
		}

		bm = ImageAccess.resizeTumbnailBitmap(bm, thumW, thumH, ImageAccess.BMPCROP_NONE, ImageAccess.BMPMARGIN_NONE);
		if (bm != null) {
			ThumbnailLoader loader = new ThumbnailLoader(mActivity, "", "", null, thumbID, new ArrayList<FileData>(), thumW, thumH, 0, ImageAccess.BMPCROP_NONE, ImageAccess.BMPMARGIN_NONE);
			loader.deleteThumbnailCache(mFilePath, thumW, thumH);
			loader.setThumbnailCache(mFilePath, bm);
			Toast.makeText(this, R.string.ThumbConfigured, Toast.LENGTH_SHORT).show();
		}
	}

	//指定したページを書庫／フォルダのサムネイルに設定
	public void setThumb(int page) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		long thumbID = System.currentTimeMillis();
		int thumH = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeH(mSharedPreferences));
		int thumW = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeW(mSharedPreferences));
		Bitmap bm = null;
		try {
			Object lock = mImageMgr.getLockObject();
			synchronized (lock) {
				Logcat.d(logLevel, "Call LoadThumbnail(" + page + ", " + thumW + ", " + thumH + ") start.");
				// 読み込み処理とは排他する
				bm = mImageMgr.LoadThumbnail(page, thumW, thumH);
			}
		} catch (Exception e) {
			Logcat.e(logLevel, "error");
		}
		if (bm != null) {
			bm = ImageAccess.resizeTumbnailBitmap(bm, thumW, thumH, ImageAccess.BMPCROP_NONE, ImageAccess.BMPMARGIN_NONE);
		}
		if (bm != null) {
			ThumbnailLoader loader = new ThumbnailLoader(mActivity, "", "", null, thumbID, new ArrayList<FileData>(), thumW, thumH, 0, ImageAccess.BMPCROP_NONE, ImageAccess.BMPMARGIN_NONE);
			loader.deleteThumbnailCache(mFilePath, thumW, thumH);
			loader.setThumbnailCache(mFilePath, bm);
			Toast.makeText(this, R.string.ThumbConfigured, Toast.LENGTH_SHORT).show();
		}
	}
	public void toggleCenterMargin() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		mCMargin = !mCMargin;
		setViewConfig();
		setBitmapImage();
	}

	public void toggleCenterShadow() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		mCShadow = !mCShadow;
		setViewConfig();
		setBitmapImage();
	}

	private void setMgrConfig(boolean scaleinit) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. scaleinit=" + scaleinit);

		if (mImageMgr != null) {
			mImageMgr.setConfig(mScaleMode, mCenter, mFitDual, mDispMode, mNoExpand, mAlgoMode, mRotate, mWAdjust
					, mWidthScale, mImgScale, mPageWay, mMgnCut, mMgnCutColor, 0, mBright, mGamma, mSharpen, mInvert, mGray, mPseLand, mMoire, mTopSingle, scaleinit, mEpubOrder, mZoomType, mContrast, mHue, mSaturation, mColoring, mMgnBlkMsk, mMarginLevel, mMarginLimit, mMarginSpace, mMarginRange, mMarginStart, mMarginAspectMask, mMarginForceIgnoreAspect);
		}
		// モードが変わればスケールは初期化
		if (scaleinit) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			synchronized (mImageView) {
				mPinchScale = SetImageActivity.getPinScale(sharedPreferences);
				mImageMgr.setImageScale(mPinchScale);
				ImageScaling();
			}
		}
		Logcat.d(logLevel, "setMgrConfig: 終了します.");
	}

	private void setViewConfig() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mImageView != null) {
			mImageView.setConfig(this, mMgnColor, mCenColor, mTopColor1, mViewPoint, mMargin, mCenter, mShadow, mZoomType, mPageWay, mScrlWay, mScrlRngW, mScrlRngH, mPrevRev, mNoExpand, mFitDual,
					mCMargin, mCShadow, mPseLand, mEffect, mScrlNext, mViewNext, mNextFilter, mDisplayPosition);
			mImageView.setLoupeConfig(mLoupeSize);	// ルーペサイズの設定
		}
		if (mGuideView != null) {
			// 操作ガイドの設定
			mGuideView.setGuideMode(isDualView(), mBottomFile, mPageWay == DEF.PAGEWAY_RIGHT, mPageSelect, mImmEnable | mImmForce);
		}
	}

	public static int isGestureNavigationEnabled(Context context) {
		// APIレベル29以降でジェスチャーナビゲーションの有効状態を取得
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			return context.getResources().getInteger(Resources.getSystem()
	        .getIdentifier("config_navBarInteractionMode", "integer", "android"));
		}
		else {
			return 0;
		}
	}

	/**
	 * View がタッチされた時に発生します。
	 *
	 * @param v
	 *            タッチされた View。
	 * @param event
	 *            イベント データ。
	 *
	 * @return タッチ操作を他の View へ伝搬しないなら true。する場合は false。
	 */
	public boolean onTouchEvent(MotionEvent event){
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mAutoPlay) {
			Logcat.v(logLevel, "mAutoPlay");
			// オートプレイ中は解除
			setAutoPlay(false);
		}

		if (mListLoading) {
			Logcat.v(logLevel, "mListLoading");
			// ファイル一覧の読み込み中はページ操作しない
			return true;
		}
		if (mImageMgr == null || mImageMgr.length() == 0) {
			Logcat.v(logLevel, "mImageMgr == null || mImageMgr.length() == 0");
			if (mImageMgr != null) {
				// 読み込み停止
				mImageMgr.setBreakTrigger();
				// キャッシュ読込スレッド停止
				mImageMgr.closeFiles();
			}
			setResult(RESULT_OK);
			finish();
			return true;
		}
		if (mBitmapLoading) {
			// ビットマップ読込中は操作不可
			Logcat.v(logLevel, "mBitmapLoading == true");

			long now = SystemClock.uptimeMillis();
			int t = (int) (now - mEffectStart);
			if (mEffect != 0 && t >= mEffectTime) {
				// なぜか固まることがあるので暫定で実施
				// エフェクト中でない場合は mBitmapLoading を終了させる
				startViewTimer(DEF.HMSG_EVENT_EFFECT_NEXT);
			}
			// エフェクト中であればタッチを無視する
			return true;
		}

		float x;
		float y;
		int cx;
		int cy;
		if (!mPseLand) {
			x = event.getX();
			y = event.getY();
			cx = mImageView.getWidth();
			cy = mImageView.getHeight();
		}
		else {
			// 疑似横モード
			cx = mImageView.getHeight();
			cy = mImageView.getWidth();
			y = cy - event.getX();
			x = event.getY();
		}

		int action = event.getAction();

		// ピンチイン・アウト対応
		if (mPinchEnable) {
    		int action2 = action & MotionEvent.ACTION_MASK;
    		// ズーム中ではない && ページ表示ではない && ガイド表示ではない
    		if (action2 == MotionEvent.ACTION_POINTER_1_DOWN) {
    			mPinchDown = true;
    			if (mPinchOn) {
    				// 記録
    				if (mPinchCount == 0) {
    					mPinchTime = SystemClock.uptimeMillis();
    				}
    				mPinchCount ++;
    			}
    		}
    		else if (action2 == MotionEvent.ACTION_POINTER_1_UP) {
    			mPinchDown = false;
    			if (mPinchOn) {
    				// 押されてからの時間を判定
    				long nowtime = SystemClock.uptimeMillis();
    				if (nowtime - mPinchTime <= 1000) {
    					// 1000ミリ秒以内
    					if (mPinchCount == 2) {
    						// 100%にする
    						// 任意スケーリング変更中
							mPinchScaleSel = mPinchScale;
    						mImageView.setPinchChanging(mPinchScaleSel);
    						mGuideView.setGuideText(mPinchScaleSel + "%");
    					}
    				}
    				else {
    					mPinchCount = 0;
    				}

    				if (mPinchCount == 2) {
    					mPinchCount = 0;
    				}
    			}
    		}

    		if (!mPinchOn && !mPageMode && mOperation != TOUCH_COMMAND && mPinchDown) {
    			if (action2 == MotionEvent.ACTION_MOVE) {
    				int count = event.getPointerCount();
    				if (count >= 2) {
    					float x1 = (int)event.getX(0);
    					float y1 = (int)event.getY(0);
    					float x2 = (int)event.getX(1);
    					float y2 = (int)event.getY(1);
    					if (Math.abs(x1 - x2) > mSDensity * 20 || Math.abs(y1 - y2) > mSDensity * 20) {
    						// 2点間が10sp以上であれば拡大縮小開始
    						mPinchOn = true;
    						mPinchScaleSel = mPinchScale;
    						mTouchFirst = false;
    						mImageView.setZoomMode(false);
    						mLongTouchMode = false;
    					}
    				}
    			}
    		}
    		if (mPinchOn) {
    			// サイズ変更中
    			if (action == MotionEvent.ACTION_CANCEL) {
    				// サイズ変更キャンセル(画面回転など)
    				mImageView.setPinchChanging(0);
    				mGuideView.setGuideText(null);
    				mPinchOn = false;
    			}
    			else if (action2 == MotionEvent.ACTION_POINTER_1_DOWN || action2 == MotionEvent.ACTION_MOVE) {
    				// サイズ変更
    				int count = event.getPointerCount();
    				float x1 = 0;
    				float y1 = 0;
    				for (int i = 0; i < count; i++) {
    					x = (int) event.getX(i);
    					y = (int) event.getY(i);

    					if (i == 0) {
    						x1 = x;
    						y1 = y;

    					}
    					else if (i == 1) { // if (mPinchId == (int)
    									   // event.getPointerId(i)) {
    						// 距離を求める
    						int range;
    						range = (int) Math.sqrt(Math.pow(Math.abs(x1 - x), 2) + Math.pow(Math.abs(y1 - y), 2));
    						if (mPinchDown) {
    							mPinchRange = range;
    							mPinchDown = false;
    						}
    						else {
    							// 初回は記録のみ
    							int range2 = (int)((range - mPinchRange) / (8 * mSDensity));
    							if (range2 != 0) {
    								mPinchCount = 0;
    							}
    							int zoom = range2;
    							if (Math.abs(zoom) >= 6) {
    								zoom *= 8;
    							}
    							else if (Math.abs(zoom) >= 4) {
    								zoom *= 4;
    							}
    							else if (Math.abs(zoom) >= 2) {
    								zoom *= 2;
    							}
    							mPinchScaleSel += zoom;
    							if (mPinchScaleSel < 10) {
    								mPinchScaleSel = 10;
    							}
    							else if (mPinchScaleSel > 250) {
    								mPinchScaleSel = 250;
    							}
    							mPinchRange += range2 * (8 * mSDensity);
    						}
    						// 任意スケーリング変更中
    						mImageView.setPinchChanging(mPinchScaleSel);
    						mGuideView.setGuideText(mPinchScaleSel + "%");
    					}
    				}
    			}
    			else if (action2 == MotionEvent.ACTION_UP) {
    				// サイズ変更終了
    				if (mPinchScale != mPinchScaleSel) {
    					mImageView.lockDraw();
    				}
    				mImageView.setPinchChanging(0);
    				mGuideView.setGuideText(null);
    				mPinchOn = false;
    				mPinchDown = false;
    				if (mPinchScale != mPinchScaleSel) {
    					synchronized (mImageView) {
    						mPinchScale = mPinchScaleSel;
    						mImageMgr.setImageScale(mPinchScale);
    						ImageScaling();
    					}
    					this.updateOverSize(true);
    					mImageView.update(true);

						Editor ed = mSharedPreferences.edit();
						ed.putString(DEF.KEY_PinchScale, Integer.toString(mPinchScale));
						ed.apply();
    				}
    			}
    			return true;
    		}
		}

		if (mLoadingNext) {
			// ローディング中のタッチは次の移動イベントでONにする
			if (action == MotionEvent.ACTION_MOVE) {
				if (!mScrlNext) {
					action = MotionEvent.ACTION_DOWN;
				}
			}
			mLoadingNext = false;
		}

		if (mImmEnable || mImmForce) {
			if (action == MotionEvent.ACTION_DOWN) {
				// IMMERSIVEモードの発動時にタッチ処理を無視する(スワイプでバーを表示させるときに重なるのを防ぐ)
				int navibar_height = 0;
				int statusibar_height = 0;
				if (mSdkVersion >= 19) {
					// ナビゲーションバーの高さを得る
					if (mImageView.getHeight() < mImageView.getWidth()) {
						// 横向きの場合
						navibar_height = insets.right;
						statusibar_height = insets.left;
					}
					else {
						// 縦向きの場合
						navibar_height = insets.bottom;
						statusibar_height = insets.top;
					}
				}
				else {
					statusibar_height = mImmCancelRange;
					navibar_height = mImmCancelRange;
				}
				if (mHideNavigationBar) {
					// ナビゲーションバーが非表示の場合は誤検出防止のガードを入れる
					navibar_height = CLICKGUARD;
				}
				if (y <= statusibar_height || y >= cy - navibar_height) {
					mImmCancel = true;
				}
			}
			if (mImmCancel) {
				// ImmerModeの場合は上下端のタッチを無視する
				if (action == MotionEvent.ACTION_UP) {
					// UPイベントで解除
					mImmCancel = false;
				}
				return true;
			}
		}
		// タッチパネルの座標を設定する
		TouchPanelView.SetTouchPosition((int)x, (int)y, cx, cy);

		if (this.mDetector.onTouchEvent(event)) {
			// タッチイベントで捕捉できる場合
			switch (action) {
				case MotionEvent.ACTION_DOWN:
					Logcat.v(logLevel, "ACTION_DOWN");
					// 押下状態を設定
					if (!mClickGuard) {
						// ジェスチャーナビゲーションモードで画面下からのスワイプだった場合は無視する
						int navibar_height = 0;
						if (mSdkVersion >= 19) {
							// ナビゲーションバーの高さを得る
							if (mImageView.getHeight() < mImageView.getWidth()) {
								// 横向きの場合
								navibar_height = insets.right;
							}
							else {
								// 縦向きの場合
								navibar_height = insets.bottom;
							}
						}
						if (navibar_height == 0) {
							// ナビゲーションバーが非表示だった場合は固定値を入れる
							navibar_height = mImmCancelRange;
						}
						if (mHideNavigationBar) {
							// ナビゲーションバーが非表示の場合は誤検出防止のガードを入れる
							navibar_height = CLICKGUARD;
						}
						if ((y >= cy - navibar_height) && isGestureNavigationEnabled(mActivity) == 2 && (!mImmForce && !mImmEnable)) {
							mClickGuard = true;
						}
					}
					if (mClickGuard) {
					}
					else if (mTapEditMode) {
					}
					else {
						mGuideView.eventTouchDown((int)x, (int)y, cx, cy, true);
					}

					mPageMode = false;
					mTouchPointNum = 0;

					// 慣性スクロールの停止
					mImageView.scrollStop();

					if (y >= cy - mClickArea) {
						if (mClickGuard) {
						}
						else if (mTapEditMode) {
						}
						else if (mPageSelect == PAGE_SLIDE) {
							if (mClickArea <= x && x <= cx - mClickArea) {
								// ページ選択開始
								int sel = GuideView.GUIDE_BCENTER;
								mSelectPage = mCurrentPage;
								mGuideView.setGuideIndex(sel);

								mPageMode = true;
								mPageModeIn = true;
							}
						}
						else {
							mSelectPage = mCurrentPage;
						}
						// 下部押下
						if (mClickGuard) {
						}
						else if (mTapEditMode) {
						}
						else {
							if (!mClickGuard) {
								startLongTouchTimer(DEF.HMSG_EVENT_TOUCH_BOTTOM); // ロングタッチのタイマー開始
								mOperation = TOUCH_COMMAND;
								// 長押し対応のため、再設定する(IMMERSIVEがOFFでも長押し対応するため)
								mGuideView.eventTouchDown((int)x, (int)y, cx, cy, true);
								// 文書情報を表示
								mGuideView.setPageText(mImageMgr.createPageStr(mSelectPage));
								mGuideView.setPageColor(mTopColor1);
							}
						}
					}
					else if (y <= mClickArea) {
						// 上部押下
						if (mTapEditMode) {
						}
						else {
							startLongTouchTimer(DEF.HMSG_EVENT_TOUCH_TOP); // ロングタッチのタイマー開始
							mOperation = TOUCH_COMMAND;
						}
					}
					else {
						// 操作モード
						mOperation = TOUCH_OPERATION;
						if (mTapEditMode) {
						}
						else {
							if (TouchPanelView.GetTouchPositionData(3) > 0) {
								// 長押しタップの場合
								// タッチパネル設定が有効な場合
								startLongTouchTimer(DEF.HMSG_EVENT_LONG_TAP); // ロングタッチのタイマー開始
							}
							// 現在のイメージ表示位置をフリックの判定のため記憶
							mTouchDrawLeft = (int)x;
							callZoomAreaDraw(x, y);
							startLongTouchTimer(DEF.HMSG_EVENT_TOUCH_ZOOM); // ロングタッチのタイマー開始

							mTouchPoint[0].x = x;
							mTouchPoint[0].y = y;
							mTouchPointTime[0] = SystemClock.uptimeMillis();
							mTouchPointNum = 1;
						}
					}

					this.mTouchMove = false;
					this.mTouchFirst = true;	// 押してから移動してないフラグ
					this.mTouchBeginX = x;	// 最初の位置
					this.mTouchBeginY = y;
					// タップによるアクション操作の様子見時間の開始ミリ秒を取得
					mActionMoveSkipStartTime = SystemClock.uptimeMillis();
					break;

				case MotionEvent.ACTION_MOVE:
					Logcat.v(logLevel, "ACTION_MOVE");
					// Logcat.d(logLevel, "x=" + x + ", y=" + y);
					// タップによるアクション操作の様子見時間の経過ミリ秒を取得
					long nowactiontime = SystemClock.uptimeMillis();
					int t = (int) (nowactiontime - mActionMoveSkipStartTime);
					if (mClickGuard) {
					}
					else if (mTapEditMode) {
					}
					else if (t < 100) {
						// タップによるページめくりとスクロールを優先させるため100ミリ秒未満はアクション操作をスキップさせる
					}
					else {
						// タップのスクロール
						Action_Move_Sub(event);
					}
					break;

				case MotionEvent.ACTION_CANCEL:
					Logcat.v(logLevel, "ACTION_CANCEL");
					if (mLongTouchMode) {
						// ズーム表示解除
						mImageView.setZoomMode(false);
						mLongTouchMode = false;
					}
					// 押してる間のフラグクリア
					mTouchFirst = false;
					mOperation = TOUCH_NONE;
					mPinchOn = false;
					mPinchDown = false;

					// 上部/下部選択中の状態解除
					mGuideView.eventTouchCancel();
					// ページ選択中解除
					mGuideView.setGuideIndex(GuideView.GUIDE_NONE);
					break;
				case MotionEvent.ACTION_UP:
					Logcat.v(logLevel, "ACTION_UP");
					// タップの解除
					if (mClickGuard) {
						mClickGuard = false;
					}
					Action_Up_Sub(event);
					if (mOperation == TOUCH_OPERATION) {
						Logcat.v(logLevel, "通常タップ");
						// ■■■ タップ終了なら
						if (mTapEditMode) {
							// タップ操作の設定ダイアログを表示させる
							TouchPanelView.SetAlertDialog(mActivity);
						}
						else if (!this.mTouchMove && (TouchPanelView.GetTouchPositionData(1) > 0 || TouchPanelView.GetTouchPositionData(2) > 0)) {
							// シングルタップまたはダブルタップが有効の場合
							if (TouchPanelView.GetTouchPositionData(2) > 0) {
								// ダブルタップが有効の場合は外部設定を有効にする
								mDoubleTapMode = true;
							}
							else {
								// シングルタップのみの場合
								// タッチパネル設定が有効な場合
								mDoubleTapMode = false;
								SetTouchPanelCommand(1);
							}
						}
						else if (this.mTouchFirst) {
							Logcat.v(logLevel, "mTouchFirst");
							this.mTouchFirst = false;
							mPageBack = false;
							boolean next = checkTapDirectionNext(x, y, cx, cy);
							if (mTapScrl) {
								Logcat.v(logLevel, "タップでスクロール");
								// タップでスクロール
								int move = next ? 1 : -1;
								// 読込中の表示
								startScroll(move);
							} else {
								Logcat.v(logLevel, "タップでスクロールしない");
								// タップでスクロールしない
								// 普通のタッチでページ遷移
								if (next) {
									// 次ページへ
									if (mScrlNext) {
										mImageView.scrollReset();
									}
									nextPage();
								} else {
									// 前ページへ
									if (mScrlNext) {
										mImageView.scrollReset();
									}
									prevPage();
								}
							}
						}
						else {
							Logcat.v(logLevel, "スクロール終了");
							// ■■■ スクロール終了なら
							int flickPage = mImageView.checkFlick();
							if (mFlickPage && flickPage != 0) {
								// 「スクロールで前後のページへ移動する」が無効なら
								// フリックでページ遷移
								if (mFlickEdge && mTouchDrawLeft > mImmCancelRange) {
									// 端からフリックしないときはページめくりしない
									;
								} else if ((flickPage > 0 && mPageWay == DEF.PAGEWAY_RIGHT) || (flickPage < 0 && mPageWay != DEF.PAGEWAY_RIGHT) ? !mChgFlick : mChgFlick) {
									// 次ページへ
									if (mScrlNext) {
										mImageView.scrollReset();
									}
									nextPage();
								} else {
									// 前ページへ
									if (mScrlNext) {
										mImageView.scrollReset();
									}
									prevPage();
								}
							} else if (mMomentMode < DEF.MAX_MOMENTMODE) {
								long now = SystemClock.uptimeMillis();

								int i;
								for (i = 1; i < mTouchPointNum && i < MAX_TOUCHPOINT; i++) {
									if (now - mTouchPointTime[i] > TERM_MOMENT) {
										// 過去0.2秒の範囲
										break;
									}
								}
								if (i >= 3) {
									float sx = mTouchPoint[2].x - mTouchPoint[i - 1].x;
									float sy = mTouchPoint[2].y - mTouchPoint[i - 1].y;
									long term = mTouchPointTime[2] - mTouchPointTime[i - 1];
									// Logcat.d(logLevel, "i=" + i + ", sx=" + sx +
									// ", sy=" + sy + ", term=" + term);
									mImageView.momentiumStart(x, y, mScroll, sx, sy, (int) term, mMomentMode);
								}
							}
						}
					}
					// 押してる間のフラグクリア
					mTouchFirst = false;
					mOperation = TOUCH_NONE;
					this.mTouchMove = false;
					break;
			}
			return	true;
		}
		// タッチイベントで捕捉できない場合はここで処理を行う
		action = event.getAction();
		if (action == MotionEvent.ACTION_MOVE) {
			// タップのスクロール
			if (mClickGuard) {
			}
			else {
				Action_Move_Sub(event);
			}
		}
		if (action == MotionEvent.ACTION_UP) {
			// タップの解除
			if (mClickGuard) {
				mClickGuard = false;
			}
			Action_Up_Sub(event);
		}
		return super.onTouchEvent(event);
	}

	// タップの解除の部分をイベント処理から分離
	private void Action_Up_Sub(MotionEvent event) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "ACTION_UP");
		float x;
		float y;
		int cx;
		int cy;
		if (!mPseLand) {
			x = event.getX();
			y = event.getY();
			cx = mImageView.getWidth();
			cy = mImageView.getHeight();
		}
		else {
			// 疑似横モード
			cx = mImageView.getHeight();
			cy = mImageView.getWidth();
			y = cy - event.getX();
			x = event.getY();
		}
		int result = mGuideView.eventTouchUp((int)x, (int)y);
		mResult = result;
		// 情報表示クリア
		mGuideView.setPageText(null);
		mGuideView.setPageColor(Color.argb(0, 0, 0, 0));
		mGuideView.setGuideIndex(GuideView.GUIDE_NONE);

		if (mPageMode) {
			// ページ選択モード終了
			if (mPageSelect == PAGE_SLIDE) {
				if (y > cy - mClickArea) {
					if (mPageSelect == PAGE_SLIDE || (x < mClickArea || x > cx - mClickArea)) {
						// ページ選択確定
						if (mSelectPage != mCurrentPage) {
							// ページ変更時に振動
							startVibrate();
							mCurrentPage = mSelectPage;
							mPageBack = false;
							if (mScrlNext) {
								mImageView.scrollReset();
							}
							setBitmapImage();
						}
					}
				}
			}
		}
		if (result != -1) {
			int index = (result & 0x7FFF);
			if (mTapEditMode) {
			}
			else if ((result & 0x8000) != 0) {
				// 上部選択の場合は選択リストを表示
				SetViewPause();
				execCommand(mCommandId[index]);
				SetViewUpdate();
			}
			else if (result == 0x4000) {
				// 戻るボタン
				operationBack();
			}
			else if (result == 0x4001) {
				// メニューボタン
				// 独自メニュー表示
				SetViewPause();
				openMenu();
				SetViewUpdate();
			}
			else if (result == 0x4002 || result == 0x4003) {
				// 先頭/末尾ボタン
				mResult = result;

				if (mPageSelect == PAGE_SLIDE) {
					// ページ選択方法が画面下をスワイプのとき
					if (mResult == 0x4003) {
						// 左側ボタン
						int leftpage = mPageWay == DEF.PAGEWAY_RIGHT ? mImageMgr.length() - 1 : 0;
						if (mSelectPage != leftpage) {
							mSelectPage = leftpage;
						}
					}
					else {
						// 右側ボタン
						int rightpage = mPageWay == DEF.PAGEWAY_RIGHT ? 0 : mImageMgr.length() - 1;
						if (mSelectPage != rightpage) {
							mSelectPage = rightpage;
						}
					}
					// ページ選択確定
					if (mSelectPage != mCurrentPage) {
						// ページ変更時に振動
						mCurrentPage = mSelectPage;
						mPageBack = false;
						if (mScrlNext) {
							mImageView.scrollReset();
						}
						setBitmapImage();
					}
				}
				else {
					// ページ選択方法がスライダー表示かサムネイルのとき
					if (mResult == 0x4003) {
						// 左側ボタン
						int leftpage = mPageWay == DEF.PAGEWAY_RIGHT ? mImageMgr.length() - 1 : 0;
						if (mSelectPage != leftpage) {
							mSelectPage = leftpage;
						}
					}
					else {
						// 右側ボタン
						int rightpage = mPageWay == DEF.PAGEWAY_RIGHT ? 0 : mImageMgr.length() - 1;
						if (mSelectPage != rightpage) {
							mSelectPage = rightpage;
						}
					}
					// ページ選択確定
					if (mSelectPage != mCurrentPage) {
						// ページ変更時に振動
						mCurrentPage = mSelectPage;
						mPageBack = false;
						if (mScrlNext) {
							mImageView.scrollReset();
						}
						setBitmapImage();
					}
				}
			}
			else {
				// 下部選択の場合は対応する操作を実行
				switch (index) {
					case 0:
						if (isDualView()) {
							// 1ページ次へずらす
							shiftPage(1);
						}
						break;
					case 1:
						if (isDualView()) {
							// 1ページ前へずらす
							shiftPage(-1);
						}
						break;
					case 2:
						// 次巻(しおり位置)
						// 次のファイルを開き、続きから記録せず、現在頁保存
						finishActivity(CloseDialog.CLICK_NEXT, false, true);
						break;
					case 3:
						// 次巻(先頭ページ)
						// 次のファイルを開き、続きから記録せず、現在頁保存
						finishActivity(CloseDialog.CLICK_NEXTTOP, false, true);
						break;
					case 4:
						// 次巻(最終位置)
						// 次のファイルを開き、続きから記録せず、現在頁保存
						finishActivity(CloseDialog.CLICK_NEXTLAST, false, true);
						break;
					case 5:
						// 前巻(しおり位置)
						// 前のファイルを開き、続きから記録せず、現在頁保存
						finishActivity(CloseDialog.CLICK_PREV, false, true);
						break;
					case 6:
						// 前巻(先頭ページ)
						// 前のファイルを開き、続きから記録せず、現在頁保存
						finishActivity(CloseDialog.CLICK_PREVTOP, false, true);
						break;
					case 7:
						// 前巻(最終ページ)
						// 前のファイルを開き、続きから記録せず、現在頁保存
						finishActivity(CloseDialog.CLICK_PREVLAST, false, true);
						break;
					case 8:
						// 表示中の画像が1枚か2枚かを判定
						ImageData[] bm = mImageView.getImageBitmap();
						int shareType;
						if (bm[0] != null && bm[1] != null) {
							shareType = DEF.SHARE_LR;
						}
						else {
							shareType = DEF.SHARE_SINGLE;
						}
						if (mPageSelect == PAGE_INPUT) {
							// 文書情報を表示
							mGuideView.setPageText(mImageMgr.createPageStr(mSelectPage));
							mGuideView.setPageColor(0x80000000);
							// ページ番号入力
							if (!PageSelectDialog.mIsOpened) {
								PageSelectDialog pageDlg = new PageSelectDialog(this, R.style.MyDialog);
								pageDlg.setParams(DEF.IMAGE_VIEWER, mCurrentPage, mImageMgr.length(), mPageWay == DEF.PAGEWAY_RIGHT, (mImageMgr.getFileType() == mImageMgr.FILETYPE_ZIP || mImageMgr.getFileType() == mImageMgr.FILETYPE_RAR));
								pageDlg.setPageSelectListear(this);
								pageDlg.show();
								pageDlg.setShareType(shareType);
								mPageDlg = pageDlg;
							}
						}
						else if (mPageSelect == PAGE_THUMB) {
							// 文書情報を表示
							mGuideView.setPageText(mImageMgr.createPageStr(mSelectPage));
							mGuideView.setPageColor(0x80000000);
							// サムネイルページ選択
							if (!PageThumbnail.mIsOpened) {
								PageThumbnail thumbDlg = new PageThumbnail(this, R.style.MyDialog);
								thumbDlg.setParams(DEF.IMAGE_VIEWER, mCurrentPage, mPageWay == DEF.PAGEWAY_RIGHT, mImageMgr, mThumID, (mImageMgr.getFileType() == mImageMgr.FILETYPE_ZIP || mImageMgr.getFileType() == mImageMgr.FILETYPE_RAR));								thumbDlg.setPageSelectListear(this);
								thumbDlg.show();
								thumbDlg.setShareType(shareType);
								mThumbDlg = thumbDlg;
							}
						}
						break;
					case 9:
						// 閉じる
						finishActivity( true );
						break;
					case 10:
						// 設定画面に遷移
						if (mImmForce) {
						}
						else if (mImmEnable && mSdkVersion >= 19) {
							int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
							uiOptions &= ~(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
							getWindow().getDecorView().setSystemUiVisibility(uiOptions);
						}
						// バックグラウンドでのキャッシュ読み込み停止
						mImageMgr.setCacheSleep(true);
						Intent intent = new Intent(ImageActivity.this, SetConfigActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivityForResult(intent, DEF.REQUEST_SETTING);
						break;
				}
			}
		}
		else if (mLongTouchMode) {
			// ズーム表示解除
			// startVibrate();
			mImageView.setZoomMode(false);
			mLongTouchMode = false;
		}
	}

	// タップのスクロールの部分をイベント処理から分離
	private void Action_Move_Sub(MotionEvent event) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "ACTION_MOVE");
		float x;
		float y;
		int cx;
		int cy;
		if (!mPseLand) {
			x = event.getX();
			y = event.getY();
			cx = mImageView.getWidth();
			cy = mImageView.getHeight();
		}
		else {
			// 疑似横モード
			cx = mImageView.getHeight();
			cy = mImageView.getWidth();
			y = cy - event.getX();
			x = event.getY();
		}
		if (mOperation == TOUCH_COMMAND) {
			// 移動位置設定
			mGuideView.eventTouchMove((int)x, (int)y);
			if (mPageMode && mPageSelect == PAGE_SLIDE) {
				// スライドページ選択中
				int sel = GuideView.GUIDE_NOSEL;
				if (y >= cy - mClickArea) {
					// 操作エリアから出て戻ったらそこを基準にする
					if (!mPageModeIn) {
						// 指定のページを基準とした位置を設定
						mTouchBeginX = x - calcPageSelectRange(mSelectPage);
					}
					// タッチの位置でページを選択
					if (x < mClickArea) {
						int leftpage = mPageWay == DEF.PAGEWAY_RIGHT ? mImageMgr.length() - 1 : 0;
						if (mSelectPage != leftpage) {
							mSelectPage = leftpage;
							startVibrate();
						}
						sel = GuideView.GUIDE_BLEFT;
					}
					else if (x > cx - mClickArea) {
						int rightpage = mPageWay == DEF.PAGEWAY_RIGHT ? 0 : mImageMgr.length() - 1;
						if (mSelectPage != rightpage) {
							mSelectPage = rightpage;
							startVibrate();
						}
						sel = GuideView.GUIDE_BRIGHT;
					}
					else {
						// 選択中のページ
						mSelectPage = calcSelectPage(x);

						if (mSelectPage < 0) {
							// 最小値は先頭ページ
							mSelectPage = 0;
							// タッチ位置を先頭ページとしたときのCurrentPageの位置を求める
							mTouchBeginX = x - calcPageSelectRange(mSelectPage);
						}
						else if (mSelectPage > mImageMgr.length() - 1) {
							// 最大値は最終ページ
							mSelectPage = mImageMgr.length() - 1;
							// タッチ位置を最終ページとしたときのCurrentPageの位置を求める
							mTouchBeginX = x - calcPageSelectRange(mSelectPage);
						}
						sel = GuideView.GUIDE_BCENTER;
					}
					mPageModeIn = true;
				}
				else {
					mPageModeIn = false;
				}

				// ファイル名＋ページ表示
				String strPage = mImageMgr.createPageStr(mSelectPage);
				String strOld = mGuideView.getPageText();
				if (!strPage.equals(strOld)) {
					if (mCurrentPage - 1 <= mSelectPage && mSelectPage <= mCurrentPage + 1) {
						// ページ変更時に振動
						startVibrate();
					}
					mGuideView.setPageText(strPage);
				}

				mGuideView.setPageColor(mTopColor1);

				// 選択に反映
				mGuideView.setGuideIndex(sel);
			}
		}
		else if (mOperation == TOUCH_OPERATION) {
			// 移動位置設定
			mGuideView.eventTouchMove((int)x, (int)y);
			if (mLongTouchMode) {
				// ズーム表示
				callZoomAreaDraw(x, y);
			}
			else {
				// ページ戻or進、スクロール処理
				if (this.mTouchFirst && ((Math.abs(this.mTouchBeginX - x) > mMoveRange || Math.abs(this.mTouchBeginY - y) > mMoveRange))) {
					// タッチ後に範囲を超えて移動した場合はスクロールモードへ
					this.mTouchFirst = false;
					mLongTouchCount ++;
					// mGuideView.setGuideIndex(mGuideView.GUIDE_NONE,
					// mGuideView.GUIDE_NONE);
					mImageView.scrollStart(mTouchBeginX, mTouchBeginY, RANGE_FLICK, mScroll);
				}

				if (!this.mTouchFirst) {
//				if (this.mTouchFirst == false) {
					// ■■■ スクロール中なら
					// スクロール中のフラグをセット
					this.mTouchMove = true;
					long now = SystemClock.uptimeMillis();
					mImageView.scrollMoveAmount(x - mTouchPoint[0].x, y - mTouchPoint[0].y, mScroll, true);

					for (int i = MAX_TOUCHPOINT - 1; i >= 1; i--) {
						mTouchPoint[i].x = mTouchPoint[i - 1].x;
						mTouchPoint[i].y = mTouchPoint[i - 1].y;
						mTouchPointTime[i] = mTouchPointTime[i - 1];
					}
					mTouchPoint[0].x = x;
					mTouchPoint[0].y = y;
					mTouchPointTime[0] = now;
					if (mTouchPointNum < MAX_TOUCHPOINT) {
						mTouchPointNum++;
					}
				}
			}
		}
	}

	// タップが前/次どちらか判定
	private boolean checkTapDirectionNext(float x, float y, int cx, int cy) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		boolean next = false;

		float rate = mTapRate + 1;
		float rcx = cx / 10.0f;
		float rcy = (cy - mClickArea * 2) / 10.0f;
		switch (mTapPattern) {
			case 0:
				next = (x >= cx - rcx * rate) ? !mChgPage : mChgPage;
				break;
			case 1:
				next = (x <= rcx * rate / 2 || x >= cx - rcx * rate / 2) ? !mChgPage : mChgPage;
				break;
			case 2:
				next = (y > cy - mClickArea - rcy * rate) ? !mChgPage : mChgPage;
				break;
			case 3:
				next = (y > cy - mClickArea - rcy * rate / 2 || y < mClickArea + rcy * rate / 2) ? !mChgPage : mChgPage;
				break;
		}
		return next;
	}

	private void SetTouchPanelCommand(int mode) {

		int index = TouchPanelView.GetTouchPositionData(mode);
		SetTouchPanelCommandMain(index);
	}

	private void SetTouchPanelCommandMain(int index) {

		switch (index) {
			case DEF.TAP_NOSELECT:
				break;
			case DEF.TAP_BACK:
				operationBack();
				break;
			case DEF.TAP_TOOLBARNEXTSCROLL:
				// 次のページへスクロール
				if (mTapScrl) {
					// タップでスクロール
					int move = true ? 1 : -1;
					// 読込中の表示
					startScroll(move);
				} else {
					// タップでスクロールしない
					// 普通のタッチでページ遷移
					// 次ページへ
					if (mScrlNext) {
						mImageView.scrollReset();
					}
					nextPage();
				}
				break;
			case DEF.TAP_TOOLBARPREVSCROLL:
				// 前のページへスクロール
				if (mTapScrl) {
					// タップでスクロール
					int move = false ? 1 : -1;
					// 読込中の表示
					startScroll(move);
				} else {
					// タップでスクロールしない
					// 普通のタッチでページ遷移
					// 前ページへ
					if (mScrlNext) {
						mImageView.scrollReset();
					}
					prevPage();
				}
				break;
			case DEF.TAP_TOOLBARLEFTMOST:
				if (mCurrentPage == (mImageMgr.length() - 1)) {
					nextPage();
				}
				else {
					mCurrentPage = mImageMgr.length() - 1;
					mPageBack = false;
					if (mScrlNext) {
						mImageView.scrollReset();
					}
					setBitmapImage();
				}
				break;
			case DEF.TAP_TOOLBARLEFT100:
				if (mCurrentPage == (mImageMgr.length() - 1)) {
					nextPage();
				}
				else {
					mCurrentPage += 100;
					if (mCurrentPage > (mImageMgr.length() - 1)) {
						mCurrentPage = mImageMgr.length() - 1;
					}
					mPageBack = false;
					if (mScrlNext) {
						mImageView.scrollReset();
					}
					setBitmapImage();
				}
				break;
			case DEF.TAP_TOOLBARLEFT10:
				if (mCurrentPage == (mImageMgr.length() - 1)) {
					nextPage();
				}
				else {
					mCurrentPage += 10;
					if (mCurrentPage > (mImageMgr.length() - 1)) {
						mCurrentPage = mImageMgr.length() - 1;
					}
					mPageBack = false;
					if (mScrlNext) {
						mImageView.scrollReset();
					}
					setBitmapImage();
				}
				break;
			case DEF.TAP_TOOLBARLEFT1:
				if (mCurrentPage == (mImageMgr.length() - 1)) {
					nextPage();
				}
				else {
					if (mCurrentPage < (mImageMgr.length() - 1)) {
						mCurrentPage++;
						mPageBack = false;
						if (mScrlNext) {
							mImageView.scrollReset();
						}
						setBitmapImage();
					}
				}
				break;
			case DEF.TAP_TOOLBARRIGHT1:
				if (mCurrentPage == 0) {
					prevPage();
				}
				else {
					if (mCurrentPage > 0) {
						mCurrentPage--;
						mPageBack = false;
						if (mScrlNext) {
							mImageView.scrollReset();
						}
						setBitmapImage();
					}
				}
				break;
			case DEF.TAP_TOOLBARRIGHT10:
				if (mCurrentPage == 0) {
					prevPage();
				}
				else {
					mCurrentPage -= 10;
					if (mCurrentPage < 0) {
						mCurrentPage = 0;
					}
					mPageBack = false;
					if (mScrlNext) {
						mImageView.scrollReset();
					}
					setBitmapImage();
				}
				break;
			case DEF.TAP_TOOLBARRIGHT100:
				if (mCurrentPage == 0) {
					prevPage();
				}
				else {
					mCurrentPage -= 100;
					if (mCurrentPage < 0) {
						mCurrentPage = 0;
					}
					mPageBack = false;
					if (mScrlNext) {
						mImageView.scrollReset();
					}
					setBitmapImage();
				}
				break;
			case DEF.TAP_TOOLBARRIGHTMOST:
				if (mCurrentPage == 0) {
					prevPage();
				}
				else {
					mCurrentPage = 0;
					mPageBack = false;
					if (mScrlNext) {
						mImageView.scrollReset();
					}
					setBitmapImage();
				}
				break;
			case DEF.TAP_PINCHSCALEUP:
				// ピンチズーム変更
				mPinchScaleSel /= 5;
				mPinchScaleSel *= 5;
				mPinchScaleSel += 5;
				if (mPinchScaleSel > 250) {
					mPinchScaleSel = 250;
				}
				// スケーリングのリアルタイム変更は処理が重いので画面更新時にスケーリングを行う
				mImageView.setPinchChanging(mPinchScaleSel);
				mGuideView.setGuideText(mPinchScaleSel + "%");
				mPinchScaleSetting = true;
				break;
			case DEF.TAP_PINCHSCALEDOWN:
				// ピンチズーム変更
				mPinchScaleSel /= 5;
				mPinchScaleSel *= 5;
				mPinchScaleSel -= 5;
				if (mPinchScaleSel < 10) {
					mPinchScaleSel = 10;
				}
				// スケーリングのリアルタイム変更は処理が重いので画面更新時にスケーリングを行う
				mImageView.setPinchChanging(mPinchScaleSel);
				mGuideView.setGuideText(mPinchScaleSel + "%");
				mPinchScaleSetting = true;
				break;
			case DEF.TAP_TOOLBARBOOKLEFT:
				// 前のファイル(最終ページ)/次のファイル(先頭ページ)
				onSelectPageSelectDialog(DEF.TOOLBAR_BOOK_LEFT);
				break;
			case DEF.TAP_TOOLBARBOOKRIGHT:
				// 次のファイル(先頭ページ)/前のファイル(最終ページ)
				onSelectPageSelectDialog(DEF.TOOLBAR_BOOK_RIGHT);
				break;
			case DEF.TAP_TOOLBARBOOKMARKLEFT:
				// 前(次)のファイル(しおり位置)
				onSelectPageSelectDialog(DEF.TOOLBAR_BOOKMARK_LEFT);
				break;
			case DEF.TAP_TOOLBARBOOKMARKRIGHT:
				// 次(前)のファイル(しおり位置)
				onSelectPageSelectDialog(DEF.TOOLBAR_BOOKMARK_RIGHT);
				break;
			case DEF.TAP_TOOLBARTHUMBSLIDER:
				// サムネイル/スライダー切り替え(イメージビュワーのみ)
				onSelectPageSelectDialog(DEF.TOOLBAR_THUMB_SLIDER);
				break;
			case DEF.TAP_TOOLBARDIRTREE:
				// ディレクトリ選択ダイアログ表示
				execCommand(DEF.MENU_SEL_DIR_TREE);
				break;
			case DEF.TAP_TOOLBARSHARE:
				// 共有
				execCommand(DEF.MENU_SHARE);
				break;
			case DEF.TAP_TOOLBARROTATE:
				// 画面方向切り替え(イメージビュワーのみ)
				showSelectList(SELLIST_SCR_ROTATE);
				break;
			case DEF.TAP_TOOLBARROTATEIMAGE:
				// 画像の回転(イメージビュワーのみ)
				showSelectList(SELLIST_IMG_ROTATE);
				break;
			case DEF.TAP_TOOLBARSELECTTHUM:
				// サムネイルに設定
				execCommand(DEF.MENU_SETTHUMB);
				break;
			case DEF.TAP_TOOLBARTRIMTHUMB:
				//切り出してサムネイルに設定
				execCommand(DEF.MENU_SETTHUMBCROPPED);
				break;
			case DEF.TAP_TOOLBARMENU:
				// ツールバー編集ダイアログ表示
				execCommand(DEF.MENU_EDIT_TOOLBAR);
				break;
			case DEF.TAP_TOOLBARCONFIG:
				// 設定画面に遷移
				execCommand(DEF.MENU_SETTING);
				break;
			case DEF.TAP_PULLDOWNMENU:
				// プルダウンメニューに遷移
				// 独自メニュー表示
				openMenu();
				break;
			case DEF.TAP_SCRLWAY00:
				mScrlWay = DEF.SCRLWAY_V;
				break;
			case DEF.TAP_SCRLWAY01:
				mScrlWay = DEF.SCRLWAY_H;
				break;
			case DEF.TAP_TOOLBARDISPLAYPOSITION00:
			case DEF.TAP_TOOLBARDISPLAYPOSITION01:
			case DEF.TAP_TOOLBARDISPLAYPOSITION02:
			case DEF.TAP_TOOLBARDISPLAYPOSITION03:
			case DEF.TAP_TOOLBARDISPLAYPOSITION04:
			case DEF.TAP_TOOLBARDISPLAYPOSITION05:
			case DEF.TAP_TOOLBARDISPLAYPOSITION06:
			case DEF.TAP_TOOLBARDISPLAYPOSITION07:
			case DEF.TAP_TOOLBARDISPLAYPOSITION08:
				// 画面の表示位置
				// 通し番号なのでオフセットを演算する
				mDisplayPosition = index - DEF.TAP_TOOLBARDISPLAYPOSITION00;
				// 表示のコンフィグレーション
				setViewConfig();
				// 表示を更新
				setImageConfig();
				setBitmapImage();
				break;
			case DEF.TAP_SELROTA00:
			case DEF.TAP_SELROTA01:
			case DEF.TAP_SELROTA02:
			case DEF.TAP_SELROTA03:
				// 画像の回転
				// 通し番号なのでオフセットを演算する
				mRotate = index - DEF.TAP_SELROTA00;
				mImageView.setRotate(mRotate);
				setMgrConfig(false);
				setBitmapImage();
				break;
			case DEF.TAP_SELSIZE00:
			case DEF.TAP_SELSIZE01:
			case DEF.TAP_SELSIZE02:
			case DEF.TAP_SELSIZE03:
			case DEF.TAP_SELSIZE04:
			case DEF.TAP_SELSIZE05:
			case DEF.TAP_SELSIZE06:
			case DEF.TAP_SELSIZE07:
				// 画像拡大率の変更
				mImageView.lockDraw();
				// 通し番号なのでオフセットを演算する
				ChangeScale(SCALENAME_ORDER[index - DEF.TAP_SELSIZE00]);
				mImageView.update(true);
				break;
			case DEF.TAP_SELVIEW00:
			case DEF.TAP_SELVIEW01:
			case DEF.TAP_SELVIEW02:
			case DEF.TAP_SELVIEW03:
			case DEF.TAP_SELVIEW04:
				// 見開き設定変更
				// 通し番号なのでオフセットを演算する
				mDispMode = index - DEF.TAP_SELVIEW00;
				setImageConfig();
				setBitmapImage();
				break;
			case DEF.TAP_MGNCUTMENU:
				// 余白削除
				execCommand(DEF.MENU_MGNCUT);
				break;
			case DEF.TAP_MGNCUTCOLORMENU:
				// 余白削除の色
				execCommand(DEF.MENU_MGNCUTCOLOR);
				break;
			case DEF.TAP_IMGCONFMENU:
				// 画像表示設定
				execCommand(DEF.MENU_IMGCONF);
				break;
			case DEF.TAP_TGUIDE02:
				// 見開き設定
				execCommand(DEF.MENU_IMGVIEW);
				break;
			case DEF.TAP_TGUIDE03:
				// 画像サイズ
				execCommand(DEF.MENU_IMGSIZE);
				break;
			case DEF.TAP_NOISEMENU:
				// マイク開始
				execCommand(DEF.MENU_NOISE);
				break;
			case DEF.TAP_PLAYMENU:
				// オートプレイ中の設定
				execCommand(DEF.MENU_AUTOPLAY);
				break;
			case DEF.TAP_ADDBOOKMARKMENU:
				// ブックマーク追加ダイアログ表示
				execCommand(DEF.MENU_ADDBOOKMARK);
				break;
			case DEF.TAP_SELBOOKMARKMENU:
				// ブックマーク選択ダイアログ表示
				execCommand(DEF.MENU_SELBOOKMARK);
				break;
			case DEF.TAP_SHARPENMENU:
				// シャープ化
				execCommand(DEF.MENU_SHARPEN);
				break;
			case DEF.TAP_MOIREMENU:
				// モアレ軽減
				mGray = mGray ? false : true;
				setImageConfig();
				setBitmapImage();
				break;
			case DEF.TAP_BRIGHTMENU:
				// 明るさ補正
				execCommand(DEF.MENU_BRIGHT);
				break;
			case DEF.TAP_GAMMAMENU:
				// ガンマ補正
				execCommand(DEF.MENU_GAMMA);
				break;
			case DEF.TAP_CONTRASTMENU:
				// コントラスト
				execCommand(DEF.MENU_CONTRAST);
				break;
			case DEF.TAP_HUEMENU:
				// 色相
				execCommand(DEF.MENU_HUE);
				break;
			case DEF.TAP_SATURATIONMENU:
				// 彩度
				execCommand(DEF.MENU_SATURATION);
				break;
			case DEF.TAP_BKLIGHTMENU:
				// バックライト
				execCommand(DEF.MENU_BKLIGHT);
				break;
			case DEF.TAP_INVERTMENU:
				// 白黒反転
				execCommand(DEF.MENU_INVERT);
				break;
			case DEF.TAP_GRAYMENU:
				// グレースケール
				execCommand(DEF.MENU_GRAY);
				break;
			case DEF.TAP_COLORINGMENU:
				// 自動着色
				execCommand(DEF.MENU_COLORING);
				break;
			case DEF.TAP_ALGORIMENU:
				// 画像補間方式
				execCommand(DEF.MENU_IMGALGO);
				break;
			case DEF.TAP_REVERSEMENU:
				// ページを逆順にする
				execCommand(DEF.MENU_REVERSE);
				break;
			case DEF.TAP_CHGOPEMENU:
				// 操作方向の入れ替え
				execCommand(DEF.MENU_CHG_OPE);
				break;
			case DEF.TAP_PAGEWAYMENU:
				// ページめくり方向の入れ替え
				execCommand(DEF.MENU_PAGEWAY);
				break;
			case DEF.TAP_SCRLWAY2MENU:
				// スクロール方向の入れ替え
				execCommand(DEF.MENU_SCRLWAY);
				break;
			case DEF.TAP_SETTOPMENU:
				// 上部の設定
				execCommand(DEF.MENU_TOP_SETTING);
				break;
			case DEF.TAP_CMARGIN:
				// 中央に余白を表示
				execCommand(DEF.MENU_CMARGIN);
				break;
			case DEF.TAP_CSHADOW:
				// 中央に影を表示
				execCommand(DEF.MENU_CSHADOW);
				break;
			case DEF.TAP_DISPLAYPOSITIONMENU:
				// 画面の表示位置
				showSelectList(SELLIST_DISPLAY_POSITION);
				break;
			case DEF.TAP_SETPROFILE:
				// プロファイルの登録
				execCommand(DEF.MENU_SETPROFILE);
				break;
			case DEF.TAP_DELPROFILE:
				// プロファイルの削除
				execCommand(DEF.MENU_DELPROFILE);
				break;
			case DEF.TAP_PROFILE1:
				// プロファイル1
				execCommand(DEF.MENU_PROFILE1);
				break;
			case DEF.TAP_PROFILE2:
				// プロファイル2
				execCommand(DEF.MENU_PROFILE2);
				break;
			case DEF.TAP_PROFILE3:
				// プロファイル3
				execCommand(DEF.MENU_PROFILE3);
				break;
			case DEF.TAP_PROFILE4:
				// プロファイル4
				execCommand(DEF.MENU_PROFILE4);
				break;
			case DEF.TAP_PROFILE5:
				// プロファイル5
				execCommand(DEF.MENU_PROFILE5);
				break;
			case DEF.TAP_EXIT_VIEWER:
				finishActivity(true);
				break;
		}
	}

	private int mSelectMode;

	private void showSelectList(int index) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mListDialog != null) {
			return;
		}
		// if (index < 0 || index > DEF.GUIDE_TOP_NUM) {
		// // インデックスが範囲外
		// return;
		// }
		// 再読み込みになるのでページ戻は解除
		mPageBack = false;

		Resources res = getResources();

		// 選択対象
		mSelectMode = index;

		// 選択肢を設定
		String[] items = null;
		int nItem;

		String title;
		int selIndex;
		switch (index) {
			case SELLIST_ALGORITHM:
				// 画像補間法の選択肢設定
				title = res.getString(R.string.algoriMenu);
				selIndex = mAlgoMode;
				nItem = SetImageActivity.AlgoModeName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.AlgoModeName[i]);
				}
				break;
			case SELLIST_IMG_ROTATE:
				// 回転の選択肢設定
				title = res.getString(R.string.imgRotaMenu);
				selIndex = mRotate;
				nItem = SetImageActivity.ImgRotaName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.ImgRotaName[i]);
				}
				break;
			case SELLIST_VIEW_MODE:
				// 見開きモードの選択肢設定
				title = res.getString(R.string.tguide02);
				selIndex = mDispMode;
				nItem = SetImageActivity.ViewName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.ViewName[i]);
				}
				break;
			case SELLIST_SCALE_MODE:
				// サイズ設定の選択肢設定
				title = res.getString(R.string.tguide03);
				selIndex = 0;
				for (int i = 0; i < SCALENAME_ORDER.length; i++) {
					if (SCALENAME_ORDER[i] == mScaleMode) {
						selIndex = i;
						break;
					}
				}
				nItem = SetImageActivity.ScaleName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.ScaleName[SCALENAME_ORDER[i]]);
				}
				break;
			case SELLIST_MARGIN_CUT:
				// 余白削除
				title = res.getString(R.string.mgnCutMenu);
				selIndex = mMgnCut;
				nItem = SetImageActivity.MgnCutName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.MgnCutName[i]);
				}
				break;
			case SELLIST_MARGIN_CUTCOLOR:
				// 余白削除
				title = res.getString(R.string.mgnCutColorMenu);
				selIndex = mMgnCutColor;
				nItem = SetImageActivity.MgnCutColorName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.MgnCutColorName[i]);
				}
				break;
			case SELLIST_DISPLAY_POSITION:
				// 画面の表示位置
				title = res.getString(R.string.DisplayPositionMenu);
				selIndex = mDisplayPosition;
				nItem = SetImageActivity.DisplayPositionName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.DisplayPositionName[i]);
				}
				break;
			case SELLIST_SCR_ROTATE:
				// 画面方向
				title = res.getString(R.string.rotateMenu);
				selIndex = mViewRota - 1;
				nItem = SetImageActivity.RotateName.length - 1;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.RotateName[i + 1]);
				}
				break;
			case SELLIST_SETPROFILE:
				// プロファイル登録
				title = res.getString(R.string.SetProfileMenu);
				// カーソルに色を付けない
				selIndex = 5;
				nItem = SetImageActivity.SetProfileName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					if (mProfileWord[i].equals("")) {
						// 中身が未定義ならデフォルト設定のみ
						items[i] = res.getString(SetImageActivity.SetProfileName[i]);
					}
					else {
						// 後半に中身を追加
						items[i] = res.getString(SetImageActivity.SetProfileName[i]) + " : " + mProfileWord[i];
					}
				}
				break;
			case SELLIST_DELPROFILE:
				// プロファイル削除
				title = res.getString(R.string.DelProfileMenu);
				// カーソルに色を付けない
				selIndex = 5;
				nItem = SetImageActivity.SetProfileName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					if (mProfileWord[i].equals("")) {
						// 中身が未定義なら登録しない
					}
					else {
						// 後半に中身を追加
						items[i] = res.getString(SetImageActivity.SetProfileName[i]) + " : " + mProfileWord[i];
					}
				}
				break;
			default:
				return;
		}
		mListDialog = new ListDialog(this, R.style.MyDialog, title, items, selIndex, new ListSelectListener() {
			@Override
			public void onSelectItem(int index) {
				switch (mSelectMode) {
					case SELLIST_SETPROFILE:
						String title = res.getString(SetImageActivity.SetProfileName[index]);
						String message = res.getString(R.string.pfMsg);
						String profilename = mProfileWord[index];
						mTextInputDialog = new TextInputDialog(mActivity, R.style.MyDialog, title, null, message, profilename, new TextInputDialog.SearchListener() {
							@Override
							public void onSearch(String text) {
								if (text != null && text.length() > 0) {
									// テキストが有効だった場合は値を更新
									mProfileWord[index] = text;
									// プロファイルを保存
									SaveProfile(index);
									boolean[] states = loadTopMenuState();
									// 上部メニューの設定を保存
									saveTopMenuState(states);
									// 読み込みなおし
									loadTopMenuState();
									if (mGuideView != null) {
										// 上部メニューの文字列情報をガイドに設定
										mGuideView.setTopCommandStr(mCommandStr);
									}
								}
							}
							@Override
							public void onCancel() {
								// キャンセル処理
							}
							@Override
							public void onClose() {
								// 終了
								mTextInputDialog = null;
							}
						});
						mTextInputDialog.show();
						break;
					case SELLIST_DELPROFILE:
						if (mProfileWord[index].equals("")) {
							// 登録されていなければ戻る
							return;
						}
						Dialog dialog = null;
						AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity, R.style.MyDialog);
						dialogBuilder.setTitle(getString(R.string.DelProfileName, mProfileWord[index]));
						dialogBuilder.setMessage(R.string.delPfMsg);
						dialogBuilder.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// プロファイルの削除
								DeleteProfile(index);
								boolean[] states = loadTopMenuState();
								// 上部メニューの設定を保存
								saveTopMenuState(states);
								// 読み込みなおし
								loadTopMenuState();
								if (mGuideView != null) {
									// 上部メニューの文字列情報をガイドに設定
									mGuideView.setTopCommandStr(mCommandStr);
								}
								dialog.dismiss();
							}
						});
						dialogBuilder.setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// キャンセル時は何もしない
								dialog.dismiss();
							}
						});
						// ダイアログの表示
						dialog = dialogBuilder.create();
						dialog.show();
						break;
					case SELLIST_ALGORITHM:
						// 画像補間法
						if (mAlgoMode != index) {
							mAlgoMode = index;
							setImageConfig();
							setBitmapImage();
						}
						break;
					case SELLIST_IMG_ROTATE:
						// 回転
						if (mRotate != index && index >= 0 && index < 4) {
							// 角度を変更した
							mRotate = index;
							mImageView.setRotate(mRotate);
							setMgrConfig(false);
							setBitmapImage();
						}
						break;
					case SELLIST_VIEW_MODE:
						// 見開き設定変更
						if (mDispMode != index) {
							mDispMode = index;
							setImageConfig();
							setBitmapImage();
						}
						break;
					case SELLIST_SCALE_MODE: {
						// 画像拡大率の変更
						mImageView.lockDraw();
						ChangeScale(SCALENAME_ORDER[index]);
						mImageView.update(true);
						break;
					}
					case SELLIST_MARGIN_CUT:
						// 余白削除
						if (mMgnCut != index) {
							mMgnCut = index;
							setImageConfig();
							setBitmapImage();
						}
						break;
					case SELLIST_MARGIN_CUTCOLOR:
						// 余白削除の色
						if (mMgnCutColor != index) {
							mMgnCutColor = index;
							setImageConfig();
							setBitmapImage();
						}
						break;
					case SELLIST_DISPLAY_POSITION:
						// 画面の表示位置
						if (mDisplayPosition != index) {
							mDisplayPosition = index;
							// 表示のコンフィグレーション
							setViewConfig();
							// 表示を更新
							setImageConfig();
							setBitmapImage();
						}
						break;
					case SELLIST_SCR_ROTATE:
						// 画面方向
						if (mViewRota != index + 1) {
							int prevRota = mViewRota;
							mViewRota = index + 1;
							DEF.setRotation(mActivity, mViewRota);
							if (mViewRota == DEF.ROTATE_PSELAND) {
								// 疑似横画面
								mPseLand = true;
							}
							else {
								mPseLand = false;
							}

							setMgrConfig(true);
							setViewConfig();

							boolean isPrePort = true;
							boolean isAftPort = true;

							if (prevRota == DEF.ROTATE_LANDSCAPE) {
								isPrePort = false;
							}
							else if (prevRota == DEF.ROTATE_AUTO) {
								if (DEF.checkPortrait(mViewWidth, mViewHeight) == false) {
									isPrePort = false;
								}
							}

							if (mViewRota == DEF.ROTATE_LANDSCAPE) {
								isAftPort = false;
							}

							if (isPrePort == isAftPort) {
								// 変化がないとき
								mImageView.updateScreenSize();
							}
						}
						break;
				}
			}

			@Override
			public void onClose() {
				// 終了
				mListDialog = null;
			}
		});
		mListDialog.show();
		return;
	}

	private void showImageConfigDialog(int command_id) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mImageConfigDialog != null) {
			return;
		}
		mImageConfigDialog = new ImageConfigDialog(this, R.style.MyDialog, command_id, false, this);

		// 画像サイズの選択項目を求める
		int selIndex = 0;
		for (int i = 0; i < SCALENAME_ORDER.length; i++) {
			if (SCALENAME_ORDER[i] == mScaleMode) {
				selIndex = i;
				break;
			}
		}
		mImageConfigDialog.setConfig(mGray, mInvert, mMoire, mTopSingle, mSharpen, mBright, mGamma, mBkLight, mAlgoMode, mDispMode, selIndex, mMgnCut, mMgnCutColor, mIsConfSave, mDisplayPosition, mContrast, mHue, mSaturation, mColoring);
		mImageConfigDialog.setImageConfigListner(new ImageConfigListenerInterface() {
			@Override
			public void onButtonSelect(int select, boolean gray, boolean invert, boolean moire, boolean topsingle, int sharpen, int bright, int gamma, int bklight, int algomode, int dispmode, int scalemode, int mgncut, int mgncutcolor, boolean issave, int displayposition, int contrast, int hue, int saturation, boolean coloring) {
				// 選択状態を通知
				boolean ischange = false;
				// 変更があるかを確認(適用後のキャンセルの場合も含む)
				if (mGray != gray || mInvert != invert || mMoire != moire || mTopSingle != topsingle || mSharpen != sharpen || mBright != bright || mGamma != gamma || mAlgoMode != algomode || mDispMode != dispmode || mMgnCut != mgncut || mMgnCutColor != mgncutcolor || mDisplayPosition != displayposition || mContrast != contrast || mHue != hue || mSaturation != saturation || mColoring != coloring) {
					ischange = true;
				}
				mGray = gray;
				mColoring = coloring;
				mInvert = invert;
				mMoire = moire;
				mTopSingle = topsingle;
				mSharpen = sharpen;
				mBright = bright;
				mGamma = gamma;
				mContrast = contrast;
				mHue = hue;
				mSaturation = saturation;
				mAlgoMode = algomode;
				mMgnCut = mgncut;
				mMgnCutColor = mgncutcolor;
				mIsConfSave = issave;
				if (mDisplayPosition != displayposition) {
					mDisplayPosition = displayposition;
					// 画面の表示位置
					// 表示のコンフィグレーション
					setViewConfig();
					// 表示を更新
					setImageConfig();
					setBitmapImage();
					// イメージ拡大縮小
					ImageScaling();
					updateOverSize(false);
				}
				if (mScaleMode != SCALENAME_ORDER[scalemode]) {
					// 画像拡大率の変更
					mImageView.lockDraw();
					ChangeScale(SCALENAME_ORDER[scalemode]);
					ischange = true;
				}
				if (mBkLight != bklight) {
					// バックライト変更
					mBkLight = bklight;

					float l = -1;
					if (mBkLight <= 10) {
						l = (float)mBkLight / 10;
					}
					WindowManager.LayoutParams lp = getWindow().getAttributes();
					lp.screenBrightness = l;
					getWindow().setAttributes(lp);
				}
				if (mDispMode != dispmode) {
					mDispMode = dispmode;
					// 表示を更新
					setImageConfig();
					setBitmapImage();

					// 操作ガイドの設定
					mGuideView.setGuideMode(isDualView() == true, mBottomFile, mPageWay == DEF.PAGEWAY_RIGHT, mPageSelect, mImmEnable | mImmForce);
				}
				else if (ischange) {
					// 表示を更新
					mImageView.lockDraw();
					setImageConfig();
					mImageView.update(true);
				}

				if (issave) {
					// 設定を指定
					Editor ed = mSharedPreferences.edit();
					ed.putBoolean(DEF.KEY_GRAY, mGray);
					ed.putBoolean(DEF.KEY_COLORING, mColoring);
					ed.putBoolean(DEF.KEY_INVERT, mInvert);
					ed.putBoolean(DEF.KEY_MOIRE, mMoire);
					ed.putBoolean(DEF.KEY_TOPSINGLE, mTopSingle);
					ed.putString(DEF.KEY_SHARPEN, Integer.toString(mSharpen));
					ed.putString(DEF.KEY_BRIGHT, Integer.toString(mBright));
					ed.putString(DEF.KEY_GAMMA, Integer.toString(mGamma));
					ed.putString(DEF.KEY_BKLIGHT, Integer.toString(mBkLight));
					ed.putString(DEF.KEY_CONTRAST, Integer.toString(mContrast));
					ed.putString(DEF.KEY_HUE, Integer.toString(mHue));
					ed.putString(DEF.KEY_SATURATION, Integer.toString(mSaturation));
					ed.putString(DEF.KEY_ALGOMODE, Integer.toString(mAlgoMode));
					ed.putString(DEF.KEY_INITVIEW, Integer.toString(mDispMode));
					ed.putString(DEF.KEY_MARGINCUT, Integer.toString(mMgnCut));
					ed.putString(DEF.KEY_MARGINCUTCOLOR, Integer.toString(mMgnCutColor));
					ed.putString(DEF.KEY_INISCALE, Integer.toString(mScaleMode));
					ed.putString(DEF.KEY_DISPLAYPOSITION, Integer.toString(mDisplayPosition));
					
					ed.apply();
				}
			}

			@Override
			public void onClose() {
				// 終了
				mImageConfigDialog = null;
			}
		});
		mImageConfigDialog.show(getSupportFragmentManager(), TabDialogFragment.class.getSimpleName());
	}

	private void showCheckList() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mCheckDialog != null) {
			return;
		}

		Resources res = getResources();

		// 選択肢を設定
		String[] items = null;
		int nItem;

		String title;

		// 画像補間法の選択肢設定
		title = res.getString(R.string.setTopMenu);
		nItem = COMMAND_ID.length;
		items = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			if (COMMAND_INDEX[i] >= LIST_PROFILE1 && COMMAND_INDEX[i] <= LIST_PROFILE5) {
				// プロファイルの場合は個別対応
				if (mProfileWord[COMMAND_INDEX[i] - LIST_PROFILE1].equals("")) {
					// 中身が未定義ならデフォルト設定
					Logcat.d(logLevel, "中身が未定義ならデフォルト設定");
					items[i] = res.getString(COMMAND_RES[i]).replaceAll("\\(%\\)", "");
				}
				else {
					items[i] = mProfileWord[COMMAND_INDEX[i] - LIST_PROFILE1];
				}
			}
			else {
				items[i] = res.getString(COMMAND_RES[i]).replaceAll("\\(%\\)", "");
			}
		}

		boolean[] states = loadTopMenuState();

		mCheckDialog = new CheckDialog(this, R.style.MyDialog, title, states, items, new CheckListener() {
			@Override
			public void onSelected(boolean[] states) {
				// 選択完了
				saveTopMenuState(states);
				// 読み込みなおし
				loadTopMenuState();
				if (mGuideView != null) {
					// 上部メニューの文字列情報をガイドに設定
					mGuideView.setTopCommandStr(mCommandStr);
				}
			}

			@Override
			public void onClose() {
				// 終了
				mCheckDialog = null;
			}
		});
		mCheckDialog.show();
		return;
	}

	// 上部メニューの設定を読み込み
	private boolean[] loadTopMenuState() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		boolean[] states = null;
		try {
			Resources res = getResources();

			Logcat.d(logLevel, "保存された設定を取得します.");
			states = new boolean[COMMAND_INDEX.length];
			int count = 0;
			for (int i = 0; i < states.length; i++) {
				try {
					states[i] = mSharedPreferences.getBoolean(DEF.KEY_TOPMENU + COMMAND_INDEX[i], COMMAND_INDEX[i] < 4 ? true : false);
					if (states[i]) {
						// 表示する個数
						count++;
					}
				}
				catch (Exception e) {
					Logcat.e(logLevel, "ループ1でエラーが発生しました.", e);
					Logcat.e(logLevel, "COMMAND_INDEX[" + i + "]=" + COMMAND_INDEX[i]);
				}
			}

			Logcat.d(logLevel, "表示するコマンドを設定します.");
			mCommandId = new int[count];
			mCommandStr = new String[count];
			count = 0;
			for (int i = 0; i < states.length; i++) {
				try {
					if (states[i]) {
						// 表示するコマンドを設定
						mCommandId[count] = COMMAND_ID[i];
						if (COMMAND_INDEX[i] >= LIST_PROFILE1 && COMMAND_INDEX[i] <= LIST_PROFILE5) {
							// プロファイルの場合は個別対応
							if (mProfileWord[COMMAND_INDEX[i] - LIST_PROFILE1].equals("")) {
								// 中身が未定義ならデフォルト設定
								Logcat.d(logLevel, "中身が未定義ならデフォルト設定");
								mCommandStr[count] = res.getString(COMMAND_RES[i]).replaceAll("\\(%\\)", "");
							}
							else {
								mCommandStr[count] = mProfileWord[COMMAND_INDEX[i] - LIST_PROFILE1];
							}
						}
						else {
							mCommandStr[count] = res.getString(COMMAND_RES[i]).replaceAll("\\(%\\)", "");
						}
						count++;
					}
				}
				catch (Exception e) {
					Logcat.e(logLevel, "ループ2でエラーが発生しました.", e);
					Logcat.e(logLevel, "COMMAND_INDEX[" + i + "]=" + COMMAND_INDEX[i]);
				}
			}
			Logcat.d(logLevel, "終了します.");
		}
		catch (Exception e) {
			Logcat.e(logLevel, "エラーが発生しました.", e);
		}
		Logcat.d(logLevel, " 終了します.");
		return states;
	}

	// 上部メニューの設定を保存
	private void saveTopMenuState(boolean[] states) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. states.length=" + states.length);

		Editor ed = mSharedPreferences.edit();
		for (int i = 0 ; i < states.length ; i ++) {
			try {
				ed.putBoolean(DEF.KEY_TOPMENU + COMMAND_INDEX[i], states[i]);
			}
			catch (Exception e) {
				Logcat.e(logLevel, "エラーが発生しました.", e);
			}
		}
		ed.apply();
		Logcat.d(logLevel, "終了します.");
	}

	// 座標から選択するページを求める
	private int calcSelectPage(float x) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		int page = mCurrentPage;
		int pagecnt = 0;
		int range = (int) Math.abs((x - mTouchBeginX)); // 絶対値
		int sign = x < mTouchBeginX ? -1 : 1; // ページ方向
		// 右表紙なら逆転させる
		sign *= mPageWay == DEF.PAGEWAY_RIGHT ? -1 : 1;

		for (int i = 0; i < CTL_COUNT.length; i++) {
			if (range <= mPageRange * (CTL_COUNT[i] * CTL_RANGE[i])) {
				// 左右3単位分までページ変化なし
				page = mCurrentPage + (pagecnt + range / (mPageRange * CTL_RANGE[i])) * sign;
				break;
			}
			// 移動範囲から減らす
			range -= mPageRange * CTL_COUNT[i] * CTL_RANGE[i];
			// その分のページ数を加算
			pagecnt += CTL_COUNT[i];
		}
		return page;
	}

	// ページ選択時に表示する文字列を作成
	private float calcPageSelectRange(int page) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		int pagecnt = Math.abs(mCurrentPage - page); // ページの差の絶対値
		int range = 0;

		for (int i = 0; i < CTL_COUNT.length; i++) {
			if (pagecnt <= CTL_COUNT[i]) {
				// 半端分を計算
				range += pagecnt * (mPageRange * CTL_RANGE[i]);
				break;
			}
			// 移動範囲から減らす
			range += CTL_COUNT[i] * (mPageRange * CTL_RANGE[i]);

			// その分のページ数を加算
			pagecnt -= CTL_COUNT[i];
		}
		// 方向を設定
		return range * (mCurrentPage <= page ? 1 : -1) * (mPageWay == DEF.PAGEWAY_RIGHT ? -1 : 1);
	}

	/**
	 * 画像と表示領域を比較し、はみ出る量を算出します。
	 */
	public void setImageConfig() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mImageMgr != null) {
			mImageMgr.setViewSize(mViewWidth, mViewHeight);
			setMgrConfig(false);
			ImageScaling();
			// setBitmapImage();
		}
		return;
	}

	/**
	 * 画像と表示領域を比較し、はみ出る量を算出します。
	 */
	public void setImageViewSize(int width, int height) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		mViewWidth = width;
		mViewHeight = height;
		if (mImageMgr != null) {
			mImageMgr.setViewSize(mViewWidth, mViewHeight);
			ImageScaling();

			// 初回のリスト読み込み中は表示不要
			if (!mListLoading) {
				// 縦横で単ページと見開き切替える場合
				if (mDispMode == DEF.DISPMODE_IM_EXCHANGE) {
					// イメージ拡大縮小
					updateOverSize(false);
					setBitmapImage();
				}
			}
		}
	}

	private void ImageScaling() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		int page1 = -1;
		int page2 = -1;
		int half1 = ImageData.HALF_NONE;
		int half2 = ImageData.HALF_NONE;
		if (mSourceImage[0] != null) {
			page1 = mSourceImage[0].Page;
			half1 = mSourceImage[0].HalfMode;
			if (mSourceImage[1] != null) {
				page2 = mSourceImage[1].Page;
				half2 = mSourceImage[1].HalfMode;
			}
			mImageMgr.ImageScalingSync(page1, page2, half1, half2, mSourceImage[0], mSourceImage[1]);
		}
	}

	/**
	 * 画像と表示領域を比較し、はみ出る量を算出します。
	 */
	private void updateOverSize(boolean isResize) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		// ルーペの大きさを再設定
		mImageView.updateZoomView();

		// ビットマップを調整
		mImageView.updateOverSize(mPageBack, isResize);
	}

	// // オプションメニューが表示される度に呼び出されます
	// @Override
	// public boolean onPrepareOptionsMenu(Menu menu2) {
	// boolean ret = super.onPrepareOptionsMenu(menu2);
	// setOptionMenu(menu);
	// }

	// 戻る操作
	private void operationBack() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mTapEditMode) {
			// タップ操作の設定の編集中だった場合は解除して戻る
			mTapEditMode = false;
			mImageView.ViewTapSw(false);
			SetViewUpdate();
			return;
		}
		if (mGuideView.getOperationMode()) {
			mGuideView.setOperationMode(false);
			return;
		}
		else if (mListLoading || (mBitmapLoading && !mConfirmBack)) {
			if (mImageMgr != null) {
				mImageMgr.setBreakTrigger();
			}
			mTerminate = true;

			long now = SystemClock.uptimeMillis();
			int t = (int) (now - mEffectStart);
			if (mEffect != 0 && t >= mEffectTime) {
				// なぜか固まることがあるので暫定で実施
				// エフェクト中でない場合は mBitmapLoading を終了させる
				startViewTimer(DEF.HMSG_EVENT_EFFECT_NEXT);
			}

			return;
		}
		else if (mConfirmBack) {
			// 戻るで確認表示
			showCloseDialog(CloseDialog.LAYOUT_BACK);
		}
		else {
			finishActivity(true);
		}
		return;
	}

	// メニューを開く
	private void openMenu() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mImageMgr == null || mImageView == null || mMenuDialog != null) {
			return;
		}

		if (mAutoPlay) {
			// オートプレイ中は解除
			setAutoPlay(false);
		}

		Resources res = getResources();
		TabDialogFragment mMenuDialog = new TabDialogFragment(this, R.style.MyDialog, true, this);

		// 操作カテゴリ
		mMenuDialog.addSection(res.getString(R.string.operateSec));
		if (mImageMgr.getFileType() == mImageMgr.FILETYPE_ZIP || mImageMgr.getFileType() == mImageMgr.FILETYPE_RAR) {
			// ディレクトリ選択
			mMenuDialog.addItem(DEF.MENU_SEL_DIR_TREE, res.getString(R.string.selDirTreeMenu));
		}
		// ブックマーク選択
		mMenuDialog.addItem(DEF.MENU_SELBOOKMARK, res.getString(R.string.selBookmarkMenu));
		// ブックマーク追加
		mMenuDialog.addItem(DEF.MENU_ADDBOOKMARK, res.getString(R.string.addBookmarkMenu));
		// // ブックマーク選択
		// mMenuDialog.addItem(DEF.MENU_SELBOOKMARK,
		// res.getString(R.string.selBookmarkMenu));
		// // ページ選択
		// mMenuDialog.addItem(DEF.MENU_PAGESEL,
		// res.getString(R.string.pageselMenu));
		// 音操作
		mMenuDialog.addItem(DEF.MENU_NOISE, res.getString(R.string.noiseMenu), FileSelectActivity.GetRecordSw());
		// 自動再生
		mMenuDialog.addItem(DEF.MENU_AUTOPLAY, res.getString(R.string.playMenu));
		// 画面回転
		// if (mViewRota == DEF.ROTATE_PORTRAIT || mViewRota ==
		// DEF.ROTATE_LANDSCAPE) {
		mMenuDialog.addItem(DEF.MENU_ROTATE, res.getString(R.string.rotateMenu));
		// }
		ImageData[] bm = mImageView.getImageBitmap();
		if (bm[0] != null && bm[1] != null) {
			// 共有 (右画像)
			mMenuDialog.addItem(DEF.MENU_SHARER, res.getString(R.string.shareRMenu));
			// 共有 (左画像)
			mMenuDialog.addItem(DEF.MENU_SHAREL, res.getString(R.string.shareLMenu));
		}
		else {
			// 共有
			mMenuDialog.addItem(DEF.MENU_SHARE, res.getString(R.string.shareMenu));
		}
		// 共有一時ファイル削除
		mMenuDialog.addItem(DEF.MENU_DELSHARE, res.getString(R.string.delshareMenu));
		mMenuDialog.addItem(DEF.MENU_SETTHUMB, res.getString(R.string.setThumb));
		mMenuDialog.addItem(DEF.MENU_SETTHUMBCROPPED, res.getString(R.string.setThumbCropped));

		// 一時設定
		mMenuDialog.addSection(res.getString(R.string.settingSec));
		// イメージ表示設定
		mMenuDialog.addItem(DEF.MENU_IMGCONF, res.getString(R.string.imgConfMenu));
//		// 画像補間方式
//		mMenuDialog.addItem(DEF.MENU_IMGALGO, res.getString(R.string.algoriMenu));
		// 画像回転
		mMenuDialog.addItem(DEF.MENU_IMGROTA, res.getString(R.string.imgRotaMenu));
//		// 見開き設定
//		mMenuDialog.addItem(DEF.MENU_IMGVIEW, res.getString(R.string.tguide02));
//		// 画像サイズ
//		mMenuDialog.addItem(DEF.MENU_IMGSIZE, res.getString(R.string.tguide03));
//		// 余白削除
//		mMenuDialog.addItem(DEF.MENU_MGNCUT, res.getString(R.string.mgnCutMenu), mMgnCut > 0);
//		// シャープ化
//		mMenuDialog.addItem(DEF.MENU_SHARPEN, res.getString(R.string.sharpenMenu), mSharpen);
//		// 色反転
//		mMenuDialog.addItem(DEF.MENU_INVERT, res.getString(R.string.invertMenu), mInvert);
//		// グレースケール
//		mMenuDialog.addItem(DEF.MENU_GRAY, res.getString(R.string.grayMenu), mGray);
		// ページ逆順
		mMenuDialog.addItem(DEF.MENU_REVERSE, res.getString(R.string.reverseMenu), mReverseOrder);
		// 開き方向入れ替え
		mMenuDialog.addItem(DEF.MENU_CHG_OPE, res.getString(R.string.chgOpeMenu), mChgPage);
		// 表紙方向
		mMenuDialog.addItem(DEF.MENU_PAGEWAY, res.getString(R.string.pageWayMenu), res.getString(R.string.pageWayMenuSub1), res.getString(R.string.pageWayMenuSub2), mPageWay == DEF.PAGEWAY_RIGHT ? 0 : 1);
		// スクロール方向入れ替え
		mMenuDialog.addItem(DEF.MENU_SCRLWAY, res.getString(R.string.scrlWayMenu), res.getString(R.string.scrlWayMenuSub1), res.getString(R.string.scrlWayMenuSub2), mScrlWay == DEF.SCRLWAY_H ? 0 : 1);

		mMenuDialog.addItem(DEF.MENU_SETPROFILE, res.getString(R.string.SetProfileMenu));
		mMenuDialog.addItem(DEF.MENU_DELPROFILE, res.getString(R.string.DelProfileMenu));
		if (mProfileWord[0].equals("")) {
			// 中身が未定義ならデフォルト設定
			mMenuDialog.addItem(DEF.MENU_PROFILE1, res.getString(R.string.Profile1) + res.getString(R.string.undefineprofile));
		}
		else {
			mMenuDialog.addItem(DEF.MENU_PROFILE1, mProfileWord[0]);
		}
		if (mProfileWord[1].equals("")) {
			// 中身が未定義ならデフォルト設定
			mMenuDialog.addItem(DEF.MENU_PROFILE2, res.getString(R.string.Profile2) + res.getString(R.string.undefineprofile));
		}
		else {
			mMenuDialog.addItem(DEF.MENU_PROFILE2, mProfileWord[1]);
		}
		if (mProfileWord[2].equals("")) {
			// 中身が未定義ならデフォルト設定
			mMenuDialog.addItem(DEF.MENU_PROFILE3, res.getString(R.string.Profile3) + res.getString(R.string.undefineprofile));
		}
		else {
			mMenuDialog.addItem(DEF.MENU_PROFILE3, mProfileWord[2]);
		}
		if (mProfileWord[3].equals("")) {
			// 中身が未定義ならデフォルト設定
			mMenuDialog.addItem(DEF.MENU_PROFILE4, res.getString(R.string.Profile4) + res.getString(R.string.undefineprofile));
		}
		else {
			mMenuDialog.addItem(DEF.MENU_PROFILE4, mProfileWord[3]);
		}
		if (mProfileWord[4].equals("")) {
			// 中身が未定義ならデフォルト設定
			mMenuDialog.addItem(DEF.MENU_PROFILE5, res.getString(R.string.Profile5) + res.getString(R.string.undefineprofile));
		}
		else {
			mMenuDialog.addItem(DEF.MENU_PROFILE5, mProfileWord[4]);
		}

		// 一時設定
		mMenuDialog.addSection(res.getString(R.string.otherSec));
		// ヘルプ
		mMenuDialog.addItem(DEF.MENU_ONLINE, res.getString(R.string.onlineMenu));
		// 操作確認
		mMenuDialog.addItem(DEF.MENU_HELP, res.getString(R.string.helpMenu), mGuideView.getOperationMode());
		// 上部選択メニュー設定
		mMenuDialog.addItem(DEF.MENU_TOP_SETTING, res.getString(R.string.setTopMenu));
		// ツールバー設定
		mMenuDialog.addItem(DEF.MENU_EDIT_TOOLBAR, res.getString(R.string.ToolbarEditToolbar));
		// 設定
		mMenuDialog.addItem(DEF.MENU_SETTING, res.getString(R.string.setMenu));
		mMenuDialog.addItem(DEF.MENU_TAP_PATTERN, res.getString(R.string.tapPatternMenu));
		mMenuDialog.addItem(DEF.MENU_TAP_CLICK, res.getString(R.string.tapClickMenu));
		mMenuDialog.addItem(DEF.MENU_TAP_SETTING, res.getString(R.string.tapSettingMenu));
		mMenuDialog.addItem(DEF.MENU_CUSTOMKEY_SETTING, res.getString(R.string.CustonkeySettingMenu));
		// バージョン情報
		mMenuDialog.addItem(DEF.MENU_ABOUT, res.getString(R.string.aboutMenu));

		mMenuDialog.show(getSupportFragmentManager(), TabDialogFragment.class.getSimpleName());
	}

	// メニューを開く
	private void openDirTreeMenu() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mImageMgr == null || mImageView == null || mMenuDialog != null) {
			return;
		}

		// 文書情報を表示
		mGuideView.setPageText(mImageMgr.createPageStr(mSelectPage));
		mGuideView.setPageColor(0x80000000);

		Resources res = getResources();
		DirTreeDialog mMenuDialog = new DirTreeDialog(this, R.style.MyDialog, true, false, false, true, this);

		// タイトル
		mMenuDialog.addSection(res.getString(R.string.selDirTreeMenu));
		// ファイルリスト
		mMenuDialog.addFileList(mImageMgr.getList());

		mMenuDialog.show();

	}

	// メニューを開く
	private void openBookmarkMenu() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mImageMgr == null || mImageView == null || mMenuDialog != null) {
			return;
		}

		Resources res = getResources();
		TabDialogFragment mMenuDialog = new TabDialogFragment(this, R.style.MyDialog, true, this);

		// ブックマーク選択
		mMenuDialog.addSection(res.getString(R.string.selBookmarkMenu));

		ArrayList<RecordItem> list = RecordList.load(null, RecordList.TYPE_BOOKMARK, mServer, mPath, mFileName);

		boolean isAdd = false;
		for (int i = 0; i < list.size(); i++) {
			// ブックマーク追加
			RecordItem data = list.get(i);
			String image = data.getImage();
			for (int j = 0; j < mImageMgr.mFileList.length; j++) {
				if (mImageMgr.mFileList[j].name.equals(image)) {
					mMenuDialog.addItem(DEF.MENU_BOOKMARK + j, data.getDispName(), "P." + (j + 1));
					isAdd = true;
					break;
				}
			}
		}
		if (isAdd) {
			mMenuDialog.show(getSupportFragmentManager(), TabDialogFragment.class.getSimpleName());
		}
		else {
			Toast.makeText(this, R.string.bmNotFound, Toast.LENGTH_SHORT).show();
			mMenuDialog = null;
		}
	}

	@Override
	public void onSelectMenuDialog(int id) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		mMenuDialog = null;

		execCommand(id);
	}

	@Override
	public void onCloseMenuDialog() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		// メニュー終了
		mMenuDialog = null;
		// 情報表示クリア
		mGuideView.setPageText(null);
		mGuideView.setPageColor(Color.argb(0, 0, 0, 0));
		mGuideView.setGuideIndex(GuideView.GUIDE_NONE);
	}

	private void execCommand(int id) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		// メニュー選択
		// ページ戻りにはしない

		// ページ番号入力が開いていたら閉じる
		if (PageSelectDialog.mIsOpened) {
			mPageDlg.dismiss();
		}
		// サムネイルページ選択が開いていたら閉じる
		if (PageThumbnail.mIsOpened) {
			mThumbDlg.dismiss();
		}

		mPageBack = false;
		switch (id) {
			case DEF.MENU_IMGCONF: {
				// 画像表示設定
				showImageConfigDialog(DEF.MENU_IMGCONF);
				break;
			}
			case DEF.MENU_IMGALGO: {
				// 画像補間方式
				showSelectList(SELLIST_ALGORITHM);
				break;
			}
			case DEF.MENU_IMGROTA: {
				// 画像回転
				showSelectList(SELLIST_IMG_ROTATE);
				break;
			}
			case DEF.MENU_IMGVIEW: {
				// 見開き設定
				showSelectList(SELLIST_VIEW_MODE);
				break;
			}
			case DEF.MENU_IMGSIZE: {
				// 画像サイズ
				showSelectList(SELLIST_SCALE_MODE);
				break;
			}
			case DEF.MENU_MGNCUT: {
				// 余白削除
				showSelectList(SELLIST_MARGIN_CUT);
				break;
			}
			case DEF.MENU_MGNCUTCOLOR: {
				// 余白削除
				showSelectList(SELLIST_MARGIN_CUTCOLOR);
				break;
			}
			case DEF.MENU_DISPLAY_POSITION: {
				// 画面の表示位置
				showSelectList(SELLIST_DISPLAY_POSITION);
				break;
			}
			case DEF.MENU_ROTATE: {
				// 画面方向
				showSelectList(SELLIST_SCR_ROTATE);
				break;
			}
			case DEF.MENU_SHARPEN: {
				// シャープ化
				showImageConfigDialog(DEF.MENU_SHARPEN);
				break;
			}
			case DEF.MENU_BRIGHT: {
				// 明るさ補正
				showImageConfigDialog(DEF.MENU_BRIGHT);
				break;
			}
			case DEF.MENU_GAMMA: {
				// ガンマ補正
				showImageConfigDialog(DEF.MENU_GAMMA);
				break;
			}
			case DEF.MENU_CONTRAST: {
				// コントラスト
				showImageConfigDialog(DEF.MENU_CONTRAST);
				break;
			}
			case DEF.MENU_HUE: {
				// 色相
				showImageConfigDialog(DEF.MENU_HUE);
				break;
			}
			case DEF.MENU_SATURATION: {
				// 彩度
				showImageConfigDialog(DEF.MENU_SATURATION);
				break;
			}
			case DEF.MENU_BKLIGHT: {
				// バックライト
				showImageConfigDialog(DEF.MENU_BKLIGHT);
				break;
			}
			case DEF.MENU_INVERT: {
				// 白黒反転
				mInvert = mInvert ? false : true;
				setImageConfig();
				setBitmapImage();
				break;
			}
			case DEF.MENU_GRAY: {
				// グレースケール
				mGray = mGray ? false : true;
				setImageConfig();
				setBitmapImage();
				break;
			}
			case DEF.MENU_COLORING: {
				// 自動着色
				mColoring = mColoring ? false : true;
				setImageConfig();
				setBitmapImage();
				break;
			}
			case DEF.MENU_HELP: {
				boolean flag = !mGuideView.getOperationMode();
				mGuideView.setOperationMode(flag);
				break;
			}
			case DEF.MENU_ONLINE: {
				// 操作方法画面に遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_operate);	// 設定画面
				Intent intent;
				intent = new Intent(ImageActivity.this, HelpActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("Url", url);
				startActivity(intent);
				break;
			}
			case DEF.MENU_SETTING: {
				// 設定画面に遷移
				if (mImmForce) {
				}
				else if (mImmEnable && mSdkVersion >= 19) {
					int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
					uiOptions &= ~(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
					getWindow().getDecorView().setSystemUiVisibility(uiOptions);
				}

				// バックグラウンドでのキャッシュ読み込み停止
				mImageMgr.setCacheSleep(true);

				Intent intent = new Intent(ImageActivity.this, SetConfigActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, DEF.REQUEST_SETTING);
				break;
			}
			case DEF.MENU_TOP_SETTING: {
				// 上部の設定
				showCheckList();
				break;
			}
			case DEF.MENU_NOISE: {
				// マイク開始
				if (!FileSelectActivity.GetRecordSw()) {
					mNoiseSwitch.setConfig(mNoiseUnder, mNoiseOver, mNoiseDec);
					mNoiseSwitch.recordStart();
					FileSelectActivity.SetRecordSw(true);
					// 画面をスリープ無効
					getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
				else {
					mNoiseSwitch.recordStop();
					FileSelectActivity.SetRecordSw(false);
					mGuideView.setNoiseState(0, 0);
					// 画面をスリープ有効
					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
				break;
			}
			case DEF.MENU_AUTOPLAY: {
				// オートプレイ中の設定
				setAutoPlay(true);
				startViewTimer(DEF.HMSG_EVENT_AUTOPLAY);
				break;
			}
			case DEF.MENU_SHARE:
			case DEF.MENU_SHARER:
			case DEF.MENU_SHAREL: {
				// 共有
				int page = mCurrentPage;
				if ((id == DEF.MENU_SHAREL && mPageWay == DEF.PAGEWAY_RIGHT) || (id == DEF.MENU_SHARER && mPageWay == DEF.PAGEWAY_LEFT)) {
					page++;
				}
				String path = mImageMgr.decompFile(page);
				if (path != null && path.length() >= 5) {
					String ext = DEF.getFileExt(path);
					String mime = FileData.getMimeType(mActivity, path);

					// インテント起動
					if (id == DEF.MENU_SHARE || id == DEF.MENU_SHARER || id == DEF.MENU_SHAREL) {
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType(mime);
						// 保存した画像のURIを第二引数に。
						//intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path));
						Uri uri = FileProvider.getUriForFile(this,getApplicationContext().getPackageName() + ".provider", new File(path));
						intent.putExtra(Intent.EXTRA_STREAM, uri);
						SetViewPause();
						startActivity(intent);
						SetViewUpdate();
					}
				}
				break;
			}
			case DEF.MENU_DELSHARE: {
				// 共有用一時ファイルを削除する
				mImageMgr.deleteShareCache();
				break;
			}
			case DEF.MENU_REVERSE: {
				// ページを逆順にする
				reverseOrder();
				break;
			}

			case DEF.MENU_SETTHUMB: {
				// サムネイルに設定
				setThumb(mCurrentPage);
				break;
			}
			case DEF.MENU_SETTHUMBCROPPED: {
				//切り出してサムネイルに設定
				setThumbCropped(mCurrentPage);
				break;
			}
			case DEF.MENU_CMARGIN: {
				// 中央に余白を表示
				toggleCenterMargin();
				break;
			}
			case DEF.MENU_CSHADOW: {
				// 中央に影を表示
				toggleCenterShadow();
				break;
			}

			case DEF.MENU_CHG_OPE: {
				// 操作方向の入れ替え
				mChgPage = !mChgPage;
				mGuideView.setGuideSize(mClickArea, mTapPattern, mTapRate, mChgPage, mOldMenu);
				// mGuideView.invalidate();
				break;
			}
			case DEF.MENU_PAGEWAY: {
				// ページめくり方向の入れ替え
				if (mPageWay == DEF.PAGEWAY_RIGHT) {
					mPageWay = DEF.PAGEWAY_LEFT;
				}
				else {
					mPageWay = DEF.PAGEWAY_RIGHT;
				}

				// ページ基準点
				if (mViewPoint == DEF.VIEWPT_LEFTTOP) {
					mViewPoint = DEF.VIEWPT_RIGHTTOP;
				}
				else if (mViewPoint == DEF.VIEWPT_RIGHTTOP) {
					mViewPoint = DEF.VIEWPT_LEFTTOP;
				}
				else if (mViewPoint == DEF.VIEWPT_LEFTBTM) {
					mViewPoint = DEF.VIEWPT_RIGHTBTM;
				}
				else if (mViewPoint == DEF.VIEWPT_RIGHTBTM) {
					mViewPoint = DEF.VIEWPT_LEFTBTM;
				}
				setMgrConfig(false);
				setViewConfig();

				// イメージ拡大縮小
				ImageScaling();
				this.updateOverSize(false);
				setBitmapImage();
				break;
			}
			case DEF.MENU_SCRLWAY: {
				// スクロール方向の入れ替え
				if (mScrlWay == DEF.SCRLWAY_H) {
					mScrlWay = DEF.SCRLWAY_V;
				}
				else {
					mScrlWay = DEF.SCRLWAY_H;
				}
				setViewConfig();

				// スクロール位置の反映
				this.updateOverSize(false);
				break;
			}
			case DEF.MENU_SEL_DIR_TREE: {
				// ディレクトリ選択ダイアログ表示
				openDirTreeMenu();
				break;
			}
			case DEF.MENU_ADDBOOKMARK: {
				// ブックマーク追加ダイアログ表示
				BookmarkDialog bookmarkDlg = new BookmarkDialog(this, R.style.MyDialog);
				bookmarkDlg.setBookmarkListear(this);
				bookmarkDlg.setName((mCurrentPage + 1) + " / " + mImageMgr.mFileList.length);
				bookmarkDlg.show();
				break;
			}
			case DEF.MENU_SELBOOKMARK: {
				// ブックマーク選択ダイアログ表示
				openBookmarkMenu();
				break;
			}
			case DEF.MENU_EDIT_TOOLBAR: {
				// ツールバー編集ダイアログ表示
				ToolbarEditDialog dialog = new ToolbarEditDialog(this, R.style.MyDialog, mImageView.getWidth(), mImageView.getHeight());
				dialog.show();
				break;
			}
			
			case DEF.MENU_SETPROFILE: {
				// プロファイルの登録
				showSelectList(SELLIST_SETPROFILE);
				break;
			}
			case DEF.MENU_DELPROFILE: {
				// プロファイルの削除
				showSelectList(SELLIST_DELPROFILE);
				break;
			}

			case DEF.MENU_PROFILE1: {
				LoadProfile(0);
				break;
			}
			case DEF.MENU_PROFILE2: {
				LoadProfile(1);
				break;
			}
			case DEF.MENU_PROFILE3: {
				LoadProfile(2);
				break;
			}
			case DEF.MENU_PROFILE4: {
				LoadProfile(3);
				break;
			}
			case DEF.MENU_PROFILE5: {
				LoadProfile(4);
				break;
			}
			case DEF.MENU_TAP_PATTERN: {
				// タップ操作のパターンのダイアログを表示させる
				SetViewPause();
				TouchPanelView.SetAlertDialogTag(mActivity);
				SetViewUpdate();
				break;
			}
			case DEF.MENU_TAP_CLICK: {
				// タップ操作のクリックのダイアログを表示させる
				SetViewPause();
				TouchPanelView.SetAlertDialogClick(mActivity);
				SetViewUpdate();
				break;
			}
			case DEF.MENU_TAP_SETTING: {
				if (TouchPanelView.GetEditMode()) {
					// タップ操作の設定の編集中にする
					mTapEditMode = true;
					SetViewPause();
					mImageView.ViewTapSw(true);
				}
				break;
			}
			case DEF.MENU_CUSTOMKEY_SETTING:
				TouchPanelView.SetAlertDialogCustom(mActivity);
				break;
			
			default: {
				if (id >= DEF.MENU_DIR_TREE) {
					onSelectPage(id - DEF.MENU_DIR_TREE);
				}
				else if (id >= DEF.MENU_BOOKMARK) {
					onSelectPage(id - DEF.MENU_BOOKMARK);
				}
				else {
					// バージョン情報
					mInformation = new Information(this);
					mInformation.showAbout();
					mInformation.checkRecentRelease(mHandler, true);
				}
				break;
			}
		}
	}

	@Override
	public void onSelectPage(int page) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (!mListLoading && !mBitmapLoading && !mScrolling) {
			if (mCurrentPage != page) {
				// ページ選択
				mCurrentPage = page;

				// ページ変更時に振動
				startVibrate();
				mPageBack = false;
				if (mScrlNext) {
					mImageView.scrollReset();
				}
				setBitmapImage();
				mPageSelecting = true;
			}
		}
		else {
			mNextPage = page;
		}

		// 文書情報を更新
		mGuideView.setPageText(mImageMgr.createPageStr(mCurrentPage));

		Logcat.d(logLevel, "onSelectPage: currentPage=" + mCurrentPage + ", nextPage=" + mNextPage);
	}

	@Override
	public void onSelectPageSelectDialog(int menuId) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		switch (menuId) {
			case DEF.TOOLBAR_DISMISS: {
				if (!mKeepGuide) {
					// 情報表示クリア
					mGuideView.setPageText(null);
					mGuideView.setPageColor(Color.argb(0, 0, 0, 0));
					mGuideView.setGuideIndex(GuideView.GUIDE_NONE);
				}
				mKeepGuide = false;
				break;
			}
			case DEF.TOOLBAR_BOOK_LEFT: {
				if (mPageWay == DEF.PAGEWAY_RIGHT) {
					// 次巻(先頭ページ)
					// 次のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_NEXTTOP, false, true);
				}
				else {
					// 前巻(最終ページ)
					// 前のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_PREVLAST, false, true);
				}
				break;
			}
			case DEF.TOOLBAR_BOOK_RIGHT: {
				if (mPageWay == DEF.PAGEWAY_RIGHT) {
					// 前巻(最終ページ)
					// 前のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_PREVLAST, false, true);
				}
				else {
					// 次巻(先頭ページ)
					// 次のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_NEXTTOP, false, true);
				}
				break;
			}
			case DEF.TOOLBAR_BOOKMARK_LEFT: {
				if (mPageWay == DEF.PAGEWAY_RIGHT) {
					// 次巻(しおり位置)
					// 次のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_NEXT, false, true);
				}
				else {
					// 前巻(しおり位置)
					// 前のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_PREV, false, true);
				}
				break;
			}
			case DEF.TOOLBAR_BOOKMARK_RIGHT: {
				if (mPageWay == DEF.PAGEWAY_RIGHT) {
					// 前巻(しおり位置)
					// 前のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_PREV, false, true);
				}
				else {
					// 次巻(しおり位置)
					// 次のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_NEXT, false, true);
				}
				break;
			}
			case DEF.TOOLBAR_THUMB_SLIDER: {
				// イメージビュワー専用

				// ダイアログを閉じるときにガイド表示を消さない
				mKeepGuide = true;

				// 表示中の画像が1枚か2枚かを判定
				ImageData[] bm = mImageView.getImageBitmap();
				int shareType;
				if (bm[0] != null && bm[1] != null) {
					shareType = DEF.SHARE_LR;
				}
				else {
					shareType = DEF.SHARE_SINGLE;
				}

				if (mPageSelect == PAGE_INPUT) {

					mPageSelect = PAGE_THUMB;
					Editor ed = mSharedPreferences.edit();
					ed.putString(DEF.KEY_PAGESELECT, String.valueOf(mPageSelect));
					ed.apply();

					// サムネイルページ選択
					if (PageThumbnail.mIsOpened) {
						PageThumbnail thumbDlg = new PageThumbnail(this, R.style.MyDialog);
						thumbDlg.setParams(DEF.IMAGE_VIEWER, mCurrentPage, mPageWay == DEF.PAGEWAY_RIGHT, mImageMgr, mThumID, (mImageMgr.getFileType() == mImageMgr.FILETYPE_ZIP || mImageMgr.getFileType() == mImageMgr.FILETYPE_RAR));
						thumbDlg.setPageSelectListear(this);
						thumbDlg.show();
						thumbDlg.setShareType(shareType);
						mThumbDlg = thumbDlg;
					}
				}
				else if (mPageSelect == PAGE_THUMB) {

					mPageSelect = PAGE_INPUT;
					Editor ed = mSharedPreferences.edit();
					ed.putString(DEF.KEY_PAGESELECT, String.valueOf(mPageSelect));
					ed.apply();

					// ページ番号入力
					if (PageSelectDialog.mIsOpened) {
						PageSelectDialog pageDlg = new PageSelectDialog(this, R.style.MyDialog);
						pageDlg.setParams(DEF.IMAGE_VIEWER, mCurrentPage, mImageMgr.length(), mPageWay == DEF.PAGEWAY_RIGHT, (mImageMgr.getFileType() == mImageMgr.FILETYPE_ZIP || mImageMgr.getFileType() == mImageMgr.FILETYPE_RAR));
						pageDlg.setPageSelectListear(this);
						pageDlg.show();
						pageDlg.setShareType(shareType);
						mPageDlg = pageDlg;
					}
				}

				break;
			}
			case DEF.TOOLBAR_DIR_TREE: {
				// イメージビュワー専用

				// ダイアログを閉じるときにガイド表示を消さない
				mKeepGuide = true;

				// ページ番号入力が開いていたら閉じる
				if (PageSelectDialog.mIsOpened) {
					mPageDlg.dismiss();
				}
				// サムネイルページ選択が開いていたら閉じる
				if (PageThumbnail.mIsOpened) {
					mThumbDlg.dismiss();
				}

				openDirTreeMenu();
				break;
			}
			case DEF.TOOLBAR_TOC: {
				// テキストビュワー専用
				break;
			}
			case DEF.TOOLBAR_FAVORITE: {
				execCommand(DEF.MENU_SELBOOKMARK);
				break;
			}
			case DEF.TOOLBAR_ADD_FAVORITE: {
				execCommand(DEF.MENU_ADDBOOKMARK);
				break;
			}
			case DEF.TOOLBAR_SEARCH: {
				// テキストビュワー専用
				break;
			}
			case DEF.TOOLBAR_SHARE: {
				// イメージビュワー専用
				execCommand(DEF.MENU_SHARE);
				break;
			}
			case DEF.TOOLBAR_SHARE_LEFT_PAGE: {
				// イメージビュワー専用
				execCommand(DEF.MENU_SHAREL);
				break;
			}
			case DEF.TOOLBAR_SHARE_RIGHT_PAGE: {
				// イメージビュワー専用
				execCommand(DEF.MENU_SHARER);
				break;
			}
			case DEF.TOOLBAR_ROTATE: {
				// イメージビュワー専用
				execCommand(DEF.MENU_ROTATE);
				break;
			}
			case DEF.TOOLBAR_ROTATE_IMAGE: {
				// イメージビュワー専用
				execCommand(DEF.MENU_IMGROTA);
				break;
			}
			case DEF.TOOLBAR_SELECT_THUMB: {
				// イメージビュワー専用
				execCommand(DEF.MENU_SETTHUMB);
				break;
			}
			case DEF.TOOLBAR_TRIM_THUMB: {
				// イメージビュワー専用
				execCommand(DEF.MENU_SETTHUMBCROPPED);
				break;
			}
			case DEF.TOOLBAR_CONTROL: {
				execCommand(DEF.MENU_IMGCONF);
				break;
			}
			case DEF.TOOLBAR_MENU: {
				// ページ番号入力が開いていたら閉じる
				if (PageSelectDialog.mIsOpened) {
					mPageDlg.dismiss();
				}
				// サムネイルページ選択が開いていたら閉じる
				if (PageThumbnail.mIsOpened) {
					mThumbDlg.dismiss();
				}

				openMenu();
				break;
			}
			case DEF.TOOLBAR_CONFIG: {
				// ページ番号入力が開いていたら閉じる
				if (PageSelectDialog.mIsOpened) {
					mPageDlg.dismiss();
				}
				// サムネイルページ選択が開いていたら閉じる
				if (PageThumbnail.mIsOpened) {
					mThumbDlg.dismiss();
				}

				execCommand(DEF.MENU_SETTING);
				break;
			}
			case DEF.TOOLBAR_EDIT_TOOLBAR: {
				execCommand(DEF.MENU_EDIT_TOOLBAR);
				break;
			}
		}
	}

	// 他アクティビティからの復帰通知
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		mPageBack = false;

		// バックグラウンドでのキャッシュ読み込み再開
		mImageMgr.setCacheSleep(false);

		if (requestCode == DEF.REQUEST_SETTING) {
			// 設定の読込
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

			ReadSetting(sharedPreferences);
			// 他アクティビティからの復帰通知時に元に戻す
			LoadCurrentSetting();

			if ((mImmEnable || mImmForce)  && mSdkVersion >= 19) {
				int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
				uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
				uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
				getWindow().getDecorView().setSystemUiVisibility(uiOptions);
			}

			mImageView.setImageBitmap(null);

			// 表示のコンフィグレーション
			setViewConfig();
			// 画面サイズの更新
			mImageView.updateScreenSize();

			// 色とサイズを指定
			mGuideView.setColor(mTopColor1, mTopColor2, mMgnColor);
			mGuideView.setGuideSize(mClickArea, mTapPattern, mTapRate, mChgPage, mOldMenu);
			// mGuideView.setRotateMode(mPseLand);
			mGuideView.setPageNumberFormat(mPnumDisp, mPnumFormat, mPnumPos, mPnumSize, mPnumColor);
			mGuideView.setPageNumberString(null);

			setMgrConfig(true);

			// ファイルリストを再作成
			// if (mFileList != null) {
			// mFileList.close();
			// }
			// mFileList = new ImageFileList(mUriPath, mFileName, mFileSort);
			setBitmapImage();

			// スケーリングを元に戻す
			// 描画スレッド停止
			mImageView.lockDraw();
			synchronized (mImageView) {
				// スケーリング変更
				mPinchScale = mPinchScaleSel;
				mImageMgr.setImageScale(mPinchScale);
				// イメージ拡大縮小
				ImageScaling();
			}
			// ビットマップを調整
			this.updateOverSize(true);
			// 描画スレッド開始
			mImageView.update(true);
		}
		else if (requestCode == DEF.REQUEST_FILE) {
			setBitmapImage();
		}
		else if (requestCode == DEF.REQUEST_CROP) {
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				setThumb(uri);
			}
		}
	}

	// 設定の読み込み
	private void ReadSetting(SharedPreferences sharedPreferences) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		// 設定値取得
		try {
			mFileSort = SetImageActivity.getFileSort(sharedPreferences);
			mZoomType = SetImageActivity.getZoomType(sharedPreferences);
			mViewPoint = SetImageText.getViewPt(sharedPreferences);
			mScaleMode = SetImageActivity.getIniScale(sharedPreferences);
			mEffect = SetImageActivity.getEffect(sharedPreferences);

			mScroll = DEF.calcScroll(SetImageTextDetailActivity.getScroll(sharedPreferences));
			mClickArea = DEF.calcClickAreaPix(SetImageTextDetailActivity.getClickArea(sharedPreferences), mSDensity);
			mPageRange = DEF.calcPageRangePix(SetImageTextDetailActivity.getPageRange(sharedPreferences), mSDensity);
			mMoveRange = DEF.calcTapRangePix(SetImageTextDetailActivity.getTapRange(sharedPreferences), mSDensity);
			mLongTapZoom = DEF.calcMSec200(SetImageDetailActivity.getLongTap(sharedPreferences));
			mWAdjust = DEF.calcWAdjust(SetImageDetailActivity.getWAdjust(sharedPreferences));
			mWidthScale = DEF.calcWScaling(SetImageDetailActivity.getWScaling(sharedPreferences));
			mImgScale = DEF.calcScaling(SetImageDetailActivity.getScaling(sharedPreferences));
			Logcat.d(logLevel, "mWidthScale=" + mWidthScale + ", mImgScale=" + mImgScale);

			mEffectTime = DEF.calcEffectTime(SetImageTextDetailActivity.getEffectTime(sharedPreferences));
			mAutoPlayTerm = DEF.calcAutoPlay(SetImageDetailActivity.getAutoPlay(sharedPreferences));
			mPageSelect = SetImageText.getPageSelect(sharedPreferences);

			if (SetImageActivity.getCenterMargin(sharedPreferences)) {
				mCenter = SetImageTextDetailActivity.getCenter(sharedPreferences);
			}
			else {
				mCenter = 0;
			}
			if (SetImageActivity.getCenterShadow(sharedPreferences)) {
				mShadow = SetImageTextDetailActivity.getGradation(sharedPreferences);
			}
			else {
				mShadow = 0;
			}
			mMargin = SetImageTextDetailActivity.getMargin(sharedPreferences);
			if (mSdkVersion >= 19) {
				// KitKat以降のみ設定読み込み
				mImmEnable = SetImageTextDetailActivity.getImmEnable(sharedPreferences);
				mImmForce = SetCommonActivity.getForceHideNavigationBar(sharedPreferences);
			}
			else {
				mImmEnable = false;
				mImmForce = false;
			}
			mOldMenu = SetImageTextDetailActivity.getOldMenu(sharedPreferences);
			mBottomFile = SetImageTextDetailActivity.getBottomFile(sharedPreferences);
			mPinchEnable = SetImageTextDetailActivity.getPinchEnable(sharedPreferences);

			mVolScrl = DEF.calcScrlSpeedPix(SetImageTextDetailActivity.getVolScrl(sharedPreferences), mSDensity);
			mScrlWay = SetImageActivity.getScrlWay(sharedPreferences);
			mTapScrl = SetImageText.getTapScrl(sharedPreferences);
			mFlickPage = SetImageText.getFlickPage(sharedPreferences);

			mMgnCut = SetImageActivity.getMgnCut(sharedPreferences);
			mMgnCutColor = SetImageActivity.getMgnCutColor(sharedPreferences);

			mMgnBlkMsk = SetMarginCutActivity.getMargingBlackMask(sharedPreferences);
			mMarginLevel = SetMarginCutActivity.getMarginLevel(sharedPreferences);
			mMarginSpace = SetMarginCutActivity.getMarginSpace(sharedPreferences);
			mMarginRange = SetMarginCutActivity.getMarginRange(sharedPreferences);
			mMarginStart = SetMarginCutActivity.getMarginStart(sharedPreferences);
			mMarginLimit = SetMarginCutActivity.getMarginLimit(sharedPreferences);
			mMarginAspectMask = SetMarginCutActivity.getMarginAspectMask(sharedPreferences);
			mMarginForceIgnoreAspect = SetMarginCutActivity.getMarginForceIgnoreAspect(sharedPreferences);

			mDisplayPosition = SetImageActivity.getDisplayPosition(sharedPreferences);
			mBright = SetImageActivity.getBright(sharedPreferences);
			mGamma = SetImageActivity.getGamma(sharedPreferences);
			mBkLight = SetImageActivity.getBkLight(sharedPreferences);
			mContrast = SetImageActivity.getContrast(sharedPreferences);
			mHue = SetImageActivity.getHue(sharedPreferences);
			mSaturation = SetImageActivity.getSaturation(sharedPreferences);
			mSharpen = SetImageActivity.getSharpen(sharedPreferences);
			mInvert = SetImageActivity.getInvert(sharedPreferences);
			mGray = SetImageActivity.getGray(sharedPreferences);
			mColoring = SetImageActivity.getColoring(sharedPreferences);
			mMoire = SetImageActivity.getMoire(sharedPreferences);
			mTopSingle = SetImageActivity.getTopSingle(sharedPreferences);
			mPinchScale = SetImageActivity.getPinScale(sharedPreferences);
			mPinchScaleSel = mPinchScale;

			mScrlRngW = DEF.calcScrlRange(SetImageDetailActivity.getScrlRngW(sharedPreferences));
			mScrlRngH = DEF.calcScrlRange(SetImageDetailActivity.getScrlRngH(sharedPreferences));

			// バックライト設定
			if (mBkLight <= 10) {
				// バックライト変更
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.screenBrightness = (float)mBkLight / 10;
				getWindow().setAttributes(lp);
			}

			// 処理スレッド数
			mMaxThread = SetImageDetailActivity.getMaxThread(sharedPreferences);
			if (mMaxThread == 0) {
				mMaxThread = Runtime.getRuntime().availableProcessors();
				if (mMaxThread > 7) {
					// ひとつ落とす
					mMaxThread = 7;
				}
				else if (mMaxThread <= 0) {
					mMaxThread = 1;
				}
			}

			mLoupeSize = SetImageDetailActivity.getLoupeSize(sharedPreferences);

			mNoiseScrl = DEF.calcScrlSpeedPix(SetNoiseActivity.getNoiseScrl(sharedPreferences), mSDensity);
			mNoiseUnder = DEF.calcNoiseLevel(SetNoiseActivity.getNoiseUnder(sharedPreferences));
			mNoiseOver = DEF.calcNoiseLevel(SetNoiseActivity.getNoiseOver(sharedPreferences));
			mNoiseLevel = SetNoiseActivity.getNoiseLevel(sharedPreferences);
			mNoiseDec = SetNoiseActivity.getNoiseDec(sharedPreferences);
			mNoiseSwitch.setConfig(mNoiseUnder, mNoiseOver, mNoiseDec);

			mMgnColor = SetImageTextColorActivity.getMgnColor(sharedPreferences);
			mCenColor = SetImageTextColorActivity.getCntColor(sharedPreferences);
			// mTopColor = 0x60000000 |
			// ((mMgnColor & 0x00010000) != 0 ? 0x00700000 : 0) |
			// ((mMgnColor & 0x00000100) != 0 ? 0x00007000 : 0) |
			// ((mMgnColor & 0x00000001) != 0 ? 0x00000070 : 0);
			mTopColor1 = SetImageTextColorActivity.getGuiColor(sharedPreferences);
			mTopColor2 = 0x40000000 | (mTopColor1 & 0x00FFFFFF);

			mNotice = SetImageActivity.getNotice(sharedPreferences);
			mForceNotice = SetCommonActivity.getForceHideStatusBar(sharedPreferences);
			mNoSleep = SetImageActivity.getNoSleep(sharedPreferences);
			mViewPause = SetImageActivity.getViewPause(sharedPreferences);
			mChgPage = SetImageText.getChgPage(sharedPreferences);
			mChgFlick = SetImageText.getChgFlick(sharedPreferences);
			mLastMsg = SetImageText.getLastPage(sharedPreferences);
			mSavePage = SetImageText.getSavePage(sharedPreferences);
			mFitDual = SetImageActivity.getFitDual(sharedPreferences);
			mCMargin = SetImageActivity.getCenterMargin(sharedPreferences);
			mCShadow = SetImageActivity.getCenterShadow(sharedPreferences);
			mNoExpand = SetImageActivity.getNoExpand(sharedPreferences);
			mVibFlag = SetImageText.getVibFlag(sharedPreferences);
			mMomentMode = SetImageTextDetailActivity.getMomentMode(sharedPreferences);
			mFlickEdge = SetImageText.getFlickEdge(sharedPreferences);

			mPrevRev = SetImageText.getPrevRev(sharedPreferences); // ページ戻り時の左右位置反転
			mPageWay = SetImageActivity.getPageWay(sharedPreferences); // ページ方向(右表紙/左表紙)
			mDispMode = SetImageActivity.getInitView(sharedPreferences); // 表示モード(NORMAL/DUAL/HALF/縦横で切替)
			mAlgoMode = SetImageActivity.getAlgoMode(sharedPreferences); // 補間モード
			mDelShare = SetImageActivity.getDelShare(sharedPreferences); // 共有ファイルの削除
			mViewRota = SetImageActivity.getViewRota(sharedPreferences);
			DEF.setRotation(this, mViewRota);
			if (mViewRota == DEF.ROTATE_PSELAND) {
				// 疑似横画面
				mPseLand = true;
			}
			else {
				mPseLand = false;
			}

			mVolKeyMode = SetImageText.getVolKey(sharedPreferences); // 音量キー操作
			mTapPattern = SetImageText.getTapPattern(sharedPreferences); // タップパターン
			mTapRate = SetImageText.getTapRate(sharedPreferences); // タップの比率
			mChgPageKey = SetImageText.getChgPageKey(sharedPreferences); // 左右キー操作入れ替え

			mPnumDisp = SetImageActivity.getPnumDisp(sharedPreferences); // ページ表示有無
			mPnumFormat = SetImageActivity.getPnumFormat(sharedPreferences); // ページ表示書式
			mPnumPos = SetImageActivity.getPnumPos(sharedPreferences); // ページ表示位置
			mPnumSize = DEF.calcPnumSizePix(SetImageActivity.getPnumSize(sharedPreferences), mSDensity); // ページ表示サイズ
			mPnumColor = SetImageActivity.getPnumColor(sharedPreferences); // ページ表示色

			mTimeDisp = SetImageActivity.getTimeDisp(sharedPreferences); // 時刻と充電表示有無
			mTimeFormat = SetImageActivity.getTimeFormat(sharedPreferences); // 時刻と充電表示書式
			mTimePos = SetImageActivity.getTimePos(sharedPreferences); // 時刻と充電表示位置
			mTimeSize = DEF.calcPnumSizePix(SetImageActivity.getTimeSize(sharedPreferences), mSDensity); // 時刻と充電表示サイズ
			mTimeColor = SetImageActivity.getTimeColor(sharedPreferences); // 時刻と充電表示色

			if (mGuideView != null) {
				// 時刻＆バッテリー表示の情報をガイドに設定
				mGuideView.setTimeFormat(mTimeDisp, mTimeFormat, mTimePos, mTimeSize, mTimeColor);
			}
			mConfirmBack = SetImageText.getConfirmBack(sharedPreferences); // 戻るキーで確認メッセージ
			// mResumeOpen = false;
			mReturnListView = SetImageText.getReturnListView(sharedPreferences); // 画面が裏に入った場合にリスト一覧へ戻る

			mHidden = SetCommonActivity.getHiddenFile(sharedPreferences);

			mMemSize = DEF.calcMemSize(SetCacheActivity.getMemSize(sharedPreferences));
			mMemNext = DEF.calcMemPage(SetCacheActivity.getMemNext(sharedPreferences));
			mMemPrev = DEF.calcMemPage(SetCacheActivity.getMemPrev(sharedPreferences));
			mMemCache = SetCacheActivity.getMemCache(sharedPreferences);

			mScrlNext = SetImageActivity.getScrlNext(sharedPreferences); // スクロールで次のページへ移動
			mViewNext = SetImageActivity.getViewNext(sharedPreferences); // 次のページを表示
			mNextFilter = SetImageActivity.getNextFilter(sharedPreferences); // 次のページのエフェクト表示

			mRotateBtn = DEF.RotateBtnList[SetCommonActivity.getRotateBtn(sharedPreferences)];

			// アクセス状態表示
			mAccessLamp = SetImageDetailActivity.getAccessLamp(sharedPreferences);

			// 上部メニューの設定を読み込み
			loadTopMenuState();
			if (mGuideView != null) {
				// 上部メニューの文字列情報をガイドに設定
				mGuideView.setTopCommandStr(mCommandStr);
			}

			mEpubOrder = SetEpubActivity.getEpubOrder(sharedPreferences);
		}
		catch (Exception e) {
			Logcat.e(logLevel, "error.");
		}
		return;
	}

	// 拡大位置の座標補正 & 設定
	private void callZoomAreaDraw(float x, float y) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		// 2011/11/18 ルーペ機能
		mImageView.setZoomPos((int) x, (int) y);
		return;
	}

	private void startVibrate() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		long nowTime = System.currentTimeMillis();

		if (mVibFlag) {
			if (nowTime > mPrevVibTime + TIME_VIB_TERM) {
				// 前回と間が空いているときだけ振動
				mVibrator.vibrate(TIME_VIB_RANGE);
				mPrevVibTime = nowTime;
			}
		}
	}

	private void changePage(int move) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (move >= 0) {
			// 次ページ
			nextPage();
		}
		else {
			// 前ページ
			prevPage();
		}
		return;
	}

	public void nextPage() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		// 次ページへ
		if ((mCurrentPage >= mImageMgr.length() - 1 && (!mCurrentPageHalf || (mCurrentPageHalf && mHalfPos == HALFPOS_2ND))) || (mCurrentPage >= mImageMgr.length() - 2 && mCurrentPageDual)) {
			// 分割表示中は最終ページの2ページ目なら次ページに遷移しない
			// 並べて表示中以外は最終ページなら次ページに遷移しない
			// 並べて表示中は最終ページ-1なら次ページに遷移しない

			// 最終ページ
			if (mAutoPlay) {
				// 自動再生中は停止
				setAutoPlay(false);
			}
			else if (mLastMsg == DEF.LASTMSG_DIALOG) {
				showCloseDialog(CloseDialog.LAYOUT_LAST);
			}
			else if (mLastMsg == DEF.LASTMSG_NEXT) {
				if (mUriPath.substring(mUriPath.length() - 1).equals("/")) {
					if (mImageName == null || mImageName.isEmpty()) {
						// ディレクトリオープンのとき
						// 前のファイルを開き、続きから記録せず、現在頁保存
						finishActivity(CloseDialog.CLICK_NEXTTOP, false, true);
					} else {
						// イメージファイル直接オープンのとき
						// 閉じる
						finishActivity(false);
					}
				}
				else {
					// 圧縮ファイルオープンのとき
					// 前のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_NEXTTOP, false, true);
				}
			}
			else {
				// 閉じる
				finishActivity(false);
			}
			return;
		}

		if (mCurrentPageHalf && mHalfPos != HALFPOS_2ND) {
			// 分割表示の2ページ目表示中
			mHalfPos = HALFPOS_2ND;
		}
		else {
			if (mCurrentPageDual) {
				// 2ページ表示のときは2ページ進む
				mCurrentPage += 2;
			}
			else {
				// 1ページ進む
				mCurrentPage++;
			}
			mHalfPos = HALFPOS_1ST;
		}
		mPageBack = false;
		startVibrate();
		if (mImageView.getPageLock()) {
			mPageSelecting = true;
		}
		setBitmapImage();

	}

	public void prevPage() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		// 前ページへ
		if (mCurrentPage <= 0 && (!mCurrentPageHalf || (mCurrentPageHalf && mHalfPos != HALFPOS_2ND))) {
			// 先頭ページかつ分割表示中かつ2ページ目でないなら前ページはない
			// 先頭ページかつ分割表示でないなら前ページはない

			// 先頭ページ
			if (mLastMsg == DEF.LASTMSG_DIALOG) {
				showCloseDialog(CloseDialog.LAYOUT_TOP);
			}
			else if (mLastMsg == DEF.LASTMSG_NEXT) {
				if (mUriPath.substring(mUriPath.length() - 1).equals("/")) {
					if (mImageName == null || mImageName.length() == 0) {
						// ディレクトリオープンのとき
						// 前のファイルを開き、続きから記録せず、現在頁保存
						finishActivity(CloseDialog.CLICK_PREVLAST, false, true);
					} else {
						// イメージファイル直接オープンのとき
						// 閉じる
						finishActivity(false);
					}
				}
				else {
					// 圧縮ファイルオープンのとき
					// 前のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_PREVLAST, false, true);
				}
			}
			else {
				// 閉じる
				finishActivity(false);
			}
			return;
		}
		if (mCurrentPageHalf && mHalfPos == HALFPOS_2ND) {
			// 分割表示の2ページ目表示中
			mHalfPos = HALFPOS_1ST;
		}
		else {
			// 1ページ戻る
			if (isDualView()) {
				mCurrentPage -= 2;
			}
			else {
				mCurrentPage--;
			}
			mHalfPos = HALFPOS_2ND;
		}
		mPageBack = true;
		startVibrate();
		if (mImageView.getPageLock()) {
			mPageSelecting = true;
		}
		setBitmapImage();
	}

	// 1ページずらし
	private void shiftPage(int way) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (isDualView()) {
			// 見開きモードの時だけ
			if (way < 0) {
				// 1ページ前へ
				if (mCurrentPage > 0) {
					startVibrate();
					mCurrentPage--;
					mPageBack = true;
					setBitmapImage();
				}
			}
			else if (way > 0) {
				// 1ページ次へ
				if (mCurrentPage < mImageMgr.length() - 1) {
					startVibrate();
					mCurrentPage++;
					mPageBack = false;
					setBitmapImage();
				}
			}
		}
	}

	// 現在見開き表示中かを返す
	private boolean isDualView() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mDispMode == DEF.DISPMODE_IM_DUAL) {
			return true;
		}
		else if (mDispMode == DEF.DISPMODE_IM_EXCHANGE) {
			if (mViewRota == DEF.ROTATE_PSELAND) {
				return true;
			}
			else {
				if (!DEF.checkPortrait(mViewWidth, mViewHeight)) {
					// /* getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT */
					return true;
				}
			}
		}
		return false;
	}

	// 現在単ページ表示中かを返す
	private boolean isHalfView() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mDispMode == DEF.DISPMODE_IM_HALF) {
			return true;
		}
		else if (mDispMode == DEF.DISPMODE_IM_EXCHANGE) {
			if (mViewRota != DEF.ROTATE_PSELAND) {
				if (DEF.checkPortrait(mViewWidth, mViewHeight)) {
					/* getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT */
					return true;
				}
			}
		}
		return false;
	}

	private void startScroll(int move) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. mListLoading=" + mListLoading + ", mBitmapLoading=" + mBitmapLoading + ", mScrolling=" + mScrolling);

		// ピンチズームの更新
		SetPinchScaleSetting();

		if (!mListLoading && !mBitmapLoading && !mScrolling) {
			if (!mImageView.setViewPosScroll(move)) {
				// スクロールする余地がなければ次ページ
				if (mScrlNext) {
					mImageView.scrollReset();
				}
				changePage(move);
			}
			else {
				// スクロール開始
				startViewTimer(DEF.HMSG_EVENT_SCROLL);
			}
		}
		else {
			// なぜかスクロールが完了していない場合にスクロールを再開させる
			if (!mImageView.checkScrollTime(mVolScrl)) {
				startViewTimer(DEF.HMSG_EVENT_SCROLL_NEXT);
			}
		}
	}

	// 長押しタイマー開始
	public boolean startLongTouchTimer(int longtouch_event) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		int longtaptime;
		if (longtouch_event == DEF.HMSG_EVENT_TOUCH_ZOOM || longtouch_event == DEF.HMSG_EVENT_LONG_TAP) {
			longtaptime = mLongTapZoom;
		}
		else {
			// 下部押下時のみIMMERSIVEがOFFでも長押しにする(先頭・末尾の誤爆対策)
			if (longtouch_event == DEF.HMSG_EVENT_TOUCH_BOTTOM) {
				longtaptime = LONGTAP_TIMER_BTM;
			}
			else {
				if (!mImmEnable && !mImmForce) {
					return false;
				}
				longtaptime = LONGTAP_TIMER_UI;
			}
		}

		Message msg = mHandler.obtainMessage(longtouch_event);
		msg.arg1 = ++ mLongTouchCount;
		long NextTime = SystemClock.uptimeMillis() + longtaptime;

		mHandler.sendMessageAtTime(msg, NextTime);
//		Logcat.d(logLevel, "msg=" + longtouch_event + ", count=" + mLongTouchCount);
		return true;
	}

	// 起動時のプログレスダイアログ表示
	public boolean startDialogTimer(int time) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		mReadTimerMsg = mHandler.obtainMessage(DEF.HMSG_EVENT_READTIMER);
		long NextTime = SystemClock.uptimeMillis() + time;

		mHandler.sendMessageAtTime(mReadTimerMsg, NextTime);
		return (true);
	}

	// タイマー開始
	public void startViewTimer(int event) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "開始します. event=" + event);

		Message msg = mHandler.obtainMessage(event);
		long NextTime = SystemClock.uptimeMillis();

		if (event == DEF.HMSG_EVENT_EFFECT) {
			// エフェクト開始
			if (mEffectTime > 0) {
				mEffectRate = 0.99f * (mPageBack ? -1 : 1) * (mPageWay != DEF.PAGEWAY_LEFT ? 1 : -1);
				mEffectStart = NextTime;
				// mImageView.setEffectRate(mEffectRate);
				NextTime += DEF.INTERVAL_EFFECT;
			}
			else {
				mImageView.setEffectRate(0.0f);
				return;
			}
		}
		if (event == DEF.HMSG_EVENT_EFFECT_NEXT) {
			// エフェクト進行
			NextTime += DEF.INTERVAL_EFFECT_NEXT;
		}
		else if (event == DEF.HMSG_EVENT_SCROLL) {
			Logcat.v(logLevel, "HMSG_EVENT_SCROLL.");
			// スクロール開始
			if (!mImageView.moveToNextPoint(mVolScrl)) {
				return;
			}
			NextTime += DEF.INTERVAL_SCROLL;
			mScrolling = true;
		}
		else if (event == DEF.HMSG_EVENT_SCROLL_NEXT) {
			Logcat.v(logLevel, "HMSG_EVENT_SCROLL_NEXT.");
			// スクロール進行
			NextTime += DEF.INTERVAL_SCROLL_NEXT;
		}
		else if (event == DEF.HMSG_EVENT_AUTOPLAY) {
			// 自動再生
			if (!mAutoPlay) {
				return;
			}
			NextTime += mAutoPlayTerm;
		}
		else if (event == DEF.HMSG_EVENT_LOADING) {
			// ローディング表示開始
			NextTime += DEF.INTERVAL_LOADING;
		}
		else if (event == DEF.HMSG_EVENT_LOADING_NEXT) {
			// ローディング表示進行
			NextTime += DEF.INTERVAL_LOADING_NEXT;
		}
		else {
			NextTime += DEF.INTERVAL_DEFAULT;
		}

		if (logLevel <= Logcat.LOG_LEVEL_VERBOSE) {
			String next_ms = (new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())).format(NextTime);
			Logcat.v(logLevel, "NextTime=" + next_ms);
		}

		if(!mHandler.sendMessageAtTime(msg, NextTime)) {
			Logcat.w(logLevel, "失敗しました. sendMessageAtTime(msg=" + msg + ", NextTime=" + NextTime +")");
		}
	}

	private void setAutoPlay(boolean mode) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		Window window = getWindow();
		if (mode) {
			// 画面をスリープしないように設定
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		else {
			// 画面をスリープするように戻す
			window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		mAutoPlay = mode;
	}

	private void showCloseDialog(int layout) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mCloseDialog != null) {
			return;
		}
		mCloseDialog = new CloseDialog(this, R.style.MyDialog);
		mCloseDialog.setTitleText(layout);
		mCloseDialog.setCloseListear(new CloseListenerInterface() {
			@Override
			public void onCloseSelect(int select, boolean resume, boolean mark) {
				//
				if (select != CloseDialog.CLICK_CANCEL) {
					finishActivity(select, resume, mark);
				}
			}

			@Override
			public void onClose() {
				// 終了
				mCloseDialog = null;
			}
		});
		mCloseDialog.show();
	}

	@Override
	public void onAddBookmark(String name) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		Logcat.d(logLevel, "mServer=" + mServer + ", mURI=" + mURI + ", mPath=" + mPath
				+ ", mFileName=" + mFileName + ", mImageName=" + mImageName + ", mCurrentPage=" + mCurrentPage);
		// ブックマーク追加
		if ((mFileName == null || mFileName.isEmpty()) && (mImageName != null && !mImageName.isEmpty())) {
			Logcat.d(logLevel, "画像ファイル指定.");
				// 画像ファイル直接指定
			RecordList.add(RecordList.TYPE_BOOKMARK, RecordItem.TYPE_IMAGEDIRECT, mServer, mPath, mFileName
					, new Date().getTime(), mImageMgr.mFileList[mCurrentPage].name, mCurrentPage, name);
		}
		else {
			Logcat.d(logLevel, "ディレクトリまたは圧縮ファイル.");
			// ディレクトリまたは圧縮ファイル
			RecordList.add(RecordList.TYPE_BOOKMARK, RecordItem.TYPE_IMAGE, mServer, mPath, mFileName
					, new Date().getTime(), mImageMgr.mFileList[mCurrentPage].name, mCurrentPage, name);
		}
	}

	private void finishActivity(boolean resume) {
		finishActivity(CloseDialog.CLICK_CLOSE, resume, true);
	}

	public void finishActivity(int select, boolean resume, boolean mark) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		// 続きから読み込みの設定
		if (!resume) {
			removeLastFile();
		}

		if (mark && !mSavePage) {
			// しおりを保存する
			saveCurrentPage();
		} else if (!mark && mSavePage) {
			// しおりを起動時の状態に戻す
			restoreCurrentPage();
		}

		// 履歴保存
		saveHistory();
		mFinishActivity = true;

		// 開いているファイルorディレクトリ
		String lastfile = null;
		String lastpath = null;
		if (mFileName == null || mFileName.isEmpty()) {
			// 圧縮ファイルオープン以外の時
			if (select == CloseDialog.CLICK_CLOSE && mImageName != null && !mImageName.isEmpty()) {
				// クローズかつイメージファイル直接オープンのとき
				lastfile = mImageName;
				lastpath = mPath;
			}
			else {
				// ディレクトリオープンのとき
				if (mPath != null) {
					int plen = mPath.length();
					if (plen > 2) {
						int index = mPath.lastIndexOf('/', plen - 2);
						if (index >= 0) {
							// 先頭以外で/があれば切り出し
							lastfile = mPath.substring(index + 1);
							lastpath = mPath.substring(0, index + 1);
						}
					}
				}
			}
		}
		else {
			lastfile = mFileName;
			lastpath = mPath;
		}

		// 呼び出し元に通知
		Intent intent = new Intent();
		intent.putExtra("NextOpen", select);
		intent.putExtra("LastFile", lastfile);
		intent.putExtra("LastPath", lastpath);
		intent.putExtra("LastUri", lastpath);
		setResult(RESULT_OK, intent);

		mSourceImage[0] = null;
		mSourceImage[1] = null;
		mImageView.setImageBitmap(mSourceImage);
		mImageView.breakThread();

		// ZIPをオープンしていたら閉じる
		if (mImageMgr != null) {
			// 読み込み停止
			mImageMgr.setBreakTrigger();

			// キャッシュ読込スレッド停止
			mImageMgr.closeFiles();
			if (mDelShare) {
				// 共有一時ファイルの削除
				mImageMgr.deleteShareCache();
			}
		}

		if (mBitmapLoading) {
			mTerminate = true;
			return;
		}
		else {
			// 即終了
			System.gc();
			finish();
		}
	}

	// 現在ページ情報を保存
	private void saveCurrentPage() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mImageMgr != null) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			Editor ed = sp.edit();
			int savePage = mCurrentPage;
			int	maxpage = mImageMgr.length();
			Logcat.d(logLevel, "Url=" + DEF.createUrl(mFilePath, mUser, mPass));

			if (maxpage > 0) {
				// ページ数が0でないときは保存する
				Logcat.d(logLevel, "page=" + savePage + ", maxpage=" + maxpage);
				Intent intent = getIntent();
				intent.putExtra("Page", savePage);

				ed.putInt(DEF.createUrl(mFilePath, mUser, mPass) + "#maxpage", maxpage);
				ed.putInt(DEF.createUrl(mFilePath, mUser, mPass), savePage);

				if (mTimestamp != 0L) {
					ed.putInt(DEF.createUrl(mFilePath, mUser, mPass) + "#date", (int) (mTimestamp / 1000));
				}
				ed.apply();
			} else {
				// ページ数が0の時は保存しない
			}
		}


	}

	// アクティビティ一時停止時に保存される
	private void SaveCurrentSetting() {
		mGrayBackup = mGray;
		mColoringBackup = mColoring;
		mInvertBackup = mInvert;
		mMoireBackup = mMoire;
		mSharpenBackup = mSharpen;
		mBrightBackup = mBright;
		mGammaBackup = mGamma;
		mContrastBackup = mContrast;
		mHueBackup = mHue;
		mSaturationBackup = mSaturation;
		mRotateBackup = mRotate;
		mReverseOrderBackup = mReverseOrder;
		mChgPageBackup = mChgPage;
		mPageWayBackup = mPageWay;
		mScrlWayBackup = mScrlWay;
		mTopSingleBackup = mTopSingle;
		mBkLightBackup = mBkLight;
		mAlgoModeBackup = mAlgoMode;
		mDispModeBackup = mDispMode;
		mScaleModeBackup = mScaleMode;
		mMgnCutBackup = mMgnCut;
		mMgnCutColorBackup = mMgnCutColor;
		mPinchScaleBackup = mPinchScaleSel;
		mDisplayPositionBackup = mDisplayPosition;
	}

	// 他アクティビティからの復帰通知時に元に戻す
	private void LoadCurrentSetting() {
		mGray = mGrayBackup;
		mColoring = mColoringBackup;
		mInvert = mInvertBackup;
		mMoire = mMoireBackup;
		mSharpen = mSharpenBackup;
		mBright = mBrightBackup;
		mGamma = mGammaBackup;
		mContrast = mContrastBackup;
		mHue = mHueBackup;
		mSaturation = mSaturationBackup;
		mRotate = mRotateBackup;
		mReverseOrder = mReverseOrderBackup;
		mChgPage = mChgPageBackup;
		mPageWay = mPageWayBackup;
		mScrlWay = mScrlWayBackup;
		mTopSingle = mTopSingleBackup;
		mBkLight = mBkLightBackup;
		mAlgoMode = mAlgoModeBackup;
		mDispMode = mDispModeBackup;
		mScaleMode = mScaleModeBackup;
		mMgnCut = mMgnCutBackup;
		mMgnCutColor = mMgnCutColorBackup;
		mPinchScaleSel = mPinchScaleBackup;
		mDisplayPosition = mDisplayPositionBackup;
	}

	// 起動時のページ情報に戻す
	private void restoreCurrentPage() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mImageMgr != null && mImageMgr.length() > 0) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			Editor ed = sp.edit();
			if (mRestorePage == DEF.PAGENUMBER_UNREAD) {
				ed.remove(DEF.createUrl(mFilePath, mUser, mPass) + "#maxpage");
				ed.remove(DEF.createUrl(mFilePath, mUser, mPass));
				ed.remove(DEF.createUrl(mFilePath, mUser, mPass) + "#date");
			}
			else {
				ed.putInt(DEF.createUrl(mFilePath, mUser, mPass) + "#maxpage", mRestoreMaxPage);
				ed.putInt(DEF.createUrl(mFilePath, mUser, mPass), mRestorePage);

				if (mTimestamp != 0L) {
					ed.putInt(DEF.createUrl(mFilePath, mUser, mPass) + "#date", (int) (mTimestamp / 1000));
				}
			}

			if (mImageName != null && !mImageName.isEmpty()) {
				ed.putString("LastImage", mImageName);
			}
			ed.apply();
		}
	}

	private void saveLastFile() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		Editor ed = mSharedPreferences.edit();
		ed.putInt("LastServer", mServer);
		ed.putString("LastUri", mURI);
		ed.putString("LastPath", mPath);
		ed.putString("LastUser", mUser);
		ed.putString("LastPass", mPass);
		ed.putString("LastFile", mFileName);
		ed.putString("LastImage", mImageName);
		ed.putInt("LastOpen", DEF.LASTOPEN_IMAGE);
		ed.apply();
	}

	private void removeLastFile() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		Editor ed = mSharedPreferences.edit();
		ed.putInt("LastOpen", DEF.LASTOPEN_NONE);
		ed.apply();
	}

	private void saveHistory() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		Logcat.d(logLevel, "mServer=" + mServer + ", mURI=" + mURI + ", mPath=" + mPath
				+ ", mFileName=" + mFileName + ", mImageName=" + mImageName + ", mCurrentPage=" + mCurrentPage);
		// 履歴追加
		if (!mReadBreak && mImageMgr != null && mImageMgr.length() > 0) {
			if ((mFileName == null || mFileName.isEmpty()) && (mImageName != null && !mImageName.isEmpty())) {
				Logcat.d(logLevel, "画像ファイル指定.");
				// 画像ファイル直接指定
				RecordList.add(RecordList.TYPE_HISTORY, RecordItem.TYPE_IMAGEDIRECT
						, mServer, mPath, mFileName, new Date().getTime()
						, mImageMgr.mFileList[mCurrentPage].name, mCurrentPage, null);
			}
			else {
				Logcat.d(logLevel, "ディレクトリまたは圧縮ファイル.");
				// ディレクトリまたは圧縮ファイル
				RecordList.add(RecordList.TYPE_HISTORY, RecordItem.TYPE_IMAGE
						, mServer, mPath, mFileName, new Date().getTime()
						, mImageMgr.mFileList[mCurrentPage].name, mCurrentPage, null);
			}
		}
	}

	// プロファイルの保存
	private void SaveProfile(int index) {
		Editor ed = mSharedPreferences.edit();
		switch (index) {
			case 0:
				ed.putString(DEF.KEY_PROFILE_WORD_01, mProfileWord[0]);
				ed.putBoolean(DEF.KEY_PROFILE_GRAY_01, mGray);
				ed.putBoolean(DEF.KEY_PROFILE_COLORING_01, mColoring);
				ed.putBoolean(DEF.KEY_PROFILE_INVERT_01, mInvert);
				ed.putBoolean(DEF.KEY_PROFILE_MOIRE_01, mMoire);
				ed.putInt(DEF.KEY_PROFILE_SHARPEN_01, mSharpen);
				ed.putInt(DEF.KEY_PROFILE_BRIGHT_01, mBright);
				ed.putInt(DEF.KEY_PROFILE_GAMMA_01, mGamma);
				ed.putInt(DEF.KEY_PROFILE_CONTRAST_01, mContrast);
				ed.putInt(DEF.KEY_PROFILE_HUE_01, mHue);
				ed.putInt(DEF.KEY_PROFILE_SATURATION_01, mSaturation);
				ed.putInt(DEF.KEY_PROFILE_ROTATE_01, mRotate);
				ed.putBoolean(DEF.KEY_PROFILE_REVERSE_01, mReverseOrder);
				ed.putBoolean(DEF.KEY_PROFILE_CHGPAGE_01, mChgPage);
				ed.putInt(DEF.KEY_PROFILE_PAGEWAY_01, mPageWay);
				ed.putInt(DEF.KEY_PROFILE_SCRLWAY_01, mScrlWay);
				ed.putBoolean(DEF.KEY_PROFILE_TOPSINGLE_01, mTopSingle);
				ed.putInt(DEF.KEY_PROFILE_BKLIGHT_01, mBkLight);
				ed.putInt(DEF.KEY_PROFILE_ALGOMODE_01, mAlgoMode);
				ed.putInt(DEF.KEY_PROFILE_DISPMODE_01, mDispMode);
				ed.putInt(DEF.KEY_PROFILE_SCALEMODE_01, mScaleMode);
				ed.putInt(DEF.KEY_PROFILE_MGNCUT_01, mMgnCut);
				ed.putInt(DEF.KEY_PROFILE_MGNCUTCOLOR_01, mMgnCutColor);
				ed.putInt(DEF.KEY_PROFILE_PINCHSCALE_01, mPinchScale);
				ed.putInt(DEF.KEY_PROFILE_DISPLAYPOSITION_01, mDisplayPosition);
				break;
			case 1:
				ed.putString(DEF.KEY_PROFILE_WORD_02, mProfileWord[1]);
				ed.putBoolean(DEF.KEY_PROFILE_GRAY_02, mGray);
				ed.putBoolean(DEF.KEY_PROFILE_COLORING_02, mColoring);
				ed.putBoolean(DEF.KEY_PROFILE_INVERT_02, mInvert);
				ed.putBoolean(DEF.KEY_PROFILE_MOIRE_02, mMoire);
				ed.putInt(DEF.KEY_PROFILE_SHARPEN_02, mSharpen);
				ed.putInt(DEF.KEY_PROFILE_BRIGHT_02, mBright);
				ed.putInt(DEF.KEY_PROFILE_GAMMA_02, mGamma);
				ed.putInt(DEF.KEY_PROFILE_CONTRAST_02, mContrast);
				ed.putInt(DEF.KEY_PROFILE_HUE_02, mHue);
				ed.putInt(DEF.KEY_PROFILE_SATURATION_02, mSaturation);
				ed.putInt(DEF.KEY_PROFILE_ROTATE_02, mRotate);
				ed.putBoolean(DEF.KEY_PROFILE_REVERSE_02, mReverseOrder);
				ed.putBoolean(DEF.KEY_PROFILE_CHGPAGE_02, mChgPage);
				ed.putInt(DEF.KEY_PROFILE_PAGEWAY_02, mPageWay);
				ed.putInt(DEF.KEY_PROFILE_SCRLWAY_02, mScrlWay);
				ed.putBoolean(DEF.KEY_PROFILE_TOPSINGLE_02, mTopSingle);
				ed.putInt(DEF.KEY_PROFILE_BKLIGHT_02, mBkLight);
				ed.putInt(DEF.KEY_PROFILE_ALGOMODE_02, mAlgoMode);
				ed.putInt(DEF.KEY_PROFILE_DISPMODE_02, mDispMode);
				ed.putInt(DEF.KEY_PROFILE_SCALEMODE_02, mScaleMode);
				ed.putInt(DEF.KEY_PROFILE_MGNCUT_02, mMgnCut);
				ed.putInt(DEF.KEY_PROFILE_MGNCUTCOLOR_02, mMgnCutColor);
				ed.putInt(DEF.KEY_PROFILE_PINCHSCALE_02, mPinchScale);
				ed.putInt(DEF.KEY_PROFILE_DISPLAYPOSITION_02, mDisplayPosition);
				break;
			case 2:
				ed.putString(DEF.KEY_PROFILE_WORD_03, mProfileWord[2]);
				ed.putBoolean(DEF.KEY_PROFILE_GRAY_03, mGray);
				ed.putBoolean(DEF.KEY_PROFILE_COLORING_03, mColoring);
				ed.putBoolean(DEF.KEY_PROFILE_INVERT_03, mInvert);
				ed.putBoolean(DEF.KEY_PROFILE_MOIRE_03, mMoire);
				ed.putInt(DEF.KEY_PROFILE_SHARPEN_03, mSharpen);
				ed.putInt(DEF.KEY_PROFILE_BRIGHT_03, mBright);
				ed.putInt(DEF.KEY_PROFILE_GAMMA_03, mGamma);
				ed.putInt(DEF.KEY_PROFILE_CONTRAST_03, mContrast);
				ed.putInt(DEF.KEY_PROFILE_HUE_03, mHue);
				ed.putInt(DEF.KEY_PROFILE_SATURATION_03, mSaturation);
				ed.putInt(DEF.KEY_PROFILE_ROTATE_03, mRotate);
				ed.putBoolean(DEF.KEY_PROFILE_REVERSE_03, mReverseOrder);
				ed.putBoolean(DEF.KEY_PROFILE_CHGPAGE_03, mChgPage);
				ed.putInt(DEF.KEY_PROFILE_PAGEWAY_03, mPageWay);
				ed.putInt(DEF.KEY_PROFILE_SCRLWAY_03, mScrlWay);
				ed.putBoolean(DEF.KEY_PROFILE_TOPSINGLE_03, mTopSingle);
				ed.putInt(DEF.KEY_PROFILE_BKLIGHT_03, mBkLight);
				ed.putInt(DEF.KEY_PROFILE_ALGOMODE_03, mAlgoMode);
				ed.putInt(DEF.KEY_PROFILE_DISPMODE_03, mDispMode);
				ed.putInt(DEF.KEY_PROFILE_SCALEMODE_03, mScaleMode);
				ed.putInt(DEF.KEY_PROFILE_MGNCUT_03, mMgnCut);
				ed.putInt(DEF.KEY_PROFILE_MGNCUTCOLOR_03, mMgnCutColor);
				ed.putInt(DEF.KEY_PROFILE_PINCHSCALE_03, mPinchScale);
				ed.putInt(DEF.KEY_PROFILE_DISPLAYPOSITION_03, mDisplayPosition);
				break;
			case 3:
				ed.putString(DEF.KEY_PROFILE_WORD_04, mProfileWord[3]);
				ed.putBoolean(DEF.KEY_PROFILE_GRAY_04, mGray);
				ed.putBoolean(DEF.KEY_PROFILE_COLORING_04, mColoring);
				ed.putBoolean(DEF.KEY_PROFILE_INVERT_04, mInvert);
				ed.putBoolean(DEF.KEY_PROFILE_MOIRE_04, mMoire);
				ed.putInt(DEF.KEY_PROFILE_SHARPEN_04, mSharpen);
				ed.putInt(DEF.KEY_PROFILE_BRIGHT_04, mBright);
				ed.putInt(DEF.KEY_PROFILE_GAMMA_04, mGamma);
				ed.putInt(DEF.KEY_PROFILE_CONTRAST_04, mContrast);
				ed.putInt(DEF.KEY_PROFILE_HUE_04, mHue);
				ed.putInt(DEF.KEY_PROFILE_SATURATION_04, mSaturation);
				ed.putInt(DEF.KEY_PROFILE_ROTATE_04, mRotate);
				ed.putBoolean(DEF.KEY_PROFILE_REVERSE_04, mReverseOrder);
				ed.putBoolean(DEF.KEY_PROFILE_CHGPAGE_04, mChgPage);
				ed.putInt(DEF.KEY_PROFILE_PAGEWAY_04, mPageWay);
				ed.putInt(DEF.KEY_PROFILE_SCRLWAY_04, mScrlWay);
				ed.putBoolean(DEF.KEY_PROFILE_TOPSINGLE_04, mTopSingle);
				ed.putInt(DEF.KEY_PROFILE_BKLIGHT_04, mBkLight);
				ed.putInt(DEF.KEY_PROFILE_ALGOMODE_04, mAlgoMode);
				ed.putInt(DEF.KEY_PROFILE_DISPMODE_04, mDispMode);
				ed.putInt(DEF.KEY_PROFILE_SCALEMODE_04, mScaleMode);
				ed.putInt(DEF.KEY_PROFILE_MGNCUT_04, mMgnCut);
				ed.putInt(DEF.KEY_PROFILE_MGNCUTCOLOR_04, mMgnCutColor);
				ed.putInt(DEF.KEY_PROFILE_PINCHSCALE_04, mPinchScale);
				ed.putInt(DEF.KEY_PROFILE_DISPLAYPOSITION_04, mDisplayPosition);
				break;
			case 4:
				ed.putString(DEF.KEY_PROFILE_WORD_05, mProfileWord[4]);
				ed.putBoolean(DEF.KEY_PROFILE_GRAY_05, mGray);
				ed.putBoolean(DEF.KEY_PROFILE_COLORING_05, mColoring);
				ed.putBoolean(DEF.KEY_PROFILE_INVERT_05, mInvert);
				ed.putBoolean(DEF.KEY_PROFILE_MOIRE_05, mMoire);
				ed.putInt(DEF.KEY_PROFILE_SHARPEN_05, mSharpen);
				ed.putInt(DEF.KEY_PROFILE_BRIGHT_05, mBright);
				ed.putInt(DEF.KEY_PROFILE_GAMMA_05, mGamma);
				ed.putInt(DEF.KEY_PROFILE_CONTRAST_05, mContrast);
				ed.putInt(DEF.KEY_PROFILE_HUE_05, mHue);
				ed.putInt(DEF.KEY_PROFILE_SATURATION_05, mSaturation);
				ed.putInt(DEF.KEY_PROFILE_ROTATE_05, mRotate);
				ed.putBoolean(DEF.KEY_PROFILE_REVERSE_05, mReverseOrder);
				ed.putBoolean(DEF.KEY_PROFILE_CHGPAGE_05, mChgPage);
				ed.putInt(DEF.KEY_PROFILE_PAGEWAY_05, mPageWay);
				ed.putInt(DEF.KEY_PROFILE_SCRLWAY_05, mScrlWay);
				ed.putBoolean(DEF.KEY_PROFILE_TOPSINGLE_05, mTopSingle);
				ed.putInt(DEF.KEY_PROFILE_BKLIGHT_05, mBkLight);
				ed.putInt(DEF.KEY_PROFILE_ALGOMODE_05, mAlgoMode);
				ed.putInt(DEF.KEY_PROFILE_DISPMODE_05, mDispMode);
				ed.putInt(DEF.KEY_PROFILE_SCALEMODE_05, mScaleMode);
				ed.putInt(DEF.KEY_PROFILE_MGNCUT_05, mMgnCut);
				ed.putInt(DEF.KEY_PROFILE_MGNCUTCOLOR_05, mMgnCutColor);
				ed.putInt(DEF.KEY_PROFILE_PINCHSCALE_05, mPinchScale);
				ed.putInt(DEF.KEY_PROFILE_DISPLAYPOSITION_05, mDisplayPosition);
				break;
		}
		ed.apply();
	}

	// プロファイルのロード
	private void LoadProfile(int index) {
		switch (index) {
			case 0:
				if (mProfileWord[0].equals("")) {
					// 登録されていなければ戻る
					return;
				}
				// ロードに失敗した場合は元の値を入れる
				mGray = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_GRAY_01, SetImageActivity.getGray(mSharedPreferences));
				mColoring = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_COLORING_01, SetImageActivity.getGray(mSharedPreferences));
				mInvert = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_INVERT_01, SetImageActivity.getInvert(mSharedPreferences));
				mMoire = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_MOIRE_01, SetImageActivity.getMoire(mSharedPreferences));
				mSharpen = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SHARPEN_01, SetImageActivity.getSharpen(mSharedPreferences));
				mBright = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_BRIGHT_01, SetImageActivity.getBright(mSharedPreferences));
				mGamma = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_GAMMA_01, SetImageActivity.getGamma(mSharedPreferences));
				mContrast = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_CONTRAST_01, SetImageActivity.getContrast(mSharedPreferences));
				mHue = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_HUE_01, SetImageActivity.getHue(mSharedPreferences));
				mSaturation = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SATURATION_01, SetImageActivity.getSaturation(mSharedPreferences));
				mRotate = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_ROTATE_01, mRotate);
				mReverseOrderProfile = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_REVERSE_01, mReverseOrder);
				mChgPageProfile = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_CHGPAGE_01, SetImageText.getChgPage(mSharedPreferences));
				mPageWay = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_PAGEWAY_01, SetImageActivity.getPageWay(mSharedPreferences));
				mScrlWay = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SCRLWAY_01, SetImageActivity.getScrlWay(mSharedPreferences));
				mTopSingle = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_TOPSINGLE_01, SetImageActivity.getTopSingle(mSharedPreferences));
				mBkLight = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_BKLIGHT_01, SetImageActivity.getBkLight(mSharedPreferences));
				mAlgoMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_ALGOMODE_01, SetImageActivity.getAlgoMode(mSharedPreferences));
				mDispMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_DISPMODE_01, SetImageActivity.getInitView(mSharedPreferences));
				mScaleMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SCALEMODE_01, SetImageActivity.getIniScale(mSharedPreferences));
				mMgnCut = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_MGNCUT_01, SetImageActivity.getMgnCut(mSharedPreferences));
				mMgnCutColor = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_MGNCUTCOLOR_01, SetImageActivity.getMgnCutColor(mSharedPreferences));
				mPinchScale = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_PINCHSCALE_01, SetImageActivity.getPinScale(mSharedPreferences));
				mDisplayPosition = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_DISPLAYPOSITION_01, SetImageActivity.getDisplayPosition(mSharedPreferences));
				break;
			case 1:
				if (mProfileWord[1].equals("")) {
					// 登録されていなければ戻る
					return;
				}
				// ロードに失敗した場合は元の値を入れる
				mGray = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_GRAY_02, SetImageActivity.getGray(mSharedPreferences));
				mColoring = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_COLORING_02, SetImageActivity.getGray(mSharedPreferences));
				mInvert = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_INVERT_02, SetImageActivity.getInvert(mSharedPreferences));
				mMoire = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_MOIRE_02, SetImageActivity.getMoire(mSharedPreferences));
				mSharpen = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SHARPEN_02, SetImageActivity.getSharpen(mSharedPreferences));
				mBright = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_BRIGHT_02, SetImageActivity.getBright(mSharedPreferences));
				mGamma = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_GAMMA_02, SetImageActivity.getGamma(mSharedPreferences));
				mContrast = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_CONTRAST_02, SetImageActivity.getContrast(mSharedPreferences));
				mHue = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_HUE_02, SetImageActivity.getHue(mSharedPreferences));
				mSaturation = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SATURATION_02, SetImageActivity.getSaturation(mSharedPreferences));
				mRotate = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_ROTATE_02, mRotate);
				mReverseOrderProfile = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_REVERSE_02, mReverseOrder);
				mChgPageProfile = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_CHGPAGE_02, SetImageText.getChgPage(mSharedPreferences));
				mPageWay = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_PAGEWAY_02, SetImageActivity.getPageWay(mSharedPreferences));
				mScrlWay = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SCRLWAY_02, SetImageActivity.getScrlWay(mSharedPreferences));
				mTopSingle = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_TOPSINGLE_02, SetImageActivity.getTopSingle(mSharedPreferences));
				mBkLight = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_BKLIGHT_02, SetImageActivity.getBkLight(mSharedPreferences));
				mAlgoMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_ALGOMODE_02, SetImageActivity.getAlgoMode(mSharedPreferences));
				mDispMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_DISPMODE_02, SetImageActivity.getInitView(mSharedPreferences));
				mScaleMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SCALEMODE_02, SetImageActivity.getIniScale(mSharedPreferences));
				mMgnCut = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_MGNCUT_02, SetImageActivity.getMgnCut(mSharedPreferences));
				mMgnCutColor = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_MGNCUTCOLOR_02, SetImageActivity.getMgnCutColor(mSharedPreferences));
				mPinchScale = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_PINCHSCALE_02, SetImageActivity.getPinScale(mSharedPreferences));
				mDisplayPosition = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_DISPLAYPOSITION_02, SetImageActivity.getDisplayPosition(mSharedPreferences));
				break;
			case 2:
				if (mProfileWord[2].equals("")) {
					// 登録されていなければ戻る
					return;
				}
				// ロードに失敗した場合は元の値を入れる
				mGray = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_GRAY_03, SetImageActivity.getGray(mSharedPreferences));
				mColoring = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_COLORING_03, SetImageActivity.getGray(mSharedPreferences));
				mInvert = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_INVERT_03, SetImageActivity.getInvert(mSharedPreferences));
				mMoire = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_MOIRE_03, SetImageActivity.getMoire(mSharedPreferences));
				mSharpen = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SHARPEN_03, SetImageActivity.getSharpen(mSharedPreferences));
				mBright = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_BRIGHT_03, SetImageActivity.getBright(mSharedPreferences));
				mGamma = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_GAMMA_03, SetImageActivity.getGamma(mSharedPreferences));
				mContrast = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_CONTRAST_03, SetImageActivity.getContrast(mSharedPreferences));
				mHue = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_HUE_03, SetImageActivity.getHue(mSharedPreferences));
				mSaturation = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SATURATION_03, SetImageActivity.getSaturation(mSharedPreferences));
				mRotate = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_ROTATE_03, mRotate);
				mReverseOrderProfile = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_REVERSE_03, mReverseOrder);
				mChgPageProfile = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_CHGPAGE_03, SetImageText.getChgPage(mSharedPreferences));
				mPageWay = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_PAGEWAY_03, SetImageActivity.getPageWay(mSharedPreferences));
				mScrlWay = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SCRLWAY_03, SetImageActivity.getScrlWay(mSharedPreferences));
				mTopSingle = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_TOPSINGLE_03, SetImageActivity.getTopSingle(mSharedPreferences));
				mBkLight = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_BKLIGHT_03, SetImageActivity.getBkLight(mSharedPreferences));
				mAlgoMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_ALGOMODE_03, SetImageActivity.getAlgoMode(mSharedPreferences));
				mDispMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_DISPMODE_03, SetImageActivity.getInitView(mSharedPreferences));
				mScaleMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SCALEMODE_03, SetImageActivity.getIniScale(mSharedPreferences));
				mMgnCut = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_MGNCUT_03, SetImageActivity.getMgnCut(mSharedPreferences));
				mMgnCutColor = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_MGNCUTCOLOR_03, SetImageActivity.getMgnCutColor(mSharedPreferences));
				mPinchScale = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_PINCHSCALE_03, SetImageActivity.getPinScale(mSharedPreferences));
				mDisplayPosition = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_DISPLAYPOSITION_03, SetImageActivity.getDisplayPosition(mSharedPreferences));
				break;
			case 3:
				if (mProfileWord[3].equals("")) {
					// 登録されていなければ戻る
					return;
				}
				// ロードに失敗した場合は元の値を入れる
				mGray = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_GRAY_04, SetImageActivity.getGray(mSharedPreferences));
				mColoring = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_COLORING_04, SetImageActivity.getGray(mSharedPreferences));
				mInvert = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_INVERT_04, SetImageActivity.getInvert(mSharedPreferences));
				mMoire = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_MOIRE_04, SetImageActivity.getMoire(mSharedPreferences));
				mSharpen = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SHARPEN_04, SetImageActivity.getSharpen(mSharedPreferences));
				mBright = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_BRIGHT_04, SetImageActivity.getBright(mSharedPreferences));
				mGamma = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_GAMMA_04, SetImageActivity.getGamma(mSharedPreferences));
				mContrast = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_CONTRAST_04, SetImageActivity.getContrast(mSharedPreferences));
				mHue = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_HUE_04, SetImageActivity.getHue(mSharedPreferences));
				mSaturation = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SATURATION_04, SetImageActivity.getSaturation(mSharedPreferences));
				mRotate = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_ROTATE_04, mRotate);
				mReverseOrderProfile = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_REVERSE_04, mReverseOrder);
				mChgPageProfile = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_CHGPAGE_04, SetImageText.getChgPage(mSharedPreferences));
				mPageWay = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_PAGEWAY_04, SetImageActivity.getPageWay(mSharedPreferences));
				mScrlWay = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SCRLWAY_04, SetImageActivity.getScrlWay(mSharedPreferences));
				mTopSingle = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_TOPSINGLE_04, SetImageActivity.getTopSingle(mSharedPreferences));
				mBkLight = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_BKLIGHT_04, SetImageActivity.getBkLight(mSharedPreferences));
				mAlgoMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_ALGOMODE_04, SetImageActivity.getAlgoMode(mSharedPreferences));
				mDispMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_DISPMODE_04, SetImageActivity.getInitView(mSharedPreferences));
				mScaleMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SCALEMODE_04, SetImageActivity.getIniScale(mSharedPreferences));
				mMgnCut = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_MGNCUT_04, SetImageActivity.getMgnCut(mSharedPreferences));
				mMgnCutColor = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_MGNCUTCOLOR_04, SetImageActivity.getMgnCutColor(mSharedPreferences));
				mPinchScale = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_PINCHSCALE_04, SetImageActivity.getPinScale(mSharedPreferences));
				mDisplayPosition = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_DISPLAYPOSITION_04, SetImageActivity.getDisplayPosition(mSharedPreferences));
				break;
			case 4:
				if (mProfileWord[4].equals("")) {
					// 登録されていなければ戻る
					return;
				}
				// ロードに失敗した場合は元の値を入れる
				mGray = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_GRAY_05, SetImageActivity.getGray(mSharedPreferences));
				mColoring = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_COLORING_05, SetImageActivity.getGray(mSharedPreferences));
				mInvert = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_INVERT_05, SetImageActivity.getInvert(mSharedPreferences));
				mMoire = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_MOIRE_05, SetImageActivity.getMoire(mSharedPreferences));
				mSharpen = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SHARPEN_05, SetImageActivity.getSharpen(mSharedPreferences));
				mBright = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_BRIGHT_05, SetImageActivity.getBright(mSharedPreferences));
				mGamma = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_GAMMA_05, SetImageActivity.getGamma(mSharedPreferences));
				mContrast = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_CONTRAST_05, SetImageActivity.getContrast(mSharedPreferences));
				mHue = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_HUE_05, SetImageActivity.getHue(mSharedPreferences));
				mSaturation = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SATURATION_05, SetImageActivity.getSaturation(mSharedPreferences));
				mRotate = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_ROTATE_05, mRotate);
				mReverseOrderProfile = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_REVERSE_05, mReverseOrder);
				mChgPageProfile = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_CHGPAGE_05, SetImageText.getChgPage(mSharedPreferences));
				mPageWay = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_PAGEWAY_05, SetImageActivity.getPageWay(mSharedPreferences));
				mScrlWay = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SCRLWAY_05, SetImageActivity.getScrlWay(mSharedPreferences));
				mTopSingle = DEF.getBoolean(mSharedPreferences, DEF.KEY_PROFILE_TOPSINGLE_05, SetImageActivity.getTopSingle(mSharedPreferences));
				mBkLight = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_BKLIGHT_05, SetImageActivity.getBkLight(mSharedPreferences));
				mAlgoMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_ALGOMODE_05, SetImageActivity.getAlgoMode(mSharedPreferences));
				mDispMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_DISPMODE_05, SetImageActivity.getInitView(mSharedPreferences));
				mScaleMode = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_SCALEMODE_05, SetImageActivity.getIniScale(mSharedPreferences));
				mMgnCut = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_MGNCUT_05, SetImageActivity.getMgnCut(mSharedPreferences));
				mMgnCutColor = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_MGNCUTCOLOR_05, SetImageActivity.getMgnCutColor(mSharedPreferences));
				mPinchScale = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_PINCHSCALE_05, SetImageActivity.getPinScale(mSharedPreferences));
				mDisplayPosition = DEF.getInt(mSharedPreferences, DEF.KEY_PROFILE_DISPLAYPOSITION_05, SetImageActivity.getDisplayPosition(mSharedPreferences));
				break;
		}
		if (mReverseOrder != mReverseOrderProfile) {
			// ページ逆順
			mReverseOrder = !mReverseOrder;
			mImageMgr.reverseOrder();
		}
		if (mChgPage != mChgPageProfile) {
			// 操作方向の入れ替え
			mChgPage = !mChgPage;
			mGuideView.setGuideSize(mClickArea, mTapPattern, mTapRate, mChgPage, mOldMenu);
		}
		mPinchScaleSel = mPinchScale;
		// 画面回転
		mImageView.setRotate(mRotate);
		// 画像と表示領域を比較してはみ出る量を算出
		setImageConfig();
		// ビットマップの読み込み
		setBitmapImage();
		// 表示のコンフィグレーション
		setViewConfig();
		// 画面サイズの更新
		mImageView.updateScreenSize();

		// 描画スレッド停止
		mImageView.lockDraw();
		synchronized (mImageView) {
			// スケーリング変更
			mImageMgr.setImageScale(mPinchScale);
			// イメージ拡大縮小
			ImageScaling();
		}
		// ビットマップを調整
		this.updateOverSize(true);
		// 描画スレッド開始
		mImageView.update(true);

		// バックライト設定
		float l = -1;
		if (mBkLight <= 10) {
			l = (float)mBkLight / 10;
		}
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = l;
		getWindow().setAttributes(lp);
	}

	// プロファイルの削除
	private void DeleteProfile(int index) {
		Editor ed = mSharedPreferences.edit();
		switch (index) {
			case 0:
				if (mProfileWord[0].equals("")) {
					// 登録されていなければ戻る
					return;
				}
				mProfileWord[0] = "";
				ed.remove(DEF.KEY_PROFILE_WORD_01);
				ed.remove(DEF.KEY_PROFILE_GRAY_01);
				ed.remove(DEF.KEY_PROFILE_COLORING_01);
				ed.remove(DEF.KEY_PROFILE_INVERT_01);
				ed.remove(DEF.KEY_PROFILE_MOIRE_01);
				ed.remove(DEF.KEY_PROFILE_SHARPEN_01);
				ed.remove(DEF.KEY_PROFILE_BRIGHT_01);
				ed.remove(DEF.KEY_PROFILE_GAMMA_01);
				ed.remove(DEF.KEY_PROFILE_CONTRAST_01);
				ed.remove(DEF.KEY_PROFILE_HUE_01);
				ed.remove(DEF.KEY_PROFILE_SATURATION_01);
				ed.remove(DEF.KEY_PROFILE_ROTATE_01);
				ed.remove(DEF.KEY_PROFILE_REVERSE_01);
				ed.remove(DEF.KEY_PROFILE_CHGPAGE_01);
				ed.remove(DEF.KEY_PROFILE_PAGEWAY_01);
				ed.remove(DEF.KEY_PROFILE_SCRLWAY_01);
				ed.remove(DEF.KEY_PROFILE_TOPSINGLE_01);
				ed.remove(DEF.KEY_PROFILE_BKLIGHT_01);
				ed.remove(DEF.KEY_PROFILE_ALGOMODE_01);
				ed.remove(DEF.KEY_PROFILE_DISPMODE_01);
				ed.remove(DEF.KEY_PROFILE_SCALEMODE_01);
				ed.remove(DEF.KEY_PROFILE_MGNCUT_01);
				ed.remove(DEF.KEY_PROFILE_MGNCUTCOLOR_01);
				ed.remove(DEF.KEY_PROFILE_PINCHSCALE_01);
				ed.remove(DEF.KEY_PROFILE_DISPLAYPOSITION_01);
				break;
			case 1:
				if (mProfileWord[1].equals("")) {
					// 登録されていなければ戻る
					return;
				}
				mProfileWord[1] = "";
				ed.remove(DEF.KEY_PROFILE_WORD_02);
				ed.remove(DEF.KEY_PROFILE_GRAY_02);
				ed.remove(DEF.KEY_PROFILE_COLORING_02);
				ed.remove(DEF.KEY_PROFILE_INVERT_02);
				ed.remove(DEF.KEY_PROFILE_MOIRE_02);
				ed.remove(DEF.KEY_PROFILE_SHARPEN_02);
				ed.remove(DEF.KEY_PROFILE_BRIGHT_02);
				ed.remove(DEF.KEY_PROFILE_GAMMA_02);
				ed.remove(DEF.KEY_PROFILE_CONTRAST_02);
				ed.remove(DEF.KEY_PROFILE_HUE_02);
				ed.remove(DEF.KEY_PROFILE_SATURATION_02);
				ed.remove(DEF.KEY_PROFILE_ROTATE_02);
				ed.remove(DEF.KEY_PROFILE_REVERSE_02);
				ed.remove(DEF.KEY_PROFILE_CHGPAGE_02);
				ed.remove(DEF.KEY_PROFILE_PAGEWAY_02);
				ed.remove(DEF.KEY_PROFILE_SCRLWAY_02);
				ed.remove(DEF.KEY_PROFILE_TOPSINGLE_02);
				ed.remove(DEF.KEY_PROFILE_BKLIGHT_02);
				ed.remove(DEF.KEY_PROFILE_ALGOMODE_02);
				ed.remove(DEF.KEY_PROFILE_DISPMODE_02);
				ed.remove(DEF.KEY_PROFILE_SCALEMODE_02);
				ed.remove(DEF.KEY_PROFILE_MGNCUT_02);
				ed.remove(DEF.KEY_PROFILE_MGNCUTCOLOR_02);
				ed.remove(DEF.KEY_PROFILE_PINCHSCALE_02);
				ed.remove(DEF.KEY_PROFILE_DISPLAYPOSITION_02);
				break;
			case 2:
				if (mProfileWord[2].equals("")) {
					// 登録されていなければ戻る
					return;
				}
				mProfileWord[2] = "";
				ed.remove(DEF.KEY_PROFILE_WORD_03);
				ed.remove(DEF.KEY_PROFILE_GRAY_03);
				ed.remove(DEF.KEY_PROFILE_COLORING_03);
				ed.remove(DEF.KEY_PROFILE_INVERT_03);
				ed.remove(DEF.KEY_PROFILE_MOIRE_03);
				ed.remove(DEF.KEY_PROFILE_SHARPEN_03);
				ed.remove(DEF.KEY_PROFILE_BRIGHT_03);
				ed.remove(DEF.KEY_PROFILE_GAMMA_03);
				ed.remove(DEF.KEY_PROFILE_CONTRAST_03);
				ed.remove(DEF.KEY_PROFILE_HUE_03);
				ed.remove(DEF.KEY_PROFILE_SATURATION_03);
				ed.remove(DEF.KEY_PROFILE_ROTATE_03);
				ed.remove(DEF.KEY_PROFILE_REVERSE_03);
				ed.remove(DEF.KEY_PROFILE_CHGPAGE_03);
				ed.remove(DEF.KEY_PROFILE_PAGEWAY_03);
				ed.remove(DEF.KEY_PROFILE_SCRLWAY_03);
				ed.remove(DEF.KEY_PROFILE_TOPSINGLE_03);
				ed.remove(DEF.KEY_PROFILE_BKLIGHT_03);
				ed.remove(DEF.KEY_PROFILE_ALGOMODE_03);
				ed.remove(DEF.KEY_PROFILE_DISPMODE_03);
				ed.remove(DEF.KEY_PROFILE_SCALEMODE_03);
				ed.remove(DEF.KEY_PROFILE_MGNCUT_03);
				ed.remove(DEF.KEY_PROFILE_MGNCUTCOLOR_03);
				ed.remove(DEF.KEY_PROFILE_PINCHSCALE_03);
				ed.remove(DEF.KEY_PROFILE_DISPLAYPOSITION_03);
				break;
			case 3:
				if (mProfileWord[3].equals("")) {
					// 登録されていなければ戻る
					return;
				}
				mProfileWord[3] = "";
				ed.remove(DEF.KEY_PROFILE_WORD_04);
				ed.remove(DEF.KEY_PROFILE_GRAY_04);
				ed.remove(DEF.KEY_PROFILE_COLORING_04);
				ed.remove(DEF.KEY_PROFILE_INVERT_04);
				ed.remove(DEF.KEY_PROFILE_MOIRE_04);
				ed.remove(DEF.KEY_PROFILE_SHARPEN_04);
				ed.remove(DEF.KEY_PROFILE_BRIGHT_04);
				ed.remove(DEF.KEY_PROFILE_GAMMA_04);
				ed.remove(DEF.KEY_PROFILE_CONTRAST_04);
				ed.remove(DEF.KEY_PROFILE_HUE_04);
				ed.remove(DEF.KEY_PROFILE_SATURATION_04);
				ed.remove(DEF.KEY_PROFILE_ROTATE_04);
				ed.remove(DEF.KEY_PROFILE_REVERSE_04);
				ed.remove(DEF.KEY_PROFILE_CHGPAGE_04);
				ed.remove(DEF.KEY_PROFILE_PAGEWAY_04);
				ed.remove(DEF.KEY_PROFILE_SCRLWAY_04);
				ed.remove(DEF.KEY_PROFILE_TOPSINGLE_04);
				ed.remove(DEF.KEY_PROFILE_BKLIGHT_04);
				ed.remove(DEF.KEY_PROFILE_ALGOMODE_04);
				ed.remove(DEF.KEY_PROFILE_DISPMODE_04);
				ed.remove(DEF.KEY_PROFILE_SCALEMODE_04);
				ed.remove(DEF.KEY_PROFILE_MGNCUT_04);
				ed.remove(DEF.KEY_PROFILE_MGNCUTCOLOR_04);
				ed.remove(DEF.KEY_PROFILE_PINCHSCALE_04);
				ed.remove(DEF.KEY_PROFILE_DISPLAYPOSITION_04);
				break;
			case 4:
				if (mProfileWord[4].equals("")) {
					// 登録されていなければ戻る
					return;
				}
				mProfileWord[4] = "";
				ed.remove(DEF.KEY_PROFILE_WORD_05);
				ed.remove(DEF.KEY_PROFILE_GRAY_05);
				ed.remove(DEF.KEY_PROFILE_COLORING_05);
				ed.remove(DEF.KEY_PROFILE_INVERT_05);
				ed.remove(DEF.KEY_PROFILE_MOIRE_05);
				ed.remove(DEF.KEY_PROFILE_SHARPEN_05);
				ed.remove(DEF.KEY_PROFILE_BRIGHT_05);
				ed.remove(DEF.KEY_PROFILE_GAMMA_05);
				ed.remove(DEF.KEY_PROFILE_CONTRAST_05);
				ed.remove(DEF.KEY_PROFILE_HUE_05);
				ed.remove(DEF.KEY_PROFILE_SATURATION_05);
				ed.remove(DEF.KEY_PROFILE_ROTATE_05);
				ed.remove(DEF.KEY_PROFILE_REVERSE_05);
				ed.remove(DEF.KEY_PROFILE_CHGPAGE_05);
				ed.remove(DEF.KEY_PROFILE_PAGEWAY_05);
				ed.remove(DEF.KEY_PROFILE_SCRLWAY_05);
				ed.remove(DEF.KEY_PROFILE_TOPSINGLE_05);
				ed.remove(DEF.KEY_PROFILE_BKLIGHT_05);
				ed.remove(DEF.KEY_PROFILE_ALGOMODE_05);
				ed.remove(DEF.KEY_PROFILE_DISPMODE_05);
				ed.remove(DEF.KEY_PROFILE_SCALEMODE_05);
				ed.remove(DEF.KEY_PROFILE_MGNCUT_05);
				ed.remove(DEF.KEY_PROFILE_MGNCUTCOLOR_05);
				ed.remove(DEF.KEY_PROFILE_PINCHSCALE_05);
				ed.remove(DEF.KEY_PROFILE_DISPLAYPOSITION_05);
				break;
		}
		ed.apply();
	}

	public static void UpdateTouchPanelData() {
		if (mImageView != null) {
			mImageView.ViewTapSw(true);
		}
	}

}
