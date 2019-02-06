package com.beyond.note5.view.adapter;

import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.beyond.note5.view.DocumentView;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/6
 */

public abstract class AbstractFragmentDocumentView<T> extends Fragment implements DocumentView<T> {

    @Override
    public void onAddSuccess(T note) {
        msg("添加成功");
    }

    @Override
    public void onAddFail(T note) {
        msg("添加失败");
    }

    @Override
    public void onFindAllSuccess(List<T> allT) {

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
    public void onDeleteSuccess(T note) {

    }

    @Override
    public void onUpdateSuccess(T note) {

    }

    @Override
    public void onUpdateFail(T note) {

    }

    protected void msg(String msg) {
        Toast.makeText(this.getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}
