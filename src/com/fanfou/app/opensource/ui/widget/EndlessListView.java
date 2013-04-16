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
public class EndlessListView extends ListView implements OnItemClickListener {
    public interface OnRefreshListener {
        public void onItemClick(ListView view, View row, int position);

        public void onLoadMore(ListView view);

        public void onRefresh(ListView view);
    }

    private static final String TAG = EndlessListView.class.getSimpleName();
    protected static final int FOOTER_NONE = 0;
    protected static final int FOOTER_HIDE = 1;
    protected static final int FOOTER_NORMAL = 2;

    protected static final int FOOTER_LOADING = 3;
    protected static final int HEADER_NONE = 10;
    protected static final int HEADER_HIDE = 11;
    protected static final int HEADER_NORMAL = 12;

    protected static final int HEADER_LOADING = 13;

    protected static final int MAX_OVERSCROLL_Y = 240;
    Context mContext;

    LayoutInflater mInflater;
    ViewGroup mRefershView;
    ProgressBar mRefreshProgressView;

    TextView mRefreshTextView;
    ViewGroup mLoadMoreView;
    ProgressBar mLoadMoreProgressView;

    // protected int mScrollState;
    // protected int mFirstVisible;
    // protected int mLastFirstVisible;
    // protected int mVisibleItemCount;
    // protected int mTotalItemCount;
    // protected int mMaxOverScrollY;

    TextView mLoadMoreTextView;

    OnRefreshListener mOnRefreshListener;
    protected boolean isLoading;

    protected boolean isRefresh;

    public EndlessListView(final Context context) {
        super(context);
        init(context);
    }

    public EndlessListView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void addFooter() {
        if (noFooter()) {
            addFooterView(this.mLoadMoreView);
        }
    }

    public void addHeader() {
        if (noHeader()) {
            addHeaderView(this.mRefershView);
        }
    }

    private void init(final Context context) {
        this.mContext = context;
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        setOnItemClickListener(this);

        setCacheColorHint(0);
        setSelector(getResources().getDrawable(R.drawable.list_selector));
        setDivider(getResources().getDrawable(R.drawable.separator));

        initHeaderAndFooter();
    }

    private void initHeaderAndFooter() {
        this.mInflater = LayoutInflater.from(this.mContext);

        this.mRefershView = (ViewGroup) this.mInflater.inflate(
                R.layout.list_header, null);
        this.mRefreshProgressView = (ProgressBar) this.mRefershView
                .findViewById(R.id.list_header_progress);
        this.mRefreshTextView = (TextView) this.mRefershView
                .findViewById(R.id.list_header_text);
        addHeaderView(this.mRefershView);

        this.mLoadMoreView = (ViewGroup) this.mInflater.inflate(
                R.layout.list_footer, null);
        this.mLoadMoreProgressView = (ProgressBar) this.mLoadMoreView
                .findViewById(R.id.list_footer_progress);
        this.mLoadMoreTextView = (TextView) this.mLoadMoreView
                .findViewById(R.id.list_footer_text);
        addFooterView(this.mLoadMoreView);
    }

    public boolean isLoading() {
        return this.isLoading;
    }

    public boolean isRefreshing() {
        return this.isRefresh;
    }

    void log(final String message) {
        Log.d(EndlessListView.TAG, message);
    }

    public boolean noFooter() {
        return getFooterViewsCount() == 0;
    }

