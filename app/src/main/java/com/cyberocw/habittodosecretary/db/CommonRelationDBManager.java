package com.cyberocw.habittodosecretary.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.common.vo.RelationVO;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;

import java.util.Calendar;

/**
 * Created by cyberocw on 2015-12-26.
 * TABLE_ALARAM_RELATION
 * KEY_TYPE + " text , " +
 * KEY_F_ALARM_ID + " integer , " +
 * KEY_F_ID + " integer, " +
 */
public class CommonRelationDBManager extends DbHelper {
	public static CommonRelationDBManager sInstance = null;
	public CommonRelationDBManager(Context context) {
		super(context);
	}
	public static synchronized CommonRelationDBManager getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx

		if (sInstance == null) {
			sInstance = new CommonRelationDBManager(context);
		}
		return sInstance;
	}

	public RelationVO getByAlarmId(long alarmId){
		String selectQuery = " SELECT * FROM " + TABLE_ALARAM_RELATION + " WHERE " + KEY_F_ALARM_ID + " = " + alarmId;
		return getQuery(selectQuery);
	}

	public RelationVO getByTypeId(String type, long id){
		String selectQuery = " SELECT * FROM " + TABLE_ALARAM_RELATION + " WHERE " + KEY_F_ID + " = " + id + " AND " + KEY_TYPE + " = '" + type + "'";
		return getQuery(selectQuery);
	}

	public long insert(String type, long alarmId, long fId){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TYPE, type);
		values.put(KEY_F_ALARM_ID, alarmId);
		values.put(KEY_F_ID, fId);

		long _id = db.insert(TABLE_ALARAM_RELATION, null, values);
		return _id;
	}

	public RelationVO getQuery(String selectQuery) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);

		RelationVO vo = new RelationVO();

		if (c.moveToFirst()) {
			vo.setAlarmId(c.getLong((c.getColumnIndex(KEY_F_ALARM_ID))));
			vo.setfId((c.getLong(c.getColumnIndex(KEY_F_ID))));
			vo.setType(c.getString(c.getColumnIndex(KEY_TYPE)));
		}
		closeDB();
		return vo;
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

	public boolean deleteByAlarmId(long id) {
		SQLiteDatabase db = this.getWritableDatabase();

		int affectedRow = db.delete(TABLE_ALARAM_RELATION, KEY_F_ALARM_ID + "=?", new String[]{String.valueOf(id)});

		closeDB();

		return affectedRow == 1;
	}

	public boolean insert(RelationVO vo) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TYPE, vo.getType());
		values.put(KEY_F_ALARM_ID, vo.getAlarmId());
		values.put(KEY_F_ID, vo.getfId());

		long _id = db.insert(TABLE_ALARAM_RELATION, null, values);

		if(_id == -1){
			Log.e(Const.DEBUG_TAG, "DB Relation INSERT ERROR");

			throw new Error("DB Relation INSERT ERROR");
			//return false;
		}
		else{
			return true;
		}

	}
}
