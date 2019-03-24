package com.beyond.note5.view.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.event.DeleteDeepNoteEvent;
import com.beyond.note5.event.DetailNoteEvent;
import com.beyond.note5.event.FillNoteModifyEvent;
import com.beyond.note5.event.ModifyNoteDoneEvent;
import com.beyond.note5.event.UpdateNoteEvent;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.custom.ViewSwitcher;
import com.beyond.note5.view.listener.OnSlideListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: beyond
 * @date: 2019/2/2
 */
@Deprecated
public class NoteDetailFragment extends DialogFragment {

    private Context context;
    private View root;
    private ViewSwitcher viewSwitcher;
    private TextView pageCountTextView;
    private DetailViewHolder detailViewHolder;

    private List<Note> data;
    private int currIndex;

    private View operationContainer;
    private View operationItemsContainer;
    private View deleteButton;
    private View searchButton;
    private View browserSearchButton;
    private View stickButton;

    public static AtomicBoolean isShowing = new AtomicBoolean(false);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        root = LayoutInflater.from(context).inflate(R.layout.fragment_note_detail, null);
        builder.setView(root)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Modify", null);
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        root = inflater.inflate(R.layout.fragment_note_detail, container, false);
        initView(root);
        initDialogAnimation();
        initEvent();
        return root;
    }

    private void initView(View view) {
        viewSwitcher = view.findViewById(R.id.fragment_note_detail_view_switcher);
        pageCountTextView = view.findViewById(R.id.fragment_note_detail_page_count);
        operationContainer = view.findViewById(R.id.fragment_note_detail_operation_container);
        operationItemsContainer = view.findViewById(R.id.fragment_note_detail_operation_items);
        deleteButton = view.findViewById(R.id.fragment_note_detail_operation_delete);
        searchButton = view.findViewById(R.id.fragment_note_detail_operation_search);
        browserSearchButton = view.findViewById(R.id.fragment_note_detail_operation_browser_search);
        stickButton = view.findViewById(R.id.fragment_note_detail_operation_stick);
    }

    @SuppressWarnings("ConstantConditions")
    private void initDialogAnimation() {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setWindowAnimations(R.style.detail_dialog_animation);
    }

    private static final boolean IS_OPERATION_AUTO_HIDE = false;
    private Timer operationItemsTimer;

    private void initEvent() {
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
                        getDialog().dismiss();
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
                        getDialog().dismiss();
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
                    WebViewUtil.addWebViewProgressBar(new DetailViewHolder(viewSwitcher.getCurrentView()).displayWebView);
                    new DetailViewHolder(viewSwitcher.getCurrentView()).displayWebView.loadUrl(url);
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
        initDialogSize();
        EventBus.getDefault().register(this);
        isShowing.set(true);
        initDialogButton(); // 初始化dialogButton， 放在别的地方报空指针
    }

    protected void initDialogButton(){
        //DialogButton
        ((AlertDialog) getDialog()).getButton(-1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ((AlertDialog) getDialog()).getButton(-2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ((AlertDialog) getDialog()).getButton(-3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModifyView();
            }
        });
    }

    private void initDialogSize() {
        //要放到这里才有用, 可能是onCreateView的时候没有加载全
        //初始化默认弹出窗口大小设置
        Window win = getDialog().getWindow();
//        // 一定要设置Background，如果不设置，window属性设置无效
        assert win != null;
        win.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = (int) (dm.widthPixels * 0.9);
        params.height = (int) (dm.heightPixels * 0.87);
        win.setAttributes(params);
        root.setMinimumHeight(dm.heightPixels);
        root.setMinimumHeight(dm.heightPixels);
    }

    @Override
    public void onStop() {
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
                    note.setReadFlag(DocumentConst.READ_FLAG_DONE);
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
        viewSwitcher.removeAllViews();
        reloadView();
    }

    @SuppressWarnings("ConstantConditions")
    private void reloadView() {
        // 默认情况下，dialog布局中设置EditText，在点击EditText后输入法不能弹出来, 将此标志位清除，则可以显示输入法
        this.getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        viewSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.fragment_note_detail_content, null);
                view.setMinimumHeight(2000);

                detailViewHolder = new DetailViewHolder(view);

                initDetailConfig(detailViewHolder);
                initDetailData(detailViewHolder);
                initDetailEvent(detailViewHolder);

                return view;
            }
        });
    }

    private void initDetailConfig(DetailViewHolder detailViewHolder) {
        WebViewUtil.configWebView(detailViewHolder.displayWebView);
    }

    private void initDetailData(DetailViewHolder detailViewHolder) {
        this.detailViewHolder = detailViewHolder;
        WebViewUtil.loadWebContent(detailViewHolder.displayWebView, data.get(currIndex));
        String pageCount = String.format("%s/%s", currIndex + 1, data.size());
        pageCountTextView.setText(pageCount);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initDetailEvent(final DetailViewHolder detailViewHolder) {
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
            initDetailData(new DetailViewHolder(viewSwitcher.getNextView()));
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
            initDetailData(new DetailViewHolder(viewSwitcher.getNextView()));
            viewSwitcher.showPrevious();
            processDetailTools();
        }
    }

    private void showModifyView() {
        NoteModifyFragment noteModifyFragment = new NoteModifyFragment();
        noteModifyFragment.show(getActivity().getSupportFragmentManager(), "modifyDialog");
        EventBus.getDefault().postSticky(new FillNoteModifyEvent(data.get(currIndex)));
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
