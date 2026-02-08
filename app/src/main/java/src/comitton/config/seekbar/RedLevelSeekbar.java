package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class RedLevelSeekbar extends SeekBarPreference {

	public RedLevelSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_WEBVIEWREDLEVEL;
		mMaxValue = DEF.MAX_WEBVIEWREDLEVEL;
		super.setKey(DEF.KEY_WEBVIEWREDLEVEL);
	}
}
