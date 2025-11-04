package src.comitton.config.color;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorFifSetting extends ColorPreference {

	public ColorFifSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストモード
		super.setConfig(DEF.KEY_FIFCOLOR, DEF.KEY_FIFRGB, DEF.ColorList[1], true);
	}
}
