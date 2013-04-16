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

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.db.FanFouProvider;

/**
 * @author mcxiaoke
 * @version 1.0 2011.09.29
 * @version 1.1 2011.10.26
 * @version 1.2 2011.10.28
 * @version 1.3 2011.11.18
 * @version 1.4 2011.12.05
 * @version 1.5 2011.12.06
 * 
 */
public final class CacheManager {

    private static final String TAG = CacheManager.class.getSimpleName();

    private static UserCache sUserCache;
    private static StatusCache sStatusCache;

    static {
        CacheManager.sUserCache = new UserCache();
        CacheManager.sStatusCache = new StatusCache();
    }

    public static Status getStatus(final Context context, final String key) {
        Status status = CacheManager.sStatusCache.get(key);

        if (status == null) {
            status = CacheManager.getStatusFromDB(context, key);
            if (status != null) {
                CacheManager.put(status);
            }
        }
        return status;
    }

    public static Status getStatus(final String key) {
        return CacheManager.sStatusCache.get(key);
    }

    public static Status getStatusFromDB(final Context context, final String id) {
        final Cursor cursor = context.getContentResolver()
                .query(FanFouProvider.buildUriWithStatusId(id), null, null,
                        null, null);
        try {
            if ((cursor != null) && cursor.moveToFirst()) {
                if (AppContext.DEBUG) {
                    Log.d(CacheManager.TAG,
                            "queryStatus cursor.size=" + cursor.getCount());
                }
                final Status status = Status.parse(cursor);
                return status;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static User getUser(final Context context, final String key) {
        User user = CacheManager.sUserCache.get(key);
        if (user == null) {
            user = CacheManager.getUserFromDB(context, key);
            if (user != null) {
                CacheManager.put(user);
            }
        }
        return user;
    }

    public static User getUser(final String key) {
        if (AppContext.DEBUG) {
            Log.v("CacheManager", "get user from cache : " + key);
        }
        return CacheManager.sUserCache.get(key);
    }

    public static User getUserFromDB(final Context context, final String id) {
        final Cursor cursor = context.getContentResolver().query(
                FanFouProvider.buildUriWithUserId(id), null, null, null, null);
        try {
            if ((cursor != null) && cursor.moveToFirst()) {
                if (AppContext.DEBUG) {
                    Log.d(CacheManager.TAG,
                            "queryUser cursor.size=" + cursor.getCount());
                }
                final User user = User.parse(cursor);
                return user;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static void put(final Status status) {
        if (status != null) {
            CacheManager.sStatusCache.put(status.id, status);
        }
    }

    public static void put(final User user) {
        if (user != null) {
            CacheManager.sUserCache.put(user.id, user);
        }
    }

}
