package com.beyond.note5.utils;

import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;

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
}
