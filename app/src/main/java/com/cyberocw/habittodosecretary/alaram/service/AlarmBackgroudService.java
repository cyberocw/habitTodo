package com.cyberocw.habittodosecretary.alaram.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.alaram.receiver.AlarmReceiver;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmTimeVO;
import com.cyberocw.habittodosecretary.util.TTSNoti;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by cyberocw on 2016-09-19.
 * service 및 alarm notibar 모두 중단 시키는 메서드 구현
 * start 시 위 메서드 호출 후 다시 등록하는 루틴 실행
 * 이 서비스는 한번에 한개만 돌아감
  작업 큐에서 max remainTime을 가져와서 ramain time을 돌면서 큐에 들어있는 것을 체크하고 알림 울림
   로직 상 10분 이상 점검하는 일은 생기지 않도록 만들기 -- 로직상 이러한 일은 생길 수 없도록10분 이내 다른 알림이 있는 경우들 외에는
 생길 수 없음!!
 -추가- cancel은 어떻게 할지 고민 필요
 */
public class AlarmBackgroudService extends Service {
    public ArrayList<AlarmTimeVO> mArrAlarmVOList = new ArrayList<AlarmTimeVO>();
    public long mMillisRemainTime = -1;
    public TextView mTv;
    NumberFormat mNumberFormat = new DecimalFormat("##00");
    /**
     * Created by cyberocw on 2015-11-16.
     */
    private Context mCtx = this;
    private CountDownTimer mCountDownTimer = null;
    private int mMinRemainPosition = -1;
    private String mTitle = "";

    private AlarmReceiver mTimerListAdapter = null;

    public AlarmBackgroudService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Const.DEBUG_TAG, "onStartCommand" + " mCountdownTimer is null=" + (mCountDownTimer == null));
        //if(mCountDownTimer != null)
        //	mTimerListAdapter.showRunningAlert();
        Bundle extras = intent.getExtras();

        if(extras == null){
            Log.d("service", " extras null!!!!");
        }

        // Get messager from the Activity
        if (extras != null) {
            Log.d("service", "onBind with extra @@@@@@@@@@@@");
            //mMillisRemainTime = (Long) extras.get("realTime");

            AlarmTimeVO alarmTimeVO = (AlarmTimeVO) intent.getSerializableExtra("alarmTimeVO");

            int index = findAlarmIndex(alarmTimeVO);

            if(index == -1) {
                mArrAlarmVOList.add(alarmTimeVO);
                setMinReaminTime();
                startTimer();
            }
        }


        return START_REDELIVER_INTENT;
        //return super.onStartCommand(intent, flags, startId);
    }
    public void setMinReaminTime(){
        mMillisRemainTime = -1;
        mMinRemainPosition = -1;
        if(mArrAlarmVOList == null)
            return;
        for(int i = 0 ; i < mArrAlarmVOList.size(); i++){
            if(i == 0){
                mMinRemainPosition = 0;
                mMillisRemainTime = mArrAlarmVOList.get(i).getTimeStamp();
            }

            if(mArrAlarmVOList.get(i).getTimeStamp() < mMillisRemainTime) {
               mMillisRemainTime = mArrAlarmVOList.get(i).getTimeStamp();
                mMinRemainPosition = i;
            }
        }

        if(mMillisRemainTime > -1){
            mMillisRemainTime = mMillisRemainTime - Calendar.getInstance().getTimeInMillis();
        }
    }
    public int findAlarmIndex(AlarmTimeVO alarmTimeVO){
        int index = -1;
        for(int i = 0 ; i < mArrAlarmVOList.size(); i++){
            if(alarmTimeVO.getId() == mArrAlarmVOList.get(i).getId()) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void startTimer() {
        if(mMillisRemainTime == -1){
            stopForeground(true);
            stopSelf();
        }
        startTimer(mMillisRemainTime, mArrAlarmVOList.get(mMinRemainPosition));
    }

    public void startTimer(long remainTime, AlarmTimeVO alarmTimeVO) {
        if (mCountDownTimer != null)
            return;
        int callTime = alarmTimeVO.getCallTime();

        mTitle = alarmTimeVO.getAlarmTitle() + " " + (callTime < 0 ? callTime + "분 전" : (callTime > 0 ? callTime + "분 후" : ""));
        Notification notification = new Notification(R.drawable.ic_launcher, "타이머", System.currentTimeMillis());

        int second = (int) (remainTime / 1000) % 60;
        int minute = (int) ((remainTime / (1000 * 60)) % 60);
        int hour = (int) ((remainTime / (1000 * 60 * 60)));

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, Const.ONGOING_TIMER_NOTI_ID, notificationIntent, 0);

        notification.setLatestEventInfo(this, "HbitTodo Timer is running",
                mNumberFormat.format(hour) + ":" + mNumberFormat.format(minute) +
                        ":" + mNumberFormat.format(second), pendingIntent);
        startForeground(Const.ONGOING_TIMER_NOTI_ID, notification);

        mCountDownTimer = new CountDownTimer(remainTime, 1000) {
            public void onTick(long millisUntilFinished) {
                mMillisRemainTime = millisUntilFinished;
                int second = (int) (millisUntilFinished / 1000) % 60;
                int minute = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                int hour = (int) ((millisUntilFinished / (1000 * 60 * 60)));

                //Log.d(Const.DEBUG_TAG, "on tinck =" + second);
            }

            public void onFinish() {
                Log.d("Service", "on tinck finish");
                startAleart();
                cancelTimer();

            }
        }.start();
    }

    private void startAleart() {
        Intent myIntent = new Intent(mCtx, NotificationService.class);
        myIntent.putExtra("title", mTitle);
        myIntent.putExtra("notes", "");
        myIntent.putExtra("reqCode", mArrAlarmVOList.get(mMinRemainPosition).getId());
        Log.d("Service", "start noti ");

        mCtx.startService(myIntent);

        Intent ttsIntent = new Intent(mCtx, TTSNoti.class);
        ttsIntent.putExtra("alaramTitle", mTitle);
        mCtx.startService(ttsIntent);
    }

    public void cancelTimer() {
        mMillisRemainTime = -1;
        if(mCountDownTimer != null)
            mCountDownTimer.cancel();
        mArrAlarmVOList.remove(mMinRemainPosition);
        mCountDownTimer = null;

        Log.d("Service", "remove and mArrAlarmVOList.size() == " + mArrAlarmVOList.size());
        if(mArrAlarmVOList.size() > 0){
            setMinReaminTime();
            startTimer();
        }
        else {
            //setMinAlarm 호출해서 다시 등록 루틴 타야함
            AlarmDataManager mAlarmDataManager = new AlarmDataManager(mCtx, Calendar.getInstance());
            mAlarmDataManager.resetMinAlarmCall();
            stopForeground(true);
            stopSelf();
        }
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(Const.DEBUG_TAG, "onTaskRemoved Service");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(Const.DEBUG_TAG, "onDestroy Service");
        if(mCountDownTimer != null)
            mCountDownTimer.cancel();
        stopForeground(true);
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }


}
