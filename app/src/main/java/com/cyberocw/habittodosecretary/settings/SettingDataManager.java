package com.cyberocw.habittodosecretary.settings;

import android.content.Context;

import com.cyberocw.habittodosecretary.settings.db.SettingDbManager;

/**
 * Created by cyber on 2017-02-02.
 */

public class SettingDataManager {
    Context mCtx = null;
    SettingDbManager mDb;

    public SettingDataManager (Context ctx) {
        mCtx = ctx;
        mDb = SettingDbManager.getInstance(ctx);
    }

    public void insertHoliday(){

    }
}
