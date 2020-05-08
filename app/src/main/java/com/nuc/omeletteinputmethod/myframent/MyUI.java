package com.nuc.omeletteinputmethod.myframent;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nuc.omeletteinputmethod.MainActivity;
import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.entityclass.FloatShortInputEntity;
import com.nuc.omeletteinputmethod.entityclass.NotepadEntity;
import com.nuc.omeletteinputmethod.entityclass.ScheduleEntity;
import com.nuc.omeletteinputmethod.floatwindow.FloatingWindowDisplayService;
import com.nuc.omeletteinputmethod.floatwindow.notepad.Notepad;
import com.nuc.omeletteinputmethod.login.LoginActivity;
import com.nuc.omeletteinputmethod.login.SharedPreferencesUtils;
import com.nuc.omeletteinputmethod.network.HttpUrls;
import com.nuc.omeletteinputmethod.util.TitleBar;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Utf8;

public class MyUI extends LazyFrament{
    private TextView tv_my_name;
    private TextView tv_my_phone;
    private ImageView imageView;
    private TextView tv_my_local;
    private TitleBar titleBar;
    private TextView allShedule;
    private RelativeLayout selfInfo;

    private TextView tv_my_tongbu;

    private TextView tv_my_download;
    static MyUI fragment;


    private android.content.SharedPreferences pref;
    private android.content.SharedPreferences.Editor editor;

    @Override
    public void fetchData() {
    }

    @Override
    protected int getLayoutId() {
        return R.layout.myinformation;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        pref = android.preference.PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = pref.edit();
        tv_my_name = rootView.findViewById(R.id.tv_my_name);
        tv_my_phone = rootView.findViewById(R.id.tv_my_phone);
        tv_my_tongbu = rootView.findViewById(R.id.tv_my_tongbu);
        selfInfo = rootView.findViewById(R.id.self_info);
        //pieChart.setDataList(mPieDataList);

        titleBar = rootView.findViewById(R.id.title_bar);
        titleBar.setMyCenterTitle("个人信息");
        imageView=rootView.findViewById(R.id.iv_my_header);
        tv_my_local=rootView.findViewById(R.id.tv_my_local);
        allShedule = rootView.findViewById(R.id.tv_my_all_shedule);
        //tv_my_name .setText("新用户");
        tv_my_phone.setText("18406587399");
        tv_my_download = rootView.findViewById(R.id.tv_my_download);
        initLocalData();
        initClick();


    }

    public void initClick(){
        selfInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (tv_my_name.getText().toString().equals("未登录")){
                   Intent intent = new Intent(getContext(), LoginActivity.class);
                   startActivity(intent);
               }
            }
        });
        tv_my_tongbu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ScheduleEntity> upScheduleEntityArrayList =
                        FloatingWindowDisplayService.getDbManage().getAllSchedule();
                ArrayList<FloatShortInputEntity> upFloatShortInputEntityArrayList =
                        FloatingWindowDisplayService.getDbManage().getAllFloatShortInput();
                ArrayList<NotepadEntity> upNotepadEntityArrayList =
                        FloatingWindowDisplayService.getDbManage().getNotepad();

                //insertShortInput String usernumber ,String packagename ,String info,String cishu
                //insertSchedule String usernumber ,String time ,String info
                //insertNotepad String usernumber ,String text

                //postForUploadDelete("","","");
                int allOfData = upFloatShortInputEntityArrayList.size()+upScheduleEntityArrayList.size()+upNotepadEntityArrayList.size();
                for(FloatShortInputEntity floatShortInputEntity:upFloatShortInputEntityArrayList){
                    postForUploadShortInput(HttpUrls.URL+"insertShortInput.do",
                            "18406587399",floatShortInputEntity.getPackageName(),floatShortInputEntity.getTag()
                    ,"0");
                }
                for (ScheduleEntity scheduleEntity:upScheduleEntityArrayList){
                    postForUploadSchedule(HttpUrls.URL+"insertSchedule.do","18406587399",
                            scheduleEntity.getTime(),scheduleEntity.getInfo());
                }
               for (NotepadEntity notepadEntity:upNotepadEntityArrayList){
                   postForUploadNotepad(HttpUrls.URL+"insertSchedule.do","18406587399",
                           notepadEntity.getText());
               }
            }
        });
        tv_my_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        allShedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ScheduleEntity> upScheduleEntityArrayList = FloatingWindowDisplayService.getDbManage().getAllSchedule();

            }
        });
    }


    public void postForUploadDelete(String url1, String usernumber ,String time){
        Log.i("上传数据", "postForUploadShortInput: 准备发送 usernumber ="+usernumber +"time ="+time);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = builder.build();
        FormBody.Builder formbuilder = new FormBody.Builder();
        formbuilder.add("usernumber", usernumber);
        formbuilder.add("time", time);
        FormBody body = formbuilder.build();
        Request request = new Request.Builder()
                .post(body)
                .url(url1)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String callback  = response.body().string();
                Log.d("上传数据", "postForUploadShortInput response:"+ callback);
            }
        });
    }

    public void postForUploadShortInput(String url1, String usernumber ,String packagename ,String info,String cishu){
        Log.i("上传数据", "postForUploadShortInput: 准备发送 usernumber ="+usernumber +"packagename ="+packagename
        +"info"+info+"cishu"+cishu);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = builder.build();
        FormBody.Builder formbuilder = new FormBody.Builder();
        formbuilder.add("usernumber", usernumber);
        formbuilder.add("packagename", packagename);
        formbuilder.add("info", info);
        formbuilder.add("cishu", cishu);

        FormBody body = formbuilder.build();
        Request request = new Request.Builder()
                .post(body)
                .url(url1)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String callback  = response.body().string();
                Log.d("上传数据", "postForUploadShortInput response:"+ callback);
            }
        });
    }
    public void postForUploadSchedule(String url1, String usernumber ,String time ,String info){
        Log.i("上传数据", "postForUploadShedule postForLogin: 准备发送 usernumber ="+usernumber +"time ="+time+"info"+info);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = builder.build();
        FormBody.Builder formbuilder = new FormBody.Builder();
        formbuilder.add("usernumber", usernumber);
        formbuilder.add("time", time);
        formbuilder.add("info", info);
        FormBody body = formbuilder.build();
        Request request = new Request.Builder()
                .post(body)
                .url(url1)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String callback  = response.body().string();
                Log.d("上传数据", "postForUploadShedule response:"+ callback);

            }
        });
    }
    public void postForUploadNotepad(String url1, String usernumber ,String text){
        Log.i("上传数据", "postForUploadNotepad: 准备发送 usernumber ="+usernumber +"text ="+text);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = builder.build();
        FormBody.Builder formbuilder = new FormBody.Builder();
        formbuilder.add("username", usernumber);
        formbuilder.add("password", text);
        FormBody body = formbuilder.build();
        Request request = new Request.Builder()
                .post(body)
                .url(url1)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String callback  = response.body().string();
                Log.d("上传数据", "postForUploadNotepad response:"+ callback);
            }
        });
    }
    public void initLocalData(){
        SharedPreferencesUtils helper = new SharedPreferencesUtils(getContext(), "setting");
        String name = helper.getString("name");
        if (name == null){
            tv_my_name.setText("未登录");
        }else tv_my_name.setText(name);

    }



    @Override
    protected void initWidgetActions() {
    }

    public static MyUI newInstance() {
        if (fragment == null){
            fragment= new MyUI();
        }
        return fragment;
    }

    protected float pxTodp(float value){
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float valueDP= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,value,metrics);
        return valueDP;
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {

        super.onResume();
    }

}
