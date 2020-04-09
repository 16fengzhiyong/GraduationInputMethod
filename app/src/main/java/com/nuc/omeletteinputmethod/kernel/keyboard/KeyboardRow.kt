package com.nuc.omeletteinputmethod.kernel.keyboard

import android.content.res.Resources
import android.content.res.TypedArray
import android.content.res.XmlResourceParser
import android.util.Xml

import com.nuc.omeletteinputmethod.R
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil

class KeyboardRow(res: Resources, parent: MyKeyboard, parser: XmlResourceParser) {
    var rowHeight: Int = 0
        private set
    var rowVerticalGap: Int = 0
        private set

    init {
        rowHeight = 120
        rowVerticalGap = 10
        val a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.MyKeyboardRow)
        //        Log.i("loadKeyboard", "Key: a = "+a.toString());
        //        Log.i("loadKeyboard", "Key: parser = "+parser.toString());
        rowHeight = KeyboardUtil.getDimensionOrFraction(a, R.styleable.MyKeyboardRow_rowHeight,
                100, 120)
        //        Log.i("loadKeyboard", "Key: length = "+length);
        rowVerticalGap = KeyboardUtil.getDimensionOrFraction(a, R.styleable.MyKeyboardRow_rowVerticalGap,
                100, 20)
        //        Log.i("loadKeyboard", "Key: lengthf = "+lengthf);
        a.recycle()
    }
}
