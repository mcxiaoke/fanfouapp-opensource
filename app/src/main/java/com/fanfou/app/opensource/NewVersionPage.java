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
import android.content.Intent;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.service.DownloadService;
import com.fanfou.app.opensource.ui.ActionBar;
import com.fanfou.app.opensource.update.AppVersionInfo;
import com.fanfou.app.opensource.util.CommonHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.25
 * @version 2.0 2011.10.27
 * @version 2.5 2011.10.31
 * 
 */
public class NewVersionPage extends Activity implements View.OnClickListener {
    private static String buildText(final AppVersionInfo info) {
        if (info == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("\n最新版本： ").append(info.versionName).append("(Build")
                .append(info.versionCode).append(")");
        sb.append("\n更新日期：").append(info.releaseDate);
        sb.append("\n更新级别：").append(info.forceUpdate ? "重要更新" : "一般更新");
        sb.append("\n\n更新内容：\n").append(info.changelog);
        return sb.toString();
    }

    private TextView mTitleView;
    private TextView mTextView;
    private Button mButton1;
    private Button mButton2;

    private ActionBar mBar;

    private AppVersionInfo mVersionInfo;

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        switch (id) {
        case R.id.button1:
            if (this.mVersionInfo != null) {
                DownloadService.startDownload(this, this.mVersionInfo);
            }
            finish();
            break;
        case R.id.button2:
            finish();
            break;
        default:
            break;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonHelper.initScreenConfig(this);
        parseIntent(getIntent());
        setLayout();
        updateUI();

    }

    private void parseIntent(final Intent intent) {
        this.mVersionInfo = intent.getParcelableExtra(Constants.EXTRA_DATA);
    }

    private void setActionBar() {
        this.mBar = (ActionBar) findViewById(R.id.actionbar);
        this.mBar.setLeftAction(new ActionBar.BackAction(this));
        // mBar.setTitle("饭否版本升级");
    }

    protected void setBlurEffect() {
        final Window window = getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        // lp.alpha=0.8f;
        lp.dimAmount = 0.6f;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        // window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
    }

    private void setLayout() {
        setContentView(R.layout.newversion);

        setActionBar();

        this.mTitleView = (TextView) findViewById(R.id.title);
        final TextPaint tp = this.mTitleView.getPaint();
        tp.setFakeBoldText(true);

        this.mTextView = (TextView) findViewById(R.id.text);

        this.mButton1 = (Button) findViewById(R.id.button1);
        this.mButton1.setText("立即升级");
        this.mButton1.setOnClickListener(this);

        this.mButton2 = (Button) findViewById(R.id.button2);
        this.mButton2.setText("以后再说");
        this.mButton2.setOnClickListener(this);

    }

    private void updateUI() {
        this.mTitleView.setText("发现新版本，是否升级？");
        this.mTextView.setText(NewVersionPage.buildText(this.mVersionInfo));
    }

}
