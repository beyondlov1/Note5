package com.beyond.note5.view.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.event.DeleteNoteEvent;
import com.beyond.note5.event.DetailNoteEvent;
import com.beyond.note5.event.UpdateNoteEvent;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.custom.ViewSwitcher;
import com.beyond.note5.view.listener.OnSlideListener;

import org.apache.commons.lang3.ObjectUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by beyond on 2019/2/2.
 */

public class NoteDetailSwitcherFragment extends DialogFragment {

    private Context context;
    private View view;
    private ViewSwitcher viewSwitcher;
    private TextView pageCountTextView;
    private DetailViewHolder detailViewHolder;

    private List<Note> notes;
    private int position;
    private ViewGroup operationContainer;
    private View operationItemsContainer;
    private View deleteButton;
    private View searchButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        view = LayoutInflater.from(context).inflate(R.layout.fragment_note_detail_switcher, null);
        builder.setView(view)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Modify", null);
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        root = inflater.inflate(R.layout.fragment_note_detail_switcher, container, false);
        initView(view);
        initDialogAnimation();
        initEvent();
        return view;
    }

    private void initView(View view) {
        viewSwitcher = view.findViewById(R.id.fragment_note_detail_view_switcher);
        pageCountTextView = view.findViewById(R.id.fragment_note_detail_page_count);
        operationContainer = view.findViewById(R.id.fragment_note_detail_operation_container);
        operationItemsContainer = view.findViewById(R.id.fragment_note_detail_operation_items);
        deleteButton = view.findViewById(R.id.fragment_note_detail_operation_delete);
        searchButton = view.findViewById(R.id.fragment_note_detail_operation_search);
    }

    private void initDialogAnimation() {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setWindowAnimations(R.style.detail_dialog_animation);
    }

    private Timer operationItemsTimer;
    private void initEvent() {
        //Operation
        final Handler handler = new Handler();
        operationItemsTimer = new Timer();
        final AtomicBoolean isCanceled= new AtomicBoolean(true);
        class HideOperationTimerTask extends TimerTask{
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
                Note currentNote = notes.get(position);
                EventBus.getDefault().post(new DeleteNoteEvent(currentNote));
                if (notes.isEmpty()) {
                    getDialog().dismiss();
                    return;
                }
                if (position == notes.size()) {
                    position--;
                }
                viewSwitcher.removeAllViews();
                reloadView();

                if (!isCanceled.get()){
                    operationItemsTimer.cancel();
                }
                operationItemsTimer = new Timer();
                operationItemsTimer.schedule(new HideOperationTimerTask(), 5000);
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(WebViewUtil.getUrl(notes.get(position)));
                new DetailViewHolder(view).displayWebView.loadUrl(WebViewUtil.getUrl(notes.get(position)));
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
        initDialogSize();
        EventBus.getDefault().register(this);
    }

    private void initDialogSize() {
            //要放到这里才有用, 可能是onCreateView的时候没有加载全
            //初始化默认弹出窗口大小设置
            Window win = getDialog().getWindow();
//        // 一定要设置Background，如果不设置，window属性设置无效
            win.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            WindowManager.LayoutParams params = win.getAttributes();
            params.gravity = Gravity.CENTER;
            params.width = (int) (dm.widthPixels*0.9);
            params.height = (int) (dm.heightPixels*0.85);
            win.setAttributes(params);
            view.setMinimumHeight(dm.heightPixels);
            view.setMinimumHeight(dm.heightPixels);
        }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(DetailNoteEvent detailNoteEvent) {
        notes = detailNoteEvent.get();
        position = detailNoteEvent.getPosition();
        reloadView();
    }

    private void reloadView() {

        // 默认情况下，dialog布局中设置EditText，在点击EditText后输入法不能弹出来, 将此标志位清除，则可以显示输入法
        this.getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        viewSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                View view = LayoutInflater.from(context).inflate(R.layout.fragment_note_detail, null);
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
        hideModifyView(detailViewHolder);
        WebViewUtil.loadWebContent(detailViewHolder.displayWebView, notes.get(position));
        detailViewHolder.titleEditText.setText(notes.get(position).getTitle());
        detailViewHolder.contentEditText.setText(notes.get(position).getContent());

        String pageCount = String.format("%s/%s", position + 1, notes.size());
        pageCountTextView.setText(pageCount);
    }

    private void initDetailEvent(final DetailViewHolder detailViewHolder) {
        //DialogButton
        ((AlertDialog) getDialog()).getButton(-1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isModifyViewShown(detailViewHolder)) {
                    EventBus.getDefault().post(new UpdateNoteEvent(generateModifiedNote(detailViewHolder)));
                    hideModifyView(detailViewHolder);
                    dismiss();
                } else {
                    dismiss();
                }
//                dismiss();

            }
        });
        ((AlertDialog) getDialog()).getButton(-2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isModifyViewShown(detailViewHolder)) {
                    WebViewUtil.loadWebContent(detailViewHolder.displayWebView, notes.get(position));
                    detailViewHolder.titleEditText.setText(notes.get(position).getTitle());
                    detailViewHolder.contentEditText.setText(notes.get(position).getContent());
                    hideModifyView(detailViewHolder);
                    ((AlertDialog) getDialog()).getButton(-3).setText("Modify");
                } else {
                    dismiss();
                }
