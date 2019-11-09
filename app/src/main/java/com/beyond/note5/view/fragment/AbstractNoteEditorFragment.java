package com.beyond.note5.view.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.event.HideKeyBoardEvent2;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.event.SpeechBeginEvent;
import com.beyond.note5.event.SpeechEndEvent;
import com.beyond.note5.event.SpeechRecognizedEvent;
import com.beyond.note5.speech.SpeechService;
import com.beyond.note5.speech.XfSpeechServiceImpl;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.utils.StatusBarUtil;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.custom.MarkdownAutoRenderEditText;
import com.beyond.note5.view.listener.OnClickToInsertBeforeLineListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

import static com.beyond.note5.speech.XfSpeechServiceImpl.XF_APP_ID;


/**
 * @author: beyond
 * @date: 2019/1/30
 */

public abstract class AbstractNoteEditorFragment extends AbstractDocumentEditorFragment<Note> {

    @BindView(R.id.fragment_edit_note_view_stub)
    protected ViewStub editorToolViewStub;
    @BindView(R.id.fragment_edit_note_web)
    protected WebView displayWebView;
    @BindView(R.id.fragment_edit_note_content)
    protected EditText editorContent;
    @BindView(R.id.fragment_edit_note_container)
    protected LinearLayout editorContainer;

    protected ImageButton clearButton;
    protected ImageButton saveButton;
    protected ImageButton speechButton;

    private SpeechService speechService;

