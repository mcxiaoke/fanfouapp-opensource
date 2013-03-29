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
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.cache.ImageLoader;
import com.fanfou.app.opensource.util.OptionHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.01
 * @version 1.5 2011.10.24
 * @version 1.6 2011.10.30
 * @version 1.7 2011.11.10
 * @version 1.8 2011.12.06
 * 
 */
public abstract class BaseArrayAdapter<T> extends BaseAdapter {
    Context mContext;
    LayoutInflater mInflater;
    ImageLoader mLoader;
    private int fontSize;
    private boolean textMode;

    public BaseArrayAdapter(final Context context, final List<T> t) {
        super();
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
        this.mLoader = AppContext.getImageLoader();
        this.textMode = OptionHelper.readBoolean(this.mContext,
                R.string.option_text_mode, false);
        this.fontSize = OptionHelper.readInt(this.mContext,
                R.string.option_fontsize,
                context.getResources().getInteger(R.integer.defaultFontSize));
    }

    public int getFontSize() {
        return this.fontSize;
    }

    abstract int getLayoutId();

    public boolean isTextMode() {
        return this.textMode;
    }

    public void setFontSize(final int size) {
        this.fontSize = size;
    }

    protected void setHeadImage(final ImageView headIcon) {
        if (this.textMode) {
            headIcon.setVisibility(View.GONE);
        } else {
            headIcon.setVisibility(View.VISIBLE);
        }
    }

    public void setTextMode(final boolean mode) {
        this.textMode = mode;
    }

}
