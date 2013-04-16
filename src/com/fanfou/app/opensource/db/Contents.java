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
package com.fanfou.app.opensource.db;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.01
 * @version 2.0 2011.07.04
 * @version 3.0 2011.09.10
 * @version 4.0 2011.09.26
 * @version 5.0 2011.10.20
 * @version 5.1 2011.10.25
 * @version 5.5 2011.10.26
 * @version 5.6 2011.11.07
 * @version 6.0 2011.11.10
 * @version 6.1 2011.11.11
 * 
 */
public final class Contents {

    public static interface BasicColumns extends BaseColumns {
        public static final String ID = "id";
        public static final String OWNER_ID = "owner_id";
        public static final String CREATED_AT = "created_at";
        public static final String TYPE = "type";
    }

    public static interface DirectMessageInfo extends BasicColumns {
        public static final String TABLE_NAME = "messages";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Contents.AUTHORITY + "/" + DirectMessageInfo.TABLE_NAME);
        public static final String URI_PATH = DirectMessageInfo.TABLE_NAME;
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.fanfou.message";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.fanfou.message";

        public static final String TEXT = "text";
        public static final String SENDER_ID = "sender_id";
        public static final String SENDER_SCREEN_NAME = "sender_screen_name";
        public static final String RECIPIENT_ID = "recipient_id";
        public static final String RECIPIENT_SCREEN_NAME = "recipient_screen_name";
        public static final String SENDER_PROFILE_IMAGE_URL = "sender_profile_image_url";
        public static final String RECIPIENT_PROFILE_IMAGE_URL = "recipient_profile_image_url";

        public static final String THREAD_USER_ID = "thread_user_id";
        public static final String THREAD_USER_NAME = "thread_user_name";
        public static final String IS_READ = "is_read";

        public static final String COLUMNS[] = { BaseColumns._ID,
                BasicColumns.ID, BasicColumns.OWNER_ID, DirectMessageInfo.TEXT,
                BasicColumns.CREATED_AT, DirectMessageInfo.SENDER_ID,
                DirectMessageInfo.SENDER_SCREEN_NAME,
                DirectMessageInfo.RECIPIENT_ID,
                DirectMessageInfo.RECIPIENT_SCREEN_NAME,
                DirectMessageInfo.SENDER_PROFILE_IMAGE_URL,
                DirectMessageInfo.RECIPIENT_PROFILE_IMAGE_URL,
                BasicColumns.TYPE,

                DirectMessageInfo.THREAD_USER_ID,
                DirectMessageInfo.THREAD_USER_NAME, DirectMessageInfo.IS_READ,

        };

        public static final String CREATE_TABLE = "create table "
                + DirectMessageInfo.TABLE_NAME + " (" + BaseColumns._ID
                + " integer primary key autoincrement, " + BasicColumns.ID
                + " text not null, " + BasicColumns.OWNER_ID + " text , "
                + DirectMessageInfo.TEXT + " text not null, "
                + BasicColumns.CREATED_AT + " integer not null, "
                + DirectMessageInfo.SENDER_ID + " text not null, "
                + DirectMessageInfo.SENDER_SCREEN_NAME + " text not null, "
                + DirectMessageInfo.RECIPIENT_ID + " text not null, "
                + DirectMessageInfo.RECIPIENT_SCREEN_NAME + " text not null, "
                + DirectMessageInfo.SENDER_PROFILE_IMAGE_URL
                + " text not null, "
                + DirectMessageInfo.RECIPIENT_PROFILE_IMAGE_URL
                + " text not null, " + BasicColumns.TYPE
                + " integer not null, " + DirectMessageInfo.THREAD_USER_ID
                + " text , " + DirectMessageInfo.THREAD_USER_NAME + " text , "
                + DirectMessageInfo.IS_READ + " boolean not null, "

