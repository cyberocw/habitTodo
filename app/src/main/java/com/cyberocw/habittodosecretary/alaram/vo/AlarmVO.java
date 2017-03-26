package com.cyberocw.habittodosecretary.alaram.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by cyberocw on 2015-08-16.
 */
public class AlarmVO implements Serializable {

	private long id = -1;
	private long rfid = -1;

	private ArrayList<Integer> repeatDay = null;
	private String alarmTitle;
	private int alarmDateType;

	private ArrayList<Calendar> alarmDateList = null;

	private int alarmOption;
	private int hour;
	private int minute;
	private int alarmType;
	private int useYn;
	private String etcType;
	private ArrayList<Integer> alarmCallList = null;

	private int isHolidayALL = 0;
	private int isHolidayNone = 0;

	private Calendar createDt = null;
	private Calendar updateDt = null;


	@Override
	public String toString() {
		return "AlarmVO{" +
				"id=" + id +
				", rfid=" + rfid +
				", repeatDay=" + repeatDay +
				", alarmTitle='" + alarmTitle + '\'' +
				", alarmDateType=" + alarmDateType +
				", alarmDateList=" + alarmDateList +
				", alarmOption=" + alarmOption +
				", hour=" + hour +
				", minute=" + minute +
				", alarmType=" + alarmType +
				", useYn=" + useYn +
				", etcType='" + etcType + '\'' +
				", alarmCallList=" + alarmCallList +
				", isHolidayALL=" + isHolidayALL +
				", isHolidayNone=" + isHolidayNone +
				", createDt=" + createDt +
				", updateDt=" + updateDt +
				'}';
	}

	public AlarmVO(){
	}

	public AlarmVO(Calendar date, String alarmTitle, ArrayList<Integer> repeatDay){
		this.alarmTitle = alarmTitle;
		this.repeatDay = repeatDay;
	}


	public String getTimeText(){
		return String.format("%02d", getHour()) + ":" + String.format("%02d", getMinute());
		//return dateForm.format(this.date.getTime());
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ArrayList<Integer> getRepeatDay() {
		return repeatDay;
	}

	public void setRepeatDay(ArrayList<Integer> repeatDay) {
		this.repeatDay = repeatDay;
	}

	public String getAlarmTitle() {
		return alarmTitle;
	}

	public void setAlarmTitle(String alarmTitle) {
		this.alarmTitle = alarmTitle;
	}

	public int getAlarmDateType() {
		return alarmDateType;
	}

	public void setAlarmDateType(int alarmDateType) {
		this.alarmDateType = alarmDateType;
	}

	public ArrayList<Calendar> getAlarmDateList() {
		return alarmDateList;
	}

	public void setAlarmDateList(ArrayList<Calendar> alarmDateList) {
		this.alarmDateList = alarmDateList;
	}

	public int getAlarmOption() {
		return alarmOption;
	}

	public void setAlarmOption(int alarmOption) {
		this.alarmOption = alarmOption;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public int getAlarmType() {
		return alarmType;
	}

	public void setAlarmType(int alarmType) {
		this.alarmType = alarmType;
	}

	public ArrayList<Integer> getAlarmCallList() {
		return alarmCallList;
	}

	public void setAlarmCallList(ArrayList<Integer> alarmCallList) {
		this.alarmCallList = alarmCallList;
	}

	public String getEtcType() {
		return etcType;
	}

	public void setEtcType(String etcType) {
		this.etcType = etcType;
	}
	public int getUseYn() {
		return useYn;
	}

	public void setUseYn(int useYn) {
		this.useYn = useYn;
	}

	public Calendar getCreateDt() {
		return createDt;
	}

	public void setCreateDt(Calendar createDt) {
		this.createDt = createDt;
	}

	public Calendar getUpdateDt() {
		return updateDt;
	}

	public void setUpdateDt(Calendar updateDt) {
		this.updateDt = updateDt;
	}

	public int getIsHolidayALL() {
		return isHolidayALL;
	}

	public void setIsHolidayALL(int isHolidayALL) {
		this.isHolidayALL = isHolidayALL;
	}

	public int getIsHolidayNone() {
		return isHolidayNone;
	}

	public void setIsHolidayNone(int isHolidayNone) {
		this.isHolidayNone = isHolidayNone;
	}

	public long getRfid() {
		return rfid;
	}

	public void setRfid(long rfid) {
		this.rfid = rfid;
	}
}
