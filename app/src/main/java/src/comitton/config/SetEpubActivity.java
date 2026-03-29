package src.comitton.config;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.WindowManager;

import android.preference.ListPreference;

import android.preference.CheckBoxPreference;
import androidx.preference.PreferenceManager;

import java.io.File;

import src.comitton.config.seekbar.EpubFontBodySeekbar;
import src.comitton.config.seekbar.EpubFontTextSeekbar;
import src.comitton.config.seekbar.EpubFontInfoSeekbar;
import src.comitton.config.seekbar.EpubMarginHSeekbar;
import src.comitton.config.seekbar.EpubMarginWSeekbar;
import src.comitton.config.SetCommonActivity;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;

public class SetEpubActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	Resources mResources;

//    private static final String TAG = "EPUB_DEBUG";
	private EpubFontBodySeekbar mFontBody;
	private EpubFontTextSeekbar mFontText;
	private EpubFontInfoSeekbar mFontInfo;
	private EpubMarginWSeekbar mMarginW;
	private EpubMarginHSeekbar mMarginH;
	private CheckBoxPreference mChkTextSize;
	private ListPreference mInitView;
	private ListPreference mViewRota;
	private ListPreference mFontName;
	private ListPreference mVolKey;
	private ListPreference mLastPage;
	private OperationPreference mTapPattern;
	private TimeAndBatteryPreference mTimeAndBattery;

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;
	private static SharedPreferences sharedPreferences;

	public static final int[] ViewName =
		{ R.string.selepubview01	// 単ページ表示
		, R.string.selepubview02	// 見開き表示(左右)
		, R.string.selepubview03	// 見開き表示(上下)
		, R.string.selepubview04 };	// 見開き表示(上下左右)
	public static final int[] RotateName =
		{ R.string.rota00		// 回転あり
		, R.string.rota01		// 縦固定
		, R.string.rota02		// 横固定
		, R.string.rota03		// 縦固定(90°回転)
		, R.string.rota04		// 回転あり(縦上下反転)
		, R.string.rota05		// 回転あり(横上下反転)
		, R.string.rota06		// 回転あり(縦横上下反転)
		, R.string.rota07		// 縦固定(上下反転)
		, R.string.rota08 };	// 横固定(上下反転)
	public static final int[] TimePosName =
		{ R.string.pnumpos00	// 左上
		, R.string.pnumpos01	// 中央上
		, R.string.pnumpos02	// 右上
		, R.string.pnumpos03	// 左下
		, R.string.pnumpos04	// 中央下
		, R.string.pnumpos05 };	// 右下
	public static final int[] PnumColorName =
		{ R.string.pnumcolor00		// 白
		, R.string.pnumcolor01 };		// 黒
	public static final int[] TimeFormatName =
		{ R.string.timeformat00		// 24:00
		, R.string.timeformat01		// 24:00 [100%]
		, R.string.timeformat02		// 24:00 [100%] [AC]
		, R.string.timeformat03		// 24:00
		, R.string.timeformat04		// 24:00 [100%]
		, R.string.timeformat05 };	// 24:00 [100%] [AC]

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

		addPreferencesFromResource(R.xml.epub);

		mInitView = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_EP_INITVIEW);
		mViewRota = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_EP_VIEWROTA);
		mFontText  = (EpubFontTextSeekbar)getPreferenceScreen().findPreference(DEF.KEY_EP_FONTTEXT);
		mFontBody = (EpubFontBodySeekbar)getPreferenceScreen().findPreference(DEF.KEY_EP_FONTBODY);
		mFontInfo = (EpubFontInfoSeekbar)getPreferenceScreen().findPreference(DEF.KEY_EP_FONTINFO);
		mMarginW  = (EpubMarginWSeekbar)getPreferenceScreen().findPreference(DEF.KEY_EP_MARGINW);
		mMarginH  = (EpubMarginHSeekbar)getPreferenceScreen().findPreference(DEF.KEY_EP_MARGINH);
		mChkTextSize = (CheckBoxPreference) findPreference(DEF.KEY_EP_TEXTSIZEVALIABLE);
		mFontName = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_EP_FONTNAME);
		mFontName.setTitle(mFontName.getTitle().toString().replace("[sdcard]", "[" + Environment.getExternalStorageDirectory().getAbsolutePath() + "]"));

		mTapPattern = (OperationPreference)getPreferenceScreen().findPreference(DEF.KEY_TAPPATTERN);

		mVolKey = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_VOLKEY);

		mLastPage = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_LASTPAGE);

		mTimeAndBattery = (TimeAndBatteryPreference) getPreferenceScreen().findPreference(DEF.KEY_TIMEANDBATTERY);


		if (!getTextSizeVariable(sharedPreferences)) {
			mFontText.setEnabled(false);
		}

	    mChkTextSize.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
	        @Override
	        public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
