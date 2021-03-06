package com.beyond.note5.model;

import com.beyond.note5.bean.Todo;

public interface CalendarModel extends Model<Todo> {
    void deleteReminder(Todo todo);
    void restoreCalendarReminder(Todo todo);
}
