package com.nuc.omeletteinputmethod.adapters;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.entityclass.AppInfomationEntity;
import com.nuc.omeletteinputmethod.entityclass.FloatShortInputEntity;
import com.nuc.omeletteinputmethod.kernel.OmeletteIME;

import java.util.ArrayList;
import java.util.List;

public class FloatShortInputAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<FloatShortInputEntity> floatShortInputEntities ;
    FloatShortInputEntity floatShortInputEntity;
    Context mContext;
    private LayoutInflater mLayoutInflater;
    public static OmeletteIME omeletteIME;
    public FloatShortInputAdapter(ArrayList<FloatShortInputEntity> floatShortInputEntities,Context context){
        this.floatShortInputEntities=floatShortInputEntities;
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView tagView;
        View myitemView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            myitemView=itemView;
            tagView = itemView.findViewById(R.id.id_float_shortinput_item_TV);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = mLayoutInflater.inflate(R.layout.float_shortinput_item, viewGroup, false);
        ItemViewHolder holder = new ItemViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        floatShortInputEntity = floatShortInputEntities.get(position);
        final ItemViewHolder myViewHolder = (ItemViewHolder) viewHolder;
        myViewHolder.tagView.setText(floatShortInputEntity.getTag());
        myViewHolder.tagView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                InputMethodService inputMethodService = (InputMethodService)mContext.getSystemService(getDefaultInputMethodPkgName(mContext));
//                inputMethodService.getCurrentInputConnection().commitText(floatShortInputEntities.get(position).getTag(),0);

                if (omeletteIME!=null)
                omeletteIME.commitText(floatShortInputEntities.get(position).getTag());
                else Log.i("FloatShortInputAdapter", "fzy onClick: 62 null");
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
//    //获取默认输入法包名：
//    private String getDefaultInputMethodPkgName(Context context) {
//        String mDefaultInputMethodPkg = null;
//
//        String mDefaultInputMethodCls = Settings.Secure.getString(
//                context.getContentResolver(),
//                Settings.Secure.DEFAULT_INPUT_METHOD);
//        //输入法类名信息
//        Log.d("TAG", "mDefaultInputMethodCls=" + mDefaultInputMethodCls);
//        if (!TextUtils.isEmpty(mDefaultInputMethodCls)) {
//            //输入法包名
//            mDefaultInputMethodPkg = mDefaultInputMethodCls.split("/")[0];
//            Log.d("TAG", "mDefaultInputMethodPkg=" + mDefaultInputMethodPkg);
//        }
//        return mDefaultInputMethodPkg;
//    }
}
