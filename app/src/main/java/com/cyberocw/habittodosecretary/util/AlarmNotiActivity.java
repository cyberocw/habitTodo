package com.cyberocw.habittodosecretary.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.cyberocw.habittodosecretary.R;

/**
 * Created by cyberocw on 2015-11-16.
 */
public class AlarmNotiActivity extends AppCompatActivity {
	Vibrator mVibe = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer_noti);

		mVibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = {1000, 200, 1000, 2000, 1200};          // 진동, 무진동, 진동 무진동 숫으로 시간을 설정한다.
		mVibe.vibrate(pattern, 0);                                         // 패턴을 지정하고 반복횟수를 지정
		mVibe.vibrate(30000);                                                   //1초 동안 진동이 울린다.

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
