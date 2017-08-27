package com.cyberocw.habittodosecretary.record;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.file.StorageHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by cyber on 2017-07-28.
 */

public class RecorderDataManager {
    Context mCtx;
    public RecorderDataManager(Context mCtx){
        this.mCtx = mCtx;
    }

    public boolean saveFile(String fromFileName, File targetFile){
        //File f = StorageHelper.createNewAttachmentFile(mCtx, Environment.DIRECTORY_RINGTONES, ".wav");//Const.RECORDER.CACHE_FILE_NAME;
        File fFromFile = new File(fromFileName);
        Crashlytics.log(Log.DEBUG, this.toString(), "saveFile from=" + fromFileName + " target=" + targetFile.getAbsolutePath());
        return StorageHelper.copyFile(fFromFile, targetFile);
        //return this.saveFile(mCtx.getExternalCacheDir().getAbsolutePath() + File.separator  + fromFileName, targetFileName);
    }
    //fromFileName 은 mPlayingFile 인 경우만 있음
    public boolean saveFile(String fromFileName, String targetFileName, boolean aa){
        String FILENAME = targetFileName;
        File fFromFile = new File(fromFileName);
        Log.d(this.toString(), "fFromFile is File="+ fFromFile.isFile());

        if(fFromFile.isFile()){
            FileInputStream fi = null;
            FileOutputStream fos = null;
            try {
                fi = new FileInputStream(fFromFile);
                File rootDir = new File(mCtx.getFilesDir(), Environment.DIRECTORY_RINGTONES);
                rootDir.mkdirs();
                fos = new FileOutputStream(new File(rootDir, FILENAME));
                byte[] buffer = new byte[16384];
                int bytesRead;
                while ((bytesRead = fi.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    fi.close();
                    fos.close();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public void deleteRecordFile(String filePath) {
        StorageHelper.delete(mCtx, filePath);
    }

    public void deleteRecordFile(long id) {
        File rootDir = new File(mCtx.getFilesDir(), Environment.DIRECTORY_RINGTONES);
        Log.d(Const.DEBUG_TAG, "deleteRecordFile start id="+id + " rootDir.isDirectory=" + rootDir.isDirectory());
        if(rootDir.isDirectory()){
            File f = new File(rootDir, id+".wav");
            Log.d(Const.DEBUG_TAG, "file id is file = ="+ f.isFile());
            if(f.isFile()){
                Log.d(Const.DEBUG_TAG, "SOUND FILE IS");
                f.delete();
            }
        }
    }
}
