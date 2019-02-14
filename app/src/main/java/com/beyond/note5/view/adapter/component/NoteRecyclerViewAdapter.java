package com.beyond.note5.view.adapter.component;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.beyond.note5.bean.Note;
import com.beyond.note5.event.DetailNoteEvent;
import com.beyond.note5.view.adapter.component.header.ItemDataGenerator;
import com.beyond.note5.view.fragment.NoteDetailFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

public class NoteRecyclerViewAdapter extends DocumentRecyclerViewAdapter<Note> {

    private FragmentManager fragmentManager;

    public NoteRecyclerViewAdapter(Context context, ItemDataGenerator<Note> itemDataGenerator, FragmentManager fragmentManager) {
        super(context, itemDataGenerator);
        this.fragmentManager = fragmentManager;
    }

    @Override
    protected void onItemClick(View v, List<Note> data, Note note, int index) {
        super.onItemClick(v,data, note, index);
        if (!NoteDetailFragment.isShowing.get()){
            NoteDetailFragment noteDetailFragment = new NoteDetailFragment();
            noteDetailFragment.show(fragmentManager, "detail");
            EventBus.getDefault().postSticky(new DetailNoteEvent(data,index));
        }
    }

}
