package com.cyberocw.habittodosecretary.alaram.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
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
 * Created by cyber on 2017-09-26.
 */

public class ReminderService extends Service {
    private Context mCtx = this;
    private Handler mHandler = null;
    private final IBinder mBinder = new ReminderService.LocalBinder();
    private AlarmDataManager mAlarmDataManager = null;
    private ArrayList<AlarmVO> mArrayList = new ArrayList<>();

    public class LocalBinder extends Binder {
        public ReminderService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ReminderService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "onStartCommand ");
        if(mAlarmDataManager == null){
            mAlarmDataManager = new AlarmDataManager(this);
        }
        if(intent == null) {
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        Bundle bundle =  intent.getExtras();
        long alarmId = bundle.getLong(Const.PARAM.ALARM_ID);
        String mode = bundle.getString(Const.PARAM.MODE, "ADD");
        int reqCode = bundle.getInt(Const.PARAM.REQ_CODE, 0);
        int callTime = bundle.getInt(Const.PARAM.CALL_TIME, 0);
        int repeatDayId = bundle.getInt(Const.PARAM.REPEAT_DAY_ID, 0);
        Log.d(this.toString(), "mode = " +mode + " alarmId="+alarmId);
        if(mode.equals("RESET")){
            stopALL();
            return super.onStartCommand(intent, flags, startId);
        }
        if(mode.equals("CLOSE")){
            for(int i =  0; i < mArrayList.size(); i++){
                if(mArrayList.get(i).getId() == alarmId){
                    mArrayList.remove(i);
                    break;
                }
            }
            AlarmVO vo = mAlarmDataManager.getItemByIdInDB(alarmId);

            if(vo == null) {
                Toast.makeText(mCtx, "not found alarm", Toast.LENGTH_SHORT).show();
                Crashlytics.log(Log.DEBUG, this.toString(), "not found alarm alarmId = "+alarmId);
            }
            //날짜 지정
            else if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.SET_DATE){
                vo.setUseYn(0);
                mAlarmDataManager.modifyUseYn(vo);
            }
            //반복 알람
            else {
                SharedPreferences prefsReminder = mCtx.getSharedPreferences(Const.REMINDER.PREFS_ID, Context.MODE_PRIVATE);
                //완료 flag
                SharedPreferences.Editor editor = prefsReminder.edit();
                Log.d(this.toString(), "reminder key id=" + CommonUtils.getReminderDayId(Calendar.getInstance(), repeatDayId, alarmId));
                editor.putBoolean(CommonUtils.getReminderDayId(Calendar.getInstance(), repeatDayId, alarmId), true);
                //year month weekno dayid 조합으로 키를 만들고 지우는 로직을 별도로 만들지 -> 주기적으로 제거 해줘야 함 언제? 업그레이드 시 year, month 기준 제거;
                //key를 weekno 로 하고 values 를 컬렉션 행태로 만들지 -> close때마다 이전 한주씩 제거 -> 문자를 쪼개는 과정이 매번 생김 ;
                editor.commit();
            }
            mAlarmDataManager.resetMinAlarm();

            if(mArrayList.size() == 0){
                stopSelf();
                return super.onStartCommand(intent, flags, startId);
            }
            refreshNotibar(reqCode, mode);
            return super.onStartCommand(intent, flags, startId);
        }else if(mode.equals("NEXT")){
            refreshNotibar(reqCode + 1, mode);
            return super.onStartCommand(intent, flags, startId);

        }else if(mode.equals("PREV")){
            refreshNotibar(reqCode - 1, mode);
            return super.onStartCommand(intent, flags, startId);
        }

        AlarmVO vo = mAlarmDataManager.getItemByIdInDB(alarmId);
        vo.setRepeatDayId(repeatDayId);
        ArrayList<Integer> callList = vo.getAlarmCallList();
        int index = -1;

        boolean hasMid = false;
        if(callList.size() > 2)
            hasMid = true;

        Log.d(this.toString(), "mArrayList size="+ mArrayList.size() ) ;
        boolean isRunning = false;
        for(int i =  0; i < mArrayList.size(); i++){
            if(mArrayList.get(i).getId() == alarmId){
                isRunning = true;
                index = i;
                break;
            }
        }

        if(mode.equals("REFRESH")){
            if(!isRunning)
                mArrayList.add(vo);
        }
        //첫 알람, 중간 알람 -> 진행중인 알람이 아닐 경우 add
        else if(callTime == callList.get(0) || (callTime == callList.get(1) && hasMid)){
            if(!isRunning)
                mArrayList.add(vo);
        }

