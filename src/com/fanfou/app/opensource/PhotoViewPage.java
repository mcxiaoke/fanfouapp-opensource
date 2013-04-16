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
package com.fanfou.app.opensource;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.ui.imagezoom.ImageViewTouch;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.IOHelper;
import com.fanfou.app.opensource.util.ImageHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.08.28
 * @version 2.0 2011.10.12
 * @version 2.1 2011.10.27
 * @version 2.2 2011.11.09
 * @version 3.0 2011.11.16
 * @version 3.1 2011.11.17
 * @version 3.2 2011.11.22
 * 
 */
public class PhotoViewPage extends BaseActivity {

    private static final String TAG = PhotoViewPage.class.getSimpleName();
    private String mPhotoPath;
    private Bitmap bitmap;

    private ImageViewTouch mImageView;

    private void doSave() {
        final File file = new File(this.mPhotoPath);
        if (file.exists()) {
            final File dest = new File(IOHelper.getPhotoDir(this),
                    file.getName());
            if (dest.exists()) {
                CommonHelper.notify(this, "照片已保存到 " + dest.getAbsolutePath());
            } else {
                try {
                    IOHelper.copyFile(file, dest);
                    CommonHelper.notify(this,
                            "照片已保存到 " + dest.getAbsolutePath());
                } catch (final IOException e) {
                    if (AppContext.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.zoom_enter_2, R.anim.zoom_exit_2);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        this.mImageView = (ImageViewTouch) findViewById(R.id.photoview_pic);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoview);
        parseIntent(getIntent());

        if (TextUtils.isEmpty(this.mPhotoPath)) {
            finish();
            return;
        }
        if (AppContext.DEBUG) {
            Log.d(PhotoViewPage.TAG, "mPhotoPath=" + this.mPhotoPath);
        }

        try {
            this.bitmap = ImageHelper.loadFromPath(this, this.mPhotoPath, 1200,
                    1200);
            if (AppContext.DEBUG) {
                Log.d(PhotoViewPage.TAG,
                        "Bitmap width=" + this.bitmap.getWidth() + " height="
                                + this.bitmap.getHeight());
            }
            this.mImageView.setImageBitmapReset(this.bitmap, true);
        } catch (final IOException e) {
            if (AppContext.DEBUG) {
                Log.e(PhotoViewPage.TAG, "" + e);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuItem option = menu.add(0, BaseActivity.MENU_ID_SAVE,
                BaseActivity.MENU_ID_SAVE, "保存图片");
        option.setIcon(R.drawable.ic_menu_save);
        return true;
    }

    @Override
    protected void onDestroy() {
        ImageHelper.releaseBitmap(this.bitmap);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
        case MENU_ID_SAVE:
            doSave();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void parseIntent(final Intent intent) {
        final String action = intent.getAction();
        if (action == null) {
            this.mPhotoPath = intent.getStringExtra(Constants.EXTRA_URL);
        } else if (action.equals(Intent.ACTION_VIEW)) {
            final Uri uri = intent.getData();
            if (uri.getScheme().equals("content")) {
                this.mPhotoPath = IOHelper.getRealPathFromURI(this, uri);
            } else {
                this.mPhotoPath = uri.getPath();
            }
        }

    }

}