    @Override
    public Dialog createDialogInternal(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root)
                .setPositiveButton("OK",
                        (dialog, id) -> {
                            String content = getEditorContent();
                            if (content.length() > 0) {
                                save(content);
                            }
                            dialog.dismiss();
                        }).setNegativeButton("Cancel", null);
        AlertDialog alertDialog = builder.create();
        processStatusBarColor(alertDialog);
        return alertDialog;
    }

    @NonNull
    private String getEditorContent() {
        if (editorContent instanceof MarkdownAutoRenderEditText) {
            return ((MarkdownAutoRenderEditText) editorContent).getRealContent();
        }
        return editorContent.getText().toString();
    }

    private void processStatusBarColor(AlertDialog dialog) {
        StatusBarUtil.showWhiteStatusBarForDialog(getActivity(), dialog);
    }

    @Override
    protected void initCommonView() {

        editorContent = root.findViewById(R.id.fragment_edit_note_content);
        displayWebView = root.findViewById(R.id.fragment_edit_note_web);

        TextView markdownToolHead = root.findViewById(R.id.keyboard_top_tool_head);
        TextView markdownToolHead3 = root.findViewById(R.id.keyboard_top_tool_head_3);
        TextView markdownToolList = root.findViewById(R.id.keyboard_top_tool_list);
        TextView markdownToolEnterList = root.findViewById(R.id.keyboard_top_tool_enter_list);
        TextView markdownToolOrderList = root.findViewById(R.id.keyboard_top_tool_order_list);
        TextView markdownToolLine = root.findViewById(R.id.keyboard_top_tool_line);
        TextView markdownToolBracketsLeft = root.findViewById(R.id.keyboard_top_tool_brackets_left);
        TextView markdownToolBracketsRight = root.findViewById(R.id.keyboard_top_tool_brackets_right);
        TextView markdownToolStrike = root.findViewById(R.id.keyboard_top_tool_strike);
        View markdownToolContainer = root.findViewById(R.id.keyboard_top_tool_tip_container);

        OnMarkdownToolItemClickListener onMarkdownToolItemClickListener = new OnMarkdownToolItemClickListener(editorContent);
        OnClickToInsertBeforeLineListener onClickToInsertBeforeLineListener = new OnClickToInsertBeforeLineListener(editorContent);

        markdownToolHead.setOnClickListener(onClickToInsertBeforeLineListener);
        markdownToolHead3.setOnClickListener(onClickToInsertBeforeLineListener);
        markdownToolList.setOnClickListener(onClickToInsertBeforeLineListener);
        markdownToolOrderList.setOnClickListener(onClickToInsertBeforeLineListener);

        markdownToolEnterList.setOnClickListener(onMarkdownToolItemClickListener);
        markdownToolLine.setOnClickListener(onMarkdownToolItemClickListener);
        markdownToolBracketsLeft.setOnClickListener(onMarkdownToolItemClickListener);
        markdownToolBracketsRight.setOnClickListener(onMarkdownToolItemClickListener);
        markdownToolStrike.setOnClickListener(new OnMarkdownToolStrikeClickListener(editorContent));

    }

    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    protected void initCommonEvent() {
        displayWebView.getSettings().setJavaScriptEnabled(true);
        displayWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        displayWebView.scrollTo(0, 100000);
        editorContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                creatingDocument.setContent(s.toString());
                WebViewUtil.loadWebContent(displayWebView, creatingDocument);
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
    protected void initDialogView() {
        super.initDialogView();
        initDialogAnimation();
    }

    @SuppressWarnings("ConstantConditions")
    private void initDialogAnimation() {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setWindowAnimations(R.style.edit_dialog_animation);
    }

    @Override
    protected void initFragmentView() {
        super.initFragmentView();

        initSpeech();

        editorContainer.setBackgroundColor(Color.WHITE);
        editorContainer.setBackgroundResource(R.drawable.corners_5dp);
        editorContainer.setPadding(5, 5, 5, 5);
        editorToolViewStub.inflate();
        clearButton = root.findViewById(R.id.fragment_edit_note_clear);
        View convertButton = root.findViewById(R.id.fragment_edit_note_to_note);
        convertButton.setVisibility(View.GONE);
        View browserSearchButton = root.findViewById(R.id.fragment_edit_note_browser_search);
        browserSearchButton.setVisibility(View.GONE);
        saveButton = root.findViewById(R.id.fragment_edit_note_save);
        speechButton = root.findViewById(R.id.fragment_edit_note_speech);
        InputMethodUtil.showKeyboard(editorContent);
    }

    private void initSpeech() {
        SpeechUtility.createUtility(getContext(), SpeechConstant.APPID + "=" + XF_APP_ID);
        speechService = new XfSpeechServiceImpl(SpeechRecognizer.createRecognizer(getContext(), null));
    }

    @Override
    protected void initFragmentEvent() {
        super.initFragmentEvent();
        clearButton.setOnClickListener(v -> editorContent.setText(null));
        saveButton.setOnClickListener(v -> {
            String content = getEditorContent();
            if (StringUtils.isNotBlank(content)) {
                save(content);
            }
            InputMethodUtil.hideKeyboard(editorContent);
        });
        speechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard_voice_green_600_24dp,null));
                speechService.speak(getContext(), new SpeechService.SpeakListener() {
                    @Override
                    public void onRecognized(String result) {
                        appendToContent(result);
                    }
                });
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onSpeechRecognized(SpeechRecognizedEvent event) {
        appendToContent(event.get());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSpeechRecognized(SpeechBeginEvent event) {
        speechButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard_voice_green_600_24dp,null));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSpeechRecognized(SpeechEndEvent event) {
        speechButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard_voice_black_24dp,null));
    }

    private void appendToContent(String s) {
        editorContent.append(s);
    }

    @Override
    protected int getDialogLayoutResId() {
        return R.layout.fragment_note_edit;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_note_edit;
    }

    @Override
    protected void onDialogStartInternal() {
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
        editorContent.setMinimumHeight(dm.heightPixels);
        InputMethodUtil.showKeyboard(editorContent);
    }

    @Override
    protected void onDialogShowKeyboard(ShowKeyBoardEvent event) {
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
        if (dialogHeightWithSoftInputMethod == 0) {
            dialogHeightWithSoftInputMethod = dm.heightPixels - y - 50;
            InputMethodUtil.rememberDialogHeightWithSoftInputMethod(dm.heightPixels - y - 50);
        }
        params.height = dialogHeightWithSoftInputMethod + 75;//因为改写了edit的通知栏，所以要加上通知栏的高度
        win.setAttributes(params);

        displayWebView.setMinimumHeight(dm.heightPixels);
        editorContent.setMinimumHeight(dm.heightPixels);
    }

    @Override
    protected void onDialogHideKeyboard(HideKeyBoardEvent2 event) {
        dismiss();
    }

    class OnMarkdownToolItemClickListener implements View.OnClickListener {

        private EditText editText;

        OnMarkdownToolItemClickListener(EditText editText) {
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

        OnMarkdownToolStrikeClickListener(EditText editText) {
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
