package src.comitton.fileview.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.view.View;

import java.io.FileInputStream;

import jp.dip.muracoro.comittonx.R;

@SuppressLint("ViewConstructor")
public class SelectIconViewCustom extends View {
	private final int MARGIN1 = 4;
	private final int MARGIN2 = 12;

	public Context mContext;

	// アイコンのビットマップ
	private Bitmap mIcon;
	
	private Paint mBakPaint;
	private Paint mCurPaint;
	private Paint mBmpPaint;

	private Rect mIconSrcRect;
	private Rect mIconDstRect;
	private Rect mFrameRect;

	private boolean mSelect;

	private static final int[] SetBookShelfCustomName =
		{ R.string.BookShelfcustom01
		, R.string.BookShelfcustom02
		, R.string.BookShelfcustom03
		, R.string.BookShelfcustom04
		, R.string.BookShelfcustom05
		, R.string.BookShelfcustom06
		, R.string.BookShelfcustom07
		, R.string.BookShelfcustom08 };

	public SelectIconViewCustom(Context context, int iconId, String file, int index, int bakcolor, int curcolor) {
		super(context);
		mContext = context;

		// 描画用設定
		mBakPaint = new Paint();
		mBakPaint.setStyle(Style.FILL);
		mBakPaint.setColor(bakcolor);

		mCurPaint = new Paint();
		mCurPaint.setStyle(Style.STROKE);
		mCurPaint.setStrokeWidth(4);
		mCurPaint.setColor(curcolor);

		mBmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBmpPaint.setStyle(Style.FILL);

		if (iconId == 0) {
			// 文字列を表示
			// ビットマップの土台を作成
			mIcon = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(mIcon);
			Paint paint = new Paint();
			// 白色で埋める
			paint.setColor(Color.WHITE);
			canvas.drawRect(0, 0, 96, 96, paint);
			// 文字色を黒に設定
			paint.setColor(Color.BLACK);
			// 文字サイズを40に設定
			paint.setTextSize(40);
			// アンチエイリアスを有効にして文字を滑らかにする
			paint.setAntiAlias(true);
			Resources res = getResources();
			String text = res.getString(R.string.BookShelf000);
			// X座標
			float xPosition;
			// 中心座標を求める
			xPosition = (96 - (int)paint.measureText(text)) / 2;
			// Y座標
			float yPosition = 40;
			// 文字列を描画
			canvas.drawText(text, xPosition, yPosition, paint);
			text = res.getString(R.string.BookShelf001);
			// 中心座標を求める
			xPosition = (96 - (int)paint.measureText(text)) / 2;
			// Y座標
			yPosition = 80;
			// 文字列を描画
			canvas.drawText(text, xPosition, yPosition, paint);
		}
		else if (iconId == 1) {
			boolean check = false;
			if (file == "") {
				// ファイル名が空白だった場合
				check = true;
			}
			else {
				try {
					// ファイルを読み出してビットマップへ展開する
					FileInputStream fis = new FileInputStream(file);
					mIcon = BitmapFactory.decodeStream(fis);
					fis.close();
				} catch (Exception e) {
					// 読み込みに失敗した場合
					check = true;
				}
			}
			if (check) {
				// 文字列を表示
				// ビットマップの土台を作成
				mIcon = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(mIcon);
				Paint paint = new Paint();
				// 白色で埋める
				paint.setColor(Color.WHITE);
				canvas.drawRect(0, 0, 96, 96, paint);
				// 文字色を黒に設定
				paint.setColor(Color.BLACK);
				// 文字サイズを28に設定
				paint.setTextSize(28);
				// アンチエイリアスを有効にして文字を滑らかにする
				paint.setAntiAlias(true);
				Resources res = getResources();
				String text = res.getString(R.string.BookShelfcustom00);
				// X座標
				float xPosition;
				// 中心座標を求める
				xPosition = (96 - (int)paint.measureText(text)) / 2;
				// Y座標
				float yPosition = 40;
				// 文字列を描画
				canvas.drawText(text, xPosition, yPosition, paint);
				// 文字サイズを32に設定
				paint.setTextSize(32);
				text = res.getString(SetBookShelfCustomName[index]);
				// 中心座標を求める
				xPosition = (96 - (int)paint.measureText(text)) / 2;
				// Y座標
				yPosition = 80;
				// 文字列を描画
				canvas.drawText(text, xPosition, yPosition, paint);			
			}
		}
		else {
			Resources res = getResources();
			mIcon = BitmapFactory.decodeResource(res, iconId);
		}

		if (mIcon != null) {
			mIconSrcRect = new Rect(0, 0, mIcon.getWidth(), mIcon.getHeight());
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// 選択中
		canvas.drawRect(mFrameRect, mSelect == true ? mCurPaint : mBakPaint);
		if (mIcon != null) {
			canvas.drawBitmap(mIcon, mIconSrcRect, mIconDstRect, mBmpPaint);
		}
		return;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		mFrameRect = new Rect(MARGIN1, MARGIN1, width - MARGIN1, height - MARGIN1);
		mIconDstRect = new Rect(MARGIN2, MARGIN2, width - MARGIN2, height - MARGIN2);

		setMeasuredDimension(width, height);
	}

	public void setSelect(boolean select) {
		if (mSelect != select) {
			mSelect = select;
			invalidate();
		}
	}
}
