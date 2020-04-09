package com.nuc.omeletteinputmethod.myframent

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.nuc.omeletteinputmethod.R
import com.nuc.omeletteinputmethod.adapters.SettingShortInputAdapter
import com.nuc.omeletteinputmethod.entityclass.AppInfomationEntity

import java.util.ArrayList

class ShortInputFrament : LazyFrament() {
     lateinit var mRecyclerView: RecyclerView

    protected override val layoutId: Int
        get() = R.layout.setting_shortinput_layout

    override fun fetchData() {

    }

    override fun initView(savedInstanceState: Bundle?) {
        val layoutManager = LinearLayoutManager(mContext)
        mRecyclerView = rootView!!.findViewById<View>(R.id.id_setting_shortinput_RV) as RecyclerView
        //调整RecyclerView的排列方向
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.adapter = SettingShortInputAdapter(getItems(mContext!!), mContext!!)
    }

    override fun initWidgetActions() {

    }


    /**
     * 获取应用信息
     * @param context
     * @return
     */
    fun getItems(context: Context): ArrayList<AppInfomationEntity> {
        val pckMan = context.packageManager
        val appInfomationEntities = ArrayList<AppInfomationEntity>()
        //        ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
        var id = 0
        val packageInfo = pckMan.getInstalledPackages(0)

        for (pInfo in packageInfo) {
            appInfomationEntities.add(AppInfomationEntity(id++, pInfo.applicationInfo.loadLabel(pckMan).toString(), pInfo.packageName, pInfo.versionName, pInfo.applicationInfo.loadIcon(pckMan)))
            //            HashMap<String, Object> item = new HashMap<String, Object>();
            //
            //            item.put("appimage", pInfo.applicationInfo.loadIcon(pckMan));
            //            item.put("packageName", pInfo.packageName);
            //            item.put("versionCode", pInfo.versionCode);
            //            item.put("versionName", pInfo.versionName);
            //            item.put("appName", pInfo.applicationInfo.loadLabel(pckMan).toString());
            //
            //            items.add(item);
            Log.i("应用信息：", "appName :" + pInfo.applicationInfo.loadLabel(pckMan).toString() +
                    "appPackageName :" + pInfo.packageName)
        }
        return appInfomationEntities
    }
    companion object {
        private var fragment: ShortInputFrament? = null
        fun newInstance(): ShortInputFrament {
            if (fragment == null) {
                fragment = ShortInputFrament()
            }
            return fragment as ShortInputFrament
        }
    }
}
