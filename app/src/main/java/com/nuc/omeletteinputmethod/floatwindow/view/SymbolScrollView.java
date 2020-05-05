package com.nuc.omeletteinputmethod.floatwindow.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.kernel.OmeletteIME;

import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.VelocityTrackerCompat;
import androidx.core.view.ViewCompat;


/**
 * Created by zhufeng on 2017/7/26.
 */

public class SymbolScrollView extends View {
    private Context mContext;
    private int SCREEN_WIDTH = 0;
    private int SCREEN_HEIGHT = 0;
    private int mWidth = 0;
    private int mHeight = 0;
    private static final int INVALID_POINTER = -1;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;

    private int mScrollState = SCROLL_STATE_IDLE;
    private int mScrollPointerId = INVALID_POINTER;
    private VelocityTracker mVelocityTracker;
    private int mLastTouchY;
    private int mTouchSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private final ViewFlinger mViewFlinger = new ViewFlinger();

    private String[] titles = new String[]{"早餐前", "早餐后", "午餐前", "午餐后", "晚餐前", "晚餐后", "睡前"};
    private Paint paint;
    private Rect textBound;
    private int downY;

    private int onClickNumber = -1;
    private RectF nowClickRecT ;

    private OmeletteIME omeletteIME = null;

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x11) {
                onClickNumber = -1;
                invalidate();
            }else if (msg.what == 0x01){
            }
        }
    };

    public void setOmeletteIME(OmeletteIME omeletteIME) {
        this.omeletteIME = omeletteIME;
    }

    //f(x) = (x-1)^5 + 1
    private static final Interpolator sQuinticInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    public SymbolScrollView(Context context) {
        this(context, null);
    }

    public SymbolScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SymbolScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        textBound = new Rect();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setTextSize(sp2px(getContext(), 10));
        init(context);
    }
    private int getCurrentPosition( float downX,float downY) {
        int position = 1;
        int realX,realY;
        realX = (int) (downX + getScrollX());
        realY = (int) (downY + getScrollY());
        Log.i("点击位置", "getCurrentPosition  x: "+downX);
        Log.i("点击位置", "getCurrentPosition  y: "+downY);
        Log.i("真实长高", "getCurrentPosition: getMeasuredWidth  " + getMeasuredWidth());
        Log.i("真实长高", "getCurrentPosition: getMeasuredHeight  " + getMeasuredHeight());
        int ver = 0;
        //getMeasuredWidth() / 6 * (2 * i % 5 + 1)- getMeasuredWidth() / 14,(getMeasuredHeight() / 10 * (2 * ver +1) - getMeasuredHeight() / 12),
        //                    getMeasuredWidth() / 14+ getMeasuredWidth() / 6 * (2 * i % 5 + 1),(getMeasuredHeight() / 10 * (2 * ver +1))+getMeasuredHeight() / 12
        for (int i = 0; i < titles.length; i++) {
            if (i%5 == 0 && i != 0){
                ver++;
            }
            if (realX >= getMeasuredWidth() / 6 * (2 * i % 5 + 1)- getMeasuredWidth() / 12
                    && realX <=  getMeasuredWidth() / 12+ getMeasuredWidth() / 6 * (2 * i % 5 + 1)
                    && realY >= (getMeasuredHeight() / 10 * (2 * ver +1) - getMeasuredHeight() / 10)
                    && realY <= (getMeasuredHeight() / 10 * (2 * ver +1) + getMeasuredHeight() / 10)
            ) {

                nowClickRecT = new RectF(getMeasuredWidth() / 6 * (2 * i % 5 + 1)- getMeasuredWidth() / 12,
                        getMeasuredWidth() / 12+ getMeasuredWidth() / 6 * (2 * i % 5 + 1),
                        (getMeasuredHeight() / 10 * (2 * ver +1) - getMeasuredHeight() / 10),
                        (getMeasuredHeight() / 10 * (2 * ver +1) + getMeasuredHeight() / 10));
                onClickNumber = i;
                position = i;
                invalidate();
                break;
            }
        }
        Log.i("点击数据为", "getCurrentPosition: "+titles[position]);
        return position;
    }
    Canvas canvas;
    Paint mPaint;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        Paint lspaint = new Paint();
        mPaint = new Paint();
        // 绘制画布背景
        canvas.drawColor(Color.parseColor("#ECEFF1"));
        //mPaint.setColor(Color.WHITE);
        mPaint.setColor(R.color.colorPrimary);
        mPaint.setTextSize(sp2px(getContext(), 20));
        int ver = 0;
        for (int i = 0; i < titles.length; i++) {
            if (onClickNumber == i){

            }
            mPaint.getTextBounds(titles[i], 0, titles[i].length(), textBound);
            if (i%5 == 0 && i != 0){
                ver++;
            }
            RectF rect = new RectF(getMeasuredWidth() / 6 * (2 * i % 5 + 1)- getMeasuredWidth() / 14,(getMeasuredHeight() / 10 * (2 * ver +1) - getMeasuredHeight() / 12),
                    getMeasuredWidth() / 14+ getMeasuredWidth() / 6 * (2 * i % 5 + 1),(getMeasuredHeight() / 10 * (2 * ver +1))+getMeasuredHeight() / 12 );
            if (onClickNumber == i){
                lspaint.setColor(Color.YELLOW);
            }else {
                lspaint.setColor(Color.WHITE);
            }

            canvas.drawRoundRect(rect, 20, 20, lspaint);
            canvas.drawText(titles[i], getMeasuredWidth() / 6 * (2 * i % 5 + 1) - (textBound.width() / 2), (getMeasuredHeight() / 10 * (2 * ver +1)) + textBound.height() / 2, mPaint);
        }
        mHeight = (getMeasuredHeight() / 10 * (2 * ver +1)) + textBound.height()*2;
        mWidth=1080;
    }

    public void setTitles(String titles[]) {
        this.titles = titles;
        invalidate();
    }
    /**
     * sp转px
     *
     * @return
     */
    public static int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }

    private void init(Context context) {
        this.mContext = context;
        final ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        SCREEN_WIDTH = metric.widthPixels;
        SCREEN_HEIGHT = metric.heightPixels;
    }

    int oldx;
    int oldy;
    int oldscrollX;
    int oldscrollY;
    boolean ifTouch = true;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        boolean eventAddedToVelocityTracker = false;
        final int action = MotionEventCompat.getActionMasked(event);
        final int actionIndex = MotionEventCompat.getActionIndex(event);
        final MotionEvent vtev = MotionEvent.obtain(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                setScrollState(SCROLL_STATE_IDLE);
                mScrollPointerId = event.getPointerId(0);
                mLastTouchY = (int) (event.getY() + 0.5f);
                oldscrollX = getScrollX();
                oldscrollY = getScrollY();
                oldx = (int) event.getRawX();
                oldy = (int) event.getRawY();

                ifTouch = true;
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                mScrollPointerId = event.getPointerId(actionIndex);
                mLastTouchY = (int) (event.getY(actionIndex) + 0.5f);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int nowX = (int) event.getRawX();
                int nowY = (int) event.getRawY();
                int movedX = nowX - oldx;
                int movedY = nowY - oldy;
                if (movedX > 1||movedY>1||getScrollX() - oldscrollX>1||getScrollY() - oldscrollY >1){
                    ifTouch = false;
                }

                final int index = event.findPointerIndex(mScrollPointerId);
                if (index < 0) {
                    Log.e("zhufeng", "Error processing scroll; pointer index for id " + mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                final int y = (int) (event.getY(index) + 0.5f);
                int dy = mLastTouchY - y;

                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    boolean startScroll = false;

                    if (Math.abs(dy) > mTouchSlop) {
                        if (dy > 0) {
                            dy -= mTouchSlop;
                        } else {
                            dy += mTouchSlop;
                        }
                        startScroll = true;
                    }
                    if (startScroll) {
                        setScrollState(SCROLL_STATE_DRAGGING);
                    }
                }

                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    mLastTouchY = y;
                    constrainScrollBy(0, dy);
                }

                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP: {
                if (event.getPointerId(actionIndex) == mScrollPointerId) {
                    // Pick a new pointer to pick up the slack.
                    final int newIndex = actionIndex == 0 ? 1 : 0;
                    mScrollPointerId = event.getPointerId(newIndex);
                    mLastTouchY = (int) (event.getY(newIndex) + 0.5f);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                mVelocityTracker.addMovement(vtev);
                eventAddedToVelocityTracker = true;
                mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                float yVelocity = -VelocityTrackerCompat.getYVelocity(mVelocityTracker, mScrollPointerId);
                Log.i("zhufeng", "速度取值：" + yVelocity);
                if (Math.abs(yVelocity) < mMinFlingVelocity) {
                    yVelocity = 0F;
                } else {
                    yVelocity = Math.max(-mMaxFlingVelocity, Math.min(yVelocity, mMaxFlingVelocity));
                }
                if (yVelocity != 0) {
                    mViewFlinger.fling((int) yVelocity);
                } else {
                    setScrollState(SCROLL_STATE_IDLE);
                }
                resetTouch();
                if (ifTouch){
                    invalidate();
                    omeletteIME.getCurrentInputConnection()
                            .commitText(titles[getCurrentPosition(event.getX(),event.getY())],0);
                    Message message = Message.obtain();
                    message.what = 0x11;
                    handler.sendMessageDelayed(message,100);
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                resetTouch();
                break;
            }
        }
        if (!eventAddedToVelocityTracker) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        return true;

    }

    private void resetTouch() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
        }
    }

    private void setScrollState(int state) {
        if (state == mScrollState) {
            return;
        }
        mScrollState = state;
        if (state != SCROLL_STATE_SETTLING) {
            mViewFlinger.stop();
        }
    }

    private class ViewFlinger implements Runnable {

        private int mLastFlingY = 0;
        private OverScroller mScroller;
        private boolean mEatRunOnAnimationRequest = false;
        private boolean mReSchedulePostAnimationCallback = false;

        public ViewFlinger() {
            mScroller = new OverScroller(getContext(), sQuinticInterpolator);
        }

        @Override
        public void run() {
            disableRunOnAnimationRequests();
            final OverScroller scroller = mScroller;
            if (scroller.computeScrollOffset()) {
                final int y = scroller.getCurrY();
                int dy = y - mLastFlingY;
                mLastFlingY = y;
                constrainScrollBy(0, dy);
                postOnAnimation();
            }
            enableRunOnAnimationRequests();
        }

        public void fling(int velocityY) {
            mLastFlingY = 0;
            setScrollState(SCROLL_STATE_SETTLING);
            mScroller.fling(0, 0, 0, velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            postOnAnimation();
        }

        public void stop() {
            removeCallbacks(this);
            mScroller.abortAnimation();
        }

        private void disableRunOnAnimationRequests() {
            mReSchedulePostAnimationCallback = false;
            mEatRunOnAnimationRequest = true;
        }

        private void enableRunOnAnimationRequests() {
            mEatRunOnAnimationRequest = false;
            if (mReSchedulePostAnimationCallback) {
                postOnAnimation();
            }
        }

        void postOnAnimation() {
            if (mEatRunOnAnimationRequest) {
                mReSchedulePostAnimationCallback = true;
            } else {
                removeCallbacks(this);
                ViewCompat.postOnAnimation(SymbolScrollView.this, this);
            }
        }
    }

    private void constrainScrollBy(int dx, int dy) {
        Rect viewport = new Rect();
        getGlobalVisibleRect(viewport);
        int height = viewport.height();
        int width = viewport.width();

        int scrollX = getScrollX();
        int scrollY = getScrollY();

        //右边界
        if (mWidth - scrollX - dx < width) {
            dx = mWidth - scrollX - width;
        }
        //左边界
        if (-scrollX - dx > 0) {
            dx = -scrollX;
        }
        //下边界
        if (mHeight - scrollY - dy < height) {
            dy = mHeight - scrollY - height;
        }
        //上边界
        if (scrollY + dy < 0) {
            dy = -scrollY;
        }
        scrollBy(dx, dy);
    }
}