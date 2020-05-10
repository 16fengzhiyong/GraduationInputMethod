package com.nuc.omeletteinputmethod.myframent;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.adapters.SettingShortInputAdapter;
import com.nuc.omeletteinputmethod.entityclass.AppInfomationEntity;
import com.nuc.omeletteinputmethod.entityclass.FloatShortInputEntity;
import com.nuc.omeletteinputmethod.floatwindow.FloatingWindowDisplayService;

import java.util.ArrayList;
import java.util.List;

public class ShortInputFrament extends LazyFrament {
    RecyclerView mRecyclerView;
    ArrayList<AppInfomationEntity> alreadHave = new ArrayList<>();
    ArrayList<AppInfomationEntity> all;
    ArrayList<AppInfomationEntity> nohave;
    @Override
    public void fetchData() {

    }
    RecyclerView haveShortInputRecyclerView;
    @Override
    protected int getLayoutId() {
        return R.layout.setting_shortinput_layout;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        Log.i("app开始运行：", "initView: ShortInputFrament 33");

        LinearLayoutManager layoutManager1 = new LinearLayoutManager(mContext);
        layoutManager1.setOrientation(LinearLayoutManager.VERTICAL);
        haveShortInputRecyclerView =(RecyclerView)rootView.findViewById(R.id.id_setting_shortinput_have_RV);
        haveShortInputRecyclerView.setLayoutManager(layoutManager1);


        haveShortInputRecyclerView.setNestedScrollingEnabled(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView=(RecyclerView)rootView.findViewById(R.id.id_setting_shortinput_RV);
        //调整RecyclerView的排列方向
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        haveShortInputRecyclerView.setNestedScrollingEnabled(false);

        mRecyclerView.setNestedScrollingEnabled(false);

        Log.i("app开始运行：", "initView: ShortInputFrament 49");

        new Thread(new Runnable() {
            @Override
            public void run() {
                all = getItems(mContext);
                Message message = Message.obtain();
                message.what = 0;
                handler.sendMessage(message);
            }
        }).start();
//        mRecyclerView.setLayoutManager(new
//                LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
//        SettingShortInputAdapter adapter = new SettingShortInputAdapter(getItems(mContext),mContext);
//        ItemTouchHelper helper = new ItemTouchHelper(new MyItemTouchHelper(adapter));
//        helper.attachToRecyclerView(mRecyclerView);
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
        Log.i("app开始运行 ", "getItems: 开始");
        PackageManager pckMan = context.getPackageManager();
        ArrayList<AppInfomationEntity> appInfomationEntities = new ArrayList<>();
//        ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
        int id = 0;
        List<PackageInfo> packageInfo = pckMan.getInstalledPackages(0);

        for (PackageInfo pInfo : packageInfo) {
            appInfomationEntities.add(new AppInfomationEntity(id++,pInfo.applicationInfo.loadLabel(pckMan).toString()
                    ,pInfo.packageName,pInfo.versionName,pInfo.applicationInfo.loadIcon(pckMan)));
            Log.i("应用信息：", "appName :" +pInfo.applicationInfo.loadLabel(pckMan).toString()+
                    "appPackageName :"+pInfo.packageName);
//            if (id >10){
//                break;
//            }

        }
        Log.i("app开始运行 ", "getItems: 结束");
        return appInfomationEntities;
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 0:
                    ArrayList<FloatShortInputEntity> floatShortInputEntityArrayList =
                    FloatingWindowDisplayService.getDbManage().getAllFloatShortInput();
                    for (int i = 0; i < floatShortInputEntityArrayList.size(); i++) {
                        Log.i("储存的应用包名", "handleMessage: "+floatShortInputEntityArrayList.get(i).getPackageName());
                        for (int j = 0; j < all.size(); j++) {
                            if (floatShortInputEntityArrayList.get(i).getPackageName().equals(all.get(j).getAppPackageName())){
                                alreadHave.add(all.get(j));
                                Log.i("all list 清楚了 ", "handleMessage: " + all.get(j));
                                all.remove(j);
                            }
                        }

                    }
                    Log.i("现在拥有的", "handleMessage: " + all.size());
                    haveShortInputRecyclerView.setAdapter(new SettingShortInputAdapter(alreadHave,mContext));
                    mRecyclerView.setAdapter(new SettingShortInputAdapter(all,mContext));

                    //刷新布局
                    break;

            }
            return false;
        }
    });
    private static ShortInputFrament fragment = null;
    public static ShortInputFrament newInstance() {
        if (fragment == null){
            fragment = new ShortInputFrament();
        }
        return fragment;
    }
}
