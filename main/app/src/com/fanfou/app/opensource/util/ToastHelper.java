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
package com.fanfou.app.opensource.util;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.fanfou.app.opensource.util.support.AppMsg;

/**
 * toast 显示工具类
 * 
 * @author jianhang
 * 
 */
public class ToastHelper {
    public static void show(final Context context, final int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();

    }

    public static void showAuthorizationErrorToast(final Activity context,
            final String text) {
        AppMsg.makeText(context, text, AppMsg.STYLE_ALERT)
                .setLayoutGravity(Gravity.CENTER).show();
    }

    public static void showErrorToast(final Activity context, final String text) {
        AppMsg.makeText(context, text, AppMsg.STYLE_ALERT)
                .setLayoutGravity(Gravity.BOTTOM).show();
    }

    public static void showInfoToast(final Activity context, final String text) {
        AppMsg.makeText(context, text, AppMsg.STYLE_INFO)
                .setLayoutGravity(Gravity.BOTTOM).show();
    }
}
