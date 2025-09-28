package src.comitton.config.color;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorBsfSetting extends ColorPreference {

	public ColorBsfSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストモード
		super.setConfig(DEF.KEY_BSFCOLOR, DEF.KEY_BSFRGB, DEF.ColorList[1], true);
	}
}
