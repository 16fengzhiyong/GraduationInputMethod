package com.nuc.omeletteinputmethod.floatwindow;

import android.app.Service;
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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.floatwindow.view.FloatWindowLayout;
import com.nuc.omeletteinputmethod.floatwindow.view.PathMenu;
import com.nuc.omeletteinputmethod.floatwindow.view.StateMenu;
import com.nuc.omeletteinputmethod.floatwindow.view.niv.NiceImageView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dongzhong on 2018/5/30.
 */

public class FloatingImageDisplayService extends Service {
    public static boolean isStarted = false;

    private String TAG = "fzy FloatingImageDisplayService";
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View displayView;
    private View zhankai;
    NiceImageView centerImage;

    private int[] images;
    private int imageIndex = 0;

    private Handler changeImageHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 120;
        layoutParams.height = 120;
        layoutParams.x = 0;
        layoutParams.y = 300;


        images = new int[] {
                R.drawable.jiandan,
                R.drawable.jiandan,
                R.drawable.jiandan,
                R.drawable.jiandan,
                R.drawable.jiandan,
        };

        changeImageHandler = new Handler(this.getMainLooper(), changeImageCallback);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        zhankai = layoutInflater.inflate(R.layout.click_show_bar_enum, null);
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
    LayoutInflater layoutInflater ;
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            layoutInflater = LayoutInflater.from(this);
            displayView = layoutInflater.inflate(R.layout.image_display, null);
            displayView.setOnTouchListener(new FloatingOnTouchListener());
            centerImage = displayView.findViewById(R.id.image_display_imageview);
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setImageResource(images[imageIndex]);
            windowManager.addView(displayView, layoutParams);

            changeImageHandler.sendEmptyMessageDelayed(0, 2000);
        }
    }

    private Handler.Callback changeImageCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
