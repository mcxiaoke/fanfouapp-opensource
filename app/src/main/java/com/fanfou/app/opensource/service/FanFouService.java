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

import static com.fanfou.app.opensource.service.Constants.TYPE_ACCOUNT_NOTIFICATION;
import static com.fanfou.app.opensource.service.Constants.TYPE_ACCOUNT_RATE_LIMIT_STATUS;
import static com.fanfou.app.opensource.service.Constants.TYPE_ACCOUNT_REGISTER;
import static com.fanfou.app.opensource.service.Constants.TYPE_ACCOUNT_UPDATE_PROFILE;
import static com.fanfou.app.opensource.service.Constants.TYPE_ACCOUNT_UPDATE_PROFILE_IMAGE;
import static com.fanfou.app.opensource.service.Constants.TYPE_ACCOUNT_VERIFY_CREDENTIALS;
import static com.fanfou.app.opensource.service.Constants.TYPE_BLOCKS;
import static com.fanfou.app.opensource.service.Constants.TYPE_BLOCKS_CREATE;
import static com.fanfou.app.opensource.service.Constants.TYPE_BLOCKS_DESTROY;
import static com.fanfou.app.opensource.service.Constants.TYPE_BLOCKS_EXISTS;
import static com.fanfou.app.opensource.service.Constants.TYPE_BLOCKS_IDS;
import static com.fanfou.app.opensource.service.Constants.TYPE_DIRECT_MESSAGES_CONVERSTATION;
import static com.fanfou.app.opensource.service.Constants.TYPE_DIRECT_MESSAGES_CONVERSTATION_LIST;
import static com.fanfou.app.opensource.service.Constants.TYPE_DIRECT_MESSAGES_CREATE;
import static com.fanfou.app.opensource.service.Constants.TYPE_DIRECT_MESSAGES_DESTROY;
import static com.fanfou.app.opensource.service.Constants.TYPE_DIRECT_MESSAGES_INBOX;
import static com.fanfou.app.opensource.service.Constants.TYPE_DIRECT_MESSAGES_OUTBOX;
import static com.fanfou.app.opensource.service.Constants.TYPE_FAVORITES_CREATE;
import static com.fanfou.app.opensource.service.Constants.TYPE_FAVORITES_DESTROY;
import static com.fanfou.app.opensource.service.Constants.TYPE_FAVORITES_LIST;
import static com.fanfou.app.opensource.service.Constants.TYPE_FOLLOWERS_IDS;
import static com.fanfou.app.opensource.service.Constants.TYPE_FRIENDSHIPS_ACCEPT;
import static com.fanfou.app.opensource.service.Constants.TYPE_FRIENDSHIPS_CREATE;
import static com.fanfou.app.opensource.service.Constants.TYPE_FRIENDSHIPS_DENY;
import static com.fanfou.app.opensource.service.Constants.TYPE_FRIENDSHIPS_DESTROY;
import static com.fanfou.app.opensource.service.Constants.TYPE_FRIENDSHIPS_EXISTS;
import static com.fanfou.app.opensource.service.Constants.TYPE_FRIENDSHIPS_REQUESTS;
import static com.fanfou.app.opensource.service.Constants.TYPE_FRIENDSHIPS_SHOW;
import static com.fanfou.app.opensource.service.Constants.TYPE_FRIENDS_IDS;
import static com.fanfou.app.opensource.service.Constants.TYPE_NONE;
import static com.fanfou.app.opensource.service.Constants.TYPE_PHOTOS_UPLOAD;
import static com.fanfou.app.opensource.service.Constants.TYPE_PHOTOS_USER_TIMELINE;
import static com.fanfou.app.opensource.service.Constants.TYPE_SAVED_SEARCHES_CREATE;
import static com.fanfou.app.opensource.service.Constants.TYPE_SAVED_SEARCHES_DESTROY;
import static com.fanfou.app.opensource.service.Constants.TYPE_SAVED_SEARCHES_LIST;
import static com.fanfou.app.opensource.service.Constants.TYPE_SAVED_SEARCHES_SHOW;
import static com.fanfou.app.opensource.service.Constants.TYPE_SEARCH_PUBLIC_TIMELINE;
import static com.fanfou.app.opensource.service.Constants.TYPE_SEARCH_USERS;
import static com.fanfou.app.opensource.service.Constants.TYPE_SEARCH_USER_TIMELINE;
import static com.fanfou.app.opensource.service.Constants.TYPE_STATUSES_CONTEXT_TIMELINE;
import static com.fanfou.app.opensource.service.Constants.TYPE_STATUSES_DESTROY;
import static com.fanfou.app.opensource.service.Constants.TYPE_STATUSES_HOME_TIMELINE;
import static com.fanfou.app.opensource.service.Constants.TYPE_STATUSES_MENTIONS;
import static com.fanfou.app.opensource.service.Constants.TYPE_STATUSES_PUBLIC_TIMELINE;
import static com.fanfou.app.opensource.service.Constants.TYPE_STATUSES_SHOW;
import static com.fanfou.app.opensource.service.Constants.TYPE_STATUSES_UPDATE;
import static com.fanfou.app.opensource.service.Constants.TYPE_STATUSES_USER_TIMELINE;
import static com.fanfou.app.opensource.service.Constants.TYPE_TRENDS_LIST;
import static com.fanfou.app.opensource.service.Constants.TYPE_USERS_FOLLOWERS;
import static com.fanfou.app.opensource.service.Constants.TYPE_USERS_FRIENDS;
import static com.fanfou.app.opensource.service.Constants.TYPE_USERS_SHOW;

