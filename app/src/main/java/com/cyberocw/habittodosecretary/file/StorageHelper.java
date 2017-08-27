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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.common.vo.FileVO;
import com.cyberocw.habittodosecretary.util.Constants;
import com.cyberocw.habittodosecretary.file.FileHelper;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StorageHelper {

    public static boolean checkStorage() {
        boolean mExternalStorageAvailable;
        boolean mExternalStorageWriteable;
        String state = Environment.getExternalStorageState();

        switch (state) {
            case Environment.MEDIA_MOUNTED:
                // We can read and write the media
                mExternalStorageAvailable = mExternalStorageWriteable = true;
                break;
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                // We can only read the media
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
                break;
            default:
                // Something else is wrong. It may be one of many other states, but
                // all we need
                // to know is we can neither read nor write
                mExternalStorageAvailable = mExternalStorageWriteable = false;
                break;
        }
        return mExternalStorageAvailable && mExternalStorageWriteable;
    }


    public static String getStorageDir() {
        // return Environment.getExternalStorageDirectory() + File.separator +
        // Const.ERROR_TAG + File.separator;
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    }


    public static File getAttachmentDir(Context mContext) {
        return mContext.getExternalFilesDir(null);
    }
    public static File getAttachmentDir(Context mContext, String dir) {
        return mContext.getExternalFilesDir(dir);
    }

    /**
     * Retrieves the folderwhere to store data to sync notes
     *
     * @param mContext
     * @return
     */
    public static File getDbSyncDir(Context mContext) {
        File extFilesDir = mContext.getExternalFilesDir(null);
        File dbSyncDir = new File(extFilesDir, Constants.APP_STORAGE_DIRECTORY_SB_SYNC);
        dbSyncDir.mkdirs();
        if (dbSyncDir.exists() && dbSyncDir.isDirectory()) {
            return dbSyncDir;
        } else {
            return null;
        }
    }


    /**
     * Create a path where we will place our private file on external
     */
    public static File createExternalStoragePrivateFile(Context mContext, Uri uri, String extension) {

        // Checks for external storage availability
        if (!checkStorage()) {
            Toast.makeText(mContext, "storage_not_available", Toast.LENGTH_SHORT).show();
            return null;
        }
        //날짜로 파일 이름 만들어옴 getExternalFilesDir 경로로
        File file = createNewAttachmentFile(mContext, extension);

        InputStream is = null;
        OutputStream os = null;
        try {
            is = mContext.getContentResolver().openInputStream(uri);
            os = new FileOutputStream(file);
            copyFile(is, os);
        } catch (IOException e) {
            try {
                is = new FileInputStream(FileHelper.getPath(mContext, uri));
                os = new FileOutputStream(file);
                copyFile(is, os);
                // It's a path!!
            } catch (NullPointerException e1) {
                try {
                    is = new FileInputStream(uri.getPath());
                    os = new FileOutputStream(file);
                    copyFile(is, os);
                } catch (FileNotFoundException e2) {
                    Log.e(Const.ERROR_TAG, "Error writing " + file, e2);
                    file = null;
                }
            } catch (FileNotFoundException e2) {
                Log.e(Const.ERROR_TAG, "Error writing " + file, e2);
                file = null;
            }
        } finally {
			try {
				if (is != null) {
					is.close();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				Log.e(Const.ERROR_TAG, "Error closing streams", e);
			}

		}
		return file;
    }

    public static FileVO createAttachmentFromUri(Context mContext, Uri uri) {
        return createAttachmentFromUri(mContext, uri, false);
    }

    /**
     * Creates a fiile to be used as attachment.
     */
    public static FileVO createAttachmentFromUri(Context mContext, Uri uri, boolean moveSource) {
        String name = FileHelper.getNameFromUri(mContext, uri);
        String extension = FileHelper.getFileExtension(name).toLowerCase(
                Locale.getDefault());
        File f;
        if (moveSource) {
            f = createNewAttachmentFile(mContext, extension);
            try {
                FileUtils.moveFile(new File(uri.getPath()), f);
            } catch (IOException e) {
                //Log.e(Constants.TAG, "Can't move file " + uri.getPath());
            }
        }
        else {
            //getExternalFilesDir 경로로 날짜 파일 생성 복사
            f = StorageHelper.createExternalStoragePrivateFile(mContext, uri, extension);
        }
        FileVO mAttachment = null;
        if (f != null) {
            mAttachment = new FileVO(Uri.fromFile(f), StorageHelper.getMimeTypeInternal(mContext, uri));
            mAttachment.setName(name);
            mAttachment.setSize(f.length());
        }
        return mAttachment;
    }

    public static boolean copyFile(File source, File destination) {
		FileInputStream is = null;
		FileOutputStream os = null;
        try {
			is = new FileInputStream(source);
			os = new FileOutputStream(destination);
            return copyFile(is, os);
        } catch (FileNotFoundException e) {
            Log.e(Const.ERROR_TAG, "Error copying file", e);
            return false;
        } finally {
			try {
				if (is != null) {
					is.close();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				Log.e(Const.ERROR_TAG, "Error closing streams", e);
			}
		}
	}


    /**
     * Generic file copy method
     *
     * @param is Input
     * @param os Output
     * @return True if copy is done, false otherwise
     */
    public static boolean copyFile(InputStream is, OutputStream os) {
        boolean res = false;
        byte[] data = new byte[1024];
        int len;
        try {
            while ((len = is.read(data)) > 0) {
                os.write(data, 0, len);
            }
            is.close();
            os.close();
            res = true;
        } catch (IOException e) {
            Log.e(Const.ERROR_TAG, "Error copying file", e);
        }
        return res;
    }


    public static boolean deleteExternalStoragePrivateFile(Context mContext, String name) {
        boolean res = false;

        // Checks for external storage availability
        if (!checkStorage()) {
            //Toast.makeText(mContext, mContext.getString(R.string.storage_not_available), Toast.LENGTH_SHORT).show();
            return false;
        }

        File file = new File(mContext.getExternalFilesDir(null), name);
        if(file.isFile())
            file.delete();
        else{
            //Toast.makeText(mContext, "Not Found file 1", Toast.LENGTH_SHORT).show();
        }

        return true;
    }
    public static boolean deleteExternalStoragePrivateFile(Context mContext, String name, String dir) {

        // Checks for external storage availability
        if (!checkStorage()) {
            //Toast.makeText(mContext, mContext.getString(R.string.storage_not_available), Toast.LENGTH_SHORT).show();
            return false;
        }

        File file = new File(mContext.getExternalFilesDir(dir), name);

        Log.d("storage helper", "file absolutepaht="+file.getAbsolutePath());

        if(file.isFile())
            file.delete();
        else{
            Toast.makeText(mContext, "Not Found file 2", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    public static boolean delete(Context mContext, String name) {
        boolean res = false;

        // Checks for external storage availability
        if (!checkStorage()) {
            //Toast.makeText(mContext, mContext.getString(R.string.storage_not_available), Toast.LENGTH_SHORT).show();
            return false;
        }

        File file = new File(name);
        if (file.isFile()) {
            res = file.delete();
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file2 : files) {
                res = delete(mContext, file2.getAbsolutePath());
            }
            res = file.delete();
        }

        return res;
    }


    public static String getRealPathFromURI(Context mContext, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);
		if (cursor == null) {
			return null;
		}
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
		String path = cursor.getString(column_index);
		cursor.close();
        return path;
    }


    public static File createNewAttachmentFile(Context mContext, String extension) {
        File f = null;
        if (checkStorage()) {
            f = new File(mContext.getExternalFilesDir(null), createNewAttachmentName(extension));
        }
        return f;
    }

    public static File createNewAttachmentFile(Context mContext, String folder, String extension) {
        File f = null;
        if (checkStorage()) {
            f = new File(mContext.getExternalFilesDir(folder), createNewAttachmentName(extension));
        }
        return f;
    }


    public static synchronized String createNewAttachmentName(String extension) {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_SORTABLE);
        String name = sdf.format(now.getTime());
        name += extension != null ? extension : "";
        return name;
    }


    public static File createNewAttachmentFile(Context mContext) {
        return createNewAttachmentFile(mContext, null);
    }


    /**
     * Create a path where we will place our private file on external
     */
    public static File copyToBackupDir(File backupDir, File file) {
        if (!checkStorage()) {
            return null;
        }
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        File destination = new File(backupDir, file.getName());
        copyFile(file, destination);
        return destination;
    }


    public static File getCacheDir(Context mContext) {
        File dir = mContext.getExternalCacheDir();
        if (!dir.exists())
            dir.mkdirs();
        return dir;
    }

    public static File getSharedPreferencesFile(Context mContext) {
        File appData = mContext.getFilesDir().getParentFile();
        String packageName = mContext.getApplicationContext().getPackageName();
        return new File(appData
                + System.getProperty("file.separator")
                + "shared_prefs"
                + System.getProperty("file.separator")
                + packageName
                + "_preferences.xml");
    }


    /**
     * Returns a directory size in bytes
     */
    @SuppressWarnings("deprecation")
    public static long getSize(File directory) {
        StatFs statFs = new StatFs(directory.getAbsolutePath());
        long blockSize = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFs.getBlockSizeLong();
            } else {
                blockSize = statFs.getBlockSize();
            }
            // Can't understand why on some devices this fails
        } catch (NoSuchMethodError e) {
            Log.e(Const.ERROR_TAG, "Mysterious error", e);
        }
        return getSize(directory, blockSize);
    }


    private static long getSize(File directory, long blockSize) {
        File[] files = directory.listFiles();
        if (files != null) {

            // space used by directory itself 
            long size = directory.length();

            for (File file : files) {
                if (file.isDirectory()) {
                    // space used by subdirectory
                    size += getSize(file, blockSize);
                } else {
                    // file size need to rounded up to full block sizes
                    // (not a perfect function, it adds additional block to 0 sized files
                    // and file who perfectly fill their blocks) 
                    size += (file.length() / blockSize + 1) * blockSize;
                }
            }
            return size;
        } else {
            return 0;
        }
    }


    public static boolean copyDirectory(File sourceLocation, File targetLocation) {
        boolean res = true;

        // If target is a directory the method will be iterated
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {
                res = res && copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation,
                        children[i]));
            }

            // Otherwise a file copy will be performed
        } else {
			res = copyFile(sourceLocation, targetLocation);
        }
        return res;
    }


    /**
     * Retrieves uri mime-type using ContentResolver
     *
     * @param mContext
     * @param uri
     * @return
     */
    public static String getMimeType(Context mContext, Uri uri) {
        ContentResolver cR = mContext.getContentResolver();
        String mimeType = cR.getType(uri);
        if (mimeType == null) {
            mimeType = getMimeType(uri.toString());
        }
        return mimeType;
    }



    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }


    /**
     * Retrieves uri mime-type between the ones managed by application
     *
     * @param mContext
     * @param uri
     * @return
     */
    public static String getMimeTypeInternal(Context mContext, Uri uri) {
        String mimeType = getMimeType(mContext, uri);
        mimeType = getMimeTypeInternal(mContext, mimeType);
        return mimeType;
    }


    /**
     * Retrieves mime-type between the ones managed by application from given string
     *
     * @param mContext
     * @param mimeType
     * @return
     */
    public static String getMimeTypeInternal(Context mContext, String mimeType) {
        if (mimeType != null) {
            if (mimeType.contains("image/")) {
                mimeType = Constants.MIME_TYPE_IMAGE;
            } else if (mimeType.contains("audio/")) {
                mimeType = Constants.MIME_TYPE_AUDIO;
            } else if (mimeType.contains("video/")) {
                mimeType = Constants.MIME_TYPE_VIDEO;
            } else {
                mimeType = Constants.MIME_TYPE_FILES;
            }
        }
        return mimeType;
    }

}
