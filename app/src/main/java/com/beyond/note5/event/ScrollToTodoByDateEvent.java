package com.beyond.note5.event;

import java.util.Date;

public class ScrollToTodoByDateEvent extends AbstractEvent<Date> {
    public ScrollToTodoByDateEvent(Date date) {
        super(date);
    }
}
