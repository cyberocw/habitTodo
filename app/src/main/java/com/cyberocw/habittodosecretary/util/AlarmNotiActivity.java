package com.cyberocw.habittodosecretary.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cyberocw.habittodosecretary.R;

import org.w3c.dom.Text;

/**
 * Created by cyberocw on 2015-11-16.
 */
public class AlarmNotiActivity extends AppCompatActivity {
	Vibrator mVibe = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer_noti);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String title = "";
		if(bundle != null)
			title = intent.getExtras().getString("title");

		mVibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = {1000, 200, 1000, 2000, 1200};          // 진동, 무진동, 진동 무진동 숫으로 시간을 설정한다.
		mVibe.vibrate(pattern, 0);                                         // 패턴을 지정하고 반복횟수를 지정
		//mVibe.vibrate(30000);                                                   //1초 동안 진동이 울린다.

		PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
		wakeLock.acquire();

		KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
		keyguardLock.disableKeyguard();

		TextView tvTitle = (TextView) findViewById(R.id.tvAlarmTitle);

		if(!title.equals("")){
			tvTitle.setText(title);
		}

		Button btnStop = (Button) findViewById(R.id.btnTimerStop);
		btnStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mVibe.cancel();
				finish();
			}
		});

	}

	public AlarmNotiActivity() {

	}

}
