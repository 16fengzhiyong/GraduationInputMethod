package com.nuc.omeletteinputmethod.kernel.keyboard;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.util.Xml;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil;


public class Key {
    private RectF rect;
    private float gap = 0;
    private int rowsNumber ;
    private int altCode;
    private String moreKeys;
    private String keySpec;
    private float paddingLeft;
    private float paddingRight;
    private float length;
    private float height;
    private float startingPosition;
    private int keyStyle;
    private boolean ifimagekey;
    private int imgresource;
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
        gap = a.getFraction(R.styleable.MyKeyboard_Key_padding,100,5,0);
        startingPosition = a.getFraction(R.styleable.MyKeyboard_Key_startingPosition,100,5,0);
        keySpec = a.getString(R.styleable.MyKeyboard_Key_keySpec);
        moreKeys = a.getString(R.styleable.MyKeyboard_Key_moreKeys);
        keyStyle = a.getInt(R.styleable.MyKeyboard_Key_keyStyle,0);
        altCode = a.getInteger(R.styleable.MyKeyboard_Key_altCode,0);
        ifimagekey = a.getBoolean(R.styleable.MyKeyboard_Key_ifimagekey,false);
        if (ifimagekey){
            imgresource = a.getResourceId(R.styleable.MyKeyboard_Key_imgresource,0);
        }
        a.recycle();
    }


    public boolean isIfimagekey() {
        return ifimagekey;
    }

    public int getImgresource() {
        return imgresource;
    }

    public void setKeySpec(String keySpec) {
        this.keySpec = keySpec;
    }

    public int getKeyStyle() {
        return keyStyle;
    }

    public void setKeyStyle(int keyStyle) {
        this.keyStyle = keyStyle;
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

    public float getGap() {
        return gap;
    }

    public RectF getRect() {
        return rect;
    }

    public void setRect(RectF rect) {
        this.rect = rect;
    }

    public float getStartingPosition() {
        return startingPosition;
    }

    public void setStartingPosition(float startingPosition) {
        this.startingPosition = startingPosition;
    }
}
