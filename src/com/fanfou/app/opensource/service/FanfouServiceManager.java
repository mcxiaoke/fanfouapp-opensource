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
/**
 * 
 */
package com.fanfou.app.opensource.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.BaseAdapter;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.ui.ActionManager.ResultListener;
import com.fanfou.app.opensource.ui.UIManager.ActionResultHandler;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * 
 */
public final class FanfouServiceManager {
    private static final String TAG = "FanfouServiceManager";

    public static void doDirectMessagesDelete(final Context context,
            final String id, final Handler handler) {
        final Intent intent = new Intent(context, FanFouService.class);
        intent.putExtra(Constants.EXTRA_TYPE,
                Constants.TYPE_DIRECT_MESSAGES_DESTROY);
        intent.putExtra(Constants.EXTRA_ID, id);
        intent.putExtra(Constants.EXTRA_MESSENGER, new Messenger(handler));
        context.startService(intent);
    }

    public static void doFavorite(final Activity activity, final Status status) {
        FanfouServiceManager.doFavorite(activity, status, null, false);
    }

    public static void doFavorite(final Activity activity, final Status s,
            final BaseAdapter adapter) {
        final ActionResultHandler li = new ActionResultHandler() {
            @Override
            public void onActionSuccess(final int type, final String message) {
                if (type == Constants.TYPE_FAVORITES_CREATE) {
                    s.favorited = true;
                } else {
                    s.favorited = false;
                }
                adapter.notifyDataSetChanged();
            }
        };
        FanfouServiceManager.doFavorite(activity, s, li);
    }

    public static void doFavorite(final Activity activity, final Status status,
            final boolean finish) {
        FanfouServiceManager.doFavorite(activity, status, null, finish);
    }

    public static void doFavorite(final Activity activity, final Status s,
            final Cursor c) {
        final ActionResultHandler li = new ActionResultHandler() {
            @Override
            public void onActionSuccess(final int type, final String message) {
                c.requery();
            }
        };
        FanfouServiceManager.doFavorite(activity, s, li);
    }

    public static void doFavorite(final Activity activity, final Status status,
            final ResultListener li) {
        FanfouServiceManager.doFavorite(activity, status, li, false);
    }

    public static void doFavorite(final Activity activity, final Status status,
            final ResultListener li, final boolean finish) {
        if ((status == null) || status.isNull()) {
            if (AppContext.DEBUG) {
                Log.d(FanfouServiceManager.TAG, "doFavorite: status is null.");
            }
            throw new NullPointerException("status cannot be null.");
        }
        final int type = status.favorited ? Constants.TYPE_FAVORITES_DESTROY
                : Constants.TYPE_FAVORITES_CREATE;

        final Handler handler = new Handler() {

            @Override
            public void handleMessage(final Message msg) {
                switch (msg.what) {
                case Constants.RESULT_SUCCESS:
                    final Status result = (Status) msg.getData().getParcelable(
                            Constants.EXTRA_DATA);
                    final String text = result.favorited ? "收藏成功" : "取消收藏成功";
                    CommonHelper.notify(activity.getApplicationContext(), text);
                    FanfouServiceManager.onSuccess(li, type, text);
                    if (finish) {
                        activity.finish();
                    }
                    break;
                case Constants.RESULT_ERROR:
                    final String errorMessage = msg.getData().getString(
                            Constants.EXTRA_ERROR);
                    CommonHelper.notify(activity.getApplicationContext(),
                            errorMessage);
                    FanfouServiceManager.onFailed(li, type, "收藏失败");
                    break;
                default:
                    break;
                }
            }
        };
        if (status.favorited) {
            FanfouServiceManager.doUnfavorite(activity, status.id, handler);
        } else {
            FanfouServiceManager.doFavorite(activity, status.id, handler);
        }
    }

    public static void doFavorite(final Context context, final String id,
            final Handler handler) {
        final Intent intent = new Intent(context, FanFouService.class);
        intent.putExtra(Constants.EXTRA_TYPE, Constants.TYPE_FAVORITES_CREATE);
        intent.putExtra(Constants.EXTRA_ID, id);
        intent.putExtra(Constants.EXTRA_MESSENGER, new Messenger(handler));
        context.startService(intent);
    }

    public static void doFetchDirectMessagesConversationList(
            final Context context, final Messenger messenger,
            final boolean doGetMore) {
        final Intent intent = new Intent(context, FanFouService.class);
        intent.putExtra(Constants.EXTRA_TYPE,
                Constants.TYPE_DIRECT_MESSAGES_CONVERSTATION_LIST);
        intent.putExtra(Constants.EXTRA_MESSENGER, messenger);
        intent.putExtra(Constants.EXTRA_BOOLEAN, doGetMore);
        context.startService(intent);
    }

    public static void doFetchDirectMessagesInbox(final Context context,
            final Messenger messenger, final boolean doGetMore) {
        final Intent intent = new Intent(context, FanFouService.class);
        intent.putExtra(Constants.EXTRA_TYPE,
                Constants.TYPE_DIRECT_MESSAGES_INBOX);
        intent.putExtra(Constants.EXTRA_MESSENGER, messenger);
        intent.putExtra(Constants.EXTRA_BOOLEAN, doGetMore);
        context.startService(intent);
    }

