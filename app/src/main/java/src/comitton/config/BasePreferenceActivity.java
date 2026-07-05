package src.comitton.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import src.comitton.common.Logcat;

// アプリ内のすべての設定画面(PreferenceActivity)の土台となる共通クラス
public class BasePreferenceActivity extends PreferenceActivity {

	@Override
	protected void attachBaseContext(Context newBase) {
		// 共通のSharedPreferencesから現在のテーマ設定を取得
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(newBase);
		int themeValue = SetCommonActivity.getSelectTheme(sp);
		Configuration overrideConfig = new Configuration(newBase.getResources().getConfiguration());
		// 各モードに合わせてuiModeを強制上書きする
		if (themeValue == 0) {
			overrideConfig.uiMode = (overrideConfig.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | Configuration.UI_MODE_NIGHT_NO;
		}
		else if (themeValue == 1) {
			overrideConfig.uiMode = (overrideConfig.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | Configuration.UI_MODE_NIGHT_YES;
		}
		// 上書きした環境変数を適用
		Context context = newBase.createConfigurationContext(overrideConfig);
		super.attachBaseContext(context);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// 引数の型を PreferenceScreen に合わせて直接渡す
		if (getPreferenceScreen() != null) {
			setupListPreferences(getPreferenceScreen());
	    }
	}
	private void setupListPreferences(android.preference.PreferenceScreen preferenceScreen) {
		for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
			android.preference.Preference preference = preferenceScreen.getPreference(i);
			if (preference instanceof android.preference.ListPreference) {
				final android.preference.ListPreference listPref = (android.preference.ListPreference) preference;
				// 初回の表示更新
				updateSummary(listPref, listPref.getValue());
				// 変更時のその場反映リスナーを自動登録
				listPref.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(android.preference.Preference pref, Object newValue) {
						updateSummary(listPref, newValue.toString());
						return true;
					}
				});
			}
			else if (preference instanceof android.preference.PreferenceCategory) {
				// PreferenceCategoryの中身を走査する
				setupListPreferencesFromCategory((android.preference.PreferenceCategory) preference);
			}
		}
	}
	// PreferenceCategory内を走査するヘルパー
	private void setupListPreferencesFromCategory(android.preference.PreferenceCategory category) {
		for (int i = 0; i < category.getPreferenceCount(); i++) {
			android.preference.Preference preference = category.getPreference(i);
			if (preference instanceof android.preference.ListPreference) {
				final android.preference.ListPreference listPref = (android.preference.ListPreference) preference;
				updateSummary(listPref, listPref.getValue());
				listPref.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(android.preference.Preference pref, Object newValue) {
						updateSummary(listPref, newValue.toString());
						return true;
					}
				});
			}
		}
	}
	private void updateSummary(android.preference.ListPreference listPref, String value) {
		if (value == null) return;
		int index = listPref.findIndexOfValue(value);
		if (index >= 0) {
		    listPref.setSummary(listPref.getEntries()[index]);
		}
	}
}
