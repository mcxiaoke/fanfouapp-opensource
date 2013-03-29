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
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.widget.Toast;

import com.fanfou.app.opensource.HomePage;
import com.fanfou.app.opensource.LoginPage;
import com.fanfou.app.opensource.service.Constants;

public final class IntentHelper {
    private static final String TAG = IntentHelper.class.getSimpleName();

    public static Intent getLauncherIntent() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        return intent;
    }

    public static void goHomePage(final Context context, final int page) {
        final Intent intent = new Intent(context, HomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.EXTRA_PAGE, page);
        context.startActivity(intent);
    }

    public static void goLoginPage(final Context context) {
        final Intent intent = new Intent(context, LoginPage.class);
        intent.addFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void logIntent(final String tag, final Intent intent) {
        if (intent == null) {
            return;
        }
        final StringBuffer sb = new StringBuffer();
        sb.append("\nAction:" + intent.getAction());
        sb.append("\nData:" + intent.getData());
        sb.append("\nDataStr:" + intent.getDataString());
        sb.append("\nScheme:" + intent.getScheme());
        sb.append("\nType:" + intent.getType());
        final Bundle extras = intent.getExtras();
        if ((extras != null) && !extras.isEmpty()) {
            for (final String key : extras.keySet()) {
                final Object value = extras.get(key);
                sb.append("\nEXTRA: {" + key + "::" + value + "}");
            }
        } else {
            sb.append("\nNO EXTRAS");
        }
        Log.i(tag, sb.toString());
    }

    public static int sdkVersion() {
        return Integer.valueOf(Build.VERSION.SDK);
    }

    public static void startDialer(final Context context,
            final String phoneNumber) {
        try {
            final Intent dial = new Intent();
            dial.setAction(Intent.ACTION_DIAL);
            dial.setData(Uri.parse("tel:" + phoneNumber));
            context.startActivity(dial);
        } catch (final Exception ex) {
            Log.e(IntentHelper.TAG, "Error starting phone dialer intent.", ex);
            Toast.makeText(context,
                    "Sorry, we couldn't find any app to place a phone call!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void startEmailIntent(final Context context,
            final String emailAddress) {
        try {
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(android.content.Intent.EXTRA_EMAIL,
                    new String[] { emailAddress });
            context.startActivity(intent);
        } catch (final Exception ex) {
            Log.e(IntentHelper.TAG, "Error starting email intent.", ex);
            Toast.makeText(context,
                    "Sorry, we couldn't find any app for sending emails!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void startSmsIntent(final Context context,
            final String phoneNumber) {
        try {
            final Uri uri = Uri.parse("sms:" + phoneNumber);
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra("address", phoneNumber);
            intent.setType("vnd.android-dir/mms-sms");
            context.startActivity(intent);
        } catch (final Exception ex) {
            Log.e(IntentHelper.TAG, "Error starting sms intent.", ex);
            Toast.makeText(context,
                    "Sorry, we couldn't find any app to send an SMS!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void startWebIntent(final Context context, final String url) {
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        } catch (final Exception ex) {
            Log.e(IntentHelper.TAG, "Error starting url intent.", ex);
            Toast.makeText(context,
                    "Sorry, we couldn't find any app for viewing this url!",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
