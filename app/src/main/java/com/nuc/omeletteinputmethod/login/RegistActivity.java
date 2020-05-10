package com.nuc.omeletteinputmethod.login;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.network.HttpUrls;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import androidx.annotation.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegistActivity extends Activity {
    EditText regist_name;
    EditText regist_password1;
    EditText regist_password2;
    Button regist_ok;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        initViews();
        initListener();
    }
    public void initViews(){
        regist_name = findViewById(R.id.id_regist_name);
        regist_password1 = findViewById(R.id.id_regist_password1);
        regist_password2 = findViewById(R.id.id_regist_password2);
        regist_ok = findViewById(R.id.id_regist_ok);
    }
    public void initListener(){
        regist_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (regist_password1.getText().toString().equals(regist_password2.getText().toString())&&!regist_name.getText().toString().equals("")){
                    userRegist();
                }else {
                    Toast.makeText(RegistActivity.this,"两次密码不一样",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * 显示加载的进度款
     */
    LoadingDialog mLoadingDialog;
    public void showLoading() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this, getString(R.string.loading), false);
        }
        mLoadingDialog.show();
    }
    /**
     * 隐藏加载的进度框
     */
    public void hideLoading() {
        if (mLoadingDialog != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoadingDialog.hide();
                }
            });

        }
    }
    private void userRegist() {

        //先做一些基本的判断，比如输入的用户命为空，密码为空，网络不可用多大情况，都不需要去链接服务器了，而是直接返回提示错误
        if (regist_name.getText().toString().isEmpty()){
            Toast.makeText(RegistActivity.this,"你输入的账号为空",Toast.LENGTH_SHORT).show();
            return;
        }

        if (regist_password1.getText().toString().isEmpty()||regist_password2.getText().toString().isEmpty()){
            Toast.makeText(RegistActivity.this,"你输入的密码为空",Toast.LENGTH_SHORT).show();
            return;
        }
        //登录一般都是请求服务器来判断密码是否正确，要请求网络，要子线程
        //showLoading();//显示加载框
        Thread loginRunnable = new Thread() {

            @Override
            public void run() {
                super.run();
                postForRegist(HttpUrls.URL+"/userRegist.do",regist_name.getText().toString()
                        ,strTOmd5(regist_password1.getText().toString()));
            }
        };
        loginRunnable.start();
    }
    public void postForRegist(String url,String usernumber,String password){
        Log.i("保存的密码", "postForLogin: 准备发送 usernumber ="+usernumber +"password ="+password);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = builder.build();
        FormBody.Builder formbuilder = new FormBody.Builder();
        formbuilder.add("usernumber", usernumber);
        formbuilder.add("password", password);
        FormBody body = formbuilder.build();
        Request request = new Request.Builder()
                .post(body)
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("保存的密码", "user:"+call.toString());
                Log.d("保存的密码", "user:"+e.toString());

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String callback  = response.body().string();
                Log.d("保存的密码", "response:"+ callback);
                httpAnalysis(callback);
            }
        });
    }
    public boolean httpAnalysis(String result){
        try {
            JsonParser parser = new JsonParser();  //创建JSON解析器
            JsonObject array = (JsonObject)parser.parse(result) ;
            int abc = array.get("status").getAsInt();
            Log.d("jsonshow", "status:"+"???abc"+abc);
            if(abc==0){
                Toast.makeText(RegistActivity.this,"登录失败",Toast.LENGTH_SHORT).show();
                Log.i("保存的密码是", "httpAnalysis: 登录失败");
                SharedPreferencesUtils helper = new SharedPreferencesUtils(this, "setting");
                helper.putValues(
                        new SharedPreferencesUtils.ContentValue("remenberPassword", false));
                hideLoading();//隐藏加载框
                return false;
            }
            if(abc==1){
                //获取SharedPreferences对象，使用自定义类的方法来获取对象
                SharedPreferencesUtils helper = new SharedPreferencesUtils(this, "setting");
                helper.putValues(
                        new SharedPreferencesUtils.ContentValue("usernumber",regist_name.getText().toString()),
                        new SharedPreferencesUtils.ContentValue("remenberPassword", true),
                        new SharedPreferencesUtils.ContentValue("autoLogin", true),
                        new SharedPreferencesUtils.ContentValue("password", strTOmd5(regist_password1.getText().toString())));
                hideLoading();//隐藏加载框
                finish();
                return true;
            }
        }catch (Exception e){
            Log.e("保存", "httpAnalysis: ",e );
            return false;
        }

        return false;
    }
    @NonNull
    public static String strTOmd5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
