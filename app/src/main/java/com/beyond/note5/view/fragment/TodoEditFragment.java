package com.beyond.note5.view.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.HideKeyBoardEvent2;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.presenter.CalendarPresenterImpl;
import com.beyond.note5.presenter.PredictPresenterImpl;
import com.beyond.note5.presenter.TodoCompositePresenter;
import com.beyond.note5.presenter.TodoCompositePresenterImpl;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.adapter.view.CalendarViewAdapter;
import com.beyond.note5.view.adapter.view.PredictViewAdapter;
import com.beyond.note5.view.adapter.view.TodoViewAdapter;
import com.beyond.note5.view.custom.DialogButton;
import com.beyond.note5.view.custom.SelectionListenableEditText;
import com.beyond.note5.view.listener.OnTagClickToAppendListener;
import com.beyond.note5.view.listener.TimeExpressionDetectiveTextWatcher;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

@SuppressWarnings({"Duplicates", "WeakerAccess"})
public class TodoEditFragment extends DialogFragment {

    private static int dialogHeightWithSoftInputMethod;
    private static final String DIALOG_HEIGHT_WITH_SOFT_INPUT_METHOD = "dialogHeightWithSoftInputMethod";

    protected View root;

    @BindView(R.id.fragment_edit_todo_content)
    SelectionListenableEditText editorContent;
    @BindView(R.id.fragment_edit_todo_tag_view_stub)
    ViewStub editorTagViewStub;
    @BindView(R.id.fragment_edit_todo_view_stub)
    ViewStub editorToolViewStub;
    @BindView(R.id.fragment_todo_edit_container)
    LinearLayout editorContainer;

    private View clearButton;
    private View convertButton;
    private View browserSearchButton;
    private View saveButton;
    Unbinder unbinder;

    private DialogButton neutralButton;
    private TagFlowLayout flowLayout;
    private TagAdapter<String> tagAdapter;

    private List<String> tagData = new ArrayList<>();
    private Handler handler = new Handler();

    private boolean dialog = false;

    TodoCompositePresenter todoCompositePresenter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化弹出框的位置
        if (dialogHeightWithSoftInputMethod == 0) {
            dialogHeightWithSoftInputMethod =
                    getActivity().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                            .getInt(DIALOG_HEIGHT_WITH_SOFT_INPUT_METHOD, 0);
        }
        initInjection();
    }

    private void initInjection() {

        todoCompositePresenter = new TodoCompositePresenterImpl.Builder(new TodoPresenterImpl(new MyTodoView()))
                .calendarPresenter(new CalendarPresenterImpl(getActivity(), new MyCalendarView()))
                .predictPresenter(new PredictPresenterImpl(new MyPredictView()))
                .build();

    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_todo_edit, null);
        builder.setView(root)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String content = editorContent.getText().toString();
                                saveTodo(content);
                                dialog.dismiss();
                                InputMethodUtil.hideKeyboard(editorContent);
                            }
                        }).setNegativeButton("Cancel", null);
        neutralButton = initNeutralButton();
        if (neutralButton != null) {
            builder.setNeutralButton(neutralButton.getName(), null);
        }
        AlertDialog alertDialog = builder.create();
        processStatusBarColor(alertDialog);
        return alertDialog;
    }

    protected DialogButton initNeutralButton() {
        return null;
    }

    private void processStatusBarColor(AlertDialog dialog) {
        Objects.requireNonNull(dialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        dialog.getWindow().setStatusBarColor(ContextCompat.getColor(this.getContext(), R.color.white));
        dialog.getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
    }

    protected void saveTodo(String content) {
        if (StringUtils.isBlank(content)) {
            return;
        }
        Todo todo = Todo.newTodo(content);
        todoCompositePresenter.add(todo);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_todo_edit, null);
        }
        unbinder = ButterKnife.bind(this, root);
        initView(root);
        if (dialog) {
            initDialogAnimation();
        }
        initEvent();
        return root;
    }

    private void initView(View view) {
        editorTagViewStub.inflate();
        flowLayout = view.findViewById(R.id.fragment_edit_todo_tags);
        tagAdapter = new TagAdapter<String>(tagData) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                TextView tv = new TextView(parent.getContext());
                tv.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.white));
                tv.setBackground(parent.getResources().getDrawable(R.drawable.radius_24dp_blue, null));
                tv.setText(s);
                return tv;
            }
        };
        flowLayout.setAdapter(tagAdapter);

        if (!dialog) {
            editorContainer.setBackgroundColor(Color.WHITE);
            editorContainer.setBackgroundResource(R.drawable.corners_5dp);
            editorContainer.setPadding(5,5,5,0);
            editorToolViewStub.inflate();
            clearButton = view.findViewById(R.id.fragment_edit_todo_clear);
            convertButton = view.findViewById(R.id.fragment_edit_todo_to_note);
            convertButton.setVisibility(View.GONE);
            browserSearchButton = view.findViewById(R.id.fragment_edit_todo_browser_search);
            browserSearchButton.setVisibility(View.GONE);
            saveButton = view.findViewById(R.id.fragment_edit_todo_save);
            InputMethodUtil.showKeyboard(editorContent);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void initDialogAnimation() {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setWindowAnimations(R.style.edit_dialog_animation);
    }

    private void initEvent() {
        TimeExpressionDetectiveTextWatcher.Builder builder = new TimeExpressionDetectiveTextWatcher.Builder(editorContent);
        editorContent.addTextChangedListener(builder.handler(handler).build());
        editorContent.setOnSelectionChanged(new SelectionListenableEditText.OnSelectionChangeListener() {

            @Override
            public void onChanged(String content, int selStart, int selEnd) {
                if (content.length() >= selStart) {
                    todoCompositePresenter.predict(content.substring(0, selStart));
                }
            }
        });
        // 刚打开时预测
        editorContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (editorContent== null ||editorContent.getText() == null){
                    todoCompositePresenter.predict(null);
                    return;
                }
                todoCompositePresenter.predict(editorContent.getText().toString());
            }
        });
        flowLayout.setOnTagClickListener(new OnTagClickToAppendListener(editorContent));

        if (!dialog){
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editorContent.setText(null);
                }
            });
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveTodo(editorContent.getText().toString());
                    InputMethodUtil.hideKeyboard(editorContent);
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if (dialog) {
            initDialogSize();
            if (neutralButton != null) {
                ((AlertDialog) getDialog()).getButton(-3).setOnClickListener(neutralButton.getOnClickListener());
            }
        }
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
        params.height = dialogHeightWithSoftInputMethod;
        win.setAttributes(params);
        editorContent.setMinimumHeight(dm.heightPixels);
        InputMethodUtil.showKeyboard(editorContent);
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
        if (dialogHeightWithSoftInputMethod == 0) {
            dialogHeightWithSoftInputMethod = dm.heightPixels - y - 50;
            SharedPreferences.Editor editor = getActivity().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
            editor.putInt(DIALOG_HEIGHT_WITH_SOFT_INPUT_METHOD, dm.heightPixels - y - 50);
            editor.apply();
        }
        params.height = dialogHeightWithSoftInputMethod + 75;//因为改写了edit的通知栏，所以要加上通知栏的高度 //FIXME: Pixel模拟会不包括这个75,不知道为什么
        win.setAttributes(params);

        editorContent.setMinimumHeight(dm.heightPixels);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(HideKeyBoardEvent2 event) {
        if (Document.TODO.equals(event.getType())) {
            dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private class MyTodoView extends TodoViewAdapter {

        @Override
        public void onAddFail(Todo document) {
            ToastUtil.toast(getContext(), "添加失败");
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
}
