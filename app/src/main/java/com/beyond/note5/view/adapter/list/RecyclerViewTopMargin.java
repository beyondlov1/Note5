package com.beyond.note5.view.adapter.list;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.beyond.note5.view.adapter.list.viewholder.ItemType;

/**
 * @author: beyond
 * @date: 2019/8/2
 */

public class RecyclerViewTopMargin extends RecyclerView.ItemDecoration {

    private int mSize = 150;

//    @Override
//    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
//
//        final int left = parent.getPaddingLeft();
//        final int right = parent.getWidth() - parent.getPaddingRight();
//        final int childCount = parent.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            final View child = parent.getChildAt(i);
//            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
//                    .getLayoutParams();
//            final int top = child.getTop();
//
//            Paint paint = new Paint();
//            paint.setColor(Color.RED);
//            paint.setTextSize(30);
//            c.drawText("hello",left, top,paint);
//        }
//    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getTop();

            Paint paint = new Paint();
            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(1);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) child.getLayoutParams();

            DocumentRecyclerViewAdapter adapter = (DocumentRecyclerViewAdapter)parent.getAdapter();
            if (layoutParams.isFullSpan() && i != 0 && adapter.getItemType(i) != ItemType.HEAD && !(adapter instanceof TodoRecyclerViewAdapter)){
                c.drawLine(left,top,right,top,paint);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        outRect.set(0, position == 0 ? mSize : 0, 0, 0);
    }
}
