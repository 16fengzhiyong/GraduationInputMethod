package com.nuc.omeletteinputmethod.DBoperation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库操作类，只允许存在一个实例
 */
public class DBHelper extends SQLiteOpenHelper {
    /*数据库名字*/
    private static final String DATABASE_NAME = "myandroid.db";
    /*数据库的版本号*/
    private static final int DB_VERSION = 1;

    private static final int DATABASE_VERSION = 1;
    public DBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    /**
     * 第一次创建数据库的时候会执行这个函数
     * 所以在这个函数里建表
     * db.execSQL() 函数 这个函数经常要用的用来执行sql命令  接收一个String参数
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db){
    }

    /**
     * 升级数据库的时候会执行这个函数
     * 新增字段 新增加表
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){

    }
}