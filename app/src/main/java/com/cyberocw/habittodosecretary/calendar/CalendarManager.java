package com.cyberocw.habittodosecretary.calendar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.db.AlarmDbManager;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.alaram.vo.HolidayVO;
import com.cyberocw.habittodosecretary.settings.db.SettingDbManager;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by cyberocw on 2015-10-09.
 */
public class CalendarManager {
	private Context mCtx;
	private Calendar mCalendar;
	private AlarmDbManager mAlarmDbManager;
	private SettingDbManager mSettingDbManager;
	private LinearLayout mWrapper;
	private LinearLayout[] arrIconWrap = new LinearLayout[7];
	private HashMap[] mArrDateMap = new HashMap[7];
	private String[] dayText = null;
	private TextView[] arrTextViewDayTitle = new TextView[7];
	private TextView[] arrTextViewDayNum = new TextView[7];
	private ImageView[] arrImageView = new ImageView[7];
	private TextView[] arrTextDayName= new TextView[7];
	private TextView mFullDateView = null;
	private ArrayList<Integer> mArrAlarmList;
	private DatePickerDialog.OnDateSetListener mListener;
	private Calendar mStartDate;
	private Calendar mEndDate;
	int pixelsHeight, pixelsIcon, pixelsDayName, pixelsIconWrapHeight;
	private HashMap<String, ArrayList> mHolidayMap;


	public CalendarManager(Context context) {
		mAlarmDbManager = AlarmDbManager.getInstance(mCtx);
		mSettingDbManager = SettingDbManager.getInstance(mCtx);
	}

	public CalendarManager(Context context, LinearLayout llWeekOfDayWrap, Calendar calendar, TextView fullDateView) {
		mCtx = context;
		mCalendar = calendar;
		mWrapper = llWeekOfDayWrap;
		mFullDateView = fullDateView;
		final float scale = mCtx.getResources().getDisplayMetrics().density;
		pixelsHeight = mCtx.getResources().getDimensionPixelSize(R.dimen.calendarDayTextHeight);
		pixelsIcon = (int) (4 * scale + 0.5f);
		pixelsDayName = mCtx.getResources().getDimensionPixelSize(R.dimen.calendarDayNameHeight);
		pixelsIconWrapHeight = mCtx.getResources().getDimensionPixelSize(R.dimen.calendarDayIconWrapHeight);
		dayText = new String[]{context.getString(R.string.cal_sun), context.getString(R.string.cal_mon), context.getString(R.string.cal_tue),
				context.getString(R.string.cal_wed), context.getString(R.string.cal_Thu), context.getString(R.string.cal_fri), context.getString(R.string.cal_sat)};
	}

	public void init(){
		mAlarmDbManager = AlarmDbManager.getInstance(mCtx);
		mSettingDbManager = SettingDbManager.getInstance(mCtx);
		initWeekDay();
		renderDayNum();
	}

