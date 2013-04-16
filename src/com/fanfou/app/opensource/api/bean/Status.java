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
package com.fanfou.app.opensource.api.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.api.ApiException;
import com.fanfou.app.opensource.api.ApiParser;
import com.fanfou.app.opensource.db.Contents.BasicColumns;
import com.fanfou.app.opensource.db.Contents.StatusInfo;
import com.fanfou.app.opensource.db.Contents.UserInfo;
import com.fanfou.app.opensource.http.ResponseCode;
import com.fanfou.app.opensource.http.SimpleResponse;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.util.PatternsHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.04.30
 * @version 1.1 2011.05.01
 * @version 1.2 2011.05.02
 * @version 1.3 2011.06.03
 * @version 1.4 2011.07.21
 * @version 1.5 2011.10.19
 * @version 1.6 2011.10.21
 * @version 1.7 2011.11.04
 * @version 2.0 2011.11.10
 * @version 2.1 2011.11.11
 * @version 2.2 2011.12.01
 * @version 2.3 2011.12.16
 * @version 2.4 2011.12.19
 * @version 3.0 2011.12.21
 * 
 */
public class Status implements Storable<Status> {

    public static final String TAG = Status.class.getSimpleName();

    private static void log(final String message) {
        Log.d(Status.TAG, message);
    }

    public String id;
    public String ownerId;
    public Date createdAt;

    public String text;
    public String simpleText;
    public String source;

    public String inReplyToStatusId;
    public String inReplyToUserId;
    public String inReplyToScreenName;

    public String photoImageUrl;
    public String photoThumbUrl;
    public String photoLargeUrl;

    public String userId;
    public String userScreenName;
    public String userProfileImageUrl;

    public boolean truncated;
    public boolean favorited;
    public boolean self;

    public boolean isRead;
    public boolean isThread;
    public boolean hasPhoto;
    public boolean special;

    public static final Parcelable.Creator<Status> CREATOR = new Parcelable.Creator<Status>() {

        @Override
        public Status createFromParcel(final Parcel source) {
            return new Status(source);
        }

        @Override
        public Status[] newArray(final int size) {
            return new Status[size];
        }
    };

    public static Status parse(final Cursor c) {
        if (c == null) {
            return null;
        }
        final Status s = new Status();

        s.id = ApiParser.parseString(c, BasicColumns.ID);
        s.ownerId = ApiParser.parseString(c, BasicColumns.OWNER_ID);
        s.createdAt = ApiParser.parseDate(c, BasicColumns.CREATED_AT);

        s.text = ApiParser.parseString(c, StatusInfo.TEXT);
        s.simpleText = ApiParser.parseString(c, StatusInfo.SIMPLE_TEXT);
        s.source = ApiParser.parseString(c, StatusInfo.SOURCE);

        s.inReplyToStatusId = ApiParser.parseString(c,
                StatusInfo.IN_REPLY_TO_STATUS_ID);
        s.inReplyToUserId = ApiParser.parseString(c,
                StatusInfo.IN_REPLY_TO_USER_ID);
        s.inReplyToScreenName = ApiParser.parseString(c,
                StatusInfo.IN_REPLY_TO_SCREEN_NAME);

        s.photoImageUrl = ApiParser.parseString(c, StatusInfo.PHOTO_IMAGE_URL);
        s.photoLargeUrl = ApiParser.parseString(c, StatusInfo.PHOTO_LARGE_URL);
        s.photoThumbUrl = ApiParser.parseString(c, StatusInfo.PHOTO_THUMB_URL);

        s.userId = ApiParser.parseString(c, StatusInfo.USER_ID);
        s.userScreenName = ApiParser
                .parseString(c, StatusInfo.USER_SCREEN_NAME);
        s.userProfileImageUrl = ApiParser.parseString(c,
                StatusInfo.USER_PROFILE_IMAGE_URL);

        s.truncated = ApiParser.parseBoolean(c, StatusInfo.TRUNCATED);
        s.favorited = ApiParser.parseBoolean(c, StatusInfo.FAVORITED);
        s.self = ApiParser.parseBoolean(c, StatusInfo.IS_SELF);

        s.isRead = ApiParser.parseBoolean(c, StatusInfo.IS_READ);
        s.isThread = ApiParser.parseBoolean(c, StatusInfo.IS_THREAD);
        s.hasPhoto = ApiParser.parseBoolean(c, StatusInfo.HAS_PHOTO);
        s.special = ApiParser.parseBoolean(c, StatusInfo.SPECIAL);

        s.type = ApiParser.parseInt(c, BasicColumns.TYPE);

        if (TextUtils.isEmpty(s.id)) {
            return null;
        }
        return s;

    }

    public static Status parse(final JSONObject o) throws ApiException {
        return Status.parse(o, Constants.TYPE_NONE);
    }

