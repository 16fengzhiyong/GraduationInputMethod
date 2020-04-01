package com.nuc.omeletteinputmethod.floatwindow.util;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.floatwindow.FloatingImageDisplayService;
import com.nuc.omeletteinputmethod.floatwindow.view.DisplayFloatWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ArcTipViewController implements View.OnTouchListener {
    private static final String TAG = "TipViewController";

    private static final int MOVETOEDGE = 10010;
    private static final int HIDETOEDGE = 10011;


    private static final float DEFAULT_MAX_LENGTH = 146;
    private static final float DEFAULT_MIN_LENGTH = 50;
    public static final int DEFAULT_MIN_WIDTH_HIDE = 20;

    private static float MAX_LENGTH = DEFAULT_MAX_LENGTH;
    private static float MIN_LENGTH = DEFAULT_MIN_LENGTH;
    private DisplayFloatWindow displayFloatWindow;
    private boolean isShowIcon;
    private ImageView floatImageView;
    private float mCurrentIconAlpha = 1f;
    int padding = ViewUtil.dp2px(3);
    int arcMenupadding = ViewUtil.dp2px(8);
    private boolean isChangedColor;

    private long floatViewLastModified=-1;
    private Drawable floatViewLastCache;

    private boolean isStick;
    private int mScreenWidth, mScreenHeight;

    private ArcTipViewController(Context application) {
        mContext = application;
        initView();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }


    private static class InnerClass {
        private static ArcTipViewController instance = new ArcTipViewController(FloatingImageDisplayService.getInstance());
    }

    public static ArcTipViewController getInstance() {
        return InnerClass.instance;
    }

    private WindowManager mWindowManager;
    private Context mContext;
    private ViewGroup acrFloatView;
    private LinearLayout iconFloatView;


    private Handler mainHandler;
    private WindowManager.LayoutParams layoutParams;
    private float mTouchStartX, mTouchStartY;
    private int rotation;
    private boolean isMovingToEdge = false;
    private float density = 0;
    private boolean showBigBang = true;
    private boolean isMoving = false;
    private boolean isLongPressed = false;
    private int mScaledTouchSlop;

    private boolean isRemoved = false;
    private boolean isTempAdd = false;

    private int mStatusBarHeight = 0;



    int[] icons;
    String[] contentDiscription;

    private void initView() {


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            Point point = new Point();
//            mWindowManager.getDefaultDisplay().getSize(point);
//            mScreenWidth = point.x;
//            mScreenHeight = point.y;
//        } else {
//            mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
//            mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();
//        }
        acrFloatView = (RelativeLayout) View.inflate(mContext, R.layout.arc_view_float, null);
        displayFloatWindow = (DisplayFloatWindow) acrFloatView.findViewById(R.id.arc_menu);
        // event listeners
//        acrFloatView.setOnTouchListener(this);
//        iconFloatView.setOnTouchListener(this);
    }


    public static String getTAG() {
        return TAG;
    }

    public DisplayFloatWindow getDisplayFloatWindow() {
        return displayFloatWindow;
    }

    public ViewGroup getAcrFloatView() {
        return acrFloatView;
    }

    public LinearLayout getIconFloatView() {
        return iconFloatView;
    }

    public void initArcMenu(DisplayFloatWindow mdisplayFloatWindow, int[] itemDrawables) {
//        displayFloatWindow.removeAllItemViews();
        final int itemCount = itemDrawables.length;
//        applySizeChange();

//        if (showBigBang) {
//            mCurrentIconAlpha = SPHelper.getInt(ConstantUtil.FLOATVIEW_ALPHA, 70) / 100f;
//        } else {
//            mCurrentIconAlpha = 0.6f * SPHelper.getInt(ConstantUtil.FLOATVIEW_ALPHA, 70) / 100f;
//        }
        for (int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(acrFloatView.getContext());
            item.setImageResource(itemDrawables[i]);
//            item.setContentDescription(contentDiscription[i]);
            item.setContentDescription("contentDiscription[i]"+i);
            item.setPadding(arcMenupadding, arcMenupadding, arcMenupadding, arcMenupadding);
            item.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            if (i == 0) {
                item.setAlpha(mCurrentIconAlpha);
                if (showBigBang) {
                    item.setContentDescription("contentDiscription[i]"+i);
                }else {
                    item.setContentDescription("?????");
                }
            } else {
//                item.setAlpha(SPHelper.getInt(ConstantUtil.FLOATVIEW_ALPHA, 70) / 100f);
            }


            final int position = i;

            mdisplayFloatWindow.addItem(item, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
//                    showFuncation(position);
//                    showFloatImageView();
                }
            });
        }
    }

}
