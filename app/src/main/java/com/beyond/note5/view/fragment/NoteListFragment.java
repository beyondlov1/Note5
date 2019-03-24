package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.event.*;
import com.beyond.note5.module.DaggerNoteComponent;
import com.beyond.note5.module.NoteComponent;
import com.beyond.note5.module.NoteModule;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.view.adapter.AbstractFragmentNoteView;
import com.beyond.note5.view.adapter.component.NoteRecyclerViewAdapter;
import com.beyond.note5.view.adapter.component.header.ReadFlagItemDataGenerator;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

/**
 * @author: beyond
 * @date: 2019/1/30
 */

public class NoteListFragment extends AbstractFragmentNoteView {

    private List<Note> data = new ArrayList<>();

    public RecyclerView noteRecyclerView;

    public NoteRecyclerViewAdapter noteRecyclerViewAdapter;

    @Inject
    NotePresenter notePresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noteRecyclerViewAdapter = new NoteRecyclerViewAdapter(this.getContext(), new ReadFlagItemDataGenerator<>(data));
        initInjection();
    }

    private void initInjection() {
        NoteComponent noteComponent = DaggerNoteComponent.builder().noteModule(new NoteModule(this)).build();
        noteComponent.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_note_list, container, false);
        initView(viewGroup);
        initOnScrollListener();
        //显示所有Note
        notePresenter.findAll();
        return viewGroup;
    }

    private void initOnScrollListener() {
        noteRecyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                //上划
                if (velocityY<0){
                    EventBus.getDefault().post(new ShowFABEvent(R.id.note_recycler_view));
                }
                return false;
            }
        });
        noteRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //未到滚动的高度
                if(!recyclerView.canScrollVertically(1)&&!recyclerView.canScrollVertically(-1)){
                    EventBus.getDefault().post(new ShowFABEvent(R.id.note_recycler_view));
                    return;
                }
                //下划到底
                if (!recyclerView.canScrollVertically(1)) {
                    EventBus.getDefault().post(new HideFABEvent(R.id.note_recycler_view));
                }
                //上划到顶
                if (!recyclerView.canScrollVertically(-1)) {
                    EventBus.getDefault().post(new ShowFABEvent(R.id.note_recycler_view));
                }

            }

        });
    }

    private void initView(ViewGroup viewGroup) {
        noteRecyclerView = viewGroup.findViewById(R.id.note_recycler_view);
        noteRecyclerView.setAdapter(noteRecyclerViewAdapter);
        //设置显示格式
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        noteRecyclerView.setLayoutManager(staggeredGridLayoutManager);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(AddNoteEvent event) {
        notePresenter.add(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(UpdateNoteEvent event) {
        notePresenter.update(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(DeleteNoteEvent event) {
        notePresenter.delete(event.get());
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(DeleteDeepNoteEvent event) {
        notePresenter.deleteDeep(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(RefreshNoteListEvent event) {
        notePresenter.findAll();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onAddSuccess(Note note) {
        int insertIndex = noteRecyclerViewAdapter.getItemDataGenerator().getInsertIndex(note);
        data.add(insertIndex, note);
        noteRecyclerViewAdapter.notifyInserted(note);
        noteRecyclerView.scrollToPosition(insertIndex);
        msg("添加成功");
    }

    @Override
    public void onFindAllSuccess(List<Note> allNote) {
        data.clear();
        noteRecyclerViewAdapter.notifyFullRangeRemoved();
        data.addAll(allNote);
        noteRecyclerViewAdapter.notifyFullRangeInserted();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onDeleteSuccess(Note note) {
        int index = noteRecyclerViewAdapter.getItemDataGenerator().getIndex(note);
        if (index!=-1){
            data.remove(note);
            noteRecyclerViewAdapter.notifyRemoved(note);
            msg("删除成功");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onUpdateSuccess(Note note) {
        Iterator<Note> iterator = data.iterator();
        while (iterator.hasNext()) {
            Note oldNote = iterator.next();
            if (StringUtils.equals(oldNote.getId(), note.getId())) {
                iterator.remove();
                noteRecyclerViewAdapter.notifyRemoved(oldNote);
                int insertIndex = noteRecyclerViewAdapter.getItemDataGenerator().getInsertIndex(note);
                data.add(insertIndex, note);
                noteRecyclerViewAdapter.notifyInserted(note);
                noteRecyclerView.scrollToPosition(insertIndex);
                msg("更新成功");
                break;
            }
        }
    }
}
