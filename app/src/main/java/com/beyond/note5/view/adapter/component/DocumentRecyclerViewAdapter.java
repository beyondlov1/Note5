package com.beyond.note5.view.adapter.component;

import android.content.Context;
import android.graphics.Color;
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
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;
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
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new DocumentRecyclerViewAdapter.MyViewHolder(view);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (data.isEmpty()) {
            return;
        }
        MyViewHolder viewHolder = (MyViewHolder) holder;
        if (headers.get(position) != null) {
            initHeaderView(viewHolder, position);
        } else {
            initContentView(position, viewHolder);
        }
    }

    private void initHeaderView(MyViewHolder viewHolder, int position) {
        viewHolder.title.setVisibility(View.VISIBLE);
        viewHolder.title.setText(headers.get(position).content);
        viewHolder.title.setTextColor(context.getResources().getColor(R.color.dark_yellow));
        viewHolder.content.setVisibility(View.GONE);
        viewHolder.content.setText(headers.get(position).content);
        viewHolder.container.setOnClickListener(null);
        viewHolder.dataContainer.setBackground(null);
        processHeaderFullSpan(viewHolder, position);
    }

    private void processHeaderFullSpan(MyViewHolder viewHolder, int position) {
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        layoutParams.setFullSpan(true);
    }

    private void initContentView(int position, MyViewHolder viewHolder) {
        int count = 0;
        for (int i = 0; i <= position; i++) {
            if (headers.get(i) != null) {
                count++;
            }
        }
        T t = data.get(position - count);
        initContentDisplay(viewHolder, t, position - count);
        initContentEvent(viewHolder, t, position - count);
    }

    protected void initContentDisplay(final MyViewHolder viewHolder, T document, int position) {
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
        viewHolder.title.setTextColor(Color.DKGRAY);
        viewHolder.content.setVisibility(View.VISIBLE);
        viewHolder.content.setTextSize(12);
        viewHolder.content.setText(StringUtils.trim(document.getContent()));

        processContentFullSpan(viewHolder, position);
    }

    private void initContentEvent(MyViewHolder viewHolder, final T t, final int position) {
        viewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(v, data, t, position);
            }
        });
    }

    private void processContentFullSpan(MyViewHolder viewHolder, int position) {
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        if (position>0&&position<data.size()-1) {
            Date lastTime = data.get(position - 1).getLastModifyTime();
            Date thisTime = data.get(position).getLastModifyTime();
            Date nextTime = data.get(position + 1).getLastModifyTime();
            if (DateUtils.truncatedEquals(lastTime,thisTime, Calendar.DATE)||DateUtils.truncatedEquals(nextTime,thisTime,Calendar.DATE)){
                layoutParams.setFullSpan(false);
            }else {
                layoutParams.setFullSpan(true);
            }
        }else if (position == 0){
            if(data.size() <= 1){
                layoutParams.setFullSpan(true);
                return;
            }
            Date thisTime = data.get(position).getLastModifyTime();
            Date nextTime = data.get(position + 1).getLastModifyTime();
            if (DateUtils.truncatedEquals(nextTime,thisTime,Calendar.DATE)){
                layoutParams.setFullSpan(false);
            }else {
                layoutParams.setFullSpan(true);
            }
        }else {
            Date lastTime = data.get(position - 1).getLastModifyTime();
            Date thisTime = data.get(position).getLastModifyTime();
            if (DateUtils.truncatedEquals(lastTime,thisTime,Calendar.DATE)){
                layoutParams.setFullSpan(false);
            }else {
                layoutParams.setFullSpan(true);
            }
        }
    }

    protected void onItemClick(View v, List<T> data, final T t, final int position) {
    }

    @Override
    public int getItemCount() {
        return data.size() + headers.size();
    }

    @SuppressWarnings("SameParameterValue")
    public void notifyRangeInserted(int positionStart, int itemCount) {
        int addedCount = initHeaderData(headers);
        if (addedCount > 0) {
            notifyItemRangeInserted(positionStart, itemCount + addedCount);
        } else {
            notifyItemRangeInserted(positionStart, itemCount);
        }
        notifyItemRangeChanged(0, data.size() + headers.size());
    }

    public void notifyFullRangeChanged() {
        int addedCount = initHeaderData(headers);
        if (addedCount > 0) {
            notifyItemRangeInserted(0, addedCount);
        }
        notifyItemRangeChanged(0, data.size() + headers.size());
    }

    //TODO
    public void notifyRangeChanged(int positionStart, int itemCount){
        int addedCount = initHeaderData(headers);
        if (addedCount > 0) {
            notifyItemRangeInserted(0, addedCount);
        }
        if (addedCount < 0) {
            notifyItemRangeRemoved(positionStart, itemCount - addedCount);
        }
        notifyItemRangeChanged(positionStart, data.size() + headers.size() - positionStart);
    }

    public void notifyRangeRemoved(int positionStart, int itemCount) {
        int addedCount = initHeaderData(headers);
        if (addedCount < 0) {
            notifyItemRangeRemoved(positionStart, itemCount - addedCount);
        } else {
            notifyItemRangeRemoved(positionStart, itemCount);
        }
        notifyItemRangeChanged(positionStart, data.size() + headers.size() - positionStart);
    }

    private int initHeaderData(SparseArray<Header> headers) {
        int oldHeaderCount = headers.size();
        headers.clear();
        addHeaderData(headers);
        return headers.size() - oldHeaderCount;
    }

    protected void addHeaderData(SparseArray<Header> headers){
        Date lastDate = null;
        int index = 0;
        for (Document datum : data) {
            Date lastModifyTime = datum.getLastModifyTime();
            if (lastDate == null) {
                headers.put(index + headers.size(), new DocumentRecyclerViewAdapter.Header(index + headers.size(), DateFormatUtils.format(lastModifyTime, "yyyy-MM-dd")));
            }
            if (lastDate != null && !DateUtils.truncatedEquals(lastModifyTime, lastDate, Calendar.DATE)) {
                headers.put(index + headers.size(), new DocumentRecyclerViewAdapter.Header(index + headers.size(), DateFormatUtils.format(lastModifyTime, "yyyy-MM-dd")));
            }
            lastDate = lastModifyTime;
            index++;
        }
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
