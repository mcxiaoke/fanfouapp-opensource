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
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.fanfou.app.opensource.adapter.BaseCursorAdapter;
import com.fanfou.app.opensource.adapter.MessageCursorAdapter;
import com.fanfou.app.opensource.adapter.StatusCursorAdapter;
import com.fanfou.app.opensource.adapter.ViewsAdapter;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.db.Contents.BasicColumns;
import com.fanfou.app.opensource.db.Contents.DirectMessageInfo;
import com.fanfou.app.opensource.db.Contents.StatusInfo;
import com.fanfou.app.opensource.db.FanFouProvider;
import com.fanfou.app.opensource.dialog.ConfirmDialog;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.service.DownloadService;
import com.fanfou.app.opensource.service.FanfouServiceManager;
import com.fanfou.app.opensource.service.NotificationService;
import com.fanfou.app.opensource.ui.ActionBar;
import com.fanfou.app.opensource.ui.ActionManager;
import com.fanfou.app.opensource.ui.UIManager;
import com.fanfou.app.opensource.ui.viewpager.TitlePageIndicator;
import com.fanfou.app.opensource.ui.viewpager.TitleProvider;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.IntentHelper;
import com.fanfou.app.opensource.util.OptionHelper;
import com.fanfou.app.opensource.util.SoundManager;
import com.fanfou.app.opensource.util.ToastHelper;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.31
 * 
 */
