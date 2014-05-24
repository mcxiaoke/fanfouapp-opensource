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
package com.fanfou.app.opensource.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.RemoteViews;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.NewVersionPage;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.http.SimpleClient;
import com.fanfou.app.opensource.update.AppVersionInfo;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.IOHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.09.04
 * @version 2.0 2011.10.31
 * @version 2.1 2011.11.24
 * @version 2.2 2011.11.25
 * @version 2.3 2011.11.28
 * @version 2.4 2011.12.02
 * @version 2.5 2011.12.19
 * @version 2.6 2011.12.30
 * @version 2.7 2012.01.05
 * @version 2.8 2012.01.16
 * 
 */
public class DownloadService extends BaseIntentService {
    @SuppressLint("HandlerLeak")
    private class DownloadHandler extends Handler {
        private static final long UPDATE_TIME = 1500;
        private long lastTime = 0;

        @Override
        public void handleMessage(final Message msg) {
            if (AppContext.DEBUG) {
                log("DownloadHandler what=" + msg.what + " progress="
                        + msg.arg1);
            }
            if (DownloadService.MSG_PROGRESS == msg.what) {
                final long now = System.currentTimeMillis();
                if ((now - this.lastTime) < DownloadHandler.UPDATE_TIME) {
                    return;
                }
                final int progress = msg.arg1;
                updateProgress(progress);
                this.lastTime = System.currentTimeMillis();
            } else if (DownloadService.MSG_SUCCESS == msg.what) {
                DownloadService.this.nm
                        .cancel(DownloadService.NOTIFICATION_PROGRESS_ID);
                final String filePath = msg.getData().getString(
                        Constants.EXTRA_FILENAME);
                CommonHelper.open(DownloadService.this, filePath);
            }
        }
    }

    private static final String TAG = DownloadService.class.getSimpleName();

    public static final String UPDATE_VERSION_FILE = "http://apps.fanfou.com/android/update.json";
    private static final int NOTIFICATION_PROGRESS_ID = -12345;
    public static final long AUTO_UPDATE_INTERVAL = 2 * 3600 * 1000L;
    private NotificationManager nm;
    private Notification notification;

    private Handler mHandler;
    public static final int TYPE_CHECK = 0;

    public static final int TYPE_DOWNLOAD = 1;

    private static final int MSG_PROGRESS = 0;

    private static final int MSG_SUCCESS = 1;

    public static AppVersionInfo fetchVersionInfo() {
        final SimpleClient client = new SimpleClient(AppContext.getAppContext());
        try {
            final HttpResponse response = client
                    .get(DownloadService.UPDATE_VERSION_FILE);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (AppContext.DEBUG) {
                Log.d(DownloadService.TAG, "statusCode=" + statusCode);
            }
            if (statusCode == 200) {
                final String content = EntityUtils.toString(
                        response.getEntity(), HTTP.UTF_8);
                if (AppContext.DEBUG) {
                    Log.d(DownloadService.TAG, "response=" + content);
                }
                return AppVersionInfo.parse(content);
            }
        } catch (final IOException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        } finally {
        }
        return null;
    }

    public static Intent getNewVersionIntent(final Context context,
            final AppVersionInfo info) {
        final Intent intent = new Intent(context, NewVersionPage.class);
        intent.putExtra(Constants.EXTRA_DATA, info);
        return intent;
    }

