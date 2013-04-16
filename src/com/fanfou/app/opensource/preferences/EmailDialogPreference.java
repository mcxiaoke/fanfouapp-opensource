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
package com.fanfou.app.opensource.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class EmailDialogPreference extends DialogPreference {
    Context c;
    String version;

    public EmailDialogPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.c = context;
    }

    public EmailDialogPreference(final Context context,
            final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        this.c = context;
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        super.onClick(dialog, which);
        //
        // if (which == DialogInterface.BUTTON_POSITIVE) {
        // SmsPopupUtils.launchEmailToIntent(c, c.getString(R.string.app_name) +
        // version, true);
        // } else if (which == DialogInterface.BUTTON_NEGATIVE) {
        // SmsPopupUtils.launchEmailToIntent(c, c.getString(R.string.app_name) +
        // version, false);
        // }
    }

    public void setVersion(final String v) {
        this.version = v;
    }
}
