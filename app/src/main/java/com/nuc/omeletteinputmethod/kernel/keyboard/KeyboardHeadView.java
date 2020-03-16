package com.nuc.omeletteinputmethod.kernel.keyboard;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.kernel.OmeletteIME;

public class KeyboardHeadView {
    OmeletteIME omeletteIME;

    MyKeyboardView myKeyboardView;
    View mkeyView;
    LinearLayout setting;
    public KeyboardHeadView(OmeletteIME omeletteIME,View mkeyView){
        this.omeletteIME = omeletteIME;
        this.mkeyView = mkeyView;
        initView();
    }

    public void initView(){
        myKeyboardView = mkeyView.findViewById(R.id.test_mykeyboardview);
        myKeyboardView.SetMyKeyboardView(omeletteIME,R.xml.nomal_qwerty);
        //omeletteIME.setCandidatesViewShown(true);
        setting = mkeyView.findViewById(R.id.linearLayout_candidates_setting);
    }

    public View setSettingView(){
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (KeyboardState.SETTING_KEYBOARD==KeyboardState.getInstance().getWitchKeyboardNow()){
                    myKeyboardView.setVisibility(View.VISIBLE);
                    mkeyView.findViewById(R.id.id_linear_setting_view).setVisibility(View.GONE);
                    KeyboardState.getInstance().setWitchKeyboardNow(KeyboardState.ENGLISH_26_KEY_KEYBOARD);
                }else {
                    myKeyboardView.setVisibility(View.GONE);
                    mkeyView.findViewById(R.id.id_linear_setting_view).setVisibility(View.VISIBLE);
                    KeyboardState.getInstance().setWitchKeyboardNow(KeyboardState.SETTING_KEYBOARD);
                }

            }
        });
        return mkeyView;
    }
}
