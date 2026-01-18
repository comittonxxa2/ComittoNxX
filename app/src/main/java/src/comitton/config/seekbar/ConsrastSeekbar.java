package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class ConsrastSeekbar extends SeekBarPreference {

	public ConsrastSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_WEBVIEWCONTRAST;
		mMaxValue = DEF.MAX_WEBVIEWCONTRAST;
		super.setKey(DEF.KEY_WEBVIEWCONTRAST);
	}
}
