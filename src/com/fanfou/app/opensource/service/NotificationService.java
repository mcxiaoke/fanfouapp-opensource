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

import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.ApiClient;
import com.fanfou.app.opensource.api.ApiException;
import com.fanfou.app.opensource.api.ApiParser;
import com.fanfou.app.opensource.api.bean.DirectMessage;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.db.Contents.BasicColumns;
import com.fanfou.app.opensource.db.Contents.DirectMessageInfo;
import com.fanfou.app.opensource.db.Contents.StatusInfo;
import com.fanfou.app.opensource.db.FanFouProvider;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.DateTimeHelper;
import com.fanfou.app.opensource.util.IntentHelper;
import com.fanfou.app.opensource.util.OptionHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.09.20
 * @version 1.1 2011.11.02
 * @version 1.2 2011.11.03
 * @version 1.3 2011.11.04
 * @version 2.0 2011.11.18
 * @version 2.1 2011.11.23
 * @version 2.2 2011.11.25
 * @version 2.5 2011.12.02
 * @version 2.6 2011.12.09
 * @version 2.7 2011.12.19
 * @version 2.8 2011.12.23
 * @version 2.9 2011.12.26
 * @version 3.0 2011.12.27
 * @version 3.1 2011.12.30
 * @version 3.2 2012.01.16
 * 
 */
public class NotificationService extends WakefulIntentService {
    private static final String TAG = NotificationService.class.getSimpleName();

    public static final int NOTIFICATION_TYPE_HOME = Constants.TYPE_STATUSES_HOME_TIMELINE;
    public static final int NOTIFICATION_TYPE_MENTION = Constants.TYPE_STATUSES_MENTIONS; // @消息
    public static final int NOTIFICATION_TYPE_DM = Constants.TYPE_DIRECT_MESSAGES_INBOX; // 私信

    private static final int DEFAULT_COUNT = Constants.DEFAULT_TIMELINE_COUNT;
    private static final int MAX_COUNT = Constants.MAX_TIMELINE_COUNT;
    private static final int DEFAULT_PAGE = 0;

    private final static PendingIntent getPendingIntent(final Context context) {
        final Intent intent = new Intent(context, NotificationService.class);
        final PendingIntent pi = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }

