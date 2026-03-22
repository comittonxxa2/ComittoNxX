package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class EpubFontTextSeekbar extends SeekBarPreference {

	public EpubFontTextSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_EP_FONTTEXT;
		mMaxValue = DEF.MAX_EP_FONTTEXT;
		super.setKey(DEF.KEY_EP_FONTTEXT);
	}
}
