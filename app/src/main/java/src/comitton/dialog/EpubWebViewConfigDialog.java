package src.comitton.dialog;

import java.util.EventListener;

import src.comitton.common.DEF;
import src.comitton.config.SetTextActivity;
import src.comitton.dialog.ListDialog.ListSelectListener;
import jp.dip.muracoro.comittonx.R;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.view.View.OnClickListener;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("NewApi")
public class EpubWebViewConfigDialog extends TabDialogFragment implements OnClickListener, OnDismissListener, OnSeekBarChangeListener {
	public static final int CLICK_REVERT   = 0;
	public static final int CLICK_OK       = 1;
	public static final int CLICK_APPLY    = 2;

	private EpubWebViewConfigListenerInterface mListener = null;

	private ListDialog mListDialog;

	private int mFontText;
	private int mFontBody;
	private int mFontInfo;
	private int mMarginW;
	private int mMarginH;
	private boolean mIsSave;

	// ボタンは現在値を覚える必要がある
	private Button mBtnRevert;
	private Button mBtnApply;
	private Button mBtnOK;

	private TextView mTxtFontText;
	private TextView mTxtFontBody;
	private TextView mTxtFontInfo;
	private TextView mTxtMarginW;
	private TextView mTxtMarginH;
	private SeekBar mSkbFontText;
	private SeekBar mSkbFontBody;
	private SeekBar mSkbFontInfo;
	private SeekBar mSkbMarginW;
	private SeekBar mSkbMarginH;
	private CheckBox mChkIsSave;

	private String mFontTextStr;
	private String mFontBodyStr;
	private String mFontInfoStr;
	private String mMarginWStr;
	private String mMarginHStr;

	private String mDefaultStr;
	private String mSpUnitStr;
	private String mDotUnitStr;
	private String mSpSrngStr;

	private boolean mFontTextEnable;

	public EpubWebViewConfigDialog(AppCompatActivity activity, @StyleRes int themeResId, boolean isclose, MenuDialog.MenuSelectListener listener) {
		super(activity, themeResId, isclose, false, false, true, listener);

		Resources res = mActivity.getResources();
		mDefaultStr = res.getString(R.string.auto);
		mSpUnitStr = res.getString(R.string.unitSumm1);
		mDotUnitStr = res.getString(R.string.rangeSumm1);
		mSpSrngStr = res.getString(R.string.srngSumm2);

		int nItem;

		LayoutInflater inflater = LayoutInflater.from(mActivity);

		addSection(res.getString(R.string.txtConfFormat));
		addItem(inflater.inflate(R.layout.epubwebviewconfig_format, null, false));

	}

	public void setConfig(int top, int body, int info, int marginw, int marginh, boolean topdisable, boolean issave) {
		mFontText = top;
		mFontBody = body;
		mFontInfo = info;
		mMarginW = marginw;
		mMarginH = marginh;
		mFontTextEnable = topdisable;

		mIsSave = issave;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);

		Resources res = mActivity.getResources();
		addHeader(res.getString(R.string.txtConfMenu));

		LinearLayout footer = (LinearLayout)inflater.inflate(R.layout.imagetextconfig_footer, null, false);
		footer.setBackgroundColor(0x80000000);
		// Android 5.1でテキストの色がおかしかったので暫定
		((CheckBox)footer.findViewById(R.id.chk_save)).setTextAppearance(mActivity, mThemeResId);
		addFooter(footer);

		mChkIsSave = (CheckBox) mView.findViewById(R.id.chk_save);

		mChkIsSave.setChecked(mIsSave);

