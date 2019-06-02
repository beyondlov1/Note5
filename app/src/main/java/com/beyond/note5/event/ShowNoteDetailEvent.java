package com.beyond.note5.event;

import android.view.View;

import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.LoadType;

import java.util.List;

public class ShowNoteDetailEvent extends AbstractEvent<View> {

    private List<Note> data;
    private int Index;
    private LoadType loadType;

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

    public LoadType getLoadType() {
        return loadType;
    }

    public void setLoadType(LoadType loadType) {
        this.loadType = loadType;
    }

}
