package src.comitton.dialog;

import static src.comitton.imageview.ImageActivity.mImageView;

import java.util.EventListener;

import src.comitton.common.DEF;
import src.comitton.config.SetImageActivity;
import src.comitton.dialog.ListDialog.ListSelectListener;
import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.view.View.OnClickListener;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

@SuppressLint("NewApi")
public class FloatingIconDialog extends TabDialogFragment implements OnClickListener, OnDismissListener, OnSeekBarChangeListener {
	public static final int CLICK_REVERT   = 0;
	public static final int CLICK_OK       = 1;
	public static final int CLICK_APPLY    = 2;

	private final int SELLIST_VIEW_MODE  = 1;

	private FloatingIconListenerInterface mListener = null;

	private ListDialog mListDialog;

	private int mDirectionModeTemp;
	private int mDirectionMode;

	private CheckBox mChkEnable;

	private Button mBtnRevert;
	private Button mBtnApply;
	private Button mBtnOK;
	private Button mBtnEditFloatingIcon;
	private Button mBtnDirectionFloatingIcon;

	private String mDirectionModeTitle;
	private TextView mDummyText1;
	private TextView mDummyText2;

	private boolean mEnable;
	private int mSize;
	private int mHorizontal;
	private int mVertical;
	private int mTransparency;

	private String mSizeStr;
	private String mHorizontalStr;
	private String mVerticalStr;
	private String mTransparencyStr;

	private TextView mTxtSize;
	private TextView mTxtHorizontal;
	private TextView mTxtVertical;
	private TextView mTxtTransparency;
	private SeekBar mSkbSize;
	private SeekBar mSkbHorizontal;
	private SeekBar mSkbVertical;
	private SeekBar mSkbTransparency;

	private String[] mDirectionModeItems;

	private int mSelectMode;
	private AppCompatActivity mActivity;
	private Handler mHandler;

	private static final int[] Direction =
		{ R.string.floatingicondir00
		, R.string.floatingicondir01 };

	public FloatingIconDialog(AppCompatActivity activity, @StyleRes int themeResId, int command_id, boolean isclose, MenuDialog.MenuSelectListener listener, Handler handler) {
		super(activity, themeResId, isclose, false, false, true, listener);

		Resources res = activity.getResources();
		mActivity = activity;
		mHandler = handler;

		int nItem;

		// 向きの設定
		mDirectionModeTitle = res.getString(R.string.directionfloatingicon);
		nItem = Direction.length;
		mDirectionModeItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mDirectionModeItems[i] = res.getString(Direction[i]);
		}

		LayoutInflater inflater = LayoutInflater.from(mActivity);

