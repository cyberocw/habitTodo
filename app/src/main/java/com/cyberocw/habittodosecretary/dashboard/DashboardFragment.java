package com.cyberocw.habittodosecretary.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.alaram.AlarmListAdapter;
import com.cyberocw.habittodosecretary.keyword.KeywordAPI;
import com.cyberocw.habittodosecretary.keyword.KeywordDataManager;
import com.cyberocw.habittodosecretary.keyword.KeywordListAdapter;
import com.cyberocw.habittodosecretary.memo.MemoDataManager;
import com.cyberocw.habittodosecretary.memo.MemoListAdapter;
import com.cyberocw.habittodosecretary.util.CommonUtils;
import com.cyberocw.habittodosecretary.util.PopMessageEvent;
import com.cyberocw.habittodosecretary.util.TitleMessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import butterknife.ButterKnife;

/**
 * Created by cyber on 2017-07-15.
 */

public class DashboardFragment extends android.support.v4.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ActionBar mActionBar = null;
    private View mView;
    private ScrollView mScrollView;
    private Context mCtx;
    private TextView mTvTime;
    SharedPreferences mPrefs;
    Calendar mCalendar;
    AlarmDataManager mAlarmDataManager;
    AlarmListAdapter mAlarmListAdapter;
    MemoDataManager mMemoDataManager;
    MemoListAdapter mMemoListAdapter;
    KeywordDataManager mKeywordDataManagerTime, mKeywordDataManagerSum;
    KeywordListAdapter mKeywordListAdapterTime, mKeywordListAdapterSum;
    KeywordAPI mKeywordAPI;
    ProgressBar mProgressKeyword, mProgressKeywordTime;

    private boolean isEtcMode;

    private AlertDialog mCatePopupBilder;
    private EditText mEtCateTitle;

    public DashboardFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_dashboard1, container, false);
        return mView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCtx = getActivity();
        EventBus.getDefault().post(new TitleMessageEvent(getString(R.string.nav_item_dashboard), false));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initActivity();
    }

    public void initActivity(){
        Crashlytics.log(Log.DEBUG, this.toString(), "init activtiy start");

        //mScrollView = ButterKnife.findById(mView, R.id.scrollView);

        mCalendar = Calendar.getInstance();

        mAlarmDataManager = new AlarmDataManager(mCtx, mCalendar);
        mAlarmDataManager.resetMinAlarm();
        mAlarmListAdapter = new AlarmListAdapter(mCtx, mAlarmDataManager);

        mMemoDataManager = new MemoDataManager(mCtx, -1l , 2);

        mMemoListAdapter = new MemoListAdapter(mCtx, mMemoDataManager);

        mKeywordDataManagerSum = new KeywordDataManager(mCtx, 3);
        mKeywordDataManagerTime = new KeywordDataManager(mCtx, 4);

        mKeywordListAdapterSum = new KeywordListAdapter( mCtx, mKeywordDataManagerSum, true);
        mKeywordListAdapterTime = new KeywordListAdapter( mCtx, mKeywordDataManagerTime, true);


        makeAlarmList();

        if(CommonUtils.isLocaleKo(getResources().getConfiguration())) {
            makeKeywordList();
        }else{
            ButterKnife.findById(mView, R.id.keywordWrap0).setVisibility(View.GONE);
            ButterKnife.findById(mView, R.id.keywordWrap1).setVisibility(View.GONE);
            ButterKnife.findById(mView, R.id.keywordWrap2).setVisibility(View.GONE);
            ButterKnife.findById(mView, R.id.keywordWrap3).setVisibility(View.GONE);
        }
        makeMemoList();

        ScrollView scrollView = ButterKnife.findById(mView, R.id.scrollView);
        scrollView.smoothScrollTo(0,0);

        /*scrollView.pageScroll(View.FOCUS_UP);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ready, move up
                scrollView.fullScroll(View.FOCUS_UP);
            }
        });
        */
        CommonUtils.logCustomEvent("DashboardFragment", "1");
    }

    public void makeAlarmList(){
        Crashlytics.log(Log.DEBUG, this.toString(), "makeAlarmList start");
        mAlarmDataManager.makeDataListDashboard();
        //알람이 없을 경우
        if(mAlarmDataManager.getCount() == 0) {
            TextView tv = ButterKnife.findById(mView, R.id.tvNoAlarm);
            ButterKnife.findById(mView, R.id.alarmListView).setVisibility(View.GONE);
            tv.setVisibility(View.VISIBLE);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).onNavigationItemSelected(R.id.nav_item_alaram);
                }
            });

        }else {
            ButterKnife.findById(mView, R.id.tvNoAlarm).setVisibility(View.GONE);

            ListView listView = ButterKnife.findById(mView, R.id.alarmListView);
            listView.setAdapter((ListAdapter) mAlarmListAdapter);
            listView.setClickable(true);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(Const.DEBUG_TAG, "position " + position + " id=" + id);
                    ((MainActivity) getActivity()).onNavigationItemSelected(R.id.nav_item_alaram);
                }
            });
        }
        TextView tv = ButterKnife.findById(mView, R.id.tvAlarmTitle);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).onNavigationItemSelected(R.id.nav_item_alaram);
            }
        });



    }
    public void makeMemoList(){
        if(mMemoDataManager.getCount() == 0){
            TextView tv = ButterKnife.findById(mView, R.id.tvNoMemo);
            tv.setVisibility(View.VISIBLE);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).onNavigationItemSelected(R.id.nav_item_memo);
                }
            });
            ButterKnife.findById(mView, R.id.memoListView).setVisibility(View.GONE);
        }
        else {
            ButterKnife.findById(mView, R.id.tvNoMemo).setVisibility(View.GONE);
            ListView listView = ButterKnife.findById(mView, R.id.memoListView);

            listView.setAdapter(mMemoListAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ((MainActivity) getActivity()).onNavigationItemSelected(R.id.nav_item_memo);
                }
            });
        }
    }
    public void makeKeywordList(){
        mTvTime = ButterKnife.findById(mView, R.id.tvTime);
        mProgressKeyword = ButterKnife.findById(mView, R.id.progressKeywordList);
        mProgressKeywordTime = ButterKnife.findById(mView, R.id.progressKeywordListTime);

        Button btnRefresh = ButterKnife.findById(mView, R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeKeywordListTime();
                makeKeywordListSum();
            }
        });

        makeKeywordListTime();
        makeKeywordListSum();
    }
    public void makeKeywordListTime(){
        ListView listViewTime = ButterKnife.findById(mView, R.id.keywordListViewTime);
        listViewTime.setAdapter(mKeywordListAdapterTime);
        listViewTime.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences prefs = mCtx.getSharedPreferences(Const.KEYWORD.PARAM.PREFS, Context.MODE_PRIVATE);
                prefs.edit().putString(Const.KEYWORD.PARAM.VIEW_MODE, Const.KEYWORD.API.MODE.TIME).commit();
                ((MainActivity) getActivity()).onNavigationItemSelected(R.id.nav_item_keyword);
            }
        });

        mKeywordListAdapterTime.setListVIew(listViewTime);
        getKeywordData(Const.KEYWORD.API.MODE.TIME, mKeywordDataManagerTime, mKeywordListAdapterTime, mProgressKeywordTime);
    }
    public void makeKeywordListSum(){
        ListView listViewSum = ButterKnife.findById(mView, R.id.keywordListViewSum);
        listViewSum.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences prefs = mCtx.getSharedPreferences(Const.KEYWORD.PARAM.PREFS, Context.MODE_PRIVATE);
                prefs.edit().putString(Const.KEYWORD.PARAM.VIEW_MODE, Const.KEYWORD.API.MODE.SUM).commit();
                ((MainActivity) getActivity()).onNavigationItemSelected(R.id.nav_item_keyword);
            }
        });

        listViewSum.setAdapter(mKeywordListAdapterSum);
        mKeywordListAdapterSum.setListVIew(listViewSum);
        getKeywordData(Const.KEYWORD.API.MODE.SUM, mKeywordDataManagerSum, mKeywordListAdapterSum, mProgressKeyword);
    }
    private void getKeywordData(String mode, KeywordDataManager dataManager, KeywordListAdapter adapter, ProgressBar progressBar){
        Calendar cal = Calendar.getInstance();
        if(cal.get(Calendar.MINUTE) < 30)
            cal.set(Calendar.MINUTE, 0);
        else{
            cal.set(Calendar.MINUTE, 30);
        }
        refreshDate(cal);
        int typeCode = 2;
        if(mode.equals(Const.KEYWORD.API.MODE.SUM)){
            typeCode = 1;
        }
        mKeywordAPI = new KeywordAPI(getContext(), dataManager, adapter, progressBar);
        String url = Const.KEYWORD.API.LIST + "?simpleDate=" + CommonUtils.convertKeywordSimpleDateType(cal) + "&typeCode=" + typeCode + "&mode=" + mode;
        Crashlytics.log(Log.DEBUG, this.toString(), " url = = " + url);
        //getData(url);
        //async task 여러번 실행되게 하는 법 찾아야 함
        mKeywordAPI.execute(url);
    }
    private void refreshDate(Calendar cal){
        mTvTime.setText(cal.get(Calendar.DAY_OF_MONTH) + "일 " + CommonUtils.numberDigit(2, cal.get(Calendar.HOUR_OF_DAY)) + ":" + CommonUtils.numberDigit(2, cal.get(Calendar.MINUTE)));
    }

    @Override
    public void onDestroy() {
        if(mKeywordAPI != null && mKeywordAPI.getStatus() == AsyncTask.Status.RUNNING)
            mKeywordAPI.cancel(true);
        //MainActivity.popActionbarInfo();
        EventBus.getDefault().post(new PopMessageEvent());
        super.onDestroy();
    }
}
