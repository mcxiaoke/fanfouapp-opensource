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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;

import com.fanfou.app.opensource.AppContext;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.05
 * @version 2.0 2011.09.23
 * @version 3.0 2011.10.29
 * @version 3.1 2011.11.08
 * @version 3.2 2011.12.26
 * 
 */
final public class ImageHelper {
    private static final String TAG = ImageHelper.class.getSimpleName();
    public static final int IMAGE_QUALITY_HIGH = 90;
    public static final int IMAGE_QUALITY_MEDIUM = 80;
    public static final int IMAGE_QUALITY_LOW = 70;
    public static final int IMAGE_MAX_WIDTH = 596;// 640 596
    private static final int IMAGE_DIM_OFFSET = 48;
    private static final int IMAGE_COMPRESS_SIZE = 64 * 1024;
    // public static final int IMAGE_MAX_HEIGHT = 1200;// 1320 1192
    public static final int OUTPUT_BUFFER_SIZE = 8196;

    public static Bitmap captureViewToBitmap(final View view) {
        Bitmap result = null;

        try {
            result = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                    Bitmap.Config.RGB_565);
            view.draw(new Canvas(result));
        } catch (final Exception e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private static boolean checkFsWritable() {
        // Create a temporary file to see whether a volume is really writeable.
        // It's important not to put it in the root directory which may have a
        // limit on the number of files.
        final String directoryName = Environment.getExternalStorageDirectory()
                .toString() + "/DCIM";
        final File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }
        return directory.canWrite();
    }

