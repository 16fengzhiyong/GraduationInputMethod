package com.nuc.omeletteinputmethod.floatwindow.translate;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.adapters.FloatShortInputAdapter;
import com.nuc.omeletteinputmethod.entityclass.FloatShortInputEntity;
import com.nuc.omeletteinputmethod.util.TranslateCallback;
import com.nuc.omeletteinputmethod.util.TranslateUtil;

import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 此处实现 复制信息的翻译 工作
 * 如果没有复制信息 那么 将提供翻译界面
 */
public class Translate {
    Context mContext;
    WindowManager windowManager;
    WindowManager.LayoutParams layoutParams;
    View displayView;


    TextView textView ;
    TranslateCallback translateCallback = new TranslateCallback() {
        @Override
        public void onTranslateDone(String result) {
            Log.i("翻译返回内容", "onTranslateDone: "+result+"开始显示");
            textView.setText(result);
            // result是翻译结果，在这里使用翻译结果，比如使用对话框显示翻译结果
            Log.i("翻译返回内容", "onTranslateDone: "+result);
        }
    };

    ImageView imageView ;
    public Translate(Context mContext, WindowManager mWindowManager, WindowManager.LayoutParams layoutParams, View displayView) {
        this.mContext = mContext;
        this.windowManager = mWindowManager;
        this.layoutParams = layoutParams;
        this.displayView = displayView;
    }


    public void removeView(View zhankai){
        windowManager.removeView(zhankai);
    }


    public void showTranslate(){
        try {

            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            displayView.findViewById(R.id.id_float_translate_parent_LL).setVisibility(View.VISIBLE);
            layoutParams.flags = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
            textView = displayView.findViewById(R.id.id_float_translate_text);
            imageView = displayView.findViewById(R.id.id_float_translate_canal);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayView.findViewById(R.id.id_float_translate_parent_LL).setVisibility(View.GONE);
                    layoutParams.flags = 16777256;
                    windowManager.updateViewLayout(displayView,layoutParams);
                    return;
                }
            });
            displayView.findViewById(R.id.id_float_translate_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String ret = ((EditText)displayView.findViewById(R.id.id_float_translate_edit)).getText().toString();
                    new TranslateUtil().translate(mContext, "auto", "en", ret, translateCallback);
                }
            });


            windowManager.addView(displayView,layoutParams);
        }catch (Exception e){

        }
    }

}
