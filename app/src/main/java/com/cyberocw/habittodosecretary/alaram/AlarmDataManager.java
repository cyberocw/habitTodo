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
import com.cyberocw.habittodosecretary.common.vo.RelationVO;
import com.cyberocw.habittodosecretary.db.CommonRelationDBManager;

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

	public AlarmDataManager(Context ctx) {
		this(ctx, Calendar.getInstance());
	}

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

	public AlarmVO getItemByIdInList(long id){
		for(int i = 0 ; i < dataList.size() ; i++){
			if(dataList.get(i).getId() == id){
				return dataList.get(i);
			}
		}
		return null;
	}

	public AlarmVO getItemByIdInDB(long id){
		return mDb.getAlarmById(id);
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
		return true;
	}

	public AlarmVO deleteItemById(int position){
		return dataList.remove(position);
	}

	public AlarmVO addItem(Calendar date, String title, ArrayList<Integer> repeatDay, String type){
		AlarmVO item = new AlarmVO(date, title, repeatDay);
		this.dataList.add(item);
		return item;
	}

	public boolean addItem(AlarmVO item){

		mDb.insertAlarm(item);

		//알람 인던트 등록
		if(item.getId() == -1){
			Log.e(Const.DEBUG_TAG, "오류 : 알림 ID가 생성되지 않았습니다");
			Toast.makeText(mCtx, "오류 : 알림 ID가 생성되지 않았습니다", Toast.LENGTH_LONG);
			return false;
		}

		this.dataList.add(item);

		return true;
	}

	/**
	 * alarm을 지웠다가 새로 insert 하는 메서드
	 * relation 또한 지우고 새로 insert 해줌
	 * @param item (AlaramVO)
	 * @return boolean
	 */
	public boolean modifyItem(AlarmVO item){
		//삭제 후 새로 insert, relation 또한 새로 insert 해줌 (일단 하나의 alarm은 하나의 relation만을 가진 상태의 로직)
		long oriId = item.getId();
		CommonRelationDBManager relationDBManager = CommonRelationDBManager.getInstance(mCtx);
		RelationVO rvo = relationDBManager.getByAlarmId(oriId);

		boolean delResult = this.deleteItemById(oriId);

		if(delResult == false)
			return false;

		item.setId(-1);

		addItem(item);

		rvo.setAlarmId(item.getId());
		relationDBManager.insert(rvo);

		//알람 인던트 등록
		if(item.getId() == -1){
			Log.e(Const.DEBUG_TAG, "오류 : 알림 ID가 생성되지 않았습니다");
			Toast.makeText(mCtx, "오류 : 알림 ID가 생성되지 않았습니다", Toast.LENGTH_LONG);
			return false;
		}

		this.dataList.add(item);

		return true;
	}

	public boolean modifyUseYn(AlarmVO item){
		return mDb.modifyUse(item);
	}

	public void resetMinAlarmCall(){
		resetMinAlarmCall(Const.ALARM_DATE_TYPE.REPEAT);
		resetMinAlarmCall(Const.ALARM_DATE_TYPE.SET_DATE);
	}

	public void resetMinAlarmCall(int type){
		ArrayList<AlarmTimeVO> alarmTimeList = null;
		String reqCode;

		if(type == Const.ALARM_DATE_TYPE.SET_DATE) {
			alarmTimeList = mDb.getMinAlarmTime();
			reqCode = Const.REQ_CODE;
		}
		else if(type == Const.ALARM_DATE_TYPE.REPEAT){
			Calendar cal = Calendar.getInstance();
			int dayNum = cal.get(Calendar.DAY_OF_WEEK); //sun 1 mon 2 ...
			alarmTimeList = mDb.getMinRepeatAlarm(dayNum);
			reqCode = Const.REQ_CODE_REPEAT;
		}
		else{
			Toast.makeText(mCtx, "알람 TYPE을 가져오지 못했습니다" + type, Toast.LENGTH_LONG).show();
			Log.e(Const.DEBUG_TAG, "resetMinalarmCall type is miss match =" + type);
			return;
		}

		SharedPreferences prefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);

		String text = prefs.getString(reqCode, null);

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
		String[] arrReq = new String[alarmTimeList.size()];

		for(int i = 0; i < alarmTimeList.size(); i++){
			arrReq[i] = String.valueOf(setAlarm(alarmTimeList.get(i), type));
		}

		String newReqCode = TextUtils.join("," , arrReq);

		//등록된 code 저장해둠
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(reqCode);
		editor.putString(reqCode, newReqCode);
	}

	public long setAlarm(AlarmTimeVO alarmVO, int type) {
		AlarmManager alarmDataManager = (AlarmManager) mCtx.getSystemService(Context.ALARM_SERVICE);

		Intent myIntent = new Intent(mCtx, AlarmReceiver.class);

		int callTime = alarmVO.getCallTime();
		Calendar ccc = Calendar.getInstance();
		long reqCode = ccc.getTimeInMillis();//alarmVO.getId() * 100 + callTime;

		//myIntent.removeExtra("title");

		String strDay = ccc.get(Calendar.HOUR_OF_DAY) + "시 " + ccc.get(Calendar.MINUTE) + "분 " + ccc.get(Calendar.SECOND) + " 초";

		long timeStamp = alarmVO.getTimeStamp();
		ccc.setTimeInMillis(timeStamp);
		ccc.add(Calendar.MINUTE, -10);
		myIntent.putExtra("alarmTimeVO", alarmVO);
		myIntent.putExtra("title", alarmVO.getAlarmTitle() + " " + (callTime < 0 ? callTime + "분 전" : (callTime > 0 ? callTime + "분 후" : "")));
		myIntent.putExtra("reqCode", reqCode);
		myIntent.putExtra("alarmDateType", type);
		myIntent.putExtra("realTime", timeStamp);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mCtx, (int) reqCode, myIntent, 0);

		setAlarmExact(alarmDataManager, AlarmManager.RTC_WAKEUP, alarmVO.getTimeStamp(), pendingIntent);

		return reqCode;
	}

	@SuppressLint("NewApi")
	private void setAlarmExact(AlarmManager am, int type, long time, PendingIntent it){
		final int sdkVersion = Build.VERSION.SDK_INT;
		/*
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			am.setExactAndAllowWhileIdle(type, time, it);
		}
		else*/ if(sdkVersion >= Build.VERSION_CODES.KITKAT) {
			Log.d(Const.DEBUG_TAG, "kitkat set alarmExact");
			am.setExact(type, time, it);
		}
		else {
			Log.d(Const.DEBUG_TAG, "low version set alarm");
			am.set(type, time, it);
		}
	}
}
