package com.beyond.note5.view.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
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
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

import com.beyond.note5.R;
import com.beyond.note5.bean.Document;
import com.beyond.note5.event.Event;
import com.beyond.note5.event.HideKeyBoardEvent2;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.custom.DialogButton;
import com.beyond.note5.view.listener.OnClickToInsertBeforeLineListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;


/**
 * @author: beyond
 * @date: 2019/1/30
 */

public abstract class AbstractDocumentEditFragment<T extends Document> extends DialogFragment {

    protected View root;
    protected EditText contentEditText;
    protected WebView displayWebView;

    protected T createdDocument;

    public AbstractDocumentEditFragment() {
        createdDocument = initCreatedDocument();
    }

    protected abstract T initCreatedDocument();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("InflateParams")
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
                                    sendEventsOnOKClick(content);
                                }
                                dialog.dismiss();
                            }
                        }).setNegativeButton("Cancel", null);
        DialogButton neutralButton = getNeutralButton();
        if (neutralButton != null) {
            builder.setNeutralButton(neutralButton.getName(), null);
        }
        AlertDialog alertDialog = builder.create();
        processStatusBarColor(alertDialog);
        return alertDialog;
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void processStatusBarColor(AlertDialog dialog){
        Objects.requireNonNull(dialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        dialog.getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        dialog.getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
    }

    protected abstract void sendEventsOnOKClick(String content);

    public void post(Event event) {
        EventBus.getDefault().post(event);
    }

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
        TextView markdownToolStrike = view.findViewById(R.id.keyboard_top_tool_strike);
        View markdownToolContainer = view.findViewById(R.id.keyboard_top_tool_tip_container);

        OnMarkdownToolItemClickListener onMarkdownToolItemClickListener = new OnMarkdownToolItemClickListener(contentEditText);
        OnClickToInsertBeforeLineListener onClickToInsertBeforeLineListener = new OnClickToInsertBeforeLineListener(contentEditText);

        markdownToolHead.setOnClickListener(onClickToInsertBeforeLineListener);
        markdownToolHead3.setOnClickListener(onClickToInsertBeforeLineListener);
        markdownToolList.setOnClickListener(onClickToInsertBeforeLineListener);

        markdownToolEnterList.setOnClickListener(onMarkdownToolItemClickListener);
        markdownToolLine.setOnClickListener(onMarkdownToolItemClickListener);
        markdownToolBracketsLeft.setOnClickListener(onMarkdownToolItemClickListener);
        markdownToolBracketsRight.setOnClickListener(onMarkdownToolItemClickListener);
        markdownToolStrike.setOnClickListener(new OnMarkdownToolStrikeClickListener(contentEditText));
    }

    @SuppressWarnings("ConstantConditions")
    private void initDialogAnimation() {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setWindowAnimations(R.style.edit_dialog_animation);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initEvent() {
        displayWebView.getSettings().setJavaScriptEnabled(true);
        displayWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        displayWebView.scrollTo(0, 100000);
        contentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                createdDocument.setContent(s.toString());
                WebViewUtil.loadWebContent(displayWebView, createdDocument);
                final CharSequence target = s.subSequence(start + count, s.length()).toString()
                        .replaceAll("\\p{Punct}*", "")
                        .replaceAll("\\s*|\n|\t|\r", "");
                ScrollWebViewClient scrollWebViewClient = ScrollWebViewClient.getInstance();
                scrollWebViewClient.setTarget(target);
                scrollWebViewClient.setInputLengthEachTime(count);
                displayWebView.setWebViewClient(scrollWebViewClient);
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
        // 一定要设置Background，如果不设置，window属性设置无效
        assert win != null;
        win.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.TOP;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = InputMethodUtil.getDialogHeightWithSoftInputMethod();
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
        adjustDialogSize(y);
    }

    private void adjustDialogSize(Integer y) {
        Window win = getDialog().getWindow();
        // 一定要设置Background，如果不设置，window属性设置无效
        assert win != null;
        win.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.TOP;
        params.width = dm.widthPixels;
        //设置初始的dialogHeightWithSoftInputMethod, 为了不让开始的时候动画跳一下
        int dialogHeightWithSoftInputMethod = InputMethodUtil.getDialogHeightWithSoftInputMethod();
        if ( dialogHeightWithSoftInputMethod== 0) {
            dialogHeightWithSoftInputMethod = dm.heightPixels - y -50;
            InputMethodUtil.rememberDialogHeightWithSoftInputMethod(dm.heightPixels - y -50);
        }
        params.height = dialogHeightWithSoftInputMethod+75;//因为改写了edit的通知栏，所以要加上通知栏的高度
        win.setAttributes(params);

        displayWebView.setMinimumHeight(dm.heightPixels);
        contentEditText.setMinimumHeight(dm.heightPixels);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(HideKeyBoardEvent2 event) {
        dismiss();
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

                if (textView.getId() == R.id.keyboard_top_tool_strike) {
                    int caretPosition = editText.getSelectionEnd();
                    String editText = this.editText.getText().toString();
                    if (isLineHasStrike(caretPosition, editText)) {
                        editText = removeStrikeTagForLineAt(caretPosition, editText);
                    } else {
                        editText = insertStrikeTagForLineAt(caretPosition, editText);
                    }
                    this.editText.setText(editText);
                }
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

        private boolean isLineHasStrike(int caretPosition, String text) {
            char[] chars = (text + "\n").toCharArray();
            int start = getLineStart(caretPosition, chars);
            int end = getLineEnd(caretPosition, chars);
            String substring = String.valueOf(chars).substring(start, end + 1);
            System.out.println(substring);
            return substring.contains(" <strike>") && substring.contains("</strike>");
        }

        private int getLineStart(int caretPosition, char[] chars) {
            int start = caretPosition;
            while (chars[start] != '-') {
                if (start > 0) {
                    start--;
                } else {
                    break;
                }
            }
            start++;
            return start;
        }

        private int getLineEnd(int caretPosition, char[] chars) {
            int end = caretPosition;
            while (chars[end] != '\n') {
                if (end < chars.length - 1) {
                    end++;
                } else {
                    break;
                }
            }
            end--;
            return end;
        }

        private String removeStrikeTagForLineAt(int caretPosition, String text) {
            StringBuilder result = new StringBuilder();
            char[] chars = (text + "\n").toCharArray();

            int start = getLineStart(caretPosition, chars);
            int end = getLineEnd(caretPosition, chars);

            String substring = String.valueOf(chars).substring(start, end + 1);
            if (substring.contains(" <strike>") && substring.contains("</strike>")) {
                substring = substring.replace(" <strike>", "");
                substring = substring.replace("</strike>", "");
            }

            result.append(text, 0, start);
            result.append(substring);
            result.append(text, end + 1, text.length());

            return result.toString();
        }

        private String insertStrikeTagForLineAt(int caretPosition, String text) {
            StringBuilder result = new StringBuilder();
            char[] chars = (text + "\n").toCharArray();

            int start = getLineStart(caretPosition, chars);
            int end = getLineEnd(caretPosition, chars);

            int index = 0;
            for (char aChar : chars) {
                if (index == start) {
                    result.append(" <strike>");
                }
                result.append(aChar);
                if (index == end) {
                    result.append("</strike>");
                }
                index++;
            }

            return result.substring(0, result.length() - 1);
        }
    }

    class OnMarkdownToolStrikeClickListener implements View.OnClickListener {

        private EditText editText;

        public OnMarkdownToolStrikeClickListener(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.keyboard_top_tool_strike) {
                int caretPosition = editText.getSelectionEnd();
                String editText = this.editText.getText().toString();
                if (isLineHasStrike(caretPosition, editText)) {
                    editText = removeStrikeTagForLineAt(caretPosition, editText);
                } else {
                    editText = insertStrikeTagForLineAt(caretPosition, editText);
                }
                this.editText.setText(editText);
            }
        }

        private boolean isLineHasStrike(int caretPosition, String text) {
            char[] chars = (text + "\n").toCharArray();
            int start = getLineStart(caretPosition, chars);
            int end = getLineEnd(caretPosition, chars);
            String substring = String.valueOf(chars).substring(start, end + 1);
            System.out.println(substring);
            return substring.contains(" <strike>") && substring.contains("</strike>");
        }

        private int getLineStart(int caretPosition, char[] chars) {
            int start = caretPosition;
            while (chars[start] != '-') {
                if (start > 0) {
                    start--;
                } else {
                    break;
                }
            }
            start++;
            return start;
        }

        private int getLineEnd(int caretPosition, char[] chars) {
            int end = caretPosition;
            while (chars[end] != '\n') {
                if (end < chars.length - 1) {
                    end++;
                } else {
                    break;
                }
            }
            end--;
            return end;
        }

        private String removeStrikeTagForLineAt(int caretPosition, String text) {
            StringBuilder result = new StringBuilder();
            char[] chars = (text + "\n").toCharArray();

            int start = getLineStart(caretPosition, chars);
            int end = getLineEnd(caretPosition, chars);

            String substring = String.valueOf(chars).substring(start, end + 1);
            if (substring.contains(" <strike>") && substring.contains("</strike>")) {
                substring = substring.replace(" <strike>", "");
                substring = substring.replace("</strike>", "");
            }

            result.append(text, 0, start);
            result.append(substring);
            result.append(text, end + 1, text.length());

            return result.toString();
        }

        private String insertStrikeTagForLineAt(int caretPosition, String text) {
            StringBuilder result = new StringBuilder();
            char[] chars = (text + "\n").toCharArray();

            int start = getLineStart(caretPosition, chars);
            int end = getLineEnd(caretPosition, chars);

            int index = 0;
            for (char aChar : chars) {
                if (index == start) {
                    result.append(" <strike>");
                }
                result.append(aChar);
                if (index == end) {
                    result.append("</strike>");
                }
                index++;
            }

            return result.substring(0, result.length() - 1);
        }
    }

    static class ScrollWebViewClient extends WebViewClient {

        private CharSequence target;
        private int inputLengthEachTime;

        private ScrollWebViewClient() {
        }

        static ScrollWebViewClient getInstance() {
            return WebViewClientHolder.instance;
        }

        private static class WebViewClientHolder {
            private static final ScrollWebViewClient instance = new ScrollWebViewClient();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
        }

        @Override
        public void onPageFinished(final WebView view, String url) {
            super.onPageFinished(view, url);
            view.evaluateJavascript("javascript:searchAndScrollTo('" + target + "','" + inputLengthEachTime + "')", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                }
            });
        }

        public CharSequence getTarget() {
            return target;
        }

        void setTarget(CharSequence target) {
            this.target = target;
        }

        public int getInputLengthEachTime() {
            return inputLengthEachTime;
        }

        void setInputLengthEachTime(int inputLengthEachTime) {
            this.inputLengthEachTime = inputLengthEachTime;
        }
    }

}
