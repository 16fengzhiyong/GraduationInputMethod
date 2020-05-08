package com.nuc.omeletteinputmethod.network;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpPosts {
    public static String postForLogin(String url1, String username, String password){
        final String[] callback = {null};
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = builder.build();
        FormBody.Builder formbuilder = new FormBody.Builder();
        formbuilder.add("username", username);
        formbuilder.add("password", password);
        FormBody body = formbuilder.build();
        Request request = new Request.Builder()
                .post(body)
                .url(url1)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("保存的密码", "user:"+call.toString());
                Log.d("保存的密码", "user:"+e.toString());

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback[0] = response.body().string();
                Log.d("保存的密码", "response:"+ callback[0]);
            }
        });
        return callback[0];
    }
}
