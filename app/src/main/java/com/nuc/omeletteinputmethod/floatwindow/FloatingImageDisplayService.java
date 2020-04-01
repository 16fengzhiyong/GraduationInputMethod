package com.nuc.omeletteinputmethod.floatwindow;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import com.nuc.omeletteinputmethod.floatwindow.util.ArcTipViewController;

/**
 * Created by dongzhong on 2018/5/30.
 */

public class FloatingImageDisplayService extends Service {
    private static FloatingImageDisplayService instance;

    public static boolean isStarted = false;

    private View displayView;

    private int[] images;
    private int imageIndex = 0;

    //private Handler changeImageHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        isStarted = true;
        ArcTipViewController.getInstance();
    //    changeImageHandler = new Handler(this.getMainLooper(), changeImageCallback);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        }
        return super.onStartCommand(intent, flags, startId);
    }




    public static FloatingImageDisplayService getInstance() {
        return instance;
    }
}
