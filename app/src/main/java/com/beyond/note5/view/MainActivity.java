package com.beyond.note5.view;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.event.AddNoteEvent;
import com.beyond.note5.event.DetailNoteEvent;
import com.beyond.note5.event.HideFABEvent;
import com.beyond.note5.event.HideKeyBoardEvent;
import com.beyond.note5.event.HideNoteDetailEvent;
import com.beyond.note5.event.ShowFABEvent;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.event.ShowNoteDetailEvent;
import com.beyond.note5.event.ShowTodoEditEvent;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.PhotoUtil;
import com.beyond.note5.utils.ViewUtil;
import com.beyond.note5.view.adapter.component.header.ItemDataGenerator;
import com.beyond.note5.view.fragment.NoteDetailSuperFragment;
import com.beyond.note5.view.fragment.NoteEditFragment;
import com.beyond.note5.view.fragment.NoteListFragment;
import com.beyond.note5.view.fragment.TodoEditFragment;
import com.beyond.note5.view.fragment.TodoEditSuperFragment;
import com.beyond.note5.view.fragment.TodoListFragment;
import com.beyond.note5.view.listener.OnBackPressListener;
import com.beyond.note5.view.listener.OnKeyboardChangeListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: beyond
 * @date: 2019/1/30
 */
public class MainActivity extends FragmentActivity {

    public View mainContainer;
    private ViewPager mainViewPager;
    private View noteDetailFragmentContainer;
    private View todoEditFragmentContainer;
    private FloatingActionButton addDocumentButton;
    private List<Fragment> fragments = new ArrayList<>();

    private Fragment noteDetailFragment;
    private Fragment todoEditFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initViewPagerData();
        initEvent();

        initNoteDetailFragmentContainer();
//        initTodoEditFragmentContainer();

