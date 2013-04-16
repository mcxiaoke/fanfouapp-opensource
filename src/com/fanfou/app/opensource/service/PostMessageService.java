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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.LoginPage;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.ApiClient;
import com.fanfou.app.opensource.api.ApiException;
import com.fanfou.app.opensource.api.bean.DirectMessage;
import com.fanfou.app.opensource.util.IOHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.25
 * @version 1.1 2011.10.25
 * @version 2.0 2011.11.18
 * @version 2.1 2011.11.21
 * @version 2.2 2011.12.13
 * 
 */
public class PostMessageService extends BaseIntentService {

    private static final String TAG = PostMessageService.class.getSimpleName();
    private NotificationManager nm;
    private String content;

    private String userId;
    private String userName;

    public PostMessageService() {
        super("UpdateService");

    }

    private boolean doSend() {
        showSendingNotification();
        boolean res = true;
        final ApiClient api = AppContext.getApiClient();
        try {
            final DirectMessage result = api.directMessagesCreate(this.userId,
                    this.content, null, Constants.MODE);
            this.nm.cancel(10);
            if ((result == null) || result.isNull()) {
                IOHelper.copyToClipBoard(this, this.content);
                showFailedNotification("私信未发送，内容已保存到剪贴板", "未知原因");
                res = false;
            } else {
                IOHelper.storeDirectMessage(this, result);
                res = true;
                sendSuccessBroadcast();
            }
        } catch (final ApiException e) {
            this.nm.cancel(10);
            if (AppContext.DEBUG) {
                Log.e(PostMessageService.TAG, "error: code=" + e.statusCode
                        + " msg=" + e.getMessage());
            }
            IOHelper.copyToClipBoard(this, this.content);
            if (e.statusCode >= 500) {
                showFailedNotification("私信未发送，内容已保存到剪贴板",
                        getString(R.string.msg_server_error));
            } else {
                showFailedNotification("私信未发送，内容已保存到剪贴板",
                        getString(R.string.msg_connection_error));
            }
        } finally {
            this.nm.cancel(12);
        }
        return res;
    }

    public void log(final String message) {
        Log.i(PostMessageService.TAG, message);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (intent == null) {
            return;
        }
        log("intent=" + intent);
        parseIntent(intent);
        this.nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        doSend();
    }

    private void parseIntent(final Intent intent) {
        this.userId = intent.getStringExtra(Constants.EXTRA_ID);
        this.userName = intent.getStringExtra(Constants.EXTRA_USER_NAME);
        this.content = intent.getStringExtra(Constants.EXTRA_TEXT);
        if (AppContext.DEBUG) {
            log("parseIntent userId=" + this.userId);
            log("parseIntent userName=" + this.userName);
            log("parseIntent content=" + this.content);
        }
    }

    private void sendSuccessBroadcast() {
        final Intent intent = new Intent(Constants.ACTION_MESSAGE_SENT);
        intent.setPackage(getPackageName());
        sendOrderedBroadcast(intent, null);
    }

    private int showFailedNotification(final String title, final String message) {
        final int id = 11;

        final Notification notification = new Notification(
                R.drawable.ic_notify_icon, title, System.currentTimeMillis());
        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(), 0);
        notification.setLatestEventInfo(this, title, message, contentIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        this.nm.notify(id, notification);
        return id;

    }

    private int showSendingNotification() {
        final int id = 10;
        final Notification notification = new Notification(
                R.drawable.ic_notify_icon, "饭否私信正在发送...",
                System.currentTimeMillis());
        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(), 0);
        notification.setLatestEventInfo(this, "饭否私信", "正在发送...", contentIntent);
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        this.nm.notify(id, notification);
        return id;
    }

    @SuppressWarnings("unused")
    private int showSuccessNotification() {
        final int id = 12;
        final Notification notification = new Notification(
                R.drawable.ic_notify_icon, "私信发送成功", System.currentTimeMillis());
        final Intent intent = new Intent(this, LoginPage.class);
        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0);
        notification.setLatestEventInfo(this, "饭否私信", "私信发送成功", contentIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        this.nm.notify(id, notification);
        return id;
    }

}
