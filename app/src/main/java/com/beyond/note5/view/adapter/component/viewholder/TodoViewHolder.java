package com.beyond.note5.view.adapter.component.viewholder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.beyond.note5.R;

public class TodoViewHolder extends DocumentViewHolder {

    public View container;
    public View dataContainer;
    public TextView title;
    public CheckBox checkbox;
    public TextView content;

    public TodoViewHolder(View itemView) {
        super(itemView);

        container = itemView.findViewById(R.id.item_todo_container);
        dataContainer = itemView.findViewById(R.id.item_todo_data_container);
        checkbox = itemView.findViewById(R.id.item_todo_checkbox);
        title = itemView.findViewById(R.id.item_todo_title);
        content = itemView.findViewById(R.id.item_todo_content);
    }
}
