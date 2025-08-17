package src.comitton.dialog;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;

import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;

// プログレスダイアログ互換クラス
// APIレベル26からProgressDialogが非推奨なため互換ダイアログを作成
public class CustomProgressDialog extends DialogFragment {
	private ProgressBar progressBar;
	private TextView progress1TextView;
	private TextView progress2TextView;
	private String mMessage;
	private Handler mHandler;
	private String mTitle;
	private boolean mCancelableOutsideTouch;

	public CustomProgressDialog(String title, String message, boolean cancelableOutsideTouch, Handler handler) {
		mMessage = message;
		mTitle = title;
		mCancelableOutsideTouch = cancelableOutsideTouch;
		mHandler = handler;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// ダイアログを作るためのビルダーを作成
		// ビルダーでタイトルやメッセージを設定
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.MyDialog);

		// レイアウトを設定
		LayoutInflater inflater = requireActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.customprogressdialog, null);
		// プログレスバーを設定
		progressBar = view.findViewById(R.id.progress_bar);
		progress1TextView = view.findViewById(R.id.progress_1_text_view);
		progress2TextView = view.findViewById(R.id.progress_2_text_view);
		// ダイアログのタイトル文を設定
		builder.setTitle(mTitle);
		// ダイアログのメッセージ文を設定
		builder.setMessage(mMessage);
		builder.setView(view);
		// 設定したダイアログを作成
		AlertDialog dialog = builder.create();
		// 周りをタッチしてダイアログをキャンセルさせるかどうかを設定
		dialog.setCanceledOnTouchOutside(mCancelableOutsideTouch);
		// ダイアログを返す
		return dialog;
    }

	@Override
	public void onCancel(DialogInterface dialog){
		// DialogFragmentはsetOnCancelListenerをオーバーライドできないので代わりにメッセージを送ることで処理を代替えする
		if (mHandler != null) {
			// 周りをタッチしてダイアログをキャンセルさせた場合はメッセージを送る
			Message message = new Message();
			message.what = DEF.HMSG_PROGRESS_CANCEL;
			mHandler.sendMessage(message);
		}
	}

	// プログレスバーの位置を設定
	public void setProgress(int progress) {
		if (progressBar != null) {
			progressBar.setProgress(progress);
			progress1TextView.setText(progress + "%");
			progress2TextView.setText(progress + "/100");
		}
	}
}
