package src.comitton.config;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import android.preference.CheckBoxPreference;
import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import src.comitton.common.DEF;
import src.comitton.fileview.FileSelectActivity;
import jp.dip.muracoro.comittonx.R;

public class SetBookmarkSyncActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private static SharedPreferences sharedPreferences;
	private EditTextPreference mBookmarkSyncHost;
	private EditTextPreference mBookmarkSyncPort;
	private EditTextPreference mBookmarkSyncUser;
	private EditTextPreference mBookmarkSyncPass;
	private CheckBoxPreference mBookmarkSyncEnable;
	private CheckBoxPreference mReadPositionSyncEnable;
	private CheckBoxPreference mHistorySyncEnable;
	private CheckBoxPreference mLibrarySyncEnable;

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

		addPreferencesFromResource(R.xml.setbookmarksync);

		mBookmarkSyncHost = (EditTextPreference) getPreferenceScreen().findPreference(DEF.KEY_BOOKMARKSYNC_HOST);
		mBookmarkSyncPort = (EditTextPreference) getPreferenceScreen().findPreference(DEF.KEY_BOOKMARKSYNC_PORT);
		mBookmarkSyncUser = (EditTextPreference) getPreferenceScreen().findPreference(DEF.KEY_BOOKMARKSYNC_USER);
		mBookmarkSyncPass = (EditTextPreference) getPreferenceScreen().findPreference(DEF.KEY_BOOKMARKSYNC_PASS);
		mBookmarkSyncEnable = (CheckBoxPreference) findPreference(DEF.KEY_BOOKMARKSYNC_ENABLE);
		mReadPositionSyncEnable = (CheckBoxPreference) findPreference(DEF.KEY_READPOSITIONSYNC_ENABLE);
		mHistorySyncEnable = (CheckBoxPreference) findPreference(DEF.KEY_HISTORYSYNC_ENABLE);
		mLibrarySyncEnable = (CheckBoxPreference) findPreference(DEF.KEY_LIBRARYSYNC_ENABLE);

		mBookmarkSyncEnable.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
				// 親のActivityを再生成させる
				FileSelectActivity.setChangeTheme();
				// trueを返すと設定値が保存される
				return true;
			}
		});
		mReadPositionSyncEnable.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
				// 親のActivityを再生成させる
				FileSelectActivity.setChangeTheme();
				// trueを返すと設定値が保存される
				return true;
			}
		});
		mHistorySyncEnable.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
				// 親のActivityを再生成させる
				FileSelectActivity.setChangeTheme();
				// trueを返すと設定値が保存される
				return true;
			}
		});
		mLibrarySyncEnable.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
				// 親のActivityを再生成させる
				FileSelectActivity.setChangeTheme();
				// trueを返すと設定値が保存される
				return true;
			}
		});
		// バリデーション設定の追加
		if (mBookmarkSyncHost != null) {
			mBookmarkSyncHost.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String host = (String) newValue;
					if (!isValidIpv4(host)) {
						Toast.makeText(SetBookmarkSyncActivity.this, getString(R.string.ValidIPaddress), Toast.LENGTH_SHORT).show();
						// 不正な場合は変更を保存しない
						return false;
					}
					return true;
				}
			});
		}

		if (mBookmarkSyncPort != null) {
			mBookmarkSyncPort.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String portStr = (String) newValue;
					if (!isValidPort(portStr)) {
						Toast.makeText(SetBookmarkSyncActivity.this, getString(R.string.ValidPortaddress), Toast.LENGTH_SHORT).show();
						// 不正な場合は変更を保存しない
						return false;
					}
					return true;
				}
			});
		}
		// ボタンのクリックリスナー処理
		DownloadPreference downloadPref = (DownloadPreference) findPreference(DEF.KEY_DOWNLOADSOURCECODE);
		if (downloadPref != null) {
			downloadPref.setOnButtonClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// assetsフォルダからソースコードを読み込む
					String sourceCodeString = loadTextFromAssets(DEF.SERVER_SAMPLE_CODE);
					if (sourceCodeString == null) {
						Toast.makeText(SetBookmarkSyncActivity.this, R.string.LoadErrorServerCode, Toast.LENGTH_SHORT).show();
						return;
					}
					// 元々用意されているアプリのローカルフォルダに保存する
					boolean success = saveTextToDownloadFolder(sourceCodeString, DEF.SERVER_SAMPLE_CODE);
					if (success) {
						Toast.makeText(SetBookmarkSyncActivity.this, R.string.SaveServerCode, Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(SetBookmarkSyncActivity.this, R.string.SaveErrorServerCode, Toast.LENGTH_SHORT).show();
					}
				}
			});
		}
		// ListViewの位置を元に戻す
		ListViewScrollUtils.restorePosition(this, getListView());

		ButtonPreferenceCategory bookmarksyncCategory = (ButtonPreferenceCategory) findPreference("bookmarksync_category");
		if (bookmarksyncCategory != null) {
			bookmarksyncCategory.setOnButtonClickListener(() -> {
				// 初期設定に戻す
				SharedPreferences.Editor ed = sharedPreferences.edit();
				ed.putBoolean(DEF.KEY_BOOKMARKSYNC_ENABLE, false);
				ed.putBoolean(DEF.KEY_READPOSITIONSYNC_ENABLE, false);
				ed.putBoolean(DEF.KEY_HISTORYSYNC_ENABLE, false);
				ed.putBoolean(DEF.KEY_LIBRARYSYNC_ENABLE, false);
				ed.putString(DEF.KEY_BOOKMARKSYNC_HOST, DEF.DEFAULT_BOOKMARKSYNC_HOST);
				ed.putString(DEF.KEY_BOOKMARKSYNC_PORT, DEF.DEFAULT_BOOKMARKSYNC_PORT);
				ed.putString(DEF.KEY_BOOKMARKSYNC_USER, DEF.DEFAULT_BOOKMARKSYNC_USER);
				ed.putString(DEF.KEY_BOOKMARKSYNC_PASS, DEF.DEFAULT_BOOKMARKSYNC_PASS);
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
		String host = sharedPreferences.getString(DEF.KEY_BOOKMARKSYNC_HOST, DEF.DEFAULT_BOOKMARKSYNC_HOST);
		String hostVal = host.isEmpty() ? "" : host;
		mBookmarkSyncHost.setSummary(getString(R.string.BookmarkSyncHost) + getString(R.string.SetValue) + hostVal);

		// ポート
		String port = sharedPreferences.getString(DEF.KEY_BOOKMARKSYNC_PORT, DEF.DEFAULT_BOOKMARKSYNC_PORT);
		mBookmarkSyncPort.setSummary(getString(R.string.BookmarkSyncPort) + getString(R.string.SetValue) + port);

		// ユーザー名
		String user = sharedPreferences.getString(DEF.KEY_BOOKMARKSYNC_USER, DEF.DEFAULT_BOOKMARKSYNC_USER);
		String userVal = user.isEmpty() ? "" : user;
		mBookmarkSyncUser.setSummary(getString(R.string.BookmarkSyncUser) + getString(R.string.SetValue) + userVal);

		// パスワード
		String pass = sharedPreferences.getString(DEF.KEY_BOOKMARKSYNC_PASS, DEF.DEFAULT_BOOKMARKSYNC_PASS);
		String passVal = pass.isEmpty() ? "" : "********";
		mBookmarkSyncPass.setSummary(getString(R.string.BookmarkSyncPass) + getString(R.string.SetValue) + passVal);
	}

	public static boolean getBookmarkSyncEanble(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_BOOKMARKSYNC_ENABLE, false);
		return flag;
	}
	public static boolean getReadPositionSyncEnable(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_READPOSITIONSYNC_ENABLE, false);
		return flag;
	}
	public static boolean getHistorySyncEnable(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_HISTORYSYNC_ENABLE, false);
		return flag;
	}
	public static boolean getLibrarySyncEnable(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_LIBRARYSYNC_ENABLE, false);
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
	// Android 7～16 対応：テキストデータを共有のダウンロードフォルダに保存する
	private boolean saveTextToDownloadFolder(String text, String fileName) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			// Android10(API29)～Android16以降
			ContentResolver resolver = getContentResolver();
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
			values.put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream");
			values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
			Uri uri = null;
			try {
				uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);
				if (uri == null) {
					return false;
				}
				try (OutputStream os = resolver.openOutputStream(uri)) {
					if (os != null) {
						os.write(text.getBytes("UTF-8"));
						os.flush();
						return true;
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				if (uri != null) {
					resolver.delete(uri, null, null);
				}
			}
			return false;
		}
		else {
			// Android7(API24)～Android9(API28)
			try {
				File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				if (!downloadDir.exists()) {
					downloadDir.mkdirs();
				}
				File file = new File(downloadDir, fileName);
				try (FileOutputStream fos = new FileOutputStream(file)) {
					fos.write(text.getBytes("UTF-8"));
					fos.flush();
					return true;
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	// assetsフォルダ内のテキストファイルをStringとして読み込む
	private String loadTextFromAssets(String fileName) {
		StringBuilder stringBuilder = new StringBuilder();
		AssetManager assetManager = getAssets();
		try (InputStream inputStream = assetManager.open(fileName);
	         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
		
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line).append("\n");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	
		return stringBuilder.toString();
	}
}
