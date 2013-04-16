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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fanfou.app.opensource.R;

/**
 * QuickAction dialog, shows action list as icon and text like the one in
 * Gallery3D app. Currently supports vertical and horizontal layout.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 * 
 *         Contributors: - Kevin Peck <kevinwpeck@gmail.com>
 */
public class QuickAction extends PopupWindows implements OnDismissListener {
    /**
     * Listener for item click
     * 
     */
    public interface OnActionItemClickListener {
        public abstract void onItemClick(QuickAction source, int pos,
                int actionId);
    }

    /**
     * Listener for window dismiss
     * 
     */
    public interface OnDismissListener {
        public abstract void onDismiss();
    }

    private View mRootView;
    private ImageView mArrowUp;
    private ImageView mArrowDown;
    private final LayoutInflater mInflater;
    private ViewGroup mTrack;
    private ScrollView mScroller;

    private OnActionItemClickListener mItemClickListener;

    private OnDismissListener mDismissListener;

    private final List<ActionItem> actionItems = new ArrayList<ActionItem>();
    private boolean mDidAction;
    private int mChildPos;
    private int mInsertPos;
    private int mAnimStyle;

    private final int mOrientation;
    private int rootWidth = 0;

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int ANIM_GROW_FROM_LEFT = 1;
    public static final int ANIM_GROW_FROM_RIGHT = 2;
    public static final int ANIM_GROW_FROM_CENTER = 3;

    public static final int ANIM_REFLECT = 4;

    public static final int ANIM_AUTO = 5;

    /**
     * Constructor for default vertical layout
     * 
     * @param context
     *            Context
     */
    public QuickAction(final Context context) {
        this(context, VERTICAL);
    }

    /**
     * Constructor allowing orientation override
     * 
     * @param context
     *            Context
     * @param orientation
     *            Layout orientation, can be vartical or horizontal
     */
    public QuickAction(final Context context, final int orientation) {
        super(context);

        mOrientation = orientation;

        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (mOrientation == HORIZONTAL) {
            setRootViewId(R.layout.popup_horizontal);
        } else {
            setRootViewId(R.layout.popup_vertical);
        }

        mAnimStyle = ANIM_AUTO;
        mChildPos = 0;
    }

    /**
     * Add action item
     * 
     * @param action
     *            {@link ActionItem}
     */
    public void addActionItem(final ActionItem action) {
        actionItems.add(action);

        final String title = action.getTitle();
        final Drawable icon = action.getIcon();

        View container;

        if (mOrientation == HORIZONTAL) {
            container = mInflater
                    .inflate(R.layout.action_item_horizontal, null);
        } else {
            container = mInflater.inflate(R.layout.action_item_vertical, null);
        }

        final ImageView img = (ImageView) container.findViewById(R.id.iv_icon);
        final TextView text = (TextView) container.findViewById(R.id.tv_title);

        if (icon != null) {
            img.setImageDrawable(icon);
        } else {
            img.setVisibility(View.GONE);
        }

        if (title != null) {
            text.setText(title);
        } else {
            text.setVisibility(View.GONE);
        }

        final int pos = mChildPos;
        final int actionId = action.getActionId();

        container.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(QuickAction.this, pos,
                            actionId);
                }

