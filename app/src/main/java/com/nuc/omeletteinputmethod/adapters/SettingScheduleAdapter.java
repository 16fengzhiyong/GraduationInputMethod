package com.nuc.omeletteinputmethod.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.entityclass.ScheduleEntity;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SettingScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<ScheduleEntity> scheduleEntityArrayList;
    ScheduleEntity mScheduleEntity;
    private LayoutInflater mLayoutInflater;
    Context mContext;
    public SettingScheduleAdapter(ArrayList<ScheduleEntity> scheduleEntityArrayList, Context mContext) {
        this.scheduleEntityArrayList = scheduleEntityArrayList;
        this.mContext = mContext;
        mLayoutInflater = LayoutInflater.from(mContext);
    }
    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView timeView;
        TextView infoView;

        LinearLayout nomalLayot;
        ImageView addItem;
        View myitemView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            myitemView=itemView;
            timeView = itemView.findViewById(R.id.id_setting_schedule_time_TV);
            infoView = itemView.findViewById(R.id.id_setting_schedule_info_TV);
            addItem = itemView.findViewById(R.id.id_schedule_item_add_IV);
            nomalLayot = itemView.findViewById(R.id.id_schedule_item_parent);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 1111){
        }
        else {

        }
        View view = mLayoutInflater.inflate(R.layout.setting_schedule_item, parent, false);
        SettingScheduleAdapter.ItemViewHolder holder = new SettingScheduleAdapter.ItemViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final SettingScheduleAdapter.ItemViewHolder myViewHolder = (SettingScheduleAdapter.ItemViewHolder) viewHolder;

        if(position == scheduleEntityArrayList.size()){
            myViewHolder.nomalLayot.setVisibility(View.GONE);
            myViewHolder.addItem.setVisibility(View.VISIBLE);
        }else{
            mScheduleEntity = scheduleEntityArrayList.get(position);
            myViewHolder.timeView.setText(mScheduleEntity.getTime());
            myViewHolder.infoView.setText(mScheduleEntity.getInfo());
        }
    }

    @Override
    public int getItemCount() {
        return scheduleEntityArrayList.size()+1;
    }


    @Override
    public int getItemViewType(int position) {
        if(position == scheduleEntityArrayList.size()){
            return 1111;
        }else{
            return super.getItemViewType(position);
        }
    }
}
