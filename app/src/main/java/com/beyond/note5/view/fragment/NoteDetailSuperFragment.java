package com.beyond.note5.view.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.event.AddTodoSuccessEvent;
import com.beyond.note5.event.DeleteNoteSuccessEvent;
import com.beyond.note5.event.DetailNoteEvent;
import com.beyond.note5.event.FillNoteModifyEvent;
import com.beyond.note5.event.HideNoteDetailEvent;
import com.beyond.note5.event.ModifyNoteDoneEvent;
import com.beyond.note5.event.ScrollToNoteEvent;
import com.beyond.note5.event.ShowNoteDetailEvent;
import com.beyond.note5.event.UpdateNoteSuccessEvent;
import com.beyond.note5.presenter.CalendarPresenterImpl;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.presenter.PredictPresenterImpl;
import com.beyond.note5.presenter.TodoCompositePresenter;
import com.beyond.note5.presenter.TodoCompositePresenterImpl;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.utils.ViewUtil;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.adapter.view.CalendarViewAdapter;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;
import com.beyond.note5.view.adapter.view.PredictViewAdapter;
import com.beyond.note5.view.adapter.view.TodoViewAdapter;
import com.beyond.note5.view.animator.SmoothScalable;
import com.beyond.note5.view.animator.SmoothScaleAnimation;
import com.beyond.note5.view.custom.ViewSwitcher;
import com.beyond.note5.view.listener.OnBackPressListener;
import com.beyond.note5.view.listener.OnSlideListener;

