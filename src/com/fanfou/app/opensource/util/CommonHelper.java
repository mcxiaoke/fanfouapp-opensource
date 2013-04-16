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

import java.io.File;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.PhotoViewPage;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.SendPage;
import com.fanfou.app.opensource.StatusPage;
import com.fanfou.app.opensource.api.bean.DirectMessage;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.http.ResponseCode;
import com.fanfou.app.opensource.service.Constants;

/**
 * 网络连接包需要用到的一些静态工具函数
 * 
 * @author mcxiaoke
 * @version 1.0 2011.06.01
 * @version 2.0 2011.09.10
 * @version 3.0 2011.09.28
 * @version 3.5 2011.10.28
 * @version 3.6 2011.12.26
 * 
 */
public final class CommonHelper {

    private static final String TAG = "Utils";

    public static void checkErrorCode(final Activity context,
            final int errorCode, final String errorMessage) {
        if ((errorCode == ResponseCode.HTTP_UNAUTHORIZED)
                || (errorCode == ResponseCode.HTTP_BAD_REQUEST)) {
            ToastHelper.showAuthorizationErrorToast(context,
                    context.getString(R.string.msg_authorization_error));
        } else {
            ToastHelper.showErrorToast(context, errorMessage);
        }
    }

    public static ProgressBar createProgress(final Context context) {
        final ProgressBar p = new ProgressBar(context);
        p.setIndeterminate(true);
        final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                40, 40);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        p.setLayoutParams(lp);
        return p;
    }

    public static float easeOut(float time, final float start, final float end,
            final float duration) {
        return (end * (((time = (time / duration) - 1) * time * time) + 1))
                + start;
    }

    public static String getDmMaxId(final Cursor c) {
        if ((c != null) && c.moveToLast()) {
            final DirectMessage last = DirectMessage.parse(c);
            if (last != null) {
                if (AppContext.DEBUG) {
                    Log.d(CommonHelper.TAG, "getDmMaxId() id=" + last.id);
                }
                return last.id;
            }
        }
        return null;
    }

    public static String getDmSinceId(final Cursor c) {
        if ((c != null) && c.moveToFirst()) {
            final DirectMessage first = DirectMessage.parse(c);
            if (first != null) {
                if (AppContext.DEBUG) {
                    Log.d(CommonHelper.TAG, "getDmSinceId() id=" + first.id);
                }
                return first.id;
            }
        }
        return null;
    }

    public static String getExtension(final String filename) {
        final String filenameArray[] = filename.split("\\.");
        return filenameArray[filenameArray.length - 1].toLowerCase();
    }

    /**
     * 获取MaxId
     * 
     * @param c
     * @return
     */
    public static String getMaxId(final Cursor c) {
        if ((c != null) && c.moveToLast()) {
            final Status first = Status.parse(c);
            if (first != null) {
                if (AppContext.DEBUG) {
                    Log.d(CommonHelper.TAG, "getMaxId() id=" + first.id);
                }
                return first.id;
            }
        }
        return null;
    }

    /**
     * 获取SinceId
     * 
     * @param c
     * @return
     */
    public static String getSinceId(final Cursor c) {
        if ((c != null) && c.moveToFirst()) {
            final Status first = Status.parse(c);
            if (first != null) {
                if (AppContext.DEBUG) {
                    Log.d(CommonHelper.TAG, "getSinceId() id=" + first.id);
                }
                return first.id;
            }
        }
        return null;
    }

    public static void goMessageChatPage(final Context context, final Cursor c) {
        if (c != null) {
            final DirectMessage dm = DirectMessage.parse(c);
            if (dm != null) {
                final Intent intent = new Intent(context, SendPage.class);
                intent.putExtra(Constants.EXTRA_ID, dm.senderId);
                intent.putExtra(Constants.EXTRA_USER_NAME, dm.senderScreenName);
                context.startActivity(intent);
            }
        }
    }

    public static void goPhotoViewPage(final Context context,
            final String photoUrl) {
        final Intent intent = new Intent(context, PhotoViewPage.class);
        intent.putExtra(Constants.EXTRA_URL, photoUrl);
        context.startActivity(intent);
    }

    public static void goStatusPage(final Context context, final Status s) {
        if (s != null) {
            final Intent intent = new Intent(context, StatusPage.class);
            intent.putExtra(Constants.EXTRA_DATA, s);
            context.startActivity(intent);
        }
    }

    public static void goStatusPage(final Context context, final String id) {
        if (!StringHelper.isEmpty(id)) {
            final Intent intent = new Intent(context, StatusPage.class);
            intent.putExtra(Constants.EXTRA_ID, id);
            context.startActivity(intent);
        }
    }

    public static void hideKeyboard(final Context context, final EditText input) {
        final InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    public static void initScreenConfig(final Activity context) {
        final boolean portrait = OptionHelper.readBoolean(context,
                R.string.option_force_portrait, false);
        if (portrait) {
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * @param c
     *            集合
     * @return 判断集合对象是否为空
     */
    public static boolean isEmpty(final Collection<?> c) {
        return (c == null) || (c.size() == 0);
    }

    public static boolean isEmpty(final String str) {
        return (str == null) || str.equals("");
    }

    /**
     * Checks whether the recording service is currently running.
     * 
     * @param ctx
     *            the current context
     * @return true if the service is running, false otherwise
     */
    public static boolean isServiceRunning(final Context ctx, final Class<?> cls) {
        final ActivityManager activityManager = (ActivityManager) ctx
                .getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager
                .getRunningServices(Integer.MAX_VALUE);

        for (final RunningServiceInfo serviceInfo : services) {
            final ComponentName componentName = serviceInfo.service;
            final String serviceName = componentName.getClassName();
            if (serviceName.equals(cls.getName())) {
                return true;
            }
        }
        return false;
    }

    public static void lockScreenOrientation(final Activity context) {
        final boolean portrait = OptionHelper.readBoolean(context,
                R.string.option_force_portrait, false);
        if (portrait) {
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    public static void logTime(final String event, final long time) {
        Log.e("Timer", event + " use time: " + time);
    }

    public static void notify(final Context context, final CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (AppContext.active) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    public static void notify(final Context context, final int resId) {
        if (AppContext.active) {
            Toast.makeText(context, context.getText(resId), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public static void open(final Context context, final String fileName) {
        final String mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(CommonHelper.getExtension(fileName));
        if (mimeType != null) {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(fileName)), mimeType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static void setFullScreen(final Activity activity,
            final boolean fullscreen) {
        if (fullscreen) {
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            activity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public static void setPortraitOrientation(final Activity activity,
            final boolean portrait) {
        if (portrait) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    public static void unlockScreenOrientation(final Activity context) {
        final boolean portrait = OptionHelper.readBoolean(context,
                R.string.option_force_portrait, false);
        if (!portrait) {
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

}
