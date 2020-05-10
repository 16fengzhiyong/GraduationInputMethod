package com.nuc.omeletteinputmethod.floatwindow.schedule;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.internal.$Gson$Preconditions;
import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.ShortInputActivity;
import com.nuc.omeletteinputmethod.adapters.FloatShortInputAdapter;
import com.nuc.omeletteinputmethod.entityclass.FloatShortInputEntity;
import com.nuc.omeletteinputmethod.entityclass.ScheduleEntity;
import com.nuc.omeletteinputmethod.floatwindow.FloatingWindowDisplayService;
import com.nuc.omeletteinputmethod.floatwindow.view.wheelview.WheelItem;
import com.nuc.omeletteinputmethod.floatwindow.view.wheelview.WheelItemView;
import com.nuc.omeletteinputmethod.floatwindow.view.wheelview.WheelView;

import java.util.ArrayList;
import java.util.Locale;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 用于添加日程
 */
public class Schedule {

    Context mContext;
    WindowManager windowManager;
    WindowManager.LayoutParams layoutParams;
    View displayView;
    WheelItemView wheelViewLeft;
    WheelItemView wheelViewCenter;
    WheelItemView wheelViewRight;
    int[] years = new int[30];
    int[] months = new int[12];
    int[] days;
    int[] tian31= new int[]{1,3,5,7,8,10,12};
    int selectYear,selectMonth,selectDay;

    WheelView.OnSelectedListener listener = new WheelView.OnSelectedListener() {
        @Override
        public void onSelected(Context context, int selectedIndex) {
            Log.i("选择时间为", "onSelected: "+wheelViewLeft.getSelectedIndex()+"  "+
                    wheelViewCenter.getSelectedIndex()+"  "+wheelViewRight.getSelectedIndex());

            switch (wheelViewCenter.getSelectedIndex()+1 ){
                case 2:
                    Log.i("选择时间为", "day 2");
                    if (years[wheelViewLeft.getSelectedIndex()]%4==0){
                        if (years[wheelViewLeft.getSelectedIndex()]%100==0&&years[wheelViewLeft.getSelectedIndex()-1]%400!=0){

                        }else {
                            days = new int[29];
                            for (int i = 0; i <29 ; i++) {
                                days[i] = 1+i;
                            }
                        }
                        loadData(wheelViewRight,  "日",29);
                    }else {
                        days = new int[28];
                        for (int i = 0; i <28 ; i++) {
                            days[i] = 1+i;
                        }
                        loadData(wheelViewRight,  "日",28);
                    }
                    break;
                default:
                    for (int tian:tian31){
                        Log.i("选择时间为", "tian ="+tian);
                        if (tian == (wheelViewCenter.getSelectedIndex()+1)){
                            Log.i("选择时间为", "day 1|3|5|7|8|10|12");
                            days = new int[31];
                            for (int i = 0; i <31 ; i++) {
                                days[i] = 1+i;
                            }
                            loadData(wheelViewRight,  "日",31);
                            selectYear = years[wheelViewLeft.getSelectedIndex()];
                            selectMonth = months[wheelViewCenter.getSelectedIndex()];
                            try {
                                selectDay = days[wheelViewRight.getSelectedIndex()];
                            }catch (Exception e){
                                selectDay = 29;
                            }
                            return;
                        }
                    }
                    days = new int[30];
                    for (int i = 0; i <30 ; i++) {
                        days[i] = 1+i;
                    }
                    loadData(wheelViewRight,  "日",30);
                    break;


            }
            selectYear = years[wheelViewLeft.getSelectedIndex()];
            selectMonth = months[wheelViewCenter.getSelectedIndex()];
            try {
                selectDay = days[wheelViewRight.getSelectedIndex()];
            }catch (Exception e){
                selectDay = 29;
            }

        }
    };


    public Schedule(Context mContext, WindowManager mWindowManager, WindowManager.LayoutParams layoutParams, View displayView) {
        this.mContext = mContext;
        this.windowManager = mWindowManager;
        this.layoutParams = layoutParams;
        this.displayView = displayView;
    }
    public void removeView(View zhankai){
        windowManager.removeView(zhankai);
    }
    public void showSchedule(){
        try {
            for (int i = 0; i < 30; i++) {
                years[i] = 2020+i;
            }
            for (int i = 0; i < 12; i++) {
                months[i] = 1+i;
            }
            days = new int[30];
            for (int i = 0; i <30 ; i++) {
                days[i] = 1+i;
            }

            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            displayView.findViewById(R.id.id_float_schedule_parent).setVisibility(View.VISIBLE);
            displayView.findViewById(R.id.id_float_schedule_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String savestr = ((EditText)displayView.findViewById(R.id.id_float_schedule_edit)).getText().toString();
                    ContentValues values = new ContentValues();
                    if (selectDay<10){
                        values.put("time",selectYear+"-"+selectMonth+"-0"+selectDay);
                    }else values.put("time",selectYear+"-"+selectMonth+"-"+selectDay);
                    values.put("info",savestr);
                    FloatingWindowDisplayService.getDbManage().getMyDBHelper().insert("schedule", null, values);
                    Toast.makeText(mContext,"添加成功",Toast.LENGTH_SHORT).show();
                    for (ScheduleEntity s:FloatingWindowDisplayService.getDbManage().getAllSchedule()) {
                        Log.i("所有的日程", "onClick: "+s.toString());

                    }
                }
            });
            //layoutParams.flags = 16777256;
            displayView.findViewById(R.id.id_float_schedule_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    layoutParams.flags = 16777256;
                    displayView.findViewById(R.id.id_float_schedule_parent).setVisibility(View.GONE);
                    windowManager.updateViewLayout(displayView,layoutParams);
                    return;
                }
            });
            layoutParams.flags = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
            wheelViewLeft = displayView.findViewById(R.id.wheel_view_left);
            wheelViewCenter = displayView.findViewById(R.id.wheel_view_center);
            wheelViewRight = displayView.findViewById(R.id.wheel_view_right);
            wheelViewLeft.setOnSelectedListener(listener);
            wheelViewCenter.setOnSelectedListener(listener);
            wheelViewRight.setOnSelectedListener(listener);
            loadData(wheelViewLeft, "年",30);
            loadData(wheelViewCenter, "月",12);
            loadData(wheelViewRight,  "日",30);
            windowManager.addView(displayView,layoutParams);

        }catch (Exception e){

            Log.e("日历错误", "showSchedule: ", e);
        }
    }
    private void loadData(WheelItemView wheelItemView, String label,int geshu) {
        WheelItem[] items = new WheelItem[geshu];
        if (label.equals("年")){
            for (int i = 0; i < years.length; i++) {
                items[i] = new WheelItem(years[i]+label);
            }
        }
        if (label.equals("月")){
            for (int i = 0; i < months.length; i++) {
                items[i] = new WheelItem(months[i]+label);
            }
        }
        if (label.equals("日")){
            for (int i = 0; i < days.length; i++) {
                items[i] = new WheelItem(days[i]+label);
            }
        }
//        for (int i = 0; i < geshu; i++) {
//            items[i] = new WheelItem(i+label);
//        }

        wheelItemView.setItems(items);
    }


}
