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
 * @version 1.0 2011.10.25
 * @version 2.0 2011.10.27
 * 
 */
public class ConfirmDialog extends Dialog implements View.OnClickListener {

    public abstract static class AbstractClickHandler implements ClickHandler {

        @Override
        public void onButton1Click() {
        }

        @Override
        public void onButton2Click() {
        }

    }

    public static interface ClickHandler {
        public void onButton1Click();

        public void onButton2Click();
    }

    private final Context mContext;
    private TextView mTitleView;
    private TextView mTextView;

    private Button mButton1;
    private Button mButton2;

    private CharSequence mTitle;

    private CharSequence mText;

    private ClickHandler mClickListener;

    public ConfirmDialog(final Context context, final String title,
            final String text) {
        super(context, R.style.Dialog);
        this.mContext = context;
        this.mTitle = title;
        this.mText = text;

        init();
    }

    private void init() {
        setContentView(R.layout.dialog_confirm);

        this.mTitleView = (TextView) findViewById(R.id.title);
        final TextPaint tp = this.mTitleView.getPaint();
        tp.setFakeBoldText(true);
        this.mTitleView.setText(this.mTitle);

        this.mTextView = (TextView) findViewById(R.id.text);
        this.mTextView.setText(this.mText);

        this.mButton1 = (Button) findViewById(R.id.button1);
        this.mButton1.setOnClickListener(this);

        this.mButton2 = (Button) findViewById(R.id.button2);
        this.mButton2.setOnClickListener(this);

    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        switch (id) {
        case R.id.button1:
            if (this.mClickListener != null) {
                this.mClickListener.onButton1Click();
            }
            cancel();
            break;
        case R.id.button2:
            if (this.mClickListener != null) {
                this.mClickListener.onButton2Click();
            }
            cancel();
            break;
        default:
            break;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBlurEffect();

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

    public void setButton1Text(final CharSequence text) {
        this.mButton1.setText(text);
    }

    public void setButton2Text(final CharSequence text) {
        this.mButton2.setText(text);
    }

    public void setClickListener(final ClickHandler clickListener) {
        this.mClickListener = clickListener;
    }

    public void setMessage(final CharSequence message) {
        this.mText = message;
        this.mTextView.setText(this.mText);
    }

    public void setMessage(final int resId) {
        this.mText = this.mContext.getResources().getText(resId);
        this.mTextView.setText(this.mText);
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
