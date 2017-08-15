package com.cyberocw.habittodosecretary.file;

import android.content.Context;

import com.cyberocw.habittodosecretary.common.vo.FileVO;
import com.cyberocw.habittodosecretary.keyword.vo.KeywordVO;

import java.util.ArrayList;

/**
 * Created by cyber on 2017-08-15.
 */

public class FileDataManager {
    Context mCtx = null;
    int mLimit = 0;
    ArrayList<FileVO> dataList = new ArrayList();

    public FileDataManager(Context ctx) {
        mCtx = ctx;
    }

    public void setDataList(ArrayList<FileVO> dataList) {
        ArrayList<FileVO> arr = new ArrayList<FileVO>();
        if(mLimit > 0){
            arr.addAll(dataList.subList(0, mLimit));
            this.dataList = arr;
        }
        else{
            this.dataList = dataList;
        }
    }

    public int getCount(){
        return this.dataList.size();
    }

    public FileVO getItem(int position){
        return this.dataList.get(position);
    }
}
