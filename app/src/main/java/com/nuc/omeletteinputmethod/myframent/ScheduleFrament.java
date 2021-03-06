package com.nuc.omeletteinputmethod.myframent;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ldf.calendar.Utils;
import com.ldf.calendar.component.CalendarAttr;
import com.ldf.calendar.component.CalendarViewAdapter;
import com.ldf.calendar.interf.OnSelectDateListener;
import com.ldf.calendar.model.CalendarDate;
import com.ldf.calendar.view.Calendar;
import com.ldf.calendar.view.MonthPager;
import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.ShortInputActivity;
import com.nuc.omeletteinputmethod.adapters.SettingScheduleAdapter;
import com.nuc.omeletteinputmethod.entityclass.ScheduleEntity;
import com.nuc.omeletteinputmethod.floatwindow.FloatingWindowDisplayService;
import com.nuc.omeletteinputmethod.setting.view.CustomDayView;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;


public class ScheduleFrament extends LazyFrament {

    TextView tvYear;
    TextView tvMonth;
    TextView backToday;
    CoordinatorLayout content;
    MonthPager monthPager;
    //RecyclerView rvToDoList;
    TextView scrollSwitch;
    TextView themeSwitch;
    TextView nextMonthBtn;
    TextView lastMonthBtn;
    RecyclerView scheduleList;
    Button takePutButton;

    int mYear;
    int mMonth;
    int mDay;



    private ArrayList<Calendar> currentCalendars = new ArrayList<>();
    private CalendarViewAdapter calendarAdapter;
    private OnSelectDateListener onSelectDateListener;
    private int mCurrentPage = MonthPager.CURRENT_DAY_INDEX;
    ImageView addImageView;
    private Context context;

