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

import com.cyberocw.habittodosecretary.alaram.AlarmFragment;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.category.CategoryFragment;
import com.cyberocw.habittodosecretary.common.vo.RelationVO;
import com.cyberocw.habittodosecretary.memo.MemoFragment;
import com.cyberocw.habittodosecretary.memo.ui.MemoDialogNew;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;

public class ShareActivity extends AppCompatActivity{
	public AlarmFragment mMainFragment;
	public static String TAG = "mainActivity";

	private NavigationView mNavigationView;
	private DrawerLayout mDrawer;

	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		MemoDialogNew dialogNew = new MemoDialogNew();
		Bundle bundle = new Bundle();

		MemoVO memoVO = new MemoVO();

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if ("text/plain".equals(type)) {
			memoVO.setContents(intent.getStringExtra(Intent.EXTRA_TEXT));    // 가져온 인텐트의 텍스트 정보
			memoVO.setTitle(intent.getStringExtra(Intent.EXTRA_TITLE));
		}

		bundle.putSerializable(Const.MEMO_VO, memoVO);
		bundle.putSerializable(Const.CATEGORY.CATEGORY_ID, 1);

		dialogNew.setArguments(bundle);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(R.id.main_container, dialogNew);
		ft.addToBackStack(null).commit();
		//dialogNew.setTargetFragment(this, Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_CODE);
	}

}
