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

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;

class RotateBitmap {

    public static final String TAG = "RotateBitmap";
    private Bitmap mBitmap;
    private int mRotation;
    private int mWidth;
    private int mHeight;
    private int mBitmapWidth;
    private int mBitmapHeight;

    public RotateBitmap(final Bitmap bitmap, final int rotation) {
        this.mRotation = rotation % 360;
        setBitmap(bitmap);
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public Matrix getRotateMatrix() {
        final Matrix matrix = new Matrix();
        if (this.mRotation != 0) {
            final int cx = this.mBitmapWidth / 2;
            final int cy = this.mBitmapHeight / 2;
            matrix.preTranslate(-cx, -cy);
            matrix.postRotate(this.mRotation);
            matrix.postTranslate(this.mWidth / 2, this.mHeight / 2);
        }

        return matrix;
    }

    public int getRotation() {
        return this.mRotation % 360;
    }

    public int getWidth() {
        return this.mWidth;
    }

    private void invalidate() {
        final Matrix matrix = new Matrix();
        final int cx = this.mBitmapWidth / 2;
        final int cy = this.mBitmapHeight / 2;
        matrix.preTranslate(-cx, -cy);
        matrix.postRotate(this.mRotation);
        matrix.postTranslate(cx, cx);

        final RectF rect = new RectF(0, 0, this.mBitmapWidth,
                this.mBitmapHeight);
        matrix.mapRect(rect);
        this.mWidth = (int) rect.width();
        this.mHeight = (int) rect.height();
    }

    public void recycle() {
        if (this.mBitmap != null) {
            this.mBitmap.recycle();
            this.mBitmap = null;
        }
    }

    public void setBitmap(final Bitmap bitmap) {
        this.mBitmap = bitmap;

        if (this.mBitmap != null) {
            this.mBitmapWidth = bitmap.getWidth();
            this.mBitmapHeight = bitmap.getHeight();
            invalidate();
        }
    }

    public void setRotation(final int rotation) {
        this.mRotation = rotation;
        invalidate();
    }
}
