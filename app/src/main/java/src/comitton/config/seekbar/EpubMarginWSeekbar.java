package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class EpubMarginWSeekbar extends SeekBarPreference {

	public EpubMarginWSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_EP_MARGINW;
		mMaxValue = DEF.MAX_EP_MARGINW;
		super.setKey(DEF.KEY_EP_MARGINW);
	}
}
