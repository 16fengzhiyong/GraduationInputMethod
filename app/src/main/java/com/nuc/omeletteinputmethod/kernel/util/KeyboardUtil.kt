package com.nuc.omeletteinputmethod.kernel.util

import android.content.Context
import android.content.res.TypedArray
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.util.TypedValue
import android.view.WindowManager

import com.nuc.omeletteinputmethod.R
import com.nuc.omeletteinputmethod.kernel.OmeletteIME

import java.lang.reflect.Field

class KeyboardUtil(private val omeletteIME: OmeletteIME, private val keyboardView: KeyboardView) {
    private val k1: Keyboard// 字母键盘

    private val listener = object : OnKeyboardActionListener {

        override fun swipeUp() {}

        override fun swipeRight() {}

        override fun swipeLeft() {}

        override fun swipeDown() {}

        override fun onText(text: CharSequence) {}

        override fun onRelease(primaryCode: Int) {}

        override fun onPress(primaryCode: Int) {}

        override fun onKey(primaryCode: Int, keyCodes: IntArray) {
            when (primaryCode) {
                Keyboard.KEYCODE_DELETE -> omeletteIME.deleteText()
                Keyboard.KEYCODE_CANCEL -> omeletteIME.hideInputMethod()
                else -> omeletteIME.commitText(Character.toString(primaryCode.toChar()))
            }
        }
    }

    init {
        keyboardView.setOnKeyboardActionListener(listener)
        k1 = Keyboard(omeletteIME.applicationContext, R.xml.qwerty)
        keyboardView.keyboard = k1
        keyboardView.isEnabled = true
        keyboardView.isPreviewEnabled = true
    }

    companion object {

        fun getDimensionOrFraction(a: TypedArray, index: Int, base: Int, defValue: Int): Int {
            val value = a.peekValue(index) ?: return defValue
            if (value.type == TypedValue.TYPE_DIMENSION) {
                return a.getDimensionPixelOffset(index, defValue)
            } else if (value.type == TypedValue.TYPE_FRACTION) {
                // Round it to avoid values like 47.9999 from getting truncated
                return Math.round(a.getFraction(index, base, base, defValue.toFloat()))
            }
            return defValue
        }

        private var viewHeight: Int = 0
        private var viewWidth: Int = 0

        fun getStatusBarHeight(context: Context): Int {
            var c: Class<*>? = null
            var obj: Any? = null
            var field: Field? = null
            var x = 0
            var statusBarHeight = 0
            try {
                c = Class.forName("com.android.internal.R\$dimen")
                obj = c.newInstance()
                field = c.getField("status_bar_height")
                x = Integer.parseInt(field.get(obj)!!.toString())
                statusBarHeight = context.resources.getDimensionPixelSize(x)
            } catch (e1: Exception) {
                e1.printStackTrace()
            }

            val scale = context.resources.displayMetrics.density
            return (statusBarHeight * scale + 0.5f).toInt()
        }

        fun getViewHeight(context: Context): Int {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            viewHeight = wm.defaultDisplay.height + getStatusBarHeight(context)
            return viewHeight
        }

        fun getViewWidth(context: Context): Int {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            viewWidth = wm.defaultDisplay.width
            return viewWidth
        }
    }
}
