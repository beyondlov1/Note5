package com.beyond.note5.view.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.beyond.note5.R;
import com.beyond.note5.view.adapter.component.viewholder.NoteViewHolder;

public class TestFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_note_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.note_recycler_view);
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.item_note, parent, false);
                return new NoteViewHolder(view);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                NoteViewHolder noteViewHolder = (NoteViewHolder) holder;
                noteViewHolder.content.setText(position+"");
                noteViewHolder.dataContainer.setBackgroundColor(Color.BLUE);
            }

            @Override
            public int getItemCount() {
                return 40;
            }

        });
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setBackgroundColor(Color.RED);
        recyclerView.scrollToPosition(10);
        return view;
    }
}
