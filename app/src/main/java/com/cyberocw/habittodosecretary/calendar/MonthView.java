package com.cyberocw.habittodosecretary.calendar;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.alaram.AlarmFragment;
import com.cyberocw.habittodosecretary.alaram.ui.CustomViewFlipper;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.alaram.vo.HolidayVO;
import com.cyberocw.habittodosecretary.calendar.vo.DayVO;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by cyber on 2017-12-06.
 */

public class MonthView extends LinearLayout implements View.OnClickListener{

    private static final String TAG = "MONTH VIEW";
    private static final String NAME = "MonthView";
    private final String CLASS = NAME + "@" + Integer.toHexString(hashCode());
    private AlarmFragment mAlarmFragment = null;
    private Context mContext;
    private CustomViewFlipper mFlipper = null;
    CalendarManager mCalendarManager;
    private int mYear;
    private int mMonth;
    private OnClickDayListener onClickDayListener;
    private final OnClickDayListener dummyClickDayListener = new OnClickDayListener() {
        @Override
        public void onClick(Calendar calendar) {
            Log.d("monthView", "dummy onclick");
        }
    };

    public void setCalendarManager(CalendarManager calendarManager) {
        this.mCalendarManager = calendarManager;
    }

    public interface OnClickDayListener {
        void onClick(Calendar calendar);
    }
    public void setOnClickDayListener(OnClickDayListener onClickDayListener) {
        if (onClickDayListener != null) {
            this.onClickDayListener = onClickDayListener;
        }
        else {
            this.onClickDayListener = dummyClickDayListener;
        }
    }
    public MonthView(Context context) {
        this(context, null);
    }

    public MonthView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MonthView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;

        setOrientation(LinearLayout.VERTICAL);
        onClickDayListener = dummyClickDayListener;

