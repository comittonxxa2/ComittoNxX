package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class BookShelfBrightLevelSeekbar extends SeekBarPreference {

	public BookShelfBrightLevelSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_BOOKSHELFBRIGHTLEVEL;
		mMaxValue = DEF.MAX_BOOKSHELFBRIGHTLEVEL;
		super.setKey(DEF.KEY_BOOKSHELFBRIGHTLEVEL);
	}
}
