package com.cyberocw.habittodosecretary.file;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.common.vo.FileVO;
import com.cyberocw.habittodosecretary.db.DbHelper;

import java.util.Calendar;

/**
 * Created by cyber on 2017-08-17.
 *
 * sql = "create table if not exists " + TABLE_FILE_INFO + " (" +
 KEY_ID + " integer primary key autoincrement, " +
 KEY_URI + " text , " +
 KEY_NAME + " text , " +
 KEY_SIZE + " integer , " +
 KEY_LENGTH + " integer, " +
 KEY_MIME_TYPE + " text," +
 KEY_TYPE + " text," +
 KEY_F_ID + " integer )" +
 */

public class FileDbManager extends DbHelper {
    private static Context mCtx;
    public static FileDbManager sInstance = null;
    public FileDbManager(Context context) {
        super(context);
    }
    public static synchronized FileDbManager getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new FileDbManager(context);
        }
        mCtx = context;

        return sInstance;
    }

    public void insert(FileVO item) {
        SQLiteDatabase db = this.getWritableDatabase();

        long now = Calendar.getInstance().getTimeInMillis();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, item.getName());
        values.put(KEY_URI, item.getUriPath());
        values.put(KEY_SIZE, item.getSize());
        values.put(KEY_LENGTH, item.getLength());
        values.put(KEY_MIME_TYPE, item.getMimeType());
        values.put(KEY_TYPE, item.getType());
        values.put(KEY_F_ID, item.getfId());
        values.put(KEY_CREATE_DATE, Calendar.getInstance().getTimeInMillis());

        long _id = db.insert(TABLE_FILE_INFO, null, values);

        if(_id == -1){
            Crashlytics.log(Log.ERROR, this.toString(), "DB File INSERT ERROR");
            throw new Error("DB File INSERT ERROR");
        }
        else
            item.setId(_id);

    }
}
