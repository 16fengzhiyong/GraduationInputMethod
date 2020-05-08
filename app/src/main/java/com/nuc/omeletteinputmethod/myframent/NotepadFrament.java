package com.nuc.omeletteinputmethod.myframent;

import android.os.Bundle;

import com.nuc.omeletteinputmethod.R;

public class NotepadFrament extends LazyFrament {
    @Override
    public void fetchData() {

    }
    @Override
    protected int getLayoutId() {
        return R.layout.setting_notepad_layout;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {

    }
    @Override
    protected void initWidgetActions() {

    }



    private static NotepadFrament fragment = null;
    public static NotepadFrament newInstance() {
        if (fragment == null){
            fragment = new NotepadFrament();
        }
        return fragment;
    }
}
