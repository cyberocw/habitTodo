package com.cyberocw.habittodosecretary.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by cyberocw on 2015-11-01.
 */
public class CommonUtils {
	public static String numberDigit(int digit, int value){
		String strDigit = "";
		for(int i = 0 ; i  < digit; i++){
			strDigit += "0";
		}
		DecimalFormat df = new DecimalFormat(strDigit);

		return df.format(value);
	}

	public static String convertDateType(Calendar c){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);// cal.get(Calendar.YEAR)
		return sdf.format(c.getTime());//sdf.format(c);
	}

	public static Calendar convertDateType(String s) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);// cal.get(Calendar.YEAR)
		Calendar cal = Calendar.getInstance();

		try {
			cal.setTime(sdf.parse(s));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return cal;
	}

	public static String convertKeywordDateType(Calendar c){
		SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm", Locale.ENGLISH);// cal.get(Calendar.YEAR)
		return sdf.format(c.getTime());//sdf.format(c);
	}

	public static String convertKeywordSimpleDateType(Calendar c){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH);// cal.get(Calendar.YEAR)
		return sdf.format(c.getTime());//sdf.format(c);
	}

	public static String convertFullDateType(Calendar c){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);// cal.get(Calendar.YEAR)
		return sdf.format(c.getTime());//sdf.format(c);
	}

	public static boolean putLogPreference(Context ctx, String value){
		/*SharedPreferences prefs = ctx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
		String data = prefs.getString(Const.DEBUG_TAG, "");
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(Const.DEBUG_TAG);
		editor.putString(Const.DEBUG_TAG, data + CommonUtils.convertFullDateType(Calendar.getInstance()) + " : " + value + "\n\n");
		return editor.commit();*/
		return true;
	}

	public static String getLogPreference(Context ctx){
		SharedPreferences prefs = ctx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
		return prefs.getString(Const.DEBUG_TAG, "");
	}

	public static void logCustomEvent(String name, String id){
		logCustomEvent(name, id, null, 0);
	}
	public static void logCustomEvent(String name, String id, String customName, int customValue){
		if(Const.IS_DEBUG)
			return;
		ContentViewEvent event = new ContentViewEvent();
		event.putContentName(name).putContentId(id);
		if(customName != null)
			event.putCustomAttribute(customName, customValue);
		Answers.getInstance().logContentView(event);
	}

	public static void setupUI(View view, final Activity activity) {
		try {
			// Set up touch listener for non-text box views to hide keyboard.
			if (!(view instanceof EditText)) {
				view.setOnTouchListener(new View.OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
						return false;
					}
				});
			}

			//If a layout container, iterate over children and seed recursion.
			if (view instanceof ViewGroup) {
				for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
					View innerView = ((ViewGroup) view).getChildAt(i);
					setupUI(innerView, activity);
				}
			}
		}catch (Exception e){}
	}

	public static void setupUI(View view, final Activity activity, final Dialog dialog) {
		// Set up touch listener for non-text box views to hide keyboard.
		if (!(view instanceof EditText)) {
			view.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(dialog.getWindow().getCurrentFocus().getWindowToken(), 0);
					return false;
				}
			});
		}

		//If a layout container, iterate over children and seed recursion.
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				View innerView = ((ViewGroup) view).getChildAt(i);
				setupUI(innerView, activity, dialog);
			}
		}
	}

	public static void clearLogPreference(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(Const.DEBUG_TAG);
		editor.commit();
	}

	@SuppressWarnings("deprecation")
	public static Locale getSystemLocaleLegacy(Configuration config){
		return config.locale;
	}

	@TargetApi(Build.VERSION_CODES.N)
	public static Locale getSystemLocale(Configuration config){
		return config.getLocales().get(0);
	}

	public static boolean isLocaleKo(Configuration config){
		Locale locale ;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			locale = CommonUtils.getSystemLocale(config);
		}else {
			locale = CommonUtils.getSystemLocaleLegacy(config);
		}
		return locale.getLanguage().equals("ko");
	}

    public static int getAlarmOptionPosition(int value) {
		switch (value){
			case Const.ALARM_OPTION_TO_SOUND.NONE :
				return 0;
			case Const.ALARM_OPTION_TO_SOUND.TTS :
				return 1;
			case Const.ALARM_OPTION_TO_SOUND.RECORD :
				return 2;
			case Const.ALARM_OPTION_TO_SOUND.VIBRATION:
				return 3;
			default :
				return 1;
		}

    }

	public static int getAlarmOptionValue(int selectedItemPosition) {
		//나중에 순서나 내용이 바뀌면 알맞는 번호 리턴하게 만들기
		return selectedItemPosition;
	}

    public static String getRecordFullPath(Context mCtx, AlarmVO mAlarmVO) {
		return mCtx.getFilesDir().getAbsolutePath() + File.separator + Environment.DIRECTORY_RINGTONES + File.separator +  mAlarmVO.getId() + ".wav";
    }
	public static String getRecordFullPath(Context mCtx, long id) {
		return mCtx.getFilesDir().getAbsolutePath() + File.separator + Environment.DIRECTORY_RINGTONES + File.separator +  id + ".wav";
	}

	public static String getReminderDayId(Calendar cal, int repeatDay, long alarmId){
		return cal.get(Calendar.WEEK_OF_YEAR) + "." + repeatDay + "." + alarmId;
	}
}
