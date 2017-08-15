package com.cyberocw.habittodosecretary.common.vo;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by cyber on 2017-08-15.
 */

public class FileVO implements Serializable{
    private static final long serialVersionUID = 1242L;
    private Long id;
    private String uriPath;
    private String name;
    private long size;
    private long length;
    private String mime_type;
    private Uri uri;

    public FileVO(Long id, String uri, String name, long size, long length, String mime_type) {
        this.id = id;
        this.uriPath = uri;
        this.name = name;
        this.size = size;
        this.length = length;
        this.setMime_type(mime_type);
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

    public String getMime_type() {
        return mime_type;
    }

    public void setMime_type(String mime_type) {
        this.mime_type = mime_type;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "FileVO{" +
                "id=" + id +
                ", uriPath='" + uriPath + '\'' +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", length=" + length +
                ", mime_type='" + mime_type + '\'' +
                ", uri=" + uri +
                '}';
    }
}
