package src.comitton.textview;

import io.documentnode.epub4j.domain.Spine;
import jp.dip.muracoro.comittonx.BuildConfig;
import jp.dip.muracoro.comittonx.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.os.Vibrator;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.webkit.JavascriptInterface;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.annotation.SuppressLint;

import src.comitton.common.CustomKeySharedData;
import src.comitton.config.SetConfigActivity;
import src.comitton.config.SetEpubActivity;
import src.comitton.config.SetFileColorActivity;
import src.comitton.config.SetHardwareEpubWebViewKeyActivity;
import src.comitton.config.SetImageActivity;
import src.comitton.config.SetImageText;
import src.comitton.config.SetImageTextColorActivity;
import src.comitton.config.SetImageTextDetailActivity;
import src.comitton.config.SetNoiseActivity;
import src.comitton.dialog.BookmarkDialog;
import src.comitton.dialog.CloseDialog;
import src.comitton.dialog.EpubWebViewConfigDialog;
import src.comitton.dialog.Information;
import src.comitton.dialog.ListDialog;
import src.comitton.dialog.MenuDialog.MenuSelectListener;
import src.comitton.dialog.BookmarkDialog.BookmarkListenerInterface;
import src.comitton.dialog.TextInputDialog;
import src.comitton.dialog.TextInputDialog.SearchListener;
import src.comitton.dialog.ToolbarDialog;
import src.comitton.config.SetHardwareTextViewerKeyActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.preference.PreferenceManager;
import androidx.activity.OnBackPressedCallback;

import javax.xml.parsers.DocumentBuilderFactory;

import org.codelibs.jcifs.smb.CIFSContext;
import org.codelibs.jcifs.smb.context.SingletonContext;
import org.codelibs.jcifs.smb.impl.NtlmPasswordAuthenticator;
import org.codelibs.jcifs.smb.impl.SmbFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

import io.documentnode.epub4j.domain.Book;
import io.documentnode.epub4j.domain.Resource;
import io.documentnode.epub4j.epub.EpubReader;
import io.documentnode.epub4j.domain.SpineReference;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import src.comitton.common.Logcat;
import src.comitton.common.DEF;
import src.comitton.common.EpubWebViewSharedData;
import src.comitton.config.SetCommonActivity;
import src.comitton.config.SetTextActivity;
import src.comitton.cropimageview.CropImageActivity;
import src.comitton.dialog.PageSelectDialog;
import src.comitton.dialog.TabDialogFragment;
import src.comitton.dialog.ToolbarEditDialog;
import src.comitton.fileaccess.FileAccess;
import src.comitton.fileview.FileSelectActivity;
import src.comitton.fileview.data.FileData;
import src.comitton.fileview.data.RecordItem;
import src.comitton.fileview.filelist.RecordList;
import src.comitton.helpview.HelpActivity;
import src.comitton.imageview.ImageActivity;
import src.comitton.imageview.PageSelectListener;
import src.comitton.common.GuideView;
import src.comitton.imageview.TouchPanelView;
import src.comitton.noise.NoiseSwitch;

import android.widget.Toast;

import net.sf.sevenzipjbinding.IInStream;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jsoup.Jsoup;

public class EpubWebViewActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, Handler.Callback, MenuSelectListener, PageSelectListener, BookmarkListenerInterface {
	private static final String TAG = "EPUB_DEBUG";

	private EpubWebViewActivity mActivity;
	private boolean mNotice = false;
	private boolean mNoSleep = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;

	private GestureDetectorCompat mDetector;
	private TabDialogFragment mMenuDialog;

	private Book currentBook;
	private int currentSpineIndex = 0;
	private boolean isJapaneseMode = false;
	private InputStream epub4jis;

	private static final int PAGE_SLIDE = 0;
	private static final int PAGE_INPUT = 1;

	private static final int CLICKGUARD = 32;
	private static final int TAP_MOVE_LIMIT = 100;

	private static final int TIME_VIB_TERM = 20;
	private static final int TIME_VIB_RANGE = 30;

	private final int TOUCH_NONE = 0;
	private final int TOUCH_COMMAND = 1;
	private final int TOUCH_OPERATION = 2;

	private final int INFOCOLOR = 0xff000000;
	private final int INFOBAKCOLOR = 0xffe0e0e0;

	private static final int NOISE_NEXTPAGE = 1;
	private static final int NOISE_PREVPAGE = 2;
	private static final int NOISE_NEXTSCRL = 3;
	private static final int NOISE_PREVSCRL = 4;

	private final int VOLKEY_NONE = 0;
	private final int VOLKEY_DOWNTONEXT = 1;

	private static final int[] CTL_COUNT = { 1, 1, 2, 99999 }; // 対象のページ数
	private static final int[] CTL_RANGE = { 2, 4, 3, 1 }; // 1ページ選択に必要な移動幅(単位)

	private int currentScrolllX = 0, currentScrolllY = 0;
	private int maxScrollX = 0, maxScrollY = 0;

	private final int[] COMMAND_RES =
	{
		R.string.rotateMenu,		// 画面方向
		R.string.tguide02,			// 見開き設定
		R.string.tguide03,			// 画像サイズ
		R.string.selChapterMenu,	// 見出し選択
	};
	private String[] mCommandStr;
	private int mLongTapZoom = 800; // 長押し時間
	private int mLongTouchCount = 0;
	private boolean mDoubleTapMode = false;
	// 上下の操作領域タッチ後何msでボタンを表示するか
	private static final int LONGTAP_TIMER_UI = 400;
	private static final int LONGTAP_TIMER_BTM = 400;
	private boolean mTapScrl;
	private boolean mChgPage;
	private boolean mChgPageKey;
	private int mVolKeyMode = VOLKEY_DOWNTONEXT;
	private int mViewRota;
	private int mGetRotateBtn;
	private int mRotateBtn;
	private static boolean mRevtRota;
	private int mPageWay = DEF.PAGEWAY_RIGHT; // テキストビュワーは右開きに固定

	private OverScroller scroller;
	private GestureDetector gestureDetector;
	private SharedPreferences mSharedPreferences;
	private String mFontFile;
	private int mServer;
	private String mURI = "";
	private String mPath;
	private String mUriPath = "";
	private String mLocalFileName;
	private static String mUser = "";
	private static String mPass = "";
	private String mFileName = "";
	private String mFilePath;
	private String mTextName;
	private int mTimestamp = 0;
	private int pendingPos = 0; 
	private FrameLayout container;
	// クラス変数にキャッシュ用のMapを追加
	private java.util.Map<String, Resource> resourceCache = new java.util.HashMap<>();
	// 遷移の状態を保持
	private boolean isMovingToPreviousChapter = false;
	private PageMetadata meta;

	private GestureDetector mGestureDetector;
	private ProgressBar mLoadingSpinner;
	private WebView measurementWebView;
	private java.util.List<Integer> measurementQueue = new java.util.ArrayList<>();
	private final Handler mainHandler = new Handler(android.os.Looper.getMainLooper());
	 // 現在計測している章のインデックス
	private int measurementCurrentIdx = 0;
	private final ReentrantLock lock = new ReentrantLock();
	private int mPageOffset = 0;
	private int mScrollOffset = 0;
	private boolean mAnchorl = false;
	private boolean mAnchorr = false;
	private String mAnchorStr;
	private boolean mFindWord = false;
	private boolean mFindNextWordl = false;
	private boolean mFindNextWordr = false;
	private Resource currentResource;
	private boolean mPageMoveEnable = false;
	private boolean mScrollxEnable = false;
	private int currentViewWidth;
	private int currentChapterMaxPages;
	private int currentPages;
	private int currentOffset;
	private int scrollXStep;
	private int scrollYStep;
	private int currentIndex;
	private String searchKeyword;
	private int lastlX = -1;
	private int lastlY = -1;
	private int lastrX = -1;
	private int lastrY = -1;
	private LinearLayout rootContainer;
	private LinearLayout doublePageBox;
	private View parentLayout;
	private WebView leftWebView;
	private WebView rightWebView;
	private boolean mScalingOn = false;
	private boolean mImageOnly = false;
	private final int[] mAnchorScrolll = {0};
	private final int[] mAnchorScrollr = {0};
	private String targetItem;
	private int colorInt;
	private int bodyColor;
	private boolean mScrollMode = false;
	private boolean mDoubleMode = false;
	private boolean mFixBodyColor = false;
	private boolean mFixBackgroundColor = false;
	private boolean mUpDownMode = false;
	private TextView headerView;
	private TextView footerView;
	private int mGap = 0;
	private int mTextGap = 5;
	// 重ね合わせ用のコンテナ
	private FrameLayout contentWrapper;
	private int mClickArea = 16;
	private GuideView mGuideView = null;
	private int mTapPattern;
	private int mTapRate;
	private MyGuideSurfaceView mSurfaceView;
	private DrawThread mThread;
	private SurfaceHolder surfaceHolder;
	private PageSelectDialog mPageDlg = null;
	private Handler mHandler;
	private int selectpage = -1;
	private boolean mRenderBusy = false;
	private boolean mTapEditMode = false;
	private boolean mOnflingMode = false;
	private NoiseSwitch mNoiseSwitch = null;
	private int mNoiseScroll = 0;
	private int mNoiseScrl;
	private int mNoiseUnder;
	private int mNoiseOver;
	private int mNoiseDec;
	private boolean mNoiseLevel;
	private long mPrevVibTime = 0;
	private int mLastMsg;
	private Vibrator mVibrator;
	private boolean mVibFlag;
	private boolean mFlickPage;
	private boolean mFlickEdge;
	private String mRestoreValue;
	private String mReturnValue;
	private boolean mIsConfSave;
	private boolean mForceNotice = false;
	private boolean mAutoRepeatCheck = false;

	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Handler triggerHandler = new Handler(Looper.getMainLooper());
	private Runnable pendingRunnable;
	private ListDialog mListDialog;
	private int mSelectMode;
	private int mFileType;
	private int mDispMode;
	private int mDispModeBackup;
	private boolean mBottomFile;
	private int mPageSelect;
	private boolean mDisablePageButton;
	private int mTextColor;
	private int mBackColor;
	private int mGradColor;
	private int mGradation;
	private int mSrchColor;
	private int mMgnColor;
	private int mCenColor;
	private int mTopColor1;
	private int mTopColor2;
	private boolean mTimeDisp;
	private int mTimeFormat;
	private int mTimePos;
	private int mTimeSize;
	private int mTimeColor;
	private float mDensity;
	private Information mInformation;
	private TextManager mTextMgr;
	private TextInputDialog mTextInputDialog;
	private EpubWebViewConfigDialog mEpubWebViewConfigDialog;
	private CloseDialog mCloseDialog;
	private String mSearchText;
	private boolean mConfirmBack;
	private boolean mHistorySaved;
	private List<SearchItem> SearchList;
	private int mSearchCount = 0;
	private int mSearchIndex = 0;
	private int mNowFindTotal = 0;
	private int mNowFindCount = 0;
	private boolean mNowPrevSet = false;
	private boolean mNowPrevSetl = false;
	private boolean mNowPrevSetr = false;
	private boolean mReturnListView;
	private boolean mImmForce;
	private FrameLayout leftWrapper, rightWrapper; // メンバ変数として定義
	private float ratiox = 0;
	private float ratioy = 0;
	private int mNowPage = 0;
	private boolean mBusyRotate = false;
	private float mScaleDensity;
	private volatile boolean isMeasurementCancelled = false;
	private boolean mCompleted = false;
	private boolean mFirstRead = false;
	private boolean mScrollMovel = false;
	private boolean mScrollMover = false;
	private ArrayList<RecordItem> list_copy;
	private boolean mInternaerror = false;
	private int mImmCancelRange;
	private boolean mImmCancel;
	private Insets insets;
	private boolean mActionUp = false;
	private boolean mActionDown = false;
	private int mOperation; 	// 操作種別
	private boolean mPageMode = false; // ページ選択モード
	private boolean mPageModeIn = false; // ページ選択中の操作エリア外フラグ
	private boolean mHideNavigationBar = false;
	private boolean mClickGuard = false;
	private boolean mOldMenu;
	private int mSelectPage = 0;
	private int mPageRange = 16;
	// 開始x座標
	private float mTouchBeginX;
	// 開始y座標
	private float mTouchBeginY;
	// タッチ開始後リミットを超えて移動していない
	private boolean mTouchFirst = false; 
	private boolean mTouchMove = false;
	private int mTouchDrawLeft;
	private int mTouchDrawTop;
	private TextDrawData[][] mTextPages = null;
	private boolean mHeaderOn = true;
	private boolean mFooterOn = true;
	// ヘッダー/フッターの枠
	private int mFrameLineSize1 = 2;
	// WebViewの内側の余白
	private int mFrameLineSize5 = 2;

	private int mFontBody;
	private int mFontText;
	private int mFontInfo;
	private int mMarginW;
	private int mMarginH;
	private int mTxtColor;
	private int mBakColor;

	private int mFontBodyBackup;
	private int mFontTextBackup;
	private int mFontInfoBackup;
	private int mMarginWBackup;
	private int mMarginHBackup;

	private int mTextSize = 100;
	private int mInfoSize;
	private int mScale = 100;
	private int mScaleFix;
	private boolean mScaleValiable;
	private boolean mDisableTextInfo;
	private boolean mTextFrame;
	private boolean mTextColorFix;
	private boolean mTextBakColorFix;
	private int mDispOffset = 0;
	private boolean mNoCache;
	private boolean mHorizontalWriting;
	private boolean mEnableHorizontalWriting = false;
	private boolean mDispChange = false;
	private boolean mInitSet = false;
	private int mBackupCurrentScrolllX;
	private int mBackupCurrentScrolllY;
	private int mTextLength;
	private boolean mSearchInitSet = false;
	private boolean isAozora = false;
	private File mAozoraFile;
	private String fileUrl;
	private String mAozoraHtml;
	private int mAozoratextLength;
	private int mAozoraDirText;
	private int[] option =  {0, 0, 0, 0, 0};
	private String fontname;
	private int[] mLoadCustomkeyCode = new int[DEF.KEY_CODE_CUSTOM_MAX];
	private int[] mGetHardwareKeySetData = new int[DEF.KEYCODE_INDEX.length + DEF.KEY_CODE_CUSTOM_MAX];
	private int mRBSort;
	private String jsonEpubWebViewString;
	private String jsonDialogString;
	private String jsonTappatternString;
	private String jsonCustomkeyString;

	public class PageMetadata {
		// 判定項目
		// rendition:layout-reflowable が無ければ基本は Fixed
		public boolean isFixed;
		// 縦書き判定
		public boolean isVertical;
		// media-type が image/* かどうか
		public boolean isImageOnly;
		public boolean isScrollOff;
		// landscape, portrait, auto
		public String orientation;
		// center, left, right, none
		public String spread;
		public boolean isReflowable;
		// 補助メソッド
		public boolean shouldDisableScroll() {
			// 固定レイアウトまたは画像直接表示の場合はスクロールを殺す
			return isFixed || isImageOnly;
		}
	}

