package com.nuc.omeletteinputmethod.kernel.keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.nuc.omeletteinputmethod.kernel.OmeletteIME;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MyKeyboardView extends View {
    KeyboardBuider keyboardBuider ;
    MyKeyboard myKeyboard;
    //定义一个paint
    private Paint mPaint;
    Context context;
    private ArrayList<KeyboardRow> rows = new ArrayList<>();
    private ArrayList<Key> mKeys = new ArrayList<>();



    public MyKeyboardView(Context context) {
        super(context);
        Log.i("loadKeyboard", "MyKeyboardView: 调用这里1");
        this.context = context;
    }

    public MyKeyboardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.i("loadKeyboard", "MyKeyboardView: 调用这里11");
        this.context = context;
    }

    public MyKeyboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.i("loadKeyboard", "MyKeyboardView: 调用这里111");
        this.context = context;
    }

    public MyKeyboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Log.i("loadKeyboard", "MyKeyboardView: 调用这里1111");
        this.context = context;
    }
    public void SetMyKeyboardView(Context context,int xmlLayoutResId) {
        this.context = context;
        keyboardBuider = new KeyboardBuider((OmeletteIME) context,xmlLayoutResId);
        myKeyboard = keyboardBuider.getKeyboard();
        rows = myKeyboard.getRows();
        mKeys = myKeyboard.getmKeys();
//        measure(myKeyboard.getKeyboardWidth(),myKeyboard.getKeyboardHeight());
//        setMeasuredDimension(myKeyboard.getKeyboardWidth(),myKeyboard.getKeyboardHeight());
    }
    //计时器，计时点击时长
    Timer timer;
    TimerTask timerTask;

    boolean isCick=true;//判断是否进行点击
    private static final int LONGPRESSTIME = 300;//长按超过0.3秒，触发长按事件

    //记录上次点击的位置，用来进行移动的模糊处理
    int lastX=0;
    int lastY=0;

    //此处可以视为将View划分为10行10列的方格，在方格内移动看作没有移动。
    private static final int MOHUFANWEI=10;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        Log.i("loadKeyboard", "onMeasure: widthMeasureSpec ="+widthMeasureSpec);
        Log.i("loadKeyboard", "onMeasure: heightMeasureSpec ="+heightMeasureSpec);
        //setMeasuredDimension(myKeyboard.getKeyboardWidth(),myKeyboard.getKeyboardHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float X = event.getX();
        float Y = event.getY();
        //手指移动的模糊范围，手指移动超出该范围则取消事件处理
        int length=getWidth()/MOHUFANWEI;
        final int indexX=(int)(Y/length);
        final int indexY=(int)(X/length);
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && event.getPointerCount() == 1) {
            //长按计时器
            timer=new Timer();
            timerTask=new TimerTask() {
                @Override
                public void run() {
                    //长按逻辑触发，isClick置为false，手指移开后，不触发点击事件
                    isCick=false;
                    doLongPress(indexX,indexY);
                }
            };
            isCick=true;
            timer.schedule(timerTask,LONGPRESSTIME,1000*60*60*24);
        }

        if(event.getAction() == MotionEvent.ACTION_UP
                && event.getPointerCount() == 1)
        {
            //没有触发长按逻辑，进行点击事件
            if(isCick==true)
            {
                doClick(indexX,indexY);
            }
            //取消计时
            timerTask.cancel();
            timer.cancel();
        }

        //出现移动，取消点击和长按事件
        if(event.getAction() == MotionEvent.ACTION_MOVE)
        {
            //如果在一定范围内移动，不处理移动事件
            if(lastX==indexX&&lastY==indexY)
            {
                return true;
            }
            isCick=false;
            timerTask.cancel();
            timer.cancel();
        }
        //一旦触发事件，即改变上次触发事件的坐标
        lastY=indexY;
        lastX=indexX;
        return true;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawKeyboard(canvas);
//        drawNomal(canvas);
//        drawTest(canvas);
//        mPaint =new Paint();
//        //设置Path路径
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(Color.GREEN);
//        mPaint.setStrokeWidth(3);
//        Path path = new Path();
//        path.moveTo(100, 100);
//        path.lineTo(1000, 100);
//        path.lineTo(1000, 600);
//        path.lineTo(100, 600);
//        path.close();
//        mPaint.setTextSize(46);
//        canvas.drawPath(path, mPaint);
//        canvas.drawTextOnPath("这里是键盘绘制区", path, 260, 250, mPaint);
//        path.close();
//        canvas.drawPath(path, mPaint);
//         绘制画布背景
//        canvas.drawColor(Color.GRAY);
//        mPaint.setColor(Color.WHITE);
//        RectF rect = new RectF(100, 700, 200, 770);
//        // 画圆角矩形
//        canvas.drawRoundRect(rect, 20, 20, mPaint);
    }

    /**
     * 绘制键盘以0.0为起点
     * @param canvas
     */
    private void drawKeyboard(Canvas canvas){
        mPaint =new Paint();
        // 绘制画布背景
        canvas.drawColor(Color.GRAY);
        mPaint.setColor(Color.WHITE);
        KeyboardRow row;
        int drawX = 0;
        int drawY = 0;
        int beginRownumber = 0;
        for (Key key : mKeys){

            if (key.getRowsNumber()!=beginRownumber){
                beginRownumber = key.getRowsNumber();
                row = rows.get(beginRownumber);
                drawY = drawY+row.getRowHeight();
                drawX = 0;
            }
            row = rows.get(beginRownumber);
            RectF rect = new RectF(drawX+(key.getGap()/2*myKeyboard.getKeyboardWidth()/100), drawY, drawX+(key.getLength()*myKeyboard.getKeyboardWidth()/100)+
                    (key.getGap()/2*myKeyboard.getKeyboardWidth()/100), drawY+row.getRowHeight());


            canvas.drawRoundRect(rect, 20, 20, mPaint);
            //mPaint.setTypeface();
            Paint paint = new Paint();
            paint.setTextSize(50);
            paint.setColor(Color.BLACK);
            paint.setTextAlign(Paint.Align.CENTER);//对其方式
            canvas.drawText(key.getKeySpec(),drawX+(key.getLength()*myKeyboard.getKeyboardWidth()/200)+
                    (key.getGap()/2*myKeyboard.getKeyboardWidth()/100),drawY+row.getRowHeight()/2,paint);
            drawX = (int) (drawX+(key.getLength()*myKeyboard.getKeyboardWidth()/100)+(key.getGap()*myKeyboard.getKeyboardWidth()/100));
        }
    }
    /**
     * 常规绘制  以(0,0)作为坐标原点参考点
     * @param canvas
     */
    private void drawNomal(Canvas canvas){
        mPaint =new Paint();
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
     * @param canvas
     */
    private void drawTest(Canvas canvas){
        mPaint =new Paint();
        mPaint.setColor(Color.RED);
        //平移测试
        canvas.translate(50, 900);
        canvas.drawRect(new Rect(0, 0, 100, 100), mPaint);
        canvas.translate(50, 50);
        canvas.drawRect(new Rect(0, 0, 100, 100), mPaint);
        //缩放测试
        canvas.translate(100,-50);
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
        canvas.rotate(45,200,200);
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

    private void doLongPress(int x,int y)
    {
        Log.i("onTouchEvent","长按了"+x+"   "+y);
    }

    private void doClick(int x,int y)
    {
        Log.i("onTouchEvent","点击了"+x+"   "+y);
    }
}
