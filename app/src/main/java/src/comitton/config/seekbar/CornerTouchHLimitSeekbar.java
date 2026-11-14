package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class CornerTouchHLimitSeekbar extends SeekBarPreference {

	public CornerTouchHLimitSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_CORNERTOUCHHLIMIT;
		mMaxValue = DEF.MAX_CORNERTOUCHHLIMIT;
		super.setKey(DEF.KEY_CORNERTOUCHHLIMIT);
	}
}
