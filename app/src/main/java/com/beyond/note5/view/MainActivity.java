package com.beyond.note5.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Intent;
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
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Note;
import com.beyond.note5.bean.Reminder;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.AddNoteEvent;
import com.beyond.note5.event.DetailNoteEvent;
import com.beyond.note5.event.HideFABEvent;
import com.beyond.note5.event.HideKeyBoardEvent;
import com.beyond.note5.event.HideNoteDetailEvent;
import com.beyond.note5.event.HideTodoEditEvent;
import com.beyond.note5.event.ShowFABEvent;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.event.ShowNoteDetailEvent;
import com.beyond.note5.event.ShowTodoEditEvent;
import com.beyond.note5.model.CalendarModel;
import com.beyond.note5.model.CalendarModelImpl;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.PhotoUtil;
import com.beyond.note5.utils.PreferenceUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.adapter.component.header.ItemDataGenerator;
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

import org.apache.commons.lang3.StringUtils;
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

    private void test() {
        CalendarModel calendarModel = new CalendarModelImpl(this);
        Todo todo = new Todo();
        todo.setId(IDUtil.uuid());
        todo.setTitle("hahah");
        todo.setContent("content");
        Reminder reminder = new Reminder();
        String reminderId = IDUtil.uuid();
        reminder.setId(reminderId);
        reminder.setStart(new Date());
        todo.setReminderId(reminderId);
        todo.setReminder(reminder);
        calendarModel.add(todo);
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

    private void initEvent() {
        //监控输入法
        this.getWindow().getDecorView().getViewTreeObserver()
                .addOnGlobalLayoutListener(new OnKeyboardChangeListener(this) {
                    @Override
                    protected void onKeyBoardShow(int x, int y) {
                        super.onKeyBoardShow(x, y);
                        ShowKeyBoardEvent showKeyBoardEvent = new ShowKeyBoardEvent(y);
                        showKeyBoardEvent.setType(currentType);
                        EventBus.getDefault().post(showKeyBoardEvent);
                    }

                    @Override
                    protected void onKeyBoardHide() {
                        super.onKeyBoardHide();
                        HideKeyBoardEvent hideKeyBoardEvent = new HideKeyBoardEvent(null);
                        hideKeyBoardEvent.setType(currentType);
                        EventBus.getDefault().post(hideKeyBoardEvent);
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainViewPager.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    int currentItemPosition = mainViewPager.getCurrentItem();
                    Fragment fragment = fragments.get(currentItemPosition);
                    if (fragment instanceof NoteListFragment) {
                        currentType = Document.NOTE;
                    } else if (fragment instanceof TodoListFragment) {
                        currentType = Document.TODO;
                    }
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
        addDocumentButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (StringUtils.equals(currentType, Document.NOTE)) {
//                    takePhoto();
                    new IntentIntegrator(MainActivity.this).initiateScan();
                    return true;
                }
                return false;
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
        EventBus.getDefault().post(new ShowFABEvent(null));

        //获取viewSwitcher划到的位置，获取动画要返回的view
        Integer currIndex = event.get();
        View view = getViewToReturn(currIndex);

        SmoothScalable smoothScalable = (SmoothScalable) noteDetailFragment;
        smoothScalable.setEndView(view);
        smoothScalable.hide();
        isDetailShow = false;
    }

    private boolean isTodoEditShow = false;
    private View clickedView = null;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(final ShowTodoEditEvent event) {
        todoEditFragmentContainer.setVisibility(View.VISIBLE);
        EventBus.getDefault().post(new HideFABEvent(null));

        View view = event.get();
        clickedView = view;

        SmoothScalable smoothScalable = (SmoothScalable) this.todoModifyFragment;
        smoothScalable.setContainer(todoEditFragmentContainer);
        smoothScalable.setStartView(view);
        smoothScalable.setShowingView(mainContainer);

        smoothScalable.show();
        isTodoEditShow = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(HideTodoEditEvent event) {
        EventBus.getDefault().post(new ShowFABEvent(null));

        SmoothScalable smoothScalable = (SmoothScalable) todoModifyFragment;
        smoothScalable.setEndView(clickedView == null ? getViewToReturn(-1) : clickedView);
        smoothScalable.hide();
        isTodoEditShow = false;
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
        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                ToastUtil.toast(this, "Cancelled", Toast.LENGTH_LONG);
            } else {
                addQRCodeNote(result.getContents());
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

}
