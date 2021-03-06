package com.beyond.note5.model;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.model.dao.ReminderDao;
import com.beyond.note5.permission.RequirePermission;
import com.beyond.note5.utils.CalendarUtil;

import org.apache.commons.lang3.time.DateUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class CalendarModelImpl implements CalendarModel {

    private static final String TAG = "CalendarModelImpl";

    private Activity activity;

    private ReminderDao reminderDao;

    private static Map<Activity, CalendarModel> instanceContainer = new HashMap<>(2);

    public static CalendarModel getRelativeSingletonInstance(Activity activity) {
        if (instanceContainer.get(activity) == null) {
            synchronized (CalendarModel.class) {
                CalendarModel calendarModel = new CalendarModelImpl(activity);
                instanceContainer.put(activity, calendarModel);
                return calendarModel;
            }
        }
        return instanceContainer.get(activity);
    }

    public CalendarModelImpl(Activity activity) {
        this.activity = activity;

        DaoSession daoSession = MyApplication.getInstance().getDaoSession();
        reminderDao = daoSession.getReminderDao();
    }

    @Override
    @RequirePermission({Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_CALENDAR})
    public void add(final Todo todo) {

        ContentValues values = getContentValues(todo);
        if (values == null) {
            return;
        }

        ContentResolver cr = MyApplication.getInstance().getContentResolver();
        @SuppressLint("MissingPermission") Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

        if (uri != null) {
            long eventID = Long.parseLong(uri.getLastPathSegment());

            //add Reminder
            ContentValues reminderValues = new ContentValues();
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventID);
            reminderValues.put(CalendarContract.Reminders.MINUTES, 0);
            reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            @SuppressLint("MissingPermission") Uri reminderUri = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);
            if (reminderUri == null) {
                Log.e(TAG, "添加提醒失败");
                throw new RuntimeException("添加提醒失败");
            }

            todo.getReminder().setCalendarEventId(eventID);
            todo.getReminder().setCalendarReminderId(Long.parseLong(reminderUri.getLastPathSegment()));
            reminderDao.update(todo.getReminder());
        } else {
            Log.e(TAG, "事件添加失败");
            throw new RuntimeException("添加提醒失败");
        }
    }

    @Override
    @RequirePermission({Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_CALENDAR})
    public void update(final Todo todo) {
        ContentValues values = getContentValues(todo);
        if (values == null) {
            return;
        }
        Long eventId = todo.getReminder().getCalendarEventId();
        if (eventId == null) {
            return;
        }
        ContentResolver cr = MyApplication.getInstance().getContentResolver();
        Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        int rows = cr.update(updateUri, values, null, null);
        Log.i(TAG, "Rows updated: " + rows);
    }

    @Override
    @RequirePermission({Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_CALENDAR})
    public void delete(final Todo todo) {
        if (todo.getReminder() == null){
            return;
        }
        Long eventId = todo.getReminder().getCalendarEventId();
        if (eventId == null) {
            return;
        }
        ContentResolver cr = MyApplication.getInstance().getContentResolver();
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        int rows = cr.delete(deleteUri, null, null);
        Log.i(TAG, "Rows deleted: " + rows);
    }

    @Override
    public List<Todo> findAll() {
        return null;
    }

    @Override
    @RequirePermission({Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_CALENDAR})
    public void deleteReminder(final Todo todo) {
        Long reminderId = todo.getReminder().getCalendarReminderId();
        if (reminderId == null) {
            return;
        }
        ContentResolver cr = MyApplication.getInstance().getContentResolver();
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Reminders.CONTENT_URI, reminderId);
        int rows = cr.delete(deleteUri, null, null);
        Log.i(TAG, "Rows deleted: " + rows);
        todo.getReminder().setCalendarReminderId(null);
        reminderDao.update(todo.getReminder());
    }

    @Override
    @RequirePermission({Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_CALENDAR})
    public void restoreCalendarReminder(final Todo todo) {
        if (todo.getReminder() == null || todo.getReminder().getCalendarEventId() == null) {
            return;
        }
        ContentResolver cr = MyApplication.getInstance().getContentResolver();
        long eventID = todo.getReminder().getCalendarEventId();
        //add Reminder
        ContentValues reminderValues = new ContentValues();
        reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventID);
        reminderValues.put(CalendarContract.Reminders.MINUTES, 0);
        reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        @SuppressLint("MissingPermission") Uri reminderUri = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);
        if (reminderUri == null) {
            Log.e(TAG, "恢复提醒失败");
            throw new RuntimeException("恢复提醒失败");
        }

        todo.getReminder().setCalendarReminderId(Long.parseLong(reminderUri.getLastPathSegment()));
        reminderDao.update(todo.getReminder());
    }

    private ContentValues getContentValues(Todo todo) {
        Long startMillis;
        long endMillis;
        String title;
        String content;
        String contentWithoutTime;
        Long calendarId = CalendarUtil.getCalendarId(activity);

        if (todo.getReminder() == null) {
            return null;
        }
        if (todo.getReminder().getStart() == null) {
            return null;
        }
        startMillis = todo.getReminder().getStart().getTime();

        if (todo.getReminder().getEnd() == null) {
            endMillis = startMillis + DateUtils.MILLIS_PER_MINUTE * 15;
        } else {
            endMillis = todo.getReminder().getEnd().getTime();
        }
        title = todo.getTitle();
        content = todo.getContent();
        contentWithoutTime = todo.getContentWithoutTime();

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, contentWithoutTime);
        values.put(CalendarContract.Events.DESCRIPTION, content);
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getDisplayName());
        return values;
    }
}
