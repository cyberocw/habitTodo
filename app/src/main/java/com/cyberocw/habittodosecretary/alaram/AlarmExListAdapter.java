package com.cyberocw.habittodosecretary.alaram;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.ui.RenderAlarmView;
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
    public boolean areAllItemsEnabled() {
        return true;
    }
    @Override
    public View getChildView(final int groupPosition, final int position, boolean b, View convertView, ViewGroup parent) {
        final AlarmVO vo = mManager.getGroupItem(groupPosition, position);

        if(convertView == null){
            convertView = inflater.inflate(R.layout.alarm_view, parent, false);
        }

        RenderAlarmView.RenderAlarmView(mCtx, mMainFragment, mManager, vo, convertView, Const.ALARM_LIST_VIEW_TYPE.EXPENDABLE_LIST, position);

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

    public void setRenderView(){

    }
}
