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
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.Selection;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;

import com.fanfou.app.opensource.adapter.AutoCompleteCursorAdapter;
import com.fanfou.app.opensource.adapter.MessageCursorAdapter;
import com.fanfou.app.opensource.adapter.SpaceTokenizer;
import com.fanfou.app.opensource.api.bean.DirectMessage;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.db.Contents.BasicColumns;
import com.fanfou.app.opensource.db.Contents.DirectMessageInfo;
import com.fanfou.app.opensource.db.Contents.UserInfo;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.service.FanfouServiceManager;
import com.fanfou.app.opensource.service.PostMessageService;
import com.fanfou.app.opensource.ui.ActionBar;
import com.fanfou.app.opensource.ui.ActionBar.AbstractAction;
import com.fanfou.app.opensource.ui.TextChangeListener;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.IOHelper;
import com.fanfou.app.opensource.util.IntentHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.08
 * @version 1.1 2011.10.25
 * @version 1.2 2011.10.26
 * @version 1.3 2011.11.07
 * @version 1.4 2011.11.18
 * 
 */
public class SendPage extends BaseActivity {

    private class SendAction extends AbstractAction {

        public SendAction() {
            super(R.drawable.ic_send);
        }

        @Override
        public void performAction(final View view) {
            doSend(true);
        }

    }

    private static final String TAG = SendPage.class.getSimpleName();
    private String mUserId;
    private String mUserName;

    private Cursor mCursor;
    private ListView mListView;

    private MessageCursorAdapter mCursorAdapter;
    private ViewStub mViewStub;
    private ViewGroup mSelectView;
    private ImageView mSelectButton;

    private MultiAutoCompleteTextView mSelectAutoComplete;

    private ActionBar mActionBar;

    private EditText mEditText;

    private Button mSendButton;

    private String mContent;

    private static final int REQUEST_CODE_SELECT_USER = 2001;

    private void checkUserId() {
        if (AppContext.DEBUG) {
            Log.d(SendPage.TAG, "checkUserId userId=" + this.mUserId);
        }
        if (StringHelper.isEmpty(this.mUserId)) {
            this.mSelectView = (ViewGroup) this.mViewStub.inflate();
            this.mSelectButton = (ImageView) findViewById(R.id.send_select_button);
            this.mSelectButton.setOnClickListener(this);
            setAutoComplete();
        } else {
            setListView();
            updateUI();
        }
    }

    private void doCopy(final DirectMessage dm) {
        IOHelper.copyToClipBoard(this, dm.senderScreenName + "：" + dm.text);
        CommonHelper.notify(this, "私信内容已复制到剪贴板");
    }

    private void doDelete(final DirectMessage dm) {
        FanfouServiceManager.doMessageDelete(this, dm.id, null, false);
    }

    private void doSend(final boolean finish) {
        if (StringHelper.isEmpty(this.mContent)) {
            CommonHelper.notify(this, "私信内容不能为空");
            return;
        }
        if (StringHelper.isEmpty(this.mUserId)) {
            CommonHelper.notify(this, "请选择收件人");
            return;
        }

        startSendService();
        if (finish) {
            CommonHelper.hideKeyboard(this, this.mEditText);
            finish();
        } else {
            this.mEditText.setText("");
        }
    }

    @Override
    protected IntentFilter getIntentFilter() {
        final IntentFilter filter = new IntentFilter(
                Constants.ACTION_MESSAGE_SENT);
        filter.setPriority(1000);
        return filter;
    }

