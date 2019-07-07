package com.beyond.note5.view.adapter.component.viewholder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.beyond.note5.R;
import com.shehuan.niv.NiceImageView;

public class NoteViewHolder extends DocumentViewHolder {

    public View container;
    public View nonImageContainer;
    public TextView title;
    public View dataContainer;
    public TextView content;
    public ImageButton link;
    public NiceImageView image;

    public NoteViewHolder(View itemView) {
        super(itemView);

        container = itemView.findViewById(R.id.item_note_container);
        dataContainer = itemView.findViewById(R.id.item_note_data_container);
        title = itemView.findViewById(R.id.item_note_title);
        content = itemView.findViewById(R.id.item_note_content);
        link = itemView.findViewById(R.id.item_note_link);
        image = itemView.findViewById(R.id.item_note_image);
        nonImageContainer = itemView.findViewById(R.id.item_note_non_image_container);
    }

}
