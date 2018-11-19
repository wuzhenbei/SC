package com.example.bigbay.stepcounter.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TimeTools {


    /**======= 获取任意时刻的时间戳 =======**/

    public static long getTimeStamp(String date_string, String pattern) {

        // 载入pattern的日期格式
        SimpleDateFormat date_format = new SimpleDateFormat(pattern);

        // 获取今天的日期
        Date date = new Date();

        try{
            // 解析输入的date_string的日期
            date = date_format.parse(date_string);
        } catch(ParseException e) {
            e.printStackTrace();
        }

        // 返回单位为秒的时间戳
        return date.getTime()/1000;

    }


    /**======== 获取今天或历史日期 =======**/

    public static String getDate(int distance_day, String pattern) {

        // 实例化Calendar
        Calendar calendar = Calendar.getInstance();

        // 把日期设置为今天
        Date today_date = new Date();
        calendar.setTime(today_date);

        // 获得历史日期（今天 + distance_day），其中distance_day负值代表今天以前的日期
        calendar.add(Calendar.DAY_OF_MONTH, distance_day);

        // 按照pattern的格式返回日期
        SimpleDateFormat date_format = new SimpleDateFormat(pattern);

        return date_format.format(calendar.getTime());

    }


}