    private void initCursor() {
        final String where = DirectMessageInfo.THREAD_USER_ID + "=?";
        final String[] whereArgs = new String[] { this.mUserId };
        final String orderBy = BasicColumns.CREATED_AT;
        this.mCursor = managedQuery(DirectMessageInfo.CONTENT_URI,
                DirectMessageInfo.COLUMNS, where, whereArgs, orderBy);
    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SendPage.REQUEST_CODE_SELECT_USER) {
                this.mUserId = data.getStringExtra(Constants.EXTRA_ID);
                this.mUserName = data.getStringExtra(Constants.EXTRA_USER_NAME);
                this.mSelectAutoComplete.setText(this.mUserName);
                Selection.setSelection(
                        this.mSelectAutoComplete.getEditableText(),
                        this.mSelectAutoComplete.getEditableText().length());
            }
        }
    }

    @Override
    protected boolean onBroadcastReceived(final Intent intent) {
        // Utils.notify(this, "私信发送成功！");
        // mListView.setSelection(mCursorAdapter.getCount());
        return true;
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        switch (id) {
        case R.id.button_ok:
            doSend(false);
            break;
        case R.id.send_select_button:
            startSelectUser();
            break;
        default:
            break;
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
                .getMenuInfo();
        final int id = item.getItemId();
        final Cursor c = (Cursor) this.mCursorAdapter
                .getItem(menuInfo.position);
        if (c != null) {
            final DirectMessage dm = DirectMessage.parse(c);
            if ((dm != null) && !dm.isNull()) {
                switch (id) {
                case R.id.dm_copy:
                    doCopy(dm);
                    break;
                case R.id.dm_delete:
                    doDelete(dm);
                    break;
                default:
                    break;
                }
            }
        }

        return true;
        // return super.onContextItemSelected(item);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        setLayout();
        checkUserId();
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.dm_list_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void parseIntent() {
        final Intent intent = getIntent();
        this.mUserId = intent.getStringExtra(Constants.EXTRA_ID);
        this.mUserName = intent.getStringExtra(Constants.EXTRA_USER_NAME);
        if (AppContext.DEBUG) {
            IntentHelper.logIntent(SendPage.TAG, intent);
        }
    }

    private void setActionBar() {
        this.mActionBar = (ActionBar) findViewById(R.id.actionbar);
        this.mActionBar.setTitle("写私信");
        this.mActionBar.setRightAction(new SendAction());
        this.mActionBar.setLeftAction(new ActionBar.BackAction(this));
    }

    private void setAutoComplete() {
        this.mSelectAutoComplete = (MultiAutoCompleteTextView) findViewById(R.id.send_select_edit);
        this.mSelectAutoComplete.setTokenizer(new SpaceTokenizer());
        this.mSelectAutoComplete.setBackgroundColor(getResources().getColor(
                R.color.background_color));
        final String[] projection = new String[] { BaseColumns._ID,
                BasicColumns.ID, UserInfo.SCREEN_NAME, BasicColumns.TYPE,
                BasicColumns.OWNER_ID };
        final String where = BasicColumns.TYPE + " = '"
                + Constants.TYPE_USERS_FRIENDS + "'";
        final Cursor c = managedQuery(UserInfo.CONTENT_URI, projection, where,
                null, null);
        this.mSelectAutoComplete.setAdapter(new AutoCompleteCursorAdapter(this,
                c));
        this.mSelectAutoComplete
                .addTextChangedListener(new TextChangeListener() {

                    @Override
                    public void onTextChanged(final CharSequence s,
                            final int start, final int before, final int count) {
                        SendPage.this.mUserId = null;
                        SendPage.this.mUserName = null;
                    }
                });

        this.mSelectAutoComplete
                .setOnItemClickListener(new ListView.OnItemClickListener() {

                    @Override
                    public void onItemClick(final AdapterView<?> parent,
                            final View view, final int position, final long id) {
                        if (AppContext.DEBUG) {
                            Log.d(SendPage.TAG, "onItemClick position="
                                    + position);
                        }
                        final Cursor c = (Cursor) parent
                                .getItemAtPosition(position);
                        if (c != null) {
                            final User user = User.parse(c);
                            if ((user != null) && !user.isNull()) {
                                SendPage.this.mUserId = user.id;
                                SendPage.this.mUserName = user.screenName;
                                if (AppContext.DEBUG) {
                                    Log.d(SendPage.TAG, "onItemClick user.id="
                                            + user.id);
                                }
                            }
                        }
                    }
                });

    }

    private void setLayout() {
        setContentView(R.layout.send);

        setActionBar();
        this.mEditText = (EditText) findViewById(R.id.msgchat_input);
        this.mEditText.addTextChangedListener(new TextChangeListener() {

            @Override
            public void onTextChanged(final CharSequence s, final int start,
                    final int before, final int count) {
                SendPage.this.mContent = s.toString().trim();
            }
        });

        this.mSendButton = (Button) findViewById(R.id.button_ok);
        this.mSendButton.setOnClickListener(this);

        this.mListView = (ListView) findViewById(R.id.list);
        this.mViewStub = (ViewStub) findViewById(R.id.stub);

    }

    private void setListView() {
        initCursor();
        registerForContextMenu(this.mListView);
        this.mCursorAdapter = new MessageCursorAdapter(this, this.mCursor,
                true, true);
        this.mListView.setCacheColorHint(0);
        this.mListView.setHorizontalScrollBarEnabled(false);
        this.mListView.setVerticalScrollBarEnabled(false);
        this.mListView.setSelector(getResources().getDrawable(
                R.drawable.list_selector));
        this.mListView.setDivider(getResources().getDrawable(
                R.drawable.separator));
        this.mListView
                .setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        this.mListView.setAdapter(this.mCursorAdapter);
        this.mListView.setSelection(this.mListView.getCount() - 1);
    }

    private void startSelectUser() {
        final Intent intent = new Intent(this, UserSelectPage.class);
        startActivityForResult(intent, SendPage.REQUEST_CODE_SELECT_USER);
    }

    private void startSendService() {
        final Intent i = new Intent(this.mContext, PostMessageService.class);
        i.putExtra(Constants.EXTRA_ID, this.mUserId);
        i.putExtra(Constants.EXTRA_USER_NAME, this.mUserName);
        i.putExtra(Constants.EXTRA_TEXT, this.mContent);
        startService(i);
    }

    private void updateUI() {
        if (this.mCursor != null) {
            this.mCursor.requery();
        }
    }

}
