package com.beyond.note5.view.adapter.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.beyond.note5.R;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Element;
import com.beyond.note5.view.adapter.list.header.Header;
import com.beyond.note5.view.adapter.list.header.ItemDataGenerator;
import com.beyond.note5.view.adapter.list.viewholder.DocumentViewHolder;
import com.beyond.note5.view.adapter.list.viewholder.ItemType;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/1/30
 */

public abstract class DocumentRecyclerViewAdapter<T extends Document, S extends DocumentViewHolder> extends RecyclerView.Adapter {

    protected Context context;

    private List<Element> itemData;

    private SparseArray<ItemType> itemTypeMap;

    ItemDataGenerator<T, ? extends Header> itemDataGenerator;

    static final int[] colorResIds = new int[]{
            R.color.google_red,
            R.color.dark_yellow,
            R.color.google_blue,
            R.color.google_green
    };

    DocumentRecyclerViewAdapter(Context context, ItemDataGenerator<T, ? extends Header> itemDataGenerator) {
        this.context = context;
        this.itemDataGenerator = itemDataGenerator;
        this.itemData = itemDataGenerator.getItemData();
        this.itemTypeMap  = new SparseArray<>();
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
        initVisibility(viewHolder);
        if (itemData.get(position) instanceof Header) {
            Header header = (Header) itemData.get(position);
            initHeader(position, header, viewHolder);
            itemTypeMap.put(position,ItemType.HEAD);
        } else if (itemData.get(position) instanceof Document) {
            T document = (T) itemData.get(position);
            initContent(position, document, viewHolder);
            itemTypeMap.put(position,ItemType.CONTENT);
        }else {
            Log.d(getClass().getSimpleName(),"position:"+position+", data:" + itemData.get(position));
        }
    }

    public ItemType getItemType(int position){
        return itemTypeMap.get(position);
    }

    private void initHeader(int position, Header header, S viewHolder) {
        initHeaderView(position, header, viewHolder);
        initHeadEvent(position, header, viewHolder);
    }

    protected abstract void initVisibility(S viewHolder);

    protected abstract void initHeaderView(int position, Header header, S viewHolder);

    protected void initHeadEvent(int position, Header header, S viewHolder) {
        //do nothing
    }

    private void initContent(int position, T document, S viewHolder) {
        initContentView(viewHolder, document, position);
        initContentEvent(viewHolder, document,position);
    }

    protected abstract void initContentView(final S viewHolder, T document, int position);

    protected abstract void initContentEvent(S viewHolder, final T t, int position);

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

        if (addedCount > 1) {
            int insertedPosition = itemDataGenerator.getPosition(t);
            notifyItemRangeInserted(insertedPosition - addedCount + 1, addedCount);
            notifyItemRangeChanged(insertedPosition + 1, itemDataGenerator.getItemData().size() - insertedPosition - addedCount);
        }

        notifyFullRangeChanged(); // 保证能更新到header， 但是em....
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

    @SuppressWarnings("WeakerAccess")
    public void notifyFullRangeChanged() {
        notifyItemRangeChanged(0, itemDataGenerator.getItemData().size());
    }

    /**
     * 在位置不变的情况下更新视图
     * @param t note
     */
    public void notifyChanged(T t){
        int position = itemDataGenerator.getPosition(t);
        notifyItemChanged(position);
    }

    /**
     * 刷新itemData
     *
     * @return 增加的个数
     */
    private int refreshItemData() {
        int oldCount = itemDataGenerator.getItemData().size();
        itemDataGenerator.refresh();
        return itemDataGenerator.getItemData().size() - oldCount;
    }

    public ItemDataGenerator getItemDataGenerator() {
        return itemDataGenerator;
    }

    boolean isDefaultPriority(T t) {
        return t.getPriority() == null || t.getPriority() == 0;
    }

}
