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
        String newText = editText.getText().insert(lineStart, text + " ").toString();
        editText.setText(newText);
        editText.setSelection(caretPosition + (text.length() == 0 ? -1 : text.length()) + 1);
    }

    private void deleteLinePrefix(EditText editText, CharSequence text) {
        int caretPosition = editText.getSelectionEnd();
        int lineStart = getLineStart(caretPosition, (editText.getText() + "\n").toCharArray());
        String newText = editText.getText().delete(lineStart, lineStart+text.length()+1).toString();
        editText.setText(newText);
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

}
