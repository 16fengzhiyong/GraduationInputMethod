package com.nuc.omeletteinputmethod.floatwindow.shortcutinput;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import com.nuc.omeletteinputmethod.entityclass.AppInfomationEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 快捷输入 将常用的 信息 加载至输入块中
 * 并 添加 判断 是否 开启 普通输入法栏(获取当前运行的app的包名 或者 是否为 游戏模式 )
 * 其中 快捷语句 均存入数据库中
 * 此中申请权限的代码全部移入到 主界面中
 */
public class  ShortcutInput {

    Context mContext;
    WindowManager mWindowManager;

    ShortcutInput(Context context, WindowManager windowManager){
        this.mContext = context;
        this.mWindowManager = windowManager;
    }
    public void initView(){

    }
    //检测用户是否对本app开启了“Apps with usage access”权限
    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                mContext.getSystemService(Context.APP_OPS_SERVICE);
        int mode = 0;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), mContext.getPackageName());
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1101;

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS) {
//            if (!hasPermission()) {
//                //若用户未开启权限，则引导用户开启“Apps with usage access”权限
//                mContext.startActivityForResult(
//                        new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
//                        MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
//            }
//        }
//    }
//    ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
//    RunningTaskInfo runningTaskInfo = manager.getRunningTasks(1).get(0);
//    Log.v("TAG", "getClassName:"+runningTaskInfo.baseActivity.getPackageName());
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
            Log.i("应用信息：", "appName :" +pInfo.applicationInfo.loadLabel(pckMan).toString()+
                    "appPackageName :"+pInfo.packageName);
        }
        return appInfomationEntities;
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
