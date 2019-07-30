package com.beyond.note5.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Account;
import com.beyond.note5.bean.Note;
import com.beyond.note5.event.AddNoteAllSuccessEvent;
import com.beyond.note5.event.HideFABEvent;
import com.beyond.note5.event.RefreshNoteListEvent;
import com.beyond.note5.event.ScrollNoteToTopEvent;
import com.beyond.note5.event.ScrollToNoteEvent;
import com.beyond.note5.event.ShowFABEvent;
import com.beyond.note5.event.SyncNoteListEvent;
import com.beyond.note5.event.UpdateNoteAllSuccessEvent;
import com.beyond.note5.event.note.AddNoteSuccessEvent;
import com.beyond.note5.event.note.DeleteNoteSuccessEvent;
import com.beyond.note5.event.note.UpdateNotePriorityEvent;
import com.beyond.note5.event.note.UpdateNoteSuccessEvent;
import com.beyond.note5.inject.BeanInjectUtils;
import com.beyond.note5.inject.Qualifier;
import com.beyond.note5.inject.SingletonInject;
import com.beyond.note5.ocr.OCRCallBack;
import com.beyond.note5.ocr.OCRTask;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.presenter.NoteSyncPresenterImpl;
import com.beyond.note5.presenter.SyncPresenter;
import com.beyond.note5.utils.RecyclerViewUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.DavLoginActivity;
import com.beyond.note5.view.MainActivity;
import com.beyond.note5.view.NoteView;
import com.beyond.note5.view.SyncView;
import com.beyond.note5.view.adapter.component.DocumentRecyclerViewAdapter;
import com.beyond.note5.view.adapter.component.NoteRecyclerViewAdapter;
import com.beyond.note5.view.adapter.component.header.ItemDataGenerator;
import com.beyond.note5.view.adapter.component.header.ReadFlagItemDataGenerator;
import com.beyond.note5.view.adapter.view.DocumentViewBase;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/1/30
 */
@SuppressWarnings("unchecked")
public class NoteListFragment extends Fragment {

    protected RecyclerView recyclerView;
    private RefreshLayout refreshLayout;

    protected DocumentRecyclerViewAdapter recyclerViewAdapter;

    protected List<Note> data = new ArrayList<>();

    protected NotePresenter notePresenter;

    protected SyncPresenter syncPresenter;

    @SingletonInject
    @Qualifier(implementClass = MyNoteView.class)
    protected NoteView noteView;

    @SingletonInject
    @Qualifier(implementClass = MySyncView.class)
    protected SyncView syncView;

