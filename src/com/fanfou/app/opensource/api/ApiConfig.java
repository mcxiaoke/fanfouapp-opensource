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

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.02
 * @version 1.1 2011.05.04
 * @version 1.2 2011.05.15
 * @version 1.3 2011.10.18
 * @version 1.4 2011.11.04
 * @version 1.5 2011.11.07
 * @version 1.6 2011.11.07
 * @version 1.7 2011.11.11
 * @version 2.0 2011.11.18
 * @version 3.0 2011.11.21
 * 
 */
interface ApiConfig {

    public static final String HOST = "http://fanfou.com/";
    public static final String API_BASE = "http://api.fanfou.com";
    public static final String EXTENSION = ".json";

    // verify account
    public static final String URL_VERIFY_CREDENTIALS = ApiConfig.API_BASE
            + "/account/verify_credentials" + ApiConfig.EXTENSION;

    // register
    public static final String URL_REGISTER = ApiConfig.API_BASE
            + "/account/register" + ApiConfig.EXTENSION;

    // public timeline
    // param count -- 1-20
    // param format -- format=html
    public static final String URL_TIMELINE_PUBLIC = ApiConfig.API_BASE
            + "/statuses/public_timeline" + ApiConfig.EXTENSION;

    // param id -- userid
    // param count -- 1-20
    // param since_id
    // param max_id
    // param page 1~
    // param format -- format=html
    // home timeline
    public static final String URL_TIMELINE_HOME = ApiConfig.API_BASE
            + "/statuses/home_timeline" + ApiConfig.EXTENSION;

    // show home timeline/replies/mentions
    public static final String URL_TIMELINE_USER = ApiConfig.API_BASE
            + "/statuses/user_timeline" + ApiConfig.EXTENSION;
    public static final String URL_TIMELINE_REPLIES = ApiConfig.API_BASE
            + "/statuses/replies" + ApiConfig.EXTENSION;
    public static final String URL_TIMELINE_MENTIONS = ApiConfig.API_BASE
            + "/statuses/mentions" + ApiConfig.EXTENSION;

    public static final String URL_TIMELINE_CONTEXT = ApiConfig.API_BASE
            + "/statuses/context_timeline" + ApiConfig.EXTENSION;

    // timeline contains photos
    public static final String URL_TIMELINE_PHOTOS = ApiConfig.API_BASE
            + "/photos/user_timeline" + ApiConfig.EXTENSION;

    // show a status, param id -- status id
    public static final String URL_STATUS_SHOW = ApiConfig.API_BASE
            + "/statuses/show/%s" + ApiConfig.EXTENSION;

    // post a status
    // param status -- status content
    // param in_reply_to_status_id -- reply a status
    // param source -- api source
    // param location -- location string or latitude
    // param repost_status_id -- only for repost
    // POST METHOD
    public static final String URL_STATUS_UPDATE = ApiConfig.API_BASE
            + "/statuses/update" + ApiConfig.EXTENSION;

    // delete a status
    // param id -- status id
    // POST METHOD
    public static final String URL_STATUS_DESTROY = ApiConfig.API_BASE
            + "/statuses/destroy/%s" + ApiConfig.EXTENSION;

    // photo upload
    // param photo -- photo file
    // param status -- photo description
    // param source -- api source
    // param location -- optional
    // POST METHOD
    public static final String URL_PHOTO_UPLOAD = ApiConfig.API_BASE
            + "/photos/upload" + ApiConfig.EXTENSION;

    // user timeline only contains photos
    public static final String URL_PHOTO_USER_TIMELINE = ApiConfig.API_BASE
            + "/photos/user_timeline" + ApiConfig.EXTENSION;

    // search for public timeline
    // param q -- search keywords
    // param max_id -- max status id
    public static final String URL_SEARCH = ApiConfig.API_BASE
            + "/search/public_timeline" + ApiConfig.EXTENSION;

    public static final String URL_SEARCH_USERS = ApiConfig.API_BASE
            + "/search/users" + ApiConfig.EXTENSION;

    public static final String URL_TRENDS_LIST = ApiConfig.API_BASE
            + "/trends/list" + ApiConfig.EXTENSION;

    // show saved searches list
    public static final String URL_SAVED_SEARCHES_LIST = ApiConfig.API_BASE
            + "/saved_searches/list" + ApiConfig.EXTENSION;

    // show item in saved searches
    // param id -- keyword id
    public static final String URL_SAVED_SEARCHES_SHOW = ApiConfig.API_BASE
            + "/saved_searches/show" + ApiConfig.EXTENSION;

    // create a saved search
    // param query -- keyword to save
    public static final String URL_SAVED_SEARCHES_CREATE = ApiConfig.API_BASE
            + "/saved_searches/create" + ApiConfig.EXTENSION;

    // remove a saved search
    // param id -- saved search item id
    // POST METHOD
    public static final String URL_SAVED_SEARCHES_DESTROY = ApiConfig.API_BASE
            + "/saved_searches/destroy" + ApiConfig.EXTENSION;

