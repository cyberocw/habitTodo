package com.cyberocw.habittodosecretary.record;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;

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

    public boolean saveFile(String targetFileName){
        String fromFileName = Const.RECORDER.CACHE_FILE_NAME;
        return this.saveFile(mCtx.getExternalCacheDir().getAbsolutePath() + File.separator  + fromFileName, targetFileName);
    }
    public boolean saveFile(String fromFileName, String targetFileName){
        String FILENAME = targetFileName;
        File fFromFile = new File(fromFileName);
        Log.d(this.toString(), "fFromFile is File="+ fFromFile.isFile());

        if(fFromFile.isFile()){
            FileInputStream fi = null;
            FileOutputStream fos = null;
            try {
                fi = new FileInputStream(fFromFile);
                File rootDir=new File(mCtx.getFilesDir(), "voice");
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

    public void deleteRecordFile(long id) {
        File rootDir = new File(mCtx.getFilesDir(), "voice");
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
