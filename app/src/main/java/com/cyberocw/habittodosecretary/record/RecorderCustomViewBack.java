package com.cyberocw.habittodosecretary.record;

import android.Manifest;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.io.File;
import java.io.IOException;

import butterknife.ButterKnife;

/**
 * Created by cyber on 2017-07-24.
 */

public class RecorderCustomViewBack extends LinearLayout {
    private Context mCtx;
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String mFileName = null, mPlayFileName = null;
    private View mView;
    private TextView mTvRecording, mTvTime;
    private MediaRecorder mRecorder = null;
    private Button mBtnRecord, mBtnStop, mBtnStart, mBtnRemove;
    private MediaPlayer mPlayer = null;
    private boolean mStartPlaying = true, mStartRecording = true;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    long mMillisRemainTime;

    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private boolean isRecord = false;
    private CountDownTimer mCountDownTimer = null;

    private recordDialogInterface mListener;

    public RecorderCustomViewBack(Context context) {
        super(context);
        mCtx = context;
        init();
    }

    public RecorderCustomViewBack(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;
        init();
    }

    public RecorderCustomViewBack(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCtx = context;
        init();
    }
    private void bindEvent(){
        mBtnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay();

            }
        });
        mBtnRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
            }
        });
    }
    private void init() {
       /* View view = inflater.inflate(R.layout.fragment_dialog_record, container, false);
        mView = view;*/
        // Record to the external cache directory for visibility

        Log.d(this.toString(), " init start  mPlayFileName=" + mPlayFileName);

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        mView =  li.inflate(R.layout.fragment_dialog_record, this, false);

        addView(mView);

        mFileName = mCtx.getExternalCacheDir().getAbsolutePath();
        mFileName += File.separator + Const.RECORDER.CACHE_FILE_NAME;
        mBtnRecord = ButterKnife.findById(mView, R.id.btnRecord);
        mBtnStart = ButterKnife.findById(mView, R.id.btnPlay);
        mBtnStop = ButterKnife.findById(mView, R.id.btnStop);
        //mBtnRemove = ButterKnife.findById(mView, R.id.btnRemove);
        mTvTime = ButterKnife.findById(mView, R.id.tvTime);
        mTvRecording = ButterKnife.findById(mView, R.id.tvRecording);
        bindEvent();

        initUi();
    }
    private void initUi(){
        //기존 파일 세팅
        mBtnStop.setVisibility(GONE);

        if(mPlayFileName != null){
            mBtnStart.setVisibility(VISIBLE);
            //mBtnRemove.setVisibility(VISIBLE);
        }else{
            mBtnStart.setVisibility(GONE);
            //mBtnRemove.setVisibility(GONE);
        }
    }

    private void onRecord(boolean start) {
        if(mStartPlaying == false){
            Toast.makeText(mCtx, "음성을 재생중입니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay() {
        if(mStartRecording == false){
            Toast.makeText(mCtx, "녹음중입니다", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mStartPlaying) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        if(mPlayer == null)
            mPlayer = new MediaPlayer();
        try {
            Log.d(Const.DEBUG_TAG, "mPlayFileName="+mPlayFileName);

            mPlayer.setDataSource(mPlayFileName);
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    int total = mPlayer.getDuration();
                    Log.d(this.toString(), "duration = " + total);
                    mp.start();
                    mTvTime.setVisibility(View.VISIBLE);
                    mStartPlaying = !mStartPlaying;
                    refreshPlayButton();
                }
            });
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });
            mPlayer.prepareAsync();

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            Toast.makeText(mCtx, "Play Failed", Toast.LENGTH_SHORT).show();
        }
    }
    private void stopPlaying() {
        mStartPlaying = true;
        mPlayer.release();
        refreshPlayButton();
        mPlayer = null;
        mTvTime.setVisibility(View.GONE);
    }

    public boolean isRecord(){
        return isRecord;
    }

    public String getFilePath(){
        return mPlayFileName;
    }
    public void setRecordFile(String path){
        Log.d(this.toString(), "setRecordFile = " + path);
        mPlayFileName = path;
        initUi();
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioEncodingBitRate(64000);
        mRecorder.setAudioSamplingRate(16000);

        try {
            mRecorder.prepare();
            mTvRecording.setVisibility(View.VISIBLE);
            countRecord();
            refreshRecordButton();
            mStartRecording = !mStartRecording;
            mRecorder.start();

        } catch (IOException e) {
            Log.e(LOG_TAG, "recorder prepare() failed");
            Toast.makeText(mCtx, "Record Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        mRecorder.stop();
        cancelTimer();
        mRecorder.release();
        mRecorder = null;
        mTvRecording.setVisibility(View.GONE);
        mTvTime.setVisibility(View.GONE);
        setRecordFile(mFileName);
        isRecord = true;

        refreshRecordButton();
        mStartRecording = true;
        initUi();
    }

    private void countRecord(){
        mTvTime.setVisibility(View.VISIBLE);

        mCountDownTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                mMillisRemainTime = millisUntilFinished;
                int second = (int) (millisUntilFinished / 1000) % 60;
                int minute = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                int hour = (int) ((millisUntilFinished / (1000 * 60 * 60)));

                //Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "on tinck =" + second);
                mTvTime.setText(CommonUtils.numberDigit(2, second));
            }
            public void onFinish() {
                mTvTime.setVisibility(View.GONE);
                mBtnRecord.callOnClick();
                cancelTimer();
                mCountDownTimer = null;
            }
        }.start();
    }

    private void cancelTimer(){
        if(mCountDownTimer != null){
            mCountDownTimer.cancel();
        }
    }

    public void setListener(recordDialogInterface l){
        mListener = l;

    }

    private void refreshPlayButton(){
        if(!mStartPlaying)
            mBtnStart.setBackground(AppCompatResources.getDrawable(mCtx, R.drawable.ic_stop_black_24dp));
        else
            mBtnStart.setBackground(AppCompatResources.getDrawable(mCtx, R.drawable.ic_play_circle_filled_black_24dp));
    }
    private void refreshRecordButton(){
        if (mStartRecording) {
            //setText("Stop recording");
            mBtnRecord.setBackground(AppCompatResources.getDrawable(mCtx, R.drawable.ic_stop_black_24dp));
        } else {
            mBtnRecord.setBackground(AppCompatResources.getDrawable(mCtx, R.drawable.ic_mic_black_24dp));
            //setText("Start recording");
        }
    }

        public void onStop() {
            if (mRecorder != null) {
                mRecorder.release();
                mRecorder = null;
            }

            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
        }
        public void finish(){

        }

        public interface recordDialogInterface{
            public void onDialogPositiveClick(boolean isRecord);
            public void onDialogNegativeClick();
        }
}
