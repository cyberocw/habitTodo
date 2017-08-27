package com.cyberocw.habittodosecretary.memo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.alaram.db.AlarmDbManager;
import com.cyberocw.habittodosecretary.common.vo.FileVO;
import com.cyberocw.habittodosecretary.common.vo.RelationVO;
import com.cyberocw.habittodosecretary.db.CommonRelationDBManager;
import com.cyberocw.habittodosecretary.db.DbHelper;
import com.cyberocw.habittodosecretary.file.FileDbManager;
import com.cyberocw.habittodosecretary.file.StorageHelper;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by cyberocw on 2015-12-06.
 */
public class MemoDbManager extends DbHelper{
	private static Context mCtx;
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
		mCtx = context;

		return sInstance;
	}

	public ArrayList<MemoVO> getListByCate(long cateId, String sortOption, int cnt){
		String selectQuery = " SELECT TM.*, AR." + KEY_F_ALARM_ID + " FROM " + TABLE_MEMO + " TM LEFT JOIN " + TABLE_ALARAM_RELATION + " AR " +
				" ON TM." + KEY_ID + "= AR." + KEY_F_ID + " AND AR." + KEY_TYPE + " = '"+ Const.ETC_TYPE.MEMO + "'";
			selectQuery += " where TM." + KEY_USE_YN + " = 1 ";
			if(cateId > -1)
				selectQuery += " AND " + KEY_CATEGORY_ID + " = " + cateId;

		if(sortOption.equals(Const.MEMO.SORT_REG_DATE_ASC))
			selectQuery += " order by " + KEY_UPDATE_DATE + " asc";
		else if(sortOption.equals(Const.MEMO.SORT_STAR_DESC))
			selectQuery += " order by " + KEY_RANK + " desc";
		else if(sortOption.equals(Const.MEMO.SORT_STAR_ASC))
			selectQuery += " order by " + KEY_RANK + " asc";
		else
			selectQuery += " order by " + KEY_UPDATE_DATE + " desc";

		if(cnt > 0){
			selectQuery += " limit " + cnt;
		}

		return getQuery(selectQuery);
	}
	public ArrayList<MemoVO> getList(){
		return getList(0);
	}
	public ArrayList<MemoVO> getList(int cnt){
		String selectQuery = " SELECT * FROM " + TABLE_MEMO + " where " + KEY_USE_YN + " = 1  order by " + KEY_UPDATE_DATE + " desc " ;
		if(cnt > 0)
			selectQuery += " limit " + cnt ;
		return getQuery(selectQuery);
	}
	public MemoVO getById(long id){
		String selectQuery = " SELECT * FROM " + TABLE_MEMO + " where " + KEY_USE_YN + " = 1  and " + KEY_ID + " = " + id ;
		ArrayList<MemoVO> list = getQuery(selectQuery);
		if(list.size() > 0){
			return list.get(0);
		}
		return null;
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
				vo.setType((c.getString(c.getColumnIndex(KEY_TYPE))));
				vo.setContents(c.getString(c.getColumnIndex(KEY_CONTENTS)));
				vo.setCategoryId(c.getLong(c.getColumnIndex(KEY_CATEGORY_ID)));
				vo.setUrl(c.getString(c.getColumnIndex(KEY_URL)));
				vo.setCreateDt(c.getLong(c.getColumnIndex(KEY_CREATE_DATE)));
				vo.setUpdateDt(c.getLong(c.getColumnIndex(KEY_UPDATE_DATE)));
				vo.setViewCnt(c.getInt(c.getColumnIndex(KEY_VIEW_CNT)));
				vo.setRank(c.getInt(c.getColumnIndex(KEY_RANK)));
				vo.setUseYn(c.getInt(c.getColumnIndex(KEY_USE_YN)));

				if(!c.isNull(c.getColumnIndex(KEY_F_ALARM_ID)))
					vo.setAlarmId(c.getLong(c.getColumnIndex(KEY_F_ALARM_ID)));
				list.add(vo);
			} while (c.moveToNext());
		}
		closeDB();
		return list;
	}

	public int update(MemoVO item) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		int result = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(KEY_TITLE, item.getTitle());
			values.put(KEY_CONTENTS, item.getContents());
			values.put(KEY_TYPE, item.getType());
			values.put(KEY_VIEW_CNT, 0);
			values.put(KEY_USE_YN, item.getUseYn());
			values.put(KEY_URL, item.getUseYn());
			values.put(KEY_RANK, item.getRank());
			values.put(KEY_CATEGORY_ID, item.getCategoryId());

			Calendar c = Calendar.getInstance();
			values.put(KEY_UPDATE_DATE, c.getTimeInMillis());

			result = db.update(TABLE_MEMO, values, KEY_ID + "=?", new String[]{Long.toString(item.getId())});

			ArrayList<FileVO> fileList = item.getFileList();
			if (fileList != null) {
				FileDbManager fdb = FileDbManager.getInstance(mCtx);
				FileVO fvo;
				for (int i = 0; i < fileList.size(); i++) {
					fvo = fileList.get(i);
					fvo.setfId(item.getId());
					fdb.update(fileList.get(i), db);
				}
			}
			fileList = item.getDelFileList();
			if (fileList != null) {
				FileDbManager fdb = FileDbManager.getInstance(mCtx);
				FileVO vo;
				for (int i = 0; i < fileList.size(); i++) {
					vo = fileList.get(i);
					fdb.delete(fileList.get(i).getId(), db);
				}
			}
			db.setTransactionSuccessful();
			//db 삭제 성공 후 실제 파일 지움
			if (fileList != null) {
				FileVO vo;
				for (int i = 0; i < fileList.size(); i++) {
					vo = fileList.get(i);
					StorageHelper.deleteExternalStoragePrivateFile(mCtx, Uri.parse(vo.getUriPath()).getLastPathSegment());
				}
			}
		}
		catch (Exception e){
			Crashlytics.log(Log.ERROR, this.toString(), e.getStackTrace().toString() + " mess" + e.getMessage());
			result = 0;
		}
		finally {
			db.endTransaction();
		}

		closeDB();
		return result;
	}

	public boolean delete(long id) {

		CommonRelationDBManager commonRelationDbManager = CommonRelationDBManager.getInstance(mCtx);
		RelationVO relationVO = commonRelationDbManager.getByTypeId(Const.ETC_TYPE.MEMO, id);

		long alarmId = relationVO.getAlarmId();
		boolean result = true;

		SQLiteDatabase db = this.getWritableDatabase();

		try {
			db.beginTransaction();
			// relation이 맺어져 있다면
			if(alarmId > -1) {
				AlarmDbManager alarmDbManager = AlarmDbManager.getInstance(mCtx);
				alarmDbManager.deleteAlarm(alarmId, db);
				commonRelationDbManager.deleteByTypeAndId(Const.ETC_TYPE.MEMO, id, db);
			}
			db.delete(TABLE_MEMO, KEY_ID + "=?", new String[]{String.valueOf(id)});
			/*ContentValues values = new ContentValues();
			values.put(KEY_USE_YN, 0);
			db.update(TABLE_MEMO, values, KEY_ID + " =?", new String[]{String.valueOf(id)});*/
			db.setTransactionSuccessful();
		}
		catch (Exception e){
			Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "error msg="+e.getMessage());
			result = false;
		}
		finally{
			db.endTransaction();
			closeDB();
		}
		return result;
	}

	public boolean insert(MemoVO item) {
		SQLiteDatabase db = this.getWritableDatabase();

		db.beginTransaction();
		boolean result = true;
		try {
			long now = Calendar.getInstance().getTimeInMillis();

			ContentValues values = new ContentValues();
			values.put(KEY_TITLE, item.getTitle());
			values.put(KEY_CONTENTS, item.getContents());
			values.put(KEY_TYPE, item.getType());
			values.put(KEY_USE_YN, 1);
			values.put(KEY_VIEW_CNT, 0);
			values.put(KEY_URL, item.getUrl());
			values.put(KEY_RANK, item.getRank());
			values.put(KEY_CATEGORY_ID, item.getCategoryId());
			values.put(KEY_CREATE_DATE, now);
			values.put(KEY_UPDATE_DATE, now);

			long _id = db.insert(TABLE_MEMO, null, values);

			if (_id == -1) {
				Log.e(Const.DEBUG_TAG, "DB memo INSERT ERROR");
				throw new Error("DB memo INSERT ERROR");
			} else
				item.setId(_id);

			ArrayList<FileVO> fileList = item.getFileList();
			if (fileList != null) {
				FileDbManager fdb = FileDbManager.getInstance(mCtx);
				FileVO fvo;
				for (int i = 0; i < fileList.size(); i++) {
					fvo = fileList.get(i);
					fvo.setfId(item.getId());
					fdb.update(fileList.get(i), db);
				}
			}
			db.setTransactionSuccessful();
		}
		catch (Exception e){
			Crashlytics.log(Log.ERROR, this.toString(), e.getStackTrace().toString() + " mess" + e.getMessage());
			result = false;
		}
		finally {
			db.endTransaction();
		}
		closeDB();
		return result;
	}
}
