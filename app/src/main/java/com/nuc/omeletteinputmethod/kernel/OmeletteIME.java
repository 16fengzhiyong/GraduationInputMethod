package com.nuc.omeletteinputmethod.kernel;

import android.inputmethodservice.InputMethodService;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.nuc.omeletteinputmethod.adapters.FloatShortInputAdapter;
import com.nuc.omeletteinputmethod.inputC.InputC;


import java.util.ArrayList;
import java.util.Map;

public class OmeletteIME extends InputMethodService {
    static {
        System.loadLibrary("native-lib");
    }

    public static boolean canShowWindow = false;
    /**
     * 屏幕宽度
     */
    private int mScreenWidth;
    /**
     * 屏幕高度
     */
    private int mScreenHeight;

    /**
     * 状态栏高度
     */
    private int mStatusBarHeight;

    private KeyboardSwisher keyboardSwisher;

    public static InputC inputC = new InputC();

    @Override
    public void onCreate() {
        super.onCreate();
        inputC.stringFromJNI();
        keyboardSwisher = new KeyboardSwisher(this);
        FloatShortInputAdapter.omeletteIME = this;
        // /storage/emulated/0
        //File file = new File(basepath+"/zhengti2.txt");

    }

    @Override
    public View onCreateInputView() {
        return keyboardSwisher.choseKeyboard();
    }

    @Override
    public View onCreateCandidatesView() {
        return keyboardSwisher.choseCandidatesView();
    }


    public void commitText(String data) {
        getCurrentInputConnection().commitText(data.replace("'", ""), 0); // 往输入框输出内容

        setCandidatesViewShown(false); // 隐藏 CandidatesView
    }

    public void deleteText() {
        getCurrentInputConnection().deleteSurroundingText(1, 0);
    }

    public void hideInputMethod() {
        hideWindow();
    }

    public KeyboardSwisher getKeyboardSwisher() {
        return keyboardSwisher;
    }



}
