package com.nuc.omeletteinputmethod.floatwindow.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.floatwindow.FloatingWindowDisplayService;

public class FloatWindowLayout extends ViewGroup {

    private int mRadius = 150;// 中心菜单圆点到子菜单中心的距离
    private boolean mExpanded = false;
    private int mChildPadding = 5;

    private int centerX = 0;
    private int centerY = 0;
    private int position = PathMenu.LEFT_CENTER;
    private int nowState;

    public static final float DEFAULT_FROM_DEGREES = 270.0f;
    public static final float DEFAULT_TO_DEGREES = 360.0f;
    private float mFromDegrees = DEFAULT_FROM_DEGREES;
    private float mToDegrees = DEFAULT_TO_DEGREES;

    private int mChildSize = 0;

    public FloatWindowLayout(Context context) {
        super(context);
    }

    public FloatWindowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatWindowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FloatWindowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        computeCenterXY(position);
        //当子菜单要收缩时radius=0，在ViewGroup坐标中心
//        final int radius = mExpanded ? mRadius : 0;

        final int childCount = getChildCount();


        View child;
        int childWidth;//item的宽
        int childHeight;//item的高
        double corner ;//旋转角度

        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }
            corner = 180 / childCount * i;

            childWidth = child.getMeasuredWidth()/4;
            childHeight = child.getMeasuredHeight()/4;
            Log.wtf("分配的大小是什么","onLayout: childWidth childWidth"+childWidth+"childHeight " +childHeight);
            Log.wtf("分配的大小是什么","onLayout: childWidth childid"+child.getId() +
                     " R.id.bar_image_1"+R.id.bar_image_1 + " R.id.bar_image_1"+R.id.bar_image_2);

            int thisRadius = 0;
            if (child.getId() == R.id.bar_image_center){
                thisRadius = 0;
                childWidth = child.getMeasuredWidth()/4;
                childHeight = child.getMeasuredHeight()/4;
            }else thisRadius = mRadius;

            int cX =0,cY = 0;
            if (position==PathMenu.LEFT_CENTER){
                cX = (int) (centerX - thisRadius * Math.cos(Math.toRadians(corner +120)));
                cY = (int) (centerY - thisRadius * Math.sin(Math.toRadians(corner +120)));
            }
            Log.e("分配的大小是什么","onLayout: childWidth centerX"+centerX+"centerY " +centerY +"mRadius "+mRadius);

            Log.e("分配的大小是什么","onLayout: childWidth cX"+cX+"cY " +cY +"mRadius "+mRadius);
            Log.d("分配的大小是什么","onLayout: old childWidth"+String.valueOf(cX-childWidth/2)+"childHeight"+ String.valueOf(cY-childHeight/2)+"childWidth"
                    + String.valueOf(cX + childWidth / 2)+"childHeight"+ String.valueOf(cY + childHeight / 2));
            child.layout(cX - childWidth / 2, cY - childHeight / 2, cX + childWidth / 2, cY + childHeight / 2);
        }
    }

    FloatingWindowDisplayService floatingWindowDisplayService;
    /**
     * 切换中心按钮的展开缩小
     */
    public boolean switchState(final boolean showAnimation, int position , FloatingWindowDisplayService floatingWindowDisplayService, int nowState) {
        this.floatingWindowDisplayService = floatingWindowDisplayService;
//        this.position = position;
        this.nowState = nowState;
        if (showAnimation) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                bindChildAnimation(getChildAt(i), i, 300);
            }
        }

        mExpanded = !mExpanded;

        if (!showAnimation) {
            requestLayout();
        }

        invalidate();
        return mExpanded;
    }

    /**
     * 绑定子菜单项动画
     */
    private void bindChildAnimation(final View child, final int index,
                                    final long duration) {

        final boolean expanded = mExpanded;
//        final int centerX = getWidth() / 2 - mRadius;  //ViewGroup的中心X坐标
//        final int centerY = getHeight() / 2;

        computeCenterXY(position);
        final int radius = expanded ? 0 : mRadius;

        final int childCount = getChildCount();
        final float perDegrees = Math.abs(mToDegrees - mFromDegrees) == 360 ? (mToDegrees - mFromDegrees) / (childCount) : (mToDegrees - mFromDegrees) / (childCount - 1);
        Rect frame = computeChildFrame(centerX, centerY, radius, mFromDegrees
                + index * perDegrees, 0);


         int toXDelta = frame.left - child.getLeft();//展开或收缩动画,child沿X轴位移距离
         int toYDelta = frame.top - child.getTop();//展开或收缩动画,child沿Y轴位移距离

        toXDelta = centerX - child.getLeft() - child.getWidth()/2;
        toYDelta = centerY - child.getTop() - child.getHeight()/2;


        Log.d("arcLayout","toX:"+ toXDelta+" toY:"+toYDelta);
        Interpolator interpolator = mExpanded ? new AccelerateInterpolator()
                : new OvershootInterpolator(1.5f);
        final long startOffset = computeStartOffset(childCount, mExpanded,
                index, 0.1f, duration, interpolator);
        //TODO toXDelta toYDelta 设置为0 为什么可以
        //mExpanded为true，已经展开，收缩动画；为false,展开动画


        Log.d("onLayout", "bindChildAnimation: toXDelta "+toXDelta+"toYDelta "+toYDelta +"mExpanded" + mExpanded);
        Log.i("onLayout", "bindChildAnimation: child.getLeft() "+child.getLeft()+"child.getTop() "+child.getTop() +"mExpanded" + mExpanded);
        Log.v("onLayout", "bindChildAnimation: child.getWidth() "+child.getWidth()+"child.getHeight() "+child.getHeight() +"mExpanded" + mExpanded);


        Animation animation = mExpanded ? createShrinkAnimation(child,0, toXDelta, 0,
                toYDelta, startOffset, duration, interpolator)
                : createExpandAnimation(child,0, toXDelta, 0, toYDelta, startOffset,
                duration, interpolator);

        final boolean isLast = getTransformedIndex(expanded, childCount, index) == childCount - 1;
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isLast) {
                    postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            onAllAnimationsEnd();
                        }
                    }, 0);
                }
                if (!mExpanded&&child.getId()!=R.id.bar_image_center){
                    child.setVisibility(GONE);
                }
                if (isLast&&!mExpanded){
                    switch (nowState){
                        case 0:
                            Log.i("点击中心的图标", "onAnimationEnd: ");
                            floatingWindowDisplayService.sendMessageToHandler(2);
                            break;
                        case StateMenu.SHORT_INPUT:
                            floatingWindowDisplayService.sendMessageToHandler(StateMenu.SHORT_INPUT);
                            break;
                            case StateMenu.SCHEDULE:
                                floatingWindowDisplayService.sendMessageToHandler(StateMenu.SCHEDULE);
                                break;
                        case StateMenu.NOTEPAD:
                            floatingWindowDisplayService.sendMessageToHandler(StateMenu.NOTEPAD);
                            break;
                        case StateMenu.TRANSLATE:
                            floatingWindowDisplayService.sendMessageToHandler(StateMenu.TRANSLATE);
                            break;

                    }

                }

            }
        });

        child.setAnimation(animation);
    }


    /**
     * 计算动画开始时的偏移量
     */
    private static long computeStartOffset(final int childCount,
                                           final boolean expanded, final int index, final float delayPercent,
                                           final long duration, Interpolator interpolator) {
        final float delay = delayPercent * duration;
        final long viewDelay = (long) (getTransformedIndex(expanded,
                childCount, index) * delay);
        final float totalDelay = delay * childCount;

        float normalizedDelay = viewDelay / totalDelay;
        normalizedDelay = interpolator.getInterpolation(normalizedDelay);

        return (long) (normalizedDelay * totalDelay);
    }
    /**
     * 变换时的子菜单项索引
     */
    private static int getTransformedIndex(final boolean expanded,
                                           final int count, final int index) {
        if (expanded) {
            return count - 1 - index;
        }

        return index;
    }

    /**
     * 展开动画
     */
    private static Animation createExpandAnimation(View child ,float fromXDelta,
                                                   float toXDelta, float fromYDelta, float toYDelta, long startOffset,
                                                   long duration, Interpolator interpolator) {
        child.setVisibility(VISIBLE);
        Animation animation = new RotateAndTranslateAnimation(toXDelta,0, toYDelta,0,
                 0, 720);
        animation.setStartOffset(startOffset);
        animation.setDuration(duration);
        animation.setInterpolator(interpolator);
        animation.setFillAfter(true);

        return animation;
    }

    /**
     * 收缩动画
     */
    private static Animation createShrinkAnimation(View child,float fromXDelta,
                                                   float toXDelta, float fromYDelta, float toYDelta, long startOffset,
                                                   long duration, Interpolator interpolator) {
        AnimationSet animationSet = new AnimationSet(false);
        animationSet.setFillAfter(true);
        //收缩过程中，child 逆时针自旋转360度
        final long preDuration = duration / 2;
        Animation rotateAnimation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotateAnimation.setStartOffset(startOffset);
        rotateAnimation.setDuration(preDuration);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setFillAfter(true);

        animationSet.addAnimation(rotateAnimation);
        //收缩过程中位移，并逆时针旋转360度
        Animation translateAnimation = new RotateAndTranslateAnimation(0,
                toXDelta, 0, toYDelta, 360, 720);
        translateAnimation.setStartOffset(startOffset + preDuration);
        translateAnimation.setDuration(duration - preDuration);
        translateAnimation.setInterpolator(interpolator);
        translateAnimation.setFillAfter(true);

        animationSet.addAnimation(translateAnimation);
        animationSet.setFillAfter(true);
        return animationSet;
    }
    /**
     * 结束所有动画
     */
    private void onAllAnimationsEnd() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).clearAnimation();
        }

        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


        //拥有的view的数量
        int childCount = getChildCount();//item的数量

        //可用宽高
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int ps = getPaddingStart();
        int pe = getPaddingEnd();
        int pt = getPaddingTop();
        int pb = getPaddingBottom();

        //设置大小
        setMeasuredDimension(widthSize, heightSize);

        //子view最高多少，最宽多少
        int childMaxWidth = 0;
        int childMaxHeight = 0;
        View child;
        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            child.measure(MeasureSpec.makeMeasureSpec(widthSize - ps - pe, MeasureSpec.UNSPECIFIED)
                    , MeasureSpec.makeMeasureSpec(heightSize - pt - pb, MeasureSpec.UNSPECIFIED));

            childMaxWidth = Math.max(childMaxWidth, child.getMeasuredWidth());
            childMaxHeight = Math.max(childMaxHeight, child.getMeasuredHeight());
        }
    }


    private int getRadiusAndPadding() {
        return mRadius/2 + (mChildPadding );
    }


    /**
     * 从什么地方展开 用于屏幕各个位置
     * @param position 展开位置
     */
    public void computeCenterXY(int position) {
        switch (position) {
            case PathMenu.LEFT_TOP://左上
                centerX = getWidth() / 2 - getRadiusAndPadding();
                centerY = getHeight() / 2 - getRadiusAndPadding();
                break;
            case PathMenu.LEFT_CENTER://左中
                centerX = getWidth() / 2 - getRadiusAndPadding();
                centerY = getHeight() / 2;
                break;
            case PathMenu.LEFT_BOTTOM://左下
                centerX = getWidth() / 2 - getRadiusAndPadding();
                centerY = getHeight() / 2 + getRadiusAndPadding();
                break;
            case PathMenu.CENTER_TOP://上中
                centerX = getWidth() / 2;
                centerY = getHeight() / 2 - getRadiusAndPadding();
                break;
            case PathMenu.CENTER_BOTTOM://下中
                centerX = getWidth() / 2;
                centerY = getHeight() / 2 + getRadiusAndPadding();
                break;
            case PathMenu.RIGHT_TOP://右上
                centerX = getWidth() / 2 + getRadiusAndPadding();
                centerY = getHeight() / 2 - getRadiusAndPadding();
                break;
            case PathMenu.RIGHT_CENTER://右中
                centerX = getWidth() / 2 + getRadiusAndPadding();
                centerY = getHeight() / 2;
                break;
            case PathMenu.RIGHT_BOTTOM://右下
                centerX = getWidth() / 2 + getRadiusAndPadding();
                centerY = getHeight() / 2 + getRadiusAndPadding();
                break;

            case PathMenu.CENTER:
                centerX = getWidth() / 2;
                centerY = getHeight() / 2;
                break;
        }
    }



    /**
     * 设定弧度
     */
    public void setArc(float fromDegrees, float toDegrees, int position) {
        this.position = position;
        if (mFromDegrees == fromDegrees && mToDegrees == toDegrees) {
            return;
        }

        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        computeCenterXY(position);
        requestLayout();
    }

    /**
     * 计算子菜单项的范围
     */
    private static Rect computeChildFrame(final int centerX, final int centerY,
                                          final int radius, final float degrees, final int size) {
        //子菜单项中心点
        final double childCenterX = centerX + radius
                * Math.cos(Math.toRadians(degrees));
        final double childCenterY = centerY + radius
                * Math.sin(Math.toRadians(degrees));
        //子菜单项的左上角，右上角，左下角，右下角
        return new Rect((int) (childCenterX - size / 2),
                (int) (childCenterY - size / 2),
                (int) (childCenterX + size / 2),
                (int) (childCenterY + size / 2));
    }
}
