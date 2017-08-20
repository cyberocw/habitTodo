package com.cyberocw.habittodosecretary.file;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.common.vo.FileVO;
import com.cyberocw.habittodosecretary.keyword.vo.KeywordVO;
import com.cyberocw.habittodosecretary.memo.db.MemoDbManager;

import java.io.File;
import java.util.ArrayList;

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
    public void makeDataList(long memoId){
        this.dataList = mDb.getAttachList(memoId);
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
        //StorageHelper.delete(mCtx, vo.getUri().getPath());
        StorageHelper.deleteExternalStoragePrivateFile(mCtx, Uri.parse(vo.getUriPath())
                .getLastPathSegment());
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
}
