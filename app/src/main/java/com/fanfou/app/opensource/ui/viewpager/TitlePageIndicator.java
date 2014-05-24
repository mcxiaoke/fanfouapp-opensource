/*
 * Copyright (C) 2011 Patrik Akerfeldt
 * Copyright (C) 2011 Francisco Figueiredo Jr.
 * Copyright (C) 2011 Jake Wharton
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
package com.fanfou.app.opensource.ui.viewpager;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.fanfou.app.opensource.HomePage;
import com.fanfou.app.opensource.R;

/**
 * A TitlePageIndicator is a PageIndicator which displays the title of left view
 * (if exist), the title of the current select view (centered) and the title of
 * the right view (if exist). When the user scrolls the ViewPager then titles
 * are also scrolled.
 */
/**
 * @author mcxiaoke
 * @version 1.1 2011.08.20
 * @version 1.2 2011.11.04
 * @version 1.3 2011.11.08
 * @version 1.4 2011.11.11
 * 
 */
public class TitlePageIndicator extends View implements PageIndicator {

    public enum IndicatorStyle {
        None(0), Triangle(1), Underline(2);

        public static IndicatorStyle fromValue(final int value) {
            for (final IndicatorStyle style : IndicatorStyle.values()) {
                if (style.value == value) {
                    return style;
                }
            }
            return null;
        }

        public final int value;

        private IndicatorStyle(final int value) {
            this.value = value;
        }
    }

    static class SavedState extends BaseSavedState {
        int currentPage;

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(final Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(final int size) {
                return new SavedState[size];
            }
        };

        private SavedState(final Parcel in) {
            super(in);
            this.currentPage = in.readInt();
        }

