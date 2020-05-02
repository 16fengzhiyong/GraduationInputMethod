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
    Canvas canvas;
    KeyboardBuider keyboardBuider;
    MyKeyboard myKeyboard;
    //定义一个paint
    private Paint mPaint;
    OmeletteIME omeletteIME;
    String nowPinYin = "";
    String allCandidates = null;


    ArrayList<CandidatesEntity> candidatesEntityArrayList = new ArrayList<>();
    ArrayList<Rect> arrows = new ArrayList<>();

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x11) {
                invalidate();
            }else if (msg.what == 0x01){
                dealLongClickKeyEvent(clickKey);
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
        this.omeletteIME = (OmeletteIME) context;
        keyboardBuider = new KeyboardBuider((OmeletteIME) context, xmlLayoutResId);
        myKeyboard = keyboardBuider.getKeyboard();
        rows = myKeyboard.getRows();
        mKeys = myKeyboard.getmKeys();
//        measure(myKeyboard.getKeyboardWidth(),myKeyboard.getKeyboardHeight());
//        setMeasuredDimension(myKeyboard.getKeyboardWidth(),myKeyboard.getKeyboardHeight());
    }

    //计时器，计时点击时长
    Timer timer;
    TimerTask timerTask;

    boolean ifOnClick = true;//判断是否为点击
    boolean keyClick = false;
    Key clickKey = null;
    private static final int LONGPRESSTIME = 300;//长按超过0.3秒，触发长按事件

    //记录上次点击的位置，用来进行移动的模糊处理
    int lastX = 0;
    int lastY = 0;

    //此处可以视为将View划分为10行10列的方格，在方格内移动看作没有移动。
    private static final int MOHUFANWEI = 10;

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
                arrowsKeyboardEvent(event);
                break;
            case KeyboardState.ENGLISH_26_KEY_KEYBOARD:
                Log.i("onTouchEvent", "onTouchEvent: ENGLISH_26_KEY_KEYBOARD");
                englishKeyboardEvent(event);
                break;
        }
        return true;
    }

    private boolean  englishKeyboardEvent(MotionEvent event) {
        float X = event.getX();
        float Y = event.getY();
        //手指移动的模糊范围，手指移动超出该范围则取消事件处理
        int length = getWidth() / MOHUFANWEI;
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
                    LongClickKey(keyX,keyY);
                    //omeletteIME.deleteText();
                }
            };
            ifOnClick = true;
            drawClickKeyColor(keyX,keyY);
            Log.i("MyKeyboardView", "onTouchEvent: ACTION_DOWN");
            timer.schedule(timerTask, LONGPRESSTIME, 1000 * 60 * 60 * 24);
        }

        if (event.getAction() == MotionEvent.ACTION_UP
                && event.getPointerCount() == 1) {
            //没有触发长按逻辑，进行点击事件
            if (ifOnClick == true) {
                doClick(indexX, indexY);
                foundKey(keyX, keyY);
            }
            //取消计时
            timerTask.cancel();
            timer.cancel();
            Log.i("MyKeyboardView", "onTouchEvent: ACTION_UP ");
            LongClickState = LONGCLICKEND;
            Message message = Message.obtain();
            message.what = 0x11;
            handler.sendMessage(message);
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

    private boolean arrowsKeyboardEvent(MotionEvent event){
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && event.getPointerCount() == 1) {
            ifOnClick = true;
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


    private void dealArrowsEvent(int flag){

        switch (flag){
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

        //  omelet
//        //复制：
//        inputConnection.performContextMenuAction(android.R.id.copy);
//        //剪切
//        inputConnection.performContextMenuAction(android.R.id.cut);
//        //全选
//        inputConnection.performContextMenuAction(android.R.id.selectAll);

//        //移动光标（上下左右）：
//        omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP);
//        omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
//        omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
//        omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN);
       // omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_D);

        // 要按住Shift键，再使用上面移动光标（上下左右）的代码，
        // 就可以实现选中状态时移动上下左右。按住Shift键只要发送按键事件。

    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i("MyKeyboardView", "onDraw: ");
        this.canvas = canvas;
        switch (KeyboardState.getInstance().getWitchKeyboardNow()) {
            case KeyboardState.ARROWS_KEYBOARD:
                drawArrowsKeyboard(canvas);
                break;
            case KeyboardState.ENGLISH_26_KEY_KEYBOARD:
                drawKeyboard(canvas);
                break;
            default:
                drawKeyboard(canvas);
                KeyboardState.getInstance().setWitchKeyboardNow(KeyboardState.ENGLISH_26_KEY_KEYBOARD);
                break;
        }
    }

    @SuppressLint("ResourceAsColor")
    private void drawArrowsKeyboard(Canvas canvas) {
        mPaint = new Paint();
        canvas.drawColor(Color.parseColor("#ECEFF1"));
        ;
        //canvas.drawColor(R.color.keyboardBackround);
        mPaint.setColor(Color.WHITE);
        Resources r = this.getContext().getResources();

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

    /**
     * 绘制键盘以0.0为起点
     *
     * @param canvas
     */
    private void drawKeyboard(Canvas canvas) {
        Resources r = this.getContext().getResources();
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

                Bitmap bmp = BitmapFactory.decodeResource(r, key.getImgresource());
                Rect rect1 = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
                Rect rect2 = new Rect((int) (drawX + (key.getGap() / 2 * myKeyboard.getKeyboardWidth() / 100))
                        , (int) drawY + row.getRowHeight() / 2
                        , (int) (drawX + (key.getLength() * myKeyboard.getKeyboardWidth() / 200) +
                        (key.getGap() / 2 * myKeyboard.getKeyboardWidth() / 100))
                        , (int) (drawY + row.getRowHeight()));
                canvas.drawBitmap(bmp, rect1, rect2, mPaint);
            } else {
                canvas.drawText(key.getKeySpec(), drawX + (key.getLength() * myKeyboard.getKeyboardWidth() / 200) +
                        (key.getGap() / 2 * myKeyboard.getKeyboardWidth() / 100), drawY + row.getRowHeight() / 2, paint);
            }
            drawX = (int) (drawX + (key.getLength() * myKeyboard.getKeyboardWidth() / 100) + (key.getGap() * myKeyboard.getKeyboardWidth() / 100));


        }

    }

    /**
     * 常规绘制  以(0,0)作为坐标原点参考点
     *
     * @param canvas
     */
    private void drawNomal(Canvas canvas) {
        mPaint = new Paint();
        // 绘制画布背景
        canvas.drawColor(Color.GRAY);
        //设置画笔颜色
        mPaint.setColor(Color.BLUE);
        //设置画笔为空心     如果将这里改为Style.STROKE  这个图中的实线圆柱体就变成了空心的圆柱体
        mPaint.setStyle(Paint.Style.STROKE);
        //绘制直线
        canvas.drawLine(50, 50, 450, 50, mPaint);
        //绘制矩形
        canvas.drawRect(100, 100, 200, 300, mPaint);
        //绘制矩形
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(300, 100, 400, 400, mPaint);
        mPaint.setColor(Color.YELLOW);
        RectF r = new RectF(150, 500, 270, 600);
        // 画矩形
        canvas.drawRect(r, mPaint);
        // 画圆
        canvas.drawCircle(50, 500, 50, mPaint);
        RectF oval = new RectF(350, 500, 450, 700);
        // 画椭圆
        canvas.drawOval(oval, mPaint);
        RectF rect = new RectF(100, 700, 170, 800);
        // 画圆角矩形
        canvas.drawRoundRect(rect, 30, 20, mPaint);
        //绘制圆弧 绘制弧形
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);
        RectF re1 = new RectF(1000, 50, 1400, 200);
        canvas.drawArc(re1, 10, 270, false, mPaint);
        RectF re2 = new RectF(1000, 300, 1400, 500);
        canvas.drawArc(re2, 10, 270, true, mPaint);
        //设置Path路径
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(3);
        Path path = new Path();
        path.moveTo(500, 100);
        path.lineTo(920, 80);
        path.lineTo(720, 200);
        path.lineTo(600, 400);
        path.close();
        mPaint.setTextSize(46);
        canvas.drawPath(path, mPaint);
        canvas.drawTextOnPath("7qiuwoeruowoqjifasdkfjksjfiojio23ur8950", path, -20, -20, mPaint);
        //三角形
        path.moveTo(10, 330);
        path.lineTo(70, 330);
        path.lineTo(40, 270);
        path.close();
        canvas.drawPath(path, mPaint);
        //把开始的点和最后的点连接在一起，构成一个封闭梯形
        path.moveTo(10, 410);//绘画基点
        path.lineTo(70, 410);
        path.lineTo(55, 350);
        path.lineTo(25, 350);
        //如果是Style.FILL的话，不设置close,也没有区别，可是如果是STROKE模式， 如果不设置close,图形不封闭。当然，你也可以不设置close，再添加一条线，效果一样。
        path.close();
        canvas.drawPath(path, mPaint);
        //参数一为渐变起初点坐标x位置，参数二为y轴位置，参数三和四分辨对应渐变终点,其中参数new int[]{startColor, midleColor,endColor}是参与渐变效果的颜色集合，
        // 其中参数new float[]{0 , 0.5f, 1.0f}是定义每个颜色处于的渐变相对位置， 这个参数可以为null，如果为null表示所有的颜色按顺序均匀的分布
        // Shader.TileMode三种模式
        // REPEAT:沿着渐变方向循环重复
        // CLAMP:如果在预先定义的范围外画的话，就重复边界的颜色
        // MIRROR:与REPEAT一样都是循环重复，但这个会对称重复
        Shader mShader = new LinearGradient(0, 0, 100, 100,
                new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW},
                null, Shader.TileMode.REPEAT);
        mPaint.setShader(mShader);// 用Shader中定义定义的颜色来话
        mPaint.setStyle(Paint.Style.FILL);
        Path path1 = new Path();
        path1.moveTo(170, 410);
        path1.lineTo(230, 410);
        path1.lineTo(215, 350);
        path1.lineTo(185, 350);
        path1.close();
        canvas.drawPath(path1, mPaint);
        canvas.save();
    }


    /**
     * 绘制方法练习
     *
     * @param canvas
     */
    private void drawTest(Canvas canvas) {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        //平移测试
        canvas.translate(50, 900);
        canvas.drawRect(new Rect(0, 0, 100, 100), mPaint);
        canvas.translate(50, 50);
        canvas.drawRect(new Rect(0, 0, 100, 100), mPaint);
        //缩放测试
        canvas.translate(100, -50);
        canvas.drawRect(new Rect(0, 0, 300, 300), mPaint);
        // 保存画布状态
        canvas.save();
        canvas.scale(0.5f, 0.5f);
        mPaint.setColor(Color.YELLOW);
        canvas.drawRect(new Rect(0, 0, 300, 300), mPaint);
        // 画布状态回滚
        canvas.restore();
        // 先将画布平移到矩形的中心
        canvas.translate(400, -50);
        canvas.drawRect(new Rect(0, 0, 300, 300), mPaint);
        //旋转测试
        canvas.save();
        canvas.translate(350, 50);
        canvas.drawRect(new Rect(0, 0, 200, 200), mPaint);
        mPaint.setColor(Color.RED);
        canvas.rotate(45, 200, 200);
        canvas.drawRect(new Rect(0, 0, 200, 200), mPaint);
        canvas.restore();
        //画布错切 三角函数tan的值
        canvas.translate(350, 300);
        canvas.drawRect(new Rect(0, 0, 400, 400), mPaint);
        // y 方向上倾斜45 度
        canvas.skew(0, 1);
        mPaint.setColor(0x8800ff00);
        canvas.drawRect(new Rect(0, 0, 400, 400), mPaint);
    }

    private void doLongPress(int x, int y) {
        Log.i("onTouchEvent", "长按了" + x + "   " + y);
    }

    private void doClick(int x, int y) {
        Log.i("MyKeyboardView", "点击了" + x + "   " + y);
        //foundKey(x, y);
    }

    private final int LONGCLICKBEGIN = 1;
    private final int LONGCLICKEND = 0;
    private int LongClickState = LONGCLICKEND;
    private String LongClickKey(float x, float y){
            long nowTime = System.currentTimeMillis();
            int waittime = 500;
            while (LongClickState == LONGCLICKBEGIN){
                if (System.currentTimeMillis()-nowTime>=waittime){
                    nowTime = System.currentTimeMillis();
                    Message message = Message.obtain();
                    message.what = 0x01;
                    handler.sendMessage(message);
                    if (waittime>100){
                        waittime = waittime -200;
                    }else if (waittime>50){
                        waittime = waittime -50;
                    }
                }
            }
            return clickKey.getKeySpec();
    }
    private void drawClickKeyColor(float x, float y){
        for (Key key : mKeys) {
            if (x > key.getRect().left && x < key.getRect().right && y > key.getRect().top && y < key.getRect().bottom) {
                //Toast.makeText(omeletteIME, "您点击了" + key.getKeySpec(), Toast.LENGTH_SHORT).show();
                clickKey = key;
                keyClick = true;
                Message message = Message.obtain();
                message.what = 0x11;
                handler.sendMessage(message);
            }
        }
    }
    private String foundKey(float x, float y) {

        dealKeyEvent(clickKey);
        return clickKey.getKeySpec();
    }
    public void clearAndHideInputView(){
        Log.i("clickAndInput", "clickAndInput: 现在拼音为空了");
        candidatesEntityArrayList.clear();
        omeletteIME.getKeyboardSwisher().showCandidatesView(candidatesEntityArrayList,nowPinYin);
        omeletteIME.getKeyboardSwisher().hideCandidatesView();
        nowPinYin = "";
    }
    String returnstr;
    public void clickAndInput(Key key,int altCode){
        //omeletteIME.commitText(key.getKeySpec());
        if (0 ==altCode){
            nowPinYin = nowPinYin + key.getKeySpec();
        }
        if (nowPinYin == ""||nowPinYin == null||nowPinYin.length()<=0){
            clearAndHideInputView();
            return;
        }
        Log.i("c++返回信息", "clickAndInput: 获取返回");
        if (nowPinYin.contains("'")){
            int one1 = nowPinYin.lastIndexOf("'");
            String Suffix1 = nowPinYin.substring((one1+1),nowPinYin.length());
            Log.i("c++返回信息", "clickAndInput: 传入信息"+Suffix1+"   "+countInnerStr(nowPinYin,"'"));
            returnstr = omeletteIME.inputC.getStringOfScendFromJNI(Suffix1,countInnerStr(nowPinYin,"'"));
        }else {
            returnstr = omeletteIME.inputC.getStringOfFristFromJNI(nowPinYin);
        }
        if (returnstr == null||returnstr == ""||returnstr.equals("")){
            Log.i("c++返回信息", "clickAndInput: returnstr == null ");
            if (nowPinYin.toCharArray()[0]=='v'||nowPinYin.toCharArray()[0]=='V'){
                nowPinYin.replace('v','u');
            }
            if (nowPinYin.length()>=2){
                nowPinYin = nowPinYin.substring(0,nowPinYin.length()-1)
                        +"'"+nowPinYin.substring(nowPinYin.length()-1);
            }
            int one2 = nowPinYin.lastIndexOf("'");
            String Suffix2 = nowPinYin.substring((one2+1),nowPinYin.length());
            Log.i("c++返回信息", "clickAndInput: 当单个返回为空 传入信息:"+Suffix2+"   "+countInnerStr(nowPinYin,"'"));

            returnstr = omeletteIME.inputC.getStringOfScendFromJNI(Suffix2 ,countInnerStr(nowPinYin,"'"));
        }
        Log.i("c++返回信息", "clickAndInput: returnstr:"+returnstr);
        String[] strArrays = returnstr.split(",");
        candidatesEntityArrayList.clear();
        int t = 0;
        for (String i:strArrays){
            candidatesEntityArrayList.add(new CandidatesEntity(t++,i));
            if (t>10){
                break;
            }
        }
        omeletteIME.getKeyboardSwisher().showCandidatesView(removeRepetiton(candidatesEntityArrayList),nowPinYin);
    }
    public static int countInnerStr(final String str, final String patternStr) {
        int count = 0;
        final Pattern r = Pattern.compile(patternStr);
        final Matcher m = r.matcher(str);
        while (m.find()) {
            count++;
        }
        return count;
    }
    private ArrayList<CandidatesEntity>  removeRepetiton(ArrayList<CandidatesEntity> arrayList){
        ArrayList<CandidatesEntity> retlist = new ArrayList<>();
        for (CandidatesEntity candidatesEntity:arrayList){
            if (retlist.size() == 0){
                retlist.add(candidatesEntity);
            }
            for (int i = 0;i<retlist.size();i++){

                if (retlist.get(i).getCandidates().equals(candidatesEntity.getCandidates())){
                    continue;
                }else if (i == retlist.size()-1){
                    retlist.add(candidatesEntity);
                }
            }
        }
        return retlist;
    }
    private void dealLongClickKeyEvent(Key key) {
        switch (key.getAltCode()) {
            case 0:
                break;
            case -1:
                // 目标：删除最后一个输入的符号
                if (nowPinYin != ""&&nowPinYin != null&&nowPinYin.length()>0){
                    nowPinYin = "";
                    Log.i("MyKeyboardView", "dealKeyEvent: 现在拼音字符长度是 "+nowPinYin.length());
                    clickAndInput(key,-1);
                }else {
                    omeletteIME.deleteText();
                }
                //clickAndInput(key,-1);
                break;
            case -5://大小写切换
                break;
            case -2://确认按键
                break;
            case -3://符号按键
                break;
            case 3://空格按键
                break;
            default:
                break;
        }
    }

    private void dealKeyEvent(Key key) {
        switch (key.getAltCode()) {
            case 0:
                clickAndInput(key,0);
                break;
            case -1:
                // 目标：删除最后一个输入的符号
                if (nowPinYin != ""&&nowPinYin != null&&nowPinYin.length()>0){
                    if (nowPinYin.length() >1){
                        if (nowPinYin.substring(nowPinYin.length()-2,nowPinYin.length()-1).equals("'")){
                            nowPinYin = nowPinYin.substring(0, nowPinYin.length() - 2);
                        }else nowPinYin = nowPinYin.substring(0, nowPinYin.length() - 1);
                    }else nowPinYin = nowPinYin.substring(0, nowPinYin.length() - 1);
                    Log.i("MyKeyboardView", "dealKeyEvent: 现在拼音字符长度是 "+nowPinYin.length());
                }else {
                    omeletteIME.deleteText();
                }
                clickAndInput(key,-1);
                break;
            case -5://大小写切换
                break;
            case -2://确认按键
                omeletteIME.getKeyboardSwisher().enterInputPinYin(nowPinYin);
                clearAndHideInputView();
                break;
            case -3://符号按键
                break;
            case 3://空格按键
                omeletteIME.commitText(" ");
                break;
            default:
                omeletteIME.commitText(key.getKeySpec());
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
    /**
     * 之后为箭头键盘处理时间
     */
    public void copythis(){
//        //  omelet
//        //复制：
//        inputConnection.performContextMenuAction(android.R.id.copy);
//        //剪切
//        inputConnection.performContextMenuAction(android.R.id.cut);
//        //全选
//        inputConnection.performContextMenuAction(android.R.id.selectAll);
//
//        //移动光标（上下左右）：
//        omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP);
//        omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
//        omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
//        omeletteIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN);
//
//        // 要按住Shift键，再使用上面移动光标（上下左右）的代码，
//        // 就可以实现选中状态时移动上下左右。按住Shift键只要发送按键事件。
//        long eventTime = SystemClock.uptimeMillis();
//        inputConnection.sendKeyEvent(new KeyEvent(eventTime, eventTime,
//                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0,
//                KeyCharacterMap.BUILT_IN_KEYBOARD, 0,
//                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
    }

}