//                dismiss();
            }
        });
        ((AlertDialog) getDialog()).getButton(-3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isModifyViewShown(detailViewHolder)) {
                    hideModifyView(detailViewHolder);
                    ((AlertDialog) getDialog()).getButton(-3).setText("Modify");
                } else {
                    showModifyView(detailViewHolder);
                    ((AlertDialog) getDialog()).getButton(-3).setText("Hide");
                }
//                showModifyView(detailViewHolder);
            }
        });

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
//                showModifyView(detailViewHolder);
            }

            @Override
            protected void onSlideDown() {
//                hideModifyView(detailViewHolder);
            }

            @Override
            protected void onDoubleClick(MotionEvent e) {
                if (isModifyViewShown(detailViewHolder)) {
                    hideModifyView(detailViewHolder);
                } else {
                    showModifyView(detailViewHolder);
                }
            }
        });
        detailViewHolder.modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModifyView(detailViewHolder);
            }
        });
        detailViewHolder.modifyConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new UpdateNoteEvent(generateModifiedNote(detailViewHolder)));
                hideModifyView(detailViewHolder);
            }
        });
        detailViewHolder.modifyCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewUtil.loadWebContent(detailViewHolder.displayWebView, notes.get(position));
                hideModifyView(detailViewHolder);
            }
        });
        //与webview同步显示
        detailViewHolder.contentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Note note = ObjectUtils.clone(notes.get(position));
                note.setContent(s.toString());
                WebViewUtil.loadWebContent(detailViewHolder.displayWebView, note);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        detailViewHolder.titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Note note = ObjectUtils.clone(notes.get(position));
                note.setTitle(s.toString());
                WebViewUtil.loadWebContent(detailViewHolder.displayWebView, note);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void next() {
        if (position == notes.size() - 1) {
            msg("已到达最后一页");
        }
        if (position < notes.size() - 1) {
            position++;
            viewSwitcher.setInAnimation(context, R.anim.slide_in_right);
            viewSwitcher.setOutAnimation(context, R.anim.slide_out_left);
            initDetailData(new DetailViewHolder(viewSwitcher.getNextView()));
            viewSwitcher.showNext();
        }
    }

    private void prev() {
        if (position == 0) {
            msg("已到达第一页");
        }
        if (position > 0) {
            position--;
            viewSwitcher.setInAnimation(context, R.anim.slide_in_left);
            viewSwitcher.setOutAnimation(context, R.anim.slide_out_right);
            initDetailData(new DetailViewHolder(viewSwitcher.getNextView()));
            viewSwitcher.showPrevious();
        }
    }

    private boolean isModifyViewShown(DetailViewHolder detailViewHolder) {
        return detailViewHolder.modifyContainer.getVisibility() == View.VISIBLE;
    }

    private void hideModifyView(DetailViewHolder detailViewHolder) {
        detailViewHolder.modifyContainer.setVisibility(View.GONE);
        detailViewHolder.modifyButton.setVisibility(View.GONE);//隐藏modify
    }

    private void showModifyView(DetailViewHolder detailViewHolder) {
        detailViewHolder.modifyContainer.setVisibility(View.VISIBLE);
        detailViewHolder.modifyConfirmButton.setVisibility(View.GONE);//隐藏ok
        detailViewHolder.modifyCancelButton.setVisibility(View.GONE);//隐藏cancel
        detailViewHolder.modifyButton.setVisibility(View.GONE);//隐藏modify

//        NoteModifyFragment noteModifyFragment = new NoteModifyFragment();
//        noteModifyFragment.show(getActivity().getSupportFragmentManager(), "modifyDialog");
//        EventBus.getDefault().postSticky(new FillNoteModifyEvent(notes.get(position)));
    }

    private Note generateModifiedNote(DetailViewHolder detailViewHolder) {
        Note oldNote = notes.get(position);
        String title = detailViewHolder.titleEditText.getText().toString();
        String content = detailViewHolder.contentEditText.getText().toString();
        oldNote.setTitle(title);
        oldNote.setContent(content);
        oldNote.setLastModifyTime(new Date());
        return oldNote;
    }

    class DetailViewHolder {
        ViewGroup displayContainer;
        WebView displayWebView;
        Button modifyButton;
        ViewGroup modifyContainer;
        EditText titleEditText;
        EditText contentEditText;
        Button modifyConfirmButton;
        Button modifyCancelButton;

        public DetailViewHolder(View view) {
            displayContainer = view.findViewById(R.id.fragment_document_display_container);
            displayWebView = view.findViewById(R.id.fragment_document_display_web);
            modifyButton = view.findViewById(R.id.fragment_document_modify_button);
            modifyContainer = view.findViewById(R.id.fragment_document_modify_container);
            titleEditText = view.findViewById(R.id.fragment_document_modify_title);
            contentEditText = view.findViewById(R.id.fragment_document_modify_content);
            modifyConfirmButton = view.findViewById(R.id.fragment_document_modify_confirm);
            modifyCancelButton = view.findViewById(R.id.fragment_document_modify_cancel);
        }
    }

}
