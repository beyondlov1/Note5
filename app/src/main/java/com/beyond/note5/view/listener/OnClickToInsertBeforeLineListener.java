package com.beyond.note5.view.listener;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
            insertBeforeLineToEditText(editText, text);
        }
    }

    private void insertBeforeLineToEditText(EditText editText, CharSequence text) {
        int caretPosition = editText.getSelectionEnd();
        int lineStart = getLineStart(caretPosition, (editText.getText() + "\n").toCharArray());
        String newText = editText.getText().insert(lineStart, text + " ").toString();
        editText.setText(newText);
        editText.setSelection(caretPosition+text.length()+1);
    }

    private int getLineStart(int caretPosition, char[] chars) {
        int start = caretPosition;
        while (start > 0 && chars[start-1] != '\n') {
            start--;
        }
        return start;
    }

}
