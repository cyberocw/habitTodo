package com.cyberocw.habittodosecretary.alaram.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static android.util.Log.*;

/**
 * Created by cyberocw on 2015-08-19.
 */

public class AlarmDialogNew extends DialogFragment{
	private AlarmVO mAlarmVO;
	private MemoVO mMemoVO;

	private LinkedHashMap mEtcMap;
	private int mModifyMode = 0;
	private Boolean mMemoMode = false;
	private Spinner mSpAlarmType, mSpAppList, mSpDateType;
	private Button mBtnAddAlarm = null;
	private EditText mTxAlarmTitle;
	private Context mCtx = null;
	private TimePicker mAddAlarmTimePicker;
	private NumberPicker mNpHour, mNpMinute, mNpSecond;
	private ScrollView mScvAddAlarm;
	private int mAlarmOption = Const.ALARM_OPTION.SET_DATE_TIMER;
	private int mAlarmDateType = Const.ALARM_DATE_TYPE.REPEAT; //날짜지정 or repeat
	private TextView mTvAlarmDate, mTvAlarmTime = null;

	private RadioGroup mRgAlarmOption;
	private LinearLayout mAlarmList, llTimerWrap, llDateTypeWrap, llDatePicWrap, llTimePickWrap, llRepeatDayWrap, llAlertTimeWrap;
	private HashMap<Integer, Button> mMapDay = new HashMap<>();
	private int[] mArrDayString = {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY};//{"mon", "tue", "wed", "thu", "fri", "sat", "sun"};
	private int[] mArrDayId = {R.id.btnRepeatSun, R.id.btnRepeatMon, R.id.btnRepeatTue, R.id.btnRepeatWed, R.id.btnRepeatThur, R.id.btnRepeatFri, R.id.btnRepeatSat};
	private ArrayList<Integer> mDataRepeatDay = new ArrayList<>();
	private ArrayList<Integer> mArrAlarmCall = new ArrayList<Integer>();
	private String mEtcType = "";
	private Object mTemp;
	private Calendar mCalendar;

	//private String[] mArrDayBtn = {"btnRepeatMon", "btnRepeatThue", "btnRepeat"}

	public AlarmDialogNew() {
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mCtx = getActivity();

		Bundle arguments = getArguments();

		if(arguments != null) {
			mAlarmVO = (AlarmVO) arguments.getSerializable(Const.ALARM_VO);

			if(mAlarmVO != null)
				mModifyMode = 1;

			mMemoVO = (MemoVO) arguments.getSerializable(Const.MEMO_VO);

			if(mMemoVO != null)
				mMemoMode = true;

		}
		if(mAlarmVO == null){
			mAlarmVO = new AlarmVO();
		}
		mEtcMap = new LinkedHashMap();

		mEtcMap.put(Const.ETC_TYPE.NONE, "없음");
		mEtcMap.put(Const.ETC_TYPE.WEATHER, "날씨");
		mEtcMap.put(Const.ETC_TYPE.MEMO, "메모");

//		arraylist.add("날씨");
//		arraylist.add("뉴스 구독");
//		arraylist.add("메모");
//		arraylist.add("앱 단축아이콘");
//		arraylist.add("링크");

		makeSpinnerDateType();
		makeSpinnerAlarmType();
		makeSpinnerAppList();
		bindEvent();
		init();
		super.onActivityCreated(savedInstanceState);
	}

