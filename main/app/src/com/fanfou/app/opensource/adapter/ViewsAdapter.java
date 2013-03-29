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

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * @author mcxiaoke
 * @version 1.1 2011.11.04
 * @version 1.2 2011.11.11
 * 
 */
public class ViewsAdapter extends PagerAdapter {
    private final View[] mViews;
    private final boolean endless;

    public ViewsAdapter(final View[] views) {
        this.mViews = views;
        this.endless = false;
    }

    public ViewsAdapter(final View[] views, final boolean endless) {
        this.mViews = views;
        this.endless = endless;
    }

    @Override
    public void destroyItem(final View container, final int position,
            final Object view) {
        ((ViewPager) container).removeView((View) view);
    }

    @Override
    public void finishUpdate(final View container) {
    }

    @Override
    public int getCount() {
        return this.endless ? Integer.MAX_VALUE : this.mViews.length;
    }

    @Override
    public Object instantiateItem(final View container, final int position) {
        final View view = this.mViews[position % this.mViews.length];
        ((ViewPager) container).addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view == (View) object;
    }

    @Override
    public void restoreState(final Parcelable state, final ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(final View container) {
    }

}
