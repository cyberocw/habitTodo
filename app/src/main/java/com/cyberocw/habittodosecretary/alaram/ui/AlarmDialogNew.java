package com.cyberocw.habittodosecretary.alaram.ui;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.category.CategoryFragment;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import butterknife.ButterKnife;

import static android.util.Log.d;

/**
 * Created by cyberocw on 2015-08-19.
 * data colum 관련
 * alarm_title - 제목
 * alarm_date_type - 반복 0, 날짜 지정 1
 * alarm_type - 1회 알림 계속 알림
  * alarm_option - tts 여부 -- 나중에 컬럼 명 변경하기
 * alaram_contents - 안씀
 * type - 안씀
 *
 *
 */

public class AlarmDialogNew extends DialogFragment{
	private View mView;
	private Dialog mDialog;
	private AlarmVO mAlarmVO = null;
	private MemoVO mMemoVO = null;
	private LinkedHashMap mEtcMap;
	private int mModifyMode = 0;
	private Boolean mIsInitMemoMode = false;
	private Spinner mSpAlarmType, mSpAppList, mSpDateType;
	private Button mBtnAddAlarm = null, mBtnClose, mBtnSave;
	private EditText mTxAlarmTitle;
	private Context mCtx = null;
	private TimePicker mAddAlarmTimePicker;
	private NumberPicker mNpHour, mNpMinute, mNpSecond;
	private ScrollView mScvAddAlarm;
	private int mAlarmOption = 1;
	private int mAlarmDateType = Const.ALARM_DATE_TYPE.SET_DATE; //날짜지정 or repeat
	private TextView mTvAlarmDate, mTvAlarmTime = null, mTvEtcTitle;
	private CheckBox mCbHolidayAll = null;
	private CheckBox mCbHolidayNone = null;
	private CheckBox mCbTTS = null;


	//private RadioGroup mRgAlarmOption;
	private LinearLayout mAlarmList, llTimerWrap, llDateTypeWrap, llDatePicWrap, llTimePickWrap, llRepeatDayWrap, llAlertTimeWrap, llHolidayOptionWrap;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		Crashlytics.log(Log.DEBUG, this.toString(), " onCreateView ");
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_dialog_alarm_add, container, false);
		mView = view;

		//mBtnClose = ButterKnife.findById(mView, R.id.btnClose);
		mBtnSave = ButterKnife.findById(mView, R.id.btnSave);

		mSpAlarmType = (Spinner) view.findViewById(R.id.spAlarmType);
		mBtnAddAlarm = (Button) view.findViewById(R.id.btnAddAlarm);
		mAlarmList = (LinearLayout) view.findViewById(R.id.alarmList);
		mSpAppList = (Spinner) view.findViewById(R.id.spAppList);
		mSpDateType = (Spinner) view.findViewById(R.id.spDateType);
		mTxAlarmTitle = (EditText) view.findViewById(R.id.txAlarmTitle);
		//mAddAlarmTimePicker = (TimePicker) view.findViewById(R.id.addAlarmTimePicker);
		//mRgAlarmOption = (RadioGroup) view.findViewById(R.id.rgAlarmOption);

		mNpHour = (NumberPicker) view.findViewById(R.id.addAlarmHourPicker);
		mNpMinute = (NumberPicker) view.findViewById(R.id.addAlarmMinutePicker);
		mNpSecond = (NumberPicker) view.findViewById(R.id.addAlarmSecondPicker);

		mTvAlarmDate = (TextView) view.findViewById(R.id.tvAlarmDate);
		mTvAlarmTime = (TextView) view.findViewById(R.id.tvAlarmTime);

		mCbHolidayAll = (CheckBox) view.findViewById(R.id.cbHolidayAll);
		mCbHolidayNone = (CheckBox) view.findViewById(R.id.cbHolidayNone);

		//mTimePickerWrap = (LinearLayout) view.findViewById(R.id.timePickerWrap);

		bindVarLayoutView(view);

		mScvAddAlarm = (ScrollView) view.findViewById(R.id.scvAddAlarm);

		mCbTTS = (CheckBox) view.findViewById(R.id.cbTTS);

		mTvEtcTitle = (TextView) view.findViewById(R.id.etcTitle);

		for(int i = 0; i < mArrDayId.length; i++) {
			mMapDay.put(mArrDayString[i], (Button) view.findViewById(mArrDayId[i]));
		}
		Toolbar toolbar = (Toolbar) mView.findViewById(R.id.toolbar);
		toolbar.setVisibility(View.VISIBLE);
		toolbar.setTitle(getResources().getString(R.string.dialog_title_alaram));
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});
		return mView;
	}
