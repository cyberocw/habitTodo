package com.cyberocw.habittodosecretary.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.util.TTSNoti;

import io.fabric.sdk.android.Fabric;

/**
 * Created by cyberocw on 2016-11-06.
 */
public class SettingFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    CheckBox mCbAllAlarm;
    private View mView;
    private Context mCtx;
    private SettingDataManager mSettingDataManager;
    private AlarmDataManager mAlarmDataManager = null;
    private SeekBar mSeekBar;
    private AudioManager mAudioManager;

    SharedPreferences mPrefs;

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance(Context param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_setting, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mCtx = getActivity();

        initActivity();
        Fabric.with(mCtx, new Crashlytics());
    }

    private void initActivity(){
        mSettingDataManager = new SettingDataManager(mCtx);

        mPrefs = mCtx.getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);

        bindBtnEvent();
    }

    private void bindBtnEvent(){
        Button btnHolidaySync = (Button) mView.findViewById(R.id.btnHolidaySync);
        Button btnDbBackup = (Button) mView.findViewById(R.id.btnDbBackup);
        Button btnDbRestore= (Button) mView.findViewById(R.id.btnDbRestore);
        mCbAllAlarm = (CheckBox) mView.findViewById(R.id.checkAllAlarm);
        final CheckBox cbBackgroundNoti = (CheckBox) mView.findViewById(R.id.checkBackgroundNoti);

        boolean isUseNotibar = mPrefs.getBoolean(Const.SETTING.IS_NOTIBAR_USE, true);
        boolean isBackgNoti = mPrefs.getBoolean(Const.SETTING.IS_BACKGROUND_NOTI_USE, true);



        if(isBackgNoti)
            cbBackgroundNoti.setChecked(true);

        if(isUseNotibar)
            mCbAllAlarm.setChecked(true);

        cbBackgroundNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAlarmDataManager == null)
                    mAlarmDataManager = new AlarmDataManager(mCtx);

                SharedPreferences.Editor editor = mPrefs.edit();
                if(cbBackgroundNoti.isChecked()){
                    editor.putBoolean(Const.SETTING.IS_BACKGROUND_NOTI_USE, true);

                }else{
                    editor.putBoolean(Const.SETTING.IS_BACKGROUND_NOTI_USE, false);
                }
                editor.commit();
                mAlarmDataManager.resetMinAlarm();
            }
        });

        mCbAllAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAlarmDataManager == null)
                    mAlarmDataManager = new AlarmDataManager(mCtx);

                //editor.remove(Const.SETTING.IS_NOTIBAR_USE);

                if(!mCbAllAlarm.isChecked()){
                    showConfirmAlarm();
                    //editor.putBoolean(Const.SETTING.IS_NOTIBAR_USE, false);
                }else{
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putBoolean(Const.SETTING.IS_NOTIBAR_USE, true);
                    editor.commit();
                    mAlarmDataManager.resetMinAlarm();
                }

            }
        });

        btnDbBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Const.DEBUG_TAG, "onclick backup");
                mSettingDataManager.exportDB();
            }
        });

        btnDbRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingDataManager.importDB();
            }
        });

        btnHolidaySync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holidaySync();
            }
        });

        mSeekBar = (SeekBar) mView.findViewById(R.id.seekbarTTSVol);
        bindSeekBarListener();
    }

    private void bindSeekBarListener(){
        mAudioManager = (AudioManager) mCtx.getSystemService(Context.AUDIO_SERVICE);
        int amStreamMusicMaxVol = mAudioManager.getStreamMaxVolume(mAudioManager.STREAM_MUSIC);
        //am.setStreamVolume(am.STREAM_MUSIC, amStreamMusicMaxVol, 0);
        mSeekBar.setMax(amStreamMusicMaxVol);
        //int nowVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        int ttsVol = mPrefs.getInt(Const.SETTING.TTS_VOLUME, amStreamMusicMaxVol/2);

        mSeekBar.setProgress(ttsVol);
        final Intent ttsIntent = new Intent(mCtx, TTSNoti.class);
        ttsIntent.putExtra("alaramTitle", "test sounds");

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                mCtx.startService(ttsIntent);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putInt(Const.SETTING.TTS_VOLUME, seekBar.getProgress());
                editor.commit();
            }
        });
    }

    private void showConfirmAlarm(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        builder.setTitle("알림 시간 추가");

        builder.setMessage("주의! 정말로 상태바 알림 메세지를 받지 않겠습니까?");

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putBoolean(Const.SETTING.IS_NOTIBAR_USE, false);
                editor.commit();
                mAlarmDataManager.resetMinAlarm();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mCbAllAlarm.setChecked(true);
            }
        });

// 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //공휴일 데이터 동기화
    private void holidaySync(){
        //CheckTypesTask task = new CheckTypesTask();
        //task.execute();
        InitializeSetting is = new InitializeSetting(mCtx);
        is.execute();
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}


