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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.fanfou.app.opensource.adapter.ConversationAdapter;
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
 * @version 1.0 2011.10.25
 * @version 1.1 2011.10.26
 * @version 1.2 2011.10.28
 * @version 1.3 2011.11.07
 * @version 2.0 2011.11.11
 * @version 3.0 2011.11.18
 * 
 */
public class ConversationPage extends BaseActivity implements
        OnRefreshListener, OnItemLongClickListener {

    private class FetchTask
            extends
            AsyncTask<Void, com.fanfou.app.opensource.api.bean.Status, List<Status>> {

        @Override
        protected List<com.fanfou.app.opensource.api.bean.Status> doInBackground(
                final Void... params) {
            final ApiClient api = AppContext.getApiClient();
            final String id = ConversationPage.this.mStatus.id;
            try {
                if (!StringHelper.isEmpty(id)) {
                    return api.contextTimeline(id, Constants.FORMAT,
                            Constants.MODE);
                }
            } catch (final ApiException e) {
                if (AppContext.DEBUG) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(
                final List<com.fanfou.app.opensource.api.bean.Status> result) {
            if ((result != null) && (result.size() > 0)) {
                ConversationPage.this.mThread.addAll(result);
            }
            ConversationPage.this.mListView.onNoRefresh();
        }

    }

    protected ActionBar mActionBar;
    protected EndlessListView mListView;

    protected ViewGroup mEmptyView;

    protected ConversationAdapter mStatusAdapter;

    private List<Status> mThread;

    private Status mStatus;

    private static final String tag = ConversationPage.class.getSimpleName();

    private void doFetchThreads() {
        new FetchTask().execute();
        this.mListView.setRefreshing();
    }

    protected void initialize() {
        this.mThread = new ArrayList<Status>();
        this.mStatusAdapter = new ConversationAdapter(this, this.mThread);
    }

    private void log(final String message) {
        Log.d(ConversationPage.tag, message);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate");
        if (parseIntent()) {
            initialize();
            setLayout();
            doFetchThreads();
        } else {
            finish();
        }

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
    }

    @Override
    protected void onPause() {
        AppContext.active = false;
        super.onPause();
    }

    @Override
    public void onRefresh(final ListView view) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppContext.active = true;
    }

    protected boolean parseIntent() {
        final Intent intent = getIntent();
        this.mStatus = (Status) intent.getParcelableExtra(Constants.EXTRA_DATA);
        return this.mStatus != null;
    }

    /**
     * 初始化和设置ActionBar
     */
    private void setActionBar() {
        this.mActionBar = (ActionBar) findViewById(R.id.actionbar);
        this.mActionBar.setLeftAction(new ActionBar.BackAction(this));
        this.mActionBar.setTitle("对话");
    }

    private void setLayout() {
        setContentView(R.layout.list);

        setActionBar();

        this.mEmptyView = (ViewGroup) findViewById(R.id.empty);
        this.mEmptyView.setVisibility(View.GONE);

        this.mListView = (EndlessListView) findViewById(R.id.list);
        this.mListView.setOnRefreshListener(this);
        this.mListView.setOnItemLongClickListener(this);
        this.mListView.setAdapter(this.mStatusAdapter);
        this.mListView.setHeaderDividersEnabled(false);
        this.mListView.setFooterDividersEnabled(false);
        this.mListView.removeFooter();
    }

    private void showPopup(final View view, final Status s) {
        if (s != null) {
            UIManager.showPopup(this.mContext, view, s, this.mStatusAdapter);
        }
    }

}
