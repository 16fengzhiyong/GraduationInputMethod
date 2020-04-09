package com.nuc.omeletteinputmethod

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.viewpager.widget.ViewPager

/**
 * 重载接口增加设置主界面是否可以进行滑动
 * Created by Jorstin on 2015/3/18.
 */
class CCPCustomViewPager : ViewPager {

    /**
     * 控制页面是否可以左右滑动
     */
    private var mSlidenabled = true

    /**
     * @param context
     */
    constructor(context: Context) : super(context) {}

    /**
     * @param context
     * @param attrs
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    /**
     * 设置是否可以滑动
     */
    fun setSlideEnabled(enabled: Boolean) {
        mSlidenabled = enabled
    }

    override fun onInterceptTouchEvent(arg0: MotionEvent): Boolean {

        return if (!mSlidenabled) {
            false
        } else super.onInterceptTouchEvent(arg0)
    }

    override fun onTouchEvent(arg0: MotionEvent): Boolean {

        return if (!mSlidenabled) {
            false
        } else super.onTouchEvent(arg0)
    }

}

