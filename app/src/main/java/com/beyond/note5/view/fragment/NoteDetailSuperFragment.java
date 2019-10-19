package com.beyond.note5.view.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.constant.LoadType;
import com.beyond.note5.event.FillNoteDetailEvent;
import com.beyond.note5.event.FillNoteModifyEvent;
import com.beyond.note5.event.HideNoteDetailEvent;
import com.beyond.note5.event.ScrollToNoteEvent;
import com.beyond.note5.event.note.UpdateNoteSuccessEvent;
import com.beyond.note5.presenter.CalendarPresenterImpl;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.presenter.PredictPresenterImpl;
import com.beyond.note5.presenter.TodoCompositePresenter;
import com.beyond.note5.presenter.TodoCompositePresenterImpl;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.utils.StatusBarUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.utils.ViewUtil;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.NoteModifyEditorActivity;
import com.beyond.note5.view.adapter.view.CalendarViewAdapter;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;
import com.beyond.note5.view.adapter.view.PredictViewAdapter;
import com.beyond.note5.view.adapter.view.TodoViewAdapter;
import com.beyond.note5.view.animator.SmoothScalable;
import com.beyond.note5.view.animator.SmoothScaleAnimation;
import com.beyond.note5.view.listener.OnBackPressListener;
import com.beyond.note5.view.listener.OnSlideListener;

import org.apache.commons.collections4.CollectionUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.List;

public class NoteDetailSuperFragment extends AbstractDocumentDialogFragment implements OnBackPressListener, SmoothScalable, FragmentContainerAware {
    private static final String TAG = NoteDetailSuperFragment.class.getSimpleName();

    protected MultiDetailStage<Note> multiDetailStage;

    protected TextView pageCountTextView;
    protected View operationContainer;
    protected View stageAndPageCountContainer;
    protected View operationItemsContainer;
    protected View deleteButton;
    protected View searchButton;
    protected View browserSearchButton;
    protected View stickButton;
    protected View convertButton;
    protected View doneButton;
    private View fragmentContainer;

    private LoadType loadType;

    private MyCalendarView calendarView = new MyCalendarView();
    private MyPredictView predictView = new MyPredictView();
    private MyTodoView todoView = new MyTodoView();
    private MyNoteView noteView = new MyNoteView();

    private TodoCompositePresenter todoCompositePresenter;
    private NotePresenter notePresenter;

    @Override
    protected void init(Bundle savedInstanceState) {
        todoCompositePresenter = new TodoCompositePresenterImpl.Builder(new TodoPresenterImpl(todoView))
                .calendarPresenter(new CalendarPresenterImpl(getActivity(), calendarView))
                .predictPresenter(new PredictPresenterImpl(predictView))
                .build();
        notePresenter = new NotePresenterImpl(noteView);
    }

    @Override
    protected Dialog createDialogInternal(Bundle savedInstanceState) {
        return null;
    }

    @Override
    protected void initCommonView() {
        multiDetailStage = root.findViewById(R.id.fragment_note_detail_view_switcher);
        multiDetailStage.setViewFactory(new MyViewFactory());

        pageCountTextView = root.findViewById(R.id.fragment_note_detail_page_count);
        operationContainer = root.findViewById(R.id.fragment_note_detail_operation_container);
        stageAndPageCountContainer = root.findViewById(R.id.fragment_note_detail_stage_and_page_count);
        operationItemsContainer = root.findViewById(R.id.fragment_note_detail_operation_items);
        deleteButton = root.findViewById(R.id.fragment_note_detail_operation_delete);
        searchButton = root.findViewById(R.id.fragment_note_detail_operation_search);
        browserSearchButton = root.findViewById(R.id.fragment_note_detail_operation_browser_search);
        stickButton = root.findViewById(R.id.fragment_note_detail_operation_stick);
        convertButton = root.findViewById(R.id.fragment_note_detail_to_todo);
        doneButton = root.findViewById(R.id.fragment_note_detail_operation_done);
        pageCountTextView.getLayoutParams().height = 100;
        pageCountTextView.setLayoutParams(pageCountTextView.getLayoutParams());

    }

