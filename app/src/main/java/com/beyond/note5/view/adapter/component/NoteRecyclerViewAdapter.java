package com.beyond.note5.view.adapter.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.event.ShowNoteDetailEvent;
import com.beyond.note5.view.adapter.component.header.Header;
import com.beyond.note5.view.adapter.component.header.ItemDataGenerator;
import com.beyond.note5.view.adapter.component.viewholder.NoteViewHolder;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

public class NoteRecyclerViewAdapter extends DocumentRecyclerViewAdapter<Note,NoteViewHolder> {


    public NoteRecyclerViewAdapter(Context context, ItemDataGenerator<Note> itemDataGenerator) {
        super(context, itemDataGenerator);
    }

    @Override
    protected NoteViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    protected void initHeaderDisplay(int position, Header header, NoteViewHolder viewHolder) {
        viewHolder.title.setVisibility(View.VISIBLE);
        viewHolder.title.setText(header.getContent());
        viewHolder.title.setTextColor(context.getResources().getColor(R.color.dark_yellow));
        viewHolder.content.setVisibility(View.GONE);
        viewHolder.content.setText(header.getContent());
        viewHolder.container.setOnClickListener(null);
        viewHolder.dataContainer.setBackground(null);
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        layoutParams.setFullSpan(true);
    }

    @Override
    protected void initContentDisplay(NoteViewHolder viewHolder, Note note, int position) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(13);
        gradientDrawable.setStroke(1, ContextCompat.getColor(context, R.color.dark_gray));
        viewHolder.dataContainer.setBackground(gradientDrawable);
        if (StringUtils.isNotBlank(note.getTitle())) {
            viewHolder.title.setText(StringUtils.trim(note.getTitle()));
            viewHolder.title.setVisibility(View.VISIBLE);
        } else {
            viewHolder.title.setVisibility(View.GONE);
        }
        viewHolder.title.setTextColor(Color.DKGRAY);
        viewHolder.content.setVisibility(View.VISIBLE);
        viewHolder.content.setTextSize(12);
        viewHolder.content.setText(StringUtils.trim(note.getContent()));

        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        if (itemDataGenerator.getSingleContentPositions().contains(position)) {
            layoutParams.setFullSpan(true);
        } else {
            layoutParams.setFullSpan(false);
        }
    }

    @Override
    protected void initContentEvent(NoteViewHolder viewHolder, final Note note) {
        final int index = itemDataGenerator.getIndex(note);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(v, itemDataGenerator.getContentData(), note , index);
            }
        });
    }

    private void onItemClick(View v, List<Note> data, Note note, int index) {
        ShowNoteDetailEvent showNoteDetailEvent = new ShowNoteDetailEvent(v);
        showNoteDetailEvent.setData(data);
        showNoteDetailEvent.setIndex(index);
        EventBus.getDefault().post(showNoteDetailEvent);
    }

}
