package src.comitton.common;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import src.comitton.config.SetConfigActivity;
import src.comitton.fileview.FileSelectActivity;
import src.comitton.textview.EpubWebViewActivity;
import src.comitton.webview.WebViewActivity;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 2つの別プロセス用サービスを裏で起動する
		startService(new Intent(this, SplashActivity.ConfigService.class));
		startService(new Intent(this, SplashActivity.WebViewService.class));
		// メイン画面を起動
		Intent intent = new Intent(SplashActivity.this, FileSelectActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(intent);
		finish();
		// 描画せず即終了
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
			overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0);
			overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0);
		}
		else {
			overridePendingTransition(0, 0);
		}
	}
	// 設定画面プロセス用のサービス(内部クラス化)
	public static class ConfigService extends Service {
		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			stopSelf();
			return START_NOT_STICKY;
		}
		@Override
		public IBinder onBind(Intent intent) { return null; }
	}
	// WebViewプロセス用のサービス(内部クラス化)
	public static class WebViewService extends Service {
		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			stopSelf();
			return START_NOT_STICKY;
		}
		@Override
		public IBinder onBind(Intent intent) { return null; }
	}
}