        //permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private void initNoteDetailFragmentContainer(){
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        noteDetailFragment = new NoteDetailSuperFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_fragment_note_detail, noteDetailFragment);
        fragmentTransaction.commit();

        noteDetailFragmentContainer = findViewById(R.id.container_fragment_note_detail);
        noteDetailFragmentContainer.setVisibility(View.GONE);
    }

    private void initTodoEditFragmentContainer() {
        todoEditFragment = new TodoEditSuperFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_fragment_todo_edit, todoEditFragment);
        fragmentTransaction.commit();

        todoEditFragmentContainer = findViewById(R.id.container_fragment_todo_edit);
        todoEditFragmentContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            System.out.println("yes");
        }
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
    public void onRecieved(final ShowNoteDetailEvent event) {
        noteDetailFragmentContainer.setVisibility(View.VISIBLE);
        EventBus.getDefault().post(new HideFABEvent(null));

        View view = event.get();

        //获取view 位置、大小信息
        final int clickItemWidth = ViewUtil.getWidth(view);
        final int clickItemHeight = ViewUtil.getHeight(view);
        final float clickItemX = ViewUtil.getXInScreenWithoutNotification(view);
        final float clickItemY = ViewUtil.getYInScreenWithoutNotification(view);

        //设置初始位置
        final int containerWidth = ViewUtil.getWidth(mainContainer);
        final int containerHeight = ViewUtil.getHeight(mainContainer);
        noteDetailFragmentContainer.getLayoutParams().width = clickItemWidth;
        noteDetailFragmentContainer.getLayoutParams().height = clickItemHeight;
        noteDetailFragmentContainer.setX(clickItemX);
        noteDetailFragmentContainer.setY(clickItemY);

        //出现动画
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1f).setDuration(300);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.playTogether(valueAnimator);
        animatorSet.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                noteDetailFragmentContainer.setX(clickItemX - animatedValue * clickItemX);
                noteDetailFragmentContainer.setY(clickItemY - animatedValue * clickItemY);
                noteDetailFragmentContainer.getLayoutParams().width = (int) (clickItemWidth + animatedValue * (containerWidth - clickItemWidth));
                noteDetailFragmentContainer.getLayoutParams().height = (int) (clickItemHeight + animatedValue * (containerHeight - clickItemHeight));
                noteDetailFragmentContainer.setLayoutParams(noteDetailFragmentContainer.getLayoutParams());
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            }
        });
        EventBus.getDefault().postSticky(new DetailNoteEvent(event.getData(), event.getIndex()));
        isDetailShow = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecieved(HideNoteDetailEvent event) {
        EventBus.getDefault().post(new ShowFABEvent(null));

        //获取viewSwitcher划到的位置，获取动画要返回的view
        Integer currIndex = event.get();
        View view = getViewToReturn(currIndex);

        //获取view 位置、大小信息
        final int clickItemWidth = ViewUtil.getWidth(view);
        final int clickItemHeight = ViewUtil.getHeight(view);
        final float clickItemX = ViewUtil.getXInScreenWithoutNotification(view);
        final float clickItemY = ViewUtil.getYInScreenWithoutNotification(view);
        final int containerWidth = ViewUtil.getWidth(mainContainer);
        final int containerHeight = ViewUtil.getHeight(mainContainer);

        //出现动画
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0).setDuration(300);
        animatorSet.playTogether(valueAnimator);
        animatorSet.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                noteDetailFragmentContainer.setX(clickItemX - animatedValue * clickItemX);
                noteDetailFragmentContainer.setY(clickItemY - animatedValue * clickItemY);
                noteDetailFragmentContainer.getLayoutParams().width = (int) (clickItemWidth + animatedValue * (containerWidth - clickItemWidth));
                noteDetailFragmentContainer.getLayoutParams().height = (int) (clickItemHeight + animatedValue * (containerHeight - clickItemHeight));
                noteDetailFragmentContainer.setLayoutParams(noteDetailFragmentContainer.getLayoutParams());
            }
        });

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                noteDetailFragmentContainer.setVisibility(View.GONE);
            }
        });
        isDetailShow = false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecieved(final ShowTodoEditEvent event) {
        todoEditFragmentContainer.setVisibility(View.VISIBLE);
        EventBus.getDefault().post(new HideFABEvent(null));

        View view = event.get();

        //获取view 位置、大小信息
        final int clickItemWidth = ViewUtil.getWidth(view);
        final int clickItemHeight = ViewUtil.getHeight(view);
        final float clickItemX = ViewUtil.getXInScreenWithoutNotification(view);
        final float clickItemY = ViewUtil.getYInScreenWithoutNotification(view);

        //设置初始位置
        final int containerWidth = ViewUtil.getWidth(mainContainer);
        final int containerHeight = ViewUtil.getHeight(mainContainer);
        todoEditFragmentContainer.getLayoutParams().width = clickItemWidth;
        todoEditFragmentContainer.getLayoutParams().height = clickItemHeight;
        todoEditFragmentContainer.setX(clickItemX);
        todoEditFragmentContainer.setY(clickItemY);

        //出现动画
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1f).setDuration(300);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.playTogether(valueAnimator);
        animatorSet.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                todoEditFragmentContainer.setX(clickItemX - animatedValue * clickItemX);
                todoEditFragmentContainer.setY(clickItemY - animatedValue * clickItemY);
                todoEditFragmentContainer.getLayoutParams().width = (int) (clickItemWidth + animatedValue * (containerWidth - clickItemWidth));
                todoEditFragmentContainer.getLayoutParams().height = (int) (clickItemHeight + animatedValue * (containerHeight - clickItemHeight));
                todoEditFragmentContainer.setLayoutParams(todoEditFragmentContainer.getLayoutParams());
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private View getViewToReturn(Integer currIndex) {
        View view;
        if (currIndex == -1) {
            view = new View(this);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mainContainer.getLayoutParams());
            layoutParams.width = 0;
            layoutParams.height = 0;
            view.setLayoutParams(layoutParams);
            view.setX(0);
            view.setY(0);
        } else {
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
        if (isDetailShow) {
            OnBackPressListener onBackPressListener = (OnBackPressListener) noteDetailFragment;
            boolean consumed = onBackPressListener.onBackPressed();
            if (!consumed) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                addPhotoNote();
            }
        }
    }

    private String currPhotoPath;
    
    private void takePhoto() {
        File file = PhotoUtil.takePhoto(this,1);
        if (file != null) {
            currPhotoPath = file.getAbsolutePath();
        }
//        testFilePath();
    }

    private void addPhotoNote() {
        String content = "!file://" + currPhotoPath;
        String noteId = IDUtil.uuid();
        File file = new File(currPhotoPath);
        String name = file.getName();
        Date currDate = new Date();

        List<Attachment> attachments = new ArrayList<>();
        Attachment attachment = new Attachment();
        attachment.setId(IDUtil.uuid());
        attachment.setName(name);
        attachment.setNoteId(noteId);
        attachment.setPath(currPhotoPath);
        attachments.add(attachment);

        Note note = new Note();
        note.setId(noteId);
        note.setContent(content);
        note.setAttachments(attachments);
        note.setCreateTime(currDate);
        note.setLastModifyTime(currDate);

        EventBus.getDefault().post(new AddNoteEvent(note));
    }

}
