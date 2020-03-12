package com.nuc.omeletteinputmethod.kernel.util;

import android.content.res.TypedArray;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.util.TypedValue;

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

	public static int getDimensionOrFraction(TypedArray a, int index, int base, int defValue) {
		TypedValue value = a.peekValue(index);
		if (value == null) return defValue;
		if (value.type == TypedValue.TYPE_DIMENSION) {
			return a.getDimensionPixelOffset(index, defValue);
		} else if (value.type == TypedValue.TYPE_FRACTION) {
			// Round it to avoid values like 47.9999 from getting truncated
			return Math.round(a.getFraction(index, base, base, defValue));
		}
		return defValue;
	}
}
