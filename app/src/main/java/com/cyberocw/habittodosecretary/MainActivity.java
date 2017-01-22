package com.cyberocw.habittodosecretary;

import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.cyberocw.habittodosecretary.alaram.AlarmFragment;
import com.cyberocw.habittodosecretary.category.CategoryFragment;
import com.cyberocw.habittodosecretary.memo.MemoFragment;
import com.cyberocw.habittodosecretary.settings.ui.SettingFragment;

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
	    fragmentManager.beginTransaction()
			    .replace(R.id.main_container, new AlarmFragment()).commit();
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                return true;
        }
		return super.onOptionsItemSelected(item);
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

	@Override
    public void onFragmentInteraction(Uri uri) {

    }

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item){
		// update the main content by replacing fragments
		Fragment fragment = null;
		int id = item.getItemId();
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

			fragmentManager.beginTransaction()
					.replace(R.id.main_container, fragment).commit();
			// update selected item and title, then close the drawer

			mDrawer.closeDrawer(GravityCompat.START);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
		return true;
	}
}
