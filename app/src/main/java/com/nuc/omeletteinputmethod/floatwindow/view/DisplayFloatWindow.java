package com.nuc.omeletteinputmethod.floatwindow.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.floatwindow.util.ViewUtil;

public class DisplayFloatWindow extends FrameLayout {
    private ImageView mHintView;

    private ViewGroup controlLayout;
    private FloatWindowArcLayout mArcLayout;
    private int mPosition;

    private OnModeSeletedListener mOnModeSeleter;

    private void applyAttrs(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatWindowArcLayout, 0, 0);

            float fromDegrees = a.getFloat(R.styleable.FloatWindowArcLayout_fromDegrees, FloatWindowArcLayout.DEFAULT_FROM_DEGREES);
            float toDegrees = a.getFloat(R.styleable.FloatWindowArcLayout_toDegrees, FloatWindowArcLayout.DEFAULT_TO_DEGREES);
            mArcLayout.setArc(fromDegrees, toDegrees);

            int defaultChildSize = mArcLayout.getChildSize();
            int newChildSize = a.getDimensionPixelSize(R.styleable.FloatWindowArcLayout_childSize, defaultChildSize);
            mArcLayout.setChildSize(newChildSize);

            a.recycle();
        }
    }

    public DisplayFloatWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        applyAttrs(attrs);
    }

    private void init(Context context) {
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.float_window_display, this);

        mArcLayout = (FloatWindowArcLayout) findViewById(R.id.item_layout);

        controlLayout = (ViewGroup) findViewById(R.id.control_layout);
//        controlLayout.setClickable(false);
//        controlLayout.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    mHintView.startAnimation(createHintSwitchAnimation(mArcLayout.isExpanded()));
//                    mArcLayout.switchState(true,mPosition);
//                    mArcLayout.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            if(mOnModeSeleter != null)
//                                mOnModeSeleter.onModeSelected();
//                        }
//                    },350);
//
//                }
//
//                return false;
//            }
//        });

        mHintView = (ImageView) findViewById(R.id.control_hint);
    }




    public void applySizeChange(float persents){
        setArcLayoutSize((int) (ViewUtil.dp2px(146)* persents));
        setMenuSize((int) (ViewUtil.dp2px(40)* persents));
        setHintViewSize((int) (ViewUtil.dp2px(47)* persents));
        int padding = (int) (ViewUtil.dp2px(10)*persents);
        mHintView.setPadding(padding,padding,padding,padding);
        mArcLayout.setMinRadius(persents);
    }
    public void setMenuSize(int radiu){
        mArcLayout.setChildSize(radiu);
    }

    public void setHintViewSize(int width){
        FrameLayout.LayoutParams layoutParams= (LayoutParams) mHintView.getLayoutParams();
        if (layoutParams!=null){
            layoutParams.width=width;
            layoutParams.height=width;
            mHintView.setLayoutParams(layoutParams);
            mHintView.setMaxWidth(width);
            mHintView.setMaxHeight(width);
        }
    }
    public void setArcLayoutSize(int width){
        ViewGroup.LayoutParams layoutParams= (LayoutParams) mArcLayout.getLayoutParams();
        if (layoutParams!=null){
            layoutParams.width=width;
            layoutParams.height=width;
            mArcLayout.setLayoutParams(layoutParams);
        }
    }








    public ViewGroup getControlLayout() {
        return controlLayout;
    }

    public ImageView getmHintView() {
        return mHintView;
    }

    public FloatWindowArcLayout getmArcLayout() {
        return mArcLayout;
    }

    private static Animation createHintSwitchAnimation(final boolean expanded) {
        Animation animation = new RotateAnimation(expanded ? 45 : 0, expanded ? 0 : 45, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setStartOffset(0);
        animation.setDuration(100);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setFillAfter(true);

        return animation;
    }
    public interface OnModeSeletedListener{
        void onModeSelected();
        void onNothing();
    }

    public void addItem(View item, OnClickListener listener) {
        mArcLayout.addView(item);
     //   item.setOnClickListener(getItemClickListener(listener));
    }

    private OnClickListener getItemClickListener(final OnClickListener listener) {
        return new OnClickListener() {

            @Override
            public void onClick(final View viewClicked) {
                Animation animation = bindItemAnimation(viewClicked, true, 350);
                animation.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        postDelayed(new Runnable() {

                            @Override
                            public void run() {
//                                itemDidDisappear();
                            }
                        }, 0);
                    }
                });

                final int itemCount = mArcLayout.getChildCount();
                for (int i = 0; i < itemCount; i++) {
                    View item = mArcLayout.getChildAt(i);
                    if (viewClicked != item) {
                        bindItemAnimation(item, false, 300);
                    }
                }

                mArcLayout.invalidate();
                mHintView.startAnimation(createHintSwitchAnimation(true));

                if (listener != null) {
                    listener.onClick(viewClicked);
                }
            }
        };
    }
    private Animation bindItemAnimation(final View child, final boolean isClicked, final long duration) {
        Animation animation = createItemDisapperAnimation(duration, isClicked);
        child.setAnimation(animation);

        return animation;
    }
    private static Animation createItemDisapperAnimation(final long duration, final boolean isClicked) {
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(new ScaleAnimation(1.0f, isClicked ? 2.0f : 0.0f, 1.0f, isClicked ? 2.0f : 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f));
        animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));

        animationSet.setDuration(duration);
        animationSet.setInterpolator(new DecelerateInterpolator());
        animationSet.setFillAfter(true);

        return animationSet;
    }
}
