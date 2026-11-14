package src.comitton.common;

import android.app.Application;
import android.os.Build;
import android.webkit.WebView;

// 「メニュー」の「操作ヘルプ」と「ComittoNxXについて/バージョン情報」を続けて選択するとクラッシュするので作成した
public class StartApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		// Android9(API28)以降のマルチプロセス環境でのデータ競合を防ぐ
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			String processName = getProcessName();
			// メインプロセス以外で動く場合、プロセス名毎に固有のWebViewデータディレクトリを自動割り当て
			if (!getPackageName().equals(processName)) {
				WebView.setDataDirectorySuffix(processName);
			}
		}
	}
}
