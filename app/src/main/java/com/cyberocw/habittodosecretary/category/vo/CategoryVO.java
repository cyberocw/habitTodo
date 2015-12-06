package com.cyberocw.habittodosecretary.category.vo;

import java.util.Calendar;

/**
 * Created by cyberocw on 2015-12-06.
 */
public class CategoryVO {
	private long id = -1;
	private String title;
	private String type;
	private int sortOrder;
	private int useYn;
	private int cnt;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public int getUseYn() {
		return useYn;
	}

	public void setUseYn(int useYn) {
		this.useYn = useYn;
	}

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	@Override
	public String toString() {
		return "CategoryVO{" +
				"id=" + id +
				", title='" + title + '\'' +
				", type='" + type + '\'' +
				", sortOrder=" + sortOrder +
				'}';
	}
}

