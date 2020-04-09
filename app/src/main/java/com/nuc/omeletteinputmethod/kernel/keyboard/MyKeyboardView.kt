package com.nuc.omeletteinputmethod.kernel.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

import com.nuc.omeletteinputmethod.R
import com.nuc.omeletteinputmethod.entityclass.CandidatesEntity
import com.nuc.omeletteinputmethod.entityclass.SinograFromDB
import com.nuc.omeletteinputmethod.kernel.OmeletteIME

import java.util.ArrayList
import java.util.Timer
import java.util.TimerTask


class MyKeyboardView : View {
    internal lateinit var canvas: Canvas
    internal lateinit var keyboardBuider: KeyboardBuider
    internal lateinit var myKeyboard: MyKeyboard
    //定义一个paint
    private var mPaint: Paint? = null
    internal var omeletteIME: OmeletteIME
    var nowPinYin: String? = ""
    internal var allCandidates: String? = null
    internal var candidatesEntityArrayList = ArrayList<CandidatesEntity>()

    internal val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0x11) {
                invalidate()
            } else if (msg.what == 0x01) {
                dealLongClickKeyEvent(clickKey!!)
            }
        }
    }

    private var rows = ArrayList<KeyboardRow>()
    private var mKeys = ArrayList<Key>()

    //计时器，计时点击时长
    internal lateinit var timer: Timer
    internal lateinit var timerTask: TimerTask

    internal var ifOnClick = true//判断是否为点击
    internal var keyClick = false
    internal var clickKey: Key? = null

    //记录上次点击的位置，用来进行移动的模糊处理
    internal var lastX = 0
    internal var lastY = 0

    private val LONGCLICKBEGIN = 1
    private val LONGCLICKEND = 0
    private var LongClickState = LONGCLICKEND


    constructor(context: Context) : super(context) {
        Log.i("loadKeyboard", "MyKeyboardView: 调用这里1")
        this.omeletteIME = context as OmeletteIME
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        Log.i("loadKeyboard", "MyKeyboardView: 调用这里11")
        this.omeletteIME = context as OmeletteIME
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        Log.i("loadKeyboard", "MyKeyboardView: 调用这里111")
        this.omeletteIME = context as OmeletteIME
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        Log.i("loadKeyboard", "MyKeyboardView: 调用这里1111")
        this.omeletteIME = context as OmeletteIME
    }

    fun SetMyKeyboardView(context: Context, xmlLayoutResId: Int) {
        this.omeletteIME = context as OmeletteIME
        keyboardBuider = KeyboardBuider(context, xmlLayoutResId)
        myKeyboard = keyboardBuider.keyboard
        rows = myKeyboard.rows
        mKeys = myKeyboard.getmKeys()
        //        measure(myKeyboard.getKeyboardWidth(),myKeyboard.getKeyboardHeight());
        //        setMeasuredDimension(myKeyboard.getKeyboardWidth(),myKeyboard.getKeyboardHeight());
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.i("loadKeyboard", "onMeasure: widthMeasureSpec =$widthMeasureSpec")
        Log.i("loadKeyboard", "onMeasure: heightMeasureSpec =$heightMeasureSpec")
        setMeasuredDimension(myKeyboard.keyboardWidth, myKeyboard.keyboardHeight + 60)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (KeyboardState.witchKeyboardNow) {
            KeyboardState.ARROWS_KEYBOARD -> Log.i("onTouchEvent", "onTouchEvent: ARROWS_KEYBOARD")
            KeyboardState.ENGLISH_26_KEY_KEYBOARD -> {
                Log.i("onTouchEvent", "onTouchEvent: ENGLISH_26_KEY_KEYBOARD")
                englishKeyboardEvent(event)
            }
        }
        return true
    }

    private fun englishKeyboardEvent(event: MotionEvent): Boolean {
        val X = event.x
        val Y = event.y
        //手指移动的模糊范围，手指移动超出该范围则取消事件处理
        val length = width / MOHUFANWEI
        val indexX = (Y / length).toInt()
        val indexY = (X / length).toInt()
        val keyX = X
        val keyY = Y
        if (event.action == MotionEvent.ACTION_DOWN && event.pointerCount == 1) {
            //长按计时器
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    //长按逻辑触发，isClick置为false，手指移开后，不触发点击事件
                    ifOnClick = false
                    // doLongPress(indexX, indexY);
                    LongClickState = LONGCLICKBEGIN
                    LongClickKey(keyX, keyY)
                    //omeletteIME.deleteText();
                }
            }
            ifOnClick = true
            drawClickKeyColor(keyX, keyY)
            Log.i("MyKeyboardView", "onTouchEvent: ACTION_DOWN")
            timer.schedule(timerTask, LONGPRESSTIME.toLong(), (1000 * 60 * 60 * 24).toLong())
        }

        if (event.action == MotionEvent.ACTION_UP && event.pointerCount == 1) {
            //没有触发长按逻辑，进行点击事件
            if (ifOnClick == true) {
                doClick(indexX, indexY)
                foundKey(keyX, keyY)
            }
            //取消计时
            timerTask.cancel()
            timer.cancel()
            Log.i("MyKeyboardView", "onTouchEvent: ACTION_UP ")
            LongClickState = LONGCLICKEND
            val message = Message.obtain()
            message.what = 0x11
            handler.sendMessage(message)
            //            handler.sendMessageDelayed(message, 50);


        }

        //出现移动，取消点击和长按事件
        if (event.action == MotionEvent.ACTION_MOVE) {
            //如果在一定范围内移动，不处理移动事件
            if (lastX == indexX && lastY == indexY) {
                return true
            }
            ifOnClick = false
            timerTask.cancel()
            timer.cancel()
        }
        //一旦触发事件，即改变上次触发事件的坐标
        lastY = indexY
        lastX = indexX
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.i("MyKeyboardView", "onDraw: ")
        this.canvas = canvas
        when (KeyboardState.witchKeyboardNow) {
            KeyboardState.ARROWS_KEYBOARD -> drawArrowsKeyboard(canvas)
            KeyboardState.ENGLISH_26_KEY_KEYBOARD -> drawKeyboard(canvas)
            else -> {
                drawKeyboard(canvas)
                KeyboardState.witchKeyboardNow = KeyboardState.ENGLISH_26_KEY_KEYBOARD
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun drawArrowsKeyboard(canvas: Canvas) {
        mPaint = Paint()
        canvas.drawColor(Color.parseColor("#ECEFF1"))
        //canvas.drawColor(R.color.keyboardBackround);
        mPaint!!.color = Color.WHITE
        val r = this.context.resources

        var bmp = BitmapFactory.decodeResource(r, R.drawable.arrows_up)
        var rect = Rect(0, 0, bmp.width, bmp.height)
        var rect2 = Rect(300, 50, 400, 150)
        canvas.drawBitmap(bmp, rect, rect2, mPaint)
        bmp = BitmapFactory.decodeResource(r, R.drawable.arrows_down)

        rect = Rect(0, 0, bmp.width, bmp.height)
        rect2 = Rect(300, 350, 400, 450)
        canvas.drawBitmap(bmp, rect, rect2, mPaint)
        bmp = BitmapFactory.decodeResource(r, R.drawable.arrows_right)

        rect = Rect(0, 0, bmp.width, bmp.height)
        rect2 = Rect(450, 200, 550, 300)
        canvas.drawBitmap(bmp, rect, rect2, mPaint)
        bmp = BitmapFactory.decodeResource(r, R.drawable.arrows_left)

        rect = Rect(0, 0, bmp.width, bmp.height)
        rect2 = Rect(150, 200, 250, 300)
        canvas.drawBitmap(bmp, rect, rect2, mPaint)
        bmp = BitmapFactory.decodeResource(r, R.drawable.arrows_ok)

        rect = Rect(0, 0, bmp.width, bmp.height)
        rect2 = Rect(300, 200, 400, 300)
        canvas.drawBitmap(bmp, rect, rect2, mPaint)
        bmp = BitmapFactory.decodeResource(r, R.drawable.arrows_left_go)

        rect = Rect(0, 0, bmp.width, bmp.height)
        rect2 = Rect(130, 420, 230, 520)
        canvas.drawBitmap(bmp, rect, rect2, mPaint)
        bmp = BitmapFactory.decodeResource(r, R.drawable.arrows_right_go)

        rect = Rect(0, 0, bmp.width, bmp.height)
        rect2 = Rect(470, 420, 570, 520)
        canvas.drawBitmap(bmp, rect, rect2, mPaint)
        bmp.recycle()//回收内存


        var rectf = RectF(600f, 50f, 760f, 150f)
        canvas.drawRoundRect(rectf, 20f, 20f, mPaint!!)
        val paint = Paint()
        paint.textSize = 50f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER//对其方式
        canvas.drawText("复制", 680f, 165f, paint)

        rectf = RectF(800f, 50f, 960f, 150f)
        canvas.drawRoundRect(rectf, 20f, 20f, mPaint!!)
        canvas.drawText("粘贴", 880f, 165f, paint)
    }

    /**
     * 绘制键盘以0.0为起点
     *
     * @param canvas
     */
    private fun drawKeyboard(canvas: Canvas) {
        val r = this.context.resources
        mPaint = Paint()
        // 绘制画布背景
        canvas.drawColor(Color.parseColor("#ECEFF1"))
        mPaint!!.color = Color.WHITE
        var row: KeyboardRow
        var drawX = 0f
        var drawY = 0f
        var beginRownumber = -1
        for (key in mKeys) {

            if (key.rowsNumber != beginRownumber) {
                if (beginRownumber == -1) {
                    row = rows[beginRownumber + 1]
                    drawY = drawY + row.rowVerticalGap
                } else {
                    row = rows[beginRownumber]
                    drawY = drawY + row.rowHeight.toFloat() + row.rowVerticalGap.toFloat()
                }
                beginRownumber = key.rowsNumber
                drawX = 0f
            }
            row = rows[beginRownumber]
            if (key.startingPosition != 0f) {
                drawX = key.startingPosition * myKeyboard.keyboardWidth / 100
            }

            val rect = RectF(drawX + key.gap / 2 * myKeyboard.keyboardWidth / 100, drawY,
                    drawX + key.length * myKeyboard.keyboardWidth / 100 +
                            key.gap / 2 * myKeyboard.keyboardWidth / 100,
                    drawY + row.rowHeight)
            val rectsave = RectF(drawX, drawY - row.rowVerticalGap / 2,
                    drawX + key.length * myKeyboard.keyboardWidth / 100 +
                            key.gap * myKeyboard.keyboardWidth / 100,
                    drawY + row.rowHeight.toFloat() + (row.rowVerticalGap / 2).toFloat())
            key.rect = rectsave
            canvas.drawRoundRect(rect, 20f, 20f, mPaint!!)
            //mPaint.setTypeface();
            val paint = Paint()
            paint.textSize = 50f
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.CENTER//对其方式
            if (keyClick && clickKey == key) {
                Log.i("MyKeyboardView", "drawKeyboard: 绘制点击键")
                mPaint = Paint()
                mPaint!!.color = Color.YELLOW
                canvas.drawRoundRect(clickKey!!.rect!!, 20f, 20f, mPaint!!)
                keyClick = false
                mPaint!!.color = Color.WHITE

                Log.i("onTouchEvent", "修改了" + clickKey!!.keySpec!!)
            }
            if (key.isIfimagekey) {

                val bmp = BitmapFactory.decodeResource(r, key.imgresource)
                val rect1 = Rect(0, 0, bmp.width, bmp.height)
                val rect2 = Rect((drawX + key.gap / 2 * myKeyboard.keyboardWidth / 100).toInt(), drawY.toInt() + row.rowHeight / 2, (drawX + key.length * myKeyboard.keyboardWidth / 200 +
                        key.gap / 2 * myKeyboard.keyboardWidth / 100).toInt(), (drawY + row.rowHeight).toInt())
                canvas.drawBitmap(bmp, rect1, rect2, mPaint)
            } else {
                canvas.drawText(key.keySpec!!, drawX + key.length * myKeyboard.keyboardWidth / 200 +
                        key.gap / 2 * myKeyboard.keyboardWidth / 100, drawY + row.rowHeight / 2, paint)
            }
            drawX = (drawX + key.length * myKeyboard.keyboardWidth / 100 + key.gap * myKeyboard.keyboardWidth / 100).toInt().toFloat()


        }

    }

    /**
     * 常规绘制  以(0,0)作为坐标原点参考点
     *
     * @param canvas
     */
    private fun drawNomal(canvas: Canvas) {
        mPaint = Paint()
        // 绘制画布背景
        canvas.drawColor(Color.GRAY)
        //设置画笔颜色
        mPaint!!.color = Color.BLUE
        //设置画笔为空心     如果将这里改为Style.STROKE  这个图中的实线圆柱体就变成了空心的圆柱体
        mPaint!!.style = Paint.Style.STROKE
        //绘制直线
        canvas.drawLine(50f, 50f, 450f, 50f, mPaint!!)
        //绘制矩形
        canvas.drawRect(100f, 100f, 200f, 300f, mPaint!!)
        //绘制矩形
        mPaint!!.style = Paint.Style.FILL
        canvas.drawRect(300f, 100f, 400f, 400f, mPaint!!)
        mPaint!!.color = Color.YELLOW
        val r = RectF(150f, 500f, 270f, 600f)
        // 画矩形
        canvas.drawRect(r, mPaint!!)
        // 画圆
        canvas.drawCircle(50f, 500f, 50f, mPaint!!)
        val oval = RectF(350f, 500f, 450f, 700f)
        // 画椭圆
        canvas.drawOval(oval, mPaint!!)
        val rect = RectF(100f, 700f, 170f, 800f)
        // 画圆角矩形
        canvas.drawRoundRect(rect, 30f, 20f, mPaint!!)
        //绘制圆弧 绘制弧形
        mPaint!!.style = Paint.Style.FILL
        mPaint!!.color = Color.RED
        val re1 = RectF(1000f, 50f, 1400f, 200f)
        canvas.drawArc(re1, 10f, 270f, false, mPaint!!)
        val re2 = RectF(1000f, 300f, 1400f, 500f)
        canvas.drawArc(re2, 10f, 270f, true, mPaint!!)
        //设置Path路径
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.color = Color.GREEN
        mPaint!!.strokeWidth = 3f
        val path = Path()
        path.moveTo(500f, 100f)
        path.lineTo(920f, 80f)
        path.lineTo(720f, 200f)
        path.lineTo(600f, 400f)
        path.close()
        mPaint!!.textSize = 46f
        canvas.drawPath(path, mPaint!!)
        canvas.drawTextOnPath("7qiuwoeruowoqjifasdkfjksjfiojio23ur8950", path, -20f, -20f, mPaint!!)
        //三角形
        path.moveTo(10f, 330f)
        path.lineTo(70f, 330f)
        path.lineTo(40f, 270f)
        path.close()
        canvas.drawPath(path, mPaint!!)
        //把开始的点和最后的点连接在一起，构成一个封闭梯形
        path.moveTo(10f, 410f)//绘画基点
        path.lineTo(70f, 410f)
        path.lineTo(55f, 350f)
        path.lineTo(25f, 350f)
        //如果是Style.FILL的话，不设置close,也没有区别，可是如果是STROKE模式， 如果不设置close,图形不封闭。当然，你也可以不设置close，再添加一条线，效果一样。
        path.close()
        canvas.drawPath(path, mPaint!!)
        //参数一为渐变起初点坐标x位置，参数二为y轴位置，参数三和四分辨对应渐变终点,其中参数new int[]{startColor, midleColor,endColor}是参与渐变效果的颜色集合，
        // 其中参数new float[]{0 , 0.5f, 1.0f}是定义每个颜色处于的渐变相对位置， 这个参数可以为null，如果为null表示所有的颜色按顺序均匀的分布
        // Shader.TileMode三种模式
        // REPEAT:沿着渐变方向循环重复
        // CLAMP:如果在预先定义的范围外画的话，就重复边界的颜色
        // MIRROR:与REPEAT一样都是循环重复，但这个会对称重复
        val mShader = LinearGradient(0f, 0f, 100f, 100f,
                intArrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW), null, Shader.TileMode.REPEAT)
        mPaint!!.shader = mShader// 用Shader中定义定义的颜色来话
        mPaint!!.style = Paint.Style.FILL
        val path1 = Path()
        path1.moveTo(170f, 410f)
        path1.lineTo(230f, 410f)
        path1.lineTo(215f, 350f)
        path1.lineTo(185f, 350f)
        path1.close()
        canvas.drawPath(path1, mPaint!!)
        canvas.save()
    }


    /**
     * 绘制方法练习
     *
     * @param canvas
     */
    private fun drawTest(canvas: Canvas) {
        mPaint = Paint()
        mPaint!!.color = Color.RED
        //平移测试
        canvas.translate(50f, 900f)
        canvas.drawRect(Rect(0, 0, 100, 100), mPaint!!)
        canvas.translate(50f, 50f)
        canvas.drawRect(Rect(0, 0, 100, 100), mPaint!!)
        //缩放测试
        canvas.translate(100f, -50f)
        canvas.drawRect(Rect(0, 0, 300, 300), mPaint!!)
        // 保存画布状态
        canvas.save()
        canvas.scale(0.5f, 0.5f)
        mPaint!!.color = Color.YELLOW
        canvas.drawRect(Rect(0, 0, 300, 300), mPaint!!)
        // 画布状态回滚
        canvas.restore()
        // 先将画布平移到矩形的中心
        canvas.translate(400f, -50f)
        canvas.drawRect(Rect(0, 0, 300, 300), mPaint!!)
        //旋转测试
        canvas.save()
        canvas.translate(350f, 50f)
        canvas.drawRect(Rect(0, 0, 200, 200), mPaint!!)
        mPaint!!.color = Color.RED
        canvas.rotate(45f, 200f, 200f)
        canvas.drawRect(Rect(0, 0, 200, 200), mPaint!!)
        canvas.restore()
        //画布错切 三角函数tan的值
        canvas.translate(350f, 300f)
        canvas.drawRect(Rect(0, 0, 400, 400), mPaint!!)
        // y 方向上倾斜45 度
        canvas.skew(0f, 1f)
        mPaint!!.color = -0x77ff0100
        canvas.drawRect(Rect(0, 0, 400, 400), mPaint!!)
    }

    private fun doLongPress(x: Int, y: Int) {
        Log.i("onTouchEvent", "长按了$x   $y")
    }

    private fun doClick(x: Int, y: Int) {
        Log.i("MyKeyboardView", "点击了$x   $y")
        //foundKey(x, y);
    }

    private fun LongClickKey(x: Float, y: Float): String? {
        var nowTime = System.currentTimeMillis()
        var waittime = 500
        while (LongClickState == LONGCLICKBEGIN) {
            if (System.currentTimeMillis() - nowTime >= waittime) {
                nowTime = System.currentTimeMillis()
                val message = Message.obtain()
                message.what = 0x01
                handler.sendMessage(message)
                if (waittime > 100) {
                    waittime = waittime - 200
                } else if (waittime > 50) {
                    waittime = waittime - 50
                }
            }
        }
        return clickKey!!.keySpec
    }

    private fun drawClickKeyColor(x: Float, y: Float) {
        for (key in mKeys) {
            if (x > key.rect!!.left && x < key.rect!!.right && y > key.rect!!.top && y < key.rect!!.bottom) {
                //Toast.makeText(omeletteIME, "您点击了" + key.getKeySpec(), Toast.LENGTH_SHORT).show();
                clickKey = key
                keyClick = true
                val message = Message.obtain()
                message.what = 0x11
                handler.sendMessage(message)
            }
        }
    }

    private fun foundKey(x: Float, y: Float): String? {
        //        for (Key key : mKeys) {
        //            if (x > key.getRect().left && x < key.getRect().right && y > key.getRect().top && y < key.getRect().bottom) {
        //                //Toast.makeText(omeletteIME, "您点击了" + key.getKeySpec(), Toast.LENGTH_SHORT).show();
        //                clickKey = key;
        //                keyClick = true;
        //                Message message = Message.obtain();
        //                message.what = 0x11;
        //                handler.sendMessage(message);
        //                dealKeyEvent(key);
        //                return key.getKeySpec();
        //            }
        //        }

        dealKeyEvent(clickKey!!)
        return clickKey!!.keySpec
    }

    fun clearAndHideInputView() {
        Log.i("clickAndInput", "clickAndInput: 现在拼音为空了")
        candidatesEntityArrayList.clear()
        omeletteIME.keyboardSwisher!!.showCandidatesView(candidatesEntityArrayList, nowPinYin!!)
        omeletteIME.keyboardSwisher!!.hideCandidatesView()
        nowPinYin = ""
    }

    fun clickAndInput(key: Key, altCode: Int) {
        //omeletteIME.commitText(key.getKeySpec());
        if (0 == altCode) {
            nowPinYin = nowPinYin!! + key.keySpec!!
        }
        if (nowPinYin === "" || nowPinYin == null || nowPinYin!!.length <= 0) {
            clearAndHideInputView()
            return
        }
        Log.i("dealKeyEvent", "dealKeyEvent: nowPinYin " + nowPinYin!!)
        if (omeletteIME.dbManage == null) Log.i("数据库是否为空", "dbManage == null")

        candidatesEntityArrayList.clear()


        if (nowPinYin!!.contains("'")) {
            for (sinograFromDB in omeletteIME.dbManage!!.getSomeSinogramByPinyin(nowPinYin!!, 0)!!) {
                Log.i("dealKeyEvent", "遍历返回文字 " + sinograFromDB.wenzi1!!)
                candidatesEntityArrayList.add(CandidatesEntity(sinograFromDB.id, sinograFromDB.wenzi1!!, sinograFromDB.allcishu, sinograFromDB.usercishu))
            }
        } else {
            for (sinograFromDB in omeletteIME.dbManage!!.getSinogramByPinyin(nowPinYin!!)!!) {
                Log.i("dealKeyEvent", "遍历返回文字 " + sinograFromDB.wenzi1!!)
                candidatesEntityArrayList.add(CandidatesEntity(sinograFromDB.id, sinograFromDB.wenzi1!!, sinograFromDB.allcishu, sinograFromDB.usercishu))
            }
        }

        omeletteIME.keyboardSwisher!!.showCandidatesView(removeRepetiton(candidatesEntityArrayList), nowPinYin!!)
    }

    private fun removeRepetiton(arrayList: ArrayList<CandidatesEntity>): ArrayList<CandidatesEntity> {
        val retlist = ArrayList<CandidatesEntity>()
        for (candidatesEntity in arrayList) {
            if (retlist.size == 0) {
                retlist.add(candidatesEntity)
            }
            for (i in retlist.indices) {

                if (retlist[i].candidates == candidatesEntity.candidates) {
                    continue
                } else if (i == retlist.size - 1) {
                    retlist.add(candidatesEntity)
                }
            }
        }
        return retlist
    }

    private fun dealLongClickKeyEvent(key: Key) {
        when (key.altCode) {
            0 -> {
            }
            -1 ->
                // 目标：删除最后一个输入的符号
                if (nowPinYin !== "" && nowPinYin != null && nowPinYin!!.length > 0) {
                    nowPinYin = ""
                    Log.i("MyKeyboardView", "dealKeyEvent: 现在拼音字符长度是 " + nowPinYin!!.length)
                    clickAndInput(key, -1)
                } else {
                    omeletteIME.deleteText()
                }
            -5//大小写切换
            -> {
            }
            -2//确认按键
            -> {
            }
            -3//符号按键
            -> {
            }
            3//空格按键
            -> {
            }
            else -> {
            }
        }//clickAndInput(key,-1);
    }

    private fun dealKeyEvent(key: Key) {
        when (key.altCode) {
            0 -> clickAndInput(key, 0)
            -1 -> {
                // 目标：删除最后一个输入的符号
                if (nowPinYin !== "" && nowPinYin != null && nowPinYin!!.length > 0) {
                    if (nowPinYin!!.length > 1) {
                        if (nowPinYin!!.substring(nowPinYin!!.length - 2, nowPinYin!!.length - 1) == "'") {
                            nowPinYin = nowPinYin!!.substring(0, nowPinYin!!.length - 2)
                        } else
                            nowPinYin = nowPinYin!!.substring(0, nowPinYin!!.length - 1)
                    } else
                        nowPinYin = nowPinYin!!.substring(0, nowPinYin!!.length - 1)
                    Log.i("MyKeyboardView", "dealKeyEvent: 现在拼音字符长度是 " + nowPinYin!!.length)
                } else {
                    omeletteIME.deleteText()
                }
                clickAndInput(key, -1)
            }
            -5//大小写切换
            -> {
            }
            -2//确认按键
            -> {
                omeletteIME.keyboardSwisher!!.enterInputPinYin(nowPinYin!!)
                clearAndHideInputView()
            }
            -3//符号按键
            -> {
            }
            3//空格按键
            -> omeletteIME.commitText(" ")
            else -> omeletteIME.commitText(key.keySpec!!)
        }
    }

    //用于清除当前存在的拼音
    fun clearNowPinYin() {
        nowPinYin = ""
    }

    companion object {
        private val LONGPRESSTIME = 300//长按超过0.3秒，触发长按事件

        //此处可以视为将View划分为10行10列的方格，在方格内移动看作没有移动。
        private val MOHUFANWEI = 10
    }
}
