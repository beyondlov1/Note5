package com.beyond.note5.event;

import java.util.Date;

public class HideTodoEditEvent extends AbstractEvent<Integer> {
    private Date changedReminderStartOnTyping;

    public HideTodoEditEvent(Integer index) {
        super(index);
    }

    public void setChangedReminderStartOnTyping(Date changedReminderStartOnTyping) {
        this.changedReminderStartOnTyping = changedReminderStartOnTyping;
    }

    public Date getChangedReminderStartOnTyping() {
        return changedReminderStartOnTyping;
    }
}