    public static void set(final Context context) {
        final boolean need = OptionHelper.readBoolean(context,
                R.string.option_notification, true);
        if (!need) {
            return;
        }
        final int interval = OptionHelper.parseInt(context,
                R.string.option_notification_interval, "5");
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, interval);
        final AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                NotificationService.getPendingIntent(context));

        if (AppContext.DEBUG) {
            Log.d(NotificationService.TAG, "set interval=" + interval
                    + " next time=" + DateTimeHelper.formatDate(c.getTime()));
        }
    }

    public static void set(final Context context, final boolean set) {
        if (set) {
            NotificationService.set(context);
        } else {
            NotificationService.unset(context);
        }
    }

    public static void setIfNot(final Context context) {
        final boolean set = OptionHelper.readBoolean(context,
                R.string.option_set_notification, false);
        if (AppContext.DEBUG) {
            Log.d(NotificationService.TAG, "setIfNot flag=" + set);
        }
        if (!set) {
            OptionHelper.saveBoolean(context, R.string.option_set_notification,
                    true);
            NotificationService.set(context);
        }
    }

    public static void unset(final Context context) {
        final AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        am.cancel(NotificationService.getPendingIntent(context));
        if (AppContext.DEBUG) {
            Log.d(NotificationService.TAG, "unset");
        }
    }

    private ApiClient mApi;

    public NotificationService() {
        super("NotificationService");
    }

    private void broadcast(final Intent intent) {
        intent.setPackage(getPackageName());
        sendOrderedBroadcast(intent, null);
        if (AppContext.DEBUG) {
            Log.d(NotificationService.TAG, "broadcast() ");
            IntentHelper.logIntent(NotificationService.TAG, intent);
        }
    }

    @Override
    protected void doWakefulWork(final Intent intent) {
        final boolean dm = OptionHelper.readBoolean(this,
                R.string.option_notification_dm, true);
        final boolean mention = OptionHelper.readBoolean(this,
                R.string.option_notification_mention, true);
        final boolean home = OptionHelper.readBoolean(this,
                R.string.option_notification_home, false);

        int count = NotificationService.DEFAULT_COUNT;
        if (AppContext.isWifi()) {
            count = NotificationService.MAX_COUNT;
        }
        if (dm) {
            handleDm(count);
        }
        if (mention) {
            handleMention(count);
        }
        if (home) {
            handleHome(count);
        }
        NotificationService.set(this);
    }

    private void handleDm(final int count) {
        final Cursor mc = initCursor(Constants.TYPE_DIRECT_MESSAGES_INBOX);
        List<DirectMessage> dms = null;
        try {
            dms = this.mApi.directMessagesInbox(count,
                    NotificationService.DEFAULT_PAGE,
                    CommonHelper.getDmSinceId(mc), null, Constants.MODE);
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                Log.e(NotificationService.TAG, " code=" + e.statusCode
                        + " message=" + e.getMessage());
            }
        } catch (final Exception e) {
            if (AppContext.DEBUG) {
                Log.e(NotificationService.TAG, e.getMessage());
            }
        }
        mc.close();
        if (dms != null) {
            final int size = dms.size();
            if (size > 0) {
                if (AppContext.DEBUG) {
                    Log.d(NotificationService.TAG, "handleDm() size=" + size);
                }
                if (size == 1) {
                    final DirectMessage dm = dms.get(0);
                    getContentResolver().insert(DirectMessageInfo.CONTENT_URI,
                            dm.toContentValues());
                    notifyDmOne(NotificationService.NOTIFICATION_TYPE_DM,
                            dms.get(0));
                } else {
                    getContentResolver().bulkInsert(
                            DirectMessageInfo.CONTENT_URI,
                            ApiParser.toContentValuesArray(dms));
                    notifyDmList(NotificationService.NOTIFICATION_TYPE_DM, size);
                }
                getContentResolver().notifyChange(
                        DirectMessageInfo.CONTENT_URI, null, false);
            }
        }

    }

    private void handleHome(final int count) {
        final Cursor mc = initCursor(Constants.TYPE_STATUSES_HOME_TIMELINE);
        List<Status> ss = null;
        try {
            ss = this.mApi.homeTimeline(count,
                    NotificationService.DEFAULT_PAGE,
                    CommonHelper.getSinceId(mc), null, Constants.FORMAT,
                    Constants.MODE);
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                Log.e(NotificationService.TAG, " code=" + e.statusCode
                        + " message=" + e.getMessage());
            }
        } catch (final Exception e) {
            if (AppContext.DEBUG) {
                Log.e(NotificationService.TAG, e.getMessage());
            }
        }
        mc.close();
        if (ss != null) {
            final int size = ss.size();
            if (size > 0) {
                if (AppContext.DEBUG) {
                    Log.d(NotificationService.TAG, "handleHome() size=" + size);
                }
                if (size == 1) {
                    final Status s = ss.get(0);
                    getContentResolver().insert(StatusInfo.CONTENT_URI,
                            s.toContentValues());
                    notifyStatusOne(NotificationService.NOTIFICATION_TYPE_HOME,
                            ss.get(0));
                } else {
                    getContentResolver().bulkInsert(StatusInfo.CONTENT_URI,
                            ApiParser.toContentValuesArray(ss));
                    notifyStatusList(
                            NotificationService.NOTIFICATION_TYPE_HOME, size);
                }
                getContentResolver().notifyChange(StatusInfo.CONTENT_URI, null,
                        false);
            }
        }

    }

    private void handleMention(final int count) {
        final Cursor mc = initCursor(Constants.TYPE_STATUSES_MENTIONS);
        List<Status> ss = null;
        try {
            ss = this.mApi.mentions(count, NotificationService.DEFAULT_PAGE,
                    CommonHelper.getSinceId(mc), null, Constants.FORMAT,
                    Constants.MODE);
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                Log.e(NotificationService.TAG, " code=" + e.statusCode
                        + " message=" + e.getMessage());
            }
        } catch (final Exception e) {
            if (AppContext.DEBUG) {
                Log.e(NotificationService.TAG, e.getMessage());
            }
        }
        mc.close();
        if (ss != null) {
            final int size = ss.size();
            if (size > 0) {
                if (AppContext.DEBUG) {
                    Log.d(NotificationService.TAG, "handleMention() size="
                            + size);
                }
                if (size == 1) {
                    final Status s = ss.get(0);
                    getContentResolver().insert(StatusInfo.CONTENT_URI,
                            s.toContentValues());
                    notifyStatusOne(
                            NotificationService.NOTIFICATION_TYPE_MENTION, s);
                } else {
                    getContentResolver().bulkInsert(StatusInfo.CONTENT_URI,
                            ApiParser.toContentValuesArray(ss));
                    notifyStatusList(
                            NotificationService.NOTIFICATION_TYPE_MENTION, size);
                }
                getContentResolver().notifyChange(StatusInfo.CONTENT_URI, null,
                        false);
            }
        }

    }

    private Cursor initCursor(final int type) {
        final String where = BasicColumns.TYPE + " =? ";
        final String[] whereArgs = new String[] { String.valueOf(type) };
        Uri uri = StatusInfo.CONTENT_URI;
        String[] columns = StatusInfo.COLUMNS;
        final String orderBy = FanFouProvider.ORDERBY_DATE_DESC;
        if (type == Constants.TYPE_DIRECT_MESSAGES_INBOX) {
            uri = DirectMessageInfo.CONTENT_URI;
            columns = DirectMessageInfo.COLUMNS;
        }
        return getContentResolver().query(uri, columns, where, whereArgs,
                orderBy);
    }

    private void notifyDmList(final int type, final int count) {
        sendMessageNotification(type, count, null);
    }

    private void notifyDmOne(final int type, final DirectMessage dm) {
        sendMessageNotification(type, 1, dm);
    }

    private void notifyStatusList(final int type, final int count) {
        sendStatusNotification(type, count, null);
    }

    private void notifyStatusOne(final int type, final Status status) {
        sendStatusNotification(type, 1, status);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mApi = AppContext.getApiClient();
    }

    private void sendMessageNotification(final int type, final int count,
            final DirectMessage dm) {
        if (AppContext.DEBUG) {
            Log.d(NotificationService.TAG, "sendMessageNotification type="
                    + type + " count=" + count + " dm=" + dm);
        }
        final Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_TYPE, type);
        intent.putExtra(Constants.EXTRA_COUNT, count);
        intent.putExtra(Constants.EXTRA_DATA, dm);
        intent.setAction(Constants.ACTION_NOTIFICATION);
        broadcast(intent);
    }

    private void sendStatusNotification(final int type, final int count,
            final Status status) {
        if (AppContext.DEBUG) {
            Log.d(NotificationService.TAG, "sendStatusNotification type="
                    + type + " count=" + count + " status="
                    + (status == null ? "null" : status) + " active="
                    + AppContext.active);
        }
        final Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_TYPE, type);
        intent.putExtra(Constants.EXTRA_COUNT, count);
        intent.putExtra(Constants.EXTRA_DATA, status);
        intent.setAction(Constants.ACTION_NOTIFICATION);
        broadcast(intent);
    }

}
