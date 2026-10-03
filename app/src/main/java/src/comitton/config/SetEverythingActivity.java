package src.comitton.config;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import android.preference.CheckBoxPreference;
import androidx.preference.PreferenceManager;

import src.comitton.common.DEF;
import src.comitton.fileview.FileSelectActivity;
import jp.dip.muracoro.comittonx.R;

public class SetEverythingActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private static SharedPreferences sharedPreferences;
	private EditTextPreference mEverythingHost;
	private EditTextPreference mEverythingPort;
	private EditTextPreference mEverythingUser;
	private EditTextPreference mEverythingPass;
	private EditTextPreference mEverythingReplaceFrom;
	private EditTextPreference mEverythingReplaceTo;
	private CheckBoxPreference mEnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		boolean notice = SetCommonActivity.getForceHideStatusBar(sharedPreferences);
		if (notice) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		boolean immEnable = SetCommonActivity.getForceHideNavigationBar(sharedPreferences);
		if (immEnable && android.os.Build.VERSION.SDK_INT >= 19) {
			int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
			uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			getWindow().getDecorView().setSystemUiVisibility(uiOptions);
		}
		SetCommonActivity.SetOrientationEventListener(this, sharedPreferences);

		addPreferencesFromResource(R.xml.seteverything);

		mEverythingHost = (EditTextPreference) getPreferenceScreen().findPreference(DEF.KEY_EVERYTHING_HOST);
		mEverythingPort = (EditTextPreference) getPreferenceScreen().findPreference(DEF.KEY_EVERYTHING_PORT);
		mEverythingUser = (EditTextPreference) getPreferenceScreen().findPreference(DEF.KEY_EVERYTHING_USER);
		mEverythingPass = (EditTextPreference) getPreferenceScreen().findPreference(DEF.KEY_EVERYTHING_PASS);
		mEverythingReplaceFrom = (EditTextPreference) getPreferenceScreen().findPreference(DEF.KEY_EVERYTHING_REPLACE_FROM);
		mEverythingReplaceTo = (EditTextPreference) getPreferenceScreen().findPreference(DEF.KEY_EVERYTHING_REPLACE_TO);
		mEnable = (CheckBoxPreference) findPreference(DEF.KEY_EVERYTHING_ENABLE);

		mEnable.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
				// 親のActivityを再生成させる
				FileSelectActivity.setChangeTheme();
				// trueを返すと設定値が保存される
				return true;
			}
		});
		// バリデーション設定の追加
		if (mEverythingHost != null) {
			mEverythingHost.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String host = (String) newValue;
					if (!isValidIpv4(host)) {
						Toast.makeText(SetEverythingActivity.this, getString(R.string.ValidIPaddress), Toast.LENGTH_SHORT).show();
						// 不正な場合は変更を保存しない
						return false;
					}
					return true;
				}
			});
		}

		if (mEverythingPort != null) {
			mEverythingPort.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String portStr = (String) newValue;
					if (!isValidPort(portStr)) {
						Toast.makeText(SetEverythingActivity.this, getString(R.string.ValidPortaddress), Toast.LENGTH_SHORT).show();
						// 不正な場合は変更を保存しない
						return false;
					}
					return true;
				}
			});
		}

		// ListViewの位置を元に戻す
		ListViewScrollUtils.restorePosition(this, getListView());

		ButtonPreferenceCategory everythingCategory = (ButtonPreferenceCategory) findPreference("everything_category");
		if (everythingCategory != null) {
			everythingCategory.setOnButtonClickListener(() -> {
				// 初期設定に戻す
				SharedPreferences.Editor ed = sharedPreferences.edit();
				ed.putBoolean(DEF.KEY_EVERYTHING_ENABLE, false);
				ed.putString(DEF.KEY_EVERYTHING_HOST, DEF.DEFAULT_EVERYTHING_HOST);
				ed.putString(DEF.KEY_EVERYTHING_PORT, DEF.DEFAULT_EVERYTHING_PORT);
				ed.putString(DEF.KEY_EVERYTHING_USER, DEF.DEFAULT_EVERYTHING_USER);
				ed.putString(DEF.KEY_EVERYTHING_PASS, DEF.DEFAULT_EVERYTHING_PASS);
				ed.putString(DEF.KEY_EVERYTHING_REPLACE_FROM, DEF.DEFAULT_EVERYTHING_REPLACE_FROM);
				ed.putString(DEF.KEY_EVERYTHING_REPLACE_TO, DEF.DEFAULT_EVERYTHING_REPLACE_TO);
				ed.apply();
				// アクティビティを再起動
				ListViewScrollUtils.restartActivityWithPosition(this, getListView());
			});
		}
		// 画面の描画準備が終わった直後に画面内の全タイトル部品を探して制限を解除する
		PreferenceHelper.enableMultilineTitles(getWindow().getDecorView());
	}

	@Override
	protected void onResume() {
		super.onResume();
		SetCommonActivity.SetOrientationEventListenerEnable(sharedPreferences);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		updateSummaries();
	}

	@Override
	protected void onPause() {
		super.onPause();
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		SetCommonActivity.SetOrientationEventListenerDisable(sharedPreferences);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updateSummaries();
	}

	private void updateSummaries() {
		// ホスト
		String host = sharedPreferences.getString(DEF.KEY_EVERYTHING_HOST, DEF.DEFAULT_EVERYTHING_HOST);
		String hostVal = host.isEmpty() ? "" : host;
		mEverythingHost.setSummary(getString(R.string.EverythingHost) + getString(R.string.SetValue) + hostVal);

		// ポート
		String port = sharedPreferences.getString(DEF.KEY_EVERYTHING_PORT, DEF.DEFAULT_EVERYTHING_PORT);
		mEverythingPort.setSummary(getString(R.string.EverythingPort) + getString(R.string.SetValue) + port);

		// ユーザー名
		String user = sharedPreferences.getString(DEF.KEY_EVERYTHING_USER, DEF.DEFAULT_EVERYTHING_USER);
		String userVal = user.isEmpty() ? "" : user;
		mEverythingUser.setSummary(getString(R.string.EverythingUser) + getString(R.string.SetValue) + userVal);

		// パスワード
		String pass = sharedPreferences.getString(DEF.KEY_EVERYTHING_PASS, DEF.DEFAULT_EVERYTHING_PASS);
		String passVal = pass.isEmpty() ? "" : "********";
		mEverythingPass.setSummary(getString(R.string.EverythingPass) + getString(R.string.SetValue) + passVal);

		// プレフィックス
		String replacefrom = sharedPreferences.getString(DEF.KEY_EVERYTHING_REPLACE_FROM, DEF.DEFAULT_EVERYTHING_REPLACE_FROM);
		mEverythingReplaceFrom.setSummary(getString(R.string.EverythingReplaceFrom) + getString(R.string.SetValue) + replacefrom);

		// プレフィックス
		String replaceto = sharedPreferences.getString(DEF.KEY_EVERYTHING_REPLACE_TO, DEF.DEFAULT_EVERYTHING_REPLACE_TO);
		mEverythingReplaceTo.setSummary(getString(R.string.EverythingReplaceTo) + getString(R.string.SetValue) + replaceto);
	}

	public static boolean getEverythingEanble(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EVERYTHING_ENABLE, false);
		return flag;
	}

	// バリデーション用ヘルパーメソッド
	private boolean isValidPort(String portStr) {
		if (portStr == null || portStr.isEmpty()) {
			return false;
		}
		try {
			int port = Integer.parseInt(portStr);
			return port >= 1 && port <= 65535;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	// IPv4アドレス専用の検証メソッド
	private boolean isValidIpv4(String host) {
		if (host == null || host.isEmpty()) {
			// 空欄を許容する場合はtrue(空欄不可にしたい場合はfalseを返す)
			return false;
		}
		// ドットで分割(空要素も保持)
		String[] parts = host.split("\\.", -1);
		if (parts.length != 4) {
			// 4つに分かれない場合はIPアドレスではない
			return false;
		}
		try {
			for (String part : parts) {
				// 空文字(例: "192..1.1")や、不自然な先頭ゼロ(例: "01" 等を厳密に弾きたい場合など)のチェック
				if (part.isEmpty() || (part.length() > 1 && part.startsWith("0"))) {
					return false;
				}
				int val = Integer.parseInt(part);
				if (val < 0 || val > 255) {
					// 各オクテットが0～255の範囲外
					return false;
				}
			}
			return true;
		}
		catch (NumberFormatException e) {
			// 数字以外の文字が含まれている場合
			return false;
		}
	}
}
