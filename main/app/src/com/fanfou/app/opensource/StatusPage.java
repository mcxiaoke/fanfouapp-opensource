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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fanfou.app.opensource.api.bean.Status;
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
import com.fanfou.app.opensource.util.IOHelper;
import com.fanfou.app.opensource.util.OptionHelper;
import com.fanfou.app.opensource.util.PatternsHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.01
 * @version 1.2 2011.10.24
 * @version 2.0 2011.10.25
 * @version 2.1 2011.10.26
 * @version 2.2 2011.10.28
 * @version 2.3 2011.10.29
 * @version 2.4 2011.11.04
 * @version 2.5 2011.11.07
 * @version 2.6 2011.11.17
 * @version 2.7 2011.11.22
 * @version 2.8 2011.11.28
 * @version 2.9 2011.12.08
 * @version 3.0 2011.12.21
 * 
 */
public class StatusPage extends BaseActivity {

    private class WriteAction extends AbstractAction {

        public WriteAction() {
            super(R.drawable.i_write);
        }

        @Override
        public void performAction(final View view) {
            ActionManager.doWrite(StatusPage.this.mContext, getUserName());

        }
    }

    private static final int PHOTO_LOADING = -1;
    private static final int PHOTO_ICON = 0;
    private static final int PHOTO_SMALL = 1;

    private static final int PHOTO_LARGE = 2;

    private int mPhotoState = StatusPage.PHOTO_ICON;

    private ActionBar mActionBar;

    private ImageLoader mLoader;
    private String statusId;

    private Status status;

    private View vUser;
    private ImageView iUserHead;

    private TextView tUserName;
    private TextView tContent;

    private ImageView iPhoto;
    private TextView tDate;

    private TextView tSource;
    private ImageView bReply;
    private ImageView bRepost;
    private ImageView bFavorite;

    private ImageView bShare;

    private TextView vThread;

    private TextView vConversation;

    private boolean isMe;

    private String mPhotoUrl;

    private static final String TAG = StatusPage.class.getSimpleName();

    private void checkPhoto(final boolean textMode, final Status s) {
        if (!s.hasPhoto) {
            this.iPhoto.setVisibility(View.GONE);
            return;
        }

        this.mPhotoState = StatusPage.PHOTO_ICON;
        this.iPhoto.setVisibility(View.VISIBLE);
        this.iPhoto.setOnClickListener(this);

        // 先检查本地是否有大图缓存
        Bitmap bitmap = this.mLoader.getImage(s.photoLargeUrl, null);
        this.mPhotoUrl = s.photoLargeUrl;
        if (bitmap != null) {
            this.iPhoto.setImageBitmap(bitmap);
            this.mPhotoState = StatusPage.PHOTO_LARGE;
            return;
        }

        // 再检查本地是否有缩略图缓存
        bitmap = this.mLoader.getImage(s.photoImageUrl, null);
        this.mPhotoUrl = s.photoImageUrl;
        if (bitmap != null) {
            this.iPhoto.setImageBitmap(bitmap);
            this.mPhotoState = StatusPage.PHOTO_SMALL;
            return;
        }

        // 是否需要显示图片
        if (textMode) {
            this.iPhoto.setImageResource(R.drawable.photo_icon);
        } else {
            if (AppContext.isWifi()) {
                loadPhoto(StatusPage.PHOTO_LARGE);
            } else {
                this.iPhoto.setImageResource(R.drawable.photo_icon);
            }
        }
    }

    private void doCopy(final String content) {
        IOHelper.copyToClipBoard(this, content);
        CommonHelper.notify(this, "消息内容已复制到剪贴板");
    }

    private void doDelete() {
        final ConfirmDialog dialog = new ConfirmDialog(this, "删除消息",
                "要删除这条消息吗？");
        dialog.setClickListener(new ConfirmDialog.AbstractClickHandler() {
            @Override
            public void onButton1Click() {
                FanfouServiceManager.doStatusDelete(StatusPage.this.mContext,
                        StatusPage.this.status.id, true);
            }
        });
        dialog.show();

    }

