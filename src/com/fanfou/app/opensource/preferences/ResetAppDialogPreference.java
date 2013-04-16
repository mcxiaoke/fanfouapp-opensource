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

import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.OptionHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.28
 * 
 */
public class ResetAppDialogPreference extends DialogPreference {

    private static class CleanTask extends AsyncTask<Void, Void, Boolean> {
        private final Context context;
        private ProgressDialog pd = null;

        public CleanTask(final Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
            // AppContext.handleReset(context);
            OptionHelper.clearSettings();
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            super.onPostExecute(result);
            this.pd.dismiss();
            if (result.booleanValue() == true) {
                CommonHelper.notify(this.context, "设置已恢复为默认状态");
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.pd = new ProgressDialog(this.context);
            this.pd.setTitle("清除设置");
            this.pd.setMessage("正在清除所有个性化设置...");
            this.pd.setCancelable(false);
            this.pd.setIndeterminate(true);
            this.pd.show();
        }
    }

    public ResetAppDialogPreference(final Context context,
            final AttributeSet attrs) {
        super(context, attrs);
    }

    public ResetAppDialogPreference(final Context context,
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
