package src.comitton.config;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.ListPreference;
import android.view.View;
import android.view.WindowManager;

import androidx.preference.PreferenceManager;

import src.comitton.config.SetCommonActivity;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;

public class SetServerMessageBlockActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;
	private static SharedPreferences sharedPreferences;
	private ListPreference mSelectSmbLib;

	public static final int[] SelectSmbLib =
		{ R.string.selectsmblib00
		, R.string.selectsmblib01
		, R.string.selectsmblib02 };

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

		mSelectSmbLib = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_SELECTSMBLIB);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SetCommonActivity.SetOrientationEventListenerEnable(sharedPreferences);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mSelectSmbLib.setSummary(getSelectSmbLibSummary(sharedPreferences));
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		SetCommonActivity.SetOrientationEventListenerDisable(sharedPreferences);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(DEF.KEY_SELECTSMBLIB)){
			mSelectSmbLib.setSummary(getSelectSmbLibSummary(sharedPreferences));
		}
	}

	public static boolean getSmbMode(SharedPreferences sharedPreferences){
		boolean num =  DEF.getBoolean(sharedPreferences, DEF.KEY_SMB_MODE, false);
		return num;
	}

	public static boolean getSmbRetryMode(SharedPreferences sharedPreferences){
		boolean num =  DEF.getBoolean(sharedPreferences, DEF.KEY_SMBRETRYMODE, false);
		return num;
	}

	public static int getSelectSmbLib(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_SELECTSMBLIB, "0");
		if (val < 0 || val > SelectSmbLib.length) {
			val = 0;
		}
		return val;
	}

	private String getSelectSmbLibSummary(SharedPreferences sharedPreferences){
		int val = getSelectSmbLib(sharedPreferences);
		Resources res = getResources();
		return res.getString(SelectSmbLib[val]);
	}
}