    public static void doFetchFavorites(final Context context,
            final Messenger messenger, final int page, final String userId) {
        FanfouServiceManager.doFetchTimeline(context,
                Constants.TYPE_FAVORITES_LIST, messenger, page, userId, null,
                null);
    }

    public static void doFetchFollowers(final Context context,
            final Handler handler, final int page, final String userId) {
        FanfouServiceManager.doFetchUsers(context,
                Constants.TYPE_USERS_FOLLOWERS, handler, page, userId);
    }

    public static void doFetchFriends(final Context context,
            final Handler handler, final int page, final String userId) {
        FanfouServiceManager.doFetchUsers(context,
                Constants.TYPE_USERS_FRIENDS, handler, page, userId);
    }

    public static void doFetchHomeTimeline(final Context context,
            final Messenger messenger, final String sinceId, final String maxId) {
        FanfouServiceManager.doFetchTimeline(context,
                Constants.TYPE_STATUSES_HOME_TIMELINE, messenger, 0, null,
                sinceId, maxId);
    }

    public static void doFetchMentions(final Context context,
            final Messenger messenger, final String sinceId, final String maxId) {
        FanfouServiceManager.doFetchTimeline(context,
                Constants.TYPE_STATUSES_MENTIONS, messenger, 0, null, sinceId,
                maxId);
    }

    public static void doFetchPublicTimeline(final Context context,
            final Messenger messenger) {
        FanfouServiceManager.doFetchTimeline(context,
                Constants.TYPE_STATUSES_PUBLIC_TIMELINE, messenger, 0, null,
                null, null);
    }

    private static void doFetchTimeline(final Context context, final int type,
            final Messenger messenger, final int page, final String userId,
            final String sinceId, final String maxId) {
        final Intent intent = new Intent(context, FanFouService.class);
        intent.putExtra(Constants.EXTRA_TYPE, type);
        intent.putExtra(Constants.EXTRA_MESSENGER, messenger);
        intent.putExtra(Constants.EXTRA_COUNT, Constants.MAX_TIMELINE_COUNT);
        intent.putExtra(Constants.EXTRA_PAGE, page);
        intent.putExtra(Constants.EXTRA_ID, userId);
        intent.putExtra(Constants.EXTRA_SINCE_ID, sinceId);
        intent.putExtra(Constants.EXTRA_MAX_ID, maxId);
        if (AppContext.DEBUG) {
            Log.d(FanfouServiceManager.TAG, "doFetchTimeline() type=" + type
                    + " page=" + page + " userId=" + userId);
        }
        context.startService(intent);
    }

    private static void doFetchUsers(final Context context, final int type,
            final Handler handler, final int page, final String userId) {
        final Intent intent = new Intent(context, FanFouService.class);
        intent.putExtra(Constants.EXTRA_TYPE, type);
        intent.putExtra(Constants.EXTRA_MESSENGER, new Messenger(handler));
        intent.putExtra(Constants.EXTRA_COUNT, Constants.MAX_USERS_COUNT);
        intent.putExtra(Constants.EXTRA_PAGE, page);
        intent.putExtra(Constants.EXTRA_ID, userId);
        context.startService(intent);
    }

    public static void doFetchUserTimeline(final Context context,
            final Messenger messenger, final String userId,
            final String sinceId, final String maxId) {
        FanfouServiceManager.doFetchTimeline(context,
                Constants.TYPE_STATUSES_USER_TIMELINE, messenger, 0, userId,
                sinceId, maxId);
    }

    public static void doFollow(final Context context, final String userId,
            final Handler handler) {
        final Intent intent = new Intent(context, FanFouService.class);
        intent.putExtra(Constants.EXTRA_TYPE, Constants.TYPE_FRIENDSHIPS_CREATE);
        intent.putExtra(Constants.EXTRA_ID, userId);
        intent.putExtra(Constants.EXTRA_MESSENGER, new Messenger(handler));
        context.startService(intent);

    }

    public static void doFollow(final Context context, final User user,
            final Handler handler) {
        if (user.following) {
            FanfouServiceManager.doUnFollow(context, user.id, handler);
        } else {
            FanfouServiceManager.doFollow(context, user.id, handler);
        }
    }

    public static void doFriendshipsExists(final Context context,
            final String userA, final String userB, final Handler handler) {
        final Intent intent = new Intent(context, FanFouService.class);
        intent.putExtra(Constants.EXTRA_TYPE, Constants.TYPE_FRIENDSHIPS_EXISTS);
        intent.putExtra("user_a", userA);
        intent.putExtra("user_b", userB);
        intent.putExtra(Constants.EXTRA_MESSENGER, new Messenger(handler));
        context.startService(intent);
    }

