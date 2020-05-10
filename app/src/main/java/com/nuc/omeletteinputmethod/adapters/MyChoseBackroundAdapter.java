package com.nuc.omeletteinputmethod.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.login.SharedPreferencesUtils;
import com.nuc.omeletteinputmethod.myframent.MyUI;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyChoseBackroundAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder>{
    SharedPreferencesUtils helper ;
    ArrayList<Integer> strings;
    private LayoutInflater mLayoutInflater;
    Context mContext;
    RecyclerView recyclerView;
    public MyChoseBackroundAdapter(ArrayList<Integer> strings, Context mContext,RecyclerView recyclerView) {
        helper = new SharedPreferencesUtils(mContext, "setting");
        this.strings = strings;
        this.mContext = mContext;
        this.recyclerView = recyclerView;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView img1;
        ImageView img2;

        View myitemView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            myitemView = itemView;
            img1 = itemView.findViewById(R.id.my_chose_backround_item_1);
            img2 = itemView.findViewById(R.id.my_chose_backround_item_2);
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.my_chose_img_item, parent, false);
        ItemViewHolder holder = new ItemViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {

        Log.i("个人用户选择", "onBindViewHolder: aaaa  position ="+position);
        final ItemViewHolder myViewHolder = (ItemViewHolder) viewHolder;
        if ((position * 2)<strings.size()){
            Log.i("个人用户选择", "onBindViewHolder: position ="+position);
            myViewHolder.img1.setImageResource(strings.get(position * 2));
            myViewHolder.img1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    helper.putValues(new SharedPreferencesUtils.ContentValue("choseimage", strings.get(position * 2)));
                    recyclerView.setVisibility(View.GONE);
                }
            });
        }
        if ((position * 2+1)<strings.size()){
            myViewHolder.img2.setImageResource(strings.get(position * 2 + 1));

            myViewHolder.img2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    helper.putValues(new SharedPreferencesUtils.ContentValue("choseimage", strings.get(position * 2 + 1)));
                    recyclerView.setVisibility(View.GONE);
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        Log.i("个人用户选择", "onBindViewHolder: ret  position ="+(strings.size()/2));
        return strings.size()/2;

    }
}
