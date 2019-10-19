package com.beyond.note5.view.component;

import org.greenrobot.greendao.annotation.NotNull;

public interface EditorListener {

    void beforeEditing(CharSequence cs);

    void afterEditing(CharSequence cs);

    void beforeClear(CharSequence oldValue);

    void afterClear(CharSequence oldValue);

    void beforeSave(@NotNull CharSequence cs);

    void afterSave(@NotNull CharSequence cs);
}
