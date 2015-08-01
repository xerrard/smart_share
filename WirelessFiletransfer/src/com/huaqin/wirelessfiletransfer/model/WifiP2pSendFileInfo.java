package com.huaqin.wirelessfiletransfer.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

/**
 * This class stores information about a single sending file It will only be
 * used for outbound share.
 */
public class WifiP2pSendFileInfo {
    private static final String TAG = "[Bluetooth.OPP]BluetoothOppSendFileInfo";


    /** Reusable SendFileInfo for error status. */
    static final WifiP2pSendFileInfo SEND_FILE_INFO_ERROR = new WifiP2pSendFileInfo(
            null, null, 0, null);

    /** readable media file name */
    public final String mFileName;

    /** media file input stream */
    public final FileInputStream mInputStream;

    /** vCard string data */
    public final String mData;

    public final String mMimetype;

    public final long mLength;

    /** for media file */
    public WifiP2pSendFileInfo(String fileName, String type, long length,
            FileInputStream inputStream) {
        mFileName = fileName;
        mMimetype = type;
        mLength = length;
        mInputStream = inputStream;
        mData = null;
    }

    /** for vCard, or later for vCal, vNote. Not used currently */
    public WifiP2pSendFileInfo(String data, String type, long length) {
        mFileName = null;
        mInputStream = null;
        mData = data;
        mMimetype = type;
        mLength = length;
    }

    public static WifiP2pSendFileInfo generateFileInfo(Context context, Uri uri,
            String type) {
        ContentResolver contentResolver = context.getContentResolver();
        String scheme = uri.getScheme();
        String fileName = null;
        String contentType;
        long length = 0;
        Log.i(TAG, "generateFileInfo ++");
        // Support all Uri with "content" scheme
        // This will allow more 3rd party applications to share files via
        // bluetooth
        if ("content".equals(scheme)) {
            contentType = contentResolver.getType(uri);
            Cursor metadataCursor;
            try {
                metadataCursor = contentResolver.query(uri, new String[] {
                        OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
                }, null, null, null);
            } catch (Exception e) {//roshan modify can't support gms Permission 20150115
                // some content providers don't support the DISPLAY_NAME or SIZE columns
                metadataCursor = null;
            }
            if (metadataCursor != null) {
                try {
                    if (metadataCursor.moveToFirst()) {
                        fileName = metadataCursor.getString(0);
                        length = metadataCursor.getInt(1);
                    }
                } finally {
                    metadataCursor.close();
                }
            }
            if (fileName == null) {
                // use last segment of URI if DISPLAY_NAME query fails
                fileName = uri.getLastPathSegment();
            }
        } else if ("file".equals(scheme)) {
            fileName = uri.getLastPathSegment();
            contentType = type;
            File f = new File(uri.getPath());
            length = f.length();
        } else {
            // currently don't accept other scheme
            return SEND_FILE_INFO_ERROR;
        }
        FileInputStream is = null;
        if (scheme.equals("content")) {
            try {
                // We've found that content providers don't always have the
                // right size in _OpenableColumns.SIZE
                // As a second source of getting the correct file length,
                // get a file descriptor and get the stat length
                AssetFileDescriptor fd = contentResolver.openAssetFileDescriptor(uri, "r");
                long statLength = fd.getLength();
                if (length != statLength && statLength > 0) {
                    Log.e(TAG, "Content provider length is wrong (" + Long.toString(length) +
                            "), using stat length (" + Long.toString(statLength) + ")");
                    length = statLength;
                }
                try {
                    // This creates an auto-closing input-stream, so
                    // the file descriptor will be closed whenever the InputStream
                    // is closed.
                    is = fd.createInputStream();
                } catch (IOException e) {
                    try {
                        fd.close();
                    } catch (IOException e2) {
                        // Ignore
                        Log.e(TAG, "close exception");
                        e2.printStackTrace();
                    }
                    Log.e(TAG, "createInputStream exception");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                // Ignore
                Log.e(TAG, "file not found exception");
                e.printStackTrace();
            }
        }
        if (is == null) {
            Log.w(TAG, "is == null");
            try {
                is = (FileInputStream) contentResolver.openInputStream(uri);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "return send_file_info_error");
                e.printStackTrace();
                return SEND_FILE_INFO_ERROR;
            }
        }
        // If we can not get file length from content provider, we can try to
        // get the length via the opened stream.
        if (length == 0) {
            try {
                length = is.available();
            } catch (IOException e) {
                Log.e(TAG, "Read stream exception: ", e);
                return SEND_FILE_INFO_ERROR;
            }
        }

        return new WifiP2pSendFileInfo(fileName, contentType, length, is);
    }
}

