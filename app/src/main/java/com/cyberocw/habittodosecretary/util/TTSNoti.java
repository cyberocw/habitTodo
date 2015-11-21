package com.cyberocw.habittodosecretary.util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;

import java.util.Locale;

/**
 * Created by cyberocw on 2015-10-04.
 */
public class TTSNoti extends Service implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
	private TextToSpeech mTts;
	private String spokenText;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@SuppressWarnings({ "static-access", "deprecation" })
	@Override
	public void onStart(Intent intent, int startId) {
		String Noti_title = intent.getExtras().getString("alaramTitle");
		spokenText = Noti_title;
		mTts = new TextToSpeech(this, this);
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
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
