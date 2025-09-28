package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class ThumbnailBottomSeekbar extends SeekBarPreference {

	public ThumbnailBottomSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_THUMBNAILBOTTOMSPACE;
		mMaxValue = DEF.MAX_THUMBNAILBOTTOMSPACE;
		super.setKey(DEF.KEY_THUMBNAILBOTTOMSPACE);
	}
}
