package com.cyberocw.habittodosecretary.memo.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cyberocw.habittodosecretary.db.DbHelper;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;

import java.util.ArrayList;

/**
 * Created by cyberocw on 2015-12-06.
 */
public class MemoDbManager extends DbHelper{
	public static MemoDbManager sInstance = null;
	public MemoDbManager(Context context) {
		super(context);
	}
	public static synchronized MemoDbManager getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx

		if (sInstance == null) {
			sInstance = new MemoDbManager(context);
		}
		return sInstance;
	}
	public ArrayList<MemoVO> getMemoList() {

		String selectQuery = " SELECT * FROM " + TABLE_MEMO;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);

		ArrayList<MemoVO> list = new ArrayList<MemoVO>();
		MemoVO vo;

		if (c.moveToFirst()) {
			do {
				vo = new MemoVO();
				vo.setId(c.getLong((c.getColumnIndex(KEY_ID))));
				vo.setTitle((c.getString(c.getColumnIndex(KEY_TITLE))));
				vo.setContents(c.getString(c.getColumnIndex(KEY_CONTENTS)));
				vo.setCategoryId(c.getLong(c.getColumnIndex(KEY_CATEGORY_ID)));
				vo.setUrl(c.getString(c.getColumnIndex(KEY_URL)));
				vo.setCreateDt(c.getInt(c.getColumnIndex(KEY_CREATE_DATE)));
				vo.setUpdateDt(c.getInt(c.getColumnIndex(KEY_UPDATE_DATE)));
				vo.setViewCnt(c.getInt(c.getColumnIndex(KEY_VIEW_CNT)));
				vo.setRank(c.getInt(c.getColumnIndex(KEY_RANK)));

				list.add(vo);
			} while (c.moveToNext());
		}
		closeDB();

		return list;

	}

	public int updateCategory(MemoVO item) {
		return 0;
	}

	public boolean deleteCategory(long id) {
		return false;
	}

	public void insertCategory(MemoVO item) {

	}
}
