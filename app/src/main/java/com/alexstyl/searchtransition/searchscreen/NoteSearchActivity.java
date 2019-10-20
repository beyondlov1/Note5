package com.alexstyl.searchtransition.searchscreen;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.LoadType;
import com.beyond.note5.event.FillNoteDetailEvent;
import com.beyond.note5.event.HideNoteDetailEvent;
import com.beyond.note5.event.ShowNoteDetailEvent;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;
import com.beyond.note5.view.animator.DefaultSmoothScaleAnimation;
import com.beyond.note5.view.animator.SmoothScalable;
import com.beyond.note5.view.animator.SmoothScaleAnimation;
import com.beyond.note5.view.fragment.FragmentContainerAware;
import com.beyond.note5.view.fragment.NoteDetailSuperFragment;
import com.beyond.note5.view.fragment.NoteSearchResultFragment;
import com.beyond.note5.view.listener.OnBackPressListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

public class NoteSearchActivity extends SearchActivity {

    private NotePresenter notePresenter;

    private Fragment noteDetailFragment;

    @BindView(R.id.note_detail_fragment_container)
    FrameLayout noteDetailFragmentContainer;

    private SmoothScaleAnimation noteDetailSmoothScaleAnimation = new DefaultSmoothScaleAnimation();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        searchbar.setHint("Search Notes");

        initNoteDetailFragmentContainer();

        initPresenter();

        // 显示搜索结果
        showSearchResult();

    }


    private void initNoteDetailFragmentContainer() {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        noteDetailFragment = new NoteDetailSuperFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.note_detail_fragment_container, noteDetailFragment);
        fragmentTransaction.commit();

        noteDetailFragmentContainer = findViewById(R.id.note_detail_fragment_container);
        noteDetailFragmentContainer.setVisibility(View.GONE);

        if (noteDetailFragment instanceof FragmentContainerAware) {
            ((FragmentContainerAware) noteDetailFragment).setFragmentContainer(noteDetailFragmentContainer);
        }

        if (noteDetailFragment instanceof SmoothScalable) {
            ((SmoothScalable) noteDetailFragment).registerHooks(noteDetailSmoothScaleAnimation);
        }
    }

    private void initPresenter() {
        notePresenter = new NotePresenterImpl(new MyNoteView());
    }

    @Override
    protected void showSearchResult() {
        String searchKey = searchbar.getText();
        notePresenter.search(searchKey);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(final ShowNoteDetailEvent event) {
        showNoteDetail(event.get(), event.getData(), event.getIndex(), event.getLoadType());
    }

    private void showNoteDetail(View startView, List<Note> data, int index, LoadType loadType) {
        noteDetailFragmentContainer.setVisibility(View.VISIBLE);

        FillNoteDetailEvent fillNoteDetailEvent = new FillNoteDetailEvent(data, index);
        fillNoteDetailEvent.setLoadType(loadType);
        EventBus.getDefault().postSticky(fillNoteDetailEvent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(HideNoteDetailEvent event) {
        noteDetailFragmentContainer.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (noteDetailFragmentContainer.getVisibility() == View.VISIBLE) {
            OnBackPressListener noteDetailOnBackPressListener = (OnBackPressListener) noteDetailFragment;
            boolean consumed = noteDetailOnBackPressListener.onBackPressed();
            if (!consumed) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
        ToastUtil.cancel();
    }

    class MyNoteView extends NoteViewAdapter {

        @Override
        public void onSearchSuccess(List<Note> documents) {
            super.onSearchSuccess(documents);
            NoteSearchResultFragment fragment = new NoteSearchResultFragment();
            fragment.setData(documents);
            fragment.setSearchKey(searchbar.getText());
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.search_result_container, fragment);
            fragmentTransaction.commit();
        }
    }

}
