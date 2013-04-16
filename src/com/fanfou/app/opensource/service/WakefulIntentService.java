/***
  Copyright (c) 2009-11 CommonsWare, LLC
  
  Licensed under the Apache License, Version 2.0 (the "License"); you may
  not use this file except in compliance with the License. You may obtain
  a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.fanfou.app.opensource.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

abstract public class WakefulIntentService extends IntentService {
    static final String NAME = "com.fanfou.app.service.WakefulIntentService";

    private static volatile PowerManager.WakeLock lockStatic = null;

    synchronized private static PowerManager.WakeLock getLock(
            final Context context) {
        if (WakefulIntentService.lockStatic == null) {
            final PowerManager mgr = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);

            WakefulIntentService.lockStatic = mgr.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, WakefulIntentService.NAME);
            WakefulIntentService.lockStatic.setReferenceCounted(true);
        }
        return (WakefulIntentService.lockStatic);
    }

    public static void sendWakefulWork(final Context ctxt,
            final Class<?> clsService) {
        WakefulIntentService
                .sendWakefulWork(ctxt, new Intent(ctxt, clsService));
    }

    public static void sendWakefulWork(final Context ctxt, final Intent i) {
        WakefulIntentService.getLock(ctxt.getApplicationContext()).acquire();
        ctxt.startService(i);
    }

    public WakefulIntentService(final String name) {
        super(name);
    }

    abstract protected void doWakefulWork(Intent intent);

    @Override
    final protected void onHandleIntent(final Intent intent) {
        try {
            doWakefulWork(intent);
        } finally {
            WakefulIntentService.getLock(this.getApplicationContext())
                    .release();
        }
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags,
            final int startId) {
        WakefulIntentService.getLock(this.getApplicationContext()).acquire();
        super.onStartCommand(intent, flags, startId);
        return (Service.START_NOT_STICKY);
    }
}
