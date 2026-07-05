package src.comitton.helpview;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import src.comitton.common.MultiProcessPreferences;
import src.comitton.config.SetCommonActivity;
import src.comitton.cropimageview.CropImageActivity;
import src.comitton.fileview.FileSelectActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class HelpActivity extends AppCompatActivity {

    private WebView mWebView;

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;
	private static SharedPreferences sharedPreferences;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			try {
				WebView.setDataDirectorySuffix("webview_process");
			}
			catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
		sharedPreferences = MultiProcessPreferences.getInstance(this);

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

		// 全画面(Edge-to-Edge)を可能な限り抑制する
		androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
		View root = findViewById(android.R.id.content);
		androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
			// 直接数値を取り出す
			int l = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()).left;
			int t = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()).top;
			int r = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()).right;
			int b = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()).bottom;
			// この箱自体に余白を持たせることで中のView(onTouchEventを持つViewを押し戻す
			v.setPadding(l, t, r, b);
			return androidx.core.view.WindowInsetsCompat.CONSUMED;
		});

		if (!SetCommonActivity.getFalseDisplayViewRotate(sharedPreferences)) {
			CropImageActivity.SetOrientationEventListener(this, sharedPreferences);
		}

        // Intentを取得する
        Intent intent = getIntent();
        String url = intent.getStringExtra("Url");

        mWebView = (WebView) new WebView(this);
		SharedPreferences sharedPreferences = MultiProcessPreferences.getInstance(this);
		FileSelectActivity.applyAppTheme(sharedPreferences);
        mWebView.loadUrl("file:///android_asset/" + url);
        setContentView(mWebView);

    }

	@Override
	protected void onResume() {
		super.onResume();
		// バックグラウンドからフォアグランドに戻った時
		CropImageActivity.SetOrientationEventListenerEnable(sharedPreferences);
	}
	@Override
	protected void onPause() {
		super.onPause();
		CropImageActivity.SetOrientationEventListenerDisable(sharedPreferences);
	}
}
