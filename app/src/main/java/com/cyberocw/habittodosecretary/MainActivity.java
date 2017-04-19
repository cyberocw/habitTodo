package com.cyberocw.habittodosecretary;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.alaram.AlarmFragment;
import com.cyberocw.habittodosecretary.category.CategoryFragment;
import com.cyberocw.habittodosecretary.memo.MemoFragment;
import com.cyberocw.habittodosecretary.settings.InitializeSetting;
import com.cyberocw.habittodosecretary.settings.SettingFragment;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements AlarmFragment.OnFragmentInteractionListener, CategoryFragment.OnFragmentInteractionListener, MemoFragment.OnFragmentInteractionListener,
		SettingFragment.OnFragmentInteractionListener,
		NavigationView.OnNavigationItemSelectedListener {
    public AlarmFragment mMainFragment;
    public static String TAG = "mainActivity";

	private NavigationView mNavigationView;
	private DrawerLayout mDrawer;

	private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
        actionBar.setDisplayHomeAsUpEnabled(true);

	    mDrawer = (DrawerLayout)findViewById(R.id.dl_activity_main_drawer);
	    mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
	    mNavigationView.setNavigationItemSelectedListener(this);

	    FragmentManager fragmentManager = getSupportFragmentManager();

		Fragment fragment;
		Intent intent = getIntent();

		if(intent != null && intent.getExtras() != null) {
			Bundle bundle = intent.getExtras();
			if (bundle.containsKey(Const.PARAM.REQ_CODE)) {
				NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
				int reqCode = bundle.getInt(Const.PARAM.REQ_CODE);
				Crashlytics.log(Log.DEBUG, this.toString(), "cancel reqCode=" + reqCode);
				manager.cancel(reqCode);
			}
			if(!bundle.getString(Const.PARAM.ETC_TYPE_KEY, "").equals("")){
				Crashlytics.log(Log.DEBUG, this.toString(), "bundle.getString(Const.PARAM.ETC_TYPE_KEY)="+bundle.getString(Const.PARAM.ETC_TYPE_KEY));
				if(bundle.getString(Const.PARAM.ETC_TYPE_KEY).equals(Const.ETC_TYPE.MEMO)){
					fragment = new MemoFragment();
				}else{
					Toast.makeText(getApplicationContext(), "etcType이 잘못 되었습니다", Toast.LENGTH_LONG).show();
					Log.e(this.toString(), "버그! etcType이 잘못 되었습니다 etcType="+ bundle.getString("etcType"));
					fragment = new AlarmFragment();
				}
			}else{
				fragment = new AlarmFragment();
			}
			fragment.setArguments(bundle);
		}else
			fragment = new AlarmFragment();



	    fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
			    .replace(R.id.main_container, fragment).commit();

		afterUpdateVersion();


    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "onResume start");

		if(intent != null) {
			Bundle bundle = intent.getExtras();

			if (bundle != null) {

				NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
				int reqCode = bundle.getInt(Const.PARAM.REQ_CODE);
				Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "reqCode=" + reqCode);
				manager.cancel(reqCode);

				FragmentManager fragmentManager = getSupportFragmentManager();

				Fragment alarmFragment = new AlarmFragment();

				alarmFragment.setArguments(intent.getExtras());

				fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
						.replace(R.id.main_container, alarmFragment).commit();

				//afterUpdateVersion();
			} else {
				Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, " on resume bundle is null");
			}
			setIntent(intent);
		}
	}

	private void afterUpdateVersion(){
		Context ctx = getApplicationContext();
		SharedPreferences setPrefs = ctx.getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);

		String prefsSavedVersion = setPrefs.getString(Const.SETTING.VERSION, "0");
		String versionName = "";
		PackageInfo info = null;

		try {
			info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			versionName = info.versionName;
		} catch (PackageManager.NameNotFoundException e) {

		}

		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "versionName = " + versionName + " , prefsSavedVersion= " + prefsSavedVersion);

		if(prefsSavedVersion.equals("0") || !prefsSavedVersion.equals(versionName)){
			InitializeSetting initializeSetting = new InitializeSetting(getApplication());
			initializeSetting.execute();
			SharedPreferences.Editor editor = setPrefs.edit();
			editor.putString(Const.SETTING.VERSION, versionName);
			editor.apply();
		}
	}

	protected boolean putAlarmPreference(String key, boolean value){
		SharedPreferences prefs = getApplicationContext().getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(key);
		editor.putBoolean(key, value);
		return editor.commit();
	}

	private boolean getAlarmPreference(String key){
		SharedPreferences prefs = getApplicationContext().getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
		return prefs.getBoolean(key, true);
	}

	private boolean toggleAlarmPreference(String key){
		return putAlarmPreference(key, !getAlarmPreference(key));
	}


	public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.isAlarmNoti:
				toggleAlarmPreference(Const.SETTING.IS_ALARM_NOTI);
				if(getAlarmPreference(Const.SETTING.IS_ALARM_NOTI))
					item.setChecked(true);
				else
					item.setChecked(false);
				return true;
			case R.id.isTTSNoti:
				toggleAlarmPreference(Const.SETTING.IS_TTS_NOTI);
				if(getAlarmPreference(Const.SETTING.IS_TTS_NOTI))
					item.setChecked(true);
				else
					item.setChecked(false);
				return true;
			case R.id.setting:
				this.onNavigationItemSelected(R.id.nav_item_setting);
        }
		return super.onOptionsItemSelected(item);
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

	public boolean onPrepareOptionsMenu(Menu menu) {
		if(getAlarmPreference(Const.SETTING.IS_ALARM_NOTI))
			menu.findItem(R.id.isAlarmNoti).setChecked(true);
		if(getAlarmPreference(Const.SETTING.IS_TTS_NOTI))
			menu.findItem(R.id.isTTSNoti).setChecked(true);
		return true;
	}

	@Override
    public void onFragmentInteraction(Uri uri) {

    }

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item){
		return this.onNavigationItemSelected(item.getItemId());
	}
	public boolean onNavigationItemSelected(int id){
		// update the main content by replacing fragments
		Fragment fragment = null;
		//int id = item.getItemId();
		switch (id) {
			case R.id.nav_item_alaram:
				fragment = new AlarmFragment();
				break;
			case R.id.nav_item_memo:
				fragment = new MemoFragment();
				break;
			case R.id.nav_item_cate:
				fragment = new CategoryFragment();
				break;
			case R.id.nav_item_setting:
				fragment = new SettingFragment();
				break;
			default:
                //fragment = new AlarmFragment();
				android.os.Process.killProcess(android.os.Process.myPid());
				break;
		}
		if (fragment != null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.replace(R.id.main_container, fragment).commit();
			// update selected item and title, then close the drawer

			mDrawer.closeDrawer(GravityCompat.START);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
		return true;
	}

	public void forceCrash(View view) {
		throw new RuntimeException("This is a crash");
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}
}
