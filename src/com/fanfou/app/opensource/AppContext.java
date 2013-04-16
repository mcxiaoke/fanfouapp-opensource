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

package com.fanfou.app.opensource;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fanfou.app.opensource.api.ApiClient;
import com.fanfou.app.opensource.api.ApiClientFactory;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.auth.OAuthToken;
import com.fanfou.app.opensource.cache.ImageLoader;
import com.fanfou.app.opensource.cache.ImageLoaderFactory;
import com.fanfou.app.opensource.db.Contents.DirectMessageInfo;
import com.fanfou.app.opensource.db.Contents.DraftInfo;
import com.fanfou.app.opensource.db.Contents.StatusInfo;
import com.fanfou.app.opensource.db.Contents.UserInfo;
import com.fanfou.app.opensource.util.AlarmHelper;
import com.fanfou.app.opensource.util.IOHelper;
import com.fanfou.app.opensource.util.NetworkHelper;
import com.fanfou.app.opensource.util.OptionHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.25
 */

public class AppContext extends Application {

    public static enum Mobile {
        WIFI, HSDPA, NET, WAP;
    }

    // DEBUG开关，为false时不打印任何日志
    public static final boolean DEBUG = false;

    public static final boolean TEST = false;
    public static boolean active = false;
    public static boolean noConnection = false;
    public static boolean verified;

    public static int appVersionCode;

    public static String appVersionName;
    private static String sUserId;

    private static String sUserScreenName;

    private static AppContext sInstance;
    private static SharedPreferences sPreferences;
    private static OAuthToken sToken;

    private static ImageLoader sLoader;

    private static ApiClient sApiClient;

    public static ApiClient getApiClient() {
        return AppContext.sApiClient;
    }

    public static AppContext getAppContext() {
        return AppContext.sInstance;
    }

    public static String getConsumerKey() {
        return AppContext.getAppContext().getString(R.string.api_consumer_key);
    }

    public static String getConsumerSecret() {
        return AppContext.getAppContext().getString(
                R.string.api_consumer_secret);
    }

    public static ImageLoader getImageLoader() {
        return AppContext.sLoader;
    }

    public static OAuthToken getOAuthToken() {
        return AppContext.sToken;
    }

    public static SharedPreferences getPreferences() {
        return AppContext.sPreferences;
    }

    public static String getUserId() {
        return AppContext.sUserId;
    }

    public static String getUserName() {
        return AppContext.sUserScreenName;
    }

    public static synchronized void handleLogout(final Context context) {
        AppContext.getImageLoader().clearQueue();
        AppContext.removeAccountAlarms(context);
        AppContext.removeAccountInfo(context);
        AppContext.removeAccountData(context);
    }

    public static synchronized void handleReset(final Context context) {
        AppContext.getImageLoader().clearQueue();
        AppContext.removeAccountAlarms(context);
        AppContext.removeAccountInfo(context);
        AppContext.removeAccountData(context);
        OptionHelper.clearSettings();
        IOHelper.ClearCache(context);
    }

    public static boolean isWifi() {
        return NetworkHelper.isWifi(AppContext.getAppContext());
    }

    private static void removeAccountAlarms(final Context context) {
        AlarmHelper.unsetScheduledTasks(context);
    }

    private static void removeAccountData(final Context context) {
        if (AppContext.DEBUG) {
            Log.d("App", "removeAccountData");
        }
        final ContentResolver cr = context.getContentResolver();
        cr.delete(StatusInfo.CONTENT_URI, null, null);
        cr.delete(UserInfo.CONTENT_URI, null, null);
        cr.delete(DirectMessageInfo.CONTENT_URI, null, null);
        cr.delete(DraftInfo.CONTENT_URI, null, null);
    }

    private static void removeAccountInfo(final Context context) {
        if (AppContext.DEBUG) {
            Log.d("App", "removeAccountInfo");
        }
        AppContext.setOAuthToken(null);
        AppContext.sUserId = null;
        AppContext.sUserScreenName = null;
        OptionHelper.removeAccountInfo(context);
    }

    public synchronized static void setOAuthToken(final OAuthToken otoken) {
        if (otoken == null) {
            AppContext.verified = false;
            AppContext.sToken = null;
        } else {
            AppContext.verified = true;
            AppContext.sToken = otoken;
        }
    }

    public static void updateAccountInfo(final Context context, final User u,
            final OAuthToken otoken) {
        if (AppContext.DEBUG) {
            Log.d("App", "updateAccountInfo");
        }
        AppContext.sUserId = u.id;
        AppContext.sUserScreenName = u.screenName;
        AppContext.setOAuthToken(otoken);
        OptionHelper.updateAccountInfo(context, u, otoken);

    }

    private void init() {

        AppContext.sInstance = this;
        AppContext.sPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
    }

    private void initAppInfo() {
        if (AppContext.DEBUG) {
            Log.d("App", "initAppInfo");
        }

        final PackageManager pm = getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(getPackageName(), 0);
        } catch (final NameNotFoundException e) {
            pi = new PackageInfo();
            pi.versionName = "1.0";
            pi.versionCode = 20110901;
        }
        AppContext.appVersionCode = pi.versionCode;
        AppContext.appVersionName = pi.versionName;
    }

    private void initPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.options, false);
        AppContext.sUserId = OptionHelper.readString(this,
                R.string.option_userid, null);
        AppContext.sUserScreenName = OptionHelper.readString(this,
                R.string.option_username, null);
        final String oauthAccessToken = OptionHelper.readString(this,
                R.string.option_oauth_token, null);
        final String oauthAccessTokenSecret = OptionHelper.readString(this,
                R.string.option_oauth_token_secret, null);
        AppContext.verified = !StringHelper.isEmpty(oauthAccessTokenSecret);
        if (AppContext.verified) {
            AppContext.sToken = new OAuthToken(oauthAccessToken,
                    oauthAccessTokenSecret);
        }
    }

    @Override
    public void onCreate() {

        super.onCreate();
        init();
        initAppInfo();
        initPreferences();
        versionCheck();
        AppContext.sLoader = ImageLoaderFactory.createImageLoader();
        AppContext.sApiClient = ApiClientFactory.createApiClient(this);
    }

    public void updateUserInfo(final User u) {
        if (AppContext.DEBUG) {
            Log.d("App", "updateAccountInfo u");
        }
        AppContext.sUserId = u.id;
        AppContext.sUserScreenName = u.screenName;
        OptionHelper.updateUserInfo(this, u);
    }

    private void versionCheck() {
        final int oldVersionCode = OptionHelper.readInt(this,
                R.string.option_old_version_code, 0);
        if (oldVersionCode < AppContext.appVersionCode) {
            OptionHelper.saveInt(this, R.string.option_old_version_code,
                    AppContext.appVersionCode);
            AlarmHelper.cleanAlarmFlags(this);
        }
        if (AppContext.DEBUG) {
            Log.d("App", "versionCheck old=" + oldVersionCode + " current="
                    + AppContext.appVersionCode);
        }
        AlarmHelper.checkScheduledTasks(this);
    }

}
