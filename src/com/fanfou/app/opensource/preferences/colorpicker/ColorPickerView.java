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
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Displays a color picker to the user and allow them to select a color. A
 * slider for the alpha channel is also available. Enable it by setting
 * setAlphaSliderVisible(boolean) to true.
 * 
 * @author Daniel Nilsson
 */
public class ColorPickerView extends View {

    public interface OnColorChangedListener {
        public void onColorChanged(int color);
    }

    private final static int PANEL_SAT_VAL = 0;
    private final static int PANEL_HUE = 1;

    private final static int PANEL_ALPHA = 2;

    /**
     * The width in pixels of the border surrounding all color panels.
     */
    private final static float BORDER_WIDTH_PX = 1;
    /**
     * The width in dp of the hue panel.
     */
    private float HUE_PANEL_WIDTH = 30f;
    /**
     * The height in dp of the alpha panel
     */
    private float ALPHA_PANEL_HEIGHT = 20f;
    /**
     * The distance in dp between the different color panels.
     */
    private float PANEL_SPACING = 10f;
    /**
     * The radius in dp of the color palette tracker circle.
     */
    private float PALETTE_CIRCLE_TRACKER_RADIUS = 5f;

    /**
     * The dp which the tracker of the hue or alpha panel will extend outside of
     * its bounds.
     */
    private float RECTANGLE_TRACKER_OFFSET = 2f;

    private float mDensity = 1f;

    private OnColorChangedListener mListener;
    private Paint mSatValPaint;

    private Paint mSatValTrackerPaint;
    private Paint mHuePaint;

    private Paint mHueTrackerPaint;
    private Paint mAlphaPaint;

    private Paint mAlphaTextPaint;

    private Paint mBorderPaint;
    private Shader mValShader;
    private Shader mSatShader;
    private Shader mHueShader;

    private Shader mAlphaShader;
    private int mAlpha = 0xff;
    private float mHue = 360f;
    private float mSat = 0f;

    private float mVal = 0f;
    private String mAlphaSliderText = "";
    private int mSliderTrackerColor = 0xff1c1c1c;
    private int mBorderColor = 0xff6E6E6E;

    private boolean mShowAlphaPanel = false;

    /*
     * To remember which panel that has the "focus" when processing hardware
     * button data.
     */
    private int mLastTouchedPanel = ColorPickerView.PANEL_SAT_VAL;

    /**
     * Offset from the edge we must have or else the finger tracker will get
     * clipped when it is drawn outside of the view.
     */
    private float mDrawingOffset;

    /*
     * Distance form the edges of the view of where we are allowed to draw.
     */
    private RectF mDrawingRect;
    private RectF mSatValRect;
    private RectF mHueRect;

    private RectF mAlphaRect;

    private AlphaPatternDrawable mAlphaPattern;

    private Point mStartTouchPoint = null;

    public ColorPickerView(final Context context) {
        this(context, null);
    }

    public ColorPickerView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private Point alphaToPoint(final int alpha) {

        final RectF rect = this.mAlphaRect;
        final float width = rect.width();

        final Point p = new Point();

        p.x = (int) ((width - ((alpha * width) / 0xff)) + rect.left);
        p.y = (int) rect.top;

        return p;

    }

    private int[] buildHueColorArray() {

        final int[] hue = new int[361];

        int count = 0;
        for (int i = hue.length - 1; i >= 0; i--, count++) {
            hue[count] = Color.HSVToColor(new float[] { i, 1f, 1f });
        }

        return hue;
    }

    private float calculateRequiredOffset() {
        float offset = Math.max(this.PALETTE_CIRCLE_TRACKER_RADIUS,
                this.RECTANGLE_TRACKER_OFFSET);
        offset = Math.max(offset, ColorPickerView.BORDER_WIDTH_PX
                * this.mDensity);

        return offset * 1.5f;
    }

    private int chooseHeight(final int mode, final int size) {
        if ((mode == MeasureSpec.AT_MOST) || (mode == MeasureSpec.EXACTLY)) {
            return size;
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            return getPrefferedHeight();
        }
    }

