package com.cyberocw.habittodosecretary.alaram.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.vo.TimerVO;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.util.ArrayList;

import butterknife.ButterKnife;


/**
 * Created by cyberocw on 2015-10-18.
 */
public class TimerDialog extends DialogFragment {
	private Context mCtx = null;
	private int mModifyMode = 0;
	private View mView = null;
	private TimerVO mTimerVO = null;
	private EditText mTxAlarmTitle = null;
	private Spinner mSpAlarmType = null, mSpSoundType = null;
	private NumberPicker mNpHour = null;
	private NumberPicker mNpMinute = null;
	private NumberPicker mNpSecond = null;
	Button mBtnSave = null;

	public TimerDialog() {
	}
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		Crashlytics.log(Log.DEBUG, this.toString(), " onCreateView ");
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_dialog_timer, container, false);
		mView = view;

		Toolbar toolbar = (Toolbar) mView.findViewById(R.id.toolbar);
		toolbar.setVisibility(View.VISIBLE);
		toolbar.setTitle(getResources().getString(R.string.dialog_title_timer));
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
	public void onActivityCreated(Bundle savedInstanceState) {
		mCtx = getActivity();

		Bundle arguments = getArguments();

		if(arguments != null) {
			mTimerVO = (TimerVO) arguments.getSerializable(Const.PARAM.TIMER_VO);
			mModifyMode = 1;
		}
		else{
			mTimerVO = new TimerVO();
		}

		//bindEvent();
		init();
		CommonUtils.setupUI(mView, getActivity());
		super.onActivityCreated(savedInstanceState);
	}

	public Dialog onCreateDialog2(Bundle savedInstanceState) {
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
		View view = inflater.inflate(R.layout.fragment_dialog_timer, null);
		mView = view;

		b.setView(view);

		return b.create();
	}

	public void init(){
		getViewRes();
		//makeSpinnerAlarmType();
	}

	public void getViewRes(){
		mBtnSave = ButterKnife.findById(mView, R.id.btnSave);
		mTxAlarmTitle = (EditText) mView.findViewById(R.id.txAlarmTitle);
		mNpHour = (NumberPicker) mView.findViewById(R.id.addAlarmHourPicker);
		mNpMinute = (NumberPicker) mView.findViewById(R.id.addAlarmMinutePicker);
		mNpSecond = (NumberPicker) mView.findViewById(R.id.addAlarmSecondPicker);
		mSpAlarmType = ButterKnife.findById(mView, R.id.spAlarmType);
		mSpSoundType = ButterKnife.findById(mView, R.id.spVoiceType);
		mNpHour.setMaxValue(10);
		mNpHour.setMinValue(0);
		mNpMinute.setMaxValue(59);
		mNpMinute.setMinValue(0);
		mNpSecond.setMaxValue(59);
		mNpSecond.setMinValue(0);

		makeSpinnerAlarmType();
		makeSpinnerSoundType();

		if(mModifyMode == 1){
			mTxAlarmTitle.setText(mTimerVO.getAlarmTitle());
			mNpHour.setValue(mTimerVO.getHour());
			mNpMinute.setValue(mTimerVO.getMinute());
			mNpSecond.setValue(mTimerVO.getSecond());
			mSpAlarmType.setSelection(mTimerVO.getAlarmType());
			mSpSoundType.setSelection(mTimerVO.getAlarmSoundOption());
		}else{
			mSpAlarmType.setSelection(1);
			mSpSoundType.setSelection(1);
		}

		mBtnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				returnData();
			}
		});
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

	}
	public void makeSpinnerSoundType(){
		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.add(getString(R.string.none));
		arrayList.add(getString(R.string.dialog_alarm_sp_sound_tts));
		//arrayList.add(getString(R.string.dialog_alarm_sp_sound_record));

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.simple_spinner_item_small, arrayList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		//스피너 속성
		//mSpAlarmType.setPrompt("알람 종류"); // 스피너 제목
		mSpSoundType.setAdapter(adapter);
	}

	private void returnData(){
		int hour = mNpHour.getValue();
		int minute = mNpMinute.getValue();
		int second = mNpSecond.getValue();
		int sum = hour * 60 * 60 + minute * 60 + second;

		if(sum == 0){
			Toast.makeText(mCtx, "시간을 지정해주세요", Toast.LENGTH_LONG).show();
			return ;
		}

		mTimerVO.setHour(hour);
		mTimerVO.setMinute(minute);
		mTimerVO.setSecond(second);

		mTimerVO.setAlarmType(mSpAlarmType.getSelectedItemPosition());
		mTimerVO.setAlarmSoundOption(mSpSoundType.getSelectedItemPosition());

		String alarmTitle = mTxAlarmTitle.getText().toString();

		if(alarmTitle.equals("")){
			alarmTitle = getString(R.string.alarm_no_title);
		}
		mTimerVO.setAlarmTitle(alarmTitle);

		//알리는 방법

		Bundle bundle = new Bundle();
		bundle.putSerializable("timerVO", mTimerVO);
		Intent intent = new Intent();
		intent.putExtras(bundle);

		int returnCode = mModifyMode == 1 ? Const.ALARM_INTERFACE_CODE.ADD_TIMER_MODIFY_FINISH_CODE : Const.ALARM_INTERFACE_CODE.ADD_TIMER_FINISH_CODE;
		getTargetFragment().onActivityResult(getTargetRequestCode(), returnCode, intent);
		getActivity().getSupportFragmentManager().popBackStackImmediate();
	}
}
