package com.nuc.omeletteinputmethod.setting;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.nuc.omeletteinputmethod.CCPCustomViewPager;
import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.adapters.MainViewPagerAdapter;
import com.nuc.omeletteinputmethod.floatwindow.FloatingWindowDisplayService;
import com.nuc.omeletteinputmethod.myframent.MyUI;
import com.nuc.omeletteinputmethod.myframent.NotepadFrament;
import com.nuc.omeletteinputmethod.myframent.ScheduleFrament;
import com.nuc.omeletteinputmethod.myframent.ShortInputFrament;
import com.nuc.omeletteinputmethod.util.SymbolsManager;
import com.nuc.omeletteinputmethod.util.TitleBar;

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
    TitleBar titleBar;

    ImageView tabScheduleIV ;
    ImageView tabShortinputIV ;
    ImageView tabNotepadIV ;
    ImageView tabMyIV ;
    TextView tabScheduleTV ;
    TextView tabShortinputTV ;
    TextView tabNotepadTV ;
    TextView tabMyTV ;
//    id_tab_schedule_iv




    private ArrayList<Fragment> fragmentList;
    public static String dbName="myandroid.db";//数据库的名字
    private static String DATABASE_PATH="/data/data/com.nuc.omeletteinputmethod/databases/";//数据库在手机里的路径

    int index = 0;

    CCPCustomViewPager vp;
    public MainViewPagerAdapter mMainViewPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting);
        titleBar = findViewById(R.id.setting_title_bar);
//        titleBar.setTitle("我的日程");
//        titleBar.setMyCenterTitle("teas");

        Log.i("字符信息", "onCreate: "+new SymbolsManager(this).toString());
        initTooleBar(titleBar,true,"我的日程");
        initView();
        //verifyStoragePermissions(this);
        getPermissions();
//        判断数据库是否存在
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
        Log.i("app启动运行：", "onCreate: SettingActivity 72");



    }
    public void initTooleBar( TitleBar mTitleBar,boolean showBack, String title) {
        if (mTitleBar == null) {
            return;
        }
        //mTitleBar.setTitleMargin();
        if (showBack) {
            mTitleBar.setNavigationIcon(R.drawable.setting_back2);
            mTitleBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        } else {
            mTitleBar.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(title)) {
            mTitleBar.setVisibility(View.GONE);
        } else {
            mTitleBar.setVisibility(View.VISIBLE);
            mTitleBar.setMyCenterTitle(title);
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
    View.OnClickListener onClickListener =  new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.id_tab_schedule_FL:
                    index = 0;
                    tabScheduleIV.setSelected(true);
                    tabScheduleTV.setTextColor(Color.GREEN);
                    vp.setCurrentItem(index, true);
                    break;
                case R.id.id_tab_shortinput_FL:
                    index = 1;
                    tabShortinputIV.setSelected(true);
                    tabShortinputTV.setTextColor(Color.GREEN);
                    vp.setCurrentItem(index, true);
                    break;
                case R.id.id_tab_notepad_FL:
                    index = 2;
                    tabNotepadIV.setSelected(true);
                    tabNotepadTV.setTextColor(Color.GREEN);
                    vp.setCurrentItem(index, true);
                    break;
                case R.id.id_tab_my_FL:
                    index = 3;
                    tabMyIV.setSelected(true);
                    tabMyTV.setTextColor(Color.GREEN);
                    vp.setCurrentItem(index, true);
                    break;

            }
        }
    };
    private void reSetButton() {
        tabScheduleIV.setSelected(false);
        tabShortinputIV.setSelected(false);
        tabNotepadIV.setSelected(false);
        tabMyIV.setSelected(false);
        tabScheduleTV.setTextColor(Color.BLACK);
        tabShortinputTV.setTextColor(Color.BLACK);
        tabNotepadTV.setTextColor(Color.BLACK);
        tabMyTV.setTextColor(Color.BLACK);
    }
    protected void initWidgetAciotns(){

        vp.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                reSetButton();
                switch (position) {
                    case 0:
                        tabScheduleIV.setSelected(true);
                        tabScheduleTV.setTextColor(Color.GREEN);
                        break;
                    case 1:
                        tabShortinputIV.setSelected(true);
                        tabShortinputTV.setTextColor(Color.GREEN);
                        break;
                    case 2:
                        tabNotepadIV.setSelected(true);
                        tabNotepadTV.setTextColor(Color.GREEN);
                        break;
                    case 3:
                        tabMyIV.setSelected(true);
                        tabMyTV.setTextColor(Color.GREEN);
                        break;
                }
            }
        });

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
        tabScheduleIV = findViewById(R.id.id_tab_schedule_iv);
        tabScheduleTV = findViewById(R.id.id_tab_schedule_tv);
        tabShortinputIV = findViewById(R.id.id_tab_shortinput_iv);
        tabShortinputTV = findViewById(R.id.id_tab_shortinput_tv);
        tabNotepadIV = findViewById(R.id.id_tab_notepad_iv);
        tabNotepadTV = findViewById(R.id.id_tab_notepad_tv);
        tabMyIV = findViewById(R.id.id_tab_my_iv);
        tabMyTV = findViewById(R.id.id_tab_my_tv);
        findViewById(R.id.id_tab_schedule_FL).setOnClickListener(onClickListener);
        findViewById(R.id.id_tab_shortinput_FL) .setOnClickListener(onClickListener);
        findViewById(R.id.id_tab_notepad_FL) .setOnClickListener(onClickListener);
        findViewById(R.id.id_tab_my_FL) .setOnClickListener(onClickListener);

        
        
        
        initWidgetAciotns();
    }
    private void initFragments() {
        fragmentList = new ArrayList<Fragment>();
        ScheduleFrament homeFra = ScheduleFrament.newInstance();//消息
        fragmentList.add(homeFra);
        ShortInputFrament shortInputFrament = ShortInputFrament.newInstance();
        fragmentList.add(shortInputFrament);
        NotepadFrament notepadFrament = NotepadFrament.newInstance();
        fragmentList.add(notepadFrament);
        MyUI myUI = MyUI.newInstance();
        fragmentList.add(myUI);
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
                    startService(new Intent(SettingsActivity.this, FloatingWindowDisplayService.class));

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
                    startService(new Intent(SettingsActivity.this, FloatingWindowDisplayService.class));
                }
            }
        }
    }
}
