package com.nuc.omeletteinputmethod.kernel.keyboard

import android.content.res.Resources
import android.content.res.TypedArray
import android.content.res.XmlResourceParser
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.util.Xml

import com.nuc.omeletteinputmethod.R
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil


class Key {
    var rect: RectF? = null
    var gap = 0f
    var rowsNumber: Int = 0
    var altCode: Int = 0
    var moreKeys: String? = null
    var keySpec: String? = null
    var paddingLeft: Float = 0.toFloat()
    var paddingRight: Float = 0.toFloat()
    var length: Float = 0.0f
    var height: Float = 0.0f
    var startingPosition: Float = 0.toFloat()
    var keyStyle: Int = 0
    var isIfimagekey: Boolean = false
    var imgresource: Int = 0
        private set

    constructor(res: Resources, parent: KeyboardRow, x: Int, y: Int, parser: XmlResourceParser) {
        val a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.MyKeyboard_Key)
        //        Log.i("loadKeyboard", "Key: a = "+a.toString());
        //        Log.i("loadKeyboard", "Key: parser = "+parser.toString());
        val length = KeyboardUtil.getDimensionOrFraction(a, R.styleable.MyKeyboard_Key_length,
                0, 5)
        //        Log.i("loadKeyboard", "Key: length = "+length);
        val lengthf = a.getFraction(R.styleable.MyKeyboard_Key_length,
                100, 100, 100.0.toFloat())
        //        Log.i("loadKeyboard", "Key: lengthf = "+lengthf);
        a.recycle()
    }

    constructor(res: Resources, parent: KeyboardRow, rowsNumber: Int, parser: XmlResourceParser) {
        this.rowsNumber = rowsNumber
        val a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.MyKeyboard_Key)
        length = a.getFraction(R.styleable.MyKeyboard_Key_length,
                100, 5, 0f)
        Log.i("loadKeyboard", "Key: length = $length")
        height = a.getFraction(R.styleable.MyKeyboard_Key_height,
                100, 5, 5.toFloat())
        gap = a.getFraction(R.styleable.MyKeyboard_Key_padding, 100, 5, 0f)
        startingPosition = a.getFraction(R.styleable.MyKeyboard_Key_startingPosition, 100, 5, 0f)
        keySpec = a.getString(R.styleable.MyKeyboard_Key_keySpec)
        moreKeys = a.getString(R.styleable.MyKeyboard_Key_moreKeys)
        keyStyle = a.getInt(R.styleable.MyKeyboard_Key_keyStyle, 0)
        altCode = a.getInteger(R.styleable.MyKeyboard_Key_altCode, 0)
        isIfimagekey = a.getBoolean(R.styleable.MyKeyboard_Key_ifimagekey, false)
        if (isIfimagekey) {
            imgresource = a.getResourceId(R.styleable.MyKeyboard_Key_imgresource, 0)
        }
        a.recycle()
    }
}
