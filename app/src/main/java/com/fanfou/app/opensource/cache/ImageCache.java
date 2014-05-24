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
package com.fanfou.app.opensource.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.util.IOHelper;
import com.fanfou.app.opensource.util.ImageHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.01
 * @version 1.1 2011.09.23
 * @version 1.5 2011.11.23
 * @version 1.6 2011.11.24
 * @version 1.7 2011.12.05
 * @version 1.8 2011.12.09
 * 
 */
final class ImageCache implements ICache<Bitmap> {
    private static final String TAG = ImageCache.class.getSimpleName();

    public static final int IMAGE_QUALITY = 100;

    public static ImageCache INSTANCE = null;

    public synchronized static ImageCache getInstance() {
        if (ImageCache.INSTANCE == null) {
            ImageCache.INSTANCE = new ImageCache(AppContext.getAppContext());
        }
        return ImageCache.INSTANCE;
    }

    final ConcurrentHashMap<String, SoftReference<Bitmap>> memoryCache;

    private File mCacheDir = null;

    Context mContext;

    private ImageCache(final Context context) {
        this.mContext = context;
        this.memoryCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>();
        this.mCacheDir = IOHelper.getImageCacheDir(this.mContext);
    }

    @Override
    public void clear() {
        final String[] files = this.mContext.fileList();
        for (final String file : files) {
            this.mContext.deleteFile(file);
        }
        synchronized (this) {
            this.memoryCache.clear();
        }
    }

    @Override
    public boolean containsKey(final String key) {
        return get(key) != null;
    }

    @Override
    public Bitmap get(final String key) {
        if (StringHelper.isEmpty(key)) {
            return null;
        }
        Bitmap bitmap = null;

        final SoftReference<Bitmap> reference = this.memoryCache.get(key);
        if (reference != null) {
            bitmap = reference.get();
        }
        if (bitmap == null) {
            bitmap = loadFromFile(key);
            if (bitmap == null) {
                this.memoryCache.remove(key);
            } else {
                if (AppContext.DEBUG) {
                    Log.d(ImageCache.TAG,
                            "get() bitmap from disk, put to memory cache");
                }
                this.memoryCache.put(key, new SoftReference<Bitmap>(bitmap));
            }
        }
        return bitmap;
    }

    @Override
    public int getCount() {
        return this.memoryCache.size();
    }

    @Override
    public boolean isEmpty() {
        return this.memoryCache.isEmpty();
    }

    private Bitmap loadFromFile(final String key) {
        final String filename = StringHelper.md5(key) + ".jpg";
        final File file = new File(this.mCacheDir, filename);
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis);
            if (AppContext.DEBUG) {
                Log.d(ImageCache.TAG, "loadFromFile() key is " + key);
            }
        } catch (final FileNotFoundException e) {
            if (AppContext.DEBUG) {
                Log.d(ImageCache.TAG, "loadFromFile: " + e.getMessage());
            }
        } finally {
            IOHelper.forceClose(fis);
        }
        return bitmap;
    }

    @Override
    public boolean put(final String key, final Bitmap bitmap) {
        if ((key == null) || (bitmap == null)) {
            return false;
        }
        this.memoryCache.put(key, new SoftReference<Bitmap>(bitmap));
        final boolean result = writeToFile(key, bitmap);
        if (AppContext.DEBUG) {
            Log.d(ImageCache.TAG, "put() put to cache, write to disk result="
                    + result);
        }
        return result;
    }

    protected boolean replace(final String oldKey, final String key,
            final Bitmap bitmap) {
        boolean result = false;
        put(key, bitmap);
        synchronized (this) {
            result = this.memoryCache.put(key,
                    new SoftReference<Bitmap>(bitmap)) != null;
            this.memoryCache.remove(oldKey);
        }
        this.mContext.deleteFile(StringHelper.md5(oldKey));
        return result;
    }

    private boolean writeToFile(final String key, final Bitmap bitmap) {
        final String filename = StringHelper.md5(key) + ".jpg";
        return ImageHelper.writeToFile(new File(this.mCacheDir, filename),
                bitmap);
    }

}
