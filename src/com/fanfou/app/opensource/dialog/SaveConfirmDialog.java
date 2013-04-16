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
 * @version 1.0 2011.10.27
 * 
 */
public class SaveConfirmDialog extends Dialog implements View.OnClickListener {

    public static interface OnButtonClickListener {
        public void onDiscardClick();

        public void onSaveClick();
    }

    private final Context mContext;
    private TextView mTitleView;
    private TextView mTextView;
    private Button mButtonSave;
    private Button mButtonDisCard;

    private Button mButtonCancel;
    private CharSequence mTitle;

    private CharSequence mText;

    private OnButtonClickListener mClickListener;

    public SaveConfirmDialog(final Context context, final String title,
            final String text) {
        super(context, R.style.Dialog);
        this.mContext = context;
        this.mTitle = title;
        this.mText = text;
    }

    private void init() {
        setContentView(R.layout.dialog_save_confirm);

        this.mTitleView = (TextView) findViewById(R.id.title);
        final TextPaint tp = this.mTitleView.getPaint();
        tp.setFakeBoldText(true);
        this.mTitleView.setText(this.mTitle);

        this.mTextView = (TextView) findViewById(R.id.text);
        this.mTextView.setText(this.mText);

        this.mButtonSave = (Button) findViewById(R.id.button_save);
        this.mButtonSave.setOnClickListener(this);

        this.mButtonDisCard = (Button) findViewById(R.id.button_discard);
        this.mButtonDisCard.setOnClickListener(this);

        this.mButtonCancel = (Button) findViewById(R.id.button_cancel);
        this.mButtonCancel.setOnClickListener(this);

    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        switch (id) {
        case R.id.button_save:
            if (this.mClickListener != null) {
                this.mClickListener.onSaveClick();
            }
            cancel();
            break;
        case R.id.button_discard:
            if (this.mClickListener != null) {
                this.mClickListener.onDiscardClick();
            }
            cancel();
            break;
        case R.id.button_cancel:
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

    public void setOnClickListener(final OnButtonClickListener clickListener) {
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