    private int chooseWidth(final int mode, final int size) {
        if ((mode == MeasureSpec.AT_MOST) || (mode == MeasureSpec.EXACTLY)) {
            return size;
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            return getPrefferedWidth();
        }
    }

    private void drawAlphaPanel(final Canvas canvas) {

        if (!this.mShowAlphaPanel || (this.mAlphaRect == null)
                || (this.mAlphaPattern == null)) {
            return;
        }

        final RectF rect = this.mAlphaRect;

        if (ColorPickerView.BORDER_WIDTH_PX > 0) {
            this.mBorderPaint.setColor(this.mBorderColor);
            canvas.drawRect(rect.left - ColorPickerView.BORDER_WIDTH_PX,
                    rect.top - ColorPickerView.BORDER_WIDTH_PX, rect.right
                            + ColorPickerView.BORDER_WIDTH_PX, rect.bottom
                            + ColorPickerView.BORDER_WIDTH_PX,
                    this.mBorderPaint);
        }

        this.mAlphaPattern.draw(canvas);

        final float[] hsv = new float[] { this.mHue, this.mSat, this.mVal };
        final int color = Color.HSVToColor(hsv);
        final int acolor = Color.HSVToColor(0, hsv);

        this.mAlphaShader = new LinearGradient(rect.left, rect.top, rect.right,
                rect.top, color, acolor, TileMode.CLAMP);

        this.mAlphaPaint.setShader(this.mAlphaShader);

        canvas.drawRect(rect, this.mAlphaPaint);

        if (!TextUtils.isEmpty(this.mAlphaSliderText)) {
            canvas.drawText(this.mAlphaSliderText, rect.centerX(),
                    rect.centerY() + (4 * this.mDensity), this.mAlphaTextPaint);
        }

        final float rectWidth = (4 * this.mDensity) / 2;

        final Point p = alphaToPoint(this.mAlpha);

        final RectF r = new RectF();
        r.left = p.x - rectWidth;
        r.right = p.x + rectWidth;
        r.top = rect.top - this.RECTANGLE_TRACKER_OFFSET;
        r.bottom = rect.bottom + this.RECTANGLE_TRACKER_OFFSET;

        canvas.drawRoundRect(r, 2, 2, this.mHueTrackerPaint);

    }

    private void drawHuePanel(final Canvas canvas) {

        final RectF rect = this.mHueRect;

        if (ColorPickerView.BORDER_WIDTH_PX > 0) {
            this.mBorderPaint.setColor(this.mBorderColor);
            canvas.drawRect(rect.left - ColorPickerView.BORDER_WIDTH_PX,
                    rect.top - ColorPickerView.BORDER_WIDTH_PX, rect.right
                            + ColorPickerView.BORDER_WIDTH_PX, rect.bottom
                            + ColorPickerView.BORDER_WIDTH_PX,
                    this.mBorderPaint);
        }

        if (this.mHueShader == null) {
            this.mHueShader = new LinearGradient(rect.left, rect.top,
                    rect.left, rect.bottom, buildHueColorArray(), null,
                    TileMode.CLAMP);
            this.mHuePaint.setShader(this.mHueShader);
        }

        canvas.drawRect(rect, this.mHuePaint);

        final float rectHeight = (4 * this.mDensity) / 2;

        final Point p = hueToPoint(this.mHue);

        final RectF r = new RectF();
        r.left = rect.left - this.RECTANGLE_TRACKER_OFFSET;
        r.right = rect.right + this.RECTANGLE_TRACKER_OFFSET;
        r.top = p.y - rectHeight;
        r.bottom = p.y + rectHeight;

        canvas.drawRoundRect(r, 2, 2, this.mHueTrackerPaint);

    }

