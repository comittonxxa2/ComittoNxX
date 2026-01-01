package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class ScrlDiagSeekbar extends SeekBarPreference {

	public ScrlDiagSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_SCRLDIAG;
		mMaxValue = DEF.MAX_SCRLDIAG;
		super.setKey(DEF.KEY_SCRLDIAG);
	}
}
