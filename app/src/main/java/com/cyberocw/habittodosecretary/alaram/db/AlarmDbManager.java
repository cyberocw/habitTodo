package com.cyberocw.habittodosecretary.alaram.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmTimeVO;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.alaram.vo.HolidayVO;
import com.cyberocw.habittodosecretary.alaram.vo.TimerVO;
import com.cyberocw.habittodosecretary.db.CommonRelationDBManager;
import com.cyberocw.habittodosecretary.db.DbHelper;
import com.cyberocw.habittodosecretary.settings.db.SettingDbManager;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.fabric.sdk.android.Fabric;

/**
 * Created by cyberocw on 2015-12-06.
 */
public class AlarmDbManager extends DbHelper{
	private static AlarmDbManager sInstance;
	private static Context mCtx;

	public AlarmDbManager(Context ctx) {
		super(ctx);
	}
	public static synchronized AlarmDbManager getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		mCtx = context;
		Fabric.with(context, new Crashlytics());
		if (sInstance == null) {
			sInstance = new AlarmDbManager(context);
		}
		return sInstance;
	}

	public boolean insertAlarm(AlarmVO vo){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ALARM_REMINDER_TYPE, vo.getAlarmReminderType());
		values.put(KEY_ALARM_TITLE, vo.getAlarmTitle());
		values.put(KEY_ALARM_TYPE, vo.getAlarmType());
		values.put(KEY_ALARM_DATE_TYPE, vo.getAlarmDateType());
		values.put(KEY_ALARM_OPTION, vo.getAlarmOption());
		values.put(KEY_HOUR, vo.getHour());
		values.put(KEY_MINUTE, vo.getMinute());
		values.put(KEY_HOLIDAY_ALL, vo.getIsHolidayALL());
		values.put(KEY_HOLIDAY_NONE, vo.getIsHolidayNone());
		values.put(KEY_TYPE, vo.getEtcType());
		values.put(KEY_ALARM_CALL_TYPE, vo.getAlarmCallType());

		Calendar c = vo.getCreateDt();

		if(c == null)
			c = Calendar.getInstance();
		values.put(KEY_CREATE_DATE, c.getTimeInMillis());

		c = Calendar.getInstance();

		values.put(KEY_UPDATE_DATE, c.getTimeInMillis());

		values.put(KEY_USE_YN, 1);

		values.put(KEY_ALARM_CALL_LIST, serialize(vo.getAlarmCallList().toArray()));
		//values.put(KEY_ALARM_CONTENTS, vo.getA());

		long id = db.insert(TABLE_ALARM, null, values);

		if(id == -1){
			Log.e(this.toString(), "DB Alarm INSERT ERROR");
			throw new Error("DB Alarm INSERT ERROR");
		}
		vo.setId(id);

		ArrayList<Calendar> dateList = vo.getAlarmDateList();

		//매달 반복
		if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.REPEAT_MONTH && dateList != null && !dateList.isEmpty()){
			insertRepeatDay(id, dateList.get(0));
			dateList = null;
		}

		//날짜지정 알람
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

	public boolean modifyUse(AlarmVO vo){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_USE_YN, vo.getUseYn());

		int affRow = db.update(TABLE_ALARM, values, KEY_ID + "=" + vo.getId(), null);

		closeDB();

		if(affRow < 1){
			Log.e(this.toString(), "DB Alarm USE UPDATE ERROR");
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
		Log.d(this.toString(), "insertAlarmCallOrder vo=" + vo.getAlarmTitle());
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
			cal.set(Calendar.MILLISECOND, 0);

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
				"SELECT B." + KEY_ID + ", A." + KEY_TIME_STAMP + ", A." + KEY_CALL_TIME + ", B." + KEY_ALARM_TITLE + ", A." + KEY_F_ALARM_ID +
						", B." + KEY_ALARM_TYPE + ", B." + KEY_ALARM_OPTION + ", B." + KEY_TYPE + ", B." + KEY_ALARM_CALL_TYPE + ", B." + KEY_ALARM_REMINDER_TYPE +
						", B." + KEY_ALARM_DATE_TYPE +
						" FROM " + TABLE_ALARM_ORDER +" AS A INNER JOIN " + TABLE_ALARM + " AS B ON " +
						" A." + KEY_F_ALARM_ID + " = B." + KEY_ID + " WHERE  B." + KEY_USE_YN + " = 1 AND A." + KEY_TIME_STAMP + " = " +
						" (SELECT MIN(" + KEY_TIME_STAMP + " ) FROM " + TABLE_ALARM_ORDER + " AS C INNER JOIN " + TABLE_ALARM +
						" AS D ON C." + KEY_F_ALARM_ID + " = D." + KEY_ID + " WHERE D." + KEY_USE_YN + " = 1 AND " + KEY_TIME_STAMP + " > " + nowTime + ")";

		Log.e(this.toString(), selectQuery);
		Cursor c = null;
		ArrayList<AlarmTimeVO> alarmTimeVOList = new ArrayList<AlarmTimeVO>();
		try {
			SQLiteDatabase db = this.getReadableDatabase();
			c = db.rawQuery(selectQuery, null);

			AlarmTimeVO vo;

			Crashlytics.log(Log.DEBUG, this.toString(), " min set time record count=" + c.getCount());

			if (c.moveToFirst()) {
				do {
					vo = new AlarmTimeVO();
					vo.setId(c.getLong((c.getColumnIndex(KEY_ID))));
					vo.setAlarmTitle((c.getString(c.getColumnIndex(KEY_ALARM_TITLE))));
					vo.setTimeStamp(c.getLong(c.getColumnIndex(KEY_TIME_STAMP)));
					vo.setCallTime(c.getInt(c.getColumnIndex(KEY_CALL_TIME)));
					vo.setfId(c.getLong(c.getColumnIndex(KEY_F_ALARM_ID)));
					vo.setAlarmType(c.getInt(c.getColumnIndex(KEY_ALARM_TYPE)));
					vo.setAlarmOption(c.getInt(c.getColumnIndex(KEY_ALARM_OPTION)));
					vo.setAlarmDateType(c.getInt(c.getColumnIndex(KEY_ALARM_DATE_TYPE)));
					vo.setEtcType(c.getString(c.getColumnIndex(KEY_TYPE)));
					vo.setAlarmCallType(c.getInt(c.getColumnIndex(KEY_ALARM_CALL_TYPE)));
					vo.setAlarmReminderType(c.getInt(c.getColumnIndex(KEY_ALARM_REMINDER_TYPE)));
					Log.d(this.toString(), "volist add =" + vo.getAlarmTitle());
					alarmTimeVOList.add(vo);
				} while (c.moveToNext());
			}
		} finally {
			if (c != null)
				c.close();
		}

		closeDB();
		return alarmTimeVOList;
	}

	protected void insertDate(long id, ArrayList<Calendar> dateList){
		for (int i = 0; i < dateList.size(); i++) {
			insertDate(id, dateList.get(i));
		}
	}

	private void insertDate(long id, Calendar cal) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ALARM_DATE, CommonUtils.convertDateType(cal));
		values.put(KEY_F_ALARM_ID, id);

		long _id = db.insert(TABLE_ALARM_DATE, null, values);

		if(_id == -1){
			Log.e(this.toString(), "DB Date INSERT ERROR");
			throw new Error("DB Date INSERT ERROR");
		}
	}

	private void insertRepeatDay(long id, Calendar repeatDay){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_REPEAT_DAY, repeatDay.get(Calendar.DAY_OF_MONTH));
		values.put(KEY_F_ALARM_ID, id);

		long _id = db.insert(TABLE_ALARM_REPEAT, null, values);

		if(_id == -1){
			Log.e(this.toString(), "DB Date INSERT ERROR");
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
			Log.e(this.toString(), "DB Date INSERT ERROR");
			throw new Error("DB Date INSERT ERROR");
		}
	}

	//반복 알람 가장 가까운 시간 가져오기
	// day num = sun 1 mon 2 ...
	public ArrayList<AlarmTimeVO> getMinRepeatAlarm(int dayNum) {
		return getMinRepeatAlarm(dayNum, false);
	}
	public ArrayList<AlarmTimeVO> getMinRepeatAlarm(int dayNum, boolean isReminderMode) {
		if(dayNum == -1) {
			Calendar cal = Calendar.getInstance();
			dayNum = cal.get(Calendar.DAY_OF_WEEK); //sun 1 mon 2 ...
		}
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
			//현재 요일 (day) 이후 일 경우 이후 요일과 오늘 요일 차이 값을 map value로 지정 - 사용 안함 나중에 봐서 daymap 로직 삭제하기
			//arrDayResult는 사용
			//arrDayResult에 오늘 요일부터 순서대로 요일
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
		Calendar cal = Calendar.getInstance(), nowCal = Calendar.getInstance(), cal2;
		nowCal.set(Calendar.SECOND, 0);
		nowCal.set(Calendar.MILLISECOND, 0);

		long nowTimeInMil = nowCal.getTimeInMillis();
		// get holiday list for week

		String startDate = CommonUtils.convertDateType(nowCal);
		cal.add(Calendar.DAY_OF_MONTH, 7);
		String endDate = CommonUtils.convertDateType(cal);

		SettingDbManager settingDb = SettingDbManager.getInstance(mCtx);
		HashMap<String, ArrayList> holidayMap = settingDb.getHolidayMap(startDate, endDate);

		int dayofWeek, day2;
		Cursor c = null;
		if(mCtx == null){
			mCtx = mContext;
		}
		SharedPreferences prefsReminder = mCtx.getSharedPreferences(Const.REMINDER.PREFS_ID, Context.MODE_PRIVATE);

		//오늘 기준 7번 반복하면서 최소값 찾으면 +1 일 더 찾아보고 중지
		for(int i = 0; i < arrDayResult.length; i++) {
			queryString = "SELECT B." + KEY_ID + ", B." + KEY_ALARM_CALL_LIST + ", B." + KEY_ALARM_REMINDER_TYPE + ", B." + KEY_ALARM_TITLE + ", B."+KEY_HOUR + ", B." + KEY_MINUTE + ", A." + KEY_F_ALARM_ID +
					", B." + KEY_ALARM_OPTION + ", B." + KEY_ALARM_TYPE + ", B." + KEY_HOLIDAY_ALL + ", B." + KEY_HOLIDAY_NONE + ", B." + KEY_TYPE + ", B." + KEY_ALARM_CALL_TYPE +
					" FROM " + TABLE_ALARM_REPEAT + " A INNER JOIN " + TABLE_ALARM + " B ON A." + KEY_F_ALARM_ID + " = B." + KEY_ID  +
					" WHERE B." + KEY_USE_YN + " = 1 AND (A." + arrDayResult[i] + " = 1 ";

			cal = (Calendar) nowCal.clone();
			cal.add(Calendar.DAY_OF_MONTH, i);
			dayofWeek = cal.get(Calendar.DAY_OF_WEEK);
			day2 = cal.get(Calendar.DAY_OF_MONTH);

			//매달 반복 검사
			queryString += " or A." + KEY_REPEAT_DAY + " = " + day2;

			//평일일 경우에만 holiday 여부 체크
			if(dayofWeek != 1 && dayofWeek != 7){
				String strCal = CommonUtils.convertDateType(cal);
				if (holidayMap.containsKey(strCal)) {
					ArrayList<HolidayVO> arrHoliday = holidayMap.get(strCal);
					for (int m = 0; m < arrHoliday.size(); m++) {
						HolidayVO hVO = arrHoliday.get(m);
						if (hVO.getType().equals("h") || hVO.getType().equals("i")) {
							queryString += " or B." + KEY_HOLIDAY_ALL + " = 1 ) and (B." + KEY_HOLIDAY_NONE + " <> 1";
							break;
						}
					}
				}
			}

			queryString += ") ORDER BY B." + KEY_HOUR + ", B." + KEY_MINUTE;
			c = db.rawQuery(queryString, null);
			if (c.moveToFirst()) {
				do {
					hour =  c.getInt(c.getColumnIndex(KEY_HOUR));
					minute = c.getInt(c.getColumnIndex(KEY_MINUTE));
					alarmCallList = derialize(c.getString(c.getColumnIndex(KEY_ALARM_CALL_LIST)));

					//오늘 날짜 기준으로 초기화
					cal = (Calendar) nowCal.clone();
					cal.set(Calendar.HOUR_OF_DAY, hour);
					cal.set(Calendar.MINUTE, minute);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);

					//지금 시간 이후가 아니면 통과 ( 몇 분 후 로직이 안먹혀서 삭제 )
					//if( i == 0 && nowTimeInMil >= cal.getTimeInMillis())
					//	continue;

					//오늘 기준 하루씩 추가
					if (i > 0)
						cal.add(Calendar.DAY_OF_MONTH, i);


					Log.d(this.toString(), "reminder type="+c.getInt(c.getColumnIndex(KEY_ALARM_REMINDER_TYPE)) +  " title="+c.getString(c.getColumnIndex(KEY_ALARM_TITLE)));
					if(c.getInt(c.getColumnIndex(KEY_ALARM_REMINDER_TYPE)) == Const.ALARM_REMINDER_MODE.REMINDER){
						if(prefsReminder != null && prefsReminder.getBoolean(CommonUtils.getReminderDayId(cal, dayofWeek, c.getInt(c.getColumnIndex(KEY_ID))), false)) {
							continue;
						}
					}
					if(isReminderMode && c.getInt(c.getColumnIndex(KEY_ALARM_REMINDER_TYPE)) != Const.ALARM_REMINDER_MODE.REMINDER)
						continue;


					// 몇분전 값이 실제 시간이기 때문에 이부분에서 최소 값을 찾음
					int val;
					for(int j = 0 ; j < alarmCallList.length; j++) {
						cal2 = (Calendar) cal.clone();
						cal2.add(Calendar.MINUTE, Integer.valueOf(alarmCallList[j]));
						timeinMil = cal2.getTimeInMillis();

						//리마인더 계산시 0분이 현재보다 이후면 해당 알람 그룹 건너 뜀
						//1.현재보다 0분이 이후에 있으면 모두 통과
						//2.중간 혹은 마지막 알람이 현재 보다 이후에 있으면 진행중 - 0분은 현재보다 이전이면서
						if(isReminderMode){
							 if(Integer.valueOf(alarmCallList[j]) == 0) {
								 //통과
								 if (nowTimeInMil < timeinMil) {
									 break;
								 }
								 else
								 	continue;
							 }
							 //중간 혹은 막탐이 현재 이후인것 찾기
							 else{
								 if (nowTimeInMil < timeinMil) {
									 alarmTimeVO = new AlarmTimeVO();
									 alarmTimeVO.setAlarmReminderType(c.getInt(c.getColumnIndex(KEY_ALARM_REMINDER_TYPE)));
									 alarmTimeVO.setCallTime(Integer.valueOf(alarmCallList[j]));
									 alarmTimeVO.setTimeStamp(timeinMil);
									 alarmTimeVO.setAlarmTitle(c.getString(c.getColumnIndex(KEY_ALARM_TITLE)));
									 alarmTimeVO.setId(c.getInt(c.getColumnIndex(KEY_ID)));
									 alarmTimeVO.setfId(c.getInt(c.getColumnIndex(KEY_F_ALARM_ID)));
									 alarmTimeVO.setAlarmOption(c.getInt(c.getColumnIndex(KEY_ALARM_OPTION)));
									 alarmTimeVO.setAlarmType(c.getInt(c.getColumnIndex(KEY_ALARM_TYPE)));
									 alarmTimeVO.setEtcType(c.getString(c.getColumnIndex(KEY_TYPE)));
									 alarmTimeVO.setAlarmCallType(c.getInt(c.getColumnIndex(KEY_ALARM_CALL_TYPE)));
									 alarmTimeVO.setRepeatDayId(dayofWeek);
									 Log.d(this.toString(), "added vo = " + alarmTimeVO.getAlarmTitle() + " reminderType="+alarmTimeVO.getAlarmReminderType());
									 arrList.add(alarmTimeVO);
									 break;
								 }

							 }
						}
						else {
							// - 몇분전/후 계산 값이 현재 시간보다 빠르면 건너 뜀
							if (nowTimeInMil >= timeinMil)
								continue;

							// min 값을 arr에 추가 (동일 시간) , min보다 더 작을 경우 arr 초기화 후 추가
							if (min >= timeinMil || min == 0) {
								if (min > timeinMil) {
									arrList.clear();
								}
								min = timeinMil;

								alarmTimeVO = new AlarmTimeVO();
								alarmTimeVO.setAlarmReminderType(c.getInt(c.getColumnIndex(KEY_ALARM_REMINDER_TYPE)));
								alarmTimeVO.setCallTime(Integer.valueOf(alarmCallList[j]));
								alarmTimeVO.setTimeStamp(timeinMil);
								alarmTimeVO.setAlarmTitle(c.getString(c.getColumnIndex(KEY_ALARM_TITLE)));
								alarmTimeVO.setId(c.getInt(c.getColumnIndex(KEY_ID)));
								alarmTimeVO.setfId(c.getInt(c.getColumnIndex(KEY_F_ALARM_ID)));
								alarmTimeVO.setAlarmOption(c.getInt(c.getColumnIndex(KEY_ALARM_OPTION)));
								alarmTimeVO.setAlarmType(c.getInt(c.getColumnIndex(KEY_ALARM_TYPE)));
								alarmTimeVO.setEtcType(c.getString(c.getColumnIndex(KEY_TYPE)));
								alarmTimeVO.setAlarmCallType(c.getInt(c.getColumnIndex(KEY_ALARM_CALL_TYPE)));
								alarmTimeVO.setRepeatDayId(dayofWeek);
								Log.d(this.toString(), "added vo22 = " + alarmTimeVO.getAlarmTitle());
								arrList.add(alarmTimeVO);
							}
						}
					}//end calltime for
				} while (c.moveToNext());
			}//movetofirst if end

			if(isReminderMode && i >= 2){
				break;
			}

			if(searchDayIndex >= 1)
				break;

			// 한번 찾은 뒤 다음 날 더 찾아보고 중지
			if(searchDayIndex == 0 && arrList.size() > 0)
				searchDayIndex = 1;
		} // end for
		if(c != null && !c.isClosed())
			c.close();

		closeDB();

		return arrList;
	}
	public boolean deleteAlarm(long id) {
		SQLiteDatabase db = this.getWritableDatabase();
		boolean result = true;

		try {
			db.beginTransaction();
			CommonRelationDBManager.getInstance(mCtx).deleteByAlarmId(id, db);
			deleteAlarm(id, db);
			db.setTransactionSuccessful();
		}catch(Exception e){
			e.printStackTrace();
			Log.e(Const.ERROR_TAG, "delete alarm error" + e.getMessage());
			result = false;
		}
		finally{
			db.endTransaction();
			closeDB();
		}
		return result;
	}

	public SQLiteDatabase deleteAlarm(long id, SQLiteDatabase db) {
		db.delete(TABLE_ALARM, KEY_ID + "=?", new String[]{String.valueOf(id)});
		db.delete(TABLE_ALARM_REPEAT, KEY_F_ALARM_ID + "=?", new String[]{String.valueOf(id)});
		db.delete(TABLE_ALARM_DATE, KEY_F_ALARM_ID + "=?", new String[]{String.valueOf(id)});
		db.delete(TABLE_ALARM_ORDER, KEY_F_ALARM_ID + "=?", new String[]{String.valueOf(id)});
		//db.delete(TABLE_FILE_INFO, KEY_F_ALARM_ID + "=? AND " + KEY_TYPE + "=?" , new String[]{String.valueOf(id), Const.ETC_TYPE.ALARM});
		return db;
	}

	/**
	 * 하루치 일정 모두 가져옴 - 날짜지정 + 요일 반복 알림
	 * */
	public ArrayList<AlarmVO> getAlarmList(Calendar date){
		int[] day = {date.get(Calendar.DAY_OF_WEEK)};
		Crashlytics.log(Log.DEBUG, this.toString(), "getAlarmList only startDate");
		return getAlarmList(-1, date, null, day);
	}
	public ArrayList<AlarmVO> getAlarmList(Calendar startDate, Calendar endDate){
		//1주일치 불러와서 아래 o 아이콘 삽입을 위한 용도
		Crashlytics.log(Log.DEBUG, this.toString(), "getAlarmList start, end date is not null");
		return getAlarmList(-1, startDate, endDate, null);
	}
	public ArrayList<AlarmVO> getAlarmRepeatList(){
		int[] day = {1,2,3,4,5,6,7};
		return getAlarmList(-1, null, null, day);
	}
	public AlarmVO getAlarmById(long id){
		Crashlytics.log(Log.DEBUG, this.toString(), "getAlarmList getAlarmById");
		ArrayList<AlarmVO> arrayList = getAlarmList(id, null, null, null);
		if(arrayList.size() == 0){
			return null;
		}
		return arrayList.get(0);

	}

	/*
	나중에 한번 싹 정리해야 할 필요가 있음
	 */
	private ArrayList<AlarmVO> getAlarmList(long id, Calendar startDate, Calendar endDate, int[] dayName) {
		String selectQuery =
				"SELECT  A.*, B." + KEY_ID + " as " + KEY_REPEAT_ID + ", C." + KEY_ID + " as " + KEY_DATE_ID + ", sun, mon, tue, wed, thu, fri, sat, C." +
						KEY_ALARM_DATE +", B." + KEY_REPEAT_DAY + ", D." + KEY_F_ID + ", A." + KEY_TYPE + " FROM " + TABLE_ALARM + " AS A LEFT JOIN " +
						TABLE_ALARM_REPEAT + " AS B ON A." + KEY_ID + " = B."+ KEY_F_ALARM_ID + " LEFT JOIN " +
						TABLE_ALARM_DATE + " AS C ON A." + KEY_ID + " = C." + KEY_F_ALARM_ID + " LEFT JOIN " +
						TABLE_ALARAM_RELATION + " AS D ON A." + KEY_ID + " = D." + KEY_F_ALARM_ID +

						" WHERE A." + KEY_ID + " IN ";
		if(id == -1) {
			ContentValues values = new ContentValues();

			if(dayName != null) {
				//아직까지는 dayName이 배열일 경우는 없음...
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
				//repeat 테이블 내용 불러옴
				if (values.size() > 0) {

					SettingDbManager settingDb = SettingDbManager.getInstance(mCtx);
					HashMap<String, ArrayList> holidayMap = new HashMap<>();
					//하루 휴일 정보 가져옴
					if(startDate != null && endDate == null){
						holidayMap = settingDb.getHolidayMap(CommonUtils.convertDateType(startDate), CommonUtils.convertDateType(startDate));
					}
					//날짜 범위 휴일 정보
					else if(startDate != null && endDate != null){
						holidayMap = settingDb.getHolidayMap(CommonUtils.convertDateType(startDate), CommonUtils.convertDateType(endDate));
					}
					boolean isHoliday = false;
					if(startDate != null) {
						int dayofWeek = startDate.get(Calendar.DAY_OF_WEEK);

						//평일일 경우에만 holiday 여부 체크
						//토일 제외
						if (dayofWeek != 1 && dayofWeek != 7) {
							String strCal = CommonUtils.convertDateType(startDate); //String.valueOf(startDate.get(Calendar.YEAR)) + CommonUtils.numberDigit(2, startDate.get(Calendar.MONTH) + 1) + CommonUtils.numberDigit(2, cal.get(Calendar.DAY_OF_MONTH));
							Crashlytics.log(Log.DEBUG, this.toString(), "strCal = " + strCal);
							//현재는 startDate 하루만 계산함....
							if (holidayMap.containsKey(strCal)) {
								ArrayList<HolidayVO> arrHoliday = holidayMap.get(strCal);
								for (int m = 0; m < arrHoliday.size(); m++) {
									HolidayVO hVO = arrHoliday.get(m);
									Crashlytics.log(Log.DEBUG, this.toString(), "hVO type = " + hVO.getType());
									if (hVO.getType().equals("h") || hVO.getType().equals("i")) {
										isHoliday = true;
										break;
									}
								}
							}
						}
					}
					//기본 주간 반복 select - in 다음 조건 붙이는 것임
					selectQuery += "(SELECT " + KEY_F_ALARM_ID + " FROM " + TABLE_ALARM_REPEAT + " WHERE ( ";
					int tempIndex = 0;
					for (String key : values.keySet()) {
						//and 인지 or인지 잘 구분 필요, 아직까지는 dayName이 배열일 경우는 없어서 and던 or던 상관 없음...(목금을 동시에 가져올 일이 업다는 뜻)
						//일단은 달력에서 월~금 다 가져오는 경우가 있어서 OR 처리

						selectQuery +=  (tempIndex > 0 ? " OR " : " ") +  key + " = 1";
						tempIndex++;
					}
					if(isHoliday){
						selectQuery += " AND " + KEY_HOLIDAY_NONE + " <> 1)";
						selectQuery += " OR (" + KEY_HOLIDAY_ALL + " = 1  )";
					}
					else{
						selectQuery += ")";
					}
					selectQuery += ")";
				}
				//endDate가 null일때 -- 한개 날짜만 사용
				if (startDate != null && endDate == null) {
					selectQuery += " OR A." + KEY_ID + " IN (SELECT " + KEY_F_ALARM_ID + " FROM " + TABLE_ALARM_DATE + " WHERE " +
							KEY_ALARM_DATE + " = " + CommonUtils.convertDateType(startDate) + ")";
				}
				if (startDate != null) {
					//매달 반복 가져옴
					selectQuery += " OR A." + KEY_ID + " IN (SELECT " + KEY_F_ALARM_ID + " FROM " + TABLE_ALARM_REPEAT + " WHERE " + KEY_REPEAT_DAY +
							" = " + startDate.get(Calendar.DAY_OF_MONTH) + " ) ";
				}
			}
			//범위
			//////// 날짜 지정 알림에서 불러옴 - 주간 달력에 settime 유형 o 표시 위함 -> 주간 날짜 지정 알림만 가져옴
			else if (startDate != null && endDate != null) {
				selectQuery += " (SELECT " + KEY_F_ALARM_ID + " FROM " + TABLE_ALARM_DATE + " WHERE " +
						KEY_ALARM_DATE + " BETWEEN " + CommonUtils.convertDateType(startDate) + " AND " + CommonUtils.convertDateType(endDate) + ")";
				//매달 반복 가져옴
				selectQuery += " OR A." + KEY_ID + " IN (SELECT " + KEY_F_ALARM_ID + " FROM " + TABLE_ALARM_REPEAT + " WHERE " + KEY_REPEAT_DAY +
						" >= " + startDate.get(Calendar.DAY_OF_MONTH) + " AND " + KEY_REPEAT_DAY + " <= " + endDate.get(Calendar.DAY_OF_MONTH) + ") ";
			}
			else{
				Toast.makeText(mCtx, "getAlarmList 인자가 잘못 전달됐습니다.", Toast.LENGTH_SHORT).show();
				selectQuery += " (-1) ";
			}

			//매달 반복의 경우, 오늘 날짜의 연월을 합쳐서 가져오기 (날짜 지정알림인 것처럼)
		}
		else{
			selectQuery += "("+id+")";
		}

		selectQuery += " ORDER BY A." + KEY_HOUR + " ASC, A." + KEY_MINUTE + " ASC";

		Log.e(this.toString(), selectQuery);

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);
		String tempData;

		String[] arrDateColumn = {KEY_MON, KEY_TUE, KEY_WED, KEY_THU, KEY_FRI, KEY_SAT, KEY_SUN}, arrTempString;

		ArrayList<Calendar> arrCal;
		ArrayList<Integer> arrRepeatDay;
		ArrayList<Integer> alarmCallList = new ArrayList<Integer>();
		String alarmDate;
		// 2015-08-30 date가 여럿일 경우는 고려하지 않았음, 일단 여러개일 경우를 대비하기 위해 테이블은 분리해 둠

		ArrayList<AlarmVO> alarmVOList = new ArrayList<AlarmVO>();
		HashMap<Long, AlarmVO> voMap = new HashMap<Long, AlarmVO>();

		AlarmVO vo;
		Calendar cc = Calendar.getInstance();
		Calendar repeatDayCal;
		if(startDate != null)
			repeatDayCal = (Calendar) startDate.clone();
		else
			repeatDayCal = Calendar.getInstance();

		repeatDayCal.set(Calendar.HOUR, 0);
		repeatDayCal.set(Calendar.MINUTE, 0);
		repeatDayCal.set(Calendar.SECOND, 0);
		repeatDayCal.set(Calendar.MILLISECOND, 0);

		int repeatDay = 0;

		if (c.moveToFirst()) {
			do {
				vo = new AlarmVO();
				vo.setId(c.getLong((c.getColumnIndex(KEY_ID))));
				alarmDate = c.getString(c.getColumnIndex(KEY_ALARM_DATE));

				//기존 ID 데이터가 있으면 date만 add하고 건너뜀 - 일단 현재는 그럴 일 없음
				if (voMap.containsKey(vo.getId())) {
					if (alarmDate != null && !"".equals(alarmDate)) {
						voMap.get(vo.getId()).getAlarmDateList().add(CommonUtils.convertDateType(alarmDate));
					}
					continue;
				}else {
					//없으면 새로 생성
					arrCal = new ArrayList<Calendar>();
					repeatDay = c.getInt(c.getColumnIndex(KEY_REPEAT_DAY));
					if(repeatDay > 0){
						repeatDayCal.set(Calendar.DAY_OF_MONTH, repeatDay);
						Crashlytics.log(Log.DEBUG, this.toString(), "repeaday string = " + CommonUtils.convertDateType(repeatDayCal));
						arrCal.add((Calendar) repeatDayCal.clone());
					}
					else if (alarmDate != null && !"".equals(alarmDate))
						arrCal.add(CommonUtils.convertDateType(alarmDate));

					vo.setAlarmDateList(arrCal);
				}

				vo.setAlarmReminderType((c.getInt(c.getColumnIndex(KEY_ALARM_REMINDER_TYPE))));
				vo.setAlarmTitle((c.getString(c.getColumnIndex(KEY_ALARM_TITLE))));
				vo.setAlarmType(c.getInt(c.getColumnIndex(KEY_ALARM_TYPE)));
				vo.setHour(c.getInt(c.getColumnIndex(KEY_HOUR)));
				vo.setMinute(c.getInt(c.getColumnIndex(KEY_MINUTE)));
				vo.setIsHolidayALL((c.getInt(c.getColumnIndex(KEY_HOLIDAY_ALL))));
				vo.setIsHolidayNone((c.getInt(c.getColumnIndex(KEY_HOLIDAY_NONE))));
				vo.setEtcType(c.getString(c.getColumnIndex(KEY_TYPE)));
				vo.setAlarmCallType(c.getInt(c.getColumnIndex(KEY_ALARM_CALL_TYPE)));

				tempData = c.getString(c.getColumnIndex(KEY_ALARM_CALL_LIST));

				if (tempData != null && !"".equals(tempData)) {
					arrTempString = derialize(tempData);
					alarmCallList = new ArrayList<Integer>();

					for (String a : arrTempString) {
						alarmCallList.add(Integer.valueOf(a));
					}
					vo.setAlarmCallList(alarmCallList);
				}
				vo.setAlarmDateType((c.getInt(c.getColumnIndex(KEY_ALARM_DATE_TYPE))));
				vo.setAlarmOption((c.getInt(c.getColumnIndex(KEY_ALARM_OPTION))));

				arrRepeatDay = new ArrayList<Integer>();

				for (int i = 0; i < arrDateColumn.length; i++) {
					if (c.getInt(c.getColumnIndex(arrDateColumn[i])) == 1)
						arrRepeatDay.add(Const.DAY.ARR_CAL_DAY[i]);
				}

				vo.setRfid(c.getLong(c.getColumnIndex(KEY_F_ID)));

				vo.setRepeatDay(arrRepeatDay);
				vo.setUseYn(c.getInt(c.getColumnIndex(KEY_USE_YN)));

				cc.setTimeInMillis(c.getInt(c.getColumnIndex(KEY_CREATE_DATE)));
				vo.setCreateDt((Calendar) cc.clone());

				cc.setTimeInMillis(c.getInt(c.getColumnIndex(KEY_UPDATE_DATE)));
				vo.setUpdateDt((Calendar) cc.clone());

				alarmVOList.add(vo);
				voMap.put(vo.getId(), vo);
			} while (c.moveToNext());
		}
		if(c != null && !c.isClosed())
			c.close();

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
		values.put(KEY_ALARM_OPTION, vo.getAlarmSoundOption());


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

		if(id == -1){
			Log.e(this.toString(), "DB Alarm INSERT ERROR");
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
				vo.setAlarmSoundOption(c.getInt(c.getColumnIndex(KEY_ALARM_OPTION)));
				//vo.setCreateDt(c.getInt(c.getColumnIndex(KEY_CREATE_DATE)));
				//vo.setUpdateDt(c.getInt(c.getColumnIndex(KEY_UPDATE_DATE)));

				timerList.add(vo);
			} while (c.moveToNext());
		}

		if(c != null)
			c.close();
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
		values.put(KEY_ALARM_OPTION, vo.getAlarmSoundOption());
		Calendar c = Calendar.getInstance();
		values.put(KEY_UPDATE_DATE, c.getTimeInMillis());
		int result = db.update(TABLE_TIMER, values, KEY_ID + "=?", new String[]{Long.toString(vo.getId())});
		closeDB();
		return result;
	}

	/*
		Holiday
		KEY_ID + " integer primary key autoincrement, " +
					KEY_YEAR + " integer , " +
					KEY_MONTH + " integer , " +
					KEY_DAY + " integer , " +
					KEY_TYPE + " text, " +
					KEY_NAME + " text )" +
	 */

	private void insertHoliday(long id, Calendar cal) {
		SQLiteDatabase db = this.getWritableDatabase();
		String year = "" , month = "", day = "", type = "", name = "";

		ContentValues values = new ContentValues();

		values.put(KEY_YEAR, year);
		values.put(KEY_MONTH, month);
		values.put(KEY_DAY, day);
		values.put(KEY_TYPE, type);
		values.put(KEY_NAME, name);

		long _id = db.insert(TABLE_HOLIDAY, null, values);

		if(_id == -1){
			Log.e(this.toString(), "DB Date INSERT ERROR");
			throw new Error("DB Date INSERT ERROR");
		}
	}

	public boolean deleteTimer(long id) {
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

	//호출 타임은 수정/삭제 + 재부팅
	public ArrayList<AlarmTimeVO> getReminderRunningList(){
		//setTime -> reminder중 현재시간 +23시간 59분 것 가져옴 지금부터 23시간59분 가능하니까 -> alarmVO 순회 하면서 startTime <현재 > endtime 시간으로 걸리는것 추려서 띄움
		//repeat -> 기존 repeat 로직을 통해서 calltime for문 돌면서 startTime < > end time 검사해서 띄우면 될듯
		Calendar cal = Calendar.getInstance();
		ArrayList<AlarmTimeVO> list = getMinReminderTime(cal);
		ArrayList<AlarmTimeVO> list2 =  getMinRepeatAlarm(-1, true);
		Log.d(this.toString(), "lisot2 size="+list2.size());
		list.addAll(list2);
		return list;
	}

	//시간 순서에 따라 가장 가까운 시간 알림 가져옴
	public ArrayList<AlarmTimeVO> getMinReminderTime(Calendar cal){
		Calendar calEnd = (Calendar) cal.clone();
		calEnd.add(Calendar.MINUTE, 59);

		String selectQuery =
				"SELECT B.*, A." + KEY_TIME_STAMP + ", A." + KEY_CALL_TIME + ", B." + KEY_ALARM_TITLE + ", A." + KEY_F_ALARM_ID +
						", B." + KEY_ALARM_TYPE + ", B." + KEY_ALARM_OPTION + ", B." + KEY_TYPE + ", B." + KEY_ALARM_CALL_TYPE + ", B." + KEY_ALARM_REMINDER_TYPE +
						" FROM " + TABLE_ALARM_ORDER +" AS A INNER JOIN " + TABLE_ALARM + " AS B ON " +
						" A." + KEY_F_ALARM_ID + " = B." + KEY_ID + " WHERE B." + KEY_ALARM_REMINDER_TYPE + " = "  + Const.ALARM_REMINDER_MODE.REMINDER +
						//" AND B." + KEY_CALL_TIME + " > 0 " +
						" AND B." + KEY_USE_YN + " = 1 AND A." + KEY_TIME_STAMP + " >= " + cal.getTimeInMillis() +
						" AND A." + KEY_TIME_STAMP + " <= " + calEnd.getTimeInMillis() ;
		Cursor c = null;

		ArrayList<AlarmTimeVO> alarmTimeVOList = new ArrayList<AlarmTimeVO>();
		HashSet<Long> setAlarmId = new HashSet<Long>();
		try {
			SQLiteDatabase db = this.getReadableDatabase();
			c = db.rawQuery(selectQuery, null);

			AlarmTimeVO vo;

			Crashlytics.log(Log.DEBUG, this.toString(), " min set time record count=" + c.getCount());

			if (c.moveToFirst()) {
				do {
					long alarmId = (c.getLong(c.getColumnIndex(KEY_F_ALARM_ID)));
					if(setAlarmId.contains(alarmId))
						continue;
					if(c.getInt(c.getColumnIndex(KEY_CALL_TIME)) == 0){
						setAlarmId.add(alarmId);
						continue;
					}
					vo = new AlarmTimeVO();
					vo.setId(c.getLong((c.getColumnIndex(KEY_ID))));
					vo.setAlarmTitle((c.getString(c.getColumnIndex(KEY_ALARM_TITLE))));
					vo.setTimeStamp(c.getLong(c.getColumnIndex(KEY_TIME_STAMP)));
					vo.setCallTime(c.getInt(c.getColumnIndex(KEY_CALL_TIME)));
					vo.setfId(c.getLong(c.getColumnIndex(KEY_F_ALARM_ID)));
					vo.setAlarmType(c.getInt(c.getColumnIndex(KEY_ALARM_TYPE)));
					vo.setAlarmOption(c.getInt(c.getColumnIndex(KEY_ALARM_OPTION)));
					vo.setEtcType(c.getString(c.getColumnIndex(KEY_TYPE)));
					vo.setAlarmCallType(c.getInt(c.getColumnIndex(KEY_ALARM_CALL_TYPE)));
					vo.setAlarmReminderType(c.getInt(c.getColumnIndex(KEY_ALARM_REMINDER_TYPE)));

					alarmTimeVOList.add(vo);
				} while (c.moveToNext());
			}
		} finally {
			if (c != null)
				c.close();
		}
		int len = alarmTimeVOList.size() ;

		for(int i = 0 ; i < len; i++){
			if(i >= alarmTimeVOList.size()){
				break;
			}
			if(setAlarmId.contains(alarmTimeVOList.get(i).getfId())){
				alarmTimeVOList.remove(i);
				i--;
				len = alarmTimeVOList.size();
			}
		}

		closeDB();
		return alarmTimeVOList;
	}
}
