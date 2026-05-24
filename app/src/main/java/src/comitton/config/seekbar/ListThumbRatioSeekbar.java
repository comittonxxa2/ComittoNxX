package src.comitton.config.seekbar;


import android.content.Context;
import android.util.AttributeSet;

import src.comitton.common.DEF;

public class ListThumbRatioSeekbar extends SeekBarPreference {

    public ListThumbRatioSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDefValue = DEF.DEFAULT_LISTTHUMBRATIO;
        mMaxValue = DEF.MAX_LISTTHUMBRATIO;
        super.setKey(DEF.KEY_LISTTHUMBRATIO);
    }
}
