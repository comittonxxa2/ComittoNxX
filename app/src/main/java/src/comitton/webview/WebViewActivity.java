package src.comitton.webview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicColorMatrix;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.renderscript.ScriptIntrinsicLUT;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.HttpAuthHandler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;

import android.content.Intent;
import android.content.SharedPreferences;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Base64;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.Logcat;
import src.comitton.common.DEF;
import src.comitton.config.SetWebViewActivity;
import src.comitton.cropimageview.CropImageActivity;
import src.comitton.dialog.ImageConfigDialog;
import src.comitton.dialog.TabDialogFragment;
import src.comitton.fileaccess.FileAccess;
import src.comitton.dialog.MenuDialog.MenuSelectListener;
import src.comitton.imageview.PageSelectListener;
import src.comitton.dialog.BookmarkDialog.BookmarkListenerInterface;
import src.comitton.config.SetCommonActivity;
import src.comitton.config.SetImageActivity;

import android.os.Handler;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupMenu;

@SuppressLint("NewApi")
public class WebViewActivity extends AppCompatActivity implements MenuSelectListener {

	private static final String TAG = "WebViewActivity";

	private boolean mNotice = false;
	private boolean mNoSleep = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;
	private static SharedPreferences sharedPreferences;

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

	private ImageConfigDialog mImageConfigDialog;

	private static SharedPreferences mSharedPreferences;
	private boolean mWebviewFilter;
	private boolean mWebviewUserAgent;
	private boolean mWebviewPulldownMenu;
	private int mWebviewPulldownTapPosition;
	private float[] mColorMatrix;
	private int mContrast;
	private int mHue;
	private int mSaturation;
	private int mSharpen;
	private boolean mInvert;
	private boolean mGray;
	private boolean mColoring;
	private int mGamma;
	private int mBright;
	private boolean mIsConfSave;

	// RenderScriptの再利用に用いる
	private RenderScript mRS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		mActivity = this;

		super.onCreate(savedInstanceState);