		addSection(res.getString(R.string.FloatingIconSettingMenu));
		addItem(inflater.inflate(R.layout.floatingiconconfig, null, false));
	}

	public void setConfig(int directionmode, int size, int horizontal, int vertical, int transparency, boolean enable) {

		mDirectionModeTemp  = mDirectionMode  = directionmode;
		mSize = size;
		mHorizontal = horizontal;
		mVertical = vertical;
		mTransparency = transparency;
		mEnable = enable;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);

		Resources res = mActivity.getResources();

		LinearLayout footer = (LinearLayout)inflater.inflate(R.layout.floatingiconconfig_footer, mFooter, true);
		footer.setBackgroundColor(0x80000000);

		for( int i = 0; i < mViewArray.size(); ++i) {

			mChkEnable = mChkEnable != null ? mChkEnable : (CheckBox) mViewArray.get(i).findViewById(R.id.chk_enablefloatingicon);

			mDummyText1 = mDummyText1 != null ? mDummyText1 : (TextView) mViewArray.get(i).findViewById(R.id.label_editfloatingicon);
			mDummyText2 = mDummyText2 != null ? mDummyText2 : (TextView) mViewArray.get(i).findViewById(R.id.label_directionfloatingicon);

			mTxtSize = mTxtSize != null ? mTxtSize : (TextView) mViewArray.get(i).findViewById(R.id.label_sizefloatingicon);
			mTxtHorizontal = mTxtHorizontal != null ? mTxtHorizontal : (TextView) mViewArray.get(i).findViewById(R.id.label_horizontalfloatingicon);
			mTxtVertical = mTxtVertical != null ? mTxtVertical : (TextView) mViewArray.get(i).findViewById(R.id.label_verticalfloatingicon);
			mTxtTransparency = mTxtTransparency != null ? mTxtTransparency : (TextView) mViewArray.get(i).findViewById(R.id.label_transparencyfloatingicon);

			mSkbSize = mSkbSize != null ? mSkbSize : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_sizefloatingicon);
			mSkbHorizontal = mSkbHorizontal != null ? mSkbHorizontal : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_horizontalfloatingicon);
			mSkbVertical = mSkbVertical != null ? mSkbVertical : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_verticalfloatingicon);
			mSkbTransparency = mSkbTransparency != null ? mSkbTransparency : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_transparencyfloatingicon);

			mBtnEditFloatingIcon = mBtnEditFloatingIcon != null ? mBtnEditFloatingIcon : (Button) mViewArray.get(i).findViewById(R.id.btn_editfloatingicon);
			mBtnDirectionFloatingIcon = mBtnDirectionFloatingIcon != null ? mBtnDirectionFloatingIcon : (Button) mViewArray.get(i).findViewById(R.id.btn_directionfloatingicon);

		}

		if (mChkEnable != null) mChkEnable.setChecked(mEnable);

		if (mTxtSize != null) mSizeStr = mTxtSize.getText().toString();
		if (mTxtSize != null) mTxtSize.setText(mSizeStr.replaceAll("%", getSizeStr(mSize)));
		if (mTxtHorizontal != null) mHorizontalStr = mTxtHorizontal.getText().toString();
		if (mTxtHorizontal != null) mTxtHorizontal.setText(mHorizontalStr.replaceAll("%", getHorizontialStr(mHorizontal)));
		if (mTxtVertical != null) mVerticalStr = mTxtVertical.getText().toString();
		if (mTxtVertical != null) mTxtVertical.setText(mVerticalStr.replaceAll("%", getVerticalStr(mVertical)));
		if (mTxtTransparency != null) mTransparencyStr = mTxtTransparency.getText().toString();
		if (mTxtTransparency != null) mTxtTransparency.setText(mTransparencyStr.replaceAll("%", getTransparencyStr(mTransparency)));

		if (mSkbSize != null) mSkbSize.setMax(48);
		if (mSkbSize != null) mSkbSize.setOnSeekBarChangeListener(this);
		if (mSkbSize != null) mSkbSize.setProgress(mSize);

		if (mSkbHorizontal != null) mSkbHorizontal.setMax(100);
		if (mSkbHorizontal != null) mSkbHorizontal.setOnSeekBarChangeListener(this);
		if (mSkbHorizontal != null) mSkbHorizontal.setProgress(mHorizontal);

		if (mSkbVertical != null) mSkbVertical.setMax(100);
		if (mSkbVertical != null) mSkbVertical.setOnSeekBarChangeListener(this);
		if (mSkbVertical != null) mSkbVertical.setProgress(mVertical);

		if (mSkbTransparency != null) mSkbTransparency.setMax(100);
		if (mSkbTransparency != null) mSkbTransparency.setOnSeekBarChangeListener(this);
		if (mSkbTransparency != null) mSkbTransparency.setProgress(mTransparency);

		if (mBtnEditFloatingIcon != null) mBtnEditFloatingIcon.setText(R.string.openfloatingiconsetting);
		if (mBtnDirectionFloatingIcon != null) mBtnDirectionFloatingIcon.setText(mDirectionModeItems[mDirectionMode]);

		if (mBtnEditFloatingIcon != null) mBtnEditFloatingIcon.setOnClickListener(this);
		if (mBtnDirectionFloatingIcon != null) mBtnDirectionFloatingIcon.setOnClickListener(this);

		mBtnOK = (Button) mView.findViewById(R.id.btn_ok);
		mBtnApply = (Button) mView.findViewById(R.id.btn_apply);
		mBtnRevert = (Button) mView.findViewById(R.id.btn_revert);

		mBtnOK.setOnClickListener(this);
		mBtnApply.setOnClickListener(this);
		mBtnRevert.setOnClickListener(this);

		return mView;
	}

	// 設定を取得
	public static int getSize(SharedPreferences sharedPreferences) {
		int val = DEF.getInt(sharedPreferences, DEF.KEY_FLOATINGICONSIZE, DEF.DEFAULT_FLOATINGICONSIZE);
		return val;
	}
	public static int getHorizontal(SharedPreferences sharedPreferences) {
		int val = DEF.getInt(sharedPreferences, DEF.KEY_FLOATINGICONHORIZENTIAL, DEF.DEFAULT_FLOATINGICONHORIZENTIAL);
		return val;
	}
	public static int getVertical(SharedPreferences sharedPreferences) {
		int val = DEF.getInt(sharedPreferences, DEF.KEY_FLOATINGICONVIRTICAL, DEF.DEFAULT_FLOATINGICONVIRTICAL);
		return val;
	}
	public static int getTransparency(SharedPreferences sharedPreferences) {
		int val = DEF.getInt(sharedPreferences, DEF.KEY_FLOATINGICONTRANSPARENCY, DEF.DEFAULT_FLOATINGICONTRANSPARENCY);
		return val;
	}
	public static int getDirectionMode(SharedPreferences sharedPreferences) {
		int val = DEF.getInt(sharedPreferences, DEF.KEY_FLOATINGICONDIRECTIONMODE, DEF.DEFAULT_FLOATINGICONDIRECTIONMODE);
		return val;
	}
	public static boolean getEnable(SharedPreferences sharedPreferences) {
		boolean val = DEF.getBoolean(sharedPreferences, DEF.KEY_FLOATINGICONENABLE, false);
		return val;
	}

	public void setFloatingIconListner(FloatingIconListenerInterface listener) {
		mListener = listener;
	}

	public interface FloatingIconListenerInterface extends EventListener {

	    // メニュー選択された
	    public void onButtonSelect(int select, int size, int horizontal, int vertical, int transparency, int directionmode, boolean enable);
	    public void onClose();
	}

	private void showSelectList(int index) {
		if (mListDialog != null) {
			return;
		}

		// 選択対象
		mSelectMode = index;

		// 選択肢を設定
		String[] items = null;

		String title;
		int selIndex;
		switch (index) {
			case SELLIST_VIEW_MODE:
				// 向き
				title = mDirectionModeTitle;
				items = mDirectionModeItems;
				selIndex = mDirectionModeTemp;
				break;
			default:
				return;
		}
		mListDialog = new ListDialog(mActivity, R.style.MyDialog, title, items, selIndex, new ListSelectListener() {
			@Override
			public void onSelectItem(int index) {
				switch (mSelectMode) {
					case SELLIST_VIEW_MODE:
						// 向き
						mDirectionModeTemp = index;
						mBtnDirectionFloatingIcon.setText(mDirectionModeItems[index]);
						break;
				}
			}

			@Override
			public void onClose() {
				// 終了
				mListDialog = null;
			}
		});
		mListDialog.show();
		return;
	}

	@Override
	public void onClick(View v) {
		if (mBtnEditFloatingIcon == v) {
			// 編集ダイアログ表示
            FloatingIconEditDialog dialog = new FloatingIconEditDialog(mActivity, R.style.MyDialog, mImageView.getWidth(), mImageView.getHeight(), mHandler);
			dialog.show();
			return;
		}
		if (mBtnDirectionFloatingIcon == v) {
			// 向き
			showSelectList(SELLIST_VIEW_MODE);
			return;
		}

		int select = CLICK_REVERT;

		// ボタンクリック
		if (mBtnOK == v) {
			select = CLICK_OK;
		}
		else if (mBtnApply == v) {
			select = CLICK_APPLY;
		}

		if (select == CLICK_REVERT) {
			// 戻す場合は元の値を通知
			mListener.onButtonSelect(select, mSize, mHorizontal, mVertical, mTransparency, mDirectionMode, mEnable);
		}
		else {
			// OK/適用は設定された値を通知
			int size = mSkbSize.getProgress();
			int horizontal = mSkbHorizontal.getProgress();
			int vertical = mSkbVertical.getProgress();
			int transparency = mSkbTransparency.getProgress();
			boolean enable = mChkEnable.isChecked();
			mListener.onButtonSelect(select, size, horizontal, vertical, transparency, mDirectionModeTemp, enable);
		}

		if (select != CLICK_APPLY) {
			// 適用以外では閉じる
			this.dismiss();
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mListener.onClose();
		super.dismiss();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// 変更通知
		if (seekBar == mSkbSize) {
			String str = getSizeStr(progress);
			mTxtSize.setText(mSizeStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbHorizontal) {
			String str = getHorizontialStr(progress);
			mTxtHorizontal.setText(mHorizontalStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbVertical) {
			String str = getVerticalStr(progress);
			mTxtVertical.setText(mVerticalStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbTransparency) {
			String str = getTransparencyStr(progress);
			mTxtTransparency.setText(mTransparencyStr.replaceAll("%", str));
		}
	}

	private String getSizeStr(int progress) {
		Resources res = mActivity.getResources();
		String str;
		str = String.valueOf(progress + 16) + res.getString(R.string.floatingdot);
		return str;
	}
	private String getHorizontialStr(int progress) {
		String str;
		str = String.valueOf(progress) + "%";
		return str;
	}
	private String getVerticalStr(int progress) {
		String str;
		str = String.valueOf(progress) + "%";
		return str;
	}
	private String getTransparencyStr(int progress) {
		String str;
		str = String.valueOf(progress) + "%";
		return str;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// シークバーのトラッキング開始
		return;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// シークバーのトラッキング終了
		return;
	}
}