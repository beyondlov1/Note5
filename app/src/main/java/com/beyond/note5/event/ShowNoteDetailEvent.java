package com.beyond.note5.event;

import android.view.View;

import com.beyond.note5.bean.Note;

import java.util.List;

public class ShowNoteDetailEvent extends AbstractEvent<View> {

    private List<Note> data;
    private int Index;

    public ShowNoteDetailEvent(View view) {
        super(view);
    }

    public List<Note> getData() {
        return data;
    }

    public void setData(List<Note> data) {
        this.data = data;
    }

    public int getIndex() {
        return Index;
    }

    public void setIndex(int index) {
        Index = index;
    }
}
