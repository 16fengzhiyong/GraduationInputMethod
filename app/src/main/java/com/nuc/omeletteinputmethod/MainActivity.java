package com.nuc.omeletteinputmethod;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private final int REQUEST_DIALOG_PERMISSION = 1010;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!canDrawOverlays(this)){
            Log.i("MainActivity", "onCreate: 现在状态是："+canDrawOverlays(this));
            requestSettingCanDrawOverlays();
        }

    }


    //判断是否拥有悬浮窗权限
    public boolean canDrawOverlays(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return Settings.canDrawOverlays(context);
            }else {
                return false;
            }
        } catch (NoSuchMethodError e) {
            return false;
        }
    }

    //申请悬浮窗权限
    private void requestSettingCanDrawOverlays() {
        Toast.makeText(MainActivity.this, "请打开显示悬浮窗开关!", Toast.LENGTH_LONG).show();
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.O) {//8.0以上
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, REQUEST_DIALOG_PERMISSION);
        } else if (sdkInt >= Build.VERSION_CODES.M) {//6.0-8.0
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_DIALOG_PERMISSION);
        } else {
            //4.4-6.0一下
            //无需处理了asdasd
        }
    }
}
