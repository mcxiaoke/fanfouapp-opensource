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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

import com.fanfou.app.opensource.adapter.UserCursorAdapter;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.db.Contents.BasicColumns;
import com.fanfou.app.opensource.db.Contents.UserInfo;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.service.FanfouServiceManager;
import com.fanfou.app.opensource.ui.ActionBar;
import com.fanfou.app.opensource.ui.TextChangeListener;
import com.fanfou.app.opensource.ui.widget.EndlessListView;
import com.fanfou.app.opensource.ui.widget.EndlessListView.OnRefreshListener;
import com.fanfou.app.opensource.util.CommonHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.11.09
 * @version 1.1 2011.11.21
 * 
 */

// select direct message target
public class UserSelectPage extends BaseActivity implements OnRefreshListener,
        FilterQueryProvider {

    private class MyTextWatcher extends TextChangeListener {
        @Override
        public void onTextChanged(final CharSequence s, final int start,
                final int before, final int count) {
            UserSelectPage.this.mCursorAdapter.getFilter().filter(s.toString());
        }
    }

    protected class ResultHandler extends Handler {
        private final boolean doGetMore;

        public ResultHandler(final boolean doGetMore) {
            this.doGetMore = doGetMore;
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
            case Constants.RESULT_SUCCESS:
                if (!UserSelectPage.this.isInitialized) {
                    showContent();
                }
                msg.getData().getInt(Constants.EXTRA_COUNT);
                if (this.doGetMore) {
                    UserSelectPage.this.mListView.onLoadMoreComplete();
                } else {
                    UserSelectPage.this.mListView.onRefreshComplete();
                }
                updateUI();
                break;
            case Constants.RESULT_ERROR:
                final String errorMessage = msg.getData().getString(
                        Constants.EXTRA_ERROR);
                final int errorCode = msg.getData()
                        .getInt(Constants.EXTRA_CODE);
                if (!UserSelectPage.this.isInitialized) {
                    showContent();
                }
                if (this.doGetMore) {
                    UserSelectPage.this.mListView.onLoadMoreComplete();
                } else {
                    UserSelectPage.this.mListView.onRefreshComplete();
                }
                CommonHelper.checkErrorCode(UserSelectPage.this.mContext,
                        errorCode, errorMessage);
                break;
            default:
                break;
            }
        }

    }

    protected ActionBar mActionBar;

    protected EndlessListView mListView;

    protected ViewGroup mEmptyView;
    protected EditText mEditText;

    protected Cursor mCursor;

    protected UserCursorAdapter mCursorAdapter;

    private boolean isInitialized = false;

    private int page = 1;

    private static final String tag = UserSelectPage.class.getSimpleName();

    private static final String LIST_STATE = "listState";

    private Parcelable mState = null;

    protected void doGetMore() {
        this.page++;
        doRetrieve(true);
    }

    protected void doRefresh() {
        this.page = 1;
        doRetrieve(false);
    }

    protected void doRetrieve(final boolean isGetMore) {
        FanfouServiceManager.doFetchFriends(this, new ResultHandler(isGetMore),
                this.page, AppContext.getUserId());
    }

    protected void initCheckState() {
        if (this.mCursor.getCount() > 0) {
            showContent();
        } else {
            doRefresh();
            showProgress();
        }
    }

    protected void initCursor() {
        final String where = BasicColumns.TYPE + "=? AND "
                + BasicColumns.OWNER_ID + "=?";
        final String[] whereArgs = new String[] {
                String.valueOf(Constants.TYPE_USERS_FRIENDS),
                AppContext.getUserId() };
        this.mCursor = managedQuery(UserInfo.CONTENT_URI, UserInfo.COLUMNS,
                where, whereArgs, null);
    }

    protected void initialize() {
        initCursor();
    }

    private void log(final String message) {
        Log.i(UserSelectPage.tag, message);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppContext.DEBUG) {
            log("onCreate");
        }
        initialize();
        setLayout();
        initCheckState();
    }

    @Override
    public void onItemClick(final ListView view, final View row,
            final int position) {
        final Cursor c = (Cursor) view.getItemAtPosition(position);
        final User u = User.parse(c);
        if (u != null) {
            if (AppContext.DEBUG) {
                log("userId=" + u.id + " username=" + u.screenName);
            }
            onSelected(u);
        }
    }

    @Override
    public void onLoadMore(final ListView view) {
        doGetMore();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRefresh(final ListView view) {
        doRefresh();
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mState = savedInstanceState
                .getParcelable(UserSelectPage.LIST_STATE);
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
            outState.putParcelable(UserSelectPage.LIST_STATE, this.mState);
        }
    }

    private void onSelected(final User user) {
        final Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_ID, user.id);
        intent.putExtra(Constants.EXTRA_USER_NAME, user.screenName);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppContext.getImageLoader().clearQueue();
    }

    @Override
    public Cursor runQuery(final CharSequence constraint) {
        final String where = BasicColumns.TYPE + " = "
                + Constants.TYPE_USERS_FRIENDS + " AND "
                + BasicColumns.OWNER_ID + " = '" + AppContext.getUserId()
                + "' AND (" + UserInfo.SCREEN_NAME + " like '%" + constraint
                + "%' OR " + BasicColumns.ID + " like '%" + constraint + "%' )";
        ;
        return managedQuery(UserInfo.CONTENT_URI, UserInfo.COLUMNS, where,
                null, null);
    }

    /**
     * 初始化和设置ActionBar
     */
    private void setActionBar() {
        this.mActionBar = (ActionBar) findViewById(R.id.actionbar);
        this.mActionBar.setLeftAction(new ActionBar.BackAction(this));
        this.mActionBar.setTitle("我关注的人");
    }

    private void setLayout() {
        setContentView(R.layout.list_users);

        setActionBar();

        this.mEmptyView = (ViewGroup) findViewById(R.id.empty);

        this.mEditText = (EditText) findViewById(R.id.choose_input);
        this.mEditText.addTextChangedListener(new MyTextWatcher());

        this.mListView = (EndlessListView) findViewById(R.id.list);
        this.mListView.setOnRefreshListener(this);

        this.mCursorAdapter = new UserCursorAdapter(this.mContext, this.mCursor);
        this.mCursorAdapter.setFilterQueryProvider(this);

        this.mListView.setAdapter(this.mCursorAdapter);
        registerForContextMenu(this.mListView);

        this.mListView.post(new Runnable() {

            @Override
            public void run() {
                UserSelectPage.this.mListView.setSelection(1);
            }
        });
    }

    private void showContent() {
        if (AppContext.DEBUG) {
            log("showContent()");
        }
        this.isInitialized = true;
        this.mEmptyView.setVisibility(View.GONE);
        this.mListView.setVisibility(View.VISIBLE);
    }

    private void showProgress() {
        this.mListView.setVisibility(View.GONE);
        this.mEmptyView.setVisibility(View.VISIBLE);
    }

    protected void updateUI() {
        if (AppContext.DEBUG) {
            log("updateUI()");
        }
        if (this.mCursor != null) {
            this.mCursor.requery();
        }
    }

}
