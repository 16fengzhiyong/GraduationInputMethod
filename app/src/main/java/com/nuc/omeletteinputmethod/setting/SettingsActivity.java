package com.nuc.omeletteinputmethod.setting;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.nuc.omeletteinputmethod.CCPCustomViewPager;
import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.adapters.MainViewPagerAdapter;
import com.nuc.omeletteinputmethod.floatwindow.FloatingImageDisplayService;
import com.nuc.omeletteinputmethod.floatwindow.schedule.Schedule;
import com.nuc.omeletteinputmethod.myframent.ScheduleFrament;
import com.nuc.omeletteinputmethod.myframent.ShortInputFrament;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class SettingsActivity extends FragmentActivity {
//    private Spinner spinner;
    private final int REQUEST_CODE = 0;
    public static boolean showMyselfkeyboard = false;


    private ArrayList<Fragment> fragmentList;

    public static String dbName="myandroid.db";//数据库的名字
    private static String DATABASE_PATH="/data/data/com.nuc.omeletteinputmethod/databases/";//数据库在手机里的路径


    CCPCustomViewPager vp;
    public MainViewPagerAdapter mMainViewPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting);
        initView();
        //verifyStoragePermissions(this);
        getPermissions();
        //判断数据库是否存在
        boolean dbExist = checkDataBase();
        if(dbExist){
            Log.d("复制数据库", "onCreate: 数据库存在");
        }else{//不存在就把raw里的数据库写入手机
            try{
                copyDataBase();
            }catch(IOException e){
                throw new Error("Error copying database");
            }
        }



    }
    /**
     * 判断数据库是否存在
     * @return false or true
     */
    public boolean checkDataBase(){
        SQLiteDatabase checkDB = null;
        try{
            String databaseFilename = DATABASE_PATH+dbName;
            checkDB =SQLiteDatabase.openDatabase(databaseFilename, null,
                    SQLiteDatabase.OPEN_READONLY);
        }catch(SQLiteException e){
            Log.e("复制数据库", "checkDataBase: ", e);
        }
        if(checkDB!=null){
            checkDB.close();
        }
        return checkDB !=null?true:false;
    }
    /**
     * 复制数据库到手机指定文件夹下
     * @throws IOException
     */
    public void copyDataBase() throws IOException{
        String databaseFilenames =DATABASE_PATH+dbName;
        File dir = new File(DATABASE_PATH);
        if(!dir.exists())//判断文件夹是否存在，不存在就新建一个
            dir.mkdir();
        FileOutputStream os = null;
        try{
            os = new FileOutputStream(databaseFilenames);//得到数据库文件的写入流
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        InputStream is = SettingsActivity.this.getResources().openRawResource(R.raw.myandroid);//得到数据库文件的数据流
        byte[] buffer = new byte[8192];
        int count = 0;
        try{

            while((count=is.read(buffer))>0){
                os.write(buffer, 0, count);
                os.flush();
            }
        }catch(IOException e){
            Log.e("复制数据库", "copyDataBase: ", e);
        }
        try{
            is.close();
            os.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void initView(){
        initFragments();
        vp = findViewById(R.id.vp);
        vp.setSlideEnabled(true);
        vp.setOffscreenPageLimit(fragmentList.size());  //更改：总页数
        mMainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(),
                fragmentList);
        vp.setAdapter(mMainViewPagerAdapter);
    }
    private void initFragments() {
        fragmentList = new ArrayList<Fragment>();
        ScheduleFrament homeFra = ScheduleFrament.newInstance();//消息
        fragmentList.add(homeFra);
        ShortInputFrament shortInputFrament = ShortInputFrament.newInstance();
        fragmentList.add(shortInputFrament);
//        ScheduleFrament dialFra = ScheduleFrament.newInstance();//通讯录
//        fragmentList.add(dialFra);
        // WorkbenchFragment workbenchFragment = WorkbenchFragment.newInstance();//工作台
        // fragmentList.add(workbenchFragment);
//        ScheduleFrament myFrament = ScheduleFrament.newInstance();//我的
//        fragmentList.add(myFrament);

    }
    public void getPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i("弹窗", "getPermissions: ");
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT);
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
            } else {
                Log.i("弹窗", "getPermissions: 准备开启 FloatingImageDisplayService");
                try {
                    startService(new Intent(SettingsActivity.this, FloatingImageDisplayService.class));

                }catch (Exception e){

                }
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
