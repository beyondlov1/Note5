package com.beyond.note5.utils;

import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author beyondlov1
 * @date 2019/03/30
 */
public class HtmlUtil {
    public static Spanned fromHtml(String html){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        }
        return new SpannableString(html);
    }

    public static String getTitleFromHtml(String html){
        Log.d("HtmlUtil",html);
        Document document = Jsoup.parse(html);
        String title = document.title();
        Log.d("HtmlUtil",title);
        if (StringUtils.isNotBlank(title)){
            return title;
        }
        return null;
    }

    @SuppressWarnings("RegExpRedundantEscape")
    public static String getUrl(String content){
        if (StringUtils.isBlank(content)){
            return null;
        }
        String url = null;
        if (content.contains("http://") || content.contains("https://")) {
            //含网址的获取网址
            //网址正则式
            Pattern pattern = Pattern.compile("^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-Z0-9\\.&%\\$\\-]+)*@)?((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,4})(\\:[0-9]+)?(/[^/][a-zA-Z0-9\\.\\,\\?\\'\\\\/\\+&%\\$#\\=~_\\-@]*)*$");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()){
                url = matcher.group();
            }
        }
        return url;
    }
}
