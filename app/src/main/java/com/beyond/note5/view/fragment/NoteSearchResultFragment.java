package com.beyond.note5.view.fragment;

import android.view.View;

import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.LoadType;
import com.beyond.note5.event.ShowNoteDetailEvent;
import com.beyond.note5.utils.InputMethodUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class NoteSearchResultFragment extends SearchResultFragment<Note> {

    @Override
    protected void initViewHolder(List<Note> data, int position, MyViewHolder holder) {
        super.initViewHolder(data, position, holder);
        holder.titleTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public View.OnClickListener getOnItemClickListener(List<Note> data, int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodUtil.hideKeyboard(v);
                ShowNoteDetailEvent showNoteDetailEvent = new ShowNoteDetailEvent(v);
                showNoteDetailEvent.setData(data);
                showNoteDetailEvent.setIndex(position);
                showNoteDetailEvent.setLoadType(LoadType.CONTENT);
                EventBus.getDefault().post(showNoteDetailEvent);
            }
        };
    }
}
