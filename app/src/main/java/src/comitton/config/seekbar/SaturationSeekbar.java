package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class SaturationSeekbar extends SeekBarPreference {

	public SaturationSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_WEBVIEWSATURATION;
		mMaxValue = DEF.MAX_WEBVIEWSATURATION;
		super.setKey(DEF.KEY_WEBVIEWSATURATION);
	}
}
