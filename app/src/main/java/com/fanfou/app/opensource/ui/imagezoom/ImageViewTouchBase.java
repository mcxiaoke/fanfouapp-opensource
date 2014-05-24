/*******************************************************************************
 * Copyright Alessandro Crugnola
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
package com.fanfou.app.opensource.ui.imagezoom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class ImageViewTouchBase extends ImageView {

    protected enum Command {
        Center, Move, Zoom, Layout, Reset,
    };

    public interface OnBitmapChangedListener {

        void onBitmapChanged(Bitmap bitmap);
    };

    public static final String LOG_TAG = "image";

    public static float easeOut(float time, final float start, final float end,
            final float duration) {
        return (end * (((time = (time / duration) - 1) * time * time) + 1))
                + start;
    }

    protected Matrix mBaseMatrix = new Matrix();
    protected Matrix mSuppMatrix = new Matrix();
    protected Handler mHandler = new Handler();
    protected Runnable mOnLayoutRunnable = null;
    protected float mMaxZoom;
    protected final Matrix mDisplayMatrix = new Matrix();
    protected final float[] mMatrixValues = new float[9];

    protected int mThisWidth = -1, mThisHeight = -1;
    final protected RotateBitmap mBitmapDisplayed = new RotateBitmap(null, 0);

    final protected float MAX_ZOOM = 2.0f;

    private OnBitmapChangedListener mListener;

    public ImageViewTouchBase(final Context context) {
        super(context);
        init();
    }

    public ImageViewTouchBase(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void center(final boolean horizontal, final boolean vertical) {
        if (this.mBitmapDisplayed.getBitmap() == null) {
            return;
        }
        final RectF rect = getCenter(horizontal, vertical);
        if ((rect.left != 0) || (rect.top != 0)) {
            postTranslate(rect.left, rect.top);
        }
    }

    public void clear() {
        setImageBitmapReset(null, true);
    }

    public void dispose() {
        if (this.mBitmapDisplayed.getBitmap() != null) {
            if (!this.mBitmapDisplayed.getBitmap().isRecycled()) {
                this.mBitmapDisplayed.getBitmap().recycle();
            }
        }
        clear();
    }

    protected RectF getBitmapRect() {
        if (this.mBitmapDisplayed.getBitmap() == null) {
            return null;
        }
        final Matrix m = getImageViewMatrix();
        final RectF rect = new RectF(0, 0, this.mBitmapDisplayed.getBitmap()
                .getWidth(), this.mBitmapDisplayed.getBitmap().getHeight());
        m.mapRect(rect);
        return rect;
    }

    protected RectF getCenter(final boolean horizontal, final boolean vertical) {
        if (this.mBitmapDisplayed.getBitmap() == null) {
            return new RectF(0, 0, 0, 0);
        }
        final RectF rect = getBitmapRect();
        final float height = rect.height();
        final float width = rect.width();
        float deltaX = 0, deltaY = 0;
        if (vertical) {
            final int viewHeight = getHeight();
            if (height < viewHeight) {
                deltaY = ((viewHeight - height) / 2) - rect.top;
            } else if (rect.top > 0) {
                deltaY = -rect.top;
            } else if (rect.bottom < viewHeight) {
                deltaY = getHeight() - rect.bottom;
            }
        }
        if (horizontal) {
            final int viewWidth = getWidth();
            if (width < viewWidth) {
                deltaX = ((viewWidth - width) / 2) - rect.left;
            } else if (rect.left > 0) {
                deltaX = -rect.left;
            } else if (rect.right < viewWidth) {
                deltaX = viewWidth - rect.right;
            }
        }
        return new RectF(deltaX, deltaY, 0, 0);
    }

    public RotateBitmap getDisplayBitmap() {
        return this.mBitmapDisplayed;
    }

    protected Matrix getImageViewMatrix() {
        this.mDisplayMatrix.set(this.mBaseMatrix);
        this.mDisplayMatrix.postConcat(this.mSuppMatrix);
        return this.mDisplayMatrix;
    }

    public float getMaxZoom() {
        return this.mMaxZoom;
    }

    /**
     * Setup the base matrix so that the image is centered and scaled properly.
     * 
     * @param bitmap
     * @param matrix
     */
    protected void getProperBaseMatrix(final RotateBitmap bitmap,
            final Matrix matrix) {
        final float viewWidth = getWidth();
        final float viewHeight = getHeight();
        final float w = bitmap.getWidth();
        final float h = bitmap.getHeight();
        matrix.reset();
        final float widthScale = Math.min(viewWidth / w, this.MAX_ZOOM);
        final float heightScale = Math.min(viewHeight / h, this.MAX_ZOOM);
        final float scale = Math.min(widthScale, heightScale);
        matrix.postConcat(bitmap.getRotateMatrix());
        matrix.postScale(scale, scale);
        matrix.postTranslate((viewWidth - (w * scale)) / this.MAX_ZOOM,
                (viewHeight - (h * scale)) / this.MAX_ZOOM);
    }

    public float getScale() {
        return getScale(this.mSuppMatrix);
    }

    protected float getScale(final Matrix matrix) {
        return getValue(matrix, Matrix.MSCALE_X);
    }

    protected float getValue(final Matrix matrix, final int whichValue) {
        matrix.getValues(this.mMatrixValues);
        return this.mMatrixValues[whichValue];
    }

    protected void init() {
        setScaleType(ImageView.ScaleType.MATRIX);
    }

    protected float maxZoom() {
        if (this.mBitmapDisplayed.getBitmap() == null) {
            return 1F;
        }
        final float fw = (float) this.mBitmapDisplayed.getWidth()
                / (float) this.mThisWidth;
        final float fh = (float) this.mBitmapDisplayed.getHeight()
                / (float) this.mThisHeight;
        final float max = Math.max(fw, fh) * 4;
        return max;
    }

    @Override
    protected void onLayout(final boolean changed, final int left,
            final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.mThisWidth = right - left;
        this.mThisHeight = bottom - top;
        final Runnable r = this.mOnLayoutRunnable;
        if (r != null) {
            this.mOnLayoutRunnable = null;
            r.run();
        }
        if (this.mBitmapDisplayed.getBitmap() != null) {
            getProperBaseMatrix(this.mBitmapDisplayed, this.mBaseMatrix);
            setImageMatrix(Command.Layout, getImageViewMatrix());
        }
    }

    protected void onZoom(final float scale) {
    }

    protected void panBy(final float dx, final float dy) {
        final RectF rect = getBitmapRect();
        final RectF srect = new RectF(dx, dy, 0, 0);
        updateRect(rect, srect);
        postTranslate(srect.left, srect.top);
        center(true, true);
    }

    protected void postScale(final float scale, final float centerX,
            final float centerY) {
        this.mSuppMatrix.postScale(scale, scale, centerX, centerY);
        setImageMatrix(Command.Zoom, getImageViewMatrix());
    }

    protected void postTranslate(final float deltaX, final float deltaY) {
        this.mSuppMatrix.postTranslate(deltaX, deltaY);
        setImageMatrix(Command.Move, getImageViewMatrix());
    }

    public void scrollBy(final float x, final float y) {
        panBy(x, y);
    }

    protected void scrollBy(final float distanceX, final float distanceY,
            final float durationMs) {
        final float dx = distanceX;
        final float dy = distanceY;
        final long startTime = System.currentTimeMillis();
        this.mHandler.post(new Runnable() {

            float old_x = 0;
            float old_y = 0;

            @Override
            public void run() {
                final long now = System.currentTimeMillis();
                final float currentMs = Math.min(durationMs, now - startTime);
                final float x = ImageViewTouchBase.easeOut(currentMs, 0, dx,
                        durationMs);
                final float y = ImageViewTouchBase.easeOut(currentMs, 0, dy,
                        durationMs);
                panBy((x - this.old_x), (y - this.old_y));
                this.old_x = x;
                this.old_y = y;
                if (currentMs < durationMs) {
                    ImageViewTouchBase.this.mHandler.post(this);
                } else {
                    final RectF centerRect = getCenter(true, true);
                    if ((centerRect.left != 0) || (centerRect.top != 0)) {
                        scrollBy(centerRect.left, centerRect.top);
                    }
                }
            }
        });
    }

    @Override
    public void setImageBitmap(final Bitmap bitmap) {
        setImageBitmap(bitmap, 0);
    }

    /**
     * This is the ultimate method called when a new bitmap is set
     * 
     * @param bitmap
     * @param rotation
     */
    protected void setImageBitmap(final Bitmap bitmap, final int rotation) {
        super.setImageBitmap(bitmap);
        final Drawable d = getDrawable();
        if (d != null) {
            d.setDither(true);
        }
        this.mBitmapDisplayed.setBitmap(bitmap);
        this.mBitmapDisplayed.setRotation(rotation);
    }

    public void setImageBitmapReset(final Bitmap bitmap, final boolean reset) {
        setImageRotateBitmapReset(new RotateBitmap(bitmap, 0), reset);
    }

    public void setImageBitmapReset(final Bitmap bitmap, final int rotation,
            final boolean reset) {
        setImageRotateBitmapReset(new RotateBitmap(bitmap, rotation), reset);
    }

    protected void setImageMatrix(final Command command, final Matrix matrix) {
        setImageMatrix(matrix);
    }

    public void setImageRotateBitmapReset(final RotateBitmap bitmap,
            final boolean reset) {
        Log.d(ImageViewTouchBase.LOG_TAG, "setImageRotateBitmapReset");

        final int viewWidth = getWidth();
        if (viewWidth <= 0) {
            this.mOnLayoutRunnable = new Runnable() {

                @Override
                public void run() {
                    setImageBitmapReset(bitmap.getBitmap(),
                            bitmap.getRotation(), reset);
                }
            };
            return;
        }

        if (bitmap.getBitmap() != null) {
            getProperBaseMatrix(bitmap, this.mBaseMatrix);
            setImageBitmap(bitmap.getBitmap(), bitmap.getRotation());
        } else {
            this.mBaseMatrix.reset();
            setImageBitmap(null);
        }

        if (reset) {
            this.mSuppMatrix.reset();
        }

        setImageMatrix(Command.Reset, getImageViewMatrix());
        this.mMaxZoom = maxZoom();

        if (this.mListener != null) {
            this.mListener.onBitmapChanged(bitmap.getBitmap());
        }
    }

    public void setOnBitmapChangedListener(
            final OnBitmapChangedListener listener) {
        this.mListener = listener;
    }

    protected void updateRect(final RectF bitmapRect, final RectF scrollRect) {
        final float width = getWidth();
        final float height = getHeight();

        if ((bitmapRect.top >= 0) && (bitmapRect.bottom <= height)) {
            scrollRect.top = 0;
        }
        if ((bitmapRect.left >= 0) && (bitmapRect.right <= width)) {
            scrollRect.left = 0;
        }
        if (((bitmapRect.top + scrollRect.top) >= 0)
                && (bitmapRect.bottom > height)) {
            scrollRect.top = (int) (0 - bitmapRect.top);
        }
        if (((bitmapRect.bottom + scrollRect.top) <= (height - 0))
                && (bitmapRect.top < 0)) {
            scrollRect.top = (int) ((height - 0) - bitmapRect.bottom);
        }
        if ((bitmapRect.left + scrollRect.left) >= 0) {
            scrollRect.left = (int) (0 - bitmapRect.left);
        }
        if ((bitmapRect.right + scrollRect.left) <= (width - 0)) {
            scrollRect.left = (int) ((width - 0) - bitmapRect.right);
            // Log.d( LOG_TAG, "scrollRect(2): " + scrollRect.toString() );
        }
    }

    protected void zoomTo(final float scale) {
        final float cx = getWidth() / 2F;
        final float cy = getHeight() / 2F;
        zoomTo(scale, cx, cy);
    }

    public void zoomTo(final float scale, final float durationMs) {
        final float cx = getWidth() / 2F;
        final float cy = getHeight() / 2F;
        zoomTo(scale, cx, cy, durationMs);
    }

    protected void zoomTo(float scale, final float centerX, final float centerY) {
        if (scale > this.mMaxZoom) {
            scale = this.mMaxZoom;
        }
        final float oldScale = getScale();
        final float deltaScale = scale / oldScale;
        postScale(deltaScale, centerX, centerY);
        onZoom(getScale());
        center(true, true);
    }

    protected void zoomTo(final float scale, final float centerX,
            final float centerY, final float durationMs) {
        // Log.d( LOG_TAG, "zoomTo: " + scale + ", " + centerX + ": " + centerY
        // );
        final long startTime = System.currentTimeMillis();
        final float incrementPerMs = (scale - getScale()) / durationMs;
        final float oldScale = getScale();
        this.mHandler.post(new Runnable() {

            @Override
            public void run() {
                final long now = System.currentTimeMillis();
                final float currentMs = Math.min(durationMs, now - startTime);
                final float target = oldScale + (incrementPerMs * currentMs);
                zoomTo(target, centerX, centerY);
                if (currentMs < durationMs) {
                    ImageViewTouchBase.this.mHandler.post(this);
                } else {
                    // if ( getScale() < 1f ) {}
                }
            }
        });
    }
}
