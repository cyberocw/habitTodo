package com.cyberocw.habittodosecretary.file;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.common.vo.FileVO;

import org.w3c.dom.Text;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by cyber on 2017-08-15.
 */

public class FileListAdapter extends BaseAdapter{
    Context mCtx;
    private int mFileType;
    private List<FileVO> mDataList;
    private LayoutInflater mInflater;
    private FileDataManager mManager;

    public FileListAdapter(Context ctx, FileDataManager dataManager, int fileType){
        mCtx = ctx;
        this.mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mManager = dataManager;
        mFileType = fileType;
    }
    @Override
    public int getCount() {
        Crashlytics.log(Log.DEBUG, this.toString(), "mManager.getCount="+mManager.getCount());
        return mManager.getCount();
    }

    @Override
    public Object getItem(int position) {
        return mManager.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return ((FileVO) getItem(position)).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FileVO vo = mManager.getItem(position);
        Crashlytics.log(Log.DEBUG, this.toString(), "vo="+vo.toString());
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.view_file, parent, false);
        }

        TextView tv = ButterKnife.findById(convertView, R.id.tvTitle);
        tv.setText(vo.getName());

        //ButterKnife.findById(convertView, R.id.ivImage).setVisibility(View.GONE);

        ImageView imageView = ButterKnife.findById(convertView, R.id.ivImage);
        imageView.setVisibility(View.VISIBLE);
        Uri thumbnailUri = vo.getUri();
        Glide.with(mCtx)
                .load(thumbnailUri)
                .centerCrop()
                .crossFade()
                .into(imageView);


        return convertView;
    }
}
