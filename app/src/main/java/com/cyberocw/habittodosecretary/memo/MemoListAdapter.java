package com.cyberocw.habittodosecretary.memo;

import android.content.Context;
import android.content.DialogInterface;
import android.media.Rating;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;
import com.cyberocw.habittodosecretary.memo.MemoDataManager;
import com.cyberocw.habittodosecretary.memo.MemoFragment;

import java.util.Calendar;

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
		return mManager.getCount();
	}

	@Override
	public MemoVO getItem(int position) {
		return mManager.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return mManager.getItem(position).getId();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		MemoVO vo = mManager.getItem(position);

		if(convertView == null){
			convertView = inflater.inflate(R.layout.memo_view, parent, false);
		}
		TextView tvTitle = (TextView) convertView.findViewById(R.id.txMemoTitle);
		TextView txMemoCont = (TextView) convertView.findViewById(R.id.tvMemoCont);
		TextView tvRegDate = (TextView) convertView.findViewById(R.id.tvRegDate);
		RatingBar r = (RatingBar) convertView.findViewById(R.id.ratingBar);
		ImageView ivAlarm = (ImageView) convertView.findViewById(R.id.ivAlarm);
		ImageView ivAttachFile = (ImageView) convertView.findViewById(R.id.ivAttachFile);
		ImageView ivInfo = (ImageView) convertView.findViewById(R.id.ivInfo);
		r.setRating((float) vo.getRank());

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(vo.getUpdateDt());
		tvRegDate.setText(cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DAY_OF_MONTH));
		tvTitle.setText(vo.getTitle());
		txMemoCont.setText(vo.getContents());

		if(vo.getAlarmId() > -1){
			ivAlarm.setVisibility(View.VISIBLE);
		}

		ImageButton ibtn = (ImageButton) convertView.findViewById(R.id.memoOptionButton);
		ibtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				longClickPopup(position, mManager.getItem(position).getId());
			}
		});

		return convertView;
	}

	private void longClickPopup(int position, final long _id){
		String names[] ={"편집","삭제"};
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mCtx);

		ListView lv = new ListView(mCtx);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		alertDialog.setView(lv);
		alertDialog.setTitle("옵션");

		lv.setLayoutParams(params);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mCtx,android.R.layout.simple_list_item_1,names);
		lv.setAdapter(adapter);

		final DialogInterface dialogInterface = alertDialog.show();

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0:
						mFragment.showNewMemoDialog(_id);
						break;
					case 1:
						mFragment.deleteItemAlertDialog(_id);
						break;
				}
				dialogInterface.dismiss();
			}
		});


	}

}
