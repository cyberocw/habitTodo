package com.cyberocw.habittodosecretary.record;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import butterknife.ButterKnife;

/**
 * Created by cyber on 2017-07-24.
 */

public class RecorderCustomView extends LinearLayout {
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

    RecordAudio recordTask;
    PlayAudio playTask;
    File recordingFile;

    boolean isRecording = false,isPlaying = false;

    private boolean isRecord = false;
    private CountDownTimer mCountDownTimer = null;

    private recordDialogInterface mListener;

    public RecorderCustomView(Context context) {
        super(context);
        mCtx = context;
        init();
    }

    public RecorderCustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;
        init();
    }

    public RecorderCustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCtx = context;
        init();
    }
    private void bindEvent(){
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay();

            }
        });
        mBtnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
            }
        });
    }
    private void init() {
        Log.d(this.toString(), " init start  mPlayFileName=" + mPlayFileName);

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        mView =  li.inflate(R.layout.fragment_dialog_record, this, false);

        addView(mView);

        if(mCtx == null || mCtx.getExternalCacheDir() == null)
            return;
        mFileName = mCtx.getExternalCacheDir().getAbsolutePath();
        mFileName += File.separator + Const.RECORDER.CACHE_FILE_NAME;
        mBtnRecord = ButterKnife.findById(mView, R.id.btnRecord);
        mBtnStart = ButterKnife.findById(mView, R.id.btnPlay);
        mBtnStop = ButterKnife.findById(mView, R.id.btnStop);
        //mBtnRemove = ButterKnife.findById(mView, R.id.btnRemove);
        mTvTime = ButterKnife.findById(mView, R.id.tvTime);
        mTvRecording = ButterKnife.findById(mView, R.id.tvRecording);

        initRecorder();

        bindEvent();

        initUi();
    }

    private void initRecorder(){
        File path = new File(mFileName);
        recordingFile = path;
        /*try {
            //recordingFile = path;//File.createTempFile("recording", ".pcm", path);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create file on SD card", e);
        }*/

    }

    private void initUi(){
        //기존 파일 세팅
        mBtnStop.setVisibility(GONE);
        mTvRecording.setVisibility(View.GONE);
        mTvTime.setVisibility(View.GONE);
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
            Toast.makeText(mCtx, getResources().getString(R.string.customRecorder_playing_voice), Toast.LENGTH_SHORT).show();
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

    private void startPlaying3() {
        if(mPlayer == null)
            mPlayer = new MediaPlayer();
        try {
            Log.d(Const.DEBUG_TAG, "mPlayFileName="+mPlayFileName);

            mPlayer.setDataSource(mPlayFileName);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
    private void stopPlaying3() {
        mStartPlaying = true;
        mPlayer.release();
        refreshPlayButton();
        mPlayer = null;
        mTvTime.setVisibility(View.GONE);
    }

    private void startPlaying() {
        playTask = new PlayAudio();
        playTask.execute();
        countRecord(true, getDuration(new File(mPlayFileName)));
        mTvRecording.setText("playing....");

        mTvTime.setVisibility(View.VISIBLE);
        mTvRecording.setVisibility(View.VISIBLE);

        mStartPlaying = !mStartPlaying;
        refreshPlayButton();
    }
    private void stopPlaying() {
        mStartPlaying = true;
        isPlaying = false;
        //mPlayer.release();
        refreshPlayButton();
        cancelTimer();
        mPlayer = null;
        mTvTime.setVisibility(View.GONE);
        mTvRecording.setVisibility(GONE);
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
        CommonUtils.logCustomEvent("startRecording", "1");
        File path = new File(mFileName);
        if(path.isFile()) {
            boolean delResult = path.delete();
            Log.d(this.toString(), "delResult=" + delResult);
            try {
                path.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        recordTask = new RecordAudio();
        recordTask.execute();
        mTvRecording.setText("recording....");
        mTvRecording.setVisibility(View.VISIBLE);
        countRecord(false, 10000);
        refreshRecordButton();
        mStartRecording = !mStartRecording;
    }

    private void stopRecording() {
        //mRecorder.stop();
        cancelTimer();
        isRecording = false;

        mTvRecording.setVisibility(View.GONE);
        mTvTime.setVisibility(View.GONE);
        //녹음 된 파일 등록 - mPlayFileName
        setRecordFile(mFileName);
        isRecord = true;
        refreshRecordButton();
        mStartRecording = true;
        initUi();
    }

    private void countRecord(final boolean isPlay, long duration){
        mTvTime.setVisibility(View.VISIBLE);
        if(duration == 0)
            duration = 10000;

        Log.d(this.toString(), "duration = " + duration);
        final int finalDuration = (int) (duration / 1000) % 60;;
        mCountDownTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                mMillisRemainTime = millisUntilFinished;
                int second = (int) (millisUntilFinished / 1000) % 60;
                Log.d(Const.DEBUG_TAG, "remainTimeText="+CommonUtils.numberDigit(2, finalDuration  - second) + " / " + CommonUtils.numberDigit(2, finalDuration));
                if(isPlay){

                    mTvTime.setText(CommonUtils.numberDigit(2, finalDuration - second) + " / " + CommonUtils.numberDigit(2, finalDuration));
                }else
                    mTvTime.setText(CommonUtils.numberDigit(2, second));
            }
            public void onFinish() {
                mTvTime.setVisibility(View.GONE);
                //녹음일때 자동 정치 클릭
                if(!isPlay)
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
        isRecord = false;
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        isPlaying = false;
        if (mPlayer != null) {

            mPlayer.release();
            mPlayer = null;
        }
    }
    public void finish(){

    }

    @Override
    public boolean isInEditMode() {
        return false;
    }

    public interface recordDialogInterface{
        public void onDialogPositiveClick(boolean isRecord);
        public void onDialogNegativeClick();
    }

    private long getDuration(File file) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Log.d(this.toString(), "durationStr="+durationStr);
        if(durationStr != null) {
            return Long.parseLong(durationStr);
        }else
            return 0;
    }

    private class RecordAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isRecording = true;
            try {
                DataOutputStream dos = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(
                                recordingFile)));
                int bufferSize = AudioRecord.getMinBufferSize(Const.RECORDER.FREQUENCY,
                        Const.RECORDER.CHANNEL_CONFIGURATION_IN, Const.RECORDER.AUDIO_ENCODING);
                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, Const.RECORDER.FREQUENCY,
                        Const.RECORDER.CHANNEL_CONFIGURATION_IN, Const.RECORDER.AUDIO_ENCODING, bufferSize);

                short[] buffer = new short[bufferSize];
                audioRecord.startRecording();
                double gain = 5.0;
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0,
                            bufferSize);
                    for (int i = 0; i < bufferReadResult; i++) {
                        short s = buffer[i];
                        int increased = (int) (s * gain);
                        s = (short) Math.min(Math.max(increased, Short.MIN_VALUE), Short.MAX_VALUE);
                        dos.writeShort(s);
                    }
                }
                audioRecord.stop();
                dos.close();

                String newFile = recordingFile.getAbsolutePath();

                newFile = newFile.replace("pcm", "wav");
                Log.d(this.toString(), "newFile="+newFile);
                File targetFile = new File(newFile);


                RawToWave r = new RawToWave(recordingFile, targetFile);
                r.run();

                mPlayFileName = targetFile.getAbsolutePath();

            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }
    }

    private class PlayAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isPlaying = true;

            if(mPlayFileName == null){
                return null;
            }

            Log.d(this.toString(), " audiotrack  mPlayFileName="+ mPlayFileName);
            File fPlay = new File(mPlayFileName);

            if(!fPlay.isFile()) {
                Toast.makeText(mCtx, "파일을 찾을수 없습니다", Toast.LENGTH_SHORT).show();
                return null;
            }
            int bufferSize = AudioTrack.getMinBufferSize(Const.RECORDER.FREQUENCY,Const.RECORDER.CHANNEL_CONFIGURATION_OUT, Const.RECORDER.AUDIO_ENCODING);
            short[] audiodata = new short[bufferSize / 4];

            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fPlay)));
                AudioTrack audioTrack = new AudioTrack(
                        AudioManager.STREAM_MUSIC, Const.RECORDER.FREQUENCY,
                        Const.RECORDER.CHANNEL_CONFIGURATION_OUT, Const.RECORDER.AUDIO_ENCODING, bufferSize,
                        AudioTrack.MODE_STREAM);

                audioTrack.play();
                while (isPlaying && dis.available() > 0) {
                    int i = 0;
                    while (dis.available() > 0 && i < audiodata.length) {
                        audiodata[i] = dis.readShort();
                        i++;
                    }
                    audioTrack.write(audiodata, 0, audiodata.length);
                }
                dis.close();

            } catch (Throwable t) {
                Log.e("AudioTrack", "Playback Failed");
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            stopPlaying();
        }
    }


}
