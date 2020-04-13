package com.nuc.omeletteinputmethod;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nuc.omeletteinputmethod.setting.SettingsActivity;


public class MainActivity extends AppCompatActivity {
    private final int REQUEST_DIALOG_PERMISSION = 1010;
    private final String TAG = "MainActivity";
    private TextView tosetting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tosetting = findViewById(R.id.tosetting);
        tosetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        if (!canDrawOverlays(this)){
            if(AllLogShow.Debug)Log.i(TAG, "onCreate: 现在状态是："+canDrawOverlays(this));
            requestSettingCanDrawOverlays();
        }
        if (!getDefaultInputMethodPkgName(this).equals("com.nuc.omeletteinputmethod")){

            Toast.makeText(MainActivity.this, "请将输入法设置为默认输入法或首选输入法", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
        }

        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    //获取默认输入法包名：
    private String getDefaultInputMethodPkgName(Context context) {
        String mDefaultInputMethodPkg = null;

        String mDefaultInputMethodCls = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
        //输入法类名信息
        Log.d(TAG, "mDefaultInputMethodCls=" + mDefaultInputMethodCls);
        if (!TextUtils.isEmpty(mDefaultInputMethodCls)) {
            //输入法包名
            mDefaultInputMethodPkg = mDefaultInputMethodCls.split("/")[0];
            Log.d(TAG, "mDefaultInputMethodPkg=" + mDefaultInputMethodPkg);
        }
        return mDefaultInputMethodPkg;
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
