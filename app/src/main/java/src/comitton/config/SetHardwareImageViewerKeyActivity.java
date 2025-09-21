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

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.view.View;
import android.view.WindowManager;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.Logcat;
import src.comitton.common.DEF;
import src.comitton.imageview.TouchPanelView;
import src.comitton.config.SetCommonActivity;

public class SetHardwareImageViewerKeyActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private static TouchPanelView mTpView;

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;

	private ListPreference mBackKey;
	private ListPreference mVolumeUpKey;
	private ListPreference mVolumeDownKey;
	private ListPreference mCameraKey;
	private ListPreference mFocusKey;
	private ListPreference mMenuKey;
	private ListPreference mDpadLeftKey;
	private ListPreference mDpadRightKey;
	private ListPreference mDpadUpKey;
	private ListPreference mDpadDownKey;
	private ListPreference mDpadCenterKey;
	private ListPreference mEnterKey;
	private ListPreference mDelKey;
	private ListPreference mSpaceKey;
	private ListPreference mSearchKey;
	private ListPreference mPageUpKey;
	private ListPreference mPageDownKey;
	private ListPreference mEscapeKey;
	private ListPreference mMoveHomeKey;
	private ListPreference mMoveEndKey;
	private ListPreference mForwardKey;
	private ListPreference mButtonL1Key;
	private ListPreference mButtonL2Key;
	private ListPreference mButtonL3Key;
	private ListPreference mButtonR1Key;
	private ListPreference mButtonR2Key;
	private ListPreference mButtonR3Key;
	private ListPreference mButtonAKey;
	private ListPreference mButtonBKey;
	private ListPreference mButtonXKey;
	private ListPreference mButtonYKey;
	private ListPreference mButtonSelectKey;
	private ListPreference mButtonStartKey;
	private ListPreference mMediaNextKey;
	private ListPreference mMediaPrevKey;
	private ListPreference mMediaPlayPauseKey;
	private ListPreference mCustom01Key;
	private ListPreference mCustom02Key;
	private ListPreference mCustom03Key;
	private ListPreference mCustom04Key;
	private ListPreference mCustom05Key;
	private ListPreference mCustom06Key;
	private ListPreference mCustom07Key;
	private ListPreference mCustom08Key;
	private ListPreference mCustom09Key;
	private ListPreference mCustom10Key;
	private int mMode = 1;

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

		final int[] loop = {0};
		String[] items_temp = new String[mTpView.HardwareKeyName.length];
		// タッチパネル設定に有効な項目を取り出して格納する
		for (int i = 0; i < mTpView.HardwareKeyName.length; i++) {
			// イメージビューア
			if (mTpView.ImgEnable[i]) {
				// 有効な項目のみ格納する
				items_temp[loop[0]] = this.getResources().getString(mTpView.HardwareKeyName[i]);
				loop[0]++;
			}
		}
		// 最大数を合わせなおして格納する
		// 表示される項目と保存される値を作成
		CharSequence[] items = null;
		CharSequence[] values = null;
		items = new String[loop[0]];
		values = new String[loop[0]];
		for (int i = 0; i < loop[0]; i++) {
			items[i] = items_temp[i];
			values[i] = String.valueOf(i);
		}

		// 設定数が多いためxmlに記述するのは大変なので動的に作成
		// PreferenceScreenを作成
		PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
		PreferenceCategory category = new PreferenceCategory(this);
		category.setTitle(R.string.imageviwerhardwarekeytitle);  
		screen.addPreference(category);  

		CharSequence[] titles = new String[DEF.HardwareKeyTitleName.length];
		for (int i = 0; i < DEF.HardwareKeyTitleName.length; i++) {
			titles[i] = this.getResources().getString(DEF.HardwareKeyTitleName[i]);
		}
		// 表示される項目と保存される値を作成
		for (int i = 0; i < DEF.HardwareKeyTitleName.length; i++) {
			// ListPreferenceを作成
			ListPreference listPreference = new ListPreference(this);
			// キーを設定
			listPreference.setKey(DEF.HardwareImageKeyIdName[i]);
			// タイトルを設定
			listPreference.setTitle(titles[i]);
			listPreference.setDialogTitle(titles[i]);
			// 表示される項目
			listPreference.setEntries(items);
			// 保存される値
			listPreference.setEntryValues(values);
			// デフォルト値
			listPreference.setDefaultValue("0");
			listPreference.setSummary("dummy");
			// 作成したListPreferenceをPreferenceScreenに追加
			screen.addPreference(listPreference);
			// 作成したPreferenceScreenをフラグメントに設定
			setPreferenceScreen(screen);
		}

		mBackKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_BACK);
		mVolumeUpKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_VOLUME_UP);
		mVolumeDownKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_VOLUME_DOWN);
		mCameraKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_CAMERA);
		mFocusKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_FOCUS);
		mMenuKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_MENU);
		mDpadLeftKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_DPAD_LEFT);
		mDpadRightKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_DPAD_RIGHT);
		mDpadUpKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_DPAD_UP);
		mDpadDownKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_DPAD_DOWN);
		mDpadCenterKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_DPAD_CENTER);
		mEnterKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_ENTER);
		mDelKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_DEL);
		mSpaceKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_SPACE);
		mSearchKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_SEARCH);
		mPageUpKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_PAGE_UP);
		mPageDownKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_PAGE_DOWN);
		mEscapeKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_ESCAPE);
		mMoveHomeKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_MOVEHOME);
		mMoveEndKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_MOVEEND);
		mForwardKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_FORWARD);
		mButtonL1Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_BUTTON_L1);
		mButtonL2Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_BUTTON_L2);
		mButtonL3Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_BUTTON_THUMBL);
		mButtonR1Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_BUTTON_R1);
		mButtonR2Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_BUTTON_R2);
		mButtonR3Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_BUTTON_THUMBR);
		mButtonAKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_BUTTON_A);
		mButtonBKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_BUTTON_B);
		mButtonXKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_BUTTON_X);
		mButtonYKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_BUTTON_Y);
		mButtonSelectKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_BUTTON_SELECT);
		mButtonStartKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_BUTTON_START);
		mMediaNextKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_MEDIA_NEXT);
		mMediaPrevKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_MEDIA_PREVIOUS);
		mMediaPlayPauseKey  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_MEDIA_PLAY_PAUSE);
		mCustom01Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_CUSTOMKEY01);
		mCustom02Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_CUSTOMKEY02);
		mCustom03Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_CUSTOMKEY03);
		mCustom04Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_CUSTOMKEY04);
		mCustom05Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_CUSTOMKEY05);
		mCustom06Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_CUSTOMKEY06);
		mCustom07Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_CUSTOMKEY07);
		mCustom08Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_CUSTOMKEY08);
		mCustom09Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_CUSTOMKEY09);
		mCustom10Key  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CODE_I_CUSTOMKEY10);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mBackKey.setSummary(getBackKeySummary(sharedPreferences));
		mVolumeUpKey.setSummary(getVolumeUpKeySummary(sharedPreferences));
		mVolumeDownKey.setSummary(getVolumeDownKeySummary(sharedPreferences));
		mCameraKey.setSummary(getCameraKeySummary(sharedPreferences));
		mFocusKey.setSummary(getFocusKeySummary(sharedPreferences));
		mMenuKey.setSummary(getMenuKeySummary(sharedPreferences));
		mDpadLeftKey.setSummary(getDpadLeftKeySummary(sharedPreferences));
		mDpadRightKey.setSummary(getDpadRightKeySummary(sharedPreferences));
		mDpadUpKey.setSummary(getDpadUpKeySummary(sharedPreferences));
		mDpadDownKey.setSummary(getDpadDownKeySummary(sharedPreferences));
		mDpadCenterKey.setSummary(getDpadCenterKeySummary(sharedPreferences));
		mEnterKey.setSummary(getEnterKeySummary(sharedPreferences));
		mDelKey.setSummary(getDelKeySummary(sharedPreferences));
		mSpaceKey.setSummary(getSpaceKeySummary(sharedPreferences));
		mSearchKey.setSummary(getSearchKeySummary(sharedPreferences));
		mPageUpKey.setSummary(getPageUpKeySummary(sharedPreferences));
		mPageDownKey.setSummary(getPageDownKeySummary(sharedPreferences));
		mEscapeKey.setSummary(getEscapeKeySummary(sharedPreferences));
		mMoveHomeKey.setSummary(getMoveHomeKeySummary(sharedPreferences));
		mMoveEndKey.setSummary(getMoveEndKeySummary(sharedPreferences));
		mForwardKey.setSummary(getForwardKeySummary(sharedPreferences));
		mButtonL1Key.setSummary(getButtonL1KeySummary(sharedPreferences));
		mButtonL2Key.setSummary(getButtonL2KeySummary(sharedPreferences));
		mButtonL3Key.setSummary(getButtonL3KeySummary(sharedPreferences));
		mButtonR1Key.setSummary(getButtonR1KeySummary(sharedPreferences));
		mButtonR2Key.setSummary(getButtonR2KeySummary(sharedPreferences));
		mButtonR3Key.setSummary(getButtonR3KeySummary(sharedPreferences));
		mButtonAKey.setSummary(getButtonAKeySummary(sharedPreferences));
		mButtonBKey.setSummary(getButtonBKeySummary(sharedPreferences));
		mButtonXKey.setSummary(getButtonXKeySummary(sharedPreferences));
		mButtonYKey.setSummary(getButtonYKeySummary(sharedPreferences));
		mButtonSelectKey.setSummary(getButtonSelectKeySummary(sharedPreferences));
		mButtonStartKey.setSummary(getButtonStartKeySummary(sharedPreferences));
		mMediaNextKey.setSummary(getMediaNextKeySummary(sharedPreferences));
		mMediaPrevKey.setSummary(getMediaPrevKeySummary(sharedPreferences));
		mMediaPlayPauseKey.setSummary(getMediaPlayPauseKeySummary(sharedPreferences));
		mCustom01Key.setSummary(getCustom01KeySummary(sharedPreferences));
		mCustom02Key.setSummary(getCustom02KeySummary(sharedPreferences));
		mCustom03Key.setSummary(getCustom03KeySummary(sharedPreferences));
		mCustom04Key.setSummary(getCustom04KeySummary(sharedPreferences));
		mCustom05Key.setSummary(getCustom05KeySummary(sharedPreferences));
		mCustom06Key.setSummary(getCustom06KeySummary(sharedPreferences));
		mCustom07Key.setSummary(getCustom07KeySummary(sharedPreferences));
		mCustom08Key.setSummary(getCustom08KeySummary(sharedPreferences));
		mCustom09Key.setSummary(getCustom09KeySummary(sharedPreferences));
		mCustom10Key.setSummary(getCustom10KeySummary(sharedPreferences));

	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(DEF.KEY_CODE_I_BACK)){
			mBackKey.setSummary(getBackKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_VOLUME_UP)){
			mVolumeUpKey.setSummary(getVolumeUpKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_VOLUME_DOWN)){
			mVolumeDownKey.setSummary(getVolumeDownKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_CAMERA)){
			mCameraKey.setSummary(getCameraKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_FOCUS)){
			mFocusKey.setSummary(getFocusKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_MENU)){
			mMenuKey.setSummary(getMenuKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_DPAD_LEFT)){
			mDpadLeftKey.setSummary(getDpadLeftKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_DPAD_RIGHT)){
			mDpadRightKey.setSummary(getDpadRightKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_DPAD_UP)){
			mDpadUpKey.setSummary(getDpadUpKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_DPAD_DOWN)){
			mDpadDownKey.setSummary(getDpadDownKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_DPAD_CENTER)){
			mDpadCenterKey.setSummary(getDpadCenterKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_ENTER)){
			mEnterKey.setSummary(getEnterKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_DEL)){
			mDelKey.setSummary(getDelKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_SPACE)){
			mSpaceKey.setSummary(getSpaceKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_SEARCH)){
			mSearchKey.setSummary(getSearchKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_PAGE_UP)){
			mPageUpKey.setSummary(getPageUpKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_PAGE_DOWN)){
			mPageDownKey.setSummary(getPageDownKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_ESCAPE)){
			mEscapeKey.setSummary(getEscapeKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_MOVEHOME)){
			mMoveHomeKey.setSummary(getMoveHomeKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_MOVEEND)){
			mMoveEndKey.setSummary(getMoveEndKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_FORWARD)){
			mForwardKey.setSummary(getForwardKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_BUTTON_L1)){
			mButtonL1Key.setSummary(getButtonL1KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_BUTTON_L2)){
			mButtonL2Key.setSummary(getButtonL2KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_BUTTON_THUMBL)){
			mButtonL3Key.setSummary(getButtonL3KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_BUTTON_R1)){
			mButtonR1Key.setSummary(getButtonR1KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_BUTTON_R2)){
			mButtonR2Key.setSummary(getButtonR2KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_BUTTON_THUMBR)){
			mButtonR3Key.setSummary(getButtonR3KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_BUTTON_A)){
			mButtonAKey.setSummary(getButtonAKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_BUTTON_B)){
			mButtonBKey.setSummary(getButtonBKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_BUTTON_X)){
			mButtonXKey.setSummary(getButtonXKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_BUTTON_Y)){
			mButtonYKey.setSummary(getButtonYKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_BUTTON_SELECT)){
			mButtonSelectKey.setSummary(getButtonSelectKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_BUTTON_START)){
			mButtonStartKey.setSummary(getButtonStartKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_MEDIA_NEXT)){
			mMediaNextKey.setSummary(getMediaNextKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_MEDIA_PREVIOUS)){
			mMediaPrevKey.setSummary(getMediaPrevKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_MEDIA_PLAY_PAUSE)){
			mMediaPlayPauseKey.setSummary(getMediaPlayPauseKeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_CUSTOMKEY01)){
			mCustom01Key.setSummary(getCustom01KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_CUSTOMKEY02)){
			mCustom02Key.setSummary(getCustom02KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_CUSTOMKEY03)){
			mCustom03Key.setSummary(getCustom03KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_CUSTOMKEY04)){
			mCustom04Key.setSummary(getCustom04KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_CUSTOMKEY05)){
			mCustom05Key.setSummary(getCustom05KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_CUSTOMKEY06)){
			mCustom06Key.setSummary(getCustom06KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_CUSTOMKEY07)){
			mCustom07Key.setSummary(getCustom07KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_CUSTOMKEY08)){
			mCustom08Key.setSummary(getCustom08KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_CUSTOMKEY09)){
			mCustom09Key.setSummary(getCustom09KeySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CODE_I_CUSTOMKEY10)){
			mCustom10Key.setSummary(getCustom10KeySummary(sharedPreferences));
		}
	}

	public static int getBackKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_BACK, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getVolumeUpKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_VOLUME_UP, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getVolumeDownKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_VOLUME_DOWN, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getCameraKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_CAMERA, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getFocusKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_FOCUS, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getMenuKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_MENU, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getDpadLeftKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_DPAD_LEFT, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getDpadRightKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_DPAD_RIGHT, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getDpadUpKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_DPAD_UP, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getDpadDownKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_DPAD_DOWN, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getDpadCenterKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_DPAD_CENTER, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getEnterKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_ENTER, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getDelKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_DEL, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getSpaceKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_SPACE, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getSearchKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_SEARCH, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getPageUpKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_PAGE_UP, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getPageDownKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_PAGE_DOWN, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getEscapeKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_ESCAPE, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getMoveHomeKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_MOVEHOME, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getMoveEndKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_MOVEEND, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getForwardKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_FORWARD, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getButtonL1Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_BUTTON_L1, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getButtonL2Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_BUTTON_L2, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getButtonL3Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_BUTTON_THUMBL, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getButtonR1Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_BUTTON_R1, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getButtonR2Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_BUTTON_R2, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getButtonR3Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_BUTTON_THUMBR, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getButtonAKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_BUTTON_A, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getButtonBKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_BUTTON_B, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getButtonXKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_BUTTON_X, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getButtonYKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_BUTTON_Y, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getButtonSelectKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_BUTTON_SELECT, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getButtonStartKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_BUTTON_START, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getMediaNextKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_MEDIA_NEXT, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getMediaPrevKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_MEDIA_PREVIOUS, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getMediaPlayPauseKey(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_MEDIA_PLAY_PAUSE, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	public static int getCustom01Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_CUSTOMKEY01, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getCustom02Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_CUSTOMKEY02, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getCustom03Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_CUSTOMKEY03, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getCustom04Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_CUSTOMKEY04, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getCustom05Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_CUSTOMKEY05, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getCustom06Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_CUSTOMKEY06, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getCustom07Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_CUSTOMKEY07, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getCustom08Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_CUSTOMKEY08, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getCustom09Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_CUSTOMKEY09, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}
	public static int getCustom10Key(SharedPreferences sharedPreferences){
		int val = convertdata(DEF.getInt(sharedPreferences, DEF.KEY_CODE_I_CUSTOMKEY10, "0"));
		if (val < 0 || val >= mTpView.HardwareKeyName.length){
			val = 0;
		}
		return val;
	}

	private String getBackKeySummary(SharedPreferences sharedPreferences){
		int val = getBackKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getVolumeUpKeySummary(SharedPreferences sharedPreferences){
		int val = getVolumeUpKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getVolumeDownKeySummary(SharedPreferences sharedPreferences){
		int val = getVolumeDownKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getCameraKeySummary(SharedPreferences sharedPreferences){
		int val = getCameraKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getFocusKeySummary(SharedPreferences sharedPreferences){
		int val = getFocusKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getMenuKeySummary(SharedPreferences sharedPreferences){
		int val = getMenuKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getDpadLeftKeySummary(SharedPreferences sharedPreferences){
		int val = getDpadLeftKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getDpadRightKeySummary(SharedPreferences sharedPreferences){
		int val = getDpadRightKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getDpadUpKeySummary(SharedPreferences sharedPreferences){
		int val = getDpadUpKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getDpadDownKeySummary(SharedPreferences sharedPreferences){
		int val = getDpadDownKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getDpadCenterKeySummary(SharedPreferences sharedPreferences){
		int val = getDpadCenterKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getEnterKeySummary(SharedPreferences sharedPreferences){
		int val = getEnterKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getDelKeySummary(SharedPreferences sharedPreferences){
		int val = getDelKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getSpaceKeySummary(SharedPreferences sharedPreferences){
		int val = getSpaceKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getSearchKeySummary(SharedPreferences sharedPreferences){
		int val = getSearchKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getPageUpKeySummary(SharedPreferences sharedPreferences){
		int val = getPageUpKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getPageDownKeySummary(SharedPreferences sharedPreferences){
		int val = getPageDownKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getEscapeKeySummary(SharedPreferences sharedPreferences){
		int val = getEscapeKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getMoveHomeKeySummary(SharedPreferences sharedPreferences){
		int val = getMoveHomeKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getMoveEndKeySummary(SharedPreferences sharedPreferences){
		int val = getMoveEndKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getForwardKeySummary(SharedPreferences sharedPreferences){
		int val = getForwardKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getButtonL1KeySummary(SharedPreferences sharedPreferences){
		int val = getButtonL1Key(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getButtonL2KeySummary(SharedPreferences sharedPreferences){
		int val = getButtonL2Key(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getButtonL3KeySummary(SharedPreferences sharedPreferences){
		int val = getButtonL3Key(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getButtonR1KeySummary(SharedPreferences sharedPreferences){
		int val = getButtonR1Key(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getButtonR2KeySummary(SharedPreferences sharedPreferences){
		int val = getButtonR2Key(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getButtonR3KeySummary(SharedPreferences sharedPreferences){
		int val = getButtonR3Key(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getButtonAKeySummary(SharedPreferences sharedPreferences){
		int val = getButtonAKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getButtonBKeySummary(SharedPreferences sharedPreferences){
		int val = getButtonBKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getButtonXKeySummary(SharedPreferences sharedPreferences){
		int val = getButtonXKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getButtonYKeySummary(SharedPreferences sharedPreferences){
		int val = getButtonYKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getButtonSelectKeySummary(SharedPreferences sharedPreferences){
		int val = getButtonSelectKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getButtonStartKeySummary(SharedPreferences sharedPreferences){
		int val = getButtonStartKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getMediaNextKeySummary(SharedPreferences sharedPreferences){
		int val = getMediaNextKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getMediaPrevKeySummary(SharedPreferences sharedPreferences){
		int val = getMediaPrevKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}
	private String getMediaPlayPauseKeySummary(SharedPreferences sharedPreferences){
		int val = getMediaPlayPauseKey(sharedPreferences);
		Resources res = getResources();
		return res.getString(mTpView.HardwareKeyName[val]);
	}

	private String getCustom01KeySummary(SharedPreferences sharedPreferences){
		int val = getCustom01Key(sharedPreferences);
		Resources res = getResources();
		String temp = TouchPanelView.LoadCustomkeyTitle(sharedPreferences, 0);
		if (temp.equals("")) {
			temp = res.getString(R.string.nohardwarekeytitle);
		}
		temp += " : " + res.getString(mTpView.HardwareKeyName[val]);
		return temp;
	}
	private String getCustom02KeySummary(SharedPreferences sharedPreferences){
		int val = getCustom02Key(sharedPreferences);
		Resources res = getResources();
		String temp = TouchPanelView.LoadCustomkeyTitle(sharedPreferences, 1);
		if (temp.equals("")) {
			temp = res.getString(R.string.nohardwarekeytitle);
		}
		temp += " : " + res.getString(mTpView.HardwareKeyName[val]);
		return temp;
	}
	private String getCustom03KeySummary(SharedPreferences sharedPreferences){
		int val = getCustom03Key(sharedPreferences);
		Resources res = getResources();
		String temp = TouchPanelView.LoadCustomkeyTitle(sharedPreferences, 2);
		if (temp.equals("")) {
			temp = res.getString(R.string.nohardwarekeytitle);
		}
		temp += " : " + res.getString(mTpView.HardwareKeyName[val]);
		return temp;
	}
	private String getCustom04KeySummary(SharedPreferences sharedPreferences){
		int val = getCustom04Key(sharedPreferences);
		Resources res = getResources();
		String temp = TouchPanelView.LoadCustomkeyTitle(sharedPreferences, 3);
		if (temp.equals("")) {
			temp = res.getString(R.string.nohardwarekeytitle);
		}
		temp += " : " + res.getString(mTpView.HardwareKeyName[val]);
		return temp;
	}
	private String getCustom05KeySummary(SharedPreferences sharedPreferences){
		int val = getCustom05Key(sharedPreferences);
		Resources res = getResources();
		String temp = TouchPanelView.LoadCustomkeyTitle(sharedPreferences, 4);
		if (temp.equals("")) {
			temp = res.getString(R.string.nohardwarekeytitle);
		}
		temp += " : " + res.getString(mTpView.HardwareKeyName[val]);
		return temp;
	}
	private String getCustom06KeySummary(SharedPreferences sharedPreferences){
		int val = getCustom06Key(sharedPreferences);
		Resources res = getResources();
		String temp = TouchPanelView.LoadCustomkeyTitle(sharedPreferences, 5);
		if (temp.equals("")) {
			temp = res.getString(R.string.nohardwarekeytitle);
		}
		temp += " : " + res.getString(mTpView.HardwareKeyName[val]);
		return temp;
	}
	private String getCustom07KeySummary(SharedPreferences sharedPreferences){
		int val = getCustom07Key(sharedPreferences);
		Resources res = getResources();
		String temp = TouchPanelView.LoadCustomkeyTitle(sharedPreferences, 6);
		if (temp.equals("")) {
			temp = res.getString(R.string.nohardwarekeytitle);
		}
		temp += " : " + res.getString(mTpView.HardwareKeyName[val]);
		return temp;
	}
	private String getCustom08KeySummary(SharedPreferences sharedPreferences){
		int val = getCustom08Key(sharedPreferences);
		Resources res = getResources();
		String temp = TouchPanelView.LoadCustomkeyTitle(sharedPreferences, 7);
		if (temp.equals("")) {
			temp = res.getString(R.string.nohardwarekeytitle);
		}
		temp += " : " + res.getString(mTpView.HardwareKeyName[val]);
		return temp;
	}
	private String getCustom09KeySummary(SharedPreferences sharedPreferences){
		int val = getCustom09Key(sharedPreferences);
		Resources res = getResources();
		String temp = TouchPanelView.LoadCustomkeyTitle(sharedPreferences, 8);
		if (temp.equals("")) {
			temp = res.getString(R.string.nohardwarekeytitle);
		}
		temp += " : " + res.getString(mTpView.HardwareKeyName[val]);
		return temp;
	}
	private String getCustom10KeySummary(SharedPreferences sharedPreferences){
		int val = getCustom10Key(sharedPreferences);
		Resources res = getResources();
		String temp = TouchPanelView.LoadCustomkeyTitle(sharedPreferences, 9);
		if (temp.equals("")) {
			temp = res.getString(R.string.nohardwarekeytitle);
		}
		temp += " : " + res.getString(mTpView.HardwareKeyName[val]);
		return temp;
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

	public static int GetHardwareKeySetData(SharedPreferences sharedPreferences, int index) {
		int data = 0;
		switch (index) {
			case DEF.CODE_BACK:
				data = getBackKey(sharedPreferences);
				break;
			case DEF.CODE_VOLUME_UP:
				data = getVolumeUpKey(sharedPreferences);
				break;
			case DEF.CODE_VOLUME_DOWN:
				data = getVolumeDownKey(sharedPreferences);
				break;
			case DEF.CODE_CAMERA:
				data = getCameraKey(sharedPreferences);
				break;
			case DEF.CODE_FOCUS:
				data = getFocusKey(sharedPreferences);
				break;
			case DEF.CODE_MENU:
				data = getMenuKey(sharedPreferences);
				break;
			case DEF.CODE_DPAD_LEFT:
				data = getDpadLeftKey(sharedPreferences);
				break;
			case DEF.CODE_DPAD_RIGHT:
				data = getDpadRightKey(sharedPreferences);
				break;
			case DEF.CODE_DPAD_UP:
				data = getDpadUpKey(sharedPreferences);
				break;
			case DEF.CODE_DPAD_DOWN:
				data = getDpadDownKey(sharedPreferences);
				break;
			case DEF.CODE_DPAD_CENTER:
				data = getDpadCenterKey(sharedPreferences);
				break;
			case DEF.CODE_ENTER:
				data = getEnterKey(sharedPreferences);
				break;
			case DEF.CODE_DEL:
				data = getDelKey(sharedPreferences);
				break;
			case DEF.CODE_SPACE:
				data = getSpaceKey(sharedPreferences);
				break;
			case DEF.CODE_SERACH:
				data = getSearchKey(sharedPreferences);
				break;
			case DEF.CODE_PAGEUP:
				data = getPageUpKey(sharedPreferences);
				break;
			case DEF.CODE_PAGEDOWN:
				data = getPageDownKey(sharedPreferences);
				break;
			case DEF.CODE_ESCAPE:
				data = getEscapeKey(sharedPreferences);
				break;
			case DEF.CODE_MOVEHOME:
				data = getMoveHomeKey(sharedPreferences);
				break;
			case DEF.CODE_MOVEEND:
				data = getMoveEndKey(sharedPreferences);
				break;
			case DEF.CODE_FORWARD:
				data = getForwardKey(sharedPreferences);
				break;
			case DEF.CODE_BUTTON_L1:
				data = getButtonL1Key(sharedPreferences);
				break;
			case DEF.CODE_BUTTON_L2:
				data = getButtonL2Key(sharedPreferences);
				break;
			case DEF.CODE_BUTTON_THUMBL:
				data = getButtonL3Key(sharedPreferences);
				break;
			case DEF.CODE_BUTTON_R1:
				data = getButtonR1Key(sharedPreferences);
				break;
			case DEF.CODE_BUTTON_R2:
				data = getButtonR2Key(sharedPreferences);
				break;
			case DEF.CODE_BUTTON_THUMBR:
				data = getButtonR3Key(sharedPreferences);
				break;
			case DEF.CODE_BUTTON_A:
				data = getButtonAKey(sharedPreferences);
				break;
			case DEF.CODE_BUTTON_B:
				data = getButtonBKey(sharedPreferences);
				break;
			case DEF.CODE_BUTTON_X:
				data = getButtonXKey(sharedPreferences);
				break;
			case DEF.CODE_BUTTON_Y:
				data = getButtonYKey(sharedPreferences);
				break;
			case DEF.CODE_BUTTON_SELECT:
				data = getButtonSelectKey(sharedPreferences);
				break;
			case DEF.CODE_BUTTON_START:
				data = getButtonStartKey(sharedPreferences);
				break;
			case DEF.CODE_MEDIANEXT:
				data = getMediaNextKey(sharedPreferences);
				break;
			case DEF.CODE_MEDIAPREV:
				data = getMediaPrevKey(sharedPreferences);
				break;
			case DEF.CODE_MEDIAPLAYPAUSE:
				data = getMediaPlayPauseKey(sharedPreferences);
				break;
			case DEF.CODE_CUSTOMKEY01:
				data = getCustom01Key(sharedPreferences);
				break;
			case DEF.CODE_CUSTOMKEY02:
				data = getCustom02Key(sharedPreferences);
				break;
			case DEF.CODE_CUSTOMKEY03:
				data = getCustom03Key(sharedPreferences);
				break;
			case DEF.CODE_CUSTOMKEY04:
				data = getCustom04Key(sharedPreferences);
				break;
			case DEF.CODE_CUSTOMKEY05:
				data = getCustom05Key(sharedPreferences);
				break;
			case DEF.CODE_CUSTOMKEY06:
				data = getCustom06Key(sharedPreferences);
				break;
			case DEF.CODE_CUSTOMKEY07:
				data = getCustom07Key(sharedPreferences);
				break;
			case DEF.CODE_CUSTOMKEY08:
				data = getCustom08Key(sharedPreferences);
				break;
			case DEF.CODE_CUSTOMKEY09:
				data = getCustom09Key(sharedPreferences);
				break;
			case DEF.CODE_CUSTOMKEY10:
				data = getCustom10Key(sharedPreferences);
				break;
		}
		return	data;
	}

}