import org.apache.commons.collections4.CollectionUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class NoteDetailSuperFragment extends DialogFragment implements OnBackPressListener, SmoothScalable,FragmentContainerAware {
    private static final String TAG = NoteDetailSuperFragment.class.getSimpleName();
    protected Activity context;
    protected View root;
    protected ViewSwitcher viewSwitcher;
    protected TextView pageCountTextView;
    protected DetailViewHolder detailViewHolder;

    protected List<Note> data;
    protected int currIndex;
    private int firstInIndex;

    private ShowNoteDetailEvent.ShowType showType;

    protected View operationContainer;
    protected View operationItemsContainer;
    protected View deleteButton;
    protected View searchButton;
    protected View browserSearchButton;
    protected View stickButton;
    protected View convertButton;
    protected View doneButton;
    private View modifyButton;
    private View hideButton;

    private View fragmentContainer;

    public static AtomicBoolean isShowing = new AtomicBoolean(false);

    private MyCalendarView calendarView = new MyCalendarView();
    private MyPredictView predictView = new MyPredictView();
    private MyTodoView todoView = new MyTodoView();

    private TodoCompositePresenter todoCompositePresenter;
    private NotePresenter notePresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();

        initInjection();
    }

    private void initInjection() {
        todoCompositePresenter = new TodoCompositePresenterImpl.Builder(new TodoPresenterImpl(todoView))
                .calendarPresenter(new CalendarPresenterImpl(getActivity(), calendarView))
                .predictPresenter(new PredictPresenterImpl(predictView))
                .build();
        notePresenter = new NotePresenterImpl(new MyNoteView());

    }


    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        System.out.println("onCreateView");

        if (root == null) {
            root = LayoutInflater.from(context).inflate(R.layout.fragment_note_detail, null);
        }
        initView(root);
        initEvent(root);
        return root;
    }

    protected void initView(View view) {
        viewSwitcher = view.findViewById(R.id.fragment_note_detail_view_switcher);
        pageCountTextView = view.findViewById(R.id.fragment_note_detail_page_count);
        operationContainer = view.findViewById(R.id.fragment_note_detail_operation_container);
        operationItemsContainer = view.findViewById(R.id.fragment_note_detail_operation_items);
        deleteButton = view.findViewById(R.id.fragment_note_detail_operation_delete);
        searchButton = view.findViewById(R.id.fragment_note_detail_operation_search);
        browserSearchButton = view.findViewById(R.id.fragment_note_detail_operation_browser_search);
        stickButton = view.findViewById(R.id.fragment_note_detail_operation_stick);
        convertButton = view.findViewById(R.id.fragment_note_detail_to_todo);
        doneButton = view.findViewById(R.id.fragment_note_detail_operation_done);
        modifyButton = view.findViewById(R.id.fragment_note_detail_modify);
        modifyButton.setVisibility(View.GONE);
        hideButton = view.findViewById(R.id.fragment_note_detail_hide);
        hideButton.setVisibility(View.GONE);
        pageCountTextView.getLayoutParams().height = 100;
        pageCountTextView.setLayoutParams(pageCountTextView.getLayoutParams());
    }

    protected void initEvent(View view) {
        //防止事件向下传递
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note currentNote = data.get(currIndex);
                notePresenter.deleteDeep(currentNote);
                if (data.isEmpty()) {
                    sendHideMessage();
                    return;
                }
                if (currIndex == data.size()) {
                    currIndex--;
                }
                reloadView();
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadWebPage();
            }
        });
        browserSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = WebViewUtil.getUrlOrSearchUrl(data.get(currIndex));
                if (url != null) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } else {
                    ToastUtil.toast(context, "搜索文字不能超过32个字", Toast.LENGTH_SHORT);
                }
            }
        });
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note note = data.get(currIndex);
                Todo todo = new Todo();
                todo.setId(note.getId());
                note.setTitle(note.getTitle());
                todo.setContent(note.getContent());
                todo.setCreateTime(note.getCreateTime());
                todo.setLastModifyTime(new Date());
                todo.setVersion(note.getVersion());
                todoCompositePresenter.add(todo);
                notePresenter.delete(note);
                sendHideMessage();
                ToastUtil.toast(getContext(), "已转化为TODO", Toast.LENGTH_SHORT);
            }

        });
        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModifyView();
            }
        });
        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHideMessage();
            }
        });
    }

    private void loadWebPage() {
        Note currNote = data.get(currIndex);
        String url = WebViewUtil.getUrlOrSearchUrl(currNote);
        if (url != null) {
            WebView currentWebView = new DetailViewHolder(viewSwitcher.getCurrentView()).displayWebView;
            WebViewUtil.addWebViewProgressBar(currentWebView);
            currentWebView.loadUrl(url);
        } else {
            ToastUtil.toast(context, "搜索文字不能超过32个字", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("onStart");

        EventBus.getDefault().register(this);
        isShowing.set(true);
    }

    @Override
    public void onStop() {
        System.out.println("onStop");

        EventBus.getDefault().unregister(this);
        isShowing.set(false);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(DetailNoteEvent detailNoteEvent) {
        if (detailNoteEvent.isConsumed()) {
            return;
        }
        data = detailNoteEvent.get();
        currIndex = detailNoteEvent.getIndex();
        firstInIndex = currIndex;
        showType = detailNoteEvent.getShowType();
        reloadView();
        detailNoteEvent.setConsumed(true);
    }

    /**
     * 处理可变的按钮
     */
    private void processVariableTools() {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        // 置顶按钮
        if (data.get(currIndex).getReadFlag() < 0) { // 置顶
            ((ImageButton) stickButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up_blue_400_24dp, null));
        } else { //其他
            ((ImageButton) stickButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up_grey_600_24dp, null));

        }
        stickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note note = data.get(currIndex);
                note.setLastModifyTime(new Date());
                if (data.get(currIndex).getReadFlag() < 0) { // 置顶
                    note.setReadFlag(DocumentConst.READ_FLAG_NORMAL);
                    EventBus.getDefault().post(new UpdateNoteSuccessEvent(note));
                    ToastUtil.toast(context, "取消置顶", Toast.LENGTH_SHORT);
                    EventBus.getDefault().post(new ModifyNoteDoneEvent(note));
                    ((ImageButton) stickButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up_grey_600_24dp, null));
                } else { //其他
                    note.setReadFlag(DocumentConst.READ_FLAG_STICK);
                    EventBus.getDefault().post(new UpdateNoteSuccessEvent(note));
                    ToastUtil.toast(context, "置顶成功", Toast.LENGTH_SHORT);
                    EventBus.getDefault().post(new ModifyNoteDoneEvent(note));
                    ((ImageButton) stickButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up_blue_400_24dp, null));
                }
            }
        });


        // 置顶按钮
        if (data.get(currIndex).getReadFlag() > 0) { // 置顶
            ((ImageButton) doneButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_done_blue_24dp, null));
        } else { //其他
            ((ImageButton) doneButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_done_grey_600_24dp, null));

        }
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note note = data.get(currIndex);
                note.setLastModifyTime(new Date());
                if (data.get(currIndex).getReadFlag() > 0) {

                    Note currentNote = data.get(currIndex);
                    currentNote.setReadFlag(DocumentConst.READ_FLAG_NORMAL);
                    EventBus.getDefault().post(new UpdateNoteSuccessEvent(currentNote));
                    ToastUtil.toast(context, "取消已读", Toast.LENGTH_SHORT);
                    reloadView();
                    ((ImageButton) doneButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_done_grey_600_24dp, null));
                } else { //其他
                    Note currentNote = data.get(currIndex);
                    currentNote.setReadFlag(DocumentConst.READ_FLAG_DONE);
                    int oldIndex = currIndex;
                    EventBus.getDefault().post(new UpdateNoteSuccessEvent(currentNote));
                    currIndex = oldIndex;
                    ToastUtil.toast(context, "已读", Toast.LENGTH_SHORT);
                    reloadView();
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ModifyNoteDoneEvent modifyNoteDoneEvent) {
        Note note = modifyNoteDoneEvent.get();
        int index = data.indexOf(note);
        currIndex = index == -1 ? 0 : index;
        reloadView();
    }

    @SuppressWarnings("ConstantConditions")
    private void reloadView() {
        processVariableTools();
        viewSwitcher.removeAllViews();
        viewSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.fragment_note_detail_content, null);
                view.setMinimumHeight(2000);

                detailViewHolder = new NoteDetailSuperFragment.DetailViewHolder(view);

                initContentConfig(detailViewHolder);
                initContentData(detailViewHolder);
                initContentEvent(detailViewHolder);

                return view;
            }
        });
        openWebPage();
        resetShowType();
        scrollRecyclerViewTo(data.get(currIndex));
    }

    private void initContentConfig(NoteDetailSuperFragment.DetailViewHolder detailViewHolder) {
        WebViewUtil.configWebView(detailViewHolder.displayWebView);
    }

    private void initContentData(NoteDetailSuperFragment.DetailViewHolder detailViewHolder) {
        this.detailViewHolder = detailViewHolder;
        WebViewUtil.clearHistory();
        WebViewUtil.loadWebContent(detailViewHolder.displayWebView, data.get(currIndex));
        String pageCount = String.format("%s/%s", currIndex + 1, data.size());
        pageCountTextView.setText(pageCount);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initContentEvent(final NoteDetailSuperFragment.DetailViewHolder detailViewHolder) {
        detailViewHolder.displayWebView.setOnTouchListener(new OnSlideListener(context) {
            @Override
            protected void onSlideLeft() {
                next();
            }

            @Override
            protected void onSlideRight() {
                prev();
            }

            @Override
            protected void onSlideUp() {
            }

            @Override
            protected void onSlideDown() {
            }

            @Override
            protected void onDoubleClick(MotionEvent e) {
                showModifyView();
            }

            @Override
            protected int getSlideXSensitivity() {
                return 250;
            }

            @Override
            protected int getSlideYSensitivity() {
                return (int) (ViewUtil.getScreenSize().y * 0.33);
            }
        });
        pageCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHideMessage();
            }
        });
    }

    private void openWebPage() {
        if (showType == ShowNoteDetailEvent.ShowType.WEB) {
            loadWebPage();
        }
    }

    private void scrollRecyclerViewTo(Note note) {
        EventBus.getDefault().post(new ScrollToNoteEvent(note));
    }

    private void resetShowType() {
        this.showType = ShowNoteDetailEvent.ShowType.CONTENT;
    }

    private void next() {
        if (currIndex == data.size() - 1) {
            msg("已到达最后一页");
        }
        if (currIndex < data.size() - 1) {
            currIndex++;
            viewSwitcher.setInAnimation(context, R.anim.slide_in_right);
            viewSwitcher.setOutAnimation(context, R.anim.slide_out_left);
            initContentData(new NoteDetailSuperFragment.DetailViewHolder(viewSwitcher.getNextView()));
            viewSwitcher.showNext();
            processVariableTools();
        }
    }

    private void prev() {
        if (currIndex == 0) {
            msg("已到达第一页");
        }
        if (currIndex > 0) {
            currIndex--;
            viewSwitcher.setInAnimation(context, R.anim.slide_in_left);
            viewSwitcher.setOutAnimation(context, R.anim.slide_out_right);
            initContentData(new NoteDetailSuperFragment.DetailViewHolder(viewSwitcher.getNextView()));
            viewSwitcher.showPrevious();
            processVariableTools();
        }
    }

    protected void showModifyView() {
        NoteModifyFragment noteModifyFragment = new NoteModifyFragment();
        noteModifyFragment.show(getActivity().getSupportFragmentManager(), "modifyDialog");
        EventBus.getDefault().postSticky(new FillNoteModifyEvent(data.get(currIndex)));
    }

    @Override
    public boolean onBackPressed() {
        WebView displayWebView = new DetailViewHolder(viewSwitcher.getCurrentView()).displayWebView;
        if (WebViewUtil.canGoBack(displayWebView)) {
            WebViewUtil.goBack(displayWebView);
        } else {
            WebViewUtil.clearHistory();
            sendHideMessage();
        }
        return true;
    }

    private void sendHideMessage() {
        viewSwitcher.removeAllViews();
        if (data.isEmpty()) {
            currIndex = -1;
        }
        HideNoteDetailEvent event = new HideNoteDetailEvent(currIndex);
        event.setFirstIndex(firstInIndex);
        EventBus.getDefault().post(event);
    }

    public void registerHooks(SmoothScaleAnimation smoothScaleAnimation) {
        smoothScaleAnimation.setAfterShowHook(new Runnable() {
            @Override
            public void run() {
                getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.white));
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
            }
        });
        smoothScaleAnimation.setBeforeHideHook(new Runnable() {
            @Override
            public void run() {
                getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(),R.color.white));
            }
        });
    }

    private void msg(String msg) {
        ToastUtil.toast(context, msg, Toast.LENGTH_SHORT);
    }

    @Override
    public void setFragmentContainer(View fragmentContainer) {
        this.fragmentContainer = fragmentContainer;
    }

    class DetailViewHolder {
        ViewGroup displayContainer;
        WebView displayWebView;

        DetailViewHolder(View view) {
            displayContainer = view.findViewById(R.id.fragment_document_display_container);
            displayWebView = view.findViewById(R.id.fragment_document_display_web);
        }
    }


    private class MyTodoView extends TodoViewAdapter {
        @Override
        public void onAddSuccess(Todo document) {
            EventBus.getDefault().post(new AddTodoSuccessEvent(document));
        }

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
            ToastUtil.toast(getContext(),"添加到日历事件失败");
        }

        @Override
        public void onEventFindAllSuccess(List<Todo> allTodo) {
            ToastUtil.toast(getContext(),"成功查询日历事件");
        }
    }

    private class MyNoteView extends NoteViewAdapter {
        @Override
        public void onDeleteSuccess(Note document) {
            EventBus.getDefault().post(new DeleteNoteSuccessEvent(document));
        }
    }
}
