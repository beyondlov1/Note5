package com.beyond.note5.utils;

import android.util.Log;

import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.TimeZone;

public class TimeNLPUtil {

    private static TimeNormalizer normalizer;

    public static Date parse(String chineseWithTimeMean) {
        Date date = null;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            date = timeUnits[0].getTime();
        } catch (Exception e) {
            Log.i("TimeNLPUtil",e.getMessage());
        }
        return date;
    }

    public static String getTimeExpression(String chineseWithTimeMean) {
        String timeExpression = null;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            timeExpression = timeUnits[0].Time_Expression;
        } catch (Exception e) {
            Log.i("TimeNLPUtil",e.getMessage());
        }
        return timeExpression;
    }

    public static String getTimeNorm(String chineseWithTimeMean) {
        String timeNorm = null;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            timeNorm = timeUnits[0].Time_Norm;
        } catch (Exception e) {
            Log.i("TimeNLPUtil",e.getMessage());
        }
        return timeNorm;
    }

    public static String getDateStringForMicrosoftEvent(Date date, String timeZoneId) {
        if (date == null) return null;
        return DateFormatUtils.format(date,"yyyy-MM-dd'T'HH:mm:ss",TimeZone.getTimeZone(timeZoneId));
    }

    private static TimeNormalizer getNormalizer() {
        if (normalizer == null) {
            normalizer = initNormalizer();
        }
        return normalizer;
    }

    private static TimeNormalizer initNormalizer() {
        URL url = TimeNormalizer.class.getResource("/TimeExp.m");
        TimeNormalizer normalizer = null;
        try {
            normalizer = new TimeNormalizer(url.toURI().toString());
            normalizer.setPreferFuture(true);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return normalizer;
    }


}
