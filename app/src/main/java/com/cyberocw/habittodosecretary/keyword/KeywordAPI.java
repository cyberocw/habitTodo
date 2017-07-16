package com.cyberocw.habittodosecretary.keyword;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.keyword.vo.KeywordVO;
import com.cyberocw.habittodosecretary.settings.HolidaySync;
import com.cyberocw.habittodosecretary.settings.SettingDataManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by cyber on 2017-07-06.
 */

public class KeywordAPI extends AsyncTask<String, Void, String> {

    private ProgressDialog asyncDialog;
    KeywordDataManager keywordDataManager;
    KeywordListAdapter keywordListAdapter;
    String mUrl;
    private Context mCtx;
    SharedPreferences mPrefs;
    private ArrayList<KeywordVO> dataList;

    public KeywordAPI(Context context, KeywordDataManager keywordDataManager, KeywordListAdapter keywordListAdapter){
        mCtx = context;
        dataList = new ArrayList<>();
        this.keywordDataManager = keywordDataManager;
        this.keywordListAdapter = keywordListAdapter;
        asyncDialog = new ProgressDialog(mCtx);
        mPrefs = mCtx.getSharedPreferences(Const.KEYWORD.PARAM.PREFS_CACHE, Context.MODE_PRIVATE);
        Map<String, ?> map = mPrefs.getAll();
        Crashlytics.log(Log.DEBUG, this.toString(), "keyword cache size="+map.size());

        if(map.size() > 10){
            mPrefs.edit().clear().commit();
        }
    }

    @Override
    protected void onPreExecute() {
        asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        asyncDialog.setMessage("데이터 수신중..");
        asyncDialog.show();

        // show dialog
        //asyncDialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... addr) {
        mUrl = addr[0];
        //String addr = "http://61.97.142.3/summary";
        String cache = getCache(mUrl);
        if(cache != null){
            try {
                makeData(cache);
                return "성공";
            } catch (JSONException e) {
                e.printStackTrace();
                return "실패";
            }
        }

        StringBuilder html = new StringBuilder();
        try {
            URL url = new URL(addr[0]);
            //HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            HttpURLConnection conn = null;

            if (url.getProtocol().toLowerCase().equals("https")) {
                /*trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                conn = https;*/
            } else {

                conn = (HttpURLConnection) url.openConnection();
            }

            if (conn != null) {
                conn.setRequestMethod("GET"); // get방식 통신
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-type", "application/json");
                //conn.setRequestProperty("TDCProjectKey", "1106e8d5-2f94-41a6-84ac-9683c68c9be6");
                conn.setConnectTimeout(10000);
                conn.setUseCaches(true); // 캐싱데이터를 받을지 안받을지
                conn.setDefaultUseCaches(true); // 캐싱데이터 디폴트 값 설정

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
            String resData = html.toString();
            makeData(resData);

            if(this.dataList.size() >= 10) {
                storeCache(mUrl, resData);
                return "완료";
            }
            else{
                return "실패";
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "실패";
        }


    }

    public void makeData(String html) throws JSONException{
        this.dataList = new ArrayList<>();

        JSONArray jArray = new JSONArray(html.toString());
        for(int i = 0 ; i< jArray.length(); i++) {
            KeywordVO vo = new KeywordVO();
            JSONObject obj = jArray.getJSONObject(i);
            vo.setKeyword(obj.getString("keyword"));
            vo.setSimpleDate(obj.getLong("simpleDate"));
            vo.setTypeCode(obj.getInt("typeCode"));

            if (obj.getInt("rank") == -1)
                vo.setRank(i + 1);
            else
                vo.setRank(obj.getInt("rank"));


            if (vo.getTypeCode() == 1) {
                if (!obj.getString("rankNAVER").equals("null"))
                    vo.setRankNAVER(obj.getString("rankNAVER"));
                if (!obj.getString("rankDAUM").equals("null"))
                    vo.setRankDAUM(obj.getString("rankDAUM"));
                if (!obj.getString("rankZUM").equals("null"))
                    vo.setRankZUM(obj.getString("rankZUM"));
            }

            vo.setFromSite(obj.getString("fromSite"));
            vo.setId(obj.getLong("id"));

            this.dataList.add(vo);
        }
    }

    private String getCache(String url){
        return mPrefs.getString(url, null);
    }

    //통신을 통해서 가져온 데이터만 호출함
    private void storeCache(String url, String html){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(url, html);
        editor.commit();
    }

    @Override
    protected void onPostExecute(String result) {
        asyncDialog.dismiss();

        if(result.equals("실패")) {
            Toast.makeText(mCtx, result, Toast.LENGTH_LONG).show();
            return;
        }
        keywordDataManager.setDataList(this.dataList);
        keywordListAdapter.notifyDataSetChanged();

    }


}