//				Log.v(TAG, "newValue=" + newValue);
	            // newValue には新しいチェック状態(Boolean)が入ってくる
	            boolean isChecked = (Boolean) newValue;
	            mFontText.setEnabled(isChecked); 
	            return true; // trueを返すと設定値が保存される
	        }
	    });

		mResources = getResources();

		String fontpath = DEF.getFontDirectory();
		CharSequence[] items;
		CharSequence[] values;
		// キャッシュ保存先
		File[] files = new File(fontpath).listFiles();
		if (files == null) {
			// ファイルなし
			items = new CharSequence[1];
			values = new CharSequence[1];
		}
		else {
			// 数える
			int i = 1;
			for (File file : files) {
				if (file != null && file.isFile()) {
					i ++;
				}
			}
			items = new CharSequence[i];
			values = new CharSequence[i];

			// 設定
			i = 1;
			for (File file : files) {
				if (file != null && file.isFile()) {
					if (i < items.length) {
						items[i] = file.getName();
						values[i] = file.getName();
						i ++;
					}
				}
			}
		}
		// リソースから読み込み
		Resources res = getResources();
		items[0] = res.getString(R.string.defaultFont);
		values[0] = "";

		mFontName.setEntries(items);
		mFontName.setEntryValues(values);
		mFontName.setDefaultValue(values[0]);

	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mInitView.setSummary(getInitViewSummary(sharedPreferences));	// 表示モード
		mViewRota.setSummary(getViewRotaSummary(sharedPreferences));	// イメージ画面の回転制御
		mFontText.setSummary(getFontTextSummary(sharedPreferences));		// フォントサイズ
		mFontBody.setSummary(getFontBodySummary(sharedPreferences));	// フォントサイズ
		mFontInfo.setSummary(getFontInfoSummary(sharedPreferences));	// フォントサイズ(px)
		mMarginW.setSummary(getMarginWSummary(sharedPreferences));		// 左右余白(px)
		mMarginH.setSummary(getMarginHSummary(sharedPreferences));		// 上下余白(px)
		mFontName.setSummary(getFontNameSummary(sharedPreferences));	// フォント名
		mTimeAndBattery.setSummary(getTimeSummary(sharedPreferences));	// 時刻と充電表示

		mTapPattern.setSummary(SetImageText.getTapPatternSummary(mResources, sharedPreferences));	// 操作パターン
		mVolKey.setSummary(SetImageText.getVolKeySummary(mResources, sharedPreferences));		// Volキー動作
		mLastPage.setSummary(SetImageText.getLastPageSummary(mResources, sharedPreferences));	// 確認メッセージ


		SetCommonActivity.SetOrientationEventListenerEnable(sharedPreferences);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		SetCommonActivity.SetOrientationEventListenerDisable(sharedPreferences);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if(key.equals(DEF.KEY_EP_INITVIEW)){
			//
			mInitView.setSummary(getInitViewSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_EP_VIEWROTA)){
			//
			mViewRota.setSummary(getViewRotaSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_EP_FONTTEXT)){
			// テキストのフォントサイズ
			mFontText.setSummary(getFontTextSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_EP_FONTBODY)){
			// テキストのフォントサイズ
			mFontBody.setSummary(getFontBodySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_EP_FONTINFO)){
			// テキストのフォントサイズ
			mFontInfo.setSummary(getFontInfoSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_EP_MARGINW)){
			// テキストのフォントサイズ
			mMarginW.setSummary(getMarginWSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_EP_MARGINH)){
			// テキストのフォントサイズ
			mMarginH.setSummary(getMarginHSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_EP_FONTNAME)){
			// フォント名
			mFontName.setSummary(getFontNameSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TAPPATTERN) || key.equals(DEF.KEY_TAPRATE)){
			// タップパターン
			mTapPattern.setSummary(SetImageText.getTapPatternSummary(mResources, sharedPreferences));
		}
		else if(key.equals(DEF.KEY_VOLKEY)){
			//
			mVolKey.setSummary(SetImageText.getVolKeySummary(mResources, sharedPreferences));
		}
		else if(key.equals(DEF.KEY_LASTPAGE)){
			//
			mLastPage.setSummary(SetImageText.getLastPageSummary(mResources, sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TIMEDISP) || key.equals(DEF.KEY_TIMEFORMAT) || key.equals(DEF.KEY_TIMEPOS) || key.equals(DEF.KEY_TIMESIZE) || key.equals(DEF.KEY_TIMECOLOR)){
			//
			mTimeAndBattery.setSummary(getTimeSummary(sharedPreferences));
		}
	}

	public static int getInitView(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_EP_INITVIEW, "0");
		return val;
	}

	public static int getViewRota(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_EP_VIEWROTA, "0");
		return val;
	}

	// 設定の読込
	public static boolean getViewer(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_VIEWER, false);
		return flag;
	}

	// 設定の読込
	public static boolean getEpubOrder(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_ORDER, true);
		return flag;
	}

	// 設定の読込
	public static boolean getEpubThumb(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_THUMB, true);
		return flag;
	}

	// 設定の読込
	public static boolean getEpubWebView(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_WEBVIEW, false);
		return flag;
	}

	public static boolean getNotice(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_NOTICE, true);
		return flag;
	}

	public static boolean getNoSleep(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_NOSLEEP, false);
		return flag;
	}

	// 設定の読込
	public static boolean getDisableTextInfo(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_DISABLETEXTINFO, false);
		return flag;
	}

	public static boolean getTextFrame(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_TEXTFRAME, false);
		return flag;
	}

	public static boolean getTextColorFix(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_TEXTCOLORFIX, false);
		return flag;
	}

	public static boolean getTextBakColorFix(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_TEXTBAKCOLORFIX, false);
		return flag;
	}

	public static boolean getTextSizeVariable(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_TEXTSIZEVALIABLE, false);
		return flag;
	}

	public static String getFontName(SharedPreferences sharedPreferences){
		return sharedPreferences.getString(DEF.KEY_EP_FONTNAME, "");
	}

	public static int getFontText(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_EP_FONTTEXT, DEF.DEFAULT_EP_FONTTEXT);
		return num;
	}

	public static int getFontBody(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_EP_FONTBODY, DEF.DEFAULT_EP_FONTBODY);
		return num;
	}

	public static int getFontInfo(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_EP_FONTINFO, DEF.DEFAULT_EP_FONTINFO);
		return num;
	}

	public static int getMarginW(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_EP_MARGINW, DEF.DEFAULT_EP_MARGINW);
		return num;
	}

	public static int getMarginH(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_EP_MARGINH, DEF.DEFAULT_EP_MARGINH);
		return num;
	}

	public static int getTimeFormat(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TIMEFORMAT, DEF.DEFAULT_TIMEFORMAT);
		if( val < 0 || val >= TimeFormatName.length){
			val = 1;
		}
		return val;
	}

	public static int getTimePos(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TIMEPOS, DEF.DEFAULT_TIMEPOS);
		if( val < 0 || val >= TimePosName.length){
			val = 5;
		}
		return val;
	}

	public static int getTimeSize(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TIMESIZE, DEF.DEFAULT_TIMESIZE);
		return val;
	}

	public static boolean getTimeDisp(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_TIMEDISP, false);
		return flag;
	}

	public static int getTimeColor(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TIMECOLOR, DEF.DEFAULT_TIMECOLOR);
		if( val < 0 || val >= PnumColorName.length){
			val = 1;
		}
		return val;
	}

	public static boolean getNoCache(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_NOCACHE, false);
		return flag;
	}

	// 設定の読込(定義変更中)
	private String getInitViewSummary(SharedPreferences sharedPreferences){
		int val = getInitView(sharedPreferences);
		Resources res = getResources();
		return res.getString(ViewName[val]);
	}

	private String getViewRotaSummary(SharedPreferences sharedPreferences){
		int val = getViewRota(sharedPreferences);
		Resources res = getResources();
		return res.getString(RotateName[val]);
	}

	private String getFontNameSummary(SharedPreferences sharedPreferences){
		String val = getFontName(sharedPreferences);
		if (val != null && val.length() > 0) {
			return val;
		}
		Resources res = getResources();
		return res.getString(R.string.defaultFont);
	}

	private String getFontTextSummary(SharedPreferences sharedPreferences){
		int val = getFontText(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.srngSumm2);

		return	DEF.getDispMarginStr(val, summ1);
	}

	private String getFontBodySummary(SharedPreferences sharedPreferences){
		int val = getFontBody(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.srngSumm2);

		return	DEF.getDispMarginStr(val, summ1);
	}

	private String getFontInfoSummary(SharedPreferences sharedPreferences){
		int val = getFontInfo(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getFontSpStr(val, summ1);
	}

	private String getMarginWSummary(SharedPreferences sharedPreferences){
		int val = getMarginW(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.rangeSumm1);

		return	DEF.getDispMarginStr(val, summ1);
	}

	private String getMarginHSummary(SharedPreferences sharedPreferences){
		int val = getMarginH(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.rangeSumm1);

		return	DEF.getDispMarginStr(val, summ1);
	}

	private String getTimeSummary(SharedPreferences sharedPreferences){
		boolean disp = getTimeDisp(sharedPreferences);
		int format = getTimeFormat(sharedPreferences);
		int pos = getTimePos(sharedPreferences);
		int size = getTimeSize(sharedPreferences);
		int color = getTimeColor(sharedPreferences);
		Resources res = getResources();

		String summ;
		if (disp) {
			summ = res.getString(TimeFormatName[format])
					+ ", " + res.getString(TimePosName[pos])
					+ ", " + DEF.getPnumSizeStr(size, res.getString(R.string.unitSumm1))
					+ ", " + res.getString(PnumColorName[color]);
		}
		else {
			summ = res.getString(R.string.pnumnodisp);
		}
		return summ;
	}
}
