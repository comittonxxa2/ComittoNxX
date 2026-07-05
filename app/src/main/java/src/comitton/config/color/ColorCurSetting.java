package src.comitton.config.color;

import src.comitton.common.DEF;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import jp.dip.muracoro.comittonx.R;
import src.comitton.common.MultiProcessPreferences;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class ColorCurSetting extends ColorPreference {

	private static SharedPreferences mSP;

	public ColorCurSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 背景モード
		super.setConfig(null, DEF.KEY_CURRGB, 0xFF8080FF, false);
		mSP = MultiProcessPreferences.getInstance(context);
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);
		View v = view.findViewById(R.id.indicatorcurrgb);
		int color = mSP.getInt(DEF.KEY_CURRGB, 0xFF8080FF);
		GradientDrawable border = new GradientDrawable();
		border.setColor(color);
		border.setStroke(3, Color.BLACK);
		v.setBackground(border);
	}
}
