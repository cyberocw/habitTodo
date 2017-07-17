package com.cyberocw.habittodosecretary.alaram.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
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
    public static void RenderAlarmView(final Context ctx, final AlarmFragment mMainFragment, final AlarmDataManager mManager, final AlarmVO vo, View convertView, int listViewType, int position){
        boolean isDashboard = false;
        if(mMainFragment == null)
            isDashboard = true;

        SharedPreferences prefs = ctx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
        String text = prefs.getString(Const.PARAM.ALARM_ID, null);
        String[] arrAlarmId = null;
        if(text != null && !"".equals(text)) {
            arrAlarmId = text.split(",");
            if (arrAlarmId.length == 0)
                arrAlarmId[0] = text;
        }
        ToggleButton dateToggleBtn = (ToggleButton) convertView.findViewById(R.id.timeText);
        LinearLayout listViewTextWrap = (LinearLayout) convertView.findViewById(R.id.listViewTextWrap);
        if(position == mManager.getCount()-1){
            int padding = ctx.getResources().getDimensionPixelOffset(R.dimen.listViewBottom);
            if(!isDashboard)
                listViewTextWrap.setPadding(0, 0, 0, padding);
        }else{
            listViewTextWrap.setPadding(0, 0, 0, 0);
        }
        dateToggleBtn.setText(vo.getTimeText());
        dateToggleBtn.setTextOn(vo.getTimeText());
        dateToggleBtn.setTextOff(vo.getTimeText());

        boolean isFind = false;
/*
        Calendar alarmDate = (Calendar) mManager.mCalendar.clone();

        alarmDate.set(Calendar.MINUTE, vo.getMinute());
        alarmDate.set(Calendar.HOUR_OF_DAY, vo.getHour());
        alarmDate.set(Calendar.SECOND, 0);
        alarmDate.set(Calendar.MILLISECOND, 0);*/
        //활성화된 알림 중
        if(vo.getUseYn() == 1) {
            for (int i = 0; i < arrAlarmId.length; i++) {

                Log.d(Const.DEBUG_TAG, "arrAlarmId = " + arrAlarmId[i] + " void = " + vo.getId());

                //동일 아이디일 경우만 처리
                if (Long.valueOf(arrAlarmId[i]) == vo.getId()) {
                    /*Drawable img = ContextCompat.getDrawable(ctx, R.drawable.ic_chevron_right_black_24dp);
                    dateToggleBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
*/
                    int dateType = vo.getAlarmDateType();
                    if(dateType == Const.ALARM_DATE_TYPE.REPEAT || dateType == Const.ALARM_DATE_TYPE.REPEAT_MONTH){
                        isFind = true;
                        break;
                    }
                    ArrayList<Calendar> arrAlarmDate = vo.getAlarmDateList();
                    Calendar alarmDate2 = null;
                    if(arrAlarmDate == null && arrAlarmDate.size() == 0) {
                        break;
                    }

                    alarmDate2 = arrAlarmDate.get(0);
                    alarmDate2.set(Calendar.HOUR_OF_DAY, vo.getHour());
                    alarmDate2.set(Calendar.MINUTE, vo.getMinute());

                    ArrayList<Integer> arrAlarmCall = vo.getAlarmCallList();
                    int temp;
                    Calendar now = Calendar.getInstance();
                    if(arrAlarmCall != null) {
                        for (int k = 0; k < arrAlarmCall.size(); k++) {
                            temp = arrAlarmCall.get(k);
                            Calendar c = (Calendar) alarmDate2.clone();
                            c.add(Calendar.MINUTE, temp);
                            //현재 이후 알림이 있으면 추가하고 종료
                            if(now.getTimeInMillis() < c.getTimeInMillis()){
                                isFind = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (!isFind) {
            Drawable img = ContextCompat.getDrawable(ctx, R.drawable.toggle_timer);
            dateToggleBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }else{
            Drawable img = ContextCompat.getDrawable(ctx, R.drawable.ic_chevron_right_black_24dp);
            dateToggleBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
        ImageButton btnOption = (ImageButton) convertView.findViewById(R.id.optionButton);

        if(isDashboard == false) {
            btnOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //AlarmVO vo = mManager.getItem(position);
                    if (vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.POSTPONE_DATE) {
                        mMainFragment.deleteItemAlertDialog(vo.getId());
                    } else
                        mMainFragment.longClickPopup(0, vo.getId());
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //AlarmVO vo = mManager.getItem(position);

                    if (vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.POSTPONE_DATE) {
                        mMainFragment.deleteItemAlertDialog(vo.getId());
                    } else
                        mMainFragment.showNewAlarmDialog(vo.getId());
                }
            });
        }
        else{
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //AlarmVO vo = mManager.getItem(position);
                    Log.d(Const.DEBUG_TAG, "nav item activity gogo");
                    ((MainActivity) ctx).onNavigationItemSelected(R.id.nav_item_alaram);

                }
            });
            btnOption.setVisibility(View.GONE);
        }

        if(vo.getUseYn() == 1)
            dateToggleBtn.setChecked(true);
        else
            dateToggleBtn.setChecked(false);

        final boolean finalIsDashboard = isDashboard;
        dateToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AlarmVO vo = mManager.getItem(position);
                ToggleButton btn = (ToggleButton) v;
                boolean isChecked = btn.isChecked();

                if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.POSTPONE_DATE){
                    btn.setChecked(true);
                    if(finalIsDashboard == false)
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
                if(finalIsDashboard == false)
                    mMainFragment.refreshAlarmList();
            }
        });

        String title = vo.getAlarmTitle();

        TextView tv = (TextView) convertView.findViewById(R.id.alarmTitle);
        tv.setText(title);

        TextView tvGroupTitle = ButterKnife.findById(convertView, R.id.tvGroupTitle);

        boolean headerVisible = false;

