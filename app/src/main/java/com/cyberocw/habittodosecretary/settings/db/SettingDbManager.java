package com.cyberocw.habittodosecretary.settings.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.alaram.vo.HolidayVO;
import com.cyberocw.habittodosecretary.db.DbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

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


    public boolean insertHolidays(JSONArray jsonArray, int year) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        //Calendar cal = Calendar.getInstance();
        //int year = cal.get(Calendar.YEAR);
        if(jsonArray == null || jsonArray.length() == 0)
            return false;
        boolean result = false;
        try {
            if(year < 0)
                year = jsonArray.getJSONObject(0).getInt("year");

            Log.d(Const.DEBUG_TAG, " delete year = " + year);

            db.delete(TABLE_HOLIDAY, KEY_YEAR + "=?", new String[]{String.valueOf(year)});

            Log.d(Const.DEBUG_TAG, "jsonArray.length();="+jsonArray.length());

            JSONObject jsonObject;
            for (int i = 0; i < jsonArray.length(); i++) {
                ContentValues values = new ContentValues();
                jsonObject = (JSONObject) jsonArray.get(i);

                values.put(KEY_YEAR, jsonObject.getString("year"));
                values.put(KEY_MONTH, jsonObject.getString("month"));
                values.put(KEY_DAY, jsonObject.getString("day"));
                values.put(KEY_TYPE, jsonObject.getString("type"));
                values.put(KEY_NAME, jsonObject.getString("name"));

                db.insert(TABLE_HOLIDAY, null, values);
            }
            db.setTransactionSuccessful();

            result = true;
        }catch(Exception e){

            result = false;
        }finally {
            db.endTransaction();
            db.close();
            return result;
        }

    }

    public ArrayList<HolidayVO> getHolidayList(int year){
        String selectQuery = " SELECT * FROM " + TABLE_HOLIDAY  ;//+ " where year = " + year;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        ArrayList<HolidayVO> list = new ArrayList<HolidayVO>();
        HolidayVO vo;
        Log.d(Const.DEBUG_DB_TAG, " holiday cnt = " + c.getCount());

        if (c.moveToFirst()) {
            do {
                vo = new HolidayVO();
                vo.setId(c.getLong(c.getColumnIndex(KEY_ID)));
                vo.setYear(c.getInt(c.getColumnIndex(KEY_YEAR)));
                vo.setMonth(c.getInt(c.getColumnIndex(KEY_MONTH)));
                vo.setDay(c.getInt(c.getColumnIndex(KEY_DAY)));
                vo.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                vo.setType(c.getString(c.getColumnIndex(KEY_TYPE)));

                //vo.setCreateDt(c.getInt(c.getColumnIndex(KEY_CREATE_DATE)));
                //vo.setUpdateDt(c.getInt(c.getColumnIndex(KEY_UPDATE_DATE)));
                Log.d(Const.DEBUG_DB_TAG, "holiday=" + vo.toString());
                list.add(vo);
            } while (c.moveToNext());
        }
        closeDB();
        return list;
    }
}
