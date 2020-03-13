package com.nuc.omeletteinputmethod.kernel.keyboard;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.Xml;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil;


public class Key {
    private float gap;
    private int rowsNumber ;
    private int altCode;
    private String moreKeys;
    private String keySpec;
    private float paddingLeft;
    private float paddingRight;
    private float length;
    private float height;

    public Key(Resources res, KeyboardRow parent, int x, int y, XmlResourceParser parser) {
        TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.MyKeyboard_Key);
//        Log.i("loadKeyboard", "Key: a = "+a.toString());
//        Log.i("loadKeyboard", "Key: parser = "+parser.toString());
        int length = KeyboardUtil.getDimensionOrFraction(a, R.styleable.MyKeyboard_Key_length,
                0, 5);
//        Log.i("loadKeyboard", "Key: length = "+length);
        float lengthf = a.getFraction(R.styleable.MyKeyboard_Key_length,
                100, 100, (float) 100.0);
//        Log.i("loadKeyboard", "Key: lengthf = "+lengthf);
        a.recycle();
    }
    public Key(Resources res, KeyboardRow parent, int rowsNumber, XmlResourceParser parser) {
        this.rowsNumber = rowsNumber;
        TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.MyKeyboard_Key);
        length = a.getFraction(R.styleable.MyKeyboard_Key_length,
                100, 5, 0);
        Log.i("loadKeyboard", "Key: length = " +length);
        height = a.getFraction(R.styleable.MyKeyboard_Key_height,
                100, 5, (float)5);
        paddingLeft = a.getFraction(R.styleable.MyKeyboard_Key_padding_left,
                0, 0, 0);
        paddingRight = a.getFraction(R.styleable.MyKeyboard_Key_padding_right,
                0, 0, 0);
        keySpec = a.getString(R.styleable.MyKeyboard_Key_keySpec);
        moreKeys = a.getString(R.styleable.MyKeyboard_Key_moreKeys);
        a.recycle();
    }

    public void setAltCode(int altCode){
        this.altCode = altCode;
    }
    public int getRowsNumber() {
        return rowsNumber;
    }

    public int getAltCode() {
        return altCode;
    }

    public String getMoreKeys() {
        return moreKeys;
    }

    public String getKeySpec() {
        return keySpec;
    }

    public float getPaddingLeft() {
        return paddingLeft;
    }

    public float getPaddingRight() {
        return paddingRight;
    }

    public float getLength() {
        return length;
    }

    public float getHeight() {
        return height;
    }
}