    private void drawSatValPanel(final Canvas canvas) {

        final RectF rect = this.mSatValRect;

        if (ColorPickerView.BORDER_WIDTH_PX > 0) {
            this.mBorderPaint.setColor(this.mBorderColor);
            canvas.drawRect(this.mDrawingRect.left, this.mDrawingRect.top,
                    rect.right + ColorPickerView.BORDER_WIDTH_PX, rect.bottom
                            + ColorPickerView.BORDER_WIDTH_PX,
                    this.mBorderPaint);
        }

        if (this.mValShader == null) {
            this.mValShader = new LinearGradient(rect.left, rect.top,
                    rect.left, rect.bottom, 0xffffffff, 0xff000000,
                    TileMode.CLAMP);
        }

        final int rgb = Color.HSVToColor(new float[] { this.mHue, 1f, 1f });

        this.mSatShader = new LinearGradient(rect.left, rect.top, rect.right,
                rect.top, 0xffffffff, rgb, TileMode.CLAMP);
        final ComposeShader mShader = new ComposeShader(this.mValShader,
                this.mSatShader, PorterDuff.Mode.MULTIPLY);
        this.mSatValPaint.setShader(mShader);

        canvas.drawRect(rect, this.mSatValPaint);

        final Point p = satValToPoint(this.mSat, this.mVal);

        this.mSatValTrackerPaint.setColor(0xff000000);
        canvas.drawCircle(p.x, p.y, this.PALETTE_CIRCLE_TRACKER_RADIUS
                - (1f * this.mDensity), this.mSatValTrackerPaint);

        this.mSatValTrackerPaint.setColor(0xffdddddd);
        canvas.drawCircle(p.x, p.y, this.PALETTE_CIRCLE_TRACKER_RADIUS,
                this.mSatValTrackerPaint);

    }

    /**
     * Get the current value of the text that will be shown in the alpha slider.
     * 
     * @return
     */
    public String getAlphaSliderText() {
        return this.mAlphaSliderText;
    }

    /**
     * Get the color of the border surrounding all panels.
     */
    public int getBorderColor() {
        return this.mBorderColor;
    }

    /**
     * Get the current color this view is showing.
     * 
     * @return the current color.
     */
    public int getColor() {
        return Color.HSVToColor(this.mAlpha, new float[] { this.mHue,
                this.mSat, this.mVal });
    }

    /**
     * Get the drawing offset of the color picker view. The drawing offset is
     * the distance from the side of a panel to the side of the view minus the
     * padding. Useful if you want to have your own panel below showing the
     * currently selected color and want to align it perfectly.
     * 
     * @return The offset in pixels.
     */
    public float getDrawingOffset() {
        return this.mDrawingOffset;
    }

    private int getPrefferedHeight() {

        int height = (int) (200 * this.mDensity);

        if (this.mShowAlphaPanel) {
            height += this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT;
        }

        return height;
    }

    private int getPrefferedWidth() {

        int width = getPrefferedHeight();

        if (this.mShowAlphaPanel) {
            width -= (this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT);
        }

        return (int) (width + this.HUE_PANEL_WIDTH + this.PANEL_SPACING);

    }

    public int getSliderTrackerColor() {
        return this.mSliderTrackerColor;
    }

    private Point hueToPoint(final float hue) {

        final RectF rect = this.mHueRect;
        final float height = rect.height();

        final Point p = new Point();

        p.y = (int) ((height - ((hue * height) / 360f)) + rect.top);
        p.x = (int) rect.left;

        return p;
    }

