package com.nuc.omeletteinputmethod.floatwindow.view.niv;


import android.content.Context;

public class Utils {
    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
