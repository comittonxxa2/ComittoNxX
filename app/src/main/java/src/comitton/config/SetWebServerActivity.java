package src.comitton.config;

import static src.comitton.WebSharing.NetworkUtils.getWiFiIpAddress;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.WindowManager;

import android.preference.ListPreference;
import android.widget.Button;
import android.widget.CheckBox;

import android.preference.CheckBoxPreference;

import android.preference.Preference;
import android.widget.Toast;

import android.preference.EditTextPreference;
import android.preference.CheckBoxPreference;
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
import src.comitton.WebSharing.WebServerService;
import src.comitton.fileview.FileSelectActivity;

public class SetWebServerActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;
	private static SharedPreferences sharedPreferences;
	private Preference dirPreference;
	private Preference statusPreference;
	private boolean mServerStart = false;
	private EditTextPreference mWebserverPort;
	private EditTextPreference mBulkdownload;
	private String mStartpath = null;
	private Intent intent;
	private DownloadPreference downloadPref;
	private CheckBoxPreference mServerStartEnable;
	private CheckBoxPreference mPopupEnable;

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

		addPreferencesFromResource(R.xml.webserver);

		mWebserverPort = (EditTextPreference) getPreferenceScreen().findPreference(DEF.KEY_WEBSERVER_PORT);

		if (mWebserverPort != null) {
			// ポート番号の設定
			mWebserverPort.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String portStr = (String) newValue;
					if (!isValidPort(portStr)) {
						Toast.makeText(SetWebServerActivity.this, getString(R.string.ValidPortaddress), Toast.LENGTH_SHORT).show();
						// 不正な場合は変更を保存しない
						return false;
					}
					return true;
				}
			});
		}

		mBulkdownload = (EditTextPreference) getPreferenceScreen().findPreference(DEF.KEY_WEBSERVER_BULKDOWNLOAD);

		if (mBulkdownload != null) {
			// ZIP圧縮してダウンロード時のファイル名の設定
			mBulkdownload.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String portStr = (String) newValue;
					if (!isValidBulkDownload(portStr)) {
						Toast.makeText(SetWebServerActivity.this, getString(R.string.ValidFilename), Toast.LENGTH_SHORT).show();
						// 不正な場合は変更を保存しない
						return false;
					}
					return true;
				}
			});
		}
		mServerStartEnable = (CheckBoxPreference) findPreference(DEF.KEY_WEBSERVER_START);
		mPopupEnable = (CheckBoxPreference) findPreference(DEF.KEY_WEBSERVER_POPUPIMAGE);

		setStatus();

		dirPreference = findPreference("pref_dir_selector");
		if (dirPreference != null) {
			// Web共有の初期フォルダの選択
			mStartpath = getWebserverPathSummary(sharedPreferences);
			dirPreference.setSummary(mStartpath);
			dirPreference.setOnPreferenceClickListener(preference -> {
				Intent intent = new Intent(this, DirChooserActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("START_PATH", mStartpath);
				// DirChooserActivityを起動
				startActivityForResult(intent, DEF.APP_STORAGE_ACCESS_REQUEST_CODE);
				return true;
			});
		}

		// ボタンのクリックリスナー処理
		downloadPref = (DownloadPreference) findPreference(DEF.KEY_DOWNLOADSOURCECODE);
		if (downloadPref != null) {
			// Web共有サービス開始/停止
			if (FileSelectActivity.getWebServerEnable()) {
				downloadPref.setButtonText(R.string.StopService);
			}
			else {
				downloadPref.setButtonText(R.string.StartService);
			}
			downloadPref.setOnButtonClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// クリックされたら実行
					SetService();
				}
			});
		}
		if (FileSelectActivity.getWebServerEnable()) {
			// Web共有サービス中は選択させない
			setEnable(false);
		}

		// ListViewの位置を元に戻す
		ListViewScrollUtils.restorePosition(this, getListView());

		ButtonPreferenceCategory webviewCategory = (ButtonPreferenceCategory) findPreference("webserver_category");
		if (webviewCategory != null) {
			webviewCategory.setOnButtonClickListener(() -> {
				// 初期設定に戻す
				if (FileSelectActivity.getWebServerEnable()) {
					// Web共有サービスを停止させる
					SetService();
					downloadPref.setButtonText(R.string.StopService);
				}
				SharedPreferences.Editor ed = sharedPreferences.edit();
				ed.putBoolean(DEF.KEY_WEBSERVER_START, false);
				ed.putString(DEF.KEY_WEBSERVER_PORT, DEF.DEFAULT_WEBSERVER_PORT);
				ed.putString(DEF.KEY_WEBSERVER_DIRECTORY, FileSelectActivity.getPath());
				ed.putString(DEF.KEY_WEBSERVER_BULKDOWNLOAD, DEF.DEFAULT_WEBSERVER_BULKDOWNLOAD);
				ed.putBoolean(DEF.KEY_WEBSERVER_POPUPIMAGE, false);
				ed.apply();
				// アクティビティを再起動
				ListViewScrollUtils.restartActivityWithPosition(this, getListView());
			});
		}
		// 画面の描画準備が終わった直後に画面内の全タイトル部品を探して制限を解除する
		PreferenceHelper.enableMultilineTitles(getWindow().getDecorView());
	}

	private void SetService() {
		// Web共有サービス開始/停止
		if (FileSelectActivity.getWebServerEnable()) {
			// Web共有サービス停止
			downloadPref.setButtonText(R.string.StartService);
			intent = new Intent(this, WebServerService.class);
			stopService(intent);
			android.widget.Toast.makeText(this, R.string.StopWebServer, android.widget.Toast.LENGTH_SHORT).show();
			FileSelectActivity.setWebServerEnable(false);
			// Web共有サービス停止中は選択させる
			setEnable(true);
		}
		else {
			// Web共有サービス開始
			downloadPref.setButtonText(R.string.StopService);
			intent = new Intent(this, WebServerService.class);
			intent.putExtra("PORT", Integer.parseInt(getWebserverPort(sharedPreferences)));
			intent.putExtra("PATH", getWebserverPath(sharedPreferences));
			intent.putExtra("BULKDOWNLOAD", getBulkDownload(sharedPreferences));
			intent.putExtra("POPUPIMAGE", getPopupImage(sharedPreferences));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				// Android 8.0以降はフォアグラウンドサービス専用の起動メソッドを使用
				startForegroundService(intent);
			}
			else {
				startService(intent);
			}
			android.widget.Toast.makeText(this, R.string.StartWebServer, android.widget.Toast.LENGTH_SHORT).show();
			FileSelectActivity.setWebServerEnable(true);
			// Web共有サービス中は選択させない
			setEnable(false);
		}
		setStatus();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SetCommonActivity.SetOrientationEventListenerEnable(sharedPreferences);
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		mWebserverPort.setSummary(getWebserverPortSummary(sharedPreferences));
		dirPreference.setSummary(getWebserverPathSummary(sharedPreferences));
		mBulkdownload.setSummary(getBulkDownloadSummary(sharedPreferences));	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		SetCommonActivity.SetOrientationEventListenerDisable(sharedPreferences);
	}

	// DirChooserActivityから戻ってきたときの処理を受け取る
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 起動時に使ったリクエストコードと、結果が OK かどうかを判定
		if (requestCode == DEF.APP_STORAGE_ACCESS_REQUEST_CODE && resultCode == RESULT_OK) {
			if (data != null) {
				// DirChooserActivity側で putExtra したキー名でパスを取り出す
				String selectedPath = data.getStringExtra("EXTRA_SELECTED_PATH");
				if (selectedPath != null && !selectedPath.equals(mStartpath)) {
					// 選択されたフォルダパスを保存
					Toast.makeText(this, getString(R.string.SelectedPath) + " " + selectedPath, Toast.LENGTH_LONG).show();
					SharedPreferences.Editor ed = sharedPreferences.edit();
					ed.putString(DEF.KEY_WEBSERVER_DIRECTORY, selectedPath);
					ed.apply();
					dirPreference.setSummary(selectedPath);
				}
			}
		}
	}

	private void setEnable(boolean value) {
		mServerStartEnable.setEnabled(value);
		mWebserverPort.setEnabled(value);
		mBulkdownload.setEnabled(value);
		mPopupEnable.setEnabled(value);
		dirPreference.setEnabled(value);
	}

	// バリデーション用ヘルパーメソッド
	private boolean isValidPort(String portStr) {
		if (portStr == null || portStr.isEmpty()) {
			return false;
		}
		try {
			int port = Integer.parseInt(portStr);
			return port >= 1024 && port <= 65535;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
	private boolean isValidBulkDownload(String portStr) {
		if (portStr == null || portStr.isEmpty()) {
			return false;
		}
		return true;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(DEF.KEY_WEBSERVER_PORT)){
			mWebserverPort.setSummary(getWebserverPortSummary(sharedPreferences));
		}
		if(key.equals(DEF.KEY_WEBSERVER_BULKDOWNLOAD)){
			mBulkdownload.setSummary(getBulkDownloadSummary(sharedPreferences));
		}
	}

	private void setStatus() {
		statusPreference = findPreference("status_info");
		if (statusPreference != null) {
			statusPreference.setSummary(getString(R.string.IPaddrtess) + " " + getWiFiIpAddress(this) + "\n" + getString(R.string.PortNumber) + " " + getWebserverPort(sharedPreferences) + "\n" + ((FileSelectActivity.getWebServerEnable()) ? getString(R.string.inoperation) : getString(R.string.Stopped)));
		}
	}

	// 設定の読込
	public static String getWebserverPort(SharedPreferences sharedPreferences){
		String val = sharedPreferences.getString(DEF.KEY_WEBSERVER_PORT, DEF.DEFAULT_WEBSERVER_PORT);
		return val;
	}
	public static String getWebserverPath(SharedPreferences sharedPreferences){
		String val = sharedPreferences.getString(DEF.KEY_WEBSERVER_DIRECTORY, FileSelectActivity.getPath());
		return val;
	}
	public static String getBulkDownload(SharedPreferences sharedPreferences){
		String val = sharedPreferences.getString(DEF.KEY_WEBSERVER_BULKDOWNLOAD, DEF.DEFAULT_WEBSERVER_BULKDOWNLOAD);
		return val;
	}
	public static boolean getWebServerStart(SharedPreferences sharedPreferences) {
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_WEBSERVER_START, false);
		return flag;
	}
	public static boolean getPopupImage(SharedPreferences sharedPreferences) {
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_WEBSERVER_POPUPIMAGE, false);
		return flag;
	}
	// 設定の読込(定義変更中)
	private String getWebserverPortSummary(SharedPreferences sharedPreferences){
		String val = getWebserverPort(sharedPreferences);
		return val;
	}
	private String getWebserverPathSummary(SharedPreferences sharedPreferences){
		String val = getWebserverPath(sharedPreferences);
		return val;
	}
	private String getBulkDownloadSummary(SharedPreferences sharedPreferences){
		String val = getBulkDownload(sharedPreferences);
		return val;
	}

}