    // show friends or followers or user profile
    // param id -- user id,optional
    public static final String URL_USERS_FRIENDS = ApiConfig.API_BASE
            + "/users/friends" + ApiConfig.EXTENSION;
    public static final String URL_USERS_FOLLOWERS = ApiConfig.API_BASE
            + "/users/followers" + ApiConfig.EXTENSION;
    public static final String URL_USER_SHOW = ApiConfig.API_BASE
            + "/users/show" + ApiConfig.EXTENSION;

    // add or delete a friend
    // param id -- userid
    // POST METHOD
    public static final String URL_FRIENDSHIPS_CREATE = ApiConfig.API_BASE
            + "/friendships/create/%s" + ApiConfig.EXTENSION;
    public static final String URL_FRIENDSHIPS_DESTROY = ApiConfig.API_BASE
            + "/friendships/destroy/%s" + ApiConfig.EXTENSION;

    // friendships exists?
    // param user_a -- user id
    // param user_b -- user_id
    public static final String URL_FRIENDSHIS_EXISTS = ApiConfig.API_BASE
            + "/friendships/exists" + ApiConfig.EXTENSION;

    // friends ids or followers ids
    // param id -- userid
    public static final String URL_USERS_FRIENDS_IDS = ApiConfig.API_BASE
            + "/friends/ids" + ApiConfig.EXTENSION;
    public static final String URL_USERS_FOLLOWERS_IDS = ApiConfig.API_BASE
            + "/followers/ids" + ApiConfig.EXTENSION;

    // show direct messages in outbox and inbox
    // param count -- 1-20
    // param since_id
    // param max_id
    // param page
    public static final String URL_DIRECT_MESSAGES_INBOX = ApiConfig.API_BASE
            + "/direct_messages/inbox" + ApiConfig.EXTENSION;

    public static final String URL_DIRECT_MESSAGES_OUTBOX = ApiConfig.API_BASE
            + "/direct_messages/sent" + ApiConfig.EXTENSION;

    public static final String URL_DIRECT_MESSAGES_CONVERSATION_LIST = ApiConfig.API_BASE
            + "/direct_messages/conversation_list" + ApiConfig.EXTENSION;

    public static final String URL_DIRECT_MESSAGES_CONVERSATION = ApiConfig.API_BASE
            + "/direct_messages/conversation" + ApiConfig.EXTENSION;

    // send direct message
    // param user -- recipient user id
    // param text -- message content
    // param in_reply_to_id -- in reply to a message
    // POST METHOD
    public static final String URL_DIRECT_MESSAGES_NEW = ApiConfig.API_BASE
            + "/direct_messages/new" + ApiConfig.EXTENSION;

    // delete a message
    // param id -- message id
    // POST METHOD
    public static final String URL_DIRECT_MESSAGES_DESTROY = ApiConfig.API_BASE
            + "/direct_messages/destroy/%s" + ApiConfig.EXTENSION;

    // show favorites
    // param id -- userid
    // param count
    // param page
    public static final String URL_FAVORITES_LIST = ApiConfig.API_BASE
            + "/favorites/list" + ApiConfig.EXTENSION;

    // favorite or unfavorite a status
    // param id -- status id
    // POST METHOD
    public static final String URL_FAVORITES_CREATE = ApiConfig.API_BASE
            + "/favorites/create/%s" + ApiConfig.EXTENSION;
    public static final String URL_FAVORITES_DESTROY = ApiConfig.API_BASE
            + "/favorites/destroy/%s" + ApiConfig.EXTENSION;

    // add or remove in blocks
    // param id --userid
    // POST METHOD
    public static final String URL_BLOCKS_CREATE = ApiConfig.API_BASE
            + "/blocks/create/%s" + ApiConfig.EXTENSION;
    public static final String URL_BLOCKS_DESTROY = ApiConfig.API_BASE
            + "/blocks/destroy/%s" + ApiConfig.EXTENSION;

    // show blocking list
    // param count count 0-60, default is 20
    // param page page >=0
    public static final String URL_BLOCKS_USERS = ApiConfig.API_BASE
            + "/blocks/blocking" + ApiConfig.EXTENSION;

    public static final String URL_BLOCKS_IDS = ApiConfig.API_BASE
            + "/blocks/ids" + ApiConfig.EXTENSION;

    // show user is or not in my blocks
    // param id userId
    // POST METHOD
    public static final String URL_BLOCKS_EXISTS = ApiConfig.API_BASE
            + "/blocks/exists" + ApiConfig.EXTENSION;

    // update my profile
    // param url
    // param location
    // param description
    // param name realname
    // param email email
    // POST METHOD
    public static final String URL_ACCOUNT_UPDATE_PROFILE = ApiConfig.API_BASE
            + "/account/update_profile" + ApiConfig.EXTENSION;

    // update my profile image
    // param image photo file
    // POST METHOD
    public static final String URL_ACCOUNT_UPDATE_PROFILE_IMAGE = ApiConfig.API_BASE
            + "/account/update_profile_image" + ApiConfig.EXTENSION;

}
