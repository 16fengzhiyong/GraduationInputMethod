package com.nuc.omeletteinputmethod.floatwindow.view.niv


import android.content.Context

object Utils {
    fun dp2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}
