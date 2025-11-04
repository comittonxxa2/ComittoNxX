package src.comitton.config.color;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorFibSetting extends ColorPreference {

	public ColorFibSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストモード
		super.setConfig(DEF.KEY_FIBCOLOR, DEF.KEY_FIBRGB, DEF.ColorList[0], true);
	}
}