    private void doFavorite() {
        final ActionManager.ResultListener li = new ActionManager.ResultListener() {

            @Override
            public void onActionFailed(final int type, final String message) {
            }

            @Override
            public void onActionSuccess(final int type, final String message) {
                if (AppContext.DEBUG) {
                    log("type="
                            + (type == Constants.TYPE_FAVORITES_CREATE ? "收藏"
                                    : "取消收藏") + " message=" + message);
                }
                if (type == Constants.TYPE_FAVORITES_CREATE) {
                    StatusPage.this.status.favorited = true;
                } else {
                    StatusPage.this.status.favorited = false;
                }
                updateFavoriteButton(StatusPage.this.status.favorited);
            }
        };
        updateFavoriteButton(!this.status.favorited);
        FanfouServiceManager.doFavorite(this, this.status, li);
    }

    private String getPhotoPath(final String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        final File file = new File(IOHelper.getImageCacheDir(this.mContext),
                StringHelper.md5(key) + ".jpg");
        if (AppContext.DEBUG) {
            log("loadFile path=" + file);
        }
        if (file.exists()) {
            return file.getAbsolutePath();
        } else {
            return null;
        }

    }

    private String getUserName() {
        if ((this.status != null)
                && !TextUtils.isEmpty(this.status.userScreenName)) {
            final StringBuilder sb = new StringBuilder();
            sb.append("@").append(this.status.userScreenName).append(" ");
            return sb.toString();
        }
        return null;
    }

