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

import android.content.Context;
import android.content.SharedPreferences.Editor;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.auth.OAuthToken;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.01
 * @version 1.1 2011.10.10
 * @version 1.2 2011.10.26
 * @version 2.0 2011.12.06
 * @version 3.0 2011.12.26
 * 
 */
public final class OptionHelper {

    public final static void clearSettings() {
        final Editor sp = AppContext.getPreferences().edit();
        sp.clear();
        sp.commit();
    }

    public final static int parseInt(final Context context, final int resId) {
        final String res = AppContext.getPreferences().getString(
                context.getString(resId), "-1");
        return Integer.parseInt(res);
    }

    public final static int parseInt(final Context context, final int resId,
            final String defaultValue) {
        final String res = AppContext.getPreferences().getString(
                context.getString(resId), defaultValue);
        return Integer.parseInt(res);
    }

    public final static int parseInt(final String key) {
        final String res = AppContext.getPreferences().getString(key, "-1");
        return Integer.parseInt(res);
    }

    public final static int parseInt(final String key, final String defaultValue) {
        final String res = AppContext.getPreferences().getString(key,
                defaultValue);
        return Integer.parseInt(res);
    }

    public final static boolean readBoolean(final Context context,
            final int resId, final boolean defValue) {
        final boolean res = AppContext.getPreferences().getBoolean(
                context.getString(resId), defValue);
        return res;
    }

    public final static boolean readBoolean(final String key,
            final boolean defValue) {
        final boolean res = AppContext.getPreferences().getBoolean(key,
                defValue);
        return res;
    }

    public final static int readInt(final Context context, final int resId,
            final int defValue) {
        final int res = AppContext.getPreferences().getInt(
                context.getString(resId), defValue);
        return res;
    }

    public final static int readInt(final String key, final int defValue) {
        final int res = AppContext.getPreferences().getInt(key, defValue);
        return res;
    }

    public final static long readLong(final Context context, final int resId,
            final long defValue) {
        final long res = AppContext.getPreferences().getLong(
                context.getString(resId), defValue);
        return res;
    }

    public final static long readLong(final String key, final int defValue) {
        final long res = AppContext.getPreferences().getLong(key, defValue);
        return res;
    }

    public final static String readString(final Context context,
            final int resId, final String defValue) {
        final String res = AppContext.getPreferences().getString(
                context.getString(resId), defValue);
        return res;
    }

    public final static String readString(final String key,
            final String defValue) {
        final String res = AppContext.getPreferences().getString(key, defValue);
        return res;
    }

    public final static void remove(final Context context, final int resId) {
        final Editor sp = AppContext.getPreferences().edit();
        sp.remove(context.getString(resId));
        sp.commit();
    }

    public final static void remove(final String key) {
        final Editor sp = AppContext.getPreferences().edit();
        sp.remove(key);
        sp.commit();
    }

    public final static void removeAccountInfo(final Context context) {
        final Editor editor = AppContext.getPreferences().edit();
        editor.remove(context.getString(R.string.option_userid));
        editor.remove(context.getString(R.string.option_username));
        editor.remove(context.getString(R.string.option_profile_image));
        editor.remove(context.getString(R.string.option_oauth_token));
        editor.remove(context.getString(R.string.option_oauth_token_secret));
        editor.commit();
    }

    public final static void saveBoolean(final Context context,
            final int resId, final boolean value) {
        final Editor sp = AppContext.getPreferences().edit();
        sp.putBoolean(context.getString(resId), value);
        sp.commit();
    }

    public final static void saveBoolean(final String key, final boolean value) {
        final Editor sp = AppContext.getPreferences().edit();
        sp.putBoolean(key, value);
        sp.commit();
    }

    public final static void saveInt(final Context context, final int resId,
            final int value) {
        final Editor sp = AppContext.getPreferences().edit();
        sp.putInt(context.getString(resId), value);
        sp.commit();
    }

    public final static void saveInt(final String key, final int value) {
        final Editor sp = AppContext.getPreferences().edit();
        sp.putInt(key, value);
        sp.commit();
    }

    public final static void saveLong(final Context context, final int resId,
            final long value) {
        final Editor sp = AppContext.getPreferences().edit();
        sp.putLong(context.getString(resId), value);
        sp.commit();
    }

    public final static void saveLong(final String key, final long value) {
        final Editor sp = AppContext.getPreferences().edit();
        sp.putLong(key, value);
        sp.commit();
    }

    public final static void saveString(final Context context, final int resId,
            final String value) {
        final Editor sp = AppContext.getPreferences().edit();
        sp.putString(context.getString(resId), value);
        sp.commit();
    }

    public final static void saveString(final String key, final String value) {
        final Editor sp = AppContext.getPreferences().edit();
        sp.putString(key, value);
        sp.commit();
    }

    public final static void updateAccountInfo(final Context context,
            final User u, final OAuthToken otoken) {
        final Editor editor = AppContext.getPreferences().edit();
        editor.putString(context.getString(R.string.option_userid), u.id);
        editor.putString(context.getString(R.string.option_username),
                u.screenName);
        editor.putString(context.getString(R.string.option_profile_image),
                u.profileImageUrl);
        editor.putString(context.getString(R.string.option_oauth_token),
                otoken.getToken());
        editor.putString(context.getString(R.string.option_oauth_token_secret),
                otoken.getTokenSecret());
        editor.commit();
    }

    public final static void updateUserInfo(final Context context, final User u) {
        final Editor editor = AppContext.getPreferences().edit();
        editor.putString(context.getString(R.string.option_userid), u.id);
        editor.putString(context.getString(R.string.option_username),
                u.screenName);
        editor.putString(context.getString(R.string.option_profile_image),
                u.profileImageUrl);
        editor.commit();
    }

}
