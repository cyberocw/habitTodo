package com.cyberocw.habittodosecretary.alaram.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.TimerListAdapter;
import com.cyberocw.habittodosecretary.alaram.ui.AlarmNotiActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

/**
 * Created by cyberocw on 2015-11-16.
 */
public class TimerService extends Service {
	private Context mCtx = this;
	private final IBinder mBinder = new LocalBinder();
	private CountDownTimer mCountDownTimer = null;
	public long mMillisRemainTime = -1;
	private int mPosition = -1;
	private String mTitle = "";
	public TextView mTv;
	private TimerListAdapter mTimerListAdapter = null;

	NumberFormat mNumberFormat = new DecimalFormat("##00");
	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public TimerService getService() {
			// Return this instance of LocalService so clients can call public methods
			return TimerService.this;
		}
	}
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		Bundle extras = intent.getExtras();
		Crashlytics.log(Log.DEBUG, "service", "onBind");
		// Get messager from the Activity
		if (extras != null) {
			Crashlytics.log(Log.DEBUG, "service", "onBind with extra");
			mPosition = (Integer) extras.get("position");
			mMillisRemainTime = (Long) extras.get("remainTime");
			mTitle = (String) extras.get("title");
		}
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		Toast.makeText(TimerService.this, "on rebind", Toast.LENGTH_SHORT).show();

		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "on rebind" + " mCountdownTimer is null=" + (mCountDownTimer == null));

		super.onRebind(intent);

		if(mCountDownTimer != null)
			mTimerListAdapter.showRunningAlert();
	}

	public TimerService() {
	}

	public void setTxtMap(HashMap<Integer, View> map){
		setTxtView((TextView) map.get(mPosition));
	}

	public void setTxtView(TextView view){
		mTv = view;
	}

	@Override
	public void onCreate() {

		super.onCreate();
		Fabric.with(this, new Crashlytics());

	}

	public void startTimer(){
		startTimer(mMillisRemainTime);
	}

	public void startTimer(long remainTime){

		if(mCountDownTimer != null)
			return ;

		int second = (int) (remainTime / 1000) % 60;
		int minute = (int) ((remainTime / (1000 * 60)) % 60);
		int hour = (int) ((remainTime / (1000 * 60 * 60)));

		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, Const.ONGOING_TIMER_NOTI_ID, notificationIntent, 0);

		NotificationCompat.Builder mCompatBuilder = new NotificationCompat.Builder(this);
		mCompatBuilder.setSmallIcon(R.drawable.ic_launcher);
		mCompatBuilder.setTicker("Habit Todo Timer");
		mCompatBuilder.setWhen(System.currentTimeMillis());
		//mCompatBuilder.setVibrate(new long[] { 100L, 100L, 200L, 200L, 300L, 300L, 400L, 400L });
		mCompatBuilder.setContentTitle("Habit Todo timer is running");
		mCompatBuilder.setContentText(mNumberFormat.format(hour) + ":" + mNumberFormat.format(minute) +
				":" + mNumberFormat.format(second));
		//mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
		mCompatBuilder.setContentIntent(pendingIntent);
		//mCompatBuilder.setAutoCancel(true);
		startForeground(Const.ONGOING_TIMER_NOTI_ID, mCompatBuilder.build());
		startCaountDownTimer(remainTime);
	}

	public void startTimer2(long remainTime){
		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "startTimer remainTime=" + mMillisRemainTime);

		if(mCountDownTimer != null)
			return ;

		Notification notification = new Notification(R.drawable.ic_launcher , "타이머" , System.currentTimeMillis());

		int second = (int) (remainTime / 1000) % 60;
		int minute = (int) ((remainTime / (1000 * 60)) % 60);
		int hour = (int) ((remainTime / (1000 * 60 * 60)));

		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, Const.ONGOING_TIMER_NOTI_ID, notificationIntent, 0);
		/*
		notification.setLatestEventInfo(this, "HbitTodo Timer is running",
				mNumberFormat.format(hour) + ":" + mNumberFormat.format(minute) +
						":" + mNumberFormat.format(second), pendingIntent);
		startForeground(Const.ONGOING_TIMER_NOTI_ID, notification);

		startCaountDownTimer(remainTime);
		*/
	}
	private void startCaountDownTimer(long remainTime){
		mCountDownTimer = new CountDownTimer(remainTime, 1000) {
			public void onTick(long millisUntilFinished) {
				mMillisRemainTime = millisUntilFinished;
				int second = (int) (millisUntilFinished / 1000) % 60;
				int minute = (int) ((millisUntilFinished / (1000 * 60)) % 60);
				int hour = (int) ((millisUntilFinished / (1000 * 60 * 60)));

				//Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "on tinck =" + second);

				if(mTv != null) {
					mTv.setText(mNumberFormat.format(hour) + ":" + mNumberFormat.format(minute) +
							":" + mNumberFormat.format(second));
				}
			}
			public void onFinish() {
				if(mTv != null) {
					mTv.setText(mNumberFormat.format(0) + ":" + mNumberFormat.format(0) +
							":" + mNumberFormat.format(0));
				}
				startAleart();
				cancelTimer();
				mCountDownTimer = null;
			}
		}.start();
	}
	private void startAleart(){
		Intent myIntent = new Intent(mCtx, AlarmNotiActivity.class);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		myIntent.putExtra("title", mTitle);
		mCtx.startActivity(myIntent);
	}

	public void cancelTimer(){
		mMillisRemainTime = -1;
		mCountDownTimer.cancel();
		mTimerListAdapter.resetButton();
		mTimerListAdapter.unbindService();
		//stopForeground(true);
		stopSelf();
	}

	public void bindInterface(TimerListAdapter timerListAdapter){
		mTimerListAdapter = timerListAdapter;
	}

	public long getRemainTime(){
		return mMillisRemainTime;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "onUnBind Service");
		return super.onUnbind(intent);
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "onTaskRemoved Service");
		super.onTaskRemoved(rootIntent);
	}

	@Override
	public void onDestroy() {
		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "onDestroy Service");
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "onStartCommand" + " mCountdownTimer is null=" + (mCountDownTimer == null));
		//if(mCountDownTimer != null)
		//	mTimerListAdapter.showRunningAlert();
		return super.onStartCommand(intent, flags, startId);
	}
}
