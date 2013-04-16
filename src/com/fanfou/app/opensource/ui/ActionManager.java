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
package com.fanfou.app.opensource.ui;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.DraftsPage;
import com.fanfou.app.opensource.MyProfilePage;
import com.fanfou.app.opensource.ProfilePage;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.SendPage;
import com.fanfou.app.opensource.UserFavoritesPage;
import com.fanfou.app.opensource.UserListPage;
import com.fanfou.app.opensource.UserTimelinePage;
import com.fanfou.app.opensource.WritePage;
import com.fanfou.app.opensource.api.bean.DirectMessage;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.util.OptionHelper;
import com.fanfou.app.opensource.util.PatternsHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.08.25
 * @version 1.1 2011.10.26
 * @version 1.2 2011.10.27
 * @version 1.3 2011.10.28
 * @version 2.0 2011.10.29
 * @version 2.1 2011.11.09
 * @version 2.2 2011.11.11
 * @version 2.3 2011.11.21
 * @version 2.4 2011.12.08
 * @version 3.0 2011.12.19
 * 
 */
public final class ActionManager {
    public interface ResultListener {
        public void onActionFailed(int type, String message);

        public void onActionSuccess(int type, String message);
    }

    private static final String TAG = ActionManager.class.getSimpleName();

    public static void doMessage(final Context context, final User user) {
        final Intent intent = new Intent(context, SendPage.class);
        intent.putExtra(Constants.EXTRA_ID, user.id);
        intent.putExtra(Constants.EXTRA_USER_NAME, user.screenName);
        context.startActivity(intent);
    }

    public static void doMyProfile(final Context context) {
        final Intent intent = new Intent(context, MyProfilePage.class);
        context.startActivity(intent);
    }

    public static void doProfile(final Context context, final DirectMessage dm) {
        if ((dm == null) || dm.isNull()) {
            if (AppContext.DEBUG) {
                Log.d(ActionManager.TAG, "doProfile: status is null.");
            }
            throw new NullPointerException("directmessage cannot be null.");
        }
        if (dm.senderId.equals(AppContext.getUserId())) {
            ActionManager.doMyProfile(context);
            return;
        }
        final Intent intent = new Intent(context, ProfilePage.class);
        intent.putExtra(Constants.EXTRA_ID, dm.senderId);
        intent.putExtra(Constants.EXTRA_USER_NAME, dm.senderScreenName);
        intent.putExtra(Constants.EXTRA_USER_HEAD, dm.senderProfileImageUrl);
        context.startActivity(intent);
    }

    public static void doProfile(final Context context, final Status status) {
        if ((status == null) || status.isNull()) {
            if (AppContext.DEBUG) {
                Log.d(ActionManager.TAG, "doProfile: status is null.");
            }
            throw new NullPointerException("status cannot be null.");
        }
        if (status.userId.equals(AppContext.getUserId())) {
            ActionManager.doMyProfile(context);
            return;
        }
        final Intent intent = new Intent(context, ProfilePage.class);
        intent.putExtra(Constants.EXTRA_ID, status.userId);
        intent.putExtra(Constants.EXTRA_USER_NAME, status.userScreenName);
        intent.putExtra(Constants.EXTRA_USER_HEAD, status.userProfileImageUrl);
        context.startActivity(intent);
    }

    public static void doProfile(final Context context, final String userId) {
        if (StringHelper.isEmpty(userId)) {
            if (AppContext.DEBUG) {
                Log.d(ActionManager.TAG, "doProfile: userid is null.");
            }
            throw new NullPointerException("userid cannot be null.");
        }
        if (userId.equals(AppContext.getUserId())) {
            ActionManager.doMyProfile(context);
            return;
        }
        final Intent intent = new Intent(context, ProfilePage.class);
        intent.putExtra(Constants.EXTRA_ID, userId);
        context.startActivity(intent);
    }

    public static void doProfile(final Context context, final User user) {
        if ((user == null) || user.isNull()) {
            if (AppContext.DEBUG) {
                Log.d(ActionManager.TAG, "doProfile: user is null.");
            }
            throw new NullPointerException("user cannot be null.");
        }
        if (user.id.equals(AppContext.getUserId())) {
            ActionManager.doMyProfile(context);
            return;
        }
        final Intent intent = new Intent(context, ProfilePage.class);
        intent.putExtra(Constants.EXTRA_DATA, user);
        context.startActivity(intent);
    }

