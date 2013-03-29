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
import java.util.List;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.fanfou.app.opensource.db.Contents.BasicColumns;
import com.fanfou.app.opensource.db.Contents.StatusInfo;

/**
 * @author mcxiaoke
 * @version 1.0 2011.12.21
 * 
 */
public class StatusModel extends AbstractModel<StatusModel> {

    public static final String TAG = StatusModel.class.getSimpleName();

    private String id;
    private String account;
    private String owner;
    private Date time;

    private String text;
    private String simpleText;
    private String source;

    private String inReplyToStatusId;
    private String inReplyToUserId;
    private String inReplyToScreenName;

    private String photoImageUrl;
    private String photoThumbUrl;
    private String photoLargeUrl;

    private String userId;
    private String userScreenName;
    private String userProfileImageUrl;

    private String location;

    private boolean truncated;
    private boolean favorited;
    private boolean self;

    private boolean read;
    private boolean thread;
    private boolean photo;
    private boolean special;

    private int type;

    private List<String> urls;
    private List<String> hashtags;
    private List<String> names;

    public static final Parcelable.Creator<StatusModel> CREATOR = new Parcelable.Creator<StatusModel>() {

        @Override
        public StatusModel createFromParcel(final Parcel source) {
            return new StatusModel(source);
        }

        @Override
        public StatusModel[] newArray(final int size) {
            return new StatusModel[size];
        }
    };

    public StatusModel() {
    }

