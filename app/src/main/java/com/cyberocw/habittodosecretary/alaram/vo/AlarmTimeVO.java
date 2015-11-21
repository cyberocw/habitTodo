package com.cyberocw.habittodosecretary.alaram.vo;

/**
 * Created by cyberocw on 2015-09-06.
 *
 */
public class AlarmTimeVO {
	private long timeStamp;
	private long id;
	private int callTime;
	private int useYn;
	private long fId;
	private String alarmTitle;

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


	@Override
	public String toString() {
		return "AlarmTimeVO{" +
				"timeStamp=" + timeStamp +
				", id=" + id +
				", callTime=" + callTime +
				", useYn=" + useYn +
				", fId=" + fId +
				", alarmTitle='" + alarmTitle + '\'' +
				'}';
	}
}
