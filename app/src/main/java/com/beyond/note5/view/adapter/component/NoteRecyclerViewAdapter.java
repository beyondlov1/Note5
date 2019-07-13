package com.beyond.note5.view.adapter.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.note5.R;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.constant.LoadType;
import com.beyond.note5.event.ShowNoteDetailEvent;
import com.beyond.note5.event.note.UpdateNotePriorityEvent;
import com.beyond.note5.utils.BitmapUtil;
import com.beyond.note5.utils.HtmlUtil;
import com.beyond.note5.utils.PreferenceUtil;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.adapter.component.header.Header;
import com.beyond.note5.view.adapter.component.header.ItemDataGenerator;
import com.beyond.note5.view.adapter.component.viewholder.NoteViewHolder;
import com.beyond.note5.view.animator.svg.VectorAnimation;
import com.beyond.note5.view.animator.svg.VectorAnimationImpl;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/2
 */

public class NoteRecyclerViewAdapter extends DocumentRecyclerViewAdapter<Note, NoteViewHolder> {

    private static final String SHOULD_SHOW_LINK = "shouldShowLink";
    private boolean shouldLinkShow = false;
    private VectorAnimation longClickAnimation;


    public NoteRecyclerViewAdapter(Context context, ItemDataGenerator<Note, Header> itemDataGenerator) {
        super(context, itemDataGenerator);
        refreshShouldShowLink();
        longClickAnimation = new VectorAnimationImpl(context);
    }

    @Override
    protected NoteViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    protected void initHeaderView(int position, Header header, NoteViewHolder viewHolder) {
        viewHolder.title.setVisibility(View.VISIBLE);
        viewHolder.title.setText(header.getContent());
        viewHolder.title.setTextColor(ContextCompat.getColor(context, R.color.dark_yellow));
        viewHolder.content.setVisibility(View.GONE);
        viewHolder.content.setText(header.getContent());
        viewHolder.container.setOnClickListener(null);
        viewHolder.dataContainer.setBackground(null);
        viewHolder.link.setVisibility(View.GONE);
        viewHolder.image.setVisibility(View.GONE);
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        layoutParams.setFullSpan(true);
    }

