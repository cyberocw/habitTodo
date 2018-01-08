package com.cyberocw.habittodosecretary.calendar.vo;

import android.graphics.Color;

import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.alaram.vo.HolidayVO;

import java.util.Calendar;
import java.util.List;

/**
 * Created by cyber on 2017-12-06.
 */

public class DayVO {
    Calendar cal;
    private CharSequence msg = "";
    List<HolidayVO> holidayList = null;
    List<AlarmVO> alarmList = null;
    int year, month, day;
    boolean isToday = false, isSelDay = false;
    /**
     * OneDayData
     */
    public DayVO() {
        this.cal = Calendar.getInstance();
    }

    public Calendar getCal() {
        return cal;
    }

    public void setCal(Calendar cal) {
        this.cal = cal;
    }

    /**
     * Set info by given data
     * @param year 4 digits of a year
     * @param month month Calendar.JANUARY ~ Calendar.DECEMBER
     * @param day day of month (1~#)
     */
    public void setDay(int year, int month, int day) {
        cal = Calendar.getInstance();
        cal.set(year, month, day);
    }

    /**
     * Set info by cloning calendar
     * @param cal calendar to clone
     */
    public void setDay(Calendar cal) {
        this.cal = (Calendar) cal.clone();
    }

    /**
     * Get calendar
     * @return Calendar instance
     */
    public Calendar getDay() {
        return cal;
    }

    /**
     * Returns the value of the given field after computing the field values by
     * calling {@code complete()} first.
     *
     * @throws IllegalArgumentException
     *                if the fields are not set, the time is not set, and the
     *                time cannot be computed from the current field values.
     * @throws ArrayIndexOutOfBoundsException
     *                if the field is not inside the range of possible fields.
     *                The range is starting at 0 up to {@code FIELD_COUNT}.
     */
    public int get(int field) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        return cal.get(field);
    }
    /**
     * Get message
     * @return message
     */
    public CharSequence getMessage() {
        return msg;
    }

    /**
     * Set message
     * @param msg message to display
     */
    public void setMessage(CharSequence msg) {
        this.msg = msg;
    }

    public void setHolidayList(List<HolidayVO> holidayList) {
        this.holidayList = holidayList;
    }

    public List<HolidayVO> getHolidayList() {
        return holidayList;
    }

    public void setAlarmList(List<AlarmVO> alarmList) {
        this.alarmList = alarmList;
    }

    public List<AlarmVO> getAlarmList() {
        return alarmList;
    }

    public void setIsToday(boolean isToday) {
        this.isToday = isToday;
    }

    public boolean getIsToday() {
        return isToday;
    }

    public void setIsSelDay(boolean isSelDay) {
        this.isSelDay = isSelDay;
    }

    public boolean getIsSelDay() {
        return isSelDay;
    }
}