import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.ApiClient;
import com.fanfou.app.opensource.api.ApiException;
import com.fanfou.app.opensource.api.ApiParser;
import com.fanfou.app.opensource.api.bean.DirectMessage;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.db.Contents.BasicColumns;
import com.fanfou.app.opensource.db.Contents.DirectMessageInfo;
import com.fanfou.app.opensource.db.Contents.StatusInfo;
import com.fanfou.app.opensource.db.Contents.UserInfo;
import com.fanfou.app.opensource.db.FanFouProvider;
import com.fanfou.app.opensource.http.ResponseCode;
import com.fanfou.app.opensource.util.CommonHelper;

/**
 * @author mcxiaoke
 * @version 1.0 20110602
 * @version 2.0 20110714
 * @version 2.1 2011.10.10
 * @version 3.0 2011.10.20
 * @version 3.1 2011.10.21
 * @version 3.2 2011.10.24
 * @version 3.3 2011.10.28
 * @version 4.0 2011.11.04
 * @version 4.1 2011.11.07
 * @version 4.2 2011.11.10
 * @version 4.3 2011.11.11
 * @version 4.4 2011.11.17
 * @version 5.0 2011.11.18
 * @version 5.1 2011.11.21
 * @version 5.2 2011.11.22
 * @version 5.3 2011.12.13
 * @version 6.0 2011.12.16
 * @version 6.1 2011.12.19
 * @version 7.0 2011.12.23
 * @version 7.1 2011.12.26
 * 
 */
public class FanFouService extends IntentService {
    private static final String TAG = FanFouService.class.getSimpleName();

    private int type;

    private Messenger messenger;

    private ApiClient api;

    public FanFouService() {
        super("FetchService");
    }

