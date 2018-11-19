package com.example.bigbay.stepcounter.util;



import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.bigbay.stepcounter.WebViewActivity;
import com.example.bigbay.stepcounter.database.DatabaseManager;
import com.example.bigbay.stepcounter.database.StepDatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static com.example.bigbay.stepcounter.util.StepTools.getHistoryStep;
import static com.example.bigbay.stepcounter.util.StepTools.getTodayUserStep;
import static com.example.bigbay.stepcounter.util.TimeTools.getDate;
import static com.example.bigbay.stepcounter.util.TimeTools.getTimeStamp;
//import org.json.JSONObject;
/**
 * Created by xuan on 2018/9/14.
 */

public class UploadTools {

    static final String TAG = "UploadTools";

    private static Logger logger = LoggerFactory.getLogger(UploadTools.class);




    //*******************解析JSON数据***********************//

    public static void parseJSONWithJSONObject(String msg, DatabaseManager mDatabaseManager) throws JSONException {

        JSONObject jsonData = new JSONObject(msg);
        String userid = jsonData.getString("userid");
        String jaccount = jsonData.getString("jaccount");
        String token = jsonData.getString("token");
        String stepsgoal= jsonData.getString("goal");


        // 读取数据库
        SQLiteDatabase db = mDatabaseManager.getWritableDatabase();
        Cursor cursor = db.query("UserInfo", null, null, null, null, null, null);

        // 如果存在数据，那么清空数据库
        if (cursor.moveToFirst()) {

            db.delete("UserInfo", null, null);

        }
        cursor.close();

        // 将最新的用户信息保存到数据库
        ContentValues values = new ContentValues();
        values.put("userid", userid);
        values.put("jaccount", jaccount);
        values.put("token", token);
        db.insert("UserInfo",null, values);

        mDatabaseManager.closeDatabase();



        Log.d(TAG, "已将用户信息保存到数据库！" + "userid:" + userid +" jaccount:"+ jaccount +" token:"+ token+"goal:"+stepsgoal);

    }




    //*******************获取baseUrl***********************//

