package com.nuc.omeletteinputmethod.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.nuc.omeletteinputmethod.R
import com.nuc.omeletteinputmethod.entityclass.CandidatesEntity
import com.nuc.omeletteinputmethod.kernel.OmeletteIME
import com.nuc.omeletteinputmethod.kernel.keyboard.MyKeyboardView

import java.util.ArrayList
import androidx.recyclerview.widget.RecyclerView


class FirstViewAdapter(private val omeletteIME: OmeletteIME, internal var candidatesEntityArrayList: ArrayList<CandidatesEntity>, private val myKeyboardView: MyKeyboardView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal lateinit var candidatesEntity: CandidatesEntity
    private val mLayoutInflater: LayoutInflater

    init {
        mLayoutInflater = LayoutInflater.from(omeletteIME)

        Log.d("firstadapter", "First_RecyclerViewAdapter: ")
    }

    class ItemViewHolder(internal var myitemView: View) : RecyclerView.ViewHolder(myitemView) {
        internal var candidatesView: TextView
        internal var CandidatesIdView: TextView

        init {
            candidatesView = myitemView.findViewById(R.id.id_textview_candidates)
            CandidatesIdView = myitemView.findViewById(R.id.id_textview_candidates_id)

        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {

        val view = mLayoutInflater.inflate(R.layout.candidates_layout, viewGroup, false)

        return ItemViewHolder(view)
    }

    //在此处添加 文本的使用频率
    //data.matches("[a-zA-Z]+")
    //是否为汉字
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        candidatesEntity = candidatesEntityArrayList[position]
        val myViewHolder = viewHolder as FirstViewAdapter.ItemViewHolder
        myViewHolder.CandidatesIdView.text = candidatesEntity.id.toString()
        myViewHolder.candidatesView.text = candidatesEntity.candidates
        myViewHolder.candidatesView.setOnClickListener {
            myKeyboardView.clearNowPinYin()
            omeletteIME.keyboardSwisher!!.hideCandidatesView()
            //omeletteIME.commitText(myViewHolder.candidatesView.getText().toString());
            omeletteIME.commitText(candidatesEntityArrayList[position].candidates!!)
            //输入的是文字
            Log.i("输入的文字信息", "commitText: data " + candidatesEntityArrayList[position].candidates
                    + "长度是:" + candidatesEntityArrayList[position].candidates!!.length)
            val finalData = candidatesEntityArrayList[position].candidates
            Thread(Runnable {
                if (finalData!!.length == 1) {
                    omeletteIME.dbManage!!.savePinlvOfOneSinogra(candidatesEntityArrayList[position])
                } else
                    omeletteIME.dbManage!!.savePinlvOfMoreSinogra(candidatesEntityArrayList[position])
            }).start()
        }


    }

    override fun getItemCount(): Int {
        return candidatesEntityArrayList.size
    }

    fun removeItem(pos: Int) {
        this.candidatesEntityArrayList.removeAt(pos)
        notifyItemRemoved(pos)
        if (pos != candidatesEntityArrayList.size) { // 如果移除的是最后一个，忽略
            notifyItemRangeChanged(pos, candidatesEntityArrayList.size - pos)
        }
    }

}
