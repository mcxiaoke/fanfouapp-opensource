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
import java.util.List;

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
import com.fanfou.app.opensource.db.Contents.DirectMessageInfo;
import com.fanfou.app.opensource.http.ResponseCode;
import com.fanfou.app.opensource.http.SimpleResponse;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.04.30
 * @version 1.1 2011.05.02
 * @version 1.5 2011.05.20
 * @version 1.6 2011.07.22
 * @version 1.7 2011.10.21
 * @version 1.8 2011.11.04
 * @version 1.9 2011.11.21
 * @version 2.0 2011.11.23
 * @version 2.1 2011.12.01
 * @version 2.2 2011.12.16
 * @version 2.3 2011.12.19
 * @version 3.0 2011.12.21
 * @version 3.1 2011.12.23
 * 
 */
public class DirectMessage implements Storable<DirectMessage> {

    public static final String TAG = DirectMessage.class.getSimpleName();

    private static void log(final String message) {
        Log.d(DirectMessage.TAG, message);
    }

    public String id;
    public String ownerId;
    public String text;
    public Date createdAt;
    public String senderId;
    public String senderScreenName;
    public String recipientId;
    public String recipientScreenName;

    public String senderProfileImageUrl;
    public String recipientProfileImageUrl;

    public int type;

    public String threadUserId;
    public String threadUserName;
    public boolean isRead;

    public long realId;

    public User sender = null;
    public User recipient = null;

    public static final Parcelable.Creator<DirectMessage> CREATOR = new Parcelable.Creator<DirectMessage>() {

        @Override
        public DirectMessage createFromParcel(final Parcel source) {
            return new DirectMessage(source);
        }

        @Override
        public DirectMessage[] newArray(final int size) {
            return new DirectMessage[size];
        }
    };

    public static DirectMessage parse(final Cursor c) {
        if (c == null) {
            return null;
        }
        final DirectMessage dm = new DirectMessage();
        dm.id = ApiParser.parseString(c, BasicColumns.ID);
        dm.ownerId = ApiParser.parseString(c, BasicColumns.OWNER_ID);
        dm.text = ApiParser.parseString(c, DirectMessageInfo.TEXT);
        dm.createdAt = ApiParser.parseDate(c, BasicColumns.CREATED_AT);
        dm.senderId = ApiParser.parseString(c, DirectMessageInfo.SENDER_ID);
        dm.senderScreenName = ApiParser.parseString(c,
                DirectMessageInfo.SENDER_SCREEN_NAME);
        dm.recipientId = ApiParser.parseString(c,
                DirectMessageInfo.RECIPIENT_ID);
        dm.recipientScreenName = ApiParser.parseString(c,
                DirectMessageInfo.RECIPIENT_SCREEN_NAME);
        dm.senderProfileImageUrl = ApiParser.parseString(c,
                DirectMessageInfo.SENDER_PROFILE_IMAGE_URL);
        dm.recipientProfileImageUrl = ApiParser.parseString(c,
                DirectMessageInfo.RECIPIENT_PROFILE_IMAGE_URL);

        dm.type = ApiParser.parseInt(c, BasicColumns.TYPE);

        dm.threadUserId = ApiParser.parseString(c,
                DirectMessageInfo.THREAD_USER_ID);
        dm.threadUserName = ApiParser.parseString(c,
                DirectMessageInfo.THREAD_USER_NAME);
        dm.isRead = ApiParser.parseBoolean(c, DirectMessageInfo.IS_READ);

        if (TextUtils.isEmpty(dm.id)) {
            return null;
        }

        return dm;
    }

    public static DirectMessage parse(final JSONObject o, final int type)
            throws ApiException {
        if (o == null) {
            return null;
        }
        DirectMessage dm = null;
        try {
            dm = new DirectMessage();
            dm.id = o.getString(BasicColumns.ID);
            dm.realId = ApiParser.decodeMessageRealId(dm.id);
            dm.text = o.getString(DirectMessageInfo.TEXT);
            dm.createdAt = ApiParser.date(o.getString(BasicColumns.CREATED_AT));
            dm.senderId = o.getString(DirectMessageInfo.SENDER_ID);
            dm.senderScreenName = o
                    .getString(DirectMessageInfo.SENDER_SCREEN_NAME);
            dm.recipientId = o.getString(DirectMessageInfo.RECIPIENT_ID);
            dm.recipientScreenName = o
                    .getString(DirectMessageInfo.RECIPIENT_SCREEN_NAME);

            if (o.has("sender")) {
                final JSONObject so = o.getJSONObject("sender");
                dm.sender = User.parse(so);
                dm.senderProfileImageUrl = dm.sender.profileImageUrl;
            }
            if (o.has("recipient")) {
                final JSONObject so = o.getJSONObject("recipient");
                dm.recipient = User.parse(so);
                dm.recipientProfileImageUrl = dm.recipient.profileImageUrl;
            }

            dm.isRead = false;

            dm.type = type;

            dm.ownerId = AppContext.getUserId();
        } catch (final JSONException e) {
            throw new ApiException(ResponseCode.ERROR_JSON_EXCEPTION,
                    e.getMessage(), e);
        }
        if (AppContext.DEBUG) {
            DirectMessage.log("DirectMessage.parse id=" + dm.id);
        }
        return dm;
    }

