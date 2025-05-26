package src.comitton.config.color;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorRrbSetting extends ColorPreference {

	public ColorRrbSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストモード
		super.setConfig(DEF.KEY_RRBCOLOR, DEF.KEY_RRBRGB, DEF.ColorList[23], true);
	}
}
