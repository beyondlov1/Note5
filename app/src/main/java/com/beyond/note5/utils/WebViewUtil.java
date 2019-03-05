package com.beyond.note5.utils;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.beyond.note5.bean.Document;
import com.beyond.note5.utils.converter.Converter;
import com.beyond.note5.utils.converter.ConverterBuilder;
import com.beyond.note5.utils.converter.Document2HtmlConverter;
import com.beyond.note5.utils.converter.Document2MarkdownConverter;
import com.beyond.note5.utils.converter.Markdown2HtmlConverter;
import com.beyond.note5.utils.converter.MarkdownHtml2HtmlConverter;

import java.util.Stack;
import java.util.regex.Pattern;


/**
 * @author: beyond
 * @date: 2019/2/3
 */

@SuppressWarnings("unchecked")
public class WebViewUtil {

    public static final String MIME_TYPE = "text/html; charset=UTF-8";
    private static final String TAG = "WebViewUtil";

    private static Stack<String> historyUrlStack = new Stack<>();

    private static Document2HtmlConverter document2HtmlConverter = new Document2HtmlConverter();

    private static Converter<Document,String> converter;

    static {
        Document2MarkdownConverter document2MarkdownConverter = new Document2MarkdownConverter();
        Markdown2HtmlConverter markdown2HtmlConverter = new Markdown2HtmlConverter();
        MarkdownHtml2HtmlConverter markdownHtml2HtmlConverter = new MarkdownHtml2HtmlConverter();
        ConverterBuilder<Document,String> builder = new ConverterBuilder<>();
        converter=builder.addConverter(document2MarkdownConverter)
                .addConverter(markdown2HtmlConverter)
                .addConverter(markdownHtml2HtmlConverter).build();
    }

    public static void loadWebContent(WebView webView, Document document){
        loadWebContent(webView,document,converter);
    }

    public static void loadWebContent(WebView webView, Document document, Converter<Document,String> converter){
        String html = converter.convert(document);
        loadWebContent(webView,html);
    }

    public static void loadWebContent(WebView webView, String content){
        //直接用webView.loadData(content, MIME_TYPE,"UTF-8");会乱码
        //https://blog.csdn.net/top_code/article/details/9163597
        webView.loadDataWithBaseURL(null,content, MIME_TYPE,null,null);
        Log.d(TAG,content);
    }

    private static String lastUrl;
    public static void configWebView(final WebView webView){
        final WebSettings webSettings = webView.getSettings();
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("webviewUtil",url);
                if (lastUrl!=null){
                    historyUrlStack.push(lastUrl);
                }
                lastUrl = url;
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                removeWebViewAllViews(view);
            }
        });
    }

    public static boolean canGoBack(WebView webView){
        return !historyUrlStack.empty();
    }

    /**
     * @param webView webview
     * @return canGoBack
     */
    public static void goBack(WebView webView){
        if (!historyUrlStack.empty()){
            String url = historyUrlStack.pop();
            addWebViewProgressBar(webView);
            webView.loadUrl(url);
            lastUrl = url;
        }else {
            lastUrl = null;
        }
    }

    public static void clearHistory() {
        historyUrlStack.clear();
        lastUrl = null;
    }

    public static void addWebViewProgressBar(WebView webView){
        final ProgressBar progressBar = new ProgressBar(webView.getContext(),null,android.R.attr.progressBarStyleHorizontal);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 8);
        progressBar.setLayoutParams(layoutParams);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    if (progressBar.getVisibility() == View.GONE)
                        progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
                progressBar.setProgress(newProgress);
            }
        });

        webView.addView(progressBar);
    }

    public static void removeWebViewAllViews(WebView webView){
        webView.removeAllViews();
    }

    public static String getUrl(Document document){
        String urlWeGet=null;
        String things = document.getContent();
        if (things.contains("http://")||things.contains("https://")){
            //含网址的获取网址
            //网址正则式
            Pattern pattern = Pattern.compile("^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-Z0-9\\.&%\\$\\-]+)*@)?((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,4})(\\:[0-9]+)?(/[^/][a-zA-Z0-9\\.\\,\\?\\'\\\\/\\+&%\\$#\\=~_\\-@]*)*$");

            if (things.length()<200){
                //小于200有网址的获取网址
                if (pattern.matcher(things.substring(things.indexOf("http"), things.length())).matches()){
                    urlWeGet=things.substring(things.indexOf("http"), things.length());
                }else {
                    for (int i = things.length(); !pattern.matcher(things.substring(things.indexOf("http"), i)).matches(); i--) {
                        urlWeGet = things.substring(things.indexOf("http"), i);
                    }
                }
            }else {
                //大于200的有网址的获取网址
                String shortThings=things.substring(0,200);
                for (int i = shortThings.length(); !pattern.matcher(things.substring(shortThings.indexOf("http"), i)).matches(); i--) {
                    urlWeGet = shortThings.substring(shortThings.indexOf("http"), i);
                }
            }
        }else if (things.contains("content://")){
            Uri uri=Uri.parse(things);

            urlWeGet=things;

        } else {
            //不含网址的搜索
            if (removeSpecialChars(things).length()>32){
                urlWeGet=null;
            }else {
                urlWeGet = "https://www.bing.com/search?q=" + removeSpecialChars(things);
            }
        }

        return urlWeGet;
    }

    public static String removeSpecialChars(String source){
        return source.replaceAll("\\p{Punct} *", "")
                .replaceAll("|\t|\r","")
                .replaceAll("\n","+")
                .replaceAll("\\s+","+");
    }

}
