package com.cyberocw.habittodosecretary.calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.alaram.vo.HolidayVO;
import com.cyberocw.habittodosecretary.calendar.vo.DayVO;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by cyber on 2018-03-20.
 */

public class CalendarWidgetProvider extends AppWidgetProvider{
    private static final String TAG = "WIDGET PROVIDER";
    CalendarManager mCalendarManager;
    private ArrayList<RemoteViews> weeks = null;
    private ArrayList<RemoteViews> dayViews = null;
    private final String WIDGET_PREV = "widgetPrevMonth";
    private final String WIDGET_NEXT = "widgetNextMonth";
    private final String WIDGET_TODAY = "widgetToday";
    private final String YEAR = "widgetYear";
    private final String MONTH = "widgetMonth";

    private Calendar mCalendar = null;
    /**
     * 브로드캐스트를 수신할때, Override된 콜백 메소드가 호출되기 직전에 호출됨
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        Calendar cal;
        cal = Calendar.getInstance();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int year = prefs.getInt(YEAR, cal.get( Calendar.YEAR));
        int month = prefs.getInt(MONTH, cal.get( Calendar.MONTH));

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        /*
        Bundle bundle = intent.getExtras();

        if(bundle != null){
            year = bundle.getInt(YEAR);
            month = bundle.getInt(MONTH);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
        }*/

        if(action.equals(WIDGET_PREV)){
            cal.add(Calendar.MONTH, -1);
        }
        else if(action.equals(WIDGET_NEXT)){
            cal.add(Calendar.MONTH, 1);
        }
        else if(action.equals(WIDGET_TODAY)){
            cal = Calendar.getInstance();
        }
        else{
            return;
        }
        mCalendar = cal;

