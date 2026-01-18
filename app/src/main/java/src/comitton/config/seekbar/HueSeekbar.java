package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class HueSeekbar extends SeekBarPreference {

	public HueSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_WEBVIEWHUE;
		mMaxValue = DEF.MAX_WEBVIEWHUE;
		super.setKey(DEF.KEY_WEBVIEWHUE);
	}
}
