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
package com.fanfou.app.opensource.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fanfou.app.opensource.db.Contents.DirectMessageInfo;
import com.fanfou.app.opensource.db.Contents.DraftInfo;
import com.fanfou.app.opensource.db.Contents.StatusInfo;
import com.fanfou.app.opensource.db.Contents.UserInfo;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.20
 * @version 2.0 2011.10.21
 * @version 3.0 2011.10.26
 * @version 3.1 2011.10.27
 * @version 3.2 2011.10.28
 * @version 3.3 2011.11.07
 * @version 3.4 2011.11.10
 * @version 3.5 2011.11.18
 * @version 3.6 2011.11.23
 * 
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    public static final String TAG = "SQLiteHelper";

    public static final String DATABASE_NAME = "fanfou.db";
    public static final int DATABASE_VERSION = 11;

    /**
     * @param context
     * @param name
     * @param factory
     * @param version
     */
    public SQLiteHelper(final Context context) {
        super(context, SQLiteHelper.DATABASE_NAME, null,
                SQLiteHelper.DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(StatusInfo.CREATE_TABLE);
        db.execSQL(UserInfo.CREATE_TABLE);
        db.execSQL(DirectMessageInfo.CREATE_TABLE);
        db.execSQL(DraftInfo.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
            final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + StatusInfo.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserInfo.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DirectMessageInfo.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DraftInfo.TABLE_NAME);
        onCreate(db);

    }

}
