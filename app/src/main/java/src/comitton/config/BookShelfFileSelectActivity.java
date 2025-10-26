package src.comitton.config;

import src.comitton.helpview.HelpActivity;
import src.comitton.common.DEF;
import src.comitton.config.SetCommonActivity;
import jp.dip.muracoro.comittonx.R;
import src.comitton.common.Logcat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.preference.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class BookShelfFileSelectActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;
	private static final int PICK_FILE_REQUEST_CODE = 1;
	private int mScreenWidth;
	private int mScreenHeight;
	private int mGetCustomNumber;

	private static String[] BookShelfBmpFile = {
		DEF.KEY_BOOKSHELFBMPFILE1,
		DEF.KEY_BOOKSHELFBMPFILE2,
		DEF.KEY_BOOKSHELFBMPFILE3,
		DEF.KEY_BOOKSHELFBMPFILE4,
		DEF.KEY_BOOKSHELFBMPFILE5,
		DEF.KEY_BOOKSHELFBMPFILE6,
		DEF.KEY_BOOKSHELFBMPFILE7,
		DEF.KEY_BOOKSHELFBMPFILE8
	};

	Resources mResources;
	SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mResources = getResources();

		// Intentに保存されたデータを取り出す
		Intent intent = getIntent();
		// カスタム画像の番号が入る
		mGetCustomNumber = intent.getIntExtra("i", 0);

        WindowManager windowManager = this.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();

        // Android 4.2 (Jelly Bean MR1) 以降で getRealSize を使用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(point);
        } else {
            // それ以前のバージョンでは getSize を使用
            display.getSize(point);
        }
        // 画像の縦と横のサイズを得る
        mScreenWidth = point.x;
        mScreenHeight = point.y;

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
		// ファイル選択を開く
		OpenFile();
	}

	// ファイル選択を開く
	private void OpenFile() {
		try {
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			// ファイル選択のインテントを作成
			// ユーザーが選択できるファイルタイプを絞る（ここでは画像ファイル
			intent.setType("image/*");
			// ファイル選択UIを起動
			startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			// キャンセルされた場合はエラーにして戻る
			ErrorCustomFile();
			finish();
		}
	}

	// ファイル選択の結果が返ってくる
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// File load
		if (requestCode == PICK_FILE_REQUEST_CODE) {
			if (resultCode == RESULT_OK && data != null) {
				Uri uri = data.getData();
				if (uri != null) {
					boolean result = false;
					result = loadStrFromUri(uri);
					if (result) {
						// 成功すれば終了
						SuccessCustomFile();
						finish();
						// 終了させる
						return;
					}
				}
			}
		}
		// 成功しなければ全てエラーにして戻る
		ErrorCustomFile();
		finish();
	}

	private boolean loadStrFromUri(Uri uri) {

		try {
			if (uri.getScheme().equals("content")) {
				Toast.makeText(this, R.string.SaveCustomFileStart, Toast.LENGTH_SHORT).show();

				InputStream iStream = getContentResolver().openInputStream(uri);				// InputStreamからビットマップの情報を読み込む
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				Bitmap bitmap = BitmapFactory.decodeStream(iStream, null, options);
				iStream.close();
				float scaleX = 1;
				float scaleY = 1;
				if (options.outWidth > (mScreenWidth / 4)) {
					// 画面の4分の1を超えた場合はリサイズする
					scaleX = options.outWidth / (mScreenWidth / 4);
					scaleY = options.outHeight / (mScreenHeight / 4);
				}
				options.inSampleSize = (int) Math.floor(Float.valueOf(Math.max(scaleX, scaleY)).doubleValue());
				// ビットマップを読み込む
				options.inJustDecodeBounds = false;
				iStream = this.getContentResolver().openInputStream(uri);
				bitmap = BitmapFactory.decodeStream(iStream, null, options);
				iStream.close();
				// URIを文字列へ変換
				String name = uri.toString();
				// 区切りをアンダーバーへ変換
				name = name.replace("\\", "_");
				name = name.replace("/", "_");
				// ファイル名をMD5のハッシュ値へ変換
				String pathcode = DEF.makeCode(name + String.valueOf(SystemClock.uptimeMillis()), 0, 0);
				String file = DEF.getBaseDirectory() + "filelist/" + pathcode + ".cache";
				// ファイルリストを格納するディレクトリを作成する
				new File(DEF.getBaseDirectory() + "filelist/").mkdirs();

				try {
					// 保存処理開始
					FileOutputStream fos = null;
					fos = new FileOutputStream(new File(file));

					// ビットマップをPNGで保存
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

					// 保存処理終了
					fos.close();
				} catch (Exception e) {
					return false;
				}
				// 変換したファイル名をSharedPreferencesへ書き込む
				// 古いファイル名を取り出す
				String oldfile = sharedPreferences.getString(BookShelfBmpFile[mGetCustomNumber], "");
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(BookShelfBmpFile[mGetCustomNumber], file);
				editor.apply();
				// ファイルを削除する場合は初回に失敗するのでtry～catchで囲む
				try {
					// 古いファイルを削除
					File deletefile = new File(oldfile);
					deletefile.delete();
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private void SuccessCustomFile() {
		Toast.makeText(this, R.string.SaveCustomFile, Toast.LENGTH_SHORT).show();
	}
	private void ErrorCustomFile() {
		Toast.makeText(this, R.string.ErrorSaveCustomFile, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SetCommonActivity.SetOrientationEventListenerEnable();
	}

	@Override
	protected void onPause() {
		super.onPause();
		SetCommonActivity.SetOrientationEventListenerDisable();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

	}

}