    @Override
    protected void initCommonEvent() {
        //防止事件向下传递
        root.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note currentNote = multiDetailStage.getCurrentData();
                notePresenter.deleteDeepLogic(currentNote);
                if (multiDetailStage.getData().isEmpty()) {
                    closeWithAnimation();
                    return;
                }
                if (multiDetailStage.getCurrentIndex() == multiDetailStage.getData().size()) {
                    multiDetailStage.setCurrentIndex(multiDetailStage.getCurrentIndex() - 1);
                }
                refresh();
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                multiDetailStage.load(LoadType.WEB);
            }
        });
        browserSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = WebViewUtil.getUrlOrSearchUrl(multiDetailStage.getCurrentData());
                if (url != null) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } else {
                    ToastUtil.toast(getActivity(), "搜索文字不能超过32个字", Toast.LENGTH_SHORT);
                }
            }
        });
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note note = multiDetailStage.getCurrentData();
                Todo todo = new Todo();
                todo.setId(note.getId());
                note.setTitle(note.getTitle());
                todo.setContent(note.getContent());
                todo.setCreateTime(note.getCreateTime());
                todo.setLastModifyTime(new Date());
                todo.setVersion(note.getVersion());
                todoCompositePresenter.add(todo);
                notePresenter.deleteLogic(note);
                closeWithAnimation();
                ToastUtil.toast(getContext(), "已转化为TODO", Toast.LENGTH_SHORT);
            }

        });
        pageCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeWithAnimation();
            }
        });

    }

    @Override
    protected int getDialogLayoutResId() {
        return R.layout.fragment_note_detail;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_note_detail;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(FillNoteDetailEvent fillNoteDetailEvent) {
        if (fillNoteDetailEvent.isConsumed()) {
            return;
        }
        multiDetailStage.setData(fillNoteDetailEvent.get());
        multiDetailStage.setCurrentIndex(fillNoteDetailEvent.getIndex());
        multiDetailStage.setEnterIndex(multiDetailStage.getCurrentIndex());

        operationContainer.setVisibility(View.GONE);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) stageAndPageCountContainer.getLayoutParams();
        layoutParams.topMargin = 0;
        stageAndPageCountContainer.setLayoutParams(layoutParams);

        loadType = fillNoteDetailEvent.getLoadType();
        refresh();
        fillNoteDetailEvent.setConsumed(true);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UpdateNoteSuccessEvent updateNoteSuccessEvent) {
        noteView.onUpdateSuccess(updateNoteSuccessEvent.get());
    }

    @SuppressWarnings("ConstantConditions")
    private void refresh() {
        multiDetailStage.refresh(loadType);
        processFrameView();
        resetLoadType();
        scrollRecyclerViewTo(multiDetailStage.getCurrentData());
    }

    private void processFrameView() {
        processVariableTools();
        processPageCount();
    }

    /**
     * 处理可变的按钮
     */
    private void processVariableTools() {
        if (CollectionUtils.isEmpty(multiDetailStage.getData())) {
            return;
        }
        // 置顶按钮
        if (multiDetailStage.getCurrentData().getReadFlag() < 0) { // 置顶
            ((ImageButton) stickButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up_blue_400_24dp, null));
        } else { //其他
            ((ImageButton) stickButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up_grey_600_24dp, null));

        }
        stickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note note = multiDetailStage.getCurrentData();
                note.setLastModifyTime(new Date());
                if (multiDetailStage.getCurrentData().getReadFlag() < 0) { // 置顶
                    note.setReadFlag(DocumentConst.READ_FLAG_NORMAL);
                    notePresenter.update(note);
                    ToastUtil.toast(getActivity(), "取消置顶", Toast.LENGTH_SHORT);
                    ((ImageButton) stickButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up_grey_600_24dp, null));
                } else { //其他
                    note.setReadFlag(DocumentConst.READ_FLAG_STICK);
                    notePresenter.update(note);
                    ToastUtil.toast(getActivity(), "置顶成功", Toast.LENGTH_SHORT);
                    ((ImageButton) stickButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up_blue_400_24dp, null));
                }
            }
        });


        // 置顶按钮
        if (multiDetailStage.getCurrentData().getReadFlag() > 0) { // 置顶
            ((ImageButton) doneButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_done_blue_24dp, null));
        } else { //其他
            ((ImageButton) doneButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_done_grey_600_24dp, null));

        }
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note note = multiDetailStage.getCurrentData();
                note.setLastModifyTime(new Date());
                if (multiDetailStage.getCurrentData().getReadFlag() > 0) {

                    Note currentNote = multiDetailStage.getCurrentData();
                    currentNote.setReadFlag(DocumentConst.READ_FLAG_NORMAL);
                    notePresenter.update(note);
                    ToastUtil.toast(getActivity(), "取消已读", Toast.LENGTH_SHORT);
                    ((ImageButton) doneButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_done_grey_600_24dp, null));
                } else { //其他
                    Note currentNote = multiDetailStage.getCurrentData();
                    currentNote.setReadFlag(DocumentConst.READ_FLAG_DONE);
                    int oldIndex = multiDetailStage.getCurrentIndex();
                    notePresenter.update(note);
                    multiDetailStage.setCurrentIndex(oldIndex);
                    refresh();
                    ToastUtil.toast(getActivity(), "已读", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void processPageCount() {
        String pageCount = String.format("%s/%s", multiDetailStage.getCurrentIndex() + 1, multiDetailStage.getData().size());
        pageCountTextView.setText(pageCount);
    }

    private void scrollRecyclerViewTo(Note note) {
        EventBus.getDefault().post(new ScrollToNoteEvent(note));
    }

    private void next() {
        multiDetailStage.next();
        processFrameView();
    }

    private void prev() {
        multiDetailStage.prev();
        processFrameView();
    }

    private void resetLoadType() {
        this.loadType = LoadType.CONTENT;
    }

    protected void showModifyView() {
        //NoteModifyFragment noteModifyFragment = new NoteModifyFragment();
        //noteModifyFragment.show(getActivity().getSupportFragmentManager(), "modifyDialog");
        Intent intent = new Intent(getContext(), NoteModifyEditorActivity.class);
        startActivity(intent);
        EventBus.getDefault().postSticky(new FillNoteModifyEvent(multiDetailStage.getCurrentData()));
    }

    @Override
    public boolean onBackPressed() {
        boolean consumed = multiDetailStage.onBackPressed();
        if (!consumed) {
            closeWithAnimation();
        }
        return true;
    }

    private void closeWithAnimation() {
        if (multiDetailStage.getData().isEmpty()) {
            multiDetailStage.setCurrentIndex(-1);
        }
        HideNoteDetailEvent event = new HideNoteDetailEvent(multiDetailStage.getCurrentIndex());
        event.setFirstIndex(multiDetailStage.getEnterIndex());
        EventBus.getDefault().post(event);
    }

    @Override
    public void registerHooks(SmoothScaleAnimation smoothScaleAnimation) {
        smoothScaleAnimation.setAfterShowHook(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams layoutParams = fragmentContainer.getLayoutParams();
                layoutParams.height = ViewUtil.getScreenSize().y;
                fragmentContainer.setLayoutParams(layoutParams);
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
                if (getActivity() == null) {
                    return;
                }
                StatusBarUtil.hideStatusBar(getActivity());
            }
        });
        smoothScaleAnimation.setBeforeHideHook(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null) {
                    return;
                }
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
        public void onAddFail(Todo document) {
            ToastUtil.toast(getContext(), "添加失败");
        }
    }

    private class MyPredictView extends PredictViewAdapter {

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

    private class MyNoteView extends NoteViewAdapter {
        @Override
        public void onUpdateSuccess(Note note) {
            if (multiDetailStage.getData() == null) {
                return;
            }
            int index = multiDetailStage.getData().indexOf(note);
            multiDetailStage.setCurrentIndex(index == -1 ? 0 : index);
            refresh();
        }
    }

    private class MyViewFactory implements MultiDetailStage.ViewFactory {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public View getView() {

            @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_note_detail_content, null);
            view.setMinimumHeight(ViewUtil.getScreenSize().y);

            WebView displayWebView = view.findViewById(R.id.fragment_document_display_web);

            WebViewUtil.configWebView(displayWebView);
            WebViewUtil.clearHistory();
            displayWebView.setOnTouchListener(new OnSlideListener(getContext()) {

                @Override
                protected void onSlideLeft() {
                    next();
                }

                @Override
                protected void onSlideRight() {
                    prev();
                }

                @Override
                protected void onSlideDown() {
                    if (operationContainer.getVisibility() == View.GONE) {
                        pageCountTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_gray));
                        operationContainer.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                protected void onSlideUp() {
                    operationContainer.setVisibility(View.GONE);
                }

                @Override
                protected void onDoubleClick(MotionEvent e) {
                    showModifyView();
                }

                @Override
                protected int getSlideXSensitivity() {
                    return 100;
                }

                @Override
                protected int getSlideYSensitivity() {
                    return (int) (ViewUtil.getScreenSize().y * 0.22);
                }
            });
            return view;
        }

    }
}
