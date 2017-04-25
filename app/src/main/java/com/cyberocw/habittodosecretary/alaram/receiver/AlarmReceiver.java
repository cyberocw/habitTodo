package com.cyberocw.habittodosecretary.alaram.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.alaram.service.AlarmBackgroudService;
import com.cyberocw.habittodosecretary.alaram.service.NotificationService;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmTimeVO;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.util.Calendar;
import java.util.Set;

/**
 * Created by cyberocw on 2015-08-31.
 */
//// TODO: 2016-10-03 데이터 전달할 객체 생성하여 전달하기 serializable 혹은 parceable
public class AlarmReceiver extends BroadcastReceiver{
	private AlarmBackgroudService mService;
	private boolean mBound;
	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmDataManager mAlarmDataManager = new AlarmDataManager(context, Calendar.getInstance());

		try { Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mAlarmDataManager.resetMinAlarmCall();


		//onReceiveOri(context, intent);



		/*
		Intent myIntent = new Intent(context, AlarmBackgroudService.class);
		myIntent.putExtras(intent.getExtras());

		Bundle bundle = intent.getExtras();

		Set<String> keySet = bundle.keySet();
		StringBuilder sb = new StringBuilder();
		for (String key: keySet
			 ) {
			sb.append(key + "\n");
		}
		Crashlytics.log(Log.DEBUG, this.toString(), " extara keys = " + sb.toString());
		context.startService(myIntent);*/


		//context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		//AlarmDataManager mAlarmDataManager = new AlarmDataManager(context, Calendar.getInstance());
		//mAlarmDataManager.resetMinAlarmCall();
	}
	/* nougat 버젼에서는 serializable 안됨 */
	public void onReceiveOri(Context context, Intent intent) {

		AlarmTimeVO alarmTimeVO = (AlarmTimeVO) intent.getSerializableExtra("alarmTimeVO");

		Log.d(this.toString(), "alarmTimeVO= " + alarmTimeVO);

		Calendar tempCal = Calendar.getInstance();
		tempCal.setTimeInMillis(alarmTimeVO.getTimeStamp());

		String aa = "alarmReceiver 다음 알람은 " + alarmTimeVO.getAlarmTitle() + " 알람 시간:" + CommonUtils.convertFullDateType(tempCal) + " ocwocw\n";

		CommonUtils.putLogPreference(context, aa);
	}

}