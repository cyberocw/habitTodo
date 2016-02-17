package com.cyberocw.habittodosecretary.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
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

			alarmDataManager.resetMinAlarmCall(Const.ALARM_DATE_TYPE.REPEAT);
			alarmDataManager.resetMinAlarmCall(Const.ALARM_DATE_TYPE.SET_DATE);
		}
	}
}