    @Override
    protected void initHeadEvent(int position, Header header, NoteViewHolder viewHolder) {
        super.initHeadEvent(position, header, viewHolder);
        viewHolder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shouldLinkShow) {
                    PreferenceUtil.put(SHOULD_SHOW_LINK, false);
                } else {
                    PreferenceUtil.put(SHOULD_SHOW_LINK, true);
                }
                refreshShouldShowLink();
                notifyFullRangeChanged();
            }
        });
    }

    private void refreshShouldShowLink() {
        shouldLinkShow = PreferenceUtil.getBoolean(SHOULD_SHOW_LINK);
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void initContentView(NoteViewHolder viewHolder, Note note, int position) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(13);
        gradientDrawable.setStroke(1, ContextCompat.getColor(context, R.color.dark_gray));
        if (note.getPriority() != null && note.getPriority() > 0) {
            gradientDrawable.setStroke(2, ContextCompat.getColor(context, R.color.google_red));
        }

        viewHolder.dataContainer.setBackground(gradientDrawable);
        viewHolder.title.setVisibility(View.GONE);
        viewHolder.title.setTextColor(Color.DKGRAY);
        viewHolder.content.setVisibility(View.VISIBLE);
        viewHolder.content.setTextSize(12);
        if (StringUtils.isNotBlank(note.getTitle())) {
            viewHolder.content.setText(StringUtils.trim(note.getTitle()));
        } else {
            String content = StringUtils.trim(note.getContent());
            String url = HtmlUtil.getUrl2(content);
            if (url != null) {
                String contentWithoutUrl = content.replace(url, "");
                if (StringUtils.isBlank(contentWithoutUrl)) {
                    viewHolder.content.setText(content);
                } else {
                    viewHolder.content.setText(contentWithoutUrl);
                }
            } else {
                viewHolder.content.setText(content);
            }
        }

        if (shouldLinkShow && WebViewUtil.getUrlOrSearchUrl(note) != null) {
            viewHolder.link.setVisibility(View.VISIBLE);
        } else {
            viewHolder.link.setVisibility(View.GONE);
        }

        viewHolder.image.setVisibility(View.GONE);
        viewHolder.nonImageContainer.setVisibility(View.VISIBLE);
        if (!note.getAttachments().isEmpty()) {
            showImage(viewHolder, note);
        }

        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        if (itemDataGenerator.getSingleContentPositions().contains(position)) {
            layoutParams.setFullSpan(true);
        } else {
            layoutParams.setFullSpan(false);
        }
    }

    private void showImage(NoteViewHolder viewHolder, Note note) {
        Attachment attachment = note.getAttachments().get(0);
        if (new File(attachment.getPath()).exists()) {

           double factor =  BitmapUtil.getHeightWidthFactor(attachment.getPath());
            Bitmap placeHolderBitmap = BitmapUtil.getPlaceHolderBitmap(200,(int)(factor*200));
            BitmapUtil.asyncBitmap(context.getResources(),viewHolder.image,placeHolderBitmap,attachment.getPath());
            viewHolder.image.setAdjustViewBounds(true);
            viewHolder.image.setVisibility(View.VISIBLE);
            String newContent = StringUtils.replace(viewHolder.content.getText().toString(), "!file://" + attachment.getPath(), "");
            if (StringUtils.trim(newContent).isEmpty()) {
                viewHolder.image.setCornerTopLeftRadius(5);
                viewHolder.image.setCornerTopRightRadius(5);
                viewHolder.image.setCornerBottomLeftRadius(5);
                viewHolder.image.setCornerBottomRightRadius(5);
                viewHolder.nonImageContainer.setVisibility(View.GONE);
            } else {
                viewHolder.image.setCornerTopLeftRadius(5);
                viewHolder.image.setCornerTopRightRadius(5);
                viewHolder.image.setCornerBottomLeftRadius(0);
                viewHolder.image.setCornerBottomRightRadius(0);
                viewHolder.nonImageContainer.setVisibility(View.VISIBLE);
                viewHolder.content.setText(StringUtils.trim(newContent));
            }
        }
    }

    @Override
    protected void initContentEvent(final NoteViewHolder viewHolder, final Note note, final int position) {
        final int index = itemDataGenerator.getIndex(note);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContentDetail(v, itemDataGenerator.getContentData(), note, index);
            }
        });
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                longClickAnimation.setTarget(viewHolder.dataContainer);
                //横跨屏幕时，动画区分大小
                int animatedVectorRectResId;
                int animatedVectorRectReserveResId;
                if (itemDataGenerator.getSingleContentPositions().contains(position)) {
                    animatedVectorRectResId = R.drawable.animated_vector_rect_large;
                    animatedVectorRectReserveResId = R.drawable.animated_vector_rect_reserve_large;
                } else {
                    animatedVectorRectResId = R.drawable.animated_vector_rect;
                    animatedVectorRectReserveResId = R.drawable.animated_vector_rect_reserve;
                }
                if (isDefaultPriority(note)) {
                    longClickAnimation.setVectorDrawable(animatedVectorRectResId);
                } else {
                    longClickAnimation.setVectorDrawable(animatedVectorRectReserveResId);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    longClickAnimation.registerAnimationCallback(new Animatable2.AnimationCallback() {
                        @Override
                        public void onAnimationEnd(Drawable drawable) {
                            super.onAnimationEnd(drawable);
                            if (isDefaultPriority(note)) {
                                note.setPriority(DocumentConst.PRIORITY_FOCUS);
                            } else {
                                note.setPriority(DocumentConst.PRIORITY_DEFAULT);
                            }
                            EventBus.getDefault().post(new UpdateNotePriorityEvent(note));
                        }
                    });
                }
                longClickAnimation.start();
                return true;
            }
        });
        viewHolder.link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWebDetail(v, index);
            }
        });
    }

    private void showContentDetail(View v, List<Note> data, Note note, int index) {
        ShowNoteDetailEvent showNoteDetailEvent = new ShowNoteDetailEvent(v);
        showNoteDetailEvent.setData(data);
        showNoteDetailEvent.setIndex(index);
        showNoteDetailEvent.setLoadType(LoadType.CONTENT);
        EventBus.getDefault().post(showNoteDetailEvent);
    }

    private void showWebDetail(View v, int index) {
        ShowNoteDetailEvent showNoteDetailEvent = new ShowNoteDetailEvent(v);
        showNoteDetailEvent.setData(itemDataGenerator.getContentData());
        showNoteDetailEvent.setIndex(index);
        showNoteDetailEvent.setLoadType(LoadType.WEB);
        EventBus.getDefault().post(showNoteDetailEvent);
    }
}
