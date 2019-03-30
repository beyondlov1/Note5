package com.beyond.note5.utils;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
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
}
