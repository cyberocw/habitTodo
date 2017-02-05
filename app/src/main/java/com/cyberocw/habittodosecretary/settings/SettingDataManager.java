package com.cyberocw.habittodosecretary.settings;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.alaram.vo.HolidayVO;
import com.cyberocw.habittodosecretary.settings.db.SettingDbManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by cyber on 2017-02-02.
 */

public class SettingDataManager {
    Context mCtx = null;
    SettingDbManager mDb;

    private ArrayList<HolidayVO> dataList = new ArrayList<>();

    public SettingDataManager (Context ctx) {
        mCtx = ctx;
        mDb = SettingDbManager.getInstance(ctx);
    }

    public void removeAll(){

    }

    public boolean addItems(JSONObject jsonObject){
        return this.addItems(jsonObject, 0);
    }

    public boolean addItems(JSONObject jsonObject, int year){
        try {
            JSONArray arrObj = jsonObject.getJSONArray("results");
            return mDb.insertHolidays(arrObj, year);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void getList(int year) {
        Log.d(Const.DEBUG_TAG, "getHolidayList start");
        mDb.getHolidayList(year);

    }
}