/*
        TextView tvDayTitle = ButterKnife.findById(convertView, R.id.tvDayTitle);
        //대시 보드
        if(isDashboard){
            tvDayTitle.setVisibility(View.VISIBLE);
            vo.getAlarmDateType()
            tvDayTitle.setText();
            headerVisible = true;
        }else{
            tvDayTitle.setVisibility(View.GONE);
        }
*/

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
            headerVisible = true;
        }else{
            tvGroupTitle.setVisibility(View.GONE);
        }

        TextView tvRelationTitle = ButterKnife.findById(convertView, R.id.tvRelationTitle);
        if(vo.getEtcType() != null && vo.getEtcType().equals(Const.ETC_TYPE.MEMO)){
            tvRelationTitle.setVisibility(View.VISIBLE);
            tvRelationTitle.setText(ctx.getString(R.string.memoGroupTitle));
            headerVisible = true;
        }else{
            tvRelationTitle.setVisibility(View.GONE);
        }

        //TextView tvTimeTitle = ButterKnife.findById(convertView, R.id.tvTimeTitle);
        ArrayList<Integer> arrCall = vo.getAlarmCallList();

        LinearLayout linearLayout = ButterKnife.findById(convertView, R.id.alarmOptionWrap);
        linearLayout.removeAllViewsInLayout();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(5, 0, 5, 0);

        if(arrCall != null)
            for(int i = 0 ; i < arrCall.size(); i++){
                if(arrCall.get(i) == 0)
                    continue;

                TextView tvTime = new TextView(ctx);
                tvTime.setText(arrCall.get(i) < 0 ? arrCall.get(i).toString() : "+" + arrCall.get(i));
                tvTime.setLayoutParams(params);
                tvTime.setTextColor(ContextCompat.getColor(ctx, R.color.black_semi_transparent));
                tvTime.setBackground(ContextCompat.getDrawable(ctx, R.drawable.button_alarm_time_round));
                tvTime.setPadding(8, 5, 8, 5);
                tvTime.setTextSize(ctx.getResources().getDimension(R.dimen.alarmViewTopGroupTextSize));
                linearLayout.addView(tvTime);
                headerVisible = true;
            }

        LinearLayout llTitleWrap = ButterKnife.findById(convertView, R.id.alarmViewTitleWrap);
        llTitleWrap.setVisibility(headerVisible == true ? View.VISIBLE : View.GONE);
    }
}
