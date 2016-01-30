package com.cyberocw.habittodosecretary.common.vo;

/**
 * Created by cyberocw on 2015-12-26.
 */
public class RelationVO {
	private long fId;
	private long alarmId = -1;
	private String type;

	public long getfId() {
		return fId;
	}

	public void setfId(long fId) {
		this.fId = fId;
	}

	public long getAlarmId() {
		return alarmId;
	}

	public void setAlarmId(long alarmId) {
		this.alarmId = alarmId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "RelationVO{" +
				"fId=" + fId +
				", alarmId=" + alarmId +
				", type='" + type + '\'' +
				'}';
	}
}
