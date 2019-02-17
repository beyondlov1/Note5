package com.beyond.note5.view.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
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
import com.beyond.note5.event.DeleteNoteEvent;
import com.beyond.note5.event.DetailNoteEvent;
import com.beyond.note5.event.FillNoteModifyEvent;
import com.beyond.note5.event.HideNoteDetailEvent;
import com.beyond.note5.event.ModifyNoteDoneEvent;
import com.beyond.note5.event.UpdateNoteEvent;
import com.beyond.note5.utils.ViewUtil;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.custom.ViewSwitcher;
import com.beyond.note5.view.listener.OnBackPressListener;
import com.beyond.note5.view.listener.OnSlideListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class NoteDetailSuperFragment extends DialogFragment implements OnBackPressListener {
    private static final String TAG = "NoteDetailSuperFragment";
    protected Context context;
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
    private View modifyButton;
    private View hideButton;

    public static AtomicBoolean isShowing = new AtomicBoolean(false);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        System.out.println("onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("onCreate");

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
                    EventBus.getDefault().post(new DeleteNoteEvent(currentNote));
                    if (data.isEmpty()) {
                        hide();
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
                    EventBus.getDefault().post(new DeleteNoteEvent(currentNote));
                    if (data.isEmpty()) {
                        hide();
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
                    Toast.makeText(context, "搜索文字不能超过32个字", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(context, "搜索文字不能超过32个字", Toast.LENGTH_SHORT).show();
                }
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
                hide();
            }
        });
    }

    protected void show() {
//        EventBus.getDefault().post( new ShowNoteDetailEvent(this));
    }

    protected void hide() {
        viewSwitcher.removeAllViews();
        if(data.isEmpty()){
            currIndex = -1;
        }
        EventBus.getDefault().post(new HideNoteDetailEvent(currIndex));
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
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
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
    public void onResume() {
        super.onResume();
        System.out.println("onResume");


    }

    @Override
    public void onStop() {
        System.out.println("onStop");

        EventBus.getDefault().unregister(this);
        isShowing.set(false);
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        System.out.println("onDetach");
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(DetailNoteEvent detailNoteEvent) {
        show();
        data = detailNoteEvent.get();
        currIndex = detailNoteEvent.getIndex();
        processDetailTools();
        reloadView();
    }

    private void processDetailTools() {
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
                    note.setReadFlag(0);
                    EventBus.getDefault().post(new UpdateNoteEvent(note));
                    Toast.makeText(context, "取消置顶", Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(new ModifyNoteDoneEvent(note));
                    ((ImageButton) stickButton).setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up_grey_600_24dp, null));
                } else { //其他
                    note.setReadFlag(-1);
                    EventBus.getDefault().post(new UpdateNoteEvent(note));
                    Toast.makeText(context, "置顶成功", Toast.LENGTH_SHORT).show();
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
                hide();
            }

            @Override
            protected void onSlideDown() {
            }

            @Override
            protected void onDoubleClick(MotionEvent e) {
                showModifyView();
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
                hide();
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
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        System.out.println("onAttachFragment");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        System.out.println("onHiddenChanged");
    }


    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        System.out.println("onInflate");
    }

    @Override
    public boolean onBackPressed() {
        hide();
        return true;
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
