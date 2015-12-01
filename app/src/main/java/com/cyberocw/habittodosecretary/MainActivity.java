package com.cyberocw.habittodosecretary;

import java.util.Locale;

import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener, MemoFragment.OnFragmentInteractionListener {
    public MainFragment mMainFragment;
    public static String TAG = "mainActivity";
    private ListView lvNavList;
    private FrameLayout flContainer;
	private String[] navItems = {"Brown", "Cadet Blue", "Dark Olive Green",
			"Dark Orange", "Golden Rod"};
	private DrawerLayout dlDrawer;
	private ActionBarDrawerToggle dtToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        lvNavList = (ListView)findViewById(R.id.lv_activity_main_nav_list);
        flContainer = (FrameLayout)findViewById(R.id.fl_activity_main_container);

        lvNavList.setAdapter(
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, navItems));
        lvNavList.setOnItemClickListener(new DrawerItemClickListener());

	    dlDrawer = (DrawerLayout)findViewById(R.id.dl_activity_main_drawer);
	    dtToggle = new ActionBarDrawerToggle(this, dlDrawer,
			    R.drawable.ic_drawer, R.string.open_drawer, R.string.close_drawer){

		    @Override
		    public void onDrawerClosed(View drawerView) {
			    super.onDrawerClosed(drawerView);
		    }

		    @Override
		    public void onDrawerOpened(View drawerView) {
			    super.onDrawerOpened(drawerView);
		    }

	    };
	    dlDrawer.setDrawerListener(dtToggle);
	    getActionBar().setDisplayHomeAsUpEnabled(true);
    }

	protected void onPostCreate(Bundle savedInstanceState){
		super.onPostCreate(savedInstanceState);
		dtToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		dtToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(dtToggle.onOptionsItemSelected(item)){
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
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

	    @Override
	    public void onItemClick(AdapterView<?> adapter, View view, int position,
	                            long id) {
		    switch (position) {
			    case 0:
				    flContainer.setBackgroundColor(Color.parseColor("#A52A2A"));
				    break;
			    case 1:
				    flContainer.setBackgroundColor(Color.parseColor("#5F9EA0"));
				    break;
			    case 2:
				    flContainer.setBackgroundColor(Color.parseColor("#556B2F"));
				    break;
			    case 3:
				    flContainer.setBackgroundColor(Color.parseColor("#FF8C00"));
				    break;
			    case 4:
				    flContainer.setBackgroundColor(Color.parseColor("#DAA520"));
				    break;

		    }

	    }
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
