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

public class ColorTvgSetting extends ColorPreference {

	private static SharedPreferences mSP;

	public ColorTvgSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストビューアの背景(グラデーション用)
		super.setConfig(null, DEF.KEY_TX_TVGRGB, DEF.COLOR_TX_TVGRGB, false);
		mSP = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);
		View v = view.findViewById(R.id.indicatortxtvgrgb);
		int color = mSP.getInt(DEF.KEY_TX_TVGRGB, DEF.COLOR_TX_TVGRGB);
		GradientDrawable border = new GradientDrawable();
		border.setColor(color);
		border.setStroke(3, Color.BLACK);
		v.setBackground(border);
	}
}
