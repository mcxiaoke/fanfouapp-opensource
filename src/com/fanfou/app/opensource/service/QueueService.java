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
package com.fanfou.app.opensource.service;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.WritePage;
import com.fanfou.app.opensource.api.ApiClient;
import com.fanfou.app.opensource.api.ApiException;
import com.fanfou.app.opensource.api.bean.Draft;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.db.Contents.DraftInfo;
import com.fanfou.app.opensource.util.ImageHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.28
 * @version 1.1 2011.11.02
 * @version 2.0 2011.11.15
 * @version 3.0 2011.11.18
 * @version 3.1 2011.11.22
 * @version 3.2 2011.11.28
 * @version 3.3 2011.12.05
 * @version 3.4 2011.12.13
 * @version 3.4 2011.12.26
 * 
 */
public class QueueService extends BaseIntentService {
    private static final String TAG = QueueService.class.getSimpleName();

    public static void start(final Context context) {
        context.startService(new Intent(context, QueueService.class));
    }

    public QueueService() {
        super("TaskQueueService");
    }

    private void deleteDraft(final int id) {
        if (id > -1) {
            final Uri uri = ContentUris.withAppendedId(DraftInfo.CONTENT_URI,
                    id);
            getContentResolver().delete(uri, null, null);
        }
    }

    private boolean doSend(final Draft d) {
        boolean res = false;
        try {
            final ApiClient api = AppContext.getApiClient();
            Status result = null;
            final File srcFile = new File(d.filePath);
            if ((srcFile == null) || !srcFile.exists()) {
                if (d.type == WritePage.TYPE_REPLY) {
                    result = api.statusesCreate(d.text, d.replyTo, null, null,
                            null, Constants.FORMAT, Constants.MODE);
                } else {
                    result = api.statusesCreate(d.text, null, null, null,
                            d.replyTo, Constants.FORMAT, Constants.MODE);
                }
            } else {
                int quality;
                if (AppContext.isWifi()) {
                    quality = ImageHelper.IMAGE_QUALITY_HIGH;
                } else {
                    quality = ImageHelper.IMAGE_QUALITY_LOW;
                }
                final File photo = ImageHelper.prepareUploadFile(this, srcFile,
                        quality);
                if ((photo != null) && (photo.length() > 0)) {
                    if (AppContext.DEBUG) {
                        log("photo file=" + srcFile.getName() + " size="
                                + (photo.length() / 1024) + " quality="
                                + quality);
                    }
                    result = api.photosUpload(photo, d.text, null, null,
                            Constants.FORMAT, Constants.MODE);
                    photo.delete();
                }
            }
            if ((result != null) && !result.isNull()) {
                // IOHelper.storeStatus(this, result);
                res = true;
            }
        } catch (final ApiException e) {
            if (AppContext.DEBUG) {
                Log.e(QueueService.TAG, "error: code=" + e.statusCode + " msg="
                        + e.getMessage());
            }
        }
        return res;
    }

    private void log(final String message) {
        Log.d(QueueService.TAG, message);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        sendQueue();
    }

    private void sendQueue() {
        final BlockingQueue<Draft> queue = new LinkedBlockingQueue<Draft>();
        boolean running = true;
        final Cursor cursor = getContentResolver().query(DraftInfo.CONTENT_URI,
                DraftInfo.COLUMNS, null, null, null);
        if ((cursor != null) && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                final Draft d = Draft.parse(cursor);
                if (d != null) {
                    queue.add(d);
                }
                cursor.moveToNext();
            }
        }

        int nums = 0;
        while (running) {
            final Draft d = queue.poll();
            if (d != null) {
                if (AppContext.DEBUG) {
                    log("Start sending draft: text=" + d.text + " file="
                            + d.filePath);
                }
                if (doSend(d)) {
                    deleteDraft(d.id);
                    nums++;
                    if (AppContext.DEBUG) {
                        log("Send draft successful: id=" + d.id + " text="
                                + d.text + " filepath=" + d.filePath);
                    }
                }
            } else {
                running = false;
            }
        }
        if (nums > 0) {
            sendSuccessBroadcast();
        }
    }

    private void sendSuccessBroadcast() {
        final Intent intent = new Intent(Constants.ACTION_STATUS_SENT);
        intent.setPackage(getPackageName());
        sendOrderedBroadcast(intent, null);
    }

}
