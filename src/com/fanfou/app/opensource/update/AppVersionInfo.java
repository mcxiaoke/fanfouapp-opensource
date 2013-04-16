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
package com.fanfou.app.opensource.update;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.fanfou.app.opensource.AppContext;

/**
 * @author mcxiaoke
 * @version 1.0 2011.09.04
 * @version 2.0 2011.10.31
 * 
 */
public final class AppVersionInfo implements Parcelable {

    public static final String TYPE_BUGFIX = "bugfix";
    public static final String TYPE_MINOR = "minor";
    public static final String TYPE_MAJOR = "major";

    public int versionCode;// 版本号
    public String versionName;// 版本显示
    public String releaseDate;// 发布日期
    public String changelog;// 升级日志
    public String downloadUrl;// 下载地址
    public String versionType;// 升级类型：BUG修复，功能改进，重大更新
    public String packageName;// 安装包文件名
    public static final Parcelable.Creator<AppVersionInfo> CREATOR = new Parcelable.Creator<AppVersionInfo>() {

        @Override
        public AppVersionInfo createFromParcel(final Parcel source) {
            final Bundle bundle = source.readBundle();
            return AppVersionInfo.parseBundle(bundle);
        }

        @Override
        public AppVersionInfo[] newArray(final int size) {
            return new AppVersionInfo[size];
        }

    };

    public static AppVersionInfo parse(final String response) {
        try {
            final JSONObject o = new JSONObject(response);
            final AppVersionInfo info = new AppVersionInfo();
            info.versionCode = o.getInt("versionCode");
            info.versionName = o.getString("versionName");
            info.releaseDate = o.getString("releaseDate");
            info.changelog = o.getString("changelog");
            info.downloadUrl = o.getString("downloadUrl");
            info.versionType = o.getString("versionType");
            info.packageName = o.getString("packageName");
            info.forceUpdate = o.getBoolean("forceUpdate");
            return info;
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static AppVersionInfo parseBundle(final Bundle bundle) {
        final AppVersionInfo info = new AppVersionInfo();
        info.versionCode = bundle.getInt("versionCode");
        info.versionName = bundle.getString("versionName");
        info.releaseDate = bundle.getString("releaseDate");
        info.changelog = bundle.getString("changelog");
        info.downloadUrl = bundle.getString("downloadUrl");
        info.versionType = bundle.getString("versionType");
        info.packageName = bundle.getString("packageName");
        info.forceUpdate = bundle.getBoolean("forceUpdate");
        if (info.versionCode > 0) {
            return info;
        } else {
            return null;
        }
    }

    public boolean forceUpdate;// 是否强制升级

    public AppVersionInfo() {
    }

    public AppVersionInfo(final Parcel in) {
        this();
        final Bundle bundle = in.readBundle();
        readFromBundle(bundle);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromBundle(final Bundle bundle) {
        this.versionCode = bundle.getInt("versionCode");
        this.versionName = bundle.getString("versionName");
        this.releaseDate = bundle.getString("releaseDate");
        this.changelog = bundle.getString("changelog");
        this.downloadUrl = bundle.getString("downloadUrl");
        this.versionType = bundle.getString("versionType");
        this.packageName = bundle.getString("packageName");
        this.forceUpdate = bundle.getBoolean("forceUpdate");
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("[VersionInfo] versionCode=" + this.versionCode);
        sb.append("[VersionInfo] versionName=" + this.versionName);
        sb.append("[VersionInfo] releaseDate=" + this.releaseDate);
        sb.append("[VersionInfo] changelog=(" + this.changelog).append(")");
        sb.append("[VersionInfo] downloadUrl=" + this.downloadUrl);
        sb.append("[VersionInfo] versionType=" + this.versionType);
        sb.append("[VersionInfo] packageName=" + this.packageName);
        sb.append("[VersionInfo] forceUpdate=" + this.forceUpdate);
        return sb.toString();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        final Bundle bundle = new Bundle();
        bundle.putInt("versionCode", this.versionCode);
        bundle.putString("versionName", this.versionName);
        bundle.putString("releaseDate", this.releaseDate);
        bundle.putString("changelog", this.changelog);
        bundle.putString("downloadUrl", this.downloadUrl);
        bundle.putString("versionType", this.versionType);
        bundle.putString("packageName", this.packageName);
        bundle.putBoolean("forceUpdate", this.forceUpdate);
        dest.writeBundle(bundle);
    }

}
