package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class BookShelfEdgeLevelSeekbar extends SeekBarPreference {

	public BookShelfEdgeLevelSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_BOOKSHELFEDGELEVEL;
		mMaxValue = DEF.MAX_BOOKSHELFEDGELEVEL;
		super.setKey(DEF.KEY_BOOKSHELFEDGELEVEL);
	}
}
