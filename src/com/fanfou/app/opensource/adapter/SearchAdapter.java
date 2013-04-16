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

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.bean.Search;

public class SearchAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView name;

        public ViewHolder(final View base) {
            this.name = (TextView) base.findViewById(R.id.item_status_text);
        }
    }

    private final LayoutInflater mInflater;
    private final List<Search> array;

    public SearchAdapter(final Context context, final List<Search> values) {
        super();
        this.mInflater = LayoutInflater.from(context);
        this.array = values;
    }

    @Override
    public int getCount() {
        return this.array.size();
    }

    @Override
    public Search getItem(final int position) {
        return this.array.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView,
            final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.list_item_search,
                    null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(this.array.get(position).name);

        return convertView;
    }

}
