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
package com.fanfou.app.opensource.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.HomePage;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.SendPage;
import com.fanfou.app.opensource.StatusPage;
import com.fanfou.app.opensource.api.bean.DirectMessage;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.service.NotificationService;
import com.fanfou.app.opensource.util.AlarmHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.09.21
 * @version 1.1 2011.11.03
 * 
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID_DM = -101;
    private static final int NOTIFICATION_ID_MENTION = -102;
    private static final int NOTIFICATION_ID_HOME = -103;
    private static final String TAG = NotificationReceiver.class
            .getSimpleName();

    private static void showDmMoreNotification(final Context context,
            final int count) {
        if (AppContext.DEBUG) {
            Log.d(NotificationReceiver.TAG, "showDmMoreNotification count="
                    + count);
        }
        final String title = "饭否私信";
        final String message = "收到" + count + "条发给你的私信";
        final Intent intent = new Intent(context, HomePage.class);
        intent.setAction("DUMY_ACTION " + System.currentTimeMillis());
        intent.putExtra(Constants.EXTRA_PAGE, 2);
        final PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, intent, 0);
        NotificationReceiver.showNotification(
                NotificationReceiver.NOTIFICATION_ID_DM, context,
                contentIntent, title, message, R.drawable.ic_notify_dm);

    }

    private static void showDmOneNotification(final Context context,
            final DirectMessage dm) {
        if (AppContext.DEBUG) {
            Log.d(NotificationReceiver.TAG, "showDmOneNotification " + dm);
        }
        final Intent intent = new Intent(context, SendPage.class);
        intent.setAction("DUMY_ACTION " + System.currentTimeMillis());
        intent.putExtra(Constants.EXTRA_ID, dm.senderId);
        intent.putExtra(Constants.EXTRA_USER_NAME, dm.senderScreenName);
        final PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final String title = "收到" + dm.senderScreenName + "的私信";
        final String message = dm.senderScreenName + ":" + dm.text;
        NotificationReceiver.showNotification(
                NotificationReceiver.NOTIFICATION_ID_DM, context,
                contentIntent, title, message, R.drawable.ic_notify_dm);
    }

    private static void showHomeMoreNotification(final Context context,
            final int count) {
        if (AppContext.DEBUG) {
            Log.d(NotificationReceiver.TAG, "showHomeMoreNotification  count="
                    + count);
        }
        final String title = "饭否消息";
        final String message = "收到" + count + "条来自好友的消息";
        final Intent intent = new Intent(context, HomePage.class);
        intent.setAction("DUMY_ACTION " + System.currentTimeMillis());
        intent.putExtra(Constants.EXTRA_PAGE, 0);
        final PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, intent, 0);
        NotificationReceiver.showNotification(
                NotificationReceiver.NOTIFICATION_ID_HOME, context,
                contentIntent, title, message, R.drawable.ic_notify_home);
    }

    private static void showHomeOneNotification(final Context context,
            final Status status) {
        if (AppContext.DEBUG) {
            Log.d(NotificationReceiver.TAG, "showHomeOneNotification " + status);
        }
        final String title = status.userScreenName;
        final String message = status.simpleText;
        final Intent intent = new Intent(context, StatusPage.class);
        intent.setAction("DUMY_ACTION " + System.currentTimeMillis());
        intent.putExtra(Constants.EXTRA_DATA, status);
        final PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationReceiver.showNotification(
                NotificationReceiver.NOTIFICATION_ID_HOME, context,
                contentIntent, title, message, R.drawable.ic_notify_home);
    }

    private static void showMentionMoreNotification(final Context context,
            final int count) {
        if (AppContext.DEBUG) {
            Log.d(NotificationReceiver.TAG,
                    "showMentionMoreNotification count=" + count);
        }
        final String title = "饭否消息";
        final String message = "收到" + count + "条提到你的消息";
        final Intent intent = new Intent(context, HomePage.class);
        intent.setAction("DUMY_ACTION " + System.currentTimeMillis());
        intent.putExtra(Constants.EXTRA_PAGE, 1);
        final PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, intent, 0);
        NotificationReceiver.showNotification(
                NotificationReceiver.NOTIFICATION_ID_MENTION, context,
                contentIntent, title, message, R.drawable.ic_notify_mention);
    }

    private static void showMentionOneNotification(final Context context,
            final Status status) {
        if (AppContext.DEBUG) {
            Log.i(NotificationReceiver.TAG, "showMentionOneNotification "
                    + status);
        }
        final String title = status.userScreenName + "@你的消息";
        final String message = status.simpleText;
        final Intent intent = new Intent(context, StatusPage.class);
        intent.setAction("DUMY_ACTION " + System.currentTimeMillis());
        intent.putExtra(Constants.EXTRA_DATA, status);
        final PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationReceiver.showNotification(
                NotificationReceiver.NOTIFICATION_ID_MENTION, context,
                contentIntent, title, message, R.drawable.ic_notify_mention);
    }

    private static void showNotification(final int notificationId,
            final Context context, final PendingIntent contentIntent,
            final String title, final String message, final int iconId) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification notification = new Notification(iconId, title,
                System.currentTimeMillis());
        notification.setLatestEventInfo(context, title, message, contentIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        AlarmHelper.setNotificationType(context, notification);
        nm.notify(notificationId, notification);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final int type = intent.getIntExtra(Constants.EXTRA_TYPE, -1);
        final int count = intent.getIntExtra(Constants.EXTRA_COUNT, 1);
        if (AppContext.DEBUG) {
            Log.d(NotificationReceiver.TAG, "broadcast received type=" + type
                    + " count=" + count);
        }
        switch (type) {
        case NotificationService.NOTIFICATION_TYPE_HOME:
            if (count == 1) {
                final Status status = (Status) intent
                        .getParcelableExtra(Constants.EXTRA_DATA);
                if (status != null) {
                    NotificationReceiver.showHomeOneNotification(context,
                            status);
                }
            } else {
                NotificationReceiver.showHomeMoreNotification(context, count);
            }
            break;
        case NotificationService.NOTIFICATION_TYPE_MENTION:
            if (count == 1) {
                final Status status = (Status) intent
                        .getParcelableExtra(Constants.EXTRA_DATA);
                if (status != null) {
                    NotificationReceiver.showMentionOneNotification(context,
                            status);
                }
            } else {
                NotificationReceiver
                        .showMentionMoreNotification(context, count);
            }
            break;
        case NotificationService.NOTIFICATION_TYPE_DM:
            if (count == 1) {
                final DirectMessage dm = (DirectMessage) intent
                        .getParcelableExtra(Constants.EXTRA_DATA);
                if (dm != null) {
                    NotificationReceiver.showDmOneNotification(context, dm);
                }
            } else {
                NotificationReceiver.showDmMoreNotification(context, count);
            }
            break;
        default:
            break;
        }
    }

}
