package com.nuc.omeletteinputmethod.omeletteview

import android.content.Context
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet

class OmeletteKeyboardView : KeyboardView {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}
}
