package src.comitton.webview;

import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;

import android.content.Intent;
import android.content.SharedPreferences;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AppCompatActivity;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.Logcat;
import src.comitton.common.DEF;
import src.comitton.fileaccess.FileAccess;
import src.comitton.dialog.MenuDialog.MenuSelectListener;
import src.comitton.imageview.PageSelectListener;
import src.comitton.dialog.BookmarkDialog.BookmarkListenerInterface;

import android.os.Handler;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

@SuppressLint("NewApi")
public class WebViewActivity extends AppCompatActivity {

	private static final String TAG = "WebViewActivity";

	private boolean mNotice = false;
	private boolean mNoSleep = false;

	// ファイル情報
	/** 選択したサーバのインデックス */
	private int mServer;
	/** URI(サーバのTOPディレクトリ) */
	private String mURI = "";
	private String mUser = "";
	private String mPass = "";
	/** ベースURIからの相対パス名 */
	private String mPath = "";
	/** ZIP指定時 */
	private String mFileName = "";
	private String mTextName;
	/** URIとパス */
	private String mUriPath = "";
	/** URIとパスと圧縮ファイル */
	private String mFilePath = "";

	private WebView mywebView;
	private WebViewActivity mActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		mActivity = this;

		super.onCreate(savedInstanceState);

		// タイトル非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		if (mNotice) {
			// 通知領域非表示
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		if (mNoSleep) {
			// スリープしない
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		// Intentを取得する
		Intent intent = getIntent();
		// Intentに保存されたデータを取り出す
		mServer = intent.getIntExtra("Server", -1);	// サーバ選択番号
		//mHost = intent.getStringExtra("Uri");
		mURI = intent.getStringExtra("Uri");					// ベースディレクトリのuri
		mPath = intent.getStringExtra("Path");				// ベースURIからの相対パス名
		mUser = intent.getStringExtra("User");				// SMB認証用
		mPass = intent.getStringExtra("Pass");				// SMB認証用
		mFileName = intent.getStringExtra("File");			// ZIP指定時
		mTextName = intent.getStringExtra("Text");				// テキストファイル名

		Logcat.d(logLevel, "mServer=" + mServer + ", mURI=" + mURI + ", mPath=" + mPath + ", mUser=" + mUser + ", mPass=" + mPass
				+ ", mFileName=" + mFileName + ", mTextName=" + mTextName);

		if (mPath == null) {
			// パス名が未定なら戻る
			return;
		}

		// 最後に保存したファイル用
		mUriPath = DEF.relativePath(mActivity, mURI, mPath);
		if (mFileName.isEmpty()) {
			// 圧縮ファイルじゃなければパスのURLを解決する
			mFilePath = DEF.relativePath(mActivity, mUriPath, mTextName);
		}
		else {
			// 圧縮ファイルなら中身のファイル名を連結する
			mFilePath = DEF.relativePath(mActivity, mUriPath, mFileName) + mTextName;;
		}

		Logcat.d(logLevel, "mFilePath=" + mFilePath);
		// WebViewのレイアウトを設定
		setContentView(R.layout.webview);
		// WebViewを有効にする
 		mywebView = (WebView) findViewById(R.id.Webview);
 		// ERR_ACCESS_DENIEDを出さないようにする
 		WebSettings webViewSettings = mywebView.getSettings();
 		webViewSettings.setAllowFileAccess(true);
		// 表示サイズを合わせる
		mywebView.getSettings().setLoadWithOverviewMode(true);
 		mywebView.getSettings().setUseWideViewPort(true);
 		mywebView.setInitialScale(1);
		// スクロールバーをWebView内に含める
		mywebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
 		// WebView内のピンチズームを許可
		mywebView.getSettings().setBuiltInZoomControls(true);
		// WebView内のJavaScriptの実行を許可
		mywebView.getSettings().setJavaScriptEnabled(true);
		// 外部ブラウザを利用しない
		mywebView.setWebViewClient(new WebViewClient());
		// URLを読み込む
		mywebView.loadUrl(mFilePath); 
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e){
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 戻るボタンがタップされた時
			if (mywebView != null && mywebView.canGoBack()) {
				// 閲覧履歴があるなら一つ前のウェブページを表示する
				mywebView.goBack();
			}
			else {
				// 閲覧履歴が無ければ戻る
				return super.onKeyDown(keyCode, e);
			}
			return true;
		}else{
			// 他のボタンの場合は戻る
			return super.onKeyDown(keyCode, e);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// バックグラウンドからフォアグランドに戻った時
		if (mywebView != null) {
			// WebViewが空でなければ現在のウェブページを再表示する
			String url = mywebView.getUrl();
			mywebView.loadUrl(url);
		}
	}
}
