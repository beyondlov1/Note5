package com.beyond.note5.view.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beyond.note5.R;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.view.custom.SelectionListenableEditText;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;


/**
 * 编辑todo的抽象类
 * 主要功能：
 *   1. 调整dialog大小
 *   2. 初始化公共的view
 * @author: beyond
 * @date: 2019/1/30
 */

public abstract class AbstractTodoEditorFragment extends AbstractDocumentEditorFragment<Todo> {

    @BindView(R.id.fragment_edit_todo_content)
    SelectionListenableEditText editorContent;
    @BindView(R.id.fragment_edit_todo_tag_view_stub)
    ViewStub editorTagViewStub;
    @BindView(R.id.fragment_edit_todo_view_stub)
    ViewStub editorToolViewStub;
    @BindView(R.id.fragment_todo_edit_container)
    LinearLayout editorContainer;

    TagFlowLayout flowLayout;

    ImageButton clearButton;
    ImageButton convertButton;
    ImageButton browserSearchButton;
    ImageButton saveButton;

    protected TagAdapter<String> tagAdapter;
    protected List<String> tagData = new ArrayList<>();

    protected Handler handler = new Handler();

    @Override
    protected Dialog createDialogInternal(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String content = editorContent.getText().toString();
                                creatingDocument.setContent(content);
                                onOKClick();
                                dialog.dismiss();
                                InputMethodUtil.hideKeyboard(editorContent);
                            }
                        }).setNegativeButton("Cancel", null);
        AlertDialog alertDialog = builder.create();
        processStatusBarColor(alertDialog);
        return alertDialog;
    }

    private void processStatusBarColor(AlertDialog dialog) {
        Objects.requireNonNull(dialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        dialog.getWindow().setStatusBarColor(ContextCompat.getColor(this.getContext(), R.color.white));
        dialog.getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
    }

    protected abstract void onOKClick();

    @Override
    protected void initCommonView(){

        editorTagViewStub.inflate();
        flowLayout = root.findViewById(R.id.fragment_edit_todo_tags);
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
    }

    @Override
    protected void initDialogView() {
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
        editorContainer.setBackgroundColor(Color.WHITE);
        editorContainer.setBackgroundResource(R.drawable.corners_5dp);
        editorContainer.setPadding(5,5,5,0);
    }

    @Override
    protected void onDialogStartInternal() {
        super.onDialogStartInternal();
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
        params.height = InputMethodUtil.getDialogHeightWithSoftInputMethod();;
        win.setAttributes(params);
        editorContent.setMinimumHeight(dm.heightPixels);
        InputMethodUtil.showKeyboard(editorContent);
    }

    @Override
    protected void onDialogShowKeyboard(ShowKeyBoardEvent event) {
        super.onDialogShowKeyboard(event);
        adjustDialogSize(event.get());
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

        editorContent.setMinimumHeight(dm.heightPixels);
    }
}
