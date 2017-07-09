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
    
    ArrayList<KeywordVO> dataList = new ArrayList();

    public KeywordDataManager(Context ctx) {
        mCtx = ctx;
        //makeDataList();
    }

    public ArrayList<KeywordVO> getDataList() {
        return dataList;
    }

    public void makeDataList(){
        //this.dataList = mDb.getCategoryList();

        String addr = "http://61.97.142.3/summary";

        StringBuilder html = new StringBuilder();
        try {
            URL url = new URL(addr);
            //HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            HttpURLConnection conn = null;

            if (url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                conn = https;
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }

            if (conn != null) {
                conn.setRequestMethod("GET"); // get방식 통신
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-type", "application/json");
                //conn.setRequestProperty("TDCProjectKey", "1106e8d5-2f94-41a6-84ac-9683c68c9be6");
                conn.setConnectTimeout(10000);
                conn.setUseCaches(false);
                int resultcode = conn.getResponseCode();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    for (;;) {
                        String line = br.readLine();
                        if (line == null) break;
                        html.append(line + '\n');
                    }
                    br.close();
                }
                conn.disconnect();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Crashlytics.log(Log.ERROR, Const.ERROR_TAG, ex.getMessage());
        }
        //Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "year = " + year + ", result="+html.toString());

        try {
            JSONArray jArray = new JSONArray(html.toString());
            for(int i = 0 ; i< jArray.length(); i++){
                KeywordVO vo = new KeywordVO();
                JSONObject obj = jArray.getJSONObject(i);
                vo.setKeyword(obj.getString("keyword"));
                vo.setSimpleDate(obj.getLong("simpleDate"));
                vo.setRank(obj.getInt("rank"));
                vo.setTypeCode(obj.getInt("typeCode"));
                vo.setFromSite(obj.getString("fromSite"));
                vo.setId(obj.getLong("id"));
                dataList.add(vo);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }



        //return jObject;
    }

    public void setDataList(ArrayList<KeywordVO> dataList) {
        this.dataList = dataList;
    }

    public int getCount(){
        return this.dataList.size();
    }

    public KeywordVO getItem(int position){
        return this.dataList.get(position);
    }

    public KeywordVO getItemById(long id){
        for(int i = 0 ; i < dataList.size() ; i++){
            if(dataList.get(i).getId() == id){
                return dataList.get(i);
            }
        }
        return null;
    }

    public int getItemIndexById(long id){
        for(int i = 0 ; i < dataList.size() ; i++){
            if(dataList.get(i).getId() == id){
                return i;
            }
        }
        return -1;
    }
    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException {
                // TODO Auto-generated method stub

            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException {
                // TODO Auto-generated method stub

            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {

        @Override
        public boolean verify(String arg0, SSLSession arg1) {
            // TODO Auto-generated method stub
            return true;
        }
    };
}
