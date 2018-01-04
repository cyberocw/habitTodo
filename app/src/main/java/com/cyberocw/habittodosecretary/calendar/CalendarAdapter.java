package com.cyberocw.habittodosecretary.calendar;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by cyber on 2017-12-14.
 */

public class CalendarAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener{
    @SuppressWarnings("unused")
    private Context mContext;

    private MonthView[] monthViews;
    /** Default year to calculate the page position */
    final static int BASE_YEAR = 2018;
    /** Default month to calculate the page position */
    final static int BASE_MONTH = Calendar.JANUARY;
    /** Calendar instance based on default year and month */
    final Calendar BASE_CAL;
    /** Page numbers to reuse */
    final static int PAGES = 5;
    /** Inner virtual pages, I think it may be infinite scroll. */
    final static int TOTAL_PAGES = Integer.MAX_VALUE;
    /** position basis */
    final static int BASE_POSITION = TOTAL_PAGES / 2;
    /** previous position */
    private int previousPosition;
    CalendarManager mCalendarManager;
    private ViewPager mViewPager;

    private MonthView.OnClickDayListener onClickDayListener = null;
    private OnPageScrolledListener onPageScrolledListener = null;

    public CalendarAdapter(Context context, CalendarManager calendarManager) {
        this.mContext = context;
        Calendar base = Calendar.getInstance();
        base.set(BASE_YEAR, BASE_MONTH, 1);
        BASE_CAL = base;
        mCalendarManager = calendarManager;
        monthViews = new MonthView[PAGES];
        for(int i = 0; i < PAGES; i++) {
            monthViews[i] = new MonthView(context);
        }
    }
    public void resetMonthView(){
        //monthViews = new MonthView[PAGES];
        for(int i = 0; i < PAGES; i++) {
            Log.d(this.toString(), "pages create");
            monthViews[i].removeAllViewsInLayout();
            monthViews[i].setOnClickDayListener(null);
            monthViews[i] = new MonthView(mContext);
        }
    }

    public void setOnPageScrolledListener(OnPageScrolledListener listener){
        onPageScrolledListener = listener;
    }

    /**
     * Get the particular date by page position
     * @param position page position
     * @return YearMonth
     */
    public YearMonth getYearMonth(int position) {
        Calendar cal = (Calendar)BASE_CAL.clone();
        cal.add(Calendar.MONTH, position - BASE_POSITION);
        return new YearMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
    }

    /**
     * Get the page position by given date
     * @param year 4 digits number of year
     * @param month month number
     * @return page position
     */
    public int getPosition(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        return BASE_POSITION + howFarFromBase(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
    }

    /**
     * How many months exist from the base month to the given values?
     * @param year the year to compare with the base year
     * @param month the month to compare with the base month
     * @return counts of month
     */
    private int howFarFromBase(int year, int month) {

        int disY = (year - BASE_YEAR) * 12;
        int disM = month - BASE_MONTH;

        return disY + disM;
    }
    public void setOnClickDayListener(MonthView.OnClickDayListener listener){
        this.onClickDayListener = listener;
    }
/*
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
*/

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.d(this.toString(), "instantiateItem destroyItem start position="+position);

        int howFarFromBase = position - BASE_POSITION;
        Log.d(this.toString(), "howfarfrom ="+howFarFromBase);
        Calendar cal = (Calendar) BASE_CAL.clone();
        cal.add(Calendar.MONTH, howFarFromBase);
        //destroyItem(container, position, monthViews[position % PAGES]);
        //monthViews[position % PAGES].removeAllViewsInLayout();
        position = position % PAGES;
        //container.removeView();
        try {
            container.addView(monthViews[position]);
        }
        catch (Exception e){}

        monthViews[position].setCalendarManager(mCalendarManager);
        monthViews[position].make(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
        monthViews[position].setOnClickDayListener(onClickDayListener);
        Log.d(this.toString(), "re position="+position);
        return monthViews[position];
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d(this.toString(), "destroyItem position="+position);
        ((MonthView) object).setOnClickDayListener(null);
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return TOTAL_PAGES;
    }

    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }
    public void setViewPager(ViewPager viewPager){
        this.mViewPager = viewPager;
    }
    @Override
    public void onPageScrollStateChanged(int state) {
        Log.d(this.toString(), "onPageScrollStateChanged start");
        switch(state) {
            case ViewPager.SCROLL_STATE_IDLE:
                //HLog.d(TAG, CLASS, "SCROLL_STATE_IDLE");
                break;
            case ViewPager.SCROLL_STATE_DRAGGING:
                //HLog.d(TAG, CLASS, "SCROLL_STATE_DRAGGING");
                previousPosition = mViewPager.getCurrentItem();
                break;
            case ViewPager.SCROLL_STATE_SETTLING:
                //HLog.d(TAG, CLASS, "SCROLL_STATE_SETTLING");
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        //HLog.d(TAG, CLASS, position + "-  " + positionOffset);
        if(previousPosition != position) {
            previousPosition = position;

            YearMonth ym = getYearMonth(position);
            this.onPageScrolledListener.onChange(ym.year, ym.month);

            //Log.d(this.toString(), position + " onPageScrolled-  " + ym.year + "." + ym.month);
        }
    }

    public interface OnPageScrolledListener{
        void onChange(int year, int month);
    }

    @Override
    public void onPageSelected(int position) {
    }

    /**
     * Object to preserve year and month
     * @author Brownsoo
     *
     */
    public class YearMonth {
        int year;
        int month;

        YearMonth(int year, int month) {
            this.year = year;
            this.month = month;
        }
    }

}
