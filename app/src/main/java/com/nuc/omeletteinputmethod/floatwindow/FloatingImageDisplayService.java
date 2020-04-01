package com.nuc.omeletteinputmethod.floatwindow;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.floatwindow.util.ArcTipViewController;
import com.nuc.omeletteinputmethod.floatwindow.view.DisplayFloatWindow;

/**
 * Created by dongzhong on 2018/5/30.
 */

public class FloatingImageDisplayService extends Service {
    private static FloatingImageDisplayService instance;

    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View displayView;

    private int[] images;
    private int imageIndex = 0;

    //private Handler changeImageHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        isStarted = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
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

        images = new int[] {
                R.drawable.ic_notepad,
                R.drawable.ic_schedule,
                R.drawable.ic_shortcutinput,
                R.drawable.ic_translate,
        };

    //    changeImageHandler = new Handler(this.getMainLooper(), changeImageCallback);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showFloatingWindow();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private View acrFloatView;
    private DisplayFloatWindow displayFloatWindow;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);



       //     acrFloatView = (RelativeLayout) layoutInflater.inflate(R.layout.arc_view_float, null);

      //      displayFloatWindow = (DisplayFloatWindow) acrFloatView.findViewById(R.id.arc_menu);


            acrFloatView = ArcTipViewController.getInstance().getAcrFloatView();
            displayFloatWindow = ArcTipViewController.getInstance().getDisplayFloatWindow();
            acrFloatView.setOnTouchListener(new FloatingOnTouchListener());
            displayFloatWindow.setOnTouchListener(new FloatingOnTouchListener());
            ArcTipViewController.getInstance().initArcMenu(displayFloatWindow,images);
            //displayFloatWindow.setOnTouchListener(new FloatingOnTouchListener());
//            displayView = layoutInflater.inflate(R.layout.image_display, null);
//            displayView.setOnTouchListener(new FloatingOnTouchListener());
//            ImageView imageView = displayView.findViewById(R.id.image_display_imageview);
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setImageResource(images[imageIndex]);


            windowManager.addView(acrFloatView, layoutParams);



//            changeImageHandler.sendEmptyMessageDelayed(0, 2000);
        }
    }

//    private Handler.Callback changeImageCallback = new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            if (msg.what == 0) {
//                imageIndex++;
//                if (imageIndex >= 5) {
//                    imageIndex = 0;
//                }
//                if (displayView != null) {
//                    ((ImageView) displayView.findViewById(R.id.image_display_imageview)).setImageResource(images[imageIndex]);
//                }
////                changeImageHandler.sendEmptyMessageDelayed(0, 2000);
//            }
//            return false;
//        }
//    };
private static Animation createHintSwitchAnimation(final boolean expanded) {
    Animation animation = new RotateAnimation(expanded ? 45 : 0, expanded ? 0 : 45, Animation.RELATIVE_TO_SELF,
            0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    animation.setStartOffset(0);
    animation.setDuration(100);
    animation.setInterpolator(new DecelerateInterpolator());
    animation.setFillAfter(true);

    return animation;
}
    private class FloatingOnTouchListener implements View.OnTouchListener {
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
                    windowManager.updateViewLayout(view, layoutParams);
//                    if (layoutParams.x>120){
//                        layoutParams.width = 120;
//                    }else layoutParams.width = 60;
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i("悬浮窗事件", "onTouch: ACTION_UP actionDown :" + actionDown);
                    if (actionDown){
                        displayFloatWindow.getmHintView().startAnimation(createHintSwitchAnimation(displayFloatWindow.getmArcLayout().isExpanded()));
                        displayFloatWindow.getmHintView().setVisibility(View.GONE);

                        displayFloatWindow.getmArcLayout().setVisibility(View.VISIBLE);
                        displayFloatWindow.getmArcLayout().switchState(true,60);
                        displayFloatWindow.getmArcLayout().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                            }
                        },350);
                        actionDown = false;
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    public static FloatingImageDisplayService getInstance() {
        return instance;
    }
}
