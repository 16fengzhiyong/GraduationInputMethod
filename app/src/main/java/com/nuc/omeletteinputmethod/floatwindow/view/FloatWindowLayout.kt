package com.nuc.omeletteinputmethod.floatwindow.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout

import com.nuc.omeletteinputmethod.R
import com.nuc.omeletteinputmethod.floatwindow.FloatingImageDisplayService

class FloatWindowLayout : ViewGroup {

    private val mRadius = 150// 中心菜单圆点到子菜单中心的距离
    private var mExpanded = false
    private val mChildPadding = 5

    private var centerX = 0
    private var centerY = 0
    private var position = PathMenu.LEFT_CENTER
    private var nowState: Int = 0
    private var mFromDegrees = DEFAULT_FROM_DEGREES
    private var mToDegrees = DEFAULT_TO_DEGREES

    private val mChildSize = 0

    internal lateinit var floatingImageDisplayService: FloatingImageDisplayService


    private val radiusAndPadding: Int
        get() = mRadius / 2 + mChildPadding

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        computeCenterXY(position)
        //当子菜单要收缩时radius=0，在ViewGroup坐标中心
        //        final int radius = mExpanded ? mRadius : 0;

        val childCount = childCount


        var child: View
        var childWidth: Int//item的宽
        var childHeight: Int//item的高
        var corner: Double//旋转角度

