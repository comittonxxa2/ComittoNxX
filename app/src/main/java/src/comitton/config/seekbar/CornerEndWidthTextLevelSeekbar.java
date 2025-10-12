package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class CornerEndWidthTextLevelSeekbar extends SeekBarPreference {

	public CornerEndWidthTextLevelSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_CORNERENDLEVEL;
		mMaxValue = DEF.MAX_CORNERENDLEVEL;
		super.setKey(DEF.KEY_CORNERENDTWIDTHLEVEL);
	}
}
