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
package com.fanfou.app.opensource.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.SplashPage;
import com.fanfou.app.opensource.service.Constants;

/**
 * @author mcxiaoke
 * @version 1.0 2011.11.08
 * @version 1.5 2011.11.11
 * @version 1.6 2011.12.19
 * 
 */
public class WidgetSmall extends AppWidgetProvider {

    private PendingIntent getCameraPendingIntent(final Context context) {
        final Intent intent = new Intent(Constants.ACTION_SEND_FROM_CAMERA);
        intent.setPackage(context.getPackageName());
        return getPendingIntent(context, intent);
    }

    private PendingIntent getGalleryPendingIntent(final Context context) {
        final Intent intent = new Intent(Constants.ACTION_SEND_FROM_GALLERY);
        intent.setPackage(context.getPackageName());
        return getPendingIntent(context, intent);
    }

    private PendingIntent getPendingIntent(final Context context,
            final Intent intent) {
        final PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
                0);
        return pi;
    }

    private PendingIntent getSplashPendingIntent(final Context context) {
        final Intent intent = new Intent(context, SplashPage.class);
        return getPendingIntent(context, intent);
    }

    private PendingIntent getWritePendingIntent(final Context context) {
        final Intent intent = new Intent(Constants.ACTION_SEND);
        intent.setPackage(context.getPackageName());
        return getPendingIntent(context, intent);
    }

    @Override
    public void onUpdate(final Context context,
            final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        final RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.widget_small);
        views.setOnClickPendingIntent(R.id.widget_home,
                getSplashPendingIntent(context));
        views.setOnClickPendingIntent(R.id.widget_write,
                getWritePendingIntent(context));
        views.setOnClickPendingIntent(R.id.widget_gallery,
                getGalleryPendingIntent(context));
        views.setOnClickPendingIntent(R.id.widget_camera,
                getCameraPendingIntent(context));
        appWidgetManager.updateAppWidget(appWidgetIds, views);

    }

}
