package com.beyond.note5.constant;

import android.webkit.WebView;

import com.beyond.note5.bean.Document;
import com.beyond.note5.utils.WebViewUtil;

public enum  LoadType {
    CONTENT, WEB;

    public void show(WebView webView, Document document) {
        switch (this) {
            case CONTENT:
                WebViewUtil.loadWebContent(webView,document);
                break;
            case WEB:
                String url = WebViewUtil.getUrlOrSearchUrl(document);
                if (url != null) {
                    WebViewUtil.addWebViewProgressBar(webView);
                    webView.loadUrl(url);
                }
                break;
        }
    }
}
