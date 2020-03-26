package com.nuc.omeletteinputmethod;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nuc.omeletteinputmethod.DBoperation.CopyDB;
import com.nuc.omeletteinputmethod.entityclass.OneSinograEntity;
import com.nuc.omeletteinputmethod.floatwindow.FloatingImageDisplayService;
import com.nuc.omeletteinputmethod.kernel.util.SinogramLibrary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

public class SettingsActivity extends Activity {
    private Spinner spinner;
    private TextView json_textView;
    private final int REQUEST_CODE = 0;
    public static boolean showMyselfkeyboard = false;

    public static String dbName="myandroid.db";//数据库的名字
    private static String DATABASE_PATH="/data/data/com.nuc.omeletteinputmethod/databases/";//数据库在手机里的路径


    public static ArrayList<OneSinograEntity> getOneSinograEntityArrayList() {
        return oneSinograEntityArrayList;
    }

    private static ArrayList<OneSinograEntity> oneSinograEntityArrayList = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting);
        initView();
        //verifyStoragePermissions(this);
        getPermissions();
        setOneSinograEntityArrayList(SinogramLibrary.oneSinogramString);
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
        String[] arrayStrings = new String[]{"个人编写版","系统接口板"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,arrayStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        json_textView = findViewById(R.id.id_json_list);
        spinner = findViewById(R.id.switch_keyboard);
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
        spinner.setAdapter(adapter);
        json_textView.setText(SinogramLibrary.oneSinogramString);
    }
    private void setOneSinograEntityArrayList(String result){
        oneSinograEntityArrayList.clear();
        JsonParser parser = new JsonParser();  //创建JSON解析器
        JsonArray array = (JsonArray)parser.parse(result) ;
        for (int i = 0; i < array.size(); i++) {
            JsonArray array2 = array.get(i).getAsJsonArray();
            //Log.e("SettingsActivity", "setOneSinograEntityArrayList: array.size() "+array.size() );

            for (int j = 0; j < array2.size(); j++) {
                //Log.e("SettingsActivity", "setOneSinograEntityArrayList: array2.size() "+array2.size() );

                JsonObject subObject = array2.get(j).getAsJsonObject();
//                Log.i("SettingsActivity", "setOneSinograEntityArrayList: " + subObject.getAsString());
              //  Log.i("SettingsActivity", "setOneSinograEntityArrayList: " + subObject.entrySet());
                for (Map.Entry entry : subObject.entrySet()) {
                    Log.i("SettingsActivity", "entry.getKey():" + entry.getKey());
                    Log.i("SettingsActivity", "entry.getKey():" + entry.getValue().toString());
//                    entry.getKey();
//                    entry.getValue().toString();
                    oneSinograEntityArrayList.add(new OneSinograEntity(entry.getKey().toString(), entry.getValue().toString()));
                }

            }
        }
    }
    //先定义
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    //然后通过一个函数来申请
    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
