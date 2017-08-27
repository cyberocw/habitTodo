package com.cyberocw.habittodosecretary.alaram.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.category.CategoryFragment;
import com.cyberocw.habittodosecretary.common.vo.FileVO;
import com.cyberocw.habittodosecretary.intro.Intro;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;
import com.cyberocw.habittodosecretary.record.RecorderCustomView;
import com.cyberocw.habittodosecretary.record.RecorderDataManager;
import com.cyberocw.habittodosecretary.util.CommonUtils;
import com.cyberocw.habittodosecretary.record.RecorderDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

import belka.us.androidtoggleswitch.widgets.ToggleSwitch;
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

public class AlarmDialogNew extends DialogFragment implements RecorderDialog.recordDialogInterface {
	private View mView;
	private Dialog mDialog;
	private AlarmVO mAlarmVO = null;
	private MemoVO mMemoVO = null;
	private LinkedHashMap mEtcMap;
	private int mModifyMode = 0;
	private Boolean mIsInitMemoMode = false;
	private Spinner mSpAlarmType, mSpAppList, mSpDateType, mSpSoundType;
	private Button mBtnAddAlarm = null, mBtnClose, mBtnSave, mBtnHelp;
	private EditText mTxAlarmTitle;
	private Context mCtx = null;
	private TimePicker mAddAlarmTimePicker;
	private NumberPicker mNpHour, mNpMinute, mNpSecond;
	private ScrollView mScvAddAlarm;
	private int mAlarmOption = 1;
	private int mAlarmDateType = Const.ALARM_DATE_TYPE.SET_DATE; //날짜지정 or repeat
	private TextView mTvAlarmDate, mTvAlarmTime = null;
    private Button mTvEtcTitle = null;
	private CheckBox mCbHolidayAll = null;
	private CheckBox mCbHolidayNone = null;
	private CheckBox mCbTTS = null;
	private boolean mPrevRecord = false;
    private RecorderCustomView mRecorderCustomView;
	private ToggleSwitch mToggleCallType = null;


	//private RadioGroup mRgAlarmOption;
	private LinearLayout mAlarmList, llTimerWrap, llDateTypeWrap, llDatePicWrap, llTimePickWrap, llRepeatDayWrap, llAlertTimeWrap, llHolidayOptionWrap, llRecorderWrap, llAlertTimeOptionWrap;
	private HashMap<Integer, Button> mMapDay = new HashMap<>();
	private int[] mArrDayString = {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY};//{"mon", "tue", "wed", "thu", "fri", "sat", "sun"};
	private int[] mArrDayId = {R.id.btnRepeatSun, R.id.btnRepeatMon, R.id.btnRepeatTue, R.id.btnRepeatWed, R.id.btnRepeatThur, R.id.btnRepeatFri, R.id.btnRepeatSat};
	private ArrayList<Integer> mDataRepeatDay = new ArrayList<>();
	private ArrayList<Integer> mArrAlarmCall = new ArrayList<Integer>();
	private String mEtcType = "";
	private Object mTemp;
	private Calendar mCalendar;
	private boolean isRecord;

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
		mBtnHelp = ButterKnife.findById(mView, R.id.btnHelp);
		mSpAlarmType = (Spinner) view.findViewById(R.id.spAlarmType);
		mToggleCallType = ButterKnife.findById(mView, R.id.toggleCallTimeOption);
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

		//mCbTTS = (CheckBox) view.findViewById(R.id.cbTTS);
		mSpSoundType = (Spinner) view.findViewById(R.id.spVoiceType);
        mRecorderCustomView = (RecorderCustomView) view.findViewById(R.id.recorderCustomView);

