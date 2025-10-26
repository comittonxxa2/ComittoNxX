package src.comitton.config;

import src.comitton.config.seekbar.MemNextSeekbar;
import src.comitton.config.seekbar.MemPrevSeekbar;
import src.comitton.config.seekbar.MemSizeSeekbar;
import src.comitton.helpview.HelpActivity;
import src.comitton.common.DEF;
import src.comitton.config.SetCommonActivity;
import jp.dip.muracoro.comittonx.R;
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

public class SetCacheActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private MemSizeSeekbar mMemSize;
	private MemNextSeekbar mMemNext;
	private MemPrevSeekbar mMemPrev;
	private ListPreference mMemCacheStartThreshold;

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;

	public static final int[] MemCache =
		{ R.string.memcache00		// 自動
		, R.string.memcache01		// 10%
		, R.string.memcache02		// 20%
		, R.string.memcache03		// 30%
		, R.string.memcache04		// 40%
		, R.string.memcache05		// 50%
		, R.string.memcache06		// 60%
		, R.string.memcache07		// 70%
		, R.string.memcache08		// 80%
		, R.string.memcache09 };	// 90%

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		mNotice = SetCommonActivity.getForceHideStatusBar(mSharedPreferences);
		if (mNotice) {
			// 通知領域非表示
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		mImmEnable = SetCommonActivity.getForceHideNavigationBar(mSharedPreferences);
		if (mImmEnable && mSdkVersion >= 19) {
			int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
				uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
				uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
				getWindow().getDecorView().setSystemUiVisibility(uiOptions);
		}
		SetCommonActivity.SetOrientationEventListener(this, mSharedPreferences);

		addPreferencesFromResource(R.xml.cache);

		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

		mMemSize  = (MemSizeSeekbar)getPreferenceScreen().findPreference(DEF.KEY_MEMSIZE);
		mMemNext  = (MemNextSeekbar)getPreferenceScreen().findPreference(DEF.KEY_MEMNEXT);
		mMemPrev  = (MemPrevSeekbar)getPreferenceScreen().findPreference(DEF.KEY_MEMPREV);
		mMemCacheStartThreshold = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_MEMCACHESTARTTHRESHOLD);

		// 項目選択
		PreferenceScreen onlineHelp = (PreferenceScreen) findPreference(DEF.KEY_CACHEHELP);
		onlineHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// Activityの遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_cache);	// 設定画面
				Intent intent;
				intent = new Intent(SetCacheActivity.this, HelpActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("Url", url);
				startActivity(intent);
				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		// シークバー
		mMemSize.setSummary(getMemSizeSummary(sharedPreferences));	// 使用メモリサイズ
		mMemNext.setSummary(getMemNextSummary(sharedPreferences));	// 次ページ数
		mMemPrev.setSummary(getMemPrevSummary(sharedPreferences));	// 前ページ数
		mMemCacheStartThreshold.setSummary(getMemCacheSummary(sharedPreferences));
		SetCommonActivity.SetOrientationEventListenerEnable();
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		SetCommonActivity.SetOrientationEventListenerDisable();

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if(key.equals(DEF.KEY_MEMSIZE)){
			// 使用メモリサイズ
			mMemSize.setSummary(getMemSizeSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_MEMNEXT)){
			// 次ページ数
			mMemNext.setSummary(getMemNextSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_MEMPREV)){
			// 前ページ数
			mMemPrev.setSummary(getMemPrevSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_MEMCACHESTARTTHRESHOLD)){
			mMemCacheStartThreshold.setSummary(getMemCacheSummary(sharedPreferences));
		}
	}

	// 設定の読込
	public static int getMemSize(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_MEMSIZE, DEF.DEFAULT_MEMSIZE);
		return num;
	}

	public static int getMemNext(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_MEMNEXT, DEF.DEFAULT_MEMNEXT);
		return num;
	}

	public static int getMemPrev(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_MEMPREV, DEF.DEFAULT_MEMPREV);
		return num;
	}

	public static int getMemCache(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_MEMCACHESTARTTHRESHOLD, DEF.DEFAULT_MEMCACHE);
		return num;
	}

	private String getMemSizeSummary(SharedPreferences sharedPreferences){
		int val = getMemSize(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.mSizeSumm1);
		String summ2 = res.getString(R.string.mSizeSumm2);

		return	DEF.getMemSizeStr(val, summ1, summ2);
	}

	private String getMemNextSummary(SharedPreferences sharedPreferences){
		int val = getMemNext(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.mPageSumm1);

		return	DEF.getMemPageStr(val, summ1);
	}

	private String getMemPrevSummary(SharedPreferences sharedPreferences){
		int val = getMemPrev(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.mPageSumm1);

		return	DEF.getMemPageStr(val, summ1);
	}

	private String getMemCacheSummary(SharedPreferences sharedPreferences){
		int val = getMemCache(sharedPreferences);
		Resources res = getResources();
		return	res.getString(MemCache[val]);
	}
}
