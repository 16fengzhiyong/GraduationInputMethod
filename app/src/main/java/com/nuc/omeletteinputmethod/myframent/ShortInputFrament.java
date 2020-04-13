package com.nuc.omeletteinputmethod.myframent;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.adapters.SettingShortInputAdapter;
import com.nuc.omeletteinputmethod.entityclass.AppInfomationEntity;

import java.util.ArrayList;
import java.util.List;

public class ShortInputFrament extends LazyFrament {
    RecyclerView mRecyclerView;
    @Override
    public void fetchData() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.setting_shortinput_layout;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView=(RecyclerView)rootView.findViewById(R.id.id_setting_shortinput_RV);
        //调整RecyclerView的排列方向
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(new SettingShortInputAdapter(getItems(mContext),mContext));

    }

    @Override
    protected void initWidgetActions() {

    }


    /**
     * 获取应用信息
     * @param context
     * @return
     */
    public ArrayList<AppInfomationEntity> getItems(Context context) {
        PackageManager pckMan = context.getPackageManager();
        ArrayList<AppInfomationEntity> appInfomationEntities = new ArrayList<>();
//        ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
        int id = 0;
        List<PackageInfo> packageInfo = pckMan.getInstalledPackages(0);

        for (PackageInfo pInfo : packageInfo) {
            appInfomationEntities.add(new AppInfomationEntity(id++,pInfo.applicationInfo.loadLabel(pckMan).toString()
                    ,pInfo.packageName,pInfo.versionName,pInfo.applicationInfo.loadIcon(pckMan)));
//            HashMap<String, Object> item = new HashMap<String, Object>();
//
//            item.put("appimage", pInfo.applicationInfo.loadIcon(pckMan));
//            item.put("packageName", pInfo.packageName);
//            item.put("versionCode", pInfo.versionCode);
//            item.put("versionName", pInfo.versionName);
//            item.put("appName", pInfo.applicationInfo.loadLabel(pckMan).toString());
//
//            items.add(item);
            Log.i("应用信息：", "appName :" +pInfo.applicationInfo.loadLabel(pckMan).toString()+
                    "appPackageName :"+pInfo.packageName);
        }
        return appInfomationEntities;
    }

    private static ShortInputFrament fragment = null;
    public static ShortInputFrament newInstance() {
        if (fragment == null){
            fragment = new ShortInputFrament();
        }
        return fragment;
    }
}
