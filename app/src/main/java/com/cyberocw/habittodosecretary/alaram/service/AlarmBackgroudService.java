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
import com.cyberocw.habittodosecretary.alaram.receiver.AlarmReceiver;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmTimeVO;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.util.TTSNoti;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

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
    private int mPosition = -1;
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
            Log.d("service", "onBind with extra");
            //mMillisRemainTime = (Long) extras.get("realTime");
            long realTime = (Long) extras.get("realTime");
            int alarmDateType = (int) extras.get("alarmDateType");
            AlarmTimeVO alarmTimeVO = (AlarmTimeVO) intent.getSerializableExtra("alarmTimeVO");

            int index = findAlarmIndex(alarmTimeVO);

            if(index == -1)
                mArrAlarmVOList.add(alarmTimeVO);
        }
        setMaxReaminTime();
        startTimer();

        return START_REDELIVER_INTENT;
        //return super.onStartCommand(intent, flags, startId);
    }
    public void setMaxReaminTime(){
        mMillisRemainTime = -1;
        for(int i = 0 ; i < mArrAlarmVOList.size(); i++){
            if(mArrAlarmVOList.get(i).getTimeStamp() > mMillisRemainTime) {
               mMillisRemainTime = mArrAlarmVOList.get(i).getTimeStamp();
            }
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
        startTimer(mMillisRemainTime);
    }

    public void startTimer(long remainTime) {
        if (mCountDownTimer != null)
            return;

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

                Log.d(Const.DEBUG_TAG, "on tinck =" + second);

                if (mTv != null) {
                    mTv.setText(mNumberFormat.format(hour) + ":" + mNumberFormat.format(minute) +
                            ":" + mNumberFormat.format(second));
                }
            }

            public void onFinish() {
                if (mTv != null) {
                    mTv.setText(mNumberFormat.format(0) + ":" + mNumberFormat.format(0) +
                            ":" + mNumberFormat.format(0));
                }
                startAleart();
                cancelTimer();
                mCountDownTimer = null;
            }
        }.start();
    }

    private void startAleart() {
        Intent ttsIntent = new Intent(mCtx, TTSNoti.class);
        ttsIntent.putExtra("alaramTitle", mTitle);
        mCtx.startService(ttsIntent);
    }

    public void cancelTimer() {
        mMillisRemainTime = -1;
        mCountDownTimer.cancel();
        //stopForeground(true);
        stopSelf();

    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(Const.DEBUG_TAG, "onTaskRemoved Service");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(Const.DEBUG_TAG, "onDestroy Service");
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }


}
