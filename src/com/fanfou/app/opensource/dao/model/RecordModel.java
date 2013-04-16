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

import com.fanfou.app.opensource.db.Contents.DraftInfo;

/**
 * @author mcxiaoke
 * @version 1.0 2011.12.21
 * 
 */
public class RecordModel extends AbstractModel<RecordModel> {

    public static final int TYPE_NONE = 0;

    public static final int ID_NONE = 0;

    public static final String TAG = RecordModel.class.getSimpleName();

    public int id;
    public String ownerId;

    public String text;

    public Date createdAt;
    public int type;
    public String replyTo;
    public String filePath;
    public static final Parcelable.Creator<RecordModel> CREATOR = new Parcelable.Creator<RecordModel>() {

        @Override
        public RecordModel createFromParcel(final Parcel source) {
            return new RecordModel(source);
        }

        @Override
        public RecordModel[] newArray(final int size) {
            return new RecordModel[size];
        }
    };

    public RecordModel() {

    }

    public RecordModel(final Parcel in) {
        this.id = in.readInt();
        this.type = in.readInt();
        this.createdAt = new Date(in.readLong());
        this.ownerId = in.readString();
        this.text = in.readString();
        this.replyTo = in.readString();
        this.filePath = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public RecordModel get(final String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void put() {
        // TODO Auto-generated method stub

    }

    @Override
    public String toString() {
        return "id=" + this.id + " text= " + this.text + " filepath="
                + this.filePath;
    }

    @Override
    public ContentValues values() {
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