    public boolean noHeader() {
        return getHeaderViewsCount() == 0;
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
            if (position == 0) {
                setRefreshing();
            } else if (position == (parent.getCount() - 1)) {
                setLoading();
            }
        } else {
            if (this.mOnRefreshListener != null) {
                this.mOnRefreshListener.onItemClick(this, view, position);
            }
        }
    }

    public void onLoadMoreComplete() {
        if (AppContext.DEBUG) {
            log("onLoadMoreComplete()");
        }
        setFooterStatus(EndlessListView.FOOTER_NORMAL);
    }

    public void onNoLoadMore() {
        setFooterStatus(EndlessListView.FOOTER_NONE);
    }

    public void onNoRefresh() {
        setHeaderStatus(EndlessListView.HEADER_NONE);
    }

    public void onRefreshComplete() {
        if (this.isRefresh) {
            if (AppContext.DEBUG) {
                log("onRefreshComplete()");
            }
            setListSelection(1);
            setHeaderStatus(EndlessListView.HEADER_NORMAL);
        }
    }

    protected void reachBottom() {
        if (AppContext.DEBUG) {
            Log.d(EndlessListView.TAG, "readBottom()");
        }
        if (!noFooter()) {
            setLoading();
        }
    }

    protected void reachTop() {
        if (AppContext.DEBUG) {
            Log.d(EndlessListView.TAG, "reachTop()");
        }
        setRefreshing();
    }

    public void removeFooter() {
        if (getFooterViewsCount() == 1) {
            removeFooterView(this.mLoadMoreView);
        }
    }

    public void removeHeader() {
        if (getHeaderViewsCount() == 1) {
            removeHeaderView(this.mRefershView);
        }
    }

    protected void setFooterStatus(final int status) {
        if (status == EndlessListView.FOOTER_NONE) {
            this.isLoading = false;
            removeFooterView(this.mLoadMoreView);
            return;
        }

        if (noFooter()) {
            addFooterView(this.mLoadMoreView);
        }

        if (status == EndlessListView.FOOTER_HIDE) {
            this.isLoading = false;
            this.mLoadMoreView.setVisibility(View.GONE);
        } else if (status == EndlessListView.FOOTER_NORMAL) {
            this.isLoading = false;
            this.mLoadMoreView.setVisibility(View.VISIBLE);
            this.mLoadMoreProgressView.setVisibility(View.GONE);
            this.mLoadMoreTextView.setVisibility(View.VISIBLE);
        } else if (status == EndlessListView.FOOTER_LOADING) {
            // isLoading = true;
            this.mLoadMoreView.setVisibility(View.VISIBLE);
            this.mLoadMoreProgressView.setVisibility(View.VISIBLE);
            this.mLoadMoreTextView.setVisibility(View.GONE);
        }
    }

    protected void setHeaderStatus(final int status) {
        if (status == EndlessListView.HEADER_NONE) {
            this.isRefresh = false;
            removeHeaderView(this.mRefershView);
            return;
        }

        if (status == EndlessListView.HEADER_HIDE) {
            this.isRefresh = false;
            this.mRefershView.setVisibility(View.GONE);
        } else if (status == EndlessListView.HEADER_NORMAL) {
            this.isRefresh = false;
            this.mRefershView.setVisibility(View.VISIBLE);
            this.mRefreshProgressView.setVisibility(View.GONE);
            this.mRefreshTextView.setVisibility(View.VISIBLE);
        } else if (status == EndlessListView.HEADER_LOADING) {
            // isRefresh = true;
            this.mRefershView.setVisibility(View.VISIBLE);
            this.mRefreshProgressView.setVisibility(View.VISIBLE);
            this.mRefreshTextView.setVisibility(View.GONE);
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
        setFooterStatus(EndlessListView.FOOTER_LOADING);
        if (this.mOnRefreshListener != null) {
            if (AppContext.DEBUG) {
                log("onLoadMore()");
            }
            this.mOnRefreshListener.onLoadMore(this);
        }
    }

    // @Override
    // public void onScrollStateChanged(AbsListView view, int scrollState) {
    // if(Build.VERSION.SDK_INT<9){
    // return;
    // }
    // mScrollState = scrollState;
    // switch (scrollState) {
    // case SCROLL_STATE_FLING:
    // if(App.DEBUG){
    // Log.d(TAG, "FLING mFirstVisible=" + mFirstVisible);
    // }
    // break;
    // case SCROLL_STATE_IDLE:
    // if(App.DEBUG){
    // Log.d(TAG, "IDLE mFirstVisible=" + mFirstVisible
    // + " mLastFirstVisible=" + mLastFirstVisible);
    // }
    // if ( mLastFirstVisible + mVisibleItemCount>= mTotalItemCount
    // && mFirstVisible + mVisibleItemCount>= mTotalItemCount) {
    // reachBottom();
    // } else if (mLastFirstVisible <3 && mFirstVisible == 0) {
    // reachTop();
    // }
    // break;
    // case SCROLL_STATE_TOUCH_SCROLL:
    // mLastFirstVisible = mFirstVisible;
    // if(App.DEBUG){
    // Log.i(TAG, "TOUCH_SCROLL mFirstVisible=" + mFirstVisible);
    // }
    // break;
    // default:
    // break;
    // }
    // }

    // @Override
    // public void onScroll(AbsListView view, int firstVisibleItem,
    // int visibleItemCount, int totalItemCount) {
    // mFirstVisible = firstVisibleItem;
    // mVisibleItemCount = visibleItemCount;
    // mTotalItemCount = totalItemCount;
    // }

    // @Override
    // protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
    // int scrollY, int scrollRangeX, int scrollRangeY,
    // int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
    // return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
    // scrollRangeX, scrollRangeY, maxOverScrollX, mMaxOverScrollY,
    // isTouchEvent);
    // }

    public void setOnRefreshListener(final OnRefreshListener li) {
        this.mOnRefreshListener = li;
    }

    public void setRefreshing() {
        if (this.isRefresh) {
            return;
        }
        if (AppContext.DEBUG) {
            log("setHeaderStatus(HEADER_LOADING);");
        }
        this.isRefresh = true;
        setHeaderStatus(EndlessListView.HEADER_LOADING);
        if (this.mOnRefreshListener != null) {
            if (AppContext.DEBUG) {
                log("onRefresh()");
            }
            this.mOnRefreshListener.onRefresh(this);
        }
    }
}
