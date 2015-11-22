package com.cyberocw.habittodosecretary.alaram.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmTimeVO;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.alaram.vo.TimerVO;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by cyberocw on 2015-08-23.
 */
public class DbHelper extends SQLiteOpenHelper {
	private static final String DB_NME = "habit_todo";
	private static final int DB_VERSION = 6;

	private static final String ARRAY_DIV = "_ho8c7wt_";

	public static final String TABLE_ALARM = "alarm";
	public static final String TABLE_ALARM_DATE = "alarm_date";
	public static final String TABLE_ALARM_REPEAT = "alarm_repeat";
	public static final String TABLE_ALARM_ORDER = "alarm_order";
	public static final String TABLE_TIMER = "timer";

	public static final String KEY_ID = "_id";
	public static final String KEY_REPEAT_ID = "_rid";
	public static final String KEY_DATE_ID = "_mid";
	public static final String KEY_ALARM_TITLE = "alarm_title";
	public static final String KEY_ALARM_TYPE = "alarm_type";
	public static final String KEY_ALARM_DATE_TYPE = "alarm_date_type";
	public static final String KEY_ALARM_OPTION = "alarm_option";
	public static final String KEY_HOUR = "hour";
	public static final String KEY_MINUTE = "minute";
	public static final String KEY_SECOND = "second";
	public static final String KEY_ALARM_CALL_LIST = "alarm_call_list";
	public static final String KEY_ALARM_CONTENTS = "alarm_Contents";

	public static final String KEY_ALARM_DATE = "alarm_date";
	public static final String KEY_F_ALARM_ID = "alarm_id";

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

