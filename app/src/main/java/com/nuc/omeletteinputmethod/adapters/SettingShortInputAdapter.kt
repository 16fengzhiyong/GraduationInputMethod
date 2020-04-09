package com.nuc.omeletteinputmethod.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.nuc.omeletteinputmethod.R
import com.nuc.omeletteinputmethod.entityclass.AppInfomationEntity

import java.util.ArrayList

class SettingShortInputAdapter(internal var appInfomationEntities: ArrayList<AppInfomationEntity>, internal var mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal lateinit var appInfomationEntity: AppInfomationEntity
    private val mLayoutInflater: LayoutInflater

    init {
        mLayoutInflater = LayoutInflater.from(mContext)
    }

    inner class ItemViewHolder(internal var myitemView: View) : RecyclerView.ViewHolder(myitemView) {
        internal var appNameView: TextView
        internal var appVersionView: TextView
        internal var appIconView: ImageView

        init {
            appIconView = myitemView.findViewById(R.id.id_setting_shortinput_icon_item_IV)
            appNameView = myitemView.findViewById(R.id.id_setting_shortinput_appname_item_TV)
            appVersionView = myitemView.findViewById(R.id.id_setting_shortinput_versionname_item_TV)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = mLayoutInflater.inflate(R.layout.setting_shortinput_item, viewGroup, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        appInfomationEntity = appInfomationEntities[position]
        val myViewHolder = viewHolder as ItemViewHolder
        myViewHolder.appIconView.setImageDrawable(appInfomationEntity.appIcon)
        myViewHolder.appNameView.text = appInfomationEntity.appName
        myViewHolder.appVersionView.text = appInfomationEntity.versionName
    }

    override fun getItemCount(): Int {
        return appInfomationEntities.size
    }

    fun removeItem(pos: Int) {
        this.appInfomationEntities.removeAt(pos)
        notifyItemRemoved(pos)
        if (pos != appInfomationEntities.size) { // 如果移除的是最后一个，忽略
            notifyItemRangeChanged(pos, appInfomationEntities.size - pos)
        }
    }
}
