package com.nuc.omeletteinputmethod.myframent;


import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nuc.omeletteinputmethod.MainActivity;
import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.adapters.FirstViewAdapter;
import com.nuc.omeletteinputmethod.adapters.MyChoseBackroundAdapter;
import com.nuc.omeletteinputmethod.entityclass.FloatShortInputEntity;
import com.nuc.omeletteinputmethod.entityclass.NotepadEntity;
import com.nuc.omeletteinputmethod.entityclass.ScheduleEntity;
import com.nuc.omeletteinputmethod.floatwindow.FloatingWindowDisplayService;
import com.nuc.omeletteinputmethod.floatwindow.notepad.Notepad;
import com.nuc.omeletteinputmethod.floatwindow.schedule.Schedule;
import com.nuc.omeletteinputmethod.login.LoginActivity;
import com.nuc.omeletteinputmethod.login.SharedPreferencesUtils;
import com.nuc.omeletteinputmethod.network.HttpUrls;
import com.nuc.omeletteinputmethod.util.TitleBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    private TextView allShedule;
    private RelativeLayout selfInfo;

    private TextView tv_my_tongbu;

    private TextView tv_my_download;
    static MyUI fragment;
    private Switch aSwitch;

    RecyclerView choseBackroundList;

    ArrayList<Integer> sources = new ArrayList<>(); ;

    SharedPreferencesUtils helper = null;

    Spinner spinner;
    Button button;

    List<String> getBackTime = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    int selescTime = 0;

    String usernumber = null;

    @Override
    public void fetchData() {
    }

    @Override
    protected int getLayoutId() {
        return R.layout.myinformation;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {

        sources.add(R.drawable.ic_keyboard_backround);
        sources.add(R.drawable.ic_keyboard_backround_2);
        sources.add(R.drawable.ic_keyboard_backround_3);
        sources.add(R.drawable.ic_keyboard_backround_4);
        sources.add(R.drawable.ic_keyboard_backround_5);
        sources.add(R.drawable.ic_keyboard_backround_6);
        sources.add(R.drawable.ic_keyboard_backround_7);
        sources.add(R.drawable.ic_keyboard_backround_8);
        sources.add(R.drawable.ic_keyboard_backround_9);

        helper = new SharedPreferencesUtils(getContext(), "setting");
        tv_my_name = rootView.findViewById(R.id.tv_my_name);
        tv_my_phone = rootView.findViewById(R.id.tv_my_phone);
        tv_my_tongbu = rootView.findViewById(R.id.tv_my_tongbu);
        selfInfo = rootView.findViewById(R.id.self_info);
        //pieChart.setDataList(mPieDataList);

        imageView=rootView.findViewById(R.id.iv_my_header);
        tv_my_local=rootView.findViewById(R.id.tv_my_local);
        allShedule = rootView.findViewById(R.id.tv_my_all_shedule);
        //tv_my_name .setText("新用户");
        tv_my_phone.setText("18406587399");
        tv_my_download = rootView.findViewById(R.id.tv_my_download);
        aSwitch = rootView.findViewById(R.id.switch1);
        choseBackroundList = rootView.findViewById(R.id.my_chose_backround_list);

        spinner = rootView.findViewById(R.id.tv_my_download_spinner);
        button = rootView.findViewById(R.id.tv_my_download_chose);





        //aSwitch.isChecked();
        aSwitch.setChecked(helper.getBoolean("useownimg",false));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        choseBackroundList.setLayoutManager(layoutManager);
        initLocalData();
        initClick();
    }

    public void initClick(){



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (helper.getString("usernumber") != null) {
                    Log.i("显示当前选择", "onClick: = " + getBackTime.get(selescTime));
                    getAlldataByBftime(HttpUrls.URL + "getAlldataByBftime.do", helper.getString("usernumber"), getBackTime.get(selescTime));
                }
            }
        });
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true){
                    try {
                        Log.i("个人用户选择", "onCheckedChanged: "+isChecked);

                        choseBackroundList.setAdapter(new MyChoseBackroundAdapter(sources,getContext(),choseBackroundList));
                        choseBackroundList.setNestedScrollingEnabled(false);
                        choseBackroundList.setVisibility(View.VISIBLE);
                        Log.i("个人用户选择", "onCheckedChanged: end");
                    }catch (Exception e){
                        Log.e("个人用户选择", "onCheckedChanged: ",e);
                    }

                }else {
                    Log.i("个人用户选择", "onCheckedChanged: "+isChecked);
                    choseBackroundList.setVisibility(View.GONE);
                }
                helper.putValues(new SharedPreferencesUtils.ContentValue("useownimg", aSwitch.isChecked()));
            }
        });

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
                if (helper.getString("usernumber") != null) {
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
                    int allOfData = upFloatShortInputEntityArrayList.size() + upScheduleEntityArrayList.size() + upNotepadEntityArrayList.size();
                    for (FloatShortInputEntity floatShortInputEntity : upFloatShortInputEntityArrayList) {
                        postForUploadShortInput(HttpUrls.URL + "insertShortInput.do",
                                helper.getString("usernumber"), floatShortInputEntity.getPackageName(), floatShortInputEntity.getTag()
                                , "0");
                    }
                    for (ScheduleEntity scheduleEntity : upScheduleEntityArrayList) {
                        postForUploadSchedule(HttpUrls.URL + "insertSchedule.do", helper.getString("usernumber"),
                                scheduleEntity.getTime(), scheduleEntity.getInfo());
                    }
                    for (NotepadEntity notepadEntity : upNotepadEntityArrayList) {
                        postForUploadNotepad(HttpUrls.URL + "insertNotepad.do", helper.getString("usernumber"),
                                notepadEntity.getText());
                    }
                }
            }
        });
        tv_my_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (helper.getString("usernumber")!= null){
                postForAllTime(HttpUrls.URL+"getTheNumberfortime.do",
                        helper.getString("usernumber"));
                /*
                postForDownloadShortInput(HttpUrls.URL+"insertShortInput.do",
                        "18406587399",floatShortInputEntity.getPackageName(),floatShortInputEntity.getTag()
                        ,"0");
                postForDownloadSchedule(HttpUrls.URL+"insertSchedule.do","18406587399",
                        scheduleEntity.getTime(),scheduleEntity.getInfo());
                postForDownloadNotepad(HttpUrls.URL+"insertSchedule.do","18406587399",
                        notepadEntity.getText());
                 */
                }
            }
        });
        allShedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ScheduleEntity> upScheduleEntityArrayList = FloatingWindowDisplayService.getDbManage().getAllSchedule();

            }
        });
    }


    public void getAlldataByBftime(String url1, String usernumber,String beifenshijian){
        Log.i("获取备份by时间", "postForAllTimr postForLogin: 准备发送 usernumber ="+usernumber);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = builder.build();
        FormBody.Builder formbuilder = new FormBody.Builder();
        formbuilder.add("usernumber", usernumber);
        formbuilder.add("beifenshijian", beifenshijian);
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
                Log.d("获取备份by时间", "getAlldataByBftime response:"+ callback);
                resetDatabase(callback);
                /*
                {"shortInput":[
                {"beifenshijian":"2020-05-09","id":157,"info":"test1","time":"com.nuc.omeletteinputmethod","usernumber":"18406587399"}],
                "schedule":[{"beifenshijian":"2020-05-09","id":10,"info":"嘲讽","time":"2020-5-9","usernumber":"18406587399"},
                {"beifenshijian":"2020-05-09","id":11,"info":"嘲讽","time":"2020-5-9","usernumber":"18406587399"}],
                "notepad":[{"beifenshijian":"2020-05-09","id":3,"text":"adsa","usernumber":"18406587399"},{"beifenshijian":"2020-05-09","id":4,"text":"","usernumber":"18406587399"},{"beifenshijian":"2020-05-09","id":5,"text":"awe","usernumber":"18406587399"}]}
                 */
            }
        });
    }
    public void postForAllTime(String url1, String usernumber){
        Log.i("获取数据", "postForAllTimr postForLogin: 准备发送 usernumber ="+usernumber);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = builder.build();
        FormBody.Builder formbuilder = new FormBody.Builder();
        formbuilder.add("usernumber", usernumber);
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
                Log.d("获取数据", "postForAllTime response:"+ callback);
                //{"data":["18406587399","2020-05-08","2020-05-09","18406587399"]}
                jiexie(callback);
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
        formbuilder.add("usernumber", usernumber);
        formbuilder.add("text", text);
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
         helper = new SharedPreferencesUtils(getContext(), "setting");
        if (helper.getString("usernumber")== null){
            tv_my_name.setText("未登录");
        }else tv_my_name.setText(helper.getString("usernumber"));

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
    //解析返回数据
    public void jiexie(String result){
        getBackTime.clear();
        JsonParser parser = new JsonParser();  //创建JSON解析器
        JsonObject jsonObject = (JsonObject)parser.parse(result) ;
        JsonArray array = jsonObject.get("data").getAsJsonArray();
        Log.d("课设:主页面解析", "array.size(): "+array.size());
        for (int i = 0; i < array.size(); i++) {
            getBackTime.add(array.get(i).getAsString());
        }
        Message message = new Message();
        message.what = 0 ;
        message.obj = "子线程发送的消息Hi~Hi";
        mHandler .sendMessage(message);
    }

    //解析返回数据
    public void resetDatabase(String result){
        JsonParser parser = new JsonParser();  //创建JSON解析器
        JsonObject jsonObject = (JsonObject)parser.parse(result) ;
        JsonArray shortInputArray = jsonObject.get("shortInput").getAsJsonArray();
        JsonArray scheduleArray = jsonObject.get("schedule").getAsJsonArray();
        JsonArray notepadnArray = jsonObject.get("notepad").getAsJsonArray();
        FloatingWindowDisplayService.getDbManage().getMyDBHelper().delete("shortinput","",new String[]{});
        FloatingWindowDisplayService.getDbManage().getMyDBHelper().delete("schedule","",new String[]{});
        FloatingWindowDisplayService.getDbManage().getMyDBHelper().delete("notepad","",new String[]{});

        for (int i = 0; i < shortInputArray.size(); i++) {
            JsonObject shortInputJsonObject = shortInputArray.get(i).getAsJsonObject();
            ContentValues values = new ContentValues();
            values.put("packagename",shortInputJsonObject.get("packagename").getAsString());
            values.put("info",shortInputJsonObject.get("text").getAsString());
            values.put("cishu",shortInputJsonObject.get("cishu").getAsString());

            FloatingWindowDisplayService.getDbManage().getMyDBHelper().insert("shortinput", null, values);
        }
        for (int i = 0; i < scheduleArray.size(); i++) {
            JsonObject scheduleJsonObject = scheduleArray.get(i).getAsJsonObject();
            ContentValues values = new ContentValues();
            //{"beifenshijian":"2020-05-09","id":10,"info":"嘲讽","time":"2020-5-9","usernumber":"18406587399"}
            values.put("time",scheduleJsonObject.get("time").getAsString());
            values.put("info",scheduleJsonObject.get("info").getAsString());
            FloatingWindowDisplayService.getDbManage().getMyDBHelper().insert("schedule", null, values);
        }
        for (int i = 0; i < notepadnArray.size(); i++) {
            JsonObject notepadnJsonObject = notepadnArray.get(i).getAsJsonObject();
            ContentValues values = new ContentValues();
            values.put("text",notepadnJsonObject.get("text").getAsString());
            FloatingWindowDisplayService.getDbManage().getMyDBHelper().insert("notepad", null, values);
        }


        Message message = new Message();
        message.what = 1 ;
        message.obj = "子线程发送的消息Hi~Hi";
        mHandler .sendMessage(message);
    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    rootView.findViewById(R.id.tv_my_download_chose_LL).setVisibility(View.VISIBLE);
                    arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getBackTime);
                    //第三步：设置下拉列表下拉时的菜单样式
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //第四步：将适配器添加到下拉列表上
                    spinner.setAdapter(arrayAdapter);
                    //第五步：添加监听器，为下拉列表设置事件的响应
                    spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selescTime = position;
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    break;
                case 1:
                    rootView.findViewById(R.id.tv_my_download_chose_LL).setVisibility(View.GONE);
                    break;
                case 2:
                    break;
                default:
                    break;
            }
        }

    };

}
