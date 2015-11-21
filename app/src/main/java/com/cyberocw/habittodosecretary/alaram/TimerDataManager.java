package com.cyberocw.habittodosecretary.alaram;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.alaram.db.DbHelper;
import com.cyberocw.habittodosecretary.alaram.receiver.AlarmReceiver;
import com.cyberocw.habittodosecretary.alaram.vo.TimerVO;

import java.util.ArrayList;

/**
 * Created by cyberocw on 2015-10-18.
 */
public class TimerDataManager {
	AlarmManager mManager;
	Context mCtx = null;
	DbHelper mDb;
	ArrayList<TimerVO> dataList = null;

	public TimerDataManager(Context ctx) {
		mCtx = ctx;
		mDb = new DbHelper(ctx);
		this.dataList = mDb.getTimerList();
		Log.d(Const.DEBUG_TAG, "timerList length = " + this.dataList.size());
	}

	public ArrayList<TimerVO> getDataList() {
		return dataList;
	}

	public void makeDataList(){
		this.dataList = mDb.getTimerList();
	}

	public void setDataList(ArrayList<TimerVO> dataList) {
		this.dataList = dataList;
	}

	public int getCount(){
		return this.dataList.size();
	}

	public TimerVO getItem(int position){
		return this.dataList.get(position);
	}

	public TimerVO getItemById(long id){
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

	public boolean addItem(TimerVO item){
		mDb.insertTimer(item);

		//알람 인던트 등록
		if(item.getId() == -1){
			Log.d(Const.DEBUG_TAG, "오류 : 알림 ID가 생성되지 않았습니다");
			Toast.makeText(mCtx, "오류 : 알림 ID가 생성되지 않았습니다", Toast.LENGTH_LONG);
			return false;
		}

		this.dataList.add(item);

		return true;
	}

	public long setTimer(TimerVO alarmVO) {
		AlarmManager alarmManager = (AlarmManager) mCtx.getSystemService(Context.ALARM_SERVICE);

		Intent myIntent = new Intent(mCtx, AlarmReceiver.class);


		long reqCode = (Long) alarmVO.getId() * 100 ;


		//myIntent.putExtra("title", alarmVO.getAlarmTitle() + " " + (callTime < 0 ? callTime + "분 전" : (callTime > 0 ? callTime + "분 후" : "")));
		myIntent.putExtra("reqCode", reqCode);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(mCtx, (int) reqCode, myIntent, 0);

		//setAlarmExact(alarmDataManager, AlarmManager.RTC, alarmVO.getTimeStamp(), pendingIntent);

		return reqCode;
	}

	@SuppressLint("NewApi")
	private void setTimerExact(AlarmManager am, int type, long time, PendingIntent it){
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
