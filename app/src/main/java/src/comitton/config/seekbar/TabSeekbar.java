package src.comitton.config.seekbar;


import android.content.Context;
import android.util.AttributeSet;

import src.comitton.common.DEF;

public class TabSeekbar extends SeekBarPreference {

    public TabSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDefValue = DEF.DEFAULT_TABSEEK;
        mMaxValue = DEF.MAX_TABSEEK;
        super.setKey(DEF.KEY_TABSEEK);
    }
}
