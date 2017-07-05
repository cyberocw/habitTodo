package com.cyberocw.habittodosecretary.keyword;

import android.app.ProgressDialog;
import android.content.Context;
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

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by cyber on 2017-07-06.
 */

public class KeywordAPI extends AsyncTask<Void, Void, String> {

    private ProgressDialog asyncDialog;
    KeywordDataManager keywordDataManager;
    KeywordListAdapter keywordListAdapter;

    private Context mCtx;

    private ArrayList<KeywordVO> dataList;

    public KeywordAPI(Context context, KeywordDataManager keywordDataManager, KeywordListAdapter keywordListAdapter){
        mCtx = context;
        dataList = new ArrayList<>();
        this.keywordDataManager = keywordDataManager;
        this.keywordListAdapter = keywordListAdapter;
        asyncDialog = new ProgressDialog(mCtx);

    }

    @Override
    protected void onPreExecute() {
        asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //asyncDialog.setMessage("공휴일 데이터 생성중입니다..");
        //asyncDialog.show();

        // show dialog
        //asyncDialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... arg0) {
        HolidaySync sync = new HolidaySync();


        String addr = "http://61.97.142.3/summary";

        StringBuilder html = new StringBuilder();
        try {
            URL url = new URL(addr);
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
                conn.setUseCaches(false);
                int resultcode = conn.getResponseCode();

                Log.d(Const.DEBUG_TAG, "resultcode="+resultcode);

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
        Log.d(Const.DEBUG_TAG, "html.toString()="+html.toString());
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
                Log.d(Const.DEBUG_TAG, "this.dataList="+this.dataList);
                this.dataList.add(vo);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "완료";
    }

    @Override
    protected void onPostExecute(String result) {
        asyncDialog.dismiss();

        Toast.makeText(mCtx, result, Toast.LENGTH_LONG).show();

        keywordDataManager.setDataList(this.dataList);
        keywordListAdapter.notifyDataSetChanged();
        /*Intent i = new Intent(mCtx, Intro.class);
        mCtx.startActivity(i);
        super.onPostExecute(result);*/
    }


}