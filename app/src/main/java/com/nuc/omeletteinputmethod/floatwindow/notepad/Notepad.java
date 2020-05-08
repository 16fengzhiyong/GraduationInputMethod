package com.nuc.omeletteinputmethod.floatwindow.notepad;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.adapters.FloatShortInputAdapter;
import com.nuc.omeletteinputmethod.entityclass.FloatShortInputEntity;

import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 此文件中需要实现 文本复制之后
 * 将其转化为记事本 放在 数据库 中
 * 如果没有复制信息 则显示 记事本
 */
public class Notepad {
    ClipboardManager mClipboardManager;
    ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener;
    Context mContext;

    WindowManager windowManager;
    WindowManager.LayoutParams layoutParams;
    View displayView;
    public Notepad(Context mContext, WindowManager mWindowManager, WindowManager.LayoutParams layoutParams, View displayView) {
        this.mContext = mContext;
        this.windowManager = mWindowManager;
        this.layoutParams = layoutParams;
        this.displayView = displayView;
    }
    /**
     * 注册剪切板复制、剪切事件监听
     */
    private void registerClipEvents() {
        mClipboardManager = (ClipboardManager) mContext.getSystemService(mContext.CLIPBOARD_SERVICE);
        mOnPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                if (mClipboardManager.hasPrimaryClip()
                        && mClipboardManager.getPrimaryClip().getItemCount() > 0) {
                    // 获取复制、剪切的文本内容
                    CharSequence content =
                            mClipboardManager.getPrimaryClip().getItemAt(0).getText();
                    Log.d("TAG", "复制、剪切的内容为：" + content);
                }
            }
        };
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
    }

    /**
     * 注销监听，避免内存泄漏。
     */
    protected void onDestroy() {
        if (mClipboardManager != null && mOnPrimaryClipChangedListener != null) {
            mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        }
    }

    public void removeView(View zhankai){
        windowManager.removeView(zhankai);
    }
    public void showNotepad(){
        try {
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            displayView.findViewById(R.id.id_float_notepad_partner).setVisibility(View.VISIBLE);
            Log.i("layoutParams 1", "showNotepad: "+layoutParams.flags);
            //16777256
            layoutParams.flags = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
            Log.i("layoutParams 2", "showNotepad: "+layoutParams.flags);

            displayView.findViewById(R.id.id_float_notepad_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayView.findViewById(R.id.id_float_notepad_partner).setVisibility(View.GONE);
                    layoutParams.flags = 16777256;
                    windowManager.addView(displayView,layoutParams);
                }
            });
            windowManager.addView(displayView,layoutParams);
        }catch (Exception e){

        }
    }
    private void showInputMethod() {
        //自动弹出键盘
        InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        //强制隐藏Android输入法窗口
        // inputManager.hideSoftInputFromWindow(edit.getWindowToken(),0);
    }


}