    public static void doMessageDelete(final Activity activity,
            final String id, final ResultListener li, final boolean finish) {
        if (StringHelper.isEmpty(id)) {
            if (AppContext.DEBUG) {
                Log.d(FanfouServiceManager.TAG,
                        "doMessageDelete: status id is null.");
            }
            throw new NullPointerException("directmessageid cannot be null.");
        }

        final Handler handler = new Handler() {

            @Override
            public void handleMessage(final Message msg) {
                switch (msg.what) {
                case Constants.RESULT_SUCCESS:
                    CommonHelper.notify(AppContext.getAppContext(), "删除成功");
                    FanfouServiceManager.onSuccess(li,
                            Constants.TYPE_DIRECT_MESSAGES_DESTROY, "删除成功");
                    if (finish && (activity != null)) {
                        activity.finish();
                    }
                    break;
                case Constants.RESULT_ERROR:
                    final String errorMessage = msg.getData().getString(
                            Constants.EXTRA_ERROR);
                    CommonHelper.notify(activity.getApplicationContext(),
                            errorMessage);
                    FanfouServiceManager.onFailed(li,
                            Constants.TYPE_DIRECT_MESSAGES_DESTROY, "删除失败");
                    break;
                default:
                    break;
                }
            }
        };
        FanfouServiceManager.doDirectMessagesDelete(activity, id, handler);
    }

    public static void doProfile(final Context context, final String userId,
            final Handler handler) {
        final Intent intent = new Intent(context, FanFouService.class);
        intent.putExtra(Constants.EXTRA_TYPE, Constants.TYPE_USERS_SHOW);
        intent.putExtra(Constants.EXTRA_ID, userId);
        intent.putExtra(Constants.EXTRA_MESSENGER, new Messenger(handler));
        context.startService(intent);
    }

    public static void doStatusDelete(final Activity activity, final String id) {
        FanfouServiceManager.doStatusDelete(activity, id, null);
    }

    public static void doStatusDelete(final Activity activity, final String id,
            final boolean finish) {
        FanfouServiceManager.doStatusDelete(activity, id, null, finish);
    }

    public static void doStatusDelete(final Activity activity, final String id,
            final ResultListener li) {
        FanfouServiceManager.doStatusDelete(activity, id, li, false);
    }

    public static void doStatusDelete(final Activity activity, final String id,
            final ResultListener li, final boolean finish) {
        if (StringHelper.isEmpty(id)) {
            if (AppContext.DEBUG) {
                Log.d(FanfouServiceManager.TAG,
                        "doStatusDelete: status id is null.");
            }
            throw new NullPointerException("statusid cannot be null.");
        }
        final Handler handler = new Handler() {

            @Override
            public void handleMessage(final Message msg) {
                switch (msg.what) {
                case Constants.RESULT_SUCCESS:
                    CommonHelper.notify(AppContext.getAppContext(), "删除成功");
                    FanfouServiceManager.onSuccess(li,
                            Constants.TYPE_STATUSES_DESTROY, "删除成功");
                    if (finish && (activity != null)) {
                        activity.finish();
                    }
                    break;
                case Constants.RESULT_ERROR:
                    final String errorMessage = msg.getData().getString(
                            Constants.EXTRA_ERROR);
                    CommonHelper.notify(activity.getApplicationContext(),
                            errorMessage);
                    FanfouServiceManager.onFailed(li,
                            Constants.TYPE_STATUSES_DESTROY, "删除失败");
                    break;
                default:
                    break;
                }
            }
        };
        FanfouServiceManager.doStatusesDelete(activity, id, handler);
    }

    public static void doStatusesDelete(final Context context, final String id,
            final Handler handler) {
        final Intent intent = new Intent(context, FanFouService.class);
        intent.putExtra(Constants.EXTRA_TYPE, Constants.TYPE_STATUSES_DESTROY);
        intent.putExtra(Constants.EXTRA_ID, id);
        intent.putExtra(Constants.EXTRA_MESSENGER, new Messenger(handler));
        context.startService(intent);
    }

    public static void doUnfavorite(final Context context, final String id,
            final Handler handler) {
        final Intent intent = new Intent(context, FanFouService.class);
        intent.putExtra(Constants.EXTRA_TYPE, Constants.TYPE_FAVORITES_DESTROY);
        intent.putExtra(Constants.EXTRA_ID, id);
        intent.putExtra(Constants.EXTRA_MESSENGER, new Messenger(handler));
        context.startService(intent);
    }

    public static void doUnFollow(final Context context, final String userId,
            final Handler handler) {
        final Intent intent = new Intent(context, FanFouService.class);
        intent.putExtra(Constants.EXTRA_TYPE,
                Constants.TYPE_FRIENDSHIPS_DESTROY);
        intent.putExtra(Constants.EXTRA_ID, userId);
        intent.putExtra(Constants.EXTRA_MESSENGER, new Messenger(handler));
        context.startService(intent);

    }

    private static void onFailed(final ResultListener li, final int type,
            final String message) {
        if (li != null) {
            li.onActionFailed(type, message);
        }
    }

    private static void onSuccess(final ResultListener li, final int type,
            final String message) {
        if (li != null) {
            li.onActionSuccess(type, message);
        }
    }
}
