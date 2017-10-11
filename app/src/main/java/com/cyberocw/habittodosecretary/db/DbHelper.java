package com.cyberocw.habittodosecretary.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by cyberocw on 2015-08-23.
 */
public class DbHelper extends SQLiteOpenHelper {
	private static DbHelper sInstance;

	private static final String DB_NME = "habit_todo";
	private static final int DB_VERSION = 21;

	private static final String ARRAY_DIV = "_ho8c7wt_";

	public static final String TABLE_ALARM = "alarm";
	public static final String TABLE_ALARM_DATE = "alarm_date";
	public static final String TABLE_ALARM_REPEAT = "alarm_repeat";
	public static final String TABLE_ALARM_ORDER = "alarm_order";
	public static final String TABLE_TIMER = "timer";
	public static final String TABLE_MEMO = "memo";
	public static final String TABLE_CATEGORY = "category";
	public static final String TABLE_ALARAM_RELATION = "alarm_relation";
	public static final String TABLE_HOLIDAY = "holiday";
	public static final String TABLE_FILE_INFO = "file_info";

	public static final String KEY_ID = "_id";
	public static final String KEY_REPEAT_ID = "_rid";
	public static final String KEY_DATE_ID = "_mid";
	public static final String KEY_ALARM_TITLE = "alarm_title";
	public static final String KEY_ALARM_TYPE = "alarm_type";
	public static final String KEY_ALARM_CALL_TYPE = "alarm_call_type";
	public static final String KEY_ALARM_DATE_TYPE = "alarm_date_type";
	public static final String KEY_ALARM_OPTION = "alarm_option";
	public static final String KEY_HOUR = "hour";
	public static final String KEY_MINUTE = "minute";
	public static final String KEY_SECOND = "second";
	public static final String KEY_REPEAT_DAY = "repeat_day";
	public static final String KEY_ALARM_CALL_LIST = "alarm_call_list";
	public static final String KEY_ALARM_CONTENTS = "alarm_Contents"; // key_type 이랑 동일하게 씀
	public static final String KEY_ALARM_REMINDER_TYPE = "alarm_reminder_type"; //


	public static final String KEY_HOLIDAY_ALL = "is_holiday_all";
	public static final String KEY_HOLIDAY_NONE = "is_holiday_none";

	public static final String KEY_ALARM_DATE = "alarm_date";
	public static final String KEY_F_ALARM_ID = "alarm_id";
	public static final String KEY_F_ID = "foreign_id";


	public static final String KEY_MON = "mon";
	public static final String KEY_TUE = "tue";
	public static final String KEY_WED = "wed";
	public static final String KEY_THU = "thu";
	public static final String KEY_FRI = "fri";
	public static final String KEY_SAT = "sat";
	public static final String KEY_SUN = "sun";

	public static final String KEY_TIME_STAMP = "time_stamp";
	public static final String KEY_CALL_TIME = "call_time";
	public static final String KEY_USE_YN = "use_yn";

	public static final String KEY_CREATE_DATE = "create_dt";
	public static final String KEY_UPDATE_DATE = "update_dt";

	public static final String KEY_CONTENTS = "contents";
	public static final String KEY_TITLE = "title";
	public static final String KEY_CATEGORY_ID = "category_id";
	public static final String KEY_URL = "url";
	public static final String KEY_VIEW_CNT = "view_cnt";
	public static final String KEY_RANK = "rank";

	public static final String KEY_TYPE = "type";
	public static final String KEY_SORT = "sort_order";

	// holiday
	public static final String KEY_NAME = "name";
	public static final String KEY_YEAR = "year";
	public static final String KEY_MONTH = "month";
	public static final String KEY_DAY = "day";
	public static final String KEY_FULL_DATE = "full_date";

	//file
	public static final String KEY_URI = "uri";
	public static final String KEY_SIZE = "size";
	public static final String KEY_LENGTH = "length";
	public static final String KEY_MIME_TYPE = "MIME_TYPE";
	public static Context mContext;