    @SingletonInject
    private Handler handler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        recyclerViewAdapter = new NoteRecyclerViewAdapter(this.getContext(), new ReadFlagItemDataGenerator<>(data));
        initInjection();
    }

    private void initInjection() {
        BeanInjectUtils.inject(this);
        notePresenter = new NotePresenterImpl(noteView);
        syncPresenter = new NoteSyncPresenterImpl(syncView, handler);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_note_list, container, false);
        initView(viewGroup);
        initEvent(viewGroup);
        //显示所有Note
        notePresenter.findAll();
        return viewGroup;
    }

    protected void initView(ViewGroup viewGroup) {
        refreshLayout = viewGroup.findViewById(R.id.note_refresh_layout);
        recyclerView = viewGroup.findViewById(R.id.note_recycler_view);
        recyclerView.setAdapter(recyclerViewAdapter);
        //设置显示格式
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
    }

    protected void initEvent(ViewGroup viewGroup) {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                if (!checkAccount(refreshLayout)) {
                    return;
                }
                syncPresenter.sync();
            }
        });
        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                //上划
                if (velocityY < 0) {
                    EventBus.getDefault().post(new ShowFABEvent(R.id.note_recycler_view));
                }
                return false;
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //未到滚动的高度
                if (!recyclerView.canScrollVertically(1) && !recyclerView.canScrollVertically(-1)) {
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

    private boolean checkAccount(@NonNull RefreshLayout refreshLayout) {
        List<Account> all = MyApplication.getInstance().getAccountModel().findAllValid();
        if (all == null || all.isEmpty()) {
            refreshLayout.finishRefresh();
            Intent intent = new Intent(getContext(), DavLoginActivity.class);
            startActivity(intent);
            return false;
        }
        return true;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Deprecated
    private void testOcr(Note note) {
        if (note.getAttachments() == null || note.getAttachments().isEmpty()) {
            return;
        }
        try (FileInputStream fileInputStream = new FileInputStream(note.getAttachments().get(0).getPath())) {
            byte[] bytes = new byte[fileInputStream.available()];
            fileInputStream.read(bytes);
            String base64String = Base64.encodeToString(bytes, Base64.DEFAULT);
            base64String = "data:image/jpeg;base64," + base64String;
            Log.d(MainActivity.class.getName(), base64String);
            OCRTask ocrTask = new OCRTask("2dcb07678f88957", false, base64String, "eng", new OCRCallBack() {

                @Override
                public void getOCRCallBackResult(String response) {
                    Log.d(MainActivity.class.getName(), response);
                }
            });
            ocrTask.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(AddNoteSuccessEvent event) {
        noteView.onAddSuccess(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
    public void onReceived(UpdateNoteSuccessEvent event) {
        noteView.onUpdateSuccess(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(AddNoteAllSuccessEvent event) {
        notePresenter.findAll();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
    public void onReceived(UpdateNoteAllSuccessEvent event) {
        notePresenter.findAll();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(UpdateNotePriorityEvent event) {
        notePresenter.updatePriority(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(DeleteNoteSuccessEvent event) {
        noteView.onDeleteSuccess(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(RefreshNoteListEvent event) {
        notePresenter.findAll();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(SyncNoteListEvent event) {
        refreshLayout.autoRefresh();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(ScrollToNoteEvent event) {
        Note note = event.get();
        int position = recyclerViewAdapter.getItemDataGenerator().getPosition(note);
        recyclerView.scrollToPosition(position);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(ScrollNoteToTopEvent event) {
        Note note = event.get();
        int position = recyclerViewAdapter.getItemDataGenerator().getPosition(note);
        RecyclerViewUtil.tryScrollItemToTop(recyclerView, position);
    }

    public void scrollTo(Integer index) {
        ItemDataGenerator itemDataGenerator = recyclerViewAdapter.getItemDataGenerator();
        Object note = itemDataGenerator.getContentData().get(index);
        int position = itemDataGenerator.getPosition(note);
        recyclerView.scrollToPosition(position);
    }

    public View findViewBy(Integer index) {
        ItemDataGenerator itemDataGenerator = recyclerViewAdapter.getItemDataGenerator();
        Object note = itemDataGenerator.getContentData().get(index);
        int position = itemDataGenerator.getPosition(note);
        return recyclerView.getLayoutManager().findViewByPosition(position);
    }

    @Override
    public void onDestroy() {
        System.out.println("NoteListFragment onDestroy");
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    private class MyNoteView extends DocumentViewBase<Note> implements NoteView {

        @Override
        public void onAddSuccess(Note note) {
            super.onAddSuccess(note);
            ToastUtil.toast(getContext(), "添加成功");
        }

        public DocumentRecyclerViewAdapter getRecyclerViewAdapter() {
            return recyclerViewAdapter;
        }

        public RecyclerView getRecyclerView() {
            return recyclerView;
        }

        public List<Note> getData() {
            return data;
        }
    }

    private class MySyncView implements SyncView {
        @Override
        public void onSyncSuccess(String msg) {
            refreshLayout.finishRefresh();
            ToastUtil.toast(getContext(), msg);
        }

        @Override
        public void onSyncFail(String msg) {
            refreshLayout.finishRefresh();
            ToastUtil.toast(getContext(), msg);
        }
    }
}
