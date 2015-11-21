package com.cyberocw.habittodosecretary.util;

import java.text.DecimalFormat;

/**
 * Created by cyberocw on 2015-11-01.
 */
public class CommonUtils {
	public static String numberFormat(int digit, int value){

		DecimalFormat df = new DecimalFormat("##");

		return df.format(value);
	}
}
