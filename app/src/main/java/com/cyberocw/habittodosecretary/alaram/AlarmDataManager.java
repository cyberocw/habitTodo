package com.cyberocw.habittodosecretary.alaram;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.alaram.db.AlarmDbManager;
import com.cyberocw.habittodosecretary.alaram.receiver.AlarmReceiver;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmTimeVO;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by cyberocw on 2015-08-16.
 */
public class AlarmDataManager {
	AlarmManager mManager;
	Context mCtx = null;
	AlarmDbManager mDb;

	// 각각의 알람이 저장 됨
	private ArrayList<AlarmVO> dataList = new ArrayList<>();


	public AlarmDataManager(Context ctx, Calendar cal) {
		mCtx = ctx;
		mDb = AlarmDbManager.getInstance(ctx);
		mManager = (AlarmManager) mCtx.getSystemService(Context.ALARM_SERVICE);
		this.dataList = mDb.getAlarmList(cal);
	}

	public ArrayList<AlarmVO> getDataList() {
		return dataList;
	}

	public void makeDataList(Calendar cal){
		this.dataList = mDb.getAlarmList(cal);
	}

	public void setDataList(ArrayList<AlarmVO> dataList) {
		this.dataList = dataList;
	}

	public int getCount(){
		return this.dataList.size();
	}

	public AlarmVO getItem(int position){
		return this.dataList.get(position);
	}

	public AlarmVO getItemById(long id){
		for(int i = 0 ; i < dataList.size() ; i++){
			if(dataList.get(i).getId() == id){
				return dataList.get(i);
			}
		}
		return null;
	}

	public boolean deleteItemById(long id){
		boolean delResult = mDb.deleteAlarm(id);

		if(delResult == false)
			return false;

		for(int i = 0 ; i < dataList.size() ; i++){
			if(dataList.get(i).getId() == id){
				dataList.remove(i);
				return true;
			}
		}
		return false;
	}

	public AlarmVO deleteItemById(int position){
		return dataList.remove(position);
	}

	public AlarmVO addItem(Calendar date, String title, ArrayList<Integer> repeatDay, String type){

		Log.d(Const.DEBUG_TAG, "data added1");

		AlarmVO item = new AlarmVO(date, title, repeatDay);
		this.dataList.add(item);
		return item;
	}

	public boolean addItem(AlarmVO item){

		mDb.insertAlarm(item);

		//알람 인던트 등록
		if(item.getId() == -1){
			Log.d(Const.DEBUG_TAG, "오류 : 알림 ID가 생성되지 않았습니다");
			Toast.makeText(mCtx, "오류 : 알림 ID가 생성되지 않았습니다", Toast.LENGTH_LONG);
			return false;
		}

		this.dataList.add(item);

		return true;
	}

	public boolean modifyItem(AlarmVO item){
		//삭제 후 새로 insert

		boolean delResult = this.deleteItemById(item.getId());

		if(delResult == false)
			return false;

		item.setId(-1);

		addItem(item);

		//알람 인던트 등록
		if(item.getId() == -1){
			Log.d(Const.DEBUG_TAG, "오류 : 알림 ID가 생성되지 않았습니다");
			Toast.makeText(mCtx, "오류 : 알림 ID가 생성되지 않았습니다", Toast.LENGTH_LONG);
			return false;
		}

		this.dataList.add(item);

		return true;
	}

	public boolean modifyUseYn(AlarmVO item){
		return mDb.modifyUse(item);
	}

	public void resetMinAlarmCall(int type){
		Log.d(Const.DEBUG_TAG, "resetMinAlarmCall start type=" + type);
		ArrayList<AlarmTimeVO> alarmTimetList = null;
		String reqCode;
		reqCode = Const.REQ_CODE;

		if(type == Const.ALARM_DATE_TYPE.SET_DATE) {
			alarmTimetList = mDb.getMinAlarmTime();
		}
		else if(type == Const.ALARM_DATE_TYPE.REPEAT){
			Calendar cal = Calendar.getInstance();
			int dayNum = cal.get(Calendar.DAY_OF_WEEK); //sun 1 mon 2 ...
			alarmTimetList = mDb.getMinRepeatAlarm(dayNum);
		}
		else{
			Toast.makeText(mCtx, "알람 TYPE을 가져오지 못했습니다" + type, Toast.LENGTH_LONG).show();
			Log.e(Const.DEBUG_TAG, "resetMinalarmCall type is miss match =" + type);
			return;
		}

		SharedPreferences prefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, mCtx.MODE_PRIVATE);

