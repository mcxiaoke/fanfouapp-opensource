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
import android.text.TextPaint;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.bean.DirectMessage;
import com.fanfou.app.opensource.ui.ActionManager;
import com.fanfou.app.opensource.util.DateTimeHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.09
 * @version 1.5 2011.10.24
 * 
 */
public class MessageCursorAdapter extends BaseCursorAdapter {

    static class ViewHolder {
        ImageView headIcon = null;
        TextView nameText = null;
        TextView dateText = null;
        TextView contentText = null;

        ViewHolder(final View base) {
            this.headIcon = (ImageView) base
                    .findViewById(R.id.item_message_head);
            this.contentText = (TextView) base
                    .findViewById(R.id.item_message_text);
            this.dateText = (TextView) base
                    .findViewById(R.id.item_message_date);
            this.nameText = (TextView) base
                    .findViewById(R.id.item_message_user);

        }
    }

    public static final String TAG = "MessageCursorAdapter";
    private static final int ITEM_TYPE_ME = 0;
    private static final int ITEM_TYPE_NONE = 1;
    private static final int[] TYPES = new int[] {
            MessageCursorAdapter.ITEM_TYPE_ME,
            MessageCursorAdapter.ITEM_TYPE_NONE };

    private final boolean autoLink;

    public MessageCursorAdapter(final Context context, final Cursor c) {
        super(context, c, false);
        this.autoLink = false;
    }

    public MessageCursorAdapter(final Context context, final Cursor c,
            final boolean autoRequery, final boolean autoLink) {
        super(context, c, autoRequery);
        this.autoLink = autoLink;
    }

    @Override
    public void bindView(final View view, final Context context,
            final Cursor cursor) {
        final View row = view;
        final ViewHolder holder = (ViewHolder) row.getTag();

        final DirectMessage dm = DirectMessage.parse(cursor);

        if (getItemViewType(cursor.getPosition()) == MessageCursorAdapter.ITEM_TYPE_ME) {
            row.setBackgroundColor(0x33999999);
        }

        if (!isTextMode()) {
            holder.headIcon.setTag(dm.senderProfileImageUrl);
            this.mLoader.displayImage(dm.senderProfileImageUrl,
                    holder.headIcon, R.drawable.default_head);
        }

        holder.headIcon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                if (dm != null) {
                    ActionManager.doProfile(MessageCursorAdapter.this.mContext,
                            dm);
                }
            }
        });

        holder.nameText.setText(dm.senderScreenName);
        holder.dateText.setText(DateTimeHelper.getInterval(dm.createdAt));
        holder.contentText.setText(dm.text);
    }

    @Override
    public int getItemViewType(final int position) {
        final Cursor c = (Cursor) getItem(position);
        if (c == null) {
            return MessageCursorAdapter.ITEM_TYPE_NONE;
        }
        final DirectMessage dm = DirectMessage.parse(c);
        if ((dm == null) || dm.isNull()) {
            return MessageCursorAdapter.ITEM_TYPE_NONE;
        }

        if (dm.senderId.equals(dm.threadUserId)) {
            return MessageCursorAdapter.ITEM_TYPE_NONE;
        } else {
            return MessageCursorAdapter.ITEM_TYPE_ME;
        }
    }

    @Override
    int getLayoutId() {
        return R.layout.list_item_message;
    }

    @Override
    public int getViewTypeCount() {
        return MessageCursorAdapter.TYPES.length;
    }

    @Override
    public View newView(final Context context, final Cursor cursor,
            final ViewGroup parent) {
        final View view = this.mInflater.inflate(getLayoutId(), null);
        final ViewHolder holder = new ViewHolder(view);
        setHeadImage(this.mContext, holder.headIcon);
        setTextStyle(holder);
        view.setTag(holder);
        return view;
    }

    private void setTextStyle(final ViewHolder holder) {
        final int fontSize = getFontSize();
        holder.contentText.setTextSize(fontSize);
        holder.nameText.setTextSize(fontSize);
        holder.dateText.setTextSize(fontSize - 4);
        final TextPaint tp = holder.nameText.getPaint();
        tp.setFakeBoldText(true);

        if (this.autoLink) {
            holder.contentText.setAutoLinkMask(Linkify.ALL);
        }
    }

}
