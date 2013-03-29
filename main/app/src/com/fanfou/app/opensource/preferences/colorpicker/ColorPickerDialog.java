/*
 * Copyright (C) 2010 Daniel Nilsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fanfou.app.opensource.preferences.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.fanfou.app.opensource.R;

public class ColorPickerDialog extends Dialog implements
        ColorPickerView.OnColorChangedListener, View.OnClickListener {

    public interface OnColorChangedListener {
        public void onColorChanged(int color);
    }

    private ColorPickerView mColorPicker;
    private ColorPickerPanelView mOldColor;

    private ColorPickerPanelView mNewColor;

    private OnColorChangedListener mListener;

    public ColorPickerDialog(final Context context, final int initialColor) {
        super(context);

        init(initialColor);
    }

    public int getColor() {
        return this.mColorPicker.getColor();
    }

    private void init(final int color) {
        // To fight color branding.
        getWindow().setFormat(PixelFormat.RGBA_8888);

        setUp(color);

    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
        case R.id.old_color_panel:
            break;
        case R.id.new_color_panel:
            if (this.mListener != null) {
                this.mListener.onColorChanged(this.mNewColor.getColor());
            }
            break;
        }
        dismiss();
    }

    @Override
    public void onColorChanged(final int color) {

        this.mNewColor.setColor(color);

        /*
         * if (mListener != null) { mListener.onColorChanged(color); }
         */

    }

    public void setAlphaSliderVisible(final boolean visible) {
        this.mColorPicker.setAlphaSliderVisible(visible);
    }

    /**
     * Set a OnColorChangedListener to get notified when the color selected by
     * the user has changed.
     * 
     * @param listener
     */
    public void setOnColorChangedListener(final OnColorChangedListener listener) {
        this.mListener = listener;
    }

    private void setUp(final int color) {

        final LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View layout = inflater
                .inflate(R.layout.dialog_color_picker, null);

        setContentView(layout);

        setTitle(R.string.dialog_color_picker);

        this.mColorPicker = (ColorPickerView) layout
                .findViewById(R.id.color_picker_view);
        this.mOldColor = (ColorPickerPanelView) layout
                .findViewById(R.id.old_color_panel);
        this.mNewColor = (ColorPickerPanelView) layout
                .findViewById(R.id.new_color_panel);

        ((LinearLayout) this.mOldColor.getParent()).setPadding(
                Math.round(this.mColorPicker.getDrawingOffset()), 0,
                Math.round(this.mColorPicker.getDrawingOffset()), 0);

        this.mOldColor.setOnClickListener(this);
        this.mNewColor.setOnClickListener(this);
        this.mColorPicker.setOnColorChangedListener(this);
        this.mOldColor.setColor(color);
        this.mColorPicker.setColor(color, true);

    }

}
