package com.cyberocw.habittodosecretary.util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;

/**
 * Created by cyberocw on 2015-10-04.
 */
public class TTSNoti extends Service implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
	private TextToSpeech mTts;
	private String spokenText;
	private boolean mIsNUll = true;

	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
	}

	@SuppressWarnings({ "static-access", "deprecation" })
	@Override
	public void onStart(Intent intent, int startId) {
		if(intent != null && intent.getExtras() != null) {
			String Noti_title = intent.getExtras().getString("alaramTitle");
			spokenText = Noti_title;
			mTts = new TextToSpeech(this, this);
			mIsNUll = false;
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS && mIsNUll == false) {
			int result = mTts.setLanguage(Locale.KOREA);
			if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
				Toast.makeText(this, "tts start" + spokenText, Toast.LENGTH_SHORT).show();
				Log.d(Const.DEBUG_TAG, "tts start");
				mTts.speak(spokenText+ "   " + spokenText, TextToSpeech.QUEUE_FLUSH, null);
			}
		}
	}

	@Override
	public void onUtteranceCompleted(String uttId) {
		stopSelf();
	}

	@Override
	public void onDestroy() {
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
