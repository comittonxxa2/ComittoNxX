package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class ThumbnailTopSeekbar extends SeekBarPreference {

	public ThumbnailTopSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_THUMBNAILTOPSPACE;
		mMaxValue = DEF.MAX_THUMBNAILTOPSPACE;
		super.setKey(DEF.KEY_THUMBNAILTOPSPACE);
	}
}
