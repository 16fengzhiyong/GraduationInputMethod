package com.nuc.omeletteinputmethod;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nuc.omeletteinputmethod.DBoperation.DBManage;
import com.nuc.omeletteinputmethod.adapters.SetingChangeShortInputAdapter;
import com.nuc.omeletteinputmethod.entityclass.FloatShortInputEntity;
import com.nuc.omeletteinputmethod.floatwindow.FloatingWindowDisplayService;
import com.nuc.omeletteinputmethod.floatwindow.view.niv.NiceImageView;
import com.nuc.omeletteinputmethod.util.MyItemTouchHelper;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ShortInputActivity extends Activity {
    RecyclerView mRecyclerView;
    NiceImageView niceImageView;
    TextView nameText;
    DBManage dbManage;
    EditText editText;


    Button takePutButton;
    private byte[] appIcon;
    private Bitmap icon;

    ImageView addImageView;
    String packageName;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_change_shortinput_layout);
        mRecyclerView = findViewById(R.id.id_setting_change_shortinput_list);
        niceImageView = findViewById(R.id.id_setting_change_shortinput_icon);
        nameText = findViewById(R.id.id_setting_change_shortinput_name);
        addImageView = findViewById(R.id.id_setting_change_add_imageview);
        takePutButton = findViewById(R.id.id_setting_change_add_ok_Button);
        editText = findViewById(R.id.id_setting_change_add_EditText);


        dbManage = FloatingWindowDisplayService.getDbManage();
        Intent intent = getIntent();
        appIcon = intent.getByteArrayExtra("appIcon");
        icon = BitmapFactory.decodeByteArray(appIcon,0,appIcon.length);
        niceImageView.setImageBitmap(icon);
        nameText.setText(intent.getStringExtra("appName"));
        packageName = intent.getStringExtra("packageName");
        if (dbManage!=null){
            mRecyclerView.setLayoutManager(new
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            ArrayList<FloatShortInputEntity> ls = dbManage.getDataByPackageName(packageName);
            if (ls != null){
                SetingChangeShortInputAdapter adapter =  new SetingChangeShortInputAdapter(ls, this);
                mRecyclerView.setAdapter(adapter);
                ItemTouchHelper helper = new ItemTouchHelper(new MyItemTouchHelper(adapter));
                helper.attachToRecyclerView(mRecyclerView);
            }else {

            }
        }


        addImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (findViewById(R.id.id_setting_change_add_RelativeLayout).getVisibility() == View.GONE) {
                    findViewById(R.id.id_setting_change_add_RelativeLayout).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.id_setting_change_add_RelativeLayout).setVisibility(View.GONE);
                }
               // InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            }
        });
        findViewById(R.id.id_setting_change_add_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.id_setting_change_add_RelativeLayout).setVisibility(View.GONE);
            }
        });
        takePutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put("packagename",packageName);
                values.put("info",editText.getText().toString());
                values.put("cishu",0);
                dbManage.getMyDBHelper().insert("shortinput", null, values);
                findViewById(R.id.id_setting_change_add_RelativeLayout).setVisibility(View.GONE);
                Toast.makeText(ShortInputActivity.this,"添加成功",Toast.LENGTH_SHORT).show();
                if (dbManage!=null){
                    ArrayList<FloatShortInputEntity> ls2 = dbManage.getDataByPackageName(packageName);
                    if (ls2 != null){
                        SetingChangeShortInputAdapter adapter =  new SetingChangeShortInputAdapter(ls2, ShortInputActivity.this);
                        mRecyclerView.setAdapter(adapter);
                    }else {

                    }
                }
            }
        });
//        ContentValues values = new ContentValues();
//        values.put("id","1");
//        values.put("name","the sy");
//        values.put("author","dan");
//        values.put("pages",454);
//        values.put("price",16.96);
//        db.insert("Book", null, values);

    }


    //获取默认输入法包名：
    private String getDefaultInputMethodPkgName(Context context) {
        String mDefaultInputMethodPkg = null;

        String mDefaultInputMethodCls = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
        //输入法类名信息
        Log.d("TAG", "mDefaultInputMethodCls=" + mDefaultInputMethodCls);
        if (!TextUtils.isEmpty(mDefaultInputMethodCls)) {
            //输入法包名
            mDefaultInputMethodPkg = mDefaultInputMethodCls.split("/")[0];
            Log.d("TAG", "mDefaultInputMethodPkg=" + mDefaultInputMethodPkg);
        }
        return mDefaultInputMethodPkg;
    }

}