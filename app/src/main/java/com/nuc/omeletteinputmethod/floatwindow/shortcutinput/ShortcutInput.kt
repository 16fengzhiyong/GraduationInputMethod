package com.nuc.omeletteinputmethod.floatwindow.shortcutinput

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.WindowManager

/**
 * 快捷输入 将常用的 信息 加载至输入块中
 * 并 添加 判断 是否 开启 普通输入法栏(获取当前运行的app的包名 或者 是否为 游戏模式 )
 * 其中 快捷语句 均存入数据库中
 * 此中申请权限的代码全部移入到 主界面中
 */
class ShortcutInput internal constructor(internal var mContext: Context, internal var mWindowManager: WindowManager) {
    fun initView() {

    }

    //检测用户是否对本app开启了“Apps with usage access”权限
    private fun hasPermission(): Boolean {
        val appOps = mContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        var mode = 0
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), mContext.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    companion object {

        private val MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1101
    }

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


}
