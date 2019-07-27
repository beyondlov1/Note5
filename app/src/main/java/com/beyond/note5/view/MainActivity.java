package com.beyond.note5.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.LoadType;
import com.beyond.note5.event.PollRequest;
import com.beyond.note5.event.PollResponse;
import com.beyond.note5.event.FillNoteDetailEvent;
import com.beyond.note5.event.HideFABEvent;
import com.beyond.note5.event.HideKeyBoardEvent2;
import com.beyond.note5.event.HideNoteDetailEvent;
import com.beyond.note5.event.HideTodoEditorEvent;
import com.beyond.note5.event.ShowFABEvent;
import com.beyond.note5.event.ShowFloatButtonEvent;
import com.beyond.note5.event.ShowKeyBoardEvent;
import com.beyond.note5.event.ShowNoteDetailEvent;
import com.beyond.note5.event.ShowTodoEditorEvent;
import com.beyond.note5.presenter.NotePresenter;
import com.beyond.note5.presenter.NotePresenterImpl;
import com.beyond.note5.service.FloatEditorService;
import com.beyond.note5.utils.GBData;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.PhotoUtil;
import com.beyond.note5.utils.PreferenceUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.utils.ViewUtil;
import com.beyond.note5.view.adapter.view.NoteViewAdapter;
import com.beyond.note5.view.animator.DefaultSmoothScaleAnimation;
import com.beyond.note5.view.animator.SmoothScalable;
import com.beyond.note5.view.animator.SmoothScaleAnimation;
import com.beyond.note5.view.fragment.PreferenceFragment;
import com.beyond.note5.view.fragment.FragmentContainerAware;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

/**
 * @author: beyond
 * @date: 2019/1/30
 */
