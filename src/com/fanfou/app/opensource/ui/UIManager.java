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

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.BaseAdapter;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.dialog.ConfirmDialog;
import com.fanfou.app.opensource.service.FanfouServiceManager;
import com.fanfou.app.opensource.ui.quickaction.ActionItem;
import com.fanfou.app.opensource.ui.quickaction.QuickAction;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.09
 * @version 1.1 2011.10.25
 * @version 1.2 2011.10.27
 * @version 1.3 2011.10.28
 * @version 2.0 2011.10.29
 * @version 2.1 2011.11.07
 * @version 3.0 2011.12.19
 * @version 3.1 2011.12.23
 * 
 */
public final class UIManager {
    public abstract static class ActionResultHandler implements
            ActionManager.ResultListener {
        @Override
        public void onActionFailed(final int type, final String message) {
        }
    }

    public static final int QUICK_ACTION_ID_REPLY = 0;
    public static final int QUICK_ACTION_ID_DELETE = 1;
    public static final int QUICK_ACTION_ID_RETWEET = 2;
    public static final int QUICK_ACTION_ID_FAVORITE = 3;
    public static final int QUICK_ACTION_ID_UNFAVORITE = 4;
    public static final int QUICK_ACTION_ID_PROFILE = 5;

    public static final int QUICK_ACTION_ID_SHARE = 6;

    public static void doDelete(final Activity activity, final Status s,
            final BaseAdapter adapter) {
        final ActionResultHandler li = new ActionResultHandler() {
            @Override
            public void onActionSuccess(final int type, final String message) {
                adapter.notifyDataSetChanged();
            }
        };
        FanfouServiceManager.doStatusDelete(activity, s.id, li);
    }

    public static void doDelete(final Activity activity, final Status s,
            final BaseAdapter adapter, final List<Status> ss) {
        final ActionResultHandler li = new ActionResultHandler() {
            @Override
            public void onActionSuccess(final int type, final String message) {
                ss.remove(s);
                adapter.notifyDataSetChanged();
            }
        };
        FanfouServiceManager.doStatusDelete(activity, s.id, li);
    }

    public static void doDelete(final Activity activity, final Status s,
            final Cursor c) {
        final ActionResultHandler li = new ActionResultHandler() {
            @Override
            public void onActionSuccess(final int type, final String message) {
                c.requery();
            }
        };
        FanfouServiceManager.doStatusDelete(activity, s.id, li);
    }

    public static QuickAction makePopup(final Context context,
            final Status status) {
        final ActionItem reply = new ActionItem(
                UIManager.QUICK_ACTION_ID_REPLY, "回复", context.getResources()
                        .getDrawable(R.drawable.ic_pop_reply));

        final ActionItem delete = new ActionItem(
                UIManager.QUICK_ACTION_ID_DELETE, "删除", context.getResources()
                        .getDrawable(R.drawable.ic_pop_delete));

        final ActionItem retweet = new ActionItem(
                UIManager.QUICK_ACTION_ID_RETWEET, "转发", context.getResources()
                        .getDrawable(R.drawable.ic_pop_retweet));

        final ActionItem favorite = new ActionItem(
                UIManager.QUICK_ACTION_ID_FAVORITE, "收藏", context
                        .getResources().getDrawable(R.drawable.ic_pop_favorite));
        // favorite.setSticky(true);

        final ActionItem unfavorite = new ActionItem(
                UIManager.QUICK_ACTION_ID_UNFAVORITE, "取消", context
                        .getResources().getDrawable(
                                R.drawable.ic_pop_unfavorite));
        // unfavorite.setSticky(true);

        final ActionItem profile = new ActionItem(
                UIManager.QUICK_ACTION_ID_PROFILE, "空间", context.getResources()
                        .getDrawable(R.drawable.ic_pop_profile));

        final ActionItem share = new ActionItem(
                UIManager.QUICK_ACTION_ID_SHARE, "分享", context.getResources()
                        .getDrawable(R.drawable.ic_pop_share));

        final boolean me = status.userId.equals(AppContext.getUserId());

        final QuickAction q = new QuickAction(context, QuickAction.HORIZONTAL);
        q.addActionItem(me ? delete : reply);
        q.addActionItem(retweet);
        q.addActionItem(status.favorited ? unfavorite : favorite);
        q.addActionItem(share);
        q.addActionItem(profile);

        return q;
    }

