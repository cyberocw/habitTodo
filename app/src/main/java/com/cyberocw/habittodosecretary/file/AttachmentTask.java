/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cyberocw.habittodosecretary.file;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.text.TextUtils;


import com.cyberocw.habittodosecretary.common.vo.FileVO;

import java.lang.ref.WeakReference;

public class AttachmentTask extends AsyncTask<Void, Void, FileVO> {

    private final WeakReference<Fragment> mFragmentWeakReference;
    private OnAttachingFileListener mOnAttachingFileListener;
    private Uri uri;
    private String fileName;
    private Context mCtx;


    public AttachmentTask(Context ctx, Fragment mFragment, Uri uri, OnAttachingFileListener mOnAttachingFileListener) {
        this(ctx, mFragment, uri, null, mOnAttachingFileListener);
    }


    public AttachmentTask(Context ctx, Fragment mFragment, Uri uri, String fileName,
                          OnAttachingFileListener mOnAttachingFileListener) {
        mCtx = ctx;
        mFragmentWeakReference = new WeakReference<>(mFragment);
        this.uri = uri;
        this.fileName = TextUtils.isEmpty(fileName) ? "" : fileName;
        this.mOnAttachingFileListener = mOnAttachingFileListener;
    }


    @Override
    protected FileVO doInBackground(Void... params) {
        return StorageHelper.createAttachmentFromUri(mCtx, uri);
    }


    @Override
    protected void onPostExecute(FileVO mAttachment) {
        if (isAlive()) {
            if (mAttachment != null) {
                mOnAttachingFileListener.onAttachingFileFinished(mAttachment);
            } else {
                mOnAttachingFileListener.onAttachingFileErrorOccurred(null);
            }
        } else {
            if (mAttachment != null) {
                StorageHelper.delete(mCtx, mAttachment.getUri().getPath());
            }
        }
    }


    private boolean isAlive() {
        return mFragmentWeakReference != null
                && mFragmentWeakReference.get() != null
                && mFragmentWeakReference.get().isAdded()
                && mFragmentWeakReference.get().getActivity() != null
                && !mFragmentWeakReference.get().getActivity().isFinishing();
    }

}