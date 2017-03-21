package com.cyberocw.habittodosecretary.util;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;

import java.util.HashMap;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

/**
 * Created by cyberocw on 2015-10-04.
 */
public class TTSNoti extends Service implements TextToSpeech.OnInitListener{
	private TextToSpeech mTTS;
	private String spokenText;
	private boolean mIsNUll = true;
	private long mAlarmId = -1;
	private  AudioManager mAudioManager;
	private int mOriginalVolume, mPrefsTTSVol;

	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(this.toString(), "intent="+intent + " extras="+intent.getExtras());
		if(intent != null && intent.getExtras() != null) {
			String Noti_title = intent.getExtras().getString("alaramTitle");
			mAlarmId = intent.getExtras().getLong("alarmId", -1);
			spokenText = Noti_title;
			mTTS = new TextToSpeech(getApplicationContext(), this);
			mIsNUll = false;
		}

		return START_NOT_STICKY;
		//return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onInit(int status) {
		Log.d(this.toString(), "oninit start  status="+ status);
		if (status == TextToSpeech.SUCCESS && mIsNUll == false) {
			int result = mTTS.setLanguage(Locale.getDefault());
			if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
				Toast.makeText(this, "tts start" + spokenText, Toast.LENGTH_SHORT).show();
				Log.d(Const.DEBUG_TAG, "tts start");

				if(mAlarmId > -1) {
					mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
					mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					SharedPreferences mPrefs = getApplicationContext().getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
					mPrefsTTSVol = mPrefs.getInt(Const.SETTING.TTS_VOLUME, mOriginalVolume);
				}

				mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
					@Override
					public void onStart(String utteranceId) {
						Log.d(this.toString(), "start utteranceId="+utteranceId);
						if(mAlarmId > -1)
							mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mPrefsTTSVol, 0);
					}
					@Override
					public void onDone(String utteranceId) {
						Log.d(this.toString(), " done utteranceId="+utteranceId);

						if(mAlarmId > -1)
							mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);

						stopSelf();
					}
					@Override
					public void onError(String utteranceId) {
						Log.d(this.toString(), " error utteranceId="+utteranceId);

						if(mAlarmId > -1)
							mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
						stopSelf();
					}
				});

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					ttsGreater21(spokenText, mAlarmId);
				} else {
					ttsUnder20(spokenText, mAlarmId);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void ttsUnder20(String text, long alarmId) {
		HashMap<String, String> map = new HashMap<>();
		map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(alarmId));
		mTTS.speak(text + "      "  +text, TextToSpeech.QUEUE_FLUSH, map);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void ttsGreater21(String text, long alarmId) {
		String utteranceId=String.valueOf(alarmId);
		mTTS.speak(text + "      "  +text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
		}
		//unbindService();
		Log.d(this.toString(), " tts service on destroy ");

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
