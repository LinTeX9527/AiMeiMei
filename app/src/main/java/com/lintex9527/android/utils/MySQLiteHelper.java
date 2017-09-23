package com.lintex9527.android.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * SQLiteOpenHelper 有以下两点作用：
 * 1、提供了 onCreate(), onUpgrade() 等创建数据库、更新数据库的方法；
 * 2、提供了获取数据库对象的函数。
 * Created by LinTeX9527 on 2017/9/22.
 */

public class MySQLiteHelper extends SQLiteOpenHelper {

    private final String TAG = MySQLiteHelper.class.getSimpleName();

    /**
     * 构造函数
     * @param context 上下文对象
     * @param name 要创建的数据库的名称
     * @param factory 游标工厂
     * @param version 数据库的版本号，必须>=1
     */
    public MySQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    /**
     * 简化版本的构造函数
     * @param context 上下文对象
     */
    public MySQLiteHelper(Context context) {
        super(context, Constant.DATABASE_NAME, null, Constant.DATABASE_VERSION);
    }

    /**
     * 创建数据库的回调函数
     * 可以在这里创建表
     * @param db 数据库对象
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "------ onCreate()  ------");

        // 创建表 conversation
        String sql = "create table if not exists " + Constant.TABLE_NAME + " (" +
                " " + Constant._ID +  " integer primary key, " +
                " " + Constant.USER_NAME + " varchar(32), " +
                " " + Constant.USER_MESG + " varchar(140))"; // sql 语句末尾不用添加分号吗？

        db.execSQL(sql);

    }

    /**
     * 数据库版本更新时的回调函数
     * @param db 数据库对象
     * @param oldVersion 数据库旧版本
     * @param newVersion 数据库新版本
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "------ onUpgrade()  ------");
    }

    /**
     * 数据库打开是的回调函数
     * @param db 数据库对象
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        Log.d(TAG, "------ onOpen()  ------");
    }
}
