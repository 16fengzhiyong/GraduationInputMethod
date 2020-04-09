package com.nuc.omeletteinputmethod.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.nuc.omeletteinputmethod.R
import com.nuc.omeletteinputmethod.entityclass.FloatShortInputEntity
import com.nuc.omeletteinputmethod.kernel.OmeletteIME

import java.util.ArrayList

class FloatShortInputAdapter(private val floatShortInputEntities: ArrayList<FloatShortInputEntity>, internal var mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal lateinit var floatShortInputEntity: FloatShortInputEntity
    private val mLayoutInflater: LayoutInflater

    init {
        mLayoutInflater = LayoutInflater.from(mContext)
    }


    inner class ItemViewHolder(internal var myitemView: View) : RecyclerView.ViewHolder(myitemView) {
        internal var tagView: TextView

        init {
            tagView = myitemView.findViewById(R.id.id_float_shortinput_item_TV)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = mLayoutInflater.inflate(R.layout.float_shortinput_item, viewGroup, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        floatShortInputEntity = floatShortInputEntities[position]
        val myViewHolder = viewHolder as ItemViewHolder
        myViewHolder.tagView.text = floatShortInputEntity.tag
        myViewHolder.tagView.setOnClickListener { omeletteIME!!.commitText(floatShortInputEntities[position].tag!!) }
    }

    override fun getItemCount(): Int {
        return floatShortInputEntities.size
    }

    fun removeItem(pos: Int) {
        this.floatShortInputEntities.removeAt(pos)
        notifyItemRemoved(pos)
        if (pos != floatShortInputEntities.size) { // 如果移除的是最后一个，忽略
            notifyItemRangeChanged(pos, floatShortInputEntities.size - pos)
        }
    }

    companion object {
        var omeletteIME: OmeletteIME? = null
    }
}
