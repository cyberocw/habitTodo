package com.cyberocw.habittodosecretary.memo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;
import com.cyberocw.habittodosecretary.memo.MemoDataManager;
import com.cyberocw.habittodosecretary.memo.MemoFragment;

/**
 * Created by cyberocw on 2015-12-06.
 */
public class MemoListAdapter extends BaseAdapter {
	private MemoDataManager mManager;
	private LayoutInflater inflater;
	private Context mCtx;
	private MemoFragment mFragment;

	public MemoListAdapter(Context ctx, MemoDataManager mManager) {
		this.mManager = mManager;
		mCtx = ctx;
		inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public MemoListAdapter(MemoFragment fragment, Context ctx, MemoDataManager mManager) {
		this.mFragment = fragment;
		this.mManager = mManager;
		mCtx = ctx;
		inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		Log.d(Const.DEBUG_TAG, "alarmListAdapter getCount = " + mManager.getCount());
		return mManager.getCount();
	}

	@Override
	public Object getItem(int position) {
		return mManager.getItem(position).getTitle();
	}

	@Override
	public long getItemId(int position) {
		return mManager.getItem(position).getId();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		MemoVO vo = mManager.getItem(position);

		Log.d(Const.DEBUG_TAG, "catelist adapter position = " + position);

		if(convertView == null){
			convertView = inflater.inflate(R.layout.cate_view, parent, false);
		}
		TextView tvTitle = (TextView) convertView.findViewById(R.id.tvCateTitle);
		TextView tvCnt = (TextView) convertView.findViewById(R.id.tvCateCnt);

		tvTitle.setText(vo.getTitle());


		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//mFragment.showMemoList(mManager.getItem(position).getId());
			}
		});
		return convertView;
	}
}
