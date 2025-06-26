package src.comitton.config;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.EditTextPreference;

import src.comitton.common.Logcat;
import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;

public class SetCustomUrlSchemeActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	Resources mResources;

	private EditTextPreference mPriorityWord;
	private String getkey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.customurlscheme);

		PackageManager packageManager = this.getPackageManager();
		try {
			// パッケージ名を取得
			packageManager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e) {
			// 取得不可エラー(有り得ないが入れておく)
		}

		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		// 初期値を読み出す
		getkey = sharedPreferences.getString(DEF.KEY_CUSTOM_URL_SCHEME_KEY, "");
		if (getkey.equals("")) {
			// 初期値が空白だった場合
			Editor ed = sharedPreferences.edit();
			// パッケージ名を入れて書き込む
			ed.putString(DEF.KEY_CUSTOM_URL_SCHEME_KEY, this.getPackageName());
			ed.apply();
			// 設定を反映させるため再描画
			recreate();
		}

		mPriorityWord = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_CUSTOM_URL_SCHEME_KEY);
		// 値の表示更新
		mPriorityWord.setSummary(getkey);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(DEF.KEY_CUSTOM_URL_SCHEME_KEY)) {
			// 該当するキーだった場合
			String value = sharedPreferences.getString(DEF.KEY_CUSTOM_URL_SCHEME_KEY, "");
			if (value.equals("")) {
				// 空白だった場合は初期値を入れる
				value = getkey;
				Editor ed = sharedPreferences.edit();
				// 初期値を書き込む
				ed.putString(DEF.KEY_CUSTOM_URL_SCHEME_KEY, value);
				ed.apply();
				// 設定を反映させるため再描画
				recreate();
			}
			// 初期値を更新
			getkey = value;
			// 値の表示更新
			mPriorityWord.setSummary(value);
		}
	}
}