	public class WebAppInterface {
		Context mContext;
		WebAppInterface(Context c) {
			mContext = c;
		}
		@JavascriptInterface
		public void showToast(String toast) {
			Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
		}
		@JavascriptInterface
		public void onRenderReady() {
			// JS側でレンダリングが準備できた時に呼ばれる
			// 何か処理をさせたい場合はここに書く
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Android9以降のマルチプロセス対策
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			try {
				WebView.setDataDirectorySuffix("webview_process");
			}
			catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
		super.onCreate(savedInstanceState);
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// タイトル非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mActivity = this;
		mHandler = new Handler(this);
		new TouchPanelView(mActivity, 3, mHandler);
		setContentView(R.layout.epubreader);
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		mDensity = getResources().getDisplayMetrics().scaledDensity;
		mImmCancelRange = (int)(getResources().getDisplayMetrics().density * 32);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
			// ステータスバーとナビゲーションバーの高さを求めるための準備を行う
			WindowMetrics windowMetrics = this.getWindowManager().getCurrentWindowMetrics();
			insets = windowMetrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
		}
		mIsConfSave = true;
		mNoiseSwitch = new NoiseSwitch(mHandler);
		// JSONデータを取り出す
		jsonEpubWebViewString = getIntent().getStringExtra("EpubWebview_Data");
		jsonDialogString = getIntent().getStringExtra("Dialog_Data");
		jsonTappatternString = getIntent().getStringExtra("Tappattern_Data");
		jsonCustomkeyString = getIntent().getStringExtra("Customkey_Data");
		// 設定の読み込み
		ReadSetting();
		// 別プロセスなので新規で書き込む(呼び出し元で書き戻す必要あり)
		FileSelectActivity.ReadDialogSetting(mSharedPreferences, jsonDialogString);
		FileSelectActivity.ReadTappatternSetting(mSharedPreferences, jsonTappatternString);
		FileSelectActivity.ReadCustomKeySetting(mSharedPreferences, jsonCustomkeyString);
		// JSONデータを破棄する
		getIntent().removeExtra("EpubWebview_Data");
		// 強制再描画
		parentLayout = findViewById(R.id.container);
		parentLayout.setBackgroundColor(colorInt);
		parentLayout.invalidate();
		if (mNotice || mForceNotice) {
			// 通知領域非表示
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		if (mImmEnable && mSdkVersion >= 19) {
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
		ImageActivity.SetOrientationEventListener(mActivity, mViewRota, mSharedPreferences);

		getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
		// Intentに保存されたデータを取り出す
		Intent intent = getIntent();
		mServer = intent.getIntExtra("Server", -1);		// サーバー選択番号
		mURI = intent.getStringExtra("Uri");					// ベースディレクトリのuri
		mPath = intent.getStringExtra("Path");					// ベースURIからの相対パス名
		mUser = intent.getStringExtra("User");				// SMB認証用
		mPass = intent.getStringExtra("Pass");				// SMB認証用
		mFileName = intent.getStringExtra("File");			// ZIP指定時
		mTextName = intent.getStringExtra("Text");				// テキストファイル名
		mUriPath = DEF.relativePath(mActivity, mURI, mPath);
		if (mFileName != null) {
			mFilePath = DEF.relativePath(mActivity, mUriPath, mFileName);
		}
		else {
			mFilePath = mUriPath;
		}
		if (mFilePath.toLowerCase().endsWith(".zip") || mFilePath.toLowerCase().endsWith(".txt")) {
			isAozora = true;
		}
		mLocalFileName = DEF.relativePath(mActivity, mPath, mFileName);
		mTimestamp = (int) FileAccess.date(mActivity, mFilePath, mUser, mPass);
		// 続きから開く設定を記録
		saveLastFile();
		// 読書の情報を読みこむ
		String id = (isAozora) ? "#aozora" : "#newepub";
		Logcat.v(logLevel, "mFilePath=" + mFilePath + ", mURI=" + mURI + mPath + ", mFileName=" + mFileName + ", id=" + id);
		String mRestoreValue = intent.getStringExtra("Value");
		mFirstRead = false;
		mAozoraDirText = 0;
		if (mRestoreValue != null) {
			String[] parts = mRestoreValue.split(",");
			if (parts.length >= 6) {
				if (parts.length >= 7) {
					for (int i = 0 ; i < (parts.length - 6) ; i++) {
						option[i] = Integer.parseInt(parts[6 + i]);
					}
				}
				Logcat.v(logLevel, "parts=" + parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3] + "," + parts[4] + "," + parts[5] + "," + option[0] + "," + option[1] + "," + option[2] + "," + option[3] + "," + option[4]);
				// 文字列が破損していれば例外が出るのでtry～catchで囲む
				try {
					mFirstRead = (Integer.parseInt(parts[0]) == -1 && Integer.parseInt(parts[1]) == -1) ? true : false;
					mScrollOffset = 0;
					mPageOffset = Integer.parseInt(parts[3]);
					if (isAozora) mAozoraDirText = Integer.parseInt(parts[3]);
					ratiox = Float.parseFloat(parts[4]);
					ratioy = Float.parseFloat(parts[5]);
					mCompleted = (ratiox == 1.0 && ratioy == 1.0) ? true : false;
					Logcat.v(logLevel, "mFirstRead=" + mFirstRead + ", mPageOffset=" + mPageOffset + ", ratiox=" + ratiox + ", ratioy=" + ratioy + ", mCompleted=" + mCompleted + ", mAozoraDirText=" + mAozoraDirText);
				}
				catch (Exception e) {
				}
			}
		}
		Resources res = getResources();
		mCommandStr = new String[COMMAND_RES.length];
		for (int i = 0 ; i < mCommandStr.length ; i ++) {
			mCommandStr[i] = res.getString(COMMAND_RES[i]);
		}
		SetFrameLayout(true);
		// 慣性スクロールを定義
		scroller = new OverScroller(this);
		initWrappers();
		if (mDispMode == 1) {
			mDoubleMode = true;
		}
		else {
			mDoubleMode = false;
		}
		View contentView = findViewById(android.R.id.content);
		ViewGroup rootView = (ViewGroup)contentView.getRootView();
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
			// ナビゲーションバーの表示更新を検出するためリスナーをセット
			rootView.setOnApplyWindowInsetsListener((view, insets) -> {
				// ナビゲーションバーの情報を取得
				boolean isVisible = insets.isVisible(WindowInsets.Type.navigationBars());
				if (isVisible) {
					// ナビゲーションバーが表示されている場合の処理
					mHideNavigationBar = false;
				}
				else {
					// ナビゲーションバーが非表示の場合の処理
					mHideNavigationBar = true;
				}
				return insets;
			});
		}
		// Android16の勝手に終了を防ぐための"おまじない"
		if (Build.VERSION.SDK_INT >= 36) {
			// Android 16以降
			getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
				@Override
				public void handleOnBackPressed() {
					// 既存の operationBack() を呼び出す
					operationBack();
				}
			});
		}
		// 全画面(Edge-to-Edge)を可能な限り抑制する
		androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
		View root = findViewById(android.R.id.content);
		androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
			// 直接数値を取り出す
			int l = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()).left;
			int t = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()).top;
			int r = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()).right;
			int b = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()).bottom;
			// この箱自体に余白を持たせることで中のView(onTouchEventを持つViewを押し戻す
			v.setPadding(l, t, r, b);
			return androidx.core.view.WindowInsetsCompat.CONSUMED;
		});
		// ジェスチャー検出を有効にする
		mDetector = new GestureDetectorCompat(this,this);
		// ダブルタップ検出のリスナーを有効にする
		mDetector.setOnDoubleTapListener(this);
		// ユーザーフォントのファイルパスを取得
		mFontFile = (fontname.length() > 0) ? DEF.getFontDirectory() + fontname : null;
		// ユーザーフォントを読み込む
		prepareUserFont();
		mFileType = FileData.FILETYPE_EPUB;
		// EPUBファイルを読み込む
		if (mFilePath != null) loadEpub(mFilePath);
	}
	// 設定を呼び出す前に現在のページに関する内容を保存する
	private void backupSetting() {
		mDispModeBackup = mDispMode;
		if (mScaleValiable) {
			mFontTextBackup = mFontText;
		}
		mFontBodyBackup = mFontBody;
		mFontInfoBackup = mFontInfo;
		mMarginWBackup = mMarginW;
		mMarginHBackup = mMarginH;
	}
	// 設定を呼び出した後に直前に設定したページに関する内容を復元する
	private void restoreSetting() {
		if (mScaleValiable) {
			mFontText = mFontTextBackup;
		}
		mDispMode = mDispModeBackup;
		mFontBody = mFontBodyBackup;
		mFontInfo = mFontInfoBackup;
		mMarginW = mMarginWBackup;
		mMarginH = mMarginHBackup;
	}

	// 他アクティビティからの復帰通知
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DEF.REQUEST_SETTING) {
			// ダブルタップ検出のリスナーを有効にする(これを入れないとタップ操作ができなくなる)
			mDetector.setOnDoubleTapListener(this);
			// JSONデータを取り出す
			jsonEpubWebViewString = data.getStringExtra("EpubWebview_Data");
			// 設定の読込
			ReadSetting();
			// JSONデータを破棄する
			data.removeExtra("EpubWebview_Data");
			// 直前に設定したページに関する内容を復元する
			restoreSetting();
			// 本文のフォントの拡大率を変更
			mTextSize = mFontBody * 2;
			WebSettings settings = leftWebView.getSettings();
			settings.setTextZoom(mTextSize);
			leftWebView.setInitialScale(mScaleFix);
			leftWebView.setBackgroundColor(colorInt);
			settings = rightWebView.getSettings();
			settings.setTextZoom(mTextSize);
			rightWebView.setInitialScale(mScaleFix);
			rightWebView.setBackgroundColor(colorInt);
			if (mLoadingSpinner != null) {
				mLoadingSpinner.setVisibility(View.VISIBLE);
				mLoadingSpinner.bringToFront();
				// レイアウトを更新
				rootContainer.requestLayout(); 
			}
			// 表示を再初期化
			runOnUiThread(() -> {
				parentLayout.setBackgroundColor(colorInt);
				// 強制再描画
				parentLayout.invalidate();
			});
			initWrappers();
			SetFrameLayout(false);
			setAsyncScrollSet();
			// 先にレイアウトを更新
			updateLayout();
			renderSpine(currentSpineIndex, leftWebView);
		}
	}

	private void SetFrameLayout(boolean mode) {
		// 縦書きと横書きのWebViewを作成
		// 一番外側の土台を FrameLayout に変更(重なりを許容するため)
		FrameLayout screenRoot = new FrameLayout(this);
		screenRoot.setLayoutParams(new ViewGroup.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, 
			ViewGroup.LayoutParams.MATCH_PARENT));
		screenRoot.setBackgroundColor(INFOBAKCOLOR);
		// 一番外側の土台を作る
		rootContainer = new LinearLayout(this);
		rootContainer.setOrientation(LinearLayout.VERTICAL);
		rootContainer.setLayoutParams(new ViewGroup.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, 
			ViewGroup.LayoutParams.MATCH_PARENT));
			// 背景色同期
		rootContainer.setBackgroundColor(0xffe0e0e0);
		// WebViewと合わせる
		int borderWidth = mFrameLineSize1 + 0; 
		int borderColor = Color.GRAY;
		// 元々の背景色
		int headerFooterBg = INFOBAKCOLOR;
		if (mHeaderOn) {
			// ヘッダー(ファイル名)の作成
			headerView = new TextView(this);
			headerView.setLayoutParams(new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT));
			String fileNameWithExt = new File(mFileName).getName();
			int lastDotIndex = fileNameWithExt.lastIndexOf('.');
			headerView.setText(fileNameWithExt.substring(0, lastDotIndex));
			headerView.setGravity(Gravity.CENTER);
			headerView.setTextColor(INFOCOLOR);
			headerView.setBackgroundColor(INFOBAKCOLOR);
			headerView.setPadding(mInfoSize, mTextGap, mInfoSize, mTextGap);
			headerView.setTextSize(mInfoSize);
			// 1行に強制する
			headerView.setSingleLine(true);
			// 末尾を「…」にする
			headerView.setEllipsize(TextUtils.TruncateAt.END);
			// 横スクロールを無効化(念のため)
			headerView.setHorizontallyScrolling(false); 
			// ヘッダーに適用
			headerView.setBackground(createBorderDrawable(headerFooterBg, borderColor, borderWidth));
		}
		if (mFooterOn) {
			// フッター(ページ数)の作成
			footerView = new TextView(this);
			footerView.setLayoutParams(new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT));
			String pagetext = "- / -";
			footerView.setText(pagetext);
			footerView.setGravity(Gravity.CENTER);
			footerView.setTextColor(INFOCOLOR);
			footerView.setBackgroundColor(INFOBAKCOLOR);
			footerView.setPadding(mInfoSize, mTextGap, mInfoSize, mTextGap);
			footerView.setTextSize(mInfoSize);
			// フッターに適用
			footerView.setBackground(createBorderDrawable(headerFooterBg, borderColor, borderWidth));
		}
		// WebViewとプログレスバーを重ねるためのFrameLayout
		contentWrapper = new FrameLayout(this);
		LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
		contentWrapper.setLayoutParams(wrapperParams);
		// 枠線専用のコンテナ(外枠)を作成
		FrameLayout borderContainer = new FrameLayout(this);
		FrameLayout.LayoutParams borderParams = new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		// 任意の余白
		int margin = 0; 
		borderParams.setMargins(margin, margin, margin, margin);
		borderContainer.setLayoutParams(borderParams);
		// 枠線の描画設定
		GradientDrawable borderline = new GradientDrawable();
		// 中は透明
		borderline.setColor(Color.TRANSPARENT);
		borderline.setStroke(0, colorInt);
		borderContainer.setBackground(borderline);
		// doublePageBox を borderContainer の中に入れる
		doublePageBox = new LinearLayout(this);
		FrameLayout.LayoutParams boxParams = new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		// 枠線の太さ分だけ内側にパディングを入れる
		int strokeWidth = 1;
		doublePageBox.setPadding(strokeWidth, strokeWidth, strokeWidth, strokeWidth);
		doublePageBox.setLayoutParams(boxParams);
		// 階層構造の組み立て
		borderContainer.addView(doublePageBox);
		contentWrapper.addView(borderContainer);
		// プログレスバーを作成して FrameLayout に入れる(WebViewより後に追加することで上に重なる)
		int sizeInDp = 80;
		float density = getResources().getDisplayMetrics().density;
		int sizePx = (int) (sizeInDp * density);
		mLoadingSpinner = findViewById(R.id.loading_spinner);
		// 高さを指定
		FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(sizePx, sizePx);
		// 画面上部に配置
		progressParams.gravity = Gravity.CENTER;
		mLoadingSpinner.setLayoutParams(progressParams);
		// 砂時計を表示(最初は隠しておく)
		mLoadingSpinner.setVisibility(View.GONE);
		// 親から切り離す処理を追加
		if (mLoadingSpinner.getParent() != null) {
			((ViewGroup) mLoadingSpinner.getParent()).removeView(mLoadingSpinner);
		}
		contentWrapper.addView(mLoadingSpinner);
		mGuideView = new GuideView(this);
		mGuideView.setGuideMode(mDispMode != DEF.DISPMODE_EPUB_NORMAL, mBottomFile, true, mPageSelect, false, mDisablePageButton);
		mGuideView.setColor(mTopColor1, mTopColor2, mMgnColor);
		mGuideView.setGuideSize(mClickArea, mTapPattern, mTapRate, false, mOldMenu);
		mGuideView.eventTouchTimer();
		mGuideView.setTimeFormat(mTimeDisp, mTimeFormat, mTimePos, mTimeSize, mTimeColor);
		mGuideView.setPageText(null);
		mGuideView.setPageColor(mTopColor1);
		// 上部メニューの文字列情報をガイドに設定
		mGuideView.setTopCommandStr(mCommandStr);
		// 自作したSurfaceViewの準備(ここにmGuideViewを渡す)
		mSurfaceView = new MyGuideSurfaceView(this, mGuideView);
		mGuideView.setParentView(mSurfaceView);
		if (mHeaderOn) {
			rootContainer.addView(headerView);
		}
		// フレーム全体をrootに追加
		rootContainer.addView(contentWrapper);
		if (mFooterOn) {
			rootContainer.addView(footerView);
		}
		// 土台に「縦並びUI」を追加
		screenRoot.addView(rootContainer);
		if (mSurfaceView != null) {
			// 画面全体を覆うサイズ設定
			FrameLayout.LayoutParams guideParams = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.MATCH_PARENT);
			// rootContainerより後に追加することでヘッダー・フッター・WebView全ての上に重なる
			screenRoot.addView(mSurfaceView, guideParams);
		}
		// 枠線用のDrawableを生成
		GradientDrawable border = new GradientDrawable();
		border.setColor(colorInt); // 中は透明
		border.setStroke(0, colorInt);
		// 親ボックスに背景としてセット
		doublePageBox.setBackground(border);
		// 枠と中身がくっつかないように余白(Padding)を設定
		int paddingPx = mMarginW * 2; 
		int paddingPy = mMarginH * 2; 
		doublePageBox.setPadding(paddingPx, paddingPy, paddingPx, paddingPy);
		// 仕切り線用のDrawableを生成
		GradientDrawable divider = new GradientDrawable();
		divider.setColor(colorInt);
		divider.setSize(0, 0);
		// LinearLayoutの設定
		doublePageBox.setDividerDrawable(divider);
		doublePageBox.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
		// 左右それぞれのWebViewを50%ずつにする設定
		LinearLayout.LayoutParams halfParams = new LinearLayout.LayoutParams(
		 0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
		if (mode) {
			leftWebView = createWebView();
			rightWebView = createWebView();
		}
		// 最後に screenRoot をセット
		setContentView(screenRoot);
	}
	// 枠線を描画するヘルパーメソッド(角の丸みや色を一括管理)
	private GradientDrawable createBorderDrawable(int backgroundColor, int strokeColor, int strokeWidth) {
		GradientDrawable gd = new GradientDrawable();
		// 背景色
		gd.setColor(backgroundColor);
		// 枠線の太さと色
		gd.setStroke(strokeWidth, strokeColor);
		return gd;
	}

	private void initWrappers() {
		int borderWidth = (mTextFrame) ? mFrameLineSize5 : 0;
		int borderColor = Color.GRAY;
		GradientDrawable border = new GradientDrawable();
		border.setColor(Color.TRANSPARENT);
		border.setStroke(borderWidth, borderColor);
		// WebViewを包むパラメータ(親はFrameLayout)
		FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		leftWrapper = new FrameLayout(this);
		leftWrapper.setBackground(border);
		leftWrapper.setPadding(borderWidth, borderWidth, borderWidth, borderWidth);
		// leftWebViewを親から切り離す
		if (leftWebView != null && leftWebView.getParent() != null) {
			((ViewGroup) leftWebView.getParent()).removeView(leftWebView);
		}
		leftWrapper.addView(leftWebView, webParams);
		rightWrapper = new FrameLayout(this);
		rightWrapper.setBackground(border);
		rightWrapper.setPadding(borderWidth, borderWidth, borderWidth, borderWidth);
		if (rightWebView != null) {
			// rightWebViewを親から切り離す
			if (rightWebView.getParent() != null) {
				((ViewGroup) rightWebView.getParent()).removeView(rightWebView);
			}
			rightWrapper.addView(rightWebView, webParams);
		}
	}
	// メインのレイアウト更新処理
	private void updateLayout() {
		mBusyRotate = true;
		if (mLoadingSpinner != null) {
			mLoadingSpinner.setVisibility(View.VISIBLE);
			mLoadingSpinner.bringToFront();
		}
		int orientation = getResources().getConfiguration().orientation;
		// Dividerの設定
		GradientDrawable divider = new GradientDrawable();
		divider.setColor(colorInt);
		// レイアウトパラメータの準備 (doublePageBox = LinearLayout用)
		LinearLayout.LayoutParams params;
		doublePageBox.removeAllViews();
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// 横向き：左右並び
			mUpDownMode = false;
			doublePageBox.setOrientation(LinearLayout.HORIZONTAL);
			divider.setSize(mMarginW * 2, 0);
			// 幅0, 高さMATCH, weight 1
			params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
		}
		else {
			// 縦向き：上下並び
			mUpDownMode = true;
			doublePageBox.setOrientation(LinearLayout.VERTICAL);
			divider.setSize(0, mMarginH * 2);
			// 幅MATCH, 高さ0, weight 1
			params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
		}
		// Wrapperにパラメータをセット(WebViewではなくWrapperを操作する)
		leftWrapper.setLayoutParams(params);
		rightWrapper.setLayoutParams(params);
		// 順序に従って Wrapper を追加
		boolean isReverse = isJapaneseMode && !mPageMoveEnable;
		if ((mDispMode == DEF.DISPMODE_EPUB_DUAL_HORIZON || mDispMode == DEF.DISPMODE_EPUB_DUAL_BOTH) && !mUpDownMode || (mDispMode == DEF.DISPMODE_EPUB_DUAL_VARTICAL || mDispMode == DEF.DISPMODE_EPUB_DUAL_BOTH) && mUpDownMode) {
			mDoubleMode = true;
		}
		else {
			mDoubleMode = false;
		}
		if (isReverse) {
			if (mDoubleMode) doublePageBox.addView(rightWrapper);
			doublePageBox.addView(leftWrapper);
		}
		else {
			doublePageBox.addView(leftWrapper);
			if (mDoubleMode) doublePageBox.addView(rightWrapper);
		}
		// 後処理
		GetLeftWidth(leftWebView, isReverse, true);
		// フッターを更新
		PutFooterView();
		doublePageBox.setDividerDrawable(divider);
		doublePageBox.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
		doublePageBox.requestLayout();
	}

	/**
	 * @Override アクティビティ一時停止時に呼び出される
	 */
	protected void onPause() {
		super.onPause();
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "onPause()");
		if (mNoiseSwitch != null) {
			mNoiseSwitch.recordPause(true);
		}
		ImageActivity.SetOrientationEventListenerDisable(mSharedPreferences);
	}

	/**
	 * @Override アクティビティ再開時に呼び出される
	 */
	public void onResume() {
		super.onResume();
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "onResume()");
		if (mNoiseSwitch != null) {
			mNoiseSwitch.recordPause(false);
		}
		updateLayout();
		ImageActivity.SetOrientationEventListenerEnable(mSharedPreferences);
	}

	/**
	 * @Override アクティビティ停止時に呼び出される
	 */
	protected void onStop() {
		super.onStop();
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "onStop()");
		// 途中経過を保存
		SaveTotalPage();
		// 履歴保存
		if (mHistorySaved == false) {
			saveHistory(true);
		}
		// マイク停止
		if (mNoiseSwitch != null) {
			mNoiseSwitch.recordPause(true);
		}
	}

	/**
	 * @Override アクティビティ再開時に呼び出される
	 */
	public void onRestart(){
		super.onRestart();
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "onRestart()");
	}

	// 終了処理
	protected void onDestroy() {
		super.onDestroy();
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "onDestroy()");
		// WebViewのプロセスを止める
		if (leftWebView != null) {
			ViewParent parent = leftWebView.getParent();
			if (parent instanceof ViewGroup) {
				((ViewGroup) parent).removeView(leftWebView);
			}
			leftWebView.removeAllViews();
			leftWebView.destroy();
			leftWebView = null;
		}
		if (rightWebView != null) {
			ViewParent parent = rightWebView.getParent();
			if (parent instanceof ViewGroup) {
				((ViewGroup) parent).removeView(rightWebView);
			}
			rightWebView.removeAllViews();
			rightWebView.destroy();
			rightWebView = null;
		}
		if (measurementWebView != null) {
			ViewParent parent = measurementWebView.getParent();
			if (parent instanceof ViewGroup) {
				((ViewGroup) parent).removeView(measurementWebView);
			}
			measurementWebView.stopLoading();
			measurementWebView.clearHistory();
			measurementWebView.removeAllViews();
			measurementWebView.destroy();
			measurementWebView = null;
		}
		// サーフェスビューを止める
		try {
			mSurfaceView.setVisibility(View.GONE);
		}
		catch (Exception e) {
		}
		// EPUBファイルをクローズする
		try {
			if (epub4jis != null) {
				epub4jis.close();
			}
		}
		catch (Exception e) {
		}
		if (currentBook != null) {
			currentBook = null;
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
		}
	}

	@Override
	protected void onUserLeaveHint(){
		super.onUserLeaveHint();
		if (mReturnListView) {
			// 画面が裏に入った場合にリスト一覧へ戻す(Android13の一部の機種でフリーズしてしまうための対策)
			finish();
		}
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
				found_code = i;
				break;
			}
		}
		if (found_code == -1) {
			data = 0;
			// ハードウェアキーが見つからない場合はカスタムキーの中から探す
			for (int i = 0; i < DEF.KEY_CODE_CUSTOM_MAX; i++) {
				if (mLoadCustomkeyCode[i] == code) {
					// 見つかった場合はハードウェアキーの設定を取り出す
					// カスタムキーは末尾から追加されているの長さ分を加算する
					data = mGetHardwareKeySetData[DEF.KEYCODE_INDEX.length + i];
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
			data = mGetHardwareKeySetData[found_code];
		}

		found_code = -1;
		// 戻るキー設定があるかどうかを確認する
		for (int i = 0; i < (DEF.KEYCODE_INDEX.length + DEF.KEY_CODE_CUSTOM_MAX) ; i++) {
			if (mGetHardwareKeySetData[i] == DEF.TAP_BACK) {
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
			if (data == 0) {
				// システム設定の場合
				switch (code) {
					case KeyEvent.KEYCODE_DPAD_RIGHT:
					{
						if (mChgPageKey) {
							// 前ページへ
							prevPage(false);
						}
						else {
							// 次ページへ
							nextPage(false);
						}
						break;
					}
					case KeyEvent.KEYCODE_DPAD_LEFT:
					{
						if (mChgPageKey) {
							// 次ページへ
							nextPage(false);
						}
						else {
							// 前ページへ
							prevPage(false);
						}
						break;
					}
					case KeyEvent.KEYCODE_MENU:
						// 独自メニュー表示
						openMenu();
						return true;
					case KeyEvent.KEYCODE_DEL:
					case KeyEvent.KEYCODE_BACK:
						if (Build.VERSION.SDK_INT >= 36 && code == KeyEvent.KEYCODE_BACK) {
							// Android16以降の戻るキーの場合は予測型戻るジェスチャーに委ねる(ランチャーからの戻るキーの呼び出しに対応)
							return true;
						}
						operationBack();
						return true;
					case KeyEvent.KEYCODE_VOLUME_DOWN:
					case KeyEvent.KEYCODE_VOLUME_UP:
					{
						// ボリュームモード
						if (mVolKeyMode == VOLKEY_NONE) {
							// Volキーを使用しない
							break;
						}

						int move = mVolKeyMode == VOLKEY_DOWNTONEXT ? 1 : -1;
						if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
							move *= -1;
						}
						// 読込中の表示
						if (move == 1) {
							nextPage(true);
						}
						else {
							prevPage(true);
						}
						return true;
					}
					case KeyEvent.KEYCODE_SPACE:
					{
						int meta = event.getMetaState();
						int move = (meta & KeyEvent.META_SHIFT_ON) == 0 ? 1 : -1;
						// 読込中の表示
						if (move == 1) {
							nextPage(true);
						}
						else {
							prevPage(true);
						}
						return true;
					}
					case KeyEvent.KEYCODE_CAMERA:
					case KeyEvent.KEYCODE_FOCUS:
						if (mRotateBtn == 0) {
							break;
						}
						else if (event.getKeyCode() != mRotateBtn) {
							return true;
						}
						ImageActivity.SetRotate(mViewRota, mRevtRota);
						break;
					default:
						break;
				}
			}
			else {
				// 通常設定の場合
				if (Build.VERSION.SDK_INT >= 36 && code == KeyEvent.KEYCODE_BACK) {
					// Android16以降の戻るキーの場合は予測型戻るジェスチャーに委ねる(ランチャーからの戻るキーの呼び出しに対応)
					return true;
				}
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
							if (mVolKeyMode == VOLKEY_NONE) {
								// Volキーを使用しない
								break;
							}
							return true;
						default:
							break;
					}
				default:
					// 通常設定の場合
					mAutoRepeatCheck = false;
					return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	private void SetHardwareKey(int data) {
		SetTouchPanelCommandMain(data);
	}

	// 長押しタイマー開始
	public boolean startLongTouchTimer(int longtouch_event) {
		int longtaptime;
		if (longtouch_event == DEF.HMSG_EVENT_LONG_TAP) {
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
		return (true);
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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// 画面が回転した瞬間にレイアウトを更新
		// 表示開始
		// レンダリング処理の後にレイアウトを更新
		renderSpine(currentSpineIndex, leftWebView);
		setAsyncScrollSet();
		updateLayout();
	}

	@Override
	public boolean onDown(MotionEvent event) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		int x = (int)event.getRawX();
		int y = (int)event.getRawY();
		int cx = rootContainer.getWidth();
		int cy = rootContainer.getHeight();
		int buttonsize = mGuideView.eventTouchDown(x, y, cx, cy, false);
		Logcat.v(logLevel, "onDown()=" + buttonsize);
		if (y < cy - buttonsize && y > buttonsize) {
			ActionDownEvent(event);
		}
		else if (!mActionDown) {
			Logcat.v(logLevel, "ボタンエリア");
			ActionDownEvent(event);
			mActionUp = false;
		}
		scroller.forceFinished(true);
		return true;
	}

	@Override
	public boolean onFling(MotionEvent event1, MotionEvent event2,
		float velocityX, float velocityY) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (mRenderBusy || mFindWord) return false;
		Logcat.v(logLevel, "onFling x=" + velocityX + ", y=" + velocityY);
		int vx = (int)-velocityX;
		int vy = (int)-velocityY;
		int cx = rootContainer.getWidth();
		int cy = rootContainer.getHeight();
		mOnflingMode = true;
		if (isJapaneseMode && !mPageMoveEnable) {
		// フリックで移動
		Logcat.v(logLevel, "currentScrolllX=" + currentScrolllX + ", maxScrollX=" + maxScrollX);
			if (currentScrolllX <= 0 && velocityX > 2000 && mFlickPage && (!mFlickEdge || mFlickEdge && mTouchDrawLeft < mImmCancelRange)) {
				moveToNextSpine(1);
				return false;
			}
			if (currentScrolllX >= maxScrollX && velocityX < -2000 && mFlickPage && (!mFlickEdge || mFlickEdge && mTouchDrawLeft > cx - mImmCancelRange)) {
				moveToNextSpine(-1);
				return false;
			}
		}
		else {
			Logcat.v(logLevel, "currentScrolllY=" + currentScrolllY + ", maxScrollY=" + maxScrollY);
			if (currentScrolllY >= maxScrollY && velocityY < -2000 && mFlickPage) {
				moveToNextSpine(1);
				return false;
			}
			if (currentScrolllY <= 0 && velocityY > 2000 && mFlickPage) {
				moveToNextSpine(-1);
				return false;
			}
		}
		// フリックでスクロール移動
		scroller.fling(currentScrolllX, currentScrolllY, vx, vy, 0, maxScrollX, 0, maxScrollY);
		rootContainer.postOnAnimation(flingRunnable);
		return false;
	}

	@Override
	public void onLongPress(MotionEvent event) {
	}

	@Override
	public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
		if (mRenderBusy || mFindWord) return false;
		// 縦書き・横書きに関わらず計算された maxScrollX/Y の範囲内で自由に動かす
		currentScrolllX = Math.max(0, Math.min(currentScrolllX + (int)distanceX, maxScrollX));
		currentScrolllY = Math.max(0, Math.min(currentScrolllY + (int)distanceY, maxScrollY));
		syncWebViewScroll();
		return true;
	}

	@Override
	public void onShowPress(MotionEvent event) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "onSingleTapUp");
		int x = (int)event.getRawX();
		int y = (int)event.getRawY();
		int cx = rootContainer.getWidth();
		int cy = rootContainer.getHeight();
		int buttonsize = mGuideView.eventTouchDown(x, y, cx, cy, false);
		Logcat.v(logLevel, "mActionUp=" + mActionUp + ", y=" + y + ", cy=" + cy);
		if (y < cy - buttonsize && y > buttonsize) {
			ActionUpEvent(event);
		}
		else if (!mActionUp) {
			Logcat.v(logLevel, "ボタンエリア");
			mActionDown = false;
			ActionUpEvent(event);
		}
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

	// 戻る操作
	private void operationBack() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "operationBack()");
		if (mTapEditMode) {
			// タップ操作の設定の編集中だった場合は解除して戻る
			mTapEditMode = false;
			return;
		}
		if (mGuideView.getOperationMode()) {
			mGuideView.setOperationMode(false);
			Logcat.v(logLevel, "mGuideView.setOperationMode(false)");
			return;
		}
		else {
			if (mConfirmBack) {
				// 終了
				showCloseDialog(CloseDialog.LAYOUT_BACK);
			}
			else {
				Logcat.v(logLevel, "finishActivity(true)");
				finishActivity(true);
			}
		}
		return;
	}

	private void finishActivity(boolean resume) {
		finishActivity(CloseDialog.CLICK_CLOSE, resume, true);
	}

	private void saveLastFile() {
	}

	private void removeLastFile() {
	}

	private void saveCurrentPage() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		// 現在ページ情報を保存
		if (isAozora) {
		}
		else {
			if (currentBook == null) return;
		}
		int nowpage = GetNowPage();
		int maxpage = getTotalPagesNow();
		if (isAozora) {
			if (isJapaneseMode) {
				if (ratiox == 0 && ratioy == 0) {
					nowpage = maxpage;
				}
			}
			else {
				if (ratiox == 0 && ratioy == 1) {
					nowpage = maxpage;
				}
			}
		}
		else {
			if (isJapaneseMode) {
				if (ratiox == 0 && ratioy == 0 && currentSpineIndex >= currentBook.getSpine().size() - 1) {
					nowpage = maxpage;
				}
			}
			else {
				if (ratiox == 0 && ratioy == 1 && currentSpineIndex >= currentBook.getSpine().size() - 1) {
					nowpage = maxpage;
				}
			}
		}
		String value;
		if (isAozora) {
			value = nowpage + "," + maxpage + "," + mTimestamp + "," + mAozoraDirText + "," + ratiox + "," + ratioy + "," + option[0] + "," + option[1] + "," + option[2] + "," + option[3] + "," + option[4];
		}
		else {
			value = nowpage + "," + maxpage + "," + mTimestamp + "," + currentSpineIndex + "," + ratiox + "," + ratioy + "," + option[0] + "," + option[1] + "," + option[2] + "," + option[3] + "," + option[4];
		}
		mReturnValue = value;
		Logcat.v(logLevel, "value=" + value);
	}

	// 起動時のページ情報に戻す
	private void restoreCurrentPage() {

		mReturnValue = mRestoreValue;
	}

	// 履歴を保存
	private void saveHistory(boolean isSavePage) {

		int type = (mFileName == null || mFileName.isEmpty()) ? RecordItem.TYPE_TEXT : RecordItem.TYPE_COMPTEXT;
		RecordList.add(RecordList.TYPE_HISTORY, type, mServer, mLocalFileName
			, mTextName, new Date().getTime(), null, 0, (float)GetNowPage() / getTotalPagesNow(), GetNowPage(), null);
		// タスク切り替え時しおりを保存
		if (isSavePage) {
			saveCurrentPage();
		}
	}

	private void finishActivity(int select, boolean resume, boolean mark) {
		if (mInternaerror) {
			// 内部エラーの場合は終了させる
			finish();
			return;
		}
		// 続きから読み込みの設定
		if (!resume) {
			removeLastFile();
		}
		if (mark) {
			// しおりを保存する
			saveCurrentPage();
		}
		else {
			// しおりを起動時の状態に戻す
			restoreCurrentPage();
		}
		// 履歴保存
		saveHistory(false);
		mHistorySaved = true;

		cancelMeasurement();
		SaveTotalPage();

		Intent intent = new Intent();
		intent.putExtra("NextOpen", select);
		intent.putExtra("LastFile", mFileName);
		intent.putExtra("LastPath", mPath);
		intent.putExtra("ReturnValue", mReturnValue);
		intent.putExtra("ReturnMode", isAozora);
		intent.putExtra("FilePath", mFilePath);
		intent.putExtra("User", mUser);
		intent.putExtra("Pass", mPass);
		intent.putExtra("FontBody", mFontBody);
		intent.putExtra("FontText", mFontText);
		intent.putExtra("FontInfo", mFontInfo);
		intent.putExtra("MarginW", mMarginW);
		intent.putExtra("MarginH", mMarginH);
		intent.putExtra("IsConfSave", mIsConfSave);
		intent.putExtra("LastServer", mServer);
		intent.putExtra("LastUser", mUser);
		intent.putExtra("LastPass", mPass);
		intent.putExtra("LastFile", mFileName);
		intent.putExtra("LastText", mTextName);
		intent.putExtra("LastOpen", DEF.LASTOPEN_TEXT);
		if (!resume) {
			intent.putExtra("LastOpen", DEF.LASTOPEN_NONE);
		}
		// ツールバーの内容を書き戻す
		FileSelectActivity.setDialogSharedData(mSharedPreferences);
		intent.putExtra("Dialog_Data", FileSelectActivity.getJsonDialogData());
		// タップ操作のパターンの内容を書き戻す
		FileSelectActivity.setTappatternSharedData(mSharedPreferences);
		intent.putExtra("Tappattern_Data", FileSelectActivity.getJsonTappatternData());
		// カスタムキーの内容を書き戻す
		FileSelectActivity.setCustomKeySharedData(mSharedPreferences);
		intent.putExtra("Customkey_Data", FileSelectActivity.getJsonCustomkeyData());
		setResult(RESULT_OK, intent);
		finish();
	}

	private void showCloseDialog(int layout) {
		if (mCloseDialog != null) {
			return;
		}
		mCloseDialog = new CloseDialog(this, R.style.MyDialog);
		mCloseDialog.setTitleText(layout);
		mCloseDialog.setCloseListear(new CloseDialog.CloseListenerInterface() {
			@Override
			public void onCloseSelect(int select, boolean resume, boolean mark) {
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

	// メニューを開く
	private void openChapterMenu() {
		Resources res = getResources();
		TabDialogFragment mMenuDialog = new TabDialogFragment(this, R.style.MyDialog, true, false, false, true, this);

		if (!isAozora && mFileType == FileData.FILETYPE_EPUB) {
			// 見出しの追加
			mMenuDialog.addSection(res.getString(R.string.ChapMenuTOC));

			List<TocItem> tocList = getTocItems(currentBook);
			if (tocList.isEmpty()) {
				Toast.makeText(this, "目次が見つかりません", Toast.LENGTH_SHORT).show();
			}
			else {
				for (int i = 0; i < tocList.size(); i++) {
					TocItem item = tocList.get(i);
					int itemId = DEF.MENU_CHAPTER + i; 
					mMenuDialog.addItem(itemId, item.title, ""); 
				}
			}
		}

		// 見出しの追加
		mMenuDialog.addSection(res.getString(R.string.ChapMenuChapter));
		List<TocItem> htmltagList = (isAozora)? getHtmlTagItems(mAozoraHtml) : getHtmlTagItems(currentBook);

		if (htmltagList.isEmpty()) {
			Toast.makeText(this, "見出しが見つかりません", Toast.LENGTH_SHORT).show();
		}
		else {
			for (int i = 0; i < htmltagList.size(); i++) {
				TocItem item = htmltagList.get(i);
				int itemId = DEF.MENU_HTMLTAG + i; 
				mMenuDialog.addItem(itemId, item.title, ""); 
			}
		}

		// 見出しの追加
		mMenuDialog.show(getSupportFragmentManager(), TabDialogFragment.class.getSimpleName());
	}

	// メニューを開く
	private void openMenu() {
		Resources res = getResources();
		TabDialogFragment mMenuDialog = new TabDialogFragment(this, R.style.MyDialog, true, this);

		mMenuDialog.addSection(res.getString(R.string.operateSec));
		// 見出し選択
		mMenuDialog.addItem(DEF.MENU_SELCHAPTER, res.getString(R.string.selChapterMenu));
		// ブックマーク選択
		mMenuDialog.addItem(DEF.MENU_SELBOOKMARK, res.getString(R.string.selBookmarkMenu));
		// ブックマーク追加
		mMenuDialog.addItem(DEF.MENU_ADDBOOKMARK, res.getString(R.string.addBookmarkMenu));
		// 検索
		mMenuDialog.addItem(DEF.MENU_SEARCHTEXT, res.getString(R.string.searchTextMenu));
		// 音操作
		mMenuDialog.addItem(DEF.MENU_NOISE, res.getString(R.string.noiseMenu), FileSelectActivity.GetRecordSw());

		mMenuDialog.addSection(res.getString(R.string.settingSec));
		// テキスト表示設定
		mMenuDialog.addItem(DEF.MENU_TXTCONF, res.getString(R.string.txtConfMenu));
		// 見開き設定
		mMenuDialog.addItem(DEF.MENU_IMGVIEW, res.getString(R.string.tguide02));		// 横書き表示に変更
		if (mHorizontalWriting && mEnableHorizontalWriting) {
			mMenuDialog.addItem(DEF.MENU_HORIZONTIALWRITING, res.getString(R.string.HorizontalWriting), getSetHorizontalWriting());
		}

		// 画面方向切り替え
		if (isAozora) {
			mMenuDialog.addItem(DEF.MENU_ROTATE, res.getString(R.string.rotateMenu));
		}
		mMenuDialog.addSection(res.getString(R.string.otherSec));
		// ヘルプ
		mMenuDialog.addItem(DEF.MENU_ONLINE, res.getString(R.string.onlineMenu));
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

	private void ActionDownEvent(MotionEvent event) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		float x;
		float y;
		int cx;
		int cy;
		x = event.getRawX();
		y = event.getRawY();
		cx = rootContainer.getWidth();
		cy = rootContainer.getHeight();

		if (mImmEnable || mImmForce) {
			// IMMERSIVEモードの発動時にタッチ処理を無視する(スワイプでバーを表示させるときに重なるのを防ぐ)
			int navibar_height = 0;
			int statusibar_height = 0;
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
				// ナビゲーションバーの高さを得る
				if (rootContainer.getHeight() < rootContainer.getWidth()) {
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
			if (mImmCancel) {
				// ImmerModeの場合は上下端のタッチを無視する
				return;
			}
		}

		if (!mClickGuard) {
			// ジェスチャーナビゲーションモードで画面下からのスワイプだった場合は無視する
			int navibar_height = 0;
			int statusbar_height = 0;
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
				// ナビゲーションバーの高さを得る
				if (rootContainer.getHeight() < rootContainer.getWidth()) {
					// 横向きの場合
					navibar_height = insets.right;
					if (!mImmEnable && !mImmForce) {
						statusbar_height = insets.left;
					}
				}
				else {
					// 縦向きの場合
					navibar_height = insets.bottom;
					if (!mImmEnable && !mImmForce) {
						statusbar_height = insets.top;
					}
				}
			}
			else {
				navibar_height = mImmCancelRange;
				if (!mImmEnable && !mImmForce) {
					statusbar_height = mImmCancelRange;
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
			if ((y >= cy + statusbar_height - navibar_height) && isGestureNavigationEnabled(mActivity) == 2 && (!mImmForce && !mImmEnable)) {
				mClickGuard = true;
			}
		}
		if (mClickGuard) {
		}
		else if (mTapEditMode) {
		}
		else {
			Logcat.v(logLevel, "mGuideView.eventTouchDown()");
			mGuideView.eventTouchDown((int)x, (int)y, cx, cy, true);
		}

		mPageMode = false;

		if (y > cy - mClickArea) {
			if (mClickGuard) {
			}
			else if (mTapEditMode) {
			}
			else if (mPageSelect == PAGE_SLIDE) {
				if (mClickArea <= x && x <= cx - mClickArea) {
					// ページ選択開始
					Logcat.v(logLevel, "ページ選択開始");
					int sel = GuideView.GUIDE_BCENTER;
 					mSelectPage = GetNowPage() - 1;
					mGuideView.setGuideIndex(sel);
					
					mPageMode = true;
					mPageModeIn = true;
					
				}
			}
			else {
				mSelectPage = GetNowPage() - 1;
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
					Logcat.v(logLevel, "mGuideView.eventTouchDown()");
					mGuideView.eventTouchDown((int)x, (int)y, cx, cy, true);
					// 文書情報を表示
					
					mGuideView.setPageText(createPageStr(mSelectPage, mFilePath));
					
					mGuideView.setPageColor(mTopColor1);
				}
			}
		}
		else if (y < mClickArea) {
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
			Logcat.v(logLevel, "操作モード");
			mOperation = TOUCH_OPERATION;
			if (mTapEditMode) {
			}
			else {
				mTouchDrawLeft = (int)x;
				mTouchDrawTop = (int)y;

				if (TouchPanelView.GetTouchPositionData(3) > 0) {
					// 長押しタップの場合
					// タッチパネル設定が有効な場合
					startLongTouchTimer(DEF.HMSG_EVENT_LONG_TAP); // ロングタッチのタイマー開始
				}
			}
		}
		// タッチパネルの座標を設定する
		TouchPanelView.SetTouchPosition((int)x, (int)y, cx, cy);

		this.mTouchMove = false;
		this.mTouchFirst = true;
		this.mTouchBeginX = x;
		this.mTouchBeginY = y;
		this.mOnflingMode = false;

	}

	private void ActionUpEvent(MotionEvent event) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		float x;
		float y;
		int cx;
		int cy;
		x = event.getRawX();
		y = event.getRawY();
		cx = rootContainer.getWidth();
		cy = rootContainer.getHeight();

		if (mImmEnable || mImmForce) {
			if (mImmCancel) {
				// ImmerModeの場合は上下端のタッチを無視する
				Logcat.v(logLevel, "ImmerModeの場合は上下端のタッチを無視する");
				// UPイベントで解除
				mImmCancel = false;
				return;
			}
		}
		// タップの解除
		if (mClickGuard) {
			mClickGuard = false;
		}
		// 選択されたコマンド
		int result = mGuideView.eventTouchUp((int)x, (int)y);
		Logcat.v(logLevel, "result=" + result + ", x=" + x + ", y=" + y);
		// 情報表示クリア
		mGuideView.setPageText(null);
		mGuideView.setPageColor(Color.argb(0, 0, 0, 0));
		mGuideView.setGuideIndex(GuideView.GUIDE_NONE);

		if (mPageMode) {
			// ページ選択モード終了
			if (mPageSelect == PAGE_SLIDE) {
				if (y > cy - mClickArea) {
					if (mPageSelect == 0 || x < mClickArea || x > cx - mClickArea) {
						// ページ選択確定
						if (mSelectPage != GetNowPage() - 1) {
							// ページ変更時に振動
							startVibrate();
							RenderExec(mSelectPage);
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
				Logcat.v(logLevel, "上部選択の場合は選択リストを表示");
				showSelectList(index);
			}
			else if (result == 0x4000) {
				// 戻るボタン
				Logcat.v(logLevel, "戻るボタン");
				operationBack();
			}
			else if (result == 0x4001) {
				// メニューボタン
				// 独自メニュー表示
				Logcat.v(logLevel, "独自メニュー表示");
				openMenu();
			}
			else if (result == 0x4002 || result == 0x4003) {
				int mPageWay = DEF. PAGEWAY_RIGHT;
				int nowpage = GetNowPage() - 1;
				int totalpage = getTotalPagesNow();

				if (mDisablePageButton) {
					// 先頭/末尾ボタンを無効にする場合は何もしない
				}
				else if (mPageSelect == PAGE_SLIDE) {
					// ページ選択方法が画面下をスワイプのとき
					// 末尾ボタン
					if (result == 0x4003) {
						if (mSelectPage != totalpage - 1) {
							mSelectPage = totalpage - 1;
						}
					}
					else {
						// 右側ボタン
						if (mSelectPage != 0) {
							mSelectPage = 0;
						}
					}
					// ページ選択確定
					if (mSelectPage != nowpage) {
						// ページ変更時に振動
						startVibrate();
						RenderExec(mSelectPage);
					}
				}
				else {
					// ページ選択方法がスライダー表示かサムネイルのとき

					if (result == 0x4003) {
						// 左側ボタン
						int leftpage = mPageWay == DEF.PAGEWAY_RIGHT ? totalpage - 1 : 0;
						if (mSelectPage != leftpage) {
							mSelectPage = leftpage;
						}
					}
					else {
						// 右側ボタン
						int rightpage = mPageWay == DEF.PAGEWAY_RIGHT ? 0 : totalpage - 1;
						if (mSelectPage != rightpage) {
							mSelectPage = rightpage;
						}
					}
					// ページ選択確定
					if (mSelectPage != nowpage) {
						// ページ変更時に振動
						startVibrate();
						RenderExec(mSelectPage);
					}
				}
			}
			else {
				int nowpage = GetNowPage() - 1;
				int totalpage = getTotalPagesNow();
				switch (index) {
					case 0:
						// 1ページ次へずらす
						if (mDispMode != DEF.DISPMODE_EPUB_NORMAL && nowpage > 0) {
							startVibrate();
							mDispOffset = 1;
							RenderExec(nowpage);
						}
						break;
					case 1:
						// 1ページ前へずらす
						if (mDispMode != DEF.DISPMODE_EPUB_NORMAL && nowpage < totalpage - 1) {
							startVibrate();
							mDispOffset = -1;
							RenderExec(nowpage);
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
						// 次巻(最終ページ)
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
						if (mPageSelect == PAGE_INPUT) {
							Logcat.v(logLevel, "クリック可能エリア(upの上)");
							// 下部選択の場合は対応する操作を実行
							if (PageSelectDialog.mIsOpened == false) {
								PageSelectDialog pageDlg = new PageSelectDialog(this, R.style.MyDialog);
	
								pageDlg.setParams(DEF.TEXT_VIEWER, nowpage, totalpage, true, false, mHandler);
								pageDlg.setPageSelectListear(this);
								pageDlg.show();
								mPageDlg = pageDlg;
							}
						}
						break;
				}
			}
		}
		if (mOperation == TOUCH_OPERATION) {
			Logcat.v(logLevel, "通常タップ");
			if (mTapEditMode) {
				Logcat.v(logLevel, "タップ操作の設定ダイアログを表示させる");
				// タップ操作の設定ダイアログを表示させる
				TouchPanelView.SetAlertDialog(mActivity);
			}
			else if (mOnflingMode) {
			}
			else if (!this.mTouchMove && (TouchPanelView.GetTouchPositionData(1) > 0 || TouchPanelView.GetTouchPositionData(2) > 0)) {
				// シングルタップまたはダブルタップが有効の場合
				Logcat.v(logLevel, "シングルタップまたはダブルタップが有効の場合");
				if (TouchPanelView.GetTouchPositionData(2) > 0) {
					// ダブルタップが有効の場合は外部設定を有効にする
					Logcat.v(logLevel, "ダブルタップが有効の場合は外部設定を有効にする");
					mDoubleTapMode = true;
				}
				else {
					// シングルタップのみの場合
					// タッチパネル設定が有効な場合
					Logcat.v(logLevel, "シングルタップのみの場合");
					mDoubleTapMode = false;
					SetTouchPanelCommand(1);
				}
			}
			else if (this.mTouchFirst) {
				this.mTouchFirst = false;
				boolean next = checkTapDirectionNext(x, y, cx, cy);
				Logcat.v(logLevel, "next=" + next);
				if (mTapScrl) {
					// タップでスクロール
					if (next) {
						nextPage(true);
					}
					else {
						prevPage(true);
					}
				}
				else {
					// タップでスクロールしない
					if (next) {
						// 次ページへ
						nextPage(false);
					}
					else {
						// 前ページへ
						prevPage(false);
					}
				}
			}
		}
		// 押してる間のフラグクリア
		mTouchFirst = false;
		mOperation = TOUCH_NONE;
		this.mTouchMove = false;
	}

	private float oldx = 0;
	private float oldy = 0;

	// タップのスクロールの部分をイベント処理から分離
	private void Action_Move_Sub(MotionEvent event) {
		float x;
		float y;
		int cx;
		int cy;
		x = event.getRawX();
		y = event.getRawY();
		cx = rootContainer.getWidth();
		cy = rootContainer.getHeight();

		if (mImmCancel) {
			// ImmerModeの場合は上下端のタッチを無視する
			return;
		}

		if (mOperation == TOUCH_COMMAND) {
			// 移動位置設定
			mGuideView.eventTouchMove((int)x, (int)y);
			if (this.mPageMode && mPageSelect == PAGE_SLIDE) {
				// スライドページ選択中
				int nowpage = GetNowPage() - 1;
				int totalpage = getTotalPagesNow();
				int sel = GuideView.GUIDE_NOSEL;
				if (y >= cy - mClickArea) {
					// 操作エリアから出て戻ったらそこを基準にする
					if (!mPageModeIn) {
						// 指定のページを基準とした位置を設定
						mTouchBeginX = x - calcPageSelectRange(mSelectPage);
					}

					// タッチの位置でページを選択
					if (x < mClickArea) {
						if (mSelectPage != totalpage - 1) {
							mSelectPage = totalpage - 1;
							startVibrate();
						}
						sel = GuideView.GUIDE_BLEFT;
					}
					else if (x > cx - mClickArea) {
						if (mSelectPage != 0) {
							mSelectPage = 0;
							startVibrate();
						}
						sel = GuideView.GUIDE_BRIGHT;
					}
					else {
						mSelectPage = calcSelectPage(x);

						if (mSelectPage < 0) {
							// 最小値は先頭ページ
							mSelectPage = 0;
							// タッチ位置を先頭ページとしたときのCurrentPageの位置を求める
							mTouchBeginX = x - calcPageSelectRange(mSelectPage);
						}
						else if (mSelectPage > totalpage - 1) {
							// 最大値は最終ページ
							mSelectPage = totalpage - 1;
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

				String strPage = createPageStr(mSelectPage, mFilePath);
				String strOld = mGuideView.getPageText();
				if (!strPage.equals(strOld)) {
					if (nowpage - 1 <= mSelectPage && mSelectPage <= nowpage + 1) {
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
			if (Math.abs(oldx - x) > TAP_MOVE_LIMIT && Math.abs(oldx - x) > TAP_MOVE_LIMIT) {
				if (!this.mTouchFirst) {
					this.mTouchMove = true;
				}
			}
			oldx = x;
			oldy = y;
		}
	}

	private void startVibrate() {
		long nowTime = System.currentTimeMillis();

		if (mVibFlag) {
			if (nowTime > mPrevVibTime + TIME_VIB_TERM) {
				// 前回と間が空いているときだけ振動
				mVibrator.vibrate(TIME_VIB_RANGE);
				mPrevVibTime = nowTime;
			}
		}
	}

	// タップが前/次どちらか判定
	private boolean checkTapDirectionNext(float x, float y, int cx, int cy){
		int logLevel = Logcat.LOG_LEVEL_WARN;
		boolean next = false;

		float rate = mTapRate + 1;
		float rcx = cx / 10.0f;
		float rcy = (cy - mClickArea * 2) / 10.0f;
		Logcat.v(logLevel, "mTapPattern=" + mTapPattern + ", x=" + x + ", y=" + y + ", cx=" + cx + ", cy=" + cy + ", rcx=" + rcx + ", rcy=" + rcy + ", rate=" + rate);
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
		int logLevel = Logcat.LOG_LEVEL_WARN;
		int index = TouchPanelView.GetTouchPositionData(mode);
		Logcat.v(logLevel, "SetTouchPanelCommand() index=" + index);
		SetTouchPanelCommandMain(index);
	}

	private void SetTouchPanelCommandMain(int index) {

		int nowpage = GetNowPage() - 1;
		int totalpage = getTotalPagesNow();

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
					nextPage(true);
				}
				else {
					// タップでスクロールしない
					// 普通のタッチでページ遷移
					// 次ページへ
					nextPage(false);
				}
				break;
			case DEF.TAP_TOOLBARPREVSCROLL:
				// 前のページへスクロール
				if (mTapScrl) {
					// タップでスクロール
					prevPage(true);
				}
				else {
					// タップでスクロールしない
					// 普通のタッチでページ遷移
					// 前ページへ
					prevPage(false);
				}
				break;
			case DEF.TAP_TOOLBARLEFTMOST:
				if (nowpage == (totalpage - 1)) {
					nextPage(false);
				}
				else {
					nowpage = totalpage - 1;
					RenderExec(nowpage);
				}
				break;
			case DEF.TAP_TOOLBARLEFT100:
				if (nowpage == (totalpage - 1)) {
					nextPage(false);
				}
				else {
					nowpage += 100;
					if (nowpage > (totalpage - 1)) {
						nowpage = totalpage - 1;
					}
					RenderExec(nowpage);
				}
				break;
			case DEF.TAP_TOOLBARLEFT10:
				if (nowpage == (totalpage - 1)) {
					nextPage(false);
				}
				else {
					nowpage += 10;
					if (nowpage > (totalpage - 1)) {
						nowpage = totalpage - 1;
					}
					RenderExec(nowpage);
				}
				break;
			case DEF.TAP_TOOLBARLEFT1:
				if (nowpage == (totalpage - 1)) {
					nextPage(false);
				}
				else {
					if (nowpage < (totalpage - 1)) {
						nowpage++;
						RenderExec(nowpage);
					}
				}
				break;
			case DEF.TAP_TOOLBARRIGHT1:
				if (nowpage == 0) {
					prevPage(false);
				}
				else {
					if (nowpage > 0) {
						nowpage--;
						RenderExec(nowpage);
					}
				}
				break;
			case DEF.TAP_TOOLBARRIGHT10:
				if (nowpage == 0) {
					prevPage(false);
				}
				else {
					nowpage -= 10;
					if (nowpage < 0) {
						nowpage = 0;
					}
					RenderExec(nowpage);
				}
				break;
			case DEF.TAP_TOOLBARRIGHT100:
				if (nowpage == 0) {
					prevPage(false);
				}
				else {
					nowpage -= 100;
					if (nowpage < 0) {
						nowpage = 0;
					}
					RenderExec(nowpage);
				}
				break;
			case DEF.TAP_TOOLBARRIGHTMOST:
				if (nowpage == 0) {
					prevPage(false);
				}
				else {
					nowpage = 0;
					RenderExec(nowpage);
				}
				break;
			case DEF.TAP_TOOLBARMENU:
				// ツールバー編集ダイアログ表示
				onSelectMenuDialog(DEF.MENU_EDIT_TOOLBAR);
				break;
			case DEF.TAP_TOOLBARCONFIG:
				// 設定画面に遷移
				onSelectMenuDialog(DEF.MENU_SETTING);
				break;
			case DEF.TAP_PULLDOWNMENU:
				// プルダウンメニューに遷移
				// 独自メニュー表示
				openMenu();
				break;
			case DEF.TAP_TXTCONFMENU:
				// テキスト設定
				onSelectMenuDialog(DEF.MENU_TXTCONF);
				break;
			case DEF.TAP_TGUIDE02:
				// 見開き設定
				onSelectMenuDialog(DEF.MENU_IMGVIEW);
				break;
			case DEF.TAP_NOISEMENU:
				// マイク開始
				onSelectMenuDialog(DEF.MENU_NOISE);
				break;
			case DEF.TAP_CHGOPEMENU:
				// 操作方向の入れ替え
				onSelectMenuDialog(DEF.MENU_CHG_OPE);
				break;
			case DEF.TAP_ADDBOOKMARKMENU:
				// ブックマーク追加ダイアログ表示
				onSelectMenuDialog(DEF.MENU_ADDBOOKMARK);
				break;
			case DEF.TAP_SELBOOKMARKMENU:
				// ブックマーク選択ダイアログ表示
				onSelectMenuDialog(DEF.MENU_SELBOOKMARK);
				break;
			case DEF.TAP_TOOLBARTOC:
				// 見出し選択ダイアログ表示
				openChapterMenu();
				break;
			case DEF.TAP_TOOLBARSEARCH:
				// 検索文字列設定
				onSelectMenuDialog(DEF.MENU_SEARCHTEXT);
				break;
			case DEF.TAP_SELVIEW01:
			case DEF.TAP_SELVIEW02:
				// 見開き設定変更
				// 通し番号なのでオフセットを演算する
				int mode = index - DEF.TAP_SELVIEW01;
				if (mode == 0) {
					mDispMode = 3;
				}
				else {
					mDispMode = 0;
				}
				mGuideView.setGuideMode(mDispMode != DEF.DISPMODE_EPUB_NORMAL, mBottomFile, true, mPageSelect, false, mDisablePageButton);
				if (mDispMode != 0) {
					mDoubleMode = true;
				}
				else {
					mDoubleMode = false;
				}
				// 表示開始
				// レンダリング処理の後にレイアウトを更新
				renderSpine(currentSpineIndex, leftWebView);
				setAsyncScrollSet();
				updateLayout();
				break;
			case DEF.TAP_EXIT_VIEWER:
				finishActivity(true);
				break;
		}
	}

	// 座標から選択するページを求める
	private int calcSelectPage(float x) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		int nowpage = GetNowPage() - 1;
		int page = nowpage;
		int pagecnt = 0;
		int range = (int) Math.abs((x - mTouchBeginX)); // 絶対値
		int sign = x < mTouchBeginX ? 1 : -1; // ページ方向

		for (int i = 0; i < CTL_COUNT.length; i++) {
			if (range <= mPageRange * (CTL_COUNT[i] * CTL_RANGE[i])) {
				// 左右3単位分までページ変化なし
				page = nowpage + (pagecnt + range / (mPageRange * CTL_RANGE[i])) * sign;
				break;
			}
			// 移動範囲から減らす
			range -= mPageRange * CTL_COUNT[i] * CTL_RANGE[i];
			// その分のページ数を加算
			pagecnt += CTL_COUNT[i];
		}
		Logcat.v(logLevel, "calcSelectPage() page=" + page + ", nowpage=" + nowpage + ", range=" + range + ", x=" + x + ", mTouchBeginX=" + mTouchBeginX);
		return page;
	}

	// ページ選択時に表示する文字列を作成
	private float calcPageSelectRange(int page) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		int nowpage = GetNowPage() - 1;
		int pagecnt = Math.abs(nowpage - page); // ページの差の絶対値
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
		Logcat.v(logLevel, "calcPageSelectRange() page=" + page + ", nowpage=" + nowpage + ", range=" + range);
		// 方向を設定
		return range * (nowpage <= page ? -1 : 1);
	}

	// ページ選択時に表示する文字列を作成
	public String createPageStr(int page, String filename) {

		String strPath = filename;
		if (strPath.indexOf("smb://") == 0) {
			int idx = strPath.indexOf("@");
			if (idx >= 0) {
				strPath = "smb://" + strPath.substring(idx + 1);
			}
		}

		String pageStr;
		pageStr = (page + 1) + " / " + getTotalPagesNow() + "\n" + strPath;
		return pageStr;
	}

	public boolean onTouchEvent(MotionEvent event){
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "開始します.");

		float x;
		float y;
		int cx;
		int cy;
		x = event.getX();
		y = event.getY();
		cx = rootContainer.getWidth();
		cy = rootContainer.getHeight();

		int action = event.getAction();
		// タッチイベントで捕捉できない場合はここで処理を行う
		if (action == MotionEvent.ACTION_DOWN) {
			Logcat.v(logLevel, "ACTION_DOWN");

			ActionDownEvent(event);

			mActionDown = true;
			mActionUp = false;
		}

		if (action == MotionEvent.ACTION_MOVE) {
			Action_Move_Sub(event);
		}

		if (action == MotionEvent.ACTION_CANCEL) {
			// 押してる間のフラグクリア
			mTouchFirst = false;
			mOperation = TOUCH_NONE;

			// 上部/下部選択中の状態解除
			mGuideView.eventTouchCancel();
			// ページ選択中解除
			mGuideView.setGuideIndex(GuideView.GUIDE_NONE);
		}

		if (action == MotionEvent.ACTION_UP) {
			Logcat.v(logLevel, "ACTION_UP");
			mActionUp = true;
			mActionDown = false;

			ActionUpEvent(event);
		}
		// 親クラスの処理を返す
		return super.onTouchEvent(event);
	}
	// スレッドからの通知取得
	@SuppressLint("SuspiciousIndentation")
	public boolean handleMessage(Message msg) {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		if (DEF.ToastMessage(mActivity, msg)) {
			// HMSG_TOASTならトーストを表示して終了
			return true;
		}
		Logcat.v(logLevel, "handleMessage msg=" + msg.what);

		switch (msg.what) {
			case DEF.HMSG_RECENT_RELEASE:
				// 最新バージョンを表示
				mInformation.showRecentRelease();
				return true;

			case DEF.HMSG_ERROR:
				// 読込中の表示
				Toast.makeText(this, (String) msg.obj, Toast.LENGTH_SHORT).show();
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
					// スクロール開始
					nextPage(false);
				}
				else if (msg.arg1 == NOISE_PREVPAGE) {
					if (mNoiseScroll != 0) {
						// スクロール停止
						mNoiseScroll = 0;
					}
					// スクロール開始
					prevPage(false);
				}
				else if (msg.arg1 == NOISE_NEXTSCRL || msg.arg1 == NOISE_PREVSCRL) {
					int way = 1;
					if (msg.arg1 == NOISE_PREVSCRL) {
						way = -1;
					}
					if (way == 1) {
						nextPage(true);
					}
					else {
						prevPage(true);
					}
				}
				return true;

			case DEF.HMSG_EVENT_TOOLBAR:
				// ツールバーのタッチイベントを実行
				ToolbarDialog.SetListner(msg.arg1);
				break;
			case DEF.HMSG_EVENT_SEARCHWORD:
				// 全文検索の文字列を受信
				Logcat.v(logLevel, "DEF.HMSG_EVENT_SEARCHWORD text=" + msg.obj);
				mSearchText = (String)msg.obj;
				if (!mSearchText.isEmpty()) {
					SearchWord(mSearchText);
				}
				break;
			case DEF.HMSG_EVENT_SEARCHNEXT:
				// 全文検索の次のエントリーを受信
				Logcat.v(logLevel, "DEF.HMSG_EVENT_SEARCHNEXT index=" + msg.arg1);
				if (msg.arg1 == 1) {
					// 次検索
					SearchNext(false);
				}
				else if (msg.arg1 == 2) {
					// 前検索
					SearchPrev(false);
				}
				else if (msg.arg1 == 3) {
					// 次の章
					SearchNext(true);
				}
				else if (msg.arg1 == 4) {
					// 前の章
					SearchPrev(true);
				}
				break;
			case DEF.HMSG_EVENT_SEARCHCLEAR:
				// 全文検索のクリアを受信
				Logcat.v(logLevel, "全文検索のクリアを受信");
				clearSearchWord();
				break;

		}
		return true;
	}

	@Override
	public void onCloseMenuDialog() {
		// メニュー終了
		mMenuDialog = null;
	}

	@Override
	public void onSelectMenuDialog(int id) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// メニュークローズ
		mMenuDialog = null;
		// ページ番号入力が開いていたら閉じる
		if (PageSelectDialog.mIsOpened == true) {
			mPageDlg.dismiss();
		}
		switch (id) {
			case DEF.MENU_TXTCONF: {
				// テキスト設定
				showTextConfigDialog();
				break;
			}
			case DEF.MENU_ROTATE: {
				// 画面方向切替
				showSelectList(2);
				break;
			}
			case DEF.MENU_IMGVIEW: {
				// 見開き設定
				showSelectList(1);
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
				String url = res.getString(R.string.url_operatetext);	// 設定画面
				Intent intent;
				intent = new Intent(EpubWebViewActivity.this, HelpActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("Url", url);
				startActivity(intent);
				break;
			}
			case DEF.MENU_SETTING: {
				// 設定画面に遷移
				// 設定を呼び出す前に現在のページに関する内容を保存する
				backupSetting();
				Intent intent = new Intent(EpubWebViewActivity.this, SetConfigActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("KeyCallerName", EpubWebViewActivity.class.getName());
				startActivityForResult(intent, DEF.REQUEST_SETTING);
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
			case DEF.MENU_HORIZONTIALWRITING:
				// 横書き表示の切り替え
				float old_ratiox = ratiox;
				float old_ratioy = ratioy;
				if (getSetHorizontalWriting()) {
					setSetHorizontalWriting(false);
					// 横長から縦長へ変更
					isJapaneseMode = true;
					// 横を縦へ変換
					ratiox = 1.0f - old_ratioy;
					ratioy = 0.0f;
				}
				else {
					setSetHorizontalWriting(true);
					// 縦長から横長へ変更
					isJapaneseMode = false;
					// 縦を横へ変換
					ratioy = 1.0f - old_ratiox;
					ratiox = 0.0f;
				}
				renderSpine(currentSpineIndex, leftWebView);
				setAsyncScrollSet();
				updateLayout();
				break;
			case DEF.MENU_ADDBOOKMARK: {
				// ブックマーク追加ダイアログ表示
				BookmarkDialog bookmarkDlg = new BookmarkDialog(this, R.style.MyDialog);
				bookmarkDlg.setBookmarkListear(this);
				bookmarkDlg.setName(String.format("Page : %d / %d", GetNowPage() , getTotalPagesNow()));
				bookmarkDlg.show();
				break;
			}
			case DEF.MENU_SELBOOKMARK: {
				// ブックマーク選択ダイアログ表示
				openBookmarkMenu();
				break;
			}
			case DEF.MENU_SELCHAPTER: {
				// 見出し選択ダイアログ表示
				openChapterMenu();
				break;
			}
			case DEF.MENU_SEARCHTEXT: {
				TouchPanelView.SetAlertDialogSearchKey(mActivity);
				break;
			}
			case DEF.MENU_EDIT_TOOLBAR: {
				// ツールバー編集ダイアログ表示
				ToolbarEditDialog dialog = new ToolbarEditDialog(this, R.style.MyDialog, rootContainer.getWidth(), rootContainer.getHeight());
				dialog.show();
				break;
			}
			case DEF.MENU_TAP_PATTERN: {
				// タップ操作のパターンのダイアログを表示させる
				TouchPanelView.SetAlertDialogTag(mActivity);
				break;
			}
			case DEF.MENU_TAP_CLICK: {
				// タップ操作のクリックのダイアログを表示させる
				TouchPanelView.SetAlertDialogClick(mActivity);
				break;
			}
			case DEF.MENU_TAP_SETTING: {
				if (TouchPanelView.GetEditMode()) {
					// タップ操作の設定の編集中にする
					mTapEditMode = true;
				}
				break;
			}
			case DEF.MENU_CUSTOMKEY_SETTING:
				TouchPanelView.SetAlertDialogCustom(mActivity);
				break;

			default:
				if (id >= DEF.MENU_HTMLTAG) {
					// 見出しの場合はここへ飛んでくる
					JumpAnchor(id - DEF.MENU_HTMLTAG, false);
				}
				else if (id >= DEF.MENU_CHAPTER) {
					// 目次の場合はここへ飛んでくる
					JumpAnchor(id - DEF.MENU_CHAPTER, true);
				}
				else if (id >= DEF.MENU_BOOKMARK) {
					// ブックマーク選択の場合はここへ飛んでくる
					Logcat.v(logLevel, "DEF.MENU_BOOKMARK page="  + (id - DEF.MENU_BOOKMARK));
					try {
						RecordItem data = list_copy.get(id - DEF.MENU_BOOKMARK);
						Logcat.v(logLevel, "page=" + data.getChapter() + ", pagerate=" + data.getPageRate());
						if (data.getChapter() >= 0) {
							ratiox = (isJapaneseMode) ? data.getPageRate() : 0;
							ratioy = (isJapaneseMode) ? 0 : data.getPageRate();

							Logcat.v(logLevel, "ratiox=" + ratiox + ", ratioy=" + ratioy);
							mPageOffset = data.getChapter();
							mScrollOffset = 0;
							// 検索をクリア
							clearSearchWord();
							// レンダリング処理の後にレイアウトを更新
							renderSpine(0, leftWebView);
							setAsyncScrollSet();
							updateLayout();
						}
					}
					catch (Exception e) {
						Log.e(TAG, "エラーが発生しました");
					}
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

	private void showSelectList(int index) {
		if (mListDialog != null) {
			return;
		}
		if (index < 0 || index > mCommandStr.length) {
			// インデックスが範囲外
			return;
		}
		if (index == 3) {
			// 見出し選択
			openChapterMenu();
			return;

		}
		// 再読み込みになるのでページ戻は解除
		Resources res = getResources();

		// 選択対象
		mSelectMode = index;

		// 選択肢を設定
		String[] items = null;
		int nItem;

		String title;
		int selIndex;
		switch (index) {
			case 1:
				// 見開きモードの選択肢設定
				title = res.getString(R.string.tguide02);
				selIndex = mDispMode;
				nItem = SetEpubActivity.ViewName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetEpubActivity.ViewName[i]);
				}
				break;
			case 2:
				// 画面方向切り替え
				title = res.getString(R.string.rotateMenu);
				selIndex = mAozoraDirText;
				nItem = SetEpubActivity.DirectionName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetEpubActivity.DirectionName[i]);
				}
				break;
			default:
				return;
		}
		mListDialog = new ListDialog(this, R.style.MyDialog, title, items, selIndex, new ListDialog.ListSelectListener() {
			@Override
			public void onSelectItem(int index) {
				switch (mSelectMode) {
					case 1:
						// 見開き設定変更
						if (mDispMode != index) {
							mDispMode = index;
							mGuideView.setGuideMode(mDispMode != DEF.DISPMODE_EPUB_NORMAL, mBottomFile, true, mPageSelect, false, mDisablePageButton);
							if (mDispMode != 0) {
								mDoubleMode = true;
							}
							else {
								mDoubleMode = false;
							}
							// 表示開始
							// レンダリング処理の後にレイアウトを更新
							if (isAozora) {
								leftWebView.loadUrl(fileUrl);
								setAsyncScrollSet();
								updateLayout();
								GetLeftWidthSpecial(leftWebView, 500 + mAozoratextLength / 200);
							}
							else {
								renderSpine(currentSpineIndex, leftWebView);
								setAsyncScrollSet();
								updateLayout();
							}
						}
						break;
					case 2:
						// 画面方向切り替え
						if (mAozoraDirText != index) {
							mAozoraDirText = index;
							float old_ratiox = ratiox;
							float old_ratioy = ratioy;
							if (mAozoraDirText == 0) {
								// 横長から縦長へ変更
								isJapaneseMode = true;
								// 横を縦へ変換
								ratiox = 1.0f - old_ratioy;
								ratioy = 0.0f;
							}
							else {
								// 縦長から横長へ変更
								isJapaneseMode = false;
								// 縦を横へ変換
								ratioy = 1.0f - old_ratiox;
								ratiox = 0.0f;
							}
							// ファイルを読み直す
							File file = new File(mFilePath);
							loadSource(file);
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


	@Override
	public void onSelectPageSelectDialog(int menuId) {
		switch (menuId) {
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
				break;
			}
			case DEF.TOOLBAR_DIR_TREE: {
				// イメージビュワー専用
				break;
			}
			case DEF.TOOLBAR_TOC: {
				// テキストビュワー専用
				onSelectMenuDialog(DEF.MENU_SELCHAPTER);
				break;
			}
			case DEF.TOOLBAR_FAVORITE: {
				onSelectMenuDialog(DEF.MENU_SELBOOKMARK);
				break;
			}
			case DEF.TOOLBAR_ADD_FAVORITE: {
				onSelectMenuDialog(DEF.MENU_ADDBOOKMARK);
				break;
			}
			case DEF.TOOLBAR_SEARCH: {
				// テキストビュワー専用
				onSelectMenuDialog(DEF.MENU_SEARCHTEXT);
				break;
			}
			case DEF.TOOLBAR_SHARE: {
				// イメージビュワー専用
				break;
			}
			case DEF.TOOLBAR_SHARE_LEFT_PAGE: {
				// イメージビュワー専用
				break;
			}
			case DEF.TOOLBAR_SHARE_RIGHT_PAGE: {
				// イメージビュワー専用
				break;
			}
			case DEF.TOOLBAR_ROTATE: {
				break;
			}
			case DEF.TOOLBAR_ROTATE_IMAGE: {
				// イメージビュワー専用
				break;
			}
			case DEF.TOOLBAR_SELECT_THUMB: {
				// イメージビュワー専用
				break;
			}
			case DEF.TOOLBAR_TRIM_THUMB: {
				// イメージビュワー専用
				break;
			}
			case DEF.TOOLBAR_CONTROL: {
				onSelectMenuDialog(DEF.MENU_TXTCONF);
				break;
			}
			case DEF.TOOLBAR_MENU: {
				// ページ番号入力が開いていたら閉じる
				if (PageSelectDialog.mIsOpened == true) {
					mPageDlg.dismiss();
				}
				openMenu();
				break;
			}
			case DEF.TOOLBAR_CONFIG: {
				// ページ番号入力が開いていたら閉じる
				if (PageSelectDialog.mIsOpened == true) {
					mPageDlg.dismiss();
				}
				onSelectMenuDialog(DEF.MENU_SETTING);
				break;
			}
			case DEF.TOOLBAR_EDIT_TOOLBAR: {
				onSelectMenuDialog(DEF.MENU_EDIT_TOOLBAR);
				break;
			}
			case DEF.TOOLBAR_PROFILE1:
			case DEF.TOOLBAR_PROFILE2:
			case DEF.TOOLBAR_PROFILE3:
			case DEF.TOOLBAR_PROFILE4:
			case DEF.TOOLBAR_PROFILE5:
			case DEF.TOOLBAR_PROFILE6:
			case DEF.TOOLBAR_PROFILE7:
			case DEF.TOOLBAR_PROFILE8:
			case DEF.TOOLBAR_PROFILE9:
			case DEF.TOOLBAR_PROFILE10:
				// 何もしない
				break;
		}
	}

	@Override
	public void onSelectPage(int page) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// 現在ページ
		Logcat.v(logLevel, "onSelectPage=" + page);
		if (!mRenderBusy && !mFindWord) {
			if (selectpage != page) {
				selectpage = page;
				Logcat.v(logLevel, "RenderExec() page=" + page);
				RenderExec(page);
			}
		}
		else {
			if (selectpage != page) {
				Toast.makeText(this, "ページ読み込み処理中のためページ移動に失敗しました", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onAddBookmark(String name) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// ブックマーク追加
		// 追加したときはここへ飛んでくる
		Logcat.v(logLevel, "onAddBookmark() name=" + name);
		float rate = (isJapaneseMode) ? ratiox : ratioy;
		int type = (mFileName == null || mFileName.isEmpty()) ? RecordItem.TYPE_TEXT : RecordItem.TYPE_COMPTEXT;
		RecordList.add(RecordList.TYPE_BOOKMARK, type, mServer, mLocalFileName
				, mTextName, new Date().getTime(), null, currentSpineIndex, rate, GetNowPage(), name);
	}

	// テキスト設定用ダイアログ表示
	private void showTextConfigDialog() {
		if (mEpubWebViewConfigDialog != null) {
			return;
		}
		mEpubWebViewConfigDialog = new EpubWebViewConfigDialog(this, R.style.MyDialog, false, this);

		mEpubWebViewConfigDialog.setConfig(mFontText, mFontBody, mFontInfo, mMarginW, mMarginH, mScaleValiable, false);
		mEpubWebViewConfigDialog.setTextConfigListner(new EpubWebViewConfigDialog.EpubWebViewConfigListenerInterface() {
			@Override
			public void onButtonSelect(int select, int text, int body, int info, int marginw, int marginh, boolean issave) {
				// 選択状態を通知
				boolean ischange = false;
				// 変更があるかを確認(適用後のキャンセルの場合も含む)
				if (mFontText != text && mScaleValiable || mFontBody != body || mFontInfo != info || mMarginW != marginw || mMarginH != marginh) {
					ischange = true;
				}
				if (mScaleValiable) {
					mFontText = text;
				}
				mFontBody = body;
				mFontInfo = info;
				mMarginW = marginw;
				mMarginH = marginh;
				mIsConfSave = issave;
				if (ischange) {
					// 表示を更新
					mInfoSize = DEF.calcFont(mFontInfo);
					mTextSize = mFontBody * 2;
					mScaleDensity = (mScaleValiable) ? getResources().getDisplayMetrics().density * (float)(mFontText * 2) / 100 : getResources().getDisplayMetrics().density;
					mScaleFix = (mScaleValiable) ? (int)(mScaleDensity * 100) : 0;
					// 本文のフォントの拡大率を変更
					WebSettings settings = leftWebView.getSettings();
					settings.setTextZoom(mTextSize);
					leftWebView.setInitialScale(mScaleFix);
					leftWebView.setBackgroundColor(colorInt);
					settings = rightWebView.getSettings();
					settings.setTextZoom(mTextSize);
					rightWebView.setInitialScale(mScaleFix);
					rightWebView.setBackgroundColor(colorInt);

					if (mLoadingSpinner != null) {
						mLoadingSpinner.setVisibility(View.VISIBLE);
						mLoadingSpinner.bringToFront();
						rootContainer.requestLayout(); // レイアウトを更新
					}
					// 表示を再初期化
					runOnUiThread(() -> {
						parentLayout.setBackgroundColor(colorInt);
						parentLayout.invalidate(); // 強制再描画
					});
					initWrappers();
					SetFrameLayout(false);
					// 先にレイアウトを更新
					setAsyncScrollSet();
					updateLayout();
					renderSpine(currentSpineIndex, leftWebView);
				}
			}

			@Override
			public void onClose() {
				// 終了
				mEpubWebViewConfigDialog = null;
			}
		});
		mEpubWebViewConfigDialog.show(getSupportFragmentManager(), TabDialogFragment.class.getSimpleName());
	}

	// メニューを開く
	private void openBookmarkMenu() {

		ArrayList<RecordItem>list = RecordList.load(null, RecordList.TYPE_BOOKMARK, mServer, mLocalFileName, mTextName);
		if (list == null && list.size() == 0) {
			Toast.makeText(this, R.string.bmNotFound, Toast.LENGTH_SHORT).show();
			return;
		}
			Resources res = getResources();
		TabDialogFragment mMenuDialog = new TabDialogFragment(this, R.style.MyDialog, false, this);
		// ブックマーク選択
		mMenuDialog.addSection(res.getString(R.string.selBookmarkMenu));
		// ブックマークのコピーを作る
		list_copy = new ArrayList<RecordItem>(list);
		// ArrayListをソート
		if (list_copy != null) {
			Collections.sort(list_copy, new FileSelectActivity.BookmarkComparator((short) mRBSort));
		}
		ArrayList<ImageActivity.BookMark> bookmark = new ArrayList<ImageActivity.BookMark>();
		for (int i = 0; i < list_copy.size(); i++) {
			// ソートした結果でブックマークをArrayListへ追加
			RecordItem data = list_copy.get(i);
			int page = data.getPage();
			bookmark.add(new ImageActivity.BookMark(data.getDispName(), "P." + (page + 1) + "," + (data.getChapter() + 1) + "章(" + String.format("%.0f", (isJapaneseMode) ? (1.0f - data.getPageRate()) * 100 : data.getPageRate() * 100) + "%)", 0));
		}
		for (int i = 0 ; i < list.size(); i ++) {
			// ブックマーク追加
			// ここで追加した位置がそのままジャンプ先のページになる
			// テキストサイズや縦横の切り替え及び見開きの有無でページが変わってしまうのはやむを得ない
			mMenuDialog.addItem(DEF.MENU_BOOKMARK + i,bookmark.get(i).getTitle(),bookmark.get(i).getValue());
		}

		mMenuDialog.show(getSupportFragmentManager(), TabDialogFragment.class.getSimpleName());
	}

	// 見出しのテキストを頼りに、JavaScriptでその場所を探してスクロールさせる
	public void jumpToHeadingByText(WebView webView, String headingText) {
		// JavaScriptを組み立てる
		// ページ内の全 h1, h2 から、テキストが一致するものを探すスクリプト
		String js = "javascript:(function() {" +
			"  var targets = document.querySelectorAll('h1, h2');" +
			"  for (var i = 0; i < targets.length; i++) {" +
			"    if (targets[i].innerText.trim() === '" + headingText + "') {" +
			"      targets[i].scrollIntoView(true);" +
			"      break;" +
			"    }" +
			"  }" +
			"})()";
		webView.loadUrl(js);
	}

	public void getHeadingLocation(WebView webView, String headingText) {
		// JavaScriptを組み立て
		String js = "(function() {" +
			"  var targets = document.querySelectorAll('h1, h2');" +
			"  for (var i = 0; i < targets.length; i++) {" +
			"    if (targets[i].innerText.trim() === '" + headingText.replace("'", "\\'") + "') {" +
			"      var rect = targets[i].getBoundingClientRect();" +
			"      var isVertical = window.getComputedStyle(targets[i]).writingMode.includes('vertical');" +
			" var y = window.pageYOffset || "+
			"     document.documentElement.scrollTop || "+
			"     document.body.scrollTop || 0;"+
			" var x = window.pageXOffset || "+
			"     document.documentElement.scrollLeft || "+
			"     document.body.scrollLeft || 0;"+

		   	"     return x + ',' + y + ',' + isVertical;"+
			"    }" +
			"  }" +
			"  return null;" +
			"})()";

		webView.evaluateJavascript(js, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				if (value != null && !value.equals("null")) {
					// ここで値をパースしてスクロール処理などを行う
					// 前後の引用符を削除
					String cleanValue = value.replace("\"", "");
					String[] parts = cleanValue.split(",");
					if (parts.length >= 3) {
						float d = mScaleDensity;
						currentScrolllX = maxScrollX + (int)(Float.parseFloat(parts[0]) * d);
						currentScrolllY = (int)(Float.parseFloat(parts[1]) * d);
						PutFooterView();
					}
				}
			}
		});
	}

	// 見出しのジャンプ処理
	private void JumpAnchor(int offset, boolean mode) {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		if (isAozora) {
			mAnchorl = true;

			List<TocItem> tocList;
			tocList = getHtmlTagItems(mAozoraHtml);
			TocItem item = tocList.get(offset);

			// 検索をクリア
			clearSearchWord();

			jumpToHeadingByText(leftWebView, item.title);
			getHeadingLocation(leftWebView, item.title);
			GetLeftWidthSpecial(leftWebView, 500 + mAozoratextLength / 200);
			return;
		}

		List<TocItem> tocList;
		tocList = (mode) ? getTocItems(currentBook) : getHtmlTagItems(currentBook);
		TocItem item = tocList.get(offset);
		// 項目がタップされた時の処理
		Logcat.v(logLevel, "item.href=" + item.href);
		targetItem = item.href;
		String targetUrl = "https://epub.local/" + targetItem;
		if (targetUrl.contains("#epubcfi")) {
			// 何もしない(エラー画面へ遷移させない)
			Log.e(TAG, "Ignored CFI link: " + targetUrl);
		}
		else {
			try {
				URI uri = new URI(targetUrl);
				mAnchorStr = uri.getFragment(); // ここでアンカーを取得
				Logcat.v(logLevel, "アンカー名: " + mAnchorStr + ", item=" + targetItem); 
				String result = targetItem;
				// 指定した文字が最初に現れる位置を取得
				int index = result.indexOf('#');
				if (index != -1) {
					// 0番目から指定文字の手前までを切り出す
					result = result.substring(0, index);
				}
				// Bookオブジェクトから該当リソースを探す
				Logcat.v(logLevel, "result=" + result);
				Resource targetResource = currentBook.getResources().getByHref(result);
				if (targetResource != null) {
					// Spine内でのインデックス(何番目の章か)を取得
					currentSpineIndex = currentBook.getSpine().getResourceIndex(targetResource);
					Logcat.v(logLevel, "このアンカーは Spine の " + currentSpineIndex + " 番目にあります");
				}
				else {
					Logcat.v(logLevel, "Spineに直接見つかりません。画像リソースの可能性があります: " + result);
					// Spine(章のリスト)をループ
					List<SpineReference> spineReferences = currentBook.getSpine().getSpineReferences();
					for (int i = 0; i < spineReferences.size(); i++) {
						Resource chapterResource = spineReferences.get(i).getResource();
						// リソースの形式(MediaType)を確認
						String mediaType = chapterResource.getMediaType().toString();
						if (mediaType.startsWith("image/")) {
							// 画像そのものがSpineに入っている特殊なEPUBの場合
							if (result.contains(chapterResource.getHref()) || chapterResource.getHref().contains(result)) {
								currentSpineIndex = i;
								Logcat.v(logLevel, "このアンカーは Spine の " + currentSpineIndex + " 番目にあります");
								break;
							}
						}
					}
				}
			}
			catch (URISyntaxException e) {
				e.printStackTrace();
			}
			mAnchorl = true;

			// 検索をクリア
			clearSearchWord();

			if (mLoadingSpinner != null) {
				mLoadingSpinner.setVisibility(View.VISIBLE);
				mLoadingSpinner.bringToFront();
			}

			leftWebView.setAlpha(0f);
			if (mDoubleMode) rightWebView.setAlpha(0f);
			renderSpine(currentSpineIndex, leftWebView);
			jumpToAnchor(leftWebView, targetItem);
		}
	}

	// ページの頭出し
	private void RenderExec(int page) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "RenderExec page=" + page);
		int total = 0;
		int nowpage = page;
		int getindex = 0;
		// 章の頭出し
		if (isAozora) {
		}
		else {
			for (int i = 0; i < currentBook.getSpine().size(); i++) {
				getindex = i;
				total = getSavedPageCount(i);
				Logcat.v(logLevel, "index=" + i + ", total=" + total + ", page=" + page);
				// 章のページに収まったらループ終了
				Logcat.v(logLevel, "章のページに収まったらループ終了");
				if (page < total) break;
				// 次の章を頭出し
				Logcat.v(logLevel, "次の章を頭出し");
				page -= total;
			}
		}
		int totalpage = getTotalPagesNow();
		Logcat.v(logLevel, "total=" + totalpage + ", page=" + nowpage);
		Logcat.v(logLevel, "mPageOffset=" + getindex + ", mScrollOffset=" + page);
		// 表示開始
		if (!isAozora && getindex != currentSpineIndex) {
			Logcat.v(logLevel, "renderSpine()を実行 onSelectPage()");
			int dup = (mDoubleMode) ? 2 : 1;
			mPageOffset = getindex;
			mScrollOffset = page * dup;
			renderSpine(0, leftWebView);
		}
		else {
			Logcat.v(logLevel, "スクロール移動のみ");
			int dup = (mDoubleMode) ? 2 : 1;
			if (isJapaneseMode && !mPageMoveEnable) {
				currentScrolllX = maxScrollX - scrollXStep * page * dup - scrollXStep * mDispOffset;
				mDispOffset = 0;
				smoothScrollTo(Math.max(0, currentScrolllX), 0, false);
			}
			else {
				currentScrolllY = 0 + scrollYStep * page * dup + scrollYStep * mDispOffset;
				mDispOffset = 0;
				smoothScrollTo(0, Math.min(maxScrollY, currentScrolllY), false);
			}
			PutFooterView();
		}
	}
	// 現在のページを得る
	private int GetNowPage() {
		int total = 1;
		if (isAozora) {
			if (isJapaneseMode) {
				if (scrollXStep > 0) {
					total += calcCurrentPageNow((maxScrollX - currentScrolllX) ,scrollXStep, mUpDownMode, true);
				}
			}
			else {
				if (scrollYStep > 0) {
					total += calcCurrentPageNow(currentScrolllY ,scrollYStep, mUpDownMode, false);
				}
			}
			return total;
		}
		if (currentBook == null) return 0;
		for (int i = 0; i < currentSpineIndex; i++) {
			total += getSavedPageCount(i);
		}
		if (isJapaneseMode && !mPageMoveEnable) {
			if (scrollXStep > 0) {
				total += calcCurrentPageNow((maxScrollX - currentScrolllX) ,scrollXStep, mUpDownMode, true);
			}
		}
		else {
			if (scrollYStep > 0) {
				total += calcCurrentPageNow(currentScrolllY ,scrollYStep, mUpDownMode, false);
			}
		}
		return total;
	}

	// 設定の読み込み
	private void ReadSetting() {
		// 設定値取得
		EpubWebViewSharedData bigData = new Gson().fromJson(jsonEpubWebViewString, EpubWebViewSharedData.class);
		if (bigData != null) {
			mDispMode = bigData.mDispMode;
			mNotice = bigData.mNotice;
			mForceNotice = bigData.mForceNotice;
			mNoSleep = bigData.mNoSleep;
			mImmEnable = bigData.mImmEnable;
			mImmForce = bigData.mImmForce;
			mTextColor = bigData.mTextColor;
			mBackColor = bigData.mBackColor;
			mGradColor = bigData.mGradColor;
			mGradation = bigData.mGradation;
			mSrchColor = bigData.mSrchColor;
			mMgnColor = bigData.mMgnColor;
			mTopColor1 = bigData.mTopColor1;
			mTimeDisp = bigData.mTimeDisp;
			mTimeFormat = bigData.mTimeFormat;
			mTimePos = bigData.mTimePos;
			mTimeSize = bigData.mTimeSize;
			mTimeColor = bigData.mTimeColor;
			mConfirmBack = bigData.mConfirmBack;
			mClickArea = bigData.mClickArea;
			mPageSelect = bigData.mPageSelect;
			mPageRange = bigData.mPageRange;
			mOldMenu = bigData.mOldMenu;
			mBottomFile = bigData.mBottomFile;
			mChgPage = bigData.mChgPage;
			mChgPageKey = bigData.mChgPageKey;
			mTapPattern = bigData.mTapPattern;
			mTapRate = bigData.mTapRate;
			mTapScrl = bigData.mTapScrl;
			mVolKeyMode = bigData.mVolKeyMode;
			mNoiseScrl = bigData.mNoiseScrl;
			mNoiseUnder = bigData.mNoiseUnder;
			mNoiseOver = bigData.mNoiseOver;
			mNoiseLevel = bigData.mNoiseLevel;
			mNoiseDec = bigData.mNoiseDec;
			mLastMsg = bigData.mLastMsg;
			mVibFlag = bigData.mVibFlag;
			mFlickPage = bigData.mFlickPage;
			mFlickEdge = bigData.mFlickEdge;
			mFontText = bigData.mFontText;
			mFontBody = bigData.mFontBody;
			mFontInfo = bigData.mFontInfo;
			mMarginW = bigData.mMarginW;
			mMarginH = bigData.mMarginH;
			mDisableTextInfo = bigData.mDisableTextInfo;
			mTextFrame = bigData.mTextFrame;
			mDisablePageButton = bigData.mDisablePageButton;
			mScaleValiable = bigData.mScaleValiable;
			mTxtColor = bigData.mTxtColor;
			mBakColor = bigData.mBakColor;
			mFixBodyColor = bigData.mFixBodyColor;
			mFixBackgroundColor = bigData.mFixBackgroundColor;
			mReturnListView = bigData.mReturnListView;
			mViewRota = bigData.mViewRota;
			mRevtRota = bigData.mRevtRota;
			mRotateBtn = bigData.mGetRotateBtn;
			mNoCache = bigData.mNoCache;
			mHorizontalWriting = bigData.mHorizontalWriting;
			fontname = bigData.fontname;
			for (int i = 0; i < DEF.KEY_CODE_CUSTOM_MAX; i++) {
				mLoadCustomkeyCode[i] = bigData.mLoadCustomkeyCode[i];
			}
			for (int i = 0; i < (DEF.KEYCODE_INDEX.length + DEF.KEY_CODE_CUSTOM_MAX); i++) {
				mGetHardwareKeySetData[i] = bigData.mGetHardwareKeySetData[i];
			}
			mRBSort = bigData.mRBSort;
		}
		if (mSdkVersion >= 19) {
			// KitKat以降のみ設定読み込み
		}
		else {
			mImmEnable = false;
			mImmForce = false;
		}
		mTopColor2 = 0x40000000 | (mTopColor1 & 0x00FFFFFF);
		mNoiseSwitch.setConfig(mNoiseUnder, mNoiseOver, mNoiseDec);
		// ヘッダー/フッターのフォントサイズを計算
		mInfoSize = DEF.calcFont(mFontInfo);
		mTextSize = mFontBody * 2;
		mHeaderOn = (mDisableTextInfo) ? false : true;
		mFooterOn = (mDisableTextInfo) ? false : true;
		// テキストの拡大率を計算
		mScaleDensity = (mScaleValiable) ? getResources().getDisplayMetrics().density * (float)(mFontText * 2) / 100 : getResources().getDisplayMetrics().density;
		mScaleFix = (mScaleValiable) ? (int)(mScaleDensity * 100) : 0;
		colorInt = mBakColor;
		bodyColor = mTxtColor;
	}

	// サーフェスビューの設定
	private class MyGuideSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
		private GuideView guide;

		public MyGuideSurfaceView(Context context, GuideView guide) {
			super(context);
			this.guide = guide;
			surfaceHolder = getHolder(); 
			surfaceHolder.setFormat(PixelFormat.RGBA_8888);
			surfaceHolder.addCallback(this);
			// 背景を透過させる設定
			setZOrderOnTop(true);
			surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			int logLevel = Logcat.LOG_LEVEL_WARN;
			// ここでスレッドを起動して guide.draw(canvas, ...) を呼び出す
			Logcat.v(logLevel, "surfaceCreated");
			mThread = new DrawThread(holder, guide);
			mThread.setRunning(true);
			mThread.start();
		}

		@Override
		public void surfaceChanged(SurfaceHolder h, int f, int w, int h2) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder h) {
			boolean retry = true;
			mThread.setRunning(false); // ループを止める
			while (retry) {
				try {
					mThread.join(); // スレッドが完全に終了するのを待つ
					retry = false;
				}
				catch (InterruptedException e) {
					// 停止待ちを継続
				}
			}
		}
	}

	// サーフェスビューの描画スレッド
	private class DrawThread extends Thread {
		private final SurfaceHolder surfaceHolder;
		private final GuideView guideView;
		private boolean running = false;

		public DrawThread(SurfaceHolder holder, GuideView guide) {
			this.surfaceHolder = holder;
			this.guideView = guide;
		}

		public void setRunning(boolean run) {
			this.running = run;
		}

		@Override
		public void run() {
			while (running) {
				Canvas canvas = null;
				try {
					// Canvasをロックして取得
					canvas = surfaceHolder.lockCanvas();
					if (canvas != null) {
						// 以前の描画内容をクリア(透過設定を維持するため必須)
						canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
						// GuideViewの描画ロジックを呼び出す
						// ※引数のサイズは現在のCanvas(画面)のサイズを渡す
						guideView.draw(canvas, canvas.getWidth(), canvas.getHeight());
						if (mTapEditMode) {
							TouchPanelView.SetViewArea(canvas.getWidth(), canvas.getHeight());
							TouchPanelView.Drawmain(canvas);
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					// 描画を確定して画面に反映(必ずfinallyで行う)
					if (canvas != null) {
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
				// フレームレート(FPS)の調整
				// 常に全力で回すとCPU負荷が高すぎるため少し待機
				try {
					Thread.sleep(33);
				}
				catch (InterruptedException e) {
					// 終了処理
				}
			}
		}
	}

	// タップ操作の設定が完了したら呼び出される
	public static void UpdateTouchPanelData() {
	}

	// EPUBファイルをストリームから読み出す
	public static InputStream getStream(Context context, String path, String user, String pass) throws Exception {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "path=" + path);
		if (path.startsWith("content://")) {
			// SAF
			return context.getContentResolver().openInputStream(Uri.parse(path));
		}
		else if (path.startsWith("smb://")) {
			// SMB
			CIFSContext mSmbContext = SingletonContext.getInstance()
				.withCredentials(new NtlmPasswordAuthenticator(null, user, pass));
			return new SmbFile(path, mSmbContext).getInputStream();
		}
		else {
			// Local
			return new FileInputStream(path);
		}
	}

	private void setAsyncScrollSet() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// 非同期処理を開始
		CompletableFuture.runAsync(() -> {
			Logcat.v(logLevel, "mInitSet = true");
			mInitSet = true;
			// レイアウトが変更されると検索位置が先頭に戻るため初期化を行わせる
			mSearchInitSet = true;
			Logcat.v(logLevel, "バックグラウンドで重い処理を実行中...");
			Logcat.v(logLevel, "mTextLength=" + mTextLength);
			try {
				Thread.sleep(500 + mTextLength / 50);
			}
			catch (InterruptedException e) {
			}
			Logcat.v(logLevel, "処理完了！");
			Logcat.v(logLevel, "currentScrolllX=" + currentScrolllX + ", currentScrolllY=" + currentScrolllY);
			Logcat.v(logLevel, "mInitSet = false");
			if (mTextLength != 0) {
				mInitSet = false;
				if (isAozora) {
					// 青空文庫のテキストの場合は別実行されるため除外
				}
				else {
					currentScrolllX = mBackupCurrentScrolllX;
					currentScrolllY = mBackupCurrentScrolllY;
					Logcat.v(logLevel, "currentScrolllX=" + currentScrolllX + ", currentScrolllY=" + currentScrolllY + ", maxScrollX=" + maxScrollX + ", maxScrollY=" + maxScrollY + ", scrollXStep=" + scrollXStep + ", scrollYStep=" + scrollYStep);
					// WebViewの操作(syncWebViewScroll)だけをメインスレッドに戻す
					runOnUiThread(() -> {
						Logcat.v(logLevel, "UIスレッドでスクロール同期を実行");
						syncWebViewScroll(); 
					});
				}
			}
		});
	}

	// EPUBファイルを読み出す
	private void loadEpub(String path) {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		if (mLoadingSpinner != null) {
			mLoadingSpinner.setVisibility(View.VISIBLE);
			mLoadingSpinner.bringToFront();
			// レイアウトを更新
			rootContainer.requestLayout();
		}
		if (isAozora) {
			// 青空文庫の場合は特別に処理
			mTotalPage = new int[1];
			meta = null;
			try {
				meta = new PageMetadata();
				meta.isFixed = false;
				meta.isReflowable = false;
				if (mAozoraDirText == 0) {
					isJapaneseMode = true;
				}
				else {
					isJapaneseMode = false;
				}
			}
			catch (Exception e) {
				Log.e(TAG, "Error during fetchMetadata", e);
			}
			if (mCompleted) {
				mCompleted = false;
				Logcat.v(logLevel, "Completed");
				if (isJapaneseMode) {
					ratiox = 0.0f;
					ratioy = 0.0f;
				}
				else {
					ratiox = 0.0f;
					ratioy = 1.0f;
				}
			}
			else if (mFirstRead) {
				mFirstRead = false;
				Logcat.v(logLevel, "FirstRead");
				mPageOffset = 0;
				if (isJapaneseMode) {
					ratiox = 1.0f;
					ratioy = 0.0f;
				}
				else {
					ratiox = 0.0f;
					ratioy = 0.0f;
				}
			}
			File file = new File(path);
			loadSource(file);
			return;
		}

		new Thread(() -> {
			try {
				epub4jis = getStream(mActivity, path, mUser, mPass);
				// 1MBバッファ
				BufferedInputStream bis = new BufferedInputStream(epub4jis, 1024 * 1024); 
				Book book = new EpubReader().readEpub(bis);
				java.util.Map<String, Resource> tempCache = new java.util.HashMap<>();
				for (Resource r : book.getResources().getAll()) {
					if (r.getHref() != null) tempCache.put(r.getHref(), r);
				}
				// UIスレッドに戻して判定と表示を行う
				runOnUiThread(() -> {
					currentBook = book;
					initTotalPage();
					// バックグラウンドでスキャン開始
					initMeasurementWebView();
					startBackgroundScan();
					// キャッシュを反映
					resourceCache = tempCache; 
					displayNavAnchors(currentBook);
					displayNcxAnchors(currentBook);
					if (mFirstRead) {
						Logcat.v(logLevel, "FirstRead");
						mPageOffset = 0;
					}
					// 表示開始
					// レンダリング処理の後にレイアウトを更新
					renderSpine(0, leftWebView);
					setAsyncScrollSet();
					updateLayout();
				});
			}
			catch (Exception e) { 
				Log.e(TAG, "Load error", e); 
			}
		}).start();
	}

	// フォントをメモリにロードするメソッド
	private void prepareUserFont() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (mFontFile == null || mFontFile.isEmpty()) return;
		// SMBやSAFの場合一度ローカルのキャッシュファイルにコピーする
		File cacheFont = new File(getCacheDir(), "current_font.ttf");
		try (InputStream is = getStream(this, mFontFile, mUser, mPass);
			FileOutputStream fos = new FileOutputStream(cacheFont)) {
			byte[] buffer = new byte[8192];
			int len;
			while ((len = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			Logcat.v(logLevel, "Font cached to: " + cacheFont.getAbsolutePath());
		}
		catch (Exception e) {
			Log.e(TAG, "Font cache error", e);
		}
	}

	private final View.OnTouchListener commonTouchListener = (v, event) -> {
		// タッチ処理の共通ロジック
		boolean intercepted = mDetector.onTouchEvent(event);
		// ページめくり(タップやスワイプ)を検知したら true を返して
		// WebViewには「このイベントは処理済み」と伝える
		return intercepted;
	};


	private final WebChromeClient commonChromeClient = new WebChromeClient() {
		// 共通の処理(プログレスバーなど)
	};

	// WebViewクライアントの設定
	private final WebViewClient commonWebViewClient = new WebViewClient() {
		@Override
		public void onPageFinished(WebView view, String url) {
			int logLevel = Logcat.LOG_LEVEL_WARN;
			Logcat.v(logLevel, "onPageFinished()");

			view.postDelayed(() -> {
				mImageOnly = false;
				CheckImageOnly(view);

				WebSettings settings = view.getSettings();
				mScalingOn = false;
				SetTextColor(view, bodyColor, mFixBodyColor);
				SetBackGroundColor(view, colorInt, mFixBackgroundColor);
				String isImageOnlyJs = "";
				if (meta.isImageOnly) {
					Logcat.v(logLevel, "applyImageOnlyStyle()");
					view.setInitialScale(0);
				}
				else if (meta.isReflowable) {
					// epub4jで解析したviewport幅を取得
					int vWidth = getViewportWidth(currentResource); 
					if (vWidth > 0) {
						// 固定レイアウト用のスケーリング実行
						Logcat.v(logLevel, "固定レイアウト用のスケーリング実行 vWidth=" + vWidth);
						// meta viewportタグを有効にする
						settings.setUseWideViewPort(true);
						// コンテンツを画面幅にフィットさせる
						settings.setLoadWithOverviewMode(true);
						view.setInitialScale(0);
						mScalingOn = true;
					}
					else {
						// 通常のリフロー表示(width=100%にするなど)
						Logcat.v(logLevel, "通常のリフロー表示");
						// meta viewportタグを有効にする
						settings.setUseWideViewPort(true);
						// コンテンツを画面幅にフィットさせる
						settings.setLoadWithOverviewMode(false);
						view.setInitialScale(mScaleFix);
						applyReflowStyle(view);
					}
				}
				else if (meta.isFixed && !isJapaneseMode) {
					Logcat.v(logLevel, "meta.isFixed (onPageFinished)");
					// meta viewportタグを有効にする
					settings.setUseWideViewPort(true);
					// コンテンツを画面幅にフィットさせる
					settings.setLoadWithOverviewMode(true);  
					view.setInitialScale(0);
				}
				else {
					// 通常ページは画像の横幅制限だけ行いスクロールは許可する
					view.setInitialScale(mScaleFix);
					Logcat.v(logLevel, "applyNormalStyle()");
					if (isJapaneseMode) {
						applyNormalStyleWidth(view);
					}
					else {
						applyNormalStyleHeight(view);
					}
					// マージンカット
					SetMarginCut(view);
				}

				// 注入するJSを構築
				String scrollJs = "";
				// ユーザー設定フォントがある場合のみCSSを注入
				String userFontPath = mFontFile;
				if (userFontPath != null && !userFontPath.isEmpty()) {
					Logcat.v(logLevel, "すべての要素に読み込ませたフォントを強制");
					// すべての要素に読み込ませたフォント(ここでは名前を "UserFont" と仮定)を強制
					// ダミーURLでリクエストを発生させる
					String js = "var style = document.createElement('style');" +
						"style.innerHTML = '@font-face { font-family: \"UserFont\"; src: url(\"any_font.ttf\"); } " + 
						"body, div, p, span, a { font-family: \"UserFont\" !important; }';" +
						"document.head.appendChild(style);" +

						"document.fonts.ready.then(function() { AndroidHost.onRenderReady(); });";
					view.evaluateJavascript(js, null);
				}

				Logcat.v(logLevel, "スクロールサイズ解析開始");
				if (view != null) {
					view.post(() -> {
						// UIスレッドの空きを待ってから描画の直前を狙う
						view.postOnAnimation(new Runnable() {
							@Override
							public void run() {
								// ここでスクロールサイズ解析を実行
								calculateScrollRange(view);
							}
						});
					});
				}
			}, 200); // レンダリング安定のための待機
		}

		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
				String path = request.getUrl().getPath();
				if (path != null && path.startsWith("/")) path = path.substring(1);
				if (isAozora && (path.endsWith(".ttf") || path.endsWith(".otf"))) {
					File cacheFont = new File(getCacheDir(), "current_font.ttf");
					if (cacheFont.exists()) {
						try {
							// FileInputStreamを直接渡す
							// WebView側で読み込みが終わると自動的にcloseされる
							FileInputStream fis = new FileInputStream(cacheFont);
							return new WebResourceResponse("font/ttf", "UTF-8", fis);
						}
						catch (FileNotFoundException e) {
							Log.e(TAG, "Font file not found during intercept", e);
						}
					}
					return null;
				}
				if (currentBook == null || path == null) return null;

				boolean isFontRequest = (path.endsWith(".otf") || path.endsWith(".ttf") || path.endsWith(".woff"));

				String url = request.getUrl().toString().toLowerCase();
				if (url.endsWith(".mp3") || url.endsWith(".mp4") || url.endsWith(".webm") || url.endsWith(".wav") || url.endsWith(".m4a") || url.endsWith(".ogg")) {
					// 音声や動画の拡張子が含まれていたら空のデータを返してブロックする
					return new WebResourceResponse("audio/mpeg", "UTF-8", null);
				}
				// 優先順位1: ユーザーフォントがあれば返す
				if (isFontRequest) {
					File cacheFont = new File(getCacheDir(), "current_font.ttf");
					if (cacheFont.exists()) {
						try {
							// FileInputStreamを直接渡す
							// WebView側で読み込みが終わると自動的にcloseされる
							FileInputStream fis = new FileInputStream(cacheFont);
							return new WebResourceResponse("font/ttf", "UTF-8", fis);
						}
						catch (FileNotFoundException e) {
							Log.e(TAG, "Font file not found during intercept", e);
						}
					}
				}
				// 優先順位2: EPUB内の本来のリソースを探す
				Resource r = resourceCache.get(path);
				if (r == null) {
					for (String key : resourceCache.keySet()) {
						if (path.endsWith(key)) {
							r = resourceCache.get(key);
							break;
						}
					}
				}

				if (r != null) {
					try {
						String mimeType = r.getMediaType().getName();
						if (mimeType.contains("html")) {
							// 横書きモードがONのときだけ変換する
							if (getSetHorizontalWriting()) {
								String html = convertStreamToString(r.getInputStream());
								html = applyHorizontalStyles(html);
								InputStream newStream = new ByteArrayInputStream(html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
								return new WebResourceResponse(mimeType, "UTF-8", newStream);
							}
							else {
								// 横書きOFF(縦書き)なら本来のデータをそのまま返す
								return new WebResourceResponse(mimeType, "UTF-8", new ByteArrayInputStream(r.getData()));
							}
						}
						else {
							return new WebResourceResponse(mimeType, "UTF-8", new ByteArrayInputStream(r.getData()));
						}
					}
					catch (Exception e) { 
						Log.e(TAG, "Error during cache html conversion", e);
						return null; 
					}
				}

				if (isFontRequest) {
					File cacheFont = new File(getCacheDir(), "current_font.ttf");
					if (cacheFont.exists()) {
						try {
							// FileInputStreamを直接渡す
							// WebView側で読み込みが終わると自動的にcloseされる
							FileInputStream fis = new FileInputStream(cacheFont);
							return new WebResourceResponse("font/ttf", "UTF-8", fis);
						}
						catch (FileNotFoundException e) {
							Log.e(TAG, "Font file not found during intercept", e);
						}
					}
					// フォントがない場合は404
					return new WebResourceResponse("font/ttf", "UTF-8", 404, "Not Found", null, null);
				}
				// "https://epub/" で始まるリクエストをすべて横取りする
				url = request.getUrl().toString();
				if (url.startsWith("https://epub/")) {
					try {
						// epub4jからリソースを取得
						Resource res = currentBook.getSpine().getResource(currentIndex);
						InputStream is = res.getInputStream();
						// 横書きモードがONのときだけ変換する
						if (getSetHorizontalWriting()) {
							String html = convertStreamToString(is);
							html = applyHorizontalStyles(html);
							InputStream newStream = new ByteArrayInputStream(html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
							// 文字化けを防ぐため UTF-8 を指定
							return new WebResourceResponse("text/html", "UTF-8", newStream);
						}
						else {
							// 横書きOFF(縦書き)ならそのままストリームを返す
							// 文字化けを防ぐため UTF-8 を指定
							return new WebResourceResponse("text/html", "UTF-8", is);
						}
					}
					catch (Exception e) {
						Log.e(TAG, "Resource loading failed", e);
					}
				}
				return null;
		}

		@Override
		public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
			// ここで true を返すとアプリ本体のクラッシュを防げる
			Log.e(TAG, "レンダプロセスがクラッシュしました。生存状況: " + detail.didCrash());

			// ユーザーに通知してアクティビティを閉じる
			Toast.makeText(mActivity, "レンダプロセスがクラッシュしました。", Toast.LENGTH_SHORT).show();

			return true; // アプリを終了させない
		}
	};

	// 横書き置換ロジックを共通メソッド化
	private String applyHorizontalStyles(String html) {
		// 既存の縦書きCSSプロパティを横書きに置換
		html = html.replaceAll("writing-mode:\\s*vertical-rl", "writing-mode: horizontal-tb");
		html = html.replaceAll("-webkit-writing-mode:\\s*vertical-rl", "-webkit-writing-mode: horizontal-tb");
		html = html.replaceAll("-epub-writing-mode:\\s*vertical-rl", "-epub-writing-mode: horizontal-tb");
		html = html.replaceAll("direction:\\s*rtl", "direction: ltr");
		// 強制的に上書きするためのスタイルシートをインジェクション
		String forceCss = "<style type=\"text/css\">\n" +
			"html, body, p, div, span, asm, section {\n" +
			"  writing-mode: horizontal-tb !important;\n" +
			"  -webkit-writing-mode: horizontal-tb !important;\n" +
			"  -epub-writing-mode: horizontal-tb !important;\n" +
			"  direction: ltr !important;\n" +
			"}\n" +
			"</style>\n" +
			"</head>";
		if (html.contains("</head>")) {
			html = html.replace("</head>", forceCss);
		}
		else if (html.contains("<body>")) {
			html = html.replace("<body>", "<body>" + forceCss.replace("</head>", ""));
		}
		return html;
	}

	private String convertStreamToString(InputStream is) throws Exception {
		java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		reader.close();
		return sb.toString();
	}

	// 検索が完了したら呼び出される
	private final WebView.FindListener commonFindListener = (activeMatchOrdinal, numberOfMatches, isDoneCounting) -> {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// 検索結果の共通処理
		if (isDoneCounting) {
			// 検索全体が完了した時の処理
			mNowFindTotal = numberOfMatches;
			if (mSearchInitSet) {
				mSearchInitSet = false;
				// レイアウトが変更されると検索位置が先頭に戻るためクリアする
				mNowFindCount = 0;
			}
			if (mNowPrevSet) {
				mNowPrevSet = false;
				mNowFindCount = mNowFindTotal - 1;
				Logcat.v(logLevel, "末尾に移動させる");
			}
			int searchmax = (isAozora) ? (mNowFindTotal > 0) ? 1 : 0 : SearchList.size();
			TouchPanelView.SetSearchWordIndex(mSearchIndex + 1, mNowFindCount + 1, mNowFindTotal, searchmax, mNowFindTotal);
			if (numberOfMatches > 0) {
				Logcat.v(logLevel, "numberOfMatches=" + numberOfMatches);
				// ヒットした場所へのスクロール同期を開始
				Logcat.v(logLevel, "ヒットした場所へのスクロール同期を開始");
				startScrollStopMonitoring();
			}
			else {
				// ヒットしなかった
				mFindWord = false;
				if (mLoadingSpinner != null) {
					mLoadingSpinner.setVisibility(leftWebView.GONE);
				}
				runOnUiThread(() -> {
					Logcat.v(logLevel, "UIスレッドでスクロール同期を実行");
					syncWebViewScroll(); 
				});
			}
		}
	};

	// WebViewの設定を行う
	private void applyCommonSettings(WebView webView) {
		webView.setOnTouchListener(commonTouchListener);
		webView.setWebChromeClient(commonChromeClient);
		webView.setWebViewClient(commonWebViewClient);
		webView.setFindListener(commonFindListener);
	}

	// WebViewの生成と基本設定を共通化
	private WebView createWebView() {
		WebView webView = new WebView(this);
		applyCommonSettings(webView);

		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDomStorageEnabled(true);
		settings.setAllowFileAccess(true);
		// 勝手に音声を流さないようにする
		settings.setMediaPlaybackRequiresUserGesture(true);
		settings.setTextZoom(mTextSize);
		webView.setInitialScale(mScaleFix);
		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);
		webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		// ロングクリックを無効
		webView.setLongClickable(false);
		// クリックも無効(Gesture側で判定するため)
		webView.setClickable(false);
		webView.setBackgroundColor(colorInt);
		// ハードウェア描画にする(これを入れないと遅くなる)
		webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		if (mNoCache) {
			// キャッシュを使用しない
			settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
			settings.setDomStorageEnabled(false);
		}
		else {
			// キャッシュモードを「有効(基本はキャッシュ優先)」にする
			settings.setCacheMode(WebSettings.LOAD_DEFAULT);
			// データベースキャッシュを有効化
			settings.setDomStorageEnabled(true);
		}
		initScrollListener(webView);
		return webView;
	}

	// WebViewの動作が停止したら呼び出される
	private void onAnyWebViewStopped(WebView view) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (!mBusyRotate && !mInitSet) {
			Logcat.v(logLevel, "calcRatio()");
			// スクロール移動が停止したら現在座標の比率を計算
			calcRatio();
			// ページを取得して現在のページを保存
			selectpage = GetNowPage();
		}
		if (view == leftWebView) {
			Logcat.v(logLevel, "leftWebView が止まりました");
		}
		else {
			Logcat.v(logLevel, "rightWebView が止まりました");
		}
		// 停止したらフッターを更新
		PutFooterView();
	}

	// セットアップ用のメソッド
	private void initScrollListener(WebView webView) {
		final Handler handler = new Handler(Looper.getMainLooper());

		// 各WebViewごとに個別のRunnableを作成
		final Runnable stopCheck = () -> onAnyWebViewStopped(webView);

		webView.setOnScrollChangeListener((v, x, y, oldX, oldY) -> {
			handler.removeCallbacks(stopCheck);
			handler.postDelayed(stopCheck, 100); // 100ms動きがなければ停止とみなす
		});
	}

	private void CheckImageOnly(WebView view) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "CheckImageOnly()");
		// JavaScriptを実行して判定
		String js = 
			"(function() { " +
			"  var body = document.body; " +
			"  if (!body) return 'false'; " +
			// innerTextをtextContentに変更し正規表現を最小限にする
			"  var text = body.textContent.trim(); " + 
			"  var imgs = document.getElementsByTagName('img'); " +
			// 画像1枚かつテキスト(空白除く)が空か判定
			"  return (text.length === 0 && imgs.length === 1); " +
			"})();";
		view.evaluateJavascript(js, value -> {
			try {
				Logcat.v(logLevel, "value=" + value);
				if (value == null || value.equals("null")) {
					mImageOnly = false;
					return;
				}
				String[] parts = value.replace("\"", "").split(",");
				mImageOnly = Boolean.parseBoolean(parts[0]);
				if (mImageOnly) {
					// 判定：画像のみ
					// 例1(alt属性に文字はあるが画面には画像しか出ていない)はこちらに該当
					Logcat.v(logLevel, "このページは画像のみです");
				}
				else {
					// 判定：テキストあり
					// 例2(目次リンクや数字がある)はこちらに該当
					Logcat.v(logLevel, "テキストが含まれています");
				}
			}
			catch (Exception e) { 
				Log.e(TAG, "JS Error", e);
			}
		});
	}

	private void SetBackGroundColor(WebView view, int targetColor, boolean mode) {
		String colorCode = String.format("#%06x", (targetColor & 0x00ffffff));
		String javascript = "javascript:(function() {" +
				"var style = document.createElement('style');" +
				"style.innerHTML = 'body, html { background-color: " + colorCode + "!important; }';" +
				"document.head.appendChild(style);" +
				"})()";
		if (mode) {
			view.evaluateJavascript(javascript, null);
		}
	}

	private void SetMarginCut(WebView view) {
		// viewport meta タグを動的に追加
		String js = "var meta = document.createElement('meta'); " +
			"meta.name = 'viewport'; " +
			"meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'; " +
			"document.getElementsByTagName('head')[0].appendChild(meta);";
		view.evaluateJavascript(js, null);

	}

	private void SetTextColor(WebView view, int targetColor, boolean mode) {
		String colorCode = String.format("#%06x", (targetColor & 0x00ffffff));
		String script;

		if (mode) {
			script = "(function() {" +
				"  var elements = document.getElementsByTagName('*');" +
				"  for (var i = 0; i < elements.length; i++) {" +
				"      elements[i].style.color = '" + colorCode + "';" +
				"  }" +
				"})();";
		}
		else {
			script = "(function() {" +
				"  document.body.style.color = '" + colorCode + "';" +
				"  var all = document.getElementsByTagName('*');" +
				"  for (var i = 0; i < all.length; i++) {" +
				"    var color = window.getComputedStyle(all[i]).color;" +
				// 黒(rgb(0,0,0))に設定されているものだけを書き換える
				"    if (color === 'rgb(0, 0, 0)') {" +
				"      all[i].style.color = '" + bodyColor + "';" +
				"    }" +
				"  }" +
				"})();";
		}

		view.evaluateJavascript(script, null);

	}

	private void GetLeftWidthSpecial(WebView view, int delay) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		String js = "(function(){ " +
			"  var b=document.body, e=document.documentElement; " +
			" const style = window.getComputedStyle(document.body);"+
			" const fontSize = parseFloat(style.fontSize);"+
			" const lineHeight = parseFloat(style.lineHeight) || fontSize * 1.5; "+
			"  return Math.max(b.scrollWidth, e.scrollWidth) + ',' + Math.max(b.scrollHeight, e.scrollHeight) + ',' + lineHeight; " +
			"})()";
		view.postDelayed(() -> {
			// UIスレッドの空きを待ってから描画の直前を狙う
			view.postOnAnimation(new Runnable() {
				@Override
				public void run() {
					view.postDelayed(() -> {
						// UIスレッドの空きを待ってから描画の直前を狙う
						view.postOnAnimation(new Runnable() {
							public void run() {
								view.evaluateJavascript(js, value -> {
									try {
										if (value == null || value.equals("null")) return;
										String[] parts = value.replace("\"", "").split(",");
										float d = mScaleDensity;
										float contentWidth = Float.parseFloat(parts[0]) * d;
										float contentHeight = Float.parseFloat(parts[1]) * d;
										float lineHeight = Float.parseFloat(parts[2]);
										double safeLineCountX = Math.floor(view.getWidth() / lineHeight) -1;
										double safeLineCountY = Math.floor(view.getHeight() / lineHeight) -1;
										scrollXStep = (int)((int)(safeLineCountX) * lineHeight);

										scrollYStep = (int)((int)(safeLineCountY) * lineHeight);
										maxScrollX = Math.max(0, (int)contentWidth - view.getWidth());
										maxScrollY = Math.max(0, (int)contentHeight - view.getHeight());
										currentScrolllX = (int)((float)maxScrollX * ratiox);
										currentScrolllY = (int)((float)maxScrollY * ratioy);
										if (!mDoubleMode) {
											leftWebView.scrollTo(currentScrolllX, currentScrolllY);
										}
										else if (mUpDownMode) {
											leftWebView.scrollTo(currentScrolllX - scrollXStep, currentScrolllY);
											if (mDoubleMode) rightWebView.scrollTo(currentScrolllX, currentScrolllY + scrollYStep);
										}
										else {
											leftWebView.scrollTo(currentScrolllX, currentScrolllY);
											if (mDoubleMode) rightWebView.scrollTo(currentScrolllX - scrollXStep, currentScrolllY + scrollYStep);
										}
										Logcat.v(logLevel, "currentScrolllX=" + currentScrolllX + ", currentScrolllY=" + currentScrolllY + ", maxScrollX=" + maxScrollX + ", maxScrollY=" + maxScrollY);
									}
									catch (Exception e) { 
										Log.e(TAG, "JS Error", e);
									}
								});
							}
						});
					}, delay);
				}
			});
		}, delay);
	}

	private void GetLeftWidth(WebView view, boolean isVirtual, boolean set) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		String js = "(function(){ " +
			"  var b=document.body, e=document.documentElement; " +
			" const style = window.getComputedStyle(document.body);"+
			" const fontSize = parseFloat(style.fontSize);"+
			" const lineHeight = parseFloat(style.lineHeight) || fontSize * 1.5; "+
			"  return Math.max(b.scrollWidth, e.scrollWidth) + ',' + Math.max(b.scrollHeight, e.scrollHeight) + ',' + lineHeight; " +
			"})()";
		// ページめくり時は200ミリ秒にする
		int delay = (set) ? 500 : 200;
		// スマホの処理能力に応じて描画を完了させるため二段階でチェックする
		if (view != null) {
			view.postDelayed(() -> {
				// UIスレッドの空きを待ってから描画の直前を狙う
				view.postOnAnimation(new Runnable() {
					@Override
					public void run() {
						view.postDelayed(() -> {
							// UIスレッドの空きを待ってから描画の直前を狙う
							view.postOnAnimation(new Runnable() {
								public void run() {
									view.evaluateJavascript(js, value -> {
										try {
											if (value == null || value.equals("null")) return;
											String[] parts = value.replace("\"", "").split(",");
											float d = mScaleDensity;
											float contentWidth = Float.parseFloat(parts[0]) * d;
											float contentHeight = Float.parseFloat(parts[1]) * d;
											float lineHeight = Float.parseFloat(parts[2]);
											double safeLineCountX = Math.floor(view.getWidth() / lineHeight) -1;
											double safeLineCountY = Math.floor(view.getHeight() / lineHeight) -1;
											scrollXStep = (int)((int)(safeLineCountX) * lineHeight);

											scrollYStep = (int)((int)(safeLineCountY) * lineHeight);
											if (set) {
												maxScrollX = Math.max(0, (int)contentWidth - view.getWidth());
												maxScrollY = Math.max(0, (int)contentHeight - view.getHeight());
												// 回転したら再計算してスクロール移動
												currentScrolllX = (int)((float)maxScrollX * ratiox);
												currentScrolllY = (int)((float)maxScrollY * ratioy);
												if (mInitSet) {
													// 初期化中にスクロール位置を取得する
													mBackupCurrentScrolllX = currentScrolllX;
													mBackupCurrentScrolllY = currentScrolllY;
												}
												if (!mDoubleMode) {
													leftWebView.scrollTo(currentScrolllX, currentScrolllY);
												}
												else if (mUpDownMode) {
													leftWebView.scrollTo(currentScrolllX - scrollXStep, currentScrolllY);
													if (mDoubleMode) rightWebView.scrollTo(currentScrolllX, currentScrolllY + scrollYStep);
												}
												else {
													leftWebView.scrollTo(currentScrolllX, currentScrolllY);
													if (mDoubleMode) rightWebView.scrollTo(currentScrolllX - scrollXStep, currentScrolllY + scrollYStep);
												}
												Logcat.v(logLevel, "currentScrolllX=" + currentScrolllX + ", currentScrolllY=" + currentScrolllY + ", maxScrollX=" + maxScrollX + ", maxScrollY=" + maxScrollY);
												mBusyRotate = false;
											}
											Logcat.v(logLevel, "scrollXStep=" + scrollXStep + ", scrollYStep=" + scrollYStep + ", contentWidth=" + contentWidth + ", contentHeight=" + contentHeight);
											// ここで表示を消す
											if (mLoadingSpinner != null) {
												mLoadingSpinner.setVisibility(view.GONE);
											}
										}
										catch (Exception e) { 
											Log.e(TAG, "JS Error", e);
										}
									});
								}
							});
						}, delay);
					}
				});
			}, delay);
		}
	}
	// スクロールのレンジを計算
	private void calculateScrollRange(WebView view) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "calculateScrollRange()");
		int viewW = view.getWidth();
		int viewH = view.getHeight();

		WebSettings settings = view.getSettings();
		// JSは「一番下まで行け」という命令だけに使う
		if (meta.isImageOnly || mImageOnly) {
			maxScrollX = 0;
			maxScrollY = 0;
			mPageMoveEnable = false;
			if (view == leftWebView) {
				currentScrolllX = 0;
				currentScrolllY = 0;
			}
			savePageCount(currentSpineIndex, 1);
			Logcat.v(logLevel, "applyImageOnlyStyle()");
			applyImageOnlyStyle(view);
			if (mAnchorl && view == leftWebView) {
				mAnchorl = false;
			}
			else if (mDoubleMode && view == leftWebView) {
				Logcat.v(logLevel, "renderSpine rightWebView");
				renderSpine(currentSpineIndex, rightWebView);
			}
			view.postDelayed(() -> {
				syncWebViewScroll();
				if (mLoadingSpinner != null) {
					mLoadingSpinner.setVisibility(view.GONE);
				}
				// WebViewを表示(フェードが効かないのでAlphaは一気に1へ)
				if (mDoubleMode && (view == rightWebView && !isJapaneseMode || view == leftWebView && isJapaneseMode)) {
				}
				else {
					view.setAlpha(1f);
					mRenderBusy = false;
					PutFooterView();
				}
			}, 200); // レンダリング安定のための待機
		}
		else if (isJapaneseMode) {
			if (mAnchorl && view == leftWebView || mAnchorr && view == rightWebView) {
				Logcat.v(logLevel, "アンカーの距離を求める");

				view.evaluateJavascript(
					"(function getOffsetOfAnchor() {" +
					"    var element = document.getElementById('" + mAnchorStr + "');" +
					"    if (!element) return 0;" +
					// 要素の左側の座標
					"    var elementLeft = element.getBoundingClientRect().left + window.pageXOffset;" +

					"    return elementLeft;" +
					"})();",
					value3 -> {
						Logcat.v(logLevel, "アンカーまでの距離=" + value3);
						try {
							if (view == leftWebView) {
								mAnchorScrolll[0] = (int)(Double.parseDouble(value3));
							}
						}
						catch (Exception e) {
							Log.e(TAG, "", e);
						}
					}
				);
			}

			// scrollWidthを正しく取得するためJS側で一時的にoverflowを解除して計測
			String js = "(function(){ " +
					"  var b=document.body, e=document.documentElement; " +
					" const style = window.getComputedStyle(document.body);"+
					" const fontSize = parseFloat(style.fontSize);"+
					" const lineHeight = parseFloat(style.lineHeight) || fontSize * 1.5; "+
				"  var text = b.textContent || '';" +
					"  return Math.max(b.scrollWidth, e.scrollWidth) + ',' + Math.max(b.scrollHeight, e.scrollHeight) + ',' + lineHeight + ',' + text.length; " +
					"})()";
			view.evaluateJavascript(js, value -> {
				try {
					if (value == null || value.equals("null")) return;
					String[] parts = value.replace("\"", "").split(",");
					float d = mScaleDensity;
					int contentW = (int)(Float.parseFloat(parts[0]) * d);
					int contentH = (int)(Float.parseFloat(parts[1]) * d);
					float lineHeight = Float.parseFloat(parts[2]);
					int textLength = Integer.parseInt(parts[3]);

					double safeLineCount = Math.floor(viewW / lineHeight) - 1;
					scrollXStep = (int)(safeLineCount * lineHeight);
					// 縦書きモード：横スクロールを計算して縦はロック
					maxScrollX = Math.max(0, contentW - viewW);
					maxScrollY = Math.max(0, contentH - viewH);

					Logcat.v(logLevel, "contentH=" + contentH + ", viewH=" + viewH + ", contentW=" + contentW + ", viewW=" + viewW + ", lineHeight=" + lineHeight + ", scrollXStep=" + scrollXStep);
					if (contentH > viewH * 1.01f && contentW < viewW * 1.01f) {
						Logcat.v(logLevel, "縦長モード");
						maxScrollY = contentH - viewH;
						// 縦長モードならむしろ横スクロールを0にして誤動作を防ぐ
						maxScrollX = 0;
						// 縦ページ移動を可能にする
						mPageMoveEnable = true;
						applyNormalStyleHeight(view);
					}
					else {
						Logcat.v(logLevel, "テキストモード");
						// それ以外(通常のテキストページなど)は
						// 縦スクロールを物理的に 0 に封印する
						maxScrollY = 0;
						mPageMoveEnable = false;
					}
					Logcat.v(logLevel, "calcCurrentPageMax()=" + calcCurrentPageMax(contentW ,scrollXStep, true) + ", currentSpineIndex=" + currentSpineIndex);
					
					savePageCount(currentSpineIndex, calcCurrentPageMax(contentW ,scrollXStep, true));
					
					if (view == leftWebView) {
						if (pendingPos == 0) {
							// 次の章へ(通常)：右端から開始
							currentScrolllX = maxScrollX - scrollXStep * mScrollOffset;
							if (currentScrolllX < 0) {
								currentScrolllX = 0;
							}
							// オフセット加算したらクリア
							mScrollOffset = 0;
							currentScrolllY = 0;
						}
						else {
							// 前の章へ戻った時：左端(文末)から開始
							currentScrolllX = (mDoubleMode) ? scrollXStep : 0;
							// 縦長なら下端
							currentScrolllY = maxScrollY; 
						}
						// 適用後にリセット
						pendingPos = 0;
						Logcat.v(logLevel, "Calculated MaxScrollX: " + maxScrollX);
					}

					if (mAnchorl && view == leftWebView) {
						int offsetx = (int)((float)mAnchorScrolll[0] * d);
						currentScrolllX = maxScrollX + offsetx;
						if (offsetx < -(viewW / 2)) currentScrolllX -= viewW / 2;
						Logcat.v(logLevel, "currentScrolllX=" + currentScrolllX);
					}

					view.postDelayed(() -> {
						if (mAnchorl && view == leftWebView) {
							mAnchorl = false;

							if (mDoubleMode) {
								mAnchorr = true;
								Logcat.v(logLevel, "右側のWebViewのアンカーを起動");
								jumpToAnchor(rightWebView, targetItem);
							}
						}
						else if (mDoubleMode && view == leftWebView) {
							Logcat.v(logLevel, "renderSpine rightWebView");
							view.postDelayed(() -> {
								if (isAozora) {
									rightWebView.loadUrl(fileUrl);
								}
								else {
									renderSpine(currentSpineIndex, rightWebView);
								}
							}, 200); // レンダリング安定のための待機
						}
						if (mAnchorr && view == rightWebView) {
							mAnchorr = false;
						}

						if (searchKeyword != null && !searchKeyword.isEmpty()) {
							// WebView標準の検索機能(API 16以上)
							// これだけで「ハイライト」と「スクロール」をOSがやってくれる
							if (view == leftWebView) leftWebView.findAllAsync(searchKeyword);
							if (view == rightWebView) rightWebView.findAllAsync(searchKeyword);

							if (mNowPrevSetl && view == leftWebView) {
								mNowPrevSetl = false;
								leftWebView.findNext(false);
							}
							if (mNowPrevSetr && view == rightWebView) {
								mNowPrevSetr = false;
								rightWebView.findNext(false);
							}
						}
						else {
							// ここで座標を補正
							currentScrolllX = Math.max(0, Math.min(currentScrolllX + 0, maxScrollX));
							currentScrolllY = Math.max(0, Math.min(currentScrolllY + 0, maxScrollY));
							syncWebViewScroll();
							if (mLoadingSpinner != null) {
								mLoadingSpinner.setVisibility(view.GONE);
							}
						}
						// WebViewを表示(フェードが効かないのでAlphaは一気に1へ)
						view.setAlpha(1f);
						Logcat.v(logLevel, "転送完了");
						mRenderBusy = false;
						PutFooterView();
					}, 200); // レンダリング安定のための待機
				}
				catch (Exception e) { 
					Log.e(TAG, "JS Error", e);
				}
			});
		}
		else {
			String js = "(function(){ " +
					" const style = window.getComputedStyle(document.body);"+
					" const fontSize = parseFloat(style.fontSize);"+
					" const lineHeight = parseFloat(style.lineHeight) || fontSize * 1.5; "+
					"  return lineHeight; " +
					"})()";
			view.evaluateJavascript(js, value -> {
				try {
					if (value == null || value.equals("null")) return;
					String[] parts = value.replace("\"", "").split(",");
					float lineHeight = Float.parseFloat(parts[0]);
					double safeLineCount = Math.floor(viewH / lineHeight) - 1;
					scrollYStep = (int)(safeLineCount * lineHeight);
				}
				catch (Exception e) { 
					Log.e(TAG, "JS Error", e);
				}
			});
			if (mAnchorl && view == leftWebView || mAnchorr && view == rightWebView) {
				Logcat.v(logLevel, "アンカーの距離を求める");
				view.evaluateJavascript(
					"(function() { " +
					"  var el = document.getElementById('" + mAnchorStr + "'); " +
					"  return el ? el.getBoundingClientRect().top + window.pageYOffset : 0; " +
					"})();", 
					new ValueCallback<String>() {
						@Override
						public void onReceiveValue(String value) {
							// value に距離(px)が文字列として返ってくる
							Logcat.v(logLevel, "Distance from top: " + value);
							try {
								if (view == leftWebView) {
									mAnchorScrolll[0] = (int)(Double.parseDouble(value));
									Logcat.v(logLevel, "currentScrollY=" + mAnchorScrolll[0]);
								}
							}
							catch (Exception e) {
								Log.e(TAG, "", e);
							}
						}
					}
				);
			}
			// 少し待ってからWebView自身の「コンテンツ高さ」を直接取得する
			view.postDelayed(() -> {
				String wjs = "(function(){ " +
					"  var b=document.body, e=document.documentElement; " +
					"  var text = b.textContent || '';" +
					"  return Math.max(b.scrollWidth, e.scrollWidth) + ',' + Math.max(b.scrollHeight, e.scrollHeight) + ',' + text.length; " +
					"})()";
				view.evaluateJavascript(wjs, value -> {
					try {
						if (value == null || value.equals("null")) {
							Log.e(TAG, "value Error");
							return;
						}
						String[] parts = value.replace("\"", "").split(",");
						int contentWidth = (int)(Float.parseFloat(parts[0]) * view.getScale());
						int contentHeight = (int)(Float.parseFloat(parts[1]) * view.getScale());
						int textLength = Integer.parseInt(parts[2]);
						// 縦書きモード(横スクロールがメイン)
						int viewHeight = view.getHeight();
						int viewWidth = view.getWidth();
						// 実際にスクロール可能な正味の量を Android 側で算出
						int calculatedMaxY = Math.max(0, contentHeight - viewHeight);
						int calculatedMaxX = Math.max(0, contentWidth - viewWidth);
						// もし JS で最下部まで行った時のスクロール位置がもっと大きいならそっちを採用
						int actualY = view.getScrollY();
						int actualX = view.getScrollX();
						Logcat.v(logLevel, "calcCurrentPageMax()=" + calcCurrentPageMax(contentHeight ,scrollYStep, true) + ", currentSpineIndex=" + currentSpineIndex);
						savePageCount(currentSpineIndex, calcCurrentPageMax(contentHeight ,scrollYStep, true));

						int scrollstep = (mDoubleMode) ? scrollYStep : 0;
						maxScrollX = Math.max(calculatedMaxX, actualX);
						maxScrollY = Math.max(calculatedMaxY - scrollstep, actualY - scrollstep);
						Logcat.v(logLevel, "contentHeight=" + contentHeight + ", viewH=" + viewH + ", contentWidth=" + contentWidth + ", viewW=" + viewW);
						Logcat.v(logLevel, "maxScrollX=" + maxScrollX + ", maxScrollY=" + maxScrollY);

						if (contentHeight < viewHeight * 1.01f && contentWidth > viewWidth * 1.01f && mScalingOn) {
							Logcat.v(logLevel, "横長モード(スケーリング)");
							maxScrollX = contentWidth - viewWidth;
							maxScrollY = 0;
							// 横ページ移動を可能にする
							mScrollxEnable = true;
							mPageMoveEnable = false;
							view.setInitialScale(0);
							applyNormalStyleWidth(view);
						}
						else {
							Logcat.v(logLevel, "テキストモード");
							// それ以外(通常のテキストページなど)は
							// 横スクロールを物理的に 0 に封印する
							maxScrollX = 0;
							mScrollxEnable = false;
							mPageMoveEnable = (maxScrollY > 5) ? true : false;
							Logcat.v(logLevel, "mPageMoveEnable=" + mPageMoveEnable);
						}

						if (view == leftWebView) {
							if (pendingPos == 1) {
								Logcat.v(logLevel, "座標を末尾にセット");
								currentScrolllY = maxScrollY;
								currentScrolllX = maxScrollX;
							}
							else {
								Logcat.v(logLevel, "座標を先頭にセット");
								currentScrolllY = 0 + scrollYStep * mScrollOffset;
								if (currentScrolllY > maxScrollY) {
									currentScrolllY = maxScrollY;
								}
								currentScrolllX = 0;
								// オフセット加算したらクリア
								if (mScrollOffset > 0) {
								 Logcat.v(logLevel, "オフセット加算したらクリア");
								}
								mScrollOffset = 0;
								Logcat.v(logLevel, "scrollYStep=" + scrollYStep + ", currentScrolllY=" + currentScrolllY + ", mScrollOffset=" + mScrollOffset);
							}
							pendingPos = 0;
						}
						float d = mScaleDensity;
						if (mAnchorl && view == leftWebView) {
							currentScrolllY = (int)((float)mAnchorScrolll[0] * d);
							Logcat.v(logLevel, "アンカーの座標をセット=" + currentScrolllY);
						}
						view.postDelayed(() -> {
							if (mAnchorl && view == leftWebView) {
								mAnchorl = false;
								if (mDoubleMode) {
									mAnchorr = true;
									Logcat.v(logLevel, "右側のWebViewのアンカーを起動");
									jumpToAnchor(rightWebView, targetItem);
								}
							}
							else if (mDoubleMode && view == leftWebView) {
								view.postDelayed(() -> {
									if (isAozora) {
										rightWebView.loadUrl(fileUrl);
									}
									else {
										renderSpine(currentSpineIndex, rightWebView);
									}
								}, 200); // レンダリング安定のための待機
							}
							if (mAnchorr && view == rightWebView) {
								mAnchorr = false;
							}

							if (searchKeyword != null && !searchKeyword.isEmpty()) {
								// WebView標準の検索機能(API 16以上)
								// これだけで「ハイライト」と「スクロール」をOSがやってくれる
								if (view == leftWebView) leftWebView.findAllAsync(searchKeyword);
								if (view == rightWebView) rightWebView.findAllAsync(searchKeyword);
								if (mNowPrevSetl && view == leftWebView) {
									mNowPrevSetl = false;
									leftWebView.findNext(false);
								}
								if (mNowPrevSetr && view == rightWebView) {
									mNowPrevSetr = false;
									rightWebView.findNext(false);
								}
							}
							else {
								// ここで座標を補正
								currentScrolllX = Math.max(0, Math.min(currentScrolllX + 0, maxScrollX));
								currentScrolllY = Math.max(0, Math.min(currentScrolllY + 0, maxScrollY));
								syncWebViewScroll();
								if (mLoadingSpinner != null) {
									mLoadingSpinner.setVisibility(view.GONE);
								}
							}

							// WebViewを表示(フェードが効かないのでAlphaは一気に1へ)
							if (view == rightWebView && contentHeight <= viewHeight) {
								// 縦のサイズが１ページ以内になる場合
							}
							else {
								view.setAlpha(1f);
							}
							Logcat.v(logLevel, "転送完了");
							mRenderBusy = false;
							PutFooterView();

							if (view == leftWebView) {
								if (viewHeight > 0) {
									currentOffset = currentScrolllY / viewHeight;
								}
								Logcat.v(logLevel, "currentOffset=" + currentOffset);
							}
						}, 200); // レンダリング安定のための待機
					}
					catch (Exception e) { 
						Log.e(TAG, "JS Error", e);
					}
				});
			}, 200); // レンダリング安定のための待機
		}
	}

	private void SearchWebWord(WebView view, String Word, boolean virtical) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// ページ内の特定のワード(例："Chapter")の座標をすべて取得するJSを実行
		String vJs = 
			"(function() {" +
			"  var results = [];" +
			"  var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);" +
			"  var node;" +
			"  while(node = walker.nextNode()) {" +
			"    if(node.textContent.includes('" + Word + "')) {" +
			"      var range = document.createRange();" +
			"      range.selectNodeContents(node);" +
			"      var rect = range.getBoundingClientRect();" +
			"      results.push(rect.left + window.scrollX);" +
			"    }" +
			"  }" +
			"  return results;" +
			"})()";
		String hJs = 
			"(function() {" +
			"  var results = [];" +
			"  var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);" +
			"  var node;" +
			"  while(node = walker.nextNode()) {" +
			"    if(node.textContent.includes('" + Word + "')) {" +
			"      var range = document.createRange();" +
			"      range.selectNodeContents(node);" +
			"      var rect = range.getBoundingClientRect();" +
			"      results.push(rect.top + window.scrollY);" +
			"    }" +
			"  }" +
			"  return results;" +
			"})()";
		view.evaluateJavascript((virtical) ? vJs : hJs, value -> {
				// value には [120, 1500, 3200] のように上からの座標(px)が返ってくる
				// これを WebViewの高さで割ればページ番号の一覧ができる
				Logcat.v(logLevel, "座標一覧: " + value);
			}
		);
	}

	private int calcCurrentPageMax(int height, int step, boolean mode) {
		int total = 0;
		int calcstep = (mDoubleMode && mode) ? step * 2 : step;
		if (calcstep == 0) calcstep = height;
		return (int)Math.ceil(height / calcstep);
	}

	private int calcCurrentPageNow(int height, int step, boolean topbottom, boolean leftright) {
		int calcstep = (mDoubleMode && leftright) ? step * 2 : step;
		if (calcstep == 0) calcstep = height;
		return (int)Math.ceil(height / calcstep);
	}

	private void applyReflowStyle(WebView view) {
		String reflowJs = "(function() {" +
			"  var meta = document.querySelector('meta[name=\"viewport\"]') || document.createElement('meta');" +
			"  meta.name = 'viewport';" +
			"  meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';" +
			"  document.head.appendChild(meta);" +

			"  var style = document.getElementById('reflow-style') || document.createElement('style');" +
			"  style.id = 'reflow-style';" +
			"  style.innerHTML = '" +

			"    html, body { " +
			"        height: auto !important; " +
			"        min-height: 100% !important; " +
			"        overflow-y: visible !important; " + // スクロールを許可
			"        position: static !important; " +
			"    } " +

			"    * { " + // 全ての要素の絶対配置を解除
			"        position: static !important; " +
			"        height: auto !important; " +
			"        max-height: none !important; " +
			"    } " +
			"  ';" +
			"  document.head.appendChild(style);" +
			"})();";
		view.evaluateJavascript(reflowJs, null);
	}

	private void applyImageOnlyStyle(WebView view) {
		String cssColor = String.format("#%06x", (colorInt & 0x00ffffff));

		String imgOnlyJs = "(function() {" +
			"  var style = document.getElementById('custom-style') || document.createElement('style');" +
			"  style.id = 'custom-style';" +
			// 位置指定クラスの判定
			"  var mainEl = document.querySelector('.main');" +
			"  var isStart = mainEl && mainEl.classList.contains('align-start');" +
			"  var isEnd = mainEl && mainEl.classList.contains('align-end');" +
			// 寄せ方向の決定(指定がなければ center)
			"  var justifyValue = 'center';" +
			"  if (isStart) justifyValue = 'flex-start';" +
			"  else if (isEnd) justifyValue = 'flex-end';" +
			"  var cssText = '';" +
			// 全体構造：常に flex を使用しjustify-content で左右位置を制御
			"  cssText += 'html, body { ' + " +
			"    'margin: 0 !important; padding: 0 !important; ' + " +
			"    'width: 100% !important; height: 100% !important; ' + " +
			"    'background-color: " + cssColor + " !important; ' + " +
			"    'overflow: hidden !important; ' + " +
			"    'display: flex !important; ' + " +
			"    'align-items: center !important; ' + " + // 上下は常に中央
			"    'justify-content: ' + justifyValue + ' !important; ' + " + // ここで左右を切り替え
			"  '} ';" +
			// 画像設定：以前の「全画面・アスペクト比維持」設定を完全維持
			"  cssText += 'img { ' + " +
			"    'position: static !important; ' + " +
			"    'width: 100% !important; ' + " +
			"    'height: 100% !important; ' + " +
			"    'object-fit: contain !important; ' + " +
			"    'max-width: none !important; ' + " +
			"    'max-height: none !important; ' + " +
			"    'background-color: transparent !important; ' + " +
			"  '} ';" +
			// 干渉する可能性のある .main の高さを解除
			"  cssText += '.main { display: contents !important; } ';" +
			"  style.innerHTML = cssText;" +
			"  document.head.appendChild(style);" +
			"})();";

		view.evaluateJavascript(imgOnlyJs, null);
	}

	private void applyNormalStyleWidth(WebView view) {
		String normalJs = "(function() {" +
			"  var style = document.getElementById('custom-style') || document.createElement('style');" +
			"  style.id = 'custom-style';" +
			"  style.innerHTML = '" +
			"    html, body { " +
			"      margin: 0; padding: 0; " +
			"      height: 100vh !important; " +   // 高さを画面の100%に固定
			"      width: auto !important; " +     // 幅をコンテンツに合わせる
			"      overflow-y: hidden !important; " + // 縦スクロールを禁止
			"      overflow-x: auto !important; " +   // 横スクロールを有効化
			"      display: inline-block; " +      // コンテンツ幅を維持するための工夫
			"      min-width: 100vw; " +           // 最低でも画面幅は確保
			"    } " +
			"    img { " +
			"      max-height: 100% !important; " + // 画像が縦に突き抜けないように制限
			"    } " +
			"  ';" +
			"  document.head.appendChild(style);" +
			"})();";
		view.evaluateJavascript(normalJs, null);
	}

	private void applyNormalStyleHeight(WebView view) {
		String normalJs = "(function() {" +
			"  var style = document.getElementById('custom-style') || document.createElement('style');" +
			"  style.id = 'custom-style';" +
			"  style.innerHTML = '" +
			"    html, body { " +
			"      margin: 0; padding: 0; " +
			"      width: 100% !important; " +
			"      overflow-x: hidden !important; " + // 横スクロールのみ禁止
			"      overflow-y: visible !important; " + // 縦はコンテンツに応じて広がる
			"      height: auto !important; " +
			"    } " +
			"    img { "+
			"	max-width: 100% !important;" +
			" } " +
			"  ';" +
			"  document.head.appendChild(style);" +
			"})();";
		view.evaluateJavascript(normalJs, null);
	}

	private void updateWebViewSettings(WebView view, boolean isFixed, boolean isVirtical) {

		WebSettings settings = view.getSettings();
		if (isFixed) {
			// 固定レイアウトの設定
			settings.setUseWideViewPort(true);
			settings.setLoadWithOverviewMode(true);
			view.setInitialScale(0); // 自動フィット
			// スクロールを禁止する場合
			view.setVerticalScrollBarEnabled(false);
			view.setHorizontalScrollBarEnabled(false);
		}
		else {
			// リフローの設定：デフォルトに戻す
			// ズーム待機(タップ遅延)を無効化
			settings.setSupportZoom(false);
			settings.setBuiltInZoomControls(false);
			// 画面幅(device-width)を基準にする
			settings.setUseWideViewPort(false);
			// 余計な縮小計算をさせない
			settings.setLoadWithOverviewMode(false);
			view.setInitialScale(mScaleFix);
			// スクロールを許可する
			if (isVirtical) {
				view.setVerticalScrollBarEnabled(false);
				view.setHorizontalScrollBarEnabled(true);
			}
			else {
				view.setVerticalScrollBarEnabled(true);
				view.setHorizontalScrollBarEnabled(false);
			}
		}
	}

	private PageMetadata fetchMetadata(Book book, int index, boolean mode) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		PageMetadata mMeta = null;
		lock.lock(); // ロック取得
		try {
			try {
				mMeta = new PageMetadata();
				// OPFファイル(本の設定書)を解析
				Logcat.v(logLevel, "OPFファイル(本の設定書)を解析 index=" + index + ", mode=" + mode);
				Resource opfResource = book.getOpfResource();
				if (opfResource != null) {
					String opfContent = new String(opfResource.getData(), "UTF-8");
					// 全体設定(デフォルト値)の判定
					mMeta.isVertical = opfContent.contains("page-progression-direction=\"rtl\"");
					boolean defaultIsFixed = opfContent.contains("pre-paginated");

					// 現在の章(index)のIDを取得
					SpineReference spineRef = book.getSpine().getSpineReferences().get(index);
					String itemId = spineRef.getResourceId();
					// 正規表現または簡易検索で現在のitemrefタグを特定する
					// 例: <itemref idref="i" properties="rendition:layout-reflowable" />
					// 文字列検索で安全を期すためidref="ID" の前後のpropertiesを抽出
					String searchPattern = "idref=\"" + itemId + "\"";
					int idIndex = opfContent.indexOf(searchPattern);
					if (idIndex != -1) {
						// そのタグの終わりの ">" までを探す
						int tagEnd = opfContent.indexOf(">", idIndex);
						// タグ全体を抜き出す
						String itemTag = opfContent.substring(opfContent.lastIndexOf("<", idIndex), tagEnd);

						if (itemTag.contains("rendition:layout-reflowable")) {
							mMeta.isFixed = false;
							mMeta.isReflowable = true;
						}
						else if (itemTag.contains("rendition:layout-pre-paginated")) {
							mMeta.isFixed = true;
							mMeta.isReflowable = false;
						}
						else {
							// 個別指定がない場合はデフォルト(metadata)に従う
							mMeta.isFixed = defaultIsFixed;
						}
					}
					else {
						mMeta.isFixed = defaultIsFixed;
					}
					Logcat.v(logLevel, "Index [" + index + "] idref=" + itemId + " meta.isFixed=" + mMeta.isFixed);
				}
				// リソースの「実態」判定
				Resource res = book.getSpine().getSpineReferences().get(index).getResource();
				currentResource = res;
				String mediaType = res.getMediaType().getName();
				mMeta.isScrollOff = false;
				if ("image/svg+xml".equals(mediaType)) {
					mMeta.isImageOnly = true;
					mMeta.isFixed = true;
					mTextLength = 1;
				}
				else if (mediaType.contains("xhtml") || mediaType.contains("html")) {
					String content = new String(res.getData(), "UTF-8");
					// タグを除去して純粋なテキストの長さを測る
					String textOnly = content.replaceAll("<[^>]*>", "").replaceAll("\\s+", "").trim();
					int textLength = textOnly.length();
					mTextLength = textLength + 1;

					Logcat.v(logLevel, "Index [" + index + "] textLength: " + textLength);

					// 画像がなくても文字数が極端に少なければ「扉ページ」としてFixedに倒す
					// 50文字以下ならほぼ確実にタイトルかカバー
					if (textLength < 50) { 
						Logcat.v(logLevel, "文字数が極端に少ないため Fixed に昇格");
						mMeta.isScrollOff = true;
					}
				}
				else if (mediaType.startsWith("image/")) {
					mMeta.isImageOnly = true;
					mMeta.isFixed = true;
					mMeta.isScrollOff = true;
					mTextLength = 1;
				}
				String lang = currentBook.getMetadata().getLanguage();
				// OPF解析
				boolean direction = isVerticalWriting(currentBook); 
				Logcat.v(logLevel, "lang=" + lang + ", direction=" + direction);
				mEnableHorizontalWriting = (lang != null && lang.toLowerCase().startsWith("ja") && direction);
				isJapaneseMode = mEnableHorizontalWriting;
				if (getSetHorizontalWriting()) isJapaneseMode = false;
			}
			catch (Exception e) {
				Log.e(TAG, "Error during fetchMetadata", e);
			}
		}
		finally {
			lock.unlock(); // 確実に解放
		}
		return mMeta;
	}

	private boolean getSetHorizontalWriting() {
		boolean flag;
		flag =  (option[0] == 1 && mHorizontalWriting) ? true : false;
		return flag;
	}

	private void setSetHorizontalWriting(boolean value) {
		option[0] = (value) ? 1 : 0;
	}

	public int getViewportWidth(Resource resource) {
		try {
			String html = new String(resource.getData(), "UTF-8");
			// <meta name="viewport" content="width=1366, ..."> を探す
			Pattern pattern = Pattern.compile("width\\s*=\\s*(\\d+)");
			Matcher matcher = pattern.matcher(html);
			if (matcher.find()) {
				return Integer.parseInt(matcher.group(1));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return -1; // 見つからない場合はリフローとして扱う
	}

	// 縦書きを判定するための正規表現
	private static final Pattern VERTICAL_PATTERN = Pattern.compile(
		"writing-mode\\s*:\\s*(vertical-rl|epub-vertical-rl|-webkit-writing-mode\\s*:\\s*vertical-rl)",
		Pattern.CASE_INSENSITIVE
	);

	private boolean isVerticalWriting(Book book) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// OPFの方向指定を確認
		if ("rtl".equalsIgnoreCase(getDirection(book))) return true;
		// CSSリソースのスキャン
		for (Resource resource : book.getResources().getAll()) {
			if (resource.getMediaType() != null && "text/css".equals(resource.getMediaType().getName())) {
				try (InputStream is = resource.getInputStream()) {
					// 先頭8KBだけを読み込む(これならSIGTRAPを防げる)
					byte[] buffer = new byte[8192];
					int bytesRead = is.read(buffer);
					if (bytesRead <= 0) continue;
					// 読み込んだ部分だけを文字列化
					String cssChunk = new String(buffer, 0, bytesRead, "UTF-8");
					// その中に対して正規表現を実行
					if (VERTICAL_PATTERN.matcher(cssChunk).find()) {
						Logcat.v(logLevel, "縦書き指定を発見: " + resource.getHref());
						return true; 
					}
				}
				catch (Exception e) {
					Log.e(TAG, "スキャン中にエラー(スキップします): " + resource.getHref(), e);
				}
			}
		}
		return false;
	}

	private void renderSpine(int index, WebView view) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (currentBook == null) return;
		mRenderBusy = true;
		index += mPageOffset;
		// オフセット加算した後はクリア
		mPageOffset = 0;
		if (index > currentBook.getSpine().size() - 1) index = currentBook.getSpine().size() - 1;
		Logcat.v(logLevel, "renderSpine() ページ解析開始 index=" + index + ", size=" + currentBook.getSpine().size());
		currentSpineIndex = index;
		meta = fetchMetadata(currentBook, index , true);
		Logcat.v(logLevel, "isVertical=" + meta.isVertical + ", isFixed=" + meta.isFixed + ", isImageOnly=" + meta.isImageOnly + ", shouldDisableScroll=" + meta.shouldDisableScroll());

		if (mCompleted) {
			mCompleted = false;
			Logcat.v(logLevel, "Completed");
			if (isJapaneseMode) {
				ratiox = 0.0f;
				ratioy = 0.0f;
			}
			else {
				ratiox = 0.0f;
				ratioy = 1.0f;
			}
		}
		else if (mFirstRead) {
			mFirstRead = false;
			Logcat.v(logLevel, "FirstRead");
			mPageOffset = 0;
			if (isJapaneseMode) {
				ratiox = 1.0f;
				ratioy = 0.0f;
			}
			else {
				ratiox = 0.0f;
				ratioy = 0.0f;
			}
		}

		Resource res = null;
		try {
			res = currentBook.getSpine().getSpineReferences().get(index).getResource();
		}
		catch (Exception e) {
			Toast.makeText(this, "読み込みに失敗しました", Toast.LENGTH_SHORT).show();
			mInternaerror = true;
			return;
		}
		byte[] data;
		String htmlContent = res.getHref();
		try {
			data = res.getData();
			htmlContent = new String(data, "UTF-8");
			Logcat.v(logLevel, "HTML Size: " + data.length + " bytes for " + res.getHref());
		}
		catch (Exception e) {
			Log.e(TAG, "Render Error", e);
		}
		try {
			updateWebViewSettings(view, meta.isFixed, meta.isVertical);
			view.clearMatches(); 
			if (mLoadingSpinner != null) {
				mLoadingSpinner.setVisibility(View.VISIBLE);
				mLoadingSpinner.bringToFront();
			}
			// 強制再描画
			parentLayout.setBackgroundColor(colorInt);
			parentLayout.invalidate();
			// SVG対応ロジックの追加
			String mediaType = res.getMediaType().getName();
			if ("image/svg+xml".equals(mediaType)) {
				Logcat.v(logLevel, "SVG detected. Wrapping in virtual HTML: " + res.getHref());
				view.setAlpha(0f);
				// SVGを画面いっぱいに表示するためのラッパーHTML
				String wrapperHtml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<html><head><style>" +
					"html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; display: flex; align-items: center; justify-content: center; background: transparent; }" +
					"embed, svg, object { width: 100%; height: 100%; object-fit: contain; }" +
					"</style></head><body>" +
					"<embed src=\"https://epub.local/" + res.getHref() + "\" type=\"image/svg+xml\" />" +
					"</body></html>";
				// BaseURLを指定することで内部の <embed src> が shouldInterceptRequest でフックされる
				view.addJavascriptInterface(new WebAppInterface(this), "AndroidHost");
				view.loadDataWithBaseURL("https://epub.local/", wrapperHtml, "text/html", "UTF-8", null);
			}
			else if (mediaType.startsWith("image/")) {
				Logcat.v(logLevel, "Direct Image detected: " + res.getHref());
				view.setAlpha(0f);
				// 画像を画面中央にアスペクト比を維持して収めるHTMLを生成
				String imageHtml = "<html><head><style>" +
					"  body { margin: 0; padding: 0; background: transparent; display: flex; " +
					"         justify-content: center; align-items: center; height: 100vh; overflow: hidden; } " +
					"  img { max-width: 100%; max-height: 100%; object-fit: contain; } " +
					"</style></head><body>" +
					"<img src=\"https://epub.local/" + res.getHref() + "\">" +
					"</body></html>";
				WebSettings settings = view.getSettings();
				// 設定を画像用に最適化
				settings.setUseWideViewPort(true);
				settings.setLoadWithOverviewMode(true);
				Logcat.v(logLevel, "res.getHref()=" + res.getHref());
				view.addJavascriptInterface(new WebAppInterface(this), "AndroidHost");
				view.loadDataWithBaseURL("https://epub.local/", imageHtml, "text/html", "UTF-8", null);
			}
			else {
				// 通常のHTMLページ
				view.setAlpha(0f);
				WebSettings settings = view.getSettings();
				// コンテンツを画面幅にフィットさせる機能を「オフ」にする
				settings.setLoadWithOverviewMode(false);
				settings.setUseWideViewPort(false);
				// Alpha を 0 にし背景色と同じ色に固定した「空」の状態にする
				view.addJavascriptInterface(new WebAppInterface(this), "AndroidHost");
				view.loadUrl("https://epub.local/" + res.getHref());
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Render Error", e);
		}
	}

	private void prevPage(boolean mode) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		int doublemode = (mDoubleMode) ? 2 : 1;
		if (mRenderBusy || mFindWord) return;
		boolean firstpage = false;
		if (isJapaneseMode && !mPageMoveEnable) {
			// 後ろへ戻る(右タップ)
			Logcat.v(logLevel, "後ろへ戻る(右タップ)currentScrollX=" + currentScrolllX + ", maxScrollX=" + maxScrollX);
			if (currentScrolllX < maxScrollX - 5) {
				Logcat.v(logLevel, "smoothScrollTo");
				smoothScrollTo(Math.min(maxScrollX, currentScrolllX + scrollXStep * doublemode), 0, mode);
				GetLeftWidth(leftWebView, true, false);
				currentOffset--;
				if (currentOffset < 0) currentOffset = 0;
				Logcat.v(logLevel, "currentOffset=" + currentOffset);
			}
			else {
				// 前の章へ(インデックス減少チェック)
				if (currentSpineIndex > 0) {
					Logcat.v(logLevel, "前の章へ(インデックス減少チェック)");
					isMovingToPreviousChapter = true;
					Logcat.v(logLevel, "moveToNextSpine(-1)");
					moveToNextSpine(-1);
				}
				else {
					Logcat.v(logLevel, "firstpage");
					firstpage = true;
				}
			}
		}
		else {
			// 前へ(左タップ)
			Logcat.v(logLevel, "前へ(左タップ)currentScrollY=" + currentScrolllY + ", maxScrollY=" + maxScrollY);
			if (currentScrolllY > 5 && (!meta.isFixed || mPageMoveEnable)) {
				Logcat.v(logLevel, "smoothScrollTo scrollStep=" + scrollYStep);
				smoothScrollTo(currentScrolllX, Math.max(0, currentScrolllY - scrollYStep * doublemode), mode);
				GetLeftWidth(leftWebView, false, false);
				currentOffset--;
				if (currentOffset < 0) currentOffset = 0;
				Logcat.v(logLevel, "currentOffset=" + currentOffset);
			}
			else if (currentScrolllX > 5 && mScrollxEnable) {
				Logcat.v(logLevel, "smoothScrollTo scrollStep=" + scrollXStep);
				smoothScrollTo(Math.max(0, currentScrolllX - scrollXStep), 0, mode);
				Logcat.v(logLevel, "横設定=" + Math.max(0, currentScrolllX - scrollXStep));
				GetLeftWidth(leftWebView, false, false);
				currentOffset--;
				if (currentOffset < 0) currentOffset = 0;
				Logcat.v(logLevel, "currentOffset=" + currentOffset);
			}
			else {
				if (currentSpineIndex > 0) {
				Logcat.v(logLevel, "前の章へ(インデックス減少チェック)");
					isMovingToPreviousChapter = true;
					Logcat.v(logLevel, "moveToNextSpine(-1)");
					moveToNextSpine(-1);
				}
				else {
					Logcat.v(logLevel, "firstpage");
					firstpage = true;
				}
			}
		}
		if (firstpage) {
			// 先頭ページ
			if (mLastMsg == DEF.LASTMSG_DIALOG) {
				showCloseDialog(CloseDialog.LAYOUT_TOP);
			}
			else if (mLastMsg == DEF.LASTMSG_NEXT) {
				finishActivity(CloseDialog.CLICK_PREVLAST, false, true);
			}
		}
		else {
			startVibrate();
		}
	}

	private void nextPage(boolean mode) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		int doublemode = (mDoubleMode) ? 2 : 1;
		if (mRenderBusy || mFindWord) return;
		boolean lastpage = false;
		if (isJapaneseMode && !mPageMoveEnable) {
			// 前へ進む(左タップ)
			Logcat.v(logLevel, "前へ進む(左タップ)currentScrollX=" + currentScrolllX);
			if (currentScrolllX > 5) {
				Logcat.v(logLevel, "smoothScrollTo");
				smoothScrollTo(Math.max(0, currentScrolllX - scrollXStep * doublemode), 0, mode);
				GetLeftWidth(leftWebView, true, false);
				currentOffset++;
				Logcat.v(logLevel, "currentOffset=" + currentOffset);
			}
			else {
				// 次の章へ(インデックス増加チェック)
				if (isAozora) {
					// 青空文庫は次の章が無い
					lastpage = true;
				}
				else if (currentSpineIndex + 1 < currentBook.getSpine().size()) {
					Logcat.v(logLevel, "moveToNextSpine(1)");
					moveToNextSpine(1);
				}
				else {
					lastpage = true;
				}
			}
		}
		else {
			// 次へ(右タップ)
			Logcat.v(logLevel, "次へ(右タップ)currentScrollY=" + currentScrolllY + ", maxScrollY=" + maxScrollY + ", currentScrollX=" + currentScrolllX + ", maxScrollX=" + maxScrollX);
			if (currentScrolllY < (maxScrollY - 5) && (!meta.isFixed || mPageMoveEnable)) {
				Logcat.v(logLevel, "smoothScrollTo y scrollStep=" + scrollYStep);
				smoothScrollTo(0, Math.min(maxScrollY, currentScrolllY + scrollYStep * doublemode), mode);
				GetLeftWidth(leftWebView, false, false);
				currentOffset++;
				Logcat.v(logLevel, "currentOffset=" + currentOffset);
			}
			else if (currentScrolllX < (maxScrollX - 5) && mScrollxEnable) {
				Logcat.v(logLevel, "smoothScrollTo x scrollStep=" + scrollXStep);
				smoothScrollTo(Math.min(maxScrollX, currentScrolllX + scrollXStep), 0, mode);
				Logcat.v(logLevel, "横設定=" + Math.min(maxScrollX, currentScrolllX + scrollXStep));
				GetLeftWidth(leftWebView, false, false);
				currentOffset++;
				Logcat.v(logLevel, "currentOffset=" + currentOffset);
			}
			else {
				if (isAozora) {
					// 青空文庫は次の章が無い
					lastpage = true;
				}
				else if (currentSpineIndex + 1 < currentBook.getSpine().size()) {
					Logcat.v(logLevel, "moveToNextSpine(1)");
					moveToNextSpine(1);
				}
				else {
					lastpage = true;
				}
			}
		}
		if (lastpage) {
			// 最終ページ
			if (mLastMsg == DEF.LASTMSG_DIALOG) {
				showCloseDialog(CloseDialog.LAYOUT_LAST);
			}
			else if (mLastMsg == DEF.LASTMSG_NEXT) {
				finishActivity(CloseDialog.CLICK_NEXTTOP, false, true);
			}
			else {
				finishActivity(false);
			}
		}
		else {
			startVibrate();
		}
	}

	private void calcRatio() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		ratiox = (maxScrollX > 0) ? (float)currentScrolllX / (float)maxScrollX : 0;
		ratioy = (maxScrollY > 0) ? (float)currentScrolllY / (float)maxScrollY : 0;
		Logcat.v(logLevel, "ratiox=" + ratiox + ", ratioy=" + ratioy);
	}

	private void smoothScrollTo(int targetX, int targetY, boolean mode) {
		scroller.startScroll(currentScrolllX, currentScrolllY, targetX - currentScrolllX, targetY - currentScrolllY, (mode) ? 500 : 0);
		rootContainer.postOnAnimation(flingRunnable);
	}

	private final Runnable flingRunnable = new Runnable() {
		@Override
		public void run() {
			if (scroller.computeScrollOffset()) {
				currentScrolllX = scroller.getCurrX();
				currentScrolllY = scroller.getCurrY();
				syncWebViewScroll();
				rootContainer.postOnAnimation(this);
			}
		}
	};

	private void syncWebViewScroll() {
		int scrollStepX = 0;
		int scrollStepY = 0;
		if (mDoubleMode) {
			if (isJapaneseMode && !mPageMoveEnable) {
				// 縦ページ
				scrollStepX = scrollXStep;
			}
			else {
				// 横ページ
				scrollStepY = scrollYStep;
			}
		}
		try {
			if (!mDoubleMode) {
				leftWebView.scrollTo(currentScrolllX, currentScrolllY);
			}
			else if (mUpDownMode) {
				leftWebView.scrollTo(currentScrolllX - scrollStepX, currentScrolllY);
				if (mDoubleMode) rightWebView.scrollTo(currentScrolllX, currentScrolllY + scrollStepY);
			}
			else {
				leftWebView.scrollTo(currentScrolllX, currentScrolllY);
				if (mDoubleMode) rightWebView.scrollTo(currentScrolllX - scrollStepX, currentScrolllY + scrollStepY);
			}
		}
		catch (Exception e) {
		}

		PutFooterView();
	}

	private void moveToNextSpine(int delta) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (currentBook == null) return;
		int nextIndex = currentSpineIndex + delta;
		// 範囲外なら何もしない
		if (nextIndex < 0 || nextIndex >= currentBook.getSpine().size()) {
			isMovingToPreviousChapter = false; // フラグを戻す
			return;
		}
		pendingPos = (delta > 0) ? 0 : 1;
		Logcat.v(logLevel, "renderSpine()を実行 moveToNextSpine()");
		renderSpine(nextIndex, leftWebView);
	}

	private String getDirection(Book book) {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(book.getOpfResource().getInputStream());
			NodeList nodes = doc.getElementsByTagName("spine");
			if (nodes.getLength() > 0) return ((Element) nodes.item(0)).getAttribute("page-progression-direction");
		}
		catch (Exception e) {}
		return "ltr";
	}

	private void initMeasurementWebView() {
		measurementWebView = new WebView(this);
		WebSettings measureSettings = measurementWebView.getSettings();
		measureSettings.setJavaScriptEnabled(true);
		// 実機のディスプレイサイズを取得
		android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;
		// とりあえず実機の全画面サイズで初期化
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth, screenHeight);
		measurementWebView.layout(0, 0, screenWidth, screenHeight);
		// JSを有効化
		measurementWebView.getSettings().setJavaScriptEnabled(true);
		// 勝手に音声を流さないようにする
		measureSettings.setMediaPlaybackRequiresUserGesture(true);
		measurementWebView.setLayoutParams(params);
		measurementWebView.setAlpha(0.001f); // ほぼ透明
		// 隠すと計測できないのでVISIBLEのまま透明にする
		measurementWebView.setVisibility(View.VISIBLE);
		// 表示用と同じ設定をコピー
		measureSettings.setJavaScriptEnabled(true);
		measureSettings.setTextZoom(mTextSize);
		measureSettings.setDefaultFontSize(leftWebView.getSettings().getDefaultFontSize());
		measurementWebView.setInitialScale(mScaleFix);
		measureSettings.setLayoutAlgorithm(leftWebView.getSettings().getLayoutAlgorithm());
		// ローカルファイルからのアクセスをすべて許可する
		measureSettings.setAllowFileAccess(true);
		measureSettings.setAllowContentAccess(true);
		measureSettings.setAllowFileAccessFromFileURLs(true);
		measureSettings.setAllowUniversalAccessFromFileURLs(true);
		// 画面サイズに合わせる設定
		measureSettings.setUseWideViewPort(false);
		measureSettings.setLoadWithOverviewMode(false);
		measurementWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		// 表示用WebViewが使っているClientを取得してセットする
		measurementWebView.setWebViewClient(leftWebView.getWebViewClient());
		measurementWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				if (isJapaneseMode) {
					applyNormalStyleWidth(measurementWebView);
				}
				else {
					applyNormalStyleHeight(measurementWebView);
				}
				// ページ読み込み完了後少し待ってからJSを実行する
				// 読み込み直後は DOM が構築中な場合があるため
				mainHandler.postDelayed(() -> {
					runMeasurementJS(view);
				}, 200); // 念のため待つ
			}
			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
				String path = request.getUrl().getPath();
				if (path != null && path.startsWith("/")) path = path.substring(1);
				if (currentBook == null || path == null) return null;

				String url = request.getUrl().toString().toLowerCase();
				if (url.endsWith(".mp3") || url.endsWith(".mp4") || url.endsWith(".webm") || url.endsWith(".wav") || url.endsWith(".m4a") || url.endsWith(".ogg")) {
					return new WebResourceResponse("audio/mpeg", "UTF-8", null);
				}
				boolean isFontRequest = (path.endsWith(".otf") || path.endsWith(".ttf") || path.endsWith(".woff"));
				// 優先順位1: ユーザーフォントがあれば返す
				if (isFontRequest) {
					File cacheFont = new File(getCacheDir(), "current_font.ttf");
					if (cacheFont.exists()) {
						try {
							// FileInputStreamを直接渡す
							// WebView側で読み込みが終わると自動的にcloseされる
							FileInputStream fis = new FileInputStream(cacheFont);
							return new WebResourceResponse("font/ttf", "UTF-8", fis);
						}
						catch (FileNotFoundException e) {
							Log.e(TAG, "Font file not found during intercept", e);
						}
					}
				}
				// 優先順位2: EPUB内の本来のリソースを探す
				Resource r = resourceCache.get(path);
				if (r == null) {
					for (String key : resourceCache.keySet()) {
						if (path.endsWith(key)) {
							r = resourceCache.get(key);
							break;
						}
					}
				}
				if (r != null) {
					try {
						return new WebResourceResponse(r.getMediaType().getName(), "UTF-8", new ByteArrayInputStream(r.getData()));
					}
					catch (IOException e) { return null; }
				}
				if (isFontRequest) {
					File cacheFont = new File(getCacheDir(), "current_font.ttf");
					if (cacheFont.exists()) {
						try {
							// FileInputStreamを直接渡す
							// WebView側で読み込みが終わると自動的にcloseされる
							FileInputStream fis = new FileInputStream(cacheFont);
							return new WebResourceResponse("font/ttf", "UTF-8", fis);
						}
						catch (FileNotFoundException e) {
							Log.e(TAG, "Font file not found during intercept", e);
						}
					}
					// フォントがない場合は404
					return new WebResourceResponse("font/ttf", "UTF-8", 404, "Not Found", null, null);
				}
				return null;
			}
		});
	}

	private void runMeasurementJS(WebView view) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (view == null) return;
		PageMetadata rmeta = fetchMetadata(currentBook, measurementCurrentIdx, false);
		if (rmeta.isImageOnly || rmeta.isFixed) {
			// 画像のみ又は固定レイアウトの場合は1ページ固定
			Logcat.v(logLevel, "画像のみ又は固定レイアウトの場合は1ページ固定");
			Logcat.v(logLevel, "計測結果: Chapter " + measurementCurrentIdx + " / " + 1 + " pages (文字数:" + 0 + ")");
			if (getSavedPageCount(measurementCurrentIdx) == 0) {
				savePageCount(measurementCurrentIdx, 1);
			}

			processNextInQueue();
			return;
		}
		// JSで「文字数幅高さ」を返す
		String js = "(function() {" +
			"  var text = document.body.textContent || '';" +
			"  var target = document.documentElement || document.body;" +
			"  const style = window.getComputedStyle(document.body);"+
			"  const fontSize = parseFloat(style.fontSize);"+
			"  const lineHeight = parseFloat(style.lineHeight) || fontSize * 1.5; "+
			"  return text.length + ':' + target.scrollWidth + ',' + target.scrollHeight + ',' + lineHeight;" +
			"})();";

		view.evaluateJavascript(js, value -> {
			int pages = 1;
			int textLength = 0;
			try {
				String cleanValue = value.replace("\"", "");
				String[] parts = cleanValue.split(":");
				textLength = Integer.parseInt(parts[0]);
				// 以降ページ数計算ロジック
				String[] sizes = parts[1].split(",");
				float scrollWidth = Float.parseFloat(sizes[0]) * view.getScale();
				float scrollHeight = Float.parseFloat(sizes[1])* view.getScale();
				float lineHeight = Float.parseFloat(sizes[2]);

				double safeLineCountX = Math.floor(view.getWidth() / lineHeight) -1;
				double safeLineCountY = Math.floor(view.getHeight() / lineHeight) -1;
				int scrollX = (int)((int)(safeLineCountX) * lineHeight);
				int scrollY = (int)((int)(safeLineCountY) * lineHeight);
				if (isJapaneseMode) { 
					// 縦書きの場合：横方向の長さを画面の横幅で割る
					if (textLength < 50) {
						pages = 1;
					}
					else {
						pages = calcCurrentPageMax((int)scrollWidth, scrollX, false);
						Logcat.v(logLevel, "縦書き検知: 横幅 " + scrollWidth + " を基準に計算 表示範囲=" + scrollX);
					}
				}
				else {
					// 横書きの場合：縦方向の長さを画面の高さで割る
					if (textLength < 50) {
						pages = 1;
					}
					else {
						pages = calcCurrentPageMax((int)scrollHeight, scrollY, false);
					Logcat.v(logLevel, "横書きの場合: 高さ " + scrollHeight + " を基準に計算 表示範囲=" + scrollY);
					}
				}
			}
			catch (Exception e) {
			}
			Logcat.v(logLevel, "計測結果: Chapter " + measurementCurrentIdx + " / " + pages + " pages (文字数:" + textLength + ")");
			if (getSavedPageCount(measurementCurrentIdx) == 0) {
				savePageCount(measurementCurrentIdx, pages);
			}
			PutFooterView();

			processNextInQueue();
		});
	}

	private void PutFooterView() {
		int nowpage = GetNowPage();
		int totalpage = getTotalPagesNow();
		String pagetext = (nowpage > 0 && totalpage > 0) ? nowpage + " / " + totalpage : "- / -";
		if (mFooterOn && footerView != null) footerView.setText(pagetext);
	}

	private void initTotalPage() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (currentBook == null) return;
		int totalSpines = currentBook.getSpine().size();
		Logcat.v(logLevel, "totalSpines=" + totalSpines);
		mTotalPage = new int[totalSpines];

		getTotalPageFile();
	}

	private void startBackgroundScan() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (currentBook == null) return;
		int totalSpines = currentBook.getSpine().size();
		measurementQueue.clear();
		// 全章を計測リストに入れる
		for (int i = 0; i < totalSpines; i++) {
			measurementQueue.add(i);
		}
		// 計測開始
		Logcat.v(logLevel, "計測開始");
		processNextInQueue();
	}

	private File getPageCacheFile() {
		// files/page_counts/ ディレクトリ内にハッシュ名で保存
		File dir = new File(getFilesDir(), "page_counts");
		if (!dir.exists()) dir.mkdirs();
		return new File(dir, "epub_" + String.valueOf(mFilePath.hashCode()) + ".cache");
	}

	private void getTotalPageFile() {
		if (currentBook == null) return;
		File file = getPageCacheFile();
		if (!file.exists()) return;
		Properties prop = new Properties();
		try (FileInputStream fis = new FileInputStream(file)) {
			// ここで一括読み込み
			prop.load(fis);
		}
		catch (IOException e) {
			return;
		}
		int spineSize = currentBook.getSpine().size();
		for (int i = 0; i < spineSize; i++) {
			String val = prop.getProperty("spine_" + i, "0");
			mTotalPage[i] = Integer.parseInt(val);
		}
	}

	private int[] mTotalPage;

	// ページ数をファイルに保存する
	private void savePageCount(int idx, int count) {
		mTotalPage[idx] = count;
	}

	// 保存されたページ数を取得する
	private int getSavedPageCount(int idx) {
		int total = 0;
		try {
			total = mTotalPage[idx];
		}
		catch (Exception e) {
		}
		return total;
	}

	private void SaveTotalPage() {
		if (currentBook == null) return;
		File file = getPageCacheFile();
		Properties prop = new Properties();
		// 新しい値をセット
		int spineSize = currentBook.getSpine().size();
		for (int i = 0; i < spineSize ; i++) {
			prop.setProperty("spine_" + i, String.valueOf(mTotalPage[i]));
		}
		// ファイルに書き出す
		try (FileOutputStream fos = new FileOutputStream(file)) {
			prop.store(fos, null);
			fos.flush();
			fos.close();
		}
		catch (IOException e) {
			Log.e(TAG, "Cache save error", e);
		}
	}

	private int getTotalPages() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (currentBook == null) return 0;
		File file = getPageCacheFile();
		if (!file.exists()) return 0;

		Properties prop = new Properties();
		try (FileInputStream fis = new FileInputStream(file)) {
			// ここで一括読み込み
			prop.load(fis);
		}
		catch (IOException e) {
			return 0;
		}

		int total = 0;
		int spineSize = currentBook.getSpine().size();
		for (int i = 0; i < spineSize; i++) {
			String val = prop.getProperty("spine_" + i, "0");
			int count = Integer.parseInt(val);
			total += count;
			Logcat.v(logLevel, "章=" + i + " ページ数=" + count);
		}
		return total;
	}


	// 中断するためのメソッド
	public void cancelMeasurement() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		isMeasurementCancelled = true;
		measurementQueue.clear(); // キューを空にする
		Logcat.v(logLevel, "計測処理の中断がリクエストされました");
		// WebViewのロードも止める(メインスレッドで実行)
		mainHandler.post(() -> {
			if (measurementWebView != null) {
				measurementWebView.stopLoading();
			}
		});
	}

	private void processNextInQueue() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// 中断フラグが立っていたら何もしない
		if (isMeasurementCancelled) {
			Logcat.v(logLevel, "計測は中断されています。処理を終了します。");
			return;
		}
		if (measurementQueue.isEmpty()) {
			SaveTotalPage();
			Logcat.v(logLevel, "すべての章の計測が完了しました。total=" + getTotalPages());
			if (measurementWebView != null) {
				measurementWebView.stopLoading();
			}
			if (measurementWebView != null) {
				ViewParent parent = measurementWebView.getParent();
				if (parent instanceof ViewGroup) {
					((ViewGroup) parent).removeView(measurementWebView);
				}
				measurementWebView.stopLoading();
				measurementWebView.clearHistory();
				measurementWebView.removeAllViews();
				measurementWebView.destroy();
				measurementWebView = null;
			}
			return;
		}
		int nextIdx = measurementQueue.remove(0);
		measurementCurrentIdx = nextIdx;
		// 表示用WebViewの「現在の幅」を取得
		int targetWidth = leftWebView.getWidth();
		int targetHeight = leftWebView.getHeight();
		mainHandler.postDelayed(() -> {
			// 再度チェック: postされたタイミングでも中断されていないか確認
			if (isMeasurementCancelled) return;
			// 計測用WebViewのサイズを表示用と全く同じにする
			if (targetWidth > 0 && targetHeight > 0) {
				ViewGroup.LayoutParams lp = measurementWebView.getLayoutParams();
				lp.width = targetWidth;
				lp.height = targetHeight;
				measurementWebView.setLayoutParams(lp);
			}
			// processNextInQueue 内を修正
			Resource res = currentBook.getSpine().getSpineReferences().get(nextIdx).getResource();
			String href = res.getHref();
			String url = "https://epub.local/" + href;
			measurementWebView.loadUrl(url);
		}, 200);
	}

	// 全体の合計ページ数を計算
	private int getTotalPagesNow() {
		int total = 0;
		if (isAozora) {
			try {
				if (isJapaneseMode) {
					total = maxScrollX / scrollXStep + 1;
				}
				else {
					total = maxScrollY / scrollYStep + 1;
				}
			}
			catch (Exception e) {
			}
			return total;
		}
		if (currentBook == null) return 0;
		for (int i = 0; i < currentBook.getSpine().size(); i++) {
			total += getSavedPageCount(i);
		}
		return total;
	}

	// 現在の「絶対ページ数」を計算
	private int getCurrentAbsolutePage() {
		if (leftWebView == null) return 0;
		// 現在の章より前の章の合計を足す
		int currentPage = 0;
		for (int i = 0; i < currentSpineIndex; i++) {
			currentPage += getSavedPageCount(i);
		}
		// 今の章の中での進捗を足す
		// 縦書きならscrollX横書きならscrollYを使用
		int scrollPos = isJapaneseMode ? leftWebView.getScrollX() : leftWebView.getScrollY();
		int viewSize = isJapaneseMode ? leftWebView.getWidth() : leftWebView.getHeight();
		if (viewSize > 0) {
			// 現在の章の何ページ目にいるか(0始まりなので+1)
			int pageInChapter = (int) Math.floor((double) Math.abs(scrollPos) / viewSize) + 1;
			currentPage += pageInChapter;
		}
		return currentPage;
	}

	private void displayNavAnchors(Book currentBook) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// 全リソースのパス一覧から nav.xhtml を探す
		String navPath = null;
		for (String href : currentBook.getResources().getAllHrefs()) {
			if (href.endsWith("nav.xhtml")) {
				navPath = href;
				break;
			}
		}
		if (navPath == null) {
			Logcat.v(logLevel, "nav.xhtml が見つかりませんでした。");
			return;
		}
		// nav.xhtml が属するディレクトリを取得(例: "text/nav.xhtml" -> "text/")
		String baseDir = "";
		if (navPath.contains("/")) {
			baseDir = navPath.substring(0, navPath.lastIndexOf("/") + 1);
		}
		try {
			// 目次ファイルの内容を取得
			Resource navResource = currentBook.getResources().getByHref(navPath);
			String content = new String(navResource.getData(), "UTF-8");
			// XmlPullParser で解析
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(new StringReader(content));
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG && "a".equalsIgnoreCase(xpp.getName())) {
					String hrefValue = null;
					// href属性を探す
					for (int i = 0; i < xpp.getAttributeCount(); i++) {
						if ("href".equalsIgnoreCase(xpp.getAttributeName(i))) {
							hrefValue = xpp.getAttributeValue(i);
							break;
						}
					}
					if (hrefValue != null) {
						// フルパスの構築(text/ + text-1.xhtml#id = text/text-1.xhtml#id)
						String fullPath = baseDir + hrefValue;
						// タイトル(aタグの中身)を取得するために次のテキストイベントまで進む
						xpp.next();
						String title = "";
						if (xpp.getEventType() == XmlPullParser.TEXT) {
							title = xpp.getText();
						}
						Logcat.v(logLevel, "見出し: " + title + " -> パス: " + fullPath);
					}
				}
				eventType = xpp.next();
			}
		}
		catch (Exception e) {
			Log.e(TAG, "解析中にエラーが発生しました: " + e.getMessage());
		}
	}

	private void displayNcxAnchors(Book currentBook) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// toc.ncxを探す
		String ncxPath = null;
		for (String href : currentBook.getResources().getAllHrefs()) {
			if (href.endsWith("toc.ncx")) {
				ncxPath = href;
				break;
			}
		}
		if (ncxPath == null) {
			Log.e(TAG, "toc.ncx が見つかりませんでした。");
			return;
		}
		// ディレクトリ階層の取得 (例: "OEBPS/toc.ncx" -> "OEBPS/")
		String baseDir = "";
		if (ncxPath.contains("/")) {
			baseDir = ncxPath.substring(0, ncxPath.lastIndexOf("/") + 1);
		}
		try {
			Resource ncxResource = currentBook.getResources().getByHref(ncxPath);
			XmlPullParser xpp = XmlPullParserFactory.newInstance().newPullParser();
			xpp.setInput(new StringReader(new String(ncxResource.getData(), "UTF-8")));
			int eventType = xpp.getEventType();
			String currentTitle = "";
			String currentHref = "";
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tagName = xpp.getName();
				if (eventType == XmlPullParser.START_TAG) {
					if ("text".equalsIgnoreCase(tagName)) {
						// navLabelの中のtextタグ
						currentTitle = xpp.nextText();
					}
					else if ("content".equalsIgnoreCase(tagName)) {
						// contentタグのsrc属性を取得
						for (int i = 0; i < xpp.getAttributeCount(); i++) {
							if ("src".equalsIgnoreCase(xpp.getAttributeName(i))) {
								currentHref = baseDir + xpp.getAttributeValue(i);
							}
						}
					}
				}
				else if (eventType == XmlPullParser.END_TAG) {
					if ("navPoint".equalsIgnoreCase(tagName)) {
						// 1つの項目が終了したらログ出力
						Logcat.v(logLevel, "見出し: " + currentTitle + " -> パス: " + currentHref);
					}
				}
				eventType = xpp.next();
			}
		}
		catch (Exception e) {
			Log.e(TAG, "解析エラー: " + e.getMessage());
		}
	}

	public String getNavFilePathFromOpf(Book book) {
		try {
			// OPFファイル(設計図)のリソースを取得
			Resource opfResource = book.getOpfResource();
			if (opfResource == null) return null;
			// OPFファイルをXMLとして読み込む
			try (InputStream is = opfResource.getInputStream()) {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
				// <item>タグをすべて取得
				NodeList items = doc.getElementsByTagName("item");
				for (int i = 0; i < items.getLength(); i++) {
					Element item = (Element) items.item(i);
					String properties = item.getAttribute("properties");
					// properties 属性に "nav" が含まれているか確認
					if (properties != null && properties.contains("nav")) {
						// そのアイテムの href(ファイル名/パス)を返す
						return item.getAttribute("href");
					}
				}
			}
			catch (Exception e) {
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// 見つからない場合
		return null;
	}

	// これまでの解析ロジックを統合したメソッド
	private List<TocItem> getTocItems(Book book) {
		List<TocItem> items = new ArrayList<>();
		String navFileName = getNavFilePathFromOpf(book);
		if (navFileName != null) {
			// 例:"TOC.xhtml"が取得できたらそのリソースを取り出す
			String navPath = findFile(book, navFileName);
			if (navPath != null) {
				items = parseNavXhtml(book, navPath);
			}
		}
		else {
			// nav.xhtmlを探して解析
			String navPath = findFile(book, "nav.xhtml");
			if (navPath != null) {
				items = parseNavXhtml(book, navPath);
			}
			// なければ toc.ncx を探して解析
			else {
				String ncxPath = findFile(book, "toc.ncx");
				if (ncxPath != null) {
					items = parseNcx(book, ncxPath);
				}
			}
		}
		return items;
	}

	// 単一のHTML文字列から見出し(h1, h2)を抽出する
	private List<TocItem> getHtmlTagItems(String htmlString) {
		List<TocItem> headingList = new ArrayList<>();
	
		try {
			// 型をフルパス (org.jsoup.nodes.Document) で指定
			org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(htmlString);
			// 選択結果もフルパス (org.jsoup.select.Elements) で指定
			org.jsoup.select.Elements headings = doc.select("h1, h2");
			int count = 0;

			for (org.jsoup.nodes.Element heading : headings) {
				String text = heading.text().trim();

				if (!text.isEmpty() && text.length() < 50 && !text.contains("。")) {
					String id = heading.id();
					if (id == null || id.isEmpty()) {
						id = "gen_id_" + (count++);
						heading.attr("id", id);
					}
					// href は "#id" 形式
					headingList.add(new TocItem(text, "#" + id));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return headingList;
	}

	// 全リソースをループして見出しを抽出する例
	private List<TocItem> getHtmlTagItems(Book book) {
		// Spine(本文リソース)を巡回
		Spine spine = book.getSpine();
		List<TocItem> headingList = new ArrayList<>();
		for (SpineReference spineReference : spine.getSpineReferences()) {
			Resource resource = spineReference.getResource();
			String href = resource.getHref();
			try {
				String htmlContent = new String(resource.getData(), "UTF-8");
				org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(htmlContent);
				org.jsoup.select.Elements headings = doc.select("h1, h2");
				int count = 0;
				for (org.jsoup.nodes.Element heading : headings) {
					String text = heading.text().trim();
					// 前回のフィルタ条件
					if (!text.isEmpty() && text.length() < 50 && !text.contains("。")) {
						// IDがない場合はジャンプ用にユニークなIDを付与する
						String id = heading.id();
						if (id.isEmpty()) {
							id = "gen_id_" + (count++);
							heading.attr("id", id);
						}
						headingList.add(new TocItem(text, href + "#" + id));
					}
				}

			}
			catch (Exception e) {
			}
		}
		return headingList;
	}

	// ファイルを探す汎用メソッド
	private String findFile(Book book, String fileName) {
		for (String href : book.getResources().getAllHrefs()) {
			if (href.endsWith(fileName)) return href;
		}
		return null;
	}
	// EPUB3(nav.xhtml)用の解析メソッド
	private List<TocItem> parseNavXhtml(Book book, String navPath) {
		List<TocItem> items = new ArrayList<>();
		String baseDir = getBaseDir(navPath);
		try {
			Resource res = book.getResources().getByHref(navPath);
			XmlPullParser xpp = createXmlParser(res);

			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG && "a".equalsIgnoreCase(xpp.getName())) {
					String href = getAttribute(xpp, "href");
					// <a>直後のテキストを取得
					String title = xpp.nextText();
					if (href != null) {
						items.add(new TocItem(title, baseDir + href));
					}
				}
				eventType = xpp.next();
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Nav Parse Error", e);
		}
		return items;
	}

	// EPUB2(toc.ncx)用の解析メソッド
	private List<TocItem> parseNcx(Book book, String ncxPath) {
		List<TocItem> items = new ArrayList<>();
		String baseDir = getBaseDir(ncxPath);
		try {
			Resource res = book.getResources().getByHref(ncxPath);
			XmlPullParser xpp = createXmlParser(res);

			int eventType = xpp.getEventType();
			String currentTitle = "";
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tag = xpp.getName();
				if (eventType == XmlPullParser.START_TAG) {
					if ("text".equalsIgnoreCase(tag)) {
						currentTitle = xpp.nextText();
					}
					else if ("content".equalsIgnoreCase(tag)) {
						String src = getAttribute(xpp, "src");
						if (src != null) {
							items.add(new TocItem(currentTitle, baseDir + src));						}
					}
				}
				eventType = xpp.next();
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Ncx Parse Error", e);
		}
		return items;
	}
	// 共通のユーティリティメソッド
	// パスの階層を維持するための処理
	private String getBaseDir(String path) {
		if (path.contains("/")) {
			return path.substring(0, path.lastIndexOf("/") + 1);
		}
		return "";
	}
	// 属性値を安全に取得する
	private String getAttribute(XmlPullParser xpp, String attrName) {
		for (int i = 0; i < xpp.getAttributeCount(); i++) {
			if (attrName.equalsIgnoreCase(xpp.getAttributeName(i))) return xpp.getAttributeValue(i);
		}
		return null;
	}
	// パーサーの初期化
	private XmlPullParser createXmlParser(Resource res) throws Exception {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(new StringReader(new String(res.getData(), "UTF-8")));
		return xpp;
	}

	private void jumpToAnchor(WebView view, String Item) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// 強制再描画
		parentLayout.setBackgroundColor(colorInt);
		parentLayout.invalidate();
		view.setBackgroundColor(colorInt);
		// 検索をクリア
		leftWebView.clearMatches(); 
		if (mDoubleMode) rightWebView.clearMatches();

		if (Item.toLowerCase().contains("image/svg+xml")) {
			Logcat.v(logLevel, "SVG detected. Wrapping in virtual HTML: " + Item);
			// SVGを画面いっぱいに表示するためのラッパーHTML
			String wrapperHtml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<html><head><style>" +
				"html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; display: flex; align-items: center; justify-content: center; background: transparent; }" +
				"embed, svg, object { width: 100%; height: 100%; object-fit: contain; }" +
				"</style></head><body>" +
				"<embed src=\"https://epub.local/" + Item + "\" type=\"image/svg+xml\" />" +
				"</body></html>";
			// BaseURLを指定することで内部の<embed src>がshouldInterceptRequestでフックされる
			view.loadDataWithBaseURL("https://epub.local/", wrapperHtml, "text/html", "UTF-8", null);
			}
		else if (Item.toLowerCase().contains("image/")) {
			Logcat.v(logLevel, "Direct Image detected: " + Item);
			// 画像を画面中央にアスペクト比を維持して収めるHTMLを生成
			String imageHtml = "<html><head><style>" +
				"  body { margin: 0; padding: 0; background: transparent; display: flex; " +
				"         justify-content: center; align-items: center; height: 100vh; overflow: hidden; } " +
				"  img { max-width: 100%; max-height: 100%; object-fit: contain; } " +
				"</style></head><body>" +
				"<img src=\"https://epub.local/" + Item + "\">" +
				"</body></html>";
			WebSettings settings = view.getSettings();
			// 設定を画像用に最適化
			settings.setUseWideViewPort(true);
			settings.setLoadWithOverviewMode(true);
			view.loadDataWithBaseURL("https://epub.local/", imageHtml, "text/html", "UTF-8", null);
			Logcat.v(logLevel, "res.getHref()=" + Item);
		}
		else {
			WebSettings settings = leftWebView.getSettings();
			// コンテンツを画面幅にフィットさせる機能を「オフ」にする
			settings.setLoadWithOverviewMode(false);
			settings.setUseWideViewPort(false);
			String targetUrl = "https://epub.local/" + Item;
			view.loadUrl(targetUrl);
		}
	}

	public class TocItem {
		// 目次に表示するテキスト
		public String title;
		// ジャンプ先のパス(例: text/chapter1.xhtml#section1)
		public String href;
		public TocItem(String title, String href) {
			this.title = title;
			this.href = href;
		}
		// デバッグ時にログで見やすくするための設定(任意)
		@Override
		public String toString() {
			return "TocItem{" + "title='" + title + '\'' + ", href='" + href + '\'' + '}';
		}
	}

	public class EpubSearcher {
		public interface SearchCallback {
			void onFound(int spineIndex, String title, String snippet);
		}
		public void search(Book book, String query, SearchCallback callback) {
			// Spine(本の中身の並び)をすべて取得
			List<SpineReference> spineReferences = book.getSpine().getSpineReferences();
			for (int i = 0; i < spineReferences.size(); i++) {
				try {
					Resource resource = spineReferences.get(i).getResource();
					// HTMLをテキストに変換
					String html = new String(resource.getData(), resource.getInputEncoding());
					String plainText = Jsoup.parse(html).text();
					// 部分一致検索(大文字小文字を区別しない場合)
					if (plainText.toLowerCase().contains(query.toLowerCase())) {
						String title = resource.getTitle() != null ? resource.getTitle() : "第 " + (i + 1) + " 章";
						String snippet = createSnippet(plainText, query);
						// 結果をコールバックで返す
						callback.onFound(i, title, snippet);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// 検索語の前後を含めた短い文章(スニペット)を作成
		private String createSnippet(String text, String query) {
			int index = text.toLowerCase().indexOf(query.toLowerCase());
			int start = Math.max(0, index - 20);
			int end = Math.min(text.length(), index + query.length() + 20);
			return "..." + text.substring(start, end).replace("\n", " ") + "...";
		}
	}

	public class SearchItem {
		public int index;
		public SearchItem(int index) {
			this.index = index;
		}
	}

	private void SearchNext(boolean mode) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (isAozora) {
			mNowFindCount++;
			if (mNowFindCount == mNowFindTotal) {
				mNowFindCount = 0;
			}
		}
		else {
			if (SearchList == null) return;
			if (mode) {
				if (SearchList.size() == 1) return;
				mNowFindCount = 0;
				mSearchCount++;
				if (mSearchCount == SearchList.size()) {
					mSearchCount = 0;
				}
			}
			else {
				mNowFindCount++;
				if (mNowFindCount == mNowFindTotal) {
					mNowFindCount = 0;
					mSearchCount++;
					if (mSearchCount == SearchList.size()) {
						mSearchCount = 0;
					}
				}
			}
		}
		try {
			if (isAozora) {
				if (mLoadingSpinner != null) {
					mLoadingSpinner.setVisibility(leftWebView.VISIBLE);
					mLoadingSpinner.bringToFront();
				}
				Logcat.v(logLevel, "順方向に検索");
				leftWebView.findNext(true); 
				if (mDoubleMode) rightWebView.findNext(true);
			}
			else {
				SearchItem item = SearchList.get(mSearchCount);
				mSearchIndex = item.index;
				Logcat.v(logLevel, "mSearchIndex=" + mSearchIndex + ", mSearchCount=" + mSearchCount);
				Logcat.v(logLevel, "mNowFindCount=" + mNowFindCount + ", mNowFindTotal=" + mNowFindTotal);
				if (mLoadingSpinner != null) {
					mLoadingSpinner.setVisibility(leftWebView.VISIBLE);
					mLoadingSpinner.bringToFront();
				}
				if (mSearchIndex != currentSpineIndex) {
		   			renderSpine(mSearchIndex, leftWebView);
				}
				else {
					Logcat.v(logLevel, "順方向に検索");
					leftWebView.findNext(true); 
					if (mDoubleMode) rightWebView.findNext(true);
				}
			}
		}
		catch (Exception e) {
		}
	}

	private void clearSearchWord() {
		// 検索をクリア
		leftWebView.clearMatches(); 
		if (mDoubleMode) rightWebView.clearMatches();
		SearchList = null;
		searchKeyword = null;
		mNowFindTotal = 0;
		mNowFindCount = 0;
		mSearchCount = 0;
		TouchPanelView.SetSearchWordIndex(0, 0, 0, 0, 0);
	}

	private void SearchPrev(boolean mode) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (isAozora) {
			mNowFindCount--;
			if (mNowFindCount < 0) {
				mNowFindCount = mNowFindTotal - 1;
			}
		}
		else {
			if (SearchList == null) return;
			if (mode) {
				if (SearchList.size() == 1) return;
				mNowFindCount = mNowFindTotal - 1;
				mSearchCount--;
				if (mSearchCount < 0) {
					mSearchCount = SearchList.size() - 1;
				}
			}
			else {
				mNowFindCount--;
				if (mNowFindCount < 0) {
					mNowFindCount = mNowFindTotal - 1;
					mSearchCount--;
					if (mSearchCount < 0) {
						mSearchCount = SearchList.size() - 1;
					}
				}
			}
		}
		try {
			if (isAozora) {
				if (mLoadingSpinner != null) {
					mLoadingSpinner.setVisibility(leftWebView.VISIBLE);
					mLoadingSpinner.bringToFront();
				}
				Logcat.v(logLevel, "逆方向に検索");
				leftWebView.findNext(false); 
				if (mDoubleMode) rightWebView.findNext(false);
			}
			else {
				SearchItem item = SearchList.get(mSearchCount);
				mSearchIndex = item.index;
				Logcat.v(logLevel, "mSearchIndex=" + mSearchIndex + ", mSearchCount=" + mSearchCount);
				Logcat.v(logLevel, "mNowFindCount=" + mNowFindCount + ", mNowFindTotal=" + mNowFindTotal);
				if (mLoadingSpinner != null) {
					mLoadingSpinner.setVisibility(leftWebView.VISIBLE);
					mLoadingSpinner.bringToFront();
				}
				if (mSearchIndex != currentSpineIndex) {
					mNowPrevSet = true;
					mNowPrevSetl = true;
					mNowPrevSetr = true;
		   			renderSpine(mSearchIndex, leftWebView);
				}
				else {
					Logcat.v(logLevel, "逆方向に検索");
					leftWebView.findNext(false); 
					if (mDoubleMode) rightWebView.findNext(false);
				}
			}
		}
		catch (Exception e) {
		}
	}

	private void SearchWord(String Keyword) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "SearchWord()");
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Handler handler = new Handler(Looper.getMainLooper());
		SearchList = new ArrayList<>();
		searchKeyword = Keyword;
		AtomicInteger firstindex = new AtomicInteger(-1);
		executor.execute(() -> {
			if (isAozora) {
				// UIスレッドに反映
				handler.post(() -> {
					if (firstindex.get() == -1) {
						// 表示の開始
						mSearchIndex = 0;
						mSearchCount = 0;
						mNowFindCount = 0;
						firstindex.set(0);
						Logcat.v(logLevel, "index=" + firstindex.get());
						// あからじめ検索内容をクリアしておく
						leftWebView.clearMatches(); 
						if (mDoubleMode) rightWebView.clearMatches();
						mFindWord = true;
						// 検索を実行(WebView上で保持しているので即実行)
						leftWebView.findAllAsync(searchKeyword);
						if (mDoubleMode) rightWebView.findAllAsync(searchKeyword);
					}
				});
			}
			else {
				EpubSearcher searcher = new EpubSearcher();
				searcher.search(currentBook, searchKeyword, (spineIndex, title, snippet) -> {
					// UIスレッドに反映
					handler.post(() -> {
						Logcat.v(logLevel, "spineIndex=" + spineIndex + ", 見つかった章: " + title + " 内容: " + snippet);
						SearchList.add(new SearchItem(spineIndex));
						if (firstindex.get() == -1) {
							// 表示の開始
							currentIndex = spineIndex;
							currentSpineIndex = spineIndex;
							mSearchIndex = currentIndex;
							mSearchCount = 0;
							mNowFindCount = 0;
							firstindex.set(currentIndex);
							Logcat.v(logLevel, "index=" + firstindex.get());
							if (mLoadingSpinner != null) {
								mLoadingSpinner.setVisibility(View.VISIBLE);
								mLoadingSpinner.bringToFront();
							}
							leftWebView.setAlpha(0f);
							if (mDoubleMode) rightWebView.setAlpha(0f);
							mFindWord = true;
							renderSpine(currentIndex, leftWebView);
						}
					});
				});
			}
		});
	}

	private void startScrollStopMonitoring() {
		leftWebView.removeCallbacks(scrollCheckRunnable);
		lastlX = -1;
		lastlY = -1;
		leftWebView.post(scrollCheckRunnable);
	}

	private final Runnable scrollCheckRunnable = new Runnable() {
		@Override
		public void run() {
			int logLevel = Logcat.LOG_LEVEL_WARN;
			int currentlX = leftWebView.getScrollX();
			int currentlY = leftWebView.getScrollY();
			int currentrX = (rightWebView != null) ? rightWebView.getScrollX() : 0;
			int currentrY = (rightWebView != null) ? rightWebView.getScrollY() : 0;
			// まだ動いているあるいは最初の一歩が始まっていない場合
			if ((lastlX == -1 || currentlX != lastlX || currentrX != lastrX) && (isJapaneseMode) || (lastlY == -1 || currentlY != lastlY || currentrY != lastrY) && (!isJapaneseMode)) {
				if (currentlX != lastlX || currentlY != lastlY) {
					mScrollMovel = true;
				}
				if (currentrX != lastrX || currentrY != lastrY) {
					mScrollMover = true;
				}
				if (isJapaneseMode) {
					lastlX = currentlX;
					lastrX = currentrX;
				}
				else {
					lastlY = currentlY;
					lastrY = currentrY;
				}
				leftWebView.postDelayed(this, 100); // 100msごとにチェック
			}
			else {
				// 停止したのでここで初めて座標を同期
				Logcat.v(logLevel, "ここで初めて座標を同期");
				// 二つのWebViewの場合は反対側のWebViewから座標を取得する
				WebView nowView = (mDoubleMode) ? rightWebView : leftWebView;
				nowView.evaluateJavascript("(function() { " +
					" var y = window.pageYOffset || "+
					"     document.documentElement.scrollTop || "+
					"     document.body.scrollTop || 0;"+
					" var x = window.pageXOffset || "+
					"     document.documentElement.scrollLeft || "+
					"     document.body.scrollLeft || 0;"+

					" return Math.round(x) + ',' + Math.round(y);"+
					"})();", value -> {
					if (value != null && !value.equals("null")) {
						Logcat.v(logLevel, "value=" + value);
						
						String[] parts = value.replace("\"", "").split(",");
						float d = mScaleDensity;
						int scrollX = (int)(Float.parseFloat(parts[0]) * d);
						int scrollY = (int)(Float.parseFloat(parts[1]) * d);
						if (scrollX <= 0) {
							currentScrolllX = maxScrollX + scrollX;
						}
						if (scrollY >= 0) {
							currentScrolllY = scrollY;
						}
						mFindWord = false;
						Logcat.v(logLevel, "currentScrollX=" + currentScrolllX + ", currentScrollY=" + currentScrolllY + ", maxScrollX=" + maxScrollX);
						if (mLoadingSpinner != null) {
							mLoadingSpinner.setVisibility(nowView.GONE);
						}
						syncWebViewScroll();
					}
				});
			}
		}
	};


	// 青空文庫のZIPファイルまたはTXTファイルを開く
	private void loadSource(File sourceFile) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (mLoadingSpinner != null) {
			mLoadingSpinner.setVisibility(View.VISIBLE);
			mLoadingSpinner.bringToFront();
		}
		executor.execute(() -> {
			try {
				Map<String, String> imagesBase64 = new HashMap<>();
				String rawText = null;
				final String baseUrl;
				String fileName = sourceFile.getName().toLowerCase();
				// テキストファイル(.txt)の場合
				if (fileName.endsWith(".txt")) {
					// テキストファイルが置かれている「親フォルダ」をベースURLにする
					File parentDir = sourceFile.getParentFile();
					if (parentDir != null) {
						baseUrl = "file://" + parentDir.getAbsolutePath() + "/";
					}
					else {
						baseUrl = "file://" + getCacheDir().getAbsolutePath() + "/";
					}
					// テキストファイルの読み込み (Shift_JIS = MS932)
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					try (FileInputStream fis = new FileInputStream(sourceFile)) {
						byte[] buffer = new byte[8192];
						int len;
						while ((len = fis.read(buffer)) != -1) {
							bos.write(buffer, 0, len);
						}
					}
					rawText = new String(bos.toByteArray(), "MS932");
					// ※ 画像は親フォルダにあるものを直接参照するため、ここではコピーしません
				}
				// ZIPファイル(.zip)の場合
				else if (fileName.endsWith(".zip")) {
					// 解凍先のキャッシュディレクトリを参照するベースURL
					baseUrl = "file://" + getCacheDir().getAbsolutePath() + "/";
					try (ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceFile), Charset.forName("MS932"))) {
						ZipEntry entry;
						while ((entry = zis.getNextEntry()) != null) {
							String fullName = entry.getName();
							if (entry.isDirectory()) continue;
							File outFile = new File(getCacheDir(), new File(fullName).getName());
							if (fullName.toLowerCase().endsWith(".txt")) {
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								byte[] buffer = new byte[8192];
								int len;
								while ((len = zis.read(buffer)) != -1) {
									bos.write(buffer, 0, len);
								}
								rawText = new String(bos.toByteArray(), "MS932");
							}
							else if (isImage(fullName)) {
								// ZIP内の画像はキャッシュフォルダへコピー
								try (FileOutputStream fos = new FileOutputStream(outFile)) {
									byte[] buffer = new byte[8192];
									int len;
									while ((len = zis.read(buffer)) != -1) {
										fos.write(buffer, 0, len);
									}
								}
							}
						}
					}
				}
				else {
					// 対応していない拡張子の場合
					Logcat.w(logLevel, "未対応のファイル形式です: " + fileName);
					return;
				}
				// ==========================================
				// 共通処理: HTML変換・WebView表示処理
				// ==========================================
				if (rawText != null) {
					Logcat.v(logLevel, "タグを除去して純粋なテキストの長さを測る");
					String textOnly = rawText.replaceAll("<[^>]*>", "").replaceAll("\\s+", "").trim();
					mAozoratextLength = textOnly.length();
					mTextLength = mAozoratextLength + 1;
					Logcat.v(logLevel, "変換開始");
					mAozoraHtml = buildFinalHtml(rawText, imagesBase64, isJapaneseMode);
					Logcat.v(logLevel, "一時ファイルに保存");
					mAozoraFile = new File(getCacheDir(), "temp.html");
					try (FileOutputStream fos = new FileOutputStream(mAozoraFile)) {
						fos.write(mAozoraHtml.getBytes(StandardCharsets.UTF_8));
					}
					catch (IOException e) {
						e.printStackTrace();
					}
					Logcat.v(logLevel, "変換完了");
					runOnUiThread(() -> {
						fileUrl = "file://" + mAozoraFile.getAbsolutePath();
						if (mLoadingSpinner != null) {
							mLoadingSpinner.setVisibility(View.VISIBLE);
							mLoadingSpinner.bringToFront();
						}
						WebSettings settings = leftWebView.getSettings();
						settings.setLoadWithOverviewMode(false);
						settings.setUseWideViewPort(false);
						settings.setAllowFileAccess(true);
						// 正しいベースURLを適用してHTMLを読み込む
						leftWebView.loadUrl(fileUrl);
						setAsyncScrollSet();
						updateLayout();
						GetLeftWidthSpecial(leftWebView, 500 + mAozoratextLength / 200);
					});
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	// 青空文庫のZIPファイルを開く
	private void loadZip(File zipFile) {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		if (mLoadingSpinner != null) {
			mLoadingSpinner.setVisibility(View.VISIBLE);
			mLoadingSpinner.bringToFront();
		}

		executor.execute(() -> {
			try {
				Map<String, String> imagesBase64 = new HashMap<>();
				String rawText = null;
				// Zipのスキャン (Shift_JIS = MS932)
				try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile), Charset.forName("MS932"))) {
					ZipEntry entry;
					while ((entry = zis.getNextEntry()) != null) {
						String fullName = entry.getName();
						if (entry.isDirectory()) continue;
						File outFile = new File(getCacheDir(), new File(fullName).getName());
						if (fullName.toLowerCase().endsWith(".txt")) {
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							byte[] buffer = new byte[8192];
							int len;
							while ((len = zis.read(buffer)) != -1) {
								bos.write(buffer, 0, len);
							}
							rawText = new String(bos.toByteArray(), "MS932");
						}
						else if (isImage(fullName)) {
							// そのままファイルとして保存
							try (FileOutputStream fos = new FileOutputStream(outFile)) {
								byte[] buffer = new byte[8192];
								int len;
								while ((len = zis.read(buffer)) != -1) {
									fos.write(buffer, 0, len);
								}
							}
						}
					}
				}
				if (rawText != null) {
					Logcat.v(logLevel,  "タグを除去して純粋なテキストの長さを測る");
					// タグを除去して純粋なテキストの長さを測る
					String textOnly = rawText.replaceAll("<[^>]*>", "").replaceAll("\\s+", "").trim();
					mAozoratextLength = textOnly.length();
					mTextLength = mAozoratextLength + 1;
					Logcat.v(logLevel, "変換開始");
					// 青空文庫のテキストをHTMLへ変更
					mAozoraHtml = buildFinalHtml(rawText, imagesBase64, isJapaneseMode);
					// 一時ファイルに保存する場合
					Logcat.v(logLevel, "一時ファイルに保存");
					mAozoraFile = new File(getCacheDir(), "temp.html");
					try (FileOutputStream fos = new FileOutputStream(mAozoraFile)) {
						fos.write(mAozoraHtml.getBytes(StandardCharsets.UTF_8));
					}
					catch (IOException e) {
						e.printStackTrace();
					}
					Logcat.v(logLevel,  "変換完了");
					// WebViewの操作だけをメインスレッドで実行
					runOnUiThread(() -> {
						fileUrl = "file://" + mAozoraFile.getAbsolutePath();

						if (mLoadingSpinner != null) {
							mLoadingSpinner.setVisibility(View.VISIBLE);
							mLoadingSpinner.bringToFront();
						}
						WebSettings settings = leftWebView.getSettings();
						// コンテンツを画面幅にフィットさせる機能を「オフ」にする
						settings.setLoadWithOverviewMode(false);
						settings.setUseWideViewPort(false);
						settings.setAllowFileAccess(true);
						leftWebView.loadUrl(fileUrl);
						setAsyncScrollSet();
						updateLayout();
						GetLeftWidthSpecial(leftWebView, 500 + mAozoratextLength / 200);
					});
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private boolean isImage(String name) {
		String n = name.toLowerCase();
		return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg");
	}

	// 青空文庫のテキストをHTMLへ変更
	private String buildFinalHtml(String rawText, Map<String, String> images, boolean isVertical) {
		// 青空文庫のテキストをHTMLのタグをつけて変更
		String body = parseAozora(rawText, images);
		// 方向に応じた変数の設定
		String writingMode = isVertical ? "vertical-rl" : "horizontal-tb";
		String bodyPadding = isVertical ? "40px 5%" : "5% 40px";
		String fontFamily = isVertical ? "serif" : "sans-serif";
		// インデント方向の切り替え(縦書きはmargin-top、横書きはmargin-left)
		String indentDir = isVertical ? "top" : "left";
		String doublelineDir = isVertical ? "right" : "bottom";
		// 二重傍線の方向切り替え
		String overlineBorder = isVertical ? "border-right: 3px double #000;" : "border-top: 3px double #000;";
		String overlinePadding = isVertical ? "padding-right: 2px;" : "padding-top: 2px;";

		return "<html><head>" +
			"<meta charset='UTF-8'>" + 
			"<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
			"<style>" +
			"  body {" +
			"   writing-mode: " + writingMode + ";" +
			"   -webkit-writing-mode: " + writingMode + ";" +
			"   padding: " + bodyPadding + ";" +
			"   line-height: 1.8;" +
			"   font-family: " + fontFamily + ";" +
			"   line-break: strict;" +
			"   word-break: break-all;" +
			"   hanging-punctuation: allow-end;" +
			"  }" + 
			"  p {" +
			"   margin: 0;" +
			"   padding: 0;" +
			"   text-indent: 1em;" +
			"  }" +
			"  .indent-1 { margin-" + indentDir + ": 1em; }" +
			"  .indent-2 { margin-" + indentDir + ": 2em; }" +
			"  .indent-3 { margin-" + indentDir + ": 3em; }" +
			"  .dot { text-emphasis: filled sesame; -webkit-text-emphasis: filled sesame; }" +
			"  .warichu { font-size: 0.7em; line-height: 1.2; display: inline-block; vertical-align: middle; }" +
			"  img { " +
			"   max-width: 100%; " +
			"   max-height: 80vh; " +
			"   width: auto; " +
			"   height: auto; " +
			"   display: block; " +
			"   margin: 20px auto; " +
			"   object-fit: contain; " +
			"  }"+
			".inline-h2 {"+
			"  font-weight: bold;"+
			"  display: inline-block;"+
			"  vertical-align: top;"+
			"  padding: " + (isVertical ? "0 0.2em" : "0.2em 0") + ";"+
			"}"+
			".right-small {"+
			"  font-size: 0.6em;"+
			"  display: inline-block;"+
			"  vertical-align: " + (isVertical ? "super" : "top") + ";"+ 
			"  line-height: 1;"+
			"}"+
			".left-small {" +
			"  font-size: 0.6em;" +
			"  display: inline-block;" +
			"  vertical-align: " + (isVertical ? "sub" : "bottom") + ";" + 
			"  line-height: 1;" +
			"  margin-left: " + (isVertical ? "0" : "2px") + ";" +
			"}" +
			".double-overline {"+
			"  " + overlineBorder +
			"  " + overlinePadding +
			"  display: inline;"+
			"  line-height: 1;"+
			"  -webkit-box-decoration-break: clone;"+
			"  box-decoration-break: clone;"+
			"}"+
			".tcy {"+
			"  -webkit-text-combine: horizontal;"+
			"  text-combine-upright: all;"+
			"  margin: 0 0.1em; "+
			"}"+
			".madowaki {"+
			"	border: 1px solid;"+
			"	float: right; width: 1em; ..."+
			"}"+

			"[class^='jisume-'] {"+
			"  display: block;"+
			"  margin: 1em 0;"+
			"}"+
			".jisume-30 { max-width: 30em; }"+
			".jisume-20 { max-width: 20em; }"+
			".bousen {"+
			"    text-decoration: underline;"+
			"}"+
			".bousen-left {" +
			"    text-decoration: underline;" +
			"    text-underline-position: left;" +
			"}" +
			".shiromaru-boten {"+
			"    -webkit-text-emphasis: open circle;"+
			"    text-emphasis: open circle;"+
			"    -webkit-text-emphasis-color: currentColor;"+
			"    text-emphasis-color: currentColor;"+
			"    text-emphasis-position: over right;"+
			"}"+
			".small-kana {"+
			"    display: inline-block;"+
			"    transform: scale(0.8);"+
			"    vertical-align: middle;"+
			"    line-height: 1;"+
			"}"+
			".gaiji {"+
			"    font-size: 0.9em;"+
			"}"+
			"</style>" +
			"</head><body>" + body + "</body></html>";
	}

	//［＃「対象文字列」にタグ名］という形式をHTMLに変換する
	private static String convertAozoraTag(String text, String tagName, String className) {
		// 正規表現は「タグそのもの」だけをターゲットにする
		Pattern p = Pattern.compile("［＃「(.*?)」に" + tagName + "］");
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		int lastEnd = 0;

		while (m.find()) {
			// タグの中身
			String target = m.group(1);
			// 現在のタグの開始位置
			int tagStart = m.start();
			// タグの直前までの文章を一旦キープ
			String beforeTag = text.substring(lastEnd, tagStart);
			// 直前の文章が「対象文字列」で終わっているか確認
			if (beforeTag.endsWith(target)) {
				// 対象文字列の直前までをappend
				sb.append(beforeTag.substring(0, beforeTag.length() - target.length()));
				// 対象文字列をHTMLタグで囲んでappend
				sb.append("<span class='").append(className).append("'>").append(target).append("</span>");
			}
			else {
				// 万が一、直前にその文字がない場合はそのまま(安全策)
				sb.append(beforeTag);
			}
			lastEnd = m.end();
		}
		sb.append(text.substring(lastEnd));
		return sb.toString();
	}

	private static String convertKanbun(String text) {
		if (text == null) return "";
		// 送り仮名の変換
		// 括弧内の文字を抽出して、送り仮名用のクラスを付与
		text = text.replaceAll("［＃（(.*?)）］", "<span class='okurigana'>$1</span>");
		// 返り点の変換: ［＃レ］, ［＃一］, ［＃二］, ［＃上］ など
		// 文字の左下(または右下)に小さく表示するためのクラス
		text = text.replaceAll("［＃([一二三四五上下中レ]+)］", "<sub class='kaeriten'>$1</sub>");
		return text;
	}

	private static final Map<String, String> RANGE_TAG_MAP = Map.ofEntries(
		Map.entry("太字", "span class='bold'"),
		Map.entry("わり注", "span class='warichu'"),
		Map.entry("大見出し", "h1"),
		Map.entry("中見出し", "h2"),
		Map.entry("小見出し", "h3"),
		Map.entry("大きめの文字", "span style='font-size:1.2em'"),
		Map.entry("小さめの文字", "span style='font-size:0.8em'"),
		Map.entry("地付き", "div style='text-align: end;'"),
		Map.entry("キャプション", "figcaption style='font-style: italic; text-align: center'"),
		Map.entry("縦中横", "span class='tcy'"),
		Map.entry("窓書き", "div class='madowaki'")
	);

	private static String convertMidashiFast(String text) {
		if (text == null || !text.contains("［＃")) return text;

		for (Map.Entry<String, String> entry : RANGE_TAG_MAP.entrySet()) {
			String key = entry.getKey();
			String tagValue = entry.getValue();
			// 属性部分を除去して純粋なタグ名を取得 (例: "span class='...'" -> "span")
			String tagName = tagValue.contains(" ") ? tagValue.substring(0, tagValue.indexOf(" ")) : tagValue;
			// (?:...) 非キャプチャグループを使い、中身のテキストを $1 で取れるように固定
			String pattern = "(?s)［＃(?:ここから)?" + key + "］(.*?)［＃(?:ここで)?" + key + "終わ?り］";
			// 置換実行
			// $1 が中身のテキスト。開始タグには属性を含め、終了タグは純粋なタグ名のみ
			text = text.replaceAll(pattern, "<" + tagValue + ">$1</" + tagName + ">");
		}
		return text;
	}

	private static String applyRangeNotes(String text) {
		for (Map.Entry<String, String> entry : RANGE_TAG_MAP.entrySet()) {
			String key = entry.getKey();
			String tag = entry.getValue();
			// 開始タグと終了タグ(終わり/終りの両方に対応)を組み立て
			// ［＃太字］(.*?)［＃太字終わ?り］
			String pattern = "［＃" + key + "］(.*?)［＃" + key + "終わ?り］";
			// HTMLタグの形式に合わせて置換(spanなどは開始タグに属性が入るため分割)
			String startTag = "<" + tag + ">";
			// "span class=..." から "span" だけ抽出
			String endTag = "</" + tag.split(" ")[0] + ">";
			text = text.replaceAll(pattern, startTag + "$1" + endTag);
		}
		return text;
	}

	private static final Map<String, String> TAG_MAP = Map.ofEntries(
		Map.entry("太字", "b"),
		Map.entry("中見出し", "h2"),
		Map.entry("大見出し", "h1"),
		Map.entry("小見出し", "h3"),
		Map.entry("斜体", "i"),
		Map.entry("上付き小文字", "sup"),
		Map.entry("下付き小文字", "sub"),
		Map.entry("縦中横", "span class='tcy'"),
		Map.entry("窓書き", "div class='madowaki'"),
		Map.entry("行右小書き", "sup style='font-size: 0.7em'"),
		Map.entry("行左小書き", "sub style='font-size: 0.7em'")
	);

	private static String applyAozoraNotes(String text) {
		if (text == null || !text.contains("［＃")) return text;

		StringBuffer sb = new StringBuffer(text.length());
		int cursor = 0;

		while (true) {
			int startBracket = text.indexOf("［＃", cursor);
			if (startBracket == -1) break;
			// 注記の手前までの本文を「必ず」追加する
			sb.append(text, cursor, startBracket);
			int endBracket = text.indexOf("］", startBracket);
			if (endBracket == -1) {
				// 閉じ括弧がない場合は、残りを全部足して終了
				break;
			}
			// 注記の中身を解析
			String noteContent = text.substring(startBracket, endBracket + 1);
			// 「」を含む見出し系の処理
			if (noteContent.contains("「") && noteContent.contains("」は")) {
				int titleStart = noteContent.indexOf("「") + 1;
				int titleEnd = noteContent.indexOf("」は");
				int cmdStart = titleEnd + 2;
				int cmdEnd = noteContent.length() - 1;

				if (titleStart > 0 && titleEnd > titleStart) {
					String target = noteContent.substring(titleStart, titleEnd);
					String command = noteContent.substring(cmdStart, cmdEnd);
					String tagName = TAG_MAP.get(command);
					if (tagName != null) {
						// sb の「直前に追加した本文」から target を探して置換
						int lastContentStart = sb.length() - (startBracket - cursor);
						String lastContent = sb.substring(lastContentStart);
						int pos = lastContent.lastIndexOf(target);
						if (pos != -1) {
							// targetの手前まで切り詰める
							sb.setLength(lastContentStart + pos);
							sb.append("<").append(tagName).append(">")
							  .append(target)
							  .append("</").append(tagName.split(" ")[0]).append(">");
							sb.append(lastContent.substring(pos + target.length()));
						}
					}
					else {
						// 未定義なら注記をそのまま出す
						sb.append(noteContent);
					}
				}
				else {
					sb.append(noteContent);
				}
			}
			else {
				// 見出し系以外の注記(［＃改ページ］など)も、そのまま保持して本文消失を防ぐ
				sb.append(noteContent);
			}
			cursor = endBracket + 1;
		}
		// 最後の注記から文末までを必ず追加
		if (cursor < text.length()) {
			sb.append(text.substring(cursor));
		}
		return sb.toString();
	}

	private String applyAozoraLayout(String text) {
		if (text == null) return "";

		// (ここから～終わり)の置換 
		java.util.regex.Pattern pBig = java.util.regex.Pattern.compile("［＃ここから(?:([０-９0-9]+)段階)?大きな文字］(.*?)［＃ここで?(?:[０-９0-9]+段階)?大きな文字終わ?り］", java.util.regex.Pattern.DOTALL);
		java.util.regex.Matcher mBig = pBig.matcher(text);
		StringBuffer sbBig = new StringBuffer();
		while (mBig.find()) {
			int level = (mBig.group(1) == null) ? 1 : parseSafeInt(mBig.group(1));
			double fontSize = 1.0 + (level * 0.2);
			// $2 の代わりに Matcher.quoteReplacement(mBig.group(2)) を使い、安全に中身を保持
			String replacement = String.format("<span style=\"font-size: %.1fem; display: inline-block; line-height: 1.4; vertical-align: middle;\">%s</span>", fontSize, mBig.group(2));
			mBig.appendReplacement(sbBig, java.util.regex.Matcher.quoteReplacement(replacement));
		}
		mBig.appendTail(sbBig);
		text = sbBig.toString();

		java.util.regex.Pattern pSmall = java.util.regex.Pattern.compile("［＃ここから(?:([０-９0-9]+)段階)?小さな文字］(.*?)［＃ここで?(?:[０-９0-9]+段階)?小さな文字終わ?り］", java.util.regex.Pattern.DOTALL);
		java.util.regex.Matcher mSmall = pSmall.matcher(text);
		StringBuffer sbSmall = new StringBuffer();
		while (mSmall.find()) {
			int level = (mSmall.group(1) == null) ? 1 : parseSafeInt(mSmall.group(1));
			double fontSize = Math.max(0.5, 1.0 - (level * 0.1));
			String replacement = String.format("<small style=\"font-size: %.1fem; display: inline-block; line-height: 1.4; vertical-align: middle;\">%s</small>", fontSize, mSmall.group(2));
			mSmall.appendReplacement(sbSmall, java.util.regex.Matcher.quoteReplacement(replacement));
		}
		mSmall.appendTail(sbSmall);
		text = sbSmall.toString();
		// 傍点の置換
		text = text.replaceAll("(?s)［＃丸傍点］(.*?)(［＃丸傍点終わり］)", "<span style=\"-webkit-text-emphasis-style: filled circle; text-emphasis-style: filled circle;\">$1</span>");
		text = text.replaceAll("(?s)［＃白丸傍点］(.*?)(［＃白丸傍点終わり］)", "<span style=\"-webkit-text-emphasis-style: open circle; text-emphasis-style: open circle;\">$1</span>");
		// 行単位の処理
		String[] lines = text.split("\n", -1);
		StringBuffer sb = new StringBuffer();
		java.util.Stack<Integer> indentStack = new java.util.Stack<>();
		indentStack.push(0);

		java.util.regex.Pattern pIndent = java.util.regex.Pattern.compile("［＃.*?([0-9０-９]+).*?字下げ.*?］");
		java.util.regex.Pattern pLift = java.util.regex.Pattern.compile("［＃地から.*?([0-9０-９]+).*?字上げ.*?］");

		for (String line : lines) {
			// タグ掃除の前に単発の「小さな文字」があるか判定
			boolean isSmallLine = (line.contains("小さな文字］") && !line.contains("ここから"));
			boolean isBigLine = (line.contains("大きな文字］") && !line.contains("ここから"));
			boolean isBottomAligned = line.contains("［＃地付き］");
			boolean isLifted = false;
			int liftValue = 0;

			if (line.contains("字下げ終わり")) {
				if (indentStack.size() > 1) indentStack.pop();
			}
			else if (line.contains("改丁") || line.contains("改ページ")) {
				indentStack.clear();
				indentStack.push(0);
			}
			else {
				java.util.regex.Matcher mIndent = pIndent.matcher(line);
				if (mIndent.find()) indentStack.push(parseSafeInt(mIndent.group(1)));
				java.util.regex.Matcher mLift = pLift.matcher(line);
				if (mLift.find()) {
					isLifted = true;
					liftValue = parseSafeInt(mLift.group(1));
				}
			}
			// 本文の掃除(［＃...］を取り除く)
			String cleanLine = line.replaceAll("［＃[^］]+］", "").trim();

			int currentIndent = indentStack.peek();
			String style = String.format("margin-block: 0; line-height: inherit; display: block; %s%s%s", isBottomAligned ? "text-align: end; " : "", isLifted ? "text-align: end; margin-inline-end: " + liftValue + "em; " : "", (!isBottomAligned && !isLifted && currentIndent > 0) ? "margin-inline-start: " + currentIndent + "em; " : "");

			if (cleanLine.isEmpty() && !isBottomAligned && !isLifted) {
				sb.append("<br>");
			}
			else {
				sb.append("<div style=\"").append(style).append("\">");
				if (isSmallLine) {
					sb.append("<small style=\"display: inline-block; line-height: 1.8;\">").append(cleanLine).append("</small>");
				}
				else if (isBigLine) {
					sb.append("<span style=\"font-size: 1.2em; display: inline-block; line-height: 1.8; margin-inline: 0.1em;\">").append(cleanLine).append("</span>");
				}
				else {
					sb.append(cleanLine.isEmpty() ? "&nbsp;" : cleanLine);
				}
				sb.append("</div>");
			}
		}
		return sb.toString();
	}

	private int parseSafeInt(String s) {
		try {
			String n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKC);
			return Integer.parseInt(n.replaceAll("[^0-9]", "").trim());
		}
		catch (Exception e) {
			return 1;
		}
	}

	private static final Pattern PATTERN_SIZE_CHUKI = 
		Pattern.compile("［＃「([^「」]+)」は([０-９0-9]+)段階(大きく?|小さく?)な文字］");

	private String convertSpecificSizeChuki(String text) {
		if (text == null || !text.contains("段階")) return text;

		StringBuffer sb = new StringBuffer(text.length());
		Matcher m = PATTERN_SIZE_CHUKI.matcher(text);
		int lastEnd = 0;

		while (m.find()) {
			String targetWord = m.group(1);
			int level = parseSafeInt(m.group(2));
			String type = m.group(3);
			int matchStart = m.start();
			int targetLen = targetWord.length();
			// 注記の直前にその言葉があるか確認
			if (matchStart >= targetLen && 
				text.substring(matchStart - targetLen, matchStart).equals(targetWord)) {
				// 直前までのテキストを保持
				sb.append(text, lastEnd, matchStart - targetLen);
				// サイズ計算（1段階 0.2em 刻み）
				double fontSize;
				if (type.startsWith("大")) {
					fontSize = 1.0 + (level * 0.2);
				}
				else {
					fontSize = 1.0 - (level * 0.1);
					if (fontSize < 0.5) fontSize = 0.5;
				}
				// 置換
				sb.append(String.format("<span style=\"font-size: %.1fem; line-height: 1.2; display: inline-block; vertical-align: middle;\">", fontSize))
				  .append(targetWord)
				  .append("</span>");
			}
			else {
				// 一致しない場合はそのまま
				sb.append(text, lastEnd, m.end());
			}
			lastEnd = m.end();
		}
		sb.append(text.substring(lastEnd));
		return sb.toString();
	}

	private static String applyRuby(String text) {
		if (text == null || !text.contains("《")) return text == null ? "" : text;
		try {
			// ｜(縦棒) による明示的指定
			text = text.replaceAll("[｜|]([^《｜|<>]+?)《([^》]+)》", "<ruby>$1<rt>$2</rt></ruby>");
			// 実体参照や外字(カッコ含む)
			// [^<>]+? とすることで、すでにHTML化された部分を親文字にしないようにガード
			text = text.replaceAll("(&#x[0-9a-fA-F]+;)《([^》]+)》", "<ruby>$1<rt>$2</rt></ruby>");
			text = text.replaceAll("(（[^）<>]+）)《([^》]+)》", "<ruby>$1<rt>$2</rt></ruby>");
			// [^<>《》|｜\\s] = タグ文字、ルビ記号、区切り棒、空白 以外にマッチさせる
			// これによりすでに <ruby> になった場所の英字などは無視される
			text = text.replaceAll("([\\p{IsHan}々ヶ〆]+)《([^》]+)》", "<ruby>$1<rt>$2</rt></ruby>");
			text = text.replaceAll("([ァ-ヶー]+)《([^》]+)》", "<ruby>$1<rt>$2</rt></ruby>");
			// 英数字ルビは特にHTMLタグ(ruby, rt, span）と干渉しやすいため「直前が > ではない(タグの終わりではない)」という否定戻り読みを活用するか親文字の条件を厳しくする
			text = text.replaceAll("(?<![a-zA-Z>])([Ａ-Ｚa-zA-Z0-9０-９]+)《([^》]+)》", "<ruby>$1<rt>$2</rt></ruby>");
		}
		catch (Exception e) {
			android.util.Log.e("RubyError", "Rendering Crash Prevention", e);
		}
		return text;
	}

	private String safeAozoraConvert(String text) {
		// まず「範囲型」を処理(中身に他の注記が含まれている可能性があるため)［＃割り注］などは先に処理して中身を守る
		text = text.replaceAll("(?s)（?［＃割り注］〔?(.*?)〕?［＃割り注終わ?り］）?", "<small class='warichu'>（$1）</small>");
		return text;
	}

	private static final Pattern PATTERN_KOUTEI = Pattern.compile("［＃「([^「」]+)」に「([^「」]+)」の注記］");

	private String convertKouteiChuki(String text) {
		if (text == null || !text.contains("の注記］")) return text;

		StringBuffer sb = new StringBuffer(text.length());
		Matcher m = PATTERN_KOUTEI.matcher(text);
		int lastEnd = 0;

		while (m.find()) {
			String targetWord = m.group(1);
			String note = m.group(2);
			int matchStart = m.start();
			int targetLen = targetWord.length();
			// 注記の直前にある文字列がtargetWordと一致するか確認
			if (matchStart >= targetLen && 
				text.substring(matchStart - targetLen, matchStart).equals(targetWord)) {
				// 一致した場合：直前の単語を含めて置換
				sb.append(text, lastEnd, matchStart - targetLen);
				sb.append("<ruby class='koutei-chuki'>")
				  .append(targetWord)
				  .append("<rt>")
				  .append(note)
				  .append("</rt></ruby>");
			}
			else {
				// 一致しない場合：注記部分だけそのまま残す(または消去する)
				sb.append(text, lastEnd, m.end());
			}
			lastEnd = m.end();
		}
		sb.append(text.substring(lastEnd));
		return sb.toString();
	}

	// 正規表現を「注記付き終わり」の形式に変更
	private static final Pattern PATTERN_CHUKI_OWARI = 
		Pattern.compile("［＃注記付き］(.*?)［＃「([^「」]+)」の注記付き終わり］", Pattern.DOTALL);

	private String convertChukiOwari(String text) {
		if (text == null || !text.contains("注記付き終わり］")) return text;

		StringBuffer sb = new StringBuffer(text.length());
		Matcher m = PATTERN_CHUKI_OWARI.matcher(text);
		int lastEnd = 0;

		while (m.find()) {
			// 先にマッチ箇所の前までを append
			sb.append(text, lastEnd, m.start());
			String parentText = m.group(1);
			String note = m.group(2);
			// 親文字の中にある「外字」を先に解決する必要があるため、
			// ここで以前作った外字変換メソッドを呼び出すのがコツです
			String processedParent = convertSingleGaiji(parentText); 
			// HTML化
			sb.append("<ruby class='chuki-chuki'>")
			  .append(processedParent)
			  .append("<rt>")
			  .append(note)
			  .append("</rt></ruby>");
			lastEnd = m.end();
		}
		sb.append(text.substring(lastEnd));
		return sb.toString();
	}

	private static final Pattern PATTERN_BOUSEN_LEFT = Pattern.compile("［＃「([^「」]+)」の左に傍線］");

	private String convertBousenLeft(String text) {
		if (text == null || !text.contains("の左に傍線］")) return text;

		StringBuffer sb = new StringBuffer(text.length());
		Matcher m = PATTERN_BOUSEN_LEFT.matcher(text);
		int lastEnd = 0;

		while (m.find()) {
			String targetWord = m.group(1);
			int matchStart = m.start();
			int targetLen = targetWord.length();
			// 注記の直前にある文字列が一致するか確認
			if (matchStart >= targetLen && 
				text.substring(matchStart - targetLen, matchStart).equals(targetWord)) {
				// 一致した場合は直前の単語を<span>で囲む
				sb.append(text, lastEnd, matchStart - targetLen);
				sb.append("<span class='bousen-left'>")
				  .append(targetWord)
				  .append("</span>");
			}
			else {
				// 一致しない場合はそのまま残す
				sb.append(text, lastEnd, m.end());
			}
			lastEnd = m.end();
		}
		sb.append(text.substring(lastEnd));
		return sb.toString();
	}

	// ヘルパー：親文字の中に含まれる ※［＃...］ を文字に置換する
	private String convertSingleGaiji(String target) {
		// ここで以前の formatGaiji 的な処理、
		// 例えば 「二の字点」を「々」に置換するなどの処理を行う
		if (target.contains("二の字点")) return target.replaceAll("※［＃二の字点.*?］", "々");
		return target;
	}

	// 注記の文字列を、本文中の「ルビ」や「縦棒」を含んだ状態でもマッチできるように正規表現を動的に組み立てるメソッド
	private String createFlexibleRegex(String targetText) {
		StringBuffer rb = new StringBuffer();
		for (char c : targetText.toCharArray()) {
			// 各文字の前に「｜」がある可能性を考慮
			rb.append("｜?"); 
			// 文字自体をエスケープして追加(記号などに対応)
			rb.append(Pattern.quote(String.valueOf(c)));
			// 各文字の後に「《...》」がある可能性を考慮
			rb.append("(?:《[^》]+》)?");
		}
		return rb.toString();
	}

	// クラスのフィールドとして定数化
	private static final Pattern PATTERN_MIDASHI_MARKER = Pattern.compile("［＃「([^」]+)」は([中小大])見出し］");

	private String convertMidashiFinal(String text) {
		if (text == null || !text.contains("見出し］")) return text;

		StringBuffer sb = new StringBuffer(text.length());
		Matcher m = PATTERN_MIDASHI_MARKER.matcher(text);
		int lastEnd = 0;

		while (m.find()) {
			// 注記の直前までのテキストを一旦確定させてappend
			sb.append(text, lastEnd, m.start());
			// 見出しにしたい文字列
			String targetText = m.group(1);
			String type = m.group(2);
			String tag = type.equals("大") ? "h1" : (type.equals("中") ? "h2" : "h3");
			// sb(これまでに処理した全テキスト)の末尾からtargetText を探す
			// 直前30〜50文字程度を調べればルビや注記があっても十分カバー可能
			int sbLen = sb.length();
			int searchRange = Math.max(0, sbLen - 50);
			String lookBack = sb.substring(searchRange);
			// ルビや注記を無視して比較するためにlookBack側を一時的にクリーニング
			// 判定用なので、sb 自体は書き換えない
			String cleanedLookBack = lookBack.replaceAll("《[^》]+》|［＃[^］]+］|[｜|]", "");
			if (cleanedLookBack.endsWith(targetText)) {
				// 一致した場合：実際の本文(ルビ等を含む生テキスト)の開始位置を特定する
				// 非常に単純かつ高速な後ろ向きのスキャン
				int actualMatchStart = findActualStart(lookBack, targetText);
				if (actualMatchStart != -1) {
					int absoluteStart = searchRange + actualMatchStart;
					String matchedBody = sb.substring(absoluteStart);
					// 置換：sb を切り詰めてからタグで囲んだものを append
					sb.setLength(absoluteStart);
					sb.append("<").append(tag).append(">")
					  .append(matchedBody)
					  .append("</").append(tag).append(">");
				}
			}
			lastEnd = m.end();
		}
		sb.append(text.substring(lastEnd));
		return sb.toString();
	}

	// ルビや注記を含む生テキストの中からtargetTextに相当する開始位置を後ろから探す
	private int findActualStart(String lookBack, String targetText) {
		int targetIdx = targetText.length() - 1;
		int lookIdx = lookBack.length() - 1;
		// ターゲットの文字を後ろから一文字ずつ照合していく
		while (targetIdx >= 0 && lookIdx >= 0) {
			char l = lookBack.charAt(lookIdx);
			// 無視すべき文字なら lookIdx だけ進める
			if (l == '》') {
				lookIdx = lookBack.lastIndexOf('《', lookIdx);
				if (lookIdx == -1) break;
				lookIdx--;
				continue;
			}
			if (l == '］') {
				lookIdx = lookBack.lastIndexOf('［', lookIdx);
				if (lookIdx == -1) break;
				lookIdx--;
				continue;
			}
			if (l == '｜' || l == '|') {
				lookIdx--;
				continue;
			}
			// 文字比較
			if (l == targetText.charAt(targetIdx)) {
				targetIdx--;
				lookIdx--;
			} else {
				// 不一致なら即座に終了
				return -1;
			}
		}
		return (targetIdx < 0) ? lookIdx + 1 : -1;
	}

	// クラスのフィールドとして定数化
	private static final Pattern PATTERN_MIDASHI_NOTE = Pattern.compile("［＃「([^」]+)」は同行中見出し］");
	// 行右小書きも後方参照(\\1)を使わずJava側で判定する
	private static final Pattern PATTERN_SMALL_RIGHT = Pattern.compile("［＃「([^」]+)」は行右小書き］");
	private static final Pattern PATTERN_SMALL_LEFT = Pattern.compile("［＃「([^」]+)」は行左小書き］");

	private String convertSpecialNotes(String text) {
		if (text == null || !text.contains("［＃")) return text;
		// 同行中見出しの処理(StringBuffer 1回パス)
		text = processBackwardsNote(text, PATTERN_MIDASHI_NOTE, "inline-h2");
		// 行右小書きの処理 (StringBuffer 1回パス)
		text = processBackwardsNote(text, PATTERN_SMALL_RIGHT, "right-small");
		// 行左小書きの処理 (StringBuffer 1回パス)
		text = processBackwardsNote(text, PATTERN_SMALL_LEFT, "left-small");
		return text;
	}

	// 注記の直前にある単語を判定してタグで囲む共通処理
	private String processBackwardsNote(String text, Pattern pattern, String className) {
		StringBuffer sb = new StringBuffer(text.length());
		Matcher m = pattern.matcher(text);
		int lastEnd = 0;

		while (m.find()) {
			String target = m.group(1);
			int noteStart = m.start();
			// 注記の直前にあるはずの「本文」を探す(createFlexibleRegexを使わず直接比較)
			// ルビ等が含まれる可能性を考慮し直前15文字程度をスキャン
			int searchRange = Math.max(0, noteStart - 25); 
			String lookBackArea = text.substring(searchRange, noteStart);
			// targetがルビ等を含んでいる可能性を考慮した簡易的な後方一致確認
			int foundIdx = lookBackArea.lastIndexOf(target);
			if (foundIdx != -1) {
				// 見つかった位置を text 全体のインデックスに変換
				int actualStart = searchRange + foundIdx;
				// 注記までの未処理分を追加
				sb.append(text, lastEnd, actualStart);
				// タグ付け
				sb.append("<span class='").append(className).append("'>")
				  .append(text, actualStart, noteStart)
				  .append("</span>");
				lastEnd = m.end();
			}
			else {
				// 見つからない場合は注記をスルー(他の処理に任せる)
			}
		}
		sb.append(text.substring(lastEnd));
		return sb.toString();
	}

	private String convertDoubleOverline(String text) {
		// 検索パターン：［＃「...」に二重傍線］
		Pattern p = Pattern.compile("［＃「([^」]+)」に二重傍線］");
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		int lastEnd = 0;

		while (m.find()) {
			// 注記の直前までの文章を一旦追加
			sb.append(text, lastEnd, m.start());
			String targetText = m.group(1);
			// ルビ・縦棒スルーロジック
			String regex = createFlexibleRegex(targetText);
			// 直前の本文から、注記に最も近いマッチを探す
			Pattern pBody = Pattern.compile(regex);
			String currentContent = sb.toString();
			Matcher mBody = pBody.matcher(currentContent);
			int matchStart = -1;
			int matchEnd = -1;
			while (mBody.find()) {
				matchStart = mBody.start();
				matchEnd = mBody.end();
			}
			// マッチが見つかった場合(注記の直前5文字以内にあるか確認)
			if (matchStart != -1 && matchEnd >= sb.length() - 5) {
				String matched = currentContent.substring(matchStart, matchEnd);				// 本文側をタグで書き換える
				sb.setLength(matchStart);
				sb.append("<span class='double-overline'>").append(matched).append("</span>");
			}
			lastEnd = m.end();
		}
		sb.append(text.substring(lastEnd));
		return sb.toString();
	}

	private String convertGaiji(String text) {
		if (text == null || !text.contains("［＃")) return text;
		// ※ があってもなくても、［＃...］ の中身を group(1) に取る
		Pattern p = Pattern.compile("※?［＃(.+?)］"); 
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		int lastEnd = 0;

		while (m.find()) {
			sb.append(text, lastEnd, m.start());
			String content = m.group(1); 
			// Unicode直接指定(最優先)
			Matcher mUni = Pattern.compile("[Uu]\\+([0-9A-Fa-f]{4,5})").matcher(content);
			// 面区点番号の抽出
			Matcher mMen = Pattern.compile("(\\d-\\d{1,2}-\\d{1,2})").matcher(content);
			boolean hasMenkuten = mMen.find();

			if (mUni.find()) {
				sb.append("&#x").append(mUni.group(1)).append(";");
			}
			// ここで山括弧を確実に拾う
			else if (content.contains("始め二重山括弧")) {
				sb.append("《");
			} 
			else if (content.contains("終わり二重山括弧")) {
				sb.append("》");
			}
			else if (content.contains("始め角括弧")) {
				sb.append("［");
			} 
			else if (content.contains("終わり角括弧")) {
				sb.append("］");
			}
			else if (content.contains("始めきっこう（亀甲）括弧")) {
				sb.append("〔");
			} 
			else if (content.contains("終わりきっこう（亀甲）括弧")) {
				sb.append("〕");
			}
			else if (content.contains("始め二重括弧")) {
				sb.append("『");
			}
			else if (content.contains("終わり二重括弧")) {
				sb.append("』");
			}
			else if (content.contains("縦線")) {
				sb.append("｜");
			}
			else if (content.contains("井げた")) {
				sb.append("＃");
			}
			else if (content.contains("米印")) {
				sb.append("※");
			}
			else if (content.contains("歌記号")) {
				// 歌記号なら音符に変換
				sb.append("♪");
			}
			else if (content.contains("ローマ数字") || content.contains("丸")) {
				String result = resolveMenkuten(hasMenkuten ? mMen.group(1) : null, content);
				if (result != null) {
					sb.append(result);
				}
				else {
					// resolveMenkutenで変換できなかった「丸」を含む説明文などは、括弧書きへ
					sb.append("<span class=\"gaiji\">（").append(extractGaijiText(content)).append("）</span>");
				}
			}
			else if (hasMenkuten || isGaijiDescription(content)) {
				// 面区点がある、または文字説明である場合は「（説明）」にする
				sb.append("<span class=\"gaiji\">（").append(extractGaijiText(content)).append("）</span>");
			}
			else {
				// それ以外(改ページ、見出し、ルビ等)はそのまま返すか、別メソッドで処理
				sb.append(m.group(0)); 
			}
			lastEnd = m.end();
		}
		sb.append(text.substring(lastEnd));
		return sb.toString();
	}

	// 外字説明文かどうかの判定を強化
	private boolean isGaijiDescription(String content) {
		// まず、外字ではない「編集指示」系のキーワードが含まれていたら即座に false を返す
		if (content.length() > 50) return false;
		// 明らかなレイアウト指示キーワードが含まれていれば除外
		String[] layoutKeywords = {"傍点", "ルビ", "見出し", "字詰め", "罫囲み", "窓書き", "地上げ", "地下げ", "ページの左右中央", "縦中横", "行右小書き", "行左小書き", "太字", "斜体", "ママ", "下付き小文字", "上付き小文字", "分数", "キャプション"};
		for (String kw : layoutKeywords) {
			if (content.contains(kw)) return false;
		}
		return content.contains("水準") || content.contains("に代えて") || content.contains("＋") || content.contains("の右") || content.contains("の左") || content.contains("の形") || content.startsWith("「");
	}

	//「」の中身、あるいはカンマより前のテキストを抽出
	private String extractGaijiText(String content) {
		// 「 」で囲まれた部分があれば、それを繋げて説明とする
		String[] parts = content.split("[、，]");
		if (parts.length > 0) {
			String desc = parts[0];
			// あまりに長い場合は「」の中身だけに絞る
			if (desc.length() > 30 && desc.contains("「")) {
				int s = desc.indexOf("「");
				int e = desc.lastIndexOf("」");
				if (s != -1 && e > s) return desc.substring(s + 1, e);
			}
			return desc;
		}
		return content;
	}

	// 注記のテキスト内容から対応する丸付き文字(Unicode)を返す
	private String getCircleCharByText(String content) {
		// 「丸」という言葉が含まれていない場合は、丸付き数字ではないと判断
		if (!content.contains("丸") && !content.contains("まる")) return null;
		// 1文字の判定(「丸公」など)や、特定の単語(「丸付き印」など)に対応
		if (content.contains("\u5370")) return "\u329E"; // 印 -> ㊞
		if (content.contains("\u516c")) return "\u32AB"; // 公 -> ㊫
		if (content.contains("\u6b63")) return "\u329B"; // 正 -> ㊛ (丸正)
		if (content.contains("\u6ce8")) return "\u329F"; // 注 -> ㊟
		if (content.contains("\u512a")) return "\u329D"; // 優 -> ㊝
		if (content.contains("\u79d8")) return "\u3299"; // 秘 -> ㊙
		if (content.contains("\u7279")) return "\u3314"; // 特 -> ㊔
		// 曜日シリーズ
		if (content.contains("\u65e5")) return "\u3290"; // 日
		if (content.contains("\u6708")) return "\u3291"; // 月
		if (content.contains("\u706b")) return "\u3292"; // 火
		if (content.contains("\u6c34")) return "\u3293"; // 水
		if (content.contains("\u6728")) return "\u3294"; // 木
		if (content.contains("\u91d1")) return "\u3295"; // 金
		if (content.contains("\u571f")) return "\u3296"; // 土
		// 左右・男女
		if (content.contains("\u53f3")) return "\u32A8"; // 右
		if (content.contains("\u5de6")) return "\u32A7"; // 左
		if (content.contains("\u7537")) return "\u329A"; // 男
		if (content.contains("\u5973")) return "\u329B"; // 女
		// 該当がない場合は、汎用的な( )囲みで返す(フォントがない環境への配慮)
		// 「丸い」→「(い)」など
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("丸(?:付き)?(?:「(.+?)」|(.))").matcher(content);
		if (m.find()) {
			String target = (m.group(1) != null) ? m.group(1) : m.group(2);
			return "(" + target + ")";
		}
		return null;
	}

	// 漢数字の丸付き文字を判定して返す
	private String getKanjiCircleChar(String content) {
		// 「丸」という言葉が含まれていない場合は、丸付き数字ではないと判断
		if (!content.contains("丸") && !content.contains("まる")) return null;
		// Unicodeが直接指定されている場合(U+3280 ～ U+3289)
		java.util.regex.Matcher mUni = java.util.regex.Pattern.compile("[Uu]\\+(328[0-9A-Fa-f])").matcher(content);
		if (mUni.find()) {
			// 例: 3280 を 16進数として数値化し、charにキャスト
			return String.valueOf((char) Integer.parseInt(mUni.group(1), 16));
		}
		// テキスト名称で指定されている場合(フォント不足対策のため判定文字もエスケープ)
		if (content.contains("\u4e00")) return "\u3280"; // 一
		if (content.contains("\u4e8c")) return "\u3281"; // 二
		if (content.contains("\u4e09")) return "\u3282"; // 三
		if (content.contains("\u56db")) return "\u3283"; // 四
		if (content.contains("\u4e94")) return "\u3284"; // 五
		if (content.contains("\u516d")) return "\u3285"; // 六
		if (content.contains("\u4e03")) return "\u3286"; // 七
		if (content.contains("\u516b")) return "\u3287"; // 八
		if (content.contains("\u4e5d")) return "\u3288"; // 九
		if (content.contains("\u5341")) return "\u3289"; // 十
		return null;
	}

	private String resolveMenkuten(String code, String fullContent) {
		// 漢数字シリーズのチェック(ガード条件付き)
		String kanjiCircle = getKanjiCircleChar(fullContent);
		if (kanjiCircle != null) return kanjiCircle;
		// 一般的な丸付き文字(ガード条件付き)
		String commonCircle = getCircleCharByText(fullContent);
		if (commonCircle != null) return commonCircle;
		// 「ローマ数字」または「丸」という明示的なキーワードがある場合のみ数字を抽出
		boolean isRomanRequested = fullContent.contains("ローマ数字");
		boolean isCircleRequested = fullContent.contains("丸") || fullContent.contains("まる");

		if (isRomanRequested || isCircleRequested) {
			int num = extractFirstNumber(fullContent);
			if (num > 0) {
				// アラビア数字の丸数字 (①〜⑳)
				if (isCircleRequested && num <= 20) {
					String[] circles = {"\u2460","\u2461","\u2462","\u2463","\u2464","\u2465","\u2466","\u2467","\u2468","\u2469","\u246A","\u246B","\u246C","\u246D","\u246E","\u246F","\u2470","\u2471","\u2472","\u2473"};
					return circles[num-1];
				}
				// ローマ数字
				if (isRomanRequested) {
					if (num <= 12) {
						return getRomanNumericMap().get(num);
					}
					else {
						return convertToRoman(num);
					}
				}
			}
		}
		// どの特定の記号変換にも当てはまらない場合は null を返す
		// これにより呼び出し側のconvertGaijiで「（説明文）」への処理に流れるようになる
		return null;
	}

	// 1-12までの特殊記号マップ
	private Map<Integer, String> getRomanNumericMap() {
		Map<Integer, String> map = new HashMap<>();
		String[] romans = {"Ⅰ","Ⅱ","Ⅲ","Ⅳ","Ⅴ","Ⅵ","Ⅶ","Ⅷ","Ⅸ","Ⅹ","Ⅺ","Ⅻ"};
		for (int i = 0; i < romans.length; i++) {
			map.put(i + 1, romans[i]);
		}
		return map;
	}

	private String convertToRoman(int num) {
		if (num <= 0) return String.valueOf(num);
		// 基本的なローマ数字の単位(大きい順)
		int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
		String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < values.length; i++) {
			while (num >= values[i]) {
				num -= values[i];
				sb.append(symbols[i]);
			}
		}
		return sb.toString();
	}

	// 文字列から最初の数字(1文字以上)を抽出してintで返す
	// 例：「丸18、1-13-18」 -> 18
	// 例：「丸付き公」 -> -1
	private int extractFirstNumber(String content) {
		if (content == null || content.isEmpty()) return -1;
		// \\d+ は「1回以上繰り返される数字」にマッチします
		java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\d+");
		java.util.regex.Matcher m = p.matcher(content);
		if (m.find()) {
			try {
				return Integer.parseInt(m.group());
			}
			catch (NumberFormatException e) {
				return -1;
			}
		}
		return -1;
	}

	private static String simplifyGaiji(String text) {
		if (text == null) return "";
		// 先頭の「※」をオプションにしつつ、最短一致で注記を特定
		// 内部に「入る」や「キャプション」が含まれる場合は画像タグとみなして除外(否定先読み)
		// 外字特有のキーワード(水準、U+、JIS、漢字構成表現など)が含まれる場合のみマッチ
		String regex = "※?［＃((?![^］]*?(?:入る|キャプション|見出し))[^［］]+?(?:「[^」]+」|第[1-4]水準|U\\+[0-9A-F]+|\\d-\\d-\\d|[^］]+?＋[^］]+?)[^［］]*?)］";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();

		while (m.find()) {
			String content = m.group(1);
			// 抽出ロジック
			String g_text = "";
			if (content.contains("「")) {
				int s = content.indexOf("「");
				int e = content.lastIndexOf("」");
				if (s != -1 && e > s) {
					g_text = content.substring(s + 1, e);
				}
			}
			// 「」がない場合は、カンマ区切りの先頭
			if (g_text.isEmpty()) {
				g_text = content.split("[、，]")[0];
			}
			// 安全策：抽出したテキストが長すぎる、または不自然な場合は置換しない
			if (g_text.length() > 20 || g_text.contains("入る")) {
				m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
				continue;
			}
			m.appendReplacement(sb, Matcher.quoteReplacement("（" + g_text + "）"));
		}
		m.appendTail(sb);
		return sb.toString();
	}

		// クラスのフィールドとして定数化
		private static final Pattern PATTERN_CAPTION_NOTE = 
			Pattern.compile("［＃「([^」]+)」(?:はキャプション|のキャプション付きの[^］]+?入る)］");

	private String convertCaptions(String text) {
		if (text == null || !text.contains("キャプション")) return text;

		StringBuffer sb = new StringBuffer(text.length());
		Matcher m = PATTERN_CAPTION_NOTE.matcher(text);
		int lastEnd = 0;

		while (m.find()) {
			// 注記の直前までのテキストを確定させる
			sb.append(text, lastEnd, m.start());
			// 「」の中身
			String captionText = m.group(1); 
			int noteStart = m.start();
			int captionLen = captionText.length();
			// 直前のテキスト(sb の末尾)が captionText と一致するかチェック
			int sbLen = sb.length();
			if (sbLen >= captionLen) {
				// 直前の文字列を切り出して比較
				String tail = sb.substring(sbLen - captionLen);
				if (tail.equals(captionText)) {
					// 重複がある場合：末尾の重複分を削ってからタグを追加
					sb.setLength(sbLen - captionLen);
				}
			}
			// タグの追加
			sb.append("<figcaption class=\"aozora-caption\">")
			  .append(captionText)
			  .append("</figcaption>");
			lastEnd = m.end();
		}
		// 残りのテキストを追加
		sb.append(text.substring(lastEnd));
		return sb.toString();
	}

	// 青空文庫の画像指示構文をパースする
	private String preProcessAozoraImages(String text) {
		if (text == null || !text.contains("［＃")) return text;

		StringBuffer sb = new StringBuffer(text.length());
		int cursor = 0;
		while (true) {
			int start = text.indexOf("［＃", cursor);
			if (start == -1) break;
			// 注記の開始から「本当の閉じ括弧」までをスタックで特定
			int end = findClosingBracket(text, start);
			if (end == -1) break;
			String fullNote = text.substring(start, end + 1);
			// 構造チェック：「(」があり、かつ末尾が「)入る］」であるか
			// ※特定のキーワード(fig)ではなく、構文の終端「）入る］」で判断
			if (fullNote.contains("（") && fullNote.endsWith("）入る］")) {
				sb.append(text, cursor, start);
				sb.append(buildImageTagFromNote(fullNote));
				cursor = end + 1;
			}
			else {
			// 画像でなければ、そのまま次の注記へ
				sb.append(text, cursor, start + 2);
				cursor = start + 2;
			}
		}
		sb.append(text.substring(cursor));
		return sb.toString();
	}

	private String buildImageTagFromNote(String fullNote) {
		// 殻［＃ と ］ を除去
		String inner = fullNote.substring(2, fullNote.length() - 1);
		// ラベル内の括弧を考慮し最後から探す
		int openParen = inner.lastIndexOf("（");
		int closeParen = inner.lastIndexOf("）入る");
		if (openParen != -1 && closeParen > openParen) {
			// ラベル部分(外字注記が含まれていても、ここで文字に還元する)
			String labelRaw = inner.substring(0, openParen);
			String label = labelRaw.replaceAll("［＃「(.+?)」、.+?］", "$1").replace("※", "").trim();
			// ファイル名部分(カンマがあれば最初の要素を取る)
			String info = inner.substring(openParen + 1, closeParen).trim();
			String fileName = info.split("、")[0].trim();
			return String.format("<img src=\"%s\" alt=\"%s\">", fileName, label);
		}
		return fullNote;
	}

	// 入れ子に対応した閉じ括弧探し
	private static int findClosingBracket(String text, int start) {
		int depth = 0;
		for (int i = start; i < text.length(); i++) {
			if (text.startsWith("［＃", i)) {
				depth++;
				i++; 
			}
			else if (text.charAt(i) == '］') {
				depth--;
				if (depth == 0) return i;
			}
		}
		return -1;
	}

	private String convertDakuten(String input) {
		if (input == null) return null;

		final Pattern DAKUTEN_PATTERN = Pattern.compile("※［＃濁点付き片仮名(.)、[^］]+］");
		Matcher matcher = DAKUTEN_PATTERN.matcher(input);
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			// キャプチャした1文字を取得
			String targetChar = matcher.group(1);
			// 結合用濁点 (U+3099) を付与して置換
			// HTMLとして出力する場合は <span> を付けておくとCSSで制御しやすい
			String replacement = "<span class=\"gaiji\">" + targetChar + "&#x3099;</span>";
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private String convertSpecialKana(String input) {
		if (input == null) return null;
		// 「小書き」または「半濁点付き」の片仮名1文字をキャプチャする正規表現
		final Pattern KANA_PATTERN = Pattern.compile("※［＃(小書き|半濁点付き)片仮名(.)、[^］]+］");
		Matcher matcher = KANA_PATTERN.matcher(input);
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String type = matcher.group(1);
			String targetChar = matcher.group(2);

			String replacement;
			if ("半濁点付き".equals(type)) {
				// 半濁点付きの場合、Unicodeの結合用半濁点（U+309A）を付与
				replacement = "<span class='handakuten-kana'>" + targetChar + "\u309A</span>";
			}
			else {
				// 小書きの場合は既存のロジック通り
				replacement = "<span class='small-kana'>" + targetChar + "</span>";
			}
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private String convertGaijiRuby(String input) {
		if (input == null) return null;

		final Pattern GAIJI_RUBY_PATTERN = Pattern.compile("(※?\\s*［＃([^］]+)］)《([^》]+)》");
		Matcher matcher = GAIJI_RUBY_PATTERN.matcher(input);
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String fullGaijiTag = matcher.group(1);
			String gaijiContent = matcher.group(2);
			String rubyText = matcher.group(3);
			Log.v(TAG, "rubyText=" + rubyText);
			// 外字部分を先にHTML化(またはプレースホルダ化)
			String processedGaiji = formatGaiji(fullGaijiTag, gaijiContent);
			// 外字全体を親文字としてrubyタグを構成
			String replacement = "<ruby>" + processedGaiji + "<rt>" + rubyText + "</rt></ruby>";
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private String formatGaiji(String full, String content) {
		// ここで外字を画像や結合文字に変換する
		return "<span class=\"gaiji\">" + full + "</span>";
	}

	// 感嘆符疑問符の注記をキャプチャ
	private String convertSymbols(String input) {
		if (input == null) return null;

		final Pattern BANCHI_PATTERN = Pattern.compile("※［＃感嘆符疑問符、[^］]+］");
		Matcher matcher = BANCHI_PATTERN.matcher(input);
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			// Unicodeの ⁉ (U+2049) に置換
			// もしくはHTMLエンティティ "&#x2049;"
			String replacement = "\u2049"; 
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private String applyAozoraTags(String text) {
		if (text == null || !text.contains("［＃")) return text;
		// 対応表：注記名 -> 開始タグ
		Map<String, String> tags = new LinkedHashMap<>();
		tags.put("斜体", "i");
		tags.put("太字", "b");
		tags.put("横組み", "span style='writing-mode:horizontal-tb; display:inline-block;'");
		tags.put("傍点", "span class='em-dot'");

		for (Map.Entry<String, String> entry : tags.entrySet()) {
			String name = entry.getKey();
			String tag = entry.getValue();
			// 開始タグと終了タグの生成
			String openHtml = "<" + tag + ">";
			String closeHtml = "</" + tag.split(" ")[0] + ">";
			// 入れ子や「終わり」が混在していても順番に置換
			// ※［＃斜体］ -> <i> / ※［＃斜体終わり］ -> </i>
			text = text.replaceAll("※?［＃" + name + "］", openHtml);
			text = text.replaceAll("※?［＃" + name + "終わり］", closeHtml);
		}
		return text;
	}

	// 外字注記の置換後、ルビの直前に残ってしまった「※」を掃除する。
	private String cleanupGaijiMarkersBeforeRuby(String text) {
		// ※
		if (text == null || !text.contains("\u203B")) return text;
		// 実体参照 (&#x...;) の直前の ※ を消す
		text = text.replaceAll("\u203B(&#x[0-9A-Fa-f]{4,5};)", "$1");
		// ローマ数字の直前の ※ を消す
		text = text.replaceAll("\u203B([\u2160-\u216B])", "$1");
		// アラビア数字の丸数字の直前の ※ を消す
		text = text.replaceAll("\u203B([\u2460-\u2473])", "$1");
		// 漢数字の丸文字 (U+3280-3289) の直前の ※ を消す
		text = text.replaceAll("\u203B([\u3280-\u3289])", "$1");
		// 特殊な丸付き文字などが含まれる U+3200ブロックの直前の ※ を消す
		text = text.replaceAll("\u203B([\u3290-\u32AF])", "$1");
		// (公) や (い) のように括弧で置換されたものの直前の ※ を消す
		text = text.replaceAll("\u203B(\\()", "$1");
		// ルビ直前の掃除「※文字《ルビ》」となっている場合の ※ を消す
		return text.replaceAll("\u203B([^\u203B\u300A]+?\u300A)", "$1");
	}

	private String removeTeihonNotes(String text) {
		if (text == null || !text.contains("は底本では")) return text;
		// 一時的な退避用マーカー
		final String PROTECT_MARKER = "___GAIJI_PROTECTED___";
		StringBuffer sb = new StringBuffer(text);
		int index;
		// 「後ろから検索」は維持（削除によるズレを防ぐため）
		while ((index = sb.lastIndexOf("は底本では")) != -1) {
			int start = sb.lastIndexOf("［＃", index);
			if (start == -1) {
				// ［＃ が見つからない「は底本では」は、注記外の本文の可能性があるので壊さないように一時的にマーカーへ置換して検索対象から外す
				sb.replace(index, index + 5, PROTECT_MARKER);
				continue;
			}
			int end = findProperClosingBracket(sb, start);
			if (end != -1) {
				// 判定：直後にルビ《 があるか？
				boolean isGaijiRuby = (end + 1 < sb.length() && sb.charAt(end + 1) == '《');
				if (isGaijiRuby) {
					// 外字ルビなので、一時的にマーカーに置換してループを回避
					sb.replace(index, index + 5, PROTECT_MARKER);
				}
				else {
					// 純粋な底本注記なので、根こそぎ消す
					int deleteStart = start;
					if (deleteStart > 0 && sb.charAt(deleteStart - 1) == '※') {
						deleteStart--;
					}
					sb.delete(deleteStart, end + 1);
				}
			}
			else {
				// 括弧が閉じられていない異常系：検索対象から外す
				sb.replace(index, index + 5, PROTECT_MARKER);
			}
		}
		// 最後に、退避させていたマーカーを元の文字列に戻す
		String result = sb.toString().replace(PROTECT_MARKER, "は底本では");
		return result;
	}

	// 開始位置 start から、入れ子を考慮して対応する ］の位置を返す
	private int findProperClosingBracket(StringBuffer sb, int start) {
		int depth = 0;
		for (int i = start; i < sb.length(); i++) {
			// ［＃ を見つけたら深さを増やす
			if (i + 1 < sb.length() && sb.charAt(i) == '［' && sb.charAt(i + 1) == '＃') {
				depth++;
				i++; 
			}
			else if (sb.charAt(i) == '］') {
				depth--;
				if (depth == 0) return i;
			}
		}
		return -1;
	}

	private String convertYokogumi(String text) {
		String regex = "［＃ここから横組み］([\\s\\S]*?)［＃ここで横組み終わり］";
		String style = "display: inline-block !important; " +
			"transform: rotate(-90deg) !important; " + 
			"transform-origin: top left !important; " +
			"white-space: nowrap !important; " +
			"margin-left: 1.5em !important; " +
			"font-family: sans-serif !important;";
		String replacement = "<span style=\"" + style + "\">$1</span>";
		return text.replaceAll(regex, replacement);
	}

	private String convertJisume(String text) {
		if (text == null || !text.contains("字詰め")) return text;
		// 「ここから○字詰め」を <div class="jisume-n"> に変換
		// ［＃ここから(\d+)字詰め］
		Pattern pStart = Pattern.compile("［＃ここから(\\d+)字詰め］");
		Matcher mStart = pStart.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (mStart.find()) {
			String num = mStart.group(1);
			// class名はCSS側で .jisume-30 { width: 30em; } のように定義することを想定
			mStart.appendReplacement(sb, "<div class=\"jisume-" + num + "\">");
		}
		mStart.appendTail(sb);
		String midText = sb.toString();
		// 「ここで字詰め終わり」を </div> に変換
		// ［＃ここで字詰め終わり］
		return midText.replace("［＃ここで字詰め終わり］", "</div>");
	}

	// 青空文庫のテキストをHTMLのタグをつけて変更
	private String parseAozora(String text, Map<String, String> images) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// 不要なヘッダー・フッターのカット
		String[] parts = text.split("-{5,}");
		if (parts.length >= 3) {
			// 最初の部分(タイトル)と、3番目の部分(本文)を結合
			// 4番目以降(フッター)がある場合は無視する構成
			text = parts[0] + parts[2];
		}
		Logcat.v(logLevel, "開始時: " + text.length());
		text = preProcessAozoraImages(text);
		Logcat.v(logLevel, "preProcessAozoraImages: " + text.length());
		text= removeTeihonNotes(text);
		Logcat.v(logLevel, "removeTeihonNotes: " + text.length());
		text = convertYokogumi(text);
		text = convertJisume(text);
		text = safeAozoraConvert(text);
		Logcat.v(logLevel, "safeAozoraConvert: " + text.length());
		text = convertMidashiFinal(text);
		Logcat.v(logLevel, "convertMidashiFinal: " + text.length());
		text = convertSpecialNotes(text);
		Logcat.v(logLevel, "convertSpecialNotes: " + text.length());
		text = convertDoubleOverline(text);
		Logcat.v(logLevel, "convertDoubleOverline: " + text.length());
		text = convertCaptions(text);
		Logcat.v(logLevel, "convertCaptions: " + text.length());
		text = convertGaijiRuby(text);
		text = text.replaceAll("※?［＃二の字点、1-2-22］", "〻");
		text = text.replaceAll("※［＃感嘆符二つ、[^］]+］", "\u203C");
		text = convertDakuten(text);
		text = convertSpecialKana(text);
		text = convertSymbols(text);
		text = convertAozoraTag(text, "傍線", "bousen");
		text = convertAozoraTag(text, "白丸傍点", "shiromaru-boten");
		text = convertKouteiChuki(text);
		text = convertChukiOwari(text);
		text = convertBousenLeft(text);
		text = convertSpecificSizeChuki(text);
		text = convertGaiji(text);
		Logcat.v(logLevel, "convertGaiji: " + text.length());
		text = simplifyGaiji(text);
		Logcat.v(logLevel, "simplifyGaiji: " + text.length());
		Logcat.v(logLevel, "漢文処理");
		text = convertKanbun(text);
		Logcat.v(logLevel, "convertKanbun: " + text.length());
		text = applyRangeNotes(text);
		Logcat.v(logLevel, "applyRangeNotes: " + text.length());
		// ルビ直前の邪魔な「※」だけを掃除する
		text = cleanupGaijiMarkersBeforeRuby(text);
		// ルビ処理(｜あり・なし両対応)
		Logcat.v(logLevel, "ルビ処理");
		text = applyRuby(text);
		Logcat.v(logLevel, "applyRuby: " + text.length());
		text = applyAozoraNotes(text);
		Logcat.v(logLevel, "applyAozoraNotes: " + text.length());
		text = applyAozoraTags(text);
		Logcat.v(logLevel, "傍点処理");
		text = convertAozoraTag(text, "傍点", "dot");
		text = convertAozoraTag(text, "太字", "bold");
		text = convertAozoraTag(text, "キャプション", "caption");
		text = convertAozoraTag(text, "二重傍線", "span style='text-decoration: double underline;'");
		Logcat.v(logLevel, "convertAozoraTag: " + text.length());
		// 後置型見出しの処理
		Logcat.v(logLevel, "後置型見出しの処理");
		text = convertMidashiFast(text);
		Logcat.v(logLevel, "convertMidashiFast: " + text.length());
		text = applyAozoraLayout(text);
		Logcat.v(logLevel, "applyAozoraLayout: " + text.length());
		// 残ってしまった未対応の注記タグ［＃...］をすべて削除
		Logcat.v(logLevel, "text.replace: " + text.length());
		Logcat.v(logLevel, "未対応の注記タグ［＃...］をすべて削除");
		text = text.replaceAll("［＃[^］]+］", "");
		Logcat.v(logLevel, "最後に改行を<br>に変換");
		// 最後に改行を<br>に変換
		// ただし、divやh1タグの直後の改行で隙間が空きすぎるのを防ぐため調整
		text = text.replace("\n", "<br>\n");
		text = text.replace("</div><br>", "</div>");
		text = text.replace("</h1><br>", "</h1>");
		text = text.replace("</h2><br>", "</h2>");
		Logcat.v(logLevel, "キー(ファイル名)が含まれるsrcを置換");
		for (Map.Entry<String, String> entry : images.entrySet()) {
			// キー(ファイル名)が含まれるsrcを置換
			text = text.replace("src=\"" + entry.getKey() + "\"", "src=\"file://" + mAozoraFile.getAbsolutePath() + "\"");

		}
		return text;
	}
}

