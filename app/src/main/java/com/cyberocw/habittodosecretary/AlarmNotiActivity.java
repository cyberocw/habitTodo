package com.cyberocw.habittodosecretary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.record.PlayRawAudio;
import com.cyberocw.habittodosecretary.util.CommonUtils;
import com.cyberocw.habittodosecretary.util.TTSNoti;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

/**
 * Created by cyberocw on 2015-11-16.
 */
public class AlarmNotiActivity extends AppCompatActivity {
	Vibrator mVibe = null;
	String mTitle = "", mEtcType = "";
	long mAlarmId = -1;
	Bundle mBundle;
	long mStartedTimeInMilis = 0;
	boolean isButtonClick = false;
	Context mCtx;
	PlayRawAudio mPra = null;

	@BindView(R.id.tvAlarmTitle) TextView mTvTitle;
	@BindView(R.id.btnEtcView) Button mBtnEtcView;
	@BindView(R.id.btnPostpone) Button mBtnPostpone;
	@BindView(R.id.adViewFront)	AdView adView;
	//@BindView(R.id.btnTimerStop) Button mBtnStop;

	@OnClick(R.id.btnTimerStop) void submit() {
		if(mVibe != null)
			mVibe.cancel();
		if(mPra != null)
			mPra.cancel(true);

		isButtonClick = true;
		finish();
	}
	@OnClick(R.id.btnPostpone) void clickPostpone(){
		if(mVibe != null)
			mVibe.cancel();
		isButtonClick = true;
		showPostPhone();
		finish();
	}
	@OnClick(R.id.btnEtcView) void clickEtcView(){
		if(mVibe != null)
			mVibe.cancel();
		isButtonClick = true;
		showEtcView();
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		int alarmOption = -1;
		Log.d(this.toString(), " oncreated ocwocw" + intent.getExtras());

		super.onCreate(savedInstanceState);

		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
						WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
						WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
						WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_FULLSCREEN |
						WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
						WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
						WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


		setContentView(R.layout.timer_noti);
		ButterKnife.bind(this);
		mCtx = getApplicationContext();

		mBundle = intent.getExtras();
		String title = "";

		if(mBundle != null) {
			title = mBundle.getString("title");
			mAlarmId = mBundle.getLong("alarmId", -1);
			mEtcType = mBundle.getString(Const.PARAM.ETC_TYPE_KEY, "");
			if(mEtcType.equals(Const.ETC_TYPE.MEMO)){
				//mBtnEtcView.setText("메모 보기");
				mBtnEtcView.setVisibility(View.VISIBLE);
			}
			//개별 알림 TTS 재생 여부 옵션
			alarmOption = mBundle.getInt(Const.PARAM.ALARM_OPTION, -1);
		}
		SharedPreferences prefs = getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
		boolean isAlarmNoti = prefs.getBoolean(Const.SETTING.IS_ALARM_NOTI, true);
		if(isAlarmNoti) {
			mVibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			long[] pattern = {1000, 200, 1000, 2000, 1200};          // 진동, 무진동, 진동 무진동 숫으로 시간을 설정한다.
			mVibe.vibrate(pattern, 0);                                         // 패턴을 지정하고 반복횟수를 지정
			//mVibe.vibrate(30000);                                                   //1초 동안 진동이 울린다.
		}
/*

		PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
		wakeLock.acquire();

		KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
		keyguardLock.disableKeyguard();
*/

		//TextView tvTitle = (TextView) findViewById(R.id.tvAlarmTitle);

		if(!title.equals("")){
			mTvTitle.setText(title);
		}
		if(mAlarmId == -1){
			mBtnPostpone.setVisibility(View.INVISIBLE);
		}

		//MobileAds.initialize(this, "ca-app-pub-8072677228798230/5472850905"); // real

		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("048A3A6B542D3DD340272D8C1D80AC18")
				.build();
		adView.loadAd(adRequest);

		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		Fabric.with(this, new Crashlytics());
		mStartedTimeInMilis = Calendar.getInstance().getTimeInMillis();

		boolean isTTS = prefs.getBoolean(Const.SETTING.IS_TTS_NOTI, true);
		boolean isTTSManner = prefs.getBoolean(Const.SETTING.IS_TTS_NOTI_MANNER, true);


		if(alarmOption == 1 && isTTS && !isTTSManner){
			AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			switch (am.getRingerMode()) {
				case AudioManager.RINGER_MODE_SILENT:
					Log.i(Const.DEBUG_TAG, "Silent mode");
				case AudioManager.RINGER_MODE_VIBRATE:
					Log.i(Const.DEBUG_TAG, "Vibrate mode");
					isTTS = false;
					break;
				case AudioManager.RINGER_MODE_NORMAL:
					Log.i(Const.DEBUG_TAG, "Normal mode");
			}
		}
		if(alarmOption == Const.ALARM_OPTION_TO_SOUND.TTS && isTTS) {
			startTTS(title, mAlarmId);
		}else if(alarmOption == Const.ALARM_OPTION_TO_SOUND.RECORD && isTTS){
			String fileName = mAlarmId + ".wav";
			//String filePath = mCtx.getFilesDir().getAbsolutePath() + "voice" + File.separator + fileName;

			File rootDir=new File(mCtx.getFilesDir(), "voice");
			rootDir.mkdirs();
			File f = new File(rootDir, fileName);
			Log.d(this.toString(), "absolute="+f.getAbsolutePath() + " getPaht= " + f.getPath());
			try {
				if(f.isFile()){
					mPra = new PlayRawAudio(f.getAbsolutePath());
					mPra.execute();
				}
				else{
					Toast.makeText(mCtx, "파일 경로가 잘못되었습니다", Toast.LENGTH_SHORT).show();
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		CommonUtils.logCustomEvent("AlarmNotiActivity", "1");
	}
	private void startTTS(String title, long id){
		Intent ttsIntent = new Intent(getApplicationContext(), TTSNoti.class);

		ttsIntent.putExtra("alaramTitle", title);
		ttsIntent.putExtra("alarmId", id);
		getApplicationContext().startService(ttsIntent);
	}

	private void showEtcView(){
		if(mEtcType.equals(Const.ETC_TYPE.MEMO)){
			Intent intent1 = new Intent(this, MainActivity.class);
			intent1.putExtra(Const.PARAM.ETC_TYPE_KEY, mEtcType);
			intent1.putExtra(Const.PARAM.ALARM_ID, mAlarmId);
			//intent1.putExtra(Const.PARAM.REQ_CODE, reqCode);
			intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplicationContext().startActivity(intent1);
		}
	}

	private void showPostPhone(){
		Intent intentAlarm = new Intent(getApplicationContext(), MainActivity.class);
		intentAlarm.putExtra(Const.PARAM.ALARM_ID, mAlarmId);
		intentAlarm.putExtra(Const.PARAM.MODE, Const.ALARM_INTERFACE_CODE.ALARM_POSTPONE_DIALOG);

		intentAlarm.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplicationContext().startActivity(intentAlarm);

	}

	public AlarmNotiActivity() {

	}

	@Override
	public void onBackPressed() {
		Log.d(this.toString(), "onBackPressed");
		//autoPostpone();
		super.onBackPressed();
	}

	protected void autoPostpone() {
		Log.d(this.toString(), "on autoPostpone");

		AlarmDataManager alarmDataManager = new AlarmDataManager(mCtx);
		if(mAlarmId == -1)
			return;

		final AlarmVO alarmVO = alarmDataManager.getItemByIdInDB(mAlarmId);
		ArrayList<Integer> arrAlarmCall = new ArrayList<Integer>();
		arrAlarmCall.add(0);
		alarmVO.setAlarmCallList(arrAlarmCall);
		alarmVO.setAlarmDateType(Const.ALARM_DATE_TYPE.POSTPONE_DATE);
		alarmVO.setIsHolidayALL(0);
		alarmVO.setIsHolidayNone(0);
		alarmVO.setRepeatDay(null);

		//알림 날짜 계산
		ArrayList<Calendar> alarmDate = new ArrayList<Calendar>();
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MINUTE, 1);
		alarmDate.add(now);
		alarmVO.setAlarmDateList(alarmDate);
		alarmVO.setHour(now.get(Calendar.HOUR_OF_DAY));
		alarmVO.setMinute(now.get(Calendar.MINUTE));

		if(alarmDataManager.addItem(alarmVO) == true)
			Toast.makeText(mCtx, getString(R.string.noti_activity_msg_postponed), Toast.LENGTH_LONG).show();
		else
			Toast.makeText(mCtx, getString(R.string.msg_failed_insert), Toast.LENGTH_LONG).show();

		alarmDataManager.resetMinAlarmCall();
		finish();
	}
	@Override
	protected void onStop() {
		super.onStop();
		long nowTime = Calendar.getInstance().getTimeInMillis();
		Log.d(this.toString(), "onStop timeMils = " + (nowTime - mStartedTimeInMilis));


		if(nowTime - mStartedTimeInMilis > 500 && isButtonClick == false)
			autoPostpone();
	}

	@Override
	protected void onResume() {
		Log.d(this.toString(), "onResume");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.d(this.toString(), "onPause");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if(mVibe != null)
			mVibe.cancel();
		super.onDestroy();
	}
}
