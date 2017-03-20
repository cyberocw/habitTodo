package com.cyberocw.habittodosecretary.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.cyberocw.habittodosecretary.Const;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");// cal.get(Calendar.YEAR)
		return sdf.format(c.getTime());//sdf.format(c);
	}

	public static Calendar convertDateType(String s) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");// cal.get(Calendar.YEAR)
		Calendar cal = Calendar.getInstance();

		try {
			cal.setTime(sdf.parse(s));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return cal;
	}

	public static boolean putSettingPreference(Context ctx, String key, int value){
		SharedPreferences prefs = ctx.getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(key);
		editor.putInt(key, value);
		return editor.commit();
	}

	public static int getSettingPreference(Context ctx, String key){
		SharedPreferences prefs = ctx.getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
		return prefs.getInt(key, 0);
	}

}
