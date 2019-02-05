package com.beyond.note5.utils;

import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.beyond.note5.bean.Document;
import com.beyond.note5.view.convert.Converter;
import com.beyond.note5.view.convert.ConverterBuilder;
import com.beyond.note5.view.convert.Document2HtmlConverter;
import com.beyond.note5.view.convert.Document2MarkdownConverter;
import com.beyond.note5.view.convert.Markdown2HtmlConverter;
import com.beyond.note5.view.convert.MarkdownHtml2HtmlConverter;

import java.util.regex.Pattern;

/**
 * Created by beyond on 2019/2/3.
 */

@SuppressWarnings("unchecked")
public class WebViewUtil {

    public static final String MIME_TYPE = "text/html; charset=UTF-8";

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
    }


    public static void configWebView(WebView webView){
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                //return false 由webView加载url，return true 由应用代码处理url
                return false;
            }
        });
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
            if (things.length()>32){
                urlWeGet=null;
            }else {
                urlWeGet = "http://www.bing.com/search?q=" + things;
            }
        }
        return urlWeGet;
    }
}
