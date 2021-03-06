package com.cyberocw.habittodosecretary.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;

import java.util.Calendar;

/**
 * Created by cyberocw on 2015-10-04.
 */
public class BootReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			AlarmDataManager alarmDataManager = new AlarmDataManager(context, Calendar.getInstance());
			Log.d(this.toString(), "bootReceiver start");
			alarmDataManager.resetMinAlarmCall();
			alarmDataManager.resetReminderNoti();
		}
	}
}
