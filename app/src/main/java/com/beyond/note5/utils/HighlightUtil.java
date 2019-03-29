package com.beyond.note5.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author beyondlov1
 * @date 2019/03/29
 */
public class HighlightUtil {

    public static String getHighlightHtml(String source, String target) {
        return getHighlightHtml(source,target,"background:lightgray;" );
    }

    public static String getHighlightHtml(String source, String target, String style) {
        target = StringUtils.trim(target);
        if (StringUtils.isNotBlank(target)) {
            return source.replace(target, "<span style='" +
                    style +
                    "'>" +
                    target +
                    "</span>");
        }
        return null;
    }

    public static String highlightTimeExpression(String source){
        return getHighlightHtml(source,TimeNLPUtil.getOriginTimeExpression(StringUtils.trim(source)));
    }
}
