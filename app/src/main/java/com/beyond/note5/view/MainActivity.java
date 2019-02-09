package com.beyond.note5.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.beyond.note5.R;
import com.beyond.note5.event.HideFABEvent;
import com.beyond.note5.event.HideKeyBoardEvent;
import com.beyond.note5.event.ShowFABEvent;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.view.fragment.NoteEditFragment;
import com.beyond.note5.view.fragment.NoteListFragment;
import com.beyond.note5.view.fragment.TodoEditFragment;
import com.beyond.note5.view.fragment.TodoListFragment;
import com.beyond.note5.view.listener.OnKeyboardChangeListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: beyond
 * @date: 2019/1/30
 */
public class MainActivity extends FragmentActivity {

    public View mainContainer;
    private ViewPager mainViewPager;
    private FloatingActionButton addDocumentButton;
    private List<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initViewPagerData();
        initEvent();

    }

    private void initView() {
        mainContainer = findViewById(R.id.main_container);
        mainViewPager = findViewById(R.id.main_view_pager);
        PagerTabStrip pagerTabStrip = findViewById(R.id.pager_tab_strip);
        addDocumentButton = findViewById(R.id.add_document);

        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.google_yellow));
        pagerTabStrip.setTextColor(getResources().getColor(R.color.white));
    }

    private void initViewPagerData() {
        Fragment noteFragment = new NoteListFragment();
        Fragment todoFragment = new TodoListFragment();
        fragments.add(noteFragment);
        fragments.add(todoFragment);
        final List<String> fragmentTitles = new ArrayList<>();
        fragmentTitles.add("Note");
        fragmentTitles.add("Todo");
        mainViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return fragmentTitles.get(position);
            }

        });
    }

    private void initEvent() {
        //监控输入法
        this.getWindow().getDecorView().getViewTreeObserver()
                .addOnGlobalLayoutListener(new OnKeyboardChangeListener(this) {
                    @Override
                    protected void onKeyBoardShow(int x, int y) {
                        super.onKeyBoardShow(x, y);
                        EventBus.getDefault().post(new ShowKeyBoardEvent(y));
                    }

                    @Override
                    protected void onKeyBoardHide() {
                        super.onKeyBoardHide();
                        EventBus.getDefault().post(new HideKeyBoardEvent(null));
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainViewPager.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    EventBus.getDefault().post(new ShowFABEvent(R.id.note_recycler_view));
                }
            });
        }
        addDocumentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentItemPosition = mainViewPager.getCurrentItem();
                Fragment fragment = fragments.get(currentItemPosition);
                DialogFragment dialog;
                if (fragment instanceof NoteListFragment) {
                    dialog = new NoteEditFragment();
                } else if (fragment instanceof TodoListFragment) {
                    dialog = new TodoEditFragment();
                } else {
                    return;
                }
                dialog.show(getSupportFragmentManager(), "editDialog");

            }
        });
    }

    private AtomicBoolean isFabShown = new AtomicBoolean(true);
    private AnimatorSet showAnimatorSet;
    private AnimatorSet hideAnimatorSet;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        showAnimatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.fab_show);
        hideAnimatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.fab_hide);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecievedHideFABCommand(HideFABEvent hideFABEvent) {
        if (isFabShown.get()) {
            hideAnimatorSet.setTarget(addDocumentButton);
            hideAnimatorSet.start();
            hideAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isFabShown.set(false);
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecievedShowFABCommand(ShowFABEvent showFABEvent) {
        if (!isFabShown.get()) {
            showAnimatorSet.setTarget(addDocumentButton);
            showAnimatorSet.start();
            showAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isFabShown.set(true);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
