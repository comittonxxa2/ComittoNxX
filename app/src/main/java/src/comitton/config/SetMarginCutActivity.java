package src.comitton.config;

import src.comitton.config.seekbar.MarginLevelSeekbar;
import src.comitton.config.seekbar.MarginStartSeekbar;
import src.comitton.config.seekbar.MarginSpaceSeekbar;
import src.comitton.config.seekbar.MarginRangeSeekbar;
import src.comitton.config.seekbar.MarginLimitSeekbar;
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
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.WindowManager;

import androidx.preference.PreferenceManager;

public class SetMarginCutActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private MarginLevelSeekbar mMarginLevel;
	private MarginStartSeekbar mMarginStart;
	private MarginSpaceSeekbar mMarginSpace;
	private MarginRangeSeekbar mMarginRange;
	private MarginLimitSeekbar mMarginLimit;

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;

	Resources mResources;

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

		addPreferencesFromResource(R.xml.margincut);
		mResources = getResources();

		mMarginLevel = (MarginLevelSeekbar)getPreferenceScreen().findPreference(DEF.KEY_MarginLevel);
		mMarginSpace = (MarginSpaceSeekbar)getPreferenceScreen().findPreference(DEF.KEY_MarginSpace);
		mMarginStart = (MarginStartSeekbar)getPreferenceScreen().findPreference(DEF.KEY_MarginStart);
		mMarginRange = (MarginRangeSeekbar)getPreferenceScreen().findPreference(DEF.KEY_MarginRange);
		mMarginLimit = (MarginLimitSeekbar)getPreferenceScreen().findPreference(DEF.KEY_MarginLimit);

	}

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mMarginLevel.setSummary(getMarginLevelSummary(sharedPreferences));
		mMarginSpace.setSummary(getMarginSpaceSummary(sharedPreferences));
		mMarginStart.setSummary(getMarginStartSummary(sharedPreferences));
		mMarginRange.setSummary(getMarginRangeSummary(sharedPreferences));
		mMarginLimit.setSummary(getMarginLimitSummary(sharedPreferences));
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if(key.equals(DEF.KEY_MarginLevel)){
			mMarginLevel.setSummary(getMarginLevelSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_MarginSpace)){
			mMarginSpace.setSummary(getMarginSpaceSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_MarginStart)){
			mMarginStart.setSummary(getMarginStartSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_MarginRange)){
			mMarginRange.setSummary(getMarginRangeSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_MarginLimit)){
			mMarginLimit.setSummary(getMarginLimitSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_MarginCustomInit)){
			int num;
			num = DEF.getInt(sharedPreferences, DEF.KEY_MarginCustomInit, "0");
			if (num > 0) {
				SharedPreferences.Editor ed = sharedPreferences.edit();
				switch (num) {
					case 1:
						ed.putInt(DEF.KEY_MarginLevel, DEF.DEFAULT_MarginLevel_Min);
						ed.putInt(DEF.KEY_MarginStart, DEF.DEFAULT_MarginStart_Min);
						ed.putInt(DEF.KEY_MarginSpace, DEF.DEFAULT_MarginSpace_Min);
						ed.putInt(DEF.KEY_MarginRange, DEF.DEFAULT_MarginRange_Min);
						ed.putInt(DEF.KEY_MarginLimit, DEF.DEFAULT_MarginLimit_Min);
						break;
					case 2:
						ed.putInt(DEF.KEY_MarginLevel, DEF.DEFAULT_MarginLevel_Mid);
						ed.putInt(DEF.KEY_MarginStart, DEF.DEFAULT_MarginStart_Mid);
						ed.putInt(DEF.KEY_MarginSpace, DEF.DEFAULT_MarginSpace_Mid);
						ed.putInt(DEF.KEY_MarginRange, DEF.DEFAULT_MarginRange_Mid);
						ed.putInt(DEF.KEY_MarginLimit, DEF.DEFAULT_MarginLimit_Mid);
						break;
					case 3:
						ed.putInt(DEF.KEY_MarginLevel, DEF.DEFAULT_MarginLevel_Ultra);
						ed.putInt(DEF.KEY_MarginStart, DEF.DEFAULT_MarginStart_Ultra);
						ed.putInt(DEF.KEY_MarginSpace, DEF.DEFAULT_MarginSpace_Ultra);
						ed.putInt(DEF.KEY_MarginRange, DEF.DEFAULT_MarginRange_Ultra);
						ed.putInt(DEF.KEY_MarginLimit, DEF.DEFAULT_MarginLimit_Ultra);
						break;
					case 4:
						ed.putInt(DEF.KEY_MarginLevel, DEF.DEFAULT_MarginLevel_Strong);
						ed.putInt(DEF.KEY_MarginStart, DEF.DEFAULT_MarginStart_Strong);
						ed.putInt(DEF.KEY_MarginSpace, DEF.DEFAULT_MarginSpace_Strong);
						ed.putInt(DEF.KEY_MarginRange, DEF.DEFAULT_MarginRange_Strong);
						ed.putInt(DEF.KEY_MarginLimit, DEF.DEFAULT_MarginLimit_Strong);
						break;
					case 5:
						ed.putInt(DEF.KEY_MarginLevel, DEF.DEFAULT_MarginLevel_Overkill);
						ed.putInt(DEF.KEY_MarginStart, DEF.DEFAULT_MarginStart_Overkill);
						ed.putInt(DEF.KEY_MarginSpace, DEF.DEFAULT_MarginSpace_Overkill);
						ed.putInt(DEF.KEY_MarginRange, DEF.DEFAULT_MarginRange_Overkill);
						ed.putInt(DEF.KEY_MarginLimit, DEF.DEFAULT_MarginLimit_Overkill);
						break;
				}
				ed.remove(DEF.KEY_MarginCustomInit);
				ed.apply();
				// 設定を反映させるため再描画
				recreate();
			}
		}
	}

	// 設定の読込
	public static int getMarginLevel(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_MarginLevel, DEF.DEFAULT_MarginLevel);
		return num;
	}
	public static int getMarginStart(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_MarginStart, DEF.DEFAULT_MarginStart);
		return num;
	}
	public static int getMarginSpace(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_MarginSpace, DEF.DEFAULT_MarginSpace);
		return num;
	}
	public static int getMarginRange(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_MarginRange, DEF.DEFAULT_MarginRange);
		return num;
	}
	public static int getMarginLimit(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_MarginLimit, DEF.DEFAULT_MarginLimit);
		return num;
	}

	private String getMarginLevelSummary(SharedPreferences sharedPreferences){
		int val = getMarginLevel(sharedPreferences);
		return	String.valueOf(val);
	}

	private String getMarginSpaceSummary(SharedPreferences sharedPreferences){
		int val = getMarginSpace(sharedPreferences);
		return	String.valueOf(val) + " %";
	}

	private String getMarginStartSummary(SharedPreferences sharedPreferences){
		int val = getMarginStart(sharedPreferences);
		return	String.valueOf((float)val / 10) + " %";
	}

	private String getMarginRangeSummary(SharedPreferences sharedPreferences){
		int val = getMarginRange(sharedPreferences);
		return	String.valueOf(val) + " %";
	}

	private String getMarginLimitSummary(SharedPreferences sharedPreferences){
		int val = getMarginLimit(sharedPreferences);
		return	String.valueOf(val) + " %";
	}

	public static boolean getMarginAspectMask(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_MarginAspectMask, false);
		return flag;
	}

	public static boolean getMarginForceIgnoreAspect(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_MarginForceIgnoreAspect, false);
		return flag;
	}

	public static boolean getMargingBlackMask(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_MargingBlackMask, false);
		return flag;
	}

}