        for (i in 0 until childCount) {
            child = getChildAt(i)

            if (child.visibility == View.GONE) {
                continue
            }
            corner = (180 / childCount * i).toDouble()

            childWidth = child.measuredWidth / 4
            childHeight = child.measuredHeight / 4
            Log.wtf("分配的大小是什么", "onLayout: childWidth childWidth" + childWidth + "childHeight " + childHeight)
            Log.wtf("分配的大小是什么", "onLayout: childWidth childid" + child.id +
                    " R.id.bar_image_1" + R.id.bar_image_1 + " R.id.bar_image_1" + R.id.bar_image_2)

            var thisRadius = 0
            if (child.id == R.id.bar_image_center) {
                thisRadius = 0
                childWidth = child.measuredWidth / 4
                childHeight = child.measuredHeight / 4
            } else
                thisRadius = mRadius

            var cX = 0
            var cY = 0
            if (position == PathMenu.LEFT_CENTER) {
                cX = (centerX - thisRadius * Math.cos(Math.toRadians(corner + 120))).toInt()
                cY = (centerY - thisRadius * Math.sin(Math.toRadians(corner + 120))).toInt()
            }
            Log.e("分配的大小是什么", "onLayout: childWidth centerX" + centerX + "centerY " + centerY + "mRadius " + mRadius)

            Log.e("分配的大小是什么", "onLayout: childWidth cX" + cX + "cY " + cY + "mRadius " + mRadius)
            Log.d("分配的大小是什么", "onLayout: old childWidth" + (cX - childWidth / 2).toString() + "childHeight" + (cY - childHeight / 2).toString() + "childWidth"
                    + (cX + childWidth / 2).toString() + "childHeight" + (cY + childHeight / 2).toString())
            child.layout(cX - childWidth / 2, cY - childHeight / 2, cX + childWidth / 2, cY + childHeight / 2)
        }
    }

    /**
     * 切换中心按钮的展开缩小
     */
    fun switchState(showAnimation: Boolean, position: Int, floatingImageDisplayService: FloatingImageDisplayService, nowState: Int): Boolean {
        this.floatingImageDisplayService = floatingImageDisplayService
        //        this.position = position;
        this.nowState = nowState
        if (showAnimation) {
            val childCount = childCount
            for (i in 0 until childCount) {
                bindChildAnimation(getChildAt(i), i, 300)
            }
        }

        mExpanded = !mExpanded

        if (!showAnimation) {
            requestLayout()
        }

        invalidate()
        return mExpanded
    }

    /**
     * 绑定子菜单项动画
     */
    private fun bindChildAnimation(child: View, index: Int,
                                   duration: Long) {

        val expanded = mExpanded
        //        final int centerX = getWidth() / 2 - mRadius;  //ViewGroup的中心X坐标
        //        final int centerY = getHeight() / 2;

        computeCenterXY(position)
        val radius = if (expanded) 0 else mRadius

        val childCount = childCount
        val perDegrees = if (Math.abs(mToDegrees - mFromDegrees) == 360f) (mToDegrees - mFromDegrees) / childCount else (mToDegrees - mFromDegrees) / (childCount - 1)
        val frame = computeChildFrame(centerX, centerY, radius, mFromDegrees + index * perDegrees, 0)


        var toXDelta = frame.left - child.left//展开或收缩动画,child沿X轴位移距离
        var toYDelta = frame.top - child.top//展开或收缩动画,child沿Y轴位移距离

        toXDelta = centerX - child.left - child.width / 2
        toYDelta = centerY - child.top - child.height / 2


        Log.d("arcLayout", "toX:$toXDelta toY:$toYDelta")
        val interpolator = if (mExpanded)
            AccelerateInterpolator()
        else
            OvershootInterpolator(1.5f)
        val startOffset = computeStartOffset(childCount, mExpanded,
                index, 0.1f, duration, interpolator)
        //TODO toXDelta toYDelta 设置为0 为什么可以
        //mExpanded为true，已经展开，收缩动画；为false,展开动画


        Log.d("onLayout", "bindChildAnimation: toXDelta " + toXDelta + "toYDelta " + toYDelta + "mExpanded" + mExpanded)
        Log.i("onLayout", "bindChildAnimation: child.getLeft() " + child.left + "child.getTop() " + child.top + "mExpanded" + mExpanded)
        Log.v("onLayout", "bindChildAnimation: child.getWidth() " + child.width + "child.getHeight() " + child.height + "mExpanded" + mExpanded)


        val animation = if (mExpanded)
            createShrinkAnimation(child, 0f, toXDelta.toFloat(), 0f,
                    toYDelta.toFloat(), startOffset, duration, interpolator)
        else
            createExpandAnimation(child, 0f, toXDelta.toFloat(), 0f, toYDelta.toFloat(), startOffset,
                    duration, interpolator)

        val isLast = getTransformedIndex(expanded, childCount, index) == childCount - 1
        animation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationRepeat(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                if (isLast) {
                    postDelayed({ onAllAnimationsEnd() }, 0)
                }
                if (!mExpanded && child.id != R.id.bar_image_center) {
                    child.visibility = View.GONE
                }
                if (isLast && !mExpanded) {
                    when (nowState) {
                        0 -> floatingImageDisplayService.sendMessageToHandler(2)
                        1 -> floatingImageDisplayService.sendMessageToHandler(3)
                    }

                }

            }
        })

        child.animation = animation
    }

    /**
     * 结束所有动画
     */
    private fun onAllAnimationsEnd() {
        val childCount = childCount
        for (i in 0 until childCount) {
            getChildAt(i).clearAnimation()
        }

        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)


        //拥有的view的数量
        val childCount = childCount//item的数量

        //可用宽高
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        val ps = paddingStart
        val pe = paddingEnd
        val pt = paddingTop
        val pb = paddingBottom

        //设置大小
        setMeasuredDimension(widthSize, heightSize)

        //子view最高多少，最宽多少
        var childMaxWidth = 0
        var childMaxHeight = 0
        var child: View
        for (i in 0 until childCount) {
            child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            child.measure(View.MeasureSpec.makeMeasureSpec(widthSize - ps - pe, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(heightSize - pt - pb, View.MeasureSpec.UNSPECIFIED))

            childMaxWidth = Math.max(childMaxWidth, child.measuredWidth)
            childMaxHeight = Math.max(childMaxHeight, child.measuredHeight)
        }
    }


    /**
     * 从什么地方展开 用于屏幕各个位置
     * @param position 展开位置
     */
    fun computeCenterXY(position: Int) {
        when (position) {
            PathMenu.LEFT_TOP//左上
            -> {
                centerX = width / 2 - radiusAndPadding
                centerY = height / 2 - radiusAndPadding
            }
            PathMenu.LEFT_CENTER//左中
            -> {
                centerX = width / 2 - radiusAndPadding
                centerY = height / 2
            }
            PathMenu.LEFT_BOTTOM//左下
            -> {
                centerX = width / 2 - radiusAndPadding
                centerY = height / 2 + radiusAndPadding
            }
            PathMenu.CENTER_TOP//上中
            -> {
                centerX = width / 2
                centerY = height / 2 - radiusAndPadding
            }
            PathMenu.CENTER_BOTTOM//下中
            -> {
                centerX = width / 2
                centerY = height / 2 + radiusAndPadding
            }
            PathMenu.RIGHT_TOP//右上
            -> {
                centerX = width / 2 + radiusAndPadding
                centerY = height / 2 - radiusAndPadding
            }
            PathMenu.RIGHT_CENTER//右中
            -> {
                centerX = width / 2 + radiusAndPadding
                centerY = height / 2
            }
            PathMenu.RIGHT_BOTTOM//右下
            -> {
                centerX = width / 2 + radiusAndPadding
                centerY = height / 2 + radiusAndPadding
            }

            PathMenu.CENTER -> {
                centerX = width / 2
                centerY = height / 2
            }
        }
    }


    /**
     * 设定弧度
     */
    fun setArc(fromDegrees: Float, toDegrees: Float, position: Int) {
        this.position = position
        if (mFromDegrees == fromDegrees && mToDegrees == toDegrees) {
            return
        }

        mFromDegrees = fromDegrees
        mToDegrees = toDegrees
        computeCenterXY(position)
        requestLayout()
    }

    companion object {

        val DEFAULT_FROM_DEGREES = 270.0f
        val DEFAULT_TO_DEGREES = 360.0f


        /**
         * 计算动画开始时的偏移量
         */
        private fun computeStartOffset(childCount: Int,
                                       expanded: Boolean, index: Int, delayPercent: Float,
                                       duration: Long, interpolator: Interpolator): Long {
            val delay = delayPercent * duration
            val viewDelay = (getTransformedIndex(expanded,
                    childCount, index) * delay).toLong()
            val totalDelay = delay * childCount

            var normalizedDelay = viewDelay / totalDelay
            normalizedDelay = interpolator.getInterpolation(normalizedDelay)

            return (normalizedDelay * totalDelay).toLong()
        }

        /**
         * 变换时的子菜单项索引
         */
        private fun getTransformedIndex(expanded: Boolean,
                                        count: Int, index: Int): Int {
            return if (expanded) {
                count - 1 - index
            } else index

        }

        /**
         * 展开动画
         */
        private fun createExpandAnimation(child: View, fromXDelta: Float,
                                          toXDelta: Float, fromYDelta: Float, toYDelta: Float, startOffset: Long,
                                          duration: Long, interpolator: Interpolator): Animation {
            child.visibility = View.VISIBLE
            val animation = RotateAndTranslateAnimation(toXDelta, 0f, toYDelta, 0f,
                    0f, 720f)
            animation.startOffset = startOffset
            animation.duration = duration
            animation.interpolator = interpolator
            animation.fillAfter = true

            return animation
        }

        /**
         * 收缩动画
         */
        private fun createShrinkAnimation(child: View, fromXDelta: Float,
                                          toXDelta: Float, fromYDelta: Float, toYDelta: Float, startOffset: Long,
                                          duration: Long, interpolator: Interpolator): Animation {
            val animationSet = AnimationSet(false)
            animationSet.fillAfter = true
            //收缩过程中，child 逆时针自旋转360度
            val preDuration = duration / 2
            val rotateAnimation = RotateAnimation(0f, 360f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f)
            rotateAnimation.startOffset = startOffset
            rotateAnimation.duration = preDuration
            rotateAnimation.interpolator = LinearInterpolator()
            rotateAnimation.fillAfter = true

            animationSet.addAnimation(rotateAnimation)
            //收缩过程中位移，并逆时针旋转360度
            val translateAnimation = RotateAndTranslateAnimation(0f,
                    toXDelta, 0f, toYDelta, 360f, 720f)
            translateAnimation.startOffset = startOffset + preDuration
            translateAnimation.duration = duration - preDuration
            translateAnimation.interpolator = interpolator
            translateAnimation.fillAfter = true

            animationSet.addAnimation(translateAnimation)
            animationSet.fillAfter = true
            return animationSet
        }

        /**
         * 计算子菜单项的范围
         */
        private fun computeChildFrame(centerX: Int, centerY: Int,
                                      radius: Int, degrees: Float, size: Int): Rect {
            //子菜单项中心点
            val childCenterX = centerX + radius * Math.cos(Math.toRadians(degrees.toDouble()))
            val childCenterY = centerY + radius * Math.sin(Math.toRadians(degrees.toDouble()))
            //子菜单项的左上角，右上角，左下角，右下角
            return Rect((childCenterX - size / 2).toInt(),
                    (childCenterY - size / 2).toInt(),
                    (childCenterX + size / 2).toInt(),
                    (childCenterY + size / 2).toInt())
        }
    }
}
