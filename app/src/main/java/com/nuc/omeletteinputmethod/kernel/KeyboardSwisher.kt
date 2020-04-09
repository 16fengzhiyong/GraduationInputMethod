package com.nuc.omeletteinputmethod.kernel

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

import com.nuc.omeletteinputmethod.R
import com.nuc.omeletteinputmethod.adapters.FirstViewAdapter
import com.nuc.omeletteinputmethod.entityclass.CandidatesEntity
import com.nuc.omeletteinputmethod.kernel.keyboard.KeyboardBuider
import com.nuc.omeletteinputmethod.kernel.keyboard.KeyboardHeadListener
import com.nuc.omeletteinputmethod.kernel.keyboard.MyKeyboardView

import java.util.ArrayList

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class KeyboardSwisher {
    internal lateinit var omeletteIME: OmeletteIME
    internal var keyboardBuider: KeyboardBuider? = null
    lateinit internal var candidatesLayout: LinearLayout
    lateinit internal var mRecyclerView: RecyclerView
    lateinit internal var chooseBar: RelativeLayout
    lateinit var myKeyboardView: MyKeyboardView
        internal set
    lateinit internal var textviewCandidatesPinyin: TextView
    internal var layoutManager = LinearLayoutManager(omeletteIME)

    constructor() {

    }

    constructor(omeletteIME: OmeletteIME) {
        this.omeletteIME = omeletteIME
    }

    fun choseKeyboard(): View {
        var mkeyView: View? = null
        //        if (SettingsActivity.showMyselfkeyboard ==false){
        //            mkeyView = LayoutInflater.from(omeletteIME).inflate(
        //                    R.layout.layout_keyboardview, null);
        //            new KeyboardUtil(omeletteIME, (KeyboardView) mkeyView.findViewById(R.id.keyboardView));
        //            //omeletteIME.setCandidatesViewShown(true);
        //        }else {
        //            mkeyView = LayoutInflater.from(omeletteIME).inflate(
        //                    R.layout.layout_input_test, null);
        //            TextView textView = mkeyView.findViewById(R.id.change_kk_test);
        //            textView.setOnClickListener(new View.OnClickListener() {
        //                @Override
        //                public void onClick(View view) {
        //                    MyKeyboard myKeyboard = new MyKeyboard(omeletteIME,R.xml.nomal_qwerty);
        //                }
        //            });
        //        }
        //将此部分移到KeyboardBuider中 同时在里面实现 键盘切换方法
        // 实现方向转化方
        mkeyView = LayoutInflater.from(omeletteIME).inflate(
                R.layout.mytestkeyboardlayout, null)
        myKeyboardView = mkeyView!!.findViewById(R.id.test_mykeyboardview)
        myKeyboardView.SetMyKeyboardView(omeletteIME, R.xml.nomal_qwerty)
        ///omeletteIME.setCandidatesViewShown(true);
        mRecyclerView = mkeyView.findViewById<View>(R.id.id_recyclerview_candidates) as RecyclerView
        candidatesLayout = mkeyView.findViewById<View>(R.id.id_linear_candidates) as LinearLayout
        textviewCandidatesPinyin = mkeyView.findViewById<View>(R.id.id_textview_candidates_pinyin) as TextView
        ReadyCandidatesView()
        val keyboardHeadListener = KeyboardHeadListener(omeletteIME, mkeyView, myKeyboardView)
        chooseBar = mkeyView.findViewById(R.id.id_relativeLayout_candidates_choose)
        mkeyView.findViewById<View>(R.id.linearLayout_candidates_setting).setOnClickListener(keyboardHeadListener)
        mkeyView.findViewById<View>(R.id.id_linear_hide_keyboard).setOnClickListener(keyboardHeadListener)
        mkeyView.findViewById<View>(R.id.id_linear_pinyin_input).setOnClickListener(keyboardHeadListener)
        mkeyView.findViewById<View>(R.id.id_linear_english_input).setOnClickListener(keyboardHeadListener)
        mkeyView.findViewById<View>(R.id.id_linear_arrows_input).setOnClickListener(keyboardHeadListener)
        return mkeyView
        //        return setWaitView(mkeyView);
    }

    fun ReadyCandidatesView() {
        //调整RecyclerView的排列方向
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        mRecyclerView.layoutManager = layoutManager
    }

    fun showCandidatesView(candidatesEntityArrayList: ArrayList<CandidatesEntity>, pinyin: String) {
        chooseBar.visibility = View.GONE
        candidatesLayout.visibility = View.VISIBLE
        mRecyclerView.visibility = View.VISIBLE
        textviewCandidatesPinyin.visibility = View.VISIBLE
        textviewCandidatesPinyin.text = pinyin
        mRecyclerView.adapter = FirstViewAdapter(omeletteIME, candidatesEntityArrayList, myKeyboardView)
    }

    fun hideCandidatesView() {
        candidatesLayout.visibility = View.GONE
        chooseBar.visibility = View.VISIBLE
        mRecyclerView.visibility = View.GONE
        mRecyclerView.removeAllViews()
        if (mRecyclerView.adapter != null) {
            mRecyclerView.adapter!!.notifyDataSetChanged()
        }
        textviewCandidatesPinyin.visibility = View.GONE
    }

    fun enterInputPinYin(pinyin: String) {
        omeletteIME.commitText(pinyin)
    }

    fun setWaitView(mkeyView: View): View {
        var mkeyView = mkeyView
        val myKeyboardView: MyKeyboardView
        //mkeyView = new MyKeyboardView(omeletteIME,R.xml.nomal_qwerty);
        mkeyView = LayoutInflater.from(omeletteIME).inflate(
                R.layout.mytestkeyboardlayout, null)
        myKeyboardView = mkeyView.findViewById(R.id.test_mykeyboardview)
        myKeyboardView.SetMyKeyboardView(omeletteIME, R.xml.nomal_qwerty)
        ///omeletteIME.setCandidatesViewShown(true);
        val keyboardHeadListener = KeyboardHeadListener(omeletteIME, mkeyView, myKeyboardView)
        mkeyView.setOnClickListener(keyboardHeadListener)
        return mkeyView
    }

    fun choseCandidatesView(): View {
        val candidatesView: View
        candidatesView = RelativeLayout.inflate(omeletteIME, R.layout.input_waiting_layout, null)
        return candidatesView
    }
}
