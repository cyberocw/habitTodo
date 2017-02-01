package com.cyberocw.habittodosecretary.settings.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.cyberocw.habittodosecretary.alaram.vo.HolidayVO;
import com.cyberocw.habittodosecretary.db.DbHelper;

import org.json.JSONException;
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

    private long insertHoliday(JSONObject jsonObject) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        long id = -1;

        try {
            values.put(KEY_YEAR, jsonObject.getString("year"));
            values.put(KEY_MONTH, jsonObject.getString("month"));
            values.put(KEY_DAY, jsonObject.getString("day"));
            values.put(KEY_TYPE, jsonObject.getString("type"));
            values.put(KEY_NAME, jsonObject.getString("name"));

            id = db.insert(TABLE_HOLIDAY, null, values);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return id;

        /*

        values.put(KEY_ALARM_DATE, );
        values.put(KEY_F_ALARM_ID, id);
        */

        //long _id = db.insert(TABLE_ALARM_DATE, null, values);

    }

}