public class HomePage extends BaseActivity implements OnPageChangeListener,
        OnItemLongClickListener, TitleProvider, OnRefreshListener2<ListView>,
        OnLastItemVisibleListener, OnItemClickListener {

    /**
     * FetchService返回数据处理 根据resultData里面的type信息分别处理
     */
    @SuppressLint("HandlerLeak")
    @SuppressWarnings("deprecation")
    private class ResultHandler extends Handler {
        private final int requestPage;
        private final boolean doGetMore;

        public ResultHandler(final int page, final boolean getMore) {
            this.requestPage = page;
            this.doGetMore = getMore;
        }

        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            final int type = msg.arg1;
            switch (msg.what) {
            case Constants.RESULT_SUCCESS:
                final int count = bundle.getInt(Constants.EXTRA_COUNT);
                HomePage.this.views[this.requestPage].onRefreshComplete();
                if (this.doGetMore) {
                    HomePage.this.cursors[this.requestPage].requery();
                } else {
                    if (count > 0) {
                        String toastSuffix = null;
                        if (type == Constants.TYPE_STATUSES_HOME_TIMELINE) {
                            toastSuffix = "条好友消息";
                        } else if (type == Constants.TYPE_STATUSES_MENTIONS) {
                            toastSuffix = "条@你的消息";
                        } else if (type == Constants.TYPE_DIRECT_MESSAGES_CONVERSTATION_LIST) {
                            toastSuffix = "条私信";
                        } else if (type == Constants.TYPE_STATUSES_PUBLIC_TIMELINE) {
                            toastSuffix = "条随便看看消息";
                        }
                        if (AppContext.DEBUG) {
                            Log.i(HomePage.TAG, "ResultHandler page:"
                                    + this.requestPage + " message: " + count
                                    + toastSuffix);
                        }
                        showToast(count + toastSuffix);
                        if (HomePage.this.soundEffect) {
                            SoundManager.playSound(1, 0);
                        }
                        HomePage.this.cursors[this.requestPage].requery();
                        // listViews[requestPage].setSelection(0);
                    }
                }
                break;
            case Constants.RESULT_ERROR:

                HomePage.this.views[this.requestPage].onRefreshComplete();
                final String errorMessage = bundle
                        .getString(Constants.EXTRA_ERROR);
                final int errorCode = bundle.getInt(Constants.EXTRA_CODE);
                if (AppContext.DEBUG) {
                    Log.i(HomePage.TAG, "ResultHandler page:"
                            + this.requestPage + "errorCode:" + errorCode
                            + " errorMessage: " + errorMessage);
                }
                CommonHelper.checkErrorCode(HomePage.this.mContext, errorCode,
                        errorMessage);
                break;
            default:
                break;
            }
        }

    }

    public static final int NUMS_OF_PAGE = 4;
    private ActionBar mActionBar;
    private ViewPager mViewPager;
    private Handler mUiHandler;

    private ViewsAdapter mViewAdapter;

    private TitlePageIndicator mPageIndicator;
    private int mCurrentPage;
    private int initPage;

    private final PullToRefreshListView[] views = new PullToRefreshListView[HomePage.NUMS_OF_PAGE];

    private final ListView[] listViews = new ListView[HomePage.NUMS_OF_PAGE];

    private final Cursor[] cursors = new Cursor[HomePage.NUMS_OF_PAGE];

    private final BaseCursorAdapter[] adapters = new BaseCursorAdapter[HomePage.NUMS_OF_PAGE];

    private final Parcelable[] states = new Parcelable[HomePage.NUMS_OF_PAGE];

    private static final String[] PAGE_TITLES = new String[] { "我的主页", "提到我的",
            "我的私信", "随便看看" };

    private boolean endlessScroll;

    private boolean soundEffect;

    public static final String TAG = "HomePage";

    private long lastPressTime = 0;

    private void checkRefresh() {
        final boolean refresh = OptionHelper.readBoolean(this,
                R.string.option_refresh_on_open, false);
        if ((this.mCurrentPage == 0)
                && (refresh || (this.cursors[0].getCount() == 0))) {
            this.mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (HomePage.this.views[HomePage.this.mCurrentPage] != null) {
                        HomePage.this.views[HomePage.this.mCurrentPage]
                                .setRefreshing();
                    }
                }
            }, 300);
        }
    }

    private void checkUpdate() {
        final boolean autoUpdate = OptionHelper.readBoolean(this,
                R.string.option_autoupdate, true);

        if (autoUpdate) {
            final long now = System.currentTimeMillis();
            final long last = OptionHelper.readLong(this,
                    R.string.option_auto_update_last_time, 0L);
            if (AppContext.DEBUG) {
                log("checkUpdate() autoUpdate=" + autoUpdate
                        + " lastUpdateTime=" + last);
            }
            if ((now - last) > DownloadService.AUTO_UPDATE_INTERVAL) {
                OptionHelper.saveLong(this,
                        R.string.option_auto_update_last_time, now);
                final Intent intent = new Intent(this, DownloadService.class);
                intent.putExtra(Constants.EXTRA_TYPE,
                        DownloadService.TYPE_CHECK);
                startService(intent);
            }
        }
    }

    @SuppressWarnings("unused")
    private boolean doBackPress() {
        if ((System.currentTimeMillis() - this.lastPressTime) < 2000) {
            finish();
        } else {
            CommonHelper.notify(this, "再按一次退出");
            this.lastPressTime = System.currentTimeMillis();
        }
        return true;
    }

    /**
     * 载入更多，获取较旧的消息
     */
    private void doGetMore() {
        if (AppContext.DEBUG) {
            log("doRefresh()");
        }
        doRetrieve(this.mCurrentPage, true);
    }

    /**
     * 刷新，载入更新的消息
     */
    private void doRefresh() {
        if (AppContext.DEBUG) {
            log("doRefresh()");
        }
        doRetrieve(this.mCurrentPage, false);
    }

    /**
     * 刷新，获取最新的消息
     * 
     * @param page
     *            类型参数：Home/Mention/Message/Public
     */
    private void doRetrieve(final int page, final boolean doGetMore) {
        if (AppContext.DEBUG) {
            log("doRetrieve() page=" + page + " doGetMore=" + doGetMore);
        }
        final ResultHandler handler = new ResultHandler(page, doGetMore);
        String sinceId = null;
        String maxId = null;
        final Cursor cursor = this.cursors[page];
        switch (page) {
        case 0:
            if (doGetMore) {
                maxId = CommonHelper.getMaxId(cursor);
            } else {
                sinceId = CommonHelper.getSinceId(cursor);
            }
            FanfouServiceManager.doFetchHomeTimeline(this, new Messenger(
                    handler), sinceId, maxId);
            break;
        case 1:
            if (doGetMore) {
                maxId = CommonHelper.getMaxId(cursor);
            } else {
                sinceId = CommonHelper.getSinceId(cursor);
            }
            FanfouServiceManager.doFetchMentions(this, new Messenger(handler),
                    sinceId, maxId);
            break;
        case 2:
            FanfouServiceManager.doFetchDirectMessagesConversationList(this,
                    new Messenger(handler), doGetMore);
            break;
        case 3:
            if (!doGetMore) {
                FanfouServiceManager.doFetchPublicTimeline(this, new Messenger(
                        handler));
            }
            break;
        default:
            break;
        }
    }

    @Override
    protected IntentFilter getIntentFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_STATUS_SENT);
        filter.addAction(Constants.ACTION_DRAFTS_SENT);
        filter.addAction(Constants.ACTION_NOTIFICATION);
        return filter;
    }

    @Override
    protected int getPageType() {
        return BaseActivity.PAGE_HOME;
    }

    @Override
    public String getTitle(final int position) {
        return HomePage.PAGE_TITLES[position % HomePage.NUMS_OF_PAGE];
    }

    private void goTop() {
        if (this.listViews[this.mCurrentPage] != null) {
            this.listViews[this.mCurrentPage].setSelection(0);
        }
    }

    private void initAdapters() {
        this.adapters[0] = new StatusCursorAdapter(true, this, this.cursors[0]);
        this.adapters[1] = new StatusCursorAdapter(true, this, this.cursors[1]);
        this.adapters[2] = new MessageCursorAdapter(this, this.cursors[2]);
        this.adapters[3] = new StatusCursorAdapter(true, this, this.cursors[3]);
    }

    private void initialize() {
        this.initPage = getIntent().getIntExtra(Constants.EXTRA_PAGE, 0);
        this.endlessScroll = OptionHelper.readBoolean(this,
                R.string.option_page_scroll_endless, false);
        this.soundEffect = OptionHelper.readBoolean(this,
                R.string.option_play_sound_effect, true);
        this.mUiHandler = new Handler();
        initSoundManager();
    }

    @SuppressWarnings("deprecation")
    private Cursor initMessageCursor() {
        final Uri uri = Uri.withAppendedPath(DirectMessageInfo.CONTENT_URI,
                "list");
        return managedQuery(uri, DirectMessageInfo.COLUMNS, null, null, null);
    }

    private void initSoundManager() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        SoundManager.getInstance();
        SoundManager.initSounds(this);
        SoundManager.loadSounds();
    }

    @SuppressWarnings("deprecation")
    private Cursor initStatusCursor(final int type) {
        final String where = BasicColumns.TYPE + "=?";
        final String[] whereArgs = new String[] { String.valueOf(type) };
        final Uri uri = StatusInfo.CONTENT_URI;
        return managedQuery(uri, StatusInfo.COLUMNS, where, whereArgs,
                FanFouProvider.ORDERBY_DATE_DESC);
    }

    @Override
    protected boolean isHomeScreen() {
        return true;
    }

    private void log(final String message) {
        Log.d(HomePage.TAG, message);
    }

    @Override
    public void onBackPressed() {
        final boolean needConfirm = OptionHelper.readBoolean(this,
                R.string.option_confirm_on_exit, false);
        if (needConfirm) {
            final ConfirmDialog dialog = new ConfirmDialog(this, "提示",
                    "确认退出饭否吗？");
            dialog.setClickListener(new ConfirmDialog.AbstractClickHandler() {

                @Override
                public void onButton1Click() {
                    HomePage.this.mContext.finish();
                }
            });
            dialog.show();
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean onBroadcastReceived(final Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Constants.ACTION_STATUS_SENT)) {
            if (AppContext.DEBUG) {
                log("onBroadcastReceived ACTION_STATUS_SENT");
            }

            if (this.mCurrentPage == 0) {
                final boolean needRefresh = OptionHelper.readBoolean(this,
                        R.string.option_refresh_after_send, false);
                if (needRefresh) {
                    startRefresh();
                }
            }

        } else if (action.equals(Constants.ACTION_DRAFTS_SENT)) {
            if (AppContext.DEBUG) {
                log("onBroadcastReceived ACTION_DRAFTS_SENT");
            }

            if (this.mCurrentPage == 0) {
                final boolean needRefresh = OptionHelper.readBoolean(this,
                        R.string.option_refresh_after_send, false);
                if (needRefresh) {
                    startRefresh();
                }
            }
        } else if (action.equals(Constants.ACTION_NOTIFICATION)) {
            if (AppContext.DEBUG) {
                log("onBroadcastReceived ACTION_NOTIFICATION");
            }
            final int type = intent.getIntExtra(Constants.EXTRA_TYPE, -1);
            final int count = intent.getIntExtra(Constants.EXTRA_COUNT, 0);
            switch (type) {
            case NotificationService.NOTIFICATION_TYPE_HOME:
                if (count > 0) {
                    if (this.cursors[0] != null) {
                        this.cursors[0].requery();
                    }
                    this.listViews[0].setSelection(0);
                    CommonHelper.notify(this, count + "条新消息");
                    if (this.soundEffect) {
                        SoundManager.playSound(1, 0);
                    }
                }
                break;
            case NotificationService.NOTIFICATION_TYPE_MENTION:
                if (count > 0) {
                    if (this.cursors[1] != null) {
                        this.cursors[1].requery();
                    }
                    this.listViews[1].setSelection(0);
                    CommonHelper.notify(this, count + "条新@消息");
                    if (this.soundEffect) {
                        SoundManager.playSound(1, 0);
                    }
                }

                break;
            case NotificationService.NOTIFICATION_TYPE_DM:
                if (count > 0) {
                    if (this.cursors[2] != null) {
                        this.cursors[2].requery();
                    }
                    this.listViews[2].setSelection(0);
                    CommonHelper.notify(this, count + "条新私信");
                    if (this.soundEffect) {
                        SoundManager.playSound(1, 0);
                    }
                }

                break;
            default:
                break;
            }
        }
        return true;
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
            log("onCreate()");
        }

        initialize();
        setContentView(R.layout.home);

        setActionBar();
        setListViews();
        setViewPager();
        setCursors();
        setAdapters();
        checkRefresh();
        checkUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // super.onCreateOptionsMenu(menu);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppContext.getImageLoader().shutdown();
        SoundManager.cleanup();
        if (AppContext.DEBUG) {
            log("onDestroy()");
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
            final int position, final long id) {
        final Cursor c = (Cursor) parent.getItemAtPosition(position);
        if (c == null) {
            return;
        }
        if (this.mCurrentPage == 2) {
            CommonHelper.goMessageChatPage(this, c);
        } else {
            final Status s = Status.parse(c);
            if ((s != null) && !s.isNull()) {
                CommonHelper.goStatusPage(this, s);
            }
        }
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent,
            final View view, final int position, final long id) {
        if (this.mCurrentPage == 2) {
            return true;
        }
        final Cursor c = (Cursor) parent.getItemAtPosition(position);
        showPopup(view, c);
        return true;
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onLastItemVisible() {

    }

    private void onMenuLogoutClick() {
        final ConfirmDialog dialog = new ConfirmDialog(this, "注销",
                "确定注销当前登录帐号吗？");
        dialog.setClickListener(new ConfirmDialog.AbstractClickHandler() {

            @Override
            public void onButton1Click() {
                AppContext.handleLogout(HomePage.this.mContext);
                IntentHelper.goLoginPage(HomePage.this.mContext);
                finish();
            }
        });
        dialog.show();
    }

    private void onMenuOptionClick() {
        final Intent intent = new Intent(this, SettingsPage.class);
        startActivity(intent);
    }

    private void onMenuProfileClick() {
        ActionManager.doMyProfile(this);
    }

    private void onMenuSearchClick() {
        final Intent intent = new Intent(this, SearchPage.class);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        this.initPage = getIntent().getIntExtra(Constants.EXTRA_PAGE, 0);
        if (AppContext.DEBUG) {
            log("onNewIntent page=" + this.initPage);
        }

        if (this.initPage >= 0) {
            this.mViewPager.setCurrentItem(this.initPage);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
        case R.id.menu_option:
            onMenuOptionClick();
            return true;
        case R.id.menu_profile:
            onMenuProfileClick();
            return true;
        case R.id.menu_search:
            onMenuSearchClick();
            return true;
        case R.id.menu_logout:
            onMenuLogoutClick();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset,
            final int positionOffsetPixels) {
        this.mPageIndicator.onPageScrolled(position, positionOffset,
                positionOffsetPixels);
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
    }

    @Override
    public void onPageSelected(final int position) {
        this.mCurrentPage = position % HomePage.NUMS_OF_PAGE;
        this.mPageIndicator.onPageSelected(this.mCurrentPage);
        if (AppContext.DEBUG) {
            log("startRefresh mCurrentPage="+mCurrentPage);
        }
        if ((this.cursors[this.mCurrentPage] != null)
                && (this.cursors[this.mCurrentPage].getCount() == 0)) {
            startRefresh();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (AppContext.DEBUG) {
            log("onPause");
        }
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
        for (int i = 0; i < this.views.length; i++) {
            if (this.views[i] != null) {
                this.states[i] = savedInstanceState.getParcelable(this.views[i]
                        .toString());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppContext.DEBUG) {
            log("onResume");
        }

        for (int i = 0; i < this.views.length; i++) {
            if ((this.views[i] != null) && (this.states[i] != null)) {
                this.listViews[i].onRestoreInstanceState(this.states[i]);
                this.states[i] = null;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        for (int i = 0; i < this.views.length; i++) {
            if (this.views[i] != null) {
                this.states[i] = this.listViews[i].onSaveInstanceState();
                outState.putParcelable(this.views[i].toString(), this.states[i]);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!AppContext.isWifi()) {
            AppContext.getImageLoader().clearQueue();
        }
    }

    /**
     * 初始化和设置ActionBar
     */
    private void setActionBar() {
        this.mActionBar = (ActionBar) findViewById(R.id.actionbar);
        this.mActionBar.setRightAction(new ActionBar.WriteAction(this, null));

        this.mActionBar.setTitle(getString(R.string.app_name));
        if (AppContext.DEBUG) {
            this.mActionBar.setTitle("开发版 " + AppContext.appVersionName
                    + " 用户：" + AppContext.getUserName());
        }
    }

    private void setAdapters() {
        initAdapters();
        for (int i = 0; i < this.adapters.length; i++) {
            this.views[i].setAdapter(this.adapters[i]);
        }
    }

    private void setCursors() {
        this.cursors[0] = initStatusCursor(Constants.TYPE_STATUSES_HOME_TIMELINE);
        this.cursors[1] = initStatusCursor(Constants.TYPE_STATUSES_MENTIONS);
        this.cursors[2] = initMessageCursor();
        this.cursors[3] = initStatusCursor(Constants.TYPE_STATUSES_PUBLIC_TIMELINE);
    }

    /**
     * 初始化并添加四个页面的ListView
     */
    private void setListViews() {
        final LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < this.views.length; i++) {
            this.views[i] = (PullToRefreshListView) inflater.inflate(
                    R.layout.ptr_list, null);
            this.views[i].setOnRefreshListener(this);
            this.listViews[i] = this.views[i].getRefreshableView();
            this.listViews[i].setOnItemClickListener(this);
            if (i != 2) {
                this.listViews[i].setOnItemLongClickListener(this);
            }
            if (i == 3) {
                this.views[i].setMode(Mode.PULL_FROM_START);
            }
        }
    }

    private void setViewPager() {
        if (AppContext.DEBUG) {
            log("setViewPager initPage=" + this.initPage);
        }

        this.mViewAdapter = new ViewsAdapter(this.views, this.endlessScroll);
        this.mViewPager = (ViewPager) findViewById(R.id.viewpager);
        this.mViewPager.setOnPageChangeListener(this);
        this.mViewPager.setAdapter(this.mViewAdapter);
        this.mPageIndicator = (TitlePageIndicator) findViewById(R.id.viewindicator);
        this.mPageIndicator.setTitleProvider(this);
        this.mPageIndicator.setViewPager(this.mViewPager, this.initPage);

        if (this.initPage > 0) {
            this.mViewPager.setCurrentItem(this.initPage);
        }

    }

    private void showPopup(final View view, final Cursor c) {
        if (c != null) {
            final Status s = Status.parse(c);
            if (s == null) {
                return;
            }
            UIManager.showPopup(this.mContext, c, view, s);
        }
    }

    private void showToast(final String text) {
        ToastHelper.showInfoToast(this.mContext, text);
    }

    private void startRefresh() {
        if (AppContext.DEBUG) {
            log("startRefresh mCurrentPage="+mCurrentPage);
        }
        if (this.views[this.mCurrentPage] != null) {
            this.views[this.mCurrentPage].setRefreshing();
        }
    }

}
