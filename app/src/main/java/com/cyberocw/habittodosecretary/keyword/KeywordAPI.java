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

public class KeywordAPI extends AsyncTask<String, Void, String> {

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
        asyncDialog.setMessage("데이터 수신중..");
        asyncDialog.show();

        // show dialog
        //asyncDialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... addr) {

        //String addr = "http://61.97.142.3/summary";

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
                conn.setUseCaches(false); // 캐싱데이터를 받을지 안받을지
                conn.setDefaultUseCaches(false); // 캐싱데이터 디폴트 값 설정

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
            this.dataList = new ArrayList<>();

            JSONArray jArray = new JSONArray(html.toString());
            for(int i = 0 ; i< jArray.length(); i++){
                KeywordVO vo = new KeywordVO();
                JSONObject obj = jArray.getJSONObject(i);
                vo.setKeyword(obj.getString("keyword"));
                vo.setSimpleDate(obj.getLong("simpleDate"));
                vo.setTypeCode(obj.getInt("typeCode"));

                if(obj.getInt("rank") == -1)
                    vo.setRank(i+1);
                else
                    vo.setRank(obj.getInt("rank"));


                if(vo.getTypeCode() == 1){
                    if(!obj.getString("rankNAVER").equals("null"))
                        vo.setRankNAVER(obj.getString("rankNAVER"));
                    if(!obj.getString("rankDAUM").equals("null"))
                        vo.setRankDAUM(obj.getString("rankDAUM"));
                    if(!obj.getString("rankZUM").equals("null"))
                        vo.setRankZUM(obj.getString("rankZUM"));
                }

                vo.setFromSite(obj.getString("fromSite"));
                vo.setId(obj.getLong("id"));

                this.dataList.add(vo);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return "실패";
        }

        return "완료";
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
        /*Intent i = new Intent(mCtx, Intro.class);
        mCtx.startActivity(i);
        super.onPostExecute(result);*/
    }


}