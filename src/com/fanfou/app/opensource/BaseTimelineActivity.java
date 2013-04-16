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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.fanfou.app.opensource.adapter.StatusCursorAdapter;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.ui.ActionBar;
import com.fanfou.app.opensource.ui.ActionBar.AbstractAction;
import com.fanfou.app.opensource.ui.ActionManager;
import com.fanfou.app.opensource.ui.UIManager;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.StringHelper;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * @author mcxiaoke
 * @version 1.0 2011.07.10
 * @version 2.0 2011.10.19
 * @version 3.0 2011.10.21
 * @version 3.1 2011.10.24
 * @version 3.2 2011.10.29
 * @version 3.3 2011.11.18
 * @version 3.4 2011.12.13
 * @version 3.5 2011.12.23
 * @version 4.0 2013.03.09
 * 
 */
abstract class BaseTimelineActivity extends BaseActivity implements
        OnItemLongClickListener, OnItemClickListener,
        OnRefreshListener2<ListView>, OnLastItemVisibleListener {

    @SuppressLint("HandlerLeak")
    private class ResultHandler extends Handler {
        private final boolean doGetMore;

        public ResultHandler(final boolean doGetMore) {
            this.doGetMore = doGetMore;
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
            case Constants.RESULT_SUCCESS:
                if (!BaseTimelineActivity.this.isInitialized) {
                    showContent();
                }
                if (this.doGetMore) {
                    BaseTimelineActivity.this.mPullRefreshListView
                            .onRefreshComplete();
                } else {
                    BaseTimelineActivity.this.mPullRefreshListView
                            .onRefreshComplete();
                }
                updateUI();
                break;
            case Constants.RESULT_ERROR:
                final String errorMessage = msg.getData().getString(
                        Constants.EXTRA_ERROR);
                final int errorCode = msg.getData()
                        .getInt(Constants.EXTRA_CODE);

                if (!BaseTimelineActivity.this.isInitialized) {
                    showContent();
                }
                if (this.doGetMore) {
                    BaseTimelineActivity.this.mPullRefreshListView
                            .onRefreshComplete();
                } else {
                    BaseTimelineActivity.this.mPullRefreshListView
                            .onRefreshComplete();
                }
                CommonHelper.checkErrorCode(BaseTimelineActivity.this.mContext,
                        errorCode, errorMessage);
                break;
            default:
                break;
            }
        }

    }

    public class WriteAction extends AbstractAction {

        public WriteAction(final Context context) {
            super(R.drawable.i_write);
        }

        @Override
        public void performAction(final View view) {
            String text = null;
            if (BaseTimelineActivity.this.user != null) {
                text = "@" + BaseTimelineActivity.this.user.screenName + " ";
            }
            ActionManager.doWrite(BaseTimelineActivity.this.mContext, text);
        }
    }

    protected ActionBar mActionBar;
    protected ListView mListView;

    private PullToRefreshListView mPullRefreshListView;
    protected ViewGroup mEmptyView;

    protected Cursor mCursor;
    protected StatusCursorAdapter mCursorAdapter;
    protected String userId;

    protected String userName;

    protected User user;

    protected boolean isInitialized = false;

    private static final String TAG = BaseTimelineActivity.class
            .getSimpleName();

    private static final String LIST_STATE = "listState";

    private Parcelable mState = null;

    protected void doGetMore() {
        doRetrieve(true);
    }

    protected void doRefresh() {
        doRetrieve(false);
    }

    protected void doRetrieve(final boolean doGetMore) {
        doRetrieveImpl(new Messenger(new ResultHandler(doGetMore)), doGetMore);
    }

    protected abstract void doRetrieveImpl(final Messenger messenger,
            boolean isGetMore);

    protected abstract Cursor getCursor();

    protected abstract String getPageTitle();

    private void goTop() {
        if (this.mListView != null) {
            this.mListView.setSelection(0);
        }
    }

    protected void initCheckState() {
        if (this.mCursor.getCount() > 0) {
            showContent();
        } else {
            doRefresh();
            showProgress();
        }
    }

    protected void initialize() {
        this.mCursor = getCursor();
        this.mCursorAdapter = new StatusCursorAdapter(true, this, this.mCursor);

    }

    private void log(final String message) {
        Log.d(BaseTimelineActivity.TAG, message);
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
        if (AppContext.DEBUG) {
            log("onCreate");
        }
        if (parseIntent()) {
            initialize();
            setLayout();
            initCheckState();
        } else {
            finish();
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
            final int position, final long id) {
        final Cursor c = (Cursor) parent.getItemAtPosition(position);
        if (c != null) {
            final Status s = Status.parse(c);
            CommonHelper.goStatusPage(this.mContext, s);
        }
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent,
            final View view, final int position, final long id) {
        final Cursor c = (Cursor) parent.getItemAtPosition(position);
        showPopup(view, c);
        return true;
    }

    @Override
    public void onLastItemVisible() {

    }

    // @Override
    public void onLoadMore(final ListView viw) {
        doGetMore();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onPullDownToRefresh(
            final PullToRefreshBase<ListView> refreshView) {
        doRefresh();
    }

    @Override
    public void onPullUpToRefresh(final PullToRefreshBase<ListView> refreshView) {
        doGetMore();
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mState = savedInstanceState
                .getParcelable(BaseTimelineActivity.LIST_STATE);
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
            outState.putParcelable(BaseTimelineActivity.LIST_STATE, this.mState);
        }
    }

    protected boolean parseIntent() {
        final Intent intent = getIntent();
        this.user = (User) intent.getParcelableExtra(Constants.EXTRA_DATA);
        if (this.user == null) {
            this.userId = intent.getStringExtra(Constants.EXTRA_ID);
        } else {
            this.userId = this.user.id;
            this.userName = this.user.screenName;
        }
        return !StringHelper.isEmpty(this.userId);
    }

    /**
     * 初始化和设置ActionBar
     */
    private void setActionBar() {
        this.mActionBar = (ActionBar) findViewById(R.id.actionbar);
        this.mActionBar.setTitleClickListener(this);
        this.mActionBar.setRightAction(new WriteAction(this));
        this.mActionBar.setLeftAction(new ActionBar.BackAction(this));
        if (this.user != null) {
            this.mActionBar.setTitle(this.user.screenName + "的"
                    + getPageTitle());
        }
    }

    private void setLayout() {
        setContentView(R.layout.list_ptr);
        setActionBar();
        this.mEmptyView = (ViewGroup) findViewById(R.id.empty);
        this.mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.list);
        this.mPullRefreshListView.setOnRefreshListener(this);
        this.mListView = this.mPullRefreshListView.getRefreshableView();
        this.mListView.setOnItemLongClickListener(this);
        this.mListView.setOnItemClickListener(this);
        this.mListView.setAdapter(this.mCursorAdapter);
    }

    private void showContent() {
        this.isInitialized = true;
        this.mEmptyView.setVisibility(View.GONE);
        this.mListView.setVisibility(View.VISIBLE);
    }

    private void showPopup(final View view, final Cursor c) {
        if (c == null) {
            return;
        }
        final Status s = Status.parse(c);
        if (s == null) {
            return;
        }
        UIManager.showPopup(this.mContext, c, view, s);
    }

    private void showProgress() {
        this.mListView.setVisibility(View.GONE);
        this.mEmptyView.setVisibility(View.VISIBLE);
    }

    protected void updateUI() {
        if (this.mCursor != null) {
            this.mCursor.requery();
        }
    }

}
