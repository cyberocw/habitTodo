package com.cyberocw.habittodosecretary.calendar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by cyberocw on 2015-10-09.
 */
public class CalendarManager {
	private Context mCtx;
	private Calendar mCalendar;
	private LinearLayout mWrapper;
	private HashMap[] mArrDateMap = new HashMap[7];
	private String[] dayText = {"일", "월", "화", "수", "목", "금", "토"};
	private TextView[] arrTextViewDayTitle = new TextView[7];
	private TextView[] arrTextViewDayNum = new TextView[7];
	private DatePickerDialog.OnDateSetListener mListener;

	public CalendarManager(Context context, LinearLayout llWeekOfDayWrap, Calendar calendar) {
		mCtx = context;
		mCalendar = calendar;
		mWrapper = llWeekOfDayWrap;
		initWeekDay();
		renderDayNum();
	}

	public void initWeekDay(){
		for(int i = 0; i < 7; i++){
			this.initDayArea(i);
		}
	}

	private void initDayArea(final int index){
		final float scale = mCtx.getResources().getDisplayMetrics().density;
		int pixels = (int) (20 * scale + 0.5f);

		LinearLayout dayWrap = new LinearLayout(mCtx);
		LinearLayout.LayoutParams paramsLl = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
		dayWrap.setOrientation(LinearLayout.VERTICAL);
		paramsLl.gravity = Gravity.CENTER;
		dayWrap.setLayoutParams(paramsLl);
		dayWrap.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onDateSet(null, (int) mArrDateMap[index].get("year"), (int) mArrDateMap[index].get("month"), (int) mArrDateMap[index].get("day"));
			}
		});

		TextView tvTitle = new TextView(mCtx);
		LinearLayout.LayoutParams paramsTv = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, pixels);
		tvTitle.setLayoutParams(paramsTv);
		tvTitle.setText(dayText[index]);
		tvTitle.setGravity(Gravity.CENTER);
		dayWrap.addView(tvTitle);
		arrTextViewDayTitle[index] = tvTitle;

		TextView tvDayNum = new TextView(mCtx);
		tvDayNum.setLayoutParams(paramsTv);
		tvDayNum.setGravity(Gravity.CENTER);
		dayWrap.addView(tvDayNum);

		arrTextViewDayNum[index] = tvDayNum;

		mWrapper.addView(dayWrap);

		mArrDateMap[index] = new HashMap<>();
	}

	public void renderDayNum(){
		Log.d(Const.DEBUG_TAG, "render day num");
		int dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK) -1;
		Calendar cal2 = (Calendar) mCalendar.clone();
		cal2.add(Calendar.DAY_OF_MONTH, -1 * dayOfWeek);
		int fDay;

		for(int i = 0 ; i < 7; i++){
			fDay = cal2.get(Calendar.DAY_OF_MONTH);

			if(i == 0) {
				arrTextViewDayTitle[i].setTextColor(Color.RED);
				arrTextViewDayNum[i].setTextColor(Color.RED);
			}
			if(i == 6){
				arrTextViewDayTitle[i].setTextColor(Color.BLUE);
				arrTextViewDayNum[i].setTextColor(Color.BLUE);
			}
			arrTextViewDayNum[i].setText(fDay + "");

			if(mCalendar.getTimeInMillis() == cal2.getTimeInMillis()){
				arrTextViewDayNum[i].setBackgroundResource(R.drawable.day_of_week_ring);
			}else{
				arrTextViewDayNum[i].setBackgroundResource(0);
			}

			mArrDateMap[i].put("year", cal2.get(Calendar.YEAR));
			mArrDateMap[i].put("month", cal2.get(Calendar.MONTH));
			mArrDateMap[i].put("day", cal2.get(Calendar.DAY_OF_MONTH));

			cal2.add(Calendar.DAY_OF_MONTH, 1);
		}
	}

	public void setDayClickListener(DatePickerDialog.OnDateSetListener dayClickListener) {
		mListener = dayClickListener;
	}
}
