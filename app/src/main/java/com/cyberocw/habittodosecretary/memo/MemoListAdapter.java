package com.cyberocw.habittodosecretary.memo;

import android.content.Context;
import android.media.Rating;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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
		Log.d(Const.DEBUG_TAG, "memoListAdapter getCount = " + mManager.getCount());
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

		Log.d(Const.DEBUG_TAG, "memo list adapter position = " + position);

		if(convertView == null){
			convertView = inflater.inflate(R.layout.memo_view, parent, false);
		}
		TextView tvTitle = (TextView) convertView.findViewById(R.id.txMemoTitle);
		TextView txMemoCont = (TextView) convertView.findViewById(R.id.tvMemoCont);
		RatingBar r = (RatingBar) convertView.findViewById(R.id.ratingBar);
		r.setRating((float) vo.getRank());

		tvTitle.setText(vo.getTitle());
		txMemoCont.setText(vo.getContents());

		Log.d(Const.DEBUG_TAG, "vo=" + vo.toString());

		ImageButton ibtn = (ImageButton) convertView.findViewById(R.id.memoOptionButton);
		ibtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(mCtx, "selected position"+position, Toast.LENGTH_SHORT).show();
			}
		});

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			mFragment.showNewMemoDialog(mManager.getItem(position).getId());
			}
		});
		return convertView;
	}
}