		// RenderScriptを使用する準備
		mRS = RenderScript.create(this);
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		// タイトル非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		mNotice = SetCommonActivity.getForceHideStatusBar(sharedPreferences);
		if (mNotice) {
			// 通知領域非表示
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		if (mNoSleep) {
			// スリープしない
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		mImmEnable = SetCommonActivity.getForceHideNavigationBar(sharedPreferences);
		if (mImmEnable && mSdkVersion >= 19) {
			int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
				uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
				uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
				getWindow().getDecorView().setSystemUiVisibility(uiOptions);
		}
		CropImageActivity.SetOrientationEventListener(mActivity, sharedPreferences);

		mWebviewFilter = SetWebViewActivity.getWebviewFilter(mSharedPreferences);
		mWebviewUserAgent = SetWebViewActivity.getWebviewUserAgent(mSharedPreferences);
		mWebviewPulldownMenu = SetWebViewActivity.getWebviewPulldownMenu(mSharedPreferences);
		mWebviewPulldownTapPosition = SetWebViewActivity.getPulldownTap(mSharedPreferences);

		// フィルターの値の読み込み(ImageConfigDialog.onClick()と同じ補正を行う)
		mGray = SetWebViewActivity.getWebviewGray(mSharedPreferences);
		mInvert = SetWebViewActivity.getWebviewInvert(mSharedPreferences);
		mColoring = SetWebViewActivity.getWebviewColoring(mSharedPreferences);
		mSharpen = SetWebViewActivity.getWebviewSharpen(mSharedPreferences);
		mBright = SetWebViewActivity.getWebviewBright(mSharedPreferences) - 5;
		mGamma = SetWebViewActivity.getWebviewGamma(mSharedPreferences) - 5;
		mContrast = SetWebViewActivity.getWebviewContrast(sharedPreferences) * 5;
		mHue = (SetWebViewActivity.getWebviewHue(sharedPreferences) - 20) * 5;
		mSaturation = SetWebViewActivity.getWebviewSaturation(sharedPreferences) * 5;
		mIsConfSave = true;

		SetColorEffect();

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
			finish();
			return;
		}

		// 最後に保存したファイル用
		mUriPath = DEF.relativePath(mActivity, mURI, mPath);
		if (mFileName == null || mFileName.isEmpty()) {
			// 圧縮ファイルじゃなければパスのURLを解決する
			mFilePath = DEF.relativePath(mActivity, mUriPath, mTextName);
		}
		else {
			// 圧縮ファイルなら中身のファイル名を連結する
			mFilePath = DEF.relativePath(mActivity, mUriPath, mFileName) + mTextName;
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
		webViewSettings.setBuiltInZoomControls(true);
		// WebView内のJavaScriptの実行を許可
		webViewSettings.setJavaScriptEnabled(true);
		// DOM Storage を有効にする
		webViewSettings.setDomStorageEnabled(true);
		// 混合コンテンツ(HTTP/HTTPS)を許可
		webViewSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		// User-AgentをPC版に偽装する
		if (mWebviewUserAgent) {
			String desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
			webViewSettings.setUserAgentString(desktopUA);
		}
		// ブラウザの補助機能を処理
        mywebView.setWebChromeClient(new WebChromeClient());
		// 透明Viewを見つける
		View topTouchArea;
		switch (mWebviewPulldownTapPosition) {
			case 1:
				topTouchArea = findViewById(R.id.area_left);
				break;
			case 2:
				topTouchArea = findViewById(R.id.area_right);
				break;
			default:
				topTouchArea = findViewById(R.id.area_center);
				break;
		}
		// タップされた時の動作
		if (mWebviewPulldownMenu) {
			topTouchArea.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showImageConfigDialog(DEF.MENU_WEBIMGCONF);
				}
			});
		}
		// 外部ブラウザを利用しない
		if (!mWebviewFilter) {
			mywebView.setWebViewClient(new WebViewClient());
		}
		else {
			// キャッシュを無効化して常にshouldInterceptRequestを通るようにする
			webViewSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
			mywebView.setWebViewClient(new WebViewClient() {
				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
					String url = request.getUrl().toString();
					String urlLower = url.toLowerCase();
					// 画像ファイルへのリクエストを判定
					if (urlLower.endsWith(".jpg") || urlLower.endsWith(".jpeg") || urlLower.endsWith(".png") || urlLower.endsWith(".webp") || urlLower.endsWith(".gif")) {
						InputStream inputStream = null;
						try {
							if (urlLower.startsWith("file://")) {
								// パス解析
								String path = URLDecoder.decode(url.replaceFirst("(?i)^file://", ""), "UTF-8");
								inputStream = FileAccess.getInputStream(mActivity, path, mUser, mPass);
							} else if (urlLower.startsWith("http")) {
								inputStream = new URL(url).openStream();
							}
							if (inputStream != null) {
								Bitmap original = BitmapFactory.decodeStream(inputStream);
								inputStream.close();
								if (original != null) {
									// 画像フィルタリング処理を実行
									Bitmap processed = convertBitmapData(original);
									ByteArrayOutputStream out = new ByteArrayOutputStream();
									// MimeTypeと圧縮形式の決定
									Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
									String mimeType = "image/jpeg";
									if (urlLower.endsWith(".png") || urlLower.endsWith(".gif")) {
										format = Bitmap.CompressFormat.PNG;
										mimeType = "image/png";
									} else if (urlLower.endsWith(".webp")) {
										format = Bitmap.CompressFormat.WEBP;
										mimeType = "image/webp";
									}
									processed.compress(format, 100, out);
									InputStream resultStream = new ByteArrayInputStream(out.toByteArray());
									// 解放
									if (processed != original) {
										processed.recycle();
									}
									original.recycle();
									return new WebResourceResponse(mimeType, "UTF-8", resultStream);
								}
							}
						} catch (Exception e) {
							// 処理にエラーがあればここへ飛んでくる(何もしない)
							Logcat.e(TAG, e);
							return null;
						}
					}
					return super.shouldInterceptRequest(view, request);
				}
			});
		}
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
		CropImageActivity.SetOrientationEventListenerEnable(sharedPreferences);
		if (mywebView != null) {
			// WebViewが空でなければ現在のウェブページを再表示する
			String url = mywebView.getUrl();
			mywebView.loadUrl(url);
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		CropImageActivity.SetOrientationEventListenerDisable(sharedPreferences);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mRS != null) {
			mRS.destroy();
		}
	}

	// エラーを解消するためのメソッド
	@Override
	public void onCloseMenuDialog() {
	}

	// エラーを解消するためのメソッド
	@Override
	public void onSelectMenuDialog(int position) {
    }

	private void showImageConfigDialog(int command_id) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mImageConfigDialog != null) {
			return;
		}
		mImageConfigDialog = new ImageConfigDialog(this, R.style.MyDialog, command_id, false, this);

		mImageConfigDialog.setConfig(mGray, mInvert, false, false, mSharpen, mBright, mGamma, 0, 0, 0, 0, 0, 0, mIsConfSave, 0, mContrast, mHue, mSaturation, mColoring, 0);
		mImageConfigDialog.setImageConfigListner(new ImageConfigDialog.ImageConfigListenerInterface() {
			@Override
			public void onButtonSelect(int select, boolean gray, boolean invert, boolean moire, boolean topsingle, int sharpen, int bright, int gamma, int bklight, int algomode, int dispmode, int scalemode, int mgncut, int mgncutcolor, boolean issave, int displayposition, int contrast, int hue, int saturation, boolean coloring, int scrollmode) {
				// 選択状態を通知
				boolean ischange = false;
				// 変更があるかを確認(適用後のキャンセルの場合も含む)
				if (mGray != gray || mInvert != invert || mSharpen != sharpen || mBright != bright || mGamma != gamma || mContrast != contrast || mHue != hue || mSaturation != saturation || mColoring != coloring) {
					ischange = true;
				}
				mGray = gray;
				mColoring = coloring;
				mInvert = invert;
				mSharpen = sharpen;
				mBright = bright;
				mGamma = gamma;
				mContrast = contrast;
				mHue = hue;
				mSaturation = saturation;
				mIsConfSave = issave;

				if (mywebView != null) {
					// リロード
					SetColorEffect();
					mywebView.reload();
				}
				if (issave) {
					// 設定を保存(ImageConfigDialog.onCreateView()と同じ補正を行う)
					SharedPreferences.Editor ed = mSharedPreferences.edit();
					ed.putBoolean(DEF.KEY_WEBVIEWGRAY, mGray);
					ed.putBoolean(DEF.KEY_WEBVIEWCOLORING, mColoring);
					ed.putBoolean(DEF.KEY_WEBVIEWINVERT, mInvert);
					ed.putInt(DEF.KEY_WEBVIEWSHARPEN, mSharpen);
					ed.putInt(DEF.KEY_WEBVIEWBRIGHT, mBright + 5);
					ed.putInt(DEF.KEY_WEBVIEWGAMMA, mGamma + 5);
					ed.putInt(DEF.KEY_WEBVIEWCONTRAST, mContrast / 5);
					ed.putInt(DEF.KEY_WEBVIEWHUE, mHue / 5 + 20);
					ed.putInt(DEF.KEY_WEBVIEWSATURATION, mSaturation / 5);

					ed.apply();
				}
			}

			@Override
			public void onClose() {
				// 終了
				mImageConfigDialog = null;
			}
		});
		mImageConfigDialog.show(getSupportFragmentManager(), TabDialogFragment.class.getSimpleName());
	}

	// ネイティブコードと同等の動作をJavaで記述
	private Bitmap convertBitmapData(Bitmap original) {
		// 現在のビットマップを保持する変数(Mutableコピー)
		Bitmap current = original.copy(Bitmap.Config.ARGB_8888, true);
		// 3x3 畳み込みスクリプトの準備(シャープネス)
		if (mSharpen != 0) {
			int w = current.getWidth();
			int h = current.getHeight();
			// 一旦2倍に拡大(バイリニアフィルタがかかる)
			current = Bitmap.createScaledBitmap(current, w * 2, h * 2, true);
			// ビットマップの出力結果の場所を確保
			Bitmap next = Bitmap.createBitmap(current.getWidth(), current.getHeight(), current.getConfig());
			// 入力を設定
			Allocation input = Allocation.createFromBitmap(mRS, current);
			// 出力先を設定
			Allocation output = Allocation.createFromBitmap(mRS, next);
			// 3x3 畳み込みスクリプトの準備
			ScriptIntrinsicConvolve3x3 convolve = ScriptIntrinsicConvolve3x3.create(mRS, Element.U8_4(mRS));
			// シャープネスのパラメータ調整
			float a = (float) mSharpen; 
			float[] sharpnessKernel = {
				-a / 16f, -2 * a / 16f, -a / 16f,
				-2 * a / 16f, (16f + 12f * a) / 16f, -2 * a / 16f,
				-a / 16f, -2 * a / 16f, -a / 16f
			};
			// シャープネスを設定
			convolve.setCoefficients(sharpnessKernel);
			convolve.setInput(input);
			convolve.forEach(output);
			// 結果を出力
			output.copyTo(next);
			// 解放
			input.destroy();
			output.destroy();
			convolve.destroy();
			current.recycle();
			// 処理後、元のサイズに縮小して戻す
			current = Bitmap.createScaledBitmap(next, w, h, true);
			next.recycle();
		}
		// グレースケール/自動着色
		if (mGray || mColoring) {
			// ビットマップの出力結果の場所を確保
			Bitmap next = Bitmap.createBitmap(current.getWidth(), current.getHeight(), current.getConfig());
			// 入力を設定
			Allocation input = Allocation.createFromBitmap(mRS, current);
			// 出力先を設定
			Allocation output = Allocation.createFromBitmap(mRS, next);
			ScriptIntrinsicColorMatrix grayScript = ScriptIntrinsicColorMatrix.create(mRS, Element.U8_4(mRS));
			// グレースケールを設定
			grayScript.setGreyscale(); 
			// グレースケールを通す
			grayScript.forEach(input, output);
			if (mColoring) {
				// 自動着色の場合
				// 4x4 ルックアップテーブルの準備
				ScriptIntrinsicLUT lutScript = ScriptIntrinsicLUT.create(mRS, Element.U8_4(mRS));
				for (int i = 0; i < 256; i++) {
					lutScript.setRed(i, RED_DATA[i] & 0xFF);
					lutScript.setGreen(i, GREEN_DATA[i] & 0xFF);
					lutScript.setBlue(i, BLUE_DATA[i] & 0xFF);
					// アルファは維持
					lutScript.setAlpha(i, i);
				}
				// ルックアップテーブルの入力を設定
				Allocation inputLut = Allocation.createTyped(mRS, output.getType());
				inputLut.copyFrom(next); 
				// ルックアップテーブルを通す
				lutScript.forEach(output, inputLut);
				// 結果を出力
				inputLut.copyTo(next);
				// 解放
				lutScript.destroy();
				inputLut.destroy();
			} else {
				// グレースケールはそのまま
				// 結果を出力
				output.copyTo(next);
			}
			// 解放
			input.destroy();
			output.destroy();
			grayScript.destroy();
			current.recycle();
			// 結果を書き込む
			current = next;
		}
		// 色反転
		if (mInvert) {
			// ビットマップの出力結果の場所を確保
			Bitmap next = Bitmap.createBitmap(current.getWidth(), current.getHeight(), current.getConfig());
			// 色反転のパラメータ
			float[] invertMatrix = {
				-1.0f, 0, 0, 0, 255,
				0, -1.0f, 0, 0, 255,
				0, 0, -1.0f,  0, 255,
				0, 0, 0, 1.0f, 0
			};
			// マトリックスを作成
			Paint paint = new Paint();
			paint.setColorFilter(new ColorMatrixColorFilter(invertMatrix));
			Canvas canvas = new Canvas(next);
			// マトリックスを設定
			canvas.drawBitmap(current, 0, 0, paint);
			current.recycle();
			// 結果を書き込む
			current = next;
		}
		// 明るさとガンマを設定
		if (mGamma != 0 || mBright != 0) {
			// ビットマップの出力結果の場所を確保
			Bitmap next = Bitmap.createBitmap(current.getWidth(), current.getHeight(), current.getConfig());
			// 入力を設定
			Allocation input = Allocation.createFromBitmap(mRS, current);
			// 出力先を設定
			Allocation output = Allocation.createFromBitmap(mRS, next);
			// 4x4 ルックアップテーブルの準備
			ScriptIntrinsicLUT lutScript = ScriptIntrinsicLUT.create(mRS, Element.U8_4(mRS));
			double f = 1.0 / (1.0 + (double) mGamma * 0.1);
			double scale = (mBright < 0) ? (1.0 + mBright * 0.1) : (1.0 - mBright * 0.1);
			double base = (mBright < 0) ? 0.0 : (255.0 * (1.0 - scale));
			for (int i = 0; i < 256; i++) {
				double val = (Math.pow(((double) i / 255.0), f) * 255.0) * scale + base;
				int finalVal = Math.max(0, Math.min(255, (int) val));
				// RGB各チャンネルに同じ値を設定
				lutScript.setRed(i, finalVal);
				lutScript.setGreen(i, finalVal);
				lutScript.setBlue(i, finalVal);
				// アルファはそのまま通す
				lutScript.setAlpha(i, i);
			}
			// ルックアップテーブルを通す
			lutScript.forEach(input, output);
			// 結果を出力
			output.copyTo(next);
			// 解放
			input.destroy();
			output.destroy();
			lutScript.destroy();
			current.recycle();
			// 結果を書き込む
			current = next;
		}
		// カラーマトリックス
		// ビットマップの出力結果の場所を確保
		Bitmap finalBitmap = Bitmap.createBitmap(current.getWidth(), current.getHeight(), current.getConfig());
		// カラーマトリックスを作成
		Paint paint = new Paint();
		paint.setColorFilter(new ColorMatrixColorFilter(mColorMatrix));
		Canvas canvas = new Canvas(finalBitmap);
		// カラーマトリックスを設定
		canvas.drawBitmap(current, 0, 0, paint);
		current.recycle();
		// 結果を返す
		return finalBitmap;
	}

	// フィルター設定	
	private void SetColorEffect() {
		float cont = (float)mContrast * 0.02f;
		float sat = (float)mSaturation * 0.01f;
		// コントラストを変更
		float translate = (0.5f - 0.5f * cont) * 255.f;
		float[] values = {
			cont, 0, 0, 0, translate,
			0, cont, 0, 0, translate,
			0, 0, cont, 0, translate,
			0, 0, 0, 1, 0
		};
		ColorMatrix cm = new ColorMatrix(values);
		// 彩度を変更
		ColorMatrix saturationCM = new ColorMatrix();
		saturationCM.setSaturation(sat);
		// 連結する
		cm.postConcat(saturationCM);
		ColorMatrix hueCM = new ColorMatrix();
		// 色相を変更
		float hueRad = (float) (mHue * Math.PI / 180.0);
		float cosVal = (float) Math.cos(hueRad);
		float sinVal = (float) Math.sin(hueRad);
		// ITU-R BT.709 輝度の重み付け係数
		float lumR = 0.213f;
		float lumG = 0.715f;
		float lumB = 0.072f;
		// 輝度を維持したまま色相を回転させる
		float[] hueMatrix = {
			lumR + cosVal * (1 - lumR) + sinVal * (-lumR), lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,
			lumR + cosVal * (-lumR) + sinVal * (0.143f), lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
			lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * (lumG), lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0,
			0, 0, 0, 1, 0
		};
		hueCM.set(hueMatrix);
		// 連結する
		cm.postConcat(hueCM);
		// マトリックスを取り出す
		mColorMatrix = cm.getArray();
	}

	// 疑似四色刷りトーンカーブデータ
	// トーンカーブで急激に変化する部分を徐々に変化するようにしてみた
	private static final byte[] RED_DATA = {
		(byte)0x00, (byte)0x01, (byte)0x02, (byte)0x04, (byte)0x05, (byte)0x07, (byte)0x08, (byte)0x0A, (byte)0x0B, (byte)0x0D, (byte)0x0F, (byte)0x10, (byte)0x12, (byte)0x13, (byte)0x15, (byte)0x16,
		(byte)0x18, (byte)0x19, (byte)0x1B, (byte)0x1C, (byte)0x1D, (byte)0x1F, (byte)0x20, (byte)0x22, (byte)0x23, (byte)0x24, (byte)0x26, (byte)0x27, (byte)0x28, (byte)0x29, (byte)0x2A, (byte)0x2B,
		(byte)0x2C, (byte)0x2D, (byte)0x2E, (byte)0x2F, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x35, (byte)0x35, (byte)0x36, (byte)0x36, (byte)0x37,
		(byte)0x37, (byte)0x37, (byte)0x37, (byte)0x38, (byte)0x38, (byte)0x38, (byte)0x38, (byte)0x38, (byte)0x38, (byte)0x38, (byte)0x38, (byte)0x38, (byte)0x38, (byte)0x37, (byte)0x37, (byte)0x37,
		(byte)0x37, (byte)0x36, (byte)0x36, (byte)0x35, (byte)0x35, (byte)0x34, (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x2F, (byte)0x2E, (byte)0x2D, (byte)0x2C,
		(byte)0x2B, (byte)0x2A, (byte)0x29, (byte)0x28, (byte)0x27, (byte)0x27, (byte)0x26, (byte)0x25, (byte)0x24, (byte)0x23, (byte)0x23, (byte)0x22, (byte)0x21, (byte)0x21, (byte)0x20, (byte)0x1F,
		(byte)0x1F, (byte)0x1F, (byte)0x1F, (byte)0x1F, (byte)0x1F, (byte)0x1F, (byte)0x20, (byte)0x20, (byte)0x21, (byte)0x22, (byte)0x23, (byte)0x24, (byte)0x25, (byte)0x26, (byte)0x27, (byte)0x28,
		(byte)0x2A, (byte)0x2C, (byte)0x2E, (byte)0x30, (byte)0x32, (byte)0x34, (byte)0x37, (byte)0x39, (byte)0x3C, (byte)0x3E, (byte)0x41, (byte)0x44, (byte)0x47, (byte)0x4A, (byte)0x4D, (byte)0x50,
		(byte)0x53, (byte)0x56, (byte)0x59, (byte)0x5C, (byte)0x5F, (byte)0x62, (byte)0x65, (byte)0x69, (byte)0x6C, (byte)0x6F, (byte)0x73, (byte)0x76, (byte)0x79, (byte)0x7C, (byte)0x80, (byte)0x83,
		(byte)0x86, (byte)0x89, (byte)0x8C, (byte)0x8F, (byte)0x91, (byte)0x94, (byte)0x96, (byte)0x99, (byte)0x9B, (byte)0x9E, (byte)0xA0, (byte)0xA2, (byte)0xA4, (byte)0xA6, (byte)0xA8, (byte)0xAA,
		(byte)0xAB, (byte)0xAD, (byte)0xAE, (byte)0xAF, (byte)0xB1, (byte)0xB2, (byte)0xB3, (byte)0xB5, (byte)0xB6, (byte)0xB7, (byte)0xB8, (byte)0xB9, (byte)0xBA, (byte)0xBB, (byte)0xBC, (byte)0xBD,
		(byte)0xBE, (byte)0xBF, (byte)0xC0, (byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4, (byte)0xC5, (byte)0xC6, (byte)0xC7, (byte)0xC8, (byte)0xC9, (byte)0xCA, (byte)0xCB, (byte)0xCC, (byte)0xCD,
		(byte)0xCE, (byte)0xCF, (byte)0xD0, (byte)0xD1, (byte)0xD2, (byte)0xD3, (byte)0xD4, (byte)0xD5, (byte)0xD6, (byte)0xD7, (byte)0xD8, (byte)0xD9, (byte)0xDA, (byte)0xDB, (byte)0xDC, (byte)0xDD,
		(byte)0xDE, (byte)0xDF, (byte)0xE0, (byte)0xE1, (byte)0xE2, (byte)0xE3, (byte)0xE4, (byte)0xE5, (byte)0xE6, (byte)0xE7, (byte)0xE8, (byte)0xE9, (byte)0xEA, (byte)0xEB, (byte)0xED, (byte)0xEE,
		(byte)0xEF, (byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3, (byte)0xF4, (byte)0xF5, (byte)0xF6, (byte)0xF7, (byte)0xF8, (byte)0xF9, (byte)0xFA, (byte)0xFB, (byte)0xFC, (byte)0xFD, (byte)0xFE,
		(byte)0xFE, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF
	};

	private static final byte[] GREEN_DATA = {
		(byte)0x00, (byte)0x03, (byte)0x06, (byte)0x09, (byte)0x0C, (byte)0x0F, (byte)0x12, (byte)0x15, (byte)0x18, (byte)0x1B, (byte)0x1E, (byte)0x21, (byte)0x24, (byte)0x27, (byte)0x29, (byte)0x2C,
		(byte)0x2F, (byte)0x31, (byte)0x34, (byte)0x36, (byte)0x38, (byte)0x3A, (byte)0x3C, (byte)0x3E, (byte)0x40, (byte)0x42, (byte)0x44, (byte)0x45, (byte)0x46, (byte)0x47, (byte)0x48, (byte)0x49,
		(byte)0x4A, (byte)0x4B, (byte)0x4C, (byte)0x4D, (byte)0x4D, (byte)0x4E, (byte)0x4E, (byte)0x4E, (byte)0x4E, (byte)0x4E, (byte)0x4D, (byte)0x4D, (byte)0x4C, (byte)0x4C, (byte)0x4B, (byte)0x4A,
		(byte)0x49, (byte)0x48, (byte)0x47, (byte)0x46, (byte)0x44, (byte)0x43, (byte)0x42, (byte)0x41, (byte)0x40, (byte)0x3F, (byte)0x3E, (byte)0x3D, (byte)0x3C, (byte)0x3B, (byte)0x3A, (byte)0x39,
		(byte)0x38, (byte)0x37, (byte)0x37, (byte)0x36, (byte)0x36, (byte)0x36, (byte)0x36, (byte)0x36, (byte)0x37, (byte)0x37, (byte)0x38, (byte)0x39, (byte)0x3A, (byte)0x3B, (byte)0x3C, (byte)0x3D,
		(byte)0x3F, (byte)0x41, (byte)0x43, (byte)0x45, (byte)0x47, (byte)0x48, (byte)0x4A, (byte)0x4C, (byte)0x4E, (byte)0x50, (byte)0x52, (byte)0x54, (byte)0x56, (byte)0x58, (byte)0x5A, (byte)0x5B,
		(byte)0x5D, (byte)0x5F, (byte)0x61, (byte)0x62, (byte)0x63, (byte)0x64, (byte)0x65, (byte)0x66, (byte)0x67, (byte)0x68, (byte)0x69, (byte)0x6A, (byte)0x6B, (byte)0x6C, (byte)0x6D, (byte)0x6E,
		(byte)0x6F, (byte)0x70, (byte)0x70, (byte)0x71, (byte)0x71, (byte)0x72, (byte)0x72, (byte)0x73, (byte)0x73, (byte)0x73, (byte)0x74, (byte)0x74, (byte)0x74, (byte)0x74, (byte)0x75, (byte)0x75,
		(byte)0x75, (byte)0x76, (byte)0x76, (byte)0x76, (byte)0x76, (byte)0x76, (byte)0x76, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x78, (byte)0x78, (byte)0x78, (byte)0x78,
		(byte)0x78, (byte)0x78, (byte)0x79, (byte)0x79, (byte)0x79, (byte)0x78, (byte)0x78, (byte)0x78, (byte)0x78, (byte)0x77, (byte)0x77, (byte)0x76, (byte)0x76, (byte)0x75, (byte)0x74, (byte)0x73,
		(byte)0x73, (byte)0x72, (byte)0x71, (byte)0x70, (byte)0x6F, (byte)0x6E, (byte)0x6E, (byte)0x6D, (byte)0x6C, (byte)0x6B, (byte)0x6A, (byte)0x6A, (byte)0x69, (byte)0x68, (byte)0x68, (byte)0x67,
		(byte)0x67, (byte)0x67, (byte)0x66, (byte)0x66, (byte)0x66, (byte)0x66, (byte)0x67, (byte)0x67, (byte)0x67, (byte)0x68, (byte)0x69, (byte)0x6A, (byte)0x6B, (byte)0x6C, (byte)0x6D, (byte)0x6E,
		(byte)0x70, (byte)0x72, (byte)0x74, (byte)0x76, (byte)0x78, (byte)0x7A, (byte)0x7B, (byte)0x7D, (byte)0x7F, (byte)0x81, (byte)0x83, (byte)0x85, (byte)0x87, (byte)0x89, (byte)0x8C, (byte)0x8E,
		(byte)0x90, (byte)0x92, (byte)0x94, (byte)0x97, (byte)0x99, (byte)0x9B, (byte)0x9D, (byte)0xA0, (byte)0xA2, (byte)0xA4, (byte)0xA6, (byte)0xA9, (byte)0xAB, (byte)0xAE, (byte)0xB0, (byte)0xB2,
		(byte)0xB5, (byte)0xB7, (byte)0xB9, (byte)0xBC, (byte)0xBE, (byte)0xC0, (byte)0xC3, (byte)0xC5, (byte)0xC8, (byte)0xCA, (byte)0xCD, (byte)0xCF, (byte)0xD1, (byte)0xD4, (byte)0xD6, (byte)0xD9,
		(byte)0xDB, (byte)0xDE, (byte)0xE0, (byte)0xE3, (byte)0xE5, (byte)0xE8, (byte)0xEA, (byte)0xED, (byte)0xEF, (byte)0xF2, (byte)0xF4, (byte)0xF7, (byte)0xF9, (byte)0xFC, (byte)0xFE, (byte)0xFF
	};

	private static final byte[] BLUE_DATA = {
		(byte)0x00, (byte)0x02, (byte)0x04, (byte)0x06, (byte)0x08, (byte)0x09, (byte)0x0B, (byte)0x0D, (byte)0x0F, (byte)0x11, (byte)0x13, (byte)0x15, (byte)0x17, (byte)0x19, (byte)0x1B, (byte)0x1D,
		(byte)0x1F, (byte)0x21, (byte)0x23, (byte)0x25, (byte)0x27, (byte)0x29, (byte)0x2B, (byte)0x2E, (byte)0x30, (byte)0x32, (byte)0x34, (byte)0x37, (byte)0x39, (byte)0x3B, (byte)0x3E, (byte)0x40,
		(byte)0x43, (byte)0x45, (byte)0x48, (byte)0x4A, (byte)0x4D, (byte)0x50, (byte)0x52, (byte)0x55, (byte)0x58, (byte)0x5A, (byte)0x5D, (byte)0x60, (byte)0x63, (byte)0x66, (byte)0x69, (byte)0x6B,
		(byte)0x6E, (byte)0x71, (byte)0x74, (byte)0x76, (byte)0x79, (byte)0x7B, (byte)0x7E, (byte)0x80, (byte)0x82, (byte)0x84, (byte)0x85, (byte)0x87, (byte)0x88, (byte)0x8A, (byte)0x8B, (byte)0x8C,
		(byte)0x8D, (byte)0x8E, (byte)0x8F, (byte)0x8F, (byte)0x8F, (byte)0x8E, (byte)0x8E, (byte)0x8D, (byte)0x8D, (byte)0x8C, (byte)0x8B, (byte)0x8A, (byte)0x89, (byte)0x87, (byte)0x86, (byte)0x85,
		(byte)0x84, (byte)0x82, (byte)0x81, (byte)0x80, (byte)0x7F, (byte)0x7E, (byte)0x7D, (byte)0x7C, (byte)0x7B, (byte)0x7A, (byte)0x79, (byte)0x79, (byte)0x78, (byte)0x78, (byte)0x78, (byte)0x78,
		(byte)0x78, (byte)0x78, (byte)0x78, (byte)0x78, (byte)0x78, (byte)0x78, (byte)0x78, (byte)0x78, (byte)0x78, (byte)0x79, (byte)0x79, (byte)0x79, (byte)0x78, (byte)0x78, (byte)0x78, (byte)0x77,
		(byte)0x77, (byte)0x76, (byte)0x76, (byte)0x75, (byte)0x75, (byte)0x74, (byte)0x74, (byte)0x73, (byte)0x73, (byte)0x72, (byte)0x72, (byte)0x72, (byte)0x71, (byte)0x71, (byte)0x71, (byte)0x71,
		(byte)0x71, (byte)0x71, (byte)0x72, (byte)0x72, (byte)0x72, (byte)0x72, (byte)0x72, (byte)0x72, (byte)0x72, (byte)0x72, (byte)0x72, (byte)0x71, (byte)0x71, (byte)0x71, (byte)0x70, (byte)0x70,
		(byte)0x6F, (byte)0x6E, (byte)0x6D, (byte)0x6C, (byte)0x6A, (byte)0x69, (byte)0x67, (byte)0x66, (byte)0x64, (byte)0x62, (byte)0x60, (byte)0x5E, (byte)0x5C, (byte)0x59, (byte)0x57, (byte)0x55,
		(byte)0x52, (byte)0x50, (byte)0x4D, (byte)0x4B, (byte)0x48, (byte)0x46, (byte)0x44, (byte)0x41, (byte)0x3F, (byte)0x3D, (byte)0x3A, (byte)0x38, (byte)0x36, (byte)0x34, (byte)0x31, (byte)0x2F,
		(byte)0x2D, (byte)0x2B, (byte)0x29, (byte)0x28, (byte)0x26, (byte)0x25, (byte)0x24, (byte)0x23, (byte)0x22, (byte)0x21, (byte)0x21, (byte)0x21, (byte)0x21, (byte)0x21, (byte)0x21, (byte)0x21,
		(byte)0x21, (byte)0x22, (byte)0x22, (byte)0x23, (byte)0x24, (byte)0x26, (byte)0x27, (byte)0x29, (byte)0x2A, (byte)0x2C, (byte)0x2E, (byte)0x30, (byte)0x32, (byte)0x35, (byte)0x37, (byte)0x3A,
		(byte)0x3C, (byte)0x3F, (byte)0x42, (byte)0x45, (byte)0x48, (byte)0x4B, (byte)0x4F, (byte)0x52, (byte)0x56, (byte)0x59, (byte)0x5D, (byte)0x61, (byte)0x64, (byte)0x68, (byte)0x6C, (byte)0x70,
		(byte)0x74, (byte)0x78, (byte)0x7D, (byte)0x81, (byte)0x86, (byte)0x8A, (byte)0x8F, (byte)0x93, (byte)0x98, (byte)0x9D, (byte)0xA2, (byte)0xA7, (byte)0xAC, (byte)0xB1, (byte)0xB6, (byte)0xBB,
		(byte)0xC1, (byte)0xC6, (byte)0xCB, (byte)0xD1, (byte)0xD6, (byte)0xDC, (byte)0xE1, (byte)0xE7, (byte)0xEC, (byte)0xF2, (byte)0xF7, (byte)0xFD, (byte)0xFE, (byte)0xFF, (byte)0xFF, (byte)0xFF
	};
}
