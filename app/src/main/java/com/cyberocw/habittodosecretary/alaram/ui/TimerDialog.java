package com.cyberocw.habittodosecretary.alaram.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.vo.TimerVO;

import java.util.ArrayList;


/**
 * Created by cyberocw on 2015-10-18.
 */
public class TimerDialog extends DialogFragment {
	private Context mCtx = null;
	private int mModifyMode = 0;
	private View mView = null;
	private TimerVO mTimerVO = null;
	private EditText mTxAlarmTitle = null;
	private Spinner mSpAlarmType = null;
	private NumberPicker mNpHour = null;
	private NumberPicker mNpMinute = null;
	private NumberPicker mNpSecond = null;

	public TimerDialog() {
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mCtx = getActivity();

		Bundle arguments = getArguments();

		if(arguments != null) {
			mTimerVO = (TimerVO) arguments.getSerializable(Const.TIMER_VO);
			mModifyMode = 1;
		}
		else{
			mTimerVO = new TimerVO();
		}

		//bindEvent();
		init();
		super.onActivityCreated(savedInstanceState);
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
		View view = inflater.inflate(R.layout.fragment_dialog_timer, null);
		mView = view;

		b.setView(view);

		return b.create();
	}

	public void init(){
		getViewRes();
		makeSpinnerAlarmType();
	}

	public void getViewRes(){
		mTxAlarmTitle = (EditText) mView.findViewById(R.id.txAlarmTitle);
		mSpAlarmType = (Spinner) mView.findViewById(R.id.spAlarmType);
		mNpHour = (NumberPicker) mView.findViewById(R.id.addAlarmHourPicker);
		mNpMinute = (NumberPicker) mView.findViewById(R.id.addAlarmMinutePicker);
		mNpSecond = (NumberPicker) mView.findViewById(R.id.addAlarmSecondPicker);

		mNpHour.setMaxValue(10);
		mNpHour.setMinValue(0);
		mNpMinute.setMaxValue(59);
		mNpMinute.setMinValue(0);
		mNpSecond.setMaxValue(59);
		mNpSecond.setMinValue(0);

		if(mModifyMode == 1){
			mTxAlarmTitle.setText(mTimerVO.getAlarmTitle());
			mNpHour.setValue(mTimerVO.getHour());
			mNpMinute.setValue(mTimerVO.getMinute());
			mNpSecond.setValue(mTimerVO.getSecond());
		}
	}
	public void makeSpinnerAlarmType(){
		ArrayList<String> arrayList = new ArrayList<String>();
		//// TODO: 2015-10-18 공통 함수로 분리하기
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

		if(mModifyMode == 1){
			mSpAlarmType.setSelection(mTimerVO.getAlarmType());
		}

		mSpAlarmType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});


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

		String alarmTitle = mTxAlarmTitle.getText().toString();

		if(alarmTitle.equals("")){
			alarmTitle = getString(R.string.alarm_no_title);
		}
		mTimerVO.setAlarmTitle(alarmTitle);

		//알리는 방법
		int alarmType = mSpAlarmType.getSelectedItemPosition();
		mTimerVO.setAlarmType(alarmType);

		Bundle bundle = new Bundle();
		bundle.putSerializable("timerVO", mTimerVO);
		Intent intent = new Intent();
		intent.putExtras(bundle);

		int returnCode = mModifyMode == 1 ? Const.ALARM_INTERFACE_CODE.ADD_TIMER_MODIFY_FINISH_CODE : Const.ALARM_INTERFACE_CODE.ADD_TIMER_FINISH_CODE;
		getTargetFragment().onActivityResult(getTargetRequestCode(), returnCode, intent);
	}
}
