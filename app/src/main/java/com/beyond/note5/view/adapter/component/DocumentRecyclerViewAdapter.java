package com.beyond.note5.view.adapter.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beyond.note5.R;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Element;
import com.beyond.note5.view.adapter.component.header.Header;
import com.beyond.note5.view.adapter.component.header.ItemDataGenerator;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/1/30
 */

public class DocumentRecyclerViewAdapter<T extends Document> extends RecyclerView.Adapter {

    protected Context context;
    protected List<Element> itemData;

    protected ItemDataGenerator<T> itemDataGenerator;

    private static final int[] colorResIds = new int[]{
            R.color.google_blue,
            R.color.google_green,
            R.color.google_red,
            R.color.google_yellow
    };

    public DocumentRecyclerViewAdapter(Context context, ItemDataGenerator<T> itemDataGenerator) {
        this.context = context;
        this.itemDataGenerator = itemDataGenerator;
        this.itemData = itemDataGenerator.getItemData();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new DocumentRecyclerViewAdapter.MyViewHolder(view);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (itemDataGenerator.getContentData() == null || itemDataGenerator.getContentData().isEmpty()) {
            return;
        }
        MyViewHolder viewHolder = (MyViewHolder) holder;
        if (itemData.get(position) instanceof Header) {
            Header header = (Header) itemData.get(position);
            initHeaderView(position, header, viewHolder);
        } else if (itemData.get(position) instanceof Document) {
            T document = (T) itemData.get(position);
            initContentView(position, document, viewHolder);
        }
    }

    private void initHeaderView(int position, Header header, MyViewHolder viewHolder) {
        viewHolder.title.setVisibility(View.VISIBLE);
        viewHolder.title.setText(header.getContent());
        viewHolder.title.setTextColor(context.getResources().getColor(R.color.dark_yellow));
        viewHolder.content.setVisibility(View.GONE);
        viewHolder.content.setText(header.getContent());
        viewHolder.container.setOnClickListener(null);
        viewHolder.dataContainer.setBackground(null);
        processHeaderFullSpan(viewHolder);
    }

    private void processHeaderFullSpan(MyViewHolder viewHolder) {
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        layoutParams.setFullSpan(true);
    }

    private void initContentView(int position, T document, MyViewHolder viewHolder) {
        initContentDisplay(viewHolder, document, position);
        initContentEvent(viewHolder, document);
    }

    protected void initContentDisplay(final MyViewHolder viewHolder, Document document, int position) {
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

    private void processContentFullSpan(MyViewHolder viewHolder, int position) {
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        if (itemDataGenerator.getSingleContentPositions().contains(position)) {
            layoutParams.setFullSpan(true);
        } else {
            layoutParams.setFullSpan(false);
        }
    }

    private void initContentEvent(MyViewHolder viewHolder, final T t) {
        final int index = itemDataGenerator.getIndex(t);
        viewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(v, itemDataGenerator.getContentData(), t, index);
            }
        });
    }

    protected void onItemClick(View v, List<T> data, final T t, final int index) {
    }

    @Override
    public int getItemCount() {
        return itemData.size();
    }


    public void notifyFullRangeInserted() {
        int addedCount = refreshItemData();
        notifyItemRangeInserted(0, addedCount);
    }

    public void notifyInserted(T t) {
        int addedCount = refreshItemData();
        if (addedCount == 1) {
            int insertedPosition = itemDataGenerator.getPosition(t);
            notifyItemInserted(insertedPosition);
            notifyItemRangeChanged(insertedPosition + 1, itemDataGenerator.getItemData().size() - insertedPosition - 1);
        }

        if (addedCount >1) {
            int insertedPosition = itemDataGenerator.getPosition(t);
            notifyItemRangeInserted(insertedPosition -addedCount + 1, addedCount);
            notifyItemRangeChanged(insertedPosition + 1, itemDataGenerator.getItemData().size() - insertedPosition -addedCount);
        }
    }

    public void notifyFullRangeRemoved() {
        int addedCount = refreshItemData();
        notifyItemRangeRemoved(0, -addedCount);
    }

    public void notifyRemoved(T t) {
        int removedPosition = itemDataGenerator.getPosition(t);
        int addedCount = refreshItemData();
        if (addedCount == -1) {
            notifyItemRemoved(removedPosition);
//            notifyItemRangeChanged(removedPosition,itemDataGenerator.getItemData().size()-removedPosition-1);
            notifyFullRangeChanged(); // 更新全部， 避免跨列展示的item没有跨列效果： 比如删除后边的， 剩下前面那一个是单独的时候

        }

        if (addedCount < -1) {
            notifyItemRangeRemoved(removedPosition + addedCount + 1, -addedCount);
//            notifyItemRangeChanged(removedPosition-1,itemDataGenerator.getItemData().size()-removedPosition-2);
//            notifyFullRangeChanged(); // 更新全部， 避免跨列展示的item没有跨列效果： 比如删除后边的， 剩下前面那一个是单独的时候
            notifyDataSetChanged();
        }

    }

    public void notifyFullRangeChanged() {
        notifyItemRangeChanged(0, itemDataGenerator.getItemData().size());
    }

    private int refreshItemData() {
        int oldCount = itemDataGenerator.getItemData().size();
        itemDataGenerator.refresh();
        return itemDataGenerator.getItemData().size() - oldCount;
    }

    public ItemDataGenerator getItemDataGenerator() {
        return itemDataGenerator;
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


}
