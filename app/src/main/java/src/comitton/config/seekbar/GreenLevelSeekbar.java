package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class GreenLevelSeekbar extends SeekBarPreference {

	public GreenLevelSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_WEBVIEWGREENLEVEL;
		mMaxValue = DEF.MAX_WEBVIEWGREENLEVEL;
		super.setKey(DEF.KEY_WEBVIEWGREENLEVEL);
	}
}
