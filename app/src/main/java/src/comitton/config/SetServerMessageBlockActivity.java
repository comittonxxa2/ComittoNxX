package src.comitton.config;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.WindowManager;

import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;

import src.comitton.config.SetCommonActivity;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.fileaccess.SmbFileAccess;

public class SetServerMessageBlockActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;
	private static SharedPreferences sharedPreferences;
	private CheckBoxPreference mSMBCallbackMode;

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
		SetCommonActivity.SetOrientationEventListener(this, sharedPreferences);

		addPreferencesFromResource(R.xml.setservermessageblock);

		mSMBCallbackMode = (CheckBoxPreference) findPreference(DEF.KEY_SMBCALLBACKMODE);

		mSMBCallbackMode.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
				// newValue には新しいチェック状態(Boolean)が入ってくる
				boolean isChecked = (Boolean) newValue;
				SmbFileAccess.setSmbAccessSwitch(isChecked);
				// trueを返すと設定値が保存される
				return true;
			}
		});

		// ListViewの位置を元に戻す
		ListViewScrollUtils.restorePosition(this, getListView());

		ButtonPreferenceCategory smbsettingCategory = (ButtonPreferenceCategory) findPreference("smbsetting_category");
		if (smbsettingCategory != null) {
			smbsettingCategory.setOnButtonClickListener(() -> {
				// 初期設定に戻す
				SharedPreferences.Editor ed = sharedPreferences.edit();
				ed.putBoolean(DEF.KEY_SMB_MODE, false);
				ed.putBoolean(DEF.KEY_SMBRETRYMODE, false);
				ed.putBoolean(DEF.KEY_SMBCALLBACKMODE, false);
				ed.apply();
				// アクティビティを再起動
				ListViewScrollUtils.restartActivityWithPosition(this, getListView());
			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		SetCommonActivity.SetOrientationEventListenerEnable(sharedPreferences);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		SetCommonActivity.SetOrientationEventListenerDisable(sharedPreferences);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	}

	public static boolean getSmbMode(SharedPreferences sharedPreferences){
		boolean num =  DEF.getBoolean(sharedPreferences, DEF.KEY_SMB_MODE, false);
		return num;
	}

	public static boolean getSmbRetryMode(SharedPreferences sharedPreferences){
		boolean num =  DEF.getBoolean(sharedPreferences, DEF.KEY_SMBRETRYMODE, false);
		return num;
	}

	public static boolean getSMBCallbackMode(SharedPreferences sharedPreferences){
		boolean num =  DEF.getBoolean(sharedPreferences, DEF.KEY_SMBCALLBACKMODE, false);
		return num;
	}
}
