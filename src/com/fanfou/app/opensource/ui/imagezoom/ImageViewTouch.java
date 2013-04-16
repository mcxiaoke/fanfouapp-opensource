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
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.fanfou.app.opensource.ui.widget.ScaleGestureDetector;

/**
 * @author mcxiaoke
 * @version 1.1 2011.11.22
 * 
 */
public class ImageViewTouch extends ImageViewTouchBase {

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(final MotionEvent e) {
            final float scale = getScale();
            float targetScale = scale;
            targetScale = onDoubleTapPost(scale, getMaxZoom());
            targetScale = Math.min(getMaxZoom(),
                    Math.max(targetScale, ImageViewTouch.MIN_ZOOM));
            ImageViewTouch.this.mCurrentScaleFactor = targetScale;
            zoomTo(targetScale, e.getX(), e.getY(), 200);
            invalidate();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2,
                final float velocityX, final float velocityY) {
            if ((e1.getPointerCount() > 1) || (e2.getPointerCount() > 1)) {
                return false;
            }
            if (ImageViewTouch.this.mScaleDetector.isInProgress()) {
                return false;
            }

            final float diffX = e2.getX() - e1.getX();
            final float diffY = e2.getY() - e1.getY();

            if ((Math.abs(velocityX) > 800) || (Math.abs(velocityY) > 800)) {
                scrollBy(diffX / 2, diffY / 2, 300);
                invalidate();
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
                final float distanceX, final float distanceY) {
            if ((e1 == null) || (e2 == null)) {
                return false;
            }
            if ((e1.getPointerCount() > 1) || (e2.getPointerCount() > 1)) {
                return false;
            }
            if (ImageViewTouch.this.mScaleDetector.isInProgress()) {
                return false;
            }
            if (getScale() == 1f) {
                return false;
            }
            scrollBy(-distanceX, -distanceY);
            invalidate();
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {

        @SuppressWarnings("unused")
        @Override
        public boolean onScale(final ScaleGestureDetector detector) {
            final float span = detector.getCurrentSpan()
                    - detector.getPreviousSpan();
            float targetScale = ImageViewTouch.this.mCurrentScaleFactor
                    * detector.getScaleFactor();
            if (true) {
                targetScale = Math.min(getMaxZoom(),
                        Math.max(targetScale, ImageViewTouch.MIN_ZOOM));
                zoomTo(targetScale, detector.getFocusX(), detector.getFocusY());
                ImageViewTouch.this.mCurrentScaleFactor = Math.min(
                        getMaxZoom(),
                        Math.max(targetScale, ImageViewTouch.MIN_ZOOM));
                ImageViewTouch.this.mDoubleTapDirection = 1;
                invalidate();
                return true;
            }
            return false;
        }
    }

    static final float MIN_ZOOM = 0.9f;
    protected ScaleGestureDetector mScaleDetector;
    protected GestureDetector mGestureDetector;
    protected int mTouchSlop;
    protected float mCurrentScaleFactor;
    protected float mScaleFactor;
    protected int mDoubleTapDirection;

    protected GestureListener mGestureListener;

    protected ScaleListener mScaleListener;

    public ImageViewTouch(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        super.init();
        this.mTouchSlop = ViewConfiguration.getTouchSlop();
        this.mGestureListener = new GestureListener();
        this.mScaleListener = new ScaleListener();

        // compatibility for api 7
        this.mScaleDetector = new ScaleGestureDetector(getContext(),
                this.mScaleListener);
        // mGestureDetector = new GestureDetector( getContext(),
        // mGestureListener, null, true );// api>=8
        this.mGestureDetector = new GestureDetector(getContext(),
                this.mGestureListener, null);
        this.mCurrentScaleFactor = 1f;
        this.mDoubleTapDirection = 1;
    }

    protected float onDoubleTapPost(final float scale, final float maxZoom) {
        if (this.mDoubleTapDirection == 1) {
            if ((scale + (this.mScaleFactor * 2)) <= maxZoom) {
                return scale + this.mScaleFactor;
            } else {
                this.mDoubleTapDirection = -1;
                return maxZoom;
            }
        } else {
            this.mDoubleTapDirection = 1;
            return 1f;
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        this.mScaleDetector.onTouchEvent(event);
        if (!this.mScaleDetector.isInProgress()) {
            this.mGestureDetector.onTouchEvent(event);
        }
        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_UP:
            if (getScale() < 1f) {
                zoomTo(1f, 50);
            }
            break;
        }
        return true;
    }

    @Override
    protected void onZoom(final float scale) {
        super.onZoom(scale);
        if (!this.mScaleDetector.isInProgress()) {
            this.mCurrentScaleFactor = scale;
        }
    }

    @Override
    public void setImageRotateBitmapReset(final RotateBitmap bitmap,
            final boolean reset) {
        super.setImageRotateBitmapReset(bitmap, reset);
        this.mScaleFactor = getMaxZoom() / 3;
    }
}
