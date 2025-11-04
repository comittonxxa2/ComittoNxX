package src.comitton.common;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;

import jp.dip.muracoro.comittonx.BuildConfig;
import jp.dip.muracoro.comittonx.R;
import src.comitton.fileaccess.FileAccess;
import src.comitton.fileview.data.FileData;

import org.mozilla.universalchardet.UniversalDetector;

public class DEF {
	private static final String TAG = "DEF";

	public static final boolean DEBUG = BuildConfig.DEBUG;
	public static final String BUILD_DATE = (new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())).format(BuildConfig.BUILD_DATE);

	public static final Semaphore sSemaphoe = new Semaphore(1);

	public static final String DOWNLOAD_URL = "https://github.com/ComittoNxA/ComittoNxX";
	public static final String API_RECENT_RELEASE = "https://api.github.com/repos/ComittoNxA/ComittoNxX/releases/latest";
	public static final String APP_DOWNLOADS = "https://api.github.com/repos/ComittoNxA/ComittonxX/releases";
	public static final int MESSAGE_FILE_DELETE = 1000;
	public static final int MESSAGE_RECORD_DELETE = 1001;
	public static final int MESSAGE_LASTPAGE = 1002;
	public static final int MESSAGE_SHORTCUT = 1003;
	public static final int MESSAGE_EDITSERVER = 1004;
	public static final int MESSAGE_DOWNLOAD = 1005;
	public static final int MESSAGE_MARKER = 1006;
	public static final int MESSAGE_CLOSE = 1007;
	public static final int MESSAGE_RESUME = 1008;
	public static final int MESSAGE_RENAME = 1010;
	public static final int MESSAGE_LISTMODE = 1011;
	public static final int MESSAGE_SORT = 1012;
	public static final int MESSAGE_FILE_LONGCLICK = 1013;
	public static final int MESSAGE_RECORD_LONGCLICK = 1014;
	public static final int MESSAGE_FILE_RENAME = 1015;
	public static final int MESSAGE_MOVE_PATH_EROOR = 1016;
	public static final int MESSAGE_RESETLOCAL = 1017;

	public static final int MESSAGE_IMAGE = 1;
	public static final int MESSAGE_TEXT = 2;
	public static final int MESSAGE_EXPAND = 3;

	public static final int ERROR_CODE_MALLOC_FAILURE = -1001;
	public static final int ERROR_CODE_CACHE_COUNT_LIMIT_EXCEEDED = -1002;
	public static final int ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE = -1003;
	public static final int ERROR_CODE_CACHE_NOT_INITIALIZED = -1004;
	public static final int  ERROR_CODE_CACHE_IS_FULL  = -1005;
	public static final int  ERROR_CODE_IMAGE_TYPE_NOT_SUPPORT  = -1006;
	public static final int  ERROR_CODE_USER_CANCELED  = -1007;

	public static final int RETURN_CODE_ERROR_READ_DATA = -2001;
	public static final int RETURN_CODE_TERMINATED = -2002;

	public static final int HMSG_LOAD_END = 1;
	public static final int HMSG_READ_END = 2;
	public static final int HMSG_PROGRESS = 3;
	public static final int HMSG_ERROR = 4;
	public static final int HMSG_CACHE = 5;
	public static final int HMSG_LOADING = 6;
	public static final int HMSG_NOISE = 7;
	public static final int HMSG_NOISESTATE = 8;
	public static final int HMSG_THUMBNAIL = 9;
	public static final int HMSG_LOADFILELIST = 10;
	public static final int HMSG_UPDATEFILELIST = 11;
	public static final int HMSG_DRAWENABLE = 13;
	public static final int HMSG_TX_PARSE = 14;
	public static final int HMSG_TX_LAYOUT = 15;
	public static final int HMSG_EPUB_PARSE = 16;
	public static final int HMSG_HTML_PARSE = 17;
	public static final int HMSG_RECENT_RELEASE = 18;
	public static final int HMSG_TOAST = 19;
	public static final int HMSG_WORKSTREAM = 20;
	public static final int HMSG_SUB_MESSAGE = 21;
	public static final int HMSG_ERROR_MALLOC = 22;
	public static final int HMSG_APP_DOWNLOADS = 23;
	public static final int HMSG_SET_LISTVIEW_INDEX = 24;
	public static final int HMSG_FILE_STATUS = 25;
	public static final int HMSG_PROGRESS_IMAGE = 26;
	public static final int HMSG_PROGRESS_TEXT = 27;
	public static final int HMSG_PROGRESS_EXPAND = 28;
	public static final int HMSG_PROGRESS_CANCEL = 29;

	public static final int HMSG_EVENT_READTIMER = 200;
	public static final int HMSG_EVENT_EFFECT = 201;
	public static final int HMSG_EVENT_SCROLL = 202;
	public static final int HMSG_EVENT_LOADING = 203;
	public static final int HMSG_EVENT_AUTOPLAY = 204;
	public static final int HMSG_EVENT_TOUCH_ZOOM = 205;
	public static final int HMSG_EVENT_TOUCH_TOP = 206;
	public static final int HMSG_EVENT_TOUCH_BOTTOM = 207;
	public static final int HMSG_EVENT_PAGE = 208;
	public static final int HMSG_EVENT_ATTENUATE = 209;
	public static final int HMSG_EVENT_MOMENTIUM = 210;
	public static final int HMSG_EVENT_EFFECT_NEXT = 211;
	public static final int HMSG_EVENT_SCROLL_NEXT = 212;
	public static final int HMSG_EVENT_LOADING_NEXT = 213;
	public static final int HMSG_EVENT_LONG_TAP = 214;
	public static final int HMSG_EVENT_FLOATINGICON = 215;
	public static final int HMSG_EVENT_FLOATINGICON_RESET = 216;

	public static final int INTERVAL_DEFAULT = 50;	// Milliseconds
	public static final int INTERVAL_EFFECT = 1;	// Milliseconds
	public static final int INTERVAL_EFFECT_NEXT = 1;	// Milliseconds
	public static final int INTERVAL_SCROLL = 4;	// Milliseconds
	public static final int INTERVAL_SCROLL_NEXT = 4;	// Milliseconds
	public static final int INTERVAL_LOADING = 500;	// Milliseconds
	public static final int INTERVAL_LOADING_NEXT = 150;	// Milliseconds
	public static final int INTERVAL_PAGE = 1;
	public static final int INTERVAL_ATTENUATE = 10;
	public static final int INTERVAL_MOMENTIUM = 10;

	public static final int MENU_HELP = Menu.FIRST + 0;
	public static final int MENU_SETTING = Menu.FIRST + 1;
	public static final int MENU_SIORI = Menu.FIRST + 2;
	public static final int MENU_ABOUT = Menu.FIRST + 3;
	public static final int MENU_DISPDUAL = Menu.FIRST + 4;
	public static final int MENU_DISPHALF = Menu.FIRST + 5;
	public static final int MENU_DISPNORM = Menu.FIRST + 6;
	public static final int MENU_SHORTCUT = Menu.FIRST + 7;
	public static final int MENU_SERVER = Menu.FIRST + 8;
	public static final int MENU_REFRESH = Menu.FIRST + 9;
	public static final int MENU_NOISE = Menu.FIRST + 10;
	public static final int MENU_QUIT = Menu.FIRST + 11;
	public static final int MENU_THUMBDEL = Menu.FIRST + 12;
	public static final int MENU_ONLINE = Menu.FIRST + 13;
	public static final int MENU_MARKER = Menu.FIRST + 14;
	public static final int MENU_SHARE = Menu.FIRST + 15;
	public static final int MENU_SHARER = Menu.FIRST + 16;
	public static final int MENU_SHAREL = Menu.FIRST + 17;
	public static final int MENU_DELSHARE = Menu.FIRST + 18;
	public static final int MENU_REVERSE = Menu.FIRST + 19;
	public static final int MENU_ROTATE = Menu.FIRST + 20;
	public static final int MENU_CHG_OPE = Menu.FIRST + 21;
	public static final int MENU_PAGEWAY = Menu.FIRST + 22;
	public static final int MENU_SCRLWAY = Menu.FIRST + 23;
	public static final int MENU_THUMBSWT = Menu.FIRST + 24;
	public static final int MENU_IMGCONF = Menu.FIRST + 25;
	public static final int MENU_IMGROTA = Menu.FIRST + 26;
	public static final int MENU_IMGVIEW = Menu.FIRST + 27;
	public static final int MENU_IMGSIZE = Menu.FIRST + 28;
	public static final int MENU_PAGESEL = Menu.FIRST + 29;
	public static final int MENU_SHARPEN = Menu.FIRST + 30;
	public static final int MENU_INVERT = Menu.FIRST + 31;
	public static final int MENU_MGNCUT = Menu.FIRST + 32;
	public static final int MENU_MGNCUTCOLOR = Menu.FIRST + 33;
	public static final int MENU_GRAY = Menu.FIRST + 34;
	public static final int MENU_NOTICE = Menu.FIRST + 35;
	public static final int MENU_ADDBOOKMARK = Menu.FIRST + 36;
	public static final int MENU_SELBOOKMARK = Menu.FIRST + 37;
	public static final int MENU_SORT = Menu.FIRST + 38;
	public static final int MENU_ADDDIR = Menu.FIRST + 39;
	public static final int MENU_LISTMODE = Menu.FIRST + 40;
	public static final int MENU_AUTOPLAY = Menu.FIRST + 41;
	public static final int MENU_TOP_SETTING = Menu.FIRST + 42;
	public static final int MENU_SELCHAPTER = Menu.FIRST + 43;
	public static final int MENU_SEARCHTEXT = Menu.FIRST + 44;
	public static final int MENU_SEARCHJUMP = Menu.FIRST + 45;
	public static final int MENU_IMGALGO = Menu.FIRST + 46;
	public static final int MENU_TXTCONF = Menu.FIRST + 47;
	public static final int MENU_BRIGHT = Menu.FIRST + 48;
	public static final int MENU_GAMMA = Menu.FIRST + 49;
	public static final int MENU_BKLIGHT = Menu.FIRST + 50;
	public static final int MENU_SEL_DIR_TREE = Menu.FIRST + 51;
	public static final int MENU_EDIT_TOOLBAR = Menu.FIRST + 52;
	public static final int MENU_DOWLOAD_COUNT = Menu.FIRST + 53;
	public static final int MENU_SETPROFILE = Menu.FIRST + 54;
	public static final int MENU_DELPROFILE = Menu.FIRST + 55;
	public static final int MENU_PROFILE1 = Menu.FIRST + 56;
	public static final int MENU_PROFILE2 = Menu.FIRST + 57;
	public static final int MENU_PROFILE3 = Menu.FIRST + 58;
	public static final int MENU_PROFILE4 = Menu.FIRST + 59;
	public static final int MENU_PROFILE5 = Menu.FIRST + 60;
	public static final int MENU_DISPLAY_POSITION = Menu.FIRST + 61;
	public static final int MENU_CONTRAST = Menu.FIRST + 62;
	public static final int MENU_HUE = Menu.FIRST + 63;
	public static final int MENU_SATURATION = Menu.FIRST + 64;
	public static final int MENU_TAP_PATTERN = Menu.FIRST + 65;
	public static final int MENU_TAP_CLICK = Menu.FIRST + 66;
	public static final int MENU_TAP_SETTING = Menu.FIRST + 67;
	public static final int MENU_CUSTOMKEY_SETTING = Menu.FIRST + 68;
	public static final int MENU_COLORING = Menu.FIRST + 69;
	public static final int MENU_PROFILE6 = Menu.FIRST + 70;
	public static final int MENU_PROFILE7 = Menu.FIRST + 71;
	public static final int MENU_PROFILE8 = Menu.FIRST + 72;
	public static final int MENU_PROFILE9 = Menu.FIRST + 73;
	public static final int MENU_PROFILE10 = Menu.FIRST + 74;
	public static final int MENU_FLOATINGICON_SETTING = Menu.FIRST + 75;
	public static final int MENU_BOOKMARK = Menu.FIRST + 1000;
	public static final int MENU_CHAPTER = Menu.FIRST + 2000;
	public static final int MENU_DIR_TREE = MENU_CHAPTER;

	public static final int MENU_CMARGIN = Menu.FIRST + 101;
	public static final int MENU_CSHADOW = Menu.FIRST + 102;
	public static final int MENU_SETTHUMB = Menu.FIRST + 103;
	public static final int MENU_SETTHUMBCROPPED = Menu.FIRST + 104;
	public static final int MENU_LICENSE = Menu.FIRST + 105;

	public static final String KEY_PAGE_SELECT_TOOLBAR = "PageSelectToolbar";
	public static final String KEY_FLOATINGICON_TOOLBAR = "FloatingIconToolbar";
	public static final int TOOLBAR_LEFTMOST = 1001;
	public static final int TOOLBAR_LEFT100 = 1002;
	public static final int TOOLBAR_LEFT10 = 1003;
	public static final int TOOLBAR_LEFT1 = 1004;
	public static final int TOOLBAR_RIGHT1 = 1005;
	public static final int TOOLBAR_RIGHT10 = 1006;
	public static final int TOOLBAR_RIGHT100 = 1007;
	public static final int TOOLBAR_RIGHTMOST = 1008;
	public static final int TOOLBAR_PAGE_RESET = 1009;

	public static final int TOOLBAR_BOOK_LEFT = 1010;
	public static final int TOOLBAR_BOOK_RIGHT = 1011;
	public static final int TOOLBAR_BOOKMARK_LEFT = 1012;
	public static final int TOOLBAR_BOOKMARK_RIGHT = 1013;
	public static final int TOOLBAR_THUMB_SLIDER = 1014;
	public static final int TOOLBAR_DIR_TREE = 1015;
	public static final int TOOLBAR_TOC = 1016;
	public static final int TOOLBAR_FAVORITE = 1017;
	public static final int TOOLBAR_ADD_FAVORITE = 1018;
	public static final int TOOLBAR_SEARCH = 1019;
	public static final int TOOLBAR_SHARE = 1020;
	public static final int TOOLBAR_SHARE_LEFT_PAGE = 1021;
	public static final int TOOLBAR_SHARE_RIGHT_PAGE = 1022;
	public static final int TOOLBAR_ROTATE = 1023;
	public static final int TOOLBAR_ROTATE_IMAGE = 1024;
	public static final int TOOLBAR_SELECT_THUMB = 1025;
	public static final int TOOLBAR_TRIM_THUMB = 1026;
	public static final int TOOLBAR_CONTROL = 1027;
	public static final int TOOLBAR_MENU = 1028;
	public static final int TOOLBAR_CONFIG = 1029;
	public static final int TOOLBAR_EDIT_TOOLBAR = 1030;
	public static final int TOOLBAR_PROFILE1 = 1031;
	public static final int TOOLBAR_PROFILE2 = 1032;
	public static final int TOOLBAR_PROFILE3 = 1033;
	public static final int TOOLBAR_PROFILE4 = 1034;
	public static final int TOOLBAR_PROFILE5 = 1035;
	public static final int TOOLBAR_PROFILE6 = 1036;
	public static final int TOOLBAR_PROFILE7 = 1037;
	public static final int TOOLBAR_PROFILE8 = 1038;
	public static final int TOOLBAR_PROFILE9 = 1039;
	public static final int TOOLBAR_PROFILE10 = 1040;
	public static final int TOOLBAR_DISMISS = 1100;

	public static final int SHARE_SINGLE = 2001;
	public static final int SHARE_LR = 2002;

	public static final int FLOATING_LEFTMOST = 1001;
	public static final int FLOATING_LEFT100 = 1002;
	public static final int FLOATING_LEFT10 = 1003;
	public static final int FLOATING_LEFT1 = 1004;
	public static final int FLOATING_RIGHT1 = 1005;
	public static final int FLOATING_RIGHT10 = 1006;
	public static final int FLOATING_RIGHT100 = 1007;
	public static final int FLOATING_RIGHTMOST = 1008;
	public static final int FLOATING_PAGE_RESET = 1009;
	public static final int FLOATING_BOOK_LEFT = 1010;
	public static final int FLOATING_BOOK_RIGHT = 1011;
	public static final int FLOATING_BOOKMARK_LEFT = 1012;
	public static final int FLOATING_BOOKMARK_RIGHT = 1013;
	public static final int FLOATING_THUMB_SLIDER = 1014;
	public static final int FLOATING_DIR_TREE = 1015;
	public static final int FLOATING_TOC = 1016;
	public static final int FLOATING_FAVORITE = 1017;
	public static final int FLOATING_ADD_FAVORITE = 1018;
	public static final int FLOATING_SEARCH = 1019;
	public static final int FLOATING_SHARE = 1020;
	public static final int FLOATING_SHARE_LEFT_PAGE = 1021;
	public static final int FLOATING_SHARE_RIGHT_PAGE = 1022;
	public static final int FLOATING_ROTATE = 1023;
	public static final int FLOATING_ROTATE_IMAGE = 1024;
	public static final int FLOATING_SELECT_THUMB = 1025;
	public static final int FLOATING_TRIM_THUMB = 1026;
	public static final int FLOATING_CONTROL = 1027;
	public static final int FLOATING_MENU = 1028;
	public static final int FLOATING_CONFIG = 1029;
	public static final int FLOATING_EDIT_FLOATING = 1030;
	public static final int FLOATING_PROFILE1 = 1031;
	public static final int FLOATING_PROFILE2 = 1032;
	public static final int FLOATING_PROFILE3 = 1033;
	public static final int FLOATING_PROFILE4 = 1034;
	public static final int FLOATING_PROFILE5 = 1035;
	public static final int FLOATING_PROFILE6 = 1036;
	public static final int FLOATING_PROFILE7 = 1037;
	public static final int FLOATING_PROFILE8 = 1038;
	public static final int FLOATING_PROFILE9 = 1039;
	public static final int FLOATING_PROFILE10 = 1040;

	public static final int READ_REQUEST_CODE = 42;
	public static final int WRITE_REQUEST_CODE = 43;
	public static final int OPEN_REQUEST_CODE = 44;
	public static final int REQUEST_SDCARD_ACCESS = 45;
	public static final int REQUEST_SETTING = 101;
	public static final int REQUEST_FILE = 102;
	public static final int REQUEST_HELP = 103;
	public static final int REQUEST_SERVER = 104;
	public static final int REQUEST_IMAGE = 105;
	public static final int REQUEST_TEXT = 106;
	public static final int REQUEST_EXPAND = 107;
	public static final int REQUEST_RECORD = 108;
	public static final int REQUEST_LICENSE = 109;
	public static final int REQUEST_EPUB = 110;
	public static final int REQUEST_CROP = 111;
	public static final int APP_STORAGE_ACCESS_REQUEST_CODE = 501;
	public static final int REQUEST_CODE_ACTION_OPEN_DOCUMENT = 502;
	public static final int REQUEST_CODE_ACTION_OPEN_DOCUMENT_TREE = 503;

	public static final int MAX_SERVER = 10;
	public static final int INDEX_LOCAL = -1;
	public static final int ACCESS_TYPE_LOCAL = -1;
	public static final int ACCESS_TYPE_SMB = 0;
	public static final int ACCESS_TYPE_SAF = 1;
	public static final int ACCESS_TYPE_PICKER = 2;

	public static final int VIEWPT_RIGHTTOP = 0;
	public static final int VIEWPT_LEFTTOP = 1;
	public static final int VIEWPT_RIGHTBTM = 2;
	public static final int VIEWPT_LEFTBTM = 3;
	public static final int VIEWPT_CENTER = 4;

	public static final int ZOOMTYPE_ORIG10 = 0;
	public static final int ZOOMTYPE_ORIG15 = 1;
	public static final int ZOOMTYPE_ORIG20 = 2;
	public static final int ZOOMTYPE_DISP15 = 3;
	public static final int ZOOMTYPE_DISP20 = 4;
	public static final int ZOOMTYPE_DISP25 = 5;
	public static final int ZOOMTYPE_DISP30 = 6;

	public static final int TOOLBAR_NONE = -1;
	public static final int TOOLBAR_PARENT = 0;
	public static final int TOOLBAR_REFRESH = 1;
	public static final int TOOLBAR_THUMBNAIL = 2;
	public static final int TOOLBAR_MARKER = 3;
	//	public static final int TOOLBAR_SERVER = 4;
	public static final int TOOLBAR_ADDDIR = 4;
	public static final int TOOLBAR_EXIT = 5;

	// ページめくり表示方向
	public static final int PAGEWAY_RIGHT = 0; // 右から左
	public static final int PAGEWAY_LEFT = 1; // 左から右

	// スクロール方向
	public static final int SCRLWAY_H = 0; // 横→縦
	public static final int SCRLWAY_V = 1; // 縦→横

	// 拡大方法
	public static final int SCALE_ORIGINAL = 0;
	public static final int SCALE_FIT_WIDTH = 1;
	public static final int SCALE_FIT_HEIGHT = 2;
	public static final int SCALE_FIT_ALL = 3;
	public static final int SCALE_FIT_ALLMAX = 4;
	public static final int SCALE_FIT_SPRMAX = 5;
	public static final int SCALE_FIT_WIDTH2 = 6;
	public static final int SCALE_FIT_ALL2 = 7;
	public static final int SCALE_PINCH = 8;

	// 最終ページの動作
	public static final int LASTMSG_CLOSE = 0;
	public static final int LASTMSG_DIALOG = 1;
	public static final int LASTMSG_NEXT = 2;

	// ソート方法
	public static final int ZIPSORT_NONE = 0; // ソートなし
	public static final int ZIPSORT_FILEMGR = 1; // ファイル名順(ディレクトリ混在)
	public static final int ZIPSORT_FILESEP = 2; // ファイル名順(ディレクトリ分離)
	public static final int ZIPSORT_NEWMGR = 3; // 新しい順(ディレクトリ混在)
	public static final int ZIPSORT_NEWSEP = 4; // 新しい順(ディレクトリ分離)
	public static final int ZIPSORT_OLDMGR = 5; // 古い順(ディレクトリ混在)
	public static final int ZIPSORT_OLDSEP = 6; // 古い順(ディレクトリ分離)
	public static final int ZIPSORT_SHUFFLEMGR = 7; // シャッフル(ディレクトリ混在)
	public static final int ZIPSORT_SHUFFLESEP = 8; // シャッフル(ディレクトリ分離)
	public static final int SPECIFICSORT_NONE = 0;
	public static final int SPECIFICSORT_FILEACSEND = 1;
	public static final int SPECIFICSORT_FILEDESSEND = 2;
	public static final int SPECIFICSORT_NEWDATE = 3;
	public static final int SPECIFICSORT_OLDDATE = 4;
	public static final int SPECIFICSORT_SHUFFLE = 5;
	public static final int SPECIFICSORT_READINGHIGH = 6;
	public static final int SPECIFICSORT_READINGLOW = 7;

	// ファイル拡張子
	public static final String EXTENSION_SETTING = ".set";

	public static final String KEY_EXPORTSETTING = "ExportSetting";
	public static final String KEY_IMPORTSETTING = "ImportSetting";

	public static final String KEY_CONFHELP = "ConfHelp";
	public static final String KEY_FILEHELP = "FileHelp";
	public static final String KEY_FCLRHELP = "FClrHelp";
	public static final String KEY_IMAGEHELP = "ImageHelp";
	public static final String KEY_IDTLHELP = "IDtlHelp";
	public static final String KEY_COMMHELP = "CommHelp";
	public static final String KEY_NOISEHELP = "NoiseHelp";
	public static final String KEY_CACHEHELP = "CacheHelp";
	public static final String KEY_TEXTHELP = "TextHelp";
	public static final String KEY_IMTXHELP = "ImgTxtHelp";
	public static final String KEY_ITDTLHELP = "ImgTxtDtlHelp";
	public static final String KEY_ITCLRHELP = "ImgTxtClrHelp";
	public static final String KEY_RECHELP = "RecordHelp";

	public static final String KEY_CSTUPDATE = "CustomUpdate";

	public static final String KEY_LISTROTA = "ListRota";
	public static final String KEY_LISTSORT = "ListSort";
	public static final String KEY_FONTTITLE = "FontTitleSp";
	public static final String KEY_FONTMAIN = "FontMainSp";
	public static final String KEY_FONTSUB = "FontSubSp";
	public static final String KEY_FONTTILE = "FontTileSp";
	public static final String KEY_BKPARENT = "BackParent";
	public static final String KEY_ITEMMRGN = "ItemMarginSp";

	public static final String KEY_RDIRVIEW = "RDView";
	public static final String KEY_RBMVIEW = "RBView";
	public static final String KEY_RHISTVIEW = "RHView";
	public static final String KEY_SELECTOR = "Selector";
	public static final String KEY_RECLOCAL = "RHLocalRec";
	public static final String KEY_RECSAMBA = "RHSambaRec";

	public static final String KEY_PRESET = "Preset";

	public static final String KEY_TXTCOLOR = "TxtColor";
	public static final String KEY_DIRCOLOR = "DirColor";
	public static final String KEY_BEFCOLOR = "BefColor";
	public static final String KEY_NOWCOLOR = "NowColor";
	public static final String KEY_AFTCOLOR = "AftColor";
	public static final String KEY_RRBCOLOR = "RrbColor";
	public static final String KEY_IMGCOLOR = "ImgColor";
	public static final String KEY_INFCOLOR = "InfColor";
	public static final String KEY_BAKCOLOR = "BakColor";
	public static final String KEY_BSFCOLOR = "BsfColor";
	public static final String KEY_BSECOLOR = "BseColor";
	public static final String KEY_FIFCOLOR = "FifColor";
	public static final String KEY_FIBCOLOR = "FibColor";

	public static final String KEY_TXTRGB = "TxtRGB";
	public static final String KEY_DIRRGB = "DirRGB";
	public static final String KEY_BEFRGB = "BefRGB";
	public static final String KEY_NOWRGB = "NowRGB";
	public static final String KEY_AFTRGB = "AftRGB";
	public static final String KEY_RRBRGB = "RrbRGB";
	public static final String KEY_IMGRGB = "ImgRGB";
	public static final String KEY_INFRGB = "InfRGB";
	public static final String KEY_MRKRGB = "MrkRGB";
	public static final String KEY_BAKRGB = "BakRGB";
	public static final String KEY_CURRGB = "CurRGB";
	public static final String KEY_TITRGB = "TitRGB";
	public static final String KEY_TIBRGB = "TibRGB";
	public static final String KEY_TLDRGB = "TldRGB";
	public static final String KEY_TLBRGB = "TlbRGB";
	public static final String KEY_BSFRGB = "BsfRGB";
	public static final String KEY_BSERGB = "BseRGB";
	public static final String KEY_FIFRGB = "FifRGB";
	public static final String KEY_FIBRGB = "FibRGB";

	// public static final String KEY_TITLECLR = "TitleColor";
	// public static final String KEY_TOOLBCLR = "ToolbarColor";

	public static final String KEY_MGNCOLOR = "MgnColor";
	public static final String KEY_CNTCOLOR = "CntColor";
	public static final String KEY_GUICOLOR = "GuiColor";

	public static final String KEY_MGNRGB = "MgnRGB";
	public static final String KEY_CNTRGB = "CntRGB";
	public static final String KEY_GUIRGB = "GuiRGB";

	public static final String KEY_ORGWIDTH = "OrgWidth";
	public static final String KEY_ORGHEIGHT = "OrgHeight";

	public static final String KEY_VIEWROTA = "ViewRota";
	public static final String KEY_FILESORT = "FileSort";
	public static final String KEY_VIEWPT = "ViewPt";
	public static final String KEY_INISCALE = "IniScale";

	public static final String KEY_CLICKAREA = "ClickAreaSp";
	public static final String KEY_PAGERANGE = "PageRangeSp";
	public static final String KEY_TAPRANGE = "TapRangeSp";
	public static final String KEY_VOLSCRL = "VolScrlSp";
	public static final String KEY_SCROLL = "Scroll";
	public static final String KEY_MARGIN = "Mergin";
	public static final String KEY_LONGTAP = "LongTap";
	public static final String KEY_WADJUST = "WAdjust";
	public static final String KEY_WSCALING = "WScaling";
	public static final String KEY_SCALING = "Scaling";
	public static final String KEY_CENTER = "Center";
	public static final String KEY_GRADATION = "Gradation";
	public static final String KEY_ZOOMTYPE = "ZoomType";
	public static final String KEY_SCRLRNGW = "ScrlRngW";
	public static final String KEY_SCRLRNGH = "ScrlRngH";

	public static final String KEY_CHAR_DETECT = "CharDetect";
	public static final String KEY_CHARSET = "Charset";
	public static final String KEY_VIEWROTAALL = "ViewRotaAll";
	public static final String KEY_SORT_BY_IGNORE_WIDTH = "SortByIgnoreWidth";
	public static final String KEY_SORT_BY_IGNORE_CASE = "SortByIgnoreCase";
	public static final String KEY_SORT_BY_SYMBOL = "SortBySymbol";
	public static final String KEY_SORT_BY_NATURAL_NUMBERS = "SortByNaturalNumbers";
	public static final String KEY_SORT_BY_KANJI_NUMERALS = "SortByKanjiNumerals";
	public static final String KEY_SORT_BY_JAPANESE_VOLUME_NAME = "SortByJapaneseVolumeName";
	public static final String KEY_SORT_BY_FILE_TYPE = "SortByFileType";
	public static final String KEY_SORT_PRIORITY_WORD_01 = "SortPriorityWord01";
	public static final String KEY_SORT_PRIORITY_WORD_02 = "SortPriorityWord02";
	public static final String KEY_SORT_PRIORITY_WORD_03 = "SortPriorityWord03";
	public static final String KEY_SORT_PRIORITY_WORD_04 = "SortPriorityWord04";
	public static final String KEY_SORT_PRIORITY_WORD_05 = "SortPriorityWord05";
	public static final String KEY_SORT_PRIORITY_WORD_06 = "SortPriorityWord06";
	public static final String KEY_SORT_PRIORITY_WORD_07 = "SortPriorityWord07";
	public static final String KEY_SORT_PRIORITY_WORD_08 = "SortPriorityWord08";
	public static final String KEY_SORT_PRIORITY_WORD_09 = "SortPriorityWord09";
	public static final String KEY_SORT_PRIORITY_WORD_10 = "SortPriorityWord10";
	public static final String KEY_EFFECTTIME = "EffectTime";
	public static final String KEY_MOMENTMODE = "MomentMode";
	public static final String KEY_PAGESELECT = "PageSelect";
	public static final String KEY_AUTOPLAY = "AutoPlayInt";
	public static final String KEY_IMMENABLE = "ImmEnable";
	public static final String KEY_BOTTOMFILE = "BottomFile";
	public static final String KEY_PINCHENABLE = "PinchEnable";
	public static final String KEY_OLDMENU = "OldMenu";

	public static final String KEY_HIDDENFILE = "HiddenFile";

	public static final String KEY_FORCENAVIGATIONBAR = "ForceHideNavigationBar";
	public static final String KEY_FORCESTATUSBAR = "ForceHideStatusBar";
	public static final String KEY_ENCRYPTION = "encryption";
	public static final String KEY_FORCETRADDISPLAYOLDVIEWROTATE = "ForceTradOldViewRotate";
	public static final String KEY_READTEXTSETTING = "ReadTextSetting";
	public static final String KEY_REVERSEROTARE = "ReverseRotate";

	public static final String KEY_NOISESCRL = "NoiseScrlSp";
	public static final String KEY_NOISEUNDER = "NoiseUnder";
	public static final String KEY_NOISEOVER = "NoiseOver";
	public static final String KEY_NOISELEVEL = "NoiseLevel";
	public static final String KEY_NOISEDEC = "NoiseDec";

	// public static final String KEY_TITLE = "Title";
	public static final String KEY_SHARPEN = "Sharpen";
	public static final String KEY_NOTICE = "Notice";
	public static final String KEY_NOSLEEP = "NoSleep";
	public static final String KEY_CHGPAGE = "ChgPage";
	public static final String KEY_CHGFLICK = "ChgFlick";
	public static final String KEY_ORGWIDTHTWICE = "OrgWidthTwice";
	public static final String KEY_REDUCE = "Reduce";
	public static final String KEY_MEMFREE = "MemFree";
	public static final String KEY_HALFHEIGHT = "HalfHeight";
	public static final String KEY_SCALEBMP = "ScaleBmp";
	public static final String KEY_BACKMODE = "BackMode";
	public static final String KEY_STARTSERVER = "StartServer";
	public static final String KEY_VOLKEY = "VolKey";
	public static final String KEY_SCRLWAY = "ScrlWay";
	public static final String KEY_THUMBNAIL = "Thumbnail";
	public static final String KEY_LISTMODE = "ListMode";
	public static final String KEY_INITIALIZE = "Initialize";
	public static final String KEY_THUMBCACHE = "ThumbCache";
	public static final String KEY_THUMBSORTTYPE = "ThumbSortType";
	public static final String KEY_THUMBCROP = "ThumbCrop";
	public static final String KEY_THUMBMARGIN = "ThumbMargin";
	public static final String KEY_ROTATEBTN = "RotateBtn";
	public static final String KEY_ACCESSLAMP = "AccessLamp";
	public static final String KEY_MARGINCUT = "MarginCut";
	public static final String KEY_MARGINCUTCOLOR = "MarginCutColor";
	public static final String KEY_DISPLAYPOSITION = "DisplayPosition";
	public static final String KEY_TAPEXPAND = "TapExpand";
	public static final String KEY_TAPPATTERN = "TapPattern";
	public static final String KEY_PAGENUMBER = "PageNumber";
	public static final String KEY_TIMEANDBATTERY = "TimeAndBattery";
	public static final String KEY_TAPRATE = "TapRate";
	public static final String KEY_OLDPAGESEL = "OldPageSel";
	public static final String KEY_RESUMEOPEN = "ResumeOpen";
	public static final String KEY_RETURNKLISTVIEW = "ReturnListView";
	public static final String KEY_CONFIRMBACK = "ConfirmBack";
	public static final String KEY_CLEARTOP = "ClearTop";
	public static final String KEY_KEEPSORTSHUFFLE = "KeepSortShuffle";
	public static final String KEY_KEEPFILELISTCURSOR = "KeepFilelistCursor";
	public static final String KEY_DISABLELISTICON = "DisableListIcon";
	public static final String KEY_SORTDIRTOP = "SoftDirTop";
	public static final String KEY_SORTFILETOP = "SoftFileTop";
	public static final String KEY_SORTDIRFILE = "SoftDirFile";
	public static final String KEY_SORTIMAGEFILE = "SoftImageFile";
	public static final String KEY_SORTTEXTFILE = "SoftTextFile";
	public static final String KEY_SORTCOMPFILE = "SoftCompFile";
	public static final String KEY_SORTPDFFILE = "SoftPdfFile";
	public static final String KEY_SORTEPUBFILE = "SoftEpubFile";
	public static final String KEY_SORTOTHERFILE = "SoftOtherFile";
	public static final String KEY_HISTNUM = "RLSave3";
	public static final String KEY_EXTENSION = "Extension";
	public static final String KEY_SPLIT_FILENAME = "SplitFilename";
	public static final String KEY_MAX_LINES = "MaxLinesFilename";
	public static final String KEY_THUMBSORT = "ThumbSort";
	public static final String KEY_PARENTMOVE = "ParentMove";
	public static final String KEY_FILEDELMENU = "FileDelMenu";
	public static final String KEY_FILERENMENU = "FileRenMenu";
	public static final String KEY_BRIGHT = "Bright";
	public static final String KEY_GAMMA = "Gamma";
	public static final String KEY_BKLIGHT = "BkLight";
	public static final String KEY_CONTRAST = "Contrast";
	public static final String KEY_HUE = "Hue";
	public static final String KEY_SATURATION = "Saturation";
	public static final String KEY_MOIRE = "Moire";
	public static final String KEY_GRAY = "Gray";
	public static final String KEY_COLORING = "Coloring";
	public static final String KEY_INVERT = "Invert";
	public static final String KEY_TOPSINGLE = "TopSingle";
	public static final String KEY_MAXTHREAD = "MaxThread";
	public static final String KEY_THUMBNAILTAP = "ThumbnailTap";
	public static final String KEY_MENULONGTAP = "MenuLongTap";
	public static final String KEY_LOUPESIZE = "LoupeSize";
	public static final String KEY_SCRLNEXT = "ScrollNext";
	public static final String KEY_VIEWNEXT = "ViewNextPage";
	public static final String KEY_NEXTFILTER = "NextPageFilter";
	public static final String KEY_CHGPAGEKEY = "ChgPageKey";
	public static final String KEY_FILELISTCACHEOFF = "FileListCacheOff";
	public static final String KEY_FILELISTFASTREADOFF = "FileListFastReadOff";

	public static final String KEY_SHOWTOOLBAR = "ShowToolbar";
	public static final String KEY_SHOWSELECTOR = "ShowSelector";
	public static final String KEY_TOOLBARNAME = "ToolbarName";
	public static final String KEY_TOOLBARSEEK = "ToolbarSeek";
	public static final String KEY_LISTTHUMBSEEK = "ListThumbSeek";
	public static final String KEY_THUMBSEEK = "ThumbSeek";
	public static final String KEY_THUMBSIZEW = "ThumbSizeW";
	public static final String KEY_THUMBSIZEH = "ThumbSizeH";

	public static final String KEY_PNUMDISP = "PnumDisp";
	public static final String KEY_PNUMFORMAT = "PnumFormat";
	public static final String KEY_PNUMPOS = "PnumPos";
	public static final String KEY_PNUMSIZE = "PnumSizeSp";
	public static final String KEY_PNUMCOLOR = "PnumColor";

	public static final String KEY_TIMEDISP = "TimeDisp";
	public static final String KEY_TIMEFORMAT = "TimeFormat";
	public static final String KEY_TIMEPOS = "TimePos";
	public static final String KEY_TIMESIZE = "TimeSizeSp";
	public static final String KEY_TIMECOLOR = "TimeColor";

	public static final String KEY_LASTPAGE = "LastPage";
	public static final String KEY_SAVEPAGE = "SavePage";
	public static final String KEY_PREVREV = "PrevRev";
	public static final String KEY_INITVIEW = "InitView";
	public static final String KEY_QUALITY = "Quality";
	public static final String KEY_PAGEWAY = "PageWay";
	public static final String KEY_FITDUAL = "FitDual";
	public static final String KEY_ZOOMOFF = "ZoomOff";
	public static final String KEY_CMARGIN = "CMargin";
	public static final String KEY_CSHADOW = "CShadow";
	public static final String KEY_NOEXPAND = "NoExpand";
	public static final String KEY_VIBFLAG = "VibFlag";
	public static final String KEY_EFFECTLIST = "EffectList";
	public static final String KEY_DELSHARE = "DelShare";
	public static final String KEY_TAPSCRL = "TapScrl";
	public static final String KEY_FLICKPAGE = "FlickPage";
	public static final String KEY_FLICKEDGE = "FlickEdge";
	public static final String KEY_TOPMENU = "TopMenu";
	public static final String KEY_TAP_PATTERN_MENU = "tapPatternMenu";
	public static final String KEY_TAP_SETUP_MENU = "tapSettingMenu";

	public static final String KEY_MarginAspectMask = "MarginAspectMask";
	public static final String KEY_MarginForceIgnoreAspect = "MarginForceIgnoreAspect";
	public static final String KEY_MargingBlackMask = "MargingBlackMask";
	public static final String KEY_MarginCustomInit = "MarginCustomInit";

	public static final String KEY_MarginLevel = "MarginLevel";
	public static final String KEY_MarginSpace = "MarginSpace";
	public static final String KEY_MarginStart = "MarginStart";
	public static final String KEY_MarginRange = "MarginRange";
	public static final String KEY_MarginLimit = "MarginLimit";

	public static final String KEY_THUMBNAILTOPSPACE = "ThumbnailTopSpace";
	public static final String KEY_THUMBNAILBOTTOMSPACE = "ThumbnailBottomSpace";
	public static final String KEY_FILENAMEBOTTOMSPACE = "FilenameBottomSpace";

	public static final String KEY_BOOKSHELFBRIGHTLEVEL = "BookShelfBrightLevel";
	public static final String KEY_BOOKSHELFEDGELEVEL = "BookShelfEdgeLevel";
	public static final String KEY_BOOKSHELFCOLOREXTON = "BookShelfColorExtOn";
	public static final String KEY_BOOKSHELFAFTERCURCLEON = "BookShelfAfterCircleOn";
	public static final String KEY_BOOKSHELFTEXTSPLITON = "BookShelfTextSplitOn";

	public static final String KEY_CORNERENDIENABLE = "CornerEndIEnable";
	public static final String KEY_CORNERENDIWIDTHLEVEL = "CornerEndIWidthLevel";
	public static final String KEY_CORNERENDIHEIGHTLEVEL = "CornerEndIHeightLevel";
	public static final String KEY_CORNEREND_I_TAP_01 = "CornerEndITap01";
	public static final String KEY_CORNEREND_I_TAP_02 = "CornerEndITap02";
	public static final String KEY_CORNEREND_I_TAP_03 = "CornerEndITap03";
	public static final String KEY_CORNEREND_I_TAP_04 = "CornerEndITap04";
	public static final String KEY_CORNEREND_I_TAP_05 = "CornerEndITap05";
	public static final String KEY_CORNEREND_I_TAP_06 = "CornerEndITap06";
	public static final String KEY_CORNEREND_I_TAP_07 = "CornerEndITap07";
	public static final String KEY_CORNEREND_I_TAP_08 = "CornerEndITap08";
	public static final String KEY_CORNEREND_I_TAP_09 = "CornerEndITap09";
	public static final String KEY_CORNEREND_I_TAP_10 = "CornerEndITap10";
	public static final String KEY_CORNEREND_I_TAP_11 = "CornerEndITap11";
	public static final String KEY_CORNEREND_I_TAP_12 = "CornerEndITap12";
	public static final String KEY_CORNEREND_I_TAP_13 = "CornerEndITap13";
	public static final String KEY_CORNEREND_I_TAP_14 = "CornerEndITap14";

	public static final String KEY_CORNERENDTENABLE = "CornerEndTEnable";
	public static final String KEY_CORNERENDTWIDTHLEVEL = "CornerEndTWidthLevel";
	public static final String KEY_CORNERENDTHEIGHTLEVEL = "CornerEndTHeightLevel";
	public static final String KEY_CORNEREND_T_TAP_01 = "CornerEndTTap01";
	public static final String KEY_CORNEREND_T_TAP_02 = "CornerEndTTap02";
	public static final String KEY_CORNEREND_T_TAP_03 = "CornerEndTTap03";
	public static final String KEY_CORNEREND_T_TAP_04 = "CornerEndTTap04";
	public static final String KEY_CORNEREND_T_TAP_05 = "CornerEndTTap05";
	public static final String KEY_CORNEREND_T_TAP_06 = "CornerEndTTap06";
	public static final String KEY_CORNEREND_T_TAP_07 = "CornerEndTTap07";
	public static final String KEY_CORNEREND_T_TAP_08 = "CornerEndTTap08";
	public static final String KEY_CORNEREND_T_TAP_09 = "CornerEndTTap09";
	public static final String KEY_CORNEREND_T_TAP_10 = "CornerEndTTap10";
	public static final String KEY_CORNEREND_T_TAP_11 = "CornerEndTTap11";
	public static final String KEY_CORNEREND_T_TAP_12 = "CornerEndTTap12";
	public static final String KEY_CORNEREND_T_TAP_13 = "CornerEndTTap13";
	public static final String KEY_CORNEREND_T_TAP_14 = "CornerEndTTap14";

	// スケーリング設定
	public static final String KEY_ALGOMODE = "AlgoMode";

	public static final String KEY_MEMSIZE = "MemSize";
	public static final String KEY_MEMNEXT = "MemNext";
	public static final String KEY_MEMPREV = "MemPrev";
	public static final String KEY_MEMCACHESTARTTHRESHOLD = "MemCacheStartThreshold";

	public static final String KEY_PinchScale = "PinchScale";
	public static final String KEY_PinchScaleText = "PinchScaleText";

	public static final String KEY_TOOLBAR_SIZE = "ToolbarSize";

	public static final String KEY_LAST_VERSION = "LastVer";
	public static final String KEY_CHECK_RELEASE = "CheckRelease";
	public static final String KEY_TIME_CHECK_RELEASE = "TimeCheckRelease";

	public static final String KEY_CUSTOM_URL_SCHEME_KEY = "CustomUrlSchemeAuthenticationKey";
	public static final String KEY_SMB_MODE = "ComittoNMode";

	public static final String KEY_FLOATINGICONSIZE = "FloatingIconSize";
	public static final String KEY_FLOATINGICONDIRECTIONMODE = "FloatingIconDirectionMode";
	public static final String KEY_FLOATINGICONHORIZENTIAL = "FloatingIconHoraizential";
	public static final String KEY_FLOATINGICONVIRTICAL = "FloatingIconVertical";
	public static final String KEY_FLOATINGICONTRANSPARENCY = "FloatingIconTransParency";
	public static final String KEY_FLOATINGICONENABLE = "FloatingIconEnable";

	// プロファイル設定
	public static final String KEY_PROFILE_WORD_01 = "Profile1";
	public static final String KEY_PROFILE_WORD_02 = "Profile2";
	public static final String KEY_PROFILE_WORD_03 = "Profile3";
	public static final String KEY_PROFILE_WORD_04 = "Profile4";
	public static final String KEY_PROFILE_WORD_05 = "Profile5";
	public static final String KEY_PROFILE_WORD_06 = "Profile6";
	public static final String KEY_PROFILE_WORD_07 = "Profile7";
	public static final String KEY_PROFILE_WORD_08 = "Profile8";
	public static final String KEY_PROFILE_WORD_09 = "Profile9";
	public static final String KEY_PROFILE_WORD_10 = "Profile10";

	public static final String KEY_PROFILE_GRAY_01 = "ProfileGray1";
	public static final String KEY_PROFILE_GRAY_02 = "ProfileGray2";
	public static final String KEY_PROFILE_GRAY_03 = "ProfileGray3";
	public static final String KEY_PROFILE_GRAY_04 = "ProfileGray4";
	public static final String KEY_PROFILE_GRAY_05 = "ProfileGray5";
	public static final String KEY_PROFILE_GRAY_06 = "ProfileGray6";
	public static final String KEY_PROFILE_GRAY_07 = "ProfileGray7";
	public static final String KEY_PROFILE_GRAY_08 = "ProfileGray8";
	public static final String KEY_PROFILE_GRAY_09 = "ProfileGray9";
	public static final String KEY_PROFILE_GRAY_10 = "ProfileGray10";

	public static final String KEY_PROFILE_INVERT_01 = "ProfileInvert1";
	public static final String KEY_PROFILE_INVERT_02 = "ProfileInvert2";
	public static final String KEY_PROFILE_INVERT_03 = "ProfileInvert3";
	public static final String KEY_PROFILE_INVERT_04 = "ProfileInvert4";
	public static final String KEY_PROFILE_INVERT_05 = "ProfileInvert5";
	public static final String KEY_PROFILE_INVERT_06 = "ProfileInvert6";
	public static final String KEY_PROFILE_INVERT_07 = "ProfileInvert7";
	public static final String KEY_PROFILE_INVERT_08 = "ProfileInvert8";
	public static final String KEY_PROFILE_INVERT_09 = "ProfileInvert9";
	public static final String KEY_PROFILE_INVERT_10 = "ProfileInvert10";

	public static final String KEY_PROFILE_MOIRE_01 = "ProfileMoire1";
	public static final String KEY_PROFILE_MOIRE_02 = "ProfileMoire2";
	public static final String KEY_PROFILE_MOIRE_03 = "ProfileMoire3";
	public static final String KEY_PROFILE_MOIRE_04 = "ProfileMoire4";
	public static final String KEY_PROFILE_MOIRE_05 = "ProfileMoire5";
	public static final String KEY_PROFILE_MOIRE_06 = "ProfileMoire6";
	public static final String KEY_PROFILE_MOIRE_07 = "ProfileMoire7";
	public static final String KEY_PROFILE_MOIRE_08 = "ProfileMoire8";
	public static final String KEY_PROFILE_MOIRE_09 = "ProfileMoire9";
	public static final String KEY_PROFILE_MOIRE_10 = "ProfileMoire10";

	public static final String KEY_PROFILE_SHARPEN_01 = "ProfileSharpen1";
	public static final String KEY_PROFILE_SHARPEN_02 = "ProfileSharpen2";
	public static final String KEY_PROFILE_SHARPEN_03 = "ProfileSharpen3";
	public static final String KEY_PROFILE_SHARPEN_04 = "ProfileSharpen4";
	public static final String KEY_PROFILE_SHARPEN_05 = "ProfileSharpen5";
	public static final String KEY_PROFILE_SHARPEN_06 = "ProfileSharpen6";
	public static final String KEY_PROFILE_SHARPEN_07 = "ProfileSharpen7";
	public static final String KEY_PROFILE_SHARPEN_08 = "ProfileSharpen8";
	public static final String KEY_PROFILE_SHARPEN_09 = "ProfileSharpen9";
	public static final String KEY_PROFILE_SHARPEN_10 = "ProfileSharpen10";

	public static final String KEY_PROFILE_BRIGHT_01 = "ProfileBright1";
	public static final String KEY_PROFILE_BRIGHT_02 = "ProfileBright2";
	public static final String KEY_PROFILE_BRIGHT_03 = "ProfileBright3";
	public static final String KEY_PROFILE_BRIGHT_04 = "ProfileBright4";
	public static final String KEY_PROFILE_BRIGHT_05 = "ProfileBright5";
	public static final String KEY_PROFILE_BRIGHT_06 = "ProfileBright6";
	public static final String KEY_PROFILE_BRIGHT_07 = "ProfileBright7";
	public static final String KEY_PROFILE_BRIGHT_08 = "ProfileBright8";
	public static final String KEY_PROFILE_BRIGHT_09 = "ProfileBright9";
	public static final String KEY_PROFILE_BRIGHT_10 = "ProfileBright10";

	public static final String KEY_PROFILE_GAMMA_01 = "ProfileGamma1";
	public static final String KEY_PROFILE_GAMMA_02 = "ProfileGamma2";
	public static final String KEY_PROFILE_GAMMA_03 = "ProfileGamma3";
	public static final String KEY_PROFILE_GAMMA_04 = "ProfileGamma4";
	public static final String KEY_PROFILE_GAMMA_05 = "ProfileGamma5";
	public static final String KEY_PROFILE_GAMMA_06 = "ProfileGamma6";
	public static final String KEY_PROFILE_GAMMA_07 = "ProfileGamma7";
	public static final String KEY_PROFILE_GAMMA_08 = "ProfileGamma8";
	public static final String KEY_PROFILE_GAMMA_09 = "ProfileGamma9";
	public static final String KEY_PROFILE_GAMMA_10 = "ProfileGamma10";

	public static final String KEY_PROFILE_CONTRAST_01 = "ProfileContrast1";
	public static final String KEY_PROFILE_CONTRAST_02 = "ProfileContrast2";
	public static final String KEY_PROFILE_CONTRAST_03 = "ProfileContrast3";
	public static final String KEY_PROFILE_CONTRAST_04 = "ProfileContrast4";
	public static final String KEY_PROFILE_CONTRAST_05 = "ProfileContrast5";
	public static final String KEY_PROFILE_CONTRAST_06 = "ProfileContrast6";
	public static final String KEY_PROFILE_CONTRAST_07 = "ProfileContrast7";
	public static final String KEY_PROFILE_CONTRAST_08 = "ProfileContrast8";
	public static final String KEY_PROFILE_CONTRAST_09 = "ProfileContrast9";
	public static final String KEY_PROFILE_CONTRAST_10 = "ProfileContrast10";

	public static final String KEY_PROFILE_HUE_01 = "ProfileHue1";
	public static final String KEY_PROFILE_HUE_02 = "ProfileHue2";
	public static final String KEY_PROFILE_HUE_03 = "ProfileHue3";
	public static final String KEY_PROFILE_HUE_04 = "ProfileHue4";
	public static final String KEY_PROFILE_HUE_05 = "ProfileHue5";
	public static final String KEY_PROFILE_HUE_06 = "ProfileHue6";
	public static final String KEY_PROFILE_HUE_07 = "ProfileHue7";
	public static final String KEY_PROFILE_HUE_08 = "ProfileHue8";
	public static final String KEY_PROFILE_HUE_09 = "ProfileHue9";
	public static final String KEY_PROFILE_HUE_10 = "ProfileHue10";

	public static final String KEY_PROFILE_SATURATION_01 = "ProfileSaturation1";
	public static final String KEY_PROFILE_SATURATION_02 = "ProfileSaturation2";
	public static final String KEY_PROFILE_SATURATION_03 = "ProfileSaturation3";
	public static final String KEY_PROFILE_SATURATION_04 = "ProfileSaturation4";
	public static final String KEY_PROFILE_SATURATION_05 = "ProfileSaturation5";
	public static final String KEY_PROFILE_SATURATION_06 = "ProfileSaturation6";
	public static final String KEY_PROFILE_SATURATION_07 = "ProfileSaturation7";
	public static final String KEY_PROFILE_SATURATION_08 = "ProfileSaturation8";
	public static final String KEY_PROFILE_SATURATION_09 = "ProfileSaturation9";
	public static final String KEY_PROFILE_SATURATION_10 = "ProfileSaturation10";

	public static final String KEY_PROFILE_ROTATE_01 = "ProfileRotate1";
	public static final String KEY_PROFILE_ROTATE_02 = "ProfileRotate2";
	public static final String KEY_PROFILE_ROTATE_03 = "ProfileRotate3";
	public static final String KEY_PROFILE_ROTATE_04 = "ProfileRotate4";
	public static final String KEY_PROFILE_ROTATE_05 = "ProfileRotate5";
	public static final String KEY_PROFILE_ROTATE_06 = "ProfileRotate6";
	public static final String KEY_PROFILE_ROTATE_07 = "ProfileRotate7";
	public static final String KEY_PROFILE_ROTATE_08 = "ProfileRotate8";
	public static final String KEY_PROFILE_ROTATE_09 = "ProfileRotate9";
	public static final String KEY_PROFILE_ROTATE_10 = "ProfileRotate10";

	public static final String KEY_PROFILE_REVERSE_01 = "ProfileReverse1";
	public static final String KEY_PROFILE_REVERSE_02 = "ProfileReverse2";
	public static final String KEY_PROFILE_REVERSE_03 = "ProfileReverse3";
	public static final String KEY_PROFILE_REVERSE_04 = "ProfileReverse4";
	public static final String KEY_PROFILE_REVERSE_05 = "ProfileReverse5";
	public static final String KEY_PROFILE_REVERSE_06 = "ProfileReverse6";
	public static final String KEY_PROFILE_REVERSE_07 = "ProfileReverse7";
	public static final String KEY_PROFILE_REVERSE_08 = "ProfileReverse8";
	public static final String KEY_PROFILE_REVERSE_09 = "ProfileReverse9";
	public static final String KEY_PROFILE_REVERSE_10 = "ProfileReverse10";

	public static final String KEY_PROFILE_CHGPAGE_01 = "ProfileChgPage1";
	public static final String KEY_PROFILE_CHGPAGE_02 = "ProfileChgPage2";
	public static final String KEY_PROFILE_CHGPAGE_03 = "ProfileChgPage3";
	public static final String KEY_PROFILE_CHGPAGE_04 = "ProfileChgPage4";
	public static final String KEY_PROFILE_CHGPAGE_05 = "ProfileChgPage5";
	public static final String KEY_PROFILE_CHGPAGE_06 = "ProfileChgPage6";
	public static final String KEY_PROFILE_CHGPAGE_07 = "ProfileChgPage7";
	public static final String KEY_PROFILE_CHGPAGE_08 = "ProfileChgPage8";
	public static final String KEY_PROFILE_CHGPAGE_09 = "ProfileChgPage9";
	public static final String KEY_PROFILE_CHGPAGE_10 = "ProfileChgPage10";

	public static final String KEY_PROFILE_PAGEWAY_01 = "ProfilePageWay1";
	public static final String KEY_PROFILE_PAGEWAY_02 = "ProfilePageWay2";
	public static final String KEY_PROFILE_PAGEWAY_03 = "ProfilePageWay3";
	public static final String KEY_PROFILE_PAGEWAY_04 = "ProfilePageWay4";
	public static final String KEY_PROFILE_PAGEWAY_05 = "ProfilePageWay5";
	public static final String KEY_PROFILE_PAGEWAY_06 = "ProfilePageWay6";
	public static final String KEY_PROFILE_PAGEWAY_07 = "ProfilePageWay7";
	public static final String KEY_PROFILE_PAGEWAY_08 = "ProfilePageWay8";
	public static final String KEY_PROFILE_PAGEWAY_09 = "ProfilePageWay9";
	public static final String KEY_PROFILE_PAGEWAY_10 = "ProfilePageWay10";

	public static final String KEY_PROFILE_SCRLWAY_01 = "ProfileScrlWay1";
	public static final String KEY_PROFILE_SCRLWAY_02 = "ProfileScrlWay2";
	public static final String KEY_PROFILE_SCRLWAY_03 = "ProfileScrlWay3";
	public static final String KEY_PROFILE_SCRLWAY_04 = "ProfileScrlWay4";
	public static final String KEY_PROFILE_SCRLWAY_05 = "ProfileScrlWay5";
	public static final String KEY_PROFILE_SCRLWAY_06 = "ProfileScrlWay6";
	public static final String KEY_PROFILE_SCRLWAY_07 = "ProfileScrlWay7";
	public static final String KEY_PROFILE_SCRLWAY_08 = "ProfileScrlWay8";
	public static final String KEY_PROFILE_SCRLWAY_09 = "ProfileScrlWay9";
	public static final String KEY_PROFILE_SCRLWAY_10 = "ProfileScrlWay10";

	public static final String KEY_PROFILE_TOPSINGLE_01 = "ProfileTopSingle1";
	public static final String KEY_PROFILE_TOPSINGLE_02 = "ProfileTopSingle2";
	public static final String KEY_PROFILE_TOPSINGLE_03 = "ProfileTopSingle3";
	public static final String KEY_PROFILE_TOPSINGLE_04 = "ProfileTopSingle4";
	public static final String KEY_PROFILE_TOPSINGLE_05 = "ProfileTopSingle5";
	public static final String KEY_PROFILE_TOPSINGLE_06 = "ProfileTopSingle6";
	public static final String KEY_PROFILE_TOPSINGLE_07 = "ProfileTopSingle7";
	public static final String KEY_PROFILE_TOPSINGLE_08 = "ProfileTopSingle8";
	public static final String KEY_PROFILE_TOPSINGLE_09 = "ProfileTopSingle9";
	public static final String KEY_PROFILE_TOPSINGLE_10 = "ProfileTopSingle10";

	public static final String KEY_PROFILE_BKLIGHT_01 = "ProfileBkLight1";
	public static final String KEY_PROFILE_BKLIGHT_02 = "ProfileBkLight2";
	public static final String KEY_PROFILE_BKLIGHT_03 = "ProfileBkLight3";
	public static final String KEY_PROFILE_BKLIGHT_04 = "ProfileBkLight4";
	public static final String KEY_PROFILE_BKLIGHT_05 = "ProfileBkLight5";
	public static final String KEY_PROFILE_BKLIGHT_06 = "ProfileBkLight6";
	public static final String KEY_PROFILE_BKLIGHT_07 = "ProfileBkLight7";
	public static final String KEY_PROFILE_BKLIGHT_08 = "ProfileBkLight8";
	public static final String KEY_PROFILE_BKLIGHT_09 = "ProfileBkLight9";
	public static final String KEY_PROFILE_BKLIGHT_10 = "ProfileBkLight10";

	public static final String KEY_PROFILE_ALGOMODE_01 = "ProfileAlgoMode1";
	public static final String KEY_PROFILE_ALGOMODE_02 = "ProfileAlgoMode2";
	public static final String KEY_PROFILE_ALGOMODE_03 = "ProfileAlgoMode3";
	public static final String KEY_PROFILE_ALGOMODE_04 = "ProfileAlgoMode4";
	public static final String KEY_PROFILE_ALGOMODE_05 = "ProfileAlgoMode5";
	public static final String KEY_PROFILE_ALGOMODE_06 = "ProfileAlgoMode6";
	public static final String KEY_PROFILE_ALGOMODE_07 = "ProfileAlgoMode7";
	public static final String KEY_PROFILE_ALGOMODE_08 = "ProfileAlgoMode8";
	public static final String KEY_PROFILE_ALGOMODE_09 = "ProfileAlgoMode9";
	public static final String KEY_PROFILE_ALGOMODE_10 = "ProfileAlgoMode10";

	public static final String KEY_PROFILE_DISPMODE_01 = "ProfileDispMode1";
	public static final String KEY_PROFILE_DISPMODE_02 = "ProfileDispMode2";
	public static final String KEY_PROFILE_DISPMODE_03 = "ProfileDispMode3";
	public static final String KEY_PROFILE_DISPMODE_04 = "ProfileDispMode4";
	public static final String KEY_PROFILE_DISPMODE_05 = "ProfileDispMode5";
	public static final String KEY_PROFILE_DISPMODE_06 = "ProfileDispMode6";
	public static final String KEY_PROFILE_DISPMODE_07 = "ProfileDispMode7";
	public static final String KEY_PROFILE_DISPMODE_08 = "ProfileDispMode8";
	public static final String KEY_PROFILE_DISPMODE_09 = "ProfileDispMode9";
	public static final String KEY_PROFILE_DISPMODE_10 = "ProfileDispMode10";

	public static final String KEY_PROFILE_SCALEMODE_01 = "ProfileScaleMode1";
	public static final String KEY_PROFILE_SCALEMODE_02 = "ProfileScaleMode2";
	public static final String KEY_PROFILE_SCALEMODE_03 = "ProfileScaleMode3";
	public static final String KEY_PROFILE_SCALEMODE_04 = "ProfileScaleMode4";
	public static final String KEY_PROFILE_SCALEMODE_05 = "ProfileScaleMode5";
	public static final String KEY_PROFILE_SCALEMODE_06 = "ProfileScaleMode6";
	public static final String KEY_PROFILE_SCALEMODE_07 = "ProfileScaleMode7";
	public static final String KEY_PROFILE_SCALEMODE_08 = "ProfileScaleMode8";
	public static final String KEY_PROFILE_SCALEMODE_09 = "ProfileScaleMode9";
	public static final String KEY_PROFILE_SCALEMODE_10 = "ProfileScaleMode10";

	public static final String KEY_PROFILE_MGNCUT_01 = "ProfileMgnCut1";
	public static final String KEY_PROFILE_MGNCUT_02 = "ProfileMgnCut2";
	public static final String KEY_PROFILE_MGNCUT_03 = "ProfileMgnCut3";
	public static final String KEY_PROFILE_MGNCUT_04 = "ProfileMgnCut4";
	public static final String KEY_PROFILE_MGNCUT_05 = "ProfileMgnCut5";
	public static final String KEY_PROFILE_MGNCUT_06 = "ProfileMgnCut6";
	public static final String KEY_PROFILE_MGNCUT_07 = "ProfileMgnCut7";
	public static final String KEY_PROFILE_MGNCUT_08 = "ProfileMgnCut8";
	public static final String KEY_PROFILE_MGNCUT_09 = "ProfileMgnCut9";
	public static final String KEY_PROFILE_MGNCUT_10 = "ProfileMgnCut10";

	public static final String KEY_PROFILE_MGNCUTCOLOR_01 = "ProfileMgnCutColor1";
	public static final String KEY_PROFILE_MGNCUTCOLOR_02 = "ProfileMgnCutColor2";
	public static final String KEY_PROFILE_MGNCUTCOLOR_03 = "ProfileMgnCutColor3";
	public static final String KEY_PROFILE_MGNCUTCOLOR_04 = "ProfileMgnCutColor4";
	public static final String KEY_PROFILE_MGNCUTCOLOR_05 = "ProfileMgnCutColor5";
	public static final String KEY_PROFILE_MGNCUTCOLOR_06 = "ProfileMgnCutColor6";
	public static final String KEY_PROFILE_MGNCUTCOLOR_07 = "ProfileMgnCutColor7";
	public static final String KEY_PROFILE_MGNCUTCOLOR_08 = "ProfileMgnCutColor8";
	public static final String KEY_PROFILE_MGNCUTCOLOR_09 = "ProfileMgnCutColor9";
	public static final String KEY_PROFILE_MGNCUTCOLOR_10 = "ProfileMgnCutColor10";

	public static final String KEY_PROFILE_PINCHSCALE_01 = "ProfilePinchScale1";
	public static final String KEY_PROFILE_PINCHSCALE_02 = "ProfilePinchScale2";
	public static final String KEY_PROFILE_PINCHSCALE_03 = "ProfilePinchScale3";
	public static final String KEY_PROFILE_PINCHSCALE_04 = "ProfilePinchScale4";
	public static final String KEY_PROFILE_PINCHSCALE_05 = "ProfilePinchScale5";
	public static final String KEY_PROFILE_PINCHSCALE_06 = "ProfilePinchScale6";
	public static final String KEY_PROFILE_PINCHSCALE_07 = "ProfilePinchScale7";
	public static final String KEY_PROFILE_PINCHSCALE_08 = "ProfilePinchScale8";
	public static final String KEY_PROFILE_PINCHSCALE_09 = "ProfilePinchScale9";
	public static final String KEY_PROFILE_PINCHSCALE_10 = "ProfilePinchScale10";
	public static final String KEY_PROFILE_DISPLAYPOSITION_01 = "ProfileDisplayPosition1";
	public static final String KEY_PROFILE_DISPLAYPOSITION_02 = "ProfileDisplayPosition2";
	public static final String KEY_PROFILE_DISPLAYPOSITION_03 = "ProfileDisplayPosition3";
	public static final String KEY_PROFILE_DISPLAYPOSITION_04 = "ProfileDisplayPosition4";
	public static final String KEY_PROFILE_DISPLAYPOSITION_05 = "ProfileDisplayPosition5";
	public static final String KEY_PROFILE_DISPLAYPOSITION_06 = "ProfileDisplayPosition6";
	public static final String KEY_PROFILE_DISPLAYPOSITION_07 = "ProfileDisplayPosition7";
	public static final String KEY_PROFILE_DISPLAYPOSITION_08 = "ProfileDisplayPosition8";
	public static final String KEY_PROFILE_DISPLAYPOSITION_09 = "ProfileDisplayPosition9";
	public static final String KEY_PROFILE_DISPLAYPOSITION_10 = "ProfileDisplayPosition10";

	public static final String KEY_PROFILE_COLORING_01 = "ProfileColoring1";
	public static final String KEY_PROFILE_COLORING_02 = "ProfileColoring2";
	public static final String KEY_PROFILE_COLORING_03 = "ProfileColoring3";
	public static final String KEY_PROFILE_COLORING_04 = "ProfileColoring4";
	public static final String KEY_PROFILE_COLORING_05 = "ProfileColoring5";
	public static final String KEY_PROFILE_COLORING_06 = "ProfileColoring6";
	public static final String KEY_PROFILE_COLORING_07 = "ProfileColoring7";
	public static final String KEY_PROFILE_COLORING_08 = "ProfileColoring8";
	public static final String KEY_PROFILE_COLORING_09 = "ProfileColoring9";
	public static final String KEY_PROFILE_COLORING_10 = "ProfileColoring10";

	public static final String KEY_BOOKSHELFPATTERN = "BookShelfPattern";
	public static final String KEY_BOOKSHELFBMPFILE1 = "BookShelfBmpFile1";
	public static final String KEY_BOOKSHELFBMPFILE2 = "BookShelfBmpFile2";
	public static final String KEY_BOOKSHELFBMPFILE3 = "BookShelfBmpFile3";
	public static final String KEY_BOOKSHELFBMPFILE4 = "BookShelfBmpFile4";
	public static final String KEY_BOOKSHELFBMPFILE5 = "BookShelfBmpFile5";
	public static final String KEY_BOOKSHELFBMPFILE6 = "BookShelfBmpFile6";
	public static final String KEY_BOOKSHELFBMPFILE7 = "BookShelfBmpFile7";
	public static final String KEY_BOOKSHELFBMPFILE8 = "BookShelfBmpFile8";

	public static final int DEFAULT_INISCHALE = 5; //全体を表示(見開き対応)
	public static final int DEFAULT_INITVIEW = 1; //見開き表示
	public static final int DEFAULT_QUALITY = 1; //画質を優先する
	public static final int DEFAULT_CLICKAREA = 30; //上下の操作エリアサイズ:60sp
	public static final int DEFAULT_PAGERANGE = 5; //ページ選択の感度:1ページ/5sp
	public static final int DEFAULT_SCROLL = 2;
	public static final int DEFAULT_ORGWIDTH = 0;
	public static final int DEFAULT_ORGHEIGHT = 0;
	public static final int DEFAULT_TAPRANGE = 6; // スクロール開始の感度
	public static final int DEFAULT_MARGIN = 0;
	public static final int DEFAULT_LONGTAP = 4;
	public static final int DEFAULT_MENULONGTAP = 8; // 0.8秒
	public static final int DEFAULT_WADJUST = 25; // -25～+25(1%単位)
	public static final int DEFAULT_WSCALING = 25; // -25～+25(1%単位)
	public static final int DEFAULT_SCALING = 75; // -75～+125(1%単位)
	public static final int DEFAULT_CENTER = 2; // ドット
	public static final int DEFAULT_GRADATION = 5; // 0～30%(1%単位)
	public static final int DEFAULT_FONTTITLE = 10; // 28sp
	public static final int DEFAULT_FONTMAIN = 14; // 28sp
	public static final int DEFAULT_FONTSUB = 8; // 22sp
	public static final int DEFAULT_FONTTILE = 6; // 16sp
	public static final int DEFAULT_MEMSIZE = 18; // キャッシュサイズ:200MByte
	public static final int DEFAULT_MEMNEXT = 16; // キャッシュ前方:32ページ
	public static final int DEFAULT_MEMPREV = 8; // キャッシュ後方:16ページ
	public static final String DEFAULT_MEMCACHE = "0"; // 自動
	public static final int DEFAULT_NOISESCRL = 1; // 20ドット
	public static final int DEFAULT_NOISEUNDER = 8; // 800
	public static final int DEFAULT_NOISEOVER = 15; // 1500
	public static final int DEFAULT_VOLSCRL = 6; // 32ドット
	public static final int DEFAULT_SCRLRNGW = 6; // 35% ((6+1)*5)
	public static final int DEFAULT_SCRLRNGH = 6; // 35% ((6+1)*5)
	public static final int DEFAULT_ITEMMARGIN = 10; // 項目の余白:10sp
	public static final int DEFAULT_EFFECTTIME = 3; // スクロール時間:250msec
	public static final int DEFAULT_MOMENTMODE = 8; // スクロール減速:フレーム1/8ずつ減速
	public static final int DEFAULT_AUTOPLAY = 2; // 1.5sec(0.5 * (5 + 1))
	public static final boolean DEF_SAVEPAGE = true; // ページ移動時にしおりを保存
	public static final int DEFAULT_TAPPATTERN = 0; // タッチ位置のパターン:左右分割
	public static final int DEFAULT_BOOKSHELFPATTERN = 0; // 本棚のパターン
	public static final int DEFAULT_TAPRATE = 4; // 50% : 50%
	public static final boolean DEFAULT_CHGPAGE = true; // タップ操作の入替え:YES(縦書き、漫画)
	public static final boolean DEFAULT_PREVREV = true; // 前ページに戻った時に逆から表示
	public static final boolean DEFAULT_CHGPAGEKEY = false; // 左右キー操作の入替え

	public static final boolean DEFAULT_PNUMDISP = false; // ページ番号表示しない
	public static final int DEFAULT_PNUMFORMAT = 0; // page / total
	public static final int DEFAULT_PNUMPOS = 4; // 中央下
	public static final int DEFAULT_PNUMSIZE = 10; // 16px (8 + 6)
	public static final int DEFAULT_PNUMCOLOR = 0; // 0 = 白, 1 = 黒

	public static final boolean DEFAULT_TIMEDISP = false; // 時刻と充電表示しない
	public static final int DEFAULT_TIMEFORMAT = 0; // 24:00
	public static final int DEFAULT_TIMEPOS = 5; // 中央下
	public static final int DEFAULT_TIMESIZE = 10; // 16px (8 + 6)
	public static final int DEFAULT_TIMECOLOR = 0; // 0 = 白, 1 = 黒

	public static final int DEFAULT_TOOLBARSEEK = 14; // 38 (8+24)
	public static final int DEFAULT_THUMBSIZEW = 23; // 270 (23 * 10 + 40)
	public static final int DEFAULT_THUMBSIZEH = 28; // 320 (28 * 10 + 40)
	public static final int DEFAULT_LISTTHUMBSIZEH = 20; // 240 (20 * 10 + 40)
	public static final int DEFAULT_TOOLBAR_SIZE = 2; // 100%

	public static final int DEFAULT_MarginLevel_Min = 15; // 色判定時のビットマスク深度(弱設定)
	public static final int DEFAULT_MarginLevel_Mid = 15; // 色判定時のビットマスク深度(中設定)
	public static final int DEFAULT_MarginLevel_Ultra = 14; // 色判定時のビットマスク深度(強設定)
	public static final int DEFAULT_MarginLevel_Strong = 14; // 色判定時のビットマスク深度(特上設定)
	public static final int DEFAULT_MarginLevel_Overkill = 12; // 色判定時のビットマスク深度(最強設定)
	public static final int DEFAULT_MarginLevel = 14; // 色判定時のビットマスク深度(デフォルト設定)
	public static final int MAX_MarginLevel = 15;	// 色判定時のビットマスク深度(最大数)
	public static final int DEFAULT_MarginStart_Min = 1; // 余白判定を開始するまでの無視区間(弱設定)
	public static final int DEFAULT_MarginStart_Mid = 2; // 余白判定を開始するまでの無視区間(中設定)
	public static final int DEFAULT_MarginStart_Ultra = 3; // 余白判定を開始するまでの無視区間(強設定)
	public static final int DEFAULT_MarginStart_Strong = 5; // 余白判定を開始するまでの無視区間(特上設定)
	public static final int DEFAULT_MarginStart_Overkill = 10; // 余白判定を開始するまでの無視区間(最強設定)
	public static final int DEFAULT_MarginStart = 5; // 余白判定を開始するまでの無視区間(デフォルト設定)
	public static final int MAX_MarginStart = 10;	// 余白判定を開始するまでの無視区間(最大数)
	public static final int DEFAULT_MarginSpace_Min = 60; // 余白判定された幅のうち画像から削除する割合(弱設定)
	public static final int DEFAULT_MarginSpace_Mid = 80; // 余白判定された幅のうち画像から削除する割合(中設定)
	public static final int DEFAULT_MarginSpace_Ultra = 90; // 余白判定された幅のうち画像から削除する割合(強設定)
	public static final int DEFAULT_MarginSpace_Strong = 95; // 余白判定された幅のうち画像から削除する割合(特上設定)
	public static final int DEFAULT_MarginSpace_Overkill = 100; // 余白判定された幅のうち画像から削除する割合(最強設定)
	public static final int DEFAULT_MarginSpace = 95; // 余白判定された幅のうち画像から削除する割合(デフォルト設定)
	public static final int MAX_MarginSpace = 100;	// 余白判定された幅のうち画像から削除する割合(最大数)
	public static final int DEFAULT_MarginRange_Min = 25; // 余白判定を行う範囲(弱設定)
	public static final int DEFAULT_MarginRange_Mid = 30; // 余白判定を行う範囲(中設定)
	public static final int DEFAULT_MarginRange_Ultra = 45; // 余白判定を行う範囲(強設定)
	public static final int DEFAULT_MarginRange_Strong = 50; // 余白判定を行う範囲(特上設定)
	public static final int DEFAULT_MarginRange_Overkill = 100; // 余白判定を行う範囲(最強設定)
	public static final int DEFAULT_MarginRange = 50; // 余白判定を行う範囲(デフォルト設定)
	public static final int MAX_MarginRange = 100;	// 余白判定を行う範囲(最大数)
	public static final int DEFAULT_MarginLimit_Min = 5; // 違う色の出現回数の許容する割合(弱設定)
	public static final int DEFAULT_MarginLimit_Mid = 6; // 違う色の出現回数の許容する割合(中設定)
	public static final int DEFAULT_MarginLimit_Ultra = 8; // 違う色の出現回数の許容する割合(強設定)
	public static final int DEFAULT_MarginLimit_Strong = 20; // 違う色の出現回数の許容する割合(特上設定)
	public static final int DEFAULT_MarginLimit_Overkill = 50; // 違う色の出現回数の許容する割合(最強設定)
	public static final int DEFAULT_MarginLimit = 20; // 違う色の出現回数の許容する割合(デフォルト設定)
	public static final int MAX_MarginLimit = 50;	// 違う色の出現回数の許容する割合(最大数)

	public static final int DEFAULT_FLOATINGICONSIZE = 16;
	public static final int DEFAULT_FLOATINGICONHORIZENTIAL = 100;
	public static final int DEFAULT_FLOATINGICONVIRTICAL = 50;
	public static final int DEFAULT_FLOATINGICONTRANSPARENCY = 50;
	public static final int DEFAULT_FLOATINGICONDIRECTIONMODE = 0;

	public static final int DEFAULT_THUMBNAILTOPSPACE = 5;
	public static final int MAX_THUMBNAILTOPSPACE = 100;
	public static final int DEFAULT_THUMBNAILBOTTOMSPACE = 5;
	public static final int MAX_THUMBNAILBOTTOMSPACE = 100;
	public static final int DEFAULT_FILENAMEBOTTOMSPACE = 5;
	public static final int MAX_FILENAMEBOTTOMSPACE = 100;
	public static final int DEFAULT_BOOKSHELFBRIGHTLEVEL = 100;
	public static final int MAX_BOOKSHELFBRIGHTLEVEL = 100;
	public static final int DEFAULT_BOOKSHELFEDGELEVEL = 5;
	public static final int MAX_BOOKSHELFEDGELEVEL = 10;

	public static final int MAX_SCROLL = 9;
	public static final int MAX_CLICKAREA = 100;
	public static final int MAX_PAGERANGE = 25;
	public static final int MAX_ORGWIDTH = 32;
	public static final int MAX_ORGHEIGHT = 32;
	public static final int MAX_TAPRANGE = 50; // スクロール開始の感度
	public static final int MAX_MARGIN = 20;
	public static final int MAX_LONGTAP = 16;
	public static final int MIN_MENULONGTAP = 0; // 0.5秒
	public static final int MAX_MENULONGTAP = 40; // 4秒
	public static final int MAX_WADJUST = DEFAULT_WADJUST * 2;
	public static final int MAX_WSCALING = DEFAULT_WSCALING * 2;
	public static final int MAX_SCALING = 225;
	public static final int MAX_CENTER = 30; // 30ドット
	public static final int MAX_GRADATION = 20; // 20%
	public static final int MAX_FONTTITLE = 44; // 50ドット
	public static final int MAX_FONTMAIN = 44; // 50ドット
	public static final int MAX_FONTSUB = 44; // 50ドット
	public static final int MAX_FONTTILE = 44; // 50ドット
	public static final int MAX_MEMSIZE = 198; // 2000MByte
	public static final int MAX_MEMNEXT = 100; // 200ページ
	public static final int MAX_MEMPREV = 100; // 200ページ
	public static final int MAX_NOISESCRL = 39; // 200ドット
	public static final int MAX_NOISEUNDER = 50; // 5000
	public static final int MAX_NOISEOVER = 50; // 5000
	public static final int MAX_VOLSCRL = 39; // 200ドット
	public static final int MAX_SCRLRNGW = 19; // 100% ((19+1)*5)
	public static final int MAX_SCRLRNGH = 19; // 100% ((19+1)*5)
	public static final int MAX_ITEMMARGIN = 30; // 50ドット
	public static final int MAX_EFFECTTIME = 20; // 1000msec
	public static final int MAX_MOMENTMODE = 16; // 1000msec
	public static final int MAX_AUTOPLAY = 59; // 30sec (0.5*(59+1))

	public static final int MAX_TOOLBARSEEK = 36; // 60 (36+24)
	public static final int MAX_THUMBSIZE = 60; // 640 (60 * 10 + 40)
	public static final int MAX_LISTTHUMBSIZE = 60; // 640 (60 * 10 + 40)

	public static final int MAX_PNUMSIZE = 54; // 6 + 24 = 60px

	public static final int MAX_TOOLBAR_SIZE = 6; // 200%

	// テキストビュワー設定
	public static final String KEY_TX_INISCALE = "txIniScale";
	public static final String KEY_TX_INITVIEW = "txInitView";
	public static final String KEY_TX_VIEWROTA = "txViewRota";
	public static final String KEY_TX_PAPER = "txPaperSize";
	public static final String KEY_TX_PICSIZE = "txPicSize";
	public static final String KEY_TX_NOTICE = "txNotice";
	public static final String KEY_TX_NOSLEEP = "txNoSleep";
	public static final String KEY_TX_CMARGIN = "txCMargin";
	public static final String KEY_TX_CSHADOW = "txCShadow";
	public static final String KEY_TX_EFFECT = "txEffect";
	public static final String KEY_TX_FONTDL = "txFontDL";
	public static final String KEY_TX_FONTNAME = "txFontName";
	public static final String KEY_TX_PAGESELECT = "txPageSelect";
	public static final String KEY_TX_ASCMODE = "txAscMode";
	public static final String KEY_EP_VIEWER = "epubViewer";
	public static final String KEY_EP_ORDER = "epubOrder";
	public static final String KEY_EP_THUMB = "epubThumb";

	public static final String KEY_TX_MGNRGB = "txMgnRGB";
	public static final String KEY_TX_CNTRGB = "txCntRGB";
	public static final String KEY_TX_GUIRGB = "txGuiRGB";

	public static final String KEY_TX_TVTRGB = "txTvtRGB";
	public static final String KEY_TX_TVBRGB = "txTvbRGB";
	public static final String KEY_TX_TVGRGB = "txTvgRGB";
	public static final String KEY_TX_GRADATION = "txGradation";
	public static final String KEY_TX_HITRGB = "txHitRGB";

	public static final String KEY_TX_FONTTOP = "txFontTopSp";
	public static final String KEY_TX_FONTBODY = "txFontBodySp";
	public static final String KEY_TX_FONTRUBI = "txFontRubiSp";
	public static final String KEY_TX_FONTINFO = "txFontInfoSp";
	public static final String KEY_TX_SPACEW = "txSpaceW";
	public static final String KEY_TX_SPACEH = "txSpaceH";
	public static final String KEY_TX_MARGINW = "txMarginW";
	public static final String KEY_TX_MARGINH = "txMarginH";

	public static final String KEY_TX_SCRLRNGW = "txScrlRngW";
	public static final String KEY_TX_SCRLRNGH = "txScrlRngH";

	public static final String KEY_TX_BKLIGHT = "txBright";

	public static final int DEFAULT_TX_FONTTOP = 18; // 32(8～64)
	public static final int DEFAULT_TX_FONTBODY = 14; // 24(8～64)
	public static final int DEFAULT_TX_FONTRUBI = 6; // 16(8～64)
	public static final int DEFAULT_TX_FONTINFO = 10; // 18(8～64)
	public static final int DEFAULT_TX_SPACEW = 8; // 8(0～50)
	public static final int DEFAULT_TX_SPACEH = 2; // 8(0～50)
	public static final int DEFAULT_TX_MARGINW = 16; // 32(0～100,x2)
	public static final int DEFAULT_TX_MARGINH = 16; // 32(0～100,x2)
	public static final int DEFAULT_TX_SCRLRNGW = 6; // 35% ((6+1)*5)
	public static final int DEFAULT_TX_SCRLRNGH = 6; // 35% ((6+1)*5)

	public static final int MAX_TX_FONTTOP = 56; // 64ドット
	public static final int MAX_TX_FONTBODY = 56; // 64ドット
	public static final int MAX_TX_FONTRUBI = 56; // 64ドット
	public static final int MAX_TX_FONTINFO = 56; // 64ドット
	public static final int MAX_TX_SPACEW = 50; // 50ドット
	public static final int MAX_TX_SPACEH = 50; // 50ドット
	public static final int MAX_TX_MARGINW = 50; // 100ドット
	public static final int MAX_TX_MARGINH = 50; // 100ドット
	public static final int MAX_TX_SCRLRNGW = 19; // 100% ((19+1)*5)
	public static final int MAX_TX_SCRLRNGH = 19; // 100% ((19+1)*5)

	public static final int COLOR_TX_TVTRGB = 0xFF000000;
	public static final int COLOR_TX_TVBRGB = 0xFFFFFFC8;
	public static final int COLOR_TX_TVGRGB = 0xFFFFFFFF;
	public static final int COLOR_TX_HITRGB = 0xFFEBC5E2;

	public static final int VOLKEY_NONE = 0;
	public static final int VOLKEY_DOWNTONEXT = 1;
	public static final int VOLKEY_UPTONEXT = 2;

	// サムネイルサイズ
	public static final int THUMBID_NONE = -1;
	public static final int THUMBSIZE_NONE = -1;

	public static final int THUMBSTATE_NONE = -1;
	public static final int THUMBSTATE_NOLOAD = -2;
	public static final int THUMBSTATE_ERROR = -3;

	public static final int CACHE_MAXSTORE = 500;

	// 3秒以内にbackを押せば終了
	public static final long MILLIS_EXITTIME = 2000;
	public static final long MILLIS_DELETECHE = (10 * 60 * 1000); // 10分

	public static final int PAPERSEL_SCREEN = 0;
	public static final int[][] PAPERSIZE = {{0, 0}, {800, 1280}, {720, 1280}, {540, 960}, {480, 800}};

	public static final String URL_IMAGEDETIAL = "";
	public static final String URL_FILESELECT = "https://docs.google.com/document/d/197jmNnXY3BP4F9HHmJCuroWslbTFH8nWTBVWbn8T4dE/edit";
	public static final String URL_COMMON = "";
	public static final String URL_IMAGEVIEW = "";
	public static final String URL_SERVER = "";

	public static final int DISPMODE_IM_NORMAL = 0;
	public static final int DISPMODE_IM_DUAL = 1;
	public static final int DISPMODE_IM_HALF = 2;
	public static final int DISPMODE_IM_EXCHANGE = 3;

	public static final int DISPMODE_TX_DUAL = 0;
	public static final int DISPMODE_TX_HALF = 1;
	public static final int DISPMODE_TX_SERIAL = 2;

	public static final int LASTOPEN_NONE = 0;
	public static final int LASTOPEN_TEXT = 1;
	public static final int LASTOPEN_IMAGE = 2;
	public static final int LASTOPEN_EPUB = 3;

	public static final int SHOWMENU_NONE = 0;
	public static final int SHOWMENU_ALWAYS = 1;
	public static final int SHOWMENU_LOCAL = 2;
	public static final int SHOWMENU_SERVER = 3;

	public static final int ALIGN_TOP = 0;
	public static final int ALIGN_CENTER = 1;
	public static final int ALIGN_BOTTOM = 2;

	public static final int READTEXTNORMAL = 0;
	public static final int READTEXTNONE = 1;
	public static final int READTEXTAFTERDATESIZE = 2;

	public static final String KEY_TAP_I_PATTERN_NUMBER = "TapPatINum";
	public static final String KEY_TAP_T_PATTERN_NUMBER = "TapPatTNum";

	public static final String KEY_TAP_PATTERN_01_01 = "TapPat0101";
	public static final String KEY_TAP_PATTERN_01_02 = "TapPat0102";
	public static final String KEY_TAP_PATTERN_01_03 = "TapPat0103";
	public static final String KEY_TAP_PATTERN_01_04 = "TapPat0104";

	public static final String KEY_TAP_PATTERN_02_01 = "TapPat0201";
	public static final String KEY_TAP_PATTERN_02_02 = "TapPat0202";
	public static final String KEY_TAP_PATTERN_02_03 = "TapPat0203";
	public static final String KEY_TAP_PATTERN_02_04 = "TapPat0204";
	public static final String KEY_TAP_PATTERN_02_05 = "TapPat0205";
	public static final String KEY_TAP_PATTERN_02_06 = "TapPat0206";

	public static final String KEY_TAP_PATTERN_03_01 = "TapPat0301";
	public static final String KEY_TAP_PATTERN_03_02 = "TapPat0302";
	public static final String KEY_TAP_PATTERN_03_03 = "TapPat0303";
	public static final String KEY_TAP_PATTERN_03_04 = "TapPat0304";
	public static final String KEY_TAP_PATTERN_03_05 = "TapPat0305";
	public static final String KEY_TAP_PATTERN_03_06 = "TapPat0306";

	public static final String KEY_TAP_PATTERN_04_01 = "TapPat0401";
	public static final String KEY_TAP_PATTERN_04_02 = "TapPat0402";
	public static final String KEY_TAP_PATTERN_04_03 = "TapPat0403";
	public static final String KEY_TAP_PATTERN_04_04 = "TapPat0404";
	public static final String KEY_TAP_PATTERN_04_05 = "TapPat0405";
	public static final String KEY_TAP_PATTERN_04_06 = "TapPat0406";
	public static final String KEY_TAP_PATTERN_04_07 = "TapPat0407";
	public static final String KEY_TAP_PATTERN_04_08 = "TapPat0408";
	public static final String KEY_TAP_PATTERN_04_09 = "TapPat0409";

	public static final String KEY_TAP_PATTERN_05_01 = "TapPat0501";
	public static final String KEY_TAP_PATTERN_05_02 = "TapPat0502";
	public static final String KEY_TAP_PATTERN_05_03 = "TapPat0503";
	public static final String KEY_TAP_PATTERN_05_04 = "TapPat0504";
	public static final String KEY_TAP_PATTERN_05_05 = "TapPat0505";
	public static final String KEY_TAP_PATTERN_05_06 = "TapPat0506";
	public static final String KEY_TAP_PATTERN_05_07 = "TapPat0507";
	public static final String KEY_TAP_PATTERN_05_08 = "TapPat0508";
	public static final String KEY_TAP_PATTERN_05_09 = "TapPat0509";
	public static final String KEY_TAP_PATTERN_05_10 = "TapPat0510";
	public static final String KEY_TAP_PATTERN_05_11 = "TapPat0511";

	public static final String KEY_TAP_PATTERN_06_01 = "TapPat0601";
	public static final String KEY_TAP_PATTERN_06_02 = "TapPat0602";
	public static final String KEY_TAP_PATTERN_06_03 = "TapPat0603";
	public static final String KEY_TAP_PATTERN_06_04 = "TapPat0604";
	public static final String KEY_TAP_PATTERN_06_05 = "TapPat0605";

	public static final String KEY_TAP_PATTERN_07_01 = "TapPat0701";
	public static final String KEY_TAP_PATTERN_07_02 = "TapPat0702";
	public static final String KEY_TAP_PATTERN_07_03 = "TapPat0703";
	public static final String KEY_TAP_PATTERN_07_04 = "TapPat0604";
	public static final String KEY_TAP_PATTERN_07_05 = "TapPat0605";
	public static final String KEY_TAP_PATTERN_07_06 = "TapPat0606";
	public static final String KEY_TAP_PATTERN_07_07 = "TapPat0607";

	public static final String KEY_TAP_PATTERN_08_01 = "TapPat0801";
	public static final String KEY_TAP_PATTERN_08_02 = "TapPat0802";
	public static final String KEY_TAP_PATTERN_08_03 = "TapPat0803";
	public static final String KEY_TAP_PATTERN_08_04 = "TapPat0804";
	public static final String KEY_TAP_PATTERN_08_05 = "TapPat0805";

	public static final String KEY_TAP_PATTERN_09_01 = "TapPat0901";
	public static final String KEY_TAP_PATTERN_09_02 = "TapPat0902";
	public static final String KEY_TAP_PATTERN_09_03 = "TapPat0903";
	public static final String KEY_TAP_PATTERN_09_04 = "TapPat0904";
	public static final String KEY_TAP_PATTERN_09_05 = "TapPat0905";
	public static final String KEY_TAP_PATTERN_09_06 = "TapPat0906";
	public static final String KEY_TAP_PATTERN_09_07 = "TapPat0907";

	public static final String KEY_TAP_PATTERN_T_01_01 = "TapPat0101";
	public static final String KEY_TAP_PATTERN_T_01_02 = "TapPat0102";
	public static final String KEY_TAP_PATTERN_T_01_03 = "TapPat0103";
	public static final String KEY_TAP_PATTERN_T_01_04 = "TapPat0104";

	public static final String KEY_TAP_PATTERN_T_02_01 = "TapPatt0201";
	public static final String KEY_TAP_PATTERN_T_02_02 = "TapPatt0202";
	public static final String KEY_TAP_PATTERN_T_02_03 = "TapPatt0203";
	public static final String KEY_TAP_PATTERN_T_02_04 = "TapPatt0204";
	public static final String KEY_TAP_PATTERN_T_02_05 = "TapPatt0205";
	public static final String KEY_TAP_PATTERN_T_02_06 = "TapPatt0206";

	public static final String KEY_TAP_PATTERN_T_03_01 = "TapPatt0301";
	public static final String KEY_TAP_PATTERN_T_03_02 = "TapPatt0302";
	public static final String KEY_TAP_PATTERN_T_03_03 = "TapPatt0303";
	public static final String KEY_TAP_PATTERN_T_03_04 = "TapPatt0304";
	public static final String KEY_TAP_PATTERN_T_03_05 = "TapPatt0305";
	public static final String KEY_TAP_PATTERN_T_03_06 = "TapPatt0306";

	public static final String KEY_TAP_PATTERN_T_04_01 = "TapPatt0401";
	public static final String KEY_TAP_PATTERN_T_04_02 = "TapPatt0402";
	public static final String KEY_TAP_PATTERN_T_04_03 = "TapPatt0403";
	public static final String KEY_TAP_PATTERN_T_04_04 = "TapPatt0404";
	public static final String KEY_TAP_PATTERN_T_04_05 = "TapPatt0405";
	public static final String KEY_TAP_PATTERN_T_04_06 = "TapPatt0406";
	public static final String KEY_TAP_PATTERN_T_04_07 = "TapPatt0407";
	public static final String KEY_TAP_PATTERN_T_04_08 = "TapPatt0408";
	public static final String KEY_TAP_PATTERN_T_04_09 = "TapPatt0409";

	public static final String KEY_TAP_PATTERN_T_05_01 = "TapPatt0501";
	public static final String KEY_TAP_PATTERN_T_05_02 = "TapPatt0502";
	public static final String KEY_TAP_PATTERN_T_05_03 = "TapPatt0503";
	public static final String KEY_TAP_PATTERN_T_05_04 = "TapPatt0504";
	public static final String KEY_TAP_PATTERN_T_05_05 = "TapPatt0505";
	public static final String KEY_TAP_PATTERN_T_05_06 = "TapPatt0506";
	public static final String KEY_TAP_PATTERN_T_05_07 = "TapPatt0507";
	public static final String KEY_TAP_PATTERN_T_05_08 = "TapPatt0508";
	public static final String KEY_TAP_PATTERN_T_05_09 = "TapPatt0509";
	public static final String KEY_TAP_PATTERN_T_05_10 = "TapPatt0510";
	public static final String KEY_TAP_PATTERN_T_05_11 = "TapPatt0511";

	public static final String KEY_TAP_PATTERN_T_06_01 = "TapPatt0601";
	public static final String KEY_TAP_PATTERN_T_06_02 = "TapPatt0602";
	public static final String KEY_TAP_PATTERN_T_06_03 = "TapPatt0603";
	public static final String KEY_TAP_PATTERN_T_06_04 = "TapPatt0604";
	public static final String KEY_TAP_PATTERN_T_06_05 = "TapPatt0605";

	public static final String KEY_TAP_PATTERN_T_07_01 = "TapPatt0701";
	public static final String KEY_TAP_PATTERN_T_07_02 = "TapPatt0702";
	public static final String KEY_TAP_PATTERN_T_07_03 = "TapPatt0703";
	public static final String KEY_TAP_PATTERN_T_07_04 = "TapPatt0604";
	public static final String KEY_TAP_PATTERN_T_07_05 = "TapPatt0605";
	public static final String KEY_TAP_PATTERN_T_07_06 = "TapPatt0606";
	public static final String KEY_TAP_PATTERN_T_07_07 = "TapPatt0607";

	public static final String KEY_TAP_PATTERN_T_08_01 = "TapPatt0801";
	public static final String KEY_TAP_PATTERN_T_08_02 = "TapPatt0802";
	public static final String KEY_TAP_PATTERN_T_08_03 = "TapPatt0803";
	public static final String KEY_TAP_PATTERN_T_08_04 = "TapPatt0804";
	public static final String KEY_TAP_PATTERN_T_08_05 = "TapPatt0805";

	public static final String KEY_TAP_PATTERN_T_09_01 = "TapPatt0901";
	public static final String KEY_TAP_PATTERN_T_09_02 = "TapPatt0902";
	public static final String KEY_TAP_PATTERN_T_09_03 = "TapPatt0903";
	public static final String KEY_TAP_PATTERN_T_09_04 = "TapPatt0904";
	public static final String KEY_TAP_PATTERN_T_09_05 = "TapPatt0905";
	public static final String KEY_TAP_PATTERN_T_09_06 = "TapPatt0906";
	public static final String KEY_TAP_PATTERN_T_09_07 = "TapPatt0907";

	public static final int TAP_NONE = 0;
	public static final int TAP_NOSELECT = 1;
	public static final int TAP_BACK = 2;					// 戻る
	public static final int TAP_TOOLBARLEFTMOST = 3;		// 一番左のページ
	public static final int TAP_TOOLBARLEFT100 = 4;			// 左へ100ページ
	public static final int TAP_TOOLBARLEFT10 = 5;			// 左へ10ページ
	public static final int TAP_TOOLBARLEFT1 = 6;			// 左へ1ページ
	public static final int TAP_TOOLBARRIGHT1 = 7;			// 右へ1ページ
	public static final int TAP_TOOLBARRIGHT10 = 8;			// 右へ10ページ
	public static final int TAP_TOOLBARRIGHT100 = 9;		// 右へ100ページ
	public static final int TAP_TOOLBARRIGHTMOST = 10;		// 一番右のページ
	public static final int TAP_TOOLBARNEXTSCROLL = 11;		// 次のページへスクロール
	public static final int TAP_TOOLBARPREVSCROLL = 12;		// 前のページへスクロール
	public static final int TAP_PINCHSCALEUP = 13;			// ズームアップ
	public static final int TAP_PINCHSCALEDOWN = 14;			// ズームダウン
	public static final int TAP_TOOLBARBOOKLEFT = 15;		// 前のファイル(最終ページ)/次のファイル(先頭ページ)
	public static final int TAP_TOOLBARBOOKRIGHT = 16;		// 次のファイル(先頭ページ)/前のファイル(最終ページ)
	public static final int TAP_TOOLBARBOOKMARKLEFT = 17;	// 前(次)のファイル(しおり位置)
	public static final int TAP_TOOLBARBOOKMARKRIGHT = 18;	// 次(前)のファイル(しおり位置)
	public static final int TAP_TOOLBARTHUMBSLIDER = 19;	// サムネイル/スライダー切り替え
	public static final int TAP_TOOLBARDIRTREE = 20;		// サブディレクトリ選択
	public static final int TAP_TOOLBARTOC = 21;			// 見出し選択
	public static final int TAP_TOOLBARSEARCH = 22;			// 検索文字列設定
	public static final int TAP_TOOLBARSHARE = 23;			// 共有
	public static final int TAP_TOOLBARROTATE = 24;			// 画面方向切り替え
	public static final int TAP_TOOLBARROTATEIMAGE = 25;	// 画像の回転
	public static final int TAP_TOOLBARSELECTTHUM = 26;		// サムネイルに設定
	public static final int TAP_TOOLBARTRIMTHUMB = 27;		// 範囲選択してサムネイルに設定
	public static final int TAP_TOOLBARMENU = 28;			// メニューを開く
	public static final int TAP_TOOLBARCONFIG = 29;			// 設定画面を開く
	public static final int TAP_PULLDOWNMENU = 30;	// プルダウンメニューを開く

	public static final int TAP_SCRLWAY00 = 31;	// 横方向→縦方向
	public static final int TAP_SCRLWAY01 = 32;	// 縦方向→横方向
	public static final int TAP_TOOLBARDISPLAYPOSITION00 = 33;	// 中心へ移動
	public static final int TAP_TOOLBARDISPLAYPOSITION01 = 34;	// 上へ移動
	public static final int TAP_TOOLBARDISPLAYPOSITION02 = 35;	// 下へ移動
	public static final int TAP_TOOLBARDISPLAYPOSITION03 = 36;	// 左へ移動
	public static final int TAP_TOOLBARDISPLAYPOSITION04 = 37;	// 右へ移動
	public static final int TAP_TOOLBARDISPLAYPOSITION05 = 38;	// 左上へ移動
	public static final int TAP_TOOLBARDISPLAYPOSITION06 = 39;	// 右上へ移動
	public static final int TAP_TOOLBARDISPLAYPOSITION07 = 40;	// 左下へ移動
	public static final int TAP_TOOLBARDISPLAYPOSITION08 = 41;	// 右下へ移動
	public static final int TAP_SELROTA00 = 42;	// 回転無しで表示
	public static final int TAP_SELROTA01 = 43;	// 90°回転して表示
	public static final int TAP_SELROTA02 = 44;	// 180°回転して表示
	public static final int TAP_SELROTA03 = 45;	// 270°回転して表示
	public static final int TAP_SELSIZE00 = 46;	// 元のサイズで表示
	public static final int TAP_SELSIZE01 = 47;	// 幅に合わせて表示
	public static final int TAP_SELSIZE02 = 48;	// 高さに合わせて表示
	public static final int TAP_SELSIZE03 = 49;	// 全体を表示
	public static final int TAP_SELSIZE04 = 50;	// 画面全体で表示
	public static final int TAP_SELSIZE05 = 51;	// 画面全体で表示(見開き対応)
	public static final int TAP_SELSIZE06 = 52;	// 幅に合わせて表示(見開き対応)
	public static final int TAP_SELSIZE07 = 53;	// 全体を表示(見開き対応)
	public static final int TAP_SELVIEW00 = 54;	// そのまま表示
	public static final int TAP_SELVIEW01 = 55;	// 見開き表示
	public static final int TAP_SELVIEW02 = 56;	// 単ページ表示
	public static final int TAP_SELVIEW03 = 57;	// 連続表示
	public static final int TAP_SELVIEW04 = 58;	// 単ページ／見開き
	public static final int TAP_PICSIZE00 = 59;	// 元画像サイズ
	public static final int TAP_PICSIZE01 = 60;	// 元画像サイズの2倍
	public static final int TAP_PICSIZE02 = 61;	// 元画像サイズの3倍
	public static final int TAP_PICSIZE03 = 62;	// 元画像サイズの4倍
	public static final int TAP_PICSIZE04 = 63;	// 画面サイズに拡大
	public static final int TAP_PICSIZE05 = 64;	// 画面サイズに拡大(余白無視)
	public static final int TAP_PICSIZE06 = 65;	// 画面サイズに拡大(見開き対応)
	public static final int TAP_PICSIZE07 = 66;	// 画面サイズに拡大(見開き対応:余白無視)
	public static final int TAP_ASCMODE00 = 67;	// 縦表示
	public static final int TAP_ASCMODE01 = 68;	// 横表示(回転)
	public static final int TAP_ASCMODE02 = 69;	// 縦/横併用
	public static final int TAP_MGNCUTMENU = 70;	// 余白削除
	public static final int TAP_MGNCUTCOLORMENU = 71;	// 余白削除の色
	public static final int TAP_IMGCONFMENU = 72;	// 画像表示設定
	public static final int TAP_TXTCONFMENU = 73;	// テキスト表示設定
	public static final int TAP_TGUIDE02 = 74;		// 見開き設定
	public static final int TAP_TGUIDE03 = 75;		// 画像サイズ
	public static final int TAP_NOISEMENU = 76;		// 音操作
	public static final int TAP_PLAYMENU = 77;		// オートプレイ開始
	public static final int TAP_ADDBOOKMARKMENU = 78;// ブックマーク追加
	public static final int TAP_SELBOOKMARKMENU = 79;// ブックマーク選択
	public static final int TAP_SHARPENMENU = 80;	// シャープ化
	public static final int TAP_MOIREMENU = 81;		// モアレ軽減
	public static final int TAP_BRIGHTMENU = 82;	// 明るさ補正
	public static final int TAP_GAMMAMENU = 83;		// ガンマ補正
	public static final int TAP_CONTRASTMENU = 84;	// コントラスト
	public static final int TAP_HUEMENU = 85;		// 色相
	public static final int TAP_SATURATIONMENU = 86;	// 彩度
	public static final int TAP_BKLIGHTMENU = 87;	// バックライト
	public static final int TAP_INVERTMENU = 88;	// 白黒反転
	public static final int TAP_GRAYMENU = 89;		// グレースケール
	public static final int TAP_COLORINGMENU = 90;	// 自動着色
	public static final int TAP_ALGORIMENU = 91;	// 画像補間方式
	public static final int TAP_REVERSEMENU = 92;	// ページ逆順
	public static final int TAP_CHGOPEMENU = 93;	// 操作入れ替え
	public static final int TAP_PAGEWAYMENU = 94;	// 表紙方向
	public static final int TAP_SCRLWAY2MENU = 95;	// スクロール方向入れ替え
	public static final int TAP_SETTOPMENU = 96;	// 上部メニュー設定
	public static final int TAP_CMARGIN = 97;		// 中央余白表示
	public static final int TAP_CSHADOW = 98;		// 中央影表示
	public static final int TAP_DISPLAYPOSITIONMENU = 99;		// 画面の表示位置
	public static final int TAP_SETPROFILE = 100;		// プロファイルの登録
	public static final int TAP_DELPROFILE = 101;		// プロファイルの削除
	public static final int TAP_PROFILE1 = 102;		// プロファイル1
	public static final int TAP_PROFILE2 = 103;		// プロファイル2
	public static final int TAP_PROFILE3 = 104;		// プロファイル3
	public static final int TAP_PROFILE4 = 105;		// プロファイル4
	public static final int TAP_PROFILE5 = 106;		// プロファイル5
	public static final int TAP_EXIT_VIEWER = 107;	// ビューアの終了
	public static final int TAP_PROFILE6 = 108;		// プロファイル6
	public static final int TAP_PROFILE7 = 109;		// プロファイル7
	public static final int TAP_PROFILE8 = 110;		// プロファイル8
	public static final int TAP_PROFILE9 = 111;		// プロファイル9
	public static final int TAP_PROFILE10 = 112;		// プロファイル10
	public static final int TAP_SWSCREENORIENT = 113;	// 表示方向の切り替え

	// イメージビューア用のタップ操作の初期値
	public static final int TAP_PATTERN_I01_DEFAULT_01 = TAP_TOOLBARLEFT1;
	public static final int TAP_PATTERN_I01_DEFAULT_02 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_I01_DEFAULT_03 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_I01_DEFAULT_04 = TAP_TOOLBARPREVSCROLL;

	public static final int TAP_PATTERN_I02_DEFAULT_01 = TAP_TOOLBARLEFT1;
	public static final int TAP_PATTERN_I02_DEFAULT_02 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_I02_DEFAULT_03 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_I02_DEFAULT_04 = TAP_TOOLBARPREVSCROLL;
	public static final int TAP_PATTERN_I02_DEFAULT_05 = TAP_TOOLBARLEFTMOST;
	public static final int TAP_PATTERN_I02_DEFAULT_06 = TAP_TOOLBARRIGHTMOST;

	public static final int TAP_PATTERN_I03_DEFAULT_01 = TAP_TOOLBARLEFT1;
	public static final int TAP_PATTERN_I03_DEFAULT_02 = TAP_PULLDOWNMENU;
	public static final int TAP_PATTERN_I03_DEFAULT_03 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_I03_DEFAULT_04 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_I03_DEFAULT_05 = TAP_TOOLBARCONFIG;
	public static final int TAP_PATTERN_I03_DEFAULT_06 = TAP_TOOLBARPREVSCROLL;

	public static final int TAP_PATTERN_I04_DEFAULT_01 = TAP_TOOLBARLEFT1;
	public static final int TAP_PATTERN_I04_DEFAULT_02 = TAP_PULLDOWNMENU;
	public static final int TAP_PATTERN_I04_DEFAULT_03 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_I04_DEFAULT_04 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_I04_DEFAULT_05 = TAP_TOOLBARCONFIG;
	public static final int TAP_PATTERN_I04_DEFAULT_06 = TAP_TOOLBARPREVSCROLL;
	public static final int TAP_PATTERN_I04_DEFAULT_07 = TAP_TOOLBARLEFTMOST;
	public static final int TAP_PATTERN_I04_DEFAULT_08 = TAP_TOOLBARMENU;
	public static final int TAP_PATTERN_I04_DEFAULT_09 = TAP_TOOLBARRIGHTMOST;

	public static final int TAP_PATTERN_I05_DEFAULT_01 = TAP_TOOLBARLEFT10;
	public static final int TAP_PATTERN_I05_DEFAULT_02 = TAP_PULLDOWNMENU;
	public static final int TAP_PATTERN_I05_DEFAULT_03 = TAP_TOOLBARRIGHT10;
	public static final int TAP_PATTERN_I05_DEFAULT_04 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_I05_DEFAULT_05 = TAP_PINCHSCALEUP;
	public static final int TAP_PATTERN_I05_DEFAULT_06 = TAP_TOOLBARCONFIG;
	public static final int TAP_PATTERN_I05_DEFAULT_07 = TAP_PINCHSCALEDOWN;
	public static final int TAP_PATTERN_I05_DEFAULT_08 = TAP_TOOLBARPREVSCROLL;
	public static final int TAP_PATTERN_I05_DEFAULT_09 = TAP_TOOLBARLEFTMOST;
	public static final int TAP_PATTERN_I05_DEFAULT_10 = TAP_TOOLBARMENU;
	public static final int TAP_PATTERN_I05_DEFAULT_11 = TAP_TOOLBARRIGHTMOST;

	public static final int TAP_PATTERN_I06_DEFAULT_01 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_I06_DEFAULT_02 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_I06_DEFAULT_03 = TAP_TOOLBARCONFIG;
	public static final int TAP_PATTERN_I06_DEFAULT_04 = TAP_TOOLBARPREVSCROLL;
	public static final int TAP_PATTERN_I06_DEFAULT_05 = TAP_TOOLBARLEFT1;

	public static final int TAP_PATTERN_I07_DEFAULT_01 = TAP_TOOLBARLEFT1;
	public static final int TAP_PATTERN_I07_DEFAULT_02 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_I07_DEFAULT_03 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_I07_DEFAULT_04 = TAP_TOOLBARCONFIG;
	public static final int TAP_PATTERN_I07_DEFAULT_05 = TAP_TOOLBARPREVSCROLL;
	public static final int TAP_PATTERN_I07_DEFAULT_06 = TAP_TOOLBARLEFTMOST;
	public static final int TAP_PATTERN_I07_DEFAULT_07 = TAP_TOOLBARRIGHTMOST;

	public static final int TAP_PATTERN_I08_DEFAULT_01 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_I08_DEFAULT_02 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_I08_DEFAULT_03 = TAP_TOOLBARPREVSCROLL;
	public static final int TAP_PATTERN_I08_DEFAULT_04 = TAP_TOOLBARCONFIG;
	public static final int TAP_PATTERN_I08_DEFAULT_05 = TAP_TOOLBARLEFT1;

	public static final int TAP_PATTERN_I09_DEFAULT_01 = TAP_TOOLBARLEFT1;
	public static final int TAP_PATTERN_I09_DEFAULT_02 = TAP_PULLDOWNMENU;
	public static final int TAP_PATTERN_I09_DEFAULT_03 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_I09_DEFAULT_04 = TAP_TOOLBARCONFIG;
	public static final int TAP_PATTERN_I09_DEFAULT_05 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_I09_DEFAULT_06 = TAP_TOOLBARMENU;
	public static final int TAP_PATTERN_I09_DEFAULT_07 = TAP_TOOLBARPREVSCROLL;

	// テキストビューア用のタップ操作の初期値
	public static final int TAP_PATTERN_T01_DEFAULT_01 = TAP_TOOLBARLEFT1;
	public static final int TAP_PATTERN_T01_DEFAULT_02 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_T01_DEFAULT_03 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_T01_DEFAULT_04 = TAP_TOOLBARPREVSCROLL;

	public static final int TAP_PATTERN_T02_DEFAULT_01 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_T02_DEFAULT_02 = TAP_TOOLBARLEFT1;
	public static final int TAP_PATTERN_T02_DEFAULT_03 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_T02_DEFAULT_04 = TAP_TOOLBARPREVSCROLL;
	public static final int TAP_PATTERN_T02_DEFAULT_05 = TAP_TOOLBARLEFTMOST;
	public static final int TAP_PATTERN_T02_DEFAULT_06 = TAP_TOOLBARRIGHTMOST;

	public static final int TAP_PATTERN_T03_DEFAULT_01 = TAP_TOOLBARLEFT1;
	public static final int TAP_PATTERN_T03_DEFAULT_02 = TAP_PULLDOWNMENU;
	public static final int TAP_PATTERN_T03_DEFAULT_03 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_T03_DEFAULT_04 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_T03_DEFAULT_05 = TAP_TOOLBARCONFIG;
	public static final int TAP_PATTERN_T03_DEFAULT_06 = TAP_TOOLBARPREVSCROLL;

	public static final int TAP_PATTERN_T04_DEFAULT_01 = TAP_TOOLBARLEFT1;
	public static final int TAP_PATTERN_T04_DEFAULT_02 = TAP_PULLDOWNMENU;
	public static final int TAP_PATTERN_T04_DEFAULT_03 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_T04_DEFAULT_04 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_T04_DEFAULT_05 = TAP_TOOLBARCONFIG;
	public static final int TAP_PATTERN_T04_DEFAULT_06 = TAP_TOOLBARPREVSCROLL;
	public static final int TAP_PATTERN_T04_DEFAULT_07 = TAP_TOOLBARLEFTMOST;
	public static final int TAP_PATTERN_T04_DEFAULT_08 = TAP_TOOLBARMENU;
	public static final int TAP_PATTERN_T04_DEFAULT_09 = TAP_TOOLBARRIGHTMOST;

	public static final int TAP_PATTERN_T05_DEFAULT_01 = TAP_TOOLBARLEFT10;
	public static final int TAP_PATTERN_T05_DEFAULT_02 = TAP_PULLDOWNMENU;
	public static final int TAP_PATTERN_T05_DEFAULT_03 = TAP_TOOLBARRIGHT10;
	public static final int TAP_PATTERN_T05_DEFAULT_04 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_T05_DEFAULT_05 = TAP_PINCHSCALEUP;
	public static final int TAP_PATTERN_T05_DEFAULT_06 = TAP_TOOLBARCONFIG;
	public static final int TAP_PATTERN_T05_DEFAULT_07 = TAP_PINCHSCALEDOWN;
	public static final int TAP_PATTERN_T05_DEFAULT_08 = TAP_TOOLBARPREVSCROLL;
	public static final int TAP_PATTERN_T05_DEFAULT_09 = TAP_TOOLBARLEFTMOST;
	public static final int TAP_PATTERN_T05_DEFAULT_10 = TAP_TOOLBARMENU;
	public static final int TAP_PATTERN_T05_DEFAULT_11 = TAP_TOOLBARRIGHTMOST;

	public static final int TAP_PATTERN_T06_DEFAULT_01 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_T06_DEFAULT_02 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_T06_DEFAULT_03 = TAP_TOOLBARCONFIG;
	public static final int TAP_PATTERN_T06_DEFAULT_04 = TAP_TOOLBARPREVSCROLL;
	public static final int TAP_PATTERN_T06_DEFAULT_05 = TAP_TOOLBARLEFT1;

	public static final int TAP_PATTERN_T07_DEFAULT_01 = TAP_TOOLBARLEFT1;
	public static final int TAP_PATTERN_T07_DEFAULT_02 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_T07_DEFAULT_03 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_T07_DEFAULT_04 = TAP_TOOLBARCONFIG;
	public static final int TAP_PATTERN_T07_DEFAULT_05 = TAP_TOOLBARPREVSCROLL;
	public static final int TAP_PATTERN_T07_DEFAULT_06 = TAP_TOOLBARLEFTMOST;
	public static final int TAP_PATTERN_T07_DEFAULT_07 = TAP_TOOLBARRIGHTMOST;

	public static final int TAP_PATTERN_T08_DEFAULT_01 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_T08_DEFAULT_02 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_T08_DEFAULT_03 = TAP_TOOLBARPREVSCROLL;
	public static final int TAP_PATTERN_T08_DEFAULT_04 = TAP_TOOLBARCONFIG;
	public static final int TAP_PATTERN_T08_DEFAULT_05 = TAP_TOOLBARLEFT1;

	public static final int TAP_PATTERN_T09_DEFAULT_01 = TAP_TOOLBARLEFT1;
	public static final int TAP_PATTERN_T09_DEFAULT_02 = TAP_PULLDOWNMENU;
	public static final int TAP_PATTERN_T09_DEFAULT_03 = TAP_TOOLBARRIGHT1;
	public static final int TAP_PATTERN_T09_DEFAULT_04 = TAP_TOOLBARCONFIG;
	public static final int TAP_PATTERN_T09_DEFAULT_05 = TAP_TOOLBARNEXTSCROLL;
	public static final int TAP_PATTERN_T09_DEFAULT_06 = TAP_TOOLBARMENU;
	public static final int TAP_PATTERN_T09_DEFAULT_07 = TAP_TOOLBARPREVSCROLL;

	// スマホ/タブレット
	public static final int CODE_BACK = 1;		//	バックキー
	public static final int CODE_VOLUME_UP = 2;	//	ボリュームUPキー
	public static final int CODE_VOLUME_DOWN = 3;	//	ボリュームDOWNキー
	public static final int CODE_CAMERA = 4;	//	カメラボタン
	public static final int CODE_FOCUS = 5;	//	フォーカスボタン
	// キーボード
	public static final int CODE_MENU = 6;		//	メニューキー
	public static final int CODE_DPAD_LEFT = 7;	//	カーソル左キー
	public static final int CODE_DPAD_RIGHT = 8;	//	カーソル右キー
	public static final int CODE_DPAD_UP = 9;	//	カーソル上キー
	public static final int CODE_DPAD_DOWN = 10;	//	カーソル下キー
	public static final int CODE_DPAD_CENTER = 11;	// 決定キー
	public static final int CODE_ENTER = 12;	// エンターキー
	public static final int CODE_DEL = 13;	//	削除キー
	public static final int CODE_SPACE = 14;	//	スペースキー
	public static final int CODE_SERACH = 15;	//	検索キー
	public static final int CODE_PAGEUP = 16;	//	ページアップキー
	public static final int CODE_PAGEDOWN = 17;	//	ページダウンキー
	public static final int CODE_ESCAPE = 18;	//	エスケープキー
	public static final int CODE_MOVEHOME = 19;	//	ホームキー
	public static final int CODE_MOVEEND = 20;	//	エンドキー
	public static final int CODE_FORWARD = 21;	//	フォワードボタン
	// ゲームパッド
	public static final int CODE_BUTTON_L1 = 22;		//	L1ボタン
	public static final int CODE_BUTTON_L2 = 23;		//	L2ボタン
	public static final int CODE_BUTTON_THUMBL = 24;	//	L3ボタン
	public static final int CODE_BUTTON_R1 = 25;		//	R1ボタン
	public static final int CODE_BUTTON_R2 = 26;		//	R2ボタン
	public static final int CODE_BUTTON_THUMBR = 27;	//	R3ボタン
	public static final int CODE_BUTTON_A = 28;		//	Aボタン
	public static final int CODE_BUTTON_B = 29;		//	Bボタン
	public static final int CODE_BUTTON_X = 30;		//	Xボタン
	public static final int CODE_BUTTON_Y = 31;		//	Yボタン
	public static final int CODE_BUTTON_SELECT = 32;	//	SELECTボタン
	public static final int CODE_BUTTON_START = 33;	//	STARTボタン
	// メディアプレイヤー
	public static final int CODE_MEDIANEXT = 34;	//	曲送りボタン
	public static final int CODE_MEDIAPREV = 35;	//	曲戻しボタン
	public static final int CODE_MEDIAPLAYPAUSE = 36;	//	再生一時停止ボタン
	// カスタムキー
	public static final int CODE_CUSTOMKEY01 = 37;	//	カスタムキー1
	public static final int CODE_CUSTOMKEY02 = 38;	//	カスタムキー2
	public static final int CODE_CUSTOMKEY03 = 39;	//	カスタムキー3
	public static final int CODE_CUSTOMKEY04 = 40;	//	カスタムキー4
	public static final int CODE_CUSTOMKEY05 = 41;	//	カスタムキー5
	public static final int CODE_CUSTOMKEY06 = 42;	//	カスタムキー6
	public static final int CODE_CUSTOMKEY07 = 43;	//	カスタムキー7
	public static final int CODE_CUSTOMKEY08 = 44;	//	カスタムキー8
	public static final int CODE_CUSTOMKEY09 = 45;	//	カスタムキー9
	public static final int CODE_CUSTOMKEY10 = 46;	//	カスタムキー10

	public static final int KEY_CODE_CUSTOM_MAX = 10;

	// イメージビューア用
	// スマホ/タブレット
	public static final String KEY_CODE_I_BACK = "CodeIBack";		//	バックキー
	public static final String KEY_CODE_I_VOLUME_UP = "CodeIVolUp";	//	ボリュームUPキー
	public static final String KEY_CODE_I_VOLUME_DOWN = "CodeIVolDown";	//	ボリュームDOWNキー
	public static final String KEY_CODE_I_CAMERA = "CodeICamera";	//	カメラボタン
	public static final String KEY_CODE_I_FOCUS = "CodeIFocus";	//	フォーカスボタン
	// キーボード
	public static final String KEY_CODE_I_MENU = "CodeIMenu";		//	メニューキー
	public static final String KEY_CODE_I_DPAD_LEFT = "CodeIDpadLeft";	//	カーソル左キー
	public static final String KEY_CODE_I_DPAD_RIGHT = "CodeIDpadRight";	//	カーソル右キー
	public static final String KEY_CODE_I_DPAD_UP = "CodeIDpadUp";	//	カーソル上キー
	public static final String KEY_CODE_I_DPAD_DOWN = "CodeIDpadDown";	//	カーソル下キー
	public static final String KEY_CODE_I_DPAD_CENTER = "CodeIDpadCenter";	// 決定キー
	public static final String KEY_CODE_I_ENTER = "CodeIEnter";	//	エンターキー
	public static final String KEY_CODE_I_DEL = "CodeIDel";	//	削除キー
	public static final String KEY_CODE_I_SPACE = "CodeISpace";	//	スペースキー
	public static final String KEY_CODE_I_SEARCH = "CodeISearch";	//	検索キー
	public static final String KEY_CODE_I_PAGE_UP = "CodeIPageUp";	//	ページアップキー
	public static final String KEY_CODE_I_PAGE_DOWN = "CodeIPageDown";	//	ページダウンキー
	public static final String KEY_CODE_I_ESCAPE = "CodeIEscape";	//	エスケープキー
	public static final String KEY_CODE_I_MOVEHOME = "CodeIMoveHome";	//	ホームキー
	public static final String KEY_CODE_I_MOVEEND = "CodeIMoveEnd";	//	エンドキー
	public static final String KEY_CODE_I_FORWARD = "CodeIForward";	//	フォワードボタン
	// ゲームパッド
	public static final String KEY_CODE_I_BUTTON_L1 = "CodeIL1";		//	L1ボタン
	public static final String KEY_CODE_I_BUTTON_L2 = "CodeIL2";		//	L2ボタン
	public static final String KEY_CODE_I_BUTTON_THUMBL = "CodeIThumbL";	//	L3ボタン
	public static final String KEY_CODE_I_BUTTON_R1 = "CodeIR1";		//	R1ボタン
	public static final String KEY_CODE_I_BUTTON_R2 = "CodeIR2";		//	R2ボタン
	public static final String KEY_CODE_I_BUTTON_THUMBR = "CodeIThumbR";	//	R3ボタン
	public static final String KEY_CODE_I_BUTTON_A = "CodeIA";		//	Aボタン
	public static final String KEY_CODE_I_BUTTON_B = "CodeIB";		//	Bボタン
	public static final String KEY_CODE_I_BUTTON_X = "CodeIX";		//	Xボタン
	public static final String KEY_CODE_I_BUTTON_Y = "CodeIY";		//	Yボタン
	public static final String KEY_CODE_I_BUTTON_SELECT = "CodeISelect";	//	SELECTボタン
	public static final String KEY_CODE_I_BUTTON_START = "CodeIStart";	//	STARTボタン
	// メディアプレイヤー
	public static final String KEY_CODE_I_MEDIA_NEXT = "CodeIMediaNext";	//	曲送りボタン
	public static final String KEY_CODE_I_MEDIA_PREVIOUS = "CodeIMediaPrev";	//	曲戻しボタン
	public static final String KEY_CODE_I_MEDIA_PLAY_PAUSE = "CodeIMediaPlayPause";	//	再生一時停止ボタン
	// カスタムキー
	public static final String KEY_CODE_I_CUSTOMKEY01 = "CustomKeyI01";	//	カスタムキー1
	public static final String KEY_CODE_I_CUSTOMKEY02 = "CustomKeyI02";	//	カスタムキー2
	public static final String KEY_CODE_I_CUSTOMKEY03 = "CustomKeyI03";	//	カスタムキー3
	public static final String KEY_CODE_I_CUSTOMKEY04 = "CustomKeyI04";	//	カスタムキー4
	public static final String KEY_CODE_I_CUSTOMKEY05 = "CustomKeyI05";	//	カスタムキー5
	public static final String KEY_CODE_I_CUSTOMKEY06 = "CustomKeyI06";	//	カスタムキー6
	public static final String KEY_CODE_I_CUSTOMKEY07 = "CustomKeyI07";	//	カスタムキー7
	public static final String KEY_CODE_I_CUSTOMKEY08 = "CustomKeyI08";	//	カスタムキー8
	public static final String KEY_CODE_I_CUSTOMKEY09 = "CustomKeyI09";	//	カスタムキー9
	public static final String KEY_CODE_I_CUSTOMKEY10 = "CustomKeyI10";	//	カスタムキー10

	// テキストビューア用
	// スマホ/タブレット
	public static final String KEY_CODE_T_BACK = "CodeTBack";		//	バックキー
	public static final String KEY_CODE_T_VOLUME_UP = "CodeTVolUp";	//	ボリュームUPキー
	public static final String KEY_CODE_T_VOLUME_DOWN = "CodeTVolDown";	//	ボリュームDOWNキー
	public static final String KEY_CODE_T_CAMERA = "CodeTCamera";	//	カメラボタン
	public static final String KEY_CODE_T_FOCUS = "CodeTFocus";	//	フォーカスボタン
	// キーボード
	public static final String KEY_CODE_T_MENU = "CodeTMenu";		//	メニューキー
	public static final String KEY_CODE_T_DPAD_LEFT = "CodeTDpadLeft";	//	カーソル左キー
	public static final String KEY_CODE_T_DPAD_RIGHT = "CodeTDpadRight";	//	カーソル右キー
	public static final String KEY_CODE_T_DPAD_UP = "CodeTDpadUp";	//	カーソル上キー
	public static final String KEY_CODE_T_DPAD_DOWN = "CodeTDpadDown";	//	カーソル下キー
	public static final String KEY_CODE_T_DPAD_CENTER = "CodeTDpadCenter";	// 決定キー
	public static final String KEY_CODE_T_ENTER = "CodeTEnter";	//	エンターキー
	public static final String KEY_CODE_T_DEL = "CodeTDel";	//	削除キー
	public static final String KEY_CODE_T_SPACE = "CodeTSpace";	//	スペースキー
	public static final String KEY_CODE_T_SEARCH = "CodeTSearch";	//	検索キー
	public static final String KEY_CODE_T_PAGE_UP = "CodeTPageUp";	//	ページアップキー
	public static final String KEY_CODE_T_PAGE_DOWN = "CodeTPageDown";	//	ページダウンキー
	public static final String KEY_CODE_T_ESCAPE = "CodeTEscape";	//	エスケープキー
	public static final String KEY_CODE_T_MOVEHOME = "CodeTMoveHome";	//	ホームキー
	public static final String KEY_CODE_T_MOVEEND = "CodeTMoveEnd";	//	エンドキー
	public static final String KEY_CODE_T_FORWARD = "CodeTForward";	//	フォワードボタン
	// ゲームパッド
	public static final String KEY_CODE_T_BUTTON_L1 = "CodeTL1";		//	L1ボタン
	public static final String KEY_CODE_T_BUTTON_L2 = "CodeTL2";		//	L2ボタン
	public static final String KEY_CODE_T_BUTTON_THUMBL = "CodeTThumbL";	//	L3ボタン
	public static final String KEY_CODE_T_BUTTON_R1 = "CodeTR1";		//	R1ボタン
	public static final String KEY_CODE_T_BUTTON_R2 = "CodeTR2";		//	R2ボタン
	public static final String KEY_CODE_T_BUTTON_THUMBR = "CodeTThumbR";	//	R3ボタン
	public static final String KEY_CODE_T_BUTTON_A = "CodeTA";		//	Aボタン
	public static final String KEY_CODE_T_BUTTON_B = "CodeTB";		//	Bボタン
	public static final String KEY_CODE_T_BUTTON_X = "CodeTX";		//	Xボタン
	public static final String KEY_CODE_T_BUTTON_Y = "CodeTY";		//	Yボタン
	public static final String KEY_CODE_T_BUTTON_SELECT = "CodeTSelect";	//	SELECTボタン
	public static final String KEY_CODE_T_BUTTON_START = "CodeTStart";	//	STARTボタン
	// メディアプレイヤー
	public static final String KEY_CODE_T_MEDIA_NEXT = "CodeTMediaNext";	//	曲送りボタン
	public static final String KEY_CODE_T_MEDIA_PREVIOUS = "CodeTMediaPrev";	//	曲戻しボタン
	public static final String KEY_CODE_T_MEDIA_PLAY_PAUSE = "CodeTMediaPlayPause";	//	再生一時停止ボタン
	// カスタムキー
	public static final String KEY_CODE_T_CUSTOMKEY01 = "CustomKeyT01";	//	カスタムキー1
	public static final String KEY_CODE_T_CUSTOMKEY02 = "CustomKeyT02";	//	カスタムキー2
	public static final String KEY_CODE_T_CUSTOMKEY03 = "CustomKeyT03";	//	カスタムキー3
	public static final String KEY_CODE_T_CUSTOMKEY04 = "CustomKeyT04";	//	カスタムキー4
	public static final String KEY_CODE_T_CUSTOMKEY05 = "CustomKeyT05";	//	カスタムキー5
	public static final String KEY_CODE_T_CUSTOMKEY06 = "CustomKeyT06";	//	カスタムキー6
	public static final String KEY_CODE_T_CUSTOMKEY07 = "CustomKeyT07";	//	カスタムキー7
	public static final String KEY_CODE_T_CUSTOMKEY08 = "CustomKeyT08";	//	カスタムキー8
	public static final String KEY_CODE_T_CUSTOMKEY09 = "CustomKeyT09";	//	カスタムキー9
	public static final String KEY_CODE_T_CUSTOMKEY10 = "CustomKeyT10";	//	カスタムキー10

	// ファイルリスト用
	// スマホ/タブレット
	public static final String KEY_CODE_F_BACK = "CodeFBack";		//	バックキー
	public static final String KEY_CODE_F_VOLUME_UP = "CodeFVolUp";	//	ボリュームUPキー
	public static final String KEY_CODE_F_VOLUME_DOWN = "CodeFVolDown";	//	ボリュームDOWNキー
	public static final String KEY_CODE_F_CAMERA = "CodeFCamera";	//	カメラボタン
	public static final String KEY_CODE_F_FOCUS = "CodeFFocus";	//	フォーカスボタン
	// キーボード
	public static final String KEY_CODE_F_MENU = "CodeFMenu";		//	メニューキー
	public static final String KEY_CODE_F_DPAD_LEFT = "CodeFDpadLeft";	//	カーソル左キー
	public static final String KEY_CODE_F_DPAD_RIGHT = "CodeFDpadRight";	//	カーソル右キー
	public static final String KEY_CODE_F_DPAD_UP = "CodeFDpadUp";	//	カーソル上キー
	public static final String KEY_CODE_F_DPAD_DOWN = "CodeFDpadDown";	//	カーソル下キー
	public static final String KEY_CODE_F_DPAD_CENTER = "CodeFDpadCenter";	// 決定キー
	public static final String KEY_CODE_F_ENTER = "CodeFEnter";	//	エンターキー
	public static final String KEY_CODE_F_DEL = "CodeFDel";	//	削除キー
	public static final String KEY_CODE_F_SPACE = "CodeFSpace";	//	スペースキー
	public static final String KEY_CODE_F_SEARCH = "CodeFSearch";	//	検索キー
	public static final String KEY_CODE_F_PAGE_UP = "CodeFPageUp";	//	ページアップキー
	public static final String KEY_CODE_F_PAGE_DOWN = "CodeFPageDown";	//	ページダウンキー
	public static final String KEY_CODE_F_ESCAPE = "CodeFEscape";	//	エスケープキー
	public static final String KEY_CODE_F_MOVEHOME = "CodeFMoveHome";	//	ホームキー
	public static final String KEY_CODE_F_MOVEEND = "CodeFMoveEnd";	//	エンドキー
	public static final String KEY_CODE_F_FORWARD = "CodeFForward";	//	フォワードボタン
	// ゲームパッド
	public static final String KEY_CODE_F_BUTTON_L1 = "CodeFL1";		//	L1ボタン
	public static final String KEY_CODE_F_BUTTON_L2 = "CodeFL2";		//	L2ボタン
	public static final String KEY_CODE_F_BUTTON_THUMBL = "CodeFThumbL";	//	L3ボタン
	public static final String KEY_CODE_F_BUTTON_R1 = "CodeFR1";		//	R1ボタン
	public static final String KEY_CODE_F_BUTTON_R2 = "CodeFR2";		//	R2ボタン
	public static final String KEY_CODE_F_BUTTON_THUMBR = "CodeFThumbR";	//	R3ボタン
	public static final String KEY_CODE_F_BUTTON_A = "CodeFA";		//	Aボタン
	public static final String KEY_CODE_F_BUTTON_B = "CodeFB";		//	Bボタン
	public static final String KEY_CODE_F_BUTTON_X = "CodeFX";		//	Xボタン
	public static final String KEY_CODE_F_BUTTON_Y = "CodeFY";		//	Yボタン
	public static final String KEY_CODE_F_BUTTON_SELECT = "CodeFSelect";	//	SELECTボタン
	public static final String KEY_CODE_F_BUTTON_START = "CodeFStart";	//	STARTボタン
	// メディアプレイヤー
	public static final String KEY_CODE_F_MEDIA_NEXT = "CodeFMediaNext";	//	曲送りボタン
	public static final String KEY_CODE_F_MEDIA_PREVIOUS = "CodeFMediaPrev";	//	曲戻しボタン
	public static final String KEY_CODE_F_MEDIA_PLAY_PAUSE = "CodeFMediaPlayPause";	//	再生一時停止ボタン
	// カスタムキー
	public static final String KEY_CODE_F_CUSTOMKEY01 = "CustomKeyF01";	//	カスタムキー1
	public static final String KEY_CODE_F_CUSTOMKEY02 = "CustomKeyF02";	//	カスタムキー2
	public static final String KEY_CODE_F_CUSTOMKEY03 = "CustomKeyF03";	//	カスタムキー3
	public static final String KEY_CODE_F_CUSTOMKEY04 = "CustomKeyF04";	//	カスタムキー4
	public static final String KEY_CODE_F_CUSTOMKEY05 = "CustomKeyF05";	//	カスタムキー5
	public static final String KEY_CODE_F_CUSTOMKEY06 = "CustomKeyF06";	//	カスタムキー6
	public static final String KEY_CODE_F_CUSTOMKEY07 = "CustomKeyF07";	//	カスタムキー7
	public static final String KEY_CODE_F_CUSTOMKEY08 = "CustomKeyF08";	//	カスタムキー8
	public static final String KEY_CODE_F_CUSTOMKEY09 = "CustomKeyF09";	//	カスタムキー9
	public static final String KEY_CODE_F_CUSTOMKEY10 = "CustomKeyF10";	//	カスタムキー10

	public static final int TAP_FILELIST_NONE = 0;
	public static final int TAP_FILELIST_NOSELECT = 1;
	public static final int TAP_FILELIST_BACK = 2;					// 戻る
	public static final int TAP_FILELIST_ENTER = 3;		// 
	public static final int TAP_FILELIST_LEFT = 4;		// 
	public static final int TAP_FILELIST_RIGHT = 5;		// 
	public static final int TAP_FILELIST_UP = 6;		// 
	public static final int TAP_FILELIST_DOWN = 7;		// 
	public static final int TAP_FILELIST_HOME = 8;		// 
	public static final int TAP_FILELIST_END = 9;		// 
	public static final int TAP_FILELIST_PARENT_DIR = 10;		// 
	public static final int TAP_FILELIST_SELECT_LIST = 11;
	public static final int TAP_FILELIST_MOVELISTUP = 12;
	public static final int TAP_FILELIST_MOVELISTDOWN = 13;
	public static final int TAP_FILELIST_REFRESH = 14;
	public static final int TAP_FILELIST_CHANGELISTMODE = 15;
	public static final int TAP_FILELIST_ENDAPP = 16;
	public static final int TAP_FILELIST_ROTATE = 17;

	public static final String KEY_CUSTOMKEY_TITLE_01 = "Customkeytitle01";
	public static final String KEY_CUSTOMKEY_TITLE_02 = "Customkeytitle02";
	public static final String KEY_CUSTOMKEY_TITLE_03 = "Customkeytitle03";
	public static final String KEY_CUSTOMKEY_TITLE_04 = "Customkeytitle04";
	public static final String KEY_CUSTOMKEY_TITLE_05 = "Customkeytitle05";
	public static final String KEY_CUSTOMKEY_TITLE_06 = "Customkeytitle06";
	public static final String KEY_CUSTOMKEY_TITLE_07 = "Customkeytitle07";
	public static final String KEY_CUSTOMKEY_TITLE_08 = "Customkeytitle08";
	public static final String KEY_CUSTOMKEY_TITLE_09 = "Customkeytitle09";
	public static final String KEY_CUSTOMKEY_TITLE_10 = "Customkeytitle10";

	public static final String KEY_CUSTOMKEY_CODE_01 = "Customkeycode01";
	public static final String KEY_CUSTOMKEY_CODE_02 = "Customkeycode02";
	public static final String KEY_CUSTOMKEY_CODE_03 = "Customkeycode03";
	public static final String KEY_CUSTOMKEY_CODE_04 = "Customkeycode04";
	public static final String KEY_CUSTOMKEY_CODE_05 = "Customkeycode05";
	public static final String KEY_CUSTOMKEY_CODE_06 = "Customkeycode06";
	public static final String KEY_CUSTOMKEY_CODE_07 = "Customkeycode07";
	public static final String KEY_CUSTOMKEY_CODE_08 = "Customkeycode08";
	public static final String KEY_CUSTOMKEY_CODE_09 = "Customkeycode09";
	public static final String KEY_CUSTOMKEY_CODE_10 = "Customkeycode10";

	public static final int HARDWARE_NONE = 0;

	// ハードウェアキー割り当てコード
	public static final int KEYCODE_HOME = 3;
	public static final int KEYCODE_BACK = 4;
	public static final int KEYCODE_DPAD_UP = 19;
	public static final int KEYCODE_DPAD_DOWN = 20;
	public static final int KEYCODE_DPAD_LEFT = 21;
	public static final int KEYCODE_DPAD_RIGHT = 22;
	public static final int KEYCODE_DPAD_CENTER = 23;
	public static final int KEYCODE_VOLUME_UP = 24;
	public static final int KEYCODE_VOLUME_DOWN = 25;
	public static final int KEYCODE_CAMERA = 27;
	public static final int KEYCODE_SPACE = 62;
	public static final int KEYCODE_ENTER = 66;
	public static final int KEYCODE_DEL = 67;
	public static final int KEYCODE_MENU = 82;
	public static final int KEYCODE_FOCUS = 80;
	public static final int KEYCODE_SEARCH = 84;
	public static final int KEYCODE_MEDIA_PLAY_PAUSE = 85;
	public static final int KEYCODE_MEDIA_NEXT = 87;
	public static final int KEYCODE_MEDIA_PREVIOUS = 88;
	public static final int KEYCODE_PAGE_UP = 92;
	public static final int KEYCODE_PAGE_DOWN = 93;
	public static final int KEYCODE_BUTTON_A = 96;
	public static final int KEYCODE_BUTTON_B = 97;
	public static final int KEYCODE_BUTTON_X = 99;
	public static final int KEYCODE_BUTTON_Y = 100;
	public static final int KEYCODE_BUTTON_L1 = 102;
	public static final int KEYCODE_BUTTON_R1 = 103;
	public static final int KEYCODE_BUTTON_L2 = 104;
	public static final int KEYCODE_BUTTON_R2 = 105;
	public static final int KEYCODE_BUTTON_THUMBL = 106;
	public static final int KEYCODE_BUTTON_THUMBR = 107;
	public static final int KEYCODE_BUTTON_START = 108;
	public static final int KEYCODE_BUTTON_SELECT = 109;
	public static final int KEYCODE_ESCAPE = 111;
	public static final int KEYCODE_MOVE_HOME = 122;
	public static final int KEYCODE_MOVE_END = 123;
	public static final int KEYCODE_FORWARD = 125;

	public static final int[] KEYCODE_INDEX =
	{
		DEF.KEYCODE_BACK,
		DEF.KEYCODE_VOLUME_UP,
		DEF.KEYCODE_VOLUME_DOWN,
		DEF.KEYCODE_CAMERA,
		DEF.KEYCODE_FOCUS,
		DEF.KEYCODE_MENU,
		DEF.KEYCODE_DPAD_LEFT,
		DEF.KEYCODE_DPAD_RIGHT,
		DEF.KEYCODE_DPAD_UP,
		DEF.KEYCODE_DPAD_DOWN,
		DEF.KEYCODE_DPAD_CENTER,
		DEF.KEYCODE_ENTER,
		DEF.KEYCODE_DEL,
		DEF.KEYCODE_SPACE,
		DEF.KEYCODE_SEARCH,
		DEF.KEYCODE_PAGE_UP,
		DEF.KEYCODE_PAGE_DOWN,
		DEF.KEYCODE_ESCAPE,
		DEF.KEYCODE_MOVE_HOME,
		DEF.KEYCODE_MOVE_END,
		DEF.KEYCODE_FORWARD,
		DEF.KEYCODE_BUTTON_L1,
		DEF.KEYCODE_BUTTON_L2,
		DEF.KEYCODE_BUTTON_THUMBL,
		DEF.KEYCODE_BUTTON_R1,
		DEF.KEYCODE_BUTTON_R2,
		DEF.KEYCODE_BUTTON_THUMBR,
		DEF.KEYCODE_BUTTON_A,
		DEF.KEYCODE_BUTTON_B,
		DEF.KEYCODE_BUTTON_X,
		DEF.KEYCODE_BUTTON_Y,
		DEF.KEYCODE_BUTTON_SELECT,
		DEF.KEYCODE_BUTTON_START,
		DEF.KEYCODE_MEDIA_NEXT,
		DEF.KEYCODE_MEDIA_PREVIOUS,
		DEF.KEYCODE_MEDIA_PLAY_PAUSE
	};

	public static final int[] HardwareKeyTitleName =
		{ R.string.hardwarekeytitle00
		, R.string.hardwarekeytitle01
		, R.string.hardwarekeytitle02
		, R.string.hardwarekeytitle03
		, R.string.hardwarekeytitle04
		, R.string.hardwarekeytitle05
		, R.string.hardwarekeytitle06
		, R.string.hardwarekeytitle07
		, R.string.hardwarekeytitle08
		, R.string.hardwarekeytitle09
		, R.string.hardwarekeytitle10
		, R.string.hardwarekeytitle11
		, R.string.hardwarekeytitle12
		, R.string.hardwarekeytitle13
		, R.string.hardwarekeytitle14
		, R.string.hardwarekeytitle15
		, R.string.hardwarekeytitle16
		, R.string.hardwarekeytitle17
		, R.string.hardwarekeytitle18
		, R.string.hardwarekeytitle19
		, R.string.hardwarekeytitle20
		, R.string.hardwarekeytitle21
		, R.string.hardwarekeytitle22
		, R.string.hardwarekeytitle23
		, R.string.hardwarekeytitle24
		, R.string.hardwarekeytitle25
		, R.string.hardwarekeytitle26
		, R.string.hardwarekeytitle27
		, R.string.hardwarekeytitle28
		, R.string.hardwarekeytitle29
		, R.string.hardwarekeytitle30
		, R.string.hardwarekeytitle31
		, R.string.hardwarekeytitle32
		, R.string.hardwarekeytitle33
		, R.string.hardwarekeytitle34
		, R.string.hardwarekeytitle35
		, R.string.hardwarekeytitle36
		, R.string.hardwarekeytitle37
		, R.string.hardwarekeytitle38
		, R.string.hardwarekeytitle39
		, R.string.hardwarekeytitle40
		, R.string.hardwarekeytitle41
		, R.string.hardwarekeytitle42
		, R.string.hardwarekeytitle43
		, R.string.hardwarekeytitle44
		, R.string.hardwarekeytitle45 };

	public static final String[] HardwareImageKeyIdName =
		{ DEF.KEY_CODE_I_BACK
		, DEF.KEY_CODE_I_VOLUME_UP
		, DEF.KEY_CODE_I_VOLUME_DOWN
		, DEF.KEY_CODE_I_CAMERA
		, DEF.KEY_CODE_I_FOCUS
		, DEF.KEY_CODE_I_MENU
		, DEF.KEY_CODE_I_DPAD_LEFT
		, DEF.KEY_CODE_I_DPAD_RIGHT
		, DEF.KEY_CODE_I_DPAD_UP
		, DEF.KEY_CODE_I_DPAD_DOWN
		, DEF.KEY_CODE_I_DPAD_CENTER
		, DEF.KEY_CODE_I_ENTER
		, DEF.KEY_CODE_I_DEL
		, DEF.KEY_CODE_I_SPACE
		, DEF.KEY_CODE_I_SEARCH
		, DEF.KEY_CODE_I_PAGE_UP
		, DEF.KEY_CODE_I_PAGE_DOWN
		, DEF.KEY_CODE_I_ESCAPE
		, DEF.KEY_CODE_I_MOVEHOME
		, DEF.KEY_CODE_I_MOVEEND
		, DEF.KEY_CODE_I_FORWARD
		, DEF.KEY_CODE_I_BUTTON_L1
		, DEF.KEY_CODE_I_BUTTON_L2
		, DEF.KEY_CODE_I_BUTTON_THUMBL
		, DEF.KEY_CODE_I_BUTTON_R1
		, DEF.KEY_CODE_I_BUTTON_R2
		, DEF.KEY_CODE_I_BUTTON_THUMBR
		, DEF.KEY_CODE_I_BUTTON_A
		, DEF.KEY_CODE_I_BUTTON_B
		, DEF.KEY_CODE_I_BUTTON_X
		, DEF.KEY_CODE_I_BUTTON_Y
		, DEF.KEY_CODE_I_BUTTON_SELECT
		, DEF.KEY_CODE_I_BUTTON_START
		, DEF.KEY_CODE_I_MEDIA_NEXT
		, DEF.KEY_CODE_I_MEDIA_PREVIOUS
		, DEF.KEY_CODE_I_MEDIA_PLAY_PAUSE
		, DEF.KEY_CODE_I_CUSTOMKEY01
		, DEF.KEY_CODE_I_CUSTOMKEY02
		, DEF.KEY_CODE_I_CUSTOMKEY03
		, DEF.KEY_CODE_I_CUSTOMKEY04
		, DEF.KEY_CODE_I_CUSTOMKEY05
		, DEF.KEY_CODE_I_CUSTOMKEY06
		, DEF.KEY_CODE_I_CUSTOMKEY07
		, DEF.KEY_CODE_I_CUSTOMKEY08
		, DEF.KEY_CODE_I_CUSTOMKEY09
		, DEF.KEY_CODE_I_CUSTOMKEY10 };

	public static final String[] HardwareTextKeyIdName =
		{ DEF.KEY_CODE_T_BACK
		, DEF.KEY_CODE_T_VOLUME_UP
		, DEF.KEY_CODE_T_VOLUME_DOWN
		, DEF.KEY_CODE_T_CAMERA
		, DEF.KEY_CODE_T_FOCUS
		, DEF.KEY_CODE_T_MENU
		, DEF.KEY_CODE_T_DPAD_LEFT
		, DEF.KEY_CODE_T_DPAD_RIGHT
		, DEF.KEY_CODE_T_DPAD_UP
		, DEF.KEY_CODE_T_DPAD_DOWN
		, DEF.KEY_CODE_T_DPAD_CENTER
		, DEF.KEY_CODE_T_ENTER
		, DEF.KEY_CODE_T_DEL
		, DEF.KEY_CODE_T_SPACE
		, DEF.KEY_CODE_T_SEARCH
		, DEF.KEY_CODE_T_PAGE_UP
		, DEF.KEY_CODE_T_PAGE_DOWN
		, DEF.KEY_CODE_T_ESCAPE
		, DEF.KEY_CODE_T_MOVEHOME
		, DEF.KEY_CODE_T_MOVEEND
		, DEF.KEY_CODE_T_FORWARD
		, DEF.KEY_CODE_T_BUTTON_L1
		, DEF.KEY_CODE_T_BUTTON_L2
		, DEF.KEY_CODE_T_BUTTON_THUMBL
		, DEF.KEY_CODE_T_BUTTON_R1
		, DEF.KEY_CODE_T_BUTTON_R2
		, DEF.KEY_CODE_T_BUTTON_THUMBR
		, DEF.KEY_CODE_T_BUTTON_A
		, DEF.KEY_CODE_T_BUTTON_B
		, DEF.KEY_CODE_T_BUTTON_X
		, DEF.KEY_CODE_T_BUTTON_Y
		, DEF.KEY_CODE_T_BUTTON_SELECT
		, DEF.KEY_CODE_T_BUTTON_START
		, DEF.KEY_CODE_T_MEDIA_NEXT
		, DEF.KEY_CODE_T_MEDIA_PREVIOUS
		, DEF.KEY_CODE_T_MEDIA_PLAY_PAUSE
		, DEF.KEY_CODE_T_CUSTOMKEY01
		, DEF.KEY_CODE_T_CUSTOMKEY02
		, DEF.KEY_CODE_T_CUSTOMKEY03
		, DEF.KEY_CODE_T_CUSTOMKEY04
		, DEF.KEY_CODE_T_CUSTOMKEY05
		, DEF.KEY_CODE_T_CUSTOMKEY06
		, DEF.KEY_CODE_T_CUSTOMKEY07
		, DEF.KEY_CODE_T_CUSTOMKEY08
		, DEF.KEY_CODE_T_CUSTOMKEY09
		, DEF.KEY_CODE_T_CUSTOMKEY10 };

	public static final String[] HardwareFilelistKeyIdName =
		{ DEF.KEY_CODE_F_BACK
		, DEF.KEY_CODE_F_VOLUME_UP
		, DEF.KEY_CODE_F_VOLUME_DOWN
		, DEF.KEY_CODE_F_CAMERA
		, DEF.KEY_CODE_F_FOCUS
		, DEF.KEY_CODE_F_MENU
		, DEF.KEY_CODE_F_DPAD_LEFT
		, DEF.KEY_CODE_F_DPAD_RIGHT
		, DEF.KEY_CODE_F_DPAD_UP
		, DEF.KEY_CODE_F_DPAD_DOWN
		, DEF.KEY_CODE_F_DPAD_CENTER
		, DEF.KEY_CODE_F_ENTER
		, DEF.KEY_CODE_F_DEL
		, DEF.KEY_CODE_F_SPACE
		, DEF.KEY_CODE_F_SEARCH
		, DEF.KEY_CODE_F_PAGE_UP
		, DEF.KEY_CODE_F_PAGE_DOWN
		, DEF.KEY_CODE_F_ESCAPE
		, DEF.KEY_CODE_F_MOVEHOME
		, DEF.KEY_CODE_F_MOVEEND
		, DEF.KEY_CODE_F_FORWARD
		, DEF.KEY_CODE_F_BUTTON_L1
		, DEF.KEY_CODE_F_BUTTON_L2
		, DEF.KEY_CODE_F_BUTTON_THUMBL
		, DEF.KEY_CODE_F_BUTTON_R1
		, DEF.KEY_CODE_F_BUTTON_R2
		, DEF.KEY_CODE_F_BUTTON_THUMBR
		, DEF.KEY_CODE_F_BUTTON_A
		, DEF.KEY_CODE_F_BUTTON_B
		, DEF.KEY_CODE_F_BUTTON_X
		, DEF.KEY_CODE_F_BUTTON_Y
		, DEF.KEY_CODE_F_BUTTON_SELECT
		, DEF.KEY_CODE_F_BUTTON_START
		, DEF.KEY_CODE_F_MEDIA_NEXT
		, DEF.KEY_CODE_F_MEDIA_PREVIOUS
		, DEF.KEY_CODE_F_MEDIA_PLAY_PAUSE
		, DEF.KEY_CODE_F_CUSTOMKEY01
		, DEF.KEY_CODE_F_CUSTOMKEY02
		, DEF.KEY_CODE_F_CUSTOMKEY03
		, DEF.KEY_CODE_F_CUSTOMKEY04
		, DEF.KEY_CODE_F_CUSTOMKEY05
		, DEF.KEY_CODE_F_CUSTOMKEY06
		, DEF.KEY_CODE_F_CUSTOMKEY07
		, DEF.KEY_CODE_F_CUSTOMKEY08
		, DEF.KEY_CODE_F_CUSTOMKEY09
		, DEF.KEY_CODE_F_CUSTOMKEY10 };

	public static final int DEFAULT_CORNERENDLEVEL = 25;
	public static final int MAX_CORNERENDLEVEL = 50;

	public static final String[] CornerEndKeyIIdName =
		{ DEF.KEY_CORNEREND_I_TAP_01
		, DEF.KEY_CORNEREND_I_TAP_02
		, DEF.KEY_CORNEREND_I_TAP_03
		, DEF.KEY_CORNEREND_I_TAP_04
		, DEF.KEY_CORNEREND_I_TAP_05
		, DEF.KEY_CORNEREND_I_TAP_06
		, DEF.KEY_CORNEREND_I_TAP_07
		, DEF.KEY_CORNEREND_I_TAP_08
		, DEF.KEY_CORNEREND_I_TAP_09
		, DEF.KEY_CORNEREND_I_TAP_10
		, DEF.KEY_CORNEREND_I_TAP_11
		, DEF.KEY_CORNEREND_I_TAP_12
		, DEF.KEY_CORNEREND_I_TAP_13
		, DEF.KEY_CORNEREND_I_TAP_14 };

	public static final String[] CornerEndKeyTIdName =
		{ DEF.KEY_CORNEREND_T_TAP_01
		, DEF.KEY_CORNEREND_T_TAP_02
		, DEF.KEY_CORNEREND_T_TAP_03
		, DEF.KEY_CORNEREND_T_TAP_04
		, DEF.KEY_CORNEREND_T_TAP_05
		, DEF.KEY_CORNEREND_T_TAP_06
		, DEF.KEY_CORNEREND_T_TAP_07
		, DEF.KEY_CORNEREND_T_TAP_08
		, DEF.KEY_CORNEREND_T_TAP_09
		, DEF.KEY_CORNEREND_T_TAP_10
		, DEF.KEY_CORNEREND_T_TAP_11
		, DEF.KEY_CORNEREND_T_TAP_12
		, DEF.KEY_CORNEREND_T_TAP_13
		, DEF.KEY_CORNEREND_T_TAP_14 };

	public static final int[] CornerEndTitleName =
		{ R.string.CornerEndTitle00
		, R.string.CornerEndTitle01
		, R.string.CornerEndTitle02
		, R.string.CornerEndTitle03
		, R.string.CornerEndTitle04
		, R.string.CornerEndTitle05
		, R.string.CornerEndTitle06
		, R.string.CornerEndTitle07
		, R.string.CornerEndTitle08
		, R.string.CornerEndTitle09
		, R.string.CornerEndTitle10
		, R.string.CornerEndTitle11
		, R.string.CornerEndTitle12
		, R.string.CornerEndTitle13 };

	public static final int[] ColorList = {Color.rgb(0, 0, 0) // 0
			, Color.rgb(255, 255, 255) // 1
			, Color.rgb(0, 0, 255) // 2
			, Color.rgb(255, 0, 0) // 3
			, Color.rgb(255, 0, 255) // 4
			, Color.rgb(0, 255, 0) // 5
			, Color.rgb(255, 255, 0) // 6
			, Color.rgb(0, 255, 255) // 7
			, Color.rgb(129, 129, 129) // 8
			, Color.rgb(128, 128, 255) // 9
			, Color.rgb(255, 128, 128) // 10
			, Color.rgb(255, 128, 255) // 11
			, Color.rgb(128, 255, 128) // 12
			, Color.rgb(255, 255, 128) // 13
			, Color.rgb(128, 255, 255) // 14
			, Color.rgb(193, 193, 193) // 15
			, Color.rgb(0, 0, 97) // 16
			, Color.rgb(97, 0, 0) // 17
			, Color.rgb(97, 0, 97) // 18
			, Color.rgb(0, 97, 0) // 19
			, Color.rgb(97, 97, 0) // 20
			, Color.rgb(0, 97, 97) // 21
			, Color.rgb(97, 97, 97) // 22
			, Color.rgb(128, 128, 128)}; // 23
	public static final int[] GuideList = {0x80000000 // 0 : 黒
			, 0x80000070 // 1 : 青
			, 0x80700000 // 2 : 赤
			, 0x80700070 // 3 : マゼンタ
			, 0x80007000 // 4 : 緑
			, 0x80707000 // 5 : 黄
			, 0x80007070}; // 6 : シアン

	public static final int[] RotateBtnList = {0, KeyEvent.KEYCODE_FOCUS // フォーカスキー
			, KeyEvent.KEYCODE_CAMERA // シャッターキー
	};
	public static final String[] CharsetList = {"UTF-8", "Shift_JIS", "EUC-JP", "EUC-KR", "Big5", "CB2312", "GB18030", "Big5-HKSCS"};

	public static final int ROTATE_AUTO = 0;
	public static final int ROTATE_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

	public static final int ROTATE_LANDSCAPE = 2;
	public static final int ROTATE_PSELAND = 3;
	public static final int ROTATE_AUTO_REVERSE_PORTRAIT = 4;
	public static final int ROTATE_AUTO_REVERSE_LANDSCAPE = 5;
	public static final int ROTATE_AUTO_REVERSE_PORTRAIT_LANDSCAPE = 6;
	public static final int ROTATE_REVERSE_PORTRAIT = 7;
	public static final int ROTATE_REVERSE_LANDSCAPE = 8;

	public static final int ROTATE_ALL_AUTO = 0;
	public static final int ROTATE_ALL_PORTRAIT = 1;
	public static final int ROTATE_ALL_LANDSCAPE = 2;
	public static final int ROTATE_ALL_AUTO_REVERSE_PORTRAIT = 3;
	public static final int ROTATE_ALL_AUTO_REVERSE_LANDSCAPE = 4;
	public static final int ROTATE_ALL_AUTO_REVERSE_PORTRAIT_LANDSCAPE = 5;
	public static final int ROTATE_ALL_REVERSE_PORTRAIT = 6;
	public static final int ROTATE_ALL_REVERSE_LANDSCAPE = 7;

	public static final int TEXTSIZE_MESSAGE = 18;
	public static final int TEXTSIZE_SUMMARY = 14;
	public static final int TEXTSIZE_EDIT = 20;

	public static final int THUMBNAIL_PAGESIZE = 2 * 1024 * 1024;    // サムネイルバッファサイズ(4MB)
	public static final int THUMBNAIL_BLOCK = 8 * 1024;                // サムネイルブロックサイズ(8KB)
	public static final int THUMBNAIL_MAXPAGE = 4;                    // サムネイルバッファ数

	public static final boolean TEXT_VIEWER = false;
	public static final boolean IMAGE_VIEWER = true;

	/** 読書中 */
	public static final int PAGENUMBER_READING = 0;
	/** 未読 */
	public static final int PAGENUMBER_UNREAD = -1;
	/** 既読 */
	public static final int PAGENUMBER_READ = -2;
	/** 未定 */
	public static final int PAGENUMBER_NONE = -3;

	// 縦長チェック
	static public boolean checkPortrait(int cx, int cy) {
		return cx <= cy ? true : false;
	}

	static public boolean checkPortrait(int cx, int cy, int rotate) {
		if (rotate == 0 || rotate == 2) {
			return cx <= cy ? true : false;
		} else {
			return cy <= cx ? true : false;
		}
	}

	static public boolean checkHiddenFile(String path) {
		String[] folder = path.split("/");
		if (folder.length == 0) {
			return true;
		}
		// ファイル名が .で始まる
		String top = folder[folder.length - 1].substring(0, 1);
		if (top.equals(".")) {
			return true;
		}
		return false;
	}

	// 色を取得
	public static int getColorValue(SharedPreferences sp, String keyOld, String keyRGB, int defColor) {
		String str = "";
		int index = -1;
		int val;

		// 旧設定から読み込み
		if (keyOld != null) {
			str = sp.getString(keyOld, "");
		}
		if (!str.isEmpty()) {
			index = Integer.parseInt(str);
		}
		if (0 <= index && index < DEF.ColorList.length) {
			// 旧設定がある場合は色情報を取得
			val = DEF.ColorList[index];
		} else {
			// 色情報
			val = sp.getInt(keyRGB, DEF.ColorList[defColor]);
		}
		return val;
	}

	// 色を取得
	public static int getGuideValue(SharedPreferences sp, String keyOld, String keyRGB, int defColor) {
		String str = "";
		int index = -1;
		int val;

		// 旧設定から読み込み
		if (keyOld != null) {
			str = sp.getString(keyOld, "");
		}
		if (!str.isEmpty()) {
			index = Integer.parseInt(str);
		}
		if (0 <= index && index < DEF.GuideList.length) {
			// 旧設定がある場合は色情報を取得
			val = DEF.GuideList[index];
		} else {
			// 色情報
			val = sp.getInt(keyRGB, DEF.GuideList[defColor]);
		}
		return val;
	}

	// 色の補正
	public static int calcColor(int color, int gap) {
		int r = (color >> 16) & 0x000000FF;
		int g = (color >> 8) & 0x000000FF;
		int b = (color >> 0) & 0x000000FF;
		r += gap;
		g += gap;
		b += gap;
		r = r > 255 ? 255 : r;
		g = g > 255 ? 255 : g;
		b = b > 255 ? 255 : b;
		r = r < 0 ? 0 : r;
		g = g < 0 ? 0 : g;
		b = b < 0 ? 0 : b;
		return Color.rgb(r, g, b);
	}

	// 色の補正
	public static int calcGradation(int color, int to, int rate) {
		int r = (color >> 16) & 0x000000FF;
		int g = (color >> 8) & 0x000000FF;
		int b = (color >> 0) & 0x000000FF;
		r = (to - r) * rate / 100 + r;
		g = (to - g) * rate / 100 + g;
		b = (to - b) * rate / 100 + b;
		return Color.rgb(r, g, b);
	}

	// 色のマージ
	public static int margeColor(int from, int to, int rate, int total) {
		int r1 = (from >> 16) & 0x000000FF;
		int g1 = (from >> 8) & 0x000000FF;
		int b1 = (from >> 0) & 0x000000FF;
		int r2 = (to >> 16) & 0x000000FF;
		int g2 = (to >> 8) & 0x000000FF;
		int b2 = (to >> 0) & 0x000000FF;
		r1 += (r2 - r1) * rate / total;
		g1 += (g2 - g1) * rate / total;
		b1 += (b2 - b1) * rate / total;
		return Color.rgb(r1, g1, b1);
	}

	static public String makeCode(String str, int thum_cx, int thum_cy) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			String path = str + ":" + thum_cx + "x" + thum_cy;
			digest.update(path.getBytes());
			byte[] messageDigest = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				hexString.append(Integer.toHexString(messageDigest[i] & 0x00FF));
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			Logcat.e("NoSuchAlgorithmException");
		}
		return "";
	}

	static public boolean setRotation(Activity act, int rotate) {
		int way = act.getRequestedOrientation();
		// 回転制御
		if (rotate == DEF.ROTATE_PORTRAIT || rotate == ROTATE_PSELAND) {
			// 縦固定 又は 疑似横画面
			act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			if (way == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
				return true;
			}
		} else if (rotate == DEF.ROTATE_LANDSCAPE) {
			// 横固定
			act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			if (way == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				return true;
			}
		} else if (rotate == DEF.ROTATE_REVERSE_PORTRAIT) {
			// 縦固定(上下反転)
			act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
			if (way == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
				return true;
			}
		} else if (rotate == DEF.ROTATE_REVERSE_LANDSCAPE) {
			// 横固定(上下反転)
			act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			if (way == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				return true;
			}
		} else {
			// 回転あり
			act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
		return false;
	}

	static public boolean setRotationAll(Activity act, int rotate) {
		int way = act.getRequestedOrientation();
		// 回転制御
		if (rotate == DEF.ROTATE_ALL_PORTRAIT) {
			// 縦固定
			act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if (rotate == DEF.ROTATE_ALL_LANDSCAPE) {
			// 横固定
			act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else if (rotate == DEF.ROTATE_ALL_REVERSE_PORTRAIT) {
			// 縦固定(上下反転)
			act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
		} else if (rotate == DEF.ROTATE_ALL_REVERSE_LANDSCAPE) {
			// 横固定(上下反転)
			act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		} else {
			// 回転あり
			act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
		return false;
	}

	static public int calcThumbnailScale(int width, int height, int tcx, int tcy) {
		if (height >= tcy) {
			return height / tcy;
		} else {
			return 1;
		}
	}

	static public String getSizeStr(int val, String summ1, String summ2) {
		String str;

		if (val == 0) {
			str = summ1;
		} else {
			str = val * 100 + " " + summ2;
		}
		return str;
	}

	// スクロール倍率の計算
	static public int calcScroll(int val) {
		return val + 1;
	}

	// ドット数の計算(等倍)
	static public int calcRange1x(int val) {
		return val;
	}

	// 上下操作エリア
	static public int calcClickArea(int val) {
		return val * 2;
	}

	// 上下操作エリア (sp計算)
	static public int calcClickAreaPix(int val, float density) {
		return (int) (calcClickArea(val) * density);
	}

	// ページ選択感度
	static public int calcPageRange(int val) {
		return val;
	}

	// ページ選択感度 (sp計算)
	static public int calcPageRangePix(int val, float density) {
		return (int) (calcPageRange(val) * density);
	}

	// スクロール開始感度
	static public int calcTapRange(int val) {
		return val * 2;
	}

	// スクロール開始感度 (sp計算)
	static public int calcTapRangePix(int val, float density) {
		return (int) (calcTapRange(val) * density);
	}

	// イメージ、テキストのマージン
	static public int calcDispMargin(int val) {
		return val * 2;
	}

	// ミリ秒の計算
	static public int calcMSec100(int val) {
		return val * 100;
	}

	// ミリ秒の計算
	static public int calcMSec200(int val) {
		return val * 200;
	}

	// エフェクト時間の計算(msec)
	static public int calcEffectTime(int val) {
		return val * 50;
	}

	// 自動再生間隔の計算(100msec)
	static public int calcAutoPlay(int val) {
		return (val + 1) * 500;
	}

	// 縦横比(%)の計算
	static public int calcWAdjust(int val) {
		return (val - DEF.DEFAULT_WADJUST) + 100;
	}

	// 補正(%)の計算
	static public int calcWScaling(int val) {
		return (val - DEF.DEFAULT_WSCALING) + 100;
	}

	// 拡大縮小(%)の計算
	static public int calcScaling(int val) {
		return (val - DEF.DEFAULT_SCALING) + 100;
	}

	// フォントサイズの計算
	static public int calcFont(int val) {
		return (val + 6); // 最小値:2sp
	}

	// フォントサイズの計算(px)
	static public int calcFontPix(int val, float density) {
		return calcSpToPix(calcFont(val), density); // 最小値:8ドット
	}

	// ポイントをpxに変換
	static public int calcSpToPix(int val, float density) {
		return (int) (val * density);
	}

	// ツールバーサイズの計算
	static public int calcToolbarSize(int val) {
		return (val + 24); // 最小値:12sp
	}

	// ツールバーサイズの計算
	static public int calcToolbarPix(int val, float density) {
		return (int) (calcToolbarSize(val) * density); // 最小値:12sp
	}

	// サムネイルサイズの計算
	static public int calcThumbnailSize(int val) {
		return (val * 10 + 40); // 最小値:40px
	}

	// メモリサイズ
	static public int calcMemSize(int val) {
		return val * 10 + 20;
	}

	// キャッシュページ数
	static public int calcMemPage(int val) {
		return val * 2;
	}

	// スクロールドット数
	static public int calcScrlSpeed(int val) {
		return (val + 1);
	}

	// スクロールドット数 (sp単位)
	static public int calcScrlSpeedPix(int val, float density) {
		return (int) (calcScrlSpeed(val) * density);
	}

	// スクロールドット数
	static public int calcScrlRange(int val) {
		return (val + 1) * 5;
	}

	// ノイズレベル
	static public int calcNoiseLevel(int val) {
		return val * 100;
	}

	// ページ番号表示
	static public int calcPnumSize(int val) {
		return val + 6;
	}

	// ページ番号表示
	static public int calcPnumSizePix(int val, float density) {
		return (int) (calcPnumSize(val) * density);
	}

	// 保持件数
	static public int calcSaveNum(int val) {
		int calc_val = 0;
		if (val == 0) calc_val = 0;
		if (val == 1) calc_val = 50;
		if (val == 2) calc_val = 100;
		if (val == 3) calc_val = 200;
		if (val == 4) calc_val = 500;
		if (val == 5) calc_val = 1000;
		return calc_val;
	}

	// サマリ文字列作成
	static public String getScrollStr(int val, String summ1, String summ2) {
		return summ1 + " " + calcScroll(val) + " " + summ2;
	}

	// 操作エリアの文字列
	static public String getClickAreaStr(int val, String summ1) {
		return calcClickArea(val) + " " + summ1;
	}

	// ページ選択感度文字列
	static public String getPageRangeStr(int val, String summ1) {
		return calcPageRange(val) + " " + summ1;
	}

	// スクロール開始感度文字列
	static public String getTapRangeStr(int val, String summ1) {
		return calcTapRange(val) + " " + summ1;
	}

	// イメージ、テキストのマージン文字列
	static public String getDispMarginStr(int val, String summ1) {
		return calcDispMargin(val) + " " + summ1;
	}

	// サマリ文字列作成
	static public String getMarginStr(int val, String summ1) {
		return val + " " + summ1;
	}

	// テキストの字間文字列
	static public String getTextSpaceStr(int val, String summ1) {
		return val + " " + summ1;
	}

	// サマリ文字列作成(ミリ秒)
	static public String getMSecStr100(int val, String summ1) {
		int msec = calcMSec100(val);
		return (msec / 1000) + "." + (msec / 100 % 10) + " " + summ1;
	}

	// サマリ文字列作成(ミリ秒)
	static public String getMSecStr200(int val, String summ1) {
		int msec = calcMSec200(val);
		return (msec / 1000) + "." + (msec / 100 % 10) + " " + summ1;
	}

	// サマリ文字列作成(エフェクト時間)
	static public String getEffectTimeStr(int val, String summ1) {
		int msec = calcEffectTime(val);
		String m1 = "" + (msec / 1000);
		String m2 = "0" + (msec / 10 % 100);
		if (m2.length() >= 3) {
			m2 = m2.substring(1);
		}
		return m1 + "." + m2 + " " + summ1;
	}

	// サマリ文字列作成(自動再生間隔)
	static public String getAutoPlayStr(int val, String summ1) {
		int itv = calcAutoPlay(val);
		String m1 = "" + (itv / 1000);
		String m2 = "" + (itv / 100 % 10);
		return m1 + "." + m2 + " " + summ1;
	}

	// サマリ文字列作成(減速率)
	static public String getMomentModeStr(int val, String summ1, String summ2) {
		String str;

		if (val == 0) {
			str = summ1;
		} else if (val == MAX_MOMENTMODE) {
			str = summ2;
		} else {
			str = "" + val;
		}
		return str;
	}

	// サマリ文字列作成(100 : ?)
	static public String getAdjustStr(int val, String summ1, String summ2) {
		// 75～125
		int percent = calcWAdjust(val);
		String str;

		// if (percent != 100) {
		str = percent + " : 100" + " " + summ1;
		// }
		// else{
		// str = summ2;
		// }
		return str;
	}

	// サマリ文字列作成(-25%～+25%)
	static public String getWScalingStr(int val, String summ1, String summ2) {
		// -25 ～ +25
		int percent = calcWScaling(val) - 100;
		String str;

		if (percent < 0) {
			str = percent + " " + summ1;
		} else if (percent > 0) {
			str = "+" + percent + " " + summ1;
		} else {
			str = summ2;
		}
		return str;
	}

	// サマリ文字列作成(25%～+250%)
	static public String getScalingStr(int val, String summ1, String summ2) {
		int percent = calcScaling(val);
		String str;

		if (percent == 100) {
			str = summ2;
		} else {
			str = percent + " " + summ1;
		}
		return str;
	}

	// サマリ文字列作成(1 dot)
	static public String getCenterStr(int val, String summ1) {
		// 0.1%単位
		return val + " " + summ1;
	}

	// サマリ文字列作成(0%～)
	static public String getGradationStr(int val) {
		// 1%単位
		return val + " %";
	}

	// フォント用サマリ文字列作成(xx sp)
	static public String getFontSpStr(int val, String summ1) {
		// ドット単位
		return calcFont(val) + " " + summ1;
	}

	// 余白サマリ文字列作成(xx sp)
	static public String getMarginSpStr(int val, String summ1) {
		// ドット単位
		return val + " " + summ1;
	}

	// ツールバーサイズ文字列作成(xx sp)
	static public String getToolbarSeekStr(int val, String summ1) {
		// ドット単位
		return calcToolbarSize(val) + " " + summ1;
	}

	// リストサムネイルサイズ文字列作成(xx sp)
	static public String getListThumbSeekStr(int val, String summ1) {
		// ドット単位
		return "H: " + calcThumbnailSize(val) + " " + summ1;
	}

	// サムネイルサイズサマリ文字列作成(??sp x ??sp)
	static public String getThumbnailStr(int w, int h, String summ1) {
		// ドット単位
		return "W: " + calcThumbnailSize(w) + " " + summ1 + " x " + "H: " + calcThumbnailSize(h) + " " + summ1;
	}

	// キャッシュメモリサイズ
	static public String getMemSizeStr(int val, String summ1, String summ2) {
		// if (val == 0) {
		// return summ2;
		// }
		return calcMemSize(val) + " " + summ1;
	}

	// キャッシュページ数
	static public String getMemPageStr(int val, String summ1) {
		return calcMemPage(val) + " " + summ1;
	}

	// スクロールドット数
	static public String getScrlSpeedStr(int val, String summ1) {
		return calcScrlSpeed(val) + " " + summ1;
	}

	// スクロール量
	static public String getScrlRangeStr(int val, String summ1, String summ2) {
		return summ1 + ' ' + calcScrlRange(val) + " " + summ2;
	}

	// ページ番号表示サイズ
	static public String getPnumSizeStr(int val, String summ) {
		return calcPnumSize(val) + " " + summ;
	}

	// 音量レベル
	static public String getNoiseLevelStr(int val) {
		return "" + calcNoiseLevel(val);
	}

	// 2バイト数値取得
	static public short getShort(byte[] b, int pos) {
		int val;
		val = ((int) b[pos] & 0x000000FF) | (((int) b[pos + 1] << 8) & 0x0000FF00);

		return (short) val;
	}

	// 4バイト数値取得
	static public int getInt(byte[] b, int pos) {
		int val;
		val = ((int) b[pos] & 0x000000FF) | (((int) b[pos + 1] << 8) & 0x0000FF00) | (((int) b[pos + 2] << 16) & 0x00FF0000) | (((int) b[pos + 3] << 24) & 0xFF000000);

		return val;
	}

	static public String getFontDirectory() {
		return DEF.getBaseDirectory() + "font/";
	}

	static public String getConfigDirectory() {
		return DEF.getBaseDirectory() + "conf/";
	}

	// パス部分のみ取得
	static public String getDir(String filepath) {
		int prev = 0;

		if (filepath == null) {
			return "";
		}
		while (true) {
			int pos = filepath.indexOf('/', prev);
			if (pos == -1) {
				break;
			}
			prev = pos + 1;
		}
		return filepath.substring(0, prev);
	}

	// ファイル拡張子の取得
	static public String getFileExt(String filepath) {
		String ext = "";
		if (filepath != null) {
			int pos = filepath.lastIndexOf('.');

			if (pos >= 0) {
				// 拡張子切り出し
				ext = filepath.substring(pos).toLowerCase();
			}
		}
		return ext;
	}

	// 拡張子の取得
	public static String getExtension(String filename) {
		if (filename != null) {
			int extpos = filename.lastIndexOf('.');
			int slashpos = filename.lastIndexOf('/');
			if (slashpos < extpos && extpos >= 1) {
				return filename.substring(extpos).toLowerCase();
			}
		}
		return "";
	}

	// 番号とか考慮しながら文字列比較
	private static final int CHTYPE_NUM = 1;
	private static final int CHTYPE_CHAR = 2;
	private static final int CHTYPE_KANJI_NUMERALS = 3;
	private static final int CHTYPE_JAPANESE_VOLUME_NAME = 4;
	private static final int CHTYPE_SYMBOL_BEFORE = 5;
	private static final int CHTYPE_SYMBOL_AFTER = 6;

	public static boolean SORT_BY_IGNORE_WIDTH = true;
	public static boolean SORT_BY_IGNORE_CASE = true;
	public static boolean SORT_BY_SYMBOL = true;
	public static boolean SORT_BY_NATURAL_NUMBERS = true;
	public static boolean SORT_BY_KANJI_NUMERALS = true;
	public static boolean SORT_BY_JAPANESE_VOLUME_NAME = true;
	public static boolean SORT_BY_FILE_TYPE = true;
	public static String[] PRIORITY_WORDS = new String[0];

	static public int compareFileName(final String str1, final String str2) {
		return compareFileName(str1, str2, false);
	}

	static public int compareFileName(final String str1, final String str2, final boolean sortByFileType) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel,"開始します. str1=" + str1 + ", str2=" + str2 + ", sortByFileType=" + sortByFileType);

		String name1 = str1;
		String name2 = str2;
		int index1, index2;
		String dir1 = "", dir2 = "";
		String ext1 = "", ext2 = "";
		boolean flag1 = false, flag2 = false;
		boolean file1 = false, file2 = false;

		// 拡張子とそれ以外に分ける
		index1 = name1.lastIndexOf('.');
		if (index1 > name1.lastIndexOf('/')) {
			name1 = name1.substring(0, index1);
			if (index1 < name1.length() - 1) {
				ext1 = name1.substring(index1 + 1);
			}
			else {
				ext1 = "";
			}
		}
		index2 = name2.lastIndexOf('.');
		if (index2 > name2.lastIndexOf('/')) {
			name2 = name2.substring(0, index2);
			if (index2 < name2.length() - 1) {
				ext2 = name2.substring(index2 + 1);
			}
			else {
				ext2 = "";
			}
		}
		Logcat.d(logLevel,"ext1=" + ext1 + ", ext2=" + ext2 + ", sortByFileType=" + sortByFileType);
		Logcat.d(logLevel,"name1=" + name1 + ", name2=" + name2 + ", sortByFileType=" + sortByFileType);

		while (!flag1 && !flag2) {
			// 最上位ディレクトリとそれ以外に分ける
			index1 = name1.indexOf('/');
			if (index1 >= 0) {
				dir1 = name1.substring(0, index1);
				if (index1 < name1.length() - 1) {
					name1 = name1.substring(index1 + 1);
				}
				else {
					name1 = "";
				}
			} else {
				dir1 = name1;
				name1 = "";
				file1 = true;
				flag1 = true;
			}
			index2 = name2.indexOf('/');
			if (index2 >= 0) {
				dir2 = name2.substring(0, index2);
				if (index2 < name2.length() - 1) {
					name2 = name2.substring(index2 + 1);
				}
				else {
					name2 = "";
				}
			} else {
				dir2 = name2;
				name2 = "";
				file2 = true;
				flag2 = true;
			}

			Logcat.d(logLevel,"dir1=" + dir1 + ", dir2=" + dir2 + ", sortByFileType=" + sortByFileType);
			Logcat.d(logLevel,"name1=" + name1 + ", name2=" + name2 + ", sortByFileType=" + sortByFileType);
			// ファイル優先ならファイルをディレクトリより優先
			if (sortByFileType && file1 && !file2) {
				Logcat.d(logLevel,"dir1 はファイルです.");
				return -1;
			} else if (sortByFileType && !file1 && file2) {
				Logcat.d(logLevel,"dir2 はファイルです.");
				return 1;
			} else {
				// ディレクトリ同士を比較
				int returnCode = compareText(dir1, dir2);
				if (returnCode != 0) {
					return returnCode;
				}
			}
		}

		// ディレクトリ部分を削除したファイル名部分を比較
		int returnCode = compareText(name1, name2);
		if (returnCode != 0) {
			return returnCode;
		}

		// 最後まで結果が決まらなかった
		if (str1.compareTo(str2) != 0) {
			// 単純に大小比較してみる
			return str1.compareTo(str2);
		}
		else {
			// 完全一致なら拡張子を比較
			return compareText(ext1, ext2);
		}

	}

	static public int compareText(final String str1, final String str2) {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		String name1 = str1;
		String name2 = str2;

		if (name1 == null && name2 == null) {
			return 0;
		}
		if (name1 == null) {
			return 1;
		}
		if (name2 == null) {
			return -1;
		}

		Logcat.d(logLevel,"開始します. name1=" + name1 + ", name2=" + name2);

		int i1, i2;
		char ch1, ch2;
		int ct1, ct2;

		if (SORT_BY_IGNORE_WIDTH) {
			// 全角を半角に変換
			name1 = Normalizer.normalize(name1, Normalizer.Form.NFKC);
			name2 = Normalizer.normalize(name2, Normalizer.Form.NFKC);
		}

		if (SORT_BY_IGNORE_CASE) {
			// 小文字を大文字に変換
			name1 = name1.toUpperCase();
			name2 = name2.toUpperCase();
		}

		int len1 = name1.length();
		int len2 = name2.length();

		for (i1 = i2 = 0; i1 < len1 && i2 < len2; i1++, i2++) {
			Logcat.d(logLevel,"ループを実行します. i1=" + i1 + ", i2=" + i2 + ", name1=" + name1 + ", name2=" + name2);
			ch1 = name1.charAt(i1);
			ch2 = name2.charAt(i2);
			ct1 = getCharType(ch1);
			ct2 = getCharType(ch2);

			if (PRIORITY_WORDS != null && PRIORITY_WORDS.length > 0) {
				// 優先単語があれば優先単語を使用している方が先
				int wlen1 = 0;
				int wlen2 = 0;

				for (int i = 0; i < PRIORITY_WORDS.length; ++i) {
					String word;
					if (PRIORITY_WORDS[i] != null && !PRIORITY_WORDS[i].isEmpty()) {
						if (SORT_BY_IGNORE_CASE) {
							word = PRIORITY_WORDS[i].toUpperCase();
						} else {
							word = PRIORITY_WORDS[i];
						}

						if (name1.startsWith(word, i1)) {
							wlen1 = word.length();
						}
						if (name2.startsWith(word, i2)) {
							wlen2 = word.length();
						}
					}
				}
				if (wlen1 > 0 && wlen2 <= 0) {
					return -1;
				}
				if (wlen1 <= 0 && wlen2 > 0) {
					return 1;
				}
				if (wlen1 > 0 && wlen2 > 0) {
					i1 += wlen1 - 1;
					i2 += wlen2 - 1;
				}
			}

			if (SORT_BY_SYMBOL) {
				if (ct1 == CHTYPE_SYMBOL_BEFORE && ct2 != CHTYPE_SYMBOL_BEFORE) {
					// 1が記号で2が記号以外なら、1が先
					return -1;
				} else if (ct1 == CHTYPE_SYMBOL_BEFORE && ct2 == CHTYPE_SYMBOL_BEFORE) {
					// 1も2も記号なら、記号を比べる
					int s1 = getSymbolBefore(ch1);
					int s2 = getSymbolBefore(ch2);
					if (s1 != s2) {
						return s1 - s2;
					}
					else {
						continue;
					}
				} else if (ct1 != CHTYPE_SYMBOL_BEFORE && ct2 == CHTYPE_SYMBOL_BEFORE) {
					// 2が記号で1が記号以外なら、2が先
					return 1;
				}

				else if (ct1 == CHTYPE_SYMBOL_AFTER && ct2 != CHTYPE_SYMBOL_AFTER) {
					// 1が記号で2が記号以外なら、2が先
					return 1;
				}
				else if (ct1 == CHTYPE_SYMBOL_AFTER && ct2 == CHTYPE_SYMBOL_AFTER) {
					// 1も2も記号なら、記号を比べる
					int s1 = getSymbolAfter(ch1);
					int s2 = getSymbolAfter(ch2);
					if (s1 != s2) {
						return s1 - s2;
					}
					else {
						continue;
					}
				}
				else if (ct1 != CHTYPE_SYMBOL_AFTER && ct2 == CHTYPE_SYMBOL_AFTER) {
					// 2が記号で1が記号以外なら、1が先
					return -1;
				}
			}

			if (ct1 != ct2) {
				Logcat.d(logLevel,"文字種が違います. ch1=" + ch1 + ", ch2=" + ch2);
				// 文字種が違う場合
				char tmp1, tmp2;
				if (ct1 == CHTYPE_KANJI_NUMERALS) {
					tmp1 = '一';
				}
				else if (ct1 == CHTYPE_JAPANESE_VOLUME_NAME) {
					tmp1 = '上';
				}
				else {
					tmp1 = ch1;
				}

				if (ct2 == CHTYPE_KANJI_NUMERALS) {
					tmp2 = '一';
				}
				else if (ct2 == CHTYPE_JAPANESE_VOLUME_NAME) {
					tmp2 = '上';
				}
				else {
					tmp2 = ch2;
				}

				if (tmp1 != tmp2) {
					// 元の値を比較
					return tmp1 - tmp2;
				}
				else {
					continue;
				}
			}

			if (SORT_BY_NATURAL_NUMBERS) {
				if (ct1 == CHTYPE_NUM) {
					//Logcat.d(logLevel, "文字1=" + ch1 + ", 文字2=" + ch2);
					String num1 = getNumbers(name1, i1);
					String num2 = getNumbers(name2, i2);
					int nlen1 = num1.length();
					int nlen2 = num2.length();

					// カンマを取り除く
					num1 = num1.replace(",", "");
					num2 = num2.replace(",", "");

					// マイナス判定を残しているが、マイナス記号は数字に含めるのをやめた
					// ファイル名ハイフン001がマイナス001になるとおかしくなるから
					boolean minus1 = num1.startsWith("-");
					boolean minus2 = num2.startsWith("-");

					if (minus1 && !minus2) {
						// num2が大きい
						return -1;
					} else if (!minus1 && minus2) {
						// num1が大きい
						return 1;
					} else {
						//Logcat.d(logLevel, "数字1=" + num1 + ", 数字2=" + num2);
						//小数点の位置
						int index_dot1 = num1.indexOf(".");
						int index_dot2 = num2.indexOf(".");

						//小数以下の桁数
						int col_dec1;
						int col_dec2;
						if (index_dot1 == -1) {
							col_dec1 = 0;
						} else {
							col_dec1 = nlen1 - index_dot1 - 1;
						}
						if (index_dot2 == -1) {
							col_dec2 = 0;
						} else {
							col_dec2 = nlen2 - index_dot2 - 1;
						}

						// 小数点以下の桁数を合わせる
						int col_diff = col_dec1 - col_dec2;
						for (int i = 1; i <= col_diff; i++) {
							num2 = num2 + "0";
						}
						for (int i = -1; i >= col_diff; i--) {
							num1 = num1 + "0";
						}
						//Logcat.d(logLevel, "数字1=" + num1 + ", 数字2=" + num2 + ", 小数点位置1=" + index_dot1 + ", 小数点位置2=" + index_dot2 + ", 小数桁1=" + col_dec1 + ", 小数桁2=" + col_dec2);
						num1 = num1.replace(".", "");
						num2 = num2.replace(".", "");
						//Logcat.d(logLevel, "数字1=" + num1 + ", 数字2=" + num2 + ", 小数点位置1=" + index_dot1 + ", 小数点位置2=" + index_dot2 + ", 小数桁1=" + col_dec1 + ", 小数桁2=" + col_dec2);

						int num_len1 = num1.length();
						int num_len2 = num2.length();

						if (!minus1 && !minus2) {
							// どちらも正の数

							if (num_len1 < num_len2) {
								int difflen = num_len2 - num_len1;
								for (int i = 0; i < difflen; i++) {
									int diff = getNumber('0') - getNumber(num2.charAt(i));
									if (diff != 0) {
										// num1が大きければプラス
										return diff;
									}
								}
								// 残り部分で比較
								num2 = num2.substring(difflen);
							} else if (num_len1 > num_len2) {
								int difflen = num_len1 - num_len2;
								for (int i = 0; i < difflen; i++) {
									int diff = getNumber(num1.charAt(i)) - getNumber('0');
									if (diff != 0) {
										// num1が大きければプラス
										return diff;
									}
								}
								// 残り部分で比較
								num1 = num1.substring(difflen);
							}
							// 数字が異なる場合は比較
							if (num1.length() == num2.length()) {
								for (int i = 0; i < num1.length(); i++) {
									int diff = getNumber(num1.charAt(i)) - getNumber(num2.charAt(i));
									if (diff != 0) {
										// num1が大きければプラス
										return diff;
									}
								}
							} else {
								Logcat.d(logLevel, "長さが違います。 num1=" + num1 + ", num2=" + num2);
							}
						} else {
							// どちらも負の数

							// マイナスを取り除く
							num1 = num1.replace("-", "");
							num2 = num2.replace("-", "");

							if (num_len1 < num_len2) {
								int difflen = num_len2 - num_len1;
								for (int i = 0; i < difflen; i++) {
									int diff = getNumber('0') - getNumber(num2.charAt(i));
									if (diff != 0) {
										// num1が大きければマイナス
										return -diff;
									}
								}
								// 残り部分で比較
								num2 = num2.substring(difflen);
							} else if (num_len1 > num_len2) {
								int difflen = num_len1 - num_len2;
								for (int i = 0; i < difflen; i++) {
									int diff = getNumber(num1.charAt(i)) - getNumber('0');
									if (diff != 0) {
										// num1が大きければマイナス
										return -diff;
									}
								}
								// 残り部分で比較
								num1 = num1.substring(difflen);
							}
							// 数字が異なる場合は比較
							if (num1.length() == num2.length()) {
								for (int i = 0; i < num1.length(); i++) {
									int diff = getNumber(num1.charAt(i)) - getNumber(num2.charAt(i));
									if (diff != 0) {
										// num1が大きければマイナス
										return -diff;
									}
								}
							} else {
								Logcat.d(logLevel, "長さが違います。 num1=" + num1 + ", num2=" + num2);
							}
						}
						i1 += nlen1 - 1;
						i2 += nlen2 - 1;
					}
					continue;
				}
			}

			if (SORT_BY_KANJI_NUMERALS) {
				if (ct1 == CHTYPE_KANJI_NUMERALS) {
					Logcat.d(logLevel, "漢数字を比較します. ch1=" + ch1 + ", ch2=" + ch2);
					String num1 = getKanjiNumerals(name1, i1);
					String num2 = getKanjiNumerals(name2, i2);
					int nlen1 = num1.length();
					int nlen2 = num2.length();
					Logcat.d(logLevel, "漢数字を比較します. num1=" + num1 + ", num2=" + num2);
					if (nlen1 < nlen2) {
						int difflen = nlen2 - nlen1;
						for (int i = 0; i < difflen; i++) {
							if (getKanjiNumeral(num2.charAt(i)) != 0) {
								// num2の方が大きい
								Logcat.d(logLevel, "漢数字を比較します. num1が小さいです.");
								return -1;
							}
						}
						// 残り部分で比較
						num2 = num2.substring(difflen);
					} else if (nlen1 > nlen2) {
						int difflen = nlen1 - nlen2;
						for (int i = 0; i < difflen; i++) {
							if (getKanjiNumeral(num1.charAt(i)) > 0) {
								// num1の方が大きい
								Logcat.d(logLevel, "漢数字を比較します. num2が小さいです.");
								return 1;
							}
						}
						// 残り部分で比較
						num1 = num1.substring(difflen);
					}
					// 数字が異なる場合は比較
					for (int i = 0; i < num1.length(); i++) {
						int diff = getKanjiNumeral(num1.charAt(i)) - getKanjiNumeral(num2.charAt(i));
						if (diff != 0) {
							// num1の方が大きい
							if (diff>0) {Logcat.d(logLevel, "漢数字を比較します. num2が小さいです.");}
							else {Logcat.d(logLevel, "漢数字を比較します. num1が小さいです.");}
							return diff;
						}
					}
					i1 += nlen1 - 1;
					i2 += nlen2 - 1;
					continue;
				}
			}

			if (SORT_BY_JAPANESE_VOLUME_NAME) {
				if (ct1 == CHTYPE_JAPANESE_VOLUME_NAME) {
					int s1 = getJapaneseVolumeName(ch1);
					int s2 = getJapaneseVolumeName(ch2);
					if (s1 != s2) {
						return s1 - s2;
					}
					continue;
				}
			}

			if (ch1 != ch2) {
				// 両方とも特殊な文字ではない
				// 元の値を比較
				return ch1 - ch2;
			}
		}

		// 最後まで結果が決まらなかった
		if (str1.compareTo(str2) != 0) {
			// 単純に大小比較してみる
			return str1.compareTo(str2);
		}

		return 0;
	}

	static private String getNumbers(String str, int idx) {
		int i;
		for (i = idx; i < str.length(); i++) {
			int ch = str.charAt(i);
//			if ((ch < '0' || '9' < ch) && '-' != ch && '.' != ch && ',' != ch) {
			if ((ch < '0' || '9' < ch) && '.' != ch && ',' != ch) {
				break;
			}
		}
		return str.substring(idx, i);
	}

	static private int getNumber(char ch) {
		switch (ch) {
//			case '-':
//				return -1;
			case '0':
				return 0;
			case '1':
				return 1;
			case '2':
				return 2;
			case '3':
				return 3;
			case '4':
				return 4;
			case '5':
				return 5;
			case '6':
				return 6;
			case '7':
				return 7;
			case '8':
				return 8;
			case '9':
				return 9;
			case '.':
				return 10;
			case ',':
				return 11;
		}
		return -2;
	}

	static private int getSymbolBefore(char ch) {
		switch (ch) {
			case ' ':		// 半角スペース
				return 0;
			case '　':		// 全角スペース
				return 1;
			case '!':
				return 2;
			case '！':
				return 3;
			case '#':
				return 4;
			case '＃':
				return 5;
			case '$':
				return 6;
			case '＄':
				return 7;
			case '%':
				return 8;
			case '％':
				return 9;
			case '&':
				return 10;
			case '＆':
				return 11;
			case '(':
				return 12;
			case '（':
				return 13;
			case ')':
				return 14;
			case '）':
				return 15;
			case '*':
				return 16;
			case ',':
				return 17;
			case '，':
				return 18;
			case '.':
				return 19;
			case '．':
				return 20;
			case '＊':
				return 21;
			case '\'':
				return 22;
			case '-':
				return 23;
			case ':':
				return 24;
			case '：':
				return 25;
			case ';':
				return 26;
			case '；':
				return 27;
			case '?':
				return 28;
			case '？':
				return 29;
			case '@':
				return 30;
			case '＠':
				return 31;
			case '[':
				return 32;
			case '［':
				return 33;
			case ']':
				return 34;
			case '］':
				return 35;
			case '^':
				return 36;
			case '＾':
				return 37;
			case '_':
				return 38;
			case '＿':
				return 39;
			case '{':
				return 40;
			case '｛':
				return 41;
			case '|':
				return 42;
			case '｜':
				return 43;
			case '}':
				return 44;
			case '｝':
				return 45;
			case '‘':
				return 46;
			case '’':
				return 47;
			case '"':
				return 48;
			case '“':
				return 49;
			case '”':
				return 50;
			case '′':
				return 51;
			case '\\':
				return 52;
			case '￥':
				return 53;
			case '「':
				return 54;
			case '」':
				return 55;
			case '『':
				return 56;
			case '』':
				return 57;
			case '【':
				return 58;
			case '】':
				return 59;
			case '+':
				return 60;
			case '＋':
				return 61;
			case '<':
				return 62;
			case '＜':
				return 63;
			case '=':
				return 64;
			case '＝':
				return 65;
			case '>':
				return 66;
			case '＞':
				return 67;
			case '×':
				return 68;
			case '∩':
				return 69;
			case '∪':
				return 70;
			case '≡':
				return 71;
			case '⊂':
				return 72;
			case '⊃':
				return 73;
			case '■':
				return 74;
			case '□':
				return 75;
			case '▲':
				return 76;
			case '△':
				return 77;
			case '▼':
				return 78;
			case '▽':
				return 79;
			case '◆':
				return 80;
			case '◇':
				return 81;
			case '○':
				return 82;
			case '◎':
				return 83;
			case '●':
				return 84;
			case '↑':
				return 85;
			case '→':
				return 86;
			case '←':
				return 87;
			case '↓':
				return 88;
			case '・':
				return 89;
			case '☆':
				return 90;
			case '★':
				return 91;
			case '※':
				return 92;
		}
		return -2;
	}

	static private int getSymbolAfter(char ch) {
		switch (ch) {
			case 'ー':		// 長音
				return 0;
			case '―':		// ダッシュ
				return 1;
		}
		return -2;
	}

	static private String getKanjiNumerals(String str, int idx) {
		int i;
		for (i = idx; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (getCharType(ch) != CHTYPE_KANJI_NUMERALS) {
				break;
			}
		}
		return str.substring(idx, i);
	}

	static private int getJapaneseVolumeName(char ch) {
		switch (ch) {
			case '上':
				return 0;
			case '前':
				return 1;
			case '中':
				return 2;
			case '後':
				return 3;
			case '下':
				return 4;
			case '完':
				return 5;
			case '短':
				return 6;
			case '外':
				return 7;
		}
		return -1;
	}

	static private int getKanjiNumeral(char ch) {
		switch (ch) {
			case '〇':
				return 0;
			case '零':
				return 1;
			case '一':
				return 2;
			case '壱':
				return 3;
			case '二':
				return 4;
			case '弐':
				return 5;
			case '三':
				return 6;
			case '参':
				return 7;
			case '四':
				return 8;
			case '肆':
				return 9;
			case '五':
				return 10;
			case '伍':
				return 11;
			case '六':
				return 12;
			case '陸':
				return 13;
			case '七':
				return 14;
			case '漆':
				return 15;
			case '八':
				return 16;
			case '捌':
				return 17;
			case '九':
				return 18;
			case '玖':
				return 19;
			case '十':
				return 20;
			case '拾':
				return 21;
			case '什':
				return 22;
			case '廿':
				return 23;
			case '卅':
				return 24;
			case '丗':
				return 25;
			case '百':
				return 26;
			case '佰':
				return 27;
			case '千':
				return 28;
			case '仟':
				return 29;
			case '阡':
				return 30;
			case '万':
				return 31;
			case '萬':
				return 32;
			case '億':
				return 33;
			case '兆':
				return 34;
		}
		return -1;
	}

	static private int getCharType(char ch) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		boolean debug = false;
		if (SORT_BY_SYMBOL && getSymbolBefore(ch) >= 0) {
			Logcat.d(logLevel, "TYPE=CHTYPE_SYMBOL_BEFORE");
			return CHTYPE_SYMBOL_BEFORE;
		}

		if (SORT_BY_SYMBOL && getSymbolAfter(ch) >= 0) {
			Logcat.d(logLevel, "TYPE=CHTYPE_SYMBOL_AFTER");
			return CHTYPE_SYMBOL_AFTER;
		}

		if (SORT_BY_NATURAL_NUMBERS && (('0' <= ch && ch <= '9') || '.' == ch || ',' == ch)) {
			Logcat.d(logLevel, "TYPE=CHTYPE_NUM");
			return CHTYPE_NUM;
		}

		if (SORT_BY_JAPANESE_VOLUME_NAME && getJapaneseVolumeName(ch) >= 0) {
			Logcat.d(logLevel, "TYPE=CHTYPE_JAPANESE_VOLUME_NAME");
			return CHTYPE_JAPANESE_VOLUME_NAME;
		}

		if (SORT_BY_KANJI_NUMERALS && getKanjiNumeral(ch) >= 0) {
			Logcat.d(logLevel, "TYPE=CHTYPE_KANJI_NUMERALS");
			return CHTYPE_KANJI_NUMERALS;
		}

		Logcat.d(logLevel, "TYPE=CHTYPE_CHAR");
		return CHTYPE_CHAR;
	}

	// 縮小取り込みの倍率計算
	static public int calcScale(int w, int h, int type, int lw, int lh) {
		int lw2 = DEF.checkPortrait(w, h) ? lw : lw * 2;
		if (type == FileData.EXTTYPE_JPG) {
			// JPEGではライブラリで1/2^xに縮小できる
			if (w >= lw2 * 8 && h >= lh * 8) {
				return 8;
			} else if (w >= lw2 * 4 && h >= lh * 4) {
				return 4;
			} else if (w >= lw2 * 2 && h >= lh * 2) {
				return 2;
			}
		} else if (type != FileData.EXTTYPE_PNG && type != FileData.EXTTYPE_GIF) {
			// pngとgif他以外
			if (w >= lw2 * 2 && h >= lh * 2) {
				int w_scale = w / lw2;
				int h_scale = h / lh;
				return w_scale < h_scale ? w_scale : h_scale;
			}
		}
		return 1;
	}

	static public int divRoundUp(int val, int div) {
		return val / div + (val % div != 0 ? 1 : 0);
	}

	// Comittoの諸々保存先のパスを返す
	static public String getBaseDirectory() {
		return Environment.getExternalStorageDirectory() + "/comittona/";
	}

	// 設定画面のメッセージのフォントサイズ
	static public int getMessageTextSize(float density) {
		return (int) (TEXTSIZE_MESSAGE * density);
	}

	// 設定画面のメッセージのフォントサイズ
	static public int getSummaryTextSize(float density) {
		return (int) (TEXTSIZE_SUMMARY * density);
	}

	static public boolean checkExportKey(String key) {
		// サーバー情報としおり情報をExport/Import対象に含める
		/*
		if (key.indexOf('/') >= 0) {
			return false;
		}
		else if (key.startsWith("smb-")) {
			return false;
		}
		*/
		if (key.indexOf('/') >= 0) {
			// 安全のため、しおり削除の条件に一致しないKeyは対象外としておく
			int len = key.length();
			if (len >= 1 && key.startsWith("/")) {
			} else if (len >= 6 && key.startsWith("smb://")) {
			} else {
				return false;
			}
		} else if (key.equals("path")) {
			return false;
		} else if (key.equals("ImportSetting")) {
			return false;
		}
		return true;
	}

	// NxT専用キーの判別
	static public boolean checkTonlyExportKey(String key) {
		if (key.indexOf('/') >= 0) {
			// 安全のため、しおり削除の条件に一致しないKeyは対象外としておく
			int len = key.length();
			if (len >= 1 && key.startsWith("/")) {
				return true;
			} else if (len >= 6 && key.startsWith("smb://")) {
				return true;
			}
		} else if (key.startsWith("smb-")) {
			return true;
		}
		return false;
	}

	// 定義からフラグ読み込み
	static public boolean getBoolean(SharedPreferences sp, String key, boolean defval) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		boolean intval = defval;
		try {
			// 読み込み
			intval = sp.getBoolean(key, defval);
		} catch (Exception e) {
			Logcat.e(logLevel, "error(key=" + key + "):", e);
		}
		return intval;
	}

	// 定義から数値読み込み
	static public int getInt(SharedPreferences sp, String key, int defval) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		int intval = defval;
		try {
			// 読み込み
			intval = sp.getInt(key, defval);
		} catch (Exception e) {
			Logcat.e(logLevel, "getInt: error(key=" + key + "): ", e);
		}
		return intval;
	}

	// 定義から文字列読み込み
	static public int getInt(SharedPreferences sp, String key, String defval) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		String strval = defval;
		int intval = 0;
		try {
			strval = sp.getString(key, defval);
		} catch (Exception e) {
			Logcat.e(logLevel, "error(key=" + key + "): ", e);
		}

		try {
			// 読み込み
			intval = Integer.parseInt(strval);
		} catch (Exception e) {
			Logcat.e(logLevel, "parseInt error(key=" + key + ", str=" + strval + "): ", e);
		}
		return intval;
	}

	public static void StackTrace(String tag, String msg) {
		StackTraceElement[] ste = new Throwable().getStackTrace();
		for (int i = 1; i < ste.length; i++) {
			Log.d(tag, msg + ste[i]);
		}
	}

	public static boolean isUiThread() {
		return Thread.currentThread().equals(Looper.getMainLooper().getThread());
	}

	// Url文字列作成
	public static String createUrl(String url, String user, String pass) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (url == null) {
			return "";
		}
		if (url.length() <= 6) {
			return url;
		}
		if (!url.startsWith("smb://") || user == null || user.isEmpty()) {
			return url;
		}
		// サーバ名
		String ret = "";
		try {
			ret = "smb://" + URLEncoder.encode(user, "UTF-8");
			if (pass != null && !pass.isEmpty()) {
				ret += ":" + URLEncoder.encode(pass, "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			Logcat.e(logLevel, "", e);
		}
		ret += "@" + url.substring(6);
		return ret;
	}


	public static boolean CHAR_DETECT = true;
	public static String CHARSET = "Shift_JIS";


	/**
	 * ファイルにBOMが含まれていた場合、
	 * BOMなしに変換する。
	 *
	 * @param s ファイル文字列
	 * @return BOMなしのファイル文字列
	 *
	 */
	public static String removeUTF8BOM(String s) {
		// BOMをUnicodeコード表示したもの
		final String BOM = "\uFEFF";
		if (s.startsWith(BOM)) {
			// ファイルの先頭より後ろの文字列を読み込む
			s = s.substring(1);
		}
		return s;
	}

	public static String toUTF8(byte[] bytes, int offset, int length) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		String encoding = "UTF-8";
		String dst;

		int tmp_offset = offset;
		int tmp_length = length;

		// UTF-8のBOMがあったら削除する
		if( bytes[offset] == (byte)0xFE && bytes[offset+1] == (byte)0xFF ){
			tmp_offset = offset + 1;
			tmp_length = length - 1;
		}

		Logcat.d(logLevel, "文字コードを自動判定します.");
		encoding = CharDetecter(bytes, tmp_offset, tmp_length);

		dst = new String(bytes, tmp_offset, tmp_length, Charset.forName(encoding));
		Logcat.d(logLevel, "Stringを出力します. dst=" + dst);
		return dst;
	}

	public static String CharDetecter(byte[] bytes, int offset, int length) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "文字コードを判定します.");

		String encoding = null;
		int tmp_offset = offset;
		int tmp_length = length;

		// UTF-8のBOMがあったら削除する
		if( bytes[offset] == (byte)0xFE && bytes[offset+1] == (byte)0xFF ){
			tmp_offset = offset + 1;
			tmp_length = length - 1;
		}

		if (CHAR_DETECT) {
			// 文字コード判定ライブラリの実装
			UniversalDetector detector = new UniversalDetector(null);
			// 判定開始
			detector.handleData(bytes, tmp_offset, tmp_length);
			// 判定終了
			detector.dataEnd();
			// 文字コード判定
			encoding = detector.getDetectedCharset();
			// 判定の初期化
			detector.reset();

			if (encoding != null) {
				if("WINDOWS-1252".equals(encoding)) {
					// 判定された文字コードがWindows-1252の場合は誤判定ではないか確認する
					Logcat.d(logLevel, "判定結果が WINDOWS-1252 なので誤判定かどうか確認します.");
					byte[] src = Arrays.copyOfRange(bytes, tmp_offset, tmp_offset + tmp_length);
					String charset = (CHARSET.equals("Shift_JIS") ? "MS932" : CHARSET);
					if (Arrays.equals(src, new String(src, Charset.forName(charset)).getBytes(Charset.forName(charset)))) {
						Logcat.d(logLevel, "文字コードは " + charset + " です.");
						encoding = charset;
						return encoding;
					} else {
						Logcat.d(logLevel, "文字コードは " + charset + " ではありません.");
					}
					if (Arrays.equals(src, new String(src, Charset.forName("MS932")).getBytes(Charset.forName("MS932")))) {
						Logcat.d(logLevel, "文字コードは MS932 です.");
						encoding = "MS932";
					} else {
						Logcat.d(logLevel, "文字コードは MS932 ではありません.");
					}
				}
				// 判定された文字コードがShift_JISの場合は、MS932としてデータを読み込む
				if("Shift_JIS".equals(encoding)) {
					encoding = "MS932";
				}
				Logcat.d(logLevel, "文字コードを判定しました. encoding=" + encoding);
			} else {
				Logcat.d(logLevel, "文字コードを判定できませんでした.");
				byte[] src = Arrays.copyOfRange(bytes, tmp_offset, tmp_offset + tmp_length);

				Logcat.d(logLevel, "文字コードを自動判定できませんでした.");
				// 文字コード判定に失敗したので中国国家標準規格かどうか再度確認する
				Logcat.d(logLevel, "文字コードが GB18030 かどうか判定します.");
				if (Arrays.equals(src, new String(src, Charset.forName("GB18030")).getBytes(Charset.forName("GB18030")))) {
					Logcat.d(logLevel, "文字コードは GB18030 です.");
					encoding = "GB18030";
				} else {
					Logcat.d(logLevel, "文字コードは GB18030 ではありません.");
				}
			}
		}

		if (encoding == null) {
			// 判定できなかったら共通の操作設定設定で設定した文字コードに設定する
			String charset = (CHARSET.equals("Shift_JIS") ? "MS932" : CHARSET);
			Logcat.d(logLevel, "文字コードを設定画面で設定した " + charset + " に設定します.");
			encoding = charset;
		}
		return encoding;
	}

	// 相対パスを絶対パスに変換
	public static String relativePath(Context context, String... path) {
		if (path.length < 2) {
			return path[0];
		}
		String result = path[path.length - 1];
		for (int i = path.length - 1; i > 0; --i) {
			result = FileAccess.relativePath(context, path[i - 1], result);
		}
		return result;
	}


	/** ハンドラにトーストメッセージを送る */
	public static void sendMessage(Context context, @StringRes int resId, int duration, Handler handler) {
		sendMessage(context.getString(resId), duration, handler);
	}

	/** ハンドラにトーストメッセージを送る */
	public static void sendMessage(String string, int duration, Handler handler) {
		if (handler != null) {
			Message message = new Message();
			message.what = HMSG_TOAST;
			message.obj = string;
			message.arg1 = duration;
			handler.sendMessage(message);
		}
	}

	/** ハンドラにメッセージを送る */
	public static void sendMessage(Handler handler, int what, int arg1, int arg2, Object obj) {
		if (handler != null) {
			Message message = new Message();
			message.what = what;
			message.arg1 = arg1;
			message.arg2 = arg2;
			message.obj = obj;
			handler.sendMessage(message);
		}
	}

	/** ハンドラから受け取ったトーストメッセージを実行する */
	public static boolean ToastMessage(Context context, Message msg) {
		switch (msg.what) {
			case HMSG_TOAST:
				Toast.makeText(context, (String) msg.obj, msg.arg1).show();
				return true;
		}
		return false;
	}

	/** プログレスダイアログに表示するメッセージを作成する */
	public static String ProgressMessage(String message, String message2, String workMessage) {
		if (message2.isEmpty()) {
			if (workMessage.isEmpty()) {
				return message;
			}
			else {
				return message + " : " + workMessage;
			}
		}
		else {
			if (workMessage.isEmpty()) {
				return message + "\n" + message2;
			}
			else {
				return message + "\n" + message2 + " : " + workMessage;
			}
		}
	}

	public static String getMemoryString(Activity activity) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "開始します.");

		int memoryClass = ((ActivityManager) activity.getSystemService(activity.ACTIVITY_SERVICE)).getMemoryClass();
		int largeMemoryClass = 0;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			largeMemoryClass = ((ActivityManager) activity.getSystemService(activity.ACTIVITY_SERVICE)).getLargeMemoryClass();
		}

		// メモリ情報を取得
		ActivityManager activityManager = (ActivityManager)activity.getSystemService(activity.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);

		int avaliMem = (int) (memoryInfo.availMem / 1024 / 1024);
		int threshold = (int) (memoryInfo.threshold / 1024 / 1024);
		boolean lowMemory = memoryInfo.lowMemory;

		int nativeAllocate = (int) (Debug.getNativeHeapAllocatedSize() / 1024 / 1024);
		int dalvikTotal = (int) (Runtime.getRuntime().totalMemory() / 1024 / 1024);
		int dalvikFree = (int) (Runtime.getRuntime().freeMemory() / 1024 / 1024);

		int javaAllocate = dalvikTotal - dalvikFree;
		int totalAllocate = nativeAllocate + javaAllocate;

		int ratio = (int)((double) totalAllocate / memoryClass * 100);
		int largeRatio = (int)((double) totalAllocate / largeMemoryClass * 100);

		return Build.BRAND + " " + Build.MODEL + " Android " + Build.VERSION.RELEASE + "\n"
				+ "使用可能メモリ = " + String.valueOf(memoryClass) + " MB\n"
				+ "使用可能メモリ(large) = " + largeMemoryClass + " MB\n"
				+ "native割当済み = " + nativeAllocate + " MB\n"
				+ "java割当済み = " + javaAllocate + " MB\n"
				+ "total割当済み = " + totalAllocate + " MB\n"
				+ "使用率 = " + ratio + "%\n"
				+ "使用率(large) = " + largeRatio + "%\n"
				+ "(dalvik最大メモリ = " + dalvikTotal + " MB)\n"
				+ "(dalvik空きメモリ = " + dalvikFree + " MB)\n"
				+ "availMem = " + avaliMem + " MB\n"
				+ "threshold = " + threshold + " MB\n"
				+ "lowMemory = " + lowMemory;
	}

}