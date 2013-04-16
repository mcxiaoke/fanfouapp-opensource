/*******************************************************************************
 * Copyright 2011, 2012, 2013 fanfou.com, Xiaoke, Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.fanfou.app.opensource.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;
import android.text.ClipboardManager;

import com.fanfou.app.opensource.api.bean.DirectMessage;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.db.Contents.DirectMessageInfo;
import com.fanfou.app.opensource.db.Contents.DraftInfo;
import com.fanfou.app.opensource.db.Contents.StatusInfo;
import com.fanfou.app.opensource.db.Contents.UserInfo;

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.19
 * @version 1.1 2011.10.11
 * 
 */
public final class IOHelper {
    public static final SimpleDateFormat IMG_NAME_FORMAT = new SimpleDateFormat(
            "'IMG'_yyyyMMdd_HHmmss.'jpg'");
    public static final SimpleDateFormat IMG2_NAME_FORMAT = new SimpleDateFormat(
            "'IMG2'_yyyyMMdd_HHmmss.'jpg'");

    public static void cleanDB(final Context context) {
        final ContentResolver cr = context.getContentResolver();
        cr.delete(StatusInfo.CONTENT_URI, null, null);
        cr.delete(UserInfo.CONTENT_URI, null, null);
        cr.delete(DirectMessageInfo.CONTENT_URI, null, null);
        cr.delete(DraftInfo.CONTENT_URI, null, null);
    }

    public static void ClearBigPictures(final Context context) {
        IOHelper.deleteDir(IOHelper.getImageCacheDir(context), 6 * 1024);
    }

    public static void ClearCache(final Context context) {
        final File target = IOHelper.getImageCacheDir(context);
        if (target.exists() == false) {
            return;
        }
        if (target.isFile()) {
            target.delete();
        }

        if (target.isDirectory()) {
            final File[] files = target.listFiles();
            for (final File file : files) {
                IOHelper.deleteDir(file);
            }
            target.delete();
        }
    }

    public static void closeStream(final Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (final IOException e) {
            }
        }
    }

    public static void copyFile(final File src, final File dest)
            throws IOException {
        final FileChannel srcChannel = new FileInputStream(src).getChannel();
        final FileChannel destChannel = new FileOutputStream(dest).getChannel();
        srcChannel.transferTo(0, srcChannel.size(), destChannel);
        srcChannel.close();
        destChannel.close();
    }

    public static void copyStream(final InputStream in, final OutputStream out)
            throws IOException {
        final byte[] b = new byte[8 * 1024];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

    public static void copyStream(final InputStream in, final OutputStream out,
            final int bufferSize) throws IOException {
        final byte[] buf = new byte[bufferSize];

        int len = 0;

        while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }
    }

    public static void copyToClipBoard(final Context context,
            final String content) {
        final ClipboardManager cm = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(content);
    }

    public static void deleteDir(final File target) {
        if (!target.exists()) {
            return;
        }
        if (target.isFile()) {
            target.delete();
        }

        if (target.isDirectory()) {
            final File[] files = target.listFiles();
            for (final File file : files) {
                IOHelper.deleteDir(file);
            }
            target.delete();
        }
    }

    public static void deleteDir(final File target, final int minFileSize) {
        if (!target.exists()) {
            return;
        }
        if (target.isFile()) {
            if (target.length() > minFileSize) {
                target.delete();
            }
        }

        if (target.isDirectory()) {
            final File[] files = target.listFiles();
            for (final File file : files) {
                IOHelper.deleteDir(file, minFileSize);
            }
        }
    }

    public static void forceClose(final Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (final IOException e) {
        }
    }

    public static File getDownloadDir(final Context context) {
        File cacheDir;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(Environment.getExternalStorageDirectory(),
                    "/download");
        } else {
            cacheDir = context.getCacheDir();
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    public static File getFilteredPhotoFilePath(final Context context) {
        final File baseDir = IOHelper.getPhotoDir(context);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        final Date date = new Date();
        final String filename = IOHelper.IMG2_NAME_FORMAT.format(date);
        return new File(baseDir, filename);
    }

    public static File getImageCacheDir(final Context context) {
        File cacheDir;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(Environment.getExternalStorageDirectory(),
                    "/Android/data/" + context.getPackageName() + "/photocache");
        } else {
            cacheDir = context.getCacheDir();
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
            final File nomedia = new File(cacheDir, ".nomedia");
            if (!nomedia.exists()) {
                nomedia.mkdirs();
            }
        }
        return cacheDir;
    }

    public static File getPhotoDir(final Context context) {
        File photoDir;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            photoDir = new File(Environment.getExternalStorageDirectory(),
                    "/DCIM/FANFOU");
        } else {
            photoDir = context.getCacheDir();
        }
        if (!photoDir.exists()) {
            photoDir.mkdirs();
        }
        return photoDir;
    }

    public static File getPhotoFilePath(final Context context) {
        final File baseDir = IOHelper.getPhotoDir(context);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        final Date date = new Date();
        final String filename = IOHelper.IMG_NAME_FORMAT.format(date);
        return new File(baseDir, filename);
    }

    public static String getRealPathFromURI(final Context context,
            final Uri contentUri) {
        // get path from uri like content://media//
        final Cursor cursor = context.getContentResolver().query(contentUri,
                new String[] { MediaColumns.DATA }, null, null, null);
        String path = null;
        if (cursor != null) {
            final int column_index = cursor
                    .getColumnIndexOrThrow(MediaColumns.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
        } else {
            path = null;
        }
        cursor.close();
        if (path == null) {
            path = contentUri.getPath();
        }
        return path;
    }

    public static void storeDirectMessage(final Context context,
            final DirectMessage dm) {
        final ContentResolver cr = context.getContentResolver();
        cr.insert(DirectMessageInfo.CONTENT_URI, dm.toContentValues());
    }

    public static void storeStatus(final Context context, final Status s) {
        final ContentResolver cr = context.getContentResolver();
        cr.insert(StatusInfo.CONTENT_URI, s.toContentValues());
    }

    public static void storeUser(final Context context, final User u) {
        final ContentResolver cr = context.getContentResolver();
        cr.insert(UserInfo.CONTENT_URI, u.toContentValues());
    }

    private IOHelper() {
        throw new IllegalAccessError("此类为静态工具类，不能被实例化");
    }

}
