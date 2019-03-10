package com.beyond.note5.view.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.beyond.note5.R;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.HideKeyBoardEvent;
import com.beyond.note5.event.HideTodoEditEvent;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.view.animator.DefaultSmoothScalable;
import com.beyond.note5.view.animator.SmoothScalable;
import com.beyond.note5.view.listener.OnBackPressListener;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TodoEditSuperFragment extends DialogFragment implements OnBackPressListener, SmoothScalable {

    private Activity context;

    protected View root;
    protected EditText contentEditText;

    private SmoothScalable smoothScalable = new DefaultSmoothScalable();

    protected Todo createdDocument = new Todo();

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        initEvent(root);
        return root;
    }

    protected void initView(View view) {
        view.setBackgroundColor(context.getResources().getColor(R.color.white));
        contentEditText = view.findViewById(R.id.fragment_edit_todo_content);
    }

    protected void initEvent(View view) {
        //防止事件向下传递
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
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
    public void onEventMainThread(ShowKeyBoardEvent event) {
        String type = event.getType();
        if (StringUtils.equals(Document.TODO, type)){
            smoothScalable.getContainer().getLayoutParams().height = InputMethodUtil.getDialogHeightWithSoftInputMethod();
            smoothScalable.getContainer().setLayoutParams(smoothScalable.getContainer().getLayoutParams());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(HideKeyBoardEvent event) {
        String type = event.getType();
        if (StringUtils.equals(Document.TODO, type)){
            EventBus.getDefault().post(new HideTodoEditEvent(null));
        }
    }

    @Override
    public boolean onBackPressed() {
        InputMethodUtil.hideKeyboard(contentEditText);
        return true;
    }

    public void setSmoothScalable(SmoothScalable smoothScalable) {
        this.smoothScalable = smoothScalable;
    }

    @Override
    public void setContainer(View view) {
        this.smoothScalable.setContainer(view);
    }

    @Override
    public View getContainer() {
        return smoothScalable.getContainer();
    }

    @Override
    public void setStartView(View view) {
        this.smoothScalable.setStartView(view);
    }

    @Override
    public View getStartView() {
        return this.smoothScalable.getStartView();
    }

    @Override
    public void setEndView(View view) {
        this.smoothScalable.setEndView(view);
    }

    @Override
    public View getEndView() {
        return this.smoothScalable.getEndView();
    }

    @Override
    public void setShowingView(View view) {
        this.smoothScalable.setShowingView(view);
    }

    @Override
    public View getShowingView() {
        return this.smoothScalable.getShowingView();
    }

    @Override
    public void show() {
        InputMethodUtil.showKeyboard(contentEditText);
        this.smoothScalable.setOnShownListener(new Runnable() {
            @Override
            public void run() {
                context.getWindow().setStatusBarColor(ContextCompat.getColor(context,R.color.white));
            }
        });
        this.smoothScalable.setOnHiddenListener(new Runnable() {
            @Override
            public void run() {
                smoothScalable.getContainer().setVisibility(View.GONE);
            }
        });
        this.smoothScalable.show();
    }

    @Override
    public void hide() {
        context.getWindow().setStatusBarColor(getResources().getColor(R.color.google_blue));
        this.smoothScalable.hide();
    }

    @Override
    public void setOnShownListener(Runnable onShownListener) {
        //do nothing
    }

    @Override
    public void setOnHiddenListener(Runnable onHiddenListener) {
        //do nothing
    }

}
