package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class MarginLevelSeekbar extends SeekBarPreference {

	public MarginLevelSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_MarginLevel;
		mMaxValue = DEF.MAX_MarginLevel;
		super.setKey(DEF.KEY_MarginLevel);
	}
}
