package src.comitton.config;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import android.preference.ListPreference;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.widget.SeekBar;

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.view.View;
import android.view.WindowManager;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.Logcat;
import src.comitton.common.DEF;
import src.comitton.config.seekbar.CornerEndHeightImageLevelSeekbar;
import src.comitton.config.seekbar.CornerEndWidthImageLevelSeekbar;
import src.comitton.imageview.TouchPanelView;
import src.comitton.config.SetCommonActivity;

public class SetCornerEndImageViewerActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private static TouchPanelView mTpView;

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;

	private ListPreference mTopLeftCornerTap;
	private ListPreference mTopRightCornerTap;
	private ListPreference mBottomLeftCornerTap;
	private ListPreference mBottomRightCornerTap;
	private ListPreference mLeftEndTap;
	private ListPreference mRightEndTap;
	private ListPreference mTopEndTap;
	private ListPreference mBottomEndTap;
	private ListPreference mDoubleTap;
	private ListPreference mTopRightTap;
	private ListPreference mTopLeftTap;
	private ListPreference mBottomLeftTap;
	private ListPreference mBottomRightTap;
	private ListPreference mSingleTap;
	private CornerEndWidthImageLevelSeekbar mCornerEndWidthLevel;
	private CornerEndHeightImageLevelSeekbar mCornerEndHeightLevel;

	private String[] mItems = null;

	@SuppressWarnings("deprecation")
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

		String[] mProfileWord = new String[10];

		// 初期値を読み出す
		mProfileWord[0] = sharedPreferences.getString(DEF.KEY_PROFILE_WORD_01, "");
		mProfileWord[1] = sharedPreferences.getString(DEF.KEY_PROFILE_WORD_02, "");
		mProfileWord[2] = sharedPreferences.getString(DEF.KEY_PROFILE_WORD_03, "");
		mProfileWord[3] = sharedPreferences.getString(DEF.KEY_PROFILE_WORD_04, "");
		mProfileWord[4] = sharedPreferences.getString(DEF.KEY_PROFILE_WORD_05, "");
		mProfileWord[5] = sharedPreferences.getString(DEF.KEY_PROFILE_WORD_06, "");
		mProfileWord[6] = sharedPreferences.getString(DEF.KEY_PROFILE_WORD_07, "");
		mProfileWord[7] = sharedPreferences.getString(DEF.KEY_PROFILE_WORD_08, "");
		mProfileWord[8] = sharedPreferences.getString(DEF.KEY_PROFILE_WORD_09, "");
		mProfileWord[9] = sharedPreferences.getString(DEF.KEY_PROFILE_WORD_10, "");

		final int[] loop = {0};
		String[] items_temp = new String[mTpView.HardwareKeyName.length];
		// タッチパネル設定に有効な項目を取り出して格納する
		for (int i = 0; i < mTpView.HardwareKeyName.length; i++) {
			// イメージビューア
			if (mTpView.ImgEnable[i]) {
				// 有効な項目のみ格納する
				items_temp[loop[0]] = this.getResources().getString(mTpView.HardwareKeyName[i]);
				if (i >= DEF.TAP_PROFILE1 && i <= DEF.TAP_PROFILE5 && !mProfileWord[i - DEF.TAP_PROFILE1].equals("")) {
					items_temp[loop[0]] = mProfileWord[i - DEF.TAP_PROFILE1];
				}
				if (i >= DEF.TAP_PROFILE6 && i <= DEF.TAP_PROFILE10 && !mProfileWord[i - DEF.TAP_PROFILE6 + 5].equals("")) {
					items_temp[loop[0]] = mProfileWord[i - DEF.TAP_PROFILE6 + 5];
				}
				loop[0]++;
			}
		}
		// 最大数を合わせなおして格納する
		// 表示される項目と保存される値を作成
		CharSequence[] items = null;
		CharSequence[] values = null;
		items = new String[loop[0]];
		values = new String[loop[0]];
		mItems = new String[loop[0]];
		for (int i = 0; i < loop[0]; i++) {
			items[i] = items_temp[i];
			values[i] = String.valueOf(i);
			mItems[i] = items_temp[i];
		}

		// チェックボックス/シークバーをxmlで作成
		addPreferencesFromResource(R.xml.cornerendimage);

		// 設定数が多いためxmlに記述するのは大変なので動的に作成
		// PreferenceScreenを作成して上のxmlに追記
		PreferenceScreen screen = getPreferenceScreen();
		PreferenceCategory category = new PreferenceCategory(this);
		category.setTitle(R.string.CornerEndTitle);  
		screen.addPreference(category);  

		CharSequence[] titles = new String[DEF.CornerEndTitleName.length];
		for (int i = 0; i < DEF.CornerEndTitleName.length; i++) {
			titles[i] = this.getResources().getString(DEF.CornerEndTitleName[i]);
		}
		// 表示される項目と保存される値を作成
		for (int i = 0; i < DEF.CornerEndTitleName.length; i++) {
			// ListPreferenceを作成
			ListPreference listPreference = new ListPreference(this);
			// キーを設定
			listPreference.setKey(DEF.CornerEndKeyIIdName[i]);
			// タイトルを設定
			listPreference.setTitle(titles[i]);
			listPreference.setDialogTitle(titles[i]);
			// 表示される項目
			listPreference.setEntries(items);
			// 保存される値
			listPreference.setEntryValues(values);
			// デフォルト値(無反応)
			listPreference.setDefaultValue("1");
			listPreference.setSummary("dummy");
			// 作成したListPreferenceをPreferenceScreenに追加
			screen.addPreference(listPreference);
			// 作成したPreferenceScreenをフラグメントに設定
			setPreferenceScreen(screen);
		}

		mTopLeftCornerTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CORNEREND_I_TAP_01);
		mTopRightCornerTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CORNEREND_I_TAP_02);
		mBottomLeftCornerTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CORNEREND_I_TAP_03);
		mBottomRightCornerTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CORNEREND_I_TAP_04);
		mLeftEndTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CORNEREND_I_TAP_05);
		mRightEndTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CORNEREND_I_TAP_06);
		mTopEndTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CORNEREND_I_TAP_07);
		mBottomEndTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CORNEREND_I_TAP_08);
		mDoubleTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CORNEREND_I_TAP_09);
		mTopRightTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CORNEREND_I_TAP_10);
		mTopLeftTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CORNEREND_I_TAP_11);
		mBottomLeftTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CORNEREND_I_TAP_12);
		mBottomRightTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CORNEREND_I_TAP_13);
		mSingleTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CORNEREND_I_TAP_14);
		mCornerEndWidthLevel = (CornerEndWidthImageLevelSeekbar)getPreferenceScreen().findPreference(DEF.KEY_CORNERENDIWIDTHLEVEL);
		mCornerEndHeightLevel = (CornerEndHeightImageLevelSeekbar)getPreferenceScreen().findPreference(DEF.KEY_CORNERENDIHEIGHTLEVEL);

	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mTopLeftCornerTap.setSummary(getTopLeftCornerTapSummary(sharedPreferences));
		mTopRightCornerTap.setSummary(getTopRightCornerTapSummary(sharedPreferences));
		mBottomLeftCornerTap.setSummary(getBottomLeftCornerTapSummary(sharedPreferences));
		mBottomRightCornerTap.setSummary(getBottomRightCornerTapSummary(sharedPreferences));
		mLeftEndTap.setSummary(getLeftEndTapSummary(sharedPreferences));
		mRightEndTap.setSummary(getRightEndTapSummary(sharedPreferences));
		mTopEndTap.setSummary(getTopEndTapSummary(sharedPreferences));
		mBottomEndTap.setSummary(getBottomEndTapSummary(sharedPreferences));
		mDoubleTap.setSummary(getDoubleTapSummary(sharedPreferences));
		mTopRightTap.setSummary(getTopRightTapSummary(sharedPreferences));
		mTopLeftTap.setSummary(getTopLeftTapSummary(sharedPreferences));
		mBottomLeftTap.setSummary(getBottomLeftTapSummary(sharedPreferences));
		mBottomRightTap.setSummary(getBottomRightTapSummary(sharedPreferences));
		mSingleTap.setSummary(getSingleTapSummary(sharedPreferences));
		mCornerEndWidthLevel.setSummary(getCornerEndWidthLevelSummary(sharedPreferences));
		mCornerEndHeightLevel.setSummary(getCornerEndHeightLevelSummary(sharedPreferences));
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
		if(key.equals(DEF.KEY_CORNEREND_I_TAP_01)){
			mTopLeftCornerTap.setSummary(getTopLeftCornerTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNEREND_I_TAP_02)){
			mTopRightCornerTap.setSummary(getTopRightCornerTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNEREND_I_TAP_03)){
			mBottomLeftCornerTap.setSummary(getBottomLeftCornerTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNEREND_I_TAP_04)){
			mBottomRightCornerTap.setSummary(getBottomRightCornerTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNEREND_I_TAP_05)){
			mLeftEndTap.setSummary(getLeftEndTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNEREND_I_TAP_06)){
			mRightEndTap.setSummary(getRightEndTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNEREND_I_TAP_07)){
			mTopEndTap.setSummary(getTopEndTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNEREND_I_TAP_08)){
			mBottomEndTap.setSummary(getBottomEndTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNEREND_I_TAP_09)){
			mDoubleTap.setSummary(getDoubleTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNEREND_I_TAP_10)){
			mTopRightTap.setSummary(getTopRightTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNEREND_I_TAP_11)){
			mTopLeftTap.setSummary(getTopLeftTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNEREND_I_TAP_12)){
			mBottomLeftTap.setSummary(getBottomLeftTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNEREND_I_TAP_13)){
			mBottomRightTap.setSummary(getBottomRightTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNEREND_I_TAP_14)){
			mSingleTap.setSummary(getSingleTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNERENDIWIDTHLEVEL)){
			mCornerEndWidthLevel.setSummary(getCornerEndWidthLevelSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CORNERENDIHEIGHTLEVEL)){
			mCornerEndHeightLevel.setSummary(getCornerEndHeightLevelSummary(sharedPreferences));
		}

	}

	// 設定の読込
	public static int getTopLeftCornerTap(SharedPreferences sharedPreferences) {
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CORNEREND_I_TAP_01, "1"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getTopRightCornerTap(SharedPreferences sharedPreferences) {
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CORNEREND_I_TAP_02, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getBottomLeftCornerTap(SharedPreferences sharedPreferences) {
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CORNEREND_I_TAP_03, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getBottomRightCornerTap(SharedPreferences sharedPreferences) {
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CORNEREND_I_TAP_04, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getLeftEndTap(SharedPreferences sharedPreferences) {
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CORNEREND_I_TAP_05, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getRightEndTap(SharedPreferences sharedPreferences) {
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CORNEREND_I_TAP_06, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getTopEndTap(SharedPreferences sharedPreferences) {
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CORNEREND_I_TAP_07, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getBottomEndTap(SharedPreferences sharedPreferences) {
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CORNEREND_I_TAP_08, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getDoubleTap(SharedPreferences sharedPreferences) {
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CORNEREND_I_TAP_09, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getTopRightTap(SharedPreferences sharedPreferences) {
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CORNEREND_I_TAP_10, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getTopLeftTap(SharedPreferences sharedPreferences) {
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CORNEREND_I_TAP_11, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getBottomLeftTap(SharedPreferences sharedPreferences) {
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CORNEREND_I_TAP_12, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getBottomRightTap(SharedPreferences sharedPreferences) {
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CORNEREND_I_TAP_13, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getSingleTap(SharedPreferences sharedPreferences) {
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CORNEREND_I_TAP_14, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getCornerEndWidthLevel(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_CORNERENDIWIDTHLEVEL, DEF.DEFAULT_CORNERENDLEVEL);
		return num;
	}
	public static int getCornerEndHeightLevel(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_CORNERENDIHEIGHTLEVEL, DEF.DEFAULT_CORNERENDLEVEL);
		return num;
	}
	public static boolean getCornerEndEnable(SharedPreferences sharedPreferences){
		boolean num;
		num = DEF.getBoolean(sharedPreferences, DEF.KEY_CORNERENDIENABLE, false);
		return num;
	}

	// 設定の読込(定義変更中)
	private String getTopLeftCornerTapSummary(SharedPreferences sharedPreferences){
		int val = getTopLeftCornerTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getTopRightCornerTapSummary(SharedPreferences sharedPreferences){
		int val = getTopRightCornerTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getBottomLeftCornerTapSummary(SharedPreferences sharedPreferences){
		int val = getBottomLeftCornerTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getBottomRightCornerTapSummary(SharedPreferences sharedPreferences){
		int val = getBottomRightCornerTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getLeftEndTapSummary(SharedPreferences sharedPreferences){
		int val = getLeftEndTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getRightEndTapSummary(SharedPreferences sharedPreferences){
		int val = getRightEndTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getTopEndTapSummary(SharedPreferences sharedPreferences){
		int val = getTopEndTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getBottomEndTapSummary(SharedPreferences sharedPreferences){
		int val = getBottomEndTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getDoubleTapSummary(SharedPreferences sharedPreferences){
		int val = getDoubleTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getTopRightTapSummary(SharedPreferences sharedPreferences){
		int val = getTopRightTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getTopLeftTapSummary(SharedPreferences sharedPreferences){
		int val = getTopLeftTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getBottomLeftTapSummary(SharedPreferences sharedPreferences){
		int val = getBottomLeftTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getBottomRightTapSummary(SharedPreferences sharedPreferences){
		int val = getBottomRightTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getSingleTapSummary(SharedPreferences sharedPreferences){
		int val = getSingleTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getCornerEndWidthLevelSummary(SharedPreferences sharedPreferences){
		int val = getCornerEndWidthLevel(sharedPreferences);
		return	String.valueOf(val) + " %";
	}
	private String getCornerEndHeightLevelSummary(SharedPreferences sharedPreferences){
		int val = getCornerEndHeightLevel(sharedPreferences);
		return	String.valueOf(val) + " %";
	}

	private static int convertdata(int data) {
		int count = 0;
		int founddata = 0;
		// タッチパネル設定に有効な項目を取り出す
		for (int i = 0; i < mTpView.HardwareKeyName.length; i++) {
			// イメージビューア
			if (mTpView.ImgEnable[i]) {
				// 有効な項目のみ
				if (count == data) {
					// 一致した
					founddata = i;
					break;
				}
				count++;
			}
		}
		return founddata;
	}

}