    public static boolean checkStorageWritable() {
        final String directoryName = Environment.getExternalStorageDirectory()
                .toString();
        final File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }
        final File f = new File(directoryName, ".probe");
        try {
            if (!f.createNewFile()) {
                return false;
            }
            f.delete();
            return true;
        } catch (final IOException ex) {
            return false;
        }
    }

    // public static Bitmap resampleImage(String path, int maxDim)
    // throws Exception {
    //
    // BitmapFactory.Options bfo = new BitmapFactory.Options();
    // bfo.inJustDecodeBounds = true;
    // BitmapFactory.decodeFile(path, bfo);
    //
    // BitmapFactory.Options optsDownSample = new BitmapFactory.Options();
    // optsDownSample.inSampleSize = getClosestResampleSize(bfo.outWidth,
    // bfo.outHeight, maxDim);
    //
    // Bitmap bmpt = BitmapFactory.decodeFile(path, optsDownSample);
    //
    // Matrix m = new Matrix();
    //
    // if (bmpt.getWidth() > maxDim || bmpt.getHeight() > maxDim) {
    // BitmapFactory.Options optsScale = getResampling(bmpt.getWidth(),
    // bmpt.getHeight(), maxDim);
    // m.postScale((float) optsScale.outWidth / (float) bmpt.getWidth(),
    // (float) optsScale.outHeight / (float) bmpt.getHeight());
    // }
    //
    // int sdk = new Integer(Build.VERSION.SDK).intValue();
    // if (sdk > 4) {
    // int rotation = getExifOrientation(path);
    // if (rotation != 0) {
    // m.postRotate(rotation);
    // }
    // }
    // return Bitmap.createBitmap(bmpt, 0, 0, bmpt.getWidth(),
    // bmpt.getHeight(), m, true);
    // }

    /**
     * 
     * @param bitmap
     * @param quality
     *            1 ~ 100
     * @return
     */
    public static byte[] compressBitmap(final Bitmap bitmap, final int quality) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, baos);
            return baos.toByteArray();
        } catch (final Exception e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static File compressForUpload(final String srcFileName,
            final String destFileName, final int maxWidth, int quality) {
        final boolean keepOriginal = ImageHelper.keepOriginal(srcFileName,
                ImageHelper.IMAGE_MAX_WIDTH);
        if (keepOriginal) {
            if (AppContext.DEBUG) {
                Log.d(ImageHelper.TAG,
                        "compressForUpload keep Original,no Need Compress");
            }
            return new File(srcFileName);
        }
        final Bitmap bitmap = ImageHelper.compressImage(srcFileName, maxWidth);
        if (bitmap == null) {
            return null;
        }
        if (AppContext.DEBUG) {
            Log.d(ImageHelper.TAG,
                    "compressForUpload bitmap=(" + bitmap.getWidth() + ","
                            + bitmap.getHeight() + ")");
        }
        FileOutputStream fos = null;
        try {
            Bitmap.CompressFormat format = CompressFormat.JPEG;
            if (srcFileName.toLowerCase().lastIndexOf("png") > -1) {
                format = CompressFormat.PNG;
            }
            if (quality > ImageHelper.IMAGE_QUALITY_HIGH) {
                quality = ImageHelper.IMAGE_QUALITY_HIGH;
            } else if (quality < ImageHelper.IMAGE_QUALITY_LOW) {
                quality = ImageHelper.IMAGE_QUALITY_LOW;
            }
            fos = new FileOutputStream(destFileName);
            bitmap.compress(format, quality, fos);
            return new File(destFileName);
        } catch (final FileNotFoundException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            return null;
        } finally {
            IOHelper.forceClose(fos);
        }
    }

    public static Bitmap compressImage(final String path, final int maxDim) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int inSampleSize = 1;
        for (int w = options.outWidth; w > (maxDim * 2); w /= 2) {
            inSampleSize += 1;
        }
        if (AppContext.DEBUG) {
            Log.d(ImageHelper.TAG, "compressImage original=("
                    + options.outWidth + "," + options.outHeight + ")");
            Log.d(ImageHelper.TAG, "compressImage inSampleSize=" + inSampleSize);
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        final Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (bitmap != null) {
            final int bw = bitmap.getWidth();
            final int bh = bitmap.getHeight();
            final Matrix m = new Matrix();
            if ((bw > maxDim) || (bh > maxDim)) {
                final float scale = (float) maxDim / (float) bw;
                m.postScale(scale, scale);
                if (AppContext.DEBUG) {
                    Log.d(ImageHelper.TAG, "compressImage matrix scale="
                            + scale);
                }
            }
            final int rotation = ImageHelper.getExifOrientation(path);
            if (ImageHelper.getExifOrientation(path) != 0) {
                m.postRotate(rotation);
            }
            if (AppContext.DEBUG) {
                Log.d(ImageHelper.TAG, "compressImage matrix rotation="
                        + rotation);
                Log.d(ImageHelper.TAG, "compressImage bitmap=(" + bw + "," + bh
                        + ")");
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bw, bh, m, true);
        }
        return null;

    }

    private static int computeSampleSize(final InputStream stream,
            final int maxW, final int maxH) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);
        final double w = options.outWidth;
        final double h = options.outHeight;
        final int sampleSize = (int) Math.ceil(Math.max(w / maxW, h / maxH));
        return sampleSize;
    }

    private static int computeSampleSize(final String path, final int maxW,
            final int maxH) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        final double w = options.outWidth;
        final double h = options.outHeight;
        final int sampleSize = (int) Math.ceil(Math.max(w / maxW, h / maxH));
        return sampleSize;
    }

    /**
     * 
     * @param bytes
     * @return
     */
    public static Bitmap getBitmapFromBytes(final byte[] bytes) {
        try {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (final Exception e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 
     * @param path
     * @param sampleSize
     *            1 = 100%, 2 = 50%(1/2), 4 = 25%(1/4), ...
     * @return
     */
    public static Bitmap getBitmapFromPath(final String path,
            final int sampleSize) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            return BitmapFactory.decodeFile(path, options);
        } catch (final Exception e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * generate a blurred bitmap from given one
     * 
     * referenced: http://incubator.quasimondo.com/processing/superfastblur.pde
     * 
     * @param original
     * @param radius
     * @return
     */
    public static Bitmap getBlurredBitmap(final Bitmap original,
            final int radius) {
        if (radius < 1) {
            return null;
        }

        final int width = original.getWidth();
        final int height = original.getHeight();
        final int wm = width - 1;
        final int hm = height - 1;
        final int wh = width * height;
        final int div = radius + radius + 1;
        final int r[] = new int[wh];
        final int g[] = new int[wh];
        final int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, p1, p2, yp, yi, yw;
        final int vmin[] = new int[Math.max(width, height)];
        final int vmax[] = new int[Math.max(width, height)];
        final int dv[] = new int[256 * div];
        for (i = 0; i < (256 * div); i++) {
            dv[i] = i / div;
        }

        final int[] blurredBitmap = new int[wh];
        original.getPixels(blurredBitmap, 0, width, 0, 0, width, height);

        yw = 0;
        yi = 0;

        for (y = 0; y < height; y++) {
            rsum = 0;
            gsum = 0;
            bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = blurredBitmap[yi + Math.min(wm, Math.max(i, 0))];
                rsum += (p & 0xff0000) >> 16;
                gsum += (p & 0x00ff00) >> 8;
                bsum += p & 0x0000ff;
            }
            for (x = 0; x < width; x++) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                    vmax[x] = Math.max(x - radius, 0);
                }
                p1 = blurredBitmap[yw + vmin[x]];
                p2 = blurredBitmap[yw + vmax[x]];

                rsum += ((p1 & 0xff0000) - (p2 & 0xff0000)) >> 16;
                gsum += ((p1 & 0x00ff00) - (p2 & 0x00ff00)) >> 8;
                bsum += (p1 & 0x0000ff) - (p2 & 0x0000ff);
                yi++;
            }
            yw += width;
        }

        for (x = 0; x < width; x++) {
            rsum = gsum = bsum = 0;
            yp = -radius * width;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;
                rsum += r[yi];
                gsum += g[yi];
                bsum += b[yi];
                yp += width;
            }
            yi = x;
            for (y = 0; y < height; y++) {
                blurredBitmap[yi] = 0xff000000 | (dv[rsum] << 16)
                        | (dv[gsum] << 8) | dv[bsum];
                if (x == 0) {
                    vmin[y] = Math.min(y + radius + 1, hm) * width;
                    vmax[y] = Math.max(y - radius, 0) * width;
                }
                p1 = x + vmin[y];
                p2 = x + vmax[y];

                rsum += r[p1] - r[p2];
                gsum += g[p1] - g[p2];
                bsum += b[p1] - b[p2];

                yi += width;
            }
        }

        return Bitmap.createBitmap(blurredBitmap, width, height,
                Bitmap.Config.RGB_565);
    }

    private static int getClosestResampleSize(final int cx, final int cy,
            final int maxDim) {
        final int max = Math.max(cx, cy);

        int resample = 1;
        for (resample = 1; resample < Integer.MAX_VALUE; resample++) {
            if ((resample * maxDim) > max) {
                resample--;
                break;
            }
        }

        if (resample > 0) {
            return resample;
        }
        return 1;
    }

    public static Uri getContentUriFromFile(final Context ctx,
            final File imageFile) {
        Uri uri = null;
        final ContentResolver cr = ctx.getContentResolver();
        // Columns to return
        final String[] projection = { BaseColumns._ID, MediaColumns.DATA };
        // Look for a picture which matches with the requested path
        // (MediaStore stores the path in column Images.Media.DATA)
        final String selection = MediaColumns.DATA + " = ?";
        final String[] selArgs = { imageFile.toString() };

        final Cursor cursor = cr.query(Images.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selArgs, null);

        if (cursor.moveToFirst()) {

            String id;
            final int idColumn = cursor.getColumnIndex(BaseColumns._ID);
            id = cursor.getString(idColumn);
            uri = Uri.withAppendedPath(Images.Media.EXTERNAL_CONTENT_URI, id);
        }
        cursor.close();
        if (uri != null) {
            Log.d(ImageHelper.TAG,
                    "Found picture in MediaStore : " + imageFile.toString()
                            + " is " + uri.toString());
        } else {
            Log.d(ImageHelper.TAG, "Did not find picture in MediaStore : "
                    + imageFile.toString());
        }
        return uri;
    }

    public static int getExifOrientation(final String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (final IOException ex) {
            Log.e(ImageHelper.TAG, "cannot read exif", ex);
        }
        if (exif != null) {
            final int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                }

            }
        }
        return degree;
    }

    private static BitmapFactory.Options getResampling(final int cx,
            final int cy, final int max) {
        float scaleVal = 1.0f;
        final BitmapFactory.Options bfo = new BitmapFactory.Options();
        if (cx > cy) {
            scaleVal = (float) max / (float) cx;
        } else if (cy > cx) {
            scaleVal = (float) max / (float) cy;
        } else {
            scaleVal = (float) max / (float) cx;
        }
        bfo.outWidth = (int) ((cx * scaleVal) + 0.5f);
        bfo.outHeight = (int) ((cy * scaleVal) + 0.5f);
        return bfo;
    }

    /**
     * 圆角图片
     * 
     * @param bitmap
     * @param pixels
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(final Bitmap bitmap,
            final int pixels) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap getRoundedCornerBitmap2(final Bitmap bitmap,
            final int radius) {
        final Paint paintForRound = new Paint();
        paintForRound.setAntiAlias(true);
        paintForRound.setColor(0xff424242);
        paintForRound.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        canvas.drawARGB(0, 0, 0, 0);
        paintForRound.setXfermode(null);

        canvas.drawRoundRect(rectF, radius, radius, paintForRound);

        paintForRound.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paintForRound);

        return output;
    }

    public static boolean hasStorage() {
        return ImageHelper.hasStorage(true);
    }

    public static boolean hasStorage(final boolean requireWriteAccess) {
        final String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (requireWriteAccess) {
                final boolean writable = ImageHelper.checkFsWritable();
                return writable;
            } else {
                return true;
            }
        } else if (!requireWriteAccess
                && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static Uri insertImage(final ContentResolver cr, final File file,
            final int degree) {
        final long size = file.length();
        final String name = file.getName();
        final ContentValues values = new ContentValues(9);
        values.put(MediaColumns.TITLE, name);
        values.put(MediaColumns.DISPLAY_NAME, name);
        values.put(ImageColumns.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(ImageColumns.ORIENTATION, degree);
        values.put(MediaColumns.DATA, file.getAbsolutePath());
        values.put(MediaColumns.SIZE, size);
        // return cr.insert(STORAGE_URI, values);
        return null;
    }

    public static boolean isStorageWritable(final Context context) {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        final String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // can read and write
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // can only read
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        final boolean goodmount = mExternalStorageAvailable
                && mExternalStorageWriteable;
        return goodmount;
    }

    public static boolean keepOriginal(final String path, final int maxDim) {
        final File file = new File(path);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return (file.exists() && (file.length() < ImageHelper.IMAGE_COMPRESS_SIZE))
                || (options.outWidth <= (maxDim + ImageHelper.IMAGE_DIM_OFFSET));
    }

    public static Bitmap loadFromPath(final Context context, final String path,
            final int maxW, final int maxH) throws IOException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        // options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = ImageHelper.computeSampleSize(path, maxW, maxH);
        // options.inDither = false;
        options.inJustDecodeBounds = false;
        // options.inPurgeable = true;
        final Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    public static Bitmap loadFromUri(final Context context, final String uri,
            final int maxW, final int maxH) throws IOException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = null;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BufferedInputStream stream = null;
        if (uri.startsWith(ContentResolver.SCHEME_CONTENT)
                || uri.startsWith(ContentResolver.SCHEME_FILE)) {
            stream = new BufferedInputStream(context.getContentResolver()
                    .openInputStream(Uri.parse(uri)), 16384);
        }
        if (stream != null) {
            options.inSampleSize = ImageHelper.computeSampleSize(stream, maxW,
                    maxH);
            stream = null;
            stream = new BufferedInputStream(context.getContentResolver()
                    .openInputStream(Uri.parse(uri)), 16384);
        } else {
            return null;
        }
        options.inDither = false;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        bitmap = BitmapFactory.decodeStream(stream, null, options);
        return bitmap;
    }

    /**
     * apply filter to a bitmap
     * 
     * @param original
     * @param filter
     * @return filtered bitmap
     */
    // public static Bitmap applyFilter(Bitmap original, FilterBase filter) {
    // return filter.filter(original);
    // }

    public static File prepareProfileImage(final Context context,
            final File file) {
        final File destFile = new File(IOHelper.getImageCacheDir(context),
                "fanfouprofileimage.jpg");
        return ImageHelper.compressForUpload(file.getPath(),
                destFile.getPath(), 100, ImageHelper.IMAGE_QUALITY_MEDIUM);
    }

    public static File prepareUploadFile(final Context context,
            final File file, final int quality) {
        final File destFile = new File(IOHelper.getImageCacheDir(context),
                "fanfouupload.jpg");
        return ImageHelper.compressForUpload(file.getPath(),
                destFile.getPath(), ImageHelper.IMAGE_MAX_WIDTH, quality);
    }

    public static void releaseBitmap(Bitmap bitmap) {
        if ((bitmap != null) && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public static Bitmap resampleImage(final Context context, final Uri uri,
            final int maxDim) throws Exception {
        final String path = IOHelper.getRealPathFromURI(context, uri);
        return ImageHelper.resampleImage(path, maxDim);
    }

    public static Bitmap resampleImage(final File file, final int maxDim)
            throws Exception {
        return ImageHelper.resampleImage(file.getAbsolutePath(), maxDim);
    }

    public static Bitmap resampleImage(final String path, final int maxDim)
            throws Exception {

        final BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bfo);

        final BitmapFactory.Options optsDownSample = new BitmapFactory.Options();
        optsDownSample.inSampleSize = ImageHelper.getClosestResampleSize(
                bfo.outWidth, bfo.outHeight, maxDim);

        final Bitmap bmpt = BitmapFactory.decodeFile(path, optsDownSample);

        final Matrix m = new Matrix();

        if ((bmpt.getWidth() > maxDim) || (bmpt.getHeight() > maxDim)) {
            final BitmapFactory.Options optsScale = ImageHelper.getResampling(
                    bmpt.getWidth(), bmpt.getHeight(), maxDim);
            m.postScale((float) optsScale.outWidth / (float) bmpt.getWidth(),
                    (float) optsScale.outHeight / (float) bmpt.getHeight());
        }

        final int sdk = Integer.valueOf(Build.VERSION.SDK);
        if (sdk > 4) {
            final int rotation = ImageHelper.getExifOrientation(path);
            if (rotation != 0) {
                m.postRotate(rotation);
            }
        }
        return Bitmap.createBitmap(bmpt, 0, 0, bmpt.getWidth(),
                bmpt.getHeight(), m, true);
    }

    public static Bitmap resizeBitmap(final Bitmap input, int destWidth,
            int destHeight) {
        final int srcWidth = input.getWidth();
        final int srcHeight = input.getHeight();
        boolean needsResize = false;
        float p;
        if ((srcWidth > destWidth) || (srcHeight > destHeight)) {
            needsResize = true;
            if ((srcWidth > srcHeight) && (srcWidth > destWidth)) {
                p = (float) destWidth / (float) srcWidth;
                destHeight = (int) (srcHeight * p);
            } else {
                p = (float) destHeight / (float) srcHeight;
                destWidth = (int) (srcWidth * p);
            }
        } else {
            destWidth = srcWidth;
            destHeight = srcHeight;
        }
        if (needsResize) {
            final Bitmap output = Bitmap.createScaledBitmap(input, destWidth,
                    destHeight, true);
            return output;
        } else {
            return input;
        }
    }

    public static Bitmap resizeBitmap(final String filePath, final int width,
            final int height) {
        Bitmap bitmap = null;
        final Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        double sampleSize = 0;
        final Boolean scaleByHeight = Math.abs(options.outHeight - height) >= Math
                .abs(options.outWidth - width);

        if ((options.outHeight * options.outWidth * 2) >= 16384) {
            sampleSize = scaleByHeight ? options.outHeight / height
                    : options.outWidth / width;
            sampleSize = (int) Math.pow(2d,
                    Math.floor(Math.log(sampleSize) / Math.log(2d)));
        }

        options.inJustDecodeBounds = false;
        options.inTempStorage = new byte[128];
        while (true) {
            try {
                options.inSampleSize = (int) sampleSize;
                bitmap = BitmapFactory.decodeFile(filePath, options);
                break;
            } catch (final Exception ex) {
                sampleSize = sampleSize * 2;
            }
        }
        return bitmap;
    }

    /**
     * Rotate a bitmap.
     * 
     * @param bmp
     *            A Bitmap of the picture.
     * @param degrees
     *            Angle of the rotation, in degrees.
     * @return The rotated bitmap, constrained in the source bitmap dimensions.
     */
    public static Bitmap rotate(final Bitmap bmp, final float degrees) {
        if ((degrees % 360) != 0) {
            Log.d(ImageHelper.TAG, "Rotating bitmap " + degrees + "°");
            final Matrix rotMat = new Matrix();
            rotMat.postRotate(degrees);

            if (bmp != null) {
                final Bitmap dst = Bitmap.createBitmap(bmp, 0, 0,
                        bmp.getWidth(), bmp.getHeight(), rotMat, false);

                return dst;
            }
        } else {
            return bmp;
        }
        return null;
    }

    public static Bitmap rotateImageFile(final String filePath) {
        final int orientation = ImageHelper.getExifOrientation(filePath);
        final Bitmap source = BitmapFactory.decodeFile(filePath);
        final int sw = source.getWidth();
        final int sh = source.getHeight();
        final Matrix matrix = new Matrix();
        matrix.setRotate(orientation);
        final Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, sw, sh, matrix,
                true);
        ImageHelper.releaseBitmap(source);
        return bitmap;
    }

    public static int roundOrientation(final int orientationInput) {
        // landscape mode
        int orientation = orientationInput;

        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            orientation = 0;
        }

        orientation = orientation % 360;
        int retVal;
        if (orientation < ((0 * 90) + 45)) {
            retVal = 0;
        } else if (orientation < ((1 * 90) + 45)) {
            retVal = 90;
        } else if (orientation < ((2 * 90) + 45)) {
            retVal = 180;
        } else if (orientation < ((3 * 90) + 45)) {
            retVal = 270;
        } else {
            retVal = 0;
        }

        return retVal;
    }

    public static boolean saveBitmap(final Bitmap original,
            final Bitmap.CompressFormat format, final int quality,
            final File outputFile) {
        if (original == null) {
            return false;
        }

        try {
            return original.compress(format, quality, new FileOutputStream(
                    outputFile));
        } catch (final Exception e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static boolean saveBitmap(final Bitmap original,
            final Bitmap.CompressFormat format, final int quality,
            final String outputFilePath) {
        if (original == null) {
            return false;
        }
        try {
            return original.compress(format, quality, new FileOutputStream(
                    outputFilePath));
        } catch (final Exception e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static Bitmap scaleImageFile(final Context context, final File file,
            final int size) {
        final Uri uri = Uri.fromFile(file);
        return ImageHelper.scaleImageFromUri(context, uri, size);
    }

    public static Bitmap scaleImageFromUri(final Context context,
            final Uri uri, final int size) {
        Bitmap bitmap = null;
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            is.close();
            int scale = 1;
            while (((options.outWidth / scale) > size)
                    || ((options.outHeight / scale) > size)) {
                scale *= 2;
            }
            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;
            is = context.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(is, null, options);
        } catch (final IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (final IOException e) {
                }
            }
        }
        return bitmap;
    }

    /**
     * Store a picture that has just been saved to disk in the MediaStore.
     * 
     * @param imageFile
     *            The File of the picture
     * @return The Uri provided by the MediaStore.
     */
    public static Uri storePicture(final Context ctx, final File imageFile,
            String imageName) {
        final ContentResolver cr = ctx.getContentResolver();
        imageName = imageName.substring(imageName.lastIndexOf('/') + 1);
        final ContentValues values = new ContentValues(7);
        values.put(MediaColumns.TITLE, imageName);
        values.put(MediaColumns.DISPLAY_NAME, imageName);
        values.put(ImageColumns.DESCRIPTION, "");
        values.put(ImageColumns.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(ImageColumns.ORIENTATION, 0);
        final File parentFile = imageFile.getParentFile();
        final String path = parentFile.toString().toLowerCase();
        final String name = parentFile.getName().toLowerCase();
        values.put(Images.ImageColumns.BUCKET_ID, path.hashCode());
        values.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, name);
        values.put("_data", imageFile.toString());

        final Uri uri = cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values);

        return uri;
    }

    public static boolean writeToFile(final File file, final Bitmap bitmap) {
        if ((bitmap == null) || (file == null) || file.exists()) {
            return false;
        }
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file),
                    ImageHelper.OUTPUT_BUFFER_SIZE);
            return bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        } catch (final IOException e) {
            if (AppContext.DEBUG) {
                Log.d(ImageHelper.TAG, "writeToFile:" + e.getMessage());
            }
        } finally {
            IOHelper.forceClose(bos);
        }
        return false;
    }

    public static boolean writeToFile(final String filePath, final Bitmap bitmap) {
        if ((bitmap == null) || StringHelper.isEmpty(filePath)) {
            return false;
        }
        final File file = new File(filePath);
        return ImageHelper.writeToFile(file, bitmap);
    }

}
