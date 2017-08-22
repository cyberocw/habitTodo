package com.cyberocw.habittodosecretary.memo.vo;

import com.cyberocw.habittodosecretary.common.vo.FileVO;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by cyberocw on 2015-12-06.
 */
public class MemoVO implements Serializable, Cloneable {
	private long id;
	private String type = "";
	private String title;
	private String contents;
	private long categoryId;
	private String url;
	private long createDt;
	private long updateDt;
	private int viewCnt;
	private int rank;
	private int useYn;
	private long alarmId = -1;
	private ArrayList<FileVO> fileList;
	private ArrayList<FileVO> delFileList;

	public long getAlarmId() {
		return alarmId;
	}

	public void setAlarmId(long alarmId) {
		this.alarmId = alarmId;
	}

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

	public long getCreateDt() {
		return createDt;
	}

	public void setCreateDt(long createDt) {
		this.createDt = createDt;
	}

	public long getUpdateDt() {
		return updateDt;
	}

	public void setUpdateDt(long updateDt) {
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

	public int getUseYn() {
		return useYn;
	}

	public void setUseYn(int useYn) {
		this.useYn = useYn;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		MemoVO vo = (MemoVO) super.clone();
		vo.fileList = fileList != null ? (ArrayList<FileVO>) fileList.clone() : null;
		vo.delFileList = delFileList != null ? (ArrayList<FileVO>) delFileList.clone() : null;
		return vo;
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
				", alarmId=" + alarmId +
				'}';
	}

    public void setFileList(ArrayList<FileVO> fileList) {
        this.fileList = fileList;
    }

	public ArrayList<FileVO> getFileList() {
		return fileList;
	}

	public void setDelFileList(ArrayList<FileVO> fileList) {
		this.delFileList = fileList;
	}

	public ArrayList<FileVO> getDelFileList() {
		return delFileList;
	}

}
