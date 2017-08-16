package com.cyberocw.habittodosecretary.common.vo;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by cyber on 2017-08-15.
 */

public class FileVO implements Serializable{
    private static final long serialVersionUID = 1242L;
    private Long id, fId;
    private String uriPath;
    private String name, type;
    private long size;
    private long length;
    private String mimeType;
    private long createDt;
    private Uri uri;

    public FileVO(Long id, String uri, String name, long size, long length, String mimeType) {
        this.id = id;
        this.uriPath = uri;
        this.name = name;
        this.size = size;
        this.length = length;
        this.setMimeType(mimeType);
    }

    public FileVO(Uri uri, String mimeTypeInternal) {
        this(-1L, uri.getPath(), null, 0, 0, mimeTypeInternal);
        setUri(uri);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUriPath() {
        return uriPath;
    }

    public void setUriPath(String uriPath) {
        this.uriPath = uriPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getfId() {
        return fId;
    }

    public void setfId(Long fId) {
        this.fId = fId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getCreateDt() {
        return createDt;
    }

    public void setCreateDt(long createDt) {
        this.createDt = createDt;
    }

    @Override
    public String toString() {
        return "FileVO{" +
                "id=" + id +
                ", fId=" + fId +
                ", uriPath='" + uriPath + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", length=" + length +
                ", mimeType='" + mimeType + '\'' +
                ", uri=" + uri +
                '}';
    }
}
