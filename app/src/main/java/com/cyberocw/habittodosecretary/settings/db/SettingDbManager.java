package com.cyberocw.habittodosecretary.settings.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.cyberocw.habittodosecretary.db.DbHelper;

import org.json.JSONObject;

/**
 * Created by cyberocw on 2017-01-22.
 */

public class SettingDbManager extends DbHelper {
    private static SettingDbManager sInstance;
    private static Context mCtx;

    public SettingDbManager(Context ctx) {
        super(ctx);
    }
    public static synchronized SettingDbManager getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        mCtx = context;

        if (sInstance == null) {
            sInstance = new SettingDbManager(context);
        }
        return sInstance;
    }

    public void deleteHolidayData(int year, String type){

    }

    private void insertHoliday(JSONObject jsonObject) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        /*

        values.put(KEY_ALARM_DATE, );
        values.put(KEY_F_ALARM_ID, id);
        */

        //long _id = db.insert(TABLE_ALARM_DATE, null, values);

    }

}
