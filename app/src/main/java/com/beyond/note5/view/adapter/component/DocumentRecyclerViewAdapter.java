package com.beyond.note5.view.adapter.component;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.beyond.note5.R;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Element;
import com.beyond.note5.view.adapter.component.header.Header;
import com.beyond.note5.view.adapter.component.header.ItemDataGenerator;
import com.beyond.note5.view.adapter.component.viewholder.DocumentViewHolder;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/1/30
 */

public abstract class DocumentRecyclerViewAdapter<T extends Document, S extends DocumentViewHolder> extends RecyclerView.Adapter {

    protected Context context;

    protected List<Element> itemData;

    protected ItemDataGenerator<T> itemDataGenerator;

    static final int[] colorResIds = new int[]{
            R.color.google_red,
            R.color.dark_yellow,
            R.color.google_blue,
            R.color.google_green
    };

    public DocumentRecyclerViewAdapter(Context context, ItemDataGenerator<T> itemDataGenerator) {
        this.context = context;
        this.itemDataGenerator = itemDataGenerator;
        this.itemData = itemDataGenerator.getItemData();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return getViewHolder(parent, viewType);
    }
    
    protected abstract S getViewHolder(ViewGroup parent, int viewType);

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (itemDataGenerator.getContentData() == null || itemDataGenerator.getContentData().isEmpty()) {
            return;
        }
        S viewHolder = (S) holder;
        if (itemData.get(position) instanceof Header) {
            Header header = (Header) itemData.get(position);
            initHeaderView(position, header, viewHolder);
        } else if (itemData.get(position) instanceof Document) {
            T document = (T) itemData.get(position);
            initContentView(position, document, viewHolder);
        }
    }

    private void initHeaderView(int position, Header header, S viewHolder){
        initHeaderDisplay(position,header,viewHolder);
        initHeadEvent(position,header,viewHolder);
    }

    protected abstract void initHeaderDisplay(int position, Header header, S viewHolder);

    protected void initHeadEvent(int position, Header header, S viewHolder){
        //do nothing
    }

    private void initContentView(int position, T document, S viewHolder) {
        initContentDisplay(viewHolder, document, position);
        initContentEvent(viewHolder, document);
    }

    protected abstract void initContentDisplay(final S viewHolder, T document, int position) ;

    protected abstract void initContentEvent(S viewHolder, final T t) ;

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
            notifyFullRangeChanged(); // 更新全部， 避免跨列展示的item没有跨列效果： 比如删除后边的， 剩下前面那一个是单独的时候

        }

        if (addedCount < -1) {
            notifyItemRangeRemoved(removedPosition + addedCount + 1, -addedCount);
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

}
