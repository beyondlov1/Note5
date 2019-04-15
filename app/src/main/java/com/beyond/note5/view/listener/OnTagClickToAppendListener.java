package com.beyond.note5.view.listener;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagFlowLayout;
import com.zhy.view.flowlayout.TagView;

/**
 * @author beyondlov1
 * @date 2019/03/29
 */
public class OnTagClickToAppendListener implements TagFlowLayout.OnTagClickListener {

    private EditText target;

    public OnTagClickToAppendListener(EditText target) {
        this.target = target;
    }

    @Override
    public boolean onTagClick(View view, int position, FlowLayout parent) {
        if (view instanceof TagView) {
            TagView tagView = (TagView) view;
            View childView = tagView.getTagView();
            if (childView instanceof TextView) {
                TextView textView = (TextView) childView;
                CharSequence text = textView.getText();
                appendToEditText(target, text);
            }
        }
        return true;
    }

    private void appendToEditText(EditText editText, CharSequence source) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        Editable edit = editText.getEditableText();//获取EditText的文字
        if (start < 0 || start >= edit.length()) {
            edit.append(source);
        } else {
            edit.replace(start, end, source);//光标所在位置插入文字
        }
    }
}
