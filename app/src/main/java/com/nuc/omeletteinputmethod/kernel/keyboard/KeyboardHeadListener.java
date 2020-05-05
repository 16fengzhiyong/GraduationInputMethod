package com.nuc.omeletteinputmethod.kernel.keyboard;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.kernel.OmeletteIME;
/**
 * 用于顶部点击按钮的事件处理
 */
public class KeyboardHeadListener implements View.OnClickListener {
    OmeletteIME omeletteIME;

    MyKeyboardView myKeyboardView;
    View mkeyView;
    LinearLayout setting;
    public KeyboardHeadListener(OmeletteIME omeletteIME, View mkeyView, MyKeyboardView myKeyboardView){
        this.omeletteIME = omeletteIME;
        this.mkeyView = mkeyView;
        this.myKeyboardView = myKeyboardView;
    }

    @Override
    public void onClick(View view) {
        Log.i("KeyboardHeadListener", "onClick: view.getId() = "+view.getId());
        switch (view.getId()){

            case R.id.linearLayout_candidates_setting:
                Log.i("KeyboardHeadListener", "onClick: id_linear_setting_view");
                chengeSettingView();
                break;
            case R.id.id_linear_hide_keyboard:
                Log.i("KeyboardHeadListener", "onClick: id_linear_hide_keyboard");
                hideKeyboard();
                break;
            case R.id.id_linear_pinyin_input:

                break;
            case R.id.id_linear_english_input:

                break;
            case R.id.id_symbol_return:
                Log.i("点击了字符界面的返回", "onClick: ");
                omeletteIME.getKeyboardSwisher().checkSymbolKeyboard();
                break;
            case R.id.id_linear_arrows_input:
                checkArrowsKeyboard();
                Log.i("KeyboardHeadListener", "onClick: id_linear_arrows_input");
                break;
                default:
                    Toast.makeText(omeletteIME,"要不，你再重点一下",Toast.LENGTH_SHORT).show();
        }
    }
    private void chengeSettingView(){
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
    private void checkArrowsKeyboard(){
        if (KeyboardState.ARROWS_KEYBOARD==KeyboardState.getInstance().getWitchKeyboardNow()){
            ((ImageView)mkeyView.findViewById(R.id.id_img_arrows_input)).setImageResource(R.drawable.all_around);
            KeyboardState.getInstance().setWitchKeyboardNow(KeyboardState.getInstance().getWitchKeyboardLast());
            KeyboardState.getInstance().setWitchKeyboardLast(KeyboardState.ARROWS_KEYBOARD);
            myKeyboardView.invalidate();
        }else {
            ((ImageView)mkeyView.findViewById(R.id.id_img_arrows_input)).setImageResource(R.drawable.all_around_press);
            KeyboardState.getInstance().setWitchKeyboardLast(KeyboardState.getInstance().getWitchKeyboardNow());
            KeyboardState.getInstance().setWitchKeyboardNow(KeyboardState.ARROWS_KEYBOARD);
            myKeyboardView.invalidate();
        }
    }



    private void hideKeyboard(){
        omeletteIME.hideInputMethod();
    }
//    public void initView(){
//        myKeyboardView = mkeyView.findViewById(R.id.test_mykeyboardview);
//        myKeyboardView.SetMyKeyboardView(omeletteIME,R.xml.nomal_qwerty);
//        //omeletteIME.setCandidatesViewShown(true);
//        setting = mkeyView.findViewById(R.id.linearLayout_candidates_setting);
//    }
//
//    public View setSettingView(){
//        setting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (KeyboardState.SETTING_KEYBOARD==KeyboardState.getInstance().getWitchKeyboardNow()){
//                    myKeyboardView.setVisibility(View.VISIBLE);
//                    mkeyView.findViewById(R.id.id_linear_setting_view).setVisibility(View.GONE);
//                    KeyboardState.getInstance().setWitchKeyboardNow(KeyboardState.ENGLISH_26_KEY_KEYBOARD);
//                }else {
//                    myKeyboardView.setVisibility(View.GONE);
//                    mkeyView.findViewById(R.id.id_linear_setting_view).setVisibility(View.VISIBLE);
//                    KeyboardState.getInstance().setWitchKeyboardNow(KeyboardState.SETTING_KEYBOARD);
//                }
//
//            }
//        });
//        return mkeyView;
//    }
}
