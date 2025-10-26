package src.comitton.helpview;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import src.comitton.config.SetCommonActivity;
import src.comitton.cropimageview.CropImageActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class HelpActivity extends AppCompatActivity {

    private WebView mWebView;

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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

		CropImageActivity.SetOrientationEventListener(this, sharedPreferences);

        // Intentを取得する
        Intent intent = getIntent();
        String url = intent.getStringExtra("Url");

        mWebView = (WebView) new WebView(this);
        mWebView.loadUrl("file:///android_asset/" + url);
        setContentView(mWebView);

    }

	@Override
	protected void onResume() {
		super.onResume();
		// バックグラウンドからフォアグランドに戻った時
		CropImageActivity.SetOrientationEventListenerEnable();
	}
	@Override
	protected void onPause() {
		super.onPause();
		CropImageActivity.SetOrientationEventListenerDisable();
	}
}
