package com.nuc.omeletteinputmethod.floatwindow.shortcutinput;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.adapters.FloatShortInputAdapter;
import com.nuc.omeletteinputmethod.entityclass.AppInfomationEntity;
import com.nuc.omeletteinputmethod.entityclass.FloatShortInputEntity;
import com.nuc.omeletteinputmethod.floatwindow.FloatingWindowDisplayService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 快捷输入 将常用的 信息 加载至输入块中
 * 并 添加 判断 是否 开启 普通输入法栏(获取当前运行的app的包名 或者 是否为 游戏模式 )
 * 其中 快捷语句 均存入数据库中
 * 此中申请权限的代码全部移入到 主界面中
 */
public class  ShortcutInput {

    Context mContext;
    WindowManager windowManager;
    WindowManager.LayoutParams layoutParams;
    View displayView;

    ShortcutInput(Context context, WindowManager windowManager){
        this.mContext = context;

    }

    public ShortcutInput(Context mContext, WindowManager mWindowManager, WindowManager.LayoutParams layoutParams, View displayView) {
        this.mContext = mContext;
        this.windowManager = mWindowManager;
        this.layoutParams = layoutParams;
        this.displayView = displayView;
    }

    public void removeView(View zhankai){
        windowManager.removeView(zhankai);
    }
    public void showShortInput(){
        try {
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            displayView.findViewById(R.id.id_float_shortinput_parent_LL).setVisibility(View.VISIBLE);
            displayView.findViewById(R.id.id_float_shortinput_close_IV).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayView.findViewById(R.id.id_float_shortinput_parent_LL).setVisibility(View.GONE);
                }
            });
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            RecyclerView mRecyclerView=(RecyclerView)displayView.findViewById(R.id.id_float_shortinput_list_RV);
            //调整RecyclerView的排列方向
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
            ArrayList<FloatShortInputEntity> floatShortInputEntities = new ArrayList<>();
//                    com.nuc.omeletteinputmethod
            String nowpackagename = ShortcutInput.getLollipopRecentTask(mContext);
            mRecyclerView.setAdapter(
                    new FloatShortInputAdapter(FloatingWindowDisplayService.getDbManage().getDataByPackageName(
                            nowpackagename), mContext));


            ((TextView)displayView.findViewById(R.id.id_float_shortinput_appname)).setText(getAppName(nowpackagename));
            windowManager.addView(displayView,layoutParams);
        }catch (Exception e){

        }
    }


    public String getAppName(String nowpackagename){
        PackageManager pckMan = mContext.getPackageManager();

        int id = 0;
        List<PackageInfo> packageInfo = pckMan.getInstalledPackages(0);

        for (PackageInfo pInfo : packageInfo) {
            if (nowpackagename.equals(pInfo.packageName)){
                return pInfo.applicationInfo.loadLabel(pckMan).toString();
            }
        }
        Log.i("app开始运行 ", "getItems: 结束");
        return "app名字";
    }



    public static String getLollipopRecentTask(Context context) {
        final int PROCESS_STATE_TOP = 2;
        try {
            //通过反射获取私有成员变量processState，稍后需要判断该变量的值
            Field processStateField = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
            List<ActivityManager.RunningAppProcessInfo> processes = ((ActivityManager) context.getSystemService(
                    Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo process : processes) {
                //判断进程是否为前台进程
                if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    int state = processStateField.getInt(process);
                    //processState值为2
                    if (state == PROCESS_STATE_TOP) {
                        String[] packname = process.pkgList;
                        return packname[0];
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
