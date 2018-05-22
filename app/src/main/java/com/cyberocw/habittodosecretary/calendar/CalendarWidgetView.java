package com.cyberocw.habittodosecretary.calendar;

import android.os.Parcel;
import android.widget.RemoteViews;

/**
 * Created by cyber on 2018-03-22.
 */

public class CalendarWidgetView extends RemoteViews {
    public CalendarWidgetView(String packageName, int layoutId) {
        super(packageName, layoutId);
    }

    public CalendarWidgetView(RemoteViews landscape, RemoteViews portrait) {
        super(landscape, portrait);
    }

    public CalendarWidgetView(Parcel parcel) {
        super(parcel);
    }
}
