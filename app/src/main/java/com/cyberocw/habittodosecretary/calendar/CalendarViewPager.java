package com.cyberocw.habittodosecretary.calendar;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import me.kaelaela.verticalviewpager.VerticalViewPager;

/**
 * Created by cyber on 2017-12-14.
 */

public class CalendarViewPager extends ViewPager{
    public CalendarViewPager(Context context) {
        super(context);
    }

    public CalendarViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * @return {@code false} since a vertical view pager can never be scrolled horizontally
     */
    @Override
    public boolean canScrollHorizontally(int direction) {
        return false;
    }
    /**
     * @return {@code true} iff a normal view pager would support horizontal scrolling at this time
     */
    @Override
    public boolean canScrollVertically(int direction) {
        return super.canScrollHorizontally(direction);
    }
    public void init() {
        setPageTransformer(true, new DefaultTransformer());
        setOverScrollMode(View.OVER_SCROLL_NEVER);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d("calendar view pager", "intercept touchEvent");
        final boolean toIntercept = super.onInterceptTouchEvent(flipXY(ev));
        // Return MotionEvent to normal
        flipXY(ev);
        return toIntercept;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final boolean toHandle = super.onTouchEvent(flipXY(ev));
        // Return MotionEvent to normal
        flipXY(ev);
        return toHandle;
    }
    private MotionEvent flipXY(MotionEvent ev) {
        final float width = getWidth();
        final float height = getHeight();
        final float x = (ev.getY() / height) * width;
        final float y = (ev.getX() / width) * height;
        ev.setLocation(x, y);
        return ev;
    }
    private static final class DefaultTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(View view, float position) {
            float alpha = 0;
            if (0 <= position && position <= 1) {
                alpha = 1 - position;
            } else if (-1 < position && position < 0) {
                alpha = position + 1;
            }
            view.setAlpha(alpha);
            view.setTranslationX(view.getWidth() * -position);
            float yPosition = position * view.getHeight();
            view.setTranslationY(yPosition);
        }

    }
}
