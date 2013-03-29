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
package com.fanfou.app.opensource.api;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.Html;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.api.bean.Search;
import com.fanfou.app.opensource.api.bean.Storable;
import com.fanfou.app.opensource.http.ResponseCode;
import com.fanfou.app.opensource.http.SimpleResponse;
import com.fanfou.app.opensource.util.DateTimeHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.04
 * @version 1.1 2011.05.15
 * @version 1.2 2011.05.17
 * @version 1.3 2011.10.19
 * @version 1.4 2011.11.11
 * @version 1.5 2011.11.15
 * @version 1.6 2011.11.23
 * @version 1.7 2011.12.01
 * @version 1.8 2011.12.16
 * 
 */
public final class ApiParser implements ResponseCode {

    public static final String TAG = "Parser";

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String SCREEN_NAME = "screen_name";
    public static final String LOCATION = "location";
    public static final String GENDER = "gender";
    public static final String BIRTHDAY = "birthday";
    public static final String DESCRIPTION = "description";
    public static final String PROFILE_IMAGE_URL = "profile_image_url";
    public static final String URL = "url";
    public static final String PROTECTED = "protected";
    public static final String FOLLOWERS_COUNT = "followers_count";
    public static final String FRIENDS_COUNT = "friends_count";
    public static final String FAVORITES_COUNT = "favourites_count";
    public static final String STATUSES_COUNT = "statuses_count";
    public static final String FOLLOWING = "following";
    public static final String NOTIFICATIONS = "notifications";
    public static final String CREATED_AT = "created_at";
    public static final String UTC_OFFSET = "utc_offset";
    public static final String TEXT = "text";
    public static final String SOURCE = "source";
    public static final String TRUNCATED = "truncated";
    public static final String IN_REPLY_TO_LASTMSG_ID = "in_reply_to_lastmsg_id";
    public static final String IN_REPLY_TO_USER_ID = "in_reply_to_user_id";
    public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
    public static final String FAVORITED = "favorited";
    public static final String IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";
    public static final String SENDER_ID = "sender_id";
    public static final String RECIPIENT_ID = "recipient_id";
    public static final String SENDER_SCREEN_NAME = "sender_screen_name";
    public static final String RECIPIENT_SCREEN_NAME = "recipient_screen_name";
    public static final String SENDER = "sender";
    public static final String RECIPIENT = "recipient";
    public static final String USER = "user";
    public static final String STATUS = "status";
    public static final String PHOTO = "photo";
    public static final String PHOTO_IMAGEURL = "imageurl";
    public static final String PHOTO_THUMBURL = "thumburl";
    public static final String PHOTO_LARGEURL = "largeurl";
    public static final String QUERY = "query";
    public static final String TRENDS = "trends";
    public static final String TREND = "trend";
    public static final String AS_OF = "as_of";
    public static final String REQUEST = "request";
    public static final String ERROR = "error";

    static final Pattern PATTERN_SOURCE = Pattern
            .compile("<a href.+blank\">(.+)</a>");

    /**
     * @param s
     *            代表饭否日期和时间的字符串
     * @return 字符串解析为对应的Date对象
     */
    public static Date date(final String s) {
        return DateTimeHelper.fanfouStringToDate(s);
    }

    public static long decodeMessageRealId(final String id) {
        return 0;
    }

    public static long decodeStatusRealId(final String id) {
        return 0;
    }

    public static long decodeUserRealId(final String id) {
        return 0;
    }

