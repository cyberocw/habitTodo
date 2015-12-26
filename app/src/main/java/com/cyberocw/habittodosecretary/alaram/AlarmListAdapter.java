package com.cyberocw.habittodosecretary.alaram;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;

/**
 * Created by cyberocw on 2015-08-16.
 */
public class AlarmListAdapter extends BaseAdapter{
	private AlarmDataManager mManager;
	private LayoutInflater inflater;
	private Context mCtx;
	private AlarmFragment mMainFragment;

	public AlarmListAdapter(Context ctx, AlarmDataManager mManager) {
		this.mManager = mManager;
		mCtx = ctx;
		inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public AlarmListAdapter(AlarmFragment mainFragment, Context ctx, AlarmDataManager mManager) {
		this.mMainFragment = mainFragment;
		this.mManager = mManager;
		mCtx = ctx;
		inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		Log.d(Const.DEBUG_TAG, "alarmListAdapter getCount = "+mManager.getCount());
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
		AlarmVO vo = mManager.getItem(position);

		Log.d(Const.DEBUG_TAG, "alarmList adapter position = " + position);

		if(convertView == null){
			convertView = inflater.inflate(R.layout.alarm_view, parent, false);
			switch (vo.getAlarmOption()){
				case Const.ALARM_OPTION.SET_DATE_TIMER :
					break;
				case Const.ALARM_OPTION.NO_DATE_TIMER :
					break;
			}
		}
		LinearLayout ll = (LinearLayout) convertView.findViewById(R.id.actionWrap);

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMainFragment.showNewAlarmDialog(mManager.getItem(position).getId());
			}
		});

		ToggleButton dateToggleBtn = (ToggleButton) convertView.findViewById(R.id.timeText);
		dateToggleBtn.setText(vo.getTimeText());
		dateToggleBtn.setTextOn(vo.getTimeText());
		dateToggleBtn.setTextOff(vo.getTimeText());

		if(vo.getUseYn() == 1)
			dateToggleBtn.setChecked(true);
		else
			dateToggleBtn.setChecked(false);

		dateToggleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmVO vo = mManager.getItem(position);
				ToggleButton btn = (ToggleButton) v;
				boolean isChecked = btn.isChecked();

				if(isChecked == true)
					vo.setUseYn(1);
				else
					vo.setUseYn(0);

				if(mManager.modifyUseYn(vo) == false)
					Toast.makeText(mCtx, "useYn 변환에 실패했습니다", Toast.LENGTH_SHORT).show();
				else {
					Log.d(Const.DEBUG_TAG, "dateToggleBtn change resetMinAlarmCall start");
					mManager.resetMinAlarmCall(vo.getAlarmDateType());
				}
			}
		});

		TextView tv = (TextView) convertView.findViewById(R.id.alarmTitle);
			tv.setText(mManager.getItem(position).getAlarmTitle());

		return convertView;
	}
}
