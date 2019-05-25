package com.beyond.note5.view.adapter;

import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.beyond.note5.event.Event;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.DocumentView;

import org.greenrobot.eventbus.EventBus;

/**
 * @author: beyond
 * @date: 2019/2/6
 */

public abstract class AbstractDocumentViewFragment<T> extends Fragment implements DocumentView<T> {

    @Override
    public void onAddFail(T note) {
        msg("添加失败");
    }

    @Override
    public void onFindAllFail() {
        msg("查找失败");
    }


    @Override
    public void onDeleteFail(T note) {
        msg("删除失败");
    }

    @Override
    public void onUpdateFail(T note) {
        msg("更新失败");
    }

    @Override
    public void onUpdatePriorityFail(T document) {
        msg("更新失败");
    }

    protected void msg(String msg) {
        ToastUtil.toast(this.getActivity(), msg, Toast.LENGTH_SHORT);
    }

    public void post(Event event) {
        EventBus.getDefault().post(event);
    }

}
