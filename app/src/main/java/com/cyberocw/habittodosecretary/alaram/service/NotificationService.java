package com.cyberocw.habittodosecretary.alaram.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

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

		String Noti_title = intent.getExtras().getString("title");
		String Noti_message = intent.getExtras().getString("notes");
		long reqCode = intent.getExtras().getLong("reqCode");

		Log.d(Const.DEBUG_TAG, "notify reqCode = " + reqCode);

		mManager = (NotificationManager) this.getApplicationContext().getSystemService(this.getApplicationContext().NOTIFICATION_SERVICE);

		Intent intent1 = new Intent(this.getApplicationContext(), MainActivity.class);

		Notification notification = new Notification(R.drawable.ic_launcher , Noti_title , System.currentTimeMillis());
		intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notification.setLatestEventInfo(this.getApplicationContext(), Noti_title, Noti_message, pendingNotificationIntent);
		notification.vibrate = new long[] { 100L, 100L, 200L, 200L, 300L, 300L, 400L, 400L };
		notification.defaults |= Notification.DEFAULT_SOUND;
		mManager.notify((int)reqCode , notification);
		try {
			Uri notification_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification_uri);
			r.play();
		} catch (Exception e) {}

		stopSelf();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
