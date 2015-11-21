package com.cyberocw.habittodosecretary.alaram.vo;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by cyberocw on 2015-10-18.
 */
public class TimerVO implements Serializable {
	private long id;
	private int hour;
	private int minute;
	private int second;
	private int alarmType;
	private String alarmTitle;
	private Calendar createDt;
	private Calendar updateDt;
	private String alarmContents;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	public int getAlarmType() {
		return alarmType;
	}

	public void setAlarmType(int alarmType) {
		this.alarmType = alarmType;
	}

	public String getAlarmTitle() {
		return alarmTitle;
	}

	public void setAlarmTitle(String alarmTitle) {
		this.alarmTitle = alarmTitle;
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

	public String getAlarmContents() {
		return alarmContents;
	}

	public void setAlarmContents(String alarmContents) {
		this.alarmContents = alarmContents;
	}

	@Override
	public String toString() {
		return "TimerVO{" +
				"id=" + id +
				", hour=" + hour +
				", minute=" + minute +
				", second=" + second +
				", alarmType=" + alarmType +
				", alarmTitle='" + alarmTitle + '\'' +
				", createDt=" + createDt +
				", updateDt=" + updateDt +
				", alarmContents='" + alarmContents + '\'' +
				'}';
	}
}

