package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class NavigationAreaSeekbar extends SeekBarPreference {

	public NavigationAreaSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_NAVIGATIONAREA;
		mMaxValue = DEF.MAX_NAVIGATIONAREA;
		super.setKey(DEF.KEY_NAVIGATIONAREA);
	}
}
