package com.shaeen.launcher.util;

import android.content.Context;
import android.util.TypedValue;

public class DpPxUtil {

    public static int dpToPx(Context context, int dp) {
        return ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics()));
    }
}
