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
package com.fanfou.app.opensource.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.db.Contents.BasicColumns;
import com.fanfou.app.opensource.db.Contents.UserInfo;
import com.fanfou.app.opensource.service.Constants;

/**
 * @author mcxiaoke
 * @version 1.0 2011.08.28
 * @version 1.1 2011.11.02
 * @version 1.2 2011.11.21
 * @version 1.3 2011.12.05
 * 
 */
public class AutoCompleteCursorAdapter extends CursorAdapter {
    private static final String TAG = AutoCompleteCursorAdapter.class
            .getSimpleName();

    private final Activity mContext;
    // private Cursor mCursor;
    private final LayoutInflater mInflater;

    public AutoCompleteCursorAdapter(final Activity context, final Cursor cursor) {
        super(context, cursor);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
        // mCursor = cursor;
    }

    @Override
    public void bindView(final View view, final Context context,
            final Cursor cursor) {
        final String id = cursor.getString(cursor
                .getColumnIndex(BasicColumns.ID));
        final String screenName = cursor.getString(cursor
                .getColumnIndex(UserInfo.SCREEN_NAME));
        final TextView tv = (TextView) view.findViewById(R.id.item_user_name);
        tv.setText("@" + screenName + " (" + id + ")");
    }

    @Override
    public CharSequence convertToString(final Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(UserInfo.SCREEN_NAME));
    }

    @Override
    public View newView(final Context context, final Cursor cursor,
            final ViewGroup parent) {
        final View view = this.mInflater.inflate(
                R.layout.list_item_autocomplete, parent, false);
        return view;
    }

    // private static final Pattern PATTERN_SQL=Pattern.compile("[\\W]+");
    @Override
    public Cursor runQueryOnBackgroundThread(final CharSequence constraint) {
        if (TextUtils.isEmpty(constraint)) {
            return null;
        }

        // String condition=PATTERN_SQL.matcher(constraint).replaceAll("");
        if (AppContext.DEBUG) {
            Log.d(AutoCompleteCursorAdapter.TAG, "constraint = " + constraint);
            // Log.d(TAG, "condition = "+condition);
        }

        final String[] projection = new String[] { BaseColumns._ID,
                BasicColumns.ID, UserInfo.SCREEN_NAME, BasicColumns.TYPE,
                BasicColumns.OWNER_ID };
        final String where = BasicColumns.OWNER_ID + " = '"
                + AppContext.getUserId() + "' AND " + BasicColumns.TYPE
                + " = '" + Constants.TYPE_USERS_FRIENDS + "' AND "
                + UserInfo.SCREEN_NAME + " like '%" + constraint + "%' OR "
                + BasicColumns.ID + " like '%" + constraint + "%'";
        if (AppContext.DEBUG) {
            Log.d(AutoCompleteCursorAdapter.TAG,
                    "runQueryOnBackgroundThread where=" + where);
        }

        // Cursor oldCursor = getCursor();

        // return mContext.getContentResolver().query(UserInfo.CONTENT_URI,
        // projection, where, null, null);

        return this.mContext.getContentResolver().query(UserInfo.CONTENT_URI,
                projection, where, null, null);
        // if(oldCursor!=null){
        // oldCursor.close();
        // oldCursor = null;
        // }
        // return newCursor;
    }

}
