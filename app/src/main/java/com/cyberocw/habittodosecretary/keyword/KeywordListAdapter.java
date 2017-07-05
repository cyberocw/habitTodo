package com.cyberocw.habittodosecretary.keyword;

/**
 * Created by cyber on 2017-07-06.
 */
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.category.CategoryDataManager;
import com.cyberocw.habittodosecretary.category.CategoryFragment;
import com.cyberocw.habittodosecretary.category.vo.CategoryVO;
import com.cyberocw.habittodosecretary.keyword.vo.KeywordVO;

public class KeywordListAdapter extends BaseAdapter {
    private KeywordDataManager mManager;
    private LayoutInflater inflater;
    private Context mCtx;
    private CategoryFragment mCategoryFragment;

    public KeywordListAdapter(Context ctx, KeywordDataManager mManager) {
        this.mManager = mManager;
        mCtx = ctx;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mManager.getCount();
    }

    @Override
    public Object getItem(int position) {
        return mManager.getItem(position).getKeyword();
    }

    @Override
    public long getItemId(int position) {
        return mManager.getItem(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final KeywordVO vo = mManager.getItem(position);

        if(convertView == null){
            convertView = inflater.inflate(R.layout.keyword_view, parent, false);
        }
        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvKeywordTitle);

        tvTitle.setText(vo.getRank() + " : " + vo.getKeyword() + " : " + vo.getSimpleDate());

        if(position == 0){
            //btnOption.setVisibility(View.GONE);
        }

        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?query=" + vo.getKeyword()));
                mCtx.getApplicationContext().startActivity(intent);

               /* CategoryVO vo = mManager.getItem(position);
                mCategoryFragment.longClickPopup(0, mManager.getItem(position).getId());*/
                //mManager.deleteItemById(vo.getId());

            }
        });
		/*convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCategoryFragment.showMemoList(mManager.getItem(position).getId());
			}
		});*/
        return convertView;
    }
}
