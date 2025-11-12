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

public class ColorMrkSetting extends ColorPreference {

	private static SharedPreferences mSP;

	public ColorMrkSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 背景モード
		super.setConfig(null, DEF.KEY_MRKRGB, 0xFFFF8000, false);
		mSP = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);
		View v = view.findViewById(R.id.indicatormrkrgb);
		int color = mSP.getInt(DEF.KEY_MRKRGB, 0xFFFF8000);
		GradientDrawable border = new GradientDrawable();
		border.setColor(color);
		border.setStroke(3, Color.BLACK);
		v.setBackground(border);
	}
}