		mTvEtcTitle = (Button) view.findViewById(R.id.etcTitle);

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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		try {
			outState.putAll(getArguments());
		}catch (Exception e){

		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Crashlytics.log(Log.DEBUG, this.toString(), " onActivityCreated ");

		if(savedInstanceState != null){
		//	return;
		}

		mCtx = getActivity();

		Bundle arguments = null;

		if(getArguments() != null)
			arguments = getArguments();
		else if(savedInstanceState != null){
			arguments = savedInstanceState;
		}

		if(arguments != null) {
			mAlarmVO = (AlarmVO) arguments.getSerializable(Const.PARAM.ALARM_VO);

			if(mAlarmVO != null) {
				try {
					mAlarmVO = (AlarmVO) mAlarmVO.clone();
				}catch (CloneNotSupportedException e){
					e.printStackTrace();
				}
				mModifyMode = 1;
				if(mAlarmVO.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD){
					//mPrevRecord = true;
					//String fromPath = CommonUtils.getRecordFullPath(mCtx, mAlarmVO);
					ArrayList<FileVO> fileList = mAlarmVO.getFileList();
					if(fileList != null && fileList.size() > 0) {
						Log.d(this.toString(), "isModifyMode = 1 and mAlarmVO not null fromPath = " + fileList.get(0).getUriPath());
						mRecorderCustomView.setRecordFile(fileList.get(0).getUriPath());
					}
				}
			}
			/*
			if(mModifyMode == 1){
				if(mAlarmVO.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD){
					isRecord = true;
				}
			}*/

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
		makeSpinnerSoundType();
		makeSpinnerAppList();

		init();
		bindEvent();

		if(arguments != null && arguments.getInt(Const.PARAM.MODE) == Const.ALARM_INTERFACE_CODE.ALARM_POSTPONE_DIALOG){
			//makeAlarmPostpone();
		}

		CommonUtils.setupUI(mView, getActivity());

		CommonUtils.logCustomEvent("AlarmDialogNew", "1");

	}
	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 200:

				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

				} else {
					Toast.makeText(mCtx, "녹음 권한을 주지 않으면 녹음 알람 기능을 사용 할 수 없습니다", Toast.LENGTH_SHORT).show();
				}
				return;
		}
	}

	private void init(){
		/* alarmTitle, alarmType(진동,소리 등), alarmOption(타이머,시간지정), hour, minute, mArrAlarmCall(몇분전 알림 목록)
		 , mDataRepeatDay, mAlarmDateType, ArrayList<Calendar> alarmDate = null;
		*/

		if (ContextCompat.checkSelfPermission(mCtx,Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
			String [] permissions = {Manifest.permission.RECORD_AUDIO};
			int REQUEST_RECORD_AUDIO_PERMISSION = 200;
			ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
		}


		mCalendar = Calendar.getInstance();
		//수정 모드
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
			mDataRepeatDay = mAlarmVO.getRepeatDay()	;

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

				Crashlytics.log(Log.DEBUG, this.toString(), "mAlarmVO.getIsHolidayALL() " + mAlarmVO.getIsHolidayALL()  + " true = " + (mAlarmVO.getIsHolidayALL() == 1));
				if(mAlarmVO.getIsHolidayALL() == 1)
					mCbHolidayAll.setChecked(true);
				if(mAlarmVO.getIsHolidayNone() == 1)
					mCbHolidayNone.setChecked(true);

			}

			txTimeSet(mAlarmVO.getHour(), mAlarmVO.getMinute());
			mTxAlarmTitle.setText(mAlarmVO.getAlarmTitle());

			mSpAlarmType.setSelection(alarmType);
			mSpSoundType.setSelection(CommonUtils.getAlarmOptionPosition(mAlarmVO.getAlarmOption()));

			mToggleCallType.setCheckedTogglePosition(mAlarmVO.getAlarmCallType());

			ArrayList<Integer> arrAlarmCall = mAlarmVO.getAlarmCallList();
			int temp;
			if(arrAlarmCall != null) {
				for (int i = 0; i < arrAlarmCall.size(); i++) {
					temp = arrAlarmCall.get(i);
					appendAlarmRow(Math.abs(temp), (temp < 0 ? -1 : 1));
				}
			}
			mEtcType = mAlarmVO.getEtcType();
			restoreEtcType();

			if(mMemoVO != null) {
				mTvEtcTitle.setText(mMemoVO.getTitle());
				mTvEtcTitle.setVisibility(View.VISIBLE);
			}
		}
		//신규 등록 모드
		else {
			txTimeSet(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
			appendAlarmRow(0, 1);
			//mCbTTS.setChecked(true);
			mSpSoundType.setSelection(CommonUtils.getAlarmOptionPosition(2222));
		}

		if(mIsInitMemoMode) {
			initMemoMode();
		}

	}

	private void restoreEtcType(){
		Object[] arrkeys = mEtcMap.keySet().toArray();
		for(int i = 0 ; i < arrkeys.length; i++){
			if(arrkeys[i].equals(mEtcType)){
				Crashlytics.log(Log.DEBUG, this.toString(), " etc selected start");
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
		mTvEtcTitle.setOnClickListener(null);
	}

	private void returnData(){
		String alarmTitle = mTxAlarmTitle.getText().toString();

		if(alarmTitle.equals("")){
			alarmTitle = getString(R.string.alarm_no_title);
		}
		ArrayList<Calendar> alarmDate = new ArrayList<Calendar>();

		Log.d(this.toString(), "return data mAlarmDateType="+mAlarmDateType);

		// 반복이 아닐 경우 날짜 지정 데이터 삽입
		if(mAlarmDateType == Const.ALARM_DATE_TYPE.REPEAT){
			if(mDataRepeatDay.size() == 0){
				Toast.makeText(mCtx, getString(R.string.dialog_alarm_msg_select_repeat), Toast.LENGTH_LONG).show();
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

		/*
		if(mEtcType.equals(Const.ETC_TYPE.MEMO) && mMemoVO == null){
			Toast.makeText(mCtx, getString(R.string.dialog_alarm_msg_select_repeat), Toast.LENGTH_LONG).show();
			return;
		}*/

		int soundType = CommonUtils.getAlarmOptionValue(mSpSoundType.getSelectedItemPosition());

		isRecord = mRecorderCustomView.isRecord();
		//false - 이지만 기존 레코드가 있으면 통과 -> 기존거인지 신규인지 구분 필요 > 기존것도 신규로 전환시키면 됨 , 기존건 무조건 삭제

		//케시파일을 이용한 로직이기 때문에, alarm fragment에서 실제 파일 복사 및 fileVO 등록 과정을 전담함
		String voiceFile = mRecorderCustomView.getFilePath();

		if(soundType == Const.ALARM_OPTION_TO_SOUND.RECORD && voiceFile == null){
			Toast.makeText(mCtx, "녹음을 해주세요", Toast.LENGTH_SHORT).show();
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

		vo.setAlarmOption(soundType);//mCbTTS.isChecked() == true ? 1 : 0);
		vo.setHour(hour);
		vo.setMinute(minute);
		vo.setAlarmCallList(mArrAlarmCall);
		vo.setRepeatDay(mDataRepeatDay);
		vo.setAlarmDateType(mAlarmDateType);
		vo.setAlarmDateList(alarmDate);
		vo.setEtcType(mEtcType);
		vo.setAlarmCallType(mToggleCallType.getCheckedTogglePosition());

		if(mAlarmDateType == Const.ALARM_DATE_TYPE.REPEAT || mAlarmDateType == Const.ALARM_DATE_TYPE.REPEAT_MONTH){
			Crashlytics.log(Log.DEBUG, this.toString(), "mCbHolidayAll.isChecked() =" + mCbHolidayAll.isChecked());
			vo.setIsHolidayALL(mCbHolidayAll.isChecked() ? 1 : 0);
			vo.setIsHolidayNone(mCbHolidayNone.isChecked() ? 1 : 0);
		}

		Bundle bundle = new Bundle();
		bundle.putSerializable(Const.PARAM.ALARM_VO, vo);
		//녹음을 한 경우만 true가 됨.
		//bundle.putBoolean(Const.PARAM.IS_RECORD , isRecord);
		if(voiceFile != null)
			bundle.putString(Const.PARAM.FILE_PATH, voiceFile);
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
        llRecorderWrap = (LinearLayout) v.findViewById(R.id.recorderWrap);
		llAlertTimeOptionWrap = (LinearLayout) v.findViewById(R.id.alertTimeOptionWrap);
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
				Log.d(this.toString(), "position= " + position + " code="+code);
				renderDateTypeUi(code, mCalendar);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		mSpDateType.setSelection(2);
	}

	//날짜 선택 - spinner 선택에 따른 - 내일 , 모레,
	private void renderDateTypeUi(int alarmDateType, Calendar c) {
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
				else
					llHolidayOptionWrap.setVisibility(View.GONE);
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

		Log.d(this.toString(), "renderDateTypeUi mAlarmDateType="+mAlarmDateType);
	}

	public void makeSpinnerAlarmType(){
		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.add(getString(R.string.dialog_alarm_sp_notification));
		arrayList.add(getString(R.string.dialog_alarm_sp_user_stop));

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.simple_spinner_item_small, arrayList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		//스피너 속성
		//mSpAlarmType.setPrompt("알람 종류"); // 스피너 제목
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
	public void makeSpinnerSoundType(){
		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.add(getString(R.string.none));
		arrayList.add(getString(R.string.dialog_alarm_sp_sound_tts));
		arrayList.add(getString(R.string.dialog_alarm_sp_sound_record));
		//arrayList.add(getString(R.string.dialog_alarm_sp_sound_file));

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.simple_spinner_item_small, arrayList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		//스피너 속성
		//mSpAlarmType.setPrompt("알람 종류"); // 스피너 제목
		mSpSoundType.setAdapter(adapter);
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
		//mSpAppList.setPrompt("알람 종류"); // 스피너 제목
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

    private void showMemo(){
		AlertDialog.Builder alert_confirm = new AlertDialog.Builder(mCtx);
		alert_confirm.setMessage(getString(R.string.alarm_msg_move_memo)).setCancelable(false).setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					//mTvEtcTitle
					Bundle args = new Bundle();
					args.putBoolean(Const.MEMO.MEMO_INTERFACE_CODE.VIEW_MEMO_ETC_KEY, true);
					args.putLong(Const.PARAM.ALARM_ID, mAlarmVO.getId());
					Intent intent = new Intent();
					intent.putExtras(args);
					int returnCode = Const.MEMO.MEMO_INTERFACE_CODE.VIEW_MEMO_ETC_CODE;
					getTargetFragment().onActivityResult(getTargetRequestCode(), returnCode, intent);
					dialog.dismiss();
					}
				}).setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 'No'
						dialog.dismiss();
					}
				});
		AlertDialog alert = alert_confirm.create();
		alert.show();
    }

	public void bindEvent(){
		final Object[] values = mEtcMap.keySet().toArray();
		mSpAppList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mEtcType = (String) values[position];
				if(mEtcType.equals(Const.ETC_TYPE.MEMO)){
					Crashlytics.log(Log.DEBUG, this.toString(), " onitem selected ");
					//if(mIsInitMemoMode == false)
					//Crashlytics.log(Log.DEBUG, this.toString(), "(int) mSpAppList.getTag(R.id.spAppList) = "+(int) mSpAppList.getTag(R.id.spAppList) + " posi = " +position);

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
					if(mAlarmVO.getAlarmDateList() != null && mAlarmVO.getAlarmDateList().size() > 0)
						calendar = mAlarmVO.getAlarmDateList().get(0);
					else
						calendar = Calendar.getInstance();
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

		if(CommonUtils.isLocaleKo(getResources().getConfiguration())) {
			mBtnHelp.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(mCtx, Intro.class);
					i.putExtra(Const.PARAM.MODE, "alarmPopup");
					i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
					startActivity(i);
				}
			});
		}else{
			mBtnHelp.setVisibility(View.GONE);
		}

		if(!mIsInitMemoMode) {
			mTvEtcTitle.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showMemo();
				}
			});
		}

		mSpSoundType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(CommonUtils.getAlarmOptionValue(position) == Const.ALARM_OPTION_TO_SOUND.RECORD){
                    showRecordWrap(true);
				}else{
                    showRecordWrap(false);
                }
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
    }

    private void showRecordWrap(boolean isShow){
        if(isShow)
            llRecorderWrap.setVisibility(View.VISIBLE);
        else
            llRecorderWrap.setVisibility(View.GONE);
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

		//final NumberPicker np = ButterKnife.findById(container, R.id.numberPicker);// new NumberPicker(mCtx);

		final NumberPicker npHours = ButterKnife.findById(container, R.id.addAlarmHourPicker);
		final NumberPicker npMinutes = ButterKnife.findById(container, R.id.addAlarmMinutePicker);
/*
		np.setMinValue(0);
		np.setMaxValue(59);
		np.setValue(5);
		np.setWrapSelectorWheel(true);*/
		/*
		params.gravity = Gravity.CENTER;
		np.setLayoutParams(params);
		*/
		//ll.addView(np);

		Button btn = ButterKnife.findById(container, R.id.button);//new Button(mCtx);
		btn.setText(getString(R.string.before));
		//params.gravity = Gravity.CENTER_VERTICAL;
		//btn.setLayoutParams(params);
		/////////////////////////////////// 시간 단위 추가
		npHours.setMaxValue(23);
		npHours.setValue(0);
		npMinutes.setMaxValue(59);
		npMinutes.setValue(5);
		npHours.setWrapSelectorWheel(true);
		npMinutes.setWrapSelectorWheel(true);

		mTemp = btn;
		btn.setTag(-1);
		btn.setOnClickListener(new View.OnClickListener() {
			int toggle = 0;
			@Override
			public void onClick(View v) {

				if (toggle == 0) {
					v.setTag(1);
					toggle = 1;
					((Button) v).setText(getString(R.string.after));
				} else {
					v.setTag(-1);
					toggle = 0;
					((Button) v).setText(getString(R.string.before));
				}
			}
		});

		builder.setView(container);

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				d(this.toString(), "getTag = " + ((Button) mTemp).getTag());
				int aa = Integer.parseInt(((Button) mTemp).getTag().toString());
				int m = npMinutes.getValue();
				int h = npHours.getValue();
				m = h * 60 + m;
				appendAlarmRow(m, aa);
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
			Toast.makeText(mCtx, getString(R.string.dialog_alarm_msg_duplication_call), Toast.LENGTH_LONG).show();
			return ;
		}

		mArrAlarmCall.add(val * flag);

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View beforeView = inflater.inflate(R.layout.alaram_before, null);

		TextView tv = ButterKnife.findById(beforeView, R.id.tvBeforeTime);
		ImageButton bt = ButterKnife.findById(beforeView, R.id.btnRemoveTime);

		bt.setTag(val * flag);

		int h = val / 60;
		int m = val % 60;

		if(flag == -1)
			tv.setText((h > 0 ? h + getString(R.string.hours) + " " : "") + m + getString(R.string.dialog_alarm_minute_before));
		else if(val == 0){
			tv.setText( val + getString(R.string.minute));
			bt.setVisibility(View.GONE);
		}
		else
			tv.setText((h > 0 ? h + getString(R.string.hours) + " " : "") + m + getString(R.string.dialog_alarm_minute_after));

		if(val != 0) {
			bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int index = mArrAlarmCall.indexOf(v.getTag());
					mArrAlarmCall.remove(index);
					((ViewGroup) v.getParent().getParent()).removeView((ViewGroup) v.getParent());
					if(mArrAlarmCall.size() == 1){
						llAlertTimeOptionWrap.setVisibility(View.GONE);
					}
				}
			});
		}
		mAlarmList.addView(beforeView);

		Log.d(this.toString(), "typeoptionwrap visible");

		if(mArrAlarmCall.size() > 1)
			llAlertTimeOptionWrap.setVisibility(View.VISIBLE);

		mScvAddAlarm.refreshDrawableState();

	}

	/*public void setupUI(View view) {

		// Set up touch listener for non-text box views to hide keyboard.
		if (!(view instanceof EditText)) {
			view.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					Crashlytics.log(Log.DEBUG, this.toString(), "on thuch");
					InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

					inputManager.hideSoftInputFromWindow(mDialog.getWindow().getCurrentFocus().getWindowToken(), 0);
					Crashlytics.log(Log.DEBUG, this.toString(), "hideSoftKeyboard");
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

	@Override
	public void onDialogPositiveClick(boolean isRecord) {
		this.isRecord = isRecord;
	}

	@Override
	public void onDialogNegativeClick() {

	}

    @Override
    public void onStop() {
        super.onStop();
        mRecorderCustomView.onStop();
    }
}
