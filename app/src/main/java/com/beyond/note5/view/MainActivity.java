package com.beyond.note5.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Note;
import com.beyond.note5.event.AddNoteEvent;
import com.beyond.note5.event.DetailNoteEvent;
import com.beyond.note5.event.HideFABEvent;
import com.beyond.note5.event.HideKeyBoardEvent2;
import com.beyond.note5.event.HideNoteDetailEvent;
import com.beyond.note5.event.HideTodoEditEvent;
import com.beyond.note5.event.ShowFABEvent;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.event.ShowNoteDetailEvent;
import com.beyond.note5.event.ShowTodoEditEvent;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.PhotoUtil;
import com.beyond.note5.utils.PreferenceUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.utils.ViewUtil;
import com.beyond.note5.view.animator.SmoothScalable;
import com.beyond.note5.view.fragment.NoteDetailSuperFragment;
import com.beyond.note5.view.fragment.NoteEditFragment;
import com.beyond.note5.view.fragment.NoteListFragment;
import com.beyond.note5.view.fragment.TodoEditFragment;
import com.beyond.note5.view.fragment.TodoListFragment;
import com.beyond.note5.view.fragment.TodoModifySuperFragment;
import com.beyond.note5.view.listener.OnBackPressListener;
import com.beyond.note5.view.listener.OnKeyboardChangeListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
@TargetApi(Build.VERSION_CODES.M)
public class MainActivity extends FragmentActivity implements View.OnClickListener,
        View.OnLongClickListener, View.OnScrollChangeListener, View.OnTouchListener {

    private static final int TAKE_PHOTO_REQUEST_CODE = 1;

    public View mainContainer;
    private ViewPager mainViewPager;
    private View noteDetailFragmentContainer;
    private View todoEditFragmentContainer;
    private FloatingActionButton addDocumentButton;
    private List<Fragment> fragments = new ArrayList<>();

    private Fragment noteDetailFragment;
    private Fragment todoModifyFragment;

    private String[] documentTypes = new String[3];

    private String currentType;

    private StaticViewHolder staticViewHolder = new StaticViewHolder();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initViewPagerData();
        initEvent();

        initNoteDetailFragmentContainer();
        initTodoEditFragmentContainer();

    }

    private void initNoteDetailFragmentContainer() {
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
        todoModifyFragment = new TodoModifySuperFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_fragment_todo_edit, todoModifyFragment);
        fragmentTransaction.commit();

        todoEditFragmentContainer = findViewById(R.id.container_fragment_todo_edit);
        todoEditFragmentContainer.setVisibility(View.GONE);
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
        documentTypes[0] = Document.NOTE;
        documentTypes[1] = Document.TODO;
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
        mainViewPager.setCurrentItem(PreferenceUtil.getInt(MyApplication.DEFAULT_PAGE));
        currentType = documentTypes[mainViewPager.getCurrentItem()];
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        //监控输入法
        this.getWindow().getDecorView().getViewTreeObserver()
                .addOnGlobalLayoutListener(new MyOnKeyboardChangeListener(this));
        mainViewPager.setOnScrollChangeListener(this);
        addDocumentButton.setOnTouchListener(this);
        addDocumentButton.setOnClickListener(this);
        addDocumentButton.setOnLongClickListener(this);
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
    public void onReceivedHideFABCommand(HideFABEvent hideFABEvent) {
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
    public void onReceivedShowFABCommand(ShowFABEvent showFABEvent) {
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
    public void onReceived(final ShowNoteDetailEvent event) {
        noteDetailFragmentContainer.setVisibility(View.VISIBLE);
        EventBus.getDefault().post(new HideFABEvent(null));

        View view = event.get();

        SmoothScalable smoothScalable = (SmoothScalable) noteDetailFragment;
        smoothScalable.setContainer(noteDetailFragmentContainer);
        smoothScalable.setStartView(view);
        smoothScalable.setShowingView(mainContainer);

        smoothScalable.show();
        DetailNoteEvent detailNoteEvent = new DetailNoteEvent(event.getData(), event.getIndex());
        detailNoteEvent.setShowType(event.getShowType());
        EventBus.getDefault().postSticky(detailNoteEvent);
        isDetailShow = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(HideNoteDetailEvent event) {
        if (!Document.NOTE.equals(currentType)) {
            return;
        }

        EventBus.getDefault().post(new ShowFABEvent(null));

        //获取viewSwitcher划到的位置，获取动画要返回的view
        Integer currIndex = event.get();
        int firstIndex = event.getFirstIndex();
        View view = getNoteViewToReturn(currIndex, firstIndex);

        SmoothScalable smoothScalable = (SmoothScalable) noteDetailFragment;
        smoothScalable.setEndView(view);
        smoothScalable.hide();
        isDetailShow = false;
    }

    private boolean isTodoEditShow = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(final ShowTodoEditEvent event) {
        todoEditFragmentContainer.setVisibility(View.VISIBLE);
        EventBus.getDefault().post(new HideFABEvent(null));

        View view = event.get();

        SmoothScalable smoothScalable = (SmoothScalable) this.todoModifyFragment;
        smoothScalable.setContainer(todoEditFragmentContainer);
        smoothScalable.setStartView(view);
        smoothScalable.setShowingView(mainContainer);

        smoothScalable.show();
        isTodoEditShow = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(HideTodoEditEvent event) {
        if (!Document.TODO.equals(currentType)) {
            return;
        }

        EventBus.getDefault().post(new ShowFABEvent(null));

        SmoothScalable smoothScalable = (SmoothScalable) todoModifyFragment;
        smoothScalable.setEndView(getTodoViewToReturn(event.get()));
        smoothScalable.hide();
        isTodoEditShow = false;
    }

    @SuppressWarnings("unchecked")
    private View getNoteViewToReturn(Integer currIndex, Integer firstIndex) {
        View view;
        if (currIndex == -1) {
            view = staticViewHolder.getLeftTopView();
        } else {
            NoteListFragment fragment = (NoteListFragment) fragments.get(0);
            fragment.scrollTo(currIndex);
            view = fragment.findViewBy(currIndex);
            if (view == null && firstIndex < currIndex) {
                view = staticViewHolder.getRightBottomView();
            } else if (view == null && firstIndex > currIndex) {
                view = staticViewHolder.getLeftTopView();
            } else if (view == null) {
                view = staticViewHolder.getRightBottomView(); // 正常情况下不会执行
            }
        }
        return view;
    }

    @SuppressWarnings("unchecked")
    private View getTodoViewToReturn(Integer currIndex) {
        View view;
        if (currIndex == -1) {
            view = staticViewHolder.getLeftTopView();
        } else {
            TodoListFragment fragment = (TodoListFragment) fragments.get(1);
//            fragment.scrollTo(currIndex);
            view = fragment.findViewBy(currIndex);
            if (view == null) {
                view = staticViewHolder.getRightBottomView();
            }
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
    protected void onDestroy() {
        int currentItem = mainViewPager.getCurrentItem();
        PreferenceUtil.put(MyApplication.DEFAULT_PAGE, currentItem);
        Log.d("MainActivity", currentItem + "");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isDetailShow) {
            OnBackPressListener noteDetailOnBackPressListener = (OnBackPressListener) noteDetailFragment;
            boolean consumed = noteDetailOnBackPressListener.onBackPressed();
            if (!consumed) {
                super.onBackPressed();
            }
        } else if (isTodoEditShow) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO_REQUEST_CODE) {
            addPhotoNote();
        }else if (resultCode == RESULT_CANCELED &&requestCode == TAKE_PHOTO_REQUEST_CODE){
            boolean delete = new File(currPhotoPath).delete();
            Log.d(this.getClass().getSimpleName(),""+delete);
        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                ToastUtil.toast(this, "Cancelled", Toast.LENGTH_LONG);
            } else {
                addQRCodeNote(result.getContents());
                if (!Document.NOTE.equals(currentType)) {
                    mainViewPager.setCurrentItem(0);
                }
                ToastUtil.toast(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addQRCodeNote(String content) {
        Date currDate = new Date();
        Note note = new Note();
        note.setId(IDUtil.uuid());
        note.setContent(content);
        note.setCreateTime(currDate);
        note.setLastModifyTime(currDate);

        EventBus.getDefault().post(new AddNoteEvent(note));
    }

    private String currPhotoPath;

    private void takePhoto() {
        File file = PhotoUtil.takePhoto(this, TAKE_PHOTO_REQUEST_CODE);
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

    @Override
    public void onClick(View v) {
        if (v == addDocumentButton) {
            addDocument();
        }
    }

    private void addDocument() {
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

    @Override
    public boolean onLongClick(View v) {
        if (v == addDocumentButton) {
            launchQRScanner();
            return true;
        }
        return false;
    }

    private void launchQRScanner() {
        new IntentIntegrator(MainActivity.this).initiateScan();
    }

    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (v == mainViewPager) {
            switchViewPager();
        }
    }

    private void switchViewPager() {
        int currentItemPosition = mainViewPager.getCurrentItem();
        Fragment fragment = fragments.get(currentItemPosition);
        if (fragment instanceof NoteListFragment) {
            currentType = Document.NOTE;
        } else if (fragment instanceof TodoListFragment) {
            currentType = Document.TODO;
        }
        EventBus.getDefault().post(new ShowFABEvent(R.id.note_recycler_view));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == addDocumentButton) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    v.setX(v.getX() - v.getWidth() / 2 + event.getX());
                    if (v.getX() < 200) {
                        v.setX(200);
                    }

                    if (v.getX() > ViewUtil.getScreenSize().x - v.getWidth() - 200) {
                        v.setX(ViewUtil.getScreenSize().x - v.getWidth() - 200);
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    if (v.getX() == 200) {
                        v.setX(ViewUtil.getScreenSize().x / 2 - v.getWidth() / 2);
                        takePhoto();
                    } else if (v.getX() == ViewUtil.getScreenSize().x - v.getWidth() - 200) {
                        v.setX(ViewUtil.getScreenSize().x / 2 - v.getWidth() / 2);
                        launchQRScanner();
                    } else {
                        v.setX(ViewUtil.getScreenSize().x / 2 - v.getWidth() / 2);
                        v.performClick();
                    }

                    break;
            }
            return true;

        }
        return false;
    }

    private class StaticViewHolder {

        private View rightBottomView;
        private View leftTopView;

        @NonNull
        private View getRightBottomView() {
            if (rightBottomView != null) {
                return rightBottomView;
            }
            rightBottomView = new View(MainActivity.this);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mainContainer.getLayoutParams());
            layoutParams.width = 0;
            layoutParams.height = 0;
            rightBottomView.setLayoutParams(layoutParams);
            rightBottomView.setX(ViewUtil.getScreenSizeWithoutNotification().x);
            rightBottomView.setY(ViewUtil.getScreenSizeWithoutNotification().y);
            return rightBottomView;
        }

        @NonNull
        private View getLeftTopView() {
            if (leftTopView != null) {
                return leftTopView;
            }
            leftTopView = new View(MainActivity.this);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mainContainer.getLayoutParams());
            layoutParams.width = 0;
            layoutParams.height = 0;
            leftTopView.setLayoutParams(layoutParams);
            leftTopView.setX(0);
            leftTopView.setY(0);
            return leftTopView;
        }
    }

    private class MyOnKeyboardChangeListener extends OnKeyboardChangeListener {

        MyOnKeyboardChangeListener(Activity context) {
            super(context);
        }

        @Override
        protected void onKeyBoardShow(int x, int y) {
            ShowKeyBoardEvent showKeyBoardEvent = new ShowKeyBoardEvent(y);
            showKeyBoardEvent.setType(currentType);
            EventBus.getDefault().post(showKeyBoardEvent);
        }

        @Override
        protected void onKeyBoardHide() {
            //Event 为消耗品， 每次都要新建
            HideKeyBoardEvent2 hideKeyBoardEvent = new HideKeyBoardEvent2(this);
            hideKeyBoardEvent.setType(currentType);
            EventBus.getDefault().post(hideKeyBoardEvent);
            if (isExecuteHideCallback() && getHideCallback() != null) {
                getHideCallback().run();
            }
            setExecuteHideCallback(true);
        }
    }
}