        if(mCalendar != null) {
            //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putInt(YEAR, mCalendar.get(Calendar.YEAR));
            editor.putInt(MONTH, mCalendar.get(Calendar.MONTH));
            editor.commit();
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
        for (int i = 0; i < appWidgetIds.length; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
        //make(context, null, 0, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
    }

    /**
     * 위젯을 갱신할때 호출됨
     *
     * 주의 : Configure Activity를 정의했을때는 위젯 등록시 처음 한번은 호출이 되지 않습니다
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        if(mCalendar != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putInt(YEAR, mCalendar.get(Calendar.YEAR));
            editor.putInt(MONTH, mCalendar.get(Calendar.MONTH));
            editor.commit();
        }
        appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
        for (int i = 0; i < appWidgetIds.length; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    public void calendarSetting(Context context, RemoteViews wrap, int appWidgetId, Calendar calendar){
        if(calendar == null)
            calendar = Calendar.getInstance();
        mCalendarManager = new CalendarManager(context, null, calendar, null);
        mCalendarManager.init(true);
        //MonthView monthView = new MonthView(context);
        //monthView.setCalendarManager(mCalendarManager);

        bindEvent(context, wrap, appWidgetId);

        init(context, wrap);
        make(context, wrap, appWidgetId, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

    }

    private void bindEvent(Context context, RemoteViews wrap, int appWidgetId){
        Intent in = new Intent("widgetPrevMonth");
        in.putExtra(YEAR, mCalendar.get(Calendar.YEAR));
        in.putExtra(MONTH, mCalendar.get(Calendar.MONTH));
        PendingIntent pi = PendingIntent.getBroadcast(context,0,in,0);
        wrap.setOnClickPendingIntent(R.id.ibLeft, pi);

        in = new Intent("widgetNextMonth");
        in.putExtra(YEAR, mCalendar.get(Calendar.YEAR));
        in.putExtra(MONTH, mCalendar.get(Calendar.MONTH));
        pi = PendingIntent.getBroadcast(context,0,in,0);
        wrap.setOnClickPendingIntent(R.id.ibRight, pi);

        in = new Intent("widgetToday");
        in.putExtra(YEAR, mCalendar.get(Calendar.YEAR));
        in.putExtra(MONTH, mCalendar.get(Calendar.MONTH));
        pi = PendingIntent.getBroadcast(context,0,in,0);
        wrap.setOnClickPendingIntent(R.id.btnToday, pi);
    }

    private void init(Context mContext, RemoteViews wrap){
        //뷰를 미리 넉넉한 만큼 만들어 놓는다.
        if(weeks == null) {

            weeks = new ArrayList<RemoteViews>(6); //한달에 최대 6주
            dayViews = new ArrayList<RemoteViews>(42); // 7일 * 6주 = 42

            RemoteViews ll = null;
            for(int i=0; i<42; i++) {

                if(i % 7 == 0) {
                    //한 주 레이아웃 생성
                    //ll = new LinearLayout(mContext);

                    ll = new RemoteViews(mContext.getPackageName(), R.layout.widget_week);
                    weeks.add(ll);
                }

                LinearLayout.LayoutParams params
                        = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                params.weight = 1;

                //DayView ov = new DayView(mContext);
                RemoteViews ov = new RemoteViews(mContext.getPackageName(), R.layout.calendar_day);



                //ov.setOnClickListener(this);

                /*ov.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Log.d("monthView", "on touch day view");
                        return false;
                    }

                });*/
                if(i % 7 == 0) {
                    ov.setTextColor(R.id.onday_dayTv, Color.RED);
                    ov.setTextColor(R.id.onday_msgTv, Color.RED);
                    //ov.setDefaultColor(Color.RED);
                }
                else if(i % 7 == 6){
                    ov.setTextColor(R.id.onday_dayTv, Color.BLUE);
                    ov.setTextColor(R.id.onday_msgTv, Color.RED);
                }

                ll.addView(R.id.row_container, ov);
                dayViews.add(ov);
            }
        }
    }

    public void make(Context context, RemoteViews wrap, int appWidgetId, int year, int month)
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews rv;

        if(wrap == null)
            rv = new RemoteViews(context.getPackageName(),R.layout.widget_calendar);
         else
            rv = wrap;

        Log.d("monthView", "make start year=" + year + " month="+ month);
        long makeTime = System.currentTimeMillis();
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
            one.setRepeatHolidayCount(mCalendarManager.getRepeatHolidayCnt(cal.get(Calendar.DAY_OF_WEEK), true));
            one.setRepeatCount(mCalendarManager.getRepeatHolidayCnt(cal.get(Calendar.DAY_OF_WEEK), false));

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
            one.setRepeatHolidayCount(mCalendarManager.getRepeatHolidayCnt(cal.get(Calendar.DAY_OF_WEEK), true));
            one.setRepeatCount(mCalendarManager.getRepeatHolidayCnt(cal.get(Calendar.DAY_OF_WEEK), false));

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
                one.setRepeatHolidayCount(mCalendarManager.getRepeatHolidayCnt(cal.get(Calendar.DAY_OF_WEEK), true));
                one.setRepeatCount(mCalendarManager.getRepeatHolidayCnt(cal.get(Calendar.DAY_OF_WEEK), false));

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
        rv.removeAllViews(R.id.calendar);
        Log.d(TAG, "<<<<< take 5555 2222 timeMillis : " + (System.currentTimeMillis() - makeTime));
        int count = 0;
        for(DayVO oneday : DayVOs) {

            if(count % 7 == 0) {
                rv.addView(R.id.calendar, weeks.get(count / 7));
            }
            RemoteViews ov = dayViews.get(count);
            dayUpdate(context, ov, oneday);
            /*ov.setDay(oneday);
            ov.setMsg("");
            ov.refresh();*/
            count++;
        }
        Log.d(TAG, "<<<<< take 666666 timeMillis : " + (System.currentTimeMillis() - makeTime));
        // 주의 개수만큼 무게 지정
        //rv.setWeightSum(getChildCount());
        Log.d(TAG, "<<<<< take timeMillis : " + (System.currentTimeMillis() - makeTime));

    }
    private int defaultColor = Color.BLACK;
    private void dayUpdate(Context context, RemoteViews view, DayVO one){

        view.setTextViewText(R.id.onday_dayTv, String.valueOf(one.get(Calendar.DAY_OF_MONTH)));
        List<HolidayVO> holidayList = one.getHolidayList();
        List<AlarmVO> alarmList = one.getAlarmList();
        String holiday = "";
        //alarmListWrap.removeAllViewsInLayout();
        if(holidayList != null && one.getHolidayList().size() > 0) {
            view.setTextColor(R.id.onday_dayTv, Color.RED);
            for (int i = 0; i < holidayList.size(); i++) {
                holiday += holidayList.get(i).getName() + "\n";
            }

            if(one.get(Calendar.DAY_OF_WEEK) == 1 || one.get(Calendar.DAY_OF_WEEK) == 7)
                view.setTextViewText(R.id.tvRepeatCnt, String.valueOf(one.getRepeatCount()));
            else
                view.setTextViewText(R.id.tvRepeatCnt, String.valueOf(one.getRepeatHolidayCount()));

            view.setTextViewText(R.id.onday_msgTv, holiday);
        }else {
            //view.setTextColor(R.id.onday_dayTv, defaultColor);
            //msgTv.setVisibility(GONE);
            view.setTextViewText(R.id.onday_msgTv, "");
            view.setTextViewText(R.id.tvRepeatCnt, String.valueOf(one.getRepeatCount()));
            //tvRepeatCnt.setText(String.valueOf(one.getRepeatCount()));
        }

        if(one.getIsSelDay()){
            //dayTv.setTextColor(Color.MAGENTA);
            //dayTv.setBackgroundResource(R.drawable.day_of_week_ring);
        }
        else{
            //dayTv.setBackgroundResource(0);
        }

        if(one.getIsToday()){
            //오늘 날짜
            view.setTextColor(R.id.onday_dayTv, Color.GREEN);
        }
        view.removeAllViews(R.id.alarmListWrap);
        if(alarmList != null && alarmList.size() > 0) {
            for (int i = 0; i < alarmList.size(); i++) {
                //View view =  View.inflate(getContext(), R.layout.calendar_day_alarm, null);
                RemoteViews alarmView = new RemoteViews(context.getPackageName(), R.layout.calendar_day_alarm);
                //TextView tv = (TextView) view.findViewById(R.id.tvAlarmTitle);
                alarmView.setTextViewText(R.id.tvAlarmTitle, alarmList.get(i).getAlarmTitle());

                //수동으로 추가해줘야 함
                //RemoteViews ov = new RemoteViews(context.getPackageName(), R.layout.calendar_day_alarm);

                view.addView(R.id.alarmListWrap, alarmView);
                /*alarmListWrap = (LinearLayout) v.findViewById(R.id.alarmListWrap);
                alarmListWrap.addView(view);*/
            }
        }
    }

    public void updateAppWidget(Context context,
                                AppWidgetManager appWidgetManager, int appWidgetId) {
        /**
         * 현재 시간 정보를 가져오기 위한 Calendar
         */
        if(mCalendar == null)
            mCalendar = Calendar.getInstance();
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd",
                Locale.KOREA);

        /**
         * RemoteViews를 이용해 Text설정
         */
        RemoteViews updateViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_calendar);

        updateViews.setTextViewText(R.id.dateView,
                mFormat.format(mCalendar.getTime()));

        //View view = calendarSetting(context, null);

        //updateViews.addView(updateViews.getLayoutId(), (RemoteViews) view);
        //RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);



        /**
         * 레이아웃을 클릭하면 홈페이지 이동
         */

        Intent intent1 = new Intent(context, MainActivity.class);

        /*Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://naver.com/"));*/

        PendingIntent pendingIntent = PendingIntent.getActivity(context, -1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        updateViews.setOnClickPendingIntent(R.id.calendar, pendingIntent);
        calendarSetting(context, updateViews, appWidgetId, mCalendar);
        //init(context, updateViews);
        //make(context,updateViews, appWidgetId, );
        /**
         * 위젯 업데이트
         */
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
    }

    /**
     * 위젯이 처음 생성될때 호출됨
     *
     * 동일한 위젯이 생성되도 최초 생성때만 호출됨
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    /**
     * 위젯의 마지막 인스턴스가 제거될때 호출됨
     *
     * onEnabled()에서 정의한 리소스 정리할때
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    /**
     * 위젯이 사용자에 의해 제거될때 호출됨
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }
}
