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
package com.fanfou.app.opensource.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.util.CommonHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.15
 * @version 2.0 2011.08.20
 * @version 3.0 2011.09.18
 * @version 3.5 2011.10.27
 * @version 3.6 2011.11.04
 * 
 */
public class ActionBar extends RelativeLayout implements OnClickListener {

    public static abstract class AbstractAction implements Action {
        final private int mDrawable;

        public AbstractAction(final int drawable) {
            this.mDrawable = drawable;
        }

        @Override
        public int getDrawable() {
            return this.mDrawable;
        }
    }

    public interface Action {
        public int getDrawable();

        public void performAction(View view);
    }

    public static class BackAction extends AbstractAction {
        private final Activity context;

        public BackAction(final Activity mContext) {
            super(R.drawable.i_back);
            this.context = mContext;
        }

        @Override
        public void performAction(final View view) {
            this.context.finish();
        }

    }

    public static class IntentAction extends AbstractAction {
        Context mContext;
        Intent mIntent;

        public IntentAction(final Context context, final Intent intent,
                final int drawable) {
            super(drawable);
            this.mContext = context;
            this.mIntent = intent;
        }

        @Override
        public void performAction(final View view) {
            try {
                this.mContext.startActivity(this.mIntent);
            } catch (final ActivityNotFoundException e) {
                CommonHelper.notify(this.mContext, "Activity Not Found.");
            }
        }
    }

    public interface OnRefreshClickListener {
        public void onRefreshClick();
    }

    public static class RefreshAction extends AbstractAction {
        private final ActionBar ab;

        public RefreshAction(final ActionBar ab) {
            super(R.drawable.i_refresh);
            this.ab = ab;
        }

        @Override
        public void performAction(final View view) {
            this.ab.onRefreshClick();
        }

    }

    public static class SearchAction extends AbstractAction {
        private final Activity mActivity;

        public SearchAction(final Activity activity) {
            super(R.drawable.i_search);
            this.mActivity = activity;
        }

        @Override
        public void performAction(final View view) {
            this.mActivity.onSearchRequested();
        }

    }

    public static class ToastAction extends AbstractAction {
        private final Context mContext;
        private final String mText;

        public ToastAction(final Context context, final String text,
                final int drawable) {
            super(drawable);
            this.mContext = context;
            this.mText = text;
        }

        @Override
        public void performAction(final View view) {
            Toast.makeText(this.mContext, this.mText, Toast.LENGTH_SHORT)
                    .show();
        }

    }

    public static class WriteAction extends AbstractAction {
        private final Context context;
        private final Status status;

        public WriteAction(final Context context, final Status status) {
            super(R.drawable.i_write);
            this.context = context;
            this.status = status;
        }

        @Override
        public void performAction(final View view) {
            if (this.status == null) {
                ActionManager.doWrite(this.context, null);
                // ActionManager.doSend(context);
            } else {
                ActionManager.doReply(this.context, this.status);
            }
        }
    }

    public static final int TYPE_HOME = 0; // 左侧LOGO，中间标题文字，右侧编辑图标

    public static final int TYPE_NORMAL = 1; // 左侧LOGO，中间标题文字，右侧编辑图标

    // private boolean mRefreshable=false;

    public static final int TYPE_EDIT = 2; // 左侧LOGO，中间标题文字，右侧发送图标

    private Context mContext;

    private LayoutInflater mInflater;

    private ViewGroup mActionBar;// 标题栏

    private ImageView mLeftButton;// 饭否标志

    private ImageView mRightButton;// 右边的动作图标

    private ImageView mRefreshButton;// 右侧第二个图标，刷新

    private TextView mTitle;// 居中标题

    private OnRefreshClickListener mOnRefreshClickListener = null;

    public ActionBar(final Context context) {
        super(context);
        initViews(context);
    }

    public ActionBar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    private void initViews(final Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);

        this.mActionBar = (ViewGroup) this.mInflater.inflate(
                R.layout.actionbar, null);

