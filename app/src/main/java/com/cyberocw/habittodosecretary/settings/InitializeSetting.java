package com.cyberocw.habittodosecretary.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by cyber on 2017-02-08.
 */

public class InitializeSetting extends AsyncTask<Void, Void, String> {

    private ProgressDialog asyncDialog;
    private SettingDataManager mSettingDataManager;
    private Context mCtx;

    public InitializeSetting(Context context){
        mCtx = context;
        asyncDialog = new ProgressDialog(mCtx);
        mSettingDataManager = new SettingDataManager(mCtx);
    }

    @Override
    protected void onPreExecute() {
        asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        asyncDialog.setMessage("공휴일 데이터 생성중입니다..");
        asyncDialog.show();

        // show dialog
        //asyncDialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... arg0) {
        HolidaySync sync = new HolidaySync();

        int year = Calendar.getInstance().get(Calendar.YEAR) -1;
        String resultMsg = "";

        for(int i = 0 ; i < 7; i++) {
            JSONObject jObj = sync.getHolidayData(year + i);
            boolean result = mSettingDataManager.addItems(jObj, year + i);

            if (!result) {
                resultMsg = (year + i) + "년 공휴일 데이터 동기화에 실패했습니다";
                break;
            }
            else
                resultMsg = "공휴일 데이터 동기화 완료";

            mSettingDataManager.getList(year + i);
        }



        return resultMsg;
    }

    @Override
    protected void onPostExecute(String result) {
        asyncDialog.dismiss();

        Toast.makeText(mCtx, result, Toast.LENGTH_LONG).show();

        super.onPostExecute(result);
    }
}