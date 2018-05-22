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
	ArrayList<AlarmVO>[] mArrRepeatList;
	ArrayList<AlarmVO> mRepeatHolidayList;
	ArrayList[] mArrRepeatNonHolidayList;
	//int[] mArrRepeatCountNormal;
	int[] mArrRepeatCountHoliday, mArrRepeatCountDay;

	public CalendarManager(Context context) {
		mCtx = context;
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
		this.init(false);
	}

	public void init(boolean isInit){
		mAlarmDbManager = AlarmDbManager.getInstance(mCtx);
		mSettingDbManager = SettingDbManager.getInstance(mCtx);

		if(!isInit) {
			initWeekDay();
			renderDayNum();
		}
		makeRepeatHolidayInfo();
	}

	public void makeRepeatHolidayInfo(){
		//순수요일
		mArrRepeatList = new ArrayList[8];
		//반복 공휴일
		mRepeatHolidayList = new ArrayList();
		//휴일 아닐 경우
		mArrRepeatNonHolidayList = new ArrayList[8];
		//휴일 카운트
		mArrRepeatCountHoliday = new int[8];
		mArrRepeatCountDay = new int[8];

		ArrayList<AlarmVO> alarmRepeatList = getAlarmRepeatList();
		if(alarmRepeatList != null){
			AlarmVO alarmVO;
			ArrayList<Integer> arrRepeatDay;
			for(int i = 0 ; i < 8; i++)
				mArrRepeatList[i] = new ArrayList<>();
			Log.d(this.toString(), "alarmRepeatList.size()="+alarmRepeatList.size());
			for (int i = 0; i < alarmRepeatList.size(); i++) {
				alarmVO = alarmRepeatList.get(i);
				arrRepeatDay = alarmVO.getRepeatDay();
				if(alarmVO.getIsHolidayALL() == 1) {
					mRepeatHolidayList.add(alarmVO);
				}
				for(int k = 0; k < arrRepeatDay.size(); k++){

					if(mArrRepeatList[arrRepeatDay.get(k)] == null)
						mArrRepeatList[arrRepeatDay.get(k)] = new ArrayList<AlarmVO>();


					//해야 할것 - > 휴일 포함일 경우 그냥 요일이 겹치면 -1
					mArrRepeatList[arrRepeatDay.get(k)].add(alarmVO);
					mArrRepeatCountDay[arrRepeatDay.get(k)]+=1;

					if(arrRepeatDay.get(k) == 1 || arrRepeatDay.get(k) == 7)
						continue;

					if(alarmVO.getIsHolidayALL() == 1){
						//mRepeatHolidayList.add(alarmVO);
						// repeat 이랑 그냥 요일 2개 제거
						mArrRepeatCountHoliday[arrRepeatDay.get(k)] -= 1;
					}
					else{

					}
					if(alarmVO.getIsHolidayNone() == 1){
						if(alarmVO.getIsHolidayALL() == 1)
							mArrRepeatCountHoliday[arrRepeatDay.get(k)] -= 2;
						else
							mArrRepeatCountHoliday[arrRepeatDay.get(k)] -= 1;

						if(mArrRepeatNonHolidayList[arrRepeatDay.get(k)] == null)
							mArrRepeatNonHolidayList[arrRepeatDay.get(k)] = new ArrayList();
						mArrRepeatNonHolidayList[arrRepeatDay.get(k)].add(alarmVO);
					}
					//todo -> 공휴일 제외 카운트, 공휴일 포함 카운트 별도 array에 담아두기
				}

			}

			for(int i = 0 ; i < 8; i++){
				Log.d(this.toString(), "marrrepeat i = " + i + "  mArrRepeatCountDay size="+mArrRepeatCountDay[i]);
				mArrRepeatCountHoliday[i] = mArrRepeatCountHoliday[i] + mArrRepeatCountDay[i] + mRepeatHolidayList.size();
			}

			/*int oriCnt = 0;
			boolean isFind = false;
			for(int i = 0 ;i < mArrRepeatList.length; i++){
				oriCnt = mArrRepeatList[i].size();
				for(int k = 0 ; k < mArrRepeatList[i].size(); k++){
					for(int m = 0; m < mRepeatHolidayList.size(); m++){
						if(mRepeatHolidayList.get(m).getId() == mArrRepeatList[i].get(k).getId()){
							isFind = true;
						}
					}
				}
				mArrRepeatCountHoliday[i] =
			}*/
		}
	}


	public int getRepeatHolidayCnt(int dayNum, boolean isHoliday){
		if(isHoliday)
			return mArrRepeatCountHoliday[dayNum];
		else
			return mArrRepeatCountDay[dayNum];
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

	public ArrayList<AlarmVO> getAlarmRepeatList(){
		return mAlarmDbManager.getAlarmRepeatList();
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
			Log.d(this.toString(), "alarmVo title = " + arrVO.get(i).getAlarmTitle());
			if(arrVO.get(i).getAlarmDateType() == Const.ALARM_DATE_TYPE.POSTPONE_DATE)
				continue;
			mArrAlarmList.add(arrVO.get(i).getAlarmDateList().get(0).get(Calendar.DAY_OF_MONTH));
		}
	}

	private void getHolidayList(Calendar calOri){
		Calendar cal = (Calendar) calOri.clone();

		String startDate = CommonUtils.convertDateType(cal);
		cal.add(Calendar.DAY_OF_MONTH, 7);
		String endDate = CommonUtils.convertDateType(cal);

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
				Log.d(this.toString(), "on djay click");
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
			String strCal = CommonUtils.convertDateType(cal2);

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

	public Calendar getCalendar() {
		return (Calendar) mCalendar.clone();
	}

	public void setCalendar(Calendar cal) {
		mCalendar = cal;
	}

	public ArrayList<AlarmVO> getAlarmList(Calendar mCalendar) {
		return mAlarmDbManager.getAlarmList((Calendar) mCalendar.clone());
	}
}
