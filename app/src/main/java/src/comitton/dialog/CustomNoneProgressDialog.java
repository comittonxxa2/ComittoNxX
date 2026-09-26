package src.comitton.dialog;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
public class CustomNoneProgressDialog extends DialogFragment {
	private String mMessage;
	private Handler mHandler;
	private String mTitle;

	public CustomNoneProgressDialog(String title, String message, Handler handler) {
		mMessage = message;
		mTitle = title;
		mHandler = handler;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// ダイアログを作るためのビルダーを作成
		// ビルダーでタイトルやメッセージを設定
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.MyNoneDialog);

		// レイアウトを設定
		LayoutInflater inflater = requireActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.customnoneprogressdialog, null);
		// ダイアログのタイトル文を設定
		builder.setTitle(mTitle);
		// ダイアログのメッセージ文を設定
		builder.setMessage(mMessage);
		builder.setView(view);
		// 設定したダイアログを作成
		AlertDialog dialog = builder.create();
		// 周りをタッチしてダイアログをキャンセルさせるかどうかを設定
		dialog.setCanceledOnTouchOutside(false);
		// ダイアログを返す
		return dialog;
    }
}