	public static synchronized DbHelper getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		mContext= context;
		if (sInstance == null) {
			sInstance = new DbHelper(context.getApplicationContext());
		}
		return sInstance;
	}

	protected DbHelper(Context context) {
		super(context, DB_NME, null, DB_VERSION);
		mContext= context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//alarmTitle, alarmType(진동,소리 등), alarmOption(타이머,시간지정), hour, minute, mArrAlarmCall(몇분전 알림 목록), mDataRepeatDay

		String sql = "create table " + TABLE_ALARM + " (" +
				KEY_ID + " integer primary key autoincrement, " +
				KEY_ALARM_TITLE + " text, " +
				KEY_ALARM_TYPE + " integer, " +
				KEY_ALARM_DATE_TYPE + " integer, " +
				KEY_ALARM_OPTION + " integer, " +
				KEY_HOUR + " integer, " +
				KEY_MINUTE + " integer, " +
				KEY_ALARM_CALL_LIST + " text, " +
				KEY_ALARM_CONTENTS + " text," +
				KEY_TYPE + " text," +
				KEY_USE_YN + " integer, " +
				KEY_CREATE_DATE + " integer, " +
				KEY_UPDATE_DATE + " integer, " +
				KEY_HOLIDAY_ALL + " integer, " +
				KEY_HOLIDAY_NONE + " integer, " +
				KEY_ALARM_REMINDER_TYPE + " integer, " +
				KEY_ALARM_CALL_TYPE  + " integer" +
				");";

		String sql2 = "create table " + TABLE_ALARM_DATE + " (" +
				KEY_ID + " integer primary key autoincrement, " +
				KEY_ALARM_DATE + " integer, " +
				KEY_F_ALARM_ID + " integer " +
				");CREATE INDEX " + TABLE_ALARM_DATE + " alarm_date_idx ON " + TABLE_ALARM_DATE + "(" + KEY_ALARM_DATE + ");";

		String sql3 = "create table " + TABLE_ALARM_REPEAT + " (" +
				KEY_ID + " integer primary key autoincrement, " +
				KEY_MON + " integer, " +
				KEY_TUE + " integer, " +
				KEY_WED + " integer, " +
				KEY_THU + " integer, " +
				KEY_FRI + " integer, " +
				KEY_SAT + " integer, " +
				KEY_SUN + " integer, " +
				KEY_REPEAT_DAY + " integer, " +
				KEY_F_ALARM_ID + " integer" +
				");";

		String sql4 = "create table " + TABLE_ALARM_ORDER + " (" +
				KEY_ID + " integer primary key autoincrement, " +
				KEY_TIME_STAMP + " integer, " +
				KEY_CALL_TIME + " integer, " +
				KEY_USE_YN + " integer, " +
				KEY_F_ALARM_ID + " integer" +
				");CREATE INDEX " + TABLE_ALARM_ORDER + " time_stamp_idx ON " + TABLE_ALARM_ORDER + "(" + KEY_TIME_STAMP + ");";

		String sql5 = "create table " + TABLE_TIMER + " (" +
				KEY_ID + " integer primary key autoincrement, " +
				KEY_ALARM_TITLE + " text, " +
				KEY_ALARM_TYPE + " integer, " +
				KEY_ALARM_OPTION + " integer DEFAULT 0 , " +
				KEY_HOUR + " integer, " +
				KEY_MINUTE + " integer, " +
				KEY_SECOND + " integer, " +
				KEY_ALARM_CONTENTS + " text, " +
				KEY_CREATE_DATE + " integer, " +
				KEY_UPDATE_DATE + " integer " +
				");CREATE INDEX " + TABLE_TIMER + " timer_create_date_idx ON " + TABLE_TIMER + "(" + KEY_CREATE_DATE + ");";

		db.beginTransaction();
		try {
			db.execSQL(sql);
			db.execSQL(sql2);
			db.execSQL(sql3);
			db.execSQL(sql4);
			db.execSQL(sql5);

			db.execSQL(getCreateTableQuery(TABLE_MEMO));
			db.execSQL(getCreateTableQuery(TABLE_CATEGORY));
			/*ContentValues values = new ContentValues();
			values.put(KEY_TITLE, "Uncategorized");
			values.put(KEY_TYPE, "category");
			values.put(KEY_USE_YN, 1);
			values.put(KEY_SORT, 0);
			db.insert(TABLE_CATEGORY, null, values);*/

			db.execSQL(getCreateTableQuery(TABLE_ALARAM_RELATION));
			db.execSQL(getCreateTableQuery(TABLE_HOLIDAY));
			db.execSQL(getCreateTableQuery(TABLE_FILE_INFO));

			db.setTransactionSuccessful();
			Crashlytics.log(Log.DEBUG, Const.DEBUG_DB_TAG, "DB CREATE SUCCESS OCW");
		} catch (Exception e) {
			e.printStackTrace();
			Crashlytics.log(Log.DEBUG, Const.DEBUG_DB_TAG, "DB CREATE Exception " + e.getMessage() + " clause=" + e.getCause());
		} finally {

			db.endTransaction();
			Crashlytics.log(Log.DEBUG, Const.DEBUG_DB_TAG, "DB END Transaction OCW");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// db = 적용할 db, old/new 구 버전/신버전
        /*
         * db 버전이 업그레이드 되었을 때 실행되는 메소드
         * 이 부분은 사용에 조심해야 하는 일이 많이 있다. 버전이 1인 사용자가 2로 바뀌면
         * 한번의 수정만 하면 되지만 버전이 3으로 되면 1인 사용자가 2, 3을 거쳐야 하고
         * 2인 사용자는 3 까지만 거치면 된다. 이렇게 증가할 수록 수정할 일이 많아지므로
         * 적절히 사용해야 하며 가능하면 최초 설계 시에 완벽을 기하는 것이 가장 좋을 것이다.
         * 테스트에서는 기존의 데이터를 모두 지우고 다시 만드는 형태로 하겠다.
         */
		//dropTable(db);
		//onCreate(db); // 테이블을 지웠으므로 다시 테이블을 만들어주는 과정

		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "db version old=" + oldVersion + " new=" + newVersion);

		if(oldVersion == 1)
			onCreate(db);
		if(oldVersion == 5) {
			db.execSQL(getCreateTableQuery(TABLE_TIMER));
		}
		if(oldVersion == 6){
			db.execSQL(getCreateTableQuery(TABLE_MEMO));
			db.execSQL(getCreateTableQuery(TABLE_CATEGORY));
			db.execSQL(getCreateTableQuery(TABLE_ALARAM_RELATION));
		}
		if(oldVersion >= 6 && oldVersion <= 7){
			String sql = "ALTER TABLE " + TABLE_CATEGORY + " ADD COLUMN " + KEY_USE_YN + " integer ;";
			db.execSQL(sql);
			sql = "ALTER TABLE " + TABLE_ALARM + " ADD COLUMN " + KEY_TYPE + " text ;";
			db.execSQL(sql);
		}
		if(oldVersion < 10){
			String sql = "ALTER TABLE " + TABLE_ALARM + " ADD COLUMN " + KEY_HOLIDAY_ALL + " integer ;";
			db.execSQL(sql);
			sql = "ALTER TABLE " + TABLE_ALARM + " ADD COLUMN " + KEY_HOLIDAY_NONE + " integer ;";
			db.execSQL(sql);
		}
		if(oldVersion < 11){
			db.execSQL(getCreateTableQuery(TABLE_HOLIDAY));
		}
		if(oldVersion < 12){
			String sql = "ALTER TABLE " + TABLE_HOLIDAY + " ADD COLUMN " + KEY_FULL_DATE + " integer ; ";
			sql += " CREATE INDEX " + TABLE_HOLIDAY + " holiday_create_full_day_idx ON " + TABLE_HOLIDAY + "(" + KEY_FULL_DATE + ");";
			db.execSQL(sql);
		}
		if(oldVersion < 13){
			String sql = "ALTER TABLE " + TABLE_ALARM_REPEAT + " ADD COLUMN " + KEY_REPEAT_DAY + " integer ; ";
			db.execSQL(sql);
		}
		if(oldVersion < 14){
			String sql = "UPDATE " + TABLE_CATEGORY + " SET " + KEY_SORT + " = 1 ; ";
			db.execSQL(sql);
			ContentValues values = new ContentValues();
			values.put(KEY_TITLE, "미지정");
			values.put(KEY_TYPE, "category");
			values.put(KEY_USE_YN, 1);
			values.put(KEY_SORT, 0);
			db.insert(TABLE_CATEGORY, null, values);
		}
		if(oldVersion < 15){
			String sql = "ALTER TABLE " + TABLE_MEMO + " ADD COLUMN " + KEY_USE_YN + " integer ; ";
			db.execSQL(sql);
			String sql2 = "UPDATE " + TABLE_MEMO + " SET " + KEY_USE_YN + " = 1 ; ";
			db.execSQL(sql2);
		}
		if(oldVersion == 15){
			ContentValues values = new ContentValues();
			values.put(KEY_TITLE, "미지정");
			values.put(KEY_TYPE, "category");
			values.put(KEY_USE_YN, 1);
			values.put(KEY_SORT, 0);
			db.insert(TABLE_CATEGORY, null, values);
		}
		if(oldVersion <= 16){
			String sql = "ALTER TABLE " + TABLE_ALARM + " ADD COLUMN " + KEY_ALARM_CALL_TYPE  + " integer DEFAULT 0 ;";
			db.execSQL(sql);
			String sql2 = "UPDATE " + TABLE_ALARM + " SET " + KEY_ALARM_CALL_TYPE + " = 1 where " + KEY_ALARM_TYPE  + " = 1 ; ";
			db.execSQL(sql2);
		}
		if(oldVersion <=17){
			//sound - alarm option , alarm type = 끌때
			String sql = "ALTER TABLE " + TABLE_TIMER + " ADD COLUMN " + KEY_ALARM_OPTION + " integer DEFAULT 0 ;";
			db.execSQL(sql);
			String sql2 = "UPDATE " + TABLE_TIMER + " SET " + KEY_ALARM_TYPE + " = 1 ; ";
			db.execSQL(sql2);
		}
		if(oldVersion <=18) {
			//sound - alarm option , alarm type = 끌때
			String sql = "UPDATE " + TABLE_MEMO + " SET " + KEY_TYPE + " = 'MEMO' WHERE " + KEY_TYPE + " IS NULL";
			db.execSQL(sql);
		}
		if(oldVersion <=19){
			db.execSQL(getCreateTableQuery(TABLE_FILE_INFO));
		}
		if(oldVersion <=20) {
			//mode 추가 (전광판 모드)
			String sql = "ALTER TABLE " + TABLE_ALARM+ " ADD COLUMN " + KEY_ALARM_REMINDER_TYPE + " integer DEFAULT 0 ;";
			db.execSQL(sql);
		}
	}

	private String getCreateTableQuery(String tableName){
		String sql = "";

		switch (tableName) {
			case TABLE_CATEGORY:
				sql = "create table if not exists " + TABLE_CATEGORY + " (" +
						KEY_ID + " integer primary key autoincrement, " +
						KEY_TITLE + " text, " +
						KEY_TYPE + " text, " +
						KEY_SORT + " integer, " +
						KEY_USE_YN + " integer " +
						");CREATE INDEX if not exists " + TABLE_CATEGORY + " category_sort_order ON " + TABLE_CATEGORY + "(" + KEY_SORT + ");";
				sql += "CREATE INDEX if not exists " + TABLE_CATEGORY + " category_title ON " + TABLE_CATEGORY + "(" + KEY_TITLE + ");";
				break;
			case TABLE_MEMO:
				sql = "create table if not exists " + TABLE_MEMO + " (" +
						KEY_ID + " integer primary key autoincrement, " +
						KEY_TYPE + " text, " +
						KEY_TITLE + " text, " +
						KEY_CONTENTS + " text, " +
						KEY_URL + " text, " +
						KEY_VIEW_CNT + " integer, " +
						KEY_RANK + " integer, " +
						KEY_CATEGORY_ID + " integer, " +
						KEY_USE_YN + " integer, " +
						KEY_CREATE_DATE + " integer, " +
						KEY_UPDATE_DATE + " integer " +
						");CREATE INDEX if not exists " + TABLE_MEMO + " memo_create_date_idx ON " + TABLE_MEMO + "(" + KEY_CREATE_DATE + ");";
				sql += "CREATE INDEX if not exists " + TABLE_MEMO + " memo_rank ON " + TABLE_MEMO + "(" + KEY_RANK + ");";
				break;
			case TABLE_ALARAM_RELATION:
				sql = "create table if not exists " + TABLE_ALARAM_RELATION + " (" +
						KEY_TYPE + " text , " +
						KEY_F_ALARM_ID + " integer , " +
						KEY_F_ID + " integer, " +
						"PRIMARY KEY ([" + KEY_TYPE + "],[" + KEY_F_ALARM_ID + "],[" + KEY_F_ID + "]))";
				break;
			case TABLE_HOLIDAY:
				sql = "create table if not exists " + TABLE_HOLIDAY + " (" +
						KEY_ID + " integer primary key autoincrement, " +
						KEY_YEAR + " integer , " +
						KEY_MONTH + " integer , " +
						KEY_DAY + " integer , " +
						KEY_TYPE + " text, " +
						KEY_NAME + " text," +
						KEY_FULL_DATE + " integer )" +
						";CREATE INDEX if not exists " + TABLE_HOLIDAY + " holiday_idx ON " + TABLE_HOLIDAY +
						"(" + KEY_YEAR + ", " + KEY_MONTH + "," + KEY_DAY + "," + KEY_FULL_DATE + ");";
				break;
			case TABLE_FILE_INFO :
				sql = "create table if not exists " + TABLE_FILE_INFO + " (" +
						KEY_ID + " integer primary key autoincrement, " +
						KEY_URI + " text , " +
						KEY_NAME + " text , " +
						KEY_SIZE + " integer , " +
						KEY_LENGTH + " integer, " +
						KEY_MIME_TYPE + " text," +
						KEY_TYPE + " text," +
						KEY_CREATE_DATE + " integer, " +
						KEY_F_ID + " integer )" +
						";CREATE INDEX if not exists " + TABLE_FILE_INFO + " fileinfo_idx ON " + TABLE_FILE_INFO +
						"(" + KEY_F_ID + ");";
				break;
		}

		return sql;
	}

	private void dropTable(SQLiteDatabase db){
		String sql = "drop table if exists " + TABLE_ALARM;
		String sql2 = "drop table if exists " + TABLE_ALARM_DATE;
		String sql3 = "drop table if exists " + TABLE_ALARM_REPEAT;
		String sql4 = "drop table if exists " + TABLE_ALARM_ORDER;
		String sql5 = "drop table if exists " + TABLE_TIMER;
		db.execSQL(sql);
		db.execSQL(sql2);
		db.execSQL(sql3);
		db.execSQL(sql4);
		db.execSQL(sql5);
	}

	public void closeDB() {

		//this.close();

		/*
		SQLiteDatabase db = this.getReadableDatabase();
		if (db != null && db.isOpen())
			db.close();
			*/
	}

	public String serialize(Object content[]){
		Arrays.sort(content);
		return TextUtils.join(ARRAY_DIV, content);
	}

	public String[] derialize(String content){
		return content.split(ARRAY_DIV);
	}

}
