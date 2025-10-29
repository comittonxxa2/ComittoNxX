package src.comitton.config;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.WindowManager;

import androidx.preference.PreferenceManager;

import src.comitton.config.SetCommonActivity;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;

public class SetSortActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	Resources mResources;
	private ListPreference mSoftFileTop;
	private ListPreference mSoftDirFile;
	private ListPreference mSoftImageFile;
	private ListPreference mSoftTextFile;
	private ListPreference mSoftCompFile;
	private ListPreference mSoftPdfFile;
	private ListPreference mSoftEpubFile;
	private ListPreference mSoftOtherFile;

 	public static final int[] ListSortTop =
 		{ R.string.sorttop00
 		, R.string.sorttop01
 		, R.string.sorttop02
 		, R.string.sorttop03
 		, R.string.sorttop04
 		, R.string.sorttop05
 		, R.string.sorttop06
 		, R.string.sorttop07 };
 	public static final int[] ListSortName =
		{ R.string.ssort00		// ソートなし
		, R.string.ssort01		// ファイル名順(昇順)
		, R.string.ssort02		// ファイル名順(降順)
		, R.string.ssort03		// 新しい順
		, R.string.ssort04		// 古い順
		, R.string.ssort05		// シャッフル
		, R.string.ssort06		// 読書中の割合が多い順
		, R.string.ssort07 };	// 読書中の割合が少ない順

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;

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

		addPreferencesFromResource(R.xml.sort);
		mSoftFileTop = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_SORTFILETOP);
		mSoftDirFile = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_SORTDIRFILE);
		mSoftImageFile = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_SORTIMAGEFILE);
		mSoftTextFile = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_SORTTEXTFILE);
		mSoftCompFile = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_SORTCOMPFILE);
		mSoftPdfFile = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_SORTPDFFILE);
		mSoftEpubFile = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_SORTEPUBFILE);
		mSoftOtherFile = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_SORTOTHERFILE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mSoftFileTop.setSummary(getSoftFileTopSummary(sharedPreferences));
		mSoftDirFile.setSummary(getSoftDirFileSummary(sharedPreferences));
		mSoftImageFile.setSummary(getSoftImageFileSummary(sharedPreferences));
		mSoftTextFile.setSummary(getSoftTextFileSummary(sharedPreferences));
		mSoftCompFile.setSummary(getSoftCompFileSummary(sharedPreferences));
		mSoftPdfFile.setSummary(getSoftPdfFileSummary(sharedPreferences));
		mSoftEpubFile.setSummary(getSoftEpubFileSummary(sharedPreferences));
		mSoftOtherFile.setSummary(getSoftOtherFileSummary(sharedPreferences));

		SetCommonActivity.SetOrientationEventListenerEnable(sharedPreferences);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		SetCommonActivity.SetOrientationEventListenerDisable(sharedPreferences);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(DEF.KEY_SORTFILETOP)){
			mSoftFileTop.setSummary(getSoftFileTopSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_SORTDIRFILE)){
			mSoftDirFile.setSummary(getSoftDirFileSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_SORTIMAGEFILE)){
			mSoftImageFile.setSummary(getSoftImageFileSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_SORTTEXTFILE)){
			mSoftTextFile.setSummary(getSoftTextFileSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_SORTCOMPFILE)){
			mSoftCompFile.setSummary(getSoftCompFileSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_SORTPDFFILE)){
			mSoftPdfFile.setSummary(getSoftPdfFileSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_SORTEPUBFILE)){
			mSoftEpubFile.setSummary(getSoftEpubFileSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_SORTOTHERFILE)){
			mSoftOtherFile.setSummary(getSoftOtherFileSummary(sharedPreferences));
		}
	}

	// 設定の読込
	public static int getSoftFileTop(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_SORTFILETOP, "0");
		if( val < 0 || val > ListSortTop.length ){
			val = 0;
		}
		return val;
	}
	public static int getSoftDirFile(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_SORTDIRFILE, "0");
		if( val < 0 || val > ListSortName.length ){
			val = 0;
		}
		return val;
	}
	public static int getSoftImageFile(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_SORTIMAGEFILE, "0");
		if( val < 0 || val > ListSortName.length ){
			val = 0;
		}
		return val;
	}
	public static int getSoftTextFile(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_SORTTEXTFILE, "0");
		if( val < 0 || val > ListSortName.length ){
			val = 0;
		}
		return val;
	}
	public static int getSoftCompFile(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_SORTCOMPFILE, "0");
		if( val < 0 || val > ListSortName.length ){
			val = 0;
		}
		return val;
	}
	public static int getSoftPdfFile(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_SORTPDFFILE, "0");
		if( val < 0 || val > ListSortName.length ){
			val = 0;
		}
		return val;
	}
	public static int getSoftEpubFile(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_SORTEPUBFILE, "0");
		if( val < 0 || val > ListSortName.length ){
			val = 0;
		}
		return val;
	}
	public static int getSoftOtherFile(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_SORTOTHERFILE, "0");
		if( val < 0 || val > ListSortName.length ){
			val = 0;
		}
		return val;
	}

	public static boolean getSoftDirTop(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_SORTDIRTOP, false);
		return flag;
	}

	// 設定の読込(定義変更中)
	private String getSoftFileTopSummary(SharedPreferences sharedPreferences){
		int val = getSoftFileTop(sharedPreferences);
		Resources res = getResources();
		return res.getString(ListSortTop[val]);
	}
	private String getSoftDirFileSummary(SharedPreferences sharedPreferences){
		int val = getSoftDirFile(sharedPreferences);
		Resources res = getResources();
		return res.getString(ListSortName[val]);
	}
	private String getSoftImageFileSummary(SharedPreferences sharedPreferences){
		int val = getSoftImageFile(sharedPreferences);
		Resources res = getResources();
		return res.getString(ListSortName[val]);
	}
	private String getSoftTextFileSummary(SharedPreferences sharedPreferences){
		int val = getSoftTextFile(sharedPreferences);
		Resources res = getResources();
		return res.getString(ListSortName[val]);
	}
	private String getSoftCompFileSummary(SharedPreferences sharedPreferences){
		int val = getSoftCompFile(sharedPreferences);
		Resources res = getResources();
		return res.getString(ListSortName[val]);
	}
	private String getSoftPdfFileSummary(SharedPreferences sharedPreferences){
		int val = getSoftPdfFile(sharedPreferences);
		Resources res = getResources();
		return res.getString(ListSortName[val]);
	}
	private String getSoftEpubFileSummary(SharedPreferences sharedPreferences){
		int val = getSoftEpubFile(sharedPreferences);
		Resources res = getResources();
		return res.getString(ListSortName[val]);
	}
	private String getSoftOtherFileSummary(SharedPreferences sharedPreferences){
		int val = getSoftOtherFile(sharedPreferences);
		Resources res = getResources();
		return res.getString(ListSortName[val]);
	}

}
