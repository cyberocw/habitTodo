package com.cyberocw.habittodosecretary.keyword;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.keyword.vo.KeywordVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by cyber on 2017-07-06.
 */

public class KeywordDataManager {
    Context mCtx = null;
    int mLimit = 0;
    ArrayList<KeywordVO> dataList = new ArrayList();

    public KeywordDataManager(Context ctx) {
        mCtx = ctx;
    }

    public KeywordDataManager(Context ctx, int limit) {
        mCtx = ctx;
        mLimit = limit;
    }

    public void setDataList(ArrayList<KeywordVO> dataList) {
        ArrayList<KeywordVO> arr = new ArrayList<KeywordVO>();
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

    public KeywordVO getItem(int position){
        return this.dataList.get(position);
    }

}
