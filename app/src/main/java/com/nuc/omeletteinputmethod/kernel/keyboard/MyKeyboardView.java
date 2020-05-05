package com.nuc.omeletteinputmethod.kernel.keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputContentInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.entityclass.CandidatesEntity;
import com.nuc.omeletteinputmethod.entityclass.SinograFromDB;
import com.nuc.omeletteinputmethod.inputC.InputC;
import com.nuc.omeletteinputmethod.kernel.OmeletteIME;

import java.io.Console;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyKeyboardView extends View {

    ArrowKeyboard arrowKeyboard;
    Pinyin26Keyboard pinyin26Keyboard;
    NumberKeyboard numberKeyboard;

    Canvas canvas;
    KeyboardBuider keyboardBuider;
    MyKeyboard myKeyboard;
    //定义一个paint
    private Paint mPaint;
    OmeletteIME omeletteIME;
    String nowPinYin = "";



    final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x11) {
                invalidate();
            }else if (msg.what == 0x01){
                //dealLongClickKeyEvent(clickKey);
            }
        }
    };

    private ArrayList<KeyboardRow> rows = new ArrayList<>();
    private ArrayList<Key> mKeys = new ArrayList<>();


    public MyKeyboardView(Context context) {
        super(context);
        Log.i("loadKeyboard", "MyKeyboardView: 调用这里1");
        this.omeletteIME = (OmeletteIME) context;
    }

    public MyKeyboardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.i("loadKeyboard", "MyKeyboardView: 调用这里11");
        this.omeletteIME = (OmeletteIME) context;
    }

    public MyKeyboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.i("loadKeyboard", "MyKeyboardView: 调用这里111");
        this.omeletteIME = (OmeletteIME) context;
    }

    public MyKeyboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Log.i("loadKeyboard", "MyKeyboardView: 调用这里1111");
        this.omeletteIME = (OmeletteIME) context;
    }

    public void SetMyKeyboardView(Context context, int xmlLayoutResId) {
        arrowKeyboard = new ArrowKeyboard((OmeletteIME) context);
        this.omeletteIME = (OmeletteIME) context;
        keyboardBuider = new KeyboardBuider((OmeletteIME) context, xmlLayoutResId);
        myKeyboard = keyboardBuider.getKeyboard();
        rows = myKeyboard.getRows();
        mKeys = myKeyboard.getmKeys();
        pinyin26Keyboard = new Pinyin26Keyboard((OmeletteIME) context,myKeyboard,rows,mKeys);
        numberKeyboard = new NumberKeyboard((OmeletteIME) context,myKeyboard,rows,mKeys);
//        measure(myKeyboard.getKeyboardWidth(),myKeyboard.getKeyboardHeight());
//        setMeasuredDimension(myKeyboard.getKeyboardWidth(),myKeyboard.getKeyboardHeight());
    }



    boolean ifOnClick = true;//判断是否为点击

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i("loadKeyboard", "onMeasure: widthMeasureSpec =" + widthMeasureSpec);
        Log.i("loadKeyboard", "onMeasure: heightMeasureSpec =" + heightMeasureSpec);
        setMeasuredDimension(myKeyboard.getKeyboardWidth(),myKeyboard.getKeyboardHeight()+60);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (KeyboardState.getInstance().getWitchKeyboardNow()) {
            case KeyboardState.ARROWS_KEYBOARD:
                Log.i("onTouchEvent", "onTouchEvent: ARROWS_KEYBOARD");
                arrowKeyboard.arrowsKeyboardEvent(event, MyKeyboardView.this);
                break;
            case KeyboardState.ENGLISH_26_KEY_KEYBOARD:
                Log.i("onTouchEvent", "onTouchEvent: ENGLISH_26_KEY_KEYBOARD");
                //numberKeyboardEvent(event);
                pinyin26Keyboard.pinyin26KeyboardEvent(event, MyKeyboardView.this);
                break;
            case KeyboardState.NUMBER_9_KEY_KEYBOARD:
                numberKeyboard.numberKeyboardEvent(event, MyKeyboardView.this);
                break;
        }
        return true;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.canvas = canvas;
        switch (KeyboardState.getInstance().getWitchKeyboardNow()) {
            case KeyboardState.ARROWS_KEYBOARD:
                Log.i("键盘界面", "onDraw: ARROWS_KEYBOARD");
                arrowKeyboard.drawArrowsKeyboard(canvas,mPaint);
                break;
            case KeyboardState.ENGLISH_26_KEY_KEYBOARD:
                Log.i("键盘界面", "onDraw: ENGLISH_26_KEY_KEYBOARD");
                pinyin26Keyboard.drawKeyboard(canvas,mPaint);
                break;
            case KeyboardState.NUMBER_9_KEY_KEYBOARD:
                Log.i("键盘界面", "onDraw: NUMBER_9_KEY_KEYBOARD");
                numberKeyboard.drawKeyboard(canvas,mPaint);
                break;
//                case KeyboardState.Number_9_KEY_KEYBOARD:
//                    drawNumberKeyboard(canvas);
//                    break;
            default:
                pinyin26Keyboard.drawKeyboard(canvas,mPaint);
                KeyboardState.getInstance().setWitchKeyboardNow(KeyboardState.ENGLISH_26_KEY_KEYBOARD);
                break;
        }
    }

    //用于清除当前存在的拼音
    public void clearNowPinYin(){
        nowPinYin = "";
    }
    public String getNowPinYin() {
        return nowPinYin;
    }
    public void setNowPinYin(String nowPinYin) {
        this.nowPinYin = nowPinYin;
    }

}
