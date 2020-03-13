package com.nuc.omeletteinputmethod.kernel.keyboard;

import android.content.Context;
import android.view.WindowManager;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.kernel.OmeletteIME;
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil;

import java.lang.reflect.Field;

public class KeyboardBuider {
    MyKeyboard keyboard;
    OmeletteIME omeletteIME;
    int xmlLayoutResId;
    public KeyboardBuider(OmeletteIME omeletteIME,int xmlLayoutResId){
        this.omeletteIME = omeletteIME;
        this.xmlLayoutResId = xmlLayoutResId;
        keyboard = new MyKeyboard(omeletteIME, xmlLayoutResId);
    }

    public MyKeyboard getKeyboard() {
        return keyboard;
    }
}
