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

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.service.AutoCompleteService;
import com.fanfou.app.opensource.service.DownloadService;
import com.fanfou.app.opensource.service.NotificationService;

/**
 * @author mcxiaoke
 * @version 1.0 2011.09.22
 * @version 1.1 2011.10.21
 * @version 1.1 2011.10.28
 * @version 2.0 2011.11.24
 * @version 2.5 2011.11.25
 * @version 3.0 2011.12.02
 * @version 3.1 2011.12.05
 * @version 3.2 2011.12.26
 * 
 */
public final class AlarmHelper {
    private static final String TAG = AlarmHelper.class.getSimpleName();

    public final static void checkScheduledTasks(final Context context) {
        if (AppContext.DEBUG) {
            Log.d(AlarmHelper.TAG, "checkScheduledTasks");
        }
        NotificationService.setIfNot(context);
        AutoCompleteService.setIfNot(context);
    }

    public static void cleanAlarmFlags(final Context context) {
        if (AppContext.DEBUG) {
            Log.d("App", "cleanAlarmFlags");
        }
        final Editor editor = AppContext.getPreferences().edit();
        editor.remove(AppContext.getAppContext().getString(
                R.string.option_set_auto_clean));
        editor.remove(AppContext.getAppContext().getString(
                R.string.option_set_auto_update));
        editor.remove(AppContext.getAppContext().getString(
                R.string.option_set_auto_complete));
        editor.remove(AppContext.getAppContext().getString(
                R.string.option_set_notification));
        editor.commit();
    }

    public final static void setNotificationType(final Context context,
            final Notification notification) {
        final AudioManager am = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            final String ringFile = OptionHelper.readString(context,
                    R.string.option_notification_ringtone, null);
            Uri ringTone = null;
            if (!TextUtils.isEmpty(ringFile)) {
                ringTone = Uri.parse(ringFile);
                notification.sound = ringTone;
            }
        }

        final boolean vibrate = OptionHelper.readBoolean(context,
                R.string.option_notification_vibrate, false);
        if (vibrate) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        } else {
            notification.vibrate = null;
        }

        final boolean led = OptionHelper.readBoolean(context,
                R.string.option_notification_led, false);
        if (led) {
            notification.defaults |= Notification.DEFAULT_LIGHTS;
        } else {
            notification.ledOnMS = 0;
            notification.ledOffMS = 0;
        }
    }

    public final static void setScheduledTasks(final Context context) {
        if (AppContext.DEBUG) {
            Log.d(AlarmHelper.TAG, "setScheduledTasks");
        }
        NotificationService.set(context);
        AutoCompleteService.set(context);
    }

    public final static void unsetScheduledTasks(final Context context) {
        if (AppContext.DEBUG) {
            Log.d(AlarmHelper.TAG, "unsetScheduledTasks");
        }
        DownloadService.unset(context);
        NotificationService.unset(context);
        AutoCompleteService.unset(context);
    }

}
