package com.beyond.note5.utils;

import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.regex.Pattern;

/**
 * @author beyondlov1
 * @date 2019/03/30
 */
public class HtmlUtil {
    public static Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        }
        return new SpannableString(html);
    }

    public static String getTitleFromHtml(String html) {
        Log.d("HtmlUtil", html);
        Document document = Jsoup.parse(html);
        String title = document.title();
        Log.d("HtmlUtil", title);
        if (StringUtils.isNotBlank(title)) {
            return title;
        }
        return null;
    }

    @SuppressWarnings("RegExpRedundantEscape")
    public static String getUrl(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        String urlWeGet = null;
        if (content.contains("http://") || content.contains("https://")) {
            //含网址的获取网址
            //网址正则式
            Pattern pattern = Pattern.compile("^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-Z0-9\\.&%\\$\\-]+)*@)?((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,4})(\\:[0-9]+)?(/[^/][a-zA-Z0-9\\.\\,\\?\\'\\\\/\\+&%\\$#\\=~_\\-@]*)*$");

            if (content.length() < 300) {
                //小于200有网址的获取网址
                String httpContent = content.substring(content.indexOf("http"), content.length());
                if (pattern.matcher(httpContent).matches()) {
                    urlWeGet = httpContent;
                } else {
                    int i = content.length();
                    int httpIndex = content.indexOf("http");
                    String subHttpContent = content.substring(httpIndex, i);
                    while (i > httpIndex && !pattern.matcher(subHttpContent).matches()) {
                        subHttpContent = content.substring(httpIndex, i);
                        i--;
                    }
                    if (i <= httpIndex) {
                        return null;
                    }
                    return subHttpContent;
                }
            } else {
                //大于200的有网址的获取网址
                String shortThings = content.substring(0, 300);
                for (int i = shortThings.length(); !pattern.matcher(content.substring(shortThings.indexOf("http"), i)).matches(); i--) {
                    urlWeGet = shortThings.substring(shortThings.indexOf("http"), i);
                }
            }
        }
        return urlWeGet;
    }
}
