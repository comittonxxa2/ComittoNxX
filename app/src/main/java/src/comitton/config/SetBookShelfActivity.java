package src.comitton.config;

import src.comitton.config.seekbar.BookShelfBrightLevelSeekbar;
import src.comitton.config.seekbar.BookShelfEdgeLevelSeekbar;
import src.comitton.config.seekbar.FilenameBottomSeekbar;
import src.comitton.config.seekbar.ThumbnailBottomSeekbar;
import src.comitton.config.seekbar.ThumbnailTopSeekbar;
import src.comitton.helpview.HelpActivity;
import src.comitton.common.DEF;
import src.comitton.config.SetCommonActivity;
import jp.dip.muracoro.comittonx.R;
import src.comitton.common.Logcat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.WindowManager;

import androidx.preference.PreferenceManager;

public class SetBookShelfActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private BookShelfSelectPreference mBookShelfPattern;
	private ThumbnailTopSeekbar mThumbnailTopSpace;
	private ThumbnailBottomSeekbar mThumbnailBottomSpace;
	private FilenameBottomSeekbar mFilenameBottomSpace;
	private BookShelfBrightLevelSeekbar mBookShelfBrightLevel;
	private BookShelfEdgeLevelSeekbar mBookShelfEdgeLevel;

	private static final int[] SetBookShelfPatternName =
		{ R.string.BookShelf00
		, R.string.BookShelf01
		, R.string.BookShelf02
		, R.string.BookShelf03
		, R.string.BookShelf04
		, R.string.BookShelf05
		, R.string.BookShelf06
		, R.string.BookShelf07
		, R.string.BookShelf08
		, R.string.BookShelfcustom1
		, R.string.BookShelfcustom2
		, R.string.BookShelfcustom3
		, R.string.BookShelfcustom4
		, R.string.BookShelfcustom5
		, R.string.BookShelfcustom6
		, R.string.BookShelfcustom7
		, R.string.BookShelfcustom8 };

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;

	static Resources mResources;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		mNotice = SetCommonActivity.getForceHideStatusBar(sharedPreferences);
		if (mNotice) {
			// 通知領域非表示
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		mImmEnable = SetCommonActivity.getForceHideNavigationBar(sharedPreferences);
		if (mImmEnable && mSdkVersion >= 19) {
			int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
				uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
				uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
				getWindow().getDecorView().setSystemUiVisibility(uiOptions);
		}
		SetCommonActivity.SetOrientationEventListener(this, sharedPreferences);

		addPreferencesFromResource(R.xml.bookshelf);
		mResources = getResources();

		mBookShelfPattern = (BookShelfSelectPreference)getPreferenceScreen().findPreference(DEF.KEY_BOOKSHELFPATTERN);
		mThumbnailTopSpace = (ThumbnailTopSeekbar)getPreferenceScreen().findPreference(DEF.KEY_THUMBNAILTOPSPACE);
		mThumbnailBottomSpace = (ThumbnailBottomSeekbar)getPreferenceScreen().findPreference(DEF.KEY_THUMBNAILBOTTOMSPACE);
		mFilenameBottomSpace = (FilenameBottomSeekbar)getPreferenceScreen().findPreference(DEF.KEY_FILENAMEBOTTOMSPACE);
		mBookShelfBrightLevel = (BookShelfBrightLevelSeekbar)getPreferenceScreen().findPreference(DEF.KEY_BOOKSHELFBRIGHTLEVEL);
		mBookShelfEdgeLevel = (BookShelfEdgeLevelSeekbar)getPreferenceScreen().findPreference(DEF.KEY_BOOKSHELFEDGELEVEL);
	}

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mBookShelfPattern.setSummary(getBookShelfPatternSummary(mResources, sharedPreferences));
		mThumbnailTopSpace.setSummary(getmThumbnailTopSpaceSummary(sharedPreferences));
		mThumbnailBottomSpace.setSummary(getmThumbnailBottomSpaceSummary(sharedPreferences));
		mFilenameBottomSpace.setSummary(getmFilenameBottomSpaceSummary(sharedPreferences));
		mBookShelfBrightLevel.setSummary(getmBookShelfBrightLevelSummary(sharedPreferences));
		mBookShelfEdgeLevel.setSummary(getBookShelfEdgeLevelSummary(sharedPreferences));
		// カスタム画像の選択後に失敗する場合があるので再設定する
		// 一先ず無効にしてからリスナーを再設定して有効にする
		SetCommonActivity.SetOrientationEventListenerDisable(sharedPreferences);
		SetCommonActivity.SetOrientationEventListener(this, sharedPreferences);
		SetCommonActivity.SetOrientationEventListenerEnable(sharedPreferences);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SetCommonActivity.SetOrientationEventListenerDisable(sharedPreferences);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if (key.equals(DEF.KEY_BOOKSHELFPATTERN)) {
			//
			mBookShelfPattern.setSummary(getBookShelfPatternSummary(mResources, sharedPreferences));
		}
		else if(key.equals(DEF.KEY_THUMBNAILTOPSPACE)){
			mThumbnailTopSpace.setSummary(getmThumbnailTopSpaceSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_THUMBNAILBOTTOMSPACE)){
			mThumbnailBottomSpace.setSummary(getmThumbnailBottomSpaceSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_FILENAMEBOTTOMSPACE)){
			mFilenameBottomSpace.setSummary(getmFilenameBottomSpaceSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_BOOKSHELFBRIGHTLEVEL)){
			mBookShelfBrightLevel.setSummary(getmBookShelfBrightLevelSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_BOOKSHELFEDGELEVEL)){
			mBookShelfEdgeLevel.setSummary(getBookShelfEdgeLevelSummary(sharedPreferences));
		}
	}

	// 設定の読込
	public static int getBookShelfPattern(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_BOOKSHELFPATTERN, DEF.DEFAULT_BOOKSHELFPATTERN);
		if (val == 99) {
			val = 9;
		}
		return val;
	}

	public static boolean getBookShelfColorExtOn(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_BOOKSHELFCOLOREXTON, false);
		return flag;
	}

	public static boolean getBookShelfAfterCircleOn(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_BOOKSHELFAFTERCURCLEON, false);
		return flag;
	}

	public static boolean getBookShelfTextSplitOn(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_BOOKSHELFTEXTSPLITON, false);
		return flag;
	}

	public static int getmThumbnailTopSpace(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_THUMBNAILTOPSPACE, DEF.DEFAULT_THUMBNAILTOPSPACE);
		return val;
	}
	public static int getmThumbnailBottomSpace(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_THUMBNAILBOTTOMSPACE, DEF.DEFAULT_THUMBNAILBOTTOMSPACE);
		return val;
	}
	public static int getmFilenameBottomSpace(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_FILENAMEBOTTOMSPACE, DEF.DEFAULT_FILENAMEBOTTOMSPACE);
		return val;
	}
	public static int getmBookShelfBrightLevel(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_BOOKSHELFBRIGHTLEVEL, DEF.DEFAULT_BOOKSHELFBRIGHTLEVEL);
		return val;
	}
	public static int getBookShelfEdgeLevel(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_BOOKSHELFEDGELEVEL, DEF.DEFAULT_BOOKSHELFEDGELEVEL);
		return val;
	}

	public static String getBookShelfPatternSummary(Resources res, SharedPreferences sharedPreferences){
		int val1 = getBookShelfPattern(sharedPreferences);
		return res.getString(SetBookShelfPatternName[val1]);
	}

	public static String getmThumbnailTopSpaceSummary(SharedPreferences sharedPreferences){
		int val = getmThumbnailTopSpace(sharedPreferences);
		return String.valueOf(val) + " " + mResources.getString(R.string.unitSumm1);
	}
	public static String getmThumbnailBottomSpaceSummary(SharedPreferences sharedPreferences){
		int val = getmThumbnailBottomSpace(sharedPreferences);
		return String.valueOf(val) + " " + mResources.getString(R.string.unitSumm1);
	}
	public static String getmFilenameBottomSpaceSummary(SharedPreferences sharedPreferences){
		int val = getmFilenameBottomSpace(sharedPreferences);
		return String.valueOf(val) + " " + mResources.getString(R.string.unitSumm1);
	}
	public static String getmBookShelfBrightLevelSummary(SharedPreferences sharedPreferences){
		int val = getmBookShelfBrightLevel(sharedPreferences);
		return String.valueOf(val) + " " + mResources.getString(R.string.srngSumm2);
	}
	public static String getBookShelfEdgeLevelSummary(SharedPreferences sharedPreferences){
		int val = getBookShelfEdgeLevel(sharedPreferences);
		return String.valueOf(val);
	}

}
