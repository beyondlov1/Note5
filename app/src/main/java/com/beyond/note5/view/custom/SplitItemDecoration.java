package com.beyond.note5.view.custom;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.beyond.note5.view.adapter.component.NoteRecyclerViewAdapter;

/**
 * @author: beyond
 * @date: 2019/2/8
 */

public class SplitItemDecoration extends RecyclerView.ItemDecoration {
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0, 5, 0, 5);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int left = child.getRight() + params.rightMargin;
            int right = child.getLeft() - params.leftMargin;
            int top = child.getTop() - params.topMargin;
            int bottom = child.getBottom() + params.bottomMargin;

            top = top - 5;
            bottom = child.getBottom() - child.getHeight();

            int childAdapterPosition = parent.getChildAdapterPosition(child);
            NoteRecyclerViewAdapter adapter = (NoteRecyclerViewAdapter) parent.getAdapter();

        }

    }
}
