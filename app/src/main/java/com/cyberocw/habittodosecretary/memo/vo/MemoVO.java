package com.cyberocw.habittodosecretary.memo.vo;

import java.io.Serializable;

/**
 * Created by cyberocw on 2015-12-06.
 */
public class MemoVO implements Serializable {
	private long id;
	private String title;
	private String contents;
	private long categoryId;
	private String url;
	private int createDt;
	private int updateDt;
	private int viewCnt;
	private int rank;

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

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	public long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(long categoryId) {
		this.categoryId = categoryId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getCreateDt() {
		return createDt;
	}

	public void setCreateDt(int createDt) {
		this.createDt = createDt;
	}

	public int getUpdateDt() {
		return updateDt;
	}

	public void setUpdateDt(int updateDt) {
		this.updateDt = updateDt;
	}

	public int getViewCnt() {
		return viewCnt;
	}

	public void setViewCnt(int viewCnt) {
		this.viewCnt = viewCnt;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	@Override
	public String toString() {
		return "MemoVO{" +
				"id=" + id +
				", title='" + title + '\'' +
				", contents='" + contents + '\'' +
				", categoryId=" + categoryId +
				", url='" + url + '\'' +
				", createDt=" + createDt +
				", updateDt=" + updateDt +
				", viewCnt=" + viewCnt +
				", rank=" + rank +
				'}';
	}
}
