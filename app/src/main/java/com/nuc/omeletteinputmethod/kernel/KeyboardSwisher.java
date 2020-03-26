package com.nuc.omeletteinputmethod.kernel;

import android.inputmethodservice.KeyboardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nuc.omeletteinputmethod.MainActivity;
import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.SettingsActivity;
import com.nuc.omeletteinputmethod.adapters.FirstViewAdapter;
import com.nuc.omeletteinputmethod.entityclass.CandidatesEntity;
import com.nuc.omeletteinputmethod.kernel.keyboard.KeyboardBuider;
import com.nuc.omeletteinputmethod.kernel.keyboard.KeyboardHeadListener;
import com.nuc.omeletteinputmethod.kernel.keyboard.MyKeyboard;
import com.nuc.omeletteinputmethod.kernel.keyboard.MyKeyboardView;
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil;

import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class KeyboardSwisher {
    OmeletteIME omeletteIME;
    KeyboardBuider keyboardBuider;
    LinearLayout candidatesLayout;
    RecyclerView mRecyclerView;
    RelativeLayout chooseBar;
    MyKeyboardView myKeyboardView;
    TextView textviewCandidatesPinyin;
    public KeyboardSwisher(){

    }
    public KeyboardSwisher(OmeletteIME omeletteIME){
        this.omeletteIME = omeletteIME;
    }
    LinearLayoutManager layoutManager = new LinearLayoutManager(omeletteIME);

    public View choseKeyboard(){
        View mkeyView = null;
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
                R.layout.mytestkeyboardlayout, null);
        myKeyboardView = mkeyView.findViewById(R.id.test_mykeyboardview);
        myKeyboardView.SetMyKeyboardView(omeletteIME,R.xml.nomal_qwerty);
        ///omeletteIME.setCandidatesViewShown(true);
        mRecyclerView=(RecyclerView)mkeyView.findViewById(R.id.id_recyclerview_candidates);
        candidatesLayout = (LinearLayout)mkeyView.findViewById(R.id.id_linear_candidates) ;
        textviewCandidatesPinyin =(TextView)mkeyView.findViewById(R.id.id_textview_candidates_pinyin);
        ReadyCandidatesView();
        KeyboardHeadListener keyboardHeadListener = new KeyboardHeadListener(omeletteIME,mkeyView,myKeyboardView);
        chooseBar = mkeyView.findViewById(R.id.id_relativeLayout_candidates_choose);
        mkeyView.findViewById(R.id.linearLayout_candidates_setting).setOnClickListener(keyboardHeadListener);
        mkeyView.findViewById(R.id.id_linear_hide_keyboard).setOnClickListener(keyboardHeadListener);
        mkeyView.findViewById(R.id.id_linear_pinyin_input).setOnClickListener(keyboardHeadListener);
        mkeyView.findViewById(R.id.id_linear_english_input).setOnClickListener(keyboardHeadListener);
        mkeyView.findViewById(R.id.id_linear_arrows_input).setOnClickListener(keyboardHeadListener);
        return mkeyView;
//        return setWaitView(mkeyView);
    }
    public void ReadyCandidatesView(){
        //调整RecyclerView的排列方向
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
    }
    public void showCandidatesView(ArrayList<CandidatesEntity> candidatesEntityArrayList,String pinyin){
        chooseBar.setVisibility(View.GONE);
        candidatesLayout.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        textviewCandidatesPinyin.setVisibility(View.VISIBLE);
        textviewCandidatesPinyin.setText(pinyin);
        mRecyclerView.setAdapter(new FirstViewAdapter(omeletteIME, candidatesEntityArrayList,myKeyboardView));
    }
    public void hideCandidatesView(){
        candidatesLayout.setVisibility(View.GONE);
        chooseBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mRecyclerView.removeAllViews();
        if (mRecyclerView.getAdapter()!=null){
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
        textviewCandidatesPinyin.setVisibility(View.GONE);
    }
    public void enterInputPinYin(String pinyin){
        omeletteIME.commitText(pinyin);
    }
    public View setWaitView(View mkeyView){
        final MyKeyboardView myKeyboardView;
        //mkeyView = new MyKeyboardView(omeletteIME,R.xml.nomal_qwerty);
        mkeyView = LayoutInflater.from(omeletteIME).inflate(
                R.layout.mytestkeyboardlayout, null);
        myKeyboardView = mkeyView.findViewById(R.id.test_mykeyboardview);
        myKeyboardView.SetMyKeyboardView(omeletteIME,R.xml.nomal_qwerty);
        ///omeletteIME.setCandidatesViewShown(true);
        KeyboardHeadListener keyboardHeadListener = new KeyboardHeadListener(omeletteIME,mkeyView,myKeyboardView);
        mkeyView.setOnClickListener(keyboardHeadListener);
        return mkeyView;
    }
    public View choseCandidatesView(){
        View candidatesView;
        candidatesView = RelativeLayout.inflate(omeletteIME,R.layout.input_waiting_layout, null);
        return candidatesView;
    }
}
