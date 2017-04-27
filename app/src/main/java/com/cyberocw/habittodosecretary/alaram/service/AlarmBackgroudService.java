package com.cyberocw.habittodosecretary.alaram.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.alaram.receiver.AlarmReceiver;
import com.cyberocw.habittodosecretary.alaram.ui.AlarmNotiActivity;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmTimeVO;
import com.cyberocw.habittodosecretary.util.CommonUtils;
import com.cyberocw.habittodosecretary.util.TTSNotiActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import io.fabric.sdk.android.Fabric;

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
    private int mAlarmOption = -1;
    private int mAlarmType = -1;
    private final String mAppTitle = "HabitTodoSecretary";

    private AlarmReceiver mTimerListAdapter = null;

    public AlarmBackgroudService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Crashlytics.log(Log.DEBUG, this.toString(), "onStartCommand" + " mCountdownTimer is null=" + (mCountDownTimer == null));
        //if(mCountDownTimer != null)
        //	mTimerListAdapter.showRunningAlert();
        Bundle extras = intent.getExtras();

        CommonUtils.putLogPreference(mCtx, this.toString() + "background service start");

        if(extras == null){
            Crashlytics.log(Log.DEBUG, this.toString(), "extras null!!!");
        }

        // Get messager from the Activity
        if (extras != null) {
            Crashlytics.log(Log.DEBUG, "service", "onBind with extra @@@@@@@@@@@@ mArrAlarmVOList size=" + mArrAlarmVOList.size());
            //mMillisRemainTime = (Long) extras.get("realTime");

            Set<String> keySet = extras.keySet();
            StringBuilder sb = new StringBuilder();
            for (String key: keySet
                    ) {
                sb.append(key + "\n");
            }
            Crashlytics.log(Log.DEBUG, this.toString(), " extara keys = " + sb.toString());

            AlarmTimeVO alarmTimeVO = (AlarmTimeVO) intent.getSerializableExtra("alarmTimeVO");

            Crashlytics.log(Log.DEBUG, this.toString(), "alarmTimeVO = "+alarmTimeVO);

            CommonUtils.putLogPreference(mCtx, this.toString() + "alarmTimeVO = "+alarmTimeVO);

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

    public void startTimer(long remainTime, AlarmTimeVO alarmTimeVO){

        if(mCountDownTimer != null)
            return ;

        int callTime = alarmTimeVO.getCallTime();

        mTitle = alarmTimeVO.getAlarmTitle() + " " + (callTime < 0 ? callTime + "분 전" : (callTime > 0 ? callTime + "분 후" : ""));

        int second = (int) (remainTime / 1000) % 60;
        int minute = (int) ((remainTime / (1000 * 60)) % 60);
        int hour = (int) ((remainTime / (1000 * 60 * 60)));

        mAlarmOption = alarmTimeVO.getAlarmOption();
        mAlarmType = alarmTimeVO.getAlarmType();

        SharedPreferences prefs = getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
        boolean isBackg = prefs.getBoolean(Const.SETTING.IS_BACKGROUND_NOTI_USE, true);

        if(isBackg) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, Const.ONGOING_ALARM_NOTI_ID, notificationIntent, 0);

            android.support.v4.app.NotificationCompat.Builder mCompatBuilder = new android.support.v4.app.NotificationCompat.Builder(this);
            mCompatBuilder.setSmallIcon(R.drawable.ic_launcher);
            mCompatBuilder.setTicker("Habit Todo Timer");
            mCompatBuilder.setWhen(System.currentTimeMillis());
            //mCompatBuilder.setVibrate(new long[] { 100L, 100L, 200L, 200L, 300L, 300L, 400L, 400L });
            mCompatBuilder.setContentTitle(mAppTitle);
            mCompatBuilder.setContentText(mTitle + " 알림 예정 ");
            //mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
            mCompatBuilder.setContentIntent(pendingIntent);
            //mCompatBuilder.setAutoCancel(true);

            startForeground(Const.ONGOING_TIMER_NOTI_ID, mCompatBuilder.build());
        }

        startCountDownTimer(remainTime);
    }

    private void startCountDownTimer(long remainTime){
        // 음수면 바로 울림
        mCountDownTimer = new CountDownTimer(remainTime, 1000) {
            public void onTick(long millisUntilFinished) {
                mMillisRemainTime = millisUntilFinished;

                int second = (int) (millisUntilFinished / 1000) % 60;
                int minute = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                int hour = (int) ((millisUntilFinished / (1000 * 60 * 60)));

                if(second % 30 == 0) {
                    Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "on tinck =" + hour + " hour " + minute + " minute " + second + " second");
                    CommonUtils.putLogPreference(mCtx, this.toString() + "on tinck =" + hour + " hour " + minute + " minute " + second + " second");
                }
            }
            public void onFinish() {
                Crashlytics.log(Log.DEBUG, "Service", "on tinck finish");
                CommonUtils.putLogPreference(mCtx, this.toString() + "on tinck finish");
                startAleart();
                cancelTimer();

            }
        }.start();
    }

    //단순 알림, 끌때까지 울리는 알림
    private void startAleart() {

        SharedPreferences prefs = getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
        boolean isTTS = prefs.getBoolean(Const.SETTING.IS_TTS_NOTI, true);

        if(mAlarmType < 1) {
            Intent myIntent = new Intent(mCtx, NotificationService.class);
            Crashlytics.log(Log.DEBUG, this.toString(), " background mArrAlarmVOList.get(mMinRemainPosition).getReqCode() = " + mArrAlarmVOList.get(mMinRemainPosition).getReqCode());
            myIntent.putExtra("title", mTitle);
            myIntent.putExtra(Const.PARAM.ETC_TYPE_KEY, mArrAlarmVOList.get(mMinRemainPosition).getEtcType());
            myIntent.putExtra(Const.PARAM.REQ_CODE, mArrAlarmVOList.get(mMinRemainPosition).getReqCode());
            myIntent.putExtra(Const.PARAM.ALARM_ID, mArrAlarmVOList.get(mMinRemainPosition).getfId());
            mCtx.startService(myIntent);

        }else{
            Intent myIntent = new Intent(mCtx, AlarmNotiActivity.class);
            myIntent.putExtra("title", mTitle);
            myIntent.putExtra(Const.PARAM.ETC_TYPE_KEY, mArrAlarmVOList.get(mMinRemainPosition).getEtcType());
            myIntent.putExtra(Const.PARAM.ALARM_ID, mArrAlarmVOList.get(mMinRemainPosition).getfId());
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            mCtx.startActivity(myIntent);
        }

        if(mAlarmOption == 1 && isTTS) {
            startTTS(mTitle, mArrAlarmVOList.get(mMinRemainPosition).getfId());
        }
    }

    private void startTTS(String title, long id){
        Intent ttsIntent = new Intent(mCtx, TTSNotiActivity.class);
        ttsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        ttsIntent.putExtra("alaramTitle", title);
        ttsIntent.putExtra("alarmId", id);
        mCtx.startActivity(ttsIntent);
    }

    public void cancelTimer() {
        mMillisRemainTime = -1;
        if(mCountDownTimer != null)
            mCountDownTimer.cancel();
        mArrAlarmVOList.remove(mMinRemainPosition);
        mCountDownTimer = null;

        Crashlytics.log(Log.DEBUG, "Service", "remove and mArrAlarmVOList.size() == " + mArrAlarmVOList.size());
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
        Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "onTaskRemoved Service");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "onDestroy Service");
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