    private CalendarDate currentDate;
    @Override
    public void fetchData() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.setting_schedule_layout;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        context = getContext();
        monthPager = (MonthPager) rootView.findViewById(R.id.calendar_view);
        //此处强行setViewHeight，毕竟你知道你的日历牌的高度
        monthPager.setViewHeight(Utils.dpi2px(getContext(), 270));
        tvYear = (TextView) rootView.findViewById(R.id.show_year_view);
        tvMonth = (TextView) rootView.findViewById(R.id.show_month_view);
        backToday = (TextView) rootView.findViewById(R.id.back_today_button);
        scrollSwitch = (TextView) rootView.findViewById(R.id.scroll_switch);
        themeSwitch = (TextView) rootView.findViewById(R.id.theme_switch);
        nextMonthBtn = (TextView) rootView.findViewById(R.id.next_month);
        lastMonthBtn = (TextView) rootView.findViewById(R.id.last_month);
        scheduleList = (RecyclerView)rootView.findViewById(R.id.id_setting_schedule_recycler);
        addImageView = rootView.findViewById(R.id.id_setting_schedule_add_imageview);
        takePutButton = rootView.findViewById(R.id.id_setting_schedule_add_ok_Button);
        scheduleList.setHasFixedSize(true);
        //这里用线性显示 类似于listview
        scheduleList.setLayoutManager(new LinearLayoutManager(getContext()));
        //rvToDoList = (RecyclerView) findViewById(R.id.list);
        initCurrentDate();
        initCalendarView();
        initToolbarClickListener();
    }
    @Override
    protected void initWidgetActions() {


    }

    /**
     * 初始化CustomDayView，并作为CalendarViewAdapter的参数传入
     */
    private void initCalendarView() {
        initListener();
        CustomDayView customDayView = new CustomDayView(getContext(), R.layout.custom_day);
        calendarAdapter = new CalendarViewAdapter(
                context,
                onSelectDateListener,
                CalendarAttr.CalendarType.MONTH,
                CalendarAttr.WeekArrayType.Monday,
                customDayView);
        calendarAdapter.setOnCalendarTypeChangedListener(new CalendarViewAdapter.OnCalendarTypeChanged() {
            @Override
            public void onCalendarTypeChanged(CalendarAttr.CalendarType type) {
                //rvToDoList.scrollToPosition(0);
            }
        });
        initMarkData();
        initMonthPager();
    }
    /**
     * 初始化标记数据，HashMap的形式，可自定义
     * 如果存在异步的话，在使用setMarkData之后调用 calendarAdapter.notifyDataChanged();
     */
    private void initMarkData() {
        HashMap<String, String> markData = new HashMap<>();
        markData.put("2017-8-9", "1");
        markData.put("2017-7-9", "0");
        markData.put("2017-6-9", "1");
        markData.put("2017-6-10", "0");
        calendarAdapter.setMarkData(markData);
    }
    /**
     * 初始化monthPager，MonthPager继承自ViewPager
     *
     * @return void
     */
    private void initMonthPager() {
        monthPager.setAdapter(calendarAdapter);
        monthPager.setCurrentItem(MonthPager.CURRENT_DAY_INDEX);
        monthPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                position = (float) Math.sqrt(1 - Math.abs(position));
                page.setAlpha(position);
            }
        });
        monthPager.addOnPageChangeListener(new MonthPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPage = position;
                currentCalendars = calendarAdapter.getPagers();
                if (currentCalendars.get(position % currentCalendars.size()) != null) {
                    CalendarDate date = currentCalendars.get(position % currentCalendars.size()).getSeedDate();
                    currentDate = date;
                    tvYear.setText(date.getYear() + "年");
                    tvMonth.setText(date.getMonth() + "");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
    /**
     * 初始化currentDate
     *
     * @return void
     */
    private void initCurrentDate() {
        currentDate = new CalendarDate();
        tvYear.setText(currentDate.getYear() + "年");
        tvMonth.setText(currentDate.getMonth() + "");

        Log.i("当前选择日期", "initCurrentDate: 看谁快 "+currentDate.getYear() + "年"+currentDate.getMonth() + "月"
                +currentDate.getDay() + "日");

        scheduleList.setAdapter(new SettingScheduleAdapter(
                FloatingWindowDisplayService.getDbManage().getScheduleByTime(
                        currentDate.getYear()+"-"+currentDate.getMonth()+"-"+currentDate.getDay())
                ,getContext()));
    }
    private void refreshClickDate(CalendarDate date) {
        currentDate = date;
        tvYear.setText(date.getYear() + "年");
        tvMonth.setText(date.getMonth() + "");
        Log.i("当前选择日期", "refreshClickDate: "+date.getYear() + "年"+date.getMonth() + "月"
                +date.getDay() + "日");

        scheduleList.setAdapter(new SettingScheduleAdapter(
                FloatingWindowDisplayService.getDbManage().getScheduleByTime(
                        date.getYear()+"-"+date.getMonth()+"-"+date.getDay())
                ,getContext()));
//        FloatingWindowDisplayService.getDbManage().getScheduleByTime(
//                date.getYear()+"-"+date.getMonth()+"-"+date.getDay());
    }


    private void initListener() {
        onSelectDateListener = new OnSelectDateListener() {
            @Override
            public void onSelectDate(CalendarDate date) {
                refreshClickDate(date);
            }

            @Override
            public void onSelectOtherMonth(int offset) {
                //偏移量 -1表示刷新成上一个月数据 ， 1表示刷新成下一个月数据
                monthPager.selectOtherMonth(offset);
            }
        };
    }

    public void onClickBackToDayBtn() {
        refreshMonthPager();
    }

    private void refreshMonthPager() {
        CalendarDate today = new CalendarDate();
        calendarAdapter.notifyDataChanged(today);
        tvYear.setText(today.getYear() + "年");
        tvMonth.setText(today.getMonth() + "");
    }

    private void refreshSelectBackground() {
        ThemeDayView themeDayView = new ThemeDayView(context, R.layout.custom_day_focus);
        calendarAdapter.setCustomDayRenderer(themeDayView);
        calendarAdapter.notifyDataSetChanged();
        calendarAdapter.notifyDataChanged(new CalendarDate());
    }


    /**
     * 初始化对应功能的listener
     *
     * @return void
     */
    private void initToolbarClickListener() {
        addImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rootView.findViewById(R.id.id_setting_schedule_add_RelativeLayout).getVisibility() == View.GONE) {
                    rootView.findViewById(R.id.id_setting_schedule_add_RelativeLayout).setVisibility(View.VISIBLE);
                } else {
                    rootView.findViewById(R.id.id_setting_schedule_add_RelativeLayout).setVisibility(View.GONE);
                }
                // InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            }
        });
        takePutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put("time",currentDate.getYear()+"-"+currentDate.getMonth()+"-"+currentDate.getDay());
                values.put("info",((EditText)rootView.findViewById(R.id.id_setting_schedule_add_EditText)).getText().toString());
                FloatingWindowDisplayService.getDbManage().getMyDBHelper().insert("schedule", null, values);
                Toast.makeText(getContext(),"添加成功",Toast.LENGTH_SHORT).show();
                rootView.findViewById(R.id.id_setting_schedule_add_RelativeLayout).setVisibility(View.GONE);
                scheduleList.setAdapter(new SettingScheduleAdapter(
                        FloatingWindowDisplayService.getDbManage().getScheduleByTime(
                                currentDate.getYear()+"-"+currentDate.getMonth()+"-"+currentDate.getDay())
                        ,getContext()));
            }
        });
        backToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickBackToDayBtn();
            }
        });

        rootView.findViewById(R.id.id_setting_schedule_add_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootView.findViewById(R.id.id_setting_schedule_add_RelativeLayout).setVisibility(View.GONE);
            }
        });
//        scrollSwitch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (calendarAdapter.getCalendarType() == CalendarAttr.CalendarType.WEEK) {
//                    Utils.scrollTo(content, rvToDoList, monthPager.getViewHeight(), 200);
//                    calendarAdapter.switchToMonth();
//                } else {
//                    Utils.scrollTo(content, rvToDoList, monthPager.getCellHeight(), 200);
//                    calendarAdapter.switchToWeek(monthPager.getRowIndex());
//                }
//            }
//        });
        themeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshSelectBackground();
            }
        });
        nextMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                monthPager.setCurrentItem(monthPager.getCurrentPosition() + 1);
            }
        });
        lastMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                monthPager.setCurrentItem(monthPager.getCurrentPosition() - 1);
            }
        });
    }



    private static ScheduleFrament fragment = null;
    public static ScheduleFrament newInstance() {
        if (fragment == null){
            fragment = new ScheduleFrament();
        }
        return fragment;
    }
}
