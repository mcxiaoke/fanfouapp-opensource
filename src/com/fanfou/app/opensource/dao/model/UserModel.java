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
package com.fanfou.app.opensource.dao.model;

import java.util.Date;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.fanfou.app.opensource.db.Contents.BasicColumns;
import com.fanfou.app.opensource.db.Contents.UserInfo;

/**
 * @author mcxiaoke
 * @version 1.0 2011.12.21
 * 
 */
public class UserModel extends AbstractModel<UserModel> {

    public static final String TAG = UserModel.class.getSimpleName();

    private String id;
    private String account;
    private String owner;
    private int type;
    private Date time;

    private String screenName;
    private String location;
    private String gender;
    private String birthday;

    private String description;
    private String profileImageUrl;
    private String profileImageLargeUrl;
    private String url;

    private int followersCount;
    private int friendsCount;
    private int favouritesCount;
    private int statusesCount;

    private boolean following;
    private boolean protect;

    public static final Parcelable.Creator<UserModel> CREATOR = new Parcelable.Creator<UserModel>() {

        @Override
        public UserModel createFromParcel(final Parcel source) {
            return new UserModel(source);
        }

        @Override
        public UserModel[] newArray(final int size) {
            return new UserModel[size];
        }
    };

    public UserModel() {
    }

