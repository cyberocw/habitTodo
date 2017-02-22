package com.cyberocw.habittodosecretary.alaram.vo;

import java.io.Serializable;

/**
 * Created by cyberocw on 2015-09-06.
 *
 */
public class AlarmTimeVO implements Serializable{
	private long timeStamp;
	private long id;
	private int callTime;
	private int useYn;
	private long fId;
	private String alarmTitle;
	private int alarmOption;
	private int alarmType;
	private static final long serialVersionUID = 1L;

	public String getAlarmTitle() {
		return alarmTitle;
	}

	public void setAlarmTitle(String alarmTitle) {
		this.alarmTitle = alarmTitle;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getCallTime() {
		return callTime;
	}

	public void setCallTime(int callTime) {
		this.callTime = callTime;
	}

	public int getUseYn() {
		return useYn;
	}

	public void setUseYn(int useYn) {
		this.useYn = useYn;
	}

	public long getfId() {
		return fId;
	}

	public void setfId(long fId) {
		this.fId = fId;
	}

	public int getAlarmOption() {
		return alarmOption;
	}

	public void setAlarmOption(int alarmOption) {
		this.alarmOption = alarmOption;
	}

	public int getAlarmType() {
		return alarmType;
	}

	public void setAlarmType(int alarmType) {
		this.alarmType = alarmType;
	}

	@Override
	public String toString() {
		return "AlarmTimeVO{" +
				"timeStamp=" + timeStamp +
				", id=" + id +
				", callTime=" + callTime +
				", useYn=" + useYn +
				", fId=" + fId +
				", alarmTitle='" + alarmTitle + '\'' +
				", alarmOption=" + alarmOption +
				", alarmType=" + alarmType +
				'}';
	}
}
