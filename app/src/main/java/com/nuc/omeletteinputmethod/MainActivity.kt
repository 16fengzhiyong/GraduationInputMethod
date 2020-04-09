package com.nuc.omeletteinputmethod

import androidx.appcompat.app.AppCompatActivity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.nuc.omeletteinputmethod.setting.SettingsActivity


class MainActivity : AppCompatActivity() {
    private val REQUEST_DIALOG_PERMISSION = 1010
    private val TAG = "MainActivity"
    private var tosetting: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tosetting = findViewById(R.id.tosetting)
        tosetting!!.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
        if (!canDrawOverlays(this)) {
            if (AllLogShow.Debug) Log.i(TAG, "onCreate: 现在状态是：" + canDrawOverlays(this))
            requestSettingCanDrawOverlays()
        }
        if (getDefaultInputMethodPkgName(this) != "com.nuc.omeletteinputmethod") {

            Toast.makeText(this@MainActivity, "请将输入法设置为默认输入法或首选输入法", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        startActivity(Intent(Settings.ACTION_SETTINGS))
    }

    //获取默认输入法包名：
    private fun getDefaultInputMethodPkgName(context: Context): String? {
        var mDefaultInputMethodPkg: String? = null

        val mDefaultInputMethodCls = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD)
        //输入法类名信息
        Log.d(TAG, "mDefaultInputMethodCls=$mDefaultInputMethodCls")
        if (!TextUtils.isEmpty(mDefaultInputMethodCls)) {
            //输入法包名
            mDefaultInputMethodPkg = mDefaultInputMethodCls.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0]
            Log.d(TAG, "mDefaultInputMethodPkg=" + mDefaultInputMethodPkg!!)
        }
        return mDefaultInputMethodPkg
    }

    //判断是否拥有悬浮窗权限
    fun canDrawOverlays(context: Context): Boolean {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                false
            }
        } catch (e: NoSuchMethodError) {
            return false
        }

    }

    //申请悬浮窗权限
    private fun requestSettingCanDrawOverlays() {
        Toast.makeText(this@MainActivity, "请打开显示悬浮窗开关!", Toast.LENGTH_LONG).show()
        val sdkInt = Build.VERSION.SDK_INT
        if (sdkInt >= Build.VERSION_CODES.O) {//8.0以上
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivityForResult(intent, REQUEST_DIALOG_PERMISSION)
        } else if (sdkInt >= Build.VERSION_CODES.M) {//6.0-8.0
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, REQUEST_DIALOG_PERMISSION)
        } else {
            //4.4-6.0一下
            //无需处理了asdasd
        }
    }
}
