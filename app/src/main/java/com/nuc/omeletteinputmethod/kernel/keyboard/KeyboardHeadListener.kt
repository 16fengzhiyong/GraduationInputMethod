package com.nuc.omeletteinputmethod.kernel.keyboard

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast

import com.nuc.omeletteinputmethod.R
import com.nuc.omeletteinputmethod.kernel.OmeletteIME

/**
 * 用于顶部点击按钮的事件处理
 */
class KeyboardHeadListener(internal var omeletteIME: OmeletteIME, internal var mkeyView: View, internal var myKeyboardView: MyKeyboardView) : View.OnClickListener {
    internal var setting: LinearLayout? = null

    override fun onClick(view: View) {
        Log.i("KeyboardHeadListener", "onClick: view.getId() = " + view.id)
        when (view.id) {

            R.id.linearLayout_candidates_setting -> {
                Log.i("KeyboardHeadListener", "onClick: id_linear_setting_view")
                chengeSettingView()
            }
            R.id.id_linear_hide_keyboard -> {
                Log.i("KeyboardHeadListener", "onClick: id_linear_hide_keyboard")
                hideKeyboard()
            }
            R.id.id_linear_pinyin_input -> {
            }
            R.id.id_linear_english_input -> {
            }
            R.id.id_linear_arrows_input -> {
                checkArrowsKeyboard()
                Log.i("KeyboardHeadListener", "onClick: id_linear_arrows_input")
            }
            else -> Toast.makeText(omeletteIME, "要不，你再重点一下", Toast.LENGTH_SHORT).show()
        }
    }

    private fun chengeSettingView() {
        if (KeyboardState.SETTING_KEYBOARD == KeyboardState.witchKeyboardNow) {
            myKeyboardView.visibility = View.VISIBLE
            mkeyView.findViewById<View>(R.id.id_linear_setting_view).visibility = View.GONE
            KeyboardState.witchKeyboardNow = KeyboardState.ENGLISH_26_KEY_KEYBOARD
        } else {
            myKeyboardView.visibility = View.GONE
            mkeyView.findViewById<View>(R.id.id_linear_setting_view).visibility = View.VISIBLE
            KeyboardState.witchKeyboardNow = KeyboardState.SETTING_KEYBOARD
        }
    }

    private fun checkArrowsKeyboard() {
        if (KeyboardState.ARROWS_KEYBOARD == KeyboardState.witchKeyboardNow) {
            (mkeyView.findViewById<View>(R.id.id_img_arrows_input) as ImageView).setImageResource(R.drawable.all_around)
            KeyboardState.witchKeyboardNow = KeyboardState.witchKeyboardLast
            KeyboardState.witchKeyboardLast = KeyboardState.ARROWS_KEYBOARD
            myKeyboardView.invalidate()
        } else {
            (mkeyView.findViewById<View>(R.id.id_img_arrows_input) as ImageView).setImageResource(R.drawable.all_around_press)
            KeyboardState.witchKeyboardLast = KeyboardState.witchKeyboardNow
            KeyboardState.witchKeyboardNow = KeyboardState.ARROWS_KEYBOARD
            myKeyboardView.invalidate()
        }
    }

    private fun hideKeyboard() {
        omeletteIME.hideInputMethod()
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
