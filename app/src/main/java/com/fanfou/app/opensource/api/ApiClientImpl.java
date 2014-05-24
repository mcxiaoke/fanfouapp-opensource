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
package com.fanfou.app.opensource.api;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.api.bean.DirectMessage;
import com.fanfou.app.opensource.api.bean.Search;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.auth.OAuthConfig;
import com.fanfou.app.opensource.auth.OAuthService;
import com.fanfou.app.opensource.auth.OAuthToken;
import com.fanfou.app.opensource.cache.CacheManager;
import com.fanfou.app.opensource.http.ResponseCode;
import com.fanfou.app.opensource.http.SimpleClient;
import com.fanfou.app.opensource.http.SimpleRequest;
import com.fanfou.app.opensource.http.SimpleResponse;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.15
 * @version 1.1 2011.05.17
 * @version 1.2 2011.10.28
 * @version 1.3 2011.11.04
 * @version 1.4 2011.11.07
 * @version 2.0 2011.11.07
 * @version 2.1 2011.11.09
 * @version 2.2 2011.11.11
 * @version 3.0 2011.11.18
 * @version 4.0 2011.11.21
 * @version 4.1 2011.11.22
 * @version 4.2 2011.11.23
 * @version 4.3 2011.11.28
 * @version 4.4 2011.11.29
 * @version 4.5 2011.11.30
 * @version 4.6 2011.12.01
 * @version 4.7 2011.12.02
 * @version 4.8 2011.12.05
 * @version 4.9 2011.12.06
 * @version 5.0 2011.12.12
 * @version 5.1 2011.12.13
 * @version 6.0 2011.12.16
 * @version 6.1 2011.12.20
 * @version 6.2 2011.12.23
 * @version 6.3 2011.12.26
 * @version 6.4 2013.03.04
 * 
 */
class ApiClientImpl implements ApiClient, ApiConfig, ResponseCode {
    private static final String TAG = ApiClientImpl.class.getSimpleName();

    private static final OAuthConfig OAUTH_CONFIG = new OAuthConfig();
    private final Context mAppContext;

    public ApiClientImpl(final Context context) {
        this.mAppContext = context.getApplicationContext();
    }