    public static DirectMessage parse(final SimpleResponse r, final int type)
            throws ApiException {
        JSONObject o = null;
        try {
            o = r.getJSONObject();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (o != null) {
            return DirectMessage.parse(o, type);
        }
        return null;
    }

    public static ArrayList<DirectMessage> parseConversationList(
            final JSONArray a) throws ApiException {
        if (a == null) {
            return null;
        }
        final ArrayList<DirectMessage> dms = new ArrayList<DirectMessage>();
        try {
            for (int i = 0; i < a.length(); i++) {
                final JSONObject io = a.getJSONObject(i);
                final JSONObject dmo = io.getJSONObject("dm");
                final DirectMessage dm = DirectMessage.parse(dmo,
                        Constants.TYPE_DIRECT_MESSAGES_CONVERSTATION_LIST);
                dms.add(dm);
            }
        } catch (final JSONException e) {
            throw new ApiException(ResponseCode.ERROR_JSON_EXCEPTION,
                    e.getMessage(), e);
        }
        return dms;
    }

    public static ArrayList<DirectMessage> parseConversationList(
            final SimpleResponse response) throws ApiException {
        JSONArray json = null;
        try {
            json = response.getJSONArray();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (json != null) {
            return DirectMessage.parseConversationList(json);
        }
        return null;
    }

    public static List<DirectMessage> parseConversationUser(final JSONArray a)
            throws ApiException {
        return DirectMessage.parseMessges(a,
                Constants.TYPE_DIRECT_MESSAGES_CONVERSTATION);
    }

    public static List<DirectMessage> parseConversationUser(
            final SimpleResponse response) throws ApiException {
        JSONArray json = null;
        try {
            json = response.getJSONArray();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (json != null) {
            return DirectMessage.parseConversationUser(json);
        }
        return null;

    }

    public static ArrayList<DirectMessage> parseMessges(final JSONArray a,
            final int type) throws ApiException {
        if (a == null) {
            return null;
        }
        final ArrayList<DirectMessage> dms = new ArrayList<DirectMessage>();
        try {
            for (int i = 0; i < a.length(); i++) {
                final JSONObject o = a.getJSONObject(i);
                final DirectMessage dm = DirectMessage.parse(o, type);
                dms.add(dm);
            }
        } catch (final JSONException e) {
            throw new ApiException(ResponseCode.ERROR_JSON_EXCEPTION,
                    e.getMessage(), e);
        }
        return dms;
    }

    public static ArrayList<DirectMessage> parseMessges(final SimpleResponse r,
            final int type) throws ApiException {
        JSONArray a = null;
        try {
            a = r.getJSONArray();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (a != null) {
            return DirectMessage.parseMessges(a, type);
        }
        return null;
    }

    public DirectMessage() {

    }

    public DirectMessage(final Parcel in) {
        this.id = in.readString();
        this.ownerId = in.readString();
        this.createdAt = new Date(in.readLong());
        this.type = in.readInt();

        this.senderId = in.readString();
        this.recipientId = in.readString();
        this.text = in.readString();

        this.senderScreenName = in.readString();
        this.recipientScreenName = in.readString();
        this.senderProfileImageUrl = in.readString();
        this.recipientProfileImageUrl = in.readString();

        this.threadUserId = in.readString();
        this.threadUserName = in.readString();

        this.isRead = in.readInt() == 0 ? false : true;
    }

    @Override
    public int compareTo(final DirectMessage another) {
        return this.createdAt.compareTo(another.createdAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof DirectMessage) {
            final DirectMessage dm = (DirectMessage) o;
            if (this.id.equals(dm.id)) {
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
        final ContentValues cv = new ContentValues();

        cv.put(BasicColumns.ID, this.id);
        cv.put(BasicColumns.OWNER_ID, this.ownerId);
        cv.put(BasicColumns.CREATED_AT, this.createdAt.getTime());
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
        cv.put(DirectMessageInfo.IS_READ, this.isRead);

        return cv;
    }

    @Override
    public String toString() {
        return "[Message] " + BasicColumns.ID + "=" + this.id + " "
                + DirectMessageInfo.TEXT + "=" + this.text + " "
                + BasicColumns.CREATED_AT + "=" + this.createdAt + " "
                + DirectMessageInfo.SENDER_ID + "=" + this.senderId + " "
                + DirectMessageInfo.RECIPIENT_ID + "=" + this.recipientId + " ";
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(this.id);
        dest.writeString(this.ownerId);
        dest.writeLong(this.createdAt.getTime());
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

        dest.writeInt(this.isRead ? 1 : 0);

    }

}
