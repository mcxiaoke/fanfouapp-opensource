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

import android.database.Cursor;
import android.os.Messenger;

import com.fanfou.app.opensource.db.Contents.BasicColumns;
import com.fanfou.app.opensource.db.Contents.StatusInfo;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.service.FanfouServiceManager;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.21
 * @version 1.1 2011.10.24
 * 
 */
public class UserFavoritesPage extends BaseTimelineActivity {
    private int page = 1;

    @Override
    protected void doRetrieveImpl(final Messenger messenger,
            final boolean isGetMore) {
        if (isGetMore) {
            this.page++;
        } else {
            this.page = 1;
        }
        FanfouServiceManager.doFetchFavorites(this, messenger, this.page,
                this.userId);
    }

    @Override
    protected Cursor getCursor() {
        final String where = BasicColumns.TYPE + "=? AND "
                + BasicColumns.OWNER_ID + "=? ";
        final String[] whereArgs = new String[] { String.valueOf(getType()),
                this.userId };
        return managedQuery(StatusInfo.CONTENT_URI, StatusInfo.COLUMNS, where,
                whereArgs, null);
    }

    @Override
    protected String getPageTitle() {
        return "收藏";
    }

    protected int getType() {
        return Constants.TYPE_FAVORITES_LIST;
    }

}
