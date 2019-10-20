package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beyond.note5.R;
import com.beyond.note5.bean.Document;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public abstract class SearchResultFragment<T extends Document> extends Fragment {
    protected RecyclerView recyclerView;

    private RecyclerView.Adapter adapter;

    protected List<T> data = new ArrayList<>();

    @Inject
    protected Handler handler;

    private String searchKey;

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
            @SuppressWarnings("unchecked")
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                MyViewHolder myViewHolder = (MyViewHolder) holder;
                initViewHolder(data, position, myViewHolder);
                String title = data.get(position).getTitle();
                String content = data.get(position).getContent();
                if (StringUtils.isBlank(title)) {
                    myViewHolder.titleTextView.setVisibility(View.GONE);
                }
                if (StringUtils.isBlank(content)) {
                    myViewHolder.contentTextView.setVisibility(View.GONE);
                }
                Spanned titleSpanned = null;
                Spanned contentSpanned = null;
                String decoSearchKey = "<span style='color:red'>" + searchKey + "</span>";
                if (StringUtils.isNotBlank(title)) {
                    title = StringUtils.replace(title, searchKey, decoSearchKey);
                    titleSpanned = Html.fromHtml(title);
                }
                if (StringUtils.isNotBlank(content)) {
                    content = StringUtils.replace(content, searchKey, decoSearchKey);
                    contentSpanned = Html.fromHtml(content);
                }
                myViewHolder.titleTextView.setText(titleSpanned);
                myViewHolder.contentTextView.setText(contentSpanned);
                myViewHolder.container.setOnClickListener(getOnItemClickListener(data, position));
            }

            @Override
            public int getItemCount() {
                return data.size();
            }
        }

        ;
    }

    protected void initViewHolder(List<T> data, int position, MyViewHolder holder) {

    }

    public abstract View.OnClickListener getOnItemClickListener(List<T> data, int position);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_note_search_list, container, false);
        recyclerView = viewGroup.findViewById(R.id.note_recycler_view);
        recyclerView.setAdapter(adapter);
        //设置显示格式
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        return viewGroup;
    }


    public void setData(List<T> data) {
        this.data = data;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

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
}