    private void init() {
        this.mDensity = getContext().getResources().getDisplayMetrics().density;
        this.PALETTE_CIRCLE_TRACKER_RADIUS *= this.mDensity;
        this.RECTANGLE_TRACKER_OFFSET *= this.mDensity;
        this.HUE_PANEL_WIDTH *= this.mDensity;
        this.ALPHA_PANEL_HEIGHT *= this.mDensity;
        this.PANEL_SPACING = this.PANEL_SPACING * this.mDensity;

        this.mDrawingOffset = calculateRequiredOffset();

        initPaintTools();

        // Needed for receiving trackball motion events.
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private void initPaintTools() {

        this.mSatValPaint = new Paint();
        this.mSatValTrackerPaint = new Paint();
        this.mHuePaint = new Paint();
        this.mHueTrackerPaint = new Paint();
        this.mAlphaPaint = new Paint();
        this.mAlphaTextPaint = new Paint();
        this.mBorderPaint = new Paint();

        this.mSatValTrackerPaint.setStyle(Style.STROKE);
        this.mSatValTrackerPaint.setStrokeWidth(2f * this.mDensity);
        this.mSatValTrackerPaint.setAntiAlias(true);

        this.mHueTrackerPaint.setColor(this.mSliderTrackerColor);
        this.mHueTrackerPaint.setStyle(Style.STROKE);
        this.mHueTrackerPaint.setStrokeWidth(2f * this.mDensity);
        this.mHueTrackerPaint.setAntiAlias(true);

        this.mAlphaTextPaint.setColor(0xff1c1c1c);
        this.mAlphaTextPaint.setTextSize(14f * this.mDensity);
        this.mAlphaTextPaint.setAntiAlias(true);
        this.mAlphaTextPaint.setTextAlign(Align.CENTER);
        this.mAlphaTextPaint.setFakeBoldText(true);

    }

    private boolean moveTrackersIfNeeded(final MotionEvent event) {

        if (this.mStartTouchPoint == null) {
            return false;
        }

        boolean update = false;

        final int startX = this.mStartTouchPoint.x;
        final int startY = this.mStartTouchPoint.y;

        if (this.mHueRect.contains(startX, startY)) {
            this.mLastTouchedPanel = ColorPickerView.PANEL_HUE;

            this.mHue = pointToHue(event.getY());

            update = true;
        } else if (this.mSatValRect.contains(startX, startY)) {

            this.mLastTouchedPanel = ColorPickerView.PANEL_SAT_VAL;

            final float[] result = pointToSatVal(event.getX(), event.getY());

            this.mSat = result[0];
            this.mVal = result[1];

            update = true;
        } else if ((this.mAlphaRect != null)
                && this.mAlphaRect.contains(startX, startY)) {

            this.mLastTouchedPanel = ColorPickerView.PANEL_ALPHA;

            this.mAlpha = pointToAlpha((int) event.getX());

            update = true;
        }

        return update;
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        if ((this.mDrawingRect.width() <= 0)
                || (this.mDrawingRect.height() <= 0)) {
            return;
        }

        drawSatValPanel(canvas);
        drawHuePanel(canvas);
        drawAlphaPanel(canvas);

    }

    @Override
    protected void onMeasure(final int widthMeasureSpec,
            final int heightMeasureSpec) {

        int width = 0;
        int height = 0;

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int widthAllowed = MeasureSpec.getSize(widthMeasureSpec);
        int heightAllowed = MeasureSpec.getSize(heightMeasureSpec);

        widthAllowed = chooseWidth(widthMode, widthAllowed);
        heightAllowed = chooseHeight(heightMode, heightAllowed);

        if (!this.mShowAlphaPanel) {

            height = (int) (widthAllowed - this.PANEL_SPACING - this.HUE_PANEL_WIDTH);

            // If calculated height (based on the width) is more than the
            // allowed height.
            if ((height > heightAllowed) || getTag().equals("landscape")) {
                height = heightAllowed;
                width = (int) (height + this.PANEL_SPACING + this.HUE_PANEL_WIDTH);
            } else {
                width = widthAllowed;
            }
        } else {

            width = (int) ((heightAllowed - this.ALPHA_PANEL_HEIGHT) + this.HUE_PANEL_WIDTH);

            if (width > widthAllowed) {
                width = widthAllowed;
                height = (int) ((widthAllowed - this.HUE_PANEL_WIDTH) + this.ALPHA_PANEL_HEIGHT);
            } else {
                height = heightAllowed;
            }

        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw,
            final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        this.mDrawingRect = new RectF();
        this.mDrawingRect.left = this.mDrawingOffset + getPaddingLeft();
        this.mDrawingRect.right = w - this.mDrawingOffset - getPaddingRight();
        this.mDrawingRect.top = this.mDrawingOffset + getPaddingTop();
        this.mDrawingRect.bottom = h - this.mDrawingOffset - getPaddingBottom();

        setUpSatValRect();
        setUpHueRect();
        setUpAlphaRect();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        boolean update = false;

        switch (event.getAction()) {

        case MotionEvent.ACTION_DOWN:

            this.mStartTouchPoint = new Point((int) event.getX(),
                    (int) event.getY());

            update = moveTrackersIfNeeded(event);

            break;

        case MotionEvent.ACTION_MOVE:

            update = moveTrackersIfNeeded(event);

            break;

        case MotionEvent.ACTION_UP:

            this.mStartTouchPoint = null;

            update = moveTrackersIfNeeded(event);

            break;

        }

        if (update) {

            if (this.mListener != null) {
                this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha,
                        new float[] { this.mHue, this.mSat, this.mVal }));
            }

            invalidate();
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onTrackballEvent(final MotionEvent event) {

        final float x = event.getX();
        final float y = event.getY();

        boolean update = false;

        if (event.getAction() == MotionEvent.ACTION_MOVE) {

            switch (this.mLastTouchedPanel) {

            case PANEL_SAT_VAL:

                float sat,
                val;

                sat = this.mSat + (x / 50f);
                val = this.mVal - (y / 50f);

                if (sat < 0f) {
                    sat = 0f;
                } else if (sat > 1f) {
                    sat = 1f;
                }

                if (val < 0f) {
                    val = 0f;
                } else if (val > 1f) {
                    val = 1f;
                }

                this.mSat = sat;
                this.mVal = val;

                update = true;

                break;

            case PANEL_HUE:

                float hue = this.mHue - (y * 10f);

                if (hue < 0f) {
                    hue = 0f;
                } else if (hue > 360f) {
                    hue = 360f;
                }

                this.mHue = hue;

                update = true;

                break;

            case PANEL_ALPHA:

                if (!this.mShowAlphaPanel || (this.mAlphaRect == null)) {
                    update = false;
                } else {

                    int alpha = (int) (this.mAlpha - (x * 10));

                    if (alpha < 0) {
                        alpha = 0;
                    } else if (alpha > 0xff) {
                        alpha = 0xff;
                    }

                    this.mAlpha = alpha;

                    update = true;
                }

                break;
            }

        }

        if (update) {

            if (this.mListener != null) {
                this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha,
                        new float[] { this.mHue, this.mSat, this.mVal }));
            }

            invalidate();
            return true;
        }

        return super.onTrackballEvent(event);
    }

