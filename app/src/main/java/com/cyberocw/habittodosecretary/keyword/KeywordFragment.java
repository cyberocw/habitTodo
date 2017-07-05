package com.cyberocw.habittodosecretary.keyword;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import io.fabric.sdk.android.Fabric;

/**
 * Created by cyber on 2017-07-05.
 */

public class KeywordFragment extends Fragment {
    SharedPreferences mPrefs;
    private View mView;
    private Context mCtx;
    KeywordDataManager mKeywordDataManager;
    KeywordListAdapter mKeywordAdapter;
    public KeywordFragment(){

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_keyword, container, false);
/*
        if(!CommonUtils.isLocaleKo(getResources().getConfiguration()))
            mView.findViewById(R.id.holidayOptionWrap).setVisibility(View.GONE);
*/

        return mView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mCtx = getActivity();

        initActivity();
        Fabric.with(mCtx, new Crashlytics());
    }

    private void initActivity(){
        mKeywordDataManager = new KeywordDataManager(mCtx);
        mKeywordAdapter = new KeywordListAdapter( mCtx, mKeywordDataManager);

        ListView lv = (ListView) mView.findViewById(R.id.keywordListView);
        lv.setAdapter(mKeywordAdapter);

        KeywordAPI keywordAPI = new KeywordAPI(mCtx, mKeywordDataManager, mKeywordAdapter);
        keywordAPI.execute();

        mPrefs = mCtx.getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);

        //bindBtnEvent();
    }
    private void bindBtnEvent(){

    }

    public interface RefreshKeyword{
        void refreshKeyword();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
