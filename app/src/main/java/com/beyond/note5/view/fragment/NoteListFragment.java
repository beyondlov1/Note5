package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.beyond.note5.R;
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
import com.beyond.note5.view.NoteView;
import com.beyond.note5.view.adapter.NoteRecyclerViewAdapter;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by beyond on 2019/1/30.
 */

public class NoteListFragment extends AbstractDocumentFragment implements NoteView {

    private List<Note> data = new ArrayList<>();

    private RecyclerView noteRecyclerView;

    private NoteRecyclerViewAdapter noteRecyclerViewAdapter;

    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    @Inject
    NotePresenter notePresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noteRecyclerViewAdapter = new NoteRecyclerViewAdapter(this.getContext(), data, getFragmentManager());
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

    @Override
    public void onStart() {
        super.onStart();
        //显示所有Note
        notePresenter.findAllNote();
    }

    private void initView(ViewGroup viewGroup) {
        noteRecyclerView = viewGroup.findViewById(R.id.note_recycler_view);
        noteRecyclerView.setAdapter(noteRecyclerViewAdapter);
        //设置显示格式
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        noteRecyclerView.setLayoutManager(staggeredGridLayoutManager);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoteReceived(AddNoteEvent event) {
        notePresenter.addNote(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoteReceived(UpdateNoteEvent event) {
        notePresenter.updateNote(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoteReceived(DeleteNoteEvent event) {
        notePresenter.deleteNote(event.get());
    }

    @Override
    public void onAddNoteSuccess(Note note) {
        data.add(0, note);
//        noteRecyclerView.getAdapter().notifyDataSetChanged();
        noteRecyclerView.getAdapter().notifyItemInserted(0);
        noteRecyclerView.getAdapter().notifyItemRangeChanged(1, data.size() - 1);
        noteRecyclerView.scrollToPosition(0);
        msg("添加成功");
    }

    @Override
    public void msg(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFindAllNoteSuccess(List<Note> allNote) {
        int previousSize = data.size();
        data.clear();
        noteRecyclerView.getAdapter().notifyItemRangeRemoved(0, previousSize);
        data.addAll(allNote);
        noteRecyclerView.getAdapter().notifyItemRangeInserted(0, data.size());
    }

    @Override
    public void deleteNoteFail(Note note) {
        msg("删除失败");
    }

    @Override
    public void deleteNoteSuccess(Note note) {
        int index = data.indexOf(note);
        data.remove(note);
        if (index!=-1){
            noteRecyclerView.getAdapter().notifyItemRemoved(index);
            noteRecyclerView.getAdapter().notifyItemRangeChanged(index, data.size()-index);
            msg("删除成功");
        }
    }

    @Override
    public void updateNoteSuccess(Note note) {
        Iterator<Note> iterator = data.iterator();
        while (iterator.hasNext()) {
            Note oldNote = iterator.next();
            if (StringUtils.equals(oldNote.getId(), note.getId())) {
                iterator.remove();
                data.add(0, note);
                noteRecyclerView.getAdapter().notifyItemRangeChanged(0, data.size());
                msg("更新成功");
                break;
            }
        }
    }

    @Override
    public void updateNoteFail(Note note) {
        msg("更新失败");
    }
}
