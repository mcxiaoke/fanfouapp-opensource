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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.ui.ActionManager;
import com.fanfou.app.opensource.util.DateTimeHelper;

public class UserArrayAdapter extends BaseArrayAdapter<User> {

    static class ViewHolder {

        ImageView headIcon = null;
        ImageView lockIcon = null;
        TextView nameText = null;
        TextView idText = null;
        TextView dateText = null;

        ViewHolder(final View base) {
            this.headIcon = (ImageView) base.findViewById(R.id.item_user_head);
            this.lockIcon = (ImageView) base.findViewById(R.id.item_user_flag);
            this.nameText = (TextView) base.findViewById(R.id.item_user_name);
            this.idText = (TextView) base.findViewById(R.id.item_user_id);
            this.dateText = (TextView) base.findViewById(R.id.item_user_date);
        }
    }

    private static final String tag = UserArrayAdapter.class.getSimpleName();

    private List<User> mUsers;

    public UserArrayAdapter(final Context context, final List<User> users) {
        super(context, users);
        if (users == null) {
            this.mUsers = new ArrayList<User>();
        } else {
            this.mUsers = users;
        }
    }

    @Override
    public int getCount() {
        return this.mUsers.size();
    }

    @Override
    public User getItem(final int position) {
        return this.mUsers.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    int getLayoutId() {
        return R.layout.list_item_user;
    }

    @Override
    public View getView(final int position, View convertView,
            final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = this.mInflater.inflate(getLayoutId(), null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final User u = this.mUsers.get(position);

        if (!isTextMode()) {
            holder.headIcon.setTag(u.profileImageUrl);
            this.mLoader.displayImage(u.profileImageUrl, holder.headIcon,
                    R.drawable.default_head);
            holder.headIcon.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {
                    if (u != null) {
                        ActionManager.doProfile(UserArrayAdapter.this.mContext,
                                u);
                    }
                }
            });
        }

        if (u.protect) {
            holder.lockIcon.setVisibility(View.VISIBLE);
        } else {
            holder.lockIcon.setVisibility(View.GONE);
        }
        holder.nameText.setText(u.screenName);
        holder.idText.setText("(" + u.id + ")");
        holder.dateText.setText("创建时间："
                + DateTimeHelper.formatDateOnly(u.createdAt));

        return convertView;
    }

    public void updateDataAndUI(final List<User> us) {
        this.mUsers = us;
        notifyDataSetChanged();
    }

}
