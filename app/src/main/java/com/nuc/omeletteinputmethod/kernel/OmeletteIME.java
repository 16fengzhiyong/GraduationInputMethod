package com.nuc.omeletteinputmethod.kernel;

import android.inputmethodservice.InputMethodService;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nuc.omeletteinputmethod.DBoperation.DBManage;
import com.nuc.omeletteinputmethod.entityclass.SinograFromDB;
import com.nuc.omeletteinputmethod.kernel.util.SinogramLibrary;

import java.util.ArrayList;
import java.util.Map;

public class OmeletteIME extends InputMethodService {
	public static boolean canShowWindow = false;
    /**
     * 屏幕宽度
     */
    private int mScreenWidth;
    /**
     * 屏幕高度
     */
    private int mScreenHeight;

    /**
     * 状态栏高度
     */
    private int mStatusBarHeight;

    private KeyboardSwisher keyboardSwisher;
    private DBManage dbManage = null;
    @Override
    public void onCreate() {
        super.onCreate();
        keyboardSwisher = new KeyboardSwisher(this);

		// /storage/emulated/0
		//File file = new File(basepath+"/zhengti2.txt");
		if (dbManage == null){
			Log.i("OmeletteIME", "onCreate: new 数据库");
			dbManage = new DBManage(this);
		}
    }

    @Override
	public View onCreateInputView() {
		return keyboardSwisher.choseKeyboard();
	}
	
	@Override
	public View onCreateCandidatesView() {
		return keyboardSwisher.choseCandidatesView();
	}
	
	public void commitText(String data) {
		getCurrentInputConnection().commitText(data.replace("'",""), 0); // 往输入框输出内容
		setCandidatesViewShown(false); // 隐藏 CandidatesView
	}
	
	public void deleteText(){
		getCurrentInputConnection().deleteSurroundingText(1, 0);
	}
	
	public void hideInputMethod() {
		hideWindow();
	}

	public KeyboardSwisher getKeyboardSwisher() {
		return keyboardSwisher;
	}

	public DBManage getDbManage() {
		return dbManage;
	}
}
