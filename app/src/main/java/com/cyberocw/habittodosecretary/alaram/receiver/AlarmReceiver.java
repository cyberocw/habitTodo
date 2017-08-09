package com.cyberocw.habittodosecretary.alaram.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.alaram.service.AlarmBackgroudService;
import com.cyberocw.habittodosecretary.alaram.service.NotificationService;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmTimeVO;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.Set;

import io.fabric.sdk.android.Fabric;

/**
 * Created by cyberocw on 2015-08-31.
 */
//// TODO: 2016-10-03 데이터 전달할 객체 생성하여 전달하기 serializable 혹은 parceable
public class AlarmReceiver extends WakefulBroadcastReceiver {
	private AlarmBackgroudService mService;
	private boolean mBound;
	private Handler mHandler = null;
	private static PowerManager.WakeLock mCpuWakeLock;
	private static boolean isScreenLock;

	private PowerManager pm;
	private PowerManager.WakeLock wakeLock;

	@Override
	public void onReceive(Context context, Intent intent) {
		Fabric.with(context, new Crashlytics());
		pm = ((PowerManager) context.getSystemService(Context.POWER_SERVICE));
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "receive");
		// wakelock 사용

		wakeLock.acquire();
		Crashlytics.log(Log.DEBUG, this.toString(), "receive wakeLock acquire");

		String log = "onReceive start";
		Fabric.with(context, new Crashlytics());
		CommonUtils.putLogPreference(context, log);
		//Crashlytics.log(Log.DEBUG, this.toString(), log);



		AlarmDataManager mAlarmDataManager = new AlarmDataManager(context, Calendar.getInstance());

		try { Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		/*Intent myIntent1 = new Intent(context, NotificationService.class);
		myIntent1.putExtra("title", "receive");
		context.startService(myIntent1);
*/
		log = "receiver after 1sec sleep minAlarmCall start";
		CommonUtils.putLogPreference(context, log);
		//Crashlytics.log(Log.DEBUG, this.toString(), log);

		mAlarmDataManager.resetMinAlarmCall();

		log = "receiver resetMinAlarm end";
		CommonUtils.putLogPreference(context, log);
		//Crashlytics.log(Log.DEBUG, this.toString(), log);

		//onReceiveOri(context, intent);

		mHandler = new Handler();
		mHandler.postDelayed(new Runnable()
		{
			@Override public void run()
			{
				if(wakeLock.isHeld()) {
					wakeLock.release();
					Crashlytics.log(Log.DEBUG, this.toString(), "receive wakeLock release");
				}

			}
		}, 2500);


		if(1==1)
			return;


		Intent myIntent = new Intent(context, AlarmBackgroudService.class);

		ByteArrayInputStream bis = new ByteArrayInputStream(intent.getByteArrayExtra(Const.PARAM.ALARM_TIME_VO));
		ObjectInput in = null;
		AlarmTimeVO alarmTimeVO = null;
		try {
			in = new ObjectInputStream(bis);
			alarmTimeVO = (AlarmTimeVO)in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		myIntent.putExtra(Const.PARAM.ALARM_TIME_VO, alarmTimeVO);

		Bundle bundle = intent.getExtras();

		Set<String> keySet = bundle.keySet();
		StringBuilder sb = new StringBuilder();
		for (String key: keySet
			 ) {
			sb.append(key + "\n");
		}
		Crashlytics.log(Log.DEBUG, this.toString(), " extara keys = " + sb.toString());
		startWakefulService(context, myIntent);


		//context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		//AlarmDataManager mAlarmDataManager = new AlarmDataManager(context, Calendar.getInstance());
		//mAlarmDataManager.resetMinAlarmCall();
	}
	/* nougat 버젼에서는 serializable 안됨 */
	public void onReceiveOri(Context context, Intent intent) {

		AlarmTimeVO alarmTimeVO = (AlarmTimeVO) intent.getSerializableExtra(Const.PARAM.ALARM_TIME_VO);

		Log.d(this.toString(), "alarmTimeVO= " + alarmTimeVO);

		Calendar tempCal = Calendar.getInstance();
		tempCal.setTimeInMillis(alarmTimeVO.getTimeStamp());

		String aa = "alarmReceiver 다음 알람은 " + alarmTimeVO.getAlarmTitle() + " 알람 시간:" + CommonUtils.convertFullDateType(tempCal) + " ocwocw\n";

		CommonUtils.putLogPreference(context, aa);
	}

}