	public DbHelper(Context context) {
		super(context, DB_NME, null, DB_VERSION);
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
				KEY_USE_YN + " integer, " +
				KEY_CREATE_DATE + " integer, " +
				KEY_UPDATE_DATE + " integer " +
				");";
		db.execSQL(sql);

		String sql2 = "create table " + TABLE_ALARM_DATE + " (" +
				KEY_ID + " integer primary key autoincrement, " +
				KEY_ALARM_DATE + " integer, " +
				KEY_F_ALARM_ID + " integer " +
				");";
		db.execSQL(sql2);

		String sql3 = "create table " + TABLE_ALARM_REPEAT + " (" +
				KEY_ID + " integer primary key autoincrement, " +
				KEY_MON + " integer, " +
				KEY_TUE + " integer, " +
				KEY_WED + " integer, " +
				KEY_THU + " integer, " +
				KEY_FRI + " integer, " +
				KEY_SAT + " integer, " +
				KEY_SUN + " integer, " +
				KEY_F_ALARM_ID + " integer" +
				");";
		db.execSQL(sql3);

		String sql4 = "create table " + TABLE_ALARM_ORDER + " (" +
				KEY_ID + " integer primary key autoincrement, " +
				KEY_TIME_STAMP + " integer, " +
				KEY_CALL_TIME + " integer, " +
				KEY_USE_YN + " integer, " +
				KEY_F_ALARM_ID + " integer" +
				");CREATE INDEX " + TABLE_ALARM_ORDER + " time_stamp_idx ON " + TABLE_ALARM_ORDER + "(" + KEY_TIME_STAMP + ");";
		db.execSQL(sql4);

		String sql5 = "create table " + TABLE_TIMER + " (" +
				KEY_ID + " integer primary key autoincrement, " +
				KEY_ALARM_TITLE + " text, " +
				KEY_ALARM_TYPE + " integer, " +
				KEY_HOUR + " integer, " +
				KEY_MINUTE + " integer, " +
				KEY_SECOND + " integer, " +
				KEY_ALARM_CONTENTS + " text, " +
				KEY_CREATE_DATE + " integer, " +
				KEY_UPDATE_DATE + " integer " +
				");CREATE INDEX " + TABLE_TIMER + " timer_create_date_idx ON " + TABLE_TIMER + "(" + KEY_CREATE_DATE + ");";

		sql5 += "CREATE INDEX " + TABLE_ALARM_DATE + " alarm_date_idx ON " + TABLE_ALARM_DATE + "(" + KEY_ALARM_DATE + ");";

		db.execSQL(sql5);
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

		Log.d(Const.DEBUG_TAG, "db version old=" + oldVersion + " new=" + newVersion);

		if(oldVersion == 1)
			onCreate(db);
		if(oldVersion == 5) {
			String sql5 = "create table if not exists " + TABLE_TIMER + " (" +
					KEY_ID + " integer primary key autoincrement, " +
					KEY_ALARM_TITLE + " text, " +
					KEY_ALARM_TYPE + " integer, " +
					KEY_HOUR + " integer, " +
					KEY_MINUTE + " integer, " +
					KEY_SECOND + " integer, " +
					KEY_ALARM_CONTENTS + " text, " +
					KEY_CREATE_DATE + " integer, " +
					KEY_UPDATE_DATE + " integer " +
					");CREATE INDEX if not exists " + TABLE_TIMER + " timer_create_date_idx ON " + TABLE_TIMER + "(" + KEY_CREATE_DATE + ");";
			sql5 += "CREATE INDEX if not exists " + TABLE_ALARM_DATE + " alarm_date_idx ON " + TABLE_ALARM_DATE + "(" + KEY_ALARM_DATE + ");";

			db.execSQL(sql5);
		}
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

	public boolean insertAlarm(AlarmVO vo){
		Log.d(Const.DEBUG_TAG, "serialize(vo.getAlarmCallList() = " + serialize(vo.getAlarmCallList().toArray()));

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ALARM_TITLE, vo.getAlarmTitle());
		values.put(KEY_ALARM_TYPE, vo.getAlarmType());
		values.put(KEY_ALARM_DATE_TYPE, vo.getAlarmDateType());
		values.put(KEY_ALARM_OPTION, vo.getAlarmOption());
		values.put(KEY_HOUR, vo.getHour());
		values.put(KEY_MINUTE, vo.getMinute());

		Calendar c = vo.getCreateDt();

		if(c == null)
			c = Calendar.getInstance();
		values.put(KEY_CREATE_DATE, c.getTimeInMillis());

		c = vo.getUpdateDt();

		if(c == null)
			c = Calendar.getInstance();

		values.put(KEY_UPDATE_DATE, c.getTimeInMillis());

		values.put(KEY_USE_YN, 1);

		values.put(KEY_ALARM_CALL_LIST, serialize(vo.getAlarmCallList().toArray()));
		//values.put(KEY_ALARM_CONTENTS, vo.getA());

		long id = db.insert(TABLE_ALARM, null, values);

		if(id == -1){
			Log.e(Const.DEBUG_TAG, "DB Alarm INSERT ERROR");
			throw new Error("DB Alarm INSERT ERROR");
		}
		vo.setId(id);

		// 날짜 지정 알람
		ArrayList<Calendar> dateList = vo.getAlarmDateList();

		if(dateList != null && !dateList.isEmpty()) {
			insertDate(id, dateList);
			//날짜 시간 순서 테이블에 삽입
			insertAlarmCallOrder(vo);
		}

		// 반복 알람
		ArrayList<Integer> reDateList = vo.getRepeatDay();

		if(reDateList != null && !reDateList.isEmpty()){
			insertRepeatDate(id, reDateList);
		}
		closeDB();
		return true;
	}


	public boolean modifyAlarm(AlarmVO vo) {
		//여차하면 다 삭제하고 새로 insert해도 됨
		if(deleteAlarm(vo.getId()) == false)
			return false;
		return this.insertAlarm(vo);
	}

	public boolean modifyUse(AlarmVO vo){
		Log.d(Const.DEBUG_TAG, "db modify use");

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_USE_YN, vo.getUseYn());

		int affRow = db.update(TABLE_ALARM, values, KEY_ID + "=" + vo.getId(), null);

		closeDB();

		if(affRow < 1){
			Log.e(Const.DEBUG_TAG, "DB Alarm USE UPDATE ERROR");
			return false;
		}

