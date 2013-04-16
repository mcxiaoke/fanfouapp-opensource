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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.ui.ActionManager;
import com.fanfou.app.opensource.util.DateTimeHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.25
 * @version 1.1 2011.10.12
 * @version 2.0 2011.10.24
 * 
 */
public class SearchResultsAdapter extends BaseArrayAdapter<Status> {

    static class ViewHolder {
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

    private static final String TAG = SearchResultsAdapter.class
            .getSimpleName();

    private List<Status> mStatus;
    private String mKeyword;
    private Pattern mPattern;

    public SearchResultsAdapter(final Context context, final List<Status> ss) {
        super(context, ss);
        if (ss == null) {
            this.mStatus = new ArrayList<Status>();
        } else {
            this.mStatus = ss;
        }
    }

    @Override
    public int getCount() {
        return this.mStatus.size();
    }

    @Override
    public Status getItem(final int position) {
        return this.mStatus.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    int getLayoutId() {
        return R.layout.list_item_status;
    }

    @Override
    public View getView(final int position, View convertView,
            final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = this.mInflater.inflate(getLayoutId(), null);
            holder = new ViewHolder(convertView);
            setTextStyle(holder);
            setHeadImage(holder.headIcon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Status s = this.mStatus.get(position);

        if (!isTextMode()) {
            holder.headIcon.setTag(s.userProfileImageUrl);
            this.mLoader.displayImage(s.userProfileImageUrl, holder.headIcon,
                    R.drawable.default_head);
            holder.headIcon.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {
                    if (s != null) {
                        ActionManager.doProfile(
                                SearchResultsAdapter.this.mContext, s);
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

        final SpannableStringBuilder span = new SpannableStringBuilder(
                s.simpleText);
        if (!StringHelper.isEmpty(this.mKeyword)) {
            final Matcher m = this.mPattern.matcher(span);
            while (m.find()) {
                final int start = m.start();
                final int end = m.end();
                span.setSpan(
                        new ForegroundColorSpan(this.mContext.getResources()
                                .getColor(R.color.profile_relation_blue)),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new StyleSpan(Typeface.BOLD), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        holder.contentText.setText(span);

        holder.metaText.setText(DateTimeHelper.getInterval(s.createdAt) + " 通过"
                + s.source);

        return convertView;
    }

    void log(final String message) {
        Log.e(SearchResultsAdapter.TAG, message);
    }

    private void setTextStyle(final ViewHolder holder) {
        final int fontSize = getFontSize();
        holder.contentText.setTextSize(fontSize);
        holder.nameText.setTextSize(fontSize);
        holder.metaText.setTextSize(fontSize - 4);
        final TextPaint tp = holder.nameText.getPaint();
        tp.setFakeBoldText(true);
    }

    public void updateDataAndUI(final List<Status> ss, final String keyword) {
        this.mKeyword = keyword;
        this.mPattern = Pattern.compile(this.mKeyword);
        this.mStatus = ss;
        notifyDataSetChanged();
    }

}
