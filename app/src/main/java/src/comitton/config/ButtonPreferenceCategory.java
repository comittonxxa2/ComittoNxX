package src.comitton.config;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import jp.dip.muracoro.comittonx.R;

public class ButtonPreferenceCategory extends PreferenceCategory {

	private OnClickListener mButtonClickListener;
	private Button mButton;

	public interface OnClickListener {
		void onButtonClick();
	}

	public void setOnButtonClickListener(OnClickListener listener) {
		mButtonClickListener = listener;
	}

	public ButtonPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setLayoutResource(R.layout.custom_preference_category);
	}

	public ButtonPreferenceCategory(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.custom_preference_category);
	}

	public ButtonPreferenceCategory(Context context) {
		super(context);
		setLayoutResource(R.layout.custom_preference_category);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		
		TextView titleView = (TextView) view.findViewById(android.R.id.title);
		if (titleView != null && getTitle() != null) {
			titleView.setText(getTitle());
		}

		mButton = (Button) view.findViewById(R.id.category_button);
		if (mButton != null) {
			mButton.setOnClickListener(v -> {
				if (mButtonClickListener != null) {
					mButtonClickListener.onButtonClick();
				}
			});
		}
	}
}
