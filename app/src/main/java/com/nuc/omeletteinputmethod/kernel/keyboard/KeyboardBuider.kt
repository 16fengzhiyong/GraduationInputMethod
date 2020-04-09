package com.nuc.omeletteinputmethod.kernel.keyboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout

import com.nuc.omeletteinputmethod.R
import com.nuc.omeletteinputmethod.kernel.OmeletteIME
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil

import java.lang.reflect.Field

class KeyboardBuider(internal var omeletteIME: OmeletteIME, internal var xmlLayoutResId: Int) {
    var keyboard: MyKeyboard
        internal set

    init {
        keyboard = MyKeyboard(omeletteIME, xmlLayoutResId)

    }


}
