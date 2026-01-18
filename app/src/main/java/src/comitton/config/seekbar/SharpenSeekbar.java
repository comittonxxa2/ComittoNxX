package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class SharpenSeekbar extends SeekBarPreference {

	public SharpenSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_WEBVIEWSHARPEN;
		mMaxValue = DEF.MAX_WEBVIEWSHARPEN;
		super.setKey(DEF.KEY_WEBVIEWSHARPEN);
	}
}
