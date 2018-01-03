package com.cyberocw.habittodosecretary.alaram;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.common.vo.FileVO;
import com.cyberocw.habittodosecretary.file.FileDataManager;
import com.cyberocw.habittodosecretary.file.StorageHelper;
import com.cyberocw.habittodosecretary.record.RecorderDataManager;
import com.cyberocw.habittodosecretary.util.CommonUtils;
import com.cyberocw.habittodosecretary.util.TTSNoti;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
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
	PlayAudio mPra = null;
	private FirebaseAnalytics mFirebaseAnalytics;
	boolean isPlaying = false;
	NativeExpressAdView adView;
	Intent mTTSIntent = null;
	TTSNoti mTTSNotiService;
	boolean mBound = false;

	@BindView(R.id.tvAlarmTitle) TextView mTvTitle;
	@BindView(R.id.btnEtcView) Button mBtnEtcView;
	@BindView(R.id.btnPostpone) Button mBtnPostpone;

	//@BindView(R.id.btnTimerStop) Button mBtnStop;

	@OnClick(R.id.btnTimerStop) void submit() {
		stopAll();
		isButtonClick = true;
		finish();
	}
	@OnClick(R.id.btnPostpone) void clickPostpone(){
		stopAll();
		isButtonClick = true;
		showPostPhone();
		finish();
	}
	@OnClick(R.id.btnEtcView) void clickEtcView(){
		stopAll();
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
			alarmOption = mBundle.getInt(Const.PARAM.ALARM_OPTION, 1);
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

		adView = (NativeExpressAdView) findViewById(R.id.adViewFront);

        MobileAds.initialize(this, "ca-app-pub-8072677228798230~8421474102"); // real

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("048A3A6B542D3DD340272D8C1D80AC18")
                .build();

        adView.loadAd(adRequest);

		if(!title.equals("")){
			mTvTitle.setText(title);
		}
		if(mAlarmId == -1){
			mBtnPostpone.setVisibility(View.INVISIBLE);
		}
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
		Fabric.with(this, new Crashlytics());
		mStartedTimeInMilis = Calendar.getInstance().getTimeInMillis();

		boolean isTTS = prefs.getBoolean(Const.SETTING.IS_TTS_NOTI, true);
		boolean isTTSManner = prefs.getBoolean(Const.SETTING.IS_TTS_NOTI_MANNER, true);

		//매너모드에서도 재생 여부 결정
		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		//무음에서는 재생 안함
		if(am.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
			isTTS = false;
		//매너모드 강제 TTS 재생 옵션이 활성화 되어 있지 않으면 모드에 따라 재생 안함
		if(alarmOption == Const.ALARM_OPTION_TO_SOUND.TTS && isTTS && !isTTSManner){
			switch (am.getRingerMode()) {
				case AudioManager.RINGER_MODE_SILENT:
				case AudioManager.RINGER_MODE_VIBRATE:
					isTTS = false;
					break;
				case AudioManager.RINGER_MODE_NORMAL:
					break;
			}
		}

		if(alarmOption == Const.ALARM_OPTION_TO_SOUND.TTS && isTTS) {
			startTTS(title, mAlarmId);
		}else if(alarmOption == Const.ALARM_OPTION_TO_SOUND.RECORD && isTTS){
			FileDataManager fdm = new FileDataManager(mCtx);
			fdm.makeDataList(Const.ETC_TYPE.ALARM, mAlarmId);

			try {
				ArrayList<FileVO> arrFile = fdm.getDataList();
				FileVO fileVO = arrFile.get(0);
				File f = new File(fileVO.getUriPath());

				Log.d(this.toString(), "absolute="+f.getAbsolutePath() + " getPaht= " + f.getPath());

				if(f.isFile()){
					mPra = new PlayAudio(f.getAbsolutePath());
					mPra.execute();
				}
				else{
					Toast.makeText(mCtx, getResources().getString(R.string.msg_file_not_found), Toast.LENGTH_SHORT).show();
					startTTS(title, mAlarmId);
				}
			}catch (Exception e){
				startTTS(title, mAlarmId);
				e.printStackTrace();
			}
		}
		//소리 옵션 0
		else{

		}
		CommonUtils.logCustomEvent("AlarmNotiActivity", "1");
	}
	private void stopAll(){
		if(mVibe != null)
			mVibe.cancel();
		if(mPra != null) {
			isPlaying = false;
			mPra.cancel(true);
		}
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	private void startTTS(String title, long id){
		mTTSIntent = new Intent(getApplicationContext(), TTSNoti.class);
		mTTSIntent.putExtra("alaramTitle", title);
		mTTSIntent.putExtra("alarmId", id);
		//getApplicationContext().startService(mTTSIntent);
		bindService(mTTSIntent, mConnection, Context.BIND_AUTO_CREATE);
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

		if(alarmVO.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD) {
			FileDataManager fdm = new FileDataManager(mCtx);
			fdm.makeDataList(Const.ETC_TYPE.ALARM, mAlarmId);
			alarmVO.setFileList(fdm.getDataList());
		}
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
		now.add(Calendar.MINUTE, 3);
		alarmDate.add(now);
		alarmVO.setAlarmDateList(alarmDate);
		alarmVO.setHour(now.get(Calendar.HOUR_OF_DAY));
		alarmVO.setMinute(now.get(Calendar.MINUTE));

		if(alarmDataManager.addItem(alarmVO) == true) {
			RecorderDataManager rm = new RecorderDataManager(mCtx);
			if(alarmVO.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD) {
				File targetFile = StorageHelper.createNewAttachmentFile(mCtx, Environment.DIRECTORY_RINGTONES, ".wav");
				boolean result = rm.saveFile(alarmVO.getFileList().get(0).getUriPath(), targetFile);
				if (result) {
					Log.d(this.toString(), "미디어 복사 성공");
					//연기이기 때문에 기존파일 삭제 안함
					//getRecorderDataManager().deleteRecordFile(fromPath);
					//db저장
					alarmDataManager.saveFile(alarmVO, targetFile);
				}
			}

			Toast.makeText(mCtx, getString(R.string.success), Toast.LENGTH_LONG).show();
		}
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

		if(nowTime - mStartedTimeInMilis > 3500 && isButtonClick == false) {
			stopAll();
			autoPostpone();
		}
	}

	@Override
	protected void onResume() {
		Log.d(this.toString(), "onResume");
		super.onResume();
		adView.resume();
	}

	@Override
	protected void onPause() {
		Log.d(this.toString(), "onPause");
		adView.pause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		stopAll();
		super.onDestroy();
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
									   IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			TTSNoti.LocalBinder binder = (TTSNoti.LocalBinder) service;
			mTTSNotiService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};


	private class PlayAudio extends AsyncTask<Void, Integer, Void> {
		String mPlayFileName;
		public PlayAudio(String fileName) {
			mPlayFileName = fileName;
		}
		@Override
		protected Void doInBackground(Void... params) {
			Log.d(this.toString(), " audiotrack  mPlayFileName="+ mPlayFileName);
			File fPlay = new File(mPlayFileName);
			if(!fPlay.isFile()){
				Toast.makeText(mCtx, getResources().getString(R.string.msg_file_not_found), Toast.LENGTH_SHORT).show();
				return null;
			}
			isPlaying = true;
			int bufferSize = AudioTrack.getMinBufferSize(Const.RECORDER.FREQUENCY, Const.RECORDER.CHANNEL_CONFIGURATION_OUT, Const.RECORDER.AUDIO_ENCODING);
			short[] audiodata = new short[bufferSize / 4];

			try {
				DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fPlay)));
				AudioTrack audioTrack = new AudioTrack(
						AudioManager.STREAM_MUSIC, Const.RECORDER.FREQUENCY,
						Const.RECORDER.CHANNEL_CONFIGURATION_OUT, Const.RECORDER.AUDIO_ENCODING, bufferSize,
						AudioTrack.MODE_STREAM);

				audioTrack.play();
				while (isPlaying && dis.available() > 0) {
					int i = 0;
					while (dis.available() > 0 && i < audiodata.length) {
						audiodata[i] = dis.readShort();
						i++;
					}
					audioTrack.write(audiodata, 0, audiodata.length);
				}
				dis.close();
			} catch (Throwable t) {
				Log.e("AudioTrack", "Playback Failed");
			}



			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {

			}
			if(isPlaying){
				mPra = new PlayAudio(mPlayFileName);
				mPra.execute();
			}
		}
	}
}
