package com.nuc.omeletteinputmethod.myframent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * Created by zlk on 2017/7/24.
 */

public abstract  class BaseFrament extends Fragment {
    protected Context mContext;
    protected View rootView;




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if(internalReceiver!=null){
            mContext.unregisterReceiver(internalReceiver);
        }
        mContext = null;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(getLayoutId(), container, false);
            initView(savedInstanceState);
            initWidgetActions();
        } else {
            // 缓存的rootView需要判断是否已经被加过parent，如果有parent需要从parent删除，
            // 要不然会发生这个rootview已经有parent的错误。
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.removeView(rootView);
            }
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("aa",getClass().getSimpleName());
    }

    protected abstract int getLayoutId();

    protected abstract void initView(Bundle savedInstanceState);

    protected abstract void initWidgetActions();

//============================== 以下为广播===============================
    private InternalReceiver internalReceiver;

    /**
     * 注册广播
     * @param actionArray
     */
    protected final void registerReceiver(String[] actionArray) {
        if (actionArray == null) {
            return;
        }
        IntentFilter intentfilter = new IntentFilter();
        for (String action : actionArray) {
            intentfilter.addAction(action);
        }
        if (internalReceiver == null) {
            internalReceiver = new InternalReceiver();
        }
        getActivity().registerReceiver(internalReceiver, intentfilter);
    }


    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
    }

    /**
     * 自定义应用全局广播处理器，方便全局拦截广播并进行分发
     * @author 容联•云通讯
     * @date 2014-12-4
     * @version 4.0
     */
    private class InternalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null || intent.getAction() == null) {
                return ;
            }
            handleReceiver(context, intent);
        }
    }
    /**
     * 如果子界面需要拦截处理注册的广播
     * 需要实现该方法
     * @param context
     * @param intent
     */
    protected void handleReceiver(Context context, Intent intent) {
    }
    //======================= 以下封装activity跳转=======================================

    public void startActivity(Class<?> cls) {
        startActivity(cls, null);
    }

    public void startActivity(Class<?> cls, Bundle bundle ) {
        Intent intent = new Intent();
        intent.setClass(mContext, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    public void startNextActivity(Class<?> cls, String key, Serializable serializable) {
        Intent intent = new Intent(mContext, cls);
        intent.putExtra(key, serializable);
        startActivity(intent);
    }
}
