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
package com.fanfou.app.opensource.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.R;

/**
 * @author mcxiaoke
 * 
 */
public class EndlessListViewNoHeader extends ListView implements
        OnItemClickListener {
    public interface OnLoadDataListener {

        public void onItemClick(EndlessListViewNoHeader view, int position);

        public void onLoadMore(EndlessListViewNoHeader view);
    }

    private static final String TAG = EndlessListViewNoHeader.class
            .getSimpleName();
    protected static final int FOOTER_NONE = 0;
    protected static final int FOOTER_HIDE = 1;
    protected static final int FOOTER_NORMAL = 2;

    protected static final int FOOTER_LOADING = 3;

    protected static final int MAX_OVERSCROLL_Y = 240;
    Context mContext;

    LayoutInflater mInflater;
    ViewGroup mLoadMoreView;
    ProgressBar mLoadMoreProgressView;

    TextView mLoadMoreTextView;

    OnLoadDataListener mOnRefreshListener;
    protected boolean isLoading;

    protected boolean isRefresh;
    protected View curPosView;
    protected int curPos;

    protected int curPosTop;

    public EndlessListViewNoHeader(final Context context) {
        super(context);
        init(context);
    }

    public EndlessListViewNoHeader(final Context context,
            final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void addFooter() {
        if (noFooter()) {
            addFooterView(this.mLoadMoreView);
        }
    }

    private void init(final Context context) {
        this.mContext = context;
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        setOnItemClickListener(this);

        initHeaderAndFooter();
    }

    private void initHeaderAndFooter() {
        this.mInflater = LayoutInflater.from(this.mContext);
        this.mLoadMoreView = (ViewGroup) this.mInflater.inflate(
                R.layout.list_footer, null);
        this.mLoadMoreProgressView = (ProgressBar) this.mLoadMoreView
                .findViewById(R.id.list_footer_progress);
        this.mLoadMoreTextView = (TextView) this.mLoadMoreView
                .findViewById(R.id.list_footer_text);
        addFooterView(this.mLoadMoreView);

        setCacheColorHint(0);
        setSelector(getResources().getDrawable(R.drawable.list_selector));
        setDivider(getResources().getDrawable(R.drawable.separator));

    }

    public boolean isLoading() {
        return this.isLoading;
    }

    void log(final String message) {
        Log.d(EndlessListViewNoHeader.TAG, message);
    }

    public boolean noFooter() {
        return getFooterViewsCount() == 0;
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
            final int position, final long id) {
        if (AppContext.DEBUG) {
            log("onItemClick() list.size=" + parent.getCount()
                    + " adapter.size=" + parent.getAdapter().getCount()
                    + " position=" + position + " id=" + id);
        }
        final Object o = parent.getItemAtPosition(position);
        if (o == null) {
            if (position == (parent.getCount() - 1)) {
                setLoading();
            }
        } else {
            if (this.mOnRefreshListener != null) {
                this.mOnRefreshListener.onItemClick(this, position);
            }
        }
    }

    public void onLoadMoreComplete() {
        if (AppContext.DEBUG) {
            log("onLoadMoreComplete()");
        }
        setFooterStatus(EndlessListViewNoHeader.FOOTER_NORMAL);
    }

    public void onNoLoadMore() {
        setFooterStatus(EndlessListViewNoHeader.FOOTER_NONE);
    }

    public void removeFooter() {
        if (getFooterViewsCount() == 1) {
            removeFooterView(this.mLoadMoreView);
        }
    }

    public void restorePosition() {
        setSelectionFromTop(this.curPos, this.curPosTop);
    }

    public void savePosition() {
        this.curPos = getFirstVisiblePosition();
        final View v = getChildAt(this.curPos);
        this.curPosTop = (v == null) ? 0 : v.getTop();
    }

    protected void setFooterStatus(final int status) {
        if (status == EndlessListViewNoHeader.FOOTER_NONE) {
            this.isLoading = false;
            removeFooterView(this.mLoadMoreView);
            return;
        }

        if (noFooter()) {
            addFooterView(this.mLoadMoreView);
        }

        if (status == EndlessListViewNoHeader.FOOTER_HIDE) {
            this.isLoading = false;
            this.mLoadMoreView.setVisibility(View.GONE);
        } else if (status == EndlessListViewNoHeader.FOOTER_NORMAL) {
            this.isLoading = false;
            this.mLoadMoreView.setVisibility(View.VISIBLE);
            this.mLoadMoreProgressView.setVisibility(View.GONE);
            this.mLoadMoreTextView.setVisibility(View.VISIBLE);
        } else if (status == EndlessListViewNoHeader.FOOTER_LOADING) {
            // isLoading = true;
            this.mLoadMoreView.setVisibility(View.VISIBLE);
            this.mLoadMoreProgressView.setVisibility(View.VISIBLE);
            this.mLoadMoreTextView.setVisibility(View.GONE);
        }
    }

    public void setListSelection(final int pos) {
        post(new Runnable() {
            @Override
            public void run() {
                setSelection(pos);
                // View v = getChildAt(pos);
                // if (v != null) {
                // v.requestFocus();
                // }
            }
        });
    }

    public void setLoading() {
        if (this.isLoading) {
            return;
        }
        if (AppContext.DEBUG) {
            log("setFooterStatus(FOOTER_LOADING);");
        }
        this.isLoading = true;
        setFooterStatus(EndlessListViewNoHeader.FOOTER_LOADING);
        if (this.mOnRefreshListener != null) {
            if (AppContext.DEBUG) {
                log("onLoadMore()");
            }
            this.mOnRefreshListener.onLoadMore(this);
        }
    }

    public void setOnRefreshListener(final OnLoadDataListener li) {
        this.mOnRefreshListener = li;
    }
}
