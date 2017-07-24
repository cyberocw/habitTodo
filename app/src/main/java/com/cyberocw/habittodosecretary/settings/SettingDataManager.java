package com.cyberocw.habittodosecretary.settings;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.vo.HolidayVO;
import com.cyberocw.habittodosecretary.settings.db.SettingDbManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
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

    /*
    public boolean addItems(JSONObject jsonObject){
        return this.addItems(jsonObject, 0);
    }
    */

    public void addItems(JSONObject jsonObject, int year){
        try {
            JSONArray arrObj = jsonObject.getJSONArray("results");
            if(arrObj != null)
                mDb.insertHolidays(arrObj, year);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void getList(int year) {

    }

    public void exportDB() {
        try {
            //File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            File sd = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS);



                String currentDBPath = "//data//" + "com.cyberocw.habittodosecretary"
                        + "//databases//" + "habit_todo";
                String backupDBPath = "//backup//back_habit_todo";


                // Make sure the Pictures directory exists.
                sd.mkdirs();

                File currentDB = new File(data, currentDBPath);
                //File backupDB = new File(sd, backupDBPath);
                File backupDB = new File(sd, "back_habit_todo");

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "Backup Successful!");

                Toast.makeText(mCtx, mCtx.getString(R.string.success),
                        Toast.LENGTH_SHORT).show();


        } catch (Exception e) {
            Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "Backup Failed!" + e.getCause() + " " + e.getMessage());
            Toast.makeText(mCtx, mCtx.getString(R.string.message_failed), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();

        }
    }
    public void importDB() {
        try {
            //File sd = Environment.getExternalStorageDirectory();

            File sd = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS);

            File data = Environment.getDataDirectory();

                String currentDBPath = "//data//" + "com.cyberocw.habittodosecretary"
                        + "//databases//" + "habit_todo";
                String backupDBPath = "//backup//back_habit_todo";
                //File backupDB = new File(data, currentDBPath);
                File backupDB = new File(sd, "back_habit_todo");

                File currentDB = new File(data, currentDBPath);

                FileChannel src = new FileInputStream(backupDB).getChannel();
                FileChannel dst = new FileOutputStream(currentDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "Import Successful!");

                Toast.makeText(mCtx, mCtx.getString(R.string.success),
                        Toast.LENGTH_SHORT).show();


        } catch (Exception e) {
            Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "Import Failed!" + e.getCause() + " " + e.getMessage());
            Toast.makeText(mCtx, mCtx.getString(R.string.message_failed), Toast.LENGTH_SHORT)
                    .show();

            e.printStackTrace();

        }
    }

}
