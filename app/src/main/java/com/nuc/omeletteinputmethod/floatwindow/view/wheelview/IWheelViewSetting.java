package com.nuc.omeletteinputmethod.floatwindow.view.wheelview;


import androidx.annotation.ColorInt;

public interface IWheelViewSetting {

    void setTextSize(float textSize);

    void setTextColor(@ColorInt int textColor);

    void setShowCount(int showCount);

    void setTotalOffsetX(int totalOffsetX);

    void setItemVerticalSpace(int itemVerticalSpace);

    void setItems(IWheel[] items);

    int getSelectedIndex();

    void setSelectedIndex(int targetIndexPosition);

    void setSelectedIndex(int targetIndexPosition, boolean withAnimation);

    void setOnSelectedListener(WheelView.OnSelectedListener onSelectedListener);

    boolean isScrolling();
}
