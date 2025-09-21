package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class MarginLimitSeekbar extends SeekBarPreference {

	public MarginLimitSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_MarginLimit;
		mMaxValue = DEF.MAX_MarginLimit;
		super.setKey(DEF.KEY_MarginLimit);
	}
}
