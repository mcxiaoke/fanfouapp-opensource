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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * This class draws a panel which which will be filled with a color which can be
 * set. It can be used to show the currently selected color which you will get
 * from the {@link ColorPickerView}.
 * 
 * @author Daniel Nilsson
 * 
 */
public class ColorPickerPanelView extends View {

    /**
     * The width in pixels of the border surrounding the color panel.
     */
    private final static float BORDER_WIDTH_PX = 1;

    private float mDensity = 1f;

    private int mBorderColor = 0xff6E6E6E;
    private int mColor = 0xff000000;

    private Paint mBorderPaint;
    private Paint mColorPaint;

    private RectF mDrawingRect;
    private RectF mColorRect;

    private AlphaPatternDrawable mAlphaPattern;

    public ColorPickerPanelView(final Context context) {
        this(context, null);
    }

    public ColorPickerPanelView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerPanelView(final Context context,
            final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Get the color of the border surrounding the panel.
     */
    public int getBorderColor() {
        return this.mBorderColor;
    }

    /**
     * Get the color currently show by this view.
     * 
     * @return
     */
    public int getColor() {
        return this.mColor;
    }

    private void init() {
        this.mBorderPaint = new Paint();
        this.mColorPaint = new Paint();
        this.mDensity = getContext().getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        final RectF rect = this.mColorRect;

        if (ColorPickerPanelView.BORDER_WIDTH_PX > 0) {
            this.mBorderPaint.setColor(this.mBorderColor);
            canvas.drawRect(this.mDrawingRect, this.mBorderPaint);
        }

        if (this.mAlphaPattern != null) {
            this.mAlphaPattern.draw(canvas);
        }

        this.mColorPaint.setColor(this.mColor);

        canvas.drawRect(rect, this.mColorPaint);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec,
            final int heightMeasureSpec) {

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw,
            final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        this.mDrawingRect = new RectF();
        this.mDrawingRect.left = getPaddingLeft();
        this.mDrawingRect.right = w - getPaddingRight();
        this.mDrawingRect.top = getPaddingTop();
        this.mDrawingRect.bottom = h - getPaddingBottom();

        setUpColorRect();

    }

    /**
     * Set the color of the border surrounding the panel.
     * 
     * @param color
     */
    public void setBorderColor(final int color) {
        this.mBorderColor = color;
        invalidate();
    }

    /**
     * Set the color that should be shown by this view.
     * 
     * @param color
     */
    public void setColor(final int color) {
        this.mColor = color;
        invalidate();
    }

    private void setUpColorRect() {
        final RectF dRect = this.mDrawingRect;

        final float left = dRect.left + ColorPickerPanelView.BORDER_WIDTH_PX;
        final float top = dRect.top + ColorPickerPanelView.BORDER_WIDTH_PX;
        final float bottom = dRect.bottom
                - ColorPickerPanelView.BORDER_WIDTH_PX;
        final float right = dRect.right - ColorPickerPanelView.BORDER_WIDTH_PX;

        this.mColorRect = new RectF(left, top, right, bottom);

        this.mAlphaPattern = new AlphaPatternDrawable((int) (5 * this.mDensity));

        this.mAlphaPattern.setBounds(Math.round(this.mColorRect.left),
                Math.round(this.mColorRect.top),
                Math.round(this.mColorRect.right),
                Math.round(this.mColorRect.bottom));

    }

}
