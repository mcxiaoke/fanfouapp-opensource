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
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.ApiClient;
import com.fanfou.app.opensource.api.ApiParser;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.db.Contents.UserInfo;
import com.fanfou.app.opensource.util.DateTimeHelper;
import com.fanfou.app.opensource.util.OptionHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.10
 * @version 1.1 2011.11.17
 * @version 2.0 2011.11.18
 * @version 2.1 2011.11.24
 * @version 2.2 2011.11.28
 * @version 2.3 2011.11.29
 * @version 2.4 2011.12.19
 * @version 2.5 2011.12.29
 * @version 2.6 2011.12.30
 * @version 2.7 2012.01.16
 * 
 */
public class AutoCompleteService extends WakefulIntentService {
    private static final String TAG = AutoCompleteService.class.getSimpleName();

    private final static PendingIntent getPendingIntent(final Context context) {
        final Intent intent = new Intent(context, AutoCompleteService.class);
        final PendingIntent pi = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }

    public static void set(final Context context) {
        final Calendar c = Calendar.getInstance();
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH), 20, 0);
        c.add(Calendar.MINUTE, 30);
        final long interval = 7 * 24 * 3600 * 1000;
        final AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                interval, AutoCompleteService.getPendingIntent(context));
        if (AppContext.DEBUG) {
            Log.d(AutoCompleteService.TAG,
                    "set repeat interval=3day first time="
                            + DateTimeHelper.formatDate(c.getTime()));
        }
    }

    public static void setIfNot(final Context context) {
        final boolean set = OptionHelper.readBoolean(context,
                R.string.option_set_auto_complete, false);
        if (AppContext.DEBUG) {
            Log.d(AutoCompleteService.TAG, "setIfNot flag=" + set);
        }
        if (!set) {
            OptionHelper.saveBoolean(context,
                    R.string.option_set_auto_complete, true);
            AutoCompleteService.set(context);
        }
    }

    public static void unset(final Context context) {
        final AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        am.cancel(AutoCompleteService.getPendingIntent(context));
        if (AppContext.DEBUG) {
            Log.d(AutoCompleteService.TAG, "unset");
        }
    }

    public AutoCompleteService() {
        super("AutoCompleteService");
    }

    private void doFetchAutoComplete() {
        if (!AppContext.verified) {
            return;
        }
        if (AppContext.noConnection) {
            return;
        }
        final ApiClient api = AppContext.getApiClient();
        int page = 1;
        boolean more = true;
        while (more) {
            List<User> result = null;
            try {
                result = api.usersFriends(null, Constants.MAX_USERS_COUNT,
                        page, Constants.MODE);
            } catch (final Exception e) {
                if (AppContext.DEBUG) {
                    Log.e(AutoCompleteService.TAG, e.toString());
                }
            }
            if ((result != null) && (result.size() > 0)) {
                final int size = result.size();

                final int insertedNums = getContentResolver().bulkInsert(
                        UserInfo.CONTENT_URI,
                        ApiParser.toContentValuesArray(result));
                if (AppContext.DEBUG) {
                    log("doFetchAutoComplete page==" + page + " size=" + size
                            + " insert rows=" + insertedNums);
                }
                if ((size < Constants.MAX_USERS_COUNT) || (page >= 20)) {
                    more = false;
                }
            } else {
                more = false;
            }
            page++;
        }
    }

    @Override
    protected void doWakefulWork(final Intent intent) {
        doFetchAutoComplete();
    }

    public void log(final String message) {
        Log.d(AutoCompleteService.TAG, message);
    }

}
