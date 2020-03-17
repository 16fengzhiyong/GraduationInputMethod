package com.nuc.omeletteinputmethod.kernel;

import android.inputmethodservice.KeyboardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.SettingsActivity;
import com.nuc.omeletteinputmethod.kernel.keyboard.KeyboardBuider;
import com.nuc.omeletteinputmethod.kernel.keyboard.KeyboardHeadListener;
import com.nuc.omeletteinputmethod.kernel.keyboard.MyKeyboard;
import com.nuc.omeletteinputmethod.kernel.keyboard.MyKeyboardView;
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil;

public class KeyboardSwisher {
    OmeletteIME omeletteIME;
    KeyboardBuider keyboardBuider;

    public KeyboardSwisher(){

    }
    public KeyboardSwisher(OmeletteIME omeletteIME){
        this.omeletteIME = omeletteIME;
    }
    public View choseKeyboard(){
        View mkeyView = null;
        if (SettingsActivity.showMyselfkeyboard ==false){
            mkeyView = LayoutInflater.from(omeletteIME).inflate(
                    R.layout.layout_keyboardview, null);
            new KeyboardUtil(omeletteIME, (KeyboardView) mkeyView.findViewById(R.id.keyboardView));
            //omeletteIME.setCandidatesViewShown(true);
        }else {
            mkeyView = LayoutInflater.from(omeletteIME).inflate(
                    R.layout.layout_input_test, null);
            TextView textView = mkeyView.findViewById(R.id.change_kk_test);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Log.i("KeyboardSwisher", "onClick: ");
//                    SettingsActivity.showMyselfkeyboard = false;
//                    omeletteIME.onCreateInputView();
//                    omeletteIME.setCandidatesViewShown(true);
                    MyKeyboard myKeyboard = new MyKeyboard(omeletteIME,R.xml.nomal_qwerty);
                }
            });
        }
        //将此部分移到KeyboardBuider中 同时在里面实现 键盘切换方法
        // 实现方向转化方法
        MyKeyboardView myKeyboardView;
        mkeyView = LayoutInflater.from(omeletteIME).inflate(
                R.layout.mytestkeyboardlayout, null);
        myKeyboardView = mkeyView.findViewById(R.id.test_mykeyboardview);
        myKeyboardView.SetMyKeyboardView(omeletteIME,R.xml.nomal_qwerty);
        ///omeletteIME.setCandidatesViewShown(true);

        KeyboardHeadListener keyboardHeadListener = new KeyboardHeadListener(omeletteIME,mkeyView,myKeyboardView);
        mkeyView.findViewById(R.id.linearLayout_candidates_setting).setOnClickListener(keyboardHeadListener);
        mkeyView.findViewById(R.id.id_linear_hide_keyboard).setOnClickListener(keyboardHeadListener);
        mkeyView.findViewById(R.id.id_linear_pinyin_input).setOnClickListener(keyboardHeadListener);
        mkeyView.findViewById(R.id.id_linear_english_input).setOnClickListener(keyboardHeadListener);
        mkeyView.findViewById(R.id.id_linear_arrows_input).setOnClickListener(keyboardHeadListener);
        return mkeyView;
//        return setWaitView(mkeyView);
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
