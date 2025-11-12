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

public class ColorTldSetting extends ColorPreference {

	private static SharedPreferences mSP;

	public ColorTldSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 背景モード
		super.setConfig(null, DEF.KEY_TLDRGB, 0xFF404040, true);
		mSP = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);
		View v = view.findViewById(R.id.indicatortldrgb);
		int color = mSP.getInt(DEF.KEY_TLDRGB, 0xFF404040);
		GradientDrawable border = new GradientDrawable();
		border.setColor(color);
		border.setStroke(3, Color.BLACK);
		v.setBackground(border);
	}
}