    public static void doReply(final Context context, final Status status) {

        if (status != null) {
            if (AppContext.DEBUG) {
                Log.d(ActionManager.TAG, "doReply: status is null.");
            }
            final StringBuilder sb = new StringBuilder();
            final boolean replyToAll = OptionHelper.readBoolean(context,
                    R.string.option_reply_to_all_default, true);
            if (replyToAll) {
                final ArrayList<String> names = PatternsHelper
                        .getMentions(status);
                for (final String name : names) {
                    sb.append("@").append(name).append(" ");
                }
            } else {
                sb.append("@").append(status.userScreenName).append(" ");
            }

            final Intent intent = new Intent(context, WritePage.class);
            intent.putExtra(Constants.EXTRA_IN_REPLY_TO_ID, status.id);
            intent.putExtra(Constants.EXTRA_TEXT, sb.toString());
            intent.putExtra(Constants.EXTRA_TYPE, WritePage.TYPE_REPLY);
            context.startActivity(intent);
        } else {
            ActionManager.doWrite(context, null);
        }

    }

    public static void doRetweet(final Context context, final Status status) {
        if ((status == null) || status.isNull()) {
            throw new NullPointerException("status cannot be null.");
        }
        final Intent intent = new Intent(context, WritePage.class);
        intent.putExtra(Constants.EXTRA_TYPE, WritePage.TYPE_REPOST);
        intent.putExtra(Constants.EXTRA_IN_REPLY_TO_ID, status.id);
        intent.putExtra(Constants.EXTRA_TEXT, "转@" + status.userScreenName
                + " " + status.simpleText);
        context.startActivity(intent);
    }

    public static void doSend(final Context context) {
        final Intent intent = new Intent(context, SendPage.class);
        context.startActivity(intent);
    }

    public static void doShare(final Context context, final File image) {
        if (AppContext.DEBUG) {
            Log.d(ActionManager.TAG, "doShare: image is " + image);
        }
        if (image == null) {
            return;
        }
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(image));
        context.startActivity(Intent.createChooser(intent, "分享"));
    }

    public static void doShare(final Context context, final Status status) {
        if ((status == null) || status.isNull()) {
            if (AppContext.DEBUG) {
                Log.d(ActionManager.TAG, "doShare: status is null.");
            }
            throw new NullPointerException("status cannot be null.");
        }
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "来自" + status.userScreenName
                + "的饭否消息");
        intent.putExtra(Intent.EXTRA_TEXT, status.simpleText);
        context.startActivity(Intent.createChooser(intent, "分享"));
    }

    public static void doShowDrafts(final Context context) {
        final Intent intent = new Intent(context, DraftsPage.class);
        context.startActivity(intent);
    }

    public static void doShowFavorites(final Context context, final User user) {
        final Intent intent = new Intent(context, UserFavoritesPage.class);
        intent.putExtra(Constants.EXTRA_DATA, user);
        context.startActivity(intent);
    }

    public static void doShowFollowers(final Context context, final User user) {
        final Intent intent = new Intent(context, UserListPage.class);
        intent.putExtra(Constants.EXTRA_DATA, user);
        intent.putExtra(Constants.EXTRA_TYPE, Constants.TYPE_USERS_FOLLOWERS);
        context.startActivity(intent);
    }

    public static void doShowFriends(final Context context, final User user) {
        final Intent intent = new Intent(context, UserListPage.class);
        intent.putExtra(Constants.EXTRA_DATA, user);
        intent.putExtra(Constants.EXTRA_TYPE, Constants.TYPE_USERS_FRIENDS);
        context.startActivity(intent);
    }

    public static void doShowTimeline(final Context context, final User user) {
        final Intent intent = new Intent(context, UserTimelinePage.class);
        intent.putExtra(Constants.EXTRA_DATA, user);
        context.startActivity(intent);
    }

    public static void doWrite(final Context context) {
        ActionManager.doWrite(context, null);
    }

    public static void doWrite(final Context context, final String text) {
        ActionManager.doWrite(context, text, WritePage.TYPE_NORMAL);
    }

    public static void doWrite(final Context context, final String text,
            final File file, final int type) {
        final Intent intent = new Intent(context, WritePage.class);
        intent.putExtra(Constants.EXTRA_TYPE, type);
        intent.putExtra(Constants.EXTRA_TEXT, text);
        intent.putExtra(Constants.EXTRA_DATA, file);
        context.startActivity(intent);
    }

    public static void doWrite(final Context context, final String text,
            final int type) {
        ActionManager.doWrite(context, text, null, type);
    }

    private ActionManager() {
    }

}
