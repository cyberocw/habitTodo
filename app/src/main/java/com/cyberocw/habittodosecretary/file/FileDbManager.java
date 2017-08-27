package com.cyberocw.habittodosecretary.file;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.common.vo.FileVO;
import com.cyberocw.habittodosecretary.db.DbHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

    public ArrayList<FileVO> getAttachListAll(){
        String selectQuery = " SELECT * FROM " + TABLE_FILE_INFO ;
        Log.d(this.toString(), "selectQuery="+selectQuery);
        return getQuery(selectQuery);
    }

    public ArrayList<FileVO> getAttachList(String type, long fId){
        String selectQuery = " SELECT * FROM " + TABLE_FILE_INFO + " where " + KEY_TYPE + "='" + type + "' AND " + KEY_F_ID + " = " + fId;
        Log.d(this.toString(), "selectQuery="+selectQuery);
        return getQuery(selectQuery);
    }
    public ArrayList<FileVO> getQuery(String selectQuery) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        ArrayList<FileVO> list = new ArrayList<FileVO>();
        FileVO vo;

        if (c.moveToFirst()) {
            do {
                vo = new FileVO();
                vo.setId(c.getLong((c.getColumnIndex(KEY_ID))));
                vo.setName((c.getString(c.getColumnIndex(KEY_NAME))));
                //vo.setUriPath((c.getString(c.getColumnIndex(KEY_URI))));
                vo.setUri(c.getString(c.getColumnIndex(KEY_URI)));
                if(vo.getUri() != null)
                    vo.setUriPath(Uri.parse(vo.getUri()).getPath());
                vo.setSize(c.getLong(c.getColumnIndex(KEY_SIZE)));
                vo.setLength(c.getLong(c.getColumnIndex(KEY_LENGTH)));
                vo.setMimeType(c.getString(c.getColumnIndex(KEY_MIME_TYPE)));
                vo.setCreateDt(c.getLong(c.getColumnIndex(KEY_CREATE_DATE)));
                vo.setType(c.getString(c.getColumnIndex(KEY_TYPE)));
                vo.setfId(c.getLong(c.getColumnIndex(KEY_F_ID)));
                list.add(vo);
            } while (c.moveToNext());
        }
        db.close();
        return list;
    }
    public void insert(FileVO item) {
        insert(item, this.getWritableDatabase());
    }

    public void insert(FileVO item, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, item.getName());
        values.put(KEY_URI, item.getUri());
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

        closeDB();
    }
    public int update(FileVO vo){
        int result = update(vo, this.getWritableDatabase());
        closeDB();
        return result;

    }
    public int update(FileVO vo, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(KEY_F_ID, vo.getfId());
        //Calendar c = Calendar.getInstance();
        //values.put(KEY_UPDATE_DATE, c.getTimeInMillis());
        int result = db.update(TABLE_FILE_INFO, values, KEY_ID + "=?", new String[]{Long.toString(vo.getId())});

        return result;
    }

    public boolean delete(ArrayList<FileVO> arrayList) {
        if(arrayList == null || arrayList.size() == 0)
            return false;

        boolean result = true;
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.beginTransaction();
            for(int i = 0  ; i < arrayList.size(); i++) {
                db.delete(TABLE_FILE_INFO, KEY_ID + "=?", new String[]{String.valueOf(arrayList.get(i).getId())});
            }
            db.setTransactionSuccessful();
        }
        catch (Exception e){
            Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "error msg="+e.getMessage());
            result = false;
        }
        finally{
            db.endTransaction();
            closeDB();
        }
        return result;
    }
    public boolean delete(long id, SQLiteDatabase db) {
        boolean result = true;
        if(db == null)
            db = this.getWritableDatabase();

         db.delete(TABLE_FILE_INFO, KEY_ID + "=?", new String[]{String.valueOf(id)});
         closeDB();

        return result;
    }

    public boolean deleteByType(long fId, String type){
        SQLiteDatabase db = this.getWritableDatabase();
        if(fId < 0 || type == null || type.equals("")){
            return false;
        }
        int cnt = db.delete(TABLE_FILE_INFO, KEY_F_ALARM_ID + "=? AND " + KEY_TYPE + "=?" , new String[]{String.valueOf(fId), type});
        db.close();
        return cnt > 0;
    }

    public ArrayList<FileVO> deleteTrash() {

        String selectQuery = " SELECT * FROM " + TABLE_FILE_INFO + " where "  + KEY_F_ID + " = -1" ;
        Log.d(this.toString(), "selectQuery="+selectQuery);
        ArrayList<FileVO> list =  getQuery(selectQuery);

        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_FILE_INFO, KEY_F_ID  + "=?", new String[]{"-1"});
        db.close();
        return list;
    }
}
