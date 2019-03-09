package com.beyond.note5.utils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import com.beyond.note5.MyApplication;
import com.beyond.note5.R;

import java.util.TimeZone;

public class CalendarUtil {

    private static String calenderURL = "content://com.android.calendar/calendars";
    private static String calenderEventURL = "content://com.android.calendar/events";
    private static String calenderReminderURL = "content://com.android.calendar/reminders";

    public static Long getCalendarId(Activity activity) {
        return initAccount(activity);
    }

    //检查是否有账户
    private static long checkAccount(Context context) {
        Cursor userCursor = context.getContentResolver().query(Uri.parse(calenderURL), null, null, null, null);
        try {

            if (userCursor == null) {
                //没有账户
                return -1;
            } else {
                //有账户返回第一个账户ID
                int count = userCursor.getCount();
                if (count > 0) {
                    //有账户
                    userCursor.moveToFirst();
                    return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
                } else {
                    //账户数为负数或0
                    return -1;
                }
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
    }

    //添加test账户所需的静态参数
    private final static String CALENDARS_NAME = MyApplication.getInstance().getResources().getString(R.string.app_name);
    private final static String CALENDARS_ACCOUNT_NAME = MyApplication.getInstance().getResources().getString(R.string.app_name)+"@gmail.com";
    private final static String CALENDARS_ACCOUNT_TYPE = "com.android.exchange";
    private final static String CALENDARS_DISPLAY_NAME = MyApplication.getInstance().getResources().getString(R.string.app_name);

    //添加test账户
    private static long addAccount(Context context) {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, CALENDARS_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);
        Uri calendarUri = Uri.parse(calenderURL);
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
                .build();
        Uri result = context.getContentResolver().insert(calendarUri, value);
        return result == null ? -1 : ContentUris.parseId(result);
    }

    //初始化账户
    private static long initAccount(Activity activity) {
        long accountId;
        if (checkAccount(activity) < 0) {
            accountId = addAccount(activity);
        } else {
            accountId = checkAccount(activity);
        }
        return accountId;
    }

}