package src.comitton.cropimageview;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.imageview.ImageManager;
import src.comitton.config.SetCommonActivity;


public class CropImageActivity extends AppCompatActivity implements Runnable, TextWatcher, CropImageView.CropCallback{
    private String mFile;	        	// ZIPファイル名
    private String mPath;				// ベースuriからのパス名 (SAF未対応 (ComittoNxX v3.0.0 未満))
    private String mURI;		            // ベースディレクトリのuri
    private String mUser;			    // SMB認証用
    private String mPass;			    // SMB認証用
    private String mCropPath;           // 画像を切り取るファイル名
    private int mPage;                  // 画像を切り取るページ


    private CropImageView mCropImageView;
    private EditText mEditAspectRatio;
    float mAspectRatio;

    private Thread mThread;
    private Bitmap mBitmap;

	private boolean mNotice = false;
	private boolean mImmEnable = false;
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;
	private static int mviewrota;
	private static OrientationEventListener orientationEventListener = null;
	private static int deviceOrientation = -1;
	private static SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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
		SetOrientationEventListener(this, sharedPreferences);

        setContentView(R.layout.cropimage);

        Intent intent = getIntent();
        mCropPath = intent.getStringExtra("uri");   // 画像を切り取るファイル名
        if(mCropPath == null) {
            mURI = intent.getStringExtra("Uri");    // ベースディレクトリのURI
            mPath = intent.getStringExtra("Path");  // ベースURIからの相対パス名
            mFile = intent.getStringExtra("File");  // ZIPファイル名
            mUser = intent.getStringExtra("User");  // SMB認証用
            mPass = intent.getStringExtra("Pass");  // SMB認証用
            mPage = intent.getIntExtra("Page", 0);  // ページ番号
        }
//        mUri = Uri.parse("file://" + uri);
        mAspectRatio = intent.getFloatExtra("aspectRatio", 3.0f / 4.0f);

        mCropImageView = (CropImageView) findViewById(R.id.cropImageView);
        mCropImageView.setAspectRatio(mAspectRatio);
        mCropImageView.setCallback(this);

        mThread = new Thread(this);
        mThread.start();


