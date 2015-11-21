package com.cyberocw.habittodosecretary.alaram.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.cyberocw.habittodosecretary.util.AlarmNotiActivity;

import java.util.ServiceLoader;

/**
 * Created by cyberocw on 2015-11-16.
 */
public class TimerService extends Service {
	private Context mCtx = this;
	private final IBinder mBinder = new LocalBinder();
	private CountDownTimer mCountDownTimer;
	public long mMillisRemainTime = -1;

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
		return mBinder;
	}

	public TimerService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();

	}

	public void startTimer(long remainTime){
		mCountDownTimer = new CountDownTimer(remainTime, 1000) {
			public void onTick(long millisUntilFinished) {
				mMillisRemainTime = millisUntilFinished;
			}
			public void onFinish() {
				Intent myIntent = new Intent(mCtx, AlarmNotiActivity.class);
				mCtx.startActivity(myIntent);
			}
		}.start();
	}

	public void cancelTimer(){
		mMillisRemainTime = -1;
		mCountDownTimer.cancel();
		stopSelf();
	}

	public long getRemainTime(){
		return mMillisRemainTime;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}
}
