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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.cache.CacheManager;
import com.fanfou.app.opensource.cache.ImageLoader;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.service.FanfouServiceManager;
import com.fanfou.app.opensource.ui.ActionBar;
import com.fanfou.app.opensource.ui.ActionManager;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.DateTimeHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.07.18
 * @version 1.2 2011.10.29
 * @version 1.3 2011.11.07
 * @version 1.4 2011.11.08
 * @version 1.5 2011.12.06
 * @version 1.6 2011.12.19
 * 
 */
public class MyProfilePage extends BaseActivity {

    private class EditProfileAction extends ActionBar.AbstractAction {
        public EditProfileAction() {
            super(R.drawable.ic_sethead);
        }

        @Override
        public void performAction(final View view) {
            if (MyProfilePage.this.user != null) {
                MyProfilePage.goEditProfilePage(MyProfilePage.this.mContext,
                        MyProfilePage.this.user);
            }
        }

    }

    private class ResultHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {
            final int type = msg.arg1;
            switch (msg.what) {
            case Constants.RESULT_SUCCESS:
                if (msg.getData() != null) {
                    if (AppContext.DEBUG) {
                        log("result ok, update ui");
                    }
                    final User result = (User) msg.getData().getParcelable(
                            Constants.EXTRA_DATA);
                    if (result != null) {
                        AppContext.getAppContext().updateUserInfo(result);
                        MyProfilePage.this.user = result;
                    }
                    if (!MyProfilePage.this.isInitialized) {
                        showContent();
                    }
                    if (type == Constants.TYPE_USERS_SHOW) {
                        log("show result=" + MyProfilePage.this.user.id);
                        updateUI();
                        if (MyProfilePage.this.isInitialized) {
                        }
                    }
                }
                break;
            case Constants.RESULT_ERROR:
                if (type == Constants.TYPE_USERS_SHOW) {
                }
                if (!MyProfilePage.this.isInitialized) {
                    showContent();
                }
                final String errorMessage = msg.getData().getString(
                        Constants.EXTRA_ERROR);
                CommonHelper.notify(MyProfilePage.this.mContext, errorMessage);
                if (AppContext.DEBUG) {
                    log("result error");
                }
                break;
            default:
                break;
            }
        }

    }

    private ScrollView mScrollView;

    private View mEmptyView;
    private ActionBar mActionBar;

    private ImageView mHead;

    private TextView mName;

    private ImageView mProtected;

    private TextView mDescription;
    private ViewGroup mStatusesView;
    private TextView mStatusesInfo;
    private ViewGroup mFavoritesView;
    private TextView mFavoritesInfo;
    private ViewGroup mFriendsView;
    private TextView mFriendsInfo;
    private ViewGroup mFollowersView;

    private TextView mFollowersInfo;

    private TextView mExtraInfo;

    private String userId;
    private User user;

    private Handler mHandler;

    private ImageLoader mLoader;

    private boolean isInitialized = false;

    private static final int REQUEST_CODE_UPDATE_PROFILE = 0;

    private static final String tag = MyProfilePage.class.getSimpleName();

    private static void goEditProfilePage(final Activity context,
            final User user) {
        final Intent intent = new Intent(context, EditProfilePage.class);
        intent.putExtra(Constants.EXTRA_DATA, user);
        context.startActivityForResult(intent,
                MyProfilePage.REQUEST_CODE_UPDATE_PROFILE);
    }

    private void doRefresh() {
        FanfouServiceManager.doProfile(this, this.userId, new ResultHandler());
        if (this.isInitialized) {
        }
    }

    protected void initCheckState() {
        if (this.user != null) {
            showContent();
            updateUI();
        } else {
            doRefresh();
            showProgress();
        }
    }

    private void initialize() {
        this.mHandler = new Handler();
        this.mLoader = AppContext.getImageLoader();
    }

    private void log(final String message) {
        Log.d(MyProfilePage.tag, message);
    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MyProfilePage.REQUEST_CODE_UPDATE_PROFILE) {
                final User result = (User) data
                        .getParcelableExtra(Constants.EXTRA_DATA);
                if (result != null) {
                    this.user = result;
                    this.userId = this.user.id;
                    updateUI();
                }
            }
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
        case R.id.user_statuses_view:
            ActionManager.doShowTimeline(this, this.user);
            break;
        case R.id.user_favorites_view:
            ActionManager.doShowFavorites(this, this.user);
            break;
        case R.id.user_friends_view:
            ActionManager.doShowFriends(this, this.user);
            break;
        case R.id.user_followers_view:
            ActionManager.doShowFollowers(this, this.user);
            break;
        case R.id.user_headview:
            // goEditProfilePage(this,user);
            break;
        default:
            break;
        }

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        initialize();
        setLayout();
        initCheckState();
    }

    private void parseIntent() {
        this.userId = AppContext.getUserId();
        this.user = CacheManager.getUser(this, this.userId);
    }

    /**
     * 初始化和设置ActionBar
     */
    private void setActionBar() {
        this.mActionBar = (ActionBar) findViewById(R.id.actionbar);
        this.mActionBar.setTitle("我的空间");
        this.mActionBar.setRightAction(new EditProfileAction());
        this.mActionBar.setLeftAction(new ActionBar.BackAction(this));
    }

    private void setExtraInfo(final User u) {
        if (u == null) {
            this.mExtraInfo.setVisibility(View.GONE);
            return;
        }

        final StringBuffer sb = new StringBuffer();

        if (!StringHelper.isEmpty(this.user.gender)) {
            sb.append("性别：").append(this.user.gender).append("\n");
        }
        if (!StringHelper.isEmpty(this.user.birthday)) {
            sb.append("生日：").append(this.user.birthday).append("\n");
        }
        if (!StringHelper.isEmpty(this.user.location)) {
            sb.append("位置：").append(this.user.location).append("\n");
        }

        if (!StringHelper.isEmpty(this.user.url)) {
            sb.append("网站：").append(this.user.url).append("\n");
        }

        sb.append("注册时间：").append(
                DateTimeHelper.formatDateOnly(this.user.createdAt));

        this.mExtraInfo.setText(sb.toString());

    }

    private void setLayout() {
        setContentView(R.layout.myprofile);

        // View root=findViewById(R.id.root);
        // ThemeHelper.setBackgroundColor(root);

        setActionBar();

        this.mEmptyView = findViewById(R.id.empty);
        this.mScrollView = (ScrollView) findViewById(R.id.user_profile);

        this.mHead = (ImageView) findViewById(R.id.user_head);
        this.mName = (TextView) findViewById(R.id.user_name);
        final TextPaint tp = this.mName.getPaint();
        tp.setFakeBoldText(true);
        this.mExtraInfo = (TextView) findViewById(R.id.user_extrainfo);

        this.mProtected = (ImageView) findViewById(R.id.user_protected);

        this.mDescription = (TextView) findViewById(R.id.user_description);

        // mHeadView = (ViewGroup) findViewById(R.id.user_headview);

        this.mStatusesView = (ViewGroup) findViewById(R.id.user_statuses_view);
        this.mStatusesInfo = (TextView) findViewById(R.id.user_statuses);
        this.mFavoritesView = (ViewGroup) findViewById(R.id.user_favorites_view);
        this.mFavoritesInfo = (TextView) findViewById(R.id.user_favorites);
        this.mFriendsView = (ViewGroup) findViewById(R.id.user_friends_view);
        this.mFriendsInfo = (TextView) findViewById(R.id.user_friends);
        this.mFollowersView = (ViewGroup) findViewById(R.id.user_followers_view);
        this.mFollowersInfo = (TextView) findViewById(R.id.user_followers);

        // mHeadView.setOnClickListener(this);
        this.mStatusesView.setOnClickListener(this);
        this.mFavoritesView.setOnClickListener(this);
        this.mFriendsView.setOnClickListener(this);
        this.mFollowersView.setOnClickListener(this);
    }

    private void showContent() {
        if (AppContext.DEBUG) {
            log("showContent()");
        }
        this.isInitialized = true;
        this.mEmptyView.setVisibility(View.GONE);
        this.mScrollView.setVisibility(View.VISIBLE);
    }

    private void showProgress() {
        this.mScrollView.setVisibility(View.GONE);
        this.mEmptyView.setVisibility(View.VISIBLE);
    }

    private void updateUI() {
        if (this.user == null) {
            return;
        }

        if (AppContext.DEBUG) {
            log("updateUI user.name=" + this.user.screenName);
        }

        this.mHead.setTag(this.user.profileImageUrl);
        this.mLoader.displayImage(this.user.profileImageUrl, this.mHead,
                R.drawable.default_head);
        this.mName.setText(this.user.screenName);

        this.mStatusesInfo.setText("" + this.user.statusesCount);
        this.mFavoritesInfo.setText("" + this.user.favouritesCount);
        this.mFriendsInfo.setText("" + this.user.friendsCount);
        this.mFollowersInfo.setText("" + this.user.followersCount);

        if (AppContext.DEBUG) {
            log("updateUI user.description=" + this.user.description);
        }

        if (StringHelper.isEmpty(this.user.description)) {
            this.mDescription.setText("这家伙什么也没留下");
            this.mDescription.setGravity(Gravity.CENTER);
        } else {
            this.mDescription.setText(this.user.description);
        }

        setExtraInfo(this.user);

        if (this.user.protect) {
            this.mProtected.setVisibility(View.VISIBLE);
        } else {
            this.mProtected.setVisibility(View.GONE);
        }

    }

}
