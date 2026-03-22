package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class EpubMarginHSeekbar extends SeekBarPreference {

	public EpubMarginHSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_EP_MARGINH;
		mMaxValue = DEF.MAX_EP_MARGINH;
		super.setKey(DEF.KEY_EP_MARGINH);
	}
}
