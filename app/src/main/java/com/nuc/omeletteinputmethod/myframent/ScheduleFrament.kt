package com.nuc.omeletteinputmethod.myframent

import android.os.Bundle
import android.view.View

import com.nuc.omeletteinputmethod.R
import com.othershe.calendarview.bean.DateBean
import com.othershe.calendarview.listener.OnPagerChangeListener
import com.othershe.calendarview.weiget.CalendarView

class ScheduleFrament : LazyFrament() {

    protected override val layoutId: Int
        get() = R.layout.setting_schedule_layout

    override fun fetchData() {

    }

    override fun initView(savedInstanceState: Bundle?) {
        val calendarView = rootView!!.findViewById<View>(R.id.test_rili) as CalendarView
        //日历init，年月日之间用点号隔开
        calendarView
                .setStartEndDate("2010.7", "2020.4")
                .setInitDate("2020.4")
                .setSingleDate("2020.4.5")
                .init()
        //月份切换回调
        calendarView.setOnPagerChangeListener { }
    }

    override fun initWidgetActions() {

    }

    companion object {


        private var fragment: ScheduleFrament? = null
        fun newInstance(): ScheduleFrament {
            if (fragment == null) {
                fragment = ScheduleFrament()
            }
            return fragment as ScheduleFrament
        }
    }
}
