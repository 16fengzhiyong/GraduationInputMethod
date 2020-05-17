package com.nuc.omeletteinputmethod.floatwindow;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.nuc.omeletteinputmethod.DBoperation.DBManage;
import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.entityclass.AppInfomationEntity;
import com.nuc.omeletteinputmethod.floatwindow.notepad.Notepad;
import com.nuc.omeletteinputmethod.floatwindow.schedule.Schedule;
import com.nuc.omeletteinputmethod.floatwindow.shortcutinput.ShortcutInput;
import com.nuc.omeletteinputmethod.floatwindow.translate.Translate;
import com.nuc.omeletteinputmethod.floatwindow.view.FloatWindowLayout;
import com.nuc.omeletteinputmethod.floatwindow.view.PathMenu;
import com.nuc.omeletteinputmethod.floatwindow.view.StateMenu;
import com.nuc.omeletteinputmethod.floatwindow.view.niv.CircularImageView;

import java.util.ArrayList;
import java.util.List;



public class FloatingWindowDisplayService extends Service {
    public static boolean isStarted = false;

    private static DBManage dbManage = null;

    private String TAG = "fzy FloatingImageDisplayService";
    private static WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View displayView;
    private View zhankai;
    CircularImageView centerImage;

    private int[] images;
    private int imageIndex = 0;

