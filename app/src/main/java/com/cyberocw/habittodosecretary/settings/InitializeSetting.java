package com.cyberocw.habittodosecretary.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.intro.Intro;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cyber on 2017-02-08.
 */

public class InitializeSetting extends AsyncTask<Void, Void, String> {

    private ProgressDialog asyncDialog;
    private SettingDataManager mSettingDataManager;
    private Context mCtx;
    //private ArrayList<Map> arrYearData = new ArrayList();
    HashMap<Integer, JSONObject> mYearMap = new HashMap<Integer, JSONObject>();

    public InitializeSetting(Context context){
        mCtx = context;
        asyncDialog = new ProgressDialog(mCtx);
        mSettingDataManager = new SettingDataManager(mCtx);
    }

    @Override
    protected void onPreExecute() {
        try {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("공휴일 데이터 생성중입니다..");
            asyncDialog.show();
        }catch(Exception e){

        }
        // show dialog
        //asyncDialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... arg0) {
        HolidaySync sync = new HolidaySync();

        int year = Calendar.getInstance().get(Calendar.YEAR) -1;
        String resultMsg = "";

        for(int i = 0 ; i < 4; i++) {
            JSONObject jObj = sync.getHolidayData(year + i);

            if(jObj != null) {
                mYearMap.put(year+i, jObj);
            }
        }

        resultMsg = "공휴일 데이터 동기화 완료";


        return resultMsg;
    }

    @Override
    protected void onPostExecute(String result) {
        for(Object year : mYearMap.keySet()){
            Log.d(this.toString(), "year="+year);
            mSettingDataManager.addItems(mYearMap.get(year), (Integer) year);
        }

        if(asyncDialog != null)
            asyncDialog.dismiss();
        if(mCtx != null)
            Toast.makeText(mCtx, result, Toast.LENGTH_LONG).show();

        /*Intent i = new Intent(mCtx, Intro.class);
        mCtx.startActivity(i);
        super.onPostExecute(result);*/
    }
}