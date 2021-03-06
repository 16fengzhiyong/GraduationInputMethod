package com.nuc.omeletteinputmethod.kernel.keyboard;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.nuc.omeletteinputmethod.kernel.OmeletteIME;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class NumberKeyboard {

    MyKeyboard myKeyboard;
    private ArrayList<KeyboardRow> rows ;
    OmeletteIME omeletteIME;
    private ArrayList<Key> mKeys ;
    //计时器，计时点击时长
    Timer timer;
    TimerTask timerTask;
    //此处可以视为将View划分为10行10列的方格，在方格内移动看作没有移动。
    private static final int MOHUFANWEI = 10;
    boolean ifOnClick = true;//判断是否为点击
    private static final int LONGPRESSTIME = 300;//长按超过0.3秒，触发长按事件
    private final int LONGCLICKBEGIN = 1;
    private final int LONGCLICKEND = 0;
    private int LongClickState = LONGCLICKEND;
    //记录上次点击的位置，用来进行移动的模糊处理
    int lastX = 0;
    int lastY = 0;
    boolean keyClick = false;
    Key clickKey = null;

    public NumberKeyboard(OmeletteIME omeletteIME, MyKeyboard myKeyboard, ArrayList<KeyboardRow> rows, ArrayList<Key> mKeys) {
        this.omeletteIME = omeletteIME;
        this.myKeyboard = myKeyboard;
        this.rows = rows;
        this.mKeys = mKeys;
    }

    /**
     * 绘制键盘以0.0为起点
     *
     * @param canvas
     */
    public void drawKeyboard(Canvas canvas, Paint mPaint) {
        Resources r = omeletteIME.getResources();
        mPaint = new Paint();
        // 绘制画布背景
        canvas.drawColor(Color.parseColor("#ECEFF1"));
        mPaint.setColor(Color.WHITE);
        KeyboardRow row;
        float drawX = 0;
        float drawY = 0;
        int beginRownumber = -1;
        for (Key key : mKeys) {
            if (key.getRowsNumber() != beginRownumber) {
                if (beginRownumber == -1) {
                    row = rows.get(beginRownumber + 1);
                    drawY = drawY + row.getRowVerticalGap();
                } else {
                    row = rows.get(beginRownumber);
                    drawY = drawY + row.getRowHeight() + row.getRowVerticalGap();
                }
                beginRownumber = key.getRowsNumber();
                drawX = 0;
            }
            row = rows.get(beginRownumber);
            if (key.getStartingPosition() != 0) {
                drawX = key.getStartingPosition() * myKeyboard.getKeyboardWidth() / 100;
            }

            RectF rect = new RectF(drawX + (key.getGap() / 2 * myKeyboard.getKeyboardWidth() / 100), drawY,
                    drawX + (key.getLength() * myKeyboard.getKeyboardWidth() / 100) +
                            (key.getGap() / 2 * myKeyboard.getKeyboardWidth() / 100),
                    drawY + row.getRowHeight());
            RectF rectsave = new RectF(drawX , drawY - row.getRowVerticalGap()/2,
                    drawX + (key.getLength() * myKeyboard.getKeyboardWidth() / 100) +
                            (key.getGap()* myKeyboard.getKeyboardWidth() / 100),
                    drawY + row.getRowHeight()+ row.getRowVerticalGap()/2);
            key.setRect(rectsave);
            canvas.drawRoundRect(rect, 20, 20, mPaint);
            //mPaint.setTypeface();
            Paint paint = new Paint();
            paint.setTextSize(50);
            paint.setColor(Color.BLACK);
            paint.setTextAlign(Paint.Align.CENTER);//对其方式
            if (keyClick && clickKey == key) {
                Log.i("MyKeyboardView", "drawKeyboard: 绘制点击键");
                mPaint = new Paint();
                mPaint.setColor(Color.YELLOW);
                canvas.drawRoundRect(clickKey.getRect(), 20, 20, mPaint);
                keyClick = false;
                mPaint.setColor(Color.WHITE);

                Log.i("onTouchEvent", "修改了" + clickKey.getKeySpec());
            }
            if (key.isIfimagekey()) {
                int width_1_4 = (int) (key.getGap() / 2 * myKeyboard.getKeyboardWidth() / 200+ (key.getLength() * myKeyboard.getKeyboardWidth() / 100 /2 /2));
                int height_1_4 = row.getRowHeight() / 2 / 2;
                Bitmap bmp = BitmapFactory.decodeResource(r, key.getImgresource());
                Rect rect1 = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
                Rect rect2 = new Rect((int) (drawX + width_1_4)
                        , (int) drawY + height_1_4
                        , (int) (drawX + (key.getLength() * myKeyboard.getKeyboardWidth() / 100 /2 ) +
                        (key.getGap() / 2 * myKeyboard.getKeyboardWidth() / 100)) +width_1_4
                        , (int) (drawY + row.getRowHeight() /2 +height_1_4));
                canvas.drawBitmap(bmp, rect1, rect2, mPaint);
            } else {
                canvas.drawText(key.getKeySpec(), drawX + (key.getLength() * myKeyboard.getKeyboardWidth() / 200) +
                        (key.getGap() / 2 * myKeyboard.getKeyboardWidth() / 100), drawY + row.getRowHeight() / 2, paint);
            }
            drawX = (int) (drawX + (key.getLength() * myKeyboard.getKeyboardWidth() / 100) + (key.getGap() * myKeyboard.getKeyboardWidth() / 100));


        }

    }



    public boolean numberKeyboardEvent(MotionEvent event,MyKeyboardView myKeyboardView){
        float X = event.getX();
        float Y = event.getY();
        //手指移动的模糊范围，手指移动超出该范围则取消事件处理
        int length = myKeyboardView.getWidth() / MOHUFANWEI;
        final int indexX = (int) (Y / length);
        final int indexY = (int) (X / length);
        final float keyX = X;
        final float keyY = Y;
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && event.getPointerCount() == 1) {
            //长按计时器
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    //长按逻辑触发，isClick置为false，手指移开后，不触发点击事件
                    ifOnClick = false;
                    // doLongPress(indexX, indexY);
                    LongClickState = LONGCLICKBEGIN;
                    //omeletteIME.deleteText();
                }
            };
            ifOnClick = true;
            drawClickKeyColor(keyX,keyY,myKeyboardView);
            Log.i("MyKeyboardView", "onTouchEvent: ACTION_DOWN");
            timer.schedule(timerTask, LONGPRESSTIME, 1000 * 60 * 60 * 24);
        }

        if (event.getAction() == MotionEvent.ACTION_UP
                && event.getPointerCount() == 1) {
            //没有触发长按逻辑，进行点击事件
            if (ifOnClick == true) {
                dealKeyEvent(clickKey);
            }
            //取消计时
            timerTask.cancel();
            timer.cancel();
            Log.i("MyKeyboardView", "onTouchEvent: ACTION_UP ");
            LongClickState = LONGCLICKEND;
            Message message = Message.obtain();
            message.what = 0x11;
            myKeyboardView.handler.sendMessage(message);
//            handler.sendMessageDelayed(message, 50);
        }

        //出现移动，取消点击和长按事件
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            //如果在一定范围内移动，不处理移动事件
            if (lastX == indexX && lastY == indexY) {
                return true;
            }
            ifOnClick = false;
            timerTask.cancel();
            timer.cancel();
        }
        //一旦触发事件，即改变上次触发事件的坐标
        lastY = indexY;
        lastX = indexX;
        return true;
    }
    private void drawClickKeyColor(float x, float y, MyKeyboardView myKeyboardView){
        for (Key key : mKeys) {
            if (x > key.getRect().left && x < key.getRect().right && y > key.getRect().top && y < key.getRect().bottom) {
                //Toast.makeText(omeletteIME, "您点击了" + key.getKeySpec(), Toast.LENGTH_SHORT).show();
                clickKey = key;
                keyClick = true;
                Message message = Message.obtain();
                message.what = 0x11;
                myKeyboardView.handler.sendMessage(message);
            }
        }
    }
    private void dealKeyEvent(Key key) {
        switch (key.getAltCode()) {
            case 0:
                omeletteIME.commitText(key.getKeySpec());
                break;
            case -1:
                omeletteIME.deleteText();
                break;
            case -5://大小写切换
                break;
            case -2://确认按键
                omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
                break;
            case 3://空格按键
                break;
            case 4:
                omeletteIME.getKeyboardSwisher().checkNumberKeyboard();
                //返回按键
                break;
            default:
                omeletteIME.commitText(key.getKeySpec());
                break;
        }
    }
}