    public static Status parse(final JSONObject o, final int type)
            throws ApiException {
        if (o == null) {
            return null;
        }
        try {
            final Status s = new Status();

            s.id = o.getString(BasicColumns.ID);
            s.ownerId = AppContext.getUserId();
            s.createdAt = ApiParser.date(o.getString(BasicColumns.CREATED_AT));

            s.text = o.getString(StatusInfo.TEXT);
            s.simpleText = PatternsHelper.getSimpifiedText(s.text);
            s.source = ApiParser.parseSource(o.getString(StatusInfo.SOURCE));

            if (o.has(StatusInfo.IN_REPLY_TO_STATUS_ID)) {
                s.inReplyToStatusId = o
                        .getString(StatusInfo.IN_REPLY_TO_STATUS_ID);
                s.inReplyToUserId = o.getString(StatusInfo.IN_REPLY_TO_USER_ID);
                s.inReplyToScreenName = o
                        .getString(StatusInfo.IN_REPLY_TO_SCREEN_NAME);
                if (!TextUtils.isEmpty(s.inReplyToStatusId)) {
                    s.isThread = true;
                }
            }

            s.favorited = o.getBoolean(StatusInfo.FAVORITED);
            s.truncated = o.getBoolean(StatusInfo.TRUNCATED);
            s.self = o.getBoolean(StatusInfo.IS_SELF);

            s.isRead = false;
            s.special = false;

            if (o.has("photo")) {
                final JSONObject po = o.getJSONObject("photo");
                s.photo = Photo.parse(po);
                s.photoImageUrl = s.photo.imageUrl;
                s.photoLargeUrl = s.photo.largeUrl;
                s.photoThumbUrl = s.photo.thumbUrl;
                s.hasPhoto = true;
            }

            if (o.has("user")) {
                final JSONObject uo = o.getJSONObject("user");
                s.userId = uo.getString(BasicColumns.ID);
                s.userScreenName = uo.getString(UserInfo.SCREEN_NAME);
                s.userProfileImageUrl = uo
                        .getString(UserInfo.PROFILE_IMAGE_URL);
                s.user = User.parse(uo);
            }

            s.type = type;

            return s;
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            throw new ApiException(ResponseCode.ERROR_JSON_EXCEPTION,
                    e.getMessage(), e);
        }
    }

