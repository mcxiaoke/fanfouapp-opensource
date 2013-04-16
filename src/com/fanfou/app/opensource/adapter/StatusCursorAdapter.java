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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.ui.ActionManager;
import com.fanfou.app.opensource.util.DateTimeHelper;
import com.fanfou.app.opensource.util.OptionHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.01
 * @version 1.5 2011.10.24
 * @version 1.6 2011.12.06
 * 
 */
public class StatusCursorAdapter extends BaseCursorAdapter {

    private static class ViewHolder {
        ImageView headIcon = null;
        ImageView replyIcon = null;
        ImageView photoIcon = null;
        TextView nameText = null;
        TextView metaText = null;
        TextView contentText = null;

        ViewHolder(final View base) {
            this.headIcon = (ImageView) base
                    .findViewById(R.id.item_status_head);
            this.replyIcon = (ImageView) base
                    .findViewById(R.id.item_status_icon_reply);
            this.photoIcon = (ImageView) base
                    .findViewById(R.id.item_status_icon_photo);
            this.contentText = (TextView) base
                    .findViewById(R.id.item_status_text);
            this.metaText = (TextView) base.findViewById(R.id.item_status_meta);
            this.nameText = (TextView) base.findViewById(R.id.item_status_user);

        }
    }

    private static final int NONE = 0;
    private static final int MENTION = 1;
    private static final int SELF = 2;

    private static final int[] TYPES = new int[] { StatusCursorAdapter.NONE,
            StatusCursorAdapter.MENTION, StatusCursorAdapter.SELF, };
    private int mMentionedBgColor;// = 0x332266aa;
    private int mSelfBgColor;// = 0x33999999;

    private boolean colored;

    public static final String TAG = StatusCursorAdapter.class.getSimpleName();

    public StatusCursorAdapter(final boolean colored, final Context context,
            final Cursor c) {
        super(context, c, false);
        init(context, colored);
    }

    public StatusCursorAdapter(final Context context) {
        super(context, null, false);
        init(context, false);
    }

    public StatusCursorAdapter(final Context context, final Cursor c) {
        super(context, c, false);
        init(context, false);
    }

    @Override
    public void bindView(final View view, final Context context,
            final Cursor cursor) {
        final View row = view;
        final ViewHolder holder = (ViewHolder) row.getTag();

        final Status s = Status.parse(cursor);

        if (this.colored) {
            final int itemType = getItemViewType(cursor.getPosition());
            switch (itemType) {
            case MENTION:
                row.setBackgroundColor(this.mMentionedBgColor);
                break;
            case SELF:
                row.setBackgroundColor(this.mSelfBgColor);
                break;
            case NONE:
                break;
            default:
                break;
            }
        }

        if (!isTextMode()) {
            holder.headIcon.setTag(s.userProfileImageUrl);
            this.mLoader.displayImage(s.userProfileImageUrl, holder.headIcon,
                    R.drawable.default_head);
            holder.headIcon.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {
                    if (s != null) {
                        ActionManager.doProfile(
                                StatusCursorAdapter.this.mContext, s);
                    }
                }
            });
        }

        if (StringHelper.isEmpty(s.inReplyToStatusId)) {
            holder.replyIcon.setVisibility(View.GONE);
        } else {
            holder.replyIcon.setVisibility(View.VISIBLE);
        }

        if (StringHelper.isEmpty(s.photoLargeUrl)) {
            holder.photoIcon.setVisibility(View.GONE);
        } else {
            holder.photoIcon.setVisibility(View.VISIBLE);
        }

        holder.nameText.setText(s.userScreenName);
        holder.contentText.setText(s.simpleText);
        holder.metaText.setText(DateTimeHelper.getInterval(s.createdAt) + " 通过"
                + s.source);

    }

    @Override
    public int getItemViewType(final int position) {
        final Cursor c = (Cursor) getItem(position);
        if (c == null) {
            return StatusCursorAdapter.NONE;
        }
        final Status s = Status.parse(c);
        if ((s == null) || s.isNull()) {
            return StatusCursorAdapter.NONE;
        }
        if ((s.type == Constants.TYPE_STATUSES_MENTIONS)
                || s.simpleText.contains("@" + AppContext.getUserName())) {
            return StatusCursorAdapter.MENTION;
        }

        return s.self ? StatusCursorAdapter.SELF : StatusCursorAdapter.NONE;
    }

    @Override
    int getLayoutId() {
        return R.layout.list_item_status;
    }

    @Override
    public int getViewTypeCount() {
        return StatusCursorAdapter.TYPES.length;
    }

    private void init(final Context context, final boolean colored) {
        this.colored = colored;
        if (colored) {
            this.mMentionedBgColor = OptionHelper.readInt(this.mContext,
                    R.string.option_color_highlight_mention, context
                            .getResources().getColor(R.color.mentioned_color));
            this.mSelfBgColor = OptionHelper.readInt(this.mContext,
                    R.string.option_color_highlight_self, context
                            .getResources().getColor(R.color.self_color));
            if (AppContext.DEBUG) {
                log("init mMentionedBgColor="
                        + Integer.toHexString(this.mMentionedBgColor));
                log("init mSelfBgColor="
                        + Integer.toHexString(this.mSelfBgColor));
            }
        }
    }

    private void log(final String message) {
        Log.d(StatusCursorAdapter.TAG, message);

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
        holder.metaText.setTextSize(fontSize - 4);
        final TextPaint tp = holder.nameText.getPaint();
        tp.setFakeBoldText(true);
    }

    public void switchCursor(final Cursor cursor) {
        if (cursor != null) {
            this.mCursor = cursor;
            changeCursor(this.mCursor);
            this.mCursor.requery();
        } else {
            this.mCursor = null;
            changeCursor(this.mCursor);
            notifyDataSetChanged();
        }
    }

}