    public static void showPopup(final Activity a, final Cursor c,
            final View v, final Status s) {

        final QuickAction q = UIManager.makePopup(a, s);
        q.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

            @Override
            public void onItemClick(final QuickAction source, final int pos,
                    final int actionId) {

                switch (actionId) {
                case QUICK_ACTION_ID_REPLY:
                    ActionManager.doReply(a, s);
                    break;
                case QUICK_ACTION_ID_DELETE:
                    final ConfirmDialog dialog = new ConfirmDialog(a, "删除消息",
                            "要删除这条消息吗？");
                    dialog.setClickListener(new ConfirmDialog.AbstractClickHandler() {

                        @Override
                        public void onButton1Click() {
                            UIManager.doDelete(a, s, c);
                        }
                    });
                    dialog.show();
                    break;
                case QUICK_ACTION_ID_FAVORITE:
                case QUICK_ACTION_ID_UNFAVORITE:
                    FanfouServiceManager.doFavorite(a, s, c);
                    break;
                case QUICK_ACTION_ID_RETWEET:
                    ActionManager.doRetweet(a, s);
                    break;
                case QUICK_ACTION_ID_SHARE:
                    ActionManager.doShare(a, s);
                    break;
                case QUICK_ACTION_ID_PROFILE:
                    ActionManager.doProfile(a, s);
                    break;
                default:
                    break;
                }
            }
        });
        q.show(v);
    }

    public static void showPopup(final Activity a, final View v,
            final Status s, final BaseAdapter adapter) {

        final QuickAction q = UIManager.makePopup(a, s);
        q.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

            @Override
            public void onItemClick(final QuickAction source, final int pos,
                    final int actionId) {

                switch (actionId) {
                case QUICK_ACTION_ID_REPLY:
                    ActionManager.doReply(a, s);
                    break;
                case QUICK_ACTION_ID_DELETE:
                    final ConfirmDialog dialog = new ConfirmDialog(a, "删除消息",
                            "要删除这条消息吗？");
                    dialog.setClickListener(new ConfirmDialog.AbstractClickHandler() {

                        @Override
                        public void onButton1Click() {
                            UIManager.doDelete(a, s, adapter);
                        }
                    });
                    dialog.show();
                    break;
                case QUICK_ACTION_ID_FAVORITE:
                case QUICK_ACTION_ID_UNFAVORITE:
                    FanfouServiceManager.doFavorite(a, s, adapter);
                    break;
                case QUICK_ACTION_ID_RETWEET:
                    ActionManager.doRetweet(a, s);
                    break;
                case QUICK_ACTION_ID_SHARE:
                    ActionManager.doShare(a, s);
                    break;
                case QUICK_ACTION_ID_PROFILE:
                    ActionManager.doProfile(a, s);
                    break;
                default:
                    break;
                }
            }
        });
        q.show(v);
    }

    public static void showPopup(final Activity a, final View v,
            final Status s, final BaseAdapter adapter, final List<Status> ss) {

        final QuickAction q = UIManager.makePopup(a, s);
        q.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

            @Override
            public void onItemClick(final QuickAction source, final int pos,
                    final int actionId) {
                switch (actionId) {
                case QUICK_ACTION_ID_REPLY:
                    ActionManager.doReply(a, s);
                    break;
                case QUICK_ACTION_ID_DELETE:
                    final ConfirmDialog dialog = new ConfirmDialog(a, "删除消息",
                            "要删除这条消息吗？");
                    dialog.setClickListener(new ConfirmDialog.AbstractClickHandler() {

                        @Override
                        public void onButton1Click() {
                            UIManager.doDelete(a, s, adapter, ss);
                        }
                    });
                    dialog.show();
                    break;
                case QUICK_ACTION_ID_FAVORITE:
                case QUICK_ACTION_ID_UNFAVORITE:
                    FanfouServiceManager.doFavorite(a, s, adapter);
                    break;
                case QUICK_ACTION_ID_RETWEET:
                    ActionManager.doRetweet(a, s);
                    break;
                case QUICK_ACTION_ID_SHARE:
                    ActionManager.doShare(a, s);
                    break;
                case QUICK_ACTION_ID_PROFILE:
                    ActionManager.doProfile(a, s);
                    break;
                default:
                    break;
                }
            }
        });

        q.show(v);
    }

}