        addView(this.mActionBar);
        this.mLeftButton = (ImageView) this.mActionBar
                .findViewById(R.id.actionbar_left);
        this.mRightButton = (ImageView) this.mActionBar
                .findViewById(R.id.actionbar_right);
        this.mRefreshButton = (ImageView) findViewById(R.id.actionbar_refresh);
        this.mTitle = (TextView) this.mActionBar
                .findViewById(R.id.actionbar_title);

        this.mLeftButton.setOnClickListener(this);
        this.mRightButton.setOnClickListener(this);
        this.mRefreshButton.setOnClickListener(this);
    }

    @Override
    public void onClick(final View view) {
        final Object tag = view.getTag();
        if (tag instanceof Action) {
            final Action action = (Action) tag;
            action.performAction(view);
        }
    }

    private void onRefreshClick() {
        if (this.mOnRefreshClickListener != null) {
            this.mOnRefreshClickListener.onRefreshClick();
        }
    }

    public void removeLeftIcon() {
        this.mLeftButton.setVisibility(View.GONE);
    }

    public void removeRightIcon() {
        this.mRightButton.setVisibility(View.GONE);
    }

    public void setLeftAction(final Action action) {
        this.mLeftButton.setImageResource(action.getDrawable());
        this.mLeftButton.setTag(action);
    }

    public void setLeftActionEnabled(final boolean enabled) {
        this.mLeftButton.setEnabled(enabled);
    }

    public void setLeftIcon(final int resId) {
        this.mLeftButton.setImageResource(resId);
    }

    private void setRefreshAction(final Action action) {
        this.mRefreshButton.setImageResource(action.getDrawable());
        this.mRefreshButton.setTag(action);
    }

    public void setRefreshActionEnabled(final boolean enabled) {
        this.mRefreshButton.setEnabled(enabled);
    }

    public void setRefreshEnabled(
            final OnRefreshClickListener onRefreshClickListener) {
        if (onRefreshClickListener != null) {
            this.mOnRefreshClickListener = onRefreshClickListener;
            setRefreshAction(new RefreshAction(this));
        }
    }

    public void setRightAction(final Action action) {
        this.mRightButton.setImageResource(action.getDrawable());
        this.mRightButton.setTag(action);
    }

    public void setRightActionEnabled(final boolean enabled) {
        this.mRightButton.setEnabled(enabled);
    }

    public void setRightIcon(final int resId) {
        this.mRightButton.setImageResource(resId);
    }

    public void setTitle(final CharSequence text) {
        this.mTitle.setText(text);
    }

    public void setTitle(final int resId) {
        this.mTitle.setText(resId);
    }

    public void setTitleClickListener(final OnClickListener li) {
        this.mTitle.setOnClickListener(li);
    }

    public void setType() {

    }

    public void startAnimation() {
        post(new Runnable() {
            @Override
            public void run() {
                ActionBar.this.mRefreshButton.setOnClickListener(null);
                ActionBar.this.mRefreshButton.setImageDrawable(null);
                ActionBar.this.mRefreshButton
                        .setBackgroundResource(R.drawable.animation_refresh);
                final AnimationDrawable frameAnimation = (AnimationDrawable) ActionBar.this.mRefreshButton
                        .getBackground();
                frameAnimation.start();
            }
        });
    }

    public void stopAnimation() {
        post(new Runnable() {
            @Override
            public void run() {
                final AnimationDrawable frameAnimation = (AnimationDrawable) ActionBar.this.mRefreshButton
                        .getBackground();
                if (frameAnimation != null) {
                    frameAnimation.stop();
                    ActionBar.this.mRefreshButton.setBackgroundDrawable(null);
                    ActionBar.this.mRefreshButton
                            .setImageResource(R.drawable.i_refresh);
                    ActionBar.this.mRefreshButton
                            .setOnClickListener(ActionBar.this);
                }
            }
        });

    }

}
