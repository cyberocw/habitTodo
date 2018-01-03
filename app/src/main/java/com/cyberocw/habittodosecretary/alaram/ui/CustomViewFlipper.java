package com.cyberocw.habittodosecretary.alaram.ui;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.AlarmFragment;

/**
 * Created by cyber on 2017-12-14.
 */

public class CustomViewFlipper extends ViewFlipper {
    private static final int OFF_SET = 30;
    private float downX, upX, downY, upY;
    private Context mCtx;
    private AlarmFragment alarmFragment = null;
    public CustomViewFlipper(Context context) {
        super(context);
        mCtx = context;
    }

    public CustomViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action){
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                float y = ev.getY();
                //Log.d("customview flipper", "movemove");
                if(Math.abs(downX - x) > OFF_SET || Math.abs(downY - y) > OFF_SET) {
                    if(Math.abs(downY - y) > OFF_SET){
                        //Log.d("view flipper", "down YYY return false");
                        return false;
                    }
                    if(Math.abs(downX - x) > OFF_SET){
                        //Log.d("view flipper", "down XXX return true");
                        return true;
                    }else{
                        //Log.d("view flipper", "else return false");
                        return false;
                    }
                }


        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // 터치 시작지점 x좌표 저장
            Log.d(this.toString(), "get X = " + event.getX());

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            // 터치 끝난 지점 X좌표 저장
            upX = event.getX();

            Log.d(this.toString(), " upX = "  +upX + " down_x = " + downX);

//					if(OFF_SET > Math.abs(down_x - upX))
//						return false;

            if (upX < downX) {
                // 터치 할때 왼쪽방향으로 진행
                this.setInAnimation(AnimationUtils.loadAnimation(mCtx,
                        R.anim.apper_from_right));

                this.setOutAnimation(AnimationUtils.loadAnimation(mCtx,
                        R.anim.disapper_from_left));

                this.showNext();
                alarmFragment.onDateChange(null);
                Log.d(this.toString(), "show next");
            } else if (upX > downX) {
                // 터치할때 오른쪽 방향으로 진행
                this.setInAnimation(AnimationUtils.loadAnimation(mCtx,
                        R.anim.appear_from_left));
                this.setOutAnimation(AnimationUtils.loadAnimation(mCtx,
                        R.anim.disapper_from_right));
                this.showPrevious();
                alarmFragment.onDateChange(null);
                Log.d(this.toString(), "show prev");
            }
        }
        return true;
    }

    public void setAlarmFragment(AlarmFragment alarmFragment) {
        this.alarmFragment = alarmFragment;
    }
}
