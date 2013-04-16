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

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fanfou.app.opensource.AppContext;

/**
 * @author mcxiaoke
 * @version 1.0 2011.11.09
 * @version 1.5 2011.11.10
 * @version 1.6 2011.12.19
 * 
 */
public class SeekBarPreference extends DialogPreference implements
        SeekBar.OnSeekBarChangeListener {
    private static final String TAG = SeekBarPreference.class.getSimpleName();

    private static final String androidns = "http://schemas.android.com/apk/res/android";

    private SeekBar mSeekBar;
    private TextView mMessageText, mValueText;
    private final Context mContext;

    private final String mDialogMessage, mSuffix;
    private final int mDefault;

    private int mMax;

    private int mValue = 0;

    public SeekBarPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        this.mDialogMessage = attrs.getAttributeValue(
                SeekBarPreference.androidns, "dialogMessage");
        this.mSuffix = attrs.getAttributeValue(SeekBarPreference.androidns,
                "text");
        this.mDefault = attrs.getAttributeIntValue(SeekBarPreference.androidns,
                "defaultValue", 0);
        this.mMax = attrs.getAttributeIntValue(SeekBarPreference.androidns,
                "max", 100);

        if (AppContext.DEBUG) {
            Log.d(SeekBarPreference.TAG, "SeekBarPreference() mDefault="
                    + this.mDefault);
            Log.d(SeekBarPreference.TAG, "SeekBarPreference() mValue="
                    + this.mValue);
        }

    }

    public int getMax() {
        return this.mMax;
    }

    public int getProgress() {
        return this.mValue;
    }

    @Override
    protected void onBindDialogView(final View v) {
        super.onBindDialogView(v);

        if (AppContext.DEBUG) {
            Log.d(SeekBarPreference.TAG, "onBindDialogView() mDefault="
                    + this.mDefault);
            Log.d(SeekBarPreference.TAG, "onBindDialogView() mValue="
                    + this.mValue);
        }

        this.mSeekBar.setMax(this.mMax);
        this.mSeekBar.setProgress(this.mValue);
    }

    @Override
    protected View onCreateDialogView() {
        LinearLayout.LayoutParams params;
        final LinearLayout layout = new LinearLayout(this.mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6, 6, 6, 6);

        if (this.mDialogMessage != null) {
            this.mMessageText = new TextView(this.mContext);
            this.mMessageText.setText(this.mDialogMessage);
            layout.addView(this.mMessageText);
        }

        this.mValueText = new TextView(this.mContext);
        this.mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
        this.mValueText.setTextSize(32);
        params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
        layout.addView(this.mValueText, params);

        this.mSeekBar = new SeekBar(this.mContext);
        this.mSeekBar.setOnSeekBarChangeListener(this);
        layout.addView(this.mSeekBar, new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        if (shouldPersist()) {
            this.mValue = getPersistedInt(this.mDefault);
        }

        // if(App.DEBUG){
        // Log.d(TAG, "onCreateDialogView() mDefault="+mDefault);
        // Log.d(TAG, "onCreateDialogView() mMax="+mMax);
        // Log.d(TAG, "onCreateDialogView() mValue="+mValue);
        // }

        // mSeekBar.setMax(mMax);
        // mSeekBar.setProgress(mValue);
        return layout;
    }

    @Override
    protected void onDialogClosed(final boolean positiveResult) {
        if (AppContext.DEBUG) {
            Log.d(SeekBarPreference.TAG, "onDialogClosed positive="
                    + positiveResult + " mvalue=" + this.mValue);
        }
        if (positiveResult) {
            if (shouldPersist()) {
                persistInt(this.mValue);
            }

            if (AppContext.DEBUG) {
                Log.d(SeekBarPreference.TAG, "onDialogClosed() mDefault="
                        + this.mDefault);
                Log.d(SeekBarPreference.TAG, "onDialogClosed() mValue="
                        + this.mValue);
            }

            callChangeListener(Integer.valueOf(this.mValue));
        }
    }

    @Override
    public void onProgressChanged(final SeekBar seek, final int value,
            final boolean fromTouch) {
        final String t = String.valueOf(value);

        if (value > 0) {
            this.mValue = value;
        }

        if (AppContext.DEBUG) {
            Log.d(SeekBarPreference.TAG, "onProgressChanged() mDefault="
                    + this.mDefault);
            Log.d(SeekBarPreference.TAG, "onProgressChanged() mValue="
                    + this.mValue);
            Log.d(SeekBarPreference.TAG, "onProgressChanged() value=" + value);
        }

        this.mValueText.setText(this.mSuffix == null ? t : t
                .concat(this.mSuffix));
        // if (shouldPersist())
        // persistInt(value);
        // callChangeListener(new Integer(value));
    }

    @Override
    protected void onSetInitialValue(final boolean restore,
            final Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore) {
            this.mValue = shouldPersist() ? getPersistedInt(this.mDefault) : 0;
        } else {
            this.mValue = (Integer) defaultValue;
        }

        if (AppContext.DEBUG) {
            Log.d(SeekBarPreference.TAG, "onSetInitialValue() mDefault="
                    + this.mDefault);
            Log.d(SeekBarPreference.TAG, "onSetInitialValue() mValue="
                    + this.mValue);
        }

        setSummary("" + this.mValue + this.mSuffix);
    }

    @Override
    public void onStartTrackingTouch(final SeekBar seek) {
    }

    @Override
    public void onStopTrackingTouch(final SeekBar seek) {
    }

    public void setMax(final int max) {
        this.mMax = max;
    }

    public void setProgress(final int progress) {
        this.mValue = progress;
        if (this.mSeekBar != null) {
            this.mSeekBar.setProgress(progress);
        }
    }
}
