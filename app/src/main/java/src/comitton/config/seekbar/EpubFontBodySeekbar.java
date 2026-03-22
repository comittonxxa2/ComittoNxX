package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class EpubFontBodySeekbar extends SeekBarPreference {

	public EpubFontBodySeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_EP_FONTBODY;
		mMaxValue = DEF.MAX_EP_FONTBODY;
		super.setKey(DEF.KEY_EP_FONTBODY);
	}
}
