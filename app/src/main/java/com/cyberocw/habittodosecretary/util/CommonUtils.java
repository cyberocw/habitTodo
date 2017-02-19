package com.cyberocw.habittodosecretary.util;

import java.text.DecimalFormat;

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
}
