package com.cyberocw.habittodosecretary.calendar;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.alaram.vo.HolidayVO;
import com.cyberocw.habittodosecretary.calendar.vo.DayVO;

import java.util.Calendar;
import java.util.List;

/**
 * Created by cyber on 2017-12-06.
 */


public class DayView extends RelativeLayout {

    private static final String TAG = "CALENDAR DAY";
    private static final String NAME = "DayView";
    private final String CLASS = NAME + "@" + Integer.toHexString(hashCode());
    private int defaultColor = Color.BLACK;

    /** number text field */
    private TextView dayTv, tvAlarm;
    /** message text field*/
    private TextView msgTv;
    private LinearLayout alarmListWrap;
    /** Value object for a day info */
    private DayVO one;

    /**
     * DayView constructor
     * @param context
     */
    public DayView(Context context) {
        super(context);
        init(context);

    }

    /**
     * DayView constructor for xml
     * @param context
     * @param attrs
     */
    public DayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context)
    {

        View v = View.inflate(context, R.layout.calendar_day, this);

        dayTv = (TextView) v.findViewById(R.id.onday_dayTv);
        msgTv = (TextView) v.findViewById(R.id.onday_msgTv);
        //tvAlarm = (TextView) v.findViewById(R.id.tvAlarm);
        alarmListWrap = (LinearLayout) v.findViewById(R.id.alarmListWrap);
        alarmListWrap.removeAllViews();
        one = new DayVO();

    }

    /**
     * Set a day
     * @param year 4 digits of a year
     * @param month Calendar.JANUARY ~ Calendar.DECEMBER
     * @param day day of month
     */
    public void setDay(int year, int month, int day) {
        this.one.getCal().set(year, month, day);
    }

    /**
     * Set a day
     * @param cal Calendar instance
     */
    public void setDay(Calendar cal) {
        this.one.setDay((Calendar) cal.clone());
    }

    /**
     * Set a day
     * @param one DayVO instance
     */
    public void setDay(DayVO one) {
        this.one = one;
    }

    /**
     * Get a day info
     * @return DayVO instance
     */
    public DayVO getDay() {
        return one;
    }

    /**
     * Set the message to display
     * @param msg message
     */
    public void setMsg(String msg){
        one.setMessage(msg);
    }

    /**
     * Get the message is displaying
     * @return message
     */
    public CharSequence getMsg(){
        return  one.getMessage();
    }

    /**
     * Returns the value of the given field after computing the field values by
     * calling {@code complete()} first.
     *
     * @param field Calendar.YEAR or Calendar.MONTH or Calendar.DAY_OF_MONTH
     *
     * @throws IllegalArgumentException
     *                if the fields are not set, the time is not set, and the
     *                time cannot be computed from the current field values.
     * @throws ArrayIndexOutOfBoundsException
     *                if the field is not inside the range of possible fields.
     *                The range is starting at 0 up to {@code FIELD_COUNT}.
     */
    public int get(int field) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        return one.get(field);
    }


    /**
     * Updates UI upon the value object.
     */
    public void refresh() {
        dayTv.setText(String.valueOf(one.get(Calendar.DAY_OF_MONTH)));
        List<HolidayVO> holidayList = one.getHolidayList();
        List<AlarmVO> alarmList = one.getAlarmList();
        String holiday = "";
        alarmListWrap.removeAllViewsInLayout();
        if(holidayList != null && one.getHolidayList().size() > 0) {
            dayTv.setTextColor(Color.RED);
            for (int i = 0; i < holidayList.size(); i++) {
                holiday += holidayList.get(i).getName() + "\n";
            }
            msgTv.setVisibility(VISIBLE);
            msgTv.setText(holiday);
        }else {
            dayTv.setTextColor(defaultColor);
            msgTv.setVisibility(GONE);
        }

        if(one.getIsToday()){
            //오늘 날짜
            //dayTv.setBackgroundResource(R.drawable.dot);
            dayTv.setTextColor(Color.GREEN);
        }
        else
            dayTv.setBackgroundResource(0);

        if(alarmList != null && alarmList.size() > 0) {
            for (int i = 0; i < alarmList.size(); i++) {
                View view =  View.inflate(getContext(), R.layout.calendar_day_alarm, alarmListWrap);
                TextView tv = (TextView) view.findViewById(R.id.tvAlarmTitle);
                tv.setText(alarmList.get(i).getAlarmTitle());
                //alarmListWrap.addView(view);
            }
        }

        //msgTv.setText((one.getMessage()==null)?"":one.getMessage());

    }


    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }
}
