package com.example.bigbay.stepcounter.util;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.bigbay.stepcounter.database.DatabaseManager;
import com.example.bigbay.stepcounter.database.StepDatabaseHelper;

import static com.example.bigbay.stepcounter.util.TimeTools.getDate;


public class StepTools {

    static final String TAG = "StepTools";

    /**======== 获取用户今天的总步数 =========**/

    public static int getTodayUserStep(DatabaseManager mDatabaseManager) {

        // 声明变量
        int user_step = 0;
        int current_system_step;
        String today_date = getDate(0, "yyyy-MM-dd");  // 获取今天的日期
        int cursor_temp_1;
        int cursor_temp_2;


        // 读取数据库
        SQLiteDatabase db = mDatabaseManager.getWritableDatabase();

        // 获取当前系统步数
        Cursor cursor_1 = db.query("SystemStep", null, null, null, null, null, "time_stamp desc", "1");
        if (cursor_1.moveToFirst()) {
            current_system_step = cursor_1.getInt(cursor_1.getColumnIndex("system_step"));
        }else {
            current_system_step = 0;
        }
        cursor_1.close();


        Log.d(TAG, "获取到的当前系统步数：");
        Log.d(TAG, String.valueOf(current_system_step));

        // 计算今天用户步数
        Cursor cursor_2 = db.query("UserStep", null, "date = ?", new String[]{today_date}, null, null, "time_stamp");
        if (cursor_2.moveToLast()) {

            user_step += current_system_step - cursor_2.getInt(cursor_2.getColumnIndex("system_step"));

            Log.d(TAG, "UserStep中最后一条数据：");
            Log.d(TAG, String.valueOf(cursor_2.getInt(cursor_2.getColumnIndex("system_step"))));

            while (cursor_2.moveToPrevious()) {
                cursor_temp_2 = cursor_2.getInt(cursor_2.getColumnIndex("system_step"));
                cursor_2.moveToPrevious();
                cursor_temp_1 = cursor_2.getInt(cursor_2.getColumnIndex("system_step"));
                user_step += cursor_temp_2 - cursor_temp_1;

                Log.d(TAG, "cursor_temp_1：");
                Log.d(TAG, String.valueOf(cursor_temp_1));
                Log.d(TAG, "cursor_temp_2：");
                Log.d(TAG, String.valueOf(cursor_temp_2));
                Log.d(TAG, "cursor_temp_2 - cursor_temp_1：");
                Log.d(TAG, String.valueOf(cursor_temp_2 - cursor_temp_1));
            }

        }else {
            user_step = 0;
        }
        cursor_2.close();
        mDatabaseManager.closeDatabase();

        return user_step;

    }

    /**======== 获取用户的历史步数 =========**/

    public static int getHistoryStep(DatabaseManager mDatabaseManager, int distance_day) {

        // 声明变量
        int user_step = 0;
        String history_date = getDate(distance_day, "yyyy-MM-dd");  // 获取想要查询的日期
        int cursor_temp_1;
        int cursor_temp_2;


        // 读取数据库
        SQLiteDatabase db = mDatabaseManager.getWritableDatabase();

        // 计算今天用户步数
        Cursor cursor = db.query("UserStep", null, "date = ?", new String[]{history_date}, null, null, "time_stamp");
        if (cursor.moveToFirst()) {

                do {
                    cursor_temp_1 = cursor.getInt(cursor.getColumnIndex("system_step"));
                    cursor.moveToNext();
                     cursor_temp_2 = cursor.getInt(cursor.getColumnIndex("system_step"));
                    user_step += cursor_temp_2 - cursor_temp_1;

                    Log.d(TAG, "日期：" + history_date + " 步数:" + String.valueOf(user_step));

                } while (cursor.moveToNext());

        }else {
            user_step = 0;
            Log.d(TAG, "NO HISTORY DATA!");
        }
        cursor.close();
        mDatabaseManager.closeDatabase();

        return user_step;


    }

}