/*
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Crashlytics.log(Log.DEBUG, this.toString(), " onCreateDialog ");

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
		mView = view;


		b.setView(view);
		Dialog dialog = b.create();
		dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		mDialog = dialog;


		return dialog;

	}*/

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Crashlytics.log(Log.DEBUG, this.toString(), " onActivityCreated ");

		if(savedInstanceState != null){
		//	return;
		}

		mCtx = getActivity();
		Bundle arguments = getArguments();

		if(arguments != null) {
			mAlarmVO = (AlarmVO) arguments.getSerializable(Const.PARAM.ALARM_VO);

			if(mAlarmVO != null)
				mModifyMode = 1;

            if(arguments.containsKey(Const.PARAM.MEMO_VO))
			    mMemoVO = (MemoVO) arguments.getSerializable(Const.PARAM.MEMO_VO);

			if(arguments.getBoolean(Const.MEMO.IS_INIT_MEMO_MODE, false))
				mIsInitMemoMode = true;

		}
		if(mAlarmVO == null){
			mAlarmVO = new AlarmVO();
		}
		mEtcMap = new LinkedHashMap();

		mEtcMap.put(Const.ETC_TYPE.NONE, getString(R.string.none));
		//mEtcMap.put(Const.ETC_TYPE.WEATHER, "날씨");
		mEtcMap.put(Const.ETC_TYPE.MEMO, getString(R.string.memoGroupTitle));

