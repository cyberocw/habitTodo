package com.cyberocw.habittodosecretary.alaram;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainFragment;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.service.TimerService;
import com.cyberocw.habittodosecretary.alaram.vo.TimerVO;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by cyberocw on 2015-10-18.
 */
public class TimerListAdapter extends BaseAdapter {
	private TimerDataManager mManager;
	private LayoutInflater inflater;
	private Context mCtx;
	private MainFragment mMainFragment;
	private CountDownTimer mCountDownTimer;
	NumberFormat mNumberFormat = new DecimalFormat("##00");
	TimerService mService;
	boolean mBound = false;

	public TimerListAdapter(Context ctx, TimerDataManager mManager) {
		this.mManager = mManager;
		mCtx = ctx;
		inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public TimerListAdapter(MainFragment mainFragment, Context ctx, TimerDataManager mManager) {
		this.mMainFragment = mainFragment;
		this.mManager = mManager;
		mCtx = ctx;
		inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		Log.d(Const.DEBUG_TAG, "TimerListAdapter getCount = "+mManager.getCount());
		return mManager.getCount();
	}

	@Override
	public Object getItem(int position) {
		return mManager.getItem(position).getAlarmTitle();
	}

	@Override
	public long getItemId(int position) {
		return mManager.getItem(position).getId();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		TimerVO vo = mManager.getItem(position);

		if(convertView == null){
			convertView = inflater.inflate(R.layout.alarm_view, parent, false);
		}

		LinearLayout ll = (LinearLayout) convertView.findViewById(R.id.actionWrap);

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMainFragment.showNewAlarmDialog(mManager.getItem(position).getId());
			}


		});

		int hour, minute, second;
		hour = mManager.getItem(position).getHour();
		minute =  mManager.getItem(position).getMinute();
		second = mManager.getItem(position).getSecond();


		ToggleButton dateToggleBtn = (ToggleButton) convertView.findViewById(R.id.timeText);
		dateToggleBtn.setText("Start");
		//dateToggleBtn.setTextOn(vo.getTimeText());
		//dateToggleBtn.setTextOff(vo.getTimeText());

		ll.removeAllViewsInLayout();

		final TextView tvActionWrap = new TextView(mCtx);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		tvActionWrap.setLayoutParams(params);

		Log.d(Const.DEBUG_TAG, "adapter position = " + position);

		final String defaultAlarmText = mNumberFormat.format(hour) + ":" + mNumberFormat.format(minute) +
				":" + mNumberFormat.format(second) + "\n후 알림";
		tvActionWrap.setText(defaultAlarmText);

		ll.addView(tvActionWrap);

		TextView tv = (TextView) convertView.findViewById(R.id.alarmTitle);
		tv.setText(mManager.getItem(position).getAlarmTitle());

		final long remainTime = (hour * 60 * 60 + minute * 60 + second) * 1000;

		//// TODO: 2015-11-17 서비스 시작 및 복구 되도록 변경 하기
		dateToggleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			TimerVO vo = mManager.getItem(position);
			ToggleButton btn = (ToggleButton) v;
			boolean isChecked = btn.isChecked();

			//mManager.setTimer(vo);
			if (isChecked == true) {
				Log.d(Const.DEBUG_TAG, "remainTime=" + remainTime);
				Intent intent;
				intent = new Intent(mCtx, TimerService.class);
				mCtx.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
				mCountDownTimer = new CountDownTimer(remainTime, 1000) {

					public void onTick(long millisUntilFinished) {

						int seconds = (int) (millisUntilFinished / 1000) % 60;
						int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
						int hours = (int) ((millisUntilFinished / (1000 * 60 * 60)));

						tvActionWrap.setText(mNumberFormat.format(hours) + ":" + mNumberFormat.format(minutes) +
								":" + mNumberFormat.format(seconds));
					}

					public void onFinish() {
						tvActionWrap.setText(defaultAlarmText);
					}
				}.start();
			}
			else{

				mCountDownTimer.cancel();
			}
			}
		});

		return convertView;
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
		                               IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
}


