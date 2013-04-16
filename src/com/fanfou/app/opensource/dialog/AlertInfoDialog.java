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
package com.fanfou.app.opensource.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.fanfou.app.opensource.R;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.26
 * 
 */
public class AlertInfoDialog extends Dialog implements View.OnClickListener {

    public static interface OnOKClickListener {
        public void onOKClick();
    }

    private final Context mContext;
    private TextView mTitleView;
    private TextView mTextView;

    private Button mButtonOk;
    private CharSequence mTitle;

    private CharSequence mText;

    private OnOKClickListener mClickListener;

    public AlertInfoDialog(final Context context, final String title,
            final String text) {
        super(context, R.style.Dialog);
        this.mContext = context;
        this.mTitle = title;
        this.mText = text;
    }

    private void init() {
        setContentView(R.layout.dialog_alert);

        this.mTitleView = (TextView) findViewById(R.id.title);
        final TextPaint tp = this.mTitleView.getPaint();
        tp.setFakeBoldText(true);
        this.mTitleView.setText(this.mTitle);

        this.mTextView = (TextView) findViewById(R.id.text);
        this.mTextView.setText(this.mText);

        this.mButtonOk = (Button) findViewById(R.id.button_ok);
        this.mButtonOk.setOnClickListener(this);

    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        switch (id) {
        case R.id.button_ok:
            cancel();
            if (this.mClickListener != null) {
                this.mClickListener.onOKClick();
            }
            break;
        default:
            break;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBlurEffect();
        init();
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

    public void setMessage(final CharSequence message) {
        this.mText = message;
        this.mTextView.setText(this.mText);
    }

    public void setMessage(final int resId) {
        this.mText = this.mContext.getResources().getText(resId);
        this.mTextView.setText(this.mText);
    }

    public void setOnClickListener(final OnOKClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    @Override
    public void setTitle(final CharSequence title) {
        this.mTitle = title;
        this.mTitleView.setText(this.mTitle);
    }

    @Override
    public void setTitle(final int resId) {
        this.mTitle = this.mContext.getResources().getText(resId);
        this.mTitleView.setText(this.mTitle);
    }

}
