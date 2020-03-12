package com.nuc.omeletteinputmethod.kernel.keyboard;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.Xml;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil;


public class Key {
    public Key(Resources res, KeyboardRow parent, int x, int y, XmlResourceParser parser) {
        TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.MyKeyboard_Key);
        Log.i("loadKeyboard", "Key: a = "+a.toString());
        Log.i("loadKeyboard", "Key: parser = "+parser.toString());
        int length = KeyboardUtil.getDimensionOrFraction(a, R.styleable.MyKeyboard_Key_length,
                0, 5);
        Log.i("loadKeyboard", "Key: length = "+length);
        a.recycle();
    }

}
