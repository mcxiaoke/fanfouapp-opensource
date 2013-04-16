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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

import com.fanfou.app.opensource.adapter.UserChooseCursorAdapter;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.db.Contents.BasicColumns;
import com.fanfou.app.opensource.db.Contents.UserInfo;
import com.fanfou.app.opensource.service.AutoCompleteService;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.service.FanfouServiceManager;
import com.fanfou.app.opensource.service.WakefulIntentService;
import com.fanfou.app.opensource.ui.ActionBar;
import com.fanfou.app.opensource.ui.ActionBar.AbstractAction;
import com.fanfou.app.opensource.ui.TextChangeListener;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.21
 * @version 2.0 2011.10.24
 * @version 2.1 2011.10.26
 * @version 2.2 2011.11.01
 * @version 2.3 2011.11.07
 * @version 2.4 2011.11.18
 * @version 2.5 2011.11.21
 * @version 2.6 2011.11.25
 * @version 2.7 2011.12.02
 * @version 2.8 2011.12.23
 */
public class UserChoosePage extends BaseActivity implements
        FilterQueryProvider, OnItemClickListener {

    private class ConfirmAction extends AbstractAction {

        public ConfirmAction() {
            super(R.drawable.ic_ok);
        }

        @Override
        public void performAction(final View view) {
            doAddUserNames();
        }

    }

    private class MyTextWatcher extends TextChangeListener {
        @Override
        public void onTextChanged(final CharSequence s, final int start,
                final int before, final int count) {
            resetChoices();
            UserChoosePage.this.mCursorAdapter.getFilter().filter(
                    s.toString().trim());
        }
    }

    protected class ResultHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
            case Constants.RESULT_SUCCESS:
                if (!UserChoosePage.this.isInitialized) {
                    showContent();
                }
                final int count = msg.getData().getInt(Constants.EXTRA_COUNT);
                if (count > 0) {
                    updateUI();
                }
                break;
            case Constants.RESULT_ERROR:
                msg.getData().getInt(Constants.EXTRA_CODE);
                final String errorMessage = msg.getData().getString(
                        Constants.EXTRA_ERROR);
                CommonHelper.notify(UserChoosePage.this.mContext, errorMessage);
                if (!UserChoosePage.this.isInitialized) {
                    showContent();
                }
                break;
            default:
                break;
            }
        }

    }

    protected ActionBar mActionBar;

    protected ListView mListView;
    protected EditText mEditText;
    protected ViewGroup mEmptyView;
    private ViewStub mViewStub;

    private View mButtonGroup;
    private Button okButton;

    private Button cancelButton;

    protected Cursor mCursor;

    protected UserChooseCursorAdapter mCursorAdapter;

    private List<String> mUserNames;

    protected int page = 1;

    private boolean isInitialized = false;

    private static final String tag = UserChoosePage.class.getSimpleName();

    private void doAddUserNames() {
        if (!this.mUserNames.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            for (final String screenName : this.mUserNames) {
                sb.append("@").append(screenName).append(" ");
            }
            if (AppContext.DEBUG) {
                log("User Names: " + sb.toString());
            }
            final Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_TEXT, sb.toString());
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    protected void doGetMore() {
        this.page++;
        doRetrieve(true);
    }

    protected void doRefresh() {
        this.page = 1;
        doRetrieve(false);
    }

    protected void doRetrieve(final boolean isGetMore) {
        FanfouServiceManager.doFetchFriends(this, new ResultHandler(),
                this.page, AppContext.getUserId());
    }

    protected void initCheckState() {
        if (this.mCursor.getCount() > 0) {
            showContent();
        } else {
            doRefresh();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    WakefulIntentService.sendWakefulWork(
                            UserChoosePage.this.mContext,
                            AutoCompleteService.class);
                }
            }, 30000);
            showProgress();
        }
    }

    protected void initCursorAdapter() {
        final String where = BasicColumns.TYPE + "=? AND "
                + BasicColumns.OWNER_ID + "=?";
        final String[] whereArgs = new String[] {
                String.valueOf(Constants.TYPE_USERS_FRIENDS),
                AppContext.getUserId() };
        this.mCursor = managedQuery(UserInfo.CONTENT_URI, UserInfo.COLUMNS,
                where, whereArgs, null);

        this.mCursorAdapter = new UserChooseCursorAdapter(this.mContext,
                this.mCursor);
        this.mCursorAdapter.setFilterQueryProvider(this);
    }

    protected void initialize() {
        this.mUserNames = new ArrayList<String>();
        initCursorAdapter();

    }

    private void initViewStub() {

        this.mButtonGroup = this.mViewStub.inflate();
        this.mViewStub = null;

        this.okButton = (Button) findViewById(R.id.button_ok);
        this.okButton.setText(android.R.string.ok);
        this.okButton.setOnClickListener(this);

        this.cancelButton = (Button) findViewById(R.id.button_cancel);
        this.cancelButton.setText(android.R.string.cancel);
        this.cancelButton.setOnClickListener(this);
    }

    private void log(final String message) {
        Log.d(UserChoosePage.tag, message);
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        switch (id) {
        case R.id.button_ok:
            doAddUserNames();
            break;
        case R.id.button_cancel:
            finish();
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
        initialize();
        setLayout();
        initCheckState();
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
            final int position, final long id) {

        final SparseBooleanArray sba = this.mListView.getCheckedItemPositions();
        this.mUserNames.clear();
        for (int i = 0; i < sba.size(); i++) {
            final int key = sba.keyAt(i);
            final boolean value = sba.valueAt(i);
            this.mCursorAdapter.setItemChecked(key, value);
            if (AppContext.DEBUG) {
                log("sba.values i=" + i + " key=" + key + " value=" + value
                        + " cursor.size=" + this.mCursor.getCount()
                        + " adapter.size=" + this.mCursorAdapter.getCount());
            }
            if (value) {
                final Cursor cc = (Cursor) this.mCursorAdapter.getItem(key);
                final User uu = User.parse(cc);
                this.mUserNames.add(uu.screenName);
            }
        }

        if (AppContext.DEBUG) {
            log(StringHelper.toString(this.mUserNames));
        }

        if (this.mViewStub != null) {
            initViewStub();
        }

        if (this.mUserNames.isEmpty()) {
            this.mButtonGroup.setVisibility(View.GONE);
        } else {
            this.mButtonGroup.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppContext.getImageLoader().clearQueue();
    }

    private void resetChoices() {
        final SparseBooleanArray sba = this.mListView.getCheckedItemPositions();
        for (int i = 0; i < sba.size(); i++) {
            this.mCursorAdapter.setItemChecked(sba.keyAt(i), false);
        }
        this.mListView.clearChoices();
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
        this.mActionBar.setTitle("我关注的人");
        this.mActionBar.setRightAction(new ConfirmAction());
        this.mActionBar.setLeftAction(new ActionBar.BackAction(this));
    }

    private void setLayout() {
        setContentView(R.layout.user_choose);

        setActionBar();

        this.mViewStub = (ViewStub) findViewById(R.id.stub);

        this.mEmptyView = (ViewGroup) findViewById(R.id.empty);

        this.mEditText = (EditText) findViewById(R.id.choose_input);
        this.mEditText.addTextChangedListener(new MyTextWatcher());

        setListView();
    }

    private void setListView() {
        this.mListView = (ListView) findViewById(R.id.list);
        this.mListView.setCacheColorHint(0);
        this.mListView.setHorizontalScrollBarEnabled(false);
        this.mListView.setVerticalScrollBarEnabled(false);
        this.mListView.setSelector(getResources().getDrawable(
                R.drawable.list_selector));
        this.mListView.setDivider(getResources().getDrawable(
                R.drawable.separator));

        this.mListView.setOnItemClickListener(this);
        this.mListView.setItemsCanFocus(false);
        this.mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        this.mListView.setAdapter(this.mCursorAdapter);
    }

    private void showContent() {
        if (AppContext.DEBUG) {
            log("showContent()");
        }
        this.isInitialized = true;
        this.mEmptyView.setVisibility(View.GONE);
        this.mEditText.setVisibility(View.VISIBLE);
        this.mListView.setVisibility(View.VISIBLE);
    }

    private void showProgress() {
        this.mListView.setVisibility(View.GONE);
        this.mEditText.setVisibility(View.GONE);
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
