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

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fanfou.app.opensource.api.bean.DirectMessage;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.db.Contents.BasicColumns;
import com.fanfou.app.opensource.db.Contents.DirectMessageInfo;
import com.fanfou.app.opensource.db.Contents.StatusInfo;
import com.fanfou.app.opensource.db.Contents.UserInfo;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.util.CommonHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.01
 * @version 1.1 2011.05.01
 * @version 1.2 2011.05.02
 * @version 2.0 2011.05.25
 * @version 2.1 2011.12.19
 */
public class Database {
    /**
     * 删除指定类型的旧消息
     * 
     * @param type
     * @return
     */
    private static final int STATUS_STORE_MAX = 20;

    public static synchronized Database getInstance(final Context context) {
        if (Database.instance == null) {
            return new Database(context);
        }
        return Database.instance;
    }

    /**
     * 压缩数据库，删除旧消息
     * 
     * @param context
     * @param type
     */
    public static void trimDB(final Context context, final int type) {
        final Database db = Database.getInstance(context);
        final int sum = db.statusCountByType(type);
        if (sum > Database.STATUS_STORE_MAX) {
            db.statusDeleteOld(type);
        }
    }

    private final Context mContext;

    private static Database instance;

    /*************************************** 消息操作 ****************************************************/
    /**
     * 消息操作 1. 写入单条消息 2. 批量写入消息 3. 删除单条消息 4. 批量删除消息 5. 删除指定类型的消息 6.
     * 删除100条以前的指定类型消息 7. 删除全部消息 8. 更新单条消息 9. 批量更新消息属性 10. 替换单条消息 11. 批量替换消息 12.
     * 查询单条消息，返回Status对象 13. 查询批量消息，返回Status列表，返回Cursor 14. 查询指定类型消息，同上 15.
     * 查询指定用户消息，同上 16. 查询指定时间消息，同上 17. 查询消息数量 18. 查询指定类型消息数量 19. 查询指定用户消息数量
     * 
     * */

    private final SQLiteHelper mSQLiteHelper;

    private Database(final Context context) {
        this.mContext = context;
        this.mSQLiteHelper = new SQLiteHelper(this.mContext);
        Database.instance = this;
    }

    public long clearDirectMessages() {
        long result = -1;
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            result = db.delete(DirectMessageInfo.TABLE_NAME, null, null);
        } catch (final Exception e) {
            e.printStackTrace();
            result = -1;
        } finally {
            db.close();
        }

