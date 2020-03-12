package com.nuc.omeletteinputmethod.kernel.keyboard;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;

import androidx.annotation.XmlRes;

import java.util.ArrayList;

public class MyKeyboard {
    // Keyboard XML Tags
    private static final String TAG_KEYBOARD = "MyKeyboard";
    private static final String TAG_ROW = "Row";
    private static final String TAG_KEY = "Key";
    private static final String TAG = "MyKeyboard";
    private ArrayList<KeyboardRow> rows = new ArrayList<>();
    private ArrayList<Key> mKeys = new ArrayList<>();

    public MyKeyboard(){

    }
    public MyKeyboard(Context context, int xmlLayoutResId) {
        this(context, xmlLayoutResId, 0);
    }
    public MyKeyboard(Context context, @XmlRes int xmlLayoutResId, int modeId) {
        //DisplayMetrics dm = context.getResources().getDisplayMetrics();
        loadKeyboard(context, context.getResources().getXml(xmlLayoutResId));
    }
    private void loadKeyboard(Context context, XmlResourceParser parser) {
        boolean inKey = false;
        boolean inRow = false;
        boolean leftMostKey = false;
        int row = 0;
        int x = 0;
        int y = 0;
        Key key = null;
        KeyboardRow currentRow = null;
        Resources res = context.getResources();
        boolean skipRow = false;

        try {
            int event;
            while ((event = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                if (event == XmlResourceParser.START_TAG) {
                    String tag = parser.getName();
                    if (TAG_ROW.equals(tag)) {
                        inRow = true;
                        x = x + 1;
                        currentRow = createRowFromXml(res, parser);
                        rows.add(currentRow);
                        Log.i(TAG, "loadKeyboard: TAG_ROW x :" + x);
//                        skipRow = currentRow.mode != 0 && currentRow.mode != mKeyboardMode;
//                        if (skipRow) {
//                            skipToEndOfRow(parser);
//                            inRow = false;
//                        }
                    } else if (TAG_KEY.equals(tag)) {
                        inKey = true;
                        y = y + 1;
                        key = createKeyFromXml(res, currentRow, x, y, parser);
                        mKeys.add(key);
                        Log.i(TAG, "loadKeyboard: TAG_KEY y :" + y);
//                        if (key.codes[0] == KEYCODE_SHIFT) {
//                            // Find available shift key slot and put this shift key in it
//                            for (int i = 0; i < mShiftKeys.length; i++) {
//                                if (mShiftKeys[i] == null) {
//                                    mShiftKeys[i] = key;
//                                    mShiftKeyIndices[i] = mKeys.size()-1;
//                                    break;
//                                }
//                            }
//                            mModifierKeys.add(key);
//                        } else if (key.codes[0] == KEYCODE_ALT) {
//                            mModifierKeys.add(key);
//                        }
//                        currentRow.mKeys.add(key);
                    } else if (TAG_KEYBOARD.equals(tag)) {
                        parseKeyboardAttributes(res, parser);
                    }
                } else if (event == XmlResourceParser.END_TAG) {
//                    if (inKey) {
//                        inKey = false;
//                        x += key.gap + key.width;
//                        if (x > mTotalWidth) {
//                            mTotalWidth = x;
//                        }
//                    } else if (inRow) {
//                        inRow = false;
//                        y += currentRow.verticalGap;
//                        y += currentRow.defaultHeight;
//                        row++;
//                    } else {
//                        // TODO: error or extend?
//                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }
    }
    /**
     * 解析到<Row>时传递到KeyboardRow
     * {@link KeyboardRow}
     */
    protected KeyboardRow createRowFromXml(Resources res, XmlResourceParser parser) {
        return new KeyboardRow(res, this, parser);
    }
    /**
     * 解析到<Key>时传递到 Key
     * {@link Key}
     */
    protected Key createKeyFromXml(Resources res, KeyboardRow parent, int x, int y,
                                   XmlResourceParser parser) {
        return new Key(res, parent, x, y, parser);
    }

    /**
     * 解析<Keyboard></Keyboard>属性
     */
    private void parseKeyboardAttributes(Resources res, XmlResourceParser parser) {
//        TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser),
//                com.android.internal.R.styleable.Keyboard);
//
//        mDefaultWidth = getDimensionOrFraction(a,
//                com.android.internal.R.styleable.Keyboard_keyWidth,
//                mDisplayWidth, mDisplayWidth / 10);
//        mDefaultHeight = getDimensionOrFraction(a,
//                com.android.internal.R.styleable.Keyboard_keyHeight,
//                mDisplayHeight, 50);
//        mDefaultHorizontalGap = getDimensionOrFraction(a,
//                com.android.internal.R.styleable.Keyboard_horizontalGap,
//                mDisplayWidth, 0);
//        mDefaultVerticalGap = getDimensionOrFraction(a,
//                com.android.internal.R.styleable.Keyboard_verticalGap,
//                mDisplayHeight, 0);
//        mProximityThreshold = (int) (mDefaultWidth * SEARCH_DISTANCE);
//        mProximityThreshold = mProximityThreshold * mProximityThreshold; // Square it for comparison
//        a.recycle();
    }
}

