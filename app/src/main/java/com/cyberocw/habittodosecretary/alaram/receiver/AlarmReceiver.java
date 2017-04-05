package com.cyberocw.habittodosecretary.alaram.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cyberocw.habittodosecretary.alaram.service.AlarmBackgroudService;

/**
 * Created by cyberocw on 2015-08-31.
 */
//// TODO: 2016-10-03 데이터 전달할 객체 생성하여 전달하기 serializable 혹은 parceable
public class AlarmReceiver extends BroadcastReceiver{
	private AlarmBackgroudService mService;
	private boolean mBound;
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent myIntent = new Intent(context, AlarmBackgroudService.class);
		myIntent.putExtras(intent.getExtras());

		context.startService(myIntent);
		//context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		//AlarmDataManager mAlarmDataManager = new AlarmDataManager(context, Calendar.getInstance());
		//mAlarmDataManager.resetMinAlarmCall();
	}
	/*
	public void onReceiveOri(Context context, Intent intent) {
		String Noti_title = intent.getExtras().getString("title");
		String Noti_message = intent.getExtras().getString("notes");
		long reqCode = intent.getExtras().getLong("reqCode");
		int alarmDateType = intent.getExtras().getInt("alarmDateType");
		long realTime = intent.getExtras().getLong("realTime");

		Log.d("AlarmReciever", Noti_title + " " + Noti_message + " type= " + alarmDateType);
		Intent myIntent = new Intent(context, NotificationService.class);
		myIntent.putExtra("title", Noti_title);
		myIntent.putExtra("notes", Noti_message);
		myIntent.putExtra("reqCode", reqCode);
		context.startService(myIntent);

		AlarmDataManager mAlarmDataManager = new AlarmDataManager(context, Calendar.getInstance());
		mAlarmDataManager.resetMinAlarmCall(alarmDateType);

		Intent ttsIntent = new Intent(context, TTSNoti.class);
		ttsIntent.putExtra("alaramTitle", Noti_title);
		context.startService(ttsIntent);
	}
	*/
}