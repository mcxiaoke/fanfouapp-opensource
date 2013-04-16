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
package com.fanfou.app.opensource.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.28
 * 
 */
public class ScrollingTextView extends TextView {

    public ScrollingTextView(final Context context) {
        super(context);
        init();
    }

    public ScrollingTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollingTextView(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        setEllipsize(TruncateAt.MARQUEE);
        setGravity(Gravity.CENTER);
        setSingleLine();
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    protected void onFocusChanged(final boolean focused, final int direction,
            final Rect previouslyFocusedRect) {
        if (focused) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
        }
    }

    @Override
    public void onWindowFocusChanged(final boolean focused) {
        if (focused) {
            super.onWindowFocusChanged(focused);
        }
    }
}
