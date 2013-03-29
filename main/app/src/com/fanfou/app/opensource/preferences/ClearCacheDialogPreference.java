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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.AttributeSet;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.util.IOHelper;

public class ClearCacheDialogPreference extends DialogPreference {

    private static class CleanTask extends AsyncTask<Void, Void, Boolean> {
        private final Context c;
        private ProgressDialog pd = null;

        public CleanTask(final Context context) {
            this.c = context;
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
            try {
                Thread.sleep(300);
                IOHelper.ClearBigPictures(this.c);
                return true;
            } catch (final InterruptedException e) {
                if (AppContext.DEBUG) {
                    e.printStackTrace();
                }
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            super.onPostExecute(result);
            this.pd.dismiss();
            if (result.booleanValue() == true) {
                // Utils.notify(c, "缓存图片已清空");
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.pd = new ProgressDialog(this.c);
            this.pd.setTitle("清空缓存图片");
            this.pd.setMessage("正在清空缓存图片...");
            this.pd.setCancelable(false);
            this.pd.setIndeterminate(true);
            this.pd.show();
        }
    }

    public ClearCacheDialogPreference(final Context context,
            final AttributeSet attrs) {
        super(context, attrs);
    }

    public ClearCacheDialogPreference(final Context context,
            final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        super.onClick(dialog, which);
        if (which == DialogInterface.BUTTON_POSITIVE) {
            new CleanTask(this.context).execute();
        }
    }

}
