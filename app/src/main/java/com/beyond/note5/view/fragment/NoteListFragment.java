package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.event.AddNoteEvent;
import com.beyond.note5.event.DeleteDeepNoteEvent;
import com.beyond.note5.event.DeleteNoteEvent;
import com.beyond.note5.event.HideFABEvent;
import com.beyond.note5.event.ModifyNoteDoneEvent;
import com.beyond.note5.event.RefreshNoteListEvent;
import com.beyond.note5.event.ScrollToNoteEvent;
import com.beyond.note5.event.ShowFABEvent;
import com.beyond.note5.event.UpdateNoteEvent;
import com.beyond.note5.event.UpdateNotePriorityEvent;
import com.beyond.note5.ocr.OCRCallBack;
import com.beyond.note5.ocr.OCRTask;
import com.beyond.note5.view.MainActivity;
import com.beyond.note5.view.adapter.AbstractNoteViewFragment;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/1/30
 */
@SuppressWarnings("unchecked")
public class NoteListFragment extends AbstractNoteViewFragment {

    public RecyclerView noteRecyclerView;

    @Override
    protected ViewGroup initViewGroup(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.fragment_note_list, container, false);
    }

    @Override
    protected void initView() {
        noteRecyclerView = viewGroup.findViewById(R.id.note_recycler_view);
        noteRecyclerView.setAdapter(recyclerViewAdapter);
        //设置显示格式
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        noteRecyclerView.setLayoutManager(staggeredGridLayoutManager);
    }

    @Override
    protected void initListener() {
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Deprecated
    private void testOcr(Note note) {
        if (note.getAttachments()==null||note.getAttachments().isEmpty()){
            return;
        }
        try (FileInputStream fileInputStream = new FileInputStream(note.getAttachments().get(0).getPath())){
            byte[] bytes=new byte[fileInputStream.available()];
            fileInputStream.read(bytes);
            String base64String = Base64.encodeToString(bytes, Base64.DEFAULT);
            base64String = "data:image/jpeg;base64,"+base64String;
            Log.d(MainActivity.class.getName(),base64String);
            OCRTask ocrTask = new OCRTask("2dcb07678f88957", false, base64String, "eng", new OCRCallBack() {

                @Override
                public void getOCRCallBackResult(String response) {
                    Log.d(MainActivity.class.getName(),response);
                }
            });
            ocrTask.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onAddSuccess(Note note) {
        int insertIndex = recyclerViewAdapter.getItemDataGenerator().getInsertIndex(note);
        data.add(insertIndex, note);
        recyclerViewAdapter.notifyInserted(note);
        noteRecyclerView.scrollToPosition(insertIndex);
        msg("添加成功");
    }

    @Override
    public void onDeleteSuccess(Note note) {
        int index = recyclerViewAdapter.getItemDataGenerator().getIndex(note);
        if (index!=-1){
            data.remove(note);
            recyclerViewAdapter.notifyRemoved(note);
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
                recyclerViewAdapter.notifyRemoved(oldNote);
                int insertIndex = recyclerViewAdapter.getItemDataGenerator().getInsertIndex(note);
                data.add(insertIndex, note);
                recyclerViewAdapter.notifyInserted(note);
                noteRecyclerView.scrollToPosition(insertIndex);
                msg("更新成功");
                break;
            }
        }
        EventBus.getDefault().post(new ModifyNoteDoneEvent(note));
    }

    @Override
    public void onFindAllSuccess(List<Note> allNote) {
        data.clear();
        recyclerViewAdapter.notifyFullRangeRemoved();
        data.addAll(allNote);
        recyclerViewAdapter.notifyFullRangeInserted();
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
    public void onReceived(UpdateNotePriorityEvent event) {
        notePresenter.updatePriority(event.get());
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(ScrollToNoteEvent event){
        Note note = event.get();
        int position = recyclerViewAdapter.getItemDataGenerator().getPosition(note);
        noteRecyclerView.scrollToPosition(position);
    }
}
