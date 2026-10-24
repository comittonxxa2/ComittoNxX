package src.comitton.dialog;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;

@SuppressLint("NewApi")
public class WebShareIconDialog extends ImmersiveDialog implements OnClickListener, OnDismissListener {

	private TextView mTxtTitle;
	private TextView mTxtMessage;
	private Button mBtnCancel;
	private Button mBtnOk;
	private String mTitle;
	private String mMessage;
	private String mOkmessage;
	MessageListener mListener;

	public WebShareIconDialog(AppCompatActivity activity, @StyleRes int themeResId, String title, String message,String okmessage, MessageListener listener) {
		super(activity, themeResId, true);
		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);
		mTitle = title != null ? title : "";
		mMessage = message != null ? message : "";
		mOkmessage = okmessage != null ? okmessage : "";
		mListener = listener;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ボタン2つ用のレイアウトを指定
		setContentView(R.layout.websharingicondialog);
		mTxtTitle = (TextView) this.findViewById(R.id.txt_title);
		mTxtMessage = (TextView) this.findViewById(R.id.txt_message);
		mBtnCancel = (Button) this.findViewById(R.id.btn_cancel);
		mBtnOk = (Button) this.findViewById(R.id.btn_ok);
		mTxtTitle.setText(mTitle);
		mTxtMessage.setText(mMessage);
		mBtnOk.setText(mOkmessage);
		// ボタンのクリックリスナー設定
		mBtnCancel.setOnClickListener(this);
		mBtnOk.setOnClickListener(this);
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_MENU:
					dismiss();
					break;
				case KeyEvent.KEYCODE_ENTER:
				case KeyEvent.KEYCODE_DPAD_CENTER:
					// エンターキーなどでOKボタンを押した挙動にする
					onClick(mBtnOk);
					break;
			}
		}
		return super.dispatchKeyEvent(event);
	}
	public interface MessageListener extends EventListener {
		public void onOk();
		public void onCancel();
		public void onClose();
	}
	@Override
	public void onClick(View v) {
		if (v == mBtnOk) {
			if (mListener != null) {
				mListener.onOk();
			}
		}
		else {
			// キャンセルボタンクリック
			if (mListener != null) {
				mListener.onCancel();
			}
		}
		dismiss();
	}
	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mListener != null) {
			mListener.onClose();
		}
	}
}
