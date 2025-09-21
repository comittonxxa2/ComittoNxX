package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class MarginStartSeekbar extends SeekBarPreference {

	public MarginStartSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_MarginStart;
		mMaxValue = DEF.MAX_MarginStart;
		super.setKey(DEF.KEY_MarginStart);
	}
}
