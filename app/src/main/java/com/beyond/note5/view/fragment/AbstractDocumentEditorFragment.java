package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.beyond.note5.bean.Document;
import com.beyond.note5.view.component.Editor;
import com.beyond.note5.view.component.EditorListener;

/**
 * 文档编辑抽象类
 * 主要功能：
 * 1. 区分是否为对话框
 * 2. 利用butterKnife注入view
 * 3. 监听输入法显示隐藏事件
 * 4. 生命周期监听
 *
 * @param <T>
 */
public abstract class AbstractDocumentEditorFragment<T extends Document> extends AbstractDocumentDialogFragment<T> implements Editor {

    protected T creatingDocument;

    private EditorListener editorListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        creatingDocument = creatingDocument();
    }

    protected abstract T creatingDocument();

    public final void save(CharSequence content){
        if (editorListener!= null){
            editorListener.beforeSave(content);
        }
        saveInternal(content);
        if (editorListener!= null){
            editorListener.afterSave(content);
        }
    }

    protected void saveInternal(CharSequence content){

    }

    @Override
    public final void editing(CharSequence content) {
        if (editorListener!= null){
            editorListener.beforeEditing(content);
        }
        editingInternal(content);
        if (editorListener!= null){
            editorListener.afterEditing(content);
        }
    }

    protected void editingInternal(CharSequence content){

    }

    @Override
    public final void clear(CharSequence oldContent) {
        if (editorListener!= null){
            editorListener.beforeClear(oldContent);
        }
        clearInternal(oldContent);
        if (editorListener!= null){
            editorListener.afterClear(oldContent);
        }
    }

    protected void clearInternal(CharSequence content){

    }

    public EditorListener getEditorListener() {
        return editorListener;
    }

    public void setEditorListener(EditorListener editorListener) {
        this.editorListener = editorListener;
    }
}
