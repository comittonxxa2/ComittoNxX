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

public class ColorAftSetting extends ColorPreference {

	private static SharedPreferences mSP;

	public ColorAftSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストモード
		super.setConfig(DEF.KEY_AFTCOLOR, DEF.KEY_AFTRGB, DEF.ColorList[8], true);
		mSP = MultiProcessPreferences.getInstance(context);
	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);
		View v = view.findViewById(R.id.indicatoraftrgb);
		int color = mSP.getInt(DEF.KEY_AFTRGB, DEF.ColorList[8]);
		GradientDrawable border = new GradientDrawable();
		border.setColor(color);
		border.setStroke(3, Color.BLACK);
		v.setBackground(border);
	}
}
