package com.beyond.note5.view.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.beyond.note5.R;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.HideKeyBoardEvent;
import com.beyond.note5.view.listener.OnBackPressListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TodoEditSuperFragment extends DialogFragment implements OnBackPressListener {

    private Context context;
    private static int dialogHeightWithSoftInputMethod;
    private static final String DIALOG_HEIGHT_WITH_SOFT_INPUT_METHOD = "dialogHeightWithSoftInputMethod";


    protected View root;
    protected EditText contentEditText;

    protected Todo createdDocument = new Todo();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("onCreate");

        super.onCreate(savedInstanceState);
        this.context = getActivity();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = LayoutInflater.from(context).inflate(R.layout.fragment_todo_edit, null);
        }
        initView(root);
        initEvent();
        return root;
    }

    private void initView(View view) {
        //防止事件向下传递
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        contentEditText = view.findViewById(R.id.fragment_edit_todo_content);
    }

    private void initEvent() {
        contentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                createdDocument.setContent(s.toString());
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
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(HideKeyBoardEvent event) {
        dismiss();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
