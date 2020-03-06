package com.nuc.omeletteinputmethod.kernel;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.view.LayoutInflater;
import android.view.View;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil;

public class OmeletteIME extends InputMethodService {

	@Override
	public View onCreateInputView() {
		View mkeyView = LayoutInflater.from(this).inflate(
				R.layout.layout_keyboardview, null);
		new KeyboardUtil(this, (KeyboardView) mkeyView.findViewById(R.id.keyboardView));
		return mkeyView;
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
