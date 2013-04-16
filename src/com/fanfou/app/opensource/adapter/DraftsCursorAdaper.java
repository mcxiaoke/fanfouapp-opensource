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

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.bean.Draft;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.27
 * 
 */
public class DraftsCursorAdaper extends BaseCursorAdapter {
    private static class ViewHolder {
        TextView text = null;
        // TextView date = null;
        ImageView icon = null;

        ViewHolder(final View base) {
            this.text = (TextView) base.findViewById(R.id.text);
            // this.date = (TextView) base.findViewById(R.id.date);
            this.icon = (ImageView) base.findViewById(R.id.mini_icon);

        }
    }

    private static final String TAG = DraftsCursorAdaper.class.getSimpleName();

    public DraftsCursorAdaper(final Context context, final Cursor c) {
        super(context, c);
    }

    @Override
    public void bindView(final View view, final Context context,
            final Cursor cursor) {
        final View row = view;
        final Draft d = Draft.parse(cursor);
        final ViewHolder holder = (ViewHolder) row.getTag();
        holder.text.setText(d.text);
        // holder.date.setText(DateTimeHelper.formatDate(d.createdAt));
        if (AppContext.DEBUG) {
            Log.d(DraftsCursorAdaper.TAG, "bindView filePath=" + d.filePath);
        }
        holder.icon.setVisibility(StringHelper.isEmpty(d.filePath) ? View.GONE
                : View.VISIBLE);
    }

    @Override
    int getLayoutId() {
        return R.layout.list_item_draft;
    }

    @Override
    public View newView(final Context context, final Cursor cursor,
            final ViewGroup parent) {
        final View view = this.mInflater.inflate(getLayoutId(), null);
        final ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        bindView(view, context, cursor);
        return view;
    }

}
