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
package com.fanfou.app.opensource.cache;

import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

/**
 * @author mcxiaoke
 * @version 1.0 2011.09.23
 * @version 1.1 2011.09.27
 * @version 1.2 2011.12.13
 * 
 */
public interface ImageLoader {
    public static final String TAG = ImageLoader.class.getSimpleName();
    public static final int MESSAGE_FINISH = 0;
    public static final int MESSAGE_ERROR = 1;
    public static final String EXTRA_URL = "extra_url";

    void clearCache();

    void clearQueue();

    void displayImage(String key, ImageView imageView, int stubId);

    Bitmap getImage(String key, final Handler handler);

    void shutdown();

}
