package com.alexstyl.searchtransition.searchscreen;

import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.alexstyl.searchtransition.boilerplate.BoilerplateActivity;
import com.alexstyl.searchtransition.transition.FadeInTransition;
import com.alexstyl.searchtransition.transition.FadeOutTransition;
import com.alexstyl.searchtransition.transition.SimpleTransitionListener;
import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.LoadType;
import com.beyond.note5.event.FillNoteDetailEvent;
import com.beyond.note5.event.HideNoteDetailEvent;
import com.beyond.note5.event.ShowNoteDetailEvent;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.utils.StatusBarUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;
import com.beyond.note5.view.animator.DefaultSmoothScaleAnimation;
import com.beyond.note5.view.animator.SmoothScalable;
import com.beyond.note5.view.animator.SmoothScaleAnimation;
import com.beyond.note5.view.fragment.FragmentContainerAware;
import com.beyond.note5.view.fragment.NoteDetailSuperFragment;
import com.beyond.note5.view.fragment.SearchResultFragment;
import com.beyond.note5.view.listener.OnBackPressListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

import static android.view.KeyEvent.KEYCODE_ENTER;

public class SearchActivity extends BoilerplateActivity {

    private Searchbar searchbar;

    private FrameLayout resultContainer;

    private NotePresenter notePresenter;

    private Fragment noteDetailFragment;
    private Fragment todoModifyFragment;

    @BindView(R.id.note_detail_fragment_container)
    FrameLayout noteDetailFragmentContainer;

    @BindView(R.id.main_container)
    CoordinatorLayout mainContainer;

    private SmoothScaleAnimation noteDetailSmoothScaleAnimation = new DefaultSmoothScaleAnimation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        StatusBarUtil.showLightWhiteStatusBar(this);

        searchbar = (Searchbar) findViewById(R.id.search_toolbar);
        resultContainer = findViewById(R.id.search_result_container);

        // make sure to check if this is the first time running the activity
        // we don't want to play the enter animation on configuration changes (i.e. orientation)
        if (isFirstTimeRunning(savedInstanceState)) {
            // Start with an empty looking Toolbar
            // We are going to fade its contents in, as long as the activity finishes rendering
            searchbar.hideContent();

            ViewTreeObserver viewTreeObserver = searchbar.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    searchbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    // after the activity has finished drawing the initial layout, we are going to continue the animation
                    // that we left off from the MainActivity
                    showSearch();
                }

                private void showSearch() {
                    // use the TransitionManager to animate the changes of the Toolbar
                    TransitionManager.beginDelayedTransition(searchbar, FadeInTransition.createTransition());
                    // here we are just changing all children to VISIBLE
                    searchbar.showContent();
                }
            });
        }

        EventBus.getDefault().register(this);

        initNoteDetailFragmentContainer();

        initPresenter();

        initSearchEvent();

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

    private void initSearchEvent() {
        searchbar.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showSearchResult();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchbar.getEditText().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KEYCODE_ENTER) {
                    showSearchResult();
                }
                return false;
            }
        });
    }

    private void initPresenter() {
        notePresenter = new NotePresenterImpl(new MyNoteView());
    }

    private void showSearchResult() {
        String searchKey = searchbar.getText();
        notePresenter.search(searchKey);
    }

    private boolean isFirstTimeRunning(Bundle savedInstanceState) {
        return savedInstanceState == null;
    }

    @Override
    public void finish() {
        // when the user tries to finish the activity we have to animate the exit
        // let's start by hiding the keyboard so that the exit seems smooth
//        hideKeyboard();

        // at the same time, start the exit transition
        exitTransitionWithAction(new Runnable() {
            @Override
            public void run() {
                // which finishes the activity (for real) when done
                SearchActivity.super.finish();

                // override the system pending transition as we are handling ourselves
                overridePendingTransition(0, 0);
            }
        });
    }

    private void exitTransitionWithAction(final Runnable endingAction) {

        Transition transition = FadeOutTransition.withAction(new SimpleTransitionListener() {
            @Override
            public void onTransitionEnd(Transition transition) {
                endingAction.run();
            }
        });

        TransitionManager.beginDelayedTransition(searchbar, transition);
        searchbar.hideContent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_clear) {
            searchbar.clearText();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        }  else {
            super.onBackPressed();
        }
        ToastUtil.cancel();
    }

    class MyNoteView extends NoteViewAdapter {

        @Override
        public void onSearchSuccess(List<Note> documents) {
            super.onSearchSuccess(documents);
            SearchResultFragment fragment = new SearchResultFragment();
            fragment.setData(documents);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.search_result_container, fragment);
            fragmentTransaction.commit();
        }
    }

}
