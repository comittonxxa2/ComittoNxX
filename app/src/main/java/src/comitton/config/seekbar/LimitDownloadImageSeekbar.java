package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class LimitDownloadImageSeekbar extends SeekBarPreference {

	public LimitDownloadImageSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_MEMLIMIT;
		mMaxValue = DEF.MAX_MEMLIMIT;
		super.setKey(DEF.KEY_MEMLIMIT);
	}
}
