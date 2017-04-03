package com.cyberocw.habittodosecretary.category.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.db.DbHelper;
import com.cyberocw.habittodosecretary.category.vo.CategoryVO;

import java.util.ArrayList;

/**
 * Created by cyberocw on 2015-12-06.
 */
public class CategoryDbManager extends DbHelper{
	public static CategoryDbManager sInstance = null;
	public CategoryDbManager(Context context) {
		super(context);
	}
	public static synchronized CategoryDbManager getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx

		if (sInstance == null) {
			sInstance = new CategoryDbManager(context);
		}
		return sInstance;
	}
	public ArrayList<CategoryVO> getCategoryList() {

		String selectQuery = " SELECT *, (SELECT COUNT(*) FROM " + TABLE_MEMO +
				" WHERE " + KEY_CATEGORY_ID + "= CATE." + KEY_ID + " AND " + KEY_USE_YN + " = 1) AS cnt " +
				"FROM " + TABLE_CATEGORY + " CATE WHERE " + KEY_TYPE + "=\"" + Const.CATEGORY.TYPE + "\" AND " + KEY_USE_YN + " = 1";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);

		ArrayList<CategoryVO> list = new ArrayList<CategoryVO>();
		CategoryVO vo;

		if (c.moveToFirst()) {
			do {
				vo = new CategoryVO();
				vo.setId(c.getLong((c.getColumnIndex(KEY_ID))));
				vo.setTitle((c.getString(c.getColumnIndex(KEY_TITLE))));
				vo.setType((c.getString(c.getColumnIndex(KEY_TYPE))));
				vo.setSortOrder((c.getInt(c.getColumnIndex(KEY_SORT))));
				vo.setUseYn((c.getInt(c.getColumnIndex(KEY_USE_YN))));
				vo.setCnt((c.getInt(c.getColumnIndex("cnt"))));

				list.add(vo);
			} while (c.moveToNext());
		}
		closeDB();

		return list;
	}

	public int updateCategory(CategoryVO item) {
		//return 0;
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, item.getTitle());

		int result = db.update(TABLE_CATEGORY, values, KEY_ID + "=?", new String[]{Long.toString(item.getId())});
		closeDB();
		return result;
	}

	public boolean deleteCategory(long id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		boolean result = true;

		String sqlAlarm = "delete from " + TABLE_ALARM + " where " + KEY_ID + " in " +
				"( select " + KEY_F_ALARM_ID + " from " + TABLE_ALARAM_RELATION + " where " +
				KEY_TYPE + " = '" + Const.ETC_TYPE.MEMO + "' and " + KEY_F_ID + " in " +
				"( select " + KEY_ID + " from " + TABLE_MEMO + " where " + KEY_CATEGORY_ID + " = " + id +"))";

		String sqlRelation = " delete from " + TABLE_ALARAM_RELATION + " where " +
				KEY_TYPE + " = '" + Const.ETC_TYPE.MEMO + "' and " + KEY_F_ID + " in " +
				"( select " + KEY_ID + " from " + TABLE_MEMO + " where " + KEY_CATEGORY_ID + " = " + id +")";

		try {
			db.execSQL(sqlAlarm);
			db.execSQL(sqlRelation);
			ContentValues values = new ContentValues();
			values.put(KEY_USE_YN, 0);
			db.update(TABLE_CATEGORY, values, KEY_ID + "=?", new String[]{String.valueOf(id)});
			db.update(TABLE_MEMO, values, KEY_CATEGORY_ID + "=?", new String[]{String.valueOf(id)});
			db.setTransactionSuccessful();
		}
		catch (Exception e){
			result = false;
		}
		finally{
			db.endTransaction();
			closeDB();
		}

		return result;
	}

	public void insertCategory(CategoryVO item) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, item.getTitle());
		values.put(KEY_TYPE, item.getType());
		values.put(KEY_USE_YN, 1);
		values.put(KEY_SORT, 1);

		long _id = db.insert(TABLE_CATEGORY, null, values);

		if(_id == -1){
			Log.e(Const.DEBUG_TAG, "DB category INSERT ERROR");
			throw new Error("DB category INSERT ERROR");
		}
		else
			item.setId(_id);
	}
}