@TargetApi(Build.VERSION_CODES.M)
public class MainActivity extends FragmentActivity implements
        View.OnScrollChangeListener {

    private static final int TAKE_PHOTO_REQUEST_CODE = 1;
    public static final int NOTIFICATION_REDIRECT_REQUEST_CODE = 2;

    @BindView(R.id.pager_tab_strip)
    PagerTabStrip pagerTabStrip;
    @BindView(R.id.main_view_pager)
    ViewPager mainViewPager;
    @BindView(R.id.note_detail_fragment_container)
    FrameLayout noteDetailFragmentContainer;
    @BindView(R.id.todo_edit_fragment_container)
    FrameLayout todoEditFragmentContainer;
    @BindView(R.id.add_document_button)
    FloatingActionButton addDocumentButton;
    @BindView(R.id.main_container)
    CoordinatorLayout mainContainer;

    private List<Fragment> fragments = new ArrayList<>();

    private Fragment noteDetailFragment;
    private Fragment todoModifyFragment;

    private SmoothScaleAnimation noteDetailSmoothScaleAnimation = new DefaultSmoothScaleAnimation();
    private SmoothScaleAnimation todoEditSmoothScaleAnimation = new DefaultSmoothScaleAnimation();

    private String[] documentTypes = new String[3];
    private String currentType;

    private AtomicBoolean isFabShown = new AtomicBoolean(true);
    private AnimatorSet showAnimatorSet;
    private AnimatorSet hideAnimatorSet;

    protected NotePresenter notePresenter;

    private final static int REQUEST_MEDIA_PROJECTION = 2;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initInjection();

        initView();
        initViewPager();
        initEvent();

        initNoteDetailFragmentContainer();
        initTodoEditFragmentContainer();

//        initColorPicker();

    }

    private void initColorPicker() {
        mediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        assert mediaProjectionManager != null;
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),REQUEST_MEDIA_PROJECTION);
    }

    private void initInjection() {
        notePresenter = new NotePresenterImpl(new MyNoteView());
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

    private void initView() {
        pagerTabStrip.setTabIndicatorColor(ContextCompat.getColor(this, R.color.google_yellow));
        pagerTabStrip.setTextColor(ContextCompat.getColor(this, R.color.black));

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
    }

    private void initViewPager() {
        Fragment noteFragment = new NoteListFragment();
        Fragment todoFragment = new TodoListFragment();
        Fragment configFragment = new PreferenceFragment();
        fragments.add(noteFragment);
        fragments.add(todoFragment);
        fragments.add(configFragment);
        final List<String> fragmentTitles = new ArrayList<>();
        fragmentTitles.add("Note");
        fragmentTitles.add("Todo");
        fragmentTitles.add("Config");
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
        if (mainViewPager.getCurrentItem() < 2){
            currentType = documentTypes[mainViewPager.getCurrentItem()];
        }
    }

    private void initEvent() {
        //监控输入法
        OnKeyboardChangeListener onKeyboardChangeListener = new MyOnKeyboardChangeListener(this);
        this.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(onKeyboardChangeListener);
        mainViewPager.setOnScrollChangeListener(this);
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        initFABAnimator();
    }

    private void initFABAnimator() {
        showAnimatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.fab_show);
        hideAnimatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.fab_hide);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(ShowFABEvent showFABEvent) {
        showFAB();
    }

    private void showFAB(){
        if (showAnimatorSet == null){
            initFABAnimator();
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(HideFABEvent hideFABEvent) {
        hideFAB();
    }

    private void hideFAB(){
        if (hideAnimatorSet == null){
            initFABAnimator();
        }
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
    public void onReceived(final ShowNoteDetailEvent event) {
        showNoteDetail(event.get(),event.getData(),event.getIndex(),event.getLoadType());
    }

    private void showNoteDetail(View startView, List<Note> data, int index, LoadType loadType){
        noteDetailFragmentContainer.setVisibility(View.VISIBLE);
        EventBus.getDefault().post(new HideFABEvent(null));

        noteDetailSmoothScaleAnimation.setContainer(noteDetailFragmentContainer);
        noteDetailSmoothScaleAnimation.setStartView(startView);
        noteDetailSmoothScaleAnimation.setShowingView(mainContainer);
        noteDetailSmoothScaleAnimation.show();

        FillNoteDetailEvent fillNoteDetailEvent = new FillNoteDetailEvent(data, index);
        fillNoteDetailEvent.setLoadType(loadType);
        EventBus.getDefault().postSticky(fillNoteDetailEvent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(HideNoteDetailEvent event) {
        Integer currIndex = event.get();
        int firstIndex = event.getFirstIndex();
        hideNoteDetail(currIndex,firstIndex);
    }

    private void hideNoteDetail(int currIndex, int firstIndex){
        if (!Document.NOTE.equals(currentType)) {
            return;
        }
        EventBus.getDefault().post(new ShowFABEvent(null));

        //获取viewSwitcher划到的位置，获取动画要返回的view
        View view = getNoteViewToReturn(currIndex, firstIndex);
        noteDetailSmoothScaleAnimation.setEndView(view);
        noteDetailSmoothScaleAnimation.hide();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(final ShowTodoEditorEvent event) {
        showTodoEdit(event.get());
    }

    private void showTodoEdit(View startView){
        todoEditFragmentContainer.setVisibility(View.VISIBLE);
        EventBus.getDefault().post(new HideFABEvent(null));

        todoEditSmoothScaleAnimation.setContainer(todoEditFragmentContainer);
        todoEditSmoothScaleAnimation.setStartView(startView);
        todoEditSmoothScaleAnimation.setShowingView(mainContainer);
        todoEditSmoothScaleAnimation.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(HideTodoEditorEvent event) {
        hideTodoEditor(event.get());
    }

   private void hideTodoEditor(int returnIndex){
       if (!Document.TODO.equals(currentType)) {
           return;
       }
       EventBus.getDefault().post(new ShowFABEvent(null));

       todoEditSmoothScaleAnimation.setEndView(getTodoViewToReturn(returnIndex));
       todoEditSmoothScaleAnimation.hide();
   }

    @SuppressWarnings("unchecked")
    private View getNoteViewToReturn(Integer currIndex, Integer firstIndex) {
        View view;
        if (currIndex == -1) {
            view = ViewUtil.getLeftTopView(this,mainContainer);
        } else {
            NoteListFragment fragment = (NoteListFragment) fragments.get(0);
            fragment.scrollTo(currIndex);
            view = fragment.findViewBy(currIndex);
            if (view == null && firstIndex < currIndex) {
                view = ViewUtil.getRightBottomView(this,mainContainer);
            } else if (view == null && firstIndex > currIndex) {
                view = ViewUtil.getLeftTopView(this,mainContainer);
            } else if (view == null) {
                view = ViewUtil.getRightBottomView(this,mainContainer); // 正常情况下不会执行
            }
        }
        return view;
    }

    @SuppressWarnings("unchecked")
    private View getTodoViewToReturn(Integer currIndex) {
        View view;
        if (currIndex == -1) {
            view = ViewUtil.getLeftTopView(this,mainContainer);
        } else {
            TodoListFragment fragment = (TodoListFragment) fragments.get(1);
//            fragment.scrollTo(currIndex);
            view = fragment.findViewBy(currIndex);
            if (view == null) {
                view = ViewUtil.getRightBottomView(this,mainContainer);
            }
        }
        return view;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(PollRequest event) {
       if (event.get() == this.getClass()){
           EventBus.getDefault().post(new PollResponse(this.getClass()));
       }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int systemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
//        layoutParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        showFloatEditor();
    }

    private void showFloatEditor() {

//        // 悬浮窗授权
//        if (!Settings.canDrawOverlays(this)){
//            ToastUtil.toast(this,"请允许在其他应用之上");
//            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                    Uri.parse("package:"+getPackageName())),OVERLAY_REQUEST_CODE);
//        }

        if (Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(this, FloatEditorService.class);
            intent.putExtra("showFloatButton", false);
            startService(intent);
        }
    }

    @Override
    protected void onStop() {

        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().post(new ShowFloatButtonEvent(null));
        int currentItem = mainViewPager.getCurrentItem();
        PreferenceUtil.put(MyApplication.DEFAULT_PAGE, currentItem);
        Log.d("MainActivity", currentItem + "");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (noteDetailFragmentContainer.getVisibility() == View.VISIBLE) {
            OnBackPressListener noteDetailOnBackPressListener = (OnBackPressListener) noteDetailFragment;
            boolean consumed = noteDetailOnBackPressListener.onBackPressed();
            if (!consumed) {
                super.onBackPressed();
            }
        } else if (todoEditFragmentContainer.getVisibility() == View.VISIBLE) {
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



    @OnClick(R.id.add_document_button)
    public void onViewClicked() {
        addDocument();
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



    /** Media Note START **/

    @OnTouch(R.id.add_document_button)
    public boolean onAddDocumentButtonTouch(View v, MotionEvent event) {
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
                    launchCamera();
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

    private void launchQRScanner() {
        new IntentIntegrator(MainActivity.this).initiateScan();
    }

    private void launchCamera() {
        PhotoUtil.takePhoto(this, TAKE_PHOTO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION){
            if (resultCode != RESULT_OK ){
                Log.d(getClass().getSimpleName(),"canceled");
                return;
            }
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode,data);
            setUpVirtualDisplay();
        }

        if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO_REQUEST_CODE) {
            addPhotoNote();
        } else if (resultCode == RESULT_CANCELED && requestCode == TAKE_PHOTO_REQUEST_CODE) {
            if (PhotoUtil.getLastPhotoFile()!=null){
                boolean delete = PhotoUtil.getLastPhotoFile().delete();
                Log.d(this.getClass().getSimpleName(), "" + delete);
            }
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

    private void setUpVirtualDisplay() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        ImageReader imageReader = ImageReader.newInstance(displayMetrics.widthPixels,displayMetrics.heightPixels,
                PixelFormat.RGBA_8888,1);
        mediaProjection.createVirtualDisplay("ScreenCapture",
                displayMetrics.widthPixels,displayMetrics.heightPixels, displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),null,null);
        GBData.reader = imageReader;
    }

    private void addQRCodeNote(String content) {
        Note note = Note.create();
        note.setContent(content);
        notePresenter.add(note);
    }

    private void addPhotoNote() {
        File file = PhotoUtil.getLastPhotoFile();
        String currPhotoPath = file.getAbsolutePath();
        String content = "!file://" + currPhotoPath;
        String noteId = IDUtil.uuid();
        String name = file.getName();

        List<Attachment> attachments = new ArrayList<>();
        Attachment attachment = new Attachment();
        attachment.setId(IDUtil.uuid());
        attachment.setName(name);
        attachment.setNoteId(noteId);
        attachment.setPath(currPhotoPath);
        attachments.add(attachment);

        Note note = Note.create();
        note.setId(noteId);
        note.setContent(content);
        note.setAttachments(attachments);

        notePresenter.add(note);
    }

    /** Media Note END **/



    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (v == mainViewPager) {
            changeViewPager();
        }
    }

    private void changeViewPager() {
        int currentItemPosition = mainViewPager.getCurrentItem();
        Fragment fragment = fragments.get(currentItemPosition);
        if (fragment instanceof NoteListFragment) {
            currentType = Document.NOTE;
        } else if (fragment instanceof TodoListFragment) {
            currentType = Document.TODO;
        }
        EventBus.getDefault().post(new ShowFABEvent(R.id.note_recycler_view));
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

    private class MyNoteView extends NoteViewAdapter {

        @Override
        public void onAddFail(Note document) {
            ToastUtil.toast(MainActivity.this, "添加失敗");
        }
    }

}
