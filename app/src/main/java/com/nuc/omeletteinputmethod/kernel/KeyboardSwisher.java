package com.nuc.omeletteinputmethod.kernel;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.adapters.FirstViewAdapter;
import com.nuc.omeletteinputmethod.entityclass.CandidatesEntity;
import com.nuc.omeletteinputmethod.floatwindow.view.SymbolScrollChooseView;
import com.nuc.omeletteinputmethod.floatwindow.view.SymbolScrollView;
import com.nuc.omeletteinputmethod.kernel.keyboard.KeyboardBuider;
import com.nuc.omeletteinputmethod.kernel.keyboard.KeyboardHeadListener;
import com.nuc.omeletteinputmethod.kernel.keyboard.KeyboardState;
import com.nuc.omeletteinputmethod.kernel.keyboard.MyKeyboardView;
import com.nuc.omeletteinputmethod.util.SymbolsManager;
import com.nuc.omeletteinputmethod.util.TranslateCallback;
import com.nuc.omeletteinputmethod.util.TranslateUtil;

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

    TextView translatelateTextView = null;

    LinearLayout symbolLinearLayout;


    public KeyboardSwisher(){

    }
    public KeyboardSwisher(OmeletteIME omeletteIME){
        this.omeletteIME = omeletteIME;
    }
    LinearLayoutManager layoutManager = new LinearLayoutManager(omeletteIME);

    public View choseKeyboard(){
        View mkeyView = null;
        //将此部分移到KeyboardBuider中 同时在里面实现 键盘切换方法
        // 实现方向转化方
        mkeyView = LayoutInflater.from(omeletteIME).inflate(
                R.layout.mytestkeyboardlayout, null);
        translatelateTextView =mkeyView.findViewById(R.id.id_textview_candidates_translate);
        myKeyboardView = mkeyView.findViewById(R.id.test_mykeyboardview);
        myKeyboardView.SetMyKeyboardView(omeletteIME,R.xml.nomal_qwerty);
        KeyboardState.getInstance().setWitchKeyboardNow(KeyboardState.PINYIN_26_KEY_KEYBOARD);
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
        mkeyView.findViewById(R.id.id_symbol_return).setOnClickListener(keyboardHeadListener);

        symbolLinearLayout = mkeyView.findViewById(R.id.id_symbol_choose_scroll_partent);
        final SymbolsManager symbolsManager = new SymbolsManager(omeletteIME);
        final SymbolScrollView symbolScrollView = mkeyView.findViewById(R.id.mytest);
        symbolScrollView.setOmeletteIME(omeletteIME);
        SymbolScrollChooseView symbolScrollChooseView = mkeyView.findViewById(R.id.id_symbol_choose_scroll);
        final String[] choseStr =new String[]{"表情","数学","部首","特殊","网络","俄语","语音的","数字","注音","日语","希腊"};
        symbolScrollChooseView.setTitles(choseStr);
        // 表情","数学","部首","特殊","网络","俄语","语音的","数字","注音","日语","希腊
        symbolScrollChooseView.setOnScrollEndListener(new SymbolScrollChooseView.OnScrollEndListener() {
            @Override
            public void currentPosition(int position) {
                Log.d("返回 信息为", "当前positin=" + position + " " + choseStr[position]);
                switch (position){
                    case 0 :symbolScrollView.setTitles(symbolsManager.SMILE);
                        break;
                    case 1 :symbolScrollView.setTitles(symbolsManager.MATH);
                        break;
                    case 2 :symbolScrollView.setTitles(symbolsManager.BU_SHOU);
                        break;
                    case 3 :symbolScrollView.setTitles(symbolsManager.SPECIAL);
                        break;
                    case 4 :symbolScrollView.setTitles(symbolsManager.NET);
                        break;
                    case 5 :symbolScrollView.setTitles(symbolsManager.RUSSIAN);
                        break;
                    case 6 :symbolScrollView.setTitles(symbolsManager.PHONETIC);
                        break;
                    case 7 :symbolScrollView.setTitles(symbolsManager.NUMBER);
                        break;
                    case 8 :symbolScrollView.setTitles(symbolsManager.BOPOMOFO);
                        break;
                    case 9 :symbolScrollView.setTitles(symbolsManager.JAPANESE);
                        break;
                    case 10 :symbolScrollView.setTitles(symbolsManager.GREECE);
                        break;
                }

            }
        });
        symbolScrollView.setTitles(symbolsManager.BU_SHOU);

        translatelateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatelateTextView.setVisibility(View.GONE);
                omeletteIME.getCurrentInputConnection().commitText(translatelateTextView.getText().toString(),0);

            }
        });
         translateCallback = new TranslateCallback() {
            @Override
            public void onTranslateDone(String result) {
                Log.i("翻译返回内容", "onTranslateDone: "+result+"开始显示");
                translatelateTextView.setVisibility(View.VISIBLE);
                translatelateTextView.setText(result);
                // result是翻译结果，在这里使用翻译结果，比如使用对话框显示翻译结果
                Log.i("翻译返回内容", "onTranslateDone: "+result);
            }
        };
        return mkeyView;
//        return setWaitView(mkeyView);
    }



    public void checkSymbolKeyboard(){
        if (KeyboardState.SYMBOL_KEYBOARD==KeyboardState.getInstance().getWitchKeyboardNow()){
            myKeyboardView.setVisibility(View.VISIBLE);
            KeyboardState.getInstance().setWitchKeyboardNow(KeyboardState.getInstance().getWitchKeyboardLast());
            KeyboardState.getInstance().setWitchKeyboardLast(KeyboardState.SYMBOL_KEYBOARD);
            symbolLinearLayout.setVisibility(View.GONE);
        }else {
            myKeyboardView.setVisibility(View.GONE);
            KeyboardState.getInstance().setWitchKeyboardLast(KeyboardState.getInstance().getWitchKeyboardNow());
            KeyboardState.getInstance().setWitchKeyboardNow(KeyboardState.SYMBOL_KEYBOARD);
            symbolLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    public void checkNumberKeyboard(){
        if (KeyboardState.NUMBER_9_KEY_KEYBOARD==KeyboardState.getInstance().getWitchKeyboardNow()){
            KeyboardState.getInstance().setWitchKeyboardNow(KeyboardState.getInstance().getWitchKeyboardLast());
            KeyboardState.getInstance().setWitchKeyboardLast(KeyboardState.NUMBER_9_KEY_KEYBOARD);
            myKeyboardView.SetMyKeyboardView(omeletteIME,R.xml.nomal_qwerty);
        }else {
            KeyboardState.getInstance().setWitchKeyboardLast(KeyboardState.getInstance().getWitchKeyboardNow());
            KeyboardState.getInstance().setWitchKeyboardNow(KeyboardState.NUMBER_9_KEY_KEYBOARD);
            myKeyboardView.SetMyKeyboardView(omeletteIME,R.xml.number_qwerty);
        }

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

    public TextView getTranslatelateTextView() {
        return translatelateTextView;
    }

    // 使用
    TranslateCallback translateCallback;
    public void translateToEnglish(final String valuestr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("翻译返回内容", "onLongClick: ");
                new TranslateUtil().translate(omeletteIME, "auto", "en", valuestr, translateCallback);
            }
        }).start();
       }
}
