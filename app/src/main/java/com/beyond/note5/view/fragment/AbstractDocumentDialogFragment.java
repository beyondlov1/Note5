package com.beyond.note5.view.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.note5.bean.Document;
import com.beyond.note5.event.HideKeyBoardEvent2;
import com.beyond.note5.event.ShowKeyBoardEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class AbstractDocumentDialogFragment<T extends Document> extends DialogFragment {
    protected boolean dialog;

    protected View root;

    Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(savedInstanceState);
    }

    protected abstract void init(Bundle savedInstanceState);

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = true;
        root = LayoutInflater.from(getActivity()).inflate(getDialogLayoutResId(), null);
        return this.createDialogInternal(savedInstanceState);
    }

    protected abstract Dialog createDialogInternal(Bundle savedInstanceState);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        if (isDialog()) {
            unbinder = ButterKnife.bind(this, root);
            initCommonView();
            initCommonEvent();
            initDialogView();
            initDialogEvent();
        } else {
            root = inflater.inflate(getFragmentLayoutResId(), null);
            unbinder = ButterKnife.bind(this, root);
            initCommonView();
            initCommonEvent();
            initFragmentView();
            initFragmentEvent();
        }
        return root;
    }

    protected abstract void initCommonView();

    protected abstract void initCommonEvent();

    protected void initDialogView() {
    }

    protected void initFragmentView() {
    }

    protected void initDialogEvent() {
    }

    protected void initFragmentEvent() {
    }

    protected abstract @LayoutRes
    int getDialogLayoutResId();

    protected abstract @LayoutRes
    int getFragmentLayoutResId();

    protected boolean isDialog() {
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isDialog()) {
            onDialogStartInternal();
        } else {
            onFragmentStartInternal();
        }
    }

    protected void onDialogStartInternal() {

    }

    protected void onFragmentStartInternal() {

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
        unbinder.unbind();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ShowKeyBoardEvent event) {
        if (isDialog()) {
            onDialogShowKeyboard(event);
        } else {
            onFragmentShowKeyboard(event);
        }
    }

    protected void onDialogShowKeyboard(ShowKeyBoardEvent event) {

    }

    protected void onFragmentShowKeyboard(ShowKeyBoardEvent event) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(HideKeyBoardEvent2 event) {
        if (isDialog()) {
            onDialogHideKeyboard(event);
        } else {
            onFragmentHideKeyboard(event);
        }
    }

    protected void onDialogHideKeyboard(HideKeyBoardEvent2 event) {

    }

    protected void onFragmentHideKeyboard(HideKeyBoardEvent2 event) {

    }
}
