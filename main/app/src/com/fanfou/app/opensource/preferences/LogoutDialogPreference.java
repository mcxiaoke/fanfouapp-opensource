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
import android.util.AttributeSet;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.util.IntentHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.09.10
 * @version 1.1 2011.10.25
 * 
 */
public class LogoutDialogPreference extends DialogPreference {

    public LogoutDialogPreference(final Context context,
            final AttributeSet attrs) {
        super(context, attrs);
        setSummary("当前登录帐号:" + AppContext.getUserName() + "("
                + AppContext.getUserId() + ")");
    }

    private void doLogout() {
        IntentHelper.goLoginPage(this.context);
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        super.onClick(dialog, which);
        if (which == DialogInterface.BUTTON_POSITIVE) {
            doLogout();
        }
    }
}