//		arraylist.add("날씨");
//		arraylist.add("뉴스 구독");
//		arraylist.add("메모");
//		arraylist.add("앱 단축아이콘");
//		arraylist.add("링크");

		makeSpinnerDateType();
		makeSpinnerAlarmType();
		makeSpinnerAppList();
		init();
		bindEvent();

		if(arguments != null && arguments.getInt(Const.PARAM.MODE) == Const.ALARM_INTERFACE_CODE.ALARM_POSTPONE_DIALOG){
			//makeAlarmPostpone();
		}

		CommonUtils.setupUI(mView, getActivity());

		super.onActivityCreated(savedInstanceState);
	}

	private void init(){
		/* alarmTitle, alarmType(진동,소리 등), alarmOption(타이머,시간지정), hour, minute, mArrAlarmCall(몇분전 알림 목록)
		 , mDataRepeatDay, mAlarmDateType, ArrayList<Calendar> alarmDate = null;
		*/
		mCalendar = Calendar.getInstance();

		if(mModifyMode == 1) {

			//타이머 , 트리거
			int alarmOption = mAlarmVO.getAlarmOption();
			//mRgAlarmOption.check(alarmOption == Const.ALARM_OPTION.SET_DATE_TIMER ? R.id.rbSetTime : R.id.rbNoSetTime);

			//날짜 지정, 반복
			mAlarmDateType = mAlarmVO.getAlarmDateType();

			//안쓸듯?
			if(mAlarmDateType == Const.ALARM_DATE_TYPE.POSTPONE_DATE)
				mAlarmDateType = Const.ALARM_DATE_TYPE.SET_DATE;

			mSpDateType.setSelection(Const.ALARM_DATE_TYPE.getPositionByCode(mAlarmDateType), false);

			int alarmType = mAlarmVO.getAlarmType();
			mDataRepeatDay = mAlarmVO.getRepeatDay();

			ArrayList<Calendar> arrAlarmDate = mAlarmVO.getAlarmDateList();

			if(arrAlarmDate != null && arrAlarmDate.size() > 0)
				mCalendar = arrAlarmDate.get(0);

			renderDateTypeUi(mAlarmDateType, mCalendar);

			if(mAlarmDateType == Const.ALARM_DATE_TYPE.REPEAT) {
				int value = 0;
				Button view;
				int index;

				ArrayList<Integer> temp = new ArrayList<>();
				for (int i = 0; i < mDataRepeatDay.size(); i++) {
					value = mDataRepeatDay.get(i);
					index = Arrays.binarySearch(mArrDayString, value);
					//Toast.makeText(mCtx, index, Toast.LENGTH_SHORT).show();
					view = mMapDay.get(value);
					temp.add(index);
					toggleBtnRepeatDay(view, index);
				}

				Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "mAlarmVO.getIsHolidayALL() " + mAlarmVO.getIsHolidayALL()  + " true = " + (mAlarmVO.getIsHolidayALL() == 1));
				if(mAlarmVO.getIsHolidayALL() == 1)
					mCbHolidayAll.setChecked(true);
				if(mAlarmVO.getIsHolidayNone() == 1)
					mCbHolidayNone.setChecked(true);

			}

			txTimeSet(mAlarmVO.getHour(), mAlarmVO.getMinute());
			mTxAlarmTitle.setText(mAlarmVO.getAlarmTitle());

			mSpAlarmType.setSelection(alarmType);

			mCbTTS.setChecked(mAlarmVO.getAlarmOption() == 1);

			ArrayList<Integer> arrAlarmCall = mAlarmVO.getAlarmCallList();
			int temp;

			for(int i = 0; i < arrAlarmCall.size(); i++){
				temp = arrAlarmCall.get(i);
				appendAlarmRow(Math.abs(temp), (temp < 0 ? -1 : 1));
			}

			mEtcType = mAlarmVO.getEtcType();
			restoreEtcType();

			if(mMemoVO != null) {
				mTvEtcTitle.setText(mMemoVO.getTitle());
				mTvEtcTitle.setVisibility(View.VISIBLE);
			}
		}
		else {
			txTimeSet(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
			appendAlarmRow(0, 1);
			mCbTTS.setChecked(true);
		}

		if(mIsInitMemoMode) {
			initMemoMode();
		}

	}

	private void restoreEtcType(){
		Object[] arrkeys = mEtcMap.keySet().toArray();
		for(int i = 0 ; i < arrkeys.length; i++){
			if(arrkeys[i].equals(mEtcType)){
				Crashlytics.log(Log.DEBUG, this.toString(), "selected start");
				mSpAppList.setTag(R.id.spAppList, i);
				mSpAppList.setSelection(i, false);

				break;
			}
		}
	}

	private void initMemoMode(){
		mEtcType = Const.ETC_TYPE.MEMO;
		restoreEtcType();
		mSpAppList.setEnabled(false);
		if(mAlarmVO.getAlarmTitle() == null)
			mTxAlarmTitle.setText(mMemoVO.getTitle());
	}

	private void returnData(){
		String alarmTitle = mTxAlarmTitle.getText().toString();

		if(alarmTitle.equals("")){
			alarmTitle = getString(R.string.alarm_no_title);
		}


		ArrayList<Calendar> alarmDate = new ArrayList<Calendar>();

		// 반복이 아닐 경우 날짜 지정 데이터 삽입
		if(mAlarmDateType == Const.ALARM_DATE_TYPE.REPEAT){
			if(mDataRepeatDay.size() == 0){
				Toast.makeText(mCtx, "반복할 요일을 선택하세요", Toast.LENGTH_LONG).show();
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

		if(mEtcType.equals(Const.ETC_TYPE.MEMO) && mMemoVO == null){
			Toast.makeText(mCtx, "메모를 선택해 주세요", Toast.LENGTH_LONG).show();
			return;
		}

		int hour, minute;

		hour = (int) mTvAlarmTime.getTag(R.id.timeHourId);
		minute = (int) mTvAlarmTime.getTag(R.id.timeMinuteId);

		//alarmOption에 따라 time 가져오는게 다름 타이머 일 경우 arrAlarmCall에 초단위로 변환해서 삽입.
		/*
		if(mAlarmOption == Const.ALARM_OPTION.NO_DATE_TIMER) {
			mArrAlarmCall.clear();

			int m = mNpHour.getValue() * 60 + mNpMinute.getValue();
			m = m * 60 + mNpSecond.getValue();
			mArrAlarmCall.add(m);
		}
		*/
		//알리는 방법
		int alarmType = mSpAlarmType.getSelectedItemPosition();

		//mArrAlarmCall

		//int etcType = mSpAppList.getSelectedItemPosition();

		AlarmVO vo;

		if(mModifyMode == 1)
			vo = mAlarmVO;
		else
			vo = new AlarmVO();

		vo.setAlarmTitle(alarmTitle);
		vo.setAlarmType(alarmType);
		vo.setAlarmOption(mCbTTS.isChecked() == true ? 1 : 0);
		vo.setHour(hour);
		vo.setMinute(minute);
		vo.setAlarmCallList(mArrAlarmCall);
		vo.setRepeatDay(mDataRepeatDay);
		vo.setAlarmDateType(mAlarmDateType);
		vo.setAlarmDateList(alarmDate);
		vo.setEtcType(mEtcType);



		if(mAlarmDateType == Const.ALARM_DATE_TYPE.REPEAT || mAlarmDateType == Const.ALARM_DATE_TYPE.REPEAT_MONTH){
			Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "mCbHolidayAll.isChecked() =" + mCbHolidayAll.isChecked());
			vo.setIsHolidayALL(mCbHolidayAll.isChecked() ? 1 : 0);
			vo.setIsHolidayNone(mCbHolidayNone.isChecked() ? 1 : 0);
		}

		Bundle bundle = new Bundle();
		bundle.putSerializable(Const.PARAM.ALARM_VO, vo);

        if(mMemoVO != null)
        	vo.setRfid(mMemoVO.getId());
            //bundle.putSerializable(Const.MEMO_VO, mMemoVO);
		Intent intent = new Intent();
		intent.putExtras(bundle);


		int returnCode = mModifyMode == 1 ? Const.ALARM_INTERFACE_CODE.ADD_ALARM_MODIFY_FINISH_CODE : Const.ALARM_INTERFACE_CODE.ADD_ALARM_FINISH_CODE;
		getTargetFragment().onActivityResult(getTargetRequestCode(), returnCode, intent);

		getActivity().getSupportFragmentManager().popBackStackImmediate();

	}


	private void bindVarLayoutView(View v) {
		llTimerWrap = (LinearLayout) v.findViewById(R.id.llTimerWrap);
		llDateTypeWrap = (LinearLayout) v.findViewById(R.id.llDateTypeWrap);
		llDatePicWrap = (LinearLayout) v.findViewById(R.id.llDatePicWrap);
		llTimePickWrap = (LinearLayout) v.findViewById(R.id.llTimePickWrap);
		llRepeatDayWrap = (LinearLayout) v.findViewById(R.id.llRepeatDayWrap);
		llAlertTimeWrap = (LinearLayout) v.findViewById(R.id.alertTimeWrap);
		llHolidayOptionWrap = (LinearLayout) v.findViewById(R.id.holidayOptionWrap);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void makeSpinnerDateType(){

		//mSpDateType;
		ArrayList<String> arrayList = new ArrayList<String>();
		Integer[] arrDateTypeTitle = Const.ALARM_DATE_TYPE.getTextList();
		//String[] arrDateTypeTitle = Const.ALARM_DATE_TYPE.getTextList();
		for(int i = 0; i < arrDateTypeTitle.length; i++){
			arrayList.add(getString(arrDateTypeTitle[i]));
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mCtx,
				R.layout.simple_spinner_item_small, arrayList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		//스피너 속성
		//mSpDateType.setPrompt(""); // 스피너 제목
		mSpDateType.setPrompt("알림 옵션");
		mSpDateType.setAdapter(adapter);
		mSpDateType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				int code = Const.ALARM_DATE_TYPE.getNumByPosition(position);

				renderDateTypeUi(code, mCalendar);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		mSpDateType.setSelection(2);
	}


	private void timeWrapperHide() {
		View[] arrView = {llTimerWrap, llDateTypeWrap, llDatePicWrap, llTimePickWrap, llRepeatDayWrap, llAlertTimeWrap, llHolidayOptionWrap};
		for(int i = 0 ; i < arrView.length; i++) {
			arrView[i].setVisibility(View.GONE);
		}
	}

	/*
	// 시간 미지정 알람
	private void renderNoTimerUi(){
		mAlarmOption = Const.ALARM_OPTION.NO_DATE_TIMER;

		//모두 숨김
		timeWrapperHide();

		llTimePickWrap.setVisibility(View.VISIBLE);
		llTimerWrap.setVisibility(View.VISIBLE);
		llRepeatDayWrap.setVisibility(View.VISIBLE);
		llHolidayOptionWrap.setVisibility(View.VISIBLE);
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
		llAlertTimeWrap.setVisibility(View.VISIBLE);
		llHolidayOptionWrap.setVisibility(View.GONE);
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
	*/

	//날짜 선택 - spinner 선택에 따른 - 내일 , 모레,
	private void renderDateTypeUi(int alarmDateType, Calendar c) {
//		llTimerWrap = (LinearLayout) getView().findViewById(R.id.llTimerWrap);
//		llDateTypeWrap = (LinearLayout) getView().findViewById(R.id.llDateTypeWrap);
//		llDatePicWrap = (LinearLayout) getView().findViewById(R.id.llDatePicWrap);
//		llTimePickWrap = (LinearLayout) getView().findViewById(R.id.llTimePickWrap);
//		llRepeatDayWrap = (LinearLayout) getView().findViewById(R.id.llRepeatDayWrap);
		llTimePickWrap.setVisibility(View.VISIBLE);

		mAlarmDateType = alarmDateType;

		if(c == null)
			c = Calendar.getInstance();

		switch(alarmDateType){
			case Const.ALARM_DATE_TYPE.REPEAT :
				llRepeatDayWrap.setVisibility(View.VISIBLE);
				llDatePicWrap.setVisibility(View.GONE);

				if(CommonUtils.isLocaleKo(getResources().getConfiguration()))
					llHolidayOptionWrap.setVisibility(View.VISIBLE);

				llAlertTimeWrap.setVisibility(View.VISIBLE);
				break;
			case Const.ALARM_DATE_TYPE.REPEAT_MONTH :
			case Const.ALARM_DATE_TYPE.SET_DATE :
				llDatePicWrap.setVisibility(View.VISIBLE);
				llRepeatDayWrap.setVisibility(View.GONE);
				llHolidayOptionWrap.setVisibility(View.GONE);
				llAlertTimeWrap.setVisibility(View.VISIBLE);
				alarmDateChange(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
				break;
			case Const.ALARM_DATE_TYPE.TOMORROW :
				llDatePicWrap.setVisibility(View.VISIBLE);
				llRepeatDayWrap.setVisibility(View.GONE);
				llAlertTimeWrap.setVisibility(View.VISIBLE);
				llHolidayOptionWrap.setVisibility(View.GONE);

				c.add(Calendar.DAY_OF_MONTH, 1);
				alarmDateChange(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
				mAlarmDateType = Const.ALARM_DATE_TYPE.SET_DATE;
				break;
			case Const.ALARM_DATE_TYPE.AFTER_DAY_TOMORROW :
				llDatePicWrap.setVisibility(View.VISIBLE);
				llRepeatDayWrap.setVisibility(View.GONE);
				llAlertTimeWrap.setVisibility(View.VISIBLE);
				llHolidayOptionWrap.setVisibility(View.GONE);
				c = Calendar.getInstance();
				c.add(Calendar.DAY_OF_MONTH, 2);
				alarmDateChange(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
				mAlarmDateType = Const.ALARM_DATE_TYPE.SET_DATE;
				break;
		}
	}

	public void makeSpinnerAlarmType(){
		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.add(getString(R.string.dialog_alarm_sp_notification));
		arrayList.add(getString(R.string.dialog_alarm_sp_user_stop));

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.simple_spinner_item_small, arrayList);
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

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.simple_spinner_item_small, arraylist);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//스피너 속성
		mSpAppList.setPrompt("알람 종류"); // 스피너 제목
		mSpAppList.setAdapter(adapter);
		mSpAppList.setEnabled(false);

	}

	private void showCategory(){
		Crashlytics.log(Log.DEBUG, this.toString(), "showCategory start");
		//mTvEtcTitle
		Fragment fragment = new CategoryFragment();

		Bundle args = new Bundle();
		args.putBoolean(Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_ETC_KEY, true);
		fragment.setArguments(args);

		fragment.setTargetFragment(this, Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_ETC_CODE);

		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.add(R.id.main_container, fragment).addToBackStack(null).commit();
	}

	public void bindEvent(){
		final Object[] values = mEtcMap.keySet().toArray();
		mSpAppList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mEtcType = (String) values[position];
				if(mEtcType.equals(Const.ETC_TYPE.MEMO)){
					Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, " onitem selected ");
					//if(mIsInitMemoMode == false)
					//Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "(int) mSpAppList.getTag(R.id.spAppList) = "+(int) mSpAppList.getTag(R.id.spAppList) + " posi = " +position);

					if(mSpAppList.getTag(R.id.spAppList) != null && (Integer) mSpAppList.getTag(R.id.spAppList) != position)
						showCategory();
				}else{
					mMemoVO = null;
					mTvEtcTitle.setVisibility(View.GONE);
				}
				mSpAppList.setTag(R.id.spAppList, position);

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, " onitem selected listener end ");

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
					int index = Arrays.binarySearch(mArrDayId, v.getId());
					int index2 = mDataRepeatDay.indexOf(mArrDayString[index]);

					//Toast.makeText(mCtx, index, Toast.LENGTH_SHORT).show();
					Button view = mMapDay.get(mArrDayString[index]);
					toggleBtnRepeatDay(view, index, index2, true);
				}
			});
		}

		mTvAlarmDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar calendar;
				if(mModifyMode == 1 && (mAlarmDateType == Const.ALARM_DATE_TYPE.SET_DATE || mAlarmDateType == Const.ALARM_DATE_TYPE.REPEAT_MONTH)){
					calendar = mAlarmVO.getAlarmDateList().get(0);
				}else{
					calendar = Calendar.getInstance();
				}

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

		mCbHolidayAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked && mCbHolidayNone.isChecked()){
					mCbHolidayNone.setChecked(false);
				}
			}
		});

		mCbHolidayNone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked && mCbHolidayAll.isChecked()){
					mCbHolidayAll.setChecked(false);
				}
			}
		});

		/*
		mBtnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});
		*/
		mBtnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				returnData();
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

	private void toggleBtnRepeatDay(Button v, int index){
		toggleBtnRepeatDay(v, index, -1, false);
	}
	//index2 가 -1 이상이면 비활성, toggle 이 true 면 데이터 추가 삭제 동작
	private void toggleBtnRepeatDay(Button v, int index, int index2, boolean isToggle){
		if(index2 > -1) {
			if (isToggle)
				mDataRepeatDay.remove(index2);

			v.setBackgroundResource(R.drawable.button_repeat_day);

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
				setTextAppearence(v, false);
			else
				setTextAppearenceNew(v, false);
			v.setBackgroundResource(R.drawable.button_repeat_day_unselect);
		}
		else{
			if(isToggle)
				mDataRepeatDay.add(mArrDayString[index]);
			//v.setBackgroundResource(R.color.primary_header);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
				setTextAppearence(v, true);
			else
				setTextAppearenceNew(v, true);
			//v.setBackgroundResource(R.drawable.button_repeat_day);
			v.setBackgroundResource(R.drawable.button_repeat_day );
		}
	}

	private void setTextAppearence(Button v, boolean isSelected){
		Crashlytics.log(Log.DEBUG, this.toString(), "isSelected = " + isSelected);

		if(isSelected)
			v.setTextAppearance(mCtx, R.style.button_repeat_day);
		else
			v.setTextAppearance(mCtx, R.style.button_repeat_day_unselected);
	}
	@TargetApi(Build.VERSION_CODES.M)
	private void setTextAppearenceNew(Button v, boolean isSelected){
		if(isSelected)
			v.setTextAppearance(R.style.button_repeat_day);
		else
			v.setTextAppearance(R.style.button_repeat_day_unselected);
	}
	public void makeBeforeTimer(){
		AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
		builder.setTitle(getString(R.string.dialog_alarm_preinform));

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		/*
		LinearLayout ll = new LinearLayout(mCtx);
		ll.setLayoutParams(params2);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		ll.setGravity(Gravity.CENTER);
		*/
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View container = inflater.inflate(R.layout.fragment_dialog_alram_before, null);




		final NumberPicker np = ButterKnife.findById(container, R.id.numberPicker);// new NumberPicker(mCtx);

		np.setMinValue(0);
		np.setMaxValue(59);
		np.setValue(5);
		np.setWrapSelectorWheel(true);
		/*
		params.gravity = Gravity.CENTER;
		np.setLayoutParams(params);
		*/
		//ll.addView(np);

		Button btn = ButterKnife.findById(container, R.id.button);//new Button(mCtx);
		btn.setText(getString(R.string.dialog_alarm_minute_before));
		//params.gravity = Gravity.CENTER_VERTICAL;
		//btn.setLayoutParams(params);

		mTemp = btn;
		btn.setTag(-1);
		btn.setOnClickListener(new View.OnClickListener() {
			int toggle = 0;

			@Override
			public void onClick(View v) {

				if (toggle == 0) {
					v.setTag(1);
					toggle = 1;
					((Button) v).setText(getString(R.string.dialog_alarm_minute_after));
				} else {
					v.setTag(-1);
					toggle = 0;
					((Button) v).setText(getString(R.string.dialog_alarm_minute_before));
				}
			}
		});

		builder.setView(container);

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

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View beforeView = inflater.inflate(R.layout.alaram_before, null);

		TextView tv = ButterKnife.findById(beforeView, R.id.tvBeforeTime);

		if(flag == -1)
			tv.setText(val + " " + getString(R.string.dialog_alarm_minute_before));
		else
			tv.setText(val + " " + getString(R.string.dialog_alarm_minute_after));

		ImageButton bt = ButterKnife.findById(beforeView, R.id.btnRemoveTime);
		//bt.setText("-");
		bt.setTag(val * flag);
		bt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int index = mArrAlarmCall.indexOf(v.getTag());

				mArrAlarmCall.remove(index);
				((ViewGroup) v.getParent().getParent()).removeView((ViewGroup) v.getParent());
			}
		});
		mAlarmList.addView(beforeView);

		mScvAddAlarm.refreshDrawableState();

	}

	/*public void setupUI(View view) {

		// Set up touch listener for non-text box views to hide keyboard.
		if (!(view instanceof EditText)) {
			view.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "on thuch");
					InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

					inputManager.hideSoftInputFromWindow(mDialog.getWindow().getCurrentFocus().getWindowToken(), 0);
					Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "hideSoftKeyboard");
					return false;
				}
			});
		}

		//If a layout container, iterate over children and seed recursion.
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				View innerView = ((ViewGroup) view).getChildAt(i);
				setupUI(innerView);
			}
		}
	}*/

	@Override
	public void onSaveInstanceState(Bundle outState) {
		//outState.put


		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
			case Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_ETC_CODE:
				MemoVO memoVO = (MemoVO) data.getExtras().getSerializable(Const.PARAM.MEMO_VO);
				Toast.makeText(mCtx, "memo title="+memoVO.getTitle(), Toast.LENGTH_LONG).show();
				mMemoVO = memoVO;
				mTvEtcTitle.setText(memoVO.getTitle());
				mTvEtcTitle.setVisibility(View.VISIBLE);
		}
	}
}
