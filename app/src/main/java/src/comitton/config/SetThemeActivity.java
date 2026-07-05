package src.comitton.config;

import src.comitton.common.MultiProcessPreferences;
import src.comitton.config.seekbar.FontMainSeekbar;
import src.comitton.config.seekbar.FontSubSeekbar;
import src.comitton.config.seekbar.FontTileSeekbar;
import src.comitton.config.seekbar.FontTitleSeekbar;
import src.comitton.config.seekbar.ItemMarginSeekbar;
import src.comitton.config.seekbar.ToolbarSeekbar;
import src.comitton.config.color.ColorAftSetting;
import src.comitton.config.color.ColorBakSetting;
import src.comitton.config.color.ColorBefSetting;
import src.comitton.config.color.ColorBseSetting;
import src.comitton.config.color.ColorCurSetting;
import src.comitton.config.color.ColorDirSetting;
import src.comitton.config.color.ColorImgSetting;
import src.comitton.config.color.ColorInfSetting;
import src.comitton.config.color.ColorMrkSetting;
import src.comitton.config.color.ColorNowSetting;
import src.comitton.config.color.ColorRrbSetting;
import src.comitton.config.color.ColorTibSetting;
import src.comitton.config.color.ColorTitSetting;
import src.comitton.config.color.ColorTlbSetting;
import src.comitton.config.color.ColorTldSetting;
import src.comitton.config.color.ColorTxtSetting;
import src.comitton.config.color.ColorBsfSetting;
import src.comitton.config.color.ColorFifSetting;
import src.comitton.config.color.ColorFibSetting;
import src.comitton.config.color.ColorEvtSetting;
import src.comitton.config.color.ColorEvbSetting;
import src.comitton.config.SetCommonActivity;
import src.comitton.fileview.FileSelectActivity;
import src.comitton.helpview.HelpActivity;
import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import jp.dip.muracoro.comittonx.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

import android.preference.CheckBoxPreference;
import androidx.preference.PreferenceManager;

public class SetThemeActivity extends BasePreferenceActivity implements OnSharedPreferenceChangeListener {
	private ListPreference mPreset;

	private PreferenceScreen mCustomUpdate;

	private FontTitleSeekbar mFontTitle;
	private FontMainSeekbar mFontMain;
	private FontSubSeekbar mFontSub;
	private FontTileSeekbar mFontTile;
	private ItemMarginSeekbar mItemMrgn;
	private ToolbarSeekbar mToolbarSeek;

	private ColorTxtSetting mTxtColor;
	private ColorDirSetting mDirColor;
	private ColorBefSetting mBefColor;
	private ColorNowSetting mNowColor;
	private ColorAftSetting mAftColor;
	private ColorRrbSetting mRrbColor;
	private ColorImgSetting mImgColor;
	private ColorInfSetting mInfColor;
	private ColorMrkSetting mMrkColor;
	private ColorBakSetting mBakColor;
	private ColorCurSetting mCurColor;
	private ColorBsfSetting mBsfColor;
	private ColorBseSetting mBseColor;
	private ColorFifSetting mFifColor;
	private ColorFibSetting mFibColor;
	private ColorEvtSetting mEvtColor;
	private ColorEvbSetting mEvbColor;

	private ColorTitSetting mTitColor;
	private ColorTibSetting mTibColor;
	private ColorTldSetting mTldColor;	// ツールバーの描画
	private ColorTlbSetting mTlbColor;	// ツールバーの背景

	private CheckBoxPreference mEnableTheme;
	private ListPreference mThemePreset;

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;
	private int mOldTheme;

	SharedPreferences mSharedPreferences;

	static final int[] mPresetName =
	{ R.string.preset00		// カスタム
	, R.string.preset01		// 標準（黒）
	, R.string.preset02		// 標準（白）
	, R.string.preset03		// 桜
	, R.string.preset04		// 藍
	, R.string.preset05		// 若葉
	, R.string.preset06		// 蜜柑
	, R.string.preset07 };	// 墨

	static final int PRESET_TXT = 0;
	static final int PRESET_DIR = 1;
	static final int PRESET_BEF = 2;
	static final int PRESET_NOW = 3;
	static final int PRESET_AFT = 4;
	static final int PRESET_IMG = 5;
	static final int PRESET_INF = 6;
	static final int PRESET_MRK = 7;
	static final int PRESET_BAK = 8;
	static final int PRESET_CUR = 9;
	static final int PRESET_TIT = 10;
	static final int PRESET_TIB = 11;
	static final int PRESET_TLD = 12;
	static final int PRESET_TLB = 13;
	static final int PRESET_RRB = 14;
	static final int PRESET_BSF = 15;
	static final int PRESET_BSE = 16;
	static final int PRESET_FIF = 17;
	static final int PRESET_FIB = 18;
	static final int PRESET_EVT = 19;
	static final int PRESET_EVB = 20;

	static final int[][] mPresetColor =
	//   ----TXT----, ---DIR----, ---BEF----, ---NOW----, ---AFT----, ---IMG----, ---INF----, ---MRK----, ---BAK----, ---Cur----, ---TIT----, ---TIB----, ---TLD----, ---TLB----, ---RRB----, ---BSF---, ---BSE---, ---FIF---, ---FIB---, ---EVT---, ---EVB---
	{
/*標*/	{ 0xFFFFFFFF, 0xFF00FF00, 0xFFFFFFFF, 0xFF00FFFF, 0xFF808080, 0xFFFFFF00, 0xFF9F9F9F, 0xFFFFFF00, 0xFF000000, 0xFF0080FF, 0xFFFFFFFF, 0xFF202020, 0xFF000000, 0xFF808080, 0xFF888888, 0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF, 0xFF000000, 0xFF000000, 0xFFE0E0E0 },
/*黒*/ 	{ 0xFFFFFFFF, 0xFF00FF00, 0xFFFFFFFF, 0xFF00FFFF, 0xFF808080, 0xFFFFFF00, 0xFF9F9F9F, 0xFFC00000, 0xFF000000, 0xFF0040C0, 0xFFFFFFFF, 0xFF202020, 0xFF404040, 0xFFA0A0A0, 0xFF888888, 0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF, 0xFF000000, 0xFF000000, 0xFFE0E0E0 },
/*白*/	{ 0xFF000000, 0xFF008039, 0xFF000000, 0xFF1C5593, 0xFF808080, 0xFFB17A25, 0xFF3B373A, 0xFFFFFF40, 0xFFF0F0F0, 0xFF00C0FF, 0xFF232323, 0xFF8A8A8A, 0xFFD0D0D0, 0xFF606060, 0xFF888888, 0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF, 0xFF000000, 0xFF000000, 0xFFE0E0E0 },
/*桜*/	{ 0xFFDF1E6B, 0xFF5C9A00, 0xFFDF1E6B, 0xFFE99697, 0xFF968E8F, 0xFF8A67A7, 0xFFA3B0C1, 0xFFFFFF7E, 0xFFFFE5E9, 0xFF89CFFF, 0xFFFFEDF1, 0xFFC77E90, 0xFF64263A, 0xFFE7AABC, 0xFF888888, 0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF, 0xFF000000, 0xFF000000, 0xFFE0E0E0 },
/*藍*/	{ 0xFF00217A, 0xFF2D5155, 0xFF00237B, 0xFFD93D4A, 0xFF968E8F, 0xFF4B418C, 0xFF393FD3, 0xFFF1FF82, 0xFFE9EFFF, 0xFFF5B5CE, 0xFFFAF2FF, 0xFF000B4B, 0xFF111656, 0xFF889EB0, 0xFF888888, 0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF, 0xFF000000, 0xFF000000, 0xFFE0E0E0 },
/*葉*/	{ 0xFF10832A, 0xFF354E83, 0xFF10832A, 0xFF16A686, 0xFF82A18A, 0xFF92B410, 0xFF228034, 0xFFFDFB9A, 0xFFF2FFF5, 0xFFA2DAFF, 0xFFFAFFF2, 0xFF165826, 0xFF153A10, 0xFFA2B99F, 0xFF888888, 0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF, 0xFF000000, 0xFF000000, 0xFFE0E0E0 }, // O
/*橙*/	{ 0xFF8E6216, 0xFFA98A34, 0xFF8E6216, 0xFFCC9E43, 0xFFA7966D, 0xFFB07028, 0xFF88661B, 0xFFFFCEE3, 0xFFFFFAF2, 0xFF8DFFBB, 0xFFFFFEF3, 0xFFC36214, 0xFF663B10, 0xFFDDA268, 0xFF888888, 0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF, 0xFF000000, 0xFF000000, 0xFFE0E0E0 }, // O
/*墨*/	{ 0xFFDEDEE1, 0xFF2DA6C8, 0xFFDEDED1, 0xFF76AFC3, 0xFF4A4A4B, 0xFFC5C123, 0xFF9F9F9F, 0xFF004D24, 0xFF282828, 0xFF1F4594, 0xFFFFFFFF, 0xFF202020, 0xFF3A3A3F, 0xFF6F767C, 0xFF888888, 0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF, 0xFF000000, 0xFF000000, 0xFFE0E0E0 }
	};

	static final int[] mThemePresetName =
	{ R.string.themepreset00	// システム設定
	, R.string.themepreset01	// テーマ1
	, R.string.themepreset02	// テーマ2
	, R.string.themepreset03	// テーマ3
	, R.string.themepreset04	// テーマ4
	, R.string.themepreset05	// テーマ5
	, R.string.themepreset06	// テーマ6
	, R.string.themepreset07	// テーマ7
	, R.string.themepreset08 };	// テーマ8

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences sharedPreferences = MultiProcessPreferences.getInstance(this);


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

		addPreferencesFromResource(R.xml.themecolor);

		mPreset = (ListPreference) getPreferenceScreen().findPreference(DEF.KEY_PRESET);
		mFontTitle = (FontTitleSeekbar)getPreferenceScreen().findPreference(DEF.KEY_FONTTITLE);
		mFontMain  = (FontMainSeekbar)getPreferenceScreen().findPreference(DEF.KEY_FONTMAIN);
		mFontSub   = (FontSubSeekbar)getPreferenceScreen().findPreference(DEF.KEY_FONTSUB);
		mFontTile  = (FontTileSeekbar)getPreferenceScreen().findPreference(DEF.KEY_FONTTILE);
		mItemMrgn  = (ItemMarginSeekbar)getPreferenceScreen().findPreference(DEF.KEY_ITEMMRGN);
		mToolbarSeek = (ToolbarSeekbar)getPreferenceScreen().findPreference(DEF.KEY_TOOLBARSEEK);

		mTxtColor = (ColorTxtSetting) getPreferenceScreen().findPreference(DEF.KEY_TXTRGB);
		mDirColor = (ColorDirSetting) getPreferenceScreen().findPreference(DEF.KEY_DIRRGB);
		mBefColor = (ColorBefSetting) getPreferenceScreen().findPreference(DEF.KEY_BEFRGB);
		mNowColor = (ColorNowSetting) getPreferenceScreen().findPreference(DEF.KEY_NOWRGB);
		mAftColor = (ColorAftSetting) getPreferenceScreen().findPreference(DEF.KEY_AFTRGB);
		mRrbColor = (ColorRrbSetting) getPreferenceScreen().findPreference(DEF.KEY_RRBRGB);
		mImgColor = (ColorImgSetting) getPreferenceScreen().findPreference(DEF.KEY_IMGRGB);
		mInfColor = (ColorInfSetting) getPreferenceScreen().findPreference(DEF.KEY_INFRGB);
		mMrkColor = (ColorMrkSetting) getPreferenceScreen().findPreference(DEF.KEY_MRKRGB);
		mBakColor = (ColorBakSetting) getPreferenceScreen().findPreference(DEF.KEY_BAKRGB);
		mCurColor = (ColorCurSetting) getPreferenceScreen().findPreference(DEF.KEY_CURRGB);
		mBsfColor = (ColorBsfSetting) getPreferenceScreen().findPreference(DEF.KEY_BSFRGB);
		mBseColor = (ColorBseSetting) getPreferenceScreen().findPreference(DEF.KEY_BSERGB);
		mFifColor = (ColorFifSetting) getPreferenceScreen().findPreference(DEF.KEY_FIFRGB);
		mFibColor = (ColorFibSetting) getPreferenceScreen().findPreference(DEF.KEY_FIBRGB);

		mTitColor = (ColorTitSetting) getPreferenceScreen().findPreference(DEF.KEY_TITRGB);
		mTibColor = (ColorTibSetting) getPreferenceScreen().findPreference(DEF.KEY_TIBRGB);
		mTldColor = (ColorTldSetting) getPreferenceScreen().findPreference(DEF.KEY_TLDRGB);
		mTlbColor = (ColorTlbSetting) getPreferenceScreen().findPreference(DEF.KEY_TLBRGB);
		mEvtColor = (ColorEvtSetting) getPreferenceScreen().findPreference(DEF.KEY_EVTRGB);
		mEvbColor = (ColorEvbSetting) getPreferenceScreen().findPreference(DEF.KEY_EVBRGB);

