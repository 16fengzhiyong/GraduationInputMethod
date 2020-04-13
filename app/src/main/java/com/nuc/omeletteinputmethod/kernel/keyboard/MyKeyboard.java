package com.nuc.omeletteinputmethod.kernel.keyboard;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;

import androidx.annotation.XmlRes;

import com.nuc.omeletteinputmethod.kernel.OmeletteIME;
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil;

import java.util.ArrayList;

public class MyKeyboard {
    // Keyboard XML Tags
    private static final String TAG_KEYBOARD = "MyKeyboard";
    private static final String TAG_ROW = "Row";
    private static final String TAG_KEY = "Key";
    private static final String TAG = "MyKeyboard";
    private int keyboardWidth;
    private int keyboardHeight;
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
        float Bfx = 0;
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
                        x = 0;
                        currentRow = createRowFromXml(res, parser);
                        rows.add(currentRow);
                        y = currentRow.getRowHeight() + y;
                        Log.i(TAG, "loadKeyboard: TAG_ROW y :" + y);
                    } else if (TAG_KEY.equals(tag)) {
                        inKey = true;
                        key = createKeyFromXml(res, currentRow, row, parser);
                        mKeys.add(key);
                        Bfx = key.getLength() + Bfx;
                        Log.i(TAG, "loadKeyboard: TAG_KEY Bfx :" + Bfx);
                        if (Bfx<100){
                            keyboardWidth = (int) (Bfx* KeyboardUtil.getViewWidth(context)/100);
                        }else keyboardWidth = KeyboardUtil.getViewWidth(context);
                        Log.i(TAG, "loadKeyboard: TAG_KEY keyboardWidth :" + keyboardWidth);
                        Log.i(TAG, "loadKeyboard: TAG_KEY keyboardHeight :" + KeyboardUtil.getViewHeight(context));
                    } else if (TAG_KEYBOARD.equals(tag)) {
                        parseKeyboardAttributes(res, parser);
                    }
                } else if (event == XmlResourceParser.END_TAG) {
                    if (inKey) {
                        Log.i("loadKeyboard", "loadKeyboard: inKey");
                        inKey = false;
                    } else if (inRow) {
                        inRow = false;
                        row++;
                        Log.i("loadKeyboard", "loadKeyboard: inRow" + row);
                    } else {
                        // TODO: error or extend?
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }
        keyboardHeight = y+ 50;
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
    protected Key createKeyFromXml(Resources res, KeyboardRow parent, int x, int y,XmlResourceParser parser) {
        return new Key(res, parent, x, y, parser);
    }
    /**
     * 解析到<Key>时传递到 Key
     * {@link Key}
     * @param row 行数
     */
    protected Key createKeyFromXml(Resources res, KeyboardRow parent, int row, XmlResourceParser parser) {
        return new Key(res, parent,row, parser);
    }
    /**
     * 解析<Keyboard></Keyboard>属性
     */
    private void parseKeyboardAttributes(Resources res, XmlResourceParser parser) {
    }

    public ArrayList<KeyboardRow> getRows() {
        return rows;
    }

    public ArrayList<Key> getmKeys() {
        return mKeys;
    }

    public int getKeyboardWidth() {
        return keyboardWidth;
    }

    public int getKeyboardHeight() {
        return keyboardHeight;
    }
}

