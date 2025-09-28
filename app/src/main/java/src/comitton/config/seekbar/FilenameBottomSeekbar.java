package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class FilenameBottomSeekbar extends SeekBarPreference {

	public FilenameBottomSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_FILENAMEBOTTOMSPACE;
		mMaxValue = DEF.MAX_FILENAMEBOTTOMSPACE;
		super.setKey(DEF.KEY_FILENAMEBOTTOMSPACE);
	}
}
