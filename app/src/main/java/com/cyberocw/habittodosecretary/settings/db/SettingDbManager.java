package com.cyberocw.habittodosecretary.settings.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.alaram.vo.HolidayVO;
import com.cyberocw.habittodosecretary.db.DbHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

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

        // Use the application context, which will
        // ensure that you
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

    public HashMap getHolidayMap(String startDate, String endDate){
        ArrayList<HolidayVO> list = getHolidayList(startDate, endDate);
        HashMap<String, ArrayList<HolidayVO>> resultMap = new HashMap<>();

        Crashlytics.log(Log.DEBUG, Const.DEBUG_DB_TAG, "holiday list cnt = " + list.size());

        for(int i = 0 ; i < list.size(); i++){
            if(!resultMap.containsKey(list.get(i).getFullDate()))
                resultMap.put(list.get(i).getFullDate(), new ArrayList<HolidayVO>());
            resultMap.get(list.get(i).getFullDate()).add(list.get(i));
        }
        return resultMap;
    }

    public ArrayList<HolidayVO> getHolidayList(String startDate, String endDate){
        SQLiteDatabase db = this.getReadableDatabase();

        String holidayQuery = "select " + KEY_ID + ", " +
                KEY_YEAR +", " +
                KEY_MONTH +", " +
                KEY_DAY +", " +
                KEY_TYPE +", " +
                KEY_NAME +", " +
                KEY_FULL_DATE +" " +
                " from " + TABLE_HOLIDAY + " where " + KEY_FULL_DATE + " >= " + startDate + " and " + KEY_FULL_DATE + " <= " + endDate;

        Crashlytics.log(Log.DEBUG, Const.DEBUG_DB_TAG, "holidayQuery="+holidayQuery);

        Cursor c = db.rawQuery(holidayQuery, null);

        ArrayList<HolidayVO> list;
        list = bindVO(c);

        if(c != null)
            c.close();
        this.close();

        return list;
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

            Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, " delete year = " + year);

            db.delete(TABLE_HOLIDAY, KEY_YEAR + "=?", new String[]{String.valueOf(year)});

            //Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "jsonArray.length();="+jsonArray.length());

            JSONObject jsonObject;
            for (int i = 0; i < jsonArray.length(); i++) {
                ContentValues values = new ContentValues();
                jsonObject = (JSONObject) jsonArray.get(i);

                values.put(KEY_YEAR, jsonObject.getString("year"));
                values.put(KEY_MONTH, jsonObject.getString("month"));
                values.put(KEY_DAY, jsonObject.getString("day"));
                values.put(KEY_TYPE, jsonObject.getString("type"));
                values.put(KEY_NAME, jsonObject.getString("name"));
                values.put(KEY_FULL_DATE, jsonObject.getString("year") + jsonObject.getString("month") + jsonObject.getString("day"));

                db.insert(TABLE_HOLIDAY, null, values);
            }
            db.setTransactionSuccessful();

            result = true;
        }catch(Exception e){

            result = false;
        }finally {
            db.endTransaction();

            return result;
        }

    }

    private ArrayList<HolidayVO> bindVO(Cursor c){
        ArrayList<HolidayVO> list = new ArrayList<HolidayVO>();
        if(c == null || c.isClosed() ||c.getCount() == 0)
            return list;

        if (c.moveToFirst()) {
            do{
                HolidayVO vo;
                Crashlytics.log(Log.DEBUG, Const.DEBUG_DB_TAG, " holiday cnt = " + c.getCount());

                if (c.moveToFirst()) {
                    do {
                        vo = new HolidayVO();
                        vo.setId(c.getLong(c.getColumnIndex(KEY_ID)));
                        vo.setYear(c.getInt(c.getColumnIndex(KEY_YEAR)));
                        vo.setMonth(c.getInt(c.getColumnIndex(KEY_MONTH)));
                        vo.setDay(c.getInt(c.getColumnIndex(KEY_DAY)));
                        vo.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                        vo.setType(c.getString(c.getColumnIndex(KEY_TYPE)));
                        vo.setFullDate(c.getString(c.getColumnIndex(KEY_FULL_DATE)));

                        //vo.setCreateDt(c.getInt(c.getColumnIndex(KEY_CREATE_DATE)));
                        //vo.setUpdateDt(c.getInt(c.getColumnIndex(KEY_UPDATE_DATE)));
                        Crashlytics.log(Log.DEBUG, Const.DEBUG_DB_TAG, "holiday=" + vo.toString());
                        list.add(vo);
                    } while (c.moveToNext());
                }
            }while(c.moveToNext());
        }
        return list;
    }
}