    private final static PendingIntent getPendingIntent(final Context context) {
        final Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.EXTRA_TYPE, DownloadService.TYPE_CHECK);
        final PendingIntent pi = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }

    public static void notifyUpdate(final AppVersionInfo info,
            final Context context) {
        final String versionInfo = info.versionName;
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification notification = new Notification(
                R.drawable.ic_notify_icon, "饭否客户端有新版本：" + versionInfo,
                System.currentTimeMillis());

        final PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, DownloadService.getNewVersionIntent(context, info), 0);
        notification.setLatestEventInfo(context, "饭否客户端有新版本：" + versionInfo,
                "点击查看更新内容", contentIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        nm.notify(2, notification);

    }

    public static void showUpdateConfirmDialog(final Context context,
            final AppVersionInfo info) {
        final DialogInterface.OnClickListener downListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                DownloadService.startDownload(context, info);
            }
        };
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("发现新版本，是否升级？").setCancelable(true)
                .setNegativeButton("以后再说", null);
        builder.setPositiveButton("立即升级", downListener);
        final StringBuffer sb = new StringBuffer();
        sb.append("安装版本：").append(AppContext.appVersionName).append("(Build")
                .append(AppContext.appVersionCode).append(")");
        sb.append("\n最新版本：").append(info.versionName).append("(Build")
                .append(info.versionCode).append(")");
        sb.append("\n更新日期：").append(info.releaseDate);
        sb.append("\n更新级别：").append(info.forceUpdate ? "重要升级" : "一般升级");
        sb.append("\n更新内容：\n").append(info.changelog);
        builder.setMessage(sb.toString());
        final AlertDialog dialog = builder.create();
        dialog.show();

    }

    public static void startDownload(final Context context,
            final AppVersionInfo info) {
        final Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.EXTRA_TYPE, DownloadService.TYPE_DOWNLOAD);
        intent.putExtra(Constants.EXTRA_URL, info);
        context.startService(intent);
    }

    public static void unset(final Context context) {
        final AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        am.cancel(DownloadService.getPendingIntent(context));
        if (AppContext.DEBUG) {
            Log.d(DownloadService.TAG, "unset");
        }
    }

    public DownloadService() {
        super("DownloadService");

    }

    private void check() {
        final AppVersionInfo info = DownloadService.fetchVersionInfo();
        if (AppContext.DEBUG) {
            Log.d(DownloadService.TAG, "check() VersionInfo=" + info);
            if (info != null) {
                DownloadService.notifyUpdate(info, this);
                return;
            }
        }
        if ((info != null) && (info.versionCode > AppContext.appVersionCode)) {
            DownloadService.notifyUpdate(info, this);
        }
    }

    private void download(final AppVersionInfo info) {
        if ((info == null) || TextUtils.isEmpty(info.downloadUrl)) {
            return;
        }
        showProgress();
        final String url = info.downloadUrl;
        final int versionCode = info.versionCode;
        if (AppContext.DEBUG) {
            Log.v(DownloadService.TAG, "download apk file url: " + url
                    + " versionCode:" + versionCode);
        }
        InputStream is = null;
        BufferedOutputStream bos = null;
        final SimpleClient client = new SimpleClient(AppContext.getAppContext());
        boolean needDownload = true;
        final File file = new File(IOHelper.getDownloadDir(this), "fanfouapp_"
                + versionCode + ".apk");
        try {
            final HttpResponse response = client.get(url);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                final HttpEntity entity = response.getEntity();
                final long totalSize = entity.getContentLength();
                long download = 0;

                if (file.exists() && file.isFile()
                        && (file.length() == totalSize)) {
                    needDownload = false;
                    download = totalSize;
                    if (AppContext.DEBUG) {
                        Log.v(DownloadService.TAG,
                                "download apk file is already exists: "
                                        + file.getAbsolutePath());
                    }
                }

                if (needDownload) {
                    is = entity.getContent();
                    bos = new BufferedOutputStream(new FileOutputStream(file));
                    final byte[] buffer = new byte[8196];
                    int read = -1;
                    while ((read = is.read(buffer)) != -1) {
                        bos.write(buffer, 0, read);
                        download += read;
                        final int progress = (int) ((100.0 * download) / totalSize);
                        sendProgressMessage(progress);
                        if (AppContext.DEBUG) {
                            log("progress=" + progress);
                        }
                    }
                    bos.flush();
                }
                if (download >= totalSize) {
                    sendSuccessMessage(file);
                }
            }
        } catch (final IOException e) {
            if (AppContext.DEBUG) {
                Log.e(DownloadService.TAG, "download error: " + e.getMessage());
                e.printStackTrace();
            }

        } finally {
            this.nm.cancel(DownloadService.NOTIFICATION_PROGRESS_ID);
            IOHelper.forceClose(is);
            IOHelper.forceClose(bos);
        }
    }

    @SuppressWarnings("unused")
    private PendingIntent getInstallPendingIntent(final String fileName) {
        final String mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(CommonHelper.getExtension(fileName));
        if (mimeType != null) {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(fileName)), mimeType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            final PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                    0);
            return pi;
        }
        return null;
    }

    private void log(final String message) {
        Log.d("DownloadService", message);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mHandler = new DownloadHandler();
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        this.nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final int type = intent.getIntExtra(Constants.EXTRA_TYPE,
                DownloadService.TYPE_CHECK);
        if (AppContext.DEBUG) {
            Log.d(DownloadService.TAG, "onHandleIntent type=" + type);
        }
        if (type == DownloadService.TYPE_CHECK) {
            check();
        } else if (type == DownloadService.TYPE_DOWNLOAD) {
            final AppVersionInfo info = intent
                    .getParcelableExtra(Constants.EXTRA_URL);
            log("onHandleIntent TYPE_DOWNLOAD info=" + info);
            if ((info != null) && !TextUtils.isEmpty(info.downloadUrl)) {
                download(info);
            }
        }
    }

    private void sendProgressMessage(final int progress) {
        final Message message = new Message();
        message.what = DownloadService.MSG_PROGRESS;
        message.arg1 = progress;
        this.mHandler.sendMessage(message);
    }

    private void sendSuccessMessage(final File file) {
        final Message message = new Message();
        message.what = DownloadService.MSG_SUCCESS;
        message.getData().putString(Constants.EXTRA_FILENAME,
                file.getAbsolutePath());
        this.mHandler.sendMessage(message);
    }

    private void showProgress() {
        this.notification = new Notification(R.drawable.ic_notify_download,
                "正在下载饭否客户端", System.currentTimeMillis());
        this.notification.flags |= Notification.FLAG_ONGOING_EVENT;
        this.notification.flags |= Notification.FLAG_AUTO_CANCEL;
        this.notification.contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(), 0);
        final RemoteViews view = new RemoteViews(getPackageName(),
                R.layout.download_notification);
        view.setTextViewText(R.id.download_notification_text, "正在下载饭否客户端 0%");
        view.setProgressBar(R.id.download_notification_progress, 100, 0, false);
        this.notification.contentView = view;
        this.nm.notify(DownloadService.NOTIFICATION_PROGRESS_ID,
                this.notification);
    }

    private void updateProgress(final int progress) {
        final RemoteViews view = new RemoteViews(getPackageName(),
                R.layout.download_notification);
        view.setTextViewText(R.id.download_notification_text, "正在下载饭否客户端 "
                + progress + "%");
        view.setInt(R.id.download_notification_progress, "setProgress",
                progress);
        this.notification.contentView = view;
        this.nm.notify(DownloadService.NOTIFICATION_PROGRESS_ID,
                this.notification);
    }

}
