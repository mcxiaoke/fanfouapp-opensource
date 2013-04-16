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

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.api.ApiException;
import com.fanfou.app.opensource.api.ApiParser;
import com.fanfou.app.opensource.db.Contents.BasicColumns;
import com.fanfou.app.opensource.db.Contents.UserInfo;
import com.fanfou.app.opensource.http.ResponseCode;
import com.fanfou.app.opensource.http.SimpleResponse;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.04.30
 * @version 1.1 2011.05.01
 * @version 1.2 2011.05.02
 * @version 1.3 2011.07.21
 * @version 1.4 2011.10.21
 * @version 1.5 2011.11.04
 * @version 2.0 2011.11.10
 * @version 2.1 2011.11.11
 * @version 2.5 2011.11.15
 * @version 2.6 2011.12.16
 * @version 2.7 2011.12.19
 * @version 3.0 2011.12.21
 * 
 */
public class User implements Storable<User> {

    public static final String TAG = User.class.getSimpleName();

    public Date createdAt;
    public String id;
    public String ownerId;

    public String screenName;
    public String location;
    public String gender;
    public String birthday;

    public String description;
    public String profileImageUrl;
    public String url;
    public boolean protect;

    public int followersCount;
    public int friendsCount;
    public int favouritesCount;
    public int statusesCount;

    public boolean following;

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

        @Override
        public User createFromParcel(final Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(final int size) {
            return new User[size];
        }
    };

    public static User parse(final Cursor c) {
        if (c == null) {
            return null;
        }
        final User user = new User();
        user.createdAt = ApiParser.parseDate(c, BasicColumns.CREATED_AT);
        user.id = ApiParser.parseString(c, BasicColumns.ID);
        user.ownerId = ApiParser.parseString(c, BasicColumns.OWNER_ID);
        user.screenName = ApiParser.parseString(c, UserInfo.SCREEN_NAME);
        user.location = ApiParser.parseString(c, UserInfo.LOCATION);
        user.gender = ApiParser.parseString(c, UserInfo.GENDER);
        user.birthday = ApiParser.parseString(c, UserInfo.BIRTHDAY);
        user.description = ApiParser.parseString(c, UserInfo.DESCRIPTION);
        user.profileImageUrl = ApiParser.parseString(c,
                UserInfo.PROFILE_IMAGE_URL);
        user.url = ApiParser.parseString(c, UserInfo.URL);
        user.protect = ApiParser.parseBoolean(c, UserInfo.PROTECTED);
        user.followersCount = ApiParser.parseInt(c, UserInfo.FOLLOWERS_COUNT);
        user.friendsCount = ApiParser.parseInt(c, UserInfo.FRIENDS_COUNT);
        user.favouritesCount = ApiParser.parseInt(c, UserInfo.FAVORITES_COUNT);
        user.statusesCount = ApiParser.parseInt(c, UserInfo.STATUSES_COUNT);
        user.following = ApiParser.parseBoolean(c, UserInfo.FOLLOWING);
        user.type = ApiParser.parseInt(c, BasicColumns.TYPE);
        return user;
    }

    public static User parse(final JSONObject o) throws ApiException {
        if (null == o) {
            return null;
        }
        try {
            final User user = new User();
            user.id = o.getString(BasicColumns.ID);
            user.screenName = o.getString(UserInfo.SCREEN_NAME);
            user.location = o.getString(UserInfo.LOCATION);
            user.gender = o.getString(UserInfo.GENDER);
            user.birthday = o.getString(UserInfo.BIRTHDAY);
            user.description = o.getString(UserInfo.DESCRIPTION);
            user.profileImageUrl = o.getString(UserInfo.PROFILE_IMAGE_URL);
            user.url = o.getString(UserInfo.URL);
            user.protect = o.getBoolean(UserInfo.PROTECTED);
            user.followersCount = o.getInt(UserInfo.FOLLOWERS_COUNT);
            user.friendsCount = o.getInt(UserInfo.FRIENDS_COUNT);
            user.favouritesCount = o.getInt(UserInfo.FAVORITES_COUNT);
            user.statusesCount = o.getInt(UserInfo.STATUSES_COUNT);
            user.following = o.getBoolean(UserInfo.FOLLOWING);
            user.createdAt = ApiParser.date(o
                    .getString(BasicColumns.CREATED_AT));

            user.type = Constants.TYPE_NONE;
            user.ownerId = AppContext.getUserId();
            return user;
        } catch (final JSONException e) {
            throw new ApiException(ResponseCode.ERROR_JSON_EXCEPTION,
                    e.getMessage(), e);
        }
    }

