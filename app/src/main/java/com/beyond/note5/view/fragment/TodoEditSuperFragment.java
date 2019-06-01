package com.beyond.note5.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
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
import com.beyond.note5.event.HideKeyBoardEvent2;
import com.beyond.note5.event.HideTodoEditorEvent;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.view.animator.SmoothScalable;
import com.beyond.note5.view.animator.SmoothScaleAnimation;
import com.beyond.note5.view.listener.OnBackPressListener;
import com.beyond.note5.view.listener.OnKeyboardChangeListener;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TodoEditSuperFragment extends DialogFragment implements OnBackPressListener, SmoothScalable,FragmentContainerAware {


    protected Handler handler;
    protected Todo createdDocument;
    protected int currentIndex;

    protected View fragmentContainer;
    protected EditText contentEditText;

    protected OnKeyboardChangeListener onKeyboardChangeListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        createdDocument = new Todo();
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_todo_edit, null);
        initView(root);
        initEvent(root);
        return root;
    }

    protected void initView(View view) {
        view.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.white));
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

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void initOnKeyboardChangeListener(OnKeyboardChangeListener onKeyboardChangeListener) {
        this.onKeyboardChangeListener = onKeyboardChangeListener;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ShowKeyBoardEvent event) {
        String type = event.getType();
        if (StringUtils.equals(Document.TODO, type) && fragmentContainer != null) {
            fragmentContainer.getLayoutParams().height = InputMethodUtil.getDialogHeightWithSoftInputMethod();
            fragmentContainer.setLayoutParams(fragmentContainer.getLayoutParams());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(final HideKeyBoardEvent2 event) {
        final String type = event.getType();
        event.get().setHideCallback(new Runnable() {
            @Override
            public void run() {
                if (StringUtils.equals(Document.TODO, type)) {
                    EventBus.getDefault().post(new HideTodoEditorEvent(currentIndex));
                }
            }
        });

    }

    @Override
    public boolean onBackPressed() {
        InputMethodUtil.hideKeyboard(contentEditText, onKeyboardChangeListener, true);
        EventBus.getDefault().post(new HideTodoEditorEvent(currentIndex));
        return true;
    }

    public void registerHooks(SmoothScaleAnimation smoothScaleAnimation) {
        smoothScaleAnimation.setAfterShowHook(new Runnable() {
            @Override
            public void run() {
                getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.white));
            }
        });
        smoothScaleAnimation.setAfterHideHook(new Runnable() {
            @Override
            public void run() {
                fragmentContainer.setVisibility(View.GONE);
            }
        });
        smoothScaleAnimation.setBeforeShowHook(new Runnable() {
            @Override
            public void run() {
                InputMethodUtil.showKeyboard(contentEditText);
            }
        });
        smoothScaleAnimation.setBeforeHideHook(new Runnable() {
            @Override
            public void run() {
                getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(),R.color.white));
            }
        });
    }

    @Override
    public void setFragmentContainer(View fragmentContainer) {
        this.fragmentContainer = fragmentContainer;
    }

}
