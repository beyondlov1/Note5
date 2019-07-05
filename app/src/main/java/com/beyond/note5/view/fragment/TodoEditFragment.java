package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.view.View;

import com.beyond.note5.R;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.HideKeyBoardEvent2;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.presenter.CalendarPresenterImpl;
import com.beyond.note5.presenter.PredictPresenterImpl;
import com.beyond.note5.presenter.TodoCompositePresenter;
import com.beyond.note5.presenter.TodoCompositePresenterImpl;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.adapter.view.CalendarViewAdapter;
import com.beyond.note5.view.adapter.view.PredictViewAdapter;
import com.beyond.note5.view.adapter.view.TodoViewAdapter;
import com.beyond.note5.view.custom.SelectionListenableEditText;
import com.beyond.note5.view.listener.OnTagClickToAppendListener;
import com.beyond.note5.view.listener.TimeExpressionDetectiveTextWatcher;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public class TodoEditFragment extends AbstractTodoEditorFragment {

    TodoCompositePresenter todoCompositePresenter;

    @Override
    protected void init(Bundle savedInstanceState) {

        todoCompositePresenter = new TodoCompositePresenterImpl.Builder(new TodoPresenterImpl(new MyTodoView()))
                .calendarPresenter(new CalendarPresenterImpl(getActivity(), new MyCalendarView()))
                .predictPresenter(new PredictPresenterImpl(new MyPredictView()))
                .build();
    }

    @Override
    protected Todo creatingDocument() {
        return new Todo();
    }

    @Override
    protected void initCommonEvent() {
        TimeExpressionDetectiveTextWatcher.Builder builder = new TimeExpressionDetectiveTextWatcher.Builder(editorContent);
        editorContent.addTextChangedListener(builder.handler(handler).build());
        editorContent.setOnSelectionChanged(new SelectionListenableEditText.OnSelectionChangeListener() {
            @Override
            public void onChanged(String content, int selStart, int selEnd) {
                if (content.length() >= selStart) {
                    todoCompositePresenter.predict(content.substring(0, selStart));
                }
            }
        });
        // 刚打开时预测
        editorContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (editorContent== null ||editorContent.getText() == null){
                    todoCompositePresenter.predict(null);
                    return;
                }
                todoCompositePresenter.predict(editorContent.getText().toString());
            }
        });
        flowLayout.setOnTagClickListener(new OnTagClickToAppendListener(editorContent));
    }

    @Override
    protected void initDialogView() {
        super.initDialogView();
        editorToolViewStub.setVisibility(View.GONE);
    }

    @Override
    protected void initFragmentView() {
        super.initFragmentView();
        editorToolViewStub.inflate();
        clearButton = root.findViewById(R.id.fragment_edit_todo_clear);
        View convertButton = root.findViewById(R.id.fragment_edit_todo_to_note);
        convertButton.setVisibility(View.GONE);
        View browserSearchButton = root.findViewById(R.id.fragment_edit_todo_browser_search);
        browserSearchButton.setVisibility(View.GONE);
        saveButton = root.findViewById(R.id.fragment_edit_todo_save);
        InputMethodUtil.showKeyboard(editorContent);
    }

    @Override
    protected void initFragmentEvent() {
        super.initFragmentEvent();
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorContent.setText(null);
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creatingDocument.setContent(editorContent.getText().toString());
                onOKClick();
                InputMethodUtil.hideKeyboard(editorContent);
            }
        });
    }

    @Override
    protected void onOKClick() {
        String content = creatingDocument.getContent();
        if (StringUtils.isBlank(content)) {
            return;
        }
        Todo todo = Todo.create(content);
        todoCompositePresenter.add(todo);
    }

    @Override
    protected int getDialogLayoutResId() {
        return R.layout.fragment_todo_edit;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_todo_edit;
    }

    @Override
    protected void onDialogHideKeyboard(HideKeyBoardEvent2 event) {
        super.onDialogHideKeyboard(event);
        if (Document.TODO.equals(event.getType())) {
            dismiss();
        }
    }

    private class MyTodoView extends TodoViewAdapter {

        @Override
        public void onAddSuccess(Todo document) {
            super.onAddSuccess(document);
            ToastUtil.toast(getContext(), "添加成功");
        }

        @Override
        public void onAddFail(Todo document) {
            ToastUtil.toast(getContext(), "添加失败");
        }

        @Override
        public void onDeleteFail(Todo document) {
            ToastUtil.toast(getContext(), "刪除失敗");
        }

    }

    private class MyPredictView extends PredictViewAdapter {
        @Override
        public void onPredictSuccess(final List<Tag> data, final String source) {
            if (data == null || data.isEmpty()) {
                todoCompositePresenter.predict(null);
                return;
            }
            List<Tag> finalTags = data;
            tagData.clear();
            if (StringUtils.isBlank(source)) {
                Iterator<Tag> iterator = finalTags.iterator();
                while (iterator.hasNext()) {
                    Tag tag = iterator.next();
                    if (!tag.isFirst()) {
                        iterator.remove();
                    }
                }
            }
            Collections.sort(finalTags, new Comparator<Tag>() {
                @Override
                public int compare(Tag o1, Tag o2) {
                    return -o1.getScore() + o2.getScore();
                }
            });
            if (finalTags.size() >= 5) {
                finalTags = finalTags.subList(0, 5);
            }
            for (Tag tag : finalTags) {
                tagData.add(tag.getContent());
            }
            tagAdapter.notifyDataChanged();
        }

        @Override
        public void onPredictFail() {
            ToastUtil.toast(getContext(), "预测失败");
        }

        @Override
        public void onTrainFail() {
            ToastUtil.toast(getContext(), "网络状况不佳");
        }
    }

    private class MyCalendarView extends CalendarViewAdapter {
        @Override
        public void onEventAddFail(Todo todo) {
            ToastUtil.toast(getContext(), "添加到日历事件失败");
        }

        @Override
        public void onEventFindAllSuccess(List<Todo> allTodo) {
            ToastUtil.toast(getContext(), "成功查询日历事件");
        }
    }
}
