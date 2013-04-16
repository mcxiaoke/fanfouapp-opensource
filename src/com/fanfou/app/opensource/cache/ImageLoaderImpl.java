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

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.http.SimpleClient;
import com.fanfou.app.opensource.util.ImageHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.09.23
 * @version 2.0 2011.09.27
 * @version 2.1 2011.11.04
 * @version 2.5 2011.11.23
 * @version 2.6 2011.11.28
 * @version 3.0 2011.11.29
 * @version 4.0 2011.12.02
 * @version 4.1 2011.12.06
 * @version 5.0 2011.12.08
 * @version 5.1 2011.12.09
 * @version 5.2 2011.12.13
 * 
 */
class ImageLoaderImpl implements ImageLoader {

    private final class Daemon extends Thread {

        @Override
        public void run() {
            while (true) {
                if (AppContext.DEBUG) {
                    Log.d(ImageLoader.TAG, "Daemon is running.");
                }
                try {
                    final Task task = ImageLoaderImpl.this.mTaskQueue.take();
                    download(task);
                    // final Worker worker = new Worker(task, mCache, mClient);
                    // mExecutorService.execute(worker);
                } catch (final InterruptedException e) {
                    if (AppContext.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public synchronized void start() {
            super.start();
            if (AppContext.DEBUG) {
                Log.d(ImageLoader.TAG, "Daemon is start.");
            }
        }

    }

    private class InnerHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {
            final String url = msg.getData().getString(ImageLoader.EXTRA_URL);
            final ImageView view = ImageLoaderImpl.this.mViewsMap.remove(url);
            if (AppContext.DEBUG) {
                Log.d(ImageLoader.TAG, "InnerHandler what=" + msg.what
                        + " url=" + url + " view=" + view);
            }
            switch (msg.what) {
            case MESSAGE_FINISH:
                final Bitmap bitmap = (Bitmap) msg.obj;
                if ((bitmap != null) && (view != null)) {
                    if (!ImageLoaderImpl.isExpired(view, url)) {
                        if (AppContext.DEBUG) {
                            Log.d(ImageLoader.TAG,
                                    "InnerHandler onFinish() url=" + url);
                        }
                        view.setImageBitmap(bitmap);
                    }
                }
                break;
            case MESSAGE_ERROR:
                break;
            default:
                break;
            }
        }
    }

    private static final class Task {
        public final String url;
        public final Handler handler;
        public final long timestamp;

        public Task(final String url, final Handler handler) {
            this.url = url;
            this.handler = handler;
            this.timestamp = System.nanoTime();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Task other = (Task) obj;
            if (this.url == null) {
                if (other.url != null) {
                    return false;
                }
            } else if (!this.url.equals(other.url)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result)
                    + ((this.url == null) ? 0 : this.url.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return new StringBuilder().append("[Task] url:").append(this.url)
                    .append(" handler:").append(this.handler)
                    .append(" timestamp:").append(this.timestamp).toString();
        }
    }

    private static final class TaskComparator implements Comparator<Task> {
        @Override
        public int compare(final Task t1, final Task t2) {
            if (t1.timestamp > t2.timestamp) {
                return -1;
            } else if (t1.timestamp < t2.timestamp) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @SuppressWarnings("unused")
    private static final class Worker implements Runnable {
        private final String url;
        private final Handler handler;
        private final ImageCache cache;
        private final SimpleClient conn;

        public Worker(final Task pair, final ImageCache cache,
                final SimpleClient conn) {
            this.url = pair.url;
            this.handler = pair.handler;
            this.cache = cache;
            this.conn = conn;
        }

        private void download() {
            Bitmap bitmap = this.cache.get(this.url);
            if (bitmap == null) {
                try {
                    bitmap = this.conn.getBitmap(this.url);
                } catch (final IOException e) {
                    Log.e(ImageLoader.TAG, "download error:" + e.getMessage());
                }
                if (bitmap != null) {
                    this.cache.put(this.url, bitmap);
                    if (AppContext.DEBUG) {
                        Log.d(ImageLoader.TAG, "download put bitmap to cache ");
                    }
                }
            }
            if (this.handler != null) {
                final Message message = this.handler.obtainMessage();
                if (bitmap != null) {
                    message.what = ImageLoader.MESSAGE_FINISH;
                    message.obj = bitmap;
                } else {
                    message.what = ImageLoader.MESSAGE_ERROR;
                }
                if (AppContext.DEBUG) {
                    Log.d(ImageLoader.TAG, "download send message bitmap= "
                            + bitmap);
                }
                message.getData().putString(ImageLoader.EXTRA_URL, this.url);
                this.handler.sendMessage(message);
            } else {
                if (AppContext.DEBUG) {
                    Log.d(ImageLoader.TAG, "download handle is null, bitmap= "
                            + bitmap);
                }
            }
        }

        @Override
        public void run() {
            download();
        }
    }

    private static boolean isExpired(final ImageView view, final String url) {
        if (view == null) {
            return true;
        }
        final String tag = (String) view.getTag();
        return (tag == null) || !tag.equals(url);
    }

    // private final ExecutorService mExecutorService;
    private final PriorityBlockingQueue<Task> mTaskQueue = new PriorityBlockingQueue<Task>(
            60, new TaskComparator());

    private final Map<String, ImageView> mViewsMap;

    private final ImageCache mCache;

    private final Handler mHandler;

    private final SimpleClient mClient;

    private final Thread mDaemon;

    public ImageLoaderImpl() {
        if (AppContext.DEBUG) {
            Log.d(ImageLoader.TAG, "ImageLoader new instance.");
        }
        // this.mExecutorService =
        // Executors.newFixedThreadPool(CORE_POOL_SIZE,new
        // NameCountThreadFactory());
        // this.mExecutorService = Executors.newSingleThreadExecutor(new
        // NameCountThreadFactory());
        this.mCache = ImageCache.getInstance();
        this.mViewsMap = new HashMap<String, ImageView>();
        this.mClient = new SimpleClient(AppContext.getAppContext());
        this.mHandler = new InnerHandler();
        this.mDaemon = new Daemon();
        this.mDaemon.start();
        // this.mExecutorService.execute(this);
    }

    private void addInnerTask(final String url, final ImageView view) {
        final Task task = new Task(url, this.mHandler);
        if (this.mTaskQueue.contains(task)) {
            return;
        }
        if (AppContext.DEBUG) {
            Log.d(ImageLoader.TAG, "addInnerTask url=" + url + " mHandler="
                    + this.mHandler);
        }
        this.mTaskQueue.add(task);
        this.mViewsMap.put(url, view);
    }

    private void addTask(final String url, final Handler handler) {
        final Task task = new Task(url, handler);
        if (this.mTaskQueue.contains(task)) {
            return;
        }
        if (AppContext.DEBUG) {
            Log.d(ImageLoader.TAG, "addTask url=" + url + " handler=" + handler);
        }
        this.mTaskQueue.add(task);
    }

    @Override
    public void clearCache() {
        this.mCache.clear();
    }

    @Override
    public void clearQueue() {
        this.mTaskQueue.clear();
        this.mViewsMap.clear();
    }

    @Override
    public void displayImage(final String url, final ImageView view,
            final int iconId) {
        if (TextUtils.isEmpty(url) || (view == null)) {
            return;
        }
        final Bitmap bitmap = this.mCache.get(url);
        if (bitmap == null) {
            view.setImageResource(iconId);
            addInnerTask(url, view);
        } else {
            view.setImageBitmap(ImageHelper.getRoundedCornerBitmap(bitmap, 6));
        }
    }

    private void download(final Task task) {
        final String url = task.url;
        final Handler handler = task.handler;
        Bitmap bitmap = this.mCache.get(url);
        if (bitmap == null) {
            try {
                bitmap = this.mClient.getBitmap(url);
            } catch (final Exception e) {
                Log.e(ImageLoader.TAG, "download error:" + e.getMessage());
            }
            if (bitmap != null) {
                this.mCache.put(url, bitmap);
                if (AppContext.DEBUG) {
                    Log.d(ImageLoader.TAG, "download put bitmap to cache ");
                }
            }
        }
        if (handler != null) {
            final Message message = handler.obtainMessage();
            message.getData().putString(ImageLoader.EXTRA_URL, url);
            message.what = bitmap == null ? ImageLoader.MESSAGE_ERROR
                    : ImageLoader.MESSAGE_FINISH;
            message.obj = bitmap;
            handler.sendMessage(message);
            if (AppContext.DEBUG) {
                Log.d(ImageLoader.TAG, "download handle can use, bitmap= "
                        + bitmap);
            }
        } else {
            if (AppContext.DEBUG) {
                Log.d(ImageLoader.TAG, "download handle is null, bitmap= "
                        + bitmap);
            }
        }
    }

    @Override
    public Bitmap getImage(final String key, final Handler handler) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        final Bitmap bitmap = this.mCache.get(key);
        if ((bitmap == null) && (handler != null)) {
            addTask(key, handler);
        }
        return bitmap;
    }

    @Override
    public void shutdown() {
        clearQueue();
        clearCache();
    }
}
