package com.beyond.note5.view.fragment;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.constant.LoadType;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.utils.WebViewUtil;
import com.beyond.note5.view.custom.ViewSwitcher;

import java.util.List;

public class NoteMultiDetailStage extends ViewSwitcher implements MultiDetailStage<Note> {


    private List<Note> data;

    private int currentIndex;

    private int enterIndex;

    private MultiDetailStage.ViewFactory viewFactory;


    public NoteMultiDetailStage(Context context) {
        super(context);
    }

    public NoteMultiDetailStage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public List<Note> getData() {
        return data;
    }

    @Override
    public Note getCurrentData() {
        return data.get(currentIndex);
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

    @Override
    public void setViewFactory(MultiDetailStage.ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }


    private WebView getNextWebView() {
        return getNextView().findViewById(R.id.fragment_document_display_web);
    }

    WebView getCurrentWebView(){
        return getCurrentView().findViewById(R.id.fragment_document_display_web);
    }

    @Override
    public void refresh(LoadType loadType) {
        removeAllViews();
        setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                return viewFactory.getView();
            }
        });
        load(loadType);
    }

    @Override
    public void load(LoadType loadType) {
        loadType.show(getCurrentWebView(),data.get(currentIndex));
    }

    @Override
    public void clear() {
        removeAllViews();
    }

    private void initContentData(WebView displayWebView) {
        WebViewUtil.clearHistory();
        WebViewUtil.loadWebContent(displayWebView, data.get(getCurrentIndex()));
    }


    @Override
    public boolean onBackPressed() {
        if (getCurrentView() == null){
            return true;
        }
        WebView displayWebView = getCurrentWebView();
        if (WebViewUtil.canGoBack(displayWebView)) {
            WebViewUtil.goBack(displayWebView);
            return true;
        } else {
            WebViewUtil.clearHistory();
            return false;
        }
    }
}
