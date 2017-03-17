package com.cyberocw.habittodosecretary.settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.category.CategoryDataManager;
import com.cyberocw.habittodosecretary.category.CategoryListAdapter;

import org.json.JSONObject;

import java.util.Calendar;

import io.fabric.sdk.android.Fabric;

/**
 * Created by cyberocw on 2016-11-06.
 */
public class SettingFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View mView;
    private Context mCtx;
    private SettingDataManager mSettingDataManager;

    SharedPreferences mPrefs;
    CategoryDataManager mCateDataManager;
    CategoryListAdapter mCateAdapter;
    private OnFragmentInteractionListener mListener;
    private AlertDialog mCatePopupBilder;
    private EditText mEtCateTitle;

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance(Context param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_setting, container, false);
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
        mSettingDataManager = new SettingDataManager(mCtx);
        bindBtnEvent();
    }

    private void bindBtnEvent(){
        Button btnHlidaySync = (Button) mView.findViewById(R.id.btnHolidaySync);
        Button btnDbBackup = (Button) mView.findViewById(R.id.btnDbBackup);
        Button btnDbRetore= (Button) mView.findViewById(R.id.btnDbRestore);

        btnDbBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Const.DEBUG_TAG, "onclick backup");
                mSettingDataManager.exportDB();
            }
        });

        btnDbRetore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingDataManager.importDB();
            }
        });

        btnHlidaySync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holidaySync();
            }
        });
    }

    //공휴일 데이터 동기화
    private void holidaySync(){
        //CheckTypesTask task = new CheckTypesTask();
        //task.execute();
        InitializeSetting is = new InitializeSetting(mCtx);
        is.execute();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}


