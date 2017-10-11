package com.cyberocw.habittodosecretary.alaram.vo;

import java.io.Serializable;

/**
 * Created by cyberocw on 2015-09-06.
 *
 */
public class AlarmTimeVO implements Serializable{
	private long timeStamp;
	private long id;
	private int reqCode;
	private int callTime;
	private int useYn;
	private long fId;
	private String alarmTitle;
	private int alarmOption;
	private int alarmType;
	private int alarmCallType;
	private String etcType;
	private int alarmReminderType = 0;
	private int repeatDayId;

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

	public String getEtcType() {
		return etcType;
	}

	public void setEtcType(String etcType) {
		this.etcType = etcType;
	}

	public int getReqCode() {
		return reqCode;
	}

	public void setReqCode(int reqCode) {
		this.reqCode = reqCode;
	}

	public int getAlarmCallType() {
		return alarmCallType;
	}

	public void setAlarmCallType(int alarmCallType) {
		this.alarmCallType = alarmCallType;
	}

	public int getAlarmReminderType() {
		return alarmReminderType;
	}

	public void setAlarmReminderType(int alarmReminderType) {
		this.alarmReminderType = alarmReminderType;
	}

	public int getRepeatDayId() {
		return repeatDayId;
	}

	public void setRepeatDayId(int repeatDayId) {
		this.repeatDayId = repeatDayId;
	}

	@Override
	public String toString() {
		return "AlarmTimeVO{" +
				"timeStamp=" + timeStamp +
				", id=" + id +
				", reqCode=" + reqCode +
				", callTime=" + callTime +
				", useYn=" + useYn +
				", fId=" + fId +
				", alarmTitle='" + alarmTitle + '\'' +
				", alarmOption=" + alarmOption +
				", alarmType=" + alarmType +
				", etcType='" + etcType + '\'' +
				'}';
	}
}
