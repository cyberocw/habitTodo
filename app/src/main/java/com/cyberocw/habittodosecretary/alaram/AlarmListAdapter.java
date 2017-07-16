package com.cyberocw.habittodosecretary.alaram;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.ui.RenderAlarmView;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;

/**
 * Created by cyberocw on 2015-08-16.
 */
public class AlarmListAdapter extends BaseAdapter implements AlarmListAdapterInterface{
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
		return mManager.getCount();
	}

	@Override
	public Object getItem(int position) {
		return mManager.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return mManager.getItem(position).getId();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final AlarmVO vo = mManager.getItem(position);

		if(convertView == null){
			convertView = inflater.inflate(R.layout.alarm_view, parent, false);
		}

		RenderAlarmView.RenderAlarmView(mCtx, mMainFragment, mManager, vo, convertView, Const.ALARM_LIST_VIEW_TYPE.LIST, position);

		return convertView;
	}

	private void showRemovePostPhoneDidalog(AlarmVO vo){

	}
}