		for( int i = 0; i < mViewArray.size(); ++i) {
			mTxtFontText = mTxtFontText != null ? mTxtFontText : (TextView) mViewArray.get(i).findViewById(R.id.label_fontepubtext);
			mTxtFontBody = mTxtFontBody != null ? mTxtFontBody : (TextView) mViewArray.get(i).findViewById(R.id.label_fontepubbody);
			mTxtFontInfo = mTxtFontInfo != null ? mTxtFontInfo : (TextView) mViewArray.get(i).findViewById(R.id.label_fontinfo);
			mTxtMarginW = mTxtMarginW != null ? mTxtMarginW : (TextView) mViewArray.get(i).findViewById(R.id.label_marginw);
			mTxtMarginH = mTxtMarginH != null ? mTxtMarginH : (TextView) mViewArray.get(i).findViewById(R.id.label_marginh);

			mSkbFontText = mSkbFontText != null ? mSkbFontText : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_fontepubtext);
			mSkbFontBody = mSkbFontBody != null ? mSkbFontBody : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_fontepubbody);
			mSkbFontInfo = mSkbFontInfo != null ? mSkbFontInfo : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_fontinfo);
			mSkbMarginW = mSkbMarginW != null ? mSkbMarginW : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_marginw);
			mSkbMarginH = mSkbMarginH != null ? mSkbMarginH : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_marginh);
		}

		mFontTextStr  = mTxtFontText.getText().toString();
		mFontBodyStr = mTxtFontBody.getText().toString();
		mFontInfoStr = mTxtFontInfo.getText().toString();
		mMarginWStr  = mTxtMarginW.getText().toString();
		mMarginHStr  = mTxtMarginH.getText().toString();

		mTxtFontText.setText(mFontTextStr.replaceAll("%", getBkLight(mFontText)));
		mTxtFontBody.setText(mFontBodyStr.replaceAll("%", getBkLight(mFontBody)));
		mTxtFontInfo.setText(mFontInfoStr.replaceAll("%", getBkLight(mFontInfo)));
		mTxtMarginW.setText(mMarginWStr.replaceAll("%", getBkLight(mMarginW)));
		mTxtMarginH.setText(mMarginHStr.replaceAll("%", getBkLight(mMarginH)));

		mSkbFontText.setMax(50);
		mSkbFontText.setOnSeekBarChangeListener(this);
		mSkbFontBody.setMax(100);
		mSkbFontBody.setOnSeekBarChangeListener(this);
		mSkbFontInfo.setMax(56);
		mSkbFontInfo.setOnSeekBarChangeListener(this);
		mSkbMarginW.setMax(50);
		mSkbMarginW.setOnSeekBarChangeListener(this);
		mSkbMarginH.setMax(50);
		mSkbMarginH.setOnSeekBarChangeListener(this);

		mSkbFontText.setProgress(mFontText);
		mSkbFontBody.setProgress(mFontBody);
		mSkbFontInfo.setProgress(mFontInfo);
		mSkbMarginW.setProgress(mMarginW);
		mSkbMarginH.setProgress(mMarginH);

		mBtnOK = (Button) mView.findViewById(R.id.btn_ok);
		mBtnApply = (Button) mView.findViewById(R.id.btn_apply);
		mBtnRevert = (Button) mView.findViewById(R.id.btn_revert);

		mBtnOK.setOnClickListener(this);
		mBtnApply.setOnClickListener(this);
		mBtnRevert.setOnClickListener(this);

		if (!mFontTextEnable) {
			mTxtFontText.setEnabled(false);
			mSkbFontText.setEnabled(false);
		}

		return mView;
	}

	public void setTextConfigListner(EpubWebViewConfigListenerInterface listener) {
		mListener = listener;
	}

	public interface EpubWebViewConfigListenerInterface extends EventListener {

	    // メニュー選択された
	    public void onButtonSelect(int select, int top, int body, int info, int marginw, int margin, boolean issave);
	    public void onClose();
	}

	@Override
	public void onClick(View v) {

		int select = CLICK_REVERT;

		// ボタンクリック
		if (mBtnOK == v) {
			select = CLICK_OK;
		}
		else if (mBtnApply == v) {
			select = CLICK_APPLY;
		}

		if (select == CLICK_REVERT) {
			// 戻すは元の値を通知
			mListener.onButtonSelect(select, mFontText, mFontBody, mFontInfo, mMarginW, mMarginH, mIsSave);
		}
		else {
			// OK/適用は設定された値を通知
			boolean issave = mChkIsSave.isChecked();
			int top = mSkbFontText.getProgress();
			int body = mSkbFontBody.getProgress();
			int info = mSkbFontInfo.getProgress();
			int marginw = mSkbMarginW.getProgress();
			int marginh = mSkbMarginH.getProgress();

			mListener.onButtonSelect(select, top, body, info, marginw, marginh, issave);
		}

		if (select != CLICK_APPLY) {
			// 適用以外では閉じる
			dismiss();
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
		if (seekBar == mSkbFontInfo) {
			String str = DEF.getFontSpStr(progress, mSpUnitStr);
			// ヘッダフッタフォント
			mTxtFontInfo.setText(mFontInfoStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbMarginW || seekBar == mSkbMarginH) {
			String str = DEF.getDispMarginStr(progress, mDotUnitStr);
			if (seekBar == mSkbMarginW) {
				// 左右余白
				mTxtMarginW.setText(mMarginWStr.replaceAll("%", str));
			}
			else {
				// 上下余白
				mTxtMarginH.setText(mMarginHStr.replaceAll("%", str));
			}
		}
		else {
			String str = DEF.getDispMarginStr(progress, mSpSrngStr);
			if (seekBar == mSkbFontText) {
				// テキストの拡大率
				mTxtFontText.setText(mFontTextStr.replaceAll("%", str));
			}
			else if (seekBar == mSkbFontBody) {
				// 本文フォントの拡大率
				mTxtFontBody.setText(mFontBodyStr.replaceAll("%", str));
			}
		}
		return;
	}

	private String getBkLight(int progress) {
		String str;
		if (progress >= 11) {
			str = mDefaultStr;
		}
		else {
			str = String.valueOf(progress * 10) + "%";
		}
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