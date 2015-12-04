package com.cyberocw.habittodosecretary;

import java.util.Locale;

import android.net.Uri;
import android.support.design.widget.NavigationView;
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

public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener, MemoFragment.OnFragmentInteractionListener,
		NavigationView.OnNavigationItemSelectedListener {
    public MainFragment mMainFragment;
    public static String TAG = "mainActivity";

	private NavigationView mNavigationView;
	private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
        actionBar.setDisplayHomeAsUpEnabled(true);

	    mDrawer = (DrawerLayout)findViewById(R.id.dl_activity_main_drawer);
	    mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
	    mNavigationView.setNavigationItemSelectedListener(this);

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
				fragment = new MainFragment();
				break;
			case R.id.nav_item_memo:
				fragment = new MemoFragment();
				break;
			default:
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



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        MainFragment mMainFragment = new MainFragment();
        MemoFragment mMemoFragment = new MemoFragment();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment fragment = null;
            switch (position){
                case 0 : fragment = mMainFragment.newInstance(null, null);break;
                case 1 : fragment = mMemoFragment.newInstance("", "");break;
                case 2 : fragment = PlaceholderFragment.newInstance(position + 1);break;
                //default : fragment = PlaceholderFragment.newInstance(position + 1);
            }

            Log.i(TAG, "getItem position=" + position);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
