package com.cyberocw.habittodosecretary.alaram.ui;

import android.content.Context;
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

/**
 * Created by cyber on 2017-04-15.
 */

public class RenderAlarmView {
    public static void RenderAlarmView(final Context ctx, final AlarmFragment mMainFragment, final AlarmDataManager mManager, final AlarmVO vo, View convertView){

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
            }
        });

        String title = vo.getAlarmTitle();

        TextView tv = (TextView) convertView.findViewById(R.id.alarmTitle);
        tv.setText(title);
    }
}