        return result;
    }

    public long clearStatuses() {
        long result = -1;
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        try {
            result = db.delete(StatusInfo.TABLE_NAME, null, null);
        } catch (final Exception e) {
            e.printStackTrace();
            result = -1;
        }
        db.close();

        return result;
    }

    public long clearUsers() {
        long result = -1;
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        result = db.delete(UserInfo.TABLE_NAME, null, null);
        db.close();
        return result;
    }

    public int delete(final String table, final String where,
            final String[] whereArgs) {
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        final int count = db.delete(table, where, whereArgs);
        db.close();
        return count;
    }

    public long deleteDirectMessage(final String id) {

        long result = -1;
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        try {
            final String whereClause = BasicColumns.ID + "=?";
            final String[] whereArgs = new String[] { id };
            result = db.delete(DirectMessageInfo.TABLE_NAME, whereClause,
                    whereArgs);
        } catch (final Exception e) {
            e.printStackTrace();
            result = -1;
        } finally {
            db.close();
        }

        return result;
    }

    public long deleteUser(final String id) {

        long result = -1;
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        try {
            final String whereClause = BasicColumns.ID + "=?";
            final String[] whereArgs = new String[] { id };
            result = db.delete(UserInfo.TABLE_NAME, whereClause, whereArgs);
        } catch (final Exception e) {
            e.printStackTrace();
            result = -1;
        } finally {
            db.close();
        }

        return result;
    }

    public Cursor getDirectMessageCusor(final String where,
            final String[] whereArgs, final String orderBy) {
        return queryCommon(DirectMessageInfo.TABLE_NAME,
                DirectMessageInfo.COLUMNS, where, whereArgs, orderBy);
    }

    public String getDirectMessagesMaxIdInDB() {
        return null;
    }

    public String getDirectMessagesMinIdInDB() {
        return null;
    }

    public String getMentionsMaxIdInDB() {
        return null;
    }

    public String getMentionsMinIdInDB() {
        return null;
    }

    public Cursor getStatusCusor(final String where, final String[] whereArgs,
            final String orderBy) {
        return queryCommon(StatusInfo.TABLE_NAME, StatusInfo.COLUMNS, where,
                whereArgs, orderBy);
    }

    public String getStatusMaxIdInDB() {
        return null;
    }

    public String getStatusMinIdInDB() {
        return null;
    }

    public Cursor getUserCusor(final String where, final String[] whereArgs,
            final String orderBy) {
        return queryCommon(UserInfo.TABLE_NAME, UserInfo.COLUMNS, where,
                whereArgs, orderBy);
    }

    public long insert(final String table, final String nullColumnHack,
            final ContentValues values) {
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        final long rowId = db.insert(table, nullColumnHack, values);
        db.close();
        return rowId;
    }

    public long insertDirectMessage(final DirectMessage message) {
        long result = -1;
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();

        try {
            db.beginTransaction();
            result = db.insert(DirectMessageInfo.TABLE_NAME, BasicColumns.ID,
                    message.toContentValues());
            db.setTransactionSuccessful();
        } catch (final Exception e) {
            e.printStackTrace();
            result = -1;
        } finally {
            db.endTransaction();
            db.close();
        }

        return result;
    }

    public int insertDirectMessages(final List<DirectMessage> messages) {
        if (CommonHelper.isEmpty(messages)) {
            return 0;
        }

        int result = messages.size();
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            for (int i = messages.size() - 1; i >= 0; i--) {
                final DirectMessage dm = messages.get(i);
                final long id = db.insert(DirectMessageInfo.TABLE_NAME, null,
                        dm.toContentValues());
                if (id == -1) {
                    result--;
                }
            }
            db.setTransactionSuccessful();
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
        return result;
    }

    public long insertUser(final User user) {
        return insert(UserInfo.TABLE_NAME, null, user.toContentValues());
    }

    public int insertUsers(final List<User> users) {
        log("insertUsers() size=" + users.size());
        if (CommonHelper.isEmpty(users)) {
            return 0;
        }

        int result = users.size();
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            for (int i = users.size() - 1; i >= 0; i--) {
                final User u = users.get(i);
                final long id = db.insert(UserInfo.TABLE_NAME, null,
                        u.toContentValues());
                if (id == -1) {
                    log("insertUsers() user.id=" + u.id);
                    result--;
                }
            }
            db.setTransactionSuccessful();
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
        return result;
    }

    private void log(final String message) {
        // Log.e(tag, message);
    }

    public Cursor query(final String table, final String[] columns,
            final String selection, final String[] selectionArgs,
            final String groupBy, final String having, final String orderBy,
            final String limit) {
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        return db.query(table, columns, selection, selectionArgs, groupBy,
                having, null, limit);
    }

    public List<DirectMessage> queryAllDirectMessages() {
        return queryDirectMessages(-1);
    }

    public List<Status> queryAllStatuses() {
        return queryStatuses(-1);
    }

    public List<User> queryAllUsers() {
        return queryUsers(-1);
    }

    private Cursor queryCommon(final String table, final String[] columns,
            final String where, final String[] whereArgs, final String orderBy) {
        return query(table, columns, where, whereArgs, null, null, orderBy,
                null);
    }

    public Cursor queryDirectMessage(final String id) {
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        final String selection = BasicColumns.ID + "=?";
        final String[] selectionArgs = new String[] { id };
        final Cursor c = db.query(DirectMessageInfo.TABLE_NAME,
                DirectMessageInfo.COLUMNS, selection, selectionArgs, null,
                null, null);
        return c;
    }

    public DirectMessage queryDirectMessageById(final String id) {
        log("queryDirectMessageById() id=" + id);
        final Cursor c = queryDirectMessage(id);
        if (c != null) {
            log("queryDirectMessageById() cursor.length=" + c.getCount());
            c.moveToFirst();
            if (c.getCount() > 0) {
                return DirectMessage.parse(c);
            }
            c.close();
        }
        return null;
    }

    public List<DirectMessage> queryDirectMessages(final int count) {
        return queryDirectMessages(count, 0);
    }

    public List<DirectMessage> queryDirectMessages(final int count,
            final int offset) {
        log("queryDirectMessages() count=" + count + " offset=" + offset);
        String limit = null;
        // if(count<0||offset<0){
        // throw new IllegalArgumentException("查询数量和偏移量都不能为负");
        // }
        if (count > 0) {
            if (offset > 0) {
                limit = String.valueOf(offset) + " ," + String.valueOf(count);
            } else {
                limit = String.valueOf(count);
            }
        }
        final List<DirectMessage> dms = new ArrayList<DirectMessage>();
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        final Cursor c = db.query(DirectMessageInfo.TABLE_NAME,
                DirectMessageInfo.COLUMNS, null, null, null, null, null, limit);
        if (c != null) {
            log("queryDirectMessages() cursor.size=" + c.getCount());
            c.moveToFirst();
            while (!c.isAfterLast()) {
                final DirectMessage dm = DirectMessage.parse(c);
                log("queryDirectMessages() status: id=" + dm.id);
                dms.add(dm);
                c.moveToNext();
            }
        }
        return dms;
    }

    public Cursor queryStatus(final String id) {
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        final String selection = BasicColumns.ID + "=?";
        final String[] selectionArgs = new String[] { id };
        return db.query(StatusInfo.TABLE_NAME, StatusInfo.COLUMNS, selection,
                selectionArgs, null, null, null);
    }

    public Status queryStatusById(final String id) {
        log("queryStatusById() id=" + id);
        final Cursor c = queryStatus(id);
        if (c != null) {
            log("queryStatusById() cursor.length=" + c.getCount());
            c.moveToFirst();
            if (c.getCount() > 0) {
                return Status.parse(c);
            }
            c.close();
        }
        return null;
    }

    public List<Status> queryStatuses(final int count) {
        return queryStatuses(count, 0);
    }

    public List<Status> queryStatuses(final int count, final int offset) {
        log("queryStatuses() count=" + count + " offset=" + offset);
        String limit = null;
        // if(count<0||offset<0){
        // throw new IllegalArgumentException("查询数量和偏移量都不能为负");
        // }
        if (count > 0) {
            if (offset > 0) {
                limit = String.valueOf(offset) + " ," + String.valueOf(count);
            } else {
                limit = String.valueOf(count);
            }
        }
        final List<Status> statuses = new ArrayList<Status>();
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        final Cursor c = db.query(StatusInfo.TABLE_NAME, StatusInfo.COLUMNS,
                null, null, null, null, null, limit);
        if (c != null) {
            log("queryStatuses() cursor.size=" + c.getCount());
            c.moveToFirst();
            while (!c.isAfterLast()) {
                final Status s = Status.parse(c);
                log("queryStatuses() status: id=" + s.id);
                statuses.add(s);
                c.moveToNext();
            }
            c.close();
        }
        db.close();
        return statuses;
    }

    public Cursor queryUser(final String id) {
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        final String selection = BasicColumns.ID + "=?";
        final String[] selectionArgs = new String[] { id };
        return db.query(UserInfo.TABLE_NAME, UserInfo.COLUMNS, selection,
                selectionArgs, null, null, null);
    }

    public User queryUserById(final String id) {
        log("queryUserById() id=" + id);
        final Cursor c = queryUser(id);
        if (c != null) {
            log("queryUserById() cursor.length=" + c.getCount());
            c.moveToFirst();
            if (c.getCount() > 0) {
                return User.parse(c);
            }
            c.close();
        }
        return null;
    }

    public List<User> queryUsers(final int count) {
        return queryUsers(count, 0);
    }

    // select id from status order by created_at desc limit 5,3;
    // 偏移查询语法为 limit offset,count
    // 例如 limit 5,3 表示偏移量为5，条目数限制为3
    public List<User> queryUsers(final int count, final int offset) {
        log("queryUsers() count=" + count + " offset=" + offset);
        String limit = null;
        if (count > 0) {
            if (offset > 0) {
                limit = String.valueOf(offset) + " ," + String.valueOf(count);
            } else {
                limit = String.valueOf(count);
            }
        }
        final List<User> users = new ArrayList<User>();
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        final Cursor c = db.query(UserInfo.TABLE_NAME, UserInfo.COLUMNS, null,
                null, null, null, null, limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            final User u = User.parse(c);
            log("queryUsers() get user: id=" + u.id);
            users.add(u);
            c.moveToNext();
        }
        db.close();
        log("queryUsers() result count=" + users.size());
        return users;
    }

    /**
     * 删除全部消息
     * 
     * @return
     */
    public long statusClear() {
        return statusDeleteAll();
    }

    /**
     * 查询所有消息数量
     * 
     * @return
     */
    public int statusCount() {
        return statusCountByType(Constants.TYPE_NONE);
    }

    /**
     * 根据类型查询消息数量
     * 
     * @param type
     * @return
     */
    public int statusCountByType(final int type) {
        int result = -1;
        String sql = "SELECT COUNT(" + BasicColumns.ID + ") FROM "
                + StatusInfo.TABLE_NAME;
        if (type == Constants.TYPE_NONE) {
            sql += " ;";
        } else {
            sql += " WHERE " + BasicColumns.TYPE + "=" + type + ";";
        }
        final SQLiteDatabase db = this.mSQLiteHelper.getReadableDatabase();
        try {
            // String[] columns=new String[]{StatusInfo.ID};
            // String where=null;
            // String[] whereArgs=null;
            // if(type!=Status.TYPE_NONE){
            // where=StatusInfo.TYPE + "=?";
            // whereArgs=new String[]{String.valueOf(type)};
            // }
            // Cursor c=db.query(StatusInfo.TABLE_NAME, columns, where,
            // whereArgs, null, null, null);
            log("statusCountByType() sql=" + sql);
            final Cursor c = db.rawQuery(sql, null);
            if (c != null) {
                c.moveToFirst();
                result = c.getInt(0);
            }
            // 方法二
            // String where=StatusInfo.TYPE+"=?";
            // String[] whereArgs=new String[]{String.valueOf(type)};
            // Cursor c2= db.query(StatusInfo.TABLE_NAME, new
            // String[]{StatusInfo.ID}, where, whereArgs, null, null, null);
            // if(c2!=null){
            // result=c.getCount();
            // }
        } catch (final Exception e) {
            e.printStackTrace();
            result = 0;
        } finally {
            db.close();
        }
        log("statusCountByType() type=" + type + " result=" + result);
        return result;
    }

    /**
     * 删除全部消息
     * 
     * @return
     */
    public long statusDeleteAll() {
        return statusDeleteByCondition(null, null);
    }

    /**
     * 根据条件批量删除消息
     * 
     * @param where
     * @param whereArgs
     * @return
     */
    private long statusDeleteByCondition(final String where,
            final String[] whereArgs) {
        long result = -1;
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        try {
            result = db.delete(StatusInfo.TABLE_NAME, where, whereArgs);
        } catch (final Exception e) {
            e.printStackTrace();
            result = -1;
        } finally {
            db.close();
        }
        return result;
    }

    /**
     * 批量删除某一日期之前的消息
     * 
     * @param maxDate
     *            临界日期
     * @return
     */
    public long statusDeleteByDate(final int maxDate) {
        final String where = StatusInfo.USER_ID + "<?";
        final String[] whereArgs = new String[] { String.valueOf(maxDate) };
        return statusDeleteByCondition(where, whereArgs);
    }

    /**
     * 单条消息，从数据库删除
     * 
     * @param id
     *            消息ID
     * @return
     */
    public long statusDeleteById(final String id) {
        final String where = BasicColumns.ID + "=?";
        final String[] whereArgs = new String[] { id };
        return statusDeleteByCondition(where, whereArgs);
    }

    /**
     * 批量删除指定类型的消息
     * 
     * @param type
     * @return
     */
    public long statusDeleteByType(final int type) {
        final String where = BasicColumns.TYPE + "=?";
        final String[] whereArgs = new String[] { String.valueOf(type) };
        return statusDeleteByCondition(where, whereArgs);
    }

    /**
     * 批量删除某一用户的消息
     * 
     * @param userId
     *            用户ID
     * @return
     */
    public long statusDeleteByUserId(final String userId) {
        final String where = StatusInfo.USER_ID + "=?";
        final String[] whereArgs = new String[] { userId };
        return statusDeleteByCondition(where, whereArgs);
    }

    /**
     * 删除所有的Home类型的消息
     * 
     * @return
     */
    public long statusDeleteHome() {
        final String where = BasicColumns.TYPE + "=?";
        final String[] whereArgs = new String[] { String
                .valueOf(Constants.TYPE_STATUSES_HOME_TIMELINE) };
        return statusDeleteByCondition(where, whereArgs);
    }

    /**
     * 删除所有的Mention类新的消息
     * 
     * @return
     */
    public long statusDeleteMention() {
        final String where = BasicColumns.TYPE + "=?";
        final String[] whereArgs = new String[] { String
                .valueOf(Constants.TYPE_STATUSES_MENTIONS) };
        return statusDeleteByCondition(where, whereArgs);
    }

    public boolean statusDeleteOld(final int type) {
        // select created_at,id,text from status order by created_at desc limit
        // 1 offset 10;
        //
        // select id from status order by created_at desc limit 5,3;
        // 偏移查询语法为 limit offset,count
        // 例如 limit 5,3 表示偏移量为5，条目数限制为3
        // SELECT MAX（column） 表示查询改列的最大值，相应的还有
        // MIN(column) 最小值 COUNT(*) 条目数量
        // select id,text,created_at from status where created_at <
        // (select max(created_at) from status order by created_at desc limit
        // 10);
        // String sql2 = "DELETE FROM " + StatusInfo.TABLE_NAME + " WHERE "
        // + StatusInfo.CREATED_AT + " < ";
        // String conditionSql = "(SELECT MAX(" + StatusInfo.CREATED_AT
        // + ") FROM " + StatusInfo.TABLE_NAME + " ORDER BY "
        // + StatusInfo.CREATED_AT + " DESC LIMIT " + STATUS_STORE_MAX
        // + ")";
        log("statusDeleteOld()");
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        try {
            // String countSql = "SELECT id FROM "+ StatusInfo.TABLE_NAME;
            // if (type == Status.TYPE_NONE) {
            // countSql += " ;";
            // } else {
            // countSql += " WHERE " + StatusInfo.TYPE + "=" + type + ";";
            // }
            //
            // log("statusDeleteOld() countSql=" + countSql);
            //
            // Cursor aaaa = db.rawQuery(countSql, null);
            //
            // if (aaaa == null) {
            // log("statusDeleteOld() c0=null");
            // aaaa.close();
            // db.close();
            // return false;
            // }

            // aaaa.moveToFirst();
            // int bbb = aaaa.getInt(aaaa.getColumnIndex("id"));
            // int ccc=aaaa.getCount();

            // log("statusDeleteOld() countResult=" + ccc);
            // if (ccc <= STATUS_STORE_MAX) {
            // aaaa.close();
            // db.close();
            // return false;
            // }
            // aaaa.close();

            String where = " " + BasicColumns.CREATED_AT + " < " + " (SELECT "
                    + BasicColumns.CREATED_AT + " FROM "
                    + StatusInfo.TABLE_NAME;

            // String sql = "DELETE FROM " + StatusInfo.TABLE_NAME + " WHERE ";
            // + StatusInfo.CREATED_AT + " < " + " (SELECT "
            // + StatusInfo.CREATED_AT + " FROM " + StatusInfo.TABLE_NAME;

            // String sql = "SELECT id,created_at,text FROM " +
            // StatusInfo.TABLE_NAME + " WHERE "
            // + StatusInfo.CREATED_AT + " < "
            // + " (SELECT "+ StatusInfo.CREATED_AT
            // + " FROM " + StatusInfo.TABLE_NAME;

            if (type != Constants.TYPE_NONE) {
                where += " WHERE " + BasicColumns.TYPE + " = " + type + " ";
            }
            where += " ORDER BY " + BasicColumns.CREATED_AT
                    + " DESC LIMIT 1 OFFSET " + Database.STATUS_STORE_MAX + ")";

            if (type != Constants.TYPE_NONE) {
                where += " AND " + BasicColumns.TYPE + " = " + type + " ";
            }
            // sql+=where;
            // log("statusDeleteOld() type=" + type);
            log("statusDeleteOld() where=[" + where + "]");
            // log("statusDeleteOld() sql=[" + sql+"]");
            // Cursor c = db.rawQuery(sql, null);
            final int rs = db.delete(StatusInfo.TABLE_NAME, where, null);
            log("statusDeleteOld() deleted count=" + rs);
            // if (c != null) {
            // c.moveToFirst();
            // int num = c.getCount();
            // log("statusDeleteOld() status count=" + num);
            // while (!c.isAfterLast()) {
            // Status s = Status.parse(c);
            // log("statusDeleteOld() status=" + s);
            // c.moveToNext();
            // }
            // }
        } finally {
            db.close();
        }

        return true;
    }

    /**
     * 替换单条消息
     * 
     * @param status
     * @return
     */
    public long statusReplace(final Status status) {
        long result = -1;
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        try {
            result = db.replace(StatusInfo.TABLE_NAME, null,
                    status.toContentValues());
        } catch (final Exception e) {
            e.printStackTrace();
            result = -1;
        } finally {
            db.close();
        }
        return result;
    }

    /**
     * 批量更新指定消息
     * 
     * @param statuses
     * @param cv
     * @return
     */
    public long statusUpdate(final List<Status> statuses, final ContentValues cv) {
        for (final Status status : statuses) {
            statusUpdateById(status.id, cv);
        }
        return 1;

    }

    /**
     * 更新消息数据
     * 
     * @param status
     * @return
     */
    public long statusUpdate(final Status status) {
        return statusUpdateById(status.id, status.toContentValues());
    }

    /**
     * 根据条件批量更新消息
     * 
     * @param where
     * @param whereArgs
     * @param values
     * @return
     */
    public long statusUpdate(final String where, final String[] whereArgs,
            final ContentValues values) {
        long result = -1;
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        try {
            result = db.update(StatusInfo.TABLE_NAME, values, where, whereArgs);
        } catch (final Exception e) {
            e.printStackTrace();
            result = -1;
        }
        return result;
    }

    /**
     * 根据数值列更新ID对应的消息
     * 
     * @param id
     *            消息ID
     * @param cv
     *            需要更新的列
     * @return
     */
    public long statusUpdateById(final String id, final ContentValues values) {
        final String where = BasicColumns.ID + "=?";
        final String[] whereArgs = new String[] { id };
        return statusUpdate(where, whereArgs, values);
    }

    /**
     * 批量消息，写入数据库
     * 
     * @param statuses
     *            消息列表
     * @return
     */
    public int statusWrite(final List<Status> statuses) {
        if (CommonHelper.isEmpty(statuses)) {
            return 0;
        }
        int result = statuses.size();
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            for (int i = statuses.size() - 1; i >= 0; i--) {
                final Status s = statuses.get(i);
                final long id = db.insert(StatusInfo.TABLE_NAME, null,
                        s.toContentValues());
                if (id == -1) {
                    result--;
                }
            }
            db.setTransactionSuccessful();
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
        return result;
    }

    /**
     * 单条消息，写入数据库
     * 
     * @param status
     *            消息对象
     * @return
     */
    public long statusWrite(final Status status) {
        long result = -1;
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            result = db.insert(StatusInfo.TABLE_NAME, BasicColumns.ID,
                    status.toContentValues());
            db.setTransactionSuccessful();
        } catch (final Exception e) {
            e.printStackTrace();
            result = -1;
        } finally {
            db.endTransaction();
            db.close();
        }
        return result;
    }

    public int update(final String table, final ContentValues values,
            final String where, final String[] whereArgs) {
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        final int count = db.update(table, values, where, whereArgs);
        db.close();
        return count;
    }

    public long updateDirectMessage(final DirectMessage message) {
        return updateDirectMessage(message.id, message.toContentValues());
    }

    public long updateDirectMessage(final String id, final ContentValues cv) {
        long result = -1;
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            final String whereClause = BasicColumns.ID + "=?";
            final String[] whereArgs = new String[] { id };
            result = db.update(DirectMessageInfo.TABLE_NAME, cv, whereClause,
                    whereArgs);
            db.setTransactionSuccessful();
        } catch (final Exception e) {
            e.printStackTrace();
            result = -1;
        } finally {
            db.endTransaction();
            db.close();
        }
        return result;
    }

    public long updateUser(final String userId, final ContentValues cv) {
        long result = -1;
        final SQLiteDatabase db = this.mSQLiteHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            final String whereClause = BasicColumns.ID + "=?";
            final String[] whereArgs = new String[] { userId };
            result = db.update(UserInfo.TABLE_NAME, cv, whereClause, whereArgs);
            db.setTransactionSuccessful();
        } catch (final Exception e) {
            e.printStackTrace();
            result = -1;
        } finally {
            db.endTransaction();
            db.close();
        }
        return result;
    }

    public long updateUser(final User user) {
        return updateUser(user.id, user.toContentValues());
    }

}
