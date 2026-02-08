package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class BlueLevelSeekbar extends SeekBarPreference {

	public BlueLevelSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_WEBVIEWBLUELEVEL;
		mMaxValue = DEF.MAX_WEBVIEWBLUELEVEL;
		super.setKey(DEF.KEY_WEBVIEWBLUELEVEL);
	}
}