    private int pointToAlpha(int x) {

        final RectF rect = this.mAlphaRect;
        final int width = (int) rect.width();

        if (x < rect.left) {
            x = 0;
        } else if (x > rect.right) {
            x = width;
        } else {
            x = x - (int) rect.left;
        }

        return 0xff - ((x * 0xff) / width);

    }

    private float pointToHue(float y) {

        final RectF rect = this.mHueRect;

        final float height = rect.height();

        if (y < rect.top) {
            y = 0f;
        } else if (y > rect.bottom) {
            y = height;
        } else {
            y = y - rect.top;
        }

        return 360f - ((y * 360f) / height);
    }

    private float[] pointToSatVal(float x, float y) {

        final RectF rect = this.mSatValRect;
        final float[] result = new float[2];

        final float width = rect.width();
        final float height = rect.height();

        if (x < rect.left) {
            x = 0f;
        } else if (x > rect.right) {
            x = width;
        } else {
            x = x - rect.left;
        }

        if (y < rect.top) {
            y = 0f;
        } else if (y > rect.bottom) {
            y = height;
        } else {
            y = y - rect.top;
        }

        result[0] = (1.f / width) * x;
        result[1] = 1.f - ((1.f / height) * y);

        return result;
    }

    private Point satValToPoint(final float sat, final float val) {

        final RectF rect = this.mSatValRect;
        final float height = rect.height();
        final float width = rect.width();

        final Point p = new Point();

        p.x = (int) ((sat * width) + rect.left);
        p.y = (int) (((1f - val) * height) + rect.top);

        return p;
    }

    /**
     * Set the text that should be shown in the alpha slider. Set to null to
     * disable text.
     * 
     * @param res
     *            string resource id.
     */
    public void setAlphaSliderText(final int res) {
        final String text = getContext().getString(res);
        setAlphaSliderText(text);
    }

    /**
     * Set the text that should be shown in the alpha slider. Set to null to
     * disable text.
     * 
     * @param text
     *            Text that should be shown.
     */
    public void setAlphaSliderText(final String text) {
        this.mAlphaSliderText = text;
        invalidate();
    }

