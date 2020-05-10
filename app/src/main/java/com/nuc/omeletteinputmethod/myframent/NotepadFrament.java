package com.nuc.omeletteinputmethod.myframent;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.nuc.omeletteinputmethod.DBoperation.DBManage;
import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.ShortInputActivity;
import com.nuc.omeletteinputmethod.adapters.NotepadAdapter;
import com.nuc.omeletteinputmethod.entityclass.NotepadEntity;
import com.nuc.omeletteinputmethod.floatwindow.FloatingWindowDisplayService;

import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotepadFrament extends LazyFrament {
    RecyclerView recyclerView ;
    DBManage dbManage ;
    ArrayList<NotepadEntity> notepadEntityArrayList = new ArrayList<>();
    ImageView addImageView ;
    Button takePutButton;
    EditText editText;

    @Override
    public void fetchData() {

    }
    @Override
    protected int getLayoutId() {
        return R.layout.setting_notepad_layout;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView = rootView.findViewById(R.id.id_setting_notepad_list);
        addImageView = rootView.findViewById(R.id.id_setting_notepad_add_imageview);
        takePutButton = rootView.findViewById(R.id.id_setting_notepad_add_ok_Button);
        editText = rootView.findViewById(R.id.id_setting_notepad_add_EditText);

        recyclerView.setLayoutManager(layoutManager);
        dbManage = FloatingWindowDisplayService.getDbManage();
        notepadEntityArrayList = dbManage.getNotepad();
        recyclerView.setAdapter(new NotepadAdapter(notepadEntityArrayList,getContext()));
        addImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rootView.findViewById(R.id.id_setting_notepad_add_RelativeLayout).getVisibility() == View.GONE) {
                    rootView.findViewById(R.id.id_setting_notepad_add_RelativeLayout).setVisibility(View.VISIBLE);
                } else {
                    rootView.findViewById(R.id.id_setting_notepad_add_RelativeLayout).setVisibility(View.GONE);
                }
                // InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            }
        });
        takePutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put("text",editText.getText().toString());
                Log.i("添加成功", "onClick: insert" +
                dbManage.getMyDBHelper().insert("notepad", null, values));
                Toast.makeText(getContext(),"添加成功",Toast.LENGTH_SHORT).show();
                notepadEntityArrayList = dbManage.getNotepad();
                Log.i("添加成功", "onClick: notepadEntityArrayList" + notepadEntityArrayList.size());
                recyclerView.setAdapter(new NotepadAdapter(notepadEntityArrayList,getContext()));
                rootView.findViewById(R.id.id_setting_notepad_add_RelativeLayout).setVisibility(View.GONE);
            }
        });

        rootView.findViewById(R.id.id_setting_notepad_add_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootView.findViewById(R.id.id_setting_notepad_add_RelativeLayout).setVisibility(View.GONE);
            }
        });
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
