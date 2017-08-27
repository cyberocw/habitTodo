package com.cyberocw.habittodosecretary.file;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.common.vo.FileVO;
import com.cyberocw.habittodosecretary.keyword.vo.KeywordVO;
import com.cyberocw.habittodosecretary.memo.db.MemoDbManager;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by cyber on 2017-08-15.
 */

public class FileDataManager {
    Context mCtx = null;
    ArrayList<FileVO> dataList = new ArrayList();
    ArrayList<FileVO> delDataList = new ArrayList();
    FileDbManager mDb;

    public FileDataManager(Context ctx) {
        mCtx = ctx;
        mDb = FileDbManager.getInstance(ctx);
    }

    public void setDataList(ArrayList<FileVO> dataList) {
        this.dataList = dataList;
    }

    public ArrayList<FileVO> getDataList(){
        return this.dataList;
    }
    public void makeDataList(String type, long fId){
        this.dataList = mDb.getAttachList(type, fId);
    }

    public void deleteTrash(){
        Crashlytics.log(Log.DEBUG, this.toString(), "trash start");
        ArrayList<FileVO> deleteList = mDb.deleteTrash();
        if(deleteList != null) {
            for (int i = 0; i < deleteList.size(); i++) {
                deleteFile(deleteList.get(i));
            }
        }
        Crashlytics.log(Log.DEBUG, this.toString(), "trash end");
    }

    public ArrayList<FileVO> getDelDataList(){
        return this.delDataList;
    }
    public int getCount(){
        return this.dataList.size();
    }

    public FileVO getItem(int position){
        return this.dataList.get(position);
    }

    public boolean addItem(FileVO item){
        mDb.insert(item);
        if(item.getId() == -1){
            Toast.makeText(mCtx, mCtx.getString(R.string.db_failed_generate_id), Toast.LENGTH_LONG).show();
            return false;
        }
        this.dataList.add(item);
        return true;
    }

    public void addDeleteItem(long id){
        FileVO vo = getById(id);
        if(vo != null)
            delDataList.add(vo);
        else
            Toast.makeText(mCtx, "Not found File", Toast.LENGTH_SHORT).show();
    }

    public void addDeleteItem(ArrayList<FileVO> arr){
        if(arr != null)
            delDataList.addAll(arr);
    }


    public void deleteFile(FileVO vo){
        Crashlytics.log(Log.DEBUG, this.toString(), " deleteFile realFile ="+vo.toString());
        //StorageHelper.delete(mCtx, vo.getUri().getPath());
        StorageHelper.deleteExternalStoragePrivateFile(mCtx, Uri.parse(vo.getUriPath())
                .getLastPathSegment());
    }
    public void deleteFile(FileVO vo, String dir){
        Crashlytics.log(Log.DEBUG, this.toString(), " dir = " + dir + "   deleteFile with dir realFile ="+vo.toString());
        //StorageHelper.delete(mCtx, vo.getUri().getPath());
        StorageHelper.deleteExternalStoragePrivateFile(mCtx, Uri.parse(vo.getUriPath())
                .getLastPathSegment(), dir);
    }
    public FileVO getById(long id){
        if(id > -1 && dataList != null) {
            for (int i = 0; i < dataList.size(); i++) {
                if (dataList.get(i).getId() == id) {
                    return dataList.get(i);
                }
            }
        }
        return null;
    }
    public boolean delete(long id){
        FileVO vo = getById(id);
        if(vo == null){
            return false;
        }
        mDb.delete(vo.getId(), null);
        this.deleteFile(vo);
        deleteInList(id);
        return true;
    }
    public boolean delete(FileVO vo){
        if(vo == null){
            return false;
        }
        mDb.delete(vo.getId(), null);
        this.deleteFile(vo);
        deleteInList(vo.getId());
        return true;
    }
    /*public boolean delete(ArrayList<FileVO> arr){
        if(arr != null){
            return mDb.delete(arr);
        }
        return false;
    }*/

    public void deleteInList(long id){
        for(int i = 0 ; i < this.delDataList.size(); i++){
            if(this.delDataList.get(i).getId() == id) {
                delDataList.remove(i);
                break;
            }
        }
    }
    public void deleteAll(){
        mDb.delete(this.delDataList);
        for(int i = 0 ; i < this.delDataList.size(); i++){
            this.deleteFile(delDataList.get(i));
        }
        delDataList = new ArrayList<FileVO>();
    }
    public void deleteAll(String dir){
        mDb.delete(this.delDataList);
        for(int i = 0 ; i < this.delDataList.size(); i++){
            this.deleteFile(delDataList.get(i), dir);
        }
        delDataList = new ArrayList<FileVO>();
    }

    public void makeDataListAll() {
        this.dataList = mDb.getAttachListAll();
    }

    public void migrationFile(Context ctx){

        Log.d(this.toString(), "MIGRATION START time=" + CommonUtils.convertFullDateType(Calendar.getInstance()));
        File voiceFolder = new File(ctx.getFilesDir().getAbsolutePath() + File.separator + "voice");
        AlarmDataManager alarmDataManager = new AlarmDataManager(ctx);

        if(voiceFolder.isDirectory()){
            for(File file : voiceFolder.listFiles()){
                Log.d(this.toString(), "ori file="+file.getAbsolutePath());
            }
            File[] files = voiceFolder.listFiles();
            if(files == null)
                return;
            boolean result;
            ArrayList<FileVO> reultFileList = new ArrayList<FileVO>();
            for(int i = 0; i < files.length; i++){
                String fileName = files[i].getName();
                long alarmId = Long.parseLong(fileName.substring(0, fileName.lastIndexOf(".")));
                AlarmVO alarmVO = alarmDataManager.getItemByIdInDB(alarmId);
                if(alarmVO == null){
                    //삭제 할지 말지 결정 필요
                    //files[i].deleteOnExit();
                    //CommonUtils.logCustomEvent("nullFile", 1);
                    continue;
                }

                File targetFile = StorageHelper.createNewAttachmentFile(mCtx, Environment.DIRECTORY_RINGTONES, ".wav");
                result = StorageHelper.copyFile(files[i], targetFile);
                if(!result){
                    Crashlytics.log(Log.ERROR, this.toString(), "기존 파일 복사 실패");
                }
                FileVO fileVO = new FileVO(Uri.fromFile(targetFile), Const.MIME_TYPE_AUDIO_WAV);
                fileVO.setName(targetFile.getName());
                fileVO.setSize(targetFile.length());
                fileVO.setfId(alarmId);
                fileVO.setType(Const.ETC_TYPE.ALARM);
                //db만 insert
                this.addItem(fileVO);

                reultFileList.add(fileVO);
                Crashlytics.log(Log.DEBUG, this.toString(), "mig new fileVO = " + fileVO.toString());
            }
            //Toast.makeText(ctx, "reultFileList size="+reultFileList.size(), Toast.LENGTH_SHORT).show();
            Crashlytics.log(Log.DEBUG, this.toString(), "reultFileList size="+reultFileList.size());
            //마이그레이션 이전으로 롤백 필요 (여러번 테스트 위해)
/*
            this.addDeleteItem(reultFileList);
            this.deleteAll(Environment.DIRECTORY_RINGTONES);*/

            Log.d(this.toString(), "MIGRATION END time=" + CommonUtils.convertFullDateType(Calendar.getInstance()));
            Toast.makeText(ctx, "마이그레이션 완료", Toast.LENGTH_SHORT).show();
        }
        else{
            Crashlytics.log(Log.DEBUG, this.toString(), " not a dir");
        }
    }

}
