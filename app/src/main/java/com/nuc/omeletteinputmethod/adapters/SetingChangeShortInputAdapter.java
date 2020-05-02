package com.nuc.omeletteinputmethod.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.ShortInputActivity;
import com.nuc.omeletteinputmethod.entityclass.AppInfomationEntity;
import com.nuc.omeletteinputmethod.entityclass.FloatShortInputEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class SetingChangeShortInputAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    ArrayList<FloatShortInputEntity> floatShortInputEntities;
    FloatShortInputEntity floatShortInputEntity;
    private LayoutInflater mLayoutInflater;
    Context mContext;
    int ls000;//临时标识，作为是否为添加数据

    public SetingChangeShortInputAdapter(ArrayList<FloatShortInputEntity> appInfomationEntities, Context mContext) {
        this.floatShortInputEntities = appInfomationEntities;
        this.mContext = mContext;
        mLayoutInflater = LayoutInflater.from(mContext);
    }
    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView infoIDView;
        TextView infoView;

        LinearLayout deleteLinearLayout;
        Button deleteButton;
        Button cancleButton;

        View myitemView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            myitemView=itemView;
            infoIDView = itemView.findViewById(R.id.setting_change_shortinput_id);
            infoView = itemView.findViewById(R.id.setting_change_shortinput_info);

            deleteLinearLayout =  (LinearLayout)itemView.findViewById(R.id.first_page_item_deleteLinearLayout);

            deleteButton = (Button)itemView.findViewById(R.id.first_page_item_delete);
            cancleButton = (Button)itemView.findViewById(R.id.first_page_item_cancel);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = mLayoutInflater.inflate(R.layout.setting_change_shortinput_item, viewGroup, false);
        ItemViewHolder holder = new ItemViewHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        floatShortInputEntity = floatShortInputEntities.get(position);
        final ItemViewHolder myViewHolder = (ItemViewHolder) viewHolder;
        myViewHolder.infoIDView.setText(String.valueOf(position));
        myViewHolder.infoView.setText(floatShortInputEntity.getTag());

        myViewHolder.myitemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myViewHolder.deleteLinearLayout.setVisibility(View.VISIBLE);
            }
        });

//        myViewHolder.myitemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public boolean onClick(View view) {
//                myViewHolder.deleteLinearLayout.setVisibility(View.VISIBLE);
//                //是否  忽略点击事件
//                return true;
//            }
//        });

        myViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = floatShortInputEntities.get(position).getId();
                removeItem(position);
            }
        });

        myViewHolder.cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myViewHolder.deleteLinearLayout.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public int getItemCount() {
        return floatShortInputEntities.size();
    }
    public void removeItem(int pos){
        this.floatShortInputEntities.remove(pos);
        notifyItemRemoved(pos);
        if(pos != floatShortInputEntities.size()){ // 如果移除的是最后一个，忽略
            notifyItemRangeChanged(pos, floatShortInputEntities.size() - pos);
        }
    }

    public List<FloatShortInputEntity> getAllData(){
        return floatShortInputEntities;
    }
}