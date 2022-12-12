package com.druid.timepaint.utils;

import android.content.res.Resources;


public class DPUtils {
    /**
     * dp转px
     */
    public static int dip2px(float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
