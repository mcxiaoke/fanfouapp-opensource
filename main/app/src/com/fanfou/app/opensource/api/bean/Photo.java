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

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.api.ApiException;
import com.fanfou.app.opensource.db.Contents.StatusInfo;
import com.fanfou.app.opensource.http.ResponseCode;

/**
 * @author mcxiaoke
 * @version 1.0 2011.11.10
 * @version 2.0 2011.12.21
 * 
 */
public class Photo implements Storable<Photo> {
    public static final Parcelable.Creator<Photo> CREATOR = new Parcelable.Creator<Photo>() {

        @Override
        public Photo createFromParcel(final Parcel source) {
            return new Photo(source);
        }

        @Override
        public Photo[] newArray(final int size) {
            return new Photo[size];
        }
    };

    public static Photo parse(final JSONObject o) throws ApiException {
        if (o == null) {
            return null;
        }
        try {
            final Photo p = new Photo();
            p.imageUrl = o.getString(StatusInfo.PHOTO_IMAGE_URL);
            p.largeUrl = o.getString(StatusInfo.PHOTO_LARGE_URL);
            p.thumbUrl = o.getString(StatusInfo.PHOTO_THUMB_URL);
            return p;
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            throw new ApiException(ResponseCode.ERROR_JSON_EXCEPTION,
                    e.getMessage(), e);
        }
    }

    public String id;
    public Date createdAt;
    public String thumbUrl;

    public String largeUrl;

    public String imageUrl;

    public Photo() {
    }

    public Photo(final Parcel in) {
        this.id = in.readString();
        this.createdAt = new Date(in.readLong());
        this.imageUrl = in.readString();
        this.largeUrl = in.readString();
        this.thumbUrl = in.readString();
    }

    @Override
    public int compareTo(final Photo another) {
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public int hashCode() {
        return this.largeUrl.hashCode();
    }

    @Override
    public ContentValues toContentValues() {
        final ContentValues cv = new ContentValues();
        cv.put("id", this.id);
        cv.put("createdAt", this.createdAt.getTime());
        cv.put("imageUrl", this.imageUrl);
        cv.put("thumbUrl", this.thumbUrl);
        cv.put("largeUrl", this.largeUrl);
        return cv;
    }

    @Override
    public String toString() {
        return this.largeUrl;
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
