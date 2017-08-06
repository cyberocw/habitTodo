package com.cyberocw.habittodosecretary.alaram.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.util.Calendar;

import io.fabric.sdk.android.Fabric;

/**
 * Created by cyberocw on 2015-08-31.
 */
public class NotificationService extends Service{

	private NotificationManager mManager;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
	}

	@SuppressWarnings({ "static-access", "deprecation" })
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		newNotification1(intent, startId);
	}

	public void newNotification1(Intent intent, int startId){
		if(intent == null || !intent.hasExtra("title") ) {
			stopSelf();
			return;
		}

		SharedPreferences prefs = getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
		boolean isAlarmNoti = prefs.getBoolean(Const.SETTING.IS_ALARM_NOTI, true);

		Bundle bundle = intent.getExtras();
		String noti_title = bundle.getString("title");
		String etcType = bundle.getString(Const.PARAM.ETC_TYPE_KEY, "");
		//String noti_message = intent.getExtras().getString("notes");
		int reqCode = bundle.getInt(Const.PARAM.REQ_CODE, -1);
		//나중에 reqCode 가 int 범위를 넘어설것 같을때 별도 처리해주기 noti id는 int만 가능해서
		long alarmId = bundle.getLong(Const.PARAM.ALARM_ID, -1);

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent1 = new Intent(this, MainActivity.class);
		intent1.putExtra(Const.PARAM.ETC_TYPE_KEY, etcType);
		intent1.putExtra(Const.PARAM.ALARM_ID, alarmId);
		intent1.putExtra(Const.PARAM.REQ_CODE, reqCode);

		intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, -1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mCompatBuilder = new NotificationCompat.Builder(this);

		mCompatBuilder.setSmallIcon(R.drawable.ic_stat_noti);
		mCompatBuilder.setTicker("Habit Todo");
		mCompatBuilder.setWhen(System.currentTimeMillis());
		mCompatBuilder.setContentTitle(noti_title);
		//mCompatBuilder.setContentTitle(noti_title);

		//mCompatBuilder.setContentText(noti_message);
		if(isAlarmNoti) {
			mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND);
			mCompatBuilder.setVibrate(new long[] { 100L, 100L, 200L, 200L, 100L, 100L, 100L, 100L, 100L, 100L});
		}

        RemoteViews remoteView = new RemoteViews(this.getPackageName(), R.layout.alarm_notification);
		remoteView.setOnClickPendingIntent(R.id.notiWrap, pendingIntent);

        remoteView.setTextViewText(R.id.tvAlarmTitle, noti_title);
		mCompatBuilder.setContentIntent(pendingIntent);
		mCompatBuilder.setAutoCancel(true);

		if(etcType.equals(Const.ETC_TYPE.MEMO)) {
			remoteView.setViewVisibility(R.id.tvAlarmSubTitle, View.VISIBLE);
			remoteView.setTextViewText(R.id.tvAlarmSubTitle, getString(R.string.service_noti_msg_view_memo_touch));
			mCompatBuilder.setContentText(getString(R.string.service_noti_msg_view_memo_touch));
		}else if(alarmId > -1){
			// alarmId 가 -1 이면 timer에서 보낸 noti임
			mCompatBuilder.setContentText(getString(R.string.service_noti_msg_scroll_postpone));
		}
        Calendar now = Calendar.getInstance();
        remoteView.setTextViewText(R.id.tvAlarmTime, CommonUtils.numberDigit(2, now.get(Calendar.HOUR_OF_DAY)) + ":" + CommonUtils.numberDigit(2, now.get(Calendar.MINUTE)));

		Intent closeButtonIntent = new Intent(this, CloseButtonListener.class);
		closeButtonIntent.putExtra(Const.PARAM.REQ_CODE, reqCode);
		PendingIntent pendingCloseButtonIntent = PendingIntent.getBroadcast(this, (int) reqCode, closeButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteView.setOnClickPendingIntent(R.id.btnCloseNoti, pendingCloseButtonIntent);

		if(alarmId > -1) {
			Intent intentAlarm = new Intent(this, MainActivity.class);
			intentAlarm.putExtra(Const.PARAM.ALARM_ID, alarmId);
			intentAlarm.putExtra(Const.PARAM.MODE, Const.ALARM_INTERFACE_CODE.ALARM_POSTPONE_DIALOG);
			intentAlarm.putExtra(Const.PARAM.REQ_CODE, reqCode);

			intentAlarm.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY);
			PendingIntent pendingIntentAlarm = PendingIntent.getActivity(this, 0, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

			remoteView.setOnClickPendingIntent(R.id.btnPostpone, pendingIntentAlarm);
			mCompatBuilder.addAction(R.drawable.ic_add_alert_black_24dp, getString(R.string.service_noti_postpone), pendingIntentAlarm);
		}
		//mCompatBuilder.setCustomContentView(remoteView);


		//NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "연장하기", pendingIntentAlarm).build();
		//mCompatBuilder.addAction(action);

		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, " noti reqCode="+reqCode);
		nm.notify(reqCode, mCompatBuilder.build());
		CommonUtils.logCustomEvent("NotificationService", "1");
		/*
		try {

			Uri notification_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification_uri);
			r.play();
		} catch (Exception e) {}
		*/
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

    public static class CloseButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "close button on receive bundle=" + intent.getExtras());

            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
                int reqCode = bundle.getInt(Const.PARAM.REQ_CODE);
                Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "reqCode="+reqCode);
                manager.cancel(reqCode);
            }
        }
    }
}