    private void goPhotoViewer() {
        if (!TextUtils.isEmpty(this.mPhotoUrl)) {
            final String filePath = getPhotoPath(this.mPhotoUrl);
            if (AppContext.DEBUG) {
                Log.d(StatusPage.TAG, "goPhotoViewer() url=" + filePath);
            }
            final Intent intent = new Intent(this.mContext, PhotoViewPage.class);
            intent.putExtra(Constants.EXTRA_URL, filePath);
            this.mContext.startActivity(intent);
            overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_enter);
        }
    }

    @SuppressLint("HandlerLeak")
    private void loadPhoto(final int type) {
        if (type == StatusPage.PHOTO_ICON) {
            this.iPhoto.setImageResource(R.drawable.photo_icon);
            if (AppContext.DEBUG) {
                Log.d(StatusPage.TAG, "loadPhoto mPhotoState="
                        + this.mPhotoState + " type=" + type);
            }
            return;
        }
        this.mPhotoState = StatusPage.PHOTO_LOADING;
        this.iPhoto.setImageResource(R.drawable.photo_loading);
        // clear queue before load big photos;
        AppContext.getImageLoader().clearQueue();
        if (AppContext.DEBUG) {
            Log.d(StatusPage.TAG, "loadPhoto mPhotoState=" + this.mPhotoState
                    + " type=" + type);
        }
        final Handler handler = new Handler() {

            @Override
            public void handleMessage(final Message msg) {
                final int what = msg.what;
                if (what == ImageLoader.MESSAGE_FINISH) {
                    final Bitmap bitmap = (Bitmap) msg.obj;
                    if (AppContext.DEBUG) {
                        Log.d(StatusPage.TAG, "handler onfinish bitmap="
                                + bitmap);
                    }
                    if (bitmap != null) {
                        StatusPage.this.iPhoto.setImageBitmap(bitmap);
                        StatusPage.this.mPhotoState = type;
                    } else {
                        StatusPage.this.iPhoto
                                .setImageResource(R.drawable.photo_icon);
                        StatusPage.this.mPhotoState = StatusPage.PHOTO_ICON;
                    }
                } else if (what == ImageLoader.MESSAGE_ERROR) {
                    StatusPage.this.iPhoto
                            .setImageResource(R.drawable.photo_icon);
                    StatusPage.this.mPhotoState = StatusPage.PHOTO_ICON;
                }
            }
        };
        // final ImageLoaderCallback callback = new ImageLoaderCallback() {
        //
        // @Override
        // public void onFinish(String key, Bitmap bitmap) {
        // if (App.DEBUG) {
        // Log.d(TAG, "callback onfinish bitmap=" + bitmap);
        // }
        // if (bitmap != null) {
        // iPhoto.setImageBitmap(bitmap);
        // mPhotoState = type;
        // } else {
        // iPhoto.setImageResource(R.drawable.photo_icon);
        // mPhotoState = PHOTO_ICON;
        // }
        // }
        //
        // @Override
        // public void onError(String url, String message) {
        // iPhoto.setImageResource(R.drawable.photo_icon);
        // mPhotoState = PHOTO_ICON;
        // }
        //
        // @Override
        // public String toString(){
        // return "ImageLoaderCallback:"+this.hashCode();
        // }
        // };

        if (type == StatusPage.PHOTO_LARGE) {
            this.mPhotoUrl = this.status.photoLargeUrl;
        } else if (type == StatusPage.PHOTO_SMALL) {
            this.mPhotoUrl = this.status.photoThumbUrl;
        }

        if (AppContext.DEBUG) {
            Log.d(StatusPage.TAG, "loadPhoto mPhotoState=" + this.mPhotoState
                    + " type=" + type + " url=" + this.mPhotoUrl);
        }

        this.iPhoto.setTag(this.mPhotoUrl);
        final Bitmap bitmap = this.mLoader.getImage(this.mPhotoUrl, handler);
        if (bitmap != null) {
            this.iPhoto.setImageBitmap(bitmap);
            this.mPhotoState = type;
            if (AppContext.DEBUG) {
                Log.d(StatusPage.TAG, "loadPhoto has cache url="
                        + this.mPhotoUrl + " type=" + type);
            }
        }
    }

    private void log(final String message) {
        Log.d(StatusPage.TAG, message);
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        switch (id) {
        case R.id.status_action_reply:
            if (this.isMe) {
                doDelete();
            } else {
                ActionManager.doReply(this, this.status);
            }
            break;
        case R.id.status_action_retweet:
            ActionManager.doRetweet(this, this.status);
            break;
        case R.id.status_action_favorite:
            doFavorite();
            break;
        case R.id.status_action_share:
            ActionManager.doShare(this, this.status);
            break;
        case R.id.status_top:
            ActionManager.doProfile(this, this.status);
            break;
        // case R.id.status_text:
        // break;
        case R.id.status_photo:
            onClickPhoto();
            break;
        case R.id.status_thread:
            final Intent intent = new Intent(this.mContext,
                    ConversationPage.class);
            intent.putExtra(Constants.EXTRA_DATA, this.status);
            this.mContext.startActivity(intent);
            // testAnimation();
            break;
        default:
            break;
        }
    }

    private void onClickPhoto() {
        if (AppContext.DEBUG) {
            Log.d(StatusPage.TAG, "onClickPhoto() mPhotoState="
                    + this.mPhotoState);
        }
        switch (this.mPhotoState) {
        case PHOTO_ICON:
            loadPhoto(StatusPage.PHOTO_LARGE);
            break;
        case PHOTO_SMALL:
            loadPhoto(StatusPage.PHOTO_LARGE);
            break;
        case PHOTO_LARGE:
            goPhotoViewer();
            break;
        case PHOTO_LOADING:
            break;
        default:
            break;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLoader = AppContext.getImageLoader();
        setContentView(R.layout.status);

        // View root=findViewById(R.id.root);
        // ThemeHelper.setBackgroundColor(root);

        setActionBar();
        setLayout();
        parseIntent();
        updateUI();

    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        doCopy(this.status.simpleText);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        setIntent(intent);
        parseIntent();
        updateUI();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void parseIntent() {
        final Intent intent = getIntent();
        this.statusId = intent.getStringExtra(Constants.EXTRA_ID);
        this.status = (Status) intent.getParcelableExtra(Constants.EXTRA_DATA);

        if ((this.status == null) && (this.statusId != null)) {
            this.status = CacheManager.getStatus(this, this.statusId);
        } else {
            this.statusId = this.status.id;
        }

        if ((this.status != null) && (this.status.userId != null)) {
            this.isMe = this.status.userId.equals(AppContext.getUserId());
        }
    }

    /**
     * 初始化和设置ActionBar
     */
    private void setActionBar() {
        this.mActionBar = (ActionBar) findViewById(R.id.actionbar);
        this.mActionBar.setTitle("消息");
        this.mActionBar.setRightAction(new WriteAction());
        this.mActionBar.setLeftAction(new ActionBar.BackAction(this));
    }

    private void setLayout() {

        this.vUser = findViewById(R.id.status_top);
        this.vUser.setOnClickListener(this);
        this.iUserHead = (ImageView) findViewById(R.id.user_head);
        this.tUserName = (TextView) findViewById(R.id.user_name);
        final TextPaint tp = this.tUserName.getPaint();
        tp.setFakeBoldText(true);

        this.tContent = (TextView) findViewById(R.id.status_text);
        this.iPhoto = (ImageView) findViewById(R.id.status_photo);
        this.tDate = (TextView) findViewById(R.id.status_date);
        this.tSource = (TextView) findViewById(R.id.status_source);
        this.vThread = (TextView) findViewById(R.id.status_thread);

        this.vConversation = (TextView) findViewById(R.id.status_conversation);
        this.vConversation.setVisibility(View.GONE);

        this.bReply = (ImageView) findViewById(R.id.status_action_reply);
        this.bRepost = (ImageView) findViewById(R.id.status_action_retweet);
        this.bFavorite = (ImageView) findViewById(R.id.status_action_favorite);
        this.bShare = (ImageView) findViewById(R.id.status_action_share);

        this.bReply.setOnClickListener(this);
        this.bRepost.setOnClickListener(this);
        this.bFavorite.setOnClickListener(this);
        this.bShare.setOnClickListener(this);
        this.vThread.setOnClickListener(this);

        registerForContextMenu(this.tContent);
    }

    private void updateFavoriteButton(final boolean favorited) {
        if (favorited) {
            this.bFavorite.setImageResource(R.drawable.i_bar2_unfavorite);
        } else {
            this.bFavorite.setImageResource(R.drawable.i_bar2_favorite);
        }
    }

    private void updateUI() {
        if (this.status != null) {

            this.mActionBar.setTitle(this.status.userScreenName);

            final boolean textMode = OptionHelper.readBoolean(this.mContext,
                    R.string.option_text_mode, false);
            if (textMode) {
                this.iUserHead.setVisibility(View.GONE);
            } else {
                this.iUserHead.setTag(this.status.userProfileImageUrl);
                this.mLoader.displayImage(this.status.userProfileImageUrl,
                        this.iUserHead, R.drawable.default_head);
            }

            this.tUserName.setText(this.status.userScreenName);

            PatternsHelper.setStatus(this.tContent, this.status.text);
            checkPhoto(textMode, this.status);

            this.tDate.setText(DateTimeHelper
                    .getInterval(this.status.createdAt));
            this.tSource.setText("通过" + this.status.source);

            if (this.isMe) {
                this.bReply.setImageResource(R.drawable.i_bar2_delete);
            } else {
                this.bReply.setImageResource(R.drawable.i_bar2_reply);
            }

            updateFavoriteButton(this.status.favorited);

            if (this.status.isThread) {
                this.vThread.setVisibility(View.VISIBLE);
            } else {
                this.vThread.setVisibility(View.GONE);
            }
        }
    }

}
