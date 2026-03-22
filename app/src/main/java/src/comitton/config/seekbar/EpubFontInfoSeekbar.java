package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class EpubFontInfoSeekbar extends SeekBarPreference {

	public EpubFontInfoSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_EP_FONTINFO;
		mMaxValue = DEF.MAX_EP_FONTINFO;
		super.setKey(DEF.KEY_EP_FONTINFO);
	}
}
