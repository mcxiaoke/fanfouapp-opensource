/*******************************************************************************
 * Copyright Lorensius. W. L. T 
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
package com.fanfou.app.opensource.ui.quickaction;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * Custom popup window.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 * 
 */
public class PopupWindows {
    protected Context mContext;
    protected PopupWindow mWindow;
    protected View mRootView;
    protected Drawable mBackground = null;
    protected WindowManager mWindowManager;

    /**
     * Constructor.
     * 
     * @param context
     *            Context
     */
    public PopupWindows(final Context context) {
        mContext = context;
        mWindow = new PopupWindow(context);

        mWindow.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mWindow.dismiss();

                    return true;
                }

                return false;
            }
        });

        mWindowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * Dismiss the popup window.
     */
    public void dismiss() {
        mWindow.dismiss();
    }

    /**
     * On dismiss
     */
    protected void onDismiss() {
    }

    /**
     * On show
     */
    protected void onShow() {
    }

    /**
     * On pre show
     */
    protected void preShow() {
        if (mRootView == null) {
            throw new IllegalStateException(
                    "setContentView was not called with a view to display.");
        }

        onShow();

        if (mBackground == null) {
            mWindow.setBackgroundDrawable(new BitmapDrawable());
        } else {
            mWindow.setBackgroundDrawable(mBackground);
        }

        mWindow.setWidth(LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(LayoutParams.WRAP_CONTENT);
        mWindow.setTouchable(true);
        mWindow.setFocusable(true);
        mWindow.setOutsideTouchable(true);

        mWindow.setContentView(mRootView);
    }

    /**
     * Set background drawable.
     * 
     * @param background
     *            Background drawable
     */
    public void setBackgroundDrawable(final Drawable background) {
        mBackground = background;
    }

    /**
     * Set content view.
     * 
     * @param layoutResID
     *            Resource id
     */
    public void setContentView(final int layoutResID) {
        final LayoutInflater inflator = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(inflator.inflate(layoutResID, null));
    }

    /**
     * Set content view.
     * 
     * @param root
     *            Root view
     */
    public void setContentView(final View root) {
        mRootView = root;

        mWindow.setContentView(root);
    }

    /**
     * Set listener on window dismissed.
     * 
     * @param listener
     */
    public void setOnDismissListener(
            final PopupWindow.OnDismissListener listener) {
        mWindow.setOnDismissListener(listener);
    }
}