		return true;
	}

	/*
	시간형태로 알림을 위한 테이블에 등록
	KEY_ID + " integer primary key autoincrement, " +
				KEY_TIME_STAMP + " integer, " +
				KEY_CALL_TIME + " integer, " +
				KEY_USE_YN + " integer, " +
				KEY_F_ALARM_ID + " integer" +
	 */
	private void insertAlarmCallOrder(AlarmVO vo) {
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<Calendar> dateList = vo.getAlarmDateList();
		Calendar cal, cal2;
		ArrayList<Integer> callList;

		callList = vo.getAlarmCallList();

		for(int i = 0 ; i < dateList.size() ; i++) {
			cal = dateList.get(i);
			cal.set(Calendar.HOUR_OF_DAY, vo.getHour());
			cal.set(Calendar.MINUTE, vo.getMinute());
			cal.set(Calendar.SECOND, 0);

			for (int j = 0; j < callList.size(); j++) {
				cal2 = (Calendar) cal.clone();
				cal2.add(Calendar.MINUTE, callList.get(j));

				ContentValues values = new ContentValues();
				values.put(KEY_TIME_STAMP, cal2.getTimeInMillis());
				values.put(KEY_CALL_TIME, callList.get(j));
				values.put(KEY_USE_YN, 1);
				values.put(KEY_F_ALARM_ID, vo.getId());

				db.insert(TABLE_ALARM_ORDER, null, values);
			}
		}
	}

	public ArrayList<AlarmTimeVO> getMinAlarmTime(){
		Calendar cal = Calendar.getInstance();
		return getMinAlarmTime(cal.getTimeInMillis());
	}

	//시간 순서에 따라 가장 가까운 시간 알림 가져옴
	public ArrayList<AlarmTimeVO> getMinAlarmTime(long nowTime){
		String selectQuery =
				"SELECT A." + KEY_ID + ", A." + KEY_TIME_STAMP + ", A." + KEY_CALL_TIME + ", B." + KEY_ALARM_TITLE + ", A." + KEY_F_ALARM_ID +
						" FROM " + TABLE_ALARM_ORDER +" AS A INNER JOIN " + TABLE_ALARM + " AS B ON " +
						" A." + KEY_F_ALARM_ID + " = B." + KEY_ID + " WHERE  B." + KEY_USE_YN + " = 1 AND A." + KEY_TIME_STAMP + " = " +
						" (SELECT MIN(" + KEY_TIME_STAMP + " ) FROM " + TABLE_ALARM_ORDER + " AS C INNER JOIN " + TABLE_ALARM +
						" AS D ON C." + KEY_F_ALARM_ID + " = D." + KEY_ID + " WHERE D." + KEY_USE_YN + " = 1 AND " + KEY_TIME_STAMP + " > " + nowTime + ")";

		Log.e(Const.DEBUG_TAG, selectQuery);

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);

		ArrayList<AlarmTimeVO> alarmTimeVOList = new ArrayList<AlarmTimeVO>();
		AlarmTimeVO vo;

		if (c.moveToFirst()) {
			do {
				vo = new AlarmTimeVO();
				vo.setId(c.getLong((c.getColumnIndex(KEY_ID))));
				vo.setAlarmTitle((c.getString(c.getColumnIndex(KEY_ALARM_TITLE))));
				vo.setTimeStamp(c.getLong(c.getColumnIndex(KEY_TIME_STAMP)));
				vo.setCallTime(c.getInt(c.getColumnIndex(KEY_CALL_TIME)));
				vo.setfId(c.getLong(c.getColumnIndex(KEY_F_ALARM_ID)));

				alarmTimeVOList.add(vo);
			} while (c.moveToNext());
		}
		return alarmTimeVOList;
	}

	private void insertDate(long id, ArrayList<Calendar> dateList){
		for (int i = 0; i < dateList.size(); i++) {
			insertDate(id, dateList.get(i));
		}
	}

	private void insertDate(long id, Calendar cal) {
		SQLiteDatabase db = this.getWritableDatabase();

		Log.d(Const.DEBUG_TAG, "insertDate before day = " + cal.get(Calendar.MONTH) + "day2 = " + cal.get(Calendar.DAY_OF_MONTH) + " id=" + id);

		ContentValues values = new ContentValues();
		values.put(KEY_ALARM_DATE, convertDateType(cal));
		values.put(KEY_F_ALARM_ID, id);

		Log.d(Const.DEBUG_TAG, "insertDate day = " + convertDateType(cal) + " id=" + id);

		long _id = db.insert(TABLE_ALARM_DATE, null, values);

		if(_id == -1){
			Log.e(Const.DEBUG_TAG, "DB Date INSERT ERROR");
			throw new Error("DB Date INSERT ERROR");
		}
	}

	private void insertRepeatDate(long id, ArrayList<Integer> reDateList){
		SQLiteDatabase db = this.getWritableDatabase();
		Integer day = null;
		ContentValues values = new ContentValues();

		for(int i = 0 ; i < reDateList.size(); i++){
			day = reDateList.get(i);
			switch(day){
				case Calendar.MONDAY :
					values.put(KEY_MON, 1);break;
				case Calendar.TUESDAY :
					values.put(KEY_TUE, 1);break;
				case Calendar.WEDNESDAY :
					values.put(KEY_WED, 1);break;
				case Calendar.THURSDAY :
					values.put(KEY_THU, 1);break;
				case Calendar.FRIDAY:
					values.put(KEY_FRI, 1);break;
				case Calendar.SATURDAY :
					values.put(KEY_SAT, 1);break;
				case Calendar.SUNDAY :
					values.put(KEY_SUN, 1);break;
			}
		}

		values.put(KEY_F_ALARM_ID, id);

		long _id = db.insert(TABLE_ALARM_REPEAT, null, values);

		if(_id == -1){
			Log.e(Const.DEBUG_TAG, "DB Date INSERT ERROR");
			throw new Error("DB Date INSERT ERROR");
		}
	}

	//반복 알람 가장 가까운 시간 가져오기
	public ArrayList<AlarmTimeVO> getMinRepeatAlarm(int dayNum) {
		Log.d(Const.DEBUG_TAG, "getMinRepeatAlarm start");

		HashMap<Integer, Integer> dayMap = new HashMap<>();
		dayMap.put(Calendar.SUNDAY, 0);
		dayMap.put(Calendar.MONDAY, 0);
		dayMap.put(Calendar.TUESDAY, 0);
		dayMap.put(Calendar.WEDNESDAY, 0);
		dayMap.put(Calendar.THURSDAY, 0);
		dayMap.put(Calendar.FRIDAY, 0);
		dayMap.put(Calendar.SATURDAY, 0);

		int day;

		String[] arrDay = {KEY_SUN, KEY_MON, KEY_TUE, KEY_WED, KEY_THU, KEY_FRI, KEY_SAT};
		String[] arrDayResult = new String[7];
		//int cnt = 0;
		Iterator a = dayMap.keySet().iterator();
		for (Map.Entry<Integer, Integer> entry : dayMap.entrySet()){
			day = entry.getKey();
			if(day >= dayNum){
				dayMap.put(day, day - dayNum);
				arrDayResult[day-dayNum ] = arrDay[day-1];
			}
			else {
				dayMap.put(day, day + 7 - dayNum);
				arrDayResult[day + 7 - dayNum] = arrDay[day-1];
			}
		}
		SQLiteDatabase db = this.getReadableDatabase();

		String queryString;
		String[] alarmCallList;
		int hour, minute, searchDayIndex = 0;
		long timeinMil, min = 0;

		AlarmTimeVO alarmTimeVO ;
		ArrayList<AlarmTimeVO> arrList = new ArrayList<>();
		Calendar cal, nowCal = Calendar.getInstance(), cal2;
		nowCal.set(Calendar.SECOND, 0);

		long nowTimeInMil = nowCal.getTimeInMillis();

		//7번 반복
		for(int i = 0; i < arrDayResult.length; i++) {
			queryString = "SELECT B." + KEY_ID + ", B." + KEY_ALARM_CALL_LIST + ", B." + KEY_ALARM_TITLE + ", B."+KEY_HOUR + ", B." + KEY_MINUTE + ", A." + KEY_F_ALARM_ID +
				" FROM " + TABLE_ALARM_REPEAT + " A LEFT JOIN " + TABLE_ALARM + " B ON A." + KEY_F_ALARM_ID + " = B." + KEY_ID  + " WHERE " +
				" B." + KEY_USE_YN + " = 1 AND A." + arrDayResult[i] + " = 1 ORDER BY B." + KEY_HOUR + ", B." + KEY_MINUTE;

			Cursor c = db.rawQuery(queryString, null);

			if (c.moveToFirst()) {
				do {
					//c.getString(c.getColumnIndex(KEY_ALARM_TITLE))
					//Log.d(Const.DEBUG_TAG, "title = " + c.getString(c.getColumnIndex(KEY_ALARM_TITLE)));
					hour =  c.getInt(c.getColumnIndex(KEY_HOUR));
					minute = c.getInt(c.getColumnIndex(KEY_MINUTE));
					alarmCallList = derialize(c.getString(c.getColumnIndex(KEY_ALARM_CALL_LIST)));

					//오늘 날짜 기준으로 초기화
					cal = (Calendar) nowCal.clone();
					cal.set(Calendar.HOUR_OF_DAY, hour);
					cal.set(Calendar.MINUTE, minute);

					//지금 시간 이전과 같으면 통과
					if( i == 0 && nowTimeInMil >= cal.getTimeInMillis()){
						Log.d(Const.DEBUG_TAG, "continue ok");
						continue;
					}
					//오늘 기준 하루씩 추가
					if (i > 0)
						cal.add(Calendar.DAY_OF_MONTH, i);

					// 몇분전 값이 실제 시간이기 때문에 이부분에서 최소 값을 찾음
					for(int j = 0 ; j < alarmCallList.length; j++) {
						cal2 = (Calendar) cal.clone();
						cal2.add(Calendar.MINUTE, Integer.valueOf(alarmCallList[j]));
						timeinMil = cal2.getTimeInMillis();

						// - 몇분전 계산 값이 현재 시간보다 빠르면 건너 뜀
						if(nowTimeInMil >= timeinMil)
							continue;

						if (min >= timeinMil || min == 0) {
							if(min > timeinMil){
								arrList.clear();
							}
							min = timeinMil;

							alarmTimeVO = new AlarmTimeVO();
							alarmTimeVO.setCallTime(Integer.valueOf(alarmCallList[j]));
							alarmTimeVO.setTimeStamp(timeinMil);
							alarmTimeVO.setAlarmTitle(c.getString(c.getColumnIndex(KEY_ALARM_TITLE)));
							alarmTimeVO.setId(c.getInt(c.getColumnIndex(KEY_ID)));
							alarmTimeVO.setfId(c.getInt(c.getColumnIndex(KEY_F_ALARM_ID)));

							arrList.add(alarmTimeVO);
						}
					}
				} while (c.moveToNext());
			}
			if(searchDayIndex >= 1)
				break;
			// 한번 찾은 뒤 다음 날 더 찾아보고 중지
			if(searchDayIndex == 0 && arrList.size() > 0)
				searchDayIndex = 1;
		}
		closeDB();
		return arrList;
	}

	public boolean deleteAlarm(long id) {
		// TODO: 2015-08-30 알림 반복인데 오늘만 삭제 때 어떻게 할지 구현 해야 함
		SQLiteDatabase db = this.getWritableDatabase();

		db.beginTransaction();
		boolean result = true;
		try {
			db.delete(TABLE_ALARM, KEY_ID + "=?", new String[]{String.valueOf(id)});
			db.delete(TABLE_ALARM_REPEAT, KEY_F_ALARM_ID + "=?", new String[]{String.valueOf(id)});
			db.delete(TABLE_ALARM_DATE, KEY_F_ALARM_ID + "=?", new String[]{String.valueOf(id)});
			db.delete(TABLE_ALARM_ORDER, KEY_F_ALARM_ID + "=?", new String[]{String.valueOf(id)});
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

	/**
	 * getting all todos
	 * */
	public ArrayList<AlarmVO> getAlarmList(Calendar date){
		int[] day = {date.get(Calendar.DAY_OF_WEEK)};
		Calendar c = Calendar.getInstance();
		c.setTime(date.getTime());
		c.add(Calendar.DAY_OF_MONTH, 1);

		return getAlarmList(date, null, day);
	}

	private ArrayList<AlarmVO> getAlarmList(Calendar startDate, Calendar endDate, int[] dayName) {
		ContentValues values = new ContentValues();

		for (int i = 0; i < dayName.length; i++) {
			switch (dayName[i]) {
				case Calendar.MONDAY:
					values.put(KEY_MON, 1);
					break;
				case Calendar.TUESDAY:
					values.put(KEY_TUE, 1);
					break;
				case Calendar.WEDNESDAY:
					values.put(KEY_WED, 1);
					break;
				case Calendar.THURSDAY:
					values.put(KEY_THU, 1);
					break;
				case Calendar.FRIDAY:
					values.put(KEY_FRI, 1);
					break;
				case Calendar.SATURDAY:
					values.put(KEY_SAT, 1);
					break;
				case Calendar.SUNDAY:
					values.put(KEY_SUN, 1);
					break;
			}
		}
		String selectQuery =
				"SELECT  A.*, B." + KEY_ID + " as " + KEY_REPEAT_ID + ", C." + KEY_ID + " as " + KEY_DATE_ID + ", sun, mon, tue, wed, thu, fri, sat, C." +
						KEY_ALARM_DATE +" FROM " + TABLE_ALARM + " AS A LEFT JOIN " +
				TABLE_ALARM_REPEAT + " AS B ON A." + KEY_ID + " = B."+ KEY_F_ALARM_ID + " LEFT JOIN " +
				TABLE_ALARM_DATE + " AS C ON A." + KEY_ID + " = C." + KEY_F_ALARM_ID +
					" WHERE A." + KEY_ID + " IN ";

		if (values.size() > 0) {
			selectQuery += "(SELECT " + KEY_F_ALARM_ID + " FROM " + TABLE_ALARM_REPEAT + " WHERE 1=1 ";
			for (String key : values.keySet()) {
				selectQuery += " AND " + key + " = 1";
			}
			selectQuery += ")";
		}

		//endDate가 null이 아닐때 (주간 리스트 보여줄때 사용 예정)
		if (startDate != null && endDate != null ) {
			selectQuery += " OR A." + KEY_ID + " IN (SELECT " + KEY_F_ALARM_ID + " FROM " + TABLE_ALARM_DATE + " WHERE " +
			KEY_ALARM_DATE + " BETWEEN " + convertDateType(startDate) + " AND " + convertDateType(endDate) + ")";
		}
		//endDate가 null일때 - 현재 이 경우만 존재
		if (startDate != null && endDate == null ) {
			selectQuery += " OR A." + KEY_ID + " IN (SELECT " + KEY_F_ALARM_ID + " FROM " + TABLE_ALARM_DATE + " WHERE " +
					KEY_ALARM_DATE + " = " + convertDateType(startDate) + ")";
		}

		selectQuery += " ORDER BY C." + KEY_ALARM_DATE + " ASC, A." + KEY_HOUR + " ASC, A." + KEY_MINUTE + " ASC";

		Log.e(Const.DEBUG_TAG, selectQuery);

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);
		String tempData;

		String[] arrDateColumn = {KEY_MON, KEY_TUE, KEY_WED, KEY_THU, KEY_FRI, KEY_SAT, KEY_SUN}, arrTempString;

		ArrayList<Calendar> arrCal;
		ArrayList<Integer> arrRepeatDay;
		ArrayList<Integer> alarmaCallList = new ArrayList<Integer>();
		String alarmDate;
		// TODO: 2015-08-30 date가 여럿일 경우는 고려하지 않았음, 일단 여러개일 경우를 대비하기 위해 테이블은 분리해 둠

		Log.d(Const.DEBUG_TAG, "select result count = " + c.getCount());

		ArrayList<AlarmVO> alarmVOList = new ArrayList<AlarmVO>();
		HashMap<Long, AlarmVO> voMap = new HashMap<Long, AlarmVO>();

		AlarmVO vo;
		Calendar cc = Calendar.getInstance();

		if (c.moveToFirst()) {
			do {
				vo = new AlarmVO();
				vo.setId(c.getLong((c.getColumnIndex(KEY_ID))));
				alarmDate = c.getString(c.getColumnIndex(KEY_ALARM_DATE));

				//기존 ID 데이터가 있으면 date만 add하고 건너뜀
				if (voMap.containsKey(vo.getId())) {
					if (alarmDate != null && !"".equals(alarmDate)) {
						voMap.get(vo.getId()).getAlarmDateList().add(convertDateType(alarmDate));
					}
					continue;
				}
				arrCal = new ArrayList<Calendar>();
				if (alarmDate != null && !"".equals(alarmDate))
					arrCal.add(convertDateType(alarmDate));
				vo.setAlarmDateList(arrCal);
				vo.setAlarmTitle((c.getString(c.getColumnIndex(KEY_ALARM_TITLE))));
				vo.setAlarmType(c.getInt(c.getColumnIndex(KEY_ALARM_TYPE)));
				vo.setHour(c.getInt(c.getColumnIndex(KEY_HOUR)));
				vo.setMinute(c.getInt(c.getColumnIndex(KEY_MINUTE)));

				tempData = c.getString(c.getColumnIndex(KEY_ALARM_CALL_LIST));

				if (tempData != null && !"".equals(tempData)) {
					arrTempString = derialize(tempData);
					alarmaCallList = new ArrayList<Integer>();

					for (String a : arrTempString) {
						alarmaCallList.add(Integer.valueOf(a));
					}
					vo.setAlarmCallList(alarmaCallList);
				}
				vo.setAlarmDateType((c.getInt(c.getColumnIndex(KEY_ALARM_DATE_TYPE))));
				vo.setAlarmOption((c.getInt(c.getColumnIndex(KEY_ALARM_OPTION))));

				arrRepeatDay = new ArrayList<Integer>();

				for (int i = 0; i < arrDateColumn.length; i++) {

					if (c.getInt(c.getColumnIndex(arrDateColumn[i])) == 1)
						arrRepeatDay.add(Const.DAY.ARR_CAL_DAY[i]);
				}
				vo.setRepeatDay(arrRepeatDay);
				vo.setUseYn(c.getInt(c.getColumnIndex(KEY_USE_YN)));

				cc.setTimeInMillis(c.getInt(c.getColumnIndex(KEY_CREATE_DATE)));
				vo.setCreateDt((Calendar) cc.clone());

				cc.setTimeInMillis(c.getInt(c.getColumnIndex(KEY_UPDATE_DATE)));
				vo.setCreateDt((Calendar) cc.clone());

				Log.d(Const.DEBUG_TAG, "db title = " + vo.getAlarmTitle());
				Log.d(Const.DEBUG_TAG, "db setAlarmType = " + vo.getAlarmType());

				alarmVOList.add(vo);
				voMap.put(vo.getId(), vo);

				Log.d(Const.DEBUG_TAG, "selected VO = " + vo.toString());
			} while (c.moveToNext());
		}
		closeDB();
		return alarmVOList;
	}

	/*
	timer 관련
	 */
	public boolean insertTimer(TimerVO vo){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ALARM_TITLE, vo.getAlarmTitle());
		values.put(KEY_ALARM_TYPE, vo.getAlarmType());
		values.put(KEY_HOUR, vo.getHour());
		values.put(KEY_MINUTE, vo.getMinute());
		values.put(KEY_SECOND, vo.getSecond());

		Calendar c = vo.getCreateDt();

		if(c == null)
			c = Calendar.getInstance();
		values.put(KEY_CREATE_DATE, c.getTimeInMillis());

		c = vo.getUpdateDt();

		if(c == null)
			c = Calendar.getInstance();

		values.put(KEY_UPDATE_DATE, c.getTimeInMillis());
		//values.put(KEY_ALARM_CONTENTS, vo.getA());

		long id = db.insert(TABLE_TIMER, null, values);

		Log.d(Const.DEBUG_TAG, "insert timer id="+id);

		if(id == -1){
			Log.e(Const.DEBUG_TAG, "DB Alarm INSERT ERROR");
			throw new Error("DB Alarm INSERT ERROR");
		}
		vo.setId(id);

		closeDB();
		return true;
	}

	public ArrayList<TimerVO> getTimerList(){
		String selectQuery = " SELECT * FROM " + TABLE_TIMER ;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);

		ArrayList<TimerVO> timerList = new ArrayList<TimerVO>();
		TimerVO vo;

		if (c.moveToFirst()) {
			do {
				vo = new TimerVO();
				vo.setId(c.getLong((c.getColumnIndex(KEY_ID))));
				vo.setAlarmTitle((c.getString(c.getColumnIndex(KEY_ALARM_TITLE))));
				vo.setHour(c.getInt(c.getColumnIndex(KEY_HOUR)));
				vo.setMinute(c.getInt(c.getColumnIndex(KEY_MINUTE)));
				vo.setSecond(c.getInt(c.getColumnIndex(KEY_SECOND)));
				vo.setAlarmType(c.getInt(c.getColumnIndex(KEY_ALARM_TYPE)));
				//vo.setCreateDt(c.getInt(c.getColumnIndex(KEY_CREATE_DATE)));
				//vo.setUpdateDt(c.getInt(c.getColumnIndex(KEY_UPDATE_DATE)));

				timerList.add(vo);
			} while (c.moveToNext());
		}
		closeDB();
		return timerList;
	}

	public int updateTimer (TimerVO vo) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues values = new ContentValues();

		values.put(KEY_ALARM_TITLE, vo.getAlarmTitle());
		values.put(KEY_ALARM_TYPE, vo.getAlarmType());
		values.put(KEY_HOUR, vo.getHour());
		values.put(KEY_MINUTE, vo.getMinute());
		values.put(KEY_SECOND, vo.getSecond());
		values.put(KEY_ALARM_CONTENTS, vo.getAlarmContents());
		Calendar c = Calendar.getInstance();
		values.put(KEY_UPDATE_DATE, c.getTimeInMillis());
		int result = db.update(TABLE_TIMER, values, KEY_ID + "=?", new String[]{Long.toString(vo.getId())});
		closeDB();
		return result;
	}

	public boolean deleteTimer(long id) {
		// TODO: 2015-08-30 알림 반복인데 오늘만 삭제 때 어떻게 할지 구현 해야 함
		SQLiteDatabase db = this.getWritableDatabase();

		db.beginTransaction();
		boolean result = true;
		try {
			db.delete(TABLE_TIMER, KEY_ID + "=?", new String[]{String.valueOf(id)});
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

	public void closeDB() {
		SQLiteDatabase db = this.getReadableDatabase();
		if (db != null && db.isOpen())
			db.close();
	}

	public String serialize(Object content[]){
		return TextUtils.join(ARRAY_DIV, content);
	}

	public String[] derialize(String content){
		return content.split(ARRAY_DIV);
	}

	public String convertDateType(Calendar c){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");// cal.get(Calendar.YEAR)
		return sdf.format(c.getTime());//sdf.format(c);
	}

	public Calendar convertDateType(String s) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");// cal.get(Calendar.YEAR)
		Calendar cal = Calendar.getInstance();

		try {
			cal.setTime(sdf.parse(s));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return cal;
	}

}
