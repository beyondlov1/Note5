package com.beyond.note5.view.fragment;

import android.view.View;
import android.widget.EditText;

import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.HideKeyBoardEvent2;
import com.beyond.note5.event.HideTodoEditorEvent;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.utils.StatusBarUtil;
import com.beyond.note5.view.animator.SmoothScalable;
import com.beyond.note5.view.animator.SmoothScaleAnimation;
import com.beyond.note5.view.listener.OnBackPressListener;
import com.beyond.note5.view.listener.OnKeyboardChangeListener;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@Deprecated
public abstract class TodoEditSuperFragment extends AbstractTodoEditorFragment implements OnBackPressListener, SmoothScalable,FragmentContainerAware {


    protected int currentIndex;

    protected View fragmentContainer;
    protected EditText contentEditText;

    protected OnKeyboardChangeListener onKeyboardChangeListener;

    @Override
    protected Todo creatingDocument() {
        return new Todo();
    }




    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void initOnKeyboardChangeListener(OnKeyboardChangeListener onKeyboardChangeListener) {
        this.onKeyboardChangeListener = onKeyboardChangeListener;
    }

    @Override
    protected void onFragmentShowKeyboard(ShowKeyBoardEvent event) {
        super.onFragmentShowKeyboard(event);
        String type = event.getType();
        if (StringUtils.equals(Document.TODO, type) && fragmentContainer != null) {
            fragmentContainer.getLayoutParams().height = InputMethodUtil.getDialogHeightWithSoftInputMethod();
            fragmentContainer.setLayoutParams(fragmentContainer.getLayoutParams());
        }
    }

    @Override
    protected void onFragmentHideKeyboard(HideKeyBoardEvent2 event) {
        super.onFragmentHideKeyboard(event);
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
                StatusBarUtil.showLightWhiteStatusBar(getActivity());
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
                StatusBarUtil.showLightWhiteStatusBar(getActivity());
            }
        });
    }

    @Override
    public void setFragmentContainer(View fragmentContainer) {
        this.fragmentContainer = fragmentContainer;
    }

}
