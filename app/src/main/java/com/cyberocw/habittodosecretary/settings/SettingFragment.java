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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.util.CommonUtils;
import com.cyberocw.habittodosecretary.util.TTSNotiActivity;

import butterknife.ButterKnife;
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
    private int mOriginalVolume;
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

        if(!CommonUtils.isLocaleKo(getResources().getConfiguration()))
            mView.findViewById(R.id.holidayOptionWrap).setVisibility(View.GONE);

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
        Button btnShowLog = ButterKnife.findById(mView, R.id.btnShowLog);
        Button btnClearLog = ButterKnife.findById(mView, R.id.btnClearLog);
        final Switch swAlarmNoti = ButterKnife.findById(mView, R.id.isAlarmNoti);
        final Switch swTTSNoti = ButterKnife.findById(mView, R.id.isTTSNoti);
        final Switch swTTSNotiManner = ButterKnife.findById(mView, R.id.isTTSNotiManner);

        final TextView tvLog = ButterKnife.findById(mView, R.id.tvLog);
        /*
        TextView tvReqCode = (TextView) mView.findViewById(R.id.tvReqCode);

        SharedPreferences prefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
        tvReqCode.setText(prefs.getString(Const.PARAM.REQ_CODE, "없음"));
        */
        mCbAllAlarm = (CheckBox) mView.findViewById(R.id.checkAllAlarm);
        final Switch cbBackgroundNoti = (Switch) mView.findViewById(R.id.checkBackgroundNoti);
        boolean isUseNotibar = mPrefs.getBoolean(Const.SETTING.IS_NOTIBAR_USE, true);
        boolean isBackgNoti = mPrefs.getBoolean(Const.SETTING.IS_BACKGROUND_NOTI_USE, true);
        boolean isAlarmNoti = mPrefs.getBoolean(Const.SETTING.IS_ALARM_NOTI, true);
        boolean isTTSNoti = mPrefs.getBoolean(Const.SETTING.IS_TTS_NOTI, true);
        boolean isTTSNotiManner = mPrefs.getBoolean(Const.SETTING.IS_TTS_NOTI_MANNER, true);

        if(isUseNotibar)
            mCbAllAlarm.setChecked(true);
        if(isBackgNoti)
            cbBackgroundNoti.setChecked(true);
        if(isAlarmNoti)
            swAlarmNoti.setChecked(true);
        if(isTTSNoti)
            swTTSNoti.setChecked(true);
        else
            swTTSNotiManner.setEnabled(false);
        if(isTTSNotiManner)
            swTTSNotiManner.setChecked(true);



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
                Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "onclick backup");
                mSettingDataManager.exportDB();
            }
        });

        btnDbRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDbRestore();
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

        btnShowLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLog.setText(CommonUtils.getLogPreference(mCtx));
            }
        });

        btnClearLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.clearLogPreference(mCtx);
                tvLog.setText("");
            }
        });

        swAlarmNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch view = (Switch) v;
                toggleAlarmPreference(Const.SETTING.IS_ALARM_NOTI);
                if(getAlarmPreference(Const.SETTING.IS_ALARM_NOTI))
                    view.setChecked(true);
                else
                    view.setChecked(false);
            }
        });
        swTTSNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch view = (Switch) v;
                toggleAlarmPreference(Const.SETTING.IS_TTS_NOTI);
                if(getAlarmPreference(Const.SETTING.IS_TTS_NOTI)) {
                    view.setChecked(true);
                    swTTSNotiManner.setEnabled(true);
                }
                else {
                    view.setChecked(false);
                    swTTSNotiManner.setEnabled(false);
                }
            }
        });
        swTTSNotiManner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch view = (Switch) v;
                toggleAlarmPreference(Const.SETTING.IS_TTS_NOTI_MANNER);
                if(getAlarmPreference(Const.SETTING.IS_TTS_NOTI_MANNER))
                    view.setChecked(true);
                else
                    view.setChecked(false);
            }
        });
    }

    protected boolean putAlarmPreference(String key, boolean value){
        SharedPreferences prefs = mCtx.getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.putBoolean(key, value);
        return editor.commit();
    }

    private boolean getAlarmPreference(String key){
        SharedPreferences prefs = mCtx.getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
        return prefs.getBoolean(key, true);
    }

    private boolean toggleAlarmPreference(String key){
        return putAlarmPreference(key, !getAlarmPreference(key));
    }

    private void bindSeekBarListener(){
        mAudioManager = (AudioManager) mCtx.getSystemService(Context.AUDIO_SERVICE);
        int amStreamMusicMaxVol = mAudioManager.getStreamMaxVolume(mAudioManager.STREAM_MUSIC);
        //am.setStreamVolume(am.STREAM_MUSIC, amStreamMusicMaxVol, 0);
        mSeekBar.setMax(amStreamMusicMaxVol);
        mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        int ttsVol = mPrefs.getInt(Const.SETTING.TTS_VOLUME, mOriginalVolume);

        mSeekBar.setProgress(ttsVol);
        final Intent ttsIntent = new Intent(mCtx, TTSNotiActivity.class);
        ttsIntent.putExtra("alaramTitle", "sound test");

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                /*SharedPreferences.Editor editor = mPrefs.edit();
                editor.putInt(Const.SETTING.TTS_VOLUME, progress);
                editor.commit();*/

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                /*SharedPreferences.Editor editor = mPrefs.edit();
                editor.putInt(Const.SETTING.TTS_VOLUME, seekBar.getProgress());
                editor.commit();*/


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putInt(Const.SETTING.TTS_VOLUME, seekBar.getProgress());
                editor.commit();
                mCtx.startActivity(ttsIntent);

            }
        });
    }

    private void showConfirmAlarm(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        builder.setTitle(getString(R.string.caution));

        builder.setMessage(getString(R.string.fragment_setting_msg_cancel_notibar));

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

    private void showConfirmDbRestore(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        builder.setTitle(mCtx.getString(R.string.caution));

        builder.setMessage(getString(R.string.fragment_setting_msg_restore));

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mSettingDataManager.importDB();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
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


    @Override
    public void onDestroy() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
        super.onDestroy();

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


