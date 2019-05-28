package com.beyond.note5.view;

import android.app.Activity;

public interface DocumentCompositeView {

    Activity getActivity();

    NoteView getNoteView();

    TodoView getTodoView();

    CalendarView getCalendarView();

    PredictView getPredictView();
}
