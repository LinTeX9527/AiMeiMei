package com.lintex9527.android.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 工具类，操作数据库
 * Created by LinTeX9527 on 2017/9/22.
 */

public class DBManager {
    private static MySQLiteHelper helper;

    /**
     * 单实例，获得 helper 对象
     * @param context 上下文对象
     * @return 获得一个 helper 对象
     */
    public static MySQLiteHelper getInstance(Context context) {
        if (helper == null) {
            helper = new MySQLiteHelper(context);
        }
        return helper;
    }

    /**
     * 在数据库db中执行某个sql操作
     * @param db 数据库对象
     * @param sql SQL 语句
     */
    public static void execSQL(SQLiteDatabase db, String sql) {
        if (db != null) {
            if (sql != null && !"".equals(sql)) {
                db.execSQL(sql);
            }
        }
    }


    /**
     * 获取数据库 db 中表格 table 的总的行数
     * @param db 数据库对线
     * @param table 表格名称
     * @return 数据库 db 中表格 table 的总的行数
     */
    public static int getCount(SQLiteDatabase db, String table){
        Cursor cursor = db.query(table, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
}
