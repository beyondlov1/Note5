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
import android.widget.EditText;
import android.widget.TextView;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Reminder;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.constant.DocumentConst;
import com.beyond.note5.event.AddTodoEvent;
import com.beyond.note5.event.HideKeyBoardEvent2;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.module.DaggerPredictComponent;
import com.beyond.note5.module.PredictComponent;
import com.beyond.note5.module.PredictModule;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.presenter.PredictPresenter;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.utils.TimeNLPUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.PredictView;
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

@SuppressWarnings({"Duplicates", "WeakerAccess"})
public class TodoEditFragment extends DialogFragment implements PredictView {

    private static int dialogHeightWithSoftInputMethod;
    private static final String DIALOG_HEIGHT_WITH_SOFT_INPUT_METHOD = "dialogHeightWithSoftInputMethod";

    protected View root;
    protected EditText contentEditText;
    private DialogButton neutralButton;
    private TagFlowLayout flowLayout;
    private TagAdapter<String> tagAdapter;

    private List<String> tagData = new ArrayList<>();
    private Handler handler = new Handler();

    protected Todo createdDocument = new Todo();

    @Inject
    PredictPresenter predictPresenter;

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
        PredictComponent predictComponent = DaggerPredictComponent.builder().predictModule(new PredictModule(this)).build();
        predictComponent.inject(this);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_todo_edit, null);
        builder.setView(root)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String content = contentEditText.getText().toString();
                                sendEventsOnOKClick(content);
                                dialog.dismiss();
                                InputMethodUtil.hideKeyboard(contentEditText);
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
        dialog.getWindow().setStatusBarColor(ContextCompat.getColor(this.getContext(),R.color.white));
        dialog.getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
    }

    protected void sendEventsOnOKClick(String content) {
        if (StringUtils.isBlank(content)){
            return;
        }
        Todo todo = new Todo();
        todo.setId(IDUtil.uuid());
        todo.setTitle(content.length() > 10 ? content.substring(0, 10) : content);
        todo.setContent(content);
        todo.setCreateTime(new Date());
        todo.setVersion(0);
        todo.setLastModifyTime(new Date());
        todo.setReadFlag(DocumentConst.READ_FLAG_NORMAL);

        Date reminderStart = TimeNLPUtil.parse(todo.getContent());
        if (reminderStart!=null){
            Reminder reminder = new Reminder();
            reminder.setId(IDUtil.uuid());
            reminder.setStart(reminderStart);
            todo.setReminder(reminder);
            todo.setReminderId(reminder.getId());
        }

        EventBus.getDefault().post(new AddTodoEvent(todo));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        initView(root);
        initDialogAnimation();
        initEvent();
        return root;
    }

    private void initView(View view) {
        contentEditText = view.findViewById(R.id.fragment_edit_todo_content);
        ViewStub tagsContainer = view.findViewById(R.id.fragment_edit_todo_tag_view_stub);
        tagsContainer.inflate();
        flowLayout = view.findViewById(R.id.fragment_edit_todo_tags);
        tagAdapter = new TagAdapter<String>(tagData) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                TextView tv = new TextView(parent.getContext());
                tv.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.white));
                tv.setBackground(parent.getResources().getDrawable(R.drawable.radius_24dp_blue,null));
                tv.setText(s);
                return tv;
            }
        };
        flowLayout.setAdapter(tagAdapter);
    }

    @SuppressWarnings("ConstantConditions")
    private void initDialogAnimation() {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setWindowAnimations(R.style.edit_dialog_animation);
    }

    private void initEvent() {
        TimeExpressionDetectiveTextWatcher.Builder builder = new TimeExpressionDetectiveTextWatcher.Builder(contentEditText);
        contentEditText.addTextChangedListener(builder.handler(handler).build());
        if (contentEditText instanceof SelectionListenableEditText) {
            ((SelectionListenableEditText) contentEditText).setOnSelectionChanged(new SelectionListenableEditText.OnSelectionChangeListener() {

                @Override
                public void onChanged(String content, int selStart, int selEnd) {
                    if (content.length() >= selStart) {
                        predictPresenter.predict(content.substring(0, selStart));
                    }
                }
            });
        }
        // 刚打开时预测
        contentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                predictPresenter.predict(contentEditText.getText().toString());
            }
        });
        flowLayout.setOnTagClickListener(new OnTagClickToAppendListener(contentEditText));

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        initDialogSize();
        if(neutralButton!=null){
            ((AlertDialog) getDialog()).getButton(-3).setOnClickListener(neutralButton.getOnClickListener());
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
        if (dialogHeightWithSoftInputMethod == 0) {
            dialogHeightWithSoftInputMethod = dm.heightPixels - y - 50;
            SharedPreferences.Editor editor = getActivity().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
            editor.putInt(DIALOG_HEIGHT_WITH_SOFT_INPUT_METHOD, dm.heightPixels - y - 50);
            editor.apply();
        }
        params.height = dialogHeightWithSoftInputMethod +75;//因为改写了edit的通知栏，所以要加上通知栏的高度 //FIXME: Pixel模拟会不包括这个75,不知道为什么
        win.setAttributes(params);

        contentEditText.setMinimumHeight(dm.heightPixels);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(HideKeyBoardEvent2 event) {
        if (Document.TODO.equals(event.getType())){
            dismiss();
        }
    }

    @Override
    public void onPredictSuccess(final List<Tag> data, final String source) {
        handler.post(new Runnable() {
            @SuppressWarnings("Duplicates")
            @Override
            public void run() {
                if (data == null||data.isEmpty()){
                    predictPresenter.predict(null);
                    return;
                }
                List<Tag> finalTags = data;
                tagData.clear();
                if (StringUtils.isBlank(source)){
                    Iterator<Tag> iterator = finalTags.iterator();
                    while (iterator.hasNext()) {
                        Tag tag = iterator.next();
                        if (!tag.isFirst()){
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
        });
    }

    @Override
    public void onPredictFail() {
        ToastUtil.toast(this.getContext(), "预测失败");
    }

    @Override
    public void onTrainSuccess() {
        //do nothing
    }

    @Override
    public void onTrainFail() {
        ToastUtil.toast(this.getContext(), "网络状况不佳");
    }
}
