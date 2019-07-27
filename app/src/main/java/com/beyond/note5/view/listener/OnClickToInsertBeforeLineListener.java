package com.beyond.note5.view.listener;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.beyond.note5.MyApplication;
import com.beyond.note5.utils.PreferenceUtil;
import com.beyond.note5.view.custom.MarkdownAutoRenderEditText;
import com.beyond.note5.view.markdown.decorate.DefaultMarkdownDecorator;

public class OnClickToInsertBeforeLineListener implements View.OnClickListener {

    private EditText editText;

    public OnClickToInsertBeforeLineListener(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            TextView textView = (TextView) v;
            CharSequence text = textView.getText();
            if (hasPrefix(editText, text)) {
                deleteLinePrefix(editText, text);
            } else {
                insertLinePrefix(editText, text);
            }
        }
    }

    private void insertLinePrefix(EditText editText, CharSequence text) {
        int caretPosition = editText.getSelectionEnd();
        int lineStart = getLineStart(caretPosition, (editText.getText() + "\n").toCharArray());
        if (editText instanceof MarkdownAutoRenderEditText){
            if (PreferenceUtil.getBoolean(MyApplication.NOTE_SHOULD_EDIT_MARKDOWN_JUST_IN_TIME)){
                DefaultMarkdownDecorator markdownDecorator = (DefaultMarkdownDecorator)((MarkdownAutoRenderEditText) editText).getMarkdownDecorator();
                boolean decorated = markdownDecorator.isDecorated(editText.getText(), lineStart, getLineEnd(caretPosition, (editText.getText() + "\n").toCharArray()));
                if (decorated){
                    return;
                }
            }
        }
        editText.getText().insert(lineStart, text + " ");
        editText.setSelection(caretPosition + (text.length() == 0 ? -1 : text.length()) + 1);
    }

    private void deleteLinePrefix(EditText editText, CharSequence text) {
        int caretPosition = editText.getSelectionEnd();
        int lineStart = getLineStart(caretPosition, (editText.getText() + "\n").toCharArray());
        editText.getText().delete(lineStart, lineStart+text.length()+1);
        editText.setSelection(caretPosition - (text.length() == 0 ? -1 : text.length()) - 1);
    }

    private boolean hasPrefix(EditText editText, CharSequence text) {
        int caretPosition = editText.getSelectionEnd();
        int lineStart = getLineStart(caretPosition, (editText.getText() + "\n").toCharArray());
        char[] chars = new char[caretPosition - lineStart];
        editText.getText().getChars(lineStart, caretPosition, chars, 0);
        String s = charsToString(chars);
        System.out.println(s);
        System.out.println(text+" ");
        return (s.startsWith(text + " "));
    }

    private String charsToString(char[] chars){
        StringBuilder stringBuilder = new StringBuilder();
        for (char aChar : chars) {
            stringBuilder.append(aChar);
        }
        return stringBuilder.toString();
    }

    private int getLineStart(int caretPosition, char[] chars) {
        int start = caretPosition;
        while (start > 0 && chars[start - 1] != '\n') {
            start--;
        }
        return start;
    }
    private int getLineEnd(int caretPosition, char[] chars) {
        int end = caretPosition;
        while (end < chars.length && chars[end] != '\n') {
            end++;
        }
        return end;
    }

}