		mThemePreset = (ListPreference) getPreferenceScreen().findPreference(DEF.KEY_THEME_PRESET);

		mEnableTheme = (CheckBoxPreference) findPreference(DEF.KEY_ENABLE_THEME);
		if (!getCheckEnableTheme(sharedPreferences)) {
			mThemePreset.setEnabled(false);
		}
		mEnableTheme.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
				boolean isChecked = (Boolean) newValue;
				mThemePreset.setEnabled(isChecked);
				// チェックの有無で処理を変える
				if (!isChecked) {
					// チェックが無しの場合
					// 現在のプリセットを保存
					int val = getThemePreset(sharedPreferences);
					Editor ed = sharedPreferences.edit();
					ed.putInt(DEF.KEY_THEME_OLD_PRESET, val);
					ed.apply();
					bakPresetTheme(sharedPreferences, val);
					// システム設定の値にする
					val = 0;
					// 退避していたシステム設定を読み出して現在の設定へ上書き
					chgPresetTheme(sharedPreferences, val);
					// 変更したプリセットを更新
					mOldTheme = val;
					// リロード
					ReloadTheme(sharedPreferences);
				}
				else {
					// チェックがありの場合
					// 現在のシステム設定を上書き保存
					bakPresetTheme(sharedPreferences, 0);
					// プリセットを取り出す
					int val = DEF.getInt(sharedPreferences, DEF.KEY_THEME_OLD_PRESET, 0);
					mThemePreset.setSummary(getThemePresetSummary(val));
					// 退避していたプリセットを読み出して現在の設定へ上書き
					chgPresetTheme(sharedPreferences, val);
					// 変更したプリセットを更新
					mOldTheme = val;
					// リロード
					ReloadTheme(sharedPreferences);
				}
				// trueを返すと設定値が保存される
				return true;
			}
		});

		mCustomUpdate = (PreferenceScreen) findPreference(DEF.KEY_CSTUPDATE);
		mCustomUpdate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// プリセットを反映
				int index = getPreset(mSharedPreferences);
				if (index > 0 || index < mPresetColor.length) {
					String[] newKeys = { DEF.KEY_TXTRGB, DEF.KEY_DIRRGB, DEF.KEY_BEFRGB, DEF.KEY_NOWRGB, DEF.KEY_AFTRGB, DEF.KEY_IMGRGB, DEF.KEY_INFRGB, DEF.KEY_MRKRGB, DEF.KEY_BAKRGB, DEF.KEY_CURRGB, DEF.KEY_TITRGB, DEF.KEY_TIBRGB, DEF.KEY_TLDRGB, DEF.KEY_TLBRGB, DEF.KEY_RRBRGB, DEF.KEY_BSFRGB, DEF.KEY_BSERGB, DEF.KEY_FIFRGB ,DEF.KEY_FIBRGB ,DEF.KEY_EVTRGB ,DEF.KEY_EVBRGB };

					Editor ed = mSharedPreferences.edit();
					for (int i = 0 ; i < newKeys.length ; i ++) {
						ed.putInt(newKeys[i], mPresetColor[index][i]);
					}
					ed.apply();
					updateSummarys();
				}
				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSharedPreferences = getPreferenceScreen().getSharedPreferences();
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

		SetCommonActivity.SetOrientationEventListenerEnable(mSharedPreferences);
		// 色設定
		int val = getPreset(mSharedPreferences);
		mPreset.setSummary(getPresetSummary(val)); // プリセット
		mPreset.setValueIndex(val);
		setEnableViews(val == 0);

		mFontTitle.setSummary(getFontTitleSummary(mSharedPreferences));	// フォントサイズ(px)
		mFontMain.setSummary(getFontMainSummary(mSharedPreferences));	// フォントサイズ(px)
		mFontSub.setSummary(getFontSubSummary(mSharedPreferences));		// フォントサイズ(px)
		mFontTile.setSummary(getFontTileSummary(mSharedPreferences));	// フォントサイズ(px)
		mItemMrgn.setSummary(getItemMarginSummary(mSharedPreferences));	// 余白サイズ
		mToolbarSeek.setSummary(getToolbarSeekSummary(mSharedPreferences));		// ツールバー表示

		val = getThemePreset(mSharedPreferences);
		mThemePreset.setSummary(getThemePresetSummary(val)); // プリセット
		mOldTheme = val;

		// テーマの初期設定
		InitPresetTheme(mSharedPreferences);

		updateSummarys();
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		mSharedPreferences = getPreferenceScreen().getSharedPreferences();
		SetCommonActivity.SetOrientationEventListenerDisable(mSharedPreferences);

	}
	// リロード
	private void ReloadTheme(SharedPreferences sp) {
		int val = getPreset(sp);
		mPreset.setSummary(getPresetSummary(val)); // プリセット
		mPreset.setValueIndex(val);
		mFontTitle.setSummary(getFontTitleSummary(sp));
		mFontMain.setSummary(getFontMainSummary(sp));
		mFontSub.setSummary(getFontSubSummary(sp));
		mFontTile.setSummary(getFontTileSummary(sp));
		mItemMrgn.setSummary(getItemMarginSummary(sp));
		mToolbarSeek.setSummary(getToolbarSeekSummary(sp));
		updateSummarys();
	}
	// テーマの初期設定
	private void InitPresetTheme(SharedPreferences sp) {
		int mThemePresets = DEF.getInt(sp, DEF.KEY_THEMES_PRESET, "-1");
		int mThemePreset1 = DEF.getInt(sp, DEF.KEY_THEME1_PRESET, "-1");
		int mThemePreset2 = DEF.getInt(sp, DEF.KEY_THEME2_PRESET, "-1");
		int mThemePreset3 = DEF.getInt(sp, DEF.KEY_THEME3_PRESET, "-1");
		int mThemePreset4 = DEF.getInt(sp, DEF.KEY_THEME4_PRESET, "-1");
		int mThemePreset5 = DEF.getInt(sp, DEF.KEY_THEME5_PRESET, "-1");
		int mThemePreset6 = DEF.getInt(sp, DEF.KEY_THEME6_PRESET, "-1");
		int mThemePreset7 = DEF.getInt(sp, DEF.KEY_THEME7_PRESET, "-1");
		int mThemePreset8 = DEF.getInt(sp, DEF.KEY_THEME8_PRESET, "-1");
		if (mThemePresets == -1) {
			// 未定義の場合は現在の設定を上書き
			setThemeS(sp);
		}
		if (mThemePreset1 == -1) {
			// 未定義の場合は現在の設定を上書き
			setTheme1(sp);
		}
		if (mThemePreset2 == -1) {
			// 未定義の場合は現在の設定を上書き
			setTheme2(sp);
		}
		if (mThemePreset3 == -1) {
			// 未定義の場合は現在の設定を上書き
			setTheme3(sp);
		}
		if (mThemePreset4 == -1) {
			// 未定義の場合は現在の設定を上書き
			setTheme4(sp);
		}
		if (mThemePreset5 == -1) {
			// 未定義の場合は現在の設定を上書き
			setTheme5(sp);
		}
		if (mThemePreset6 == -1) {
			// 未定義の場合は現在の設定を上書き
			setTheme6(sp);
		}
		if (mThemePreset7 == -1) {
			// 未定義の場合は現在の設定を上書き
			setTheme7(sp);
		}
		if (mThemePreset8 == -1) {
			// 未定義の場合は現在の設定を上書き
			setTheme8(sp);
		}
	}
	// 現在の設定を上書き
	private void bakPresetTheme(SharedPreferences sp, int index) {
		switch (index) {
			case 0:
				setThemeS(sp);
				break;
			case 1:
				setTheme1(sp);
				break;
			case 2:
				setTheme2(sp);
				break;
			case 3:
				setTheme3(sp);
				break;
			case 4:
				setTheme4(sp);
				break;
			case 5:
				setTheme5(sp);
				break;
			case 6:
				setTheme6(sp);
				break;
			case 7:
				setTheme7(sp);
				break;
			case 8:
				setTheme8(sp);
				break;
		}
	}
	// 退避していたプリセットを読み出して現在の設定へ上書き
	private void chgPresetTheme(SharedPreferences sp, int index) {
		switch (index) {
			case 0:
				getThemeS(sp);
				break;
			case 1:
				getTheme1(sp);
				break;
			case 2:
				getTheme2(sp);
				break;
			case 3:
				getTheme3(sp);
				break;
			case 4:
				getTheme4(sp);
				break;
			case 5:
				getTheme5(sp);
				break;
			case 6:
				getTheme6(sp);
				break;
			case 7:
				getTheme7(sp);
				break;
			case 8:
				getTheme8(sp);
				break;
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		boolean change = false;
		if(key.equals(DEF.KEY_FONTTITLE)){
			// テキストのフォントサイズ
			mFontTitle.setSummary(getFontTitleSummary(sharedPreferences));
			change = true;
		}
		else if(key.equals(DEF.KEY_FONTMAIN)){
			// テキストのフォントサイズ
			mFontMain.setSummary(getFontMainSummary(sharedPreferences));
			change = true;
		}
		else if(key.equals(DEF.KEY_FONTSUB)){
			// サマリのフォントサイズ
			mFontSub.setSummary(getFontSubSummary(sharedPreferences));
			change = true;
		}
		else if(key.equals(DEF.KEY_FONTTILE)){
			// テキストのフォントサイズ
			mFontTile.setSummary(getFontTileSummary(sharedPreferences));
			change = true;
		}
		else if(key.equals(DEF.KEY_ITEMMRGN)){
			// 余白サイズ
			mItemMrgn.setSummary(getItemMarginSummary(sharedPreferences));
			change = true;
		}
		else if(key.equals(DEF.KEY_TOOLBARSEEK)){
			// ツールバーのアイコンのサイズ
			mToolbarSeek.setSummary(getToolbarSeekSummary(sharedPreferences));
			change = true;
		}
		// 色
		else if (key.equals(DEF.KEY_PRESET)) {
			// プリセット選択
			int val = getPreset(sharedPreferences);
			mPreset.setSummary(getPresetSummary(val));
			mPreset.setValueIndex(val);
			setEnableViews(val == 0);
			change = true;
		}
		else if (key.equals(DEF.KEY_THEME_PRESET)) {
			// プリセット選択
			int val = getThemePreset(sharedPreferences);
			mThemePreset.setSummary(getThemePresetSummary(val));
			// 現在の設定を上書き
			bakPresetTheme(sharedPreferences, mOldTheme);
			// 退避していたプリセットを読み出して現在の設定へ上書き
			chgPresetTheme(sharedPreferences, val);
			// 変更したプリセットを更新
			mOldTheme = val;
			// リロード
			ReloadTheme(sharedPreferences);
			change = true;
		}
		else if (key.equals(DEF.KEY_TXTRGB)) {
			//
			mTxtColor.setSummary(getColorSummary(getTxtColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_DIRRGB)) {
			//
			mDirColor.setSummary(getColorSummary(getDirColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_BEFRGB)) {
			//
			mBefColor.setSummary(getColorSummary(getBefColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_NOWRGB)) {
			//
			mNowColor.setSummary(getColorSummary(getNowColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_AFTRGB)) {
			//
			mAftColor.setSummary(getColorSummary(getAftColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_RRBRGB)) {
			//
			mRrbColor.setSummary(getColorSummary(getRrbColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_IMGRGB)) {
			//
			mImgColor.setSummary(getColorSummary(getImgColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_INFRGB)) {
			//
			mInfColor.setSummary(getColorSummary(getInfColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_MRKRGB)) {
			//
			mMrkColor.setSummary(getColorSummary(getMrkColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_BAKRGB)) {
			//
			mBakColor.setSummary(getColorSummary(getBakColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_CURRGB)) {
			//
			mCurColor.setSummary(getColorSummary(getCurColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_TITRGB)) {
			//
			mTitColor.setSummary(getColorSummary(getTitColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_TIBRGB)) {
			//
			mTibColor.setSummary(getColorSummary(getTibColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_TLDRGB)) {
			//
			mTldColor.setSummary(getColorSummary(getTldColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_TLBRGB)) {
			//
			mTlbColor.setSummary(getColorSummary(getTlbColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_BSFRGB)) {
			//
			mBsfColor.setSummary(getColorSummary(getBsfColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_BSERGB)) {
			//
			mBseColor.setSummary(getColorSummary(getBseColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_FIFRGB)) {
			//
			mFifColor.setSummary(getColorSummary(getFifColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_FIBRGB)) {
			//
			mFibColor.setSummary(getColorSummary(getFibColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_EVTRGB)) {
			//
			mEvtColor.setSummary(getColorSummary(getEvtColor(sharedPreferences, true)));
			change = true;
		}
		else if (key.equals(DEF.KEY_EVBRGB)) {
			//
			mEvbColor.setSummary(getColorSummary(getEvbColor(sharedPreferences, true)));
			change = true;
		}
		if (change) {
			// 親のActivityを再生成させる
			FileSelectActivity.setChangeTheme();
		}
	}

	private void setEnableViews(boolean enable) {
		mCustomUpdate.setEnabled(!enable);
		mTxtColor.setEnabled(enable);
		mDirColor.setEnabled(enable);
		mBefColor.setEnabled(enable);
		mNowColor.setEnabled(enable);
		mAftColor.setEnabled(enable);
		mRrbColor.setEnabled(enable);
		mImgColor.setEnabled(enable);
		mInfColor.setEnabled(enable);
		mMrkColor.setEnabled(enable);
		mBakColor.setEnabled(enable);
		mCurColor.setEnabled(enable);
		mTitColor.setEnabled(enable);
		mTibColor.setEnabled(enable);
		mTldColor.setEnabled(enable);
		mTlbColor.setEnabled(enable);
		mBsfColor.setEnabled(enable);
		mBseColor.setEnabled(enable);
		mFifColor.setEnabled(enable);
		mFibColor.setEnabled(enable);
		mEvtColor.setEnabled(enable);
		mEvbColor.setEnabled(enable);
	}

	private void updateSummarys() {
		mTxtColor.setSummary(getColorSummary(getTxtColor(mSharedPreferences, true))); // サーバ名
		mDirColor.setSummary(getColorSummary(getDirColor(mSharedPreferences, true))); // ディレクトリ
		mBefColor.setSummary(getColorSummary(getBefColor(mSharedPreferences, true))); // 未読
		mNowColor.setSummary(getColorSummary(getNowColor(mSharedPreferences, true))); // 読中
		mAftColor.setSummary(getColorSummary(getAftColor(mSharedPreferences, true))); // 既読
		mRrbColor.setSummary(getColorSummary(getRrbColor(mSharedPreferences, true))); // 読書率背景色
		mImgColor.setSummary(getColorSummary(getImgColor(mSharedPreferences, true))); // 画像
		mInfColor.setSummary(getColorSummary(getInfColor(mSharedPreferences, true))); // ファイル情報
		mMrkColor.setSummary(getColorSummary(getMrkColor(mSharedPreferences, true))); // マーカー
		mBakColor.setSummary(getColorSummary(getBakColor(mSharedPreferences, true))); // 背景
		mCurColor.setSummary(getColorSummary(getCurColor(mSharedPreferences, true))); // カーソル
		mBsfColor.setSummary(getColorSummary(getBsfColor(mSharedPreferences, true))); // 本棚の文字色
		mBseColor.setSummary(getColorSummary(getBseColor(mSharedPreferences, true))); // 本棚の文字の縁取りの色
		mFifColor.setSummary(getColorSummary(getFifColor(mSharedPreferences, true))); // フローティングアイコンの前景色
		mFibColor.setSummary(getColorSummary(getFibColor(mSharedPreferences, true))); // フローティングアイコンの背景色

		mTitColor.setSummary(getColorSummary(getTitColor(mSharedPreferences, true))); // タイトルテキスト
		mTibColor.setSummary(getColorSummary(getTibColor(mSharedPreferences, true))); // タイトル背景
		mTldColor.setSummary(getColorSummary(getTldColor(mSharedPreferences, true))); // ツールバー描画
		mTlbColor.setSummary(getColorSummary(getTlbColor(mSharedPreferences, true))); // ツールバー背景
		mEvtColor.setSummary(getColorSummary(getEvtColor(mSharedPreferences, true))); // EPUBの文字色
		mEvbColor.setSummary(getColorSummary(getEvbColor(mSharedPreferences, true))); // EPUBの背景
	}

	// 設定の読込（リストビュー）
	public static int getPreset(SharedPreferences sp) {
		int val = DEF.getInt(sp, DEF.KEY_PRESET, "1");
		return val;
	}

	public static int getThemePreset(SharedPreferences sp) {
		int val = DEF.getInt(sp, DEF.KEY_THEME_PRESET, "0");
		return val;
	}

	// 設定の読込（Activityからのアクセス）
	public static int getFontTitle(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_FONTTITLE, DEF.DEFAULT_FONTTITLE);
		return num;
	}

	public static int getFontMain(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_FONTMAIN, DEF.DEFAULT_FONTMAIN);
		return num;
	}

	public static int getFontSub(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_FONTSUB, DEF.DEFAULT_FONTSUB);
		return num;
	}

	public static int getFontTile(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_FONTTILE, DEF.DEFAULT_FONTTILE);
		return num;
	}

	public static int getItemMargin(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_ITEMMRGN, DEF.DEFAULT_ITEMMARGIN);
		return num;
	}

	public static int getToolbarSize(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TOOLBARSEEK, DEF.DEFAULT_TOOLBARSEEK);
		return val;
	}

	public static int getTxtColor(SharedPreferences sp) {
		return getTxtColor(sp, false);
	}

	public static int getDirColor(SharedPreferences sp) {
		return getDirColor(sp, false);
	}

	public static int getBefColor(SharedPreferences sp) {
		return getBefColor(sp, false);
	}

	public static int getNowColor(SharedPreferences sp) {
		return getNowColor(sp, false);
	}

	public static int getAftColor(SharedPreferences sp) {
		return getAftColor(sp, false);
	}

	public static int getRrbColor(SharedPreferences sp) {
		return getRrbColor(sp, false);
	}

	public static int getImgColor(SharedPreferences sp) {
		return getImgColor(sp, false);
	}

	public static int getInfColor(SharedPreferences sp) {
		return getInfColor(sp, false);
	}

	public static int getMrkColor(SharedPreferences sp) {
		return getMrkColor(sp, false);
	}

	public static int getBakColor(SharedPreferences sp) {
		return getBakColor(sp, false);
	}

	public static int getCurColor(SharedPreferences sp) {
		return getCurColor(sp, false);
	}

	public static int getTitColor(SharedPreferences sp) {
		return getTitColor(sp, false);
	}

	public static int getTibColor(SharedPreferences sp) {
		return getTibColor(sp, false);
	}

	public static int getTldColor(SharedPreferences sp) {
		return getTldColor(sp, false);
	}

	public static int getTlbColor(SharedPreferences sp) {
		return getTlbColor(sp, false);
	}

	public static int getBsfColor(SharedPreferences sp) {
		return getBsfColor(sp, false);
	}

	public static int getBseColor(SharedPreferences sp) {
		return getBseColor(sp, false);
	}

	public static int getFifColor(SharedPreferences sp) {
		return getFifColor(sp, false);
	}

	public static int getFibColor(SharedPreferences sp) {
		return getFibColor(sp, false);
	}

	public static int getEvtColor(SharedPreferences sp) {
		return getEvtColor(sp, false);
	}

	public static int getEvbColor(SharedPreferences sp) {
		return getEvbColor(sp, false);
	}

	// 設定の読込（スライダー）
	public static int getTxtColor(SharedPreferences sp, boolean summary) {
		return getColor(sp, DEF.KEY_TXTCOLOR, DEF.KEY_TXTRGB, PRESET_TXT, 1, summary);
	}

	public static int getDirColor(SharedPreferences sp, boolean summary) {
		return getColor(sp, DEF.KEY_DIRCOLOR, DEF.KEY_DIRRGB, PRESET_DIR, 12, summary);
	}

	public static int getBefColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_BEFCOLOR, DEF.KEY_BEFRGB, PRESET_BEF, 1, summary);
		return val;
	}

	public static int getNowColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_NOWCOLOR, DEF.KEY_NOWRGB, PRESET_NOW, 7, summary);
		return val;
	}

	public static int getAftColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_AFTCOLOR, DEF.KEY_AFTRGB, PRESET_AFT, 8, summary);
		return val;
	}

	public static int getImgColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_IMGCOLOR, DEF.KEY_IMGRGB, PRESET_IMG, 13, summary);
		return val;
	}

	public static int getInfColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_INFCOLOR, DEF.KEY_INFRGB, PRESET_INF, 15, summary);
		return val;
	}

	public static int getMrkColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, null, DEF.KEY_MRKRGB, PRESET_MRK, 20, summary);
		return val;
	}

	public static int getBakColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_BAKCOLOR, DEF.KEY_BAKRGB, PRESET_BAK, 0, summary);
		return val;
	}

	public static int getCurColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, null, DEF.KEY_CURRGB, PRESET_CUR, 9, summary);
		return val;
	}

	public static int getTitColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, null, DEF.KEY_TITRGB, PRESET_TIT, 1, summary);
		return val;
	}

	public static int getTibColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, null, DEF.KEY_TIBRGB, PRESET_TIB, 8, summary);
		return val;
	}

	public static int getTldColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, null, DEF.KEY_TLDRGB, PRESET_TLD, 0, summary);
		return val;
	}

	public static int getTlbColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, null, DEF.KEY_TLBRGB, PRESET_TLB, 15, summary);
		return val;
	}

	public static int getRrbColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_RRBCOLOR, DEF.KEY_RRBRGB, PRESET_RRB, 23, summary);
		return val;
	}

	public static int getBsfColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_BSFCOLOR, DEF.KEY_BSFRGB, PRESET_BSF, 1, summary);
		return val;
	}

	public static int getBseColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_BSECOLOR, DEF.KEY_BSERGB, PRESET_BSE, 0, summary);
		return val;
	}

	public static int getFifColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_FIFCOLOR, DEF.KEY_FIFRGB, PRESET_FIF, 1, summary);
		return val;
	}

	public static int getFibColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_FIBCOLOR, DEF.KEY_FIBRGB, PRESET_FIB, 0, summary);
		return val;
	}

	public static int getEvtColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_EVTCOLOR, DEF.KEY_EVTRGB, PRESET_EVT, 0, summary);
		return val;
	}

	public static int getEvbColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_EVBCOLOR, DEF.KEY_EVBRGB, PRESET_EVB, 24, summary);
		return val;
	}

	public static boolean getCheckEnableTheme(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_ENABLE_THEME, false);
		return flag;
	}

	private String getFontTitleSummary(SharedPreferences sharedPreferences){
		int val = getFontTitle(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getFontSpStr(val, summ1);
	}

	private String getFontMainSummary(SharedPreferences sharedPreferences){
		int val = getFontMain(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getFontSpStr(val, summ1);
	}

	private String getFontSubSummary(SharedPreferences sharedPreferences){
		int val = getFontSub(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getFontSpStr(val, summ1);
	}

	private String getFontTileSummary(SharedPreferences sharedPreferences){
		int val = getFontTile(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getFontSpStr(val, summ1);
	}

	private String getItemMarginSummary(SharedPreferences sharedPreferences){
		int val = getItemMargin(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getMarginSpStr(val, summ1);
	}

	private String getToolbarSeekSummary(SharedPreferences sharedPreferences){
		int size = getToolbarSize(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);
		return DEF.getToolbarSeekStr(size, summ1);
	}

	private String getThemePresetSummary(int val) {
		Resources res = getResources();
		return res.getString(mThemePresetName[val]);
	}

	private static int getColor(SharedPreferences sp, String keyColor, String keyRGB, int index, int def, boolean summary) {
		int preset = getPreset(sp);
		int val;
		if (preset == 0 || summary) {
			val = DEF.getColorValue(sp, keyColor, keyRGB, def);
		}
		else {
			val = mPresetColor[preset][index];
		}
		return val;
	}

	private String getPresetSummary(int val) {
		Resources res = getResources();
		return res.getString(mPresetName[val]);
	}

	private String getColorSummary(int val) {
		String[] str = { "Red", "Green", "Blue" };
		String result = "";
		for (int i = 0; i < 3; i++) {
			int v = ((val >> 8 * (2 - i)) & 0x000000FF);
			result += str[i] + "=" + v;
			if (i != 2) {
				result += ", ";
			}
		}
		result += String.format(" (%1$06X)", val & 0x00FFFFFF);
		return result;
	}

	private void setThemeS(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_THEMES_PRESET, String.valueOf(getPreset(sp)));
		ed.putInt(DEF.KEY_THEMES_FONTTITLE, getFontTitle(sp));
		ed.putInt(DEF.KEY_THEMES_FONTMAIN, getFontMain(sp));
		ed.putInt(DEF.KEY_THEMES_FONTSUB, getFontSub(sp));
		ed.putInt(DEF.KEY_THEMES_FONTTILE, getFontTile(sp));
		ed.putInt(DEF.KEY_THEMES_ITEMMRGN, getItemMargin(sp));
		ed.putInt(DEF.KEY_THEMES_TOOLBARSEEK, getToolbarSize(sp));
		ed.putInt(DEF.KEY_THEMES_TXTRGB, getTxtColor(sp));
		ed.putInt(DEF.KEY_THEMES_DIRRGB, getDirColor(sp));
		ed.putInt(DEF.KEY_THEMES_BEFRGB, getBefColor(sp));
		ed.putInt(DEF.KEY_THEMES_NOWRGB, getNowColor(sp));
		ed.putInt(DEF.KEY_THEMES_AFTRGB, getAftColor(sp));
		ed.putInt(DEF.KEY_THEMES_RRBRGB, getRrbColor(sp));
		ed.putInt(DEF.KEY_THEMES_IMGRGB, getImgColor(sp));
		ed.putInt(DEF.KEY_THEMES_INFRGB, getInfColor(sp));
		ed.putInt(DEF.KEY_THEMES_MRKRGB, getMrkColor(sp));
		ed.putInt(DEF.KEY_THEMES_BAKRGB, getBakColor(sp));
		ed.putInt(DEF.KEY_THEMES_CURRGB, getCurColor(sp));
		ed.putInt(DEF.KEY_THEMES_TITRGB, getTitColor(sp));
		ed.putInt(DEF.KEY_THEMES_TIBRGB, getTibColor(sp));
		ed.putInt(DEF.KEY_THEMES_TLDRGB, getTldColor(sp));
		ed.putInt(DEF.KEY_THEMES_TLBRGB, getTlbColor(sp));
		ed.putInt(DEF.KEY_THEMES_BSFRGB, getBsfColor(sp));
		ed.putInt(DEF.KEY_THEMES_BSERGB, getBseColor(sp));
		ed.putInt(DEF.KEY_THEMES_FIFRGB, getFifColor(sp));
		ed.putInt(DEF.KEY_THEMES_FIBRGB, getFibColor(sp));
		ed.putInt(DEF.KEY_THEMES_EVTRGB, getEvtColor(sp));
		ed.putInt(DEF.KEY_THEMES_EVBRGB, getEvbColor(sp));
		ed.apply();
	}

	private void setTheme1(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_THEME1_PRESET, String.valueOf(getPreset(sp)));
		ed.putInt(DEF.KEY_THEME1_FONTTITLE, getFontTitle(sp));
		ed.putInt(DEF.KEY_THEME1_FONTMAIN, getFontMain(sp));
		ed.putInt(DEF.KEY_THEME1_FONTSUB, getFontSub(sp));
		ed.putInt(DEF.KEY_THEME1_FONTTILE, getFontTile(sp));
		ed.putInt(DEF.KEY_THEME1_ITEMMRGN, getItemMargin(sp));
		ed.putInt(DEF.KEY_THEME1_TOOLBARSEEK, getToolbarSize(sp));
		ed.putInt(DEF.KEY_THEME1_TXTRGB, getTxtColor(sp));
		ed.putInt(DEF.KEY_THEME1_DIRRGB, getDirColor(sp));
		ed.putInt(DEF.KEY_THEME1_BEFRGB, getBefColor(sp));
		ed.putInt(DEF.KEY_THEME1_NOWRGB, getNowColor(sp));
		ed.putInt(DEF.KEY_THEME1_AFTRGB, getAftColor(sp));
		ed.putInt(DEF.KEY_THEME1_RRBRGB, getRrbColor(sp));
		ed.putInt(DEF.KEY_THEME1_IMGRGB, getImgColor(sp));
		ed.putInt(DEF.KEY_THEME1_INFRGB, getInfColor(sp));
		ed.putInt(DEF.KEY_THEME1_MRKRGB, getMrkColor(sp));
		ed.putInt(DEF.KEY_THEME1_BAKRGB, getBakColor(sp));
		ed.putInt(DEF.KEY_THEME1_CURRGB, getCurColor(sp));
		ed.putInt(DEF.KEY_THEME1_TITRGB, getTitColor(sp));
		ed.putInt(DEF.KEY_THEME1_TIBRGB, getTibColor(sp));
		ed.putInt(DEF.KEY_THEME1_TLDRGB, getTldColor(sp));
		ed.putInt(DEF.KEY_THEME1_TLBRGB, getTlbColor(sp));
		ed.putInt(DEF.KEY_THEME1_BSFRGB, getBsfColor(sp));
		ed.putInt(DEF.KEY_THEME1_BSERGB, getBseColor(sp));
		ed.putInt(DEF.KEY_THEME1_FIFRGB, getFifColor(sp));
		ed.putInt(DEF.KEY_THEME1_FIBRGB, getFibColor(sp));
		ed.putInt(DEF.KEY_THEME1_EVTRGB, getEvtColor(sp));
		ed.putInt(DEF.KEY_THEME1_EVBRGB, getEvbColor(sp));
		ed.apply();
	}

	private void setTheme2(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_THEME2_PRESET, String.valueOf(getPreset(sp)));
		ed.putInt(DEF.KEY_THEME2_FONTTITLE, getFontTitle(sp));
		ed.putInt(DEF.KEY_THEME2_FONTMAIN, getFontMain(sp));
		ed.putInt(DEF.KEY_THEME2_FONTSUB, getFontSub(sp));
		ed.putInt(DEF.KEY_THEME2_FONTTILE, getFontTile(sp));
		ed.putInt(DEF.KEY_THEME2_ITEMMRGN, getItemMargin(sp));
		ed.putInt(DEF.KEY_THEME2_TOOLBARSEEK, getToolbarSize(sp));
		ed.putInt(DEF.KEY_THEME2_TXTRGB, getTxtColor(sp));
		ed.putInt(DEF.KEY_THEME2_DIRRGB, getDirColor(sp));
		ed.putInt(DEF.KEY_THEME2_BEFRGB, getBefColor(sp));
		ed.putInt(DEF.KEY_THEME2_NOWRGB, getNowColor(sp));
		ed.putInt(DEF.KEY_THEME2_AFTRGB, getAftColor(sp));
		ed.putInt(DEF.KEY_THEME2_RRBRGB, getRrbColor(sp));
		ed.putInt(DEF.KEY_THEME2_IMGRGB, getImgColor(sp));
		ed.putInt(DEF.KEY_THEME2_INFRGB, getInfColor(sp));
		ed.putInt(DEF.KEY_THEME2_MRKRGB, getMrkColor(sp));
		ed.putInt(DEF.KEY_THEME2_BAKRGB, getBakColor(sp));
		ed.putInt(DEF.KEY_THEME2_CURRGB, getCurColor(sp));
		ed.putInt(DEF.KEY_THEME2_TITRGB, getTitColor(sp));
		ed.putInt(DEF.KEY_THEME2_TIBRGB, getTibColor(sp));
		ed.putInt(DEF.KEY_THEME2_TLDRGB, getTldColor(sp));
		ed.putInt(DEF.KEY_THEME2_TLBRGB, getTlbColor(sp));
		ed.putInt(DEF.KEY_THEME2_BSFRGB, getBsfColor(sp));
		ed.putInt(DEF.KEY_THEME2_BSERGB, getBseColor(sp));
		ed.putInt(DEF.KEY_THEME2_FIFRGB, getFifColor(sp));
		ed.putInt(DEF.KEY_THEME2_FIBRGB, getFibColor(sp));
		ed.putInt(DEF.KEY_THEME2_EVTRGB, getEvtColor(sp));
		ed.putInt(DEF.KEY_THEME2_EVBRGB, getEvbColor(sp));
		ed.apply();
	}

	private void setTheme3(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_THEME3_PRESET, String.valueOf(getPreset(sp)));
		ed.putInt(DEF.KEY_THEME3_FONTTITLE, getFontTitle(sp));
		ed.putInt(DEF.KEY_THEME3_FONTMAIN, getFontMain(sp));
		ed.putInt(DEF.KEY_THEME3_FONTSUB, getFontSub(sp));
		ed.putInt(DEF.KEY_THEME3_FONTTILE, getFontTile(sp));
		ed.putInt(DEF.KEY_THEME3_ITEMMRGN, getItemMargin(sp));
		ed.putInt(DEF.KEY_THEME3_TOOLBARSEEK, getToolbarSize(sp));
		ed.putInt(DEF.KEY_THEME3_TXTRGB, getTxtColor(sp));
		ed.putInt(DEF.KEY_THEME3_DIRRGB, getDirColor(sp));
		ed.putInt(DEF.KEY_THEME3_BEFRGB, getBefColor(sp));
		ed.putInt(DEF.KEY_THEME3_NOWRGB, getNowColor(sp));
		ed.putInt(DEF.KEY_THEME3_AFTRGB, getAftColor(sp));
		ed.putInt(DEF.KEY_THEME3_RRBRGB, getRrbColor(sp));
		ed.putInt(DEF.KEY_THEME3_IMGRGB, getImgColor(sp));
		ed.putInt(DEF.KEY_THEME3_INFRGB, getInfColor(sp));
		ed.putInt(DEF.KEY_THEME3_MRKRGB, getMrkColor(sp));
		ed.putInt(DEF.KEY_THEME3_BAKRGB, getBakColor(sp));
		ed.putInt(DEF.KEY_THEME3_CURRGB, getCurColor(sp));
		ed.putInt(DEF.KEY_THEME3_TITRGB, getTitColor(sp));
		ed.putInt(DEF.KEY_THEME3_TIBRGB, getTibColor(sp));
		ed.putInt(DEF.KEY_THEME3_TLDRGB, getTldColor(sp));
		ed.putInt(DEF.KEY_THEME3_TLBRGB, getTlbColor(sp));
		ed.putInt(DEF.KEY_THEME3_BSFRGB, getBsfColor(sp));
		ed.putInt(DEF.KEY_THEME3_BSERGB, getBseColor(sp));
		ed.putInt(DEF.KEY_THEME3_FIFRGB, getFifColor(sp));
		ed.putInt(DEF.KEY_THEME3_FIBRGB, getFibColor(sp));
		ed.putInt(DEF.KEY_THEME3_EVTRGB, getEvtColor(sp));
		ed.putInt(DEF.KEY_THEME3_EVBRGB, getEvbColor(sp));
		ed.apply();
	}

	private void setTheme4(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_THEME4_PRESET, String.valueOf(getPreset(sp)));
		ed.putInt(DEF.KEY_THEME4_FONTTITLE, getFontTitle(sp));
		ed.putInt(DEF.KEY_THEME4_FONTMAIN, getFontMain(sp));
		ed.putInt(DEF.KEY_THEME4_FONTSUB, getFontSub(sp));
		ed.putInt(DEF.KEY_THEME4_FONTTILE, getFontTile(sp));
		ed.putInt(DEF.KEY_THEME4_ITEMMRGN, getItemMargin(sp));
		ed.putInt(DEF.KEY_THEME4_TOOLBARSEEK, getToolbarSize(sp));
		ed.putInt(DEF.KEY_THEME4_TXTRGB, getTxtColor(sp));
		ed.putInt(DEF.KEY_THEME4_DIRRGB, getDirColor(sp));
		ed.putInt(DEF.KEY_THEME4_BEFRGB, getBefColor(sp));
		ed.putInt(DEF.KEY_THEME4_NOWRGB, getNowColor(sp));
		ed.putInt(DEF.KEY_THEME4_AFTRGB, getAftColor(sp));
		ed.putInt(DEF.KEY_THEME4_RRBRGB, getRrbColor(sp));
		ed.putInt(DEF.KEY_THEME4_IMGRGB, getImgColor(sp));
		ed.putInt(DEF.KEY_THEME4_INFRGB, getInfColor(sp));
		ed.putInt(DEF.KEY_THEME4_MRKRGB, getMrkColor(sp));
		ed.putInt(DEF.KEY_THEME4_BAKRGB, getBakColor(sp));
		ed.putInt(DEF.KEY_THEME4_CURRGB, getCurColor(sp));
		ed.putInt(DEF.KEY_THEME4_TITRGB, getTitColor(sp));
		ed.putInt(DEF.KEY_THEME4_TIBRGB, getTibColor(sp));
		ed.putInt(DEF.KEY_THEME4_TLDRGB, getTldColor(sp));
		ed.putInt(DEF.KEY_THEME4_TLBRGB, getTlbColor(sp));
		ed.putInt(DEF.KEY_THEME4_BSFRGB, getBsfColor(sp));
		ed.putInt(DEF.KEY_THEME4_BSERGB, getBseColor(sp));
		ed.putInt(DEF.KEY_THEME4_FIFRGB, getFifColor(sp));
		ed.putInt(DEF.KEY_THEME4_FIBRGB, getFibColor(sp));
		ed.putInt(DEF.KEY_THEME4_EVTRGB, getEvtColor(sp));
		ed.putInt(DEF.KEY_THEME4_EVBRGB, getEvbColor(sp));
		ed.apply();
	}

	private void setTheme5(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_THEME5_PRESET, String.valueOf(getPreset(sp)));
		ed.putInt(DEF.KEY_THEME5_FONTTITLE, getFontTitle(sp));
		ed.putInt(DEF.KEY_THEME5_FONTMAIN, getFontMain(sp));
		ed.putInt(DEF.KEY_THEME5_FONTSUB, getFontSub(sp));
		ed.putInt(DEF.KEY_THEME5_FONTTILE, getFontTile(sp));
		ed.putInt(DEF.KEY_THEME5_ITEMMRGN, getItemMargin(sp));
		ed.putInt(DEF.KEY_THEME5_TOOLBARSEEK, getToolbarSize(sp));
		ed.putInt(DEF.KEY_THEME5_TXTRGB, getTxtColor(sp));
		ed.putInt(DEF.KEY_THEME5_DIRRGB, getDirColor(sp));
		ed.putInt(DEF.KEY_THEME5_BEFRGB, getBefColor(sp));
		ed.putInt(DEF.KEY_THEME5_NOWRGB, getNowColor(sp));
		ed.putInt(DEF.KEY_THEME5_AFTRGB, getAftColor(sp));
		ed.putInt(DEF.KEY_THEME5_RRBRGB, getRrbColor(sp));
		ed.putInt(DEF.KEY_THEME5_IMGRGB, getImgColor(sp));
		ed.putInt(DEF.KEY_THEME5_INFRGB, getInfColor(sp));
		ed.putInt(DEF.KEY_THEME5_MRKRGB, getMrkColor(sp));
		ed.putInt(DEF.KEY_THEME5_BAKRGB, getBakColor(sp));
		ed.putInt(DEF.KEY_THEME5_CURRGB, getCurColor(sp));
		ed.putInt(DEF.KEY_THEME5_TITRGB, getTitColor(sp));
		ed.putInt(DEF.KEY_THEME5_TIBRGB, getTibColor(sp));
		ed.putInt(DEF.KEY_THEME5_TLDRGB, getTldColor(sp));
		ed.putInt(DEF.KEY_THEME5_TLBRGB, getTlbColor(sp));
		ed.putInt(DEF.KEY_THEME5_BSFRGB, getBsfColor(sp));
		ed.putInt(DEF.KEY_THEME5_BSERGB, getBseColor(sp));
		ed.putInt(DEF.KEY_THEME5_FIFRGB, getFifColor(sp));
		ed.putInt(DEF.KEY_THEME5_FIBRGB, getFibColor(sp));
		ed.putInt(DEF.KEY_THEME5_EVTRGB, getEvtColor(sp));
		ed.putInt(DEF.KEY_THEME5_EVBRGB, getEvbColor(sp));
		ed.apply();
	}

	private void setTheme6(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_THEME6_PRESET, String.valueOf(getPreset(sp)));
		ed.putInt(DEF.KEY_THEME6_FONTTITLE, getFontTitle(sp));
		ed.putInt(DEF.KEY_THEME6_FONTMAIN, getFontMain(sp));
		ed.putInt(DEF.KEY_THEME6_FONTSUB, getFontSub(sp));
		ed.putInt(DEF.KEY_THEME6_FONTTILE, getFontTile(sp));
		ed.putInt(DEF.KEY_THEME6_ITEMMRGN, getItemMargin(sp));
		ed.putInt(DEF.KEY_THEME6_TOOLBARSEEK, getToolbarSize(sp));
		ed.putInt(DEF.KEY_THEME6_TXTRGB, getTxtColor(sp));
		ed.putInt(DEF.KEY_THEME6_DIRRGB, getDirColor(sp));
		ed.putInt(DEF.KEY_THEME6_BEFRGB, getBefColor(sp));
		ed.putInt(DEF.KEY_THEME6_NOWRGB, getNowColor(sp));
		ed.putInt(DEF.KEY_THEME6_AFTRGB, getAftColor(sp));
		ed.putInt(DEF.KEY_THEME6_RRBRGB, getRrbColor(sp));
		ed.putInt(DEF.KEY_THEME6_IMGRGB, getImgColor(sp));
		ed.putInt(DEF.KEY_THEME6_INFRGB, getInfColor(sp));
		ed.putInt(DEF.KEY_THEME6_MRKRGB, getMrkColor(sp));
		ed.putInt(DEF.KEY_THEME6_BAKRGB, getBakColor(sp));
		ed.putInt(DEF.KEY_THEME6_CURRGB, getCurColor(sp));
		ed.putInt(DEF.KEY_THEME6_TITRGB, getTitColor(sp));
		ed.putInt(DEF.KEY_THEME6_TIBRGB, getTibColor(sp));
		ed.putInt(DEF.KEY_THEME6_TLDRGB, getTldColor(sp));
		ed.putInt(DEF.KEY_THEME6_TLBRGB, getTlbColor(sp));
		ed.putInt(DEF.KEY_THEME6_BSFRGB, getBsfColor(sp));
		ed.putInt(DEF.KEY_THEME6_BSERGB, getBseColor(sp));
		ed.putInt(DEF.KEY_THEME6_FIFRGB, getFifColor(sp));
		ed.putInt(DEF.KEY_THEME6_FIBRGB, getFibColor(sp));
		ed.putInt(DEF.KEY_THEME6_EVTRGB, getEvtColor(sp));
		ed.putInt(DEF.KEY_THEME6_EVBRGB, getEvbColor(sp));
		ed.apply();
	}

	private void setTheme7(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_THEME7_PRESET, String.valueOf(getPreset(sp)));
		ed.putInt(DEF.KEY_THEME7_FONTTITLE, getFontTitle(sp));
		ed.putInt(DEF.KEY_THEME7_FONTMAIN, getFontMain(sp));
		ed.putInt(DEF.KEY_THEME7_FONTSUB, getFontSub(sp));
		ed.putInt(DEF.KEY_THEME7_FONTTILE, getFontTile(sp));
		ed.putInt(DEF.KEY_THEME7_ITEMMRGN, getItemMargin(sp));
		ed.putInt(DEF.KEY_THEME7_TOOLBARSEEK, getToolbarSize(sp));
		ed.putInt(DEF.KEY_THEME7_TXTRGB, getTxtColor(sp));
		ed.putInt(DEF.KEY_THEME7_DIRRGB, getDirColor(sp));
		ed.putInt(DEF.KEY_THEME7_BEFRGB, getBefColor(sp));
		ed.putInt(DEF.KEY_THEME7_NOWRGB, getNowColor(sp));
		ed.putInt(DEF.KEY_THEME7_AFTRGB, getAftColor(sp));
		ed.putInt(DEF.KEY_THEME7_RRBRGB, getRrbColor(sp));
		ed.putInt(DEF.KEY_THEME7_IMGRGB, getImgColor(sp));
		ed.putInt(DEF.KEY_THEME7_INFRGB, getInfColor(sp));
		ed.putInt(DEF.KEY_THEME7_MRKRGB, getMrkColor(sp));
		ed.putInt(DEF.KEY_THEME7_BAKRGB, getBakColor(sp));
		ed.putInt(DEF.KEY_THEME7_CURRGB, getCurColor(sp));
		ed.putInt(DEF.KEY_THEME7_TITRGB, getTitColor(sp));
		ed.putInt(DEF.KEY_THEME7_TIBRGB, getTibColor(sp));
		ed.putInt(DEF.KEY_THEME7_TLDRGB, getTldColor(sp));
		ed.putInt(DEF.KEY_THEME7_TLBRGB, getTlbColor(sp));
		ed.putInt(DEF.KEY_THEME7_BSFRGB, getBsfColor(sp));
		ed.putInt(DEF.KEY_THEME7_BSERGB, getBseColor(sp));
		ed.putInt(DEF.KEY_THEME7_FIFRGB, getFifColor(sp));
		ed.putInt(DEF.KEY_THEME7_FIBRGB, getFibColor(sp));
		ed.putInt(DEF.KEY_THEME7_EVTRGB, getEvtColor(sp));
		ed.putInt(DEF.KEY_THEME7_EVBRGB, getEvbColor(sp));
		ed.apply();
	}

	private void setTheme8(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_THEME8_PRESET, String.valueOf(getPreset(sp)));
		ed.putInt(DEF.KEY_THEME8_FONTTITLE, getFontTitle(sp));
		ed.putInt(DEF.KEY_THEME8_FONTMAIN, getFontMain(sp));
		ed.putInt(DEF.KEY_THEME8_FONTSUB, getFontSub(sp));
		ed.putInt(DEF.KEY_THEME8_FONTTILE, getFontTile(sp));
		ed.putInt(DEF.KEY_THEME8_ITEMMRGN, getItemMargin(sp));
		ed.putInt(DEF.KEY_THEME8_TOOLBARSEEK, getToolbarSize(sp));
		ed.putInt(DEF.KEY_THEME8_TXTRGB, getTxtColor(sp));
		ed.putInt(DEF.KEY_THEME8_DIRRGB, getDirColor(sp));
		ed.putInt(DEF.KEY_THEME8_BEFRGB, getBefColor(sp));
		ed.putInt(DEF.KEY_THEME8_NOWRGB, getNowColor(sp));
		ed.putInt(DEF.KEY_THEME8_AFTRGB, getAftColor(sp));
		ed.putInt(DEF.KEY_THEME8_RRBRGB, getRrbColor(sp));
		ed.putInt(DEF.KEY_THEME8_IMGRGB, getImgColor(sp));
		ed.putInt(DEF.KEY_THEME8_INFRGB, getInfColor(sp));
		ed.putInt(DEF.KEY_THEME8_MRKRGB, getMrkColor(sp));
		ed.putInt(DEF.KEY_THEME8_BAKRGB, getBakColor(sp));
		ed.putInt(DEF.KEY_THEME8_CURRGB, getCurColor(sp));
		ed.putInt(DEF.KEY_THEME8_TITRGB, getTitColor(sp));
		ed.putInt(DEF.KEY_THEME8_TIBRGB, getTibColor(sp));
		ed.putInt(DEF.KEY_THEME8_TLDRGB, getTldColor(sp));
		ed.putInt(DEF.KEY_THEME8_TLBRGB, getTlbColor(sp));
		ed.putInt(DEF.KEY_THEME8_BSFRGB, getBsfColor(sp));
		ed.putInt(DEF.KEY_THEME8_BSERGB, getBseColor(sp));
		ed.putInt(DEF.KEY_THEME8_FIFRGB, getFifColor(sp));
		ed.putInt(DEF.KEY_THEME8_FIBRGB, getFibColor(sp));
		ed.putInt(DEF.KEY_THEME8_EVTRGB, getEvtColor(sp));
		ed.putInt(DEF.KEY_THEME8_EVBRGB, getEvbColor(sp));
		ed.apply();
	}

	private void getThemeS(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_PRESET, String.valueOf(DEF.getInt(sp, DEF.KEY_THEMES_PRESET, "1")));
		ed.putInt(DEF.KEY_FONTTITLE, DEF.getInt(sp, DEF.KEY_THEMES_FONTTITLE, DEF.DEFAULT_FONTTITLE));
		ed.putInt(DEF.KEY_FONTMAIN, DEF.getInt(sp, DEF.KEY_THEMES_FONTMAIN, DEF.DEFAULT_FONTMAIN));
		ed.putInt(DEF.KEY_FONTSUB, DEF.getInt(sp, DEF.KEY_THEMES_FONTSUB, DEF.DEFAULT_FONTSUB));
		ed.putInt(DEF.KEY_FONTTILE, DEF.getInt(sp, DEF.KEY_THEMES_FONTTILE, DEF.DEFAULT_FONTTILE));
		ed.putInt(DEF.KEY_ITEMMRGN, DEF.getInt(sp, DEF.KEY_THEMES_ITEMMRGN, DEF.DEFAULT_ITEMMARGIN));
		ed.putInt(DEF.KEY_TOOLBARSEEK, DEF.getInt(sp, DEF.KEY_THEMES_TOOLBARSEEK, DEF.DEFAULT_TOOLBARSEEK));
		ed.putInt(DEF.KEY_TXTRGB, DEF.getInt(sp, DEF.KEY_THEMES_TXTRGB, mPresetColor[1][0]));
		ed.putInt(DEF.KEY_DIRRGB, DEF.getInt(sp, DEF.KEY_THEMES_DIRRGB, mPresetColor[1][1]));
		ed.putInt(DEF.KEY_BEFRGB, DEF.getInt(sp, DEF.KEY_THEMES_BEFRGB, mPresetColor[1][2]));
		ed.putInt(DEF.KEY_NOWRGB, DEF.getInt(sp, DEF.KEY_THEMES_NOWRGB, mPresetColor[1][3]));
		ed.putInt(DEF.KEY_AFTRGB, DEF.getInt(sp, DEF.KEY_THEMES_AFTRGB, mPresetColor[1][4]));
		ed.putInt(DEF.KEY_RRBRGB, DEF.getInt(sp, DEF.KEY_THEMES_RRBRGB, mPresetColor[1][5]));
		ed.putInt(DEF.KEY_IMGRGB, DEF.getInt(sp, DEF.KEY_THEMES_IMGRGB, mPresetColor[1][6]));
		ed.putInt(DEF.KEY_INFRGB, DEF.getInt(sp, DEF.KEY_THEMES_INFRGB, mPresetColor[1][7]));
		ed.putInt(DEF.KEY_MRKRGB, DEF.getInt(sp, DEF.KEY_THEMES_MRKRGB, mPresetColor[1][8]));
		ed.putInt(DEF.KEY_BAKRGB, DEF.getInt(sp, DEF.KEY_THEMES_BAKRGB, mPresetColor[1][9]));
		ed.putInt(DEF.KEY_CURRGB, DEF.getInt(sp, DEF.KEY_THEMES_CURRGB, mPresetColor[1][10]));
		ed.putInt(DEF.KEY_TITRGB, DEF.getInt(sp, DEF.KEY_THEMES_TITRGB, mPresetColor[1][11]));
		ed.putInt(DEF.KEY_TIBRGB, DEF.getInt(sp, DEF.KEY_THEMES_TIBRGB, mPresetColor[1][12]));
		ed.putInt(DEF.KEY_TLDRGB, DEF.getInt(sp, DEF.KEY_THEMES_TLDRGB, mPresetColor[1][13]));
		ed.putInt(DEF.KEY_TLBRGB, DEF.getInt(sp, DEF.KEY_THEMES_TLBRGB, mPresetColor[1][14]));
		ed.putInt(DEF.KEY_BSFRGB, DEF.getInt(sp, DEF.KEY_THEMES_BSFRGB, mPresetColor[1][15]));
		ed.putInt(DEF.KEY_BSERGB, DEF.getInt(sp, DEF.KEY_THEMES_BSERGB, mPresetColor[1][16]));
		ed.putInt(DEF.KEY_FIFRGB, DEF.getInt(sp, DEF.KEY_THEMES_FIFRGB, mPresetColor[1][17]));
		ed.putInt(DEF.KEY_FIBRGB, DEF.getInt(sp, DEF.KEY_THEMES_FIBRGB, mPresetColor[1][18]));
		ed.putInt(DEF.KEY_EVTRGB, DEF.getInt(sp, DEF.KEY_THEMES_EVTRGB, mPresetColor[1][19]));
		ed.putInt(DEF.KEY_EVBRGB, DEF.getInt(sp, DEF.KEY_THEMES_EVBRGB, mPresetColor[1][20]));
		ed.apply();
	}

	private void getTheme1(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_PRESET, String.valueOf(DEF.getInt(sp, DEF.KEY_THEME1_PRESET, "1")));
		ed.putInt(DEF.KEY_FONTTITLE, DEF.getInt(sp, DEF.KEY_THEME1_FONTTITLE, DEF.DEFAULT_FONTTITLE));
		ed.putInt(DEF.KEY_FONTMAIN, DEF.getInt(sp, DEF.KEY_THEME1_FONTMAIN, DEF.DEFAULT_FONTMAIN));
		ed.putInt(DEF.KEY_FONTSUB, DEF.getInt(sp, DEF.KEY_THEME1_FONTSUB, DEF.DEFAULT_FONTSUB));
		ed.putInt(DEF.KEY_FONTTILE, DEF.getInt(sp, DEF.KEY_THEME1_FONTTILE, DEF.DEFAULT_FONTTILE));
		ed.putInt(DEF.KEY_ITEMMRGN, DEF.getInt(sp, DEF.KEY_THEME1_ITEMMRGN, DEF.DEFAULT_ITEMMARGIN));
		ed.putInt(DEF.KEY_TOOLBARSEEK, DEF.getInt(sp, DEF.KEY_THEME1_TOOLBARSEEK, DEF.DEFAULT_TOOLBARSEEK));
		ed.putInt(DEF.KEY_TXTRGB, DEF.getInt(sp, DEF.KEY_THEME1_TXTRGB, mPresetColor[1][0]));
		ed.putInt(DEF.KEY_DIRRGB, DEF.getInt(sp, DEF.KEY_THEME1_DIRRGB, mPresetColor[1][1]));
		ed.putInt(DEF.KEY_BEFRGB, DEF.getInt(sp, DEF.KEY_THEME1_BEFRGB, mPresetColor[1][2]));
		ed.putInt(DEF.KEY_NOWRGB, DEF.getInt(sp, DEF.KEY_THEME1_NOWRGB, mPresetColor[1][3]));
		ed.putInt(DEF.KEY_AFTRGB, DEF.getInt(sp, DEF.KEY_THEME1_AFTRGB, mPresetColor[1][4]));
		ed.putInt(DEF.KEY_RRBRGB, DEF.getInt(sp, DEF.KEY_THEME1_RRBRGB, mPresetColor[1][5]));
		ed.putInt(DEF.KEY_IMGRGB, DEF.getInt(sp, DEF.KEY_THEME1_IMGRGB, mPresetColor[1][6]));
		ed.putInt(DEF.KEY_INFRGB, DEF.getInt(sp, DEF.KEY_THEME1_INFRGB, mPresetColor[1][7]));
		ed.putInt(DEF.KEY_MRKRGB, DEF.getInt(sp, DEF.KEY_THEME1_MRKRGB, mPresetColor[1][8]));
		ed.putInt(DEF.KEY_BAKRGB, DEF.getInt(sp, DEF.KEY_THEME1_BAKRGB, mPresetColor[1][9]));
		ed.putInt(DEF.KEY_CURRGB, DEF.getInt(sp, DEF.KEY_THEME1_CURRGB, mPresetColor[1][10]));
		ed.putInt(DEF.KEY_TITRGB, DEF.getInt(sp, DEF.KEY_THEME1_TITRGB, mPresetColor[1][11]));
		ed.putInt(DEF.KEY_TIBRGB, DEF.getInt(sp, DEF.KEY_THEME1_TIBRGB, mPresetColor[1][12]));
		ed.putInt(DEF.KEY_TLDRGB, DEF.getInt(sp, DEF.KEY_THEME1_TLDRGB, mPresetColor[1][13]));
		ed.putInt(DEF.KEY_TLBRGB, DEF.getInt(sp, DEF.KEY_THEME1_TLBRGB, mPresetColor[1][14]));
		ed.putInt(DEF.KEY_BSFRGB, DEF.getInt(sp, DEF.KEY_THEME1_BSFRGB, mPresetColor[1][15]));
		ed.putInt(DEF.KEY_BSERGB, DEF.getInt(sp, DEF.KEY_THEME1_BSERGB, mPresetColor[1][16]));
		ed.putInt(DEF.KEY_FIFRGB, DEF.getInt(sp, DEF.KEY_THEME1_FIFRGB, mPresetColor[1][17]));
		ed.putInt(DEF.KEY_FIBRGB, DEF.getInt(sp, DEF.KEY_THEME1_FIBRGB, mPresetColor[1][18]));
		ed.putInt(DEF.KEY_EVTRGB, DEF.getInt(sp, DEF.KEY_THEME1_EVTRGB, mPresetColor[1][19]));
		ed.putInt(DEF.KEY_EVBRGB, DEF.getInt(sp, DEF.KEY_THEME1_EVBRGB, mPresetColor[1][20]));
		ed.apply();
	}

	private void getTheme2(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_PRESET, String.valueOf(DEF.getInt(sp, DEF.KEY_THEME2_PRESET, "1")));
		ed.putInt(DEF.KEY_FONTTITLE, DEF.getInt(sp, DEF.KEY_THEME2_FONTTITLE, DEF.DEFAULT_FONTTITLE));
		ed.putInt(DEF.KEY_FONTMAIN, DEF.getInt(sp, DEF.KEY_THEME2_FONTMAIN, DEF.DEFAULT_FONTMAIN));
		ed.putInt(DEF.KEY_FONTSUB, DEF.getInt(sp, DEF.KEY_THEME2_FONTSUB, DEF.DEFAULT_FONTSUB));
		ed.putInt(DEF.KEY_FONTTILE, DEF.getInt(sp, DEF.KEY_THEME2_FONTTILE, DEF.DEFAULT_FONTTILE));
		ed.putInt(DEF.KEY_ITEMMRGN, DEF.getInt(sp, DEF.KEY_THEME2_ITEMMRGN, DEF.DEFAULT_ITEMMARGIN));
		ed.putInt(DEF.KEY_TOOLBARSEEK, DEF.getInt(sp, DEF.KEY_THEME2_TOOLBARSEEK, DEF.DEFAULT_TOOLBARSEEK));
		ed.putInt(DEF.KEY_TXTRGB, DEF.getInt(sp, DEF.KEY_THEME2_TXTRGB, mPresetColor[1][0]));
		ed.putInt(DEF.KEY_DIRRGB, DEF.getInt(sp, DEF.KEY_THEME2_DIRRGB, mPresetColor[1][1]));
		ed.putInt(DEF.KEY_BEFRGB, DEF.getInt(sp, DEF.KEY_THEME2_BEFRGB, mPresetColor[1][2]));
		ed.putInt(DEF.KEY_NOWRGB, DEF.getInt(sp, DEF.KEY_THEME2_NOWRGB, mPresetColor[1][3]));
		ed.putInt(DEF.KEY_AFTRGB, DEF.getInt(sp, DEF.KEY_THEME2_AFTRGB, mPresetColor[1][4]));
		ed.putInt(DEF.KEY_RRBRGB, DEF.getInt(sp, DEF.KEY_THEME2_RRBRGB, mPresetColor[1][5]));
		ed.putInt(DEF.KEY_IMGRGB, DEF.getInt(sp, DEF.KEY_THEME2_IMGRGB, mPresetColor[1][6]));
		ed.putInt(DEF.KEY_INFRGB, DEF.getInt(sp, DEF.KEY_THEME2_INFRGB, mPresetColor[1][7]));
		ed.putInt(DEF.KEY_MRKRGB, DEF.getInt(sp, DEF.KEY_THEME2_MRKRGB, mPresetColor[1][8]));
		ed.putInt(DEF.KEY_BAKRGB, DEF.getInt(sp, DEF.KEY_THEME2_BAKRGB, mPresetColor[1][9]));
		ed.putInt(DEF.KEY_CURRGB, DEF.getInt(sp, DEF.KEY_THEME2_CURRGB, mPresetColor[1][10]));
		ed.putInt(DEF.KEY_TITRGB, DEF.getInt(sp, DEF.KEY_THEME2_TITRGB, mPresetColor[1][11]));
		ed.putInt(DEF.KEY_TIBRGB, DEF.getInt(sp, DEF.KEY_THEME2_TIBRGB, mPresetColor[1][12]));
		ed.putInt(DEF.KEY_TLDRGB, DEF.getInt(sp, DEF.KEY_THEME2_TLDRGB, mPresetColor[1][13]));
		ed.putInt(DEF.KEY_TLBRGB, DEF.getInt(sp, DEF.KEY_THEME2_TLBRGB, mPresetColor[1][14]));
		ed.putInt(DEF.KEY_BSFRGB, DEF.getInt(sp, DEF.KEY_THEME2_BSFRGB, mPresetColor[1][15]));
		ed.putInt(DEF.KEY_BSERGB, DEF.getInt(sp, DEF.KEY_THEME2_BSERGB, mPresetColor[1][16]));
		ed.putInt(DEF.KEY_FIFRGB, DEF.getInt(sp, DEF.KEY_THEME2_FIFRGB, mPresetColor[1][17]));
		ed.putInt(DEF.KEY_FIBRGB, DEF.getInt(sp, DEF.KEY_THEME2_FIBRGB, mPresetColor[1][18]));
		ed.putInt(DEF.KEY_EVTRGB, DEF.getInt(sp, DEF.KEY_THEME2_EVTRGB, mPresetColor[1][19]));
		ed.putInt(DEF.KEY_EVBRGB, DEF.getInt(sp, DEF.KEY_THEME2_EVBRGB, mPresetColor[1][20]));
		ed.apply();
	}

	private void getTheme3(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_PRESET, String.valueOf(DEF.getInt(sp, DEF.KEY_THEME3_PRESET, "1")));
		ed.putInt(DEF.KEY_FONTTITLE, DEF.getInt(sp, DEF.KEY_THEME3_FONTTITLE, DEF.DEFAULT_FONTTITLE));
		ed.putInt(DEF.KEY_FONTMAIN, DEF.getInt(sp, DEF.KEY_THEME3_FONTMAIN, DEF.DEFAULT_FONTMAIN));
		ed.putInt(DEF.KEY_FONTSUB, DEF.getInt(sp, DEF.KEY_THEME3_FONTSUB, DEF.DEFAULT_FONTSUB));
		ed.putInt(DEF.KEY_FONTTILE, DEF.getInt(sp, DEF.KEY_THEME3_FONTTILE, DEF.DEFAULT_FONTTILE));
		ed.putInt(DEF.KEY_ITEMMRGN, DEF.getInt(sp, DEF.KEY_THEME3_ITEMMRGN, DEF.DEFAULT_ITEMMARGIN));
		ed.putInt(DEF.KEY_TOOLBARSEEK, DEF.getInt(sp, DEF.KEY_THEME3_TOOLBARSEEK, DEF.DEFAULT_TOOLBARSEEK));
		ed.putInt(DEF.KEY_TXTRGB, DEF.getInt(sp, DEF.KEY_THEME3_TXTRGB, mPresetColor[1][0]));
		ed.putInt(DEF.KEY_DIRRGB, DEF.getInt(sp, DEF.KEY_THEME3_DIRRGB, mPresetColor[1][1]));
		ed.putInt(DEF.KEY_BEFRGB, DEF.getInt(sp, DEF.KEY_THEME3_BEFRGB, mPresetColor[1][2]));
		ed.putInt(DEF.KEY_NOWRGB, DEF.getInt(sp, DEF.KEY_THEME3_NOWRGB, mPresetColor[1][3]));
		ed.putInt(DEF.KEY_AFTRGB, DEF.getInt(sp, DEF.KEY_THEME3_AFTRGB, mPresetColor[1][4]));
		ed.putInt(DEF.KEY_RRBRGB, DEF.getInt(sp, DEF.KEY_THEME3_RRBRGB, mPresetColor[1][5]));
		ed.putInt(DEF.KEY_IMGRGB, DEF.getInt(sp, DEF.KEY_THEME3_IMGRGB, mPresetColor[1][6]));
		ed.putInt(DEF.KEY_INFRGB, DEF.getInt(sp, DEF.KEY_THEME3_INFRGB, mPresetColor[1][7]));
		ed.putInt(DEF.KEY_MRKRGB, DEF.getInt(sp, DEF.KEY_THEME3_MRKRGB, mPresetColor[1][8]));
		ed.putInt(DEF.KEY_BAKRGB, DEF.getInt(sp, DEF.KEY_THEME3_BAKRGB, mPresetColor[1][9]));
		ed.putInt(DEF.KEY_CURRGB, DEF.getInt(sp, DEF.KEY_THEME3_CURRGB, mPresetColor[1][10]));
		ed.putInt(DEF.KEY_TITRGB, DEF.getInt(sp, DEF.KEY_THEME3_TITRGB, mPresetColor[1][11]));
		ed.putInt(DEF.KEY_TIBRGB, DEF.getInt(sp, DEF.KEY_THEME3_TIBRGB, mPresetColor[1][12]));
		ed.putInt(DEF.KEY_TLDRGB, DEF.getInt(sp, DEF.KEY_THEME3_TLDRGB, mPresetColor[1][13]));
		ed.putInt(DEF.KEY_TLBRGB, DEF.getInt(sp, DEF.KEY_THEME3_TLBRGB, mPresetColor[1][14]));
		ed.putInt(DEF.KEY_BSFRGB, DEF.getInt(sp, DEF.KEY_THEME3_BSFRGB, mPresetColor[1][15]));
		ed.putInt(DEF.KEY_BSERGB, DEF.getInt(sp, DEF.KEY_THEME3_BSERGB, mPresetColor[1][16]));
		ed.putInt(DEF.KEY_FIFRGB, DEF.getInt(sp, DEF.KEY_THEME3_FIFRGB, mPresetColor[1][17]));
		ed.putInt(DEF.KEY_FIBRGB, DEF.getInt(sp, DEF.KEY_THEME3_FIBRGB, mPresetColor[1][18]));
		ed.putInt(DEF.KEY_EVTRGB, DEF.getInt(sp, DEF.KEY_THEME3_EVTRGB, mPresetColor[1][19]));
		ed.putInt(DEF.KEY_EVBRGB, DEF.getInt(sp, DEF.KEY_THEME3_EVBRGB, mPresetColor[1][20]));
		ed.apply();
	}

	private void getTheme4(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_PRESET, String.valueOf(DEF.getInt(sp, DEF.KEY_THEME4_PRESET, "1")));
		ed.putInt(DEF.KEY_FONTTITLE, DEF.getInt(sp, DEF.KEY_THEME4_FONTTITLE, DEF.DEFAULT_FONTTITLE));
		ed.putInt(DEF.KEY_FONTMAIN, DEF.getInt(sp, DEF.KEY_THEME4_FONTMAIN, DEF.DEFAULT_FONTMAIN));
		ed.putInt(DEF.KEY_FONTSUB, DEF.getInt(sp, DEF.KEY_THEME4_FONTSUB, DEF.DEFAULT_FONTSUB));
		ed.putInt(DEF.KEY_FONTTILE, DEF.getInt(sp, DEF.KEY_THEME4_FONTTILE, DEF.DEFAULT_FONTTILE));
		ed.putInt(DEF.KEY_ITEMMRGN, DEF.getInt(sp, DEF.KEY_THEME4_ITEMMRGN, DEF.DEFAULT_ITEMMARGIN));
		ed.putInt(DEF.KEY_TOOLBARSEEK, DEF.getInt(sp, DEF.KEY_THEME4_TOOLBARSEEK, DEF.DEFAULT_TOOLBARSEEK));
		ed.putInt(DEF.KEY_TXTRGB, DEF.getInt(sp, DEF.KEY_THEME4_TXTRGB, mPresetColor[1][0]));
		ed.putInt(DEF.KEY_DIRRGB, DEF.getInt(sp, DEF.KEY_THEME4_DIRRGB, mPresetColor[1][1]));
		ed.putInt(DEF.KEY_BEFRGB, DEF.getInt(sp, DEF.KEY_THEME4_BEFRGB, mPresetColor[1][2]));
		ed.putInt(DEF.KEY_NOWRGB, DEF.getInt(sp, DEF.KEY_THEME4_NOWRGB, mPresetColor[1][3]));
		ed.putInt(DEF.KEY_AFTRGB, DEF.getInt(sp, DEF.KEY_THEME4_AFTRGB, mPresetColor[1][4]));
		ed.putInt(DEF.KEY_RRBRGB, DEF.getInt(sp, DEF.KEY_THEME4_RRBRGB, mPresetColor[1][5]));
		ed.putInt(DEF.KEY_IMGRGB, DEF.getInt(sp, DEF.KEY_THEME4_IMGRGB, mPresetColor[1][6]));
		ed.putInt(DEF.KEY_INFRGB, DEF.getInt(sp, DEF.KEY_THEME4_INFRGB, mPresetColor[1][7]));
		ed.putInt(DEF.KEY_MRKRGB, DEF.getInt(sp, DEF.KEY_THEME4_MRKRGB, mPresetColor[1][8]));
		ed.putInt(DEF.KEY_BAKRGB, DEF.getInt(sp, DEF.KEY_THEME4_BAKRGB, mPresetColor[1][9]));
		ed.putInt(DEF.KEY_CURRGB, DEF.getInt(sp, DEF.KEY_THEME4_CURRGB, mPresetColor[1][10]));
		ed.putInt(DEF.KEY_TITRGB, DEF.getInt(sp, DEF.KEY_THEME4_TITRGB, mPresetColor[1][11]));
		ed.putInt(DEF.KEY_TIBRGB, DEF.getInt(sp, DEF.KEY_THEME4_TIBRGB, mPresetColor[1][12]));
		ed.putInt(DEF.KEY_TLDRGB, DEF.getInt(sp, DEF.KEY_THEME4_TLDRGB, mPresetColor[1][13]));
		ed.putInt(DEF.KEY_TLBRGB, DEF.getInt(sp, DEF.KEY_THEME4_TLBRGB, mPresetColor[1][14]));
		ed.putInt(DEF.KEY_BSFRGB, DEF.getInt(sp, DEF.KEY_THEME4_BSFRGB, mPresetColor[1][15]));
		ed.putInt(DEF.KEY_BSERGB, DEF.getInt(sp, DEF.KEY_THEME4_BSERGB, mPresetColor[1][16]));
		ed.putInt(DEF.KEY_FIFRGB, DEF.getInt(sp, DEF.KEY_THEME4_FIFRGB, mPresetColor[1][17]));
		ed.putInt(DEF.KEY_FIBRGB, DEF.getInt(sp, DEF.KEY_THEME4_FIBRGB, mPresetColor[1][18]));
		ed.putInt(DEF.KEY_EVTRGB, DEF.getInt(sp, DEF.KEY_THEME4_EVTRGB, mPresetColor[1][19]));
		ed.putInt(DEF.KEY_EVBRGB, DEF.getInt(sp, DEF.KEY_THEME4_EVBRGB, mPresetColor[1][20]));
		ed.apply();
	}

	private void getTheme5(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_PRESET, String.valueOf(DEF.getInt(sp, DEF.KEY_THEME5_PRESET, "1")));
		ed.putInt(DEF.KEY_FONTTITLE, DEF.getInt(sp, DEF.KEY_THEME5_FONTTITLE, DEF.DEFAULT_FONTTITLE));
		ed.putInt(DEF.KEY_FONTMAIN, DEF.getInt(sp, DEF.KEY_THEME5_FONTMAIN, DEF.DEFAULT_FONTMAIN));
		ed.putInt(DEF.KEY_FONTSUB, DEF.getInt(sp, DEF.KEY_THEME5_FONTSUB, DEF.DEFAULT_FONTSUB));
		ed.putInt(DEF.KEY_FONTTILE, DEF.getInt(sp, DEF.KEY_THEME5_FONTTILE, DEF.DEFAULT_FONTTILE));
		ed.putInt(DEF.KEY_ITEMMRGN, DEF.getInt(sp, DEF.KEY_THEME5_ITEMMRGN, DEF.DEFAULT_ITEMMARGIN));
		ed.putInt(DEF.KEY_TOOLBARSEEK, DEF.getInt(sp, DEF.KEY_THEME5_TOOLBARSEEK, DEF.DEFAULT_TOOLBARSEEK));
		ed.putInt(DEF.KEY_TXTRGB, DEF.getInt(sp, DEF.KEY_THEME5_TXTRGB, mPresetColor[1][0]));
		ed.putInt(DEF.KEY_DIRRGB, DEF.getInt(sp, DEF.KEY_THEME5_DIRRGB, mPresetColor[1][1]));
		ed.putInt(DEF.KEY_BEFRGB, DEF.getInt(sp, DEF.KEY_THEME5_BEFRGB, mPresetColor[1][2]));
		ed.putInt(DEF.KEY_NOWRGB, DEF.getInt(sp, DEF.KEY_THEME5_NOWRGB, mPresetColor[1][3]));
		ed.putInt(DEF.KEY_AFTRGB, DEF.getInt(sp, DEF.KEY_THEME5_AFTRGB, mPresetColor[1][4]));
		ed.putInt(DEF.KEY_RRBRGB, DEF.getInt(sp, DEF.KEY_THEME5_RRBRGB, mPresetColor[1][5]));
		ed.putInt(DEF.KEY_IMGRGB, DEF.getInt(sp, DEF.KEY_THEME5_IMGRGB, mPresetColor[1][6]));
		ed.putInt(DEF.KEY_INFRGB, DEF.getInt(sp, DEF.KEY_THEME5_INFRGB, mPresetColor[1][7]));
		ed.putInt(DEF.KEY_MRKRGB, DEF.getInt(sp, DEF.KEY_THEME5_MRKRGB, mPresetColor[1][8]));
		ed.putInt(DEF.KEY_BAKRGB, DEF.getInt(sp, DEF.KEY_THEME5_BAKRGB, mPresetColor[1][9]));
		ed.putInt(DEF.KEY_CURRGB, DEF.getInt(sp, DEF.KEY_THEME5_CURRGB, mPresetColor[1][10]));
		ed.putInt(DEF.KEY_TITRGB, DEF.getInt(sp, DEF.KEY_THEME5_TITRGB, mPresetColor[1][11]));
		ed.putInt(DEF.KEY_TIBRGB, DEF.getInt(sp, DEF.KEY_THEME5_TIBRGB, mPresetColor[1][12]));
		ed.putInt(DEF.KEY_TLDRGB, DEF.getInt(sp, DEF.KEY_THEME5_TLDRGB, mPresetColor[1][13]));
		ed.putInt(DEF.KEY_TLBRGB, DEF.getInt(sp, DEF.KEY_THEME5_TLBRGB, mPresetColor[1][14]));
		ed.putInt(DEF.KEY_BSFRGB, DEF.getInt(sp, DEF.KEY_THEME5_BSFRGB, mPresetColor[1][15]));
		ed.putInt(DEF.KEY_BSERGB, DEF.getInt(sp, DEF.KEY_THEME5_BSERGB, mPresetColor[1][16]));
		ed.putInt(DEF.KEY_FIFRGB, DEF.getInt(sp, DEF.KEY_THEME5_FIFRGB, mPresetColor[1][17]));
		ed.putInt(DEF.KEY_FIBRGB, DEF.getInt(sp, DEF.KEY_THEME5_FIBRGB, mPresetColor[1][18]));
		ed.putInt(DEF.KEY_EVTRGB, DEF.getInt(sp, DEF.KEY_THEME5_EVTRGB, mPresetColor[1][19]));
		ed.putInt(DEF.KEY_EVBRGB, DEF.getInt(sp, DEF.KEY_THEME5_EVBRGB, mPresetColor[1][20]));
		ed.apply();
	}

	private void getTheme6(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_PRESET, String.valueOf(DEF.getInt(sp, DEF.KEY_THEME6_PRESET, "1")));
		ed.putInt(DEF.KEY_FONTTITLE, DEF.getInt(sp, DEF.KEY_THEME6_FONTTITLE, DEF.DEFAULT_FONTTITLE));
		ed.putInt(DEF.KEY_FONTMAIN, DEF.getInt(sp, DEF.KEY_THEME6_FONTMAIN, DEF.DEFAULT_FONTMAIN));
		ed.putInt(DEF.KEY_FONTSUB, DEF.getInt(sp, DEF.KEY_THEME6_FONTSUB, DEF.DEFAULT_FONTSUB));
		ed.putInt(DEF.KEY_FONTTILE, DEF.getInt(sp, DEF.KEY_THEME6_FONTTILE, DEF.DEFAULT_FONTTILE));
		ed.putInt(DEF.KEY_ITEMMRGN, DEF.getInt(sp, DEF.KEY_THEME6_ITEMMRGN, DEF.DEFAULT_ITEMMARGIN));
		ed.putInt(DEF.KEY_TOOLBARSEEK, DEF.getInt(sp, DEF.KEY_THEME6_TOOLBARSEEK, DEF.DEFAULT_TOOLBARSEEK));
		ed.putInt(DEF.KEY_TXTRGB, DEF.getInt(sp, DEF.KEY_THEME6_TXTRGB, mPresetColor[1][0]));
		ed.putInt(DEF.KEY_DIRRGB, DEF.getInt(sp, DEF.KEY_THEME6_DIRRGB, mPresetColor[1][1]));
		ed.putInt(DEF.KEY_BEFRGB, DEF.getInt(sp, DEF.KEY_THEME6_BEFRGB, mPresetColor[1][2]));
		ed.putInt(DEF.KEY_NOWRGB, DEF.getInt(sp, DEF.KEY_THEME6_NOWRGB, mPresetColor[1][3]));
		ed.putInt(DEF.KEY_AFTRGB, DEF.getInt(sp, DEF.KEY_THEME6_AFTRGB, mPresetColor[1][4]));
		ed.putInt(DEF.KEY_RRBRGB, DEF.getInt(sp, DEF.KEY_THEME6_RRBRGB, mPresetColor[1][5]));
		ed.putInt(DEF.KEY_IMGRGB, DEF.getInt(sp, DEF.KEY_THEME6_IMGRGB, mPresetColor[1][6]));
		ed.putInt(DEF.KEY_INFRGB, DEF.getInt(sp, DEF.KEY_THEME6_INFRGB, mPresetColor[1][7]));
		ed.putInt(DEF.KEY_MRKRGB, DEF.getInt(sp, DEF.KEY_THEME6_MRKRGB, mPresetColor[1][8]));
		ed.putInt(DEF.KEY_BAKRGB, DEF.getInt(sp, DEF.KEY_THEME6_BAKRGB, mPresetColor[1][9]));
		ed.putInt(DEF.KEY_CURRGB, DEF.getInt(sp, DEF.KEY_THEME6_CURRGB, mPresetColor[1][10]));
		ed.putInt(DEF.KEY_TITRGB, DEF.getInt(sp, DEF.KEY_THEME6_TITRGB, mPresetColor[1][11]));
		ed.putInt(DEF.KEY_TIBRGB, DEF.getInt(sp, DEF.KEY_THEME6_TIBRGB, mPresetColor[1][12]));
		ed.putInt(DEF.KEY_TLDRGB, DEF.getInt(sp, DEF.KEY_THEME6_TLDRGB, mPresetColor[1][13]));
		ed.putInt(DEF.KEY_TLBRGB, DEF.getInt(sp, DEF.KEY_THEME6_TLBRGB, mPresetColor[1][14]));
		ed.putInt(DEF.KEY_BSFRGB, DEF.getInt(sp, DEF.KEY_THEME6_BSFRGB, mPresetColor[1][15]));
		ed.putInt(DEF.KEY_BSERGB, DEF.getInt(sp, DEF.KEY_THEME6_BSERGB, mPresetColor[1][16]));
		ed.putInt(DEF.KEY_FIFRGB, DEF.getInt(sp, DEF.KEY_THEME6_FIFRGB, mPresetColor[1][17]));
		ed.putInt(DEF.KEY_FIBRGB, DEF.getInt(sp, DEF.KEY_THEME6_FIBRGB, mPresetColor[1][18]));
		ed.putInt(DEF.KEY_EVTRGB, DEF.getInt(sp, DEF.KEY_THEME6_EVTRGB, mPresetColor[1][19]));
		ed.putInt(DEF.KEY_EVBRGB, DEF.getInt(sp, DEF.KEY_THEME6_EVBRGB, mPresetColor[1][20]));
		ed.apply();
	}

	private void getTheme7(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_PRESET, String.valueOf(DEF.getInt(sp, DEF.KEY_THEME7_PRESET, "1")));
		ed.putInt(DEF.KEY_FONTTITLE, DEF.getInt(sp, DEF.KEY_THEME7_FONTTITLE, DEF.DEFAULT_FONTTITLE));
		ed.putInt(DEF.KEY_FONTMAIN, DEF.getInt(sp, DEF.KEY_THEME7_FONTMAIN, DEF.DEFAULT_FONTMAIN));
		ed.putInt(DEF.KEY_FONTSUB, DEF.getInt(sp, DEF.KEY_THEME7_FONTSUB, DEF.DEFAULT_FONTSUB));
		ed.putInt(DEF.KEY_FONTTILE, DEF.getInt(sp, DEF.KEY_THEME7_FONTTILE, DEF.DEFAULT_FONTTILE));
		ed.putInt(DEF.KEY_ITEMMRGN, DEF.getInt(sp, DEF.KEY_THEME7_ITEMMRGN, DEF.DEFAULT_ITEMMARGIN));
		ed.putInt(DEF.KEY_TOOLBARSEEK, DEF.getInt(sp, DEF.KEY_THEME7_TOOLBARSEEK, DEF.DEFAULT_TOOLBARSEEK));
		ed.putInt(DEF.KEY_TXTRGB, DEF.getInt(sp, DEF.KEY_THEME7_TXTRGB, mPresetColor[1][0]));
		ed.putInt(DEF.KEY_DIRRGB, DEF.getInt(sp, DEF.KEY_THEME7_DIRRGB, mPresetColor[1][1]));
		ed.putInt(DEF.KEY_BEFRGB, DEF.getInt(sp, DEF.KEY_THEME7_BEFRGB, mPresetColor[1][2]));
		ed.putInt(DEF.KEY_NOWRGB, DEF.getInt(sp, DEF.KEY_THEME7_NOWRGB, mPresetColor[1][3]));
		ed.putInt(DEF.KEY_AFTRGB, DEF.getInt(sp, DEF.KEY_THEME7_AFTRGB, mPresetColor[1][4]));
		ed.putInt(DEF.KEY_RRBRGB, DEF.getInt(sp, DEF.KEY_THEME7_RRBRGB, mPresetColor[1][5]));
		ed.putInt(DEF.KEY_IMGRGB, DEF.getInt(sp, DEF.KEY_THEME7_IMGRGB, mPresetColor[1][6]));
		ed.putInt(DEF.KEY_INFRGB, DEF.getInt(sp, DEF.KEY_THEME7_INFRGB, mPresetColor[1][7]));
		ed.putInt(DEF.KEY_MRKRGB, DEF.getInt(sp, DEF.KEY_THEME7_MRKRGB, mPresetColor[1][8]));
		ed.putInt(DEF.KEY_BAKRGB, DEF.getInt(sp, DEF.KEY_THEME7_BAKRGB, mPresetColor[1][9]));
		ed.putInt(DEF.KEY_CURRGB, DEF.getInt(sp, DEF.KEY_THEME7_CURRGB, mPresetColor[1][10]));
		ed.putInt(DEF.KEY_TITRGB, DEF.getInt(sp, DEF.KEY_THEME7_TITRGB, mPresetColor[1][11]));
		ed.putInt(DEF.KEY_TIBRGB, DEF.getInt(sp, DEF.KEY_THEME7_TIBRGB, mPresetColor[1][12]));
		ed.putInt(DEF.KEY_TLDRGB, DEF.getInt(sp, DEF.KEY_THEME7_TLDRGB, mPresetColor[1][13]));
		ed.putInt(DEF.KEY_TLBRGB, DEF.getInt(sp, DEF.KEY_THEME7_TLBRGB, mPresetColor[1][14]));
		ed.putInt(DEF.KEY_BSFRGB, DEF.getInt(sp, DEF.KEY_THEME7_BSFRGB, mPresetColor[1][15]));
		ed.putInt(DEF.KEY_BSERGB, DEF.getInt(sp, DEF.KEY_THEME7_BSERGB, mPresetColor[1][16]));
		ed.putInt(DEF.KEY_FIFRGB, DEF.getInt(sp, DEF.KEY_THEME7_FIFRGB, mPresetColor[1][17]));
		ed.putInt(DEF.KEY_FIBRGB, DEF.getInt(sp, DEF.KEY_THEME7_FIBRGB, mPresetColor[1][18]));
		ed.putInt(DEF.KEY_EVTRGB, DEF.getInt(sp, DEF.KEY_THEME7_EVTRGB, mPresetColor[1][19]));
		ed.putInt(DEF.KEY_EVBRGB, DEF.getInt(sp, DEF.KEY_THEME7_EVBRGB, mPresetColor[1][20]));
		ed.apply();
	}

	private void getTheme8(SharedPreferences sp) {
		Editor ed = sp.edit();
		ed.putString(DEF.KEY_PRESET, String.valueOf(DEF.getInt(sp, DEF.KEY_THEME8_PRESET, "1")));
		ed.putInt(DEF.KEY_FONTTITLE, DEF.getInt(sp, DEF.KEY_THEME8_FONTTITLE, DEF.DEFAULT_FONTTITLE));
		ed.putInt(DEF.KEY_FONTMAIN, DEF.getInt(sp, DEF.KEY_THEME8_FONTMAIN, DEF.DEFAULT_FONTMAIN));
		ed.putInt(DEF.KEY_FONTSUB, DEF.getInt(sp, DEF.KEY_THEME8_FONTSUB, DEF.DEFAULT_FONTSUB));
		ed.putInt(DEF.KEY_FONTTILE, DEF.getInt(sp, DEF.KEY_THEME8_FONTTILE, DEF.DEFAULT_FONTTILE));
		ed.putInt(DEF.KEY_ITEMMRGN, DEF.getInt(sp, DEF.KEY_THEME8_ITEMMRGN, DEF.DEFAULT_ITEMMARGIN));
		ed.putInt(DEF.KEY_TOOLBARSEEK, DEF.getInt(sp, DEF.KEY_THEME8_TOOLBARSEEK, DEF.DEFAULT_TOOLBARSEEK));
		ed.putInt(DEF.KEY_TXTRGB, DEF.getInt(sp, DEF.KEY_THEME8_TXTRGB, mPresetColor[1][0]));
		ed.putInt(DEF.KEY_DIRRGB, DEF.getInt(sp, DEF.KEY_THEME8_DIRRGB, mPresetColor[1][1]));
		ed.putInt(DEF.KEY_BEFRGB, DEF.getInt(sp, DEF.KEY_THEME8_BEFRGB, mPresetColor[1][2]));
		ed.putInt(DEF.KEY_NOWRGB, DEF.getInt(sp, DEF.KEY_THEME8_NOWRGB, mPresetColor[1][3]));
		ed.putInt(DEF.KEY_AFTRGB, DEF.getInt(sp, DEF.KEY_THEME8_AFTRGB, mPresetColor[1][4]));
		ed.putInt(DEF.KEY_RRBRGB, DEF.getInt(sp, DEF.KEY_THEME8_RRBRGB, mPresetColor[1][5]));
		ed.putInt(DEF.KEY_IMGRGB, DEF.getInt(sp, DEF.KEY_THEME8_IMGRGB, mPresetColor[1][6]));
		ed.putInt(DEF.KEY_INFRGB, DEF.getInt(sp, DEF.KEY_THEME8_INFRGB, mPresetColor[1][7]));
		ed.putInt(DEF.KEY_MRKRGB, DEF.getInt(sp, DEF.KEY_THEME8_MRKRGB, mPresetColor[1][8]));
		ed.putInt(DEF.KEY_BAKRGB, DEF.getInt(sp, DEF.KEY_THEME8_BAKRGB, mPresetColor[1][9]));
		ed.putInt(DEF.KEY_CURRGB, DEF.getInt(sp, DEF.KEY_THEME8_CURRGB, mPresetColor[1][10]));
		ed.putInt(DEF.KEY_TITRGB, DEF.getInt(sp, DEF.KEY_THEME8_TITRGB, mPresetColor[1][11]));
		ed.putInt(DEF.KEY_TIBRGB, DEF.getInt(sp, DEF.KEY_THEME8_TIBRGB, mPresetColor[1][12]));
		ed.putInt(DEF.KEY_TLDRGB, DEF.getInt(sp, DEF.KEY_THEME8_TLDRGB, mPresetColor[1][13]));
		ed.putInt(DEF.KEY_TLBRGB, DEF.getInt(sp, DEF.KEY_THEME8_TLBRGB, mPresetColor[1][14]));
		ed.putInt(DEF.KEY_BSFRGB, DEF.getInt(sp, DEF.KEY_THEME8_BSFRGB, mPresetColor[1][15]));
		ed.putInt(DEF.KEY_BSERGB, DEF.getInt(sp, DEF.KEY_THEME8_BSERGB, mPresetColor[1][16]));
		ed.putInt(DEF.KEY_FIFRGB, DEF.getInt(sp, DEF.KEY_THEME8_FIFRGB, mPresetColor[1][17]));
		ed.putInt(DEF.KEY_FIBRGB, DEF.getInt(sp, DEF.KEY_THEME8_FIBRGB, mPresetColor[1][18]));
		ed.putInt(DEF.KEY_EVTRGB, DEF.getInt(sp, DEF.KEY_THEME8_EVTRGB, mPresetColor[1][19]));
		ed.putInt(DEF.KEY_EVBRGB, DEF.getInt(sp, DEF.KEY_THEME8_EVBRGB, mPresetColor[1][20]));
		ed.apply();
	}
}
