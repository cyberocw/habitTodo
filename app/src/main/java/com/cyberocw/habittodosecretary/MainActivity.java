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

	FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());

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
				/*

				FragmentManager fragmentManager = getSupportFragmentManager();

				Fragment alarmFragment = new AlarmFragment();

				alarmFragment.setArguments(intent.getExtras());
				actionBar.setTitle(getResources().getString(R.string.nav_item_alaram));
				fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
						.replace(R.id.main_container, alarmFragment).commit();
*/

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
					actionBar.setTitle(getResources().getString(R.string.nav_item_memo));
					if(mHelpMenu != null)
						mHelpMenu.setVisible(false);
				}else{
					Toast.makeText(getApplicationContext(), getString(R.string.main_activity_etctype_invalid), Toast.LENGTH_LONG).show();
					Log.e(this.toString(), "버그! etcType이 잘못 되었습니다 etcType="+ bundle.getString("etcType"));
					fragment = new AlarmFragment();
				}
			}else{
				fragment = new AlarmFragment();
			}
			fragment.setArguments(bundle);
		}else {
			if(CommonUtils.isLocaleKo(getResources().getConfiguration())) {
				fragment = new DashboardFragment();
				actionBar.setTitle(getResources().getString(R.string.nav_item_dashboard));
			}else{
				fragment = new AlarmFragment();
				actionBar.setTitle(getResources().getString(R.string.nav_item_alaram));
			}
		}

		fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.replace(R.id.main_container, fragment).commit();

		afterUpdateVersion();

		//showUpdateLog();
	}

	private void afterUpdateVersion(){
		Context ctx = getApplicationContext();
		SharedPreferences setPrefs = ctx.getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);

		String prefsSavedVersion = setPrefs.getString(Const.SETTING.VERSION, "0");
		//int isShowUpdateLog = setPrefs.getInt(Const.SETTING.IS_SHOW_UPDATE_LOG, 0);

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

		}
		if (prefsSavedVersion.equals("0") || !prefsSavedVersion.equals(versionName)) {
			if(CommonUtils.isLocaleKo(getResources().getConfiguration())) {
				InitializeSetting initializeSetting = new InitializeSetting(this);
				initializeSetting.execute();
			}
			if(!prefsSavedVersion.equals("0")){
				showUpdateLog();
			}
			File rootDir = new File(getApplicationContext().getFilesDir(), "voice");
			if(!rootDir.isDirectory())
				rootDir.mkdirs();

			SharedPreferences.Editor editor = setPrefs.edit();
			editor.putString(Const.SETTING.VERSION, versionName);
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
			msg = "1. 음성을 녹음하여 알림 소리로 사용하는 기능이 추가되었습니다. 좋아하는 사람의 음성을 들으면서 잠들거나 일어나면 더 좋겠지요?\n" +
					"연인 혹은 좋아하는 연예인의 음성을 녹음해보세요.\n" +
					"\n" +
					"2. 미리 알림(X분 전/후)에 대해 상태바 알림 혹은 끌때까지 알림으로 별도 알림 옵션을 설정하는 기능을 추가했습니다. 여러 알림 조합을 활용해 보세요.\n" +
					"\n" +
					"3. 미리 알림을 최대 23시간 59분까지 지정할 수 있게 변경했습니다.\n" +
					"\n" +
					"4. 타이머에서도 상태바 1회 알림 혹은 끌때까지 알람 지정 옵션과 TTS 재생 여부를 지정하는 옵션이 추가되었습니다.\n" +
					"\n" +
					"5. 끌때까지 알람의 경우, 사용자가 멈출때까지 소리가 반복 재생되도록 수정했습니다.";
		}
		else {
			msg = "1. Added the function to record voice and use it as notification sound. Would you like to sleep or wake up while listening to your favorite voice?\n" +
					"Record the voice of a lover or favorite entertainer.\n" +
					"\n" +
					"2. Added the ability to set separate notification options for pre-inform reminder(X minutes before / after), either status bar notifications or notifications until they are turned off.\n" +
					"Take advantage of multiple notification combinations.\n" +
					"\n" +
					"3. Changed pre-inform reminder to be up to 23 hours and 59 minutes.\n" +
					"\n" +
					"4. In the timer, there is also an option to specify whether to notify the status bar once or whether to turn off the alarm and specify whether to play the TTS.\n\n" +
					"5. In the case of an alarm until it is turned off, the sound is repeated until the user stops.";
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
			case R.id.nav_item_dashboard:
				fragment = new DashboardFragment();
				actionBar.setTitle(getResources().getString(R.string.nav_item_dashboard));
				if(mHelpMenu != null)
					mHelpMenu.setVisible(false);
				break;
			case R.id.nav_item_alaram:
				fragment = new AlarmFragment();
				actionBar.setTitle(getResources().getString(R.string.nav_item_alaram));
				//한글일때만 나옴
				if(mHelpMenu != null && CommonUtils.isLocaleKo(getResources().getConfiguration()))
					mHelpMenu.setVisible(true);
				break;
			case R.id.nav_item_memo:
				fragment = new MemoFragment();
				actionBar.setTitle(getResources().getString(R.string.nav_item_memo));
				if(mHelpMenu != null)
					mHelpMenu.setVisible(false);
				break;
			case R.id.nav_item_cate:
				fragment = new CategoryFragment();
				((CategoryFragment) fragment).setActionBar(actionBar);
				actionBar.setTitle(getResources().getString(R.string.nav_item_cate));
				if(mHelpMenu != null)
					mHelpMenu.setVisible(false);
				break;
			case R.id.nav_item_setting:
				fragment = new SettingFragment();
				actionBar.setTitle(getResources().getString(R.string.nav_item_setting));
				if(mHelpMenu != null)
					mHelpMenu.setVisible(false);
				break;
			case R.id.nav_item_keyword:
				fragment = new KeywordFragment();
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
