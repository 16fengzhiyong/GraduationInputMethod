package com.nuc.omeletteinputmethod.floatwindow.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;


/**
 * Created by lsp on 2017/2/24.
 */

public class SymbolScrollChooseView extends View {
    private String[] titles = new String[]{"0", "1", "2", "3", "4", "5", "6"};
    private Paint paint;
    private Rect textBound;
    private int downX;

    private Scroller mScroller;
    private int lastScrollX = 0;
    private boolean isScroll = false;

    private boolean isClick = false;
    private OnScrollEndListener onScrollEndListener;

    public SymbolScrollChooseView(Context context) {
        this(context, null);
    }

    public SymbolScrollChooseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SymbolScrollChooseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        textBound = new Rect();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setTextSize(sp2px(getContext(), 10));

        mScroller = new Scroller(context);

    }

    public void setTitles(String titles[]) {
        this.titles = titles;
        invalidate();
    }

    public void setOnScrollEndListener(OnScrollEndListener onScrollEndListener) {
        this.onScrollEndListener = onScrollEndListener;
    }


    private int getCurrentPosition() {
        int position = 1;
        Log.i("点击位置", "getCurrentPosition  x: "+getScrollX()+" y:" +getScrollY());
        Log.i("点击位置", "getCurrentPosition  getMeasuredWidth: "+getMeasuredWidth()
                +" getMeasuredHeight: "+getMeasuredHeight());
        for (int i = 0; i < titles.length ; i++) {
            if (getScrollX() >= getMeasuredWidth() / 15 * (i*3+1) - getMeasuredWidth() / 2 && getScrollX() <= getMeasuredWidth() / 15 * ((i+1)*3+1) - getMeasuredWidth()/2 ) {

                return i;
            }
        }
        return position;


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (titles.length <= 1) {
            return super.onTouchEvent(event);
        }
        int x = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isClick = true;
                downX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                isClick = false;
                int offset = x - downX;
                if (getScrollX() < -getMeasuredWidth() / 2 || getScrollX() > getMeasuredWidth() /5.0 * titles.length  - getMeasuredWidth() / 2) {
                    return super.onTouchEvent(event);
                } else {
                    scrollX(lastScrollX - offset, true);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isClick) {
                    return true;
                }
                Log.i("抬起时 移动至", "onTouchEvent: " +getMeasuredWidth() / 5.0 +"getScrollX = "+getScrollX());
                if (getScrollX() < -getMeasuredWidth() /  2  +getMeasuredWidth() / 15) {
                    scrollX((int) (-getMeasuredWidth() / 2 +getMeasuredWidth() / 15 ), true);
                    Log.i("小于时 超过范围后 移动至", "onTouchEvent: getScrollX() = "+getScrollX()
                            +" Width = "+(- getMeasuredWidth() / 2));
                }
                if (getScrollX() > getMeasuredWidth() /5.0 * titles.length  - getMeasuredWidth() / 2) {

                    Log.i("大于时 超过范围后 移动至", "onTouchEvent: getScrollX() = "+getScrollX()
                    +" Width = "+(getMeasuredWidth() /5.0 * titles.length  - getMeasuredWidth() / 2));
                    scrollX((int) (getMeasuredWidth() /5.0 * titles.length  - getMeasuredWidth() / 2), true);
                }
                Log.i("点击位置 移动至", "getCurrentPosition: 可以返回信息= "+getCurrentPosition());
                scrollX((int) (getMeasuredWidth() / 5 * (getCurrentPosition() -2)), false);

                break;
        }
        return true;
    }

    private void scrollX(int endX, boolean b) {
        Log.i("移动至", "scrollX:endX = " +endX);
        if (b) {
            scrollTo(endX, 0);
        } else {
            isScroll = true;
            lastScrollX = endX;
            smoothScrollTo(endX, 0);
        }
    }

    public void smoothScrollTo(int destX, int destY) {
        mScroller.startScroll(getScrollX(), 0, destX - getScrollX(), 0, 200);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        } else {
            if (isScroll) {
                setScrollX(lastScrollX);
                isScroll = false;
                if (onScrollEndListener != null) {
                    onScrollEndListener.currentPosition(getCurrentPosition());
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widhtMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        setMeasuredDimension(widhtMode == MeasureSpec.EXACTLY ? widthSize : widthSize, heightMode == MeasureSpec.EXACTLY ? heightSize : dp2px(getContext(), 100));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (titles == null) {
            return;
        }
        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        //canvas.drawColor(Color.BLACK);
        paint.setColor(Color.BLACK);
        for (int i = 0; i < titles.length; i++) {
            if (getCurrentPosition() == i) {
                paint.setTextSize(sp2px(getContext(), 20));
                paint.getTextBounds(titles[i], 0, titles[i].length(), textBound);
                canvas.drawText(titles[i], getMeasuredWidth() / 15 * (3 * i + 1) - (textBound.width() / 2), (getMeasuredHeight() / 2) + textBound.height() / 2, paint);
                paint.setStrokeWidth(3);
                canvas.drawLine(getMeasuredWidth() / 15  * (3 * i + 1) - (textBound.width() / 2) - 10, (getMeasuredHeight() / 2) + textBound.height(),
                        getMeasuredWidth() / 15  * (3 * i + 1) + (textBound.width() / 2) + 10, (getMeasuredHeight() / 2) + textBound.height(), paint);
            } else {
                paint.setTextSize(sp2px(getContext(), 10));
                paint.getTextBounds(titles[i], 0, titles[i].length(), textBound);
                canvas.drawText(titles[i], getMeasuredWidth() / 15  * (3 * i + 1) - (textBound.width() / 2), (getMeasuredHeight() / 2) + textBound.height() / 2, paint);
            }
        }
    }

    public interface OnScrollEndListener {
        void currentPosition(int position);
    }


    /**
     * dp转px
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
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
}

