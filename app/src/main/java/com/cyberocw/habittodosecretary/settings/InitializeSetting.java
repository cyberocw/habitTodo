package com.cyberocw.habittodosecretary.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.intro.Intro;

import org.json.JSONException;
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
        /*HolidaySync sync = new HolidaySync();

        int year = Calendar.getInstance().get(Calendar.YEAR) -1;*/
        String resultMsg = "";

        /*
        for(int i = 0 ; i < 6; i++) {
            JSONObject jObj = sync.getHolidayData(year + i);

            if(jObj != null) {
                mYearMap.put(year+i, jObj);
            }
        }
        */
        resultMsg = "공휴일 데이터 동기화 완료";


        return resultMsg;
    }

    @Override
    protected void onPostExecute(String result)  {
        try {
            String h2017 = "{\"results\":[{\"year\": \"2017\",\"month\": \"01\",\"day\": \"01\",\"type\": \"h\",\"name\": \"신정\"},{\"year\": \"2017\",\"month\": \"01\",\"day\": \"27\",\"type\": \"h\",\"name\": \"설날연휴\"},{\"year\": \"2017\",\"month\": \"01\",\"day\": \"28\",\"type\": \"h\",\"name\": \"설날\"},{\"year\": \"2017\",\"month\": \"01\",\"day\": \"29\",\"type\": \"h\",\"name\": \"설날연휴\"},{\"year\": \"2017\",\"month\": \"01\",\"day\": \"30\",\"type\": \"i\",\"name\": \"설날연휴 대체공휴일\"},{\"year\": \"2017\",\"month\": \"03\",\"day\": \"01\",\"type\": \"h\",\"name\": \"삼일절\"},{\"year\": \"2017\",\"month\": \"05\",\"day\": \"03\",\"type\": \"h\",\"name\": \"석가탄신일\"},{\"year\": \"2017\",\"month\": \"05\",\"day\": \"05\",\"type\": \"h\",\"name\": \"어린이날\"},{\"year\": \"2017\",\"month\": \"05\",\"day\": \"09\",\"type\": \"h\",\"name\": \"제 19 대 대통령 선거\"},{\"year\": \"2017\",\"month\": \"06\",\"day\": \"06\",\"type\": \"h\",\"name\": \"현충일\"},{\"year\": \"2017\",\"month\": \"08\",\"day\": \"15\",\"type\": \"h\",\"name\": \"광복절\"},{\"year\": \"2017\",\"month\": \"10\",\"day\": \"02\",\"type\": \"h\",\"name\": \"추석연휴(임시공휴일)\"},{\"year\": \"2017\",\"month\": \"10\",\"day\": \"03\",\"type\": \"h\",\"name\": \"개천절\"},{\"year\": \"2017\",\"month\": \"10\",\"day\": \"03\",\"type\": \"h\",\"name\": \"추석연휴\"},{\"year\": \"2017\",\"month\": \"10\",\"day\": \"04\",\"type\": \"h\",\"name\": \"추석\"},{\"year\": \"2017\",\"month\": \"10\",\"day\": \"05\",\"type\": \"h\",\"name\": \"추석연휴\"},{\"year\": \"2017\",\"month\": \"10\",\"day\": \"06\",\"type\": \"i\",\"name\": \"추석연휴 대체공휴일\"},{\"year\": \"2017\",\"month\": \"10\",\"day\": \"09\",\"type\": \"h\",\"name\": \"한글날\"},{\"year\": \"2017\",\"month\": \"12\",\"day\": \"25\",\"type\": \"h\",\"name\": \"성탄절\"}]}";
            String h2018 = "{\"results\":[{\"year\":\"2018\",\"month\":\"01\",\"day\":\"01\",\"type\":\"h\",\"name\":\"신정\"},{\"year\":\"2018\",\"month\":\"02\",\"day\":\"15\",\"type\":\"h\",\"name\":\"설날연휴\"},{\"year\":\"2018\",\"month\":\"02\",\"day\":\"16\",\"type\":\"h\",\"name\":\"설날\"},{\"year\":\"2018\",\"month\":\"02\",\"day\":\"17\",\"type\":\"h\",\"name\":\"설날연휴\"},{\"year\":\"2018\",\"month\":\"03\",\"day\":\"01\",\"type\":\"h\",\"name\":\"삼일절\"},{\"year\":\"2018\",\"month\":\"05\",\"day\":\"05\",\"type\":\"h\",\"name\":\"어린이날\"},{\"year\":\"2018\",\"month\":\"05\",\"day\":\"07\",\"type\":\"h\",\"name\":\"대체공휴일\"},{\"year\":\"2018\",\"month\":\"05\",\"day\":\"22\",\"type\":\"h\",\"name\":\"석가탄신일\"},{\"year\":\"2018\",\"month\":\"06\",\"day\":\"06\",\"type\":\"h\",\"name\":\"현충일\"},{\"year\":\"2018\",\"month\":\"06\",\"day\":\"13\",\"type\":\"h\",\"name\":\"지방선거\"},{\"year\":\"2018\",\"month\":\"08\",\"day\":\"15\",\"type\":\"h\",\"name\":\"광복절\"},{\"year\":\"2018\",\"month\":\"09\",\"day\":\"24\",\"type\":\"h\",\"name\":\"추석\"},{\"year\":\"2018\",\"month\":\"09\",\"day\":\"25\",\"type\":\"h\",\"name\":\"추석연휴\"},{\"year\":\"2018\",\"month\":\"09\",\"day\":\"26\",\"type\":\"h\",\"name\":\"대체공휴일\"},{\"year\":\"2018\",\"month\":\"10\",\"day\":\"03\",\"type\":\"h\",\"name\":\"개천절\"},{\"year\":\"2018\",\"month\":\"10\",\"day\":\"09\",\"type\":\"h\",\"name\":\"한글날\"},{\"year\":\"2018\",\"month\":\"12\",\"day\":\"25\",\"type\":\"h\",\"name\":\"성탄절\"}]}";
            String h2019 = "{\"results\":[{\"year\":\"2019\",\"month\":\"01\",\"day\":\"01\",\"type\":\"h\",\"name\":\"신정\"},{\"year\":\"2019\",\"month\":\"02\",\"day\":\"04\",\"type\":\"h\",\"name\":\"설날연휴\"},{\"year\":\"2019\",\"month\":\"02\",\"day\":\"05\",\"type\":\"h\",\"name\":\"설날\"},{\"year\":\"2019\",\"month\":\"02\",\"day\":\"06\",\"type\":\"h\",\"name\":\"설날연휴\"},{\"year\":\"2019\",\"month\":\"03\",\"day\":\"01\",\"type\":\"h\",\"name\":\"삼일절\"},{\"year\":\"2019\",\"month\":\"05\",\"day\":\"05\",\"type\":\"h\",\"name\":\"어린이날\"},{\"year\":\"2019\",\"month\":\"05\",\"day\":\"12\",\"type\":\"h\",\"name\":\"석가탄신일\"},{\"year\":\"2019\",\"month\":\"06\",\"day\":\"06\",\"type\":\"h\",\"name\":\"현충일\"},{\"year\":\"2019\",\"month\":\"08\",\"day\":\"15\",\"type\":\"h\",\"name\":\"광복절\"},{\"year\":\"2019\",\"month\":\"09\",\"day\":\"12\",\"type\":\"h\",\"name\":\"추석연휴\"},{\"year\":\"2019\",\"month\":\"09\",\"day\":\"13\",\"type\":\"h\",\"name\":\"추석\"},{\"year\":\"2019\",\"month\":\"10\",\"day\":\"03\",\"type\":\"h\",\"name\":\"개천절\"},{\"year\":\"2019\",\"month\":\"10\",\"day\":\"09\",\"type\":\"h\",\"name\":\"한글날\"},{\"year\":\"2019\",\"month\":\"12\",\"day\":\"25\",\"type\":\"h\",\"name\":\"성탄절\"}]}";
            String h2020 = "{\"results\":[{\"year\":\"2020\",\"month\":\"01\",\"day\":\"01\",\"type\":\"h\",\"name\":\"신정\"},{\"year\":\"2020\",\"month\":\"01\",\"day\":\"24\",\"type\":\"h\",\"name\":\"설날연휴\"},{\"year\":\"2020\",\"month\":\"01\",\"day\":\"25\",\"type\":\"h\",\"name\":\"설날\"},{\"year\":\"2020\",\"month\":\"03\",\"day\":\"01\",\"type\":\"h\",\"name\":\"삼일절\"},{\"year\":\"2020\",\"month\":\"04\",\"day\":\"15\",\"type\":\"h\",\"name\":\"제21대 국회의원 선거\"},{\"year\":\"2020\",\"month\":\"04\",\"day\":\"30\",\"type\":\"h\",\"name\":\"석가탄신일\"},{\"year\":\"2020\",\"month\":\"05\",\"day\":\"05\",\"type\":\"h\",\"name\":\"어린이날\"},{\"year\":\"2020\",\"month\":\"06\",\"day\":\"06\",\"type\":\"h\",\"name\":\"현충일\"},{\"year\":\"2020\",\"month\":\"08\",\"day\":\"15\",\"type\":\"h\",\"name\":\"광복절\"},{\"year\":\"2020\",\"month\":\"09\",\"day\":\"30\",\"type\":\"h\",\"name\":\"추석연휴\"},{\"year\":\"2020\",\"month\":\"10\",\"day\":\"01\",\"type\":\"h\",\"name\":\"추석\"},{\"year\":\"2020\",\"month\":\"10\",\"day\":\"02\",\"type\":\"h\",\"name\":\"추석연휴\"},{\"year\":\"2020\",\"month\":\"10\",\"day\":\"03\",\"type\":\"h\",\"name\":\"개천절\"},{\"year\":\"2020\",\"month\":\"10\",\"day\":\"09\",\"type\":\"h\",\"name\":\"한글날\"},{\"year\":\"2020\",\"month\":\"12\",\"day\":\"25\",\"type\":\"h\",\"name\":\"성탄절\"}]}";
            String h2021 = "{\"results\" :[{\"year\":\"2021\",\"month\":\"01\",\"day\":\"01\",\"type\":\"h\",\"name\":\"신정\"},{\"year\":\"2021\",\"month\":\"02\",\"day\":\"11\",\"type\":\"h\",\"name\":\"설날연휴\"},{\"year\":\"2021\",\"month\":\"02\",\"day\":\"12\",\"type\":\"h\",\"name\":\"설날\"},{\"year\":\"2021\",\"month\":\"03\",\"day\":\"01\",\"type\":\"h\",\"name\":\"삼일절\"},{\"year\":\"2021\",\"month\":\"05\",\"day\":\"05\",\"type\":\"h\",\"name\":\"어린이날\"},{\"year\":\"2021\",\"month\":\"05\",\"day\":\"19\",\"type\":\"h\",\"name\":\"석가탄신일\"},{\"year\":\"2021\",\"month\":\"06\",\"day\":\"06\",\"type\":\"h\",\"name\":\"현충일\"},{\"year\":\"2021\",\"month\":\"08\",\"day\":\"15\",\"type\":\"h\",\"name\":\"광복절\"},{\"year\":\"2021\",\"month\":\"09\",\"day\":\"20\",\"type\":\"h\",\"name\":\"추석연휴\"},{\"year\":\"2021\",\"month\":\"09\",\"day\":\"21\",\"type\":\"h\",\"name\":\"추석\"},{\"year\":\"2021\",\"month\":\"09\",\"day\":\"22\",\"type\":\"h\",\"name\":\"추석연휴\"},{\"year\":\"2021\",\"month\":\"10\",\"day\":\"03\",\"type\":\"h\",\"name\":\"개천절\"},{\"year\":\"2021\",\"month\":\"10\",\"day\":\"09\",\"type\":\"h\",\"name\":\"한글날\"},{\"year\":\"2021\",\"month\":\"12\",\"day\":\"25\",\"type\":\"h\",\"name\":\"성탄절\"}]}";
            String h2022 = "{\"results\" :[{\"year\":\"2022\",\"month\":\"01\",\"day\":\"01\",\"type\":\"h\",\"name\":\"신정\"},{\"year\":\"2022\",\"month\":\"01\",\"day\":\"31\",\"type\":\"h\",\"name\":\"설날연휴\"},{\"year\":\"2022\",\"month\":\"02\",\"day\":\"01\",\"type\":\"h\",\"name\":\"설날\"},{\"year\":\"2022\",\"month\":\"02\",\"day\":\"02\",\"type\":\"h\",\"name\":\"설날연휴\"},{\"year\":\"2022\",\"month\":\"03\",\"day\":\"01\",\"type\":\"h\",\"name\":\"삼일절\"},{\"year\":\"2022\",\"month\":\"05\",\"day\":\"05\",\"type\":\"h\",\"name\":\"어린이날\"},{\"year\":\"2022\",\"month\":\"05\",\"day\":\"08\",\"type\":\"h\",\"name\":\"석가탄신일\"},{\"year\":\"2022\",\"month\":\"06\",\"day\":\"06\",\"type\":\"h\",\"name\":\"현충일\"},{\"year\":\"2022\",\"month\":\"08\",\"day\":\"15\",\"type\":\"h\",\"name\":\"광복절\"},{\"year\":\"2022\",\"month\":\"09\",\"day\":\"09\",\"type\":\"h\",\"name\":\"추석연휴\"},{\"year\":\"2022\",\"month\":\"09\",\"day\":\"10\",\"type\":\"h\",\"name\":\"추석\"},{\"year\":\"2022\",\"month\":\"10\",\"day\":\"03\",\"type\":\"h\",\"name\":\"개천절\"},{\"year\":\"2022\",\"month\":\"10\",\"day\":\"09\",\"type\":\"h\",\"name\":\"한글날\"},{\"year\":\"2022\",\"month\":\"12\",\"day\":\"21\",\"type\":\"h\",\"name\":\"제21대 대통령 선거\"},{\"year\":\"2022\",\"month\":\"12\",\"day\":\"25\",\"type\":\"h\",\"name\":\"성탄절\"}]}";

            Log.d(this.toString(), "mYearMap push new");

            mYearMap.put(2017, new JSONObject(h2017));
            mYearMap.put(2018, new JSONObject(h2018));
            mYearMap.put(2019, new JSONObject(h2019));
            mYearMap.put(2020, new JSONObject(h2020));
            mYearMap.put(2021, new JSONObject(h2021));
            mYearMap.put(2022, new JSONObject(h2022));

        } catch (JSONException e) {

            Log.e(this.toString(), "holiday json make failed : {}", e);
        }
        for(Object year : mYearMap.keySet()){
            Log.d(this.toString(), "year map year="+year);
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