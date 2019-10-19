package com.beyond.note5.view.component;

public interface Editor {
    void save(CharSequence content);
    void editing(CharSequence content);
    void clear(CharSequence oldContent);
    void setEditorListener(EditorListener listener);
}
