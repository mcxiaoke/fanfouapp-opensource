/*
 * Copyright 2012 Evgeny Shishkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fanfou.app.opensource.util.support;

import java.util.LinkedList;
import java.util.Queue;

import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * 
 * @author Evgeny Shishkin
 * 
 */
class MsgManager extends Handler {

    private static final int MESSAGE_DISPLAY = 0xc2007;
    private static final int MESSAGE_ADD_VIEW = 0xc20074dd;
    private static final int MESSAGE_REMOVE = 0xc2007de1;

    private static MsgManager mInstance;

    /**
     * @return The currently used instance of the {@link MsgManager}.
     */
    static synchronized MsgManager getInstance() {
        if (MsgManager.mInstance == null) {
            MsgManager.mInstance = new MsgManager();
        }
        return MsgManager.mInstance;
    }

    private final Queue<AppMsg> msgQueue;

    private Animation inAnimation, outAnimation;

    private MsgManager() {
        this.msgQueue = new LinkedList<AppMsg>();
    }

    /**
     * Inserts a {@link AppMsg} to be displayed.
     * 
     * @param AppMsg
     */
    void add(final AppMsg appMsg) {
        this.msgQueue.add(appMsg);
        if (this.inAnimation == null) {
            this.inAnimation = AnimationUtils.loadAnimation(
                    appMsg.getActivity(), android.R.anim.fade_in);
        }
        if (this.outAnimation == null) {
            this.outAnimation = AnimationUtils.loadAnimation(
                    appMsg.getActivity(), android.R.anim.fade_out);
        }
        displayMsg();
    }

    private void addMsgToView(final AppMsg appMsg) {
        if (appMsg.getView().getParent() == null) {
            appMsg.getActivity().addContentView(appMsg.getView(),
                    appMsg.getLayoutParams());
        }
        appMsg.getView().startAnimation(this.inAnimation);
        final Message msg = obtainMessage(MsgManager.MESSAGE_REMOVE);
        msg.obj = appMsg;
        sendMessageDelayed(msg, appMsg.getDuration());
    }

    /**
     * Removes all {@link AppMsg} from the queue.
     */
    void clearAllMsg() {
        if (this.msgQueue != null) {
            this.msgQueue.clear();
        }
        removeMessages(MsgManager.MESSAGE_DISPLAY);
        removeMessages(MsgManager.MESSAGE_ADD_VIEW);
        removeMessages(MsgManager.MESSAGE_REMOVE);
    }

    /**
     * Removes all {@link AppMsg} from the queue.
     */
    void clearMsg(final AppMsg appMsg) {
        this.msgQueue.remove(appMsg);
    }

    /**
     * Displays the next {@link AppMsg} within the queue.
     */
    private void displayMsg() {
        if (this.msgQueue.isEmpty()) {
            return;
        }
        // First peek whether the AppMsg is being displayed.
        final AppMsg appMsg = this.msgQueue.peek();
        // If the activity is null we throw away the AppMsg.
        if (appMsg.getActivity() == null) {
            this.msgQueue.poll();
        }
        final Message msg;
        if (!appMsg.isShowing()) {
            // Display the AppMsg
            msg = obtainMessage(MsgManager.MESSAGE_ADD_VIEW);
            msg.obj = appMsg;
            sendMessage(msg);
        } else {
            msg = obtainMessage(MsgManager.MESSAGE_DISPLAY);
            sendMessageDelayed(msg,
                    appMsg.getDuration() + this.inAnimation.getDuration()
                            + this.outAnimation.getDuration());
        }
    }

    @Override
    public void handleMessage(final Message msg) {
        final AppMsg appMsg;
        switch (msg.what) {
        case MESSAGE_DISPLAY:
            displayMsg();
            break;
        case MESSAGE_ADD_VIEW:
            appMsg = (AppMsg) msg.obj;
            addMsgToView(appMsg);
            break;
        case MESSAGE_REMOVE:
            appMsg = (AppMsg) msg.obj;
            removeMsg(appMsg);
            break;
        default:
            super.handleMessage(msg);
            break;
        }
    }

    /**
     * Removes the {@link AppMsg}'s view after it's display duration.
     * 
     * @param appMsg
     *            The {@link AppMsg} added to a {@link ViewGroup} and should be
     *            removed.s
     */
    private void removeMsg(final AppMsg appMsg) {
        final ViewGroup parent = ((ViewGroup) appMsg.getView().getParent());
        if (parent != null) {
            appMsg.getView().startAnimation(this.outAnimation);
            // Remove the AppMsg from the queue.
            this.msgQueue.poll();
            // Remove the AppMsg from the view's parent.
            parent.removeView(appMsg.getView());
            final Message msg = obtainMessage(MsgManager.MESSAGE_DISPLAY);
            sendMessage(msg);
        }
    }
}
