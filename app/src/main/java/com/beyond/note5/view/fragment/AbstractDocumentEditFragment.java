package com.beyond.note5.view.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import com.beyond.note5.R;
import com.beyond.note5.bean.Document;
import com.beyond.note5.event.Event;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.utils.WebViewUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * Created by beyond on 2019/1/30.
 */

public abstract class AbstractDocumentEditFragment<T extends Document> extends DialogFragment {

    private static int dialogHeightWithSoftInputMethod;

    protected View root;
    protected EditText contentEditText;
    protected WebView displayWebView;
    protected T createdDocument;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_note_edit, null);
        builder.setView(root)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String content = contentEditText.getText().toString();
                                if (content.length() > 0) {
                                    EventBus.getDefault().post(onPositiveButtonClick(content));
                                }
                                dialog.dismiss();
                            }
                        }).setNegativeButton("Cancel", null);
        DialogButton neutralButton = getNeutralButton();
        if (neutralButton != null) {
            builder.setNeutralButton(neutralButton.name, neutralButton.onClickListener);
        }
        return builder.create();
    }

    protected abstract Event onPositiveButtonClick(String content);

    protected DialogButton getNeutralButton() {
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        root =  inflater.inflate(R.layout.fragment_note_edit, container, false);
        initView(root);
        initDialogAnimation();
        initEvent();
        return root;
    }

    private void initView(View view) {
        contentEditText = view.findViewById(R.id.fragment_edit_document_content);
        displayWebView = view.findViewById(R.id.fragment_edit_document_web);

        TextView markdownToolHead = view.findViewById(R.id.keyboard_top_tool_head);
        TextView markdownToolHead3 = view.findViewById(R.id.keyboard_top_tool_head_3);
        TextView markdownToolList = view.findViewById(R.id.keyboard_top_tool_list);
        TextView markdownToolEnterList = view.findViewById(R.id.keyboard_top_tool_enter_list);
        TextView markdownToolLine = view.findViewById(R.id.keyboard_top_tool_line);
        TextView markdownToolBracketsLeft = view.findViewById(R.id.keyboard_top_tool_brackets_left);
        TextView markdownToolBracketsRight = view.findViewById(R.id.keyboard_top_tool_brackets_right);
        View markdownToolContainer = view.findViewById(R.id.keyboard_top_tool_tip_container);

        OnMarkdownToolItemClickListener onMarkdownToolItemClickListener = new OnMarkdownToolItemClickListener(contentEditText);
        markdownToolHead.setOnClickListener(onMarkdownToolItemClickListener);
        markdownToolHead3.setOnClickListener(onMarkdownToolItemClickListener);
        markdownToolList.setOnClickListener(onMarkdownToolItemClickListener);
        markdownToolEnterList.setOnClickListener(onMarkdownToolItemClickListener);
        markdownToolLine.setOnClickListener(onMarkdownToolItemClickListener);
        markdownToolBracketsLeft.setOnClickListener(onMarkdownToolItemClickListener);
        markdownToolBracketsRight.setOnClickListener(onMarkdownToolItemClickListener);
    }

    private void initDialogAnimation() {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setWindowAnimations(R.style.edit_dialog_animation);
    }

    private void initEvent() {
        contentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                createdDocument.setContent(s.toString());
                WebViewUtil.loadWebContent(displayWebView, createdDocument);
                displayWebView.scrollTo(0, 1000);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        initDialogSize();
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
        params.gravity = Gravity.TOP;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = dialogHeightWithSoftInputMethod;
        win.setAttributes(params);
        displayWebView.setMinimumHeight(dm.heightPixels);
        contentEditText.setMinimumHeight(dm.heightPixels);
        InputMethodUtil.showKeyboard(contentEditText);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ShowKeyBoardEvent event) {
        Integer y = event.get();
        Window win = getDialog().getWindow();
        // 一定要设置Background，如果不设置，window属性设置无效
        win.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.TOP;
        params.width = dm.widthPixels;
        params.height = dm.heightPixels - y - 50;
        win.setAttributes(params);

        displayWebView.setMinimumHeight(dm.heightPixels);
        contentEditText.setMinimumHeight(dm.heightPixels);

        //设置初始的dialogHeightWithSoftInputMethod, 为了不让开始的时候动画跳一下
        dialogHeightWithSoftInputMethod = dm.heightPixels - y - 50;
    }

    class OnMarkdownToolItemClickListener implements View.OnClickListener {

        private EditText editText;

        public OnMarkdownToolItemClickListener(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void onClick(View v) {
            if (v instanceof TextView) {
                TextView textView = (TextView) v;
                CharSequence text = textView.getText();
                appendToEditText(editText, text);
            }
        }

        private void appendToEditText(EditText editText, CharSequence source) {
            int start = editText.getSelectionStart();
            int end = editText.getSelectionEnd();
            Editable edit = editText.getEditableText();//获取EditText的文字
            if (start < 0 || start >= edit.length()) {
                edit.append(source);
            } else {
                edit.replace(start, end, source);//光标所在位置插入文字
            }
        }

    }

    class DialogButton {
        private String name;
        private DialogInterface.OnClickListener onClickListener;

        public DialogButton(String name, DialogInterface.OnClickListener onClickListener) {
            this.name = name;
            this.onClickListener = onClickListener;
        }
    }

}
