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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.cache.CacheManager;
import com.fanfou.app.opensource.cache.ImageLoader;
import com.fanfou.app.opensource.dialog.ConfirmDialog;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.service.FanfouServiceManager;
import com.fanfou.app.opensource.ui.ActionBar;
import com.fanfou.app.opensource.ui.ActionBar.AbstractAction;
import com.fanfou.app.opensource.ui.ActionManager;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.DateTimeHelper;
import com.fanfou.app.opensource.util.OptionHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.07.18
 * @version 1.1 2011.10.25
 * @version 1.2 2011.10.27
 * @version 1.3 2011.10.28
 * @version 1.4 2011.10.29
 * @version 1.5 2011.11.11
 * @version 1.6 2011.11.16
 * @version 1.7 2011.11.18
 * @version 1.8 2011.11.22
 * @version 2.0 2011.12.19
 * 
 */
public class ProfilePage extends BaseActivity {

    private class ResultHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {
            final int type = msg.arg1;
            final Bundle bundle = msg.getData();
            switch (msg.what) {
            case Constants.RESULT_SUCCESS:
                if (!ProfilePage.this.isInitialized) {
                    showContent();
                }
                if (bundle != null) {
                    if (AppContext.DEBUG) {
                        log("result ok, update ui");
                    }
                    final User result = (User) bundle
                            .getParcelable(Constants.EXTRA_DATA);
                    if (result != null) {
                        ProfilePage.this.user = result;
                    }
                    if (type == Constants.TYPE_FRIENDSHIPS_EXISTS) {
                        final boolean follow = bundle
                                .getBoolean(Constants.EXTRA_BOOLEAN);
                        if (AppContext.DEBUG) {
                            log("user relationship result=" + follow);
                        }
                        updateRelationshipState(follow);
                    } else if (type == Constants.TYPE_USERS_SHOW) {
                        if (AppContext.DEBUG) {
                            log("show result=" + ProfilePage.this.user.id);
                        }
                        if (ProfilePage.this.isInitialized) {
                        }
                        updateUI();

                    } else if ((type == Constants.TYPE_FRIENDSHIPS_CREATE)
                            || (type == Constants.TYPE_FRIENDSHIPS_DESTROY)) {
                        if (AppContext.DEBUG) {
                            log("user.following="
                                    + ProfilePage.this.user.following);
                        }
                        updateFollowButton(ProfilePage.this.user.following);
                        CommonHelper.notify(ProfilePage.this.mContext,
                                ProfilePage.this.user.following ? "关注成功"
                                        : "取消关注成功");
                    }
                }
                break;
            case Constants.RESULT_ERROR:
                if (AppContext.DEBUG) {
                    log("result error");
                }
                if (!ProfilePage.this.isInitialized) {
                    ProfilePage.this.mEmptyView.setVisibility(View.GONE);
                }
                if (type == Constants.TYPE_FRIENDSHIPS_EXISTS) {
                    return;
                } else if (type == Constants.TYPE_USERS_SHOW) {
                } else if ((type == Constants.TYPE_FRIENDSHIPS_CREATE)
                        || (type == Constants.TYPE_FRIENDSHIPS_DESTROY)) {
                    updateFollowButton(ProfilePage.this.user.following);
                }

                final String errorMessage = bundle
                        .getString(Constants.EXTRA_ERROR);
                CommonHelper.notify(ProfilePage.this.mContext, errorMessage);
                break;
            default:
                break;
            }
        }

    }

    private class WriteAction extends AbstractAction {

        public WriteAction() {
            super(R.drawable.i_write);
        }

        @Override
        public void performAction(final View view) {
            ActionManager.doWrite(ProfilePage.this.mContext, getUserName());

        }
    }

    private ScrollView mScrollView;

    private View mEmptyView;

    private ActionBar mActionBar;
    private RelativeLayout mHeader;
    private ImageView mHead;

    private TextView mName;
    private ImageView mProtected;

    private TextView mRelationship;
    private LinearLayout mActions;
    private ImageView mReplyAction;
    private ImageView mMessageAction;

    private ImageView mFollowAction;

    private TextView mDescription;
    private ViewGroup mStatusesView;
    private TextView mStatusesTitle;

    private TextView mStatusesInfo;
    private ViewGroup mFavoritesView;
    private TextView mFavoritesTitle;

    private TextView mFavoritesInfo;
    private ViewGroup mFriendsView;
    private TextView mFriendsTitle;

    private TextView mFriendsInfo;
    private ViewGroup mFollowersView;
    private TextView mFollowersTitle;

    private TextView mFollowersInfo;

    private TextView mExtraInfo;

    private String userId;

    private User user;
    private Handler mHandler;

    private ImageLoader mLoader;
    private boolean isInitialized = false;
    private boolean noPermission = false;

    private static final String tag = ProfilePage.class.getSimpleName();

    private void doFetchRelationshipInfo() {
        FanfouServiceManager.doFriendshipsExists(this, this.user.id,
                AppContext.getUserId(), new ResultHandler());
    }

    private void doFollow() {
        if ((this.user == null) || this.user.isNull()) {
            return;
        }

        if (this.user.following) {
            final ConfirmDialog dialog = new ConfirmDialog(this, "取消关注",
                    "要取消关注" + this.user.screenName + "吗？");
            dialog.setClickListener(new ConfirmDialog.AbstractClickHandler() {

                @Override
                public void onButton1Click() {
                    updateFollowButton(false);
                    FanfouServiceManager.doFollow(ProfilePage.this.mContext,
                            ProfilePage.this.user, new ResultHandler());
                }
            });

            dialog.show();
        } else {
            updateFollowButton(true);
            FanfouServiceManager.doFollow(this.mContext, this.user,
                    new ResultHandler());
        }

    }

    private void doRefresh() {
        FanfouServiceManager.doProfile(this, this.userId, new ResultHandler());
        if (this.isInitialized) {
        }
    }

    private String getUserName() {
        if ((this.user != null) && !TextUtils.isEmpty(this.user.screenName)) {
            final StringBuilder sb = new StringBuilder();
            sb.append("@").append(this.user.screenName).append(" ");
            return sb.toString();
        }
        return null;
    }

    private boolean hasPermission() {
        if (this.noPermission) {
            CommonHelper.notify(this, "你没有通过这个用户的验证");
            return false;
        }
        return true;
    }

    protected void initCheckState() {
        if (this.user != null) {
            showContent();
            updateUI();
        } else {
            doRefresh();
            this.mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private void initialize() {
        this.mHandler = new Handler();
        this.mLoader = AppContext.getImageLoader();
    }

    private void log(final String message) {
        Log.d(ProfilePage.tag, message);
    }

    @Override
    public void onClick(final View v) {
        if ((this.user == null) || this.user.isNull()) {
            return;
        }
        switch (v.getId()) {
        case R.id.user_action_reply:
            ActionManager.doWrite(this, "@" + this.user.screenName + " ");
            break;
        case R.id.user_action_message:
            ActionManager.doMessage(this, this.user);
            break;
        case R.id.user_action_follow:
            doFollow();
            break;
        case R.id.user_statuses_view:
            if (hasPermission()) {
                ActionManager.doShowTimeline(this, this.user);
            }
            break;
        case R.id.user_favorites_view:
            if (hasPermission()) {
                ActionManager.doShowFavorites(this, this.user);
            }
            break;
        case R.id.user_friends_view:
            if (hasPermission()) {
                ActionManager.doShowFriends(this, this.user);
            }
            break;
        case R.id.user_followers_view:
            if (hasPermission()) {
                ActionManager.doShowFollowers(this, this.user);
            }
            break;
        case R.id.user_location_view:
            break;
        case R.id.user_site_view:
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
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (action == null) {
            this.userId = intent.getStringExtra(Constants.EXTRA_ID);
            this.user = (User) intent.getParcelableExtra(Constants.EXTRA_DATA);
            if (this.user != null) {
                this.userId = this.user.id;
            }
        } else if (action.equals(Intent.ACTION_VIEW)) {
            final Uri data = intent.getData();
            if (data != null) {
                this.userId = data.getLastPathSegment();
            }
        }
        if ((this.user == null) && (this.userId != null)) {
            this.user = CacheManager.getUser(this, this.userId);
        }

        if (this.user != null) {
            this.userId = this.user.id;
        }

        if (AppContext.getUserId().equals(this.userId)) {
            ActionManager.doMyProfile(this);
            finish();
        }

    }

    /**
     * 初始化和设置ActionBar
     */
    private void setActionBar() {
        this.mActionBar = (ActionBar) findViewById(R.id.actionbar);
        this.mActionBar.setTitle("个人空间");
        this.mActionBar.setRightAction(new WriteAction());
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
        setContentView(R.layout.profile);

        // View root=findViewById(R.id.root);
        // ThemeHelper.setBackgroundColor(root);

        setActionBar();

        this.mEmptyView = findViewById(R.id.empty);
        this.mScrollView = (ScrollView) findViewById(R.id.user_profile);

        this.mHeader = (RelativeLayout) findViewById(R.id.user_headview);
        this.mHead = (ImageView) findViewById(R.id.user_head);
        this.mName = (TextView) findViewById(R.id.user_name);
        final TextPaint tp = this.mName.getPaint();
        tp.setFakeBoldText(true);

        this.mExtraInfo = (TextView) findViewById(R.id.user_extrainfo);

        this.mProtected = (ImageView) findViewById(R.id.user_protected);

        this.mRelationship = (TextView) findViewById(R.id.user_relationship);

        this.mDescription = (TextView) findViewById(R.id.user_description);

        this.mActions = (LinearLayout) findViewById(R.id.user_actionview);
        this.mReplyAction = (ImageView) findViewById(R.id.user_action_reply);
        this.mMessageAction = (ImageView) findViewById(R.id.user_action_message);
        this.mFollowAction = (ImageView) findViewById(R.id.user_action_follow);

        this.mStatusesView = (ViewGroup) findViewById(R.id.user_statuses_view);
        this.mStatusesTitle = (TextView) findViewById(R.id.user_statuses_title);
        this.mStatusesInfo = (TextView) findViewById(R.id.user_statuses);

        this.mFavoritesView = (ViewGroup) findViewById(R.id.user_favorites_view);
        this.mFavoritesTitle = (TextView) findViewById(R.id.user_favorites_title);
        this.mFavoritesInfo = (TextView) findViewById(R.id.user_favorites);

        this.mFriendsView = (ViewGroup) findViewById(R.id.user_friends_view);
        this.mFriendsTitle = (TextView) findViewById(R.id.user_friends_title);
        this.mFriendsInfo = (TextView) findViewById(R.id.user_friends);

        this.mFollowersView = (ViewGroup) findViewById(R.id.user_followers_view);
        this.mFollowersTitle = (TextView) findViewById(R.id.user_followers_title);
        this.mFollowersInfo = (TextView) findViewById(R.id.user_followers);

        this.mStatusesView.setOnClickListener(this);
        this.mFavoritesView.setOnClickListener(this);
        this.mFriendsView.setOnClickListener(this);
        this.mFollowersView.setOnClickListener(this);

        this.mReplyAction.setOnClickListener(this);
        this.mMessageAction.setOnClickListener(this);
        this.mFollowAction.setOnClickListener(this);

        this.mScrollView.setVisibility(View.GONE);
    }

    private void showContent() {
        if (AppContext.DEBUG) {
            log("showContent()");
        }
        this.isInitialized = true;
        this.mEmptyView.setVisibility(View.GONE);
        this.mScrollView.setVisibility(View.VISIBLE);

    }

    private void updateFollowButton(final boolean following) {
        this.mFollowAction.setImageResource(following ? R.drawable.btn_unfollow
                : R.drawable.btn_follow);
    }

    private void updateRelationshipState(final boolean follow) {
        this.mRelationship.setVisibility(View.VISIBLE);
        this.mRelationship.setText(follow ? "(此用户正在关注你)" : "(此用户没有关注你)");
    }

    private void updateUI() {
        if (this.user == null) {
            return;
        }
        this.noPermission = !this.user.following && this.user.protect;

        if (AppContext.DEBUG) {
            log("updateUI user.name=" + this.user.screenName);
        }

        final boolean textMode = OptionHelper.readBoolean(this.mContext,
                R.string.option_text_mode, false);
        if (textMode) {
            this.mHead.setVisibility(View.GONE);
        } else {
            this.mHead.setTag(this.user.profileImageUrl);
            this.mLoader.displayImage(this.user.profileImageUrl, this.mHead,
                    R.drawable.default_head);
        }

        this.mName.setText(this.user.screenName);

        String prefix;

        if (this.user.gender.equals("男")) {
            prefix = "他";
        } else if (this.user.gender.equals("女")) {
            prefix = "她";
        } else {
            prefix = "TA";
        }

        this.mActionBar.setTitle(this.user.screenName);
        this.mStatusesTitle.setText(prefix + "的消息");
        this.mFavoritesTitle.setText(prefix + "的收藏");
        this.mFriendsTitle.setText(prefix + "关注的人");
        this.mFollowersTitle.setText("关注" + prefix + "的人");

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

        this.mProtected.setVisibility(this.user.protect ? View.VISIBLE
                : View.GONE);

        setExtraInfo(this.user);
        updateFollowButton(this.user.following);

        if (!this.noPermission) {
            doFetchRelationshipInfo();
        }
    }

}