    private void blocksCreate(final Intent intent) {
        final String id = intent.getStringExtra(Constants.EXTRA_ID);
        final String where = BasicColumns.ID + "=?";
        final String[] whereArgs = new String[] { id };
        try {
            final User u = this.api.blocksCreate(id, Constants.MODE);
            if ((u == null) || u.isNull()) {
                sendSuccessMessage();
            } else {
                getContentResolver().delete(UserInfo.CONTENT_URI, where,
                        whereArgs);
                sendParcelableMessage(u);
            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            sendErrorMessage(e);
        }
    }

    private void blocksDelete(final Intent intent) {
        final String id = intent.getStringExtra(Constants.EXTRA_ID);
        try {
            final User u = this.api.blocksDelete(id, Constants.MODE);
            if ((u == null) || u.isNull()) {
                sendSuccessMessage();
            } else {
                sendParcelableMessage(u);
            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            sendErrorMessage(e);
        }
    }

    private void directMessagesDelete(final Intent intent) {
        final String id = intent.getStringExtra(Constants.EXTRA_ID);
        final String where = BasicColumns.ID + "=?";
        final String[] whereArgs = new String[] { id };
        try {
            // 删除消息
            // 404 说明消息不存在
            // 403 说明不是你的消息，无权限删除
            final DirectMessage dm = this.api.directMessagesDelete(id,
                    Constants.MODE);
            if ((dm == null) || dm.isNull()) {
                sendSuccessMessage();
            } else {
                final ContentResolver cr = getContentResolver();
                cr.delete(DirectMessageInfo.CONTENT_URI, where, whereArgs);
                sendParcelableMessage(dm);
            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            sendErrorMessage(e);
        }
    }

    private void favoritesCreate(final Intent intent) {
        final String id = intent.getStringExtra(Constants.EXTRA_ID);
        final String where = BasicColumns.ID + "=?";
        final String[] whereArgs = new String[] { id };
        try {
            final Status s = this.api.favoritesCreate(id, Constants.FORMAT,
                    Constants.MODE);
            if ((s == null) || s.isNull()) {
                sendSuccessMessage();
            } else {
                final ContentResolver cr = getContentResolver();
                final ContentValues values = new ContentValues();
                values.put(StatusInfo.FAVORITED, true);
                cr.update(StatusInfo.CONTENT_URI, values, where, whereArgs);
                FanFouProvider.updateUserInfo(this, s.user);
                sendParcelableMessage(s);
            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            if (e.statusCode == 404) {
                final Uri uri = FanFouProvider.buildUriWithStatusId(id);
                getContentResolver().delete(uri, null, null);
            }
            sendErrorMessage(e);
        }
    }

    private void favoritesDelete(final Intent intent) {
        // 404 消息不存在
        // 404 没有通过用户验证
        final String id = intent.getStringExtra(Constants.EXTRA_ID);
        final String where = BasicColumns.ID + "=?";
        final String[] whereArgs = new String[] { id };
        try {
            final Status s = this.api.favoritesDelete(id, Constants.FORMAT,
                    Constants.MODE);
            if ((s == null) || s.isNull()) {
                sendSuccessMessage();
            } else {
                final ContentResolver cr = getContentResolver();
                final ContentValues values = new ContentValues();
                values.put(StatusInfo.FAVORITED, false);
                cr.update(StatusInfo.CONTENT_URI, values, where, whereArgs);
                FanFouProvider.updateUserInfo(this, s.user);
                sendParcelableMessage(s);
            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            if (e.statusCode == 404) {
                final Uri uri = FanFouProvider.buildUriWithStatusId(id);
                getContentResolver().delete(uri, null, null);
            }
            sendErrorMessage(e);
        }
    }

    private void fetchConversationList(final Intent intent) {
        int count = intent.getIntExtra(Constants.EXTRA_COUNT,
                count = Constants.DEFAULT_TIMELINE_COUNT);
        if (AppContext.isWifi()) {
            count = Constants.MAX_TIMELINE_COUNT;
        } else {
            count = Constants.DEFAULT_TIMELINE_COUNT;
        }
        final boolean doGetMore = intent.getBooleanExtra(
                Constants.EXTRA_BOOLEAN, false);
        try {
            if (doGetMore) {
                sendIntMessage(fetchOldDirectMessages(count));
            } else {
                sendIntMessage(fetchNewDirectMessages(count));
            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            sendErrorMessage(e);
        }
    }

    private int fetchDirectMessagesInbox(final int count,
            final boolean doGetMore) throws ApiException {
        final Cursor ic = initInboxMessagesCursor();
        List<DirectMessage> messages = null;
        if (doGetMore) {
            messages = this.api.directMessagesInbox(count, 0, null,
                    CommonHelper.getDmMaxId(ic), Constants.MODE);
        } else {
            messages = this.api.directMessagesInbox(count, 0,
                    CommonHelper.getDmSinceId(ic), null, Constants.MODE);
        }
        ic.close();
        if ((messages != null) && (messages.size() > 0)) {
            final ContentResolver cr = getContentResolver();
            final int size = messages.size();
            if (AppContext.DEBUG) {
                log("fetchDirectMessagesInbox size()=" + size);
            }
            final int nums = cr.bulkInsert(DirectMessageInfo.CONTENT_URI,
                    ApiParser.toContentValuesArray(messages));
            return nums;
        } else {
            if (AppContext.DEBUG) {
                log("fetchDirectMessagesInbox size()=0");
            }
        }
        return 0;
    }

    private void fetchDirectMessagesInbox(final Intent intent) {
        int count = intent.getIntExtra(Constants.EXTRA_COUNT,
                count = Constants.DEFAULT_TIMELINE_COUNT);
        if (AppContext.isWifi()) {
            count = Constants.MAX_TIMELINE_COUNT;
        } else {
            count = Constants.DEFAULT_TIMELINE_COUNT;
        }
        final boolean doGetMore = intent.getBooleanExtra(
                Constants.EXTRA_BOOLEAN, false);
        try {
            sendIntMessage(fetchDirectMessagesInbox(count, doGetMore));
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            sendErrorMessage(e);
        }
    }

    private int fetchDirectMessagesOutbox(final int count,
            final boolean doGetMore) throws ApiException {
        final Cursor ic = initOutboxMessagesCursor();
        List<DirectMessage> messages = null;
        if (doGetMore) {
            messages = this.api.directMessagesOutbox(count, 0, null,
                    CommonHelper.getDmMaxId(ic), Constants.MODE);
        } else {
            messages = this.api.directMessagesOutbox(count, 0,
                    CommonHelper.getDmSinceId(ic), null, Constants.MODE);
        }
        ic.close();
        if ((messages != null) && (messages.size() > 0)) {
            final ContentResolver cr = getContentResolver();
            final int size = messages.size();
            if (AppContext.DEBUG) {
                log("fetchDirectMessagesOutbox size()=" + size);
            }
            final int nums = cr.bulkInsert(DirectMessageInfo.CONTENT_URI,
                    ApiParser.toContentValuesArray(messages));
            return nums;
        } else {
            if (AppContext.DEBUG) {
                log("fetchDirectMessagesOutbox size()=0");
            }
        }
        return 0;
    }

    private void fetchDirectMessagesOutbox(final Intent intent) {
        int count = intent.getIntExtra(Constants.EXTRA_COUNT,
                count = Constants.DEFAULT_TIMELINE_COUNT);
        if (AppContext.isWifi()) {
            count = Constants.MAX_TIMELINE_COUNT;
        } else {
            count = Constants.DEFAULT_TIMELINE_COUNT;
        }
        final boolean doGetMore = intent.getBooleanExtra(
                Constants.EXTRA_BOOLEAN, false);
        try {
            sendIntMessage(fetchDirectMessagesOutbox(count, doGetMore));
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            sendErrorMessage(e);
        }
    }

    private int fetchNewDirectMessages(final int count) throws ApiException {
        final Cursor ic = initInboxMessagesCursor();
        final Cursor oc = initOutboxMessagesCursor();
        try {
            final String inboxSinceId = CommonHelper.getDmSinceId(ic);
            final String outboxSinceId = CommonHelper.getDmSinceId(oc);
            final List<DirectMessage> messages = new ArrayList<DirectMessage>();
            final List<DirectMessage> in = this.api.directMessagesInbox(count,
                    0, inboxSinceId, null, Constants.MODE);
            if ((in != null) && (in.size() > 0)) {
                messages.addAll(in);
            }
            final List<DirectMessage> out = this.api.directMessagesOutbox(
                    count, 0, outboxSinceId, null, Constants.MODE);
            if ((out != null) && (out.size() > 0)) {
                messages.addAll(out);
            }
            if ((messages != null) && (messages.size() > 0)) {
                final ContentResolver cr = getContentResolver();
                final int size = messages.size();
                if (AppContext.DEBUG) {
                    log("fetchNewDirectMessages size()=" + size);
                }
                final int nums = cr.bulkInsert(DirectMessageInfo.CONTENT_URI,
                        ApiParser.toContentValuesArray(messages));
                return nums;
            } else {
                if (AppContext.DEBUG) {
                    log("fetchNewDirectMessages size()=0");
                }
            }
        } finally {
            oc.close();
            ic.close();
        }
        return 0;
    }

    private int fetchOldDirectMessages(final int count) throws ApiException {
        final Cursor ic = initInboxMessagesCursor();
        final Cursor oc = initOutboxMessagesCursor();
        try {
            final String inboxMaxId = CommonHelper.getDmMaxId(ic);
            final String outboxMaxid = CommonHelper.getDmMaxId(oc);
            final List<DirectMessage> messages = new ArrayList<DirectMessage>();
            final List<DirectMessage> in = this.api.directMessagesInbox(count,
                    0, null, inboxMaxId, Constants.MODE);
            if ((in != null) && (in.size() > 0)) {
                messages.addAll(in);
            }
            final List<DirectMessage> out = this.api.directMessagesOutbox(
                    count, 0, null, outboxMaxid, Constants.MODE);
            if ((out != null) && (out.size() > 0)) {
                messages.addAll(out);
            }
            if ((messages != null) && (messages.size() > 0)) {
                final ContentResolver cr = getContentResolver();
                final int size = messages.size();
                if (AppContext.DEBUG) {
                    log("doFetchMessagesMore size()=" + size);
                }
                final int nums = cr.bulkInsert(DirectMessageInfo.CONTENT_URI,
                        ApiParser.toContentValuesArray(messages));
                return nums;
            } else {
                if (AppContext.DEBUG) {
                    log("doFetchMessagesMore size()=0");
                }
            }
        } finally {
            oc.close();
            ic.close();
        }
        return 0;
    }

    private void fetchTimeline(final Intent intent) {
        if (AppContext.DEBUG) {
            Log.d(FanFouService.TAG, "fetchTimeline");
        }
        List<Status> statuses = null;

        final int page = intent.getIntExtra(Constants.EXTRA_PAGE, 0);
        final String id = intent.getStringExtra(Constants.EXTRA_ID);
        final String sinceId = intent.getStringExtra(Constants.EXTRA_SINCE_ID);
        final String maxId = intent.getStringExtra(Constants.EXTRA_MAX_ID);

        int count = intent.getIntExtra(Constants.EXTRA_COUNT,
                Constants.DEFAULT_TIMELINE_COUNT);
        if (AppContext.isWifi()) {
            count = Constants.MAX_TIMELINE_COUNT;
        } else {
            count = Constants.DEFAULT_TIMELINE_COUNT;
        }
        try {
            switch (this.type) {
            case TYPE_STATUSES_HOME_TIMELINE:
                if (AppContext.DEBUG) {
                    Log.d(FanFouService.TAG, "fetchTimeline TYPE_HOME");
                }
                statuses = this.api.homeTimeline(count, page, sinceId, maxId,
                        Constants.FORMAT, Constants.MODE);

                break;
            case TYPE_STATUSES_MENTIONS:
                if (AppContext.DEBUG) {
                    Log.d(FanFouService.TAG, "fetchTimeline TYPE_MENTION");
                }
                statuses = this.api.mentions(count, page, sinceId, maxId,
                        Constants.FORMAT, Constants.MODE);
                break;
            case TYPE_STATUSES_PUBLIC_TIMELINE:
                count = Constants.DEFAULT_TIMELINE_COUNT;
                if (AppContext.DEBUG) {
                    Log.d(FanFouService.TAG, "fetchTimeline TYPE_PUBLIC");
                }
                statuses = this.api.pubicTimeline(count, Constants.FORMAT,
                        Constants.MODE);
                break;
            case TYPE_FAVORITES_LIST:
                if (AppContext.DEBUG) {
                    Log.d(FanFouService.TAG, "fetchTimeline TYPE_FAVORITES");
                }
                statuses = this.api.favorites(count, page, id,
                        Constants.FORMAT, Constants.MODE);
                break;
            case TYPE_STATUSES_USER_TIMELINE:
                if (AppContext.DEBUG) {
                    Log.d(FanFouService.TAG, "fetchTimeline TYPE_USER");
                }
                statuses = this.api.userTimeline(count, page, id, sinceId,
                        maxId, Constants.FORMAT, Constants.MODE);
                break;
            case TYPE_STATUSES_CONTEXT_TIMELINE:
                if (AppContext.DEBUG) {
                    Log.d(FanFouService.TAG, "fetchTimeline TYPE_CONTEXT");
                }
                statuses = this.api.contextTimeline(id, Constants.FORMAT,
                        Constants.MODE);
                break;
            default:
                break;
            }
            if ((statuses == null) || (statuses.size() == 0)) {
                sendIntMessage(0);
                if (AppContext.DEBUG) {
                    Log.d(FanFouService.TAG, "fetchTimeline received no items.");
                }
                return;
            } else {
                final int size = statuses.size();
                if (AppContext.DEBUG) {
                    Log.d(FanFouService.TAG,
                            "fetchTimeline received items count=" + size);
                }
                final ContentResolver cr = getContentResolver();
                if ((size >= count) && (page <= 1) && (maxId == null)) {
                    final String where = BasicColumns.TYPE + " = ?";
                    final String[] whereArgs = new String[] { String
                            .valueOf(this.type) };
                    final int delete = cr.delete(StatusInfo.CONTENT_URI, where,
                            whereArgs);
                    if (AppContext.DEBUG) {
                        Log.d(FanFouService.TAG, "fetchTimeline items count = "
                                + count + " ,remove " + delete
                                + " old statuses.");
                    }
                }
                final int insertedCount = cr.bulkInsert(StatusInfo.CONTENT_URI,
                        ApiParser.toContentValuesArray(statuses));
                sendIntMessage(insertedCount);
                updateUsersFromStatus(statuses, this.type);
            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                log("fetchTimeline [error]" + e.statusCode + ":"
                        + e.errorMessage);
                e.printStackTrace();
            }
            sendErrorMessage(e);
        }
    }

    private void fetchUsers(final Intent intent) {
        final String ownerId = intent.getStringExtra(Constants.EXTRA_ID);
        final int page = intent.getIntExtra(Constants.EXTRA_PAGE, 0);
        int count = intent.getIntExtra(Constants.EXTRA_COUNT,
                Constants.DEFAULT_USERS_COUNT);
        if (AppContext.DEBUG) {
            log("fetchFriendsOrFollowers ownerId=" + ownerId + " page=" + page);
        }

        if (AppContext.isWifi()) {
            count = Constants.MAX_USERS_COUNT;
        } else {
            count = Constants.DEFAULT_USERS_COUNT;
        }
        try {
            List<User> users = null;
            if (this.type == Constants.TYPE_USERS_FRIENDS) {
                users = this.api.usersFriends(ownerId, count, page,
                        Constants.MODE);
            } else if (this.type == Constants.TYPE_USERS_FOLLOWERS) {
                users = this.api.usersFollowers(ownerId, count, page,
                        Constants.MODE);
            }
            if ((users != null) && (users.size() > 0)) {

                final int size = users.size();
                if (AppContext.DEBUG) {
                    log("fetchFriendsOrFollowers size=" + size);
                }
                final ContentResolver cr = getContentResolver();
                if ((page < 2) && (ownerId != null)) {
                    final String where = BasicColumns.OWNER_ID + " =? ";
                    final String[] whereArgs = new String[] { ownerId };
                    final int deletedNums = cr.delete(UserInfo.CONTENT_URI,
                            where, whereArgs);
                    if (AppContext.DEBUG) {
                        log("fetchFriendsOrFollowers delete old rows "
                                + deletedNums);
                    }
                }
                final int nums = cr.bulkInsert(UserInfo.CONTENT_URI,
                        ApiParser.toContentValuesArray(users));
                if (AppContext.DEBUG) {
                    log("fetchFriendsOrFollowers refresh ,insert rows, num="
                            + nums);
                }
                sendIntMessage(nums);
            } else {
                sendIntMessage(0);
            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            sendErrorMessage(e);
        }
    }

    private void friendshipsCreate(final Intent intent) {
        final String id = intent.getStringExtra(Constants.EXTRA_ID);
        try {
            final User u = this.api.friendshipsCreate(id, Constants.MODE);
            if ((u == null) || u.isNull()) {
                sendSuccessMessage();
            } else {
                u.type = Constants.TYPE_USERS_FRIENDS;
                getContentResolver().insert(UserInfo.CONTENT_URI,
                        u.toContentValues());
                sendParcelableMessage(u);
            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            sendErrorMessage(e);
        }
    }

    private void friendshipsDelete(final Intent intent) {
        final String id = intent.getStringExtra(Constants.EXTRA_ID);
        try {
            final User u = this.api.friendshipsDelete(id, Constants.MODE);
            if ((u == null) || u.isNull()) {
                sendSuccessMessage();
            } else {
                u.type = Constants.TYPE_NONE;
                final ContentResolver cr = getContentResolver();
                cr.delete(UserInfo.CONTENT_URI, BasicColumns.ID + "=?",
                        new String[] { id });
                sendParcelableMessage(u);
                // 取消关注后要清空该用户名下的消息
                cr.delete(StatusInfo.CONTENT_URI, StatusInfo.USER_ID + "=?",
                        new String[] { id });
            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            sendErrorMessage(e);
        }
    }

    private void friendshipsExists(final Intent intent) {
        final String userA = intent.getStringExtra("user_a");
        final String userB = intent.getStringExtra("user_b");
        boolean result = false;
        try {
            result = this.api.friendshipsExists(userA, userB);
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                Log.e(FanFouService.TAG,
                        "doDetectFriendships:" + e.getMessage());
            }
            sendErrorMessage(e);
        }
        final Bundle data = new Bundle();
        data.putBoolean(Constants.EXTRA_BOOLEAN, result);
        sendSuccessMessage(data);
    }

    private Cursor initInboxMessagesCursor() {
        final String where = BasicColumns.TYPE + " = ? ";
        final String[] whereArgs = new String[] { String
                .valueOf(Constants.TYPE_DIRECT_MESSAGES_INBOX) };
        return getContentResolver().query(DirectMessageInfo.CONTENT_URI,
                DirectMessageInfo.COLUMNS, where, whereArgs, null);
    }

    private Cursor initOutboxMessagesCursor() {
        final String where = BasicColumns.TYPE + " = ? ";
        final String[] whereArgs = new String[] { String
                .valueOf(Constants.TYPE_DIRECT_MESSAGES_OUTBOX) };
        return getContentResolver().query(DirectMessageInfo.CONTENT_URI,
                DirectMessageInfo.COLUMNS, where, whereArgs, null);
    }

    public void log(final String message) {
        Log.d(FanFouService.TAG, message);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (intent == null) {
            return;
        }
        this.messenger = intent.getParcelableExtra(Constants.EXTRA_MESSENGER);
        this.type = intent.getIntExtra(Constants.EXTRA_TYPE, -1);
        this.api = AppContext.getApiClient();

        if (AppContext.DEBUG) {
            log("onHandleIntent() type=" + this.type);
        }

        switch (this.type) {
        case TYPE_NONE:
            break;
        case TYPE_ACCOUNT_REGISTER:
            break;
        case TYPE_ACCOUNT_VERIFY_CREDENTIALS:
            break;
        case TYPE_ACCOUNT_RATE_LIMIT_STATUS:
            break;
        case TYPE_ACCOUNT_UPDATE_PROFILE:
            break;
        case TYPE_ACCOUNT_UPDATE_PROFILE_IMAGE:
            break;
        case TYPE_ACCOUNT_NOTIFICATION:
            break;
        case TYPE_STATUSES_HOME_TIMELINE:
        case TYPE_STATUSES_MENTIONS:
        case TYPE_STATUSES_USER_TIMELINE:
        case TYPE_STATUSES_CONTEXT_TIMELINE:
        case TYPE_STATUSES_PUBLIC_TIMELINE:
        case TYPE_FAVORITES_LIST:
            fetchTimeline(intent);
            break;
        case TYPE_STATUSES_SHOW:
            statusesShow(intent);
            break;
        case TYPE_STATUSES_UPDATE:
            break;
        case TYPE_STATUSES_DESTROY:
            statusesDestroy(intent);
            break;
        case TYPE_DIRECT_MESSAGES_INBOX:
            fetchDirectMessagesInbox(intent);
            break;
        case TYPE_DIRECT_MESSAGES_OUTBOX:
            fetchDirectMessagesOutbox(intent);
            break;
        case TYPE_DIRECT_MESSAGES_CONVERSTATION_LIST:
            fetchConversationList(intent);
            break;
        case TYPE_DIRECT_MESSAGES_CONVERSTATION:
            break;
        case TYPE_DIRECT_MESSAGES_CREATE:
            break;
        case TYPE_DIRECT_MESSAGES_DESTROY:
            directMessagesDelete(intent);
            break;
        case TYPE_USERS_SHOW:
            userShow(intent);
            break;
        case TYPE_USERS_FRIENDS:
        case TYPE_USERS_FOLLOWERS:
            fetchUsers(intent);
            break;
        case TYPE_FRIENDSHIPS_CREATE:
            friendshipsCreate(intent);
            break;
        case TYPE_FRIENDSHIPS_DESTROY:
            friendshipsDelete(intent);
            break;
        case TYPE_FRIENDSHIPS_EXISTS:
            friendshipsExists(intent);
            break;
        case TYPE_FRIENDSHIPS_SHOW:
            break;
        case TYPE_FRIENDSHIPS_REQUESTS:
            break;
        case TYPE_FRIENDSHIPS_DENY:
            break;
        case TYPE_FRIENDSHIPS_ACCEPT:
            break;
        case TYPE_BLOCKS:
            break;
        case TYPE_BLOCKS_IDS:
            break;
        case TYPE_BLOCKS_CREATE:
            blocksCreate(intent);
            break;
        case TYPE_BLOCKS_DESTROY:
            blocksDelete(intent);
            break;
        case TYPE_BLOCKS_EXISTS:
            break;
        case TYPE_FRIENDS_IDS:
            break;
        case TYPE_FOLLOWERS_IDS:
            break;
        case TYPE_FAVORITES_CREATE:
            favoritesCreate(intent);
            break;
        case TYPE_FAVORITES_DESTROY:
            favoritesDelete(intent);
            break;
        case TYPE_PHOTOS_USER_TIMELINE:
            break;
        case TYPE_PHOTOS_UPLOAD:
            break;
        case TYPE_SEARCH_PUBLIC_TIMELINE:
            break;
        case TYPE_SEARCH_USER_TIMELINE:
            break;
        case TYPE_SEARCH_USERS:
            break;
        case TYPE_SAVED_SEARCHES_LIST:
            break;
        case TYPE_SAVED_SEARCHES_SHOW:
            break;
        case TYPE_SAVED_SEARCHES_CREATE:
            break;
        case TYPE_SAVED_SEARCHES_DESTROY:
            break;
        case TYPE_TRENDS_LIST:
        default:
            break;
        }

    }

    private void sendErrorMessage(final ApiException e) {
        String message = e.getMessage();
        if (e.statusCode == ResponseCode.ERROR_IO_EXCEPTION) {
            message = getString(R.string.msg_connection_error);
        } else if (e.statusCode >= 500) {
            message = getString(R.string.msg_server_error);
        }
        final Bundle bundle = new Bundle();
        bundle.putInt(Constants.EXTRA_CODE, e.statusCode);
        bundle.putString(Constants.EXTRA_ERROR, message);
        sendMessage(Constants.RESULT_ERROR, bundle);
    }

    private void sendIntMessage(final int size) {
        final Bundle bundle = new Bundle();
        bundle.putInt(Constants.EXTRA_COUNT, size);
        sendMessage(Constants.RESULT_SUCCESS, bundle);
    }

    private void sendMessage(final int what, final Bundle bundle) {
        if (this.messenger == null) {
            return;
        }
        final Message m = Message.obtain();
        m.what = what;
        m.arg1 = this.type;
        if (bundle != null) {
            m.getData().putAll(bundle);
        }
        try {
            this.messenger.send(m);
        } catch (final RemoteException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private void sendParcelableMessage(final Parcelable parcel) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.EXTRA_DATA, parcel);
        sendMessage(Constants.RESULT_SUCCESS, bundle);
    }

    private void sendSuccessMessage() {
        sendMessage(Constants.RESULT_SUCCESS, null);
    }

    private void sendSuccessMessage(final Bundle bundle) {
        sendMessage(Constants.RESULT_SUCCESS, bundle);
    }

    private void statusesDestroy(final Intent intent) {
        final String id = intent.getStringExtra(Constants.EXTRA_ID);
        try {
            final Status s = this.api.statusesDelete(id, Constants.FORMAT,
                    Constants.MODE);
            if ((s == null) || s.isNull()) {
                sendSuccessMessage();
            } else {
                final ContentResolver cr = getContentResolver();
                final Uri uri = Uri.parse(StatusInfo.CONTENT_URI + "/id/" + id);
                cr.delete(uri, null, null);
                sendParcelableMessage(s);
            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            if (e.statusCode == 404) {
                final Uri uri = FanFouProvider.buildUriWithStatusId(id);
                getContentResolver().delete(uri, null, null);
            }
            sendErrorMessage(e);
        }
    }

    private void statusesShow(final Intent intent) {
        final String id = intent.getStringExtra(Constants.EXTRA_ID);
        try {
            final Status s = this.api.statusesShow(id, Constants.FORMAT,
                    Constants.MODE);
            if ((s == null) || s.isNull()) {
                sendSuccessMessage();
            } else {
                if (!FanFouProvider.updateUserInfo(this, s.user)) {
                    FanFouProvider.insertUserInfo(this, s.user);
                }
                FanFouProvider.updateUserInfo(this, s.user);
                sendParcelableMessage(s);
            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            if (e.statusCode == 404) {
                final Uri uri = FanFouProvider.buildUriWithStatusId(id);
                getContentResolver().delete(uri, null, null);
            }
            sendErrorMessage(e);
        }

    }

    private int updateUsersFromStatus(final List<Status> statuses,
            final int type) {
        if ((type == Constants.TYPE_STATUSES_USER_TIMELINE)
                || (type == Constants.TYPE_FAVORITES_LIST)) {
            return 0;
        }
        final ArrayList<User> us = new ArrayList<User>();
        for (final Status s : statuses) {
            final User u = s.user;
            if (u != null) {
                if (!FanFouProvider.updateUserInfo(this, u)) {
                    if (AppContext.DEBUG) {
                        log("extractUsers from status list , udpate failed, insert it");
                    }
                    us.add(s.user);
                }
            }
        }

        int result = 0;
        if (us.size() > 0) {
            result = getContentResolver().bulkInsert(UserInfo.CONTENT_URI,
                    ApiParser.toContentValuesArray(us));
            if (AppContext.DEBUG) {
                log("extractUsers from status list , insert result=" + result);
            }
        }
        return result;
    }

    private void userShow(final Intent intent) {
        final String id = intent.getStringExtra(Constants.EXTRA_ID);
        try {
            final User u = this.api.userShow(id, Constants.MODE);
            if ((u == null) || u.isNull()) {
                sendSuccessMessage();
            } else {
                if (!FanFouProvider.updateUserInfo(this, u)) {
                    FanFouProvider.insertUserInfo(this, u);
                }
                sendParcelableMessage(u);

            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            sendErrorMessage(e);
        }
    }

}
