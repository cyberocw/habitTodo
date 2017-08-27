package com.cyberocw.habittodosecretary;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.cyberocw.habittodosecretary.category.CategoryDataManager;
import com.cyberocw.habittodosecretary.category.CategoryFragment;
import com.cyberocw.habittodosecretary.category.vo.CategoryVO;
import com.cyberocw.habittodosecretary.dashboard.DashboardFragment;
import com.cyberocw.habittodosecretary.file.FileDataManager;
import com.cyberocw.habittodosecretary.intro.Intro;
import com.cyberocw.habittodosecretary.keyword.KeywordFragment;
import com.cyberocw.habittodosecretary.memo.MemoDataManager;
import com.cyberocw.habittodosecretary.memo.MemoFragment;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;
import com.cyberocw.habittodosecretary.settings.InitializeSetting;
import com.cyberocw.habittodosecretary.settings.SettingFragment;
import com.cyberocw.habittodosecretary.util.CommonUtils;
import com.cyberocw.habittodosecretary.util.TTSNoti;
import com.cyberocw.habittodosecretary.util.TTSNotiActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
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
	private MenuItem mHelpMenu = null;

	private AlarmDataManager mAlarmDataManager;

	FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Crashlytics cr = new Crashlytics();
		Fabric.with(this, cr);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

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

		MobileAds.initialize(this, "ca-app-pub-8072677228798230~8421474102"); // real
		if(!CommonUtils.isLocaleKo(getResources().getConfiguration())) {
            MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.nav_item_keyword);
            if(menuItem != null)
                menuItem.setVisible(false);
        }
		AdView adView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("048A3A6B542D3DD340272D8C1D80AC18")
				.build();
		if(Const.IS_DEBUG){
			adView.setVisibility(View.GONE);
			//+dimension margin 0 주기
		}else{
			adView.setVisibility(View.VISIBLE);
		}
		adView.loadAd(adRequest);

		initMainActivity(getIntent());

		CommonUtils.logCustomEvent("MainActivity", "1");
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if(intent != null) {
			Bundle bundle = intent.getExtras();

			if (bundle != null) {

				NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
				int reqCode = bundle.getInt(Const.PARAM.REQ_CODE);
				Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "reqCode=" + reqCode);
				manager.cancel(reqCode);
				initMainActivity(intent);

				//afterUpdateVersion();
			} else {
				Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, " on resume bundle is null");
			}
			setIntent(intent);
		}
	}

	public void initMainActivity(Intent intent){
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment;
		String tag;
		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "ocw vintent = " + intent + " get extras = " + intent.getExtras());
		//시작하는 경우는 최초 실행, 메모보기, 알림 연장 의 경우로 아 래 로직을 탐
		if(intent != null && intent.getExtras() != null) {
			Bundle bundle = intent.getExtras();
			if (bundle.containsKey(Const.PARAM.REQ_CODE)) {
				NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
				int reqCode = bundle.getInt(Const.PARAM.REQ_CODE);
				Crashlytics.log(Log.DEBUG, this.toString(), "cancel reqCode=" + reqCode);
				manager.cancel(reqCode);
			}

			Crashlytics.log(Log.DEBUG, this.toString(), "tectype key ocw = " + bundle.getString(Const.PARAM.ETC_TYPE_KEY));

			if(!bundle.getString(Const.PARAM.ETC_TYPE_KEY, "").equals("")){
				Crashlytics.log(Log.DEBUG, this.toString(), "bundle.getString(Const.PARAM.ETC_TYPE_KEY)="+bundle.getString(Const.PARAM.ETC_TYPE_KEY));
				if(bundle.getString(Const.PARAM.ETC_TYPE_KEY).equals(Const.ETC_TYPE.MEMO)){
					fragment = new MemoFragment();
					tag = Const.FRAGMENT_TAG.MEMO;
					actionBar.setTitle(getResources().getString(R.string.nav_item_memo));
					if(mHelpMenu != null)
						mHelpMenu.setVisible(false);
				}else{
					Toast.makeText(getApplicationContext(), getString(R.string.main_activity_etctype_invalid), Toast.LENGTH_LONG).show();
					Log.e(this.toString(), "버그! etcType이 잘못 되었습니다 etcType="+ bundle.getString("etcType"));
					fragment = new AlarmFragment();
					tag = Const.FRAGMENT_TAG.ALARM;
				}
			}else{
				fragment = new AlarmFragment();
				tag = Const.FRAGMENT_TAG.ALARM;
			}
			fragment.setArguments(bundle);
		}else {
			if(CommonUtils.isLocaleKo(getResources().getConfiguration())) {
				fragment = new DashboardFragment();
				tag = Const.FRAGMENT_TAG.DASHBOARD;
				actionBar.setTitle(getResources().getString(R.string.nav_item_dashboard));
			}else{
				fragment = new AlarmFragment();
				tag = Const.FRAGMENT_TAG.ALARM;
				actionBar.setTitle(getResources().getString(R.string.nav_item_alaram));
			}
		}

		fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.replace(R.id.main_container, fragment, tag).commit();

		afterUpdateVersion();

		//showUpdateLog();
	}

	private void afterUpdateVersion(){
		Context ctx = getApplicationContext();
		SharedPreferences setPrefs = ctx.getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);

		String prefsSavedVersion = setPrefs.getString(Const.SETTING.VERSION, "0");
		int prefsSavedVersionCode = setPrefs.getInt(Const.SETTING.VERSION_CODE, 0);
		//int isShowUpdateLog = setPrefs.getInt(Const.SETTING.IS_SHOW_UPDATE_LOG, 0);

		String versionName = "";
		PackageInfo info = null;
		int versionCode = 0;
		try {
			info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			versionName = info.versionName;
			versionCode = info.versionCode;
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
			vo.setAlarmTitle(getString(R.string.ex_alarm_title));
			vo.setAlarmType(0);
			vo.setUseYn(0);
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
			memoVO.setTitle(getString(R.string.ex_memo_title));
			memoVO.setContents(getString(R.string.ex_memo_cont));
			memoVO.setRank(4);
			memoDataManager.addItem(memoVO);


			CategoryVO cateVO = new CategoryVO();
			CategoryDataManager cdm = new CategoryDataManager(ctx);
			cateVO.setTitle(getString(R.string.uncategorized));
			cateVO.setSortOrder(0);
			cateVO.setType(Const.CATEGORY.TYPE);
			cateVO.setUseYn('Y');
			cdm.addItem(cateVO);

			putAlarmPreference(Const.SETTING.IS_DISTURB_MODE, false);

			Intent i = new Intent(this, Intro.class);
			i.putExtra(Const.PARAM.MODE, "intro");
			i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			startActivity(i);

			/*Intent i = new Intent(this, Intro.class);
			i.putExtra(Const.PARAM.MODE, "intro");
			i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			startActivity(i);*/

			Intent ttsIntent = new Intent(getApplicationContext(), TTSNotiActivity.class);
			ttsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			ttsIntent.putExtra("alaramTitle", "TTS Test");
			ttsIntent.putExtra("alarmId", -1L);
			startActivity(ttsIntent);

			InitializeSetting initializeSetting = new InitializeSetting(this);
			initializeSetting.execute();

		}
		//최초 || 업그레이드 시
		if (prefsSavedVersion.equals("0") || !prefsSavedVersion.equals(versionName)) {
			File rootDir = new File(getApplicationContext().getFilesDir(), Environment.DIRECTORY_RINGTONES);
			if(!rootDir.isDirectory())
				rootDir.mkdirs();

			if(CommonUtils.isLocaleKo(getResources().getConfiguration())) {

			}
			//업그레이드시
			if(!prefsSavedVersion.equals("0")){
				//showUpdateLog();
				if(prefsSavedVersionCode < 22) {
					putAlarmPreference(Const.SETTING.IS_DISTURB_MODE, false);
					FileDataManager fdm = new FileDataManager(getApplicationContext());
					try{fdm.migrationFile(getApplicationContext());}catch(Exception e){
						Crashlytics.log(Log.ERROR, this.toString(), "migration failed =" + e.getMessage());
					}
				}
			}

			SharedPreferences.Editor editor = setPrefs.edit();
			editor.putString(Const.SETTING.VERSION, versionName);
			editor.putInt(Const.SETTING.VERSION_CODE, versionCode);
			editor.apply();
		}

	}

	private void showUpdateLog(){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.update_log));
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();     //닫기
			}
		});
		String msg;
		if(CommonUtils.isLocaleKo(getResources().getConfiguration())){
			msg = "1. 특정 상황에서 앱이 강제종료되는 현상에 대한 안정화 \n\n" + "2. 작은 화면 혹은 큰 글씨를 사용하는 경우에 대한 최적화 작업";
		}
		else {
			msg = "1. Added to-do list function for memo menu.\n\n" +
					"2. TTS playback function bug fix" ;
		}
		alert.setMessage(msg);
		alert.show();
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
	private boolean getAlarmPreference(String key, boolean defaultVal){
		SharedPreferences prefs = getApplicationContext().getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
		return prefs.getBoolean(key, defaultVal);
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
			case R.id.actionHelp:
				Intent i = new Intent(this, Intro.class);
				i.putExtra(Const.PARAM.MODE, "alarmList");
				i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				startActivity(i);
				return true;
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
			case R.id.isDisturbMode :
				toggleAlarmPreference(Const.SETTING.IS_DISTURB_MODE);
				if(mAlarmDataManager == null)
					mAlarmDataManager = new AlarmDataManager(getApplicationContext());
				if(getAlarmPreference(Const.SETTING.IS_DISTURB_MODE)) {
					item.setChecked(true);
					mAlarmDataManager.stopAllAlarm();
				}
				else {
					item.setChecked(false);
					mAlarmDataManager.resetMinAlarm();
				}
				return true;
			case R.id.setting:
				this.onNavigationItemSelected(R.id.nav_item_setting);
        }
		return super.onOptionsItemSelected(item);
	}

	public FirebaseAnalytics getFireBase(){
		return mFirebaseAnalytics;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
		mHelpMenu = menu.getItem(0);
		if(mHelpMenu != null && !CommonUtils.isLocaleKo(getResources().getConfiguration())) {
			mHelpMenu.setVisible(false);
		}
        return true;
    }

	public boolean onPrepareOptionsMenu(Menu menu) {
		if(getAlarmPreference(Const.SETTING.IS_ALARM_NOTI))
			menu.findItem(R.id.isAlarmNoti).setChecked(true);
		if(getAlarmPreference(Const.SETTING.IS_TTS_NOTI))
			menu.findItem(R.id.isTTSNoti).setChecked(true);
		if(getAlarmPreference(Const.SETTING.IS_DISTURB_MODE, false))
			menu.findItem(R.id.isDisturbMode).setChecked(true);
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
		String tag = "";
		//int id = item.getItemId();
		switch (id) {
			case R.id.nav_item_dashboard:
				fragment = new DashboardFragment();
				tag = Const.FRAGMENT_TAG.DASHBOARD;
				actionBar.setTitle(getResources().getString(R.string.nav_item_dashboard));
				if(mHelpMenu != null)
					mHelpMenu.setVisible(false);
				break;
			case R.id.nav_item_alaram:
				fragment = new AlarmFragment();
				tag = Const.FRAGMENT_TAG.ALARM;
				actionBar.setTitle(getResources().getString(R.string.nav_item_alaram));
				//한글일때만 나옴
				if(mHelpMenu != null && CommonUtils.isLocaleKo(getResources().getConfiguration()))
					mHelpMenu.setVisible(true);
				break;
			case R.id.nav_item_memo:
				fragment = new MemoFragment();
				tag = Const.FRAGMENT_TAG.MEMO;
				actionBar.setTitle(getResources().getString(R.string.nav_item_memo));
				if(mHelpMenu != null)
					mHelpMenu.setVisible(false);
				break;
			case R.id.nav_item_cate:
				fragment = new CategoryFragment();
				tag = Const.FRAGMENT_TAG.CATEGORY;
				((CategoryFragment) fragment).setActionBar(actionBar);
				actionBar.setTitle(getResources().getString(R.string.nav_item_cate));
				if(mHelpMenu != null)
					mHelpMenu.setVisible(false);
				break;
			case R.id.nav_item_setting:
				fragment = new SettingFragment();
				tag = Const.FRAGMENT_TAG.SETTING;
				actionBar.setTitle(getResources().getString(R.string.nav_item_setting));
				if(mHelpMenu != null)
					mHelpMenu.setVisible(false);
				break;
			case R.id.nav_item_keyword:
				fragment = new KeywordFragment();
				tag = Const.FRAGMENT_TAG.KEYWORD;
				actionBar.setTitle(getResources().getString(R.string.nav_item_keyword));
				if(mHelpMenu != null)
					mHelpMenu.setVisible(false);
				break;
			default:
				//fragment = new AlarmFragment();
				//android.os.Process.killProcess(android.os.Process.myPid());
				break;
		}
		if (fragment != null) {

			int top = getSupportFragmentManager().getFragments().size()-1;
			while (top > 0 && getSupportFragmentManager().getFragments().get(top) == null) {
				top--;
			}
			getSupportFragmentManager().beginTransaction()
					.remove(getSupportFragmentManager().getFragments().get(top)).commit();

			getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.replace(R.id.main_container, fragment, tag).commit();
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
		alert_confirm.setMessage(getString(R.string.confirm_msg_quit)).setCancelable(false).setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).setNegativeButton(getString(R.string.cancel),
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
