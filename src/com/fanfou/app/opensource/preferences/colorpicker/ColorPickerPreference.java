/*
 * Copyright (C) 2011 Sergey Margaritov
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fanfou.app.opensource.AppContext;

/**
 * A preference type that allows a user to choose a time
 * 
 * @author Sergey Margaritov
 */
public class ColorPickerPreference extends Preference implements
        Preference.OnPreferenceClickListener,
        ColorPickerDialog.OnColorChangedListener {
    private static final String TAG = ColorPickerPreference.class
            .getSimpleName();

    /**
     * For custom purposes. Not used by ColorPickerPreferrence
     * 
     * @param color
     * @author Unknown
     */
    public static String convertToARGB(final int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }

    /**
     * For custom purposes. Not used by ColorPickerPreferrence
     * 
     * @param argb
     * @throws NumberFormatException
     * @author Unknown
     */
    public static int convertToColorInt(String argb)
            throws NumberFormatException {

        if (argb.startsWith("#")) {
            argb = argb.replace("#", "");
        }

        int alpha = -1, red = -1, green = -1, blue = -1;

        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        } else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }

        return Color.argb(alpha, red, green, blue);
    }

    View mView;
    int mDefaultValue = Color.BLACK;
    private int mValue = Color.BLACK;

    private float mDensity = 0;

    private boolean mAlphaSliderEnabled = false;

    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    public ColorPickerPreference(final Context context) {
        super(context);
        init(context, null);
    }

    public ColorPickerPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorPickerPreference(final Context context,
            final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private Bitmap getPreviewBitmap() {
        final int d = (int) (this.mDensity * 31); // 30dip
        final int color = getValue();
        final Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
        final int w = bm.getWidth();
        final int h = bm.getHeight();
        int c = color;
        for (int i = 0; i < w; i++) {
            for (int j = i; j < h; j++) {
                c = ((i <= 1) || (j <= 1) || (i >= (w - 2)) || (j >= (h - 2))) ? Color.GRAY
                        : color;
                bm.setPixel(i, j, c);
                if (i != j) {
                    bm.setPixel(j, i, c);
                }
            }
        }

        return bm;
    }

    public int getValue() {
        try {
            if (isPersistent()) {
                this.mValue = getPersistedInt(this.mDefaultValue);
            }
        } catch (final ClassCastException e) {
            this.mValue = this.mDefaultValue;
        }
        return this.mValue;
    }

    private void init(final Context context, final AttributeSet attrs) {
        if (AppContext.DEBUG) {
            Log.d(ColorPickerPreference.TAG, "init");
        }
        this.mDensity = getContext().getResources().getDisplayMetrics().density;
        setOnPreferenceClickListener(this);
        if (attrs != null) {
            final String defaultValue = attrs.getAttributeValue(
                    ColorPickerPreference.ANDROID_NS, "defaultValue");
            if (defaultValue.startsWith("#")) {
                try {
                    this.mDefaultValue = ColorPickerPreference
                            .convertToColorInt(defaultValue);
                } catch (final NumberFormatException e) {
                    this.mDefaultValue = ColorPickerPreference
                            .convertToColorInt("#FF000000");
                }
            } else {
                final int resourceId = attrs.getAttributeResourceValue(
                        ColorPickerPreference.ANDROID_NS, "defaultValue", 0);
                if (resourceId != 0) {
                    this.mDefaultValue = context.getResources().getColor(
                            resourceId);
                }
            }
            this.mAlphaSliderEnabled = attrs.getAttributeBooleanValue(null,
                    "alphaSlider", false);
        }
        this.mValue = this.mDefaultValue;
    }

    @Override
    protected void onBindView(final View view) {
        super.onBindView(view);
        this.mView = view;
        setPreviewColor();
    }

    @Override
    public void onColorChanged(final int color) {
        if (isPersistent()) {
            persistInt(color);
        }
        this.mValue = color;
        setPreviewColor();
        try {
            getOnPreferenceChangeListener().onPreferenceChange(this, color);
        } catch (final NullPointerException e) {

        }
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {
        final ColorPickerDialog picker = new ColorPickerDialog(getContext(),
                getValue());
        picker.setOnColorChangedListener(this);
        if (this.mAlphaSliderEnabled) {
            picker.setAlphaSliderVisible(true);
        }
        picker.show();

        return false;
    }

    @Override
    protected void onSetInitialValue(final boolean restoreValue,
            final Object defaultValue) {
        onColorChanged(restoreValue ? getValue() : (Integer) defaultValue);
    }

    /**
     * Toggle Alpha Slider visibility (by default it's disabled)
     * 
     * @param enable
     */
    public void setAlphaSliderEnabled(final boolean enable) {
        this.mAlphaSliderEnabled = enable;
    }

    public void setPreviewColor() {
        if (this.mView == null) {
            return;
        }
        final ImageView iView = new ImageView(getContext());
        final LinearLayout widgetFrameView = ((LinearLayout) this.mView
                .findViewById(android.R.id.widget_frame));
        if (widgetFrameView == null) {
            return;
        }
        widgetFrameView.setPadding(widgetFrameView.getPaddingLeft(),
                widgetFrameView.getPaddingTop(), (int) (this.mDensity * 8),
                widgetFrameView.getPaddingBottom());
        // remove already create preview image
        final int count = widgetFrameView.getChildCount();
        if (count > 0) {
            widgetFrameView.removeViews(0, count);
        }
        widgetFrameView.addView(iView);
        iView.setBackgroundDrawable(new AlphaPatternDrawable(
                (int) (5 * this.mDensity)));
        iView.setImageBitmap(getPreviewBitmap());
    }

}
