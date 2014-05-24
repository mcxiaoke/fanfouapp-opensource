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

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Action item, displayed as menu with icon and text.
 * 
 * @author Lorensius. W. L. T <lorenz@londatiga.net>
 * 
 *         Contributors: - Kevin Peck <kevinwpeck@gmail.com>
 * 
 */
public class ActionItem {
    private Drawable icon;
    private Bitmap thumb;
    private String title;
    private int actionId = -1;
    private boolean selected;
    private boolean sticky;

    /**
     * Constructor
     */
    public ActionItem() {
        this(-1, null, null);
    }

    /**
     * Constructor
     * 
     * @param icon
     *            {@link Drawable} action icon
     */
    public ActionItem(final Drawable icon) {
        this(-1, null, icon);
    }

    /**
     * Constructor
     * 
     * @param actionId
     *            Action ID of item
     * @param icon
     *            {@link Drawable} action icon
     */
    public ActionItem(final int actionId, final Drawable icon) {
        this(actionId, null, icon);
    }

    /**
     * Constructor
     * 
     * @param actionId
     *            Action id of the item
     * @param title
     *            Text to show for the item
     */
    public ActionItem(final int actionId, final String title) {
        this(actionId, title, null);
    }

    /**
     * Constructor
     * 
     * @param actionId
     *            Action id for case statements
     * @param title
     *            Title
     * @param icon
     *            Icon to use
     */
    public ActionItem(final int actionId, final String title,
            final Drawable icon) {
        this.title = title;
        this.icon = icon;
        this.actionId = actionId;
    }

    /**
     * @return Our action id
     */
    public int getActionId() {
        return actionId;
    }

    /**
     * Get action icon
     * 
     * @return {@link Drawable} action icon
     */
    public Drawable getIcon() {
        return this.icon;
    }

    /**
     * Get thumb image
     * 
     * @return Thumb image
     */
    public Bitmap getThumb() {
        return this.thumb;
    }

    /**
     * Get action title
     * 
     * @return action title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Check if item is selected
     * 
     * @return true or false
     */
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * @return true if button is sticky, menu stays visible after press
     */
    public boolean isSticky() {
        return sticky;
    }

    /**
     * Set action id
     * 
     * @param actionId
     *            Action id for this action
     */
    public void setActionId(final int actionId) {
        this.actionId = actionId;
    }

    /**
     * Set action icon
     * 
     * @param icon
     *            {@link Drawable} action icon
     */
    public void setIcon(final Drawable icon) {
        this.icon = icon;
    }

    /**
     * Set selected flag;
     * 
     * @param selected
     *            Flag to indicate the item is selected
     */
    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    /**
     * Set sticky status of button
     * 
     * @param sticky
     *            true for sticky, pop up sends event but does not disappear
     */
    public void setSticky(final boolean sticky) {
        this.sticky = sticky;
    }

    /**
     * Set thumb
     * 
     * @param thumb
     *            Thumb image
     */
    public void setThumb(final Bitmap thumb) {
        this.thumb = thumb;
    }

    /**
     * Set action title
     * 
     * @param title
     *            action title
     */
    public void setTitle(final String title) {
        this.title = title;
    }
}
