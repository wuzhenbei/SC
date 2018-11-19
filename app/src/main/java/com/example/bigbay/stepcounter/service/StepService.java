package com.example.bigbay.stepcounter.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.bigbay.stepcounter.R;
import com.example.bigbay.stepcounter.StepCounterActivity;
import com.example.bigbay.stepcounter.database.StepDatabaseHelper;

import static com.example.bigbay.stepcounter.util.TimeTools.getDate;
import static com.example.bigbay.stepcounter.util.TimeTools.getTimeStamp;

public class StepService extends Service implements SensorEventListener {


    static final String TAG = "StepService";
    private SensorManager mSensorManager;
    private Sensor stepSensor;





    @Override
    public void onCreate() {

        super.onCreate();
        Log.d(TAG, "服务被创建！");


        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        stepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        boolean registerListener = mSensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (registerListener){
            Log.d(TAG, "传感器注册成功！");
        }

    }




    /**======= 每次启动service时注册记步sensor =======**/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "服务启动！");

        return super.onStartCommand(intent, flags, startId);
    }




    @Override
    public void onDestroy() {

        super.onDestroy();
        Log.d(TAG, "服务被销毁！");

        mSensorManager.unregisterListener(this);
        Log.d(TAG, "传感器取消注册！");
    }




    /*======= 连接activity和service（未启用） =======*/

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }





    /**=========== 当步数变化时更新数据库 ==========**/

    @Override
    public void onSensorChanged(SensorEvent event) {



        //**************** 声明变量 ****************//

        StepDatabaseHelper dbHelper;    // 数据库Helper
        long current_time_stamp;    // 当前系统时间戳（数据库中最后一条数据）
        long last_time_stamp;   // 当前系统时间戳的上一条（数据库中倒数第二条数据）
        int current_system_step;   // 当前系统步数
        int last_system_step;   // 当前系统步数的上一条
        String today_date = getDate(0, "yyyy-MM-dd");   // 获取今天日期
        ContentValues values = new ContentValues();



        //**************** 获取current time_stamp和system_step ****************//

        current_time_stamp = System.currentTimeMillis()/1000;   // 获取当前时间戳，单位是秒
        current_system_step = (int) event.values[0];    // 获取当前系统步数



        //**************** 获取last time_stamp和system_step ****************//

        dbHelper = new StepDatabaseHelper(this, "StepDatabaseHelper.db", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query("SystemStep", null, null, null, null, null, "time_stamp desc", "1");

       if (cursor.moveToFirst()) {

           last_time_stamp = cursor.getInt(cursor.getColumnIndex("time_stamp"));
           last_system_step = cursor.getInt(cursor.getColumnIndex("system_step"));

       }else {

           // 当前数据为今天的第一条数据，记录该节点到表UserStep
           values.put("time_stamp", current_time_stamp);
           values.put("system_step", current_system_step);
           values.put("date", today_date);
           db.insert("UserStep",null, values);
           values.clear();

           last_time_stamp = current_time_stamp;
           last_system_step = current_system_step;

       }

       cursor.close();



       //**************** 比较current和last的数据 ****************//

       if (last_time_stamp < getTimeStamp(today_date, "yyyy-MM-dd")) {

           //**************** 两条数据不在同一天 ******************//

           // 记录昨天的最后一个时间节点
           values.put("time_stamp", last_time_stamp);
           values.put("system_step", last_system_step);
           values.put("date", getDate(-1, "yyyy-MM-dd"));
           db.insert("UserStep",null, values);
           values.clear();

           // 记录今天的第一个时间节点
           values.put("time_stamp", current_time_stamp);
           values.put("system_step", current_system_step);
           values.put("date", getDate(0, "yyyy-MM-dd"));
           db.insert("UserStep",null, values);
           values.clear();

       } else if (current_system_step < last_system_step) {

           //**************** 两条数据在同一天且发生了重启 ******************//

           // 记录重启前的最后一个节点
           values.put("time_stamp", last_time_stamp);
           values.put("system_step", last_system_step);
           values.put("date", getDate(0, "yyyy-MM-dd"));
           db.insert("UserStep",null, values);
           values.clear();

           // 记录重启后的第一个节点
           values.put("time_stamp", current_time_stamp);
           values.put("system_step", current_system_step);
           values.put("date", getDate(0, "yyyy-MM-dd"));
           db.insert("UserStep",null, values);
           values.clear();

       }



        //**************** 将system_step存入表SystemStep ****************//

        values.put("time_stamp", current_time_stamp);
        values.put("system_step", current_system_step);
        db.insert("SystemStep",null, values);
        values.clear();

        // 关闭数据库
        db.close();



        // 调试日志
        Log.d(TAG, "今天日期：" + today_date);
        Log.d(TAG, "last_time_stamp：" + String.valueOf(last_time_stamp));
        Log.d(TAG, "last_system_step：" + String.valueOf(last_system_step));
        Log.d(TAG, "current_time_stamp：" + String.valueOf(current_system_step));
        Log.d(TAG, "current_system_step：" + String.valueOf(current_system_step));

    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }


}
