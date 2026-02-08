package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class KelvinSeekbar extends SeekBarPreference {

	public KelvinSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_WEBVIEWKELVIN;
		mMaxValue = DEF.MAX_WEBVIEWKELVIN;
		super.setKey(DEF.KEY_WEBVIEWKELVIN);
	}
}
