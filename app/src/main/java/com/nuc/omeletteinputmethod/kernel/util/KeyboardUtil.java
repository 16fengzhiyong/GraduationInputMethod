package com.nuc.omeletteinputmethod.kernel.util;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;

import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.kernel.OmeletteIME;

public class KeyboardUtil {

	private KeyboardView keyboardView;
	private OmeletteIME omeletteIME;
	private Keyboard k1;// 字母键盘

	public KeyboardUtil(OmeletteIME omeletteIME1, KeyboardView keyboardView1) {
		super();
		keyboardView = keyboardView1;
		keyboardView.setOnKeyboardActionListener(listener);
		omeletteIME = omeletteIME1;
		k1 = new Keyboard(omeletteIME.getApplicationContext(), R.xml.qwerty);
		keyboardView.setKeyboard(k1);
		keyboardView.setEnabled(true);
		keyboardView.setPreviewEnabled(true);
	}

	private OnKeyboardActionListener listener = new OnKeyboardActionListener() {

		@Override
		public void swipeUp() {
		}

		@Override
		public void swipeRight() {
		}

		@Override
		public void swipeLeft() {
		}

		@Override
		public void swipeDown() {
		}

		@Override
		public void onText(CharSequence text) {
		}

		@Override
		public void onRelease(int primaryCode) {
		}

		@Override
		public void onPress(int primaryCode) {
		}

		@Override
		public void onKey(int primaryCode, int[] keyCodes) {
			switch (primaryCode) {
			case Keyboard.KEYCODE_DELETE:
				omeletteIME.deleteText();
				break;
			case Keyboard.KEYCODE_CANCEL:
				omeletteIME.hideInputMethod();
				break;
			default:
				omeletteIME.commitText(Character.toString((char) primaryCode));
				break;
			}
		}
	};

}