	private void init(){
		Toast.makeText(mCtx, "alarmId= "+ mAlarmVO.getId(), Toast.LENGTH_SHORT).show();

		/* alarmTitle, alarmType(진동,소리 등), alarmOption(타이머,시간지정), hour, minute, mArrAlarmCall(몇분전 알림 목록)
		 , mDataRepeatDay, mAlarmDateType, ArrayList<Calendar> alarmDate = null;
		*/
		mCalendar = Calendar.getInstance();

		if(mModifyMode == 1) {

			//타이머 , 트리거
			int alarmOption = mAlarmVO.getAlarmOption();
			mRgAlarmOption.check(alarmOption == Const.ALARM_OPTION.SET_DATE_TIMER ? R.id.rbSetTime : R.id.rbNoSetTime);

			//날짜 지정, 반복
			mAlarmDateType = mAlarmVO.getAlarmDateType();

			//if(mAlarmDateType == Const.ALARM_DATE_TYPE.REPEAT)
			mSpDateType.setSelection(mAlarmDateType, false);

			int alarmType = mAlarmVO.getAlarmType();
			mDataRepeatDay = mAlarmVO.getRepeatDay();

			ArrayList<Calendar> arrAlarmDate = mAlarmVO.getAlarmDateList();

			if(arrAlarmDate != null && arrAlarmDate.size() > 0)
				mCalendar = arrAlarmDate.get(0);

			renderDateTypeUi(mAlarmDateType, mCalendar);

			if(mAlarmDateType == Const.ALARM_DATE_TYPE.REPEAT) {
				int value = 0;
				View view;
				int index;

				Toast.makeText(mCtx, Arrays.toString(mDataRepeatDay.toArray()), Toast.LENGTH_LONG).show();
				ArrayList<Integer> temp = new ArrayList<>();
				for (int i = 0; i < mDataRepeatDay.size(); i++) {
					value = mDataRepeatDay.get(i);
					index = Arrays.binarySearch(mArrDayString, value);
					//Toast.makeText(mCtx, index, Toast.LENGTH_SHORT).show();
					view = mMapDay.get(value);
					temp.add(index);
					toggleBtnRepeatDay(view, index);
				}

				Toast.makeText(mCtx, "ttt=" + temp.toString(), Toast.LENGTH_SHORT).show();
			}

			txTimeSet(mAlarmVO.getHour(), mAlarmVO.getMinute());
			mTxAlarmTitle.setText(mAlarmVO.getAlarmTitle());

			mSpAlarmType.setSelection(alarmType);

			ArrayList<Integer> arrAlarmCall = mAlarmVO.getAlarmCallList();
			int temp;

			for(int i = 0; i < arrAlarmCall.size(); i++){
				temp = arrAlarmCall.get(i);
				appendAlarmRow(Math.abs(temp), (temp < 0 ? -1 : 1));
			}

			mEtcType = mAlarmVO.getType();

			restoreEtcType();

		}
		else {
			txTimeSet(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
			appendAlarmRow(0, 1);
		}

		if(mMemoMode)
			initMemoMode();

	}

	private void restoreEtcType(){
		Object[] arrkeys = mEtcMap.keySet().toArray();
		for(int i = 0 ; i < arrkeys.length; i++){
			if(arrkeys[i].equals(mEtcType)){
				mSpAppList.setSelection(i);
				break;
			}
		}
	}

	private void initMemoMode(){
		mEtcType = Const.ETC_TYPE.MEMO;
		restoreEtcType();
		mSpAppList.setEnabled(false);
		mTxAlarmTitle.setText(mMemoVO.getTitle());
	}

	private void returnData(){

		/**
		 *
		 * alarmTitle, alarmType(진동,소리 등), alarmOption(타이머,시간지정), hour, minute, mArrAlarmCall(몇분전 알림 목록)
		 * , mDataRepeatDay, mAlarmDateType, ArrayList<Calendar> alarmDate = null;
		 */

		String alarmTitle = mTxAlarmTitle.getText().toString();

		if(alarmTitle.equals("")){
			alarmTitle = getString(R.string.alarm_no_title);
		}


		ArrayList<Calendar> alarmDate = new ArrayList<Calendar>();

		// 반복이 아닐 경우 날짜 지정 데이터 삽입
		if(mAlarmDateType == Const.ALARM_DATE_TYPE.REPEAT){
			if(mDataRepeatDay.size() == 0){
				Toast.makeText(mCtx, "반복할 요일을 선택하세요", Toast.LENGTH_LONG);
				return ;
			}

			Object[] aa = mDataRepeatDay.toArray();
			Arrays.sort(aa);

			alarmDate = null;
		}
		else{
			int year = (int) mTvAlarmDate.getTag(R.id.timeYearId);
			int month = (int) mTvAlarmDate.getTag(R.id.timeMonthId);
			int day = (int) mTvAlarmDate.getTag(R.id.timeDayId);
			Calendar c = Calendar.getInstance();
			c.set(year, month-1, day);
			alarmDate.add(c);
		}


		int hour, minute;

		hour = (int) mTvAlarmTime.getTag(R.id.timeHourId);
		minute = (int) mTvAlarmTime.getTag(R.id.timeMinuteId);

		//alarmOption에 따라 time 가져오는게 다름 타이머 일 경우 arrAlarmCall에 초단위로 변환해서 삽입.
		if(mAlarmOption == Const.ALARM_OPTION.NO_DATE_TIMER) {
			mArrAlarmCall.clear();

			int m = mNpHour.getValue() * 60 + mNpMinute.getValue();
			m = m * 60 + mNpSecond.getValue();
			mArrAlarmCall.add(m);
		}

		//알리는 방법
		int alarmType = mSpAlarmType.getSelectedItemPosition();

		//mArrAlarmCall

		int etcType = mSpAppList.getSelectedItemPosition();

//alarmTitle, alarmType(진동,소리 등), alarmOption(타이머,시간지정), hour, minute, mArrAlarmCall(몇분전 알림 목록), mDataRepeatDay, mAlarmDateType

		AlarmVO vo;

		if(mModifyMode == 1)
			vo = mAlarmVO;
		else
			vo = new AlarmVO();

		vo.setAlarmTitle(alarmTitle);
		vo.setAlarmType(alarmType);
		vo.setAlarmOption(mAlarmOption);
		vo.setHour(hour);
		vo.setMinute(minute);
		vo.setAlarmCallList(mArrAlarmCall);
		vo.setRepeatDay(mDataRepeatDay);
		vo.setAlarmDateType(mAlarmDateType);
		vo.setAlarmDateList(alarmDate);
		vo.setType(mEtcType);

		Bundle bundle = new Bundle();
		bundle.putSerializable("alarmVO", vo);
		Intent intent = new Intent();
		intent.putExtras(bundle);

		int returnCode = mModifyMode == 1 ? Const.ALARM_INTERFACE_CODE.ADD_ALARM_MODIFY_FINISH_CODE : Const.ALARM_INTERFACE_CODE.ADD_ALARM_FINISH_CODE;
		getTargetFragment().onActivityResult(getTargetRequestCode(), returnCode, intent);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder b=  new  AlertDialog.Builder(getActivity())
				.setTitle("알림 추가")
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								returnData();
							}
						}
				)
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.dismiss();
							}
						}
				);

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_dialog_alarm_add, null);

		mSpAlarmType = (Spinner) view.findViewById(R.id.spAlarmType);
		mBtnAddAlarm = (Button) view.findViewById(R.id.btnAddAlarm);
		mAlarmList = (LinearLayout) view.findViewById(R.id.alarmList);
		mSpAppList = (Spinner) view.findViewById(R.id.spAppList);
		mSpDateType = (Spinner) view.findViewById(R.id.spDateType);
		mTxAlarmTitle = (EditText) view.findViewById(R.id.txAlarmTitle);
		//mAddAlarmTimePicker = (TimePicker) view.findViewById(R.id.addAlarmTimePicker);
		mRgAlarmOption = (RadioGroup) view.findViewById(R.id.rgAlarmOption);

		mNpHour = (NumberPicker) view.findViewById(R.id.addAlarmHourPicker);
		mNpMinute = (NumberPicker) view.findViewById(R.id.addAlarmMinutePicker);
		mNpSecond = (NumberPicker) view.findViewById(R.id.addAlarmSecondPicker);

		mTvAlarmDate = (TextView) view.findViewById(R.id.tvAlarmDate);
		mTvAlarmTime = (TextView) view.findViewById(R.id.tvAlarmTime);

		//mTimePickerWrap = (LinearLayout) view.findViewById(R.id.timePickerWrap);

		bindVarLayoutView(view);

		mScvAddAlarm = (ScrollView) view.findViewById(R.id.scvAddAlarm);

		for(int i = 0; i < mArrDayId.length; i++) {
			mMapDay.put(mArrDayString[i], (Button) view.findViewById(mArrDayId[i]));
		}

		b.setView(view);

		return b.create();

	}

	private void bindVarLayoutView(View v) {
		llTimerWrap = (LinearLayout) v.findViewById(R.id.llTimerWrap);
		llDateTypeWrap = (LinearLayout) v.findViewById(R.id.llDateTypeWrap);
		llDatePicWrap = (LinearLayout) v.findViewById(R.id.llDatePicWrap);
		llTimePickWrap = (LinearLayout) v.findViewById(R.id.llTimePickWrap);
		llRepeatDayWrap = (LinearLayout) v.findViewById(R.id.llRepeatDayWrap);
		llAlertTimeWrap = (LinearLayout) v.findViewById(R.id.alertTimeWrap);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void makeSpinnerDateType(){
		//mSpDateType;
		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.add("반복");
		arrayList.add("날짜 지정");
		arrayList.add("내일");
		arrayList.add("모레");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mCtx,
				android.R.layout.simple_spinner_item, arrayList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		//스피너 속성
		//mSpDateType.setPrompt(""); // 스피너 제목
		mSpDateType.setAdapter(adapter);
		mSpDateType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				renderDateTypeUi(position, mCalendar);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}


	private void timeWrapperHide() {
		View[] arrView = {llTimerWrap, llDateTypeWrap, llDatePicWrap, llTimePickWrap, llRepeatDayWrap, llAlertTimeWrap};
		for(int i = 0 ; i < arrView.length; i++) {
			arrView[i].setVisibility(View.GONE);
		}
	}

	// 시간 미지정 알람
	private void renderNoTimerUi(){
		mAlarmOption = Const.ALARM_OPTION.NO_DATE_TIMER;

		//모두 숨김
		timeWrapperHide();

		llTimePickWrap.setVisibility(View.VISIBLE);
		llTimerWrap.setVisibility(View.VISIBLE);
		llRepeatDayWrap.setVisibility(View.VISIBLE);
		mNpHour.setMaxValue(10);
		mNpHour.setMinValue(0);
		mNpMinute.setMaxValue(59);
		mNpMinute.setMinValue(0);
		mNpSecond.setMaxValue(59);
		mNpSecond.setMinValue(0);
	}

	//시간 지정 알람
	private void renderSetTimeUi(){
		//timeWrapperHide();
		llTimerWrap.setVisibility(View.GONE);
		llDatePicWrap.setVisibility(View.VISIBLE);
		llTimePickWrap.setVisibility(View.VISIBLE);
		llRepeatDayWrap.setVisibility(View.VISIBLE);
		llDateTypeWrap.setVisibility(View.VISIBLE);
		llAlertTimeWrap.setVisibility(View.VISIBLE);
		mAlarmOption = Const.ALARM_OPTION.SET_DATE_TIMER;
		//mAddAlarmTimePicker = new TimePicker(mCtx);
	}

	private void renderDateTypeUi(int alarmDateType) {
		Calendar c = Calendar.getInstance();
		renderDateTypeUi(alarmDateType, c);

	}

	//날짜 선택 - spinner 선택에 따른
	private void renderDateTypeUi(int alarmDateType, Calendar c) {
//		llTimerWrap = (LinearLayout) getView().findViewById(R.id.llTimerWrap);
//		llDateTypeWrap = (LinearLayout) getView().findViewById(R.id.llDateTypeWrap);
//		llDatePicWrap = (LinearLayout) getView().findViewById(R.id.llDatePicWrap);
//		llTimePickWrap = (LinearLayout) getView().findViewById(R.id.llTimePickWrap);
//		llRepeatDayWrap = (LinearLayout) getView().findViewById(R.id.llRepeatDayWrap);
		llTimePickWrap.setVisibility(View.VISIBLE);

		switch(alarmDateType){
			case Const.ALARM_DATE_TYPE.REPEAT :
				llRepeatDayWrap.setVisibility(View.VISIBLE);
				llDatePicWrap.setVisibility(View.GONE);
				mAlarmDateType = Const.ALARM_DATE_TYPE.REPEAT;
				break;
			case Const.ALARM_DATE_TYPE.SET_DATE :
				llDatePicWrap.setVisibility(View.VISIBLE);
				llRepeatDayWrap.setVisibility(View.GONE);
				alarmDateChange(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
				mAlarmDateType = Const.ALARM_DATE_TYPE.SET_DATE;
				break;
			case Const.ALARM_DATE_TYPE.TOMORROW :
				llDatePicWrap.setVisibility(View.VISIBLE);
				llRepeatDayWrap.setVisibility(View.GONE);
				c.add(Calendar.DAY_OF_MONTH, 1);
				alarmDateChange(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
				mAlarmDateType = Const.ALARM_DATE_TYPE.SET_DATE;
				break;
			case Const.ALARM_DATE_TYPE.AFTER_DAY_TOMORROW :
				llDatePicWrap.setVisibility(View.VISIBLE);
				llRepeatDayWrap.setVisibility(View.GONE);
				c = Calendar.getInstance();
				c.add(Calendar.DAY_OF_MONTH, 2);
				alarmDateChange(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
				mAlarmDateType = Const.ALARM_DATE_TYPE.SET_DATE;
				break;
		}


	}

	public void makeSpinnerAlarmType(){
		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.add("없음");
		arrayList.add("진동");
		arrayList.add("진동+소리");
		arrayList.add("무음");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, arrayList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		//스피너 속성
		mSpAlarmType.setPrompt("알람 종류"); // 스피너 제목
		mSpAlarmType.setAdapter(adapter);
		mSpAlarmType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	public void makeSpinnerAppList(){
		//mSpAppList
		ArrayList<String> arraylist = new ArrayList<String>();

		for (Object key : mEtcMap.keySet()) {
			arraylist.add((String) mEtcMap.get(key));
		}

		final Object[] values = mEtcMap.keySet().toArray();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, arraylist);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//스피너 속성
		mSpAppList.setPrompt("알람 종류"); // 스피너 제목
		mSpAppList.setAdapter(adapter);
		mSpAppList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mEtcType = (String) values[position];
				Toast.makeText(mCtx, "mEtcType = "+mEtcType, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	public void bindEvent(){
		mBtnAddAlarm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				makeBeforeTimer();
			}

		});

		for(int i = 0; i < mArrDayString.length; i++){
			mMapDay.get(mArrDayString[i]).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//mDataRepeatDay.contains(mArrDayString[i]);
					int index = Arrays.binarySearch(mArrDayId, (int)v.getId());
					int index2 = mDataRepeatDay.indexOf(mArrDayString[index]);
					toggleBtnRepeatDay(v, index, index2, true);
				}
			});
		}

		mRgAlarmOption.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				//renderNoTimerUi(checkedId);
				//mTimePickerWrap.removeAllViews();

				if (checkedId == R.id.rbSetTime)
					//SET_DATE_TIMER
					renderSetTimeUi();

				else {
					//NO_DATE_TIMER
					renderNoTimerUi();
				}
			}
		});

		mTvAlarmDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar calendar = Calendar.getInstance();
				int myYear = calendar.get(Calendar.YEAR);
				int myMonth = calendar.get(Calendar.MONTH);
				int myDay = calendar.get(Calendar.DAY_OF_MONTH);

				//new DatePickerDialog(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog), new DatePickerDialog.OnDateSetListener() {
				new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						alarmDateChange(year, monthOfYear + 1, dayOfMonth);
					}
				}, myYear, myMonth, myDay).show();
			}
		});

		mTvAlarmTime.setOnClickListener(new View.OnClickListener() {
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);

			@Override
			public void onClick(View v) {
				TimePickerDialog timePickerDialog = new TimePickerDialog(mCtx, new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						txTimeSet(hourOfDay, minute);
					}
				}, hour, minute, false);
				timePickerDialog.show();
			}
		});
	}

	private void txTimeSet(int hourOfDay, int minute){

		boolean isPm = false;
		if (hourOfDay >= 12)
			isPm = true;
		int time = hourOfDay % 12;
		if (isPm && time == 0)
			time = 12;

		mTvAlarmTime.setTag(R.id.timeHourId, hourOfDay);
		mTvAlarmTime.setTag(R.id.timeMinuteId, minute);

		mTvAlarmTime.setText((isPm ? "PM " : "AM ") + time + ":" + minute);
	}

	private void alarmDateChange(int year, int monthOfYear, int dayOfMonth) {
		mTvAlarmDate.setTag(R.id.timeYearId, year);
		mTvAlarmDate.setTag(R.id.timeMonthId, monthOfYear);
		mTvAlarmDate.setTag(R.id.timeDayId, dayOfMonth);
		mTvAlarmDate.setText(year + "-" + monthOfYear + "-" + dayOfMonth);
	}

	private void toggleBtnRepeatDay(View v, int index){
		toggleBtnRepeatDay(v, index, -1, false);
	}
	//index2 가 -1 이상이면 비활성, toggle 이 true 면 데이터 추가 삭제 동작
	private void toggleBtnRepeatDay(View v, int index, int index2, boolean isToggle){
		if(index2 > -1) {
			if(isToggle)
				mDataRepeatDay.remove(index2);
			v.setBackgroundResource(R.color.background);
		}
		else{
			if(isToggle)
				mDataRepeatDay.add(mArrDayString[index]);
			v.setBackgroundResource(R.color.blue_semi_transparent_pressed);
		}
	}

	public void makeBeforeTimer(){
		AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
		builder.setTitle("알림 시간 추가");

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout ll = new LinearLayout(mCtx);
		ll.setLayoutParams(params2);
		ll.setOrientation(LinearLayout.HORIZONTAL);

		final NumberPicker np = new NumberPicker(mCtx);
		ll.setGravity(Gravity.CENTER);
		np.setMinValue(0);
		np.setMaxValue(59);
		np.setValue(5);
		np.setWrapSelectorWheel(true);
		params.gravity = Gravity.CENTER;
		np.setLayoutParams(params);

		ll.addView(np);

		Button btn = new Button(mCtx);
		btn.setText("분 이전");
		params.gravity = Gravity.CENTER_VERTICAL;
		btn.setLayoutParams(params);
		mTemp = btn;
		btn.setTag(-1);
		btn.setOnClickListener(new View.OnClickListener() {
			int toggle = 0;

			@Override
			public void onClick(View v) {

				if (toggle == 0) {
					v.setTag(1);
					toggle = 1;
					((Button) v).setText("분 이후");
				} else {
					v.setTag(-1);
					toggle = 0;
					((Button) v).setText("분 이전");
				}
			}
		});

		ll.addView(btn);

		builder.setView(ll);

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				d(Const.DEBUG_TAG, "getTag = " + ((Button) mTemp).getTag());
				int aa = Integer.parseInt(((Button) mTemp).getTag().toString());
				appendAlarmRow(np.getValue(), aa);
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
			}
		});

// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void appendAlarmRow(int val, int flag) {
		if(mArrAlarmCall.contains(val*flag)) {
			Toast.makeText(mCtx, "중복되는 알림이 있습니다", Toast.LENGTH_LONG).show();
			return ;
		}

		mArrAlarmCall.add(val * flag);

		LinearLayout ll = new LinearLayout(mCtx);
		ll.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		ll.setLayoutParams(params);

		TextView tv = new TextView(mCtx);
		tv.setLayoutParams(params2);
		if(flag == -1)
			tv.setText(val + " 분 전");
		else
			tv.setText(val + " 분 후");

		ll.addView(tv);

		Button bt = new Button(mCtx);
		bt.setText("-");
		bt.setTag(val * flag);
		bt.setLayoutParams(params2);
		bt.setPadding(2, 2, 2, 2);
		bt.setBackgroundColor(Color.TRANSPARENT);
		bt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int index = mArrAlarmCall.indexOf((Integer) v.getTag());
				d(Const.DEBUG_TAG, "removeIndex = " + index);

				mArrAlarmCall.remove(index);
				((ViewGroup) v.getParent()).removeAllViews();
			}
		});

		ll.addView(bt);

		mAlarmList.addView(ll);

		mScvAddAlarm.refreshDrawableState();

	}
}
