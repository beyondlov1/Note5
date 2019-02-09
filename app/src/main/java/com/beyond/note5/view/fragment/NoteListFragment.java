package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.note5.R;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Note;
import com.beyond.note5.event.AddNoteEvent;
import com.beyond.note5.event.DeleteNoteEvent;
import com.beyond.note5.event.HideFABEvent;
import com.beyond.note5.event.ShowFABEvent;
import com.beyond.note5.event.UpdateNoteEvent;
import com.beyond.note5.module.DaggerNoteComponent;
import com.beyond.note5.module.NoteComponent;
import com.beyond.note5.module.NoteModule;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.view.adapter.AbstractFragmentNoteView;
import com.beyond.note5.view.adapter.component.DocumentRecyclerViewAdapter;
import com.beyond.note5.view.adapter.component.NoteRecyclerViewAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

/**
 * @author: beyond
 * @date: 2019/1/30
 */

public class NoteListFragment extends AbstractFragmentNoteView {

    private List<Note> data = new ArrayList<>();

    private SparseArray<DocumentRecyclerViewAdapter.Header> headers = new SparseArray<>();

    private RecyclerView noteRecyclerView;

    private NoteRecyclerViewAdapter noteRecyclerViewAdapter;

    @Inject
    NotePresenter notePresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noteRecyclerViewAdapter = new NoteRecyclerViewAdapter(this.getContext(), data,headers, getFragmentManager());
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
    public void onNoteReceived(AddNoteEvent event) {
        notePresenter.add(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoteReceived(UpdateNoteEvent event) {
        notePresenter.update(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoteReceived(DeleteNoteEvent event) {
        notePresenter.delete(event.get());
    }

    @Override
    public void onAddSuccess(Note note) {
        data.add(0, note);
        noteRecyclerViewAdapter.notifyRangeInserted(0,1);
        noteRecyclerView.scrollToPosition(0);
        msg("添加成功");
    }

    @Override
    public void onFindAllSuccess(List<Note> allNote) {
        int previousSize = data.size();
        data.clear();
        noteRecyclerViewAdapter.notifyRangeRemoved(0, previousSize);
        data.addAll(allNote);
        noteRecyclerViewAdapter.notifyRangeInserted(0, data.size());
    }

    @Override
    public void onDeleteSuccess(Note note) {
        int index = data.indexOf(note);
        data.remove(note);
        if (index!=-1){
            noteRecyclerViewAdapter.notifyRangeRemoved(index,1);
            msg("删除成功");
        }
    }

    @Override
    public void onUpdateSuccess(Note note) {
        Iterator<Note> iterator = data.iterator();
        while (iterator.hasNext()) {
            Note oldNote = iterator.next();
            if (StringUtils.equals(oldNote.getId(), note.getId())) {
                iterator.remove();
                data.add(0, note);
                noteRecyclerViewAdapter.notifyFullRangeChanged();
                msg("更新成功");
                break;
            }
        }
    }

    private void initHeaders() {
        headers.clear();
        Date lastDate = null;
        int index = 0;
        for (Document datum : data) {
            Date lastModifyTime = datum.getLastModifyTime();
            if (lastDate== null ){
                headers.put(index + headers.size(),new DocumentRecyclerViewAdapter.Header(index+headers.size(), DateFormatUtils.format(lastModifyTime,"yyyy-MM-dd")));
            }
            if (lastDate!= null && !DateUtils.truncatedEquals(lastModifyTime,lastDate, Calendar.DATE)){
                headers.put(index+headers.size(),new DocumentRecyclerViewAdapter.Header(index+headers.size(), DateFormatUtils.format(lastModifyTime,"yyyy-MM-dd")));
            }
            lastDate = lastModifyTime;
            index++;
        }
    }
}
