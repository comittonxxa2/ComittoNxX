package src.comitton.config;

import src.comitton.common.Logcat;
import src.comitton.helpview.HelpActivity;
import src.comitton.common.DEF;
import jp.dip.muracoro.comittonx.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.OrientationEventListener;
import android.app.Activity;

import src.comitton.config.SetCommonActivity;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceHeaderFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;


public class SetCommonActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private static final String TAG = "SetCommonActivity";


	public class SetCommonFragment extends PreferenceHeaderFragmentCompat implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

		public SetCommonFragment () {

		}

		@Override
		public PreferenceFragmentCompat onCreatePreferenceHeader() {
			return new HeaderFragment();
		}

	}

	public class HeaderFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.common, rootKey);
		}
	}

	public static final int[] RotateName =
		{ R.string.rotaall00		// 回転あり
		, R.string.rotaall01		// 縦固定
		, R.string.rotaall02		// 横固定
		, R.string.rotaall03		// 回転あり(縦上下反転)
		, R.string.rotaall04		// 回転あり(横上下反転)
		, R.string.rotaall05		// 回転あり(縦横上下反転)
		, R.string.rotaall06		// 縦固定(上下反転)
		, R.string.rotaall07 };		// 横固定(上下反転)

	private ListPreference mRotateBtn;
	private ListPreference mCharset;
	private ListPreference mViewRotaAll;

	private EditTextPreference mPriorityWord01;
	private EditTextPreference mPriorityWord02;
	private EditTextPreference mPriorityWord03;
	private EditTextPreference mPriorityWord04;
	private EditTextPreference mPriorityWord05;
	private EditTextPreference mPriorityWord06;
	private EditTextPreference mPriorityWord07;
	private EditTextPreference mPriorityWord08;
	private EditTextPreference mPriorityWord09;
	private EditTextPreference mPriorityWord10;

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;

	private static OrientationEventListener orientationEventListener = null;
	private static int deviceOrientation = -1;
	private static SharedPreferences sharedPreferences;

	public static final int[] RotateBtnName =
		{ R.string.rotabtn00	// 使用しない
		, R.string.rotabtn01	// フォーカスキー
		, R.string.rotabtn02 };	// シャッターキー

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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
		SetOrientationEventListener(this, sharedPreferences);

		addPreferencesFromResource(R.xml.common);
		mRotateBtn  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_ROTATEBTN);
		mCharset    = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CHARSET);
		mViewRotaAll   = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_VIEWROTAALL);

		mPriorityWord01 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_01);
		mPriorityWord02 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_02);
		mPriorityWord03 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_03);
		mPriorityWord04 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_04);
		mPriorityWord05 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_05);
		mPriorityWord06 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_06);
		mPriorityWord07 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_07);
		mPriorityWord08 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_08);
		mPriorityWord09 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_09);
		mPriorityWord10 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_10);

		// 項目選択
		PreferenceScreen onlineHelp = (PreferenceScreen) findPreference(DEF.KEY_COMMHELP);
		onlineHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// Activityの遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_common);	// 設定画面
				Intent intent;
				intent = new Intent(SetCommonActivity.this, HelpActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("Url", url);
				startActivity(intent);
				return true;
			}
		});
	}

	private static void RotateMain(Activity activity, int orientation, int viewrota) {
		if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
			return;
		}
		if (orientation >= 45 && orientation < 135) {
			// 90度
			if (deviceOrientation != 3) {
				deviceOrientation = 3;
				if (viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT || viewrota == DEF.ROTATE_ALL_AUTO || viewrota == DEF.ROTATE_ALL_LANDSCAPE) {
					// 横上下反転
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
				}
				if (viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT_LANDSCAPE || viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_LANDSCAPE || viewrota == DEF.ROTATE_ALL_REVERSE_LANDSCAPE) {
					// 横通常表示
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
			}
		}
		else if (orientation >= 135 && orientation < 225) {
			// 180度
			if (deviceOrientation != 2) {
				deviceOrientation = 2;
				if (viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_LANDSCAPE || viewrota == DEF.ROTATE_ALL_AUTO || viewrota == DEF.ROTATE_ALL_PORTRAIT) {
					// 縦上下反転
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
				}
				if (viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT_LANDSCAPE || viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT || viewrota == DEF.ROTATE_ALL_REVERSE_PORTRAIT) {
					// 縦通常表示
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			}
		}
		else if (orientation >= 225 && orientation < 315) {
			// 270度
			if (deviceOrientation != 1) {
				deviceOrientation = 1;
				if (viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_LANDSCAPE || viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT_LANDSCAPE || viewrota == DEF.ROTATE_ALL_REVERSE_LANDSCAPE) {
					// 横上下反転
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
				}
				if (viewrota == DEF.ROTATE_ALL_AUTO || viewrota == DEF.ROTATE_ALL_LANDSCAPE || viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT) {
					// 横通常表示
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
			}
		}
		else {
			// 0度
			if (deviceOrientation != 0) {
				deviceOrientation = 0;
				if (viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT || viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT_LANDSCAPE || viewrota == DEF.ROTATE_ALL_REVERSE_PORTRAIT) {
					// 縦上下反転
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
				}
				if (viewrota == DEF.ROTATE_ALL_AUTO || viewrota == DEF.ROTATE_ALL_PORTRAIT || viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_LANDSCAPE) {
					// 縦通常表示
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			}
		}
	}

	public static void SetOrientationEventListener(Activity activity, SharedPreferences sharedPreferences) {
		// 起動時は回転動作にならないので固定値の場合は個別で設定する
		int viewrota = getViewRotaAll(sharedPreferences);
		if (SetCommonActivity.getForceTradOldViewRotate(sharedPreferences)) {
			// 従来の設定で回転させる
			DEF.setRotationAll(activity, viewrota);
			return;
		}
		deviceOrientation = -1;
		switch (viewrota) {
			case 1:
				RotateMain(activity, 0, viewrota);
				break;
			case 2:
				RotateMain(activity ,270, viewrota);
				break;
			case 6:
				RotateMain(activity, 180, viewrota);
				break;
			case 7:
				RotateMain(activity, 90, viewrota);
				break;
		}
		orientationEventListener = new OrientationEventListener(activity) {
			// 傾きセンサーの角度を得る
			public void onOrientationChanged(int orientation) {
				RotateMain(activity, orientation, viewrota);
			}
		};
	}

	public static void SetOrientationEventListenerEnable(SharedPreferences sharedPreferences) {
		if (SetCommonActivity.getForceTradOldViewRotate(sharedPreferences) || orientationEventListener == null) {
			return;
		}
		orientationEventListener.enable();
	}

	public static void SetOrientationEventListenerDisable(SharedPreferences sharedPreferences) {
		if (SetCommonActivity.getForceTradOldViewRotate(sharedPreferences) || orientationEventListener == null) {
			return;
		}
		orientationEventListener.disable();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mRotateBtn.setSummary(getRotateBtnSummary(sharedPreferences));	// 回転用ボタン
		mCharset.setSummary(getCharsetSummary(sharedPreferences));		// 文字コード
		mViewRotaAll.setSummary(getViewRotaAllSummary(sharedPreferences));	// 画面の回転制御

		mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_01, ""));
		mPriorityWord02.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_02, ""));
		mPriorityWord03.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_03, ""));
		mPriorityWord04.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_04, ""));
		mPriorityWord05.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_05, ""));
		mPriorityWord06.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_06, ""));
		mPriorityWord07.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_07, ""));
		mPriorityWord08.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_08, ""));
		mPriorityWord09.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_09, ""));
		mPriorityWord10.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_10, ""));
		SetOrientationEventListenerEnable(sharedPreferences);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SetOrientationEventListenerDisable(sharedPreferences);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if(key.equals(DEF.KEY_ROTATEBTN)){
			//
			mRotateBtn.setSummary(getRotateBtnSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CHARSET)){
			//
			mCharset.setSummary(getCharsetSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_VIEWROTAALL)){
			//
			mViewRotaAll.setSummary(getViewRotaAllSummary(sharedPreferences));
		}

		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_01)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_01, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_02)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_02, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_03)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_03, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_04)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_04, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_05)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_05, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_06)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_06, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_07)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_07, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_08)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_08, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_09)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_09, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_10)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_10, ""));
		}
	}

	// 設定の読込
	public static int getRotateBtn(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_ROTATEBTN, "1");
		if (val < 0 || val >= RotateBtnName.length){
			val = 0;
		}
		return val;
	}

	public static int getCharset(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_CHARSET, "1");
		if (val < 0 || val >= DEF.CharsetList.length){
			val = 1;
		}
		return val;
	}

	public static int getViewRotaAll(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_VIEWROTAALL, "1");
		if( val < 0 || val > RotateName.length ){
			val = 0;
		}
		return val;
	}

	public static boolean getHiddenFile(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_HIDDENFILE, true);
		return flag;
	}

	// 設定の読込(定義変更中)
	private String getRotateBtnSummary(SharedPreferences sharedPreferences){
		int val = getRotateBtn(sharedPreferences);
		Resources res = getResources();
		return res.getString(RotateBtnName[val]);
	}

	private static String getCharsetSummary(SharedPreferences sharedPreferences){
		int val = getCharset(sharedPreferences);
		return DEF.CharsetList[val];
	}

	private String getViewRotaAllSummary(SharedPreferences sharedPreferences){
		int val = getViewRotaAll(sharedPreferences);
		Resources res = getResources();
		return res.getString(RotateName[val]);
	}

	public static void loadSettings(SharedPreferences sharedPreferences) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		DEF.CHAR_DETECT = sharedPreferences.getBoolean(DEF.KEY_CHAR_DETECT, true);
		DEF.CHARSET = getCharsetSummary(sharedPreferences);

		DEF.SORT_BY_IGNORE_WIDTH = sharedPreferences.getBoolean(DEF.KEY_SORT_BY_IGNORE_WIDTH, true);
		DEF.SORT_BY_IGNORE_CASE = sharedPreferences.getBoolean(DEF.KEY_SORT_BY_IGNORE_CASE, true);
		DEF.SORT_BY_SYMBOL = sharedPreferences.getBoolean(DEF.KEY_SORT_BY_SYMBOL, true);
		DEF.SORT_BY_NATURAL_NUMBERS = sharedPreferences.getBoolean(DEF.KEY_SORT_BY_NATURAL_NUMBERS, true);
		DEF.SORT_BY_KANJI_NUMERALS = sharedPreferences.getBoolean(DEF.KEY_SORT_BY_KANJI_NUMERALS, true);
		DEF.SORT_BY_JAPANESE_VOLUME_NAME = sharedPreferences.getBoolean(DEF.KEY_SORT_BY_JAPANESE_VOLUME_NAME, true);
		DEF.SORT_BY_FILE_TYPE = sharedPreferences.getBoolean(DEF.KEY_SORT_BY_FILE_TYPE, true);

		ArrayList<String> priorityWords = new ArrayList<String>();
		String word = "";
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_01, "cover");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_02, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_03, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_04, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_05, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_06, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_07, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_08, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_09, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_10, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		DEF.PRIORITY_WORDS = priorityWords.toArray(new String[0]);

		Logcat.d(logLevel, "DEF.PRIORITY_WORDS=" + Arrays.toString(DEF.PRIORITY_WORDS));
	}

	public static boolean getForceHideNavigationBar(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_FORCENAVIGATIONBAR, false);
		return flag;
	}

	public static boolean getForceHideStatusBar(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_FORCESTATUSBAR, false);
		return flag;
	}

	public static boolean getForceTradOldViewRotate(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_FORCETRADDISPLAYOLDVIEWROTATE, false);
		return flag;
	}

	public static boolean getReverseRotate(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_REVERSEROTARE, false);
		return flag;
	}

	// 終了処理
	protected void onDestroy() {
		super.onDestroy();
		loadSettings(getPreferenceScreen().getSharedPreferences());
	}
}