        public SavedState(final Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.currentPage);
        }
    }

    private static final String TAG = TitlePageIndicator.class.getSimpleName();

    /**
     * Percentage indicating what percentage of the screen width away from
     * center should the underline be fully faded. A value of 0.25 means that
     * halfway between the center of the screen and an edge.
     */
    private static final float SELECTION_FADE_PERCENTAGE = 0.25f;

    /**
     * Percentage indicating what percentage of the screen width away from
     * center should the selected text bold turn off. A value of 0.05 means that
     * 10% between the center and an edge.
     */
    private static final float BOLD_FADE_PERCENTAGE = 0.05f;
    private ViewPager mViewPager;
    private TitleProvider mTitleProvider;
    private int mCurrentPage;
    private int mCurrentOffset;
    private final Paint mPaintText;
    private boolean mBoldText;
    private int mColorText;
    private int mColorSelected;
    private final Path mPath;
    private final Paint mPaintFooterLine;
    private IndicatorStyle mFooterIndicatorStyle;
    private final Paint mPaintFooterIndicator;
    private float mFooterIndicatorHeight;
    private final float mFooterIndicatorUnderlinePadding;
    private float mFooterPadding;
    private float mTitlePadding;
    /** Left and right side padding for not active view titles. */
    private float mClipPadding;

    private float mFooterLineHeight;

    public TitlePageIndicator(final Context context) {
        this(context, null);
    }

    public TitlePageIndicator(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.titlePageIndicatorStyle);
    }

    public TitlePageIndicator(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);

        // Load defaults from resources
        final Resources res = getResources();
        final int defaultFooterColor = res
                .getColor(R.color.default_title_indicator_footer_color);
        final float defaultFooterLineHeight = res
                .getDimension(R.dimen.default_title_indicator_footer_line_height);
        final int defaultFooterIndicatorStyle = res
                .getInteger(R.integer.default_title_indicator_footer_indicator_style);
        final float defaultFooterIndicatorHeight = res
                .getDimension(R.dimen.default_title_indicator_footer_indicator_height);
        final float defaultFooterIndicatorUnderlinePadding = res
                .getDimension(R.dimen.default_title_indicator_footer_indicator_underline_padding);
        final float defaultFooterPadding = res
                .getDimension(R.dimen.default_title_indicator_footer_padding);
        final int defaultSelectedColor = res
                .getColor(R.color.default_title_indicator_selected_color);
        final boolean defaultSelectedBold = res
                .getBoolean(R.bool.default_title_indicator_selected_bold);
        final int defaultTextColor = res
                .getColor(R.color.default_title_indicator_text_color);
        final float defaultTextSize = res
                .getDimension(R.dimen.default_title_indicator_text_size);
        final float defaultTitlePadding = res
                .getDimension(R.dimen.default_title_indicator_title_padding);
        final float defaultClipPadding = res
                .getDimension(R.dimen.default_title_indicator_clip_padding);

        // Retrieve styles attributes
        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.TitlePageIndicator, defStyle,
                R.style.Widget_TitlePageIndicator);

        // Retrieve the colors to be used for this view and apply them.
        this.mFooterLineHeight = a.getDimension(
                R.styleable.TitlePageIndicator_footerLineHeight,
                defaultFooterLineHeight);
        this.mFooterIndicatorStyle = IndicatorStyle.fromValue(a.getInteger(
                R.styleable.TitlePageIndicator_footerIndicatorStyle,
                defaultFooterIndicatorStyle));
        this.mFooterIndicatorHeight = a.getDimension(
                R.styleable.TitlePageIndicator_footerIndicatorHeight,
                defaultFooterIndicatorHeight);
        this.mFooterIndicatorUnderlinePadding = a.getDimension(
                R.styleable.TitlePageIndicator_footerIndicatorUnderlinePadding,
                defaultFooterIndicatorUnderlinePadding);
        this.mFooterPadding = a.getDimension(
                R.styleable.TitlePageIndicator_footerPadding,
                defaultFooterPadding);
        this.mTitlePadding = a.getDimension(
                R.styleable.TitlePageIndicator_titlePadding,
                defaultTitlePadding);
        this.mClipPadding = a.getDimension(
                R.styleable.TitlePageIndicator_clipPadding, defaultClipPadding);
        this.mColorSelected = a.getColor(
                R.styleable.TitlePageIndicator_selectedColor,
                defaultSelectedColor);
        this.mColorText = a.getColor(R.styleable.TitlePageIndicator_textColor,
                defaultTextColor);
        this.mBoldText = a.getBoolean(
                R.styleable.TitlePageIndicator_selectedBold,
                defaultSelectedBold);

        final float textSize = a.getDimension(
                R.styleable.TitlePageIndicator_textSize, defaultTextSize);
        final int footerColor = a.getColor(
                R.styleable.TitlePageIndicator_footerColor, defaultFooterColor);
        this.mPaintText = new Paint();
        this.mPaintText.setTextSize(textSize);
        this.mPaintText.setAntiAlias(true);
        this.mPaintFooterLine = new Paint();
        this.mPaintFooterLine.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mPaintFooterLine.setStrokeWidth(this.mFooterLineHeight);
        this.mPaintFooterLine.setColor(footerColor);
        this.mPaintFooterIndicator = new Paint();
        this.mPaintFooterIndicator.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mPaintFooterIndicator.setColor(footerColor);

        a.recycle();

        this.mPath = new Path();
    }

    /**
     * Calculate the bounds for a view's title
     * 
     * @param index
     * @param paint
     * @return
     */
    private RectF calcBounds(final int index, final Paint paint) {
        // Calculate the text bounds
        final RectF bounds = new RectF();
        bounds.right = paint.measureText(this.mTitleProvider.getTitle(index));
        bounds.bottom = paint.descent() - paint.ascent();
        return bounds;
    }

    /**
     * Calculate views bounds and scroll them according to the current index
     * 
     * @param paint
     * @param currentIndex
     * @return
     */
    private ArrayList<RectF> calculateAllBounds(final Paint paint) {
        final ArrayList<RectF> list = new ArrayList<RectF>();
        // For each views (If no values then add a fake one)
        final int count = getPageCount();
        final int width = getWidth();
        final int halfWidth = width / 2;
        for (int i = 0; i < count; i++) {
            final RectF bounds = calcBounds(i, paint);
            final float w = (bounds.right - bounds.left);
            final float h = (bounds.bottom - bounds.top);
            bounds.left = ((halfWidth) - (w / 2) - this.mCurrentOffset)
                    + ((i - this.mCurrentPage) * width);
            bounds.right = bounds.left + w;
            bounds.top = 0;
            bounds.bottom = h;
            list.add(bounds);
        }

        return list;
    }

    /**
     * Set bounds for the left textView including clip padding.
     * 
     * @param curViewBound
     *            current bounds.
     * @param curViewWidth
     *            width of the view.
     */
    private void clipViewOnTheLeft(final RectF curViewBound,
            final float curViewWidth, final int left) {
        curViewBound.left = left + this.mClipPadding;
        curViewBound.right = this.mClipPadding + curViewWidth;
    }

    /**
     * Set bounds for the right textView including clip padding.
     * 
     * @param curViewBound
     *            current bounds.
     * @param curViewWidth
     *            width of the view.
     */
    private void clipViewOnTheRight(final RectF curViewBound,
            final float curViewWidth, final int right) {
        curViewBound.right = right - this.mClipPadding;
        curViewBound.left = curViewBound.right - curViewWidth;
    }

    public float getClipPadding() {
        return this.mClipPadding;
    }

    public int getFooterColor() {
        return this.mPaintFooterLine.getColor();
    }

    public float getFooterIndicatorHeight() {
        return this.mFooterIndicatorHeight;
    }

    public float getFooterIndicatorPadding() {
        return this.mFooterPadding;
    }

    public IndicatorStyle getFooterIndicatorStyle() {
        return this.mFooterIndicatorStyle;
    }

    public float getFooterLineHeight() {
        return this.mFooterLineHeight;
    }

    @Override
    public int getPageCount() {
        return HomePage.NUMS_OF_PAGE;
    }

    @Override
    public int getPageWidth() {
        return this.mViewPager.getWidth();
    }

    public int getSelectedColor() {
        return this.mColorSelected;
    }

    public int getTextColor() {
        return this.mColorText;
    }

    public float getTextSize() {
        return this.mPaintText.getTextSize();
    }

    public float getTitlePadding() {
        return this.mTitlePadding;
    }

    public boolean isSelectedBold() {
        return this.mBoldText;
    }

    /**
     * Determines the height of this view
     * 
     * @param measureSpec
     *            A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(final int measureSpec) {
        float result = 0;
        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Calculate the text bounds
            final RectF bounds = new RectF();
            bounds.bottom = this.mPaintText.descent()
                    - this.mPaintText.ascent();
            result = (bounds.bottom - bounds.top) + this.mFooterLineHeight
                    + this.mFooterPadding;
            if (this.mFooterIndicatorStyle != IndicatorStyle.None) {
                result += this.mFooterIndicatorHeight;
            }
        }
        return (int) result;
    }

    /**
     * Determines the width of this view
     * 
     * @param measureSpec
     *            A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(final int measureSpec) {
        int result = 0;
        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(getClass().getSimpleName()
                    + " can only be used in EXACTLY mode.");
        }
        result = specSize;
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        // Calculate views bounds
        final ArrayList<RectF> bounds = calculateAllBounds(this.mPaintText);

        final int count = getPageCount();
        final int countMinusOne = count - 1;
        final float halfWidth = getWidth() / 2f;
        final int left = getLeft();
        final float leftClip = left + this.mClipPadding;
        final int width = getWidth();
        final int height = getHeight();
        final int right = left + width;
        final float rightClip = right - this.mClipPadding;
        final float offsetPercent = (1.0f * this.mCurrentOffset) / width;

        int page = this.mCurrentPage;
        if (this.mCurrentOffset > halfWidth) {
            page++;
        }
        final boolean currentSelected = (offsetPercent <= TitlePageIndicator.SELECTION_FADE_PERCENTAGE);
        final boolean currentBold = (offsetPercent <= TitlePageIndicator.BOLD_FADE_PERCENTAGE);
        final float selectedPercent = (TitlePageIndicator.SELECTION_FADE_PERCENTAGE - offsetPercent)
                / TitlePageIndicator.SELECTION_FADE_PERCENTAGE;

        // Verify if the current view must be clipped to the screen
        final RectF curPageBound = bounds.get(this.mCurrentPage);

        // fix null pointer exception
        if (curPageBound != null) {
            final float curPageWidth = curPageBound.right - curPageBound.left;
            if (curPageBound.left < leftClip) {
                // Try to clip to the screen (left side)
                clipViewOnTheLeft(curPageBound, curPageWidth, left);
            }
            if (curPageBound.right > rightClip) {
                // Try to clip to the screen (right side)
                clipViewOnTheRight(curPageBound, curPageWidth, right);
            }
        }

        // Left views starting from the current position
        if (this.mCurrentPage > 0) {
            for (int i = this.mCurrentPage - 1; i >= 0; i--) {
                final RectF bound = bounds.get(i);
                // Is left side is outside the screen
                if (bound.left < leftClip) {
                    final float w = bound.right - bound.left;
                    // Try to clip to the screen (left side)
                    clipViewOnTheLeft(bound, w, left);
                    // Except if there's an intersection with the right view
                    final RectF rightBound = bounds.get(i + 1);
                    // Intersection
                    if ((bound.right + this.mTitlePadding) > rightBound.left) {
                        bound.left = rightBound.left - w - this.mTitlePadding;
                        bound.right = bound.left + w;
                    }
                }
            }
        }
        // Right views starting from the current position
        if (this.mCurrentPage < countMinusOne) {
            for (int i = this.mCurrentPage + 1; i < count; i++) {
                final RectF bound = bounds.get(i);
                // If right side is outside the screen
                if (bound.right > rightClip) {
                    final float w = bound.right - bound.left;
                    // Try to clip to the screen (right side)
                    clipViewOnTheRight(bound, w, right);
                    // Except if there's an intersection with the left view
                    final RectF leftBound = bounds.get(i - 1);
                    // Intersection
                    if ((bound.left - this.mTitlePadding) < leftBound.right) {
                        bound.left = leftBound.right + this.mTitlePadding;
                        bound.right = bound.left + w;
                    }
                }
            }
        }

        // if(App.DEBUG){
        // Log.e(TAG,
        // "===============================================================");
        // Log.e(TAG, "onDraw() mCurrentPage="+mCurrentPage);
        // Log.e(TAG, "onDraw() ");
        // Log.e(TAG, "onDraw() ");
        // Log.e(TAG, "onDraw() ");
        // Log.e(TAG, "onDraw() ");
        // Log.e(TAG, "onDraw() ");
        // Log.e(TAG,
        // "===============================================================");
        // }

        // Now draw views
        for (int i = 0; i < count; i++) {
            // Get the title
            final RectF bound = bounds.get(i);
            // Only if one side is visible
            if (((bound.left > left) && (bound.left < right))
                    || ((bound.right > left) && (bound.right < right))) {
                final boolean currentPage = (i == page);
                // Only set bold if we are within bounds
                this.mPaintText.setFakeBoldText(currentPage && currentBold
                        && this.mBoldText);

                // Draw text as unselected
                this.mPaintText.setColor(this.mColorText);
                canvas.drawText(this.mTitleProvider.getTitle(i), bound.left,
                        bound.bottom, this.mPaintText);

                // If we are within the selected bounds draw the selected text
                if (currentPage && currentSelected) {
                    this.mPaintText.setColor(this.mColorSelected);
                    this.mPaintText
                            .setAlpha((int) ((this.mColorSelected >>> 24) * selectedPercent));
                    canvas.drawText(this.mTitleProvider.getTitle(i),
                            bound.left, bound.bottom, this.mPaintText);
                }
            }
        }

        // Draw the footer line
        // mPath = new Path();
        this.mPath.reset();
        this.mPath.moveTo(0, height - this.mFooterLineHeight);
        this.mPath.lineTo(width, height - this.mFooterLineHeight);
        this.mPath.close();
        canvas.drawPath(this.mPath, this.mPaintFooterLine);

        switch (this.mFooterIndicatorStyle) {
        case Triangle:
            this.mPath.moveTo(halfWidth, height - this.mFooterLineHeight
                    - this.mFooterIndicatorHeight);
            this.mPath.lineTo(halfWidth + this.mFooterIndicatorHeight, height
                    - this.mFooterLineHeight);
            this.mPath.lineTo(halfWidth - this.mFooterIndicatorHeight, height
                    - this.mFooterLineHeight);
            this.mPath.close();
            canvas.drawPath(this.mPath, this.mPaintFooterIndicator);
            break;

        case Underline:
            if (!currentSelected) {
                break;
            }

            final RectF underlineBounds = bounds.get(page);
            this.mPath.moveTo(underlineBounds.left
                    - this.mFooterIndicatorUnderlinePadding, height
                    - this.mFooterLineHeight);
            this.mPath.lineTo(underlineBounds.right
                    + this.mFooterIndicatorUnderlinePadding, height
                    - this.mFooterLineHeight);
            this.mPath.lineTo(underlineBounds.right
                    + this.mFooterIndicatorUnderlinePadding, height
                    - this.mFooterLineHeight - this.mFooterIndicatorHeight);
            this.mPath.lineTo(underlineBounds.left
                    - this.mFooterIndicatorUnderlinePadding, height
                    - this.mFooterLineHeight - this.mFooterIndicatorHeight);
            this.mPath.close();

            this.mPaintFooterIndicator.setAlpha((int) (0xFF * selectedPercent));
            canvas.drawPath(this.mPath, this.mPaintFooterIndicator);
            this.mPaintFooterIndicator.setAlpha(0xFF);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onMeasure(int, int)
     */
    @Override
    protected void onMeasure(final int widthMeasureSpec,
            final int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset,
            final int positionOffsetPixels) {
        this.mCurrentPage = position % HomePage.NUMS_OF_PAGE;
        this.mCurrentOffset = positionOffsetPixels;
        invalidate();
    }

    @Override
    public void onPageSelected(final int position) {
        this.mCurrentPage = position % HomePage.NUMS_OF_PAGE;
        invalidate();
    }

    @Override
    public void onRestoreInstanceState(final Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.mCurrentPage = savedState.currentPage;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState savedState = new SavedState(superState);
        savedState.currentPage = this.mCurrentPage;
        return savedState;
    }

    // @Override
    public boolean onTouchEvent2(final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            final int count = getPageCount();
            final int width = getWidth();
            final float halfWidth = width / 2f;
            final float sixthWidth = width / 6f;

            if ((this.mCurrentPage > 0)
                    && (event.getX() < (halfWidth - sixthWidth))) {
                this.mViewPager.setCurrentItem(this.mCurrentPage - 1);
                return true;
            } else if ((this.mCurrentPage < (count - 1))
                    && (event.getX() > (halfWidth + sixthWidth))) {
                this.mViewPager.setCurrentItem(this.mCurrentPage + 1);
                return true;
            }
        }

        return super.onTouchEvent(event);
    }

    public void setClipPadding(final float clipPadding) {
        this.mClipPadding = clipPadding;
        invalidate();
    }

    @Override
    public void setCurrentItem(final int item) {
        if (this.mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        this.mCurrentPage = item;
        invalidate();
    }

    public void setFooterColor(final int footerColor) {
        this.mPaintFooterLine.setColor(footerColor);
        this.mPaintFooterIndicator.setColor(footerColor);
        invalidate();
    }

    public void setFooterIndicatorHeight(final float footerTriangleHeight) {
        this.mFooterIndicatorHeight = footerTriangleHeight;
        invalidate();
    }

    public void setFooterIndicatorPadding(final float footerIndicatorPadding) {
        this.mFooterPadding = footerIndicatorPadding;
        invalidate();
    }

    public void setFooterIndicatorStyle(final IndicatorStyle indicatorStyle) {
        this.mFooterIndicatorStyle = indicatorStyle;
        invalidate();
    }

    public void setFooterLineHeight(final float footerLineHeight) {
        this.mFooterLineHeight = footerLineHeight;
        this.mPaintFooterLine.setStrokeWidth(this.mFooterLineHeight);
        invalidate();
    }

    public void setSelectedBold(final boolean selectedBold) {
        this.mBoldText = selectedBold;
        invalidate();
    }

    public void setSelectedColor(final int selectedColor) {
        this.mColorSelected = selectedColor;
        invalidate();
    }

    public void setTextColor(final int textColor) {
        this.mPaintText.setColor(textColor);
        this.mColorText = textColor;
        invalidate();
    }

    public void setTextSize(final float textSize) {
        this.mPaintText.setTextSize(textSize);
        invalidate();
    }

    public void setTitlePadding(final float titlePadding) {
        this.mTitlePadding = titlePadding;
        invalidate();
    }

    public void setTitleProvider(final TitleProvider provider) {
        this.mTitleProvider = provider;
    }

    @Override
    public void setViewPager(final ViewPager view) {
        if (view.getAdapter() == null) {
            throw new IllegalStateException(
                    "ViewPager does not have adapter instance.");
        }
        this.mViewPager = view;
        invalidate();
    }

    @Override
    public void setViewPager(final ViewPager view, final int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }
}
