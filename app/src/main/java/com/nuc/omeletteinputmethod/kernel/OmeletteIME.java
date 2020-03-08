package com.nuc.omeletteinputmethod.kernel;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.view.LayoutInflater;
import android.view.View;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.SettingsActivity;
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil;

public class OmeletteIME extends InputMethodService {
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

    @Override
    public void onCreate() {
        super.onCreate();
        keyboardSwisher = new KeyboardSwisher(this);
    }

    @Override
	public View onCreateInputView() {
		return keyboardSwisher.choseKeyboard();
	}
	
	@Override
	public View onCreateCandidatesView() {
		return null;
	}
	
	public void commitText(String data) {
		getCurrentInputConnection().commitText(data, 0); // 往输入框输出内容
		setCandidatesViewShown(false); // 隐藏 CandidatesView
	}
	
	public void deleteText(){
		getCurrentInputConnection().deleteSurroundingText(1, 0);
	}
	
	public void hideInputMethod() {
		hideWindow();
	}
}
