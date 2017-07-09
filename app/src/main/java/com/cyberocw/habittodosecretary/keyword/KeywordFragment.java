package com.cyberocw.habittodosecretary.keyword;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.WebViewActivity;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.alaram.vo.TimerVO;
import com.cyberocw.habittodosecretary.calendar.CalendarDialog;
import com.cyberocw.habittodosecretary.keyword.ui.KeywrordCalendarDialog;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import belka.us.androidtoggleswitch.widgets.ToggleSwitch;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

/**
 * Created by cyber on 2017-07-05.
 */

public class KeywordFragment extends Fragment {
    SharedPreferences mPrefs;
    private View mView;
    private Context mCtx;
    private Calendar mCalendar;
    KeywordDataManager mKeywordDataManager;
    KeywordListAdapter mKeywordAdapter;
    private SimpleDateFormat mSimpleDateFormat;
    ToggleSwitch mToggleSwitch;
    KeywordAPI mKeywordAPI;
    TextView mTvDate;

    public KeywordFragment(){

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_keyword, container, false);
        mCalendar = Calendar.getInstance();
/*
        if(!CommonUtils.isLocaleKo(getResources().getConfiguration()))
            mView.findViewById(R.id.holidayOptionWrap).setVisibility(View.GONE);
*/

        return mView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mCtx = getActivity();

        initActivity();
        Fabric.with(mCtx, new Crashlytics());
    }

    private void initActivity(){
        mKeywordDataManager = new KeywordDataManager(mCtx);
        mKeywordAdapter = new KeywordListAdapter( mCtx, mKeywordDataManager);

        ListView lv = (ListView) mView.findViewById(R.id.keywordListView);
        lv.setAdapter(mKeywordAdapter);
        lv.setOnItemClickListener(new ListViewItemClickListener());

        mToggleSwitch = ButterKnife.findById(mView, R.id.toggleKeywordType);


        mSimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");

        mKeywordAPI = new KeywordAPI(mCtx, mKeywordDataManager, mKeywordAdapter);

        if(mCalendar.get(Calendar.MINUTE) < 30)
            mCalendar.set(Calendar.MINUTE, 0);
        else{
            mCalendar.set(Calendar.MINUTE, 30);
        }

        mTvDate = ButterKnife.findById(mView, R.id.dateView);

        getData();

        mPrefs = mCtx.getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);

        bindBtnEvent();
    }
    private void bindBtnEvent(){
        mToggleSwitch.setOnToggleSwitchChangeListener(new ToggleSwitch.OnToggleSwitchChangeListener(){
            @Override
            public void onToggleSwitchChangeListener(int position, boolean isChecked) {
                getData();
            }
        });
        final Fragment targetFragment = this;
        mTvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeywrordCalendarDialog d = new KeywrordCalendarDialog();
                Bundle bundle = new Bundle();
                bundle.putSerializable("selectedDate", mCalendar);
                d.setArguments(bundle);
                d.show(getFragmentManager(), "calendarDialog");
                d.setTargetFragment(targetFragment, Const.ALARM_INTERFACE_CODE.SELECT_CALENDAR_DATE);
            }
        });

        Button btnPrevTime = ButterKnife.findById(mView, R.id.btnPrevTime);
        btnPrevTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendar.add(Calendar.MINUTE, -30);
                refreshDate();
            }
        });
        Button btnNextTime = ButterKnife.findById(mView, R.id.btnNextTime);
        btnNextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendar.add(Calendar.MINUTE, 30);
                if(mCalendar.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()){
                    mCalendar.add(Calendar.MINUTE, -30);

                    return;
                }
                refreshDate();
            }
        });
    }
    /*
        SUM - 해당 simpleDate 일치하는걸 sum해줌 > typeCode 는 1이어야 함

        TIME - SUM 안함 -> typeCode 2 가져올 때 사용해야 할듯 >
        누적+타임 조합은 TIME  typeCode 2 API사용
        실시간 + 타임 조합은 SUM - > typeCode 1 API 사용
     */
    private void refreshDate(){
        String strDate = CommonUtils.convertKeywordDateType(mCalendar);
        Crashlytics.log(Log.DEBUG, this.toString(), " strDate = = " + strDate +  "   mTvDate=" + mTvDate);

        mTvDate.setText(strDate);
    }
    private void getData(){

        if(mCalendar == null){
            mCalendar = Calendar.getInstance();
        }

        refreshDate();

        String mode = getToggleMode();
        int typeCode = 2;
        if(mode.equals(Const.KEYWORD.API.MODE.SUM)){
            typeCode = 1;
        }
        String url = Const.KEYWORD.API.LIST + "?simpleDate=" + CommonUtils.convertKeywordSimpleDateType(mCalendar) + "&typeCode=" + typeCode + "&mode=" + getToggleMode();
        Crashlytics.log(Log.DEBUG, this.toString(), " url = = " + url);
        //getData(url);
        //async task 여러번 실행되게 하는 법 찾아야 함
        mKeywordAPI = new KeywordAPI(getContext(), mKeywordDataManager, mKeywordAdapter);
        mKeywordAPI.execute(url);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case Const.KEYWORD.API.CALENDAR_INTERFACE_CODE:
                Calendar selectedDate = (Calendar) data.getExtras().getSerializable("selectedDate");
                //changeDate(selectedDate);
                mCalendar = selectedDate;
                getData();
                break;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    private String getToggleMode(){
        int position = mToggleSwitch.getCheckedTogglePosition();
        if(position == 0)
            return Const.KEYWORD.API.MODE.TIME;
        else
            return Const.KEYWORD.API.MODE.SUM;
    }

    private void changeDate(Calendar cal){
        mCalendar = cal;
        mTvDate.setText(CommonUtils.convertKeywordDateType(cal));
        String url = Const.KEYWORD.API.LIST + "?simpleDate=" + CommonUtils.convertKeywordSimpleDateType(cal) + "&typeCode=2&mode=" + getToggleMode();
        Crashlytics.log(Log.DEBUG, this.toString(), " url = = " + url);
        //getData(url);
    }

    public void openWebView(String url) {
        //WebViewDialog dialog = new WebViewDialog();
        Intent intent = new Intent(mCtx, WebViewActivity.class);

        Bundle bundle = new Bundle();

        bundle.putSerializable("url", url);
        intent.putExtras(bundle);

        startActivity(intent);
    }
    public interface RefreshKeyword{
        void refreshKeyword();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?query=" + mKeywordDataManager.getItem(position).getKeyword()));
            String url = "https://search.naver.com/search.naver?query=" + mKeywordDataManager.getItem(position).getKeyword();
            //mCtx.getApplicationContext().startActivity(intent);

            openWebView(url);


        }
    }
}
