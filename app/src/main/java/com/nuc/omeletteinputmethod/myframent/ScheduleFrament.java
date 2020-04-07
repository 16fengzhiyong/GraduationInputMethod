package com.nuc.omeletteinputmethod.myframent;

import android.os.Bundle;
import android.view.View;

import com.nuc.omeletteinputmethod.R;
import com.othershe.calendarview.bean.DateBean;
import com.othershe.calendarview.listener.OnPagerChangeListener;
import com.othershe.calendarview.weiget.CalendarView;

public class ScheduleFrament extends LazyFrament {
    @Override
    public void fetchData() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.setting_schedule_layout;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        CalendarView calendarView = (CalendarView) rootView.findViewById(R.id.test_rili);
        //日历init，年月日之间用点号隔开
        calendarView
                .setStartEndDate("2010.7", "2020.4")
                .setInitDate("2020.4")
                .setSingleDate("2020.4.5")
                .init();
        //月份切换回调
        calendarView.setOnPagerChangeListener(new OnPagerChangeListener() {
            @Override
            public void onPagerChanged(int[] date) {

            }
        });
    }

    @Override
    protected void initWidgetActions() {

    }


    private static ScheduleFrament fragment = null;
    public static ScheduleFrament newInstance() {
        if (fragment == null){
            fragment = new ScheduleFrament();
        }
        return fragment;
    }
}
