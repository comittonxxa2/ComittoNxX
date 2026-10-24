package src.comitton.WebSharing;

import static src.comitton.WebSharing.NetworkUtils.getWiFiIpAddress;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import fi.iki.elonen.NanoHTTPD;
import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.fileview.FileSelectActivity;
import src.comitton.common.Logcat;

public class WebServerService extends Service {

	private WebFilerServer mWebServer;
	private static final String defaultTheme = "dark";
	private static final String CHANNEL_ID = "WebServiceChannel";
	private static final int NOTIFICATION_ID = 1001;
	private static final String PREF_NAME = "WebServerServicePrefs";
	private static final String KEY_PATH = "Path";
	private static final String KEY_PORT = "Port";
	private static final String KEY_BULKDOWNLOAD = "Bulkdownload";
	private static final String KEY_POPUPIMAGE = "Popupimage";

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "onStartCommand()");
		String rootPath;
		int mPort;
		String bulkdownload;
		SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);		boolean popupimage;
		// intentがnullの場合(START_STICKYによる自動再起動時)
		if (intent == null) {
			Logcat.v(logLevel, "Intent is null. Restoring settings from SharedPreferences.");
			// SharedPreferencesから前回保存した設定を読み込む
			rootPath = prefs.getString(KEY_PATH, null);
			mPort = prefs.getInt(KEY_PORT, Integer.parseInt(DEF.DEFAULT_WEBSERVER_PORT));
			bulkdownload = prefs.getString(KEY_BULKDOWNLOAD, null);
			popupimage = prefs.getBoolean(KEY_POPUPIMAGE, false);
			// 保存データ(PATHなど)すら無い場合は起動できないため安全に終了
			if (rootPath == null) {
				Logcat.v(logLevel, "No saved path found. Stopping service.");
				stopSelf(startId);
				return START_NOT_STICKY;
			}
		}
		else {
			rootPath = intent.getStringExtra("PATH");
			mPort = intent.getIntExtra("PORT",  Integer.parseInt(DEF.DEFAULT_WEBSERVER_PORT));
			bulkdownload = intent.getStringExtra("BULKDOWNLOAD");
			popupimage = intent.getBooleanExtra("POPUPIMAGE", false);
			// 次回クラッシュ・再起動時に備えてSharedPreferencesに値を保存しておく
			prefs.edit()
				.putString(KEY_PATH, rootPath)
				.putInt(KEY_PORT, mPort)
				.putString(KEY_BULKDOWNLOAD, bulkdownload)
				.putBoolean(KEY_POPUPIMAGE, popupimage)
				.apply();
		}
		// フォアグラウンドサービス用の通知を作成して常駐させる
		createNotificationChannel();
		Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
			.setContentTitle(getString(R.string.notificationWebServerTitle))
			.setContentText(getString(R.string.notificationWebServerText) + " " + getString(R.string.IPaddrtess) + " " + getWiFiIpAddress(this) + " " + getString(R.string.PortNumber) + " " + mPort)
			// 適宜アプリのアイコンに変更
			.setSmallIcon(android.R.drawable.ic_menu_info_details)
			.setOngoing(true)
			.build();

		// Android 14以降の仕様に配慮したフォアグラウンドサービスの開始
		startForeground(NOTIFICATION_ID, notification);
		try {
			// 公開するルートパス
			Logcat.v(logLevel, "rootPath : " + rootPath + ", Port : " + mPort);
			mWebServer = new WebFilerServer(mPort, this, rootPath, defaultTheme, bulkdownload, popupimage);
			mWebServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// 万が一OSに強制終了された場合に自動で再起動させたい場合
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// サービス終了時にサーバーを確実にストップ
		if (mWebServer != null) {
			mWebServer.stop();
			mWebServer = null;
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		// バインドは使用しないため null
		return null;
	}

	// Android 8.0以降に必要な通知チャンネルの作成
	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.notificationChannel), NotificationManager.IMPORTANCE_DEFAULT);
			channel.setDescription(getString(R.string.notificationWebServer));
			channel.enableVibration(false);
			channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
			NotificationManager manager = getSystemService(NotificationManager.class);
			if (manager != null) {
				manager.createNotificationChannel(channel);
			}
		}
	}
}
