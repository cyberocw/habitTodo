package com.cyberocw.habittodosecretary.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.alaram.vo.HolidayVO;
import com.cyberocw.habittodosecretary.file.StorageHelper;
import com.cyberocw.habittodosecretary.settings.db.SettingDbManager;
import com.cyberocw.habittodosecretary.util.CommonUtils;


import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;

/**
 * Created by cyber on 2017-02-02.
 */

public class SettingDataManager {
    Context mCtx = null;
    SettingDbManager mDb;

    private ArrayList<HolidayVO> dataList = new ArrayList<>();

    public SettingDataManager(Context ctx) {
        mCtx = ctx;
        mDb = SettingDbManager.getInstance(ctx);


    }

    public void removeAll() {

    }

    /*
    public boolean addItems(JSONObject jsonObject){
        return this.addItems(jsonObject, 0);
    }
    */

    public void addItems(JSONObject jsonObject, int year) {
        try {
            JSONArray arrObj = jsonObject.getJSONArray("results");
            if (arrObj != null)
                mDb.insertHolidays(arrObj, year);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void getList(int year) {

    }

    private File exportDB() {
        try {
            File data = Environment.getDataDirectory();
            File externalDataRoot = StorageHelper.getAttachmentDir(mCtx);

            String currentDBPath = "//data//" + "com.cyberocw.habittodosecretary"
                    + "//databases//" + "habit_todo";

            if(!externalDataRoot.exists()) {
                externalDataRoot.mkdirs();
            }

            File currentDB = new File(data, currentDBPath);
            File backupDB = new File(externalDataRoot, "back_habit_todo");

            FileChannel src = new FileInputStream(currentDB).getChannel();
            FileChannel dst = new FileOutputStream(backupDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            return backupDB;

        } catch (Exception e) {
            Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "Backup Failed!" + e.getCause() + " " + e.getMessage());
            Toast.makeText(mCtx, mCtx.getString(R.string.message_failed), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
            return null;
        }
    }

    public void importDB() {
        try {
            //File sd = Environment.getExternalStorageDirectory();

            File sd = StorageHelper.getAttachmentDir(mCtx);

            File data = Environment.getDataDirectory();

            String currentDBPath = "//data//" + "com.cyberocw.habittodosecretary"
                    + "//databases//" + "habit_todo";
            //String backupDBPath = "//backup//back_habit_todo";
            //File backupDB = new File(data, currentDBPath);

            File backupDB = new File(sd, "back_habit_todo");
            File currentDB = new File(data, currentDBPath);

            if(!backupDB.exists()){
                for(File f : sd.listFiles()){
                    Log.d(this.toString(), "sd list file="+f.getAbsolutePath());
                }
                Log.e(this.toString(), "dbfile not found path=" + backupDB.getAbsolutePath());
            }

            FileChannel src = new FileInputStream(backupDB).getChannel();
            FileChannel dst = new FileOutputStream(currentDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();

            Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "Import Successful!");

        } catch (Exception e) {
            Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "Import Failed!" + e.getCause() + " " + e.getMessage());
            e.printStackTrace();
        }
    }
    public File getExternalStorage(){
        File sd = null;
        if (Build.VERSION.SDK_INT >= 19) {
            sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        }else{
            sd = new File(Environment.getExternalStorageDirectory() + "/Documents/");
        }
        if(!sd.exists()){
            sd = new File(Environment.getExternalStorageDirectory() + "/Documents/");
            boolean bMkdir = sd.mkdirs();
            Log.d(this.toString(), "bMkdir="+bMkdir);
        }

        return sd;
    }

    public void fileRestore(String pass){
        File sd = getExternalStorage();

        if (!sd.isDirectory()) {
            Toast.makeText(mCtx, "Can't Find Directory", Toast.LENGTH_SHORT).show();
            return;
        }

        File source = new File(sd, "ohreminder_backup.zip");
        File target = StorageHelper.getAttachmentDir(mCtx);

        if(target.isDirectory()){
            try {
                FileUtils.deleteDirectory(target);
                Log.d(this.toString(), "delresult= ok");
                target.mkdirs();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(this.toString(), "delresult= false");
            }

        }
        source.setReadable(true);
        Log.d(this.toString(), "source file is File = " + source.isFile() + " readable = " + source.canRead());

        try {
            ZipAsync zipAsync = new ZipAsync(mCtx, source, target, pass, true);
            zipAsync.execute();

            //다시 복사하는 후처리 해야 함
        } catch (Exception e) {
            Toast.makeText(mCtx, "zip failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        //File zipBackupDb = new File(dir, "back_habit_todo");
        //StorageHelper.copyFile(backupedDB, zipBackupDb);



    }
    public void fileBackup(String pass) {

        if(!StorageHelper.checkStorage()){
            Toast.makeText(mCtx, mCtx.getString(R.string.no_storage_auth), Toast.LENGTH_SHORT).show();
            return;
        }

        File sd = getExternalStorage();


        try {
            File targetZip = new File(sd.getAbsolutePath(), "ohreminder_backup.zip");
            if(targetZip.exists()){
                Log.d(this.toString(), "targetZip="+targetZip.getAbsolutePath());
                boolean result = targetZip.delete();
                Log.d(this.toString(), "zip file delete result="+result);
            }else{

            }
            //targetZip.createNewFile();

            ZipAsync zipAsync = new ZipAsync(mCtx, StorageHelper.getAttachmentDir(mCtx), targetZip, pass, false);
            zipAsync.execute();
        } catch (Exception e) {
            Toast.makeText(mCtx, "zip failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }


    }

    public class ZipAsync extends AsyncTask<Void, Boolean, String> {

        private ProgressDialog asyncDialog;
        private Context mCtx;
        private File mTargetFile;
        private File mSourceFile;
        private String mPass;
        boolean mIsDecode;

        public ZipAsync(Context context, File source, File target, String pass, boolean isDecode){
            mCtx = context;
            mTargetFile = target;
            mSourceFile = source;
            mPass = pass;
            mIsDecode = isDecode;

            asyncDialog = new ProgressDialog(mCtx);
        }

        @Override
        protected void onPreExecute() {
            try {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                if(!mIsDecode)
                    asyncDialog.setMessage("Backup ..");
                else
                    asyncDialog.setMessage("Restore ..");

                asyncDialog.show();
            }catch(Exception e){

            }
            // show dialog
            //asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... a) {
            try {
                if(!mSourceFile.exists())
                    return "file not found";

                if (mIsDecode) {
                    ZipFile zipFile = new ZipFile(mSourceFile);
                    zipFile.setPassword(mPass);
                    zipFile.extractAll(mTargetFile.getAbsolutePath());
                    importDB();
                }
                else{
                    Log.d(this.toString(), "mSourceFile="+mSourceFile.getAbsolutePath() + " dir="+mSourceFile.isDirectory());
                    Log.d(this.toString(), "mTargetFile="+mTargetFile.getAbsolutePath() + " isFile="+mTargetFile.isFile());

                    exportDB();

                    ZipFile zipFile = new ZipFile(mTargetFile);

                    ZipParameters parameters = new ZipParameters();
                    parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression
                    parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
                    parameters.setEncryptFiles(true);
                    parameters.setIncludeRootFolder(false);
                    parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
                    parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
                    parameters.setPassword(mPass);
                    //zipFile.createZipFile(mSourceFile, parameters);
                    zipFile.createZipFileFromFolder(mSourceFile, parameters, false, 0);
                    //zipFile.addFolder(mSourceFile, parameters);

                    //zipFile.addFolder(mSourceFile, parameters);
                }

            }catch (ZipException e){
                e.printStackTrace();
                if(e.getMessage().indexOf("Pass") > -1)
                    return mCtx.getString(R.string.password_are_diff);
                else{
                    return mCtx.getString(R.string.failed);
                }
            }
            return mCtx.getString(R.string.complete);

        }

        @Override
        protected void onPostExecute(String result) {
            File dir = StorageHelper.getAttachmentDir(mCtx);
            new File(dir, "back_habit_todo").deleteOnExit();

            if(asyncDialog != null)
                asyncDialog.dismiss();
            if(mCtx != null)
                Toast.makeText(mCtx, result, Toast.LENGTH_LONG).show();
            Log.d(this.toString(), "zip success");
            new AlarmDataManager(mCtx).resetMinAlarm();

        }
    }
}
