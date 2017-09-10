package com.cyberocw.habittodosecretary.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;

/**
 * Created by cyber on 2017-03-21.
 */

public class TTSNotiActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private TextToSpeech mTTS = null;
    private String spokenText;
    private boolean mIsNUll = true;
    private long mAlarmId = -1;

    protected static final int MY_DATA_CHECK_CODE = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        onStartCommand(getIntent(), 0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //onStartCommand(getIntent(), 0, 0);
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.getExtras() != null) {
            String Noti_title = intent.getExtras().getString("alaramTitle");
            mAlarmId = intent.getExtras().getLong("alarmId", -1);
            spokenText = Noti_title;
            //mTTS = new TextToSpeech(this, this);
            //mIsNUll = false;
        }
        Intent checkIntent = new Intent();
        try {
            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
        }
        catch(Exception e){
            checkIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            try{
                startActivity(checkIntent);
            }catch(Exception ee){
                //Toast.makeText(this, "No TTs Engine", Toast.LENGTH_SHORT).show();
                showTtsPopup();
            }
        }
    }

    @Override
    public void onInit(int status) {
        Crashlytics.log(Log.DEBUG, this.toString(), "oninit start  status="+ status);

        if (status == TextToSpeech.SUCCESS) {

            ///int result = mTTS.setLanguage(Locale.getDefault());
            if(mTTS == null){
                try {
                    startTTS(spokenText, mAlarmId);
                }catch(Exception e){
                    e.printStackTrace();
                }
                return;
            }
            int result = mTTS.isLanguageAvailable(Locale.getDefault());
            switch (result)
            {
                case TextToSpeech.LANG_AVAILABLE:
                case TextToSpeech.LANG_COUNTRY_AVAILABLE:
                case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
                    //mTTS.setLanguage(Locale.getDefault());
                    Context ctx = getApplicationContext();
                    SharedPreferences setPrefs = ctx.getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
                    setPrefs.edit().putBoolean("TTS_TEST", true).commit();
                    startTTS(spokenText, mAlarmId);

                    break;
                case TextToSpeech.LANG_MISSING_DATA:
                    Crashlytics.log(Log.DEBUG, this.toString(),  "MISSING_DATA");
                    // missing data, install it
                    Intent installIntent = new Intent();
                    installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                    break;
                case TextToSpeech.LANG_NOT_SUPPORTED:
                    Crashlytics.log(Log.DEBUG, this.toString(), "NOT SUPPORTED");
                    Toast.makeText(this, "TTS LANG_NOT_SUPPORTED", Toast.LENGTH_SHORT).show();
                    return;
            }
        }
        else if (status == TextToSpeech.ERROR) {
            Toast.makeText(this,
                    "Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
        }
        if (mTTS != null) {

            try {
                mTTS.stop();
                mTTS.shutdown();
            }catch (Exception e){
                Crashlytics.log(Log.ERROR, this.toString(), e.getMessage() + " " + e.getCause());
                //e.printStackTrace();
            }
            mTTS = null;
        }
    }

    private void showTtsPopup(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
            mTTS = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(this.toString(), "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case MY_DATA_CHECK_CODE: {
                    Crashlytics.log(Log.DEBUG, this.toString(), "resultCode=" + resultCode);
                    if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                        // the user has the necessary data - create the TTS
                        mTTS = new TextToSpeech(this, this);


                    } else {
                        // no data - install it now
                        Intent installTTSIntent = new Intent();
                        installTTSIntent
                                .setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        startActivity(installTTSIntent);
                    }
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {

            finish();
        }
    }

    private void startTTS(String title, long id){
        Log.d(this.toString(), "ttsnotiactivity start TTS ");
        Intent ttsIntent = new Intent(getApplicationContext(), TTSNoti.class);
        ttsIntent.putExtra("alaramTitle", title);
        ttsIntent.putExtra("alarmId", id);
        getApplicationContext().startService(ttsIntent);
    }
}
