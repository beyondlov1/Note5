package com.alexstyl.searchtransition.searchscreen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;

import com.beyond.note5.R;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.HideTodoEditorEvent;
import com.beyond.note5.event.ShowTodoEditorEvent;
import com.beyond.note5.presenter.TodoPresenter;
import com.beyond.note5.presenter.TodoPresenterImpl;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.adapter.view.TodoViewAdapter;
import com.beyond.note5.view.animator.DefaultSmoothScaleAnimation;
import com.beyond.note5.view.animator.SmoothScalable;
import com.beyond.note5.view.animator.SmoothScaleAnimation;
import com.beyond.note5.view.fragment.FragmentContainerAware;
import com.beyond.note5.view.fragment.TodoModifySuperFragment;
import com.beyond.note5.view.fragment.TodoSearchResultFragment;
import com.beyond.note5.view.listener.OnBackPressListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

public class TodoSearchActivity extends SearchActivity {

    private TodoPresenter todoPresenter;

    private Fragment todoModifyFragment;

    @BindView(R.id.todo_edit_fragment_container)
    FrameLayout todoEditFragmentContainer;

    private SmoothScaleAnimation todoEditSmoothScaleAnimation = new DefaultSmoothScaleAnimation();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        searchbar.setHint("Search Todos");

        initTodoEditFragmentContainer();

        initPresenter();

        // 显示搜索结果
        showSearchResult();

    }


    private void initTodoEditFragmentContainer() {
        todoModifyFragment = new TodoModifySuperFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.todo_edit_fragment_container, todoModifyFragment);
        fragmentTransaction.commit();

        todoEditFragmentContainer = findViewById(R.id.todo_edit_fragment_container);
        todoEditFragmentContainer.setVisibility(View.GONE);

        if (todoModifyFragment instanceof FragmentContainerAware) {
            ((FragmentContainerAware) todoModifyFragment).setFragmentContainer(todoEditFragmentContainer);
        }

        if (todoModifyFragment instanceof SmoothScalable) {
            ((SmoothScalable) todoModifyFragment).registerHooks(todoEditSmoothScaleAnimation);
        }
    }

    private void initPresenter() {
        todoPresenter = new TodoPresenterImpl(new MyTodoView());
    }

    @Override
    protected void showSearchResult() {
        String searchKey = searchbar.getText();
        todoPresenter.search(searchKey);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(final ShowTodoEditorEvent event) {
        showTodoEdit(event.get());
    }

    private void showTodoEdit(View startView) {
        todoEditFragmentContainer.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(HideTodoEditorEvent event) {
        hideTodoEditor(event.get());
    }

    private void hideTodoEditor(int returnIndex) {
        todoEditFragmentContainer.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (todoEditFragmentContainer.getVisibility() == View.VISIBLE) {
            OnBackPressListener todoEditOnBackPressListener = (OnBackPressListener) todoModifyFragment;
            boolean consumed = todoEditOnBackPressListener.onBackPressed();
            if (!consumed) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
        ToastUtil.cancel();
    }

    private class MyTodoView extends TodoViewAdapter {
        @Override
        public void onSearchSuccess(List<Todo> documents) {
            super.onSearchSuccess(documents);
            TodoSearchResultFragment fragment = new TodoSearchResultFragment();
            fragment.setData(documents);
            fragment.setSearchKey(searchbar.getText());
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.search_result_container, fragment);
            fragmentTransaction.commit();
        }
    }
}
