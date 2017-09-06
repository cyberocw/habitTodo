package com.cyberocw.habittodosecretary.keyword;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.WebViewActivity;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.alaram.vo.TimerVO;
import com.cyberocw.habittodosecretary.calendar.CalendarDialog;
import com.cyberocw.habittodosecretary.keyword.ui.KeywrordCalendarDialog;
import com.cyberocw.habittodosecretary.keyword.vo.KeywordVO;
import com.cyberocw.habittodosecretary.util.CommonUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import belka.us.androidtoggleswitch.widgets.ToggleSwitch;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
        MainActivity.pushActionBarInfo(R.string.nav_item_keyword, false);

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
        mPrefs = mCtx.getSharedPreferences(Const.KEYWORD.PARAM.PREFS, Context.MODE_PRIVATE);
        initActivity();
        Fabric.with(mCtx, new Crashlytics());
        CommonUtils.logCustomEvent("KeywordFragment", "1");

    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    private void initActivity(){
        mKeywordDataManager = new KeywordDataManager(mCtx);
        mKeywordAdapter = new KeywordListAdapter( mCtx, mKeywordDataManager);

        ListView lv = (ListView) mView.findViewById(R.id.keywordListView);
        lv.setAdapter(mKeywordAdapter);
        lv.setOnItemClickListener(new ListViewItemClickListener());

        mToggleSwitch = ButterKnife.findById(mView, R.id.toggleKeywordType);
        String viewMode = mPrefs.getString(Const.KEYWORD.PARAM.VIEW_MODE, Const.KEYWORD.API.MODE.SUM);
        mToggleSwitch.setCheckedTogglePosition(getTogglePosition(viewMode));
        mPrefs.edit().remove(Const.KEYWORD.PARAM.VIEW_MODE);

        mSimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");

        mKeywordAPI = new KeywordAPI(mCtx, mKeywordDataManager, mKeywordAdapter);

        if(mCalendar.get(Calendar.MINUTE) < 30)
            mCalendar.set(Calendar.MINUTE, 0);
        else{
            mCalendar.set(Calendar.MINUTE, 30);
        }

        mTvDate = ButterKnife.findById(mView, R.id.dateView);

        getData();

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
        Button option = ButterKnife.findById(mView, R.id.btnSetting);
        option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mCtx, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.keyword, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.menu_sum_portal :
                                showSumPortal();
                                return true;
                            case R.id.menu_time_portal :
                                showTimePortal();
                                return true;
                        }
                        return false;
                    }
                });
                popup.show();
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
    private int getTogglePosition(String mode){

        if(mode.equals(Const.KEYWORD.API.MODE.TIME))
            return 0;
        else
            return 1;
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //time 이 누적임
    private void showTimePortal(){
        final String items[] = { Const.KEYWORD.PARAM.NAVER, Const.KEYWORD.PARAM.DAUM, Const.KEYWORD.PARAM.ZUM };
        AlertDialog.Builder ab = new AlertDialog.Builder(mCtx);
        ab.setTitle("누적 순위 검색 엔진");
        String timePortal = mPrefs.getString(Const.KEYWORD.PARAM.TIME_PORTAL_KEY, "");
        int selectedIndex = 0;
        for(int i = 0; i < items.length; i++){
            if(items[i].equals(timePortal)){
                selectedIndex = i;
                break;
            }
        }

        ab.setSingleChoiceItems(items, selectedIndex,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(Const.DEBUG_TAG, "whichButton="+whichButton);
                        SharedPreferences.Editor editor = mPrefs.edit();
                        editor.putString(Const.KEYWORD.PARAM.TIME_PORTAL_KEY, items[whichButton]);
                        editor.commit();
                        dialog.dismiss();
                    }
                });
        ab.show();
    }

    private void showSumPortal(){
        final String items[] = {"해당 키워드 순위가 높은 검색엔진" ,  "누적 순위 엔진과 동일"};
        AlertDialog.Builder ab = new AlertDialog.Builder(mCtx);
        ab.setTitle("시점 순위 링크 검색 엔진");
        String sumPortal = mPrefs.getString(Const.KEYWORD.PARAM.SUM_PORTAL_KEY, "");
        int selectedIndex = 0;

        if(sumPortal.equals(Const.KEYWORD.PARAM.DEFAULT)){
            selectedIndex = 1;
        }

        ab.setSingleChoiceItems(items, selectedIndex,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        SharedPreferences.Editor editor = mPrefs.edit();
                        if(whichButton == 0)
                            editor.putString(Const.KEYWORD.PARAM.SUM_PORTAL_KEY, Const.KEYWORD.PARAM.TOP_RANK);
                        else
                            editor.putString(Const.KEYWORD.PARAM.SUM_PORTAL_KEY, Const.KEYWORD.PARAM.DEFAULT);


                        editor.commit();
                        dialog.dismiss();
                    }
                });
        ab.show();
    }

    @Override
    public void onDestroy() {
        MainActivity.popActionbarInfo();
        super.onDestroy();
    }

    public interface RefreshKeyword{
        void refreshKeyword();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    public class ListViewItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?query=" + mKeywordDataManager.getItem(position).getKeyword()));
            String keyword;
            try {
                keyword = URLEncoder.encode(mKeywordDataManager.getItem(position).getKeyword(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                keyword = mKeywordDataManager.getItem(position).getKeyword();
            }

            String url ; //+ keyword;

            if(getToggleMode().equals(Const.KEYWORD.API.MODE.TIME) || mPrefs.getString(Const.KEYWORD.PARAM.SUM_PORTAL_KEY, Const.KEYWORD.PARAM.TOP_RANK).equals(Const.KEYWORD.PARAM.DEFAULT)) {
                String sumPortal = mPrefs.getString(Const.KEYWORD.PARAM.TIME_PORTAL_KEY, Const.KEYWORD.PARAM.NAVER);
                switch (sumPortal) {
                    case Const.KEYWORD.PARAM.DAUM:
                        url = "https://m.search.daum.net/search?w=tot&q=";
                        break;
                    case Const.KEYWORD.PARAM.ZUM:
                        url = "http://m.search.zum.com/search.zum?method=uni&query=";
                        break;
                    default:
                        url = "https://m.search.naver.com/search.naver?query=";
                        break;
                }
                url += keyword;
                //mCtx.getApplicationContext().startActivity(intent);
            }
            else{
                KeywordVO vo = mKeywordDataManager.getItem(position);
                int naver = 20, daum = 20, zum = 20;
                if(!TextUtils.isEmpty(vo.getRankNAVER()))
                    naver = Integer.parseInt(vo.getRankNAVER());
                if(!TextUtils.isEmpty(vo.getRankDAUM()))
                    daum = Integer.parseInt(vo.getRankDAUM());
                if(!TextUtils.isEmpty(vo.getRankZUM()))
                    zum = Integer.parseInt(vo.getRankZUM());

                Log.d(Const.DEBUG_TAG, "naver =" + naver + " daum=" + daum + " zum="+zum);

                if(daum < naver && daum <= zum)
                    url = "https://m.search.daum.net/search?w=tot&q=";
                else if(zum < naver && zum <= daum)
                    url = "http://m.search.zum.com/search.zum?method=uni&query=";
                else
                    url = "https://m.search.naver.com/search.naver?query=";

                url += keyword;
            }
             openWebView(url);

        }
    }
}
