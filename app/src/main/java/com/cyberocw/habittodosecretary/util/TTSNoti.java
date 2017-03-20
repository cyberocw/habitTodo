package com.cyberocw.habittodosecretary.util;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
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
	private long alarmId = -1;

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
			alarmId = intent.getExtras().getLong("alarmId", 0);
			spokenText = Noti_title;
			mTTS = new TextToSpeech(this, this);
			mIsNUll = false;
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onInit(int status) {
		Log.d(this.toString(), "oninit start  status="+ status);
		if (status == TextToSpeech.SUCCESS && mIsNUll == false) {
			int result = mTTS.setLanguage(Locale.getDefault());
			if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
				Toast.makeText(this, "tts start" + spokenText, Toast.LENGTH_SHORT).show();
				Log.d(Const.DEBUG_TAG, "tts start");

				mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
					@Override
					public void onStart(String utteranceId) {
						Log.d(this.toString(), "start utteranceId="+utteranceId);
					}
					@Override
					public void onDone(String utteranceId) {
						Log.d(this.toString(), " done utteranceId="+utteranceId);
						//stopSelf();
					}
					@Override
					public void onError(String utteranceId) {
						Log.d(this.toString(), " error utteranceId="+utteranceId);
						//stopSelf();
					}
				});

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					ttsGreater21(spokenText, alarmId);
				} else {
					ttsUnder20(spokenText, alarmId);
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
		if (mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