    public static Status parse(final SimpleResponse response)
            throws ApiException {
        JSONObject o = null;
        try {
            o = response.getJSONObject();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return Status.parse(o);
    }

    public static Status parse(final SimpleResponse response, final int type)
            throws ApiException {
        try {
            return Status.parse(response.getJSONObject(), type);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Status parse(final String content) throws ApiException {
        return Status.parse(content, Constants.TYPE_NONE);
    }

    public static Status parse(final String content, final int type)
            throws ApiException {
        try {
            final JSONObject o = new JSONObject(content);
            return Status.parse(o, type);
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            throw new ApiException(ResponseCode.ERROR_JSON_EXCEPTION,
                    e.getMessage(), e);
        }
    }

    public static ArrayList<Status> parseStatuses(final JSONArray a,
            final int type) throws ApiException {
        if (a == null) {
            return null;
        }
        if (AppContext.DEBUG) {
            Status.log("parseStatuses jsonarray.size=" + a.length());
        }
        try {
            final ArrayList<Status> statuses = new ArrayList<Status>();
            for (int i = 0; i < a.length(); i++) {
                final JSONObject o = a.getJSONObject(i);
                final Status s = Status.parse(o, type);
                statuses.add(s);
            }
            return statuses;
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            throw new ApiException(ResponseCode.ERROR_JSON_EXCEPTION,
                    e.getMessage(), e);
        }
    }

    public static ArrayList<Status> parseStatuses(final SimpleResponse r,
            final int type) throws ApiException {
        if (AppContext.DEBUG) {
            Status.log("parseStatuses response");
        }
        try {
            return Status.parseStatuses(r.getJSONArray(), type);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Status> parseStatuses(final String content,
            final int type) throws ApiException {
        if (AppContext.DEBUG) {
            Status.log("parseStatuses content");
        }
        JSONArray a;
        try {
            a = new JSONArray(content);
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            throw new ApiException(ResponseCode.ERROR_JSON_EXCEPTION,
                    e.getMessage(), e);
        }
        return Status.parseStatuses(a, type);
    }

    public int type;

    public User user;

    public Photo photo;

    public Status() {
    }

    public Status(final Parcel in) {
        this.id = in.readString();
        this.ownerId = in.readString();
        this.createdAt = new Date(in.readLong());
        this.type = in.readInt();

        this.text = in.readString();
        this.simpleText = in.readString();
        this.source = in.readString();

        this.inReplyToStatusId = in.readString();
        this.inReplyToUserId = in.readString();
        this.inReplyToScreenName = in.readString();

        this.photoImageUrl = in.readString();
        this.photoLargeUrl = in.readString();
        this.photoThumbUrl = in.readString();

        this.userId = in.readString();
        this.userScreenName = in.readString();
        this.userProfileImageUrl = in.readString();

        this.truncated = in.readInt() == 0 ? false : true;
        this.favorited = in.readInt() == 0 ? false : true;
        this.self = in.readInt() == 0 ? false : true;

        this.isRead = in.readInt() == 0 ? false : true;
        this.isThread = in.readInt() == 0 ? false : true;
        this.hasPhoto = in.readInt() == 0 ? false : true;

        this.special = in.readInt() == 0 ? false : true;

    }

    @Override
    public int compareTo(final Status another) {
        return this.createdAt.compareTo(another.createdAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof Status) {
            final Status s = (Status) o;
            if (this.id.equals(s.id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public boolean isNull() {
        return StringHelper.isEmpty(this.id);
    }

    public Status readFromParcel(final Parcel source) {
        return new Status(source);
    }

    @Override
    public ContentValues toContentValues() {
        final ContentValues cv = new ContentValues();

        cv.put(BasicColumns.ID, this.id);
        cv.put(BasicColumns.OWNER_ID, this.ownerId);
        cv.put(BasicColumns.CREATED_AT, this.createdAt.getTime());

        cv.put(StatusInfo.TEXT, this.text);
        cv.put(StatusInfo.SOURCE, this.source);
        cv.put(StatusInfo.SIMPLE_TEXT, this.simpleText);

        cv.put(StatusInfo.IN_REPLY_TO_STATUS_ID, this.inReplyToStatusId);
        cv.put(StatusInfo.IN_REPLY_TO_USER_ID, this.inReplyToUserId);
        cv.put(StatusInfo.IN_REPLY_TO_SCREEN_NAME, this.inReplyToScreenName);

        cv.put(StatusInfo.PHOTO_IMAGE_URL, this.photoImageUrl);
        cv.put(StatusInfo.PHOTO_THUMB_URL, this.photoThumbUrl);
        cv.put(StatusInfo.PHOTO_LARGE_URL, this.photoLargeUrl);

        cv.put(StatusInfo.USER_ID, this.userId);
        cv.put(StatusInfo.USER_SCREEN_NAME, this.userScreenName);
        cv.put(StatusInfo.USER_PROFILE_IMAGE_URL, this.userProfileImageUrl);

        cv.put(StatusInfo.TRUNCATED, this.truncated);
        cv.put(StatusInfo.FAVORITED, this.favorited);
        cv.put(StatusInfo.IS_SELF, this.self);

        cv.put(StatusInfo.IS_READ, this.isRead);
        cv.put(StatusInfo.IS_THREAD, this.isThread);
        cv.put(StatusInfo.HAS_PHOTO, this.hasPhoto);
        cv.put(StatusInfo.SPECIAL, this.special);

        cv.put(BasicColumns.TYPE, this.type);

        return cv;
    }

    @Override
    public String toString() {
        // return toContentValues().toString();
        return "[Status] " + BasicColumns.ID + "=" + this.id + " "
                + StatusInfo.TEXT + "=" + this.text + " "
                + BasicColumns.CREATED_AT + "+" + this.createdAt + " "
                // +StatusInfo.SOURCE+"="+this.source+" "
                // +StatusInfo.TRUNCATED+"="+this.truncated+" "
                // +StatusInfo.IN_REPLY_TO_STATUS_ID+"="+this.inReplyToStatusId+" "
                // +StatusInfo.IN_REPLY_TO_USER_ID+"="+this.inReplyToUserId+" "
                // +StatusInfo.FAVORITED+"="+this.favorited+" "
                // +StatusInfo.IN_REPLY_TO_SCREEN_NAME+"="+this.inReplyToScreenName+" "
                // +StatusInfo.PHOTO_IMAGE_URL+"="+this.photoImageUrl+" "
                // +StatusInfo.PHOTO_LARGE_URL+"="+this.photoLargeUrl+" "
                // +StatusInfo.PHOTO_THUMB_URL+"="+this.photoThumbUrl+" "
                + StatusInfo.USER_ID + "=" + this.userId + " ";
        // +StatusInfo.USER_SCREEN_NAME+"="+this.userScreenName+" "
        // +StatusInfo.READ+"="+this.read+" "
        // +StatusInfo.TYPE+"="+this.type+" ";
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(this.id);
        dest.writeString(this.ownerId);
        dest.writeLong(this.createdAt.getTime());
        dest.writeInt(this.type);

        dest.writeString(this.text);
        dest.writeString(this.simpleText);
        dest.writeString(this.source);

        dest.writeString(this.inReplyToStatusId);
        dest.writeString(this.inReplyToUserId);
        dest.writeString(this.inReplyToScreenName);

        dest.writeString(this.photoImageUrl);
        dest.writeString(this.photoLargeUrl);
        dest.writeString(this.photoThumbUrl);

        dest.writeString(this.userId);
        dest.writeString(this.userScreenName);
        dest.writeString(this.userProfileImageUrl);

        dest.writeInt(this.truncated ? 1 : 0);
        dest.writeInt(this.favorited ? 1 : 0);
        dest.writeInt(this.self ? 1 : 0);

        dest.writeInt(this.isRead ? 1 : 0);
        dest.writeInt(this.isThread ? 1 : 0);
        dest.writeInt(this.hasPhoto ? 1 : 0);

        dest.writeInt(this.special ? 1 : 0);

    }

}
