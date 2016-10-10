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
import com.cyberocw.habittodosecretary.alaram.service.AlarmBackgroudService;
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
		//resetMinAlarmCall(Const.ALARM_DATE_TYPE.REPEAT);
		//resetMinAlarmCall(Const.ALARM_DATE_TYPE.SET_DATE);
		resetMinAlarm();
	}

	public void resetMinAlarmCall(int type) {
		resetMinAlarm();
	}
	public void resetMinAlarm(){
		ArrayList<AlarmTimeVO> alarmTimeList1 = null;
		ArrayList<AlarmTimeVO> alarmTimeList2 = null;
		ArrayList<AlarmTimeVO> alarmTimeList = null;
		String reqCode = Const.REQ_CODE;

		alarmTimeList1 = mDb.getMinAlarmTime();

		Calendar cal = Calendar.getInstance();
		int dayNum = cal.get(Calendar.DAY_OF_WEEK); //sun 1 mon 2 ...
		alarmTimeList2 = mDb.getMinRepeatAlarm(dayNum);

		int minList = 0;
		long minTimeStamp = 0;
		// setTime 과 repeat 비교하여 더 가까운 시간 것을 지정
		if(alarmTimeList1 != null && alarmTimeList1.size() > 0){
			minList = 1;
			minTimeStamp = alarmTimeList1.get(0).getTimeStamp();
		}
		if(alarmTimeList2 != null && alarmTimeList2.size() > 0){
			if(minTimeStamp == 0 || minTimeStamp > alarmTimeList2.get(0).getTimeStamp())
				minList = 2;
		}
		if(minList == 1)
			alarmTimeList = alarmTimeList1;
		else if(minList == 2)
			alarmTimeList = alarmTimeList2;

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
				boolean alarmUp = (PendingIntent.getBroadcast(mCtx, Integer.valueOf(arrReq[i]), myIntent, PendingIntent.FLAG_NO_CREATE) != null);
				if (alarmUp) {
					PendingIntent sender1 = PendingIntent.getBroadcast(mCtx, Integer.valueOf(arrReq[i]), myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					alarmDataManager.cancel(sender1);
					sender1.cancel();
				}
			}

			//background 서비스 취소
			Intent intentAlarmbackground = new Intent(mCtx, AlarmBackgroudService.class);
			mCtx.stopService(intentAlarmbackground);
		}

		//새로 등록
		if(alarmTimeList == null)
			return ;

		String[] arrReq = new String[alarmTimeList.size()];

		for(int i = 0; i < alarmTimeList.size(); i++){
			arrReq[i] = String.valueOf(setAlarm(alarmTimeList.get(i), i));
		}

		String newReqCode = TextUtils.join("," , arrReq);

		//등록된 code 저장해둠
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(reqCode);
		editor.putString(reqCode, newReqCode);
	}

	public long setAlarm(AlarmTimeVO alarmVO, int index) {
		int callTime = alarmVO.getCallTime();
		Calendar ccc = Calendar.getInstance();
		Calendar nowCal = Calendar.getInstance();
		int reqCode = index;

		//myIntent.removeExtra("title");
		long timeStamp = alarmVO.getTimeStamp();
		ccc.setTimeInMillis(timeStamp);
		ccc.add(Calendar.MINUTE, -10);

		//10분 이내일 경우 바로 서비스 실행
		if(ccc.getTimeInMillis() < nowCal.getTimeInMillis()){
			Intent myIntent = new Intent(mCtx, AlarmBackgroudService.class);
			myIntent.putExtra("alarmTimeVO", alarmVO);
			mCtx.startService(myIntent);
			return reqCode;
		}
		//10분 이상일 경우 setTime 시킴
		AlarmManager alarmDataManager = (AlarmManager) mCtx.getSystemService(Context.ALARM_SERVICE);
		Intent myIntent = new Intent(mCtx, AlarmReceiver.class);

		myIntent.putExtra("alarmTimeVO", alarmVO);
		myIntent.putExtra("title", alarmVO.getAlarmTitle() + " " + (callTime < 0 ? callTime + "분 전" : (callTime > 0 ? callTime + "분 후" : "")));
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mCtx, reqCode, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