                if (!getActionItem(pos).isSticky()) {
                    mDidAction = true;

                    dismiss();
                }
            }
        });

        container.setFocusable(true);
        container.setClickable(true);

        // if (mOrientation == HORIZONTAL && mChildPos != 0) {
        // View separator = mInflater.inflate(R.layout.horiz_separator, null);
        //
        // RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        // LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
        //
        // separator.setLayoutParams(params);
        // separator.setPadding(5, 0, 5, 0);
        //
        // mTrack.addView(separator, mInsertPos);
        //
        // mInsertPos++;
        // }

        mTrack.addView(container, mInsertPos);

        mChildPos++;
        mInsertPos++;
    }

    /**
     * Get action item at an index
     * 
     * @param index
     *            Index of item (position from callback)
     * 
     * @return Action Item at the position
     */
    public ActionItem getActionItem(final int index) {
        return actionItems.get(index);
    }

    @Override
    public void onDismiss() {
        if (!mDidAction && (mDismissListener != null)) {
            mDismissListener.onDismiss();
        }
    }

    /**
     * Set animation style
     * 
     * @param screenWidth
     *            screen width
     * @param requestedX
     *            distance from left edge
     * @param onTop
     *            flag to indicate where the popup should be displayed. Set TRUE
     *            if displayed on top of anchor view and vice versa
     */
    private void setAnimationStyle(final int screenWidth, final int requestedX,
            final boolean onTop) {
        final int arrowPos = requestedX - (mArrowUp.getMeasuredWidth() / 2);

        switch (mAnimStyle) {
        case ANIM_GROW_FROM_LEFT:
            mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left
                    : R.style.Animations_PopDownMenu_Left);
            break;

        case ANIM_GROW_FROM_RIGHT:
            mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right
                    : R.style.Animations_PopDownMenu_Right);
            break;

        case ANIM_GROW_FROM_CENTER:
            mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center
                    : R.style.Animations_PopDownMenu_Center);
            break;

        case ANIM_REFLECT:
            mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Reflect
                    : R.style.Animations_PopDownMenu_Reflect);
            break;

        case ANIM_AUTO:
            if (arrowPos <= (screenWidth / 4)) {
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left
                        : R.style.Animations_PopDownMenu_Left);
            } else if ((arrowPos > (screenWidth / 4))
                    && (arrowPos < (3 * (screenWidth / 4)))) {
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center
                        : R.style.Animations_PopDownMenu_Center);
            } else {
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right
                        : R.style.Animations_PopDownMenu_Right);
            }

            break;
        }
    }

    /**
     * Set animation style
     * 
     * @param mAnimStyle
     *            animation style, default is set to ANIM_AUTO
     */
    public void setAnimStyle(final int mAnimStyle) {
        this.mAnimStyle = mAnimStyle;
    }

    /**
     * Set listener for action item clicked.
     * 
     * @param listener
     *            Listener
     */
    public void setOnActionItemClickListener(
            final OnActionItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * Set listener for window dismissed. This listener will only be fired if
     * the quicakction dialog is dismissed by clicking outside the dialog or
     * clicking on sticky item.
     */
    public void setOnDismissListener(
            final QuickAction.OnDismissListener listener) {
        setOnDismissListener(this);

        mDismissListener = listener;
    }

    /**
     * Set root view.
     * 
     * @param id
     *            Layout resource id
     */
    public void setRootViewId(final int id) {
        mRootView = mInflater.inflate(id, null);
        mTrack = (ViewGroup) mRootView.findViewById(R.id.tracks);

        mArrowDown = (ImageView) mRootView.findViewById(R.id.arrow_down);
        mArrowUp = (ImageView) mRootView.findViewById(R.id.arrow_up);

        mScroller = (ScrollView) mRootView.findViewById(R.id.scroller);

        // This was previously defined on show() method, moved here to prevent
        // force close that occured
        // when tapping fastly on a view to show quickaction dialog.
        // Thanx to zammbi (github.com/zammbi)
        mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        setContentView(mRootView);
    }

    /**
     * Show quickaction popup. Popup is automatically positioned, on top or
     * bottom of anchor view.
     * 
     */
    public void show(final View anchor) {
        preShow();

        int xPos, yPos, arrowPos;

        mDidAction = false;

        final int[] location = new int[2];

        anchor.getLocationOnScreen(location);

        final Rect anchorRect = new Rect(location[0], location[1], location[0]
                + anchor.getWidth(), location[1] + anchor.getHeight());

        // mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        // LayoutParams.WRAP_CONTENT));

        mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        final int rootHeight = mRootView.getMeasuredHeight();

        if (rootWidth == 0) {
            rootWidth = mRootView.getMeasuredWidth();
        }

        final int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
        final int screenHeight = mWindowManager.getDefaultDisplay().getHeight();

        // automatically get X coord of popup (top left)
        if ((anchorRect.left + rootWidth) > screenWidth) {
            xPos = anchorRect.left - (rootWidth - anchor.getWidth());
            xPos = (xPos < 0) ? 0 : xPos;

            arrowPos = anchorRect.centerX() - xPos;

        } else {
            if (anchor.getWidth() > rootWidth) {
                xPos = anchorRect.centerX() - (rootWidth / 2);
            } else {
                xPos = anchorRect.left;
            }

            arrowPos = anchorRect.centerX() - xPos;
        }

        final int dyTop = anchorRect.top;
        final int dyBottom = screenHeight - anchorRect.bottom;

        final boolean onTop = (dyTop > dyBottom) ? true : false;

        if (onTop) {
            if (rootHeight > dyTop) {
                yPos = 15;
                final LayoutParams l = mScroller.getLayoutParams();
                l.height = dyTop - anchor.getHeight();
            } else {
                yPos = anchorRect.top - rootHeight;
            }
            // add by mcxiaoke
            yPos += anchorRect.height() / 3;
        } else {
            yPos = anchorRect.bottom;

            if (rootHeight > dyBottom) {
                final LayoutParams l = mScroller.getLayoutParams();
                l.height = dyBottom;
            }
            // add by mcxiaoke
            yPos -= anchorRect.height() / 3;
        }

        showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);

        setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

        mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
    }

    /**
     * Show arrow
     * 
     * @param whichArrow
     *            arrow type resource id
     * @param requestedX
     *            distance from left screen
     */
    private void showArrow(final int whichArrow, final int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp
                : mArrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown
                : mArrowUp;

        final int arrowWidth = mArrowUp.getMeasuredWidth();

        showArrow.setVisibility(View.VISIBLE);

        final ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow
                .getLayoutParams();

        param.leftMargin = requestedX - (arrowWidth / 2);

        hideArrow.setVisibility(View.INVISIBLE);
    }

    /**
     * Show quickaction popup. Popup is automatically positioned, on top or
     * bottom of anchor view.
     * 
     */
    public void showOriginal(final View anchor) {
        preShow();

        int xPos, yPos, arrowPos;

        mDidAction = false;

        final int[] location = new int[2];

        anchor.getLocationOnScreen(location);

        final Rect anchorRect = new Rect(location[0], location[1], location[0]
                + anchor.getWidth(), location[1] + anchor.getHeight());

        // mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        // LayoutParams.WRAP_CONTENT));

        mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        final int rootHeight = mRootView.getMeasuredHeight();

        if (rootWidth == 0) {
            rootWidth = mRootView.getMeasuredWidth();
        }

        final int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
        final int screenHeight = mWindowManager.getDefaultDisplay().getHeight();

        // automatically get X coord of popup (top left)
        if ((anchorRect.left + rootWidth) > screenWidth) {
            xPos = anchorRect.left - (rootWidth - anchor.getWidth());
            xPos = (xPos < 0) ? 0 : xPos;

            arrowPos = anchorRect.centerX() - xPos;

        } else {
            if (anchor.getWidth() > rootWidth) {
                xPos = anchorRect.centerX() - (rootWidth / 2);
            } else {
                xPos = anchorRect.left;
            }

            arrowPos = anchorRect.centerX() - xPos;
        }

        final int dyTop = anchorRect.top;
        final int dyBottom = screenHeight - anchorRect.bottom;

        final boolean onTop = (dyTop > dyBottom) ? true : false;

        if (onTop) {
            if (rootHeight > dyTop) {
                yPos = 15;
                final LayoutParams l = mScroller.getLayoutParams();
                l.height = dyTop - anchor.getHeight();
            } else {
                yPos = anchorRect.top - rootHeight;
            }
        } else {
            yPos = anchorRect.bottom;

            if (rootHeight > dyBottom) {
                final LayoutParams l = mScroller.getLayoutParams();
                l.height = dyBottom;
            }
        }

        showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);

        setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

        mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
    }
}
