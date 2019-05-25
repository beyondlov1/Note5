package com.beyond.note5.view;

import android.app.Activity;

import com.beyond.note5.bean.Note;
import com.beyond.note5.bean.Todo;

public interface DocumentCompositeView {

    void onNoteAddSuccess(Note document);

    void onTodoAddSuccess(Todo document);

    void onAddFail();

    Activity getActivity();
}
