package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class MarginRangeSeekbar extends SeekBarPreference {

	public MarginRangeSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_MarginRange;
		mMaxValue = DEF.MAX_MarginRange;
		super.setKey(DEF.KEY_MarginRange);
	}
}
