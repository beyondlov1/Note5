package com.beyond.note5.view.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Note;
import com.beyond.note5.bean.Reminder;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.FillTodoModifyEvent;
import com.beyond.note5.event.HideKeyBoardEvent2;
import com.beyond.note5.event.HideTodoEditorEvent;
import com.beyond.note5.event.ScrollToTodoByDateEvent;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.presenter.CalendarPresenterImpl;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.presenter.PredictPresenterImpl;
import com.beyond.note5.presenter.TodoCompositePresenter;
import com.beyond.note5.presenter.TodoCompositePresenterImpl;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.utils.HighlightUtil;
import com.beyond.note5.utils.HtmlUtil;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.utils.StatusBarUtil;
import com.beyond.note5.utils.TimeNLPUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.utils.ViewUtil;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.adapter.view.CalendarViewAdapter;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;
import com.beyond.note5.view.adapter.view.PredictViewAdapter;
import com.beyond.note5.view.adapter.view.TodoViewAdapter;
import com.beyond.note5.view.animator.SmoothScalable;
import com.beyond.note5.view.animator.SmoothScaleAnimation;
import com.beyond.note5.view.custom.SelectionListenableEditText;
import com.beyond.note5.view.listener.OnBackPressListener;
import com.beyond.note5.view.listener.OnKeyboardChangeListener;
import com.beyond.note5.view.listener.OnTagClickToAppendListener;
import com.beyond.note5.view.listener.TimeExpressionDetectiveTextWatcher;
import com.time.nlp.TimeUnit;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TodoModifySuperFragment extends AbstractTodoEditorFragment implements OnBackPressListener, SmoothScalable,FragmentContainerAware{

    protected int currentIndex;

    protected View fragmentContainer;

    protected OnKeyboardChangeListener onKeyboardChangeListener;

    private TodoCompositePresenter todoCompositePresenter;

    private NotePresenter notePresenter;

    MyNoteView noteView = new MyNoteView();

    MyTodoView todoView = new MyTodoView();

    MyCalendarView calendarView = new MyCalendarView();

    MyPredictView predictView = new MyPredictView();

    @Override
    protected void init(Bundle savedInstanceState) {
        todoCompositePresenter = new TodoCompositePresenterImpl.Builder(new TodoPresenterImpl(todoView))
                .calendarPresenter(new CalendarPresenterImpl(getActivity(), calendarView))
                .predictPresenter(new PredictPresenterImpl(predictView))
                .build();
        notePresenter = new NotePresenterImpl(noteView);
    }

    @Override
    protected Todo creatingDocument() {
        // do nothing
        return null;
    }

    @Override
    protected void initCommonEvent() {

    }

    @Override
    protected int getDialogLayoutResId() {
        return R.layout.fragment_todo_edit;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_todo_edit;
    }

    //回显
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(FillTodoModifyEvent fillTodoModifyEvent) {
        final Todo todo = fillTodoModifyEvent.get();
        currentIndex = fillTodoModifyEvent.getIndex();
        creatingDocument = ObjectUtils.clone(todo);
        creatingDocument.__setDaoSession(MyApplication.getInstance().getDaoSession());
        editorContent.setText(creatingDocument.getContent());
        editorContent.setSelection(creatingDocument.getContent().length());
        highlightTimeExpressionAsync();
    }

    private void highlightTimeExpressionAsync() {
        MyApplication.getInstance().getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                String beforeContent = creatingDocument.getContent();
                final String html = HighlightUtil.highlightTimeExpression(creatingDocument.getContent());
                String afterContent = creatingDocument.getContent();
                if (!StringUtils.equals(beforeContent, afterContent)) { //加个乐观锁， 不知道对不对
                    run();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (StringUtils.isNotBlank(html)) {
                            editorContent.setText(HtmlUtil.fromHtml(html));
                        } else {
                            editorContent.setText(creatingDocument.getContent());
                        }
                        editorContent.setSelection(creatingDocument.getContent().length());
                    }
                });

            }
        });
    }

    @Override
    protected void initFragmentView() {
        super.initFragmentView();
        root.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.white));

        editorToolViewStub.inflate();
        clearButton = root.findViewById(R.id.fragment_edit_todo_clear);
        convertButton = root.findViewById(R.id.fragment_edit_todo_to_note);
        browserSearchButton = root.findViewById(R.id.fragment_edit_todo_browser_search);
        saveButton = root.findViewById(R.id.fragment_edit_todo_save);

        flowLayout.setAdapter(tagAdapter);
    }

    @Override
    protected void initFragmentEvent() {
        super.initFragmentEvent();
        //防止事件向下传递
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        editorContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                creatingDocument.setContent(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorContent.setText(null);
            }
        });
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note note = new Note();
                note.setId(creatingDocument.getId());
                note.setContent(creatingDocument.getContent());
                note.setCreateTime(creatingDocument.getCreateTime());
                note.setLastModifyTime(new Date());
                note.setVersion(creatingDocument.getVersion());
                notePresenter.add(note);
                todoCompositePresenter.deleteLogic(creatingDocument);
                EventBus.getDefault().post(new HideTodoEditorEvent(currentIndex));
                InputMethodUtil.hideKeyboard(editorContent);
                ToastUtil.toast(TodoModifySuperFragment.this.getContext(), "已转化为NOTE", Toast.LENGTH_SHORT);
            }
        });

        browserSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = WebViewUtil.getUrlOrSearchUrl(creatingDocument);
                if (url != null) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    TodoModifySuperFragment.this.startActivity(intent);
                } else {
                    ToastUtil.toast(TodoModifySuperFragment.this.getContext(), "搜索文字不能超过32个字", Toast.LENGTH_SHORT);
                }

                onKeyboardChangeListener.setExecuteHideCallback(false);
                fragmentContainer.getLayoutParams().height = ViewUtil.getScreenSize().y;
                fragmentContainer.setLayoutParams(fragmentContainer.getLayoutParams());
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editorContent.getText().toString();
                TodoModifySuperFragment.this.save(content);
            }
        });

        if (editorContent instanceof SelectionListenableEditText) {
            ((SelectionListenableEditText) editorContent).setOnSelectionChanged(new SelectionListenableEditText.OnSelectionChangeListener() {
                @Override
                public void onChanged(String content, int selStart, int selEnd) {
                    if (content.length() >= selStart) {
                        todoCompositePresenter.predict(content.substring(0, selStart));
                        Log.d("todoModifySuperFragment", "todoCompositePresenter" + todoCompositePresenter);
                    }
                }
            });
        }

        TimeExpressionDetectiveTextWatcher.Builder builder = new TimeExpressionDetectiveTextWatcher.Builder(editorContent);
        TimeExpressionDetectiveTextWatcher.OnTimeExpressionChangedHandler onTimeExpressionChangedHandler =
                new TimeExpressionDetectiveTextWatcher.OnTimeExpressionChangedHandler() {
                    @Override
                    public void handle(TimeUnit timeUnit) {
                        Date changedStart = timeUnit.getTime();
                        Reminder reminder = creatingDocument.getReminder();
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
        editorContent.addTextChangedListener(
                builder
                        .handler(handler)
//                        .timeExpressionChangedHandler(onTimeExpressionChangedHandler)
                        .build()
        );

        flowLayout.setOnTagClickListener(new OnTagClickToAppendListener(editorContent));
    }

    @Override
    public void saveInternal(@NotNull CharSequence cs) {
        HideTodoEditorEvent hideTodoEditorEvent = new HideTodoEditorEvent(currentIndex);
        EventBus.getDefault().post(hideTodoEditorEvent);
        InputMethodUtil.hideKeyboard(editorContent, onKeyboardChangeListener, false);

        String content = cs.toString();
        if (StringUtils.isBlank(content)) {
            todoCompositePresenter.deleteLogic(creatingDocument);
        } else {
            creatingDocument.setTitle(content.length() > 10 ? content.substring(0, 10) : content);
            creatingDocument.setContent(content);
            creatingDocument.setLastModifyTime(new Date());
            creatingDocument.setVersion(creatingDocument.getVersion() == null ? 0 : creatingDocument.getVersion() + 1);
            processReminder(content);
        }
    }

    private void processReminder(String content) {
        Reminder reminder = creatingDocument.getReminder();
        Date reminderStart = TimeNLPUtil.parse(content);
        if (reminderStart != null) {
            if (reminder != null) {
                reminder.setStart(reminderStart);
            } else {
                reminder = new Reminder();
                reminder.setId(IDUtil.uuid());
                reminder.setStart(reminderStart);
            }
            creatingDocument.setReminder(reminder);
        } else {
            todoCompositePresenter.deleteReminder(creatingDocument);
        }
        todoCompositePresenter.update(creatingDocument);
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void initOnKeyboardChangeListener(OnKeyboardChangeListener onKeyboardChangeListener) {
        this.onKeyboardChangeListener = onKeyboardChangeListener;
    }

    @Override
    protected void onFragmentShowKeyboard(ShowKeyBoardEvent event) {
        super.onFragmentShowKeyboard(event);
        String type = event.getType();
        if (StringUtils.equals(Document.TODO, type) && fragmentContainer != null) {
            int y = event.get();
            fragmentContainer.getLayoutParams().height = retrieveAndUpdateHeightWithSoftInputMethodIfNecessary(y);
            fragmentContainer.setLayoutParams(fragmentContainer.getLayoutParams());
        }
    }

    private int retrieveAndUpdateHeightWithSoftInputMethodIfNecessary(int y) {
        //设置初始的dialogHeightWithSoftInputMethod, 为了不让开始的时候动画跳一下
        int dialogHeightWithSoftInputMethod = InputMethodUtil.getDialogHeightWithSoftInputMethod();
        if (dialogHeightWithSoftInputMethod == 0) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            dialogHeightWithSoftInputMethod = dm.heightPixels - y - 50;
            InputMethodUtil.rememberDialogHeightWithSoftInputMethod(dm.heightPixels - y - 50);
        }
        return dialogHeightWithSoftInputMethod;
    }

    @Override
    protected void onFragmentHideKeyboard(HideKeyBoardEvent2 event) {
        super.onFragmentHideKeyboard(event);
        final String type = event.getType();
        event.get().setHideCallback(new Runnable() {
            @Override
            public void run() {
                if (StringUtils.equals(Document.TODO, type)) {
                    EventBus.getDefault().post(new HideTodoEditorEvent(currentIndex));
                }
            }
        });

    }


    @Override
    public boolean onBackPressed() {
        InputMethodUtil.hideKeyboard(editorContent, onKeyboardChangeListener, true);
        EventBus.getDefault().post(new HideTodoEditorEvent(currentIndex));
        return true;
    }

    public void registerHooks(SmoothScaleAnimation smoothScaleAnimation) {
        smoothScaleAnimation.setAfterShowHook(new Runnable() {
            @Override
            public void run() {
                StatusBarUtil.showWhiteStatusBar(getActivity());
            }
        });
        smoothScaleAnimation.setAfterHideHook(new Runnable() {
            @Override
            public void run() {
                fragmentContainer.setVisibility(View.GONE);
            }
        });
        smoothScaleAnimation.setBeforeShowHook(new Runnable() {
            @Override
            public void run() {
                InputMethodUtil.showKeyboard(editorContent);
            }
        });
        smoothScaleAnimation.setBeforeHideHook(new Runnable() {
            @Override
            public void run() {
                StatusBarUtil.showStableLightStatusBar(getActivity());
            }
        });
    }

    @Override
    public void setFragmentContainer(View fragmentContainer) {
        this.fragmentContainer = fragmentContainer;
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
