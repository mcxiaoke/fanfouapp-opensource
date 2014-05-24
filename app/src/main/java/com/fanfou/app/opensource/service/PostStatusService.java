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

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.DraftsPage;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.WritePage;
import com.fanfou.app.opensource.api.ApiClient;
import com.fanfou.app.opensource.api.ApiException;
import com.fanfou.app.opensource.api.bean.Draft;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.db.Contents.DraftInfo;
import com.fanfou.app.opensource.util.ImageHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.10
 * @version 1.1 2011.10.25
 * @version 2.0 2011.10.27
 * @version 2.1 2011.10.28
 * @version 2.2 2011.11.02
 * @version 3.0 2011.11.18
 * @version 3.1 2011.11.28
 * @version 3.2 2011.12.05
 * @version 3.3 2011.12.13
 * @version 3.4 2011.12.26
 * 
 */
public class PostStatusService extends BaseIntentService {

    private static final String TAG = PostStatusService.class.getSimpleName();
    private NotificationManager nm;

    private String text;

    private File srcFile;
    private String location;
    private String relationId;
    private int type;

    public PostStatusService() {
        super("UpdateService");

    }

    private void doSaveDrafts() {
        final Draft d = new Draft();
        d.text = this.text;
        d.filePath = this.srcFile == null ? "" : this.srcFile.getPath();
        d.replyTo = this.relationId;
        d.type = this.type;
        final Uri resultUri = getContentResolver().insert(
                DraftInfo.CONTENT_URI, d.toContentValues());
        if (AppContext.DEBUG) {
            log("doSaveDrafts resultUri=" + resultUri + " type=" + d.type
                    + " text=" + d.text + " filepath=" + d.filePath);
        }
    }

    private boolean doSend() {
        showSendingNotification();
        boolean res = false;
        final ApiClient api = AppContext.getApiClient();
        try {
            Status result = null;
            if ((this.srcFile == null) || !this.srcFile.exists()) {
                if (this.type == WritePage.TYPE_REPLY) {
                    result = api.statusesCreate(this.text, this.relationId,
                            null, this.location, null, Constants.FORMAT,
                            Constants.MODE);
                } else {
                    result = api.statusesCreate(this.text, null, null,
                            this.location, this.relationId, Constants.FORMAT,
                            Constants.MODE);
                }
            } else {
                int quality;
                if (AppContext.isWifi()) {
                    quality = ImageHelper.IMAGE_QUALITY_HIGH;
                } else {
                    quality = ImageHelper.IMAGE_QUALITY_LOW;
                }
                final File photo = ImageHelper.prepareUploadFile(this,
                        this.srcFile, quality);
                if ((photo != null) && (photo.length() > 0)) {
                    if (AppContext.DEBUG) {
                        log("photo file=" + this.srcFile.getName() + " size="
                                + (photo.length() / 1024) + " quality="
                                + quality);
                    }
                    result = api.photosUpload(photo, this.text, null,
                            this.location, Constants.FORMAT, Constants.MODE);
                    photo.delete();
                }

            }
            this.nm.cancel(0);
            if ((result != null) && !result.isNull()) {
                res = true;
            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                Log.e(PostStatusService.TAG, e.toString());
                e.printStackTrace();
            }
            if (e.statusCode >= 500) {
                showFailedNotification("消息未发送，已保存到草稿箱",
                        getString(R.string.msg_server_error));
            } else {
                showFailedNotification("消息未发送，已保存到草稿箱",
                        getString(R.string.msg_connection_error));
            }

        } finally {
            this.nm.cancel(0);
        }
        return res;
    }

    public void log(final String message) {
        Log.d(PostStatusService.TAG, message);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (intent == null) {
            return;
        }
        if (AppContext.DEBUG) {
            log("intent=" + intent);
        }
        this.nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        parseIntent(intent);
        if (doSend()) {
            sendSuccessBroadcast();
        }
    }

    private void parseIntent(final Intent intent) {
        this.type = intent.getIntExtra(Constants.EXTRA_TYPE,
                WritePage.TYPE_NORMAL);
        this.text = intent.getStringExtra(Constants.EXTRA_TEXT);
        this.srcFile = (File) intent.getSerializableExtra(Constants.EXTRA_DATA);
        this.relationId = intent.getStringExtra(Constants.EXTRA_IN_REPLY_TO_ID);
        this.location = intent.getStringExtra(Constants.EXTRA_LOCATION);
        if (AppContext.DEBUG) {
            log("location="
                    + (StringHelper.isEmpty(this.location) ? "null"
                            : this.location));
        }
    }

    private void sendSuccessBroadcast() {
        final Intent intent = new Intent(Constants.ACTION_STATUS_SENT);
        intent.setPackage(getPackageName());
        sendOrderedBroadcast(intent, null);
    }

    private int showFailedNotification(final String title, final String message) {
        doSaveDrafts();
        final int id = 1;
        final Notification notification = new Notification(
                R.drawable.ic_notify_icon, title, System.currentTimeMillis());
        final Intent intent = new Intent(this, DraftsPage.class);
        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(this, title, message, contentIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        this.nm.notify(id, notification);
        return id;
    }

    private int showSendingNotification() {
        final int id = 0;
        final Notification notification = new Notification(
                R.drawable.ic_notify_icon, "饭否消息正在发送...",
                System.currentTimeMillis());
        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(), 0);
        notification.setLatestEventInfo(this, "饭否消息", "正在发送...", contentIntent);
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        this.nm.notify(id, notification);
        return id;
    }

}
