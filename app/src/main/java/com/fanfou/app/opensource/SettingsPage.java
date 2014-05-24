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
package com.fanfou.app.opensource;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.fanfou.app.opensource.preferences.SeekBarPreference;
import com.fanfou.app.opensource.preferences.colorpicker.ColorPickerPreference;
import com.fanfou.app.opensource.service.DownloadService;
import com.fanfou.app.opensource.service.NotificationService;
import com.fanfou.app.opensource.update.AppVersionInfo;
import com.fanfou.app.opensource.util.CommonHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.11
 * @version 1.1 2011.10.25
 * @version 1.5 2011.11.10
 * @version 1.6 2011.11.16
 * @version 1.7 2011.11.25
 * @version 2.0 2011.12.02
 * @version 2.1 2011.12.05
 * @version 2.2 2011.12.13
 * 
 */
public class SettingsPage extends PreferenceActivity implements
        OnPreferenceClickListener, OnSharedPreferenceChangeListener,
        OnPreferenceChangeListener {
    private static class CheckTask extends
            AsyncTask<Void, Void, AppVersionInfo> {
        private final Context c;
        private final ProgressDialog pd;

        public CheckTask(final Context context) {
            this.c = context;
            this.pd = new ProgressDialog(this.c);
            if (AppContext.DEBUG) {
                Log.i(SettingsPage.TAG, "CheckTask init");
            }
        }

        @Override
        protected AppVersionInfo doInBackground(final Void... params) {
            final AppVersionInfo info = DownloadService.fetchVersionInfo();
            if (AppContext.DEBUG) {
                Log.d(SettingsPage.TAG, "doInBackground " + info);
            }
            return info;
        }

        @Override
        protected void onPostExecute(final AppVersionInfo info) {
            this.pd.dismiss();
            if (AppContext.DEBUG) {
                if (info != null) {
                    DownloadService.showUpdateConfirmDialog(this.c, info);
                }
                return;
            }
            if ((info != null)
                    && (info.versionCode > AppContext.appVersionCode)) {
                DownloadService.showUpdateConfirmDialog(this.c, info);
            } else {
                CommonHelper.notify(this.c, "你使用的已经是最新版");
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.pd.setTitle("检测更新");
            this.pd.setMessage("正在检测新版本...");
            this.pd.setIndeterminate(true);
            this.pd.show();
        }
    }

    public static final String TAG = "OptionsPage";

    private boolean needRestart = false;

    private void checkUpdate() {
        if (AppContext.DEBUG) {
            Log.d(SettingsPage.TAG, "checkUpdate");
        }
        if (AppContext.noConnection) {
            CommonHelper.notify(this, "无网络连接，请稍后重试");
            return;
        }
        new CheckTask(this).execute();
    }

    @Override
    public void finish() {
        super.finish();
        if (this.needRestart) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonHelper.initScreenConfig(this);

        addPreferencesFromResource(R.xml.options);

        final ListPreference interval = (ListPreference) findPreference(getText(R.string.option_notification_interval));
        interval.setSummary(interval.getEntry());

        final Preference currentAccount = findPreference(getText(R.string.option_current_account));
        currentAccount.setSummary("" + AppContext.getUserName() + "("
                + AppContext.getUserId() + ")");

        final Preference checkUpdate = findPreference(getText(R.string.option_check_update));
        checkUpdate.setOnPreferenceClickListener(this);

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        AppContext.active = false;
        super.onPause();
    }

    @Override
    public boolean onPreferenceChange(final Preference preference,
            final Object newValue) {
        if (getString(R.string.option_fontsize).equals(preference.getKey())) {
            this.needRestart = true;
        } else if (getString(R.string.option_text_mode).equals(
                preference.getKey())) {
            this.needRestart = true;
        } else if (getString(R.string.option_page_scroll_endless).equals(
                preference.getKey())) {
            this.needRestart = true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {
        if (AppContext.DEBUG) {
            Log.d(SettingsPage.TAG,
                    "onPreferenceClick key=" + preference.getKey());
        }
        if (preference.getKey().equals(getString(R.string.option_check_update))) {
            checkUpdate();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppContext.active = true;
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sp,
            final String key) {
        final Preference p = findPreference(key);
        if (key.equals(getString(R.string.option_notification))) {
            final CheckBoxPreference cp = (CheckBoxPreference) p;
            NotificationService.set(this, cp.isChecked());
        } else if (key.equals(getString(R.string.option_notification_interval))) {
            NotificationService.set(this);
        } else if (key.equals(getString(R.string.option_autoupdate))) {
        } else if (key.equals(getString(R.string.option_force_portrait))) {
            final CheckBoxPreference cp = (CheckBoxPreference) p;
            if (cp.isChecked()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
            this.needRestart = true;
        } else if (key.equals(getString(R.string.option_fontsize))) {
            final SeekBarPreference skp = (SeekBarPreference) p;
            final int value = sp.getInt(key,
                    getResources().getInteger(R.integer.defaultFontSize));
            skp.setSummary(value + "号");
            this.needRestart = true;
        } else if (key
                .equals(getString(R.string.option_color_highlight_mention))
                || key.equals(getString(R.string.option_color_highlight_self))) {
            final ColorPickerPreference cp = (ColorPickerPreference) p;
            cp.setPreviewColor();
            this.needRestart = true;
        } else if (p instanceof ListPreference) {
            final ListPreference lp = (ListPreference) p;
            lp.setSummary(lp.getEntry());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
