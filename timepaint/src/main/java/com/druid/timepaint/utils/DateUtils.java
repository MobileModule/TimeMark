package com.druid.timepaint.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateUtils {
    /**
     * 获取当前时间的起点（00:00:00）
     */
    public static long getTodayStart(long currentTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(currentTime));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取当前时间的终点（23:59:59）
     */
    public static long getTodayEnd(long currentTime) {
        return getTodayStart(currentTime) + 24 * 60 * 60 * 1000L - 1000;
    }

    /**
     * 获取指定时间的年月日
     */
    public static String getDateByCurrentTime(long currentTime) {
        return new SimpleDateFormat("yyyy-MM-dd").format(currentTime);
    }

    public static String getTimemmssByCurrentTime(long currentTime) {
        return new SimpleDateFormat("mm:ss").format(currentTime);
    }

    public static String getTimeHHmmByCurrentTime(long currentTime) {
        return new SimpleDateFormat("HH:mm").format(currentTime);
    }

    public static String getTimeHHmmssByCurrentTime(long currentTime) {
        return new SimpleDateFormat("HH:mm:ss").format(currentTime);
    }

    /**
     * 获取指定时间的年月日
     */
    public static String getDateTime(long currentTime) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentTime);
    }

    /**
     * 获取指定日期的时间（如：10:11:12）
     */
    public static String getTime(long currentTime) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date(currentTime));
    }

    /**
     * 根据当前的秒数计算时间
     */
    public static String getTimeHHmmByCurrentSecond(int currentSecond) {
        currentSecond = currentSecond / 60;
        int minute = currentSecond % 60;
        int hour = currentSecond / 60;
        if (hour >= 24) {
            hour = hour % 24;
        }
        return (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
    }

    public static String getTimeHHmmssByCurrentSecond(int currentSecond) {
        currentSecond = currentSecond / 60;
        int minute = currentSecond % 60;
        int hour = currentSecond / 60;
        if (hour >= 24) {
            hour = hour % 24;
        }
        return (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute)+":00";
    }

    /**
     * 根据当前的秒数计算时间
     */
    public static String getTimemmssByCurrentSecond(int currentSecond) {
        int diff60 = currentSecond / 60;
        int second = currentSecond % 60;
        diff60 = diff60 % 60;
        return (diff60 < 10 ? "0" + diff60 : diff60) + ":" + (second < 10 ? "0" + second : second);
    }


    /**
     * 根据当前的秒数计算时间
     */
    public static String getTimeByCurrentHours(int currentSecond) {
        currentSecond = currentSecond * 10;
        currentSecond = currentSecond / 60;
        int minute = currentSecond % 60;
        int hour = currentSecond / 60;
        if (hour >= 24) {
            hour = hour % 24;
        }
        return (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
    }

}
