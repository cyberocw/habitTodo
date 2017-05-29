package com.cyberocw.habittodosecretary.intro;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.util.CommonUtils;

/**
 * Created by cyber on 2017-05-28.
 */

public class ViewPagerAdapter extends PagerAdapter {
    Context mCtx;
    BitmapFactory.Options options;
    Bitmap galImage;
    String mMode;
    private int[] galImages = null;

    ViewPagerAdapter(Intro ctx, String mode){
        mCtx = ctx;
        options = new BitmapFactory.Options();
        mMode = mode;
        if(mMode.equals("intro")) {
            if (CommonUtils.isLocaleKo(mCtx.getResources().getConfiguration())) {
                galImages = new int[] {
                        R.drawable.intro1,
                        R.drawable.intro2,
                        R.drawable.intro3,
                        R.drawable.intro4,
                        R.drawable.intro5,
                        R.drawable.intro6
                };
            } else {
                galImages = new int[] {
                        R.drawable.eng_intro1,
                        R.drawable.eng_intro2,
                        R.drawable.eng_intro3,
                        R.drawable.eng_intro4,
                        R.drawable.eng_intro5,
                        R.drawable.eng_intro6
                };
            }
        }
        else if(mMode.equals("alarmList")) {
            galImages = new int[]{
                    R.drawable.help_alarm_list
            };
        }
        else if(mMode.equals("alarmPopup")) {
            galImages = new int[]{
                    R.drawable.help_alarm_popup
            };
        }
    }
    @Override
    public int getCount() {
        return galImages.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ImageView) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // TODO Auto-generated method stub
        ImageView imageView = new ImageView(mCtx);

        //int padding = mCtx.getResources().getDimensionPixelSize(R.dimen.padding_medium);
        //imageView.setPadding(padding, padding, padding, padding);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        options.inSampleSize = 2;

        galImage = BitmapFactory.decodeResource(mCtx.getResources(), galImages[position], options);

        imageView.setImageBitmap(galImage);
        //imageView.setImageResource(galImages[position]);

        ((ViewPager) container).addView(imageView, 0);
        return imageView;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((ImageView) object);
    }
}
