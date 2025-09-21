package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class MarginSpaceSeekbar extends SeekBarPreference {

	public MarginSpaceSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_MarginSpace;
		mMaxValue = DEF.MAX_MarginSpace;
		super.setKey(DEF.KEY_MarginSpace);
	}
}