    /**
     * Set if the user is allowed to adjust the alpha panel. Default is false.
     * If it is set to false no alpha will be set.
     * 
     * @param visible
     */
    public void setAlphaSliderVisible(final boolean visible) {

        if (this.mShowAlphaPanel != visible) {
            this.mShowAlphaPanel = visible;

            /*
             * Reset all shader to force a recreation. Otherwise they will not
             * look right after the size of the view has changed.
             */
            this.mValShader = null;
            this.mSatShader = null;
            this.mHueShader = null;
            this.mAlphaShader = null;
            ;

            requestLayout();
        }

    }

    /**
     * Set the color of the border surrounding all panels.
     * 
     * @param color
     */
    public void setBorderColor(final int color) {
        this.mBorderColor = color;
        invalidate();
    }

    /**
     * Set the color the view should show.
     * 
     * @param color
     *            The color that should be selected.
     */
    public void setColor(final int color) {
        setColor(color, false);
    }

    /**
     * Set the color this view should show.
     * 
     * @param color
     *            The color that should be selected.
     * @param callback
     *            If you want to get a callback to your OnColorChangedListener.
     */
    public void setColor(final int color, final boolean callback) {

        final int alpha = Color.alpha(color);
        final int red = Color.red(color);
        final int blue = Color.blue(color);
        final int green = Color.green(color);

        final float[] hsv = new float[3];

        Color.RGBToHSV(red, green, blue, hsv);

        this.mAlpha = alpha;
        this.mHue = hsv[0];
        this.mSat = hsv[1];
        this.mVal = hsv[2];

        if (callback && (this.mListener != null)) {
            this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha,
                    new float[] { this.mHue, this.mSat, this.mVal }));
        }

        invalidate();
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

    public void setSliderTrackerColor(final int color) {
        this.mSliderTrackerColor = color;

        this.mHueTrackerPaint.setColor(this.mSliderTrackerColor);

        invalidate();
    }

    private void setUpAlphaRect() {

        if (!this.mShowAlphaPanel) {
            return;
        }

        final RectF dRect = this.mDrawingRect;

        final float left = dRect.left + ColorPickerView.BORDER_WIDTH_PX;
        final float top = (dRect.bottom - this.ALPHA_PANEL_HEIGHT)
                + ColorPickerView.BORDER_WIDTH_PX;
        final float bottom = dRect.bottom - ColorPickerView.BORDER_WIDTH_PX;
        final float right = dRect.right - ColorPickerView.BORDER_WIDTH_PX;

        this.mAlphaRect = new RectF(left, top, right, bottom);

        this.mAlphaPattern = new AlphaPatternDrawable((int) (5 * this.mDensity));
        this.mAlphaPattern.setBounds(Math.round(this.mAlphaRect.left),
                Math.round(this.mAlphaRect.top),
                Math.round(this.mAlphaRect.right),
                Math.round(this.mAlphaRect.bottom));

    }

    private void setUpHueRect() {
        final RectF dRect = this.mDrawingRect;

        final float left = (dRect.right - this.HUE_PANEL_WIDTH)
                + ColorPickerView.BORDER_WIDTH_PX;
        final float top = dRect.top + ColorPickerView.BORDER_WIDTH_PX;
        final float bottom = dRect.bottom
                - ColorPickerView.BORDER_WIDTH_PX
                - (this.mShowAlphaPanel ? (this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT)
                        : 0);
        final float right = dRect.right - ColorPickerView.BORDER_WIDTH_PX;

        this.mHueRect = new RectF(left, top, right, bottom);
    }

    private void setUpSatValRect() {

        final RectF dRect = this.mDrawingRect;
        float panelSide = dRect.height()
                - (ColorPickerView.BORDER_WIDTH_PX * 2);

        if (this.mShowAlphaPanel) {
            panelSide -= this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT;
        }

        final float left = dRect.left + ColorPickerView.BORDER_WIDTH_PX;
        final float top = dRect.top + ColorPickerView.BORDER_WIDTH_PX;
        final float bottom = top + panelSide;
        final float right = left + panelSide;

        this.mSatValRect = new RectF(left, top, right, bottom);
    }
}
