package com.beyond.note5.view.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.LoadType;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.utils.ViewUtil;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.custom.ViewSwitcher;
import com.beyond.note5.view.listener.OnSlideListener;

import java.util.List;

public class NoteDetailStage extends ViewSwitcher implements DetailStage<Note> {


    private List<Note> data;

    private int currentIndex;

    private int enterIndex;

    private LoadType loadType;

    private OnViewMadeListener onViewMadeListener;


    public NoteDetailStage(Context context) {
        super(context);
    }

    public NoteDetailStage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public List<Note> getData() {
        return data;
    }

    @Override
    public void setData(List<Note> data) {
        this.data = data;
    }

    @Override
    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    @Override
    public int getEnterIndex() {
        return enterIndex;
    }

    @Override
    public void setEnterIndex(int enterIndex) {
        this.enterIndex = enterIndex;
    }

    @Override
    public void prev() {

        if (currentIndex == 0) {
            ToastUtil.toast(getContext(), "已到达第一页");
        }
        if (currentIndex > 0) {
            currentIndex--;
            setInAnimation(getContext(), R.anim.slide_in_left);
            setOutAnimation(getContext(), R.anim.slide_out_right);
            initContentData(getNextWebView());
            showPrevious();
        }
        
    }

    @Override
    public void next() {
        if (currentIndex == data.size() - 1) {
            ToastUtil.toast(getContext(), "已到达最后一页");
        }
        if (currentIndex < data.size() - 1) {
            currentIndex++;
            setInAnimation(getContext(), R.anim.slide_in_right);
            setOutAnimation(getContext(), R.anim.slide_out_left);
            initContentData(getNextWebView());
            showNext();
        }
    }


    private WebView getNextWebView() {
        return getNextView().findViewById(R.id.fragment_document_display_web);
    }

    WebView getCurrentWebView(){
        return getCurrentView().findViewById(R.id.fragment_document_display_web);
    }

    @Override
    public void refresh() {
        removeAllViews();
        setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_note_detail_content, null);
                view.setMinimumHeight(2000);

                WebView displayWebView = view.findViewById(R.id.fragment_document_display_web);

                initContentConfig(displayWebView);
                initContentData(displayWebView);
                initContentEvent(displayWebView);

                onViewMadeListener.onViewMade(view);

                return view;
            }
        });
    }

    @Override
    public void setOnViewMadeListener(OnViewMadeListener listener) {
        this.onViewMadeListener = listener;
    }

    @Override
    public void setLoadType(LoadType loadType) {
        this.loadType = loadType;
    }

    @Override
    public void loadMore() {
        loadType.show(getCurrentWebView(),data.get(currentIndex));
    }

    private void initContentConfig(WebView displayWebView) {
        WebViewUtil.configWebView(displayWebView);
    }

    private void initContentData(WebView displayWebView) {
        WebViewUtil.clearHistory();
        WebViewUtil.loadWebContent(displayWebView, data.get(getCurrentIndex()));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initContentEvent(final WebView displayWebView) {
        displayWebView.setOnTouchListener(new OnSlideListener(getContext()) {
            @Override
            protected void onSlideLeft() {
                next();
            }

            @Override
            protected void onSlideRight() {
                prev();
            }

            @Override
            protected void onSlideUp() {
            }

            @Override
            protected void onSlideDown() {
            }

            @Override
            protected void onDoubleClick(MotionEvent e) {
                //TODO:showModifyView();
            }

            @Override
            protected int getSlideXSensitivity() {
                return 250;
            }

            @Override
            protected int getSlideYSensitivity() {
                return (int) (ViewUtil.getScreenSize().y * 0.33);
            }
        });
    }
}
