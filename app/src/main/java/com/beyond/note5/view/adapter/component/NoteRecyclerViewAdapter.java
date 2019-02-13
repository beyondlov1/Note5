package com.beyond.note5.view.adapter.component;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;
import android.view.View;

import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Note;
import com.beyond.note5.event.DetailNoteEvent;
import com.beyond.note5.view.fragment.NoteDetailFragment;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

public class NoteRecyclerViewAdapter extends DocumentRecyclerViewAdapter<Note> {

    private FragmentManager fragmentManager;

    public NoteRecyclerViewAdapter(Context context, List<Note> data, FragmentManager fragmentManager) {
        super(context, data);
        this.fragmentManager = fragmentManager;
    }

    @Override
    protected void onItemClick(View v, List<Note> data, Note note, int position) {
        super.onItemClick(v,data, note, position);
        if (!NoteDetailFragment.isShowing.get()){
            NoteDetailFragment noteDetailFragment = new NoteDetailFragment();
            noteDetailFragment.show(fragmentManager, "detail");
            EventBus.getDefault().postSticky(new DetailNoteEvent(data,position));
        }
    }

    @Override
    protected void addHeaderData(SparseArray<Header> headers) {
        Integer lastReadFlag = null;
        int index = 0;
        for (Note note : data) {
            Integer readFlag = note.getReadFlag();
            if (lastReadFlag == null) {
                headers.put(index + headers.size(), new DocumentRecyclerViewAdapter.Header(index + headers.size(), String.valueOf(readFlag)));
            }
            if (lastReadFlag != null && !lastReadFlag.equals(readFlag)) {
                headers.put(index + headers.size(), new DocumentRecyclerViewAdapter.Header(index + headers.size(),String.valueOf(readFlag)));
            }
            lastReadFlag = readFlag;
            index++;
        }
    }

    public void setData(List<Note> data){
        this.data = data;
    }

}
