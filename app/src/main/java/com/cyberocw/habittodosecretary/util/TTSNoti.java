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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;

/**
 * Created by cyberocw on 2015-10-04.
 */
public class TTSNoti extends Service implements TextToSpeech.OnInitListener{
	private TextToSpeech mTTS = null;
	private ArrayList<String> mArrText = new ArrayList();
	private boolean mIsNUll = true;
	private long mAlarmId = -1;
	private int mIndex = 0;
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

			//음악과 동시에 재생할때 재생바 조절에 따른 볼륨 조절을 위해 알람 아이디를 강제 지정하여 if alarmId> -1 조건이 무조건 실행되도록 일단 해둠
			mAlarmId = 1;

			Log.d(this.toString(), "mTTS == null=" + (mTTS == null));
			if(mTTS == null)
				mTTS = new TextToSpeech(getApplicationContext(), this);

			Log.d(this.toString(), " add  ");
			mArrText.add(Noti_title);

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
				Log.d(Const.DEBUG_TAG, "tts start");



				mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

				int focusResult = mAudioManager.requestAudioFocus(afChangeListener,
						// Use the music stream.
						AudioManager.STREAM_MUSIC,
						// Request permanent focus.
						AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

				Log.d(this.toString(), "mAlarmId="+mAlarmId);

				if(mAlarmId > -1) {

					mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					/*
					SharedPreferences mPrefs = getApplicationContext().getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
					mPrefsTTSVol = mPrefs.getInt(Const.SETTING.TTS_VOLUME, mOriginalVolume);
					*/
				}

				mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
					@Override
					public void onStart(String utteranceId) {
						Log.d(this.toString(), "start utteranceId="+utteranceId);
						if(mAlarmId > -1) {
							SharedPreferences mPrefs = getApplicationContext().getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
							mPrefsTTSVol = mPrefs.getInt(Const.SETTING.TTS_VOLUME, mOriginalVolume);
							mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mPrefsTTSVol, 0);
						}
					}
					@Override
					public void onDone(String utteranceId) {
						Log.d(this.toString(), " done utteranceId="+utteranceId);

						if(mAlarmId > -1){
							mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
						}

						speakText();

					}
					@Override
					public void onError(String utteranceId) {
						Log.d(this.toString(), " error utteranceId="+utteranceId);

						if(mAlarmId > -1)
							mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
						speakText();
					}
				});

				Log.d(this.toString(), "focusResult = AUDIOFOCUS_REQUEST_GRANTED = " +  (focusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED));
				if (focusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
					// Start playback
					speakText();
				} else if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
					Toast.makeText(getApplicationContext(), "오디오 권한을 얻지 못했습니다", Toast.LENGTH_LONG).show();
					//mState.audioFocusGranted = false;
				}

			}
		}
	}

	private void speakText(){
		if(mArrText.size() == 0) {
			stopSelf();
			return;
		}
		String spokenText = mArrText.get(0);
		mIndex++;
		mArrText.remove(0);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			ttsGreater21(spokenText, mIndex);
		} else {
			ttsUnder20(spokenText, mIndex);
		}
	}

	AudioManager.OnAudioFocusChangeListener afChangeListener =
			new AudioManager.OnAudioFocusChangeListener() {
				public void onAudioFocusChange(int focusChange) {
					if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
						// Permanent loss of audio focus
						// Pause playback immediately
						//mTTS.stop();
					}
					else if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT) {
						//mTTS.stop();
					} else if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
						// Lower the volume, keep playing
						//mTTS.stop();
					} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN || focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
						//// Your app has been granted audio focus again
						speakText();
						// Raise volume to normal, restart playback if necessary
					}
				}
			};

	@SuppressWarnings("deprecation")
	private void ttsUnder20(String text, long index) {
		HashMap<String, String> map = new HashMap<>();
		map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(index));
		mTTS.speak(text + "      "  +text, TextToSpeech.QUEUE_ADD, map);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void ttsGreater21(String text, long index) {
		String utteranceId=String.valueOf(index);
		mTTS.speak(text + "      "  +text, TextToSpeech.QUEUE_ADD, null, utteranceId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
		}
		if(mAudioManager != null)
			mAudioManager.abandonAudioFocus(afChangeListener);
		//unbindService();
		Log.d(this.toString(), " tts service on destroy ");

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