                + "unique ( " + BasicColumns.ID + " ) on conflict ignore );";
    }

    public static interface DraftInfo extends BaseColumns {

        public static final String TABLE_NAME = "drafts";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Contents.AUTHORITY + "/" + DraftInfo.TABLE_NAME);
        public static final String URI_PATH = DraftInfo.TABLE_NAME;
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.fanfou.draft";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.fanfou.draft";

        public static final String OWNER_ID = "owner_id";
        public static final String TYPE = "type";
        public static final String TEXT = "text";
        public static final String CREATED_AT = "created_at";
        public static final String REPLY_TO = "reply";
        public static final String FILE_PATH = "file_path";

        public static final String COLUMNS[] = { BaseColumns._ID,
                DraftInfo.OWNER_ID, DraftInfo.TEXT, DraftInfo.CREATED_AT,
                DraftInfo.TYPE, DraftInfo.REPLY_TO, DraftInfo.FILE_PATH, };

        public static final String CREATE_TABLE = "create table "
                + DraftInfo.TABLE_NAME + " (" + BaseColumns._ID
                + " integer primary key autoincrement, " + DraftInfo.OWNER_ID
                + " text , " + DraftInfo.TEXT + " text not null, "
                + DraftInfo.CREATED_AT + " integer not null, " + DraftInfo.TYPE
                + " integer not null, " + DraftInfo.REPLY_TO + " text , "
                + DraftInfo.FILE_PATH + " text , " + "unique ( "
                + BaseColumns._ID + " ) on conflict ignore );";

    }

    public static interface StatusInfo extends BasicColumns {
        public static final String TABLE_NAME = "statuses";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Contents.AUTHORITY + "/" + StatusInfo.TABLE_NAME);
        public static final String URI_PATH = StatusInfo.TABLE_NAME;
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.fanfou.status";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.fanfou.status";

        public static final String TEXT = "text";
        public static final String SIMPLE_TEXT = "simple_text";
        public static final String SOURCE = "source";

        public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
        public static final String IN_REPLY_TO_USER_ID = "in_reply_to_user_id";
        public static final String IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";

        public static final String PHOTO_IMAGE_URL = "imageurl";
        public static final String PHOTO_THUMB_URL = "thumburl";
        public static final String PHOTO_LARGE_URL = "largeurl";

        public static final String USER_ID = "user_id";
        public static final String USER_SCREEN_NAME = "user_screen_name";
        public static final String USER_PROFILE_IMAGE_URL = "user_profile_image_url";

        public static final String TRUNCATED = "truncated";
        public static final String FAVORITED = "favorited";
        public static final String IS_SELF = "is_self";

        public static final String IS_READ = "is_read";
        public static final String IS_THREAD = "is_thread";
        public static final String HAS_PHOTO = "has_photo";
        public static final String SPECIAL = "special";

        public static final String COLUMNS[] = { BaseColumns._ID,

        BasicColumns.ID, BasicColumns.OWNER_ID, BasicColumns.CREATED_AT,

        StatusInfo.TEXT, StatusInfo.SIMPLE_TEXT, StatusInfo.SOURCE,

        StatusInfo.IN_REPLY_TO_STATUS_ID, StatusInfo.IN_REPLY_TO_USER_ID,
                StatusInfo.IN_REPLY_TO_SCREEN_NAME,

                StatusInfo.PHOTO_IMAGE_URL, StatusInfo.PHOTO_THUMB_URL,
                StatusInfo.PHOTO_LARGE_URL,

                StatusInfo.USER_ID, StatusInfo.USER_SCREEN_NAME,
                StatusInfo.USER_PROFILE_IMAGE_URL,

                StatusInfo.TRUNCATED, StatusInfo.FAVORITED, StatusInfo.IS_SELF,

                StatusInfo.IS_READ, StatusInfo.IS_THREAD, StatusInfo.HAS_PHOTO,
                StatusInfo.SPECIAL,

                BasicColumns.TYPE, };

        static final String STATUS_SQL = " (" + BaseColumns._ID
                + " integer primary key autoincrement, "

                + BasicColumns.ID + " text not null, " + BasicColumns.OWNER_ID
                + " text, " + BasicColumns.CREATED_AT + " integer not null, "

                + StatusInfo.TEXT + " text not null, " + StatusInfo.SIMPLE_TEXT
                + " text not null, " + StatusInfo.SOURCE + " text not null, "

                + StatusInfo.IN_REPLY_TO_STATUS_ID + " text, "
                + StatusInfo.IN_REPLY_TO_USER_ID + " text, "
                + StatusInfo.IN_REPLY_TO_SCREEN_NAME + " text, "

                + StatusInfo.PHOTO_IMAGE_URL + " text, "
                + StatusInfo.PHOTO_THUMB_URL + " text, "
                + StatusInfo.PHOTO_LARGE_URL + " text, "

                + StatusInfo.USER_ID + " text not null, "
                + StatusInfo.USER_SCREEN_NAME + " text not null, "
                + StatusInfo.USER_PROFILE_IMAGE_URL + " text not null, "

                + StatusInfo.TRUNCATED + " boolean not null, "
                + StatusInfo.FAVORITED + " boolean not null, "
                + StatusInfo.IS_SELF + " boolean not null, "

                + StatusInfo.IS_READ + " boolean not null, "
                + StatusInfo.IS_THREAD + " boolean not null, "
                + StatusInfo.HAS_PHOTO + " boolean not null, "
                + StatusInfo.SPECIAL + " boolean not null, "

                + BasicColumns.TYPE + " integer not null, "

                + "unique ( " + BasicColumns.ID + "," + BasicColumns.TYPE + ","
                + BasicColumns.OWNER_ID + ") on conflict ignore );";

        public static final String CREATE_TABLE = "create table "
                + StatusInfo.TABLE_NAME + StatusInfo.STATUS_SQL;
    }

    public static interface UserInfo extends BasicColumns {
        public static final String TABLE_NAME = "users";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Contents.AUTHORITY + "/" + UserInfo.TABLE_NAME);
        public static final String URI_PATH = UserInfo.TABLE_NAME;
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.fanfou.user";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.fanfou.user";

        public static final String SCREEN_NAME = "screen_name";
        public static final String LOCATION = "location";
        public static final String GENDER = "gender";
        public static final String BIRTHDAY = "birthday";
        public static final String DESCRIPTION = "description";
        public static final String PROFILE_IMAGE_URL = "profile_image_url";
        public static final String URL = "url";
        public static final String PROTECTED = "protected";
        public static final String FOLLOWERS_COUNT = "followers_count";
        public static final String FRIENDS_COUNT = "friends_count";
        public static final String FAVORITES_COUNT = "favourites_count";
        public static final String STATUSES_COUNT = "statuses_count";
        public static final String FOLLOWING = "following";

        public static final String COLUMNS[] = { BaseColumns._ID,
                BasicColumns.ID, BasicColumns.OWNER_ID, UserInfo.SCREEN_NAME,
                UserInfo.LOCATION, UserInfo.GENDER, UserInfo.BIRTHDAY,
                UserInfo.DESCRIPTION, UserInfo.PROFILE_IMAGE_URL, UserInfo.URL,
                UserInfo.PROTECTED, UserInfo.FOLLOWERS_COUNT,
                UserInfo.FRIENDS_COUNT, UserInfo.FAVORITES_COUNT,
                UserInfo.STATUSES_COUNT, UserInfo.FOLLOWING,
                BasicColumns.CREATED_AT, BasicColumns.TYPE, };

        public static final String CREATE_TABLE = "create table "
                + UserInfo.TABLE_NAME + " ( " + BaseColumns._ID
                + " integer primary key autoincrement, " + BasicColumns.ID
                + " text not null, " + BasicColumns.OWNER_ID + " text , "
                + UserInfo.SCREEN_NAME + " text not null, " + UserInfo.LOCATION
                + " text not null, " + UserInfo.GENDER + " text not null, "
                + UserInfo.BIRTHDAY + " text not null, " + UserInfo.DESCRIPTION
                + " text not null, " + UserInfo.PROFILE_IMAGE_URL
                + " text not null, " + UserInfo.URL + " text not null, "
                + UserInfo.PROTECTED + " boolean not null, "
                + UserInfo.FOLLOWERS_COUNT + " integer not null, "
                + UserInfo.FRIENDS_COUNT + " integer not null, "
                + UserInfo.FAVORITES_COUNT + " integer not null, "
                + UserInfo.STATUSES_COUNT + " integer not null, "
                + UserInfo.FOLLOWING + " boolean not null, "
                + BasicColumns.CREATED_AT + " integer not null, "
                + BasicColumns.TYPE + " integer not null, " + "unique ( "
                + BasicColumns.ID + "," + BasicColumns.TYPE
                + " ) on conflict ignore );";
        // + "unique ( "+ID+","+TYPE+","+OWNER_ID+") on conflict ignore );";
    }

    public static final String AUTHORITY = "com.fanfou.app.opensource.provider";

}