        // 以下はイベントハンドラ
        Button cropButton = (Button) findViewById(R.id.btn_ok);
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // フレーム内をクロップ
                mCropImageView.Crop(mCropPath);
                Intent intent = new Intent();
                intent.setData(Uri.parse("file://" + mCropPath));
//                intent.putExtra("uri", mUri.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mEditAspectRatio = (EditText) findViewById(R.id.edit_aspect);
        mEditAspectRatio.setText(String.valueOf(mAspectRatio));
        mEditAspectRatio.addTextChangedListener(this);
        Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        // アス比減少・増加ボタン
        Button btnMinus = (Button) findViewById(R.id.btn_minus);
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAspectRatio -= 0.01f;
                mEditAspectRatio.setText(String.format("%.3f", mAspectRatio));
            }
        });
        Button btnPlus = (Button) findViewById(R.id.btn_plus);
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAspectRatio += 0.01f;
                mEditAspectRatio.setText(String.format("%.3f", mAspectRatio));
            }
        });

        // 左右移動ボタン
        Button btnLeft = (Button) findViewById(R.id.btn_left);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropImageView.move(-0.01f);
            }
        });
        Button btnRight = (Button) findViewById(R.id.btn_right);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropImageView.move(0.01f);
            }
        });
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void run() {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        Message message = new Message();
        message.what = DEF.HMSG_ERROR;
        ImageManager imageMgr = null;
        if(mCropPath == null) {
            imageMgr = new ImageManager(this, mPath, mFile, mUser, mPass, ImageManager.FILESORT_NAME_UP, handler,true, ImageManager.OPENMODE_THUMBSORT, 1);
            imageMgr.LoadImageList(0, 0, 0, 0, 0);
            mCropPath = imageMgr.decompFile(mPage, null);
        }
        if(mCropPath != null) {
            try {
                Logcat.d(logLevel, "イメージファイルを開きます. mURI=" + mURI);
                Logcat.d(logLevel, "mCropPath=" + mCropPath);
                mBitmap = imageMgr.GetBitmapFromPath(this, mCropPath, null);
                if (mBitmap == null) {
                    Logcat.e(logLevel, "ビットマップ取得に失敗しました.");
                }
                else {
                    Logcat.d(logLevel, "ビットマップ取得に成功しました.");
                    message.what = DEF.HMSG_LOAD_END;
                }
                if (mBitmap.getHeight() > 1000) {
                    Logcat.e(logLevel, "ビットマップを縮小します.");
                    float dsH = (float)1000 / (float)mBitmap.getHeight();
                    mBitmap = Bitmap.createScaledBitmap(mBitmap, (int)(mBitmap.getWidth() * dsH), (int)(mBitmap.getHeight() * dsH), true);
                }
            } catch (Exception e) {
                Logcat.e(logLevel, "エラーが発生しました." , e);
                throw new RuntimeException(e);
            } finally {
                try {
                    if (imageMgr != null) {
                        imageMgr.close();
                    }
                } catch (Exception e) {
                    // なにもしない
                }
            }
        }
        handler.sendMessage(message);
    }

    public void cropCallback(float aspectRatio){
        mEditAspectRatio.setText(String.format("%.3f", aspectRatio));
    }

    private final Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        public void handleMessage(Message message){
            switch (message.what){
                case DEF.HMSG_LOADING: // ロード状況表示
                    break;
                case DEF.HMSG_LOAD_END:
                    mCropImageView.setImageBitmap(mBitmap);
                    break;
                case DEF.HMSG_ERROR:
                    setResult(RESULT_CANCELED);
                    finish();
                    break;
                case DEF.HMSG_PROGRESS:
                    break;
                case DEF.HMSG_WORKSTREAM:
                    // ファイルアクセスの表示
                    break;
            }
        }
    };

	private static void RotateMain(AppCompatActivity activity, int orientation, int viewrota) {
		if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
			return;
		}
		if (orientation >= 45 && orientation < 135) {
			// 90度
			if (deviceOrientation != 3) {
				deviceOrientation = 3;
				if (viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT || viewrota == DEF.ROTATE_ALL_AUTO || viewrota == DEF.ROTATE_ALL_LANDSCAPE) {
					// 横上下反転
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
				}
				if (viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT_LANDSCAPE || viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_LANDSCAPE || viewrota == DEF.ROTATE_ALL_REVERSE_LANDSCAPE) {
					// 横通常表示
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
			}
		}
		else if (orientation >= 135 && orientation < 225) {
			// 180度
			if (deviceOrientation != 2) {
				deviceOrientation = 2;
				if (viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_LANDSCAPE || viewrota == DEF.ROTATE_ALL_AUTO || viewrota == DEF.ROTATE_ALL_PORTRAIT) {
					// 縦上下反転
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
				}
				if (viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT_LANDSCAPE || viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT || viewrota == DEF.ROTATE_ALL_REVERSE_PORTRAIT) {
					// 縦通常表示
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			}
		}
		else if (orientation >= 225 && orientation < 315) {
			// 270度
			if (deviceOrientation != 1) {
				deviceOrientation = 1;
				if (viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_LANDSCAPE || viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT_LANDSCAPE || viewrota == DEF.ROTATE_ALL_REVERSE_LANDSCAPE) {
					// 横上下反転
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
				}
				if (viewrota == DEF.ROTATE_ALL_AUTO || viewrota == DEF.ROTATE_ALL_LANDSCAPE || viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT) {
					// 横通常表示
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
			}
		}
		else {
			// 0度
			if (deviceOrientation != 0) {
				deviceOrientation = 0;
				if (viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT || viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_PORTRAIT_LANDSCAPE || viewrota == DEF.ROTATE_ALL_REVERSE_PORTRAIT) {
					// 縦上下反転
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
				}
				if (viewrota == DEF.ROTATE_ALL_AUTO || viewrota == DEF.ROTATE_ALL_PORTRAIT || viewrota == DEF.ROTATE_ALL_AUTO_REVERSE_LANDSCAPE) {
					// 縦通常表示
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			}
		}
	}

	public static void SetOrientationEventListener(AppCompatActivity activity, SharedPreferences sharedPreferences) {
		// 起動時は回転動作にならないので固定値の場合は個別で設定する
		mviewrota = SetCommonActivity.getViewRotaAll(sharedPreferences);
		if (!SetCommonActivity.getForceTradOldViewRotate(sharedPreferences)) {
			// 従来の設定で回転させる
			DEF.setRotationAll(activity, mviewrota);
			return;
		}
		deviceOrientation = -1;
		switch (mviewrota) {
			case 1:
				RotateMain(activity, 0, mviewrota);
				break;
			case 2:
				RotateMain(activity ,270, mviewrota);
				break;
			case 6:
				RotateMain(activity, 180, mviewrota);
				break;
			case 7:
				RotateMain(activity, 90, mviewrota);
				break;
		}
		orientationEventListener = new OrientationEventListener(activity) {
			// 傾きセンサーの角度を得る
			public void onOrientationChanged(int orientation) {
				RotateMain(activity, orientation, mviewrota);
			}
		};
	}

	public static void SetOrientationEventListenerEnable(SharedPreferences sharedPreferences) {
		if (!SetCommonActivity.getForceTradOldViewRotate(sharedPreferences) || orientationEventListener == null) {
			return;
		}
		orientationEventListener.enable();
	}

	public static void SetOrientationEventListenerDisable(SharedPreferences sharedPreferences) {
		if (!SetCommonActivity.getForceTradOldViewRotate(sharedPreferences) || orientationEventListener == null) {
			return;
		}
		orientationEventListener.disable();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// バックグラウンドからフォアグランドに戻った時
		SetOrientationEventListenerEnable(sharedPreferences);
	}
	@Override
	protected void onPause() {
		super.onPause();
		SetOrientationEventListenerDisable(sharedPreferences);
	}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        mAspectRatio = Float.parseFloat(s.toString());
        if(mAspectRatio < 0.1f)
            mAspectRatio = 0.1f;
        mCropImageView.setAspectRatio(mAspectRatio);
    }
}