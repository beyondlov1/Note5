package com.beyond.note5.view.adapter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.note5.bean.Note;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.view.NoteView;
import com.beyond.note5.view.adapter.list.NoteRecyclerViewAdapter;
import com.beyond.note5.view.adapter.list.header.ReadFlagItemDataGenerator;

import javax.inject.Inject;

/**
 * @author: beyond
 * @date: 2019/2/6
 */

public abstract class AbstractNoteFragment extends AbstractDocumentFragment<Note> implements NoteView{

    @Inject
    protected NotePresenter notePresenter;

    protected ViewGroup viewGroup;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recyclerViewAdapter = new NoteRecyclerViewAdapter(this.getContext(), new ReadFlagItemDataGenerator<>(data));
        initInjection();
    }

    private void initInjection() {
        notePresenter = new NotePresenterImpl(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = initViewGroup(inflater, container, savedInstanceState);
        initView();
        initEvent();
        //显示所有Note
        notePresenter.findAll();
        return viewGroup;
    }

    protected abstract ViewGroup initViewGroup(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    protected abstract void initView();

    protected abstract void initEvent();
}