        //뷰를 미리 넉넉한 만큼 만들어 놓는다.
        if(weeks == null) {

            weeks = new ArrayList<LinearLayout>(6); //한달에 최대 6주
            dayViews = new ArrayList<DayView>(42); // 7일 * 6주 = 42

            LinearLayout ll = null;
            for(int i=0; i<42; i++) {

                if(i % 7 == 0) {
                    //한 주 레이아웃 생성
                    ll = new LinearLayout(mContext);
                    LinearLayout.LayoutParams params
                            = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                    params.weight = 1;
                    ll.setOrientation(LinearLayout.HORIZONTAL);
                    ll.setLayoutParams(params);
                    ll.setWeightSum(7);

                    weeks.add(ll);
                }

                LinearLayout.LayoutParams params
                        = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                params.weight = 1;

                DayView ov = new DayView(mContext);
                ov.setLayoutParams(params);
                ov.setOnClickListener(this);
                /*ov.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Log.d("monthView", "on touch day view");
                        return false;
                    }

                });*/
                if(i % 7 == 0) {
                    ov.setDefaultColor(Color.RED);
                }
                else if(i % 7 == 6){
                    ov.setDefaultColor(Color.BLUE);
                }

                ll.addView(ov);
                dayViews.add(ov);
            }
        }

/*        //미리보기
        if(1==1 || isInEditMode()) {
            Calendar cal = Calendar.getInstance();
            make(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
        }*/

    }

    /**
     * 년
     * @return 4자리 년도
     */
    public int getYear() {
        return mYear;
    }

    /**
     * 달
     * @return 0~11 (Calendar.JANUARY ~ Calendar.DECEMBER)
     */
    public int getMonth() {
        return mMonth;
    }


    /**
     * Any layout manager that doesn't scroll will want this.
     */
    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }


    private ArrayList<LinearLayout> weeks = null;
    private ArrayList<DayView> dayViews = null;

    public void make(int year, int month)
    {
        Log.d("monthView", "make start year=" + year + " month="+ month);
//        if(mYear == year && mMonth == month) {
//            return;
//        }


        long makeTime = System.currentTimeMillis();
        this.mYear = year;
        this.mMonth = month;


        //if(viewRect.width() == 0 || viewRect.height() == 0) return;
        Calendar selectedDateCal = mCalendarManager.getCalendar();

        Calendar todayCal = Calendar.getInstance();
        int todayNum = todayCal.get(Calendar.DAY_OF_MONTH);
        int selectedDayNum = selectedDateCal.get(Calendar.DAY_OF_MONTH);

        String strToday = CommonUtils.convertDateType(todayCal);
        String strSelDay = CommonUtils.convertDateType(selectedDateCal);

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        cal.setFirstDayOfWeek(Calendar.SUNDAY);//일요일을 주의 시작일로 지정

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);//1일의 요일
        int maxOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);//마지막 일수
        ArrayList<DayVO> DayVOs = new ArrayList<DayVO>();

        cal.add(Calendar.DAY_OF_MONTH, Calendar.SUNDAY - dayOfWeek);//주의 첫 일로 이동
        //HLog.d(TAG, CLASS, "first day : " + cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.KOREA) + " / " + cal.get(Calendar.DAY_OF_MONTH));
        Calendar lastDayCal = (Calendar) cal.clone();
        lastDayCal.add(Calendar.DAY_OF_MONTH, 42);
        HashMap<String, List<HolidayVO>> holidayMap = new HashMap<>();
        HashMap<String, List<AlarmVO>> alarmMap = new HashMap<>();

        if(mCalendarManager != null) {
            Log.d(TAG, "<<<<< take 111 timeMillis : " + (System.currentTimeMillis() - makeTime));
            ArrayList<HolidayVO> holidayList = mCalendarManager.getHolidayList(cal, lastDayCal);
            int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            Calendar prevLastCal = (Calendar) cal.clone();
            prevLastCal.set(Calendar.DAY_OF_MONTH, lastDay);
            //이전 달
            ArrayList<AlarmVO> alarmList = mCalendarManager.getAlarmList(cal, prevLastCal);
            prevLastCal.add(Calendar.DAY_OF_MONTH, 1);
            Calendar secondCal = (Calendar) prevLastCal.clone();
            secondCal.set(Calendar.DAY_OF_MONTH, secondCal.getActualMaximum(Calendar.DAY_OF_MONTH));
            alarmList.addAll(mCalendarManager.getAlarmList(prevLastCal, secondCal));
            secondCal.add(Calendar.DAY_OF_MONTH, 1);
            alarmList.addAll(mCalendarManager.getAlarmList(secondCal, lastDayCal));
            Log.d(TAG, "<<<<< take 2222 timeMillis : " + (System.currentTimeMillis() - makeTime));
            if (alarmList != null) {
                AlarmVO alarmVO;
                for (int i = 0; i < alarmList.size(); i++) {
                    alarmVO = alarmList.get(i);
                    String day = CommonUtils.convertDateType(alarmVO.getAlarmDateList().get(0));
                    if(alarmVO.getAlarmDateType() == Const.ALARM_DATE_TYPE.POSTPONE_DATE)
                        continue;
                    if (!alarmMap.containsKey(day)) {
                        alarmMap.put(day, new ArrayList<AlarmVO>());
                    }
                    alarmMap.get(day).add(alarmVO);
                }
            }
            Log.d(TAG, "<<<<< take 33333 timeMillis : " + (System.currentTimeMillis() - makeTime));
            if (holidayList != null) {
                HolidayVO hVO;
                for (int i = 0; i < holidayList.size(); i++) {
                    hVO = holidayList.get(i);
                    if (hVO.getType().equals("h") || hVO.getType().equals("i")) {
                        if (!holidayMap.containsKey(hVO.getFullDate())) {
                            holidayMap.put(hVO.getFullDate(), new ArrayList<HolidayVO>());
                        }
                        if(hVO.getType().equals("i"))
                            hVO.setName("대체공휴일");
                        holidayMap.get(hVO.getFullDate()).add(hVO);
                    }
                }
            }

            Log.d(TAG, "<<<<< take 444 timeMillis : " + (System.currentTimeMillis() - makeTime));
        }

        // add previous month
        int seekDay;
        String dayString;
        for(;;) {
            seekDay = cal.get(Calendar.DAY_OF_WEEK);
            if(dayOfWeek == seekDay) break;

            dayString = CommonUtils.convertDateType(cal);
            DayVO one = new DayVO();
            one.setDay(cal);
            one.setHolidayList(holidayMap.get(dayString));
            one.setAlarmList(alarmMap.get(dayString));
            /*one.setRepeatHolidayCount(mCalendarManager.getRepeatHolidayCnt(cal.get(Calendar.DAY_OF_WEEK), true));
            one.setRepeatCount(mCalendarManager.getRepeatHolidayCnt(cal.get(Calendar.DAY_OF_WEEK), false));*/

            if(selectedDayNum == cal.get(Calendar.DAY_OF_MONTH) && strSelDay.equals(dayString)){
                one.setIsSelDay(true);
            }
            else
                one.setIsSelDay(false);
            if(todayNum == cal.get(Calendar.DAY_OF_MONTH) && strToday.equals(dayString)){
                one.setIsToday(true);
            }
            else
                one.setIsToday(false);

            DayVOs.add(one);
            //하루 증가
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        Log.d(TAG, "this month : " + cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.KOREA) + " / " + cal.get(Calendar.DAY_OF_MONTH));
        // add this month
        for(int i=0; i < maxOfMonth; i++) {
            dayString = CommonUtils.convertDateType(cal);
            DayVO one = new DayVO();
            one.setDay(cal);
            one.setHolidayList(holidayMap.get(dayString));
            one.setAlarmList(alarmMap.get(dayString));
            /*one.setRepeatHolidayCount(mCalendarManager.getRepeatHolidayCnt(cal.get(Calendar.DAY_OF_WEEK), true));
            one.setRepeatCount(mCalendarManager.getRepeatHolidayCnt(cal.get(Calendar.DAY_OF_WEEK), false));*/

            if(selectedDayNum == cal.get(Calendar.DAY_OF_MONTH) && strSelDay.equals(dayString)){
                one.setIsSelDay(true);
            }
            else
                one.setIsSelDay(false);
            if(todayNum == cal.get(Calendar.DAY_OF_MONTH) && strToday.equals(dayString)){
                one.setIsToday(true);
            }
            else
                one.setIsToday(false);
            DayVOs.add(one);
            //하루 증가
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // add next month
        for(;;) {
            if(cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                dayString = CommonUtils.convertDateType(cal);
                DayVO one = new DayVO();
                one.setDay(cal);
                one.setHolidayList(holidayMap.get(dayString));
                one.setAlarmList(alarmMap.get(dayString));
                /*one.setRepeatHolidayCount(mCalendarManager.getRepeatHolidayCnt(cal.get(Calendar.DAY_OF_WEEK), true));
                one.setRepeatCount(mCalendarManager.getRepeatHolidayCnt(cal.get(Calendar.DAY_OF_WEEK), false));*/

                if(selectedDayNum == cal.get(Calendar.DAY_OF_MONTH) && strSelDay.equals(dayString)){
                    one.setIsSelDay(true);
                }
                else
                    one.setIsSelDay(false);
                if(todayNum == cal.get(Calendar.DAY_OF_MONTH) && strToday.equals(dayString)){
                    one.setIsToday(true);
                }
                else
                    one.setIsToday(false);
                DayVOs.add(one);
            }
            else {
                break;
            }
            //하루 증가
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        Log.d(TAG, "<<<<< take 5555 timeMillis : " + (System.currentTimeMillis() - makeTime));
        if(DayVOs.size() == 0) return;

        //모든 주를 지우기
        this.removeAllViews();
        Log.d(TAG, "<<<<< take 5555 2222 timeMillis : " + (System.currentTimeMillis() - makeTime));
        int count = 0;
        for(DayVO oneday : DayVOs) {
            if(count % 7 == 0) {
                addView(weeks.get(count / 7));
            }
            DayView ov = dayViews.get(count);
            ov.setDay(oneday);
            ov.refresh();
            count++;
        }
        Log.d(TAG, "<<<<< take 666666 timeMillis : " + (System.currentTimeMillis() - makeTime));
        // 주의 개수만큼 무게 지정
        this.setWeightSum(getChildCount());


        Log.d(TAG, "<<<<< take timeMillis : " + (System.currentTimeMillis() - makeTime));

    }

    protected String doubleString(int value) {

        String temp;

        if(value < 10){
            temp = "0"+ String.valueOf(value);

        }else {
            temp = String.valueOf(value);
        }
        return temp;
    }

    @Override
    public void onClick(View v) {

        DayView ov = (DayView) v;

        Calendar selectedDate = (Calendar) ov.getDay().getCal().clone();
        onClickDayListener.onClick(selectedDate);
        //onDayChange(selectedDate);
        if(mFlipper != null){
            mFlipper.showNext();
        }

        //Toast.makeText(mContext, "click", Toast.LENGTH_LONG).show();
        //HLog.d(TAG, CLASS, "click " + ov.get(Calendar.MONTH) + "/" + ov.get(Calendar.DAY_OF_MONTH));
    }


}

