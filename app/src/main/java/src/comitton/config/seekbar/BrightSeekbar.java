package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class BrightSeekbar extends SeekBarPreference {

	public BrightSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_WEBVIEWBRIGHT;
		mMaxValue = DEF.MAX_WEBVIEWBRIGHT;
		super.setKey(DEF.KEY_WEBVIEWBRIGHT);
	}
}