    @Override
    public ArrayList<User> blocksBlocking(final int count, final int page,
            final String mode) throws ApiException {
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(ApiConfig.URL_BLOCKS_USERS);
        builder.count(count).page(page);
        final SimpleResponse response = fetch(builder.build());
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("userBlockedList()---statusCode=" + statusCode);
        }
        return User.parseUsers(response);
    }

    @Override
    public User blocksCreate(final String userId, final String mode)
            throws ApiException {
        // String url=String.format(URL_BLOCKS_CREATE, userId);
        if (TextUtils.isEmpty(userId)) {
            throw new NullPointerException(
                    "blocksCreate() userId must not be empty or null.");
        }
        String url;
        try {
            url = String.format(ApiConfig.URL_BLOCKS_CREATE,
                    URLEncoder.encode(userId, HTTP.UTF_8));
        } catch (final UnsupportedEncodingException e) {
            url = String.format(ApiConfig.URL_BLOCKS_CREATE, userId);
        }

        final SimpleResponse response = doPostIdAction(url, null, null, mode);
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("userBlock()---statusCode=" + statusCode + " url=" + url);
        }

        // handlerResponseError(response);
        final User u = User.parse(response);
        if (u != null) {
            u.ownerId = AppContext.getUserId();
        }
        return u;
    }

    @Override
    public User blocksDelete(final String userId, final String mode)
            throws ApiException {
        // String url=String.format(URL_BLOCKS_DESTROY, userId);
        if (TextUtils.isEmpty(userId)) {
            throw new NullPointerException(
                    "blocksDelete() userId must not be empty or null.");
        }
        String url;
        try {
            url = String.format(ApiConfig.URL_BLOCKS_DESTROY,
                    URLEncoder.encode(userId, HTTP.UTF_8));
        } catch (final UnsupportedEncodingException e) {
            url = String.format(ApiConfig.URL_BLOCKS_DESTROY, userId);
        }

        final SimpleResponse response = doPostIdAction(url, null, null, mode);
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("userUnblock()---statusCode=" + statusCode + " url=" + url);
        }

        // handlerResponseError(response);
        final User u = User.parse(response);
        if (u != null) {
            u.ownerId = AppContext.getUserId();
        }
        return u;
    }

    @Override
    public User blocksExists(final String userId, final String mode)
            throws ApiException {
        final SimpleResponse response = doPostIdAction(
                ApiConfig.URL_BLOCKS_EXISTS, userId, null, mode);
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("userIsBlocked()---statusCode=" + statusCode);
        }
        return User.parse(response);
    }

    @Override
    public ArrayList<String> blocksIDs() throws ApiException {
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(ApiConfig.URL_BLOCKS_IDS);
        final SimpleResponse response = fetch(builder.build());
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("userBlockedIDs()---statusCode=" + statusCode);
        }
        try {
            return ApiParser.ids(response);
        } catch (final IOException e) {
            e.printStackTrace();
            throw new ApiException(ResponseCode.ERROR_IO_EXCEPTION,
                    e.getMessage());
        }
    }

    @Override
    public ArrayList<Status> contextTimeline(final String id,
            final String format, final String mode) throws ApiException {
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(ApiConfig.URL_TIMELINE_CONTEXT).id(id).format("html")
                .mode("lite");
        final SimpleResponse response = fetch(builder.build());
        final ArrayList<Status> ss = Status.parseStatuses(response,
                Constants.TYPE_STATUSES_CONTEXT_TIMELINE);
        return ss;
    }

    @Override
    public ArrayList<DirectMessage> directMessagesConversation(
            final String userId, final String maxId, final int count,
            final String mode) throws ApiException {
        if (TextUtils.isEmpty(userId)) {
            throw new NullPointerException(
                    "directMessagesConversation() userId must not be empty or null.");
        }
        final SimpleRequest.Builder builder = SimpleRequest.newBuilder();
        builder.url(ApiConfig.URL_DIRECT_MESSAGES_CONVERSATION).id(userId)
                .count(count).maxId(maxId).mode(mode);
        final SimpleResponse response = fetch(builder.build());
        final List<DirectMessage> dms = DirectMessage
                .parseConversationUser(response);
        if ((dms != null) && (dms.size() > 0)) {
            for (final DirectMessage dm : dms) {
                dm.threadUserId = userId;
            }
        }
        return null;
    }

    @Override
    public ArrayList<DirectMessage> directMessagesConversationList(
            final int count, final int page, final String mode)
            throws ApiException {
        final SimpleRequest.Builder builder = SimpleRequest.newBuilder();
        builder.url(ApiConfig.URL_DIRECT_MESSAGES_CONVERSATION).count(count)
                .page(page).mode(mode);
        final SimpleResponse response = fetch(builder.build());
        return DirectMessage.parseConversationList(response);
    }

    @Override
    public DirectMessage directMessagesCreate(final String userId,
            final String text, final String inReplyToId, final String mode)
            throws ApiException {
        if (StringHelper.isEmpty(userId) || StringHelper.isEmpty(text)) {
            if (AppContext.DEBUG) {
                throw new IllegalArgumentException("收信人ID和私信内容都不能为空");
            }
            return null;
        }
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(ApiConfig.URL_DIRECT_MESSAGES_NEW);
        builder.post();
        builder.param("user", userId);
        builder.param("text", text);
        builder.param("in_reply_to_id", inReplyToId);
        final SimpleResponse response = fetch(builder.build());
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("DirectMessagesCreate()---statusCode=" + statusCode);
        }

        final DirectMessage dm = DirectMessage.parse(response,
                Constants.TYPE_DIRECT_MESSAGES_OUTBOX);
        if ((dm != null) && !dm.isNull()) {
            dm.threadUserId = dm.recipientId;
            dm.threadUserName = dm.recipientScreenName;
            return dm;
        } else {
            return null;
        }
    }

    @Override
    public DirectMessage directMessagesDelete(final String directMessageId,
            final String mode) throws ApiException {
        if (TextUtils.isEmpty(directMessageId)) {
            throw new NullPointerException(
                    "directMessagesDelete() directMessageId must not be empty or null.");
        }
        final String url = String.format(ApiConfig.URL_DIRECT_MESSAGES_DESTROY,
                directMessageId);
        final SimpleResponse response = doPostIdAction(url, null, null, mode);
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("DirectMessagesDelete()---statusCode=" + statusCode + " url="
                    + url);
        }
        return DirectMessage.parse(response, Constants.TYPE_NONE);
    }

    @Override
    public ArrayList<DirectMessage> directMessagesInbox(final int count,
            final int page, final String sinceId, final String maxId,
            final String mode) throws ApiException {
        final ArrayList<DirectMessage> dms = messages(
                ApiConfig.URL_DIRECT_MESSAGES_INBOX, count, page, sinceId,
                maxId, mode, Constants.TYPE_DIRECT_MESSAGES_INBOX);
        if ((dms != null) && (dms.size() > 0)) {
            for (final DirectMessage dm : dms) {
                dm.threadUserId = dm.senderId;
                dm.threadUserName = dm.senderScreenName;
            }
        }
        return dms;
    }

    @Override
    public ArrayList<DirectMessage> directMessagesOutbox(final int count,
            final int page, final String sinceId, final String maxId,
            final String mode) throws ApiException {
        final ArrayList<DirectMessage> dms = messages(
                ApiConfig.URL_DIRECT_MESSAGES_OUTBOX, count, page, sinceId,
                maxId, mode, Constants.TYPE_DIRECT_MESSAGES_OUTBOX);
        if ((dms != null) && (dms.size() > 0)) {
            for (final DirectMessage dm : dms) {
                dm.threadUserId = dm.recipientId;
                dm.threadUserName = dm.recipientScreenName;
            }
        }
        return dms;
    }

    /**
     * action for only id param --get
     * 
     * @param url
     *            api url
     * @param id
     *            userid or status id or dm id
     * @return string for id
     * @throws ApiException
     */
    private SimpleResponse doGetIdAction(final String url, final String id,
            final String format, final String mode) throws ApiException {
        if (AppContext.DEBUG) {
            log("doGetIdAction() ---url=" + url + " id=" + id);
        }
        return doSingleIdAction(url, id, format, mode, false);
    }

    /**
     * action for only id param --post
     * 
     * @param url
     * @param id
     * @return
     * @throws ApiException
     */
    private SimpleResponse doPostIdAction(final String url, final String id,
            final String format, final String mode) throws ApiException {
        if (AppContext.DEBUG) {
            log("doPostIdAction() ---url=" + url + " id=" + id);
        }
        return doSingleIdAction(url, id, format, mode, true);
    }

    /**
     * action for only id param --get/post
     * 
     * @param url
     * @param id
     * @param isPost
     * @return
     * @throws ApiException
     */
    private SimpleResponse doSingleIdAction(final String url, final String id,
            final String format, final String mode, final boolean isPost)
            throws ApiException {
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(url).id(id).post(isPost).format(format).mode(mode);
        return fetch(builder.build());
    }

    @Override
    public ArrayList<Status> favorites(final int count, final int page,
            final String userId, final String format, final String mode)
            throws ApiException {
        final ArrayList<Status> ss = fetchStatuses(
                ApiConfig.URL_FAVORITES_LIST, count, page, userId, null, null,
                format, mode, Constants.TYPE_FAVORITES_LIST);

        if ((userId != null) && (ss != null)) {
            for (final Status status : ss) {
                status.ownerId = userId;
            }
        }
        return ss;
    }

    @Override
    public Status favoritesCreate(final String statusId, final String format,
            final String mode) throws ApiException {
        if (TextUtils.isEmpty(statusId)) {
            throw new NullPointerException(
                    "favoritesCreate() statusId must not be empty or null.");
        }
        final String url = String.format(ApiConfig.URL_FAVORITES_CREATE,
                statusId);
        final SimpleResponse response = doPostIdAction(url, null, format, mode);
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("favoritesCreate()---statusCode=" + statusCode + " url=" + url);
        }
        final Status s = Status.parse(response);
        return s;
    }

    @Override
    public Status favoritesDelete(final String statusId, final String format,
            final String mode) throws ApiException {
        if (TextUtils.isEmpty(statusId)) {
            throw new NullPointerException(
                    "favoritesDelete() statusId must not be empty or null.");
        }
        final String url = String.format(ApiConfig.URL_FAVORITES_DESTROY,
                statusId);
        final SimpleResponse response = doPostIdAction(url, null, format, mode);
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("favoritesDelete()---statusCode=" + statusCode + " url=" + url);
        }

        final Status s = Status.parse(response);
        return s;
    }

    /**
     * exec http request
     * 
     * @param request
     * @return response object
     * @throws ApiException
     */
    private SimpleResponse fetch(final SimpleRequest request)
            throws ApiException {
        final Context context = getAppContext();
        if (context == null) {
            throw new ApiException(ResponseCode.ERROR_IO_EXCEPTION,
                    "context is null");
        }
        final OAuthToken token = AppContext.getOAuthToken();
        final OAuthService service = new OAuthService(
                ApiClientImpl.OAUTH_CONFIG, token);
        try {
            service.addOAuthSignature(request);
            final SimpleClient client = new SimpleClient(context);
            final HttpResponse response = client.exec(request);
            final SimpleResponse res = new SimpleResponse(response);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (AppContext.DEBUG) {
                log("fetch() url=" + request.url + " post=" + request.post
                        + " statusCode=" + statusCode);
            }
            if (statusCode == ResponseCode.HTTP_OK) {
                return res;
            } else {
                throw new ApiException(statusCode, ApiParser.error(res
                        .getContent()));
            }
        } catch (final IOException e) {
            if (AppContext.DEBUG) {
                Log.e(ApiClientImpl.TAG, e.toString());
            }
            throw new ApiException(ResponseCode.ERROR_IO_EXCEPTION,
                    e.getMessage(), e.getCause());
        }
    }

    /**
     * fetch statuses --get
     * 
     * @param url
     *            api url
     * @param count
     *            optional
     * @param page
     *            optional
     * @param userId
     *            optional
     * @param sinceId
     *            optional
     * @param maxId
     *            optional
     * @param isHtml
     *            optional
     * @return statuses list
     * @throws ApiException
     */
    ArrayList<Status> fetchStatuses(final String url, final int count,
            final int page, final String userId, final String sinceId,
            final String maxId, final String format, final String mode,
            final int type) throws ApiException {
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(url).count(count).page(page).id(userId).sinceId(sinceId)
                .maxId(maxId).format(format).mode(mode);
        final SimpleRequest request = builder.build();
        final SimpleResponse response = fetch(request);
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("fetchStatuses()---statusCode=" + statusCode + " url="
                    + request.url);
        }
        return Status.parseStatuses(response, type);

    }

    private ArrayList<User> fetchUsers(final String url, final String userId,
            final int count, final int page) throws ApiException {
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(url).id(userId).count(count).page(page)
                .param("mode", "noprofile");
        final SimpleResponse response = fetch(builder.build());

        return User.parseUsers(response);
    }

    @Override
    public ArrayList<String> followersIDs(final String userId, final int count,
            final int page) throws ApiException {
        return ids(ApiConfig.URL_USERS_FOLLOWERS_IDS, userId, count, page);
    }

    @Override
    public User friendshipsCreate(final String userId, final String mode)
            throws ApiException {
        if (TextUtils.isEmpty(userId)) {
            throw new NullPointerException(
                    "friendshipsCreate() userId must not be empty or null.");
        }
        String url;
        try {
            // hack for oauth chinese charactar encode
            url = String.format(ApiConfig.URL_FRIENDSHIPS_CREATE,
                    URLEncoder.encode(userId, HTTP.UTF_8));
        } catch (final UnsupportedEncodingException e) {
            url = String.format(ApiConfig.URL_FRIENDSHIPS_CREATE, userId);
        }

        final SimpleResponse response = doPostIdAction(url, null, null, mode);
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("friendshipsCreate()---statusCode=" + statusCode + " url="
                    + url);
        }
        final User u = User.parse(response);
        if (u != null) {
            u.ownerId = AppContext.getUserId();
            CacheManager.put(u);
        }
        return u;
    }

    @Override
    public User friendshipsDelete(final String userId, final String mode)
            throws ApiException {
        if (TextUtils.isEmpty(userId)) {
            throw new NullPointerException(
                    "friendshipsDelete() userId must not be empty or null.");
        }
        // String url=String.format(URL_FRIENDSHIPS_DESTROY, userId);
        String url;
        try {
            url = String.format(ApiConfig.URL_FRIENDSHIPS_DESTROY,
                    URLEncoder.encode(userId, HTTP.UTF_8));
        } catch (final UnsupportedEncodingException e) {
            url = String.format(ApiConfig.URL_FRIENDSHIPS_DESTROY, userId);
        }

        final SimpleResponse response = doPostIdAction(url, null, null, mode);
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("friendshipsDelete()---statusCode=" + statusCode + " url="
                    + url);
        }
        final User u = User.parse(response);
        if (u != null) {
            u.ownerId = AppContext.getUserId();
            CacheManager.put(u);
        }
        return u;
    }

    @Override
    public boolean friendshipsExists(final String userA, final String userB)
            throws ApiException {
        if (StringHelper.isEmpty(userA) || StringHelper.isEmpty(userB)) {
            throw new IllegalArgumentException(
                    "friendshipsExists() usera and userb must not be empty or null.");
        }
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(ApiConfig.URL_FRIENDSHIS_EXISTS);
        builder.param("user_a", userA);
        builder.param("user_b", userB);
        final SimpleResponse response = fetch(builder.build());

        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("isFriends()---statusCode=" + statusCode);
        }
        try {
            final String content = response.getContent();
            if (AppContext.DEBUG) {
                log("isFriends()---response=" + content);
            }
            return content.contains("true");
        } catch (final IOException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public ArrayList<String> friendsIDs(final String userId, final int count,
            final int page) throws ApiException {
        return ids(ApiConfig.URL_USERS_FRIENDS_IDS, userId, count, page);
    }

    @Override
    public Context getAppContext() {
        return this.mAppContext;
    }

    @Override
    public ArrayList<Status> homeTimeline(final int count, final int page,
            final String sinceId, final String maxId, final String format,
            final String mode) throws ApiException {
        final ArrayList<Status> ss = fetchStatuses(ApiConfig.URL_TIMELINE_HOME,
                count, page, null, sinceId, maxId, format, mode,
                Constants.TYPE_STATUSES_HOME_TIMELINE);
        return ss;
    }

    // 最大2000
    private ArrayList<String> ids(final String url, final String userId,
            final int count, final int page) throws ApiException {
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(url);
        builder.id(userId);
        builder.count(count);
        builder.page(page);
        final SimpleResponse response = fetch(builder.build());
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("ids()---statusCode=" + statusCode);
        }

        try {
            return ApiParser.ids(response);
        } catch (final IOException e) {
            throw new ApiException(ResponseCode.ERROR_IO_EXCEPTION,
                    e.getMessage());
        }

    }

    private void log(final String message) {
        Log.d(ApiClientImpl.TAG, message);
    }

    @Override
    public ArrayList<Status> mentions(final int count, final int page,
            final String sinceId, final String maxId, final String format,
            final String mode) throws ApiException {
        final ArrayList<Status> ss = fetchStatuses(
                ApiConfig.URL_TIMELINE_MENTIONS, count, page, null, sinceId,
                maxId, format, mode, Constants.TYPE_STATUSES_MENTIONS);
        return ss;
    }

    private ArrayList<DirectMessage> messages(final String url,
            final int count, final int page, final String sinceId,
            final String maxId, final String mode, final int type)
            throws ApiException {
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(url).count(count).page(page).maxId(maxId).sinceId(sinceId)
                .mode(mode);

        // count<=0,count>60时返回60条
        // count>=0&&count<=60时返回count条
        // int c=count;
        // if(c<1||c>ApiConfig.MAX_COUNT){
        // c=ApiConfig.MAX_COUNT;
        // }
        // builder.count(c);

        final SimpleResponse response = fetch(builder.build());
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("messages()---statusCode=" + statusCode);
        }
        return DirectMessage.parseMessges(response, type);
    }

    @Override
    public ArrayList<Status> photosTimeline(final int count, final int page,
            final String userId, final String sinceId, final String maxId,
            final String format, final String mode) throws ApiException {
        final ArrayList<Status> ss = fetchStatuses(
                ApiConfig.URL_PHOTO_USER_TIMELINE, count, page, userId,
                sinceId, maxId, format, mode,
                Constants.TYPE_STATUSES_USER_TIMELINE);
        if (AppContext.DEBUG) {
            log("photosTimeline()");
        }
        return ss;
    }

    @Override
    public Status photosUpload(final File photo, final String status,
            final String source, final String location, final String format,
            final String mode) throws ApiException {
        if (photo == null) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (AppContext.DEBUG) {
            log("upload()---photo=" + photo.getAbsolutePath() + " status="
                    + status + " source=" + source + " location=" + location);
        }
        ;

        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(ApiConfig.URL_PHOTO_UPLOAD).post();
        builder.status(status).location(location);
        builder.param("photo", photo);
        builder.param("source", source);
        builder.format(format).mode(mode);
        final SimpleResponse response = fetch(builder.build());
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("photoUpload()---statusCode=" + statusCode);
        }
        return Status.parse(response, Constants.TYPE_STATUSES_HOME_TIMELINE);
    }

    @Override
    public ArrayList<Status> pubicTimeline(final int count,
            final String format, final String mode) throws ApiException {
        final ArrayList<Status> ss = fetchStatuses(
                ApiConfig.URL_TIMELINE_PUBLIC, count, 0, null, null, null,
                format, mode, Constants.TYPE_STATUSES_PUBLIC_TIMELINE);
        return ss;
    }

    @Override
    public ArrayList<Status> replies(final int count, final int page,
            final String userId, final String sinceId, final String maxId,
            final String format, final String mode) throws ApiException {
        final ArrayList<Status> ss = fetchStatuses(
                ApiConfig.URL_TIMELINE_REPLIES, count, page, userId, sinceId,
                maxId, format, mode, Constants.TYPE_STATUSES_MENTIONS);
        return ss;
    }

    @Override
    public Search savedSearchesCreate(final String query) throws ApiException {
        if (StringHelper.isEmpty(query)) {
            if (AppContext.DEBUG) {
                throw new IllegalArgumentException("搜索词不能为空");
            }
            return null;
        }
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(ApiConfig.URL_SAVED_SEARCHES_CREATE).post();
        builder.param("query", query);

        final SimpleResponse response = fetch(builder.build());

        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("savedSearchCreate()---statusCode=" + statusCode);
        }

        try {
            return ApiParser.savedSearch(response);
        } catch (final IOException e) {
            throw new ApiException(ResponseCode.ERROR_IO_EXCEPTION,
                    e.getMessage());
        }
    }

    @Override
    public Search savedSearchesDelete(final int id) throws ApiException {
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(ApiConfig.URL_SAVED_SEARCHES_DESTROY).post()
                .id(String.valueOf(id));
        final SimpleResponse response = fetch(builder.build());
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("savedSearchDelete()---statusCode=" + statusCode);
        }

        try {
            return ApiParser.savedSearch(response);
        } catch (final IOException e) {
            throw new ApiException(ResponseCode.ERROR_IO_EXCEPTION,
                    e.getMessage());
        }
    }

    @Override
    public ArrayList<Search> savedSearchesList() throws ApiException {
        final SimpleResponse response = fetch(new SimpleRequest.Builder().url(
                ApiConfig.URL_SAVED_SEARCHES_LIST).build());

        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("savedSearchesList()---statusCode=" + statusCode);
        }

        try {
            return ApiParser.savedSearches(response);
        } catch (final IOException e) {
            throw new ApiException(ResponseCode.ERROR_IO_EXCEPTION,
                    e.getMessage());
        }

    }

    @Override
    public Search savedSearchesShow(final int id) throws ApiException {
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        final SimpleResponse response = fetch(builder
                .url(ApiConfig.URL_SAVED_SEARCHES_SHOW).param("id", id).build());

        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("savedSearchShow()---statusCode=" + statusCode);
        }

        try {
            return ApiParser.savedSearch(response);
        } catch (final IOException e) {
            throw new ApiException(ResponseCode.ERROR_IO_EXCEPTION,
                    e.getMessage());
        }

    }

    @Override
    public ArrayList<Status> search(final String keyword, final String sinceId,
            final String maxId, final int count, final String format,
            final String mode) throws ApiException {
        if (StringHelper.isEmpty(keyword)) {
            throw new IllegalArgumentException("搜索词不能为空");
        }

        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(ApiConfig.URL_SEARCH);
        builder.param("q", keyword);
        builder.maxId(maxId).sinceId(sinceId);
        builder.format("html").mode("lite");
        builder.count(count);
        final SimpleResponse response = fetch(builder.build());

        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("search()---statusCode=" + statusCode);
        }
        return Status.parseStatuses(response,
                Constants.TYPE_SEARCH_PUBLIC_TIMELINE);

    }

    @Override
    public ArrayList<User> searchUsers(final String keyword, final int count,
            final int page, final String mode) throws ApiException {
        if (StringHelper.isEmpty(keyword)) {
            if (AppContext.DEBUG) {
                throw new IllegalArgumentException("搜索词不能为空");
            }
            return null;
        }

        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(ApiConfig.URL_SEARCH_USERS);
        builder.param("q", keyword);
        builder.count(count).page(page);
        builder.format("html").mode("lite");
        builder.count(count);
        final SimpleResponse response = fetch(builder.build());

        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("search()---statusCode=" + statusCode);
        }
        return User.parseUsers(response);

    }

    @Override
    public Status statusesCreate(final String status,
            final String inReplyToStatusId, final String source,
            final String location, final String repostStatusId,
            final String format, final String mode) throws ApiException {
        if (StringHelper.isEmpty(status)) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        if (AppContext.DEBUG) {
            log("statusUpdate() ---[status=(" + status + ") replyToStatusId="
                    + inReplyToStatusId + " source=" + source + " location="
                    + location + " repostStatusId=" + repostStatusId + " ]");
        }

        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(ApiConfig.URL_STATUS_UPDATE).post();
        builder.status(status).location(location);
        builder.format(format).mode(mode);
        builder.param("in_reply_to_status_id", inReplyToStatusId);
        builder.param("repost_status_id", repostStatusId);
        builder.param("source", source);
        final SimpleResponse response = fetch(builder.build());
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("statusUpdate()---statusCode=" + statusCode);
        }
        final Status s = Status.parse(response,
                Constants.TYPE_STATUSES_HOME_TIMELINE);
        if (AppContext.DEBUG) {
            log("statusesCreate " + s);
        }
        return s;
    }

    @Override
    public Status statusesDelete(final String statusId, final String format,
            final String mode) throws ApiException {
        final String url = String
                .format(ApiConfig.URL_STATUS_DESTROY, statusId);
        final SimpleResponse response = doPostIdAction(url, null, format, mode);
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("statusDelete()---statusCode=" + statusCode + " url=" + url);
        }
        return Status.parse(response);
    }

    @Override
    public Status statusesShow(final String statusId, final String format,
            final String mode) throws ApiException {
        if (StringHelper.isEmpty(statusId)) {
            throw new IllegalArgumentException("消息ID不能为空");
        }
        if (AppContext.DEBUG) {
            log("statusShow()---statusId=" + statusId);
        }

        final String url = String.format(ApiConfig.URL_STATUS_SHOW, statusId);

        final SimpleResponse response = doGetIdAction(url, statusId, format,
                mode);
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("statusShow()---statusCode=" + statusCode);
        }
        final Status s = Status.parse(response);
        if (s != null) {
            CacheManager.put(s);
        }
        return s;
    }

    @Override
    public ArrayList<Search> trends() throws ApiException {
        final SimpleResponse response = fetch(new SimpleRequest.Builder().url(
                ApiConfig.URL_TRENDS_LIST).build());

        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("trends()---statusCode=" + statusCode);
        }

        try {
            return ApiParser.trends(response);
        } catch (final IOException e) {
            throw new ApiException(ResponseCode.ERROR_IO_EXCEPTION,
                    e.getMessage());
        }

    }

    @Override
    public User updateProfile(final String description, final String name,
            final String location, final String url, final String mode)
            throws ApiException {
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(ApiConfig.URL_ACCOUNT_UPDATE_PROFILE).post();
        builder.param("description", description);
        builder.param("name", name);
        builder.param("location", location);
        builder.param("url", url);
        final SimpleResponse response = fetch(builder.build());
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("updateProfile()---statusCode=" + statusCode);
        }
        return User.parse(response);
    }

    @Override
    public User updateProfileImage(final File image, final String mode)
            throws ApiException {
        final SimpleRequest.Builder builder = new SimpleRequest.Builder();
        builder.url(ApiConfig.URL_ACCOUNT_UPDATE_PROFILE_IMAGE).post();
        builder.param("image", image);
        final SimpleResponse response = fetch(builder.build());
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("updateProfileImage()---statusCode=" + statusCode);
        }
        return User.parse(response);
    }

    @Override
    public List<User> usersFollowers(final String userId, final int count,
            final int page, final String mode) throws ApiException {
        final List<User> users = fetchUsers(ApiConfig.URL_USERS_FOLLOWERS,
                userId, count, page);
        if ((users != null) && (users.size() > 0)) {
            for (final User user : users) {
                user.type = Constants.TYPE_USERS_FOLLOWERS;
                user.ownerId = (userId == null ? AppContext.getUserId()
                        : userId);
            }
        }
        return users;
    }

    @Override
    public ArrayList<User> usersFriends(final String userId, final int count,
            final int page, final String mode) throws ApiException {
        final ArrayList<User> users = fetchUsers(ApiConfig.URL_USERS_FRIENDS,
                userId, count, page);
        if ((users != null) && (users.size() > 0)) {
            for (final User user : users) {
                user.type = Constants.TYPE_USERS_FRIENDS;
                user.ownerId = (userId == null ? AppContext.getUserId()
                        : userId);
            }
        }
        return users;
    }

    @Override
    public User userShow(final String userId, final String mode)
            throws ApiException {
        final SimpleResponse response = doGetIdAction(ApiConfig.URL_USER_SHOW,
                userId, null, mode);
        final int statusCode = response.statusCode;
        if (AppContext.DEBUG) {
            log("userShow()---statusCode=" + statusCode);
        }

        // handlerResponseError(response);
        final User u = User.parse(response);
        if (u != null) {
            u.ownerId = AppContext.getUserId();
            CacheManager.put(u);
        }
        return u;
    }

    @Override
    public ArrayList<Status> userTimeline(final int count, final int page,
            final String userId, final String sinceId, final String maxId,
            final String format, final String mode) throws ApiException {
        if (TextUtils.isEmpty(userId)) {
            throw new NullPointerException(
                    "userTimeline() userId must not be empty or null.");
        }
        final ArrayList<Status> ss = fetchStatuses(ApiConfig.URL_TIMELINE_USER,
                count, page, userId, sinceId, maxId, format, mode,
                Constants.TYPE_STATUSES_USER_TIMELINE);
        return ss;
    }

    @Override
    public User verifyAccount(final String mode) throws ApiException {
        final SimpleResponse response = fetch(new SimpleRequest.Builder()
                .url(ApiConfig.URL_VERIFY_CREDENTIALS).mode(mode).build());
        return User.parse(response);
    }

}
