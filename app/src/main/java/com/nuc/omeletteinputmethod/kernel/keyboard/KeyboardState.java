package com.nuc.omeletteinputmethod.kernel.keyboard;

public class KeyboardState {
    public static final int SETTING_KEYBOARD = 0;
    public static final int PINYIN_26_KEY_KEYBOARD = 1;
    public static final int PINYIN_9_KEY_KEYBOARD = 2;
    public static final int ENGLISH_26_KEY_KEYBOARD = 3;
    public static final int ENGLISH_9_KEY_KEYBOARD = 4;
    public static final int ARROWS_KEYBOARD = 5;
    private int witchKeyboardNow = 1;
    private int witchKeyboardLast = 0;
    private static final KeyboardState instance = new KeyboardState();
    private KeyboardState(){

    }
    public int getWitchKeyboardNow() {
        return witchKeyboardNow;
    }

    public void setWitchKeyboardNow(int witchKeyboardNow) {
        this.witchKeyboardNow = witchKeyboardNow;
    }

    public int getWitchKeyboardLast() {
        return witchKeyboardLast;
    }

    public void setWitchKeyboardLast(int witchKeyboardLast) {
        this.witchKeyboardLast = witchKeyboardLast;
    }

    public static KeyboardState getInstance(){
        return instance;
    }
}