    public StatusModel(final Parcel in) {
        this.id = in.readString();
        this.account = in.readString();
        this.time = new Date(in.readLong());
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

        this.read = in.readInt() == 0 ? false : true;
        this.thread = in.readInt() == 0 ? false : true;
        this.photo = in.readInt() == 0 ? false : true;

        this.special = in.readInt() == 0 ? false : true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public StatusModel get(final String key) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return the account
     */
    public final String getAccount() {
        return this.account;
    }

    /**
     * @return the hashtags
     */
    public final List<String> getHashtags() {
        return this.hashtags;
    }

    /**
     * @return the id
     */
    public final String getId() {
        return this.id;
    }

    /**
     * @return the inReplyToScreenName
     */
    public final String getInReplyToScreenName() {
        return this.inReplyToScreenName;
    }

    /**
     * @return the inReplyToStatusId
     */
    public final String getInReplyToStatusId() {
        return this.inReplyToStatusId;
    }

    /**
     * @return the inReplyToUserId
     */
    public final String getInReplyToUserId() {
        return this.inReplyToUserId;
    }

    /**
     * @return the location
     */
    public final String getLocation() {
        return this.location;
    }

    /**
     * @return the names
     */
    public final List<String> getNames() {
        return this.names;
    }

    /**
     * @return the owner
     */
    public final String getOwner() {
        return this.owner;
    }

    /**
     * @return the photoImageUrl
     */
    public final String getPhotoImageUrl() {
        return this.photoImageUrl;
    }

    /**
     * @return the photoLargeUrl
     */
    public final String getPhotoLargeUrl() {
        return this.photoLargeUrl;
    }

    /**
     * @return the photoThumbUrl
     */
    public final String getPhotoThumbUrl() {
        return this.photoThumbUrl;
    }

    /**
     * @return the simpleText
     */
    public final String getSimpleText() {
        return this.simpleText;
    }

    /**
     * @return the source
     */
    public final String getSource() {
        return this.source;
    }

    /**
     * @return the text
     */
    public final String getText() {
        return this.text;
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
     * @return the urls
     */
    public final List<String> getUrls() {
        return this.urls;
    }

    /**
     * @return the userId
     */
    public final String getUserId() {
        return this.userId;
    }

    /**
     * @return the userProfileImageUrl
     */
    public final String getUserProfileImageUrl() {
        return this.userProfileImageUrl;
    }

    /**
     * @return the userScreenName
     */
    public final String getUserScreenName() {
        return this.userScreenName;
    }

    /**
     * @return the favorited
     */
    public final boolean isFavorited() {
        return this.favorited;
    }

    /**
     * @return the photo
     */
    public final boolean isPhoto() {
        return this.photo;
    }

    /**
     * @return the read
     */
    public final boolean isRead() {
        return this.read;
    }

    /**
     * @return the self
     */
    public final boolean isSelf() {
        return this.self;
    }

    /**
     * @return the special
     */
    public final boolean isSpecial() {
        return this.special;
    }

    /**
     * @return the thread
     */
    public final boolean isThread() {
        return this.thread;
    }

    /**
     * @return the truncated
     */
    public final boolean isTruncated() {
        return this.truncated;
    }

    @Override
    public void put() {
        // TODO Auto-generated method stub

    }

    public StatusModel readFromParcel(final Parcel source) {
        return new StatusModel(source);
    }

    /**
     * @param account
     *            the account to set
     */
    public final void setAccount(final String account) {
        this.account = account;
    }

    /**
     * @param favorited
     *            the favorited to set
     */
    public final void setFavorited(final boolean favorited) {
        this.favorited = favorited;
    }

    /**
     * @param hashtags
     *            the hashtags to set
     */
    public final void setHashtags(final List<String> hashtags) {
        this.hashtags = hashtags;
    }

    /**
     * @param id
     *            the id to set
     */
    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * @param inReplyToScreenName
     *            the inReplyToScreenName to set
     */
    public final void setInReplyToScreenName(final String inReplyToScreenName) {
        this.inReplyToScreenName = inReplyToScreenName;
    }

    /**
     * @param inReplyToStatusId
     *            the inReplyToStatusId to set
     */
    public final void setInReplyToStatusId(final String inReplyToStatusId) {
        this.inReplyToStatusId = inReplyToStatusId;
    }

    /**
     * @param inReplyToUserId
     *            the inReplyToUserId to set
     */
    public final void setInReplyToUserId(final String inReplyToUserId) {
        this.inReplyToUserId = inReplyToUserId;
    }

    /**
     * @param location
     *            the location to set
     */
    public final void setLocation(final String location) {
        this.location = location;
    }

    /**
     * @param names
     *            the names to set
     */
    public final void setNames(final List<String> names) {
        this.names = names;
    }

    /**
     * @param owner
     *            the owner to set
     */
    public final void setOwner(final String owner) {
        this.owner = owner;
    }

    /**
     * @param photo
     *            the photo to set
     */
    public final void setPhoto(final boolean photo) {
        this.photo = photo;
    }

    /**
     * @param photoImageUrl
     *            the photoImageUrl to set
     */
    public final void setPhotoImageUrl(final String photoImageUrl) {
        this.photoImageUrl = photoImageUrl;
    }

    /**
     * @param photoLargeUrl
     *            the photoLargeUrl to set
     */
    public final void setPhotoLargeUrl(final String photoLargeUrl) {
        this.photoLargeUrl = photoLargeUrl;
    }

    /**
     * @param photoThumbUrl
     *            the photoThumbUrl to set
     */
    public final void setPhotoThumbUrl(final String photoThumbUrl) {
        this.photoThumbUrl = photoThumbUrl;
    }

    /**
     * @param read
     *            the read to set
     */
    public final void setRead(final boolean read) {
        this.read = read;
    }

    /**
     * @param self
     *            the self to set
     */
    public final void setSelf(final boolean self) {
        this.self = self;
    }

    /**
     * @param simpleText
     *            the simpleText to set
     */
    public final void setSimpleText(final String simpleText) {
        this.simpleText = simpleText;
    }

    /**
     * @param source
     *            the source to set
     */
    public final void setSource(final String source) {
        this.source = source;
    }

    /**
     * @param special
     *            the special to set
     */
    public final void setSpecial(final boolean special) {
        this.special = special;
    }

    /**
     * @param text
     *            the text to set
     */
    public final void setText(final String text) {
        this.text = text;
    }

    /**
     * @param thread
     *            the thread to set
     */
    public final void setThread(final boolean thread) {
        this.thread = thread;
    }

    /**
     * @param time
     *            the time to set
     */
    public final void setTime(final Date time) {
        this.time = time;
    }

    /**
     * @param truncated
     *            the truncated to set
     */
    public final void setTruncated(final boolean truncated) {
        this.truncated = truncated;
    }

    /**
     * @param type
     *            the type to set
     */
    public final void setType(final int type) {
        this.type = type;
    }

    /**
     * @param urls
     *            the urls to set
     */
    public final void setUrls(final List<String> urls) {
        this.urls = urls;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public final void setUserId(final String userId) {
        this.userId = userId;
    }

    /**
     * @param userProfileImageUrl
     *            the userProfileImageUrl to set
     */
    public final void setUserProfileImageUrl(final String userProfileImageUrl) {
        this.userProfileImageUrl = userProfileImageUrl;
    }

    /**
     * @param userScreenName
     *            the userScreenName to set
     */
    public final void setUserScreenName(final String userScreenName) {
        this.userScreenName = userScreenName;
    }

    @Override
    public String toString() {
        // return toContentValues().toString();
        return "[Status] " + BasicColumns.ID + "=" + this.id + " "
                + StatusInfo.TEXT + "=" + this.text + " "
                + BasicColumns.CREATED_AT + "+" + this.time + " "
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
    public ContentValues values() {
        final ContentValues cv = new ContentValues();

        cv.put(BasicColumns.ID, this.id);
        cv.put(BasicColumns.OWNER_ID, this.account);
        cv.put(BasicColumns.CREATED_AT, this.time.getTime());

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

        cv.put(StatusInfo.IS_READ, this.read);
        cv.put(StatusInfo.IS_THREAD, this.thread);
        cv.put(StatusInfo.HAS_PHOTO, this.photo);
        cv.put(StatusInfo.SPECIAL, this.special);

        cv.put(BasicColumns.TYPE, this.type);

        return cv;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(this.id);
        dest.writeString(this.account);
        dest.writeLong(this.time.getTime());
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

        dest.writeInt(this.read ? 1 : 0);
        dest.writeInt(this.thread ? 1 : 0);
        dest.writeInt(this.photo ? 1 : 0);

        dest.writeInt(this.special ? 1 : 0);

    }

}
