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
package com.fanfou.app.opensource;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.fanfou.app.opensource.adapter.SearchResultsAdapter;
import com.fanfou.app.opensource.api.ApiClient;
import com.fanfou.app.opensource.api.ApiException;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.ui.ActionBar;
import com.fanfou.app.opensource.ui.UIManager;
import com.fanfou.app.opensource.ui.widget.EndlessListView;
import com.fanfou.app.opensource.ui.widget.EndlessListView.OnRefreshListener;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.08.05
 * @version 1.1 2011.10.12
 * @version 1.5 2011.10.24
 * @version 1.6 2011.11.21
 * @version 1.7 2011.11.25
 * 
 */
public class SearchResultsPage extends BaseActivity implements
        OnRefreshListener, OnItemLongClickListener {

    private class SearchTask extends AsyncTask<Void, Void, List<Status>> {

        @Override
        protected List<com.fanfou.app.opensource.api.bean.Status> doInBackground(
                final Void... params) {
            if (StringHelper.isEmpty(SearchResultsPage.this.keyword)) {
                return null;
            }
            List<com.fanfou.app.opensource.api.bean.Status> result = null;

            int count = Constants.DEFAULT_TIMELINE_COUNT;
            if (AppContext.isWifi()) {
                count = Constants.MAX_TIMELINE_COUNT;
            }
            try {
                result = SearchResultsPage.this.api.search(
                        SearchResultsPage.this.keyword, null,
                        SearchResultsPage.this.maxId, count, Constants.FORMAT,
                        Constants.MODE);
            } catch (final ApiException e) {
                if (AppContext.DEBUG) {
                    e.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(
                final List<com.fanfou.app.opensource.api.bean.Status> result) {
            if (!SearchResultsPage.this.showListView) {
                showContent();
            }
            if ((result != null) && (result.size() > 0)) {

                final int size = result.size();
                log("result size=" + size);
                SearchResultsPage.this.maxId = result.get(size - 1).id;
                log("maxId=" + SearchResultsPage.this.maxId);

                SearchResultsPage.this.mStatuses.addAll(result);
                updateUI(size < 20);
            }
        }

        @Override
        protected void onPreExecute() {
        }

    }

    protected ActionBar mActionBar;
    protected EndlessListView mListView;

    protected ViewGroup mEmptyView;

    protected SearchResultsAdapter mStatusAdapter;

    private List<Status> mStatuses;
    protected String keyword;

    protected String maxId;

    private ApiClient api;

    private boolean showListView = false;

    private static final String tag = SearchResultsPage.class.getSimpleName();

    private static final String LIST_STATE = "listState";

    private Parcelable mState = null;

    private void doSearch() {
        if (this.keyword != null) {
            if (AppContext.DEBUG) {
                log("doSearch() keyword=" + this.keyword);
            }
            this.mActionBar.setTitle(this.keyword);
            new SearchTask().execute();
        }

    }

    private void goTop() {
        if (this.mListView != null) {
            this.mListView.setSelection(0);
        }
    }

    protected void initialize() {
        this.mStatuses = new ArrayList<Status>();
        this.api = AppContext.getApiClient();
    }

    private void log(final String message) {
        Log.d(SearchResultsPage.tag, message);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
        case R.id.actionbar_title:
            goTop();
            break;
        default:
            break;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
        setLayout();
        search();
    }

    @Override
    public void onItemClick(final ListView view, final View row,
            final int position) {
        final Status s = (Status) view.getItemAtPosition(position);
        if (s != null) {
            CommonHelper.goStatusPage(this.mContext, s);
        }
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent,
            final View view, final int position, final long id) {
        final Status s = (Status) parent.getItemAtPosition(position);
        showPopup(view, s);
        return true;
    }

    @Override
    public void onLoadMore(final ListView view) {
        doSearch();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        setIntent(intent);
        search();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRefresh(final ListView view) {
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mState = savedInstanceState
                .getParcelable(SearchResultsPage.LIST_STATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((this.mState != null) && (this.mListView != null)) {
            this.mListView.onRestoreInstanceState(this.mState);
            this.mState = null;
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mListView != null) {
            this.mState = this.mListView.onSaveInstanceState();
            outState.putParcelable(SearchResultsPage.LIST_STATE, this.mState);
        }
    }

    protected void parseIntent() {
        final Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            this.keyword = intent.getStringExtra(SearchManager.QUERY);
            if (AppContext.DEBUG) {
                log("parseIntent() keyword=" + this.keyword);
            }
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            final Uri data = intent.getData();
            if (data != null) {
                this.keyword = data.getLastPathSegment();
                log("parseIntent() keyword=" + this.keyword);
            }
        }
    }

    protected void search() {
        parseIntent();
        this.mStatuses.clear();
        doSearch();
        showProgress();

    }

    /**
     * 初始化和设置ActionBar
     */
    private void setActionBar() {
        this.mActionBar = (ActionBar) findViewById(R.id.actionbar);
        this.mActionBar.setTitle("搜索结果");
        this.mActionBar.setTitleClickListener(this);
        this.mActionBar.setRightAction(new ActionBar.SearchAction(this));
        this.mActionBar.setLeftAction(new ActionBar.BackAction(this));
    }

    private void setLayout() {
        setContentView(R.layout.list);

        setActionBar();
        this.mEmptyView = (ViewGroup) findViewById(R.id.empty);
        this.mListView = (EndlessListView) findViewById(R.id.list);
        this.mListView.setOnItemLongClickListener(this);
        this.mListView.setOnRefreshListener(this);
    }

    private void showContent() {
        this.showListView = true;

        this.mStatusAdapter = new SearchResultsAdapter(this, this.mStatuses);
        this.mListView.setAdapter(this.mStatusAdapter);

        this.mEmptyView.setVisibility(View.GONE);
        this.mListView.removeHeader();
        this.mListView.setVisibility(View.VISIBLE);
    }

    private void showPopup(final View view, final Status s) {
        if ((s == null) || s.isNull()) {
            return;
        }
        UIManager.showPopup(this, view, s, this.mStatusAdapter, this.mStatuses);
    }

    private void showProgress() {
        this.showListView = false;
        this.mListView.setVisibility(View.GONE);
        this.mEmptyView.setVisibility(View.VISIBLE);
    }

    protected void updateUI(final boolean noMore) {
        this.mStatusAdapter.updateDataAndUI(this.mStatuses, this.keyword);
        if (noMore) {
            this.mListView.onNoLoadMore();
        } else {
            this.mListView.onLoadMoreComplete();
        }
    }

}
