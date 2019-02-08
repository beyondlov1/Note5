package com.beyond.note5.view.adapter.component;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beyond.note5.R;
import com.beyond.note5.bean.Document;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/1/30
 */

public class DocumentRecyclerViewAdapter<T extends Document> extends RecyclerView.Adapter {

    protected Context context;
    protected List<T> data;

    private SparseArray<Header> headers = new SparseArray<>();

    private static final int[] colorResIds = new int[]{
            R.color.google_blue,
            R.color.google_green,
            R.color.google_red,
            R.color.google_yellow
    };

    public DocumentRecyclerViewAdapter(Context context, List<T> data) {
        this.context = context;
        this.data = data;
        headers.put(0,new Header(0,"0"));
        headers.put(2,new Header(2,"2"));
        headers.put(6,new Header(6,"6"));
        headers.put(8,new Header(8,"80"));
        headers.put(14,new Header(14,"14"));
    }

    public DocumentRecyclerViewAdapter(Context context, List<T> data, SparseArray<Header> headers) {
        this.context = context;
        this.data = data;
        this.headers = headers;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new DocumentRecyclerViewAdapter.MyViewHolder(view);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder viewHolder = (MyViewHolder) holder;

        if (data.isEmpty()) {
            return;
        }

//        T t = data.get(position );
//        initDisplay(viewHolder, t, position );
//        initEvent(viewHolder, t, position );

        int count = 0;
        for (int i = 0; i <= position; i++) {
            if (headers.get(i)!=null){
                count++;
            }
        }
        if (headers.get(position)!=null) {
            initHeaderDisplay(viewHolder, position);
        } else {
            T t = data.get(position - count);
            initDisplay(viewHolder, t, position - count);
            initEvent(viewHolder, t, position - count);
        }
    }

    private void initHeaderDisplay(MyViewHolder viewHolder, int position) {
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) ((MyViewHolder) viewHolder).itemView.getLayoutParams();
        layoutParams.setFullSpan(true);
        viewHolder.title.setVisibility(View.VISIBLE);
        viewHolder.title.setText(headers.get(position).content);
        viewHolder.content.setVisibility(View.GONE);
        viewHolder.content.setText(headers.get(position).content);
        viewHolder.container.setOnClickListener(null);
        viewHolder.dataContainer.setBackground(null);
    }

    private void initEvent(MyViewHolder viewHolder, final T t, final int position) {
        viewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(v, data, t, position);
            }
        });
    }

    protected void onItemClick(View v, List<T> data, final T t, final int position) {

    }

    protected void initDisplay(final MyViewHolder viewHolder, T document, int position) {
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) ((MyViewHolder) viewHolder).itemView.getLayoutParams();
        layoutParams.setFullSpan(false);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(13);
        gradientDrawable.setStroke(1, ContextCompat.getColor(context, R.color.dark_gray));
        viewHolder.dataContainer.setBackground(gradientDrawable);
        if (StringUtils.isNotBlank(document.getTitle())) {
            viewHolder.title.setText(StringUtils.trim(document.getTitle()));
            viewHolder.title.setVisibility(View.VISIBLE);
        } else {
            viewHolder.title.setVisibility(View.GONE);
        }
        viewHolder.content.setVisibility(View.VISIBLE);
        viewHolder.content.setTextSize(12);
        viewHolder.content.setText(StringUtils.trim(document.getContent()));
    }


    @Override
    public int getItemCount() {
        return data.size() + headers.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        View container;
        View dataContainer;
        TextView title;
        TextView content;

        MyViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.item_container);
            dataContainer = itemView.findViewById(R.id.item_data_container);
            title = itemView.findViewById(R.id.item_title);
            content = itemView.findViewById(R.id.item_content);
        }
    }

    public static class Header {
        private int position;
        private String content;

        public Header(int position, String content) {
            this.position = position;
            this.content = content;
        }
    }
}
