package com.beyond.note5.utils;

import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
}
