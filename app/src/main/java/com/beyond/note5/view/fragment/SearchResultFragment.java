package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.component.DaggerNoteListFragmentComponent;
import com.beyond.note5.component.module.NoteModule;
import com.beyond.note5.component.module.NoteSyncModule;
import com.beyond.note5.constant.LoadType;
import com.beyond.note5.event.ShowNoteDetailEvent;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.SyncPresenter;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.view.SyncView;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SearchResultFragment extends Fragment {
    protected RecyclerView recyclerView;

    private RecyclerView.Adapter adapter;

    protected List<Note> data = new ArrayList<>();

    @Inject
    protected NotePresenter notePresenter;

    @Inject
    protected SyncPresenter syncPresenter;

    @Inject
    protected Handler handler;

    private boolean dataFromOutSide = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDependency();
    }

    private void initDependency() {
        adapter = new RecyclerView.Adapter() {

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_note_search, parent, false);
                return new MyViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                MyViewHolder myViewHolder = (MyViewHolder) holder;
                String title = data.get(position).getTitle();
                String content = data.get(position).getContent();
                if (StringUtils.isBlank(title)){
                    myViewHolder.titleTextView.setVisibility(View.GONE);
                }
                if (StringUtils.isBlank(content)){
                    myViewHolder.contentTextView.setVisibility(View.GONE);
                }
                myViewHolder.titleTextView.setText(title);
                myViewHolder.contentTextView.setText(content);
                myViewHolder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputMethodUtil.hideKeyboard(v);
                        ShowNoteDetailEvent showNoteDetailEvent = new ShowNoteDetailEvent(v);
                        showNoteDetailEvent.setData(data);
                        showNoteDetailEvent.setIndex(position);
                        showNoteDetailEvent.setLoadType(LoadType.CONTENT);
                        EventBus.getDefault().post(showNoteDetailEvent);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return data.size();
            }

            class MyViewHolder extends RecyclerView.ViewHolder{

                View container;
                TextView titleTextView;
                TextView contentTextView;
                View linkView;

                MyViewHolder(View itemView) {
                    super(itemView);
                    container = itemView.findViewById(R.id.item_note_data_container);
                    titleTextView = itemView.findViewById(R.id.item_note_title);
                    contentTextView = itemView.findViewById(R.id.item_note_content);
                    linkView = itemView.findViewById(R.id.item_note_link);
                }
            }
        };
        DaggerNoteListFragmentComponent.builder()
                .noteModule(new NoteModule(new NoteViewAdapter()))
                .noteSyncModule(new NoteSyncModule(new SyncView() {
                    @Override
                    public void onSyncSuccess(String message) {

                    }

                    @Override
                    public void onSyncFail(String message) {

                    }
                }))
                .build().inject(this);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_note_search_list, container, false);
        recyclerView = viewGroup.findViewById(R.id.note_recycler_view);
        recyclerView.setAdapter(adapter);
        //设置显示格式
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        //显示所有Note
        if (!dataFromOutSide) {
            notePresenter.findAll();
        }
        return viewGroup;
    }


    public void setData(List<Note> data){
        this.data = data;
        dataFromOutSide = true;
    }
}
