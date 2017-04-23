package com.cyberocw.habittodosecretary.calendar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.alaram.vo.HolidayVO;
import com.cyberocw.habittodosecretary.util.CommonUtils;
import com.marcohc.robotocalendar.RobotoCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by cyber on 2017-03-18.
 */

public class CalendarDialog extends DialogFragment implements RobotoCalendarView.RobotoCalendarListener {
    private Calendar mCalendar;
    private CalendarManager mCalendarManager;
    private RobotoCalendarView robotoCalendarView;
    private ArrayAdapter mAlarmAdapter;
    private ArrayList<AlarmVO> mArrAlarmList;
    private ArrayList<String> mArrTodayAlarm = new ArrayList<String>();
    ArrayList<HolidayVO> mArrHoliday;
    private TextView mTvTitle;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        android.support.v7.app.AlertDialog.Builder b = new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setTitle("날짜 선택")
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

        Bundle bundle = getArguments();
        if (bundle != null) {
            String selectedDay = bundle.getString("selectedDate");
            mCalendar = CommonUtils.convertDateType(selectedDay);
        }
        else{
            Toast.makeText(getContext(), "날짜 정보가 전달되지 않았습니다", Toast.LENGTH_SHORT).show();
            mCalendar = Calendar.getInstance();
        }

        mCalendarManager = new CalendarManager(getContext());


        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view =  inflater.inflate(R.layout.fragment_dialog_calendar, null);

        robotoCalendarView = (RobotoCalendarView) view.findViewById(R.id.robotoCalendarPicker);

        ListView lv = (ListView) view.findViewById(R.id.alramListView);
        mTvTitle = (TextView) view.findViewById(R.id.tvSelectedDateInfo);
        mAlarmAdapter = new ArrayAdapter(getContext(), R.layout.simple_small_text_view, R.id.alarmTitle, mArrTodayAlarm) ;
        lv.setAdapter(mAlarmAdapter);

        // Set listener, in this case, the same activity
        robotoCalendarView.setRobotoCalendarListener(this);

        robotoCalendarView.setShortWeekDays(true);

        robotoCalendarView.showDateTitle(true);

        robotoCalendarView.setCalendar(mCalendar);

        robotoCalendarView.updateView();
        setCalendarIcon();
        onDayClick((Calendar) mCalendar.clone());

        String sDate = dateFormat.format(mCalendar.getTime());
        mTvTitle.setText(sDate);

        b.setView(view);
        Dialog dialog = b.create();
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        return dialog;
    }
    @Override
    public void onDayClick(Calendar calendar) {
        mCalendar = calendar;

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        HolidayVO hVO;

        String sDate = dateFormat.format(calendar.getTime());

        mTvTitle.setText(sDate);

        for(int i = 0 ; i < mArrHoliday.size(); i++){
            hVO = mArrHoliday.get(i);
            if(hVO.getDay() == day){
                if (hVO.getType().equals("h") || hVO.getType().equals("i")) {
                    String daytext ="";
                    if(hVO.getType().equals("i"))
                        daytext = "대체공휴일";

                    else
                        daytext = hVO.getName();

                    //mTvTitle.setVisibility(View.VISIBLE);
                    mTvTitle.setText(sDate + " : " + daytext);
                    break;
                }
            }
        }
        getTodayAlarmList();
    }

    @Override
    public void onDayLongClick(Calendar calendar) {

    }

    @Override
    public void onRightButtonClick() {
        //여기에 해당 연월의 연휴 및 setDate 일정 가져와서 setImage 반복 호출
        mCalendar.add(Calendar.MONTH, 1);

        setCalendarIcon();

    }

    @Override
    public void onLeftButtonClick() {
        mCalendar.add(Calendar.MONTH, -1);
        //onDayClick(mCalendar);
        setCalendarIcon();
    }

    private void getTodayAlarmList(){

        ArrayList<AlarmVO> arrAlarm = mCalendarManager.getAlarmList(mCalendar);
        mArrTodayAlarm.clear();

        for(int i = 0 ; i < arrAlarm.size(); i++){
            AlarmVO vo = arrAlarm.get(i);

            if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.POSTPONE_DATE)
                continue;

            mArrTodayAlarm.add(CommonUtils.numberDigit(2, vo.getHour()) + ":" + CommonUtils.numberDigit(2, vo.getMinute()) + "  " +
                    vo.getAlarmTitle() + "  " + Const.ALARM_DATE_TYPE.getText(Const.ALARM_DATE_TYPE.getPositionByCode(vo.getAlarmDateType())));
        }

        mAlarmAdapter.notifyDataSetChanged();
        //mArrTodayAlarm = array;


    }
    private void setCalendarIcon(){
        Crashlytics.log(Log.DEBUG, this.toString(), "setCalendarIcon start");

        Calendar tempCal = (Calendar)mCalendar.clone();

        mArrHoliday = mCalendarManager.getHolidayMonthList(tempCal);
        for(int i = 0 ; i < mArrHoliday.size(); i++){
            tempCal.set(Calendar.DAY_OF_MONTH, mArrHoliday.get(i).getDay());
            robotoCalendarView.markCircleImage1(tempCal);
        }

        mArrAlarmList = mCalendarManager.getAlarmMonthList(tempCal);

        ArrayList<Integer> arrAlarmDay = new ArrayList<>();
        int day;

        for(int i = 0 ; i < mArrAlarmList.size(); i++){
            //tempCal.set(Calendar.DAY_OF_MONTH, );
            Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "arrAlarmList.get(i).getAlarmDateList().get(0))="+CommonUtils.convertDateType(mArrAlarmList.get(i).getAlarmDateList().get(0)));
            day = mArrAlarmList.get(i).getAlarmDateList().get(0).get(Calendar.DAY_OF_MONTH);
            if(mArrAlarmList.get(i).getAlarmDateType() == Const.ALARM_DATE_TYPE.POSTPONE_DATE)
                continue;
            if(!arrAlarmDay.contains(day)) {
                robotoCalendarView.markCircleImage2(mArrAlarmList.get(i).getAlarmDateList().get(0));
                arrAlarmDay.add(day);
            }

        }

        //getTodayAlarmList();

        //robotoCalendarView.clearCalendar();
    }

    private void returnData(){

        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedDate", mCalendar);
        Intent intent = new Intent();
        intent.putExtras(bundle);

        int returnCode = Const.ALARM_INTERFACE_CODE.SELECT_CALENDAR_DATE;
        getTargetFragment().onActivityResult(getTargetRequestCode(), returnCode, intent);
    }
}
