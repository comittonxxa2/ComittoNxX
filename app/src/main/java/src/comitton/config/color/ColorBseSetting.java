package src.comitton.config.color;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorBseSetting extends ColorPreference {

	public ColorBseSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストモード
		super.setConfig(DEF.KEY_BSECOLOR, DEF.KEY_BSERGB, DEF.ColorList[0], true);
	}
}
