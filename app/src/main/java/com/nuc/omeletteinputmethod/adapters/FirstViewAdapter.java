package com.nuc.omeletteinputmethod.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.entityclass.CandidatesEntity;
import com.nuc.omeletteinputmethod.kernel.OmeletteIME;
import com.nuc.omeletteinputmethod.kernel.keyboard.MyKeyboardView;
import com.nuc.omeletteinputmethod.util.TranslateCallback;
import com.nuc.omeletteinputmethod.util.TranslateUtil;

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
    //在此处添加 文本的使用频率
    //data.matches("[a-zA-Z]+")
    //是否为汉字
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
                //omeletteIME.commitText(myViewHolder.candidatesView.getText().toString());
                omeletteIME.commitText(candidatesEntityArrayList.get(position).getCandidates());
                //输入的是文字
                Log.i("输入的文字信息", "commitText: data " +candidatesEntityArrayList.get(position).getCandidates()
                        +"长度是:"+candidatesEntityArrayList.get(position).getCandidates().length());

            }
        });
        myViewHolder.candidatesView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                omeletteIME.getKeyboardSwisher().translateToEnglish(candidatesEntityArrayList.get(position).getCandidates());

                return true;
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
