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

/**
 * @author mcxiaoke
 * @version 1.0 2011.12.21
 * 
 */
public class PhotoModel extends AbstractModel<PhotoModel> {
    public String id;
    public Date createdAt;
    public String thumbUrl;
    public String largeUrl;
    public String imageUrl;

    public static final Parcelable.Creator<PhotoModel> CREATOR = new Parcelable.Creator<PhotoModel>() {

        @Override
        public PhotoModel createFromParcel(final Parcel source) {
            return new PhotoModel(source);
        }

        @Override
        public PhotoModel[] newArray(final int size) {
            return new PhotoModel[size];
        }
    };

    public PhotoModel() {
    }

    public PhotoModel(final Parcel in) {
        this.id = in.readString();
        this.createdAt = new Date(in.readLong());
        this.imageUrl = in.readString();
        this.largeUrl = in.readString();
        this.thumbUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public PhotoModel get(final String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void put() {
        // TODO Auto-generated method stub

    }

    @Override
    public String toString() {
        return this.largeUrl;
    }

    @Override
    public ContentValues values() {
        final ContentValues cv = new ContentValues();
        cv.put("id", this.id);
        cv.put("createdAt", this.createdAt.getTime());
        cv.put("imageUrl", this.imageUrl);
        cv.put("thumbUrl", this.thumbUrl);
        cv.put("largeUrl", this.largeUrl);
        return cv;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(this.id);
        dest.writeLong(this.createdAt.getTime());
        dest.writeString(this.imageUrl);
        dest.writeString(this.largeUrl);
        dest.writeString(this.thumbUrl);
    }

}
