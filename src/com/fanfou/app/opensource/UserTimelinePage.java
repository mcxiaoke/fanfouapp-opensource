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
import com.fanfou.app.opensource.db.FanFouProvider;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.service.FanfouServiceManager;
import com.fanfou.app.opensource.util.CommonHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.21
 * 
 */
public class UserTimelinePage extends BaseTimelineActivity {

    @Override
    protected void doRetrieveImpl(final Messenger messenger,
            final boolean isGetMore) {
        String sinceId = null;
        String maxId = null;
        if (isGetMore) {
            maxId = CommonHelper.getMaxId(this.mCursor);
        } else {
            sinceId = CommonHelper.getSinceId(this.mCursor);
        }
        FanfouServiceManager.doFetchUserTimeline(this, messenger, this.userId,
                sinceId, maxId);
    }

    @Override
    protected Cursor getCursor() {
        final String where = BasicColumns.TYPE + "=? AND " + StatusInfo.USER_ID
                + "=? ";
        final String[] whereArgs = new String[] { String.valueOf(getType()),
                this.userId };
        return managedQuery(StatusInfo.CONTENT_URI, StatusInfo.COLUMNS, where,
                whereArgs, FanFouProvider.ORDERBY_DATE_DESC);
    }

    @Override
    protected String getPageTitle() {
        return "消息";
    }

    protected int getType() {
        return Constants.TYPE_STATUSES_USER_TIMELINE;
    }

}
