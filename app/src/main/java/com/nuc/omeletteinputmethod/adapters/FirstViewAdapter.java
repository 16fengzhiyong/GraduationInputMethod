package com.nuc.omeletteinputmethod.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.entityclass.CandidatesEntity;
import com.nuc.omeletteinputmethod.entityclass.OneSinograEntity;
import com.nuc.omeletteinputmethod.kernel.OmeletteIME;
import com.nuc.omeletteinputmethod.kernel.keyboard.MyKeyboardView;

import java.io.IOException;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



public class FirstViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    ArrayList<CandidatesEntity> candidatesEntityArrayList;
    CandidatesEntity candidatesEntity;
    private OmeletteIME omeletteIME;
    private LayoutInflater mLayoutInflater;
    private MyKeyboardView myKeyboardView;
    public FirstViewAdapter(OmeletteIME omeletteIME, ArrayList<CandidatesEntity> newslist,MyKeyboardView myKeyboardView){
        this.candidatesEntityArrayList = newslist;
        this.omeletteIME = omeletteIME;
        this.myKeyboardView = myKeyboardView;
        mLayoutInflater = LayoutInflater.from(omeletteIME);

        Log.d("firstadapter", "First_RecyclerViewAdapter: ");
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView candidatesView;
        TextView CandidatesIdView;
        View myitemView ;



        public ItemViewHolder(View itemView) {
            super(itemView);
            myitemView=itemView;
            candidatesView = itemView.findViewById(R.id.id_textview_candidates);
            CandidatesIdView = itemView.findViewById(R.id.id_textview_candidates_id);

        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = mLayoutInflater.inflate(R.layout.candidates_layout, viewGroup, false);
        FirstViewAdapter.ItemViewHolder holder = new FirstViewAdapter.ItemViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        candidatesEntity = candidatesEntityArrayList.get(position);
        final ItemViewHolder myViewHolder = (FirstViewAdapter.ItemViewHolder)viewHolder;
        myViewHolder.CandidatesIdView.setText(String.valueOf(candidatesEntity.getId()));
        myViewHolder.candidatesView.setText(candidatesEntity.getCandidates());
        myViewHolder.candidatesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myKeyboardView.clearNowPinYin();
                omeletteIME.getKeyboardSwisher().hideCandidatesView();
                omeletteIME.commitText(myViewHolder.candidatesView.getText().toString());
            }
        });


    }

    @Override
    public int getItemCount() {
         return candidatesEntityArrayList.size();
    }
    public void removeItem(int pos){
        this.candidatesEntityArrayList.remove(pos);
        notifyItemRemoved(pos);
        if(pos != candidatesEntityArrayList.size()){ // 如果移除的是最后一个，忽略
            notifyItemRangeChanged(pos, candidatesEntityArrayList.size() - pos);
        }
    }

}
