package src.comitton.config;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.WindowManager;

import android.preference.ListPreference;
import android.widget.CheckBox;

import android.preference.CheckBoxPreference;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import src.comitton.config.SetCommonActivity;
import src.comitton.config.seekbar.BrightSeekbar;
import src.comitton.config.seekbar.ConsrastSeekbar;
import src.comitton.config.seekbar.GammaSeekbar;
import src.comitton.config.seekbar.HueSeekbar;
import src.comitton.config.seekbar.SaturationSeekbar;
import src.comitton.config.seekbar.SharpenSeekbar;
import src.comitton.config.seekbar.KelvinSeekbar;
import src.comitton.config.seekbar.RedLevelSeekbar;
import src.comitton.config.seekbar.GreenLevelSeekbar;
import src.comitton.config.seekbar.BlueLevelSeekbar;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.Logcat;
import src.comitton.common.DEF;
import src.comitton.dialog.ImageConfigDialog;

public class SetWebViewActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;
	private static SharedPreferences sharedPreferences;

	private ListPreference mPulldownTap;
	private SharpenSeekbar mSharpen;
	private BrightSeekbar mBright;
	private GammaSeekbar mGamma;
	private ConsrastSeekbar mConsrast;
	private HueSeekbar mHue;
	private SaturationSeekbar mSaturation;
	private KelvinSeekbar mKelvin;
	private RedLevelSeekbar mRedLevel;
	private GreenLevelSeekbar mGreenLevel;
	private BlueLevelSeekbar mBlueLevel;
	private CheckBoxPreference mChkRgbLevel;

	public static final int[] PulldownTap =
		{ R.string.pulldowntappos00
		, R.string.pulldowntappos01
		, R.string.pulldowntappos02 };

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

		addPreferencesFromResource(R.xml.webview);

		mPulldownTap = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_WEBVIEWPULLDOWNTAPPOSITION);
		mSharpen = (SharpenSeekbar)getPreferenceScreen().findPreference(DEF.KEY_WEBVIEWSHARPEN);
		mBright = (BrightSeekbar)getPreferenceScreen().findPreference(DEF.KEY_WEBVIEWBRIGHT);
		mGamma = (GammaSeekbar)getPreferenceScreen().findPreference(DEF.KEY_WEBVIEWGAMMA);
		mConsrast = (ConsrastSeekbar)getPreferenceScreen().findPreference(DEF.KEY_WEBVIEWCONTRAST);
		mHue = (HueSeekbar)getPreferenceScreen().findPreference(DEF.KEY_WEBVIEWHUE);
		mSaturation = (SaturationSeekbar)getPreferenceScreen().findPreference(DEF.KEY_WEBVIEWSATURATION);
		mKelvin = (KelvinSeekbar)getPreferenceScreen().findPreference(DEF.KEY_WEBVIEWKELVIN);
		mRedLevel = (RedLevelSeekbar)getPreferenceScreen().findPreference(DEF.KEY_WEBVIEWREDLEVEL);
		mGreenLevel = (GreenLevelSeekbar)getPreferenceScreen().findPreference(DEF.KEY_WEBVIEWGREENLEVEL);
		mBlueLevel = (BlueLevelSeekbar)getPreferenceScreen().findPreference(DEF.KEY_WEBVIEWBLUELEVEL);
		mChkRgbLevel = (CheckBoxPreference) findPreference(DEF.KEY_WEBVIEWCHECKRGBLEVEL);
		if (!getCheckRgbLevel(sharedPreferences)) {
			mRedLevel.setEnabled(false);
			mGreenLevel.setEnabled(false);
			mBlueLevel.setEnabled(false);
		}

    mChkRgbLevel.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
            // newValue には新しいチェック状態(Boolean)が入ってくる
            boolean isChecked = (Boolean) newValue;
            mRedLevel.setEnabled(isChecked); 
            mGreenLevel.setEnabled(isChecked); 
            mBlueLevel.setEnabled(isChecked); 
            return true; // trueを返すと設定値が保存される
        }
    });

	}

	@Override
	protected void onResume() {
		super.onResume();
		SetCommonActivity.SetOrientationEventListenerEnable(sharedPreferences);
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mPulldownTap.setSummary(getPulldownTapSummary(sharedPreferences));
		mSharpen.setSummary(getSharpenSummary(sharedPreferences));
		mBright.setSummary(getBrightSummary(sharedPreferences));
		mGamma.setSummary(getGammaSummary(sharedPreferences));
		mConsrast.setSummary(getConsrastSummary(sharedPreferences));
		mHue.setSummary(getHueSummary(sharedPreferences));
		mSaturation.setSummary(getSaturationSummary(sharedPreferences));
		mKelvin.setSummary(getKelvinSummary(sharedPreferences));
		mRedLevel.setSummary(getRedLevelSummary(sharedPreferences));
		mGreenLevel.setSummary(getGreenLevelSummary(sharedPreferences));
		mBlueLevel.setSummary(getBlueLevelSummary(sharedPreferences));
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		SetCommonActivity.SetOrientationEventListenerDisable(sharedPreferences);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(DEF.KEY_WEBVIEWPULLDOWNTAPPOSITION)){
			mPulldownTap.setSummary(getPulldownTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_WEBVIEWSHARPEN)){
			mSharpen.setSummary(getSharpenSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_WEBVIEWBRIGHT)){
			mBright.setSummary(getBrightSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_WEBVIEWGAMMA)){
			mGamma.setSummary(getGammaSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_WEBVIEWCONTRAST)){
			mConsrast.setSummary(getConsrastSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_WEBVIEWHUE)){
			mHue.setSummary(getHueSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_WEBVIEWSATURATION)){
			mSaturation.setSummary(getSaturationSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_WEBVIEWKELVIN)){
			mKelvin.setSummary(getKelvinSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_WEBVIEWREDLEVEL)){
			mRedLevel.setSummary(getRedLevelSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_WEBVIEWGREENLEVEL)){
			mGreenLevel.setSummary(getGreenLevelSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_WEBVIEWBLUELEVEL)){
			mBlueLevel.setSummary(getBlueLevelSummary(sharedPreferences));
		}
	}

	// 設定の読込
	public static int getPulldownTap(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_WEBVIEWPULLDOWNTAPPOSITION, "0");
		if (val < 0 || val > PulldownTap.length) {
			val = 0;
		}
		return val;
	}

	public static boolean getWebviewFilter(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_WEBVIEWFILTER, false);
		return flag;
	}

	public static boolean getWebviewPulldownMenu(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_WEBVIEWPULLDOWNMENU, false);
		return flag;
	}

	public static int getWebviewBright(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_WEBVIEWBRIGHT, 5);
		return val;
	}

	public static int getWebviewGamma(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_WEBVIEWGAMMA, 5);
		return val;
	}

	public static int getWebviewContrast(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_WEBVIEWCONTRAST, 10);
		return val;
	}

	public static int getWebviewHue(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_WEBVIEWHUE, 20);
		return val;
	}

	public static int getWebviewSaturation(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_WEBVIEWSATURATION, 20);
		return val;
	}

	public static int getWebviewSharpen(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_WEBVIEWSHARPEN, 0);
		return val;
	}

	public static boolean getWebviewGray(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_WEBVIEWGRAY, false);
		return flag;
	}

	public static boolean getWebviewColoring(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_WEBVIEWCOLORING, false);
		return flag;
	}

	public static boolean getWebviewInvert(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_WEBVIEWINVERT, false);
		return flag;
	}

	public static boolean getWebviewUserAgent(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_WEBVIEWUSEREGENT, false);
		return flag;
	}

	public static int getKelvin(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_WEBVIEWKELVIN, 35);
		return val;
	}

	public static boolean getCheckRgbLevel(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_WEBVIEWCHECKRGBLEVEL, false);
		return flag;
	}

	public static int getRedLevel(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_WEBVIEWREDLEVEL, 100);
		return val;
	}

	public static int getGreenLevel(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_WEBVIEWGREENLEVEL, 100);
		return val;
	}

	public static int getBlueLevel(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_WEBVIEWBLUELEVEL, 100);
		return val;
	}

	// 設定の読込(定義変更中)
	private String getPulldownTapSummary(SharedPreferences sharedPreferences){
		int val = getPulldownTap(sharedPreferences);
		Resources res = getResources();
		return res.getString(PulldownTap[val]);
	}

	private String getSharpenSummary(SharedPreferences sharedPreferences){
		int val = getWebviewSharpen(sharedPreferences);
		String str = ImageConfigDialog.getSharpenStr(this, val);
		return	str;
	}

	private String getBrightSummary(SharedPreferences sharedPreferences){
		int val = getWebviewBright(sharedPreferences);
		String str = ImageConfigDialog.getBrightGammaStr(this, val);
		return	str;
	}

	private String getGammaSummary(SharedPreferences sharedPreferences){
		int val = getWebviewGamma(sharedPreferences);
		String str = ImageConfigDialog.getBrightGammaStr(this, val);
		return	str;
	}

	private String getConsrastSummary(SharedPreferences sharedPreferences){
		int val = getWebviewContrast(sharedPreferences);
		String str = ImageConfigDialog.getContrastStr(val);
		return	str;
	}

	private String getHueSummary(SharedPreferences sharedPreferences){
		int val = getWebviewHue(sharedPreferences);
		String str = ImageConfigDialog.getHueStr(this, val);
		return	str;
	}

	private String getSaturationSummary(SharedPreferences sharedPreferences){
		int val = getWebviewSaturation(sharedPreferences);
		String str = ImageConfigDialog.getSaturationStr(val);
		return	str;
	}

	private String getKelvinSummary(SharedPreferences sharedPreferences){
		int val = getKelvin(sharedPreferences);
		String str = ImageConfigDialog.getKelvinStr(this, val);
		return	str;
	}

	private String getRedLevelSummary(SharedPreferences sharedPreferences){
		int val = getRedLevel(sharedPreferences);
		String str = ImageConfigDialog.getRgbLevelStr(val);
		return	str;
	}

	private String getGreenLevelSummary(SharedPreferences sharedPreferences){
		int val = getGreenLevel(sharedPreferences);
		String str = ImageConfigDialog.getRgbLevelStr(val);
		return	str;
	}

	private String getBlueLevelSummary(SharedPreferences sharedPreferences){
		int val = getBlueLevel(sharedPreferences);
		String str = ImageConfigDialog.getRgbLevelStr(val);
		return	str;
	}


}
