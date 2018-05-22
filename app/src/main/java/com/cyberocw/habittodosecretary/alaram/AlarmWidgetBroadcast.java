package com.cyberocw.habittodosecretary.alaram;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cyberocw.habittodosecretary.calendar.CalendarWidgetProvider;

/**
 * Created by cyber on 2018-05-20.
 */

public class AlarmWidgetBroadcast{
        public static void updateWidget(Context context) {
            Log.d("updateWidget", "updateWidgetupdateWidget");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, CalendarWidgetProvider.class.getName()));
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(intent);
        }

}
