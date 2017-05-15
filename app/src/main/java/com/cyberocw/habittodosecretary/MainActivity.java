package com.cyberocw.habittodosecretary;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.alaram.AlarmFragment;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.category.CategoryFragment;
import com.cyberocw.habittodosecretary.memo.MemoDataManager;
import com.cyberocw.habittodosecretary.memo.MemoFragment;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;
import com.cyberocw.habittodosecretary.settings.InitializeSetting;
import com.cyberocw.habittodosecretary.settings.SettingFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Calendar;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements AlarmFragment.OnFragmentInteractionListener, CategoryFragment.OnFragmentInteractionListener, MemoFragment.OnFragmentInteractionListener,
		SettingFragment.OnFragmentInteractionListener,
		NavigationView.OnNavigationItemSelectedListener {
    public AlarmFragment mMainFragment;
    public static String TAG = "mainActivity";

	private NavigationView mNavigationView;
	private DrawerLayout mDrawer;

	private ActionBar actionBar;
	private final long FINISH_INTERVAL_TIME = 2000;
	private long backPressedTime = 0;


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
		actionBar.setTitle(getResources().getString(R.string.nav_item_alaram));
	    mDrawer = (DrawerLayout)findViewById(R.id.dl_activity_main_drawer);
	    mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
	    mNavigationView.setNavigationItemSelectedListener(this);

		//MobileAds.initialize(this, "ca-app-pub-8072677228798230/9898207305"); // real
		MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713"); // test

		AdView adView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.build();
		adView.loadAd(adRequest);

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
					actionBar.setTitle(getResources().getString(R.string.nav_item_memo));
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
				actionBar.setTitle(getResources().getString(R.string.nav_item_alaram));
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

		//앱 최초 실행시 기초 데이터 생성
		if(prefsSavedVersion.equals("0")){
			AlarmDataManager alarmDataManager = new AlarmDataManager(this, Calendar.getInstance());
			AlarmVO vo = new AlarmVO();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, 4);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			vo.setAlarmTitle("4분뒤 알림");
			vo.setAlarmType(0);
			vo.setAlarmOption(1);
			vo.setHour(cal.get(Calendar.HOUR_OF_DAY));
			vo.setMinute(cal.get(Calendar.MINUTE));
			ArrayList<Integer> arrAlarmCall = new ArrayList<Integer>();
			arrAlarmCall.add(0);
			vo.setAlarmCallList(arrAlarmCall);
			//vo.setRepeatDay(mDataRepeatDay);
			vo.setAlarmDateType(Const.ALARM_DATE_TYPE.SET_DATE);
			ArrayList<Calendar> alarmDate = new ArrayList<Calendar>();
			alarmDate.add(cal);
			vo.setAlarmDateList(alarmDate);
			vo.setEtcType("");

			alarmDataManager.addItem(vo);

			MemoDataManager memoDataManager = new MemoDataManager(this);
			memoDataManager.makeDataList();
			MemoVO memoVO = new MemoVO();
			memoVO.setCategoryId(1);
			memoVO.setTitle("슈퍼에서 살것");
			memoVO.setContents("생수\n아이스크림\n두부\n참기름");
			memoVO.setRank(4);
			memoDataManager.addItem(memoVO);
		}

		if(prefsSavedVersion.equals("0") || !prefsSavedVersion.equals(versionName)){
			InitializeSetting initializeSetting = new InitializeSetting(this);
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
				actionBar.setTitle(getResources().getString(R.string.nav_item_alaram));
				break;
			case R.id.nav_item_memo:
				fragment = new MemoFragment();
				actionBar.setTitle(getResources().getString(R.string.nav_item_memo));
				break;
			case R.id.nav_item_cate:
				fragment = new CategoryFragment();
				((CategoryFragment) fragment).setActionBar(actionBar);
				actionBar.setTitle(getResources().getString(R.string.nav_item_cate));
				break;
			case R.id.nav_item_setting:
				fragment = new SettingFragment();
				actionBar.setTitle(getResources().getString(R.string.nav_item_setting));
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

	@Override
	public void onBackPressed() {
		long tempTime = System.currentTimeMillis();
		long intervalTime = tempTime - backPressedTime;

		int count = getSupportFragmentManager().getBackStackEntryCount();

		Log.d(this.toString(), "count="+count);

		if(count == 0)
			showFinishPopup();
		else
			super.onBackPressed();
	}

	public void showFinishPopup() {
		AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
		alert_confirm.setMessage("정말 종료하시겠습니까?").setCancelable(false).setPositiveButton("확인",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).setNegativeButton("취소",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 'No'
						dialog.dismiss();
					}
				});
		AlertDialog alert = alert_confirm.create();
		alert.show();
	}

}
