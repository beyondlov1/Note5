package com.beyond.note5.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.beyond.note5.R;
import com.beyond.note5.event.DetailNoteEvent;
import com.beyond.note5.event.HideFABEvent;
import com.beyond.note5.event.HideKeyBoardEvent;
import com.beyond.note5.event.HideNoteDetailEvent;
import com.beyond.note5.event.ShowFABEvent;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.event.ShowNoteDetailEvent;
import com.beyond.note5.utils.ViewUtil;
import com.beyond.note5.view.adapter.component.header.ItemDataGenerator;
import com.beyond.note5.view.fragment.NoteDetailSuperFragment;
import com.beyond.note5.view.fragment.NoteEditFragment;
import com.beyond.note5.view.fragment.NoteListFragment;
import com.beyond.note5.view.fragment.TodoEditFragment;
import com.beyond.note5.view.fragment.TodoListFragment;
import com.beyond.note5.view.listener.OnBackPressListener;
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
    private View fragmentContainer;
    private FloatingActionButton addDocumentButton;
    private List<Fragment> fragments = new ArrayList<>();

    private Fragment detailFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initViewPagerData();
        initEvent();

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        detailFragment = new NoteDetailSuperFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_fragment_note_detail, detailFragment);
        fragmentTransaction.commit();

        fragmentContainer = findViewById(R.id.container_fragment_note_detail);
        fragmentContainer.setVisibility(View.GONE);
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

    private boolean isDetailShow = false;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecieved(final ShowNoteDetailEvent event){
        fragmentContainer.setVisibility(View.VISIBLE);
        EventBus.getDefault().post(new HideFABEvent(null));

        View view = event.get();

        //获取view 位置、大小信息
        final int clickItemWidth = ViewUtil.getWidth(view);
        final int clickItemHeight =  ViewUtil.getHeight(view);
        final float clickItemX = ViewUtil.getXInScreenWithoutNotification(view);
        final float clickItemY = ViewUtil.getYInScreenWithoutNotification(view);

        //设置初始位置
        final int containerWidth = ViewUtil.getWidth(mainContainer);
        final int containerHeight = ViewUtil.getHeight(mainContainer);
        fragmentContainer.getLayoutParams().width = clickItemWidth;
        fragmentContainer.getLayoutParams().height = clickItemHeight;
        fragmentContainer.setX(clickItemX);
        fragmentContainer.setY(clickItemY);

        //出现动画
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1f).setDuration(300);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.playTogether(valueAnimator);
        animatorSet.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                fragmentContainer.setX(clickItemX -animatedValue* clickItemX);
                fragmentContainer.setY(clickItemY -animatedValue* clickItemY);
                fragmentContainer.getLayoutParams().width = (int) (clickItemWidth + animatedValue*(containerWidth - clickItemWidth));
                fragmentContainer.getLayoutParams().height = (int) (clickItemHeight + animatedValue*(containerHeight - clickItemHeight));
                fragmentContainer.setLayoutParams(fragmentContainer.getLayoutParams());
            }
        });
        EventBus.getDefault().postSticky(new DetailNoteEvent(event.getData(),event.getIndex()));
        isDetailShow = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecieved(HideNoteDetailEvent event){
        EventBus.getDefault().post(new ShowFABEvent(null));

        //获取viewSwitcher划到的位置，获取动画要返回的view
        Integer currIndex = event.get();
        View view = getViewToReturn(currIndex);

        //获取view 位置、大小信息
        final int clickItemWidth = ViewUtil.getWidth(view);
        final int clickItemHeight =  ViewUtil.getHeight(view);
        final float clickItemX = ViewUtil.getXInScreenWithoutNotification(view);
        final float clickItemY = ViewUtil.getYInScreenWithoutNotification(view);
        final int containerWidth = ViewUtil.getWidth(mainContainer);
        final int containerHeight = ViewUtil.getHeight(mainContainer);

        //出现动画
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f,0).setDuration(300);
        animatorSet.playTogether(valueAnimator);
        animatorSet.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                fragmentContainer.setX(clickItemX -animatedValue* clickItemX);
                fragmentContainer.setY(clickItemY -animatedValue* clickItemY);
                fragmentContainer.getLayoutParams().width = (int) (clickItemWidth + animatedValue*(containerWidth - clickItemWidth));
                fragmentContainer.getLayoutParams().height = (int) (clickItemHeight + animatedValue*(containerHeight - clickItemHeight));
                fragmentContainer.setLayoutParams(fragmentContainer.getLayoutParams());
            }
        });

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fragmentContainer.setVisibility(View.GONE);
            }
        });
        isDetailShow = false;
    }

    @SuppressWarnings("unchecked")
    private View getViewToReturn(Integer currIndex) {
        View view;
        if (currIndex == -1){
            view = new View(this);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mainContainer.getLayoutParams());
            layoutParams.width = 0;
            layoutParams.height = 0;
            view.setLayoutParams(layoutParams);
            view.setX(0);
            view.setY(0);
        }else {
            NoteListFragment fragment = (NoteListFragment) fragments.get(0);
            ItemDataGenerator itemDataGenerator = fragment.noteRecyclerViewAdapter.getItemDataGenerator();
            Object note = itemDataGenerator.getContentData().get(currIndex);
            int position = itemDataGenerator.getPosition(note);
            fragment.noteRecyclerView.scrollToPosition(position);
            view = fragment.noteRecyclerView.getLayoutManager().findViewByPosition(position);
        }
        return view;
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

    @Override
    public void onBackPressed() {
        if (isDetailShow){
            OnBackPressListener onBackPressListener = (OnBackPressListener) detailFragment;
            boolean consumed = onBackPressListener.onBackPressed();
            if (!consumed){
                super.onBackPressed();
            }
        }else {
            super.onBackPressed();
        }
    }
}
