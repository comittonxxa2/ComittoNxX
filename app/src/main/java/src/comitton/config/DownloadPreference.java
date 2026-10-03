package src.comitton.config;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import jp.dip.muracoro.comittonx.R;

public class DownloadPreference extends Preference {

	private View.OnClickListener mButtonListener;

	public DownloadPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public DownloadPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DownloadPreference(Context context) {
		super(context);
		init();
	}

	private void init() {
		// レイアウトを割り当て
		setLayoutResource(R.layout.preference_download_button);
		setSelectable(false);
	}

	// ボタンがクリックされたときのリスナーを設定するメソッド
	public void setOnButtonClickListener(View.OnClickListener listener) {
		mButtonListener = listener;
		notifyChanged();
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		
		Button downloadButton = (Button) view.findViewById(R.id.btn_download);
		if (downloadButton != null) {
			downloadButton.setOnClickListener(mButtonListener);
		}
	}
}
