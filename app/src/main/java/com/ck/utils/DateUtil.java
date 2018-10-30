package com.ck.utils;

import android.os.SystemClock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author fei
 * @date on 2018/10/17 0017
 * @describe TODO :
 **/
public class DateUtil {

    /**
     * 设置系统的日期
     * 需要按照如下网址 ：https://www.cnblogs.com/muouren/p/3903102.html
     * @param year
     * @param month
     * @param day
     */
    public static void setDate(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(when);
        }
    }

    /**
     * 设置系统的时间
     * 需要按照如下网址 ： https://www.cnblogs.com/muouren/p/3903102.html
     * @param hourOfDay
     * @param minute
     */
    public static void setTime(int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(when);
        }
    }

    /**
     * 根据传入的日期格式返回日期
     * @param format
     * @return
     */
    public static String getDate(String format){
        SimpleDateFormat formatter = new SimpleDateFormat (format);
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return  formatter.format(curDate);
    }

}