    public static String getBaseUrl(DatabaseManager mDatabaseManager){

        // 声明变量
        String url ="https://health.sjtu.edu.cn/post-data-from-app.php?from_APP=android";
        String jaccount = "";
        String userid = "";
        String token = "";
        String baseUrl;


        // 读取数据
        SQLiteDatabase db = mDatabaseManager.getWritableDatabase();

        Cursor cursor = db.query("UserInfo", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {

            userid = cursor.getString(cursor.getColumnIndex("userid"));
            jaccount = cursor.getString(cursor.getColumnIndex("jaccount"));
            token = cursor.getString(cursor.getColumnIndex("token"));

        }else {

            Log.d(TAG, "No User Info!");

        }

        cursor.close();
        mDatabaseManager.closeDatabase();

        baseUrl =url+"&jaccount="+jaccount+"&userid="+userid+"&token="+token;

        Log.d(TAG,baseUrl);

        return baseUrl;
    }




    //*******************上传用户当日步数***********************//

    public static void UploadCurrentStep(String vName,DatabaseManager mDatabaseManager,String baseUrl)  {

        int user_step= getTodayUserStep(mDatabaseManager);
        String url = baseUrl + "&CMD=current_steps&steps=" + user_step;
        httpRequest(vName,url,"POST","");

        Log.d(TAG,"上传当日步数!" + url);

    }

    public static void UploadStepsGoal(String vName,SpTools sp,String baseUrl){
        String stepGoal = (String) sp.getParam("stepGoal", "10000");
        String url = baseUrl + "&CMD=update_steps_goal&steps_goal="+stepGoal;
        httpRequest(vName,url,"POST","");
    }




    //********************上传用户七天步数**********************//

    public static void UploadHistoryStep( String vName,DatabaseManager mDatabaseManager,String baseUrl) {
        int history_user_step;
        long time_stamp_date;
        for (int i = -1; i > -8; i--) {

            history_user_step = getHistoryStep(mDatabaseManager, i);
            time_stamp_date = getTimeStamp(getDate(i, "yyyy-MM-dd"), "yyyy-MM-dd");
            String url = baseUrl + "&CMD=history_steps&steps="+ history_user_step + "&date=" + time_stamp_date ;

            if (history_user_step != 0) {
                httpRequest(vName,url,"POST","");
                Log.d(TAG,"上传历史步数!" + url);
            } else {
                return;
            }
        }

    }




    //*****************上传用户分时系统步数********************//

    public static void UploadDetailedStep(String vName,DatabaseManager mDatabaseManager,String baseUrl){


        long startTimestamp =getTimeStamp(getDate(0, "yyyy-MM-dd"), "yyyy-MM-dd");//获取今日开始的时间戳
        long endTimestamp = System.currentTimeMillis()/1000;     //获取结束的时间戳
        String url = baseUrl+ "&CMD=upload_detailed_steps&timestamp=" + startTimestamp + "&until+" + endTimestamp;

        String outputStr = UploadTools.Table2String(mDatabaseManager, startTimestamp);

        httpRequest(vName,url,"POST",outputStr);


        Log.d(TAG,"上传分时系统步数!" + url);
    }




    //***********将数据库中的数据转化为string流*************//

    public static String Table2String(DatabaseManager mDatabaseManager, long startTimeStamp){

        // 声明变量
        String outputStr;
        StringBuffer buffer = new StringBuffer();
        SQLiteDatabase db = mDatabaseManager.getWritableDatabase();


        Cursor cursor = db.query("SystemStep", null, "time_stamp > ?", new String[]{String.valueOf(startTimeStamp)}, null, null, "time_stamp");

        if (cursor.moveToFirst()) {

            do {
                buffer.append(cursor.getInt(cursor.getColumnIndex("time_stamp"))+" "
                        +cursor.getInt(cursor.getColumnIndex("system_step"))+"\n");
            } while (cursor.moveToNext());

        }else {
            Log.d(TAG, "NO DATA!");
        }

        outputStr = buffer.toString();

        cursor.close();
        mDatabaseManager.closeDatabase();

        return outputStr;
    }




    //************上传所有数据**********************//

    public static void uploadData(String vName,SpTools sp,DatabaseManager mDatabaseManager){

        String baseUrl = getBaseUrl(mDatabaseManager);

        UploadCurrentStep(vName,mDatabaseManager,baseUrl);
        UploadStepsGoal(vName,sp,baseUrl);
        UploadDetailedStep(vName,mDatabaseManager,baseUrl);
        UploadHistoryStep(vName,mDatabaseManager,baseUrl);

    }




    //************http request统一接口**********************//

    public static void httpRequest(String vName,String requestUrl, String requestMethod, String outputStr) {

        StringBuffer buffer = new StringBuffer();
        try {
            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            // httpUrlConn.setSSLSocketFactory(ssf);
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            httpUrlConn.setReadTimeout(8000);
            httpUrlConn.setReadTimeout(8000);
            httpUrlConn.setRequestProperty("User-agent","sjtu-android"+" "+vName);

            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);

            if ("GET".equalsIgnoreCase(requestMethod)){
                InputStream in = new BufferedInputStream(httpUrlConn.getInputStream());
                httpUrlConn.connect();
            }
            // 当有数据需要提交时
            if (null != outputStr) {
                DataOutputStream out = new DataOutputStream(httpUrlConn.getOutputStream());
                out.writeBytes(outputStr);
                out.close();
            }

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
                    httpUrlConn.disconnect();
            JSONObject jsonData = new JSONObject(buffer.toString());
            //jsonObject = JSONObject.fromObject(buffer.toString());
        } catch (ConnectException ce) {
                logger.error("server connection timed out.");
        } catch (Exception e) {
                logger.error("https request error:{}", e);
        }

    }


}
