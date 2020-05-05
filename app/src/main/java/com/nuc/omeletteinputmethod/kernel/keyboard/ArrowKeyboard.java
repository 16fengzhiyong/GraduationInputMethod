package com.nuc.omeletteinputmethod.kernel.keyboard;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.kernel.OmeletteIME;

import java.util.ArrayList;

public class ArrowKeyboard {
    OmeletteIME omeletteIME;
    public ArrowKeyboard(OmeletteIME omeletteIME) {
        this.omeletteIME = omeletteIME;
    }
    ArrayList<Rect> arrows = new ArrayList<>();
    public boolean arrowsKeyboardEvent(MotionEvent event,MyKeyboardView keyboardView){
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && event.getPointerCount() == 1) {
            keyboardView.ifOnClick = true;
            for (int i = 0;i<arrows.size();i++) {
                Rect rect = arrows.get(i);
                if (x > rect.left && x < rect.right && y > rect.top && y < rect.bottom) {
                    //Toast.makeText(omeletteIME, "您点击了" + key.getKeySpec(), Toast.LENGTH_SHORT).show();
                    dealArrowsEvent(i);
                }
            }
        }

        return true;
    }
    public void dealArrowsEvent(int flag) {

        switch (flag) {
            case 0:
                omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP);
                break;
            case 1:
                omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN);
                break;
            case 2:
                omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
                break;
            case 3:
                omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
                break;
            case 4:

                //omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_SHIFT_LEFT|KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                Log.i("方向界面", "dealArrowsEvent: 点击ok");
                //omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_SHIFT_LEFT);
                long eventTime = SystemClock.uptimeMillis();
                omeletteIME.getCurrentInputConnection().sendKeyEvent(new KeyEvent(eventTime, eventTime,
                        KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0,
                        KeyCharacterMap.BUILT_IN_KEYBOARD, 0,
                        KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
                break;
            case 5:
                // omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_SHIFT_LEFT);

                break;
            case 6:
                // omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN_RIGHT);
                break;
            case 7:
                omeletteIME.getCurrentInputConnection().performContextMenuAction(android.R.id.copy);
                break;
            case 8:
                omeletteIME.getCurrentInputConnection().performContextMenuAction(android.R.id.paste);
                break;
        }
    }
    @SuppressLint("ResourceAsColor")
    public void drawArrowsKeyboard(Canvas canvas,Paint mPaint) {
        mPaint = new Paint();
        canvas.drawColor(Color.parseColor("#ECEFF1"));
        ;
        //canvas.drawColor(R.color.keyboardBackround);
        mPaint.setColor(Color.WHITE);
        Resources r = omeletteIME.getResources();

        Bitmap bmp = BitmapFactory.decodeResource(r, R.drawable.arrows_up);
        Rect rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
        Rect rect2 = new Rect(300, 50, 400, 150);
        canvas.drawBitmap(bmp, rect, rect2, mPaint);
        arrows.add(new Rect(300, 50, 400, 150));//上


        bmp = BitmapFactory.decodeResource(r, R.drawable.arrows_down);
        rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
        rect2 = new Rect(300, 350, 400, 450);
        canvas.drawBitmap(bmp, rect, rect2, mPaint);
        arrows.add(new Rect(300, 350, 400, 450));//下

        bmp = BitmapFactory.decodeResource(r, R.drawable.arrows_left);
        rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
        rect2 = new Rect(150, 200, 250, 300);
        canvas.drawBitmap(bmp, rect, rect2, mPaint);
        arrows.add(new Rect(150, 200, 250, 300));//左

        bmp = BitmapFactory.decodeResource(r, R.drawable.arrows_right);
        rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
        rect2 = new Rect(450, 200, 550, 300);
        canvas.drawBitmap(bmp, rect, rect2, mPaint);
        arrows.add(new Rect(450, 200, 550, 300));//右




        bmp = BitmapFactory.decodeResource(r, R.drawable.arrows_ok);
        rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
        rect2 = new Rect(300, 200, 400, 300);
        canvas.drawBitmap(bmp, rect, rect2, mPaint);
        arrows.add( new Rect(300, 200, 400, 300));//选中


        bmp = BitmapFactory.decodeResource(r, R.drawable.arrows_left_go);
        rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
        rect2 = new Rect(130, 420, 230, 520);
        canvas.drawBitmap(bmp, rect, rect2, mPaint);
        arrows.add(new Rect(130, 420, 230, 520));//最左

        bmp = BitmapFactory.decodeResource(r, R.drawable.arrows_right_go);
        rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
        rect2 = new Rect(470, 420, 570, 520);
        canvas.drawBitmap(bmp, rect, rect2, mPaint);
        arrows.add(new Rect(470, 420, 570, 520));//最右
        bmp.recycle();//回收内存


        RectF rectf = new RectF(600, 50, 760, 150);
        canvas.drawRoundRect(rectf, 20, 20, mPaint);
        Paint paint = new Paint();
        paint.setTextSize(50);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);//对其方式
        canvas.drawText("复制", 680, 165, paint);
        arrows.add(new Rect(600, 50, 760, 150));//复制


        rectf = new RectF(800, 50, 960, 150);
        canvas.drawRoundRect(rectf, 20, 20, mPaint);
        canvas.drawText("粘贴", 880, 165, paint);
        arrows.add(new Rect(800, 50, 960, 150));//粘贴


    }

}
