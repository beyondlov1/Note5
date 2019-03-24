package com.beyond.note5.view.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import com.beyond.note5.event.*;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.utils.ViewUtil;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.animator.DefaultSmoothScalable;
import com.beyond.note5.view.animator.SmoothScalable;
import com.beyond.note5.view.custom.ViewSwitcher;
import com.beyond.note5.view.listener.OnBackPressListener;
import com.beyond.note5.view.listener.OnSlideListener;
import org.apache.commons.collections4.CollectionUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class NoteDetailSuperFragment extends DialogFragment implements OnBackPressListener,SmoothScalable {
    private static final String TAG = "NoteDetailSuperFragment";
    protected Activity context;
    protected View root;
    protected ViewSwitcher viewSwitcher;
    protected TextView pageCountTextView;
    protected DetailViewHolder detailViewHolder;

    protected List<Note> data;
    protected int currIndex;

    protected View operationContainer;
    protected View operationItemsContainer;
    protected View deleteButton;
    protected View searchButton;
    protected View browserSearchButton;
    protected View stickButton;
    protected View convertButton;
    private View modifyButton;
    private View hideButton;

    private SmoothScalable smoothScalable = new DefaultSmoothScalable();

    public static AtomicBoolean isShowing = new AtomicBoolean(false);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        System.out.println("onCreateView");

        if (root == null) {
            root = LayoutInflater.from(context).inflate(R.layout.fragment_note_detail, null);
        }
//        root = inflater.inflate(R.layout.fragment_note_detail, container, false);
        initCommonView(root);
        initCommonEvent(root);
        initView(root);
        initEvent();
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("onViewCreated");
    }

    private void initCommonView(View view) {
        viewSwitcher = view.findViewById(R.id.fragment_note_detail_view_switcher);
        pageCountTextView = view.findViewById(R.id.fragment_note_detail_page_count);
        operationContainer = view.findViewById(R.id.fragment_note_detail_operation_container);
        operationItemsContainer = view.findViewById(R.id.fragment_note_detail_operation_items);
        deleteButton = view.findViewById(R.id.fragment_note_detail_operation_delete);
        searchButton = view.findViewById(R.id.fragment_note_detail_operation_search);
        browserSearchButton = view.findViewById(R.id.fragment_note_detail_operation_browser_search);
        stickButton = view.findViewById(R.id.fragment_note_detail_operation_stick);
        convertButton = view.findViewById(R.id.fragment_note_detail_to_todo);
    }

    protected void initView(View view) {
        modifyButton = view.findViewById(R.id.fragment_note_detail_modify);
        modifyButton.setVisibility(View.GONE);
        hideButton = view.findViewById(R.id.fragment_note_detail_hide);
        hideButton.setVisibility(View.GONE);
        pageCountTextView.getLayoutParams().height = 100;
        pageCountTextView.setLayoutParams(pageCountTextView.getLayoutParams());
    }

    private static final boolean IS_OPERATION_AUTO_HIDE = false;
    private Timer operationItemsTimer;

    private void initCommonEvent(View view) {
        //防止事件向下传递
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        //Operation
        if (IS_OPERATION_AUTO_HIDE) {
            final Handler handler = new Handler();
            operationItemsTimer = new Timer();
            final AtomicBoolean isCanceled = new AtomicBoolean(true);
            class HideOperationTimerTask extends TimerTask {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideOperation();
                        }
                    });
                    isCanceled.set(true);
                }
            }

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Note currentNote = data.get(currIndex);
                    EventBus.getDefault().post(new DeleteDeepNoteEvent(currentNote));
                    if (data.isEmpty()) {
                        sendHideMessage();
                        return;
                    }
                    if (currIndex == data.size()) {
                        currIndex--;
                    }
                    viewSwitcher.removeAllViews();
                    reloadView();

                    if (!isCanceled.get()) {
                        operationItemsTimer.cancel();
                    }
                    operationItemsTimer = new Timer();
                    operationItemsTimer.schedule(new HideOperationTimerTask(), 5000);
                }
            });

            operationContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            operationContainer.setOnTouchListener(new OnSlideListener(context) {
                @Override
                protected void onSlideLeft() {

                }

                @Override
                protected void onSlideRight() {

                }

                @Override
                protected void onSlideUp() {
                    hideOperation();
                    isCanceled.set(true);
                }

                @Override
                protected void onSlideDown() {
                    showOperation();
                    operationItemsTimer.schedule(new HideOperationTimerTask(), 5000);
                    isCanceled.set(false);
                }

                @Override
                protected void onDoubleClick(MotionEvent e) {

                }
            });
        } else {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Note currentNote = data.get(currIndex);
                    EventBus.getDefault().post(new DeleteDeepNoteEvent(currentNote));
                    if (data.isEmpty()) {
                        sendHideMessage();
                        return;
                    }
                    if (currIndex == data.size()) {
                        currIndex--;
                    }
                    viewSwitcher.removeAllViews();
                    reloadView();
                }
            });
        }
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = WebViewUtil.getUrl(data.get(currIndex));
                if (url != null) {
                    WebViewUtil.addWebViewProgressBar(new NoteDetailSuperFragment.DetailViewHolder(viewSwitcher.getCurrentView()).displayWebView);
                    new NoteDetailSuperFragment.DetailViewHolder(viewSwitcher.getCurrentView()).displayWebView.loadUrl(url);
                } else {
                    ToastUtil.toast(context, "搜索文字不能超过32个字", Toast.LENGTH_SHORT);
                }
            }
        });
        browserSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = WebViewUtil.getUrl(data.get(currIndex));
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
                EventBus.getDefault().post(new AddTodoEvent(todo));
                EventBus.getDefault().post(new DeleteNoteEvent(note));
                sendHideMessage();
                ToastUtil.toast(getContext(),"已转化为TODO",Toast.LENGTH_SHORT);
            }
        });

    }

    protected void initEvent() {
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

    private void showOperation() {
        operationItemsContainer.setVisibility(View.VISIBLE);
        Animator animator = AnimatorInflater.loadAnimator(context, R.animator.fade_in);
        animator.setTarget(operationItemsContainer);
        animator.start();
    }

    private void hideOperation() {
        Animator animator = AnimatorInflater.loadAnimator(context, R.animator.fade_out);
        animator.setTarget(operationItemsContainer);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                operationItemsContainer.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void msg(String msg) {
        ToastUtil.toast(context, msg, Toast.LENGTH_SHORT);
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("onStart");

        EventBus.getDefault().register(this);
        isShowing.set(true);
        initDialogButtonEvent();
    }

    protected void initDialogButtonEvent() {
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
        data = detailNoteEvent.get();
        currIndex = detailNoteEvent.getIndex();
        processDetailTools();
        reloadView();
    }

    private void processDetailTools() {
        if (CollectionUtils.isEmpty(data)){
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
                    EventBus.getDefault().post(new UpdateNoteEvent(note));
                    ToastUtil.toast(context, "取消置顶", Toast.LENGTH_SHORT);
                    EventBus.getDefault().post(new ModifyNoteDoneEvent(note));
                    ((ImageButton) stickButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up_grey_600_24dp, null));
                } else { //其他
                    note.setReadFlag(DocumentConst.READ_FLAG_STICK);
                    EventBus.getDefault().post(new UpdateNoteEvent(note));
                    ToastUtil.toast(context, "置顶成功", Toast.LENGTH_SHORT);
                    EventBus.getDefault().post(new ModifyNoteDoneEvent(note));
                    ((ImageButton) stickButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up_blue_400_24dp, null));
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
        beforeReloadView();
        viewSwitcher.removeAllViews();
        viewSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.fragment_note_detail_content, null);
                view.setMinimumHeight(2000);

                detailViewHolder = new NoteDetailSuperFragment.DetailViewHolder(view);

                initDetailContentConfig(detailViewHolder);
                initDetailContentData(detailViewHolder);
                initCommonDetailContentEvent(detailViewHolder);
                initDetailContentEvent(detailViewHolder);

                return view;
            }
        });
    }

    protected void beforeReloadView() {
    }

    private void initDetailContentConfig(NoteDetailSuperFragment.DetailViewHolder detailViewHolder) {
        WebViewUtil.configWebView(detailViewHolder.displayWebView);
    }

    private void initDetailContentData(NoteDetailSuperFragment.DetailViewHolder detailViewHolder) {
        this.detailViewHolder = detailViewHolder;
        WebViewUtil.clearHistory();
        WebViewUtil.loadWebContent(detailViewHolder.displayWebView, data.get(currIndex));
        String pageCount = String.format("%s/%s", currIndex + 1, data.size());
        pageCountTextView.setText(pageCount);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initCommonDetailContentEvent(final NoteDetailSuperFragment.DetailViewHolder detailViewHolder) {
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
//                hide();
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
                return 350;
            }
            @Override
            protected int getSlideYSensitivity() {
                return (int) (ViewUtil.getScreenSize().y * 0.33);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initDetailContentEvent(DetailViewHolder detailViewHolder) {
        pageCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHideMessage();
            }
        });
    }

    private void next() {
        if (currIndex == data.size() - 1) {
            msg("已到达最后一页");
        }
        if (currIndex < data.size() - 1) {
            currIndex++;
            viewSwitcher.setInAnimation(context, R.anim.slide_in_right);
            viewSwitcher.setOutAnimation(context, R.anim.slide_out_left);
            initDetailContentData(new NoteDetailSuperFragment.DetailViewHolder(viewSwitcher.getNextView()));
            viewSwitcher.showNext();
            processDetailTools();
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
            initDetailContentData(new NoteDetailSuperFragment.DetailViewHolder(viewSwitcher.getNextView()));
            viewSwitcher.showPrevious();
            processDetailTools();
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
        if (WebViewUtil.canGoBack(displayWebView)){
            WebViewUtil.goBack(displayWebView);
        }else {
            WebViewUtil.clearHistory();
            sendHideMessage();
        }
        return true;
    }

    private void sendHideMessage(){
        viewSwitcher.removeAllViews();
        if(data.isEmpty()){
            currIndex = -1;
        }
        EventBus.getDefault().post(new HideNoteDetailEvent(currIndex));
    }

    public void setSmoothScalable(SmoothScalable smoothScalable){
        this.smoothScalable = smoothScalable;
    }

    @Override
    public void setContainer(View view) {
        this.smoothScalable.setContainer(view);
    }

    @Override
    public View getContainer() {
        return smoothScalable.getContainer();
    }

    @Override
    public void setStartView(View view) {
        this.smoothScalable.setStartView(view);
    }

    @Override
    public View getStartView() {
        return this.smoothScalable.getStartView();
    }

    @Override
    public void setEndView(View view) {
        this.smoothScalable.setEndView(view);
    }

    @Override
    public View getEndView() {
        return this.smoothScalable.getEndView();
    }

    @Override
    public void setShowingView(View view) {
        this.smoothScalable.setShowingView(view);
    }

    @Override
    public View getShowingView() {
        return this.smoothScalable.getShowingView();
    }

    @Override
    public void show() {
        this.smoothScalable.setOnShownListener(new Runnable() {
            @Override
            public void run() {
                context.getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            }
        });
        this.smoothScalable.setOnHiddenListener(new Runnable() {
            @Override
            public void run() {
                smoothScalable.getContainer().setVisibility(View.GONE);
            }
        });
        this.smoothScalable.show();
    }

    /**
     * 不要在本类调用
     */
    @Override
    public void hide() {
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.google_blue));
        this.smoothScalable.hide();
    }

    @Override
    public void setOnShownListener(Runnable onShownListener) {
        //do nothing
    }

    @Override
    public void setOnHiddenListener(Runnable onHiddenListener) {
        //do nothing
    }

    class DetailViewHolder {
        ViewGroup displayContainer;
        WebView displayWebView;

        DetailViewHolder(View view) {
            displayContainer = view.findViewById(R.id.fragment_document_display_container);
            displayWebView = view.findViewById(R.id.fragment_document_display_web);
        }
    }

}
