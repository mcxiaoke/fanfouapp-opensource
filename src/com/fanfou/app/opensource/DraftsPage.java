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

import java.io.File;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.fanfou.app.opensource.adapter.DraftsCursorAdaper;
import com.fanfou.app.opensource.api.bean.Draft;
import com.fanfou.app.opensource.db.Contents.DraftInfo;
import com.fanfou.app.opensource.dialog.ConfirmDialog;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.service.QueueService;
import com.fanfou.app.opensource.ui.ActionBar;
import com.fanfou.app.opensource.ui.ActionBar.AbstractAction;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.27
 * @version 1.1 2011.10.28
 * @version 1.2 2011.11.11
 * 
 */
public class DraftsPage extends BaseActivity implements OnItemClickListener {
    private class SendAllAction extends AbstractAction {

        public SendAllAction() {
            super(R.drawable.ic_sendall);
        }

        @Override
        public void performAction(final View view) {
            doSendAll();
        }

    }

    private ActionBar mBar;

    private ListView mListView;
    private Cursor mCursor;

    private DraftsCursorAdaper mAdapter;

    private void doSendAll() {
        final ConfirmDialog dialog = new ConfirmDialog(this, "发送所有",
                "确定发送所有草稿吗？");
        dialog.setClickListener(new ConfirmDialog.AbstractClickHandler() {

            @Override
            public void onButton1Click() {
                startTaskQueueService();
                onMenuHomeClick();
            }
        });
        dialog.show();
    }

    @Override
    protected int getPageType() {
        return BaseActivity.PAGE_DRAFTS;
    }

    private void goWritePage(final Draft draft) {
        if (draft == null) {
            return;
        }

        final Intent intent = new Intent(this, WritePage.class);
        intent.putExtra(Constants.EXTRA_TYPE, draft.type);
        intent.putExtra(Constants.EXTRA_TEXT, draft.text);
        intent.putExtra(Constants.EXTRA_ID, draft.id);
        intent.putExtra(Constants.EXTRA_IN_REPLY_TO_ID, draft.replyTo);
        if (!StringHelper.isEmpty(draft.filePath)) {
            intent.putExtra(Constants.EXTRA_DATA, new File(draft.filePath));
        }
        startActivity(intent);
        finish();

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuItem clear = menu.add(0, BaseActivity.MENU_ID_CLEAR,
                BaseActivity.MENU_ID_CLEAR, "清空草稿");
        clear.setIcon(R.drawable.ic_menu_clear);
        return true;
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
            final int position, final long id) {
        final Cursor c = (Cursor) parent.getItemAtPosition(position);
        if (c != null) {
            final Draft draft = Draft.parse(c);
            goWritePage(draft);
        }
    }

    private void onMenuClearClick() {
        getContentResolver().delete(DraftInfo.CONTENT_URI, null, null);
        this.mCursor.requery();
        CommonHelper.notify(this, "草稿箱已清空");
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
        case MENU_ID_CLEAR:
            onMenuClearClick();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }

    }

    private void setActionBar() {
        this.mBar = (ActionBar) findViewById(R.id.actionbar);
        this.mBar.setTitle("草稿箱");
        this.mBar.setRightAction(new SendAllAction());
    }

    private void setLayout() {
        setContentView(R.layout.list_drafts);
        setActionBar();
        setListView();

    }

    private void setListView() {
        this.mCursor = managedQuery(DraftInfo.CONTENT_URI, DraftInfo.COLUMNS,
                null, null, null);
        if (this.mCursor.getCount() == 0) {
            this.mBar.setRightActionEnabled(false);
        }

        this.mAdapter = new DraftsCursorAdaper(this, this.mCursor);
        this.mListView = (ListView) findViewById(R.id.list);
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setOnItemClickListener(this);
    }

    private void startTaskQueueService() {
        QueueService.start(this);
    }

}