//            if (msg.what == 0) {
//                imageIndex++;
//                if (imageIndex >= 5) {
//                    imageIndex = 0;
//                }
//                if (displayView != null) {
//                    ((ImageView) displayView.findViewById(R.id.image_display_imageview)).setImageResource(images[imageIndex]);
//                }
//                changeImageHandler.sendEmptyMessageDelayed(0, 2000);
//            }
            if (msg.what == 1){
                clickDo();
            }if (msg.what == 2){
                try {
                    windowManager.removeView(zhankai);
                    windowManager.addView(displayView,layoutParams);
                }catch (Exception e){

                }
            }if (msg.what == 3){
                try {
                    windowManager.removeView(zhankai);
                    windowManager.addView(displayView,layoutParams);
                    WindowManager.LayoutParams ls = layoutParams;
                    ls.x = ls.x + 120;
                    ls.y = ls.y + 10;
                    ls.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    ls.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    View testv = layoutInflater.inflate(R.layout.float_shortinput_layout, null);
                    windowManager.addView(testv,ls);
                    EditText editText = testv.findViewById(R.id.testinput);
                    editText.setShowSoftInputOnFocus(true);
                }catch (Exception e){

                }
            }
            return false;
        }
    };


    ImageView imageView1;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageView4;
    FloatWindowLayout floatWindowLayout;
    OpenIngOnTouchListener openIngOnTouchListener;
    /**
     * 点击悬浮图后的事件
     */
    private void clickDo(){
        openIngOnTouchListener = new OpenIngOnTouchListener();
        layoutParams.height = 350;
        layoutParams.width = 300;
//        zhankai.setMinimumHeight(600);
//        zhankai.setMinimumWidth(600);
        zhankai.setOnTouchListener(openIngOnTouchListener);
        try {
            windowManager.removeView(displayView);
            windowManager.addView(zhankai,layoutParams);
        }catch (Exception e){

        }
        floatWindowLayout = (FloatWindowLayout) zhankai.findViewById(R.id.item_layout);
        floatWindowLayout.switchState(true, PathMenu.CENTER,this, StateMenu.CENTER);

        zhankai.findViewById(R.id.bar_image_1);
        zhankai.findViewById(R.id.bar_image_2);
        zhankai.findViewById(R.id.bar_image_3).setOnTouchListener(openIngOnTouchListener);
        zhankai.findViewById(R.id.bar_image_4);

//

//        imageView1.measure(60,60);
//        floatWindowLayout.addView(imageView1);
//        floatWindowLayout.addView(imageView2);
//        floatWindowLayout.addView(imageView3);
//        floatWindowLayout.addView(imageView4);

//        TranslateAnimation translateAnimation1 = new TranslateAnimation(layoutParams.x,layoutParams.x+200,layoutParams.y,layoutParams.y);
//        translateAnimation1.setDuration(500);
//        translateAnimation1.setFillAfter(true);
//        translateAnimation1.setInterpolator(new AccelerateInterpolator());
//
//        TranslateAnimation translateAnimation2 = new TranslateAnimation(layoutParams.x,layoutParams.x+150,layoutParams.y,layoutParams.y+60);
//        translateAnimation2.setDuration(500);
//        translateAnimation2.setFillAfter(true);
//        translateAnimation2.setInterpolator(new AccelerateInterpolator());
//
//        TranslateAnimation translateAnimation3 = new TranslateAnimation(layoutParams.x,layoutParams.x+60,layoutParams.y,layoutParams.y+150);
//        translateAnimation3.setDuration(500);
//        translateAnimation3.setFillAfter(true);
//        translateAnimation3.setInterpolator(new AccelerateInterpolator());
//
//        TranslateAnimation translateAnimation4 = new TranslateAnimation(layoutParams.x,layoutParams.x+0,layoutParams.y,layoutParams.y+200);
//        translateAnimation4.setDuration(500);
//        translateAnimation4.setFillAfter(true);
//        translateAnimation4.setInterpolator(new AccelerateInterpolator());
//        translateAnimation4.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//
//                imageView4.clearAnimation();// 增加这句后，重新设置位置，物体才会移动正确
//                Log.d("fzy test","end Top:" + imageView4.getTop() + ",Y:" + imageView4.getY());
//
//                RelativeLayout.LayoutParams paramsTexas = new RelativeLayout.LayoutParams(80,80);
//                paramsTexas.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                paramsTexas.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                paramsTexas.setMargins(layoutParams.x,layoutParams.x+200,layoutParams.y,layoutParams.y+200);
//                imageView4.setLayoutParams(paramsTexas);
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//
//
//
//        imageView1.startAnimation(translateAnimation1);
//        imageView2.startAnimation(translateAnimation2);
//        imageView3.startAnimation(translateAnimation3);
//
////        imageView4.setAnimation(translateAnimation4);
//        imageView4.startAnimation(translateAnimation4);
////        ViewGroup vg =(ViewGroup)displayView.getParent();
////        if(vg!= null){
////            vg.removeAllViews();

    }

    public void sendMessageToHandler(int p){
                changeImageHandler.sendEmptyMessage(p);
    }
    /**
     * 点击悬浮图后的事件
     */
    private void clickDo2(){

        layoutParams.height = 120;
        layoutParams.width = 120;
//        zhankai.setMinimumHeight(600);
//        zhankai.setMinimumWidth(600);

        floatWindowLayout.switchState(true, PathMenu.CENTER ,this,StateMenu.CENTER);
//        changeImageHandler.sendEmptyMessageAtTime(2,30000);

    }




    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;
        private boolean isClick = false;
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isClick = true;
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:

                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    if (movedX > 1||movedY>1){
                        isClick = false;
                    }
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    if (layoutParams.x<120){
                        layoutParams.width=layoutParams.x;
                        if (layoutParams.x<40){
                            layoutParams.width = 40;
                        }
                    }else if (layoutParams.width < 120){
                        layoutParams.width = 120;
                    }
                    break;
                default:
                    if (isClick){
                        isClick = false;
//                        clickDo();
                        changeImageHandler.sendEmptyMessage(1);
                    }
                    break;
            }
            return false;
        }
    }


    private class OpenIngOnTouchListener implements View.OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    doByView(v);
                    break;
            }
            return false;
        }
        public void doByView(View view){
            switch (view.getId()){
                case R.id.bar_image_3:
                    Toast.makeText(FloatingImageDisplayService.this,"你点击了快捷输入",
                            Toast.LENGTH_LONG).show();
                    floatWindowLayout.switchState(true, PathMenu.CENTER ,
                            FloatingImageDisplayService.this,StateMenu.SHORT_INPUT);
                    layoutParams.height = 120;
                    layoutParams.width = 120;
                    centerImage.setImageResource(R.drawable.ic_shortcutinput);
                    break;
                default:
//                    clickDo2();
                    break;
            }
        }
    }
}
