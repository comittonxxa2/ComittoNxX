package src.comitton.config.color;

import src.comitton.common.DEF;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import jp.dip.muracoro.comittonx.R;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class ColorTvbSetting extends ColorPreference {

	private static SharedPreferences mSP;

	public ColorTvbSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストビューアの背景
		super.setConfig(null, DEF.KEY_TX_TVBRGB, DEF.COLOR_TX_TVBRGB, false);
		mSP = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);
		View v = view.findViewById(R.id.indicatortxtvbrgb);
		int color = mSP.getInt(DEF.KEY_TX_TVBRGB, DEF.COLOR_TX_TVBRGB);
		GradientDrawable border = new GradientDrawable();
		border.setColor(color);
		border.setStroke(3, Color.BLACK);
		v.setBackground(border);
	}
}
