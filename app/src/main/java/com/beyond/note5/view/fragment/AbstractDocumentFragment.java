package com.beyond.note5.view.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;

import com.beyond.note5.view.adapter.AbstractFragmentDocumentView;

import org.greenrobot.eventbus.EventBus;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public abstract class AbstractDocumentFragment<T> extends AbstractFragmentDocumentView<T> {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
