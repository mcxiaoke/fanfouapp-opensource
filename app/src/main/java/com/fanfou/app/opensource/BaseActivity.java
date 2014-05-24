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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.IntentHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.30
 * @version 2.0 2011.09.25
 * @version 2.1 2011.10.19
 * @version 2.1 2011.10.25
 * @version 2.2 2011.10.27
 * @version 2.3 2011.11.07
 * @version 2.4 2011.11.11
 * @version 2.5 2011.11.15
 * @version 2.6 2011.11.22
 * @version 2.7 2011.12.07
 * @version 3.0 2013.03.09
 * 
 */
abstract class BaseActivity extends Activity implements OnClickListener {

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (AppContext.DEBUG) {
                Log.d("NotificationReceiver", "active, broadcast received: "
                        + intent.toString());
            }
            if (onBroadcastReceived(intent)) {
                abortBroadcast();
            }
        }

    }

    public static final int STATE_INIT = 0;
    public static final int STATE_NORMAL = 1;

    public static final int STATE_EMPTY = 2;
    protected BaseActivity mContext;
    protected LayoutInflater mInflater;

    protected boolean isActive = false;

    protected DisplayMetrics mDisplayMetrics;
    private BroadcastReceiver mBroadcastReceiver;

    private IntentFilter mIntentFilter;

    protected static final int PAGE_NORMAL = 0;

    protected static final int PAGE_HOME = 1;

    protected static final int PAGE_LOGIN = 2;

    protected static final int PAGE_STATUS = 3;

    protected static final int PAGE_USER = 4;;

    protected static final int PAGE_TIMELINE = 5;

    protected static final int PAGE_FRIENDS = 6;

    protected static final int PAGE_FOLLOWERS = 7;

    protected static final int PAGE_DRAFTS = 8;

    protected static final int PAGE_FILTERS = 9;

    protected static final int MENU_ID_HOME = 0;

    protected static final int MENU_ID_SAVE = 1;

    protected static final int MENU_ID_CLEAR = 2;

    protected IntentFilter getIntentFilter() {
        return new IntentFilter();
    }

    protected int getPageType() {
        return BaseActivity.PAGE_NORMAL;
    }

    private void initialize() {
        this.mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(this.mDisplayMetrics);
    }

    private void initReceiver() {
        this.mBroadcastReceiver = new MyBroadcastReceiver();
        this.mIntentFilter = getIntentFilter();
        this.mIntentFilter.setPriority(1000);
    }

    protected boolean isActive() {
        return this.isActive;
    }

    protected boolean isHomeScreen() {
        return false;
    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected boolean onBroadcastReceived(final Intent intent) {
        return true;
    }

    @Override
    public void onClick(final View v) {
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonHelper.initScreenConfig(this);

        this.mContext = this;
        this.mInflater = LayoutInflater.from(this);

        initialize();
        initReceiver();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuItem home = menu.add(0, BaseActivity.MENU_ID_HOME,
                BaseActivity.MENU_ID_HOME, "返回首页");
        home.setIcon(R.drawable.ic_menu_home);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onMenuHomeClick() {
        IntentHelper.goHomePage(this, -1);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == BaseActivity.MENU_ID_HOME) {
            onMenuHomeClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        AppContext.active = this.isActive = false;
        unregisterReceiver(this.mBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppContext.active = this.isActive = true;
        registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
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
