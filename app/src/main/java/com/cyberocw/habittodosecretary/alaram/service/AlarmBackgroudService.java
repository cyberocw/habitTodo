package com.cyberocw.habittodosecretary.alaram.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.alaram.AlarmNotiActivity;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmTimeVO;
import com.cyberocw.habittodosecretary.common.vo.FileVO;
import com.cyberocw.habittodosecretary.file.FileDataManager;
import com.cyberocw.habittodosecretary.record.PlayRawAudio;
import com.cyberocw.habittodosecretary.util.CommonUtils;
import com.cyberocw.habittodosecretary.util.TTSNoti;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

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

    /**
     * Created by cyberocw on 2015-11-16.
     */
    private Context mCtx = this;
    private CountDownTimer mCountDownTimer = null;
    private int mMinRemainPosition = -1;
    private String mTitle = "";
    private int mCallTime = 0;
    private int mAlarmOption = -1;
    private int mAlarmType = -1;
    private String mAppTitle = "";
    private Handler mHandler = null;
    private static PowerManager.WakeLock mCpuWakeLock;
    private static boolean isScreenLock;

    private PowerManager pm;
    private PowerManager.WakeLock wakeLock;

    public AlarmBackgroudService() {
    }
    @Override
    public void onCreate() {
        Fabric.with(this, new Crashlytics());
        pm = ((PowerManager)getSystemService(Context.POWER_SERVICE));
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "backgroundService");
        // wakelock 사용

        wakeLock.acquire();

        //startForeground();
        Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "wakeLock acquire");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Crashlytics.log(Log.DEBUG, this.toString(), "onStartCommand" + " mCountdownTimer is null=" + (mCountDownTimer == null));
        fakeStartForeground();
        //if(mCountDownTimer != null)
        //	mTimerListAdapter.showRunningAlert();
        mAppTitle = getApplicationContext().getResources().getString(R.string.app_name);
        Bundle extras = intent.getExtras();
        CommonUtils.putLogPreference(mCtx, this.toString() + "background service start");
        if(extras == null){
            Crashlytics.log(Log.DEBUG, this.toString(), "extras null!!!");
        }
        // Get messager from the Activity
        if (extras != null) {
            Crashlytics.log(Log.DEBUG, "service", "onBind with extra @@@@@@@@@@@@ mArrAlarmVOList size=" + mArrAlarmVOList.size());
            AlarmTimeVO alarmTimeVO = (AlarmTimeVO) intent.getSerializableExtra(Const.PARAM.ALARM_TIME_VO);

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
            Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "startimer stopForeground");
            stopForeground(true);
            stopSelf();
        }
        startTimer(mMillisRemainTime, mArrAlarmVOList.get(mMinRemainPosition));
    }

    public void startTimer(long remainTime, AlarmTimeVO alarmTimeVO){

        if(mCountDownTimer != null)
            return ;

        startCountDownTimer(remainTime);

        int callTime = mCallTime = alarmTimeVO.getCallTime();
        int h = Math.abs(callTime / 60);
        int m = Math.abs(callTime % 60);

        //reminder는 몇분전 이런거 없음
        if(alarmTimeVO.getAlarmReminderType() == Const.ALARM_REMINDER_MODE.REMINDER){
            mTitle = alarmTimeVO.getAlarmTitle();
        }
        else
            mTitle = alarmTimeVO.getAlarmTitle() + " " + (h != 0 ? h + getString(R.string.hours) + " " : "") +
                        (callTime < 0 ?  m + getString(R.string.dialog_alarm_minute_before) : callTime > 0 ?  m + getString(R.string.dialog_alarm_minute_after) : "" );
        mAlarmOption = alarmTimeVO.getAlarmOption();
        mAlarmType = alarmTimeVO.getAlarmType();

        SharedPreferences prefs = getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
        boolean isBackg = true;// = prefs.getBoolean(Const.SETTING.IS_BACKGROUND_NOTI_USE, true);

        Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "is backg="+isBackg);
        if(isBackg) {
            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
            notificationIntent.setAction(Intent.ACTION_MAIN);
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), Const.ONGOING_ALARM_NOTI_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar alertTime = Calendar.getInstance();
            alertTime.setTimeInMillis(alarmTimeVO.getTimeStamp());

            String notiName = mTitle + " " + alertTime.get(Calendar.HOUR_OF_DAY) + ":" + alertTime.get(Calendar.MINUTE) + " " + getString(R.string.service_alarm_scheduled);

            NotificationCompat.Builder mCompatBuilder = new NotificationCompat.Builder(this, Const.CHANNEL.SILENT_ID);
            mCompatBuilder.setSmallIcon(R.drawable.ic_stat_noti)
            .setTicker(getResources().getString(R.string.app_name))
            .setColor
                    (ContextCompat.getColor(this, R.color.blue))
            .setWhen(System.currentTimeMillis())
            .setContentTitle(mAppTitle)
            .setVibrate(null)
            .setSound(null)
            .setContentText(notiName)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW);

            if(alarmTimeVO.getAlarmDateType() == Const.ALARM_DATE_TYPE.POSTPONE_DATE){
                Intent intentCancel = new Intent(this, MainActivity.class);
                intentCancel.putExtra(Const.PARAM.ALARM_ID, alarmTimeVO.getId());
                intentCancel.putExtra(Const.PARAM.MODE, Const.ALARM_INTERFACE_CODE.ALARM_CANCEL);
                intentCancel.putExtra(Const.PARAM.REQ_CODE, alarmTimeVO.getReqCode());
                intentCancel.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY);
                PendingIntent pendingIntentCancel = PendingIntent.getActivity(this, 0, intentCancel, PendingIntent.FLAG_UPDATE_CURRENT);
                mCompatBuilder.addAction(R.drawable.ic_add_alert_black_24dp, getString(R.string.alarm_noti_cancel), pendingIntentCancel);
            }
            Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "start foreground");
            startForeground(Const.ONGOING_TIMER_NOTI_ID, mCompatBuilder.build());
        }
    }

    private void startCountDownTimer(long millisRemainTime){
        if(mHandler == null)
            mHandler = new Handler();
        mHandler.postDelayed(new Runnable()
        {
            @Override public void run()
            {
                startAleart();
                cancelTimer();
            }
        }, millisRemainTime);
    }

    private void startCountDownTimer2(long remainTime){
        // 음수면 바로 울림
        final Calendar ccc = Calendar.getInstance();
        mCountDownTimer = new CountDownTimer(remainTime, 1000) {
            public void onTick(long millisUntilFinished) {
                //mMillisRemainTime = millisUntilFinished;

                int second = (int) (millisUntilFinished / 1000) % 60;
                int minute = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                //int hour = (int) ((millisUntilFinished / (1000 * 60 * 60)));
                //사실 30초 이상 될 일 없도록 만들었음

                if(second % 30 == 0) {
                    Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "on tinck =" + minute + " minute " + second + " second");
                    CommonUtils.putLogPreference(mCtx, this.toString() + "on tinck =" + minute + " minute " + second + " second");
                }
                // 9초 맞춰서 알림이 들어오기 때문에 최소 10 이상이어야 함
                else if(minute == 0 && second <= 10){
                    if(second == 12) {
                        long timeStamp = mArrAlarmVOList.get(mMinRemainPosition).getTimeStamp();
                        ccc.setTimeInMillis(timeStamp);
                        Calendar nowCal = Calendar.getInstance();
                        //1초 이상 오차나면 다시 남은시간 계산해도 돌림
                        if (Math.abs(ccc.getTimeInMillis() - nowCal.getTimeInMillis() - millisUntilFinished) > 1000) {
                            mCountDownTimer.cancel();
                            startCountDownTimer(ccc.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
                        }
                    }
                    Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "on tinck =" + minute + " minute " + second + " second");
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
        //음성 녹음일때 정각이 아닌 안내는 TTS 재생으로 강제 설정
        changeAlarmOptionRecorderToTTS();

        AlarmTimeVO vo = mArrAlarmVOList.get(mMinRemainPosition);
        int call = vo.getCallTime();
        //미리 알림일 경우, 미리알림 옵션을 적용
        if(call != 0){
            if(vo.getAlarmCallType() == 0){
                startNotibar();
            }
            else{
                alarmNotiActivity();
            }
            return;
        }
        //정시 알림일 경우, 정시 알림 옵션 적용
        if(mAlarmType < 1)
            startNotibar();
        else
            alarmNotiActivity();
    }

    private void startNotibar(){
        Log.d(this.toString(), "mArrAlarmVOList.get(mMinRemainPosition).getAlarmReminderType()=" + mArrAlarmVOList.get(mMinRemainPosition).getAlarmReminderType());
        Intent myIntent;
        if(mArrAlarmVOList.get(mMinRemainPosition).getAlarmReminderType() == Const.ALARM_REMINDER_MODE.REMINDER){
            myIntent = new Intent(mCtx, ReminderService.class);
        }
        else {
            myIntent = new Intent(mCtx, NotificationService.class);
        }
        Crashlytics.log(Log.DEBUG, this.toString(), " background alarmId= " + mArrAlarmVOList.get(mMinRemainPosition).getfId() + " title = "+ mArrAlarmVOList.get(mMinRemainPosition).getAlarmTitle() + " mTitle=" + mTitle);
        myIntent.putExtra("title", mTitle);
        myIntent.putExtra(Const.PARAM.ALARM_REMINDER_MODE, mArrAlarmVOList.get(mMinRemainPosition).getAlarmReminderType());
        myIntent.putExtra(Const.PARAM.ETC_TYPE_KEY, mArrAlarmVOList.get(mMinRemainPosition).getEtcType());
        myIntent.putExtra(Const.PARAM.REQ_CODE, mArrAlarmVOList.get(mMinRemainPosition).getReqCode());
        myIntent.putExtra(Const.PARAM.ALARM_ID, mArrAlarmVOList.get(mMinRemainPosition).getfId());
        myIntent.putExtra(Const.PARAM.CALL_TIME, mCallTime);
        myIntent.putExtra(Const.PARAM.REPEAT_DAY_ID, mArrAlarmVOList.get(mMinRemainPosition).getRepeatDayId());
        myIntent.putExtra(Const.PARAM.ALARM_OPTION, mArrAlarmVOList.get(mMinRemainPosition).getAlarmOption());

        if (mArrAlarmVOList.get(mMinRemainPosition).getAlarmReminderType() == Const.ALARM_REMINDER_MODE.REMINDER && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mCtx.startForegroundService(myIntent);
        }
        else{
            mCtx.startService(myIntent);
        }

        if(!(mArrAlarmVOList.get(mMinRemainPosition).getAlarmReminderType() == Const.ALARM_REMINDER_MODE.REMINDER)) {
            SharedPreferences prefs = getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
            boolean isTTS = prefs.getBoolean(Const.SETTING.IS_TTS_NOTI, true);
            boolean isTTSManner = prefs.getBoolean(Const.SETTING.IS_TTS_NOTI_MANNER, true);

            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if(am.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
                isTTS = false;
            if (isTTS && !isTTSManner) {
                switch (am.getRingerMode()) {
                    case AudioManager.RINGER_MODE_SILENT:
                    case AudioManager.RINGER_MODE_VIBRATE:
                        isTTS = false;
                        break;
                    case AudioManager.RINGER_MODE_NORMAL:
                        break;
                }
            }
            //mAlarmOption - 개별 알람 tts reminderservice에도 동일 로직 있음!!
            if (mAlarmOption == Const.ALARM_OPTION_TO_SOUND.TTS && isTTS) {
                startTTS(mTitle, mArrAlarmVOList.get(mMinRemainPosition).getfId());
            } else if (mAlarmOption == Const.ALARM_OPTION_TO_SOUND.RECORD && isTTS) {
                //String fileName = CommonUtils.getRecordFullPath(mCtx, mArrAlarmVOList.get(mMinRemainPosition).getfId());
                FileDataManager fdm = new FileDataManager(mCtx);
                fdm.makeDataList(Const.ETC_TYPE.ALARM, mArrAlarmVOList.get(mMinRemainPosition).getfId());

                try {
                    ArrayList<FileVO> arrFile = fdm.getDataList();
                    FileVO fileVO = arrFile.get(0);
                    File f = new File(fileVO.getUriPath());
                    Log.d(this.toString(), "absolute=" + f.getAbsolutePath() + " getPaht= " + f.getPath());

                    if (f.isFile()) {
                        PlayRawAudio pra = new PlayRawAudio(mCtx, f.getAbsolutePath());
                        pra.execute();
                    } else {
                        Toast.makeText(mCtx, getResources().getString(R.string.msg_file_not_found), Toast.LENGTH_SHORT).show();
                        startTTS(mTitle, mArrAlarmVOList.get(mMinRemainPosition).getfId());
                    }
                } catch (Exception e) {
                    startTTS(mTitle, mArrAlarmVOList.get(mMinRemainPosition).getfId());
                    e.printStackTrace();
                }
            }
        }
    }
    private void alarmNotiActivity(){
        Intent myIntent = new Intent(mCtx, AlarmNotiActivity.class);
        myIntent.putExtra("title", mTitle);
        myIntent.putExtra(Const.PARAM.ALARM_OPTION, mAlarmOption);
        myIntent.putExtra(Const.PARAM.ETC_TYPE_KEY, mArrAlarmVOList.get(mMinRemainPosition).getEtcType());
        myIntent.putExtra(Const.PARAM.ALARM_ID, mArrAlarmVOList.get(mMinRemainPosition).getfId());
        //myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK );
        myIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        Crashlytics.log(Log.DEBUG, this.toString(), "start alarmNotiActivity mTitle=" + mTitle);

        mCtx.startActivity(myIntent);
    }

    private void changeAlarmOptionRecorderToTTS(){
        if(mAlarmOption == Const.ALARM_OPTION_TO_SOUND.RECORD){
            AlarmTimeVO vo = mArrAlarmVOList.get(mMinRemainPosition);
            int call = vo.getCallTime();
            if(call != 0){
                mAlarmOption = Const.ALARM_OPTION_TO_SOUND.TTS;
            }
        }
    }

    private void startTTS(String title, long id){
        /*Intent ttsIntent = new Intent(mCtx, TTSNotiActivity.class);
        ttsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        ttsIntent.putExtra("alaramTitle", title);
        ttsIntent.putExtra("alarmId", id);
        mCtx.startActivity(ttsIntent);*/

        Intent ttsIntent = new Intent(getApplicationContext(), TTSNoti.class);
        ttsIntent.putExtra("alaramTitle", title);
        ttsIntent.putExtra("alarmId", id);
        getApplicationContext().startService(ttsIntent);
    }

    public void cancelTimer() {
        mMillisRemainTime = -1;
        if(mCountDownTimer != null)
            mCountDownTimer.cancel();
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
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
            Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "cancelTimer stopForeground");
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
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
        Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "ondestroy stopForeground");
        stopForeground(true);
        if(mHandler == null) {
            mHandler = new Handler();
        }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                        Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "wakeLock release");
                    }
                }
            }, 15000);

        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }
    private void fakeStartForeground() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, Const.CHANNEL.SILENT_ID)
                        .setContentTitle("")
                        .setContentText("");

        startForeground(Const.ONGOING_TIMER_NOTI_ID, builder.build());
    }

}
