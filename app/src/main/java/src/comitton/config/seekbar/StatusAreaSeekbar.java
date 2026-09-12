package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class StatusAreaSeekbar extends SeekBarPreference {

	public StatusAreaSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_STATUSAREA;
		mMaxValue = DEF.MAX_STATUSAREA;
		super.setKey(DEF.KEY_STATUSAREA);
	}
}
