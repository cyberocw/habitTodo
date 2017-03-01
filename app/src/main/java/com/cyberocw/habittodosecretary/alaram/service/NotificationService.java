package com.cyberocw.habittodosecretary.alaram.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;

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
	}

	@SuppressWarnings({ "static-access", "deprecation" })
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		if(1==1)
			newNotification1(intent, startId);
		else {
			/*
			String Noti_title = intent.getExtras().getString("title");
			String Noti_message = intent.getExtras().getString("notes");
			long reqCode = intent.getExtras().getLong("reqCode");

			mManager = (NotificationManager) this.getApplicationContext().getSystemService(this.getApplicationContext().NOTIFICATION_SERVICE);

			Intent intent1 = new Intent(this.getApplicationContext(), MainActivity.class);

			Notification notification = new Notification(R.drawable.ic_launcher, Noti_title, System.currentTimeMillis());
			intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

			PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			notification.setLatestEventInfo(this.getApplicationContext(), Noti_title, Noti_message, pendingNotificationIntent);
			notification.vibrate = new long[]{100L, 100L, 200L, 200L, 300L, 300L, 400L, 400L};
			notification.defaults |= Notification.DEFAULT_SOUND;
			mManager.notify((int) reqCode, notification);
			try {
				Uri notification_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification_uri);
				r.play();
			} catch (Exception e) {
			}

			stopSelf();
			*/
		}

	}

	public void newNotification(Intent intent, int startId) {
		String noti_title = intent.getExtras().getString("title");
		String noti_message = intent.getExtras().getString("notes");
		long reqCode = intent.getExtras().getLong("reqCode");
		//나중에 reqCode 가 int 범위를 넘어설것 같을때 별도 처리해주기 noti id는 int만 가능해서
		long alarmId = intent.getExtras().getLong("alarmId");
		Intent intent1 = new Intent(this, MainActivity.class);
		intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mCompatBuilder = new NotificationCompat.Builder(this);
		mCompatBuilder.setSmallIcon(R.drawable.ic_launcher);
		mCompatBuilder.setContentTitle(noti_title);
		//mCompatBuilder.setOngoing(true); // clear 기능에 영향 X

		mCompatBuilder.setTicker("Habit Todo");
		mCompatBuilder.setWhen(System.currentTimeMillis());
		//mCompatBuilder.setNumber(10);

		mCompatBuilder.setVibrate(new long[] { 100L, 100L, 200L, 200L, 300L, 300L, 400L, 400L });

		mCompatBuilder.setContentText(noti_message);
		mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
		mCompatBuilder.setContentIntent(pendingIntent);
		mCompatBuilder.setAutoCancel(true);

		RemoteViews remoteView = new RemoteViews(this.getPackageName(), R.layout.alarm_notification);

		//set the button listeners
		//setListeners(remoteView);
		mCompatBuilder.setContent(remoteView);
		Notification notification = mCompatBuilder.build();
		//notification.contentView = remoteView;

		NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify((int) reqCode, notification);

	}

	public void setListeners(RemoteViews view){
		//listener 1
		/*
		Intent volume = new Intent(this,NotificationReturnSlot.class);
		volume.putExtra("DO", "volume");
		PendingIntent btn1 = PendingIntent.getActivity(parent, 0, volume, 0);
		view.setOnClickPendingIntent(R.id.btn1, btn1);

		//listener 2
		Intent stop = new Intent(this, NotificationReturnSlot.class);
		stop.putExtra("DO", "stop");
		PendingIntent btn2 = PendingIntent.getActivity(parent, 1, stop, 0);
		view.setOnClickPendingIntent(R.id.btn2, btn2);
		*/
	}

	public void newNotification1(Intent intent, int startId){
		String noti_title = intent.getExtras().getString("title");
		String noti_message = intent.getExtras().getString("notes");
		long reqCode = intent.getExtras().getLong("reqCode");
		//나중에 reqCode 가 int 범위를 넘어설것 같을때 별도 처리해주기 noti id는 int만 가능해서
		long alarmId = intent.getExtras().getLong("alarmId");

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent1 = new Intent(this, MainActivity.class);
		intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mCompatBuilder = new NotificationCompat.Builder(this);
		mCompatBuilder.setSmallIcon(R.drawable.ic_launcher);
		mCompatBuilder.setTicker("Habit Todo");
		mCompatBuilder.setWhen(System.currentTimeMillis());
		mCompatBuilder.setVibrate(new long[] { 100L, 100L, 200L, 200L, 300L, 300L, 400L, 400L });
		mCompatBuilder.setContentTitle(noti_title);
		mCompatBuilder.setContentText(noti_message);
		mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
		mCompatBuilder.setContentIntent(pendingIntent);
		mCompatBuilder.setAutoCancel(true);

		Intent intentAlarm = new Intent(this, MainActivity.class);
		intentAlarm.putExtra(Const.PARAM.ALARM_ID, alarmId);
		intentAlarm.putExtra(Const.PARAM.MODE, Const.ALARM_INTERFACE_CODE.ALARM_POSTPONE_DIALOG);
		//intentAlarm.putExtra(Const.ALARM_INTERFACE_CODE.ALARM_POSTPONE_DIALOG)
		intentAlarm.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntentAlarm = PendingIntent.getActivity(this, 0, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "연장하기", pendingIntentAlarm).build();

		mCompatBuilder.addAction(action);

		nm.notify((int)reqCode, mCompatBuilder.build());

		try {
			Uri notification_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification_uri);
			r.play();
		} catch (Exception e) {}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
