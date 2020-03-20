package com.nuc.omeletteinputmethod;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.nuc.omeletteinputmethod.floatwindow.FloatingImageDisplayService;

public class SettingsActivity extends Activity {
    private final int REQUEST_CODE = 0;
    public static boolean showMyselfkeyboard = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting);
        initView();
        getPermissions();
    }
    public void initView(){
        String[] arrayStrings = new String[]{"个人编写版","系统接口板"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,arrayStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        Spinner spinner = findViewById(R.id.switch_keyboard);
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
        spinner.setAdapter(adapter);

    }
    //使用数组形式操作
    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            if (arg2 == 0){
                showMyselfkeyboard = true;
            }else showMyselfkeyboard = false;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    public void getPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i("弹窗", "getPermissions: ");
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT);
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
            } else {
                Log.i("弹窗", "getPermissions: 准备开启 FloatingImageDisplayService");
                startService(new Intent(SettingsActivity.this, FloatingImageDisplayService.class));
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                    Log.i("弹窗", "onActivityResult: 准备开启 FloatingImageDisplayService");
                    startService(new Intent(SettingsActivity.this, FloatingImageDisplayService.class));
                }
            }
        }
    }
}
