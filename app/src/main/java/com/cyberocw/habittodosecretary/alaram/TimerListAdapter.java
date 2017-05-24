package com.cyberocw.habittodosecretary.alaram;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.service.TimerService;
import com.cyberocw.habittodosecretary.alaram.vo.TimerVO;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

/**
 * Created by cyberocw on 2015-10-18.
 */
public class TimerListAdapter extends BaseAdapter {
	private TimerDataManager mManager;
	private LayoutInflater inflater;
	private Context mCtx;
	private AlarmFragment mMainFragment;
	private CountDownTimer mCountDownTimer;
	private HashMap<Integer, View> mMapConvertView = new HashMap<>();
	private ToggleButton mBtnCheckedToggle = null;
	SharedPreferences mPrefs = null;
	private long mStartedTimerId;

	NumberFormat mNumberFormat = new DecimalFormat("##00");
	TimerService mService;
	boolean mBound = false;

	public TimerListAdapter(AlarmFragment mainFragment, Context ctx, TimerDataManager mManager) {
		this.mMainFragment = mainFragment;
		this.mManager = mManager;
		mCtx = ctx;
		inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPrefs = mCtx.getSharedPreferences(Const.TIMER_RUNNING_ID, Context.MODE_PRIVATE);
		refereshStartedTimerId();
	}

	public void refereshStartedTimerId(){
		mStartedTimerId = mPrefs.getLong("timerId", -1);
	}

	@Override
	public int getCount() {
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
		if(convertView == null){
			convertView = inflater.inflate(R.layout.timer_view, parent, false);
		}

		LinearLayout ll = (LinearLayout) convertView.findViewById(R.id.actionWrap);

		LinearLayout listViewTextWrap = (LinearLayout) convertView.findViewById(R.id.listViewTextWrap);
		int padding = mCtx.getResources().getDimensionPixelOffset(R.dimen.timerListViewPadding);
		if(position == mManager.getCount()-1){
			//float scale = ctx.getResources().getDisplayMetrics().density;
			int paddingBottom = mCtx.getResources().getDimensionPixelOffset(R.dimen.listViewBottom);
			listViewTextWrap.setPadding(padding, padding, padding, paddingBottom);
		}else{
			listViewTextWrap.setPadding(padding, padding, padding, padding);
		}

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMainFragment.showNewTimerDialog(mManager.getItem(position).getId());
			}


		});

		int hour, minute, second;
		hour = mManager.getItem(position).getHour();
		minute =  mManager.getItem(position).getMinute();
		second = mManager.getItem(position).getSecond();

		final ToggleButton btnDateToggle = (ToggleButton) convertView.findViewById(R.id.timeText);

		btnDateToggle.setText("Start");
		ll.removeAllViewsInLayout();

		final TextView tvActionWrap = new TextView(mCtx);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		tvActionWrap.setLayoutParams(params);

		this.addTimeRemainView(position, tvActionWrap);

		final String defaultAlarmText = mCtx.getString(R.string.timer_list_limit_text) + "\n" + mNumberFormat.format(hour) + ":" + mNumberFormat.format(minute) +
				":" + mNumberFormat.format(second);
		tvActionWrap.setText(defaultAlarmText);
		//남은 시간 표시 영역
		ll.addView(tvActionWrap);

		//timer title
		TextView tv = (TextView) convertView.findViewById(R.id.alarmTitle);
		tv.setText(mManager.getItem(position).getAlarmTitle());

		final long remainTime = (hour * 60 * 60 + minute * 60 + second) * 1000;

		Log.d(this.toString(), "mStartedTimerId = " + mStartedTimerId + "  this alarmId = " + mManager.getItem(position).getId()
				+ "  mbound = " + mBound );

		//// TODO: 2015-11-17 서비스 시작 및 복구 되도록 변경 하기
		btnDateToggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimerVO vo = mManager.getItem(position);
				ToggleButton btn = (ToggleButton) v;
				boolean isChecked = btn.isChecked();

				Log.d(this.toString(), "mStartedTimerId = " + mStartedTimerId + "  this alarmId = " + mManager.getItem(position).getId()
						+ "  mbound = " + mBound + " isChecked = "+isChecked);

				//이미 실행중인 타이머가 있을 경우
				if (mBound == true && isChecked == true) {
					showRunningAlert();
					btn.toggle();
					return;
				}
				mBtnCheckedToggle = btnDateToggle;

				if (isChecked == true) {
					Intent intent;
					intent = new Intent(mCtx, TimerService.class);
					intent.putExtra("remainTime", remainTime);
					intent.putExtra("position", position);
					intent.putExtra("title", vo.getAlarmTitle());
					mCtx.startService(intent);
					mCtx.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
					btnDateToggle.setText(mCtx.getResources().getText(R.string.timerBtnToggleOn));
					setPrefsTimerId(mManager.getItem(position).getId());

				} else {
					btnDateToggle.setText(mCtx.getResources().getText(R.string.timerBtnToggleOff));
					if (mBound)
						mService.cancelTimer();
				}
			}
		});
		ImageButton btnOption = (ImageButton) convertView.findViewById(R.id.optionButton);
		btnOption.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//AlarmVO vo = mManager.getItem(position);
				mMainFragment.longClickPopup(0, mManager.getItem(position).getId());
			}
		});

		if(mStartedTimerId == mManager.getItem(position).getId()){
			if(mBound)
				mService.setTxtMap(mMapConvertView);

			mBtnCheckedToggle = btnDateToggle;

			Intent intent;
			intent = new Intent(mCtx, TimerService.class);
			mCtx.startService(intent);
			mCtx.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			btnDateToggle.setChecked(true);
			btnDateToggle.setText(mCtx.getResources().getText(R.string.timerBtnToggleOn));
			setPrefsTimerId(mManager.getItem(position).getId());


			//btnDateToggle.callOnClick();

		}
		return convertView;
	}

	private void addTimeRemainView(int position, View convertView){
		mMapConvertView.put(position, convertView);
	}

	//// TODO: 2015-11-22 나중에 interface로 빼기
	public void unbindService(){
		//activity가 종료되는 경우 mConnection 값이 달라져서 unbindService 호출 에러가 뜸
		Log.d(this.toString(), "unbindService ");

		mBound = false;
		try {
			if (mConnection != null)
				mCtx.unbindService(mConnection);

			this.notifyDataSetChanged();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void resetButton(){
		if(mBtnCheckedToggle.isChecked()){
			mBtnCheckedToggle.toggle();
		}
		resetTimerId();
	}

	public void resetTimerId(){
		mStartedTimerId = -1;
		setPrefsTimerId();
	}

	private void setPrefsTimerId(long id){
		mStartedTimerId = id;
		setPrefsTimerId();
	}

	private void setPrefsTimerId(){
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putLong("timerId", mStartedTimerId);
		editor.apply();
	}

	public void showRunningAlert(){
		Toast.makeText(mCtx, mCtx.getString(R.string.timer_is_running), Toast.LENGTH_LONG).show();
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
		                               IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			Log.d(this.toString(), "onServiceConnected ");

			TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
			mService = binder.getService();
			mService.bindInterface(TimerListAdapter.this);
			mService.setTxtMap(mMapConvertView);
			mBound = true;
			mService.startTimer();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};


}


