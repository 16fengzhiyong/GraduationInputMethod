package com.nuc.omeletteinputmethod.kernel;

import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.SettingsActivity;
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil;

public class KeyboardSwisher {
    OmeletteIME omeletteIME;
    public KeyboardSwisher(){ }
    public KeyboardSwisher(OmeletteIME omeletteIME){
        this.omeletteIME = omeletteIME;
    }
    public View choseKeyboard(){
        View mkeyView = null;
        if (SettingsActivity.showMyselfkeyboard ==false){
            mkeyView = LayoutInflater.from(omeletteIME).inflate(
                    R.layout.layout_keyboardview, null);
            new KeyboardUtil(omeletteIME, (KeyboardView) mkeyView.findViewById(R.id.keyboardView));
            omeletteIME.setCandidatesViewShown(true);
        }else {
            mkeyView = LayoutInflater.from(omeletteIME).inflate(
                    R.layout.layout_input_test, null);
            TextView textView = mkeyView.findViewById(R.id.change_kk_test);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("KeyboardSwisher", "onClick: ");
                    SettingsActivity.showMyselfkeyboard = false;
                    omeletteIME.onCreateInputView();
                    omeletteIME.setCandidatesViewShown(true);
                }
            });
        }
        return mkeyView;
    }
}