    public UserModel(final Parcel in) {
        this.id = in.readString();
        this.account = in.readString();
        this.time = new Date(in.readLong());
        this.type = in.readInt();

        this.screenName = in.readString();
        this.location = in.readString();
        this.gender = in.readString();
        this.birthday = in.readString();

        this.description = in.readString();
        this.profileImageUrl = in.readString();
        this.profileImageLargeUrl = in.readString();
        this.url = in.readString();
        this.protect = in.readInt() == 0 ? false : true;

        this.followersCount = in.readInt();
        this.friendsCount = in.readInt();
        this.favouritesCount = in.readInt();
        this.statusesCount = in.readInt();

        this.following = in.readInt() == 0 ? false : true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public UserModel get(final String key) {
        return null;
    }

    /**
     * @return the owner
     */
    public final String getAccount() {
        return this.account;
    }

    /**
     * @return the birthday
     */
    public final String getBirthday() {
        return this.birthday;
    }

    /**
     * @return the description
     */
    public final String getDescription() {
        return this.description;
    }

    /**
     * @return the favouritesCount
     */
    public final int getFavouritesCount() {
        return this.favouritesCount;
    }

    /**
     * @return the followersCount
     */
    public final int getFollowersCount() {
        return this.followersCount;
    }

    /**
     * @return the friendsCount
     */
    public final int getFriendsCount() {
        return this.friendsCount;
    }

    /**
     * @return the gender
     */
    public final String getGender() {
        return this.gender;
    }

    /**
     * @return the id
     */
    public final String getId() {
        return this.id;
    }

    /**
     * @return the location
     */
    public final String getLocation() {
        return this.location;
    }

    /**
     * @return the owner
     */
    public final String getOwner() {
        return this.owner;
    }

    /**
     * @return the profileImageLargeUrl
     */
    public final String getProfileImageLargeUrl() {
        return this.profileImageLargeUrl;
    }

    /**
     * @return the profileImageUrl
     */
    public final String getProfileImageUrl() {
        return this.profileImageUrl;
    }

    /**
     * @return the screenName
     */
    public final String getScreenName() {
        return this.screenName;
    }

    /**
     * @return the statusesCount
     */
    public final int getStatusesCount() {
        return this.statusesCount;
    }

    /**
     * @return the time
     */
    public final Date getTime() {
        return this.time;
    }

    /**
     * @return the type
     */
    public final int getType() {
        return this.type;
    }

    /**
     * @return the url
     */
    public final String getUrl() {
        return this.url;
    }

    /**
     * @return the following
     */
    public final boolean isFollowing() {
        return this.following;
    }

    /**
     * @return the protect
     */
    public final boolean isProtect() {
        return this.protect;
    }

    @Override
    public void put() {
    }

    /**
     * @param owner
     *            the owner to set
     */
    public final void setAccount(final String account) {
        this.account = account;
    }

    /**
     * @param birthday
     *            the birthday to set
     */
    public final void setBirthday(final String birthday) {
        this.birthday = birthday;
    }

    /**
     * @param description
     *            the description to set
     */
    public final void setDescription(final String description) {
        this.description = description;
    }

    /**
     * @param favouritesCount
     *            the favouritesCount to set
     */
    public final void setFavouritesCount(final int favouritesCount) {
        this.favouritesCount = favouritesCount;
    }

    /**
     * @param followersCount
     *            the followersCount to set
     */
    public final void setFollowersCount(final int followersCount) {
        this.followersCount = followersCount;
    }

    /**
     * @param following
     *            the following to set
     */
    public final void setFollowing(final boolean following) {
        this.following = following;
    }

    /**
     * @param friendsCount
     *            the friendsCount to set
     */
    public final void setFriendsCount(final int friendsCount) {
        this.friendsCount = friendsCount;
    }

    /**
     * @param gender
     *            the gender to set
     */
    public final void setGender(final String gender) {
        this.gender = gender;
    }

    /**
     * @param id
     *            the id to set
     */
    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * @param location
     *            the location to set
     */
    public final void setLocation(final String location) {
        this.location = location;
    }

    /**
     * @param owner
     *            the owner to set
     */
    public final void setOwner(final String owner) {
        this.owner = owner;
    }

    /**
     * @param profileImageLargeUrl
     *            the profileImageLargeUrl to set
     */
    public final void setProfileImageLargeUrl(final String profileImageLargeUrl) {
        this.profileImageLargeUrl = profileImageLargeUrl;
    }

    /**
     * @param profileImageUrl
     *            the profileImageUrl to set
     */
    public final void setProfileImageUrl(final String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * @param protect
     *            the protect to set
     */
    public final void setProtect(final boolean protect) {
        this.protect = protect;
    }

    /**
     * @param screenName
     *            the screenName to set
     */
    public final void setScreenName(final String screenName) {
        this.screenName = screenName;
    }

    /**
     * @param statusesCount
     *            the statusesCount to set
     */
    public final void setStatusesCount(final int statusesCount) {
        this.statusesCount = statusesCount;
    }

    /**
     * @param time
     *            the time to set
     */
    public final void setTime(final Date time) {
        this.time = time;
    }

    /**
     * @param type
     *            the type to set
     */
    public final void setType(final int type) {
        this.type = type;
    }

    /**
     * @param url
     *            the url to set
     */
    public final void setUrl(final String url) {
        this.url = url;
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
                + BasicColumns.CREATED_AT + "=" + this.time + " "
                + BasicColumns.TYPE + "=" + this.type + " ";
    }

    @Override
    public ContentValues values() {
        final UserModel u = this;
        final ContentValues cv = new ContentValues();

        cv.put(BasicColumns.ID, u.id);
        cv.put(BasicColumns.OWNER_ID, u.account);

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
        cv.put(BasicColumns.CREATED_AT, u.time.getTime());

        cv.put(BasicColumns.TYPE, u.type);

        return cv;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(this.id);
        dest.writeString(this.account);
        dest.writeLong(this.time.getTime());
        dest.writeInt(this.type);

        dest.writeString(this.screenName);
        dest.writeString(this.location);
        dest.writeString(this.gender);
        dest.writeString(this.birthday);

        dest.writeString(this.description);
        dest.writeString(this.profileImageUrl);
        dest.writeString(this.profileImageLargeUrl);
        dest.writeString(this.url);
        dest.writeInt(this.protect ? 1 : 0);

        dest.writeInt(this.followersCount);
        dest.writeInt(this.friendsCount);
        dest.writeInt(this.favouritesCount);
        dest.writeInt(this.statusesCount);

        dest.writeInt(this.following ? 1 : 0);

    }

}
