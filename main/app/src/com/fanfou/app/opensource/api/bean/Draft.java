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

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.fanfou.app.opensource.api.ApiParser;
import com.fanfou.app.opensource.db.Contents.DraftInfo;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.26
 * @version 1.1 2011.11.04
 * @version 2.0 2011.11.10
 * @version 3.0 2011.12.21
 * 
 */
public class Draft implements Storable<Draft> {

    public static final int TYPE_NONE = 0;

    public static final int ID_NONE = 0;

    public static final String TAG = Draft.class.getSimpleName();

    public int id;
    public String ownerId;

    public String text;

    public Date createdAt;
    public static final Parcelable.Creator<Draft> CREATOR = new Parcelable.Creator<Draft>() {

        @Override
        public Draft createFromParcel(final Parcel source) {
            return new Draft(source);
        }

        @Override
        public Draft[] newArray(final int size) {
            return new Draft[size];
        }
    };

    public static Draft parse(final Cursor c) {
        if (c == null) {
            return null;
        }
        final Draft d = new Draft();
        d.id = ApiParser.parseInt(c, BaseColumns._ID);
        d.ownerId = ApiParser.parseString(c, DraftInfo.OWNER_ID);
        d.text = ApiParser.parseString(c, DraftInfo.TEXT);
        d.createdAt = ApiParser.parseDate(c, DraftInfo.CREATED_AT);
        d.type = ApiParser.parseInt(c, DraftInfo.TYPE);
        d.replyTo = ApiParser.parseString(c, DraftInfo.REPLY_TO);
        d.filePath = ApiParser.parseString(c, DraftInfo.FILE_PATH);
        return d;
    }

    public int type;
    public String replyTo;
    public String filePath;

    public Draft() {

    }

    public Draft(final Parcel in) {
        this.id = in.readInt();
        this.type = in.readInt();
        this.createdAt = new Date(in.readLong());
        this.ownerId = in.readString();
        this.text = in.readString();
        this.replyTo = in.readString();
        this.filePath = in.readString();
    }

    @Override
    public int compareTo(final Draft another) {
        return this.createdAt.compareTo(another.createdAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Draft other = (Draft) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.text == null) {
            if (other.text != null) {
                return false;
            }
        } else if (!this.text.equals(other.text)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + this.id;
        result = (prime * result)
                + ((this.text == null) ? 0 : this.text.hashCode());
        return result;
    }

    @Override
    public ContentValues toContentValues() {
        final ContentValues cv = new ContentValues();
        cv.put(DraftInfo.OWNER_ID, this.ownerId);
        cv.put(DraftInfo.TEXT, this.text);
        cv.put(DraftInfo.CREATED_AT, new Date().getTime());
        cv.put(DraftInfo.TYPE, this.type);
        cv.put(DraftInfo.REPLY_TO, this.replyTo);
        cv.put(DraftInfo.FILE_PATH, this.filePath);
        return cv;
    }

    @Override
    public String toString() {
        return "id=" + this.id + " text= " + this.text + " filepath="
                + this.filePath;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.type);
        dest.writeLong(this.createdAt.getTime());
        dest.writeString(this.ownerId);
        dest.writeString(this.text);
        dest.writeString(this.replyTo);
        dest.writeString(this.filePath);
    }

}
