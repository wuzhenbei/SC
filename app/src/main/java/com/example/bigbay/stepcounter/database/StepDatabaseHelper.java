package com.example.bigbay.stepcounter.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class StepDatabaseHelper extends SQLiteOpenHelper {

    private static StepDatabaseHelper mInstance;

    public StepDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public synchronized static StepDatabaseHelper getInstance(Context context,String name, SQLiteDatabase.CursorFactory factory, int version){
        if(mInstance == null){
            mInstance = new StepDatabaseHelper(context,name,factory,version);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // 声明创建表的sql语句
        String CREATE_SYSTEM_STEP;
        CREATE_SYSTEM_STEP = "create table SystemStep ("
                + "time_stamp integer primary key, "    // 系统时间戳，单位是秒
                + "system_step integer)";   // 系统步数

        String CREATE_HISTORY_USER_STEP;
        CREATE_HISTORY_USER_STEP = "create table UserStep ("
                + "time_stamp integer primary key, "    // 系统时间戳，单位是秒
                + "date string, "   // 日期
                + "system_step integer)";    // 系统步数

        String CREATE_USER_INFO;
        CREATE_USER_INFO = "create table UserInfo ("
                + "userid string, "
                + "jaccount string, "
                + "token string)";


        // 创建数据库时，直接执行sql语句，创建表SystemStep
        db.execSQL(CREATE_SYSTEM_STEP);
        db.execSQL(CREATE_HISTORY_USER_STEP);
        db.execSQL(CREATE_USER_INFO);

        Log.d("数据库", "创建成功！");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
