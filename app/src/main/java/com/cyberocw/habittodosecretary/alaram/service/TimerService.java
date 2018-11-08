package com.cyberocw.habittodosecretary.alaram.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.TimerListAdapter;
import com.cyberocw.habittodosecretary.alaram.AlarmNotiActivity;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmTimeVO;
import com.cyberocw.habittodosecretary.record.PlayRawAudio;
import com.cyberocw.habittodosecretary.util.CommonUtils;
import com.cyberocw.habittodosecretary.util.TTSNoti;
import com.cyberocw.habittodosecretary.util.TTSNotiActivity;

import java.io.File;
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
	private int mPosition = -1, mAlarmType = 0, mAlarmOption = 0;
	private String mTitle = "";
	public TextView mTv;
	public ToggleButton mBtnToggle;
	private TimerListAdapter mTimerListAdapter = null;

	private Handler mHandler = null;
	private static PowerManager.WakeLock mCpuWakeLock;
	private static boolean isScreenLock;

	private PowerManager pm;
	private PowerManager.WakeLock wakeLock;

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
			mAlarmOption = extras.getInt("alarmOption", 0);
			mAlarmType = extras.getInt("alarmType", 1);
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
	public void setToggleButton(ToggleButton view){
		mBtnToggle = view;
	}

	@Override
	public void onCreate() {
		Fabric.with(mCtx, new Crashlytics());
		super.onCreate();

		pm = ((PowerManager) mCtx.getSystemService(Context.POWER_SERVICE));
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "receive");
		// wakelock 사용

		wakeLock.acquire();
		Crashlytics.log(Log.DEBUG, this.toString(), "timerservice wakeLock acquire");
	}

	public void startTimer(){
		startTimer(mMillisRemainTime);
	}

	public void startTimer(long remainTime){

		Log.d(this.toString(), "mMillisRemainTime = " + mMillisRemainTime);

		if(mCountDownTimer != null)
			return ;

		int second = (int) (remainTime / 1000) % 60;
		int minute = (int) ((remainTime / (1000 * 60)) % 60);
		int hour = (int) ((remainTime / (1000 * 60 * 60)));

		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, Const.ONGOING_TIMER_NOTI_ID, notificationIntent, 0);

		NotificationCompat.Builder mCompatBuilder = new NotificationCompat.Builder(this, Const.CHANNEL.SILENT_ID);
		mCompatBuilder.setSmallIcon(R.drawable.ic_stat_noti);
		mCompatBuilder.setTicker(getResources().getString(R.string.app_name));
		mCompatBuilder.setWhen(System.currentTimeMillis());
		//mCompatBuilder.setVibrate(new long[] { 100L, 100L, 200L, 200L, 300L, 300L, 400L, 400L });
		mCompatBuilder.setContentTitle("Habit Todo timer is running");
		mCompatBuilder.setContentText(mNumberFormat.format(hour) + ":" + mNumberFormat.format(minute) +
				":" + mNumberFormat.format(second));
		//mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
		mCompatBuilder.setContentIntent(pendingIntent);
		mCompatBuilder.setVibrate(null)
		.setSound(null);
		mCompatBuilder.setAutoCancel(false);
		Crashlytics.log(Log.DEBUG, this.toString(), "timer noti start");
		startForeground(Const.ONGOING_TIMER_NOTI_ID, mCompatBuilder.build());
		startCaountDownTimer(remainTime);
	}

	private void startCaountDownTimer(long remainTime){
		if(mBtnToggle != null)
			mBtnToggle.setChecked(true);

		if(remainTime < 0) {
			cancelTimer();
			mCountDownTimer = null;
			return;
		}

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
		if(mAlarmType < 1)
			startNotibar();
		else
			alarmNotiActivity();
	}
	private void startNotibar(){
		SharedPreferences prefs = getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
		boolean isTTS = prefs.getBoolean(Const.SETTING.IS_TTS_NOTI, true);
		boolean isTTSManner = prefs.getBoolean(Const.SETTING.IS_TTS_NOTI_MANNER, true);
		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		if(am.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
			isTTS = false;
		if(isTTS && !isTTSManner){
			switch (am.getRingerMode()) {
				case AudioManager.RINGER_MODE_SILENT:
				case AudioManager.RINGER_MODE_VIBRATE:
					isTTS = false;
					break;
				case AudioManager.RINGER_MODE_NORMAL:
					break;
			}
		}
		Intent myIntent = new Intent(mCtx, NotificationService.class);
		myIntent.putExtra("title", mTitle);
		//myIntent.putExtra(Const.PARAM.ETC_TYPE_KEY, mArrAlarmVOList.get(mMinRemainPosition).getEtcType());
		//myIntent.putExtra(Const.PARAM.REQ_CODE, mArrAlarmVOList.get(mMinRemainPosition).getReqCode());
		myIntent.putExtra(Const.PARAM.ALARM_ID, -1l);
		mCtx.startService(myIntent);

		//mAlarmOption - 개별 알람 tts
		if(mAlarmOption == Const.ALARM_OPTION_TO_SOUND.TTS && isTTS) {
			//알람 id는 현재 의미 없는 상태임
			startTTS(mTitle, -1);
		}
	}
	private void startTTS(String title, long id){
		Intent ttsIntent = new Intent(getApplicationContext(), TTSNoti.class);
		ttsIntent.putExtra("alaramTitle", title);
		ttsIntent.putExtra("alarmId", id);
		getApplicationContext().startService(ttsIntent);
		//mCtx.startActivity(ttsIntent);
	}
	private void alarmNotiActivity(){
		Intent myIntent = new Intent(mCtx, AlarmNotiActivity.class);
		myIntent.putExtra("title", mTitle);
		myIntent.putExtra(Const.PARAM.ALARM_OPTION, mAlarmOption);
		myIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		Crashlytics.log(Log.DEBUG, this.toString(), "start alarmNotiActivity mTitle=" + mTitle);

		mCtx.startActivity(myIntent);
	}
	public void cancelTimer(){
		mMillisRemainTime = -1;
		if(mCountDownTimer != null)
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
		if(mHandler == null) {
			mHandler = new Handler();
		}
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (wakeLock.isHeld()) {
					wakeLock.release();
					Crashlytics.log(Log.DEBUG, this.toString(), " timerservice wakeLock release");
				}
			}
		}, 5000);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "onStartCommand mCountdownTimer is null=" + (mCountDownTimer == null));
		//if(mCountDownTimer != null)
		//	mTimerListAdapter.showRunningAlert();
		return super.onStartCommand(intent, flags, startId);
	}
}
