package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class GammaSeekbar extends SeekBarPreference {

	public GammaSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_WEBVIEWGAMMA;
		mMaxValue = DEF.MAX_WEBVIEWGAMMA;
		super.setKey(DEF.KEY_WEBVIEWGAMMA);
	}
}
