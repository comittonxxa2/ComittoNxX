package src.comitton.config;


import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.fileview.view.SelectIconView;
import jp.dip.muracoro.comittonx.R;
import src.comitton.fileview.view.SelectIconViewCustom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.preference.DialogPreference;
import androidx.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

public class BookShelfDeletePreference extends DialogPreference implements OnClickListener {
	private Context mContext;
	private static SharedPreferences mSP;
	private float mDensity;

	private static int LAYOUT_PADDING;
	private static final int ICON_NUM = 9;

	public int mDefValue;
	public int mMaxValue;

	private Spinner mRateSel;
	private SelectIconViewCustom[] mSelIcon;
	private int mSelIndex;

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

	public BookShelfDeletePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mSP = PreferenceManager.getDefaultSharedPreferences(context);
		mDensity = context.getResources().getDisplayMetrics().scaledDensity;
		mSelIcon = new SelectIconViewCustom[ICON_NUM];

		LAYOUT_PADDING = (int)(4 * mDensity);
	}

	@Override
	protected View onCreateDialogView() {

		boolean found = false;
		// カスタム画像のファイル名を取得
		String[] file = new String[BookShelfBmpFile.length];
		for (int i = 0; i < BookShelfBmpFile.length; i++) {
			file[i] = mSP.getString(BookShelfBmpFile[i], "");
			if (file[i] != "") {
				found = true;
			}
		}
		if (found) {
			// カスタム画像が登録されて入ればToastを表示
			Toast.makeText(mContext, R.string.LoadingBookShelfFile, Toast.LENGTH_SHORT).show();
		}
		ScrollView scroll = new ScrollView(mContext);

		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(LAYOUT_PADDING, LAYOUT_PADDING, LAYOUT_PADDING, LAYOUT_PADDING);

		LinearLayout layout2 = new LinearLayout(mContext);
		layout2.setOrientation(LinearLayout.HORIZONTAL);
		layout2.setGravity(Gravity.CENTER);

		LinearLayout layout3 = new LinearLayout(mContext);
		layout3.setOrientation(LinearLayout.HORIZONTAL);
		layout3.setGravity(Gravity.CENTER);

		LinearLayout layout4 = new LinearLayout(mContext);
		layout4.setOrientation(LinearLayout.HORIZONTAL);
		layout4.setGravity(Gravity.CENTER);

		LinearLayout layout5 = new LinearLayout(mContext);
		layout5.setOrientation(LinearLayout.HORIZONTAL);
		layout5.setGravity(Gravity.CENTER);

		LinearLayout layout6 = new LinearLayout(mContext);
		layout6.setOrientation(LinearLayout.HORIZONTAL);
		layout6.setGravity(Gravity.CENTER);

		TextView text = new TextView(mContext);
		text.setTextSize(DEF.TEXTSIZE_MESSAGE);

		int size = (int)(60 * mDensity);
		// 選択なし
		mSelIcon[0] = new SelectIconViewCustom(mContext, 0, "", 0, 0xFF000000, 0xFF00A0FF);
		// カスタム画像
		mSelIcon[1] = new SelectIconViewCustom(mContext, 1, file[0], 0, 0xFF000000, 0xFF00A0FF);
		mSelIcon[2] = new SelectIconViewCustom(mContext, 1, file[1], 1, 0xFF000000, 0xFF00A0FF);
		mSelIcon[3] = new SelectIconViewCustom(mContext, 1, file[2], 2, 0xFF000000, 0xFF00A0FF);
		mSelIcon[4] = new SelectIconViewCustom(mContext, 1, file[3], 3, 0xFF000000, 0xFF00A0FF);
		mSelIcon[5] = new SelectIconViewCustom(mContext, 1, file[4], 4, 0xFF000000, 0xFF00A0FF);
		mSelIcon[6] = new SelectIconViewCustom(mContext, 1, file[5], 5, 0xFF000000, 0xFF00A0FF);
		mSelIcon[7] = new SelectIconViewCustom(mContext, 1, file[6], 6, 0xFF000000, 0xFF00A0FF);
		mSelIcon[8] = new SelectIconViewCustom(mContext, 1, file[7], 7, 0xFF000000, 0xFF00A0FF);

		mRateSel = new Spinner(mContext);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		scroll.addView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		layout.addView(text, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER_HORIZONTAL;
		layout.addView(layout2, lp);
		layout.addView(layout3, lp);
		layout.addView(layout4, lp);

		layout2.addView(mSelIcon[0], new LayoutParams(size, size));
		layout3.addView(mSelIcon[1], new LayoutParams(size, size));
		layout3.addView(mSelIcon[2], new LayoutParams(size, size));
		layout3.addView(mSelIcon[3], new LayoutParams(size, size));
		layout3.addView(mSelIcon[4], new LayoutParams(size, size));
		layout4.addView(mSelIcon[5], new LayoutParams(size, size));
		layout4.addView(mSelIcon[6], new LayoutParams(size, size));
		layout4.addView(mSelIcon[7], new LayoutParams(size, size));
		layout4.addView(mSelIcon[8], new LayoutParams(size, size));
		String str = (String) getDialogMessage();
		text.setText(str);

		int val;
		// 起動時は選択なしにする
		val = 0;
		for (int i = 0 ; i < ICON_NUM ; i ++) {
			mSelIcon[i].setSelect(i == val ? true : false);
			mSelIcon[i].setOnClickListener(this);
		}
		mSelIndex = val;

		return scroll;
	}


	@Override
	public void onClick(View v) {
		// 選択
		for (int i = 0 ; i < ICON_NUM ; i ++) {
			mSelIcon[i].setSelect(mSelIcon[i] == v ? true : false);
			if (mSelIcon[i] == v) {
				mSelIndex = i;
			}
		}
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			if (mSelIndex > 0) {
				setValue(mSelIndex - 1);
			}
		}
	}

	private void setValue(int index) {
		try {
			// ファイルを削除
			String file = mSP.getString(BookShelfBmpFile[index], "");
			if (file != "") {
				File deletefile = new File(file);
				deletefile.delete();
				SharedPreferences.Editor editor = mSP.edit();
				editor.remove(BookShelfBmpFile[index]);
				editor.apply();
				Toast.makeText(mContext, R.string.DeleteCustomFile, Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Toast.makeText(mContext, R.string.ErrorDeleteCustomFile, Toast.LENGTH_SHORT).show();
		}
	}

}
