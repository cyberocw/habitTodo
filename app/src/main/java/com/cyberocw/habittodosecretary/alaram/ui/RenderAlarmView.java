package com.cyberocw.habittodosecretary.alaram.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.alaram.AlarmFragment;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.ButterKnife;

/**
 * Created by cyber on 2017-04-15.
 */

public class RenderAlarmView {
    public static void RenderAlarmView(final Context ctx, final AlarmFragment mMainFragment, final AlarmDataManager mManager, final AlarmVO vo, View convertView, int listViewType){
        SharedPreferences prefs = ctx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
        String text = prefs.getString(Const.PARAM.ALARM_ID, null);
        String[] arrAlarmId = null;
        if(text != null && !"".equals(text)) {
            arrAlarmId = text.split(",");
            if (arrAlarmId.length == 0)
                arrAlarmId[0] = text;
        }

        /*if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.REPEAT)
            convertView.setBackgroundResource(R.color.background_repeat);
        else if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.SET_DATE)
            convertView.setBackgroundResource(R.color.background_date);
        else if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.POSTPONE_DATE)
            convertView.setBackgroundResource(R.color.background_postphone_date);
        else if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.REPEAT_MONTH)
            convertView.setBackgroundResource(R.color.background_repeat_day);*/

        ToggleButton dateToggleBtn = (ToggleButton) convertView.findViewById(R.id.timeText);

        dateToggleBtn.setText(vo.getTimeText());
        dateToggleBtn.setTextOn(vo.getTimeText());
        dateToggleBtn.setTextOff(vo.getTimeText());

        boolean isFind = false;

        Calendar alarmDate = (Calendar) mManager.mCalendar.clone();

        alarmDate.set(Calendar.MINUTE, vo.getMinute());
        alarmDate.set(Calendar.HOUR_OF_DAY, vo.getHour());
        alarmDate.set(Calendar.SECOND, 0);
        alarmDate.set(Calendar.MILLISECOND, 0);
        //활성화된 알림 중
        if(vo.getUseYn() == 1) {
            for (int i = 0; i < arrAlarmId.length; i++) {

                Log.d(Const.DEBUG_TAG, "arrAlarmId = " + arrAlarmId[i] + " void = " + vo.getId());

                //동일 아이디일 경우만 처리
                if (Long.valueOf(arrAlarmId[i]) == vo.getId()) {
                    long timeStamp = prefs.getLong(Const.PARAM.ALARM_ID_TIME_STAMP, 0);
                    ArrayList<Integer> arrAlarmCallList = vo.getAlarmCallList();

                    for (int k = 0; k < arrAlarmCallList.size(); k++) {
                        Calendar cal = (Calendar) alarmDate.clone();
                        cal.add(Calendar.MINUTE, arrAlarmCallList.get(k));
                        if(timeStamp == cal.getTimeInMillis()){
                            Drawable img = ContextCompat.getDrawable(ctx, R.drawable.ic_chevron_right_black_24dp);
                            dateToggleBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                            isFind = true;
                            break;
                        }
                    }
                    if(isFind)
                        break;
                }
            }
        }
        if (!isFind) {
            Drawable img = ContextCompat.getDrawable(ctx, R.drawable.toggle_timer);
            dateToggleBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }

        ImageButton btnOption = (ImageButton) convertView.findViewById(R.id.optionButton);
        btnOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AlarmVO vo = mManager.getItem(position);
                if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.POSTPONE_DATE) {
                    mMainFragment.deleteItemAlertDialog(vo.getId());
                }
                else
                    mMainFragment.longClickPopup(0, vo.getId());
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AlarmVO vo = mManager.getItem(position);

                if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.POSTPONE_DATE){
                    mMainFragment.deleteItemAlertDialog(vo.getId());
                }
                else
                    mMainFragment.showNewAlarmDialog(vo.getId());
            }
        });



        if(vo.getUseYn() == 1)
            dateToggleBtn.setChecked(true);
        else
            dateToggleBtn.setChecked(false);

        dateToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AlarmVO vo = mManager.getItem(position);
                ToggleButton btn = (ToggleButton) v;
                boolean isChecked = btn.isChecked();

                if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.POSTPONE_DATE){
                    btn.setChecked(true);
                    mMainFragment.deleteItemAlertDialog(vo.getId());
                    return;
                }

                if(isChecked == true)
                    vo.setUseYn(1);
                else
                    vo.setUseYn(0);

                if(mManager.modifyUseYn(vo) == false)
                    Toast.makeText(ctx, "useYn 변환에 실패했습니다", Toast.LENGTH_SHORT).show();
                else {
                    mManager.resetMinAlarmCall(vo.getAlarmDateType());
                }
                mMainFragment.refreshAlarmList();
            }
        });

        String title = vo.getAlarmTitle();

        TextView tv = (TextView) convertView.findViewById(R.id.alarmTitle);
        tv.setText(title);

        TextView tvGroupTitle = ButterKnife.findById(convertView, R.id.tvGroupTitle);

        if(listViewType == Const.ALARM_LIST_VIEW_TYPE.LIST){

            String result = "";
            switch (vo.getAlarmDateType()){
                case Const.ALARM_DATE_TYPE.REPEAT_MONTH :
                case Const.ALARM_DATE_TYPE.REPEAT : result = ctx.getResources().getString(R.string.group_title_repeat); break;
                case Const.ALARM_DATE_TYPE.SET_DATE : result = ctx.getResources().getString(R.string.group_title_set_date); break;
                case Const.ALARM_DATE_TYPE.POSTPONE_DATE : result = ctx.getResources().getString(R.string.group_title_postpone); break;

            }
            tvGroupTitle.setText(result);
            tvGroupTitle.setVisibility(View.VISIBLE);
        }else{
            tvGroupTitle.setVisibility(View.GONE);
        }
    }
}
