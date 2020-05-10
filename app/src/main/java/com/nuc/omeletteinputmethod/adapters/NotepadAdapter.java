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
import com.nuc.omeletteinputmethod.entityclass.FloatShortInputEntity;
import com.nuc.omeletteinputmethod.entityclass.NotepadEntity;
import com.nuc.omeletteinputmethod.kernel.OmeletteIME;
import com.nuc.omeletteinputmethod.util.AllBuild;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotepadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<NotepadEntity> notepadEntityArrayList;
    NotepadEntity notepadEntity;
    private LayoutInflater mLayoutInflater;
    Context mContext;
    int ls000;//临时标识，作为是否为添加数据

    public NotepadAdapter(ArrayList<NotepadEntity> notepadEntityArrayList, Context mContext) {
        this.notepadEntityArrayList = notepadEntityArrayList;
        this.mContext = mContext;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView infoIDView;
        TextView infoView;

        LinearLayout deleteLinearLayout;
        Button deleteButton;
        Button cancleButton;

        View myitemView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            myitemView = itemView;
            infoIDView = itemView.findViewById(R.id.setting_change_shortinput_id);
            infoView = itemView.findViewById(R.id.setting_change_shortinput_info);

            deleteLinearLayout = (LinearLayout) itemView.findViewById(R.id.first_page_item_deleteLinearLayout);

            deleteButton = (Button) itemView.findViewById(R.id.first_page_item_delete);
            cancleButton = (Button) itemView.findViewById(R.id.first_page_item_cancel);
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
        notepadEntity = notepadEntityArrayList.get(position);
        final ItemViewHolder myViewHolder = (ItemViewHolder) viewHolder;
        myViewHolder.infoIDView.setText(String.valueOf(position));
        myViewHolder.infoView.setText(notepadEntity.getText());

        myViewHolder.myitemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                myViewHolder.deleteLinearLayout.setVisibility(View.VISIBLE);
                return true;
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
        return notepadEntityArrayList.size();
    }

    public void removeItem(int pos) {
        this.notepadEntityArrayList.remove(pos);
        notifyItemRemoved(pos);
        if (pos != notepadEntityArrayList.size()) { // 如果移除的是最后一个，忽略
            notifyItemRangeChanged(pos, notepadEntityArrayList.size() - pos);
        }
    }

    public List<NotepadEntity> getAllData() {
        return notepadEntityArrayList;
    }
}