        //끝 알람
        else {
            Log.d(this.toString(), "끝 알람 " + vo.getId() + "  title=" + vo.getAlarmTitle());
            Intent myIntent = new Intent(mCtx, NotificationService.class);
            myIntent.putExtra("title", vo.getAlarmTitle());
            myIntent.putExtra(Const.PARAM.ALARM_REMINDER_MODE, Const.ALARM_REMINDER_MODE.ALARM);
            myIntent.putExtra(Const.PARAM.ETC_TYPE_KEY, vo.getEtcType());
            myIntent.putExtra(Const.PARAM.REQ_CODE, 500000 + index);
            myIntent.putExtra(Const.PARAM.ALARM_ID, vo.getId());
            myIntent.putExtra(Const.PARAM.ALARM_OPTION_TO_SOUND, vo.getAlarmOption());

            mCtx.startService(myIntent);

            for(int i =  0; i < mArrayList.size(); i++){
                if(mArrayList.get(i).getId() == alarmId){
                    startSound(vo);
                    mArrayList.remove(i);
                    mode = "REFRESH";

                    break;
                }
            }
            if(mArrayList.size() == 0){
                //startSound(vo);
                stopSelf();
                return super.onStartCommand(intent, flags, startId);
            }
        }
        refreshNotibar(index, mode);
        return super.onStartCommand(intent, flags, startId);
    }

    private void refreshNotibar(int index, String mode){
        Log.d(this.toString(), "mArrayList after size="+ mArrayList.size() + " get index="+index) ;

        if(mArrayList.size() == 0){
            mAlarmDataManager.resetMinAlarm();
            stopALL();
            return;
        }
        if(index == -1)
            index = mArrayList.size()-1;
        else if(index >= mArrayList.size()){
            index = 0;
        }
        AlarmVO lastVO = mArrayList.get(index);

        //String txt = TextUtils.join(", ", mArrayList);

        notiReminder2(lastVO, index, mode);
    }

    public void notiReminder2(AlarmVO vo, int index, String mode){
        SharedPreferences prefs = getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
        boolean isAlarmNoti = prefs.getBoolean(Const.SETTING.IS_ALARM_NOTI, true);
        CommonUtils.logCustomEvent("reminderService", "1");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, Const.ONGOING_REMINDER_NOTI_ID, notificationIntent, 0);

        NotificationCompat.Builder mCompatBuilder = new NotificationCompat.Builder(this);

        mCompatBuilder.setSmallIcon(R.drawable.ic_stat_noti);
        mCompatBuilder.setTicker("Habit Todo");

        RemoteViews remoteView = new RemoteViews(this.getPackageName(), R.layout.alarm_notification);
        remoteView.setOnClickPendingIntent(R.id.notiWrap, pendingIntent);

        //title 생성

        remoteView.setTextViewText(R.id.tvAlarmTitle, "(" + (index+1) + "/" + mArrayList.size() + ") " + vo.getAlarmTitle());

        if(isAlarmNoti && mode.equals("ADD")) {
            if(vo.getAlarmOption() != Const.ALARM_OPTION_TO_SOUND.VIBRATION)
                mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND);
            mCompatBuilder.setVibrate(new long[] { 100L, 100L, 200L, 200L, 100L, 100L, 100L, 100L, 100L, 100L});
        }
        remoteView.setImageViewResource(R.id.ivNoti, R.drawable.ic_stat_noti);

        //완료처리 버튼
        Intent closeButtonIntent = new Intent(this, ReminderService.CloseButtonListener.class);
        closeButtonIntent.putExtra(Const.PARAM.MODE, "CLOSE");
        closeButtonIntent.putExtra(Const.PARAM.REQ_CODE, index);
        closeButtonIntent.putExtra(Const.PARAM.ALARM_ID, vo.getId());
        closeButtonIntent.putExtra(Const.PARAM.REPEAT_DAY_ID, vo.getRepeatDayId());

        PendingIntent pendingCloseButtonIntent = PendingIntent.getBroadcast(this, 1, closeButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.btnClose, pendingCloseButtonIntent);

        //이전 버튼
        Intent prevButtonIntent = new Intent(this, ReminderService.CloseButtonListener.class);
        prevButtonIntent.putExtra(Const.PARAM.MODE, "PREV");
        prevButtonIntent.putExtra(Const.PARAM.REQ_CODE, index);
        prevButtonIntent.putExtra(Const.PARAM.ALARM_ID, vo.getId());
        PendingIntent pendingMoveButtonIntent = PendingIntent.getBroadcast(this, 2, prevButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ibLeft, pendingMoveButtonIntent);
        //다음 버튼
        Intent nextButtonIntent = new Intent(this, ReminderService.CloseButtonListener.class);
        nextButtonIntent.putExtra(Const.PARAM.MODE, "NEXT");
        nextButtonIntent.putExtra(Const.PARAM.REQ_CODE, index);
        nextButtonIntent.putExtra(Const.PARAM.ALARM_ID, vo.getId());
        PendingIntent pendingMoveButtonIntent2 = PendingIntent.getBroadcast(this, 3, nextButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ibRight, pendingMoveButtonIntent2);


        //mCompatBuilder.setCustomContentView(remoteView);
        mCompatBuilder.setContent(remoteView);
        Log.d(this.toString(), "start foreground");
        startForeground(Const.ONGOING_REMINDER_NOTI_ID, mCompatBuilder.build());
        if(mode.equals("ADD"))
            startSound(vo);
    }

    private void startSound(AlarmVO vo){
        SharedPreferences prefs = getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
        boolean isTTS = prefs.getBoolean(Const.SETTING.IS_TTS_NOTI, true);
        boolean isTTSManner = prefs.getBoolean(Const.SETTING.IS_TTS_NOTI_MANNER, true);

        if (isTTS && !isTTSManner) {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            switch (am.getRingerMode()) {
                case AudioManager.RINGER_MODE_SILENT:
                case AudioManager.RINGER_MODE_VIBRATE:
                    isTTS = false;
                    break;
                case AudioManager.RINGER_MODE_NORMAL:
                    break;
            }
        }

        //mAlarmOption - 개별 알람 tts , background service랑 동일 로직 있음!!
        if (vo.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.TTS && isTTS) {
            startTTS(vo.getAlarmTitle(), vo.getId());
        } else if (vo.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD && isTTS) {
            //String fileName = CommonUtils.getRecordFullPath(mCtx, mArrAlarmVOList.get(mMinRemainPosition).getfId());
            FileDataManager fdm = new FileDataManager(mCtx);
            fdm.makeDataList(Const.ETC_TYPE.ALARM, vo.getId());

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
                    startTTS(vo.getAlarmTitle(), vo.getId());
                }
            } catch (Exception e) {
                startTTS(vo.getAlarmTitle(), vo.getId());
                e.printStackTrace();
            }
        }
    }
    private void startTTS(String title, long id){
        Intent ttsIntent = new Intent(getApplicationContext(), TTSNoti.class);
        Log.d(this.toString(), "startTTS title= " + title);
        ttsIntent.putExtra("alaramTitle", title);
        ttsIntent.putExtra("alarmId", id);
        getApplicationContext().startService(ttsIntent);
    }
    @Override
    public void onCreate() {
        Fabric.with(mCtx, new Crashlytics());
        super.onCreate();

        Crashlytics.log(Log.DEBUG, this.toString(), "timerservice wakeLock acquire");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Bundle extras = intent.getExtras();
        Crashlytics.log(Log.DEBUG, "service", "onBind");
        // Get messager from the Activity
        if (extras != null) {
            Crashlytics.log(Log.DEBUG, "service", "onBind with extra");
//            mPosition = (Integer) extras.get("position");
        }
        return mBinder;
    }
    @Override
    public void onRebind(Intent intent) {
        Toast.makeText(ReminderService.this, "on rebind", Toast.LENGTH_SHORT).show();
        super.onRebind(intent);
    }
    public void stopALL(){
        mArrayList.clear();
        stopForeground(true);
    }
    public static class CloseButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "close button on receive bundle=" + intent.getExtras().get(Const.PARAM.MODE));

            Bundle bundle = intent.getExtras();
            if(bundle != null){
                Intent myIntent = new Intent(context, ReminderService.class);
                myIntent.putExtras(bundle);
                context.startService(myIntent);

//                NotificationManager manager = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
//                int reqCode = bundle.getInt(Const.PARAM.REQ_CODE);
//                Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "reqCode="+reqCode);
//                manager.cancel(reqCode);
            }
        }
    }
}
