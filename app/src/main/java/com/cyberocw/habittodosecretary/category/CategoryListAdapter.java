package com.cyberocw.habittodosecretary.category;

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
import com.cyberocw.habittodosecretary.category.vo.CategoryVO;

/**
 * Created by cyberocw on 2015-12-06.
 */
public class CategoryListAdapter extends BaseAdapter {
	private CategoryDataManager mManager;
	private LayoutInflater inflater;
	private Context mCtx;
	private CategoryFragment mCategoryFragment;

	public CategoryListAdapter(Context ctx, CategoryDataManager mManager) {
		this.mManager = mManager;
		mCtx = ctx;
		inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public CategoryListAdapter(CategoryFragment CategoryFragment, Context ctx, CategoryDataManager mManager) {
		this.mCategoryFragment = CategoryFragment;
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
		CategoryVO vo = mManager.getItem(position);

		Log.d(Const.DEBUG_TAG, "catelist adapter position = " + position);

		if(convertView == null){
			convertView = inflater.inflate(R.layout.cate_view, parent, false);
		}
		TextView tvTitle = (TextView) convertView.findViewById(R.id.tvCateTitle);
		TextView tvCnt = (TextView) convertView.findViewById(R.id.tvCateCnt);

		tvTitle.setText(vo.getTitle());
		Log.d(Const.DEBUG_TAG, "sortOrder = " + vo.getSortOrder());

		tvCnt.setText(Integer.toString(vo.getCnt()));

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCategoryFragment.showMemoList(mManager.getItem(position).getId());
			}
		});
		return convertView;
	}
}
