package com.nuc.omeletteinputmethod.kernel.keyboard;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;

public class KeyboardRow {
    private int rowHeight;
    public KeyboardRow(Resources res, MyKeyboard parent , XmlResourceParser parser){
        rowHeight = 80;
    }

    public int getRowHeight() {
        return rowHeight;
    }
}