    private Handler changeImageHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        windowManager = (WindowManager) this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        //windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showFloatingWindow();
        }
        if (dbManage == null){
            Log.i("OmeletteIME", "onCreate: new 数据库");
            dbManage = new DBManage(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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
            centerImage.setMaxWidth(120);
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setImageResource(images[imageIndex]);
            Log.i(TAG, "showFloatingWindow: windowManager.hashCode() "+windowManager.hashCode());
            windowManager.addView(displayView, layoutParams);
            Log.i(TAG, "showFloatingWindow: windowManager.hashCode() "+windowManager.hashCode());
            changeImageHandler.sendEmptyMessageDelayed(0, 2000);
        }
    }
    Notepad notepad;
    ShortcutInput shortcutInput ;
    Schedule schedule;
    Translate translate;
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
            }if (msg.what == StateMenu.SHORT_INPUT){
                hideAllWindow();
                shortcutInput = new ShortcutInput(FloatingWindowDisplayService.this,windowManager,
                        layoutParams,displayView);
                shortcutInput.removeView(zhankai);
                shortcutInput.showShortInput();
            }if (msg.what == StateMenu.NOTEPAD){
                hideAllWindow();
                notepad = new Notepad(FloatingWindowDisplayService.this,windowManager,
                        layoutParams,displayView);
                notepad.removeView(zhankai);
                notepad.showNotepad();
            }if (msg.what == StateMenu.SCHEDULE){
                hideAllWindow();
                schedule = new Schedule(FloatingWindowDisplayService.this,windowManager,
                        layoutParams,displayView);
                schedule.removeView(zhankai);
                schedule.showSchedule();
            }if (msg.what == StateMenu.TRANSLATE){
                hideAllWindow();

                translate = new Translate(FloatingWindowDisplayService.this,windowManager,
                        layoutParams,displayView);
                translate.removeView(zhankai);
                translate.showTranslate();
            }
            return false;

        }
    };

    public void hideAllWindow(){
        displayView.findViewById(R.id.id_float_notepad_partner).setVisibility(View.GONE);
        displayView.findViewById(R.id.id_float_schedule_parent).setVisibility(View.GONE);
        displayView.findViewById(R.id.id_float_shortinput_parent_LL).setVisibility(View.GONE);
        displayView.findViewById(R.id.id_float_translate_parent_LL).setVisibility(View.GONE);
        layoutParams.flags = 16777256;
    }


    public ArrayList<AppInfomationEntity> getItems(Context context) {
        PackageManager pckMan = context.getPackageManager();
        ArrayList<AppInfomationEntity> appInfomationEntities = new ArrayList<>();
//        ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
        int id = 0;
        List<PackageInfo> packageInfo = pckMan.getInstalledPackages(0);

        for (PackageInfo pInfo : packageInfo) {
            appInfomationEntities.add(new AppInfomationEntity(id++,pInfo.applicationInfo.loadLabel(pckMan).toString()
                    ,pInfo.packageName,pInfo.versionName,pInfo.applicationInfo.loadIcon(pckMan)));
        }
        return appInfomationEntities;
    }

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
        if (layoutParams.flags!= 16777256){
            layoutParams.flags = 16777256;
        }
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
        zhankai.findViewById(R.id.bar_image_center).setOnTouchListener(openIngOnTouchListener);
        zhankai.findViewById(R.id.bar_image_1).setOnTouchListener(openIngOnTouchListener);
        zhankai.findViewById(R.id.bar_image_2).setOnTouchListener(openIngOnTouchListener);
        zhankai.findViewById(R.id.bar_image_3).setOnTouchListener(openIngOnTouchListener);
        zhankai.findViewById(R.id.bar_image_4).setOnTouchListener(openIngOnTouchListener);
    }

    public void sendMessageToHandler(int p){
                changeImageHandler.sendEmptyMessage(p);
    }
    /**
     * 点击悬浮图后的事件
     */
    private void clickDo2(){

//        layoutParams.height = 120;
//        layoutParams.width = 120;
//        zhankai.setMinimumHeight(600);
//        zhankai.setMinimumWidth(600);

//        floatWindowLayout.switchState(true, PathMenu.CENTER ,this,StateMenu.CENTER);
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
                        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
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
                    layoutParams.flags = 16777256;
                    doByView(v);
                    break;
            }
            return false;
        }
        public void doByView(View view){
            switch (view.getId()){
                case R.id.bar_image_4:
                    Toast.makeText(FloatingWindowDisplayService.this,"你点击了记事本",
                            Toast.LENGTH_LONG).show();
                    floatWindowLayout.switchState(true, PathMenu.CENTER ,
                            FloatingWindowDisplayService.this,StateMenu.NOTEPAD);
                    layoutParams.height = 120;
                    layoutParams.width = 120;
                    centerImage.setImageResource(R.drawable.ic_notepad);
                    break;
                case R.id.bar_image_3:
                    Toast.makeText(FloatingWindowDisplayService.this,"你点击了快捷输入",
                            Toast.LENGTH_LONG).show();
                    floatWindowLayout.switchState(true, PathMenu.CENTER ,
                            FloatingWindowDisplayService.this,StateMenu.SHORT_INPUT);
                    layoutParams.height = 120;
                    layoutParams.width = 120;
                    centerImage.setImageResource(R.drawable.ic_shortcutinput);
                    break;
                case R.id.bar_image_2:

                    Toast.makeText(FloatingWindowDisplayService.this,"你点击了日程",
                            Toast.LENGTH_LONG).show();
                    floatWindowLayout.switchState(true, PathMenu.CENTER ,
                            FloatingWindowDisplayService.this,StateMenu.SCHEDULE);
                    layoutParams.height = 120;
                    layoutParams.width = 120;
                    centerImage.setImageResource(R.drawable.ic_schedule_press);
                break;

                case R.id.bar_image_1:

                    Toast.makeText(FloatingWindowDisplayService.this,"你点击了翻译",
                            Toast.LENGTH_LONG).show();
                    floatWindowLayout.switchState(true, PathMenu.CENTER ,
                            FloatingWindowDisplayService.this,StateMenu.TRANSLATE);
                    layoutParams.height = 120;
                    layoutParams.width = 120;
                    centerImage.setImageResource(R.drawable.ic_translate_press);
                    break;

                case R.id.bar_image_center:
                    Toast.makeText(FloatingWindowDisplayService.this,"你点击了中心",
                            Toast.LENGTH_LONG).show();
                    floatWindowLayout.switchState(true, PathMenu.CENTER ,
                            FloatingWindowDisplayService.this,StateMenu.CENTER);
                    layoutParams.height = 120;
                    layoutParams.width = 120;
                    centerImage.setImageResource(R.drawable.jiandan);
                    break;
                default:
//                    clickDo2();
                    break;
            }
        }
    }

    public static DBManage getDbManage() {
        return dbManage;
    }
}
