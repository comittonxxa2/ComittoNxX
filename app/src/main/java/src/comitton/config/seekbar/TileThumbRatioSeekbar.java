package src.comitton.config.seekbar;


import android.content.Context;
import android.util.AttributeSet;

import src.comitton.common.DEF;

public class TileThumbRatioSeekbar extends SeekBarPreference {

    public TileThumbRatioSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDefValue = DEF.DEFAULT_TILETHUMBRATIO;
        mMaxValue = DEF.MAX_TILETHUMBRATIO;
        super.setKey(DEF.KEY_TILETHUMBRATIO);
    }
}
