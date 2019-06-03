package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.beyond.note5.bean.Document;

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
public abstract class AbstractDocumentEditorFragment<T extends Document> extends AbstractDocumentDialogFragment<T> {

    protected T creatingDocument;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        creatingDocument = creatingDocument();
        init(savedInstanceState);
    }

    protected abstract T creatingDocument();
}
