package com.beyond.note5.utils;

import android.support.annotation.NonNull;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author beyondlov1
 * @date 2019/03/30
 */
public class DateUtil {
    private static Date parseDate(String str){
        return parseDate(str,"yyyy-MM-dd");
    }

    @SuppressWarnings("SameParameterValue")
    private static Date parseDate(String str, String format){
        try {
            return DateUtils.parseDate(str,format);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isToday(String string){
        return DateUtils.isSameDay(parseDate(string),new Date());
    }

    public static long between(@NonNull Date date1,@NonNull Date date2) {
        return date1.getTime() - date2.getTime();
    }

    public static long between(long date1,@NonNull Date date2) {
        return date1 - date2.getTime();
    }

    public static long between(@NonNull Date date1,long date2) {
        return date1.getTime() - date2;
    }

    public static boolean in(Date date, int startHour, int periodHour) {
        int hour = DateUtils.toCalendar(date).get(Calendar.HOUR_OF_DAY);
        if (startHour+periodHour>24){
            return hour >= startHour || hour < startHour + periodHour - 24;
        }else {
            return hour >= startHour && hour < startHour + periodHour;
        }
    }
}
