package com.beyond.note5.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

@Entity
public class Reminder implements Cloneable{

    @Id
    private String id;

    private Long calendarId;
    private Long calendarEventId;
    private Long calendarReminderId;

    private Date start;
    private Date end;
    private Long repeatMills;

    @Generated(hash = 1678144786)
    public Reminder(String id, Long calendarId, Long calendarEventId,
            Long calendarReminderId, Date start, Date end, Long repeatMills) {
        this.id = id;
        this.calendarId = calendarId;
        this.calendarEventId = calendarEventId;
        this.calendarReminderId = calendarReminderId;
        this.start = start;
        this.end = end;
        this.repeatMills = repeatMills;
    }

    @Generated(hash = 4427342)
    public Reminder() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }

    public Long getCalendarEventId() {
        return calendarEventId;
    }

    public void setCalendarEventId(Long calendarEventId) {
        this.calendarEventId = calendarEventId;
    }

    public Long getCalendarReminderId() {
        return calendarReminderId;
    }

    public void setCalendarReminderId(Long calendarReminderId) {
        this.calendarReminderId = calendarReminderId;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Long getRepeatMills() {
        return repeatMills;
    }

    public void setRepeatMills(Long repeatMills) {
        this.repeatMills = repeatMills;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reminder reminder = (Reminder) o;

        if (id != null ? !id.equals(reminder.id) : reminder.id != null) return false;
        if (calendarId != null ? !calendarId.equals(reminder.calendarId) : reminder.calendarId != null)
            return false;
        if (calendarEventId != null ? !calendarEventId.equals(reminder.calendarEventId) : reminder.calendarEventId != null)
            return false;
        if (calendarReminderId != null ? !calendarReminderId.equals(reminder.calendarReminderId) : reminder.calendarReminderId != null)
            return false;
        if (start != null ? !start.equals(reminder.start) : reminder.start != null) return false;
        if (end != null ? !end.equals(reminder.end) : reminder.end != null) return false;
        return repeatMills != null ? repeatMills.equals(reminder.repeatMills) : reminder.repeatMills == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (calendarId != null ? calendarId.hashCode() : 0);
        result = 31 * result + (calendarEventId != null ? calendarEventId.hashCode() : 0);
        result = 31 * result + (calendarReminderId != null ? calendarReminderId.hashCode() : 0);
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (end != null ? end.hashCode() : 0);
        result = 31 * result + (repeatMills != null ? repeatMills.hashCode() : 0);
        return result;
    }
}
