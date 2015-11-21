package com.cyberocw.habittodosecretary.alaram.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.alaram.service.NotificationService;
import com.cyberocw.habittodosecretary.util.TTSNoti;

import java.util.Calendar;

/**
 * Created by cyberocw on 2015-08-31.
 */
public class AlarmReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		String Noti_title = intent.getExtras().getString("title");
		String Noti_message = intent.getExtras().getString("notes");
		long reqCode = intent.getExtras().getLong("reqCode");
		int alarmDateType = intent.getExtras().getInt("alarmDateType");

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
}