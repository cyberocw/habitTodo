package com.cyberocw.habittodosecretary.alaram;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;

import butterknife.ButterKnife;

/**
 * Created by cyber on 2017-03-12.
 */

public class AlarmExListAdapter extends BaseExpandableListAdapter implements AlarmListAdapterInterface{
    private AlarmDataManager mManager;
    private LayoutInflater inflater;
    private Context mCtx;
    private AlarmFragment mMainFragment;

    public AlarmExListAdapter(AlarmFragment mainFragment, Context ctx, AlarmDataManager mManager) {
        this.mMainFragment = mainFragment;
        this.mManager = mManager;
        mCtx = ctx;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return mManager.getGroupCount();
    }

    @Override
    public int getChildrenCount(int i) {
        return mManager.getGroup(i).size();
    }

    @Override
    public Object getGroup(int i) {
        return mManager.getGroupTitle(i);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mManager.getGroupItem(groupPosition, childPosition);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpended, View convertView, ViewGroup parent) {
        String groupName = mManager.getGroupTitle(groupPosition);

        if(convertView == null){
            convertView = inflater.inflate(R.layout.alarm_group_view, null);
        }

        TextView tvTitle = ButterKnife.findById(convertView, R.id.tvGroupTitle);
        tvTitle.setText(groupName);

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int position, boolean b, View convertView, ViewGroup parent) {
        final AlarmVO vo = mManager.getGroupItem(groupPosition, position);

        if(convertView == null){
            convertView = inflater.inflate(R.layout.alarm_view, parent, false);
            switch (vo.getAlarmOption()){
                case Const.ALARM_OPTION.SET_DATE_TIMER :
                    break;
                case Const.ALARM_OPTION.NO_DATE_TIMER :
                    break;
            }
        }

        //Const.ALARM_DATE_TYPE.POSTPONE_DATE

        if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.REPEAT)
            convertView.setBackgroundResource(R.color.background_repeat);
        else if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.SET_DATE)
            convertView.setBackgroundResource(R.color.background_date);
        else if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.POSTPONE_DATE)
            convertView.setBackgroundResource(R.color.background_postphone_date);
        else if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.REPEAT_MONTH)
            convertView.setBackgroundResource(R.color.background_repeat_day);


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
                    Toast.makeText(mCtx, "useYn 변환에 실패했습니다", Toast.LENGTH_SHORT).show();
                else {
                    mManager.resetMinAlarmCall(vo.getAlarmDateType());
                }
            }
        });

        String title = vo.getAlarmTitle();

        TextView tv = (TextView) convertView.findViewById(R.id.alarmTitle);
        tv.setText(title);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mMainFragment.expandGroupView();
    }
}
