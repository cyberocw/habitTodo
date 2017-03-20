package com.cyberocw.habittodosecretary.alaram.ui;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

/**
 * Created by cyberocw on 2015-11-16.
 */
public class AlarmNotiActivity extends AppCompatActivity {
	Vibrator mVibe = null;
	String mTitle = "";
	long mAlarmId = -1;

	@BindView(R.id.tvAlarmTitle) TextView mTvTitle;
	@BindView(R.id.btnPostpone) Button mBtnPostpone;
	//@BindView(R.id.btnTimerStop) Button mBtnStop;

	@OnClick(R.id.btnTimerStop) void submit() {
		mVibe.cancel();
		finish();
	}
	@OnClick(R.id.btnPostpone) void clickPostpone(){
		mVibe.cancel();
		showPostPhone();
		finish();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer_noti);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String title = "";
		if(bundle != null) {
			title = bundle.getString("title");
			mAlarmId = bundle.getLong("alarmId");
		}

		ButterKnife.bind(this);

		SharedPreferences prefs = getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
		boolean isAlarmNoti = prefs.getBoolean(Const.SETTING.IS_ALARM_NOTI, true);
		if(isAlarmNoti) {
			mVibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			long[] pattern = {1000, 200, 1000, 2000, 1200};          // 진동, 무진동, 진동 무진동 숫으로 시간을 설정한다.
			mVibe.vibrate(pattern, 0);                                         // 패턴을 지정하고 반복횟수를 지정
			//mVibe.vibrate(30000);                                                   //1초 동안 진동이 울린다.
		}

		PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
		wakeLock.acquire();

		KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
		keyguardLock.disableKeyguard();

		//TextView tvTitle = (TextView) findViewById(R.id.tvAlarmTitle);

		if(!title.equals("")){
			mTvTitle.setText(title);
		}
		if(mAlarmId == -1){
			mBtnPostpone.setVisibility(View.INVISIBLE);
		}

		Fabric.with(this, new Crashlytics());
	}

	private void showPostPhone(){
		Intent intentAlarm = new Intent(getApplicationContext(), MainActivity.class);
		intentAlarm.putExtra(Const.PARAM.ALARM_ID, mAlarmId);
		intentAlarm.putExtra(Const.PARAM.MODE, Const.ALARM_INTERFACE_CODE.ALARM_POSTPONE_DIALOG);

		intentAlarm.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplicationContext().startActivity(intentAlarm);

	}

	public AlarmNotiActivity() {

	}

	@Override
	protected void onDestroy() {
		mVibe.cancel();
		finish();
		super.onDestroy();
	}
}
