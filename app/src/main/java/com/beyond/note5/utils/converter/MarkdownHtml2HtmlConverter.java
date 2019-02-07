package com.beyond.note5.utils.converter;

import android.util.Log;

import com.beyond.note5.utils.ResourceUtil;

import java.io.IOException;

/**
 * @author: beyond
 * @date: 2019/2/3
 */

public class MarkdownHtml2HtmlConverter implements Converter<String,String> {

    @Override
    public String convert(String markdownHtml) {
        String html = "%s";
        try {
            String templatePath = "tpl/detail_markdown_template.html";
            html = ResourceUtil.getAssetFile(templatePath);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("loadWebContent","读取文件失败");
        }
        return String.format(html, markdownHtml);
    }
}