	public ArrayList<AlarmVO> getAlarmMonthList(Calendar dateOri){
		Calendar startDate = (Calendar) dateOri.clone();
		Calendar endDate = (Calendar) dateOri.clone();

		startDate.set(Calendar.DAY_OF_MONTH, 1);
		endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));

		return getAlarmList(startDate, endDate);
	}

	public ArrayList<AlarmVO> getAlarmList(Calendar startDate, Calendar endDate){
		return mAlarmDbManager.getAlarmList(startDate, endDate);
	}

	public ArrayList<HolidayVO> getHolidayMonthList(Calendar dateOri){
		Calendar date = (Calendar) dateOri.clone();

		date.set(Calendar.DAY_OF_MONTH, 1);
		String startDate = CommonUtils.convertDateType(date);
		date.set(Calendar.DAY_OF_MONTH, date.getActualMaximum(Calendar.DAY_OF_MONTH));
		String endDate = CommonUtils.convertDateType(date);

		return getHolidayList(startDate, endDate);
	}

	public ArrayList<HolidayVO> getHolidayList(Calendar startDate, Calendar endDate){
		return this.getHolidayList(CommonUtils.convertDateType(startDate), CommonUtils.convertDateType(endDate));
	}

	public ArrayList<HolidayVO> getHolidayList(String startDate, String endDate){
		return mSettingDbManager.getHolidayList(startDate, endDate);
	}
	private void getAlarmList(){
		ArrayList<AlarmVO> arrVO =  mAlarmDbManager.getAlarmList(mStartDate, mEndDate);
		mArrAlarmList = new ArrayList<>();

		for(int i = 0; i < arrVO.size(); i++){
			Crashlytics.log(Log.DEBUG, getClass().toString(), " alarm day  = " + arrVO.get(i).getAlarmDateList().get(0).get(Calendar.DAY_OF_MONTH));
			if(arrVO.get(i).getAlarmDateType() == Const.ALARM_DATE_TYPE.POSTPONE_DATE)
				continue;
			mArrAlarmList.add(arrVO.get(i).getAlarmDateList().get(0).get(Calendar.DAY_OF_MONTH));
		}
	}

	private void getHolidayList(Calendar calOri){
		Calendar cal = (Calendar) calOri.clone();

		String startDate = String.valueOf(cal.get(Calendar.YEAR)) + CommonUtils.numberDigit(2, cal.get(Calendar.MONTH) + 1) + CommonUtils.numberDigit(2, cal.get(Calendar.DAY_OF_MONTH));
		cal.add(Calendar.DAY_OF_MONTH, 7);
		String endDate = String.valueOf(cal.get(Calendar.YEAR)) + CommonUtils.numberDigit(2, cal.get(Calendar.MONTH) + 1) + CommonUtils.numberDigit(2, cal.get(Calendar.DAY_OF_MONTH));

		mHolidayMap = mSettingDbManager.getHolidayMap(startDate, endDate);
	}

	public void initWeekDay(){
		for(int i = 0; i < 7; i++){
			this.initDayArea(i);
		}
	}

	private void initDayArea(final int index){

		LinearLayout dayWrap = new LinearLayout(mCtx);
		LinearLayout.LayoutParams paramsLl = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
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
		LinearLayout.LayoutParams paramsTv = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
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

		LinearLayout iconWrap = new LinearLayout(mCtx);
		LinearLayout.LayoutParams paramsIconWrap = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, pixelsIcon * 2, 1f);
		iconWrap.setOrientation(LinearLayout.VERTICAL);
		paramsIconWrap.gravity = Gravity.CENTER;
		iconWrap.setLayoutParams(paramsIconWrap);
		arrIconWrap[index] = iconWrap;

		ImageView iv = new ImageView(mCtx);

		LinearLayout.LayoutParams paramsIv = new LinearLayout.LayoutParams(pixelsIcon, pixelsIcon);
		paramsIv.gravity = Gravity.CENTER;
		//iv.setPadding(0,2,0,2);
		iv.setLayoutParams(paramsIv);
		arrImageView[index] = iv;
		iconWrap.addView(iv);

		dayWrap.addView(iconWrap);


		TextView tvDayName = new TextView(mCtx);
		//LinearLayout.LayoutParams paramsDayName = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
		tvDayName.setLayoutParams(paramsTv);
		//tvDayName.setHeight(pixelsDayName);
		tvDayName.setGravity(Gravity.CENTER);

		tvDayName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
		tvDayName.setSingleLine(true);
		tvDayName.setEllipsize(TextUtils.TruncateAt.END);
		tvDayName.setText("aa");
		dayWrap.addView(tvDayName);
		arrTextDayName[index] = tvDayName;

		mWrapper.addView(dayWrap);

		mArrDateMap[index] = new HashMap<>();
	}

	public void renderDayNum(){
		int fDay;
		int dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK) -1;
		Calendar cal2 = (Calendar) mCalendar.clone();
		cal2.add(Calendar.DAY_OF_MONTH, -1 * dayOfWeek);
		Calendar cal3 = (Calendar) cal2.clone();

		mStartDate = cal3;
		Calendar cal4 = (Calendar) cal3.clone();
		cal4.add(Calendar.DAY_OF_MONTH, 6);
		mEndDate = cal4;

		getAlarmList();
		getHolidayList(mStartDate);

		//mHolidayMap


		for(int i = 0 ; i < 7; i++){
			fDay = cal2.get(Calendar.DAY_OF_MONTH);
			if(i == 0) {
				arrTextViewDayTitle[i].setTextColor(Color.RED);
				arrTextViewDayNum[i].setTextColor(Color.RED);
			}
			else if(i == 6){
				arrTextViewDayTitle[i].setTextColor(Color.BLUE);
				arrTextViewDayNum[i].setTextColor(Color.BLUE);
			}
			else{
				arrTextViewDayTitle[i].setTextColor(Color.BLACK);
				arrTextViewDayNum[i].setTextColor(Color.BLACK);
			}
			// 공휴일정보 체크
			arrTextDayName[i].setText("");
			String strCal = String.valueOf(cal2.get(Calendar.YEAR)) + CommonUtils.numberDigit(2, cal2.get(Calendar.MONTH) + 1) + CommonUtils.numberDigit(2, cal2.get(Calendar.DAY_OF_MONTH));

			if (mHolidayMap.containsKey(strCal)) {
				ArrayList<HolidayVO> arrHoliday = mHolidayMap.get(strCal);
				for (int m = 0; m < arrHoliday.size(); m++) {
					HolidayVO hVO = arrHoliday.get(m);

					if (hVO.getType().equals("h") || hVO.getType().equals("i")) {
						arrTextViewDayTitle[i].setTextColor(Color.RED);
						arrTextViewDayNum[i].setTextColor(Color.RED);
						String daytext ="";
						if(hVO.getType().equals("i"))
							daytext = "대체공휴일";
						else
							daytext = hVO.getName();

						arrTextDayName[i].setText(daytext);

						/*
						arrTextDayName[i].setGravity(Gravity.CENTER);
						arrTextDayName[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
						arrTextDayName[i].setSingleLine(true);
						arrTextDayName[i].setEllipsize(TextUtils.TruncateAt.END);
						*/

						if(mCalendar.getTimeInMillis() == cal2.getTimeInMillis()){
							Crashlytics.log(Log.DEBUG, this.toString(), "mFullDateView.getText()="+mFullDateView.getText());
							mFullDateView.setText(mFullDateView.getText() + " - " + daytext);
						}
						break;
					}
				}
			}
			arrTextViewDayNum[i].setText(fDay + "");
			//오늘 날짜
			if(mCalendar.getTimeInMillis() == cal2.getTimeInMillis()){
				//arrTextViewDayNum[i].setBackgroundResource(R.drawable.day_of_week_ring);
				arrIconWrap[i].setBackgroundResource(R.drawable.day_of_week_ring);
			}else{
				//arrTextViewDayNum[i].setBackgroundResource(0);
				arrIconWrap[i].setBackgroundResource(0);
			}

			if(i == 0){
				mStartDate = (Calendar) cal2.clone();
			}
			else if(i == 6){
				mEndDate = (Calendar) cal2.clone();
			}

			mArrDateMap[i].put("year", cal2.get(Calendar.YEAR));
			mArrDateMap[i].put("month", cal2.get(Calendar.MONTH));
			mArrDateMap[i].put("day", cal2.get(Calendar.DAY_OF_MONTH));

			//일정이 있으면 날짜 아래에 o 표시
			if(mArrAlarmList.contains(fDay)){
				arrImageView[i].setImageResource(R.drawable.dot);

			}else{
				arrImageView[i].setImageResource(0);

			}
			cal2.add(Calendar.DAY_OF_MONTH, 1);
		}
	}

	public void setDayClickListener(DatePickerDialog.OnDateSetListener dayClickListener) {
		mListener = dayClickListener;
	}

	public ArrayList<AlarmVO> getAlarmList(Calendar mCalendar) {
		return mAlarmDbManager.getAlarmList((Calendar) mCalendar.clone());
	}
}
