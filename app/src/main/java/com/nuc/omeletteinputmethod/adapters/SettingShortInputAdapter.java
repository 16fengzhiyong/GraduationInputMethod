package com.nuc.omeletteinputmethod.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.ShortInputActivity;
import com.nuc.omeletteinputmethod.entityclass.AppInfomationEntity;
import com.nuc.omeletteinputmethod.util.AllBuild;

import java.util.ArrayList;
import java.util.List;

public class SettingShortInputAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    ArrayList<AppInfomationEntity> appInfomationEntities;
    AppInfomationEntity appInfomationEntity;
    private LayoutInflater mLayoutInflater;
    Context mContext;
    int ls000;//临时标识，作为是否为添加数据

    public SettingShortInputAdapter(ArrayList<AppInfomationEntity> appInfomationEntities, Context mContext) {
        this.appInfomationEntities = appInfomationEntities;
        this.mContext = mContext;
        mLayoutInflater = LayoutInflater.from(mContext);
    }
    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView appNameView;
        TextView appVersionView;
        ImageView appIconView;

        View myitemView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            myitemView=itemView;
            appIconView = itemView.findViewById(R.id.id_setting_shortinput_icon_item_IV);
            appNameView = itemView.findViewById(R.id.id_setting_shortinput_appname_item_TV);
            appVersionView = itemView.findViewById(R.id.id_setting_shortinput_versionname_item_TV);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.setting_shortinput_item, viewGroup, false);
        ItemViewHolder holder = new ItemViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        appInfomationEntity = appInfomationEntities.get(position);
        final ItemViewHolder myViewHolder = (ItemViewHolder) viewHolder;
        myViewHolder.appIconView.setImageDrawable(appInfomationEntity.getAppIcon());
        myViewHolder.appNameView.setText(appInfomationEntity.getAppName());
        myViewHolder.appVersionView.setText(appInfomationEntity.getVersionName());
        myViewHolder.myitemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ls000 == 0){
//                    Intent intent = new Intent(mContext,ShortInputActivity.class);
//
//                    intent.putExtra("DATA", appInfomationEntities.get(position));
//
//                    mContext.startActivity(intent);

                    Intent intent = new Intent(mContext,ShortInputActivity.class);
                    intent.putExtra("appName",appInfomationEntities.get(position).getAppName());
                    intent.putExtra("packageName",appInfomationEntities.get(position).getAppPackageName());
                    byte[] appIcon = AllBuild.bitmap2Bytes(AllBuild.drawableToBitamp(appInfomationEntities.get(position).getAppIcon()));
                    intent.putExtra("appIcon",appIcon);
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return appInfomationEntities.size();
    }
    public void removeItem(int pos){
        this.appInfomationEntities.remove(pos);
        notifyItemRemoved(pos);
        if(pos != appInfomationEntities.size()){ // 如果移除的是最后一个，忽略
            notifyItemRangeChanged(pos, appInfomationEntities.size() - pos);
        }
    }
    public List<AppInfomationEntity> getAllData(){
        return appInfomationEntities;
    }
}
