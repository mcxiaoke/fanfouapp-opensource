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

import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.bean.User;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.21
 * @version 1.1 2011.10.24
 * @version 1.5 2011.10.25
 * @version 1.6 2011.11.09
 * 
 */
public class UserChooseCursorAdapter extends BaseCursorAdapter {
    private static class ViewHolder {

        final ImageView headIcon;
        final ImageView lockIcon;
        final TextView nameText;
        final TextView idText;
        final TextView genderText;
        final TextView locationText;
        final CheckBox checkBox;

        ViewHolder(final View base) {
            this.headIcon = (ImageView) base.findViewById(R.id.item_user_head);
            this.lockIcon = (ImageView) base.findViewById(R.id.item_user_flag);
            this.nameText = (TextView) base.findViewById(R.id.item_user_name);
            this.genderText = (TextView) base
                    .findViewById(R.id.item_user_gender);
            this.locationText = (TextView) base
                    .findViewById(R.id.item_user_location);
            this.idText = (TextView) base.findViewById(R.id.item_user_id);
            this.checkBox = (CheckBox) base
                    .findViewById(R.id.item_user_checkbox);
        }
    }

    private static final String tag = UserChooseCursorAdapter.class
            .getSimpleName();
    private ArrayList<Boolean> mStates;

    private SparseBooleanArray mStateMap;

    public UserChooseCursorAdapter(final Context context, final Cursor c) {
        super(context, c, false);
        init();
    }

    public UserChooseCursorAdapter(final Context context, final Cursor c,
            final boolean autoRequery) {
        super(context, c, autoRequery);
        init();
    }

    @Override
    public void bindView(final View view, final Context context,
            final Cursor cursor) {
        final View row = view;
        final User u = User.parse(cursor);

        final ViewHolder holder = (ViewHolder) row.getTag();

        if (!isTextMode()) {
            holder.headIcon.setTag(u.profileImageUrl);
            this.mLoader.displayImage(u.profileImageUrl, holder.headIcon,
                    R.drawable.default_head);
        }

        if (u.protect) {
            holder.lockIcon.setVisibility(View.VISIBLE);
        } else {
            holder.lockIcon.setVisibility(View.GONE);
        }
        holder.nameText.setText(u.screenName);
        holder.idText.setText("(" + u.id + ")");
        holder.genderText.setText(u.gender);
        holder.locationText.setText(u.location);

        final Boolean b = this.mStateMap.get(cursor.getPosition());
        if ((b == null) || (b == Boolean.FALSE)) {
            holder.checkBox.setChecked(false);
        } else {
            holder.checkBox.setChecked(true);
        }
    }

    public ArrayList<Boolean> getCheckedStates() {
        return this.mStates;
    }

    @Override
    int getLayoutId() {
        return R.layout.list_item_chooseuser;
    }

    private void init() {
        this.mStates = new ArrayList<Boolean>();
        this.mStateMap = new SparseBooleanArray();
    }

    @Override
    public View newView(final Context context, final Cursor cursor,
            final ViewGroup parent) {
        final View view = this.mInflater.inflate(getLayoutId(), null);
        final ViewHolder holder = new ViewHolder(view);
        setHeadImage(this.mContext, holder.headIcon);
        // setTextStyle(holder);
        view.setTag(holder);
        return view;
    }

    public void setChecked(final int position) {
    }

    public void setItemChecked(final int position, final boolean checked) {
        this.mStateMap.put(position, checked);
    }

}