		String text = prefs.getString(reqCode, null);

		Log.d(Const.DEBUG_TAG, "pref text = " + text);

		AlarmManager alarmDataManager = (AlarmManager) mCtx
				.getSystemService(Context.ALARM_SERVICE);
		Intent myIntent = new Intent(mCtx, AlarmReceiver.class);

		//기존것 취소
		if(text != null && !"".equals(text)){
			String[] arrReq = text.split(",");

			if(arrReq.length == 0)
				arrReq[0] = text;

			for(int i = 0 ; i < arrReq.length; i++){
				PendingIntent pendingIntent = PendingIntent.getBroadcast(mCtx, Integer.valueOf(arrReq[i]), myIntent, 0);
				alarmDataManager.cancel(pendingIntent);
			}
		}

		//새로 등록
		String[] arrReq = new String[alarmTimetList.size()];

		for(int i = 0; i < alarmTimetList.size(); i++){
			Log.d(Const.DEBUG_TAG, "alarmTimeList for call Time = " + String.valueOf(alarmTimetList.get(i).getCallTime()));
			arrReq[i] = String.valueOf(setAlarm(alarmTimetList.get(i), type));
		}

		String newReqCode = TextUtils.join("," , arrReq);

		Log.d(Const.DEBUG_TAG, "joind pref text = " + newReqCode);

		//등록된 code 저장해둠
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(reqCode, newReqCode);
	}

	public long setAlarm(AlarmTimeVO alarmVO, int type) {
		AlarmManager alarmDataManager = (AlarmManager) mCtx.getSystemService(Context.ALARM_SERVICE);

		Intent myIntent = new Intent(mCtx, AlarmReceiver.class);

		int callTime = alarmVO.getCallTime();

		long reqCode = (Long) alarmVO.getId() * 100 + callTime;

		Log.d(Const.DEBUG_TAG, "reqCode = " + reqCode + " calltime = " + callTime);
		//myIntent.removeExtra("title");
		Calendar ccc = Calendar.getInstance();
		ccc.setTimeInMillis(alarmVO.getTimeStamp());
		String strDay = ccc.get(Calendar.HOUR_OF_DAY) + "시 " + ccc.get(Calendar.MINUTE) + "분 " + ccc.get(Calendar.SECOND) + " 초";

		Toast.makeText(mCtx, "알람 : reqCode=" + reqCode+ " 시간: " + strDay + " " + alarmVO.getAlarmTitle() + "" + callTime + (callTime < 0 ? "분 전" : (callTime > 0 ? "분 후" : "")), Toast.LENGTH_SHORT).show();

		myIntent.putExtra("title", alarmVO.getAlarmTitle() + " " + (callTime < 0 ? callTime + "분 전" : (callTime > 0 ? callTime + "분 후" : "")));
		myIntent.putExtra("reqCode", reqCode);
		myIntent.putExtra("alarmDateType", type);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mCtx, (int) reqCode, myIntent, 0);

		setAlarmExact(alarmDataManager, AlarmManager.RTC, alarmVO.getTimeStamp(), pendingIntent);

		return reqCode;
	}

	@SuppressLint("NewApi")
	private void setAlarmExact(AlarmManager am, int type, long time, PendingIntent it){
		final int sdkVersion = Build.VERSION.SDK_INT;
		if(sdkVersion >= Build.VERSION_CODES.KITKAT) {
			Log.d(Const.DEBUG_TAG, "kitkat set alarmExact");
			am.setExact(type, time, it);
		}
		else {
			Log.d(Const.DEBUG_TAG, "low version set alarm");
			am.set(type, time, it);
		}
	}
}
