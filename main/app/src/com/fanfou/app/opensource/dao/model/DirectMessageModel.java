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
import com.fanfou.app.opensource.db.Contents.DirectMessageInfo;

/**
 * @author mcxiaoke
 * @version 1.0 2011.12.21
 * 
 */
public class DirectMessageModel extends AbstractModel<DirectMessageModel> {

    public static final String TAG = DirectMessageModel.class.getSimpleName();

    private String id;
    private String account;
    private String owner;
    private int type;
    private Date time;

    private String text;
    private String senderId;
    private String senderScreenName;
    private String recipientId;
    private String recipientScreenName;

    private String senderProfileImageUrl;
    private String recipientProfileImageUrl;

    private String threadUserId;
    private String threadUserName;
    private boolean read;

    private UserModel sender = null;
    private UserModel recipient = null;

    public static final Parcelable.Creator<DirectMessageModel> CREATOR = new Parcelable.Creator<DirectMessageModel>() {

        @Override
        public DirectMessageModel createFromParcel(final Parcel source) {
            return new DirectMessageModel(source);
        }

        @Override
        public DirectMessageModel[] newArray(final int size) {
            return new DirectMessageModel[size];
        }
    };

    public DirectMessageModel() {

    }

    public DirectMessageModel(final Parcel in) {
        this.id = in.readString();
        this.account = in.readString();
        this.time = new Date(in.readLong());
        this.type = in.readInt();

        this.senderId = in.readString();
        this.recipientId = in.readString();
        this.text = in.readString();

        this.senderScreenName = in.readString();
        this.recipientScreenName = in.readString();
        this.senderProfileImageUrl = in.readString();
        this.recipientProfileImageUrl = in.readString();

        this.senderId = in.readString();
        this.senderId = in.readString();

        this.read = in.readInt() == 0 ? false : true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public DirectMessageModel get(final String key) {
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
     * @return the id
     */
    public final String getId() {
        return this.id;
    }

    /**
     * @return the owner
     */
    public final String getOwner() {
        return this.owner;
    }

    /**
     * @return the recipient
     */
    public final UserModel getRecipient() {
        return this.recipient;
    }

    /**
     * @return the recipientId
     */
    public final String getRecipientId() {
        return this.recipientId;
    }

    /**
     * @return the recipientProfileImageUrl
     */
    public final String getRecipientProfileImageUrl() {
        return this.recipientProfileImageUrl;
    }

    /**
     * @return the recipientScreenName
     */
    public final String getRecipientScreenName() {
        return this.recipientScreenName;
    }

    /**
     * @return the sender
     */
    public final UserModel getSender() {
        return this.sender;
    }

    /**
     * @return the senderId
     */
    public final String getSenderId() {
        return this.senderId;
    }

    /**
     * @return the senderProfileImageUrl
     */
    public final String getSenderProfileImageUrl() {
        return this.senderProfileImageUrl;
    }

    /**
     * @return the senderScreenName
     */
    public final String getSenderScreenName() {
        return this.senderScreenName;
    }

    /**
     * @return the text
     */
    public final String getText() {
        return this.text;
    }

    /**
     * @return the threadUserId
     */
    public final String getThreadUserId() {
        return this.threadUserId;
    }

    /**
     * @return the threadUserName
     */
    public final String getThreadUserName() {
        return this.threadUserName;
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
     * @return the read
     */
    public final boolean isRead() {
        return this.read;
    }

    @Override
    public void put() {
        // TODO Auto-generated method stub

    }

    /**
     * @param account
     *            the account to set
     */
    public final void setAccount(final String account) {
        this.account = account;
    }

    /**
     * @param id
     *            the id to set
     */
    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * @param owner
     *            the owner to set
     */
    public final void setOwner(final String owner) {
        this.owner = owner;
    }

    /**
     * @param read
     *            the read to set
     */
    public final void setRead(final boolean read) {
        this.read = read;
    }

    /**
     * @param recipient
     *            the recipient to set
     */
    public final void setRecipient(final UserModel recipient) {
        this.recipient = recipient;
    }

    /**
     * @param recipientId
     *            the recipientId to set
     */
    public final void setRecipientId(final String recipientId) {
        this.recipientId = recipientId;
    }

    /**
     * @param recipientProfileImageUrl
     *            the recipientProfileImageUrl to set
     */
    public final void setRecipientProfileImageUrl(
            final String recipientProfileImageUrl) {
        this.recipientProfileImageUrl = recipientProfileImageUrl;
    }

    /**
     * @param recipientScreenName
     *            the recipientScreenName to set
     */
    public final void setRecipientScreenName(final String recipientScreenName) {
        this.recipientScreenName = recipientScreenName;
    }

    /**
     * @param sender
     *            the sender to set
     */
    public final void setSender(final UserModel sender) {
        this.sender = sender;
    }

    /**
     * @param senderId
     *            the senderId to set
     */
    public final void setSenderId(final String senderId) {
        this.senderId = senderId;
    }

    /**
     * @param senderProfileImageUrl
     *            the senderProfileImageUrl to set
     */
    public final void setSenderProfileImageUrl(
            final String senderProfileImageUrl) {
        this.senderProfileImageUrl = senderProfileImageUrl;
    }

    /**
     * @param senderScreenName
     *            the senderScreenName to set
     */
    public final void setSenderScreenName(final String senderScreenName) {
        this.senderScreenName = senderScreenName;
    }

    /**
     * @param text
     *            the text to set
     */
    public final void setText(final String text) {
        this.text = text;
    }

    /**
     * @param threadUserId
     *            the threadUserId to set
     */
    public final void setThreadUserId(final String threadUserId) {
        this.threadUserId = threadUserId;
    }

    /**
     * @param threadUserName
     *            the threadUserName to set
     */
    public final void setThreadUserName(final String threadUserName) {
        this.threadUserName = threadUserName;
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

    @Override
    public String toString() {
        return "[Message] " + BasicColumns.ID + "=" + this.id + " "
                + DirectMessageInfo.TEXT + "=" + this.text + " "
                + BasicColumns.CREATED_AT + "=" + this.time + " "
                + DirectMessageInfo.SENDER_ID + "=" + this.senderId + " "
                + DirectMessageInfo.RECIPIENT_ID + "=" + this.recipientId + " ";
    }

    @Override
    public ContentValues values() {
        final ContentValues cv = new ContentValues();

        cv.put(BasicColumns.ID, this.id);
        cv.put(BasicColumns.OWNER_ID, this.account);
        cv.put(BasicColumns.CREATED_AT, this.time.getTime());
        cv.put(BasicColumns.TYPE, this.type);

        cv.put(DirectMessageInfo.SENDER_ID, this.senderId);
        cv.put(DirectMessageInfo.RECIPIENT_ID, this.recipientId);
        cv.put(DirectMessageInfo.TEXT, this.text);

        cv.put(DirectMessageInfo.SENDER_SCREEN_NAME, this.senderScreenName);
        cv.put(DirectMessageInfo.RECIPIENT_SCREEN_NAME,
                this.recipientScreenName);

        cv.put(DirectMessageInfo.SENDER_PROFILE_IMAGE_URL,
                this.senderProfileImageUrl);
        cv.put(DirectMessageInfo.RECIPIENT_PROFILE_IMAGE_URL,
                this.recipientProfileImageUrl);

        cv.put(DirectMessageInfo.THREAD_USER_ID, this.threadUserId);
        cv.put(DirectMessageInfo.THREAD_USER_NAME, this.threadUserName);
        cv.put(DirectMessageInfo.IS_READ, this.read);

        return cv;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(this.id);
        dest.writeString(this.account);
        dest.writeLong(this.time.getTime());
        dest.writeInt(this.type);

        dest.writeString(this.senderId);
        dest.writeString(this.recipientId);
        dest.writeString(this.text);

        dest.writeString(this.senderScreenName);
        dest.writeString(this.recipientScreenName);
        dest.writeString(this.senderProfileImageUrl);
        dest.writeString(this.recipientProfileImageUrl);

        dest.writeString(this.threadUserId);
        dest.writeString(this.threadUserName);

        dest.writeInt(this.read ? 1 : 0);

    }

}
