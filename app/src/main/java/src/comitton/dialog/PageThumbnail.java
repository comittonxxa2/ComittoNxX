package src.comitton.dialog;

import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.imageview.ImageManager;
import src.comitton.imageview.ThumbnailView;
import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("NewApi")
public class PageThumbnail extends ToolbarDialog implements OnTouchListener,
		OnClickListener, OnSeekBarChangeListener, DialogInterface.OnDismissListener {
	// 表示中フラグ
	public static boolean mIsOpened = false;

	// パラメータ
	private ImageManager mImageMgr;
	private long mThumID;
	private static Handler mHandler;

	private HorizontalScrollView mScroll;
	static ThumbnailView mThumView;

	public PageThumbnail(AppCompatActivity activity, @StyleRes int themeResId) {
		super(activity, themeResId);
		mAutoApply = false;
	}

	public void setParams(boolean viwer, int page, boolean reverse, ImageManager imgr, long thumid, boolean dirtree, Handler handler) {
		super.setParams(viwer, page, imgr.length(), reverse, dirtree, handler);
		mPage = page;
		mReverse = reverse;
		mMaxPage = imgr.length();
		mImageMgr = imgr;
		mThumID = thumid;
		mHandler = handler;
	}

	private static int[] mStates;
	private static boolean[] mEnable;

	// dpをピクセルへ変換
	private static int dpToPx(Context context, int dpValue) {
		float density = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * density + 0.5f);
	}

	// ツールバーを動的に表示する(元々はxmlで静的に表示していたのを変更)
	public static void SetIconLayout(LinearLayout parentLayout, Context context, Handler handler) {
		// 横スクロールのレイアウトを設定(この中にアイコンが登録される)
		HorizontalScrollView hr = new HorizontalScrollView(context);
		hr.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		float mScale;
		mScale = context.getResources().getDisplayMetrics().scaledDensity;
		int marginW = (int)(4 * mScale);
		hr.setPadding(marginW, marginW, marginW, marginW);
		// ツールバーのアイコンのレイアウトを設定
		LinearLayout layout = new LinearLayout(context);
		layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		// ツールバーのアイコンの順序の設定を取り出す
		mStates = new int[ToolbarEditDialog.COMMAND_DRAWABLE.length];
		mStates = ToolbarEditDialog.loadToolbarIndex(context);
		// ツールバーのアイコンを有効にする設定を取り出す
		mEnable = new boolean[ToolbarEditDialog.COMMAND_DRAWABLE.length];
		mEnable = ToolbarEditDialog.loadToolbarState(context);
		// ツールバーのアイコンのサイズの比率を取り出す
		float ratio = ToolbarEditDialog.getToolbarRatio(context);

		for (int i = 0; i < ToolbarEditDialog.COMMAND_DRAWABLE.length; i++) {
			if (mEnable[mStates[i]]) {
				// ImageViewを新しく作成
				ImageView imageView = new ImageView(context);
				// アイコン画像を登録
				imageView.setImageResource(ToolbarEditDialog.COMMAND_DRAWABLE[mStates[i]]);
				imageView.setColorFilter(Color.WHITE);
				// LayoutParamsを設定
				imageView.setLayoutParams(new LinearLayout.LayoutParams(
				    LinearLayout.LayoutParams.WRAP_CONTENT,
				    LinearLayout.LayoutParams.WRAP_CONTENT
				));
				// タッチイベントを登録
				int finalI = mStates[i];
				imageView.setOnClickListener(v -> {
					// アイコンがタッチされたらタッチイベントのメッセージを送る
					Message message = new Message();
					message.what = DEF.HMSG_EVENT_TOOLBAR;
					message.arg1 = finalI;
					handler.sendMessage(message);
				});
				// アイコンのサイズをセット
				int sizeInPx = dpToPx(context, (int)(32 * ratio));
				ViewGroup.LayoutParams paramsv = imageView.getLayoutParams();
				paramsv.width = sizeInPx;
				paramsv.height = sizeInPx;
				imageView.setLayoutParams(paramsv);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
				mScale = context.getResources().getDisplayMetrics().scaledDensity;
				marginW = (int)(4 * mScale);
				params.gravity= Gravity.CENTER;
				params.setMargins(0, marginW, marginW, marginW);
				imageView.setLayoutParams(params);
				// アイコンを登録
				layout.addView(imageView);
			}
		}
		// 横スクロールへアイコンを登録
		hr.addView(layout);
		// 親のレイアウトへ登録
		parentLayout.addView(hr);
	}

	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.pagethumbnail);

		// ツールバーを動的に表示するため親のレイアウトを取得
		LinearLayout parentLayout = findViewById(R.id.pagethumbnail);
		Context context = getContext();
		// ツールバーを動的に表示
		SetIconLayout(parentLayout, context, mHandler);

		mScroll = (HorizontalScrollView) this.findViewById(R.id.scrl_view);
		mThumView = (ThumbnailView) this.findViewById(R.id.thumb_view);
		mThumView.initialize(mPage, mMaxPage, mReverse, mImageMgr, this, mScroll, mThumID, 1);
		mThumView.setOnTouchListener(this);

		// バックグラウンドでのキャッシュ読み込み停止
		mImageMgr.setCacheSleep(true);

		// 表示中フラグ
		mIsOpened = true;

		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "View=" + v + ", event=" + event);
		if (mThumView == v) {
			int action = event.getAction();
			int x = (int) event.getX();
			switch (action) {
				case MotionEvent.ACTION_DOWN:
					Logcat.d(logLevel, "MotionEvent=ACTION_DOWN");
					break;
				case MotionEvent.ACTION_UP:
					Logcat.d(logLevel, "MotionEvent=ACTION_UP");
					// ページ選択
					int page = mThumView.getCurrentPage(x);
					Logcat.d(logLevel, "page=" + page);
					if (page >= 0) {
						mListener.onSelectPage(page);
						dismiss();
					}
					break;
			}
			// trueにするとonTouchのACTION_UPが呼ばれる
			return true;
		}
		// falseにするとonClickが呼ばれる
		return false;
	}

	@Override
	public void onClick(View v) {
		// ボタンクリック
		super.onClick(v);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		// ダイアログ終了
		mThumView.close();
		mIsOpened = false;
		mImageMgr.setCacheSleep(false);
	}

	public void onScrollChanged(int pos) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "calcProgress(mSeekPage.getProgress())=" + calcProgress(mSeekPage.getProgress()) + ", pos=" + pos);
		if (calcProgress(mSeekPage.getProgress()) != pos) {
			setProgress(pos, true);
		}
	}

	protected static void setProgress(int pos, boolean fromThumb) {
		ToolbarDialog.setProgress(pos, fromThumb);
		if (!fromThumb) {
			mThumView.setPosition(pos);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int page, boolean fromUser) {
		// 変更
		if (fromUser) {
		int cnvpage = calcProgress(page);
		mThumView.setPosition(cnvpage);
		}
	}
}