    public static User parse(final SimpleResponse r) throws ApiException {
        try {
            return User.parse(r.getJSONObject());
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<User> parseUsers(final JSONArray a)
            throws ApiException {
        if (a == null) {
            return null;
        }
        final ArrayList<User> users = new ArrayList<User>();
        try {
            for (int i = 0; i < a.length(); i++) {
                final JSONObject o = a.getJSONObject(i);
                final User u = User.parse(o);
                users.add(u);
            }
        } catch (final JSONException e) {
            throw new ApiException(ResponseCode.ERROR_JSON_EXCEPTION, e);
        }
        return users;
    }

    public static ArrayList<User> parseUsers(final SimpleResponse r)
            throws ApiException {
        try {
            return User.parseUsers(r.getJSONArray());
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int type;

    public User() {
    }

    public User(final Parcel in) {
        this.id = in.readString();
        this.ownerId = in.readString();
        this.createdAt = new Date(in.readLong());
        this.type = in.readInt();

        this.screenName = in.readString();
        this.location = in.readString();
        this.gender = in.readString();
        this.birthday = in.readString();

        this.description = in.readString();
        this.profileImageUrl = in.readString();
        this.url = in.readString();
        this.protect = in.readInt() == 0 ? false : true;

        this.followersCount = in.readInt();
        this.friendsCount = in.readInt();
        this.favouritesCount = in.readInt();
        this.statusesCount = in.readInt();

        this.following = in.readInt() == 0 ? false : true;
    }

    @Override
    public int compareTo(final User another) {
        return this.createdAt.compareTo(another.createdAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof User) {
            final User u = (User) o;
            if (this.id.equals(u.id)) {
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

    @Override
    public ContentValues toContentValues() {
        final User u = this;
        final ContentValues cv = new ContentValues();

        cv.put(BasicColumns.ID, u.id);
        cv.put(BasicColumns.OWNER_ID, u.ownerId);

        cv.put(UserInfo.SCREEN_NAME, u.screenName);
        cv.put(UserInfo.LOCATION, u.location);
        cv.put(UserInfo.GENDER, u.gender);
        cv.put(UserInfo.BIRTHDAY, u.birthday);

        cv.put(UserInfo.DESCRIPTION, u.description);
        cv.put(UserInfo.PROFILE_IMAGE_URL, u.profileImageUrl);
        cv.put(UserInfo.URL, u.url);
        cv.put(UserInfo.PROTECTED, u.protect);

        cv.put(UserInfo.FOLLOWERS_COUNT, u.followersCount);
        cv.put(UserInfo.FRIENDS_COUNT, u.friendsCount);
        cv.put(UserInfo.FAVORITES_COUNT, u.favouritesCount);
        cv.put(UserInfo.STATUSES_COUNT, u.statusesCount);

        cv.put(UserInfo.FOLLOWING, u.following);
        cv.put(BasicColumns.CREATED_AT, u.createdAt.getTime());

        cv.put(BasicColumns.TYPE, u.type);

        return cv;
    }

    public ContentValues toSimpleContentValues() {
        final User u = this;
        final ContentValues cv = new ContentValues();

        cv.put(UserInfo.SCREEN_NAME, u.screenName);
        cv.put(UserInfo.LOCATION, u.location);
        cv.put(UserInfo.GENDER, u.gender);
        cv.put(UserInfo.BIRTHDAY, u.birthday);

        cv.put(UserInfo.DESCRIPTION, u.description);
        cv.put(UserInfo.PROFILE_IMAGE_URL, u.profileImageUrl);
        cv.put(UserInfo.URL, u.url);
        cv.put(UserInfo.PROTECTED, u.protect);

        cv.put(UserInfo.FOLLOWERS_COUNT, u.followersCount);
        cv.put(UserInfo.FRIENDS_COUNT, u.friendsCount);
        cv.put(UserInfo.FAVORITES_COUNT, u.favouritesCount);
        cv.put(UserInfo.STATUSES_COUNT, u.statusesCount);

        cv.put(UserInfo.FOLLOWING, u.following);

        return cv;
    }

    @Override
    public String toString() {
        return "[User] " + BasicColumns.ID + "=" + this.id + " "
                + UserInfo.SCREEN_NAME + "=" + this.screenName + " "
                + UserInfo.LOCATION + "=" + this.location + " "
                + UserInfo.GENDER + "=" + this.gender + " " + UserInfo.BIRTHDAY
                + "=" + this.birthday + " " + UserInfo.DESCRIPTION + "="
                + this.description + " " + UserInfo.PROFILE_IMAGE_URL + "="
                + this.profileImageUrl + " " + UserInfo.URL + "=" + this.url
                + " " + UserInfo.PROTECTED + "=" + this.protect + " "
                + UserInfo.FOLLOWERS_COUNT + "=" + this.followersCount + " "
                + UserInfo.FRIENDS_COUNT + "=" + this.friendsCount + " "
                + UserInfo.FAVORITES_COUNT + "=" + this.favouritesCount + " "
                + UserInfo.STATUSES_COUNT + "=" + this.statusesCount + " "
                + UserInfo.FOLLOWING + "=" + this.following + " "
                + BasicColumns.CREATED_AT + "=" + this.createdAt + " "
                + BasicColumns.TYPE + "=" + this.type + " ";
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(this.id);
        dest.writeString(this.ownerId);
        dest.writeLong(this.createdAt.getTime());
        dest.writeInt(this.type);

        dest.writeString(this.screenName);
        dest.writeString(this.location);
        dest.writeString(this.gender);
        dest.writeString(this.birthday);

        dest.writeString(this.description);
        dest.writeString(this.profileImageUrl);
        dest.writeString(this.url);
        dest.writeInt(this.protect ? 1 : 0);

        dest.writeInt(this.followersCount);
        dest.writeInt(this.friendsCount);
        dest.writeInt(this.favouritesCount);
        dest.writeInt(this.statusesCount);

        dest.writeInt(this.following ? 1 : 0);

    }

}
