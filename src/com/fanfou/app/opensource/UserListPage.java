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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.fanfou.app.opensource.ui.ActionManager;
import com.fanfou.app.opensource.ui.TextChangeListener;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.StringHelper;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.10
 * @version 1.5 2011.10.29
 * @version 1.6 2011.11.07
 * @version 2.0 2011.11.07
 * @version 2.1 2011.11.09
 * @version 2.2 2011.11.18
 * @version 2.3 2011.11.21
 * @version 2.4 2011.12.13
 * @version 2.5 2011.12.23
 * 
 */
public class UserListPage extends BaseActivity implements
        OnRefreshListener2<ListView>, FilterQueryProvider, OnItemClickListener {

    private class MyTextWatcher extends TextChangeListener {
        @Override
        public void onTextChanged(final CharSequence s, final int start,
                final int before, final int count) {
            UserListPage.this.mCursorAdapter.getFilter().filter(
                    s.toString().trim());
        }
    }

    protected class ResultHandler extends Handler {
        public ResultHandler(final boolean doGetMore) {
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
            case Constants.RESULT_SUCCESS:
                if (!UserListPage.this.isInitialized) {
                    showContent();
                }
                UserListPage.this.mPullToRefreshListView.onRefreshComplete();
                msg.getData().getInt(Constants.EXTRA_COUNT);
                updateUI();
                break;
            case Constants.RESULT_ERROR:
                UserListPage.this.mPullToRefreshListView.onRefreshComplete();
                final String errorMessage = msg.getData().getString(
                        Constants.EXTRA_ERROR);
                final int errorCode = msg.getData()
                        .getInt(Constants.EXTRA_CODE);
                if (!UserListPage.this.isInitialized) {
                    showContent();
                }
                CommonHelper.checkErrorCode(UserListPage.this.mContext,
                        errorCode, errorMessage);
                break;
            default:
                break;
            }
        }

    }

    protected ActionBar mActionBar;
    protected PullToRefreshListView mPullToRefreshListView;

    protected ListView mListView;

    protected ViewGroup mEmptyView;
    protected EditText mEditText;

    protected Cursor mCursor;
    protected UserCursorAdapter mCursorAdapter;
    protected String userId;
    protected String userName;

    protected User user;

    protected int type;

    protected int page = 1;

    private boolean isInitialized = false;

    private static final String tag = UserListPage.class.getSimpleName();

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
        if (this.userId == null) {
            if (AppContext.DEBUG) {
                log("userId is null");
            }
            return;
        }
        final Handler handler = new ResultHandler(isGetMore);
        if (this.type == Constants.TYPE_USERS_FRIENDS) {
            FanfouServiceManager.doFetchFriends(this, handler, this.page,
                    this.userId);
        } else {
            FanfouServiceManager.doFetchFollowers(this, handler, this.page,
                    this.userId);
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

    protected void initCursor() {
        final String where = BasicColumns.TYPE + "=? AND "
                + BasicColumns.OWNER_ID + "=?";
        final String[] whereArgs = new String[] { String.valueOf(this.type),
                this.userId };
        this.mCursor = managedQuery(UserInfo.CONTENT_URI, UserInfo.COLUMNS,
                where, whereArgs, null);
    }

    protected void initialize() {
        initCursor();
    }

    private void log(final String message) {
        Log.d(UserListPage.tag, message);
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
        final User u = User.parse(c);
        if (u != null) {
            if (AppContext.DEBUG) {
                log("userId=" + u.id + " username=" + u.screenName);
            }
            ActionManager.doProfile(this.mContext, u);
        }
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
        this.mState = savedInstanceState.getParcelable(UserListPage.LIST_STATE);
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
            outState.putParcelable(UserListPage.LIST_STATE, this.mState);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (AppContext.isWifi()) {
            AppContext.getImageLoader().clearQueue();
        }
    }

    protected boolean parseIntent() {
        final Intent intent = getIntent();
        this.type = intent.getIntExtra(Constants.EXTRA_TYPE,
                Constants.TYPE_USERS_FRIENDS);
        this.user = (User) intent.getParcelableExtra(Constants.EXTRA_DATA);
        if (this.user == null) {
            this.userId = intent.getStringExtra(Constants.EXTRA_ID);
        } else {
            this.userId = this.user.id;
            this.userName = this.user.screenName;
        }
        return !StringHelper.isEmpty(this.userId);
    }

    @Override
    public Cursor runQuery(final CharSequence constraint) {
        final String where = BasicColumns.TYPE + " = " + this.type + " AND "
                + BasicColumns.OWNER_ID + " = '" + this.userId + "' AND ("
                + UserInfo.SCREEN_NAME + " like '%" + constraint + "%' OR "
                + BasicColumns.ID + " like '%" + constraint + "%' )";
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
        if (this.user != null) {
            if (this.type == Constants.TYPE_USERS_FRIENDS) {
                this.mActionBar.setTitle(this.user.screenName + "关注的人");
            } else if (this.type == Constants.TYPE_USERS_FOLLOWERS) {
                this.mActionBar.setTitle("关注" + this.user.screenName + "的人");
            }
        }
    }

    // private static final int CONTEXT_MENU_ID_TIMELINE=1001;
    // private static final int CONTEXT_MENU_ID_FAVORITES=1002;
    // private static final int CONTEXT_MENU_ID_FRIENDS=1003;
    // private static final int CONTEXT_MENU_ID_FOLLOWERS=1004;
    // private static final int CONTEXT_MENU_ID_FOLLOW=1005;
    // private static final int CONTEXT_MENU_ID_UNFOLLOW=1006;
    // private static final int CONTEXT_MENU_ID_BLOCK=1007;

    // @Override
    // public void onCreateContextMenu(ContextMenu menu, View v,
    // ContextMenuInfo menuInfo) {
    // MenuItem timeline=menu.add(0, CONTEXT_MENU_ID_TIMELINE,
    // CONTEXT_MENU_ID_TIMELINE, "查看消息");
    // MenuItem favorites=menu.add(0, CONTEXT_MENU_ID_FAVORITES,
    // CONTEXT_MENU_ID_FAVORITES, "查看收藏");
    // MenuItem friends=menu.add(0, CONTEXT_MENU_ID_FRIENDS,
    // CONTEXT_MENU_ID_FRIENDS, "查看关注的人");
    // MenuItem followers=menu.add(0, CONTEXT_MENU_ID_FOLLOWERS,
    // CONTEXT_MENU_ID_FOLLOWERS, "查看关注者");
    // MenuItem follow=menu.add(0, CONTEXT_MENU_ID_FOLLOW,
    // CONTEXT_MENU_ID_FOLLOW, "添加关注");
    // MenuItem unfollow=menu.add(0,
    // CONTEXT_MENU_ID_UNFOLLOW,CONTEXT_MENU_ID_UNFOLLOW, "取消关注");
    // MenuItem delete=menu.add(0, CONTEXT_MENU_ID_BLOCK, CONTEXT_MENU_ID_BLOCK,
    // "删除关注");
    // }

    private void setLayout() {
        setContentView(R.layout.list_users);

        setActionBar();

        this.mEmptyView = (ViewGroup) findViewById(R.id.empty);

        this.mEditText = (EditText) findViewById(R.id.choose_input);
        this.mEditText.addTextChangedListener(new MyTextWatcher());

        this.mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.list);
        this.mPullToRefreshListView.setOnRefreshListener(this);
        this.mListView = this.mPullToRefreshListView.getRefreshableView();

        this.mCursorAdapter = new UserCursorAdapter(this.mContext, this.mCursor);
        this.mCursorAdapter.setFilterQueryProvider(this);

        this.mListView.setOnItemClickListener(this);
        this.mListView.setAdapter(this.mCursorAdapter);
        registerForContextMenu(this.mListView);

        this.mListView.post(new Runnable() {

            @Override
            public void run() {
                UserListPage.this.mListView.setSelection(1);
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
