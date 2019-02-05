package com.beyond.note5.view.adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.beyond.note5.bean.Note;
import com.beyond.note5.event.DetailNoteEvent;
import com.beyond.note5.view.fragment.NoteDetailSwitcherFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by beyond on 2019/2/2.
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
        NoteDetailSwitcherFragment noteDetailSwitcherFragment = new NoteDetailSwitcherFragment();
        noteDetailSwitcherFragment.show(fragmentManager, "detail");
        EventBus.getDefault().postSticky(new DetailNoteEvent(data,position));
//        Snackbar.make(v, note.getContent(), Snackbar.LENGTH_LONG).show();
    }

    public void setData(List<Note> data){
        this.data = data;
    }

}
