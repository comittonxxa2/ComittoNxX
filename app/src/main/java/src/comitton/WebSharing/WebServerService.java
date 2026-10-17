package src.comitton.WebSharing;

import static src.comitton.WebSharing.NetworkUtils.getWiFiIpAddress;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
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

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, "onStartCommand()");
		String rootPath = intent.getStringExtra("PATH");
		int mPort = intent.getIntExtra("PORT",  Integer.parseInt(DEF.DEFAULT_WEBSERVER_PORT));
		String bulkdownload = intent.getStringExtra("BULKDOWNLOAD");
		boolean popupimage = intent.getBooleanExtra("POPUPIMAGE", false);
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
