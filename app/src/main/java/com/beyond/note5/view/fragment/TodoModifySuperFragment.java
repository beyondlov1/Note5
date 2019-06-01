package com.beyond.note5.view.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.bean.Reminder;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.FillTodoModifyEvent;
import com.beyond.note5.event.HideTodoEditorEvent;
import com.beyond.note5.event.ScrollToTodoByDateEvent;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.presenter.CalendarPresenterImpl;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.presenter.PredictPresenterImpl;
import com.beyond.note5.presenter.TodoCompositePresenter;
import com.beyond.note5.presenter.TodoCompositePresenterImpl;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.utils.HighlightUtil;
import com.beyond.note5.utils.HtmlUtil;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.utils.TimeNLPUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.utils.ViewUtil;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.adapter.view.CalendarViewAdapter;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;
import com.beyond.note5.view.adapter.view.PredictViewAdapter;
import com.beyond.note5.view.adapter.view.TodoViewAdapter;
import com.beyond.note5.view.custom.SelectionListenableEditText;
import com.beyond.note5.view.listener.OnTagClickToAppendListener;
import com.beyond.note5.view.listener.TimeExpressionDetectiveTextWatcher;
import com.time.nlp.TimeUnit;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TodoModifySuperFragment extends TodoEditSuperFragment {

    private ImageButton clearButton;
    private ImageButton convertButton;
    private ImageButton browserSearchButton;
    private ImageButton saveButton;
    private TagFlowLayout flowLayout;
    private TagAdapter<String> tagAdapter;

    private List<String> tagData = new ArrayList<>();

    private TodoCompositePresenter todoCompositePresenter;
    private NotePresenterImpl notePresenter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initInjection();
    }

    private void initInjection() {

        todoCompositePresenter = new TodoCompositePresenterImpl.Builder(new TodoPresenterImpl(new MyTodoView()))
                .calendarPresenter(new CalendarPresenterImpl(getActivity(), new MyCalendarView()))
                .predictPresenter(new PredictPresenterImpl(new MyPredictView()))
                .build();
        notePresenter = new NotePresenterImpl( new MyNoteView());
    }

    //回显
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(FillTodoModifyEvent fillTodoModifyEvent) {
        final Todo todo = fillTodoModifyEvent.get();
        currentIndex = fillTodoModifyEvent.getIndex();
        createdDocument = ObjectUtils.clone(todo);
        contentEditText.setText(createdDocument.getContent());
        contentEditText.setSelection(createdDocument.getContent().length());
        highlightTimeExpressionAsync();
    }

    private void highlightTimeExpressionAsync() {
        MyApplication.getInstance().getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                String beforeContent = createdDocument.getContent();
                final String html = HighlightUtil.highlightTimeExpression(createdDocument.getContent());
                String afterContent = createdDocument.getContent();
                if (!StringUtils.equals(beforeContent, afterContent)) { //加个乐观锁， 不知道对不对
                    run();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (StringUtils.isNotBlank(html)) {
                            contentEditText.setText(HtmlUtil.fromHtml(html));
                        } else {
                            contentEditText.setText(createdDocument.getContent());
                        }
                        contentEditText.setSelection(createdDocument.getContent().length());
                    }
                });

            }
        });
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        ViewStub toolsContainer = view.findViewById(R.id.fragment_edit_todo_view_stub);
        toolsContainer.inflate();
        clearButton = view.findViewById(R.id.fragment_edit_todo_clear);
        convertButton = view.findViewById(R.id.fragment_edit_todo_to_note);
        browserSearchButton = view.findViewById(R.id.fragment_edit_todo_browser_search);
        saveButton = view.findViewById(R.id.fragment_edit_todo_save);

        ViewStub tagsContainer = view.findViewById(R.id.fragment_edit_todo_tag_view_stub);
        tagsContainer.inflate();
        flowLayout = view.findViewById(R.id.fragment_edit_todo_tags);
        tagAdapter = new TagAdapter<String>(tagData) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                TextView tv = new TextView(getActivity());
                tv.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                tv.setBackground(getResources().getDrawable(R.drawable.radius_24dp_blue, null));
                tv.setText(s);
                return tv;
            }
        };
        flowLayout.setAdapter(tagAdapter);
    }

    @Override
    protected void initEvent(View view) {
        super.initEvent(view);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditText.setText(null);
            }
        });
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note note = new Note();
                note.setId(createdDocument.getId());
                note.setContent(createdDocument.getContent());
                note.setCreateTime(createdDocument.getCreateTime());
                note.setLastModifyTime(new Date());
                note.setVersion(createdDocument.getVersion());
                notePresenter.add(note);
                todoCompositePresenter.delete(createdDocument);
                EventBus.getDefault().post(new HideTodoEditorEvent(currentIndex));
                InputMethodUtil.hideKeyboard(contentEditText);
                ToastUtil.toast(getContext(), "已转化为NOTE", Toast.LENGTH_SHORT);
            }
        });

        browserSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = WebViewUtil.getUrlOrSearchUrl(createdDocument);
                if (url != null) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } else {
                    ToastUtil.toast(getContext(), "搜索文字不能超过32个字", Toast.LENGTH_SHORT);
                }

                onKeyboardChangeListener.setExecuteHideCallback(false);
                fragmentContainer.getLayoutParams().height = ViewUtil.getScreenSizeWithoutNotification().y;
                fragmentContainer.setLayoutParams(fragmentContainer.getLayoutParams());
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideTodoEditorEvent hideTodoEditorEvent = new HideTodoEditorEvent(currentIndex);
                EventBus.getDefault().post(hideTodoEditorEvent);
                InputMethodUtil.hideKeyboard(contentEditText, onKeyboardChangeListener, false);

                String content = contentEditText.getText().toString();
                if (StringUtils.isBlank(content)) {
                    todoCompositePresenter.delete(createdDocument);
                } else {
                    createdDocument.setTitle(content.length() > 10 ? content.substring(0, 10) : content);
                    createdDocument.setContent(content);
                    createdDocument.setLastModifyTime(new Date());
                    createdDocument.setVersion(createdDocument.getVersion() == null ? 0 : createdDocument.getVersion() + 1);
                    processReminder(content);
                }


            }

            private void processReminder(String content) {
                Reminder reminder = createdDocument.getReminder();
                Date reminderStart = TimeNLPUtil.parse(content);
                if (reminderStart != null) {
                    if (reminder != null) {
                        reminder.setStart(reminderStart);
                    } else {
                        reminder = new Reminder();
                        reminder.setId(IDUtil.uuid());
                        reminder.setStart(reminderStart);
                    }
                    createdDocument.setReminder(reminder);
                } else {
                    todoCompositePresenter.deleteReminder(createdDocument);
                }
                todoCompositePresenter.update(createdDocument);
            }
        });

        if (contentEditText instanceof SelectionListenableEditText) {
            ((SelectionListenableEditText) contentEditText).setOnSelectionChanged(new SelectionListenableEditText.OnSelectionChangeListener() {

                @Override
                public void onChanged(String content, int selStart, int selEnd) {
                    if (content.length() >= selStart) {
                        todoCompositePresenter.predict(content.substring(0, selStart));
                        Log.d("todoModifySuperFragment", "todoCompositePresenter" + todoCompositePresenter);
                    }
                }
            });
        }

        TimeExpressionDetectiveTextWatcher.Builder builder = new TimeExpressionDetectiveTextWatcher.Builder(contentEditText);
        TimeExpressionDetectiveTextWatcher.OnTimeExpressionChangedHandler onTimeExpressionChangedHandler =
                new TimeExpressionDetectiveTextWatcher.OnTimeExpressionChangedHandler() {
                    @Override
                    public void handle(TimeUnit timeUnit) {
                        Date changedStart = timeUnit.getTime();
                        Reminder reminder = createdDocument.getReminder();
                        if (reminder == null) {
                            return;
                        }
                        Date start = reminder.getStart();
                        if (!DateUtils.isSameDay(start, changedStart)) {
                            ScrollToTodoByDateEvent event = new ScrollToTodoByDateEvent(changedStart);
                            EventBus.getDefault().post(event);
                        }
                    }
                };
        contentEditText.addTextChangedListener(
                builder
                        .handler(handler)
//                        .timeExpressionChangedHandler(onTimeExpressionChangedHandler)
                        .build()
        );

        flowLayout.setOnTagClickListener(new OnTagClickToAppendListener(contentEditText));
    }


    private class MyTodoView extends TodoViewAdapter {

        @Override
        public void onUpdateFail(Todo document) {
            ToastUtil.toast(getContext(), "更新失敗");
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

    private class MyNoteView extends NoteViewAdapter{
        @Override
        public void onAddFail(Note document) {
            ToastUtil.toast(getActivity(),"添加失敗");
        }
    }
}
