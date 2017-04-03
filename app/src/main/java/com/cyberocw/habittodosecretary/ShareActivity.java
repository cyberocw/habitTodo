 package com.cyberocw.habittodosecretary;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.alaram.AlarmFragment;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.category.CategoryFragment;
import com.cyberocw.habittodosecretary.common.vo.RelationVO;
import com.cyberocw.habittodosecretary.memo.MemoFragment;
import com.cyberocw.habittodosecretary.memo.ui.MemoDialogNew;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;

import io.fabric.sdk.android.Fabric;

 public class ShareActivity extends AppCompatActivity implements AlarmFragment.OnFragmentInteractionListener, CategoryFragment.OnFragmentInteractionListener, MemoFragment.OnFragmentInteractionListener{
	public AlarmFragment mMainFragment;
	public static String TAG = "mainActivity";

	private NavigationView mNavigationView;
	private DrawerLayout mDrawer;
	private Fragment mFragment;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());
		setContentView(R.layout.activity_main);

		//MemoDialogNew dialogNew = new MemoDialogNew();
		Bundle bundle = new Bundle();

		MemoVO memoVO = new MemoVO();

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if ("text/plain".equals(type)) {
			memoVO.setContents(intent.getStringExtra(Intent.EXTRA_TEXT));    // 가져온 인텐트의 텍스트 정보
			//memoVO.setTitle(intent.getStringExtra(Intent.EXTRA_TITLE));
			memoVO.setTitle(intent.getStringExtra(Intent.EXTRA_SUBJECT));
		}
		mFragment = new MemoFragment();

		bundle.putSerializable(Const.MEMO_VO, memoVO);
		bundle.putSerializable(Const.MEMO.MEMO_INTERFACE_CODE.SHARE_MEMO_MODE, true);

		mFragment.setArguments(bundle);


		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(R.id.main_container, mFragment).commit();
		//ft.addToBackStack(null).commit();
		//fragment.setTargetFragment(this, Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_CODE);
	}

	public void finishActivity4(){
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().remove(mFragment);
		finish();
	}

	@Override
	public void onFragmentInteraction(Uri uri) {

	}
}
