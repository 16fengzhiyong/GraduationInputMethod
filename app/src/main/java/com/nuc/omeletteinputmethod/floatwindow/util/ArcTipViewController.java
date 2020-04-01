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
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
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
    private static final String TAG = "悬浮窗构建控制";


    private static ArcTipViewController instance = null;


    private WindowManager mWindowManager;
    private WindowManager.LayoutParams layoutParams;

    private View displayView;

    private int[] images;
    private int imageIndex = 0;


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


    private Context mContext;
    private ViewGroup acrFloatView;

    private boolean showBigBang = true;
    String[] contentDiscription;

    private Handler mainHandler;

    /**
     * 构造
     *
     * @param application
     */
    private ArcTipViewController(Context application) {
        mContext = application;
        start();
        initView();
    }

    public static ArcTipViewController getInstance() {
        if (instance == null) {
            instance = new ArcTipViewController(FloatingImageDisplayService.getInstance());
        } else ;
        return instance;
    }

    /**
     * 开始 and 赋值
     */
    public void start() {
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 60;
        layoutParams.height = 120;
        layoutParams.x = 0;
        layoutParams.y = 300;

        images = new int[]{
                R.drawable.ic_notepad,
                R.drawable.ic_schedule,
                R.drawable.ic_shortcutinput,
                R.drawable.ic_translate,
        };

        //    changeImageHandler = new Handler(this.getMainLooper(), changeImageCallback);
    }

    /**
     * 初始布局
     */
    private void initView() {

        acrFloatView = (RelativeLayout) View.inflate(mContext, R.layout.arc_view_float, null);
        acrFloatView.setOnTouchListener(this);
        displayFloatWindow = (DisplayFloatWindow) acrFloatView.findViewById(R.id.arc_menu);
        displayFloatWindow.setOnTouchListener(this);
        displayFloatWindow.applySizeChange(10);
        mWindowManager.addView(acrFloatView, layoutParams);
        initArcMenu(displayFloatWindow, images);
    }


    public static String getTAG() {
        return TAG;
    }


    public void initArcMenu(DisplayFloatWindow mdisplayFloatWindow, int[] itemDrawables) {
//        displayFloatWindow.removeAllItemViews();
        final int itemCount = itemDrawables.length;
        for (int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(acrFloatView.getContext());
            item.setImageResource(itemDrawables[i]);
//            item.setContentDescription(contentDiscription[i]);
            item.setContentDescription("contentDiscription[i]" + i);
            item.setPadding(arcMenupadding, arcMenupadding, arcMenupadding, arcMenupadding);
            item.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            if (i == 0) {
                item.setAlpha(mCurrentIconAlpha);
                if (showBigBang) {
                    item.setContentDescription("contentDiscription[i]" + i);
                } else {
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

//    private void showArcMenuView() {
//        //TODO 设置了大小会导致放到最右侧贴边展开时不贴边
//        // reuseSavedWindowMangerPosition(ViewUtil.dp2px(MAX_LENGTH), ViewUtil.dp2px(MAX_LENGTH));
//        reuseSavedWindowMangerPosition();
////        removeAllView();
//        mainHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                synchronized (ArcTipViewController.this) {
//                    try {
//                        acrFloatView.setVisibility(View.VISIBLE);
//                        acrFloatView.setOnTouchListener(ArcTipViewController.this);
//                        int position = getArcPostion(layoutParams);
//                        mWindowManager.addView(acrFloatView, layoutParams);
//                        reMeasureHeight(position, layoutParams);
//                        initArcMenu(archMenu, icons);
//                        archMenu.refreshPathMenu(position);
//                        mWindowManager.updateViewLayout(acrFloatView, layoutParams);
//                        archMenu.performClickShowMenu(position);
//
//                        isShowIcon = false;
//                    } catch (Throwable e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }


    private int x;
    private int y;
    private boolean actionDown = false;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionDown = true;
                Log.i("悬浮窗事件", "onTouch: ACTION_DOWN actionDown :" + actionDown);
                x = (int) event.getRawX();
                y = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                actionDown = false;
                Log.i("悬浮窗事件", "onTouch: ACTION_MOVE actionDown :" + actionDown);
                int nowX = (int) event.getRawX();
                int nowY = (int) event.getRawY();
                int movedX = nowX - x;
                int movedY = nowY - y;
                x = nowX;
                y = nowY;
                layoutParams.x = layoutParams.x + movedX;
                layoutParams.y = layoutParams.y + movedY;
                mWindowManager.updateViewLayout(view, layoutParams);
//                    if (layoutParams.x>120){
//                        layoutParams.width = 120;
//                    }else layoutParams.width = 60;
                break;
            case MotionEvent.ACTION_UP:
                Log.i("悬浮窗事件", "onTouch: ACTION_UP actionDown :" + actionDown);
                if (actionDown) {
                    displayFloatWindow.getmHintView().startAnimation(createHintSwitchAnimation(displayFloatWindow.getmArcLayout().isExpanded()));
//                    displayFloatWindow.getmHintView().setVisibility(View.GONE);

//                    displayFloatWindow.getmArcLayout().setVisibility(View.VISIBLE);
                    displayFloatWindow.getmArcLayout().switchState(true, 60);
                    displayFloatWindow.getmArcLayout().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 350);
                    actionDown = false;
                }
                break;
            default:
                break;
        }
        return false;
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

    private void reuseSavedWindowMangerPosition() {
        reuseSavedWindowMangerPosition(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    private float density = 0;
    private int mScreenWidth, mScreenHeight;
    private int rotation;

    private void reuseSavedWindowMangerPosition(int width_vale, int height_value) {
        //获取windowManager
        int w = width_vale;
        int h = height_value;
        if (layoutParams == null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
            density = displayMetrics.density;

            int flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            int type = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(mContext)) {
                type = WindowManager.LayoutParams.TYPE_PHONE;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT< Build.VERSION_CODES.N) {
                type = WindowManager.LayoutParams.TYPE_TOAST;
            } else {
                type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                Point point = new Point();
                mWindowManager.getDefaultDisplay().getSize(point);
                mScreenWidth = point.x;
                mScreenHeight = point.y;
            } else {
                mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
                mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();
            }
            rotation = mWindowManager.getDefaultDisplay().getRotation();
            int x = 0, y = 0;
            x =  mScreenWidth;
            y = mScreenHeight / 2;
            layoutParams = new WindowManager.LayoutParams(w, h, type, flags, PixelFormat.TRANSLUCENT);
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            layoutParams.x = x;
            layoutParams.y = y;
        } else {
            layoutParams.width = w;
            layoutParams.height = h;
        }

    }

}
