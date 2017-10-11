package com.cyberocw.habittodosecretary.settings;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.WebViewActivity;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.file.StorageHelper;
import com.cyberocw.habittodosecretary.util.CommonUtils;
import com.cyberocw.habittodosecretary.util.KeyboardUtils;
import com.cyberocw.habittodosecretary.util.PopMessageEvent;
import com.cyberocw.habittodosecretary.util.TTSNotiActivity;
import com.cyberocw.habittodosecretary.util.TitleMessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

/**
 * Created by cyberocw on 2016-11-06.
 */
public class SettingFragment extends Fragment {
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

    final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1122;

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
        //MainActivity.pushActionBarInfo(R.string.nav_item_setting, false);
        EventBus.getDefault().post(new TitleMessageEvent(getString(R.string.nav_item_setting), false));
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

        if(StorageHelper.getAttachmentDir(mCtx).exists()){
            for(File f : StorageHelper.getAttachmentDir(mCtx).listFiles()){
                Log.d(this.toString(), "sd list file="+f.getAbsolutePath());
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermissionReadStorage(mCtx, getActivity());

    }
    public void checkPermissionReadStorage(Context context, Activity activity){
        Log.d(this.toString(), "check permission =" + (ContextCompat.checkSelfPermission(context,      Manifest.permission.WRITE_EXTERNAL_STORAGE) !=     PackageManager.PERMISSION_GRANTED));
        if (ContextCompat.checkSelfPermission(context,      Manifest.permission.WRITE_EXTERNAL_STORAGE) !=     PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder dialog = new AlertDialog.Builder(mCtx);
                dialog.setTitle(getString(R.string.need_permission))
                        .setMessage(getString(R.string.permission_storage_cont))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
                                }

                            }
                        })
                        /*.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(mCtx, "기능을 취소했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })*/
                        .create()
                        .show();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
                //premission to read storage
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(mCtx, "We Need permission Storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void initActivity(){
        mSettingDataManager = new SettingDataManager(mCtx);

        mPrefs = mCtx.getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);

        bindBtnEvent();
        CommonUtils.logCustomEvent("SettingFragment", "1");
    }

    private void bindBtnEvent(){
        Button btnHolidaySync = (Button) mView.findViewById(R.id.btnHolidaySync);
        Button btnDbBackup = (Button) mView.findViewById(R.id.btnDbBackup);
        Button btnDbRestore= (Button) mView.findViewById(R.id.btnDbRestore);
        Button btnShowLog = ButterKnife.findById(mView, R.id.btnShowLog);
        Button btnClearLog = ButterKnife.findById(mView, R.id.btnClearLog);
        Button btnPrivacy = ButterKnife.findById(mView, R.id.btnPrivacyPolicy);

        final Switch swAlarmNoti = ButterKnife.findById(mView, R.id.isAlarmNoti);
        final Switch swTTSNoti = ButterKnife.findById(mView, R.id.isTTSNoti);
        final Switch swTTSNotiManner = ButterKnife.findById(mView, R.id.isTTSNotiManner);
        final Switch swDisturb = ButterKnife.findById(mView, R.id.isDisturb);


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
        boolean isDisturb = mPrefs.getBoolean(Const.SETTING.IS_DISTURB_MODE, false);

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
        if(isDisturb)
            swDisturb.setChecked(true);


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
                showPasswordPopup(false);
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
        swDisturb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch view = (Switch) v;
                toggleAlarmPreference(Const.SETTING.IS_DISTURB_MODE);

                if(mAlarmDataManager == null)
                    mAlarmDataManager = new AlarmDataManager(mCtx);

                if(getAlarmPreference(Const.SETTING.IS_DISTURB_MODE)) {
                    view.setChecked(true);
                    mAlarmDataManager.stopAllAlarm();
                }
                else {
                    view.setChecked(false);
                    mAlarmDataManager.resetMinAlarm();
                }
            }
        });
        btnPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //WebViewDialog dialog = new WebViewDialog();
                Intent intent = new Intent(mCtx, WebViewActivity.class);

                Bundle bundle = new Bundle();
                if(CommonUtils.isLocaleKo(getResources().getConfiguration())){
                    bundle.putSerializable("url", "https://sites.google.com/view/ohreminder/%EA%B0%9C%EC%9D%B8%EC%A0%95%EB%B3%B4%EC%B2%98%EB%A6%AC%EB%B0%A9%EC%B9%A8");
                }
                else{
                    bundle.putSerializable("url", "https://sites.google.com/view/ohreminder/personal-privacy-policy");
                }

                intent.putExtras(bundle);

                startActivity(intent);

            }
        });
    }

    private void showPasswordPopup(final boolean isDecode) {
        AlertDialog.Builder ad = new AlertDialog.Builder(mCtx);

        ad.setTitle("Backup Password");       // 제목 설정
        ad.setMessage(mCtx.getString(R.string.setting_password_cont));   // 내용 설정

        final EditText et = new EditText(mCtx);
            ad.setView(et)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Text 값 받아서 로그 남기기
                String value = et.getText().toString();

                if(isDecode){
                    mSettingDataManager.fileRestore(value);
                }
                else{
                    mSettingDataManager.fileBackup(value);
                }
                dialog.dismiss();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();     //닫기
                // Event
            }
        }).show();


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
        final EditText et = new EditText(mCtx);
        builder.setView(et);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //mSettingDataManager.importDB();
                //showPasswordPopup(true);

                mSettingDataManager.fileRestore(et.getText().toString());
                KeyboardUtils.hideKeyboard(mView);
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
        //MainActivity.popActionbarInfo();
        EventBus.getDefault().post(new PopMessageEvent());
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
        void onFragmentInteraction(Uri uri);
    }

}