    public static String error(final HttpResponse response) throws ApiException {
        try {
            final String content = EntityUtils.toString(response.getEntity());
            if (AppContext.DEBUG) {
                Log.v("Parser", "error() content=" + content);
            }
            if (content == null) {
                return null;
            }
            return ApiParser.error(content);
        } catch (final IOException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static String error(final String error) {
        if (AppContext.DEBUG) {
            Log.v(ApiParser.TAG, "Parser.error() error=" + error);
        }
        String result = error;
        try {
            final JSONObject o = new JSONObject(error);
            if (o.has("error")) {
                result = o.getString("error");
            }
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
            result = ApiParser.parseXMLError(error);
        }
        return result;
    }

    /**
     * 将Date对象解析为饭否格式的字符串
     * 
     * @param date
     *            Date对象
     * @return 饭否格式日期字符串
     */
    public static String formatDate(final Date date) {
        return DateTimeHelper.formatDate(date);
    }

    public static void handleJSONException(final JSONException e)
            throws ApiException {
        if (AppContext.DEBUG) {
            Log.e(ApiParser.TAG, e.getMessage());
        }
        throw new ApiException(ResponseCode.ERROR_JSON_EXCEPTION,
                e.getMessage(), e.getCause());
    }

    public static ArrayList<String> ids(final JSONArray a) throws IOException {
        final ArrayList<String> ids = new ArrayList<String>();
        if (a == null) {
            return ids;
        }
        try {
            for (int i = 0; i < a.length(); i++) {
                ids.add(a.getString(i));
            }
            return ids;
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }
        return ids;
    }

    public static ArrayList<String> ids(final SimpleResponse r)
            throws IOException {
        return ApiParser.ids(r.getJSONArray());
    }

    public static ArrayList<String> ids(final String content)
            throws IOException {
        try {
            final JSONArray a = new JSONArray(content);
            return ApiParser.ids(a);
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }
        return new ArrayList<String>();
    }

    public static boolean parseBoolean(final Cursor c, final String columnName) {
        return c.getInt(c.getColumnIndex(columnName)) == 1;
    }

    public static Date parseDate(final Cursor c, final String columnName) {
        return new Date(c.getLong(c.getColumnIndex(columnName)));
    }

    public static int parseInt(final Cursor c, final String columnName) {
        return c.getInt(c.getColumnIndex(columnName));
    }

    public static long parseLong(final Cursor c, final String columnName) {
        return c.getLong(c.getColumnIndex(columnName));
    }

    public static String parseSource(final String input) {
        String source = input;
        final Matcher m = ApiParser.PATTERN_SOURCE.matcher(input);
        if (m.find()) {
            source = m.group(1);
        }
        // Log.e("SourceParse", "source="+source);
        return source;
    }

    public static String parseString(final Cursor c, final String columnName) {
        try {
            return c.getString(c.getColumnIndexOrThrow(columnName));
        } catch (final IllegalArgumentException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String parseXMLError(final String error) {
        String result = error;
        XmlPullParser pull;
        String tag = null;
        try {
            pull = XmlPullParserFactory.newInstance().newPullParser();
            pull.setInput(new StringReader(error));
            boolean found = false;
            while (!found) {
                final int eventType = pull.getEventType();
                switch (eventType) {
                case XmlPullParser.START_TAG:
                    tag = pull.getName();
                    if (tag.equalsIgnoreCase("error")) {
                        result = pull.nextText();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (tag.equalsIgnoreCase("error")) {
                        found = true;
                    }
                    break;
                default:
                    break;
                }
                pull.next();
            }
        } catch (final XmlPullParserException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        } catch (final IOException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static Search savedSearch(final JSONObject o) throws IOException {
        try {
            final Search s = new Search();
            s.name = o.getString(ApiParser.NAME);
            s.query = o.getString(ApiParser.QUERY);
            return s;
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                Log.e(ApiParser.TAG, e.getMessage());
            }
        }
        return null;
    }

    public static Search savedSearch(final SimpleResponse r) throws IOException {
        return ApiParser.savedSearch(r.getJSONObject());
    }

    public static Search savedSearch(final String content) throws IOException {
        try {
            final JSONObject o = new JSONObject(content);
            return ApiParser.savedSearch(o);
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                Log.e(ApiParser.TAG, e.getMessage());
            }
        }
        return null;
    }

    public static ArrayList<Search> savedSearches(final JSONArray a)
            throws IOException {
        try {
            final ArrayList<Search> ss = new ArrayList<Search>();
            for (int i = 0; i < a.length(); i++) {
                final JSONObject o = a.getJSONObject(i);
                final Search s = ApiParser.savedSearch(o);
                ss.add(s);
            }
            return ss;
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                Log.e(ApiParser.TAG, e.getMessage());
            }
        }
        return new ArrayList<Search>();
    }

    public static ArrayList<Search> savedSearches(final SimpleResponse r)
            throws IOException {
        return ApiParser.savedSearches(r.getJSONArray());
    }

    public static List<Search> savedSearches(final String content)
            throws IOException {
        try {
            final JSONArray a = new JSONArray(content);
            return ApiParser.savedSearches(a);
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                Log.e(ApiParser.TAG, e.getMessage());
            }
        }
        return new ArrayList<Search>();
    }

    /**
     * 批量生成ContentValues Array
     * 
     * @param <T>
     * @param t
     * @return
     */
    public static <T> ContentValues[] toContentValuesArray(
            final List<? extends Storable<T>> t) {
        if ((t == null) || (t.size() == 0)) {
            return null;
        }
        final ContentValues[] values = new ContentValues[t.size()];
        for (int i = 0; i < t.size(); i++) {
            values[i] = t.get(i).toContentValues();
        }
        return values;

    }

    public static <T> ContentValues[] toContentValuesArray(
            final Set<? extends Storable<T>> t) {
        if ((t == null) || (t.size() == 0)) {
            return null;
        }

        final ArrayList<Storable<T>> s = new ArrayList<Storable<T>>();
        s.addAll(t);
        return ApiParser.toContentValuesArray(s);

        // ArrayList<ContentValues> values=new ArrayList<ContentValues>();
        // Iterator<? extends Storable<T>> i=t.iterator();
        // while (i.hasNext()) {
        // Storable<T> st=i.next();
        // values.add(st.toContentValues());
        // }
        // return (ContentValues[]) values.toArray(new
        // ContentValues[values.size()]);

    }

    /**
     * 批量生成ContentValues List
     * 
     * @param <T>
     * @param t
     * @return
     */
    public static <T> List<ContentValues> toContentValuesList(
            final List<? extends Storable<T>> t) {
        if ((t == null) || (t.size() == 0)) {
            return null;
        }
        final List<ContentValues> values = new ArrayList<ContentValues>();
        for (final Storable<?> s : t) {
            values.add(s.toContentValues());
        }
        return values;

    }

    public static Search trend(final JSONObject o) throws IOException {
        try {
            final Search t = new Search();
            t.name = Html.fromHtml(o.getString(ApiParser.NAME)).toString();
            t.query = Html.fromHtml(o.getString(ApiParser.QUERY)).toString();
            return t;
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Search trend(final SimpleResponse r) throws IOException {
        return ApiParser.trend(r.getJSONObject());
    }

    public static Search trend(final String content) throws IOException {
        try {
            final JSONObject o = new JSONObject(content);
            return ApiParser.trend(o);
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static ArrayList<Search> trends(final JSONObject o)
            throws IOException {
        final ArrayList<Search> ts = new ArrayList<Search>();
        try {
            final JSONArray a = o.getJSONArray(ApiParser.TRENDS);
            for (int i = 0; i < a.length(); i++) {
                final Search t = ApiParser.trend(a.getJSONObject(i));
                ts.add(t);
            }
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                Log.e(ApiParser.TAG, e.getMessage());
            }
        }

        return ts;
    }

    public static ArrayList<Search> trends(final SimpleResponse r)
            throws IOException {
        return ApiParser.trends(r.getJSONObject());
    }

    public static ArrayList<Search> trends(final String content)
            throws IOException {
        try {
            final JSONObject o = new JSONObject(content);
            return ApiParser.trends(o);
        } catch (final JSONException e) {
            if (AppContext.DEBUG) {
                Log.e(ApiParser.TAG, e.getMessage());
            }
        }
        return new ArrayList<Search>();
    }

}
