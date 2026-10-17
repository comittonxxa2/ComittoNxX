package src.comitton.config;

import android.content.Context;
import android.content.res.Resources;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import jp.dip.muracoro.comittonx.R;

public class DownloadPreference extends Preference {

	private View.OnClickListener mButtonListener;
	private CharSequence mButtonText;
	private Context mContext;

	public DownloadPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}

	public DownloadPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public DownloadPreference(Context context) {
		super(context);
		mContext = context;
		init();
	}

	private void init() {
		// レイアウトを割り当て
		setLayoutResource(R.layout.preference_download_button);
		setSelectable(false);
        Resources res = mContext.getResources();
		mButtonText = res.getString(R.string.dlTitle);
	}

	// ボタンがクリックされたときのリスナーを設定するメソッド
	public void setOnButtonClickListener(View.OnClickListener listener) {
		mButtonListener = listener;
		notifyChanged();
	}

    public void setButtonText(int resId) {
	   setButtonText(getContext().getString(resId));
    }

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		
		Button downloadButton = (Button) view.findViewById(R.id.btn_download);
		if (downloadButton != null) {
			downloadButton.setOnClickListener(mButtonListener);
			if (mButtonText != null) {
				downloadButton.setText(mButtonText);
			}
		}
	}

	public void setButtonText(CharSequence text) {
		mButtonText = text;
		notifyChanged();
	}
}
