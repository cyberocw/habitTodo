package com.cyberocw.habittodosecretary.record;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class RecorderDialog extends DialogFragment {
    private Context mCtx;
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;
    private View mView;
    private TextView mTvRecording, mTvTime;
    private MediaRecorder mRecorder = null;
    private Button mBtnRecord, mBtnStop, mBtnStart;
    private MediaPlayer mPlayer = null;
    private boolean mStartPlaying = true, mStartRecording = true;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    long mMillisRemainTime;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private boolean isRecord = false;
    private CountDownTimer mCountDownTimer = null;

    private recordDialogInterface mListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }
    public void setContext(Context ctx){
        mCtx = ctx;
    }
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            Toast.makeText(mCtx, "Play Failed", Toast.LENGTH_SHORT).show();
        }
        mTvTime.setVisibility(View.VISIBLE);
    }
    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
        mTvTime.setVisibility(View.GONE);
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            Toast.makeText(mCtx, "Record Error", Toast.LENGTH_SHORT).show();
        }
        mTvRecording.setVisibility(View.VISIBLE);
        countRecord();
        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        mTvRecording.setVisibility(View.GONE);
        mTvTime.setVisibility(View.GONE);

        isRecord = true;

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        mView = inflater.inflate(R.layout.fragment_dialog_record, null);
        builder.setView(mView).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onDialogPositiveClick(isRecord);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        //mListener.onDialogNegativeClick(NoticeDialogFragment.this);
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    public void setListener(recordDialogInterface l){
        mListener = l;

    }

    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
           /* View view = inflater.inflate(R.layout.fragment_dialog_record, container, false);
            mView = view;*/
            // Record to the external cache directory for visibility
            mFileName = mCtx.getExternalCacheDir().getAbsolutePath();
            mFileName += File.separator + Const.RECORDER.CACHE_FILE_NAME;

            ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);

            mBtnRecord = ButterKnife.findById(mView, R.id.btnRecord);
            mBtnStart = ButterKnife.findById(mView, R.id.btnPlay);
            mBtnStop = ButterKnife.findById(mView, R.id.btnStop);
            mTvTime = ButterKnife.findById(mView, R.id.tvTime);
            mTvRecording = ButterKnife.findById(mView, R.id.tvRecording);

            bindEvent();
            return mView;
        }

        private void bindEvent(){
            mBtnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPlay(mStartPlaying);
                    if (mStartPlaying) {
                        //setText("Stop playing");
                    } else {
                        //setText("Start playing");
                    }
                    mStartPlaying = !mStartPlaying;
                }
            });
            mBtnRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecord(mStartRecording);
                    if (mStartRecording) {
                        //setText("Stop recording");
                    } else {
                        //setText("Start recording");
                    }
                    mStartRecording = !mStartRecording;
                }
            });
        }

        @Override
        public void onStop() {
            super.onStop();
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
