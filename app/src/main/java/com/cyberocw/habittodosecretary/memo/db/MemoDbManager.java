package com.cyberocw.habittodosecretary.memo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.db.DbHelper;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;

import java.util.ArrayList;
import java.util.Calendar;

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

	public ArrayList<MemoVO> getListByCate(long cateId){
		String selectQuery = " SELECT * FROM " + TABLE_MEMO + " WHERE " + KEY_CATEGORY_ID + " = " + cateId;
		return getQuery(selectQuery);
	}
	public ArrayList<MemoVO> getList(){
		String selectQuery = " SELECT * FROM " + TABLE_MEMO;
		return getQuery(selectQuery);
	}
	public ArrayList<MemoVO> getQuery(String selectQuery) {

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

	public int update(MemoVO item) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, item.getTitle());
		values.put(KEY_CONTENTS, item.getContents());
		values.put(KEY_TYPE, "MEMO");
		values.put(KEY_VIEW_CNT, 0);
		values.put(KEY_URL, item.getUrl());
		values.put(KEY_RANK, item.getRank());
		values.put(KEY_CATEGORY_ID, item.getCategoryId());

		Calendar c = Calendar.getInstance();
		values.put(KEY_UPDATE_DATE, c.getTimeInMillis());

		int result = db.update(TABLE_MEMO, values, KEY_ID + "=?", new String[]{Long.toString(item.getId())});
		closeDB();
		return result;


	}

	public boolean delete(long id) {
		return false;
	}

	public void insert(MemoVO item) {
		SQLiteDatabase db = this.getWritableDatabase();

		long now = Calendar.getInstance().getTimeInMillis();

		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, item.getTitle());
		values.put(KEY_CONTENTS, item.getContents());
		values.put(KEY_TYPE, "MEMO");
		values.put(KEY_VIEW_CNT, 0);
		values.put(KEY_URL, item.getUrl());
		values.put(KEY_RANK, item.getRank());
		values.put(KEY_CATEGORY_ID, item.getCategoryId());
		values.put(KEY_CREATE_DATE, now);
		values.put(KEY_UPDATE_DATE, now);

		long _id = db.insert(TABLE_MEMO, null, values);

		if(_id == -1){
			Log.e(Const.DEBUG_TAG, "DB memo INSERT ERROR");
			throw new Error("DB memo INSERT ERROR");
		}
		else
			item.setId(_id);

	}
}