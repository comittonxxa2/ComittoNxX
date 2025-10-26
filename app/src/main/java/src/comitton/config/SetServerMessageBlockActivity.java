package src.comitton.config;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		mNotice = SetCommonActivity.getForceHideStatusBar(sharedPreferences);
		if (mNotice) {
			// �ʒm�̈��\��
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		SetCommonActivity.SetOrientationEventListenerEnable();
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		SetCommonActivity.SetOrientationEventListenerDisable();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	}

	public static boolean getSmbMode(SharedPreferences sharedPreferences){
		boolean num =  DEF.getBoolean(sharedPreferences, DEF.KEY_SMB_MODE, false);
		return num;
	}
}
